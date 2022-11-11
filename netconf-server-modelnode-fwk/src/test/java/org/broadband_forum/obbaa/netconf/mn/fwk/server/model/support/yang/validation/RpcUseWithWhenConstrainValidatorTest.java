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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcResponse;
import org.broadband_forum.obbaa.netconf.api.messages.RpcName;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.RpcRequestConstraintParser;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.payloadparsing.DummyRpcHandler;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.TimingLogger;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationUtil;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcPayloadConstraintParser;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcValidationException;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Element;

@RunWith(RequestScopeJunitRunner.class)
public class RpcUseWithWhenConstrainValidatorTest extends AbstractDataStoreValidatorTest {
    
    private DummyRpcHandler m_dummyRpcInputUsesWhen;
    private RpcPayloadConstraintParser m_rpcConstraintParser;
    private ModelNodeDataStoreManager m_modelNodeDsm;
    private DummyRpcHandler m_dummyRpcOutputUsesWhen;
   
    @Before
    public void setup() throws Exception {
        super.setUp();
        m_dummyRpcInputUsesWhen = new DummyRpcHandler(new RpcName("urn:org:bbf2:pma:validation", "testRpcInput"));
        m_dummyRpcOutputUsesWhen = new DummyRpcHandler(new RpcName("urn:org:bbf2:pma:validation", "testRpcOutputUsesWithWhen"));
        m_modelNodeDsm = super.m_modelNodeDsm;
        m_rpcConstraintParser = new RpcRequestConstraintParser(m_schemaRegistry, m_modelNodeDsm, m_expValidator, null);
    }
   
    @Test
    public void testRpc_UsesWithWhenCondition() throws Exception {
        getModelNode();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf2:pma:validation\">" 
                + "  <test-interfaces>"
                + "    <test-interface>" 
                + "     <name>test</name>" 
                + "     <type>interfaceType</type>"
                + "    </test-interface>" 
                + "  </test-interfaces>" 
                + "</validation>";

        EditConfigRequest request = createRequestFromString(requestXml);
        request.setMessageId("1");
        NetConfResponse response = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request, response);
        assertTrue(response.isOk());
        String xmlPath = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <validation:testRpcInput xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                + " <validation:container1>" 
                + " <validation:innerContainer>" 
                + " <validation:innerList>"
                + "<validation:innerLeaf>hello</validation:innerLeaf>" 
                + "</validation:innerList>"
                + "</validation:innerContainer>" 
                + "</validation:container1>" 
                + "<validation:upperLeaf>test1</validation:upperLeaf>"                
                + " </validation:testRpcInput>" 
                + " </rpc>";

        Element element = DocumentUtils.stringToDocument(xmlPath).getDocumentElement();
        DataStoreValidationUtil.setRootModelNodeAggregatorInCache(m_rootModelNodeAggregator);
        NetconfRpcRequest netconfrequest = new NetconfRpcRequest();
        netconfrequest.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element));
        netconfrequest.setMessageId("1");
        TimingLogger.withStartAndFinish(() -> m_dummyRpcInputUsesWhen.validate(m_rpcConstraintParser, netconfrequest));
    }
    
    @Test
    public void testRpcInputOfUsesWithWhenConditionFailureCase() throws Exception {
        getModelNode();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf2:pma:validation\">" 
                + "  <test-interfaces>"
                + "    <test-interface>" 
                + "     <name>test</name>" 
                + "     <type>interfaceType10</type>"
                + "    </test-interface>" 
                + "  </test-interfaces>" 
                + "</validation>";

        EditConfigRequest request = createRequestFromString(requestXml);
        request.setMessageId("1");
        NetConfResponse response = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request, response);
        assertTrue(response.isOk());
        String xmlPath = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <validation:testRpcInput xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                + " <validation:container1>" 
                + " <validation:innerContainer>" 
                + " <validation:innerList>"
                + "<validation:innerLeaf>hello</validation:innerLeaf>" 
                + "</validation:innerList>"
                + "</validation:innerContainer>" 
                + "</validation:container1>" 
                + "<validation:upperLeaf>test1</validation:upperLeaf>"
                + " </validation:testRpcInput>" 
                + " </rpc>";

        Element element = DocumentUtils.stringToDocument(xmlPath).getDocumentElement();
        DataStoreValidationUtil.setRootModelNodeAggregatorInCache(m_rootModelNodeAggregator);
        NetconfRpcRequest netconfrequest = new NetconfRpcRequest();
        netconfrequest.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element));
        netconfrequest.setMessageId("1");

        try {
            TimingLogger.withStartAndFinish(() -> m_dummyRpcInputUsesWhen.validate(m_rpcConstraintParser, netconfrequest));
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof RpcValidationException);
            ValidationException cause = (ValidationException) e.getCause();
            assertEquals(NetconfRpcErrorTag.UNKNOWN_ELEMENT, cause.getRpcErrors().get(0).getErrorTag());
            assertTrue(e.getMessage().contains("Violate when constraints: /validation:validation/validation:test-interfaces/validation:test-interface[validation:name='test']/validation:type='interfaceType'"));
        }
    }
    
    @Test
    public void testRpcOutputOfUsesWithWhenCondition() throws Exception{
        getModelNode();
        
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + "  <test-interfaces>"
                + "    <test-interface>"
                + "     <name>test</name>"
                + "     <type>interfaceType</type>"
                + "    </test-interface>"
                + "  </test-interfaces>"
                + "</validation>";

        EditConfigRequest request = createRequestFromString(requestXml);
        request.setMessageId("1");
        NetConfResponse response = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request, response);
        assertTrue(response.isOk());
        String xmlPath = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <validation:container1 xmlns:validation=\"urn:org:bbf2:pma:validation\">"                                
                + " <validation:innerContainer>"
                + " <validation:innerList>"
                + "<validation:innerLeaf>hello</validation:innerLeaf>"
                + "</validation:innerList>"
                + "</validation:innerContainer>"
                + "</validation:container1>"
                + "<validation:testGroup1 xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                + "<validation:groupLeaf1>test10</validation:groupLeaf1>"
                + "</validation:testGroup1>"
                + " </rpc-reply>" 
                ;

        Element returnValue = DocumentUtils.getDocumentElement(xmlPath);
        DataStoreValidationUtil.setRootModelNodeAggregatorInCache(m_rootModelNodeAggregator);
        NetconfRpcResponse netconfResponse = new NetconfRpcResponse();
        netconfResponse.setMessageId("1");
        TestUtil.addOutputElements(returnValue, netconfResponse);
        netconfResponse.setRpcName(new RpcName("urn:org:bbf2:pma:validation", "testRpcOutputUsesWithWhen"));
        m_dummyRpcOutputUsesWhen.validate(m_rpcConstraintParser, netconfResponse);
        assertTrue(netconfResponse.getErrors().isEmpty()); 
}
    
    @Test
    public void testRpcOutputOfUsesWithWhenConditionFailureCase() throws Exception{
        getModelNode();
        
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + "  <test-interfaces>"
                + "    <test-interface>"
                + "     <name>test</name>"
                + "     <type>interfaceType23</type>"
                + "    </test-interface>"
                + "  </test-interfaces>"
                + "</validation>";

        EditConfigRequest request = createRequestFromString(requestXml);
        request.setMessageId("1");
        NetConfResponse response = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request, response);
        assertTrue(response.isOk());
        String xmlPath = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <validation:container1 xmlns:validation=\"urn:org:bbf2:pma:validation\">"                                
                + " <validation:innerContainer>"
                + " <validation:innerList>"
                + "<validation:innerLeaf>hello</validation:innerLeaf>"
                + "</validation:innerList>"
                + "</validation:innerContainer>"
                + "</validation:container1>"
                + "<validation:testGroup1 xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                + "<validation:groupLeaf1>test10</validation:groupLeaf1>"
                + "</validation:testGroup1>"
                + " </rpc-reply>" 
                ;

        Element returnValue = DocumentUtils.getDocumentElement(xmlPath);
        DataStoreValidationUtil.setRootModelNodeAggregatorInCache(m_rootModelNodeAggregator);
        NetconfRpcResponse netconfResponse = new NetconfRpcResponse();
        netconfResponse.setMessageId("1");
        TestUtil.addOutputElements(returnValue, netconfResponse);
        netconfResponse.setRpcName(new RpcName("urn:org:bbf2:pma:validation", "testRpcOutputUsesWithWhen"));
        TimingLogger.withStartAndFinish(() -> m_dummyRpcOutputUsesWhen.validate(m_rpcConstraintParser, netconfResponse));
        assertEquals(NetconfRpcErrorTag.UNKNOWN_ELEMENT, netconfResponse.getErrors().get(0).getErrorTag());
        assertTrue(netconfResponse.getErrors().get(0).getErrorMessage().contains("Violate when constraints: /validation:validation/validation:test-interfaces/validation:test-interface[validation:name='test']/validation:type='interfaceType'"));
        assertFalse(netconfResponse.getErrors().isEmpty());     
}
        
    @After
    public void teardown() {
        m_dataStore.disableUTSupport();
        m_datastoreValidator.setValidatedChildCacheHitStatus(false);
   }
}


