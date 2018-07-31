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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;
import org.junit.Test;

import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorSeverity;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorType;

public class ContainerDSValidatorTest extends AbstractDataStoreValidatorTest {


    //	@Test
    public void testViolateWhenConstrant() throws ModelNodeInitException {
        testFail("/datastorevalidatortest/rangevalidation/defaultxml/invalid-when-constraint-container.xml",
                NetconfRpcErrorTag.UNKNOWN_ELEMENT,
                NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error, "when-violation",
                "Violate when constraints: ../result-container >= 10 and ../result-container <= 20",
                "/validation/when-validation/container-type");
    }

    //	@Test
    public void testViolateWhenConstraintForContainerChoiceCaseNode() throws ModelNodeInitException {
        // ChoiceCaseNode
        testFail("/datastorevalidatortest/rangevalidation/defaultxml/invalid-when-constraint-container-casenode.xml",
                NetconfRpcErrorTag.UNKNOWN_ELEMENT,
                NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error, "when-violation",
                "Violate when constraints: validation:result-choice = 'success'",
                "/validation/when-validation/choicecase/container-case-success");

        // ChoiceNode
        testFail("/datastorevalidatortest/rangevalidation/defaultxml/invalid-when-constraint-container-choicenode" +
                        ".xml", NetconfRpcErrorTag.UNKNOWN_ELEMENT,
                NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error, "when-violation",
                "Violate when constraints: validation:data-choice > 100",
                "/validation/when-validation/choicecase/container-case-success");
    }

    @Test
    public void testValidWhenCondition() throws ModelNodeInitException {
        testPass("/datastorevalidatortest/rangevalidation/defaultxml/valid-when-constraint-container.xml");
    }

    @Test
    public void testValidWhenConstraintForContainerCaseNode() throws ModelNodeInitException {
        testPass("/datastorevalidatortest/rangevalidation/defaultxml/valid-when-constraint-container-casenode.xml");
    }

    @Test
    public void testValidMustConstraint() throws ModelNodeInitException {
        testPass("/datastorevalidatortest/rangevalidation/defaultxml/valid-must-constraint-container.xml");
    }

    @Test
    public void testViolateMustConstraint() throws ModelNodeInitException {
        testFail("/datastorevalidatortest/rangevalidation/defaultxml/invalid-must-constraint-container.xml",
                NetconfRpcErrorTag.OPERATION_FAILED,
                NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error, "must-violation",
                "An ethernet MTU must be 1500",
                "/validation:validation/validation:must-validation/validation:interface");
    }

    @Test
    public void testViolateMustConstraintCustomErrorAppTag() throws ModelNodeInitException {
        testFail("/datastorevalidatortest/rangevalidation/defaultxml/invalid-must-constraint-container-2.xml",
                NetconfRpcErrorTag.OPERATION_FAILED,
                NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error, "custom-error-app-tag",
                "A MTU2 must be 1400", "/validation:validation/validation:must-validation/validation:interface");
    }

    @Override
    protected void initialiseInterceptor() {
        m_addDefaultDataInterceptor = null;
    }

    @Test
    public void testWrongPath() throws ModelNodeInitException {
        /// Add first leaf
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "	<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "  	<arithmetic-validation> " +
                "		 	<value1>15</value1> " +
                "			<error-path-validation/>" +
                "	    </arithmetic-validation> " +
                "</validation>";

        getModelNode();
        EditConfigRequest request1 = createRequestFromString(requestXml1);
        request1.setMessageId("1");
        NetConfResponse response1 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request1, response1);
        assertTrue(response1.isOk());

        String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "	<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "  	<arithmetic-validation> " +
                "		 	<error-path-validation>" +
                "				<wrong-path-validation>" +
                "					<key1>1</key1>" +
                "				</wrong-path-validation>" +
                "			</error-path-validation>" +
                "	    </arithmetic-validation> " +
                "</validation>";

        EditConfigRequest request2 = createRequestFromString(requestXml2);
        request2.setMessageId("1");
        NetConfResponse response2 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request2, response2);
        assertFalse(response2.isOk());

    }

    @Test
    public void testArithematicMustOnContainer() throws ModelNodeInitException {
        /// Add first leaf
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "	<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "  	<arithmetic-validation> " +
                "		 	<value1>15</value1> " +
                "	    </arithmetic-validation> " +
                "</validation>";

        getModelNode();
        editConfig(m_server, m_clientInfo, requestXml1, true);

        // add leaf to check + operator
        String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "	<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "		<arithmetic-validation> " +
                "			<value2>15</value2> " +
                "		</arithmetic-validation> " +
                "  </validation>";

        getModelNode();
        editConfig(m_server, m_clientInfo, requestXml2, true);

        // add leaf to check - operator
        String requestXml3 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "	<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "		<arithmetic-validation> " +
                "			<value3>15</value3> " +
                "		</arithmetic-validation> " +
                "  </validation>";

        getModelNode();
        editConfig(m_server, m_clientInfo, requestXml3, true);

        // add leaf to check * operator
        String requestXml4 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "	<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "		<arithmetic-validation> " +
                "			<value4>45</value4> " +
                "		</arithmetic-validation> " +
                "  </validation>";

        getModelNode();
        editConfig(m_server, m_clientInfo, requestXml4, true);

        // add leaf to check div operator
        String requestXml5 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "	<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "		<arithmetic-validation> " +
                "			<value5>15</value5> " +
                "		</arithmetic-validation> " +
                "  </validation>";

        getModelNode();
        editConfig(m_server, m_clientInfo, requestXml5, true);

        // test must on container with arithmatic and comparison
        String requestXml6 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "	<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "		<arithmetic-validation> " +
                "			<must-validation></must-validation> " +
                "		</arithmetic-validation> " +
                "  </validation>";

        getModelNode();
        editConfig(m_server, m_clientInfo, requestXml6, true);

        // test failure on must with container arithmatic and comparison
        String requestXml7 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "	<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "		<arithmetic-validation> " +
                "			<must-validation><fail-must-validation></fail-must-validation></must-validation> " +
                "		</arithmetic-validation> " +
                "  </validation>";

        getModelNode();
        NetConfResponse response7 = editConfig(m_server, m_clientInfo, requestXml7, false);
        assertFalse(response7.isOk());
        assertTrue(response7.getErrors().get(0).getErrorMessage().contains("Violate must constraints:  ../../value1 <" +
                " 0"));

    }

    @Test
    public void testMinElementListInContainer() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + " <validation>mandatory</validation>"
                + "  <mandatory-validation-container>"
                + "    <leafValidation>"
                + "      <leaf1>0</leaf1>"
                + "    </leafValidation>"
                + "    <listValidation/>"
                + " </mandatory-validation-container>"
                + "</validation>";
        NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals("Reached min-elements 1, cannot delete more child list1.", ncResponse.getErrors().get(0)
                .getErrorMessage());
        assertEquals("/validation:validation/validation:mandatory-validation-container/validation:listValidation" +
                        "/validation:list1",
                ncResponse.getErrors().get(0).getErrorPath());
        assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, ncResponse.getErrors().get(0).getErrorTag());

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + " <validation>mandatory</validation>"
                + "  <mandatory-validation-container>"
                + "    <leafValidation>"
                + "      <leaf1>0</leaf1>"
                + "    </leafValidation>"
                + "    <listValidation>"
                + "     <list1>"
                + "      <leaf1>0</leaf1>"
                + "     </list1>"
                + "    </listValidation>"
                + " </mandatory-validation-container>"
                + "</validation>"
        ;
        ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals("Reached min-elements 1, cannot delete more child innerList.", ncResponse.getErrors().get(0)
                .getErrorMessage());
        assertEquals(
                "/validation:validation/validation:mandatory-validation-container/validation:listValidation/validation:"
                        + "list1[validation:leaf1=0]/validation:innerContainer/validation:innerList",
                ncResponse.getErrors().get(0).getErrorPath());
        assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, ncResponse.getErrors().get(0).getErrorTag());

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + " <validation>mandatory</validation>"
                + "  <mandatory-validation-container>"
                + "    <leafValidation>"
                + "      <leaf1>0</leaf1>"
                + "    </leafValidation>"
                + "    <listValidation>"
                + "     <list1>"
                + "      <leaf1>0</leaf1>"
                + "      <innerContainer>"
                + "       <innerList>"
                + "        <leaf1>0</leaf1>"
                + "       </innerList>"
                + "      </innerContainer>"
                + "     </list1>"
                + "    </listValidation>"
                + " </mandatory-validation-container>"
                + "</validation>"
        ;
        editConfig(m_server, m_clientInfo, requestXml1, true);
        String response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + " <data>"
                        + "  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
                        + "   <validation:mandatory-validation-container>"
                        + "    <validation:leafValidation>"
                        + "     <validation:leaf1>0</validation:leaf1>"
                        + "     <validation:leafDefault>0</validation:leafDefault>"
                        + "    </validation:leafValidation>"
                        + "    <validation:listValidation>"
                        + "     <validation:list1>"
                        + "      <validation:innerContainer>"
                        + "       <validation:innerList>"
                        + "        <validation:defaultLeaf>0</validation:defaultLeaf>"
                        + "        <validation:leaf1>0</validation:leaf1>"
                        + "       </validation:innerList>"
                        + "      </validation:innerContainer>"
                        + "      <validation:leaf1>0</validation:leaf1>"
                        + "     </validation:list1>"
                        + "    </validation:listValidation>"
                        + "   </validation:mandatory-validation-container>"
                        + "   <validation:validation>mandatory</validation:validation>"
                        + "  </validation:validation>"
                        + " </data>"
                        + "</rpc-reply>";

        verifyGet(response);

    }


    @Test
    public void testForParentContainerDeletionWithAttributes() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + " <validate-parent-container-on-when-deletion>"
                + "  <leaf1>10</leaf1>"
                + "  <for-container>"
                + "   <container1/>"
                + "   <innerContainer>"
                + "    <leaf1>0</leaf1>"
                + "   </innerContainer>"
                + "  </for-container>"
                + " </validate-parent-container-on-when-deletion>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\" " +
                "xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <validate-parent-container-on-when-deletion>"
                + "  <leaf1>0</leaf1>"
                + " </validate-parent-container-on-when-deletion>"
                + "</validation>"
        ;
        editConfig(m_server, m_clientInfo, requestXml1, true);

        String response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + " <data>"
                        + " <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
                        + "  <validation:validate-parent-container-on-when-deletion>"
                        + "   <validation:for-container>"
                        + "    <validation:container1/>"
                        + "   </validation:for-container>"
                        + "   <validation:leaf1>0</validation:leaf1>"
                        + "  </validation:validate-parent-container-on-when-deletion>"
                        + " </validation:validation>"
                        + "</data>"
                        + "</rpc-reply>";
        verifyGet(response);

    }

    @Test
    public void testForParentContainerListDeletionWithAttributes() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + " <validate-parent-container-on-when-deletion>"
                + "  <leaf1>10</leaf1>"
                + "  <for-list>"
                + "   <list1>"
                + "    <key1>key1</key1>"
                + "   </list1>"
                + "   <innerContainer>"
                + "    <leaf1>0</leaf1>"
                + "   </innerContainer>"
                + "  </for-list>"
                + " </validate-parent-container-on-when-deletion>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\" " +
                "xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <validate-parent-container-on-when-deletion>"
                + "  <leaf1>0</leaf1>"
                + " </validate-parent-container-on-when-deletion>"
                + "</validation>"
        ;
        editConfig(m_server, m_clientInfo, requestXml1, true);

        String response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + " <data>"
                        + " <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
                        + "  <validation:validate-parent-container-on-when-deletion>"
                        + "   <validation:for-list>"
                        + "    <validation:list1>"
                        + "     <validation:key1>key1</validation:key1>"
                        + "    </validation:list1>"
                        + "   </validation:for-list>"
                        + "   <validation:leaf1>0</validation:leaf1>"
                        + "  </validation:validate-parent-container-on-when-deletion>"
                        + " </validation:validation>"
                        + "</data>"
                        + "</rpc-reply>";
        verifyGet(response);

    }

    @Test
    public void testForListWithSelfMustDeletion() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + " <selfOrCount>"
                + "  <index>0</index>"
                + " </selfOrCount>"
                + " <selfOrCount>"
                + "  <index>1</index>"
                + " </selfOrCount>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <selfOrCount xc:operation='delete'>"
                + "  <index>0</index>"
                + " </selfOrCount>"
                + "</validation>"
        ;
        editConfig(m_server, m_clientInfo, requestXml1, false);
    }

}
