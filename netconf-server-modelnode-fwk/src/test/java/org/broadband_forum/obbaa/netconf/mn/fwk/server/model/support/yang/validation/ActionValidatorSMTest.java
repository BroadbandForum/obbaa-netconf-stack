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
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.api.messages.ActionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.ActionResponse;
import org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorSeverity;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorType;
import org.broadband_forum.obbaa.netconf.api.server.NetconfQueryParams;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.StateAttributeGetContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.AddDefaultDataInterceptor;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregatorImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.TestActionMountSubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.YangUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@RunWith(RequestScopeJunitRunner.class)
public class ActionValidatorSMTest extends AbstractSchemaMountTest {

	private NetConfResponse m_response = new ActionResponse().setMessageId("1");
	protected AddDefaultDataInterceptor m_addDefaultDataInterceptor;

	private void doSetup() throws Exception {
	    List<YangTextSchemaSource> yangFiles = TestUtil.getByteSources(getYang());
	    SchemaRegistry registry =  new SchemaRegistryImpl(yangFiles, Collections.emptySet(), Collections.emptyMap(), new NoLockService());
		YangUtils.deployInMemoryHelpers(getYang(), getSubSystem(), m_modelNodeHelperRegistry, m_subSystemRegistry, registry, m_modelNodeDsm, null, null);
		m_rootModelNodeAggregator = new RootModelNodeAggregatorImpl(m_schemaRegistry, m_modelNodeHelperRegistry, m_modelNodeDsm, m_subSystemRegistry);
		addRootNodeHelpers();
		m_server.setRunningDataStore(m_dataStore);
		if (!m_rootModelNodeAggregator.getModelServiceRoots().isEmpty()){
			m_rootModelNode = m_rootModelNodeAggregator.getModelServiceRoots().get(0);
		}
	}

	protected void initialiseInterceptor() {
		m_addDefaultDataInterceptor = new AddDefaultDataInterceptor(m_modelNodeHelperRegistry, m_schemaRegistry, m_expValidator);
		m_addDefaultDataInterceptor.init();
	}

	protected SubSystem getSubSystem() {
		return new TestActionMountSubSystem(m_mountRegistry);
	}

	protected static List<String> getYang() {
		List<String> fileList = new ArrayList<String>();
		fileList.add("/datastorevalidatortest/yangs/test-mount-action.yang");
		return fileList;
	}

	@Test
	public void testValidActionInContainer() throws Exception {     
		doSetup();
		initialiseInterceptor();
		String req= "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				"<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
				"<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				"<xml-subtree>" +
				"<plugType>PLUG-1.0</plugType>" +
				"<schemaMountPoint>" +
				"<test:test-mount-action-container xmlns:test=\"urn:example:test-mount-action\">" +
				"<test:mount-container-reset>"+
				"<test:reset-at>2014-07-29T13:42:00Z</test:reset-at>"+
				"</test:mount-container-reset>"+
				"</test:test-mount-action-container>"+
				"</schemaMountPoint>" +
				"</xml-subtree>" +
				"</validation>" +
				"</action>"+
				"</rpc>";


		ActionRequest validActionRequest = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(req));
		m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse)m_response);
		TestUtil.assertXMLEquals(getActionResponseElement(), m_response.getResponseDocument().getDocumentElement());
	}

	@Test
	public void testActionInput_LeafWithMustConstraints() throws Exception {
		doSetup();
		initialiseInterceptor();

		//Create interface
		String editConfigRequest = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+"<validation xmlns=\"urn:org:bbf2:pma:validation\">" 
				+"<xml-subtree>" 
				+ "<plugType>PLUG-1.0</plugType>" 
				+ "	<schemaMountPoint>" 
				+ "		<interfaces xmlns=\"urn:example:test-mount-action\">" 
				+ "			<interface>"
				+ "				<name>interface1</name>" 
				+ "				<type>type1</type>" 
				+ "			</interface>"
				+ "			<interface>"
				+ "				<name>interface2</name>" 
				+ "				<type>type2</type>" 
				+ "			</interface>"
				+ "		</interfaces>"
				+ "	</schemaMountPoint>" 
				+"</xml-subtree>" 
				+"</validation>" ;

		editConfig(m_server, m_clientInfo, editConfigRequest, true);

		//verify GET request
		String response = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"+
				"<data>"+
				"	<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
				"		<validation:xml-subtree>"+
				"			<validation:currentChildTest/>"+
				"			<validation:multiContainer>"+
				"				<validation:level1>"+
				"					<validation:level2/>"+
				"				</validation:level1>"+
				"			</validation:multiContainer>"+
				"			<validation:plugType>PLUG-1.0</validation:plugType>"+
				"				<schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">"+
				"					<smt:ber-tca-profiles xmlns:smt=\"schema-mount-test\"/>"+
				"					<smt:interface-container xmlns:smt=\"schema-mount-test\"/>"+
				"					<smt:choice-case-container xmlns:smt=\"schema-mount-test\"/>"+
				"      				<smt:classifiers xmlns:smt=\"schema-mount-test\"/>" +
				"      				<smt:policies xmlns:smt=\"schema-mount-test\"/>" +
				"				    <smt:ethernet xmlns:smt=\"schema-mount-test\"/>"+
				"			        <smt:hardware xmlns:smt=\"schema-mount-test\"/>"+
				"      				<smt:hardwares xmlns:smt=\"schema-mount-test\"/>" +
				"      				<smt:hardwaresList xmlns:smt=\"schema-mount-test\"/>"+
				"					<copyconfig:copy-config-container xmlns:copyconfig=\"copy-config-test\"/>"+
				"					<if:interfaces xmlns:if=\"test-interfaces\"/>"+
				"					<test-mount:rollback-files xmlns:test-mount=\"urn:example:test-mount-action\"/>"+	
				"					<test-mount:interfaces xmlns:test-mount=\"urn:example:test-mount-action\">"+
				"						<test-mount:interface>"+
				"							<test-mount:enabled>true</test-mount:enabled>"+
				"							<test-mount:name>interface1</test-mount:name>"+
				"							<test-mount:type>type1</test-mount:type>"+
				"						</test-mount:interface>"+
				"						<test-mount:interface>"+
				"							<test-mount:enabled>true</test-mount:enabled>"+
				"							<test-mount:name>interface2</test-mount:name>"+
				"							<test-mount:type>type2</test-mount:type>"+
				"						</test-mount:interface>"+
				"					</test-mount:interfaces>"+
				"				<smt:schemaMount xmlns:smt=\"schema-mount-test\"/>"+
				"				<smt:schemaMount1 xmlns:smt=\"schema-mount-test\"/>"+
				"				<smt-cm:test-common-parent xmlns:smt-cm=\"sm-common-data-with-diff-conditions\"/>"+
				"				<test-mount:test-mount-action-container xmlns:test-mount=\"urn:example:test-mount-action\"/>"+
				"				<test-mount:test-action-with-mandatory xmlns:test-mount=\"urn:example:test-mount-action\"/>"+
				"		 		<smt:multicast xmlns:smt=\"schema-mount-test\"/>"+
				"		 		<if-ref:tconts-config xmlns:if-ref=\"https://interface-ref\"/>" +
				"                               <test-mount:test-action-maxminvalidation-leaflistandlist xmlns:test-mount=\"urn:example:test-mount-action\"/>" +
				"                               <smt:test-must-with-count xmlns:smt=\"schema-mount-test\"/>" +
				" 			</schemaMountPoint>"+
				"			<validation:test-internal-request/>"+
				"			<validation:testCurrentOnNonExistant>"+
				"				<validation:container1/>"+
				"			</validation:testCurrentOnNonExistant>"+
				"		</validation:xml-subtree>"+
				"	</validation:validation>"+
				"</data>"+
				"</rpc-reply>";
		verifyGet(response);

		String actionRequest = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				"<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
				" <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				"	<xml-subtree>" +
				"		<plugType>PLUG-1.0</plugType>" +
				"		<schemaMountPoint>" +
				"			<test:test-mount-action-container xmlns:test=\"urn:example:test-mount-action\">" +
				"				<test:action-with-must-validation>"+
				"					<test:interface>interface2</test:interface>"+
				"				</test:action-with-must-validation>"+
				"			</test:test-mount-action-container>"+
				"		</schemaMountPoint>" +
				"	</xml-subtree>" +
				" </validation>" +
				"</action>"+
				"</rpc>";

		ActionRequest validActionRequest = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(actionRequest));
		m_response = new ActionResponse().setMessageId("1");
		m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);
		//verify ok response
		TestUtil.assertXMLEquals(getActionResponseElement_OK(), m_response.getResponseDocument().getDocumentElement());


		actionRequest = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				"<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
				" <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				"	<xml-subtree>" +
				"		<plugType>PLUG-1.0</plugType>" +
				"		<schemaMountPoint>" +
				"			<test:test-mount-action-container xmlns:test=\"urn:example:test-mount-action\">" +
				"				<test:action-with-must-validation>"+
				"					<test:interface>interface1</test:interface>"+
				"				</test:action-with-must-validation>"+
				"			</test:test-mount-action-container>"+
				"		</schemaMountPoint>" +
				"	</xml-subtree>" +
				" </validation>" +
				"</action>"+
				"</rpc>";

		validActionRequest = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(actionRequest));
		m_response = new ActionResponse().setMessageId("1");
		m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);
		assertTrue(m_response.getErrors().isEmpty());

		actionRequest = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				"<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
				" <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				"	<xml-subtree>" +
				"		<plugType>PLUG-1.0</plugType>" +
				"		<schemaMountPoint>" +
				"			<test:test-mount-action-container xmlns:test=\"urn:example:test-mount-action\">" +
				"				<test:action-with-must-validation>"+
				"					<test:interface>bbf</test:interface>"+
				"				</test:action-with-must-validation>"+
				"			</test:test-mount-action-container>"+
				"		</schemaMountPoint>" +
				"	</xml-subtree>" +
				" </validation>" +
				"</action>"+
				"</rpc>";

		validActionRequest = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(actionRequest));
		m_response = new ActionResponse().setMessageId("1");
		m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);
		assertTrue(m_response.getErrors().isEmpty());
	}

	@Test
	public void testActionInput_LeafrefValidation() throws Exception {
		doSetup();
		initialiseInterceptor();

		//Create interface
		String editConfigRequest = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+"<validation xmlns=\"urn:org:bbf2:pma:validation\">" 
				+"<xml-subtree>" 
				+ "<plugType>PLUG-1.0</plugType>" 
				+ "	<schemaMountPoint>" 
				+ "		<interfaces xmlns=\"urn:example:test-mount-action\">" 
				+ "			<interface>"
				+ "				<name>interface1</name>" 
				+ "				<type>type1</type>" 
				+ "			</interface>"
				+ "			<interface>"
				+ "				<name>interface2</name>" 
				+ "				<type>type2</type>" 
				+ "			</interface>"
				+ "		</interfaces>"
				+ "	</schemaMountPoint>" 
				+"</xml-subtree>" 
				+"</validation>" ;

		editConfig(m_server, m_clientInfo, editConfigRequest, true);

		//verify GET request
		String response = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"+
				"<data>"+
				"	<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
				"		<validation:xml-subtree>"+
				"			<validation:currentChildTest/>"+
				"			<validation:multiContainer>"+
				"				<validation:level1>"+
				"					<validation:level2/>"+
				"				</validation:level1>"+
				"			</validation:multiContainer>"+
				"			<validation:plugType>PLUG-1.0</validation:plugType>"+
				"				<schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">"+
				"					<smt:ber-tca-profiles xmlns:smt=\"schema-mount-test\"/>"+
				"					<smt:interface-container xmlns:smt=\"schema-mount-test\"/>"+
				"					<smt:choice-case-container xmlns:smt=\"schema-mount-test\"/>"+
				"      				<smt:classifiers xmlns:smt=\"schema-mount-test\"/>" +
				"      				<smt:policies xmlns:smt=\"schema-mount-test\"/>" +
				"      				<smt:ethernet xmlns:smt=\"schema-mount-test\"/>" +
				"      				<smt:hardware xmlns:smt=\"schema-mount-test\"/>" +
				"      				<smt:hardwares xmlns:smt=\"schema-mount-test\"/>" +
				"      				<smt:hardwaresList xmlns:smt=\"schema-mount-test\"/>"+
				"					<copyconfig:copy-config-container xmlns:copyconfig=\"copy-config-test\"/>"+
				"					<if:interfaces xmlns:if=\"test-interfaces\"/>"+
				"					<test-mount:rollback-files xmlns:test-mount=\"urn:example:test-mount-action\"/>"+
				"					<test-mount:interfaces xmlns:test-mount=\"urn:example:test-mount-action\">"+
				"						<test-mount:interface>"+
				"							<test-mount:enabled>true</test-mount:enabled>"+
				"							<test-mount:name>interface1</test-mount:name>"+
				"							<test-mount:type>type1</test-mount:type>"+
				"						</test-mount:interface>"+
				"						<test-mount:interface>"+
				"							<test-mount:enabled>true</test-mount:enabled>"+
				"							<test-mount:name>interface2</test-mount:name>"+
				"							<test-mount:type>type2</test-mount:type>"+
				"						</test-mount:interface>"+
				"					</test-mount:interfaces>"+
				"				<smt:schemaMount xmlns:smt=\"schema-mount-test\"/>"+
				"				<smt:schemaMount1 xmlns:smt=\"schema-mount-test\"/>"+
				"				<smt-cm:test-common-parent xmlns:smt-cm=\"sm-common-data-with-diff-conditions\"/>"+
				"				<test-mount:test-mount-action-container xmlns:test-mount=\"urn:example:test-mount-action\"/>"+
				"				<test-mount:test-action-with-mandatory xmlns:test-mount=\"urn:example:test-mount-action\"/>"+
				"		 		<smt:multicast xmlns:smt=\"schema-mount-test\"/>"+
				"		 		<if-ref:tconts-config xmlns:if-ref=\"https://interface-ref\"/>"+
				"                               <test-mount:test-action-maxminvalidation-leaflistandlist xmlns:test-mount=\"urn:example:test-mount-action\"/>"+
				"                               <smt:test-must-with-count xmlns:smt=\"schema-mount-test\"/>" +
				" 			</schemaMountPoint>"+
				"			<validation:test-internal-request/>"+
				"			<validation:testCurrentOnNonExistant>"+
				"				<validation:container1/>"+
				"			</validation:testCurrentOnNonExistant>"+
				"		</validation:xml-subtree>"+
				"	</validation:validation>"+
				"</data>"+
				"</rpc-reply>";
		verifyGet(response);

		String actionRequest = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				"<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
				" <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				"	<xml-subtree>" +
				"		<plugType>PLUG-1.0</plugType>" +
				"		<schemaMountPoint>" +
				"			<test:test-mount-action-container xmlns:test=\"urn:example:test-mount-action\">" +
				"				<test:leafref-validation>"+
				"					<test:leaf1>interface1</test:leaf1>"+
				"				</test:leafref-validation>"+
				"			</test:test-mount-action-container>"+
				"		</schemaMountPoint>" +
				"	</xml-subtree>" +
				" </validation>" +
				"</action>"+
				"</rpc>";

		ActionRequest validActionRequest = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(actionRequest));
		m_response = new ActionResponse().setMessageId("1");
		m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);
		//verify ok response
		TestUtil.assertXMLEquals(getActionResponseElement_OK(), m_response.getResponseDocument().getDocumentElement());


		actionRequest = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				"<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
				" <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				"	<xml-subtree>" +
				"		<plugType>PLUG-1.0</plugType>" +
				"		<schemaMountPoint>" +
				"			<test:test-mount-action-container xmlns:test=\"urn:example:test-mount-action\">" +
				"				<test:leafref-validation>"+
				"					<test:leaf1>interface1</test:leaf1>"+
				"					<test:leaf1>interface2</test:leaf1>"+
				"				</test:leafref-validation>"+
				"			</test:test-mount-action-container>"+
				"		</schemaMountPoint>" +
				"	</xml-subtree>" +
				" </validation>" +
				"</action>"+
				"</rpc>";

		validActionRequest = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(actionRequest));
		m_response = new ActionResponse().setMessageId("1");
		m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);
		//verify ok response
		TestUtil.assertXMLEquals(getActionResponseElement_OK(), m_response.getResponseDocument().getDocumentElement());

		actionRequest = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				"<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
				" <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				"	<xml-subtree>" +
				"		<plugType>PLUG-1.0</plugType>" +
				"		<schemaMountPoint>" +
				"			<test:test-mount-action-container xmlns:test=\"urn:example:test-mount-action\">" +
				"				<test:leafref-validation>"+
				"					<test:leaf1>interface1</test:leaf1>"+
				"					<test:leaf1>bbf</test:leaf1>"+
				"				</test:leafref-validation>"+
				"			</test:test-mount-action-container>"+
				"		</schemaMountPoint>" +
				"	</xml-subtree>" +
				" </validation>" +
				"</action>"+
				"</rpc>";

		validActionRequest = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(actionRequest));
		m_response = new ActionResponse().setMessageId("1");
		m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);
		assertTrue(m_response.getErrors().isEmpty());
	}


	@Test
	public void testActionInput_GroupingWithChoiceAndMustConstraints() throws Exception {
		doSetup();
		initialiseInterceptor();

		String editConfigRequest = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+"<validation xmlns=\"urn:org:bbf2:pma:validation\">" 
				+"<xml-subtree>" 
				+ "<plugType>PLUG-1.0</plugType>" 
				+ "	<schemaMountPoint>" 
				+ "		<rollback-files xmlns=\"urn:example:test-mount-action\">" 
				+ "		</rollback-files>"
				+ "	</schemaMountPoint>" 
				+"</xml-subtree>" 
				+"</validation>" ;

		editConfig(m_server, m_clientInfo, editConfigRequest, true);

		String actionRequest = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				"<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
				" <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				"	<xml-subtree>" +
				"		<plugType>PLUG-1.0</plugType>" +
				"		<schemaMountPoint>" +
				"				<test:rollback-files xmlns:test=\"urn:example:test-mount-action\">"+
				"					<test:apply-rollback-file>"+
				"						<test:city>chennai</test:city>"+
				"					</test:apply-rollback-file>"+
				"				</test:rollback-files>"+ 
				"		</schemaMountPoint>" +
				"	</xml-subtree>" +
				" </validation>" +
				"</action>"+
				"</rpc>";


		ActionRequest validActionRequest = DocumentToPojoTransformer
				.getAction(DocumentUtils.stringToDocument(actionRequest));
		m_response = new ActionResponse().setMessageId("1");
		m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);
		assertTrue(m_response.getErrors().isEmpty());

		editConfigRequest = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+"<validation xmlns=\"urn:org:bbf2:pma:validation\">" 
				+"<xml-subtree>" 
				+ "<plugType>PLUG-1.0</plugType>" 
				+ "	<schemaMountPoint>" 
				+ "		<rollback-files xmlns=\"urn:example:test-mount-action\">" 
				+ "			<file>"
				+ "				<id>10</id>"
				+ "				<name>songs</name>" 
				+ "			</file>"
				+ "		</rollback-files>"
				+ "	</schemaMountPoint>" 
				+"</xml-subtree>" 
				+"</validation>" ;

		editConfig(m_server, m_clientInfo, editConfigRequest, true);


		//verify GET request
		String response = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"+
				"<data>"+
				"	<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
				"		<validation:xml-subtree>"+
				"			<validation:currentChildTest/>"+
				"			<validation:multiContainer>"+
				"				<validation:level1>"+
				"					<validation:level2/>"+
				"				</validation:level1>"+
				"			</validation:multiContainer>"+
				"			<validation:plugType>PLUG-1.0</validation:plugType>"+
				"				<schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">"+
				"					<smt:ber-tca-profiles xmlns:smt=\"schema-mount-test\"/>"+
				"					<smt:interface-container xmlns:smt=\"schema-mount-test\"/>"+
				"					<smt:choice-case-container xmlns:smt=\"schema-mount-test\"/>"+
				"      				<smt:classifiers xmlns:smt=\"schema-mount-test\"/>"+
				"      				<smt:policies xmlns:smt=\"schema-mount-test\"/>"+
				"      				<smt:ethernet xmlns:smt=\"schema-mount-test\"/>" +
				"      				<smt:hardware xmlns:smt=\"schema-mount-test\"/>" +
				"      				<smt:hardwares xmlns:smt=\"schema-mount-test\"/>"+
				"     				<smt:hardwaresList xmlns:smt=\"schema-mount-test\"/>"+
				"					<copyconfig:copy-config-container xmlns:copyconfig=\"copy-config-test\"/>"+
				"					<if:interfaces xmlns:if=\"test-interfaces\"/>"+
				"					<test-mount:rollback-files xmlns:test-mount=\"urn:example:test-mount-action\">" +
				"						<test-mount:file>"+
				"							<test-mount:id>10</test-mount:id>"+
				"							<test-mount:name>songs</test-mount:name>" +
				"						</test-mount:file>"+
				"					</test-mount:rollback-files>"+
				"				<smt:schemaMount xmlns:smt=\"schema-mount-test\"/>"+
				"				<smt:schemaMount1 xmlns:smt=\"schema-mount-test\"/>"+
				"		 		<smt:multicast xmlns:smt=\"schema-mount-test\"/>"+
				"				<smt-cm:test-common-parent xmlns:smt-cm=\"sm-common-data-with-diff-conditions\"/>"+
				"				<test-mount:test-action-with-mandatory xmlns:test-mount=\"urn:example:test-mount-action\"/>"+
				"				<test-mount:test-mount-action-container xmlns:test-mount=\"urn:example:test-mount-action\"/>"+
				"					<test-mount:interfaces xmlns:test-mount=\"urn:example:test-mount-action\"/>"+
				"				<if-ref:tconts-config xmlns:if-ref=\"https://interface-ref\"/>" +
				"                               <test-mount:test-action-maxminvalidation-leaflistandlist xmlns:test-mount=\"urn:example:test-mount-action\"/>" +
				"                               <smt:test-must-with-count xmlns:smt=\"schema-mount-test\"/>" +
				" 			</schemaMountPoint>" +
				"			<validation:test-internal-request/>"+
				"			<validation:testCurrentOnNonExistant>"+
				"				<validation:container1/>"+
				"			</validation:testCurrentOnNonExistant>"+
				"		</validation:xml-subtree>"+
				"	</validation:validation>"+
				"</data>"+
				"</rpc-reply>";
		verifyGet(response);


		actionRequest = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				"<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
				" <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				"	<xml-subtree>" +
				"		<plugType>PLUG-1.0</plugType>" +
				"		<schemaMountPoint>" +
				"				<test:rollback-files xmlns:test=\"urn:example:test-mount-action\">"+
				"					<test:apply-rollback-file>"+
				"						<test:id>10</test:id>"+
				"						<test:city>chennai</test:city>"+
				"					</test:apply-rollback-file>"+
				"				</test:rollback-files>"+ 
				"		</schemaMountPoint>" +
				"	</xml-subtree>" +
				" </validation>" +
				"</action>"+
				"</rpc>";


		validActionRequest = DocumentToPojoTransformer
				.getAction(DocumentUtils.stringToDocument(actionRequest));
		m_response = new ActionResponse().setMessageId("1");
		m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);
		TestUtil.assertXMLEquals(getActionResponseElement_OK(), m_response.getResponseDocument().getDocumentElement());

		actionRequest = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				"<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
				" <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				"	<xml-subtree>" +
				"		<plugType>PLUG-1.0</plugType>" +
				"		<schemaMountPoint>" +
				"				<test:rollback-files xmlns:test=\"urn:example:test-mount-action\">"+
				"					<test:apply-rollback-file>"+
				"						<test:id>20</test:id>"+
				"						<test:city>chennai</test:city>"+
				"					</test:apply-rollback-file>"+
				"				</test:rollback-files>"+ 
				"		</schemaMountPoint>" +
				"	</xml-subtree>" +
				" </validation>" +
				"</action>"+
				"</rpc>";

		validActionRequest = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(actionRequest));
		m_response = new ActionResponse().setMessageId("1");
		m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);
		assertTrue(m_response.getErrors().isEmpty());
	}

	@Test
	public void testAction_SkipWhenValidationForStateLeaf() throws Exception {
		doSetup();
		initialiseInterceptor();

		String editConfigRequest = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+"<validation xmlns=\"urn:org:bbf2:pma:validation\">" 
				+"<xml-subtree>" 
				+ "<plugType>PLUG-1.0</plugType>" 
				+ "	<schemaMountPoint>" 
				+ "		<rollback-files xmlns=\"urn:example:test-mount-action\">" 
				+ "		</rollback-files>"
				+ "	</schemaMountPoint>" 
				+"</xml-subtree>" 
				+"</validation>" ;

		editConfig(m_server, m_clientInfo, editConfigRequest, true);

		String actionRequest = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				"<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
				" <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				"	<xml-subtree>" +
				"		<plugType>PLUG-1.0</plugType>" +
				"		<schemaMountPoint>" +
				"				<test:rollback-files xmlns:test=\"urn:example:test-mount-action\">"+
				"					<test:track-container>"+
				"						<test:file-container>"+
				"							<test:activate/>"+
				"						</test:file-container>"+
				"					</test:track-container>"+
				"				</test:rollback-files>"+ 
				"		</schemaMountPoint>" +
				"	</xml-subtree>" +
				" </validation>" +
				"</action>"+
				"</rpc>";


		ActionRequest validActionRequest = DocumentToPojoTransformer
				.getAction(DocumentUtils.stringToDocument(actionRequest));
		m_response = new ActionResponse().setMessageId("1");
		m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);
		TestUtil.assertXMLEquals(getActionResponseElement_OK(), m_response.getResponseDocument().getDocumentElement());

		actionRequest = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				"<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
				" <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				"	<xml-subtree>" +
				"		<plugType>PLUG-1.0</plugType>" +
				"		<schemaMountPoint>" +
				"				<test:rollback-files xmlns:test=\"urn:example:test-mount-action\">"+
				"					<test:track-container>"+
				"						<test:file-container>"+
				"							<test:activate/>"+
				"						</test:file-container>"+
				"						<test:track>true</test:track>"+
				"					</test:track-container>"+
				"				</test:rollback-files>"+ 
				"		</schemaMountPoint>" +
				"	</xml-subtree>" +
				" </validation>" +
				"</action>"+
				"</rpc>";


		validActionRequest = DocumentToPojoTransformer
				.getAction(DocumentUtils.stringToDocument(actionRequest));
		m_response = new ActionResponse().setMessageId("1");
		m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);
		TestUtil.assertXMLEquals(getActionResponseElement_OK(), m_response.getResponseDocument().getDocumentElement());
	}

	@Test
	public void testAction_WhenValidationForConfigLeaf() throws Exception {
		doSetup();
		initialiseInterceptor();

		String editConfigRequest = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+"<validation xmlns=\"urn:org:bbf2:pma:validation\">" 
				+"<xml-subtree>" 
				+ "<plugType>PLUG-1.0</plugType>" 
				+ "	<schemaMountPoint>" 
				+ "		<rollback-files xmlns=\"urn:example:test-mount-action\">" 
				+ "		</rollback-files>"
				+ "	</schemaMountPoint>" 
				+"</xml-subtree>" 
				+"</validation>" ;

		editConfig(m_server, m_clientInfo, editConfigRequest, true);

		String actionRequest = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				"<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
				" <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				"	<xml-subtree>" +
				"		<plugType>PLUG-1.0</plugType>" +
				"		<schemaMountPoint>" +
				"				<test:rollback-files xmlns:test=\"urn:example:test-mount-action\">"+
				"					<test:track-container>"+
				"						<test:config-container>"+
				"							<test:deactivate/>"+
				"						</test:config-container>"+
				"					</test:track-container>"+
				"				</test:rollback-files>"+ 
				"		</schemaMountPoint>" +
				"	</xml-subtree>" +
				" </validation>" +
				"</action>"+
				"</rpc>";

		ActionRequest validActionRequest = DocumentToPojoTransformer
				.getAction(DocumentUtils.stringToDocument(actionRequest));
		m_response = new ActionResponse().setMessageId("1");
		m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);
		assertTrue(m_response.getErrors().isEmpty());

		actionRequest = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				"<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
				" <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				"	<xml-subtree>" +
				"		<plugType>PLUG-1.0</plugType>" +
				"		<schemaMountPoint>" +
				"				<test:rollback-files xmlns:test=\"urn:example:test-mount-action\">"+
				"					<test:track-container>"+
				"						<test:config-container>"+
				"							<test:deactivate/>"+
				"						</test:config-container>"+
				"					</test:track-container>"+
				"					<test:config-leaf>false</test:config-leaf>"+
				"				</test:rollback-files>"+ 
				"		</schemaMountPoint>" +
				"	</xml-subtree>" +
				" </validation>" +
				"</action>"+
				"</rpc>";


		validActionRequest = DocumentToPojoTransformer
				.getAction(DocumentUtils.stringToDocument(actionRequest));
		m_response = new ActionResponse().setMessageId("1");
		m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);
		assertTrue(m_response.getErrors().isEmpty());

		actionRequest = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				"<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
				" <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				"	<xml-subtree>" +
				"		<plugType>PLUG-1.0</plugType>" +
				"		<schemaMountPoint>" +
				"				<test:rollback-files xmlns:test=\"urn:example:test-mount-action\">"+
				"					<test:track-container>"+
				"						<test:config-container>"+
				"							<test:deactivate/>"+
				"						</test:config-container>"+
				"					</test:track-container>"+
				"					<test:config-leaf>true</test:config-leaf>"+
				"				</test:rollback-files>"+ 
				"		</schemaMountPoint>" +
				"	</xml-subtree>" +
				" </validation>" +
				"</action>"+
				"</rpc>";


		validActionRequest = DocumentToPojoTransformer
				.getAction(DocumentUtils.stringToDocument(actionRequest));
		m_response = new ActionResponse().setMessageId("1");
		m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);
		TestUtil.assertXMLEquals(getActionResponseElement_OK(), m_response.getResponseDocument().getDocumentElement());
	}

	@Test
	public void testActionInput_SkipLeafrefAndMustConstaintsValidationForStateLeaf() throws Exception {
		doSetup();
		initialiseInterceptor();

		//mock state atrribute data
		QName testStateList = QName.create("urn:example:test-mount-action", "2018-01-03", "interfaces-state");
		SchemaPath schemaPath = SchemaPath.create(true, testStateList );
		SubSystem testStateListSubsystem = mock(SubSystem.class);
		SubSystemRegistry subSystemRegistry = m_provider.getSubSystemRegistry(m_modelNode.getModelNodeId());
		subSystemRegistry.register("state", schemaPath , testStateListSubsystem );
		String entry1 =
				"  <test:interfaces-state xmlns:test=\"urn:example:test-mount-action\">"
						+ "    <test:interface>"
						+ "			<test:name>interface1</test:name>"
						+"			<test:type>identity1</test:type>"
						+ "      </test:interface>"
						+ "  </test:interfaces-state>";

		Element entry1Element = DocumentUtils.stringToDocument(entry1).getDocumentElement();
		List<Element> resultElements = new ArrayList<>();
		resultElements.add(entry1Element);

		doAnswer(new Answer<Map<ModelNodeId, List<Element>>>() {
			@Override
			public Map<ModelNodeId, List<Element>> answer(InvocationOnMock invocation) throws Throwable {
				Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> map = (Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>>) invocation.getArguments()[0];
				ModelNodeId modelNodeId = map.keySet().iterator().next();
				Map<ModelNodeId, List<Element>> result = new HashMap<>();
				result.put(modelNodeId, resultElements);
				return result;
			}
		}).when(testStateListSubsystem).retrieveStateAttributes(anyMap(), any(NetconfQueryParams.class),any(StateAttributeGetContext.class));

		String request =
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
						" <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
						"  <xml-subtree>" +
						"  <plugType>PLUG-1.0</plugType>" +
						"   <schemaMountPoint>" +
						"    <interfaces xmlns=\"urn:example:test-mount-action\">" +
						"     <interface>" +
						"         <name>key</name>" +
						"         <type>type</type>" +
						"     </interface>" +
						"    </interfaces>" +
						"   </schemaMountPoint>" +
						"  </xml-subtree>" +
						" </validation>"
						;
		editConfig(request);

		String filterOnStateListKey = 
				" <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
						+"   <validation:xml-subtree>"
						+ "     <validation:plugType>PLUG-1.0</validation:plugType>" 
						+"    <validation:schemaMountPoint>"
						+      "<test:interfaces-state xmlns:test=\"urn:example:test-mount-action\">"
						+"        <test:interface/>"
						+"      </test:interfaces-state>"
						+"   </validation:schemaMountPoint>"
						+"  </validation:xml-subtree>"
						+" </validation:validation>";

		//Verify GET request for state data
		String response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\"> " +
						" <data> " +
						"  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"> " +
						"   <validation:xml-subtree> " +
						"     <validation:plugType>PLUG-1.0</validation:plugType>" + 
						"    <schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\"> " +
						"		<test:interfaces-state xmlns:test=\"urn:example:test-mount-action\">"+
						"			<test:interface>"+
						"				<test:name>interface1</test:name>"+
						"				<test:type>identity1</test:type>"+
						"			</test:interface>"+
						"		</test:interfaces-state>"+
						"    </schemaMountPoint> " +
						"   </validation:xml-subtree> " +
						"  </validation:validation> " +
						" </data> " +
						"</rpc-reply>";

		verifyGet(m_server, m_clientInfo, filterOnStateListKey, response);

		// skip must validation for valid interface
		String actionRequest = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				"<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
				" <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				"	<xml-subtree>" +
				"		<plugType>PLUG-1.0</plugType>" +
				"		<schemaMountPoint>" +
				"				<test:test-mount-action-container xmlns:test=\"urn:example:test-mount-action\">"+
				"					<test:test-action-must-with-state-value>"+
				"						<test:interface>interface1</test:interface>"+
				"					</test:test-action-must-with-state-value>"+
				"				</test:test-mount-action-container>"+ 
				"		</schemaMountPoint>" +
				"	</xml-subtree>" +
				" </validation>" +
				"</action>"+
				"</rpc>";

		ActionRequest validActionRequest = DocumentToPojoTransformer
				.getAction(DocumentUtils.stringToDocument(actionRequest));
		m_response = new ActionResponse().setMessageId("1");
		m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);
		TestUtil.assertXMLEquals(getActionResponseElement_OK(), m_response.getResponseDocument().getDocumentElement());

		// skip must validation for invalid interface 
		actionRequest = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				"<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
				" <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				"	<xml-subtree>" +
				"		<plugType>PLUG-1.0</plugType>" +
				"		<schemaMountPoint>" +
				"				<test:test-mount-action-container xmlns:test=\"urn:example:test-mount-action\">"+
				"					<test:test-action-must-with-state-value>"+
				"						<test:interface>interface5</test:interface>"+
				"					</test:test-action-must-with-state-value>"+
				"				</test:test-mount-action-container>"+ 
				"		</schemaMountPoint>" +
				"	</xml-subtree>" +
				" </validation>" +
				"</action>"+
				"</rpc>";

		validActionRequest = DocumentToPojoTransformer
				.getAction(DocumentUtils.stringToDocument(actionRequest));
		m_response = new ActionResponse().setMessageId("1");
		m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);
		TestUtil.assertXMLEquals(getActionResponseElement_OK(), m_response.getResponseDocument().getDocumentElement());

		// skip leafref validation for valid interface
		actionRequest = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				"<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
				" <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				"	<xml-subtree>" +
				"		<plugType>PLUG-1.0</plugType>" +
				"		<schemaMountPoint>" +
				"				<test:test-mount-action-container xmlns:test=\"urn:example:test-mount-action\">"+
				"					<test:test-action-must-with-state-value>"+
				"						<test:interface-ref>interface1</test:interface-ref>"+
				"					</test:test-action-must-with-state-value>"+
				"				</test:test-mount-action-container>"+ 
				"		</schemaMountPoint>" +
				"	</xml-subtree>" +
				" </validation>" +
				"</action>"+
				"</rpc>";

		validActionRequest = DocumentToPojoTransformer
				.getAction(DocumentUtils.stringToDocument(actionRequest));
		m_response = new ActionResponse().setMessageId("1");
		m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);
		TestUtil.assertXMLEquals(getActionResponseElement_OK(), m_response.getResponseDocument().getDocumentElement());

		// skip leafref validation for invalid interface 
		actionRequest = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				"<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
				" <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				"	<xml-subtree>" +
				"		<plugType>PLUG-1.0</plugType>" +
				"		<schemaMountPoint>" +
				"				<test:test-mount-action-container xmlns:test=\"urn:example:test-mount-action\">"+
				"					<test:test-action-must-with-state-value>"+
				"						<test:interface-ref>interface5</test:interface-ref>"+
				"					</test:test-action-must-with-state-value>"+
				"				</test:test-mount-action-container>"+ 
				"		</schemaMountPoint>" +
				"	</xml-subtree>" +
				" </validation>" +
				"</action>"+
				"</rpc>";

		validActionRequest = DocumentToPojoTransformer
				.getAction(DocumentUtils.stringToDocument(actionRequest));
		m_response = new ActionResponse().setMessageId("1");
		m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);
		TestUtil.assertXMLEquals(getActionResponseElement_OK(), m_response.getResponseDocument().getDocumentElement());
	}

	@Test
	public void testAction_MissingMandatoryLeafInContainer() throws Exception {     
		doSetup();
		initialiseInterceptor();
		// missing mandatory leaf under container
		String actionRequest = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				"<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
				"<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				"<xml-subtree>" +
				"<plugType>PLUG-1.0</plugType>" +
				"<schemaMountPoint>" +
				"<test:test-action-with-mandatory xmlns:test=\"urn:example:test-mount-action\">" +
				"<test:mandatory-leaf-in-container>"+
				"<test:container1>"+
				"</test:container1>"+
				"</test:mandatory-leaf-in-container>"+
				"</test:test-action-with-mandatory>"+
				"</schemaMountPoint>" +
				"</xml-subtree>" +
				"</validation>" +
				"</action>"+
				"</rpc>";

		ActionRequest validActionRequest = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(actionRequest));
		m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse)m_response);
		assertEquals(1,m_response.getErrors().size());
		verifyNetconfRpcErrorForMissingMandatoryNode(m_response.getErrors().get(0), "Mandatory leaf 'mandatory-leaf' is missing", 
				"/validation:validation/validation:xml-subtree/validation:schemaMountPoint/test-mount:input/test:container1/test-mount:mandatory-leaf");
	}

	@Test
	public void testAction_MissingMandatoryNodes() throws Exception {     
		doSetup();
		initialiseInterceptor();

		// missing mandatory leaf under input
		String actionRequest = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				"<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
				"<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				"<xml-subtree>" +
				"<plugType>PLUG-1.0</plugType>" +
				"<schemaMountPoint>" +
				"<test:test-action-with-mandatory xmlns:test=\"urn:example:test-mount-action\">" +
				"<test:mandatory-leaf-action>"+
				"<test:leaf1>non-mandatory-leaf</test:leaf1>"+
				"</test:mandatory-leaf-action>"+
				"</test:test-action-with-mandatory>"+
				"</schemaMountPoint>" +
				"</xml-subtree>" +
				"</validation>" +
				"</action>"+
				"</rpc>";

		ActionRequest validActionRequest = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(actionRequest));
		m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse)m_response);
		assertEquals(1,m_response.getErrors().size());
		verifyNetconfRpcErrorForMissingMandatoryNode(m_response.getErrors().get(0), "Mandatory leaf 'mandatory-leaf' is missing",
				"/test-mount:input/test-mount:mandatory-leaf");

		// missing mandatory choice under input node
		actionRequest = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				"<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
				"<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				"<xml-subtree>" +
				"<plugType>PLUG-1.0</plugType>" +
				"<schemaMountPoint>" +
				"<test:test-action-with-mandatory xmlns:test=\"urn:example:test-mount-action\">" +
				"<test:mandatory-leaf-action>"+
				"<test:leaf1>non-mandatory-leaf</test:leaf1>"+
				"<test:mandatory-leaf>mandatory-leaf</test:mandatory-leaf>"+
				"</test:mandatory-leaf-action>"+
				"</test:test-action-with-mandatory>"+
				"</schemaMountPoint>" +
				"</xml-subtree>" +
				"</validation>" +
				"</action>"+
				"</rpc>";

		validActionRequest = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(actionRequest));
		m_response = new ActionResponse().setMessageId("1");
		m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse)m_response);
		assertEquals(1,m_response.getErrors().size());
		verifyNetconfRpcErrorForMissingMandatoryNode(m_response.getErrors().get(0), "Mandatory choice 'choice1' is missing",
				"/test-mount:input/test-mount:choice1");

		// valid action request with choice-case
		actionRequest = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				"<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
				"<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				"<xml-subtree>" +
				"<plugType>PLUG-1.0</plugType>" +
				"<schemaMountPoint>" +
				"<test:test-action-with-mandatory xmlns:test=\"urn:example:test-mount-action\">" +
				"<test:mandatory-leaf-action>"+
				"<test:leaf1>non-mandatory-leaf</test:leaf1>"+
				"<test:leaf-case1>case1</test:leaf-case1>"+
				"<test:mandatory-leaf>mandatory-leaf</test:mandatory-leaf>"+
				"</test:mandatory-leaf-action>"+
				"</test:test-action-with-mandatory>"+
				"</schemaMountPoint>" +
				"</xml-subtree>" +
				"</validation>" +
				"</action>"+
				"</rpc>";

		validActionRequest = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(actionRequest));
		m_response = new ActionResponse().setMessageId("1");
		m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse)m_response);
		assertEquals(0,m_response.getErrors().size());
		//verify ok response
		TestUtil.assertXMLEquals(getActionResponseElement_OK(), m_response.getResponseDocument().getDocumentElement());

		// missing mandatory leaf under list
		actionRequest = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				"<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
				"<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				"<xml-subtree>" +
				"<plugType>PLUG-1.0</plugType>" +
				"<schemaMountPoint>" +
				"<test:test-action-with-mandatory xmlns:test=\"urn:example:test-mount-action\">" +
				"<test:mandatory-leaf-action>"+
				"<test:leaf1>non-mandatory-leaf</test:leaf1>"+
				"<test:leaf-case1>case1</test:leaf-case1>"+
				"<test:list1>"+
				"<test:key1>key</test:key1>"+
				"</test:list1>"+
				"<test:mandatory-leaf>mandatory-leaf</test:mandatory-leaf>"+
				"</test:mandatory-leaf-action>"+
				"</test:test-action-with-mandatory>"+
				"</schemaMountPoint>" +
				"</xml-subtree>" +
				"</validation>" +
				"</action>"+
				"</rpc>";

		validActionRequest = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(actionRequest));
		m_response = new ActionResponse().setMessageId("1");
		m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse)m_response);
		assertEquals(1,m_response.getErrors().size());
		verifyNetconfRpcErrorForMissingMandatoryNode(m_response.getErrors().get(0), "Mandatory leaf 'mandatory-leaf-in-list' is missing",
				"/validation:validation/validation:xml-subtree/validation:schemaMountPoint/test-mount:input/test:list1[test:key1='key']/test-mount:mandatory-leaf-in-list");
	}

	@Test
	public void testAction_MissingMandatoryLeafInList() throws Exception {     
		doSetup();
		initialiseInterceptor();
		// missing mandatory node under list
		String actionRequest = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				"<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
				"<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				"<xml-subtree>" +
				"<plugType>PLUG-1.0</plugType>" +
				"<schemaMountPoint>" +
				"<test:test-action-with-mandatory xmlns:test=\"urn:example:test-mount-action\">" +
				"<test:mandatory-leaf-in-list>"+
				"<test:list1>"+
				"<test:name>action</test:name>"+
				"</test:list1>"+
				"</test:mandatory-leaf-in-list>"+
				"</test:test-action-with-mandatory>"+
				"</schemaMountPoint>" +
				"</xml-subtree>" +
				"</validation>" +
				"</action>"+
				"</rpc>";

		ActionRequest validActionRequest = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(actionRequest));
		m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse)m_response);
		assertEquals(1,m_response.getErrors().size());
		verifyNetconfRpcErrorForMissingMandatoryNode(m_response.getErrors().get(0),"Mandatory leaf 'mandatory-leaf' is missing",
				"/validation:validation/validation:xml-subtree/validation:schemaMountPoint/test-mount:input/test:list1[test:name='action']/test-mount:mandatory-leaf");
	}

	@Test
	public void testAction_MissingMandatoryLeafInNestedList() throws Exception {     
		doSetup();
		initialiseInterceptor();
		String actionRequest = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				"<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
				"<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				"<xml-subtree>" +
				"<plugType>PLUG-1.0</plugType>" +
				"<schemaMountPoint>" +
				"<test:test-action-with-mandatory xmlns:test=\"urn:example:test-mount-action\">" +
				"<test:mandatory-leaf-with-nested-list>"+
				"<test:list1>"+
				"<test:name>action</test:name>"+
				"</test:list1>"+
				"</test:mandatory-leaf-with-nested-list>"+
				"</test:test-action-with-mandatory>"+
				"</schemaMountPoint>" +
				"</xml-subtree>" +
				"</validation>" +
				"</action>"+
				"</rpc>";

		ActionRequest validActionRequest = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(actionRequest));
		m_response = new ActionResponse().setMessageId("1");
		m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse)m_response);
		assertEquals(0, m_response.getErrors().size());
		//verify ok response
		TestUtil.assertXMLEquals(getActionResponseElement_OK(), m_response.getResponseDocument().getDocumentElement());

		// missing mandatory leaf in nested list
		actionRequest = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				"<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
				"<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				"<xml-subtree>" +
				"<plugType>PLUG-1.0</plugType>" +
				"<schemaMountPoint>" +
				"<test:test-action-with-mandatory xmlns:test=\"urn:example:test-mount-action\">" +
				"<test:mandatory-leaf-with-nested-list>"+
				"<test:list1>"+
				"<test:name>action</test:name>"+
				"<test:list2>"+
				"<test:key1>key</test:key1>"+
				"</test:list2>"+
				"</test:list1>"+
				"</test:mandatory-leaf-with-nested-list>"+
				"</test:test-action-with-mandatory>"+
				"</schemaMountPoint>" +
				"</xml-subtree>" +
				"</validation>" +
				"</action>"+
				"</rpc>";

		validActionRequest = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(actionRequest));
		m_response = new ActionResponse().setMessageId("1");
		m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse)m_response);
		assertEquals(1, m_response.getErrors().size());
		verifyNetconfRpcErrorForMissingMandatoryNode(m_response.getErrors().get(0),"Mandatory leaf 'mandatory-leaf' is missing",
				"/validation:validation/validation:xml-subtree/validation:schemaMountPoint/test-mount:input/test:list1[test:name='action']/test:list2[test:key1='key']/test-mount:mandatory-leaf");
	}

	@Test
	public void testAction_MaxMin_List_LeafList() throws Exception {
		doSetup();
		initialiseInterceptor();
		String actionRequest = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				"<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
				"<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				"<xml-subtree>" +
				"<plugType>PLUG-1.0</plugType>" +
				"<schemaMountPoint>" +
				"<test:test-action-maxminvalidation-leaflistandlist xmlns:test=\"urn:example:test-mount-action\">" +
				"<test:movies>" +
				"<test:minmax>" +
				"	<test:noOfSongs>22</test:noOfSongs>" +
				"	<test:noOfSongs>23</test:noOfSongs>" +
				"	<test:noOfSongs>24</test:noOfSongs>" +
				"<test:testlist>" +
				"<test:testname>testname</test:testname>" +
				"<test:testcontainer>" +
				"<test:songs>" +
				"<test:name>test1</test:name>" +
				"</test:songs>" +
				"<test:songs>" +
				"<test:name>test2</test:name>" +
				"</test:songs>" +
				"<test:songs>" +
				"<test:name>test3</test:name>" +
				"</test:songs>" +
				"</test:testcontainer>" +
				"</test:testlist>" +
				"</test:minmax>" +
				"</test:movies>" +
				"</test:test-action-maxminvalidation-leaflistandlist>" +
				"</schemaMountPoint>" +
				"</xml-subtree>" +
				"</validation>" +
				"</action>" +
				"</rpc>";

		ActionRequest validActionRequest = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(actionRequest));
		m_response = new ActionResponse().setMessageId("1");
		m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);
		assertEquals(0, m_response.getErrors().size());
		//verify ok response
		TestUtil.assertXMLEquals(getActionResponseElement_OK(), m_response.getResponseDocument().getDocumentElement());

		String actionRequest1 = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				"<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
				"<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				"<xml-subtree>" +
				"<plugType>PLUG-1.0</plugType>" +
				"<schemaMountPoint>" +
				"<test:test-action-maxminvalidation-leaflistandlist xmlns:test=\"urn:example:test-mount-action\">" +
				"<test:movies>" +
				"<test:minmax>" +
				"	<test:noOfSongs>22</test:noOfSongs>" +
				"<test:testlist>" +
				"<test:testname>testname</test:testname>" +
				"<test:testcontainer>" +
				"<test:songs>" +
				"<test:name>test1</test:name>" +
				"</test:songs>" +
				"<test:songs>" +
				"<test:name>test2</test:name>" +
				"</test:songs>" +
				"<test:songs>" +
				"<test:name>test3</test:name>" +
				"</test:songs>" +
				"</test:testcontainer>" +
				"</test:testlist>" +
				"</test:minmax>" +
				"</test:movies>" +
				"</test:test-action-maxminvalidation-leaflistandlist>" +
				"</schemaMountPoint>" +
				"</xml-subtree>" +
				"</validation>" +
				"</action>" +
				"</rpc>";

		ActionRequest validActionRequest1 = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(actionRequest1));
		m_response = new ActionResponse().setMessageId("1");
		m_server.onAction(m_clientInfo, validActionRequest1, (ActionResponse) m_response);
		assertEquals(1, m_response.getErrors().size());
		verifyNetconfRpcErrorForMinMaxValidation(m_response.getErrors().get(0), "Minimum elements required for noOfSongs is 2.",
				"too-few-elements");

		String actionRequest2 = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				"<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
				"<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				"<xml-subtree>" +
				"<plugType>PLUG-1.0</plugType>" +
				"<schemaMountPoint>" +
				"<test:test-action-maxminvalidation-leaflistandlist xmlns:test=\"urn:example:test-mount-action\">" +
				"<test:movies>" +
				"<test:minmax>" +
				"	<test:noOfSongs>22</test:noOfSongs>" +
				"	<test:noOfSongs>23</test:noOfSongs>" +
				"	<test:noOfSongs>24</test:noOfSongs>" +
				"	<test:noOfSongs>25</test:noOfSongs>" +
				"<test:testlist>" +
				"<test:testname>testname</test:testname>" +
				"<test:testcontainer>" +
				"<test:songs>" +
				"<test:name>test1</test:name>" +
				"</test:songs>" +
				"<test:songs>" +
				"<test:name>test2</test:name>" +
				"</test:songs>" +
				"<test:songs>" +
				"<test:name>test3</test:name>" +
				"</test:songs>" +
				"</test:testcontainer>" +
				"</test:testlist>" +
				"</test:minmax>" +
				"</test:movies>" +
				"</test:test-action-maxminvalidation-leaflistandlist>" +
				"</schemaMountPoint>" +
				"</xml-subtree>" +
				"</validation>" +
				"</action>" +
				"</rpc>";

		ActionRequest validActionRequest2 = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(actionRequest2));
		m_response = new ActionResponse().setMessageId("1");
		m_server.onAction(m_clientInfo, validActionRequest2, (ActionResponse) m_response);
		assertEquals(1, m_response.getErrors().size());
		verifyNetconfRpcErrorForMinMaxValidation(m_response.getErrors().get(0), "Maximum elements allowed for noOfSongs is 3.",
				"too-many-elements");

		String actionRequest3 = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				"<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
				"<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				"<xml-subtree>" +
				"<plugType>PLUG-1.0</plugType>" +
				"<schemaMountPoint>" +
				"<test:test-action-maxminvalidation-leaflistandlist xmlns:test=\"urn:example:test-mount-action\">" +
				"<test:movies>" +
				"<test:minmax>" +
				"<test:testlist>" +
				"<test:testname>testname</test:testname>" +
				"<test:testcontainer>" +
				"<test:songs>" +
				"<test:name>test1</test:name>" +
				"</test:songs>" +
				"<test:songs>" +
				"<test:name>test2</test:name>" +
				"</test:songs>" +
				"<test:songs>" +
				"<test:name>test3</test:name>" +
				"</test:songs>" +
				"</test:testcontainer>" +
				"</test:testlist>" +
				"</test:minmax>" +
				"</test:movies>" +
				"</test:test-action-maxminvalidation-leaflistandlist>" +
				"</schemaMountPoint>" +
				"</xml-subtree>" +
				"</validation>" +
				"</action>" +
				"</rpc>";

		ActionRequest validActionRequest3 = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(actionRequest3));
		m_response = new ActionResponse().setMessageId("1");
		m_server.onAction(m_clientInfo, validActionRequest3, (ActionResponse) m_response);
		assertEquals(1, m_response.getErrors().size());
		verifyNetconfRpcErrorForMinMaxValidation(m_response.getErrors().get(0), "Minimum elements required for noOfSongs is 2.",
				"too-few-elements");

		String actionRequest4 = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				"<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
				"<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				"<xml-subtree>" +
				"<plugType>PLUG-1.0</plugType>" +
				"<schemaMountPoint>" +
				"<test:test-action-maxminvalidation-leaflistandlist xmlns:test=\"urn:example:test-mount-action\">" +
				"<test:movies>" +
				"<test:minmax>" +
				"	<test:noOfSongs>22</test:noOfSongs>" +
				"	<test:noOfSongs>23</test:noOfSongs>" +
				"	<test:noOfSongs>24</test:noOfSongs>" +
				"<test:testlist>" +
				"<test:testname>testname</test:testname>" +
				"<test:testcontainer>" +
				"<test:songs>" +
				"<test:name>test1</test:name>" +
				"</test:songs>" +
				"<test:songs>" +
				"<test:name>test2</test:name>" +
				"</test:songs>" +
				"<test:songs>" +
				"<test:name>test3</test:name>" +
				"</test:songs>" +
				"<test:songs>" +
				"<test:name>test4</test:name>" +
				"</test:songs>" +
				"</test:testcontainer>" +
				"</test:testlist>" +
				"</test:minmax>" +
				"</test:movies>" +
				"</test:test-action-maxminvalidation-leaflistandlist>" +
				"</schemaMountPoint>" +
				"</xml-subtree>" +
				"</validation>" +
				"</action>" +
				"</rpc>";

		ActionRequest validActionRequest4 = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(actionRequest4));
		m_response = new ActionResponse().setMessageId("1");
		m_server.onAction(m_clientInfo, validActionRequest4, (ActionResponse) m_response);
		assertEquals(1, m_response.getErrors().size());
		verifyNetconfRpcErrorForMinMaxValidation(m_response.getErrors().get(0), "Maximum elements allowed for songs is 3.",
				"too-many-elements");

		String actionRequest5 = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				"<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
				"<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				"<xml-subtree>" +
				"<plugType>PLUG-1.0</plugType>" +
				"<schemaMountPoint>" +
				"<test:test-action-maxminvalidation-leaflistandlist xmlns:test=\"urn:example:test-mount-action\">" +
				"<test:movies>" +
				"<test:minmax>" +
				"	<test:noOfSongs>22</test:noOfSongs>" +
				"	<test:noOfSongs>23</test:noOfSongs>" +
				"	<test:noOfSongs>24</test:noOfSongs>" +
				"<test:testlist>" +
				"<test:testname>testname</test:testname>" +
				"<test:testcontainer>" +
				"<test:songs>" +
				"<test:name>test1</test:name>" +
				"</test:songs>" +
				"</test:testcontainer>" +
				"</test:testlist>" +
				"</test:minmax>" +
				"</test:movies>" +
				"</test:test-action-maxminvalidation-leaflistandlist>" +
				"</schemaMountPoint>" +
				"</xml-subtree>" +
				"</validation>" +
				"</action>" +
				"</rpc>";

		ActionRequest validActionRequest5 = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(actionRequest5));
		m_response = new ActionResponse().setMessageId("1");
		m_server.onAction(m_clientInfo, validActionRequest5, (ActionResponse) m_response);
		assertEquals(1, m_response.getErrors().size());
		verifyNetconfRpcErrorForMinMaxValidation(m_response.getErrors().get(0), "Minimum elements required for songs is 2.",
				"too-few-elements");


		String actionRequest6 = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				"<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
				"<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				"<xml-subtree>" +
				"<plugType>PLUG-1.0</plugType>" +
				"<schemaMountPoint>" +
				"<test:test-action-maxminvalidation-leaflistandlist xmlns:test=\"urn:example:test-mount-action\">" +
				"<test:movies>" +
				"<test:minmax>" +
				"	<test:noOfSongs>22</test:noOfSongs>" +
				"	<test:noOfSongs>23</test:noOfSongs>" +
				"	<test:noOfSongs>24</test:noOfSongs>" +
				"<test:testlist>" +
				"<test:testname>testname</test:testname>" +
				"<test:testcontainer>" +
				"</test:testcontainer>" +
				"</test:testlist>" +
				"</test:minmax>" +
				"</test:movies>" +
				"</test:test-action-maxminvalidation-leaflistandlist>" +
				"</schemaMountPoint>" +
				"</xml-subtree>" +
				"</validation>" +
				"</action>" +
				"</rpc>";

		ActionRequest validActionRequest6 = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(actionRequest6));
		m_response = new ActionResponse().setMessageId("1");
		m_server.onAction(m_clientInfo, validActionRequest6, (ActionResponse) m_response);
		assertEquals(1, m_response.getErrors().size());
		verifyNetconfRpcErrorForMinMaxValidation(m_response.getErrors().get(0), "Minimum elements required for songs is 2.",
				"too-few-elements");
	}

	@Test
	public void testAction_MissingMandatoryLeafInNestedContainer() throws Exception {     
		doSetup();
		initialiseInterceptor();
		String actionRequest = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				"<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
				"<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				"<xml-subtree>" +
				"<plugType>PLUG-1.0</plugType>" +
				"<schemaMountPoint>" +
				"<test:test-action-with-mandatory xmlns:test=\"urn:example:test-mount-action\">" +
				"<test:mandatory-leaf-with-nested-container>"+
				"<test:container1>"+
				"<test:name>action</test:name>"+
				"</test:container1>"+
				"</test:mandatory-leaf-with-nested-container>"+
				"</test:test-action-with-mandatory>"+
				"</schemaMountPoint>" +
				"</xml-subtree>" +
				"</validation>" +
				"</action>"+
				"</rpc>";

		ActionRequest validActionRequest = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(actionRequest));
		m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse)m_response);
		assertEquals(1, m_response.getErrors().size());
		verifyNetconfRpcErrorForMissingMandatoryNode(m_response.getErrors().get(0),"Mandatory leaf 'mandatory-leaf' is missing",
				"/validation:validation/validation:xml-subtree/validation:schemaMountPoint/test-mount:input/test:container1/test-mount:container2/test-mount:mandatory-leaf");

		actionRequest = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				"<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
				"<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				"<xml-subtree>" +
				"<plugType>PLUG-1.0</plugType>" +
				"<schemaMountPoint>" +
				"<test:test-action-with-mandatory xmlns:test=\"urn:example:test-mount-action\">" +
				"<test:mandatory-leaf-with-nested-container>"+
				"<test:container1>"+
				"<test:name>action</test:name>"+
				"<test:container2>"+
				"<test:leaf1>nested-container</test:leaf1>"+
				"</test:container2>"+
				"</test:container1>"+
				"</test:mandatory-leaf-with-nested-container>"+
				"</test:test-action-with-mandatory>"+
				"</schemaMountPoint>" +
				"</xml-subtree>" +
				"</validation>" +
				"</action>"+
				"</rpc>";

		validActionRequest = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(actionRequest));
		m_response = new ActionResponse().setMessageId("1");
		m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse)m_response);
		assertEquals(1, m_response.getErrors().size());
		verifyNetconfRpcErrorForMissingMandatoryNode(m_response.getErrors().get(0),"Mandatory leaf 'mandatory-leaf' is missing", 
				"/validation:validation/validation:xml-subtree/validation:schemaMountPoint/test-mount:input/test:container1/test:container2/test-mount:mandatory-leaf");

		actionRequest = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				"<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
				"<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				"<xml-subtree>" +
				"<plugType>PLUG-1.0</plugType>" +
				"<schemaMountPoint>" +
				"<test:test-action-with-mandatory xmlns:test=\"urn:example:test-mount-action\">" +
				"<test:mandatory-leaf-with-nested-container>"+
				"<test:container1>"+
				"<test:name>action</test:name>"+
				"<test:container2>"+
				"<test:mandatory-leaf>nested-container</test:mandatory-leaf>"+
				"</test:container2>"+
				"</test:container1>"+
				"</test:mandatory-leaf-with-nested-container>"+
				"</test:test-action-with-mandatory>"+
				"</schemaMountPoint>" +
				"</xml-subtree>" +
				"</validation>" +
				"</action>"+
				"</rpc>";

		validActionRequest = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(actionRequest));
		m_response = new ActionResponse().setMessageId("1");
		m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse)m_response);
		assertEquals(0, m_response.getErrors().size());
		//verify ok response
		TestUtil.assertXMLEquals(getActionResponseElement_OK(), m_response.getResponseDocument().getDocumentElement());

		actionRequest = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				"<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
				"<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				"<xml-subtree>" +
				"<plugType>PLUG-1.0</plugType>" +
				"<schemaMountPoint>" +
				"<test:test-action-with-mandatory xmlns:test=\"urn:example:test-mount-action\">" +
				"<test:mandatory-leaf-with-nested-container>"+
				"<test:container1>"+
				"<test:name>action</test:name>"+
				"<test:container2>"+
				"<test:mandatory-leaf>nested-container</test:mandatory-leaf>"+
				"<test:list1>"+
				"<test:key1>key</test:key1>"+
				"</test:list1>"+
				"</test:container2>"+
				"</test:container1>"+
				"</test:mandatory-leaf-with-nested-container>"+
				"</test:test-action-with-mandatory>"+
				"</schemaMountPoint>" +
				"</xml-subtree>" +
				"</validation>" +
				"</action>"+
				"</rpc>";

		validActionRequest = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(actionRequest));
		m_response = new ActionResponse().setMessageId("1");
		m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse)m_response);
		assertEquals(1, m_response.getErrors().size());
		verifyNetconfRpcErrorForMissingMandatoryNode(m_response.getErrors().get(0), "Mandatory leaf 'mandatory-leaf1' is missing", 
				"/validation:validation/validation:xml-subtree/validation:schemaMountPoint/test-mount:input/test:container1/test:container2/test:list1[test:key1='key']/test-mount:mandatory-leaf1" );
	}  

	@Test
	public void testAction_MissingMandatoryLeafInNestedChoiceCase() throws Exception {     
		doSetup();
		initialiseInterceptor();
		String actionRequest = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				"<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
				"<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				"<xml-subtree>" +
				"<plugType>PLUG-1.0</plugType>" +
				"<schemaMountPoint>" +
				"<test:test-action-with-mandatory xmlns:test=\"urn:example:test-mount-action\">" +
				"<test:mandatory-leaf-choice>"+
				"<test:container1>"+
				"<test:name>case3</test:name>"+
				"</test:container1>"+
				"</test:mandatory-leaf-choice>"+
				"</test:test-action-with-mandatory>"+
				"</schemaMountPoint>" +
				"</xml-subtree>" +
				"</validation>" +
				"</action>"+
				"</rpc>";

		ActionRequest validActionRequest = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(actionRequest));
		m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse)m_response);
		assertEquals(0, m_response.getErrors().size());
		//verify ok response
		TestUtil.assertXMLEquals(getActionResponseElement_OK(), m_response.getResponseDocument().getDocumentElement());

		actionRequest = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				"<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
				"<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				"<xml-subtree>" +
				"<plugType>PLUG-1.0</plugType>" +
				"<schemaMountPoint>" +
				"<test:test-action-with-mandatory xmlns:test=\"urn:example:test-mount-action\">" +
				"<test:mandatory-leaf-choice>"+
				"<test:container1>"+
				"<test:leaf1>case1</test:leaf1>"+
				"</test:container1>"+
				"</test:mandatory-leaf-choice>"+
				"</test:test-action-with-mandatory>"+
				"</schemaMountPoint>" +
				"</xml-subtree>" +
				"</validation>" +
				"</action>"+
				"</rpc>";

		validActionRequest = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(actionRequest));
		m_response = new ActionResponse().setMessageId("1");
		m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse)m_response);
		assertEquals(1, m_response.getErrors().size());
		verifyNetconfRpcErrorForMissingMandatoryNode(m_response.getErrors().get(0),"Mandatory leaf 'mandatory-leaf' is missing",
				"/validation:validation/validation:xml-subtree/validation:schemaMountPoint/test-mount:input/test:container1/test-mount:mandatory-leaf");

		// missing choice node
		actionRequest = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				"<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
				"<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				"<xml-subtree>" +
				"<plugType>PLUG-1.0</plugType>" +
				"<schemaMountPoint>" +
				"<test:test-action-with-mandatory xmlns:test=\"urn:example:test-mount-action\">" +
				"<test:mandatory-leaf-choice>"+
				"<test:container1>"+
				"<test:leaflist>leaf-lis1</test:leaflist>"+
				"<test:leaflist>leaf-lis2</test:leaflist>"+
				"</test:container1>"+
				"</test:mandatory-leaf-choice>"+
				"</test:test-action-with-mandatory>"+
				"</schemaMountPoint>" +
				"</xml-subtree>" +
				"</validation>" +
				"</action>"+
				"</rpc>";

		validActionRequest = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(actionRequest));
		m_response = new ActionResponse().setMessageId("1");
		m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse)m_response);
		assertEquals(1, m_response.getErrors().size());
		verifyNetconfRpcErrorForMissingMandatoryNode(m_response.getErrors().get(0),"Mandatory choice 'choice2' is missing",
				"/validation:validation/validation:xml-subtree/validation:schemaMountPoint/test-mount:input/test:container1/test-mount:choice2");

		// valid action request with choice case
		actionRequest = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				"<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
				"<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				"<xml-subtree>" +
				"<plugType>PLUG-1.0</plugType>" +
				"<schemaMountPoint>" +
				"<test:test-action-with-mandatory xmlns:test=\"urn:example:test-mount-action\">" +
				"<test:mandatory-leaf-choice>"+
				"<test:container1>"+
				"<test:leaflist>leaf-lis1</test:leaflist>"+
				"<test:leaf3>case4</test:leaf3>"+
				"</test:container1>"+
				"</test:mandatory-leaf-choice>"+
				"</test:test-action-with-mandatory>"+
				"</schemaMountPoint>" +
				"</xml-subtree>" +
				"</validation>" +
				"</action>"+
				"</rpc>";

		validActionRequest = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(actionRequest));
		m_response = new ActionResponse().setMessageId("1");
		m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse)m_response);
		assertEquals(0, m_response.getErrors().size());
		//verify ok response
		TestUtil.assertXMLEquals(getActionResponseElement_OK(), m_response.getResponseDocument().getDocumentElement());

		actionRequest = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				"<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
				"<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				"<xml-subtree>" +
				"<plugType>PLUG-1.0</plugType>" +
				"<schemaMountPoint>" +
				"<test:test-action-with-mandatory xmlns:test=\"urn:example:test-mount-action\">" +
				"<test:mandatory-leaf-choice>"+
				"<test:container1>"+
				"<test:leaflist>leaf-lis1</test:leaflist>"+
				"<test:leaf3>case4</test:leaf3>"+
				"<test:list1>"+
				"<test:key1>key</test:key1>"+
				"</test:list1>"+
				"</test:container1>"+
				"</test:mandatory-leaf-choice>"+
				"</test:test-action-with-mandatory>"+
				"</schemaMountPoint>" +
				"</xml-subtree>" +
				"</validation>" +
				"</action>"+
				"</rpc>";

		validActionRequest = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(actionRequest));
		m_response = new ActionResponse().setMessageId("1");
		m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse)m_response);
		assertEquals(1, m_response.getErrors().size());
		verifyNetconfRpcErrorForMissingMandatoryNode(m_response.getErrors().get(0),"Mandatory leaf 'mandatory-leaf1' is missing",
				"/validation:validation/validation:xml-subtree/validation:schemaMountPoint/test-mount:input/test:container1/test:list1[test:key1='key']/test-mount:mandatory-leaf1");

		actionRequest = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				"<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
				"<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				"<xml-subtree>" +
				"<plugType>PLUG-1.0</plugType>" +
				"<schemaMountPoint>" +
				"<test:test-action-with-mandatory xmlns:test=\"urn:example:test-mount-action\">" +
				"<test:mandatory-leaf-choice>"+
				"<test:container1>"+
				"<test:leaflist>leaf-lis1</test:leaflist>"+
				"<test:leaf3>case4</test:leaf3>"+
				"<test:list1>"+
				"<test:key1>key</test:key1>"+
				"<test:mandatory-leaf1>mandatory-leaf</test:mandatory-leaf1>"+
				"</test:list1>"+
				"</test:container1>"+
				"</test:mandatory-leaf-choice>"+
				"</test:test-action-with-mandatory>"+
				"</schemaMountPoint>" +
				"</xml-subtree>" +
				"</validation>" +
				"</action>"+
				"</rpc>";

		validActionRequest = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(actionRequest));
		m_response = new ActionResponse().setMessageId("1");
		m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse)m_response);
		assertEquals(0, m_response.getErrors().size());
		//verify ok response
		TestUtil.assertXMLEquals(getActionResponseElement_OK(), m_response.getResponseDocument().getDocumentElement());

		actionRequest = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				"<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
				"<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				"<xml-subtree>" +
				"<plugType>PLUG-1.0</plugType>" +
				"<schemaMountPoint>" +
				"<test:test-action-with-mandatory xmlns:test=\"urn:example:test-mount-action\">" +
				"<test:mandatory-leaf-choice>"+
				"<test:container1>"+
				"<test:leaf4>case5</test:leaf4>"+
				"</test:container1>"+
				"</test:mandatory-leaf-choice>"+
				"</test:test-action-with-mandatory>"+
				"</schemaMountPoint>" +
				"</xml-subtree>" +
				"</validation>" +
				"</action>"+
				"</rpc>";

		validActionRequest = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(actionRequest));
		m_response = new ActionResponse().setMessageId("1");
		m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse)m_response);
		assertEquals(1, m_response.getErrors().size());
		verifyNetconfRpcErrorForMissingMandatoryNode(m_response.getErrors().get(0),"Mandatory leaf 'mandatory-leaf2' is missing",
				"/validation:validation/validation:xml-subtree/validation:schemaMountPoint/test-mount:input/test:container1/test-mount:mandatory-leaf2");

		actionRequest = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				"<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
				"<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				"<xml-subtree>" +
				"<plugType>PLUG-1.0</plugType>" +
				"<schemaMountPoint>" +
				"<test:test-action-with-mandatory xmlns:test=\"urn:example:test-mount-action\">" +
				"<test:mandatory-leaf-choice>"+
				"<test:container1>"+
				"<test:leaf4>case5</test:leaf4>"+
				"<test:mandatory-leaf2>mandatory-leaf</test:mandatory-leaf2>"+
				"</test:container1>"+
				"</test:mandatory-leaf-choice>"+
				"</test:test-action-with-mandatory>"+
				"</schemaMountPoint>" +
				"</xml-subtree>" +
				"</validation>" +
				"</action>"+
				"</rpc>";

		validActionRequest = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(actionRequest));
		m_response = new ActionResponse().setMessageId("1");
		m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse)m_response);
		assertEquals(0, m_response.getErrors().size());
		//verify ok response
		TestUtil.assertXMLEquals(getActionResponseElement_OK(), m_response.getResponseDocument().getDocumentElement());
	}

	@Test
	public void testAction_SkipMandatoryLeafValidation_OutsideActionNode() throws Exception {
		doSetup();
		initialiseInterceptor();
		// missing mandatory leaf outside action node
		String actionRequest = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				"<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
				"<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				"<xml-subtree>" +
				"<plugType>PLUG-1.0</plugType>" +
				"<schemaMountPoint>" +
				"<test:test-action-with-mandatory1 xmlns:test=\"urn:example:test-mount-action\">" +
				"<test:mandatory-leaf-in-container1>"+
				"<test:container1>"+
				"<test:mandatory-leaf>leaf1</test:mandatory-leaf>"+
				"</test:container1>"+
				"</test:mandatory-leaf-in-container1>"+
				"</test:test-action-with-mandatory1>"+
				"</schemaMountPoint>" +
				"</xml-subtree>" +
				"</validation>" +
				"</action>"+
				"</rpc>";

		ActionRequest validActionRequest = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(actionRequest));
		m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse)m_response);
		assertEquals(0, m_response.getErrors().size());

		// missing mandatory leaf outside action node
		actionRequest = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				"<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
				"<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				"<xml-subtree>" +
				"<plugType>PLUG-1.0</plugType>" +
				"<schemaMountPoint>" +
				"<test:test-action-with-mandatory-list xmlns:test=\"urn:example:test-mount-action\">" +
				"<test:name>bbf</test:name>"+
				"<test:mandatory-leaf-in-list>"+
				"<test:container1>"+
				"<test:mandatory-leaf>leaf1</test:mandatory-leaf>"+
				"</test:container1>"+
				"</test:mandatory-leaf-in-list>"+
				"</test:test-action-with-mandatory-list>"+
				"</schemaMountPoint>" +
				"</xml-subtree>" +
				"</validation>" +
				"</action>"+
				"</rpc>";

		m_response = new ActionResponse().setMessageId("1");
		validActionRequest = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(actionRequest));
		m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse)m_response);
		assertEquals(0, m_response.getErrors().size());

		// missing key in list which is outside action node
		actionRequest = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				"<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
				"<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				"<xml-subtree>" +
				"<plugType>PLUG-1.0</plugType>" +
				"<schemaMountPoint>" +
				"<test:test-action-with-mandatory-list xmlns:test=\"urn:example:test-mount-action\">" +
				"<test:mandatory-leaf-in-list>"+
				"<test:container1>"+
				"<test:mandatory-leaf>leaf1</test:mandatory-leaf>"+
				"</test:container1>"+
				"</test:mandatory-leaf-in-list>"+
				"</test:test-action-with-mandatory-list>"+
				"</schemaMountPoint>" +
				"</xml-subtree>" +
				"</validation>" +
				"</action>"+
				"</rpc>";

		m_response = new ActionResponse().setMessageId("1");
		validActionRequest = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(actionRequest));
		m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse)m_response);
		assertEquals(1, m_response.getErrors().size());

		NetconfRpcError rpcError= m_response.getErrors().get(0);                        
		assertEquals(NetconfRpcErrorType.Application, rpcError.getErrorType());
		assertEquals(NetconfRpcErrorTag.MISSING_ELEMENT, rpcError.getErrorTag());
		assertEquals("Expected list key(s) [name] is missing", rpcError.getErrorMessage());
	}

	private void verifyNetconfRpcErrorForMissingMandatoryNode(NetconfRpcError rpcError, String errorPath, String errorMessage) {
		assertEquals(NetconfRpcErrorType.Application, rpcError.getErrorType());
		assertEquals(NetconfRpcErrorSeverity.Error, rpcError.getErrorSeverity());
		assertEquals(NetconfRpcErrorTag.DATA_MISSING, rpcError.getErrorTag());
		assertEquals(errorPath, rpcError.getErrorMessage());
		assertEquals(errorMessage, rpcError.getErrorPath());
	}

	private void verifyNetconfRpcErrorForMinMaxValidation(NetconfRpcError rpcError, String errorMessage,String errorAppTag) {
		assertEquals(NetconfRpcErrorType.Application, rpcError.getErrorType());
		assertEquals(NetconfRpcErrorSeverity.Error, rpcError.getErrorSeverity());
		assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, rpcError.getErrorTag());
		assertEquals(errorMessage, rpcError.getErrorMessage());
		assertEquals(errorAppTag, rpcError.getErrorAppTag());
	}

	private Element getActionResponseElement() throws NetconfMessageBuilderException{
		String response =  "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"+
				"<test:reset-finished-at xmlns:test=\"urn:example:test-mount-action\">2014-07-29T13:42:00Z</test:reset-finished-at>" +
				"</rpc-reply>";
		Document document = DocumentUtils.stringToDocument(response);
		return document.getDocumentElement();
	}

	private Element getActionResponseElement_OK() throws NetconfMessageBuilderException{
		String response =  "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"+
				"<ok/>" +
				"</rpc-reply>";
		Document document = DocumentUtils.stringToDocument(response);
		return document.getDocumentElement();
	}
}
