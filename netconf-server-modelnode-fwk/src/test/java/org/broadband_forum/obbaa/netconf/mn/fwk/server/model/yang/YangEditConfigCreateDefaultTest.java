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

import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.assertXMLEquals;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.loadAsXml;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.sendEditConfig;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.verifyGetConfig;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.Collections;

import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.CompositeSubSystemImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.DataStore;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NbiNotificationHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NetConfServerImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.AddDefaultDataInterceptor;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeFactoryException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregatorImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DSExpressionValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.inmemory.InMemoryDSM;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.LocalSubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.YangUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

@RunWith(RequestScopeJunitRunner.class)
public class YangEditConfigCreateDefaultTest extends AbstractYangValidationTestSetup{
    private NetConfServerImpl m_server;
    private ModelNodeWithAttributes m_model;
	private SubSystemRegistry m_subSystemRegistry = new SubSystemRegistryImpl();
    public static final String DEFAULTCAPABILITY_YANG_FILE_YANG = "/defaultcapability/yang-file.yang";
    public static final String AUGMENTED_WHEN_CONDITION_YANG = "/defaultcapability/default-capability-test.yang";
	private static final String m_default_xmlFile = YangEditConfigCreateTest.class.getResource("/defaultcapability/default-file.xml").getPath();
    private RootModelNodeAggregator m_rootModelNodeAggregator;
    private SchemaRegistry m_schemaRegistry;
	private ModelNodeHelperRegistry m_modelNodeHelperRegistry = new ModelNodeHelperRegistryImpl(m_schemaRegistry);
    private AddDefaultDataInterceptor m_addDefaultDataInterceptor;

    @Before
    public void initServer() throws SchemaBuildException, ModelNodeInitException, ModelNodeFactoryException {
        m_schemaRegistry = new SchemaRegistryImpl(Arrays.asList(TestUtil.getByteSource(DEFAULTCAPABILITY_YANG_FILE_YANG), TestUtil.getByteSource(AUGMENTED_WHEN_CONDITION_YANG)), Collections.emptySet(), Collections.emptyMap(), new NoLockService());
        m_modelNodeHelperRegistry = new ModelNodeHelperRegistryImpl(m_schemaRegistry);
        m_expValidator = new DSExpressionValidator(m_schemaRegistry, m_modelNodeHelperRegistry, m_subSystemRegistry);
        m_addDefaultDataInterceptor = new AddDefaultDataInterceptor(m_modelNodeHelperRegistry, m_schemaRegistry, m_expValidator);
        m_addDefaultDataInterceptor.init(); // Auto Registers
        createNonEmptyServer(new LocalSubSystem());
    }
    @After
    public void tearDown(){
        m_addDefaultDataInterceptor.destroy();
    }
    
    private void createNonEmptyServer(SubSystem subSystem) throws ModelNodeInitException, ModelNodeFactoryException, SchemaBuildException {
        m_server = new NetConfServerImpl(m_schemaRegistry);
        
        InMemoryDSM modelNodeDsm = new InMemoryDSM(m_schemaRegistry);
        m_model = (ModelNodeWithAttributes) YangUtils.createInMemoryModelNode(Arrays.asList(DEFAULTCAPABILITY_YANG_FILE_YANG, AUGMENTED_WHEN_CONDITION_YANG),subSystem, m_modelNodeHelperRegistry, m_subSystemRegistry, m_schemaRegistry, modelNodeDsm);
        m_subSystemRegistry.setCompositeSubSystem(new CompositeSubSystemImpl());
        m_rootModelNodeAggregator = new RootModelNodeAggregatorImpl(m_schemaRegistry, m_modelNodeHelperRegistry, modelNodeDsm, m_subSystemRegistry).addModelServiceRoot(m_componentId, m_model);
        DataStore dataStore = new DataStore(StandardDataStores.RUNNING, m_rootModelNodeAggregator, m_subSystemRegistry);
        NbiNotificationHelper nbiNotificationHelper = mock(NbiNotificationHelper.class);
        dataStore.setNbiNotificationHelper(nbiNotificationHelper);
        m_server.setRunningDataStore(dataStore);
        YangUtils.loadXmlDataIntoServer(m_server,m_default_xmlFile);
    }
    
    @Test
    public void testCreate() throws Exception {
        NetConfResponse response = sendEditConfig(m_server, m_clientInfo, loadAsXml("/defaultcapability/edit-config-create-request.xml"), "1");
        assertXMLEquals("/ok-response.xml", response);
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/defaultcapability/get-config-result.xml", "1");
    }
    
    
    @Test
    public void testCreateWithWhenCondition() throws Exception {
        Element xmlElement = loadAsXml("/defaultcapability/edit-config-create-request.xml");
        NodeList parentKey = xmlElement.getElementsByTagName("a1");
        parentKey.item(0).setTextContent("key2");
        NetConfResponse response = sendEditConfig(m_server, m_clientInfo, xmlElement, "1");
        assertXMLEquals("/ok-response.xml", response);
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/defaultcapability/get-config-withWhen-result.xml", "1");
    }
    @Test
    public void testReplace() throws Exception {
        NetConfResponse response = sendEditConfig(m_server, m_clientInfo, loadAsXml("/defaultcapability/edit-config-replace-request.xml"), "1");
        
        // assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        // do a get-config to be sure
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/defaultcapability/get-config-result.xml", "1");
    }
    
    @Test
    public void testMerge() throws Exception {
        NetConfResponse response = sendEditConfig(m_server, m_clientInfo, loadAsXml("/defaultcapability/edit-config-merge-request.xml"), "1");
        
        // assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        // do a get-config to be sure
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/defaultcapability/get-config-result.xml", "1");
    }
    
}
