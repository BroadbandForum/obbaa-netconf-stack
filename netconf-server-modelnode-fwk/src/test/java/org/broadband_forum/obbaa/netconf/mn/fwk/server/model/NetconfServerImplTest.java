package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
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

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.ActionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.ActionResponse;
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
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaMountRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaMountRegistryProvider;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.SubtreeFilterUtil;

import org.broadband_forum.obbaa.netconf.server.rpc.MultiRpcRequestHandler;
import org.broadband_forum.obbaa.netconf.server.rpc.RequestType;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcPayloadConstraintParser;

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
        when(m_ds.get(anyObject(), anyObject(), anyObject())).thenReturn(m_values);
        m_server = new NetConfServerImpl(m_schemaRegistry, m_rpcConstraintParser, m_util);
        m_server.setRunningDataStore(m_ds);
    }

    @Test
    public void tesGetUseSystemVar(){
        System.setProperty(GET_FINAL_FILTER, "false");
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
        when(provider.getSchemaRegistry(any(ModelNode.class), anyObject())).thenReturn(mountedRegistry );
        when(provider.getSchemaRegistry(anyObject(), anyObject())).thenReturn(mountedRegistry );
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
        when(m_ds.action(any(Document.class), any(ActionRequest.class), any(NetconfClientInfo.class))).then(new Answer<Object>(){
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
				return DocumentUtils.stringToDocument(str).getDocumentElement();
			} 
        });
        m_server.onAction(m_clientInfo, request, response);
        verify(m_rpcConstraintParser, times(0)).validate(response, RequestType.ACTION);
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
        return DocumentUtils.stringToDocument(string).getDocumentElement();
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
        when(provider.getSchemaRegistry(any(ModelNode.class), anyObject())).thenReturn(mountedRegistry );
        when(provider.getSchemaRegistry(anyObject(), anyObject())).thenReturn(mountedRegistry );
        when(m_schemaRegistry.getMountRegistry()).thenReturn(mountRegistry );
        when(mountedRegistry.getRpcDefinitions()).thenReturn(Arrays.asList(rpcDef));
        when(rpcDef.getQName()).thenReturn(rpcQname);
        when(registry.getMultiRpcRequestHandler(any(NetconfRpcRequest.class))).thenReturn(rpcHandler);
        when(rpcHandler.processRequest(any(NetconfClientInfo.class), any(NetconfRpcRequest.class), any(NetconfRpcResponse.class))).then(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                NetconfRpcResponse response = (NetconfRpcResponse)invocation.getArguments()[2];
                String output = "<result xmlns=\"www.test.com\"/>";
                Element outputElement = DocumentUtils.stringToDocument(output).getDocumentElement();
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
        return DocumentUtils.stringToDocument(string).getDocumentElement();
    }
    
    @Test
    public void testUnexpectedBehaviorOnEditConfig() {
        // just a dummy request
        String requestXml = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + " <validation xmlns=\"urn:org:bbf:pma:validation\">"
                + " </validation>";
        EditConfigRequest request = createRequestFromString(requestXml);        
        request.setMessageId("1");
        NetConfResponse response = new NetConfResponse();
        
        doThrow(new NullPointerException()).when(m_rpcConstraintParser).validate(request, RequestType.EDIT_CONFIG);
        
        m_server.onEditConfig(m_clientInfo, request, response);
        List<NetconfRpcError> errors = response.getErrors();
        assertEquals(1, errors.size());
        NetconfRpcError error = errors.get(0);
        assertEquals(NetconfRpcErrorType.Application, error.getErrorType());
        assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, error.getErrorTag());
        assertEquals(NetconfRpcErrorSeverity.Error, error.getErrorSeverity());
        String message = error.getErrorMessage();
        assertTrue(message.contains("NullPointerException"));
        assertTrue(message.contains("at org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NetConfServerImpl.onEditConfig"));
        assertTrue(message.contains("at org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NetconfServerImplTest.testUnexpectedBehaviorOnEditConfig"));
    }

    private EditConfigRequest createRequestFromString(String xmlRequest) {
        return new EditConfigRequest().setTargetRunning().setTestOption(EditConfigTestOptions.SET)
                .setErrorOption(EditConfigErrorOptions.STOP_ON_ERROR)
                .setConfigElement(new EditConfigElement().addConfigElementContent(transformToElement(xmlRequest)));
    }
}
