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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;

@RunWith(RequestScopeJunitRunner.class)
public class IntegerTypeTypeNotationForDefaultDSValidatorTest extends AbstractDataStoreValidatorTest {

	private static final String INTEGER_TYPE_DEFAULT_VALUE_VALIDATION_YANG_FILE = "/datastorevalidatortest/yangs/datastore-integer-type-default-notation-validator-test.yang";
	private static final QName INTEGER_TYPE_DEFAULT_VALUE_VALIDATION_QNAME = QName.create("urn:org:bbf:pma:validation:defaultvalue", "2019-02-27", "default-value-for-integer-type-validation");
	private static final SchemaPath INTEGER_TYPE_DEFAULT_VALUE_VALIDATION_SCHEMA_PATH = SchemaPath.create(true, INTEGER_TYPE_DEFAULT_VALUE_VALIDATION_QNAME);

	@BeforeClass
    public static void initializeOnce() throws SchemaBuildException {
        List<YangTextSchemaSource> yangFiles = TestUtil.getByteSources(getYang());
        m_schemaRegistry = new SchemaRegistryImpl(yangFiles, Collections.emptySet(), Collections.emptyMap(), new NoLockService());
        m_schemaRegistry.setName(SchemaRegistry.GLOBAL_SCHEMA_REGISTRY);
    }
	
	@Test
	public void testIntegerTypeDefaultValues() throws Exception {
		sendEditConfigAndVerifyGetForIntegerTypeDefaultValues("decimal-notation-for-default-validation", "decimal-list");
		sendEditConfigAndVerifyGetForIntegerTypeDefaultValues("octal-notation-for-default-validation", "octal-list");
		sendEditConfigAndVerifyGetForIntegerTypeDefaultValues("hexa-decimal-notation-for-default-validation", "hexa-decimal-list");
	}

	private String getEditRequest(String containerName, String listName, String requestLeafXml) {
		String requestXml_prefix = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    "
				+ " <default-value-for-integer-type-validation xmlns=\"urn:org:bbf:pma:validation:defaultvalue\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
				+ "		<" + containerName + ">"
				+ "		<" + listName + ">"
				+ "   		<id>12</id>";
		String requestXml_suffix =
				"		</" + listName +">"
				+ "		</" + containerName + ">"
				+ " </default-value-for-integer-type-validation>";
		return requestXml_prefix + requestLeafXml + requestXml_suffix;
	}

	private String getDeleteRequest(String containerName) {
		String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    "
				+ " <default-value-for-integer-type-validation xmlns=\"urn:org:bbf:pma:validation:defaultvalue\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
				+ "		<" + containerName + " xc:operation=\"delete\">"
				+ "		</" + containerName + ">"
				+ " </default-value-for-integer-type-validation>";
		return requestXml;
	}

	private void sendEditConfigAndVerifyGetForIntegerTypeDefaultValues(String containerName, String listName) throws Exception {
		getModelNode();
		String requestLeafXml = "<int8-validation>true</int8-validation>"
				+ "<int16-validation>true</int16-validation>"
				+ "<int32-validation>true</int32-validation>"
				+ "<int64-validation>true</int64-validation>"
				+ "<uint8-validation>true</uint8-validation>"
				+ "<uint16-validation>true</uint16-validation>"
				+ "<uint32-validation>true</uint32-validation>"
				+ "<uint64-validation>true</uint64-validation>";
		String requestXml = getEditRequest(containerName , listName, requestLeafXml);
		editConfig(m_server, m_clientInfo, requestXml, true);
		String responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
				+ "	<default-value-for-integer-type-validation:default-value-for-integer-type-validation xmlns:default-value-for-integer-type-validation=\"urn:org:bbf:pma:validation:defaultvalue\">"
				+ "		<default-value-for-integer-type-validation:" + containerName + ">"
				+ "			<default-value-for-integer-type-validation:" + listName + ">"
				+ "				<default-value-for-integer-type-validation:id>12</default-value-for-integer-type-validation:id>"
				+ "				<default-value-for-integer-type-validation:int8-validation>true</default-value-for-integer-type-validation:int8-validation>"
				+ "				<default-value-for-integer-type-validation:int8-type>11</default-value-for-integer-type-validation:int8-type>"
				+ "				<default-value-for-integer-type-validation:int16-validation>true</default-value-for-integer-type-validation:int16-validation>"
				+ "				<default-value-for-integer-type-validation:int16-type>12</default-value-for-integer-type-validation:int16-type>"
				+ "				<default-value-for-integer-type-validation:int32-validation>true</default-value-for-integer-type-validation:int32-validation>"
				+ "				<default-value-for-integer-type-validation:int32-type>-13</default-value-for-integer-type-validation:int32-type>"
				+ "				<default-value-for-integer-type-validation:int64-validation>true</default-value-for-integer-type-validation:int64-validation>"
				+ "				<default-value-for-integer-type-validation:int64-type>14</default-value-for-integer-type-validation:int64-type>"
				+ "				<default-value-for-integer-type-validation:uint8-validation>true</default-value-for-integer-type-validation:uint8-validation>"
				+ "				<default-value-for-integer-type-validation:uint8-type>15</default-value-for-integer-type-validation:uint8-type>"
				+ "				<default-value-for-integer-type-validation:uint16-validation>true</default-value-for-integer-type-validation:uint16-validation>"
				+ "				<default-value-for-integer-type-validation:uint16-type>16</default-value-for-integer-type-validation:uint16-type>"
				+ "				<default-value-for-integer-type-validation:uint32-validation>true</default-value-for-integer-type-validation:uint32-validation>"
				+ "				<default-value-for-integer-type-validation:uint32-type>17</default-value-for-integer-type-validation:uint32-type>"
				+ "				<default-value-for-integer-type-validation:uint64-validation>true</default-value-for-integer-type-validation:uint64-validation>"
				+ "				<default-value-for-integer-type-validation:uint64-type>18</default-value-for-integer-type-validation:uint64-type>"
				+ "			</default-value-for-integer-type-validation:" + listName + ">"
				+ "		</default-value-for-integer-type-validation:" + containerName + ">"
				+ "	</default-value-for-integer-type-validation:default-value-for-integer-type-validation>"
				+ " " + "</data>"
				+ "</rpc-reply>";
		verifyGet(responseXml);
		deleteContainer(containerName);
	}

	private void deleteContainer(String containerName) throws Exception {
		getModelNode();
		String requestXml = getDeleteRequest(containerName);
		editConfig(m_server, m_clientInfo, requestXml, true);
		String responseXml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" +
				"<data>" +
				"<default-value-for-integer-type-validation:default-value-for-integer-type-validation xmlns:default-value-for-integer-type-validation=\"urn:org:bbf:pma:validation:defaultvalue\"/>" +
				"</data>" +
				"</rpc-reply>";
		verifyGet(responseXml);
	}

	protected static List<String> getYang() {
		List<String> fileList = new ArrayList<String>();
		fileList.add(INTEGER_TYPE_DEFAULT_VALUE_VALIDATION_YANG_FILE);
		return fileList;
	}

	protected void loadDefaultXml(){
		// as part of this test case, we don't have any need to create default container
	}

	@Override
	protected SchemaPath getSchemaPath() {
		return INTEGER_TYPE_DEFAULT_VALUE_VALIDATION_SCHEMA_PATH;
	}

	@Override
	protected void addRootNodeHelpers() {
		addRootContainerNodeHelpers(getSchemaPath());
	}
}