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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.broadband_forum.obbaa.netconf.api.server.NetconfQueryParams;
import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationService;
import org.broadband_forum.obbaa.netconf.api.server.notification.Stream;

import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn;

/**
 * Created by nhtoan on 2/19/16.
 */
public class NotificationSubSystemTest {
    private static final QName STREAM_NAME_QNAME = QName.create(NetconfResources.NC_NOTIFICATION_NS, "2008-07-14", "name");
    private static final QName STREAMS_QNAME = QName.create(NetconfResources.NC_NOTIFICATION_NS, "2008-07-14", "streams");
    private List<Stream> m_streamList;
    private NotificationLogger m_notificationLogger;
    private NotificationService m_notificationService;

    @Before
    public void setUp() throws Exception {
        m_streamList = new ArrayList<>();
        Stream netconfStream = new Stream();
        netconfStream.setName("NETCONF");
        m_streamList.add(netconfStream);
        Stream alarmStream = new Stream();
        alarmStream.setName("ALARM");
        m_streamList.add(alarmStream);
        m_notificationLogger = mock(NotificationLogger.class);
        m_notificationService = mock(NotificationService.class);
        when(m_notificationService.getSupportedStreams()).thenReturn(m_streamList);
    }

    @Test
    public void testRetrieveSupportedStreams() throws GetAttributeException, NetconfMessageBuilderException {
        ModelNodeRdn rdns1 = new ModelNodeRdn("container", NetconfResources.NC_NOTIFICATION_NS, "netconf");
        ModelNodeRdn rdns2 = new ModelNodeRdn("container", NetconfResources.NC_NOTIFICATION_NS, "streams");
        List<ModelNodeRdn> rdns = Arrays.asList(rdns1, rdns2);
        ModelNodeId modeNodeId = new ModelNodeId(rdns);
        List<QName> stateQNames = new ArrayList<>();
        stateQNames.add(STREAMS_QNAME);

        Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> attributes = new HashMap<>();
        List<FilterNode> filter = new ArrayList<>();
        FilterNode filterNode = mock(FilterNode.class);
        when(filterNode.getNodeName()).thenReturn(STREAMS_QNAME.getLocalName());
        filter.add(filterNode);

        attributes.put(modeNodeId, new Pair<List<QName>, List<FilterNode>>(stateQNames, filter));

        NotificationSubSystem notificationSubSystem = new NotificationSubSystem(m_notificationService, m_notificationLogger);
        Map<ModelNodeId, List<Element>> result = notificationSubSystem.retrieveStateAttributes(attributes, NetconfQueryParams.NO_PARAMS);

        assertNotNull(result);
        assertEquals(1, result.get(modeNodeId).size());
        Element streamsElement = result.get(modeNodeId).get(0);
        NodeList streamList = streamsElement.getElementsByTagName("manageEvent:stream");

        assertEquals(2, streamList.getLength());
        for (int i = 0; i < streamList.getLength(); i++) {
            Element element = (Element) streamList.item(i);
            assertNotNull(element.getElementsByTagName("manageEvent:name"));
            assertNotNull(element.getElementsByTagName("manageEvent:description"));
            assertNotNull(element.getElementsByTagName("manageEvent:replaySupport"));
            assertNotNull(element.getElementsByTagName("manageEvent:replayLogCreationTime"));
        }
    }
}
