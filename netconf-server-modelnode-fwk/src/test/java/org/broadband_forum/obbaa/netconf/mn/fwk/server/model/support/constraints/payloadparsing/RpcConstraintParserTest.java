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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.payloadparsing;

import static org.broadband_forum.obbaa.netconf.mn.fwk.tests.utils.XmlGenerator.EMPTY_STRING;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.setUpUnwrap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.api.NetconfMessage;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcResponse;
import org.broadband_forum.obbaa.netconf.api.messages.RpcName;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.RpcRequestConstraintParser;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.CompositeSubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.TimingLogger;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.LocalSubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.validation.AbstractDataStoreValidatorTest;
import org.broadband_forum.obbaa.netconf.mn.fwk.tests.utils.XmlGenerator;
import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcPayloadConstraintParser;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcRequestHandler;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcValidationException;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@RunWith(RequestScopeJunitRunner.class)
public class RpcConstraintParserTest extends AbstractDataStoreValidatorTest {

    private DummyRpcHandler m_dummyRpcHandler;
    private DummyRpcHandler m_dummyRpcHandler1;
    private DummyRpcHandler m_dummyRpcHandler2;
    private DummyRpcHandler m_dummyRpcHandler3;
    private DummyRpcHandler m_dummyRpcHandler4;
    private DummyRpcHandler m_dummyRpcHandler5;
    private DummyRpcHandler m_dummyRpcHandler6;
    private DummyRpcHandler m_dummyEmptyRpcHandler;
    private DummyRpcHandler m_dummyIfFeatureRpcHandler;
    private DummyRpcHandler m_dummyIfFeatureOutputRpcHandler;
    private DummyRpcHandler m_dummyDefaultValueRpcHandler;
    private DummyRpcHandler m_dummyDefaultValueOutputRpcHandler;
    private DummyRpcHandler m_dummyRematchValueRpcHandler;
    private DummyRpcHandler m_dummyRematchValueOutputRpcHandler;
    private DummyRpcHandler m_configRpcInputHandler;
    private DummyRpcHandler m_configRpcOutputHandler;
    private RpcName m_configOutputRpc;
    private RpcPayloadConstraintParser m_rpcConstraintParser;
    private DummyRpcHandler m_RpcInputHandler;
    private ModelNodeDataStoreManager m_modelNodeDsm;
    private SubSystemRegistry m_subSystemRegistry;
    private DummyRpcHandler m_stateDataRpcHandler;
    private DummyRpcHandler m_dummyRpcInputAnyXml;
    private DummyRpcHandler m_dummyRpcOutputAnyXml;

    private static final RpcName RPC_NAME = new RpcName("urn:org:bbf2:pma:validation", "testRpcOutput");
    private static final RpcName RPC_WITH_WHEN_AUGMENT_NAME = new RpcName("urn:org:bbf2:pma:validation", "testRpcWhenAugment");
    private static final RpcName RPC_WITH_WHEN_USES_NAME = new RpcName("urn:org:bbf2:pma:validation", "testRpcWhenUses");
    private static final String m_RPC_STR = "<rename-device-holder xmlns=\"http://www.test-company.com/solutions/anv-device-holders\">\n" +
            "  <old-device-holder-name>OLT1</old-device-holder-name>\n" +
            "  <new-device-holder-name>OLTBlah</new-device-holder-name>\n" +
            "</rename-device-holder>";

    private void failTest(){
        fail("we were expecting some exception. If we reach here!!Well we are on the wrong road");
    }

    protected SubSystemRegistry getSubSystemRegistry() {
        SubSystem subSystem = new LocalSubSystem() {
            @Override
            public Map<ModelNodeId, List<Element>> retrieveStateAttributes(
                    Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> mapAttributes) throws GetAttributeException {
                Map<ModelNodeId, List<Element>> returnValue = new HashMap<ModelNodeId,List<Element>>();
                List<Element> elements = new ArrayList<Element>();

                Document document = DocumentUtils.createDocument();
                Element element = document.createElementNS("urn:org:bbf2:pma:validation", "stateValue");
                elements.add(element);
                Element child = document.createElementNS("urn:org:bbf2:pma:validation", "value1");
                element.appendChild(child);
                child.setTextContent("test");

                returnValue.put(mapAttributes.keySet().iterator().next(), elements);

                return returnValue;
            }
        };
        QName leafRefQname = QName.create("urn:org:bbf2:pma:validation", "2015-12-14", "leaf-ref");
        SchemaPath schemaPath = buildSchemaPath(VALIDATION_SCHEMA_PATH, leafRefQname);
        m_subSystemRegistry = Mockito.mock(SubSystemRegistry.class);
        when((m_subSystemRegistry.getCompositeSubSystem())).thenReturn(mock(CompositeSubSystem.class));
        when(m_subSystemRegistry.lookupSubsystem(VALIDATION1_SCHEMA_PATH)).thenReturn(subSystem);
        when(m_subSystemRegistry.lookupSubsystem(VALIDATION_SCHEMA_PATH)).thenReturn(subSystem);
        when(m_subSystemRegistry.lookupSubsystem(schemaPath)).thenReturn(subSystem);

        QName stateValueQname = QName.create("urn:org:bbf2:pma:validation", "2015-12-14", "stateValue");
        SchemaPath stateValuePath = buildSchemaPath(schemaPath, stateValueQname);
        when(m_subSystemRegistry.lookupSubsystem(stateValuePath)).thenReturn(subSystem);

        QName deviceQname = QName.create("urn:org:bbf2:pma:validation", "2015-12-14", "device");
        SchemaPath deviceSchemaPath = buildSchemaPath(schemaPath, deviceQname);
        when(m_subSystemRegistry.lookupSubsystem(deviceSchemaPath)).thenReturn(subSystem);

        SchemaPath deviceStateSchemaPath = buildSchemaPath(deviceSchemaPath, stateValueQname);
        when(m_subSystemRegistry.lookupSubsystem(deviceStateSchemaPath)).thenReturn(subSystem);

        setUpUnwrap(m_subSystemRegistry);
        return m_subSystemRegistry;
    }

    @Before
    public void setup() {
        m_dummyRematchValueRpcHandler = new DummyRpcHandler(new RpcName("urn:org:bbf2:pma:validation", "testRematch"));
        m_dummyRematchValueOutputRpcHandler = new DummyRpcHandler(new RpcName("urn:org:bbf2:pma:validation", "testOutputRematch"));
        m_dummyDefaultValueRpcHandler = new DummyRpcHandler(new RpcName("urn:org:bbf2:pma:validation", "testDefault"));
        m_dummyDefaultValueOutputRpcHandler = new DummyRpcHandler(new RpcName("urn:org:bbf2:pma:validation", "testOutputDefault"));
        m_dummyEmptyRpcHandler = new DummyRpcHandler(new RpcName("urn:org:bbf2:pma:validation", "emptyinput"));
        m_dummyIfFeatureRpcHandler = new DummyRpcHandler(new RpcName("urn:org:bbf2:pma:validation", "testIfFeature"));
        m_dummyIfFeatureOutputRpcHandler = new DummyRpcHandler(new RpcName("urn:org:bbf2:pma:validation", "testOutputIfFeature"));
        m_dummyRpcHandler = new DummyRpcHandler(new RpcName("urn:org:bbf2:pma:validation", "testRpc"));
        m_dummyRpcHandler1 = new DummyRpcHandler(new RpcName("urn:org:bbf2:pma:validation", "testRpcOutput"));
        m_dummyRpcHandler2 = new DummyRpcHandler(new RpcName("urn:org:bbf2:pma:validation", "groupingTest"));
        m_dummyRpcHandler3 = new DummyRpcHandler(new RpcName("urn:org:bbf2:pma:validation","leafRefTest"));
        m_dummyRpcHandler4 = new DummyRpcHandler(new RpcName("urn:org:bbf2:pma:validation","mustCount"));
        m_dummyRpcHandler5 = new DummyRpcHandler(RPC_WITH_WHEN_AUGMENT_NAME);
        m_dummyRpcHandler6 = new DummyRpcHandler(RPC_WITH_WHEN_USES_NAME);
        m_configRpcInputHandler = new DummyRpcHandler(new RpcName("urn:org:bbf2:pma:validation","testConfigValidationInputRpc"));
        m_configOutputRpc = new RpcName("urn:org:bbf2:pma:validation","testConfigValidationOutputRpc");
        m_configRpcOutputHandler = new DummyRpcHandler(m_configOutputRpc);
        m_modelNodeDsm = super.m_modelNodeDsm;
        m_rpcConstraintParser = new RpcRequestConstraintParser(m_schemaRegistry, m_modelNodeDsm, m_expValidator, null);
        m_RpcInputHandler = new DummyRpcHandler(new RpcName("urn:org:bbf2:pma:validation", "leafref-grouping-rpc"));
        m_stateDataRpcHandler = new DummyRpcHandler(new RpcName("urn:org:bbf2:pma:validation", "test-leafref-state-data-rpc"));
        m_dummyRpcInputAnyXml = new DummyRpcHandler(new RpcName("urn:org:bbf2:pma:validation", "testRpcInputAnyXml"));
        m_dummyRpcOutputAnyXml = new DummyRpcHandler(new RpcName("urn:org:bbf2:pma:validation", "testRpcOutputAnyXml"));
        try {
        	getModelNode();
        } catch (ModelNodeInitException e) {}
		
    }

    @Test
    public void testRpcDSLeafRef() throws Exception {
        String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>					" +
                "<validation xmlns=\"urn:org:bbf2:pma:validation\">			" +
                " <rpcRefLeaf>hello</rpcRefLeaf>" +
                "</validation>													" ;

        EditConfigRequest request2 = createRequestFromString(requestXml2);
        request2.setMessageId("1");
        NetConfResponse response2 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request2, response2);
        assertTrue(response2.isOk());

        String xmlPath = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                " <validation:leafRefTest xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
                " <validation:leaf1>hello</validation:leaf1> " +
                " </validation:leafRefTest>"+
                "</rpc>"
                ;
        Element element = DocumentUtils.stringToDocument(xmlPath).getDocumentElement();
        NetconfRpcRequest request = new NetconfRpcRequest();
        request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element));
        request.setMessageId("1");
        TimingLogger.withStartAndFinish(() -> m_dummyRpcHandler3.validate(m_rpcConstraintParser, request));
    }

    @Test
    public void testMustCount() throws Exception {
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "   <list1>" +
                "    <someKey>9</someKey>" +
                "   </list1>" +
                "   <mustCount>must</mustCount>" +
                "</validation>                                                 " ;

        EditConfigRequest request2 = createRequestFromString(requestXml1);
        request2.setMessageId("1");
        NetConfResponse response2 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request2, response2);
        assertTrue(response2.isOk());

        String xmlPath = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                " <validation:mustCount xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
                " <validation:someLeaf>hello</validation:someLeaf> " +
                " <validation:list1>" +
                "  <validation:key>key</validation:key>" +
                " </validation:list1>" +
                " </validation:mustCount>"+
                "</rpc>"
                ;
        Element element = DocumentUtils.stringToDocument(xmlPath).getDocumentElement();
        NetconfRpcRequest request = new NetconfRpcRequest();
        request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element));
        request.setMessageId("1");
        m_dummyRpcHandler4.validate(m_rpcConstraintParser, request);
    }

    @Test
    public void testMustCurrentCount() throws Exception {
        String xmlPath = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                " <validation:mustCount xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
                " <validation:someLeaf>hello</validation:someLeaf> " +
                " <validation:list1>" +
                "  <validation:key>key</validation:key>" +
                " </validation:list1>" +
                " <validation:container1>" +
                "  <validation:list2>" +
                "   <validation:key>key</validation:key>" +
                "  </validation:list2>" +
                "  <validation:someLeaf>key</validation:someLeaf>"+
                "  <validation:someLeaf1>key</validation:someLeaf1>"+
                " </validation:container1>" +
                " </validation:mustCount>"+
                "</rpc>"
                ;
        Element element = DocumentUtils.stringToDocument(xmlPath).getDocumentElement();
        NetconfRpcRequest request = new NetconfRpcRequest();
        request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element));
        request.setMessageId("1");
        m_dummyRpcHandler4.validate(m_rpcConstraintParser, request);
    }

    @Test
    public void testInvalidRpc() throws Exception{
        NetconfRpcRequest rpc = new NetconfRpcRequest();
        rpc.setRpcInput(DocumentUtils.stringToDocument(m_RPC_STR).getDocumentElement());
        SchemaRegistry registry = mock(SchemaRegistry.class);
        RpcRequestHandler handler = new DummyRpcHandler(rpc.getRpcName());
        ModelNodeDataStoreManager dsm = mock(ModelNodeDataStoreManager.class);
        when(registry.getRpcDefinitions()).thenReturn(new ArrayList<RpcDefinition>());
        RpcRequestConstraintParser parser = new RpcRequestConstraintParser(registry,dsm, m_expValidator, null);
        try {
            handler.validate(parser, (NetconfMessage) rpc);
            fail();
        }catch(RpcValidationException exception){
            assertEquals("An unexpected element 'rename-device-holder' is present",exception.getRpcError().getErrorMessage());
        }
    }

    @Test
    public void testRpcLeaf() throws Exception {
        String xmlPath = "#validation:testRpc[@xmlns:validation='urn:org:bbf2:pma:validation', parent='rpc']"
                + "#validation:data-status[parent='validation:testRpc', value='success']";
        Element element = buildRpc(xmlPath);
        NetconfRpcRequest request = new NetconfRpcRequest();
        request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element));
        request.setMessageId("1");
        m_dummyRpcHandler.validate(m_rpcConstraintParser, request);
    }

    @Test(expected = ValidationException.class)
    public void testRpcInputWithDuplicates() throws Throwable {
        String xmlPath = "#validation:testRpc[@xmlns:validation='urn:org:bbf2:pma:validation', parent='rpc']"
                + "#validation:data-status[parent='validation:testRpc', value='success']"
                + "#validation:data-status[parent='validation:testRpc', value='success']";
        Element element = buildRpc(xmlPath);
        NetconfRpcRequest request = new NetconfRpcRequest();
        request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element));
        request.setMessageId("1");
        try {
            m_dummyRpcHandler.validate(m_rpcConstraintParser, request);
            fail("Expected ValidationException");
        } catch (Exception e) {
            assertTrue(e instanceof RpcValidationException);
            assertEquals("RPC Validation failed: Duplicate elements in node (urn:org:bbf2:pma:validation?revision=2015-12-14)data-status", e.getMessage());
            throw e.getCause();
        }
    }

    @Test
    public void testRpcOutputWithDuplicates() throws Exception {
        String xmlPath = "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\""
                + " xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                + " 	<validation:leaf2>hello</validation:leaf2> "
                + " 	<validation:leaf2>hello</validation:leaf2> "
                + " </rpc-reply>";

        Element returnValue = DocumentUtils.getDocumentElement(xmlPath);
        NetconfRpcResponse response = new NetconfRpcResponse();
        response.setMessageId("1");
        TestUtil.addOutputElements(returnValue, response);
        response.setRpcName(m_configOutputRpc);
        m_configRpcOutputHandler.validate(m_rpcConstraintParser, response);
        assertFalse(response.getErrors().isEmpty());
        assertEquals(NetconfRpcErrorTag.BAD_ELEMENT, response.getErrors().get(0).getErrorTag());
        assertEquals("data-not-unique", response.getErrors().get(0).getErrorAppTag());
        assertEquals("Duplicate elements in node (urn:org:bbf2:pma:validation?revision=2015-12-14)leaf2", response.getErrors().get(0).getErrorMessage());
    }

	@Test
    public void testRpcMandatoryOutputLeaf() throws Exception{
        RequestScope.resetScope();
        String xmlPath = "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\""
                + " xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                + " <validation:group-name >group-name</validation:group-name> "
                + " <validation:group-id >group-id</validation:group-id> "
                + " </rpc-reply>"
                ;
        Element returnValue = DocumentUtils.getDocumentElement(xmlPath);
        NetconfRpcResponse response = new NetconfRpcResponse();
        response.setMessageId("1");
        response.setRpcName(RPC_NAME);	
        TestUtil.addOutputElements(returnValue, response);
        m_dummyRpcHandler1.validate(m_rpcConstraintParser, response);
        assertEquals(0, response.getErrors().size());

        Map<?, ?> requestScope = (Map<?, ?>) RequestScope.getCurrentScope().getFromCache("MANDATORY_TYPE_VALIDATION_CACHE");
        assertEquals(1,requestScope.size());

        RequestScope.resetScope();
    }

    @Test
    public void testRpcOutputContainer() throws Exception{
        String xmlPath = "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\""
                + " xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                + " <validation:group-name >group-name</validation:group-name> "
                + " <validation:group-id >group-id</validation:group-id> "
                + "<validation:container1>"
                + " <validation:innerContainer>"
                + " <validation:innerList>"
                + "<validation:innerLeaf>hello</validation:innerLeaf>"
                + "</validation:innerList>"
                + "</validation:innerContainer>"
                + "</validation:container1>"
                + " </rpc-reply>"
                ;

        Element returnValue = DocumentUtils.getDocumentElement(xmlPath);
        NetconfRpcResponse response = new NetconfRpcResponse();
        response.setMessageId("1");
        TestUtil.addOutputElements(returnValue, response);
        response.setRpcName(RPC_NAME);
        m_dummyRpcHandler1.validate(m_rpcConstraintParser, response);
        assertTrue(response.getErrors().isEmpty());

        xmlPath = "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\""
                + " xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                + " <validation:group-name >group-name</validation:group-name> "
                + " <validation:group-id >group-id</validation:group-id> "
                + "<validation:container0>"
                + "<validation:leaf1>hello</validation:leaf1>"
                + "</validation:container0>"
                + " </rpc-reply>"
                ;

        returnValue = DocumentUtils.getDocumentElement(xmlPath);
        response = new NetconfRpcResponse();
        response.setMessageId("1");
        TestUtil.addOutputElements(returnValue, response);
        response.setRpcName(RPC_NAME);
        m_dummyRpcHandler1.validate(m_rpcConstraintParser, response);
        assertTrue(response.getErrors().isEmpty());
    }

    @Test
    public void testAugmentedRpcOutputWithWhenConditionOnDefault() throws Exception{
        String xmlPath = "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\""
                + " xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                + " <validation:group-name>group-name</validation:group-name> "
                + " <validation:group-id>group-id</validation:group-id> "
                + " <validation:test4>test4</validation:test4> "
                + " <validation:test5>test5</validation:test5> "
                + " <validation:container1>"
                + " <validation:test/>"
                + "<validation:groupingContainer>"
                + "<validation:whenOnEnum>test</validation:whenOnEnum>"
                + "</validation:groupingContainer>"
                + " <validation:innerContainer>"
                + " <validation:innerList>"
                + "<validation:innerLeaf>hello</validation:innerLeaf>"
                + "</validation:innerList>"
                + "</validation:innerContainer>"
                + "</validation:container1>"
                + " </rpc-reply>"
                ;

        Element returnValue = DocumentUtils.getDocumentElement(xmlPath);
        NetconfRpcResponse response = new NetconfRpcResponse();
        response.setMessageId("1");
        TestUtil.addOutputElements(returnValue, response);
        response.setRpcName(RPC_NAME);
        m_dummyRpcHandler1.validate(m_rpcConstraintParser, response);
        assertTrue(response.getErrors().isEmpty());

        String xmlPath1 = "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\""
                + " xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                + " <validation:group-name>group-name</validation:group-name> "
                + " <validation:group-id>group-id</validation:group-id> "
                + " <validation:test4>empty</validation:test4> "
                + " <validation:test5>test5</validation:test5> "
                + " <validation:container1>"
                + " <validation:test/>"
                + "<validation:groupingContainer>"
                + "<validation:whenOnEnum>test</validation:whenOnEnum>"
                + "</validation:groupingContainer>"
                + " <validation:innerContainer>"
                + " <validation:innerList>"
                + "<validation:innerLeaf>hello</validation:innerLeaf>"
                + "</validation:innerList>"
                + "</validation:innerContainer>"
                + "</validation:container1>"
                + " </rpc-reply>";

        Element returnValue1 = DocumentUtils.getDocumentElement(xmlPath1);
        NetconfRpcResponse response1 = new NetconfRpcResponse();
        response1.setMessageId("1");
        TestUtil.addOutputElements(returnValue1, response1);
        response1.setRpcName(RPC_NAME);
        m_dummyRpcHandler1.validate(m_rpcConstraintParser, response1);
        assertFalse(response1.isOk());
        assertEquals("Violate when constraints: ../test4='test4' and ../test5 = 'test5'", response1.getErrors().get(0).getErrorMessage());
        assertFalse(response1.getErrors().isEmpty());

        xmlPath = "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\""
                + " xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                + " <validation:group-name>group-name</validation:group-name> "
                + " <validation:group-id>group-id</validation:group-id> "
                + " <validation:test4>test4</validation:test4> "
                + " <validation:test5>test5</validation:test5> "
                + " <validation:container1>"
                + " <validation:test/>"
                + " <validation:groupingContainer>"
                + " <validation:enumLeaf>router-mode</validation:enumLeaf>"
                + " <validation:whenOnEnum>test</validation:whenOnEnum>"
                + " </validation:groupingContainer>"
                + " <validation:innerContainer>"
                + " <validation:innerList>"
                + " <validation:innerLeaf>hello</validation:innerLeaf>"
                + " </validation:innerList>"
                + " </validation:innerContainer>"
                + " </validation:container1>"
                + " </rpc-reply>";

        returnValue = DocumentUtils.getDocumentElement(xmlPath);
        response = new NetconfRpcResponse();
        response.setMessageId("1");
        TestUtil.addOutputElements(returnValue, response);
        response.setRpcName(RPC_NAME);
        m_dummyRpcHandler1.validate(m_rpcConstraintParser, response);
        assertFalse(response.isOk());
        assertEquals("Violate when constraints: ../enumLeaf = 'bridge-mode' and ../../test/enumLeaf1 = 'bridge-mode'", response.getErrors().get(0).getErrorMessage());

        xmlPath = "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\""
                + " xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                + " <validation:group-name>group-name</validation:group-name> "
                + " <validation:group-id>group-id</validation:group-id> "
                + " <validation:test4>test4</validation:test4> "
                + " <validation:test5>test5</validation:test5> "
                + " <validation:container1>"
                + " <validation:test>"
                + " <validation:enumLeaf1>router-mode</validation:enumLeaf1>"
                + " </validation:test>"
                + " <validation:groupingContainer>"
                + " <validation:enumLeaf>bridge-mode</validation:enumLeaf>"
                + " <validation:whenOnEnum>test</validation:whenOnEnum>"
                + " </validation:groupingContainer>"
                + " <validation:innerContainer>"
                + " <validation:innerList>"
                + " <validation:innerLeaf>hello</validation:innerLeaf>"
                + " </validation:innerList>"
                + " </validation:innerContainer>"
                + " </validation:container1>"
                + " </rpc-reply>";

        returnValue = DocumentUtils.getDocumentElement(xmlPath);
        response = new NetconfRpcResponse();
        response.setMessageId("1");
        TestUtil.addOutputElements(returnValue, response);
        response.setRpcName(RPC_NAME);
        m_dummyRpcHandler1.validate(m_rpcConstraintParser, response);
        assertFalse(response.isOk());
        assertEquals("Violate when constraints: ../enumLeaf = 'bridge-mode' and ../../test/enumLeaf1 = 'bridge-mode'", response.getErrors().get(0).getErrorMessage());
    }

    @Test
    public void testRpcOutputLeaf() throws Exception {
        String xmlPath = "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\""
                + " xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                + " <validation:test1>9</validation:test1>"
                + " <validation:group-name >group-name</validation:group-name> "
                + " <validation:group-id >group-id</validation:group-id> "
                + " </rpc-reply>"
                ;
        Element returnValue = DocumentUtils.getDocumentElement(xmlPath);
        NetconfRpcResponse response = new NetconfRpcResponse();
        response.setMessageId("1");
        TestUtil.addOutputElements(returnValue, response);
        response.setRpcName(RPC_NAME);
        m_dummyRpcHandler1.validate(m_rpcConstraintParser, response);
        assertTrue(response.getErrors().isEmpty());

        String xmlPath1 = "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\""
                + " xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                + " <validation:test1>11</validation:test1>"
                + " <validation:test2>H</validation:test2>"
                + " <validation:group-name >group-name</validation:group-name> "
                + " <validation:group-id >group-id</validation:group-id> "
                + " </rpc-reply>"
                ;
        returnValue = DocumentUtils.getDocumentElement(xmlPath1);
        response = new NetconfRpcResponse();
        response.setMessageId("1");
        TestUtil.addOutputElements(returnValue, response);
        response.setRpcName(RPC_NAME);
        m_dummyRpcHandler1.validate(m_rpcConstraintParser, response);
        assertTrue(response.getErrors().isEmpty());

        String xmlPath2 = "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\""
                + " xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                + " <validation:test1>9</validation:test1>"
                + " <validation:test2>H</validation:test2>"
                + " <validation:group-name >group-name</validation:group-name> "
                + " <validation:group-id >group-id</validation:group-id> "
                + " </rpc-reply>"
                ;
        returnValue = DocumentUtils.getDocumentElement(xmlPath2);
        response = new NetconfRpcResponse();
        response.setMessageId("1");
        TestUtil.addOutputElements(returnValue, response);
        response.setRpcName(RPC_NAME);
        m_dummyRpcHandler1.validate(m_rpcConstraintParser, response);

        assertEquals(null, response.getData());
        assertEquals(1, response.getErrors().size());
        assertEquals("Violate when constraints: ../test1 > 10", response.getErrors().get(0).getErrorMessage());

        String xmlPath4 = "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\""
                + " xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                + " <validation:test1>10</validation:test1>"
                + " <validation:test3>H</validation:test3>"
                + " <validation:group-name >group-name</validation:group-name> "
                + " <validation:group-id >group-id</validation:group-id> "
                + " </rpc-reply>"
                ;
        returnValue = DocumentUtils.getDocumentElement(xmlPath4);
        response = new NetconfRpcResponse();
        response.setMessageId("1");
        TestUtil.addOutputElements(returnValue, response);
        response.setRpcName(RPC_NAME);
        m_dummyRpcHandler1.validate(m_rpcConstraintParser, response);

        assertFalse(response.isOk());
        assertEquals("Violate when constraints: ../test1 != 10", response.getErrors().get(0).getErrorMessage());

    }

    @Test(expected = ValidationException.class)
    public void testRpcInvalidLeaf() throws Throwable {
        try {
            String xmlPath = "#validation:testRpc[@xmlns:validation='urn:org:bbf2:pma:validation', parent='rpc']"
                    + "#validation:data-status[parent='validation:testRpc', value='succes']";
            Element element = buildRpc(xmlPath);
            NetconfRpcRequest request = new NetconfRpcRequest();
            request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element));
            request.setMessageId("1");
            m_dummyRpcHandler.validate(m_rpcConstraintParser, request);
            failTest();
        } catch (Exception e) {
            assertTrue(e instanceof RpcValidationException);
            assertTrue(e.getMessage()
                    .contains("Value \"succes\" is an invalid value. Expected values: [success, failed, in-progress]"));
            throw e.getCause();
        }
    }

    @Test
    public void testRpcMustLeaf() throws Exception {
        // data-status = success case
        String xmlPath = "#validation:testRpc[@xmlns:validation='urn:org:bbf2:pma:validation', parent='null']"
                + "#validation:data-status[parent='validation:testRpc', value='success']"
                + "#validation:leaf-type[parent='validation:testRpc', value='good']";
        Element element = buildRpc(xmlPath);

        NetconfRpcRequest request = new NetconfRpcRequest();
        request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element));
        request.setMessageId("1");
        m_dummyRpcHandler.validate(m_rpcConstraintParser, request);

        // data-status = in-progress case
        xmlPath = "#validation:testRpc[@xmlns:validation='urn:org:bbf2:pma:validation', parent='rpc']"
                + "#validation:data-status[parent='validation:testRpc', value='in-progress']"
                + "#validation:leaf-type[parent='validation:testRpc', value='good']";
        element = buildRpc(xmlPath);
        request = new NetconfRpcRequest();
        request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element));
        request.setMessageId("1");
        m_dummyRpcHandler.validate(m_rpcConstraintParser, request);
    }

    @Test(expected = ValidationException.class)
    public void testRpcInvalidMustLeaf() throws Throwable {
        try {
            String xmlPath = "#validation:testRpc[@xmlns:validation='urn:org:bbf2:pma:validation', parent='rpc']"
                    + "#validation:data-status[parent='validation:testRpc', value='failed']"
                    + "#validation:leaf-type[parent='validation:testRpc', value='good']";
            Element element = buildRpc(xmlPath);
            NetconfRpcRequest request = new NetconfRpcRequest();
            request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element));
            request.setMessageId("1");
            m_dummyRpcHandler.validate(m_rpcConstraintParser, request);
            failTest();
        } catch (Exception e) {
            assertTrue(e instanceof RpcValidationException);
            assertTrue(e.getMessage().contains("data-status must be success or in-progress"));
            throw e.getCause();
        }
    }

    @Test
    public void testRpcList() throws Exception{
        String xmlPath = "#validation:testRpc[@xmlns:validation='urn:org:bbf2:pma:validation', parent='rpc']"
                + "#validation:result-list[parent='validation:testRpc', value='10']"
                + "#validation:list-type[parent='validation:testRpc',]"
                + "#validation:list-id[parent='validation:list-type', value='id1']"
                + "#validation:list-value[parent='validation:list-type', value='11']";
        Element element = buildRpc(xmlPath);
        NetconfRpcRequest request = new NetconfRpcRequest();
        request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element));
        request.setMessageId("1");
        m_dummyRpcHandler.validate(m_rpcConstraintParser, request);

        try{
            xmlPath = "#validation:testRpc[@xmlns:validation='urn:org:bbf2:pma:validation', parent='rpc']"
                    + "#validation:result-list[parent='validation:testRpc', value='1']"
                    + "#validation:list-type[parent='validation:testRpc',]"
                    + "#validation:list-id[parent='validation:list-type', value='id1']"
                    + "#validation:list-value[parent='validation:list-type', value='11']";
            element = buildRpc(xmlPath);
            request = new NetconfRpcRequest();
            request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element));
            request.setMessageId("1");
            m_dummyRpcHandler.validate(m_rpcConstraintParser, request);
            failTest();
        }catch(Exception e){
            assertTrue(e instanceof RpcValidationException);
            assertTrue(e.getMessage().contains("Violate when constraints: ../result-list = 10"));
        }

        /** This test the condition of a bit complex case.
		 	container a {
		 		list a{
		 			list b{
		 				when ../../c/d > 10
		 				}
		 			}
		 		list c{
		 			leaf d
		 			}
		 		}
         **/
        xmlPath = "#validation:testRpc[@xmlns:validation='urn:org:bbf2:pma:validation', parent='rpc']"
                + "#validation:result-list[parent='validation:testRpc', value='10']"
                + "#validation:list-type[parent='validation:testRpc',]"
                + "#validation:list-id[parent='validation:list-type', value='id1']"
                + "#validation:list-value[parent='validation:list-type', value='11']"
                + "#validation:inside-list[parent='validation:list-type']"
                + "#validation:some-leaf[parent='validation:inside-list', value='10']"
                + "#validation:list-type2[parent='validation:testRpc']"
                + "#validation:list-id[parent='validation:list-type2', value='11']";
        element = buildRpc(xmlPath);
        request = new NetconfRpcRequest();
        request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element));
        request.setMessageId("1");
        m_dummyRpcHandler.validate(m_rpcConstraintParser, request);

    }

    //@Test(expected = RpcValidationException.class)
    public void testRpcLeafRef() throws Exception {
        XmlGenerator generator = new XmlGenerator();
        NetconfRpcRequest request = new NetconfRpcRequest();
        generator.addElement(null, "rpc", null, "xmlns='urn:ietf:params:xml:ns:netconf:base:1.0'", "message-id='1'")
        .addElement("rpc", "validation:testRpc", null, "xmlns:validation='urn:org:bbf2:pma:validation'")
        .addElement("validation:testRpc", "validation:leaf-ref", null, EMPTY_STRING)
        .addElement("validation:leaf-ref", "validation:artist", null, EMPTY_STRING)
        .addElement("validation:artist", "validation:name", "LENNY", EMPTY_STRING)
        .addElement("validation:leaf-ref", "validation:album", null, EMPTY_STRING)
        .addElement("validation:album", "validation:name", "Album1", EMPTY_STRING)
        .addElement("validation:album", "validation:song", null, EMPTY_STRING)
        .addElement("validation:song", "validation:name", "Last Christmas", EMPTY_STRING)
        .addElement("validation:song", "validation:artist-name", "LENNY", EMPTY_STRING)
        .addElement("validation:album", "validation:song-count", "20", EMPTY_STRING)
        .addElement("validation:leaf-ref", "validation:music", null, EMPTY_STRING)
        .addElement("validation:music", "validation:kind", "Balad", EMPTY_STRING)
        .addElement("validation:music", "validation:favourite-album", "Album1", EMPTY_STRING)
        .addElement("validation:music", "validation:favourite-song", "Last Christmas", EMPTY_STRING)
        .addElement("validation:leaf-ref", "validation:current-alone", null, EMPTY_STRING)
        .addElement("validation:current-alone", "validation:current-leaf", "Album1", EMPTY_STRING)
        .addElement("validation:current-alone", "validation:current-alone-leaf", "Album1", EMPTY_STRING)
        .addElement("validation:current-alone", "validation:current-parent-leaf", "Album1", EMPTY_STRING)
        .addElement("validation:current-alone", "validation:current-leaf-list", "Test1", EMPTY_STRING)
        .addElement("validation:current-alone", "validation:current-leaf-list", "Test2", EMPTY_STRING);

        request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(generator.buildXml()));
        request.setMessageId("1");
        m_dummyRpcHandler.validate(m_rpcConstraintParser, request);

        try {
            generator = new XmlGenerator();
            generator.addElement(null, "rpc", null, "xmlns='urn:ietf:params:xml:ns:netconf:base:1.0'", "message-id='1'")
            .addElement("rpc", "validation:testRpc", null, "xmlns:validation='urn:org:bbf2:pma:validation'")
            .addElement("validation:testRpc", "validation:leaf-ref", null, EMPTY_STRING)
            .addElement("validation:leaf-ref", "validation:artist", null, EMPTY_STRING)
            .addElement("validation:artist", "validation:name", "LENNY", EMPTY_STRING)
            .addElement("validation:leaf-ref", "validation:album", null, EMPTY_STRING)
            .addElement("validation:album", "validation:name", "Album1", EMPTY_STRING)
            .addElement("validation:album", "validation:song", null, EMPTY_STRING)
            .addElement("validation:song", "validation:name", "Last Christmas", EMPTY_STRING)
            .addElement("validation:song", "validation:artist-name", "LENNY", EMPTY_STRING)
            .addElement("validation:album", "validation:song-count", "0", EMPTY_STRING)
            .addElement("validation:leaf-ref", "validation:music", null, EMPTY_STRING)
            .addElement("validation:music", "validation:kind", "Balad", EMPTY_STRING)
            .addElement("validation:music", "validation:favourite-album", "Album1", EMPTY_STRING)
            .addElement("validation:music", "validation:favourite-song", "Last Christmas", EMPTY_STRING)
            .addElement("validation:leaf-ref", "validation:current-alone", null, EMPTY_STRING)
            .addElement("validation:current-alone", "validation:current-leaf", "Album1", EMPTY_STRING)
            .addElement("validation:current-alone", "validation:current-alone-leaf", "Album1", EMPTY_STRING)
            .addElement("validation:current-alone", "validation:current-parent-leaf", "Album1", EMPTY_STRING)
            .addElement("validation:current-alone", "validation:current-leaf-list", "Test1", EMPTY_STRING)
            .addElement("validation:current-alone", "validation:current-leaf-list", "Test2", EMPTY_STRING);

            request = new NetconfRpcRequest();
            request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(generator.buildXml()));
            request.setMessageId("1");
            m_dummyRpcHandler.validate(m_rpcConstraintParser, request);
            failTest();
        } catch (Exception e) {
            assertTrue(e instanceof RpcValidationException);
            ValidationException cause = (ValidationException) e.getCause();
            assertTrue(cause.getRpcError().getErrorMessage()
                    .contains("Violate must constraints: ../../album[current()]/song-count >= 10"));
            throw e;
        }
    }

    @Test
    public void testRpcInstanceIdentifier() throws Exception{
        XmlGenerator generator = new XmlGenerator();
        generator.addElement(null, "rpc", null, "xmlns='urn:ietf:params:xml:ns:netconf:base:1.0'", "message-id='1'")
        .addElement("rpc", "validation:testRpc", null, "xmlns:validation='urn:org:bbf2:pma:validation'")
        .addElement("validation:testRpc", "validation:instance-identifier-example", null, EMPTY_STRING)
        .addElement("validation:instance-identifier-example", "validation:leaflist",
                "/testRpc/instance-identifier-example/leaf1", EMPTY_STRING)
        .addElement("validation:instance-identifier-example", "validation:leaf1", "leaf1", EMPTY_STRING)
        .addElement("validation:instance-identifier-example", "validation:student", null, EMPTY_STRING)
        .addElement("validation:student", "validation:student-id", "ST001", EMPTY_STRING)
        .addElement("validation:student", "validation:student-name", "Student 1", EMPTY_STRING)
        .addElement("validation:student", "validationn:student-instance-identifier1",
                "/testRpc/instance-identifier-example/leaf1", EMPTY_STRING);


        NetconfRpcRequest request = new NetconfRpcRequest();
        request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(generator.buildXml()));
        request.setMessageId("1");
        m_dummyRpcHandler.validate(m_rpcConstraintParser, request);

        try{
            String xmlPath = "#validation:testRpc[@xmlns:validation='urn:org:bbf2:pma:validation', parent='rpc']"
                    + "#validation:instance-identifier-example[parent='validation:testRpc']"
                    + "#validation:leaflist[parent='validation:instance-identifier-example', value='/testRpc/instance-identifier-example/leaf1']"
                    + "#validation:student[parent='validation:instance-identifier-example']"
                    + "#validation:student-id[parent='validation:student', value='ST001']"
                    + "#validation:student-name[parent='validation:student', value='Student 1']"
                    + "#validation:student-instance-identifier1[parent='validation:student', value='/testRpc/instance-identifier-example/leaf1']"

					;

            Element element = buildRpc(xmlPath);
            request = new NetconfRpcRequest();
            request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element));
            request.setMessageId("1");
            m_dummyRpcHandler.validate(m_rpcConstraintParser, request);
            failTest();
        }catch(Exception e){
            assertTrue(e instanceof RpcValidationException);
            ValidationException cause = (ValidationException) e.getCause();
            assertTrue(cause.getRpcError().toString().contains
                    ("errorPath=/validation:instance-identifier-example/validation:student/validation:student-instance-identifier1,"
                            + " errorMessage=Missing required element /testRpc/instance-identifier-example/leaf1"));
        }
    }

    @Test
    public void testRpcInstanceIdentifierWithoutPrefix() throws Exception{
        try{
            String xmlPath = "#testRpc[@xmlns='urn:org:bbf2:pma:validation', parent='rpc']"
                    + "#instance-identifier-example[parent='testRpc']"
                    + "#leaflist[parent='instance-identifier-example', value='/testRpc/instance-identifier-example/leaf1']"
                    + "#student[parent='instance-identifier-example']"
                    + "#student-id[parent='student', value='ST001']"
                    + "#student-name[parent='student', value='Student 1']"
                    + "#student-instance-identifier1[parent='student', value='/testRpc/instance-identifier-example/leaf1']"

					;

            Element element = buildRpc(xmlPath);
            NetconfRpcRequest request = new NetconfRpcRequest();
            request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element));
            request.setMessageId("1");
            m_dummyRpcHandler.validate(m_rpcConstraintParser, request);
            failTest();
        }catch(Exception e){
            assertTrue(e instanceof RpcValidationException);
            ValidationException cause = (ValidationException) e.getCause();
            assertTrue(cause.getRpcError().toString().contains
                    ("errorPath=/validation:instance-identifier-example/validation:student/validation:student-instance-identifier1,"
                            + " errorMessage=Missing required element /testRpc/instance-identifier-example/leaf1"));
        }
    }

    public void testRpcChoiceCaseList() throws Exception{
        String xmlPath = "#validation:testRpc[@xmlns:validation='urn:org:bbf2:pma:validation', parent='rpc']"
                + "#validation:choicecase[parent='validation:testRpc']"
                + "#validation:data-choice[parent='validation:choicecase', value='49']"
                + "#validation:result-choice[parent='validation:choicecase', value='success']"
                + "#validation:list-case-success[parent='validation:choicecase']"
                + "#validation:success-id[parent='validation:list-case-success', value='1']"
                + "#validation:success-value[parent='validation:list-case-success', value='leaf2']"

					;
        Element element = buildRpc(xmlPath);
        NetconfRpcRequest request = new NetconfRpcRequest();
        request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element));
        request.setMessageId("1");
        m_dummyRpcHandler.validate(m_rpcConstraintParser, request);

        xmlPath = "#validation:testRpc[@xmlns:validation='urn:org:bbf2:pma:validation', parent='rpc']"
                + "#validation:choicecase[parent='validation:testRpc']"
                + "#validation:data-choice[parent='validation:choicecase', value='49']"
                + "#validation:result-choice[parent='validation:choicecase', value='failed']"
                + "#validation:list-case-failed[parent='validation:choicecase']"
                + "#validation:failed-id[parent='validation:list-case-failed', value='1']"
                + "#validation:failed-value[parent='validation:list-case-failed', value='leaf2']"

					;
        element = buildRpc(xmlPath);
        request = new NetconfRpcRequest();
        request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element));
        request.setMessageId("1");
        m_dummyRpcHandler.validate(m_rpcConstraintParser, request);

        try{
            xmlPath = "#validation:testRpc[@xmlns:validation='urn:org:bbf2:pma:validation', parent='rpc']"
                    + "#validation:choicecase[parent='validation:testRpc']"
                    + "#validation:data-choice[parent='validation:choicecase', value='51']"
                    + "#validation:result-choice[parent='validation:choicecase', value='success']"
                    + "#validation:list-case-success[parent='validation:choicecase']"
                    + "#validation:success-id[parent='validation:list-case-success', value='1']"
                    + "#validation:success-value[parent='validation:list-case-success', value='leaf2']"

					;
            element = buildRpc(xmlPath);
            request = new NetconfRpcRequest();
            request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element));
            request.setMessageId("1");
            m_dummyRpcHandler.validate(m_rpcConstraintParser, request);
            failTest();
        }catch(Exception e){
            assertTrue(e instanceof RpcValidationException);
            assertTrue(e.getMessage().contains("Violate when constraints: ../data-choice < 50"));
        }


        try{
            xmlPath = "#validation:testRpc[@xmlns:validation='urn:org:bbf2:pma:validation', parent='rpc']"
                    + "#validation:choicecase[parent='validation:testRpc']"
                    + "#validation:data-choice[parent='validation:choicecase', value='49']"
                    + "#validation:result-choice[parent='validation:choicecase', value='failed']"
                    + "#validation:list-case-success[parent='validation:choicecase']"
                    + "#validation:success-id[parent='validation:list-case-success', value='1']"
                    + "#validation:success-value[parent='validation:list-case-success', value='leaf2']"

					;
            element = buildRpc(xmlPath);
            request = new NetconfRpcRequest();
            request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element));
            request.setMessageId("1");
            m_dummyRpcHandler.validate(m_rpcConstraintParser, request);
            failTest();
        }catch(Exception e){
            assertTrue(e instanceof RpcValidationException);
            assertTrue(e.getMessage().contains("Violate when constraints: ../../result-choice = 'success'"));
            throw e;
        }
    }

    @Test
    public void testRpcChoiceCaseContainer() throws Exception{
        String xmlPath = "#validation:testRpc[@xmlns:validation='urn:org:bbf2:pma:validation', parent='rpc']"
                + "#validation:choicecase[parent='validation:testRpc']"
                + "#validation:data-choice[parent='validation:choicecase', value='101']"
                + "#validation:result-choice[parent='validation:choicecase', value='success']"
                + "#validation:container-case-success[parent='validation:choicecase']"
                + "#validation:container-success-leaf1[parent='validation:container-case-success', value='leaf1']"
                + "#validation:container-success-leaf2[parent='validation:container-case-success', value='leaf2']"

					;
        Element element = buildRpc(xmlPath);
        NetconfRpcRequest request = new NetconfRpcRequest();
        request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element));
        request.setMessageId("1");
        m_dummyRpcHandler.validate(m_rpcConstraintParser, request);

        xmlPath = "#validation:testRpc[@xmlns:validation='urn:org:bbf2:pma:validation', parent='rpc']"
                + "#validation:choicecase[parent='validation:testRpc']"
                + "#validation:data-choice[parent='validation:choicecase', value='101']"
                + "#validation:result-choice[parent='validation:choicecase', value='failed']"
                + "#validation:container-case-failed[parent='validation:choicecase']"
                + "#validation:container-failed-leaf1[parent='validation:container-case-failed', value='leaf1']"
                + "#validation:container-failed-leaf2[parent='validation:container-case-failed', value='leaf2']"

							;
        element = buildRpc(xmlPath);
        request = new NetconfRpcRequest();
        request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element));
        request.setMessageId("1");
        m_dummyRpcHandler.validate(m_rpcConstraintParser, request);

        try{
            xmlPath = "#validation:testRpc[@xmlns:validation='urn:org:bbf2:pma:validation', parent='rpc']"
                    + "#validation:choicecase[parent='validation:testRpc']"
                    + "#validation:data-choice[parent='validation:choicecase', value='100']"
                    + "#validation:result-choice[parent='validation:choicecase', value='success']"
                    + "#validation:container-case-success[parent='validation:choicecase']"
                    + "#validation:container-success-leaf1[parent='validation:container-case-success', value='leaf1']"
                    + "#validation:container-success-leaf2[parent='validation:container-case-success', value='leaf2']"

				;
            element = buildRpc(xmlPath);
            request = new NetconfRpcRequest();
            request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element));
            request.setMessageId("1");
            m_dummyRpcHandler.validate(m_rpcConstraintParser, request);
            failTest();
        }catch(Exception e){
            assertTrue(e instanceof RpcValidationException);
            assertTrue(e.getMessage().contains("Violate when constraints: data-choice > 100"));
        }

    }

    @Test
    public void testRpcChoiceCase() throws Exception{
        String xmlPath = "#validation:testRpc[@xmlns:validation='urn:org:bbf2:pma:validation', parent='rpc']"
                + "#validation:choicecase[parent='validation:testRpc']"
                + "#validation:data-choice[parent='validation:choicecase', value='1']"
                + "#validation:result-choice[parent='validation:choicecase', value='success']"
                + "#validation:leaf-case-success[parent='validation:choicecase', value='hello']"
                ;
        Element element = buildRpc(xmlPath);
        NetconfRpcRequest request = new NetconfRpcRequest();
        request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element));
        request.setMessageId("1");
        m_dummyRpcHandler.validate(m_rpcConstraintParser, request);

        try{
            xmlPath = "#validation:testRpc[@xmlns:validation='urn:org:bbf2:pma:validation', parent='rpc']"
                    + "#validation:choicecase[parent='validation:testRpc']"
                    + "#validation:data-choice[parent='validation:choicecase', value='1']"
                    + "#validation:result-choice[parent='validation:choicecase', value='failed']"
                    + "#validation:leaf-case-success[parent='validation:choicecase', value='hello']"
                    ;
            element = buildRpc(xmlPath);
            request = new NetconfRpcRequest();
            request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element));
            request.setMessageId("1");
            m_dummyRpcHandler.validate(m_rpcConstraintParser, request);
            failTest();
        }catch(Exception e){
            assertTrue(e instanceof RpcValidationException);
            assertTrue(e.getMessage().contains("Violate when constraints: result-choice = 'success'"));
        }


    }

    Element buildRpc(String string) throws Exception{
        StringBuilder builder = new StringBuilder();
        builder.append("#rpc[@xmlns='urn:ietf:params:xml:ns:netconf:base:1.0', @message-id='1', parent='null']")
        .append(string);
        Element returnValue = XmlGenerator.buildXml(builder.toString());
        return returnValue;
    }

    @Test
    public void testTargetStateLeafRef() throws RpcValidationException, NetconfMessageBuilderException, ModelNodeInitException{
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                " <leaf-ref>" +
                " </leaf-ref>" +
                "</validation>                                                 " ;
        editConfig(m_server, m_clientInfo, requestXml1, true);

        XmlGenerator generator = new XmlGenerator();
        generator.addElement(null, "rpc", null, "xmlns='urn:ietf:params:xml:ns:netconf:base:1.0'", "message-id='1'")
        .addElement("rpc", "validation:testRpc", null, "xmlns:validation='urn:org:bbf2:pma:validation'")
        .addElement("validation:testRpc", "validation:leaf-ref", null, EMPTY_STRING)
        .addElement("validation:leaf-ref", "validation:stateValue", null, EMPTY_STRING)
        .addElement("validation:stateValue", "validation:value2", "test", EMPTY_STRING);
        NetconfRpcRequest request = new NetconfRpcRequest();
        request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(generator.buildXml()));
        request.setMessageId("1");
        NetconfRpcRequest finalRequest = request;
        TimingLogger.withStartAndFinish(() -> m_dummyRpcHandler.validate(m_rpcConstraintParser, finalRequest));

        generator = new XmlGenerator();
        generator.addElement(null, "rpc", null, "xmlns='urn:ietf:params:xml:ns:netconf:base:1.0'", "message-id='1'")
        .addElement("rpc", "validation:testRpc", null, "xmlns:validation='urn:org:bbf2:pma:validation'")
        .addElement("validation:testRpc", "validation:leaf-ref", null, EMPTY_STRING)
        .addElement("validation:leaf-ref", "validation:stateValue", null, EMPTY_STRING)
        .addElement("validation:stateValue", "validation:value3", "test", EMPTY_STRING);
        request = new NetconfRpcRequest();
        request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(generator.buildXml()));
        request.setMessageId("1");
        NetconfRpcRequest finalRequest1 = request;
        TimingLogger.withStartAndFinish(() -> m_dummyRpcHandler.validate(m_rpcConstraintParser, finalRequest1));
    }

    @Test
    public void testLeafRefWithStateDataInRpc() throws Exception {
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + " <leaf-ref>"
                + " <device>"
                + "   <device-id>TEST</device-id>"
                + " </device>"
                + " </leaf-ref>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);

        XmlGenerator generator = new XmlGenerator();
        generator.addElement(null, "rpc", null, "xmlns='urn:ietf:params:xml:ns:netconf:base:1.0'", "message-id='1'")
        .addElement("rpc", "validation:test-leafref-state-data-rpc", null,
                "xmlns:validation='urn:org:bbf2:pma:validation'")
        .addElement("validation:test-leafref-state-data-rpc", "validation:device-id", "TEST", EMPTY_STRING)
        .addElement("validation:test-leafref-state-data-rpc", "validation:state-leaf", "test", EMPTY_STRING);
        NetconfRpcRequest request = new NetconfRpcRequest();
        request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(generator.buildXml()));
        request.setMessageId("1");
        m_stateDataRpcHandler.validate(m_rpcConstraintParser, request);
    }

    @Test
    public void testLeafRefValueWithInvalidStateDataInRpc() throws Exception {
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    "
                + "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + " <leaf-ref>"
                + " <device>"
                + "   <device-id>TEST</device-id>"
                + " </device>"
                + " <device>"
                + "   <device-id>TEST2</device-id>"
                + " </device>"
                + " <device>"
                + "   <device-id>TEST3</device-id>"
                + " </device>"
                + " </leaf-ref>"
                + "</validation>                                                 ";
        editConfig(m_server, m_clientInfo, requestXml1, true);

        XmlGenerator generator = new XmlGenerator();
        generator.addElement(null, "rpc", null, "xmlns='urn:ietf:params:xml:ns:netconf:base:1.0'", "message-id='1'")
        .addElement("rpc", "validation:test-leafref-state-data-rpc", null,
                "xmlns:validation='urn:org:bbf2:pma:validation'")
        .addElement("validation:test-leafref-state-data-rpc", "validation:device-id", "TEST3", EMPTY_STRING)
        .addElement("validation:test-leafref-state-data-rpc", "validation:state-leaf", "test1",
                EMPTY_STRING);
        NetconfRpcRequest request = new NetconfRpcRequest();
        request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(generator.buildXml()));
        request.setMessageId("1");
        // should not be thrown an error if leafref points to invalid statedata for rpc/action since we are skipping leafref validation for state-data
        m_stateDataRpcHandler.validate(m_rpcConstraintParser, request);
    }

    @Test
    public void testRpc_LeafrefWithGrouping_ValidLeafRefValue() throws Exception {
        // added leaf-ref node in DS
        String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + " <validation>test</validation>"
                + "</validation>";
        EditConfigRequest request2 = createRequestFromString(requestXml2);
        request2.setMessageId("1");
        NetConfResponse response2 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request2, response2);
        assertTrue(response2.isOk());

        String responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                + "    <validation:validation>test</validation:validation>"
                + "  </validation:validation>"
                + " </data>"
                + " </rpc-reply>";
        verifyGet(responseXml);

        // grouping leaf-ref validation should be done in Datastore for RPC
        XmlGenerator generator = new XmlGenerator();
        generator.addElement(null, "rpc", null, "xmlns='urn:ietf:params:xml:ns:netconf:base:1.0'", "message-id='1'")
        .addElement("rpc", "validation:groupingTest", null, "xmlns:validation='urn:org:bbf2:pma:validation'")
        .addElement("validation:groupingTest", "validation:groupContainer1", null, EMPTY_STRING)
        .addElement("validation:groupContainer1", "validation:groupContainerLeaf1", "test", EMPTY_STRING)
        .addElement("validation:groupingTest", "validation:groupContainer2", null, EMPTY_STRING)
        .addElement("validation:groupContainer2", "validation:groupContainerLeaf2", "test", EMPTY_STRING);
        NetconfRpcRequest request = new NetconfRpcRequest();
        request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(generator.buildXml()));
        request.setMessageId("1");
        TimingLogger.withStartAndFinish(() -> m_dummyRpcHandler2.validate(m_rpcConstraintParser, request));
    }

    @Test
    public void testRpc_LeafrefWithGrouping_InvalidLeafRefValue() throws Exception {
        // added leaf-ref node in DS
        String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + " <validation>hello</validation>"
                + "</validation>";
        EditConfigRequest request2 = createRequestFromString(requestXml2);
        request2.setMessageId("1");
        NetConfResponse response2 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request2, response2);
        assertTrue(response2.isOk());

        String responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                + "    <validation:validation>hello</validation:validation>"
                + "  </validation:validation>"
                + " </data>"
                + " </rpc-reply>";
        verifyGet(responseXml);

        try {
            XmlGenerator generator = new XmlGenerator();
            generator.addElement(null, "rpc", null, "xmlns='urn:ietf:params:xml:ns:netconf:base:1.0'", "message-id='1'")
            .addElement("rpc", "validation:groupingTest", null,
                    "xmlns:validation='urn:org:bbf2:pma:validation'")
            .addElement("validation:groupingTest", "validation:groupContainer1", null, EMPTY_STRING)
            .addElement("validation:groupContainer1", "validation:groupContainerLeaf1", "test", EMPTY_STRING);
            NetconfRpcRequest request = new NetconfRpcRequest();
            request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(generator.buildXml()));
            request.setMessageId("1");
            TimingLogger.withStartAndFinish(() -> m_dummyRpcHandler2.validate(m_rpcConstraintParser, request));
            failTest();
        } catch (Exception e) {
            assertTrue(e instanceof RpcValidationException);
            ValidationException cause = (ValidationException) e.getCause();
            assertEquals(1, cause.getRpcErrors().size());
            assertEquals("Missing required element test", cause.getRpcErrors().get(0).getErrorMessage());
            assertEquals("/validation:groupContainer1/validation:groupContainerLeaf1",
                    cause.getRpcErrors().get(0).getErrorPath());
            assertEquals("instance-required", cause.getRpcErrors().get(0).getErrorAppTag());
            assertEquals(NetconfRpcErrorTag.DATA_MISSING, cause.getRpcErrors().get(0).getErrorTag());
        }
    }

    @Test
    public void testLeafrefWithGrouping_WithInRpc() throws NetconfMessageBuilderException, RpcValidationException {

        // grouping leaf-ref validation should be done with in rpc input elements.
        XmlGenerator generator = new XmlGenerator();
        generator.addElement(null, "rpc", null, "xmlns='urn:ietf:params:xml:ns:netconf:base:1.0'", "message-id='1'")
        .addElement("rpc", "validation:leafref-grouping-rpc", null,
                "xmlns:validation='urn:org:bbf2:pma:validation'")
        .addElement("validation:leafref-grouping-rpc", "validation:leaf1", "test", EMPTY_STRING)
        .addElement("validation:leafref-grouping-rpc", "validation:grouping-leafref", "test", EMPTY_STRING);
        NetconfRpcRequest request = new NetconfRpcRequest();
        request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(generator.buildXml()));
        request.setMessageId("1");
        m_RpcInputHandler.validate(m_rpcConstraintParser, request);
    }

    @Test
    public void testLeafrefWithGrouping_WithInRpc_InvalidLeafRefValue() throws NetconfMessageBuilderException, RpcValidationException, ModelNodeInitException {
        try {
            XmlGenerator generator = new XmlGenerator();
            generator.addElement(null, "rpc", null, "xmlns='urn:ietf:params:xml:ns:netconf:base:1.0'", "message-id='1'")
            .addElement("rpc", "validation:leafref-grouping-rpc", null,
                    "xmlns:validation='urn:org:bbf2:pma:validation'")
            .addElement("validation:leafref-grouping-rpc", "validation:leaf1", "hello", EMPTY_STRING)
            .addElement("validation:leafref-grouping-rpc", "validation:grouping-leafref", "test", EMPTY_STRING);
            NetconfRpcRequest request = new NetconfRpcRequest();
            request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(generator.buildXml()));
            request.setMessageId("1");
            m_RpcInputHandler.validate(m_rpcConstraintParser, request);
            failTest();
        } catch (Exception e) {
            // leaf-ref validation failed, check the rpc error details
            assertTrue(e instanceof RpcValidationException);
            ValidationException cause = (ValidationException) e.getCause();
            assertEquals(1, cause.getRpcErrors().size());
            assertEquals("Missing required element test", cause.getRpcErrors().get(0).getErrorMessage());
            assertEquals("/validation:grouping-leafref", cause.getRpcErrors().get(0).getErrorPath());
            assertEquals("instance-required", cause.getRpcErrors().get(0).getErrorAppTag());
            assertEquals(NetconfRpcErrorTag.DATA_MISSING, cause.getRpcErrors().get(0).getErrorTag());
        }
    }

    @Test
    public void testRpLeafrefValidation_EvaluateXpathinInputElementsAndDS() throws Exception {
        // added leaf-ref node in DS
        String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<validation xmlns=\"urn:org:bbf2:pma:validation\">" + "   <leafref-validation-list>"
                + "       <name>sample</name>" + "       <ref-value>test</ref-value>" + "   </leafref-validation-list>"
                + "</validation>";
        EditConfigRequest request2 = createRequestFromString(requestXml2);
        request2.setMessageId("1");
        NetConfResponse response2 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request2, response2);
        assertTrue(response2.isOk());

        String responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>" + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                + "       <validation:leafref-validation-list>" + "           <validation:name>sample</validation:name>"
                + "           <validation:ref-value>test</validation:ref-value>"
                + "       </validation:leafref-validation-list>" + "  </validation:validation>" + " </data>"
                + " </rpc-reply>";
        verifyGet(responseXml);

        // leaf-ref xpath evaluation should be done rpc input elements as well as in Datastore

        XmlGenerator generator = new XmlGenerator();
        generator.addElement(null, "rpc", null, "xmlns='urn:ietf:params:xml:ns:netconf:base:1.0'", "message-id='1'")
        .addElement("rpc", "validation:leafref-grouping-rpc", null,
                "xmlns:validation='urn:org:bbf2:pma:validation'")
        .addElement("validation:leafref-grouping-rpc", "validation:leaf1", "sample", EMPTY_STRING)
        .addElement("validation:leafref-grouping-rpc", "validation:leafref-validation", "test", EMPTY_STRING);
        NetconfRpcRequest request = new NetconfRpcRequest();
        request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(generator.buildXml()));
        request.setMessageId("1");
        NetconfRpcRequest finalRequest = request;
        TimingLogger.withStartAndFinish(() -> m_RpcInputHandler.validate(m_rpcConstraintParser, finalRequest));

        // Negative case - Invalid leaf-ref value
        try {
            generator = new XmlGenerator();
            generator.addElement(null, "rpc", null, "xmlns='urn:ietf:params:xml:ns:netconf:base:1.0'", "message-id='1'")
            .addElement("rpc", "validation:leafref-grouping-rpc", null,
                    "xmlns:validation='urn:org:bbf2:pma:validation'")
            .addElement("validation:leafref-grouping-rpc", "validation:leaf1", "sample1", EMPTY_STRING)
            .addElement("validation:leafref-grouping-rpc", "validation:leafref-validation", "test",
                    EMPTY_STRING);
            request = new NetconfRpcRequest();
            request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(generator.buildXml()));
            request.setMessageId("1");
            NetconfRpcRequest finalRequest1 = request;
            DataStoreValidationUtil.setRootModelNodeAggregatorInCache(m_rootModelNodeAggregator);
            TimingLogger.withStartAndFinish(() -> m_RpcInputHandler.validate(m_rpcConstraintParser, finalRequest1));
            failTest();
        } catch (Exception e) {
            // leaf-ref validation failed, check the rpc error details
            assertTrue(e instanceof RpcValidationException);
            ValidationException cause = (ValidationException) e.getCause();
            assertEquals(1, cause.getRpcErrors().size());
            assertEquals("Missing required element test", cause.getRpcErrors().get(0).getErrorMessage());
            assertEquals("/validation:leafref-validation", cause.getRpcErrors().get(0).getErrorPath());
            assertEquals("instance-required", cause.getRpcErrors().get(0).getErrorAppTag());
            assertEquals(NetconfRpcErrorTag.DATA_MISSING, cause.getRpcErrors().get(0).getErrorTag());
        }
    }

    @Test
    public void testRpcCount() throws NetconfMessageBuilderException, RpcValidationException {
        {
            XmlGenerator generator = new XmlGenerator();
            generator.addElement(null, "rpc", null, "xmlns='urn:ietf:params:xml:ns:netconf:base:1.0'", "message-id='1'")
            .addElement("rpc", "validation:testRpc", null, "xmlns:validation='urn:org:bbf2:pma:validation'")
            .addElement( "validation:testRpc", "validation:countable", "test", EMPTY_STRING)
            .addElement( "validation:testRpc", "validation:countLeaf", "test", EMPTY_STRING);
            NetconfRpcRequest request = new NetconfRpcRequest();
            request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(generator.buildXml()));
            request.setMessageId("1");
            m_dummyRpcHandler.validate(m_rpcConstraintParser, request);
        }

        try{
            XmlGenerator generator = new XmlGenerator();
            generator.addElement(null, "rpc", null, "xmlns='urn:ietf:params:xml:ns:netconf:base:1.0'", "message-id='1'")
            .addElement("rpc", "validation:testRpc", null, "xmlns:validation='urn:org:bbf2:pma:validation'")
            .addElement( "validation:testRpc", "validation:countLeaf", "test", EMPTY_STRING);
            NetconfRpcRequest request = new NetconfRpcRequest();
            request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(generator.buildXml()));
            request.setMessageId("1");
            m_dummyRpcHandler.validate(m_rpcConstraintParser, request);
            fail("We expect an exception");
        } catch(RpcValidationException e){
            assertEquals("Violate when constraints: count(../countable) = 1",e.getRpcError().getErrorMessage());
        }
    }

    @Test
    public void testRpcInput_ConfigAsTrue() throws Exception {
        String xmlPath = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <validation:testConfigValidationInputRpc xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                + " 	<validation:leaf2>hello</validation:leaf2> "
                + " </validation:testConfigValidationInputRpc>"
                + "</rpc>";
        Element element = DocumentUtils.stringToDocument(xmlPath).getDocumentElement();
        NetconfRpcRequest request = new NetconfRpcRequest();
        request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element));
        request.setMessageId("1");
        m_configRpcInputHandler.validate(m_rpcConstraintParser, request);
    }

    @Test
    public void testRpcInput_ConfigAsFalse() throws Exception{
        String xmlPath = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <validation:testConfigValidationInputRpc xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                + " 	<validation:leaf1>hello</validation:leaf1> "
                + " </validation:testConfigValidationInputRpc>"
                + "</rpc>";
        Element element = DocumentUtils.stringToDocument(xmlPath).getDocumentElement();
        NetconfRpcRequest request = new NetconfRpcRequest();
        request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element));
        request.setMessageId("1");
        m_configRpcInputHandler.validate(m_rpcConstraintParser, request);
    }

    @Test
    public void testRpcOutput_ConfigAsTrue() throws Exception {
        String xmlPath = "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\""
                + " xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                + " 	<validation:leaf2>hello</validation:leaf2> "
                + " </rpc-reply>";

        Element returnValue = DocumentUtils.getDocumentElement(xmlPath);
        NetconfRpcResponse response = new NetconfRpcResponse();
        response.setMessageId("1");
        TestUtil.addOutputElements(returnValue, response);
        response.setRpcName(m_configOutputRpc);
        m_configRpcOutputHandler.validate(m_rpcConstraintParser, response);
        assertTrue(response.getErrors().size() == 0);
    }

    @Test
    public void testRpcOutput_ConfigAsFalse() throws Exception {
        String xmlPath = "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\""
                + " xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                + " 	<validation:leaf1>hello</validation:leaf1> "
                + " </rpc-reply>";
        Element returnValue = DocumentUtils.getDocumentElement(xmlPath);
        NetconfRpcResponse response = new NetconfRpcResponse();
        response.setMessageId("1");
        TestUtil.addOutputElements(returnValue, response);
        response.setRpcName(m_configOutputRpc);
        m_configRpcOutputHandler.validate(m_rpcConstraintParser, response);
        assertTrue(response.getErrors().size() == 0);
    }

    /**
     * When validating RPC output elements, when condition should work even-through xpath expression has different prefix/name-space.
     */
    @Test
    public void testAugmentedRpcOutputWithWhenConditionDefault_DifferentPrefix() throws Exception{
        String xmlPath = "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\""
                + " xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                + " <validation:group-name>group-name</validation:group-name> "
                + " <validation:group-id>group-id</validation:group-id> "
                + " <validation:test4>intent</validation:test4> "
                + " <validation:test5>test5</validation:test5> "
                + " <validation:container1>"
                + "<rpcoutput:intentContainer xmlns:rpcoutput=\"urn:org:bbf2:pma:rpc-output\" >"
                + "<rpcoutput:whenOnWorkType>intent</rpcoutput:whenOnWorkType>"
                + "</rpcoutput:intentContainer>"
                + " <validation:innerContainer>"
                + " <validation:innerList>"
                + "<validation:innerLeaf>hello</validation:innerLeaf>"
                + "</validation:innerList>"
                + "</validation:innerContainer>"
                + "</validation:container1>"
                + " </rpc-reply>"
                ;

        Element returnValue = DocumentUtils.getDocumentElement(xmlPath);
        NetconfRpcResponse response = new NetconfRpcResponse();
        response.setMessageId("1");
        TestUtil.addOutputElements(returnValue, response);
        response.setRpcName(RPC_NAME);
        m_dummyRpcHandler1.validate(m_rpcConstraintParser, response);
        assertTrue(response.getErrors().isEmpty());

        String xmlPath1 = "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\""
                + " xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                + " <validation:group-name>group-name</validation:group-name> "
                + " <validation:group-id>group-id</validation:group-id> "
                + " <validation:test4>test4</validation:test4> "
                + " <validation:test5>test5</validation:test5> "
                + " <validation:container1>"
                + "<rpcoutput:intentContainer xmlns:rpcoutput=\"urn:org:bbf2:pma:rpc-output\" >"
                + "<rpcoutput:whenOnWorkType>intent</rpcoutput:whenOnWorkType>"
                + "</rpcoutput:intentContainer>"
                + " <validation:innerContainer>"
                + " <validation:innerList>"
                + "<validation:innerLeaf>hello</validation:innerLeaf>"
                + "</validation:innerList>"
                + "</validation:innerContainer>"
                + "</validation:container1>"
                + " </rpc-reply>";

        Element returnValue1 = DocumentUtils.getDocumentElement(xmlPath1);
        NetconfRpcResponse response1 = new NetconfRpcResponse();
        response1.setMessageId("1");
        TestUtil.addOutputElements(returnValue1, response1);
        response1.setRpcName(RPC_NAME);
        m_dummyRpcHandler1.validate(m_rpcConstraintParser, response1);
        assertTrue(response1.getErrors().get(0).getErrorMessage().equals("Violate when constraints: ../validation:test4='intent'"));
        assertFalse(response1.getErrors().isEmpty());
    }

    @Test
    public void testAugmentedRpcOutputWithWhenCondition() throws Exception{
        String xmlPath = "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\""
                + " xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                + " <validation:intents>"
                + " <validation:intent>"
                + " <validation:target>ONT1</validation:target> "
                + " <validation:intent-type>copper-infra</validation:intent-type> "
                + " <validation:version>1</validation:version> "
                + " <validation:configuration>"
                + " <validation:copper-infra>"
                + " <validation:mac-learning>hello</validation:mac-learning> "
                + " </validation:copper-infra>"
                + " <validation:container1>"
                + " <validation:innerCaseLeaf/>"
                + " <validation:caseLeaf/>"
                + " </validation:container1>"
                + " </validation:configuration>"
                + " </validation:intent>"
                + " </validation:intents>"
                + " </rpc-reply>"
                ;

        Element returnValue = DocumentUtils.getDocumentElement(xmlPath);
        NetconfRpcResponse response = new NetconfRpcResponse();
        response.setMessageId("1");
        TestUtil.addOutputElements(returnValue, response);
        response.setRpcName(RPC_WITH_WHEN_AUGMENT_NAME);
        m_dummyRpcHandler5.validate(m_rpcConstraintParser, response);
        assertTrue(response.getErrors().isEmpty());

        xmlPath = "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\""
                + " xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                + " <validation:intents>"
                + " <validation:intent>"
                + " <validation:target>ONT1</validation:target> "
                + " <validation:intent-type>copper-infra1</validation:intent-type> "
                + " <validation:version>1</validation:version> "
                + " <validation:configuration>"
                + " <validation:copper-infra>"
                + " <validation:mac-learning>hello</validation:mac-learning> "
                + " </validation:copper-infra>"
                + " <validation:container1>"
                + " <validation:innerCaseLeaf/>"
                + " <validation:caseLeaf/>"
                + " </validation:container1>"
                + " </validation:configuration>"
                + " </validation:intent>"
                + " </validation:intents>"
                + " </rpc-reply>"
                ;

        returnValue = DocumentUtils.getDocumentElement(xmlPath);
        response = new NetconfRpcResponse();
        response.setMessageId("1");
        TestUtil.addOutputElements(returnValue, response);
        response.setRpcName(RPC_WITH_WHEN_AUGMENT_NAME);
        m_dummyRpcHandler5.validate(m_rpcConstraintParser, response);
        assertFalse(response.isOk());
        assertEquals("Violate when constraints: ../intent-type = 'copper-infra' and ../version = '1'", response.getErrors().get(0).getErrorMessage());
    }
    @Test
    public void testAugmentedRpcOutputWithWhenConditionOnMultipleListInstances() throws Exception{
        String xmlPath = "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\""
                + " xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                + " <validation:intents>"
                + " <validation:intent>"
                + " <validation:target>ONT2</validation:target> "
                + " <validation:intent-type>copper-infra1</validation:intent-type> "
                + " <validation:version>2</validation:version> "
                + " <validation:configuration>"
                + " <validation:copper-infra1>"
                + " <validation:mac-learning1>hello</validation:mac-learning1> "
                + " </validation:copper-infra1>"
                + " </validation:configuration>"
                + " </validation:intent>"
                + " <validation:intent>"
                + " <validation:target>ONT1</validation:target> "
                + " <validation:intent-type>copper-infra</validation:intent-type> "
                + " <validation:version>1</validation:version> "
                + " <validation:configuration>"
                + " <validation:copper-infra>"
                + " <validation:mac-learning>hello</validation:mac-learning> "
                + " </validation:copper-infra>"
                + " </validation:configuration>"
                + " </validation:intent>"
                + " </validation:intents>"
                + " </rpc-reply>"
                ;

        Element returnValue = DocumentUtils.getDocumentElement(xmlPath);
        NetconfRpcResponse response = new NetconfRpcResponse();
        response.setMessageId("1");
        TestUtil.addOutputElements(returnValue, response);
        response.setRpcName(RPC_WITH_WHEN_AUGMENT_NAME);
        m_dummyRpcHandler5.validate(m_rpcConstraintParser, response);
        assertTrue(response.getErrors().isEmpty());
    }

    @Test
    public void testUsesRpcOutputWithWhenCondition() throws Exception{
        String xmlPath = "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\""
                + " xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                + " <validation:intents>"
                + " <validation:intent>"
                + " <validation:target>ONT1</validation:target> "
                + " <validation:intent-type>copper-infra</validation:intent-type> "
                + " <validation:version>1</validation:version> "
                + " <validation:configuration>"
                + " <validation:copper-infra>"
                + " <validation:mac-learning>hello</validation:mac-learning> "
                + " </validation:copper-infra>"
                + " </validation:configuration>"
                + " </validation:intent>"
                + " </validation:intents>"
                + " </rpc-reply>"
                ;

        Element returnValue = DocumentUtils.getDocumentElement(xmlPath);
        NetconfRpcResponse response = new NetconfRpcResponse();
        response.setMessageId("1");
        TestUtil.addOutputElements(returnValue, response);
        response.setRpcName(RPC_WITH_WHEN_USES_NAME);
        m_dummyRpcHandler6.validate(m_rpcConstraintParser, response);
        assertTrue(response.getErrors().isEmpty());

        xmlPath = "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\""
                + " xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                + " <validation:intents>"
                + " <validation:intent>"
                + " <validation:target>ONT1</validation:target> "
                + " <validation:intent-type>copper-infra1</validation:intent-type> "
                + " <validation:version>1</validation:version> "
                + " <validation:configuration>"
                + " <validation:copper-infra>"
                + " <validation:mac-learning>hello</validation:mac-learning> "
                + " </validation:copper-infra>"
                + " </validation:configuration>"
                + " </validation:intent>"
                + " </validation:intents>"
                + " </rpc-reply>"
                ;

        returnValue = DocumentUtils.getDocumentElement(xmlPath);
        response = new NetconfRpcResponse();
        response.setMessageId("1");
        TestUtil.addOutputElements(returnValue, response);
        response.setRpcName(RPC_WITH_WHEN_USES_NAME);
        m_dummyRpcHandler6.validate(m_rpcConstraintParser, response);
        assertFalse(response.isOk());
        assertEquals("Violate when constraints: ../intent-type = 'copper-infra' and ../version = '1'", response.getErrors().get(0).getErrorMessage());
    }

    @Test
    public void testAugmentedRpcOutputWithWhenCondition_DifferentPrefix() throws Exception{
        String xmlPath = "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\""
                + " xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                + " <validation:group-name>group-name</validation:group-name> "
                + " <validation:group-id>group-id</validation:group-id> "
                + " <validation:test4>intent</validation:test4> "
                + " <validation:test5>test5</validation:test5> "
                + " <validation:container1>"
                + "<rpcoutput:intentContainer xmlns:rpcoutput=\"urn:org:bbf2:pma:rpc-output\" >"
                + " <rpcoutput:workType>bridge-mode</rpcoutput:workType>"
                + "<rpcoutput:whenOnWorkType>intent</rpcoutput:whenOnWorkType>"
                + "</rpcoutput:intentContainer>"
                + " <validation:innerContainer>"
                + " <validation:innerList>"
                + "<validation:innerLeaf>hello</validation:innerLeaf>"
                + "</validation:innerList>"
                + "</validation:innerContainer>"
                + "</validation:container1>"
                + " </rpc-reply>"
                ;

        Element returnValue = DocumentUtils.getDocumentElement(xmlPath);
        NetconfRpcResponse response = new NetconfRpcResponse();
        response.setMessageId("1");
        TestUtil.addOutputElements(returnValue, response);
        response.setRpcName(RPC_NAME);
        m_dummyRpcHandler1.validate(m_rpcConstraintParser, response);
        assertTrue(response.getErrors().isEmpty());

        xmlPath = "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\""
                + " xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                + " <validation:group-name>group-name</validation:group-name> "
                + " <validation:group-id>group-id</validation:group-id> "
                + " <validation:test4>intent</validation:test4> "
                + " <validation:test5>test5</validation:test5> "
                + " <validation:container1>"
                + " <rpcoutput:intentContainer xmlns:rpcoutput=\"urn:org:bbf2:pma:rpc-output\" >"
                + " <rpcoutput:workType>router-mode</rpcoutput:workType>"
                + " <rpcoutput:whenOnWorkType>intent</rpcoutput:whenOnWorkType>"
                + " </rpcoutput:intentContainer>"
                + " <validation:innerContainer>"
                + " <validation:innerList>"
                + "<validation:innerLeaf>hello</validation:innerLeaf>"
                + "</validation:innerList>"
                + "</validation:innerContainer>"
                + "</validation:container1>"
                + " </rpc-reply>"
                ;

        returnValue = DocumentUtils.getDocumentElement(xmlPath);
        response = new NetconfRpcResponse();
        response.setMessageId("1");
        TestUtil.addOutputElements(returnValue, response);
        response.setRpcName(RPC_NAME);
        m_dummyRpcHandler1.validate(m_rpcConstraintParser, response);
        assertFalse(response.isOk());
        assertEquals("Violate when constraints: ../workType = 'bridge-mode'", response.getErrors().get(0).getErrorMessage());
    }


    /**
     * When validating RPC output elements, must constraint condition should work even-through xpath expression has different prefix/name-space.
     */
    @Test
    public void testAugmentedRpcOutputWithMustConstraint_DifferentPrefix() throws Exception{
        String xmlPath = "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\""
                + " xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                + " <validation:group-name>group-name</validation:group-name> "
                + " <validation:group-id>group-id</validation:group-id> "
                + " <validation:test4>intent</validation:test4> "
                + " <validation:test5>test5</validation:test5> "
                + " <validation:container1>"
                + "<rpcoutput:intentContainer xmlns:rpcoutput=\"urn:org:bbf2:pma:rpc-output\" >"
                + "<rpcoutput:ipv4-rules>"
                + "<rpcoutput:name>IPv4</rpcoutput:name>"
                + "<rpcoutput:lower-port>1</rpcoutput:lower-port>"
                + "<rpcoutput:upper-port>10</rpcoutput:upper-port>"
                + "</rpcoutput:ipv4-rules>"
                + "</rpcoutput:intentContainer>"
                + " <validation:innerContainer>"
                + " <validation:innerList>"
                + "<validation:innerLeaf>hello</validation:innerLeaf>"
                + "</validation:innerList>"
                + "</validation:innerContainer>"
                + "</validation:container1>"
                + " </rpc-reply>"
                ;

        Element returnValue = DocumentUtils.getDocumentElement(xmlPath);
        NetconfRpcResponse response = new NetconfRpcResponse();
        response.setMessageId("1");
        TestUtil.addOutputElements(returnValue, response);
        response.setRpcName(RPC_NAME);
        m_dummyRpcHandler1.validate(m_rpcConstraintParser, response);
        assertTrue(response.getErrors().isEmpty());

        String xmlPath1 = "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\""
                + " xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                + " <validation:group-name>group-name</validation:group-name> "
                + " <validation:group-id>group-id</validation:group-id> "
                + " <validation:test4>test4</validation:test4> "
                + " <validation:test5>test5</validation:test5> "
                + " <validation:container1>"
                + "<rpcoutput:intentContainer xmlns:rpcoutput=\"urn:org:bbf2:pma:rpc-output\" >"
                + "<rpcoutput:ipv4-rules>"
                + "<rpcoutput:name>IPv4</rpcoutput:name>"
                + "<rpcoutput:lower-port>1</rpcoutput:lower-port>"
                + "<rpcoutput:upper-port>10</rpcoutput:upper-port>"
                + "</rpcoutput:ipv4-rules>"
                + "</rpcoutput:intentContainer>"
                + " <validation:innerContainer>"
                + " <validation:innerList>"
                + "<validation:innerLeaf>hello</validation:innerLeaf>"
                + "</validation:innerList>"
                + "</validation:innerContainer>"
                + "</validation:container1>"
                + " </rpc-reply>"
                ;

        Element returnValue1 = DocumentUtils.getDocumentElement(xmlPath1);
        NetconfRpcResponse response1 = new NetconfRpcResponse();
        response1.setMessageId("1");
        TestUtil.addOutputElements(returnValue1, response1);
        response1.setRpcName(RPC_NAME);
        m_dummyRpcHandler1.validate(m_rpcConstraintParser, response1);
        assertEquals("Violate when constraints: ../validation:test4='intent'", response1.getErrors().get(0).getErrorMessage());
        assertFalse(response1.getErrors().isEmpty());
    }

    @Test
    public void testAugmentedRpcOutputWithDifferentPrefix_MustConstraintFail() throws Exception{
        String xmlPath = "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\""
                + " xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                + " <validation:group-name>group-name</validation:group-name> "
                + " <validation:group-id>group-id</validation:group-id> "
                + " <validation:test4>intent</validation:test4> "
                + " <validation:test5>test5</validation:test5> "
                + " <validation:container1>"
                + "<rpcoutput:intentContainer xmlns:rpcoutput=\"urn:org:bbf2:pma:rpc-output\" >"
                + "<rpcoutput:ipv4-rules>"
                + "<rpcoutput:name>IPv4</rpcoutput:name>"
                + "<rpcoutput:lower-port>5</rpcoutput:lower-port>"
                + "<rpcoutput:upper-port>1</rpcoutput:upper-port>"
                + "</rpcoutput:ipv4-rules>"
                + "</rpcoutput:intentContainer>"
                + " <validation:innerContainer>"
                + " <validation:innerList>"
                + "<validation:innerLeaf>hello</validation:innerLeaf>"
                + "</validation:innerList>"
                + "</validation:innerContainer>"
                + "</validation:container1>"
                + " </rpc-reply>"
                ;

        Element returnValue = DocumentUtils.getDocumentElement(xmlPath);
        NetconfRpcResponse response = new NetconfRpcResponse();
        response.setMessageId("1");
        TestUtil.addOutputElements(returnValue, response);
        response.setRpcName(RPC_NAME);
        m_dummyRpcHandler1.validate(m_rpcConstraintParser, response);
        assertFalse(response.isOk());
        assertEquals("Violate must constraints: . >= ../lower-port", response.getErrors().get(0).getErrorMessage());
    }

    @Test
    public void testAugmentedRpcOutputWithMustConstraint_withoutPrefix() throws Exception{
        String xmlPath = "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\""
                + " xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                + " <validation:group-name>group-name</validation:group-name> "
                + " <validation:group-id>group-id</validation:group-id> "
                + " <validation:test4>intent</validation:test4> "
                + " <validation:test5>test5</validation:test5> "
                + " <validation:container1>"
                + " <intentContainer xmlns=\"urn:org:bbf2:pma:rpc-output\" >"
                + "<ipv4-rules>"
                + "<name>IPv4</name>"
                + "<lower-port>1</lower-port>"
                + "<upper-port>10</upper-port>"
                + "</ipv4-rules>"
                + "</intentContainer>"
                + " <validation:innerContainer>"
                + " <validation:innerList>"
                + "<validation:innerLeaf>hello</validation:innerLeaf>"
                + "</validation:innerList>"
                + "</validation:innerContainer>"
                + "</validation:container1>"
                + " </rpc-reply>"
                ;

        Element returnValue = DocumentUtils.getDocumentElement(xmlPath);
        NetconfRpcResponse response = new NetconfRpcResponse();
        response.setMessageId("1");
        TestUtil.addOutputElements(returnValue, response);
        response.setRpcName(RPC_NAME);
        m_dummyRpcHandler1.validate(m_rpcConstraintParser, response);
        assertTrue(response.getErrors().isEmpty());
    }

    @Test
    public void testAugmentedRpcOutputWithMustConstraintFail_withoutPrefix() throws Exception{
        String xmlPath = "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\""
                + " xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                + " <validation:group-name>group-name</validation:group-name> "
                + " <validation:group-id>group-id</validation:group-id> "
                + " <validation:test4>intent</validation:test4> "
                + " <validation:test5>test5</validation:test5> "
                + " <validation:container1>"
                + " <intentContainer xmlns=\"urn:org:bbf2:pma:rpc-output\" >"
                + "	<ipv4-rules>"
                + "	<name>IPv4</name>"
                + "	<lower-port>15</lower-port>"
                + "	<upper-port>10</upper-port>"
                + "	</ipv4-rules>"
                + "	</intentContainer>"
                + " <validation:innerContainer>"
                + " <validation:innerList>"
                + "<validation:innerLeaf>hello</validation:innerLeaf>"
                + "</validation:innerList>"
                + "</validation:innerContainer>"
                + "</validation:container1>"
                + " </rpc-reply>"
                ;

        Element returnValue = DocumentUtils.getDocumentElement(xmlPath);
        NetconfRpcResponse response = new NetconfRpcResponse();
        response.setMessageId("1");
        TestUtil.addOutputElements(returnValue, response);
        response.setRpcName(RPC_NAME);
        m_dummyRpcHandler1.validate(m_rpcConstraintParser, response);
        assertFalse(response.isOk());
        assertEquals("Violate must constraints: . >= ../lower-port", response.getErrors().get(0).getErrorMessage());
        assertEquals("must-violation", response.getErrors().get(0).getErrorAppTag());
        assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, response.getErrors().get(0).getErrorTag());
    }

    @Test
    public void testAugmentedRpcOutputWithWhenConstraints_withoutPrefix() throws Exception{
        String xmlPath = "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\""
                + " xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                + " <validation:group-name>group-name</validation:group-name> "
                + " <validation:group-id>group-id</validation:group-id> "
                + " <validation:test4>intent</validation:test4> "
                + " <validation:test5>test5</validation:test5> "
                + " <validation:container1>"
                + " <intentContainer xmlns=\"urn:org:bbf2:pma:rpc-output\" >"
                + " <workType>bridge-mode</workType>"
                + " <whenOnWorkType>intent</whenOnWorkType>"
                + " </intentContainer>"
                + " <validation:innerContainer>"
                + " <validation:innerList>"
                + "<validation:innerLeaf>hello</validation:innerLeaf>"
                + "</validation:innerList>"
                + "</validation:innerContainer>"
                + "</validation:container1>"
                + " </rpc-reply>"
                ;

        Element returnValue = DocumentUtils.getDocumentElement(xmlPath);
        NetconfRpcResponse response = new NetconfRpcResponse();
        response.setMessageId("1");
        TestUtil.addOutputElements(returnValue, response);
        response.setRpcName(RPC_NAME);
        m_dummyRpcHandler1.validate(m_rpcConstraintParser, response);
        assertTrue(response.getErrors().isEmpty());

        xmlPath = "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\""
                + " xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                + " <validation:group-name>group-name</validation:group-name> "
                + " <validation:group-id>group-id</validation:group-id> "
                + " <validation:test4>intent</validation:test4> "
                + " <validation:test5>test5</validation:test5> "
                + " <validation:container1>"
                + " <intentContainer xmlns=\"urn:org:bbf2:pma:rpc-output\" >"
                + " <workType>router-mode</workType>"
                + " <whenOnWorkType>intent</whenOnWorkType>"
                + " </intentContainer>"
                + " <validation:innerContainer>"
                + " <validation:innerList>"
                + "<validation:innerLeaf>hello</validation:innerLeaf>"
                + "</validation:innerList>"
                + "</validation:innerContainer>"
                + "</validation:container1>"
                + " </rpc-reply>"
                ;

        returnValue = DocumentUtils.getDocumentElement(xmlPath);
        response = new NetconfRpcResponse();
        response.setMessageId("1");
        TestUtil.addOutputElements(returnValue, response);
        response.setRpcName(RPC_NAME);
        m_dummyRpcHandler1.validate(m_rpcConstraintParser, response);
        assertFalse(response.isOk());
        assertEquals("Violate when constraints: ../workType = 'bridge-mode'", response.getErrors().get(0).getErrorMessage());
        assertEquals("when-violation", response.getErrors().get(0).getErrorAppTag());
        assertEquals(NetconfRpcErrorTag.UNKNOWN_ELEMENT, response.getErrors().get(0).getErrorTag());
    }

    @Test
    public void testRpcEmptyInput() throws Exception {

        String xmlPath = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <validation:emptyInput xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                + " </validation:emptyInput>" 
                + "</rpc>";
        Element element = DocumentUtils.stringToDocument(xmlPath).getDocumentElement();
        NetconfRpcRequest request = new NetconfRpcRequest();
        request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element));
        request.setMessageId("1");
        TimingLogger.withStartAndFinish(() -> m_dummyEmptyRpcHandler.validate(m_rpcConstraintParser, request));
    }

    @Test
    public void testRpcFailureCaseOfIfFeature() throws Exception {

        String xmlPath = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <validation:testIfFeature xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                + " <validation:testLeafName>test</validation:testLeafName>" 
                + " </validation:testIfFeature>"
                + "</rpc>";
        Element element = DocumentUtils.stringToDocument(xmlPath).getDocumentElement();
        NetconfRpcRequest request = new NetconfRpcRequest();
        request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element));
        request.setMessageId("1");
        try {
            TimingLogger.withStartAndFinish(() -> m_dummyIfFeatureRpcHandler.validate(m_rpcConstraintParser, request));
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof RpcValidationException);
            ValidationException cause = (ValidationException) e.getCause();
            assertEquals(NetconfRpcErrorTag.UNKNOWN_ELEMENT, cause.getRpcErrors().get(0).getErrorTag());
            assertTrue(e.getMessage().contains("An unexpected element 'testLeafName' is present"));
        }
    }

    @Test
    public void testRpcDefaultValue() throws Exception {

        String xmlPath = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <validation:testDefault xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                + " </validation:testDefault>" 
                + "</rpc>";
        Element element = DocumentUtils.stringToDocument(xmlPath).getDocumentElement();
        NetconfRpcRequest request = new NetconfRpcRequest();
        request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element));
        request.setMessageId("1");
        TimingLogger.withStartAndFinish(() -> m_dummyDefaultValueRpcHandler.validate(m_rpcConstraintParser, request));
    }

    @Test
    public void testRpcDefaultValue1() throws Exception {

        String xmlPath = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <validation:testDefault xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                + " <validation:leaf1>test50</validation:leaf1>"
                + " </validation:testDefault>" 
                + "</rpc>";
        Element element = DocumentUtils.stringToDocument(xmlPath).getDocumentElement();
        NetconfRpcRequest request = new NetconfRpcRequest();
        request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element));
        request.setMessageId("1");
        TimingLogger.withStartAndFinish(() -> m_dummyDefaultValueRpcHandler.validate(m_rpcConstraintParser, request));
    }

    @Test
    public void testRpcDefaultValueNegativeCase() throws Exception {

        String xmlPath = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <validation:testDefault xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                + " <validation:leaf2>test50</validation:leaf2>" 
                + " </validation:testDefault>" 
                + "</rpc>";
        Element element = DocumentUtils.stringToDocument(xmlPath).getDocumentElement();
        NetconfRpcRequest request = new NetconfRpcRequest();
        request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element));
        request.setMessageId("1");
        try {
            TimingLogger.withStartAndFinish(() -> m_dummyDefaultValueRpcHandler.validate(m_rpcConstraintParser, request));
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof RpcValidationException);
            ValidationException cause = (ValidationException) e.getCause();
            assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, cause.getRpcErrors().get(0).getErrorTag());
            assertTrue(e.getMessage().contains("Violate must constraints: ../testLeafDefault = \"test10\""));
        }
    }

    @Test
    public void testRpcWithtReMatchFunction() throws Exception {
        String requestXml1 = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <validation:testRematch xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                + " <validation:testRematchName>88</validation:testRematchName>"
                + " </validation:testRematch>"
                + "</rpc>";
        Element element = DocumentUtils.stringToDocument(requestXml1).getDocumentElement();
        NetconfRpcRequest request = new NetconfRpcRequest();
        request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element));
        request.setMessageId("1");
        TimingLogger.withStartAndFinish(() -> m_dummyRematchValueRpcHandler.validate(m_rpcConstraintParser, request));
    }

    @Test
    public void testRpcErrorcaseForReMatchFunction() throws Exception {
        String requestXml1 = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <validation:testRematch xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                + " <validation:testRematchName>ab</validation:testRematchName>" 
                + " </validation:testRematch>"
                + "</rpc>";
        Element element = DocumentUtils.stringToDocument(requestXml1).getDocumentElement();
        NetconfRpcRequest request = new NetconfRpcRequest();
        request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element));
        request.setMessageId("1");
        try {
            TimingLogger.withStartAndFinish(() -> m_dummyRematchValueRpcHandler.validate(m_rpcConstraintParser, request));
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof RpcValidationException);
            ValidationException cause = (ValidationException) e.getCause();
            assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, cause.getRpcErrors().get(0).getErrorTag());
            assertTrue(e.getMessage().contains("Violate must constraints: re-match(current(),\"[0-9]{2}\")"));
        }
    }

    @Test
    public void testRpcInputAnyXml() throws Exception {
        String rpcRequest1 = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <whenMust:testRpcInputAnyXml xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + " <whenMust:leaf1>test</whenMust:leaf1>" 
                + " <whenMust:extraInfo>" 
                + "   <someData>" 
                + "    <about>"
                + "    <example/> <!--anyXml in container testRpcInputAnyXML-->" 
                + "   </about>" 
                + "   </someData>"
                + "   </whenMust:extraInfo>" 
                + " </whenMust:testRpcInputAnyXml>" 
                + " </rpc>";
        Element element1 = DocumentUtils.stringToDocument(rpcRequest1).getDocumentElement();
        NetconfRpcRequest netconfrequest1 = new NetconfRpcRequest();
        netconfrequest1.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element1));
        netconfrequest1.setMessageId("1");
        TimingLogger.withStartAndFinish(() -> m_dummyRpcInputAnyXml.validate(m_rpcConstraintParser, netconfrequest1));
    }

    @Test
    public void testRpcOutputAnyXml() throws Exception {
        String rpcResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <whenMust:container1 xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + " <whenMust:leaf1>test</whenMust:leaf1>" 
                + " <whenMust:extraInfo>" 
                + "   <someData>" 
                + "    <about>"
                + "    <example/> <!--anyXml in container testRpcOutputAnyXML-->" 
                + "   </about>" 
                + "   </someData>"
                + "   </whenMust:extraInfo>" 
                + "  </whenMust:container1>" 
                + " </rpc-reply>";

        Element element1 = DocumentUtils.stringToDocument(rpcResponse).getDocumentElement();
        NetconfRpcResponse netconfResponse = new NetconfRpcResponse();
        netconfResponse.setMessageId("1");
        TestUtil.addOutputElements(element1, netconfResponse);
        netconfResponse.setRpcName(new RpcName("urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction", "testRpcOutputAnyXml"));
        m_dummyRpcOutputAnyXml.validate(m_rpcConstraintParser, netconfResponse);
        assertTrue(netconfResponse.getErrors().isEmpty());
    }

    @Test
    public void testRpcOutputFailureAnyXml() throws Exception {
        String rpcResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <whenMust:container1 xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + " <whenMust:leaf1>test</whenMust:leaf1>" 
                + " <whenMust:extraInfo1>" 
                + "   <someData>" 
                + "    <about>"
                + "    <example/> <!--anyXml in container testRpcOutputAnyXML-->" 
                + "   </about>" 
                + "   </someData>"
                + "   </whenMust:extraInfo1>"
                + "  </whenMust:container1>" 
                + " </rpc-reply>";

        Element element1 = DocumentUtils.stringToDocument(rpcResponse).getDocumentElement();
        NetconfRpcResponse netconfResponse = new NetconfRpcResponse();
        netconfResponse.setMessageId("1");
        TestUtil.addOutputElements(element1, netconfResponse);
        netconfResponse.setRpcName(new RpcName("urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction", "testRpcOutputAnyXml"));
        m_dummyRpcOutputAnyXml.validate(m_rpcConstraintParser, netconfResponse);
        assertEquals(NetconfRpcErrorTag.UNKNOWN_ELEMENT, netconfResponse.getErrors().get(0).getErrorTag());
        assertTrue(netconfResponse.getErrors().get(0).getErrorMessage().contains("An unexpected element 'extraInfo1' is present"));        
        assertFalse(netconfResponse.getErrors().isEmpty());
    }

    @Test
    public void testRpcOutputFailureCaseOfIfFeature() throws Exception {
        String rpcResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <validation:test xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                + " <validation:testLeafName>test</validation:testLeafName>" 
                + " </validation:test>" 
                + "</rpc-reply>";
        Element element1 = DocumentUtils.stringToDocument(rpcResponse).getDocumentElement();
        NetconfRpcResponse netconfResponse = new NetconfRpcResponse();
        netconfResponse.setMessageId("1");
        TestUtil.addOutputElements(element1, netconfResponse);
        netconfResponse.setRpcName(new RpcName("urn:org:bbf2:pma:validation", "testOutputIfFeature"));
        m_dummyIfFeatureOutputRpcHandler.validate(m_rpcConstraintParser, netconfResponse);
        assertEquals(NetconfRpcErrorTag.UNKNOWN_ELEMENT, netconfResponse.getErrors().get(0).getErrorTag());
        assertTrue(netconfResponse.getErrors().get(0).getErrorMessage().contains("An unexpected element 'testLeafName' is present"));
        assertFalse(netconfResponse.getErrors().isEmpty());
    }

    @Test
    public void testRpcOutputDefaultValue() throws Exception {
        String rpcResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <validation:outputDefault xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                + " <validation:testLeafDefault>test</validation:testLeafDefault>"
                + " <validation:leaf1>test</validation:leaf1>" 
                + " </validation:outputDefault>" 
                + "</rpc-reply>";
        Element element1 = DocumentUtils.stringToDocument(rpcResponse).getDocumentElement();
        NetconfRpcResponse netconfResponse = new NetconfRpcResponse();
        netconfResponse.setMessageId("1");
        TestUtil.addOutputElements(element1, netconfResponse);
        netconfResponse.setRpcName(new RpcName("urn:org:bbf2:pma:validation", "testOutputDefault"));
        m_dummyDefaultValueOutputRpcHandler.validate(m_rpcConstraintParser, netconfResponse);
    }

    @Test
    public void testRpcOutputDefaultValueNegativeCase() throws Exception {
        String rpcResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <validation:outputDefault xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                + " <validation:testLeafDefault>test25</validation:testLeafDefault>"
                + " <validation:leaf2>test50</validation:leaf2>" 
                + " </validation:outputDefault>" 
                + "</rpc-reply>";
        Element element1 = DocumentUtils.stringToDocument(rpcResponse).getDocumentElement();
        NetconfRpcResponse netconfResponse = new NetconfRpcResponse();
        netconfResponse.setMessageId("1");
        TestUtil.addOutputElements(element1, netconfResponse);
        netconfResponse.setRpcName(new RpcName("urn:org:bbf2:pma:validation", "testOutputDefault"));
        m_dummyDefaultValueOutputRpcHandler.validate(m_rpcConstraintParser, netconfResponse);
        assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, netconfResponse.getErrors().get(0).getErrorTag());
        assertEquals("Violate must constraints: ../testLeafDefault = \"test10\"", netconfResponse.getErrors().get(0).getErrorMessage());
        assertFalse(netconfResponse.getErrors().isEmpty());
    }

    @Test
    public void testRpcOutputWithtReMatchFunction() throws Exception {
        String rpcResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <validation:outputRematch xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                + " <validation:testRematchName>88</validation:testRematchName>" 
                + " </validation:outputRematch>"
                + "</rpc-reply>";
        Element element1 = DocumentUtils.stringToDocument(rpcResponse).getDocumentElement();
        NetconfRpcResponse netconfResponse = new NetconfRpcResponse();
        netconfResponse.setMessageId("1");
        TestUtil.addOutputElements(element1, netconfResponse);
        netconfResponse.setRpcName(new RpcName("urn:org:bbf2:pma:validation", "testOutputRematch"));
        m_dummyRematchValueOutputRpcHandler.validate(m_rpcConstraintParser, netconfResponse);
    }

    @Test
    public void testRpcOutputErrorcaseForReMatchFunction() throws Exception {
        String rpcResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <validation:outputRematch xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                + " <validation:testRematchName>ab</validation:testRematchName>" 
                + " </validation:outputRematch>"
                + "</rpc-reply>";
        Element element1 = DocumentUtils.stringToDocument(rpcResponse).getDocumentElement();
        NetconfRpcResponse netconfResponse = new NetconfRpcResponse();
        netconfResponse.setMessageId("1");
        TestUtil.addOutputElements(element1, netconfResponse);
        netconfResponse.setRpcName(new RpcName("urn:org:bbf2:pma:validation", "testOutputRematch"));
        m_dummyRematchValueOutputRpcHandler.validate(m_rpcConstraintParser, netconfResponse);
        assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, netconfResponse.getErrors().get(0).getErrorTag());
        assertEquals("Violate must constraints: re-match(current(),\"[0-9]{2}\")", netconfResponse.getErrors().get(0).getErrorMessage());
        assertFalse(netconfResponse.getErrors().isEmpty());
    }
}
