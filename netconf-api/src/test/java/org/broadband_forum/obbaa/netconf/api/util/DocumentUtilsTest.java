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

package org.broadband_forum.obbaa.netconf.api.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.broadband_forum.obbaa.netconf.api.messages.CreateSubscriptionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcRequest;
import org.junit.Test;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class DocumentUtilsTest {

    @Test
    public void testGetChildNodeByName() throws NetconfMessageBuilderException {
        String getConfigRequest = getConfigString2();
        Document document = DocumentUtils.stringToDocument(getConfigRequest);
        Node childNode = DocumentUtils.getChildNodeByName(document, NetconfResources.RPC, NetconfResources
                .NETCONF_RPC_NS_1_0);
        assertEquals("nc:rpc", childNode.getNodeName());
        assertEquals("rpc", childNode.getLocalName());

        getConfigRequest = getConfigString1();
        document = DocumentUtils.stringToDocument(getConfigRequest);
        childNode = DocumentUtils.getChildNodeByName(document, NetconfResources.RPC, NetconfResources
                .NETCONF_RPC_NS_1_0);
        assertEquals("rpc", childNode.getNodeName());
        assertEquals("rpc", childNode.getLocalName());


        String invalidEdit = getInvalidEdit();
        document = DocumentUtils.stringToDocument(invalidEdit);
        childNode = DocumentUtils.getChildNodeByName(document, NetconfResources.EDIT_CONFIG_CONFIG, NetconfResources
                .NETCONF_RPC_NS_1_0);
        assertNull(childNode);
    }

    @Test
    public void testGetChildElements() throws NetconfMessageBuilderException {
        String invalidEdit = getInvalidEdit();
        Document document = DocumentUtils.stringToDocument(invalidEdit);
        List<Element> childElements = DocumentUtils.getChildElements(document.getDocumentElement(), NetconfResources
                .EDIT_CONFIG, NetconfResources.NETCONF_RPC_NS_1_0);
        assertEquals(1, childElements.size());
        Element editElement = childElements.get(0);
        assertEquals(NetconfResources.EDIT_CONFIG, editElement.getLocalName());
        assertEquals(NetconfResources.NETCONF_RPC_NS_1_0, editElement.getNamespaceURI());

        childElements = DocumentUtils.getChildElements(editElement, NetconfResources.EDIT_CONFIG_CONFIG,
                NetconfResources.NETCONF_RPC_NS_1_0);
        assertEquals(0, childElements.size());
    }


    @Test
    public void testGetElement() throws NetconfMessageBuilderException {

        Document document = DocumentUtils.createDocument();
        Node childNode1 = DocumentUtils.getElement(document, "data1", "test-namespace", "test", "test");
        String dataToString1 = DocumentUtils.documentToPrettyString(childNode1);
        assertEquals("<test:data1 xmlns:test=\"test-namespace\">test</test:data1>\n", dataToString1);

        Node childNode2 = DocumentUtils.getElement(document, "data2", "test-namespace", "test", childNode1);
        String dataToString2 = DocumentUtils.documentToPrettyString(childNode2);
        String expected = "<test:data2 xmlns:test=\"test-namespace\">\n"
                + "<test:data1>test</test:data1>\n"
                + "</test:data2>\n";
        assertEquals(expected, dataToString2);


    }

    @Test
    public void testGetMessageIdFromRpcDocument() throws NetconfMessageBuilderException {
        String getConfigRequest = getConfigString2();
        Document getConfigDocument = DocumentUtils.stringToDocument(getConfigRequest);
        assertEquals("5", DocumentUtils.getInstance().getMessageIdFromRpcDocument(getConfigDocument));

        getConfigRequest = getConfigString1();
        getConfigDocument = DocumentUtils.stringToDocument(getConfigRequest);
        assertEquals("5", DocumentUtils.getInstance().getMessageIdFromRpcDocument(getConfigDocument));
    }

    @Test
    public void testGetSource() throws NetconfMessageBuilderException {
        String getConfigRequest = getConfigString2();
        Document getConfigDocument = DocumentUtils.stringToDocument(getConfigRequest);
        assertEquals("running", DocumentUtils.getInstance().getSourceFromRpcDocument(getConfigDocument));

        getConfigRequest = getConfigString1();
        getConfigDocument = DocumentUtils.stringToDocument(getConfigRequest);
        assertEquals("running", DocumentUtils.getInstance().getSourceFromRpcDocument(getConfigDocument));

        getConfigRequest = "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"5\">\n" +
                "    <nc:get-config xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.2\">\n" +
                "        <nc:source>\n" +
                "            <nc:running/>\n" +
                "        </nc:source>\n" +
                "    </nc:get-config>\n" +
                "</rpc>";
        try {
            getConfigDocument = DocumentUtils.stringToDocument(getConfigRequest);
            DocumentUtils.getInstance().getSourceFromRpcDocument(getConfigDocument);
            fail("Expected an exception here");
        } catch (NetconfMessageBuilderException e) {
            assertEquals("<source> cannot be null/empty", e.getMessage());
        }
    }

    @Test
    public void testGetSubscriptionRequest() throws DOMException, ParseException {
        NetconfRpcRequest request = new NetconfRpcRequest();
        Element element = mock(Element.class);
        request.setRpcInput(element);
        CreateSubscriptionRequest subscriptionRequest = DocumentUtils.getInstance().getSubscriptionRequest(request);
        assertNotNull(subscriptionRequest);
    }

    @Test
    public void testGetRpcNode() throws NetconfMessageBuilderException {
        String configRequest = getConfigString1();
        Document document = DocumentUtils.stringToDocument(configRequest);
        Node rpcNode = DocumentUtils.getInstance().getRpcNode(document);
        assertNotNull(rpcNode);
        assertEquals("rpc", rpcNode.getLocalName());
    }

    @Test
    public void testGetTargetFromRpcDocument() throws NetconfMessageBuilderException {

        String targetConfigString = getTargetConfig();
        Document document = DocumentUtils.stringToDocument(targetConfigString);
        String targetNodeName = DocumentUtils.getInstance().getTargetFromRpcDocument(document);
        assertEquals("running", targetNodeName);
    }

    @Test
    public void testFormatXml() throws NetconfMessageBuilderException {
        String unformattedXml = getSampleRPC();
        String formattedXml = DocumentUtils.format(unformattedXml);
        assertNotEquals(formattedXml, unformattedXml);
    }

    @Test
    public void testGetDocumentElement() throws IOException, SAXException, ParserConfigurationException {
        Element element = DocumentUtils.getDocumentElement("<parent xmlns=\"parentNs\"><child>value</child></parent>");
        assertNotNull(element);
        assertEquals("parent", element.getLocalName());
    }

    @Test
    public void testGetRpcOtherAttributes() throws NetconfMessageBuilderException {
        String rpcString = getSampleRPC();
        Document document = DocumentUtils.stringToDocument(rpcString);
        Map<String, String> additionalAttrs = DocumentUtils.getInstance().getRpcOtherAttributes(document);
        assertNotNull(additionalAttrs);
    }

    @Test
    public void testLoadXmlDocument() throws IOException, NetconfMessageBuilderException {
        URL url = Thread.currentThread().getContextClassLoader().getResource("TestXMLResources/getConfigTest.xml");
        Document document = DocumentUtils.loadXmlDocument(url.openStream());
        assertNotNull(document);
    }

    private String getSampleRPC() {
        return "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\"><get-config><source" +
                "><running /></source></get-config></rpc>";
    }

    private String getConfigString1() {
        return "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"5\">\n" +
                "    <nc:get-config xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "        <nc:source>\n" +
                "            <nc:running/>\n" +
                "        </nc:source>\n" +
                "    </nc:get-config>\n" +
                "</rpc>";
    }

    private String getTargetConfig() {
        return "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + "<copy-config>"
                + "<target><running/></target>"
                + "<source><running/></source>"
                + "</copy-config>"
                + "</rpc>";
    }

    private String getConfigString2() {
        return "<nc:rpc xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"5\">\n" +
                "    <nc:get-config>\n" +
                "        <nc:source>\n" +
                "            <nc:running/>\n" +
                "        </nc:source>\n" +
                "    </nc:get-config>\n" +
                "</nc:rpc>";
    }

    private String getInvalidEdit() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<nc:rpc xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" " +
                "message-id=\"urn:uuid:f2e7134f-2e04-4316-84cc-78a40fad3645\">\n" +
                "  <nc:edit-config>\n" +
                "    <nc:target>\n" +
                "      <nc:running/>\n" +
                "    </nc:target>\n" +
                "    <config>\n" +
                "      <device-manager xmlns=\"http://www.test-company.com/solutions/anv\"  " +
                "nc:operation=\"replace\">\n" +
                "      </device-manager>\n" +
                "    </config>\n" +
                "  </nc:edit-config>\n" +
                "</nc:rpc>";
    }
}
