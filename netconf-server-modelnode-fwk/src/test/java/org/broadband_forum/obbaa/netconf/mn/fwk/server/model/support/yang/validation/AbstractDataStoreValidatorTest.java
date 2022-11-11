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

import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.loadAsXml;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.transformToElement;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigElement;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigErrorOptions;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigTestOptions;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorSeverity;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorType;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcResponse;
import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.ValidationHint;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.HintDetails;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.CompositeSubSystemImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.DataStore;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditConfigException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NbiNotificationHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NetConfServerImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NetconfServer;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.AddDefaultDataInterceptor;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildContainerHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeFactoryException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootEntityContainerModelNodeHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregatorImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.DataStoreIntegrityService;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.DataStoreValidatorImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.service.DataStoreIntegrityServiceImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DSExpressionValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationPathBuilder;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.inmemory.InMemoryDSM;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.LocalSubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.YangUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.yang.AbstractValidationTestSetup;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.Before;
import org.junit.BeforeClass;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class AbstractDataStoreValidatorTest extends AbstractValidationTestSetup {

    private static final String YANG_FILE1 = "/datastorevalidatortest/yangs/datastore-validator-test.yang";
    private static final String YANG_FILE2 = "/datastorevalidatortest/yangs/ietf-inet-types.yang";
    private static final String YANG_FILE3 = "/datastorevalidatortest/yangs/datastore-validator-test-yang11.yang";
    private static final String YANG_FILE4 = "/datastorevalidatortest/yangs/datastore-validator-grouping-test.yang";
    private static final String YANG_FILE6 = "/datastorevalidatortest/yangs/identityref-test.yang";
    private static final String YANG_FILE5 = "/datastorevalidatortest/yangs/datastore-extension-functions-test.yang";
    private static final String YANG_FILE7 = "/datastorevalidatortest/yangs/whenMustDSExprValidation-test.yang";
    private static final String TYPE_VALIDATION_YANG_FILE = "/datastorevalidatortest/yangs/datastore-type-validator-test.yang";
    private static final String CHOICE_CASE_YANG_FILE = "/datastorevalidatortest/yangs/datastore-validation-choice-case.yang";
    private static final String YANG_FILE8 = "/datastorevalidatortest/yangs/whenMustDSExprOnCoreLibraryFunction.yang";
    private static final String FORWARDERS_YANG = "/datastorevalidatortest/yangs/forwarders.yang";
    private static final String YANG_FILE9 = "/datastorevalidatortest/yangs/test-interfaces.yang";
    protected static final QName TEST_INTERFACES_QNAME = QName.create("test-interfaces", "interfaces");
    protected static final QName FORWARDERS_QNAME = QName.create("test-forwarders", "2019-11-07", "forwarders");
    protected static final QName VALIDATION_QNAME = QName.create("urn:org:bbf2:pma:validation", "2015-12-14", "validation");
    protected static final QName VALIDATION1_QNAME = QName.create("urn:org:bbf2:pma:validation", "2015-12-14", "validation1");
    protected static final QName VALIDATION2_QNAME = QName.create("urn:org:bbf2:pma:validation", "2015-12-14", "validation2");
    protected static final QName VALIDATION3_QNAME = QName.create("urn:org:bbf2:pma:validation", "2015-12-14", "validation3");
    protected static final QName VALIDATION5_QNAME = QName.create("urn:org:bbf2:pma:validation", "2015-12-14", "validation5");
    protected static final QName VALIDATION6_QNAME = QName.create("urn:org:bbf2:pma:validation", "2015-12-14", "validation6");
    protected static final QName CURRENT_IN_PREDICATES_QNAME = QName.create("urn:org:bbf2:pma:validation", "2015-12-14", "currentInPredicates");
    protected static final QName TYPE_VALIDATION_QNAME = QName.create("urn:org:bbf:pma:validation", "2019-02-05", "type-validation");
    protected static final QName VALIDATION_CHOICE_CASE_QNAME = QName.create("urn:org:bbf:pma:choice-case-test", "2019-02-25", "choice-container");
    protected static final QName VALIDATION7_QNAME = QName.create("urn:org:bbf2:pma:validation", "2015-12-14", "decimal64-type-validation7");
    protected static final QName VALIDATION11_QNAME = QName.create("urn:org:bbf2:pma:validation", "2015-12-14", "stateValue1");
    protected static final QName VALIDATION9_QNAME = QName.create("urn:org:bbf2:pma:validation", "2015-12-14", "order-by-user-validation");
    protected static final QName VALIDATION10_QNAME = QName.create("urn:org:bbf2:pma:validation", "2015-12-14", "container-leaf-with-default");
    protected static final QName WHENMUSTCONTAINER1_QNAME = QName.create("urn:org:bbf2:pma:whenMustDSExprValidation-test", "2015-12-14", "whenMustRefersSiblings");
    protected static final QName VALIDATION_STRINGCUSTOMSUBTYPE_QNAME = QName.create("urn:org:bbf2:pma:validation", "2015-12-14", "validate-customsubtype-string-defaultvalue");
    protected static final QName VALIDATION_STRINGCUSTOMTYPE_QNAME = QName.create("urn:org:bbf2:pma:validation", "2015-12-14", "validate-customtype-string-defaultvalue");

    protected static final QName OTHERROOTCONTAINER_QNAME = QName.create("urn:org:bbf2:pma:whenMustDSExprValidation-test", "2015-12-14", "otherRootContainer");
    protected static final QName EX_FUNCTION_CANISTER_QNAME = QName.create("urn:opendaylight:datastore-extension-functions-test", "2019-02-07", "exfunctionCanister");    
    protected static final QName VALIDATION_LEAFREF_QNAME1 = QName.create("urn:org:bbf2:pma:leafref:validation", "2019-02-11", "rootNode");
    protected static final QName VALIDATION_LEAFREF_QNAME2 = QName.create("urn:org:bbf2:pma:leafref:validation", "2019-02-11", "otherRootNode");
    protected static final QName CHOICE_VALIIDATION_ROOTNODE_QNAME = QName.create("urn:org:bbf2:pma:choice:validation", "2019-02-17", "rootNode");
    protected static final QName WHENMUST_CONTAINER_QNAME = QName.create("urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction", "2015-12-14", "whenMustContainer");
    protected static final QName LEAF_LIST_VALIDATION_QNAME = QName.create("urn:org:bbf2:pma:validation", "2015-12-14", "leaf-list-validation-container");

    public static final String YANG11_NS = "urn:org:bbf2:pma:validation-yang11";
    protected static final QName VALIDATION4_QNAME = QName.create(YANG11_NS, "2015-12-14", "validation-yang11");
    protected static final QName VALIDATION8_QNAME = QName.create("urn:org:bbf2:pma:identityref-test", "2015-12-14", "identityrefContainer");
    protected static final SchemaPath TEST_INTERFACES_SCHEMA_PATH = SchemaPath.create(true, TEST_INTERFACES_QNAME);
    protected static final SchemaPath VALIDATION_SCHEMA_PATH = SchemaPath.create(true, VALIDATION_QNAME);
    protected static final SchemaPath VALIDATION1_SCHEMA_PATH = SchemaPath.create(true, VALIDATION1_QNAME);
    protected static final SchemaPath VALIDATION2_SCHEMA_PATH = SchemaPath.create(true, VALIDATION2_QNAME);
    protected static final SchemaPath VALIDATION3_SCHEMA_PATH = SchemaPath.create(true, VALIDATION3_QNAME);
    protected static final SchemaPath VALIDATION4_SCHEMA_PATH = SchemaPath.create(true, VALIDATION4_QNAME);
    protected static final SchemaPath VALIDATION5_SCHEMA_PATH = SchemaPath.create(true, VALIDATION5_QNAME);
    protected static final SchemaPath VALIDATION6_SCHEMA_PATH = SchemaPath.create(true, VALIDATION6_QNAME);
    protected static final SchemaPath CURRENT_IN_PREDICATES_SCHEMA_PATH = SchemaPath.create(true, CURRENT_IN_PREDICATES_QNAME);
    protected static final SchemaPath TYPE_VALIDATION_SCHEMA_PATH = SchemaPath.create(true, TYPE_VALIDATION_QNAME);
    protected static final SchemaPath VALIDATION7_SCHEMA_PATH = SchemaPath.create(true, VALIDATION7_QNAME);
    protected static final SchemaPath VALIDATION_CHOICE_CASE_SCHEMA_PATH = SchemaPath.create(true, VALIDATION_CHOICE_CASE_QNAME);
    protected static final SchemaPath VALIDATION11_SCHEMA_PATH = SchemaPath.create(true, VALIDATION11_QNAME);
    protected static final SchemaPath MUST_PRESENCE_VALIDATION_SCHEMA_PATH = SchemaPathBuilder.fromString("(urn:org:bbf2:pma:validation?revision=2015-12-14)must-presence-validation");
    protected static final SchemaPath VALIDATION9_SCHEMA_PATH = SchemaPath.create(true, VALIDATION9_QNAME);
    protected static final SchemaPath VALIDATION10_SCHEMA_PATH = SchemaPath.create(true, VALIDATION10_QNAME);
    protected static final SchemaPath WHENMUST_REFERS_SIBLINGS_PATH = SchemaPath.create(true, WHENMUSTCONTAINER1_QNAME);
    protected static final SchemaPath OTHER_ROOT_CONTAINER_PATH = SchemaPath.create(true, OTHERROOTCONTAINER_QNAME);
    protected static final SchemaPath VALIDATION8_SCHEMA_PATH = SchemaPath.create(true, VALIDATION8_QNAME);
    protected static final SchemaPath EX_FUNCTION_CANISTER_SCHEMA_PATH = SchemaPath.create(true, EX_FUNCTION_CANISTER_QNAME);
    protected static final SchemaPath VALIDATION_LEAFREF1_SCHEMA_PATH = SchemaPath.create(true, VALIDATION_LEAFREF_QNAME1);
    protected static final SchemaPath VALIDATION_LEAFREF2_SCHEMA_PATH = SchemaPath.create(true, VALIDATION_LEAFREF_QNAME2);
    protected static final SchemaPath VALIDATION_STRINGCUSTOMSUBTYPE_SCHEMA_PATH = SchemaPath.create(true, VALIDATION_STRINGCUSTOMSUBTYPE_QNAME);
    protected static final SchemaPath VALIDATION_STRINGCUSTOMTYPE_SCHEMA_PATH = SchemaPath.create(true, VALIDATION_STRINGCUSTOMTYPE_QNAME);
    protected static final SchemaPath CHOICE_VALIIDATION_ROOTNODE_SCHEMA_PATH = SchemaPath.create(true, CHOICE_VALIIDATION_ROOTNODE_QNAME);
    protected static final SchemaPath WHENMUST_CONTAINER_SCHEMA_PATH = SchemaPath.create(true, WHENMUST_CONTAINER_QNAME);
    protected static final SchemaPath FORWARDER_CONTAINER_SCHEMA_PATH = SchemaPath.create(true, FORWARDERS_QNAME);
    protected static final SchemaPath LEAF_LIST_VALIDATION_SCHEMA_PATH = SchemaPath.create(true, LEAF_LIST_VALIDATION_QNAME);

    private static final String DEFAULT_XML = "/datastorevalidatortest/yangs/datastore-validator-defaultxml.xml";
    protected static final String NAMESPACE = "urn:org:bbf2:pma:validation";
    protected static final String MESSAGE_ID = "1";

    protected RootModelNodeAggregator m_rootModelNodeAggregator;
    protected ModelNodeHelperRegistry m_modelNodeHelperRegistry;
    protected SubSystemRegistry m_subSystemRegistry;
    protected NetConfServerImpl m_server;
    protected DataStoreValidatorImpl m_datastoreValidator;
    protected DataStore m_dataStore;
    protected static SchemaRegistry m_schemaRegistry;
    protected NetconfClientInfo m_clientInfo;
    protected InMemoryDSM m_modelNodeDsm;
    protected AddDefaultDataInterceptor m_addDefaultDataInterceptor;
    protected ModelNode m_rootModelNode;
    protected DataStoreIntegrityService m_integrityService;
    protected Map<SchemaPath, ValidationHint> m_deviceHints = new HashMap<>();
    protected Map<SchemaPath, HintDetails> m_globalHints = new HashMap<>();
    protected DataStoreValidationPathBuilder m_pathBuilder;
    protected SubSystem m_localSubSystemSpy;

    @BeforeClass
    public static void initializeOnce() throws SchemaBuildException {
        List<YangTextSchemaSource> yangFiles = TestUtil.getByteSources(getYang());
        m_schemaRegistry = new SchemaRegistryImpl(yangFiles, Collections.emptySet(), Collections.emptyMap(), new NoLockService());
        m_schemaRegistry.setName(SchemaRegistry.GLOBAL_SCHEMA_REGISTRY);
    }
    
    @Before
    public void setUp() throws ModelNodeInitException, SchemaBuildException {
        m_clientInfo = new NetconfClientInfo("test", 1, 1);
        m_modelNodeDsm = spy(new InMemoryDSM(m_schemaRegistry));
        m_modelNodeHelperRegistry = new ModelNodeHelperRegistryImpl(m_schemaRegistry);
        m_subSystemRegistry = getSubSystemRegistry();
        m_subSystemRegistry.setCompositeSubSystem(new CompositeSubSystemImpl());
        m_expValidator = new DSExpressionValidator(m_schemaRegistry, m_modelNodeHelperRegistry, m_subSystemRegistry);
        m_server = getNcServer();
        m_integrityService = new DataStoreIntegrityServiceImpl(m_server);
        m_datastoreValidator = new DataStoreValidatorImpl(m_schemaRegistry, m_modelNodeHelperRegistry, m_modelNodeDsm, m_integrityService, m_expValidator);
        m_expValidator = new DSExpressionValidator(m_schemaRegistry, m_modelNodeHelperRegistry, m_subSystemRegistry);
        m_pathBuilder = new DataStoreValidationPathBuilder(m_schemaRegistry, m_modelNodeHelperRegistry);
    }

    protected SubSystemRegistry getSubSystemRegistry() {
        return new SubSystemRegistryImpl();
    }

    protected NetConfServerImpl getNcServer(){
        return spy(new NetConfServerImpl(m_schemaRegistry));
    }

    @Override
    protected SchemaRegistry getSchemaRegistry() throws SchemaBuildException{
        List<YangTextSchemaSource> yangFiles = TestUtil.getByteSources(getYang());
        return new SchemaRegistryImpl(yangFiles, Collections.emptySet(), Collections.emptyMap(), new NoLockService());
    }
    
    protected static  List<String> getYang() {
        List<String> fileList = new ArrayList<String>();
        fileList.add(YANG_FILE1);
        fileList.add(YANG_FILE2);
        fileList.add(YANG_FILE3);
        fileList.add(YANG_FILE4);
        fileList.add(YANG_FILE6);
        fileList.add(YANG_FILE5);
        fileList.add(YANG_FILE7);
        fileList.add(YANG_FILE8);
        fileList.add(YANG_FILE9);
        fileList.add(FORWARDERS_YANG);
        fileList.add(TYPE_VALIDATION_YANG_FILE);
        fileList.add(CHOICE_CASE_YANG_FILE);
        fileList.add("/datastorevalidatortest/yangs/ietf-yang-schema-mount@2017-10-09.yang");
        fileList.add("/datastorevalidatortest/yangs/dummy-extension.yang");
        fileList.add("/datastorevalidatortest/yangs/mountPointTest.yang");
        fileList.add("/datastorevalidatortest/yangs/datastore-validator-augment-test.yang");
        fileList.add("/datastorevalidatortest/yangs/rpc-output-augmentation-validation-test.yang");
        fileList.add("/datastorevalidatortest/yangs/datastore-validator-leafref-test.yang");
        fileList.add("/datastorevalidatortest/yangs/datastore-validator-augment-test11.yang");
        fileList.add("/datastorevalidatortest/yangs/datastore-validator-augment-test11-replicate-namespace1.yang");
        fileList.add("/datastorevalidatortest/yangs/datastore-validator-augment-test11-replicate-namespace2.yang");
        fileList.add("/datastorevalidatortest/yangs/anv-ual-test.yang");
        fileList.add("/datastorevalidatortest/yangs/nc-stack-extensions.yang");
        return fileList;
    }


    protected String getXml() {
        return DEFAULT_XML;
    }

    protected SchemaPath getSchemaPath() {
        return VALIDATION_SCHEMA_PATH;
    }

    protected void getModelNode() throws ModelNodeInitException {

        try {
            YangUtils.deployInMemoryHelpers(getYang(), getSubSystem(), m_modelNodeHelperRegistry, m_subSystemRegistry, m_schemaRegistry, m_modelNodeDsm, null, null, m_deviceHints, m_globalHints);
        } catch (ModelNodeFactoryException e) {
            throw new ModelNodeInitException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        m_rootModelNodeAggregator = new RootModelNodeAggregatorImpl(m_schemaRegistry, m_modelNodeHelperRegistry, m_modelNodeDsm, m_subSystemRegistry);
        addRootNodeHelpers();
        NbiNotificationHelper nbiNotificationHelper = mock(NbiNotificationHelper.class);
        m_dataStore = new DataStore(StandardDataStores.RUNNING, m_rootModelNodeAggregator, m_subSystemRegistry);
        m_dataStore.setValidator(m_datastoreValidator);
        m_dataStore.setNbiNotificationHelper(nbiNotificationHelper);
        m_server.setRunningDataStore(m_dataStore);
        loadDefaultXml();
        if (!m_rootModelNodeAggregator.getModelServiceRoots().isEmpty()) {
            m_rootModelNode = m_rootModelNodeAggregator.getModelServiceRoots().get(0);
        }
        DataStoreValidationUtil.setRootModelNodeAggregatorInCache(m_rootModelNodeAggregator);

    }

    protected void loadDefaultXml(){
        YangUtils.loadXmlDataIntoServer(m_server, getClass().getResource(getXml()).getPath());
    }

    protected SubSystem getSubSystem() {
        m_localSubSystemSpy = spy(new LocalSubSystem());
        return m_localSubSystemSpy;
    }

    protected void addRootContainerNodeHelpers(SchemaPath dataNodeSchemaPath) {
        ContainerSchemaNode schemaNode = (ContainerSchemaNode) m_schemaRegistry.getDataSchemaNode(dataNodeSchemaPath);
        ChildContainerHelper containerHelper = null;
        if (schemaNode != null) {
            containerHelper = new RootEntityContainerModelNodeHelper(schemaNode, m_modelNodeHelperRegistry,
                    m_subSystemRegistry, m_schemaRegistry, m_modelNodeDsm);
            m_rootModelNodeAggregator.addModelServiceRootHelper(dataNodeSchemaPath, containerHelper);
        }
    }

    protected void addRootNodeHelpers() {
        addRootContainerNodeHelpers(getSchemaPath());
        addRootContainerNodeHelpers(VALIDATION_CHOICE_CASE_SCHEMA_PATH);
        addRootContainerNodeHelpers(VALIDATION1_SCHEMA_PATH);
        addRootContainerNodeHelpers(VALIDATION2_SCHEMA_PATH);
        addRootContainerNodeHelpers(VALIDATION3_SCHEMA_PATH);
        addRootContainerNodeHelpers(VALIDATION4_SCHEMA_PATH);
        addRootContainerNodeHelpers(VALIDATION5_SCHEMA_PATH);
        addRootContainerNodeHelpers(VALIDATION6_SCHEMA_PATH);
        addRootContainerNodeHelpers(CURRENT_IN_PREDICATES_SCHEMA_PATH);
        addRootContainerNodeHelpers(VALIDATION7_SCHEMA_PATH);
        addRootContainerNodeHelpers(VALIDATION8_SCHEMA_PATH);
        addRootContainerNodeHelpers(VALIDATION9_SCHEMA_PATH);
        addRootContainerNodeHelpers(VALIDATION10_SCHEMA_PATH);
        addRootContainerNodeHelpers(VALIDATION11_SCHEMA_PATH);
        addRootContainerNodeHelpers(MUST_PRESENCE_VALIDATION_SCHEMA_PATH);
        addRootContainerNodeHelpers(TYPE_VALIDATION_SCHEMA_PATH);
        addRootContainerNodeHelpers(WHENMUST_REFERS_SIBLINGS_PATH);
        addRootContainerNodeHelpers(OTHER_ROOT_CONTAINER_PATH);
        addRootContainerNodeHelpers(EX_FUNCTION_CANISTER_SCHEMA_PATH);
        addRootContainerNodeHelpers(CHOICE_VALIIDATION_ROOTNODE_SCHEMA_PATH);
        addRootContainerNodeHelpers(VALIDATION_LEAFREF1_SCHEMA_PATH);
        addRootContainerNodeHelpers(VALIDATION_LEAFREF2_SCHEMA_PATH);
        addRootContainerNodeHelpers(VALIDATION_STRINGCUSTOMSUBTYPE_SCHEMA_PATH);       
        addRootContainerNodeHelpers(VALIDATION_STRINGCUSTOMTYPE_SCHEMA_PATH);       
        addRootContainerNodeHelpers(WHENMUST_CONTAINER_SCHEMA_PATH);
        addRootContainerNodeHelpers(LEAF_LIST_VALIDATION_SCHEMA_PATH);
        addRootContainerNodeHelpers(TEST_INTERFACES_SCHEMA_PATH);
        addRootContainerNodeHelpers(FORWARDER_CONTAINER_SCHEMA_PATH);
    }

    protected void testPass(String editConfigElement) throws ModelNodeInitException {
        Element configElement = TestUtil.loadAsXml(editConfigElement);
        testPass(configElement);
    }

    protected void testPass(Element configElement) throws ModelNodeInitException {
        getModelNode();
        initialiseInterceptor();
        NetConfResponse response = TestUtil.sendEditConfig(m_server, m_clientInfo, configElement, MESSAGE_ID);
        assertTrue(response.isOk());
    }

    protected void testFail(String editConfigElement, NetconfRpcErrorTag errorTag, NetconfRpcErrorType errorType,
            NetconfRpcErrorSeverity errorSeverity, String errorAppTag, String errorMessage, String errorPath) throws
    ModelNodeInitException {
        Element configElement = TestUtil.loadAsXml(editConfigElement);
        testValidate(errorTag, errorType, errorSeverity, errorAppTag, errorMessage, errorPath, configElement);
    }

    protected void testFailForUniqueType(Element editConfigElement, NetconfRpcErrorTag errorTag, NetconfRpcErrorType errorType,
            NetconfRpcErrorSeverity errorSeverity, String errorAppTag, String errorMessage, String errorPath, Element expectedErrorInfo ) throws
    ModelNodeInitException, NetconfMessageBuilderException, SAXException, IOException {
        testValidateForUnique(errorTag, errorType, errorSeverity, errorAppTag, errorMessage, errorPath, expectedErrorInfo, editConfigElement);
    }

    @SuppressWarnings("deprecation")
    private void testValidate(NetconfRpcErrorTag errorTag, NetconfRpcErrorType errorType,
            NetconfRpcErrorSeverity errorSeverity, String errorAppTag, String errorMessage, String errorPath,
            Element configElement) throws ModelNodeInitException {
        NetconfRpcError netconfRpcError;
        getModelNode();
        initialiseInterceptor();
        NetConfResponse response = null;
        try {
            response = TestUtil.sendEditConfig(m_server, m_clientInfo, configElement, MESSAGE_ID);
        } catch (Exception e) {
            assertTrue(e instanceof EditConfigException);
            netconfRpcError = ((EditConfigException)e).getRpcError();
            assertNetconfRpcError(errorTag, errorType, errorSeverity, errorAppTag, errorMessage, errorPath, netconfRpcError);
        }
        if (response!=null) {
            assertFalse(response.isOk());
            netconfRpcError = response.getErrors().get(0);
            assertNetconfRpcError(errorTag, errorType, errorSeverity, errorAppTag, errorMessage, errorPath, netconfRpcError);
        }
    }
    @SuppressWarnings("deprecation")
    private void testValidateForUnique(NetconfRpcErrorTag errorTag, NetconfRpcErrorType errorType,
            NetconfRpcErrorSeverity errorSeverity, String errorAppTag, String errorMessage, String errorPath, Element expectedErrorInfo,
            Element configElement) throws ModelNodeInitException, NetconfMessageBuilderException, SAXException, IOException {
        NetconfRpcError netconfRpcError;
        getModelNode();
        initialiseInterceptor();
        NetConfResponse response = null;
        try {
            response = TestUtil.sendEditConfig(m_server, m_clientInfo, configElement, MESSAGE_ID);
        } catch (Exception e) {
            assertTrue(e instanceof EditConfigException);
            netconfRpcError = ((EditConfigException)e).getRpcError();
            assertNetconfRpcErrorForUnique(errorTag, errorType, errorSeverity, errorAppTag, errorMessage, errorPath, netconfRpcError, expectedErrorInfo);
        }
        if (response!=null) {
            assertFalse(response.isOk());
            netconfRpcError = response.getErrors().get(0);
            assertNetconfRpcErrorForUnique(errorTag, errorType, errorSeverity, errorAppTag, errorMessage, errorPath, netconfRpcError, expectedErrorInfo);
        }
    }

    protected void testFail(Element editConfigElement, NetconfRpcErrorTag errorTag, NetconfRpcErrorType errorType,
            NetconfRpcErrorSeverity errorSeverity, String errorAppTag, String errorMessage, String errorPath) throws ModelNodeInitException {
        testValidate(errorTag, errorType, errorSeverity, errorAppTag, errorMessage, errorPath, editConfigElement);
    }

    protected void assertNetconfRpcErrorForUnique(NetconfRpcErrorTag errorTag, NetconfRpcErrorType errorType, NetconfRpcErrorSeverity errorSeverity,
            String errorAppTag, String errorMessage, String errorPath, NetconfRpcError netconfRpcError, Element expectedErrorInfo) throws NetconfMessageBuilderException, SAXException, IOException {
        assertEquals(errorTag, netconfRpcError.getErrorTag());
        assertEquals(errorType,netconfRpcError.getErrorType());
        assertEquals(errorSeverity, netconfRpcError.getErrorSeverity());
        assertEquals(errorAppTag, netconfRpcError.getErrorAppTag());
        assertEquals(errorMessage, netconfRpcError.getErrorMessage());
        assertEquals(errorPath,netconfRpcError.getErrorPath());
        TestUtil.assertXMLEquals((expectedErrorInfo), (netconfRpcError.getErrorInfo()));
    }

    protected void assertNetconfRpcError(NetconfRpcErrorTag errorTag, NetconfRpcErrorType errorType, NetconfRpcErrorSeverity errorSeverity,
            String errorAppTag, String errorMessage, String errorPath, NetconfRpcError netconfRpcError) {
        assertEquals(errorTag, netconfRpcError.getErrorTag());
        assertEquals(errorType,netconfRpcError.getErrorType());
        assertEquals(errorSeverity, netconfRpcError.getErrorSeverity());
        assertEquals(errorAppTag, netconfRpcError.getErrorAppTag());
        assertEquals(errorMessage, netconfRpcError.getErrorMessage());
        assertEquals(errorPath,netconfRpcError.getErrorPath());
    }

    protected void initialiseInterceptor() {
        m_addDefaultDataInterceptor = new AddDefaultDataInterceptor(m_modelNodeHelperRegistry, m_schemaRegistry, m_expValidator);
        m_addDefaultDataInterceptor.init();
    }

    protected EditConfigRequest createRequest(String xmlRequest) {
        return new EditConfigRequest().setTargetRunning().setTestOption(EditConfigTestOptions.SET)
                .setErrorOption(EditConfigErrorOptions.STOP_ON_ERROR)
                .setConfigElement(new EditConfigElement().addConfigElementContent(loadAsXml(xmlRequest)));
    }

    public static EditConfigRequest createRequestFromString(String xmlRequest) {
        return new EditConfigRequest().setTargetRunning().setTestOption(EditConfigTestOptions.SET)
                .setErrorOption(EditConfigErrorOptions.STOP_ON_ERROR)
                .setConfigElement(new EditConfigElement().addConfigElementContent(transformToElement(xmlRequest)));
    }
    
    public static EditConfigRequest createRequestFromString(String xmlRequest, String defaultOperation) {
        return new EditConfigRequest().setTargetRunning().setTestOption(EditConfigTestOptions.SET)
                .setErrorOption(EditConfigErrorOptions.STOP_ON_ERROR).setDefaultOperation(defaultOperation)
                .setConfigElement(new EditConfigElement().addConfigElementContent(transformToElement(xmlRequest)));
    }


    protected void addOutputElements(Element doc,NetconfRpcResponse response){
        List<Element> elements = DocumentUtils.getChildElements(doc);
        for (Element element:elements){
            response.addRpcOutputElement(element);
        }
    }

    protected void verifyGet(String expectedOutput) throws SAXException, IOException {
        super.verifyGet(m_server, m_clientInfo, expectedOutput);
    }

    @Override
    public void setup() throws Exception {

    }

    protected InMemoryDSM getInMemoryDSM(){
        return m_modelNodeDsm;
    }

    protected SchemaPath buildSchemaPath(String parent, String child) {
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

    public void sendEditConfigAndVerifyGet(String requestXml, String espectedResponse)
        throws SAXException, IOException {
        editConfig(m_server, m_clientInfo, requestXml, true);
        verifyGet(m_server, m_clientInfo, espectedResponse);
    }

    public void sendEditConfigAndVerifyGet(NetconfServer server, NetconfClientInfo client,
        String requestXml, String espectedResponse)
        throws SAXException, IOException {
        editConfig(server, client, requestXml, true);
        verifyGet(server, client, espectedResponse);
    }

    public void sendEditConfigAndVerifyFailure(String requestXml,
        String expectedErrorMessageXml,
        String expectedErrorPath) {
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
        assertFalse(response.isOk());
        assertEquals(expectedErrorMessageXml, response.getErrors().get(0).getErrorMessage());
        assertEquals(expectedErrorPath, response.getErrors().get(0).getErrorPath());
    }

    public void sendEditConfigAndVerifyFailure(NetconfServer server, NetconfClientInfo client,
        String requestXml,
        String expectedErrorMessageXml,
        String expectedErrorPath) {
        NetConfResponse response = editConfig(server, client, requestXml, false);
        assertFalse(response.isOk());
        assertEquals(expectedErrorMessageXml, response.getErrors().get(0).getErrorMessage());
        assertEquals(expectedErrorPath, response.getErrors().get(0).getErrorPath());
    }
}
