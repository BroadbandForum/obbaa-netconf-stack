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

import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.ValidationConstants.VALIDATION_NS;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.ValidationConstants.VALIDATION_REVISION;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaMountRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.RpcRequestConstraintParser;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.CompositeSubSystemImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.DataStore;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NbiNotificationHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NetConfServerImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDSMRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDSMRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.utils.AnnotationAnalysisException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.utils.EntityRegistryBuilder;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildContainerHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildListHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeFactoryException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootEntityContainerModelNodeHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregatorImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.DataStoreIntegrityService;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.DataStoreValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.DataStoreValidatorImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.service.DataStoreIntegrityServiceImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DSExpressionValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.AggregatedDSM;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.EntityModelNodeHelperDeployer;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.EntityRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.EntityRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.RequestScopeXmlDSMCache;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.XmlContainerModelNodeHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.XmlDSMCache;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.XmlListModelNodeHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.XmlModelNodeToXmlMapper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.XmlModelNodeToXmlMapperImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.XmlSubtreeDSM;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.inmemory.InMemoryDSM;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.utils.TestTxUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.LocalSubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.YangUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.validation.model.OrderByUserList;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.validation.model.SomeInnerList;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.validation.model.SomeList;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.validation.model.Validation;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.yang.AbstractValidationTestSetup;
import org.broadband_forum.obbaa.netconf.persistence.EMFactory;
import org.broadband_forum.obbaa.netconf.persistence.PersistenceManagerUtil;
import org.broadband_forum.obbaa.netconf.persistence.jpa.JPAEntityManagerFactory;
import org.broadband_forum.obbaa.netconf.persistence.jpa.ThreadLocalPersistenceManagerUtil;
import org.junit.Before;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.xml.sax.SAXException;

@SuppressWarnings("deprecation")
public class AbstractRootModelTest extends AbstractValidationTestSetup{
    private XmlDSMCache m_dsmCache = new RequestScopeXmlDSMCache();
    static QName createQName(String localName){
        return QName.create(VALIDATION_NS, VALIDATION_REVISION, localName);
    }
    static final List<SchemaPath> SCHEMA_PATHS = new ArrayList<SchemaPath>();

    protected static void addSchemaPath(SchemaPath schemaPath){
        SCHEMA_PATHS.add(schemaPath);
    }

    public static SchemaPath buildSchemaPath(SchemaPath parent, QName qname){
        SchemaPath schemaPath = AbstractValidationTestSetup.buildSchemaPath(parent, qname);
        addSchemaPath(schemaPath);
        return schemaPath;
    }
    
    protected static final String COMPONENT_ID = "datastore-validator-test";
    protected static final String YANG_FILE = "/datastorevalidatortest/yangs/datastore-validator-test2.yang";

    protected static final QName VALIDATION_QNAME = createQName("validation");
    protected static final QName XML_SUB_TREE_QNAME = createQName("xml-subtree");
    protected static final QName LIST1_QNAME = createQName("list1");
    protected static final QName CONTAINER1_QNAME = createQName("container1");
    protected static final QName CONTAINER2_QNAME = createQName("container2");
    protected static final QName CHOICE_LEAF_QNAME = createQName("choice-leaf");
    protected static final QName CHOICE_LEAF1_QNAME = createQName("choice-leaf1");
    protected static final QName CHOICELEAF1_QNAME = createQName("choiceLeaf1");
    protected static final QName CHOICE_LEAF_LIST_QNAME = createQName("choice-leafList");
    protected static final QName MULTI_CONTAINER_QNAME = createQName("multiContainer");
    protected static final QName MULTI_CONTAINER_LIST_QNAME = createQName("multiContainerList");
    protected static final QName LEVEL1_CONTAINER_QNAME = createQName("level1");
    protected static final QName LEVEL2_CONTAINER_QNAME = createQName("level2");
    protected static final QName LEVEL3_CONTAINER_QNAME = createQName("level3");
    protected static final QName SOME_LIST_QNAME = createQName("someList");
    protected static final QName SOME_INNER_LIST_QNAME = createQName("someInnerList");
    protected static final QName CHILD_CONTAINER_QNAME = createQName("childContainer");
    protected static final QName OTHER_CONTAINER_LIST_QNAME = createQName("otherContainerList");
    protected static final QName XML_LIST_QNAME = createQName("xmlList");
    protected static final QName XML_LIST2_QNAME = createQName("xmlList2");
    protected static final QName DEFAULT_CONTAINER_QNAME = createQName("defaultContainer");
    protected static final QName STATE_CHECK_QNAME = createQName("stateCheck");
    protected static final QName ORDER_BY_USER_LIST = createQName("orderByUser");
    protected static final QName ORDER_BY_USER = createQName("orderByUserList");
    protected static final QName SCHEMA_MOUNT_POINT = createQName("schemaMountPoint");
    protected static final QName CURRENT_CHILD_QNAME = createQName("currentChildTest");
    protected static final QName TEST_REF_LEAF_REMOVE = createQName("testRefLeafRemove");
    protected static final QName TEST_REF_REMOVE = createQName("ref");
    protected static final QName TEST_CURRENT_ON_NON_EXISTANT = createQName("testCurrentOnNonExistant");
    protected static final QName TEST_INTERNAL_REQUEST = createQName("test-internal-request");
    protected static final QName TEST_INTERNAL_REQUEST_INTERFACE = createQName("interface");
    protected static final QName TRAPS_QNAME = createQName("traps");
    protected static final QName DUMMYLIST_QNAME = createQName("dummyList");
    protected static final QName INNER_CONTAINER_QNAME = createQName("innerContainer");
    
    protected static final SchemaPath VALIDATION_SCHEMA_PATH = SchemaPath.create(true, VALIDATION_QNAME);
    protected static final SchemaPath XML_SUBTREE_SCHEMA_PATH = buildSchemaPath(VALIDATION_SCHEMA_PATH, XML_SUB_TREE_QNAME);
    protected static final SchemaPath LIST1_SCHEMA_PATH = buildSchemaPath(XML_SUBTREE_SCHEMA_PATH, LIST1_QNAME);
    protected static final SchemaPath CONTAINER1_SCHEMA_PATH = buildSchemaPath(XML_SUBTREE_SCHEMA_PATH, CONTAINER1_QNAME);
    protected static final SchemaPath CONTAINER2_SCHEMA_PATH = buildSchemaPath(XML_SUBTREE_SCHEMA_PATH, CONTAINER2_QNAME);
    protected static final SchemaPath CONTAINER1_LIST1_SCHEMA_PATH = buildSchemaPath(CONTAINER1_SCHEMA_PATH, LIST1_QNAME);
    protected static final SchemaPath CHOICE_LEAF_SCHEMA_PATH = buildSchemaPath(CONTAINER1_LIST1_SCHEMA_PATH, CHOICE_LEAF_QNAME);
    protected static final SchemaPath CHOICE_LEAF1_SCHEMA_PATH = buildSchemaPath(CHOICE_LEAF_SCHEMA_PATH, CHOICE_LEAF1_QNAME);
    protected static final SchemaPath CHOICELEAF1_SCHEMA_PATH = buildSchemaPath(CHOICE_LEAF1_SCHEMA_PATH, CHOICELEAF1_QNAME);
    protected static final SchemaPath CHOICE_LEAF_LIST_SCHEMA_PATH = buildSchemaPath(CHOICE_LEAF1_SCHEMA_PATH, CHOICE_LEAF_LIST_QNAME);

    protected static final SchemaPath MULTI_CONTAINER_SCHEMA_PATH = buildSchemaPath(XML_SUBTREE_SCHEMA_PATH, MULTI_CONTAINER_QNAME);
    protected static final SchemaPath LEVEL1_CONTAINER_SCHEMA_PATH = buildSchemaPath(MULTI_CONTAINER_SCHEMA_PATH, LEVEL1_CONTAINER_QNAME);
    protected static final SchemaPath LEVEL2_CONTAINER_SCHEMA_PATH = buildSchemaPath(LEVEL1_CONTAINER_SCHEMA_PATH, LEVEL2_CONTAINER_QNAME);
    protected static final SchemaPath LEVEL3_CONTAINER_SCHEMA_PATH = buildSchemaPath(LEVEL2_CONTAINER_SCHEMA_PATH, LEVEL3_CONTAINER_QNAME);
    protected static final SchemaPath LEVEL3_LIST1_CONTAINER_SCHEMA_PATH = buildSchemaPath(LEVEL3_CONTAINER_SCHEMA_PATH, LIST1_QNAME);
    
    protected static final SchemaPath SOME_LIST_SCHEMA_PATH = buildSchemaPath(VALIDATION_SCHEMA_PATH, SOME_LIST_QNAME);
    protected static final SchemaPath SOME_INNER_LIST_SCHEMA_PATH = buildSchemaPath(SOME_LIST_SCHEMA_PATH, SOME_INNER_LIST_QNAME);
    protected static final SchemaPath CHILD_CONTAINER_SCHEMA_PATH = buildSchemaPath(SOME_INNER_LIST_SCHEMA_PATH, CHILD_CONTAINER_QNAME);
    protected static final SchemaPath CHILD_MULTI_CONTAINER_SCHEMA_PATH = buildSchemaPath(CHILD_CONTAINER_SCHEMA_PATH, MULTI_CONTAINER_QNAME);
    protected static final SchemaPath MULTI_CONTAINER_LIST_SCHEMA_PATH = buildSchemaPath(CHILD_MULTI_CONTAINER_SCHEMA_PATH, MULTI_CONTAINER_LIST_QNAME);
    protected static final SchemaPath OTHER_CONTAINER_LIST_SCHEMA_PATH = buildSchemaPath(CHILD_MULTI_CONTAINER_SCHEMA_PATH, OTHER_CONTAINER_LIST_QNAME);
    protected static final SchemaPath CHILD_LEVEL1_SCHEMA_PATH = buildSchemaPath(CHILD_MULTI_CONTAINER_SCHEMA_PATH, LEVEL1_CONTAINER_QNAME);
    protected static final SchemaPath CHILD_LEVEL2_SCHEMA_PATH = buildSchemaPath(CHILD_LEVEL1_SCHEMA_PATH, LEVEL2_CONTAINER_QNAME);
    protected static final SchemaPath CHILD_LEVEL3_SCHEMA_PATH = buildSchemaPath(CHILD_LEVEL2_SCHEMA_PATH, LEVEL3_CONTAINER_QNAME);
    protected static final SchemaPath CHILD_LEVEL3_LIST1_SCHEMA_PATH = buildSchemaPath(CHILD_LEVEL3_SCHEMA_PATH, LIST1_QNAME);
    
    protected static final SchemaPath XML_LIST_SCHEMA_PATH = buildSchemaPath(CHILD_CONTAINER_SCHEMA_PATH, XML_LIST_QNAME);
    protected static final SchemaPath XML_LIST2_SCHEMA_PATH = buildSchemaPath(CHILD_CONTAINER_SCHEMA_PATH, XML_LIST2_QNAME);
    protected static final SchemaPath DEFAULT_CONTAINER_SCHEMA_PATH = buildSchemaPath(XML_LIST2_SCHEMA_PATH, DEFAULT_CONTAINER_QNAME);
    
    protected static final SchemaPath CURRENT_CHILD_TEST_PATH = buildSchemaPath(XML_SUBTREE_SCHEMA_PATH, CURRENT_CHILD_QNAME);
    
    protected static final SchemaPath STATECHECK_SCHEMA_PATH = buildSchemaPath(CHILD_CONTAINER_SCHEMA_PATH, STATE_CHECK_QNAME);

    protected static final SchemaPath ORDER_BY_USER_LIST_PATH = buildSchemaPath(XML_SUBTREE_SCHEMA_PATH, ORDER_BY_USER_LIST);
    protected static final SchemaPath ORDER_BY_USER_PATH = buildSchemaPath(VALIDATION_SCHEMA_PATH, ORDER_BY_USER);
    protected static final SchemaPath ORDER_BY_USER_INNER_LIST_PATH = buildSchemaPath(SOME_INNER_LIST_SCHEMA_PATH, ORDER_BY_USER);
    protected static final SchemaPath SCHEMA_MOUNT_POINT_PATH = buildSchemaPath(XML_SUBTREE_SCHEMA_PATH, SCHEMA_MOUNT_POINT);

    protected static final SchemaPath TEST_REF_REMOVE_PATH = buildSchemaPath(XML_SUBTREE_SCHEMA_PATH, TEST_REF_LEAF_REMOVE);
    protected static final SchemaPath TEST_REF_REMOVE_LIST1_PATH = buildSchemaPath(TEST_REF_REMOVE_PATH, LIST1_QNAME);
    protected static final SchemaPath TEST_REF_REF_REMOVE_LIST1_PATH = buildSchemaPath(TEST_REF_REMOVE_LIST1_PATH, TEST_REF_REMOVE);
    
    protected static final SchemaPath TEST_CURRENT_ON_NON_EXISTANT_PATH = buildSchemaPath(XML_SUBTREE_SCHEMA_PATH, TEST_CURRENT_ON_NON_EXISTANT);
    protected static final SchemaPath TEST_CURRENT_ON_NON_EXISTENT_CONTAINER1_PATH = buildSchemaPath(TEST_CURRENT_ON_NON_EXISTANT_PATH, CONTAINER1_QNAME);
    
    protected static final SchemaPath DUMMYLIST_PATH = buildSchemaPath(XML_SUBTREE_SCHEMA_PATH, DUMMYLIST_QNAME);
    protected static final SchemaPath INNER_CONTAINER_PATH = buildSchemaPath(DUMMYLIST_PATH, INNER_CONTAINER_QNAME);
    
    protected static final SchemaPath TEST_INTERNAL_REQUEST_PATH = buildSchemaPath(XML_SUBTREE_SCHEMA_PATH, TEST_INTERNAL_REQUEST);
    protected static final SchemaPath TEST_INTERNAL_REQUEST_INTERFACE_PATH = buildSchemaPath(TEST_INTERNAL_REQUEST_PATH, TEST_INTERNAL_REQUEST_INTERFACE);
    protected static final SchemaPath INTERFACE_TRAPS_PATH = buildSchemaPath(TEST_INTERNAL_REQUEST_INTERFACE_PATH, TRAPS_QNAME);
    
    private static final String DEFAULT_XML = "/datastorevalidatortest/yangs/datastore-validator-defaultxml.xml";
    protected static final String NAMESPACE = VALIDATION_NS;
    protected static final String MESSAGE_ID = "1";

    protected AggregatedDSM m_aggregatedDSM;
    protected EntityRegistry m_entityRegistry;
    protected PersistenceManagerUtil m_persistenceManagerUtil;
    protected EntityModelNodeHelperDeployer m_entityModelNodeHelperDeployer;
    protected XmlModelNodeToXmlMapper m_xmlModelNodeToXmlMapper;
    protected ModelNodeDataStoreManager m_xmlSubtreeDSM;
    protected ModelNodeDSMRegistry m_modelNodeDSMRegistry;

    protected RootModelNodeAggregator m_rootModelNodeAggregator;
    protected ModelNodeHelperRegistry m_modelNodeHelperRegistry;
    protected SubSystemRegistry m_subSystemRegistry;
    protected NetConfServerImpl m_server;
    protected DataStoreValidator m_datastoreValidator;
    protected DataStore m_dataStore;
    protected static SchemaRegistry m_schemaRegistry;
    protected NetconfClientInfo m_clientInfo;
    protected ModelNode m_rootModelNode;
    protected DataStoreIntegrityService m_integrityService;
    protected static SchemaMountRegistry m_schemaMountRegistry;
    
    protected static List<String> getYangFiles() {
        List<String> fileNames = new LinkedList<String>();
        fileNames.add("/datastorevalidatortest/yangs/dummy-extension.yang");
        fileNames.add("/datastorevalidatortest/yangs/ietf-yang-schema-mount@2017-10-09.yang");
        fileNames.add("/datastorevalidatortest/yangs/datastore-validator-test2.yang");
        return fileNames;
    }
    
    @SuppressWarnings("unchecked")
    @Before
    public void setup() throws SchemaBuildException, AnnotationAnalysisException, ModelNodeFactoryException{
        Thread.currentThread().setContextClassLoader(AbstractRootModelTest.class.getClassLoader());
        EMFactory managerFactory = new JPAEntityManagerFactory("hsql",Collections.EMPTY_MAP);

        m_modelNodeDsm = new InMemoryDSM(m_schemaRegistry);
        m_entityRegistry = new EntityRegistryImpl();
        m_subSystemRegistry = new SubSystemRegistryImpl();
        m_subSystemRegistry.setCompositeSubSystem(new CompositeSubSystemImpl());
        m_modelNodeHelperRegistry = new ModelNodeHelperRegistryImpl(m_schemaRegistry);
        m_expValidator = new DSExpressionValidator(m_schemaRegistry, m_modelNodeHelperRegistry, m_subSystemRegistry);
        m_modelNodeDSMRegistry = new ModelNodeDSMRegistryImpl();
        m_aggregatedDSM = new AggregatedDSM(m_modelNodeDSMRegistry);
        m_persistenceManagerUtil = new ThreadLocalPersistenceManagerUtil(managerFactory);
        m_xmlSubtreeDSM = TestTxUtils.getTxDecoratedDSM(m_persistenceManagerUtil, new XmlSubtreeDSM(m_persistenceManagerUtil,
                m_entityRegistry, m_schemaRegistry, m_modelNodeHelperRegistry, m_subSystemRegistry, m_modelNodeDSMRegistry));
        m_entityModelNodeHelperDeployer = new EntityModelNodeHelperDeployer(m_modelNodeHelperRegistry, m_schemaRegistry, m_aggregatedDSM,
                m_entityRegistry, m_subSystemRegistry);
        m_xmlModelNodeToXmlMapper = new XmlModelNodeToXmlMapperImpl(m_dsmCache, m_schemaRegistry, m_modelNodeHelperRegistry, m_subSystemRegistry,
                m_entityRegistry, null);
        m_server = new NetConfServerImpl(m_schemaRegistry, new RpcRequestConstraintParser(m_schemaRegistry, m_xmlSubtreeDSM, m_expValidator, null));
        m_integrityService = spy(new DataStoreIntegrityServiceImpl(m_server));
        m_datastoreValidator = new DataStoreValidatorImpl(m_schemaRegistry, m_modelNodeHelperRegistry, m_aggregatedDSM, m_integrityService, m_expValidator);
        m_clientInfo = new NetconfClientInfo("unit-test", 1);
        
        registerXmlPath();
        loadSubSystems();
        m_rootModelNodeAggregator = new RootModelNodeAggregatorImpl(m_schemaRegistry, m_modelNodeHelperRegistry, m_aggregatedDSM, m_subSystemRegistry);
        addRootNodeHelpers();
        NbiNotificationHelper nbiNotificationHelper = mock(NbiNotificationHelper.class);
        m_dataStore = new DataStore(StandardDataStores.RUNNING, m_rootModelNodeAggregator, m_subSystemRegistry);
        m_dataStore.setValidator(m_datastoreValidator);
        m_dataStore.setNbiNotificationHelper(nbiNotificationHelper);
        m_server.setRunningDataStore(m_dataStore);
        initializeEntityRegistry();
        loadXmlDataIntoServer();
        m_rootModelNode = m_rootModelNodeAggregator.getModelServiceRoots().get(0);
    }

    protected void loadXmlDataIntoServer() {
		YangUtils.loadXmlDataIntoServer(m_server, getClass().getResource(DEFAULT_XML).getPath());
	}


	protected void loadSubSystems() throws ModelNodeFactoryException, SchemaBuildException {
		YangUtils.deployInMemoryHelpers(getYangFiles(), new LocalSubSystem(), m_modelNodeHelperRegistry,
                m_subSystemRegistry, m_schemaRegistry, m_aggregatedDSM, null, null);
	}
	
    @SuppressWarnings("rawtypes")
    protected void initializeEntityRegistry() throws AnnotationAnalysisException {
        List<Class> classes = new ArrayList<>();
        classes.add(Validation.class);
        classes.add(SomeList.class);
        classes.add(SomeInnerList.class);
        classes.add(OrderByUserList.class);
        EntityRegistryBuilder.updateEntityRegistry(COMPONENT_ID, classes, m_entityRegistry, m_schemaRegistry, m_persistenceManagerUtil.getEntityDataStoreManager(), m_modelNodeDSMRegistry);
        m_modelNodeDSMRegistry.register(COMPONENT_ID, VALIDATION_SCHEMA_PATH, m_xmlSubtreeDSM);
    }
    
    protected void addRootNodeHelpers() {
        ContainerSchemaNode schemaNode = (ContainerSchemaNode) m_schemaRegistry.getDataSchemaNode(VALIDATION_SCHEMA_PATH);
        ChildContainerHelper containerHelper = new RootEntityContainerModelNodeHelper(schemaNode, m_modelNodeHelperRegistry, m_subSystemRegistry, 
                m_schemaRegistry,m_aggregatedDSM);
        m_rootModelNodeAggregator.addModelServiceRootHelper(VALIDATION_SCHEMA_PATH, containerHelper);
    }
    
    private void registerXmlPath(){
        for (SchemaPath schemaPath:SCHEMA_PATHS){
            DataSchemaNode dataSchemaNode = m_schemaRegistry.getDataSchemaNode(schemaPath);
            if (dataSchemaNode instanceof ContainerSchemaNode){
              ChildContainerHelper helper = new XmlContainerModelNodeHelper((ContainerSchemaNode)dataSchemaNode, m_xmlSubtreeDSM, m_schemaRegistry);
              m_modelNodeHelperRegistry.registerChildContainerHelper(COMPONENT_ID, schemaPath.getParent(), schemaPath.getLastComponent(), helper);
            } else if (dataSchemaNode instanceof ListSchemaNode) {
                ChildListHelper helper = new XmlListModelNodeHelper((ListSchemaNode)dataSchemaNode, m_modelNodeHelperRegistry, m_xmlSubtreeDSM, m_schemaRegistry, m_subSystemRegistry);
                m_modelNodeHelperRegistry.registerChildListHelper(COMPONENT_ID, schemaPath.getParent(), schemaPath.getLastComponent(), helper);
            }
            m_modelNodeDSMRegistry.register(COMPONENT_ID, schemaPath, m_xmlSubtreeDSM);
        }
        
    }

    protected NetConfResponse editConfig(String requestXml){
        return editConfig(m_server, m_clientInfo, requestXml,true);
    }

    protected NetConfResponse editConfigAsFalse(String requestXml){
        return editConfig(m_server, m_clientInfo, requestXml,false);
    }
    
    protected void verifyGet(String expectedOutput) throws SAXException, IOException {
        super.verifyGet(m_server, m_clientInfo, expectedOutput);
    }
    
}
