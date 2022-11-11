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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.List;

import org.apache.commons.beanutils.DynaBean;
import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.CompositeSubSystemImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.DataStore;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NbiNotificationHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NetConfServerImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.inmemory.InMemoryDSM;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.LocalSubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.YangUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.yang.YangCopyConfigTest;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;

@RunWith(RequestScopeJunitRunner.class)
public class ModelNodeDynaBeanTest {

    private static final String COMPONENT_ID = "test";
    private static final String YANG_MODEL_PATH = YangCopyConfigTest.class.getResource("/yangs/example-jukebox.yang").getPath();

    private NetConfServerImpl m_server;
    private ModelNodeWithAttributes m_jukeboxModelNode;
    private SubSystemRegistry m_subSystemRegistry;

    private RootModelNodeAggregator m_runningRootModelNodeAggregator;
    private SchemaRegistry m_schemaRegistry;
    private String m_xmlFilePath;
    private NbiNotificationHelper m_nbiNotificationHelper = mock(NbiNotificationHelper.class);
    private ModelNodeHelperRegistry m_modelNodeHelperRegistry;
    private ModelNodeDataStoreManager m_dataStoreManager;

    @Before
    public void initServer() throws SchemaBuildException, ModelNodeInitException {
        m_schemaRegistry = new SchemaRegistryImpl(Collections.<YangTextSchemaSource>emptyList(), Collections.emptySet(), Collections.emptyMap(), new NoLockService());
        m_subSystemRegistry = new SubSystemRegistryImpl();
        m_subSystemRegistry.setCompositeSubSystem(new CompositeSubSystemImpl());
        m_server = new NetConfServerImpl(m_schemaRegistry);
        m_xmlFilePath = YangCopyConfigTest.class.getResource("/example-jukebox.xml").getPath();
        
        m_schemaRegistry.buildSchemaContext(TestUtil.getJukeBoxYangs(), Collections.emptySet(), Collections.emptyMap());
        m_modelNodeHelperRegistry = new ModelNodeHelperRegistryImpl(m_schemaRegistry);
        m_dataStoreManager = new InMemoryDSM(m_schemaRegistry);
        m_jukeboxModelNode = YangUtils.createInMemoryModelNode(YANG_MODEL_PATH, new LocalSubSystem(), m_modelNodeHelperRegistry,
                m_subSystemRegistry, m_schemaRegistry, m_dataStoreManager);
        m_runningRootModelNodeAggregator = new RootModelNodeAggregatorImpl(m_schemaRegistry, m_modelNodeHelperRegistry, m_dataStoreManager, m_subSystemRegistry).addModelServiceRoot(COMPONENT_ID, m_jukeboxModelNode);
        DataStore dataStore = new DataStore(StandardDataStores.RUNNING, m_runningRootModelNodeAggregator, m_subSystemRegistry );
        dataStore.setNbiNotificationHelper(m_nbiNotificationHelper);
        m_server.setRunningDataStore(dataStore);
        YangUtils.loadXmlDataIntoServer(m_server, m_xmlFilePath, StandardDataStores.RUNNING);        
    }

	@Test
    public void testDynaBean() throws ModelNodeInitException {
        DynaBean jukeboxBean = ModelNodeDynaBeanFactory.getDynaBean(m_jukeboxModelNode);
        assertEquals("jukebox", jukeboxBean.getDynaClass().getName());
        List<Object> libraryObjs = (List<Object>) jukeboxBean.get("library");
        Object libraryObj = libraryObjs.listIterator().next();
        assertTrue(libraryObj instanceof DynaBean);
        
        DynaBean libraryBean = (DynaBean)libraryObj;
        assertEquals("library", libraryBean.getDynaClass().getName());
        assertEquals(jukeboxBean.toString(), libraryBean.get(ModelNodeWithAttributes.PARENT).toString());
        Object artistObj = libraryBean.get("artist", 0);
        assertTrue(artistObj instanceof DynaBean);
        
        
        DynaBean artistBean = (DynaBean)artistObj;
        assertEquals("artist", artistBean.getDynaClass().getName());
        assertEquals(libraryBean, artistBean.get(ModelNodeWithAttributes.PARENT));
        assertEquals("Lenny", artistBean.get("name"));
        assertEquals(1,artistBean.get(ModelNodeWithAttributes.LEAF_COUNT));
        assertEquals(0,artistBean.get(ModelNodeWithAttributes.LEAF_LIST_COUNT));
    }

}
