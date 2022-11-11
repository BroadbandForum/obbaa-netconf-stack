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

import static org.broadband_forum.obbaa.netconf.api.messages.NetconfFilter.SUBTREE_TYPE;
import static org.broadband_forum.obbaa.netconf.api.util.DocumentUtils.stringToDocument;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NetConfServerImpl.GET_FINAL_FILTER;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.transformToElement;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.ActionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.ActionResponse;
import org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigElement;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigErrorOptions;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigTestOptions;
import org.broadband_forum.obbaa.netconf.api.messages.GetRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfFilter;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorSeverity;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorType;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcResponse;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaMountRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaMountRegistryProvider;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.SubtreeFilterUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.OperationAttributeUtil;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.broadband_forum.obbaa.netconf.server.rpc.MultiRpcRequestHandler;
import org.broadband_forum.obbaa.netconf.server.rpc.RequestType;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcPayloadConstraintParser;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@RunWith(RequestScopeJunitRunner.class)
public class NetconfServerImplTest {

    private NetConfServerImpl m_server;
    private NetconfClientInfo m_clientInfo;
    private GetRequest m_getReq;
    @Mock private NetConfResponse m_response;
    @Mock private SchemaRegistry m_schemaRegistry;
    @Mock private DataStore m_ds;
    @Mock private java.util.List<org.w3c.dom.Element> m_values;
    @Mock private SubtreeFilterUtil m_util;
    @Mock private NetconfFilter m_filter;
    @Mock private RpcPayloadConstraintParser m_rpcConstraintParser;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        m_getReq = new GetRequest();
        m_getReq.setFilter(m_filter);
        m_getReq.setMessageId("1");
        m_clientInfo = new NetconfClientInfo("ut", 1);
        when(m_ds .get(anyObject(), anyObject(), anyObject(), anyObject())).thenReturn(m_values);
        m_server = new NetConfServerImpl(m_schemaRegistry, m_rpcConstraintParser, m_util);
        m_server.setRunningDataStore(m_ds);
    }

    @Test
    public void tesGetUseSystemVar(){
        System.setProperty(GET_FINAL_FILTER, "false");
        when(m_filter.getType()).thenReturn(SUBTREE_TYPE);
        m_server.onGet(m_clientInfo, m_getReq, m_response);
        verifyZeroInteractions(m_util);
        System.setProperty(GET_FINAL_FILTER, "true");
        m_server.onGet(m_clientInfo, m_getReq, m_response);
        verify(m_util).filter(anyObject(), eq(null));
        System.clearProperty(GET_FINAL_FILTER);
        verifyNoMoreInteractions(m_util);
    }

    @After
    public void tearDown(){
        System.clearProperty(GET_FINAL_FILTER);
    }
    
    @Test
    public void testSchemaMountedAction() throws Exception {
        ActionRequest request = new ActionRequest();
        QName global = QName.create("www.global.com", "global");
        QName qname = QName.create("www.test.com", "testRoot");
        QName childQname = QName.create("www.test.com", "testChild");
        QName actionQName = QName.create("www.test.com", "testAction");
        SchemaPath actionPath = SchemaPath.create(true, qname, childQname, actionQName);
        SchemaPath actionPath2 = SchemaPath.create(true, qname, actionQName);
        DataSchemaNode rootDSN = mock(DataSchemaNode.class, withSettings().extraInterfaces(DataNodeContainer.class));
        ContainerSchemaNode containerChild = mock(ContainerSchemaNode.class);
        Collection<DataSchemaNode> childNodes = Arrays.asList(containerChild);
        SchemaMountRegistry mountRegistry = mock(SchemaMountRegistry.class);
        SchemaMountRegistryProvider provider = mock(SchemaMountRegistryProvider.class);
        SchemaRegistry mountedRegistry = mock(SchemaRegistry.class);

        when(containerChild.getQName()).thenReturn(qname);
        when(((DataNodeContainer)rootDSN).getChildNodes()).thenReturn(childNodes );
        when(rootDSN.getQName()).thenReturn(global);
        when(m_schemaRegistry.getRootDataSchemaNodes()).thenReturn(Arrays.asList(rootDSN ) );
        when(mountRegistry.getProvider(any(SchemaPath.class))).thenReturn(provider );
        when(provider.getSchemaRegistry(any(ModelNodeId.class))).thenReturn(mountedRegistry );
        when(provider.getSchemaRegistry(any(EditContainmentNode.class))).thenReturn(mountedRegistry );
        when(provider.getSchemaRegistry(any(Element.class))).thenReturn(mountedRegistry );
        when(m_schemaRegistry.getMountRegistry()).thenReturn(mountRegistry );
        Set<ActionDefinition> actionDefinitions = new HashSet<>();
        ActionDefinition actionDef = mock(ActionDefinition.class);
        ContainerSchemaNode output = mock(ContainerSchemaNode.class);
        when(actionDef.getOutput()).thenReturn(output);
        when(actionDef.getPath()).thenReturn(actionPath);
        ActionDefinition actionDef2 = mock(ActionDefinition.class);
        when(actionDef2.getPath()).thenReturn(actionPath2);
		actionDefinitions.add(actionDef);
		actionDefinitions.add(actionDef2);
		when(mountedRegistry.retrieveAllActionDefinitions()).thenReturn(actionDefinitions );
		when(mountedRegistry.getParentRegistry()).thenReturn(m_schemaRegistry);
		when(mountedRegistry.getMountPath()).thenReturn(SchemaPath.create(true, global));
        when(m_ds.withValidationContext(anyObject())).then(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ValidationContextTemplate<?> template = (ValidationContextTemplate<?>)invocation.getArguments()[0];
                return template.validate();
            } 
        });
        request.setActionTreeElement(getActionTreeElement() );
        request.setMessageId("1");
        ActionResponse response = new ActionResponse();
        m_server.onAction(m_clientInfo, request, response);
        assertTrue(response.isOk());
        when(m_ds.action(any(Document.class), any(ActionRequest.class), any(NetconfClientInfo.class), any(SchemaRegistry.class))).then(new Answer<Object>(){
        	@Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return Arrays.asList(getErrorElement());
            }

			private Element getErrorElement() throws NetconfMessageBuilderException {
				String str = "<rpc-error xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\"> s"
						+ "<error-type>application</error-type> "
						+ "<error-tag>operation-failed</error-tag> "
						+ "<error-severity>error</error-severity> "
						+ "</rpc-error>";
				return stringToDocument(str).getDocumentElement();
			} 
        });
        m_server.onAction(m_clientInfo, request, response);
        verify(m_rpcConstraintParser, times(0)).validate(response, RequestType.ACTION);
    }

    @Test
    public void testActionErrorResponse() throws Exception {
        {
            ActionRequest request = new ActionRequest();
            QName global = QName.create("www.global.com", "global");
            QName qname = QName.create("www.test.com", "testRoot");
            QName childQname = QName.create("www.test.com", "testChild");
            QName actionQName = QName.create("www.test.com", "testAction");
            SchemaPath actionPath = SchemaPath.create(true, qname, childQname, actionQName);
            SchemaPath actionPath2 = SchemaPath.create(true, qname, actionQName);
            DataSchemaNode rootDSN = mock(DataSchemaNode.class, withSettings().extraInterfaces(DataNodeContainer.class));
            ContainerSchemaNode containerChild = mock(ContainerSchemaNode.class);
            Collection<DataSchemaNode> childNodes = Arrays.asList(containerChild);
            SchemaMountRegistry mountRegistry = mock(SchemaMountRegistry.class);
            SchemaMountRegistryProvider provider = mock(SchemaMountRegistryProvider.class);
            SchemaRegistry mountedRegistry = mock(SchemaRegistry.class);

            when(containerChild.getQName()).thenReturn(qname);
            when(((DataNodeContainer)rootDSN).getChildNodes()).thenReturn(childNodes );
            when(rootDSN.getQName()).thenReturn(global);
            when(m_schemaRegistry.getRootDataSchemaNodes()).thenReturn(Arrays.asList(rootDSN ) );
            when(mountRegistry.getProvider(any(SchemaPath.class))).thenReturn(provider );
            when(provider.getSchemaRegistry(any(ModelNodeId.class))).thenReturn(mountedRegistry );
            when(provider.getSchemaRegistry(any(EditContainmentNode.class))).thenReturn(mountedRegistry );
            when(provider.getSchemaRegistry(any(Element.class))).thenReturn(mountedRegistry );
            when(m_schemaRegistry.getMountRegistry()).thenReturn(mountRegistry );
            Set<ActionDefinition> actionDefinitions = new HashSet<>();
            ActionDefinition actionDef = mock(ActionDefinition.class);
            ContainerSchemaNode output = mock(ContainerSchemaNode.class);
            when(actionDef.getOutput()).thenReturn(output);
            when(actionDef.getPath()).thenReturn(actionPath);
            ActionDefinition actionDef2 = mock(ActionDefinition.class);
            when(actionDef2.getPath()).thenReturn(actionPath2);
            actionDefinitions.add(actionDef);
            actionDefinitions.add(actionDef2);
            when(mountedRegistry.retrieveAllActionDefinitions()).thenReturn(actionDefinitions );
            when(mountedRegistry.getParentRegistry()).thenReturn(m_schemaRegistry);
            when(mountedRegistry.getMountPath()).thenReturn(SchemaPath.create(true, global));
            when(m_ds.withValidationContext(anyObject())).then(new Answer<Object>() {
                @Override
                public Object answer(InvocationOnMock invocation) throws Throwable {
                    ValidationContextTemplate<?> template = (ValidationContextTemplate<?>)invocation.getArguments()[0];
                    return template.validate();
                }
            });
            request.setActionTreeElement(getActionTreeElement() );
            request.setMessageId("1");
            ActionResponse response = new ActionResponse();
            when(m_ds.action(any(Document.class), any(ActionRequest.class), any(NetconfClientInfo.class), any(SchemaRegistry.class))).then(new Answer<Object>(){
                @Override
                public Object answer(InvocationOnMock invocation) throws Throwable {
                    return Arrays.asList(getErrorElement());
                }

                private Element getErrorElement() throws NetconfMessageBuilderException {
                    String str = "<rpc-error xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                            "<error-type>application</error-type>" +
                            "<error-tag>operation-failed</error-tag>" +
                            "<error-severity>error</error-severity>" +
                            "<error-path xmlns:test=\"urn:bbf:yang:bbf-test\" " +
                            "xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" xmlns:yangns=\"urn:ietf:params:xml:ns:yang:1\">" +
                            "/nc:rpc/yangns:action/test:testAction</error-path>" +
                            "<error-message>The component does not support reset!</error-message>" +
                            "<error-info>" +
                            "<bad-element>reset</bad-element>" +
                            "</error-info>" +
                            "</rpc-error>";
                    return stringToDocument(str).getDocumentElement();
                }
            });
            m_server.onAction(m_clientInfo, request, response);
            
            String expectedResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                    "<rpc-error>" +
                    "<error-type>application</error-type>" +
                    "<error-tag>operation-failed</error-tag>" +
                    "<error-severity>error</error-severity>" +
                    "<error-path xmlns:test=\"urn:bbf:yang:bbf-test\">" +
                    "/test:testAction</error-path>" +
                    "<error-message>The component does not support reset!</error-message>" +
                    "<error-info>" +
                    "<bad-element>reset</bad-element>" +
                    "</error-info>" +
                    "</rpc-error>" +
                    "</rpc-reply>";
            Element expectedElement = TestUtil.transformToElement(expectedResponse);
            NetconfRpcError rpcError = response.getErrors().get(0);
            assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, rpcError.getErrorTag());
            assertEquals("The component does not support reset!", rpcError.getErrorMessage());
            Element actualElement = TestUtil.transformToElement(response.responseToString());
            TestUtil.assertXMLEquals(expectedElement, actualElement);
            verify(m_rpcConstraintParser, times(0)).validate(response, RequestType.ACTION);
        }
    }
   
    private Element getActionTreeElement() throws NetconfMessageBuilderException{
        String string = "<global xmlns=\"www.global.com\">"
        		+ "<testRoot xmlns=\"www.test.com\">"
        		+ "<testChild>"
        		+ "<testAction>"
        		+ "<input/>"
        		+ "</testAction>"
        		+ "</testChild>"
        		+ "</testRoot>"
        		+ "</global>";
        return stringToDocument(string).getDocumentElement();
    }
    
    @Test
    public void testSchemaMountedRpc() throws Exception{
        ActionRequest request = new ActionRequest();
        QName qname = QName.create("www.test.com", "testRoot");
        QName childQname = QName.create("www.test.com", "testChild");
        QName rpcQname = QName.create("www.test.com", "testRpc");
        DataSchemaNode rootDSN = mock(DataSchemaNode.class, withSettings().extraInterfaces(DataNodeContainer.class));
        ContainerSchemaNode containerChild = mock(ContainerSchemaNode.class);
        Collection<DataSchemaNode> childNodes = Arrays.asList(containerChild);
        SchemaMountRegistry mountRegistry = mock(SchemaMountRegistry.class);
        SchemaMountRegistryProvider provider = mock(SchemaMountRegistryProvider.class);
        SchemaRegistry mountedRegistry = mock(SchemaRegistry.class);
        RpcDefinition rpcDef = mock(RpcDefinition.class);
        RpcRequestHandlerRegistry registry = mock(RpcRequestHandlerRegistry.class);
        MultiRpcRequestHandler rpcHandler = mock(MultiRpcRequestHandler.class);
        m_server.setRpcRequestHandlerRegistry(registry );

        when(containerChild.getQName()).thenReturn(childQname);
        when(((DataNodeContainer)rootDSN).getChildNodes()).thenReturn(childNodes );
        when(rootDSN.getQName()).thenReturn(qname);
        when(m_schemaRegistry.getRootDataSchemaNodes()).thenReturn(Arrays.asList(rootDSN ) );
        when(mountRegistry.getProvider(any(SchemaPath.class))).thenReturn(provider );
        when(provider.getSchemaRegistry(any(ModelNodeId.class))).thenReturn(mountedRegistry );
        when(provider.getSchemaRegistry(any(EditContainmentNode.class))).thenReturn(mountedRegistry );
        when(provider.getSchemaRegistry(any(Element.class))).thenReturn(mountedRegistry );
        when(m_schemaRegistry.getMountRegistry()).thenReturn(mountRegistry );
        when(mountedRegistry.getRpcDefinitions()).thenReturn(Arrays.asList(rpcDef));
        when(rpcDef.getQName()).thenReturn(rpcQname);
        when(registry.getMultiRpcRequestHandler(any(NetconfRpcRequest.class))).thenReturn(rpcHandler);
        when(rpcHandler.processRequest(any(NetconfClientInfo.class), any(NetconfRpcRequest.class), any(NetconfRpcResponse.class))).then(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                NetconfRpcResponse response = (NetconfRpcResponse)invocation.getArguments()[2];
                String output = "<result xmlns=\"www.test.com\"/>";
                Element outputElement = stringToDocument(output).getDocumentElement();
                response.addRpcOutputElement(outputElement);
                return null;
            } 
        });
        when(m_ds.withValidationContext(anyObject())).then(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ValidationContextTemplate<?> template = (ValidationContextTemplate<?>)invocation.getArguments()[0];
                return template.validate();
            } 
        });
        
        request.setActionTreeElement(getRpcTreeElement() );
        request.setMessageId("1");
        ActionResponse response = new ActionResponse();
        m_server.onAction(m_clientInfo, request, response);
        assertEquals(1, response.getActionOutputElements().size());
    }
    
    private Element getRpcTreeElement() throws NetconfMessageBuilderException{
        String string = "<testRoot xmlns=\"www.test.com\">"
        		+ "<testChild>"
        		+ "<testRpc>"
        		+ "<input/>"
        		+ "</testRpc>"
        		+ "</testChild>"
        		+ "</testRoot>";
        return stringToDocument(string).getDocumentElement();
    }
    
    @Test
    public void testUnexpectedBehaviorOnEditConfig() {
        // just a dummy request
        String requestXml = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + " <validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + " </validation>";
        EditConfigRequest request = createRequestFromString(requestXml);        
        request.setMessageId("1");
        NetConfResponse response = new NetConfResponse();
        String errorMessage = "Null value passed";
        doThrow(new NullPointerException(errorMessage)).when(m_rpcConstraintParser).validate(request, RequestType.EDIT_CONFIG);
        
        m_server.onEditConfig(m_clientInfo, request, response);
        List<NetconfRpcError> errors = response.getErrors();
        assertEquals(1, errors.size());
        NetconfRpcError error = errors.get(0);
        assertEquals(NetconfRpcErrorType.Application, error.getErrorType());
        assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, error.getErrorTag());
        assertEquals(NetconfRpcErrorSeverity.Error, error.getErrorSeverity());
        String message = error.getErrorMessage();
        String info = error.getErrorInfo().getTextContent();
        assertTrue(message.contains("Operation Failed with Exception: " + errorMessage));
        assertTrue(info.contains("at org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NetConfServerImpl.onEditConfig"));
        assertTrue(info.contains("at org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NetconfServerImplTest.testUnexpectedBehaviorOnEditConfig"));
    }


    @Test
    public void testDefaultOperationOnEditConfig() throws NetconfMessageBuilderException {

        // REPLACE is the default operation here
        String defaultOperation = "replace";
        String requestXml = getRequestWithDefaultOperation(defaultOperation);
        Document requestDocument = stringToDocument(requestXml);
        EditConfigRequest request = DocumentToPojoTransformer.getEditConfig(requestDocument);
        request.setMessageId("1");
        NetConfResponse response = new NetConfResponse();

        m_server.onEditConfig(m_clientInfo, request, response);
        assertEquals(defaultOperation, DataStoreValidationUtil.getDefaultEditConfigOperationInCache());
        assertEquals(defaultOperation, OperationAttributeUtil.getOperationAttribute(requestDocument.getDocumentElement()));

        // MERGE is the default operation here
        defaultOperation = "merge";
        requestXml = getRequestWithDefaultOperation(defaultOperation);
        requestDocument = stringToDocument(requestXml);
        request = DocumentToPojoTransformer.getEditConfig(requestDocument);
        request.setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request, response);
        assertEquals(defaultOperation, DataStoreValidationUtil.getDefaultEditConfigOperationInCache());
        assertEquals(defaultOperation, OperationAttributeUtil.getOperationAttribute(requestDocument.getDocumentElement()));

        // NO default operation here. So MERGE should be considered default operation
        defaultOperation = null;
        requestXml = getRequestWithDefaultOperation(defaultOperation);
        requestDocument = stringToDocument(requestXml);
        request = DocumentToPojoTransformer.getEditConfig(requestDocument);
        request.setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request, response);
        assertEquals("merge", DataStoreValidationUtil.getDefaultEditConfigOperationInCache());
        assertEquals("merge", OperationAttributeUtil.getOperationAttribute(requestDocument.getDocumentElement()));
    }

    private String getRequestWithDefaultOperation(String defaultOperation) {
        String requestString = "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n"
                            + "    <edit-config>\n"
                            + "        <target>\n"
                            + "            <running/>\n"
                            + "        </target>\n"
                            + "        <test-option>set</test-option>\n";
        if(defaultOperation != null) {
            requestString += "        <default-operation>" + defaultOperation +"</default-operation>\n";
        }
        requestString +=  "        <config xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n"
                            + "            <interfaces xmlns=\"urn:ietf:params:xml:ns:yang:ietf-interfaces\">\n"
                            + "              <interface xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">\n"
                            + "                <name>cg_cp1</name>\n"
                            + "                <type xmlns:bbf-xponift=\"urn:bbf:yang:bbf-xpon-if-type\">bbf-xponift:channel-group</type>\n"
                            + "                <channel-group xmlns=\"urn:bbf:yang:bbf-xpon\">\n"
                            + "                  <polling-period>100</polling-period>\n"
                            + "                  <raman-mitigation>raman_none</raman-mitigation>\n"
                            + "                  <system-id>000000</system-id>\n"
                            + "                </channel-group>\n"
                            + "              </interface>\n"
                            + "            </interfaces>\n"
                            + "        </config>\n"
                            + "    </edit-config>\n"
                            + " </rpc>";
        return requestString;
    }

    private EditConfigRequest createRequestFromString(String xmlRequest) {
        return new EditConfigRequest().setTargetRunning().setTestOption(EditConfigTestOptions.SET)
                .setErrorOption(EditConfigErrorOptions.STOP_ON_ERROR)
                .setConfigElement(new EditConfigElement().addConfigElementContent(transformToElement(xmlRequest)));
    }
}
