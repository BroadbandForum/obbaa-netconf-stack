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
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorType;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;

@RunWith(RequestScopeJunitRunner.class)
public class EnumDSValidatorTest extends AbstractDataStoreValidatorTest {

	private static final String ENUM_VALIDATION_YANG_FILE = "/datastorevalidatortest/yangs/datastore-validator-enumtest.yang";
	protected static final QName VALIDATION_ENUM_QNAME = QName.create("urn:org:bbf2:pma:enumvalidation-yang", "2015-12-14", "enum-validation");
	protected static final QName VALIDATION_ENUM_QNAME2 = QName.create("urn:org:bbf2:pma:enumvalidation-yang", "2015-12-14", "enum-with-if-feature");
	protected static final QName VALIDATION_FEATURE_ENUM_QNAME = QName.create("urn:org:bbf2:pma:enumvalidation-yang", "2015-12-14", "test-feature");
	protected static final SchemaPath VALIDATION_ENUM1_SCHEMA_PATH = SchemaPath.create(true,VALIDATION_ENUM_QNAME);
	protected static final SchemaPath VALIDATION_ENUM_WITH_FEATURE_SCHEMA_PATH = SchemaPath.create(true,VALIDATION_ENUM_QNAME2);
	private static final String ENUM_DEFAULT_XML = "/datastorevalidatortest/yangs/datastore-validator-enumdefaultxml.xml";
	
	
	@BeforeClass
    public static void initializeOnce() throws SchemaBuildException {
        List<YangTextSchemaSource> yangFiles = TestUtil.getByteSources(getYang());
        Set<QName> feature_set = new HashSet<QName>();
        feature_set.add(VALIDATION_FEATURE_ENUM_QNAME); 
        m_schemaRegistry = new SchemaRegistryImpl(yangFiles, feature_set, Collections.emptyMap(), new NoLockService());
        m_schemaRegistry.setName(SchemaRegistry.GLOBAL_SCHEMA_REGISTRY);
    }
	
	protected static List<String> getYang() {
		List<String> fileList = new ArrayList<String>();
		fileList.add(ENUM_VALIDATION_YANG_FILE);
		return fileList;
	}
	
	@Override
	protected void addRootNodeHelpers() {
		addRootContainerNodeHelpers(VALIDATION_ENUM1_SCHEMA_PATH);
		addRootContainerNodeHelpers(VALIDATION_ENUM_WITH_FEATURE_SCHEMA_PATH);
	}
	@Override	
	protected String getXml() {
		return ENUM_DEFAULT_XML;
	}

	@Test
	public void testEnumDefaultValueValidation() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<enum-validation xmlns=\"urn:org:bbf2:pma:enumvalidation-yang\">" 
				+ " <enum-type-validation>"
				+ " </enum-type-validation>" 
				+ "</enum-validation>";

		editConfig(m_server, m_clientInfo, requestXml1, true);
		String ncResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" 
				+ " <data>"
				+ "  <enumvalidation:enum-validation xmlns:enumvalidation=\"urn:org:bbf2:pma:enumvalidation-yang\">"
				+ "	  <enumvalidation:validation>true</enumvalidation:validation>"
				+ "   <enumvalidation:enum-type-validation>"
				+ "  <enumvalidation:validation1>A</enumvalidation:validation1>"
				+ "  <enumvalidation:validation2>four</enumvalidation:validation2>"
				+ "   </enumvalidation:enum-type-validation>" 
				+ "  </enumvalidation:enum-validation>" 
				+ " </data>"
				+ "</rpc-reply>";
		verifyGet(ncResponse);
	}

	@Test
	public void testEnumIffeatureValidation() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<enum-validation xmlns=\"urn:org:bbf2:pma:enumvalidation-yang\">"
				+ "<validation>false</validation>" 
				+ "<enum-with-if-feature>"
				+ "  <leaf-if-feature>red</leaf-if-feature>" 
				+ " </enum-with-if-feature>" 
				+ "</enum-validation>";

		editConfig(m_server, m_clientInfo, requestXml1, true);
		String ncResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" 
				+ " <data>"
				+ "<enumvalidation:enum-validation xmlns:enumvalidation=\"urn:org:bbf2:pma:enumvalidation-yang\">"
				+ " <enumvalidation:validation>false</enumvalidation:validation>"
				+ " <enumvalidation:enum-with-if-feature>"
				+ "<enumvalidation:leaf-if-feature>red</enumvalidation:leaf-if-feature>"
				+ "</enumvalidation:enum-with-if-feature>" 
				+ "</enumvalidation:enum-validation>" 
				+ " </data>"
				+ "</rpc-reply>";
		verifyGet(ncResponse);
	}

	//@Test
	public void testEnumIffeatureFailureValidation() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<enum-validation xmlns=\"urn:org:bbf2:pma:enumvalidation-yang\">"
				+ " <enum-with-if-feature1>"
				+ "  <leaf-if-feature>green</leaf-if-feature>" 
				+ " </enum-with-if-feature1>" 
				+ "</enum-validation>";

		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals("Value \"green\" is an invalid value. Expected values: [yellow]",
				response.getErrors().get(0).getErrorMessage());
		assertEquals(NetconfRpcErrorType.Application, response.getErrors().get(0).getErrorType());
		assertEquals("/enumvalidation:enum-validation/enumvalidation:enum-with-if-feature1/enumvalidation:leaf-if-feature",
				response.getErrors().get(0).getErrorPath());
	}

	@Test
	public void testEnumUnicodeValidation() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<enum-validation xmlns=\"urn:org:bbf2:pma:enumvalidation-yang\">" 
				+ " <enum-type-validation>"
				+ "  <validation1>&#x41;</validation1>" 
				+ "  <validation2>three</validation2>"
				+ " </enum-type-validation>" 
				+ "</enum-validation>";

		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, true);
		String ncResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" 
				+ " <data>"
				+ "  <enumvalidation:enum-validation xmlns:enumvalidation=\"urn:org:bbf2:pma:enumvalidation-yang\">"
				+ "	<enumvalidation:validation>true</enumvalidation:validation>"
				+ "  <enumvalidation:enum-type-validation>"
				+ "  <enumvalidation:validation1>A</enumvalidation:validation1>"
				+ "  <enumvalidation:validation2>three</enumvalidation:validation2>"
				+ "   </enumvalidation:enum-type-validation>" 
				+ "  </enumvalidation:enum-validation>" 
				+ " </data>"
				+ "</rpc-reply>";
		verifyGet(ncResponse);
	}

	@Test
	public void testEnumControlCodeUnicodeValidation() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<enum-validation xmlns=\"urn:org:bbf2:pma:enumvalidation-yang\">" 
				+ " <enum-type-validation>"
				+ "  <validation1>&#x0000;</validation1>" 
				+ "  <validation2>three</validation2>"
				+ " </enum-type-validation>" 
				+ "</enum-validation>";
		try {
			NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
			fail();
		} catch (Exception e) {
			assertEquals("org.xml.sax.SAXParseException; lineNumber: 1; columnNumber: 148; Character reference \"&#",
					e.getMessage());
		}
	}

	@Test
	public void testEnumSubLevelFailureValidation() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<enum-validation xmlns=\"urn:org:bbf2:pma:enumvalidation-yang\">" 
				+ " <enum-type-validation>"
				+ "  <validation1>one</validation1>" 
				+ "  <validation2>three</validation2>" 
				+ " </enum-type-validation>"
				+ "</enum-validation>";

		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals("Value \"one\" is an invalid value. Expected values: [four, A]",
				response.getErrors().get(0).getErrorMessage());
		assertEquals(NetconfRpcErrorType.Application, response.getErrors().get(0).getErrorType());
		assertEquals("/enumvalidation:enum-validation/enumvalidation:enum-type-validation/enumvalidation:validation1",
				response.getErrors().get(0).getErrorPath());
	}

	@Test
	public void testEnumSubLevelFailureValidation2() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<enum-validation xmlns=\"urn:org:bbf2:pma:enumvalidation-yang\">" 
				+ " <enum-type-validation>"
				+ "  <validation1>four</validation1>" 
				+ "  <validation2>two</validation2>" 
				+ " </enum-type-validation>"
				+ "</enum-validation>";

		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals("Value \"two\" is an invalid value. Expected values: [four, three]",
				response.getErrors().get(0).getErrorMessage());
		assertEquals(NetconfRpcErrorType.Application, response.getErrors().get(0).getErrorType());
		assertEquals("/enumvalidation:enum-validation/enumvalidation:enum-type-validation/enumvalidation:validation2",
				response.getErrors().get(0).getErrorPath());
	}

	@Test
	public void testEnumSubLevelValidation() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<enum-validation xmlns=\"urn:org:bbf2:pma:enumvalidation-yang\">" 
				+ " <enum-type-validation>"
				+ " <validation1>A</validation1>" 
				+ " <validation2>three</validation2>" 
				+ " </enum-type-validation>"
				+ "</enum-validation>";

		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, true);
		String ncResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" 
				+ " <data>"
				+ "  <enumvalidation:enum-validation xmlns:enumvalidation=\"urn:org:bbf2:pma:enumvalidation-yang\">"
				+ "<enumvalidation:validation>true</enumvalidation:validation>"
				+ "   <enumvalidation:enum-type-validation>"
				+ "  <enumvalidation:validation1>A</enumvalidation:validation1>"
				+ "  <enumvalidation:validation2>three</enumvalidation:validation2>"
				+ "   </enumvalidation:enum-type-validation>" 
				+ "  </enumvalidation:enum-validation>" 
				+ " </data>"
				+ "</rpc-reply>";
		verifyGet(ncResponse);
	}
  
    @After
    public void teardown() {
        m_dataStore.disableUTSupport();
        m_datastoreValidator.setValidatedChildCacheHitStatus(false);
   }
}
