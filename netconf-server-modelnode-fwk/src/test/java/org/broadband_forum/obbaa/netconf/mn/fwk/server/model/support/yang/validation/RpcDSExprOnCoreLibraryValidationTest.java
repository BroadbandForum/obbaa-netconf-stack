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
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcPayloadConstraintParser;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcValidationException;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Element;

@RunWith(RequestScopeJunitRunner.class)
public class RpcDSExprOnCoreLibraryValidationTest extends AbstractDataStoreValidatorTest {

    private DummyRpcHandler m_dummyRpcInputMustNumber;
    private DummyRpcHandler m_dummyRpcOutputMustNumber;
    private DummyRpcHandler m_dummyRpcInputBinary;
    private DummyRpcHandler m_dummyRpcOutputBinary;
    private DummyRpcHandler m_dummyRpcInputNamespace;
    private DummyRpcHandler m_dummyRpcOutputNamespace;
    private DummyRpcHandler  m_dummyRpcInputLocalName;
    private DummyRpcHandler  m_dummyRpcOutputLocalName;
    private DummyRpcHandler  m_dummyRpcInputString;
    private DummyRpcHandler  m_dummyRpcOutputString;
    private RpcPayloadConstraintParser m_rpcConstraintParser;
    private ModelNodeDataStoreManager m_modelNodeDsm;
    
    @Before
    public void setup() throws Exception {
        super.setUp();
        m_dummyRpcInputMustNumber = new DummyRpcHandler(new RpcName("urn:org:bbf2:pma:validation", "testRpcInputMustNumber"));
        m_dummyRpcOutputMustNumber = new DummyRpcHandler(new RpcName("urn:org:bbf2:pma:validation", "testRpcOutputMustNumber"));
        m_dummyRpcInputBinary = new DummyRpcHandler(new RpcName("urn:org:bbf2:pma:validation", "testRpcInputBoolean"));
        m_dummyRpcOutputBinary = new DummyRpcHandler(new RpcName("urn:org:bbf2:pma:validation", "testRpcOutputBoolean"));
        m_dummyRpcInputNamespace = new DummyRpcHandler(new RpcName("urn:org:bbf2:pma:validation", "testRpcInputNamespace"));
        m_dummyRpcOutputNamespace = new DummyRpcHandler(new RpcName("urn:org:bbf2:pma:validation", "testRpcOutputNamespace"));
        m_dummyRpcInputLocalName = new DummyRpcHandler(new RpcName("urn:org:bbf2:pma:validation", "testRpcInputLocalName"));
        m_dummyRpcOutputLocalName = new DummyRpcHandler(new RpcName("urn:org:bbf2:pma:validation", "testRpcOutputLocalName"));
        m_dummyRpcInputString = new DummyRpcHandler(new RpcName("urn:org:bbf2:pma:validation", "testRpcInputString"));
        m_dummyRpcOutputString = new DummyRpcHandler(new RpcName("urn:org:bbf2:pma:validation", "testRpcOutputString"));
        m_modelNodeDsm = super.m_modelNodeDsm;
        m_rpcConstraintParser = new RpcRequestConstraintParser(m_schemaRegistry, m_modelNodeDsm, m_expValidator, null);
    }         
	
    @Test
    public void testRpcNumberFunction() throws Exception {
        getModelNode();
        String rpcRequest1 = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <whenMust:testRpcInputMustNumber xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + " <whenMust:container1>" 
                + " <whenMust:innerContainer>"
                + " <whenMust:innerLeaf>45</whenMust:innerLeaf>"
                + " <whenMust:number-function-leaf>50</whenMust:number-function-leaf>" 
                + " </whenMust:innerContainer>"
                + " </whenMust:container1>" 
                + " </whenMust:testRpcInputMustNumber>" 
                + " </rpc>";

        Element element1 = DocumentUtils.stringToDocument(rpcRequest1).getDocumentElement();
        NetconfRpcRequest netconfrequest1 = new NetconfRpcRequest();
        netconfrequest1.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element1));
        netconfrequest1.setMessageId("1");
        TimingLogger.withStartAndFinish(() -> m_dummyRpcInputMustNumber.validate(m_rpcConstraintParser, netconfrequest1));

        String rpcRequest2 = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <whenMust:testRpcInputMustNumber xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + " <whenMust:container1>" 
                + " <whenMust:innerContainer>"
                + " <whenMust:innerLeaf>46</whenMust:innerLeaf>"
                + " <whenMust:number-function-leaf>50</whenMust:number-function-leaf>" 
                + " </whenMust:innerContainer>"
                + " </whenMust:container1>" 
                + " </whenMust:testRpcInputMustNumber>" 
                + " </rpc>";

        Element element2 = DocumentUtils.stringToDocument(rpcRequest2).getDocumentElement();
        NetconfRpcRequest netconfrequest2 = new NetconfRpcRequest();
        netconfrequest2.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element2));
        netconfrequest2.setMessageId("1");

        try {
            TimingLogger.withStartAndFinish(() -> m_dummyRpcInputMustNumber.validate(m_rpcConstraintParser, netconfrequest2));
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof RpcValidationException);
            ValidationException cause = (ValidationException) e.getCause();
            assertEquals(NetconfRpcErrorTag.UNKNOWN_ELEMENT, cause.getRpcErrors().get(0).getErrorTag());
            assertTrue(e.getMessage().contains("Violate when constraints: number(../innerLeaf) = 45"));
        }

    }

    @Test
    public void testRpcFloorFunction() throws Exception {
        getModelNode();
        String rpcRequest1 = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <whenMust:testRpcInputMustNumber xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + " <whenMust:container1>" 
                + " <whenMust:innerContainer>"
                + " <whenMust:innerLeaf>45.2</whenMust:innerLeaf>"
                + " <whenMust:floor-function-leaf>test</whenMust:floor-function-leaf>" 
                + " </whenMust:innerContainer>"
                + " </whenMust:container1>" 
                + " </whenMust:testRpcInputMustNumber>" 
                + " </rpc>";

        Element element1 = DocumentUtils.stringToDocument(rpcRequest1).getDocumentElement();
        NetconfRpcRequest netconfrequest1 = new NetconfRpcRequest();
        netconfrequest1.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element1));
        netconfrequest1.setMessageId("1");
        TimingLogger.withStartAndFinish(() -> m_dummyRpcInputMustNumber.validate(m_rpcConstraintParser, netconfrequest1));

        String rpcRequest2 = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <whenMust:testRpcInputMustNumber xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + " <whenMust:container1>" 
                + " <whenMust:innerContainer>"
                + " <whenMust:innerLeaf>44.2</whenMust:innerLeaf>"
                + " <whenMust:floor-function-leaf>test</whenMust:floor-function-leaf>" 
                + " </whenMust:innerContainer>"
                + " </whenMust:container1>" 
                + " </whenMust:testRpcInputMustNumber>" 
                + " </rpc>";

        Element element2 = DocumentUtils.stringToDocument(rpcRequest2).getDocumentElement();
        NetconfRpcRequest netconfrequest2 = new NetconfRpcRequest();
        netconfrequest2.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element2));
        netconfrequest2.setMessageId("1");
        try {
            TimingLogger.withStartAndFinish(() -> m_dummyRpcInputMustNumber.validate(m_rpcConstraintParser, netconfrequest2));
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof RpcValidationException);
            ValidationException cause = (ValidationException) e.getCause();
            assertEquals(NetconfRpcErrorTag.UNKNOWN_ELEMENT, cause.getRpcErrors().get(0).getErrorTag());
            assertTrue(e.getMessage().contains("RPC Validation failed: Violate when constraints: floor(../innerLeaf) = 45"));
        }
    }

    @Test
    public void testRpcCeilingFunction() throws Exception {
        getModelNode();
        String rpcRequest1 = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <whenMust:testRpcInputMustNumber xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + " <whenMust:container1>" 
                + " <whenMust:innerContainer>"
                + " <whenMust:innerLeaf>44.2</whenMust:innerLeaf>"
                + " <whenMust:ceiling-function-leaf>test</whenMust:ceiling-function-leaf>"
                + " </whenMust:innerContainer>" 
                + " </whenMust:container1>" 
                + " </whenMust:testRpcInputMustNumber>"
                + " </rpc>";

        Element element1 = DocumentUtils.stringToDocument(rpcRequest1).getDocumentElement();
        NetconfRpcRequest netconfrequest1 = new NetconfRpcRequest();
        netconfrequest1.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element1));
        netconfrequest1.setMessageId("1");
        TimingLogger.withStartAndFinish(() -> m_dummyRpcInputMustNumber.validate(m_rpcConstraintParser, netconfrequest1));

        String rpcRequest2 = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <whenMust:testRpcInputMustNumber xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + " <whenMust:container1>" 
                + " <whenMust:innerContainer>"
                + " <whenMust:innerLeaf>45.2</whenMust:innerLeaf>"
                + " <whenMust:ceiling-function-leaf>test</whenMust:ceiling-function-leaf>"
                + " </whenMust:innerContainer>" 
                + " </whenMust:container1>" 
                + " </whenMust:testRpcInputMustNumber>"
                + " </rpc>";

        Element element2 = DocumentUtils.stringToDocument(rpcRequest2).getDocumentElement();
        NetconfRpcRequest netconfrequest2 = new NetconfRpcRequest();
        netconfrequest2.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element2));
        netconfrequest2.setMessageId("1");
        try {
            TimingLogger.withStartAndFinish(() -> m_dummyRpcInputMustNumber.validate(m_rpcConstraintParser, netconfrequest2));
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof RpcValidationException);
            ValidationException cause = (ValidationException) e.getCause();
            assertEquals(NetconfRpcErrorTag.UNKNOWN_ELEMENT, cause.getRpcErrors().get(0).getErrorTag());
            assertTrue(e.getMessage().contains("RPC Validation failed: Violate when constraints: ceiling(../innerLeaf) = 45"));
        }
    }
    
    @Test
    public void testRpcRoundFunction() throws Exception {
        getModelNode();
        String rpcRequest1 = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <whenMust:testRpcInputMustNumber xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + " <whenMust:container1>" 
                + " <whenMust:innerContainer>"
                + " <whenMust:innerLeaf>45.2</whenMust:innerLeaf>"
                + " <whenMust:round-function-leaf>test</whenMust:round-function-leaf>"
                + " </whenMust:innerContainer>" 
                + " </whenMust:container1>" 
                + " </whenMust:testRpcInputMustNumber>"
                + " </rpc>";

        Element element1 = DocumentUtils.stringToDocument(rpcRequest1).getDocumentElement();
        NetconfRpcRequest netconfrequest1 = new NetconfRpcRequest();
        netconfrequest1.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element1));
        netconfrequest1.setMessageId("1");
        TimingLogger.withStartAndFinish(() -> m_dummyRpcInputMustNumber.validate(m_rpcConstraintParser, netconfrequest1));

        String rpcRequest2 = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <whenMust:testRpcInputMustNumber xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + " <whenMust:container1>" 
                + " <whenMust:innerContainer>"
                + " <whenMust:innerLeaf>46.2</whenMust:innerLeaf>"
                + " <whenMust:round-function-leaf>test</whenMust:round-function-leaf>"
                + " </whenMust:innerContainer>" 
                + " </whenMust:container1>" 
                + " </whenMust:testRpcInputMustNumber>"
                + " </rpc>";

        Element element2 = DocumentUtils.stringToDocument(rpcRequest2).getDocumentElement();
        NetconfRpcRequest netconfrequest2 = new NetconfRpcRequest();
        netconfrequest2.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element2));
        netconfrequest2.setMessageId("1");
        try {
            TimingLogger.withStartAndFinish(() -> m_dummyRpcInputMustNumber.validate(m_rpcConstraintParser, netconfrequest2));
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof RpcValidationException);
            ValidationException cause = (ValidationException) e.getCause();
            assertEquals(NetconfRpcErrorTag.UNKNOWN_ELEMENT, cause.getRpcErrors().get(0).getErrorTag());
            assertTrue(e.getMessage().contains("RPC Validation failed: Violate when constraints: round(../innerLeaf)"));
        }
    }        

    @Test
    public void testRpcBooleanFunction() throws Exception {
        getModelNode();
        String rpcRequest1 = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <whenMust:testRpcInputBoolean xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + " <whenMust:container1>" 
                + " <whenMust:innerContainer>" 
                + " <whenMust:string1>test</whenMust:string1>"
                + " <whenMust:boolean-function-non-empty-string-arg>test10</whenMust:boolean-function-non-empty-string-arg>"
                + " </whenMust:innerContainer>" 
                + " </whenMust:container1>" 
                + " </whenMust:testRpcInputBoolean>"
                + " </rpc>";

        Element element1 = DocumentUtils.stringToDocument(rpcRequest1).getDocumentElement();
        NetconfRpcRequest netconfrequest1 = new NetconfRpcRequest();
        netconfrequest1.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element1));
        netconfrequest1.setMessageId("1");
        TimingLogger.withStartAndFinish(() -> m_dummyRpcInputBinary.validate(m_rpcConstraintParser, netconfrequest1));

        String rpcRequest2 = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <whenMust:testRpcInputBoolean xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + " <whenMust:container1>" 
                + " <whenMust:innerContainer>"
                + " <whenMust:true-function-leaf>test11</whenMust:true-function-leaf>" 
                + " </whenMust:innerContainer>"
                + " </whenMust:container1>" 
                + " </whenMust:testRpcInputBoolean>" 
                + " </rpc>";

        Element element2 = DocumentUtils.stringToDocument(rpcRequest2).getDocumentElement();
        NetconfRpcRequest netconfrequest2 = new NetconfRpcRequest();
        netconfrequest2.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element2));
        netconfrequest2.setMessageId("1");
        TimingLogger.withStartAndFinish(() -> m_dummyRpcInputBinary.validate(m_rpcConstraintParser, netconfrequest2));

        String rpcRequest3 = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <whenMust:testRpcInputBoolean xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + " <whenMust:container1>" 
                + " <whenMust:innerContainer>"
                + " <whenMust:false-function-leaf>test10</whenMust:false-function-leaf>" 
                + " </whenMust:innerContainer>"
                + " </whenMust:container1>" 
                + " </whenMust:testRpcInputBoolean>" 
                + " </rpc>";

        Element element3 = DocumentUtils.stringToDocument(rpcRequest3).getDocumentElement();
        NetconfRpcRequest netconfrequest3 = new NetconfRpcRequest();
        netconfrequest3.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element3));
        netconfrequest3.setMessageId("1");
        TimingLogger.withStartAndFinish(() -> m_dummyRpcInputBinary.validate(m_rpcConstraintParser, netconfrequest3));

        String rpcRequest4 = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <whenMust:testRpcInputBoolean xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + " <whenMust:container1>" 
                + " <whenMust:innerContainer>"
                + " <whenMust:not-function-leaf>test56</whenMust:not-function-leaf>" 
                + " </whenMust:innerContainer>"
                + " </whenMust:container1>" 
                + " </whenMust:testRpcInputBoolean>" 
                + " </rpc>";

        Element element4 = DocumentUtils.stringToDocument(rpcRequest4).getDocumentElement();
        NetconfRpcRequest netconfrequest4 = new NetconfRpcRequest();
        netconfrequest4.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element4));
        netconfrequest4.setMessageId("1");                       
        TimingLogger.withStartAndFinish(() -> m_dummyRpcInputBinary.validate(m_rpcConstraintParser, netconfrequest4));

        String rpcRequest5 = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <whenMust:testRpcInputBoolean xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + " <whenMust:container1>" 
                + " <whenMust:innerContainer>"
                + " <whenMust:boolean-function-leaf>test23</whenMust:boolean-function-leaf>"
                + " </whenMust:innerContainer>" 
                + " </whenMust:container1>" 
                + " </whenMust:testRpcInputBoolean>"
                + " </rpc>";

        Element element5 = DocumentUtils.stringToDocument(rpcRequest5).getDocumentElement();
        NetconfRpcRequest netconfrequest5 = new NetconfRpcRequest();
        netconfrequest5.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element5));
        netconfrequest5.setMessageId("1");
        try {
            TimingLogger.withStartAndFinish(() -> m_dummyRpcInputBinary.validate(m_rpcConstraintParser, netconfrequest5));
            fail();
            
        } catch (Exception e) {
            assertTrue(e instanceof RpcValidationException);
            ValidationException cause = (ValidationException) e.getCause();
            assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, cause.getRpcErrors().get(0).getErrorTag());
            assertTrue(e.getMessage().contains("Violate must constraints: boolean(../string1)"));
        }
        
        String rpcRequest6 = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <whenMust:testRpcInputBoolean xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + " <whenMust:container1>" 
                + " <whenMust:innerContainer>"
                + " <whenMust:boolean-function-zero-number-arg>hello</whenMust:boolean-function-zero-number-arg>"
                + " </whenMust:innerContainer>" 
                + " </whenMust:container1>" 
                + " </whenMust:testRpcInputBoolean>"
                + " </rpc>";

        Element element6 = DocumentUtils.stringToDocument(rpcRequest6).getDocumentElement();
        NetconfRpcRequest netconfrequest6 = new NetconfRpcRequest();
        netconfrequest6.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element6));
        netconfrequest6.setMessageId("1");
        try {
            TimingLogger.withStartAndFinish(() -> m_dummyRpcInputBinary.validate(m_rpcConstraintParser, netconfrequest6));
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof RpcValidationException);
            ValidationException cause = (ValidationException) e.getCause();
            assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, cause.getRpcErrors().get(0).getErrorTag());
            assertTrue(e.getMessage().contains("Violate must constraints: boolean(0)"));
        }
        
        String rpcRequest7 = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <whenMust:testRpcInputBoolean xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + " <whenMust:container1>"
                + " <whenMust:innerContainer>"
                + " <whenMust:string1></whenMust:string1>"
                + " <whenMust:boolean-function-leaf>test</whenMust:boolean-function-leaf>" 
                + " </whenMust:innerContainer>"
                + " </whenMust:container1>" 
                + " </whenMust:testRpcInputBoolean>" 
                + " </rpc>";

        Element element7 = DocumentUtils.stringToDocument(rpcRequest7).getDocumentElement();
        NetconfRpcRequest netconfrequest7 = new NetconfRpcRequest();
        netconfrequest7.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element7));
        netconfrequest7.setMessageId("1");
        TimingLogger.withStartAndFinish(() -> m_dummyRpcInputBinary.validate(m_rpcConstraintParser, netconfrequest7));
        
        String rpcRequest8 = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <whenMust:testRpcInputBoolean xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + " <whenMust:container1>"
                + " <whenMust:innerContainer>"
                + " <whenMust:string2></whenMust:string2>"
                + " <whenMust:boolean-function-or-leaf>test</whenMust:boolean-function-or-leaf>" 
                + " </whenMust:innerContainer>"
                + " </whenMust:container1>" 
                + " </whenMust:testRpcInputBoolean>" 
                + " </rpc>";

        Element element8 = DocumentUtils.stringToDocument(rpcRequest8).getDocumentElement();
        NetconfRpcRequest netconfrequest8 = new NetconfRpcRequest();
        netconfrequest8.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element8));
        netconfrequest8.setMessageId("1");
        TimingLogger.withStartAndFinish(() -> m_dummyRpcInputBinary.validate(m_rpcConstraintParser, netconfrequest8));
    
        String rpcRequest9 = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <whenMust:testRpcInputBoolean xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + " <whenMust:container1>" 
                + " <whenMust:innerContainer>" 
                + " <whenMust:string2></whenMust:string2>"
                + " <whenMust:boolean-function-and-leaf>test</whenMust:boolean-function-and-leaf>"
                + " </whenMust:innerContainer>" 
                + " </whenMust:container1>" 
                + " </whenMust:testRpcInputBoolean>"
                + " </rpc>";

        Element element9 = DocumentUtils.stringToDocument(rpcRequest9).getDocumentElement();
        NetconfRpcRequest netconfrequest9 = new NetconfRpcRequest();
        netconfrequest9.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element9));
        netconfrequest9.setMessageId("1");
        try {
            TimingLogger.withStartAndFinish(() -> m_dummyRpcInputBinary.validate(m_rpcConstraintParser, netconfrequest9));
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof RpcValidationException);
            ValidationException cause = (ValidationException) e.getCause();
            assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, cause.getRpcErrors().get(0).getErrorTag());
            assertTrue(e.getMessage().contains("Violate must constraints: boolean(../string1) and boolean(../string2)"));
        }
        
        String rpcRequest10 = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <whenMust:testRpcInputBoolean xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + " <whenMust:container1>"
                + " <whenMust:innerContainer>"
                + " <whenMust:string1>welcome</whenMust:string1>"
                + " <whenMust:boolean-function-current-leaf>test</whenMust:boolean-function-current-leaf>" 
                + " </whenMust:innerContainer>"
                + " </whenMust:container1>" 
                + " </whenMust:testRpcInputBoolean>" 
                + " </rpc>";

        Element element10 = DocumentUtils.stringToDocument(rpcRequest10).getDocumentElement();
        NetconfRpcRequest netconfrequest10 = new NetconfRpcRequest();
        netconfrequest10.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element10));
        netconfrequest10.setMessageId("1");
        TimingLogger.withStartAndFinish(() -> m_dummyRpcInputBinary.validate(m_rpcConstraintParser, netconfrequest10));
        
        String rpcRequest11 = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <whenMust:testRpcInputBoolean xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + " <whenMust:container1>" 
                + " <whenMust:innerContainer>"
                + " <whenMust:boolean-function-current-leaf>test23</whenMust:boolean-function-current-leaf>"
                + " </whenMust:innerContainer>" 
                + " </whenMust:container1>" 
                + " </whenMust:testRpcInputBoolean>"
                + " </rpc>";

        Element element11 = DocumentUtils.stringToDocument(rpcRequest11).getDocumentElement();
        NetconfRpcRequest netconfrequest11 = new NetconfRpcRequest();
        netconfrequest11.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element11));
        netconfrequest11.setMessageId("1");
        try {
            TimingLogger.withStartAndFinish(() -> m_dummyRpcInputBinary.validate(m_rpcConstraintParser, netconfrequest11));
            fail();
            
        } catch (Exception e) {
            assertTrue(e instanceof RpcValidationException);
            ValidationException cause = (ValidationException) e.getCause();
            assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, cause.getRpcErrors().get(0).getErrorTag());
            assertTrue(e.getMessage().contains("Violate must constraints: boolean(current()/../string1)"));
        }                
    }

    @Test
    public void testRpcStringFunction() throws Exception {
        getModelNode();
        String rpcRequest1 = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <whenMust:testRpcInputString xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + " <whenMust:container1>" 
                + " <whenMust:innerContainer>"
                + "    <whenMust:string1>HE</whenMust:string1>" 
                + "    <whenMust:string2>LLO</whenMust:string2>"
                + "    <whenMust:concat-function-leaf>test</whenMust:concat-function-leaf>"
                + " </whenMust:innerContainer>" 
                + " </whenMust:container1>" 
                + " </whenMust:testRpcInputString>"
                + " </rpc>";

        Element element1 = DocumentUtils.stringToDocument(rpcRequest1).getDocumentElement();
        NetconfRpcRequest netconfrequest1 = new NetconfRpcRequest();
        netconfrequest1.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element1));
        netconfrequest1.setMessageId("1");
        TimingLogger.withStartAndFinish(() -> m_dummyRpcInputString.validate(m_rpcConstraintParser, netconfrequest1));

        String rpcRequest2 = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <whenMust:testRpcInputString xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + " <whenMust:container1>" 
                + " <whenMust:innerContainer>"
                + "    <whenMust:string1>BLACK WHITE</whenMust:string1>"
                + "    <whenMust:contains-function-leaf>test</whenMust:contains-function-leaf>"
                + " </whenMust:innerContainer>" 
                + " </whenMust:container1>" 
                + " </whenMust:testRpcInputString>"
                + " </rpc>";

        Element element2 = DocumentUtils.stringToDocument(rpcRequest2).getDocumentElement();
        NetconfRpcRequest netconfrequest2 = new NetconfRpcRequest();
        netconfrequest2.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element2));
        netconfrequest2.setMessageId("1");
        TimingLogger.withStartAndFinish(() -> m_dummyRpcInputString.validate(m_rpcConstraintParser, netconfrequest2));

        String rpcRequest3 = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <whenMust:testRpcInputString xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + " <whenMust:container1>" 
                + " <whenMust:innerContainer>" 
                + " <whenMust:string1>JACK</whenMust:string1>"
                + " <whenMust:string-length-function-leaf>test10</whenMust:string-length-function-leaf>"
                + " </whenMust:innerContainer>" 
                + " </whenMust:container1>" 
                + " </whenMust:testRpcInputString>"
                + " </rpc>";

        Element element3 = DocumentUtils.stringToDocument(rpcRequest3).getDocumentElement();
        NetconfRpcRequest netconfrequest3 = new NetconfRpcRequest();
        netconfrequest3.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element3));
        netconfrequest3.setMessageId("1");
        TimingLogger.withStartAndFinish(() -> m_dummyRpcInputString.validate(m_rpcConstraintParser, netconfrequest3));

        String rpcRequest4 = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <whenMust:testRpcInputString xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + " <whenMust:container1>" + " <whenMust:innerContainer>"
                + " <whenMust:string1>10-12-00</whenMust:string1>"
                + " <whenMust:substring-before-function-leaf>test</whenMust:substring-before-function-leaf>"
                + " </whenMust:innerContainer>" + " </whenMust:container1>" + " </whenMust:testRpcInputString>"
                + " </rpc>";

        Element element4 = DocumentUtils.stringToDocument(rpcRequest4).getDocumentElement();
        NetconfRpcRequest netconfrequest4 = new NetconfRpcRequest();
        netconfrequest4.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element4));
        netconfrequest4.setMessageId("1");
        TimingLogger.withStartAndFinish(() -> m_dummyRpcInputString.validate(m_rpcConstraintParser, netconfrequest4));

        String rpcRequest5 = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <whenMust:testRpcInputString xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + " <whenMust:container1>" 
                + " <whenMust:innerContainer>" 
                + "    <whenMust:string1>T</whenMust:string1>"
                + "    <whenMust:string2>IC</whenMust:string2>"
                + "    <whenMust:concat-function-leaf>test</whenMust:concat-function-leaf>"
                + " </whenMust:innerContainer>" 
                + " </whenMust:container1>" 
                + " </whenMust:testRpcInputString>"
                + " </rpc>";

        Element element5 = DocumentUtils.stringToDocument(rpcRequest5).getDocumentElement();
        NetconfRpcRequest netconfrequest5 = new NetconfRpcRequest();
        netconfrequest5.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element5));
        netconfrequest5.setMessageId("1");
        try {
            TimingLogger.withStartAndFinish(() -> m_dummyRpcInputString.validate(m_rpcConstraintParser, netconfrequest5));
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof RpcValidationException);
            ValidationException cause = (ValidationException) e.getCause();
            assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, cause.getRpcErrors().get(0).getErrorTag());
            assertTrue(e.getMessage().contains("Violate must constraints: concat(../string1,../string2) = 'HELLO'"));
        }
        String rpcRequest6 = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <whenMust:testRpcInputString xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + " <whenMust:container1>" 
                + " <whenMust:innerContainer>"
                + "    <whenMust:string1>WHITE HORSE</whenMust:string1>"
                + "    <whenMust:contains-function-leaf>test</whenMust:contains-function-leaf>"
                + " </whenMust:innerContainer>" 
                + " </whenMust:container1>" 
                + " </whenMust:testRpcInputString>"
                + " </rpc>";

        Element element6 = DocumentUtils.stringToDocument(rpcRequest6).getDocumentElement();
        NetconfRpcRequest netconfrequest6 = new NetconfRpcRequest();
        netconfrequest6.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element6));
        netconfrequest6.setMessageId("1");
        try {
            TimingLogger.withStartAndFinish(() -> m_dummyRpcInputString.validate(m_rpcConstraintParser, netconfrequest6));
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof RpcValidationException);
            ValidationException cause = (ValidationException) e.getCause();
            assertEquals(NetconfRpcErrorTag.UNKNOWN_ELEMENT, cause.getRpcErrors().get(0).getErrorTag());
            assertTrue(e.getMessage().contains("Violate when constraints: contains(../string1,'BLACK')"));
        }
        String rpcRequest7 = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <whenMust:testRpcInputString xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + " <whenMust:container1>" 
                + " <whenMust:innerContainer>"
                + " <whenMust:string1>TAMZHIL</whenMust:string1>"
                + "    <whenMust:string-length-function-leaf>test</whenMust:string-length-function-leaf>"
                + " </whenMust:innerContainer>" 
                + " </whenMust:container1>" 
                + " </whenMust:testRpcInputString>"
                + " </rpc>";

        Element element7 = DocumentUtils.stringToDocument(rpcRequest7).getDocumentElement();
        NetconfRpcRequest netconfrequest7 = new NetconfRpcRequest();
        netconfrequest7.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element7));
        netconfrequest7.setMessageId("1");
        try {
            TimingLogger.withStartAndFinish(() -> m_dummyRpcInputString.validate(m_rpcConstraintParser, netconfrequest7));
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof RpcValidationException);
            ValidationException cause = (ValidationException) e.getCause();
            assertEquals(NetconfRpcErrorTag.UNKNOWN_ELEMENT, cause.getRpcErrors().get(0).getErrorTag());
            assertTrue(e.getMessage().contains("Violate when constraints: string-length(../string1) = 4"));
        }
        String rpcRequest8 = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <whenMust:testRpcInputString xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + " <whenMust:container1>" 
                + " <whenMust:innerContainer>"
                + " <whenMust:string1>20-12-00</whenMust:string1>"
                + " <whenMust:substring-before-function-leaf>test</whenMust:substring-before-function-leaf>"
                + " </whenMust:innerContainer>" 
                + " </whenMust:container1>" 
                + " </whenMust:testRpcInputString>"
                + " </rpc>";

        Element element8 = DocumentUtils.stringToDocument(rpcRequest8).getDocumentElement();
        NetconfRpcRequest netconfrequest8 = new NetconfRpcRequest();
        netconfrequest8.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element8));
        netconfrequest8.setMessageId("1");
        try {
            TimingLogger.withStartAndFinish(() -> m_dummyRpcInputString.validate(m_rpcConstraintParser, netconfrequest8));
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof RpcValidationException);
            ValidationException cause = (ValidationException) e.getCause();
            assertEquals(NetconfRpcErrorTag.UNKNOWN_ELEMENT, cause.getRpcErrors().get(0).getErrorTag());
            assertTrue(e.getMessage().contains("Violate when constraints: substring-before(../string1,'-') = '10'"));
        }
    }

    @Test
    public void testRpcNamespaceFunction() throws Exception {
        getModelNode();
        String rpcrequest1 = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <whenMust:testRpcInputNamespace xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + " <whenMust:container1>" 
                + " <whenMust:namespaceleaf>Sam</whenMust:namespaceleaf>"
                + " </whenMust:container1>" 
                + " </whenMust:testRpcInputNamespace>" 
                + " </rpc>";

        Element element1 = DocumentUtils.stringToDocument(rpcrequest1).getDocumentElement();
        NetconfRpcRequest netconfrequest1 = new NetconfRpcRequest();
        netconfrequest1.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element1));
        netconfrequest1.setMessageId("1");
        TimingLogger.withStartAndFinish(() -> m_dummyRpcInputNamespace.validate(m_rpcConstraintParser, netconfrequest1));

        String rpcrequest2 = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <whenMust:testRpcInputNamespace xmlns:whenMust=\"urn:org:bbf2:pma:DSExprOnCoreLibraryFunction\">"
                + " <whenMust:container1>" 
                + " <whenMust:namespaceleaf>Sam</whenMust:namespaceleaf>"
                + " </whenMust:container1>" 
                + " </whenMust:testRpcInputNamespace>" 
                + " </rpc>";

        Element element2 = DocumentUtils.stringToDocument(rpcrequest2).getDocumentElement();
        NetconfRpcRequest netconfrequest2 = new NetconfRpcRequest();
        netconfrequest2.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element2));
        netconfrequest2.setMessageId("1");
        try {
            TimingLogger.withStartAndFinish(() -> m_dummyRpcInputNamespace.validate(m_rpcConstraintParser, netconfrequest2));
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof RpcValidationException);
            ValidationException cause = (ValidationException) e.getCause();
            assertEquals(NetconfRpcErrorTag.UNKNOWN_ELEMENT, cause.getRpcErrors().get(0).getErrorTag());
            assertTrue(e.getMessage().contains("An unexpected element 'testRpcInputNamespace' is present"));
        }
        
        String rpcrequest3 = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <whenMust:testRpcInputNamespace xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + " <whenMust:container2>" 
                + " <whenMust:namespacenestedParent>Sam</whenMust:namespacenestedParent>"
                + " </whenMust:container2>" 
                + " </whenMust:testRpcInputNamespace>" 
                + " </rpc>";

        Element element3 = DocumentUtils.stringToDocument(rpcrequest3).getDocumentElement();
        NetconfRpcRequest netconfrequest3 = new NetconfRpcRequest();
        netconfrequest3.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element3));
        netconfrequest3.setMessageId("1");
        TimingLogger.withStartAndFinish(() -> m_dummyRpcInputNamespace.validate(m_rpcConstraintParser, netconfrequest3));
        
        String rpcrequest4 = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <whenMust:testRpcInputNamespace xmlns:whenMust=\"urn:org:bbf2:pma:DSExprOnCoreLibraryFunction\">"
                + " <whenMust:container2>" 
                + " <whenMust:namespacenestedParent>Sam</whenMust:namespacenestedParent>"
                + " </whenMust:container2>" 
                + " </whenMust:testRpcInputNamespace>" 
                + " </rpc>";

        Element element4 = DocumentUtils.stringToDocument(rpcrequest4).getDocumentElement();
        NetconfRpcRequest netconfrequest4 = new NetconfRpcRequest();
        netconfrequest4.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element4));
        netconfrequest4.setMessageId("1");

        try {
            TimingLogger.withStartAndFinish(() -> m_dummyRpcInputNamespace.validate(m_rpcConstraintParser, netconfrequest4));
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof RpcValidationException);
            ValidationException cause = (ValidationException) e.getCause();
            assertEquals(NetconfRpcErrorTag.UNKNOWN_ELEMENT, cause.getRpcErrors().get(0).getErrorTag());
            assertTrue(e.getMessage().contains("An unexpected element 'testRpcInputNamespace' is present"));
        }
    }        
    
    @Test
    public void testRpcInputLocalName() throws Exception {
        getModelNode();
        String xmlPath1 = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <whenMust:testRpcInputLocalName xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + " <whenMust:container1>"
                + " <whenMust:localNameWhenRefersParent>Sam</whenMust:localNameWhenRefersParent>"
                + " </whenMust:container1>" 
                + " </whenMust:testRpcInputLocalName>" 
                + " </rpc>";

        Element element1 = DocumentUtils.stringToDocument(xmlPath1).getDocumentElement();
        NetconfRpcRequest netconfrequest1 = new NetconfRpcRequest();
        netconfrequest1.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element1));
        netconfrequest1.setMessageId("1");
        TimingLogger.withStartAndFinish(() -> m_dummyRpcInputLocalName.validate(m_rpcConstraintParser, netconfrequest1));

        String xmlPath2 = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <whenMust:testRpcInputLocalName xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + " <whenMust:container1>"
                + "   <whenMust:localNameMustWithNoArg>test</whenMust:localNameMustWithNoArg>"
                + " </whenMust:container1>" 
                + " </whenMust:testRpcInputLocalName>" 
                + " </rpc>";

        Element element2 = DocumentUtils.stringToDocument(xmlPath2).getDocumentElement();
        NetconfRpcRequest netconfrequest2 = new NetconfRpcRequest();
        netconfrequest2.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element2));
        netconfrequest2.setMessageId("1");
        TimingLogger.withStartAndFinish(() -> m_dummyRpcInputLocalName.validate(m_rpcConstraintParser, netconfrequest2));

        String xmlPath3 = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <whenMust:testRpcInputLocalName xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + " <whenMust:container1>"
                + "   <whenMust:localNameWhenRefersDummyLeaf>test</whenMust:localNameWhenRefersDummyLeaf>"
                + " </whenMust:container1>" 
                + " </whenMust:testRpcInputLocalName>" 
                + " </rpc>";

        Element element3 = DocumentUtils.stringToDocument(xmlPath3).getDocumentElement();
        NetconfRpcRequest netconfrequest3 = new NetconfRpcRequest();
        netconfrequest3.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element3));
        netconfrequest3.setMessageId("1");
        try {
            TimingLogger.withStartAndFinish(() -> m_dummyRpcInputLocalName.validate(m_rpcConstraintParser, netconfrequest3));
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof RpcValidationException);
            ValidationException cause = (ValidationException) e.getCause();
            assertEquals(NetconfRpcErrorTag.UNKNOWN_ELEMENT, cause.getRpcErrors().get(0).getErrorTag());
            assertTrue(e.getMessage().contains("Violate when constraints: local-name(../localnameleaf) = 'localnameleaf'"));
        }

        String xmlPath4 = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <whenMust:testRpcInputLocalName xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + " <whenMust:container1>" 
                + "   <whenMust:localnameleaf>test</whenMust:localnameleaf>"
                + "   <whenMust:localNameWhenRefersDummyLeaf>test</whenMust:localNameWhenRefersDummyLeaf>"
                + " </whenMust:container1>" 
                + " </whenMust:testRpcInputLocalName>" 
                + " </rpc>";

        Element element4 = DocumentUtils.stringToDocument(xmlPath4).getDocumentElement();
        NetconfRpcRequest netconfrequest4 = new NetconfRpcRequest();
        netconfrequest4.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element4));
        netconfrequest4.setMessageId("1");
        TimingLogger.withStartAndFinish(() -> m_dummyRpcInputLocalName.validate(m_rpcConstraintParser, netconfrequest4));

        String xmlPath5 = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <whenMust:testRpcInputLocalName xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + " <whenMust:container1>" 
                + "   <whenMust:localnameleaf/>"
                + "   <whenMust:localNameWhenRefersDummyLeaf>test</whenMust:localNameWhenRefersDummyLeaf>"
                + " </whenMust:container1>" 
                + " </whenMust:testRpcInputLocalName>" 
                + " </rpc>";

        Element element5 = DocumentUtils.stringToDocument(xmlPath5).getDocumentElement();
        NetconfRpcRequest netconfrequest5 = new NetconfRpcRequest();
        netconfrequest5.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element5));
        netconfrequest5.setMessageId("1");
        TimingLogger.withStartAndFinish(() -> m_dummyRpcInputLocalName.validate(m_rpcConstraintParser, netconfrequest5));

    }

    @Test
    public void testRpcOutputNumberFunction() throws Exception {
        getModelNode();
        String rpcResponse1 = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <whenMust:container1 xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + " <whenMust:innerContainer>" 
                + " <whenMust:innerLeaf>45</whenMust:innerLeaf>"
                + " <whenMust:number-function-leaf>50</whenMust:number-function-leaf>" 
                + " </whenMust:innerContainer>"
                + " </whenMust:container1>" 
                + " </rpc-reply>";

        Element element1 = DocumentUtils.stringToDocument(rpcResponse1).getDocumentElement();
        NetconfRpcResponse netconfResponse1 = new NetconfRpcResponse();
        netconfResponse1.setMessageId("1");
        TestUtil.addOutputElements(element1, netconfResponse1);
        netconfResponse1.setRpcName(new RpcName("urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction", "testRpcOutputMustNumber"));
        m_dummyRpcOutputMustNumber.validate(m_rpcConstraintParser, netconfResponse1);
        assertTrue(netconfResponse1.getErrors().isEmpty());

        String rpcResponse2 = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <whenMust:container1 xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + " <whenMust:innerContainer>" 
                + " <whenMust:innerLeaf>48</whenMust:innerLeaf>"
                + " <whenMust:number-function-leaf>55</whenMust:number-function-leaf>" 
                + " </whenMust:innerContainer>"
                + " </whenMust:container1>" 
                + " </rpc-reply>";

        Element element2 = DocumentUtils.stringToDocument(rpcResponse2).getDocumentElement();
        NetconfRpcResponse netconfResponse2 = new NetconfRpcResponse();
        netconfResponse2.setMessageId("1");
        TestUtil.addOutputElements(element2, netconfResponse2);
        netconfResponse2.setRpcName(new RpcName("urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction", "testRpcOutputMustNumber"));
        m_dummyRpcOutputMustNumber.validate(m_rpcConstraintParser, netconfResponse2);
        assertEquals(NetconfRpcErrorTag.UNKNOWN_ELEMENT, netconfResponse2.getErrors().get(0).getErrorTag());
        assertTrue(netconfResponse2.getErrors().get(0).getErrorMessage().contains("Violate when constraints: number(../innerLeaf) = 45"));
        assertFalse(netconfResponse2.getErrors().isEmpty());

        String rpcResponse3 = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <whenMust:container1 xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + " <whenMust:innerContainer>" 
                + " <whenMust:innerLeaf>45.2</whenMust:innerLeaf>"
                + " <whenMust:floor-function-leaf>test</whenMust:floor-function-leaf>" 
                + " </whenMust:innerContainer>"
                + " </whenMust:container1>" 
                + " </rpc-reply>";

        Element element3 = DocumentUtils.stringToDocument(rpcResponse3).getDocumentElement();
        NetconfRpcResponse netconfResponse3 = new NetconfRpcResponse();
        netconfResponse3.setMessageId("1");
        TestUtil.addOutputElements(element3, netconfResponse3);
        netconfResponse3.setRpcName(new RpcName("urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction", "testRpcOutputMustNumber"));
        m_dummyRpcOutputMustNumber.validate(m_rpcConstraintParser, netconfResponse3);
        assertTrue(netconfResponse3.getErrors().isEmpty());

        String rpcResponse4 = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <whenMust:container1 xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + " <whenMust:innerContainer>" 
                + " <whenMust:innerLeaf>43.2</whenMust:innerLeaf>"
                + " <whenMust:floor-function-leaf>test</whenMust:floor-function-leaf>" 
                + " </whenMust:innerContainer>"
                + " </whenMust:container1>" 
                + " </rpc-reply>";

        Element element4 = DocumentUtils.stringToDocument(rpcResponse4).getDocumentElement();
        NetconfRpcResponse netconfResponse4 = new NetconfRpcResponse();
        netconfResponse4.setMessageId("1");
        TestUtil.addOutputElements(element4, netconfResponse4);
        netconfResponse4.setRpcName(new RpcName("urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction", "testRpcOutputMustNumber"));
        m_dummyRpcOutputMustNumber.validate(m_rpcConstraintParser, netconfResponse4);
        assertEquals(NetconfRpcErrorTag.UNKNOWN_ELEMENT, netconfResponse4.getErrors().get(0).getErrorTag());
        assertTrue(netconfResponse4.getErrors().get(0).getErrorMessage().contains("Violate when constraints: floor(../innerLeaf) = 45"));
        assertFalse(netconfResponse4.getErrors().isEmpty());

        String rpcResponse5 = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <whenMust:container1 xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + " <whenMust:innerContainer>" 
                + " <whenMust:innerLeaf>44.2</whenMust:innerLeaf>"
                + " <whenMust:ceiling-function-leaf>test</whenMust:ceiling-function-leaf>"
                + " </whenMust:innerContainer>" 
                + " </whenMust:container1>" 
                + " </rpc-reply>";

        Element element5 = DocumentUtils.stringToDocument(rpcResponse5).getDocumentElement();
        NetconfRpcResponse netconfResponse5 = new NetconfRpcResponse();
        netconfResponse5.setMessageId("1");
        TestUtil.addOutputElements(element5, netconfResponse5);
        netconfResponse5.setRpcName(new RpcName("urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction", "testRpcOutputMustNumber"));
        m_dummyRpcOutputMustNumber.validate(m_rpcConstraintParser, netconfResponse5);
        assertTrue(netconfResponse5.getErrors().isEmpty());

        String rpcResponse6 = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <whenMust:container1 xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + " <whenMust:innerContainer>" 
                + " <whenMust:innerLeaf>45.2</whenMust:innerLeaf>"
                + " <whenMust:ceiling-function-leaf>test</whenMust:ceiling-function-leaf>"
                + " </whenMust:innerContainer>" 
                + " </whenMust:container1>" 
                + " </rpc-reply>";

        Element element6 = DocumentUtils.stringToDocument(rpcResponse6).getDocumentElement();
        NetconfRpcResponse netconfResponse6 = new NetconfRpcResponse();
        netconfResponse6.setMessageId("1");
        TestUtil.addOutputElements(element6, netconfResponse6);
        netconfResponse6.setRpcName(new RpcName("urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction", "testRpcOutputMustNumber"));
        m_dummyRpcOutputMustNumber.validate(m_rpcConstraintParser, netconfResponse6);
        assertEquals(NetconfRpcErrorTag.UNKNOWN_ELEMENT, netconfResponse6.getErrors().get(0).getErrorTag());
        assertTrue(netconfResponse6.getErrors().get(0).getErrorMessage().contains("Violate when constraints: ceiling(../innerLeaf) = 45"));
        assertFalse(netconfResponse6.getErrors().isEmpty());

        String rpcResponse7 = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <whenMust:container1 xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + " <whenMust:innerContainer>" 
                + " <whenMust:innerLeaf>45.2</whenMust:innerLeaf>"
                + " <whenMust:round-function-leaf>test</whenMust:round-function-leaf>" 
                + " </whenMust:innerContainer>"
                + " </whenMust:container1>" 
                + " </rpc-reply>";

        Element element7 = DocumentUtils.stringToDocument(rpcResponse7).getDocumentElement();
        NetconfRpcResponse netconfResponse7 = new NetconfRpcResponse();
        netconfResponse7.setMessageId("1");
        TestUtil.addOutputElements(element7, netconfResponse7);
        netconfResponse7.setRpcName(new RpcName("urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction", "testRpcOutputMustNumber"));
        m_dummyRpcOutputMustNumber.validate(m_rpcConstraintParser, netconfResponse7);
        assertTrue(netconfResponse7.getErrors().isEmpty());

        String rpcResponse8 = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <whenMust:container1 xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + " <whenMust:innerContainer>" 
                + " <whenMust:innerLeaf>46.2</whenMust:innerLeaf>"
                + " <whenMust:round-function-leaf>test</whenMust:round-function-leaf>" 
                + " </whenMust:innerContainer>"
                + " </whenMust:container1>" 
                + " </rpc-reply>";

        Element element8 = DocumentUtils.stringToDocument(rpcResponse8).getDocumentElement();
        NetconfRpcResponse netconfResponse8 = new NetconfRpcResponse();
        netconfResponse8.setMessageId("1");
        TestUtil.addOutputElements(element8, netconfResponse8);
        netconfResponse8.setRpcName(new RpcName("urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction", "testRpcOutputMustNumber"));
        m_dummyRpcOutputMustNumber.validate(m_rpcConstraintParser, netconfResponse8);
        assertEquals(NetconfRpcErrorTag.UNKNOWN_ELEMENT, netconfResponse8.getErrors().get(0).getErrorTag());
        assertTrue(netconfResponse8.getErrors().get(0).getErrorMessage().contains("Violate when constraints: round(../innerLeaf) = 45"));
        assertFalse(netconfResponse8.getErrors().isEmpty());
    }

    
    public void testRpcOutputBooleanFunction() throws Exception {
        getModelNode();
        String rpcResponse1 = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <whenMust:container1 xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + " <whenMust:innerContainer>" 
                + " <whenMust:string1>test</whenMust:string1>"
                + " <whenMust:boolean-function-non-empty-string-arg>test10</whenMust:boolean-function-non-empty-string-arg>"
                + " </whenMust:innerContainer>" 
                + " </whenMust:container1>" 
                + " </rpc-reply>";

        Element element1 = DocumentUtils.stringToDocument(rpcResponse1).getDocumentElement();
        NetconfRpcResponse netconfResponse1 = new NetconfRpcResponse();
        netconfResponse1.setMessageId("1");
        TestUtil.addOutputElements(element1, netconfResponse1);
        netconfResponse1.setRpcName(new RpcName("urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction", "testRpcOutputBoolean"));
        m_dummyRpcOutputBinary.validate(m_rpcConstraintParser, netconfResponse1);
        assertTrue(netconfResponse1.getErrors().isEmpty());

        String rpcResponse2 = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <whenMust:container1 xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + " <whenMust:innerContainer>" 
                + " <whenMust:true-function-leaf>test11</whenMust:true-function-leaf>"
                + " </whenMust:innerContainer>" 
                + " </whenMust:container1>" 
                + " </rpc-reply>";

        Element element2 = DocumentUtils.stringToDocument(rpcResponse2).getDocumentElement();
        NetconfRpcResponse netconfResponse2 = new NetconfRpcResponse();
        netconfResponse2.setMessageId("1");
        TestUtil.addOutputElements(element2, netconfResponse2);
        netconfResponse2.setRpcName(new RpcName("urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction", "testRpcOutputBoolean"));
        m_dummyRpcOutputBinary.validate(m_rpcConstraintParser, netconfResponse2);
        assertTrue(netconfResponse2.getErrors().isEmpty());

        String rpcResponse3 = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <whenMust:container1 xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + " <whenMust:innerContainer>" 
                + " <whenMust:false-function-leaf>test10</whenMust:false-function-leaf>"
                + " </whenMust:innerContainer>" 
                + " </whenMust:container1>" 
                + " </rpc-reply>";

        Element element3 = DocumentUtils.stringToDocument(rpcResponse3).getDocumentElement();
        NetconfRpcResponse netconfResponse3 = new NetconfRpcResponse();
        netconfResponse3.setMessageId("1");
        TestUtil.addOutputElements(element3, netconfResponse3);
        netconfResponse3.setRpcName(new RpcName("urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction", "testRpcOutputBoolean"));
        m_dummyRpcOutputBinary.validate(m_rpcConstraintParser, netconfResponse3);
        assertTrue(netconfResponse3.getErrors().isEmpty());

        String rpcResponse4 = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <whenMust:container1 xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + " <whenMust:innerContainer>" 
                + " <whenMust:not-function-leaf>test56</whenMust:not-function-leaf>"
                + " </whenMust:innerContainer>" 
                + " </whenMust:container1>" 
                + " </rpc-reply>";

        Element element4 = DocumentUtils.stringToDocument(rpcResponse4).getDocumentElement();
        NetconfRpcResponse netconfResponse4 = new NetconfRpcResponse();
        netconfResponse4.setMessageId("1");
        TestUtil.addOutputElements(element4, netconfResponse4);
        netconfResponse4.setRpcName(new RpcName("urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction", "testRpcOutputBoolean"));
        m_dummyRpcOutputBinary.validate(m_rpcConstraintParser, netconfResponse4);
        assertTrue(netconfResponse4.getErrors().isEmpty());

        String rpcResponse5 = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <whenMust:container1 xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + " <whenMust:innerContainer>"
                + " <whenMust:boolean-function-leaf>test23</whenMust:boolean-function-leaf>"
                + " </whenMust:innerContainer>" 
                + " </whenMust:container1>" 
                + " </rpc-reply>";

        Element element5 = DocumentUtils.stringToDocument(rpcResponse5).getDocumentElement();
        NetconfRpcResponse netconfResponse5 = new NetconfRpcResponse();
        netconfResponse5.setMessageId("1");
        TestUtil.addOutputElements(element5, netconfResponse5);
        netconfResponse5.setRpcName(new RpcName("urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction", "testRpcOutputBoolean"));
        m_dummyRpcOutputBinary.validate(m_rpcConstraintParser, netconfResponse5);
        assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, netconfResponse5.getErrors().get(0).getErrorTag());
        assertTrue(netconfResponse5.getErrors().get(0).getErrorMessage().contains("Violate must constraints: boolean(../string1)"));
        assertFalse(netconfResponse5.getErrors().isEmpty());

        String rpcResponse6 = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <whenMust:container1 xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + " <whenMust:innerContainer>"
                + " <whenMust:boolean-function-zero-number-arg>hello</whenMust:boolean-function-zero-number-arg>"
                + " </whenMust:innerContainer>" 
                + " </whenMust:container1>" 
                + " </rpc-reply>";

        Element element6 = DocumentUtils.stringToDocument(rpcResponse6).getDocumentElement();
        NetconfRpcResponse netconfResponse6 = new NetconfRpcResponse();
        netconfResponse6.setMessageId("1");
        TestUtil.addOutputElements(element6, netconfResponse6);
        netconfResponse6.setRpcName(new RpcName("urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction", "testRpcOutputBoolean"));
        m_dummyRpcOutputBinary.validate(m_rpcConstraintParser, netconfResponse6);
        assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, netconfResponse5.getErrors().get(0).getErrorTag());
        assertTrue(netconfResponse6.getErrors().get(0).getErrorMessage().contains("Violate must constraints: boolean(0)"));
        assertFalse(netconfResponse6.getErrors().isEmpty());

        String rpcResponse7 = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <whenMust:container1 xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + " <whenMust:innerContainer>" 
                + " <whenMust:string1></whenMust:string1>"
                + " <whenMust:boolean-function-leaf>test</whenMust:boolean-function-leaf>"
                + " </whenMust:innerContainer>" 
                + " </whenMust:container1>" 
                + " </rpc-reply>";

        Element element7 = DocumentUtils.stringToDocument(rpcResponse7).getDocumentElement();
        NetconfRpcResponse netconfResponse7 = new NetconfRpcResponse();
        netconfResponse7.setMessageId("1");
        TestUtil.addOutputElements(element7, netconfResponse7);
        netconfResponse7.setRpcName(new RpcName("urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction", "testRpcOutputBoolean"));
        m_dummyRpcOutputBinary.validate(m_rpcConstraintParser, netconfResponse7);
        assertTrue(netconfResponse7.getErrors().isEmpty());

        String rpcResponse8 = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <whenMust:container1 xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + " <whenMust:innerContainer>" 
                + " <whenMust:string2></whenMust:string2>"
                + " <whenMust:boolean-function-or-leaf>test</whenMust:boolean-function-or-leaf>"
                + " </whenMust:innerContainer>" 
                + " </whenMust:container1>" 
                + " </rpc-reply>";

        Element element8 = DocumentUtils.stringToDocument(rpcResponse8).getDocumentElement();
        NetconfRpcResponse netconfResponse8 = new NetconfRpcResponse();
        netconfResponse8.setMessageId("1");
        TestUtil.addOutputElements(element8, netconfResponse8);
        netconfResponse8.setRpcName(new RpcName("urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction", "testRpcOutputBoolean"));
        m_dummyRpcOutputBinary.validate(m_rpcConstraintParser, netconfResponse8);
        assertTrue(netconfResponse8.getErrors().isEmpty());

        String rpcResponse9 = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <whenMust:container1 xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + " <whenMust:innerContainer>" 
                + " <whenMust:string2></whenMust:string2>"
                + " <whenMust:boolean-function-and-leaf>test</whenMust:boolean-function-and-leaf>"
                + " </whenMust:innerContainer>" 
                + " </whenMust:container1>" 
                + " </rpc-reply>";

        Element element9 = DocumentUtils.stringToDocument(rpcResponse9).getDocumentElement();
        NetconfRpcResponse netconfResponse9 = new NetconfRpcResponse();
        netconfResponse9.setMessageId("1");
        TestUtil.addOutputElements(element9, netconfResponse9);
        netconfResponse9.setRpcName(new RpcName("urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction", "testRpcOutputBoolean"));
        m_dummyRpcOutputBinary.validate(m_rpcConstraintParser, netconfResponse9);
        assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, netconfResponse5.getErrors().get(0).getErrorTag());
        assertTrue(netconfResponse9.getErrors().get(0).getErrorMessage().contains("Violate must constraints: boolean(../string1) and boolean(../string2)"));
        assertFalse(netconfResponse9.getErrors().isEmpty());
    }

    @Test
    public void testRpcOutputStringFunction() throws Exception {
        getModelNode();
        String rpcResponse1 = "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <whenMust:container1 xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + " <whenMust:innerContainer>" 
                + "    <whenMust:string1>HE</whenMust:string1>"
                + "    <whenMust:string2>LLO</whenMust:string2>"
                + "    <whenMust:concat-function-leaf>test</whenMust:concat-function-leaf>"
                + " </whenMust:innerContainer>" 
                + " </whenMust:container1>" 
                + " </rpc-reply>";

        Element element1 = DocumentUtils.stringToDocument(rpcResponse1).getDocumentElement();
        NetconfRpcResponse netconfResponse1 = new NetconfRpcResponse();
        netconfResponse1.setMessageId("1");
        TestUtil.addOutputElements(element1, netconfResponse1);
        netconfResponse1.setRpcName(new RpcName("urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction", "testRpcOutputString"));
        m_dummyRpcOutputString.validate(m_rpcConstraintParser, netconfResponse1);
        assertTrue(netconfResponse1.getErrors().isEmpty());

        String rpcResponse2 = "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <whenMust:container1 xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + " <whenMust:innerContainer>" 
                + "    <whenMust:string1>BLACK WHITE</whenMust:string1>"
                + "    <whenMust:contains-function-leaf>test</whenMust:contains-function-leaf>"
                + " </whenMust:innerContainer>" 
                + " </whenMust:container1>" 
                + " </rpc-reply>";

        Element element2 = DocumentUtils.stringToDocument(rpcResponse2).getDocumentElement();
        NetconfRpcResponse netconfResponse2 = new NetconfRpcResponse();
        netconfResponse2.setMessageId("1");
        TestUtil.addOutputElements(element2, netconfResponse2);
        netconfResponse2.setRpcName(new RpcName("urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction", "testRpcOutputString"));
        m_dummyRpcOutputString.validate(m_rpcConstraintParser, netconfResponse2);
        assertTrue(netconfResponse2.getErrors().isEmpty());

        String rpcResponse3 = "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <whenMust:container1 xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + " <whenMust:innerContainer>" 
                + " <whenMust:string1>JACK</whenMust:string1>"
                + " <whenMust:string-length-function-leaf>test10</whenMust:string-length-function-leaf>"
                + " </whenMust:innerContainer>" 
                + " </whenMust:container1>" 
                + " </rpc-reply>";

        Element element3 = DocumentUtils.stringToDocument(rpcResponse3).getDocumentElement();
        NetconfRpcResponse netconfResponse3 = new NetconfRpcResponse();
        netconfResponse3.setMessageId("1");
        TestUtil.addOutputElements(element3, netconfResponse3);
        netconfResponse3.setRpcName(new RpcName("urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction", "testRpcOutputString"));
        m_dummyRpcOutputString.validate(m_rpcConstraintParser, netconfResponse3);
        assertTrue(netconfResponse3.getErrors().isEmpty());

        String rpcResponse4 = "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <whenMust:container1 xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + " <whenMust:innerContainer>" 
                + " <whenMust:string1>10-12-00</whenMust:string1>"
                + " <whenMust:substring-before-function-leaf>test</whenMust:substring-before-function-leaf>"
                + " </whenMust:innerContainer>" 
                + " </whenMust:container1>" 
                + " </rpc-reply>";

        Element element4 = DocumentUtils.stringToDocument(rpcResponse4).getDocumentElement();
        NetconfRpcResponse netconfResponse4 = new NetconfRpcResponse();
        netconfResponse4.setMessageId("1");
        TestUtil.addOutputElements(element4, netconfResponse4);
        netconfResponse4.setRpcName(new RpcName("urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction", "testRpcOutputString"));
        m_dummyRpcOutputString.validate(m_rpcConstraintParser, netconfResponse4);
        assertTrue(netconfResponse4.getErrors().isEmpty());

        String rpcResponse5 = "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <whenMust:container1 xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + " <whenMust:innerContainer>" 
                + "    <whenMust:string1>T</whenMust:string1>"
                + "    <whenMust:string2>IC</whenMust:string2>"
                + "    <whenMust:concat-function-leaf>test</whenMust:concat-function-leaf>"
                + " </whenMust:innerContainer>" 
                + " </whenMust:container1>" 
                + " </rpc-reply>";

        Element element5 = DocumentUtils.stringToDocument(rpcResponse5).getDocumentElement();
        NetconfRpcResponse netconfResponse5 = new NetconfRpcResponse();
        netconfResponse5.setMessageId("1");
        TestUtil.addOutputElements(element5, netconfResponse5);
        netconfResponse5.setRpcName(new RpcName("urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction", "testRpcOutputString"));
        m_dummyRpcOutputString.validate(m_rpcConstraintParser, netconfResponse5);
        assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, netconfResponse5.getErrors().get(0).getErrorTag());
        assertTrue(netconfResponse5.getErrors().get(0).getErrorMessage().contains("Violate must constraints: concat(../string1,../string2) = 'HELLO'"));
        assertFalse(netconfResponse5.getErrors().isEmpty());

        String rpcResponse6 = "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <whenMust:container1 xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + " <whenMust:innerContainer>" 
                + "    <whenMust:string1>WHITE HORSE</whenMust:string1>"
                + "    <whenMust:contains-function-leaf>test</whenMust:contains-function-leaf>"
                + " </whenMust:innerContainer>" 
                + " </whenMust:container1>" 
                + " </rpc-reply>";

        Element element6 = DocumentUtils.stringToDocument(rpcResponse6).getDocumentElement();
        NetconfRpcResponse netconfResponse6 = new NetconfRpcResponse();
        netconfResponse6.setMessageId("1");
        TestUtil.addOutputElements(element6, netconfResponse6);
        netconfResponse6.setRpcName(new RpcName("urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction", "testRpcOutputString"));
        m_dummyRpcOutputString.validate(m_rpcConstraintParser, netconfResponse6);
        assertEquals(NetconfRpcErrorTag.UNKNOWN_ELEMENT, netconfResponse6.getErrors().get(0).getErrorTag());
        assertTrue(netconfResponse6.getErrors().get(0).getErrorMessage().contains("Violate when constraints: contains(../string1,'BLACK')"));
        assertFalse(netconfResponse6.getErrors().isEmpty());

        String rpcResponse7 = "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <whenMust:container1 xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + " <whenMust:innerContainer>" 
                + " <whenMust:string1>TAMZHIL</whenMust:string1>"
                + "    <whenMust:string-length-function-leaf>test</whenMust:string-length-function-leaf>"
                + " </whenMust:innerContainer>" 
                + " </whenMust:container1>" 
                + " </rpc-reply>";

        Element element7 = DocumentUtils.stringToDocument(rpcResponse7).getDocumentElement();
        NetconfRpcResponse netconfResponse7 = new NetconfRpcResponse();
        netconfResponse7.setMessageId("1");
        TestUtil.addOutputElements(element7, netconfResponse7);
        netconfResponse7.setRpcName(new RpcName("urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction", "testRpcOutputString"));
        m_dummyRpcOutputString.validate(m_rpcConstraintParser, netconfResponse7);
        assertEquals(NetconfRpcErrorTag.UNKNOWN_ELEMENT, netconfResponse6.getErrors().get(0).getErrorTag());
        assertTrue(netconfResponse7.getErrors().get(0).getErrorMessage().contains("Violate when constraints: string-length(../string1) = 4"));
        assertFalse(netconfResponse7.getErrors().isEmpty());

        String rpcResponse8 = "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <whenMust:container1 xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + " <whenMust:innerContainer>" 
                + " <whenMust:string1>20-12-00</whenMust:string1>"
                + " <whenMust:substring-before-function-leaf>test</whenMust:substring-before-function-leaf>"
                + " </whenMust:innerContainer>" 
                + " </whenMust:container1>" 
                + " </rpc-reply>";

        Element element8 = DocumentUtils.stringToDocument(rpcResponse8).getDocumentElement();
        NetconfRpcResponse netconfResponse8 = new NetconfRpcResponse();
        netconfResponse8.setMessageId("1");
        TestUtil.addOutputElements(element8, netconfResponse8);
        netconfResponse8.setRpcName(new RpcName("urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction", "testRpcOutputString"));
        m_dummyRpcOutputString.validate(m_rpcConstraintParser, netconfResponse8);
        assertEquals(NetconfRpcErrorTag.UNKNOWN_ELEMENT, netconfResponse8.getErrors().get(0).getErrorTag());
        assertTrue(netconfResponse8.getErrors().get(0).getErrorMessage().contains("Violate when constraints: substring-before(../string1,'-') = '10'"));
        assertFalse(netconfResponse8.getErrors().isEmpty());
    }

    @Test
    public void testRpcOutputNamespaceFunction() throws Exception {
        getModelNode();
        String rpcResponse1 = "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <whenMust:container1 xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + " <whenMust:namespaceleaf>Sam</whenMust:namespaceleaf>" 
                + " </whenMust:container1>" 
                + " </rpc-reply>";

        Element element1 = DocumentUtils.stringToDocument(rpcResponse1).getDocumentElement();
        NetconfRpcResponse netconfResponse1 = new NetconfRpcResponse();
        netconfResponse1.setMessageId("1");
        TestUtil.addOutputElements(element1, netconfResponse1);
        netconfResponse1.setRpcName(new RpcName("urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction", "testRpcOutputNamespace"));
        m_dummyRpcOutputNamespace.validate(m_rpcConstraintParser, netconfResponse1);
        assertTrue(netconfResponse1.getErrors().isEmpty());

        String rpcResponse2 = "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <whenMust:container1 xmlns:whenMust=\"urn:org:bbf2:pma:DSExprOnCoreLibraryFunction\">"
                + " <whenMust:namespaceleaf>Sam</whenMust:namespaceleaf>" 
                + " </whenMust:container1>" 
                + " </rpc-reply>";

        Element element2 = DocumentUtils.stringToDocument(rpcResponse2).getDocumentElement();
        NetconfRpcResponse netconfResponse2 = new NetconfRpcResponse();
        netconfResponse2.setMessageId("1");
        TestUtil.addOutputElements(element2, netconfResponse2);
        netconfResponse2.setRpcName(new RpcName("urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction", "testRpcOutputNamespace"));
        m_dummyRpcOutputNamespace.validate(m_rpcConstraintParser, netconfResponse2);
        assertEquals(NetconfRpcErrorTag.UNKNOWN_NAMESPACE, netconfResponse2.getErrors().get(0).getErrorTag());
        assertTrue(netconfResponse2.getErrors().get(0).getErrorMessage().contains("An unexpected namespace 'urn:org:bbf2:pma:DSExprOnCoreLibraryFunction' is present"));
        assertFalse(netconfResponse2.getErrors().isEmpty());

    }

    @Test
    public void testRpcOutputLocalName() throws Exception {
        getModelNode();
        String rpcResponse1 = "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <whenMust:container1 xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + " <whenMust:localNameWhenRefersParent>Sam</whenMust:localNameWhenRefersParent>"
                + " </whenMust:container1>" 
                + " </rpc-reply>";

        Element element1 = DocumentUtils.stringToDocument(rpcResponse1).getDocumentElement();
        NetconfRpcResponse netconfResponse1 = new NetconfRpcResponse();
        netconfResponse1.setMessageId("1");
        TestUtil.addOutputElements(element1, netconfResponse1);
        netconfResponse1.setRpcName(new RpcName("urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction", "testRpcOutputLocalName"));
        m_dummyRpcOutputLocalName.validate(m_rpcConstraintParser, netconfResponse1);
        assertTrue(netconfResponse1.getErrors().isEmpty());

        String rpcResponse2 = "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <whenMust:container1 xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + "   <whenMust:localNameMustWithNoArg>test</whenMust:localNameMustWithNoArg>"
                + " </whenMust:container1>" 
                + " </rpc-reply>";

        Element element2 = DocumentUtils.stringToDocument(rpcResponse2).getDocumentElement();
        NetconfRpcResponse netconfResponse2 = new NetconfRpcResponse();
        netconfResponse2.setMessageId("1");
        TestUtil.addOutputElements(element2, netconfResponse2);
        netconfResponse2.setRpcName(new RpcName("urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction", "testRpcOutputLocalName"));
        m_dummyRpcOutputLocalName.validate(m_rpcConstraintParser, netconfResponse2);
        assertTrue(netconfResponse2.getErrors().isEmpty());

        String rpcResponse3 = "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <whenMust:container1 xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + "   <whenMust:localNameWhenRefersDummyLeaf>test</whenMust:localNameWhenRefersDummyLeaf>"
                + " </whenMust:container1>" 
                + " </rpc-reply>";

        Element element3 = DocumentUtils.stringToDocument(rpcResponse3).getDocumentElement();
        NetconfRpcResponse netconfResponse3 = new NetconfRpcResponse();
        netconfResponse3.setMessageId("1");
        TestUtil.addOutputElements(element3, netconfResponse3);
        netconfResponse3.setRpcName(new RpcName("urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction", "testRpcOutputLocalName"));
        m_dummyRpcOutputLocalName.validate(m_rpcConstraintParser, netconfResponse3);
        assertEquals(NetconfRpcErrorTag.UNKNOWN_ELEMENT, netconfResponse3.getErrors().get(0).getErrorTag());
        assertTrue(netconfResponse3.getErrors().get(0).getErrorMessage().contains("Violate when constraints: local-name(../localnameleaf) = 'localnameleaf'"));
        assertFalse(netconfResponse3.getErrors().isEmpty());

        String rpcResponse4 = "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <whenMust:container1 xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + "   <whenMust:localnameleaf>test</whenMust:localnameleaf>"
                + "   <whenMust:localNameWhenRefersDummyLeaf>test</whenMust:localNameWhenRefersDummyLeaf>"
                + " </whenMust:container1>" 
                + " </rpc-reply>";

        Element element4 = DocumentUtils.stringToDocument(rpcResponse4).getDocumentElement();
        NetconfRpcResponse netconfResponse4 = new NetconfRpcResponse();
        netconfResponse4.setMessageId("1");
        TestUtil.addOutputElements(element4, netconfResponse4);
        netconfResponse4.setRpcName(new RpcName("urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction", "testRpcOutputLocalName"));
        m_dummyRpcOutputLocalName.validate(m_rpcConstraintParser, netconfResponse4);
        assertTrue(netconfResponse4.getErrors().isEmpty());

        String rpcResponse5 = "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <whenMust:container1 xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + "   <whenMust:localnameleaf/>"
                + "   <whenMust:localNameWhenRefersDummyLeaf>test</whenMust:localNameWhenRefersDummyLeaf>"
                + " </whenMust:container1>" 
                + " </rpc-reply>";

        Element element5 = DocumentUtils.stringToDocument(rpcResponse5).getDocumentElement();
        NetconfRpcResponse netconfResponse5 = new NetconfRpcResponse();
        netconfResponse5.setMessageId("1");
        TestUtil.addOutputElements(element5, netconfResponse5);
        netconfResponse5.setRpcName(new RpcName("urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction", "testRpcOutputLocalName"));
        m_dummyRpcOutputLocalName.validate(m_rpcConstraintParser, netconfResponse5);
        assertTrue(netconfResponse5.getErrors().isEmpty());
    }    
}
    
    

    
