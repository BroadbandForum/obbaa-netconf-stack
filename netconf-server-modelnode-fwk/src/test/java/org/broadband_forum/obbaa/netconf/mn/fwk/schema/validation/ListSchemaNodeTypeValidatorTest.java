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
import static org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImplTest.getAnvYangFileNames;
import static org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImplTest.getAnvYangFiles;
import static org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImplTest.getGfastYangFileNames;
import static org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImplTest.getGfastYangFiles;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorInfo;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorMessages;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.RpcRequestConstraintParser;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeFactoryException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DSExpressionValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.YangUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.broadband_forum.obbaa.netconf.server.rpc.RequestType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.w3c.dom.NodeList;

/*
 * Indirectly test ListSchemaNodeValidator through EditConfigValidator
 */
@RunWith(RequestScopeJunitRunner.class)
public class ListSchemaNodeTypeValidatorTest {
	private RpcRequestConstraintParser m_editConfigValidator;
    private SchemaRegistryImpl m_schemaRegistry;
    private ModelNodeDataStoreManager m_modelNodeDsm;
    private static final Logger LOGGER = LogManager.getLogger(ListSchemaNodeTypeValidatorTest.class);

    @Before
    public void setUp() throws SchemaBuildException {
        m_schemaRegistry = new SchemaRegistryImpl(Collections.<YangTextSchemaSource>emptyList(), Collections.emptySet(), Collections.emptyMap(), new NoLockService());
        m_schemaRegistry.buildSchemaContext(getAnvYangFiles(), Collections.emptySet(), Collections.emptyMap());
        m_schemaRegistry.loadSchemaContext("G.fast-plug", getGfastYangFiles(), Collections.emptySet(), Collections.emptyMap());
        m_modelNodeDsm = Mockito.mock(ModelNodeDataStoreManager.class);
        m_editConfigValidator = new RpcRequestConstraintParser(m_schemaRegistry, m_modelNodeDsm, mock(DSExpressionValidator.class), null);
        getModelNode();
    }
    
    private void getModelNode() throws SchemaBuildException {
    	SubSystem subSystem = mock(SubSystem.class);
    	SubSystemRegistry subSystemRegistry = mock(SubSystemRegistry.class);
    	ModelNodeHelperRegistry modelNodeHelperRegistry = mock(ModelNodeHelperRegistry.class);
    	try {
    		YangUtils.deployInMemoryHelpers(getYang(), subSystem, modelNodeHelperRegistry, subSystemRegistry, m_schemaRegistry, m_modelNodeDsm, null, null);
    	} catch (ModelNodeFactoryException e) {}
    }

    private List<String> getYang() {
    	ArrayList<String> yangNames = new ArrayList<>();
    	yangNames.addAll(getAnvYangFileNames());
    	yangNames.addAll(getGfastYangFileNames());
    	return yangNames;
    }
    
    @Test
    public void testValidate() throws NetconfMessageBuilderException {
		try {
			EditConfigRequest invalidEditConfigRequest = DocumentToPojoTransformer.getEditConfig(DocumentUtils
					.loadXmlDocument(ListSchemaNodeTypeValidatorTest.class
							.getResourceAsStream("/yangSchemaValidationTest/rpcpayloadvalidatortest/requests/editconfigs/invalid-editconfig-request7.xml")));
			m_editConfigValidator.validate(invalidEditConfigRequest, RequestType.EDIT_CONFIG);
			fail("validation should have thrown an exception for request: " + invalidEditConfigRequest.requestToString());
		} catch (ValidationException e) {
			NetconfRpcError rpcError = e.getRpcError();
			assertEquals(NetconfRpcErrorTag.MISSING_ELEMENT, rpcError.getErrorTag());
			String errorMessage = String.format(NetconfRpcErrorMessages.EXPECTED_KEYS_IS_MISSING, "[name]");
			assertEquals(errorMessage, rpcError.getErrorMessage());
			assertEquals("/pma:pma/pma:device-holder", rpcError.getErrorPath());
			assertTrue(rpcError.getErrorInfo().getElementsByTagName(NetconfRpcErrorInfo.BadElement.value()) != null);
			assertEquals("name", rpcError.getErrorInfo().getElementsByTagName(NetconfRpcErrorInfo.BadElement.value()).item(0)
					.getTextContent());
		}
		
		
		try {
			EditConfigRequest invalidEditConfigRequest = DocumentToPojoTransformer.getEditConfig(DocumentUtils
					.loadXmlDocument(ListSchemaNodeTypeValidatorTest.class
							.getResourceAsStream("/yangSchemaValidationTest/rpcpayloadvalidatortest/requests/editconfigs/invalid-editconfig-request8.xml")));
			m_editConfigValidator.validate(invalidEditConfigRequest, RequestType.EDIT_CONFIG);
			fail("validation should have thrown an exception for request: " + invalidEditConfigRequest.requestToString());
		} catch (ValidationException e) {
			NetconfRpcError rpcError = e.getRpcError();
			assertEquals(NetconfRpcErrorTag.MISSING_ELEMENT, rpcError.getErrorTag());
			String errorMessage = String.format(NetconfRpcErrorMessages.EXPECTED_KEYS_IS_MISSING, "[device-id]");
			assertEquals(errorMessage, rpcError.getErrorMessage());
			assertEquals("/pma:pma/pma:device-holder[pma:name='OLT1']/pma:device", rpcError.getErrorPath());
			assertTrue(rpcError.getErrorInfo().getElementsByTagName(NetconfRpcErrorInfo.BadElement.value()) != null);
			assertEquals("device-id", rpcError.getErrorInfo().getElementsByTagName(NetconfRpcErrorInfo.BadElement.value()).item(0)
					.getTextContent());
		}
		
		
		try {
			EditConfigRequest invalidEditConfigRequest = DocumentToPojoTransformer.getEditConfig(DocumentUtils
					.loadXmlDocument(ListSchemaNodeTypeValidatorTest.class
							.getResourceAsStream("/yangSchemaValidationTest/rpcpayloadvalidatortest/requests/editconfigs/invalid-editconfig-request9.xml")));
			m_editConfigValidator.validate(invalidEditConfigRequest, RequestType.EDIT_CONFIG);
			fail("validation should have thrown an exception for request: " + invalidEditConfigRequest.requestToString());
		} catch (ValidationException e) {
			NetconfRpcError rpcError = e.getRpcError();
			assertEquals(NetconfRpcErrorTag.MISSING_ELEMENT, rpcError.getErrorTag());
			String errorMessage = String.format(NetconfRpcErrorMessages.EXPECTED_KEYS_IS_MISSING, "[dpu-hwver-type, dpu-tag]");
			assertEquals(errorMessage, rpcError.getErrorMessage());
			assertEquals("/pma:pma/pma:pma-swmgmt/pma:pma-svc/pma:pma-swver-ctl-dpu", rpcError.getErrorPath());
			NodeList badElement = rpcError.getErrorInfo().getElementsByTagName(NetconfRpcErrorInfo.BadElement.value());
			assertTrue(badElement != null);
			List<String> missingAttrs = Arrays.asList("dpu-hwver-type", "dpu-tag");
			assertTrue(missingAttrs.contains(badElement.item(0).getTextContent()));
			assertTrue(missingAttrs.contains(badElement.item(1).getTextContent()));
		}
    }
    
    @Test
    public void testListKeysOfCorrectOrder() throws NetconfMessageBuilderException{
		try {
			EditConfigRequest invalidEditConfigRequest = DocumentToPojoTransformer.getEditConfig(DocumentUtils
					.loadXmlDocument(ListSchemaNodeTypeValidatorTest.class
							.getResourceAsStream("/yangSchemaValidationTest/rpcpayloadvalidatortest/requests/editconfigs/invalid-editconfig-request25.xml")));
			m_editConfigValidator.validate(invalidEditConfigRequest, RequestType.EDIT_CONFIG);
			fail("validation should have thrown an exception for request: " + invalidEditConfigRequest.requestToString());
		} catch (ValidationException e) {
			NetconfRpcError rpcError = e.getRpcError();
			assertEquals(NetconfRpcErrorTag.MISSING_ELEMENT, rpcError.getErrorTag());
			String errorMessage = String.format(NetconfRpcErrorMessages.EXPECTED_KEYS_IS_MISPLACED, "[dpu-hwver-type, dpu-tag]");
			assertEquals(errorMessage, rpcError.getErrorMessage());
			assertEquals("/pma:pma/pma:pma-swmgmt/pma:pma-svc/pma:pma-swver-ctl-dpu[pma:dpu-tag='1.0'][pma:dpu-hwver-type='G.FAST']", rpcError.getErrorPath());
			NodeList badElement = rpcError.getErrorInfo().getElementsByTagName(NetconfRpcErrorInfo.BadElement.value());
			assertTrue(badElement != null);
			List<String> misplacedAttrs = Arrays.asList("dpu-hwver-type", "dpu-tag");
			assertTrue(misplacedAttrs.contains(badElement.item(0).getTextContent()));
			assertTrue(misplacedAttrs.contains(badElement.item(1).getTextContent()));
		}
    }
    
    @Test
    public void testValidateInsertAttributes() throws NetconfMessageBuilderException {
    	EditConfigRequest validEditConfigRequest = DocumentToPojoTransformer.getEditConfig(DocumentUtils
				.loadXmlDocument(ListSchemaNodeTypeValidatorTest.class
						.getResourceAsStream("/yangSchemaValidationTest/rpcpayloadvalidatortest/requests/editconfigs/valid-editconfig-request6.xml")));
    	try {

			m_editConfigValidator.validate(validEditConfigRequest, RequestType.EDIT_CONFIG);

		} catch (ValidationException e) {
			LOGGER.error(e);
			fail("Shouldn't throw the exception : " + validEditConfigRequest.toString());

		}

    	EditConfigRequest invalidEditConfigRequest = DocumentToPojoTransformer.getEditConfig(DocumentUtils
				.loadXmlDocument(ListSchemaNodeTypeValidatorTest.class
						.getResourceAsStream("/yangSchemaValidationTest/rpcpayloadvalidatortest/requests/editconfigs/invalid-editconfig-request28.xml")));
    	try {
			m_editConfigValidator.validate(invalidEditConfigRequest, RequestType.EDIT_CONFIG);
			fail("validation should have thrown an exception for request: " + invalidEditConfigRequest.requestToString());
		} catch (ValidationException e) {
			NetconfRpcError rpcError = e.getRpcError();
			assertEquals(NetconfRpcErrorTag.BAD_ELEMENT, rpcError.getErrorTag());
			assertEquals("Duplicate elements in node (urn:org:bbf2:pma?revision=2015-07-14)pma", rpcError.getErrorMessage());
		}

		invalidEditConfigRequest = DocumentToPojoTransformer.getEditConfig(DocumentUtils
				.loadXmlDocument(ListSchemaNodeTypeValidatorTest.class
						.getResourceAsStream("/yangSchemaValidationTest/rpcpayloadvalidatortest/requests/editconfigs/invalid-editconfig-request29.xml")));
		try {
			m_editConfigValidator.validate(invalidEditConfigRequest, RequestType.EDIT_CONFIG);
			fail("validation should have thrown an exception for request: " + invalidEditConfigRequest.requestToString());
		} catch (ValidationException e) {
			NetconfRpcError rpcError = e.getRpcError();
			assertEquals(NetconfRpcErrorTag.BAD_ELEMENT, rpcError.getErrorTag());
			assertEquals("Duplicate elements in node (urn:org:bbf2:pma?revision=2015-07-14)inner-container", rpcError.getErrorMessage());
		}

		invalidEditConfigRequest = DocumentToPojoTransformer.getEditConfig(DocumentUtils
				.loadXmlDocument(ListSchemaNodeTypeValidatorTest.class
						.getResourceAsStream("/yangSchemaValidationTest/rpcpayloadvalidatortest/requests/editconfigs/invalid-editconfig-request30.xml")));
		try {
			m_editConfigValidator.validate(invalidEditConfigRequest, RequestType.EDIT_CONFIG);
			fail("validation should have thrown an exception for request: " + invalidEditConfigRequest.requestToString());
		} catch (ValidationException e) {
			NetconfRpcError rpcError = e.getRpcError();
			assertEquals(NetconfRpcErrorTag.BAD_ELEMENT, rpcError.getErrorTag());
			assertEquals("Duplicate elements in node (urn:org:bbf2:pma?revision=2015-07-14)name", rpcError.getErrorMessage());
		}

    	invalidEditConfigRequest = DocumentToPojoTransformer.getEditConfig(DocumentUtils
				.loadXmlDocument(ListSchemaNodeTypeValidatorTest.class
						.getResourceAsStream("/yangSchemaValidationTest/rpcpayloadvalidatortest/requests/editconfigs/invalid-editconfig-request15.xml")));
    	try {
			
			m_editConfigValidator.validate(invalidEditConfigRequest, RequestType.EDIT_CONFIG);
			fail("validation should have thrown an exception for request: " + invalidEditConfigRequest.requestToString());
		} catch (ValidationException e) {
			LOGGER.error(e);
			NetconfRpcError rpcError = e.getRpcError();
			assertEquals("Key attribute can't be null or empty", rpcError.getErrorMessage());
			assertEquals(NetconfRpcErrorTag.BAD_ATTRIBUTE, rpcError.getErrorTag());
		}

    	invalidEditConfigRequest = DocumentToPojoTransformer.getEditConfig(DocumentUtils
				.loadXmlDocument(ListSchemaNodeTypeValidatorTest.class
						.getResourceAsStream("/yangSchemaValidationTest/rpcpayloadvalidatortest/requests/editconfigs/invalid-editconfig-request16.xml")));
    	try {
			
			m_editConfigValidator.validate(invalidEditConfigRequest, RequestType.EDIT_CONFIG);
			fail("validation should have thrown an exception for request: " + invalidEditConfigRequest.requestToString());
		} catch (ValidationException e) {
			LOGGER.error(e);
			NetconfRpcError rpcError = e.getRpcError();
			assertEquals("Key 'unknown-format' attribute is not the key predicates format.", rpcError.getErrorMessage());
			assertEquals(NetconfRpcErrorTag.BAD_ATTRIBUTE, rpcError.getErrorTag());
		}
    	
    	invalidEditConfigRequest = DocumentToPojoTransformer.getEditConfig(DocumentUtils
				.loadXmlDocument(ListSchemaNodeTypeValidatorTest.class
						.getResourceAsStream("/yangSchemaValidationTest/rpcpayloadvalidatortest/requests/editconfigs/invalid-editconfig-request17.xml")));
    	try {
			
			m_editConfigValidator.validate(invalidEditConfigRequest, RequestType.EDIT_CONFIG);
			fail("validation should have thrown an exception for request: " + invalidEditConfigRequest.requestToString());
		} catch (ValidationException e) {
			LOGGER.error(e);
			NetconfRpcError rpcError = e.getRpcError();
			assertEquals("'[name1=1]' is not a key predicate format.", rpcError.getErrorMessage());
			assertEquals(NetconfRpcErrorTag.BAD_ATTRIBUTE, rpcError.getErrorTag());
		}
    	
    	invalidEditConfigRequest = DocumentToPojoTransformer.getEditConfig(DocumentUtils
				.loadXmlDocument(ListSchemaNodeTypeValidatorTest.class
						.getResourceAsStream("/yangSchemaValidationTest/rpcpayloadvalidatortest/requests/editconfigs/invalid-editconfig-request18.xml")));
    	try {
			
			m_editConfigValidator.validate(invalidEditConfigRequest, RequestType.EDIT_CONFIG);
			fail("validation should have thrown an exception for request: " + invalidEditConfigRequest.requestToString());
		} catch (ValidationException e) {
			LOGGER.error(e);
			NetconfRpcError rpcError = e.getRpcError();
			assertEquals("There is an unknown key 'name1' in key '[name1='1']' attribute", rpcError.getErrorMessage());
			assertEquals(NetconfRpcErrorTag.BAD_ATTRIBUTE, rpcError.getErrorTag());
		}
    	
    	invalidEditConfigRequest = DocumentToPojoTransformer.getEditConfig(DocumentUtils
				.loadXmlDocument(ListSchemaNodeTypeValidatorTest.class
						.getResourceAsStream("/yangSchemaValidationTest/rpcpayloadvalidatortest/requests/editconfigs/invalid-editconfig-request19.xml")));
    	try {
			
			m_editConfigValidator.validate(invalidEditConfigRequest, RequestType.EDIT_CONFIG);
			fail("validation should have thrown an exception for request: " + invalidEditConfigRequest.requestToString());
		} catch (ValidationException e) {
			LOGGER.error(e);
			NetconfRpcError rpcError = e.getRpcError();
			assertEquals("There is an unknown prefix 'ex' in key '[ex:key1='1']' attribute", rpcError.getErrorMessage());
			assertEquals(NetconfRpcErrorTag.BAD_ATTRIBUTE, rpcError.getErrorTag());
		}
    	
    	invalidEditConfigRequest = DocumentToPojoTransformer.getEditConfig(DocumentUtils
				.loadXmlDocument(ListSchemaNodeTypeValidatorTest.class
						.getResourceAsStream("/yangSchemaValidationTest/rpcpayloadvalidatortest/requests/editconfigs/invalid-editconfig-request20.xml")));
    	try {
			
			m_editConfigValidator.validate(invalidEditConfigRequest, RequestType.EDIT_CONFIG);
			fail("validation should have thrown an exception for request: " + invalidEditConfigRequest.requestToString());
		} catch (ValidationException e) {
			LOGGER.error(e);
			NetconfRpcError rpcError = e.getRpcError();
			assertEquals("There is an unknown key 'ex:key1' in key '[ex:key1='1']' attribute", rpcError.getErrorMessage());
			assertEquals(NetconfRpcErrorTag.BAD_ATTRIBUTE, rpcError.getErrorTag());
		}
    	
    	invalidEditConfigRequest = DocumentToPojoTransformer.getEditConfig(DocumentUtils
				.loadXmlDocument(ListSchemaNodeTypeValidatorTest.class
						.getResourceAsStream("/yangSchemaValidationTest/rpcpayloadvalidatortest/requests/editconfigs/invalid-editconfig-request21.xml")));
    	try {
			m_editConfigValidator.validate(invalidEditConfigRequest, RequestType.EDIT_CONFIG);
			fail("validation should have thrown an exception for request: " + invalidEditConfigRequest.requestToString());
		} catch (ValidationException e) {
			LOGGER.error(e);
			NetconfRpcError rpcError = e.getRpcError();
			assertEquals("There is a duplicated key 'key1' in key '[key1='1'][key1='2']' attribute", rpcError.getErrorMessage());
			assertEquals(NetconfRpcErrorTag.BAD_ATTRIBUTE, rpcError.getErrorTag());
		}
    	
    	invalidEditConfigRequest = DocumentToPojoTransformer.getEditConfig(DocumentUtils
				.loadXmlDocument(ListSchemaNodeTypeValidatorTest.class
						.getResourceAsStream("/yangSchemaValidationTest/rpcpayloadvalidatortest/requests/editconfigs/invalid-editconfig-request22.xml")));
    	try {
			m_editConfigValidator.validate(invalidEditConfigRequest, RequestType.EDIT_CONFIG);
			fail("validation should have thrown an exception for request: " + invalidEditConfigRequest.requestToString());
		} catch (ValidationException e) {
			LOGGER.error(e);
			NetconfRpcError rpcError = e.getRpcError();
			assertEquals("Missing key 'key2' in key '[key1='1']' attribute", rpcError.getErrorMessage());
			assertEquals(NetconfRpcErrorTag.BAD_ATTRIBUTE, rpcError.getErrorTag());
		}
    	
    	invalidEditConfigRequest = DocumentToPojoTransformer.getEditConfig(DocumentUtils
				.loadXmlDocument(ListSchemaNodeTypeValidatorTest.class
						.getResourceAsStream("/yangSchemaValidationTest/rpcpayloadvalidatortest/requests/editconfigs/invalid-editconfig-request23.xml")));
    	try {
			m_editConfigValidator.validate(invalidEditConfigRequest, RequestType.EDIT_CONFIG);
			fail("validation should have thrown an exception for request: " + invalidEditConfigRequest.requestToString());
		} catch (ValidationException e) {
			LOGGER.error(e);
			NetconfRpcError rpcError = e.getRpcError();
			assertEquals("The argument is out of bounds <-128, 127>", rpcError.getErrorMessage());
			assertEquals(NetconfRpcErrorTag.BAD_ATTRIBUTE, rpcError.getErrorTag());
		}
        
    	invalidEditConfigRequest = DocumentToPojoTransformer.getEditConfig(DocumentUtils
    	        .loadXmlDocument(ListSchemaNodeTypeValidatorTest.class
    	                .getResourceAsStream("/yangSchemaValidationTest/rpcpayloadvalidatortest/requests/editconfigs/invalid-editconfig-request24.xml")));
        try {
            m_editConfigValidator.validate(invalidEditConfigRequest, RequestType.EDIT_CONFIG);
            fail("validation should have thrown an exception for request: " + invalidEditConfigRequest.requestToString());
        } catch (ValidationException e) {
            NetconfRpcError rpcError = e.getRpcError();
            assertEquals(NetconfRpcErrorTag.MISSING_ELEMENT, rpcError.getErrorTag());
            String errorMessage = String.format(NetconfRpcErrorMessages.EXPECTED_KEYS_IS_MISPLACED, "[device-id]");
            assertEquals(errorMessage, rpcError.getErrorMessage());
            assertEquals("/pma:pma/pma:device-holder[pma:name='OLT1']/pma:device[pma:device-id='R1.S1.LT1.P1.ONT1']", rpcError.getErrorPath());
            assertTrue(rpcError.getErrorInfo().getElementsByTagName(NetconfRpcErrorInfo.BadElement.value()) != null);
            assertEquals("device-id", rpcError.getErrorInfo().getElementsByTagName(NetconfRpcErrorInfo.BadElement.value()).item(0)
                    .getTextContent());
        }
    }
    
}
