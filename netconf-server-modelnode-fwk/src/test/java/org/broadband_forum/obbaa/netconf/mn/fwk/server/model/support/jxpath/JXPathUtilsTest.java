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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.jxpath;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.JXPathCompiledExpression;
import org.apache.commons.jxpath.ri.compiler.Constant;
import org.apache.commons.jxpath.ri.compiler.CoreFunction;
import org.apache.commons.jxpath.ri.compiler.CoreOperationEqual;
import org.apache.commons.jxpath.ri.compiler.Expression;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.junit.Test;
import org.w3c.dom.Document;


/**
 * Created by vgotagi on 10/18/16.
 */
public class JXPathUtilsTest {

    @Test
    public void testGetExpression() {
        Expression[] expressions = new Expression[2];
        Expression exp = new CoreOperationEqual(expressions[0], expressions[1]);
        JXPathCompiledExpression jcExp = new JXPathCompiledExpression("xPath", exp);
        assertNotNull(JXPathUtils.getExpression(jcExp));
    }

    @Test
    public void testNull() {
        assertNull(JXPathUtils.getExpression((JXPathCompiledExpression) null));
    }

    @Test
    public void testGetConstantExpression() {
        Constant result = JXPathUtils.getConstantExpression("test");
        Object constantvalue = result.compute(null);
        assertEquals(String.class, constantvalue.getClass());
        assertEquals("test", constantvalue);

        result = JXPathUtils.getConstantExpression("5.6");
        constantvalue = result.compute(null);
        assertEquals(Double.class, constantvalue.getClass());
        assertEquals(5.6D, (Double) constantvalue, 1e-15);

        result = JXPathUtils.getConstantExpression("56");
        constantvalue = result.compute(null);
        assertEquals(Double.class, constantvalue.getClass());
        assertEquals(56D, (Double) constantvalue, 1e-15);

        result = JXPathUtils.getConstantExpression("1.0.0");
        constantvalue = result.compute(null);
        assertEquals(String.class, constantvalue.getClass());
        assertEquals("1.0.0", constantvalue);

        result = JXPathUtils.getConstantExpression("04/01");
        constantvalue = result.compute(null);
        assertEquals(String.class, constantvalue.getClass());
        assertEquals("04/01", constantvalue);
    }

    @Test
    public void testGetCoreFunction() throws Exception {
        Set<String> set = new HashSet<>();
        Constant result = JXPathUtils.getConstantExpression("test");
        Object[] values = new Object[]{true, false, set, result};
        Object[] nullValues = new Object[]{null};
        Object[] emptyValues = new Object[]{};

        JXPathUtils.getCoreFunction(4, values);

        JXPathUtils.getCoreFunction(0, nullValues);

        JXPathUtils.getCoreFunction(0, emptyValues);

        JXPathUtils.getCoreFunction(Compiler.FUNCTION_NOT, values);

    }

    @Test
    public void testStringArguments() {
        Object[] values = new Object[2];
        values[0] = "3.0";
        values[1] = ".";
        CoreFunction coreFunction = JXPathUtils.getCoreFunction(Compiler.FUNCTION_SUBSTRING_BEFORE, values);
        Object result = coreFunction.compute(null);
        // without the implementation of JXPathUtils.shouldBeStringArgument, this returns "", which is wrong
        assertEquals("3", result);

        assertEquals(3.0D, JXPathUtils.getConstantExpression("3.0", false).compute(null));
        assertEquals("3.0", JXPathUtils.getConstantExpression("3.0", true).compute(null));
    }

    @Test
    public void testIsInvalidFilter_Null_Empty() {
        assertFalse(JXPathUtils.isInvalidFilter(null));
        assertFalse(JXPathUtils.isInvalidFilter(JXPathUtils.EMPTY));
    }

    @Test
    public void testIsInvalidFilter_SingleFilter_Passes() {
        // Missing close quote
        assertTrue(JXPathUtils.isInvalidFilter("/notification/device-manager/device/device-specific-data/onu-state-change/onu-state='bbf-xpon-onu-types:onu-present-and-on-intended-channel-termination"));

        // XPath Predicates
        assertTrue(JXPathUtils.isInvalidFilter("/notification/device-manager/device[device-id='MyOLT']/device-specific-data/onu-state-change/v-ani-ref = 'MyOnt'"));

        // The axes is specified in the form of <prefix>:<name>
        assertTrue(JXPathUtils.isInvalidFilter("/notification/device-manager/xxx:device='1'"));
    }

    @Test
    public void testIsInvalidFilter_MultipleFilter_Passes() {
        assertTrue(JXPathUtils.isInvalidFilter("/notification/device-manager/device='1/device-specific-data/onu-state-change='true'"));
        assertTrue(JXPathUtils.isInvalidFilter("/notification/device-manager/device='1'/device-specific-data/xxx:onu-state-change=true'"));
    }

    @Test
    public void testIsInvalidFilter_SingleFilter_Fails() {
        assertFalse(JXPathUtils.isInvalidFilter("/notification/device-manager/device/device-specific-data/onu-state-change"));
        assertFalse(JXPathUtils.isInvalidFilter("/notification/device-manager/device/device-specific-data/onu-state-change/onu-state='bbf-xpon-onu-types:onu-present-and-on-intended-channel-termination'"));

        // Without quotes
        assertFalse(JXPathUtils.isInvalidFilter("/notification/device-manager/device=1"));
        assertFalse(JXPathUtils.isInvalidFilter("/notification/device-manager/device!=1"));
        assertFalse(JXPathUtils.isInvalidFilter("/notification/device-manager/device>1"));
        assertFalse(JXPathUtils.isInvalidFilter("/notification/device-manager/device>=1"));
        assertFalse(JXPathUtils.isInvalidFilter("/notification/device-manager/device<1"));
        assertFalse(JXPathUtils.isInvalidFilter("/notification/device-manager/device<=1"));

        // With quotes
        assertFalse(JXPathUtils.isInvalidFilter("/notification/device-manager/device='1'"));
        assertFalse(JXPathUtils.isInvalidFilter("/notification/device-manager/device!='1'"));
        assertFalse(JXPathUtils.isInvalidFilter("/notification/device-manager/device>'1'"));
        assertFalse(JXPathUtils.isInvalidFilter("/notification/device-manager/device>='1'"));
        assertFalse(JXPathUtils.isInvalidFilter("/notification/device-manager/device<'1'"));
        assertFalse(JXPathUtils.isInvalidFilter("/notification/device-manager/device<='1'"));

        // Having space
        assertFalse(JXPathUtils.isInvalidFilter("/notification/device-manager/device= '1'"));
        assertFalse(JXPathUtils.isInvalidFilter("/notification/device-manager/device !='1'"));
        assertFalse(JXPathUtils.isInvalidFilter("/notification/device-manager/device > '1'"));
        assertFalse(JXPathUtils.isInvalidFilter("/notification/device-manager/device  >=  '1'"));
    }

    @Test
    public void testIsInvalidFilter_MultipleFilter_Fails() {
        assertFalse(JXPathUtils.isInvalidFilter("/notification/device-manager/device='1'/device-specific-data/onu-state-change"));
        assertFalse(JXPathUtils.isInvalidFilter("/notification/device-manager/device='1'/device-specific-data/onu-state-change='true'"));
    }

    @Test
    public void testIsNotificationMatchesXpath() throws Exception {

        String notification = "<notification\n" +
                "    xmlns=\"urn:ietf:params:xml:ns:netconf:notification:1.0\">\n" +
                "    <eventTime>2021-02-23T08:32:26.454+00:00</eventTime>\n" +
                "    <alarms:alarm-notification\n" +
                "        xmlns:alarms=\"http://www.test-company.com/solutions/anv-alarms\">\n" +
                "        <alarms:alarm>\n" +
                "            <alarms:resource\n" +
                "                xmlns:license=\"http://www.test-company.com/solutions/license-management\"\n" +
                "                xmlns:platform=\"http://www.test-company.com/solutions/anv-platform\">/platform:platform/license:license-details\n" +
                "            \n" +
                "            </alarms:resource>\n" +
                "            <alarms:alarm-type-id\n" +
                "                xmlns:license=\"http://www.test-company.com/solutions/license-management\">license:missing-license\n" +
                "            \n" +
                "            </alarms:alarm-type-id>\n" +
                "            <alarms:alarm-type-qualifier/>\n" +
                "            <alarms:resource-json-id>/anv-platform:platform/license-management:license-details</alarms:resource-json-id>\n" +
                "            <alarms:alarm-type-json-id>license-management:missing-license</alarms:alarm-type-json-id>\n" +
                "            <alarms:alarm-type-qualifier-json/>\n" +
                "            <alarms:last-status-change>2021-02-23T08:32:26.279Z</alarms:last-status-change>\n" +
                "            <alarms:last-perceived-severity>critical</alarms:last-perceived-severity>\n" +
                "            <alarms:last-alarm-text/>\n" +
                "            <alarms:last-alarm-condition>ALARM_ON</alarms:last-alarm-condition>\n" +
                "        </alarms:alarm>\n" +
                "    </alarms:alarm-notification>\n" +
                "    <a>\n" +
                "        <b>\n" +
                "            <c>xxx</c>\n" +
                "        </b>\n" +
                "    </a>\n" +
                "</notification>";

        Document document = DocumentUtils.stringToDocument(notification);

        // match
        String xPath = "/notification/alarm-notification/alarm/last-perceived-severity='critical'";
        assertTrue(JXPathUtils.isNotificationMatchesXpath(document, xPath, JXPathUtils.buildExpressionFromXpath(xPath)));

        xPath = "/notification/alarm-notification/alarm/last-perceived-severity='critical'"
                + "|"
                + "/notification/alarm-notification/alarm/last-alarm-condition='ALARM_ON'";
        assertTrue(JXPathUtils.isNotificationMatchesXpath(document, xPath, JXPathUtils.buildExpressionFromXpath(xPath)));

        // match with 2nd filter
        xPath = "/notification/alarm-notification/alarm/last-perceived-severity='minor'"
                + "|"
                + "/notification/alarm-notification/alarm/last-alarm-condition='ALARM_ON'";
        assertTrue(JXPathUtils.isNotificationMatchesXpath(document, xPath, JXPathUtils.buildExpressionFromXpath(xPath)));

        xPath = "/notification/alarm-notification/alarm/last-perceived-severity='minor'"
                + "|/notification/alarm-notification/alarm/last-alarm-condition='ALARM_OFF'"
                + "|/notification/a/b/c='xxx'";
        assertTrue(JXPathUtils.isNotificationMatchesXpath(document, xPath, JXPathUtils.buildExpressionFromXpath(xPath)));

        // Does not match
        xPath = "/notification/alarm-notification/alarm/last-perceived-severity='minor'";
        assertFalse(JXPathUtils.isNotificationMatchesXpath(document, xPath, JXPathUtils.buildExpressionFromXpath(xPath)));

        xPath = "/notification/alarm-notification/alarm/last-perceived-severity='minor'"
                + "|"
                + "/notification/alarm-notification/alarm/last-alarm-condition='ALARM_OFF'";
        assertFalse(JXPathUtils.isNotificationMatchesXpath(document, xPath, JXPathUtils.buildExpressionFromXpath(xPath)));

        // 3 filters
        xPath = "/notification/alarm-notification/alarm/last-perceived-severity='minor'"
                + "|/notification/alarm-notification/alarm/last-alarm-condition='ALARM_OFF'"
                + "|/notification/a/b/c='abc'";
        assertFalse(JXPathUtils.isNotificationMatchesXpath(document, xPath, JXPathUtils.buildExpressionFromXpath(xPath)));

        xPath = "|";
        assertFalse(JXPathUtils.isNotificationMatchesXpath(document, xPath, JXPathUtils.buildExpressionFromXpath(xPath)));
    }
}
