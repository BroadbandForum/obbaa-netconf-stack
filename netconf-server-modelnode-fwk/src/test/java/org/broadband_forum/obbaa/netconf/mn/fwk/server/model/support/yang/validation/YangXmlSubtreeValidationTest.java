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

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.utils.AnnotationAnalysisException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.utils.EntityRegistryBuilder;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildContainerHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeFactoryException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootEntityContainerModelNodeHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.YangUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.AddDefaultDataInterceptor;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.validation.model.CrossTest;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.ValidationConstants;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.xml.sax.SAXException;

import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;

@SuppressWarnings("deprecation")
public class YangXmlSubtreeValidationTest extends AbstractRootModelTest {

    static QName createQName(String localName) {
        return QName.create(ValidationConstants.CROSS_TEST_NS, ValidationConstants.CROSS_TEST_REVISION, localName);
    }

    public static final QName CROSS_TEST_QNAME = createQName("CrossTest");
    public static final QName LIST1_QNAME = createQName("list1");
    public static final QName XML_SUB_TREE_QNAME = createQName("xml-subtree");

    public static final SchemaPath CROSS_TEST_SCHEMAPATH = SchemaPath.create(true, CROSS_TEST_QNAME);
    public static final SchemaPath CT_XML_SUBTREE_SCHEMAPATH = buildSchemaPath(CROSS_TEST_SCHEMAPATH,
            XML_SUB_TREE_QNAME);
    public static final SchemaPath LIST1_SCHEMAPATH = buildSchemaPath(CT_XML_SUBTREE_SCHEMAPATH, LIST1_QNAME);

    protected void loadXmlDataIntoServer() {
        super.loadXmlDataIntoServer();
        YangUtils.loadXmlDataIntoServer(m_server,
                getClass().getResource("/datastorevalidatortest/yangs/datastore-validator-cross-test.xml").getPath());
    }

    protected List<String> getYangFiles() {
        List<String> fileNames = new LinkedList<String>();
        fileNames.add("/datastorevalidatortest/yangs/dummy-extension.yang");
        fileNames.add("/datastorevalidatortest/yangs/datastore-validator-test2.yang");
        fileNames.add("/datastorevalidatortest/yangs/datastore-validation-crossTree-test.yang");
        return fileNames;
    }

    protected void addRootNodeHelpers() {
        super.addRootNodeHelpers();
        ContainerSchemaNode schemaNode = (ContainerSchemaNode) m_schemaRegistry.getDataSchemaNode
                (CROSS_TEST_SCHEMAPATH);
        ChildContainerHelper containerHelper = new RootEntityContainerModelNodeHelper(schemaNode,
                m_modelNodeHelperRegistry,
                m_subSystemRegistry, m_schemaRegistry, m_aggregatedDSM);
        m_rootModelNodeAggregator.addModelServiceRootHelper(CROSS_TEST_SCHEMAPATH, containerHelper);
    }

    @SuppressWarnings("rawtypes")
    protected void initializeEntityRegistry() throws AnnotationAnalysisException {
        super.initializeEntityRegistry();
        List<Class> classes = new ArrayList<>();
        classes.add(CrossTest.class);
        EntityRegistryBuilder.updateEntityRegistry(COMPONENT_ID, classes, m_entityRegistry, m_schemaRegistry,
                m_persistenceManagerUtil.getEntityDataStoreManager(), m_modelNodeDSMRegistry);
        m_modelNodeDSMRegistry.register(COMPONENT_ID, CROSS_TEST_SCHEMAPATH, m_xmlSubtreeDSM);
    }

    @Override
    @Before
    public void setup() throws SchemaBuildException, AnnotationAnalysisException, ModelNodeFactoryException {
        super.setup();
    }

    @Test
    public void crossReferencedTest() throws Exception {
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "   <someList>" +
                "    <someKey>key1</someKey>" +
                "    <someInnerList>" +
                "      <someKey>skey1</someKey>" +
                "      <childContainer>" +
                "         <xmlList>" +
                "           <xmlListLeaf>10</xmlListLeaf>" +
                "           <type>20</type>" +
                "         </xmlList>" +
                "      </childContainer>" +
                "   </someInnerList>" +
                "  </someList>" +
                "</validation>                                                 ";
        editConfig(requestXml);

        //verify response has the changes
        String response = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <ctr:CrossTest xmlns:ctr=\"urn:org:bbf:yang:test:cross:tree:reference\"/>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
                + "   <validation:someList>"
                + "    <validation:someInnerList>"
                + "     <validation:childContainer>"
                + "       <validation:xmlList>"
                + "        <validation:xmlListLeaf>10</validation:xmlListLeaf>"
                + "        <validation:type>20</validation:type>"
                + "       </validation:xmlList>"
                + "      </validation:childContainer>"
                + "     <validation:someKey>skey1</validation:someKey>"
                + "    </validation:someInnerList>"
                + "    <validation:someKey>key1</validation:someKey>"
                + "   </validation:someList>"
                + "  </validation:validation>"
                + " </data>"
                + "</rpc-reply>";
        verifyGet(m_server, m_clientInfo, response);

        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\" " +
                "xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                "   <someList>" +
                "    <someKey>key1</someKey>" +
                "    <someInnerList>" +
                "      <someKey>skey1</someKey>" +
                "      <childContainer xc:operation='delete'>" +
                "         <xmlList >" +
                "           <xmlListLeaf>10</xmlListLeaf>" +
                "           <type>20</type>" +
                "         </xmlList>" +
                "      </childContainer>" +
                "   </someInnerList>" +
                "  </someList>" +
                "</validation>                                                 ";
        editConfig(requestXml);

        //verify response has the changes
        response = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <ctr:CrossTest xmlns:ctr=\"urn:org:bbf:yang:test:cross:tree:reference\"/>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
                + "   <validation:someList>"
                + "    <validation:someInnerList>"
                + "     <validation:someKey>skey1</validation:someKey>"
                + "    </validation:someInnerList>"
                + "    <validation:someKey>key1</validation:someKey>"
                + "   </validation:someList>"
                + "  </validation:validation>"
                + " </data>"
                + "</rpc-reply>"
        ;
        verifyGet(m_server, m_clientInfo, response);

    }

    @Test
    public void testDeleteList() throws Exception {
        // create two inner lists of same type, another list(xmlList2) referencing one of the two other lists
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "   <someList>" +
                "    <someKey>key1</someKey>" +
                "    <someInnerList>" +
                "      <someKey>skey1</someKey>" +
                "      <childContainer>" +
                "         <xmlList>" +
                "           <xmlListLeaf>10</xmlListLeaf>" +
                "           <type>20</type>" +
                "         </xmlList>" +
                "         <xmlList>" +
                "           <xmlListLeaf>11</xmlListLeaf>" +
                "           <type>20</type>" +
                "         </xmlList>" +
                "         <xmlList2>" +
                "           <xmlListLeaf>15</xmlListLeaf>" +
                "           <type>10</type>" +
                "         </xmlList2>" +
                "      </childContainer>" +
                "   </someInnerList>" +
                "  </someList>" +
                "</validation>                                                 ";
        editConfig(requestXml);

        //verify response has the changes
        String response = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <ctr:CrossTest xmlns:ctr=\"urn:org:bbf:yang:test:cross:tree:reference\"/>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
                + "   <validation:someList>"
                + "    <validation:someInnerList>"
                + "     <validation:childContainer>"
                + "       <validation:xmlList>"
                + "        <validation:xmlListLeaf>10</validation:xmlListLeaf>"
                + "        <validation:type>20</validation:type>"
                + "       </validation:xmlList>"
                + "       <validation:xmlList>"
                + "        <validation:type>20</validation:type>"
                + "        <validation:xmlListLeaf>11</validation:xmlListLeaf>"
                + "       </validation:xmlList>"
                + "       <validation:xmlList2>"
                + "        <validation:type>10</validation:type>"
                + "        <validation:xmlListLeaf>15</validation:xmlListLeaf>"
                + "       </validation:xmlList2>"
                + "      </validation:childContainer>"
                + "     <validation:someKey>skey1</validation:someKey>"
                + "    </validation:someInnerList>"
                + "    <validation:someKey>key1</validation:someKey>"
                + "   </validation:someList>"
                + "  </validation:validation>"
                + " </data>"
                + "</rpc-reply>";
        verifyGet(m_server, m_clientInfo, response);

        //add another xmlList2 with leafLists
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "   <someList>" +
                "    <someKey>key1</someKey>" +
                "    <someInnerList>" +
                "      <someKey>skey1</someKey>" +
                "      <childContainer>" +
                "         <xmlList2>" +
                "           <xmlListLeaf>11</xmlListLeaf>" +
                "           <leafList>a</leafList>" +
                "           <leafList>b</leafList>" +
                "           <leafList>c</leafList>" +
                "           <leafList>d</leafList>" +
                "         </xmlList2>" +
                "      </childContainer>" +
                "   </someInnerList>" +
                "  </someList>" +
                "</validation>                                                 ";
        editConfig(requestXml);

        //verify response has the changes
        response = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <ctr:CrossTest xmlns:ctr=\"urn:org:bbf:yang:test:cross:tree:reference\"/>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
                + "   <validation:someList>"
                + "    <validation:someInnerList>"
                + "     <validation:childContainer>"
                + "       <validation:xmlList>"
                + "        <validation:xmlListLeaf>10</validation:xmlListLeaf>"
                + "        <validation:type>20</validation:type>"
                + "       </validation:xmlList>"
                + "       <validation:xmlList>"
                + "        <validation:type>20</validation:type>"
                + "        <validation:xmlListLeaf>11</validation:xmlListLeaf>"
                + "       </validation:xmlList>"
                + "       <validation:xmlList2>"
                + "        <validation:type>10</validation:type>"
                + "        <validation:xmlListLeaf>15</validation:xmlListLeaf>"
                + "       </validation:xmlList2>"
                + "       <validation:xmlList2>"
                + "        <validation:xmlListLeaf>11</validation:xmlListLeaf>"
                + "        <validation:leafList>a</validation:leafList>"
                + "        <validation:leafList>b</validation:leafList>"
                + "        <validation:leafList>c</validation:leafList>"
                + "        <validation:leafList>d</validation:leafList>"
                + "       </validation:xmlList2>"
                + "      </validation:childContainer>"
                + "     <validation:someKey>skey1</validation:someKey>"
                + "    </validation:someInnerList>"
                + "    <validation:someKey>key1</validation:someKey>"
                + "   </validation:someList>"
                + "  </validation:validation>"
                + " </data>"
                + "</rpc-reply>"
        ;
        verifyGet(m_server, m_clientInfo, response);

        //add another xmlList2 with leafLists
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "   <someList>" +
                "    <someKey>key1</someKey>" +
                "    <someInnerList>" +
                "      <someKey>skey1</someKey>" +
                "      <childContainer>" +
                "         <xmlList2>" +
                "           <xmlListLeaf>11</xmlListLeaf>" +
                "           <leafList>e</leafList>" +
                "         </xmlList2>" +
                "      </childContainer>" +
                "   </someInnerList>" +
                "  </someList>" +
                "</validation>                                                 ";
        m_persistenceManagerUtil.getEntityDataStoreManager().beginTransaction();
        NetConfResponse ncResponse = editConfigAsFalse(requestXml);
        m_persistenceManagerUtil.getEntityDataStoreManager().rollbackTransaction();
        assertEquals("Reached max-elements 4, cannot add more child leafList.", ncResponse.getErrors().get(0)
                .getErrorMessage());
        assertEquals("/validation:validation/validation:someList[validation:someKey=key1]/validation:someInnerList" +
                        "[validation:someKey=skey1]/validation:childContainer/validation:xmlList2[validation" +
                        ":xmlListLeaf=11]/validation:leafList",
                ncResponse.getErrors().get(0).getErrorPath());
        verifyGet(m_server, m_clientInfo, response);

        //delete three leafRefs results in error
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\" " +
                "xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                "   <someList>" +
                "    <someKey>key1</someKey>" +
                "    <someInnerList>" +
                "      <someKey>skey1</someKey>" +
                "      <childContainer>" +
                "         <xmlList2>" +
                "           <xmlListLeaf>11</xmlListLeaf>" +
                "           <leafList xc:operation=\"delete\">a</leafList>" +
                "           <leafList xc:operation=\"delete\">b</leafList>" +
                "           <leafList xc:operation=\"delete\">c</leafList>" +
                "         </xmlList2>" +
                "      </childContainer>" +
                "   </someInnerList>" +
                "  </someList>" +
                "</validation>                                                 ";
        m_persistenceManagerUtil.getEntityDataStoreManager().beginTransaction();
        ncResponse = editConfigAsFalse(requestXml);
        m_persistenceManagerUtil.getEntityDataStoreManager().rollbackTransaction();
        assertEquals("Reached min-elements 2, cannot delete more child leafList.", ncResponse.getErrors().get(0)
                .getErrorMessage());
        assertEquals("/validation:validation/validation:someList[validation:someKey=key1]/validation:someInnerList" +
                        "[validation:someKey=skey1]/validation:childContainer/validation:xmlList2[validation" +
                        ":xmlListLeaf=11]/validation:leafList",
                ncResponse.getErrors().get(0).getErrorPath());

        // delete one leaflist
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\" " +
                "xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                "   <someList>" +
                "    <someKey>key1</someKey>" +
                "    <someInnerList>" +
                "      <someKey>skey1</someKey>" +
                "      <childContainer>" +
                "         <xmlList2>" +
                "           <xmlListLeaf>11</xmlListLeaf>" +
                "           <leafList xc:operation=\"delete\">a</leafList>" +
                "         </xmlList2>" +
                "      </childContainer>" +
                "   </someInnerList>" +
                "  </someList>" +
                "</validation>                                                 ";

        editConfig(requestXml);

        //verify response has the changes
        response = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <ctr:CrossTest xmlns:ctr=\"urn:org:bbf:yang:test:cross:tree:reference\"/>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
                + "   <validation:someList>"
                + "    <validation:someInnerList>"
                + "     <validation:childContainer>"
                + "       <validation:xmlList>"
                + "        <validation:xmlListLeaf>10</validation:xmlListLeaf>"
                + "        <validation:type>20</validation:type>"
                + "       </validation:xmlList>"
                + "       <validation:xmlList>"
                + "        <validation:type>20</validation:type>"
                + "        <validation:xmlListLeaf>11</validation:xmlListLeaf>"
                + "       </validation:xmlList>"
                + "       <validation:xmlList2>"
                + "        <validation:type>10</validation:type>"
                + "        <validation:xmlListLeaf>15</validation:xmlListLeaf>"
                + "       </validation:xmlList2>"
                + "       <validation:xmlList2>"
                + "        <validation:xmlListLeaf>11</validation:xmlListLeaf>"
                + "        <validation:leafList>b</validation:leafList>"
                + "        <validation:leafList>c</validation:leafList>"
                + "        <validation:leafList>d</validation:leafList>"
                + "       </validation:xmlList2>"
                + "      </validation:childContainer>"
                + "     <validation:someKey>skey1</validation:someKey>"
                + "    </validation:someInnerList>"
                + "    <validation:someKey>key1</validation:someKey>"
                + "   </validation:someList>"
                + "  </validation:validation>"
                + " </data>"
                + "</rpc-reply>"
        ;
        verifyGet(m_server, m_clientInfo, response);

        // delete all leaflist
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\" " +
                "xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                "   <someList>" +
                "    <someKey>key1</someKey>" +
                "    <someInnerList>" +
                "      <someKey>skey1</someKey>" +
                "      <childContainer>" +
                "         <xmlList2  xc:operation=\"delete\">" +
                "           <xmlListLeaf>11</xmlListLeaf>" +
                "           <leafList>b</leafList>" +
                "           <leafList>c</leafList>" +
                "           <leafList>d</leafList>" +
                "         </xmlList2>" +
                "      </childContainer>" +
                "   </someInnerList>" +
                "  </someList>" +
                "</validation>                                                 ";

        editConfig(requestXml);

        //verify response has the changes
        response = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <ctr:CrossTest xmlns:ctr=\"urn:org:bbf:yang:test:cross:tree:reference\"/>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
                + "   <validation:someList>"
                + "    <validation:someInnerList>"
                + "     <validation:childContainer>"
                + "       <validation:xmlList>"
                + "        <validation:xmlListLeaf>10</validation:xmlListLeaf>"
                + "        <validation:type>20</validation:type>"
                + "       </validation:xmlList>"
                + "       <validation:xmlList>"
                + "        <validation:type>20</validation:type>"
                + "        <validation:xmlListLeaf>11</validation:xmlListLeaf>"
                + "       </validation:xmlList>"
                + "       <validation:xmlList2>"
                + "        <validation:type>10</validation:type>"
                + "        <validation:xmlListLeaf>15</validation:xmlListLeaf>"
                + "       </validation:xmlList2>"
                + "      </validation:childContainer>"
                + "     <validation:someKey>skey1</validation:someKey>"
                + "    </validation:someInnerList>"
                + "    <validation:someKey>key1</validation:someKey>"
                + "   </validation:someList>"
                + "  </validation:validation>"
                + " </data>"
                + "</rpc-reply>"
        ;
        verifyGet(m_server, m_clientInfo, response);

        // delete the leaf tpye=20 and the xmlList2 referring it and the xmlList[11]
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\" " +
                "xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                "   <someList>" +
                "    <someKey>key1</someKey>" +
                "    <someInnerList>" +
                "      <someKey>skey1</someKey>" +
                "      <childContainer>" +
                "         <xmlList>" +
                "           <xmlListLeaf>10</xmlListLeaf>" +
                "           <type xc:operation=\"delete\">20</type>" +
                "         </xmlList>" +
                "         <xmlList xc:operation=\"delete\">" +
                "           <xmlListLeaf>11</xmlListLeaf>" +
                "           <type>20</type>" +
                "         </xmlList>" +
                "         <xmlList2 xc:operation=\"delete\">" +
                "           <xmlListLeaf>15</xmlListLeaf>" +
                "           <type>10</type>" +
                "         </xmlList2>" +
                "      </childContainer>" +
                "   </someInnerList>" +
                "  </someList>" +
                "</validation>                                                 ";
        editConfig(requestXml);

        // verify the datastore reflects the changes.
        response = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <ctr:CrossTest xmlns:ctr=\"urn:org:bbf:yang:test:cross:tree:reference\"/>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
                + "   <validation:someList>"
                + "    <validation:someInnerList>"
                + "     <validation:childContainer>"
                + "       <validation:xmlList>"
                + "        <validation:xmlListLeaf>10</validation:xmlListLeaf>"
                + "       </validation:xmlList>"
                + "      </validation:childContainer>"
                + "     <validation:someKey>skey1</validation:someKey>"
                + "    </validation:someInnerList>"
                + "    <validation:someKey>key1</validation:someKey>"
                + "   </validation:someList>"
                + "  </validation:validation>"
                + " </data>"
                + "</rpc-reply>"
        ;
        verifyGet(m_server, m_clientInfo, response);

        //delete someInnerList[skey1]
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\" " +
                "xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                "   <someList>" +
                "    <someKey>key1</someKey>" +
                "    <someInnerList xc:operation=\"delete\">" +
                "      <someKey>skey1</someKey>" +
                "      <childContainer>" +
                "         <xmlList>" +
                "           <xmlListLeaf>10</xmlListLeaf>" +
                "         </xmlList>" +
                "      </childContainer>" +
                "   </someInnerList>" +
                "  </someList>" +
                "</validation>                                                 ";
        editConfig(requestXml);

        // verify the datastore reflects the changes.
        response = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <ctr:CrossTest xmlns:ctr=\"urn:org:bbf:yang:test:cross:tree:reference\"/>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
                + "   <validation:someList>"
                + "    <validation:someKey>key1</validation:someKey>"
                + "   </validation:someList>"
                + "  </validation:validation>"
                + " </data>"
                + "</rpc-reply>"
        ;
        verifyGet(m_server, m_clientInfo, response);
    }

    @Test
    public void testInnerList() throws Exception {
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "  <xml-subtree>" +
                "   <multiContainer> " +
                "     <level1>" +
                "       <level2>" +
                "        <level3>" +
                "          <list1> " +
                "             <key1>key1</key1>" +
                "          </list1>" +
                "          <list1>" +
                "             <key1>key2</key1>" +
                "          </list1>" +
                "        </level3>" +
                "       </level2>" +
                "     </level1>" +
                "   </multiContainer>" +
                "  </xml-subtree>" +
                "</validation>                                                 ";
        editConfig(requestXml1);

        String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\" " +
                "xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                "  <xml-subtree>" +
                "   <multiContainer> " +
                "     <level1>" +
                "       <level2>" +
                "        <level3 xc:operation=\"replace\">" +
                "          <list1 xc:operation=\"replace\"> " +
                "             <key1>key1</key1>" +
                "          </list1>" +
                "          <list1 xc:operation=\"replace\">" +
                "             <key1>key2</key1>" +
                "          </list1>" +
                "        </level3>" +
                "       </level2>" +
                "     </level1>" +
                "   </multiContainer>" +
                "  </xml-subtree>" +
                "</validation>                                                 ";
        editConfig(requestXml2);
    }

    @Test
    public void testLeafRefAlongWithCreate() throws Exception {
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "   <someList>" +
                "    <someKey>key1</someKey>" +
                "    <someInnerList>" +
                "      <someKey>skey1</someKey>" +
                "      <childContainer>" +
                "        <multiContainer> " +
                "          <multiContainerList>" +
                "            <key1>11</key1>" +
                "          </multiContainerList>" +
                "        </multiContainer>" +
                "      </childContainer>" +
                "   </someInnerList>" +
                "  </someList>" +
                "</validation>";
        editConfig(requestXml1);

        String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "   <someList>" +
                "    <someKey>key1</someKey>" +
                "    <someInnerList>" +
                "      <someKey>skey2</someKey>" +
                "      <childContainer>" +
                "        <multiContainer> " +
                "          <otherContainerList>" +
                "            <key1>11</key1>" +
                "            <ref>12</ref>" +
                "          </otherContainerList>" +
                "          <multiContainerList>" +
                "            <key1>12</key1>" +
                "            <ref>11</ref>" +
                "          </multiContainerList>" +
                "        </multiContainer>" +
                "      </childContainer>" +
                "   </someInnerList>" +
                "  </someList>" +
                "</validation>";
        editConfig(requestXml2);
    }

    @Test
    public void testMultiLevelMultiEntityXmlTree() throws Exception {
        // create two 'someList' with key as 'key2' and 'key1'
        // create two 'someInnerList' with key as 'skey1' and 'skey1'.
        // create xml subtree for the path someList[key1]/someInnerList[skey1]
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "   <someList>" +
                "    <someKey>key2</someKey>" +
                "    <someInnerList>" +
                "     <someKey>skey1</someKey>" +
                "    </someInnerList>" +
                "   </someList>" +
                "   <someList>" +
                "    <someKey>key1</someKey>" +
                "    <someInnerList>" +
                "      <someKey>skey1</someKey>" +
                "      <childContainer>" +
                "        <multiContainer> " +
                "         <level1>" +
                "          <level2>" +
                "           <level3>" +
                "             <list1>" +
                "              <key1>11</key1>" +
                "              <condition>15</condition>" +
                "              <leafList>a</leafList>" +
                "              <leafList>b</leafList>" +
                "             </list1>" +
                "             <list1>" +
                "              <key1>12</key1>" +
                "             </list1>" +
                "           </level3>" +
                "          </level2>" +
                "         </level1>" +
                "        </multiContainer>" +
                "      </childContainer>" +
                "   </someInnerList>" +
                "  </someList>" +
                "</validation>";
        editConfig(requestXml1);

        // add 1 leaf list for someList[key1]/someInnerList[skey1]
        String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "   <someList>" +
                "    <someKey>key1</someKey>" +
                "    <someInnerList>" +
                "      <someKey>skey1</someKey>" +
                "      <childContainer>" +
                "        <multiContainer> " +
                "         <level1>" +
                "          <level2>" +
                "           <level3>" +
                "             <list1>" +
                "              <key1>11</key1>" +
                "              <leafList>c</leafList>" +
                "             </list1>" +
                "           </level3>" +
                "          </level2>" +
                "         </level1>" +
                "        </multiContainer>" +
                "      </childContainer>" +
                "   </someInnerList>" +
                "  </someList>" +
                "</validation>";
        editConfig(requestXml2);

        // verify the output for above changes.
        String verifyXml =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "<data> "
                        + "  <ctr:CrossTest xmlns:ctr=\"urn:org:bbf:yang:test:cross:tree:reference\"/>"
                        + " <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\"> "
                        + "  <validation:someList>"
                        + "  <validation:someKey>key1</validation:someKey>"
                        + "   <validation:someInnerList>"
                        + "    <validation:someKey>skey1</validation:someKey>"
                        + "    <validation:childContainer>"
                        + "     <validation:multiContainer>"
                        + "      <validation:level1>"
                        + "       <validation:level2>"
                        + "        <validation:level3>"
                        + "         <validation:list1>"
                        + "          <validation:key1>11</validation:key1>"
                        + "          <validation:condition>15</validation:condition>"
                        + "          <validation:leafList>a</validation:leafList>"
                        + "          <validation:leafList>b</validation:leafList>"
                        + "          <validation:leafList>c</validation:leafList>"
                        + "          </validation:list1>"
                        + "          <validation:list1>"
                        + "           <validation:key1>12</validation:key1>"
                        + "          </validation:list1>"
                        + "         </validation:level3>"
                        + "        </validation:level2>"
                        + "       </validation:level1>"
                        + "      </validation:multiContainer>"
                        + "     </validation:childContainer>"
                        + "   </validation:someInnerList>"
                        + " </validation:someList>"
                        + "  <validation:someList>"
                        + "  <validation:someKey>key2</validation:someKey>"
                        + "   <validation:someInnerList>"
                        + "    <validation:someKey>skey1</validation:someKey>"
                        + "   </validation:someInnerList>"
                        + " </validation:someList>"
                        + "</validation:validation>"
                        + "</data>"
                        + "</rpc-reply>";
        verifyGet(m_server, m_clientInfo, verifyXml);

        // change the value of leaf condition to 9 for someList[key1]/someInnerList[skey1]. leaf list must be removed
        // now
        String requestXml3 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "   <someList>" +
                "    <someKey>key1</someKey>" +
                "    <someInnerList>" +
                "      <someKey>skey1</someKey>" +
                "      <childContainer>" +
                "        <multiContainer> " +
                "         <level1>" +
                "          <level2>" +
                "           <level3>" +
                "             <list1>" +
                "              <key1>11</key1>" +
                "              <condition>9</condition>" +
                "             </list1>" +
                "           </level3>" +
                "          </level2>" +
                "         </level1>" +
                "        </multiContainer>" +
                "      </childContainer>" +
                "   </someInnerList>" +
                "  </someList>" +
                "</validation>";
        editConfig(requestXml3);

        // verify the output for above changes. no leaf list and condition set to 9
        verifyXml =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "<data> "
                        + "  <ctr:CrossTest xmlns:ctr=\"urn:org:bbf:yang:test:cross:tree:reference\"/>"
                        + " <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\"> "
                        + "  <validation:someList>"
                        + "  <validation:someKey>key1</validation:someKey>"
                        + "   <validation:someInnerList>"
                        + "    <validation:someKey>skey1</validation:someKey>"
                        + "    <validation:childContainer>"
                        + "     <validation:multiContainer>"
                        + "      <validation:level1>"
                        + "       <validation:level2>"
                        + "        <validation:level3>"
                        + "         <validation:list1>"
                        + "          <validation:key1>11</validation:key1>"
                        + "          <validation:condition>9</validation:condition>"
                        + "          </validation:list1>"
                        + "          <validation:list1>"
                        + "           <validation:key1>12</validation:key1>"
                        + "          </validation:list1>"
                        + "         </validation:level3>"
                        + "        </validation:level2>"
                        + "       </validation:level1>"
                        + "      </validation:multiContainer>"
                        + "     </validation:childContainer>"
                        + "   </validation:someInnerList>"
                        + " </validation:someList>"
                        + "  <validation:someList>"
                        + "  <validation:someKey>key2</validation:someKey>"
                        + "   <validation:someInnerList>"
                        + "    <validation:someKey>skey1</validation:someKey>"
                        + "   </validation:someInnerList>"
                        + " </validation:someList>"
                        + "</validation:validation>"
                        + "</data>"
                        + "</rpc-reply>"
        ;
        verifyGet(m_server, m_clientInfo, verifyXml);
    }

    @Test
    public void testMultiLevelEntityXmlTree() throws Exception {
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "   <someList>" +
                "    <someKey>key1</someKey>" +
                "    <someInnerList>" +
                "      <someKey>skey1</someKey>" +
                "      <childContainer>" +
                "        <multiContainer> " +
                "         <level1>" +
                "          <level2>" +
                "           <level3>" +
                "             <list1>" +
                "              <key1>11</key1>" +
                "              <condition>15</condition>" +
                "              <leafList>a</leafList>" +
                "              <leafList>b</leafList>" +
                "             </list1>" +
                "             <list1>" +
                "              <key1>12</key1>" +
                "             </list1>" +
                "           </level3>" +
                "          </level2>" +
                "         </level1>" +
                "        </multiContainer>" +
                "      </childContainer>" +
                "   </someInnerList>" +
                "  </someList>" +
                "</validation>";
        editConfig(requestXml1);

        String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "   <someList>" +
                "    <someKey>key1</someKey>" +
                "    <someInnerList>" +
                "      <someKey>skey1</someKey>" +
                "      <childContainer>" +
                "        <multiContainer> " +
                "         <level1>" +
                "          <level2>" +
                "           <level3>" +
                "             <list1>" +
                "              <key1>11</key1>" +
                "              <leafList>c</leafList>" +
                "             </list1>" +
                "           </level3>" +
                "          </level2>" +
                "         </level1>" +
                "        </multiContainer>" +
                "      </childContainer>" +
                "   </someInnerList>" +
                "  </someList>" +
                "</validation>";
        editConfig(requestXml2);

        String verifyXml =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "<data> "
                        + "  <ctr:CrossTest xmlns:ctr=\"urn:org:bbf:yang:test:cross:tree:reference\"/>"
                        + " <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\"> "
                        + "  <validation:someList>"
                        + "  <validation:someKey>key1</validation:someKey>"
                        + "   <validation:someInnerList>"
                        + "    <validation:someKey>skey1</validation:someKey>"
                        + "    <validation:childContainer>"
                        + "     <validation:multiContainer>"
                        + "      <validation:level1>"
                        + "       <validation:level2>"
                        + "        <validation:level3>"
                        + "         <validation:list1>"
                        + "          <validation:key1>11</validation:key1>"
                        + "          <validation:condition>15</validation:condition>"
                        + "          <validation:leafList>a</validation:leafList>"
                        + "          <validation:leafList>b</validation:leafList>"
                        + "          <validation:leafList>c</validation:leafList>"
                        + "          </validation:list1>"
                        + "          <validation:list1>"
                        + "           <validation:key1>12</validation:key1>"
                        + "          </validation:list1>"
                        + "         </validation:level3>"
                        + "        </validation:level2>"
                        + "       </validation:level1>"
                        + "      </validation:multiContainer>"
                        + "     </validation:childContainer>"
                        + "   </validation:someInnerList>"
                        + " </validation:someList>"
                        + "</validation:validation>"
                        + "</data>"
                        + "</rpc-reply>";
        verifyGet(m_server, m_clientInfo, verifyXml);

        String requestXml3 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "   <someList>" +
                "    <someKey>key1</someKey>" +
                "    <someInnerList>" +
                "      <someKey>skey1</someKey>" +
                "      <childContainer>" +
                "        <multiContainer> " +
                "         <level1>" +
                "          <level2>" +
                "           <level3>" +
                "             <list1>" +
                "              <key1>11</key1>" +
                "              <condition>9</condition>" +
                "              <ref>11</ref>" +
                "             </list1>" +
                "           </level3>" +
                "          </level2>" +
                "         </level1>" +
                "        </multiContainer>" +
                "      </childContainer>" +
                "   </someInnerList>" +
                "  </someList>" +
                "</validation>";
        editConfig(requestXml3);

        verifyXml =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "<data> "
                        + "  <ctr:CrossTest xmlns:ctr=\"urn:org:bbf:yang:test:cross:tree:reference\"/>"
                        + " <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\"> "
                        + "  <validation:someList>"
                        + "  <validation:someKey>key1</validation:someKey>"
                        + "   <validation:someInnerList>"
                        + "    <validation:someKey>skey1</validation:someKey>"
                        + "    <validation:childContainer>"
                        + "     <validation:multiContainer>"
                        + "      <validation:level1>"
                        + "       <validation:level2>"
                        + "        <validation:level3>"
                        + "         <validation:list1>"
                        + "          <validation:key1>11</validation:key1>"
                        + "          <validation:condition>9</validation:condition>"
                        + "          <validation:ref>11</validation:ref>"
                        + "          </validation:list1>"
                        + "          <validation:list1>"
                        + "           <validation:key1>12</validation:key1>"
                        + "          </validation:list1>"
                        + "         </validation:level3>"
                        + "        </validation:level2>"
                        + "       </validation:level1>"
                        + "      </validation:multiContainer>"
                        + "     </validation:childContainer>"
                        + "   </validation:someInnerList>"
                        + " </validation:someList>"
                        + "</validation:validation>"
                        + "</data>"
                        + "</rpc-reply>"
        ;
        verifyGet(m_server, m_clientInfo, verifyXml);
    }

    @Test
    public void testContainerWithWhen() throws Exception {
        AddDefaultDataInterceptor addDefaultDataInterceptor = new AddDefaultDataInterceptor
                (m_modelNodeHelperRegistry, getSchemaRegistry(), m_expValidator);
        addDefaultDataInterceptor.init();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "  <xml-subtree>" +
                "    <leaf1>HELLO</leaf1>" +
                "  </xml-subtree>" +
                "</validation>                                                 ";
        editConfig(requestXml1);

        String response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + " <data>"
                        + "  <ctr:CrossTest xmlns:ctr=\"urn:org:bbf:yang:test:cross:tree:reference\"/>"
                        + " <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
                        + "  <validation:xml-subtree>"
                        + "   <validation:container1/>"
                        + "   <validation:container2>"
                        + "    <validation:leaf1>0</validation:leaf1>"
                        + "   </validation:container2>"
                        + "   <validation:leaf1>HELLO</validation:leaf1>"
                        + "   <validation:multiContainer>"
                        + "    <validation:level1>"
                        + "     <validation:level2/>"
                        + "    </validation:level1>"
                        + "   </validation:multiContainer>"
                        + "  </validation:xml-subtree>"
                        + " </validation:validation>"
                        + "</data>"
                        + "</rpc-reply>";
        verifyGet(m_server, getClientInfo(), response);
        addDefaultDataInterceptor.destroy();
    }

    @Test
    public void testMultiLeafList() throws SAXException, IOException {
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "  <xml-subtree>" +
                "    <leaf1>hello</leaf1>" +
                "    <list1>" +
                "      <someKey>key</someKey>" +
                "      <listLeaf>1</listLeaf>" +
                "      <listLeaf>2</listLeaf>" +
                "    </list1>" +
                "  </xml-subtree>" +
                "</validation>                                                 ";
        editConfig(requestXml1);

        String response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + " <data>"
                        + "  <ctr:CrossTest xmlns:ctr=\"urn:org:bbf:yang:test:cross:tree:reference\"/>"
                        + " <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
                        + "  <validation:xml-subtree>"
                        + "   <validation:leaf1>hello</validation:leaf1>"
                        + "   <validation:list1>"
                        + "    <validation:listLeaf>1</validation:listLeaf>"
                        + "    <validation:listLeaf>2</validation:listLeaf>"
                        + "    <validation:someKey>key</validation:someKey>"
                        + "   </validation:list1>"
                        + "  </validation:xml-subtree>"
                        + " </validation:validation>"
                        + "</data>"
                        + "</rpc-reply>";
        verifyGet(m_server, getClientInfo(), response);

        String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "  <xml-subtree>" +
                "    <leaf1>hello1</leaf1>" +
                "  </xml-subtree>" +
                "</validation>                                                 ";
        editConfig(requestXml2);

        String requestXml3 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "  <xml-subtree>" +
                "    <leaf1>hello1</leaf1>" +
                "    <container1>" +
                "      <list1>" +
                "        <key1>9</key1>" +
                "      </list1>" +
                "    </container1>" +
                "  </xml-subtree>" +
                "</validation>                                                 ";
        editConfig(requestXml3);

        String requestXml4 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "  <xml-subtree>" +
                "    <container1>" +
                "      <list1>" +
                "        <key1>9</key1>" +
                "        <choiceLeaf1>10</choiceLeaf1>" +
                "        <choice-leafList>11</choice-leafList>" +
                "        <choice-leafList>12</choice-leafList>" +
                "      </list1>" +
                "    </container1>" +
                "  </xml-subtree>" +
                "</validation>                                                 ";
        editConfig(requestXml4);

        String expectedOutput =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\"> "
                        + "<data>"
                        + "  <ctr:CrossTest xmlns:ctr=\"urn:org:bbf:yang:test:cross:tree:reference\"/>"
                        + "  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
                        + "    <validation:xml-subtree>"
                        + "      <validation:leaf1>hello1</validation:leaf1>"
                        + "      <validation:container1>"
                        + "        <validation:list1>"
                        + "            <validation:key1>9</validation:key1>"
                        + "            <validation:choiceLeaf1>10</validation:choiceLeaf1>"
                        + "            <validation:choice-leafList>11</validation:choice-leafList>"
                        + "            <validation:choice-leafList>12</validation:choice-leafList>"
                        + "        </validation:list1>"
                        + "      </validation:container1>"
                        + "    </validation:xml-subtree>"
                        + "  </validation:validation>"
                        + "</data>"
                        + "</rpc-reply>";
        verifyGet(m_server, m_clientInfo, expectedOutput);
    }

    @Test
    public void testMultiChildFilters() throws Exception {
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "   <someList>" +
                "    <someKey>key1</someKey>" +
                "    <someInnerList>" +
                "      <someKey>skey1</someKey>" +
                "      <childContainer>" +
                "         <xmlList>" +
                "           <xmlListLeaf>10</xmlListLeaf>" +
                "           <type>20</type>" +
                "         </xmlList>" +
                "      </childContainer>" +
                "   </someInnerList>" +
                "  </someList>" +
                "</validation>                                                 ";
        editConfig(requestXml);

        //filter request with state alone
        String filterGet =
                " <validation xmlns=\"urn:org:bbf:pma:validation\">"
                        + " <someList>"
                        + "  <someKey>key1</someKey>"
                        + "  <someInnerList>"
                        + "   <someKey>skey1</someKey>"
                        + "   <childContainer>"
                        + "    <stateCheck>"
                        + "     <leaf1>10</leaf1>"
                        + "    </stateCheck>"
                        + "   </childContainer>"
                        + "  </someInnerList>"
                        + " </someList>"
                        + "</validation>";

        String response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + " <data/>"
                        + " </rpc-reply>";
        verifyGet(m_server, m_clientInfo, filterGet, response);

        //filter request with both state and config subtree
        filterGet =
                " <validation xmlns=\"urn:org:bbf:pma:validation\">"
                        + " <someList>"
                        + "  <someKey>key1</someKey>"
                        + "  <someInnerList>"
                        + "   <someKey>skey1</someKey>"
                        + "   <childContainer>"
                        + "    <xmlList>"
                        + "     <xmlListLeaf>10</xmlListLeaf>"
                        + "    </xmlList>"
                        + "    <stateCheck>"
                        + "     <leaf1>10</leaf1>"
                        + "    </stateCheck>"
                        + "   </childContainer>"
                        + "  </someInnerList>"
                        + " </someList>"
                        + "</validation>";

        //verify response has the changes
        response = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
                + "   <validation:someList>"
                + "    <validation:someInnerList>"
                + "     <validation:childContainer>"
                + "       <validation:xmlList>"
                + "        <validation:xmlListLeaf>10</validation:xmlListLeaf>"
                + "        <validation:type>20</validation:type>"
                + "       </validation:xmlList>"
                + "      </validation:childContainer>"
                + "     <validation:someKey>skey1</validation:someKey>"
                + "    </validation:someInnerList>"
                + "    <validation:someKey>key1</validation:someKey>"
                + "   </validation:someList>"
                + "  </validation:validation>"
                + " </data>"
                + "</rpc-reply>"
        ;
        verifyGet(m_server, m_clientInfo, filterGet, response);

        //filter request with config alone
        filterGet =
                " <validation xmlns=\"urn:org:bbf:pma:validation\">"
                        + " <someList>"
                        + "  <someKey>key1</someKey>"
                        + "  <someInnerList>"
                        + "   <someKey>skey1</someKey>"
                        + "   <childContainer>"
                        + "    <xmlList>"
                        + "     <xmlListLeaf>10</xmlListLeaf>"
                        + "    </xmlList>"
                        + "   </childContainer>"
                        + "  </someInnerList>"
                        + " </someList>"
                        + "</validation>";
        verifyGet(m_server, m_clientInfo, filterGet, response);

    }


    @Test
    public void testCrossRefTree() throws Exception {
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "  <xml-subtree>" +
                "    <leaf1>hello</leaf1>" +
                "    <list1>" +
                "      <someKey>key</someKey>" +
                "      <listLeaf>1</listLeaf>" +
                "      <listLeaf>2</listLeaf>" +
                "    </list1>" +
                "  </xml-subtree>" +
                "</validation>                                                 ";
        editConfig(requestXml1);

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<CrossTest xmlns=\"urn:org:bbf:yang:test:cross:tree:reference\">" +
                " <xml-subtree>" +
                " <list1>" +
                "  <key>key</key>" +
                "  <leaf1>2</leaf1>" +
                " </list1>" +
                " </xml-subtree>" +
                "</CrossTest>"
        ;
        editConfig(requestXml1);

    }

    //@Test
    public void testPerformance() throws Exception {
        String requestXml1 =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        "<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                        "   <someList>" +
                        "    <someKey>key1</someKey>" +
                        "    <someInnerList>" +
                        "      <someKey>skey1</someKey>" +
                        "      <childContainer>";
        for (int i = 0; i < 100; i++) {
            requestXml1 = requestXml1 + "<xmlList><xmlListLeaf>" + i + "</xmlListLeaf><type>" + ((i + 1) * 10) +
                    "</type></xmlList>";
            requestXml1 = requestXml1 + "<xmlList2><xmlListLeaf>" + i + "</xmlListLeaf><type>" + i +
                    "</type></xmlList2>";
        }

        requestXml1 = requestXml1 +
                "        <multiContainer> " +
                "         <level1>" +
                "          <level2>" +
                "           <level3>";

        for (int i = 0; i < 100; i++) {
            requestXml1 = requestXml1 + "<list1><key1>" + i + "</key1><condition>" + ((i + 2) * 10) + "</condition>"
                    + "<leafList>" + i + "</leafList><leafList>" + (i + 1) + "</leafList><ref>" + i + "</ref></list1>";
        }
        requestXml1 = requestXml1 +
                "           </level3>" +
                "          </level2>" +
                "         </level1>" +
                "        </multiContainer>" +
                "      </childContainer>" +
                "   </someInnerList>" +
                "  </someList>" +
                "</validation>"
        ;
        long startTime = System.currentTimeMillis();
        editConfig(m_server, m_clientInfo, requestXml1, true);
        long endTime = System.currentTimeMillis();

        assertTrue((endTime - startTime) < 5000);
    }

    @Test
    public void testListKeyName() throws Exception {
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "  <xml-subtree>" +
                "   <leaf1>hello</leaf1>" +
                "   <list1>" +
                "    <someKey>9</someKey>" +
                "    <listLeaf>11</listLeaf>" +
                "    <listLeaf>12</listLeaf>" +
                "   </list1>" +
                "   <list1>" +
                "    <someKey>10</someKey>" +
                "    <listLeaf>11</listLeaf>" +
                "    <listLeaf>12</listLeaf>" +
                "   </list1>" +
                "   <multiContainer>" +
                "    <listKeyName>10</listKeyName>" +
                "    <listKeyName1>10</listKeyName1>" +
                "   </multiContainer>" +
                "  </xml-subtree>" +
                "</validation>                                                 ";

        editConfig(m_server, m_clientInfo, requestXml1, true);
    }

    @Test
    public void testOrderByUser() throws Exception {
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\" xmlns:yang=\"urn:ietf:params:xml:ns:yang:1\">"
                + " <xml-subtree>"
                + "  <orderByUserLeaf yang:insert=\"first\">a</orderByUserLeaf>"
                + "  <orderByUserLeaf yang:insert=\"after\" yang:value=\"a\">c</orderByUserLeaf>"
                + " </xml-subtree>"
                + "</validation>";

        editConfig(m_server, m_clientInfo, requestXml1, true);

        String response = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <ctr:CrossTest xmlns:ctr=\"urn:org:bbf:yang:test:cross:tree:reference\"/>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
                + "   <validation:xml-subtree>"
                + "    <validation:orderByUserLeaf>a</validation:orderByUserLeaf>"
                + "    <validation:orderByUserLeaf>c</validation:orderByUserLeaf>"
                + "   </validation:xml-subtree>"
                + "  </validation:validation>"
                + " </data>"
                + "</rpc-reply>";
        verifyGet(m_server, m_clientInfo, response);

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\" xmlns:yang=\"urn:ietf:params:xml:ns:yang:1\">"
                + " <xml-subtree>"
                + "  <orderByUserLeaf yang:insert=\"last\">z</orderByUserLeaf>"
                + "  <orderByUserLeaf yang:insert=\"before\" yang:value=\"c\">b</orderByUserLeaf>"
                + " </xml-subtree>"
                + "</validation>"
        ;

        editConfig(m_server, m_clientInfo, requestXml1, true);

        response = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <ctr:CrossTest xmlns:ctr=\"urn:org:bbf:yang:test:cross:tree:reference\"/>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
                + "   <validation:xml-subtree>"
                + "    <validation:orderByUserLeaf>a</validation:orderByUserLeaf>"
                + "    <validation:orderByUserLeaf>b</validation:orderByUserLeaf>"
                + "    <validation:orderByUserLeaf>c</validation:orderByUserLeaf>"
                + "    <validation:orderByUserLeaf>z</validation:orderByUserLeaf>"
                + "   </validation:xml-subtree>"
                + "  </validation:validation>"
                + " </data>"
                + "</rpc-reply>"
        ;

    }

    @Test
    public void testOrderByUserListOnAnnotatedEntityInnerList() throws Exception {
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<validation xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" "
                + "xmlns=\"urn:org:bbf:pma:validation\" xmlns:yang=\"urn:ietf:params:xml:ns:yang:1\" " +
                "xc:operation='replace'>"
                + "  <someList>"
                + "   <someKey>key1</someKey>"
                + "   <someInnerList>"
                + "    <someKey>key1</someKey>"
                + "     <orderByUserList yang:insert=\"last\">"
                + "      <someKey>a</someKey>"
                + "     </orderByUserList>"
                + "     <orderByUserList yang:insert=\"after\" yang:key=\"[someKey='a']\">"
                + "      <someKey>b</someKey>"
                + "     </orderByUserList>"
                + "     <orderByUserList yang:insert=\"after\" yang:key=\"[someKey='b']\">"
                + "      <someKey>d</someKey>"
                + "     </orderByUserList>"
                + "     <orderByUserList yang:insert=\"after\" yang:key=\"[someKey='b']\">"
                + "      <someKey>c</someKey>"
                + "     </orderByUserList>"
                + "     <orderByUserList yang:insert=\"last\">"
                + "      <someKey>f</someKey>"
                + "     </orderByUserList>"
                + "     <orderByUserList yang:insert=\"before\" yang:key=\"[someKey='f']\">"
                + "      <someKey>e</someKey>"
                + "     </orderByUserList>"
                + "    </someInnerList>"
                + "   </someList>"
                + "</validation>";

        editConfig(m_server, m_clientInfo, requestXml1, true);
        String response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "  <data>"
                        + "   <ctr:CrossTest xmlns:ctr=\"urn:org:bbf:yang:test:cross:tree:reference\"/>"
                        + "   <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
                        + "    <validation:someList>"
                        + "     <validation:someInnerList>"
                        + "      <validation:orderByUserList>"
                        + "       <validation:someKey>a</validation:someKey>"
                        + "      </validation:orderByUserList>"
                        + "      <validation:orderByUserList>"
                        + "       <validation:someKey>b</validation:someKey>"
                        + "      </validation:orderByUserList>"
                        + "      <validation:orderByUserList>"
                        + "       <validation:someKey>c</validation:someKey>"
                        + "      </validation:orderByUserList>"
                        + "      <validation:orderByUserList>"
                        + "       <validation:someKey>d</validation:someKey>"
                        + "      </validation:orderByUserList>"
                        + "      <validation:orderByUserList>"
                        + "       <validation:someKey>e</validation:someKey>"
                        + "      </validation:orderByUserList>"
                        + "      <validation:orderByUserList>"
                        + "       <validation:someKey>f</validation:someKey>"
                        + "      </validation:orderByUserList>"
                        + "       <validation:someKey>key1</validation:someKey>"
                        + "      </validation:someInnerList>"
                        + "      <validation:someKey>key1</validation:someKey>"
                        + "     </validation:someList>"
                        + "    </validation:validation>"
                        + "   </data>"
                        + "  </rpc-reply>";
        verifyGet(m_server, m_clientInfo, response);
    }

    @Test
    public void testOrderByUserListOnAnnotatedEntity() throws Exception {
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<validation xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" "
                + "xmlns=\"urn:org:bbf:pma:validation\" xmlns:yang=\"urn:ietf:params:xml:ns:yang:1\" xc:operation='replace'>"
                + "  <orderByUserList yang:insert=\"first\">"
                + "   <someKey>a</someKey>"
                + "  </orderByUserList>"
                + "  <orderByUserList yang:insert=\"after\" yang:key=\"[someKey='a']\">"
                + "   <someKey>b</someKey>"
                + "  </orderByUserList>"
                + "  <orderByUserList yang:insert=\"after\" yang:key=\"[someKey='b']\">"
                + "   <someKey>c</someKey>"
                + "  </orderByUserList>"
                + "  <orderByUserList yang:insert=\"after\" yang:key=\"[someKey='c']\">"
                + "   <someKey>d</someKey>"
                + "  </orderByUserList>"
                + "  <orderByUserList yang:insert=\"last\">"
                + "   <someKey>f</someKey>"
                + "  </orderByUserList>"
                + "  <orderByUserList yang:insert=\"before\" yang:key=\"[someKey='f']\">"
                + "   <someKey>e</someKey>"
                + "  </orderByUserList>"
                + "</validation>";

        editConfig(m_server, m_clientInfo, requestXml1, true);

        String response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + " <data>"
                        + "  <ctr:CrossTest xmlns:ctr=\"urn:org:bbf:yang:test:cross:tree:reference\"/>"
                        + "  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
                        + "    <validation:orderByUserList>"
                        + "     <validation:someKey>a</validation:someKey>"
                        + "    </validation:orderByUserList>"
                        + "    <validation:orderByUserList>"
                        + "     <validation:someKey>b</validation:someKey>"
                        + "    </validation:orderByUserList>"
                        + "    <validation:orderByUserList>"
                        + "     <validation:someKey>c</validation:someKey>"
                        + "    </validation:orderByUserList>"
                        + "    <validation:orderByUserList>"
                        + "     <validation:someKey>d</validation:someKey>"
                        + "    </validation:orderByUserList>"
                        + "    <validation:orderByUserList>"
                        + "     <validation:someKey>e</validation:someKey>"
                        + "    </validation:orderByUserList>"
                        + "    <validation:orderByUserList>"
                        + "     <validation:someKey>f</validation:someKey>"
                        + "    </validation:orderByUserList>"
                        + "  </validation:validation>"
                        + " </data>"
                        + "</rpc-reply>";

        verifyGet(m_server, m_clientInfo, response);

    }

}
