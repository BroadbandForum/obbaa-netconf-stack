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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.GenericConfigAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.CommandExecutionException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditChangeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigAttributeHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeDeleteException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.SetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildLeafListHelper;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

import org.broadband_forum.obbaa.netconf.api.messages.EditConfigOperations;
import org.broadband_forum.obbaa.netconf.api.messages.InsertOperation;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetAttributeException;

public class SetConfigAttributeCommandTest {

    private static final String TEST_LOCAL_NAME = "testLocalName";
    private static final String TEST_NAMESPACE = "testNamespace";
    private static final String TEST_NODE_VALUE1 = "testNodeValue1";
    private static final String TEST_NODE_VALUE2 = "testNodeValue2";
    private static final String TEST_NODE_VALUE3 = "testNodeValue3";

    private QName m_qName;
    private ConfigAttributeHelper m_configAttributeHelper;
    private ChildLeafListHelper m_childLeafListHelper;
    private Map<QName, ConfigAttributeHelper> m_testConfigAttributeHelpers = new HashMap<>();
    private Map<QName, ChildLeafListHelper> m_testChildLeafListeHelpers = new HashMap<>();
    private SetConfigAttributeCommand m_setConfigAttributeCommand = new SetConfigAttributeCommand();
    private EditContext m_testEditcontext;
    private EditContainmentNode m_testEditContainmentNode;
    private EditChangeNode m_testEditChangeNode1;
    private EditChangeNode m_testEditChangeNode2;
    private EditChangeNode m_testEditChangeNode3;

    private ModelNode m_testInstance;
    private SchemaRegistry m_testSchemaRegistry;
    private GenericConfigAttribute m_testNodeValue2;
    private GenericConfigAttribute m_testNodeValue3;
    private GenericConfigAttribute m_testNodeValue1;
    private GenericConfigAttribute m_testValue;

    @Before
    public void initialize() throws SetAttributeException {
        m_qName = QName.create(TEST_NAMESPACE, TEST_LOCAL_NAME);
        m_testEditChangeNode1 = mock(EditChangeNode.class);
        m_testEditChangeNode2 = mock(EditChangeNode.class);
        m_testEditChangeNode3 = mock(EditChangeNode.class);

        m_testValue = new GenericConfigAttribute(TEST_LOCAL_NAME, TEST_NAMESPACE, "testValue");
        m_testNodeValue1 = new GenericConfigAttribute(TEST_LOCAL_NAME, TEST_NAMESPACE, TEST_NODE_VALUE1);
        m_testNodeValue2 = new GenericConfigAttribute(TEST_LOCAL_NAME, TEST_NAMESPACE, TEST_NODE_VALUE2);
        m_testNodeValue3 = new GenericConfigAttribute(TEST_LOCAL_NAME, TEST_NAMESPACE, TEST_NODE_VALUE3);

        m_testEditcontext = mock(EditContext.class);
        m_testEditContainmentNode = mock(EditContainmentNode.class);
        m_testInstance = mock(ModelNode.class);
        m_testSchemaRegistry = mock(SchemaRegistry.class);
        m_configAttributeHelper = mock(ConfigAttributeHelper.class);
        m_testConfigAttributeHelpers.put(m_qName, m_configAttributeHelper);
        m_childLeafListHelper = mock(ChildLeafListHelper.class);
        m_testChildLeafListeHelpers.put(m_qName, m_childLeafListHelper);
    }

    private void initializerForSetConfigAttributeCommandExecute() {
        when(m_testEditChangeNode1.getQName()).thenReturn(m_qName);
        when(m_testEditcontext.getEditNode()).thenReturn(m_testEditContainmentNode);
        List<EditChangeNode> listEditChangeNodes = new ArrayList<>();
        listEditChangeNodes.add(m_testEditChangeNode1);
        when(m_testEditChangeNode1.getConfigLeafAttribute()).thenReturn(m_testNodeValue1);
        when(m_testEditChangeNode1.getValue()).thenReturn(TEST_NODE_VALUE1);
        when(m_testEditcontext.getEditNode().getChangeNodes()).thenReturn(listEditChangeNodes);
    }

    private void initializerForCommandExecute() throws GetAttributeException {
        initializerForSetConfigAttributeCommandExecute();
        SchemaPath schemaPath = mock(SchemaPath.class);
        when(m_testInstance.getModelNodeSchemaPath()).thenReturn(schemaPath);
        Collection<DataSchemaNode> dataSchemaNodes = new ArrayList<>();
        DataSchemaNode dataSchemaNode = mock(LeafListSchemaNode.class);
        dataSchemaNodes.add(dataSchemaNode);
        when(m_testSchemaRegistry.getChildren(schemaPath)).thenReturn(dataSchemaNodes);
        when(dataSchemaNode.getQName()).thenReturn(m_qName);
        when(m_configAttributeHelper.getValue(m_testInstance)).thenReturn(m_testValue);
        Collection<ConfigLeafAttribute> listObjects = new ArrayList<>();
        listObjects.add(new GenericConfigAttribute(TEST_LOCAL_NAME, TEST_NAMESPACE, TEST_NODE_VALUE1));
        when(m_childLeafListHelper.getValue(m_testInstance)).thenReturn(listObjects);
        when(m_testEditChangeNode1.getOperation()).thenReturn(EditConfigOperations.MERGE);
        m_setConfigAttributeCommand.addSetInfo(m_testSchemaRegistry, m_testConfigAttributeHelpers,
                m_testChildLeafListeHelpers, m_testInstance, m_testEditcontext);
    }

    private void initializerForCommandExecuteOfUserOrdered() throws GetAttributeException {
        when(m_testEditChangeNode2.getQName()).thenReturn(m_qName);
        when(m_testEditChangeNode3.getQName()).thenReturn(m_qName);
        when(m_testEditcontext.getEditNode()).thenReturn(m_testEditContainmentNode);
        List<EditChangeNode> listEditChangeNodes = new ArrayList<>();
        listEditChangeNodes.add(m_testEditChangeNode2);
        listEditChangeNodes.add(m_testEditChangeNode3);
        when(m_testEditChangeNode2.getConfigLeafAttribute()).thenReturn(m_testNodeValue2);
        when(m_testEditChangeNode2.getValue()).thenReturn(TEST_NODE_VALUE2);
        when(m_testEditChangeNode3.getConfigLeafAttribute()).thenReturn(m_testNodeValue3);
        when(m_testEditChangeNode3.getValue()).thenReturn(TEST_NODE_VALUE3);
        when(m_testEditcontext.getEditNode().getChangeNodes()).thenReturn(listEditChangeNodes);
        SchemaPath schemaPath = mock(SchemaPath.class);
        when(m_testInstance.getModelNodeSchemaPath()).thenReturn(schemaPath);
        Collection<DataSchemaNode> dataSchemaNodes = new ArrayList<>();
        DataSchemaNode dataSchemaNode = mock(LeafListSchemaNode.class);
        dataSchemaNodes.add(dataSchemaNode);
        when(m_testSchemaRegistry.getChildren(schemaPath)).thenReturn(dataSchemaNodes);
        when(dataSchemaNode.getQName()).thenReturn(m_qName);
        when(m_configAttributeHelper.getValue(m_testInstance)).thenReturn(m_testValue);
        Collection<ConfigLeafAttribute> listObjects = new ArrayList<>();
        listObjects.add(new GenericConfigAttribute(TEST_LOCAL_NAME, TEST_NAMESPACE, TEST_NODE_VALUE1));
        when(m_childLeafListHelper.getValue(m_testInstance)).thenReturn(listObjects);
        when(m_testEditChangeNode2.getOperation()).thenReturn(EditConfigOperations.CREATE);
        when(m_testEditChangeNode3.getOperation()).thenReturn(EditConfigOperations.CREATE);
        m_setConfigAttributeCommand.addSetInfo(m_testSchemaRegistry, m_testConfigAttributeHelpers,
                m_testChildLeafListeHelpers, m_testInstance, m_testEditcontext);

        when(((LeafListSchemaNode) dataSchemaNode).isUserOrdered()).thenReturn(true);
    }

    @Test
    public void testExecuteWhenEditConfigOperationIsDeleteOrRemove() throws CommandExecutionException,
            GetAttributeException {
        initializerForSetConfigAttributeCommandExecute();
        when(m_testEditChangeNode1.getOperation()).thenReturn(EditConfigOperations.DELETE);
        Collection<ConfigLeafAttribute> listObjects = new ArrayList<>();
        when(m_childLeafListHelper.getValue(m_testInstance)).thenReturn(listObjects);
        m_setConfigAttributeCommand.addSetInfo(m_testSchemaRegistry, m_testConfigAttributeHelpers,
                m_testChildLeafListeHelpers, m_testInstance, m_testEditcontext);
        try {
            m_setConfigAttributeCommand.execute();
            fail("Should have thrown an Exception");
        } catch (CommandExecutionException e) {
            assertEquals("The instance - testLocalName = testNodeValue1 does not exist; Request Failed.", e
                    .getMessage());
        }
    }

    @Test
    public void testExecuteWhenEditConfigOperationIsNotDeleteOrRemove() throws CommandExecutionException,
            GetAttributeException {
        initializerForSetConfigAttributeCommandExecute();
        when(m_testEditChangeNode1.getOperation()).thenReturn(EditConfigOperations.CREATE);
        Collection<ConfigLeafAttribute> listObjects = new ArrayList<>();
        listObjects.add(m_testNodeValue1);
        when(m_childLeafListHelper.getValue(m_testInstance)).thenReturn(listObjects);
        SchemaPath schemaPath = mock(SchemaPath.class);
        when(m_testInstance.getModelNodeSchemaPath()).thenReturn(schemaPath);
        m_setConfigAttributeCommand.addSetInfo(m_testSchemaRegistry, m_testConfigAttributeHelpers,
                m_testChildLeafListeHelpers, m_testInstance, m_testEditcontext);
        try {
            m_setConfigAttributeCommand.execute();
            fail("Should have thrown an Exception");
        } catch (CommandExecutionException e) {
            assertEquals("Create instance attempted while the instance - testLocalName = testNodeValue1 already " +
                    "exists; Request Failed.", e.getMessage());
        }
    }

    @Test
    public void testExecuteWhenLeafIsOrdered() throws CommandExecutionException, GetAttributeException {
        initializerForSetConfigAttributeCommandExecute();
        when(m_testEditChangeNode1.getOperation()).thenReturn(EditConfigOperations.MERGE);
        Collection<ConfigLeafAttribute> listObjects = new ArrayList<>();
        listObjects.add(new GenericConfigAttribute(TEST_LOCAL_NAME, TEST_NAMESPACE, TEST_NODE_VALUE1));
        when(m_childLeafListHelper.getValue(m_testInstance)).thenReturn(listObjects);
        SchemaPath schemaPath = mock(SchemaPath.class);
        when(m_testInstance.getModelNodeSchemaPath()).thenReturn(schemaPath);
        Collection<DataSchemaNode> dataSchemaNodes = new ArrayList<>();
        DataSchemaNode dataSchemaNode = mock(LeafListSchemaNode.class);
        dataSchemaNodes.add(dataSchemaNode);
        when(m_testSchemaRegistry.getChildren(schemaPath)).thenReturn(dataSchemaNodes);
        QName qName2 = QName.create("testNamespace2");
        when(dataSchemaNode.getQName()).thenReturn(qName2);
        m_setConfigAttributeCommand.addSetInfo(m_testSchemaRegistry, m_testConfigAttributeHelpers,
                m_testChildLeafListeHelpers, m_testInstance, m_testEditcontext);
        try {
            m_setConfigAttributeCommand.execute();
            fail("Should have thrown an Exception");
        } catch (CommandExecutionException e) {
            assertEquals("org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.SetAttributeException: Cannot get the " +
                    "schema node for '(testNamespace)testLocalName'", e.getMessage());
        }
    }

    @Test
    public void testExecuteWhenLeafIsOrderedofMultiInsert() throws Exception {
        initializerForCommandExecuteOfUserOrdered();
        InsertOperation insertOp1 = new InsertOperation(InsertOperation.FIRST, null);
        InsertOperation insertOp2 = new InsertOperation(InsertOperation.AFTER, TEST_NODE_VALUE2);
        when(m_testEditChangeNode2.getInsertOperation()).thenReturn(insertOp1);
        when(m_testEditChangeNode3.getInsertOperation()).thenReturn(insertOp2);

        m_setConfigAttributeCommand.addSetInfo(m_testSchemaRegistry, m_testConfigAttributeHelpers,
                m_testChildLeafListeHelpers, m_testInstance, m_testEditcontext);
        m_setConfigAttributeCommand.execute();
        verify(m_childLeafListHelper).addChildByUserOrder(m_testInstance, m_testNodeValue2,
                EditConfigOperations.CREATE, insertOp1);
        verify(m_childLeafListHelper).addChildByUserOrder(m_testInstance, m_testNodeValue3,
                EditConfigOperations.CREATE, insertOp2);
    }

    @Test
    public void testExecuteWhenRollBackThrowsExceptionAtSetValue() throws CommandExecutionException,
            GetAttributeException, SetAttributeException {
        initializerForCommandExecute();
        m_setConfigAttributeCommand.execute();
    }

    @Test
    public void testExecuteWhenRollBackThrowsExceptionAtRemoveAllChild() throws CommandExecutionException, GetAttributeException, ModelNodeDeleteException {
        initializerForCommandExecute();
        m_setConfigAttributeCommand.execute();
    }

    @Test
    public void testExecuteWhenRollBackThrowsExceptionAtAddChild() throws CommandExecutionException, GetAttributeException, SetAttributeException {
        initializerForCommandExecute();
        m_setConfigAttributeCommand.execute();
    }

    @Test
    public void testExecuteWhenChildIsAdded() throws CommandExecutionException, GetAttributeException, SetAttributeException {
        initializerForCommandExecute();
        m_setConfigAttributeCommand.execute();
    }

    @Test
    public void testToString() {
        assertNotNull(m_setConfigAttributeCommand.toString());
    }
}
