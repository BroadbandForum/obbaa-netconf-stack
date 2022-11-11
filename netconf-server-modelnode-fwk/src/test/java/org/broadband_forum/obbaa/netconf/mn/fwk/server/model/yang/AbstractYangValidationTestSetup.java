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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.yang;

import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.service.ModelServiceDeployerTest.COMPONENT_ID;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.YangUtils.loadXmlDataIntoServer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.api.utils.SystemPropertyUtilsUserTest;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.CompositeSubSystemImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.DataStore;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NbiNotificationHelperImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NetConfServerImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregatorImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DSExpressionValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.inmemory.InMemoryDSM;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.LocalSubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.YangUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcPayloadConstraintParser;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;

public abstract class AbstractYangValidationTestSetup extends AbstractValidationTestSetup{
    public static final String YANG_PATH = TestUtil.class.getResource("/yangs/example-jukebox.yang").getPath();
    public static final String XML_PATH = TestUtil.class.getResource("/example-jukebox.xml").getPath();
    public static final String YANG_MOULE_REL_PATH = "/referenceyangs/choice-case-module.yang";
    public static final String YANG_MODEL_PATH = YangChoiceCaseTest.class.getResource(YANG_MOULE_REL_PATH).getPath();
    public static final String NESTED_CHOICE_YANG_MODULE_REL_PATH = "/referenceyangs/nested-choice-with-leaf-list-and-list.yang";
    public static final String NESTED_CHOICE_YANG_MODULE_PATH = YangChoiceCaseTest.class.getResource(NESTED_CHOICE_YANG_MODULE_REL_PATH).getPath();

    protected NetConfServerImpl m_server;
    protected SubSystemRegistry m_subSystemRegistry = new SubSystemRegistryImpl();
    protected RootModelNodeAggregator m_rootModelNode;
    protected ModelNodeWithAttributes m_modelWithAttributes;

    private ModelNode m_runningModel;
    protected LocalSubSystem m_testSubSystem = spy(new LocalSubSystem());

    private static final String ENABLE_NEW_NOTIF_STRUCTURE = "ENABLE_NEW_NOTIF_STRUCTURE";
    private static SystemPropertyUtilsUserTest c_systemPropertyUtils;


    @AfterClass
    public static void tearDownEnv(){
        c_systemPropertyUtils.tearDownMockUtils();
    }

    @BeforeClass
    public static void setUpEnv() {
        c_systemPropertyUtils = new SystemPropertyUtilsUserTest();
        c_systemPropertyUtils.setUpMockUtils();
        c_systemPropertyUtils.mockPropertyUtils(ENABLE_NEW_NOTIF_STRUCTURE, "true");
    }

    @Override
    public void setup() throws SchemaBuildException, ModelNodeInitException {
        m_schemaRegistry = new SchemaRegistryImpl(Collections.emptyList(), Collections.emptySet(), Collections.emptyMap(), new NoLockService());
        m_schemaRegistry.loadSchemaContext("jukebox", TestUtil.getJukeBoxYangsWithoutAlbumImage(), Collections.emptySet(), Collections.emptyMap());
        m_server = getNcServer();
        m_modelNodeHelperRegistry = new ModelNodeHelperRegistryImpl(m_schemaRegistry);
        m_subSystemRegistry.setCompositeSubSystem(new CompositeSubSystemImpl());
        m_expValidator = new DSExpressionValidator(m_schemaRegistry, m_modelNodeHelperRegistry, m_subSystemRegistry);
        createRootModelNode();
        m_server.setRunningDataStore(getDataStore());
        loadXmlDataIntoServer(m_server, XML_PATH);
    }

    public void setUpForChoiceCase() throws SchemaBuildException, ModelNodeInitException {
        m_subSystemRegistry = new SubSystemRegistryImpl();
        m_subSystemRegistry.setCompositeSubSystem(new CompositeSubSystemImpl());
        m_schemaRegistry = new SchemaRegistryImpl(getYangFilePath(YANG_MOULE_REL_PATH), Collections.emptySet(), Collections.emptyMap(), new NoLockService());
        ModelNodeHelperRegistry modelNodeHelperRegistry = new ModelNodeHelperRegistryImpl(m_schemaRegistry);
        ModelNodeDataStoreManager modelNodeDsm = new InMemoryDSM(m_schemaRegistry);
        m_runningModel = YangUtils.createInMemoryModelNode(YANG_MODEL_PATH, m_testSubSystem, modelNodeHelperRegistry,
                m_subSystemRegistry, m_schemaRegistry, modelNodeDsm);
        m_rootModelNode = new RootModelNodeAggregatorImpl(m_schemaRegistry, modelNodeHelperRegistry, modelNodeDsm, m_subSystemRegistry)
                .addModelServiceRoot(COMPONENT_ID, m_runningModel);
       m_dataStore=getDataStore();
       m_server = new NetConfServerImpl(m_schemaRegistry);
       m_server.setRunningDataStore(m_dataStore);
    }

    public void setUpForNestedChoiceCase() throws SchemaBuildException, ModelNodeInitException {
        m_subSystemRegistry = new SubSystemRegistryImpl();
        m_subSystemRegistry.setCompositeSubSystem(new CompositeSubSystemImpl());
        m_schemaRegistry = new SchemaRegistryImpl(getYangFilePath(NESTED_CHOICE_YANG_MODULE_REL_PATH), Collections.emptySet(), Collections.emptyMap(), new NoLockService());
        ModelNodeHelperRegistry modelNodeHelperRegistry = new ModelNodeHelperRegistryImpl(m_schemaRegistry);
        ModelNodeDataStoreManager modelNodeDsm = new InMemoryDSM(m_schemaRegistry);
        m_runningModel = YangUtils.createInMemoryModelNode(NESTED_CHOICE_YANG_MODULE_PATH, m_testSubSystem, modelNodeHelperRegistry,
                m_subSystemRegistry, m_schemaRegistry, modelNodeDsm);
        m_rootModelNode = new RootModelNodeAggregatorImpl(m_schemaRegistry, modelNodeHelperRegistry, modelNodeDsm, m_subSystemRegistry)
                .addModelServiceRoot(COMPONENT_ID, m_runningModel);
        m_dataStore=getDataStore();
        m_server = new NetConfServerImpl(m_schemaRegistry);
        m_server.setRunningDataStore(m_dataStore);
    }

    protected DataStore getDataStore() {
        m_dataStore = new DataStore(StandardDataStores.RUNNING, m_rootModelNode, m_subSystemRegistry);
        m_nbiNotificationHelper = new NbiNotificationHelperImpl();
        m_dataStore.setNbiNotificationHelper(m_nbiNotificationHelper);
        return m_dataStore;
    }

    protected InMemoryDSM getInMemoryDSM() {
        return new InMemoryDSM(m_schemaRegistry);
    }

    protected void createRootModelNode() {
        m_modelNodeDsm = getInMemoryDSM();
        try {
            m_rootModelNode= new RootModelNodeAggregatorImpl(m_schemaRegistry, m_modelNodeHelperRegistry, m_modelNodeDsm, m_subSystemRegistry)
                    .addModelServiceRoot(m_componentId, getModelNodeWithAttrs());
        } catch (ModelNodeInitException e) {
            e.printStackTrace();
        }
    }
    
    protected List<YangTextSchemaSource> getYangFilePath(String YangPath) {
        return TestUtil.getByteSources(Arrays.asList(YangPath));
    }

    protected List<YangTextSchemaSource> getXmlFilePath(String XmlPath) {
        return TestUtil.getByteSources(Arrays.asList(XmlPath));
    }

    protected SchemaRegistry getSchemaRegistry() throws SchemaBuildException {
        List<YangTextSchemaSource> yangs = TestUtil.getJukeBoxDeps();
        yangs.add(TestUtil.getByteSource("/yangs/example-jukebox.yang"));
        return m_schemaRegistry = new SchemaRegistryImpl(yangs, new NoLockService());
    }

    protected NetConfServerImpl getNcServer() throws SchemaBuildException {
        return new NetConfServerImpl(m_schemaRegistry, mock(RpcPayloadConstraintParser.class));
    }

    protected ModelNodeWithAttributes getModelNodeWithAttrs() throws ModelNodeInitException {
        return YangUtils.createInMemoryModelNode(YANG_PATH, m_testSubSystem, m_modelNodeHelperRegistry, m_subSystemRegistry,
                m_schemaRegistry, m_modelNodeDsm);
    }
}
