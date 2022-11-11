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

package org.broadband_forum.obbaa.netconf.api.validation.xsd;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.xml.transform.dom.DOMSource;

import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

public class XmlValidatorEditConfigTest {

    @Test
    public void testEditConfigVerifyTargetIsMandatoryChildElement() throws Exception {
        String requestString =
                "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"6\">\n"
                        + "<edit-config>\n"
                        + "  <config/>\n"
                        + "</edit-config>\n"
                        + "</rpc>";

        String expectedErrorMessage = "cvc-complex-type.2.4.b: The content of element 'edit-config' is not complete. One of '{\"urn:ietf:params:xml:ns:netconf:base:1.0\":target, \"urn:ietf:params:xml:ns:netconf:base:1.0\":default-operation, \"urn:ietf:params:xml:ns:netconf:base:1.0\":test-option, \"urn:ietf:params:xml:ns:netconf:base:1.0\":error-option, \"http://tail-f.com/ns/netconf/with-transaction-id\":with-transaction-id, \"http://www.test-company.com/solutions/netconf-extensions\":trigger-sync-upon-success, \"http://www.test-company.com/solutions/netconf-extensions\":force-instance-creation}' is expected.";
        try {
            XmlValidator.validateXmlMessage(new DOMSource(DocumentUtils.stringToDocument(requestString)));
            fail("xsd validation should throw exception");
        } catch (SAXException e) {
            assertEquals(expectedErrorMessage, e.getMessage());
        }
    }

    @Test
    public void testEditConfigVerifyTargetShouldHaveChildElement() throws Exception {
        String requestString =
                "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"\">\n" +
                        "    <edit-config>\n" +
                        "        <target>\n" +
                        "        </target>\n" +
                        "        <config/>\n" +
                        "    </edit-config>\n" +
                        "</rpc>";

        String expectedErrorMessage = "cvc-complex-type.2.4.b: The content of element 'target' is not complete." +
                " One of '{\"urn:ietf:params:xml:ns:netconf:base:1.0\":candidate, \"urn:ietf:params:xml:ns:netconf:base:1.0\":running}' is expected.";
        try {
            XmlValidator.validateXmlMessage(new DOMSource(DocumentUtils.stringToDocument(requestString)));
            fail("xsd validation should throw exception");
        } catch (SAXException e) {
            assertEquals(expectedErrorMessage, e.getMessage());
        }
    }


    @Test
    public void testEditConfigWithTxID() throws Exception {
        String requestString =
                "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"14\">\n" +
                        "<edit-config>\n" +
                        "<target>\n" +
                        "<running/>\n" +
                        "</target>\n" +
                        "<default-operation>merge</default-operation>\n" +
                        "<test-option>set</test-option>\n" +
                        "<error-option>stop-on-error</error-option>\n" +
                        "<with-transaction-id xmlns=\"http://tail-f.com/ns/netconf/with-transaction-id\"/>\n" +
                        "<config/>\n" +
                        "</edit-config>\n" +
                        "</rpc>\n";
        XmlValidator.validateXmlMessage(new DOMSource(DocumentUtils.stringToDocument(requestString)));
    }

    @Test
    public void testEditConfigWithTxIDFailForNonEmptyValue() throws NetconfMessageBuilderException {
        String requestString =
                "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"14\">\n" +
                        "<edit-config>\n" +
                        "<target>\n" +
                        "<running/>\n" +
                        "</target>\n" +
                        "<default-operation>merge</default-operation>\n" +
                        "<test-option>set</test-option>\n" +
                        "<error-option>stop-on-error</error-option>\n" +
                        "<with-transaction-id xmlns=\"http://tail-f.com/ns/netconf/with-transaction-id\">abc</with-transaction-id>\n"+
                        "<config/>\n" +
                        "</edit-config>\n" +
                        "</rpc>\n";
        try {
            XmlValidator.validateXmlMessage(new DOMSource(DocumentUtils.stringToDocument(requestString)));
            fail("Expected SAXParseException");
        } catch (SAXException | IOException e) {
            assertEquals("cvc-complex-type.2.1: Element 'with-transaction-id' must have no character or element information item [children], because the type's content type is empty.",
                    e.getMessage());
        }
    }

    @Test
    public void testEditConfigWithTxIDFailForInvalidNamespace() throws NetconfMessageBuilderException {
        String requestString =
                "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"14\">\n" +
                        "<edit-config>\n" +
                        "<target>\n" +
                        "<running/>\n" +
                        "</target>\n" +
                        "<default-operation>merge</default-operation>\n" +
                        "<test-option>set</test-option>\n" +
                        "<error-option>stop-on-error</error-option>\n" +
                        "<with-transaction-id xmlns=\"http://tail-f.com/netconf/with-transaction-id\"/>\n"+
                        "<config/>\n" +
                        "</edit-config>\n" +
                        "</rpc>\n";
        try {
            XmlValidator.validateXmlMessage(new DOMSource(DocumentUtils.stringToDocument(requestString)));
            fail("Expected SAXParseException");
        } catch (SAXException | IOException e) {
            assertEquals("cvc-complex-type.2.4.a: Invalid content was found starting with element 'with-transaction-id'. One of '{\"http://tail-f.com/ns/netconf/with-transaction-id\":with-transaction-id, \"http://www.test-company.com/solutions/netconf-extensions\":trigger-sync-upon-success, \"http://www.test-company.com/solutions/netconf-extensions\":force-instance-creation, \"urn:ietf:params:xml:ns:netconf:base:1.0\":config}' is expected.",
                    e.getMessage());
        }
    }

    @Test
    public void testEditConfigVerifyRunningIsValidChildForTarget() throws Exception {
        String requestString =
                "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"\">\n" +
                        "    <edit-config>\n" +
                        "        <target>\n" +
                        "            <running/>\n" +
                        "        </target>\n" +
                        "        <config/>\n" +
                        "    </edit-config>\n" +
                        "</rpc>";
        XmlValidator.validateXmlMessage(new DOMSource(DocumentUtils.stringToDocument(requestString)));
    }

    @Test
    public void testEditConfigVerifyForTargetRunningIsValidChildWithEmptyValue() throws Exception {
        String requestString =
                "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"\">\n" +
                        "    <edit-config>\n" +
                        "        <target>\n" +
                        "            <running></running>\n" +
                        "        </target>\n" +
                        "        <config/>\n" +
                        "    </edit-config>\n" +
                        "</rpc>";
        XmlValidator.validateXmlMessage(new DOMSource(DocumentUtils.stringToDocument(requestString)));
    }

    @Test
    public void testEditConfigVerifyCandidateIsValidChildForTarget() throws Exception {
        String requestString =
                "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"\">\n" +
                        "    <edit-config>\n" +
                        "        <target>\n" +
                        "            <candidate/>\n" +
                        "        </target>\n" +
                        "        <config/>\n" +
                        "    </edit-config>\n" +
                        "</rpc>";
        XmlValidator.validateXmlMessage(new DOMSource(DocumentUtils.stringToDocument(requestString)));
    }

    @Test
    public void testEditConfigVerifyForTargetCandidateIsValidChildWithEmptyValue() throws Exception {
        String requestString =
                "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"\">\n" +
                        "    <edit-config>\n" +
                        "        <target>\n" +
                        "            <candidate></candidate>\n" +
                        "        </target>\n" +
                        "        <config/>\n" +
                        "    </edit-config>\n" +
                        "</rpc>";
        XmlValidator.validateXmlMessage(new DOMSource(DocumentUtils.stringToDocument(requestString)));
    }

    @Test
    public void testEditConfigTargetVerifyWeIgnoreCandidateTagWithValue() throws Exception {
        String requestString =
                "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"\">\n" +
                        "    <edit-config>\n" +
                        "        <target>\n" +
                        "            <candidate>\n" +
                        "                <device/>\n" +
                        "            </candidate>\n" +
                        "        </target>\n" +
                        "        <config/>\n" +
                        "    </edit-config>\n" +
                        "</rpc>";
        XmlValidator.validateXmlMessage(new DOMSource(DocumentUtils.stringToDocument(requestString)));
    }

    @Test
    public void test_editConfigTargetVerifyWeIgnoreRunningTagWithValue() throws Exception {
        String requestString =
                "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"\">\n" +
                        "    <edit-config>\n" +
                        "        <target>\n" +
                        "            <running>\n" +
                        "                <device/>\n" +
                        "            </running>\n" +
                        "        </target>\n" +
                        "        <config/>\n" +
                        "    </edit-config>\n" +
                        "</rpc>";
        XmlValidator.validateXmlMessage(new DOMSource(DocumentUtils.stringToDocument(requestString)));
    }

    @Test
    public void testEditConfigVerifyTargetAllowOnlyCandidateOrRunningElements() throws Exception {
        String requestString =
                "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"\">\n" +
                        "    <edit-config>\n" +
                        "        <target>\n" +
                        "            <url>\n" +
                        "            </url>\n" +
                        "        </target>\n" +
                        "        <config/>\n" +
                        "    </edit-config>\n" +
                        "</rpc>";

        String expectedErrorMessage = "cvc-complex-type.2.4.a: Invalid content was found starting with element 'url'." +
                " One of '{\"urn:ietf:params:xml:ns:netconf:base:1.0\":candidate, \"urn:ietf:params:xml:ns:netconf:base:1.0\":running}' is expected.";
        try {
            XmlValidator.validateXmlMessage(new DOMSource(DocumentUtils.stringToDocument(requestString)));
            fail("xsd validation should throw exception");
        } catch (SAXException e) {
            assertEquals(expectedErrorMessage, e.getMessage());
        }
    }

    @Test
    public void testEditConfigVerifyDefaultOperationCannotBeEmpty() throws Exception {
        String requestString =
                "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"\">\n" +
                        "    <edit-config>\n" +
                        "        <target>\n" +
                        "            <candidate/>\n" +
                        "        </target>\n" +
                        "        <default-operation></default-operation>\n" +
                        "        <config/>\n" +
                        "    </edit-config>\n" +
                        "</rpc>";

        String expectedErrorMessage = "cvc-enumeration-valid: Value '' is not facet-valid with respect to enumeration '[merge, replace, none]'." +
                " It must be a value from the enumeration.";
        try {
            XmlValidator.validateXmlMessage(new DOMSource(DocumentUtils.stringToDocument(requestString)));
            fail("xsd validation should throw exception");
        } catch (SAXException e) {
            Assert.assertEquals(expectedErrorMessage, e.getMessage());
        }
    }

    @Test
    public void testEditConfigVerifyForDefaultOperationMergeIsValidValue() throws Exception {
        String requestString =
                "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"\">\n" +
                        "    <edit-config>\n" +
                        "        <target>\n" +
                        "            <candidate/>\n" +
                        "        </target>\n" +
                        "        <default-operation>merge</default-operation>\n" +
                        "        <config/>\n" +
                        "    </edit-config>\n" +
                        "</rpc>";
        XmlValidator.validateXmlMessage(new DOMSource(DocumentUtils.stringToDocument(requestString)));
    }

    @Test
    public void testEditConfigVerifyForDefaultOperationNoneIsValidValue() throws Exception {
        String requestString =
                "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"\">\n" +
                        "    <edit-config>\n" +
                        "        <target>\n" +
                        "            <candidate/>\n" +
                        "        </target>\n" +
                        "        <default-operation>none</default-operation>\n" +
                        "        <config/>\n" +
                        "    </edit-config>\n" +
                        "</rpc>";
        XmlValidator.validateXmlMessage(new DOMSource(DocumentUtils.stringToDocument(requestString)));
    }

    @Test
    public void testEditConfigVerifyForDefaultOperationReplaceIsValidValue() throws Exception {
        String requestString =
                "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"\">\n" +
                        "    <edit-config>\n" +
                        "        <target>\n" +
                        "            <candidate/>\n" +
                        "        </target>\n" +
                        "        <default-operation>replace</default-operation>\n" +
                        "        <config/>\n" +
                        "    </edit-config>\n" +
                        "</rpc>";
        XmlValidator.validateXmlMessage(new DOMSource(DocumentUtils.stringToDocument(requestString)));
    }

    @Test
    public void testEditConfigVerifyForDefaultOperationInvalidValuesAreNotAllowed() throws Exception {
        String requestString =
                "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"\">\n" +
                        "    <edit-config>\n" +
                        "        <target>\n" +
                        "            <candidate/>\n" +
                        "        </target>\n" +
                        "        <default-operation>add</default-operation>\n" +
                        "        <config/>\n" +
                        "    </edit-config>\n" +
                        "</rpc>";

        String expectedErrorMessage = "cvc-enumeration-valid: Value 'add' is not facet-valid with respect to enumeration '[merge, replace, none]'." +
                " It must be a value from the enumeration.";
        try {
            XmlValidator.validateXmlMessage(new DOMSource(DocumentUtils.stringToDocument(requestString)));
            fail("xsd validation should throw exception");
        } catch (SAXException e) {
            assertEquals(expectedErrorMessage, e.getMessage());
        }
    }

    @Test
    public void testEditConfigVerifyTestOptionCannotBeEmpty() throws Exception {
        String requestString =
                "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"\">\n" +
                        "    <edit-config>\n" +
                        "        <target>\n" +
                        "            <candidate/>\n" +
                        "        </target>\n" +
                        "        <test-option></test-option>\n" +
                        "        <config/>\n" +
                        "    </edit-config>\n" +
                        "</rpc>";

        String expectedErrorMessage = "cvc-enumeration-valid: Value '' is not facet-valid with respect to enumeration '[test-then-set, set, test-only]'." +
                " It must be a value from the enumeration.";
        try {
            XmlValidator.validateXmlMessage(new DOMSource(DocumentUtils.stringToDocument(requestString)));
            fail("xsd validation should throw exception");
        } catch (SAXException e) {
            assertEquals(expectedErrorMessage, e.getMessage());
        }
    }

    @Test
    public void testEditConfigVerifyForTestOptionInvalidValueIsNotAllowed() throws Exception {
        String requestString =
                "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"\">\n" +
                        "    <edit-config>\n" +
                        "        <target>\n" +
                        "            <candidate/>\n" +
                        "        </target>\n" +
                        "        <test-option>device</test-option>\n" +
                        "        <config/>\n" +
                        "    </edit-config>\n" +
                        "</rpc>";

        String expectedErrorMessage = "cvc-enumeration-valid: Value 'device' is not facet-valid with respect to enumeration '[test-then-set, set, test-only]'." +
                " It must be a value from the enumeration.";
        try {
            XmlValidator.validateXmlMessage(new DOMSource(DocumentUtils.stringToDocument(requestString)));
            fail("xsd validation should throw exception");
        } catch (SAXException e) {
            assertEquals(expectedErrorMessage, e.getMessage());
        }
    }

    @Test
    public void testEditConfigVerifyForTestOptionTestThenSetIsValidValue() throws Exception {
        String requestString =
                "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"\">\n" +
                        "    <edit-config>\n" +
                        "        <target>\n" +
                        "            <candidate/>\n" +
                        "        </target>\n" +
                        "        <test-option>test-then-set</test-option>\n" +
                        "        <config/>\n" +
                        "    </edit-config>\n" +
                        "</rpc>";
        XmlValidator.validateXmlMessage(new DOMSource(DocumentUtils.stringToDocument(requestString)));
    }

    @Test
    public void testEditConfigVerifyTestOptionSetIsValidValue() throws Exception {
        String requestString =
                "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"\">\n" +
                        "    <edit-config>\n" +
                        "        <target>\n" +
                        "            <candidate/>\n" +
                        "        </target>\n" +
                        "        <test-option>set</test-option>\n" +
                        "        <config/>\n" +
                        "    </edit-config>\n" +
                        "</rpc>";
        XmlValidator.validateXmlMessage(new DOMSource(DocumentUtils.stringToDocument(requestString)));
    }

    @Test
    public void testEditConfigVerifyTestOptionTestOnlyIsValidValue() throws Exception {
        String requestString =
                "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"\">\n" +
                        "    <edit-config>\n" +
                        "        <target>\n" +
                        "            <candidate/>\n" +
                        "        </target>\n" +
                        "        <test-option>test-only</test-option>\n" +
                        "        <config/>\n" +
                        "    </edit-config>\n" +
                        "</rpc>";
        XmlValidator.validateXmlMessage(new DOMSource(DocumentUtils.stringToDocument(requestString)));
    }

    @Test
    public void test_editConfigVerifyErrorOptionCannotBeEmpty() throws Exception {
        String requestString =
                "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"\">\n" +
                        "    <edit-config>\n" +
                        "        <target>\n" +
                        "            <candidate/>\n" +
                        "        </target>\n" +
                        "        <error-option></error-option>\n" +
                        "        <config/>\n" +
                        "    </edit-config>\n" +
                        "</rpc>";

        String expectedErrorMessage = "cvc-enumeration-valid: Value '' is not facet-valid with respect to enumeration '[stop-on-error, continue-on-error, rollback-on-error]'." +
                " It must be a value from the enumeration.";
        try {
            XmlValidator.validateXmlMessage(new DOMSource(DocumentUtils.stringToDocument(requestString)));
            fail("xsd validation should throw exception");
        } catch (SAXException e) {
            assertEquals(expectedErrorMessage, e.getMessage());
        }
    }

    @Test
    public void testEditConfigVerifyForErrorOptionStopOnErrorIsValidValue() throws Exception {
        String requestString =
                "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"\">\n" +
                        "    <edit-config>\n" +
                        "        <target>\n" +
                        "            <candidate/>\n" +
                        "        </target>\n" +
                        "        <error-option>stop-on-error</error-option>\n" +
                        "        <config/>\n" +
                        "    </edit-config>\n" +
                        "</rpc>";
        XmlValidator.validateXmlMessage(new DOMSource(DocumentUtils.stringToDocument(requestString)));
    }

    @Test
    public void testEditConfigVerifyForErrorOptionRollbackOnErrorIsValidValue() throws Exception {
        String requestString =
                "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"\">\n" +
                        "    <edit-config>\n" +
                        "        <target>\n" +
                        "            <candidate/>\n" +
                        "        </target>\n" +
                        "        <error-option>rollback-on-error</error-option>\n" +
                        "        <config/>\n" +
                        "    </edit-config>\n" +
                        "</rpc>";
        XmlValidator.validateXmlMessage(new DOMSource(DocumentUtils.stringToDocument(requestString)));
    }

    @Test
    public void testEditConfigVerifyForErrorOptionContinueOnErrorIsValidValue() throws Exception {
        String requestString =
                "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"\">\n" +
                        "    <edit-config>\n" +
                        "        <target>\n" +
                        "            <candidate/>\n" +
                        "        </target>\n" +
                        "        <error-option>continue-on-error</error-option>\n" +
                        "        <config/>\n" +
                        "    </edit-config>\n" +
                        "</rpc>";
        XmlValidator.validateXmlMessage(new DOMSource(DocumentUtils.stringToDocument(requestString)));
    }

    @Test
    public void testEditConfigVerifyForErrorOptionInvalidValueIsNotAllowed() throws Exception {
        String requestString =
                "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"\">\n" +
                        "    <edit-config>\n" +
                        "        <target>\n" +
                        "            <candidate/>\n" +
                        "        </target>\n" +
                        "        <error-option>device</error-option>\n" +
                        "        <config/>\n" +
                        "    </edit-config>\n" +
                        "</rpc>";

        String expectedErrorMessage = "cvc-enumeration-valid: Value 'device' is not facet-valid with respect to enumeration '[stop-on-error, continue-on-error, rollback-on-error]'." +
                " It must be a value from the enumeration.";
        try {
            XmlValidator.validateXmlMessage(new DOMSource(DocumentUtils.stringToDocument(requestString)));
            fail("xsd validation should throw exception");
        } catch (SAXException e) {
            assertEquals(expectedErrorMessage, e.getMessage());
        }
    }

    @Test
    public void testEditConfigVerifyForConfigAnyXmlIsAllowed() throws Exception {
        String requestString =
                "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"\">\n" +
                        "    <edit-config>\n" +
                        "        <target>\n" +
                        "            <candidate/>\n" +
                        "        </target>\n" +
                        "        <config>\n" +
                        "        <device/>\n" +
                        "        </config>\n" +
                        "    </edit-config>\n" +
                        "</rpc>";
        XmlValidator.validateXmlMessage(new DOMSource(DocumentUtils.stringToDocument(requestString)));
    }

    @Test
    public void testEditConfigMoreThanOneConfigElementsAreNotAllowed() throws Exception {
        String requestString =
                "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"\">\n" +
                        "    <edit-config>\n" +
                        "        <target>\n" +
                        "            <candidate/>\n" +
                        "        </target>\n" +
                        "        <config>\n" +
                        "        <device/>\n" +
                        "        </config>\n" +
                        "        <config/>\n" +
                        "    </edit-config>\n" +
                        "</rpc>";

        String expectedErrorMessage = "cvc-complex-type.2.4.a: Invalid content was found starting with element 'config'. One of '{\"urn:ietf:params:xml:ns:netconf:base:1.0\":default-operation, \"urn:ietf:params:xml:ns:netconf:base:1.0\":test-option, \"urn:ietf:params:xml:ns:netconf:base:1.0\":error-option, \"http://tail-f.com/ns/netconf/with-transaction-id\":with-transaction-id, \"http://www.test-company.com/solutions/netconf-extensions\":trigger-sync-upon-success, \"http://www.test-company.com/solutions/netconf-extensions\":force-instance-creation}' is expected.";
        try {
            XmlValidator.validateXmlMessage(new DOMSource(DocumentUtils.stringToDocument(requestString)));
            fail("xsd validation should throw exception");
        } catch (SAXException e) {
            assertEquals(expectedErrorMessage, e.getMessage());
        }
    }

    @Test
    public void testEditConfigVerifyValidRequestIsPassingXsdValidation() throws Exception {
        String requestString =
                "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
                        "  <edit-config>\n" +
                        "    <target>\n" +
                        "      <running/>\n" +
                        "    </target>\n" +
                        "    <test-option>set</test-option>\n" +
                        "    <config>\n" +
                        "      <pma:pma xmlns:pma=\"urn:org:bbf:pma\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" xc:operation=\"\">\n" +
                        "        <pma:device-holder>\n" +
                        "          <pma:name>OLT1</pma:name>\n" +
                        "          <pma:device>\n" +
                        "            <pma:connection-initiator>pma</pma:connection-initiator>\n" +
                        "            <pma:configured-device-properties>\n" +
                        "              <pma:ip-address>135.2.2.2</pma:ip-address>\n" +
                        "              <pma:ip-port>9291</pma:ip-port>\n" +
                        "              <pma:transport-protocol>ssh</pma:transport-protocol>\n" +
                        "              <pma:pma-authentication-method>username-and-password</pma:pma-authentication-method>\n" +
                        "              <pma:username>user</pma:username>\n" +
                        "              <pma:password>password</pma:password>\n" +
                        "            </pma:configured-device-properties>\n" +
                        "          </pma:device>\n" +
                        "        </pma:device-holder>\n" +
                        "      </pma:pma>\n" +
                        "    </config>\n" +
                        "  </edit-config>\n" +
                        "</rpc>";
        XmlValidator.validateXmlMessage(new DOMSource(DocumentUtils.stringToDocument(requestString)));
    }

    @Test
    public void testEditConfigVerifyValidRequestWithRpcInConfigElementIsPassingXsdValidation() throws Exception {
        String requestString =
                "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
                        "  <edit-config>\n" +
                        "    <target>\n" +
                        "      <running/>\n" +
                        "    </target>\n" +
                        "    <test-option>set</test-option>\n" +
                        "    <config>\n" +
                        "      <rpc/>\n" +
                        "    </config>\n" +
                        "  </edit-config>\n" +
                        "</rpc>";
        XmlValidator.validateXmlMessage(new DOMSource(DocumentUtils.stringToDocument(requestString)));
    }


    @Test
    public void testEditConfigTargetVerifyRunningWthSpaceAndNotFormattedIsAllowed() throws Exception {
        //don't format below xml, even format matter for validation
        String requestString =
                "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"328\"><edit-config> <target> <running> </running> </target> <test-option>set</test-option> <error-option>rollback-on-error</error-option> <config> <ibn xmlns=\"http://www.test-company.com/solutions/ibn\"> <intent xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" xc:operation=\"merge\"> <target>DPU2</target> <intent-type>device-configuration</intent-type> <configuration> <device-configuration xmlns=\"http://www.test-company.com/solutions/device-configuration\"> <label> <category>State</category> <value>TN</value> </label> <push-nav-configuration-to-device>true</push-nav-configuration-to-device> <duid>DPU2</duid> <av-name>automation-test-av</av-name> <hardware-type>DPU-CFAS-H</hardware-type> <device-version>19A.03</device-version> <device-template>DPU-CFAS-H-19A.03_default</device-template> </device-configuration> </configuration> <intent-type-version>1</intent-type-version> <required-network-state>active</required-network-state> </intent> </ibn> </config> </edit-config></rpc>";
        try {
            XmlValidator.validateXmlMessage(new DOMSource(DocumentUtils.stringToDocument(requestString)));
        } catch (SAXException e) {
            fail("xsd validation should not throw error, but got error: " + e.getMessage());
        }
    }
}