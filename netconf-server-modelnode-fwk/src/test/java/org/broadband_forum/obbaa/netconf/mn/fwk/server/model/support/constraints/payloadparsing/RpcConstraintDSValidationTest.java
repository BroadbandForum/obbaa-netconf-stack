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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcRequest;
import org.broadband_forum.obbaa.netconf.api.messages.RpcName;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.RpcRequestConstraintParser;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeDynaBeanFactory;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.TimingLogger;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.validation.AbstractDataStoreValidatorTest;
import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcPayloadConstraintParser;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcValidationException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Element;

@RunWith(RequestScopeJunitRunner.class)
public class RpcConstraintDSValidationTest extends AbstractDataStoreValidatorTest{
	private static final Logger LOGGER = LogManager.getLogger(RpcConstraintDSValidationTest.class);

	private DummyRpcHandler m_datastoreLeafRefRpcHandler;
	private DummyRpcHandler m_datastoreMustWithDerivedFromOrSelfRpcHandler;
	private DummyRpcHandler m_datastoreMustWithCurrentRpcHandler;
	private DummyRpcHandler m_datastoreWhenWithDerivedFromOrSelfRpcHandler;
	private DummyRpcHandler m_datastoreWhenCurrentRpcHandler;
	private DummyRpcHandler m_datastoreLeafrefDSConcurrentRpcHandler;
	private DummyRpcHandler m_datastoreMaxMinLeafListRpcHandler;
	private RpcPayloadConstraintParser m_rpcConstraintParser;

	@Before
	public void setup() throws Exception{
		super.setup();
		m_datastoreMaxMinLeafListRpcHandler = new DummyRpcHandler(new RpcName("urn:org:bbf2:pma:validation","leaf-list-minmax"));
		m_datastoreLeafRefRpcHandler = new DummyRpcHandler(new RpcName("urn:org:bbf2:pma:validation","leaf-ref-validation"));
		m_datastoreMustWithDerivedFromOrSelfRpcHandler = new DummyRpcHandler(new RpcName("urn:org:bbf2:pma:validation","must-with-derived-from-or-self-validation"));
		m_datastoreMustWithCurrentRpcHandler = new DummyRpcHandler(new RpcName("urn:org:bbf2:pma:validation","must-with-current-validation"));
		m_datastoreWhenWithDerivedFromOrSelfRpcHandler = new DummyRpcHandler(new RpcName("urn:org:bbf2:pma:validation","when-with-derived-from-or-self-validation"));
		m_datastoreWhenCurrentRpcHandler = new DummyRpcHandler(new RpcName("urn:org:bbf2:pma:validation","when-with-current-validation"));
		m_datastoreLeafrefDSConcurrentRpcHandler = new DummyRpcHandler(new RpcName("urn:org:bbf2:pma:validation","leaf-ref-validation-concurrent"));
		m_rpcConstraintParser = new RpcRequestConstraintParser(m_schemaRegistry, m_modelNodeDsm, m_expValidator, null);
	}

	@Test
	public void testDSLeafRefValidationConcurrent() throws Exception {
		getModelNode();
		String editRequestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
				+ "  <test-interfaces>"
				+ "    <test-interface>"
				+ "     <name>testInterface</name>"
				+ "     <type>type1</type>"
				+ "     <version>1</version>"
				+ "    </test-interface>"
				+ "  </test-interfaces>"
				+ "</validation>";

		EditConfigRequest editRequest = createRequestFromString(editRequestXml);
		editRequest.setMessageId("1");
		NetConfResponse response2 = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, editRequest, response2);
		assertTrue(response2.isOk());

		// Verify the response
		String getResponse =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" +
						"	<data>"+
						"		<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
						"			<validation:test-interfaces>"+
						"				<validation:test-interface>"+
						"					<validation:name>testInterface</validation:name>"+
						"					<validation:type>type1</validation:type>"+
						"                   <validation:version>1</validation:version> " +
						"				</validation:test-interface>"+
						"			</validation:test-interfaces>"+
						"		</validation:validation>"+
						"	</data>"+
						"</rpc-reply>";

		verifyGet(getResponse);

		// Leaf-ref validation for rpc request input elements
		String rpcRequest = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				" <validation:leaf-ref-validation-concurrent xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
				" <validation:interface>testInterface</validation:interface> " +
				" <validation:version>1</validation:version> " +
				" </validation:leaf-ref-validation-concurrent>"+
				"</rpc>"
				;
		Element rpcElement = DocumentUtils.stringToDocument(rpcRequest).getDocumentElement();
		NetconfRpcRequest request = new NetconfRpcRequest();
		request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(rpcElement));
		request.setMessageId("1");
		m_datastoreLeafrefDSConcurrentRpcHandler.validate(m_rpcConstraintParser, request);
	}

	@Test
	public void testDSLeafRefValidationConcurrentMultiThread() throws Exception {
		getModelNode();
		String editRequestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
				+ "  <test-interfaces>"
				+ "    <test-interface>"
				+ "     <name>testInterface</name>"
				+ "     <type>type1</type>"
				+ "     <version>1</version>"
				+ "    </test-interface>"
				+ "  </test-interfaces>"
				+ "</validation>";

		EditConfigRequest editRequest = createRequestFromString(editRequestXml);
		editRequest.setMessageId("1");
		NetConfResponse response2 = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, editRequest, response2);
		assertTrue(response2.isOk());

		// Verify the response
		String getResponse =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" +
						"	<data>"+
						"		<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
						"			<validation:test-interfaces>"+
						"				<validation:test-interface>"+
						"					<validation:name>testInterface</validation:name>"+
						"					<validation:type>type1</validation:type>"+
						"                   <validation:version>1</validation:version> " +
						"				</validation:test-interface>"+
						"			</validation:test-interfaces>"+
						"		</validation:validation>"+
						"	</data>"+
						"</rpc-reply>";

		verifyGet(getResponse);

		List<Thread> threadList = new ArrayList<Thread>();
		for (int i = 0; i < 100; i++) {
			TestClass1 thread1 = new TestClass1();
			TestClass2 thread2 = new TestClass2();
			thread1.start();
			thread2.start();
			threadList.add(thread1);
			threadList.add(thread2);
		}

		for (Thread thread : threadList) {
			thread.join();
		}
	}

	private class TestClass1 extends Thread {
		@Override
		public void run() {
			RequestScope.withScope(new RequestScope.RsTemplate<Void>() {
				@Override
				public Void execute() {
					runRPCRefersDSLeafrefConcurrently();
					return null;
				}
			});
		}
	}

	private class TestClass2 extends Thread {
		@Override
		public void run() {
			RequestScope.withScope(new RequestScope.RsTemplate<Void>() {
				@Override
				public Void execute() {
					runRPCRefersDSLeafrefConcurrently();
					return null;
				}
			});
		}
	}

	private void runRPCRefersDSLeafrefConcurrently() {
		// Leaf-ref validation for rpc request input elements
		String rpcRequest = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				" <validation:leaf-ref-validation-concurrent xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
				" <validation:interface>testInterface</validation:interface> " +
				" <validation:version>1</validation:version> " +
				" </validation:leaf-ref-validation-concurrent>"+
				"</rpc>"
				;
		Element rpcElement = null;
		try {
			rpcElement = DocumentUtils.stringToDocument(rpcRequest).getDocumentElement();
		} catch (NetconfMessageBuilderException e) {
			LOGGER.error("Error while building netconf RPC input element");
		}
		NetconfRpcRequest request = new NetconfRpcRequest();
		request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(rpcElement));
		request.setMessageId("1");
		m_datastoreLeafrefDSConcurrentRpcHandler.validate(m_rpcConstraintParser, request);
	}

	@Test
	public void testDSLeafRefValidation() throws Exception {
		getModelNode();
		String editRequestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
				+ "  <test-interfaces>"
				+ "    <test-interface>"
				+ "     <name>testInterface</name>"
				+ "     <type>type1</type>"
				+ "    </test-interface>"
				+ "  </test-interfaces>"
				+ "</validation>";

		EditConfigRequest editRequest = createRequestFromString(editRequestXml);
		editRequest.setMessageId("1");
		NetConfResponse response2 = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, editRequest, response2);
		assertTrue(response2.isOk());

		// Verify the response
		String getResponse =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" +
						"	<data>"+
						"		<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
						"			<validation:test-interfaces>"+
						"				<validation:test-interface>"+
						"					<validation:name>testInterface</validation:name>"+
						"					<validation:type>type1</validation:type>"+
						"				</validation:test-interface>"+
						"			</validation:test-interfaces>"+
						"		</validation:validation>"+
						"	</data>"+
						"</rpc-reply>";

		verifyGet(getResponse);

		// Leaf-ref validation for rpc request input elements
		String rpcRequest = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				" <validation:leaf-ref-validation xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
				" <validation:interface>testInterface</validation:interface> " +
				" </validation:leaf-ref-validation>"+
				"</rpc>"
				;
		Element rpcElement = DocumentUtils.stringToDocument(rpcRequest).getDocumentElement();
		NetconfRpcRequest request = new NetconfRpcRequest();
		request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(rpcElement));
		request.setMessageId("1");
		TimingLogger.withStartAndFinish(() -> m_datastoreLeafRefRpcHandler.validate(m_rpcConstraintParser, request));
	}

	@Test
	public void testDSLeaflistMinMaxValidation_ExceedMaxRange() throws Exception {
		getModelNode();
		String rpcRequest = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				" <validation:leaf-list-minmax xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
				" <validation:minmax xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
				" <validation:noOfSongs>2</validation:noOfSongs> " +
				" <validation:noOfSongs>3</validation:noOfSongs> " +
				" <validation:noOfSongs>4</validation:noOfSongs> " +
				" <validation:noOfSongs>5</validation:noOfSongs> " +
				"<validation:testlist xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
				"<validation:testname>testname</validation:testname>" +
				"<validation:testcontainer xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
				" <validation:songs xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
				" <validation:name>test1</validation:name>" +
				" </validation:songs>" +
				" <validation:songs xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
				" <validation:name>test2</validation:name>" +
				" </validation:songs>" +
				"</validation:testcontainer>" +
				"</validation:testlist>" +
				" </validation:minmax>" +
				" </validation:leaf-list-minmax>" +
				"</rpc>";
		Element rpcElement = DocumentUtils.stringToDocument(rpcRequest).getDocumentElement();
		NetconfRpcRequest request = new NetconfRpcRequest();
		request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(rpcElement));
		request.setMessageId("1");
		try {
			TimingLogger.withStartAndFinish(() -> m_datastoreMaxMinLeafListRpcHandler.validate(m_rpcConstraintParser, request));
			fail();
		} catch (RpcValidationException exception) {
			assertEquals("Maximum elements allowed for noOfSongs is 3.", exception.getRpcError().getErrorMessage());
			assertEquals("too-many-elements", exception.getRpcError().getErrorAppTag());
			assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, exception.getRpcError().getErrorTag());
		}
	}

	@Test
	public void testDSLeaflistMinMaxValidation_MinRang() throws Exception {
		getModelNode();
		String rpcRequest = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				" <validation:leaf-list-minmax xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
				" <validation:minmax xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
				" <validation:noOfSongs>2</validation:noOfSongs> " +
				"<validation:testlist xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
				"<validation:testname>testname</validation:testname>" +
				"<validation:testcontainer xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
				" <validation:songs xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
				" <validation:name>test1</validation:name>" +
				" </validation:songs>" +
				" <validation:songs xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
				" <validation:name>test2</validation:name>" +
				" </validation:songs>" +
				"</validation:testcontainer>" +
				"</validation:testlist>" +
				" </validation:minmax>" +
				" </validation:leaf-list-minmax>" +
				"</rpc>";
		Element rpcElement = DocumentUtils.stringToDocument(rpcRequest).getDocumentElement();
		NetconfRpcRequest request = new NetconfRpcRequest();
		request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(rpcElement));
		request.setMessageId("1");
		try {
			TimingLogger.withStartAndFinish(() -> m_datastoreMaxMinLeafListRpcHandler.validate(m_rpcConstraintParser, request));
			fail();
		} catch (RpcValidationException exception) {
			assertEquals("Minimum elements required for noOfSongs is 2.", exception.getRpcError().getErrorMessage());
			assertEquals("too-few-elements", exception.getRpcError().getErrorAppTag());
			assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, exception.getRpcError().getErrorTag());
		}
	}

	@Test
	public void testDSLeaflistMinMaxValidation_NotinMinRange() throws Exception {
		getModelNode();
		String rpcRequest = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				" <validation:leaf-list-minmax xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
				" <validation:minmax xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
				"<validation:testlist xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
				"<validation:testname>testname</validation:testname>" +
				"<validation:testcontainer xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
				" <validation:songs xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
				" <validation:name>test1</validation:name>" +
				" </validation:songs>" +
				" <validation:songs xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
				" <validation:name>test2</validation:name>" +
				" </validation:songs>" +
				"</validation:testcontainer>" +
				"</validation:testlist>" +
				" </validation:minmax>" +
				" </validation:leaf-list-minmax>" +
				"</rpc>";
		Element rpcElement = DocumentUtils.stringToDocument(rpcRequest).getDocumentElement();
		NetconfRpcRequest request = new NetconfRpcRequest();
		request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(rpcElement));
		request.setMessageId("1");
		try {
			TimingLogger.withStartAndFinish(() -> m_datastoreMaxMinLeafListRpcHandler.validate(m_rpcConstraintParser, request));
			fail();
		} catch (RpcValidationException exception) {
			assertEquals("Minimum elements required for noOfSongs is 2.", exception.getRpcError().getErrorMessage());
			assertEquals("too-few-elements", exception.getRpcError().getErrorAppTag());
			assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, exception.getRpcError().getErrorTag());
		}
	}

	@Test
	public void testDSLeaflistMinMaxValidation() throws Exception {
		getModelNode();
		String rpcRequest = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				" <validation:leaf-list-minmax xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
				" <validation:minmax xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
				" <validation:noOfSongs>2</validation:noOfSongs> " +
				" <validation:noOfSongs>3</validation:noOfSongs> " +
				" <validation:noOfSongs>4</validation:noOfSongs> " +
				"<validation:testlist xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
				"<validation:testname>testname</validation:testname>" +
				"<validation:testcontainer xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
				" <validation:songs xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
				" <validation:name>test1</validation:name>" +
				" </validation:songs>" +
				" <validation:songs xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
				" <validation:name>test2</validation:name>" +
				" </validation:songs>" +
				"</validation:testcontainer>" +
				"</validation:testlist>" +
				" </validation:minmax>" +
				" </validation:leaf-list-minmax>" +
				"</rpc>";
		Element rpcElement = DocumentUtils.stringToDocument(rpcRequest).getDocumentElement();
		NetconfRpcRequest request = new NetconfRpcRequest();
		request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(rpcElement));
		request.setMessageId("1");
		TimingLogger.withStartAndFinish(() -> m_datastoreMaxMinLeafListRpcHandler.validate(m_rpcConstraintParser, request));
	}

	@Test
	public void testDSlistMaxValidation_RangeExceed() throws Exception {
		getModelNode();
		String rpcRequest = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				" <validation:leaf-list-minmax xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
				" <validation:minmax xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
				" <validation:noOfSongs>2</validation:noOfSongs> " +
				" <validation:noOfSongs>3</validation:noOfSongs> " +
				" <validation:noOfSongs>4</validation:noOfSongs> " +
				" <validation:testlist xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
				" <validation:testname>testname</validation:testname>" +
				" <validation:testcontainer xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
				" <validation:songs xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
				" <validation:name>test1</validation:name>" +
				" </validation:songs>" +
				" <validation:songs xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
				" <validation:name>test2</validation:name>" +
				" </validation:songs>" +
				" <validation:songs xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
				" <validation:name>test3</validation:name>" +
				" </validation:songs>" +
				" <validation:songs xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
				" <validation:name>test4</validation:name>" +
				" </validation:songs>" +
				"</validation:testcontainer>" +
				"</validation:testlist>" +
				" </validation:minmax>" +
				" </validation:leaf-list-minmax>" +
				"</rpc>";
		Element rpcElement = DocumentUtils.stringToDocument(rpcRequest).getDocumentElement();
		NetconfRpcRequest request = new NetconfRpcRequest();
		request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(rpcElement));
		request.setMessageId("1");
		try {
			TimingLogger.withStartAndFinish(() -> m_datastoreMaxMinLeafListRpcHandler.validate(m_rpcConstraintParser, request));
			fail();
		} catch (RpcValidationException exception) {
			assertEquals("Maximum elements allowed for songs is 3.", exception.getRpcError().getErrorMessage());
			assertEquals("too-many-elements", exception.getRpcError().getErrorAppTag());
			assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, exception.getRpcError().getErrorTag());
		}
	}

	@Test
	public void testDSlistMinValidationFails() throws Exception {
		getModelNode();
		String rpcRequest = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				" <validation:leaf-list-minmax xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
				" <validation:minmax xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
				" <validation:noOfSongs>2</validation:noOfSongs> " +
				" <validation:noOfSongs>3</validation:noOfSongs> " +
				" <validation:noOfSongs>4</validation:noOfSongs> " +
				"<validation:testlist xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
				"<validation:testname>testname</validation:testname>" +
				"<validation:testcontainer xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
				" <validation:songs xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
				" <validation:name>test1</validation:name>" +
				" </validation:songs>" +
				"</validation:testcontainer>" +
				"</validation:testlist>" +
				" </validation:minmax>" +
				" </validation:leaf-list-minmax>" +
				"</rpc>";
		Element rpcElement = DocumentUtils.stringToDocument(rpcRequest).getDocumentElement();
		NetconfRpcRequest request = new NetconfRpcRequest();
		request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(rpcElement));
		request.setMessageId("1");
		try {
			TimingLogger.withStartAndFinish(() -> m_datastoreMaxMinLeafListRpcHandler.validate(m_rpcConstraintParser, request));
			fail();
		} catch (RpcValidationException exception) {
			assertEquals("Minimum elements required for songs is 2.", exception.getRpcError().getErrorMessage());
			assertEquals("too-few-elements", exception.getRpcError().getErrorAppTag());
			assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, exception.getRpcError().getErrorTag());
		}
	}

	@Test
	public void testDSlistMinValidationFails1() throws Exception {
		getModelNode();
		String rpcRequest = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				" <validation:leaf-list-minmax xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
				" <validation:minmax xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
				" <validation:noOfSongs>2</validation:noOfSongs> " +
				" <validation:noOfSongs>3</validation:noOfSongs> " +
				" <validation:noOfSongs>4</validation:noOfSongs> " +
				"<validation:testlist xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
				"<validation:testname>testname</validation:testname>" +
				"<validation:testcontainer xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
				"</validation:testcontainer>" +
				"</validation:testlist>" +
				" </validation:minmax>" +
				" </validation:leaf-list-minmax>" +
				"</rpc>";
		Element rpcElement = DocumentUtils.stringToDocument(rpcRequest).getDocumentElement();
		NetconfRpcRequest request = new NetconfRpcRequest();
		request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(rpcElement));
		request.setMessageId("1");
		try {
			TimingLogger.withStartAndFinish(() -> m_datastoreMaxMinLeafListRpcHandler.validate(m_rpcConstraintParser, request));
			fail();
		} catch (RpcValidationException exception) {
			assertEquals("Minimum elements required for songs is 2.", exception.getRpcError().getErrorMessage());
			assertEquals("too-few-elements", exception.getRpcError().getErrorAppTag());
			assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, exception.getRpcError().getErrorTag());
		}
	}

	@Test
	public void testDSLeafRefValidation_InvalidLeafRef() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
				+ "  <test-interfaces>"
				+ "    <test-interface>"
				+ "     <name>testInterface</name>"
				+ "     <type>type1</type>"
				+ "    </test-interface>"
				+ "  </test-interfaces>"
				+ "</validation>";

		EditConfigRequest editRequest = createRequestFromString(requestXml1);
		editRequest.setMessageId("1");
		NetConfResponse response2 = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, editRequest, response2);
		assertTrue(response2.isOk());

		// Verify the response
		String getResponse =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" +
						"	<data>"+
						"		<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
						"			<validation:test-interfaces>"+
						"				<validation:test-interface>"+
						"					<validation:name>testInterface</validation:name>"+
						"					<validation:type>type1</validation:type>"+
						"				</validation:test-interface>"+
						"			</validation:test-interfaces>"+
						"		</validation:validation>"+
						"	</data>"+
						"</rpc-reply>";

		verifyGet(getResponse);

		String rpcRequest = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				" <validation:leaf-ref-validation xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
				" <validation:interface>testInterface1</validation:interface> " +
				" </validation:leaf-ref-validation>"+
				"</rpc>"
				;
		Element rpcElement = DocumentUtils.stringToDocument(rpcRequest).getDocumentElement();
		NetconfRpcRequest request = new NetconfRpcRequest();
		request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(rpcElement));
		request.setMessageId("1");
		try {
			TimingLogger.withStartAndFinish(() -> m_datastoreLeafRefRpcHandler.validate(m_rpcConstraintParser, request));
			fail();
		} catch (RpcValidationException exception){
			assertEquals("Missing required element testInterface1",exception.getRpcError().getErrorMessage());
			assertEquals("instance-required", exception.getRpcError().getErrorAppTag());
			assertEquals(NetconfRpcErrorTag.DATA_MISSING, exception.getRpcError().getErrorTag());
		}
	}

	@Test
	public void testDSValidationMustwithDerivedFromOrSelf_validIdentity() throws Exception {
		getModelNode();
		String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
				+ "  <test-interfaces>"
				+ "    <test-interface>"
				+ "     <name>testInterface</name>"
				+ "     <type>sampleType</type>"
				+"		<identity-leaf>identity1</identity-leaf>"
				+ "    </test-interface>"
				+ "  </test-interfaces>"
				+ "</validation>";

		EditConfigRequest editRequest = createRequestFromString(requestXml);
		editRequest.setMessageId("1");
		NetConfResponse response = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, editRequest, response);
		assertTrue(response.isOk());

		String getResponse =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" +
						"	<data>"+
						"		<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
						"			<validation:test-interfaces>"+
						"				<validation:test-interface>"+
						"					<validation:name>testInterface</validation:name>"+
						"					<validation:type>sampleType</validation:type>"+
						"					<validation:identity-leaf>validation:identity1</validation:identity-leaf>"+
						"				</validation:test-interface>"+
						"			</validation:test-interfaces>"+
						"		</validation:validation>"+
						"	</data>"+
						"</rpc-reply>";

		verifyGet(getResponse);

		String rpcRequestXml = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				" <validation:must-with-derived-from-or-self-validation xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
				" <validation:interface>testInterface</validation:interface> " +
				" </validation:must-with-derived-from-or-self-validation>"+
				"</rpc>"
				;
		Element rpcElement = DocumentUtils.stringToDocument(rpcRequestXml).getDocumentElement();
		DataStoreValidationUtil.setRootModelNodeAggregatorInCache(m_rootModelNodeAggregator);
		NetconfRpcRequest request = new NetconfRpcRequest();
		request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(rpcElement));
		request.setMessageId("1");
		TimingLogger.withStartAndFinish(() -> m_datastoreMustWithDerivedFromOrSelfRpcHandler.validate(m_rpcConstraintParser, request));

	}

	@Test
	public void testDSValidationMustwithDerivedFromOrSelf_InvalidIdentity() throws Exception {

		getModelNode();
		String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
				+ "  <test-interfaces>"
				+ "    <test-interface>"
				+ "     <name>testInterface</name>"
				+ "     <type>sampleType</type>"
				+"		<identity-leaf>identity1</identity-leaf>"
				+ "    </test-interface>"
				+ "    <test-interface>"
				+ "     <name>testInterface1</name>"
				+ "     <type>sampleType</type>"
				+"		<identity-leaf>identity2</identity-leaf>"
				+ "    </test-interface>"
				+ "  </test-interfaces>"
				+ "</validation>";

		EditConfigRequest editRequest = createRequestFromString(requestXml);
		editRequest.setMessageId("1");
		NetConfResponse response2 = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, editRequest, response2);
		assertTrue(response2.isOk());

		String getResponse =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" +
						"	<data>"+
						"		<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
						"			<validation:test-interfaces>"+
						"				<validation:test-interface>"+
						"					<validation:name>testInterface</validation:name>"+
						"					<validation:type>sampleType</validation:type>"+
						"					<validation:identity-leaf>validation:identity1</validation:identity-leaf>"+
						"				</validation:test-interface>"+
						"				<validation:test-interface>"+
						"					<validation:name>testInterface1</validation:name>"+
						"					<validation:type>sampleType</validation:type>"+
						"					<validation:identity-leaf>validation:identity2</validation:identity-leaf>"+
						"				</validation:test-interface>"+
						"			</validation:test-interfaces>"+
						"		</validation:validation>"+
						"	</data>"+
						"</rpc-reply>";

		verifyGet(getResponse);

		String rpcRequestxml = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				" <validation:must-with-derived-from-or-self-validation xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
				" <validation:interface>test</validation:interface> " +
				" </validation:must-with-derived-from-or-self-validation>"+
				"</rpc>"
				;
		Element rpcElement = DocumentUtils.stringToDocument(rpcRequestxml).getDocumentElement();
		DataStoreValidationUtil.setRootModelNodeAggregatorInCache(m_rootModelNodeAggregator);
		NetconfRpcRequest request = new NetconfRpcRequest();
		request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(rpcElement));
		request.setMessageId("1");
		try {
			m_datastoreMustWithDerivedFromOrSelfRpcHandler.validate(m_rpcConstraintParser, request);
			fail();
		}catch(RpcValidationException exception){
			assertEquals("Violate must constraints: derived-from-or-self(/validation:validation/validation:test-interfaces/validation:test-interface[validation:name=current()]/validation:identity-leaf,'validation:identity1')",exception.getRpcError().getErrorMessage());
			assertEquals("must-violation", exception.getRpcError().getErrorAppTag());
			assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, exception.getRpcError().getErrorTag());
		}

		rpcRequestxml = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				" <validation:must-with-derived-from-or-self-validation xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
				" <validation:interface>testInterface1</validation:interface> " +
				" </validation:must-with-derived-from-or-self-validation>"+
				"</rpc>"
				;
		rpcElement = DocumentUtils.stringToDocument(rpcRequestxml).getDocumentElement();
		DataStoreValidationUtil.setRootModelNodeAggregatorInCache(m_rootModelNodeAggregator);
		request = new NetconfRpcRequest();
		request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(rpcElement));
		request.setMessageId("1");
		try {
			m_datastoreMustWithDerivedFromOrSelfRpcHandler.validate(m_rpcConstraintParser, request);
			fail();
		}catch(RpcValidationException exception){
			assertEquals("Violate must constraints: derived-from-or-self(/validation:validation/validation:test-interfaces/validation:test-interface[validation:name=current()]/validation:identity-leaf,'validation:identity1')",exception.getRpcError().getErrorMessage());
			assertEquals("must-violation", exception.getRpcError().getErrorAppTag());
			assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, exception.getRpcError().getErrorTag());
		}
	}

	//@Test
	public void testDSValidationMustWithCurrent() throws Exception {
		getModelNode();
		String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
				+ "  <test-interfaces>"
				+ "    <test-interface>"
				+ "     <name>test</name>"
				+ "     <type>interfaceType</type>"
				+ "    </test-interface>"
				+ "    <test-interface>"
				+ "     <name>test1</name>"
				+ "     <type>interfaceType1</type>"
				+ "    </test-interface>"
				+ "  </test-interfaces>"
				+ "</validation>";

		EditConfigRequest request = createRequestFromString(requestXml);
		request.setMessageId("1");
		NetConfResponse response = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request, response);
		assertTrue(response.isOk());


		String getResponse =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" +
						"	<data>"+
						"		<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
						"			<validation:test-interfaces>"+
						"				<validation:test-interface>"+
						"					<validation:name>test</validation:name>"+
						"					<validation:type>interfaceType</validation:type>"+
						"				</validation:test-interface>"+
						"				<validation:test-interface>"+
						"					<validation:name>test1</validation:name>"+
						"					<validation:type>interfaceType1</validation:type>"+
						"				</validation:test-interface>"+
						"			</validation:test-interfaces>"+
						"		</validation:validation>"+
						"	</data>"+
						"</rpc-reply>";

		verifyGet(getResponse);

		String rpcRequestXml = "<rpc message-id=\"3\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				" <validation:must-with-current-validation xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
				" <validation:interface>test</validation:interface> " +
				" </validation:must-with-current-validation>"+
				"</rpc>"
				;
		Element rpcElement = DocumentUtils.stringToDocument(rpcRequestXml).getDocumentElement();
		NetconfRpcRequest rpcRequest = new NetconfRpcRequest();
		rpcRequest.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(rpcElement));
		rpcRequest.setMessageId("3");

		m_datastoreMustWithCurrentRpcHandler.validate(m_rpcConstraintParser, rpcRequest);

	}
	@Test
	public void testDSValidationMustWithCurrent_WrongCurrentValue() throws Exception {

		getModelNode();
		String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
				+ "  <test-interfaces>"
				+ "    <test-interface>"
				+ "     <name>test</name>"
				+ "     <type>interfaceType</type>"
				+ "    </test-interface>"
				+ "    <test-interface>"
				+ "     <name>test1</name>"
				+ "     <type>interfaceType1</type>"
				+ "    </test-interface>"
				+ "  </test-interfaces>"
				+ "</validation>";

		EditConfigRequest request = createRequestFromString(requestXml);
		request.setMessageId("1");
		NetConfResponse response = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request, response);
		assertTrue(response.isOk());

		String getResponse =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" +
						"	<data>"+
						"		<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
						"			<validation:test-interfaces>"+
						"				<validation:test-interface>"+
						"					<validation:name>test</validation:name>"+
						"					<validation:type>interfaceType</validation:type>"+
						"				</validation:test-interface>"+
						"				<validation:test-interface>"+
						"					<validation:name>test1</validation:name>"+
						"					<validation:type>interfaceType1</validation:type>"+
						"				</validation:test-interface>"+
						"			</validation:test-interfaces>"+
						"		</validation:validation>"+
						"	</data>"+
						"</rpc-reply>";

		verifyGet(getResponse);


		String rpcRequestXml = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				" <validation:must-with-current-validation xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
				" <validation:interface>test1</validation:interface> " +
				" </validation:must-with-current-validation>"+
				"</rpc>"
				;
		Element rpcElement = DocumentUtils.stringToDocument(rpcRequestXml).getDocumentElement();
		DataStoreValidationUtil.setRootModelNodeAggregatorInCache(m_rootModelNodeAggregator);
		NetconfRpcRequest rpcRequest = new NetconfRpcRequest();
		rpcRequest.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(rpcElement));
		rpcRequest.setMessageId("1");
		try {
			m_datastoreMustWithCurrentRpcHandler.validate(m_rpcConstraintParser, rpcRequest);
			fail();

		}catch(RpcValidationException exception){
			assertEquals("Violate must constraints: /validation:validation/validation:test-interfaces/validation:test-interface[validation:name=current()]/validation:type = 'interfaceType'",exception.getRpcError().getErrorMessage());
			assertEquals("must-violation", exception.getRpcError().getErrorAppTag());
			assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, exception.getRpcError().getErrorTag());
		}

		rpcRequestXml = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				" <validation:must-with-current-validation xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
				" <validation:interface>test3</validation:interface> " +
				" </validation:must-with-current-validation>"+
				"</rpc>"
				;
		rpcElement = DocumentUtils.stringToDocument(rpcRequestXml).getDocumentElement();
		rpcRequest = new NetconfRpcRequest();
		rpcRequest.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(rpcElement));
		rpcRequest.setMessageId("1");
		try {
			m_datastoreMustWithCurrentRpcHandler.validate(m_rpcConstraintParser, rpcRequest);
			fail();
		}catch(RpcValidationException exception){
			assertEquals("Violate must constraints: /validation:validation/validation:test-interfaces/validation:test-interface[validation:name=current()]/validation:type = 'interfaceType'",exception.getRpcError().getErrorMessage());
			assertEquals("must-violation", exception.getRpcError().getErrorAppTag());
			assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, exception.getRpcError().getErrorTag());
		}
	}


	@Test
	public void testDSValidationWhenwithDerivedFromOrSelf() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
				+ "  <test-interfaces>"
				+ "    <test-interface>"
				+ "     <name>test</name>"
				+ "     <type>sampleType</type>"
				+"		<identity-leaf>identity2</identity-leaf>"
				+ "    </test-interface>"
				+ "    <test-interface>"
				+ "     <name>test1</name>"
				+ "     <type>sampleType</type>"
				+"		<identity-leaf>identity1</identity-leaf>"
				+ "    </test-interface>"
				+ "  </test-interfaces>"
				+ "</validation>";

		EditConfigRequest request2 = createRequestFromString(requestXml1);
		request2.setMessageId("1");
		NetConfResponse response2 = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request2, response2);
		assertTrue(response2.isOk());

		String getResponse =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" +
						"	<data>"+
						"		<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
						"			<validation:test-interfaces>"+
						"				<validation:test-interface>"+
						"					<validation:name>test</validation:name>"+
						"					<validation:type>sampleType</validation:type>"+
						"					<validation:identity-leaf>validation:identity2</validation:identity-leaf>"+
						"				</validation:test-interface>"+
						"				<validation:test-interface>"+
						"					<validation:name>test1</validation:name>"+
						"					<validation:type>sampleType</validation:type>"+
						"					<validation:identity-leaf>validation:identity1</validation:identity-leaf>"+
						"				</validation:test-interface>"+
						"			</validation:test-interfaces>"+
						"		</validation:validation>"+
						"	</data>"+
						"</rpc-reply>";

		verifyGet(getResponse);


		String rpcRequestXml = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				" <validation:when-with-derived-from-or-self-validation xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
				" <validation:interface>test</validation:interface> " +
				"  <validation:type>type</validation:type>" +
				" </validation:when-with-derived-from-or-self-validation>"+
				"</rpc>"
				;
		Element element = DocumentUtils.stringToDocument(rpcRequestXml).getDocumentElement();
		DataStoreValidationUtil.setRootModelNodeAggregatorInCache(m_rootModelNodeAggregator);
		NetconfRpcRequest rpcRequest = new NetconfRpcRequest();
		rpcRequest.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element));
		rpcRequest.setMessageId("1");
		ModelNodeDynaBeanFactory.resetCache();
		m_datastoreWhenWithDerivedFromOrSelfRpcHandler.validate(m_rpcConstraintParser, rpcRequest);


		rpcRequestXml = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				" <validation:when-with-derived-from-or-self-validation xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
				" <validation:interface>test1</validation:interface> " +
				"  <validation:type>type</validation:type>" +
				" </validation:when-with-derived-from-or-self-validation>"+
				"</rpc>"
				;
		element = DocumentUtils.stringToDocument(rpcRequestXml).getDocumentElement();
		rpcRequest = new NetconfRpcRequest();
		rpcRequest.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element));
		rpcRequest.setMessageId("1");
		try {
			m_datastoreWhenWithDerivedFromOrSelfRpcHandler.validate(m_rpcConstraintParser, rpcRequest);
			fail();
		} catch (RpcValidationException exception) {
			assertEquals("Violate when constraints: derived-from-or-self(/validation:validation/validation:test-interfaces/validation:test-interface[validation:name=current()]/validation:identity-leaf,'validation:identity2')",exception.getRpcError().getErrorMessage());
			assertEquals("when-violation", exception.getRpcError().getErrorAppTag());
			assertEquals(NetconfRpcErrorTag.UNKNOWN_ELEMENT, exception.getRpcError().getErrorTag());
		}

	}

	@Test
	public void testDSValidationWhenwithCurrent() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
				+ "  <test-interfaces>"
				+ "    <test-interface>"
				+ "     <name>test</name>"
				+ "     <type>interface-type</type>"
				+"		<identity-leaf>identity2</identity-leaf>"
				+ "    </test-interface>"
				+ "    <test-interface>"
				+ "     <name>test1</name>"
				+ "     <type>sampleType</type>"
				+"		<identity-leaf>identity1</identity-leaf>"
				+ "    </test-interface>"
				+ "  </test-interfaces>"
				+ "</validation>";

		EditConfigRequest request2 = createRequestFromString(requestXml1);
		request2.setMessageId("1");
		NetConfResponse response2 = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request2, response2);
		assertTrue(response2.isOk());

		String getResponse =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" +
						"	<data>"+
						"		<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
						"			<validation:test-interfaces>"+
						"				<validation:test-interface>"+
						"					<validation:name>test</validation:name>"+
						"					<validation:type>interface-type</validation:type>"+
						"					<validation:identity-leaf>validation:identity2</validation:identity-leaf>"+
						"				</validation:test-interface>"+
						"				<validation:test-interface>"+
						"					<validation:name>test1</validation:name>"+
						"					<validation:type>sampleType</validation:type>"+
						"					<validation:identity-leaf>validation:identity1</validation:identity-leaf>"+
						"				</validation:test-interface>"+
						"			</validation:test-interfaces>"+
						"		</validation:validation>"+
						"	</data>"+
						"</rpc-reply>";

		verifyGet(getResponse);


		String rpcRequestXml = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				" <validation:when-with-current-validation xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
				" <validation:interface>test</validation:interface> " +
				" </validation:when-with-current-validation>"+
				"</rpc>"
				;
		Element element = DocumentUtils.stringToDocument(rpcRequestXml).getDocumentElement();
		DataStoreValidationUtil.setRootModelNodeAggregatorInCache(m_rootModelNodeAggregator);
		NetconfRpcRequest rpcRequest = new NetconfRpcRequest();
		rpcRequest.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element));
		rpcRequest.setMessageId("1");
		m_datastoreWhenCurrentRpcHandler.validate(m_rpcConstraintParser, rpcRequest);


		rpcRequestXml = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				" <validation:when-with-current-validation xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
				" <validation:interface>test1</validation:interface> " +
				"  <validation:type>type</validation:type>" +
				" </validation:when-with-current-validation>"+
				"</rpc>"
				;
		element = DocumentUtils.stringToDocument(rpcRequestXml).getDocumentElement();
		rpcRequest = new NetconfRpcRequest();
		rpcRequest.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element));
		rpcRequest.setMessageId("1");
		try {
			m_datastoreWhenCurrentRpcHandler.validate(m_rpcConstraintParser, rpcRequest);
			fail("Should not be reached here.");
		} catch (RpcValidationException rpcError) {
			assertEquals("Violate when constraints: /validation:validation/validation:test-interfaces/validation:test-interface[validation:name=current()]/validation:type = 'interface-type'",rpcError.getRpcError().getErrorMessage());
			assertEquals("when-violation", rpcError.getRpcError().getErrorAppTag());
			assertEquals(NetconfRpcErrorTag.UNKNOWN_ELEMENT, rpcError.getRpcError().getErrorTag());
		}

		rpcRequestXml = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				" <validation:when-with-current-validation xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
				" <validation:interface>test123</validation:interface> " +
				"  <validation:type>type</validation:type>" +
				" </validation:when-with-current-validation>"+
				"</rpc>"
				;
		element = DocumentUtils.stringToDocument(rpcRequestXml).getDocumentElement();
		rpcRequest = new NetconfRpcRequest();
		rpcRequest.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element));
		rpcRequest.setMessageId("1");
		try {
			m_datastoreWhenCurrentRpcHandler.validate(m_rpcConstraintParser, rpcRequest);
			fail("Should not be reached here.");
		} catch (RpcValidationException rpcError) {
			assertEquals("Violate when constraints: /validation:validation/validation:test-interfaces/validation:test-interface[validation:name=current()]/validation:type = 'interface-type'",rpcError.getRpcError().getErrorMessage());
			assertEquals("when-violation", rpcError.getRpcError().getErrorAppTag());
			assertEquals(NetconfRpcErrorTag.UNKNOWN_ELEMENT, rpcError.getRpcError().getErrorTag());
		}


	}
}
