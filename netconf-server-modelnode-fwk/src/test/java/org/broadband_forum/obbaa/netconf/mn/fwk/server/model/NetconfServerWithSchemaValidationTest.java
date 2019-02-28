package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.DataStoreValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.utils.TxService;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigElement;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorSeverity;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorType;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcResponse;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.RpcRequestConstraintParser;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.NetconfRpcErrorUtil;

import org.broadband_forum.obbaa.netconf.server.rpc.RequestType;

/**
 * Created by keshava on 11/25/15.
 */
public class NetconfServerWithSchemaValidationTest {
    NetConfServerImpl m_server;
    private RpcRequestConstraintParser m_mockEditcofigValidator;
    private SchemaRegistry m_mockSchemaRegistry;
    private NetconfClientInfo m_clientInfo = new NetconfClientInfo("UT", 1);

	private List<Element> getElements() {
		List<Element> elements = new ArrayList<Element>();
        Document document = DocumentUtils.createDocument();
        Element element  = document.createElementNS("ns", "config");
        elements.add(element);
		return elements;
	}
    
    @Before
    public void setUp(){
        m_mockEditcofigValidator = mock(RpcRequestConstraintParser.class);
        m_mockSchemaRegistry = mock(SchemaRegistry.class);
        m_server = new NetConfServerImpl(m_mockSchemaRegistry, m_mockEditcofigValidator);
    }

    @Test
    public void testErrorIsReturned() throws Exception {
        Element dummyElement = DocumentUtils.createDocument().createElement("dummy");
        EditConfigRequest request = new EditConfigRequest();
        List<Element> elements = getElements();
        EditConfigElement element = mock(EditConfigElement.class);
        when(element.getConfigElementContents()).thenReturn(elements);
        when(element.getXmlElement()).thenReturn(dummyElement);
        request.setConfigElement(element);
        
        request.setMessageId("1");
        
        NetconfRpcError error = NetconfRpcErrorUtil.getNetconfRpcError(NetconfRpcErrorTag.ACCESS_DENIED, NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Warning, "message");
        doThrow(new ValidationException(error)).when(m_mockEditcofigValidator).validate(request, RequestType.EDIT_CONFIG);
       
        NetConfResponse response = new NetConfResponse();
        m_server.onEditConfig(m_clientInfo, request, response);
        assertFalse(response.isOk());
        assertEquals(error, response.getErrors().get(0));
    }

    @Test
    public void testValidateMessageId() throws ValidationException {
        EditConfigRequest request = new EditConfigRequest();
        NetConfResponse response = new NetConfResponse();
        Object returnValue = m_server.onEditConfig(m_clientInfo, request, response);
        assertEquals(null, returnValue);
    }

    @Test
    public void testRpcRequestIsValidated() throws ValidationException {
        RootModelNodeAggregator aggregator = mock(RootModelNodeAggregator.class);
        SubSystemRegistry subSysRegistry = mock(SubSystemRegistry.class);
        DataStoreValidator validator = mock(DataStoreValidator.class);
        DataStore datastore = new DataStore("test", aggregator, subSysRegistry, validator, new TxService(), null);
        m_server.setRunningDataStore(datastore);
        m_server.setRpcRequestHandlerRegistry(mock(RpcRequestHandlerRegistry.class));
        NetconfRpcRequest request = new NetconfRpcRequest();
        request.setRpcInput(getElements().get(0));
        NetconfRpcResponse response = new NetconfRpcResponse();
        Object returnValue = m_server.onRpc(m_clientInfo, request, response);
        assertEquals(null, returnValue);
        request.setMessageId("1");
        m_server.onRpc(m_clientInfo, request, response);
    }
}
