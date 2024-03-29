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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class NetconfNotificationTest {

    private static final QName START_TIME = QName.create(NetconfResources.IETF_NOTIFICATION_NS, "startTime");
    private static final String EVENT_TIME = "eventTime";
    private NetconfNotification m_netconfNotification;
    private Document m_notifElement;
    private Element m_element;
    private NodeList m_nodeList;

    @Before
    public void setUp() throws Exception {
        m_netconfNotification = new NetconfNotification();
        m_notifElement = mock(Document.class);
        m_element = mock(Element.class);
        m_nodeList = mock(NodeList.class);
        when(m_notifElement.getDocumentElement()).thenReturn(m_element);
        when(m_element.getChildNodes()).thenReturn(m_nodeList);
        when(m_nodeList.getLength()).thenReturn(2);
        Node node = mock(Element.class);
        when(node.getNamespaceURI()).thenReturn(NetconfResources.IETF_NOTIFICATION_NS);
        when(node.getLocalName()).thenReturn("startTime");
        when(m_nodeList.item(1)).thenReturn(node);
    }

    @After
    public void tearDown() throws Exception {
        m_netconfNotification = null;
    }

    @Test
    public void testSetEventTimeWithMillis() throws NetconfMessageBuilderException{
        String dateTimeMillis = "2016-09-06T16:55:19.508724+04:00";
        m_netconfNotification.setEventTime(dateTimeMillis);
        DateTime expectedDateTimeMillis = new DateTime(DateTime.parse(dateTimeMillis).getMillis());
        assertEquals(expectedDateTimeMillis.toString(NetconfResources.DATE_TIME_WITH_TZ), m_netconfNotification.getEventTime());
    }

    @Test
    public void testSetEventTimeWithoutMillis() throws NetconfMessageBuilderException{
        String dateTime = "2016-09-06T16:55:19+04:00";
        m_netconfNotification.setEventTime(dateTime);
        DateTime expectedDateTime = new DateTime(DateTime.parse(dateTime).getMillis());
        assertEquals(expectedDateTime.toString(NetconfResources.DATE_TIME_WITH_TZ), m_netconfNotification.getEventTime());
    }

    @Test
    public void testSetEventTimeWithMillisInUTC() throws NetconfMessageBuilderException{
        String dateTimeMillis = "2016-09-06T16:55:19.508724Z";
        m_netconfNotification.setEventTime(dateTimeMillis);
        DateTime expectedDateTimeMillis = new DateTime(DateTime.parse(dateTimeMillis).getMillis());
        assertEquals(expectedDateTimeMillis.toString(NetconfResources.DATE_TIME_WITH_TZ), m_netconfNotification.getEventTime());
    }

    @Test
    public void testSetEventTimeWithoutMillisInUTC() throws NetconfMessageBuilderException{
        String dateTime = "2016-09-06T16:55:19Z";
        m_netconfNotification.setEventTime(dateTime);
        DateTime expectedDateTime = new DateTime(DateTime.parse(dateTime).getMillis());
        assertEquals(expectedDateTime.toString(NetconfResources.DATE_TIME_WITH_TZ), m_netconfNotification.getEventTime());
    }

    @Test(expected = NetconfMessageBuilderException.class)
    public void testSetEventTimeWithInvalidDateTime() throws NetconfMessageBuilderException{
        String dateTime = "2016-09-06T16:55:19";
        m_netconfNotification.setEventTime(dateTime);
    }

    @Test
    public void testGetEventTime() throws NetconfMessageBuilderException {
        NetconfNotification netconfNotification = new NetconfNotification(m_notifElement);
        assertNotNull(netconfNotification.getEventTime());
    }

    @Test
    public void testGetType() throws NetconfMessageBuilderException {
        Node node = mock(Element.class);
        when(node.getNodeName()).thenReturn(EVENT_TIME);
        when(node.getTextContent()).thenReturn("2017-03-14T10:06:49+00:00");
        when(m_nodeList.item(0)).thenReturn(node);
        NetconfNotification netconfNotification = new NetconfNotification(m_notifElement);
        assertEquals(START_TIME, netconfNotification.getType());
    }

    @Test
    public void testGetTypeWithInvalidEvenTime() throws NetconfMessageBuilderException {
        Node node = mock(Element.class);
        when(node.getNodeName()).thenReturn(EVENT_TIME);
        when(node.getTextContent()).thenReturn("Invalid Time");
        when(m_nodeList.item(0)).thenReturn(node);
        try {
            NetconfNotification netconfNotification = new NetconfNotification(m_notifElement);
        } catch (Exception e) {
            assertTrue(e instanceof NetconfMessageBuilderException);
            assertEquals("Invalid event time format: Invalid Time", e.getMessage());
        }
    }

    @Test
    public void testNotificationToString() throws NetconfMessageBuilderException {
        Document doc = DocumentUtils.stringToDocument("<notification/>");
        NetconfNotification testObj = new NetconfNotification(doc);
        String expectedNotificationString = "<notification xmlns=\"urn:ietf:params:xml:ns:netconf:notification:1.0\">" + "<eventTime>"
                + testObj.getEventTime() + "</eventTime></notification>";
        assertEquals(expectedNotificationString, testObj.notificationToString());
    }

    @Test
    public void testNotificationToPrettyString() throws NetconfMessageBuilderException {
        Document doc = DocumentUtils.stringToDocument("<notification/>");
        NetconfNotification testObj = new NetconfNotification(doc);
        String expectedNotificationString = "<notification xmlns=\"urn:ietf:params:xml:ns:netconf:notification:1.0\">" + "\n   <eventTime>"
                + testObj.getEventTime() + "</eventTime>\n</notification>\n";
        assertEquals(expectedNotificationString, testObj.notificationToPrettyString());
    }
    
    @Test
    public void testToString() throws NetconfMessageBuilderException {
        Document doc = DocumentUtils.stringToDocument("<notification/>");
        NetconfNotification testObj = new NetconfNotification(doc);
        String expectedNotificationString = "<notification xmlns=\"urn:ietf:params:xml:ns:netconf:notification:1.0\">" + "\n   <eventTime>"
                + testObj.getEventTime() + "</eventTime>\n</notification>\n";
        assertEquals(expectedNotificationString, testObj.toString());
    }

    @Test
    public void testNotificationWithoutWR() throws NetconfMessageBuilderException {
        m_netconfNotification = new NetconfNotification(m_notifElement);
        assertNull(m_netconfNotification.getNotificationString());
        assertNull(m_netconfNotification.getNotificationElementWR());
    }

    @Test
    public void testNotificationWithWR() throws NetconfMessageBuilderException {
        m_netconfNotification = new NetconfNotification(m_notifElement, "NotificationString");
        assertEquals("NotificationString", m_netconfNotification.getNotificationString());
        assertNotNull(m_netconfNotification.getNotificationElementWR());
    }

    @Test
    public void testGetTypeWithNotifElementWR() throws NetconfMessageBuilderException{
        Node node = mock(Element.class);
        when(node.getNodeName()).thenReturn(EVENT_TIME);
        when(node.getTextContent()).thenReturn("2017-03-14T10:06:49+00:00");
        when(m_nodeList.item(0)).thenReturn(node);
        NetconfNotification netconfNotification = new NetconfNotification(m_notifElement, "NotificationString");
        assertEquals(START_TIME, netconfNotification.getType());
    }

    @Test
    public void testGetTypeWithInvalidEvenTimeWithNotifElementWR() {
        Node node = mock(Element.class);
        when(node.getNodeName()).thenReturn(EVENT_TIME);
        when(node.getTextContent()).thenReturn("Invalid Time");
        when(m_nodeList.item(0)).thenReturn(node);
        try {
            new NetconfNotification(m_notifElement, "NotificationString");
        } catch (Exception e) {
            assertTrue(e instanceof NetconfMessageBuilderException);
            assertEquals("Invalid event time format: Invalid Time", e.getMessage());
        }
    }

    @Test
    public void testGetNotificationElementWithNotifElementWRNull() throws NetconfMessageBuilderException {
        String notificationString = "<notification xmlns=\"urn:ietf:params:xml:ns:netconf:notification:1.0\">\n" +
                "    <eventTime>2021-03-29T18:56:13.894+00:00</eventTime>\n" +
                "    <hw:hardware-state xmlns:hw=\"urn:ietf:params:xml:ns:yang:ietf-hardware\">\n" +
                "       <hw:component>\n" +
                "          <hw:name>Chassis</hw:name>\n" +
                "          <bbf-sim:software xmlns:bbf-sim=\"urn:bbf:yang:bbf-software-image-management-one-dot-one\">\n" +
                "             <bbf-sim:software>\n" +
                "                <bbf-sim:name>application_software</bbf-sim:name>\n" +
                "                <bbf-sim:revisions>\n" +
                "                   <bbf-sim:revision>\n" +
                "                      <bbf-sim:name>L6GQAF61.373</bbf-sim:name>\n" +
                "                      <bbf-sim:commit-revision>\n" +
                "                         <bbf-sim:revision-committed/>\n" +
                "                      </bbf-sim:commit-revision>\n" +
                "                   </bbf-sim:revision>\n" +
                "                </bbf-sim:revisions>\n" +
                "             </bbf-sim:software>\n" +
                "          </bbf-sim:software>\n" +
                "       </hw:component>\n" +
                "    </hw:hardware-state>\n" +
                "</notification>";
        Document document = DocumentUtils.stringToDocument(notificationString);
        NetconfNotification netconfNotification = new NetconfNotification(document, notificationString);
        netconfNotification.setNotificationElementWR(null);
        Element elementNotification = netconfNotification.getNotificationElement();
        String expectedElementNotification = "<hw:hardware-state xmlns:hw=\"urn:ietf:params:xml:ns:yang:ietf-hardware\"\n" +
                "                   xmlns=\"urn:ietf:params:xml:ns:netconf:notification:1.0\">\n" +
                "   <hw:component>\n" +
                "      <hw:name>Chassis</hw:name>\n" +
                "      <bbf-sim:software xmlns:bbf-sim=\"urn:bbf:yang:bbf-software-image-management-one-dot-one\">\n" +
                "         <bbf-sim:software>\n" +
                "            <bbf-sim:name>application_software</bbf-sim:name>\n" +
                "            <bbf-sim:revisions>\n" +
                "               <bbf-sim:revision>\n" +
                "                  <bbf-sim:name>L6GQAF61.373</bbf-sim:name>\n" +
                "                  <bbf-sim:commit-revision>\n" +
                "                     <bbf-sim:revision-committed/>\n" +
                "                  </bbf-sim:commit-revision>\n" +
                "               </bbf-sim:revision>\n" +
                "            </bbf-sim:revisions>\n" +
                "         </bbf-sim:software>\n" +
                "      </bbf-sim:software>\n" +
                "   </hw:component>\n" +
                "</hw:hardware-state>\n";

        assertEquals(expectedElementNotification, DocumentUtils.documentToPrettyString(elementNotification));
    }

}
