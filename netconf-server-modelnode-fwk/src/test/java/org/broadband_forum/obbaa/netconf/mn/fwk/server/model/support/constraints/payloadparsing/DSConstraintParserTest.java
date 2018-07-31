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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.payloadparsing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.validation.AbstractDataStoreValidatorTest;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import org.junit.Test;

import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorSeverity;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorType;

public class DSConstraintParserTest extends AbstractDataStoreValidatorTest {

    /*
     * Leaf
     */
    @Test
    public void testLeafValid() throws ModelNodeInitException, ValidationException {
        String requestXml1 = "/datastorevalidatortest/rangevalidation/defaultxml/valid-when-constraint-leaf.xml";
        String requestXml2 = "/datastorevalidatortest/rangevalidation/defaultxml/create-when-leaf.xml";
        getModelNode();
        EditConfigRequest request1 = createRequest(requestXml1);
        EditConfigRequest request2 = createRequest(requestXml2);
        request1.setMessageId("1");
        request2.setMessageId("1");
        NetConfResponse response1 = new NetConfResponse().setMessageId("1");
        NetConfResponse response2 = new NetConfResponse().setMessageId("1");

        m_server.onEditConfig(m_clientInfo, request1, response1);
        m_server.onEditConfig(m_clientInfo, request2, response2);
        assertTrue(response1.isOk());
        assertTrue(response2.isOk());
    }

    @Test
    public void testLeafInValid() throws ValidationException, ModelNodeInitException {
        String requestXml1 = "/datastorevalidatortest/rangevalidation/defaultxml/invalid-when-constraint-leaf.xml";
        String requestXml2 = "/datastorevalidatortest/rangevalidation/defaultxml/create-when-leaf.xml";
        getModelNode();
        EditConfigRequest request1 = createRequest(requestXml1);
        EditConfigRequest request2 = createRequest(requestXml2);
        request1.setMessageId("1");
        request2.setMessageId("1");
        NetConfResponse response1 = new NetConfResponse().setMessageId("1");
        NetConfResponse response2 = new NetConfResponse().setMessageId("1");

        m_server.onEditConfig(m_clientInfo, request1, response1);
        m_server.onEditConfig(m_clientInfo, request2, response2);
        assertTrue(response1.isOk());
        assertFalse(response2.isOk());
        assertEquals(1, response2.getErrors().size());
        NetconfRpcError rpcError = response2.getErrors().get(0);
        assertEquals(NetconfRpcErrorTag.UNKNOWN_ELEMENT, rpcError.getErrorTag());
        assertEquals(NetconfRpcErrorType.Application, rpcError.getErrorType());
        assertEquals(NetconfRpcErrorSeverity.Error, rpcError.getErrorSeverity());
        assertEquals("Violate when constraints: ../result-leaf >= 10", rpcError.getErrorMessage());
    }

    /*
     * Leaflist
     */
    @Test
    public void testLeafListValid() throws ModelNodeInitException, ValidationException {
        String requestXml1 = "/datastorevalidatortest/rangevalidation/defaultxml/valid-when-constraint-leaflist.xml";
        String requestXml2 = "/datastorevalidatortest/rangevalidation/defaultxml/create-when-leaflist.xml";
        getModelNode();
        EditConfigRequest request1 = createRequest(requestXml1);
        EditConfigRequest request2 = createRequest(requestXml2);
        request1.setMessageId("1");
        request2.setMessageId("1");
        NetConfResponse response1 = new NetConfResponse().setMessageId("1");
        NetConfResponse response2 = new NetConfResponse().setMessageId("1");

        m_server.onEditConfig(m_clientInfo, request1, response1);
        m_server.onEditConfig(m_clientInfo, request2, response2);
        assertTrue(response1.isOk());
        assertTrue(response2.isOk());
    }

    @Test
    public void testLeafListInvalid() throws ValidationException, ModelNodeInitException {
        String requestXml1 = "/datastorevalidatortest/rangevalidation/defaultxml/invalid-when-constraint-leaflist.xml";
        String requestXml2 = "/datastorevalidatortest/rangevalidation/defaultxml/create-when-leaflist.xml";
        getModelNode();
        EditConfigRequest request1 = createRequest(requestXml1);
        EditConfigRequest request2 = createRequest(requestXml2);
        request1.setMessageId("1");
        request2.setMessageId("1");
        NetConfResponse response1 = new NetConfResponse().setMessageId("1");
        NetConfResponse response2 = new NetConfResponse().setMessageId("1");

        m_server.onEditConfig(m_clientInfo, request1, response1);
        m_server.onEditConfig(m_clientInfo, request2, response2);

        assertTrue(response1.isOk());
        assertFalse(response2.isOk());
        assertEquals(1, response2.getErrors().size());
        NetconfRpcError rpcError = response2.getErrors().get(0);
        assertEquals(NetconfRpcErrorTag.UNKNOWN_ELEMENT, rpcError.getErrorTag());
        assertEquals(NetconfRpcErrorType.Application, rpcError.getErrorType());
        assertEquals(NetconfRpcErrorSeverity.Error, rpcError.getErrorSeverity());
        assertEquals("Violate when constraints: ../result-leaflist >= 5 and ../result-leaflist <= 10", rpcError
                .getErrorMessage());
    }

    /*
     * Container
     */
    @Test
    public void testContainerValid() throws ModelNodeInitException, ValidationException {
        String requestXml1 =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                        + " <validation xmlns=\"urn:org:bbf:pma:validation\">"
                        + "  <when-validation>"
                        + "   <result-container>15</result-container>"
                        + "  </when-validation>"
                        + " </validation>";

        String requestXml2 =

                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                        + " <validation xmlns=\"urn:org:bbf:pma:validation\">"
                        + "  <when-validation>"
                        + "   <container-type>"
                        + "    <container-value>the constraint evaluates to true when result-container from 10 to " +
                        "20</container-value>"
                        + "   </container-type>"
                        + "  </when-validation>"
                        + " </validation>";

        getModelNode();
        editConfig(m_server, m_clientInfo, requestXml1, true);

        getModelNode();
        editConfig(m_server, m_clientInfo, requestXml2, true);
    }

    @Test
    public void testContainerInvalid() throws ValidationException, ModelNodeInitException {
        String requestXml1 = "/datastorevalidatortest/rangevalidation/defaultxml/invalid-when-constraint-container.xml";
        String requestXml2 = "/datastorevalidatortest/rangevalidation/defaultxml/create-when-container.xml";
        getModelNode();
        EditConfigRequest request1 = createRequest(requestXml1);
        EditConfigRequest request2 = createRequest(requestXml2);
        request1.setMessageId("1");
        request2.setMessageId("2");
        NetConfResponse response1 = new NetConfResponse().setMessageId("1");
        NetConfResponse response2 = new NetConfResponse().setMessageId("2");
        m_server.onEditConfig(m_clientInfo, request1, response1);
        m_server.onEditConfig(m_clientInfo, request2, response2);

        assertTrue(response1.isOk());
        assertFalse(response2.isOk());
        assertEquals(1, response2.getErrors().size());
        NetconfRpcError rpcError = response2.getErrors().get(0);
        assertEquals(NetconfRpcErrorTag.UNKNOWN_ELEMENT, rpcError.getErrorTag());
        assertEquals(NetconfRpcErrorType.Application, rpcError.getErrorType());
        assertEquals(NetconfRpcErrorSeverity.Error, rpcError.getErrorSeverity());
        assertEquals("Violate when constraints: ../result-container >= 10 and ../result-container <= 20", rpcError
                .getErrorMessage());
    }

    /*
     * Choice
     */
    @Test
    public void testChoiceValid() throws ModelNodeInitException, ValidationException {
        String requestXml1 = "/datastorevalidatortest/rangevalidation/defaultxml/valid-when-constraint-container" +
                "-choicenode.xml";
        String requestXml2 = "/datastorevalidatortest/rangevalidation/defaultxml/create-when-container-choicenode.xml";
        getModelNode();
        EditConfigRequest request1 = createRequest(requestXml1);
        EditConfigRequest request2 = createRequest(requestXml2);
        request1.setMessageId("1");
        request2.setMessageId("1");
        NetConfResponse response1 = new NetConfResponse().setMessageId("1");
        NetConfResponse response2 = new NetConfResponse().setMessageId("1");

        m_server.onEditConfig(m_clientInfo, request1, response1);
        m_server.onEditConfig(m_clientInfo, request2, response2);
        assertTrue(response1.isOk());
        assertTrue(response2.isOk());
    }

    @Test
    public void testChoiceInvalid() throws ModelNodeInitException, ValidationException {
        String requestXml1 = "/datastorevalidatortest/rangevalidation/defaultxml/invalid-when-constraint-container" +
                "-choicenode.xml";
        String requestXml2 = "/datastorevalidatortest/rangevalidation/defaultxml/create-when-container-choicenode.xml";
        getModelNode();
        EditConfigRequest request1 = createRequest(requestXml1);
        EditConfigRequest request2 = createRequest(requestXml2);
        request1.setMessageId("1");
        request2.setMessageId("1");
        NetConfResponse response1 = new NetConfResponse().setMessageId("1");
        NetConfResponse response2 = new NetConfResponse().setMessageId("1");

        m_server.onEditConfig(m_clientInfo, request1, response1);
        m_server.onEditConfig(m_clientInfo, request2, response2);
        assertTrue(response1.isOk());
        assertFalse(response2.isOk());
        assertEquals(1, response2.getErrors().size());
        NetconfRpcError rpcError = response2.getErrors().get(0);
        assertEquals(NetconfRpcErrorTag.UNKNOWN_ELEMENT, rpcError.getErrorTag());
        assertEquals(NetconfRpcErrorType.Application, rpcError.getErrorType());
        assertEquals(NetconfRpcErrorSeverity.Error, rpcError.getErrorSeverity());
        assertEquals("Violate when constraints: ../validation:data-choice > 100", rpcError.getErrorMessage());
    }

    @Test
    public void testChoiceCaseNodeInvalid() throws ValidationException, ModelNodeInitException {
        String requestXml1 = "/datastorevalidatortest/rangevalidation/defaultxml/invalid-when-constraint-container" +
                "-casenode.xml";
        String requestXml2 = "/datastorevalidatortest/rangevalidation/defaultxml/create-when-container-choicenode.xml";
        getModelNode();
        EditConfigRequest request1 = createRequest(requestXml1);
        EditConfigRequest request2 = createRequest(requestXml2);
        request1.setMessageId("1");
        request2.setMessageId("1");
        NetConfResponse response1 = new NetConfResponse().setMessageId("1");
        NetConfResponse response2 = new NetConfResponse().setMessageId("1");

        m_server.onEditConfig(m_clientInfo, request1, response1);
        m_server.onEditConfig(m_clientInfo, request2, response2);
        assertTrue(response1.isOk());
        assertFalse(response2.isOk());
        assertEquals(1, response2.getErrors().size());
        NetconfRpcError rpcError = response2.getErrors().get(0);
        assertEquals(NetconfRpcErrorTag.UNKNOWN_ELEMENT, rpcError.getErrorTag());
        assertEquals(NetconfRpcErrorType.Application, rpcError.getErrorType());
        assertEquals(NetconfRpcErrorSeverity.Error, rpcError.getErrorSeverity());
        assertEquals("Violate when constraints: ../../validation:result-choice = 'success'", rpcError.getErrorMessage
                ());
    }

    @Test
    public void testBooleanLeafInvalid() throws ModelNodeInitException {
        String requestXml1 = "/datastorevalidatortest/rangevalidation/defaultxml/invalid-when-constraint-boolean-leaf" +
                ".xml";
        String requestXml2 = "/datastorevalidatortest/rangevalidation/defaultxml/create-when-boolean-leaf.xml";
        getModelNode();
        EditConfigRequest request1 = createRequest(requestXml1);
        EditConfigRequest request2 = createRequest(requestXml2);
        request1.setMessageId("1");
        request2.setMessageId("2");
        NetConfResponse response1 = new NetConfResponse().setMessageId("1");
        NetConfResponse response2 = new NetConfResponse().setMessageId("2");
        m_server.onEditConfig(m_clientInfo, request1, response1);
        m_server.onEditConfig(m_clientInfo, request2, response2);

        assertTrue(response1.isOk());
        assertFalse(response2.isOk());
        assertEquals(1, response2.getErrors().size());
        NetconfRpcError rpcError = response2.getErrors().get(0);
        assertEquals(NetconfRpcErrorTag.UNKNOWN_ELEMENT, rpcError.getErrorTag());
        assertEquals(NetconfRpcErrorType.Application, rpcError.getErrorType());
        assertEquals(NetconfRpcErrorSeverity.Error, rpcError.getErrorSeverity());
        assertEquals("Violate when constraints: ../boolean-leaf = true and ../boolean-leaf != false", rpcError
                .getErrorMessage());
    }

    @Test
    public void testInvalidateExistingNodeInDS() throws ModelNodeInitException {
        String requestXml1 = "/datastorevalidatortest/rangevalidation/defaultxml/valid-when-constraint-create" +
                "-unsatisfied-leaflist.xml";
        String requestXml2 = "/datastorevalidatortest/rangevalidation/defaultxml/valid-when-constraint-leaflist2.xml";
        getModelNode();
        EditConfigRequest request1 = createRequest(requestXml1);
        EditConfigRequest request2 = createRequest(requestXml2);
        request1.setMessageId("1");
        request2.setMessageId("2");
        NetConfResponse response1 = new NetConfResponse().setMessageId("1");
        NetConfResponse response2 = new NetConfResponse().setMessageId("2");
        m_server.onEditConfig(m_clientInfo, request1, response1);
        m_server.onEditConfig(m_clientInfo, request2, response2);

        assertTrue(response1.isOk());
        assertFalse(response2.isOk());
        assertEquals(1, response2.getErrors().size());
        NetconfRpcError rpcError = response2.getErrors().get(0);
        assertEquals(NetconfRpcErrorTag.UNKNOWN_ELEMENT, rpcError.getErrorTag());
        assertEquals(NetconfRpcErrorType.Application, rpcError.getErrorType());
        assertEquals(NetconfRpcErrorSeverity.Error, rpcError.getErrorSeverity());
        assertEquals("Violate when constraints: ../result-leaflist >= 5 and ../result-leaflist <= 10", rpcError
                .getErrorMessage());
    }

    @Test
    public void testValidateExistingNodeInDS() throws ModelNodeInitException {
        String requestXml1 = "/datastorevalidatortest/rangevalidation/defaultxml/valid-when-constraint-create" +
                "-satisfied-leaflist.xml";
        String requestXml2 = "/datastorevalidatortest/rangevalidation/defaultxml/valid-when-constraint-leaflist2.xml";
        getModelNode();
        EditConfigRequest request1 = createRequest(requestXml1);
        EditConfigRequest request2 = createRequest(requestXml2);
        request1.setMessageId("1");
        request2.setMessageId("2");
        NetConfResponse response1 = new NetConfResponse().setMessageId("1");
        NetConfResponse response2 = new NetConfResponse().setMessageId("2");
        m_server.onEditConfig(m_clientInfo, request1, response1);
        m_server.onEditConfig(m_clientInfo, request2, response2);

        assertTrue(response1.isOk());
        assertTrue(response2.isOk());
    }
}
