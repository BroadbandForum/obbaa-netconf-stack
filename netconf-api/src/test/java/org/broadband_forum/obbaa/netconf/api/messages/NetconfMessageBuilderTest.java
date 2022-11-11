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

import static org.broadband_forum.obbaa.netconf.api.util.DocumentUtils.getDocFromFile;
import static org.broadband_forum.obbaa.netconf.api.util.DocumentUtils.getNewDocument;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.api.LogAppNames;
import org.broadband_forum.obbaa.netconf.api.server.NetconfQueryParams;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceConstants;
import org.custommonkey.xmlunit.DifferenceListener;
import org.custommonkey.xmlunit.ElementNameAndTextQualifier;
import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class NetconfMessageBuilderTest extends XMLTestCase {

    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(NetconfMessageBuilderTest.class, LogAppNames.NETCONF_LIB);

    @Override
    protected void setUp() throws Exception {
        XMLUnit.setIgnoreWhitespace(true);
    }

    public void testNetconfRpcDocumentBuild() throws NetconfMessageBuilderException {
        Document doc = new PojoToDocumentTransformer().newNetconfRpcDocument("101").build();
        assertNotNull(doc.getChildNodes().item(0));
        assertEquals(NetconfResources.RPC, doc.getChildNodes().item(0).getNodeName());

        doc = new PojoToDocumentTransformer().newNetconfRpcDocument("101").build();
        assertNotNull(doc.getChildNodes().item(0));
        assertEquals(NetconfResources.RPC, doc.getChildNodes().item(0).getNodeName());
    }

    public void testGetConfigRpcDocumentBuild() throws Exception {
        Document tempDoc = getNewDocument();
        Element topElement = tempDoc.createElement("top");
        topElement.setAttribute("xmlns", "http://example.com/schema/1.2/config");
        Element users = tempDoc.createElement("users");
        topElement.appendChild(users);
        DocumentUtils.prettyPrint(tempDoc);
        NetconfFilter axsFilter = new NetconfFilter().setType(NetconfResources.SUBTREE_FILTER).addXmlFilter(topElement);
        axsFilter.toString();// make sure this does not fail
        Map<String, List<QName>> fieldValues = new HashMap<>();
        fieldValues.put("user", Arrays.asList(QName.create("www.test.com", "userName")));
        Document actualDoc = new PojoToDocumentTransformer().newNetconfRpcDocument("101")
                .addGetConfigElement(StandardDataStores.RUNNING, axsFilter, null, 0, NetconfQueryParams.UNBOUNDED, fieldValues).build();
        verifyFields(actualDoc.getDocumentElement());

        Node rpc = actualDoc.getChildNodes().item(0);
        assertNotNull(rpc);
        assertEquals(NetconfResources.RPC, rpc.getNodeName());

        Node getConfig = rpc.getChildNodes().item(0);
        assertNotNull(getConfig);
        assertEquals(NetconfResources.GET_CONFIG, getConfig.getNodeName());

        Node source = getConfig.getChildNodes().item(0);
        assertEquals(NetconfResources.DATA_SOURCE, source.getNodeName());
        assertEquals(StandardDataStores.RUNNING, source.getChildNodes().item(0).getNodeName());
        Node filter = getConfig.getChildNodes().item(1);
        Node top = filter.getChildNodes().item(0);
        assertEquals("top", top.getNodeName());

        assertEquals("http://example.com/schema/1.2/config", top.getAttributes().getNamedItem("xmlns").getNodeValue());
        assertEquals("users", top.getChildNodes().item(0).getNodeName());
    }

    private void verifyFields(Element element) {
        NodeList fieldElements = element.getElementsByTagNameNS(NetconfResources.EXTENSION_NS, NetconfResources.FIELDS);
        assertEquals(1, fieldElements.getLength());
        Element fieldElement = (Element) fieldElements.item(0);
        String dataNodeValue = fieldElement.getElementsByTagName(NetconfResources.DATA_NODE).item(0).getTextContent();
        assertEquals("user", dataNodeValue);
        String attributeValue = fieldElement.getElementsByTagName(NetconfResources.ATTRIBUTE).item(0).getTextContent();
        assertEquals("userName", attributeValue);
    }

    public void testEditConfigRpcDocumentBuild() throws Exception {
        Document tempDoc = getNewDocument();
        Element topElement = tempDoc.createElement("top");
        topElement.setAttribute("xmlns", "http://example.com/schema/1.2/config");
        Element users = tempDoc.createElement("users");
        topElement.setAttributeNS(NetconfResources.NETCONF_RPC_NS_1_0, NetconfResources.EDIT_CONFIG_OPERATION, EditConfigOperations.DELETE);
        topElement.appendChild(users);

        EditConfigElement editConfigElement = new EditConfigElement().setConfigElementContents(Collections.singletonList(topElement));
        editConfigElement.toString();

        PojoToDocumentTransformer docBuilder = new PojoToDocumentTransformer().newNetconfRpcDocument("101").addEditConfigElement(false,"backup",
                EditConfigDefaultOperations.MERGE, EditConfigTestOptions.TEST_THEN_SET, EditConfigErrorOptions.CONTINUE_ON_ERROR, 0,
                editConfigElement, "test", "testId");
        Node rpc = getRPCNodeWithAssertCheck(docBuilder);

        Node editConfig = rpc.getChildNodes().item(0);
        assertNotNull(editConfig);
        assertEquals(NetconfResources.EDIT_CONFIG, editConfig.getNodeName());

        Node target = editConfig.getChildNodes().item(0);
        assertEquals(NetconfResources.DATA_TARGET, target.getNodeName());
        assertEquals("backup", target.getChildNodes().item(0).getNodeName());
        Node defaultOper = editConfig.getChildNodes().item(1);
        assertNotNull(defaultOper);
        assertEquals(EditConfigDefaultOperations.MERGE, defaultOper.getTextContent());

        Node tespOption = editConfig.getChildNodes().item(2);
        assertEquals(EditConfigTestOptions.TEST_THEN_SET, tespOption.getTextContent());

        Node errorOption = editConfig.getChildNodes().item(3);
        assertEquals(EditConfigErrorOptions.CONTINUE_ON_ERROR, errorOption.getTextContent());

        Node configElement = editConfig.getChildNodes().item(4);
        assertEquals(NetconfResources.EDIT_CONFIG_CONFIG, configElement.getNodeName());

        Node topUsersElement = configElement.getChildNodes().item(0);
        assertEquals("top", topUsersElement.getNodeName());

        assertEquals(NetconfResources.EDIT_CONFIG_OPERATION,
                topUsersElement.getAttributes().getNamedItem(NetconfResources.EDIT_CONFIG_OPERATION).getNodeName());
        assertEquals(EditConfigOperations.DELETE, topUsersElement.getAttributes().getNamedItem(NetconfResources.EDIT_CONFIG_OPERATION)
                .getNodeValue());
    }

    @Test
    public void testCopyConfigRpcDocumentBuild() throws Exception {
        PojoToDocumentTransformer docBuilder = new PojoToDocumentTransformer().newNetconfRpcDocument("101").addCopyConfigElement(
                false, "https://user:password@example.com/cfg/backup.txt", true, StandardDataStores.RUNNING, false, null);
        Node rpc = getRPCNodeWithAssertCheck(docBuilder);

        Node copyConfig = rpc.getChildNodes().item(0);
        assertNotNull(copyConfig);
        assertEquals(NetconfResources.COPY_CONFIG, copyConfig.getNodeName());

        targetAssert(copyConfig);
        
        Node source = copyConfig.getChildNodes().item(1);
        assertEquals(NetconfResources.DATA_SOURCE, source.getNodeName());
        Node src = source.getChildNodes().item(0);
        assertEquals(NetconfResources.URL, src.getNodeName());
        assertEquals("https://user:password@example.com/cfg/backup.txt", src.getTextContent());
        assertEquals(1, source.getChildNodes().getLength());
    }

    private void targetAssert(Node nodeToAssert) {
        Node target = nodeToAssert.getChildNodes().item(0);
        assertEquals(NetconfResources.DATA_TARGET, target.getNodeName());
        assertEquals(1, target.getChildNodes().getLength());
        Node targetContent = target.getFirstChild();
        assertEquals(StandardDataStores.RUNNING, targetContent.getNodeName());
        assertEquals("", targetContent.getTextContent());
    }

    private Node getRPCNodeWithAssertCheck(PojoToDocumentTransformer docBuilder) throws NetconfMessageBuilderException {
        Document actualDoc = docBuilder.build();

        LOGGER.info(DocumentUtils.documentToString(actualDoc));

        Node rpc = actualDoc.getChildNodes().item(0);
        assertNotNull(rpc);
        assertEquals(NetconfResources.RPC, rpc.getNodeName());
        return rpc;
    }

    @Test
    public void testDeleteConfigRpcDocumentBuild() throws Exception {
        PojoToDocumentTransformer docBuilder = new PojoToDocumentTransformer().newNetconfRpcDocument("101").addDeleteConfigElement(
                StandardDataStores.RUNNING);
        Node rpc = getRPCNodeWithAssertCheck(docBuilder);

        Node deleteConfig = rpc.getChildNodes().item(0);
        assertNotNull(deleteConfig);
        assertEquals(NetconfResources.DELETE_CONFIG, deleteConfig.getNodeName());

        targetAssert(deleteConfig);
    }

    @Test
    public void testLockRpcDocumentBuild() throws Exception {
        PojoToDocumentTransformer docBuilder = new PojoToDocumentTransformer().newNetconfRpcDocument("101").addLockElement(
                StandardDataStores.RUNNING);
        Node rpc = getRPCNodeWithAssertCheck(docBuilder);

        Node lock = rpc.getChildNodes().item(0);
        assertNotNull(lock);
        assertEquals(NetconfResources.LOCK, lock.getNodeName());

        targetAssert(lock);
    }

    @Test
    public void testUnLockRpcDocumentBuild() throws Exception {
        PojoToDocumentTransformer docBuilder = new PojoToDocumentTransformer().newNetconfRpcDocument("101").addUnLockElement(
                StandardDataStores.RUNNING);
        Node rpc = getRPCNodeWithAssertCheck(docBuilder);

        Node unlock = rpc.getChildNodes().item(0);
        assertNotNull(unlock);
        assertEquals(NetconfResources.UNLOCK, unlock.getNodeName());

        targetAssert(unlock);
    }

    public void testGetRpcDocumentBuild() throws Exception {
        Document tempDoc = getNewDocument();
        Element topElement = tempDoc.createElement("top");
        topElement.setAttribute("xmlns", "http://example.com/schema/1.2/config");
        Element users = tempDoc.createElement("users");
        topElement.appendChild(users);
        DocumentUtils.prettyPrint(tempDoc);
        NetconfFilter axsFilter = new NetconfFilter().setType(NetconfResources.SUBTREE_FILTER).addXmlFilter(topElement);
        axsFilter.toString();// to make sure this does not end up failing..
        Document actualDoc = new PojoToDocumentTransformer().newNetconfRpcDocument("101").addGetElement(axsFilter, null,null, 0,
                NetconfQueryParams.UNBOUNDED, Collections.<String, List<QName>>emptyMap()).build();

        Node rpc = actualDoc.getChildNodes().item(0);
        assertNotNull(rpc);
        assertEquals(NetconfResources.RPC, rpc.getNodeName());

        Node get = rpc.getChildNodes().item(0);
        assertNotNull(get);
        assertEquals(NetconfResources.GET, get.getNodeName());

        Node filter = get.getChildNodes().item(0);
        Node top = filter.getChildNodes().item(0);
        assertEquals("top", top.getNodeName());

        assertEquals("http://example.com/schema/1.2/config", top.getAttributes().getNamedItem("xmlns").getNodeValue());
        assertEquals("users", top.getChildNodes().item(0).getNodeName());

    }

    @Test
    public void testCloseSessionRpcDocumentBuild() throws Exception {
        PojoToDocumentTransformer docBuilder = new PojoToDocumentTransformer().newNetconfRpcDocument("101").addCloseSessionElement();
        Node rpc = getRPCNodeWithAssertCheck(docBuilder);

        Node closeSession = rpc.getChildNodes().item(0);
        assertNotNull(closeSession);
        assertEquals(NetconfResources.CLOSE_SESSION, closeSession.getNodeName());

    }

    @Test
    public void testGetCopyConfig() throws Exception {
        String inputFile = "copyConfig.xml";
        Document document = getDocumentFile(inputFile);
        CopyConfigRequest request = DocumentToPojoTransformer.getCopyConfig(document);

        assertEquals("101", request.getMessageId());
        assertEquals("https://user:password@example.com/cfg/new.txt", request.getSource());
        assertEquals(StandardDataStores.RUNNING, request.getTarget());
        // one more test
        request.setSourceRunning();
        assertEquals(StandardDataStores.RUNNING, request.getSource());
    }

    private Document getDocumentFile(String inputFile) {
        URL url = Thread.currentThread().getContextClassLoader().getResource(inputFile);
        File file = new File(url.getPath());
        return getDocFromFile(file);
    }

    @Test
    public void testGetCopyConfig2() throws Exception {
        String inputFile = "copyConfigTargetIsUrl.xml";
        Document document = getDocumentFile(inputFile);
        CopyConfigRequest request = DocumentToPojoTransformer.getCopyConfig(document);

        assertEquals("101", request.getMessageId());
        assertEquals("https://user:password@example.com/cfg/new.txt", request.getSource());
        assertEquals("https://user:password@example.com/cfg/target.txt", request.getTarget());
    }

    @Test
    public void testGetCopyConfig3() throws Exception {
        String inputFile = "copyConfigSourceIsConfigElement.xml";
        URL url = Thread.currentThread().getContextClassLoader().getResource(inputFile);
        File file = new File(url.getPath());
        Document document = getDocFromFile(file);
        CopyConfigRequest request = DocumentToPojoTransformer.getCopyConfig(document);

        String inputFile2 = "copyConfigSourceConfigElement.xml";
        url = Thread.currentThread().getContextClassLoader().getResource(inputFile2);
        file = new File(url.getPath());
        document = getDocFromFile(file);

        assertEquals("101", request.getMessageId());
        assertXMLEquals(document.getDocumentElement(), request.getSourceConfigElement());
        assertEquals("running", request.getTarget());
    }

    @Test
    public void testGetCopyConfigErrorScenario1() throws Exception {
        String inputFile = "copyConfigErrorScenario1.xml";
        checkCopyConfigErrorScenario(inputFile);
    }

    private void checkCopyConfigErrorScenario(String inputFile) {
        Document document = getDocumentFile(inputFile);
        try {
            DocumentToPojoTransformer.getCopyConfig(document);
            fail("Expected exception did not occur");
        } catch (NetconfMessageBuilderException e) {
            // fine
        }
    }

    @Test
    public void testGetCopyConfigErrorScenario2() throws Exception {
        checkCopyConfigErrorScenario("copyConfigErrorScenario2.xml");
    }

    @Test
    public void testGetCopyConfigErrorScenario3() throws Exception {
        checkCopyConfigErrorScenario("copyConfigErrorScenario3.xml");
    }

    @Test
    public void testGetCopyConfigErrorScenario4() throws Exception {
        checkCopyConfigErrorScenario("copyConfigErrorScenario4.xml");
    }

    @Test
    public void testGetCopyConfigErrorScenario5() throws Exception {
        checkCopyConfigErrorScenario("copyConfigErrorScenario5.xml");
    }

    @Test
    public void testGetCopyConfigErrorScenario6() throws Exception {
        checkCopyConfigErrorScenario("copyConfigErrorScenario6.xml");
    }

    @Test
    public void testGetGetWithoutFilter() throws Exception {
        Document document = getDocumentFile("getWithoutFilter.xml");
        GetRequest request = DocumentToPojoTransformer.getGet(document);

        assertEquals("101", request.getMessageId());
        assertEquals(null, request.getFilter());
    }

    @Test
    public void testGetGet() throws Exception {
        Document document = getDocumentFile("get.xml");
        GetRequest request = DocumentToPojoTransformer.getGet(document);

        assertEquals("101", request.getMessageId());
        assertEquals(NetconfResources.SUBTREE_FILTER, request.getFilter().getType());
        assertNotNull(request.getFilter().getXmlFilterElements());
    }

    @Test
    public void testGetGetConfig() throws Exception {
        Document document = getDocumentFile("getConfig.xml");
        GetConfigRequest request = DocumentToPojoTransformer.getGetConfig(document);

        assertEquals("101", request.getMessageId());
        assertEquals(NetconfResources.SUBTREE_FILTER, request.getFilter().getType());
        assertNotNull(request.getFilter().getXmlFilterElements());
        assertEquals(StandardDataStores.RUNNING, request.getSource());
    }

    @Test
    public void testGetGetConfigWithoutFilter() throws Exception {
        Document document = getDocumentFile("getConfigWithoutFilter.xml");
        GetConfigRequest request = DocumentToPojoTransformer.getGetConfig(document);

        assertEquals("101", request.getMessageId());
        assertEquals(null, request.getFilter());
    }

    /*
     * @Test public void testGetBytesFromDocument() throws Exception{ URL url =
     * Thread.currentThread().getContextClassLoader().getResource("invalidXmlFile.xml"); File file = new File(url.getPath()); Document
     * document = getDocFromFile(file); try{ DocumentToPojoTransformer.getBytesFromDocument(document);
     * fail("Expected exception did not occur"); }catch(NetconfMessageBuilderException e){ //ok } }
     */

    @Test
    public void testGetKillSession() throws Exception {
        Document document = getDocumentFile("killSession.xml");
        KillSessionRequest request = DocumentToPojoTransformer.getKillSession(document);
        assertEquals("101", request.getMessageId());
        assertEquals(new Integer(4), request.getSessionId());
        // check the transformation
        assertXMLEquals(document.getDocumentElement(), request.getRequestDocument().getDocumentElement());
        LOGGER.info(request.toString());
    }

    public static void assertXMLEquals(Element expectedOutput, Element actualOutput) throws SAXException, IOException {
        String expected = "";
        try {
            expected = DocumentUtils.documentToPrettyString(expectedOutput);
        } catch (NetconfMessageBuilderException e) {
            LOGGER.error("Error while converting document to String ", e);
            throw new IOException("Error while converting document to String ", e);
        }
        String actual = "";
        try {
            actual = DocumentUtils.documentToPrettyString(actualOutput);
        } catch (NetconfMessageBuilderException e) {
            LOGGER.error("Error while converting document to String ", e);
            throw new IOException("Error while converting document to String ", e);
        }

        LOGGER.info("expected: \n" + expected);
        LOGGER.info("actual: \n" + actual);
        Diff diff = new Diff(expected, actual);
        diff.overrideElementQualifier(new ElementNameAndTextQualifier());
        diff.overrideDifferenceListener(new DifferenceListener() {

            @Override
            public void skippedComparison(Node arg0, Node arg1) {

            }

            @Override
            public int differenceFound(Difference arg0) {
                if (DifferenceConstants.CHILD_NODELIST_SEQUENCE.equals(arg0)) {
                    return RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
                }
                return RETURN_ACCEPT_DIFFERENCE;

            }
        });
        XMLUnit.setIgnoreAttributeOrder(true);
        XMLUnit.setNormalizeWhitespace(true);
        assertTrue(diff.identical());
    }

    @Test
    public void testGetKillSessionThrowsException() {
        Document document = getDocumentFile("killSessionInvalid.xml");
        try {
            DocumentToPojoTransformer.getKillSession(document);
            fail("Expected exception here.");
        } catch (NetconfMessageBuilderException e) {
            // we are fine here
        }

    }

    @Test
    public void testGetDeleteCofig() throws NetconfMessageBuilderException, SAXException, IOException {
        Document document = getDocumentFile("deleteConfig.xml");
        DeleteConfigRequest request = DocumentToPojoTransformer.getDeleteConfig(document);
        assertEquals("101", request.getMessageId());
        assertEquals("startup", request.getTarget());
        // check the transformation
        assertXMLEquals(document.getDocumentElement(), request.getRequestDocument().getDocumentElement());
        LOGGER.info(request.toString());

        // one more test
        request.setTargetRunning();
        assertEquals(StandardDataStores.RUNNING, request.getTarget());
    }

    @Test
    public void testKillSessionRpcDocumentBuild() throws Exception {
        PojoToDocumentTransformer docBuilder = new PojoToDocumentTransformer().newNetconfRpcDocument("101").addKillSessionElement(23);
        Node rpc = getRPCNodeWithAssertCheck(docBuilder);

        Node killSession = rpc.getChildNodes().item(0);
        assertNotNull(killSession);
        assertEquals(NetconfResources.KILL_SESSION, killSession.getNodeName());

        Node sessionIdElement = killSession.getFirstChild();
        assertNotNull(sessionIdElement);
        assertEquals(NetconfResources.SESSION_ID, sessionIdElement.getNodeName());
        assertEquals("23", sessionIdElement.getTextContent());

    }

    @Test
    public void testHelloMessageBuild() throws Exception {
        Set<String> caps = new HashSet<String>();
        caps.add("urn:ietf:params:netconf:base:1.1");
        caps.add("http://example.net/router/2.3/myfeature");
        PojoToDocumentTransformer docBuilder = new PojoToDocumentTransformer().newServerHelloMessage(caps, 10);
        Document actualDoc = docBuilder.build();

        LOGGER.info(DocumentUtils.documentToString(actualDoc));

        Node hello = actualDoc.getChildNodes().item(0);
        assertNotNull(hello);
        assertEquals(NetconfResources.HELLO, hello.getNodeName());
        Node capabilities = hello.getFirstChild();
        assertEquals(NetconfResources.CAPABILITIES, capabilities.getNodeName());
        assertTrue(capabilities.getChildNodes().getLength() > 0);

        Set<String> actualCaps = new HashSet<String>();
        for (int i = 0; i < capabilities.getChildNodes().getLength(); i++) {
            Node capability = capabilities.getChildNodes().item(i);
            assertEquals(NetconfResources.CAPABILITY, capability.getNodeName());
            actualCaps.add(capability.getTextContent());
        }
        assertEquals(caps, actualCaps);

        Node sessionIdElement = hello.getChildNodes().item(1);
        assertEquals(NetconfResources.SESSION_ID, sessionIdElement.getNodeName());
        assertEquals("10", sessionIdElement.getTextContent());

    }

    @Test
    public void testRpcReplyBuild() throws Exception {

        Document tempDoc = getNewDocument();
        Element dataElement = tempDoc.createElement(NetconfResources.RPC_REPLY_DATA);
        Element users = tempDoc.createElement("users");
        dataElement.appendChild(users);

        Map<String, String> additionalAttributes = new HashMap<String, String>();
        // NetconfServer should send all the attributes set by the client on rpc message
        additionalAttributes.put("dummyAttr1", "dummyAttrValue");
        Map<String, String> nsByPrefix = new HashMap<>();
        nsByPrefix.put("prefix", "http://some.namespace");
        NetconfRpcError error = new NetconfRpcError(NetconfRpcErrorTag.IN_USE, NetconfRpcErrorType.Transport,
                NetconfRpcErrorSeverity.Error, "error-message").setErrorAppTag("app-tag").setErrorPath("prefix:rootObject/prefix:subObject", nsByPrefix)
                .addErrorInfoElement(NetconfRpcErrorInfo.ErrElement, "info1Content");
        // .addErrorInfoElement("info1", "info1Content");
        PojoToDocumentTransformer docBuilder = new PojoToDocumentTransformer().newNetconfRpcReplyDocument("1", additionalAttributes)
                .addData(dataElement).addRpcError(error).addOk();
        Document actualDoc = docBuilder.build();

        LOGGER.info(DocumentUtils.documentToString(actualDoc));

        Node rpcReply = actualDoc.getChildNodes().item(0);
        assertEquals(NetconfResources.RPC_REPLY, rpcReply.getNodeName());
        Node data = rpcReply.getFirstChild();
        assertEquals(NetconfResources.RPC_REPLY_DATA, data.getNodeName());
        assertEquals("users", data.getFirstChild().getNodeName());

        // there can be more than on as well
        NodeList rpcErrors = actualDoc.getElementsByTagName(NetconfResources.RPC_ERROR);
        assertEquals(1, rpcErrors.getLength());
        Node rpcError = rpcErrors.item(0);
        assertEquals(NetconfResources.RPC_ERROR_TYPE, rpcError.getChildNodes().item(0).getNodeName());
        assertEquals(NetconfRpcErrorType.Transport.value(), rpcError.getChildNodes().item(0).getTextContent());

        assertEquals(NetconfResources.RPC_ERROR_TAG, rpcError.getChildNodes().item(1).getNodeName());
        assertEquals("in-use", rpcError.getChildNodes().item(1).getTextContent());

        assertEquals(NetconfResources.RPC_ERROR_SEVERITY, rpcError.getChildNodes().item(2).getNodeName());
        assertEquals(NetconfRpcErrorSeverity.Error.value(), rpcError.getChildNodes().item(2).getTextContent());

        assertEquals(NetconfResources.RPC_ERROR_APP_TAG, rpcError.getChildNodes().item(3).getNodeName());
        assertEquals("app-tag", rpcError.getChildNodes().item(3).getTextContent());

        Element errorPathElement = (Element)rpcError.getChildNodes().item(4);
        assertEquals(NetconfResources.RPC_ERROR_PATH, errorPathElement.getNodeName());
        assertEquals("prefix:rootObject/prefix:subObject", errorPathElement.getTextContent());
        assertEquals("http://some.namespace", errorPathElement.lookupNamespaceURI("prefix"));

        assertEquals(NetconfResources.RPC_ERROR_MESSAGE, rpcError.getChildNodes().item(5).getNodeName());
        assertEquals("error-message", rpcError.getChildNodes().item(5).getTextContent());

        assertEquals(NetconfResources.RPC_ERROR_INFO, rpcError.getChildNodes().item(6).getNodeName());
        assertEquals("error-info", rpcError.getChildNodes().item(6).getNodeName());

        assertEquals(NetconfResources.OK, rpcReply.getChildNodes().item(2).getNodeName());
    }

    @Test
    public void testErrorRpcReplyBuild() throws Exception {

        Document tempDoc = getNewDocument();
        Element dataElement = tempDoc.createElement(NetconfResources.RPC_REPLY_DATA);
        Element users = tempDoc.createElement("users");
        dataElement.appendChild(users);

        Map<String, String> additionalAttributes = new HashMap<String, String>();
        // NetconfServer should send all the attributes set by the client on rpc message
        additionalAttributes.put("dummyAttr1", "dummyAttrValue");
        NetConfResponse response = new NetConfResponse();
        NetconfRpcError rpcError = new NetconfRpcError(NetconfRpcErrorTag.MISSING_ATTRIBUTE, NetconfRpcErrorType.RPC,
                NetconfRpcErrorSeverity.Error, null).addErrorInfoElement(NetconfRpcErrorInfo.BadAttribute, "message-id")
                .addErrorInfoElement(NetconfRpcErrorInfo.BadElement, "rpc");
        response.addError(rpcError);
        assertEquals(load("/missing-message-id-error.xml"), response.responseToString());

    }

    public static String load(String name) {
        StringBuffer sb = new StringBuffer();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(NetconfMessageBuilderTest.class.getResourceAsStream(name)))) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append(System.getProperty("line.separator"));
            }
            return sb.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testAddMessageId() throws NetconfMessageBuilderException {
        Document document = getDocumentFile("copyConfig.xml");

        DocumentToPojoTransformer.addMessageId(document, 102);
        CopyConfigRequest request = DocumentToPojoTransformer.getCopyConfig(document);
        assertEquals("102", request.getMessageId());
    }

    @Test
    public void testAddNetcofnNamespace() {
        Document document = getDocumentFile("copyConfig.xml");
        DocumentToPojoTransformer.addNetconfNamespace(document, NetconfResources.NETCONF_RPC_NS_1_0);
        assertEquals(NetconfResources.NETCONF_RPC_NS_1_0, document.getDocumentElement().getAttribute(NetconfResources.XMLNS));
    }

    @Test
    public void testGetHelloMessage() throws NetconfMessageBuilderException {
        Document document = getDocumentFile("hello.xml");
        NetconfHelloMessage hello = DocumentToPojoTransformer.getHelloMessage(document);

        assertEquals(hello.getSessionId(), 47);

        Set<String> expectedCaps = new HashSet<String>();
        expectedCaps.add("urn:ietf:params:netconf:base:1.0");
        expectedCaps.add("urn:ietf:params:netconf:base:1.1");
        expectedCaps.add("urn:ietf:params:netconf:capability:candidate:1.0");

        assertEquals(expectedCaps, hello.getCapabilities());

        hello.toString();
        NetconfHelloMessage hello2 = DocumentToPojoTransformer.getHelloMessage(hello.getRequestDocument());
        assertEquals(expectedCaps, hello2.getCapabilities());

    }

    @Test
    public void testProcessChunkedMessageSingleChunk() throws IOException, NetconfMessageBuilderException {
        isProcessedChunkedMesageEqual("sampleChunkedMessage1.txt", "expectedMessage1.txt");

    }

    @Test
    public void testProcessChunkedMessageSingleChunk_withNewLine() throws IOException, NetconfMessageBuilderException {
        isProcessedChunkedMesageEqual("sampleChunkedMessageWithNewLine.txt", "expectedMessageWithNewLine.txt");

    }

    @Test
    public void testProcessChunkedMessageSingleChunk_multipleChunksWithNewLinetxt() throws IOException, NetconfMessageBuilderException {
        isProcessedChunkedMesageEqual("sampleChunkedMessage_multipleChunksWithNewLine.txt", "expectedMessage_multipleChunksWithNewLinetxt");

    }

    @Test
    public void testProcessChunkedMessageThrowsException() throws IOException, NetconfMessageBuilderException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        URL url = Thread.currentThread().getContextClassLoader().getResource("sampleChunkedMessageInvalid.txt");
        File file = new File(url.getPath());
        byte[] fileData = new byte[(int) file.length()];
        DataInputStream dis = new DataInputStream(new FileInputStream(file));
        dis.readFully(fileData);
        dis.close();
        stream.write(fileData);

        try {
            DocumentToPojoTransformer.processChunkedMessage(stream.toString());
            fail("Did not get exception");

        } catch (NetconfMessageBuilderException e) {
            // fine
        }

    }

    @Test
    public void testProcessChunkedMessageThrowsException3() throws IOException, NetconfMessageBuilderException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        URL url = Thread.currentThread().getContextClassLoader().getResource("sampleChunkedMessageInvalid3.txt");
        File file = new File(url.getPath());
        byte[] fileData = new byte[(int) file.length()];
        DataInputStream dis = new DataInputStream(new FileInputStream(file));
        dis.readFully(fileData);
        dis.close();
        stream.write(fileData);

        try {
            DocumentToPojoTransformer.processChunkedMessage(stream.toString());
            fail("Did not get exception");

        } catch (NetconfMessageBuilderException e) {
            assertEquals("java.lang.StringIndexOutOfBoundsException: String index out of range: 587", e.getCause().toString());
            // fine
        }
    }

    @Test
    public void testProcessChunkedMessageTwoChunks() throws IOException, NetconfMessageBuilderException {
        isProcessedChunkedMesageEqual("sampleChunkedMessage2.txt", "expectedMessage2.txt");

    }

    @Test
    public void testProcessChunkedMessageThreeChunks() throws IOException, NetconfMessageBuilderException {
        String inputFile = "sampleChunkedMessage3.txt";
        String outputFile = "expectedMessage3.txt";

        isProcessedChunkedMesageEqual(inputFile, outputFile);
    }

    private void isProcessedChunkedMesageEqual(String inputFile, String outputFile) throws IOException, NetconfMessageBuilderException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        URL url = Thread.currentThread().getContextClassLoader().getResource(inputFile);
        File file = new File(url.getPath());
        byte[] fileData = new byte[(int) file.length()];
        DataInputStream dis = new DataInputStream(new FileInputStream(file));
        dis.readFully(fileData);
        dis.close();
        stream.write(fileData);

        url = Thread.currentThread().getContextClassLoader().getResource(outputFile);
        file = new File(url.getPath());
        fileData = new byte[(int) file.length()];
        dis = new DataInputStream(new FileInputStream(file));
        dis.readFully(fileData);
        dis.close();
        ByteArrayOutputStream expectedStream = new ByteArrayOutputStream();
        expectedStream.write(fileData);

        String xml = DocumentToPojoTransformer.processChunkedMessage(stream.toString());

        assertEquals(expectedStream.toString(), xml);
    }

    @Test
    public void testGetMessageChunks() throws IOException, NetconfMessageBuilderException {
        String inputFile = "sampleChunkedMessage2.txt";
        String outputFile = "expectedMessage2.txt";
        int chunkSize = 580;

        isProcessedChunkedMesageEqual(inputFile, outputFile, chunkSize);

    }

    private void isProcessedChunkedMesageEqual(String inputFile, String outputFile, int chunkSize) throws IOException, NetconfMessageBuilderException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        URL url = Thread.currentThread().getContextClassLoader().getResource(outputFile);
        File file = new File(url.getPath());
        byte[] fileData = new byte[(int) file.length()];
        DataInputStream dis = new DataInputStream(new FileInputStream(file));
        dis.readFully(fileData);
        dis.close();
        stream.write(fileData);
        String message = stream.toString();


        String chunkedMessage = DocumentToPojoTransformer.chunkMessage(chunkSize, message);

        url = Thread.currentThread().getContextClassLoader().getResource(inputFile);
        file = new File(url.getPath());
        fileData = new byte[(int) file.length()];
        dis = new DataInputStream(new FileInputStream(file));
        dis.readFully(fileData);
        dis.close();
        ByteArrayOutputStream expectedStream = new ByteArrayOutputStream();
        expectedStream.write(fileData);

        assertEquals(expectedStream.toString(), chunkedMessage);
    }

    @Test
    public void testGetMessageChunks3() throws IOException, NetconfMessageBuilderException {
        int chunkSize = 1000;
        isProcessedChunkedMesageEqual("sampleChunkedMessage5.txt", "expectedMessage2.txt", chunkSize);

    }

    @Test
    public void testGetMessageChunks2() throws IOException, NetconfMessageBuilderException {
        int chunkSize = 100;
        isProcessedChunkedMesageEqual("sampleChunkedMessage4.txt", "expectedMessage2.txt", chunkSize);

    }

    @Test
    public void testGetCloseSession() throws NetconfMessageBuilderException {
        Document request = getDocumentFile("closeSession.xml");
        CloseSessionRequest closeSessionReq = DocumentToPojoTransformer.getCloseSession(request);

        assertEquals("101", closeSessionReq.getMessageId());
    }

    @Test
    public void testGetEditConfig() throws Exception {
        Document request = getDocumentFile("editConfig.xml");
        EditConfigRequest editConfigReq = DocumentToPojoTransformer.getEditConfig(request);

        assertEquals("101", editConfigReq.getMessageId());
        assertEquals(EditConfigDefaultOperations.REPLACE, editConfigReq.getDefaultOperation());
        assertEquals(EditConfigErrorOptions.CONTINUE_ON_ERROR, editConfigReq.getErrorOption());
        assertEquals(EditConfigTestOptions.TEST_ONLY, editConfigReq.getTestOption());

        assertEquals(StandardDataStores.RUNNING, editConfigReq.getTarget());

        assertNotNull(editConfigReq.getConfigElement());
        assertEquals("configuration", editConfigReq.getConfigElement().getConfigElementContents().get(0).getLocalName());
        assertTrue(editConfigReq.getConfigElement().getConfigElementContents().get(0).getChildNodes().getLength() > 0);

        LOGGER.debug(DocumentUtils.documentToString(editConfigReq.getRequestDocument()));
    }

    @Test
    public void testGetEditConfigWithoutDefaults() throws Exception {
        Document request = getDocumentFile("editConfigWithoutDefaults.xml");
        EditConfigRequest editConfigReq = DocumentToPojoTransformer.getEditConfig(request);

        assertEquals("101", editConfigReq.getMessageId());
        assertEquals(EditConfigDefaultOperations.MERGE, editConfigReq.getDefaultOperation());
        assertEquals(EditConfigErrorOptions.STOP_ON_ERROR, editConfigReq.getErrorOption());
        assertEquals(EditConfigTestOptions.SET, editConfigReq.getTestOption());

        assertEquals(StandardDataStores.RUNNING, editConfigReq.getTarget());

        assertNotNull(editConfigReq.getConfigElement());
        assertEquals("configuration", editConfigReq.getConfigElement().getConfigElementContents().get(0).getLocalName());
        assertTrue(editConfigReq.getConfigElement().getConfigElementContents().get(0).getChildNodes().getLength() > 0);

        LOGGER.debug(DocumentUtils.documentToString(editConfigReq.getRequestDocument()));
    }

    @Test
    public void testGetEditConfigThrowsException() throws Exception {
        Document request = getDocumentFile("editConfigInvalid.xml");
        try {
            DocumentToPojoTransformer.getEditConfig(request);
            fail("Expected exception did not occur");
        } catch (Exception e) {
            // we are fine if this happens
        }
    }

    @Test
    public void testGetLockRequest() throws NetconfMessageBuilderException {
        Document request = getDocumentFile("lock.xml");
        LockRequest lockReq = DocumentToPojoTransformer.getLockRequest(request);

        assertEquals("101", lockReq.getMessageId());
        assertEquals(StandardDataStores.CANDIDATE, lockReq.getTarget());

        // one more test
        lockReq.setTargetRunning();
        assertEquals(StandardDataStores.RUNNING, lockReq.getTarget());
    }

    @Test
    public void testGetUnLockRequest() throws NetconfMessageBuilderException {
        Document request = getDocumentFile("unLock.xml");
        UnLockRequest unlockReq = DocumentToPojoTransformer.getUnLockRequest(request);

        assertEquals("101", unlockReq.getMessageId());
        assertEquals(StandardDataStores.CANDIDATE, unlockReq.getTarget());

        unlockReq.toString();
        unlockReq.setTargetRunning();
        assertEquals(StandardDataStores.RUNNING, unlockReq.getTarget());
    }
}