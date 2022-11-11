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

package org.broadband_forum.obbaa.netconf.api.messages;

import static org.broadband_forum.obbaa.netconf.api.util.DocumentUtils.stringToDocument;
import static org.broadband_forum.obbaa.netconf.api.util.TestXML.assertXMLEquals;
import static org.broadband_forum.obbaa.netconf.api.util.TestXML.loadAsXml;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.junit.Test;
import org.xml.sax.SAXException;

public class EditConfigRequestTest extends RpcTypeTest {

    private EditConfigRequest m_editConfigRequest = new EditConfigRequest();
    private EditConfigRequest m_editConfigRequest_null;
    private String m_target = StandardDataStores.RUNNING;
    private String m_testOption = "test-only";
    private String m_errorOption = EditConfigErrorOptions.CONTINUE_ON_ERROR;
    private EditConfigElement m_configElement = new EditConfigElement();
    private String m_defaultOperation = EditConfigDefaultOperations.REPLACE;
    private EditConfigElement m_configElement_null = null;
    private String m_defaultOperation_null = null;
    private String m_target_null = null;
    private String m_testOption_null = null;
    private String m_errorOption_null = null;
    private boolean m_uploadToPmaRequest = false;
    private int m_withDelay = 1;
    private String m_messageId = "101";
    private EditConfigRequest m_editConfigRequest_new = new EditConfigRequest();

    public EditConfigRequestTest() throws NetconfMessageBuilderException {
        super(DocumentToPojoTransformer.getEditConfig(stringToDocument("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
                "  <edit-config>\n" +
                "    <target>\n" +
                "      <running/>\n" +
                "    </target>\n" +
                "    <config>\n" +
                "      <test xmlns=\"ns:ns\"/>" +
                "    </config>\n" +
                "  </edit-config>\n" +
                "</rpc>")));
    }

    @Test
    public void testSetAndGetTarget() {
        assertEquals(m_editConfigRequest, m_editConfigRequest.setTarget(m_target));
        assertEquals(m_target, m_editConfigRequest.getTarget());
    }

    @Test
    public void testSetTargetRunning() {
        EditConfigRequest editConfigRequest1 = m_editConfigRequest.setTargetRunning();
        assertEquals("running", editConfigRequest1.getTarget());
    }
    
    @Test
    public void test_Synchronize_NotSpecified_False() {
    	assertEquals(false, m_editConfigRequest.isTriggerSyncUponSuccess());
    }
    
    @Test(expected = NetconfMessageBuilderException.class)
    public void test_Synchronize_Specified_Empty_NotAllowed() throws Exception {
    	DocumentToPojoTransformer.getEditConfig(stringToDocument("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
                "  <edit-config>\n" +
                "    <target>\n" +
                "      <running/>\n" +
                "    </target>\n" +
                "    <config>\n" +
                "      <test xmlns=\"ns:ns\"/>" +
                "    </config>\n" +
                "    <trigger-sync-upon-success xmlns=\"http://www.test-company.com/solutions/netconf-extensions\"/>\n" +
                "  </edit-config>\n" +
                "</rpc>"));
    }
    
    @Test
    public void test_Synchronize_Specified_Valid_True() throws Exception {
    	EditConfigRequest request = DocumentToPojoTransformer.getEditConfig(stringToDocument("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
                "  <edit-config>\n" +
                "    <target>\n" +
                "      <running/>\n" +
                "    </target>\n" +
                "    <config>\n" +
                "      <test xmlns=\"ns:ns\"/>" +
                "    </config>\n" +
                "    <trigger-sync-upon-success xmlns=\"http://www.test-company.com/solutions/netconf-extensions\">true</trigger-sync-upon-success>\n" +
                "  </edit-config>\n" +
                "</rpc>"));
    	assertEquals(true, request.isTriggerSyncUponSuccess());
    }
    
    @Test
    public void test_Synchronize_Specified_Valid_False() throws Exception {
    	EditConfigRequest request = DocumentToPojoTransformer.getEditConfig(stringToDocument("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
                "  <edit-config>\n" +
                "    <target>\n" +
                "      <running/>\n" +
                "    </target>\n" +
                "    <config>\n" +
                "      <test xmlns=\"ns:ns\"/>" +
                "    </config>\n" +
                "    <trigger-sync-upon-success xmlns=\"http://www.test-company.com/solutions/netconf-extensions\">false</trigger-sync-upon-success>\n" +
                "  </edit-config>\n" +
                "</rpc>"));
    	assertEquals(false, request.isTriggerSyncUponSuccess());
    }

    @Test
    public void testSetAndGetTestOption() {

        assertEquals(m_editConfigRequest, m_editConfigRequest.setTestOption(m_testOption));
        assertEquals(m_testOption, m_editConfigRequest.getTestOption());
    }

    @Test
    public void testSetAndGetErrorOption() {

        assertEquals(m_editConfigRequest, m_editConfigRequest.setErrorOption(m_errorOption));
        assertEquals(m_errorOption, m_editConfigRequest.getErrorOption());
    }

    @Test
    public void testSetAndGetConfigElement() {

        assertEquals(m_editConfigRequest, m_editConfigRequest.setConfigElement(m_configElement));
        assertNotNull(m_editConfigRequest.getConfigElement());
    }

    @Test
    public void testGetRequestDocument() throws NetconfMessageBuilderException, SAXException, IOException {

        m_editConfigRequest.setTarget(m_target);
        m_editConfigRequest.setDefaultOperation(m_defaultOperation);
        m_editConfigRequest.setTestOption(m_testOption);
        m_editConfigRequest.setErrorOption(m_errorOption);
        m_editConfigRequest.setWithDelay(m_withDelay);
        m_editConfigRequest.setConfigElement(m_configElement);
        m_editConfigRequest.setMessageId(m_messageId);
        m_editConfigRequest.setTransactionId(false);
        assertNotNull(m_editConfigRequest.getRequestDocument());
        assertXMLEquals(loadAsXml("TestXMLResources/editConfigTest.xml"), m_editConfigRequest.getRequestDocument().getDocumentElement());

    }

    @Test
    public void testGetRequestDocumentWithTxId() throws NetconfMessageBuilderException, SAXException, IOException {

        m_editConfigRequest.setTarget(m_target);
        m_editConfigRequest.setDefaultOperation(m_defaultOperation);
        m_editConfigRequest.setTestOption(m_testOption);
        m_editConfigRequest.setErrorOption(m_errorOption);
        m_editConfigRequest.setTransactionId(true);
        m_editConfigRequest.setConfigElement(m_configElement);
        m_editConfigRequest.setMessageId(m_messageId);
        assertNotNull(m_editConfigRequest.getRequestDocument());
        assertXMLEquals(loadAsXml("editConfigWithTxId.xml"), m_editConfigRequest.getRequestDocument().getDocumentElement());

    }

    @Test
    public void testGetRequestDocumentWithUserContextAttributes() throws NetconfMessageBuilderException, SAXException, IOException {

        m_editConfigRequest.setTarget(m_target);
        m_editConfigRequest.setDefaultOperation(m_defaultOperation);
        m_editConfigRequest.setTestOption(m_testOption);
        m_editConfigRequest.setErrorOption(m_errorOption);
        m_editConfigRequest.setTransactionId(true);
        m_editConfigRequest.setConfigElement(m_configElement);
        m_editConfigRequest.setMessageId(m_messageId);
        m_editConfigRequest.setUserContext("admin");
        m_editConfigRequest.setContextSessionId("12345");
        assertNotNull(m_editConfigRequest.getRequestDocument());
        String editRequestXml = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" " +
                "     xmlns:ctx=\"http://www.test-company.com/solutions/netconf-extensions\"\n" +
                "     ctx:session-id=\"12345\"\n" +
                "     ctx:user-context=\"admin\">\n" +
                "    <edit-config>\n" +
                "        <config/>\n" +
                "        <default-operation>replace</default-operation>\n" +
                "        <error-option>continue-on-error</error-option>\n" +
                "        <target>\n" +
                "            <running/>\n" +
                "        </target>\n" +
                "        <test-option>test-only</test-option>\n" +
                "        <with-transaction-id xmlns=\"http://tail-f.com/ns/netconf/with-transaction-id\"/>\n" +
                "    </edit-config>\n" +
                "</rpc>";
        assertXMLEquals(DocumentUtils.stringToDocumentElement(editRequestXml), m_editConfigRequest.getRequestDocument().getDocumentElement());
    }

    @Test
    public void testSetAndGetDefaultOperation() {

        assertEquals(m_editConfigRequest, m_editConfigRequest.setDefaultOperation(m_defaultOperation));
        assertEquals("replace", m_editConfigRequest.getDefaultOperation());
    }

    @Test
    public void testSetUploadToPmaRequest() {
        m_editConfigRequest.setUploadToPmaRequest();
        assertNotNull(m_editConfigRequest);
    }

    @Test
    public void testIsUploadToPmaRequest() {

        assertEquals(m_uploadToPmaRequest, m_editConfigRequest.isUploadToPmaRequest());
    }

    @Test
    public void testHashCode() {

        assertEquals(2129374033, m_editConfigRequest_new.hashCode());
        m_editConfigRequest.setConfigElement(m_configElement);
        m_editConfigRequest.setDefaultOperation(m_defaultOperation_null);
        m_editConfigRequest.setTarget(m_target_null);
        m_editConfigRequest.setErrorOption(m_errorOption_null);
        m_editConfigRequest.setTestOption(m_testOption_null);
        assertEquals(1803636513, m_editConfigRequest.hashCode());

    }

    @Test
    public void testEquals() {

        m_editConfigRequest_new.equals(m_editConfigRequest_null);
        assertTrue(m_editConfigRequest.equals(m_editConfigRequest));
        assertTrue(m_editConfigRequest.equals(m_editConfigRequest_new));
        assertFalse(m_configElement.equals(null));
        assertNull(m_configElement_null);
        assertNull(m_defaultOperation_null);
        assertNull(m_target_null);
        assertNull(m_errorOption_null);
        assertNull(m_testOption_null);
        m_editConfigRequest_new.setTarget(m_target);
        m_editConfigRequest_new.setDefaultOperation(m_defaultOperation);
        m_editConfigRequest_new.setTestOption(m_testOption);
        
        m_editConfigRequest.setDefaultOperation(m_defaultOperation_null);
        assertFalse(m_editConfigRequest.equals(m_editConfigRequest_new));
        m_editConfigRequest.setDefaultOperation(m_defaultOperation);
        m_editConfigRequest.setErrorOption(m_errorOption_null);
        assertFalse(m_editConfigRequest.equals(m_editConfigRequest_new));
        m_editConfigRequest.setErrorOption(m_errorOption);
        m_editConfigRequest.setTarget(m_target_null);
        assertFalse(m_editConfigRequest.equals(m_editConfigRequest_new));
        
        m_editConfigRequest_new.setErrorOption(m_errorOption);
        m_editConfigRequest_new.setWithDelay(m_withDelay);
        m_editConfigRequest_new.setConfigElement(m_configElement);
        assertFalse(m_editConfigRequest_new.equals(m_configElement));
        assertFalse(m_editConfigRequest_new.equals(m_editConfigRequest));
        assertFalse(m_editConfigRequest_new.equals(m_defaultOperation));
        assertFalse(m_editConfigRequest_new.equals(m_editConfigRequest));
        assertFalse(m_editConfigRequest.equals(m_editConfigRequest_new));
        assertFalse(m_editConfigRequest.equals(m_editConfigRequest_null));
        

    }

    @Test
    public void test_forceInstanceCreation_NotSpecified() {
    	assertEquals(false, m_editConfigRequest.isForceInstanceCreation());
    }

    @Test(expected = NetconfMessageBuilderException.class)
    public void test_forceInstanceCreation_EmptyValue() throws Exception {
    	DocumentToPojoTransformer.getEditConfig(stringToDocument("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
                "  <edit-config>\n" +
                "    <target>\n" +
                "      <running/>\n" +
                "    </target>\n" +
                "    <config>\n" +
                "      <test xmlns=\"ns:ns\"/>" +
                "    </config>\n" +
                "    <force-instance-creation xmlns=\"http://www.test-company.com/solutions/netconf-extensions\"/>\n" +
                "  </edit-config>\n" +
                "</rpc>"));
    }

    @Test(expected = NetconfMessageBuilderException.class)
    public void test_forceInstanceCreation_InvalidValue() throws Exception {
    	DocumentToPojoTransformer.getEditConfig(stringToDocument("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
                "  <edit-config>\n" +
                "    <target>\n" +
                "      <running/>\n" +
                "    </target>\n" +
                "    <config>\n" +
                "      <test xmlns=\"ns:ns\"/>" +
                "    </config>\n" +
                "    <force-instance-creation xmlns=\"http://www.test-company.com/solutions/netconf-extensions\">dummy</force-instance-creation>\n" +
                "  </edit-config>\n" +
                "</rpc>"));
    }

    @Test
    public void test_forceInstanceCreation_Valid_True() throws Exception {
    	EditConfigRequest request = DocumentToPojoTransformer.getEditConfig(stringToDocument("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
                "  <edit-config>\n" +
                "    <target>\n" +
                "      <running/>\n" +
                "    </target>\n" +
                "    <config>\n" +
                "      <test xmlns=\"ns:ns\"/>" +
                "    </config>\n" +
                "    <force-instance-creation xmlns=\"http://www.test-company.com/solutions/netconf-extensions\">true</force-instance-creation>\n" +
                "  </edit-config>\n" +
                "</rpc>"));
    	assertEquals(true, request.isForceInstanceCreation());
    }

    @Test
    public void test_forceInstanceCreation_Specified_Valid_False() throws Exception {
    	EditConfigRequest request = DocumentToPojoTransformer.getEditConfig(stringToDocument("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
                "  <edit-config>\n" +
                "    <target>\n" +
                "      <running/>\n" +
                "    </target>\n" +
                "    <config>\n" +
                "      <test xmlns=\"ns:ns\"/>" +
                "    </config>\n" +
                "    <force-instance-creation xmlns=\"http://www.test-company.com/solutions/netconf-extensions\">false</force-instance-creation>\n" +
                "  </edit-config>\n" +
                "</rpc>"));
    	assertEquals(false, request.isForceInstanceCreation());
    }

}
