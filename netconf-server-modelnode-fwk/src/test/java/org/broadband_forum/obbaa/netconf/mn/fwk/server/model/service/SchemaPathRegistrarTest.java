package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.jxpath.ri.compiler.Expression;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;    
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;

import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.DataStore;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditConfigException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditConfigTestFailedException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.LockedByOtherSessionException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.PersistenceException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.validation.AbstractDataStoreValidatorTest;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;

public class SchemaPathRegistrarTest extends AbstractDataStoreValidatorTest {
    static QName createQName(String localName){
        return QName.create("urn:org:bbf:pma:extension:test", "2015-12-14", localName);
    }

    public static final QName SOMEABS_QNAME = createQName("someAbs");
    public static final QName VALIDATION_EXT_QNAME = createQName("validation");
    private static final QName EXTLEAF_QNAME = createQName("extLeaf");
    private static final QName EXTLEAF1_QNAME = createQName("extLeaf1");
    private static final QName EXTLEAF2_QNAME = createQName("extLeaf2");
    private static final QName EXTLEAF3_QNAME = createQName("extLeaf3");
    private static final QName SOME_CONTAINER_QNAME = createQName("someContainer");
    private static final QName REF_QNAME = createQName("ref");
    
    private static final QName TYPE_QNAME = QName.create("urn:org:bbf:pma:validation", "2015-12-14", "type");
    private static final QName INTFSTATE_QNAME = QName.create("urn:org:bbf:pma:validation", "2015-12-14", "testinterface-state");
    private static final QName TESTNOTIF_QNAME = QName.create("urn:org:bbf:pma:validation", "2015-12-14", "testNotification");
    private static final QName TESTNOTIF_CONTAINER_QNAME = QName.create("urn:org:bbf:pma:validation", "2015-12-14", "notif-container");
    private static final QName TESTNOTIF_LEAF_QNAME = QName.create("urn:opendaylight:datastore-validator-augment-test", "2018-03-07", "sample-leaf");

    public static final SchemaPath TYPE_SCHEMAPATH = SchemaPath.create(true, VALIDATION5_QNAME, TYPE_QNAME);
    public static final SchemaPath INTFSTATE_SCHEMAPATH = SchemaPath.create(true, VALIDATION5_QNAME, INTFSTATE_QNAME);

    public static final SchemaPath SOMEABS_SCHEMAPATH = SchemaPath.create(true, SOMEABS_QNAME);
    public static final SchemaPath VALIDATION_SCHEMAPATH = buildSchemaPath(SOMEABS_SCHEMAPATH, VALIDATION_EXT_QNAME);
    public static final SchemaPath EXTLEAF_SCHEMAPATH = buildSchemaPath(VALIDATION_SCHEMAPATH, EXTLEAF_QNAME);
    public static final SchemaPath EXTLEAF1_SCHEMAPATH = buildSchemaPath(VALIDATION_SCHEMAPATH, EXTLEAF1_QNAME);
    public static final SchemaPath EXTLEAF2_SCHEMAPATH = buildSchemaPath(VALIDATION_SCHEMAPATH, EXTLEAF2_QNAME);
    public static final SchemaPath EXTLEAF3_SCHEMAPATH = buildSchemaPath(VALIDATION_SCHEMAPATH, EXTLEAF3_QNAME);
    public static final SchemaPath SOME_CONTAINER_SCHEMAPATH = buildSchemaPath(VALIDATION_SCHEMAPATH, SOME_CONTAINER_QNAME);
    public static final SchemaPath REF_SCHEMAPATH = buildSchemaPath(SOME_CONTAINER_SCHEMAPATH, REF_QNAME);
    
    private static final SchemaPath TESTNOTIF_LEAF_SCHEMAPATH = SchemaPath.create(true, TESTNOTIF_QNAME, TESTNOTIF_CONTAINER_QNAME, TESTNOTIF_LEAF_QNAME);
    
    private static final String COMPONENT_ID = "G.Fast-1.1";
    
    protected SchemaPathRegistrar m_pathRegistrar;

    protected List<String> getYang() {
        List<String> fileNames = new ArrayList<String>();
        fileNames.add("/datastorevalidatortest/yangs/datastore-validator-test.yang");
        fileNames.add("/datastorevalidatortest/yangs/datastore-validator-augment-test.yang");
        fileNames.add("/datastorevalidatortest/yangs/dummy-extension.yang");
        fileNames.add("/datastorevalidatortest/yangs/extension-test.yang");
        fileNames.add("/datastorevalidatortest/yangs/extension-test-container.yang");
        fileNames.add("/datastorevalidatortest/yangs/ietf-inet-types.yang");
        fileNames.add("/datastorevalidatortest/yangs/datastore-validator-grouping-test.yang");
        fileNames.add("/datastorevalidatortest/yangs/rpc-output-augmentation-validation-test.yang");
        return fileNames;
        
    }

    @Test
    @Ignore
    public void testForRelativePath(){
        Expression relativePath = m_schemaRegistry.getRelativePath("/someAbs/validation/leaf1", m_schemaRegistry.getDataSchemaNode(EXTLEAF_SCHEMAPATH));
        assertEquals("../../validation/leaf1",relativePath.toString());
        relativePath = m_schemaRegistry.getRelativePath("/extTest:someAbs/extTest:validation/extTest:leaf1", m_schemaRegistry.getDataSchemaNode(EXTLEAF1_SCHEMAPATH));
        assertEquals("../../extTest:validation/extTest:leaf1",relativePath.toString());
        relativePath = m_schemaRegistry.getRelativePath("/someAbs/validation/leaf1 > 10", m_schemaRegistry.getDataSchemaNode(EXTLEAF2_SCHEMAPATH));
        assertEquals("../../validation/leaf1 > 10",relativePath.toString());
        relativePath = m_schemaRegistry.getRelativePath("count(/someAbs/validation/leaf1) > 1", m_schemaRegistry.getDataSchemaNode(EXTLEAF3_SCHEMAPATH));
        assertEquals("count(../../validation/leaf1) > 1",relativePath.toString());
        relativePath = m_schemaRegistry.getRelativePath("/someAbs/validation/leaf1", m_schemaRegistry.getDataSchemaNode(REF_SCHEMAPATH));
        assertEquals("../../../validation/leaf1",relativePath.toString());
    }
    
    @Override
    protected SchemaRegistry getSchemaRegistry() throws SchemaBuildException{
        List<YangTextSchemaSource> yangFiles = TestUtil.getByteSources(getYang());
        SchemaRegistry schemaRegistry =  new SchemaRegistryImpl(yangFiles, Collections.emptySet(), Collections.emptyMap(), new NoLockService());
        schemaRegistry.registerAppAllowedAugmentedPath("Module1","/someAbs", mock(SchemaPath.class));
        schemaRegistry.registerAppAllowedAugmentedPath("Module1","/extTest:someAbs", mock(SchemaPath.class));
        return schemaRegistry;
        
    }
    
    @Before
    public void setUp() throws ModelNodeInitException, SchemaBuildException {
        super.setUp();
        m_pathRegistrar = new SchemaPathRegistrar(m_schemaRegistry, m_modelNodeHelperRegistry);
        getModelNode();
    }

    @Test
    public void testGetExceptionScenarioOnEditConfig() throws ModelNodeInitException, EditConfigException, EditConfigTestFailedException, PersistenceException, LockedByOtherSessionException {
        String requestXml1 = "/datastorevalidatortest/rangevalidation/defaultxml/valid-when-constraint-leaf.xml";
        String exceptionMessage = "Get-Exception invalid hardware type";
        EditConfigRequest request1 = createRequest(requestXml1);
        request1.setMessageId("1");
        NetConfResponse response1 = new NetConfResponse().setMessageId("1");
        DataStore dataStore = mock(DataStore.class);
        m_server.setRunningDataStore(dataStore);
        NetconfRpcError error = mock(NetconfRpcError.class);
        when(error.getErrorMessage()).thenReturn(exceptionMessage);
        GetException e = new GetException(error);
        when(dataStore.edit(any(), any())).thenThrow(e);
        m_server.onEditConfig(m_clientInfo, request1, response1);
        assertEquals(exceptionMessage, response1.getErrors().get(0).getErrorMessage());
    }

    @Test
    public void visitLeafNodeTest() throws ModelNodeInitException {
        String requestXml1 = "/datastorevalidatortest/rangevalidation/defaultxml/valid-when-constraint-leaf.xml";
        EditConfigRequest request1 = createRequest(requestXml1);
        request1.setMessageId("1");
        NetConfResponse response1 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request1, response1);

        SchemaPath whenSchemaPath = buildSchemaPath("when-validation", null);
        SchemaPath leafSchemaPath = buildSchemaPath("when-validation", "leaf-type");
        DataSchemaNode leafNode = m_schemaRegistry.getDataSchemaNode(leafSchemaPath);
        m_pathRegistrar.visitLeafNode(COMPONENT_ID, whenSchemaPath, (LeafSchemaNode) leafNode);

        Collection<SchemaPath> schemaPaths = m_schemaRegistry.getSchemaPathsForComponent(COMPONENT_ID);
        assertTrue(schemaPaths.size() == 1);
        
        requestXml1 = "/datastorevalidatortest/rangevalidation/defaultxml/valid-must-constraint-leaf.xml";
        request1 = createRequest(requestXml1);
        request1.setMessageId("1");
        response1 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request1, response1);

        SchemaPath mustSchemaPath = buildSchemaPath("must-validation", null);
        leafSchemaPath = buildSchemaPath("must-validation", "leaf-type");
        leafNode = m_schemaRegistry.getDataSchemaNode(leafSchemaPath);
        m_pathRegistrar.visitLeafNode(COMPONENT_ID, mustSchemaPath, (LeafSchemaNode) leafNode);

        schemaPaths = m_schemaRegistry.getSchemaPathsForComponent(COMPONENT_ID);
        assertTrue(schemaPaths.size() == 2);
    }

    @Test
    public void visitLeafNodeInNotificationTest() throws ModelNodeInitException {
        
        DataSchemaNode leafNode = m_schemaRegistry.getDataSchemaNode(TESTNOTIF_LEAF_SCHEMAPATH);
        m_pathRegistrar.visitLeafNode(COMPONENT_ID, TESTNOTIF_LEAF_SCHEMAPATH, (LeafSchemaNode) leafNode);

        Collection<SchemaPath> schemaPaths = m_schemaRegistry.getSchemaPathsForComponent(COMPONENT_ID);
        assertEquals(0, schemaPaths.size());
    }
    
    @Test
    public void visitLeafListNodeTest() throws ModelNodeInitException {
        String requestXml1 = "/datastorevalidatortest/rangevalidation/defaultxml/valid-when-constraint-leaflist.xml";
        EditConfigRequest request1 = createRequest(requestXml1);
        request1.setMessageId("1");
        NetConfResponse response1 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request1, response1);

        SchemaPath whenSchemaPath = buildSchemaPath("when-validation", null);
        SchemaPath leafListSchemaPath = buildSchemaPath("when-validation", "leaflist-type");
        DataSchemaNode leafListNode = m_schemaRegistry.getDataSchemaNode(leafListSchemaPath);
        m_pathRegistrar.visitLeafListNode(COMPONENT_ID, whenSchemaPath, (LeafListSchemaNode) leafListNode);

        Collection<SchemaPath> schemaPaths = m_schemaRegistry.getSchemaPathsForComponent(COMPONENT_ID);
        assertTrue(schemaPaths.size() == 1);
    }
    
    @Test
    public void visitContainerNodeTest() throws ModelNodeInitException {
        String requestXml1 = "/datastorevalidatortest/rangevalidation/defaultxml/valid-when-constraint-container.xml";
        EditConfigRequest request1 = createRequest(requestXml1);
        request1.setMessageId("1");
        NetConfResponse response1 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request1, response1);

        SchemaPath whenSchemaPath = buildSchemaPath("when-validation", null);
        SchemaPath containerSchemaPath = buildSchemaPath("when-validation", "container-type");
        DataSchemaNode containerSchemaNode = m_schemaRegistry.getDataSchemaNode(containerSchemaPath);
        m_pathRegistrar.visitContainerNode(COMPONENT_ID, whenSchemaPath, (ContainerSchemaNode) containerSchemaNode);

        Collection<SchemaPath> schemaPaths = m_schemaRegistry.getSchemaPathsForComponent(COMPONENT_ID);
        assertTrue(schemaPaths.size() == 1);
    }
    
    @Test
    public void testWhenDerivedFromOrLeafOnStateNode() throws Exception {        
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation5 xmlns=\"urn:org:bbf:pma:validation\">"+
                "    <type>identity2</type>" +
                "</validation5>";
        editConfig(m_server, m_clientInfo, requestXml, true);

        String expectedOutput = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>" 
                + " <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\"/>"
                + "     <validation:validation5 xmlns:validation=\"urn:org:bbf:pma:validation\">"
                + "         <validation:must-with-derived-from-or-self>" 
                + "             <validation:mustLeaf>must</validation:mustLeaf>"
                + "         </validation:must-with-derived-from-or-self>"
                + "         <validation:type>validation:identity2</validation:type>" 
                + "     </validation:validation5>" 
                + " </data>"
                + "</rpc-reply>";

        verifyGet(expectedOutput);    
        assertFalse(m_schemaRegistry.getReferencedNodesForSchemaPaths(TYPE_SCHEMAPATH).containsKey(INTFSTATE_SCHEMAPATH));
    }
    
    @Test
    public void visitListNodeTest() throws ModelNodeInitException {
        String requestXml1 = "/datastorevalidatortest/rangevalidation/defaultxml/valid-when-constraint-list.xml";
        EditConfigRequest request1 = createRequest(requestXml1);
        request1.setMessageId("1");
        NetConfResponse response1 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request1, response1);

        SchemaPath whenSchemaPath = buildSchemaPath("when-validation", null);
        SchemaPath listSchemaPath = buildSchemaPath("when-validation", "list-type");
        DataSchemaNode listSchemaNode = m_schemaRegistry.getDataSchemaNode(listSchemaPath);
        m_pathRegistrar.visitListNode(COMPONENT_ID, whenSchemaPath, (ListSchemaNode) listSchemaNode);

        Collection<SchemaPath> schemaPaths = m_schemaRegistry.getSchemaPathsForComponent(COMPONENT_ID);
        assertTrue(schemaPaths.size() == 1);
    }
    
    @Test
    public void visitChoiceNodeTest() throws ModelNodeInitException {
        String requestXml1 = "/datastorevalidatortest/rangevalidation/defaultxml/valid-when-constraint-container-choicenode.xml";
        EditConfigRequest request1 = createRequest(requestXml1);
        request1.setMessageId("1");
        NetConfResponse response1 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request1, response1);

        SchemaPath whenSchemaPath = buildSchemaPath("when-validation", null);
        SchemaPath choiceContainerSchemaPath = buildSchemaPath("when-validation", "choicecase");
        SchemaPath choiceSchemaPath = m_schemaRegistry.getDescendantSchemaPath(choiceContainerSchemaPath,
                m_schemaRegistry.lookupQName(NAMESPACE, "container-type"));
        DataSchemaNode choiceSchemaNode = m_schemaRegistry.getDataSchemaNode(choiceSchemaPath);
        m_pathRegistrar.visitChoiceNode(COMPONENT_ID, whenSchemaPath, (ChoiceSchemaNode) choiceSchemaNode);

        Collection<SchemaPath> schemaPaths = m_schemaRegistry.getSchemaPathsForComponent(COMPONENT_ID);
        assertTrue(schemaPaths.size() == 2);
    }
    
    @Test
    public void visitChoiceCaseNodeTest() throws ModelNodeInitException {
        String requestXml1 = "/datastorevalidatortest/rangevalidation/defaultxml/valid-when-constraint-container-casenode.xml";
        EditConfigRequest request1 = createRequest(requestXml1);
        request1.setMessageId("1");
        NetConfResponse response1 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request1, response1);

        SchemaPath whenSchemaPath = buildSchemaPath("when-validation", null);
        SchemaPath choiceContainerSchemaPath = buildSchemaPath("when-validation", "choicecase");
        SchemaPath choiceSchemaPath = m_schemaRegistry.getDescendantSchemaPath(choiceContainerSchemaPath,
                m_schemaRegistry.lookupQName(NAMESPACE, "container-type"));
        ChoiceSchemaNode choiceSchemaNode = (ChoiceSchemaNode) m_schemaRegistry.getDataSchemaNode(choiceSchemaPath);
        m_pathRegistrar.visitChoiceCaseNode(COMPONENT_ID, whenSchemaPath, choiceSchemaNode.getCaseNodeByName(m_schemaRegistry.lookupQName(NAMESPACE, "container-case-success")));

        Collection<SchemaPath> schemaPaths = m_schemaRegistry.getSchemaPathsForComponent(COMPONENT_ID);
        assertTrue(schemaPaths.size() == 1);
    }
    
    @Test
    public void visitLeafRefNodeTest() throws ModelNodeInitException {
        String requestXml1 = "/datastorevalidatortest/leafrefvalidation/defaultxml/valid-leaf-ref.xml";
        EditConfigRequest request1 = createRequest(requestXml1);
        request1.setMessageId("1");
        NetConfResponse response1 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request1, response1);

        SchemaPath leafRefContainerSchemaPath = buildSchemaPath("leaf-ref", null);
        SchemaPath leafRefSchemaPath = m_schemaRegistry.getDescendantSchemaPath(leafRefContainerSchemaPath,
                m_schemaRegistry.lookupQName(NAMESPACE, "artist-name"));
        DataSchemaNode leafNode = m_schemaRegistry.getDataSchemaNode(leafRefSchemaPath);
        m_pathRegistrar.visitLeafNode(COMPONENT_ID, leafRefContainerSchemaPath, (LeafSchemaNode) leafNode);

        Collection<SchemaPath> schemaPaths = m_schemaRegistry.getSchemaPathsForComponent(COMPONENT_ID);
        assertTrue(schemaPaths.size() == 1);
    }
    
    private SchemaPath buildSchemaPath(String parent, String child) {
        SchemaPath parentSchemaPath = m_schemaRegistry.getDescendantSchemaPath(m_rootModelNode.getModelNodeSchemaPath(),
                m_schemaRegistry.lookupQName(NAMESPACE, parent));

        if (child != null) {
            SchemaPath childSchemaPath = m_schemaRegistry.getDescendantSchemaPath(parentSchemaPath,
                    m_schemaRegistry.lookupQName(NAMESPACE, child));
            return childSchemaPath;
        } else {
            return parentSchemaPath;
        }
    }
    
}
