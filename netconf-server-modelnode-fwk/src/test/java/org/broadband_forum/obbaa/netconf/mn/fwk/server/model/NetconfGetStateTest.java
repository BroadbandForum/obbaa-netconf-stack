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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.YangUtils.createInMemoryModelNode;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.Collections;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.LocalSubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregatorImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.YangUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.xml.sax.SAXException;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.GetRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.inmemory.InMemoryDSM;

public class NetconfGetStateTest {

    private NetConfServerImpl m_server;
    private SubSystemRegistry m_subSystemRegistry = new SubSystemRegistryImpl();
    private SchemaRegistry m_schemaRegistry;
    private String m_componentId = "test";
    private NbiNotificationHelper m_nbiNotificationHelper;
    private ModelNodeHelperRegistry m_modelNodeHelperRegistry = new ModelNodeHelperRegistryImpl(m_schemaRegistry);

    @Before
    public void initServer() throws SchemaBuildException, ModelNodeInitException {
        m_schemaRegistry = new SchemaRegistryImpl(Collections.<YangTextSchemaSource>emptyList(), new NoLockService());
        m_server = new NetConfServerImpl(m_schemaRegistry);
        RootModelNodeAggregator rootModelNodeAggregator = new RootModelNodeAggregatorImpl(m_schemaRegistry,
                m_modelNodeHelperRegistry,
                mock(ModelNodeDataStoreManager.class), m_subSystemRegistry);
        String yangFilePath = TestUtil.class.getResource("/yangs/example-jukebox.yang").getPath();
        String xmlFilePath = TestUtil.class.getResource("/example-jukebox.xml").getPath();
        m_schemaRegistry.loadSchemaContext("jukebox", TestUtil.getJukeBoxYangs(), Collections.emptySet(), Collections
                .emptyMap());

        ModelNodeWithAttributes jukeBoxWithYang = createInMemoryModelNode(yangFilePath, new LocalSubSystem(),
                m_modelNodeHelperRegistry,
                m_subSystemRegistry, m_schemaRegistry, new InMemoryDSM(m_schemaRegistry));
        rootModelNodeAggregator.addModelServiceRoot(m_componentId, jukeBoxWithYang);
        m_nbiNotificationHelper = mock(NbiNotificationHelper.class);
        DataStore dataStore = new DataStore(StandardDataStores.RUNNING, rootModelNodeAggregator, m_subSystemRegistry);
        dataStore.setNbiNotificationHelper(m_nbiNotificationHelper);
        m_server.setRunningDataStore(dataStore);
        YangUtils.loadXmlDataIntoServer(m_server, xmlFilePath);
    }

    @Test
    public void testGet() throws SAXException, IOException {
        NetconfClientInfo client = new NetconfClientInfo("test", 1);
        GetRequest request = new GetRequest();
        request.setIncludeConfig(false);
        request.setMessageId("1");
        NetConfResponse response = new NetConfResponse();
        response.setMessageId("1");
        m_server.onGet(client, request, response);
        TestUtil.assertXMLEquals("/getstate-unfiltered-jukebox.xml", response);
    }
}
