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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
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
        Node childNode = DocumentUtils.getChildNodeByName(document, NetconfResources.RPC, NetconfResources.NETCONF_RPC_NS_1_0);
        assertEquals("nc:rpc", childNode.getNodeName());
        assertEquals("rpc", childNode.getLocalName());

        getConfigRequest = getConfigString1();
        document = DocumentUtils.stringToDocument(getConfigRequest);
        childNode = DocumentUtils.getChildNodeByName(document, NetconfResources.RPC, NetconfResources.NETCONF_RPC_NS_1_0);
        assertEquals("rpc", childNode.getNodeName());
        assertEquals("rpc", childNode.getLocalName());


        String invalidEdit = getInvalidEdit();
        document = DocumentUtils.stringToDocument(invalidEdit);
        childNode = DocumentUtils.getChildNodeByName(document, NetconfResources.EDIT_CONFIG_CONFIG, NetconfResources.NETCONF_RPC_NS_1_0);
        assertNull(childNode);
    }

    @Test
    public void testGetChildElements() throws NetconfMessageBuilderException {
        String invalidEdit = getInvalidEdit();
        Document document = DocumentUtils.stringToDocument(invalidEdit);
        List<Element> childElements = DocumentUtils.getChildElements(document.getDocumentElement(), NetconfResources.EDIT_CONFIG, NetconfResources.NETCONF_RPC_NS_1_0);
        assertEquals(1, childElements.size());
        Element editElement = childElements.get(0);
        assertEquals(NetconfResources.EDIT_CONFIG, editElement.getLocalName());
        assertEquals(NetconfResources.NETCONF_RPC_NS_1_0, editElement.getNamespaceURI());

        childElements = DocumentUtils.getChildElements(editElement, NetconfResources.EDIT_CONFIG_CONFIG, NetconfResources.NETCONF_RPC_NS_1_0);
        assertEquals(0, childElements.size());
    }

    @Test
    public void testGetDirectChildElements() throws Exception {
        String input = getDirectChildElementsInput();
        Document document = DocumentUtils.stringToDocument(input);
        List<Element> modules = DocumentUtils.getChildElements(document.getDocumentElement(), "module");
        assertEquals(1, modules.size());

        Element module = modules.get(0);
        Element moduleNameElt = DocumentUtils.getDirectChildElement(module, "name");
        assertNotNull(moduleNameElt);
        assertEquals("ietf-ip-aug", moduleNameElt.getTextContent());

        Element moduleReleaseElt = DocumentUtils.getDirectChildElement(module, "release");
        assertNull(moduleReleaseElt);

        List<Element> submoduleElts = DocumentUtils.getDirectChildElements(module, "submodule");
        assertEquals(1, submoduleElts.size());
        Element submoduleNameElt = DocumentUtils.getDirectChildElement(submoduleElts.get(0), "name");
        assertEquals("ietf-ip-aug-sub1", submoduleNameElt.getTextContent());
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
    public void testGetSubscriptionRequestWithEmptyOrNullStream() throws DOMException, ParseException, NetconfMessageBuilderException {
        NetconfRpcRequest request = new NetconfRpcRequest();
        String rpcInput = "<create-subscription xmlns=\"urn:ietf:params:xml:ns:netconf:notification:1.0\">\n" +
                "              <stream></stream>\n" +
                "          </create-subscription>";
        Element element = DocumentUtils.stringToDocument(rpcInput).getDocumentElement();
        request.setRpcInput(element);
        CreateSubscriptionRequest subscriptionRequest = DocumentUtils.getInstance().getSubscriptionRequest(request);
        assertEquals(NetconfResources.NETCONF, subscriptionRequest.getStream());

        rpcInput = "<create-subscription xmlns=\"urn:ietf:params:xml:ns:netconf:notification:1.0\">\n" +
                "  </create-subscription>";
        element = DocumentUtils.stringToDocument(rpcInput).getDocumentElement();
        request.setRpcInput(element);
        subscriptionRequest = DocumentUtils.getInstance().getSubscriptionRequest(request);
        assertEquals(NetconfResources.NETCONF, subscriptionRequest.getStream());
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
        return "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\"><get-config><source><running /></source></get-config></rpc>";
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
                "      <device-manager xmlns=\"http://www.test-company.com/solutions/anv\"  nc:operation=\"replace\">\n" +
                "      </device-manager>\n" +
                "    </config>\n" +
                "  </nc:edit-config>\n" +
                "</nc:rpc>";
    }

    private String getDirectChildElementsInput() {
        return "<?xml version='1.0' encoding='UTF-8'?>\n" +
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
                "  <data>\n" +
                "    <modules-state xmlns=\"urn:ietf:params:xml:ns:yang:ietf-yang-library\">\n" +
                "      <module>\n" +
                "        <name>ietf-ip-aug</name>\n" +
                "        <namespace>urn:ietf:params:xml:ns:yang:ietf-ip-aug</namespace>\n" +
                "        <submodule>\n" +
                "          <name>ietf-ip-aug-sub1</name>\n" +
                "          <submodule>\n" +
                "            <name>ietf-ip-aug-sub2</name>\n" +
                "          </submodule>\n" +
                "        </submodule>\n" +
                "        <deviation>\n" +
                "          <name>ietf-ip-dev</name>\n" +
                "          <revision>2016-11-16</revision>\n" +
                "        </deviation>\n" +
                "        <conformance-type>implement</conformance-type>\n" +
                "      </module>\n" +
                "    </modules-state>\n" +
                "  </data>\n" +
                "</rpc-reply>"
                ;
    }

    @Test
    public void testIsChildNodeExists() throws ParserConfigurationException, SAXException, IOException {
        String parentXML = "<InterfaceList>\n" +
                "    <InterfaceListKey1>InterfaceListKey1Value1</InterfaceListKey1>\n" +
                "    <InterfaceListKey2>InterfaceListKey2Value1</InterfaceListKey2>\n" +
                "    <InterfaceListLeafState1>InterfaceListLeafState1Value1</InterfaceListLeafState1>\n" +
                "    <InterfaceListconfig1>InterfaceListconfig1Value1</InterfaceListconfig1>\n" +
                "    <InterfaceStateContainer>\n" +
                "        <InterfaceStateContainerLeaf1>InterfaceStateContainerLeaf1Value1</InterfaceStateContainerLeaf1>\n" +
                "        <InterfaceStateContainerLeaf2>InterfaceStateContainerLeaf2Value1</InterfaceStateContainerLeaf2>\n" +
                "    </InterfaceStateContainer>\n" +
                "</InterfaceList>";

        String childXML = "<InterfaceListKey1>InterfaceListKey1Value1</InterfaceListKey1>";

        Node parent = DocumentUtils.getDocumentElement(parentXML);
        Node child = DocumentUtils.getDocumentElement(childXML);
        assertTrue(DocumentUtils.isChildNodeExists(parent, child));

        childXML = "<InterfaceListKey1>InterfaceListKey1</InterfaceListKey1>";
        child = DocumentUtils.getDocumentElement(childXML);
        assertFalse(DocumentUtils.isChildNodeExists(parent, child));

    }

    @Test
    public void testMatchingNodeXmlFileIncaseNotificationContainsPrefix() throws NetconfMessageBuilderException {
        String successPath = "/hardware-state/component/software/software/download/software-downloaded";
        String notification = "<notification xmlns=\"urn:ietf:params:xml:ns:netconf:notification:1.0\">\n" +
                "<eventTime>2018-10-18T15:35:19+00:00</eventTime>\n" +
                "<hw:hardware-state xmlns:hw=\"urn:ietf:params:xml:ns:yang:ietf-hardware\">\n" +
                "   <hw:component>\n" +
                "      <hw:name>Chassis</hw:name>\n" +
                "      <bbf-sim:software xmlns:bbf-sim=\"urn:bbf:yang:bbf-software-image-management-one-dot-one\">\n" +
                "         <bbf-sim:software>\n" +
                "            <bbf-sim:name>application_software</bbf-sim:name>\n" +
                "               <bbf-sim:download>\n" +
                "                  <bbf-sim:software-downloaded>\n" +
                "                     <bbf-sim:name>IMAGE_3</bbf-sim:name>\n" +
                "                  </bbf-sim:software-downloaded>\n" +
                "               </bbf-sim:download>\n" +
                "         </bbf-sim:software>\n" +
                "      </bbf-sim:software>\n" +
                "   </hw:component>\n" +
                "</hw:hardware-state>\n" +
                "</notification>";
        Document documentNotification = DocumentUtils.stringToDocument(notification);
        assertTrue(DocumentUtils.matchingXmlNotification(successPath, documentNotification));

        String failurePath = "/hardware-state/component/software/software/name/download/software-downloaded";
        assertFalse(DocumentUtils.matchingXmlNotification(failurePath, documentNotification));
    }

    @Test
    public void testMatchingNodeXmlFileIncaseNotificationDoesnotContainsPrefix() throws NetconfMessageBuilderException {
        String sucessPath = "/hardware-state/component/software/software/download/software-downloaded";
        String notification = "<notification xmlns=\"urn:ietf:params:xml:ns:netconf:notification:1.0\">\n" +
                "<eventTime>2018-10-18T15:35:19+00:00</eventTime>\n" +
                "<hardware-state xmlns:hw=\"urn:ietf:params:xml:ns:yang:ietf-hardware\">\n" +
                "   <component>\n" +
                "      <name>Chassis</name>\n" +
                "      <software xmlns:bbf-sim=\"urn:bbf:yang:bbf-software-image-management-one-dot-one\">\n" +
                "         <software>\n" +
                "            <name>application_software</name>\n" +
                "               <download>\n" +
                "                  <software-downloaded>\n" +
                "                     <name>IMAGE_3</name>\n" +
                "                  </software-downloaded>\n" +
                "               </download>\n" +
                "         </software>\n" +
                "      </software>\n" +
                "   </component>\n" +
                "</hardware-state>\n" +
                "</notification>";
        Document documentNotification = DocumentUtils.stringToDocument(notification);
        assertTrue(DocumentUtils.matchingXmlNotification(sucessPath, documentNotification));

        String failurePath = "/hardware-state/component/software/software/name/download/software-downloaded";
        assertFalse(DocumentUtils.matchingXmlNotification(failurePath, documentNotification));
    }
}
