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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.dsm;


import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.sendEditConfig;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.verifyGetConfig;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.CompositeSubSystemImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.DataStore;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NbiNotificationHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NetConfServerImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.xml.sax.SAXException;

@RunWith(RequestScopeJunitRunner.class)
public class DsmConfigAttributeHelperTest {

    private static final String MESSAGE_ID = "1";
    private static final String COMPONENT_ID = "Jukebox";
    private static final String EXAMPLE_JUKEBOX_YANGFILE = "/dsmconfigattributehelpertest/example-jukebox.yang";
    private static final String EXAMPLE_JUKEBOX_XML = "/dsmconfigattributehelpertest/example-jukebox-default.xml";

    private NetConfServerImpl m_netConfServer;
    private ModelNodeHelperRegistry m_modelNodeHelperRegistry;
    private SubSystemRegistry m_subSystemRegistry;
    private SchemaRegistry m_schemaRegistry;
    private InMemoryDSM m_inMemoryDSM;
    private RootModelNodeAggregator m_rootModelNodeAggregator;
    private String m_yangFilePath;
    private java.net.URI m_uri =  java.net.URI.create("");
    private QName m_qname = QName.create(m_uri, (Revision)null, "localname");
    private QName m_qnameTwo = QName.create(m_uri, (Revision)null, "localname");
    
    @Before
    public void initServer() throws SchemaBuildException, ModelNodeInitException {
        List<YangTextSchemaSource> yangs = TestUtil.getJukeBoxDeps();
        yangs.add(TestUtil.getByteSource(EXAMPLE_JUKEBOX_YANGFILE));
        m_schemaRegistry = new SchemaRegistryImpl(yangs, Collections.emptySet(), Collections.emptyMap(), new NoLockService());

        m_modelNodeHelperRegistry = new ModelNodeHelperRegistryImpl(m_schemaRegistry);
        m_subSystemRegistry = new SubSystemRegistryImpl();
        m_subSystemRegistry.setCompositeSubSystem(new CompositeSubSystemImpl());
        m_yangFilePath = TestUtil.class.getResource(EXAMPLE_JUKEBOX_YANGFILE).getPath();
        m_inMemoryDSM = new InMemoryDSM(m_schemaRegistry);
        ModelNode yangModel = YangUtils.createInMemoryModelNode(m_yangFilePath, new LocalSubSystem(), m_modelNodeHelperRegistry,
                m_subSystemRegistry, m_schemaRegistry, m_inMemoryDSM);
        m_rootModelNodeAggregator = new RootModelNodeAggregatorImpl(m_schemaRegistry, m_modelNodeHelperRegistry, m_inMemoryDSM, m_subSystemRegistry);
        m_rootModelNodeAggregator.addModelServiceRoot(COMPONENT_ID, yangModel);

        m_netConfServer = new NetConfServerImpl(m_schemaRegistry, mock(RpcPayloadConstraintParser.class));
        NbiNotificationHelper nbiNotificationHelper = mock(NbiNotificationHelper.class);
        DataStore dataStore = new DataStore(StandardDataStores.RUNNING, m_rootModelNodeAggregator, m_subSystemRegistry);
        dataStore.setNbiNotificationHelper(nbiNotificationHelper);
        m_netConfServer.setRunningDataStore(dataStore);
        String xmlFilePath = TestUtil.class.getResource(EXAMPLE_JUKEBOX_XML).getPath();
        YangUtils.loadXmlDataIntoServer(m_netConfServer, xmlFilePath);

    }

    @Test
    public void testUpdateMultipleNodes() throws IOException, SAXException {
        verifyGetConfig(m_netConfServer, StandardDataStores.RUNNING, null, "/dsmconfigattributehelpertest/getconfig-response.xml", "1");

        sendEditConfig(m_netConfServer, new NetconfClientInfo("test", 1),TestUtil.loadAsXml("/dsmconfigattributehelpertest/editconfig-request.xml"), MESSAGE_ID);

        verifyGetConfig(m_netConfServer, StandardDataStores.RUNNING, null, "/dsmconfigattributehelpertest/getconfig-response-after-edit.xml", "1");
    }
    
    @Test
    public void testGetConfigWithEmptyKey() throws SAXException, IOException {
        verifyGetConfig(m_netConfServer, StandardDataStores.RUNNING, null, "/dsmconfigattributehelpertest/getconfig-response.xml", "1");

        sendEditConfig(m_netConfServer, new NetconfClientInfo("test", 1),TestUtil.loadAsXml("/dsmconfigattributehelpertest/editconfig-request-add-emptyKey.xml"), MESSAGE_ID);

        verifyGetConfig(m_netConfServer, StandardDataStores.RUNNING, null, "/dsmconfigattributehelpertest/getconfig-response-after-add-empty-key.xml", "1");
    }

    @Test
    public void testEqualsMethod(){
        DsmConfigAttributeHelper helper = new DsmConfigAttributeHelper(mock(ModelNodeDataStoreManager.class) , mock(SchemaRegistry.class) , mock(LeafSchemaNode.class) , m_qname);
        DsmConfigAttributeHelper helperTwo = new DsmConfigAttributeHelper(mock(ModelNodeDataStoreManager.class) , mock(SchemaRegistry.class) , mock(LeafSchemaNode.class) , m_qname);

        assertFalse(helper.equals(helperTwo));
        assertTrue(helper.equals(helper));
        assertFalse(helper.equals(null));
        assertFalse(helper.equals(new Object()));
        assertFalse(helper.equals(helperTwo));

        helper = new DsmConfigAttributeHelper(mock(ModelNodeDataStoreManager.class) , mock(SchemaRegistry.class) , null , m_qname);
        assertFalse(helper.equals(helperTwo));

        helper = new DsmConfigAttributeHelper(mock(ModelNodeDataStoreManager.class) , null , mock(LeafSchemaNode.class) , m_qname);
        assertFalse(helper.equals(helperTwo));

        LeafSchemaNode node = mock(LeafSchemaNode.class);
        helperTwo = new DsmConfigAttributeHelper(mock(ModelNodeDataStoreManager.class) , mock(SchemaRegistry.class) , node , m_qname);
        helper = new DsmConfigAttributeHelper(mock(ModelNodeDataStoreManager.class) , mock(SchemaRegistry.class) ,node , null);
        assertFalse(helper.equals(helperTwo));


        helperTwo = new DsmConfigAttributeHelper(null , mock(SchemaRegistry.class) , node , m_qname);
        helper = new DsmConfigAttributeHelper(null , mock(SchemaRegistry.class) ,node , null);
        assertFalse(helper.equals(helperTwo));

        helperTwo = new DsmConfigAttributeHelper(null , mock(SchemaRegistry.class) , node ,m_qnameTwo );
        helper = new DsmConfigAttributeHelper(null , mock(SchemaRegistry.class) ,node , m_qname);
        assertFalse(helper.equals(helperTwo));
    }

    @Test
    public void testGetDefault() throws Exception{
        java.net.URI uri =  java.net.URI.create("");
        QName qnqmeTwo = QName.create(uri, (Revision)null, "localname");
        LeafSchemaNode node = mock(LeafSchemaNode.class);
        TypeDefinition type = mock(TypeDefinition.class);
        when(node.getType()).thenReturn(type);
        when(type.getDefaultValue()).thenReturn(Optional.of("mydefault"));
        DsmConfigAttributeHelper helper = new DsmConfigAttributeHelper(mock(ModelNodeDataStoreManager.class) , mock(SchemaRegistry.class) , node , qnqmeTwo);
        assertEquals(node.getType().getDefaultValue().get(),helper.getDefault());
    }
}