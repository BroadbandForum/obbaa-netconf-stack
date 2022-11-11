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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.net.util.Base64;
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
public class BinaryDSValidatorTest extends AbstractDataStoreValidatorTest {

    private static final String BINARY_VALIDATION_YANG_FILE = "/datastorevalidatortest/yangs/datastore-validator-binarytest.yang";
    protected static final QName VALIDATION_BINARY_QNAME = QName.create("urn:org:bbf2:pma:binaryvalidation-yang", "2020-03-26", "binary-validation");
    protected static final SchemaPath VALIDATION_BINARY_SCHEMA_PATH = SchemaPath.create(true, VALIDATION_BINARY_QNAME);
    private static final String BINARY_DEFAULT_XML = "/datastorevalidatortest/yangs/datastore-validator-binarydefaultxml.xml";

    @BeforeClass
    public static void initializeOnce() throws SchemaBuildException {
        List<YangTextSchemaSource> yangFiles = TestUtil.getByteSources(getYang());
        m_schemaRegistry = new SchemaRegistryImpl(yangFiles, Collections.EMPTY_SET, Collections.emptyMap(), new NoLockService());
        m_schemaRegistry.setName(SchemaRegistry.GLOBAL_SCHEMA_REGISTRY);
    }

    protected static List<String> getYang() {
        List<String> fileList = new ArrayList<String>();
        fileList.add(BINARY_VALIDATION_YANG_FILE);
        return fileList;
    }

    @Override
    protected void addRootNodeHelpers() {
        addRootContainerNodeHelpers(VALIDATION_BINARY_SCHEMA_PATH);
    }

    @Override
    protected String getXml() {
        return BINARY_DEFAULT_XML;
    }
    
    public static String convertOctettoBase64(byte[] string) {
        return Base64.encodeBase64String(string);
    }

    @Test
    public void testBinarytypeOverriddenValidation() throws Exception {
        getModelNode();
        String base64EncodedValue = convertOctettoBase64(new byte[] { 116, 101, 115, 116, 49, 50 });
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<binary-validation xmlns=\"urn:org:bbf2:pma:binaryvalidation-yang\">"
                + " <type-validation>" 
                + "<id>test</id>"
                + "<binary-type>" + base64EncodedValue + "</binary-type>" 
                + " </type-validation>" 
                + "</binary-validation>";

        editConfig(m_server, m_clientInfo, requestXml1, true);

        String ncResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
                + "<binaryvalidation:binary-validation xmlns:binaryvalidation=\"urn:org:bbf2:pma:binaryvalidation-yang\">"
                + "<binaryvalidation:type-validation>" 
                + "<binaryvalidation:binary-type>dGVzdDEy" 
                + "</binaryvalidation:binary-type>"
                + "<binaryvalidation:id>test</binaryvalidation:id>" 
                + "</binaryvalidation:type-validation>"
                + "</binaryvalidation:binary-validation>" 
                + "</data>" 
                + "</rpc-reply>";
        verifyGet(ncResponse);
    }

    @Test
    public void testFailurecaseBinarytypeOverriddenValidation() throws Exception {
        getModelNode();
        String base64EncodedValue = convertOctettoBase64(new byte[] { 116, 101, 115, 116, 49, 50, 51, 52, 53 });
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<binary-validation xmlns=\"urn:org:bbf2:pma:binaryvalidation-yang\">"
                + " <type-validation>" 
                + "<id>test</id>"
                + "<binary-type>" + base64EncodedValue + "</binary-type>" 
                + " </type-validation>" 
                + "</binary-validation>";
        editConfig(m_server, m_clientInfo, requestXml1, false);

        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals("The argument is out of bounds <6, 6>, <7, 7>", response.getErrors().get(0).getErrorMessage());
        assertEquals(NetconfRpcErrorType.Application, response.getErrors().get(0).getErrorType());
        assertEquals("/binaryvalidation:binary-validation/binaryvalidation:type-validation[binaryvalidation:id='test']/binaryvalidation:binary-type", response.getErrors().get(0).getErrorPath());
        assertEquals("length-out-of-specified-bounds", response.getErrors().get(0).getErrorAppTag());
    }

    @Test
    public void testBinarytypeValidation() throws Exception {
        getModelNode();
        String base64EncodedValue = convertOctettoBase64(new byte[] { 116, 101, 115, 116, 49, 50, 51, 52 });
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<binary-validation xmlns=\"urn:org:bbf2:pma:binaryvalidation-yang\">"
                + " <type-validation>" 
                + "<id>test</id>"
                + "<custom-binary-type>" + base64EncodedValue + "</custom-binary-type>" 
                + " </type-validation>" 
                + "</binary-validation>";

        editConfig(m_server, m_clientInfo, requestXml1, true);

        String ncResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
                + "<binaryvalidation:binary-validation xmlns:binaryvalidation=\"urn:org:bbf2:pma:binaryvalidation-yang\">"
                + "<binaryvalidation:type-validation>" 
                + "<binaryvalidation:custom-binary-type>dGVzdDEyMzQ="
                + "</binaryvalidation:custom-binary-type>" 
                + "<binaryvalidation:id>test</binaryvalidation:id>"
                + "</binaryvalidation:type-validation>" 
                + "</binaryvalidation:binary-validation>" 
                + "</data>" 
                + "</rpc-reply>";
        verifyGet(ncResponse);
    }
      
    @Test
    public void testCustomErrorMsgOfBinarytypeValidation() throws Exception {
        getModelNode();
        String base64EncodedValue = convertOctettoBase64(new byte[] { 116, 101, 115, 116, 49, 50, 51, 52, 53 });
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<binary-validation xmlns=\"urn:org:bbf2:pma:binaryvalidation-yang\">"
                + " <type-validation>" 
                + "<id>test</id>"
                + "<custom-binary-type>" + base64EncodedValue + "</custom-binary-type>" 
                + " </type-validation>" 
                + "</binary-validation>";

        editConfig(m_server, m_clientInfo, requestXml1, false);

        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals("length constraint error-app-message", response.getErrors().get(0).getErrorMessage());
        assertEquals(NetconfRpcErrorType.Application, response.getErrors().get(0).getErrorType());
        assertEquals("/binaryvalidation:binary-validation/binaryvalidation:type-validation[binaryvalidation:id='test']/binaryvalidation:custom-binary-type", response.getErrors().get(0).getErrorPath());
        assertEquals("length constraint error-app-tag", response.getErrors().get(0).getErrorAppTag());
    }

    @Test
    public void testSingleLengthValueInBinarytypeValidation() throws Exception {
        getModelNode();
        String base64EncodedValue = convertOctettoBase64(new byte[] { 116, 101, 115, 116, 49, 50, 51 });
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<binary-validation xmlns=\"urn:org:bbf2:pma:binaryvalidation-yang\">"
                + " <type-validation>" 
                + "<id>test</id>"
                + "<single-binary-type>" + base64EncodedValue + "</single-binary-type>" 
                + " </type-validation>" 
                + "</binary-validation>";

        editConfig(m_server, m_clientInfo, requestXml1, true);

        String ncResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<data>"
                + "<binaryvalidation:binary-validation xmlns:binaryvalidation=\"urn:org:bbf2:pma:binaryvalidation-yang\">"
                + "<binaryvalidation:type-validation>" 
                + "<binaryvalidation:single-binary-type>dGVzdDEyMw=="
                + "</binaryvalidation:single-binary-type>" 
                + "<binaryvalidation:id>test</binaryvalidation:id>"
                + "</binaryvalidation:type-validation>" 
                + "</binaryvalidation:binary-validation>" 
                + "</data>" 
                + "</rpc-reply>";
        verifyGet(ncResponse);
    }

    @Test
    public void testFailureCaseBinaryValidation() throws Exception {
        getModelNode();
        String base64EncodedValue = convertOctettoBase64(new byte[] { 116, 101, 115, 116, 49, 50, 51, 52, 53 });
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<binary-validation xmlns=\"urn:org:bbf2:pma:binaryvalidation-yang\">"
                + " <type-validation>" 
                + "<id>test</id>"
                + "<single-binary-type>" + base64EncodedValue + "</single-binary-type>" 
                + " </type-validation>" 
                + "</binary-validation>";

        editConfig(m_server, m_clientInfo, requestXml1, false);

        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals("The argument is out of bounds <7, 7>", response.getErrors().get(0).getErrorMessage());
        assertEquals(NetconfRpcErrorType.Application, response.getErrors().get(0).getErrorType());
        assertEquals("/binaryvalidation:binary-validation/binaryvalidation:type-validation[binaryvalidation:id='test']/binaryvalidation:single-binary-type", response.getErrors().get(0).getErrorPath());
        assertEquals("length-out-of-specified-bounds", response.getErrors().get(0).getErrorAppTag());
    }
     
    @After
    public void teardown() {
        m_dataStore.disableUTSupport();
        m_datastoreValidator.setValidatedChildCacheHitStatus(false);
    }
}
