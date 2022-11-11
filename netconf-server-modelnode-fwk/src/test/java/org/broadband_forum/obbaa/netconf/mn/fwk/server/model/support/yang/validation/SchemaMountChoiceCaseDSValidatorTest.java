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

import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.utils.AnnotationAnalysisException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.AddDefaultDataInterceptor;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeFactoryException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.xml.sax.SAXException;

import java.io.IOException;

@RunWith(RequestScopeJunitRunner.class)
public class SchemaMountChoiceCaseDSValidatorTest extends AbstractSchemaMountTest{

	@Before
	public void setup() throws SchemaBuildException, AnnotationAnalysisException, ModelNodeFactoryException {
		super.setup();
		MockitoAnnotations.initMocks(this);
		initialiseInterceptor();
	}
	
	@Override
    protected void initialiseInterceptor() {
        m_addDefaultDataInterceptor = new AddDefaultDataInterceptor(m_mountModelNodeHelperRegistry, m_mountRegistry, m_expValidator);
        m_addDefaultDataInterceptor.init();
    }


	@Test
	public void testChoiceWithMandatoryAndDefault() throws ModelNodeInitException, SAXException, IOException {
		initialiseInterceptor();
		String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
				" <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				"  <xml-subtree>" +
				"   <plugType>PLUG-1.0</plugType>" +
				"   <schemaMountPoint>" +
				"    <choice-case-container xmlns=\"schema-mount-test\">" +
				" 		<testList6>" +
				"   		<key>test</key>" +
				" 			<configured-device-properties>" +
				"   			<username>username</username>" +
				" 			</configured-device-properties>" +
				" 		</testList6>" +
				"    </choice-case-container>" +
				"   </schemaMountPoint>" +
				"  </xml-subtree>" +
				" </validation>";
		editConfig(m_server, m_clientInfo, requestXml, true);

		String response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ "  <data>"
						+ "   <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
						+ "    <validation:xml-subtree>"
						+ "     <validation:plugType>PLUG-1.0</validation:plugType>"
						+ "     <validation:schemaMountPoint>"
						+ "      <smt:choice-case-container xmlns:smt=\"schema-mount-test\">"
						+ "			<smt:case1-default-leaf>case1</smt:case1-default-leaf>"
						+ "           <smt:outer-default-leaf>one</smt:outer-default-leaf>"
						+ "			<smt:testList6>"
						+ "				<smt:key>test</smt:key>"
						+ "				<smt:configured-device-properties>"
						+ "					<smt:username>username</smt:username>"
						+ "				</smt:configured-device-properties>"
						+ "				<smt:supervision-state>on</smt:supervision-state>"
						+ "			</smt:testList6>"
						+ "      </smt:choice-case-container>"
						+ "     </validation:schemaMountPoint>"
						+ "    </validation:xml-subtree>"
						+ "   </validation:validation>"
						+ "  </data>"
						+ " </rpc-reply>";

		verifyGet(response);
	}

	@Test
	public void testChoiceWithDefaultCaseNodesCreation() throws Exception {

		// default choice/case leaf nodes will be initiated
		String request = 
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
						" <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
						"  <xml-subtree>" +
						"   <plugType>PLUG-1.0</plugType>" +
						"   <schemaMountPoint>" +
						"    <choice-case-container xmlns=\"schema-mount-test\">" +
						"		<outer-leaf>leaf1</outer-leaf>"+
						"    </choice-case-container>" +
						"   </schemaMountPoint>" +
						"  </xml-subtree>" +
						" </validation>"
						;
		editConfig(request);

		// verify GET request - Default leafs are created
		String response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ "  <data>"
						+ "   <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
						+ "    <validation:xml-subtree>"
						+ "     <validation:plugType>PLUG-1.0</validation:plugType>" 
						+ "     <validation:schemaMountPoint>"
						+ "      <smt:choice-case-container xmlns:smt=\"schema-mount-test\">"
						+ "		  <smt:case1-default-leaf>case1</smt:case1-default-leaf>"
						+ "       <smt:outer-leaf>leaf1</smt:outer-leaf>"
						+ " 	  <smt:outer-default-leaf>one</smt:outer-default-leaf>"
						+ "      </smt:choice-case-container>"
						+ "     </validation:schemaMountPoint>"
						+ "    </validation:xml-subtree>"
						+ "   </validation:validation>"
						+ "  </data>"
						+ " </rpc-reply>"
						;
		verifyGet(m_server, m_clientInfo, response);
	}

	@Test
	public void testMustConstraint_ChoiceWithDefaultCaseNodes() throws Exception {
		// must constraint evaluates as TRUE --> must "../case1-default-leaf = 'case1'"; 
		String request = 
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
						" <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
						"  <xml-subtree>" +
						"   <plugType>PLUG-1.0</plugType>" +
						"   <schemaMountPoint>" +
						"    <choice-case-container xmlns=\"schema-mount-test\">" +
						"		<case1-must-leaf>must-leaf</case1-must-leaf>"+
						"    </choice-case-container>" +
						"   </schemaMountPoint>" +
						"  </xml-subtree>" +
						" </validation>"
						;
		editConfig(request);

		// verify GET response
		String response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ "  <data>"
						+ "   <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
						+ "    <validation:xml-subtree>"
						+ "     <validation:plugType>PLUG-1.0</validation:plugType>" 
						+ "     <validation:schemaMountPoint>"
						+ "      <smt:choice-case-container xmlns:smt=\"schema-mount-test\">"
						+ "		  <smt:case1-default-leaf>case1</smt:case1-default-leaf>"
						+ "		  <smt:case1-must-leaf>must-leaf</smt:case1-must-leaf>"
						+ " 	  <smt:outer-default-leaf>one</smt:outer-default-leaf>"
						+ "      </smt:choice-case-container>"
						+ "     </validation:schemaMountPoint>"
						+ "    </validation:xml-subtree>"
						+ "   </validation:validation>"
						+ "  </data>"
						+ " </rpc-reply>"
						;
		verifyGet(m_server, m_clientInfo, response);

		// modify the default leaf value --> should be failed
		request = 
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
						" <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
						"  <xml-subtree>" +
						"   <plugType>PLUG-1.0</plugType>" +
						"   <schemaMountPoint>" +
						"    <choice-case-container xmlns=\"schema-mount-test\">" +
						"		<case1-default-leaf>non-default</case1-default-leaf>"+
						"    </choice-case-container>" +
						"   </schemaMountPoint>" +
						"  </xml-subtree>" +
						" </validation>"
						;
		NetConfResponse ncResponse = editConfigAsFalse(request);
		assertEquals("Violate must constraints: ../case1-default-leaf = 'case1'", ncResponse.getErrors().get(0).getErrorMessage());
		assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/smt:choice-case-container/smt:case1-must-leaf", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("must-violation", ncResponse.getErrors().get(0).getErrorAppTag());
		assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, ncResponse.getErrors().get(0).getErrorTag());
	}

	@Test
	public void testMustConstraintChoiceWithDefaultCase_DeleteDefaultLeaf() throws Exception {
		// create default-leaf with 'default-leaf' and must constraint evaluates as TRUE
		String request = 
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
						" <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
						"  <xml-subtree>" +
						"   <plugType>PLUG-1.0</plugType>" +
						"   <schemaMountPoint>" +
						"    <choice-case-container xmlns=\"schema-mount-test\">" +
						"		<case1-must-leaf1>must-leaf</case1-must-leaf1>"+
						"		<case1-default-leaf>default-leaf</case1-default-leaf>"+
						"    </choice-case-container>" +
						"   </schemaMountPoint>" +
						"  </xml-subtree>" +
						" </validation>"
						;
		editConfig(request);

		// verify GET response
		String response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ "  <data>"
						+ "   <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
						+ "    <validation:xml-subtree>"
						+ "     <validation:plugType>PLUG-1.0</validation:plugType>" 
						+ "     <validation:schemaMountPoint>"
						+ "      <smt:choice-case-container xmlns:smt=\"schema-mount-test\">"
						+ "		  <smt:case1-default-leaf>default-leaf</smt:case1-default-leaf>"
						+ "		  <smt:case1-must-leaf1>must-leaf</smt:case1-must-leaf1>"
						+ " 	  <smt:outer-default-leaf>one</smt:outer-default-leaf>"
						+ "      </smt:choice-case-container>"
						+ "     </validation:schemaMountPoint>"
						+ "    </validation:xml-subtree>"
						+ "   </validation:validation>"
						+ "  </data>"
						+ " </rpc-reply>"
						;
		verifyGet(m_server, m_clientInfo, response);

		// delete the default-leaf --> default-leaf recreate it with default value with 'case1', so in this case must constraint should be failed
		request = 
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
						" <validation xmlns=\"urn:org:bbf2:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
						"  <xml-subtree>" +
						"   <plugType>PLUG-1.0</plugType>" +
						"   <schemaMountPoint>" +
						"    <choice-case-container xmlns=\"schema-mount-test\">" +
						"		<case1-default-leaf xc:operation=\"delete\">default-leaf</case1-default-leaf>"+
						"    </choice-case-container>" +
						"   </schemaMountPoint>" +
						"  </xml-subtree>" +
						" </validation>"
						;
		NetConfResponse ncResponse = editConfigAsFalse(request);
		assertEquals("Violate must constraints: ../case1-default-leaf != 'case1'", ncResponse.getErrors().get(0).getErrorMessage());
		assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/smt:choice-case-container/smt:case1-must-leaf1", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("must-violation", ncResponse.getErrors().get(0).getErrorAppTag());
		assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, ncResponse.getErrors().get(0).getErrorTag());
	}

	@Test
	public void testMustConstraintWithDefaultLeaf_CaseNodePointsToOutsideChoiceNode() throws Exception {
		// create CASE2 nodes
		String request = 
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
						" <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
						"  <xml-subtree>" +
						"   <plugType>PLUG-1.0</plugType>" +
						"   <schemaMountPoint>" +
						"    <choice-case-container xmlns=\"schema-mount-test\">" +
						"		<case2-must-leaf>must-leaf</case2-must-leaf>"+
						"		<case2-leaf>case2</case2-leaf>"+
						"    </choice-case-container>" +
						"   </schemaMountPoint>" +
						"  </xml-subtree>" +
						" </validation>"
						;
		editConfig(request);

		// verify GET response
		String response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ "  <data>"
						+ "   <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
						+ "    <validation:xml-subtree>"
						+ "     <validation:plugType>PLUG-1.0</validation:plugType>" 
						+ "     <validation:schemaMountPoint>"
						+ "      <smt:choice-case-container xmlns:smt=\"schema-mount-test\">"
						+ "			<smt:case2-leaf>case2</smt:case2-leaf>"
						+" 			<smt:case2-must-leaf>must-leaf</smt:case2-must-leaf>"
						+"			<smt:outer-default-leaf>one</smt:outer-default-leaf>"
						+ "      </smt:choice-case-container>"
						+ "     </validation:schemaMountPoint>"
						+ "    </validation:xml-subtree>"
						+ "   </validation:validation>"
						+ "  </data>"
						+ " </rpc-reply>"
						;
		verifyGet(m_server, m_clientInfo, response);

		// delete one of CASE2 nodes --> should be passed
		request = 
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
						" <validation xmlns=\"urn:org:bbf2:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
						"  <xml-subtree>" +
						"   <plugType>PLUG-1.0</plugType>" +
						"   <schemaMountPoint>" +
						"    <choice-case-container xmlns=\"schema-mount-test\">" +
						"		<case2-leaf xc:operation=\"remove\">case2</case2-leaf>"+
						"    </choice-case-container>" +
						"   </schemaMountPoint>" +
						"  </xml-subtree>" +
						" </validation>";
		editConfig(request);

		// verify GET request after 'case2-leaf' deletion
		response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ "  <data>"
						+ "   <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
						+ "    <validation:xml-subtree>"
						+ "     <validation:plugType>PLUG-1.0</validation:plugType>" 
						+ "     <validation:schemaMountPoint>"
						+ "      <smt:choice-case-container xmlns:smt=\"schema-mount-test\">"
						+" 			<smt:case2-must-leaf>must-leaf</smt:case2-must-leaf>"
						+"			<smt:outer-default-leaf>one</smt:outer-default-leaf>"
						+ "      </smt:choice-case-container>"
						+ "     </validation:schemaMountPoint>"
						+ "    </validation:xml-subtree>"
						+ "   </validation:validation>"
						+ "  </data>"
						+ " </rpc-reply>"
						;
		verifyGet(m_server, m_clientInfo, response);

		// change the default leaf value which is outside choice node --> in this case must constraint should be failed 
		request = 
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
						" <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
						"  <xml-subtree>" +
						"   <plugType>PLUG-1.0</plugType>" +
						"   <schemaMountPoint>" +
						"    <choice-case-container xmlns=\"schema-mount-test\">" +
						"	 	<outer-default-leaf>two</outer-default-leaf>"+
						"    </choice-case-container>" +
						"   </schemaMountPoint>" +
						"  </xml-subtree>" +
						" </validation>"
						;

		NetConfResponse ncResponse = editConfigAsFalse(request);
		assertEquals("Violate must constraints: ../outer-default-leaf = 'one'", ncResponse.getErrors().get(0).getErrorMessage());
		assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/smt:choice-case-container/smt:case2-must-leaf", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("must-violation", ncResponse.getErrors().get(0).getErrorAppTag());
		assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, ncResponse.getErrors().get(0).getErrorTag());
	}

	@Test
	public void testMustConstraintWithDefaultLeaf_CaseNodePointsToOutsideChoiceNode_Fail() throws Exception {
		// create outer default leaf with non default value --> must constraint failed
		String request = 
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
						" <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
						"  <xml-subtree>" +
						"   <plugType>PLUG-1.0</plugType>" +
						"   <schemaMountPoint>" +
						"    <choice-case-container xmlns=\"schema-mount-test\">" +
						"		<case2-must-leaf>must-leaf</case2-must-leaf>"+
						"	 	<outer-default-leaf>two</outer-default-leaf>"+
						"    </choice-case-container>" +
						"   </schemaMountPoint>" +
						"  </xml-subtree>" +
						" </validation>"
						;
		NetConfResponse ncResponse = editConfigAsFalse(request);
		assertEquals("Violate must constraints: ../outer-default-leaf = 'one'", ncResponse.getErrors().get(0).getErrorMessage());
		assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/smt:choice-case-container/smt:case2-must-leaf", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("must-violation", ncResponse.getErrors().get(0).getErrorAppTag());
		assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, ncResponse.getErrors().get(0).getErrorTag());
	}

	@Test
	public void testMustConstraintWithLeafList_ChoiceCaseNodeswithDefaultLeaf() throws Exception {
		// create leaf-list nodes which has must constraint
		String request = 
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
						" <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
						"  <xml-subtree>" +
						"   <plugType>PLUG-1.0</plugType>" +
						"   <schemaMountPoint>" +
						"    <choice-case-container xmlns=\"schema-mount-test\">" +
						"		<case3-must-leaf-list>leaflist1</case3-must-leaf-list>"+
						"		<case3-leaf>case3</case3-leaf>"+
						"    </choice-case-container>" +
						"   </schemaMountPoint>" +
						"  </xml-subtree>" +
						" </validation>"
						;
		editConfig(request);

		// verify GET response
		String response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ "  <data>"
						+ "   <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
						+ "    <validation:xml-subtree>"
						+ "     <validation:plugType>PLUG-1.0</validation:plugType>" 
						+ "     <validation:schemaMountPoint>"
						+ "      <smt:choice-case-container xmlns:smt=\"schema-mount-test\">"
						+ "			<smt:case3-leaf>case3</smt:case3-leaf>"
						+"			<smt:case3-default-leaf>case3</smt:case3-default-leaf>"
						+" 			<smt:case3-must-leaf-list>leaflist1</smt:case3-must-leaf-list>"
						+"			<smt:outer-default-leaf>one</smt:outer-default-leaf>"
						+ "      </smt:choice-case-container>"
						+ "     </validation:schemaMountPoint>"
						+ "    </validation:xml-subtree>"
						+ "   </validation:validation>"
						+ "  </data>"
						+ " </rpc-reply>"
						;
		verifyGet(m_server, m_clientInfo, response);

		// remove existing leaf-list node and create new leaf-list node and modify an existing leaf value in CASE3
		request = 
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
						" <validation xmlns=\"urn:org:bbf2:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
						"  <xml-subtree>" +
						"   <plugType>PLUG-1.0</plugType>" +
						"   <schemaMountPoint>" +
						"    <choice-case-container xmlns=\"schema-mount-test\">" +
						"		<case3-must-leaf-list xc:operation=\"remove\">leaflist1</case3-must-leaf-list>"+
						"		<case3-must-leaf-list>leaflist2</case3-must-leaf-list>"+
						"		<case3-leaf>case33</case3-leaf>"+
						"    </choice-case-container>" +
						"   </schemaMountPoint>" +
						"  </xml-subtree>" +
						" </validation>"
						;
		editConfig(request);

		// verify GET response
		response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ "  <data>"
						+ "   <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
						+ "    <validation:xml-subtree>"
						+ "     <validation:plugType>PLUG-1.0</validation:plugType>" 
						+ "     <validation:schemaMountPoint>"
						+ "      <smt:choice-case-container xmlns:smt=\"schema-mount-test\">"
						+ "			<smt:case3-leaf>case33</smt:case3-leaf>"
						+"			<smt:case3-default-leaf>case3</smt:case3-default-leaf>"
						+" 			<smt:case3-must-leaf-list>leaflist2</smt:case3-must-leaf-list>"
						+"			<smt:outer-default-leaf>one</smt:outer-default-leaf>"
						+ "      </smt:choice-case-container>"
						+ "     </validation:schemaMountPoint>"
						+ "    </validation:xml-subtree>"
						+ "   </validation:validation>"
						+ "  </data>"
						+ " </rpc-reply>"
						;
		verifyGet(m_server, m_clientInfo, response);

		// change the default-leaf value --> must constraint fails for leaf-list node
		request = 
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
						" <validation xmlns=\"urn:org:bbf2:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
						"  <xml-subtree>" +
						"   <plugType>PLUG-1.0</plugType>" +
						"   <schemaMountPoint>" +
						"    <choice-case-container xmlns=\"schema-mount-test\">" +
						"		<case3-default-leaf>default-leaf</case3-default-leaf>"+
						"    </choice-case-container>" +
						"   </schemaMountPoint>" +
						"  </xml-subtree>" +
						" </validation>"
						;

		NetConfResponse ncResponse = editConfigAsFalse(request);
		assertEquals("Violate must constraints: ../case3-default-leaf = 'case3'", ncResponse.getErrors().get(0).getErrorMessage());
		assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/smt:choice-case-container/smt:case3-must-leaf-list", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("must-violation", ncResponse.getErrors().get(0).getErrorAppTag());
		assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, ncResponse.getErrors().get(0).getErrorTag());
	}

	@Test
	public void testMustConstraint_OuterLeafPointsToChoiceCaseNodes() throws Exception {
		// create outer must-leaf which is point to CASE4 leaf nodes
		String request = 
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
						" <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
						"  <xml-subtree>" +
						"   <plugType>PLUG-1.0</plugType>" +
						"   <schemaMountPoint>" +
						"    <choice-case-container xmlns=\"schema-mount-test\">" +
						"		<case4-leaf>case4</case4-leaf>"+
						"		<outer-must-leaf>outerleaf</outer-must-leaf>"+
						"    </choice-case-container>" +
						"   </schemaMountPoint>" +
						"  </xml-subtree>" +
						" </validation>"
						;
		editConfig(request);

		// verify GET response
		String response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ "  <data>"
						+ "   <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
						+ "    <validation:xml-subtree>"
						+ "     <validation:plugType>PLUG-1.0</validation:plugType>" 
						+ "     <validation:schemaMountPoint>"
						+ "      <smt:choice-case-container xmlns:smt=\"schema-mount-test\">"
						+ "			<smt:case4-leaf>case4</smt:case4-leaf>"
						+ " 		<smt:outer-must-leaf>outerleaf</smt:outer-must-leaf>"	
						+"			<smt:outer-default-leaf>one</smt:outer-default-leaf>"
						+ "      </smt:choice-case-container>"
						+ "     </validation:schemaMountPoint>"
						+ "    </validation:xml-subtree>"
						+ "   </validation:validation>"
						+ "  </data>"
						+ " </rpc-reply>"
						;
		verifyGet(m_server, m_clientInfo, response);

		// delete CASE4 node --> it will automatically created default case (here CASE1), so must constraint should be failed for outer leaf
		request = 
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
						" <validation xmlns=\"urn:org:bbf2:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
						"  <xml-subtree>" +
						"   <plugType>PLUG-1.0</plugType>" +
						"   <schemaMountPoint>" +
						"    <choice-case-container xmlns=\"schema-mount-test\">" +
						"		<case4-leaf xc:operation=\"remove\">case4</case4-leaf>"+
						"    </choice-case-container>" +
						"   </schemaMountPoint>" +
						"  </xml-subtree>" +
						" </validation>"
						;

		NetConfResponse ncResponse = editConfigAsFalse(request);
		assertEquals("Violate must constraints: ../case4-leaf = 'case4'", ncResponse.getErrors().get(0).getErrorMessage());
		assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/smt:choice-case-container/smt:outer-must-leaf", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("must-violation", ncResponse.getErrors().get(0).getErrorAppTag());
		assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, ncResponse.getErrors().get(0).getErrorTag());
	}

	@Test
	public void testMustConstraint_OuterLeafPointsToChoiceCaseNodes1() throws Exception {
		// create outer must-leaf which is point to CASE4 leaf nodes
		String request = 
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
						" <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
						"  <xml-subtree>" +
						"   <plugType>PLUG-1.0</plugType>" +
						"   <schemaMountPoint>" +
						"    <choice-case-container xmlns=\"schema-mount-test\">" +
						"		<case4-leaf>case4</case4-leaf>"+
						"		<outer-must-leaf>outerleaf</outer-must-leaf>"+
						"    </choice-case-container>" +
						"   </schemaMountPoint>" +
						"  </xml-subtree>" +
						" </validation>"
						;
		editConfig(request);

		// verify GET response
		String response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ "  <data>"
						+ "   <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
						+ "    <validation:xml-subtree>"
						+ "     <validation:plugType>PLUG-1.0</validation:plugType>" 
						+ "     <validation:schemaMountPoint>"
						+ "      <smt:choice-case-container xmlns:smt=\"schema-mount-test\">"
						+ "			<smt:case4-leaf>case4</smt:case4-leaf>"
						+ " 		<smt:outer-must-leaf>outerleaf</smt:outer-must-leaf>"	
						+"			<smt:outer-default-leaf>one</smt:outer-default-leaf>"
						+ "      </smt:choice-case-container>"
						+ "     </validation:schemaMountPoint>"
						+ "    </validation:xml-subtree>"
						+ "   </validation:validation>"
						+ "  </data>"
						+ " </rpc-reply>"
						;
		verifyGet(m_server, m_clientInfo, response);

		//delete both CASE4 nodes as well as outer-must-leaf --> default case will be created automatically
		request = 
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
						" <validation xmlns=\"urn:org:bbf2:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
						"  <xml-subtree>" +
						"   <plugType>PLUG-1.0</plugType>" +
						"   <schemaMountPoint>" +
						"    <choice-case-container xmlns=\"schema-mount-test\">" +
						"		<outer-must-leaf xc:operation=\"remove\">outerleaf</outer-must-leaf>"+
						"		<case4-leaf xc:operation=\"remove\">case4</case4-leaf>"+
						"    </choice-case-container>" +
						"   </schemaMountPoint>" +
						"  </xml-subtree>" +
						" </validation>"
						;
		editConfig(request);

		// verify GET response for default CASE nodes
		response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ "  <data>"
						+ "   <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
						+ "    <validation:xml-subtree>"
						+ "     <validation:plugType>PLUG-1.0</validation:plugType>" 
						+ "     <validation:schemaMountPoint>"
						+ "      <smt:choice-case-container xmlns:smt=\"schema-mount-test\">"
						+"			<smt:case1-default-leaf>case1</smt:case1-default-leaf>"
						+"			<smt:outer-default-leaf>one</smt:outer-default-leaf>"
						+ "      </smt:choice-case-container>"
						+ "     </validation:schemaMountPoint>"
						+ "    </validation:xml-subtree>"
						+ "   </validation:validation>"
						+ "  </data>"
						+ " </rpc-reply>"
						;
		verifyGet(m_server, m_clientInfo, response);
	}

	@Test
	@Ignore
	public void testMustConstraint_OuterLeafPointsToChoiceCaseNodes_SwitchBetweenCases() throws Exception {

		// create outer must-leaf which is point to CASE4 leaf nodes
		String request = 
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
						" <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
						"  <xml-subtree>" +
						"   <plugType>PLUG-1.0</plugType>" +
						"   <schemaMountPoint>" +
						"    <choice-case-container xmlns=\"schema-mount-test\">" +
						"		<case4-leaf>case4</case4-leaf>"+
						"		<outer-must-leaf>outerleaf</outer-must-leaf>"+
						"    </choice-case-container>" +
						"   </schemaMountPoint>" +
						"  </xml-subtree>" +
						" </validation>"
						;
		editConfig(request);

		// verify GET response
		String response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ "  <data>"
						+ "   <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
						+ "    <validation:xml-subtree>"
						+ "     <validation:plugType>PLUG-1.0</validation:plugType>" 
						+ "     <validation:schemaMountPoint>"
						+ "      <smt:choice-case-container xmlns:smt=\"schema-mount-test\">"
						+ "			<smt:case4-leaf>case4</smt:case4-leaf>"
						+ " 		<smt:outer-must-leaf>outerleaf</smt:outer-must-leaf>"	
						+"			<smt:outer-default-leaf>one</smt:outer-default-leaf>"
						+ "      </smt:choice-case-container>"
						+ "     </validation:schemaMountPoint>"
						+ "    </validation:xml-subtree>"
						+ "   </validation:validation>"
						+ "  </data>"
						+ " </rpc-reply>"
						;
		verifyGet(m_server, m_clientInfo, response);

		//create CASE2 nodes -->
		//	1) It will automatically delete all the nodes from CASE4 and create CASE2 nodes, 
		//  2) so in this case, must-constraint will be failed for outer-must-leaf since it is already points to CASE4 nodes
		request = 
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
						" <validation xmlns=\"urn:org:bbf2:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
						"  <xml-subtree>" +
						"   <plugType>PLUG-1.0</plugType>" +
						"   <schemaMountPoint>" +
						"    <choice-case-container xmlns=\"schema-mount-test\">" +
						"		<case2-leaf>case2</case2-leaf>"+
						"    </choice-case-container>" +
						"   </schemaMountPoint>" +
						"  </xml-subtree>" +
						" </validation>"
						;
		editConfigAsFalse(request);
	}

	@Test
	public void testWhenConstraintWithChoice_ImpactValidation() throws Exception {
		// create Choice - CASE1 nodes with invalid when constraint
		String request =
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
						" <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
						"  <xml-subtree>" +
						"   <plugType>PLUG-1.0</plugType>" +
						"   <schemaMountPoint>" +
						"	 <schemaMount  xmlns=\"schema-mount-test\">" +
						"     <when-container>" +
						"       <when-leaf>must</when-leaf>" +
						"    	<choice-container>" +
						"			<leaf1>case1</leaf1>" +
						"    	</choice-container>" +
						"    </when-container>" +
						"	 </schemaMount>" +
						"   </schemaMountPoint>" +
						"  </xml-subtree>" +
						" </validation>";

		//validate Error messages
		NetConfResponse ncResponse = editConfigAsFalse(request);
		assertEquals("Violate when constraints: /schemaMount/when-container/when-leaf = 'when'", ncResponse.getErrors().get(0).getErrorMessage());
		assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/smt:schemaMount/smt:when-container/smt:choice-container/smt:leaf1", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("when-violation", ncResponse.getErrors().get(0).getErrorAppTag());
		assertEquals(NetconfRpcErrorTag.UNKNOWN_ELEMENT, ncResponse.getErrors().get(0).getErrorTag());

		//Create Choice-case nodes
		request =
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
						" <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
						"  <xml-subtree>" +
						"   <plugType>PLUG-1.0</plugType>" +
						"   <schemaMountPoint>" +
						"	 <schemaMount  xmlns=\"schema-mount-test\">" +
						"     <when-container>" +
						"       <when-leaf>when</when-leaf>" +
						"    	<choice-container>" +
						"			<leaf1>case1</leaf1>" +
						"    	</choice-container>" +
						"    </when-container>" +
						"	 </schemaMount>" +
						"   </schemaMountPoint>" +
						"  </xml-subtree>" +
						" </validation>";

		editConfig(request);

		// Verify GET response
		String response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ "  <data>"
						+ "   <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
						+ "    <validation:xml-subtree>"
						+ "     <validation:plugType>PLUG-1.0</validation:plugType>"
						+ "     <schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">"
						+ "      <smt:schemaMount xmlns:smt=\"schema-mount-test\">"
						+"		<smt:boolean-validation/>"
						+ "		 <smt:channelpair/>"
						+ "		 <smt:classifiers/>"
						+ "       <smt:configure>"
						+ "		  <smt:qos/>"
						+ "		  <smt:service/>"
						+ " 	 </smt:configure>"
						+ "		 <smt:container1>"
						+ "			<smt:choice-container/>"
						+ " 		<smt:trap/>"
						+ "		 </smt:container1>"
						+ "		 <smt:forwarding/>"
						+ "		 <smt:nested-predicates/>"
						+ "		 <smt:pae>\n" +
						"          <smt:port-capabilities/>\n" +
						"        </smt:pae>"
						+ "		 <smt:predicates-with-operation/>"
						+ "      <smt:outerContainer11/>"
						+ "      <smt:when-container>"
						+ "      <smt:when-leaf>when</smt:when-leaf>"
						+ "      <smt:choice-container>"
						+ "			<smt:leaf1>case1</smt:leaf1>"
						+ "      </smt:choice-container>"
						+ "      </smt:when-container>"
						+ "      </smt:schemaMount>"
						+ "     </schemaMountPoint>"
						+ "    </validation:xml-subtree>"
						+ "   </validation:validation>"
						+ "  </data>"
						+ " </rpc-reply>";
		verifyGet(m_server, m_clientInfo, response);

		// Modify 'when-leaf' value
		request =
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
						" <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
						"  <xml-subtree>" +
						"   <plugType>PLUG-1.0</plugType>" +
						"   <schemaMountPoint>" +
						"	 <schemaMount  xmlns=\"schema-mount-test\">" +
						"     <when-container>" +
						"       <when-leaf>must</when-leaf>" +
						"    </when-container>" +
						"	 </schemaMount>" +
						"   </schemaMountPoint>" +
						"  </xml-subtree>" +
						" </validation>";

		editConfig(request);

		// Choice-case node should be removed due to impact validation of 'when-leaf' node
		response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ "  <data>"
						+ "   <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
						+ "    <validation:xml-subtree>"
						+ "     <validation:plugType>PLUG-1.0</validation:plugType>"
						+ "     <schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">"
						+ "      <smt:schemaMount xmlns:smt=\"schema-mount-test\">"
						+"		<smt:boolean-validation/>"
						+ "		 <smt:channelpair/>"
						+ "		 <smt:classifiers/>"
						+ "       <smt:configure>"
						+ "		  <smt:qos/>"
						+ "		  <smt:service/>"
						+ " 	 </smt:configure>"
						+ "		 <smt:container1>"
						+ "			<smt:choice-container/>"
						+ " 		<smt:trap/>"
						+ "		 </smt:container1>"
						+ "		 <smt:forwarding/>"
						+ "		 <smt:nested-predicates/>"
						+ "		 <smt:outerContainer11/>"
						+ "		 <smt:pae>\n" +
						"          <smt:port-capabilities/>\n" +
						"        </smt:pae>"
						+ "		 <smt:predicates-with-operation/>"
						+ "      <smt:when-container>"
						+ "      <smt:when-leaf>must</smt:when-leaf>"
						+ "      </smt:when-container>"
						+ "      </smt:schemaMount>"
						+ "     </schemaMountPoint>"
						+ "    </validation:xml-subtree>"
						+ "   </validation:validation>"
						+ "  </data>"
						+ " </rpc-reply>"
		;
		verifyGet(m_server, m_clientInfo, response);
	}

	@Test
	public void testDefaultLeafWithImpactValidation_NestedChoiceCases() throws Exception {

		String requestXml =
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    "
						+ " <validation xmlns=\"urn:org:bbf2:pma:validation\">"
						+ "  <xml-subtree>"
						+ "   <plugType>PLUG-1.0</plugType>"
						+ "   <schemaMountPoint>"
						+ "	  <schemaMount  xmlns=\"schema-mount-test\">"
						+ "   <outerContainer11>"
						+ "     <url-leaf>url-string</url-leaf>"
						+ "     <case1-string-leaf>mystring</case1-string-leaf>"
						+ "     <nested-case2-leaf-list>leaflist1</nested-case2-leaf-list>"
						+ "     <nested-case2-leaf-list>leaflist2</nested-case2-leaf-list>"
						+ "    </outerContainer11>"
						+ "	  </schemaMount>"
						+ "   </schemaMountPoint>"
						+ "  </xml-subtree>"
						+ " </validation>";
		editConfig(requestXml);

		String responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" +
				"<data>" +
				"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
				"<validation:xml-subtree>" +
				"<validation:plugType>PLUG-1.0</validation:plugType>" +
				"<schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">" +
				"<smt:schemaMount xmlns:smt=\"schema-mount-test\">" +
				"		 <smt:pae>\n" +
				"          <smt:port-capabilities/>\n" +
				"        </smt:pae>"+
				"	<smt:boolean-validation/>" +
				"  <smt:channelpair/>" +
				"<smt:classifiers/>" +
				"<smt:configure>" +
				"<smt:qos/>" +
				"<smt:service/>" +
				"</smt:configure>" +
				"<smt:container1>" +
				"  <smt:choice-container/>" +
				"<smt:trap/>" +
				"</smt:container1>" +
				"<smt:forwarding/>" +
				"<smt:nested-predicates/>" +
				"<smt:outerContainer11>" +
				"<smt:case1-string-leaf>mystring</smt:case1-string-leaf>" +
				"<smt:nested-case2-leaf-list>leaflist1</smt:nested-case2-leaf-list>" +
				"<smt:nested-case2-leaf-list>leaflist2</smt:nested-case2-leaf-list>" +
				"<smt:url-leaf>url-string</smt:url-leaf>" +
				"</smt:outerContainer11>" +
				"<smt:predicates-with-operation/>" +
				"<smt:when-container>" +
				"  <smt:choice-container/>" +
				"</smt:when-container>" +
				"</smt:schemaMount>" +
				"</schemaMountPoint>" +
				"</validation:xml-subtree>" +
				"</validation:validation>" +
				"</data>" +
				"</rpc-reply>";

		verifyGet(responseXml);

		// switch the nested case && change the impact node value --> default leaf should not be created under nested case
		requestXml =
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    "
						+ " <validation xmlns=\"urn:org:bbf2:pma:validation\">"
						+ "  <xml-subtree>"
						+ "   <plugType>PLUG-1.0</plugType>"
						+ "   <schemaMountPoint>"
						+ "	  <schemaMount  xmlns=\"schema-mount-test\">"
						+ " <outerContainer11>"
						+ "  <case1-string-leaf>test</case1-string-leaf>"
						+ "  <nested-case1-enum-leaf>first</nested-case1-enum-leaf>"
						+ "  </outerContainer11>"
						+ "	  </schemaMount>"
						+ "   </schemaMountPoint>"
						+ "  </xml-subtree>"
						+ " </validation>";
		editConfig(requestXml);

		responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" +
				"<data>" +
				"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
				"<validation:xml-subtree>" +
				"<validation:plugType>PLUG-1.0</validation:plugType>" +
				"<schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">" +
				"<smt:schemaMount xmlns:smt=\"schema-mount-test\">" +
				"		 <smt:pae>\n" +
				"          <smt:port-capabilities/>\n" +
				"        </smt:pae>"+
				"  <smt:channelpair/>" +
				"	<smt:boolean-validation/>" +
				"<smt:classifiers/>" +
				"<smt:configure>" +
				"<smt:qos/>" +
				"<smt:service/>" +
				"</smt:configure>" +
				"<smt:container1>" +
				"  <smt:choice-container/>" +
				"<smt:trap/>" +
				"</smt:container1>" +
				"<smt:forwarding/>" +
				"<smt:nested-predicates/>" +
				"<smt:outerContainer11>" +
				"<smt:case1-string-leaf>test</smt:case1-string-leaf>" +
				"<smt:nested-case1-enum-leaf>first</smt:nested-case1-enum-leaf>" +
				"<smt:url-leaf>url-string</smt:url-leaf>" +
				"</smt:outerContainer11>" +
				"<smt:predicates-with-operation/>" +
				"<smt:when-container>" +
				"  <smt:choice-container/>" +
				"</smt:when-container>" +
				"</smt:schemaMount>" +
				"</schemaMountPoint>" +
				"</validation:xml-subtree>" +
				"</validation:validation>" +
				"</data>" +
				"</rpc-reply>";

		verifyGet(responseXml);

		// change impact node value --> default leaf should be created
		requestXml =
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    "
						+ " <validation xmlns=\"urn:org:bbf2:pma:validation\">"
						+ "  <xml-subtree>"
						+ "   <plugType>PLUG-1.0</plugType>"
						+ "   <schemaMountPoint>"
						+ "	  <schemaMount  xmlns=\"schema-mount-test\">"
						+ "   <outerContainer11>"
						+ "     <case1-string-leaf>mystring</case1-string-leaf>"
						+ "		<nested-case1-leaf>leaf1</nested-case1-leaf>"
						+ "    </outerContainer11>"
						+ "	  </schemaMount>"
						+ "   </schemaMountPoint>"
						+ "  </xml-subtree>"
						+ " </validation>";
		editConfig(requestXml);

		responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" +
				"<data>" +
				"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
				"<validation:xml-subtree>" +
				"<validation:plugType>PLUG-1.0</validation:plugType>" +
				"<schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">" +
				"<smt:schemaMount xmlns:smt=\"schema-mount-test\">" +
				"		 <smt:pae>\n" +
				"          <smt:port-capabilities/>\n" +
				"        </smt:pae>"+
				"	<smt:boolean-validation/>" +
				"  <smt:channelpair/>" +
				"<smt:classifiers/>" +
				"<smt:configure>" +
				"<smt:qos/>" +
				"<smt:service/>" +
				"</smt:configure>" +
				"<smt:container1>" +
				"  <smt:choice-container/>" +
				"<smt:trap/>" +
				"</smt:container1>" +
				"<smt:forwarding/>" +
				"<smt:nested-predicates/>" +
				"<smt:outerContainer11>" +
				"<smt:case1-string-leaf>mystring</smt:case1-string-leaf>" +
				"<smt:nested-case1-enum-leaf>first</smt:nested-case1-enum-leaf>" +
				"<smt:nested-case1-leaf>leaf1</smt:nested-case1-leaf>" +
				"<smt:nested-case1-default-leaf>default-leaf</smt:nested-case1-default-leaf>" +
				"<smt:url-leaf>url-string</smt:url-leaf>" +
				"</smt:outerContainer11>" +
				"<smt:predicates-with-operation/>" +
				"<smt:when-container>" +
				"  <smt:choice-container/>" +
				"</smt:when-container>" +
				"</smt:schemaMount>" +
				"</schemaMountPoint>" +
				"</validation:xml-subtree>" +
				"</validation:validation>" +
				"</data>" +
				"</rpc-reply>";
		verifyGet(responseXml);

		// switch to outer choice/case nodes --> created reference-case2-leaf AND nested-case1 CASE nodes should be removed

		requestXml =
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    "
						+ " <validation xmlns=\"urn:org:bbf2:pma:validation\">"
						+ "  <xml-subtree>"
						+ "   <plugType>PLUG-1.0</plugType>"
						+ "   <schemaMountPoint>"
						+ "	  <schemaMount  xmlns=\"schema-mount-test\">"
						+ "   <outerContainer11>"
						+ "     <reference-case2-leaf>outer-choice-case2</reference-case2-leaf>"
						+ "    </outerContainer11>"
						+ "	  </schemaMount>"
						+ "   </schemaMountPoint>"
						+ "  </xml-subtree>"
						+ " </validation>";
		editConfig(requestXml);

		responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" +
				"<data>" +
				"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
				"<validation:xml-subtree>" +
				"<validation:plugType>PLUG-1.0</validation:plugType>" +
				"<schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">" +
				"<smt:schemaMount xmlns:smt=\"schema-mount-test\">" +
				"		 <smt:pae>\n" +
				"          <smt:port-capabilities/>\n" +
				"        </smt:pae>"+
				"	<smt:boolean-validation/>" +
				"  <smt:channelpair/>" +
				"<smt:classifiers/>" +
				"<smt:configure>" +
				"<smt:qos/>" +
				"<smt:service/>" +
				"</smt:configure>" +
				"<smt:container1>" +
				"  <smt:choice-container/>" +
				"<smt:trap/>" +
				"</smt:container1>" +
				"<smt:forwarding/>" +
				"<smt:nested-predicates/>" +
				"<smt:outerContainer11>" +
				"<smt:reference-case2-leaf>outer-choice-case2</smt:reference-case2-leaf>" +
				"<smt:url-leaf>url-string</smt:url-leaf>" +
				"</smt:outerContainer11>" +
				"<smt:predicates-with-operation/>" +
				"<smt:when-container>" +
				"  <smt:choice-container/>" +
				"</smt:when-container>" +
				"</smt:schemaMount>" +
				"</schemaMountPoint>" +
				"</validation:xml-subtree>" +
				"</validation:validation>" +
				"</data>" +
				"</rpc-reply>";
		verifyGet(responseXml);
	}
}
