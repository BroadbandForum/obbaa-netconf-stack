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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.yang;

import static junit.framework.TestCase.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.api.server.NetconfQueryParams;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder;
import org.broadband_forum.obbaa.netconf.mn.fwk.ChangeTreeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.AbstractSubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ChangeNotification;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeChangeType;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemValidationException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.DataStoreException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.StateAttributeUtil;
import org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A jukebox subsystem that uses DSM to get the various counts.
 * Created by pgorai on 4/4/16.
 */
public class DsmJukeboxSubsystem extends AbstractSubSystem{

	private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(DsmJukeboxSubsystem.class, LogAppNames.NETCONF_STACK);
    private SchemaPath m_jukeboxSchemaPath;
    private SchemaPath m_librarySchemaPath;
    private SchemaPath m_artistSchemaPath;
    private SchemaPath m_albumShcemaPath;
    private SchemaPath m_songSchemaPath;

    private QName m_songCountQName;
    private QName m_albumCountQName;
    private QName m_artistCountQName;

    private ModelNodeDataStoreManager m_modelNodeDsm;
    private ModelNodeId m_refLibrary;
    private SchemaRegistry m_schemaRegistry;
    private Map<SchemaPath, List<ChangeTreeNode>> m_changesMap = new HashMap<>();;

    public DsmJukeboxSubsystem(ModelNodeDataStoreManager dsm, String jukeBoxNamespace, SchemaRegistry schemaRegistry){
        m_modelNodeDsm = dsm;
        m_refLibrary = new ModelNodeId("/container=jukebox/container=library", jukeBoxNamespace);
        m_schemaRegistry = schemaRegistry;
        updateQname(jukeBoxNamespace);
        updateSchemaPath(jukeBoxNamespace);
    }

    @Override
    protected boolean byPassAuthorization() {
        return true;
    }

    @Override
    public void postCommit(Map<SchemaPath, List<ChangeTreeNode>> changesMap) {
        clearChanges();
        m_changesMap.putAll(changesMap);
    }

    private void clearChanges() {
        m_changesMap.clear();
    }

    public void assertChangeTreeNodeForGivenSchemaPath(SchemaPath schemaPath, List<String> changeTreeNodes) {
        boolean changeFound;
        List<ChangeTreeNode> changeTreeNodeList = m_changesMap.get(schemaPath);
        for(String ctn : changeTreeNodes) {
            changeFound = false;
            List<String> changeTreeNodePrints = new ArrayList<>();
            for(ChangeTreeNode changeTreeNode : changeTreeNodeList) {
                changeTreeNodePrints.add(changeTreeNode.print());
                if(changeTreeNode.print().equals(ctn)) {
                    changeFound = true;
                    break;
                }
            }
            if(!changeFound) {
                fail("Specified ChangeTreeNodes Not found.\n\nExpected ChangeTreeNodes:\n\n" + changeTreeNodes.toString() + "\n\nActual ChangeTreeNodes:\n\n" + changeTreeNodePrints.toString());
            }
        }
    }

    @Override
    public void notifyChanged(List<ChangeNotification> changeNotificationList) {

    }

    @Override
    public Map<ModelNodeId, List<Element>> retrieveStateAttributes(Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> attributes,NetconfQueryParams queryParams) throws GetAttributeException {
        Map<ModelNodeId, List<Element>> stateInfo = new HashMap<>();
        List<ModelNode> modelNodeList = null;
        try {
            modelNodeList = m_modelNodeDsm.listNodes(m_librarySchemaPath, m_schemaRegistry);
        } catch (DataStoreException e) {
            LOGGER.error("Error while listing library nodes..",e);
            return stateInfo;
        }

        Document doc = DocumentUtils.createDocument();
        for (Map.Entry<ModelNodeId, Pair<List<QName>, List<FilterNode>>> entry : attributes.entrySet()) {
            ModelNodeId nodeId = entry.getKey();
            ModelNodeId library = modelNodeList.get(0).getModelNodeId();
            if (library.beginsWithTemplate(m_refLibrary)) {
                Map<QName, Object> stateAttributes = new HashMap<QName, Object>();
                try {
                    List<ModelNode> artistList = m_modelNodeDsm.listChildNodes(m_artistSchemaPath, library, m_schemaRegistry);
                    int artistCount = artistList.size();
                    int albumCount = 0;
                    int songCount = 0;
                    for (ModelNode artist : artistList) {
                        List<ModelNode> albumLists = m_modelNodeDsm.listChildNodes(m_albumShcemaPath, artist.getModelNodeId(), m_schemaRegistry);
                        albumCount += albumLists.size();
                        for (ModelNode album : albumLists) {
                            List<ModelNode> songList = m_modelNodeDsm.listChildNodes(m_songSchemaPath, album.getModelNodeId(), m_schemaRegistry);
                            songCount += songList.size();
                        }
                    }
                    stateAttributes.put(m_artistCountQName, artistCount);
                    stateAttributes.put(m_albumCountQName, albumCount);
                    stateAttributes.put(m_songCountQName, songCount);
                    List<Element> stateElements = StateAttributeUtil.convertToStateElements(stateAttributes, "jbox", doc);
                    stateInfo.put(nodeId, stateElements);
                } catch (DataStoreException e) {
                    LOGGER.error("Error while listing library child nodes..",e);
                }
            }
        }
        return stateInfo;
    }

    @Override
    protected Map<ModelNodeId, List<Element>> retrieveStateAttributes(Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> attributes) throws GetAttributeException {
        return null;
    }

    @Override
    public void testChange(EditContext editContext, ModelNodeChangeType changeType, ModelNode changedNode, ModelNodeHelperRegistry modelNodeHelperRegistry) throws SubSystemValidationException {

    }

    @Override
    public boolean handleChanges(EditContext editContext, ModelNodeChangeType changeType, ModelNode changedNode) {
        return true;
    }

    private void updateQname(String jukeBoxNamespace) {
        m_songCountQName = QName.create(jukeBoxNamespace, "2014-07-03", "song-count");
        m_albumCountQName = QName.create(jukeBoxNamespace, "2014-07-03", "album-count");
        m_artistCountQName = QName.create(jukeBoxNamespace, "2014-07-03", "artist-count");
    }

    private void updateSchemaPath(String jukeBoxNamespace) {
        m_jukeboxSchemaPath = SchemaPath.create(true, QName.create(jukeBoxNamespace, JukeboxConstants.JB_REVISION, JukeboxConstants.JUKEBOX_LOCAL_NAME));
        m_librarySchemaPath = new SchemaPathBuilder().withParent(m_jukeboxSchemaPath).appendLocalName(JukeboxConstants.LIBRARY_LOCAL_NAME).build();
        m_artistSchemaPath = new SchemaPathBuilder().withParent(m_librarySchemaPath).appendLocalName(JukeboxConstants.ARTIST_LOCAL_NAME).build();
        m_albumShcemaPath = new SchemaPathBuilder().withParent(m_artistSchemaPath).appendLocalName(JukeboxConstants.ALBUM_LOCAL_NAME).build();
        m_songSchemaPath = new SchemaPathBuilder().withParent(m_albumShcemaPath).appendLocalName(JukeboxConstants.SONG_LOCAL_NAME).build();
    }
}
