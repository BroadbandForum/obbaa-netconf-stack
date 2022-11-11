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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.api.messages.ActionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.ActionResponse;
import org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorSeverity;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorType;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.RpcRequestConstraintParser;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.DataStore;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NetConfServerImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildContainerHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootEntityContainerModelNodeHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.TestActionSubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@RunWith(RequestScopeJunitRunner.class)
public class ActionValidatorTest extends AbstractDataStoreValidatorTest {
    private static final String YANG_FILE = "/datastorevalidatortest/yangs/test-action.yang";
    private static final String YANG_FILE1 = "/datastorevalidatortest/yangs/ietf-yang-types.yang";
    private static final String NAMESPACE = "urn:example:test-action";
    private static final QName ACTION_CONTAINER = QName.create(NAMESPACE, "2015-12-14", "test-action-container");
    private static final SchemaPath ACTION_CONTAINER_SCHEMA_PATH = SchemaPath.create(true, ACTION_CONTAINER);
    private static final String DEFAULT_XML = "/datastorevalidatortest/yangs/test-action-default.xml";
    private static final String PREFIX = "test";
    
    private static final QName ACTION_LEAFREF_CONTAINER = QName.create(NAMESPACE, "2015-12-14", "test-action-leafref-container");
    private static final SchemaPath ACTION_LEAFREF_CONTAINER_SCHEMA_PATH = SchemaPath.create(true, ACTION_LEAFREF_CONTAINER);
    
    
    private static final QName ACTION_WHEN_MUST_VALIDATION_CONTAINER = QName.create(NAMESPACE, "2015-12-14", "test-action-when-must-validation");
    private static final SchemaPath ACTION_WHEN_MUST_VALIDATION_CONTAINER_SCHEMA_PATH = SchemaPath.create(true, ACTION_WHEN_MUST_VALIDATION_CONTAINER);
    
    private static final QName ACTION_DEVICES_CONTAINER = QName.create(NAMESPACE, "2015-12-14", "devices");
    private static final SchemaPath ACTION_DEVICES_CONTAINER_SCHEMA_PATH = SchemaPath.create(true, ACTION_DEVICES_CONTAINER);
    
    private static final QName ACTION_ROLL_BACK_FILES_CONTAINER = QName.create(NAMESPACE, "2015-12-14", "rollback-files");
    private static final SchemaPath ACTION_ROLL_BACK_FILES_CONTAINER_SCHEMA_PATH = SchemaPath.create(true, ACTION_ROLL_BACK_FILES_CONTAINER);

	private static final QName ACTION_CPE_SOFTWARE_MANAGEMENT_CONTAINER = QName.create(NAMESPACE, "2015-12-14", "cpe-software-management");
	private static final SchemaPath ACTION_CPE_SOFTWARE_MANAGEMENT_SCHEMA_PATH = SchemaPath.create(true, ACTION_CPE_SOFTWARE_MANAGEMENT_CONTAINER);

	private static final String ACTION_OUTPUT_WITH_DUPLICATES =
					"	<test:device-list xmlns:test=\"urn:example:test-action\">" +
					"		<test:hardware-type xmlns:test=\"urn:example:test-action\">123</test:hardware-type>" +
					"		<test:device-id xmlns:test=\"urn:example:test-action\">device1</test:device-id>" +
					"		<test:device-id xmlns:test=\"urn:example:test-action\">device1</test:device-id>" +
					"	</test:device-list>";

    private NetConfResponse m_response = new ActionResponse().setMessageId("1");

    protected NetConfServerImpl getNcServer(){
        RpcRequestConstraintParser rpcValidator = new RpcRequestConstraintParser(m_schemaRegistry, m_modelNodeDsm, m_expValidator, null);
    	return new NetConfServerImpl(m_schemaRegistry, rpcValidator);
    }
    
    protected static List<String> getYang() {
    	return Arrays.asList(YANG_FILE1, YANG_FILE);
    }
    
    protected String getXml() {
    	return DEFAULT_XML;
    }

    protected SubSystem getSubSystem() {
        return new TestActionSubSystem(m_schemaRegistry);
    }
    
    protected void addRootNodeHelpers() {
        ContainerSchemaNode schemaNode = (ContainerSchemaNode) m_schemaRegistry.getDataSchemaNode(ACTION_CONTAINER_SCHEMA_PATH);
        ChildContainerHelper containerHelper = new RootEntityContainerModelNodeHelper(schemaNode,
                m_modelNodeHelperRegistry, m_subSystemRegistry, m_schemaRegistry,
                m_modelNodeDsm);
        m_rootModelNodeAggregator.addModelServiceRootHelper(ACTION_CONTAINER_SCHEMA_PATH, containerHelper);
        
        schemaNode = (ContainerSchemaNode) m_schemaRegistry.getDataSchemaNode(ACTION_DEVICES_CONTAINER_SCHEMA_PATH);
        containerHelper = new RootEntityContainerModelNodeHelper(schemaNode,
                m_modelNodeHelperRegistry, m_subSystemRegistry, m_schemaRegistry,
                m_modelNodeDsm);
        m_rootModelNodeAggregator.addModelServiceRootHelper(ACTION_DEVICES_CONTAINER_SCHEMA_PATH, containerHelper);
        
        schemaNode = (ContainerSchemaNode) m_schemaRegistry.getDataSchemaNode(ACTION_LEAFREF_CONTAINER_SCHEMA_PATH);
        containerHelper = new RootEntityContainerModelNodeHelper(schemaNode,
                m_modelNodeHelperRegistry, m_subSystemRegistry, m_schemaRegistry,
                m_modelNodeDsm);
        m_rootModelNodeAggregator.addModelServiceRootHelper(ACTION_LEAFREF_CONTAINER_SCHEMA_PATH, containerHelper);
        
        schemaNode = (ContainerSchemaNode) m_schemaRegistry.getDataSchemaNode(ACTION_WHEN_MUST_VALIDATION_CONTAINER_SCHEMA_PATH);
        containerHelper = new RootEntityContainerModelNodeHelper(schemaNode,
                m_modelNodeHelperRegistry, m_subSystemRegistry, m_schemaRegistry,
                m_modelNodeDsm);
        m_rootModelNodeAggregator.addModelServiceRootHelper(ACTION_WHEN_MUST_VALIDATION_CONTAINER_SCHEMA_PATH, containerHelper);
        
        schemaNode = (ContainerSchemaNode) m_schemaRegistry.getDataSchemaNode(ACTION_ROLL_BACK_FILES_CONTAINER_SCHEMA_PATH);
        containerHelper = new RootEntityContainerModelNodeHelper(schemaNode,
                m_modelNodeHelperRegistry, m_subSystemRegistry, m_schemaRegistry,
                m_modelNodeDsm);
        m_rootModelNodeAggregator.addModelServiceRootHelper(ACTION_ROLL_BACK_FILES_CONTAINER_SCHEMA_PATH, containerHelper);

		schemaNode = (ContainerSchemaNode) m_schemaRegistry.getDataSchemaNode(ACTION_CPE_SOFTWARE_MANAGEMENT_SCHEMA_PATH);
		containerHelper = new RootEntityContainerModelNodeHelper(schemaNode,
				m_modelNodeHelperRegistry, m_subSystemRegistry, m_schemaRegistry,
				m_modelNodeDsm);
		m_rootModelNodeAggregator.addModelServiceRootHelper(ACTION_CPE_SOFTWARE_MANAGEMENT_SCHEMA_PATH, containerHelper);
    }
    
    @BeforeClass
    public static void initializeOnce() throws SchemaBuildException {
        List<YangTextSchemaSource> yangFiles = TestUtil.getByteSources(getYang());
        m_schemaRegistry = new SchemaRegistryImpl(yangFiles, Collections.emptySet(), Collections.emptyMap(), new NoLockService());
        m_schemaRegistry.setName(SchemaRegistry.GLOBAL_SCHEMA_REGISTRY);
    }
	private Element getActionResponseElement() throws NetconfMessageBuilderException{
		String response =  "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"+
		                    "<test:reset-finished-at xmlns:test=\"urn:example:test-action\">2014-07-29T13:42:00Z</test:reset-finished-at>" +
		                    "</rpc-reply>";
		Document document = DocumentUtils.stringToDocument(response);
		return document.getDocumentElement();
	}
	
	private Element getActionResponseElement_OK() throws NetconfMessageBuilderException{
		String response =  "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"+
		                    "<ok/>" +
		                    "</rpc-reply>";
		Document document = DocumentUtils.stringToDocument(response);
		return document.getDocumentElement();
	}

	@Test
	public void testNegativecaseOfRpcContain_2SameActionInList() throws Exception {
		getModelNode();
		initialiseInterceptor();
		String req= "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				"<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
				"<test:test-action-container xmlns:test=\"urn:example:test-action\">" +
				"<test:action-list>"+
				"<test:name>apache</test:name>"+
				"<test:reset>"+
				"<test:reset-at>2014-07-29T13:42:00Z</test:reset-at>"+
				"</test:reset>"+
				"</test:action-list>"+
				"<test:action-list>"+
				"<test:name>jboss</test:name>"+
				"<test:reset>"+
				"<test:reset-at>2015-07-29T13:42:00Z</test:reset-at>"+
				"</test:reset>"+
				"</test:action-list>"+
				"</test:test-action-container>"+
				"</action>"+
				"</rpc>";

		ActionRequest validActionRequest = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(req));
		m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);
		NetconfRpcError error = m_response.getErrors().get(0);
		assertEquals(NetconfRpcErrorType.Protocol, error.getErrorType());
		assertEquals(NetconfRpcErrorTag.BAD_ELEMENT, error.getErrorTag());
		assertEquals("Multiple action element exists within RPC (urn:example:test-action?revision=2015-12-14)reset", error.getErrorMessage());

	}

	@Test
	public void testRpcContain_OnlyOneActionElement() throws Exception {
		getModelNode();
		initialiseInterceptor();
		String req= "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				"<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
				"<test:test-action-container xmlns:test=\"urn:example:test-action\">" +
				"<test:action-list>"+
				"<test:name>apache</test:name>"+
				"<test:reset>"+
				"<test:reset-at>2014-07-29T13:42:00Z</test:reset-at>"+
				"</test:reset>"+
				"</test:action-list>"+
				"<test:action-list>"+
				"<test:name>jboss</test:name>"+
				"</test:action-list>"+
				"</test:test-action-container>"+
				"</action>"+
				"</rpc>";

		ActionRequest validActionRequest = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(req));
		m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);
		TestUtil.assertXMLEquals(getActionResponseElement(), m_response.getResponseDocument().getDocumentElement());
	}

	@Test
	public void testRpcContain_OneWithActionAndOneWithoutActionElementInList() throws Exception {
		getModelNode();
		initialiseInterceptor();
		String req= "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				"<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
				"<test:test-action-container xmlns:test=\"urn:example:test-action\">" +
				"<test:action-list>"+
				"<test:name>apache</test:name>"+
				"<test:reset>"+
				"<test:reset-at>2014-07-29T13:42:00Z</test:reset-at>"+
				"</test:reset>"+
				"</test:action-list>"+
				"<test:action-anotherlist>"+
				"<test:name>jboss</test:name>"+
				"</test:action-anotherlist>"+
				"</test:test-action-container>"+
				"</action>"+
				"</rpc>";

		ActionRequest validActionRequest = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(req));
		m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);
		TestUtil.assertXMLEquals(getActionResponseElement(), m_response.getResponseDocument().getDocumentElement());
	}

	@Test
	public void testNegativeCase_OfHaving2differentactionInList() throws Exception {
		getModelNode();
		initialiseInterceptor();
		String req= "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				"<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
				"<test:test-action-container xmlns:test=\"urn:example:test-action\">" +
				"<test:action-list>"+
				"<test:name>apache</test:name>"+
				"<test:reset>"+
				"<test:reset-at>2014-07-29T13:42:00Z</test:reset-at>"+
				"</test:reset>"+
				"</test:action-list>"+
				"<test:action-anotherlist>"+
				"<test:name>jboss</test:name>"+
				"<test:anotherreset>"+
				"<test:reset-at>2015-07-29T13:42:00Z</test:reset-at>"+
				"</test:anotherreset>"+
				"</test:action-anotherlist>"+
				"</test:test-action-container>"+
				"</action>"+
				"</rpc>";

		ActionRequest validActionRequest = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(req));
		m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);
		NetconfRpcError error = m_response.getErrors().get(0);
		assertEquals(NetconfRpcErrorType.Protocol, error.getErrorType());
		assertEquals(NetconfRpcErrorTag.BAD_ELEMENT, error.getErrorTag());
		assertEquals("Multiple action element exists within RPC", error.getErrorMessage());
	}

	@Test
    public void testValidActionInList() throws Exception {     
    	getModelNode();
    	initialiseInterceptor();
    	String req= "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
    			"<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
    			"<test:test-action-container xmlns:test=\"urn:example:test-action\">" +
    			"<test:action-list>"+
    			"<test:name>apache</test:name>"+
    			"<test:reset>"+
				"<test:reset-at>2014-07-29T13:42:00Z</test:reset-at>"+
    			"</test:reset>"+
    			"</test:action-list>"+
    			"</test:test-action-container>"+
    			"</action>"+
    			"</rpc>";


    	ActionRequest validActionRequest = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(req));
    	m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse)m_response);
    	TestUtil.assertXMLEquals(getActionResponseElement(), m_response.getResponseDocument().getDocumentElement());
    }

	@Test
    public void testValidActionInContainer() throws Exception {     
    	getModelNode();
    	initialiseInterceptor();
    	String req= "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
    			"<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
    			"<test:test-action-container xmlns:test=\"urn:example:test-action\">" +
    			"<test:container-reset>"+
    			"<test:reset-at>2014-07-29T13:42:00Z</test:reset-at>"+
    			"</test:container-reset>"+
    			"</test:test-action-container>"+
    			"</action>"+
    			"</rpc>";


    	ActionRequest validActionRequest = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(req));
    	m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse)m_response);
    	TestUtil.assertXMLEquals(getActionResponseElement(), m_response.getResponseDocument().getDocumentElement());
    }

	@Test
	public void testActionInputWithDuplicates() throws Exception {
		getModelNode();
		initialiseInterceptor();
		String req= "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				"<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
				"<test:test-action-container xmlns:test=\"urn:example:test-action\">" +
				"<test:container-reset>"+
				"<test:reset-at>2014-07-29T13:42:00Z</test:reset-at>"+
				"<test:reset-at>2014-07-29T13:43:00Z</test:reset-at>"+
				"</test:container-reset>"+
				"</test:test-action-container>"+
				"</action>"+
				"</rpc>";

		ActionRequest validActionRequest = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(req));
		m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse)m_response);
		assertFalse(m_response.getErrors().isEmpty());
		assertEquals(NetconfRpcErrorTag.BAD_ELEMENT, m_response.getErrors().get(0).getErrorTag());
		assertEquals("data-not-unique", m_response.getErrors().get(0).getErrorAppTag());
		assertEquals("Duplicate elements in node (urn:example:test-action?revision=2015-12-14)reset-at", m_response.getErrors().get(0).getErrorMessage());
	}

	@Test
	public void testActionOutputWithDuplicates() throws Exception {
		getModelNode();
		initialiseInterceptor();
		String actionRequest = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				"	<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
				"		<test:test-action-container xmlns:test=\"urn:example:test-action\">" +
				"			<test:get-device-list/>"+
				"		</test:test-action-container>"+
				"	</action>"+
				"</rpc>";

		DataStore dataStore = mock(DataStore.class);
		m_server.setRunningDataStore(dataStore);
		List<Element> list = new ArrayList<Element>();
		list.add(DocumentUtils.stringToDocument(ACTION_OUTPUT_WITH_DUPLICATES).getDocumentElement());
		when(dataStore.action(any(),any(),any(),any())).thenReturn(list);
		when(dataStore.withValidationContext(any())).thenCallRealMethod();
		ActionRequest validActionRequest = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(actionRequest));
		m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse)m_response);
		assertFalse(m_response.getErrors().isEmpty());
		assertEquals(NetconfRpcErrorTag.BAD_ELEMENT, m_response.getErrors().get(0).getErrorTag());
		assertEquals("data-not-unique", m_response.getErrors().get(0).getErrorAppTag());
		assertEquals("Duplicate elements in node (urn:example:test-action?revision=2015-12-14)device-id", m_response.getErrors().get(0).getErrorMessage());
	}

    @Test
    public void testMultipleActionInRpc() throws Exception {     
        getModelNode();
        initialiseInterceptor();
        String req= "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
                "<test:test-action-container xmlns:test=\"urn:example:test-action\">" +
                "<test:container-reset>"+
                "<test:reset-at>2014-07-29T13:42:00Z</test:reset-at>"+
                "</test:container-reset>"+
                "<test:container-other-reset>"+
                "<test:reset-at>2014-07-29T13:42:00Z</test:reset-at>"+
                "</test:container-other-reset>"+
                "</test:test-action-container>"+
                "</action>"+
                "</rpc>";

        ActionRequest invalidActionRequest = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(req));
        m_server.onAction(m_clientInfo, invalidActionRequest, (ActionResponse)m_response);
        NetconfRpcError error = m_response.getErrors().get(0);
        assertTrue(error.getErrorType().equals(NetconfRpcErrorType.Protocol));
        assertTrue(error.getErrorTag().equals(NetconfRpcErrorTag.BAD_ELEMENT));
        assertEquals("Multiple action element exists within RPC", error.getErrorMessage());
    }
    
    @Test
    public void testInvalidNotExistedAction() throws NetconfMessageBuilderException, ModelNodeInitException {
    	getModelNode();
        initialiseInterceptor();
		String req= "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				     "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
				         "<test:test-action-container xmlns:test=\"urn:example:test-action\">" +
		                   "<test:container-reset1>"+
		                    "<test:reset-at>2014-07-29T13:42:00Z</test:reset-at>"+
		                   "</test:container-reset1>"+
				        "</test:test-action-container>"+
				     "</action>"+
				 "</rpc>";
		
		
    	ActionRequest validActionRequest = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(req));
	    m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse)m_response);
    	NetconfRpcError error = m_response.getErrors().get(0);
    	assertTrue(error.getErrorType().equals(NetconfRpcErrorType.Protocol));
    	assertTrue(error.getErrorTag().equals(NetconfRpcErrorTag.BAD_ELEMENT));
    	assertEquals("No matched action found on the models", error.getErrorMessage());
    }
    
    @Test
    public void testValidActionInGrouping() throws Exception {     
    	getModelNode();
    	initialiseInterceptor();
    	String req= "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
    			"<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
    			"<test:test-action-container xmlns:test=\"urn:example:test-action\">" +
    			"<test:grouping-reset>"+
    			"<test:reset-at>2014-07-29T13:42:00Z</test:reset-at>"+
    			"</test:grouping-reset>"+
    			"</test:test-action-container>"+
    			"</action>"+
    			"</rpc>";


    	ActionRequest validActionRequest = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(req));
    	m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse)m_response);
    	TestUtil.assertXMLEquals(getActionResponseElement(), m_response.getResponseDocument().getDocumentElement());

    }
    
    @Test
    public void testValidActionInAugment() throws Exception {     
    	getModelNode();
    	initialiseInterceptor();
    	String req= "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
    			"<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
    			"<test:test-action-container xmlns:test=\"urn:example:test-action\">" +
    			"<test:augmented-reset>"+
    			"<test:reset-at>2014-07-29T13:42:00Z</test:reset-at>"+
    			"</test:augmented-reset>"+
    			"</test:test-action-container>"+
    			"</action>"+
    			"</rpc>";

    	ActionRequest validActionRequest = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(req));		
    	m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse)m_response);
    	TestUtil.assertXMLEquals(getActionResponseElement(), m_response.getResponseDocument().getDocumentElement());
    }

    private void verifyNoDeviceExists() throws Exception {
		String expectedVerifyGetResponse =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" +
						"	<data>" +
						"		<test:test-action-container xmlns:test=\"urn:example:test-action\"/>" +
						"	</data>" +
						"</rpc-reply>";
		verifyGet(expectedVerifyGetResponse);
	}

	private void createDevice(String deviceId) {
		String editConfigRequest = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<test:test-action-container xmlns:test=\"urn:example:test-action\">" +
				"	<test:device>"+
				"		<test:device-id>" + deviceId +"</test:device-id>"+
				"		<test:hardware-type>gfast</test:hardware-type>"+
				"	</test:device>"+
				"</test:test-action-container>";
		editConfig(m_server, m_clientInfo, editConfigRequest, true);
	}

	private void verifyDeviceExists(String deviceId) throws Exception{
		String expectedVerifyGetResponse =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" +
						"	<data>" +
						"		<test:test-action-container xmlns:test=\"urn:example:test-action\">" +
						"			<test:device>" +
						"				<test:device-id>" + deviceId + "</test:device-id>" +
						"				<test:hardware-type>gfast</test:hardware-type>" +
						"			</test:device>" +
						"		</test:test-action-container>" +
						"	</data>" +
						"</rpc-reply>";
		verifyGet(expectedVerifyGetResponse);
	}

	@Test
	public void testValidActionWithDeviceRefInOutput_withDeviceInDS() throws Exception {
		getModelNode();
		initialiseInterceptor();

		// verify no device is present
		verifyNoDeviceExists();

		// Create device1
		createDevice("device1");

		// verify device1
		verifyDeviceExists("device1");

		// This action returns device-list which has 'device1' as leaf-ref to device
		String actionRequest =
				"<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				"	<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
				"		<test:test-action-container xmlns:test=\"urn:example:test-action\">" +
				"			<test:get-device-list/>"+
				"		</test:test-action-container>"+
				"	</action>"+
				"</rpc>";

		ActionRequest validActionRequest = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(actionRequest));
		m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse)m_response);

		String expectedActionResponse =
				"<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"+
				"	<test:device-list xmlns:test=\"urn:example:test-action\">" +
				"		<test:hardware-type xmlns:test=\"urn:example:test-action\">123</test:hardware-type>" +
				"		<test:device-id xmlns:test=\"urn:example:test-action\">device1</test:device-id>" +
				"	</test:device-list>" +
				"</rpc-reply>";
		Document document = DocumentUtils.stringToDocument(expectedActionResponse);
		Element responseElement = document.getDocumentElement();

		TestUtil.assertXMLEquals(responseElement, m_response.getResponseDocument().getDocumentElement());
	}

	@Test
	public void testValidActionWithDeviceRefInOutput_withoutDeviceInDS() throws Exception {
		getModelNode();
		initialiseInterceptor();

		// verify no device is present
		verifyNoDeviceExists();

		// Create device2
		createDevice("device2");

		// verify device2
		verifyDeviceExists("device2");

		// This action returns list of devices which has 'device1' as leaf-ref to device
		String actionRequest =
				"<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				"	<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
				"		<test:test-action-container xmlns:test=\"urn:example:test-action\">" +
				"			<test:get-device-list/>"+
				"		</test:test-action-container>"+
				"	</action>"+
				"</rpc>";

		ActionRequest validActionRequest = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(actionRequest));
		m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse)m_response);

		// device1 is NOT present.. hence error is expected
		NetconfRpcError error = m_response.getErrors().get(0);
		assertTrue(error.getErrorType().equals(NetconfRpcErrorType.Application));
		assertTrue(error.getErrorTag().equals(NetconfRpcErrorTag.DATA_MISSING));
		assertEquals("/test:device-id", error.getErrorPath());
		assertEquals("Missing required element device1", error.getErrorMessage());

	}

	@Test
	public void testValidActionWithDeviceRefInOutputAsLeaf_withDeviceInDS() throws Exception {
		getModelNode();
		initialiseInterceptor();

		// verify no device is present
		verifyNoDeviceExists();

		// Create device1
		createDevice("device1");

		// verify device1
		verifyDeviceExists("device1");

		// This action returns device which has 'device1' as leaf-ref to device
		String actionRequest =
				"<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
						"	<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
						"		<test:test-action-container xmlns:test=\"urn:example:test-action\">" +
						"			<test:get-device/>"+
						"		</test:test-action-container>"+
						"	</action>"+
						"</rpc>";

		ActionRequest validActionRequest = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(actionRequest));
		m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse)m_response);

		String expectedActionResponse =
				"<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
				"	<test:device-id xmlns:test=\"urn:example:test-action\">device1</test:device-id>\n" +
				"</rpc-reply>";
		Document document = DocumentUtils.stringToDocument(expectedActionResponse);
		Element responseElement = document.getDocumentElement();

		TestUtil.assertXMLEquals(responseElement, m_response.getResponseDocument().getDocumentElement());
	}

	@Test
	public void testValidActionWithDeviceRefInOutputAsLeaf_withOutDeviceInDS() throws Exception {
		getModelNode();
		initialiseInterceptor();

		// verify no device is present
		verifyNoDeviceExists();

		// Create device2
		createDevice("deviceId2");

		// verify device2
		verifyDeviceExists("deviceId2");

		// This action returns device which has 'device1' as leaf-ref to device
		String actionRequest =
				"<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
						"	<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
						"		<test:test-action-container xmlns:test=\"urn:example:test-action\">" +
						"			<test:get-device/>"+
						"		</test:test-action-container>"+
						"	</action>"+
						"</rpc>";

		ActionRequest validActionRequest = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(actionRequest));
		m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse)m_response);

		String expectedActionResponse =
				"<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
						"	<test:device-id xmlns:test=\"urn:example:test-action\">device1</test:device-id>\n" +
						"</rpc-reply>";
		Document document = DocumentUtils.stringToDocument(expectedActionResponse);
		Element responseElement = document.getDocumentElement();

		// device1 is NOT present.. hence error is expected
		NetconfRpcError error = m_response.getErrors().get(0);
		assertTrue(error.getErrorType().equals(NetconfRpcErrorType.Application));
		assertTrue(error.getErrorTag().equals(NetconfRpcErrorTag.DATA_MISSING));
		assertEquals("/test:device-id", error.getErrorPath());
		assertEquals("Missing required element device1", error.getErrorMessage());
	}

	@Test
	public void testValidActionWithDeviceRefInOutputAsLeafList_withDeviceInDS() throws Exception {
		getModelNode();
		initialiseInterceptor();

		// verify no device is present
		verifyNoDeviceExists();

		// Create device1
		createDevice("device1");

		// verify device1
		verifyDeviceExists("device1");

		// Create device2
		createDevice("device2");

		// This action returns device which has 'device1' as leaf-ref to device
		String actionRequest =
				"<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
						"	<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
						"		<test:test-action-container xmlns:test=\"urn:example:test-action\">" +
						"			<test:get-device-leaf-list/>"+
						"		</test:test-action-container>"+
						"	</action>"+
						"</rpc>";

		ActionRequest validActionRequest = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(actionRequest));
		m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse)m_response);

		String expectedActionResponse =
				"<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
						"	<test:device-id xmlns:test=\"urn:example:test-action\">device1</test:device-id>\n" +
						"	<test:device-id xmlns:test=\"urn:example:test-action\">device2</test:device-id>\n" +
						"</rpc-reply>";
		Document document = DocumentUtils.stringToDocument(expectedActionResponse);
		Element responseElement = document.getDocumentElement();

		TestUtil.assertXMLEquals(responseElement, m_response.getResponseDocument().getDocumentElement());
	}

	@Test
	public void testValidActionWithDeviceRefInOutputAsLeafList_withOutDeviceInDS() throws Exception {
		getModelNode();
		initialiseInterceptor();

		// verify no device is present
		verifyNoDeviceExists();

		// Create device1
		createDevice("device1");

		// verify device1
		verifyDeviceExists("device1");

		// This action returns device which has 'device1' and 'device2' as leaf-ref to device
		String actionRequest =
				"<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
						"	<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
						"		<test:test-action-container xmlns:test=\"urn:example:test-action\">" +
						"			<test:get-device-leaf-list/>"+
						"		</test:test-action-container>"+
						"	</action>"+
						"</rpc>";

		ActionRequest validActionRequest = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(actionRequest));
		m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse)m_response);

		// device2 is NOT present.. hence error is expected
		NetconfRpcError error = m_response.getErrors().get(0);
		assertTrue(error.getErrorType().equals(NetconfRpcErrorType.Application));
		assertTrue(error.getErrorTag().equals(NetconfRpcErrorTag.DATA_MISSING));
		assertEquals("/test:device-id", error.getErrorPath());
		assertEquals("Missing required element device2", error.getErrorMessage());
	}

    @Test
    public void testInvalidActionWithoutListKey() throws NetconfMessageBuilderException, ModelNodeInitException {     
    	getModelNode();
    	initialiseInterceptor();
    	String req= "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
    			"<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
    			"<test:test-action-container xmlns:test=\"urn:example:test-action\">" +
    			"<test:action-list>"+
    			"<test:reset>"+
    			"<test:reset-at>2014-07-29T13:42:00Z</test:reset-at>"+
    			"</test:reset>"+
    			"</test:action-list>"+
    			"</test:test-action-container>"+
    			"</action>"+
    			"</rpc>";


    	ActionRequest validActionRequest = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(req));

    	m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse)m_response);
    	NetconfRpcError error = m_response.getErrors().get(0);
    	assertTrue(error.getErrorType().equals(NetconfRpcErrorType.Application));
    	assertTrue(error.getErrorTag().equals(NetconfRpcErrorTag.MISSING_ELEMENT));
    	assertEquals("/test:test-action-container/test:action-list", error.getErrorPath());
    	assertEquals("Expected list key(s) [name] is missing", error.getErrorMessage());
    }

	@Test
	public void testValidActionWithMulitpleKey() throws Exception {
		getModelNode();
		initialiseInterceptor();
		String req= "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				"<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
				"<test:test-action-container xmlns:test=\"urn:example:test-action\">" +
				"<test:action-list-multiple-key>"+
				"<test:name>nav</test:name>"+
				"<test:name1>nac</test:name1>"+
				"<test:reset>"+
				"<test:reset-at>2014-07-29T13:42:00Z</test:reset-at>"+
				"</test:reset>"+
				"</test:action-list-multiple-key>"+
				"</test:test-action-container>"+
				"</action>"+
				"</rpc>";

		ActionRequest validActionRequest = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(req));
		m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse)m_response);
		TestUtil.assertXMLEquals(getActionResponseElement(), m_response.getResponseDocument().getDocumentElement());
	}

	@Test
	public void testInvalidActionWithMissingKey() throws NetconfMessageBuilderException, ModelNodeInitException {
		getModelNode();
		initialiseInterceptor();
		String req= "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				"<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
				"<test:test-action-container xmlns:test=\"urn:example:test-action\">" +
				"<test:action-list-multiple-key>"+
				"<test:name>nav</test:name>"+
				"<test:reset>"+
				"<test:reset-at>2014-07-29T13:42:00Z</test:reset-at>"+
				"</test:reset>"+
				"</test:action-list-multiple-key>"+
				"</test:test-action-container>"+
				"</action>"+
				"</rpc>";

		ActionRequest validActionRequest = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(req));

		m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse)m_response);
		NetconfRpcError error = m_response.getErrors().get(0);

		assertTrue(error.getErrorType().equals(NetconfRpcErrorType.Application));
		assertTrue(error.getErrorTag().equals(NetconfRpcErrorTag.MISSING_ELEMENT));
		assertEquals("/test:test-action-container/test:action-list-multiple-key[test:name='nav']", error.getErrorPath());
		assertEquals("Expected list key(s) [name1] is missing", error.getErrorMessage());
	}

	@Test
	public void testInvalidActionWithDuplicateKey() throws NetconfMessageBuilderException, ModelNodeInitException {
		getModelNode();
		initialiseInterceptor();
		String req= "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				"<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
				"<test:test-action-container xmlns:test=\"urn:example:test-action\">" +
				"<test:action-list-multiple-key>"+
				"<test:name>nav</test:name>"+
				"<test:name>nac</test:name>"+
				"<test:name1>nac</test:name1>"+
				"<test:reset>"+
				"<test:reset-at>2014-07-29T13:42:00Z</test:reset-at>"+
				"</test:reset>"+
				"</test:action-list-multiple-key>"+
				"</test:test-action-container>"+
				"</action>"+
				"</rpc>";

		ActionRequest validActionRequest = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(req));

		m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse)m_response);
		NetconfRpcError error = m_response.getErrors().get(0);

		assertTrue(error.getErrorType().equals(NetconfRpcErrorType.Application));
		assertTrue(error.getErrorTag().equals(NetconfRpcErrorTag.BAD_ELEMENT));
		assertEquals("/test:test-action-container/test:action-list-multiple-key[test:name='nav'][test:name='nac'][test:name1='nac']/test:name", error.getErrorPath());
		assertEquals("Duplicate elements in node (urn:example:test-action?revision=2015-12-14)name", error.getErrorMessage());
	}

	@Test
	public void testInvalidActionWithMisplacedKey() throws Exception {
		getModelNode();
		initialiseInterceptor();
		String req= "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				"<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
				"<test:test-action-container xmlns:test=\"urn:example:test-action\">" +
				"<test:action-list-multiple-key>"+
				"<test:name>nav</test:name>"+
				"<test:reset>"+
				"<test:reset-at>2014-07-29T13:42:00Z</test:reset-at>"+
				"</test:reset>"+
				"<test:name1>nac</test:name1>"+
				"</test:action-list-multiple-key>"+
				"</test:test-action-container>"+
				"</action>"+
				"</rpc>";

		ActionRequest validActionRequest = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(req));

		m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse)m_response);
		NetconfRpcError error = m_response.getErrors().get(0);

		assertTrue(error.getErrorType().equals(NetconfRpcErrorType.Application));
		assertTrue(error.getErrorTag().equals(NetconfRpcErrorTag.MISSING_ELEMENT));
		assertEquals("/test:test-action-container/test:action-list-multiple-key[test:name='nav'][test:name1='nac']", error.getErrorPath());
		assertEquals("Expected list key(s) [name1] is not placed in the proper location in the message", error.getErrorMessage());
	}

	@Test
    public void testInvalidActionWithinNotification() throws NetconfMessageBuilderException, ModelNodeInitException {     
    	getModelNode();
    	initialiseInterceptor();
    	String req= "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
    			"<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
    			"<test:test-action-container xmlns:test=\"urn:example:test-action\">" +
    			"<test:test-notification>"+
    			"<test:action-list>"+
    			"<test:reset>"+
    			"<test:reset-at>2014-07-29T13:42:00Z</test:reset-at>"+
    			"</test:reset>"+
    			"</test:action-list>"+
    			"</test:test-notification>"+
    			"</test:test-action-container>"+
    			"</action>"+
    			"</rpc>";


    	ActionRequest validActionRequest = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(req));

    	m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse)m_response);
    	Map<String, String> prefixToNsMap = new HashMap<>();
    	prefixToNsMap.put(PREFIX, NAMESPACE);
    	NetconfRpcError error = m_response.getErrors().get(0);
    	assertTrue(error.getErrorType().equals(NetconfRpcErrorType.Protocol));
    	assertTrue(error.getErrorTag().equals(NetconfRpcErrorTag.BAD_ELEMENT));
    	assertNull(error.getErrorPath());
    	assertNull(error.getErrorPathNsByPrefix());
    	assertEquals("No matched action found on the models", error.getErrorMessage());
    }
    
    @Test
    public void testInvalidActionWithinRPC() throws NetconfMessageBuilderException, ModelNodeInitException {     
    	getModelNode();
    	initialiseInterceptor();
    	String req= "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
    			"<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
    			"<test:test-action-container xmlns:test=\"urn:example:test-action\">" +
    			"<test:test-rpc>"+
    			"<test:action-list>"+
    			"<test:reset>"+
    			"<test:reset-at>2014-07-29T13:42:00Z</test:reset-at>"+
    			"</test:reset>"+
    			"</test:action-list>"+
    			"</test:test-rpc>"+
    			"</test:test-action-container>"+
    			"</action>"+
    			"</rpc>";


    	ActionRequest validActionRequest = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(req));

    	m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse)m_response);
    	Map<String, String> prefixToNsMap = new HashMap<>();
    	prefixToNsMap.put(PREFIX, NAMESPACE);
    	NetconfRpcError error = m_response.getErrors().get(0);
    	assertTrue(error.getErrorType().equals(NetconfRpcErrorType.Protocol));
    	assertTrue(error.getErrorTag().equals(NetconfRpcErrorTag.BAD_ELEMENT));
    	assertNull(error.getErrorPath());
    	assertNull(error.getErrorPathNsByPrefix());
    	assertEquals("No matched action found on the models", error.getErrorMessage());
    }
    
    @Test
    public void testInvalidActionWithinAction() throws NetconfMessageBuilderException, ModelNodeInitException {     
    	getModelNode();
    	initialiseInterceptor();
    	String req= "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
    			"<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
    			"<test:test-action-container xmlns:test=\"urn:example:test-action\">" +
    			"<test:container-reset>"+
    			"<test:action-list>"+
    			"<test:name>apache</test:name>"+
    			"<test:reset>"+
    			"<test:reset-at>2014-07-29T13:42:00Z</test:reset-at>"+
    			"</test:reset>"+
    			"</test:action-list>"+
    			"</test:container-reset>"+
    			"</test:test-action-container>"+
    			"</action>"+
    			"</rpc>";


    	ActionRequest validActionRequest = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(req));			
    	m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse)m_response);
    	Map<String, String> prefixToNsMap = new HashMap<>();
    	prefixToNsMap.put(PREFIX, NAMESPACE);
    	NetconfRpcError error = m_response.getErrors().get(0);
    	assertEquals(error.getErrorType(), NetconfRpcErrorType.Application);
    	assertEquals(error.getErrorTag(), NetconfRpcErrorTag.UNKNOWN_ELEMENT);
    	assertEquals("An unexpected element 'action-list' is present", error.getErrorMessage());
    	assertEquals("/test:input/test:action-list", error.getErrorPath());
    	assertEquals(prefixToNsMap, error.getErrorPathNsByPrefix());
    }
    
    @Test
    public void testValidAction_withoutOutput() throws Exception {     
    	getModelNode();
    	initialiseInterceptor();
    	String req= "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
    			"<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
    			"	<test:test-action-container1 xmlns:test=\"urn:example:test-action\">" +
    			"		<test:test-action>"+
    			"			<test:leaf1>bbf</test:leaf1>"+
    			"		</test:test-action>"+
    			"	</test:test-action-container1>"+
    			"</action>"+
    			"</rpc>";
    	ActionRequest validActionRequest = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(req));
    	m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse)m_response);
    	TestUtil.assertXMLEquals(getActionResponseElement_OK(), m_response.getResponseDocument().getDocumentElement());
    }
    
    @Test
    public void testAction_InvalidOutputResponse() throws Exception {     
    	getModelNode();
    	initialiseInterceptor();
    	String req= "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
    			"<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
    			"	<test:test-action-container2 xmlns:test=\"urn:example:test-action\">" +
    			"		<test:test-action1>"+
    			"			<test:leaf1>bbf</test:leaf1>"+
    			"		</test:test-action1>"+
    			"	</test:test-action-container2>"+
    			"</action>"+
    			"</rpc>";
    	DataStore dataStore = mock(DataStore.class);
    	m_server.setRunningDataStore(dataStore);
    	List<Element> list = new ArrayList<Element>();
    	list.add(DocumentUtils.createDocument().createElement("ok"));
    	when(dataStore.action(any(),any(),any(),any())).thenReturn(list);
    	when(dataStore.withValidationContext(any())).thenCallRealMethod();
    	ActionRequest validActionRequest = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(req));
    	m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse)m_response);
    	NetconfRpcError error = m_response.getErrors().get(0);
    	assertTrue(error.getErrorType().equals(NetconfRpcErrorType.Application));
    	assertTrue(error.getErrorTag().equals(NetconfRpcErrorTag.OPERATION_FAILED));
    	assertEquals("<ok/> cannot be part of response if there is output data", error.getErrorMessage());
    }
    
    @Test
    public void testAction_InsideChoiceAndCase() throws Exception {     
    	getModelNode();
    	initialiseInterceptor();
    	String reqest = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
    			"<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
    			"<test:test-action-choice-container xmlns:test=\"urn:example:test-action\">" +
    			"<test:choice-container>"+
    			"<test:pmd-control>"+
    			"<test:test-action-request>"+
    			"<test:input1>test</test:input1>"+
    			"</test:test-action-request>"+
    			"</test:pmd-control>"+
    			"</test:choice-container>"+
    			"</test:test-action-choice-container>"+
    			"</action>"+
    			"</rpc>";

    	ActionRequest validActionRequest = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(reqest));		
    	m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse)m_response);
    	TestUtil.assertXMLEquals(getActionResponseElement_OK(), m_response.getResponseDocument().getDocumentElement());
    }
    
    @Test
    public void testActionInput_ListWithLeafref() throws Exception {
    	getModelNode();
    	initialiseInterceptor();

    	//Create interface
    	String editConfigRequest = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
    			+ "<devices xmlns=\"urn:example:test-action\">" 
    			+ "	<interface>"
    			+ "		<name>interface1</name>" 
    			+ "		<type>type1</type>" 
    			+ "	</interface>"
    			+ "</devices>";

    	editConfig(m_server, m_clientInfo, editConfigRequest, true);
    	
    	//verify GET request
    	String response = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
    			+"<data>"
    			+"<test:test-action-container xmlns:test=\"urn:example:test-action\"/>"
    			+"	<test:devices xmlns:test=\"urn:example:test-action\">"
    			+"		<test:interface>"
    			+"			<test:enabled>true</test:enabled>"
    			+"			<test:name>interface1</test:name>"
    			+"			<test:type>type1</test:type>"
    			+"		</test:interface>"
    			+"	</test:devices>"
    			+"</data>"
    			+"</rpc-reply>"
    			;
    	verifyGet(response);

    	// action to verify list/leaf with leafref case
    	String actionRequest = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
    			+ "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">"
    			+ "	<test:test-action-leafref-container xmlns:test=\"urn:example:test-action\">"
    			+ "	  <test:input-list-leafref-action>" 
    			+ "		<test:input-list>"
    			+ "			<test:interface>interface1</test:interface>" 
    			+ "		</test:input-list>"
    			+ "	  </test:input-list-leafref-action>" 
    			+ "	</test:test-action-leafref-container>" 
    			+ "</action>"
    			+ "</rpc>";

    	ActionRequest validActionRequest = DocumentToPojoTransformer
    			.getAction(DocumentUtils.stringToDocument(actionRequest));
    	m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);

    	//verify ok response
    	TestUtil.assertXMLEquals(getActionResponseElement_OK(), m_response.getResponseDocument().getDocumentElement());

    	// verify invalid leafref value 'bbf'
    	actionRequest = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
    			+ "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">"
    			+ "	<test:test-action-leafref-container xmlns:test=\"urn:example:test-action\">"
    			+ "	  <test:input-list-leafref-action>" 
    			+ "		<test:input-list>"
    			+ "			<test:interface>bbf</test:interface>"
    			+ "		</test:input-list>"
    			+ "	  </test:input-list-leafref-action>" 
    			+ "	</test:test-action-leafref-container>" 
    			+ "</action>"
    			+ "</rpc>";

    	validActionRequest = DocumentToPojoTransformer
    			.getAction(DocumentUtils.stringToDocument(actionRequest));
    	m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);

    	NetconfRpcError error = m_response.getErrors().get(0);
    	assertEquals(NetconfRpcErrorType.Application, error.getErrorType());
    	assertEquals(NetconfRpcErrorTag.DATA_MISSING, error.getErrorTag());
    	assertEquals("instance-required",error.getErrorAppTag());
    	assertEquals("/test:input-list-leafref-action/test:input-list/test:interface", error.getErrorPath());
    	assertEquals("Missing required element bbf", error.getErrorMessage());
    }

    @Test
    public void testActionInput_ContainerInsideLeafrefValidation() throws Exception {
    	getModelNode();
    	initialiseInterceptor();

    	//create interface
    	String editConfigRequest = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
    			+ "<devices xmlns=\"urn:example:test-action\">" 
    			+ "	<interface>"
    			+ "		<name>interface1</name>" 
    			+ "		<type>type1</type>" 
    			+ "	</interface>"
    			+ "	<interface>"
    			+ "		<name>interface2</name>" 
    			+ "		<type>type2</type>" 
    			+ "	</interface>"
    			+ "</devices>";

    	editConfig(m_server, m_clientInfo, editConfigRequest, true);

    	//verify GET request
    	String response = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
    			+"<data>"
    			+"<test:test-action-container xmlns:test=\"urn:example:test-action\"/>"
    			+"	<test:devices xmlns:test=\"urn:example:test-action\">"
    			+"		<test:interface>"
    			+"			<test:enabled>true</test:enabled>"
    			+"			<test:name>interface1</test:name>"
    			+"			<test:type>type1</test:type>"
    			+"		</test:interface>"
    			+"		<test:interface>"
    			+"			<test:enabled>true</test:enabled>"
    			+"			<test:name>interface2</test:name>"
    			+"			<test:type>type2</test:type>"
    			+"		</test:interface>"
    			+"	</test:devices>"
    			+"</data>"
    			+"</rpc-reply>"
    			;
    	verifyGet(response);

    	// verify input container/leaf with leafref case
    	String actionRequest = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
    			+ "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">"
    			+ "	<test:test-action-leafref-container xmlns:test=\"urn:example:test-action\">"
    			+ "	  <test:input-container-leafref-action>" 
    			+ "		<test:input-container>"
    			+ "			<test:interface>interface1</test:interface>" 
    			+ "		</test:input-container>"
    			+ "	  </test:input-container-leafref-action>" 
    			+ "	</test:test-action-leafref-container>" 
    			+ "</action>"
    			+ "</rpc>";

    	ActionRequest validActionRequest = DocumentToPojoTransformer
    			.getAction(DocumentUtils.stringToDocument(actionRequest));
    	m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);

    	//Verify ok response
    	TestUtil.assertXMLEquals(getActionResponseElement_OK(), m_response.getResponseDocument().getDocumentElement());

    	// verify leafref case for interface2
    	actionRequest = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
    			+ "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">"
    			+ "	<test:test-action-leafref-container xmlns:test=\"urn:example:test-action\">"
    			+ "	  <test:input-container-leafref-action>" 
    			+ "		<test:input-container>"
    			+ "			<test:interface>interface2</test:interface>" 
    			+ "		</test:input-container>"
    			+ "	  </test:input-container-leafref-action>" 
    			+ "	</test:test-action-leafref-container>" 
    			+ "</action>"
    			+ "</rpc>";

    	validActionRequest = DocumentToPojoTransformer
    			.getAction(DocumentUtils.stringToDocument(actionRequest));
    	m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);

    	//Verify ok response
    	TestUtil.assertXMLEquals(getActionResponseElement_OK(), m_response.getResponseDocument().getDocumentElement());

    	// verify invalid leafref value 'bbf'
    	actionRequest = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
    			+ "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">"
    			+ "	<test:test-action-leafref-container xmlns:test=\"urn:example:test-action\">"
    			+ "	  <test:input-container-leafref-action>" 
    			+ "		<test:input-container>"
    			+ "			<test:interface>bbf</test:interface>"
    			+ "		</test:input-container>"
    			+ "	  </test:input-container-leafref-action>" 
    			+ "	</test:test-action-leafref-container>" 
    			+ "</action>"
    			+ "</rpc>";

    	validActionRequest = DocumentToPojoTransformer
    			.getAction(DocumentUtils.stringToDocument(actionRequest));
    	m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);

    	NetconfRpcError error = m_response.getErrors().get(0);
    	assertEquals(NetconfRpcErrorType.Application, error.getErrorType());
    	assertEquals(NetconfRpcErrorTag.DATA_MISSING, error.getErrorTag());
    	assertEquals("instance-required",error.getErrorAppTag());
    	assertEquals("/test:input-container-leafref-action/test:input-container/test:interface", error.getErrorPath());
    	assertEquals("Missing required element bbf", error.getErrorMessage());
    }
    @Test
    public void testActionInput_LeafrefWithCurrent() throws Exception {
    	getModelNode();
    	initialiseInterceptor();

    	//Create interface
    	String editConfigRequest = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
    			+ "<devices xmlns=\"urn:example:test-action\">" 
    			+ "	<interface>"
    			+ "		<name>interface1</name>" 
    			+ "		<type>type1</type>" 
    			+ "	</interface>"
    			+ "</devices>";

    	editConfig(m_server, m_clientInfo, editConfigRequest, true);

    	//verify GET request
    	String response = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
    			+"<data>"
    			+"<test:test-action-container xmlns:test=\"urn:example:test-action\"/>"
    			+"	<test:devices xmlns:test=\"urn:example:test-action\">"
    			+"		<test:interface>"
    			+"			<test:enabled>true</test:enabled>"
    			+"			<test:name>interface1</test:name>"
    			+"			<test:type>type1</test:type>"
    			+"		</test:interface>"
    			+"	</test:devices>"
    			+"</data>"
    			+"</rpc-reply>"
    			;
    	verifyGet(response);

    	// verify input container/leaf with leafref case - xpath with current
    	String actionRequest = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
    			+ "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">"
    			+ "	<test:test-action-leafref-container xmlns:test=\"urn:example:test-action\">"
    			+ "	  <test:input-container-leafref-action>" 
    			+ "		<test:input-container>"
    			+ "			<test:name>interface1</test:name>"
    			+ "			<test:interface-type-ref>type1</test:interface-type-ref>" 
    			+ "		</test:input-container>"
    			+ "	  </test:input-container-leafref-action>" 
    			+ "	</test:test-action-leafref-container>" 
    			+ "</action>"
    			+ "</rpc>";

    	ActionRequest validActionRequest = DocumentToPojoTransformer
    			.getAction(DocumentUtils.stringToDocument(actionRequest));
    	m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);
    	TestUtil.assertXMLEquals(getActionResponseElement_OK(), m_response.getResponseDocument().getDocumentElement());

    	// verify invalid leaf-ref value
    	actionRequest = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
    			+ "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">"
    			+ "	<test:test-action-leafref-container xmlns:test=\"urn:example:test-action\">"
    			+ "	  <test:input-container-leafref-action>" 
    			+ "		<test:input-container>"
    			+ "			<test:name>interface5</test:name>"
    			+ "			<test:interface-type-ref>type1</test:interface-type-ref>" 
    			+ "		</test:input-container>"
    			+ "	  </test:input-container-leafref-action>" 
    			+ "	</test:test-action-leafref-container>" 
    			+ "</action>"
    			+ "</rpc>";

    	validActionRequest = DocumentToPojoTransformer
    			.getAction(DocumentUtils.stringToDocument(actionRequest));
    	m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);
    	NetconfRpcError error = m_response.getErrors().get(0);
    	assertEquals(NetconfRpcErrorType.Application, error.getErrorType());
    	assertEquals(NetconfRpcErrorTag.DATA_MISSING, error.getErrorTag());
    	assertEquals("instance-required",error.getErrorAppTag());
    	assertEquals("/test:input-container-leafref-action/test:input-container/test:interface-type-ref", error.getErrorPath());
    	assertEquals("Missing required element type1", error.getErrorMessage());

    	// verify invalid leaf-ref value for type
    	actionRequest = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
    			+ "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">"
    			+ "	<test:test-action-leafref-container xmlns:test=\"urn:example:test-action\">"
    			+ "	  <test:input-container-leafref-action>" 
    			+ "		<test:input-container>"
    			+ "			<test:name>interface1</test:name>"
    			+ "			<test:interface-type-ref>type2</test:interface-type-ref>" 
    			+ "		</test:input-container>"
    			+ "	  </test:input-container-leafref-action>" 
    			+ "	</test:test-action-leafref-container>" 
    			+ "</action>"
    			+ "</rpc>";

    	validActionRequest = DocumentToPojoTransformer
    			.getAction(DocumentUtils.stringToDocument(actionRequest));
    	m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);
    	error = m_response.getErrors().get(0);
    	assertEquals(NetconfRpcErrorType.Application, error.getErrorType());
    	assertEquals(NetconfRpcErrorTag.DATA_MISSING, error.getErrorTag());
    	assertEquals("instance-required",error.getErrorAppTag());
    	assertEquals("/test:input-container-leafref-action/test:input-container/test:interface-type-ref", error.getErrorPath());
    	assertEquals("Missing required element type1", error.getErrorMessage());
    }

    @Test
    public void testActionInput_LeafListWithLeafref() throws Exception {
    	getModelNode();
    	initialiseInterceptor();

    	//Create interfaces and profiles
    	String editConfigRequest = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
    			+ "<devices xmlns=\"urn:example:test-action\">" 
    			+ "	<interface>"
    			+ "		<name>interface1</name>" 
    			+ "		<type>type1</type>" 
    			+ "	</interface>"
    			+ "	<profile>"
    			+ "		<name>profile1</name>" 
    			+ "	</profile>"
    			+ "	<profile>"
    			+ "		<name>profile2</name>" 
    			+ "	</profile>"
    			+ "	<profile>"
    			+ "		<name>profile3</name>" 
    			+ "	</profile>"
    			+ "</devices>";

    	editConfig(m_server, m_clientInfo, editConfigRequest, true);

    	//verify GET request
    	String response = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
    			+"<data>"
    			+"<test:test-action-container xmlns:test=\"urn:example:test-action\"/>"
    			+"	<test:devices xmlns:test=\"urn:example:test-action\">"
    			+"		<test:interface>"
    			+"			<test:enabled>true</test:enabled>"
    			+"			<test:name>interface1</test:name>"
    			+"			<test:type>type1</test:type>"
    			+"		</test:interface>"
    			+"		<test:profile>"
    			+"			<test:name>profile1</test:name>"
    			+"		</test:profile>"
    			+"		<test:profile>"
    			+"			<test:name>profile2</test:name>"
    			+"		</test:profile>"
    			+"		<test:profile>"
    			+"			<test:name>profile3</test:name>"
    			+"		</test:profile>"
    			+"	</test:devices>"
    			+"</data>"
    			+"</rpc-reply>"
    			;
    	verifyGet(response);

    	// Leaf-list with leafref case 
    	String actionRequest = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
    			+ "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">"
    			+ "	<test:test-action-leafref-container xmlns:test=\"urn:example:test-action\">"
    			+ "	  <test:input-leafref-action>" 
    			+ "			<test:interface>interface1</test:interface>"
    			+ "			<test:profile>profile1</test:profile>"
    			+ "	  </test:input-leafref-action>" 
    			+ "	</test:test-action-leafref-container>" 
    			+ "</action>"
    			+ "</rpc>";

    	ActionRequest validActionRequest = DocumentToPojoTransformer
    			.getAction(DocumentUtils.stringToDocument(actionRequest));
    	m_response = new ActionResponse().setMessageId("1");
    	m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);
    	TestUtil.assertXMLEquals(getActionResponseElement_OK(), m_response.getResponseDocument().getDocumentElement());

    	// Leaf-list with leafref case 
    	actionRequest = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
    			+ "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">"
    			+ "	<test:test-action-leafref-container xmlns:test=\"urn:example:test-action\">"
    			+ "	  <test:input-leafref-action>" 
    			+ "			<test:interface>interface1</test:interface>"
    			+ "			<test:profile>profile2</test:profile>"
    			+ "			<test:profile>profile3</test:profile>"
    			+ "	  </test:input-leafref-action>" 
    			+ "	</test:test-action-leafref-container>" 
    			+ "</action>"
    			+ "</rpc>";

    	validActionRequest = DocumentToPojoTransformer
    			.getAction(DocumentUtils.stringToDocument(actionRequest));
    	m_response = new ActionResponse().setMessageId("1");
    	m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);
    	TestUtil.assertXMLEquals(getActionResponseElement_OK(), m_response.getResponseDocument().getDocumentElement());

    	// verify invalid leaf-ref value
    	actionRequest = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
    			+ "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">"
    			+ "	<test:test-action-leafref-container xmlns:test=\"urn:example:test-action\">"
    			+ "	  <test:input-leafref-action>" 
    			+ "			<test:profile>profile3</test:profile>"
    			+ "			<test:profile>profile4</test:profile>"
    			+ "	  </test:input-leafref-action>" 
    			+ "	</test:test-action-leafref-container>" 
    			+ "</action>"
    			+ "</rpc>";

    	validActionRequest = DocumentToPojoTransformer
    			.getAction(DocumentUtils.stringToDocument(actionRequest));
    	m_response = new ActionResponse().setMessageId("1");
    	m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);
    	NetconfRpcError error = m_response.getErrors().get(0);
    	assertEquals(NetconfRpcErrorType.Application, error.getErrorType());
    	assertEquals(NetconfRpcErrorTag.DATA_MISSING, error.getErrorTag());
    	assertEquals("instance-required",error.getErrorAppTag());
    	assertEquals("/test:input-leafref-action/test:profile", error.getErrorPath());
    	assertEquals("Missing required element profile4", error.getErrorMessage());
    }


    @Test
    public void testActionInput_ChoiceCasesWithDefualtLeafrefValidation() throws Exception {
    	getModelNode();
    	initialiseInterceptor();

    	// verify leafref referred to default value 
    	String actionRequest = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
    			+ "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">"
    			+ "	<test:test-action-leafref-container xmlns:test=\"urn:example:test-action\">"
    			+ "	  <test:input-choice-leafref-action>" 
    			+ "		<test:input-container>"
    			+ "		<test:list1>"
    			+ "			<test:name>key1</test:name>"
    			+ "			<test:testing>test</test:testing>" 
    			+ "		</test:list1>"
    			+ "		</test:input-container>"
    			+ "	  </test:input-choice-leafref-action>" 
    			+ "	</test:test-action-leafref-container>" 
    			+ "</action>"
    			+ "</rpc>";

    	ActionRequest validActionRequest = DocumentToPojoTransformer
    			.getAction(DocumentUtils.stringToDocument(actionRequest));
    	m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);
    	TestUtil.assertXMLEquals(getActionResponseElement_OK(), m_response.getResponseDocument().getDocumentElement());

    	//verify invalid leafref which is referred to with-in input elemets
    	actionRequest = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
    			+ "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">"
    			+ "	<test:test-action-leafref-container xmlns:test=\"urn:example:test-action\">"
    			+ "	  <test:input-choice-leafref-action>" 
    			+ "		<test:input-container>"
    			+ "		<test:list1>"
    			+ "			<test:name>key1</test:name>"
    			+ "			<test:default-leaf>testing</test:default-leaf>"
    			+ "			<test:testing>test</test:testing>" 
    			+ "		</test:list1>"
    			+ "		</test:input-container>"
    			+ "	  </test:input-choice-leafref-action>" 
    			+ "	</test:test-action-leafref-container>" 
    			+ "</action>"
    			+ "</rpc>";

    	validActionRequest = DocumentToPojoTransformer
    			.getAction(DocumentUtils.stringToDocument(actionRequest));
    	m_response = new ActionResponse().setMessageId("1");
    	m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);
    	NetconfRpcError error = m_response.getErrors().get(0);
    	assertEquals(NetconfRpcErrorType.Application, error.getErrorType());
    	assertEquals(NetconfRpcErrorTag.DATA_MISSING, error.getErrorTag());
    	assertEquals("instance-required",error.getErrorAppTag());
    	assertEquals("/test:input-choice-leafref-action/test:input-container/test:list1/test:testing", error.getErrorPath());
    	assertEquals("Missing required element test", error.getErrorMessage());
    }

    @Test
    public void testActionInput_ChoiceCasesWithLeafrefValidation() throws Exception {
    	getModelNode();
    	initialiseInterceptor();

    	//Create interfaces and profiles
    	String editConfigRequest = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
    			+ "<devices xmlns=\"urn:example:test-action\">" 
    			+ "	<interface>"
    			+ "		<name>interface1</name>" 
    			+ "		<type>type1</type>" 
    			+ "	</interface>"
    			+ "	<interface>"
    			+ "		<name>interface2</name>" 
    			+ "		<type>type2</type>" 
    			+ "	</interface>"
    			+ "	<profile>"
    			+ "		<name>profile1</name>" 
    			+ "	</profile>"
    			+ "</devices>";

    	editConfig(m_server, m_clientInfo, editConfigRequest, true);

    	//verify GET request
    	String response = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
    			+"<data>"
    			+"<test:test-action-container xmlns:test=\"urn:example:test-action\"/>"
    			+"	<test:devices xmlns:test=\"urn:example:test-action\">"
    			+"		<test:interface>"
    			+"			<test:enabled>true</test:enabled>"
    			+"			<test:name>interface1</test:name>"
    			+"			<test:type>type1</test:type>"
    			+"		</test:interface>"
    			+"		<test:interface>"
    			+"			<test:enabled>true</test:enabled>"
    			+"			<test:name>interface2</test:name>"
    			+"			<test:type>type2</test:type>"
    			+"		</test:interface>"
    			+"		<test:profile>"
    			+"			<test:name>profile1</test:name>"
    			+"		</test:profile>"
    			+"	</test:devices>"
    			+"</data>"
    			+"</rpc-reply>"
    			;
    	verifyGet(response);

    	// verify valid leafref value for leaf-list
    	String actionRequest = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
    			+ "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">"
    			+ "	<test:test-action-leafref-container xmlns:test=\"urn:example:test-action\">"
    			+ "	  <test:input-choice-leafref-action>" 
    			+ "		<test:input-container>"
    			+ "			<test:profile>profile1</test:profile>"
    			+ "		</test:input-container>"
    			+ "	  </test:input-choice-leafref-action>" 
    			+ "	</test:test-action-leafref-container>" 
    			+ "</action>"
    			+ "</rpc>";

    	ActionRequest validActionRequest = DocumentToPojoTransformer
    			.getAction(DocumentUtils.stringToDocument(actionRequest));
    	m_response = new ActionResponse().setMessageId("1");
    	m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);
    	TestUtil.assertXMLEquals(getActionResponseElement_OK(), m_response.getResponseDocument().getDocumentElement());

    	// verify invalid leafref value
    	actionRequest = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
    			+ "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">"
    			+ "	<test:test-action-leafref-container xmlns:test=\"urn:example:test-action\">"
    			+ "	  <test:input-choice-leafref-action>" 
    			+ "		<test:input-container>"
    			+ "			<test:profile>profile1</test:profile>"
    			+ "			<test:profile>profile2</test:profile>"
    			+ "		</test:input-container>"
    			+ "	  </test:input-choice-leafref-action>" 
    			+ "	</test:test-action-leafref-container>" 
    			+ "</action>"
    			+ "</rpc>";

    	validActionRequest = DocumentToPojoTransformer
    			.getAction(DocumentUtils.stringToDocument(actionRequest));
    	m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);
    	NetconfRpcError error = m_response.getErrors().get(0);
    	assertEquals(NetconfRpcErrorType.Application, error.getErrorType());
    	assertEquals(NetconfRpcErrorTag.DATA_MISSING, error.getErrorTag());
    	assertEquals("instance-required",error.getErrorAppTag());
    	assertEquals("/test:input-choice-leafref-action/test:input-container/test:profile", error.getErrorPath());
    	assertEquals("Missing required element profile2", error.getErrorMessage());
    }

    @Test
    public void testActionInput_NestedChoiceCasesWithLeafrefValidation() throws Exception {
    	getModelNode();
    	initialiseInterceptor();

    	//Create profile
    	String editConfigRequest = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
    			+ "<devices xmlns=\"urn:example:test-action\">" 
    			+ "	<profile>"
    			+ "		<name>profile1</name>" 
    			+ "	</profile>"
    			+ "</devices>";

    	editConfig(m_server, m_clientInfo, editConfigRequest, true);

    	//verify GET request
    	String response = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
    			+"<data>"
    			+"<test:test-action-container xmlns:test=\"urn:example:test-action\"/>"
    			+"	<test:devices xmlns:test=\"urn:example:test-action\">"
    			+"		<test:profile>"
    			+"			<test:name>profile1</test:name>"
    			+"		</test:profile>"
    			+"	</test:devices>"
    			+"</data>"
    			+"</rpc-reply>"
    			;
    	verifyGet(response);
    	
    	//Missing mandatory nodes with nested choice case
    	String actionRequest = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
    			+ "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">"
    			+ "	<test:test-action-leafref-container xmlns:test=\"urn:example:test-action\">"
    			+ "	  <test:input-choice-leafref-action>" 
    			+ "		<test:input-container>"
    			+ "			<test:type>type1</test:type>"
    			+ "		</test:input-container>"
    			+ "	  </test:input-choice-leafref-action>" 
    			+ "	</test:test-action-leafref-container>" 
    			+ "</action>"
    			+ "</rpc>";

    	ActionRequest validActionRequest = DocumentToPojoTransformer
    			.getAction(DocumentUtils.stringToDocument(actionRequest));
    	m_response = new ActionResponse().setMessageId("1");
    	m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);
    	assertEquals(1,m_response.getErrors().size());
		NetconfRpcError rpcError = m_response.getErrors().get(0);
		assertEquals(NetconfRpcErrorType.Application, rpcError.getErrorType());
		assertEquals(NetconfRpcErrorSeverity.Error, rpcError.getErrorSeverity());
		assertEquals(NetconfRpcErrorTag.DATA_MISSING, rpcError.getErrorTag());
		assertEquals("Mandatory leaf 'mandatory-leaf' is missing", rpcError.getErrorMessage());
		assertEquals("/test:input/test:input-container/test:mandatory-leaf", rpcError.getErrorPath());

    	// action with nested choice-case
    	actionRequest = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
    			+ "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">"
    			+ "	<test:test-action-leafref-container xmlns:test=\"urn:example:test-action\">"
    			+ "	  <test:input-choice-leafref-action>" 
    			+ "		<test:input-container>"
    			+ "			<test:type>type1</test:type>"
    			+ "			<test:mandatory-leaf>mandatory</test:mandatory-leaf>"	
    			+ "		</test:input-container>"
    			+ "	  </test:input-choice-leafref-action>" 
    			+ "	</test:test-action-leafref-container>" 
    			+ "</action>"
    			+ "</rpc>";

    	validActionRequest = DocumentToPojoTransformer
    			.getAction(DocumentUtils.stringToDocument(actionRequest));
    	m_response = new ActionResponse().setMessageId("1");
    	m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);
    	TestUtil.assertXMLEquals(getActionResponseElement_OK(), m_response.getResponseDocument().getDocumentElement());

    	// nested choice with leaf-ref
    	actionRequest = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
    			+ "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">"
    			+ "	<test:test-action-leafref-container xmlns:test=\"urn:example:test-action\">"
    			+ "	  <test:input-choice-leafref-action>" 
    			+ "		<test:input-container>"
    			+ "			<test:innerList>"
    			+ "				<test:id>10</test:id>"
    			+ "				<test:refvalue>profile1</test:refvalue>"	
    			+ "			</test:innerList>"
    			+ "		</test:input-container>"
    			+ "	  </test:input-choice-leafref-action>" 
    			+ "	</test:test-action-leafref-container>" 
    			+ "</action>"
    			+ "</rpc>";

    	validActionRequest = DocumentToPojoTransformer
    			.getAction(DocumentUtils.stringToDocument(actionRequest));
    	m_response = new ActionResponse().setMessageId("1");
    	m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);
    	TestUtil.assertXMLEquals(getActionResponseElement_OK(), m_response.getResponseDocument().getDocumentElement());

    	// invalid leaf-ref value
    	actionRequest = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
    			+ "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">"
    			+ "	<test:test-action-leafref-container xmlns:test=\"urn:example:test-action\">"
    			+ "	  <test:input-choice-leafref-action>" 
    			+ "		<test:input-container>"
    			+ "			<test:innerList>"
    			+ "				<test:id>10</test:id>"
    			+ "				<test:refvalue>profile5</test:refvalue>"	
    			+ "			</test:innerList>"
    			+ "		</test:input-container>"
    			+ "	  </test:input-choice-leafref-action>" 
    			+ "	</test:test-action-leafref-container>" 
    			+ "</action>"
    			+ "</rpc>";

    	validActionRequest = DocumentToPojoTransformer
    			.getAction(DocumentUtils.stringToDocument(actionRequest));
    	m_response = new ActionResponse().setMessageId("1");
    	m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);
    	NetconfRpcError error = m_response.getErrors().get(0);
    	assertEquals(NetconfRpcErrorType.Application, error.getErrorType());
    	assertEquals(NetconfRpcErrorTag.DATA_MISSING, error.getErrorTag());
    	assertEquals("instance-required",error.getErrorAppTag());
    	assertEquals("/test:input-choice-leafref-action/test:input-container/test:innerList/test:refvalue", error.getErrorPath());
    	assertEquals("Missing required element profile5", error.getErrorMessage());
    }

    @Test
    public void testActionInput_MustConstraintsCurrentWithDerivedFromORSelf() throws Exception {
    	getModelNode();
    	initialiseInterceptor();

    	//Create interfaces and profiles
    	String editConfigRequest = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
    			+ "<devices xmlns=\"urn:example:test-action\">" 
    			+ "	<interface>"
    			+ "		<name>interface1</name>" 
    			+ "		<type>type1</type>"
    			+ "		<identity-leaf>identity1</identity-leaf>" 
    			+ "	</interface>"
    			+ "	<interface>"
    			+ "		<name>interface2</name>" 
    			+ "		<type>type2</type>"
    			+ "		<identity-leaf>identity2</identity-leaf>" 
    			+ "	</interface>"
    			+ "</devices>";

    	editConfig(m_server, m_clientInfo, editConfigRequest, true);

    	//verify GET request
    	String response = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
    			+"<data>"
    			+"<test:test-action-container xmlns:test=\"urn:example:test-action\"/>"
    			+"	<test:devices xmlns:test=\"urn:example:test-action\">"
    			+"		<test:interface>"
    			+"			<test:name>interface1</test:name>"
    			+"			<test:enabled>true</test:enabled>"
    			+"			<test:identity-leaf>test:identity1</test:identity-leaf>"
    			+"			<test:type>type1</test:type>"
    			+"		</test:interface>"
    			+"		<test:interface>"
    			+"			<test:name>interface2</test:name>"
    			+"			<test:enabled>true</test:enabled>"
    			+"			<test:identity-leaf>test:identity2</test:identity-leaf>"
    			+"			<test:type>type2</test:type>"
    			+"		</test:interface>"
    			+"	</test:devices>"
    			+"</data>"
    			+"</rpc-reply>"
    			;
    	verifyGet(response);

    	// verify identity value
    	String actionRequest = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
    			+ "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">"
    			+ "	<test:test-action-when-must-validation xmlns:test=\"urn:example:test-action\">"
    			+ "	  <test:must-with-derived-from-or-self-validation>" 
    			+ "		<test:interface>interface1</test:interface>"
    			+ "	  </test:must-with-derived-from-or-self-validation>" 
    			+ "	</test:test-action-when-must-validation>" 
    			+ "</action>"
    			+ "</rpc>";

    	ActionRequest validActionRequest = DocumentToPojoTransformer
    			.getAction(DocumentUtils.stringToDocument(actionRequest));
    	m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);
    	TestUtil.assertXMLEquals(getActionResponseElement_OK(), m_response.getResponseDocument().getDocumentElement());

    	// invalid identify type value
    	actionRequest = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
    			+ "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">"
    			+ "	<test:test-action-when-must-validation xmlns:test=\"urn:example:test-action\">"
    			+ "	  <test:must-with-derived-from-or-self-validation>" 
    			+ "		<test:interface>interface2</test:interface>"
    			+ "	  </test:must-with-derived-from-or-self-validation>" 
    			+ "	</test:test-action-when-must-validation>" 
    			+ "</action>"
    			+ "</rpc>";

    	validActionRequest = DocumentToPojoTransformer
    			.getAction(DocumentUtils.stringToDocument(actionRequest));
    	m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);

    	NetconfRpcError error = m_response.getErrors().get(0);
    	assertEquals(NetconfRpcErrorType.Application, error.getErrorType());
    	assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, error.getErrorTag());
    	assertEquals("must-violation",error.getErrorAppTag());
    	assertEquals("Violate must constraints: derived-from-or-self(/test:devices/test:interface[test:name=current()]/test:identity-leaf,'test:identity1')", error.getErrorMessage());

    }

    @Test
    public void testActionInput_MustConstraintsCurrentWithCoreOperation() throws Exception {
    	getModelNode();
    	initialiseInterceptor();

    	//Create interfaces
    	String editConfigRequest = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
    			+ "<devices xmlns=\"urn:example:test-action\">" 
    			+ "	<interface>"
    			+ "		<name>interface1</name>" 
    			+ "		<type>interfaceType</type>"
    			+ "	</interface>"
    			+ "	<interface>"
    			+ "		<name>interface2</name>" 
    			+ "		<type>type2</type>"
    			+ "	</interface>"
    			+ "</devices>";

    	editConfig(m_server, m_clientInfo, editConfigRequest, true);

    	//verify GET request
    	String response = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
    			+"<data>"
    			+"<test:test-action-container xmlns:test=\"urn:example:test-action\"/>"
    			+"	<test:devices xmlns:test=\"urn:example:test-action\">"
    			+"		<test:interface>"
    			+"			<test:name>interface1</test:name>"
    			+"			<test:enabled>true</test:enabled>"
    			+"			<test:type>interfaceType</test:type>"
    			+"		</test:interface>"
    			+"		<test:interface>"
    			+"			<test:name>interface2</test:name>"
    			+"			<test:enabled>true</test:enabled>"
    			+"			<test:type>type2</test:type>"
    			+"		</test:interface>"
    			+"	</test:devices>"
    			+"</data>"
    			+"</rpc-reply>"
    			;
    	verifyGet(response);

    	String actionRequest = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
    			+ "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">"
    			+ "	<test:test-action-when-must-validation xmlns:test=\"urn:example:test-action\">"
    			+ "	  <test:must-with-current-validation>" 
    			+ "		<test:interface>interface1</test:interface>"
    			+ "	  </test:must-with-current-validation>" 
    			+ "	</test:test-action-when-must-validation>" 
    			+ "</action>"
    			+ "</rpc>";

    	ActionRequest validActionRequest = DocumentToPojoTransformer
    			.getAction(DocumentUtils.stringToDocument(actionRequest));
    	m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);
    	TestUtil.assertXMLEquals(getActionResponseElement_OK(), m_response.getResponseDocument().getDocumentElement());

    	actionRequest = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
    			+ "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">"
    			+ "	<test:test-action-when-must-validation xmlns:test=\"urn:example:test-action\">"
    			+ "	  <test:must-with-current-validation>" 
    			+ "		<test:interface>interface2</test:interface>"
    			+ "	  </test:must-with-current-validation>" 
    			+ "	</test:test-action-when-must-validation>" 
    			+ "</action>"
    			+ "</rpc>";

    	validActionRequest = DocumentToPojoTransformer
    			.getAction(DocumentUtils.stringToDocument(actionRequest));
    	m_response = new ActionResponse().setMessageId("1");
    	m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);
    	NetconfRpcError error = m_response.getErrors().get(0);
    	assertEquals(NetconfRpcErrorType.Application, error.getErrorType());
    	assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, error.getErrorTag());
    	assertEquals("must-violation",error.getErrorAppTag());
    	assertEquals("Violate must constraints: /test:devices/test:interface[test:name=current()]/test:type = 'interfaceType'", error.getErrorMessage());

    }

    @Test
    public void testActionInput_WhenConstraintsCurrentWithDerivedFromORSelf() throws Exception {
    	getModelNode();
    	initialiseInterceptor();

    	//Create interfaces and profiles
    	String editConfigRequest = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
    			+ "<devices xmlns=\"urn:example:test-action\">" 
    			+ "	<interface>"
    			+ "		<name>interface1</name>" 
    			+ "		<type>type1</type>"
    			+ "		<identity-leaf>identity1</identity-leaf>" 
    			+ "	</interface>"
    			+ "	<interface>"
    			+ "		<name>interface2</name>" 
    			+ "		<type>type2</type>"
    			+ "		<identity-leaf>identity2</identity-leaf>" 
    			+ "	</interface>"
    			+ "</devices>";

    	editConfig(m_server, m_clientInfo, editConfigRequest, true);

    	//verify GET request
    	String response = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
    			+"<data>"
    			+"<test:test-action-container xmlns:test=\"urn:example:test-action\"/>"
    			+"	<test:devices xmlns:test=\"urn:example:test-action\">"
    			+"		<test:interface>"
    			+"			<test:name>interface1</test:name>"
    			+"			<test:enabled>true</test:enabled>"
    			+"			<test:identity-leaf>test:identity1</test:identity-leaf>"
    			+"			<test:type>type1</test:type>"
    			+"		</test:interface>"
    			+"		<test:interface>"
    			+"			<test:name>interface2</test:name>"
    			+"			<test:enabled>true</test:enabled>"
    			+"			<test:identity-leaf>test:identity2</test:identity-leaf>"
    			+"			<test:type>type2</test:type>"
    			+"		</test:interface>"
    			+"	</test:devices>"
    			+"</data>"
    			+"</rpc-reply>"
    			;
    	verifyGet(response);

    	String actionRequest = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
    			+ "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">"
    			+ "	<test:test-action-when-must-validation xmlns:test=\"urn:example:test-action\">"
    			+ "	  <test:when-with-derived-from-or-self-validation>" 
    			+ "		<test:interface>interface2</test:interface>"
    			+ "		<test:type>type2</test:type>"
    			+ "	  </test:when-with-derived-from-or-self-validation>" 
    			+ "	</test:test-action-when-must-validation>" 
    			+ "</action>"
    			+ "</rpc>";

    	ActionRequest validActionRequest = DocumentToPojoTransformer
    			.getAction(DocumentUtils.stringToDocument(actionRequest));
    	m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);
    	TestUtil.assertXMLEquals(getActionResponseElement_OK(), m_response.getResponseDocument().getDocumentElement());

    	actionRequest = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
    			+ "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">"
    			+ "	<test:test-action-when-must-validation xmlns:test=\"urn:example:test-action\">"
    			+ "	  <test:when-with-derived-from-or-self-validation>" 
    			+ "		<test:interface>interface1</test:interface>"
    			+ "		<test:type>type1</test:type>"
    			+ "	  </test:when-with-derived-from-or-self-validation>" 
    			+ "	</test:test-action-when-must-validation>" 
    			+ "</action>"
    			+ "</rpc>";

    	validActionRequest = DocumentToPojoTransformer
    			.getAction(DocumentUtils.stringToDocument(actionRequest));
    	m_response = new ActionResponse().setMessageId("1");
    	m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);
    	NetconfRpcError error = m_response.getErrors().get(0);
    	assertEquals(NetconfRpcErrorType.Application, error.getErrorType());
    	assertEquals(NetconfRpcErrorTag.UNKNOWN_ELEMENT, error.getErrorTag());
    	assertEquals("when-violation",error.getErrorAppTag());
    	assertEquals("Violate when constraints: derived-from-or-self(/test:devices/test:interface[test:name = current()/../interface]/test:identity-leaf,'test:identity2')", error.getErrorMessage());

    }

    @Test
    public void testActionInput_WhenConstraintsCurrentWithCoreOperation() throws Exception {
    	getModelNode();
    	initialiseInterceptor();

    	//Create interfaces
    	String editConfigRequest = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
    			+ "<devices xmlns=\"urn:example:test-action\">" 
    			+ "	<interface>"
    			+ "		<name>interface1</name>" 
    			+ "		<type>interfaceType</type>"
    			+ "	</interface>"
    			+ "	<interface>"
    			+ "		<name>interface2</name>" 
    			+ "		<type>type2</type>"
    			+ "	</interface>"
    			+ "</devices>";

    	editConfig(m_server, m_clientInfo, editConfigRequest, true);

    	//verify GET request
    	String response = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
    			+"<data>"
    			+"<test:test-action-container xmlns:test=\"urn:example:test-action\"/>"
    			+"	<test:devices xmlns:test=\"urn:example:test-action\">"
    			+"		<test:interface>"
    			+"			<test:name>interface1</test:name>"
    			+"			<test:enabled>true</test:enabled>"
    			+"			<test:type>interfaceType</test:type>"
    			+"		</test:interface>"
    			+"		<test:interface>"
    			+"			<test:name>interface2</test:name>"
    			+"			<test:enabled>true</test:enabled>"
    			+"			<test:type>type2</test:type>"
    			+"		</test:interface>"
    			+"	</test:devices>"
    			+"</data>"
    			+"</rpc-reply>"
    			;
    	verifyGet(response);

    	String actionRequest = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
    			+ "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">"
    			+ "	<test:test-action-when-must-validation xmlns:test=\"urn:example:test-action\">"
    			+ "	  <test:when-with-current-validation>" 
    			+ "		<test:interface>interface1</test:interface>"
    			+ "		<test:type>type1</test:type>"
    			+ "	  </test:when-with-current-validation>" 
    			+ "	</test:test-action-when-must-validation>" 
    			+ "</action>"
    			+ "</rpc>";

    	ActionRequest validActionRequest = DocumentToPojoTransformer
    			.getAction(DocumentUtils.stringToDocument(actionRequest));
    	m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);
    	TestUtil.assertXMLEquals(getActionResponseElement_OK(), m_response.getResponseDocument().getDocumentElement());

    	actionRequest = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
    			+ "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">"
    			+ "	<test:test-action-when-must-validation xmlns:test=\"urn:example:test-action\">"
    			+ "	  <test:when-with-current-validation>" 
    			+ "		<test:interface>interface2</test:interface>"
    			+ "		<test:type>type2</test:type>"
    			+ "	  </test:when-with-current-validation>" 
    			+ "	</test:test-action-when-must-validation>" 
    			+ "</action>"
    			+ "</rpc>";

    	validActionRequest = DocumentToPojoTransformer
    			.getAction(DocumentUtils.stringToDocument(actionRequest));
    	m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);

    	NetconfRpcError error = m_response.getErrors().get(0);
    	assertEquals(NetconfRpcErrorType.Application, error.getErrorType());
    	assertEquals(NetconfRpcErrorTag.UNKNOWN_ELEMENT, error.getErrorTag());
    	assertEquals("when-violation",error.getErrorAppTag());
    	assertEquals("Violate when constraints: /test:devices/test:interface[test:name=current()/../interface]/test:type = 'interfaceType'", error.getErrorMessage());

    }

    @Test
    public void testActionInput_MustConstraintsWithCoreFunction() throws Exception {
    	getModelNode();
    	initialiseInterceptor();

    	// verify leafref referred to with-in input elements
    	String actionRequest = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
    			+ "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">"
    			+ "	<test:test-action-when-must-validation xmlns:test=\"urn:example:test-action\">"
    			+ "	  <test:must-validation>" 
    			+ "		<test:countLeaf>one</test:countLeaf>"
    			+ "	  </test:must-validation>" 
    			+ "	</test:test-action-when-must-validation>" 
    			+ "</action>"
    			+ "</rpc>";

    	ActionRequest validActionRequest = DocumentToPojoTransformer
    			.getAction(DocumentUtils.stringToDocument(actionRequest));
    	m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);
    	NetconfRpcError error = m_response.getErrors().get(0);
    	assertEquals(NetconfRpcErrorType.Application, error.getErrorType());
    	assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, error.getErrorTag());
    	assertEquals("must-violation",error.getErrorAppTag());
    	assertEquals("Violate must constraints: count(../list1) > 0", error.getErrorMessage());

    	actionRequest = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
    			+ "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">"
    			+ "	<test:test-action-when-must-validation xmlns:test=\"urn:example:test-action\">"
    			+ "	  <test:must-validation>" 
    			+ "		<test:list1>"
    			+ "		<test:key>name1</test:key>"
    			+ "		</test:list1>"
    			+ "		<test:countLeaf>one</test:countLeaf>"
    			+ "	  </test:must-validation>" 
    			+ "	</test:test-action-when-must-validation>" 
    			+ "</action>"
    			+ "</rpc>";

    	validActionRequest = DocumentToPojoTransformer
    			.getAction(DocumentUtils.stringToDocument(actionRequest));
    	m_response = new ActionResponse().setMessageId("1");
    	m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);
    	TestUtil.assertXMLEquals(getActionResponseElement_OK(), m_response.getResponseDocument().getDocumentElement());

    	actionRequest = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
    			+ "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">"
    			+ "	<test:test-action-when-must-validation xmlns:test=\"urn:example:test-action\">"
    			+ "	  <test:must-validation>" 
    			+"		<test:container1>"
    			+ "			<test:list2>"
    			+ "				<test:key>name1</test:key>"
    			+ "			</test:list2>"
    			+"		</test:container1>"
    			+ "	  </test:must-validation>" 
    			+ "	</test:test-action-when-must-validation>" 
    			+ "</action>"
    			+ "</rpc>";

    	validActionRequest = DocumentToPojoTransformer
    			.getAction(DocumentUtils.stringToDocument(actionRequest));
    	m_response = new ActionResponse().setMessageId("1");
    	m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);
    	TestUtil.assertXMLEquals(getActionResponseElement_OK(), m_response.getResponseDocument().getDocumentElement());

    	actionRequest = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
    			+ "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">"
    			+ "	<test:test-action-when-must-validation xmlns:test=\"urn:example:test-action\">"
    			+ "	  <test:must-validation>" 
    			+"		<test:container1>"
    			+"		</test:container1>"
    			+ "	  </test:must-validation>" 
    			+ "	</test:test-action-when-must-validation>" 
    			+ "</action>"
    			+ "</rpc>";

    	validActionRequest = DocumentToPojoTransformer
    			.getAction(DocumentUtils.stringToDocument(actionRequest));
    	m_response = new ActionResponse().setMessageId("1");
    	m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);

    	error = m_response.getErrors().get(0);
    	assertEquals(NetconfRpcErrorType.Application, error.getErrorType());
    	assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, error.getErrorTag());
    	assertEquals("must-violation",error.getErrorAppTag());
    	assertEquals("Violate must constraints: count(current()/list2) >= 1", error.getErrorMessage());

    	/**
    	 * must "../../countLeaf = 'hello' and count(../list2) > 0 and count(../../list1[key = current()]) > 0";
    	 */

    	actionRequest = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
    			+ "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">"
    			+ "	<test:test-action-when-must-validation xmlns:test=\"urn:example:test-action\">"
    			+ "	  <test:must-validation>"
    			+ "		<test:list1>"
    			+ "			<test:key>key1</test:key>"
    			+ "		</test:list1>"
    			+ "		<test:countLeaf>hello</test:countLeaf>"
    			+"		<test:container1>"
    			+ "			<test:list2>"
    			+ "				<test:key>name1</test:key>"
    			+ "			</test:list2>"
    			+"		<test:someLeaf>key1</test:someLeaf>"
    			+"		</test:container1>"
    			+ "	  </test:must-validation>" 
    			+ "	</test:test-action-when-must-validation>" 
    			+ "</action>"
    			+ "</rpc>";

    	validActionRequest = DocumentToPojoTransformer
    			.getAction(DocumentUtils.stringToDocument(actionRequest));
    	m_response = new ActionResponse().setMessageId("1");
    	m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);
    	TestUtil.assertXMLEquals(getActionResponseElement_OK(), m_response.getResponseDocument().getDocumentElement());

    	actionRequest = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
    			+ "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">"
    			+ "	<test:test-action-when-must-validation xmlns:test=\"urn:example:test-action\">"
    			+ "	  <test:must-validation>"
    			+ "		<test:list1>"
    			+ "			<test:key>key1</test:key>"
    			+ "		</test:list1>"
    			+ "		<test:countLeaf>hello</test:countLeaf>"
    			+"		<test:container1>"
    			+ "			<test:list2>"
    			+ "				<test:key>key2</test:key>"
    			+ "			</test:list2>"
    			+"		<test:someLeaf>key</test:someLeaf>"
    			+"		</test:container1>"
    			+ "	  </test:must-validation>" 
    			+ "	</test:test-action-when-must-validation>" 
    			+ "</action>"
    			+ "</rpc>";

    	validActionRequest = DocumentToPojoTransformer
    			.getAction(DocumentUtils.stringToDocument(actionRequest));
    	m_response = new ActionResponse().setMessageId("1");
    	m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);

    	error = m_response.getErrors().get(0);
    	assertEquals(NetconfRpcErrorType.Application, error.getErrorType());
    	assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, error.getErrorTag());
    	assertEquals("must-violation",error.getErrorAppTag());
    	assertEquals("Violate must constraints: ../../countLeaf = 'hello' and count(../list2) > 0 and count(../../list1[key = current()]) > 0", error.getErrorMessage());

    	actionRequest = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
    			+ "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">"
    			+ "	<test:test-action-when-must-validation xmlns:test=\"urn:example:test-action\">"
    			+ "	  <test:must-validation>"
    			+ "		<test:countLeaf>hello</test:countLeaf>"
    			+ "		<test:list1>"
    			+ "			<test:key>key</test:key>"
    			+ "		</test:list1>"
    			+"		<test:container1>"
    			+ "			<test:list2>"
    			+ "				<test:key>key</test:key>"
    			+ "			</test:list2>"
    			+"		<test:someLeaf1>key</test:someLeaf1>"
    			+"		<test:someLeaf>key</test:someLeaf>"
    			+"		</test:container1>"
    			+ "	  </test:must-validation>" 
    			+ "	</test:test-action-when-must-validation>" 
    			+ "</action>"
    			+ "</rpc>";

    	validActionRequest = DocumentToPojoTransformer
    			.getAction(DocumentUtils.stringToDocument(actionRequest));
    	m_response = new ActionResponse().setMessageId("1");
    	m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);
    	TestUtil.assertXMLEquals(getActionResponseElement_OK(), m_response.getResponseDocument().getDocumentElement());

    	actionRequest = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
    			+ "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">"
    			+ "	<test:test-action-when-must-validation xmlns:test=\"urn:example:test-action\">"
    			+ "	  <test:must-validation>"
    			+ "		<test:countLeaf>hello</test:countLeaf>"
    			+ "		<test:list1>"
    			+ "			<test:key>key3</test:key>"
    			+ "		</test:list1>"
    			+"		<test:container1>"
    			+ "			<test:list2>"
    			+ "				<test:key>key</test:key>"
    			+ "			</test:list2>"
    			+"		<test:someLeaf1>key</test:someLeaf1>"
    			+"		<test:someLeaf>key</test:someLeaf>"
    			+"		</test:container1>"
    			+ "	  </test:must-validation>" 
    			+ "	</test:test-action-when-must-validation>" 
    			+ "</action>"
    			+ "</rpc>";

    	validActionRequest = DocumentToPojoTransformer
    			.getAction(DocumentUtils.stringToDocument(actionRequest));
    	m_response = new ActionResponse().setMessageId("1");
    	m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);

    	error = m_response.getErrors().get(0);
    	assertEquals(NetconfRpcErrorType.Application, error.getErrorType());
    	assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, error.getErrorTag());
    	assertEquals("must-violation",error.getErrorAppTag());
    	assertEquals("Violate must constraints: ../../countLeaf = 'hello' and count(../list2) > 0 and count(../../list1[key = current()]) > 0", error.getErrorMessage());

    	actionRequest = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
    			+ "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">"
    			+ "	<test:test-action-when-must-validation xmlns:test=\"urn:example:test-action\">"
    			+ "	  <test:must-validation>"
    			+"		<test:container1>"
    			+ "			<test:list2>"
    			+ "				<test:key>key1</test:key>"
    			+ "			</test:list2>"
    			+"		<test:someLeaf1>key</test:someLeaf1>"
    			+"		<test:someLeaf>key</test:someLeaf>"
    			+"		</test:container1>"
    			+ "	  </test:must-validation>" 
    			+ "	</test:test-action-when-must-validation>" 
    			+ "</action>"
    			+ "</rpc>";

    	validActionRequest = DocumentToPojoTransformer
    			.getAction(DocumentUtils.stringToDocument(actionRequest));
    	m_response = new ActionResponse().setMessageId("1");
    	m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);

    	error = m_response.getErrors().get(0);
    	assertEquals(NetconfRpcErrorType.Application, error.getErrorType());
    	assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, error.getErrorTag());
    	assertEquals("must-violation",error.getErrorAppTag());
    	assertEquals("Violate must constraints: current() = ../someLeaf and . = ../../container1/list2[key = current()/../someLeaf]", error.getErrorMessage());

    	actionRequest = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
    			+ "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">"
    			+ "	<test:test-action-when-must-validation xmlns:test=\"urn:example:test-action\">"
    			+ "	  <test:must-validation>"
    			+"		<test:container1>"
    			+ "			<test:list2>"
    			+ "				<test:key>key</test:key>"
    			+ "			</test:list2>"
    			+"		<test:someLeaf1>key</test:someLeaf1>"
    			+"		<test:someLeaf>key1</test:someLeaf>"
    			+"		</test:container1>"
    			+ "	  </test:must-validation>" 
    			+ "	</test:test-action-when-must-validation>" 
    			+ "</action>"
    			+ "</rpc>";

    	validActionRequest = DocumentToPojoTransformer
    			.getAction(DocumentUtils.stringToDocument(actionRequest));
    	m_response = new ActionResponse().setMessageId("1");
    	m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);

    	error = m_response.getErrors().get(0);
    	assertEquals(NetconfRpcErrorType.Application, error.getErrorType());
    	assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, error.getErrorTag());
    	assertEquals("must-violation",error.getErrorAppTag());
    	assertEquals("Violate must constraints: current() = ../someLeaf and . = ../../container1/list2[key = current()/../someLeaf]", error.getErrorMessage());

    }

	@Test
	public void testActionInput_TargetLeafOutSideOfAction_NegativeCase() throws Exception {
		getModelNode();
		initialiseInterceptor();

		String actionRequest = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
				+ "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">"
				+ "	<test:test-action-when-must-validation xmlns:test=\"urn:example:test-action\">"
				+ "	  <test:default-when-validation>"
				+ "		<test:when-validation>test</test:when-validation>"
				+ "		<test:targetOutSideAction>hello</test:targetOutSideAction>"
				+ "	  </test:default-when-validation>"
				+ "	</test:test-action-when-must-validation>"
				+ "</action>"
				+ "</rpc>";

		ActionRequest validActionRequest = DocumentToPojoTransformer
				.getAction(DocumentUtils.stringToDocument(actionRequest));
		m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);
		NetconfRpcError error = m_response.getErrors().get(0);
		assertEquals(NetconfRpcErrorType.Application, error.getErrorType());
		assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, error.getErrorTag());
		assertEquals("must-violation",error.getErrorAppTag());
		assertEquals("Violate must constraints: ../../../mustTargetLeafOutSideAction = 'hello'", error.getErrorMessage());
	}

	@Test
	public void testActionInput_TargetLeafOutSideOfAction_PositiveCase() throws Exception {
		getModelNode();
		initialiseInterceptor();

		String actionRequest = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
				+ "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">"
				+ "	<test:test-action-when-must-validation xmlns:test=\"urn:example:test-action\">"
				+ "   <test:mustTargetLeafOutSideAction>hello</test:mustTargetLeafOutSideAction>"
				+ "	  <test:default-when-validation>"
				+ "		<test:when-validation>test</test:when-validation>"
				+ "		<test:targetOutSideAction>hello</test:targetOutSideAction>"
				+ "	  </test:default-when-validation>"
				+ "	</test:test-action-when-must-validation>"
				+ "</action>"
				+ "</rpc>";

		ActionRequest validActionRequest = DocumentToPojoTransformer
				.getAction(DocumentUtils.stringToDocument(actionRequest));
		m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);
		TestUtil.assertXMLEquals(getActionResponseElement_OK(), m_response.getResponseDocument().getDocumentElement());
	}

    @Test
    public void testActionInput_WhenWithDefaultLeafValidation() throws Exception {
    	getModelNode();
    	initialiseInterceptor();

    	String actionRequest = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
    			+ "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">"
    			+ "	<test:test-action-when-must-validation xmlns:test=\"urn:example:test-action\">"
    			+ "	  <test:default-when-validation>" 
    			+ "		<test:when-validation>test</test:when-validation>"
    			+ "	  </test:default-when-validation>" 
    			+ "	</test:test-action-when-must-validation>" 
    			+ "</action>"
    			+ "</rpc>";

    	ActionRequest validActionRequest = DocumentToPojoTransformer
    			.getAction(DocumentUtils.stringToDocument(actionRequest));
    	m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);
    	TestUtil.assertXMLEquals(getActionResponseElement_OK(), m_response.getResponseDocument().getDocumentElement());

    	actionRequest = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
    			+ "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">"
    			+ "	<test:test-action-when-must-validation xmlns:test=\"urn:example:test-action\">"
    			+ "	  <test:default-when-validation>" 
    			+ "		<test:when-validation>test1</test:when-validation>"
    			+ "	  </test:default-when-validation>" 
    			+ "	</test:test-action-when-must-validation>" 
    			+ "</action>"
    			+ "</rpc>";

    	validActionRequest = DocumentToPojoTransformer
    			.getAction(DocumentUtils.stringToDocument(actionRequest));
    	m_response = new ActionResponse().setMessageId("1");
    	m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);

    	NetconfRpcError error = m_response.getErrors().get(0);
    	assertEquals(NetconfRpcErrorType.Application, error.getErrorType());
    	assertEquals(NetconfRpcErrorTag.UNKNOWN_ELEMENT, error.getErrorTag());
    	assertEquals("when-violation",error.getErrorAppTag());
    	assertEquals("Violate when constraints: ../default-leaf = current()", error.getErrorMessage());

    	actionRequest = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
    			+ "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">"
    			+ "	<test:test-action-when-must-validation xmlns:test=\"urn:example:test-action\">"
    			+ "	  <test:default-when-validation>" 
    			+"		<test:default-leaf>test1</test:default-leaf>"
    			+ "		<test:when-validation>test1</test:when-validation>"
    			+ "	  </test:default-when-validation>" 
    			+ "	</test:test-action-when-must-validation>" 
    			+ "</action>"
    			+ "</rpc>";

    	validActionRequest = DocumentToPojoTransformer
    			.getAction(DocumentUtils.stringToDocument(actionRequest));
    	m_response = new ActionResponse().setMessageId("1");
    	m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);
    	TestUtil.assertXMLEquals(getActionResponseElement_OK(), m_response.getResponseDocument().getDocumentElement());
    }

    @Test
    public void testActionWithoutInputElements() throws Exception {
    	getModelNode();
    	initialiseInterceptor();

    	//action without any input elements
    	String actionRequest = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
    			+ "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">"
    			+ "	<test:test-action-when-must-validation xmlns:test=\"urn:example:test-action\">"
    			+ "	  <test:default-when-validation>" 
    			+ "	  </test:default-when-validation>" 
    			+ "	</test:test-action-when-must-validation>" 
    			+ "</action>"
    			+ "</rpc>";

    	ActionRequest validActionRequest = DocumentToPojoTransformer
    			.getAction(DocumentUtils.stringToDocument(actionRequest));
    	m_response = new ActionResponse().setMessageId("1");
    	m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);
    	TestUtil.assertXMLEquals(getActionResponseElement_OK(), m_response.getResponseDocument().getDocumentElement());
    }

    @Test
    public void testActionWithoutAction() throws Exception {
    	getModelNode();
    	initialiseInterceptor();

    	// with out action
    	String actionRequest = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
    			+ "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">"
    			+ "	<test:test-action-when-must-validation xmlns:test=\"urn:example:test-action\">"
    			+ "	</test:test-action-when-must-validation>" 
    			+ "</action>"
    			+ "</rpc>";

    	ActionRequest validActionRequest = DocumentToPojoTransformer
    			.getAction(DocumentUtils.stringToDocument(actionRequest));
    	m_response = new ActionResponse().setMessageId("1");
    	m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);

    	String response = "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
    			+ "<rpc-error>" 
    			+ "	<error-message>No matched action found on the models</error-message>"
    			+ "	<error-severity>error</error-severity>" 
    			+ " <error-tag>bad-element</error-tag>"
    			+ "	<error-type>protocol</error-type>" 
    			+ "</rpc-error>" 
    			+ "</rpc-reply>";
    	Document responseDocument = DocumentUtils.stringToDocument(response);
    	TestUtil.assertXMLEquals(responseDocument.getDocumentElement(), m_response.getResponseDocument().getDocumentElement());
    }

    @Test
    public void testActionInput_AugmentWithLeafrefValidation() throws Exception {
    	getModelNode();
    	initialiseInterceptor();

    	//Create interfaces
    	String editConfigRequest = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
    			+ "<devices xmlns=\"urn:example:test-action\">" 
    			+ "	<interface>"
    			+ "		<name>interface1</name>" 
    			+ "		<type>interfaceType</type>"
    			+ "	</interface>"
    			+ "</devices>";

    	editConfig(m_server, m_clientInfo, editConfigRequest, true);

    	//verify GET request
    	String response = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
    			+"<data>"
    			+"<test:test-action-container xmlns:test=\"urn:example:test-action\"/>"
    			+"	<test:devices xmlns:test=\"urn:example:test-action\">"
    			+"		<test:interface>"
    			+"			<test:name>interface1</test:name>"
    			+"			<test:enabled>true</test:enabled>"
    			+"			<test:type>interfaceType</test:type>"
    			+"		</test:interface>"
    			+"	</test:devices>"
    			+"</data>"
    			+"</rpc-reply>"
    			;
    	verifyGet(response);

    	// verify valid leafref value for leaf-list
    	String actionRequest = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
    			+ "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">"
    			+ "	<test:test-action-leafref-container xmlns:test=\"urn:example:test-action\">"
    			+ "		<test:test-action-augment>"
    			+ "			<test:test-interface>interface1</test:test-interface>"
    			+ "		</test:test-action-augment>"
    			+ "	</test:test-action-leafref-container>" 
    			+ "</action>"
    			+ "</rpc>";

    	ActionRequest validActionRequest = DocumentToPojoTransformer
    			.getAction(DocumentUtils.stringToDocument(actionRequest));
    	m_response = new ActionResponse().setMessageId("1");
    	m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);
    	TestUtil.assertXMLEquals(getActionResponseElement_OK(), m_response.getResponseDocument().getDocumentElement());

    	// verify invalid leafref value for leaf-list
    	actionRequest = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
    			+ "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">"
    			+ "	<test:test-action-leafref-container xmlns:test=\"urn:example:test-action\">"
    			+ "		<test:test-action-augment>"
    			+ "			<test:test-interface>interface4</test:test-interface>"
    			+ "		</test:test-action-augment>"
    			+ "	</test:test-action-leafref-container>" 
    			+ "</action>"
    			+ "</rpc>";

    	validActionRequest = DocumentToPojoTransformer
    			.getAction(DocumentUtils.stringToDocument(actionRequest));
    	m_response = new ActionResponse().setMessageId("1");
    	m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);
    	NetconfRpcError error = m_response.getErrors().get(0);
    	assertEquals(NetconfRpcErrorType.Application, error.getErrorType());
    	assertEquals(NetconfRpcErrorTag.DATA_MISSING, error.getErrorTag());
    	assertEquals("instance-required",error.getErrorAppTag());
    	assertEquals("/test:test-action-augment/test:test-interface", error.getErrorPath());
    	assertEquals("Missing required element interface4", error.getErrorMessage());
    }

    @Test
    public void testActionInput_GoupingUsesUnderAugment() throws Exception {
    	getModelNode();
    	initialiseInterceptor();

    	//Create interfaces
    	String editConfigRequest = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
    			+ "<devices xmlns=\"urn:example:test-action\">" 
    			+ "	<interface>"
    			+ "		<name>interface1</name>" 
    			+ "		<type>interfaceType</type>"
    			+ "	</interface>"
    			+ "</devices>";

    	editConfig(m_server, m_clientInfo, editConfigRequest, true);

    	//verify GET request
    	String response = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
    			+"<data>"
    			+"<test:test-action-container xmlns:test=\"urn:example:test-action\"/>"
    			+"	<test:devices xmlns:test=\"urn:example:test-action\">"
    			+"		<test:interface>"
    			+"			<test:name>interface1</test:name>"
    			+"			<test:enabled>true</test:enabled>"
    			+"			<test:type>interfaceType</test:type>"
    			+"		</test:interface>"
    			+"	</test:devices>"
    			+"</data>"
    			+"</rpc-reply>"
    			;
    	verifyGet(response);

    	// verify valid leafref value for leaf-list
    	String actionRequest = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
    			+ "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">"
    			+ "	<test:test-action-leafref-container xmlns:test=\"urn:example:test-action\">"
    			+ "	  <test:validation-container>" 
    			+ "		<test:test-action>"
    			+ "			<test:test-interface>interface1</test:test-interface>"
    			+ "		</test:test-action>"
    			+ "	  </test:validation-container>" 
    			+ "	</test:test-action-leafref-container>" 
    			+ "</action>"
    			+ "</rpc>";

    	ActionRequest validActionRequest = DocumentToPojoTransformer
    			.getAction(DocumentUtils.stringToDocument(actionRequest));
    	m_response = new ActionResponse().setMessageId("1");
    	m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);
    	TestUtil.assertXMLEquals(getActionResponseElement_OK(), m_response.getResponseDocument().getDocumentElement());

    	// verify invalid leafref value for leaf-list
    	actionRequest = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
    			+ "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">"
    			+ "	<test:test-action-leafref-container xmlns:test=\"urn:example:test-action\">"
    			+ "	  <test:validation-container>" 
    			+ "		<test:test-action>"
    			+ "			<test:test-interface>interface4</test:test-interface>"
    			+ "		</test:test-action>"
    			+ "	  </test:validation-container>" 
    			+ "	</test:test-action-leafref-container>" 
    			+ "</action>"
    			+ "</rpc>";

    	validActionRequest = DocumentToPojoTransformer
    			.getAction(DocumentUtils.stringToDocument(actionRequest));
    	m_response = new ActionResponse().setMessageId("1");
    	m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);
    	NetconfRpcError error = m_response.getErrors().get(0);
    	assertEquals(NetconfRpcErrorType.Application, error.getErrorType());
    	assertEquals(NetconfRpcErrorTag.DATA_MISSING, error.getErrorTag());
    	assertEquals("instance-required",error.getErrorAppTag());
    	assertEquals("/test:validation-container/test:test-action/test:test-interface", error.getErrorPath());
    	assertEquals("Missing required element interface4", error.getErrorMessage());
    }

    //   @Test
    public void testAction_AugmentWhenGoupingUsesLeafrefValidation() throws Exception {
    	getModelNode();
    	initialiseInterceptor();

    	//Create interfaces
    	String editConfigRequest = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
    			+ "<devices xmlns=\"urn:example:test-action\">" 
    			+ "	<interface>"
    			+ "		<name>interface1</name>" 
    			+ "		<type>interfaceType</type>"
    			+ "	</interface>"
    			+ "	<profile>"
    			+ "		<name>profile1</name>" 
    			+ "	</profile>"
    			+ "</devices>";

    	editConfig(m_server, m_clientInfo, editConfigRequest, true);

    	//verify GET request
    	String response = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
    			+"<data>"
    			+"<test:test-action-container xmlns:test=\"urn:example:test-action\"/>"
    			+"	<test:devices xmlns:test=\"urn:example:test-action\">"
    			+"		<test:interface>"
    			+"			<test:name>interface1</test:name>"
    			+"			<test:enabled>true</test:enabled>"
    			+"			<test:type>interfaceType</test:type>"
    			+"		</test:interface>"
    			+"		<test:profile>"
    			+"			<test:name>profile1</test:name>"
    			+"		</test:profile>"
    			+"	</test:devices>"
    			+"</data>"
    			+"</rpc-reply>"
    			;
    	verifyGet(response);

    	// verify valid leafref value for leaf-list
    	String actionRequest = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
    			+ "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">"
    			+ "	<test:test-action-leafref-container xmlns:test=\"urn:example:test-action\">"
    			+ "	  <test:when-augment-container>" 
    			+ "		<test:test-action>"
    			+ "			<test:test-interface>interface1</test:test-interface>"
    			+ "		</test:test-action>"
    			+ "	  </test:when-augment-container>" 
    			+ "	</test:test-action-leafref-container>" 
    			+ "</action>"
    			+ "</rpc>";

    	ActionRequest validActionRequest = DocumentToPojoTransformer
    			.getAction(DocumentUtils.stringToDocument(actionRequest));
    	m_response = new ActionResponse().setMessageId("1");
    	m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);
    	// TODO : This should be failed, Augment-when is not supported yet, this need to fix it.
    	TestUtil.assertXMLEquals(getActionResponseElement_OK(), m_response.getResponseDocument().getDocumentElement());
    }

	@Test
	public void testActionInput_MustConstaintsWithCurrent() throws Exception {
		getModelNode();
		initialiseInterceptor();

		String editConfigRequest = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "		<cpe-software-management xmlns=\"urn:example:test-action\">"
				+ "		  <software-preferences>"
				+ " 		<preference>"
				+ " 		  <cpe-type>key</cpe-type>"
				+ " 		  <preferred-software-version>1</preferred-software-version>"
				+ " 		</preference>"
				+ "		  </software-preferences>"
				+ "		</cpe-software-management>";

		editConfig(m_server, m_clientInfo, editConfigRequest, true);

		//verify GET request
		String response = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
				+"<data>"
				+"	<test:cpe-software-management xmlns:test=\"urn:example:test-action\">"
				+"		<test:software-preferences>"
				+"			<test:preference>"
				+"			<test:cpe-type>key</test:cpe-type>"
				+"			<test:preferred-software-version>1</test:preferred-software-version>"
				+"			</test:preference>"
				+"		</test:software-preferences>"
				+"	</test:cpe-software-management>"
				+"  <test:test-action-container xmlns:test=\"urn:example:test-action\"/>"
				+"</data>"
				+"</rpc-reply>"
				;
		verifyGet(response);

		String actionRequest = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
				+ "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">"
				+ "	<cpe-software-management xmlns=\"urn:example:test-action\">"
				+ "	  <software-actions>"
				+ "		   <delete>"
				+ " 	     <software-version>0</software-version>"
				+ "		   </delete>"
				+ "	  </software-actions>"
				+ "	</cpe-software-management>"
				+ "</action>"
				+ "</rpc>";

		ActionRequest validActionRequest = DocumentToPojoTransformer
				.getAction(DocumentUtils.stringToDocument(actionRequest));
		m_response = new ActionResponse().setMessageId("1");
		m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);
		NetconfRpcError error = m_response.getErrors().get(0);
		assertEquals(NetconfRpcErrorType.Application, error.getErrorType());
		assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, error.getErrorTag());
		assertEquals("must-violation", error.getErrorAppTag());
		assertEquals("Violate must constraints: count(/cpe-software-management/software-preferences/preference[preferred-software-version" +
				" = current()]) = 1", error.getErrorMessage());

		actionRequest = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
				+ "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">"
				+ "	<cpe-software-management xmlns=\"urn:example:test-action\">"
				+ "	  <software-actions>"
				+ "		   <delete>"
				+ " 	     <software-version>1</software-version>"
				+ "		   </delete>"
				+ "	  </software-actions>"
				+ "	</cpe-software-management>"
				+ "</action>"
				+ "</rpc>";

		validActionRequest = DocumentToPojoTransformer
				.getAction(DocumentUtils.stringToDocument(actionRequest));
		m_response = new ActionResponse().setMessageId("1");
		m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);
		assertEquals(0, m_response.getErrors().size());

	}

	@Test
	public void testActionInput_MustConstaintsWithCurrent1() throws Exception {
		getModelNode();
		initialiseInterceptor();

		String actionRequest = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
				+ "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">"
				+ "	<cpe-software-management xmlns=\"urn:example:test-action\">"
				+ "	  <software-actions>"
				+ "		<remove>"
				+ "			<software-version>1</software-version>"
				+ "			<software>"
				+ "				<name>name1</name>"
				+ "				<version>1</version>"
				+ "			</software>"
				+ "		</remove>"
				+ "	  </software-actions>"
				+ "	</cpe-software-management>"
				+ "</action>"
				+ "</rpc>";

		ActionRequest validActionRequest = DocumentToPojoTransformer
				.getAction(DocumentUtils.stringToDocument(actionRequest));
		m_response = new ActionResponse().setMessageId("1");
		m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);
		assertEquals(0, m_response.getErrors().size());

		actionRequest = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
				+ "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">"
				+ "	<cpe-software-management xmlns=\"urn:example:test-action\">"
				+ "	  <software-actions>"
				+ "		<remove>"
				+ "			<software-version>1</software-version>"
				+ "			<software>"
				+ "				<name>name1</name>"
				+ "				<version>2</version>"
				+ "			</software>"
				+ "		</remove>"
				+ "	  </software-actions>"
				+ "	</cpe-software-management>"
				+ "</action>"
				+ "</rpc>";

		validActionRequest = DocumentToPojoTransformer
				.getAction(DocumentUtils.stringToDocument(actionRequest));
		m_response = new ActionResponse().setMessageId("1");
		m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);
		NetconfRpcError error = m_response.getErrors().get(0);
		assertEquals(NetconfRpcErrorType.Application, error.getErrorType());
		assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, error.getErrorTag());
		assertEquals("must-violation", error.getErrorAppTag());
		assertEquals("Violate must constraints: count(current()/../software[version = current()]) = 1", error.getErrorMessage());
	}

    @Test
    public void testActionInput_GroupingWithChoiceAndMustConstaints() throws Exception {
    	getModelNode();
    	initialiseInterceptor();

    	//Create rollback file
    	String editConfigRequest = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
    			+ "<rollback-files xmlns=\"urn:example:test-action\">" 
    			+ "	<file>"
    			+ "		<id>10</id>"
    			+ "		<name>songs</name>" 
    			+ "	</file>"
    			+ "</rollback-files>";

    	editConfig(m_server, m_clientInfo, editConfigRequest, true);

    	//verify GET request
    	String response = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
    			+"<data>"
    			+"	<test:rollback-files xmlns:test=\"urn:example:test-action\">"
    			+"		<test:file>"
    			+"			<test:id>10</test:id>"
    			+"			<test:name>songs</test:name>"
    			+"		</test:file>"
    			+"	</test:rollback-files>"
    			+"<test:test-action-container xmlns:test=\"urn:example:test-action\"/>"
    			+"</data>"
    			+"</rpc-reply>"
    			;
    	verifyGet(response);

    	String actionRequest = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
    			+ "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">"
    			+ "	<test:rollback-files xmlns:test=\"urn:example:test-action\">"
    			+ "		<test:apply-rollback-file>"
    			+ "			<test:id>10</test:id>"
    			+ "			<test:city>chennai</test:city>"
    			+ "		</test:apply-rollback-file>"
    			+ "	</test:rollback-files>" 
    			+ "</action>"
    			+ "</rpc>";

    	ActionRequest validActionRequest = DocumentToPojoTransformer
    			.getAction(DocumentUtils.stringToDocument(actionRequest));
    	m_response = new ActionResponse().setMessageId("1");
    	m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);
    	NetconfRpcError error = m_response.getErrors().get(0);
    	assertEquals(NetconfRpcErrorType.Application, error.getErrorType());
    	assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, error.getErrorTag());
    	assertEquals("must-violation",error.getErrorAppTag());
    	assertEquals("Violate must constraints: count(/rollback-files/file) > 1", error.getErrorMessage());

    	// create another file
    	editConfigRequest = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
    			+ "<rollback-files xmlns=\"urn:example:test-action\">" 
    			+ "	<file>"
    			+ "		<id>11</id>"
    			+ "		<name>films</name>" 
    			+ "	</file>"
    			+ "</rollback-files>";

    	editConfig(m_server, m_clientInfo, editConfigRequest, true);

    	//verify GET request
    	response = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
    			+"<data>"
    			+"	<test:rollback-files xmlns:test=\"urn:example:test-action\">"
    			+"		<test:file>"
    			+"			<test:id>10</test:id>"
    			+"			<test:name>songs</test:name>"
    			+"		</test:file>"
    			+"		<test:file>"
    			+"			<test:id>11</test:id>"
    			+"			<test:name>films</test:name>"
    			+"		</test:file>"
    			+"	</test:rollback-files>"
    			+"<test:test-action-container xmlns:test=\"urn:example:test-action\"/>"
    			+"</data>"
    			+"</rpc-reply>"
    			;
    	verifyGet(response);

    	// verify valid leafref value
    	actionRequest = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
    			+ "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">"
    			+ "	<test:rollback-files xmlns:test=\"urn:example:test-action\">"
    			+ "		<test:apply-rollback-file>"
    			+ "			<test:id>11</test:id>"
    			+ "			<test:city>chennai</test:city>"
    			+ "			<test:city>mumbai</test:city>"
    			+ "		</test:apply-rollback-file>"
    			+ "	</test:rollback-files>" 
    			+ "</action>"
    			+ "</rpc>";

    	validActionRequest = DocumentToPojoTransformer
    			.getAction(DocumentUtils.stringToDocument(actionRequest));
    	m_response = new ActionResponse().setMessageId("1");
    	m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);
    	TestUtil.assertXMLEquals(getActionResponseElement_OK(), m_response.getResponseDocument().getDocumentElement());

    	// verify invalid leafref value
    	actionRequest = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
    			+ "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">"
    			+ "	<test:rollback-files xmlns:test=\"urn:example:test-action\">"
    			+ "		<test:apply-rollback-file>"
    			+ "			<test:id>18</test:id>"
    			+ "			<test:city>chennai</test:city>"
    			+ "		</test:apply-rollback-file>"
    			+ "	</test:rollback-files>" 
    			+ "</action>"
    			+ "</rpc>";

    	validActionRequest = DocumentToPojoTransformer
    			.getAction(DocumentUtils.stringToDocument(actionRequest));
    	m_response = new ActionResponse().setMessageId("1");
    	m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);
    	error = m_response.getErrors().get(0);
    	assertEquals(NetconfRpcErrorType.Application, error.getErrorType());
    	assertEquals(NetconfRpcErrorTag.DATA_MISSING, error.getErrorTag());
    	assertEquals("instance-required",error.getErrorAppTag());
    	assertEquals("/test:apply-rollback-file/test:id", error.getErrorPath());
    	assertEquals("Missing required element 18", error.getErrorMessage());
    }
}
