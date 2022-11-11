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

import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorType;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;

@RunWith(RequestScopeJunitRunner.class)
public class RefineUnderUsesTest extends AbstractDataStoreValidatorTest {

    private static final QName REFINE_UNDER_USES_QNAME = QName.create("urn:org:bbf2:pma:refineUnderUses", "2020-03-20", "test-refine-under-uses");
    private static final QName DESCRIPTION_LEAF_QNAME = QName.create("urn:org:bbf2:pma:refineUnderUses", "2020-03-20", "description-leaf");
    private static final QName OVERRIDDEN_DESCRIPTION_QNAME = QName.create("urn:org:bbf2:pma:refineUnderUses", "2020-03-20", "overridden-description");
    private static final String REFINE_UNDER_USES_YANG = "/datastorevalidatortest/yangs/refine-under-uses.yang";
    private static final SchemaPath REFINE_UNDER_USES_SCHEMA_PATH = SchemaPath.create(true, REFINE_UNDER_USES_QNAME);
    private static final String REFINE_UNDER_USES_DEFAULT_XML = "/datastorevalidatortest/yangs/datastore-validator-refine-under-uses-default.xml";
    private static final SchemaPath DESCRIPTION_LEAF_SCHEMA_PATH = REFINE_UNDER_USES_SCHEMA_PATH.createChild(OVERRIDDEN_DESCRIPTION_QNAME).createChild(DESCRIPTION_LEAF_QNAME);

    @BeforeClass
    public static void initializeOnce() throws SchemaBuildException {
        List<YangTextSchemaSource> yangFiles = TestUtil.getByteSources(getYang());
        m_schemaRegistry = new SchemaRegistryImpl(yangFiles, Collections.EMPTY_SET, Collections.emptyMap(), new NoLockService());
        m_schemaRegistry.setName(SchemaRegistry.GLOBAL_SCHEMA_REGISTRY);

    }

    protected static List<String> getYang() {
        List<String> fileList = new ArrayList<String>();
        fileList.add(REFINE_UNDER_USES_YANG);
        return fileList;
    }

    @Override
    protected void addRootNodeHelpers() {
        addRootContainerNodeHelpers(REFINE_UNDER_USES_SCHEMA_PATH);
    }

    @Override
    protected String getXml() {
        return REFINE_UNDER_USES_DEFAULT_XML;
    }

    @Before
    @Override
    public void setUp() throws ModelNodeInitException, SchemaBuildException {
        super.setUp();
        initialiseInterceptor();
    }

    @Test
    public void testOverriddenDefaults() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<test-refine-under-uses xmlns=\"urn:org:bbf2:pma:refineUnderUses\">"
                + "  <overridden-defaults>"
                + "  </overridden-defaults>" 
                + "</test-refine-under-uses>";

        editConfig(m_server, m_clientInfo, requestXml1, true);

        String ncResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" 
                + " <data>"
                + "   <ref:test-refine-under-uses xmlns:ref=\"urn:org:bbf2:pma:refineUnderUses\">"
                + "     <ref:overridden-defaults>"
                + "       <ref:leaf1>1</ref:leaf1>" 
                + "       <ref:leafB>caseB</ref:leafB>" 
                + "     </ref:overridden-defaults>"
                + "   </ref:test-refine-under-uses>" 
                + " </data>" 
                + "</rpc-reply>";
        verifyGet(ncResponse);

    }

    @Test
    public void testOverriddenDescriptionandReference() throws Exception {
        String expected_modified_description = "This is the changed description.";
        String expected_modified_reference = "This is the overridden reference.";
        String yang_description = m_schemaRegistry.getDataSchemaNode(DESCRIPTION_LEAF_SCHEMA_PATH).getDescription().get();
        String yang_reference = m_schemaRegistry.getDataSchemaNode(DESCRIPTION_LEAF_SCHEMA_PATH).getReference().get();
        assertEquals(expected_modified_description, yang_description);
        assertEquals(expected_modified_reference, yang_reference);
    }

    @Test
    public void testOverriddenMandatory() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<test-refine-under-uses xmlns=\"urn:org:bbf2:pma:refineUnderUses\">"
                + " <overridden-mandatory>" 
                + "<name>test</name>"
                + "<masterName>vijay</masterName>" 
                + "<studentName>sanjay</studentName>" 
                + "<relative>mysql</relative>"
                + "</overridden-mandatory>" 
                + "</test-refine-under-uses>";

        editConfig(m_server, m_clientInfo, requestXml1, true);

        String ncResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" 
                + " <data>"
                + "   <ref:test-refine-under-uses xmlns:ref=\"urn:org:bbf2:pma:refineUnderUses\">"
                + "     <ref:overridden-defaults>"
                + "       <ref:leaf1>1</ref:leaf1>" 
                + "       <ref:leafB>caseB</ref:leafB>" 
                + "     </ref:overridden-defaults>"
                + " <ref:overridden-mandatory>" 
                + "<ref:masterName>vijay</ref:masterName>" 
                + "<ref:name>test</ref:name>"
                + "<ref:relative>mysql</ref:relative>" 
                + "<ref:studentName>sanjay</ref:studentName>" 
                + "</ref:overridden-mandatory>"
                + "   </ref:test-refine-under-uses>" 
                + " </data>" 
                + "</rpc-reply>";
        verifyGet(ncResponse);
    }

    @Test
    public void testFailurecase_OverriddenMandatory() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<test-refine-under-uses xmlns=\"urn:org:bbf2:pma:refineUnderUses\">"
                + " <overridden-mandatory>" 
                + "<name>test</name>"
                + "<studentName>sanjay</studentName>" 
                + "<relative>mysql</relative>" 
                + "</overridden-mandatory>"
                + "</test-refine-under-uses>";
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals(1, response.getErrors().size());
        assertEquals(NetconfRpcErrorType.Application, response.getErrors().get(0).getErrorType());
        assertEquals("/ref:test-refine-under-uses/ref:overridden-mandatory[ref:name='test']/ref:masterName", response.getErrors().get(0).getErrorPath());
        assertEquals("Missing mandatory node - masterName", response.getErrors().get(0).getErrorMessage());
    }

    @Test
    public void testFailurecase_OverriddenMaxElements_List() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<test-refine-under-uses xmlns=\"urn:org:bbf2:pma:refineUnderUses\">"
                + "<overridden-maxminelements>"
                + "<name>checkrefine</name>" 
                + "<leaflistname>test</leaflistname>" 
                + " <maxminelements>" 
                + "<name>test1</name>"
                + "</maxminelements>" 
                + " <maxminelements>" 
                + "<name>test2</name>" 
                + "</maxminelements>" 
                + " <maxminelements>"
                + "<name>test3</name>" 
                + "</maxminelements>" 
                + " <maxminelements>" 
                + "<name>test4</name>" 
                + "</maxminelements>"
                + "<maxminelements>" 
                + "<name>test5</name>" 
                + "</maxminelements>" 
                + "</overridden-maxminelements>"
                + "</test-refine-under-uses>";
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals(1, response.getErrors().size());
        assertEquals(NetconfRpcErrorType.Application, response.getErrors().get(0).getErrorType());
        assertEquals("/ref:test-refine-under-uses/ref:overridden-maxminelements[ref:name='checkrefine']/ref:maxminelements", response.getErrors().get(0).getErrorPath());
        assertEquals("Maximum elements allowed for maxminelements is 4.", response.getErrors().get(0).getErrorMessage());
    }

    @Test
    public void testFailurecase_OverriddenMinElements_LeafList() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<test-refine-under-uses xmlns=\"urn:org:bbf2:pma:refineUnderUses\">"
                + "<overridden-maxminelements>"
                + "<name>checkrefine</name>" 
                + " <maxminelements>" 
                + "<name>test1</name>" 
                + "</maxminelements>"
                + "</overridden-maxminelements>" 
                + "</test-refine-under-uses>";
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals(1, response.getErrors().size());
        assertEquals(NetconfRpcErrorType.Application, response.getErrors().get(0).getErrorType());
        assertEquals("/ref:test-refine-under-uses/ref:overridden-maxminelements[ref:name='checkrefine']/ref:leaflistname", response.getErrors().get(0).getErrorPath());
        assertEquals("Minimum elements required for leaflistname is 1.", response.getErrors().get(0).getErrorMessage());
    }

    @Test
    public void testOverriddenMaxMinElements() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<test-refine-under-uses xmlns=\"urn:org:bbf2:pma:refineUnderUses\">"
                + "<overridden-maxminelements>"
                + "<name>checkrefine</name>" 
                + "<leaflistname>test</leaflistname>" 
                + " <maxminelements>" 
                + "<name>test1</name>"
                + "</maxminelements>" 
                + " <maxminelements>" 
                + "<name>test2</name>" 
                + "</maxminelements>" 
                + " <maxminelements>"
                + "<name>test3</name>" 
                + "</maxminelements>" 
                + " <maxminelements>" 
                + "<name>test4</name>" 
                + "</maxminelements>"
                + "</overridden-maxminelements>" 
                + "</test-refine-under-uses>";
        editConfig(m_server, m_clientInfo, requestXml1, true);
        String ncResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" 
                + " <data>"
                + "   <ref:test-refine-under-uses xmlns:ref=\"urn:org:bbf2:pma:refineUnderUses\">"
                + "     <ref:overridden-defaults>"
                + "       <ref:leaf1>1</ref:leaf1>" 
                + "       <ref:leafB>caseB</ref:leafB>" 
                + "     </ref:overridden-defaults>"
                + "<ref:overridden-maxminelements>" 
                + "<ref:leaflistname>test</ref:leaflistname>" 
                + "<ref:maxminelements>"
                + "<ref:name>test1</ref:name>" 
                + "</ref:maxminelements>" 
                + "<ref:maxminelements>" 
                + "<ref:name>test2</ref:name>"
                + "</ref:maxminelements>" 
                + "<ref:maxminelements>" 
                + "<ref:name>test3</ref:name>" 
                + "</ref:maxminelements>"
                + "<ref:maxminelements>" 
                + "<ref:name>test4</ref:name>" 
                + "</ref:maxminelements>" 
                + "<ref:name>checkrefine</ref:name>"
                + "</ref:overridden-maxminelements>" 
                + "   </ref:test-refine-under-uses>" 
                + " </data>" 
                + "</rpc-reply>";
        verifyGet(ncResponse);
    }

    @Test
    public void testOverriddenPresenceContainer() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<test-refine-under-uses xmlns=\"urn:org:bbf2:pma:refineUnderUses\">"
                + "<overridden-presencecontainer>"
                + "<name>test</name>" 
                + "<testcontainer/>" 
                + "</overridden-presencecontainer>" 
                + "</test-refine-under-uses>";
        editConfig(m_server, m_clientInfo, requestXml1, true);
        String ncResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" 
                + " <data>"
                + "   <ref:test-refine-under-uses xmlns:ref=\"urn:org:bbf2:pma:refineUnderUses\">"
                + "     <ref:overridden-defaults>"
                + "       <ref:leaf1>1</ref:leaf1>" 
                + "       <ref:leafB>caseB</ref:leafB>" 
                + "     </ref:overridden-defaults>"
                + "<ref:overridden-presencecontainer>" 
                + "<ref:name>test</ref:name>" 
                + "<ref:testcontainer/>"
                + "</ref:overridden-presencecontainer>" 
                + "   </ref:test-refine-under-uses>" 
                + " </data>" 
                + "</rpc-reply>";
        verifyGet(ncResponse);
    }

    @Test
    public void testFailurecase_Overridden_Config() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<test-refine-under-uses xmlns=\"urn:org:bbf2:pma:refineUnderUses\">"
                + "<overridden-config>" 
                + "<name>test</name>"
                + "<config>changable</config>" 
                + "</overridden-config>" 
                + "</test-refine-under-uses>";
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals(1, response.getErrors().size());
        assertEquals(NetconfRpcErrorType.Application, response.getErrors().get(0).getErrorType());
        assertEquals("/ref:test-refine-under-uses/ref:overridden-config[ref:name='test']/ref:config", response.getErrors().get(0).getErrorPath());
        assertEquals("config is config false", response.getErrors().get(0).getErrorMessage());
    }

    @Test
    public void testOverriddenMust() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<test-refine-under-uses xmlns=\"urn:org:bbf2:pma:refineUnderUses\">"
                + "<overridden-must>" 
                + "<name>test</name>"
                + "<leaf1>leaf1</leaf1>" 
                + "<leaf2>leaf2</leaf2>" 
                + "<leaf3>leaf3</leaf3>" 
                + "<leaf4>leaf4</leaf4>" 
                + "<testlist>"
                + "<name>testname</name>" 
                + "</testlist>" 
                + "<testleaflist>leaflist1</testleaflist>"
                + "<testleaflist>leaflist2</testleaflist>" 
                + "</overridden-must>" 
                + "</test-refine-under-uses>";

        editConfig(m_server, m_clientInfo, requestXml1, true);

        String ncResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" 
                + " <data>"
                + "   <ref:test-refine-under-uses xmlns:ref=\"urn:org:bbf2:pma:refineUnderUses\">"
                + "     <ref:overridden-defaults>"
                + "       <ref:leaf1>1</ref:leaf1>" 
                + "       <ref:leafB>caseB</ref:leafB>" 
                + "     </ref:overridden-defaults>"
                + "<ref:overridden-must>" 
                + "<ref:name>test</ref:name>" 
                + "<ref:leaf1>leaf1</ref:leaf1>" 
                + "<ref:leaf2>leaf2</ref:leaf2>"
                + "<ref:leaf3>leaf3</ref:leaf3>" 
                + "<ref:leaf4>leaf4</ref:leaf4>" 
                + "<ref:testcontainer/>"
                + "<ref:testleaflist>leaflist1</ref:testleaflist>" 
                + "<ref:testleaflist>leaflist2</ref:testleaflist>" 
                + "<ref:testlist>"
                + "<ref:name>testname</ref:name>" 
                + "</ref:testlist>" 
                + "</ref:overridden-must>" 
                + "   </ref:test-refine-under-uses>"
                + " </data>" 
                + "</rpc-reply>";
        verifyGet(ncResponse);
    }

    @Test
    public void testFailurecase_OverriddenMust_Leaf() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<test-refine-under-uses xmlns=\"urn:org:bbf2:pma:refineUnderUses\">"
                + "<overridden-must>" 
                + "<name>test</name>"
                + "<leaf1>leaf</leaf1>" 
                + "<leaf3>leaf3</leaf3>" 
                + "</overridden-must>" 
                + "</test-refine-under-uses>";

        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals(1, response.getErrors().size());
        assertEquals("must-violation", response.getErrors().get(0).getErrorAppTag());
        assertEquals(NetconfRpcErrorType.Application, response.getErrors().get(0).getErrorType());
        assertEquals("/ref:test-refine-under-uses/ref:overridden-must[ref:name='test']/ref:leaf3", response.getErrors().get(0).getErrorPath());
        assertEquals("Violate must constraints: ../leaf1 = 'leaf1'", response.getErrors().get(0).getErrorMessage());
    }

    @Test
    public void testFailurecase_OverriddenMust_Container() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<test-refine-under-uses xmlns=\"urn:org:bbf2:pma:refineUnderUses\">"
                + "<overridden-must>" 
                + "<name>test</name>"
                + "<leaf1>leaf1</leaf1>" 
                + "<leaf2>leaf</leaf2>" 
                + "</overridden-must>" 
                + "</test-refine-under-uses>";

        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals(1, response.getErrors().size());
        assertEquals("must-violation", response.getErrors().get(0).getErrorAppTag());
        assertEquals(NetconfRpcErrorType.Application, response.getErrors().get(0).getErrorType());
        assertEquals("/ref:test-refine-under-uses/ref:overridden-must[ref:name='test']/ref:testcontainer", response.getErrors().get(0).getErrorPath());
        assertEquals("Violate must constraints: ../leaf2 = 'leaf2'", response.getErrors().get(0).getErrorMessage());
    }

    @Test
    public void testFailurecase_OverriddenMust_List() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<test-refine-under-uses xmlns=\"urn:org:bbf2:pma:refineUnderUses\">"
                + "<overridden-must>" 
                + "<name>test</name>"
                + "<leaf1>leaf1</leaf1>" 
                + "<leaf3>leaf</leaf3>" 
                + "<testlist>" 
                + "<name>testname</name>" 
                + "</testlist>"
                + "</overridden-must>" 
                + "</test-refine-under-uses>";
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals(1, response.getErrors().size());
        assertEquals("must-violation", response.getErrors().get(0).getErrorAppTag());
        assertEquals(NetconfRpcErrorType.Application, response.getErrors().get(0).getErrorType());
        assertEquals("Violate must constraints: ../leaf3 = 'leaf3'", response.getErrors().get(0).getErrorMessage());
    }

    @Test
    public void testFailurecase_OverriddenMust_Leaflist() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<test-refine-under-uses xmlns=\"urn:org:bbf2:pma:refineUnderUses\">"
                + "<overridden-must>" 
                + "<name>test</name>"
                + "<leaf4>leaf</leaf4>" 
                + "<testleaflist>leaflist1</testleaflist>" 
                + "<testleaflist>leaflist2</testleaflist>"
                + "</overridden-must>" 
                + "</test-refine-under-uses>";
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals(1, response.getErrors().size());
        assertEquals("must-violation", response.getErrors().get(0).getErrorAppTag());
        assertEquals(NetconfRpcErrorType.Application, response.getErrors().get(0).getErrorType());
        assertEquals("/ref:test-refine-under-uses/ref:overridden-must[ref:name='test']/ref:testleaflist", response.getErrors().get(0).getErrorPath());
        assertEquals("Violate must constraints: ../leaf4 = 'leaf4'", response.getErrors().get(0).getErrorMessage());
    }

    @After
    public void tearDown() {
        m_addDefaultDataInterceptor.destroy();
    }

}