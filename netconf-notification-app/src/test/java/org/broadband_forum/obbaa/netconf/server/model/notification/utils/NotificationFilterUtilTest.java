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

package org.broadband_forum.obbaa.netconf.server.model.notification.utils;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfFilter;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterNode;

/**
 * Created by pregunat on 2/5/16.
 */
public class NotificationFilterUtilTest {

    @Test
    public void testMatches() throws NetconfMessageBuilderException {
        Notification notification = DocumentToPojoTransformer.getNotification(DocumentUtils.loadXmlDocument(NotificationFilterUtilTest.class.getResourceAsStream("/sample-state-change-notification.xml")));
        FilterNode filterNode = new FilterNode();
        filterNode.addSelectNode(NetconfResources.STATE_CHANGE_NOTIFICATION, NetconfResources.IETF_NOTIFICATION_NS);

        // test select filter
        assertTrue(NotificationFilterUtil.matches(notification, filterNode));

        filterNode = new FilterNode();
        filterNode.addContainmentNode(NetconfResources.STATE_CHANGE_NOTIFICATION, NetconfResources.IETF_NOTIFICATION_NS)
                .addMatchNode(NetconfResources.TARGET, NetconfResources.IETF_NOTIFICATION_NS, "song-count");
        filterNode.getContainmentNodes(NetconfResources.STATE_CHANGE_NOTIFICATION).get(0).addSelectNode(NetconfResources.STATE_CHANGE_VALUE, NetconfResources.IETF_NOTIFICATION_NS);

        // test match & select filter
        assertTrue(NotificationFilterUtil.matches(notification, filterNode));

        filterNode = new FilterNode();
        filterNode.addContainmentNode(NetconfResources.STATE_CHANGE_NOTIFICATION, NetconfResources.IETF_NOTIFICATION_NS)
                .addSelectNode(NetconfResources.TARGET, NetconfResources.IETF_NOTIFICATION_NS);

        // test filter match failing case
        assertFalse(NotificationFilterUtil.matches(notification, filterNode));

        filterNode = new FilterNode();
        filterNode.addSelectNode(NetconfResources.TARGET, NetconfResources.IETF_NOTIFICATION_NS);

        // test filter match failing case
        assertFalse(NotificationFilterUtil.matches(notification, filterNode));

        notification = DocumentToPojoTransformer.getNotification(DocumentUtils.loadXmlDocument(NotificationFilterUtilTest.class.getResourceAsStream("/sample-netconf-config-change-notification.xml")));
        filterNode = new FilterNode();
        filterNode.addContainmentNode("netconf-config-change", NetconfResources.IETF_NOTIFICATION_NS)
                .addContainmentNode("edit", NetconfResources.IETF_NOTIFICATION_NS)
                .addMatchNode("target", NetconfResources.IETF_NOTIFICATION_NS, "/pma/device-holder[name=OLT1]");

        filterNode.getContainmentNodes("netconf-config-change").get(0).getContainmentNodes("edit").get(0).addSelectNode("operation", NetconfResources.IETF_NOTIFICATION_NS);

        // test filter with child nodes, match and select nodes
        assertTrue(NotificationFilterUtil.matches(notification, filterNode));

    }

    @Test
    public void testFilterNotificationMatches() throws NetconfMessageBuilderException {
        Notification notification = DocumentToPojoTransformer.getNotification(DocumentUtils.loadXmlDocument(NotificationFilterUtilTest.class.getResourceAsStream("/sample-alarm-notification.xml")));
        NetconfFilter filter = new NetconfFilter();
        String xmlFilter = "<filter type=\"subtree\">\n" +
                "  <alarm-notification xmlns=\"http://www.test.com/solutions/anv-alarms\">\n" +
                "      <alarm>\n" +
                "        <last-perceived-severity>major</last-perceived-severity>\n" +
                "      </alarm>\n" +
                "  </alarm-notification>\n" +
                "</filter>";
        filter.setXmlFilters(DocumentUtils.getChildElements(DocumentUtils.stringToDocument(xmlFilter).getElementsByTagName("filter").item(0)));

        String expected = "<alarms:alarm-notification xmlns:alarms=\"http://www.test.com/solutions/anv-alarms\"><alarms:alarm>\n" +
                "            <alarms:resource xmlns:adh=\"http://www.test.com/solutions/anv-device-holders\" xmlns:anv=\"http://www.test.com/solutions/anv\">/anv:device-manager/adh:new-devices/adh:new-device[adh:identification-method='duid'][adh:identification-value='TestANV.R1.S1.LT1.PON1.ONT5']</alarms:resource>\n" +
                "            <alarms:alarm-type-id xmlns:adh=\"http://www.test.com/solutions/anv-device-holders\">adh:new-device</alarms:alarm-type-id>\n" +
                "            <alarms:alarm-type-qualifier/>\n" +
                "            <alarms:last-status-change>2018-10-31T09:41:11.776Z</alarms:last-status-change>\n" +
                "            <alarms:last-perceived-severity>major</alarms:last-perceived-severity>\n" +
                "            <alarms:last-alarm-text>hostAddress: 30.1.1.20 port: 50602</alarms:last-alarm-text>\n" +
                "            <alarms:last-alarm-condition>ALARM_OFF</alarms:last-alarm-condition>\n" +
                "        </alarms:alarm><alarms:alarm>\n" +
                "            <alarms:resource xmlns:adh=\"http://www.test.com/solutions/anv-device-holders\" xmlns:anv=\"http://www.test.com/solutions/anv\">/anv:device-manager/adh:device[adh:device-id='R1.S1.LT1.PON1.ONT1']</alarms:resource>\n" +
                "            <alarms:alarm-type-id xmlns:adh=\"http://www.test.com/solutions/anv-device-holders\">adh:device-unreachable</alarms:alarm-type-id>\n" +
                "            <alarms:alarm-type-qualifier/>\n" +
                "            <alarms:last-status-change>2018-10-31T09:41:11.785Z</alarms:last-status-change>\n" +
                "            <alarms:last-perceived-severity>major</alarms:last-perceived-severity>\n" +
                "            <alarms:last-alarm-text/>\n" +
                "            <alarms:last-alarm-condition>ALARM_ON</alarms:last-alarm-condition>\n" +
                "        </alarms:alarm></alarms:alarm-notification>";

        Notification result = NotificationFilterUtil.filterNotification(notification, filter);
        assertEquals(expected, DocumentUtils.documentToString(result.getNotificationElement()));

        xmlFilter = "<filter type=\"subtree\">\n" +
                "  <alarm-notification xmlns=\"http://www.test.com/solutions/anv-alarms\">\n" +
                "      <alarm>\n" +
                "        <last-perceived-severity>major</last-perceived-severity>\n" +
                "      </alarm>\n" +
                "      <alarm>\n" +
                "        <last-perceived-severity>critical</last-perceived-severity>\n" +
                "      </alarm>\n" +
                "  </alarm-notification>\n" +
                "</filter>";
        filter.setXmlFilters(DocumentUtils.getChildElements(DocumentUtils.stringToDocument(xmlFilter).getElementsByTagName("filter").item(0)));
        expected = "<alarms:alarm-notification xmlns:alarms=\"http://www.test.com/solutions/anv-alarms\"><alarms:alarm>\n" +
                "            <alarms:resource xmlns:adh=\"http://www.test.com/solutions/anv-device-holders\" xmlns:anv=\"http://www.test.com/solutions/anv\">/anv:device-manager/adh:new-devices/adh:new-device[adh:identification-method='duid'][adh:identification-value='TestANV.R1.S1.LT1.PON1.ONT5']</alarms:resource>\n" +
                "            <alarms:alarm-type-id xmlns:adh=\"http://www.test.com/solutions/anv-device-holders\">adh:new-device</alarms:alarm-type-id>\n" +
                "            <alarms:alarm-type-qualifier/>\n" +
                "            <alarms:last-status-change>2018-10-31T09:41:11.776Z</alarms:last-status-change>\n" +
                "            <alarms:last-perceived-severity>major</alarms:last-perceived-severity>\n" +
                "            <alarms:last-alarm-text>hostAddress: 30.1.1.20 port: 50602</alarms:last-alarm-text>\n" +
                "            <alarms:last-alarm-condition>ALARM_OFF</alarms:last-alarm-condition>\n" +
                "        </alarms:alarm><alarms:alarm>\n" +
                "            <alarms:resource xmlns:adh=\"http://www.test.com/solutions/anv-device-holders\" xmlns:anv=\"http://www.test.com/solutions/anv\">/anv:device-manager/adh:new-devices/adh:new-device[adh:identification-method='duid'][adh:identification-value='TestANV.R1.S1.LT1.PON1.ONT2']</alarms:resource>\n" +
                "            <alarms:alarm-type-id xmlns:adh=\"http://www.test.com/solutions/anv-device-holders\">adh:new-device</alarms:alarm-type-id>\n" +
                "            <alarms:alarm-type-qualifier/>\n" +
                "            <alarms:last-status-change>2018-10-31T09:41:11.776Z</alarms:last-status-change>\n" +
                "            <alarms:last-perceived-severity>critical</alarms:last-perceived-severity>\n" +
                "            <alarms:last-alarm-text>hostAddress: 30.1.1.20 port: 58321</alarms:last-alarm-text>\n" +
                "            <alarms:last-alarm-condition>ALARM_OFF</alarms:last-alarm-condition>\n" +
                "        </alarms:alarm><alarms:alarm>\n" +
                "            <alarms:resource xmlns:adh=\"http://www.test.com/solutions/anv-device-holders\" xmlns:anv=\"http://www.test.com/solutions/anv\">/anv:device-manager/adh:device[adh:device-id='R1.S1.LT1.PON1.ONT1']</alarms:resource>\n" +
                "            <alarms:alarm-type-id xmlns:adh=\"http://www.test.com/solutions/anv-device-holders\">adh:device-unreachable</alarms:alarm-type-id>\n" +
                "            <alarms:alarm-type-qualifier/>\n" +
                "            <alarms:last-status-change>2018-10-31T09:41:11.785Z</alarms:last-status-change>\n" +
                "            <alarms:last-perceived-severity>major</alarms:last-perceived-severity>\n" +
                "            <alarms:last-alarm-text/>\n" +
                "            <alarms:last-alarm-condition>ALARM_ON</alarms:last-alarm-condition>\n" +
                "        </alarms:alarm></alarms:alarm-notification>";
        result = NotificationFilterUtil.filterNotification(notification, filter);
        assertEquals(expected, DocumentUtils.documentToString(result.getNotificationElement()));

        xmlFilter = "<filter type=\"subtree\">\n" +
                "  <alarm-notification xmlns=\"http://www.test.com/solutions/anv-alarms\">\n" +
                "  </alarm-notification>\n" +
                "</filter>";
        filter.setXmlFilters(DocumentUtils.getChildElements(DocumentUtils.stringToDocument(xmlFilter).getElementsByTagName("filter").item(0)));

        expected = "<alarms:alarm-notification xmlns:alarms=\"http://www.test.com/solutions/anv-alarms\">\n" +
                "        <alarms:alarm>\n" +
                "            <alarms:resource xmlns:adh=\"http://www.test.com/solutions/anv-device-holders\" xmlns:anv=\"http://www.test.com/solutions/anv\">/anv:device-manager/adh:new-devices/adh:new-device[adh:identification-method='duid'][adh:identification-value='TestANV.R1.S1.LT1.PON1.ONT5']</alarms:resource>\n" +
                "            <alarms:alarm-type-id xmlns:adh=\"http://www.test.com/solutions/anv-device-holders\">adh:new-device</alarms:alarm-type-id>\n" +
                "            <alarms:alarm-type-qualifier/>\n" +
                "            <alarms:last-status-change>2018-10-31T09:41:11.776Z</alarms:last-status-change>\n" +
                "            <alarms:last-perceived-severity>major</alarms:last-perceived-severity>\n" +
                "            <alarms:last-alarm-text>hostAddress: 30.1.1.20 port: 50602</alarms:last-alarm-text>\n" +
                "            <alarms:last-alarm-condition>ALARM_OFF</alarms:last-alarm-condition>\n" +
                "        </alarms:alarm>\n" +
                "        <alarms:alarm>\n" +
                "            <alarms:resource xmlns:adh=\"http://www.test.com/solutions/anv-device-holders\" xmlns:anv=\"http://www.test.com/solutions/anv\">/anv:device-manager/adh:new-devices/adh:new-device[adh:identification-method='duid'][adh:identification-value='TestANV.R1.S1.LT1.PON1.ONT2']</alarms:resource>\n" +
                "            <alarms:alarm-type-id xmlns:adh=\"http://www.test.com/solutions/anv-device-holders\">adh:new-device</alarms:alarm-type-id>\n" +
                "            <alarms:alarm-type-qualifier/>\n" +
                "            <alarms:last-status-change>2018-10-31T09:41:11.776Z</alarms:last-status-change>\n" +
                "            <alarms:last-perceived-severity>critical</alarms:last-perceived-severity>\n" +
                "            <alarms:last-alarm-text>hostAddress: 30.1.1.20 port: 58321</alarms:last-alarm-text>\n" +
                "            <alarms:last-alarm-condition>ALARM_OFF</alarms:last-alarm-condition>\n" +
                "        </alarms:alarm>\n" +
                "        <alarms:alarm>\n" +
                "            <alarms:resource xmlns:adh=\"http://www.test.com/solutions/anv-device-holders\" xmlns:anv=\"http://www.test.com/solutions/anv\">/anv:device-manager/adh:device[adh:device-id='R1.S1.LT1.PON1.ONT1']</alarms:resource>\n" +
                "            <alarms:alarm-type-id xmlns:adh=\"http://www.test.com/solutions/anv-device-holders\">adh:device-unreachable</alarms:alarm-type-id>\n" +
                "            <alarms:alarm-type-qualifier/>\n" +
                "            <alarms:last-status-change>2018-10-31T09:41:11.785Z</alarms:last-status-change>\n" +
                "            <alarms:last-perceived-severity>major</alarms:last-perceived-severity>\n" +
                "            <alarms:last-alarm-text/>\n" +
                "            <alarms:last-alarm-condition>ALARM_ON</alarms:last-alarm-condition>\n" +
                "        </alarms:alarm>\n" +
                "    </alarms:alarm-notification>";
        result = NotificationFilterUtil.filterNotification(notification, filter);
        assertEquals(expected, DocumentUtils.documentToString(result.getNotificationElement()));
    }

    @Test
    public void testFilterNotificationMissMatches() throws NetconfMessageBuilderException {
        Notification notification = DocumentToPojoTransformer.getNotification(DocumentUtils.loadXmlDocument(NotificationFilterUtilTest.class.getResourceAsStream("/sample-alarm-notification.xml")));
        NetconfFilter filter = new NetconfFilter();
        String xmlFilter = "<filter type=\"subtree\">\n" +
                "  <alarm-notification xmlns=\"http://www.test.com/solutions/anv-alarms\">\n" +
                "      <alarm>\n" +
                "        <last-perceived-severity>aaa</last-perceived-severity>\n" +
                "      </alarm>\n" +
                "  </alarm-notification>\n" +
                "</filter>";
        filter.setXmlFilters(DocumentUtils.getChildElements(DocumentUtils.stringToDocument(xmlFilter).getElementsByTagName("filter").item(0)));

        Notification result = NotificationFilterUtil.filterNotification(notification, filter);
        assertNull(result);

        xmlFilter = "<filter type=\"subtree\">\n" +
                "  <device-notification xmlns=\"http://www.test.com/solutions/anv-alarms\">\n" +
                "  </device-notification>\n" +
                "</filter>";
        filter.setXmlFilters(DocumentUtils.getChildElements(DocumentUtils.stringToDocument(xmlFilter).getElementsByTagName("filter").item(0)));

        result = NotificationFilterUtil.filterNotification(notification, filter);
        assertNull(result);
    }
}
