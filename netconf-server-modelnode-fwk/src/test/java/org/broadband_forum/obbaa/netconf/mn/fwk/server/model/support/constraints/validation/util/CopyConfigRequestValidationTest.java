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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util;

import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.transformToElement;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.xmlToString;

import org.broadband_forum.obbaa.netconf.api.messages.CopyConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigDefaultOperations;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigElement;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigTestOptions;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.validation.AbstractSchemaMountTest;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Element;

@RunWith(RequestScopeJunitRunner.class)
public class CopyConfigRequestValidationTest extends AbstractSchemaMountTest {

    private static final String CONFIG_ELEMENT_START = "<config>";
    private static final String CONFIG_ELEMENT_START_EDIT_CONFIG = "<config xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">";
    private static final String CONFIG_ELEMENT_END = "</config>";

    @Test
    public void testCopyConfigRequest_removeDefaultLeafValuesUnderContainer() throws Exception {

        String configElement =
                    "  <config>" +
                        "   <copy-config-container xmlns=\"copy-config-test\">" +
                        "    <container1 >" +
                        "		<default-leaf>enabled</default-leaf>" +
                        "		<type>vlan</type>" +
                        "       <inner-container>" +
                        "           <default-leaf1>10</default-leaf1>" +
                        "       </inner-container>" +
                        "       <typedef-leaf>abc</typedef-leaf>" +
                        "    </container1>" +
                        "   </copy-config-container>" +
                        " </config>";

        String expectedChildElements =
                        "   <copy-config-container xmlns=\"copy-config-test\">" +
                        "    <container1 >" +
                        "		<type>vlan</type>" +
                        "       <typedef-leaf>abc</typedef-leaf>" +
                        "    </container1>" +
                        "   </copy-config-container>";

        // Validate expected result after removing default leaf values
        validateRemovedDefaultValuesRequest(configElement, expectedChildElements);

        configElement =
                "  <config>" +
                        "   <copy-config-container xmlns=\"copy-config-test\">" +
                        "    <container1>" +
                        "		<default-leaf>disabled</default-leaf>" +
                        "		<type>gfast</type>" +
                        "       <inner-container>" +
                        "           <default-leaf1>12</default-leaf1>" +
                        "       </inner-container>" +
                        "       <typedef-leaf>xyz</typedef-leaf>" +
                        "    </container1>" +
                        "   </copy-config-container>" +
                        " </config>";

        // should not remove the element if LEAF element has non-default values
        expectedChildElements =
                        "   <copy-config-container xmlns=\"copy-config-test\">" +
                        "    <container1>" +
                        "		<default-leaf>disabled</default-leaf>" +
                        "		<type>gfast</type>" +
                        "       <inner-container>" +
                        "           <default-leaf1>12</default-leaf1>" +
                        "       </inner-container>" +
                        "    </container1>" +
                        "   </copy-config-container>" ;

        validateRemovedDefaultValuesRequest(configElement, expectedChildElements);
    }

    @Test
    public void testCopyConfigRequest_removeDefaultLeafValuesUnderList() throws Exception {
        String configElement =
                "  <config>" +
                        "   <copy-config-container xmlns=\"copy-config-test\">" +
                        "    <list1>" +
                        "		<name>name1</name>" +
                        "		<default-leaf2>list1</default-leaf2>" +
                        "    </list1>" +
                        "    <list1>" +
                        "		<name>name2</name>" +
                        "		<default-leaf2>list2</default-leaf2>" +
                        "    </list1>" +
                        "   </copy-config-container>" +
                        " </config>";

        // remove the element if LEAF element has default values under LIST
        String expectedChildElements =
                        "   <copy-config-container xmlns=\"copy-config-test\">" +
                        "    <list1>" +
                        "		<name>name1</name>" +
                        "    </list1>" +
                        "    <list1>" +
                        "		<name>name2</name>" +
                        "		<default-leaf2>list2</default-leaf2>" +
                        "    </list1>" +
                        "   </copy-config-container>" ;

        validateRemovedDefaultValuesRequest(configElement, expectedChildElements);
    }

    @Test
    public void testCopyConfigRequest_removeDefaultLeafValuesUnderChoiceCase() throws Exception {
        String configElement =
                "  <config>" +
                        "   <copy-config-container xmlns=\"copy-config-test\">" +
                        "    <choice-container>" +
                        "		<case1-leaf2>case1</case1-leaf2>" +
                        "    </choice-container>" +
                        "   </copy-config-container>" +
                        " </config>";

        String expectChildElements = "<copy-config-container xmlns=\"copy-config-test\"/>" ;
        // remove non presence container elements and default-value LEAF element AND dont remove top-level root container
        validateRemovedDefaultValuesRequest(configElement, expectChildElements);
    }

    @Test
    public void testCopyConfigRequest_DontremoveDefaultKeyLeafValuesinList() throws Exception {
        String configElement =
                "  <config>" +
                        "   <copy-config-container xmlns=\"copy-config-test\">" +
                        "    <default-key-list>" +
                        "		<name>Bbf</name>" +
                        "		<name2>name2</name2>" +
                        "    </default-key-list>" +
                        "   </copy-config-container>" +
                        " </config>";

        String expectedChildElements =
                "   <copy-config-container xmlns=\"copy-config-test\">" +
                        "    <default-key-list>" +
                        "		<name>Bbf</name>" +
                        "		<name2>name2</name2>" +
                        "    </default-key-list>" +
                        "   </copy-config-container>" ;

        // Dont remove key leaf if it is default-leaf
        validateRemovedDefaultValuesRequest(configElement, expectedChildElements);
    }

    @Test
    public void testCopyConfigRequest_removeDefaultLeafValuesUnderPresenceContainer() throws Exception {
        String configElement =
                "  <config>" +
                        "   <copy-config-container xmlns=\"copy-config-test\">" +
                        "    <container2>" +
                        "		<default-leaf>presence</default-leaf>" +
                        "    </container2>" +
                        "   </copy-config-container>" +
                        " </config>";

        // Don't remove presence container elements even-though all  default value children are removed
        String expectedChildElements =
                        "   <copy-config-container xmlns=\"copy-config-test\">" +
                        "    <container2/>" +
                        "   </copy-config-container>" ;

        validateRemovedDefaultValuesRequest(configElement, expectedChildElements);
    }

    @Test
    public void testCopyConfigRequest_removeDefaultLeafValues() throws Exception {

        String configElement =
                "  <config>" +
                        "   <copy-config-container xmlns=\"copy-config-test\">" +
                        "    <container1 >" +
                        "		<default-leaf>enabled</default-leaf>" +
                        "		<type>vlan</type>" +
                        "       <inner-container>" +
                        "           <default-leaf1>10</default-leaf1>" +
                        "       </inner-container>" +
                        "       <typedef-leaf>abc</typedef-leaf>" +
                        "    </container1>" +
                        "    <container2>" +
                        "		<default-leaf>presence</default-leaf>" +
                        "    </container2>" +
                        "    <list1>" +
                        "		<name>name1</name>" +
                        "		<default-leaf2>list1</default-leaf2>" +
                        "    </list1>" +
                        "    <choice-container>" +
                        "		<case1-leaf2>case1</case1-leaf2>" +
                        "    </choice-container>" +
                        "   </copy-config-container>" +
                        "   <schemaMount xmlns=\"schema-mount-test\"/>" +
                        "   <choice-case-container xmlns=\"schema-mount-test\">" +
                        "     <outer-default-leaf>one</outer-default-leaf>" +
                        "     <outer-leaf>leaf1</outer-leaf>" +
                        "   </choice-case-container>" +
                        " </config>";

        //Removed default values and non-presence container (which does not have any children) from copy-config
        String expectedChildElements =
                        "   <copy-config-container xmlns=\"copy-config-test\">" +
                        "    <container1 >" +
                        "		<type>vlan</type>" +
                        "       <typedef-leaf>abc</typedef-leaf>" +
                        "    </container1>" +
                        "    <container2>" +
                        "    </container2>" +
                        "    <list1>" +
                        "		<name>name1</name>" +
                        "    </list1>" +
                        "   </copy-config-container>" +
                        "   <choice-case-container xmlns=\"schema-mount-test\">" +
                        "     <outer-leaf>leaf1</outer-leaf>" +
                        "   </choice-case-container>" +
                        "   <schemaMount xmlns=\"schema-mount-test\"/>" ;

        validateRemovedDefaultValuesRequest(configElement, expectedChildElements);
    }

    private void validateRemovedDefaultValuesRequest(String configElement, String expectedConfigElemet) throws Exception {
        // validate copy-config request
        CopyConfigRequest copyConfigRequest = createCopyConfigRequest(configElement);
        Element removedDefaultValuesConfig = DataStoreValidationUtil.removeDefaultLeafValueFromXmlConfig(m_mountRegistry, copyConfigRequest.getSourceConfigElement());
        Assert.assertTrue(TestUtil.assertXMLStringEquals(appendConfigElement(expectedConfigElemet),
                xmlToString(removedDefaultValuesConfig)));

        // validate edit-config request
        EditConfigRequest editConfigRequest = createEditConfigRequest(configElement);
        removedDefaultValuesConfig = DataStoreValidationUtil.removeDefaultLeafValueFromXmlConfig(m_mountRegistry,
                editConfigRequest.getConfigElement().getXmlElement());
        Assert.assertTrue(TestUtil.assertXMLStringEquals(appendConfigElementWithNameSpace(expectedConfigElemet),
                xmlToString(removedDefaultValuesConfig)));
    }

    private String appendConfigElement(String configElementStr){
        return CONFIG_ELEMENT_START + configElementStr + CONFIG_ELEMENT_END;
    }

    private String appendConfigElementWithNameSpace(String configElementStr){
        return CONFIG_ELEMENT_START_EDIT_CONFIG + configElementStr + CONFIG_ELEMENT_END;
    }

    protected CopyConfigRequest createCopyConfigRequest(String configElement) {
        CopyConfigRequest copyConfigRequest = new CopyConfigRequest();
        copyConfigRequest.setSourceConfigElement(transformToElement(configElement));
        copyConfigRequest.setTargetRunning();
        return copyConfigRequest;
    }

    protected EditConfigRequest createEditConfigRequest(String configElementStr) {
        EditConfigRequest editConfigRequest = new EditConfigRequest();
        editConfigRequest.setDefaultOperation(EditConfigDefaultOperations.REPLACE);
        editConfigRequest.setTargetRunning();
        editConfigRequest.setTestOption(EditConfigTestOptions.SET);
        EditConfigElement configElement = new EditConfigElement().setConfigElementContents(
                DocumentUtils.getChildElements(transformToElement(configElementStr)));
        editConfigRequest.setConfigElement(configElement);
        return editConfigRequest;
    }
}
