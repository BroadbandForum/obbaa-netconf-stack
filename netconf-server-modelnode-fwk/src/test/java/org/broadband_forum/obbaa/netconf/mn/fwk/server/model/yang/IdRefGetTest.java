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

import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.verifyGet;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.List;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NbiNotificationHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.LocalSubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.xml.sax.SAXException;

import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.DataStore;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NetConfServerImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregatorImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.inmemory.InMemoryDSM;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.YangUtils;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcPayloadConstraintParser;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;

public class IdRefGetTest {
    private NetConfServerImpl m_server;
    private SubSystemRegistry m_subSystemRegistry = new SubSystemRegistryImpl();
    private SchemaRegistry m_schemaRegistry;
    private String m_componentId = "test";
    private ModelNodeHelperRegistry m_modelNodeHelperRegistry = new ModelNodeHelperRegistryImpl(m_schemaRegistry);

    @Before
    public void initServer() throws SchemaBuildException, ModelNodeInitException {
        List<YangTextSchemaSource> yangs = TestUtil.getJukeBoxDeps();
        yangs.add(TestUtil.getByteSource("/idreftest/example-jukebox.yang"));
        m_schemaRegistry = new SchemaRegistryImpl(yangs, new NoLockService());
        m_modelNodeHelperRegistry = new ModelNodeHelperRegistryImpl(m_schemaRegistry);
        m_server = new NetConfServerImpl(m_schemaRegistry, mock(RpcPayloadConstraintParser.class));
        ModelNodeDataStoreManager modelNodeDsm = new InMemoryDSM(m_schemaRegistry);
        RootModelNodeAggregator rootModelNodeAggregator = new RootModelNodeAggregatorImpl(m_schemaRegistry,
                m_modelNodeHelperRegistry, modelNodeDsm, m_subSystemRegistry);
        String yangFilePath = TestUtil.class.getResource("/idreftest/example-jukebox.yang").getPath();
        ModelNode yangModel = YangUtils.createInMemoryModelNode(yangFilePath, new LocalSubSystem(),
                m_modelNodeHelperRegistry,
                m_subSystemRegistry, m_schemaRegistry, modelNodeDsm);
        rootModelNodeAggregator.addModelServiceRoot(m_componentId, yangModel);
        DataStore dataStore = new DataStore(StandardDataStores.RUNNING, rootModelNodeAggregator, m_subSystemRegistry);
        NbiNotificationHelper nbiNotificationHelper = mock(NbiNotificationHelper.class);
        dataStore.setNbiNotificationHelper(nbiNotificationHelper);
        m_server.setRunningDataStore(dataStore);

        String xmlFilePath = TestUtil.class.getResource("/idreftest/example-jukebox-idref.xml").getPath();

        YangUtils.loadXmlDataIntoServer(m_server, xmlFilePath);

    }

    @Test
    public void testGet() throws SAXException, IOException {
        verifyGet(m_server, "/idreftest/filter-matching-genre.xml", "/idreftest/get-with-idref.xml", "1");
    }


}
