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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.RpcRequestConstraintParser;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.validation.AbstractDataStoreValidatorTest;
import org.broadband_forum.obbaa.netconf.mn.fwk.tests.utils.XmlGenerator;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.w3c.dom.Element;

import org.broadband_forum.obbaa.netconf.api.NetconfMessage;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcResponse;
import org.broadband_forum.obbaa.netconf.api.messages.RpcName;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcPayloadConstraintParser;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcRequestHandler;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcValidationException;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;

public class RpcConstraintParserTest extends AbstractDataStoreValidatorTest {

    private DummyRpcHandler m_dummyRpcHandler;
    private DummyRpcHandler m_dummyRpcHandler1;
    private DummyRpcHandler m_dummyRpcHandler2;
    private DummyRpcHandler m_dummyRpcHandler3;
    private RpcPayloadConstraintParser m_rpcConstraintParser;
    private ModelNodeDataStoreManager m_modelNodeDsm;
    private static final RpcName RPC_NAME = new RpcName("urn:org:bbf:pma:validation", "testRpcOutput");
    private static final String m_RPC_STR = "<rename-device-holder xmlns=\"http://www.test-company" +
            ".com/management-solutions/anv-device-holders\">\n" +
            "  <old-device-holder-name>OLT1</old-device-holder-name>\n" +
            "  <new-device-holder-name>OLTBlah</new-device-holder-name>\n" +
            "</rename-device-holder>";

    private void failTest() {
        fail("we were expecting some exception. If we reach here!!Well we are on the wrong road");
    }

    private void failTest(Exception e) {
        fail("we were not expecting any exception. If we reach here!!Well we are on the wrong road-" + e.getMessage());
    }

    @Before
    public void setup() {
        m_dummyRpcHandler = new DummyRpcHandler(new RpcName("urn:org:bbf:pma:validation", "testRpc"));
        m_dummyRpcHandler1 = new DummyRpcHandler(new RpcName("urn:org:bbf:pma:validation", "testRpcOutput"));
        m_dummyRpcHandler2 = new DummyRpcHandler(new RpcName("urn:org:bbf:pma:validation", "groupingTest"));
        m_dummyRpcHandler3 = new DummyRpcHandler(new RpcName("urn:org:bbf:pma:validation", "leafRefTest"));
        m_modelNodeDsm = super.m_modelNodeDsm;
        m_rpcConstraintParser = new RpcRequestConstraintParser(m_schemaRegistry, m_modelNodeDsm, m_expValidator);
    }

    @Test
    public void testRpcDSLeafRef() throws Exception {
        String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>					" +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">			" +
                " <rpcRefLeaf>hello</rpcRefLeaf>" +
                "</validation>													";

        getModelNode();
        EditConfigRequest request2 = createRequestFromString(requestXml2);
        request2.setMessageId("1");
        NetConfResponse response2 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request2, response2);
        assertTrue(response2.isOk());

        String xmlPath = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                " <validation:leafRefTest xmlns:validation=\"urn:org:bbf:pma:validation\">" +
                " <validation:leaf1>hello</validation:leaf1> " +
                " </validation:leafRefTest>" +
                "</rpc>";
        Element element = DocumentUtils.stringToDocument(xmlPath).getDocumentElement();
        NetconfRpcRequest request = new NetconfRpcRequest();
        request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element));
        request.setMessageId("1");
        m_dummyRpcHandler3.validate(m_rpcConstraintParser, request);
    }

    @Test
    public void testInvalidRpc() throws Exception {
        NetconfRpcRequest rpc = new NetconfRpcRequest();
        rpc.setRpcInput(DocumentUtils.stringToDocument(m_RPC_STR).getDocumentElement());
        SchemaRegistry registry = mock(SchemaRegistry.class);
        RpcRequestHandler handler = new DummyRpcHandler(rpc.getRpcName());
        ModelNodeDataStoreManager dsm = mock(ModelNodeDataStoreManager.class);
        when(registry.getRpcDefinitions()).thenReturn(new ArrayList<RpcDefinition>());
        RpcRequestConstraintParser parser = new RpcRequestConstraintParser(registry, dsm, m_expValidator);
        try {
            handler.validate(parser, (NetconfMessage) rpc);
            fail();
        } catch (RpcValidationException exception) {
            assertEquals("An unexpected element rename-device-holder is present", exception.getRpcError()
                    .getErrorMessage());
        }
    }

    @Test
    public void testRpcLeaf() throws Exception {
        try {
            String xmlPath = "#validation:testRpc[@xmlns:validation='urn:org:bbf:pma:validation', parent='rpc']"
                    + "#validation:data-status[parent='validation:testRpc', value='success']";
            Element element = buildRpc(xmlPath);
            NetconfRpcRequest request = new NetconfRpcRequest();
            request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element));
            request.setMessageId("1");
            m_dummyRpcHandler.validate(m_rpcConstraintParser, request);
        } catch (Exception e) {
            failTest(e);
        }
    }


    @Test
    public void testRpcMandatoryOutputLeaf() throws Exception {
        RequestScope.setEnableThreadLocalInUT(true);
        String xmlPath = "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\""
                + " xmlns:validation=\"urn:org:bbf:pma:validation\">"
                + " <validation:group-name >group-name</validation:group-name> "
                + " <validation:group-id >group-id</validation:group-id> "
                + " </rpc-reply>";

        Element returnValue = DocumentUtils.getDocumentElement(xmlPath);
        NetconfRpcResponse response = new NetconfRpcResponse();
        response.setMessageId("1");
        response.setRpcName(RPC_NAME);
        TestUtil.addOutputElements(returnValue, response);
        m_dummyRpcHandler1.validate(m_rpcConstraintParser, response);
        assertEquals(0, response.getErrors().size());

        Map<?, ?> requestScope = (Map<?, ?>) RequestScope.getCurrentScope().getFromCache
                ("MANDATORY_TYPE_VALIDATION_CACHE");
        assertEquals(1, requestScope.size());
        RequestScope.setEnableThreadLocalInUT(false);
    }

    @Test
    public void testRpcOutputContainer() throws Exception {
        String xmlPath = "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\""
                + " xmlns:validation=\"urn:org:bbf:pma:validation\">"
                + " <validation:group-name >group-name</validation:group-name> "
                + " <validation:group-id >group-id</validation:group-id> "
                + "<validation:container1>"
                + " <validation:innerContainer>"
                + " <validation:innerList>"
                + "<validation:innerLeaf>hello</validation:innerLeaf>"
                + "</validation:innerList>"
                + "</validation:innerContainer>"
                + "</validation:container1>"
                + " </rpc-reply>";

        Element returnValue = DocumentUtils.getDocumentElement(xmlPath);
        NetconfRpcResponse response = new NetconfRpcResponse();
        response.setMessageId("1");
        TestUtil.addOutputElements(returnValue, response);
        response.setRpcName(RPC_NAME);
        m_dummyRpcHandler1.validate(m_rpcConstraintParser, response);
        assertTrue(response.getErrors().isEmpty());

        xmlPath = "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\""
                + " xmlns:validation=\"urn:org:bbf:pma:validation\">"
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
    public void testRpcOutputLeaf() throws Exception {
        String xmlPath = "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\""
                + " xmlns:validation=\"urn:org:bbf:pma:validation\">"
                + " <validation:test1>9</validation:test1>"
                + " <validation:group-name >group-name</validation:group-name> "
                + " <validation:group-id >group-id</validation:group-id> "
                + " </rpc-reply>";
        Element returnValue = DocumentUtils.getDocumentElement(xmlPath);
        NetconfRpcResponse response = new NetconfRpcResponse();
        response.setMessageId("1");
        TestUtil.addOutputElements(returnValue, response);
        response.setRpcName(RPC_NAME);
        m_dummyRpcHandler1.validate(m_rpcConstraintParser, response);
        assertTrue(response.getErrors().size() == 0);

        String xmlPath1 = "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\""
                + " xmlns:validation=\"urn:org:bbf:pma:validation\">"
                + " <validation:test1>11</validation:test1>"
                + " <validation:test2>H</validation:test2>"
                + " <validation:group-name >group-name</validation:group-name> "
                + " <validation:group-id >group-id</validation:group-id> "
                + " </rpc-reply>";
        returnValue = DocumentUtils.getDocumentElement(xmlPath1);
        response = new NetconfRpcResponse();
        response.setMessageId("1");
        TestUtil.addOutputElements(returnValue, response);
        response.setRpcName(RPC_NAME);
        m_dummyRpcHandler1.validate(m_rpcConstraintParser, response);
        assertTrue(response.getErrors().size() == 0);

        String xmlPath2 = "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\""
                + " xmlns:validation=\"urn:org:bbf:pma:validation\">"
                + " <validation:test1>9</validation:test1>"
                + " <validation:test2>H</validation:test2>"
                + " <validation:group-name >group-name</validation:group-name> "
                + " <validation:group-id >group-id</validation:group-id> "
                + " </rpc-reply>";
        returnValue = DocumentUtils.getDocumentElement(xmlPath2);
        response = new NetconfRpcResponse();
        response.setMessageId("1");
        TestUtil.addOutputElements(returnValue, response);
        response.setRpcName(RPC_NAME);
        try {
            m_dummyRpcHandler1.validate(m_rpcConstraintParser, response);
        } catch (Exception e) {
            fail();
        }
        assertEquals(null, response.getData());
        assertEquals(1, response.getErrors().size());
        assertTrue(response.getErrors().get(0).getErrorMessage().contains("Violate when constraints"));

        String xmlPath4 = "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\""
                + " xmlns:validation=\"urn:org:bbf:pma:validation\">"
                + " <validation:test1>10</validation:test1>"
                + " <validation:test3>H</validation:test3>"
                + " <validation:group-name >group-name</validation:group-name> "
                + " <validation:group-id >group-id</validation:group-id> "
                + " </rpc-reply>";
        returnValue = DocumentUtils.getDocumentElement(xmlPath4);
        response = new NetconfRpcResponse();
        response.setMessageId("1");
        TestUtil.addOutputElements(returnValue, response);
        response.setRpcName(RPC_NAME);
        m_dummyRpcHandler1.validate(m_rpcConstraintParser, response);

        assertFalse(response.isOk());
        assertTrue(response.getErrors().get(0).getErrorMessage().contains("Violate when constraints"));

    }

    @Test(expected = ValidationException.class)
    public void testRpcInvalidLeaf() throws Throwable {
        try {
            String xmlPath = "#validation:testRpc[@xmlns:validation='urn:org:bbf:pma:validation', parent='rpc']"
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
                    .contains("Value \"succes\" is an invalid value! Expected values: [success, failed, in-progress]"));
            throw e.getCause();
        }
    }

    @Test
    public void testRpcMustLeaf() throws Exception {
        try {
            // data-status = success case
            String xmlPath = "#validation:testRpc[@xmlns:validation='urn:org:bbf:pma:validation', parent='null']"
                    + "#validation:data-status[parent='validation:testRpc', value='success']"
                    + "#validation:leaf-type[parent='validation:testRpc', value='good']";
            Element element = buildRpc(xmlPath);

            NetconfRpcRequest request = new NetconfRpcRequest();
            request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element));
            request.setMessageId("1");
            m_dummyRpcHandler.validate(m_rpcConstraintParser, request);

            // data-status = in-progress case
            xmlPath = "#validation:testRpc[@xmlns:validation='urn:org:bbf:pma:validation', parent='rpc']"
                    + "#validation:data-status[parent='validation:testRpc', value='in-progress']"
                    + "#validation:leaf-type[parent='validation:testRpc', value='good']";
            element = buildRpc(xmlPath);
            request = new NetconfRpcRequest();
            request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element));
            request.setMessageId("1");
            m_dummyRpcHandler.validate(m_rpcConstraintParser, request);
        } catch (Exception e) {
            failTest(e);
        }
    }

    @Test(expected = ValidationException.class)
    public void testRpcInvalidMustLeaf() throws Throwable {
        try {
            String xmlPath = "#validation:testRpc[@xmlns:validation='urn:org:bbf:pma:validation', parent='rpc']"
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
    public void testRpcList() throws Exception {
        try {
            String xmlPath = "#validation:testRpc[@xmlns:validation='urn:org:bbf:pma:validation', parent='rpc']"
                    + "#validation:result-list[parent='validation:testRpc', value='10']"
                    + "#validation:list-type[parent='validation:testRpc',]"
                    + "#validation:list-id[parent='validation:list-type', value='id1']"
                    + "#validation:list-value[parent='validation:list-type', value='11']";
            Element element = buildRpc(xmlPath);
            NetconfRpcRequest request = new NetconfRpcRequest();
            request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element));
            request.setMessageId("1");
            m_dummyRpcHandler.validate(m_rpcConstraintParser, request);
        } catch (Exception e) {
            failTest(e);
        }

        try {
            String xmlPath = "#validation:testRpc[@xmlns:validation='urn:org:bbf:pma:validation', parent='rpc']"
                    + "#validation:result-list[parent='validation:testRpc', value='1']"
                    + "#validation:list-type[parent='validation:testRpc',]"
                    + "#validation:list-id[parent='validation:list-type', value='id1']"
                    + "#validation:list-value[parent='validation:list-type', value='11']";
            Element element = buildRpc(xmlPath);
            NetconfRpcRequest request = new NetconfRpcRequest();
            request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element));
            request.setMessageId("1");
            m_dummyRpcHandler.validate(m_rpcConstraintParser, request);
            failTest();
        } catch (Exception e) {
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
        try {
            String xmlPath = "#validation:testRpc[@xmlns:validation='urn:org:bbf:pma:validation', parent='rpc']"
                    + "#validation:result-list[parent='validation:testRpc', value='10']"
                    + "#validation:list-type[parent='validation:testRpc',]"
                    + "#validation:list-id[parent='validation:list-type', value='id1']"
                    + "#validation:list-value[parent='validation:list-type', value='11']"
                    + "#validation:inside-list[parent='validation:list-type']"
                    + "#validation:some-leaf[parent='validation:inside-list', value='10']"
                    + "#validation:list-type2[parent='validation:testRpc']"
                    + "#validation:list-id[parent='validation:list-type2', value='11']";
            Element element = buildRpc(xmlPath);
            NetconfRpcRequest request = new NetconfRpcRequest();
            request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element));
            request.setMessageId("1");
            m_dummyRpcHandler.validate(m_rpcConstraintParser, request);
        } catch (Exception e) {
            failTest();
        }

    }

    @Test(expected = RpcValidationException.class)
    public void testRpcLeafRef() throws Exception {
        XmlGenerator generator = new XmlGenerator();
        NetconfRpcRequest request = new NetconfRpcRequest();
        try {
            generator.addElement(null, "rpc", null, "xmlns='urn:ietf:params:xml:ns:netconf:base:1.0'", "message-id='1'")
                    .addElement("rpc", "validation:testRpc", null, "xmlns:validation='urn:org:bbf:pma:validation'")
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
        } catch (Exception e) {
            failTest(e);
        }

        try {
            generator = new XmlGenerator();
            generator.addElement(null, "rpc", null, "xmlns='urn:ietf:params:xml:ns:netconf:base:1.0'", "message-id='1'")
                    .addElement("rpc", "validation:testRpc", null, "xmlns:validation='urn:org:bbf:pma:validation'")
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
        } catch (Exception e) {
            assertTrue(e instanceof RpcValidationException);
            ValidationException cause = (ValidationException) e.getCause();
            assertTrue(cause.getRpcError().getErrorMessage()
                    .contains("Violate when constraints: ../../album[current()]/song-count >= 10"));
            throw e;
        }
        failTest();
    }

    @Test
    public void testRpcInstanceIdentifier() throws Exception {
        try {
            XmlGenerator generator = new XmlGenerator();
            generator.addElement(null, "rpc", null, "xmlns='urn:ietf:params:xml:ns:netconf:base:1.0'", "message-id='1'")
                    .addElement("rpc", "validation:testRpc", null, "xmlns:validation='urn:org:bbf:pma:validation'")
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
        } catch (Exception e) {
            failTest(e);
        }

        try {
            String xmlPath = "#validation:testRpc[@xmlns:validation='urn:org:bbf:pma:validation', parent='rpc']"
                    + "#validation:instance-identifier-example[parent='validation:testRpc']"
                    + "#validation:leaflist[parent='validation:instance-identifier-example', " +
                    "value='/testRpc/instance-identifier-example/leaf1']"
                    + "#validation:student[parent='validation:instance-identifier-example']"
                    + "#validation:student-id[parent='validation:student', value='ST001']"
                    + "#validation:student-name[parent='validation:student', value='Student 1']"
                    + "#validation:student-instance-identifier1[parent='validation:student', " +
                    "value='/testRpc/instance-identifier-example/leaf1']";

            Element element = buildRpc(xmlPath);
            NetconfRpcRequest request = new NetconfRpcRequest();
            request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element));
            request.setMessageId("1");
            m_dummyRpcHandler.validate(m_rpcConstraintParser, request);
            failTest();
        } catch (Exception e) {
            assertTrue(e instanceof RpcValidationException);
            ValidationException cause = (ValidationException) e.getCause();
            assertTrue(cause.getRpcError().toString().contains
                    ("errorPath=/validation:instance-identifier-example/validation:student/validation:student" +
                            "-instance-identifier1,"
                            + " errorMessage=Missing required element /testRpc/instance-identifier-example/leaf1"));
        }
    }

    @Test
    public void testRpcInstanceIdentifierWithoutPrefix() throws Exception {
        try {
            String xmlPath = "#testRpc[@xmlns='urn:org:bbf:pma:validation', parent='rpc']"
                    + "#instance-identifier-example[parent='testRpc']"
                    + "#leaflist[parent='instance-identifier-example', " +
                    "value='/testRpc/instance-identifier-example/leaf1']"
                    + "#student[parent='instance-identifier-example']"
                    + "#student-id[parent='student', value='ST001']"
                    + "#student-name[parent='student', value='Student 1']"
                    + "#student-instance-identifier1[parent='student', " +
                    "value='/testRpc/instance-identifier-example/leaf1']";

            Element element = buildRpc(xmlPath);
            NetconfRpcRequest request = new NetconfRpcRequest();
            request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element));
            request.setMessageId("1");
            m_dummyRpcHandler.validate(m_rpcConstraintParser, request);
            failTest();
        } catch (Exception e) {
            assertTrue(e instanceof RpcValidationException);
            ValidationException cause = (ValidationException) e.getCause();
            assertTrue(cause.getRpcError().toString().contains
                    ("errorPath=/validation:instance-identifier-example/validation:student/validation:student" +
                            "-instance-identifier1,"
                            + " errorMessage=Missing required element /testRpc/instance-identifier-example/leaf1"));
        }
    }

    public void testRpcChoiceCaseList() throws Exception {
        try {
            String xmlPath = "#validation:testRpc[@xmlns:validation='urn:org:bbf:pma:validation', parent='rpc']"
                    + "#validation:choicecase[parent='validation:testRpc']"
                    + "#validation:data-choice[parent='validation:choicecase', value='49']"
                    + "#validation:result-choice[parent='validation:choicecase', value='success']"
                    + "#validation:list-case-success[parent='validation:choicecase']"
                    + "#validation:success-id[parent='validation:list-case-success', value='1']"
                    + "#validation:success-value[parent='validation:list-case-success', value='leaf2']";
            Element element = buildRpc(xmlPath);
            NetconfRpcRequest request = new NetconfRpcRequest();
            request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element));
            request.setMessageId("1");
            m_dummyRpcHandler.validate(m_rpcConstraintParser, request);

            xmlPath = "#validation:testRpc[@xmlns:validation='urn:org:bbf:pma:validation', parent='rpc']"
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

        } catch (Exception e) {
            failTest(e);
        }

        try {
            String xmlPath = "#validation:testRpc[@xmlns:validation='urn:org:bbf:pma:validation', parent='rpc']"
                    + "#validation:choicecase[parent='validation:testRpc']"
                    + "#validation:data-choice[parent='validation:choicecase', value='51']"
                    + "#validation:result-choice[parent='validation:choicecase', value='success']"
                    + "#validation:list-case-success[parent='validation:choicecase']"
                    + "#validation:success-id[parent='validation:list-case-success', value='1']"
                    + "#validation:success-value[parent='validation:list-case-success', value='leaf2']";
            Element element = buildRpc(xmlPath);
            NetconfRpcRequest request = new NetconfRpcRequest();
            request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element));
            request.setMessageId("1");
            m_dummyRpcHandler.validate(m_rpcConstraintParser, request);
            failTest();
        } catch (Exception e) {
            assertTrue(e instanceof RpcValidationException);
            assertTrue(e.getMessage().contains("Violate when constraints: ../data-choice < 50"));
        }


        try {
            String xmlPath = "#validation:testRpc[@xmlns:validation='urn:org:bbf:pma:validation', parent='rpc']"
                    + "#validation:choicecase[parent='validation:testRpc']"
                    + "#validation:data-choice[parent='validation:choicecase', value='49']"
                    + "#validation:result-choice[parent='validation:choicecase', value='failed']"
                    + "#validation:list-case-success[parent='validation:choicecase']"
                    + "#validation:success-id[parent='validation:list-case-success', value='1']"
                    + "#validation:success-value[parent='validation:list-case-success', value='leaf2']";
            Element element = buildRpc(xmlPath);
            NetconfRpcRequest request = new NetconfRpcRequest();
            request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element));
            request.setMessageId("1");
            m_dummyRpcHandler.validate(m_rpcConstraintParser, request);
            failTest();
        } catch (Exception e) {
            assertTrue(e instanceof RpcValidationException);
            assertTrue(e.getMessage().contains("Violate when constraints: ../../result-choice = 'success'"));
            throw e;
        }
    }

    @Test
    public void testRpcChoiceCaseContainer() throws Exception {
        try {
            String xmlPath = "#validation:testRpc[@xmlns:validation='urn:org:bbf:pma:validation', parent='rpc']"
                    + "#validation:choicecase[parent='validation:testRpc']"
                    + "#validation:data-choice[parent='validation:choicecase', value='101']"
                    + "#validation:result-choice[parent='validation:choicecase', value='success']"
                    + "#validation:container-case-success[parent='validation:choicecase']"
                    + "#validation:container-success-leaf1[parent='validation:container-case-success', value='leaf1']"
                    + "#validation:container-success-leaf2[parent='validation:container-case-success', value='leaf2']";
            Element element = buildRpc(xmlPath);
            NetconfRpcRequest request = new NetconfRpcRequest();
            request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element));
            request.setMessageId("1");
            m_dummyRpcHandler.validate(m_rpcConstraintParser, request);

            xmlPath = "#validation:testRpc[@xmlns:validation='urn:org:bbf:pma:validation', parent='rpc']"
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

        } catch (Exception e) {
            failTest(e);
        }

        try {
            String xmlPath = "#validation:testRpc[@xmlns:validation='urn:org:bbf:pma:validation', parent='rpc']"
                    + "#validation:choicecase[parent='validation:testRpc']"
                    + "#validation:data-choice[parent='validation:choicecase', value='100']"
                    + "#validation:result-choice[parent='validation:choicecase', value='success']"
                    + "#validation:container-case-success[parent='validation:choicecase']"
                    + "#validation:container-success-leaf1[parent='validation:container-case-success', value='leaf1']"
                    + "#validation:container-success-leaf2[parent='validation:container-case-success', value='leaf2']";
            Element element = buildRpc(xmlPath);
            NetconfRpcRequest request = new NetconfRpcRequest();
            request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element));
            request.setMessageId("1");
            m_dummyRpcHandler.validate(m_rpcConstraintParser, request);
            failTest();
        } catch (Exception e) {
            assertTrue(e instanceof RpcValidationException);
            assertTrue(e.getMessage().contains("Violate when constraints: ../data-choice > 100"));
        }

    }

    @Test
    public void testRpcChoiceCase() throws Exception {
        try {
            String xmlPath = "#validation:testRpc[@xmlns:validation='urn:org:bbf:pma:validation', parent='rpc']"
                    + "#validation:choicecase[parent='validation:testRpc']"
                    + "#validation:data-choice[parent='validation:choicecase', value='1']"
                    + "#validation:result-choice[parent='validation:choicecase', value='success']"
                    + "#validation:leaf-case-success[parent='validation:choicecase', value='hello']";
            Element element = buildRpc(xmlPath);
            NetconfRpcRequest request = new NetconfRpcRequest();
            request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element));
            request.setMessageId("1");
            m_dummyRpcHandler.validate(m_rpcConstraintParser, request);
        } catch (Exception e) {
            failTest(e);
        }

        try {
            String xmlPath = "#validation:testRpc[@xmlns:validation='urn:org:bbf:pma:validation', parent='rpc']"
                    + "#validation:choicecase[parent='validation:testRpc']"
                    + "#validation:data-choice[parent='validation:choicecase', value='1']"
                    + "#validation:result-choice[parent='validation:choicecase', value='failed']"
                    + "#validation:leaf-case-success[parent='validation:choicecase', value='hello']";
            Element element = buildRpc(xmlPath);
            NetconfRpcRequest request = new NetconfRpcRequest();
            request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element));
            request.setMessageId("1");
            m_dummyRpcHandler.validate(m_rpcConstraintParser, request);
            failTest();
        } catch (Exception e) {
            assertTrue(e instanceof RpcValidationException);
            assertTrue(e.getMessage().contains("Violate when constraints: ../../result-choice = 'success'"));
        }


    }

    Element buildRpc(String string) throws Exception {
        StringBuilder builder = new StringBuilder();
        builder.append("#rpc[@xmlns='urn:ietf:params:xml:ns:netconf:base:1.0', @message-id='1', parent='null']")
                .append(string);
        Element returnValue = XmlGenerator.buildXml(builder.toString());
        return returnValue;
    }

    @Test
    public void testTargetStateLeafRef() throws RpcValidationException, NetconfMessageBuilderException {
        XmlGenerator generator = new XmlGenerator();
        generator.addElement(null, "rpc", null, "xmlns='urn:ietf:params:xml:ns:netconf:base:1.0'", "message-id='1'")
                .addElement("rpc", "validation:testRpc", null, "xmlns:validation='urn:org:bbf:pma:validation'")
                .addElement("validation:testRpc", "validation:leaf-ref", null, EMPTY_STRING)
                .addElement("validation:leaf-ref", "validation:stateValue", null, EMPTY_STRING)
                .addElement("validation:stateValue", "validation:value2", "test", EMPTY_STRING);
        NetconfRpcRequest request = new NetconfRpcRequest();
        request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(generator.buildXml()));
        request.setMessageId("1");
        m_dummyRpcHandler.validate(m_rpcConstraintParser, request);

        generator = new XmlGenerator();
        generator.addElement(null, "rpc", null, "xmlns='urn:ietf:params:xml:ns:netconf:base:1.0'", "message-id='1'")
                .addElement("rpc", "validation:testRpc", null, "xmlns:validation='urn:org:bbf:pma:validation'")
                .addElement("validation:testRpc", "validation:leaf-ref", null, EMPTY_STRING)
                .addElement("validation:leaf-ref", "validation:stateValue", null, EMPTY_STRING)
                .addElement("validation:stateValue", "validation:value3", "test", EMPTY_STRING);
        request = new NetconfRpcRequest();
        request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(generator.buildXml()));
        request.setMessageId("1");
        m_dummyRpcHandler.validate(m_rpcConstraintParser, request);
    }

    @Test
    public void testGrouping() throws NetconfMessageBuilderException, RpcValidationException {
        XmlGenerator generator = new XmlGenerator();
        generator.addElement(null, "rpc", null, "xmlns='urn:ietf:params:xml:ns:netconf:base:1.0'", "message-id='1'")
                .addElement("rpc", "validation:groupingTest", null, "xmlns:validation='urn:org:bbf:pma:validation'")
                .addElement("validation:groupingTest", "validation:groupContainer1", null, EMPTY_STRING)
                .addElement("validation:groupContainer1", "validation:groupContainerLeaf1", "test", EMPTY_STRING);
        NetconfRpcRequest request = new NetconfRpcRequest();
        request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(generator.buildXml()));
        request.setMessageId("1");
        m_dummyRpcHandler2.validate(m_rpcConstraintParser, request);

    }

    @Test
    public void testRpcCount() throws NetconfMessageBuilderException, RpcValidationException {
        {
            XmlGenerator generator = new XmlGenerator();
            generator.addElement(null, "rpc", null, "xmlns='urn:ietf:params:xml:ns:netconf:base:1.0'", "message-id='1'")
                    .addElement("rpc", "validation:testRpc", null, "xmlns:validation='urn:org:bbf:pma:validation'")
                    .addElement("validation:testRpc", "validation:countable", "test", EMPTY_STRING)
                    .addElement("validation:testRpc", "validation:countLeaf", "test", EMPTY_STRING);
            NetconfRpcRequest request = new NetconfRpcRequest();
            request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(generator.buildXml()));
            request.setMessageId("1");
            m_dummyRpcHandler.validate(m_rpcConstraintParser, request);
        }

        try {
            XmlGenerator generator = new XmlGenerator();
            generator.addElement(null, "rpc", null, "xmlns='urn:ietf:params:xml:ns:netconf:base:1.0'", "message-id='1'")
                    .addElement("rpc", "validation:testRpc", null, "xmlns:validation='urn:org:bbf:pma:validation'")
                    .addElement("validation:testRpc", "validation:countLeaf", "test", EMPTY_STRING);
            NetconfRpcRequest request = new NetconfRpcRequest();
            request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(generator.buildXml()));
            request.setMessageId("1");
            m_dummyRpcHandler.validate(m_rpcConstraintParser, request);
            fail("We expect an exception");
        } catch (RpcValidationException e) {
            assertEquals("Violate when constraints: count(countable) = 1", e.getRpcError().getErrorMessage());
        }
    }

}
