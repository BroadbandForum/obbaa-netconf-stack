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
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(RequestScopeJunitRunner.class)
public class UnionTypeLeafListDSValidatorTest extends AbstractDataStoreValidatorTest{

	private String getLeafListEditRequest(String requestLeafXml) {
		String requestXml_prefix = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    "
				+ " <type-validation xmlns=\"urn:org:bbf:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
				+ "	<union-type-validation>"
				+"<leaf-list-with-union-type>";
		String requestXml_suffix = "</leaf-list-with-union-type>"
				+"	</union-type-validation>" 
				+ " </type-validation>";
		return requestXml_prefix + requestLeafXml + requestXml_suffix;
	}

	/**
	 * UTs are covered Union Type for Leaf-list schema node
	 */

	@Test
	public void testLeafListWithUnion_IntType() throws Exception{

		getModelNode();
		//Verify Integer type
		String requestXml = getLeafListEditRequest("<name>11</name>");
		editConfig(m_server, m_clientInfo, requestXml, true);
		String responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
				+ "	<type-validator:type-validation xmlns:type-validator=\"urn:org:bbf:pma:validation\">"
				+ "		<type-validator:union-type-validation>" 
				+"			<type-validator:leaf-list-with-union-type>"
				+ "				<type-validator:name>11</type-validator:name>"
				+"			</type-validator:leaf-list-with-union-type>"
				+ "		</type-validator:union-type-validation>"
				+ "	</type-validator:type-validation>"
				+ " <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>" + "</data>"
				+ "</rpc-reply>";
		verifyGet(responseXml);

		requestXml = getLeafListEditRequest("<name>14</name>");
		editConfig(m_server, m_clientInfo, requestXml, true);
		responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
				+ "	<type-validator:type-validation xmlns:type-validator=\"urn:org:bbf:pma:validation\">"
				+ "		<type-validator:union-type-validation>" 
				+"			<type-validator:leaf-list-with-union-type>"
				+ "				<type-validator:name>11</type-validator:name>"
				+ "				<type-validator:name>14</type-validator:name>"
				+"			</type-validator:leaf-list-with-union-type>"
				+ "		</type-validator:union-type-validation>"
				+ "	</type-validator:type-validation>"
				+ " <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>" + "</data>"
				+ "</rpc-reply>";
		verifyGet(responseXml);

		// verify out of range
		requestXml = getLeafListEditRequest("<name>100</name>");
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
		assertEquals(1,response.getErrors().size());
		assertEquals("/type-validator:type-validation/type-validator:union-type-validation/type-validator:leaf-list-with-union-type/type-validator:name", response.getErrors().get(0).getErrorPath());
		assertEquals("The argument is out of bounds <1, 25> or Value \"100\" is an invalid value. Expected values: [mumbai] or Supplied value does not match the regular expression ^(?:[a-dS-X].*)$. or Missing required element 100 or Dependency violated, '100' must exist", response.getErrors().get(0).getErrorMessage());

		//max element case
		requestXml = getLeafListEditRequest("<name>15</name><name>20</name>");
		response = editConfig(m_server, m_clientInfo, requestXml, false);
		assertEquals(1,response.getErrors().size());
		assertEquals("/type-validator:type-validation/type-validator:union-type-validation/type-validator:leaf-list-with-union-type/type-validator:name", response.getErrors().get(0).getErrorPath());
		assertEquals("Maximum elements allowed for name is 3.", response.getErrors().get(0).getErrorMessage());
	}

	@Test
	public void testLeafListWithUnion_EnumType() throws Exception{

		getModelNode();
		// Verify enum type
		String requestXml = getLeafListEditRequest("<name>mumbai</name>");
		editConfig(m_server, m_clientInfo, requestXml, true);
		String responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
				+ "	<type-validator:type-validation xmlns:type-validator=\"urn:org:bbf:pma:validation\">"
				+ "		<type-validator:union-type-validation>" 
				+"			<type-validator:leaf-list-with-union-type>"
				+ "				<type-validator:name>mumbai</type-validator:name>"
				+"			</type-validator:leaf-list-with-union-type>"
				+ "		</type-validator:union-type-validation>"
				+ "	</type-validator:type-validation>"
				+ " <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>" + "</data>"
				+ "</rpc-reply>";
		verifyGet(responseXml);

		// invalid enum value
		requestXml = getLeafListEditRequest("<name>noida</name>");
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
		assertEquals(1,response.getErrors().size());
		assertEquals("/type-validator:type-validation/type-validator:union-type-validation/type-validator:leaf-list-with-union-type/type-validator:name", response.getErrors().get(0).getErrorPath());
		assertEquals("The argument is out of bounds <1, 25> or Value \"noida\" is an invalid value. Expected values: [mumbai] or Supplied value does not match the regular expression ^(?:[a-dS-X].*)$. or Missing required element noida or Dependency violated, 'noida' must exist", response.getErrors().get(0).getErrorMessage());
	}

	@Test
	public void testLeafListWithUnion_StringType() throws Exception{
		getModelNode();
		
		// verify string type
		String requestXml = getLeafListEditRequest("<name>chennai</name>");
		editConfig(m_server, m_clientInfo, requestXml, true);
		String responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
				+ "	<type-validator:type-validation xmlns:type-validator=\"urn:org:bbf:pma:validation\">"
				+ "		<type-validator:union-type-validation>" 
				+"			<type-validator:leaf-list-with-union-type>"
				+ "				<type-validator:name>chennai</type-validator:name>"
				+"			</type-validator:leaf-list-with-union-type>"
				+ "		</type-validator:union-type-validation>"
				+ "	</type-validator:type-validation>"
				+ " <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>" + "</data>"
				+ "</rpc-reply>";
		verifyGet(responseXml);

		requestXml = getLeafListEditRequest("<name>Sydney</name>");
		editConfig(m_server, m_clientInfo, requestXml, true);
		responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
				+ "	<type-validator:type-validation xmlns:type-validator=\"urn:org:bbf:pma:validation\">"
				+ "		<type-validator:union-type-validation>" 
				+"			<type-validator:leaf-list-with-union-type>"
				+ "				<type-validator:name>chennai</type-validator:name>"
				+ "				<type-validator:name>Sydney</type-validator:name>"
				+"			</type-validator:leaf-list-with-union-type>"
				+ "		</type-validator:union-type-validation>"
				+ "	</type-validator:type-validation>"
				+ " <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>" + "</data>"
				+ "</rpc-reply>";
		verifyGet(responseXml);

		// verify max element
		requestXml = getLeafListEditRequest("<name>delhi</name><name>chandigarh</name>");
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
		assertEquals(1,response.getErrors().size());
		assertEquals("/type-validator:type-validation/type-validator:union-type-validation/type-validator:leaf-list-with-union-type/type-validator:name", response.getErrors().get(0).getErrorPath());
		assertEquals("Maximum elements allowed for name is 3.", response.getErrors().get(0).getErrorMessage());
	}

	@Test
	public void testLeafListWithUnion_InvalidStringType() throws Exception{
		getModelNode();
		//verify string type
		String requestXml = getLeafListEditRequest("<name>chennai</name>");
		editConfig(m_server, m_clientInfo, requestXml, true);
		String responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
				+ "	<type-validator:type-validation xmlns:type-validator=\"urn:org:bbf:pma:validation\">"
				+ "		<type-validator:union-type-validation>" 
				+"			<type-validator:leaf-list-with-union-type>"
				+ "				<type-validator:name>chennai</type-validator:name>"
				+"			</type-validator:leaf-list-with-union-type>"
				+ "		</type-validator:union-type-validation>"
				+ "	</type-validator:type-validation>"
				+ " <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>" + "</data>"
				+ "</rpc-reply>";
		verifyGet(responseXml);

		// Invalid string pattern
		requestXml = getLeafListEditRequest("<name>kolkata</name>");
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
		assertEquals(1,response.getErrors().size());
		assertEquals("/type-validator:type-validation/type-validator:union-type-validation/type-validator:leaf-list-with-union-type/type-validator:name", response.getErrors().get(0).getErrorPath());
		assertEquals("The argument is out of bounds <1, 25> or Value \"kolkata\" is an invalid value. Expected values: [mumbai] or Supplied value does not match the regular expression ^(?:[a-dS-X].*)$. or Missing required element kolkata or Dependency violated, 'kolkata' must exist", response.getErrors().get(0).getErrorMessage());

	}

	@Test
	public void testLeafListWithUnion_LeafrefType() throws Exception{
		getModelNode();
		//verify leaf-ref type
		String requestXml = getLeafListEditRequest("<list1><id>65</id><reference-leaf>65</reference-leaf></list1><name>65</name>");
		editConfig(m_server, m_clientInfo, requestXml, true);
		String responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
				+ "	<type-validator:type-validation xmlns:type-validator=\"urn:org:bbf:pma:validation\">"
				+ "		<type-validator:union-type-validation>" 
				+"			<type-validator:leaf-list-with-union-type>"
				+ "				<type-validator:name>65</type-validator:name>"
				+"				<type-validator:list1>"
				+"				<type-validator:id>65</type-validator:id>"
				+"				<type-validator:reference-leaf>65</type-validator:reference-leaf>"
				+"				</type-validator:list1>"
				+"			</type-validator:leaf-list-with-union-type>"
				+ "		</type-validator:union-type-validation>"
				+ "	</type-validator:type-validation>"
				+ " <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>" + "</data>"
				+ "</rpc-reply>";
		verifyGet(responseXml);

		// remove leaf-ref reference node
		requestXml = getLeafListEditRequest("<list1><id>65</id><reference-leaf xc:operation=\"delete\">65</reference-leaf></list1>");
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
		assertEquals(1,response.getErrors().size());
		assertEquals("/type-validator:type-validation/type-validator:union-type-validation/type-validator:leaf-list-with-union-type/type-validator:name", response.getErrors().get(0).getErrorPath());
		assertEquals("The argument is out of bounds <1, 25> or Value \"65\" is an invalid value. Expected values: [mumbai] or Supplied value does not match the regular expression ^(?:[a-dS-X].*)$. or Missing required element 65 or Dependency violated, '65' must exist", response.getErrors().get(0).getErrorMessage());
	}

	@Test
	public void testLeafListWithUnion_InstanceIdentifierType() throws Exception{
		getModelNode();
		// verify instance identifier type under union tye
		String requestXml = getLeafListEditRequest("<list1><id>65</id><instance-identifier-leaf>instance</instance-identifier-leaf></list1><name>/type-validation/union-type-validation/leaf-list-with-union-type/list1[id = '65']/instance-identifier-leaf</name>");
		editConfig(m_server, m_clientInfo, requestXml, true);
		String responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
				+ "	<type-validator:type-validation xmlns:type-validator=\"urn:org:bbf:pma:validation\">"
				+ "		<type-validator:union-type-validation>" 
				+"			<type-validator:leaf-list-with-union-type>"
				+ "				<type-validator:name>/type-validation/union-type-validation/leaf-list-with-union-type/list1[id = '65']/instance-identifier-leaf</type-validator:name>"
				+"				<type-validator:list1>"
				+"				<type-validator:id>65</type-validator:id>"
				+"				<type-validator:instance-identifier-leaf>instance</type-validator:instance-identifier-leaf>"
				+"				</type-validator:list1>"
				+"			</type-validator:leaf-list-with-union-type>"
				+ "		</type-validator:union-type-validation>"
				+ "	</type-validator:type-validation>"
				+ " <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>" + "</data>"
				+ "</rpc-reply>";
		verifyGet(responseXml);

	}

	/**
	 * Below UT covers combinations of types like int,string,enum,leaf-ref,instance-identifier 
	 */

	@Test
	public void testLeafListWithNestedUnionAndCustomUnionType() throws Exception{

		getModelNode();

		// Verify combination of Enum, Integer, String
		String requestXml = getLeafListEditRequest("<leaflist-with-nested-union>chennai</leaflist-with-nested-union><leaflist-with-nested-union>15</leaflist-with-nested-union><leaflist-with-nested-union>two</leaflist-with-nested-union><leaflist-with-nested-union>Pune</leaflist-with-nested-union>");
		editConfig(m_server, m_clientInfo, requestXml, true);
		
		//Verify Get request
		String responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
				+ "	<type-validator:type-validation xmlns:type-validator=\"urn:org:bbf:pma:validation\">"
				+ "		<type-validator:union-type-validation>" 
				+"			<type-validator:leaf-list-with-union-type>"
				+ "				<type-validator:leaflist-with-nested-union>chennai</type-validator:leaflist-with-nested-union>"
				+ "				<type-validator:leaflist-with-nested-union>Pune</type-validator:leaflist-with-nested-union>"
				+ "				<type-validator:leaflist-with-nested-union>two</type-validator:leaflist-with-nested-union>"
				+ "				<type-validator:leaflist-with-nested-union>15</type-validator:leaflist-with-nested-union>"
				+"			</type-validator:leaf-list-with-union-type>"
				+ "		</type-validator:union-type-validation>"
				+ "	</type-validator:type-validation>"
				+ " <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>" + "</data>"
				+ "</rpc-reply>";
		verifyGet(responseXml);
		
		//delete exiting leaf and add a new leaf
		requestXml = getLeafListEditRequest("<leaflist-with-nested-union xc:operation=\"delete\">15</leaflist-with-nested-union><leaflist-with-nested-union>3</leaflist-with-nested-union>");
		editConfig(m_server, m_clientInfo, requestXml, true);
		
		//Verify Get request
		responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
				+ "	<type-validator:type-validation xmlns:type-validator=\"urn:org:bbf:pma:validation\">"
				+ "		<type-validator:union-type-validation>" 
				+"			<type-validator:leaf-list-with-union-type>"
				+ "				<type-validator:leaflist-with-nested-union>chennai</type-validator:leaflist-with-nested-union>"
				+ "				<type-validator:leaflist-with-nested-union>Pune</type-validator:leaflist-with-nested-union>"
				+ "				<type-validator:leaflist-with-nested-union>two</type-validator:leaflist-with-nested-union>"
				+ "				<type-validator:leaflist-with-nested-union>3</type-validator:leaflist-with-nested-union>"
				+"			</type-validator:leaf-list-with-union-type>"
				+ "		</type-validator:union-type-validation>"
				+ "	</type-validator:type-validation>"
				+ " <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>" + "</data>"
				+ "</rpc-reply>";
		verifyGet(responseXml);

		//verify leaf-ref type
		requestXml = getLeafListEditRequest("<list1><id>65</id><reference-leaf>65</reference-leaf></list1><leaflist-with-nested-union>65</leaflist-with-nested-union>");
		editConfig(m_server, m_clientInfo, requestXml, true);

		// Leaf-ref nodes are noded in DS
		responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
				+ "	<type-validator:type-validation xmlns:type-validator=\"urn:org:bbf:pma:validation\">"
				+ "		<type-validator:union-type-validation>" 
				+"			<type-validator:leaf-list-with-union-type>"
				+ "				<type-validator:leaflist-with-nested-union>chennai</type-validator:leaflist-with-nested-union>"
				+ "				<type-validator:leaflist-with-nested-union>Pune</type-validator:leaflist-with-nested-union>"
				+ "				<type-validator:leaflist-with-nested-union>two</type-validator:leaflist-with-nested-union>"
				+ "				<type-validator:leaflist-with-nested-union>3</type-validator:leaflist-with-nested-union>"
				+ "				<type-validator:leaflist-with-nested-union>65</type-validator:leaflist-with-nested-union>"
				+"				<type-validator:list1>"
				+"				<type-validator:id>65</type-validator:id>"
				+"				<type-validator:reference-leaf>65</type-validator:reference-leaf>"
				+"				</type-validator:list1>"
				+"			</type-validator:leaf-list-with-union-type>"
				+ "		</type-validator:union-type-validation>"
				+ "	</type-validator:type-validation>"
				+ " <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>" + "</data>"
				+ "</rpc-reply>";
		verifyGet(responseXml);

		// Added another leaf-ref node and it will be refernced to already existing node.
		requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    "
				+ " <type-validation xmlns=\"urn:org:bbf:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" 
				+ " <leafref-value>65</leafref-value>"
				+ " </type-validation>";

		editConfig(m_server, m_clientInfo, requestXml, true);

		responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
				+ "	<type-validator:type-validation xmlns:type-validator=\"urn:org:bbf:pma:validation\">"
				+ "		<type-validator:leafref-value>65</type-validator:leafref-value>"
				+ "		<type-validator:union-type-validation>" 
				+ "			<type-validator:leaf-list-with-union-type>"
				+ "				<type-validator:leaflist-with-nested-union>chennai</type-validator:leaflist-with-nested-union>"
				+ "				<type-validator:leaflist-with-nested-union>Pune</type-validator:leaflist-with-nested-union>"
				+ "				<type-validator:leaflist-with-nested-union>two</type-validator:leaflist-with-nested-union>"
				+ "				<type-validator:leaflist-with-nested-union>3</type-validator:leaflist-with-nested-union>"
				+ "				<type-validator:leaflist-with-nested-union>65</type-validator:leaflist-with-nested-union>"
				+"				<type-validator:list1>"
				+"				<type-validator:id>65</type-validator:id>"
				+"				<type-validator:reference-leaf>65</type-validator:reference-leaf>"
				+"				</type-validator:list1>"
				+"			</type-validator:leaf-list-with-union-type>"
				+ "		</type-validator:union-type-validation>"
				+ "	</type-validator:type-validation>"
				+ " <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>" + "</data>"
				+ "</rpc-reply>";
		verifyGet(responseXml);

		// Trying to remove leaf-ref node. NAV should not thrown any error, because value of 65 will be matched by another leaf-ref type.

		requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    "
				+ " <type-validation xmlns=\"urn:org:bbf:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" 
				+ " <leafref-value xc:operation=\"delete\">65</leafref-value>"
				+ " </type-validation>";
		editConfig(m_server, m_clientInfo, requestXml, true);

		responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
				+ "	<type-validator:type-validation xmlns:type-validator=\"urn:org:bbf:pma:validation\">"
				+ "		<type-validator:union-type-validation>" 
				+ "			<type-validator:leaf-list-with-union-type>"
				+ "				<type-validator:leaflist-with-nested-union>chennai</type-validator:leaflist-with-nested-union>"
				+ "				<type-validator:leaflist-with-nested-union>Pune</type-validator:leaflist-with-nested-union>"
				+ "				<type-validator:leaflist-with-nested-union>two</type-validator:leaflist-with-nested-union>"
				+ "				<type-validator:leaflist-with-nested-union>3</type-validator:leaflist-with-nested-union>"
				+ "				<type-validator:leaflist-with-nested-union>65</type-validator:leaflist-with-nested-union>"
				+"				<type-validator:list1>"
				+"				<type-validator:id>65</type-validator:id>"
				+"				<type-validator:reference-leaf>65</type-validator:reference-leaf>"
				+"				</type-validator:list1>"
				+"			</type-validator:leaf-list-with-union-type>"
				+ "		</type-validator:union-type-validation>"
				+ "	</type-validator:type-validation>"
				+ " <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>" + "</data>"
				+ "</rpc-reply>";
		verifyGet(responseXml);

		// Trying to remove latest leaf-ref value.
		requestXml = getLeafListEditRequest("<list1 xc:operation=\"delete\"><id>65</id></list1>");
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
		assertEquals(1,response.getErrors().size());
		assertEquals("/type-validator:type-validation/type-validator:union-type-validation/type-validator:leaf-list-with-union-type/type-validator:leaflist-with-nested-union", response.getErrors().get(0).getErrorPath());
		assertEquals("The argument is out of bounds <10, 25> or Dependency violated, '65' must exist or Value \"65\" is an invalid value. Expected values: [one, two, three] or The argument is out of bounds <1, 5> or Value \"65\" is an invalid value. Expected values: [chennai] or Supplied value does not match the regular expression ^(?:[P-Z].*)$. or Missing required element 65 or Dependency violated, '65' must exist", response.getErrors().get(0).getErrorMessage());
	}
}
