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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.Node;

import org.apache.log4j.Logger;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

public class PojoToDocumentTransformerTest {

    private AbstractNetconfGetRequest m_abstractNetconfGetRequest = mock(AbstractNetconfGetRequest.class);
    private static final Logger LOGGER = Logger.getLogger(PojoToDocumentTransformerTest.class);
    private String m_source = "running";
    private NetconfFilter m_axsFilter = new NetconfFilter();
    private WithDefaults m_withDefaults = WithDefaults.REPORT_ALL;
    private int m_withDelay = m_abstractNetconfGetRequest.m_withDelay;
    private int m_depth = 1;
    private String m_stream = NetconfResources.NETCONF;
    private DateTime m_startTime = new DateTime("2012-08-16T07:22:05Z");
    private DateTime m_stopTime = new DateTime("2016-09-26T09:20:10Z");
    private String string = "\n";
    private String emptyString = "";
    private String string_line = "\n\n";
    private String RUNNING = "running";
    private Node node = mock(Node.class);
    protected Document m_doc;
    private Element m_element = mock(Element.class);
    private Document m_document = mock(Document.class);
    protected static final String ERROR_WHILE_BUILDING_DOCUMENT = "Error while building document ";
    private static final String EXPECTED_CLIENT_HELLO_MESSAGE = "<hello " +
            "xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
            + "<capabilities><capability>urn:ietf:params:netconf:base:1.0</capability>"
            + "<capability>urn:ietf:params:netconf:base:1.1</capability></capabilities></hello>";
    private static final String EXPECTED_SERVER_HELLO_MESSAGE = "<hello " +
            "xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
            + "<capabilities><capability>urn:ietf:params:netconf:base:1.0</capability>"
            + "<capability>urn:ietf:params:netconf:base:1.1</capability>"
            + "<capability>urn:ietf:params:netconf:capability:writable-running:1.0</capability>"
            + "</capabilities><session-id>1</session-id></hello>";

    private PojoToDocumentTransformer m_transformer = new PojoToDocumentTransformer();

    private void initializeExecute() {

        m_transformer.m_doc = m_document;
    }

    @Test
    public void testNewConfigChangeNotificationElement() throws NetconfMessageBuilderException {
        String dataStore = "running";
        List<EditInfo> editList = new ArrayList<>();
        EditInfo editInfo = new EditInfo();
        Map<String, String> namespaceDeclareMap = new HashMap<>();
        namespaceDeclareMap.put("prefix1", "http://www.test-company.com/solutions/namespace1");
        namespaceDeclareMap.put("prefix2", "http://www.test-company.com/solutions/namespace2");
        String target = "/prefix1:container1/prefix2:container2[prefix2:name=OLT-1]";
        String operation = "merge";
        editInfo.setNamespaceDeclareMap(namespaceDeclareMap);
        editInfo.setOperation(operation);
        editInfo.setTarget(target);
        ChangedLeafInfo changedLeafInfo = new ChangedLeafInfo("leaf1", "leaf1Value", "http://www.test-company.com/solutions/namespace3",
                "prefix3");
        editInfo.setChangedLeafInfos(changedLeafInfo);
        editInfo.setImplied(true);
        editList.add(editInfo);

        SessionInfo sessionInfo = new SessionInfo();
        sessionInfo.setSessionId(1);
        sessionInfo.setSourceHostIpAddress("192.168.92.62");
        sessionInfo.setUserName("admin");
        ChangedByParams changedByParams = new ChangedByParams(sessionInfo);

        Element configChangeNotificationElement = m_transformer.getConfigChangeNotificationElement(dataStore,
                editList, changedByParams);
        Element datastoreElement = DocumentUtils.getElement(configChangeNotificationElement, NetconfResources
                .DATA_STORE);
        assertEquals("running", datastoreElement.getTextContent());
        Element changeByElement = DocumentUtils.getElement(configChangeNotificationElement, NetconfResources
                .CHANGED_BY);
        assertNotNull(changeByElement);

        Element userNameElement = DocumentUtils.getElement(configChangeNotificationElement, NetconfResources.USER_NAME);
        assertEquals("admin", userNameElement.getTextContent());

        Element editElement = DocumentUtils.getElement(configChangeNotificationElement, NetconfResources.EDIT);
        assertNotNull(editElement);
        Element impliedElement = DocumentUtils.getElement(configChangeNotificationElement, NetconfResources.IMPLIED);
        assertNotNull(impliedElement);
        assertEquals("urn:ietf:params:xml:ns:yang:ietf-netconf-notifications", impliedElement.getNamespaceURI());
        Element targetElement = DocumentUtils.getElement(configChangeNotificationElement, NetconfResources.TARGET);
        NamedNodeMap attributes = targetElement.getAttributes();
        assertEquals("http://www.test-company.com/solutions/namespace1", attributes.getNamedItem("xmlns:prefix1")
                .getNodeValue());
        assertEquals("http://www.test-company.com/solutions/namespace2", attributes.getNamedItem("xmlns:prefix2")
                .getNodeValue());
        assertEquals(2, attributes.getLength());
        assertEquals("/prefix1:container1/prefix2:container2[prefix2:name=OLT-1]", targetElement.getTextContent());

        Element operationElement = DocumentUtils.getElement(configChangeNotificationElement, NetconfResources
                .OPERATION);
        assertEquals("merge", operationElement.getTextContent());

        Element changedLeafElement = DocumentUtils.getElement(configChangeNotificationElement, NetconfResources
                .CHANGED_LEAF);
        assertNotNull(changedLeafElement);

        Element leaf1Element = DocumentUtils.getChildElement(changedLeafElement, "prefix3:leaf1");
        assertNotNull(leaf1Element);
        assertEquals("leaf1Value", leaf1Element.getTextContent());
        assertEquals("http://www.test-company.com/solutions/namespace3", leaf1Element.getNamespaceURI());
        assertEquals("prefix3", leaf1Element.getPrefix());

    }

    @Test
    public void testNewStateChangeNotificationElement() throws NetconfMessageBuilderException {
        List<StateChangeInfo> changesList = new ArrayList<>();
        StateChangeInfo stateChangeInfo = new StateChangeInfo();
        Map<String, String> namespaceDeclareMap = new HashMap<>();
        namespaceDeclareMap.put("prefix1", "http://www.test-company.com/solutions/namespace1");
        namespaceDeclareMap.put("prefix2", "http://www.test-company.com/solutions/namespace2");
        String target = "/prefix1:container1/prefix2:container2[prefix2:name=name1]";
        stateChangeInfo.setNamespaceDeclareMap(namespaceDeclareMap);
        stateChangeInfo.setTarget(target);
        ChangedLeafInfo changedLeafInfo1 = new ChangedLeafInfo("leaf1", "newValue1",
                "http://www.test-company.com/solutions/namespace3",
                "prefix3");
        ChangedLeafInfo changedLeafInfo2 = new ChangedLeafInfo("leaf2", "newValue2",
                "http://www.test-company.com/solutions/namespace3",
                "prefix3");
        stateChangeInfo.setChangedLeafInfos(changedLeafInfo1);
        stateChangeInfo.setChangedLeafInfos(changedLeafInfo2);

        changesList.add(stateChangeInfo);

        Element stateChangeNotificationElement = m_transformer.getStateChangeNotificationElement(changesList);

        Element changesElement = DocumentUtils.getElement(stateChangeNotificationElement, NetconfResources.CHANGES);
        assertNotNull(changesElement);

        Element targetElement = DocumentUtils.getElement(stateChangeNotificationElement, NetconfResources.TARGET);
        NamedNodeMap attributes = targetElement.getAttributes();
        assertEquals("http://www.test-company.com/solutions/namespace1", attributes.getNamedItem("xmlns:prefix1")
                .getNodeValue());
        assertEquals("http://www.test-company.com/solutions/namespace2", attributes.getNamedItem("xmlns:prefix2")
                .getNodeValue());
        assertEquals(2, attributes.getLength());
        assertEquals("/prefix1:container1/prefix2:container2[prefix2:name=name1]", targetElement.getTextContent());

        Element changedLeafElement = DocumentUtils.getElement(stateChangeNotificationElement, NetconfResources
                .CHANGED_LEAF);
        assertNotNull(changedLeafElement);


        Element leaf1Element1 = DocumentUtils.getChildElement(changesElement, "prefix3:leaf1");
        assertNotNull(leaf1Element1);
        assertEquals("newValue1", leaf1Element1.getTextContent());
        assertEquals("http://www.test-company.com/solutions/namespace3", leaf1Element1.getNamespaceURI());
        assertEquals("prefix3", leaf1Element1.getPrefix());

        Element leaf1Element2 = DocumentUtils.getChildElement(changesElement, "prefix3:leaf2");
        assertNotNull(leaf1Element2);
        assertEquals("newValue2", leaf1Element2.getTextContent());
        assertEquals("http://www.test-company.com/solutions/namespace3", leaf1Element1.getNamespaceURI());
        assertEquals("prefix3", leaf1Element2.getPrefix());

        String changesElementStringExpected = "<changes xmlns=\"http://bbf.org/netconf-stack\"><target xmlns:prefix1=\"http://www.test-company.com/solutions/namespace1\" xmlns:prefix2=\"http://www.test-company.com/solutions/namespace2\">/prefix1:container1/prefix2:container2[prefix2:name=name1" +
                "]</target><changed-leaf><key>1</key><prefix3:leaf1 xmlns:prefix3=\"http://www.test-company.com/solutions/namespace3\">newValue1</prefix3:leaf1></changed-leaf><changed-leaf><key>2" +
                "</key><prefix3:leaf2 xmlns:prefix3=\"http://www.test-company.com/solutions/namespace3\">newValue2</prefix3:leaf2></changed-leaf></changes>";
        assertEquals(changesElementStringExpected, DocumentUtils.documentToString(changesElement));

    }

    @Test
    public void testCreateSubscription() throws NetconfMessageBuilderException {
        DateTime startTime = new DateTime();
        DateTime stopTime = new DateTime();
        PojoToDocumentTransformer subscription = new PojoToDocumentTransformer().newNetconfRpcDocument("1")
                .addCreateSubscriptionElement(NetconfResources.NETCONF, null, startTime, stopTime);
        Document subscriptionDoc = subscription.build();
        String startTimeAsString = DocumentUtils.getInstance().getElementByName(subscriptionDoc, "startTime")
                .getTextContent();
        String stopTimeAsString = DocumentUtils.getInstance().getElementByName(subscriptionDoc, "stopTime")
                .getTextContent();

        ISODateTimeFormat.dateTimeNoMillis().parseDateTime(startTimeAsString);
        ISODateTimeFormat.dateTimeNoMillis().parseDateTime(stopTimeAsString);
        try {
            ISODateTimeFormat.dateTimeNoMillis().parseDateTime("2016-07-11T23:05:39.00-05:00");
            fail("Expected the timestamp with milliseconds doesn't allow");
        } catch (IllegalArgumentException e) {
            LOGGER.info("Expected exception here. " + e.getMessage());
        }
    }

    @Test
    public void testNewClientHelloMessage() throws NetconfMessageBuilderException {
        Set<String> clientCapability = new LinkedHashSet<>();
        clientCapability.add(NetconfResources.NETCONF_BASE_CAP_1_0);
        clientCapability.add(NetconfResources.NETCONF_BASE_CAP_1_1);
        m_transformer.newClientHelloMessage(clientCapability);
        String clientHelloMessage = DocumentUtils.documentToString(m_transformer.build());
        assertEquals(EXPECTED_CLIENT_HELLO_MESSAGE, clientHelloMessage);
    }

    @Test
    public void testNewServerHelloMessage() throws NetconfMessageBuilderException {
        Set<String> serverCapability = new LinkedHashSet<>();
        serverCapability.add(NetconfResources.NETCONF_BASE_CAP_1_0);
        serverCapability.add(NetconfResources.NETCONF_BASE_CAP_1_1);
        serverCapability.add(NetconfResources.NETCONF_WRITABLE_RUNNNG);
        int sessionId = 1;
        m_transformer.newServerHelloMessage(serverCapability, sessionId);
        String serverHelloMessage = DocumentUtils.documentToString(m_transformer.build());
        assertEquals(EXPECTED_SERVER_HELLO_MESSAGE, serverHelloMessage);

    }

    @Test
    public void testAddGetConfigElementWhenRpcIsNull() throws NetconfMessageBuilderException,
            ParserConfigurationException {

        initializeExecute();
        try {
            m_transformer.addGetConfigElement(m_source, m_axsFilter, m_withDefaults, m_withDelay, m_depth, null);
            fail("Should throw NetconfMessageBuilderException");
        } catch (Exception e) {
            assertEquals("<rpc> Element is null, create the rpc element first", e.getMessage());
        }
    }

    @Test
    public void testAddCreateSubscriptionElementWhenRpcIsNull() throws ParserConfigurationException {

        initializeExecute();
        try {
            m_transformer.addCreateSubscriptionElement(m_stream, m_axsFilter, m_startTime, m_stopTime);
            fail("Should throw NetconfMessageBuilderException");
        } catch (Exception e) {
            assertEquals("<rpc> Element is null, create the rpc element first", e.getMessage());
        }
    }

    @Test
    public void testAddGetElementWhenRpcIsNull() throws ParserConfigurationException {

        initializeExecute();
        try {
            m_transformer.addGetElement(m_axsFilter, m_withDefaults, m_withDelay, m_depth, null);
            fail("Should throw NetconfMessageBuilderException");
        } catch (Exception e) {
            assertEquals("<rpc> Element is null, create the rpc element first", e.getMessage());
        }
    }

    @Test
    public void testAddCloseSessionElementWhenRpcIsNull() throws ParserConfigurationException {

        initializeExecute();
        try {
            m_transformer.addCloseSessionElement();
            fail("Should throw NetconfMessageBuilderException");
        } catch (Exception e) {
            assertEquals("<rpc> Element is null, create the rpc element first", e.getMessage());
        }
    }

    @Test
    public void testAddKillSessionElementWhenRpcIsNull() throws ParserConfigurationException {

        initializeExecute();
        try {
            m_transformer.addKillSessionElement(null);
            fail("Should throw NetconfMessageBuilderException");
        } catch (Exception e) {
            assertEquals("<rpc> Element is null, create the rpc element first", e.getMessage());
        }
    }

    @Test
    public void testAddOkWhenRpcIsNull() throws ParserConfigurationException {

        initializeExecute();
        try {
            m_transformer.addOk();
            fail("Should throw NetconfMessageBuilderException");
        } catch (Exception e) {
            assertEquals("<rpc-reply> Element is null, create the rpc element first", e.getMessage());
        }
    }

    @Test
    public void testAddDataWhenRpcIsNull() throws ParserConfigurationException {

        initializeExecute();
        try {
            m_transformer.addData(null);
            fail("Should throw NetconfMessageBuilderException");
        } catch (Exception e) {
            assertEquals("<rpc-reply> Element is null, create the rpc element first", e.getMessage());
        }
    }

    @Test
    public void testRequestToString() {
        initializeExecute();
        assertEquals(string, PojoToDocumentTransformer.requestToString(m_document));
    }

    @Test
    public void testPrettyPrint() {
        Element element1 = mock(Element.class);
        Collection<Element> elements = new ArrayList<>();
        elements.add(element1);
        assertEquals(string, PojoToDocumentTransformer.prettyPrint(m_element));
        assertEquals(string_line, PojoToDocumentTransformer.prettyPrint(elements));

    }

    @Test
    public void testNotificationToStringAndPrettyString() {
        Notification notification = mock(Notification.class);
        assertEquals(string, PojoToDocumentTransformer.notificationToPrettyString(notification));
        assertEquals(emptyString, PojoToDocumentTransformer.notificationToString(notification));
    }

    @Test
    public void testAddRpcErrorWhenRpcIsNull() throws ParserConfigurationException {
        initializeExecute();
        try {
            m_transformer.addRpcError(null);
            fail("Should throw NetconfMessageBuilderException");
        } catch (Exception e) {
            assertEquals("<rpc-reply> Element is null, create the rpc element first", e.getMessage());
        }
    }

    @Test
    public void testAddCopyConfigElementWithEmptySource() throws NetconfMessageBuilderException,
            ParserConfigurationException {

        String source = "";
        String target = RUNNING;
        boolean targetIsUrl = true;
        boolean srcIsUrl = false;
        Element config = null;
        try {
            m_transformer.addCopyConfigElement(source, srcIsUrl, target, targetIsUrl, config);
            fail("Should throw NetconfMessageBuilderException");
        } catch (Exception e) {
            assertEquals("<source> element not set for <copy-config>", e.getMessage());
        }

    }

    @Test
    public void testAddCopyConfigElementWithNullSource() throws NetconfMessageBuilderException,
            ParserConfigurationException {

        String source = null;
        String target = RUNNING;
        boolean targetIsUrl = true;
        boolean srcIsUrl = false;
        Element config = null;
        try {
            m_transformer.addCopyConfigElement(source, srcIsUrl, target, targetIsUrl, config);
            fail("Should throw NetconfMessageBuilderException");
        } catch (Exception e) {
            assertEquals("<source> element not set for <copy-config>", e.getMessage());
        }

    }

    @Test
    public void testAddCopyConfigElementWithEmptyTarget() throws NetconfMessageBuilderException,
            ParserConfigurationException {

        String source = RUNNING;
        Document doc = DocumentUtils.getNewDocument();
        Element config = doc.createElementNS("http://www.test-company.com/ns/tester", "name");
        String target = "";
        boolean targetIsUrl = true;
        boolean srcIsUrl = false;
        try {
            m_transformer.addCopyConfigElement(source, srcIsUrl, target, targetIsUrl, config);
            fail("Should throw NetconfMessageBuilderException");
        } catch (Exception e) {
            assertEquals("<target> element not set for <copy-config>", e.getMessage());
        }

    }

    @Test
    public void testAddCopyConfigElementWithNullTarget() throws NetconfMessageBuilderException,
            ParserConfigurationException {

        String source = RUNNING;
        Document doc = DocumentUtils.getNewDocument();
        Element config = doc.createElementNS("http://www.test-company.com/ns/tester", "name");
        String target = null;
        boolean targetIsUrl = true;
        boolean srcIsUrl = false;
        try {
            m_transformer.addCopyConfigElement(source, srcIsUrl, target, targetIsUrl, config);
            fail("Should throw NetconfMessageBuilderException");

        } catch (Exception e) {
            assertEquals("<target> element not set for <copy-config>", e.getMessage());
        }

    }

    @Test
    public void testAddDeleteConfigElementWithEmptyTarget() throws NetconfMessageBuilderException,
            ParserConfigurationException {
        String target = "";
        try {
            m_transformer.addDeleteConfigElement(target);
            fail("Should throw NetconfMessageBuilderException");

        } catch (Exception e) {
            assertEquals("<target> element not set for <delete-config>", e.getMessage());
        }

    }

    @Test
    public void testAddDeleteConfigElementWithNullTarget() throws NetconfMessageBuilderException,
            ParserConfigurationException {
        String target = null;
        try {
            m_transformer.addDeleteConfigElement(target);
            fail("Should throw NetconfMessageBuilderException");

        } catch (Exception e) {
            assertEquals("<target> element not set for <delete-config>", e.getMessage());
        }

    }

    @Test
    public void testAddLockElementWithEmptyTarget() throws NetconfMessageBuilderException,
            ParserConfigurationException {
        String target = "";
        try {
            m_transformer.addLockElement(target);
            fail("Should throw NetconfMessageBuilderException");

        } catch (Exception e) {
            assertEquals("<target> element not set for <lock>", e.getMessage());
        }

    }

    @Test
    public void testAddLockElementWithNullTarget() throws NetconfMessageBuilderException, ParserConfigurationException {
        String target = null;
        try {
            m_transformer.addLockElement(target);
            fail("Should throw NetconfMessageBuilderException");

        } catch (Exception e) {
            assertEquals("<target> element not set for <lock>", e.getMessage());
        }

    }

    @Test
    public void testAddUnLockElementWithEmptyTarget() throws NetconfMessageBuilderException,
            ParserConfigurationException {
        String target = "";
        try {
            m_transformer.addUnLockElement(target);
            fail("Should throw NetconfMessageBuilderException");

        } catch (Exception e) {
            assertEquals("<target> element not set for <unlock>", e.getMessage());
        }

    }

    @Test
    public void testAddUnLockElementWithNullTarget() throws NetconfMessageBuilderException,
            ParserConfigurationException {
        String target = null;
        try {
            m_transformer.addUnLockElement(target);
            fail("Should throw NetconfMessageBuilderException");
        } catch (Exception e) {
            assertEquals("<target> element not set for <unlock>", e.getMessage());
        }

    }

    @Test
    public void testAddEditConfigElementWithNull() throws NetconfMessageBuilderException, ParserConfigurationException {
        initializeExecute();
        EditConfigElement configElement = null;
        Element element2 = mock(Element.class);
        when(m_document.createElementNS(NetconfResources.NETCONF_RPC_NS_1_0, NetconfResources.EDIT_CONFIG))
                .thenReturn(m_element);
        when(m_document.createElementNS(NetconfResources.WITH_DELAY_NS, NetconfResources.WITH_DELAY)).thenReturn
                (element2);
        when(m_document.getFirstChild()).thenReturn(node);

        try {
            m_transformer.addEditConfigElement(null, null, null, null, m_withDelay, configElement);
            fail("Should throw NetconfMessageBuilderException");

        } catch (Exception e) {
            assertEquals("empty/null <config> for <edit-config> :null", e.getMessage());
        }

    }

    @Test
    public void testAddRpcElementWhenRpcIsNull() throws ParserConfigurationException {
        initializeExecute();
        try {
            m_transformer.addRpcElement(m_element);
            fail("Should throw NetconfMessageBuilderException");
        } catch (Exception e) {
            assertEquals("<rpc> Element is null, create the rpc element first", e.getMessage());
        }
    }

    @Test
    public void testAddRpcElementNotNull() throws ParserConfigurationException {
        initializeExecute();
        when(m_document.getFirstChild()).thenReturn(node);
        try {
            m_transformer.addRpcElement(m_element);
        } catch (Exception e) {
            assertEquals("<rpc> Element is null, create the rpc element first", e.getMessage());
        }
    }

    @Test
    public void testAddEditConfigElementWithValidateConfigElementNotNull()
            throws NetconfMessageBuilderException, ParserConfigurationException {

        EditConfigElement configElement = mock(EditConfigElement.class);
        initializeExecute();
        Element element2 = mock(Element.class);
        when(m_document.createElementNS(NetconfResources.NETCONF_RPC_NS_1_0, NetconfResources.EDIT_CONFIG))
                .thenReturn(m_element);
        when(m_document.createElementNS(NetconfResources.WITH_DELAY_NS, NetconfResources.WITH_DELAY)).thenReturn
                (element2);
        when(m_document.getFirstChild()).thenReturn(node);
        when(configElement.getConfigElementContents()).thenReturn(null);

        try {
            m_transformer.addEditConfigElement(null, null, null, null, m_withDelay, configElement);
            fail("Should throw NetconfMessageBuilderException");

        } catch (Exception e) {
            assertEquals("One or more <config> elements in <edit-config> request are null ", e.getMessage());
        }

    }

    @Test
    public void testValidateErrorOption() {
        assertFalse(PojoToDocumentTransformer.validateErrorOption("1"));
        assertTrue(PojoToDocumentTransformer.validateErrorOption(EditConfigErrorOptions.STOP_ON_ERROR));
        assertTrue(PojoToDocumentTransformer.validateErrorOption(EditConfigErrorOptions.CONTINUE_ON_ERROR));
        assertTrue(PojoToDocumentTransformer.validateErrorOption(EditConfigErrorOptions.ROLLBACK_ON_ERROR));
    }

    @Test
    public void testValidateTestOption() {
        assertFalse(PojoToDocumentTransformer.validateTestOption("1"));
        assertTrue(PojoToDocumentTransformer.validateTestOption(EditConfigTestOptions.SET));
        assertTrue(PojoToDocumentTransformer.validateTestOption(EditConfigTestOptions.TEST_ONLY));
        assertTrue(PojoToDocumentTransformer.validateTestOption(EditConfigTestOptions.TEST_THEN_SET));
    }

    @Test
    public void testValidateDefaultEditOpertation() {
        assertFalse(PojoToDocumentTransformer.validateDefaultEditOpertation("1"));
        assertTrue(PojoToDocumentTransformer.validateDefaultEditOpertation(EditConfigDefaultOperations.MERGE));
        assertTrue(PojoToDocumentTransformer.validateDefaultEditOpertation(EditConfigDefaultOperations.NONE));
        assertTrue(PojoToDocumentTransformer.validateDefaultEditOpertation(EditConfigDefaultOperations.REPLACE));
    }

}
