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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.commands;

import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.YangUtils.loadXmlDataIntoServer;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ALBUM_QNAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ALBUM_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ARTIST_QNAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ARTIST_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JB_NS;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JUKEBOX_LOCAL_NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JUKEBOX_QNAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JUKEBOX_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.LIBRARY_QNAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.LIBRARY_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.SONG_QNAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.SONG_SCHEMA_PATH;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigOperations;
import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.mn.fwk.AttributeIndex;
import org.broadband_forum.obbaa.netconf.mn.fwk.ChangeTreeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.CompositeSubSystemImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.DataStore;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditChangeSource;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterMatchNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NbiNotificationHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NetConfServerImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NotificationContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.TestEditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregatorImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.inmemory.InMemoryDSM;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.LocalSubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.YangUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcPayloadConstraintParser;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;

@RunWith(RequestScopeJunitRunner.class)
public abstract class AbstractCommandTest {

    public static final String EXAMPLE_JUKEBOX_YANG = "/yangs/example-jukebox.yang";
    public static final String XML_PATH = TestUtil.class.getResource("/example-jukebox.xml").getPath();
    public static final ModelNodeId JUKEBOX_MNID = new ModelNodeId("/container=jukebox", JB_NS);
    public static final ModelNodeId LIBRARY_MNID = new ModelNodeId("/container=jukebox/container=library", JB_NS);
    public static final ModelNodeId LENNY_MNID = new ModelNodeId("/container=jukebox/container=library/container=artist/name=Lenny", JB_NS);
    public static final ModelNodeId CIRCUS_MNID = new ModelNodeId("/container=jukebox/container=library/" +
            "container=artist/name=Lenny/container=album/name=Circus", JB_NS);
    public static final ModelNodeId GREATEST_HITS_ID = new ModelNodeId("/container=jukebox/container=library/" +
            "container=artist/name=Lenny/container=album/name=Greatest Hits", JB_NS);

    public NetConfServerImpl m_netconfServer;
    public SubSystemRegistry m_subSystemRegistry;
    public InMemoryDSM m_modelNodeDsm;
    public InMemoryDSM m_modelNodeDsmSpy;
    public RootModelNodeAggregator m_rootModelNodeAggregator;
    public ModelNodeHelperRegistry m_modelNodeHelperRegistry;
    public SchemaRegistry m_schemaRegistry;
    public NotificationContext m_notificationContext;
    public NetconfClientInfo m_clientInfo;
    public Map<SchemaPath, Map<ModelNodeId, ChangeTreeNode>> m_nodesOfType;
    public Map<SchemaPath, Map<ModelNodeId, ChangeTreeNode>> m_nodesOfTypeWithinSchemaMount;
    public Map<AttributeIndex, Set<ChangeTreeNode>> m_attrIndex;
    public Set<SchemaPath> m_changedNodeSPs;
    public Map<String, Object> m_contextMap;
    public EditContainmentNode m_editNode;

    public ModelNode m_jukeboxNode;
    public ModelNode m_libraryNode;
    public ModelNode m_artistNode;
    public ModelNode m_albumNode;
    public ModelNode m_songNode;

    public DataSchemaNode m_jukeboxDSN;
    public DataSchemaNode m_libraryDSN;
    public DataSchemaNode m_artistDSN;
    public DataSchemaNode m_albumDSN;
    public DataSchemaNode m_songDSN;

    @Mock
    public EditContext m_editContext;

    @Before
    public void setup() throws SchemaBuildException, ModelNodeInitException {
        MockitoAnnotations.initMocks(this);
        List<YangTextSchemaSource> yangs = TestUtil.getJukeBoxDeps();
        yangs.add(TestUtil.getByteSource(EXAMPLE_JUKEBOX_YANG));
        m_schemaRegistry = new SchemaRegistryImpl(yangs, Collections.emptySet(), Collections.emptyMap(), new NoLockService());
        m_modelNodeHelperRegistry = new ModelNodeHelperRegistryImpl(m_schemaRegistry);
        m_subSystemRegistry = new SubSystemRegistryImpl();
        m_subSystemRegistry.setCompositeSubSystem(new CompositeSubSystemImpl());
        m_netconfServer = new NetConfServerImpl(m_schemaRegistry, mock(RpcPayloadConstraintParser.class));
        m_modelNodeDsm = new InMemoryDSM(m_schemaRegistry);
        m_modelNodeDsmSpy = spy(m_modelNodeDsm);
        m_jukeboxNode = YangUtils.createInMemoryModelNode(TestUtil.class.getResource(EXAMPLE_JUKEBOX_YANG).getPath(), new LocalSubSystem(), m_modelNodeHelperRegistry,
                m_subSystemRegistry, m_schemaRegistry, m_modelNodeDsmSpy);

        m_rootModelNodeAggregator = new RootModelNodeAggregatorImpl(m_schemaRegistry, m_modelNodeHelperRegistry, m_modelNodeDsmSpy, m_subSystemRegistry);
        m_rootModelNodeAggregator.addModelServiceRoot(JUKEBOX_LOCAL_NAME, m_jukeboxNode);
        DataStore dataStore = new DataStore(StandardDataStores.RUNNING, m_rootModelNodeAggregator, m_subSystemRegistry);
        NbiNotificationHelper nbiNotificationHelper = mock(NbiNotificationHelper.class);
        dataStore.setNbiNotificationHelper(nbiNotificationHelper);
        m_netconfServer.setRunningDataStore(dataStore);
        m_notificationContext = new NotificationContext();
        m_clientInfo = new NetconfClientInfo("UT", 1);
        loadXmlDataIntoServer(m_netconfServer, XML_PATH);

        m_libraryNode = m_jukeboxNode.getChildModelNode(LIBRARY_QNAME);
        m_artistNode = m_libraryNode.getChildModelNodes(ARTIST_QNAME, Collections.singletonList(new FilterMatchNode(NAME, JB_NS, "Lenny"))).iterator().next();
        m_albumNode = m_artistNode.getChildModelNodes(ALBUM_QNAME, Collections.singletonList(new FilterMatchNode(NAME, JB_NS, "Greatest hits"))).iterator().next();
        m_songNode = m_albumNode.getChildModelNodes(SONG_QNAME, Collections.singletonList(new FilterMatchNode(NAME, JB_NS, "Fly Away"))).iterator().next();

        m_editNode = new TestEditContainmentNode(JUKEBOX_QNAME, EditConfigOperations.MERGE, m_schemaRegistry);
        m_editNode.setChangeSource(EditChangeSource.user);
        when(m_editContext.getEditNode()).thenReturn(m_editNode);

        m_jukeboxDSN = m_schemaRegistry.getDataSchemaNode(JUKEBOX_SCHEMA_PATH);
        m_libraryDSN = m_schemaRegistry.getDataSchemaNode(LIBRARY_SCHEMA_PATH);
        m_artistDSN = m_schemaRegistry.getDataSchemaNode(ARTIST_SCHEMA_PATH);
        m_albumDSN = m_schemaRegistry.getDataSchemaNode(ALBUM_SCHEMA_PATH);
        m_songDSN = m_schemaRegistry.getDataSchemaNode(SONG_SCHEMA_PATH);

        m_changedNodeSPs = new HashSet<>();
        m_attrIndex = new HashMap<>();
        m_nodesOfType = new HashMap<>();
        m_nodesOfTypeWithinSchemaMount = new HashMap<>();
        m_contextMap = new HashMap<>();
    }
}
