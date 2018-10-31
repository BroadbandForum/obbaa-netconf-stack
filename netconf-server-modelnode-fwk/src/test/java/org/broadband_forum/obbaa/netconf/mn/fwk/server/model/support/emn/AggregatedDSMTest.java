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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn;

import static org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.TestConstants.EMPTY_NODE_ID;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ALBUM_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ARTIST_LOCAL_NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JB_NS;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JUKEBOX_LOCAL_NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JUKEBOX_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.LIBRARY_LOCAL_NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.LIBRARY_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.NAME_QNAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.NAME;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.utils.AnnotationAnalysisException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.DataStoreException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDSMRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDSMRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.GenericConfigAttribute;


public class AggregatedDSMTest {

    private ModelNodeDataStoreManager m_mnDSM1;
    private ModelNodeDataStoreManager m_mnDSM2;
    private ModelNodeDSMRegistry m_modelNodeDSMRegistry;
    private AggregatedDSM m_aggregatedDSM;
    private XmlModelNodeImpl m_jukeboxModelNode;
    private XmlModelNodeImpl m_albumModelNode;
    private ModelNodeDataStoreManager m_mnDSM3;
    private SchemaRegistry m_schemaRegistry;

    @Before
    public void setUp() throws AnnotationAnalysisException {
        m_mnDSM1 = mock(ModelNodeDataStoreManager.class);
        m_mnDSM2 = mock(ModelNodeDataStoreManager.class);
        m_mnDSM3 = mock(ModelNodeDataStoreManager.class);
        m_modelNodeDSMRegistry = new ModelNodeDSMRegistryImpl();
        m_aggregatedDSM = new AggregatedDSM(m_modelNodeDSMRegistry);
        m_schemaRegistry = mock(SchemaRegistry.class);

        m_modelNodeDSMRegistry.register("Jukebox", JUKEBOX_SCHEMA_PATH, m_mnDSM1);
        m_modelNodeDSMRegistry.register("Jukebox", ALBUM_SCHEMA_PATH, m_mnDSM2);
        m_modelNodeDSMRegistry.register("Jukebox", LIBRARY_SCHEMA_PATH, m_mnDSM3);

        DataSchemaNode jukeboxSchemaNode = mock(ContainerSchemaNode.class);
        when(m_schemaRegistry.getDataSchemaNode(JUKEBOX_SCHEMA_PATH)).thenReturn(jukeboxSchemaNode);
        DataSchemaNode albumSchemaNode = mock(ContainerSchemaNode.class);
        when(m_schemaRegistry.getDataSchemaNode(ALBUM_SCHEMA_PATH)).thenReturn(albumSchemaNode);
        m_jukeboxModelNode = new XmlModelNodeImpl(JUKEBOX_SCHEMA_PATH, Collections.emptyMap(), null, null, null,
                null, null, m_schemaRegistry, null, null);
        m_albumModelNode = new XmlModelNodeImpl(ALBUM_SCHEMA_PATH, Collections.emptyMap(), null, null, null, null,
                null, m_schemaRegistry, null, null);
    }

    @Test
    public void testDSMDelegationListNodes() throws DataStoreException {

        m_aggregatedDSM.listNodes(JUKEBOX_SCHEMA_PATH);
        verify(m_mnDSM1, times(1)).listNodes(JUKEBOX_SCHEMA_PATH);

        m_aggregatedDSM.listNodes(ALBUM_SCHEMA_PATH);
        verify(m_mnDSM2, times(1)).listNodes(ALBUM_SCHEMA_PATH);
    }

    @Test
    public void testDSMDelegationListChildNodes() throws DataStoreException {

        m_aggregatedDSM.listChildNodes(JUKEBOX_SCHEMA_PATH, EMPTY_NODE_ID);
        verify(m_mnDSM1, times(1)).listChildNodes(JUKEBOX_SCHEMA_PATH, EMPTY_NODE_ID);

        m_aggregatedDSM.listChildNodes(ALBUM_SCHEMA_PATH, EMPTY_NODE_ID);
        verify(m_mnDSM2, times(1)).listChildNodes(ALBUM_SCHEMA_PATH, EMPTY_NODE_ID);
    }

    @Test
    public void testDSMDelegationFindNode() throws DataStoreException {

        m_aggregatedDSM.findNode(JUKEBOX_SCHEMA_PATH, null, EMPTY_NODE_ID);
        verify(m_mnDSM1, times(1)).findNode(JUKEBOX_SCHEMA_PATH, null, EMPTY_NODE_ID);

        m_aggregatedDSM.findNode(ALBUM_SCHEMA_PATH, null, EMPTY_NODE_ID);
        verify(m_mnDSM2, times(1)).findNode(ALBUM_SCHEMA_PATH, null, EMPTY_NODE_ID);
    }

    @Test
    public void testDSMDelegationCreateNode() throws DataStoreException {

        m_aggregatedDSM.createNode(m_jukeboxModelNode, EMPTY_NODE_ID);
        verify(m_mnDSM1, times(1)).createNode(m_jukeboxModelNode, EMPTY_NODE_ID);

        m_aggregatedDSM.createNode(m_albumModelNode, EMPTY_NODE_ID);
        verify(m_mnDSM2, times(1)).createNode(m_albumModelNode, EMPTY_NODE_ID);
    }

    @Test
    public void testDSMDelegationUpdateNode() throws DataStoreException {

        m_aggregatedDSM.updateNode(m_jukeboxModelNode, EMPTY_NODE_ID, null, null, false);
        verify(m_mnDSM1, times(1)).updateNode(m_jukeboxModelNode, EMPTY_NODE_ID, null, null, false);

        m_aggregatedDSM.updateNode(m_albumModelNode, EMPTY_NODE_ID, null, null, false);
        verify(m_mnDSM2, times(1)).updateNode(m_albumModelNode, EMPTY_NODE_ID, null, null, false);
    }

    @Test
    public void testDSMDelegationRemoveNode() throws DataStoreException {

        m_aggregatedDSM.removeNode(m_jukeboxModelNode, EMPTY_NODE_ID);
        verify(m_mnDSM1, times(1)).removeNode(m_jukeboxModelNode, EMPTY_NODE_ID);

        m_aggregatedDSM.removeNode(m_albumModelNode, EMPTY_NODE_ID);
        verify(m_mnDSM2, times(1)).removeNode(m_albumModelNode, EMPTY_NODE_ID);
    }

    @Test
    public void testDSMDelegationRemoveAllNodes() throws DataStoreException {
        m_aggregatedDSM.removeAllNodes(m_jukeboxModelNode, LIBRARY_SCHEMA_PATH, EMPTY_NODE_ID);
        verify(m_mnDSM3, times(1)).removeAllNodes(m_jukeboxModelNode, LIBRARY_SCHEMA_PATH, EMPTY_NODE_ID);

        m_aggregatedDSM.removeAllNodes(m_jukeboxModelNode, ALBUM_SCHEMA_PATH, EMPTY_NODE_ID);
        verify(m_mnDSM2, times(1)).removeAllNodes(m_jukeboxModelNode, ALBUM_SCHEMA_PATH, EMPTY_NODE_ID);
    }

    @Test
    public void testDSMDelegationFindNodes() throws DataStoreException {

        m_aggregatedDSM.findNodes(JUKEBOX_SCHEMA_PATH, null, EMPTY_NODE_ID);
        verify(m_mnDSM1, times(1)).findNodes(JUKEBOX_SCHEMA_PATH, null, EMPTY_NODE_ID);

        m_aggregatedDSM.findNodes(ALBUM_SCHEMA_PATH, null, EMPTY_NODE_ID);
        verify(m_mnDSM2, times(1)).findNodes(ALBUM_SCHEMA_PATH, null, EMPTY_NODE_ID);
    }

    @Test
    public void testBeginEditDelegation() {
        m_aggregatedDSM.beginModify();
        verify(m_mnDSM1).beginModify();
        verify(m_mnDSM2).beginModify();
        verify(m_mnDSM3).beginModify();
    }

    @Test
    public void testEndEditDelegation() {
        m_aggregatedDSM.endModify();
        verify(m_mnDSM1).endModify();
        verify(m_mnDSM2).endModify();
        verify(m_mnDSM3).endModify();
    }

    //@Test
    public void testFindNodes_CacheHasEmptyList() {
        ModelNodeId parentId = new ModelNodeId(Arrays.asList(
                new ModelNodeRdn("container", JB_NS, JUKEBOX_LOCAL_NAME),
                new ModelNodeRdn("container", JB_NS, LIBRARY_LOCAL_NAME), new ModelNodeRdn("container",
                        JB_NS, ARTIST_LOCAL_NAME), new ModelNodeRdn(NAME_QNAME, "artist1")));
        DataSchemaNode albumSchemaNode = mock(ListSchemaNode.class);
        when(m_schemaRegistry.getDataSchemaNode(ALBUM_SCHEMA_PATH)).thenReturn(albumSchemaNode);
        ModelNodeWithAttributes albumModelNode = new ModelNodeWithAttributes(ALBUM_SCHEMA_PATH, parentId, null, null,
                m_schemaRegistry, null);
        Map<QName, ConfigLeafAttribute> keyAttributes = new HashMap<>();
        keyAttributes.put(NAME_QNAME,new GenericConfigAttribute(NAME, JB_NS, "album1"));
        albumModelNode.setAttributes(keyAttributes);
        m_aggregatedDSM.createNode(albumModelNode, parentId);

        Map<QName, ConfigLeafAttribute> matchCriteria = new HashMap<>();
        keyAttributes.put(NAME_QNAME,new GenericConfigAttribute(NAME, JB_NS, "album1"));
        m_aggregatedDSM.findNodes(ALBUM_SCHEMA_PATH, matchCriteria, parentId);
        when(m_mnDSM2.findNodes(ALBUM_SCHEMA_PATH, matchCriteria, parentId)).thenReturn(Collections.singletonList
                (albumModelNode));

        m_aggregatedDSM.findNodes(ALBUM_SCHEMA_PATH, matchCriteria, parentId);
        verify(m_mnDSM2, times(2)).findNodes(ALBUM_SCHEMA_PATH, matchCriteria, parentId);

        // subsequent calls
        m_aggregatedDSM.findNodes(ALBUM_SCHEMA_PATH, matchCriteria, parentId);
        verify(m_mnDSM2, times(2)).findNodes(ALBUM_SCHEMA_PATH, matchCriteria, parentId);

    }
}
