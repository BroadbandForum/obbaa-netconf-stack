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
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.GenericConfigAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditConfigException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeGetException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.CommandExecutionException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeCreateException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildListHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.HelperDrivenModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeSetException;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

import org.broadband_forum.obbaa.netconf.api.messages.InsertOperation;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;

public class MergeChildInListCommandTest {

    private static final String CONTAINER = "container";
    private static final String TEST_VALUE = "test='A'fter";
    private static final String NAMESPACE = "namespace";
    private MergeChildInListCommand m_oneCommand = new MergeChildInListCommand();
    private ChildListHelper m_childList1Helper = mock(ChildListHelper.class);
    private QName m_qname = QName.create(NAMESPACE, "testQname");
    private EditContext m_editContext = mock(EditContext.class);
    private ModelNode m_instance = mock(HelperDrivenModelNode.class);
    private ModelNode m_childNode = mock(ModelNode.class);
    private ModelNode m_parentNode = mock(ModelNode.class);
    private ModelNodeHelperRegistry m_modelNodeHelperRegistry = mock(ModelNodeHelperRegistry.class);
    private EditContainmentNode m_editNode = mock(EditContainmentNode.class);
    private SchemaPath m_schemaPath = mock(SchemaPath.class);
    private SchemaRegistry m_schemaRegistry = mock(SchemaRegistry.class);
    private Collection<ModelNode> childList = new ArrayList<ModelNode>();
    private Collection<DataSchemaNode> dataSchemaNodeCollection = new ArrayList<>();
    private ListSchemaNode m_listSchemaNode = null;
    private QName m_keyQname;
    private LeafSchemaNode m_keyLeafnode;

    @Before
    public void setUp() {
        m_keyQname = QName.create(NAMESPACE, "est");
    }

    private void initializeExecute() {
        when(m_editContext.getEditNode()).thenReturn(m_editNode);
        when(m_editNode.getQName()).thenReturn(m_qname);
        when(m_instance.getModelNodeSchemaPath()).thenReturn(m_schemaPath);
        when(m_instance.getSchemaRegistry()).thenReturn(m_schemaRegistry);
        DataSchemaNode dataSchemaNode = mock(ListSchemaNode.class);
        dataSchemaNodeCollection.add(dataSchemaNode);
        when(m_schemaRegistry.getChildren(m_schemaPath)).thenReturn(dataSchemaNodeCollection);
        when(dataSchemaNode.getQName()).thenReturn(m_qname);
        m_listSchemaNode = (ListSchemaNode) dataSchemaNode;
        when(m_listSchemaNode.isUserOrdered()).thenReturn(true);
        List<QName> keyDefs = new ArrayList<>();
        keyDefs.add(m_keyQname);
        when(m_listSchemaNode.getKeyDefinition()).thenReturn(keyDefs);
        SchemaPath listSchemaPath = SchemaPath.create(true, QName.create("urn:ns", "listNode"));
        when(m_listSchemaNode.getPath()).thenReturn(listSchemaPath);
        m_keyLeafnode = mock(LeafSchemaNode.class);
        when(m_schemaRegistry.getDataSchemaNode(new SchemaPathBuilder().withParent(listSchemaPath).appendQName(m_keyQname).build())).thenReturn(m_keyLeafnode);
    }

    @Test
    public void testExecuteOuter()
            throws CommandExecutionException, EditConfigException, ModelNodeSetException, ModelNodeGetException,
            ModelNodeCreateException {
        initializeExecute();
        Map<QName, ConfigLeafAttribute> map = new HashMap<>();
        map.put(m_qname, new GenericConfigAttribute(m_qname.getLocalName(), null, CONTAINER));
        childList.addAll(m_childList1Helper.getValue(m_parentNode, map));
        Collection<ModelNode> modelNodeCollection = new ArrayList<>();
        modelNodeCollection.add(m_childNode);
        when(m_childList1Helper.getValue(m_instance, Collections.<QName, ConfigLeafAttribute>emptyMap())).thenReturn
                (modelNodeCollection);
        m_oneCommand.addAddInfo(m_childList1Helper, m_instance, m_editContext, m_childNode);
        m_oneCommand.execute();
    }

    @Test
    public void testExecuteOuterChoiceSchema()
            throws CommandExecutionException, EditConfigException, ModelNodeSetException, ModelNodeGetException,
            ModelNodeCreateException {
        when(m_editContext.getEditNode()).thenReturn(m_editNode);
        when(m_editNode.getQName()).thenReturn(m_qname);
        when(m_instance.getModelNodeSchemaPath()).thenReturn(m_schemaPath);
        when(m_instance.getSchemaRegistry()).thenReturn(m_schemaRegistry);
        DataSchemaNode dataSchemaNode = mock(ChoiceSchemaNode.class);
        dataSchemaNodeCollection.add(dataSchemaNode);
        when(dataSchemaNode.getQName()).thenReturn(QName.create(NAMESPACE));
        ChoiceSchemaNode choiceSchemaNode = (ChoiceSchemaNode) dataSchemaNode;
        when(m_schemaRegistry.getChildren(m_schemaPath)).thenReturn(dataSchemaNodeCollection);
        ChoiceCaseNode choice = mock(ChoiceCaseNode.class);
        Set<ChoiceCaseNode> schemaChoiceCases = new HashSet<ChoiceCaseNode>();
        schemaChoiceCases.add(choice);
        when(choiceSchemaNode.getCases()).thenReturn(schemaChoiceCases);
        DataSchemaNode dataSchemaNode2 = mock(ListSchemaNode.class);
        when(choice.getDataChildByName(m_qname)).thenReturn(dataSchemaNode2);
        m_oneCommand.addAddInfo(m_childList1Helper, m_instance, m_editContext, m_childNode);
        m_oneCommand.execute();
    }

    @Test
    public void testExecuteListSchemaNull()
            throws CommandExecutionException, EditConfigException, ModelNodeSetException, ModelNodeGetException,
            ModelNodeCreateException {
        when(m_editContext.getEditNode()).thenReturn(m_editNode);
        when(m_editNode.getQName()).thenReturn(null);
        when(m_instance.getModelNodeSchemaPath()).thenReturn(m_schemaPath);
        when(m_instance.getSchemaRegistry()).thenReturn(m_schemaRegistry);
        m_oneCommand.addAddInfo(m_childList1Helper, m_instance, m_editContext, m_childNode);
        try {
            m_oneCommand.execute();
            fail("should have thrown ModelNodeGetException");
        } catch (Exception e) {
            assertEquals("org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeGetException: Cannot get the " +
                            "schema node for 'null'",
                    e.getMessage());
        }
    }

    @Test
    public void testExecuteOuterWhenInsertOpNotEqual()
            throws CommandExecutionException, EditConfigException, ModelNodeSetException, ModelNodeGetException,
            ModelNodeCreateException {
        initializeExecute();
        InsertOperation insertOperation = new InsertOperation(InsertOperation.FIRST, null);
        when(m_editNode.getInsertOperation()).thenReturn(insertOperation);
        when(((HelperDrivenModelNode) m_instance).getModelNodeHelperRegistry()).thenReturn(m_modelNodeHelperRegistry);
        m_oneCommand.addAddInfo(m_childList1Helper, m_instance, m_editContext, m_childNode);
        m_oneCommand.execute();
    }

    @Test
    public void testExecuteOuterNull()
            throws CommandExecutionException, EditConfigException, ModelNodeSetException, ModelNodeGetException,
            ModelNodeCreateException {
        initializeExecute();
        InsertOperation insertOperation = new InsertOperation(InsertOperation.AFTER, TEST_VALUE);
        when(m_keyLeafnode.getQName()).thenReturn(m_qname);
        when(m_editNode.getInsertOperation()).thenReturn(insertOperation);
        when(((HelperDrivenModelNode) m_instance).getModelNodeHelperRegistry()).thenReturn(m_modelNodeHelperRegistry);
        m_oneCommand.addAddInfo(m_childList1Helper, m_instance, m_editContext, m_childNode);
        try {
            m_oneCommand.execute();
            fail("should have thrown CommandExecutionException");
        } catch (CommandExecutionException e) {
            assertEquals("The instance - testQname getting by key 'test='A'fter' does not exist. Request Failed.", e
                    .getMessage());
        }
    }

    @Test
    public void testExecuteOuterNotNull()
            throws CommandExecutionException, EditConfigException, ModelNodeSetException, ModelNodeGetException,
            ModelNodeCreateException {
        initializeExecute();
        InsertOperation insertOperation = new InsertOperation(InsertOperation.AFTER, TEST_VALUE);
        when(m_keyLeafnode.getQName()).thenReturn(m_qname);
        when(m_editNode.getInsertOperation()).thenReturn(insertOperation);
        when(((HelperDrivenModelNode) m_instance).getModelNodeHelperRegistry()).thenReturn(m_modelNodeHelperRegistry);
        m_oneCommand.addAddInfo(m_childList1Helper, m_instance, m_editContext, m_childNode);
        Map<QName, ConfigLeafAttribute> keyPredicates = new HashMap<>();
        keyPredicates.put(m_keyQname, new GenericConfigAttribute(m_keyQname.getLocalName(), NAMESPACE, "A"));
        childList.add(m_childNode);
        when(m_childList1Helper.getValue(m_instance, keyPredicates)).thenReturn(childList);
        try {
            m_oneCommand.execute();
            fail("should have thrown CommandExecutionException");
        } catch (CommandExecutionException e) {
            assertEquals("The instance - testQname getting by key 'test='A'fter' can't be same the edit node. Request" +
                            " Failed.",
                    e.getMessage());
        }
    }
}
