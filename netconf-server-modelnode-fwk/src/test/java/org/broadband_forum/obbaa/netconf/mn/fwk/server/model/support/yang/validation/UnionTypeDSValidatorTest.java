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
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(RequestScopeJunitRunner.class)
public class UnionTypeDSValidatorTest extends AbstractDataStoreValidatorTest{

	private String getUnionTypeEditRequest(String requestLeafXml) {
		String requestXml_prefix = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    "
				+ " <type-validation xmlns=\"urn:org:bbf:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" 
				+ "	<union-type-validation>"
				+ "		<union-type-validation-list>" 
				+ "   		<id>12</id>";
		String requestXml_suffix =
				"		</union-type-validation-list>" 
						+ "	</union-type-validation>" 
						+ " </type-validation>";
		return requestXml_prefix + requestLeafXml + requestXml_suffix;
	}

	@Test
	public void testUnionTypeWithOnlyEnum_ValidEnumValue() throws Exception{
		getModelNode();
		String requestXml = getUnionTypeEditRequest("<union-with-only-enum-type>union</union-with-only-enum-type>");
		editConfig(m_server, m_clientInfo, requestXml, true);
		String responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
				+ "	<type-validator:type-validation xmlns:type-validator=\"urn:org:bbf:pma:validation\">"
				+ "		<type-validator:union-type-validation>" 
				+ "			<type-validator:union-type-validation-list>"
				+ "				<type-validator:id>12</type-validator:id>"
				+ "				<type-validator:union-with-only-enum-type>union</type-validator:union-with-only-enum-type>"
				+ "			</type-validator:union-type-validation-list>" 
				+ "		</type-validator:union-type-validation>"
				+ "	</type-validator:type-validation>"
				+ " <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>" + "</data>"
				+ "</rpc-reply>";
		verifyGet(responseXml);

		requestXml = getUnionTypeEditRequest("<union-with-only-enum-type>union1</union-with-only-enum-type>");
		editConfig(m_server, m_clientInfo, requestXml, true);
		responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
				+ "	<type-validator:type-validation xmlns:type-validator=\"urn:org:bbf:pma:validation\">"
				+ "		<type-validator:union-type-validation>" 
				+ "			<type-validator:union-type-validation-list>"
				+ "				<type-validator:id>12</type-validator:id>"
				+ "				<type-validator:union-with-only-enum-type>union1</type-validator:union-with-only-enum-type>"
				+ "			</type-validator:union-type-validation-list>" 
				+ "		</type-validator:union-type-validation>"
				+ "	</type-validator:type-validation>"
				+ " <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>" + "</data>"
				+ "</rpc-reply>";
		verifyGet(responseXml);
	}

	@Test
	public void testUnionTypeWithConstraintsOnBooleanEnumValidAndInvalidValues() throws Exception{
		getModelNode();
		String requestXml = getUnionTypeEditRequest("<boolean-type-container> <union-with-boolean-enum-type>false</union-with-boolean-enum-type> </boolean-type-container>");
		editConfig(m_server, m_clientInfo, requestXml, true);
		String responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
				+ "	<type-validator:type-validation xmlns:type-validator=\"urn:org:bbf:pma:validation\">"
				+ "		<type-validator:union-type-validation>" 
				+ "			<type-validator:union-type-validation-list>"
				+ "				<type-validator:id>12</type-validator:id>"
				+ "             <type-validator:boolean-type-container>"
				+ "				 <type-validator:union-with-boolean-enum-type>false</type-validator:union-with-boolean-enum-type>"
				+ "             </type-validator:boolean-type-container>"
				+ "			</type-validator:union-type-validation-list>" 
				+ "		</type-validator:union-type-validation>"
				+ "	</type-validator:type-validation>"
				+ " <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>" + "</data>"
				+ "</rpc-reply>";
		verifyGet(responseXml);

		requestXml = getUnionTypeEditRequest("<boolean-type-container> <union-with-boolean-enum-type>true</union-with-boolean-enum-type> </boolean-type-container>");
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
		assertEquals(1,response.getErrors().size());
		assertEquals("/type-validator:type-validation/type-validator:union-type-validation/type-validator:union-type-validation-list[type-validator:id='12']/type-validator:boolean-type-container", response.getErrors().get(0).getErrorPath());
		assertEquals("Only false is supported", response.getErrors().get(0).getErrorMessage());

		requestXml = getUnionTypeEditRequest("<boolean-type-container> <union-with-boolean-enum-type>union</union-with-boolean-enum-type> </boolean-type-container>");
		response = editConfig(m_server, m_clientInfo, requestXml, false);
		assertEquals(1,response.getErrors().size());
		assertEquals("/type-validator:type-validation/type-validator:union-type-validation/type-validator:union-type-validation-list[type-validator:id='12']/type-validator:boolean-type-container", response.getErrors().get(0).getErrorPath());
		assertEquals("Only false is supported", response.getErrors().get(0).getErrorMessage());

		requestXml = getUnionTypeEditRequest("<boolean-type-container> <union-with-boolean-enum-type>union1</union-with-boolean-enum-type> </boolean-type-container>");
		response = editConfig(m_server, m_clientInfo, requestXml, false);
		assertEquals(1,response.getErrors().size());
		assertEquals("/type-validator:type-validation/type-validator:union-type-validation/type-validator:union-type-validation-list[type-validator:id='12']/type-validator:boolean-type-container", response.getErrors().get(0).getErrorPath());
		assertEquals("Only false is supported", response.getErrors().get(0).getErrorMessage());

		requestXml = getUnionTypeEditRequest("<boolean-type-container> <union-with-boolean-enum-type>union11</union-with-boolean-enum-type> </boolean-type-container>");
		response = editConfig(m_server, m_clientInfo, requestXml, false);
		assertEquals(1,response.getErrors().size());
		assertEquals("/type-validator:type-validation/type-validator:union-type-validation/type-validator:union-type-validation-list[type-validator:id='12']/type-validator:boolean-type-container/type-validator:union-with-boolean-enum-type", response.getErrors().get(0).getErrorPath());
		assertEquals("Invalid value. It should be \"true\" or \"false\" instead of \"union11\" or Value \"union11\" is an invalid value. Expected values: [union, union1]", response.getErrors().get(0).getErrorMessage());

		requestXml = getUnionTypeEditRequest("<boolean-type-container> <union-with-boolean-enum-type>yes</union-with-boolean-enum-type> </boolean-type-container>");
		response = editConfig(m_server, m_clientInfo, requestXml, false);
		assertEquals(1,response.getErrors().size());
		assertEquals("/type-validator:type-validation/type-validator:union-type-validation/type-validator:union-type-validation-list[type-validator:id='12']/type-validator:boolean-type-container/type-validator:union-with-boolean-enum-type", response.getErrors().get(0).getErrorPath());
		assertEquals("Invalid value. It should be \"true\" or \"false\" instead of \"yes\" or Value \"yes\" is an invalid value. Expected values: [union, union1]", response.getErrors().get(0).getErrorMessage());
	}
	
	@Test
	public void testUnionTypeWithBooleanEnumValidAndInvalidValues() throws Exception{
		getModelNode();
		// should take default value "true"
		String requestXml = getUnionTypeEditRequest("<enum-default-validation>true</enum-default-validation>");
		editConfig(m_server, m_clientInfo, requestXml, true);
		String responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
				+ "	<type-validator:type-validation xmlns:type-validator=\"urn:org:bbf:pma:validation\">"
				+ "		<type-validator:union-type-validation>" 
				+ "			<type-validator:union-type-validation-list>"
				+ "				<type-validator:id>12</type-validator:id>"
				+ "				<type-validator:union-with-boolean-enum-type>true</type-validator:union-with-boolean-enum-type>"
				+ "				<type-validator:enum-default-validation>true</type-validator:enum-default-validation>"
				+ "				<type-validator:union-with-enum-type-default>union</type-validator:union-with-enum-type-default>"
				+ "			</type-validator:union-type-validation-list>" 
				+ "		</type-validator:union-type-validation>"
				+ "	</type-validator:type-validation>"
				+ " <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>" + "</data>"
				+ "</rpc-reply>";
		verifyGet(responseXml);

		requestXml = getUnionTypeEditRequest("<union-with-boolean-enum-type>false</union-with-boolean-enum-type>");
		editConfig(m_server, m_clientInfo, requestXml, true);
		responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
				+ "	<type-validator:type-validation xmlns:type-validator=\"urn:org:bbf:pma:validation\">"
				+ "		<type-validator:union-type-validation>" 
				+ "			<type-validator:union-type-validation-list>"
				+ "				<type-validator:id>12</type-validator:id>"
				+ "				<type-validator:union-with-boolean-enum-type>false</type-validator:union-with-boolean-enum-type>"
				+ "				<type-validator:enum-default-validation>true</type-validator:enum-default-validation>"
				+ "				<type-validator:union-with-enum-type-default>union</type-validator:union-with-enum-type-default>"
				+ "			</type-validator:union-type-validation-list>" 
				+ "		</type-validator:union-type-validation>"
				+ "	</type-validator:type-validation>"
				+ " <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>" + "</data>"
				+ "</rpc-reply>";
		verifyGet(responseXml);

		requestXml = getUnionTypeEditRequest("<union-with-boolean-enum-type>true</union-with-boolean-enum-type>");
		editConfig(m_server, m_clientInfo, requestXml, true);
		responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
				+ "	<type-validator:type-validation xmlns:type-validator=\"urn:org:bbf:pma:validation\">"
				+ "		<type-validator:union-type-validation>" 
				+ "			<type-validator:union-type-validation-list>"
				+ "				<type-validator:id>12</type-validator:id>"
				+ "				<type-validator:union-with-boolean-enum-type>true</type-validator:union-with-boolean-enum-type>"
				+ "				<type-validator:enum-default-validation>true</type-validator:enum-default-validation>"
				+ "				<type-validator:union-with-enum-type-default>union</type-validator:union-with-enum-type-default>"
				+ "			</type-validator:union-type-validation-list>" 
				+ "		</type-validator:union-type-validation>"
				+ "	</type-validator:type-validation>"
				+ " <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>" + "</data>"
				+ "</rpc-reply>";
		verifyGet(responseXml);

		requestXml = getUnionTypeEditRequest("<enum-default-validation>false</enum-default-validation>");
		editConfig(m_server, m_clientInfo, requestXml, true);
		responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
				+ "	<type-validator:type-validation xmlns:type-validator=\"urn:org:bbf:pma:validation\">"
				+ "		<type-validator:union-type-validation>" 
				+ "			<type-validator:union-type-validation-list>"
				+ "				<type-validator:id>12</type-validator:id>"
				+ "				<type-validator:enum-default-validation>false</type-validator:enum-default-validation>"
				+ "			</type-validator:union-type-validation-list>" 
				+ "		</type-validator:union-type-validation>"
				+ "	</type-validator:type-validation>"
				+ " <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>" + "</data>"
				+ "</rpc-reply>";
		verifyGet(responseXml);

		requestXml = getUnionTypeEditRequest("<union-with-boolean-enum-type>union11</union-with-boolean-enum-type>");
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
		assertEquals(1,response.getErrors().size());
		assertEquals("/type-validator:type-validation/type-validator:union-type-validation/type-validator:union-type-validation-list[type-validator:id='12']/type-validator:union-with-boolean-enum-type", response.getErrors().get(0).getErrorPath());
		assertEquals("Invalid value. It should be \"true\" or \"false\" instead of \"union11\" or Value \"union11\" is an invalid value. Expected values: [union, union1]", response.getErrors().get(0).getErrorMessage());
	}
	
	@Test
	public void testUnionTypeWithOnlyEnum_InvalidEnumValue() throws Exception{
		getModelNode();

		String requestXml = getUnionTypeEditRequest("<union-with-only-enum-type>union2</union-with-only-enum-type>");
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
		assertEquals(1,response.getErrors().size());
		assertEquals("/type-validator:type-validation/type-validator:union-type-validation/type-validator:union-type-validation-list[type-validator:id='12']/type-validator:union-with-only-enum-type", response.getErrors().get(0).getErrorPath());
		assertEquals("Value \"union2\" is an invalid value. Expected values: [union, union1]", response.getErrors().get(0).getErrorMessage());

		requestXml = getUnionTypeEditRequest("<union-with-only-enum-type>10</union-with-only-enum-type>");
		response = editConfig(m_server, m_clientInfo, requestXml, false);
		assertEquals(1,response.getErrors().size());
		assertEquals("/type-validator:type-validation/type-validator:union-type-validation/type-validator:union-type-validation-list[type-validator:id='12']/type-validator:union-with-only-enum-type", response.getErrors().get(0).getErrorPath());
		assertEquals("Value \"10\" is an invalid value. Expected values: [union, union1]", response.getErrors().get(0).getErrorMessage());

	}


	@Test
	public void testUnionTypeWithOnlyEnum_DefaultValue() throws Exception{
		getModelNode();
		String requestXml = getUnionTypeEditRequest("<enum-default-validation>false</enum-default-validation>");
		editConfig(m_server, m_clientInfo, requestXml, true);
		String responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
				+ "	<type-validator:type-validation xmlns:type-validator=\"urn:org:bbf:pma:validation\">"
				+ "		<type-validator:union-type-validation>" 
				+ "			<type-validator:union-type-validation-list>"
				+ "				<type-validator:id>12</type-validator:id>"
				+ "				<type-validator:enum-default-validation>false</type-validator:enum-default-validation>"
				+ "			</type-validator:union-type-validation-list>" 
				+ "		</type-validator:union-type-validation>"
				+ "	</type-validator:type-validation>"
				+ " <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>" + "</data>"
				+ "</rpc-reply>";
		verifyGet(responseXml);

		requestXml = getUnionTypeEditRequest("<enum-default-validation>true</enum-default-validation>");
		editConfig(m_server, m_clientInfo, requestXml, true);

		// Added default enum value, if when condition is true
		responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
				+ "	<type-validator:type-validation xmlns:type-validator=\"urn:org:bbf:pma:validation\">"
				+ "		<type-validator:union-type-validation>" 
				+ "			<type-validator:union-type-validation-list>"
				+ "				<type-validator:id>12</type-validator:id>"
				+"				<type-validator:union-with-enum-type-default>union</type-validator:union-with-enum-type-default>"
				+ "				<type-validator:enum-default-validation>true</type-validator:enum-default-validation>"
				+ "				<type-validator:union-with-boolean-enum-type>true</type-validator:union-with-boolean-enum-type>"
				+ "			</type-validator:union-type-validation-list>" 
				+ "		</type-validator:union-type-validation>"
				+ "	</type-validator:type-validation>"
				+ " <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>" + "</data>"
				+ "</rpc-reply>";
		verifyGet(responseXml);


		// deleted default enum value if when condition is false
		requestXml = getUnionTypeEditRequest("<enum-default-validation>false</enum-default-validation>");
		editConfig(m_server, m_clientInfo, requestXml, true);
		responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
				+ "	<type-validator:type-validation xmlns:type-validator=\"urn:org:bbf:pma:validation\">"
				+ "		<type-validator:union-type-validation>" 
				+ "			<type-validator:union-type-validation-list>"
				+ "				<type-validator:id>12</type-validator:id>"
				+ "				<type-validator:enum-default-validation>false</type-validator:enum-default-validation>"
				+ "			</type-validator:union-type-validation-list>" 
				+ "		</type-validator:union-type-validation>"
				+ "	</type-validator:type-validation>"
				+ " <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>" + "</data>"
				+ "</rpc-reply>";
		verifyGet(responseXml);

		// Negative case, Give invalid enum value (empty value)
		requestXml = getUnionTypeEditRequest("<enum-default-validation>true</enum-default-validation> <union-with-enum-type-default></union-with-enum-type-default>");
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
		assertEquals(1,response.getErrors().size());
		assertEquals("/type-validator:type-validation/type-validator:union-type-validation/type-validator:union-type-validation-list[type-validator:id='12']/type-validator:union-with-enum-type-default", response.getErrors().get(0).getErrorPath());
		assertEquals("Value \"\" is an invalid value. Expected values: [union, union1]", response.getErrors().get(0).getErrorMessage());

	}

	@Test
	public void testUnionTypeWithOnlyEnum_CustomTypeDef() throws Exception{

		getModelNode();
		String requestXml = getUnionTypeEditRequest("<enum-custom-default-validation>false</enum-custom-default-validation>");
		editConfig(m_server, m_clientInfo, requestXml, true);
		String responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
				+ "	<type-validator:type-validation xmlns:type-validator=\"urn:org:bbf:pma:validation\">"
				+ "		<type-validator:union-type-validation>" 
				+ "			<type-validator:union-type-validation-list>"
				+ "				<type-validator:id>12</type-validator:id>"
				+ "				<type-validator:enum-custom-default-validation>false</type-validator:enum-custom-default-validation>"
				+ "			</type-validator:union-type-validation-list>" 
				+ "		</type-validator:union-type-validation>"
				+ "	</type-validator:type-validation>"
				+ " <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>" + "</data>"
				+ "</rpc-reply>";
		verifyGet(responseXml);

		requestXml = getUnionTypeEditRequest("<enum-custom-default-validation>true</enum-custom-default-validation>");
		editConfig(m_server, m_clientInfo, requestXml, true);

		responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
				+ "	<type-validator:type-validation xmlns:type-validator=\"urn:org:bbf:pma:validation\">"
				+ "		<type-validator:union-type-validation>" 
				+ "			<type-validator:union-type-validation-list>"
				+ "				<type-validator:id>12</type-validator:id>"
				+"				<type-validator:union-with-custom-enum-type-default>one</type-validator:union-with-custom-enum-type-default>"
				+ "				<type-validator:enum-custom-default-validation>true</type-validator:enum-custom-default-validation>"
				+ "			</type-validator:union-type-validation-list>" 
				+ "		</type-validator:union-type-validation>"
				+ "	</type-validator:type-validation>"
				+ " <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>" + "</data>"
				+ "</rpc-reply>";
		verifyGet(responseXml);


		requestXml = getUnionTypeEditRequest("<union-with-custom-enum-type-default>two</union-with-custom-enum-type-default>");
		editConfig(m_server, m_clientInfo, requestXml, true);

		responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
				+ "	<type-validator:type-validation xmlns:type-validator=\"urn:org:bbf:pma:validation\">"
				+ "		<type-validator:union-type-validation>" 
				+ "			<type-validator:union-type-validation-list>"
				+ "				<type-validator:id>12</type-validator:id>"
				+"				<type-validator:union-with-custom-enum-type-default>two</type-validator:union-with-custom-enum-type-default>"
				+ "				<type-validator:enum-custom-default-validation>true</type-validator:enum-custom-default-validation>"
				+ "			</type-validator:union-type-validation-list>" 
				+ "		</type-validator:union-type-validation>"
				+ "	</type-validator:type-validation>"
				+ " <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>" + "</data>"
				+ "</rpc-reply>";
		verifyGet(responseXml);

		// Negative case, Give invalid Enum value
		requestXml = getUnionTypeEditRequest("<union-with-custom-enum-type-default>five</union-with-custom-enum-type-default>");
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
		assertEquals(1,response.getErrors().size());
		assertEquals("/type-validator:type-validation/type-validator:union-type-validation/type-validator:union-type-validation-list[type-validator:id='12']/type-validator:union-with-custom-enum-type-default", response.getErrors().get(0).getErrorPath());
		assertEquals("Value \"five\" is an invalid value. Expected values: [one, two, three]", response.getErrors().get(0).getErrorMessage());


		requestXml = getUnionTypeEditRequest("<enum-custom-default-validation>false</enum-custom-default-validation>");
		editConfig(m_server, m_clientInfo, requestXml, true);
		responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
				+ "	<type-validator:type-validation xmlns:type-validator=\"urn:org:bbf:pma:validation\">"
				+ "		<type-validator:union-type-validation>" 
				+ "			<type-validator:union-type-validation-list>"
				+ "				<type-validator:id>12</type-validator:id>"
				+ "				<type-validator:enum-custom-default-validation>false</type-validator:enum-custom-default-validation>"
				+ "			</type-validator:union-type-validation-list>" 
				+ "		</type-validator:union-type-validation>"
				+ "	</type-validator:type-validation>"
				+ " <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>" + "</data>"
				+ "</rpc-reply>";
		verifyGet(responseXml);
	}


	@Test
	public void testUnionTypeWithOnlyString() throws Exception{
		getModelNode();
		String requestXml = getUnionTypeEditRequest("<union-with-only-string-type-leaf>bbf</union-with-only-string-type-leaf>");
		editConfig(m_server, m_clientInfo, requestXml, true);
		String responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
				+ "	<type-validator:type-validation xmlns:type-validator=\"urn:org:bbf:pma:validation\">"
				+ "		<type-validator:union-type-validation>" 
				+ "			<type-validator:union-type-validation-list>"
				+ "				<type-validator:id>12</type-validator:id>"
				+ "				<type-validator:union-with-only-string-type-leaf>bbf</type-validator:union-with-only-string-type-leaf>"
				+ "			</type-validator:union-type-validation-list>" 
				+ "		</type-validator:union-type-validation>"
				+ "	</type-validator:type-validation>"
				+ " <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>" + "</data>"
				+ "</rpc-reply>";
		verifyGet(responseXml);
	}

	@Test
	public void testUnionTypeWithOnlyString_DefaultValue() throws Exception{
		getModelNode();
		String requestXml = getUnionTypeEditRequest("<string-type-default-validation>false</string-type-default-validation>");
		editConfig(m_server, m_clientInfo, requestXml, true);
		String responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
				+ "	<type-validator:type-validation xmlns:type-validator=\"urn:org:bbf:pma:validation\">"
				+ "		<type-validator:union-type-validation>" 
				+ "			<type-validator:union-type-validation-list>"
				+ "				<type-validator:id>12</type-validator:id>"
				+ "				<type-validator:string-type-default-validation>false</type-validator:string-type-default-validation>"
				+ "			</type-validator:union-type-validation-list>" 
				+ "		</type-validator:union-type-validation>"
				+ "	</type-validator:type-validation>"
				+ " <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>" + "</data>"
				+ "</rpc-reply>";
		verifyGet(responseXml);

		requestXml = getUnionTypeEditRequest("<string-type-default-validation>true</string-type-default-validation>");
		editConfig(m_server, m_clientInfo, requestXml, true);

		// Default leaf added if when condition is true
		responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
				+ "	<type-validator:type-validation xmlns:type-validator=\"urn:org:bbf:pma:validation\">"
				+ "		<type-validator:union-type-validation>" 
				+ "			<type-validator:union-type-validation-list>"
				+ "				<type-validator:id>12</type-validator:id>"
				+"				<type-validator:union-with-string-type-default-leaf>string-default</type-validator:union-with-string-type-default-leaf>"
				+ "				<type-validator:string-type-default-validation>true</type-validator:string-type-default-validation>"
				+ "			</type-validator:union-type-validation-list>" 
				+ "		</type-validator:union-type-validation>"
				+ "	</type-validator:type-validation>"
				+ " <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>" + "</data>"
				+ "</rpc-reply>";
		verifyGet(responseXml);

		//Default leaf deleted if when condition is false
		requestXml = getUnionTypeEditRequest("<string-type-default-validation>false</string-type-default-validation>");
		editConfig(m_server, m_clientInfo, requestXml, true);
		responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
				+ "	<type-validator:type-validation xmlns:type-validator=\"urn:org:bbf:pma:validation\">"
				+ "		<type-validator:union-type-validation>" 
				+ "			<type-validator:union-type-validation-list>"
				+ "				<type-validator:id>12</type-validator:id>"
				+ "				<type-validator:string-type-default-validation>false</type-validator:string-type-default-validation>"
				+ "			</type-validator:union-type-validation-list>" 
				+ "		</type-validator:union-type-validation>"
				+ "	</type-validator:type-validation>"
				+ " <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>" + "</data>"
				+ "</rpc-reply>";
		verifyGet(responseXml);
	}

	@Test
	public void testUnionTypeOnlyInt() throws Exception{
		getModelNode();
		String requestXml = getUnionTypeEditRequest("<union-with-only-int-type-leaf>15</union-with-only-int-type-leaf>");
		editConfig(m_server, m_clientInfo, requestXml, true);
		String responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
				+ "	<type-validator:type-validation xmlns:type-validator=\"urn:org:bbf:pma:validation\">"
				+ "		<type-validator:union-type-validation>" 
				+ "			<type-validator:union-type-validation-list>"
				+ "				<type-validator:id>12</type-validator:id>"
				+ "				<type-validator:union-with-only-int-type-leaf>15</type-validator:union-with-only-int-type-leaf>"
				+ "			</type-validator:union-type-validation-list>" 
				+ "		</type-validator:union-type-validation>"
				+ "	</type-validator:type-validation>"
				+ " <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>" + "</data>"
				+ "</rpc-reply>";
		verifyGet(responseXml);
	}

	@Test
	public void testUnionTypeOnlyInt_InvalidValue() throws Exception{
		getModelNode();
		// Integer value exceed the range
		String requestXml = getUnionTypeEditRequest("<union-with-only-int-type-leaf>250</union-with-only-int-type-leaf>");
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
		assertEquals(1,response.getErrors().size());
		assertEquals("/type-validator:type-validation/type-validator:union-type-validation/type-validator:union-type-validation-list[type-validator:id='12']/type-validator:union-with-only-int-type-leaf", response.getErrors().get(0).getErrorPath());
		assertEquals("The argument is out of bounds <-128, 127>", response.getErrors().get(0).getErrorMessage());

		// Invalid integer value - abcd
		requestXml = getUnionTypeEditRequest("<union-with-only-int-type-leaf>abcd</union-with-only-int-type-leaf>");
		response = editConfig(m_server, m_clientInfo, requestXml, false);
		assertEquals(1,response.getErrors().size());
		assertEquals("/type-validator:type-validation/type-validator:union-type-validation/type-validator:union-type-validation-list[type-validator:id='12']/type-validator:union-with-only-int-type-leaf", response.getErrors().get(0).getErrorPath());
		assertEquals("The argument is out of bounds <-128, 127>", response.getErrors().get(0).getErrorMessage());

	}


	@Test
	public void testUnionTypeOnlyLeafref() throws Exception{
		getModelNode();
		// invalid leaf-ref value
		String requestXml = getUnionTypeEditRequest("<ref-value>10</ref-value><union-with-leafref-type>15</union-with-leafref-type>");
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
		assertEquals(1,response.getErrors().size());
		assertEquals("/type-validator:type-validation/type-validator:union-type-validation/type-validator:union-type-validation-list[type-validator:id='12']/type-validator:union-with-leafref-type", response.getErrors().get(0).getErrorPath());
		assertEquals("Dependency violated, '15' must exist", response.getErrors().get(0).getErrorMessage());

		requestXml = getUnionTypeEditRequest("<ref-value>15</ref-value><union-with-leafref-type>15</union-with-leafref-type>");
		editConfig(m_server, m_clientInfo, requestXml, true);
		String responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
				+ "	<type-validator:type-validation xmlns:type-validator=\"urn:org:bbf:pma:validation\">"
				+ "		<type-validator:union-type-validation>" 
				+ "			<type-validator:union-type-validation-list>"
				+ "				<type-validator:id>12</type-validator:id>"
				+ "				<type-validator:union-with-leafref-type>15</type-validator:union-with-leafref-type>"
				+ "				<type-validator:ref-value>15</type-validator:ref-value>"
				+ "			</type-validator:union-type-validation-list>" 
				+ "		</type-validator:union-type-validation>"
				+ "	</type-validator:type-validation>"
				+ " <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>" + "</data>"
				+ "</rpc-reply>";
		verifyGet(responseXml);

		// delete the leaf-ref value
		requestXml = getUnionTypeEditRequest("<ref-value xc:operation=\"delete\">15</ref-value>");
		response = editConfig(m_server, m_clientInfo, requestXml, false);
		assertEquals(1,response.getErrors().size());
		assertEquals("/type-validator:type-validation/type-validator:union-type-validation/type-validator:union-type-validation-list[type-validator:id='12']/type-validator:union-with-leafref-type", response.getErrors().get(0).getErrorPath());
		assertEquals("Dependency violated, '15' must exist", response.getErrors().get(0).getErrorMessage());

	}

	@Test
	public void testUnionTypeWithOnlyLeafref_RemoveLeafref() throws Exception{
		getModelNode();
		String requestXml = getUnionTypeEditRequest("<ref-value>15</ref-value><union-with-leafref-type>15</union-with-leafref-type>");
		editConfig(m_server, m_clientInfo, requestXml, true);
		String responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
				+ "	<type-validator:type-validation xmlns:type-validator=\"urn:org:bbf:pma:validation\">"
				+ "		<type-validator:union-type-validation>" 
				+ "			<type-validator:union-type-validation-list>"
				+ "				<type-validator:id>12</type-validator:id>"
				+ "				<type-validator:union-with-leafref-type>15</type-validator:union-with-leafref-type>"
				+ "				<type-validator:ref-value>15</type-validator:ref-value>"
				+ "			</type-validator:union-type-validation-list>" 
				+ "		</type-validator:union-type-validation>"
				+ "	</type-validator:type-validation>"
				+ " <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>" + "</data>"
				+ "</rpc-reply>";
		verifyGet(responseXml);

		requestXml = getUnionTypeEditRequest("<union-with-leafref-type xc:operation=\"delete\">15</union-with-leafref-type>");
		editConfig(m_server, m_clientInfo, requestXml, true);

		responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
				+ "	<type-validator:type-validation xmlns:type-validator=\"urn:org:bbf:pma:validation\">"
				+ "		<type-validator:union-type-validation>" 
				+ "			<type-validator:union-type-validation-list>"
				+ "				<type-validator:id>12</type-validator:id>"
				+ "				<type-validator:ref-value>15</type-validator:ref-value>"
				+ "			</type-validator:union-type-validation-list>" 
				+ "		</type-validator:union-type-validation>"
				+ "	</type-validator:type-validation>"
				+ " <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>" + "</data>"
				+ "</rpc-reply>";
		verifyGet(responseXml);
	}

	/**
	 * 
	 * 
	 * 
	 * @throws Exception
	 */

	@Test
	public void testUnionWithAllType_LeafrefCase() throws Exception{

		/**
		 *   leaf union-leaf-without-string-type {
		 *           type union {
		 *              type int8 {
		 *                 range "10..25";
		 *            	}
		 *           	type enumeration {
		 *              	enum "union";
		 *         		}
		 *        		type leafref {
		 *           		path "../ref-value";
		 *      		}
		 * 			}
		 *		}
		 */
		getModelNode();

		// create leaf with int type
		String requestXml = getUnionTypeEditRequest("<union-leaf-without-string-type>15</union-leaf-without-string-type>");
		editConfig(m_server, m_clientInfo, requestXml, true);
		String responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
				+ "	<type-validator:type-validation xmlns:type-validator=\"urn:org:bbf:pma:validation\">"
				+ "		<type-validator:union-type-validation>" 
				+ "			<type-validator:union-type-validation-list>"
				+ "				<type-validator:id>12</type-validator:id>"
				+ "				<type-validator:union-leaf-without-string-type>15</type-validator:union-leaf-without-string-type>"
				+ "			</type-validator:union-type-validation-list>" 
				+ "		</type-validator:union-type-validation>"
				+ "	</type-validator:type-validation>"
				+ " <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>" + "</data>"
				+ "</rpc-reply>";
		verifyGet(responseXml);

		requestXml = getUnionTypeEditRequest("<ref-value>10</ref-value>");
		editConfig(m_server, m_clientInfo, requestXml, true);
		responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
				+ "	<type-validator:type-validation xmlns:type-validator=\"urn:org:bbf:pma:validation\">"
				+ "		<type-validator:union-type-validation>" 
				+ "			<type-validator:union-type-validation-list>"
				+ "				<type-validator:id>12</type-validator:id>"
				+ "				<type-validator:union-leaf-without-string-type>15</type-validator:union-leaf-without-string-type>"
				+ "				<type-validator:ref-value>10</type-validator:ref-value>"
				+ "			</type-validator:union-type-validation-list>" 
				+ "		</type-validator:union-type-validation>"
				+ "	</type-validator:type-validation>"
				+ " <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>" + "</data>"
				+ "</rpc-reply>";
		verifyGet(responseXml);

		requestXml = getUnionTypeEditRequest("<ref-value xc:operation=\"delete\">10</ref-value>");
		editConfig(m_server, m_clientInfo, requestXml, true);

		responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
				+ "	<type-validator:type-validation xmlns:type-validator=\"urn:org:bbf:pma:validation\">"
				+ "		<type-validator:union-type-validation>" 
				+ "			<type-validator:union-type-validation-list>"
				+ "				<type-validator:id>12</type-validator:id>"
				+ "				<type-validator:union-leaf-without-string-type>15</type-validator:union-leaf-without-string-type>"
				+ "			</type-validator:union-type-validation-list>" 
				+ "		</type-validator:union-type-validation>"
				+ "	</type-validator:type-validation>"
				+ " <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>" + "</data>"
				+ "</rpc-reply>";
		verifyGet(responseXml);

		// modify leaf with leaf-ref type
		requestXml = getUnionTypeEditRequest("<union-leaf-without-string-type>30</union-leaf-without-string-type><ref-value>30</ref-value>");
		editConfig(m_server, m_clientInfo, requestXml, true);
		
		responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
				+ "	<type-validator:type-validation xmlns:type-validator=\"urn:org:bbf:pma:validation\">"
				+ "		<type-validator:union-type-validation>" 
				+ "			<type-validator:union-type-validation-list>"
				+ "				<type-validator:id>12</type-validator:id>"
				+ "				<type-validator:union-leaf-without-string-type>30</type-validator:union-leaf-without-string-type>"
				+ "				<type-validator:ref-value>30</type-validator:ref-value>"
				+ "			</type-validator:union-type-validation-list>" 
				+ "		</type-validator:union-type-validation>"
				+ "	</type-validator:type-validation>"
				+ " <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>" + "</data>"
				+ "</rpc-reply>";
		verifyGet(responseXml);

		// Thrown an error is trying to delete the reference leaf alone
		requestXml = getUnionTypeEditRequest("<ref-value xc:operation=\"delete\">30</ref-value>");
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
		assertEquals(1,response.getErrors().size());
		assertEquals("/type-validator:type-validation/type-validator:union-type-validation/type-validator:union-type-validation-list[type-validator:id='12']/type-validator:union-leaf-without-string-type", response.getErrors().get(0).getErrorPath());
		assertEquals("The argument is out of bounds <10, 25> or Value \"30\" is an invalid value. Expected values: [union] or Dependency violated, '30' must exist", response.getErrors().get(0).getErrorMessage());
	}

	@Test
	public void testUnionWithAllType_InvalidLeafref() throws Exception{
		getModelNode();
		String requestXml = getUnionTypeEditRequest("<union-leaf-without-string-type1>15</union-leaf-without-string-type1>");
		editConfig(m_server, m_clientInfo, requestXml, true);
		String responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
				+ "	<type-validator:type-validation xmlns:type-validator=\"urn:org:bbf:pma:validation\">"
				+ "		<type-validator:union-type-validation>" 
				+ "			<type-validator:union-type-validation-list>"
				+ "				<type-validator:id>12</type-validator:id>"
				+ "				<type-validator:union-leaf-without-string-type1>15</type-validator:union-leaf-without-string-type1>"
				+ "			</type-validator:union-type-validation-list>" 
				+ "		</type-validator:union-type-validation>"
				+ "	</type-validator:type-validation>"
				+ " <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>" + "</data>"
				+ "</rpc-reply>";
		verifyGet(responseXml);

		requestXml = getUnionTypeEditRequest("<ref-value>10</ref-value>");
		editConfig(m_server, m_clientInfo, requestXml, true);
		responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
				+ "	<type-validator:type-validation xmlns:type-validator=\"urn:org:bbf:pma:validation\">"
				+ "		<type-validator:union-type-validation>" 
				+ "			<type-validator:union-type-validation-list>"
				+ "				<type-validator:id>12</type-validator:id>"
				+ "				<type-validator:union-leaf-without-string-type1>15</type-validator:union-leaf-without-string-type1>"
				+ "				<type-validator:ref-value>10</type-validator:ref-value>"
				+ "			</type-validator:union-type-validation-list>" 
				+ "		</type-validator:union-type-validation>"
				+ "	</type-validator:type-validation>"
				+ " <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>" + "</data>"
				+ "</rpc-reply>";
		verifyGet(responseXml);


		requestXml = getUnionTypeEditRequest("<ref-value xc:operation=\"delete\">10</ref-value>");
		editConfig(m_server, m_clientInfo, requestXml, true);

		responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
				+ "	<type-validator:type-validation xmlns:type-validator=\"urn:org:bbf:pma:validation\">"
				+ "		<type-validator:union-type-validation>" 
				+ "			<type-validator:union-type-validation-list>"
				+ "				<type-validator:id>12</type-validator:id>"
				+ "				<type-validator:union-leaf-without-string-type1>15</type-validator:union-leaf-without-string-type1>"
				+ "			</type-validator:union-type-validation-list>" 
				+ "		</type-validator:union-type-validation>"
				+ "	</type-validator:type-validation>"
				+ " <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>" + "</data>"
				+ "</rpc-reply>";
		verifyGet(responseXml);

		requestXml = getUnionTypeEditRequest("<union-leaf-without-string-type1>30</union-leaf-without-string-type1><ref-value>30</ref-value>");
		editConfig(m_server, m_clientInfo, requestXml, true);

		responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
				+ "	<type-validator:type-validation xmlns:type-validator=\"urn:org:bbf:pma:validation\">"
				+ "		<type-validator:union-type-validation>" 
				+ "			<type-validator:union-type-validation-list>"
				+ "				<type-validator:id>12</type-validator:id>"
				+ "				<type-validator:union-leaf-without-string-type1>30</type-validator:union-leaf-without-string-type1>"
				+ "				<type-validator:ref-value>30</type-validator:ref-value>"
				+ "			</type-validator:union-type-validation-list>" 
				+ "		</type-validator:union-type-validation>"
				+ "	</type-validator:type-validation>"
				+ " <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>" + "</data>"
				+ "</rpc-reply>";
		verifyGet(responseXml);
		
		requestXml = getUnionTypeEditRequest("<ref-value xc:operation=\"delete\">30</ref-value>");
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
		assertEquals(1,response.getErrors().size());
		assertEquals("/type-validator:type-validation/type-validator:union-type-validation/type-validator:union-type-validation-list[type-validator:id='12']/type-validator:union-leaf-without-string-type1", response.getErrors().get(0).getErrorPath());
		assertEquals("Dependency violated, '30' must exist or The argument is out of bounds <10, 25> or Value \"30\" is an invalid value. Expected values: [union]", response.getErrors().get(0).getErrorMessage());
	}

	@Test
	public void testUnionWithAllType_RemoveLeafref() throws Exception{
		getModelNode();
		String requestXml = getUnionTypeEditRequest("<union-leaf-without-string-type1>15</union-leaf-without-string-type1><ref-value>15</ref-value>");
		editConfig(m_server, m_clientInfo, requestXml, true);
		String responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
				+ "	<type-validator:type-validation xmlns:type-validator=\"urn:org:bbf:pma:validation\">"
				+ "		<type-validator:union-type-validation>" 
				+ "			<type-validator:union-type-validation-list>"
				+ "				<type-validator:id>12</type-validator:id>"
				+ "				<type-validator:union-leaf-without-string-type1>15</type-validator:union-leaf-without-string-type1>"
				+ "				<type-validator:ref-value>15</type-validator:ref-value>"
				+ "			</type-validator:union-type-validation-list>" 
				+ "		</type-validator:union-type-validation>"
				+ "	</type-validator:type-validation>"
				+ " <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>" + "</data>"
				+ "</rpc-reply>";
		verifyGet(responseXml);

		//Here remove the leaf-ref value, but actual value matched with other union type.
		requestXml = getUnionTypeEditRequest("<ref-value xc:operation=\"delete\">15</ref-value>");
		editConfig(m_server, m_clientInfo, requestXml, true);


		responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
				+ "	<type-validator:type-validation xmlns:type-validator=\"urn:org:bbf:pma:validation\">"
				+ "		<type-validator:union-type-validation>" 
				+ "			<type-validator:union-type-validation-list>"
				+ "				<type-validator:id>12</type-validator:id>"
				+ "				<type-validator:union-leaf-without-string-type1>15</type-validator:union-leaf-without-string-type1>"
				+ "			</type-validator:union-type-validation-list>" 
				+ "		</type-validator:union-type-validation>"
				+ "	</type-validator:type-validation>"
				+ " <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>" + "</data>"
				+ "</rpc-reply>";
		verifyGet(responseXml);
	}


	@Test
	public void testUnionWithAllTypeExceptInt() throws Exception{
		getModelNode();

		// Verify Leafref case with string type
		String requestXml = getUnionTypeEditRequest("<ref-string-value>bbf</ref-string-value>");
		editConfig(m_server, m_clientInfo, requestXml, true);
		String responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
				+ "	<type-validator:type-validation xmlns:type-validator=\"urn:org:bbf:pma:validation\">"
				+ "		<type-validator:union-type-validation>" 
				+ "			<type-validator:union-type-validation-list>"
				+ "				<type-validator:id>12</type-validator:id>"
				+ "				<type-validator:ref-string-value>bbf</type-validator:ref-string-value>"
				+ "			</type-validator:union-type-validation-list>" 
				+ "		</type-validator:union-type-validation>"
				+ "	</type-validator:type-validation>"
				+ " <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>" + "</data>"
				+ "</rpc-reply>";
		verifyGet(responseXml);

		requestXml = getUnionTypeEditRequest("<union-without-int-type>union</union-without-int-type>");
		editConfig(m_server, m_clientInfo, requestXml, true);
		responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
				+ "	<type-validator:type-validation xmlns:type-validator=\"urn:org:bbf:pma:validation\">"
				+ "		<type-validator:union-type-validation>" 
				+ "			<type-validator:union-type-validation-list>"
				+ "				<type-validator:id>12</type-validator:id>"
				+ "				<type-validator:union-without-int-type>union</type-validator:union-without-int-type>"
				+ "				<type-validator:ref-string-value>bbf</type-validator:ref-string-value>"
				+ "			</type-validator:union-type-validation-list>" 
				+ "		</type-validator:union-type-validation>"
				+ "	</type-validator:type-validation>"
				+ " <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>" + "</data>"
				+ "</rpc-reply>";
		verifyGet(responseXml);

		requestXml = getUnionTypeEditRequest("<union-without-int-type>bbf</union-without-int-type>");
		editConfig(m_server, m_clientInfo, requestXml, true);
		responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
				+ "	<type-validator:type-validation xmlns:type-validator=\"urn:org:bbf:pma:validation\">"
				+ "		<type-validator:union-type-validation>" 
				+ "			<type-validator:union-type-validation-list>"
				+ "				<type-validator:id>12</type-validator:id>"
				+ "				<type-validator:union-without-int-type>bbf</type-validator:union-without-int-type>"
				+ "				<type-validator:ref-string-value>bbf</type-validator:ref-string-value>"
				+ "			</type-validator:union-type-validation-list>" 
				+ "		</type-validator:union-type-validation>"
				+ "	</type-validator:type-validation>"
				+ " <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>" + "</data>"
				+ "</rpc-reply>";
		verifyGet(responseXml);


		requestXml = getUnionTypeEditRequest("<ref-string-value xc:operation=\"delete\">bbf</ref-string-value>");
		editConfig(m_server, m_clientInfo, requestXml, true);

		responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
				+ "	<type-validator:type-validation xmlns:type-validator=\"urn:org:bbf:pma:validation\">"
				+ "		<type-validator:union-type-validation>" 
				+ "			<type-validator:union-type-validation-list>"
				+ "				<type-validator:id>12</type-validator:id>"
				+ "				<type-validator:union-without-int-type>bbf</type-validator:union-without-int-type>"
				+ "			</type-validator:union-type-validation-list>" 
				+ "		</type-validator:union-type-validation>"
				+ "	</type-validator:type-validation>"
				+ " <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>" + "</data>"
				+ "</rpc-reply>";
		verifyGet(responseXml);

	}


	@Test
	public void testUnionWithAllType_DefaultValue() throws Exception{
		getModelNode();
		String requestXml = getUnionTypeEditRequest("<all-type-default-validation>true</all-type-default-validation>");
		editConfig(m_server, m_clientInfo, requestXml, true);
		String responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
				+ "	<type-validator:type-validation xmlns:type-validator=\"urn:org:bbf:pma:validation\">"
				+ "		<type-validator:union-type-validation>" 
				+ "			<type-validator:union-type-validation-list>"
				+ "				<type-validator:id>12</type-validator:id>"
				+ "				<type-validator:union-all-type-default>xyz</type-validator:union-all-type-default>"
				+ "				<type-validator:all-type-default-validation>true</type-validator:all-type-default-validation>"
				+ "			</type-validator:union-type-validation-list>" 
				+ "		</type-validator:union-type-validation>"
				+ "	</type-validator:type-validation>"
				+ " <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>" + "</data>"
				+ "</rpc-reply>";
		verifyGet(responseXml);

		requestXml = getUnionTypeEditRequest("<union-all-type-default>default</union-all-type-default>");
		editConfig(m_server, m_clientInfo, requestXml, true);
		responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
				+ "	<type-validator:type-validation xmlns:type-validator=\"urn:org:bbf:pma:validation\">"
				+ "		<type-validator:union-type-validation>" 
				+ "			<type-validator:union-type-validation-list>"
				+ "				<type-validator:id>12</type-validator:id>"
				+ "				<type-validator:union-all-type-default>default</type-validator:union-all-type-default>"
				+ "				<type-validator:all-type-default-validation>true</type-validator:all-type-default-validation>"
				+ "			</type-validator:union-type-validation-list>" 
				+ "		</type-validator:union-type-validation>"
				+ "	</type-validator:type-validation>"
				+ " <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>" + "</data>"
				+ "</rpc-reply>";
		verifyGet(responseXml);
	}

	@Test
	public void testNestedUnionWithAlltype() throws Exception{

		getModelNode();
		String requestXml = getUnionTypeEditRequest("<nested-union-type>1020</nested-union-type>");
		editConfig(m_server, m_clientInfo, requestXml, true);
		String responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
				+ "	<type-validator:type-validation xmlns:type-validator=\"urn:org:bbf:pma:validation\">"
				+ "		<type-validator:union-type-validation>" 
				+ "			<type-validator:union-type-validation-list>"
				+ "				<type-validator:id>12</type-validator:id>"
				+ "				<type-validator:nested-union-type>1020</type-validator:nested-union-type>"
				+ "			</type-validator:union-type-validation-list>" 
				+ "		</type-validator:union-type-validation>"
				+ "	</type-validator:type-validation>"
				+ " <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>" + "</data>"
				+ "</rpc-reply>";
		verifyGet(responseXml);

		requestXml = getUnionTypeEditRequest("<nested-union-type>12</nested-union-type>");
		editConfig(m_server, m_clientInfo, requestXml, true);
		responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
				+ "	<type-validator:type-validation xmlns:type-validator=\"urn:org:bbf:pma:validation\">"
				+ "		<type-validator:union-type-validation>" 
				+ "			<type-validator:union-type-validation-list>"
				+ "				<type-validator:id>12</type-validator:id>"
				+ "				<type-validator:nested-union-type>12</type-validator:nested-union-type>"
				+ "			</type-validator:union-type-validation-list>" 
				+ "		</type-validator:union-type-validation>"
				+ "	</type-validator:type-validation>"
				+ " <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>" + "</data>"
				+ "</rpc-reply>";
		verifyGet(responseXml);

		// Negative case
		requestXml = getUnionTypeEditRequest("<nested-union-type>1620</nested-union-type>");
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
		assertEquals(1,response.getErrors().size());
		assertEquals("/type-validator:type-validation/type-validator:union-type-validation/type-validator:union-type-validation-list[type-validator:id='12']/type-validator:nested-union-type", response.getErrors().get(0).getErrorPath());
		assertEquals("The argument is out of bounds <10, 25> or Dependency violated, \'1620\' must exist or Value \"1620\" is an invalid value. Expected values: [one, two, three] or The argument is out of bounds <1000, 1025> or Dependency violated, \'1620\' must exist or Dependency violated, \'1620\' must exist", response.getErrors().get(0).getErrorMessage());
	}

	@Test
	public void testNestedUnionWithMultipleLeafrefType() throws Exception{

		getModelNode();
		String requestXml = getUnionTypeEditRequest("<nested-union-type>bbf</nested-union-type><ref-string-value>bbf</ref-string-value><leafref-container><leafref-string>bbf</leafref-string></leafref-container>");
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, true);
		String responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
				+ "	<type-validator:type-validation xmlns:type-validator=\"urn:org:bbf:pma:validation\">"
				+ "		<type-validator:union-type-validation>" 
				+ "			<type-validator:union-type-validation-list>"
				+ "				<type-validator:id>12</type-validator:id>"
				+ "				<type-validator:nested-union-type>bbf</type-validator:nested-union-type>"
				+ "				<type-validator:ref-string-value>bbf</type-validator:ref-string-value>"
				+ "				<type-validator:leafref-container>"
				+ "					<type-validator:leafref-string>bbf</type-validator:leafref-string>"
				+ "				</type-validator:leafref-container>	"	
				+ "			</type-validator:union-type-validation-list>" 
				+ "		</type-validator:union-type-validation>"
				+ "	</type-validator:type-validation>"
				+ " <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>" + "</data>"
				+ "</rpc-reply>";
		verifyGet(responseXml);


		requestXml = getUnionTypeEditRequest("<ref-string-value xc:operation=\"delete\">bbf</ref-string-value>");
		editConfig(m_server, m_clientInfo, requestXml, true);

		responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
				+ "	<type-validator:type-validation xmlns:type-validator=\"urn:org:bbf:pma:validation\">"
				+ "		<type-validator:union-type-validation>" 
				+ "			<type-validator:union-type-validation-list>"
				+ "				<type-validator:id>12</type-validator:id>"
				+ "				<type-validator:nested-union-type>bbf</type-validator:nested-union-type>"
				+ "				<type-validator:leafref-container>"
				+ "					<type-validator:leafref-string>bbf</type-validator:leafref-string>"
				+ "				</type-validator:leafref-container>	"	
				+ "			</type-validator:union-type-validation-list>" 
				+ "		</type-validator:union-type-validation>"
				+ "	</type-validator:type-validation>"
				+ " <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>" + "</data>"
				+ "</rpc-reply>";
		verifyGet(responseXml);

		// Negative case
		requestXml = getUnionTypeEditRequest("<leafref-container><leafref-string xc:operation=\"delete\">bbf</leafref-string></leafref-container>");
		response = editConfig(m_server, m_clientInfo, requestXml, false);
		assertEquals(1,response.getErrors().size());
		assertEquals("/type-validator:type-validation/type-validator:union-type-validation/type-validator:union-type-validation-list[type-validator:id='12']/type-validator:nested-union-type", response.getErrors().get(0).getErrorPath());
		assertEquals("The argument is out of bounds <10, 25> or Dependency violated, \'bbf\' must exist or Value \"bbf\" is an invalid value. Expected values: [one, two, three] or The argument is out of bounds <1000, 1025> or Dependency violated, \'bbf\' must exist or Dependency violated, \'bbf\' must exist", response.getErrors().get(0).getErrorMessage());
	}


	@Test
	public void testUnionWithAlltype_CustomTypeDef_DefualtCase() throws Exception{

		getModelNode();
		String requestXml = getUnionTypeEditRequest("<default-validation>true</default-validation>");
		editConfig(m_server, m_clientInfo, requestXml, true);
		String responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
				+ "	<type-validator:type-validation xmlns:type-validator=\"urn:org:bbf:pma:validation\">"
				+ "		<type-validator:union-type-validation>" 
				+ "			<type-validator:union-type-validation-list>"
				+ "				<type-validator:id>12</type-validator:id>"
				+ "				<type-validator:custom-union-type-default>10</type-validator:custom-union-type-default>"
				+ "				<type-validator:nested-custom-union-type-default>10</type-validator:nested-custom-union-type-default>"
				+ "				<type-validator:default-validation>true</type-validator:default-validation>"
				+ "			</type-validator:union-type-validation-list>" 
				+ "		</type-validator:union-type-validation>"
				+ "	</type-validator:type-validation>"
				+ " <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>" + "</data>"
				+ "</rpc-reply>";
		verifyGet(responseXml);

	}

	@Test
	public void testUnionTypeOnlyInstanceIdentifier_InvalidValue() throws Exception{

		getModelNode();
		String requestXml = getUnionTypeEditRequest("<union-with-only-instance-identifier>/type-validation/union-type-validation/union-type-validation-list[id = '12']/leafref-container/instance-leaf</union-with-only-instance-identifier>");
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
		assertEquals(1,response.getErrors().size());
		assertEquals("/type-validator:type-validation/type-validator:union-type-validation/type-validator:union-type-validation-list[type-validator:id='12']/type-validator:union-with-only-instance-identifier", response.getErrors().get(0).getErrorPath());
		assertEquals("Missing required element /type-validation/union-type-validation/union-type-validation-list[id = '12']/leafref-container/instance-leaf", response.getErrors().get(0).getErrorMessage());

	}

	@Test
	public void testUnionTypeOnlyInstanceIdentifier() throws Exception{

		getModelNode();
		String requestXml = getUnionTypeEditRequest("<union-with-only-instance-identifier>/type-validation/union-type-validation/union-type-validation-list[id = '12']</union-with-only-instance-identifier>");
		editConfig(m_server, m_clientInfo, requestXml, true);

		String responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
				+ "	<type-validator:type-validation xmlns:type-validator=\"urn:org:bbf:pma:validation\">"
				+ "		<type-validator:union-type-validation>" 
				+ "			<type-validator:union-type-validation-list>"
				+ "				<type-validator:id>12</type-validator:id>"
				+ "				<type-validator:union-with-only-instance-identifier>/type-validation/union-type-validation/union-type-validation-list[id = '12']</type-validator:union-with-only-instance-identifier>"
				+ "			</type-validator:union-type-validation-list>" 
				+ "		</type-validator:union-type-validation>"
				+ "	</type-validator:type-validation>"
				+ " <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>" + "</data>"
				+ "</rpc-reply>";
		verifyGet(responseXml);
	}

	@Test
	public void testUnionAllType_ValidInstanceIdentifier() throws Exception {
		getModelNode();
		String requestXml = getUnionTypeEditRequest("<leafref-container><instance-leaf>bbf</instance-leaf></leafref-container><union-with-instance-identifier>/type-validation/union-type-validation/union-type-validation-list[id = '12']/leafref-container/instance-leaf</union-with-instance-identifier>");
		editConfig(m_server, m_clientInfo, requestXml, true);
		String responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
				+ "	<type-validator:type-validation xmlns:type-validator=\"urn:org:bbf:pma:validation\">"
				+ "		<type-validator:union-type-validation>" 
				+ "			<type-validator:union-type-validation-list>"
				+ "				<type-validator:id>12</type-validator:id>"
				+ "				<type-validator:leafref-container>"
				+ "					<type-validator:instance-leaf>bbf</type-validator:instance-leaf>"
				+ "				</type-validator:leafref-container>	"	
				+ "				<type-validator:union-with-instance-identifier>/type-validation/union-type-validation/union-type-validation-list[id = '12']/leafref-container/instance-leaf</type-validator:union-with-instance-identifier>"
				+ "			</type-validator:union-type-validation-list>" 
				+ "		</type-validator:union-type-validation>"
				+ "	</type-validator:type-validation>"
				+ " <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>" + "</data>"
				+ "</rpc-reply>";
		verifyGet(responseXml);
	}

	@Test
	public void testUnionWithAllType_InvalidTypes() throws Exception{

		getModelNode();

		// Invalid instance-identifier value
		String requestXml = getUnionTypeEditRequest("<union-with-instance-identifier>/type-validation/union-type-validation/union-type-validation-list[id = '12']/leafref-container/instance-leaf</union-with-instance-identifier>");
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
		assertEquals(1,response.getErrors().size());
		assertEquals("/type-validator:type-validation/type-validator:union-type-validation/type-validator:union-type-validation-list[type-validator:id='12']/type-validator:union-with-instance-identifier", response.getErrors().get(0).getErrorPath());
		assertEquals("The argument is out of bounds <-128, 127> or Missing required element /type-validation/union-type-validation/union-type-validation-list[id = '12']/leafref-container/instance-leaf or Value \"/type-validation/union-type-validation/union-type-validation-list[id = '12']/leafref-container/instance-leaf\" is an invalid value. Expected values: [union, union1]", response.getErrors().get(0).getErrorMessage());

		// out of range  of integer value
		requestXml = getUnionTypeEditRequest("<union-with-instance-identifier>250</union-with-instance-identifier>");
		response = editConfig(m_server, m_clientInfo, requestXml, false);
		assertEquals(1,response.getErrors().size());
		assertEquals("/type-validator:type-validation/type-validator:union-type-validation/type-validator:union-type-validation-list[type-validator:id='12']/type-validator:union-with-instance-identifier", response.getErrors().get(0).getErrorPath());
		assertEquals("The argument is out of bounds <-128, 127> or Missing required element 250 or Value \"250\" is an invalid value. Expected values: [union, union1]", response.getErrors().get(0).getErrorMessage());

		// Invalid Enum type
		requestXml = getUnionTypeEditRequest("<union-with-instance-identifier>abc</union-with-instance-identifier>");
		response = editConfig(m_server, m_clientInfo, requestXml, false);
		assertEquals(1,response.getErrors().size());
		assertEquals("/type-validator:type-validation/type-validator:union-type-validation/type-validator:union-type-validation-list[type-validator:id='12']/type-validator:union-with-instance-identifier", response.getErrors().get(0).getErrorPath());
		assertEquals("The argument is out of bounds <-128, 127> or Missing required element abc or Value \"abc\" is an invalid value. Expected values: [union, union1]", response.getErrors().get(0).getErrorMessage());
	}
	
	@Test
    public void testRemoveUnionLeaf() throws Exception {
        getModelNode();
        String requestXml = "<type-validation xmlns=\"urn:org:bbf:pma:validation\">"
                + "<unionWithEnumAndInt>"
                +    "<id>1</id>"
                +    "<max-address>4</max-address>"
                + "</unionWithEnumAndInt>"
                + "</type-validation>";
        editConfig(m_server, m_clientInfo, requestXml, true);
        
        requestXml = "<type-validation xmlns=\"urn:org:bbf:pma:validation\">"
                + "<unionWithEnumAndInt xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                +    "<id>1</id>"
                +    "<max-address xc:operation=\"remove\" />"
                + "</unionWithEnumAndInt>"
                + "</type-validation>";
        editConfig(m_server, m_clientInfo, requestXml, true);
        
        String expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\"><data>"
                + "<type-validator:type-validation xmlns:type-validator=\"urn:org:bbf:pma:validation\">"
                + "<type-validator:unionWithEnumAndInt>"
                + "<type-validator:id>1</type-validator:id>"
                + "<max-address xmlns=\"urn:org:bbf:pma:validation\">no-limit</max-address>"
                + "</type-validator:unionWithEnumAndInt>"
                + "</type-validator:type-validation>"
                +"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";
        
        verifyGet(expResponse);
        
        requestXml = "<type-validation xmlns=\"urn:org:bbf:pma:validation\">"
                + "<unionWithEnumAndInt>"
                +    "<id>2</id>"
                +    "<max-address/>"
                + "</unionWithEnumAndInt>"
                + "</type-validation>";
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
        assertEquals("Value \"\" is an invalid value. Expected values: [no-limit] or The argument is out of bounds <0, 4294967295>", response.getErrors().get(0).getErrorMessage());
    }

}

