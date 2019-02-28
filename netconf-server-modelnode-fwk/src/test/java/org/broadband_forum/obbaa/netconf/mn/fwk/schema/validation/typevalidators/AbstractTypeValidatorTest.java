package org.broadband_forum.obbaa.netconf.mn.fwk.schema.validation.typevalidators;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.RpcRequestConstraintParser;
import org.junit.BeforeClass;
import org.mockito.Mockito;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;

import org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.parser.YangParserUtil;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DSExpressionValidator;
import org.broadband_forum.obbaa.netconf.server.rpc.RequestType;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;

public abstract class AbstractTypeValidatorTest {
	private static RpcRequestConstraintParser c_editConfigValidator;
	protected static SchemaRegistryImpl c_schemaRegistry;
	private static final Logger LOGGER = Logger.getLogger(AbstractTypeValidatorTest.class);
	private static ModelNodeDataStoreManager m_modelNodeDsm;

	@BeforeClass
	public static void initialValidator() throws SchemaBuildException {
		c_schemaRegistry = new SchemaRegistryImpl(Collections.<YangTextSchemaSource> emptyList(), Collections.emptySet(), Collections.emptyMap(), new NoLockService());
		List<YangTextSchemaSource> yangFiles = Arrays.asList(
                YangParserUtil.getYangSource(AbstractTypeValidatorTest.class.getResource("/yangSchemaValidationTest/rpcpayloadvalidatortest/yangs/ietf-yang-types@2010-09-24.yang")),
                YangParserUtil.getYangSource(AbstractTypeValidatorTest.class.getResource("/yangSchemaValidationTest/rpcpayloadvalidatortest/yangs/type-validator-tests@2015-12-02.yang")));
		c_schemaRegistry.buildSchemaContext(yangFiles, Collections.emptySet(), Collections.emptyMap());
		m_modelNodeDsm = Mockito.mock(ModelNodeDataStoreManager.class);
		SubSystemRegistry registry = Mockito.mock(SubSystemRegistry.class);
		DSExpressionValidator expValidator = new DSExpressionValidator(c_schemaRegistry, mock(ModelNodeHelperRegistry.class), registry);
		c_editConfigValidator = new RpcRequestConstraintParser(c_schemaRegistry, m_modelNodeDsm, expValidator);
	}

	protected void testPass(String requestFile) throws NetconfMessageBuilderException {
		EditConfigRequest validEditConfigRequest = getRequestFromXmlResource(requestFile);
		testValidate(validEditConfigRequest);
	}

	protected void testCustomPass(String request) throws NetconfMessageBuilderException {
		EditConfigRequest validEditConfigRequest = getRequestFromXmlString(request);
		testValidate(validEditConfigRequest);
	}

	private void testValidate(EditConfigRequest validEditConfigRequest) {
		try {
			c_editConfigValidator.validate(validEditConfigRequest, RequestType.EDIT_CONFIG);
		} catch (ValidationException e) {
			LOGGER.error(e);
			fail("validation should NOT have thrown an exception for request: " + validEditConfigRequest.requestToString());
		}
	}

	protected void testFail(String requestFile, String expectedErrorMessage, String errorPath, String expectedErrorAppTag) throws NetconfMessageBuilderException {
		EditConfigRequest invalidEditConfigRequest = getRequestFromXmlResource(requestFile);
		testValidate(expectedErrorMessage, errorPath, expectedErrorAppTag, invalidEditConfigRequest);
	}

	private void testValidate(String expectedErrorMessage, String errorPath, String expectedErrorAppTag,
			EditConfigRequest invalidEditConfigRequest) {
		try {
			c_editConfigValidator.validate(invalidEditConfigRequest, RequestType.EDIT_CONFIG);
			fail("validation should have thrown an exception for request: " + invalidEditConfigRequest.requestToString());
		} catch (ValidationException e) {
			NetconfRpcError rpcError = e.getRpcError();
			assertEquals(NetconfRpcErrorTag.INVALID_VALUE, rpcError.getErrorTag());
			assertEquals(expectedErrorMessage, rpcError.getErrorMessage());
			assertEquals(errorPath, rpcError.getErrorPath());
			assertEquals(expectedErrorAppTag, rpcError.getErrorAppTag());
		}
	}

	protected void testCustomFail(String request, String expectedErrorMessage, String errorPath, String expectedErrorAppTag) throws NetconfMessageBuilderException {
		EditConfigRequest invalidEditConfigRequest = getRequestFromXmlString(request);
		testValidate(expectedErrorMessage, errorPath, expectedErrorAppTag, invalidEditConfigRequest);
	}
	
	protected EditConfigRequest getRequestFromXmlResource(String fileName) throws NetconfMessageBuilderException {
		String filePath = "/yangSchemaValidationTest/rpcpayloadvalidatortest/typeValidator/request/editconfigs/" + fileName;
		return DocumentToPojoTransformer.getEditConfig(DocumentUtils.loadXmlDocument(AbstractTypeValidatorTest.class
				.getResourceAsStream(filePath)));
	}

	protected EditConfigRequest getRequestFromXmlString(String request) throws NetconfMessageBuilderException {
		return DocumentToPojoTransformer.getEditConfig(DocumentUtils.stringToDocument(request));
	}
	
	public String formRequestString(String customMsg){
		String req1 = "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
		              + "<edit-config>"
			          + "<target>"
				      + "<running />"
			          + "</target>"
			          + "<test-option>set</test-option>"
			          + "<config>"
				      + "<validation xmlns=\"urn:org:bbf:pma\">"
	                  + "<type-validation xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" nc:operation=\"create\">"
	                  + "<id>1</id>";
		String req2 = "</type-validation>"
				      + "</validation>"
			          + "</config>"
		              + "</edit-config>"
	                  + "</rpc>";
		
		return req1 + customMsg + req2;
	}
}
