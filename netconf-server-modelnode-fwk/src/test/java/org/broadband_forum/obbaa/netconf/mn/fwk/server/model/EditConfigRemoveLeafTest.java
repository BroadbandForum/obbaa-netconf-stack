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

import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.NotifSwitchUtil.ENABLE_NEW_NOTIF_STRUCTURE;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.NotifSwitchUtil.resetSystemProperty;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.NotifSwitchUtil.setSystemPropertyAndReturnResetValue;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.loadAsXml;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.sendEditConfig;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfFilter;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorSeverity;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildContainerHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeFactoryException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootEntityContainerModelNodeHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregatorImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.DataStoreIntegrityService;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.DataStoreValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.DataStoreValidatorImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.service.DataStoreIntegrityServiceImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DSExpressionValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.inmemory.InMemoryDSM;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.LocalSubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.YangUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.restaurant.RestaurantConstants;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.xml.sax.SAXException;

@RunWith(RequestScopeJunitRunner.class)
public class EditConfigRemoveLeafTest extends AbstractEditConfigTestSetup {
    private NetConfServerImpl m_server;


    private SubSystemRegistry m_subSystemRegistry = new SubSystemRegistryImpl();

    private SchemaRegistry m_schemaRegistry = null;

    private RootModelNodeAggregator m_rootModelNodeAggregator;

    private DataStore m_dataStore;

    private DataStoreValidator m_datastoreValidator;

    private ModelNodeDataStoreManager m_modelNodeDsm;

    private NbiNotificationHelper m_nbiNotificationHelper;

    private DataStoreIntegrityService m_integrityService;
    private ModelNodeHelperRegistry m_modelNodeHelperRegistry = new ModelNodeHelperRegistryImpl(m_schemaRegistry);
    private LocalSubSystem m_localSubsystem = spy(new LocalSubSystem());
    private String m_previousValue;
    private boolean m_toBeReset;

    @Before
    public void initServer() throws SchemaBuildException, ModelNodeInitException, ModelNodeFactoryException {

        String yangFile = "/leaftest/example-restaurant.yang";
        String xmlFile = "/leaftest/example-restaurant-instance.xml";
        String yangFilePath = getClass().getResource(yangFile).getPath();
        String xmlFilePath = EditConfigDeleteLeafTest.class.getResource(xmlFile).getPath();

        m_previousValue = System.getProperty(ENABLE_NEW_NOTIF_STRUCTURE);
        m_toBeReset = setSystemPropertyAndReturnResetValue(m_previousValue, "true");
        m_schemaRegistry = new SchemaRegistryImpl(Collections.<YangTextSchemaSource>emptyList(), Collections.emptySet(), Collections.emptyMap(), new NoLockService());
        m_schemaRegistry.loadSchemaContext("restaurant", Arrays.asList(TestUtil.getByteSource("/leaftest/example-restaurant.yang")), Collections.emptySet(), Collections.emptyMap());
        m_modelNodeDsm = new InMemoryDSM(m_schemaRegistry);
        m_subSystemRegistry.setCompositeSubSystem(new CompositeSubSystemImpl());
        m_rootModelNodeAggregator = new RootModelNodeAggregatorImpl(m_schemaRegistry, m_modelNodeHelperRegistry,
                mock(ModelNodeDataStoreManager.class), m_subSystemRegistry);
        m_server = new NetConfServerImpl(m_schemaRegistry);
        m_integrityService = new DataStoreIntegrityServiceImpl(m_server);
        m_expValidator = new DSExpressionValidator(m_schemaRegistry, m_modelNodeHelperRegistry, m_subSystemRegistry);
        m_datastoreValidator = new DataStoreValidatorImpl(m_schemaRegistry, m_modelNodeHelperRegistry, m_modelNodeDsm, m_integrityService,m_expValidator);

        YangUtils.deployInMemoryHelpers(yangFilePath, m_localSubsystem, m_modelNodeHelperRegistry,
                m_subSystemRegistry, m_schemaRegistry, m_modelNodeDsm);
        ContainerSchemaNode schemaNode = (ContainerSchemaNode)m_schemaRegistry.getDataSchemaNode(RestaurantConstants.RESTAURANT_SCHEMA_PATH);
        ChildContainerHelper containerHelper = new RootEntityContainerModelNodeHelper(schemaNode,
                m_modelNodeHelperRegistry, m_subSystemRegistry, m_schemaRegistry, m_modelNodeDsm);
        m_rootModelNodeAggregator.addModelServiceRootHelper(RestaurantConstants.RESTAURANT_SCHEMA_PATH, containerHelper);
        m_dataStore = new DataStore(StandardDataStores.RUNNING, m_rootModelNodeAggregator, m_subSystemRegistry);
        m_dataStore.setValidator(m_datastoreValidator);
        m_nbiNotificationHelper = mock(NbiNotificationHelper.class);
        m_dataStore.setNbiNotificationHelper(m_nbiNotificationHelper);
        m_server.setRunningDataStore(m_dataStore);
        YangUtils.loadXmlDataIntoServer(m_server, xmlFilePath);
    }

    @After
    public void tearDown() throws Exception {
        resetSystemProperty(m_toBeReset, m_previousValue);
    }

    /*
	 * Delete the leaf which does not exist
	 */
    @Test
    public void testDeleteNonExistingLeaf() throws SAXException, IOException {
        String requestXml = "/leaftest/delete-non-existing-leaf.xml";
        NetConfResponse response = sendEditConfig(m_server, m_clientInfo, loadAsXml(requestXml), "1");
        assertFalse(response.isOk());
        assertEquals(1, response.getErrors().size());
        NetconfRpcError netconfRpcError = response.getErrors().get(0);
        assertEquals(NetconfRpcErrorTag.DATA_MISSING, netconfRpcError.getErrorTag());
        assertEquals("/restaurant:restaurant", netconfRpcError.getErrorPath());
        assertEquals(NetconfRpcErrorSeverity.Error, netconfRpcError.getErrorSeverity());
        assertEquals("Data does not exist opening-time", netconfRpcError.getErrorMessage());
    }

    /*
     * Remove the leaf which does not exist
     */
    @Test
    public void testRemoveNonExistingLeaf() throws SAXException, IOException {
        String requestXml = "/leaftest/remove-opening-time.xml";
        String expectedXml = "/leaftest/expected-remove-opening-time.xml";
        NetConfResponse response = sendEditConfig(m_server, m_clientInfo, loadAsXml(requestXml), "1");
        assertTrue(response.isOk());
        TestUtil.verifyGet(m_server, (NetconfFilter) null, expectedXml, "1");
    }

    /*
     * Remove the leaf twice
     */
    @Test
    public void testRemoveLeafTwice() {
        String requestXml = "/leaftest/remove-owner.xml";
        NetConfResponse response = sendEditConfig(m_server, m_clientInfo, loadAsXml(requestXml), "1");
        assertTrue(response.isOk());
        verify(m_localSubsystem, times(2)).postCommit(anyMap());
        reset(m_localSubsystem);
        NetConfResponse response2 = sendEditConfig(m_server, m_clientInfo, loadAsXml(requestXml), "1");
        assertTrue(response2.isOk());
        verify(m_localSubsystem, times(0)).postCommit(anyMap());
    }

    /*
     * Remove the leaf twice without specifying value
     */
    @Test
    public void testRemoveLeafTwiceWithoutSpecifyingValueInRequest() {
        String requestXml = "/leaftest/remove-owner-without-specifying-value.xml";
        NetConfResponse response = sendEditConfig(m_server, m_clientInfo, loadAsXml(requestXml), "1");
        assertTrue(response.isOk());
        verify(m_localSubsystem, times(2)).postCommit(anyMap());
        reset(m_localSubsystem);
        NetConfResponse response2 = sendEditConfig(m_server, m_clientInfo, loadAsXml(requestXml), "1");
        assertTrue(response2.isOk());
        verify(m_localSubsystem, times(0)).postCommit(anyMap());
    }

    /*
     * Remove the leaf which has a default value
     */
    @Test
    public void testRemoveOpeningTime() throws SAXException, IOException {
        String requestXml = "/leaftest/remove-opening-time2.xml";
        String expectedXml = "/leaftest/expected-remove-opening-time2.xml";
        NetConfResponse response = sendEditConfig(m_server, m_clientInfo, loadAsXml(requestXml), "1");
        assertTrue(response.isOk());
        TestUtil.verifyGet(m_server, (NetconfFilter) null, expectedXml, "1");
    }
}
