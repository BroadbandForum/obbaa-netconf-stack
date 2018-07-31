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

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.RpcRequestConstraintParser;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;

import org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorInfo;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorSeverity;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorType;
import org.broadband_forum.obbaa.netconf.api.parser.YangParserUtil;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImplTest;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DSExpressionValidator;
import org.broadband_forum.obbaa.netconf.server.rpc.RequestType;

public class MandatoryTypeTypeValidatorTest {

    public static final String MANDATORY_LEAF_ABSENT_IN_CONTAINER_XML =
            "/yangSchemaValidationTest/mandatorytypevalidationtest/mandatoryLeafAbsentInContainer.xml";
    public static final String MANDATORY_LEAF_ABSENT_IN_LIST_XML =
            "/yangSchemaValidationTest/mandatorytypevalidationtest/mandatoryLeafAbsentInList.xml";
    public static final String MANDATORY_LEAF_ABSENT_IN_SUBLIST_XML =
            "/yangSchemaValidationTest/mandatorytypevalidationtest/mandatoryLeafAbsentInSublist.xml";
    public static final String MANDATORY_CHOICE_ABSENT_IN_CONTAINER_XML =
            "/yangSchemaValidationTest/mandatorytypevalidationtest/mandatoryChoiceAbsentInContainer.xml";
    public static final String MANDATORY_CHOICE_PRESENT_WITH_INVALID_ELEMENT_IN_CONTAINER_XML =
            "/yangSchemaValidationTest/mandatorytypevalidationtest" +
                    "/mandatoryChoicePresentWithInvalidElementInContainer.xml";
    public static final String MANDATORY_CHOICE_PRESENT_IN_CONTAINER_XML =
            "/yangSchemaValidationTest/mandatorytypevalidationtest/mandatoryChoicePresentInContainer.xml";
    public static final String MANDATORY_CHOICE_ABSENT_IN_LIST_XML =
            "/yangSchemaValidationTest/mandatorytypevalidationtest/mandatoryChoiceAbsentInList.xml";
    public static final String MANDATORY_CHOICE_PRESENT_WITH_INVALID_ELEMENT_IN_LIST_XML =
            "/yangSchemaValidationTest/mandatorytypevalidationtest/mandatoryChoicePresentWithInvalidElementInList.xml";
    public static final String MANDATORY_CHOICE_PRESENT_IN_LIST_XML =
            "/yangSchemaValidationTest/mandatorytypevalidationtest/mandatoryChoicePresentInList.xml";
    public static final String MANDATORY_ANY_XML_ABSENT_IN_CONTAINER_XML =
            "/yangSchemaValidationTest/mandatorytypevalidationtest/mandatoryAnyXmlAbsentInContainer.xml";
    public static final String MANDATORY_ANY_XML_PRESENT_IN_CONTAINER_XML =
            "/yangSchemaValidationTest/mandatorytypevalidationtest/mandatoryAnyXmlPresentInContainer.xml";
    public static final String MANDATORY_ANY_XML_ABSENT_IN_LIST_XML =
            "/yangSchemaValidationTest/mandatorytypevalidationtest/mandatoryAnyXmlAbsentInList.xml";
    public static final String MANDATORY_ANY_XML_PRESENT_IN_LIST_XML =
            "/yangSchemaValidationTest/mandatorytypevalidationtest/mandatoryAnyXmlPresentInList.xml";
    public static final String CONTAINER_WITH_MANDATORY_LEAF_ABSENT_XML =
            "/yangSchemaValidationTest/mandatorytypevalidationtest/containerWithMandatoryLeafAbsent.xml";
    public static final String CONTAINER_WITH_MANDATORY_CHOICE_ABSENT_XML =
            "/yangSchemaValidationTest/mandatorytypevalidationtest/containerWithMandatoryChoiceAbsent.xml";
    public static final String CONTAINER_WITH_LEAF_IN_MANDATORY_CHOICE_ABSENT_XML =
            "/yangSchemaValidationTest/mandatorytypevalidationtest/containerWithLeafInMandatoryChoiceAbsent.xml";
    public static final String ALL_MANDATORY_ELEMENTS_PRESENT_XML =
            "/yangSchemaValidationTest/mandatorytypevalidationtest/allMandatoryElementsPresent.xml";
    public static final String VALID_REQUEST_FAILED = "Validation should not have thrown an exception for request: ";
    public static final String INVALID_REQUEST_SUCCESS = "validation should have thrown an exception for request: ";
    private RpcRequestConstraintParser m_editConfigValidator;
    private SchemaRegistryImpl m_schemaRegistry;
    private ModelNodeDataStoreManager m_modelNodeDsm;
    private DSExpressionValidator m_expValidator;

    @Before
    public void setUp() throws SchemaBuildException {
        m_schemaRegistry = new SchemaRegistryImpl(Collections.<YangTextSchemaSource>emptyList(), new NoLockService());
        YangTextSchemaSource jukeboxYangFile = YangParserUtil.getYangSource(SchemaRegistryImplTest.class.getResource
                ("/yangSchemaValidationTest/mandatorytypevalidationtest/example-jukebox@2014-07-03.yang"));

        m_schemaRegistry.buildSchemaContext((List<YangTextSchemaSource>) Arrays.asList(jukeboxYangFile));
        m_modelNodeDsm = Mockito.mock(ModelNodeDataStoreManager.class);
        m_expValidator = Mockito.mock(DSExpressionValidator.class);
        m_editConfigValidator = new RpcRequestConstraintParser(m_schemaRegistry, m_modelNodeDsm, m_expValidator);
    }

    @Test
    public void testMandatoryLeafValidationInContainer() throws NetconfMessageBuilderException {

        // Invalid case
        try {
            EditConfigRequest invalidEditConfigRequest = DocumentToPojoTransformer.getEditConfig(DocumentUtils
                    .loadXmlDocument(MandatoryTypeTypeValidatorTest.class
                            .getResourceAsStream(MANDATORY_LEAF_ABSENT_IN_CONTAINER_XML)));
            m_editConfigValidator.validate(invalidEditConfigRequest, RequestType.EDIT_CONFIG);
            fail(INVALID_REQUEST_SUCCESS + invalidEditConfigRequest.requestToString());
        } catch (ValidationException e) {
            NetconfRpcError rpcError = e.getRpcError();
            assertEquals(NetconfRpcErrorTag.DATA_MISSING, rpcError.getErrorTag());
            assertTrue(rpcError.getErrorInfo().getElementsByTagName(NetconfRpcErrorInfo.MissingLeaf.value()) != null);
            assertEquals("name", rpcError.getErrorInfo().getElementsByTagName(NetconfRpcErrorInfo.MissingLeaf.value()
            ).item(0).getTextContent());
            assertEquals("/jbox:jukebox/jbox:name", rpcError.getErrorPath());
        }

        // Valid case
        EditConfigRequest validEditConfigRequest = null;
        try {
            validEditConfigRequest = DocumentToPojoTransformer.getEditConfig(DocumentUtils
                    .loadXmlDocument(MandatoryTypeTypeValidatorTest.class
                            .getResourceAsStream(ALL_MANDATORY_ELEMENTS_PRESENT_XML)));
            m_editConfigValidator.validate(validEditConfigRequest, RequestType.EDIT_CONFIG);
        } catch (ValidationException e) {
            fail(VALID_REQUEST_FAILED + validEditConfigRequest.requestToString());
        }
    }

    @Test
    public void testMandatoryChoiceValidationInContainer() throws NetconfMessageBuilderException {

        // Invalid case
        EditConfigRequest invalidEditConfigRequest;
        try {
            invalidEditConfigRequest = DocumentToPojoTransformer.getEditConfig(DocumentUtils
                    .loadXmlDocument(MandatoryTypeTypeValidatorTest.class
                            .getResourceAsStream(MANDATORY_CHOICE_ABSENT_IN_CONTAINER_XML)));
            m_editConfigValidator.validate(invalidEditConfigRequest, RequestType.EDIT_CONFIG);
            fail(INVALID_REQUEST_SUCCESS + invalidEditConfigRequest.requestToString());
        } catch (ValidationException e) {
            NetconfRpcError rpcError = e.getRpcError();
            assertEquals(NetconfRpcErrorTag.DATA_MISSING, rpcError.getErrorTag());
            assertTrue(rpcError.getErrorInfo().getElementsByTagName(NetconfRpcErrorInfo.MissingChoice.value()) != null);
            assertEquals("play", rpcError.getErrorInfo().getElementsByTagName(NetconfRpcErrorInfo.MissingChoice.value
                    ()).item(0).getTextContent());
            assertEquals("/jbox:jukebox/jbox:player/jbox:play", rpcError.getErrorPath());
        }

        // Invalid case
        try {
            invalidEditConfigRequest = DocumentToPojoTransformer.getEditConfig(DocumentUtils
                    .loadXmlDocument(MandatoryTypeTypeValidatorTest.class
                            .getResourceAsStream(MANDATORY_CHOICE_PRESENT_WITH_INVALID_ELEMENT_IN_CONTAINER_XML)));
            m_editConfigValidator.validate(invalidEditConfigRequest, RequestType.EDIT_CONFIG);
            fail(INVALID_REQUEST_SUCCESS + invalidEditConfigRequest.requestToString());
        } catch (ValidationException e) {
            NetconfRpcError rpcError = e.getRpcError();
            assertEquals(NetconfRpcErrorTag.BAD_ELEMENT, rpcError.getErrorTag());
            assertEquals(NetconfRpcErrorType.Application, rpcError.getErrorType());
            assertEquals(NetconfRpcErrorSeverity.Error, rpcError.getErrorSeverity());
        }

        // Valid case
        EditConfigRequest validEditConfigRequest = null;

        try {
            validEditConfigRequest = DocumentToPojoTransformer.getEditConfig(DocumentUtils
                    .loadXmlDocument(MandatoryTypeTypeValidatorTest.class
                            .getResourceAsStream(MANDATORY_CHOICE_PRESENT_IN_CONTAINER_XML)));
            m_editConfigValidator.validate(validEditConfigRequest, RequestType.EDIT_CONFIG);
        } catch (ValidationException e) {
            fail(VALID_REQUEST_FAILED + validEditConfigRequest.requestToString());
        }

    }

    @Test
    public void testMandatoryLeafValidationInList() throws NetconfMessageBuilderException {

        // Invalid case
        try {
            EditConfigRequest invalidEditConfigRequest = DocumentToPojoTransformer.getEditConfig(DocumentUtils
                    .loadXmlDocument(MandatoryTypeTypeValidatorTest.class
                            .getResourceAsStream(MANDATORY_LEAF_ABSENT_IN_LIST_XML)));
            m_editConfigValidator.validate(invalidEditConfigRequest, RequestType.EDIT_CONFIG);
            fail(INVALID_REQUEST_SUCCESS + invalidEditConfigRequest.requestToString());
        } catch (ValidationException e) {
            NetconfRpcError rpcError = e.getRpcError();
            assertEquals(NetconfRpcErrorTag.DATA_MISSING, rpcError.getErrorTag());
            assertTrue(rpcError.getErrorInfo().getElementsByTagName(NetconfRpcErrorInfo.MissingLeaf.value()) != null);
            assertEquals("year", rpcError.getErrorInfo().getElementsByTagName(NetconfRpcErrorInfo.MissingLeaf.value()
            ).item(0).getTextContent());
            assertEquals("/jbox:jukebox/jbox:library/jbox:artist[jbox:name='Lenny']/jbox:album[jbox:name='Greatest " +
                    "hits']/jbox:year", rpcError.getErrorPath());
        }
    }

    @Test
    public void testMandatoryLeafValidationInSublist() throws NetconfMessageBuilderException {

        // Invalid case
        try {
            EditConfigRequest invalidEditConfigRequest = DocumentToPojoTransformer.getEditConfig(DocumentUtils
                    .loadXmlDocument(MandatoryTypeTypeValidatorTest.class
                            .getResourceAsStream(MANDATORY_LEAF_ABSENT_IN_SUBLIST_XML)));
            m_editConfigValidator.validate(invalidEditConfigRequest, RequestType.EDIT_CONFIG);
            fail(INVALID_REQUEST_SUCCESS + invalidEditConfigRequest.requestToString());
        } catch (ValidationException e) {
            NetconfRpcError rpcError = e.getRpcError();
            assertEquals(NetconfRpcErrorTag.DATA_MISSING, rpcError.getErrorTag());
            assertTrue(rpcError.getErrorInfo().getElementsByTagName(NetconfRpcErrorInfo.MissingLeaf.value()) != null);
            assertEquals("year", rpcError.getErrorInfo().getElementsByTagName(NetconfRpcErrorInfo.MissingLeaf.value()
            ).item(0).getTextContent());
            assertEquals("/jbox:jukebox/jbox:library/jbox:artist[jbox:name='Lenny']/jbox:album[jbox:name='Greatest " +
                    "hits']/jbox:year", rpcError.getErrorPath());
        }
    }

    @Test
    public void testContainerWithMandatoryLeafAbsent() throws NetconfMessageBuilderException {

        // Invalid case
        try {
            EditConfigRequest invalidEditConfigRequest = DocumentToPojoTransformer.getEditConfig(DocumentUtils
                    .loadXmlDocument(MandatoryTypeTypeValidatorTest.class
                            .getResourceAsStream(CONTAINER_WITH_MANDATORY_LEAF_ABSENT_XML)));
            m_editConfigValidator.validate(invalidEditConfigRequest, RequestType.EDIT_CONFIG);
            fail(INVALID_REQUEST_SUCCESS + invalidEditConfigRequest.requestToString());
        } catch (ValidationException e) {
            NetconfRpcError rpcError = e.getRpcError();
            assertEquals(NetconfRpcErrorTag.DATA_MISSING, rpcError.getErrorTag());
            assertTrue(rpcError.getErrorInfo().getElementsByTagName(NetconfRpcErrorInfo.MissingLeaf.value()) != null);
            assertEquals("name", rpcError.getErrorInfo().getElementsByTagName(NetconfRpcErrorInfo.MissingLeaf.value()
            ).item(0).getTextContent());
            assertEquals("/jbox:jukebox/jbox:presence-container/jbox:non-presence-player-with-leaf/jbox:name",
                    rpcError.getErrorPath());
        }
    }

    @Test
    public void testContainerWithLeafInMandatoryChoiceAbsent() throws NetconfMessageBuilderException {

        // Invalid case
        try {
            EditConfigRequest invalidEditConfigRequest = DocumentToPojoTransformer.getEditConfig(DocumentUtils
                    .loadXmlDocument(MandatoryTypeTypeValidatorTest.class
                            .getResourceAsStream(CONTAINER_WITH_LEAF_IN_MANDATORY_CHOICE_ABSENT_XML)));
            m_editConfigValidator.validate(invalidEditConfigRequest, RequestType.EDIT_CONFIG);
            fail(INVALID_REQUEST_SUCCESS + invalidEditConfigRequest.requestToString());
        } catch (ValidationException e) {
            NetconfRpcError rpcError = e.getRpcError();
            assertEquals(NetconfRpcErrorTag.DATA_MISSING, rpcError.getErrorTag());
            assertTrue(rpcError.getErrorInfo().getElementsByTagName(NetconfRpcErrorInfo.MissingLeaf.value()) != null);
            assertEquals("creator", rpcError.getErrorInfo().getElementsByTagName(NetconfRpcErrorInfo.MissingLeaf
                    .value()).item(0).getTextContent());
            assertEquals("/jbox:jukebox/jbox:presence-container/jbox:non-presence-player-with-choice/jbox:info/jbox" +
                    ":creator", rpcError.getErrorPath());
        }
    }

    @Test
    public void testContainerWithMandatoryChoiceAbsent() throws NetconfMessageBuilderException {

        // Invalid case
        try {
            EditConfigRequest invalidEditConfigRequest = DocumentToPojoTransformer.getEditConfig(DocumentUtils
                    .loadXmlDocument(MandatoryTypeTypeValidatorTest.class
                            .getResourceAsStream(CONTAINER_WITH_MANDATORY_CHOICE_ABSENT_XML)));
            m_editConfigValidator.validate(invalidEditConfigRequest, RequestType.EDIT_CONFIG);
            fail(INVALID_REQUEST_SUCCESS + invalidEditConfigRequest.requestToString());
        } catch (ValidationException e) {
            NetconfRpcError rpcError = e.getRpcError();
            assertEquals(NetconfRpcErrorTag.DATA_MISSING, rpcError.getErrorTag());
            assertTrue(rpcError.getErrorInfo().getElementsByTagName(NetconfRpcErrorInfo.MissingLeaf.value()) != null);
            assertEquals("play", rpcError.getErrorInfo().getElementsByTagName(NetconfRpcErrorInfo.MissingChoice.value
                    ()).item(0).getTextContent());
            assertEquals("/jbox:jukebox/jbox:presence-container/jbox:non-presence-player-with-choice/jbox:play",
                    rpcError.getErrorPath());
        }
    }

    @Test
    public void testMandatoryChoiceValidationInList() throws NetconfMessageBuilderException {

        // Invalid case
        EditConfigRequest invalidEditConfigRequest;
        try {
            invalidEditConfigRequest = DocumentToPojoTransformer.getEditConfig(DocumentUtils
                    .loadXmlDocument(MandatoryTypeTypeValidatorTest.class
                            .getResourceAsStream(MANDATORY_CHOICE_ABSENT_IN_LIST_XML)));
            m_editConfigValidator.validate(invalidEditConfigRequest, RequestType.EDIT_CONFIG);
            fail(INVALID_REQUEST_SUCCESS + invalidEditConfigRequest.requestToString());
        } catch (ValidationException e) {
            NetconfRpcError rpcError = e.getRpcError();
            assertEquals(NetconfRpcErrorTag.DATA_MISSING, rpcError.getErrorTag());
            assertTrue(rpcError.getErrorInfo().getElementsByTagName(NetconfRpcErrorInfo.MissingChoice.value()) != null);
            assertEquals("play", rpcError.getErrorInfo().getElementsByTagName(NetconfRpcErrorInfo.MissingChoice.value
                    ()).item(0).getTextContent());
            assertEquals("/jbox:jukebox/jbox:library/jbox:artist[jbox:name='Lenny']/jbox:album[jbox:name='Greatest " +
                    "hits']/jbox:song[jbox:name='Are you gonne go my way']/jbox:play", rpcError.getErrorPath());
        }

        // Invalid case
        try {
            invalidEditConfigRequest = DocumentToPojoTransformer.getEditConfig(DocumentUtils
                    .loadXmlDocument(MandatoryTypeTypeValidatorTest.class
                            .getResourceAsStream(MANDATORY_CHOICE_PRESENT_WITH_INVALID_ELEMENT_IN_LIST_XML)));
            m_editConfigValidator.validate(invalidEditConfigRequest, RequestType.EDIT_CONFIG);
            fail(INVALID_REQUEST_SUCCESS + invalidEditConfigRequest.requestToString());
        } catch (ValidationException e) {
            NetconfRpcError rpcError = e.getRpcError();
            assertEquals(NetconfRpcErrorTag.BAD_ELEMENT, rpcError.getErrorTag());
            assertEquals(NetconfRpcErrorType.Application, rpcError.getErrorType());
            assertEquals(NetconfRpcErrorSeverity.Error, rpcError.getErrorSeverity());
        }

        // Valid case
        EditConfigRequest validEditConfigRequest = null;
        try {
            validEditConfigRequest = DocumentToPojoTransformer.getEditConfig(DocumentUtils
                    .loadXmlDocument(MandatoryTypeTypeValidatorTest.class
                            .getResourceAsStream(MANDATORY_CHOICE_PRESENT_IN_LIST_XML)));
            m_editConfigValidator.validate(validEditConfigRequest, RequestType.EDIT_CONFIG);
        } catch (ValidationException e) {
            fail(VALID_REQUEST_FAILED + validEditConfigRequest.requestToString());
        }
    }

    @Test
    public void testMandatoryAnyXmlValidationInContainer() throws NetconfMessageBuilderException {
        // Invalid case
        EditConfigRequest invalidEditConfigRequest;
        try {
            invalidEditConfigRequest = DocumentToPojoTransformer.getEditConfig(DocumentUtils
                    .loadXmlDocument(MandatoryTypeTypeValidatorTest.class
                            .getResourceAsStream(MANDATORY_ANY_XML_ABSENT_IN_CONTAINER_XML)));
            m_editConfigValidator.validate(invalidEditConfigRequest, RequestType.EDIT_CONFIG);
            fail(INVALID_REQUEST_SUCCESS + invalidEditConfigRequest.requestToString());
        } catch (ValidationException e) {
            NetconfRpcError rpcError = e.getRpcError();
            assertEquals(NetconfRpcErrorTag.DATA_MISSING, rpcError.getErrorTag());
            assertTrue(rpcError.getErrorInfo().getElementsByTagName(NetconfRpcErrorInfo.MissingAnyxml.value()) != null);
            assertEquals("extraInfo", rpcError.getErrorInfo().getElementsByTagName(NetconfRpcErrorInfo.MissingAnyxml
                    .value()).item(0).getTextContent());
            assertEquals("/jbox:jukebox/jbox:extraInfo", rpcError.getErrorPath());
        }

        // Valid case
        EditConfigRequest validEditConfigRequest = null;
        try {
            validEditConfigRequest = DocumentToPojoTransformer.getEditConfig(DocumentUtils
                    .loadXmlDocument(MandatoryTypeTypeValidatorTest.class
                            .getResourceAsStream(MANDATORY_ANY_XML_PRESENT_IN_CONTAINER_XML)));
            m_editConfigValidator.validate(validEditConfigRequest, RequestType.EDIT_CONFIG);
        } catch (ValidationException e) {
            fail(VALID_REQUEST_FAILED + e.getRpcError().toString());
        }
    }

    @Test
    public void testMandatoryAnyXmlValidationInList() throws NetconfMessageBuilderException {
        // Invalid case
        EditConfigRequest invalidEditConfigRequest;
        try {
            invalidEditConfigRequest = DocumentToPojoTransformer.getEditConfig(DocumentUtils
                    .loadXmlDocument(MandatoryTypeTypeValidatorTest.class
                            .getResourceAsStream(MANDATORY_ANY_XML_ABSENT_IN_LIST_XML)));
            m_editConfigValidator.validate(invalidEditConfigRequest, RequestType.EDIT_CONFIG);
            fail(INVALID_REQUEST_SUCCESS + invalidEditConfigRequest.requestToString());
        } catch (ValidationException e) {
            NetconfRpcError rpcError = e.getRpcError();
            assertEquals(NetconfRpcErrorTag.DATA_MISSING, rpcError.getErrorTag());
            assertTrue(rpcError.getErrorInfo().getElementsByTagName(NetconfRpcErrorInfo.MissingAnyxml.value()) != null);
            assertEquals("extraInfo", rpcError.getErrorInfo().getElementsByTagName(NetconfRpcErrorInfo.MissingAnyxml
                    .value()).item(0).getTextContent());
            assertEquals("/jbox:jukebox/jbox:library/jbox:artist[jbox:name='Lenny']/jbox:album[jbox:name='Greatest " +
                    "hits']/jbox:song[jbox:name='Are you gonne go my way']/jbox:extraInfo", rpcError.getErrorPath());
        }

        // Valid case
        EditConfigRequest validEditConfigRequest = null;
        try {
            validEditConfigRequest = DocumentToPojoTransformer.getEditConfig(DocumentUtils
                    .loadXmlDocument(MandatoryTypeTypeValidatorTest.class
                            .getResourceAsStream(MANDATORY_ANY_XML_PRESENT_IN_LIST_XML)));
            m_editConfigValidator.validate(validEditConfigRequest, RequestType.EDIT_CONFIG);
        } catch (ValidationException e) {
            fail(VALID_REQUEST_FAILED + validEditConfigRequest.requestToString());
        }
    }
}
