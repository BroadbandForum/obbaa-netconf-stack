/*
 * Copyright 2018 Broadband Forum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.broadband_forum.obbaa.netconf.server.model.notification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opendaylight.yangtools.yang.common.QName;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationService;
import org.broadband_forum.obbaa.netconf.api.server.notification.Stream;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.AbstractSubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterMatchNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;

/**
 * NotificationSubSystem implements state data retrieval state sub tree /netconf/streams
 *
 * @author gnanavek
 */
public class NotificationSubSystem extends AbstractSubSystem {

    private static final String NC_NOTIFICATION_PREFIX = "manageEvent";

    private static final String STREAMS = "streams";
    public static final String STREAM = "stream";
    private static final String STREAM_NAME = "name";
    private static final String STREAM_DESCRIPTION = "description";
    private static final String STREAM_RELAY_SUPPORT = "replaySupport";
    private static final String STREAM_RELAY_LOG_AGE_TIME = "replayLogAgedTime";
    private static final String STREAM_RELAY_LOG_CREATION_TIME = "replayLogCreationTime";

    private List<Stream> m_streamList;

    public NotificationSubSystem(NotificationService notificationService, NotificationLogger notificationLogger) {
        m_streamList = notificationService.getSupportedStreams();
    }

    @Override
    public Map<ModelNodeId, List<Element>>  retrieveStateAttributes( Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> mapAttributes) throws GetAttributeException {
        Map<ModelNodeId, List<Element>> stateInfo = new HashMap<>();

        for (Map.Entry<ModelNodeId, Pair<List<QName>, List<FilterNode>>> entry : mapAttributes.entrySet()) {
            Pair<List<QName>, List<FilterNode>> stateAttrs = entry.getValue();
            List<FilterNode> filters = stateAttrs.getSecond();
            List<Element> stateSubTreeElements = retrieveStateSubTree(filters);
            stateInfo.put(entry.getKey(), stateSubTreeElements);
        }

        return stateInfo;
    }

    private List<Element> retrieveStateSubTree(List<FilterNode> stateSubTreeFilters) {
        List<Element> result = new ArrayList<>();
        Document document = DocumentUtils.createDocument();
        for (FilterNode filterNode : stateSubTreeFilters) {
            if (filterNode.getNodeName().equals(STREAMS)) {
                List<FilterNode> selectNodes = filterNode.getSelectNodes();
                List<FilterNode> childNodes = filterNode.getChildNodes();
                Element streamsElement = document.createElementNS(NetconfResources.NC_NOTIFICATION_NS, STREAMS);
                streamsElement.setPrefix(NC_NOTIFICATION_PREFIX);
                result.add(streamsElement);
                if (selectNodes.isEmpty() && childNodes.isEmpty()) {
                    // copy everything
                    for (Stream stream : m_streamList) {
                        streamsElement.appendChild(getStreamElement(stream,document));
                    }
                } else {
                    for (FilterNode childNode : childNodes) {
                        for (FilterMatchNode filterMatchNode : childNode.getMatchNodes()) {
                            Stream filteredStream = getStreamByName(filterMatchNode.getFilter());
                            if(null != filteredStream){
                                streamsElement.appendChild(getStreamElement(filteredStream, document));
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    private Stream getStreamByName(String streamName){
        for (Stream stream : m_streamList) {
            if(stream.getName().equals(streamName)){
                return stream;
            }
        }
        return null;
    }
    
    private Element getStreamElement(Stream stream, Document document) {
        List<Pair<String, Object>> children = new ArrayList<>();
        children.add(new Pair<String, Object>(STREAM_NAME, stream.getName()));
        children.add(new Pair<String, Object>(STREAM_DESCRIPTION, stream.getDescription()));
        children.add(new Pair<String, Object>(STREAM_RELAY_SUPPORT, stream.getReplaySupport()));
        children.add(new Pair<String, Object>(STREAM_RELAY_LOG_AGE_TIME, stream.getReplayLogAgedTime()));
        children.add(new Pair<String, Object>(STREAM_RELAY_LOG_CREATION_TIME, stream.getReplayLogCreationTime()));
        Element element = DocumentUtils.getElement(document, STREAM, children, NetconfResources.NC_NOTIFICATION_NS, NC_NOTIFICATION_PREFIX);
        return element;

    }

}