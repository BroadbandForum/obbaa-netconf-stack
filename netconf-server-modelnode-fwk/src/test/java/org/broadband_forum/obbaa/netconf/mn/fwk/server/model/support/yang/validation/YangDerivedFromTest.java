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

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.xml.sax.SAXException;

import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;

public class YangDerivedFromTest extends AbstractRootModelTest {

    static QName createQName(String localName) {
        return QName.create("urn:org:bbf:yang:test:derived:from", "2017-08-20", localName);
    }

    static QName AUGMENT_TEST_QNAME = createQName("augmentTest");
    static QName AUG1_QNAME = createQName("aug1");
    static QName AUG2_QNAME = createQName("aug2");
    static QName AUG3_QNAME = createQName("aug3");
    static QName AUG4_QNAME = createQName("aug4");
    static QName AUGTEST_LIST1_QNAME = createQName("list1");
    static QName AUGTEST_LIST2_QNAME = createQName("list2");
    static QName AUGTEST_SOME_THING = createQName("someThing");
    static QName ID_REF_DELETE_TEST = createQName("idRefDeleteTest");

    static SchemaPath AUGMENT_TEST_SCHEMAPATH = buildSchemaPath(XML_SUBTREE_SCHEMA_PATH, AUGMENT_TEST_QNAME);
    static SchemaPath AUG1_SCHEMAPATH = buildSchemaPath(AUGMENT_TEST_SCHEMAPATH, AUG1_QNAME);
    static SchemaPath AUG2_SCHEMAPATH = buildSchemaPath(AUGMENT_TEST_SCHEMAPATH, AUG2_QNAME);
    static SchemaPath AUG3_SCHEMAPATH = buildSchemaPath(AUGMENT_TEST_SCHEMAPATH, AUG3_QNAME);
    static SchemaPath AUG4_SCHEMAPATH = buildSchemaPath(AUGMENT_TEST_SCHEMAPATH, AUG4_QNAME);
    static SchemaPath AUGTEST_LIST1_SCHEMAPATH = buildSchemaPath(AUGMENT_TEST_SCHEMAPATH, AUGTEST_LIST1_QNAME);
    static SchemaPath AUGTEST_LIST2_SCHEMAPATH = buildSchemaPath(AUGTEST_LIST1_SCHEMAPATH, AUGTEST_LIST2_QNAME);
    static SchemaPath AUG1_AUG1_SCHEMAPATH = buildSchemaPath(AUG1_SCHEMAPATH, AUG1_QNAME);
    static SchemaPath AUGTEST_SOME_THING_SCHEMAPATH = buildSchemaPath(AUGMENT_TEST_SCHEMAPATH, AUGTEST_SOME_THING);
    static SchemaPath ID_REF_DELETE_TEST_SCHEMAPATH = buildSchemaPath(AUGMENT_TEST_SCHEMAPATH, ID_REF_DELETE_TEST);

    protected List<String> getYangFiles() {
        List<String> fileNames = new LinkedList<String>();
        fileNames.add("/datastorevalidatortest/yangs/dummy-extension.yang");
        fileNames.add("/datastorevalidatortest/yangs/datastore-validator-test2.yang");
        fileNames.add("/datastorevalidatortest/yangs/datastore-derived-from.yang");
        return fileNames;
    }

    @Test
    public void testWhenFailureOnNonExistantNodeAugment() throws Exception {
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                " <xml-subtree>" +
                " </xml-subtree>" +
                "</validation>                                                 ";
        editConfig(requestXml);

        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                " <validation>nothing</validation>" +
                " <xml-subtree>" +
                " </xml-subtree>" +
                "</validation>                                                 ";
        editConfig(requestXml);

        String response =
                " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + " <data>"
                        + "  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
                        + "   <validation:validation>nothing</validation:validation>"
                        + "   <validation:xml-subtree>"
                        + "    <ddf:augmentTest xmlns:ddf=\"urn:org:bbf:yang:test:derived:from\">"
                        + "     <ddf:someThing>"
                        + "      <ddf:someThing>0</ddf:someThing>"
                        + "     </ddf:someThing>"
                        + "    </ddf:augmentTest>"
                        + "   </validation:xml-subtree>"
                        + "  </validation:validation>"
                        + " </data>"
                        + "</rpc-reply>";
        verifyGet(m_server, m_clientInfo, response);


    }

    @Test
    public void testAugmentValidationOnSameNameChildParent() throws Exception {
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                " <xml-subtree>" +
                " <augmentTest xmlns=\"urn:org:bbf:yang:test:derived:from\">" +
                "  <baseType xmlns=\"urn:org:bbf:yang:test:derived:from\">identity1</baseType>" +
                "  <aug1>" +
                "    <leaf1>hello</leaf1>" +
                "    <aug1/>" +
                "  </aug1> " +
                " </augmentTest>" +
                " </xml-subtree>" +
                "</validation>                                                 ";
        editConfig(requestXml);

        String response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + " <data>"
                        + "  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
                        + "   <validation:xml-subtree>"
                        + "    <ddf:augmentTest xmlns:ddf=\"urn:org:bbf:yang:test:derived:from\">"
                        + "     <ddf:aug1>"
                        + "      <ddf:aug1/>"
                        + "      <ddf:leaf1>hello</ddf:leaf1>"
                        + "     </ddf:aug1>"
                        + "     <ddf:baseType>ddf:identity1</ddf:baseType>"
                        + "    </ddf:augmentTest>"
                        + "   </validation:xml-subtree>"
                        + "  </validation:validation>"
                        + " </data>"
                        + "</rpc-reply>";
        verifyGet(m_server, m_clientInfo, response);
    }

    @Test
    public void testDerivedFromOrSelfInWhen() throws Exception {
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                " <xml-subtree>" +
                " <augmentTest xmlns=\"urn:org:bbf:yang:test:derived:from\">" +
                "  <list1>" +
                "   <leaf1>hello</leaf1>" +
                "   <leaf3 xmlns=\"urn:org:bbf:yang:test:derived:from\">identity1</leaf3>" +
                "   <leaf2>hello</leaf2>" +
                "  </list1>" +
                " </augmentTest>" +
                " </xml-subtree>" +
                "</validation>                                                 ";
        editConfig(requestXml);

        String response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + " <data>"
                        + "  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
                        + "   <validation:xml-subtree>"
                        + "    <ddf:augmentTest xmlns:ddf=\"urn:org:bbf:yang:test:derived:from\">"
                        + "     <ddf:list1>"
                        + "      <ddf:leaf1>hello</ddf:leaf1>"
                        + "      <ddf:leaf2>hello</ddf:leaf2>"
                        + "      <ddf:leaf3>ddf:identity1</ddf:leaf3>"
                        + "     </ddf:list1>"
                        + "    </ddf:augmentTest>"
                        + "   </validation:xml-subtree>"
                        + "  </validation:validation>"
                        + " </data>"
                        + "</rpc-reply>";
        verifyGet(m_server, m_clientInfo, response);
    }

    @Test
    public void testFailForDerivedFromInWhen() throws Exception {
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                " <xml-subtree>" +
                " <augmentTest xmlns=\"urn:org:bbf:yang:test:derived:from\">" +
                "  <list1>" +
                "   <leaf1>hello</leaf1>" +
                "   <leaf3 xmlns=\"urn:org:bbf:yang:test:derived:from\">some-identity</leaf3>" +
                "   <leaf2>hello</leaf2>" +
                "  </list1>" +
                " </augmentTest>" +
                " </xml-subtree>" +
                "</validation>                                                 ";
        NetConfResponse response = editConfigAsFalse(requestXml);

        assertEquals("/validation:validation/validation:xml-subtree/ddf:augmentTest/ddf:list1[ddf:leaf1=hello]/ddf" +
                ":leaf2", response.getErrors().get(0).getErrorPath());
        assertEquals("Violate when constraints: derived-from(../../list1[current()]/leaf3, \'some-identity\')",
                response.getErrors().get(0).getErrorMessage());

    }

    @Test
    public void testForDerivedOrSelfOnAContainer() throws Exception {
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                " <xml-subtree>" +
                " <augmentTest xmlns=\"urn:org:bbf:yang:test:derived:from\">" +
                "  <list1>" +
                "   <leaf1>hello</leaf1>" +
                "   <leaf3 xmlns=\"urn:org:bbf:yang:test:derived:from\">identity1</leaf3>" +
                "   <list2>" +
                "    <leaf1>hello</leaf1>" +
                "   </list2>" +
                "  </list1>" +
                " </augmentTest>" +
                " </xml-subtree>" +
                "</validation>                                                 ";
        editConfig(requestXml);

        String response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + " <data>"
                        + "  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
                        + "   <validation:xml-subtree>"
                        + "    <ddf:augmentTest xmlns:ddf=\"urn:org:bbf:yang:test:derived:from\">"
                        + "     <ddf:list1>"
                        + "      <ddf:leaf1>hello</ddf:leaf1>"
                        + "      <ddf:leaf3>ddf:identity1</ddf:leaf3>"
                        + "      <ddf:list2>"
                        + "       <ddf:leaf1>hello</ddf:leaf1>"
                        + "      </ddf:list2>"
                        + "     </ddf:list1>"
                        + "    </ddf:augmentTest>"
                        + "   </validation:xml-subtree>"
                        + "  </validation:validation>"
                        + " </data>"
                        + "</rpc-reply>";
        verifyGet(m_server, m_clientInfo, response);

    }

    @Test
    public void testForDerivedOrSelfWithAbsPath() throws Exception {
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                " <xml-subtree>" +
                " <augmentTest xmlns=\"urn:org:bbf:yang:test:derived:from\">" +
                "  <list1>" +
                "   <leaf1>hello</leaf1>" +
                "   <leaf3 xmlns=\"urn:org:bbf:yang:test:derived:from\">identity1</leaf3>" +
                "   <leaf4>hello</leaf4>" +
                "  </list1>" +
                " </augmentTest>" +
                " </xml-subtree>" +
                "</validation>                                                 ";
        editConfig(requestXml);

        String response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + " <data>"
                        + "  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
                        + "   <validation:xml-subtree>"
                        + "    <ddf:augmentTest xmlns:ddf=\"urn:org:bbf:yang:test:derived:from\">"
                        + "     <ddf:list1>"
                        + "      <ddf:leaf1>hello</ddf:leaf1>"
                        + "      <ddf:leaf3>ddf:identity1</ddf:leaf3>"
                        + "      <ddf:leaf4>hello</ddf:leaf4>"
                        + "     </ddf:list1>"
                        + "    </ddf:augmentTest>"
                        + "   </validation:xml-subtree>"
                        + "  </validation:validation>"
                        + " </data>"
                        + "</rpc-reply>";
        verifyGet(m_server, m_clientInfo, response);

    }

    @Test
    public void testDerivedFromOrSelf() throws Exception {
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                " <xml-subtree>" +
                " <augmentTest xmlns=\"urn:org:bbf:yang:test:derived:from\">" +
                "  <baseType xmlns=\"urn:org:bbf:yang:test:derived:from\">some-identity</baseType>" +
                "  <aug2/>" +
                " </augmentTest>" +
                " </xml-subtree>" +
                "</validation>                                                 ";
        editConfig(requestXml);

        String response = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
                + "   <validation:xml-subtree> "
                + "    <ddf:augmentTest xmlns:ddf=\"urn:org:bbf:yang:test:derived:from\">"
                + "     <ddf:aug2/>"
                + "     <ddf:baseType>ddf:some-identity</ddf:baseType>"
                + "    </ddf:augmentTest>"
                + "   </validation:xml-subtree>"
                + "  </validation:validation>"
                + " </data>"
                + "</rpc-reply>";
        verifyGet(m_server, m_clientInfo, response);

    }

    @Test
    public void testFailDerivedFromOrSelf() throws Exception {
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                " <xml-subtree>" +
                " <augmentTest xmlns=\"urn:org:bbf:yang:test:derived:from\">" +
                "  <baseType xmlns=\"urn:org:bbf:yang:test:derived:from\">some-identity</baseType>" +
                "  <aug4/>" +
                " </augmentTest>" +
                " </xml-subtree>" +
                "</validation>                                                 ";
        NetConfResponse response = editConfigAsFalse(requestXml);

        assertEquals("/validation:validation/validation:xml-subtree/ddf:augmentTest/ddf:aug4", response.getErrors()
                .get(0).getErrorPath());
        assertEquals("Violate when constraints: derived-from-or-self(baseType, \'some-identity1\') or " +
                "derived-from-or-self(baseType, \'some-identity2\')", response.getErrors().get(0).getErrorMessage());
    }

    @Test
    public void testDerviedFrom() throws SAXException, IOException {
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                " <xml-subtree>" +
                " <augmentTest xmlns=\"urn:org:bbf:yang:test:derived:from\">" +
                "  <baseType xmlns=\"urn:org:bbf:yang:test:derived:from\">identity1</baseType>" +
                "  <aug1/>" +
                " </augmentTest>" +
                " </xml-subtree>" +
                "</validation>                                                 ";
        editConfig(requestXml);

        String response = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
                + "   <validation:xml-subtree> "
                + "    <ddf:augmentTest xmlns:ddf=\"urn:org:bbf:yang:test:derived:from\">"
                + "     <ddf:aug1/>"
                + "     <ddf:baseType>ddf:identity1</ddf:baseType>"
                + "    </ddf:augmentTest>"
                + "   </validation:xml-subtree>"
                + "  </validation:validation>"
                + " </data>"
                + "</rpc-reply>";
        verifyGet(m_server, m_clientInfo, response);
    }

    @Test
    public void testDerviedFromDifferentModule() throws SAXException, IOException {
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                " <xml-subtree>" +
                " <augmentTest xmlns=\"urn:org:bbf:yang:test:derived:from\">" +
                "  <baseTypeFromDifferentModule " +
                "xmlns=\"urn:org:bbf:yang:test:derived:from\">identity4</baseTypeFromDifferentModule>" +
                "  <aug3/>" +
                " </augmentTest>" +
                " </xml-subtree>" +
                "</validation>                                                 ";
        editConfig(requestXml);

        String response = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
                + "   <validation:xml-subtree> "
                + "    <ddf:augmentTest xmlns:ddf=\"urn:org:bbf:yang:test:derived:from\">"
                + "     <ddf:aug3/>"
                + "     <ddf:baseTypeFromDifferentModule>ddf:identity4</ddf:baseTypeFromDifferentModule>"
                + "    </ddf:augmentTest>"
                + "   </validation:xml-subtree>"
                + "  </validation:validation>"
                + " </data>"
                + "</rpc-reply>";
        verifyGet(m_server, m_clientInfo, response);
    }

    @Test
    public void testDerivedFromOnMust() throws Exception {
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                " <xml-subtree>" +
                " <augmentTest xmlns=\"urn:org:bbf:yang:test:derived:from\">" +
                "  <baseTypeFromDifferentModule " +
                "xmlns=\"urn:org:bbf:yang:test:derived:from\">identity4</baseTypeFromDifferentModule>" +
                "  <list1>" +
                "   <leaf1>anything</leaf1>" +
                "   <leafUnwanted>unwanted</leafUnwanted>" +
                "  </list1>" +
                "  <aug3/>" +
                " </augmentTest>" +
                " </xml-subtree>" +
                "</validation>                                                 ";
        NetConfResponse response = editConfigAsFalse(requestXml);
        assertEquals("/validation:validation/validation:xml-subtree/ddf:augmentTest/ddf:list1[ddf:leaf1=anything]/ddf" +
                ":leafUnwanted", response.getErrors().get(0).getErrorPath());
        assertEquals("Violate must constraints: derived-from-or-self(../../list1[current()]/leaf3, \"some-identity\")" +
                "", response.getErrors().get(0).getErrorMessage());

        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                " <xml-subtree>" +
                " <augmentTest xmlns=\"urn:org:bbf:yang:test:derived:from\">" +
                "  <baseTypeFromDifferentModule " +
                "xmlns=\"urn:org:bbf:yang:test:derived:from\">identity4</baseTypeFromDifferentModule>" +
                "  <list1>" +
                "   <leaf1>unwanted</leaf1>" +
                "   <leafUnwanted>unwanted</leafUnwanted>" +
                "   <leaf3 xmlns=\"urn:org:bbf:yang:test:derived:from\">some-identity</leaf3>" +
                "  </list1>" +
                "  <aug3/>" +
                " </augmentTest>" +
                " </xml-subtree>" +
                "</validation>                                                 ";
        editConfig(requestXml);
    }

    @Test
    public void testIdRefWithWhen() throws Exception {
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                " <xml-subtree>" +
                " <augmentTest xmlns=\"urn:org:bbf:yang:test:derived:from\">" +
                "  <idRefDeleteTest>" +
                "   <key1 xmlns:validation=\"urn:org:bbf:pma:validation\">validation:identity1</key1>" +
                "   <someLeaf>hello</someLeaf>" +
                "  </idRefDeleteTest>" +
                " </augmentTest>" +
                " </xml-subtree>" +
                "</validation>                                                 ";
        NetConfResponse response = editConfig(requestXml);
        assertEquals(true, response.isOk());

        String xmlResponse =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + " <data>"
                        + "  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
                        + "   <validation:xml-subtree>"
                        + "    <ddf:augmentTest xmlns:ddf=\"urn:org:bbf:yang:test:derived:from\">"
                        + "     <ddf:idRefDeleteTest>"
                        + "      <ddf:key1>validation:identity1</ddf:key1>"
                        + "      <ddf:someLeaf>hello</ddf:someLeaf>"
                        + "      <ddf:someLeaf1>1</ddf:someLeaf1>"
                        + "     </ddf:idRefDeleteTest>"
                        + "    </ddf:augmentTest>"
                        + "   </validation:xml-subtree>"
                        + "  </validation:validation>"
                        + " </data>"
                        + "</rpc-reply>";
        verifyGet(m_server, m_clientInfo, xmlResponse);

        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                " <xml-subtree>" +
                " <augmentTest xmlns=\"urn:org:bbf:yang:test:derived:from\">" +
                "  <idRefDeleteTest xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                "   <key1 xmlns:validation=\"urn:org:bbf:pma:validation\">validation:identity1</key1>" +
                "   <someLeaf xc:operation=\"delete\">hello</someLeaf>" +
                "  </idRefDeleteTest>" +
                " </augmentTest>" +
                " </xml-subtree>" +
                "</validation>                                                 ";
        response = editConfig(requestXml);
        assertEquals(true, response.isOk());

        xmlResponse =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + " <data>"
                        + "  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
                        + "   <validation:xml-subtree>"
                        + "    <ddf:augmentTest xmlns:ddf=\"urn:org:bbf:yang:test:derived:from\">"
                        + "     <ddf:idRefDeleteTest>"
                        + "      <ddf:key1>validation:identity1</ddf:key1>"
                        + "     </ddf:idRefDeleteTest>"
                        + "    </ddf:augmentTest>"
                        + "   </validation:xml-subtree>"
                        + "  </validation:validation>"
                        + " </data>"
                        + "</rpc-reply>"
        ;
        verifyGet(m_server, m_clientInfo, xmlResponse);
    }

}
