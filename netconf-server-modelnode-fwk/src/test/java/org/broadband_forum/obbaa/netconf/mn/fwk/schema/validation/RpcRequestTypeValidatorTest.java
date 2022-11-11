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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorInfo;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcRequest;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImplTest;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.RpcRequestConstraintParser;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.TimingLogger;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.validation.AbstractDataStoreValidatorTest;
import org.broadband_forum.obbaa.netconf.mn.fwk.tests.utils.XmlGenerator;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.broadband_forum.obbaa.netconf.server.rpc.RequestType;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcValidationException;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;

@RunWith(RequestScopeJunitRunner.class)
public class RpcRequestTypeValidatorTest extends AbstractDataStoreValidatorTest {

	RpcRequestConstraintParser m_rpcRequestValidator;

	@Override
	protected SchemaRegistry getSchemaRegistry() throws SchemaBuildException {
		SchemaRegistry schemaRegistry = new SchemaRegistryImpl(Collections.<YangTextSchemaSource> emptyList(), Collections.emptySet(), Collections.emptyMap(), new NoLockService());
		schemaRegistry.buildSchemaContext(SchemaRegistryImplTest.getAnvYangFiles(), Collections.emptySet(), Collections.emptyMap());
		schemaRegistry.loadSchemaContext("G.fast-plug", SchemaRegistryImplTest.getGfastYangFiles(), Collections.emptySet(), Collections.emptyMap());
		return schemaRegistry;
    }
	
	@Override
	protected String getXml() {
		return "/datastore-defaultxml.xml";
	}
	
	@BeforeClass
    public static void initializeOnce() throws SchemaBuildException {
        List<YangTextSchemaSource> yangFiles = TestUtil.getByteSources(getYang());
        m_schemaRegistry = new SchemaRegistryImpl(yangFiles, Collections.emptySet(), Collections.emptyMap(), new NoLockService());
        m_schemaRegistry.setName(SchemaRegistry.GLOBAL_SCHEMA_REGISTRY);
    }
	
	protected static List<String> getYang() {
		List<String> yangs = new ArrayList<>();
		yangs.add("/yangSchemaValidationTest/referenceyangs/anvyangs/alu-pma-types@2015-08-13.yang");
		yangs.add("/yangSchemaValidationTest/referenceyangs/anvyangs/alu-pma-swmgmt@2015-07-14.yang");
		yangs.add("/yangSchemaValidationTest/referenceyangs/anvyangs/alu-device-plugs@2015-07-14.yang");
        yangs.add("/yangSchemaValidationTest/referenceyangs/anvyangs/alu-pma-certificates@2015-07-14.yang");
        yangs.add("/yangSchemaValidationTest/referenceyangs/anvyangs/alu-pma-statistics@2015-07-14.yang");
        yangs.add("/yangSchemaValidationTest/referenceyangs/anvyangs/alu-pma-users@2015-07-14.yang");
        yangs.add("/yangSchemaValidationTest/referenceyangs/anvyangs/ietf-yang-types@2010-09-24.yang");
        yangs.add("/yangSchemaValidationTest/referenceyangs/anvyangs/ietf-inet-types@2010-09-24.yang");
        yangs.add("/yangSchemaValidationTest/referenceyangs/anvyangs/alu-pma@2015-07-14.yang");
        yangs.add("/yangSchemaValidationTest/referenceyangs/anvyangs/ietf-interfaces@2014-05-08.yang");
        yangs.add("/yangSchemaValidationTest/referenceyangs/anvyangs/alu-dpu-swmgmt@2015-07-14.yang");
        yangs.add("/yangSchemaValidationTest/referenceyangs/anvyangs/bbf-fast@2015-02-27.yang");
        yangs.add("/yangSchemaValidationTest/referenceyangs/anvyangs/iana-if-type@2014-05-08.yang");
        yangs.add("/yangSchemaValidationTest/referenceyangs/anvyangs/ietf-netconf@2011-03-08.yang");
        yangs.add("/yangSchemaValidationTest/referenceyangs/anvyangs/yuma-types@2012-06-01.yang");
        yangs.add("/yangSchemaValidationTest/referenceyangs/anvyangs/yuma-app-common@2012-08-16.yang");
        yangs.add("/yangSchemaValidationTest/referenceyangs/anvyangs/yuma-arp@2012-01-13.yang");
        yangs.add("/yangSchemaValidationTest/referenceyangs/anvyangs/yuma-interfaces@2012-01-13.yang");
        yangs.add("/yangSchemaValidationTest/referenceyangs/anvyangs/yuma-nacm@2012-10-05.yang");
        yangs.add("/yangSchemaValidationTest/referenceyangs/anvyangs/yuma-proc@2012-10-10.yang");
        yangs.add("/yangSchemaValidationTest/referenceyangs/anvyangs/yuma-system@2012-10-05.yang");
        return yangs;
	}
	
	@Override
	protected SchemaPath getSchemaPath() {
		QName qname = QName.create("urn:org:bbf2:pma", "2015-07-14", "pma");
	    return SchemaPath.create(true, qname);
	}
	
	@Before
	public void setup() throws SchemaBuildException {
		m_rpcRequestValidator = new RpcRequestConstraintParser(m_schemaRegistry, m_modelNodeDsm, m_expValidator, null);
	}

	@Test
	public void testValidateValidRpcRequest() throws ModelNodeInitException{
		//Valid rpc request
		try {
			getModelNode();
			String createDevice = "#pma:pma[@xmlns:pma='urn:org:bbf2:pma', parent='null']"
					+ "#pma:device-holder[parent='pma:pma']"
					+ "#pma:name[parent='pma:device-holder', value='OLT-1']"
					+ "#pma:device[parent='pma:device-holder']]"
					+ "#pma:device-id[parent='pma:device', value='R1.S1.S1.LT1.DPU1']"
					+ "#pma:hardware-type[parent='pma:device', value='G.FAST']"
					+ "#pma:interface-version[parent='pma:device', value='1.0']"
					+ "#pma:connection-initiator[parent='pma:device', value='device']";
			testPass(XmlGenerator.buildXml(createDevice));
			
			NetconfRpcRequest rpcRequest = DocumentToPojoTransformer.getRpcRequest(DocumentUtils.loadXmlDocument(RpcRequestTypeValidatorTest.class
					.getResourceAsStream("/yangSchemaValidationTest/rpcpayloadvalidatortest/requests/rpcs/valid-rpc-request.xml")));
			TimingLogger.withStartAndFinish(() -> m_rpcRequestValidator.validate(rpcRequest, RequestType.RPC));

		} catch (NetconfMessageBuilderException e) {
			fail("Failed in transform xml content to NetconfRpcRequest");
		} catch (RpcValidationException e) {
			fail("It cannot reach here because it is a valid NetconfRpcRequest !"+e);
		}
	}
	
	@Test
	public void testValidateUnknownElement() throws ModelNodeInitException {
		//unknown element
		try {
			getModelNode();
			NetconfRpcRequest rpcRequest = DocumentToPojoTransformer.getRpcRequest(DocumentUtils.loadXmlDocument(EditConfigTypeValidatorTest.class
					.getResourceAsStream("/yangSchemaValidationTest/rpcpayloadvalidatortest/requests/rpcs/invalid-rpc-request0.xml")));
			TimingLogger.withStartAndFinish(() -> m_rpcRequestValidator.validate(rpcRequest, RequestType.RPC));
			fail("validation should have thrown an exception for request: " + rpcRequest.requestToString());
		} catch (NetconfMessageBuilderException e) {
			fail("Failed in transform xml content to NetconfRpcRequest");
		} catch (RpcValidationException e) {
			NetconfRpcError rpcError = e.getRpcError();
			assertNotNull(rpcError);
            assertEquals(NetconfRpcErrorTag.UNKNOWN_ELEMENT, rpcError.getErrorTag());
            assertTrue(rpcError.getErrorInfo().getElementsByTagName(NetconfRpcErrorInfo.BadElement.value()) != null);
            assertEquals("invalid", rpcError.getErrorInfo().getElementsByTagName(NetconfRpcErrorInfo.BadElement.value()).item(0).getTextContent());
		}
	}

	@Test
	public void testValidateInvalidValue() throws ModelNodeInitException {
		//Invalid Value
		try {
			getModelNode();
			NetconfRpcRequest rpcRequest = DocumentToPojoTransformer.getRpcRequest(DocumentUtils.loadXmlDocument(EditConfigTypeValidatorTest.class
					.getResourceAsStream("/yangSchemaValidationTest/rpcpayloadvalidatortest/requests/rpcs/invalid-rpc-request3.xml")));
			TimingLogger.withStartAndFinish(() -> m_rpcRequestValidator.validate(rpcRequest, RequestType.RPC));
			fail("validation should have thrown an exception for request: " + rpcRequest.requestToString());
		} catch (NetconfMessageBuilderException e) {
			fail("Failed in transform xml content to NetconfRpcRequest");
		} catch (RpcValidationException e) {
			NetconfRpcError rpcError = e.getRpcError();
			assertNotNull(rpcError);
            assertEquals(NetconfRpcErrorTag.UNKNOWN_ELEMENT, rpcError.getErrorTag());
            assertEquals("An unexpected element 'pma-datastore' is present", rpcError.getErrorMessage());
		}
		}

	@Test
	public void testValidateInvalidNameSpace() throws ModelNodeInitException {
		//invalid namespace
		try {
			getModelNode();
			NetconfRpcRequest rpcRequest = DocumentToPojoTransformer.getRpcRequest(DocumentUtils.loadXmlDocument(EditConfigTypeValidatorTest.class
					.getResourceAsStream("/yangSchemaValidationTest/rpcpayloadvalidatortest/requests/rpcs/invalid-rpc-request2.xml")));
			TimingLogger.withStartAndFinish(() -> m_rpcRequestValidator.validate(rpcRequest, RequestType.RPC));
			fail("validation should have thrown an exception for request: " + rpcRequest.requestToString());
		} catch (NetconfMessageBuilderException e) {
			fail("Failed in transform xml content to NetconfRpcRequest");
		} catch (RpcValidationException e) {
			NetconfRpcError rpcError = e.getRpcError();
			assertNotNull(rpcError);
            assertEquals(NetconfRpcErrorTag.UNKNOWN_NAMESPACE, rpcError.getErrorTag());
            assertEquals("An unexpected namespace 'urn:org:bbf2:unknown' is present", rpcError.getErrorMessage());
		}
	}

	@Test
	public void testValidateMissingMandatoryInput() throws ModelNodeInitException {
		//missing mandatory input
		try {
			getModelNode();
			NetconfRpcRequest rpcRequest = DocumentToPojoTransformer.getRpcRequest(DocumentUtils.loadXmlDocument(EditConfigTypeValidatorTest.class
					.getResourceAsStream("/yangSchemaValidationTest/rpcpayloadvalidatortest/requests/rpcs/invalid-rpc-request1.xml")));
			TimingLogger.withStartAndFinish(() -> m_rpcRequestValidator.validate(rpcRequest, RequestType.RPC));
			fail("validation should have thrown an exception for request: " + rpcRequest.requestToString());
		} catch (NetconfMessageBuilderException e) {
			fail("Failed in transform xml content to NetconfRpcRequest");
		} catch (RpcValidationException e) {
			NetconfRpcError rpcError = e.getRpcError();
			assertNotNull(rpcError);
            assertEquals(NetconfRpcErrorTag.DATA_MISSING, rpcError.getErrorTag());
            assertEquals("Mandatory leaf 'device-id' is missing", rpcError.getErrorMessage());
		}
	}

}
