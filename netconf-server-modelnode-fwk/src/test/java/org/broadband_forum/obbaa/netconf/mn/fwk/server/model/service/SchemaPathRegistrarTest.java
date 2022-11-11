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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.service;

import static org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder.fromString;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.NotifSwitchUtil.ENABLE_NEW_NOTIF_STRUCTURE;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.NotifSwitchUtil.resetSystemProperty;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.NotifSwitchUtil.setSystemPropertyAndReturnResetValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.jxpath.ri.compiler.Expression;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder;
import org.broadband_forum.obbaa.netconf.mn.fwk.ChangeTreeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.ReferringNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.ReferringNodes;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.ValidationHint;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.DataStore;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditConfigException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditConfigTestFailedException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.LockedByOtherSessionException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemValidationException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.PersistenceException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.validation.AbstractDataStoreValidatorTest;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;

@RunWith(RequestScopeJunitRunner.class)
public class SchemaPathRegistrarTest extends AbstractDataStoreValidatorTest {
    private static final String VALIDATION = "validation";
    private static final String DS_VALIDATOR_TEST_YANG_REVISION = "2015-12-14";
    private static final String DS_VALIDATOR_TEST_YANG_NS = "urn:org:bbf2:pma:validation";
    private static final String SAMPLE_CONTAINER = "sampleContainer";

    static QName createQName(String localName){
        return QName.create("urn:org:bbf2:pma:extension:test", DS_VALIDATOR_TEST_YANG_REVISION, localName);
    }

    static QName createQNameForWhenConstraint(String localName){
        return QName.create("when-constraint", "2020-06-05", localName);
    }

    static QName createQNameForMustConstraint(String localName){
        return QName.create("must-constraint", "2021-04-13", localName);
    }
    public static final QName SOMEABS_QNAME = createQName("someAbs");
    public static final QName VALIDATION_EXT_QNAME = createQName(VALIDATION);
    private static final QName EXTLEAF_QNAME = createQName("extLeaf");
    private static final QName EXTLEAF1_QNAME = createQName("extLeaf1");
    private static final QName EXTLEAF2_QNAME = createQName("extLeaf2");
    private static final QName EXTLEAF3_QNAME = createQName("extLeaf3");
    private static final QName SOME_CONTAINER_QNAME = createQName("someContainer");
    private static final QName REF_QNAME = createQName("ref");
    
    private static final QName TYPE_QNAME = QName.create(DS_VALIDATOR_TEST_YANG_NS, DS_VALIDATOR_TEST_YANG_REVISION, "type");
    private static final QName INTFSTATE_QNAME = QName.create(DS_VALIDATOR_TEST_YANG_NS, DS_VALIDATOR_TEST_YANG_REVISION, "testinterface-state");
    private static final QName TESTNOTIF_QNAME = QName.create(DS_VALIDATOR_TEST_YANG_NS, DS_VALIDATOR_TEST_YANG_REVISION, "testNotification");
    private static final QName TESTNOTIF_CONTAINER_QNAME = QName.create(DS_VALIDATOR_TEST_YANG_NS, DS_VALIDATOR_TEST_YANG_REVISION, "notif-container");
    private static final QName TESTNOTIF_LEAF_QNAME = QName.create("urn:opendaylight:datastore-validator-augment-test", "2018-03-07", "sample-leaf");

    private static final String COMPONENTID = "test-when-constraint-schema-path?revision=2020-06-05";
    private static final QName WHEN_CONSTRAINT_ROOT_QNAME = createQNameForWhenConstraint("test-container");
    private static final SchemaPath WHEN_CONSTRAINT_ROOT_SCHEMAPATH = SchemaPath.create(true, WHEN_CONSTRAINT_ROOT_QNAME);

    private static final QName WHEN_LEAF_QNAME = createQNameForWhenConstraint("when-leaf");
    private static final SchemaPath WHEN_LEAF_SCHEMAPATH = buildSchemaPath(WHEN_CONSTRAINT_ROOT_SCHEMAPATH, WHEN_LEAF_QNAME);

    private static final QName TEST_LEAF_QNAME = createQNameForWhenConstraint("test-leaf");
    private static final SchemaPath TEST_LEAF_SCHEMAPATH = buildSchemaPath(WHEN_CONSTRAINT_ROOT_SCHEMAPATH, TEST_LEAF_QNAME);

    private static final QName STAET_CONTAINER_QNAME = createQNameForWhenConstraint("state-container");
    private static final SchemaPath STAET_CONTAINER_SCHEMAPATH = buildSchemaPath(WHEN_CONSTRAINT_ROOT_SCHEMAPATH, STAET_CONTAINER_QNAME);

    private static final QName STAET_CONTAINER_ACTION_QNAME = createQNameForWhenConstraint("state-action");
    private static final SchemaPath STAET_CONTAINER_ACTION_SCHEMAPATH = buildSchemaPath(STAET_CONTAINER_SCHEMAPATH,
            STAET_CONTAINER_ACTION_QNAME);

    private static final QName STAET_CONTAINER_ACTION_INPUT_QNAME = createQNameForWhenConstraint("input");
    private static final SchemaPath STAET_CONTAINER_ACTION_INPUT_SCHEMAPATH = buildSchemaPath(STAET_CONTAINER_ACTION_SCHEMAPATH, STAET_CONTAINER_ACTION_INPUT_QNAME);

    private static final QName STAET_CONTAINER_ACTION_INPUT_DEVICE_QNAME = createQNameForWhenConstraint("device");
    private static final SchemaPath STAET_CONTAINER_ACTION_INPUT_DEVICE_SCHEMAPATH = buildSchemaPath(STAET_CONTAINER_ACTION_INPUT_SCHEMAPATH, STAET_CONTAINER_ACTION_INPUT_DEVICE_QNAME);

    private static final QName STAET_CONTAINER_ACTION_INPUT_DEVICE_DUID_QNAME = createQNameForWhenConstraint("duid");
    private static final SchemaPath STAET_CONTAINER_ACTION_INPUT_DEVICE_DUID_SCHEMAPATH = buildSchemaPath(STAET_CONTAINER_ACTION_INPUT_DEVICE_SCHEMAPATH, STAET_CONTAINER_ACTION_INPUT_DEVICE_DUID_QNAME);

    private static final QName STAET_CONTAINER_ACTION_INPUT_DEVICE_DEVICEID_QNAME = createQNameForWhenConstraint("device-id");
    private static final SchemaPath STAET_CONTAINER_ACTION_INPUT_DEVICE_DEVICEID_SCHEMAPATH = buildSchemaPath(STAET_CONTAINER_ACTION_INPUT_DEVICE_SCHEMAPATH, STAET_CONTAINER_ACTION_INPUT_DEVICE_DEVICEID_QNAME);

    private static final QName WHEN_CONSTRAINT_RPC_ROOT_QNAME = createQNameForWhenConstraint("when-constraint-rpc");
    private static final SchemaPath WHEN_CONSTRAINT_RPC_ROOT_SCHEMAPATH = SchemaPath.create(true, WHEN_CONSTRAINT_RPC_ROOT_QNAME);
    private static final SchemaPath WHEN_CONSTRAINT_RPC_INPUT_SCHEMAPATH = buildSchemaPath(WHEN_CONSTRAINT_RPC_ROOT_SCHEMAPATH, STAET_CONTAINER_ACTION_INPUT_QNAME);
    private static final SchemaPath WHEN_CONSTRAINT_RPC_INPUT_DEVCE_SCHEMAPATH = buildSchemaPath(WHEN_CONSTRAINT_RPC_INPUT_SCHEMAPATH, STAET_CONTAINER_ACTION_INPUT_DEVICE_QNAME);
    private static final SchemaPath WHEN_CONSTRAINT_RPC_INPUT_DEVCE_DUID_SCHEMAPATH = buildSchemaPath(WHEN_CONSTRAINT_RPC_INPUT_DEVCE_SCHEMAPATH,
            STAET_CONTAINER_ACTION_INPUT_DEVICE_DUID_QNAME);

    private static final SchemaPath WHEN_CONSTRAINT_RPC_INPUT_DEVCE_DEVICEID_SCHEMAPATH =
            buildSchemaPath(WHEN_CONSTRAINT_RPC_INPUT_DEVCE_SCHEMAPATH, STAET_CONTAINER_ACTION_INPUT_DEVICE_DEVICEID_QNAME);

    private static final String MUST_COMPONENTID = "test-must-constraint-schema-path?revision=2021-04-13";
    private static final QName MUST_CONSTRAINT_ROOT_QNAME = createQNameForMustConstraint("test-container");
    private static final SchemaPath MUST_CONSTRAINT_ROOT_SCHEMAPATH = SchemaPath.create(true, MUST_CONSTRAINT_ROOT_QNAME);

    private static final QName MUST_LEAF_QNAME = createQNameForMustConstraint("must-leaf");
    private static final SchemaPath MUST_LEAF_SCHEMAPATH = buildSchemaPath(MUST_CONSTRAINT_ROOT_SCHEMAPATH, MUST_LEAF_QNAME);

    private static final QName MUST_TEST_LEAF_QNAME = createQNameForMustConstraint("test-leaf");
    private static final SchemaPath MUST_TEST_LEAF_SCHEMAPATH = buildSchemaPath(MUST_CONSTRAINT_ROOT_SCHEMAPATH, MUST_TEST_LEAF_QNAME);

    private static final QName MUST_STATE_CONTAINER_QNAME = createQNameForMustConstraint("state-container");
    private static final SchemaPath MUST_STATE_CONTAINER_SCHEMAPATH = buildSchemaPath(MUST_CONSTRAINT_ROOT_SCHEMAPATH, MUST_STATE_CONTAINER_QNAME);

    private static final QName MUST_STATE_CONTAINER_ACTION_QNAME = createQNameForMustConstraint("state-action");
    private static final SchemaPath MUST_STATE_CONTAINER_ACTION_SCHEMAPATH = buildSchemaPath(MUST_STATE_CONTAINER_SCHEMAPATH,
            MUST_STATE_CONTAINER_ACTION_QNAME);

    private static final QName MUST_STATE_CONTAINER_ACTION_INPUT_QNAME = createQNameForMustConstraint("input");
    private static final SchemaPath MUST_STATE_CONTAINER_ACTION_INPUT_SCHEMAPATH = buildSchemaPath(MUST_STATE_CONTAINER_ACTION_SCHEMAPATH, MUST_STATE_CONTAINER_ACTION_INPUT_QNAME);

    private static final QName MUST_STATE_CONTAINER_ACTION_INPUT_DEVICE_QNAME = createQNameForMustConstraint("device");
    private static final SchemaPath MUST_STATE_CONTAINER_ACTION_INPUT_DEVICE_SCHEMAPATH = buildSchemaPath(MUST_STATE_CONTAINER_ACTION_INPUT_SCHEMAPATH, MUST_STATE_CONTAINER_ACTION_INPUT_DEVICE_QNAME);

    private static final QName MUST_STATE_CONTAINER_ACTION_INPUT_DEVICE_DUID_QNAME = createQNameForMustConstraint("duid");
    private static final SchemaPath MUST_STATE_CONTAINER_ACTION_INPUT_DEVICE_DUID_SCHEMAPATH = buildSchemaPath(MUST_STATE_CONTAINER_ACTION_INPUT_DEVICE_SCHEMAPATH, MUST_STATE_CONTAINER_ACTION_INPUT_DEVICE_DUID_QNAME);

    private static final QName MUST_STATE_CONTAINER_ACTION_INPUT_DEVICE_DEVICEID_QNAME = createQNameForMustConstraint("device-id");
    private static final SchemaPath MUST_STATE_CONTAINER_ACTION_INPUT_DEVICE_DEVICEID_SCHEMAPATH = buildSchemaPath(MUST_STATE_CONTAINER_ACTION_INPUT_DEVICE_SCHEMAPATH, MUST_STATE_CONTAINER_ACTION_INPUT_DEVICE_DEVICEID_QNAME);

    private static final QName MUST_CONSTRAINT_RPC_ROOT_QNAME = createQNameForMustConstraint("must-constraint-rpc");
    private static final SchemaPath MUST_CONSTRAINT_RPC_ROOT_SCHEMAPATH = SchemaPath.create(true, MUST_CONSTRAINT_RPC_ROOT_QNAME);
    private static final SchemaPath MUST_CONSTRAINT_RPC_INPUT_SCHEMAPATH = buildSchemaPath(MUST_CONSTRAINT_RPC_ROOT_SCHEMAPATH, MUST_STATE_CONTAINER_ACTION_INPUT_QNAME);
    private static final SchemaPath MUST_CONSTRAINT_RPC_INPUT_DEVCE_SCHEMAPATH = buildSchemaPath(MUST_CONSTRAINT_RPC_INPUT_SCHEMAPATH, MUST_STATE_CONTAINER_ACTION_INPUT_DEVICE_QNAME);
    private static final SchemaPath MUST_CONSTRAINT_RPC_INPUT_DEVCE_DUID_SCHEMAPATH = buildSchemaPath(MUST_CONSTRAINT_RPC_INPUT_DEVCE_SCHEMAPATH,
            MUST_STATE_CONTAINER_ACTION_INPUT_DEVICE_DUID_QNAME);

    private static final SchemaPath MUST_CONSTRAINT_RPC_INPUT_DEVCE_DEVICEID_SCHEMAPATH =
            buildSchemaPath(MUST_CONSTRAINT_RPC_INPUT_DEVCE_SCHEMAPATH, MUST_STATE_CONTAINER_ACTION_INPUT_DEVICE_DEVICEID_QNAME);

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
    
    @BeforeClass
    public static void initializeOnce() throws SchemaBuildException {
        List<YangTextSchemaSource> yangFiles = TestUtil.getByteSources(getYang());
        m_schemaRegistry = new SchemaRegistryImpl(yangFiles, Collections.emptySet(), Collections.emptyMap(), new NoLockService());
        m_schemaRegistry.setName(SchemaRegistry.GLOBAL_SCHEMA_REGISTRY);
        m_schemaRegistry.registerAppAllowedAugmentedPath("Module1","/someAbs", mock(SchemaPath.class));
        m_schemaRegistry.registerAppAllowedAugmentedPath("Module1","/extTest:someAbs", mock(SchemaPath.class));
    }

    protected static List<String> getYang() {
        List<String> fileNames = new ArrayList<String>();
        fileNames.add("/schemaregistryutiltest/nc-stack-extensions.yang");
        fileNames.add("/datastorevalidatortest/yangs/datastore-validator-test.yang");
        fileNames.add("/datastorevalidatortest/yangs/datastore-validator-augment-test.yang");
        fileNames.add("/datastorevalidatortest/yangs/dummy-extension.yang");
        fileNames.add("/datastorevalidatortest/yangs/extension-test.yang");
        fileNames.add("/datastorevalidatortest/yangs/extension-test-container.yang");
        fileNames.add("/datastorevalidatortest/yangs/ietf-inet-types.yang");
        fileNames.add("/datastorevalidatortest/yangs/datastore-validator-grouping-test.yang");
        fileNames.add("/datastorevalidatortest/yangs/rpc-output-augmentation-validation-test.yang");
        fileNames.add("/datastorevalidatortest/yangs/test-interfaces.yang");
        fileNames.add("/datastorevalidatortest/yangs/test-when-constraint-schema-path.yang");
        fileNames.add("/datastorevalidatortest/yangs/test-must-constraint-schema-path.yang");
        fileNames.add("/schemaregistryutiltest/module-with-hint.yang");
        fileNames.add("/schemaregistryutiltest/augmenting-module-with-when-condition.yang");
        fileNames.add("/schemaregistryutiltest/module-with-node-hints.yang");
        return fileNames;
        
    }
    
    @Test
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
    
    
    @Before
    public void setUp() throws ModelNodeInitException, SchemaBuildException {
        Map<SchemaPath, ValidationHint> hints = new HashMap<>();
        hints.put(fromString("(urn:org:bbf2:pma:validation?revision=2015-12-14)must-with-hints-impacting-node,referred-container"),
                ValidationHint.SKIP_IMPACT_VALIDATION);
        m_deviceHints.putAll(hints);
        
        super.setUp();
        Module module = m_schemaRegistry.getModuleByNamespace("urn:module-with-hint");        
        m_globalHints = SchemaRegistryUtil.getHintDetails(module, m_schemaRegistry, m_pathBuilder);
        m_pathRegistrar = new SchemaPathRegistrar(m_schemaRegistry, m_modelNodeHelperRegistry);
        getModelNode();
    }
    
    @After
    public void finish() {
        m_schemaRegistry.deRegisterNodesReferencedInConstraints(COMPONENT_ID);
        m_schemaRegistry.deRegisterAppAllowedAugmentedPath(COMPONENT_ID);
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
        when(dataStore.edit(any(), any(), any())).thenThrow(e);
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
    public void testPreCommitsAreCalledInOrder() throws SubSystemValidationException {
        String previousValue = System.getProperty(ENABLE_NEW_NOTIF_STRUCTURE);
        boolean toBeReset = setSystemPropertyAndReturnResetValue(previousValue, "true");
        String requestXml1 = "/datastorevalidatortest/rangevalidation/defaultxml/valid-when-constraint-container.xml";
        EditConfigRequest request1 = createRequest(requestXml1);
        request1.setMessageId("1");
        NetConfResponse response1 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request1, response1);

        ArgumentCaptor<Map> changesMapCaptor = ArgumentCaptor.forClass(Map.class);
        verify(m_localSubSystemSpy, times(2)).preCommit(changesMapCaptor.capture());

        SchemaPath validationSP = SchemaPathBuilder.fromString("(urn:org:bbf2:pma:validation?revision=2015-12-14)validation");
        List<Map> changesMapValues = changesMapCaptor.getAllValues();

        assertEquals("validation[/validation] -> modify\n" +
                " when-validation[/validation/when-validation] -> create\n" +
                "  result-container -> create { previousVal = 'null', currentVal = '15' }\n",
                ((ChangeTreeNode)((ArrayList)changesMapValues.get(0).get(validationSP)).get(0)).print());

        assertEquals("validation[/validation] -> modify\n" +
                        " when-validation[/validation/when-validation] -> modify\n" +
                        "  container-type[/validation/when-validation/container-type] -> create\n",
                ((ChangeTreeNode)((ArrayList)changesMapValues.get(1).get(validationSP)).get(0)).print());
        resetSystemProperty(toBeReset, previousValue);
    }
    
    @Test
    public void testWhenDerivedFromOrLeafOnStateNode() throws Exception {        
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation5 xmlns=\"urn:org:bbf2:pma:validation\">"+
                "    <type>identity2</type>" +
                "</validation5>";
        editConfig(m_server, m_clientInfo, requestXml, true);

        String expectedOutput = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>" 
                + " <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "     <validation:validation5 xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                + "         <validation:must-with-derived-from-or-self>" 
                + "             <validation:mustLeaf>must</validation:mustLeaf>"
                + "         </validation:must-with-derived-from-or-self>"
                + "         <validation:type>validation:identity2</validation:type>" 
                + "     </validation:validation5>" 
                + " </data>"
                + "</rpc-reply>";

        verifyGet(expectedOutput);    
        assertFalse(m_schemaRegistry.getReferringNodesForSchemaPath(TYPE_SCHEMAPATH).containsKey(INTFSTATE_SCHEMAPATH));
    }
    
    @Test
    public void visitListNodeTest() throws ModelNodeInitException {
        String requestXml1 = "/datastorevalidatortest/rangevalidation/defaultxml/valid-when-constraint-list.xml";
        EditConfigRequest request1 = createRequest(requestXml1);
        request1.setMessageId("1");
        NetConfResponse response1 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request1, response1);

        assertTrue(response1.isOk());
        assertEquals(0,response1.getErrors().size());

        SchemaPath whenSchemaPath = buildSchemaPath("when-validation", null);
        SchemaPath listSchemaPath = buildSchemaPath("when-validation", "list-type");
        DataSchemaNode listSchemaNode = m_schemaRegistry.getDataSchemaNode(listSchemaPath);
        m_pathRegistrar.visitListNode(COMPONENT_ID, whenSchemaPath, (ListSchemaNode) listSchemaNode);

        Collection<SchemaPath> schemaPaths = m_schemaRegistry.getSchemaPathsForComponent(COMPONENT_ID);
        assertTrue(schemaPaths.size() == 1);
    }
    
    @Test
    public void testRegisteringAndUnregisteringOfWhenReferringNodes() throws Exception {
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + "  <sampleContainer>"
                + "    <master>sample</master>" 
                + "    <innerContainer>"
                + "      <innerMostLeaf xmlns=\"urn:opendaylight:datastore-validator-augment-test\">dummy</innerMostLeaf>" 
                + "    </innerContainer>"
                + "  </sampleContainer>"
                + "</validation>";

        editConfig(m_server, m_clientInfo, requestXml1, true);
        
        String getResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
                "<data>\n" +
                "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">\n" +
                "    <validation:sampleContainer>\n" +
                "      <validation:innerContainer>\n" +
                "        <validation-augment:innerMostLeaf xmlns:validation-augment=\"urn:opendaylight:datastore-validator-augment-test\">dummy</validation-augment:innerMostLeaf>\n" +
                "      </validation:innerContainer>\n" +
                "      <validation:master>sample</validation:master>\n" +
                "    </validation:sampleContainer>\n" +
                "  </validation:validation>\n" +
                "</data>\n" +
                "</rpc-reply>";
        verifyGet(getResponse);
        /*
         * Yang model:
         * leafWhen, leafWhen has a when target as master
         * leafMust has a must target as master
         * innerMostLeaf is augmenting and present in another module with a when target as master
         * 
         * Expectation:
         * New API will list only THREE when referring nodes (or when aware nodes) for the target node "master".
         * Old API will list FIVE referring nodes (3 when and 1 must and 1 leafref) for the target node "master"
         */
        
        SchemaPath masterSchemaPath = buildSchemaPathForChildOfSampleContainer("master");
        SchemaPath leafWhenSchemaPath = buildSchemaPathForChildOfSampleContainer("leafWhen");
        SchemaPath leafWhen2SchemaPath = buildSchemaPathForChildOfSampleContainer("leafWhen2");
        SchemaPath leafMustSchemaPath = buildSchemaPathForChildOfSampleContainer("leafMust");
        SchemaPath leafWithLeafRefSchemaPath = buildSchemaPathForChildOfSampleContainer("leaf-with-leafref");
        SchemaPath innerContainerSchemaPath = buildSchemaPathForChildOfSampleContainer("innerContainer");
        QName innerMostLeafQName = QName.create("urn:opendaylight:datastore-validator-augment-test", "2018-03-07", "innerMostLeaf");
        SchemaPath innerMostLeafSchemaPath = new SchemaPathBuilder().withParent(innerContainerSchemaPath).appendQName(innerMostLeafQName)
                .build();

        assertEquals(5, m_schemaRegistry.getReferringNodesForSchemaPath(masterSchemaPath).size());
        assertTrue(m_schemaRegistry.getReferringNodesForSchemaPath(masterSchemaPath).containsKey(leafWhenSchemaPath));
        assertTrue(m_schemaRegistry.getReferringNodesForSchemaPath(masterSchemaPath).containsKey(leafWhen2SchemaPath));
        assertTrue(m_schemaRegistry.getReferringNodesForSchemaPath(masterSchemaPath).containsKey(leafMustSchemaPath));
        assertTrue(m_schemaRegistry.getReferringNodesForSchemaPath(masterSchemaPath).containsKey(leafWithLeafRefSchemaPath));
        assertTrue(m_schemaRegistry.getReferringNodesForSchemaPath(masterSchemaPath).containsKey(innerMostLeafSchemaPath));

        assertEquals(3, m_schemaRegistry.getWhenReferringNodes(masterSchemaPath).size());
        assertTrue(m_schemaRegistry.getWhenReferringNodes(masterSchemaPath).containsKey(leafWhenSchemaPath));
        assertTrue(m_schemaRegistry.getWhenReferringNodes(masterSchemaPath).containsKey(leafWhen2SchemaPath));
        assertTrue(m_schemaRegistry.getWhenReferringNodes(masterSchemaPath).containsKey(innerMostLeafSchemaPath));
        
        //it should not have must referring and leafref referring nodes
        assertFalse(m_schemaRegistry.getWhenReferringNodes(masterSchemaPath).containsKey(leafMustSchemaPath));
        assertFalse(m_schemaRegistry.getWhenReferringNodes(masterSchemaPath).containsKey(leafWithLeafRefSchemaPath));
    }

    private SchemaPath buildSchemaPathForChildOfSampleContainer(String child) {
        SchemaPath schemaPath = new SchemaPathBuilder()
                .withNamespace(DS_VALIDATOR_TEST_YANG_NS)
                .withRevision(DS_VALIDATOR_TEST_YANG_REVISION)
                .appendLocalName(VALIDATION)
                .appendLocalName(SAMPLE_CONTAINER)
                .appendLocalName(child)
                .build();
        return schemaPath;
    }

    @Test
    public void testIsChildBigListUpdatedInSchemaRegistry() {
        SchemaPath listTypeSchemaPath = buildSchemaPath("when-validation", "list-type");
        SchemaPath listType1SchemaPath = buildSchemaPath("when-validation", "list-type1");
        SchemaPath listType2SchemaPath = buildSchemaPath("when-validation", "list-type2");
        boolean isChildBigList;
        isChildBigList = m_schemaRegistry.isChildBigList(listTypeSchemaPath);
        assertTrue(isChildBigList);
        isChildBigList = m_schemaRegistry.isChildBigList(listType1SchemaPath);
        assertFalse(isChildBigList);
        isChildBigList = m_schemaRegistry.isChildBigList(listType2SchemaPath);
        assertTrue(isChildBigList);
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
        m_pathRegistrar.visitChoiceCaseNode(COMPONENT_ID, whenSchemaPath, choiceSchemaNode.getCases().get(m_schemaRegistry.lookupQName(NAMESPACE, "container-case-success")));

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

    @Test
    public void testRegisterGlobalValidationHints() {
        ReferringNodes referringNodesForSchemaPath = m_schemaRegistry.getReferringNodesForSchemaPath(fromString("(urn:org:bbf2:pma" +
                ":validation?revision=2015-12-14)must-with-hints-impacting-node,referred-container"));
        assertEquals(1, referringNodesForSchemaPath.getReferringNodes().size());
        assertEquals(1, referringNodesForSchemaPath.getReferringNodes().get(fromString("(urn:org:bbf2" +
                ":pma" +
                ":validation?revision=2015-12-14)must-with-hints-impacted-node")).size());
        assertEquals(ValidationHint.SKIP_IMPACT_VALIDATION, referringNodesForSchemaPath.getReferringNodes().get(fromString("(urn:org:bbf2" +
                ":pma" +
                ":validation?revision=2015-12-14)must-with-hints-impacted-node")).iterator().next().getValidationHint());
    }

    @Test
    public void testWithGlobalHintsOnNode(){
    	QName hintContainer = QName.create("urn:module-with-node-hints", "2018-06-28", "hintContainer");
    	QName nodeWithHint1 = QName.create("urn:module-with-node-hints", "2018-06-28","nodeWithHint1");
    	QName nodeWithHint2 = QName.create("urn:module-with-node-hints", "2018-06-28", "nodeWithHint2");
    	QName nodeWithHint6 = QName.create("urn:module-with-node-hints", "2018-06-28", "nodeWithHint6");
    	QName nodeWithHint8 = QName.create("urn:module-with-node-hints", "2018-06-28", "nodeWithHint8");
    	QName nodeWithHint9 = QName.create("urn:module-with-node-hints", "2018-06-28", "nodeWithHint9");
    	QName nodeLeafListWithHint = QName.create("urn:module-with-node-hints", "2018-06-28", "nodeLeafList");
    	SchemaPath nodeWithHint1SP = SchemaPath.create(true, hintContainer, nodeWithHint1);
    	SchemaPath nodeWithHint2SP = SchemaPath.create(true, hintContainer, nodeWithHint2);
    	SchemaPath nodeWithHint6SP = SchemaPath.create(true, hintContainer, nodeWithHint6);
    	SchemaPath nodeWithHint8SP = SchemaPath.create(true, hintContainer, nodeWithHint8);
    	SchemaPath nodeWithHint9SP = SchemaPath.create(true, hintContainer, nodeWithHint9);
    	SchemaPath nodeLeafListWithHintSP = SchemaPath.create(true, hintContainer, nodeLeafListWithHint);
    	ReferringNodes referringNodesForHint1 = m_schemaRegistry.getReferringNodesForSchemaPath(nodeWithHint2SP);
    	Map<SchemaPath, Set<ReferringNode>> referringNodesList = referringNodesForHint1.getReferringNodes();
    	assertEquals(2, referringNodesList.size());
    	assertTrue(referringNodesList.containsKey(nodeWithHint6SP));
        ReferringNode referringNode = referringNodesList.get(nodeWithHint6SP).iterator().next();
    	assertEquals(nodeWithHint6SP, referringNode.getReferringSP());
    	assertEquals(nodeWithHint2SP, referringNode.getReferredSP());
    	assertEquals(ValidationHint.SKIP_IMPACT_ON_CREATE, referringNode.getValidationHint());

    	assertTrue(referringNodesList.containsKey(nodeWithHint1SP));
    	referringNode = referringNodesList.get(nodeWithHint1SP).iterator().next();
    	assertEquals(nodeWithHint1SP, referringNode.getReferringSP());
    	assertEquals(nodeWithHint2SP, referringNode.getReferredSP());
    	assertEquals(ValidationHint.SKIP_IMPACT_ON_CREATE, referringNode.getValidationHint());

    	referringNodesForHint1 = m_schemaRegistry.getReferringNodesForSchemaPath(nodeLeafListWithHintSP);
    	referringNodesList = referringNodesForHint1.getReferringNodes();
    	assertEquals(2, referringNodesList.size());
    	assertTrue(referringNodesList.containsKey(nodeWithHint9SP));
    	referringNode = referringNodesList.get(nodeWithHint9SP).iterator().next();
    	assertEquals(nodeWithHint9SP, referringNode.getReferringSP());
    	assertEquals(nodeLeafListWithHintSP, referringNode.getReferredSP());
    	assertEquals(ValidationHint.SKIP_VALIDATION, referringNode.getValidationHint());
        assertTrue(m_schemaRegistry.isSkipValidationPath(nodeWithHint9SP));
    	assertTrue(m_schemaRegistry.isSkipValidationPath(nodeWithHint8SP));
    	assertFalse(m_schemaRegistry.isSkipValidationPath(nodeLeafListWithHintSP));
    }

    @Test
    public void testWhenReferringNodesForAllSchemaNodes() {
        //Verify When referring nodes for State schema nodes (config:false)
        Map<SchemaPath, Expression> whenReferringNodesMap = m_schemaRegistry.getWhenReferringNodesForAllSchemaNodes(COMPONENTID,
                WHEN_LEAF_SCHEMAPATH);
        assertEquals(2, whenReferringNodesMap.size());
        assertTrue(whenReferringNodesMap.containsKey(TEST_LEAF_SCHEMAPATH));
        assertTrue(whenReferringNodesMap.containsKey(STAET_CONTAINER_SCHEMAPATH));
        assertEquals("when:when-leaf/when:test-container/when:test-leaf", whenReferringNodesMap.get(TEST_LEAF_SCHEMAPATH).toString());
        assertEquals("when:when-leaf/when:test-container/when:state-container", whenReferringNodesMap.get(STAET_CONTAINER_SCHEMAPATH).toString());

        // Verify When referring nodes for action schema nodes
        whenReferringNodesMap = m_schemaRegistry.getWhenReferringNodesForAllSchemaNodes(COMPONENTID,
                STAET_CONTAINER_ACTION_INPUT_DEVICE_DUID_SCHEMAPATH);
        assertEquals(1, whenReferringNodesMap.size());
        assertTrue(whenReferringNodesMap.containsKey(STAET_CONTAINER_ACTION_INPUT_DEVICE_DEVICEID_SCHEMAPATH));
        assertEquals("when:duid/when:device/when:device-id", whenReferringNodesMap.get(STAET_CONTAINER_ACTION_INPUT_DEVICE_DEVICEID_SCHEMAPATH).toString());

        // verify when referring nodes for RPC schema nodes
        whenReferringNodesMap = m_schemaRegistry.getWhenReferringNodesForAllSchemaNodes(COMPONENTID,
                WHEN_CONSTRAINT_RPC_INPUT_DEVCE_DUID_SCHEMAPATH);
        assertEquals(1, whenReferringNodesMap.size());
        assertTrue(whenReferringNodesMap.containsKey(WHEN_CONSTRAINT_RPC_INPUT_DEVCE_DEVICEID_SCHEMAPATH));
        assertEquals("when:duid/when:device/when:device-id", whenReferringNodesMap.get(WHEN_CONSTRAINT_RPC_INPUT_DEVCE_DEVICEID_SCHEMAPATH).toString());
    }

    @Test
    public void testMustReferringNodesForAllSchemaNodes() {

        //Verify Must referring nodes for State schema nodes (config:false) and Configuration data as well (config:true)
        Map<SchemaPath, Expression> mustReferringNodesMap = m_schemaRegistry.getMustReferringNodesForAllSchemaNodes(MUST_COMPONENTID,
                MUST_LEAF_SCHEMAPATH);
        assertEquals(2, mustReferringNodesMap.size());
        assertTrue(mustReferringNodesMap.containsKey(MUST_TEST_LEAF_SCHEMAPATH));
        assertTrue(mustReferringNodesMap.containsKey(MUST_STATE_CONTAINER_SCHEMAPATH));
        assertEquals("must:must-leaf/must:test-container/must:test-leaf", mustReferringNodesMap.get(MUST_TEST_LEAF_SCHEMAPATH).toString());
        assertEquals("must:must-leaf/must:test-container/must:state-container", mustReferringNodesMap.get(MUST_STATE_CONTAINER_SCHEMAPATH).toString());

        // Verify Must referring nodes for action schema nodes
        mustReferringNodesMap = m_schemaRegistry.getMustReferringNodesForAllSchemaNodes(MUST_COMPONENTID,
                MUST_STATE_CONTAINER_ACTION_INPUT_DEVICE_DUID_SCHEMAPATH);
        assertEquals(1, mustReferringNodesMap.size());
        assertTrue(mustReferringNodesMap.containsKey(MUST_STATE_CONTAINER_ACTION_INPUT_DEVICE_DEVICEID_SCHEMAPATH));
        assertEquals("must:duid/must:device/must:device-id", mustReferringNodesMap.get(MUST_STATE_CONTAINER_ACTION_INPUT_DEVICE_DEVICEID_SCHEMAPATH).toString());

        // verify Must referring nodes for RPC schema nodes
        mustReferringNodesMap = m_schemaRegistry.getMustReferringNodesForAllSchemaNodes(MUST_COMPONENTID,
                MUST_CONSTRAINT_RPC_INPUT_DEVCE_DUID_SCHEMAPATH);
        assertEquals(1, mustReferringNodesMap.size());
        assertTrue(mustReferringNodesMap.containsKey(MUST_CONSTRAINT_RPC_INPUT_DEVCE_DEVICEID_SCHEMAPATH));
        assertEquals("must:duid/must:device/must:device-id", mustReferringNodesMap.get(MUST_CONSTRAINT_RPC_INPUT_DEVCE_DEVICEID_SCHEMAPATH).toString());
    }
}
