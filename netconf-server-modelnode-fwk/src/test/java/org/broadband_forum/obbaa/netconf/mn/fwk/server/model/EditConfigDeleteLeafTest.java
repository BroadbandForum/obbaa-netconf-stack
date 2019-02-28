package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.loadAsXml;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.sendEditConfig;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.xml.sax.SAXException;

import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfFilter;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorSeverity;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorType;
import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.restaurant.RestaurantConstants;
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
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.inmemory.InMemoryDSM;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.LocalSubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.YangUtils;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;

public class EditConfigDeleteLeafTest extends AbstractEditConfigTestSetup {

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

	@Before
	public void initServer() throws SchemaBuildException, ModelNodeInitException, ModelNodeFactoryException {

		String yangFile = "/leaftest/example-restaurant.yang";
		String xmlFile = "/leaftest/example-restaurant-instance.xml";
		String yangFilePath = getClass().getResource(yangFile).getPath();
		String xmlFilePath = EditConfigDeleteLeafTest.class.getResource(xmlFile).getPath();

		m_schemaRegistry = new SchemaRegistryImpl(Collections.<YangTextSchemaSource>emptyList(), Collections.emptySet(), Collections.emptyMap(), new NoLockService());
		m_schemaRegistry.loadSchemaContext("restaurant", Arrays.asList(TestUtil.getByteSource("/leaftest/example-restaurant.yang")), Collections.emptySet(), Collections.emptyMap());
		m_modelNodeDsm = new InMemoryDSM(m_schemaRegistry);
		m_rootModelNodeAggregator = new RootModelNodeAggregatorImpl(m_schemaRegistry, m_modelNodeHelperRegistry,
				mock(ModelNodeDataStoreManager.class), m_subSystemRegistry);
		m_server = new NetConfServerImpl(m_schemaRegistry);
		m_integrityService = new DataStoreIntegrityServiceImpl(m_server);
		m_datastoreValidator = new DataStoreValidatorImpl(m_schemaRegistry, m_modelNodeHelperRegistry, m_modelNodeDsm, m_integrityService, m_expValidator);

		YangUtils.deployInMemoryHelpers(yangFilePath, new LocalSubSystem(), m_modelNodeHelperRegistry,
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

	/*
	 * Delete the normal leaf
	 */
	@Test
	public void testDeleteLocation() throws SAXException, IOException {
		String requestXml = "/leaftest/delete-location.xml";
		String expectedXml = "/leaftest/expected-delete-location.xml";
		NetConfResponse response = sendEditConfig(m_server, m_clientInfo, loadAsXml(requestXml), "1");
		assertTrue(response.isOk());
		TestUtil.verifyGet(m_server, (NetconfFilter) null, expectedXml, "1");
	}
	
	/*
	 * Delete the unconfig leaf
	 */
	@Test
	public void testDeleteCounter() throws SAXException, IOException {
		String requestXml = "/leaftest/delete-count-viands.xml";
		NetConfResponse response = sendEditConfig(m_server, m_clientInfo, loadAsXml(requestXml), "1");
		assertFalse(response.isOk());
		assertEquals(1, response.getErrors().size());
        NetconfRpcError rpcError = response.getErrors().get(0);
        assertEquals(NetconfRpcErrorTag.UNKNOWN_ELEMENT, rpcError.getErrorTag());
        assertEquals(NetconfRpcErrorType.Application, rpcError.getErrorType());
        assertEquals(NetconfRpcErrorSeverity.Error, rpcError.getErrorSeverity());
	}
	
	/*
	 * Delete the leaf which has a default value
	 */
	@Test
	public void testDeleteOpeningTime() throws SAXException, IOException {
		String requestXml = "/leaftest/delete-opening-time.xml";
		String expectedXml = "/leaftest/expected-delete-opening-time.xml";
		NetConfResponse response = sendEditConfig(m_server, m_clientInfo, loadAsXml(requestXml), "1");
		assertTrue(response.isOk());
		TestUtil.verifyGet(m_server, (NetconfFilter) null, expectedXml, "1");
	}
	
	/*
	 * Delete the mandatory leaf
	 */
	@Test
	public void testDeleteRestaurantName() throws SAXException, IOException {
		String requestXml = "/leaftest/delete-restaurant-name.xml";
		NetConfResponse response = sendEditConfig(m_server, m_clientInfo, loadAsXml(requestXml), "1");
		assertFalse(response.isOk());
		assertEquals(1, response.getErrors().size());
        NetconfRpcError rpcError = response.getErrors().get(0);
        assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, rpcError.getErrorTag());
        assertEquals(NetconfRpcErrorType.Application, rpcError.getErrorType());
        assertEquals(NetconfRpcErrorSeverity.Error, rpcError.getErrorSeverity());
	}
    
    /*
     * Delete a non-mandatory leaf with minimum size
     */
    @Test
    public void testDeleteOwner() throws SAXException, IOException {
        String requestXml = "/leaftest/delete-owner.xml";
        String expectedXml = "/leaftest/expected-delete-owner.xml";
        NetConfResponse response = sendEditConfig(m_server, m_clientInfo, loadAsXml(requestXml), "1");
        assertTrue(response.isOk());
        TestUtil.verifyGet(m_server, (NetconfFilter) null, expectedXml, "1");
    }
    
    /*
     * Delete a non-mandatory leaf with minimum size, without value provided in request
     */
    @Test
    public void testDeleteOwnerNoValueInRequest() throws SAXException, IOException {
        String requestXml = "/leaftest/delete-owner-no-value-in-request.xml";
        String expectedXml = "/leaftest/expected-delete-owner.xml";
        NetConfResponse response = sendEditConfig(m_server, m_clientInfo, loadAsXml(requestXml), "1");
        assertTrue(response.isOk());
        TestUtil.verifyGet(m_server, (NetconfFilter) null, expectedXml, "1");
    }
	
	/*
	 * Delete the key leaf in list
	 */
	@Test
	public void testDeleteViandName() throws SAXException, IOException {
		String requestXml = "/leaftest/delete-viand-name.xml";
		NetConfResponse response = sendEditConfig(m_server, m_clientInfo, loadAsXml(requestXml), "1");
		assertFalse(response.isOk());
		assertEquals(1, response.getErrors().size());
        NetconfRpcError rpcError = response.getErrors().get(0);
        assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, rpcError.getErrorTag());
        assertEquals(NetconfRpcErrorType.Application, rpcError.getErrorType());
        assertEquals(NetconfRpcErrorSeverity.Error, rpcError.getErrorSeverity());
	}
	
	/*
	 * Delete the leaf, has a default, in list
	 */
	@Test
	public void testDeleteViandPrice() throws SAXException, IOException {
		String requestXml = "/leaftest/delete-viand-price.xml";
		String expectedXml = "/leaftest/expected-delete-viand-price.xml";
		NetConfResponse response = sendEditConfig(m_server, m_clientInfo, loadAsXml(requestXml), "1");
		assertTrue(response.isOk());
		TestUtil.verifyGet(m_server, (NetconfFilter) null, expectedXml, "1");
	}

    /*
     * Delete a leaf along with update (value change) in another leaf
     */
    @Test
    public void testDeleteOwnerWithModifiedLocation() throws SAXException, IOException {
        String requestXml = "/leaftest/delete-owner-with-location-change.xml";
        String expectedXml = "/leaftest/expected-delete-owner-with-location-change.xml";
        NetConfResponse response = sendEditConfig(m_server, m_clientInfo, loadAsXml(requestXml), "1");
        assertTrue(response.isOk());
        TestUtil.verifyGet(m_server, (NetconfFilter) null, expectedXml, "1");
    }
    @Override
    public void setup() {
        
    }
}
