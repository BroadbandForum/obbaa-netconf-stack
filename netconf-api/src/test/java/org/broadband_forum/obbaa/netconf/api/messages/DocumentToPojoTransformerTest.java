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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.joda.time.DateTime;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class DocumentToPojoTransformerTest {

    public static final String RUNNING = "running";
    public static final String MERGE = "merge";
    public static final String SET = "set";
    public static final String STARTUP = "startup";
    public static final String SUBTREE = "subtree";
    public static final String TOP = "top";
    public static final String TARGET_CANNOT_BE_NULL_EMPTY = "<target> cannot be null/empty";
    public static final String SOURCE_CANNOT_BE_NULL_EMPTY = "<source> cannot be null/empty";
    private static final String ACTION_CANNOT_BE_NULL_EMPTY = "<action> cannot be null";
    private static final String ACTIONTREE_CANNOT_BE_NULL_EMPTY = "action tree element cannot be null";
    public static final String URL = "http://www.abc.com";

    @Test
    public void testGetChangedByParamsFromNotification() throws NetconfMessageBuilderException {

        URL url = Thread.currentThread().getContextClassLoader().getResource("netconfConfigChangeNotification.xml");
        File file = new File(url.getPath());
        Document notificationDocument = DocumentUtils.getDocFromFile(file);

        ChangedByParams params = DocumentToPojoTransformer.getChangedByParamsFromNotification(notificationDocument);
        assertEquals("admin", params.getCommonSessionParams().getUserName());
        assertEquals(2, params.getCommonSessionParams().getSessionId());
        assertEquals("192.168.92.62", params.getCommonSessionParams().getSourceHostIpAddress());
    }

    @Test
    public void testGetNetconfConfigChangeNotification() throws NetconfMessageBuilderException {
        URL url = Thread.currentThread().getContextClassLoader().getResource("netconfConfigChangeNotification.xml");
        File file = new File(url.getPath());
        Document notificationDocument = DocumentUtils.getDocFromFile(file);
        Notification notification = DocumentToPojoTransformer.getNotification(notificationDocument);
        assertTrue(notification instanceof NetconfConfigChangeNotification);

        List<EditInfo> editList = ((NetconfConfigChangeNotification) notification).getEditList();
        assertEquals(1, editList.size());
        assertEquals("remove", editList.get(0).getOperation());
        assertEquals("/prefix1:pma/prefix2:device-holder[prefix2:name=OLT-1]", editList.get(0).getTarget());
        assertEquals(2, editList.get(0).getNamespaceDeclareMap().size());
        assertTrue(editList.get(0).getNamespaceDeclareMap().containsKey("prefix1"));
        assertTrue(editList.get(0).getNamespaceDeclareMap().containsKey("prefix2"));
        assertTrue(editList.get(0).getNamespaceDeclareMap().containsValue("namespace1"));
        assertTrue(editList.get(0).getNamespaceDeclareMap().containsValue("namespace2"));
    }

    @Test
    public void testGetStateChangeNotification() throws NetconfMessageBuilderException {
        URL url = Thread.currentThread().getContextClassLoader().getResource("stateChangeNotification.xml");
        File file = new File(url.getPath());
        Document notificationDocument = DocumentUtils.getDocFromFile(file);
        Notification notification = DocumentToPojoTransformer.getNotification(notificationDocument);
        assertTrue(notification instanceof StateChangeNotification);

        assertEquals("song-count", ((StateChangeNotification) notification).getTarget());
        assertEquals("8", ((StateChangeNotification) notification).getValue());
    }

    @Test
    public void testGetReplayCompleteNotification() throws NetconfMessageBuilderException {
        URL url = Thread.currentThread().getContextClassLoader().getResource("replayCompleteNotification.xml");
        File file = new File(url.getPath());
        Document notificationDocument = DocumentUtils.getDocFromFile(file);
        Notification notification = DocumentToPojoTransformer.getNotification(notificationDocument);
        assertTrue(notification instanceof ReplayComplete);
        DateTime expectedDateTime = NetconfResources.DATE_TIME_WITH_TZ_WITHOUT_MS.parseDateTime
                ("2016-04-26T16:13:01+07:00");
        DateTime actualDateTime = NetconfResources.DATE_TIME_WITH_TZ_WITHOUT_MS.parseDateTime(notification
                .getEventTime());
        assertEquals(expectedDateTime, actualDateTime);
    }

    @Test
    public void testGetNotificationCompleteNotification() throws NetconfMessageBuilderException {
        URL url = Thread.currentThread().getContextClassLoader().getResource("notificationCompleteNotification.xml");
        File file = new File(url.getPath());
        Document notificationDocument = DocumentUtils.getDocFromFile(file);
        Notification notification = DocumentToPojoTransformer.getNotification(notificationDocument);

        assertTrue(notification instanceof NotificationComplete);
        DateTime expectedDateTime = NetconfResources.DATE_TIME_WITH_TZ_WITHOUT_MS.parseDateTime
                ("2016-04-26T16:12:59+07:00");
        DateTime actualDateTime = NetconfResources.DATE_TIME_WITH_TZ_WITHOUT_MS.parseDateTime(notification
                .getEventTime());
        assertEquals(expectedDateTime, actualDateTime);
    }

    @Test
    public void testGetNullDataFromRpcReply() throws Exception {
        String okResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"10\">\n" +
                "<ok/>"
                + "</rpc-reply>";
        Document responseDoc = DocumentUtils.stringToDocument(okResponse);
        NetConfResponse netconfResponse = DocumentToPojoTransformer.getNetconfResponse(responseDoc);
        assertNull(netconfResponse.getData());
    }

    @Test
    public void testErrorDataFromRpcReply() throws Exception {
        String errorResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"2\">\n"
                + "<rpc-error>\n"
                + "<error-type>application</error-type>\n"
                + "<error-tag>invalid-value</error-tag>\n"
                + "<error-severity>error</error-severity>\n"
                + "<error-path>/retrieve-active-alarms/max-number-of-alarms</error-path>\n"
                + "<error-message>Value \"abcd\" does not meet the range constraints. Expected range of value: " +
                "1..4294967295</error-message>\n"
                + "</rpc-error>\n" + "</rpc-reply>\n";
        Document responseDoc = DocumentUtils.stringToDocument(errorResponse);
        NetConfResponse netconfResponse = DocumentToPojoTransformer.getNetconfResponse(responseDoc);
        assertNull(netconfResponse.getData());
        assertEquals(1, netconfResponse.getErrors().size());
        assertEquals("Value \"abcd\" does not meet the range constraints. Expected range of value: 1..4294967295",
                netconfResponse.getErrors().get(0).getErrorMessage());

    }

    @Test
    public void testMultiElementDataFromRpcReply() throws Exception {
        String someResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"10\">\n"
                + "<a>A</a>"
                + "<b>B</b>"
                + "</rpc-reply>";
        Document responseDoc = DocumentUtils.stringToDocument(someResponse);
        NetConfResponse netconfResponse = DocumentToPojoTransformer.getNetconfResponse(responseDoc);
        assertTrue(netconfResponse.getData() == null);
        assertTrue(netconfResponse instanceof NetconfRpcResponse);
        NetconfRpcResponse rpcResponse = (NetconfRpcResponse) netconfResponse;
        assertEquals(2, rpcResponse.getRpcOutputElements().size());
        assertEquals("a", rpcResponse.getRpcOutputElements().get(0).getLocalName());
        assertEquals("b", rpcResponse.getRpcOutputElements().get(1).getLocalName());
    }

    @Test
    public void testGetDataFromRpcReply() throws NetconfMessageBuilderException {
        {
            String getResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n"
                    + "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" + "<pma:pma " +
                    "xmlns:pma=\"urn:org:bbf:pma\">\n"
                    + "<pma:users>\n" + "<pma:user-count>1</pma:user-count>\n" + "<pma:user>\n" +
                    "<pma:username>UT</pma:username>\n"
                    + "</pma:user>\n" + "</pma:users>\n" + "</pma:pma>\n" + "</data>\n" + "</rpc-reply>";
            Document responseDoc = DocumentUtils.stringToDocument(getResponse);
            NetConfResponse netconfResponse = DocumentToPojoTransformer.getNetconfResponse(responseDoc);
            assertTrue(netconfResponse.getData().getLocalName().equals("data"));
            assertNotNull(netconfResponse.getData());

        }
        {
            String getActiveAlarmsResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" " +
                    "message-id=\"10\">\n"
                    + "<alarms:active-alarms xmlns:alarms=\"http://www.test-company.com/solutions/anv-alarms\">\n"
                    + "<alarms:offset>-1</alarms:offset>\n" + "</alarms:active-alarms>\n" + "</rpc-reply>";
            Document responseDoc = DocumentUtils.stringToDocument(getActiveAlarmsResponse);
            NetConfResponse netconfResponse = DocumentToPojoTransformer.getNetconfResponse(responseDoc);
            assertTrue(netconfResponse.getData().getLocalName().equals("active-alarms"));
            assertNotNull(netconfResponse.getData());

        }
    }

    @Test
    public void testGetNotificationNewNotification() throws NetconfMessageBuilderException {
        URL url = Thread.currentThread().getContextClassLoader().getResource("newNetconfNotification.xml");
        File file = new File(url.getPath());
        Document notificationDocument = DocumentUtils.getDocFromFile(file);
        Notification notification = DocumentToPojoTransformer.getNotification(notificationDocument);

        assertTrue(notification instanceof NetconfNotification);
        DateTime expectedDateTime = NetconfResources.DATE_TIME_WITH_TZ_WITHOUT_MS.parseDateTime
                ("2016-04-26T16:13:01+07:00");
        DateTime actualDateTime = NetconfResources.DATE_TIME_WITH_TZ_WITHOUT_MS.parseDateTime(notification
                .getEventTime());
        assertEquals(expectedDateTime, actualDateTime);
    }

    @Test
    public void testGetNotificationStringXml() throws NetconfMessageBuilderException {

        String xmlContent = "<notification xmlns=\"urn:ietf:params:xml:ns:netconf:notification:1.0\">"
                + "<eventTime>2016-04-26T16:13:01+07:00</eventTime>"
                + "</notification>";
        Notification notification = DocumentToPojoTransformer.getNotification(xmlContent);

        assertTrue(notification instanceof NetconfNotification);
        DateTime expectedDateTime = NetconfResources.DATE_TIME_WITH_TZ_WITHOUT_MS.parseDateTime
                ("2016-04-26T16:13:01+07:00");
        DateTime actualDateTime = NetconfResources.DATE_TIME_WITH_TZ_WITHOUT_MS.parseDateTime(notification
                .getEventTime());
        assertEquals(expectedDateTime, actualDateTime);
    }

    @Test
    public void testGetEditInfoListFromNotification() throws NetconfMessageBuilderException, SAXException, IOException {
        URL url = Thread.currentThread().getContextClassLoader().getResource
                ("netconfConfigChangeNotificationWithChangedLeaf.xml");
        File file = new File(url.getPath());
        Document notificationDocument = DocumentUtils.getDocFromFile(file);
        Notification notification = DocumentToPojoTransformer.getNotification(notificationDocument);

        assertTrue(notification instanceof NetconfConfigChangeNotification);
        List<EditInfo> editList = ((NetconfConfigChangeNotification) notification).getEditList();
        assertEquals(1, editList.size());
        assertEquals("remove", editList.get(0).getOperation());
        assertTrue(editList.get(0).isImplied());
        assertEquals("/prefix1:pma/prefix2:device-holder[prefix2:name=OLT-1]", editList.get(0).getTarget());
        assertEquals("namespace1", editList.get(0).getNamespaceDeclareMap().get("prefix1"));
        assertEquals("namespace2", editList.get(0).getNamespaceDeclareMap().get("prefix2"));
        assertEquals("namespace3", editList.get(0).getChangedLeafInfos().get(0).getNamespace());
    }

    @Test
    public void testGetTypeOfNetconfRequest() throws NetconfMessageBuilderException {
        String getConfigRequest = "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"5\" >\n" +
                "    <nc:get-config xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "        <nc:source>\n" +
                "            <nc:running/>\n" +
                "        </nc:source>\n" +
                "    </nc:get-config>\n" +
                "</rpc>\n";
        Document getConfigDocument = DocumentUtils.stringToDocument(getConfigRequest);
        String requestType = DocumentToPojoTransformer.getTypeOfNetconfRequest(getConfigDocument);
        assertEquals("get-config", requestType);

        getConfigRequest = "<nc:rpc xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"5\">\n" +
                "    <nc:get-config xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "        <nc:source>\n" +
                "            <nc:running/>\n" +
                "        </nc:source>\n" +
                "    </nc:get-config>\n" +
                "</nc:rpc>";
        getConfigDocument = DocumentUtils.stringToDocument(getConfigRequest);
        requestType = DocumentToPojoTransformer.getTypeOfNetconfRequest(getConfigDocument);
        assertEquals("get-config", requestType);

        getConfigRequest = "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"5\">\n" +
                "    <get-config >\n" +
                "        <nc:source xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "            <nc:running/>\n" +
                "        </nc:source>\n" +
                "    </get-config>\n" +
                "</rpc>";
        getConfigDocument = DocumentUtils.stringToDocument(getConfigRequest);
        requestType = DocumentToPojoTransformer.getTypeOfNetconfRequest(getConfigDocument);
        assertEquals("get-config", requestType);

        getConfigRequest = "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"5\">\n" +
                "    <get-config >\n" +
                "        <source>\n" +
                "            <nc:running xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\"/>\n" +
                "        </source>\n" +
                "    </get-config>\n" +
                "</rpc>";
        getConfigDocument = DocumentUtils.stringToDocument(getConfigRequest);
        requestType = DocumentToPojoTransformer.getTypeOfNetconfRequest(getConfigDocument);
        assertEquals("get-config", requestType);
    }

    @Test
    public void testEditConfigRequest() throws NetconfMessageBuilderException {
        String editConfigRequestString = "<nc:rpc xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" " +
                "message-id=\"20\">\n" +
                "<nc:edit-config>\n" +
                "    <nc:target>\n" +
                "        <nc:running />\n" +
                "    </nc:target>\n" +
                "    <nc:test-option>set</nc:test-option>\n" +
                "    <nc:config>\n" +
                "        <anv:device-manager xmlns:anv=\"http://www.test-company.com/solutions/anv\" " +
                "                            xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "            <adh:device-holder xmlns:adh=\"http://www.test-company" +
                ".com/management-solutions/anv-device-holders\" " +
                "                               xc:operation=\"create\">\n" +
                "                <adh:name>OLT1</adh:name>\n" +
                "            </adh:device-holder>\n" +
                "        </anv:device-manager>\n" +
                "    </nc:config>\n" +
                "</nc:edit-config>\n" +
                "</nc:rpc>";
        EditConfigRequest editConfigRequest = DocumentToPojoTransformer.getEditConfig(DocumentUtils.stringToDocument
                (editConfigRequestString));
        assertEquals(RUNNING, editConfigRequest.getTarget());
        assertEquals(MERGE, editConfigRequest.getDefaultOperation());
        assertEquals(SET, editConfigRequest.getTestOption());

        editConfigRequestString = "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"20\">\n" +
                "<nc:edit-config xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "    <nc:target>\n" +
                "        <nc:running />\n" +
                "    </nc:target>\n" +
                "    <nc:test-option>set</nc:test-option>\n" +
                "    <nc:config>\n" +
                "        <anv:device-manager xmlns:anv=\"http://www.test-company.com/solutions/anv\" " +
                "                             xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "            <adh:device-holder xmlns:adh=\"http://www.test-company" +
                ".com/management-solutions/anv-device-holders\" " +
                "                               xc:operation=\"create\">\n" +
                "                <adh:name>OLT1</adh:name>\n" +
                "            </adh:device-holder>\n" +
                "        </anv:device-manager>\n" +
                "    </nc:config>\n" +
                "</nc:edit-config>\n" +
                "</rpc>";
        editConfigRequest = DocumentToPojoTransformer.getEditConfig(DocumentUtils.stringToDocument
                (editConfigRequestString));
        assertEquals(RUNNING, editConfigRequest.getTarget());
        assertEquals(MERGE, editConfigRequest.getDefaultOperation());
        assertEquals(SET, editConfigRequest.getTestOption());

        editConfigRequestString = "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"20\">\n" +
                "<edit-config >\n" +
                "    <target>\n" +
                "        <nc:running xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\"/>\n" +
                "    </target>\n" +
                "    <test-option>set</test-option>\n" +
                "    <config>\n" +
                "        <anv:device-manager xmlns:anv=\"http://www.test-company.com/solutions/anv\" " +
                "                            xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "            <adh:device-holder xmlns:adh=\"http://www.test-company" +
                ".com/management-solutions/anv-device-holders\" " +
                "                               xc:operation=\"create\">\n" +
                "                <adh:name>OLT1</adh:name>\n" +
                "            </adh:device-holder>\n" +
                "        </anv:device-manager>\n" +
                "    </config>\n" +
                "</edit-config>\n" +
                "</rpc>";
        editConfigRequest = DocumentToPojoTransformer.getEditConfig(DocumentUtils.stringToDocument
                (editConfigRequestString));
        assertEquals(RUNNING, editConfigRequest.getTarget());
        assertEquals(MERGE, editConfigRequest.getDefaultOperation());
        assertEquals(SET, editConfigRequest.getTestOption());

        editConfigRequestString = "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"20\">\n" +
                "<edit-config >\n" +
                "    <nc:target xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "        <nc:running />\n" +
                "    </nc:target>\n" +
                "    <test-option>set</test-option>\n" +
                "    <config>\n" +
                "        <anv:device-manager xmlns:anv=\"http://www.test-company.com/solutions/anv\" " +
                "                             xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "            <adh:device-holder xmlns:adh=\"http://www.test-company" +
                ".com/management-solutions/anv-device-holders\" " +
                "                               xc:operation=\"create\">\n" +
                "                <adh:name>OLT1</adh:name>\n" +
                "            </adh:device-holder>\n" +
                "        </anv:device-manager>\n" +
                "    </config>\n" +
                "</edit-config>\n" +
                "</rpc>";
        editConfigRequest = DocumentToPojoTransformer.getEditConfig(DocumentUtils.stringToDocument
                (editConfigRequestString));
        assertEquals(RUNNING, editConfigRequest.getTarget());
        assertEquals(MERGE, editConfigRequest.getDefaultOperation());
        assertEquals(SET, editConfigRequest.getTestOption());

        editConfigRequestString = "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"20\">\n" +
                "<edit-config >\n" +
                "    <nc:target xmlns:nc=\"urn:ietf:params:xml:ns:netconf:rebase:1.9\">\n" +
                "        <nc:running />\n" +
                "    </nc:target>\n" +
                "    <test-option>set</test-option>\n" +
                "    <config>\n" +
                "        <anv:device-manager xmlns:anv=\"http://www.test-company.com/solutions/anv\" " +
                "                            xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "            <adh:device-holder xmlns:adh=\"http://www.test-company" +
                ".com/management-solutions/anv-device-holders\" " +
                "                               xc:operation=\"create\">\n" +
                "                <adh:name>OLT1</adh:name>\n" +
                "            </adh:device-holder>\n" +
                "        </anv:device-manager>\n" +
                "    </config>\n" +
                "</edit-config>\n" +
                "</rpc>";
        try {
            DocumentToPojoTransformer.getEditConfig(DocumentUtils.stringToDocument(editConfigRequestString));
            fail("Excepted an exception here");
        } catch (NetconfMessageBuilderException e) {
            assertEquals(TARGET_CANNOT_BE_NULL_EMPTY, e.getMessage());
        }

        editConfigRequestString = "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"20\">\n" +
                "<edit-config >\n" +
                "    <nc:target xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "        <nc:running />\n" +
                "    </nc:target>\n" +
                "    <test-option>set</test-option>\n" +
                "</edit-config>\n" +
                "</rpc>";
        try {
            DocumentToPojoTransformer.getEditConfig(DocumentUtils.stringToDocument(editConfigRequestString));
            fail("Expected an exception here");
        } catch (NetconfMessageBuilderException e) {
            assertEquals("<config> cannot be null/empty", e.getMessage());
        }
    }

    @Test
    public void testCopyConfigRequest() throws NetconfMessageBuilderException {
        String copyConfigRequestString = "<nc:rpc message-id=\"101\" " +
                "xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "       <nc:copy-config>\n" +
                "         <nc:target>\n" +
                "           <nc:running/>\n" +
                "         </nc:target>\n" +
                "         <nc:source>\n" +
                "           <nc:running/>\n" +
                "         </nc:source>\n" +
                "       </nc:copy-config>\n" +
                "     </nc:rpc>";
        CopyConfigRequest copyConfigRequest = DocumentToPojoTransformer.getCopyConfig(DocumentUtils.stringToDocument
                (copyConfigRequestString));
        assertEquals(RUNNING, copyConfigRequest.getTarget());
        assertEquals(RUNNING, copyConfigRequest.getSource());

        copyConfigRequestString = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "       <nc:copy-config xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "         <nc:target>\n" +
                "           <nc:running/>\n" +
                "         </nc:target>\n" +
                "         <nc:source>\n" +
                "           <nc:running/>\n" +
                "         </nc:source>\n" +
                "       </nc:copy-config>\n" +
                "     </rpc>";
        copyConfigRequest = DocumentToPojoTransformer.getCopyConfig(DocumentUtils.stringToDocument
                (copyConfigRequestString));
        assertEquals(RUNNING, copyConfigRequest.getTarget());
        assertEquals(RUNNING, copyConfigRequest.getSource());

        copyConfigRequestString = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "       <copy-config>\n" +
                "         <target>\n" +
                "           <running/>\n" +
                "         </target>\n" +
                "         <nc:source xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "           <nc:running/>\n" +
                "         </nc:source>\n" +
                "       </copy-config>\n" +
                "     </rpc>";
        copyConfigRequest = DocumentToPojoTransformer.getCopyConfig(DocumentUtils.stringToDocument
                (copyConfigRequestString));
        assertEquals(RUNNING, copyConfigRequest.getTarget());
        assertEquals(RUNNING, copyConfigRequest.getSource());

        copyConfigRequestString = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "       <copy-config>\n" +
                "         <nc:target xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "           <nc:running/>\n" +
                "         </nc:target>\n" +
                "         <source>\n" +
                "           <running/>\n" +
                "         </source>\n" +
                "       </copy-config>\n" +
                "     </rpc>";
        copyConfigRequest = DocumentToPojoTransformer.getCopyConfig(DocumentUtils.stringToDocument
                (copyConfigRequestString));
        assertEquals(RUNNING, copyConfigRequest.getTarget());
        assertEquals(RUNNING, copyConfigRequest.getSource());

        copyConfigRequestString = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "       <copy-config >\n" +
                "         <nc:target xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.9\">\n" +
                "           <nc:running/>\n" +
                "         </nc:target>\n" +
                "         <source>\n" +
                "           <running/>\n" +
                "         </source>\n" +
                "       </copy-config>\n" +
                "     </rpc>";
        try {
            DocumentToPojoTransformer.getCopyConfig(DocumentUtils.stringToDocument(copyConfigRequestString));
            fail("Expected an exception here");
        } catch (NetconfMessageBuilderException e) {
            assertEquals(TARGET_CANNOT_BE_NULL_EMPTY, e.getMessage());
        }

        copyConfigRequestString = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "       <copy-config >\n" +
                "         <target>\n" +
                "           <running/>\n" +
                "         </target>\n" +
                "         <nc:source xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.9\">\n" +
                "           <nc:running/>\n" +
                "         </nc:source>\n" +
                "       </copy-config>\n" +
                "     </rpc>";
        try {
            DocumentToPojoTransformer.getCopyConfig(DocumentUtils.stringToDocument(copyConfigRequestString));
            fail("Expected an exception here");
        } catch (NetconfMessageBuilderException e) {
            assertEquals(SOURCE_CANNOT_BE_NULL_EMPTY, e.getMessage());
        }

        copyConfigRequestString = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "       <nc:copy-config xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "         <nc:target>\n" +
                "           <nc:running/>\n" +
                "         </nc:target>\n" +
                "         <nc:source>\n" +
                "           <nc:url>http://www.abc.com</nc:url>\n" +
                "         </nc:source>\n" +
                "       </nc:copy-config>\n" +
                "     </rpc>";
        copyConfigRequest = DocumentToPojoTransformer.getCopyConfig(DocumentUtils.stringToDocument
                (copyConfigRequestString));
        assertEquals(RUNNING, copyConfigRequest.getTarget());
        assertEquals(URL, copyConfigRequest.getSource());

        copyConfigRequestString = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "       <nc:copy-config xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "         <nc:target>\n" +
                "           <nc:url>http://www.abc.com</nc:url>\n" +
                "         </nc:target>\n" +
                "         <nc:source>\n" +
                "           <nc:running />\n" +
                "         </nc:source>\n" +
                "       </nc:copy-config>\n" +
                "     </rpc>";
        copyConfigRequest = DocumentToPojoTransformer.getCopyConfig(DocumentUtils.stringToDocument
                (copyConfigRequestString));
        assertEquals(URL, copyConfigRequest.getTarget());
        assertEquals(RUNNING, copyConfigRequest.getSource());

        copyConfigRequestString = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "       <nc:copy-config xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "         <nc:target>\n" +
                "           <nc:url></nc:url>\n" +
                "         </nc:target>\n" +
                "         <nc:source>\n" +
                "           <nc:running />\n" +
                "         </nc:source>\n" +
                "       </nc:copy-config>\n" +
                "     </rpc>";
        try {
            DocumentToPojoTransformer.getCopyConfig(DocumentUtils.stringToDocument(copyConfigRequestString));
            fail("Expected an exception here");
        } catch (NetconfMessageBuilderException e) {
            assertEquals("target <url> cannot be null/empty", e.getMessage());
        }

        copyConfigRequestString = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "       <nc:copy-config xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "         <nc:target>\n" +
                "           <nc:startup/>\n" +
                "         </nc:target>\n" +
                "         <nc:source>\n" +
                "           <nc:url></nc:url>\n" +
                "         </nc:source>\n" +
                "       </nc:copy-config>\n" +
                "     </rpc>";
        try {
            DocumentToPojoTransformer.getCopyConfig(DocumentUtils.stringToDocument(copyConfigRequestString));
            fail("Expected an exception here");
        } catch (NetconfMessageBuilderException e) {
            assertEquals("source <url> cannot be null/empty", e.getMessage());
        }
    }

    @Test
    public void testGetRequest() throws NetconfMessageBuilderException {
        String getRequestWithoutFilterString = "<nc:rpc message-id=\"100\" " +
                "xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "    <nc:get>\n" +
                "    </nc:get>\n" +
                "</nc:rpc>";
        GetRequest getRequest = DocumentToPojoTransformer.getGet(DocumentUtils.stringToDocument
                (getRequestWithoutFilterString));
        assertEquals(null, getRequest.getFilter());

        String getRequestWithFilterString = "<nc:rpc xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" " +
                "message-id=\"100\">\n" +
                "<nc:get>\n" +
                "  <nc:filter>\n" +
                "     <nc:top>" +
                "        <nc:users/> " +
                "    </nc:top>\n" +
                "  </nc:filter>\n" +
                "</nc:get>\n" +
                "</nc:rpc>";
        getRequest = DocumentToPojoTransformer.getGet(DocumentUtils.stringToDocument(getRequestWithFilterString));
        assertEquals(null, getRequest.getFilter().getType());
        Document document = DocumentUtils.stringToDocument(getRequestWithFilterString);
        Node filterChildElement = (DocumentUtils.getChildNodeByName(document, NetconfResources.FILTER,
                NetconfResources.NETCONF_RPC_NS_1_0));
        Node childElement = DocumentUtils.getChildNodeByName(filterChildElement, TOP, NetconfResources
                .NETCONF_RPC_NS_1_0);
        assertEquals(DocumentUtils.documentToPrettyString(childElement),
                DocumentUtils.documentToPrettyString(getRequest.getFilter().getXmlFilterElements().get(0)));

        getRequestWithFilterString = "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" " +
                "message-id=\"100\">\n" +
                "<nc:get xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "  <nc:filter>\n" +
                "     <nc:top>" +
                "        <nc:users/> " +
                "    </nc:top>\n" +
                "  </nc:filter>\n" +
                "</nc:get>\n" +
                "</rpc>";
        getRequest = DocumentToPojoTransformer.getGet(DocumentUtils.stringToDocument(getRequestWithFilterString));
        assertEquals(null, getRequest.getFilter().getType());
        document = DocumentUtils.stringToDocument(getRequestWithFilterString);
        filterChildElement = (DocumentUtils.getChildNodeByName(document, NetconfResources.FILTER,
                NetconfResources.NETCONF_RPC_NS_1_0));
        childElement = DocumentUtils.getChildNodeByName(filterChildElement, TOP, NetconfResources.NETCONF_RPC_NS_1_0);
        assertEquals(DocumentUtils.documentToPrettyString(childElement),
                DocumentUtils.documentToPrettyString(getRequest.getFilter().getXmlFilterElements().get(0)));

        getRequestWithFilterString = "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" " +
                "message-id=\"100\">\n" +
                "<get>\n" +
                "  <nc:filter  xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "     <nc:top>" +
                "        <nc:users/> " +
                "    </nc:top>\n" +
                "  </nc:filter>\n" +
                "</get>\n" +
                "</rpc>";
        getRequest = DocumentToPojoTransformer.getGet(DocumentUtils.stringToDocument(getRequestWithFilterString));
        assertEquals(null, getRequest.getFilter().getType());
        document = DocumentUtils.stringToDocument(getRequestWithFilterString);
        filterChildElement = (DocumentUtils.getChildNodeByName(document, NetconfResources.FILTER,
                NetconfResources.NETCONF_RPC_NS_1_0));
        childElement = DocumentUtils.getChildNodeByName(filterChildElement, TOP, NetconfResources.NETCONF_RPC_NS_1_0);
        assertEquals(DocumentUtils.documentToPrettyString(childElement),
                DocumentUtils.documentToPrettyString(getRequest.getFilter().getXmlFilterElements().get(0)));
    }

    @Test
    public void testDeleteConfigRequest() throws NetconfMessageBuilderException {
        String deleteConfigRequestString = "<nc:rpc message-id=\"101\" " +
                "xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "       <nc:delete-config>\n" +
                "         <nc:target>\n" +
                "           <nc:startup/>\n" +
                "         </nc:target>\n" +
                "       </nc:delete-config>\n" +
                "     </nc:rpc>";
        DeleteConfigRequest deleteConfigRequest = DocumentToPojoTransformer.getDeleteConfig
                (DocumentUtils.stringToDocument(deleteConfigRequestString));
        assertEquals(STARTUP, deleteConfigRequest.getTarget());

        deleteConfigRequestString = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "       <nc:delete-config xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "         <nc:target>\n" +
                "           <nc:startup/>\n" +
                "         </nc:target>\n" +
                "       </nc:delete-config>\n" +
                "     </rpc>";
        deleteConfigRequest = DocumentToPojoTransformer.getDeleteConfig(DocumentUtils.stringToDocument
                (deleteConfigRequestString));
        assertEquals(STARTUP, deleteConfigRequest.getTarget());

        deleteConfigRequestString = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "       <delete-config>\n" +
                "         <nc:target xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "           <nc:startup/>\n" +
                "         </nc:target>\n" +
                "       </delete-config>\n" +
                "     </rpc>";
        deleteConfigRequest = DocumentToPojoTransformer.getDeleteConfig(DocumentUtils.stringToDocument
                (deleteConfigRequestString));
        assertEquals(STARTUP, deleteConfigRequest.getTarget());

        deleteConfigRequestString = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "       <delete-config>\n" +
                "         <target>\n" +
                "           <nc:startup  xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\"/>\n" +
                "         </target>\n" +
                "       </delete-config>\n" +
                "     </rpc>";
        deleteConfigRequest = DocumentToPojoTransformer.getDeleteConfig(DocumentUtils.stringToDocument
                (deleteConfigRequestString));
        assertEquals(STARTUP, deleteConfigRequest.getTarget());

        deleteConfigRequestString = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "       <delete-config>\n" +
                "         <nc:target xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.9\">\n" +
                "           <nc:startup/>\n" +
                "         </nc:target>\n" +
                "       </delete-config>\n" +
                "     </rpc>";
        try {
            DocumentToPojoTransformer.getDeleteConfig(DocumentUtils.stringToDocument(deleteConfigRequestString));
            fail("Expected an exception here");
        } catch (NetconfMessageBuilderException e) {
            assertEquals(TARGET_CANNOT_BE_NULL_EMPTY, e.getMessage());
        }
    }

    @Test
    public void testGetConfigRequest() throws NetconfMessageBuilderException {
        String getConfigRequestWithoutFilter = "<nc:rpc xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" " +
                "message-id=\"5\">\n" +
                "    <nc:get-config xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "        <nc:source>\n" +
                "            <nc:running/>\n" +
                "        </nc:source>\n" +
                "    </nc:get-config>\n" +
                "</nc:rpc>";
        GetConfigRequest getConfigRequest = DocumentToPojoTransformer.getGetConfig(DocumentUtils.stringToDocument
                (getConfigRequestWithoutFilter));
        assertEquals(RUNNING, getConfigRequest.getSource());
        assertEquals(null, getConfigRequest.getFilter());

        getConfigRequestWithoutFilter = "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"5\">\n" +
                "    <get-config >\n" +
                "        <source>\n" +
                "            <nc:running xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.1\"/>\n" +
                "        </source>\n" +
                "    </get-config>\n" +
                "</rpc>";
        getConfigRequest = DocumentToPojoTransformer.getGetConfig(DocumentUtils.stringToDocument
                (getConfigRequestWithoutFilter));
        assertEquals(RUNNING, getConfigRequest.getSource());
        assertEquals(null, getConfigRequest.getFilter());

        getConfigRequestWithoutFilter = "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"5\">\n" +
                "    <get-config >\n" +
                "        <nc:source xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "            <nc:running/>\n" +
                "        </nc:source>\n" +
                "    </get-config>\n" +
                "</rpc>";
        getConfigRequest = DocumentToPojoTransformer.getGetConfig(DocumentUtils.stringToDocument
                (getConfigRequestWithoutFilter));
        assertEquals(RUNNING, getConfigRequest.getSource());
        assertEquals(null, getConfigRequest.getFilter());

        String getConfigRequestWithFilter = "<nc:rpc message-id=\"101\" " +
                "xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "       <nc:get-config>\n" +
                "         <nc:source>\n" +
                "           <nc:running/>\n" +
                "         </nc:source>\n" +
                "         <nc:filter type=\"subtree\">\n" +
                "           <nc:top>\n" +
                "             <nc:users/>\n" +
                "           </nc:top>\n" +
                "         </nc:filter>\n" +
                "       </nc:get-config>\n" +
                "     </nc:rpc>";
        getConfigRequest = DocumentToPojoTransformer.getGetConfig(DocumentUtils.stringToDocument
                (getConfigRequestWithFilter));
        assertEquals(RUNNING, getConfigRequest.getSource());
        assertEquals(SUBTREE, getConfigRequest.getFilter().getType());
        Document document = DocumentUtils.stringToDocument(getConfigRequestWithFilter);
        Node filterChildElement = (DocumentUtils.getChildNodeByName(document, NetconfResources.FILTER,
                NetconfResources.NETCONF_RPC_NS_1_0));
        Node childElement = DocumentUtils.getChildNodeByName(filterChildElement, TOP, NetconfResources
                .NETCONF_RPC_NS_1_0);
        assertEquals(DocumentUtils.documentToPrettyString(childElement),
                DocumentUtils.documentToPrettyString(getConfigRequest.getFilter().getXmlFilterElements().get(0)));

        getConfigRequestWithFilter = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "       <nc:get-config xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "         <nc:source>\n" +
                "           <nc:running/>\n" +
                "         </nc:source>\n" +
                "         <nc:filter type=\"subtree\">\n" +
                "           <nc:top>\n" +
                "             <nc:users/>\n" +
                "           </nc:top>\n" +
                "         </nc:filter>\n" +
                "       </nc:get-config>\n" +
                "     </rpc>";
        getConfigRequest = DocumentToPojoTransformer.getGetConfig(DocumentUtils.stringToDocument
                (getConfigRequestWithFilter));
        assertEquals(RUNNING, getConfigRequest.getSource());
        assertEquals(SUBTREE, getConfigRequest.getFilter().getType());
        document = DocumentUtils.stringToDocument(getConfigRequestWithFilter);
        filterChildElement = (DocumentUtils.getChildNodeByName(document, NetconfResources.FILTER,
                NetconfResources.NETCONF_RPC_NS_1_0));
        childElement = DocumentUtils.getChildNodeByName(filterChildElement, TOP, NetconfResources.NETCONF_RPC_NS_1_0);
        assertEquals(DocumentUtils.documentToPrettyString(childElement),
                DocumentUtils.documentToPrettyString(getConfigRequest.getFilter().getXmlFilterElements().get(0)));

        getConfigRequestWithFilter = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "       <get-config >\n" +
                "         <source>\n" +
                "           <running/>\n" +
                "         </source>\n" +
                "         <nc:filter type=\"subtree\" xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "           <nc:top>\n" +
                "             <nc:users/>\n" +
                "           </nc:top>\n" +
                "         </nc:filter>\n" +
                "       </get-config>\n" +
                "     </rpc>";
        getConfigRequest = DocumentToPojoTransformer.getGetConfig(DocumentUtils.stringToDocument
                (getConfigRequestWithFilter));
        assertEquals(RUNNING, getConfigRequest.getSource());
        assertEquals(SUBTREE, getConfigRequest.getFilter().getType());
        document = DocumentUtils.stringToDocument(getConfigRequestWithFilter);
        filterChildElement = (DocumentUtils.getChildNodeByName(document, NetconfResources.FILTER,
                NetconfResources.NETCONF_RPC_NS_1_0));
        childElement = DocumentUtils.getChildNodeByName(filterChildElement, TOP, NetconfResources.NETCONF_RPC_NS_1_0);

        assertEquals(DocumentUtils.documentToPrettyString(childElement),
                DocumentUtils.documentToPrettyString(getConfigRequest.getFilter().getXmlFilterElements().get(0)));

        getConfigRequestWithFilter = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "       <get-config >\n" +
                "         <nc:source xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.5\">\n" +
                "           <nc:running/>\n" +
                "         </nc:source>\n" +
                "         <filter type=\"subtree\">\n" +
                "           <top>\n" +
                "             <users/>\n" +
                "           </top>\n" +
                "         </filter>\n" +
                "       </get-config>\n" +
                "     </rpc>";
        try {
            DocumentToPojoTransformer.getGetConfig(DocumentUtils.stringToDocument(getConfigRequestWithFilter));
            fail("Expected an exception here");
        } catch (NetconfMessageBuilderException e) {
            assertEquals(SOURCE_CANNOT_BE_NULL_EMPTY, e.getMessage());
        }
    }

    @Test
    public void testLockRequest() throws NetconfMessageBuilderException {
        String getLockRequestString = "<nc:rpc message-id=\"101\" " +
                "xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "  <nc:lock>\n" +
                "   <nc:target>\n" +
                "      <nc:running/>\n" +
                "    </nc:target>\n" +
                "  </nc:lock>\n" +
                "</nc:rpc>";
        LockRequest getLockRequest = DocumentToPojoTransformer.getLockRequest(DocumentUtils.stringToDocument
                (getLockRequestString));
        assertEquals(RUNNING, getLockRequest.getTarget());

        getLockRequestString = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "  <nc:lock xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "   <nc:target>\n" +
                "      <nc:running/>\n" +
                "    </nc:target>\n" +
                "  </nc:lock>\n" +
                "</rpc>";
        getLockRequest = DocumentToPojoTransformer.getLockRequest(DocumentUtils.stringToDocument(getLockRequestString));
        assertEquals(RUNNING, getLockRequest.getTarget());

        getLockRequestString = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "  <lock>\n" +
                "   <nc:target xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "      <nc:running/>\n" +
                "    </nc:target>\n" +
                "  </lock>\n" +
                "</rpc>";
        getLockRequest = DocumentToPojoTransformer.getLockRequest(DocumentUtils.stringToDocument(getLockRequestString));
        assertEquals(RUNNING, getLockRequest.getTarget());

        getLockRequestString = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "  <lock>\n" +
                "   <nc:target xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.8\">\n" +
                "      <nc:running/>\n" +
                "    </nc:target>\n" +
                "  </lock>\n" +
                "</rpc>";
        try {
            DocumentToPojoTransformer.getLockRequest(DocumentUtils.stringToDocument(getLockRequestString));
            fail("Expected an exception here");
        } catch (NetconfMessageBuilderException e) {
            assertEquals(TARGET_CANNOT_BE_NULL_EMPTY, e.getMessage());
        }
    }

    @Test
    public void testUnlockRequest() throws NetconfMessageBuilderException {
        String getUnlockRequestString = "<nc:rpc message-id=\"101\" " +
                "xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "  <nc:unlock>\n" +
                "   <nc:target>\n" +
                "      <nc:running/>\n" +
                "    </nc:target>\n" +
                "  </nc:unlock>\n" +
                "</nc:rpc>";
        UnLockRequest getUnlockRequest = DocumentToPojoTransformer.getUnLockRequest
                (DocumentUtils.stringToDocument(getUnlockRequestString));
        assertEquals(RUNNING, getUnlockRequest.getTarget());

        getUnlockRequestString = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "  <nc:unlock xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "   <nc:target>\n" +
                "      <nc:running/>\n" +
                "    </nc:target>\n" +
                "  </nc:unlock>\n" +
                "</rpc>";
        getUnlockRequest = DocumentToPojoTransformer.getUnLockRequest(DocumentUtils.stringToDocument
                (getUnlockRequestString));
        assertEquals(RUNNING, getUnlockRequest.getTarget());

        getUnlockRequestString = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "  <unlock>\n" +
                "   <nc:target xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "      <nc:running/>\n" +
                "    </nc:target>\n" +
                "  </unlock>\n" +
                "</rpc>";
        getUnlockRequest = DocumentToPojoTransformer.getUnLockRequest(DocumentUtils.stringToDocument
                (getUnlockRequestString));
        assertEquals(RUNNING, getUnlockRequest.getTarget());

        getUnlockRequestString = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "  <unlock>\n" +
                "   <nc:target xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.8\">\n" +
                "      <nc:running/>\n" +
                "    </nc:target>\n" +
                "  </unlock>\n" +
                "</rpc>";
        try {
            DocumentToPojoTransformer.getUnLockRequest(DocumentUtils.stringToDocument(getUnlockRequestString));
            fail("Expected an exception here");
        } catch (NetconfMessageBuilderException e) {
            assertEquals(TARGET_CANNOT_BE_NULL_EMPTY, e.getMessage());
        }
    }

    @Test
    public void testGetAction() throws NetconfMessageBuilderException {
        String req = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
                "<test:test-action-container xmlns:test=\"urn:example:test-action\">" +
                "<test:action-list>" +
                "<test:name>apache</test:name>" +
                "<test:reset>" +
                "<test:reset-at>2014-07-29T13:42:00Z</test:reset-at>" +
                "</test:reset>" +
                "</test:action-list>" +
                "</test:test-action-container>" +
                "</action>" +
                "</rpc>";

        String expectedActionElement = "<test:test-action-container xmlns:test=\"urn:example:test-action\">" +
                "<test:action-list>" +
                "<test:name>apache</test:name>" +
                "<test:reset>" +
                "<test:reset-at>2014-07-29T13:42:00Z</test:reset-at>" +
                "</test:reset>" +
                "</test:action-list>" +
                "</test:test-action-container>";
        ActionRequest validActionRequest = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(req));
        assertEquals(expectedActionElement, DocumentUtils.documentToString(validActionRequest.getActionTreeElement()));


        String invalidActionTreeReq = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
                "</action>" +
                "</rpc>";
        try {
            ActionRequest invalidActionTreeRequest = DocumentToPojoTransformer.getAction(DocumentUtils
                    .stringToDocument(invalidActionTreeReq));
            fail("Expected an exception here");
        } catch (NetconfMessageBuilderException e) {
            assertEquals(ACTIONTREE_CANNOT_BE_NULL_EMPTY, e.getMessage());
        }

        try {
            ActionRequest invalidActionRequest = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument
                    (expectedActionElement));
            fail("Expected an exception here");
        } catch (NetconfMessageBuilderException e) {
            assertEquals(ACTION_CANNOT_BE_NULL_EMPTY, e.getMessage());
        }
    }

    @Test
    public void testGetRpcRequest() throws NetconfMessageBuilderException {

        String rpcRequest = "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"10\">\n"
                + "    <get-config >\n"
                + "        <source>\n"
                + "            <nc:running xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.1\"/>\n"
                + "        </source>\n"
                + "    </get-config>\n"
                + "</rpc>";
        NetconfRpcRequest netconfRpcRequest = DocumentToPojoTransformer
                .getRpcRequest(DocumentUtils.stringToDocument(rpcRequest));
        assertNotNull(netconfRpcRequest);
    }

    @Test
    public void testGetCreateSubscriptionRequest() throws NetconfMessageBuilderException {

        String request = "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<create-subscription xmlns=\"urn:ietf:params:xml:ns:netconf:notification:1.0\">"
                + "	<stream>NETCONF</stream>"
                + "	<filter type=\"subtree\">"
                + "		<state-change-notification xmlns=\"urn:ietf:params:xml:ns:yang:ietf-netconf-notifications\"/>"
                + "	</filter>"
                + "</create-subscription>"
                + "</rpc>";
        CreateSubscriptionRequest subscriptionRequest = DocumentToPojoTransformer
                .getCreateSubscriptionRequest(DocumentUtils.stringToDocument(request));
        assertNotNull(subscriptionRequest);
    }

    @Test
    public void testGetBytesFromDocument() throws NetconfMessageBuilderException {
        URL url = Thread.currentThread().getContextClassLoader().getResource("get.xml");
        File file = new File(url.getPath());
        Document document = DocumentUtils.getDocFromFile(file);
        byte[] result = DocumentToPojoTransformer.getBytesFromDocument(document);
        assertNotNull(result);
    }

}
