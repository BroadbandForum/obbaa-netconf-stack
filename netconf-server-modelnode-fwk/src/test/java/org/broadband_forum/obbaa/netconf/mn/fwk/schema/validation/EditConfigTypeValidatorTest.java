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

package org.broadband_forum.obbaa.netconf.mn.fwk.schema.validation;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.fail;

import java.util.Collections;

import org.apache.log4j.Logger;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImplTest;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.RpcRequestConstraintParser;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.w3c.dom.NodeList;

import org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorInfo;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DSExpressionValidator;
import org.broadband_forum.obbaa.netconf.server.rpc.RequestType;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;

/**
 * Created by keshava on 11/24/15.
 */
public class EditConfigTypeValidatorTest {
    RpcRequestConstraintParser m_editConfigValidator;
    private SchemaRegistryImpl m_schemaRegistry;
    private ModelNodeDataStoreManager m_modelNodeDsm;
    private DSExpressionValidator m_expValidator;
    private static final Logger LOGGER = Logger.getLogger(EditConfigTypeValidatorTest.class);

    @Before
    public void setUp() throws SchemaBuildException {
        m_schemaRegistry = new SchemaRegistryImpl(Collections.<YangTextSchemaSource>emptyList(), new NoLockService());
        m_schemaRegistry.buildSchemaContext(SchemaRegistryImplTest.getAnvYangFiles());
        m_schemaRegistry.loadSchemaContext("G.fast-plug", SchemaRegistryImplTest.getGfastYangFiles(), null,
                Collections.emptyMap());
        m_modelNodeDsm = Mockito.mock(ModelNodeDataStoreManager.class);
        m_expValidator = Mockito.mock(DSExpressionValidator.class);
        m_editConfigValidator = new RpcRequestConstraintParser(m_schemaRegistry, m_modelNodeDsm, m_expValidator);
    }

    @Test
    public void testEditConfigValidation() throws NetconfMessageBuilderException {
        try {
            EditConfigRequest invalidEditConfigRequest = DocumentToPojoTransformer.getEditConfig(DocumentUtils
                    .loadXmlDocument(EditConfigTypeValidatorTest.class
                            .getResourceAsStream
                                    ("/yangSchemaValidationTest/rpcpayloadvalidatortest/requests/editconfigs/invalid" +
                                            "-editconfig-request0.xml")));
            m_editConfigValidator.validate(invalidEditConfigRequest, RequestType.EDIT_CONFIG);
            fail("validation should have thrown an exception for request: " + invalidEditConfigRequest
                    .requestToString());
        } catch (ValidationException e) {
            NetconfRpcError rpcError = e.getRpcError();
            assertEquals(NetconfRpcErrorTag.UNKNOWN_ELEMENT, rpcError.getErrorTag());
            assertTrue(rpcError.getErrorInfo().getElementsByTagName(NetconfRpcErrorInfo.BadElement.value()) != null);
            assertEquals("pma2", rpcError.getErrorInfo().getElementsByTagName(NetconfRpcErrorInfo.BadElement.value())
                    .item(0).getTextContent());
            assertEquals("/", rpcError.getErrorPath());
        }
        try {
            EditConfigRequest invalidEditConfigRequest = DocumentToPojoTransformer.getEditConfig(DocumentUtils
                    .loadXmlDocument(EditConfigTypeValidatorTest.class
                            .getResourceAsStream
                                    ("/yangSchemaValidationTest/rpcpayloadvalidatortest/requests/editconfigs/invalid" +
                                            "-editconfig-request1.xml")));
            m_editConfigValidator.validate(invalidEditConfigRequest, RequestType.EDIT_CONFIG);
            fail("validation should have thrown an exception for request: " + invalidEditConfigRequest
                    .requestToString());
        } catch (ValidationException e) {
            NetconfRpcError rpcError = e.getRpcError();
            assertEquals(NetconfRpcErrorTag.UNKNOWN_NAMESPACE, rpcError.getErrorTag());
            assertTrue(rpcError.getErrorInfo().getElementsByTagName(NetconfRpcErrorInfo.BadElement.value()) != null);
            assertEquals("device-holder", rpcError.getErrorInfo().getElementsByTagName(NetconfRpcErrorInfo.BadElement
                    .value()).item(0).getTextContent());
            assertEquals("non:existent:nmaespace", rpcError.getErrorInfo().getElementsByTagName(NetconfRpcErrorInfo
                    .BadNamespace.value()).item(0)
                    .getTextContent());
            assertEquals("/pma:pma/device-holder", rpcError.getErrorPath());
        }
        try {
            EditConfigRequest invalidEditConfigRequest = DocumentToPojoTransformer.getEditConfig(DocumentUtils
                    .loadXmlDocument(EditConfigTypeValidatorTest.class
                            .getResourceAsStream
                                    ("/yangSchemaValidationTest/rpcpayloadvalidatortest/requests/editconfigs/invalid" +
                                            "-editconfig-request2.xml")));
            m_editConfigValidator.validate(invalidEditConfigRequest, RequestType.EDIT_CONFIG);
            fail("validation should have thrown an exception for request: " + invalidEditConfigRequest
                    .requestToString());
        } catch (ValidationException e) {
            NetconfRpcError rpcError = e.getRpcError();
            assertEquals(NetconfRpcErrorTag.UNKNOWN_ELEMENT, rpcError.getErrorTag());
            assertTrue(rpcError.getErrorInfo().getElementsByTagName(NetconfRpcErrorInfo.BadElement.value()) != null);
            assertEquals("invalid-device-holder", rpcError.getErrorInfo().getElementsByTagName(NetconfRpcErrorInfo
                    .BadElement.value()).item(0).getTextContent());
            assertEquals("/pma:pma/pma:invalid-device-holder", rpcError.getErrorPath());
        }
        try {
            EditConfigRequest invalidEditConfigRequest = DocumentToPojoTransformer.getEditConfig(DocumentUtils
                    .loadXmlDocument(EditConfigTypeValidatorTest.class
                            .getResourceAsStream
                                    ("/yangSchemaValidationTest/rpcpayloadvalidatortest/requests/editconfigs/invalid" +
                                            "-editconfig-request3.xml")));
            m_editConfigValidator.validate(invalidEditConfigRequest, RequestType.EDIT_CONFIG);
            fail("validation should have thrown an exception for request: " + invalidEditConfigRequest
                    .requestToString());
        } catch (ValidationException e) {
            NetconfRpcError rpcError = e.getRpcError();
            assertEquals(NetconfRpcErrorTag.UNKNOWN_ELEMENT, rpcError.getErrorTag());
            assertTrue(rpcError.toString().contains("invalid-key"));
            assertTrue(rpcError.getErrorInfo().getElementsByTagName(NetconfRpcErrorInfo.BadElement.value()) != null);
            assertEquals("invalid-key", rpcError.getErrorInfo().getElementsByTagName(NetconfRpcErrorInfo.BadElement
                    .value()).item(0).getTextContent());
            assertEquals("/pma:pma/pma:device-holder[pma:name='OLT-1']/pma:invalid-key", rpcError.getErrorPath());
        }
        EditConfigRequest validEditConfigRequest = DocumentToPojoTransformer.getEditConfig(DocumentUtils
                .loadXmlDocument(EditConfigTypeValidatorTest.class
                .getResourceAsStream("/yangSchemaValidationTest/rpcpayloadvalidatortest/requests/editconfigs/valid" +
                        "-editconfig-request1.xml")));
        try {
            m_editConfigValidator.validate(validEditConfigRequest, RequestType.EDIT_CONFIG);
        } catch (ValidationException e) {
            LOGGER.error(e);
            fail("validation should NOT have thrown an exception for request: " + validEditConfigRequest
                    .requestToString());
        }

        validEditConfigRequest = DocumentToPojoTransformer.getEditConfig(DocumentUtils.loadXmlDocument
                (EditConfigTypeValidatorTest.class
                .getResourceAsStream("/yangSchemaValidationTest/rpcpayloadvalidatortest/requests/editconfigs/valid" +
                        "-editconfig-request2.xml")));
        try {

            m_editConfigValidator.validate(validEditConfigRequest, RequestType.EDIT_CONFIG);

        } catch (ValidationException e) {
            LOGGER.error(e);
            fail("validation should NOT have thrown an exception for request: " + validEditConfigRequest
                    .requestToString());
        }
        validEditConfigRequest = DocumentToPojoTransformer.getEditConfig(DocumentUtils.loadXmlDocument
                (EditConfigTypeValidatorTest.class
                .getResourceAsStream("/yangSchemaValidationTest/rpcpayloadvalidatortest/requests/editconfigs/valid" +
                        "-editconfig-request3.xml")));
        try {
            m_editConfigValidator.validate(validEditConfigRequest, RequestType.EDIT_CONFIG);
        } catch (ValidationException e) {
            LOGGER.error(e);
            fail("validation should NOT have thrown an exception for request: " + validEditConfigRequest
                    .requestToString());
        }
    }

    @Test
    public void testTypeValidation() throws NetconfMessageBuilderException {
        try {
            EditConfigRequest invalidEditConfigRequest = DocumentToPojoTransformer.getEditConfig(
                    DocumentUtils.loadXmlDocument(EditConfigTypeValidatorTest.class.getResourceAsStream
                            ("/yangSchemaValidationTest/rpcpayloadvalidatortest/requests/editconfigs/invalid" +
                                    "-editconfig-request4.xml")));
            m_editConfigValidator.validate(invalidEditConfigRequest, RequestType.EDIT_CONFIG);
            fail("validation should have thrown an exception for request: " + invalidEditConfigRequest
                    .requestToString());
        } catch (ValidationException e) {
            NetconfRpcError rpcError = e.getRpcError();
            assertEquals("range-out-of-specified-bounds", rpcError.getErrorAppTag());
            assertEquals("The argument is out of bounds <0, 65535>", rpcError.getErrorMessage());
            assertEquals(NetconfRpcErrorTag.INVALID_VALUE, rpcError.getErrorTag());
            assertEquals("/pma:pma/pma:device-holder[pma:name='OLT1']/pma:device[pma:device-id='device1']/pma" +
                    ":configured-device-properties/pma:ip-port", rpcError.getErrorPath());
        }
    }

    @Test
    public void testStateAttributeValidation() throws NetconfMessageBuilderException {
        try {
            EditConfigRequest invalidEditConfigRequest = DocumentToPojoTransformer.getEditConfig(DocumentUtils
                    .loadXmlDocument(EditConfigTypeValidatorTest.class
                            .getResourceAsStream
                                    ("/yangSchemaValidationTest/rpcpayloadvalidatortest/requests/editconfigs/invalid" +
                                            "-editconfig-request5.xml")));
            m_editConfigValidator.validate(invalidEditConfigRequest, RequestType.EDIT_CONFIG);
            fail("validation should have thrown an exception for request: " + invalidEditConfigRequest
                    .requestToString());
        } catch (ValidationException e) {
            NetconfRpcError rpcError = e.getRpcError();
            assertEquals(NetconfRpcErrorTag.UNKNOWN_ELEMENT, rpcError.getErrorTag());
            NodeList badElement = rpcError.getErrorInfo().getElementsByTagName(NetconfRpcErrorInfo.BadElement.value());
            assertTrue(badElement != null);
            assertEquals("number-of-devices", badElement.item(0).getTextContent());
            assertEquals("/pma:pma/pma:device-holder[pma:name='OLT1']/pma:number-of-devices", rpcError.getErrorPath());
        }


        try {
            EditConfigRequest invalidEditConfigRequest = DocumentToPojoTransformer.getEditConfig(DocumentUtils
                    .loadXmlDocument(EditConfigTypeValidatorTest.class
                            .getResourceAsStream
                                    ("/yangSchemaValidationTest/rpcpayloadvalidatortest/requests/editconfigs/invalid" +
                                            "-editconfig-request6.xml")));
            m_editConfigValidator.validate(invalidEditConfigRequest, RequestType.EDIT_CONFIG);
            fail("validation should have thrown an exception for request: " + invalidEditConfigRequest
                    .requestToString());
        } catch (ValidationException e) {
            NetconfRpcError rpcError = e.getRpcError();
            assertEquals(NetconfRpcErrorTag.UNKNOWN_ELEMENT, rpcError.getErrorTag());
            NodeList badElement = rpcError.getErrorInfo().getElementsByTagName(NetconfRpcErrorInfo.BadElement.value());
            assertTrue(badElement != null);
            assertEquals("device-state", badElement.item(0).getTextContent());
            assertEquals("/pma:pma/pma:device-holder[pma:name='OLT1']/pma:device[pma:device-id='R1.S1.LT1.P1.ONT1" +
                    "']/pma:device-state", rpcError.getErrorPath());
        }
    }


    @Test
    public void testOperationValidation() throws NetconfMessageBuilderException {
        try {
            EditConfigRequest invalidEditConfigRequest = DocumentToPojoTransformer.getEditConfig(DocumentUtils
                    .loadXmlDocument(EditConfigTypeValidatorTest.class
                            .getResourceAsStream
                                    ("/yangSchemaValidationTest/rpcpayloadvalidatortest/requests/editconfigs/invalid" +
                                            "-editconfig-request10.xml")));
            m_editConfigValidator.validate(invalidEditConfigRequest, RequestType.EDIT_CONFIG);
            fail("validation should have thrown an exception for request: " + invalidEditConfigRequest
                    .requestToString());
        } catch (ValidationException e) {
            NetconfRpcError rpcError = e.getRpcError();
            assertEquals(NetconfRpcErrorTag.BAD_ATTRIBUTE, rpcError.getErrorTag());
            NodeList badAttribute = rpcError.getErrorInfo().getElementsByTagName(NetconfRpcErrorInfo.BadAttribute
                    .value());
            assertTrue(badAttribute != null);
            assertEquals("pma", badAttribute.item(0).getTextContent());
            assertEquals("/", rpcError.getErrorPath());
            assertEquals("Do not allow to remove/delete root node", rpcError.getErrorMessage());
        }

        try {
            EditConfigRequest invalidEditConfigRequest = DocumentToPojoTransformer.getEditConfig(DocumentUtils
                    .loadXmlDocument(EditConfigTypeValidatorTest.class
                            .getResourceAsStream
                                    ("/yangSchemaValidationTest/rpcpayloadvalidatortest/requests/editconfigs/invalid" +
                                            "-editconfig-request11.xml")));
            m_editConfigValidator.validate(invalidEditConfigRequest, RequestType.EDIT_CONFIG);
            fail("validation should have thrown an exception for request: " + invalidEditConfigRequest
                    .requestToString());
        } catch (ValidationException e) {
            NetconfRpcError rpcError = e.getRpcError();
            assertEquals(NetconfRpcErrorTag.BAD_ATTRIBUTE, rpcError.getErrorTag());
            NodeList badAttribute = rpcError.getErrorInfo().getElementsByTagName(NetconfRpcErrorInfo.BadAttribute
                    .value());
            assertTrue(badAttribute != null);
            assertEquals("device", badAttribute.item(0).getTextContent());
            assertEquals("/pma:pma/pma:device-holder[pma:name='OLT1']/pma:device", rpcError.getErrorPath());
            assertEquals("Child cannot override operation if parent is set to replace", rpcError.getErrorMessage());
        }
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testInsertAttributesValidation() throws NetconfMessageBuilderException {
        EditConfigRequest validEditConfigRequest = DocumentToPojoTransformer.getEditConfig(DocumentUtils
                .loadXmlDocument(EditConfigTypeValidatorTest.class
                        .getResourceAsStream("/yangSchemaValidationTest/rpcpayloadvalidatortest/requests/editconfigs" +
                                "/valid-editconfig-request4.xml")));
        try {

            m_editConfigValidator.validate(validEditConfigRequest, RequestType.EDIT_CONFIG);
        } catch (ValidationException e) {
            LOGGER.error(e);
            fail("validation should NOT have thrown an exception for request: " + validEditConfigRequest.toString());
        }

        validEditConfigRequest = DocumentToPojoTransformer.getEditConfig(DocumentUtils
                .loadXmlDocument(EditConfigTypeValidatorTest.class
                        .getResourceAsStream("/yangSchemaValidationTest/rpcpayloadvalidatortest/requests/editconfigs" +
                                "/valid-editconfig-request5.xml")));
        try {

            m_editConfigValidator.validate(validEditConfigRequest, RequestType.EDIT_CONFIG);
        } catch (ValidationException e) {
            LOGGER.error(e);
            fail("validation should NOT have thrown an exception for request: " + validEditConfigRequest.toString());
        }

        try {
            EditConfigRequest invalidEditConfigRequest = DocumentToPojoTransformer.getEditConfig(DocumentUtils
                    .loadXmlDocument(EditConfigTypeValidatorTest.class
                            .getResourceAsStream
                                    ("/yangSchemaValidationTest/rpcpayloadvalidatortest/requests/editconfigs/invalid" +
                                            "-editconfig-request12.xml")));
            m_editConfigValidator.validate(invalidEditConfigRequest, RequestType.EDIT_CONFIG);
            fail("validation should have thrown an exception for request: " + invalidEditConfigRequest
                    .requestToString());
        } catch (ValidationException e) {
            NetconfRpcError rpcError = e.getRpcError();
            assertEquals(NetconfRpcErrorTag.BAD_ATTRIBUTE, rpcError.getErrorTag());
            NodeList badAttribute = rpcError.getErrorInfo().getElementsByTagName(NetconfRpcErrorInfo.BadAttribute
                    .value());
            assertTrue(badAttribute != null);
            assertEquals("insert", badAttribute.item(0).getTextContent());
            assertEquals("Bad insert attribute : unknown", rpcError.getErrorMessage());
        }

        try {
            EditConfigRequest invalidEditConfigRequest = DocumentToPojoTransformer.getEditConfig(DocumentUtils
                    .loadXmlDocument(EditConfigTypeValidatorTest.class
                            .getResourceAsStream
                                    ("/yangSchemaValidationTest/rpcpayloadvalidatortest/requests/editconfigs/invalid" +
                                            "-editconfig-request13.xml")));
            m_editConfigValidator.validate(invalidEditConfigRequest, RequestType.EDIT_CONFIG);
            fail("validation should have thrown an exception for request: " + invalidEditConfigRequest
                    .requestToString());
        } catch (ValidationException e) {
            NetconfRpcError rpcError = e.getRpcError();
            assertEquals(NetconfRpcErrorTag.BAD_ATTRIBUTE, rpcError.getErrorTag());
            NodeList badAttribute = rpcError.getErrorInfo().getElementsByTagName(NetconfRpcErrorInfo.BadAttribute
                    .value());
            assertTrue(badAttribute != null);
            assertEquals("insert", badAttribute.item(0).getTextContent());
            assertEquals("Bad insert attribute : unknown", rpcError.getErrorMessage());
        }

        try {
            EditConfigRequest invalidEditConfigRequest = DocumentToPojoTransformer.getEditConfig(DocumentUtils
                    .loadXmlDocument(EditConfigTypeValidatorTest.class
                            .getResourceAsStream
                                    ("/yangSchemaValidationTest/rpcpayloadvalidatortest/requests/editconfigs/invalid" +
                                            "-editconfig-request14.xml")));
            m_editConfigValidator.validate(invalidEditConfigRequest, RequestType.EDIT_CONFIG);
            fail("validation should have thrown an exception for request: " + invalidEditConfigRequest.requestToString());
        } catch (ValidationException e) {
            NetconfRpcError rpcError = e.getRpcError();
            assertEquals(NetconfRpcErrorTag.BAD_ATTRIBUTE, rpcError.getErrorTag());
            NodeList badAttribute = rpcError.getErrorInfo().getElementsByTagName(NetconfRpcErrorInfo.BadAttribute.value());
            assertTrue(badAttribute != null);
            assertEquals("value", badAttribute.item(0).getTextContent());
            assertEquals("value attribute can't be null or empty", rpcError.getErrorMessage());
        }
    }
}
