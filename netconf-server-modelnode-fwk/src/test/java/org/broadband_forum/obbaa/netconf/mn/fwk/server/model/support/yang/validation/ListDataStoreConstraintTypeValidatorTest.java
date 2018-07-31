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

import static org.junit.Assert.assertTrue;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;
import org.junit.Test;
import org.w3c.dom.Element;

import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorSeverity;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorType;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;

public class ListDataStoreConstraintTypeValidatorTest extends AbstractDataStoreValidatorTest {

    @Test
    public void testViolateMaxElements() throws ModelNodeInitException {
        testFail("/datastorevalidatortest/rangevalidation/defaultxml/list-range-1.xml", NetconfRpcErrorTag
                        .OPERATION_FAILED,
                NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error, "too-many-elements",
                "Reached max-elements 2, cannot add more child list-range.",
                "/validation:validation/validation:list-range");
    }

    @Test
    public void testViolateWhenConditions() throws ModelNodeInitException {
        String requestXml1 = "/datastorevalidatortest/rangevalidation/defaultxml/invalid-when-constraint-list.xml";
        getModelNode();
        EditConfigRequest request1 = createRequest(requestXml1);
        request1.setMessageId("1");
        NetConfResponse response1 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request1, response1);
        assertTrue(response1.isOk());

        testFail("/datastorevalidatortest/rangevalidation/defaultxml/create-when-constraint-list.xml",
                NetconfRpcErrorTag.UNKNOWN_ELEMENT,
                NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error, "when-violation",
                "Violate when constraints: ../result-list = 10",
                "/validation:validation/validation:when-validation/validation:list-type[validation:list-id=1]");
    }

    @Test
    public void testValidWhenConditions() throws ModelNodeInitException {
        String requestXml1 = "/datastorevalidatortest/rangevalidation/defaultxml/valid-when-constraint-list.xml";
        getModelNode();
        EditConfigRequest request1 = createRequest(requestXml1);
        request1.setMessageId("1");
        NetConfResponse response1 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request1, response1);
        assertTrue(response1.isOk());

        testPass("/datastorevalidatortest/rangevalidation/defaultxml/create-when-constraint-list.xml");
    }

    @Test
    public void testValidMustConstraint() throws ModelNodeInitException {
        String requestXml1 = "/datastorevalidatortest/rangevalidation/defaultxml/valid-must-constraint-list.xml";
        getModelNode();
        EditConfigRequest request1 = createRequest(requestXml1);
        request1.setMessageId("1");
        NetConfResponse response1 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request1, response1);
        assertTrue(response1.isOk());

        testPass("/datastorevalidatortest/rangevalidation/defaultxml/create-must-constraint-list.xml");
    }

    @Test
    public void testViolateMustConstraint() throws ModelNodeInitException {
        String requestXml1 = "/datastorevalidatortest/rangevalidation/defaultxml/valid-must-constraint-list.xml";
        getModelNode();
        EditConfigRequest request1 = createRequest(requestXml1);
        request1.setMessageId("1");
        NetConfResponse response1 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request1, response1);
        assertTrue(response1.isOk());
        testFail("/datastorevalidatortest/rangevalidation/defaultxml/invalid-must-constraint-list.xml",
                NetconfRpcErrorTag.OPERATION_FAILED,
                NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error, "must-violation",
                "data-value must be  64 .. 10000",
                "/validation:validation/validation:must-validation/validation:interface/validation:data[validation" +
                        ":data-id=3]");
    }

    @Test
    public void testViolateWhenConstraintForLeafListCaseNode() throws ModelNodeInitException {
        String requestXml1 = "/datastorevalidatortest/rangevalidation/defaultxml/valid-when-constraint-container" +
                "-choicenode.xml";
        getModelNode();
        EditConfigRequest request1 = createRequest(requestXml1);
        request1.setMessageId("1");
        NetConfResponse response1 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request1, response1);
        assertTrue(response1.isOk());

        testFail("/datastorevalidatortest/rangevalidation/defaultxml/invalid-when-constraint-list-casenode.xml",
                NetconfRpcErrorTag.UNKNOWN_ELEMENT,
                NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error, "when-violation",
                "Violate when constraints: ../../validation:result-choice = 'failed'",
                "/validation:validation/validation:when-validation/validation:choicecase/validation:list-case-failed" +
                        "[validation:failed-id=1]");
    }


    @Test
    public void testValidWhenConstraintForLeafListCaseNode() throws ModelNodeInitException {
        String requestXml1 = "/datastorevalidatortest/rangevalidation/defaultxml/valid-when-constraint-list-casenode" +
                ".xml";
        getModelNode();
        EditConfigRequest request1 = createRequest(requestXml1);
        request1.setMessageId("1");
        NetConfResponse response1 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request1, response1);
        assertTrue(response1.isOk());

        testPass("/datastorevalidatortest/rangevalidation/defaultxml/create-when-constraint-list-casenode.xml");
    }

    @Test
    public void testUniqueConstraintViolationException() throws ModelNodeInitException {
        String singleUniqueRequest = "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + "<single-unique>"
                + "<id>1</id>"
                + "<value>value1</value>"
                + "<status>ok</status>"
                + "</single-unique>>"
                + "<single-unique>>"
                + "<id>2</id>"
                + "<value>value2</value>"
                + "<status>ok</status>"
                + "</single-unique>>"
                + "<single-unique>>"
                + "<id>3</id>"
                + "<value>value2</value>"
                + "<status>Nok</status>"
                + "</single-unique>>"
                + "</validation>";
        Element singleUniqueElement = TestUtil.transformToElement(singleUniqueRequest);

        testFail(singleUniqueElement, NetconfRpcErrorTag.OPERATION_FAILED,
                NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error, "data-not-unique",
                "value already present: UniqueConstraintCheck [m_attributes={" +
                        "(urn:org:bbf:pma:validation?revision=2015-12-14)value=value2}]",
                "/validation:validation/validation:single-unique");
    }

    @Test
    public void testForDuplicateKeyinList() throws ModelNodeInitException {
        String singleUniqueRequest = "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + "<single-unique>"
                + "<id>2</id>"
                + "<value>value1</value>"
                + "<status>ok</status>"
                + "</single-unique>>"
                + "<single-unique>>"
                + "<id>1</id>"
                + "<value>value2</value>"
                + "<status>ok</status>"
                + "</single-unique>>"
                + "<single-unique>>"
                + "<id>3</id>"
                + "<value>value3</value>"
                + "<status>Nok</status>"
                + "</single-unique>>"
                + "<single-unique>>"
                + "<id>1</id>"
                + "<value>value4</value>"
                + "<status>Nok</status>"
                + "</single-unique>>"
                + "</validation>";
        Element singleUniqueElement = TestUtil.transformToElement(singleUniqueRequest);

        testFail(singleUniqueElement, NetconfRpcErrorTag.OPERATION_FAILED,
                NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error, "data-not-unique",
                "duplicate elements in list (urn:org:bbf:pma:validation?revision=2015-12-14)single-unique",
                "/validation:validation/validation:single-unique[validation:id=1]");
    }

    @Test
    public void testMultiUniqueConstraintViolationException() throws ModelNodeInitException {

        String multiUniqueRequest = "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + "<multi-unique>"
                + "<id>1</id>"
                + "<value>value1</value>"
                + "<status>ok</status>"
                + "</multi-unique>>"
                + "<multi-unique>>"
                + "<id>2</id>"
                + "<value>value2</value>"
                + "<status>ok</status>"
                + "</multi-unique>>"
                + "<multi-unique>>"
                + "<id>3</id>"
                + "<value>value3</value>"
                + "<status>Nok</status>"
                + "</multi-unique>>"
                + "<multi-unique>>"
                + "<id>4</id>"
                + "<value>value3</value>"
                + "<status>Nok</status>"
                + "</multi-unique>>"
                + "</validation>";
        Element multiUniqueElement = TestUtil.transformToElement(multiUniqueRequest);

        testFail(multiUniqueElement, NetconfRpcErrorTag.OPERATION_FAILED,
                NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error, "data-not-unique",
                "value already present: UniqueConstraintCheck [m_attributes={" +
                        "(urn:org:bbf:pma:validation?revision=2015-12-14)status=Nok, " +
                        "(urn:org:bbf:pma:validation?revision=2015-12-14)value=value3}]",
                "/validation:validation/validation:multi-unique");
    }

    @Test
    public void testValidValidUniqueConstraint() throws ModelNodeInitException {
        String request = "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + "<single-unique>"
                + "<id>1</id>"
                + "<value>value1</value>"
                + "<status>ok</status>"
                + "</single-unique>>"
                + "<single-unique>>"
                + "<id>2</id>"
                + "<value>value2</value>"
                + "<status>ok</status>"
                + "</single-unique>>"
                + "<single-unique>>"
                + "<id>3</id>"
                + "<value>value3</value>"
                + "<status>Nok</status>"
                + "</single-unique>>"
                + "<single-unique>>"
                + "<id>4</id>"
                + "<value>value4</value>"
                + "<status>ok</status>"
                + "</single-unique>>"
                + "</validation>";

        Element editRequestElement = TestUtil.transformToElement(request);
        testPass(editRequestElement);
    }

    @Test
    public void testValidMultiUniqueConstraintViolationException() throws ModelNodeInitException {

        String multiUniqueRequest = "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + "<multi-unique>"
                + "<id>1</id>"
                + "<value>value1</value>"
                + "<status>ok</status>"
                + "</multi-unique>>"
                + "<multi-unique>>"
                + "<id>2</id>"
                + "<value>value2</value>"
                + "<status>ok</status>"
                + "</multi-unique>>"
                + "<multi-unique>>"
                + "<id>3</id>"
                + "<value>value3</value>"
                + "<status>Nok</status>"
                + "</multi-unique>>"
                + "<multi-unique>>"
                + "<id>4</id>"
                + "<value>value2</value>"
                + "<status>Nok</status>"
                + "</multi-unique>>"
                + "</validation>";
        Element multiUniqueElement = TestUtil.transformToElement(multiUniqueRequest);

        testPass(multiUniqueElement);
    }

}
