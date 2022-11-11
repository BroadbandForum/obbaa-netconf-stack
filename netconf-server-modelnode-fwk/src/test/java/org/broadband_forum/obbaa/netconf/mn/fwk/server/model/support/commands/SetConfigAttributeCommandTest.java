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

import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.commands.AbstractCommandTest.EXAMPLE_JUKEBOX_YANG;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.FORMAT;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.FORMAT_QNAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.FORMAT_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JB_NS;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.LOCATION;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.LOCATION_QNAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.LOCATION_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.NAME_QNAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.SINGER_LOCAL_NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.SINGER_QNAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.SINGER_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.SONG_NAME_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.SONG_QNAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.SONG_SCHEMA_PATH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.api.messages.EditConfigOperations;
import org.broadband_forum.obbaa.netconf.api.messages.InsertOperation;
import org.broadband_forum.obbaa.netconf.mn.fwk.AttributeIndex;
import org.broadband_forum.obbaa.netconf.mn.fwk.ChangeTreeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.ChangeTreeNodeImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.WritableChangeTreeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.CommandExecutionException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditChangeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditChangeSource;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.TestEditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeKey;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.dom.YinAnnotationService;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildLeafListHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigAttributeHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.GenericConfigAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.HelperDrivenModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeDeleteException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.SetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;

@RunWith(RequestScopeJunitRunner.class)
public class SetConfigAttributeCommandTest {

    private static final String TEST_LOCAL_NAME = "testLocalName";
    private static final String TEST_JB_NS = "testNamespace";
    private static final String TEST_VALUE = "testValue";
    private static final String TEST_NODE_VALUE1 = "testNodeValue1";
    private static final String TEST_NODE_VALUE2 = "testNodeValue2";
    private static final String TEST_NODE_VALUE3 = "testNodeValue3";
    private static final ModelNodeId FLY_AWAY_MNID = new ModelNodeId("/container=jukebox/container=library/container=artist/name=Lenny/" +
            "container=album/name=Circus/container=song/name='fly away'", JB_NS);
    private static final ModelNodeId LOCATION_MNID = new ModelNodeId("/container=jukebox/container=library/container=artist/name=Lenny/" +
            "container=album/name=Circus/container=song/name='fly away'/container=location", JB_NS);
    private static final ModelNodeId FORMAT_MNID = new ModelNodeId("/container=jukebox/container=library/container=artist/name=Lenny/" +
            "container=album/name=Circus/container=song/name='fly away'/container=format", JB_NS);
    private static final ModelNodeId SINGER_MNID = new ModelNodeId("/container=jukebox/container=library/container=artist/name=Lenny/" +
            "container=album/name=Circus/container=song/name='fly away'/container=singer", JB_NS);

    private QName m_qName;
    private ConfigAttributeHelper m_configAttributeHelper;
    private ChildLeafListHelper m_childLeafListHelper;
    private Map<QName, ConfigAttributeHelper> m_testConfigAttributeHelpers;
    private Map<QName, ChildLeafListHelper> m_testChildLeafListeHelpers;
    private SetConfigAttributeCommand m_setConfigAttributeCommand;
    private EditContext m_testEditContext;
    private EditContainmentNode m_testEditContainmentNode;
    private EditChangeNode m_testEditChangeNode1;
    private EditChangeNode m_testEditChangeNode2;
    private EditChangeNode m_testEditChangeNode3;

    private HelperDrivenModelNode m_testInstance;
    private SchemaRegistry m_testSchemaRegistry;
    private GenericConfigAttribute m_testNodeValue2;
    private GenericConfigAttribute m_testNodeValue3;
    private GenericConfigAttribute m_testNodeValue1;
    private GenericConfigAttribute m_testValue;
    private SchemaRegistry m_schemaRegistry;
    private EditContext m_editContext;
    private HelperDrivenModelNode m_songModelNode;

    private Map<SchemaPath, Map<ModelNodeId, ChangeTreeNode>> m_nodesOfType;
    private Map<SchemaPath, Map<ModelNodeId, ChangeTreeNode>> m_nodesOfTypeWithinSchemaMount;
    private Map<AttributeIndex, Set<ChangeTreeNode>> m_attrIndex;
    private Set<SchemaPath> m_changedNodeSPs;
    private Map<String, Object> m_contextMap;

    public DataSchemaNode m_songDSN;
    private WritableChangeTreeNode m_testChangeTreeNode;

    @Before
    public void initialize() {
        m_qName = QName.create(TEST_JB_NS, TEST_LOCAL_NAME);
        m_testEditChangeNode1 = mock(EditChangeNode.class);
        m_testEditChangeNode2 = mock(EditChangeNode.class);
        m_testEditChangeNode3 = mock(EditChangeNode.class);

        m_testValue = new GenericConfigAttribute(TEST_LOCAL_NAME, TEST_JB_NS, TEST_VALUE);
        m_testNodeValue1 = new GenericConfigAttribute(TEST_LOCAL_NAME, TEST_JB_NS, TEST_NODE_VALUE1);
        m_testNodeValue2 = new GenericConfigAttribute(TEST_LOCAL_NAME, TEST_JB_NS, TEST_NODE_VALUE2);
        m_testNodeValue3 = new GenericConfigAttribute(TEST_LOCAL_NAME, TEST_JB_NS, TEST_NODE_VALUE3);

        m_setConfigAttributeCommand = new SetConfigAttributeCommand();
        m_testConfigAttributeHelpers = new HashMap<>();
        m_testChildLeafListeHelpers = new HashMap<>();

        m_testEditContext = mock(EditContext.class);
        m_testChangeTreeNode = mock(WritableChangeTreeNode.class);
        m_testEditContainmentNode = mock(EditContainmentNode.class);
        m_testInstance = mock(HelperDrivenModelNode.class);
        m_testSchemaRegistry = mock(SchemaRegistry.class);
        m_configAttributeHelper = mock(ConfigAttributeHelper.class);
        m_testConfigAttributeHelpers.put(m_qName, m_configAttributeHelper);
        m_childLeafListHelper = mock(ChildLeafListHelper.class);
        m_testChildLeafListeHelpers.put(m_qName, m_childLeafListHelper);
        when(m_testInstance.getSchemaRegistry()).thenReturn(m_testSchemaRegistry);
    }

    @Test
    public void testExecuteWhenEditConfigOperationIsRemove() throws CommandExecutionException, GetAttributeException, ModelNodeDeleteException {
        initializerForSetConfigAttributeCommandExecute();
        when(m_testEditChangeNode1.getOperation()).thenReturn(EditConfigOperations.REMOVE);
        Collection<ConfigLeafAttribute> listObjects = new ArrayList<>();
        when(m_childLeafListHelper.getValue(m_testInstance)).thenReturn(listObjects);
        m_setConfigAttributeCommand.addSetInfo(m_testSchemaRegistry, m_testConfigAttributeHelpers, m_testChildLeafListeHelpers,
                m_testInstance, m_testEditContext, m_testChangeTreeNode);
        m_setConfigAttributeCommand.execute();
        verify(m_testInstance).interceptEditConfig(m_testEditContext, m_testChangeTreeNode);
        verify(m_childLeafListHelper).removeChild(m_testInstance, m_testNodeValue1);
    }

    @Test
    public void testExecuteWhenLeafIsOrdered() throws GetAttributeException {
        initializerForSetConfigAttributeCommandExecute();
        when(m_testEditChangeNode1.getOperation()).thenReturn(EditConfigOperations.MERGE);
        Collection<ConfigLeafAttribute> listObjects = new ArrayList<>();
        listObjects.add(new GenericConfigAttribute(TEST_LOCAL_NAME, TEST_JB_NS, TEST_NODE_VALUE1));
        when(m_childLeafListHelper.getValue(m_testInstance)).thenReturn(listObjects);
        SchemaPath schemaPath = mock(SchemaPath.class);
        when(m_testInstance.getModelNodeSchemaPath()).thenReturn(schemaPath);
        Collection<DataSchemaNode> dataSchemaNodes = new ArrayList<>();
        DataSchemaNode dataSchemaNode = mock(LeafListSchemaNode.class);
        dataSchemaNodes.add(dataSchemaNode);
        when(m_testSchemaRegistry.getChildren(schemaPath)).thenReturn(dataSchemaNodes);
        QName qName2 = QName.create(TEST_JB_NS, "testNamespace2");
        when(dataSchemaNode.getQName()).thenReturn(qName2);
        m_setConfigAttributeCommand.addSetInfo(m_testSchemaRegistry, m_testConfigAttributeHelpers, m_testChildLeafListeHelpers,
                m_testInstance, m_testEditContext, m_testChangeTreeNode);
        try {
            m_setConfigAttributeCommand.execute();
            fail("Should have thrown an Exception");
        } catch (CommandExecutionException e) {
            assertEquals("org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.SetAttributeException: " +
                    "Cannot get the schema node for '(testNamespace)testLocalName'", e.getMessage());
        }
    }

    @Test
    public void testExecuteWhenLeafIsOrderedofMultiInsert() throws Exception {
        initializerForCommandExecuteOfUserOrdered();
        InsertOperation insertOp1 = InsertOperation.FIRST_OP;
        InsertOperation insertOp2 = InsertOperation.get(InsertOperation.AFTER, TEST_NODE_VALUE2);
        when(m_testEditChangeNode2.getInsertOperation()).thenReturn(insertOp1);
        when(m_testEditChangeNode3.getInsertOperation()).thenReturn(insertOp2);

        ModelNodeDataStoreManager mockDSM = mock(ModelNodeDataStoreManager.class);
        when(m_testInstance.getModelNodeDSM()).thenReturn(mockDSM);
        when(m_testInstance.getSchemaRegistry()).thenReturn(m_testSchemaRegistry);
        when(m_testInstance.getModelNodeId()).thenReturn(mock(ModelNodeId.class));

        when(mockDSM.findNode(Mockito.any(SchemaPath.class), Mockito.any(ModelNodeKey.class), Mockito.any(ModelNodeId.class),
                Mockito.any(SchemaRegistry.class))).thenReturn(m_testInstance);

        m_setConfigAttributeCommand.addSetInfo(m_testSchemaRegistry, m_testConfigAttributeHelpers, m_testChildLeafListeHelpers,
                m_testInstance, m_testEditContext, m_testChangeTreeNode);
        m_setConfigAttributeCommand.execute();

        verify(m_testInstance).interceptEditConfig(m_testEditContext, m_testChangeTreeNode);
        verify(m_childLeafListHelper).addChildByUserOrder(m_testInstance, m_testNodeValue2,
                EditConfigOperations.CREATE, insertOp1);
        verify(m_childLeafListHelper).addChildByUserOrder(m_testInstance, m_testNodeValue3,
                EditConfigOperations.CREATE, insertOp2);
    }

    @Test
    public void testExecuteWhenRollBackThrowsExceptionAtSetValue() throws CommandExecutionException, GetAttributeException {
        initializerForCommandExecute();
        m_setConfigAttributeCommand.execute();
    }

    @Test
    public void testExecuteWhenRollBackThrowsExceptionAtRemoveAllChild() throws CommandExecutionException, GetAttributeException {
        initializerForCommandExecute();
        m_setConfigAttributeCommand.execute();
    }

    @Test
    public void testExecuteWhenRollBackThrowsExceptionAtAddChild() throws CommandExecutionException, GetAttributeException {
        initializerForCommandExecute();
        m_setConfigAttributeCommand.execute();
    }

    @Test
    public void testExecuteWhenChildIsAdded() throws CommandExecutionException, GetAttributeException, SetAttributeException {
        initializerForCommandExecute();
        m_setConfigAttributeCommand.execute();
        verify(m_testInstance).interceptEditConfig(m_testEditContext, m_testChangeTreeNode);
        verify(m_childLeafListHelper).addChild(m_testInstance, m_testNodeValue1);
    }

    @Test
    public void testToString() {
        assertNotNull(m_setConfigAttributeCommand.toString());
    }

    @Test
    public void testAppendChangesForCreatedLeaf() throws SchemaBuildException {
        initializePreRequisiteForLeafNode();
        EditContainmentNode editNode = new TestEditContainmentNode(SONG_QNAME, EditConfigOperations.MERGE, m_schemaRegistry);
        editNode.addMatchNode(NAME_QNAME, new GenericConfigAttribute(NAME, JB_NS, "fly away"));
        EditChangeNode locationCN = new EditChangeNode(LOCATION_QNAME, new GenericConfigAttribute(LOCATION, JB_NS, "desktop/somelocation"));
        editNode.addChangeNode(locationCN);
        editNode.setChangeSource(EditChangeSource.user);
        locationCN.setChangeSource(EditChangeSource.system);
        m_editContext = new EditContext(editNode, null, null, null);

        when(m_songModelNode.getAttribute(NAME_QNAME)).thenReturn(new GenericConfigAttribute(NAME, JB_NS, "fly away"));
        when(m_songModelNode.getAttribute(LOCATION_QNAME)).thenReturn(null);

        m_setConfigAttributeCommand.addSetInfo(m_schemaRegistry, m_testConfigAttributeHelpers, m_testChildLeafListeHelpers, m_songModelNode, m_editContext, m_testChangeTreeNode);

        WritableChangeTreeNode songCTN = new ChangeTreeNodeImpl(m_testSchemaRegistry, null, FLY_AWAY_MNID, m_songDSN, m_nodesOfType,
                m_attrIndex, m_changedNodeSPs, m_contextMap, m_nodesOfTypeWithinSchemaMount);
        songCTN.setMountPoint(true);

        m_setConfigAttributeCommand.appendChange(songCTN, false);

        Map<ModelNodeId, ChangeTreeNode> songNodeChildren = songCTN.getChildren();
        assertEquals(2, songNodeChildren.size());
        assertEquals("location -> create { previousVal = 'null', currentVal = 'desktop/somelocation' }",
                songCTN.getNodesOfType(LOCATION_SCHEMA_PATH).iterator().next().print().trim());
        assertFalse(songNodeChildren.get(LOCATION_MNID).isImplied());
        assertEquals(EditChangeSource.system, songNodeChildren.get(LOCATION_MNID).getChangeSource());
        assertTrue(songCTN.getNodesOfType(LOCATION_SCHEMA_PATH).iterator().next().isMountPoint());
    }

    @Test
    public void testAppendChangesForModifiedLeaf() throws SchemaBuildException {
        initializePreRequisiteForLeafNode();
        EditContainmentNode editNode = new TestEditContainmentNode(SONG_QNAME, EditConfigOperations.MERGE, m_schemaRegistry);
        editNode.addMatchNode(NAME_QNAME, new GenericConfigAttribute(NAME, JB_NS, "fly away"));
        EditChangeNode locationCN = new EditChangeNode(LOCATION_QNAME, new GenericConfigAttribute(LOCATION, JB_NS, "desktop/somelocation"));
        editNode.addChangeNode(locationCN);
        locationCN.setChangeSource(EditChangeSource.user);
        m_editContext = new EditContext(editNode, null, null, null);

        when(m_songModelNode.getAttribute(NAME_QNAME)).thenReturn(new GenericConfigAttribute(NAME, JB_NS, "fly away"));
        when(m_songModelNode.getAttribute(LOCATION_QNAME)).thenReturn(new GenericConfigAttribute(LOCATION, JB_NS, "desktop/oldlocation"));

        m_setConfigAttributeCommand.addSetInfo(m_schemaRegistry, m_testConfigAttributeHelpers, m_testChildLeafListeHelpers, m_songModelNode, m_editContext, m_testChangeTreeNode);

        WritableChangeTreeNode songCTN = new ChangeTreeNodeImpl(m_testSchemaRegistry, null, FLY_AWAY_MNID, m_songDSN, m_nodesOfType,
                m_attrIndex, m_changedNodeSPs, m_contextMap, m_nodesOfTypeWithinSchemaMount);

        m_setConfigAttributeCommand.appendChange(songCTN, false);

        Map<ModelNodeId, ChangeTreeNode> songNodeChildren = songCTN.getChildren();
        assertEquals(2, songNodeChildren.size());
        assertEquals("location -> modify { previousVal = 'desktop/oldlocation', currentVal = 'desktop/somelocation' }",
                songCTN.getNodesOfType(LOCATION_SCHEMA_PATH).iterator().next().print().trim());
        assertFalse(songNodeChildren.get(LOCATION_MNID).isImplied());
        assertEquals(EditChangeSource.user, songNodeChildren.get(LOCATION_MNID).getChangeSource());
        assertFalse(songCTN.getNodesOfType(LOCATION_SCHEMA_PATH).iterator().next().isMountPoint());
    }

    @Test
    public void testAppendChangesForDeletedLeaf() throws SchemaBuildException {
        initializePreRequisiteForLeafNode();
        EditContainmentNode editNode = new TestEditContainmentNode(SONG_QNAME, EditConfigOperations.MERGE, m_schemaRegistry);
        editNode.addMatchNode(NAME_QNAME, new GenericConfigAttribute(NAME, JB_NS, "fly away"));
        EditChangeNode locationCN = new EditChangeNode(LOCATION_QNAME, null);
        editNode.addChangeNode(locationCN);
        locationCN.setChangeSource(EditChangeSource.user);
        m_editContext = new EditContext(editNode, null, null, null);

        when(m_songModelNode.getAttribute(NAME_QNAME)).thenReturn(new GenericConfigAttribute(NAME, JB_NS, "fly away"));
        when(m_songModelNode.getAttribute(LOCATION_QNAME)).thenReturn(new GenericConfigAttribute(LOCATION, JB_NS, "desktop/oldlocation"));

        m_setConfigAttributeCommand.addSetInfo(m_schemaRegistry, m_testConfigAttributeHelpers, m_testChildLeafListeHelpers, m_songModelNode, m_editContext, m_testChangeTreeNode);

        WritableChangeTreeNode songCTN = new ChangeTreeNodeImpl(m_testSchemaRegistry, null, FLY_AWAY_MNID, m_songDSN, m_nodesOfType,
                m_attrIndex, m_changedNodeSPs, m_contextMap, m_nodesOfTypeWithinSchemaMount);

        m_setConfigAttributeCommand.appendChange(songCTN, false);

        Map<ModelNodeId, ChangeTreeNode> songNodeChildren = songCTN.getChildren();
        assertEquals(2, songNodeChildren.size());
        ChangeTreeNode locationCTN = songCTN.getNodesOfType(LOCATION_SCHEMA_PATH).iterator().next();
        assertEquals("location -> delete { previousVal = 'desktop/oldlocation', currentVal = 'null' }", locationCTN.print().trim());
        assertEquals(EditChangeSource.user, locationCTN.getChangeSource());
        assertFalse(songCTN.getNodesOfType(LOCATION_SCHEMA_PATH).iterator().next().isMountPoint());
    }

    @Test
    public void testAppendChangesForLeafWithNoChange() throws SchemaBuildException {
        initializePreRequisiteForLeafNode();
        EditContainmentNode editNode = new TestEditContainmentNode(SONG_QNAME, EditConfigOperations.MERGE, m_schemaRegistry);
        editNode.addMatchNode(NAME_QNAME, new GenericConfigAttribute(NAME, JB_NS, "fly away"));
        EditChangeNode locationCN = new EditChangeNode(LOCATION_QNAME, new GenericConfigAttribute(LOCATION, JB_NS, "desktop/oldlocation"));
        editNode.addChangeNode(locationCN);
        m_editContext = new EditContext(editNode, null, null, null);

        when(m_songModelNode.getAttribute(NAME_QNAME)).thenReturn(new GenericConfigAttribute(NAME, JB_NS, "fly away"));
        when(m_songModelNode.getAttribute(LOCATION_QNAME)).thenReturn(new GenericConfigAttribute(LOCATION, JB_NS, "desktop/oldlocation"));
        m_setConfigAttributeCommand.addSetInfo(m_schemaRegistry, m_testConfigAttributeHelpers, m_testChildLeafListeHelpers, m_songModelNode, m_editContext, m_testChangeTreeNode);

        WritableChangeTreeNode songCTN = new ChangeTreeNodeImpl(m_testSchemaRegistry, null, FLY_AWAY_MNID, m_songDSN, m_nodesOfType,
                m_attrIndex, m_changedNodeSPs, m_contextMap, m_nodesOfTypeWithinSchemaMount);

        m_setConfigAttributeCommand.appendChange(songCTN, false);

        Map<ModelNodeId, ChangeTreeNode> songNodeChildren = songCTN.getChildren();
        assertEquals(2, songNodeChildren.size());
        assertEquals("location -> none { previousVal = 'desktop/oldlocation', currentVal = 'desktop/oldlocation' }",
                songCTN.getNodesOfType(LOCATION_SCHEMA_PATH).iterator().next().print().trim());
        assertFalse(songCTN.getNodesOfType(LOCATION_SCHEMA_PATH).iterator().next().isMountPoint());
    }

    @Test
    public void testAppendChangesSimultaneousModifyAndRemoveOfLeaves() throws SchemaBuildException {
        initializePreRequisiteForLeafNode();
        EditContainmentNode editNode = new TestEditContainmentNode(SONG_QNAME, EditConfigOperations.MERGE, m_schemaRegistry);
        editNode.addMatchNode(NAME_QNAME, new GenericConfigAttribute(NAME, JB_NS, "fly away"));
        EditChangeNode locationCN = new EditChangeNode(LOCATION_QNAME, new GenericConfigAttribute(LOCATION, JB_NS, "desktop/new-location"));
        editNode.addChangeNode(locationCN);
        EditChangeNode formatCN = new EditChangeNode(FORMAT_QNAME, new GenericConfigAttribute(FORMAT, JB_NS, "mp3"));
        formatCN.setOperation(EditConfigOperations.REMOVE); //format leaf with remove operation
        editNode.addChangeNode(formatCN);
        m_editContext = new EditContext(editNode, null, null, null);

        when(m_songModelNode.getAttribute(NAME_QNAME)).thenReturn(new GenericConfigAttribute(NAME, JB_NS, "fly away"));
        when(m_songModelNode.getAttribute(LOCATION_QNAME)).thenReturn(new GenericConfigAttribute(LOCATION, JB_NS, "desktop/old-location"));
        when(m_songModelNode.getAttribute(FORMAT_QNAME)).thenReturn(new GenericConfigAttribute(FORMAT, JB_NS, "mp3"));

        m_setConfigAttributeCommand.addSetInfo(m_schemaRegistry, m_testConfigAttributeHelpers, m_testChildLeafListeHelpers, m_songModelNode, m_editContext, m_testChangeTreeNode);

        WritableChangeTreeNode songCTN = new ChangeTreeNodeImpl(m_testSchemaRegistry, null, FLY_AWAY_MNID, m_songDSN, m_nodesOfType,
                m_attrIndex, m_changedNodeSPs, m_contextMap, m_nodesOfTypeWithinSchemaMount);
        songCTN.setMountPoint(true);

        m_setConfigAttributeCommand.appendChange(songCTN, false);

        Map<ModelNodeId, ChangeTreeNode> songNodeChildren = songCTN.getChildren();
        assertEquals(2, songNodeChildren.size());
        assertEquals("location -> modify { previousVal = 'desktop/old-location', currentVal = 'desktop/new-location' }",
                songCTN.getNodesOfType(LOCATION_SCHEMA_PATH).iterator().next().print().trim());
        assertTrue(songCTN.getNodesOfType(LOCATION_SCHEMA_PATH).iterator().next().isMountPoint());
        assertNull(songNodeChildren.get(FORMAT_MNID));
    }

    @Test
    public void testAppendChangesSimultaneousModifyAndDeleteOfLeaves() throws SchemaBuildException {
        initializePreRequisiteForLeafNode();
        EditContainmentNode editNode = new TestEditContainmentNode(SONG_QNAME, EditConfigOperations.MERGE, m_schemaRegistry);
        editNode.addMatchNode(NAME_QNAME, new GenericConfigAttribute(NAME, JB_NS, "fly away"));
        EditChangeNode locationCN = new EditChangeNode(LOCATION_QNAME, new GenericConfigAttribute(LOCATION, JB_NS, "desktop/new-location"));
        editNode.addChangeNode(locationCN);
        EditChangeNode formatCN = new EditChangeNode(FORMAT_QNAME, new GenericConfigAttribute(FORMAT, JB_NS, "mp3"));
        formatCN.setOperation(EditConfigOperations.DELETE); //format leaf with delete operation
        editNode.addChangeNode(formatCN);
        m_editContext = new EditContext(editNode, null, null, null);

        when(m_songModelNode.getAttribute(NAME_QNAME)).thenReturn(new GenericConfigAttribute(NAME, JB_NS, "fly away"));
        when(m_songModelNode.getAttribute(LOCATION_QNAME)).thenReturn(new GenericConfigAttribute(LOCATION, JB_NS, "desktop/old-location"));
        when(m_songModelNode.getAttribute(FORMAT_QNAME)).thenReturn(new GenericConfigAttribute(FORMAT, JB_NS, "mp3"));

        m_setConfigAttributeCommand.addSetInfo(m_schemaRegistry, m_testConfigAttributeHelpers, m_testChildLeafListeHelpers, m_songModelNode, m_editContext, m_testChangeTreeNode);

        WritableChangeTreeNode songCTN = new ChangeTreeNodeImpl(m_testSchemaRegistry, null, FLY_AWAY_MNID, m_songDSN, m_nodesOfType,
                m_attrIndex, m_changedNodeSPs, m_contextMap, m_nodesOfTypeWithinSchemaMount);
        songCTN.setMountPoint(true);

        m_setConfigAttributeCommand.appendChange(songCTN, false);

        Map<ModelNodeId, ChangeTreeNode> songNodeChildren = songCTN.getChildren();
        assertEquals(2, songNodeChildren.size());
        assertEquals("location -> modify { previousVal = 'desktop/old-location', currentVal = 'desktop/new-location' }",
                songCTN.getNodesOfType(LOCATION_SCHEMA_PATH).iterator().next().print().trim());
        assertTrue(songCTN.getNodesOfType(LOCATION_SCHEMA_PATH).iterator().next().isMountPoint());
        assertNull(songNodeChildren.get(FORMAT_MNID));
    }

    @Test
    public void testAppendChangesForCreatedLeafLists() throws SchemaBuildException {
        initializePreRequisiteForLeafListNode();
        EditContainmentNode editNode = new TestEditContainmentNode(SONG_QNAME, EditConfigOperations.MERGE, m_schemaRegistry);
        editNode.addMatchNode(NAME_QNAME, new GenericConfigAttribute(NAME, JB_NS, "fly away"));
        ConfigLeafAttribute singer1Attribute = new GenericConfigAttribute(SINGER_LOCAL_NAME, JB_NS, "NewSinger");
        ConfigLeafAttribute singer2Attribute = new GenericConfigAttribute(SINGER_LOCAL_NAME, JB_NS, "NewSinger2");
        EditChangeNode singerCN = new EditChangeNode(SINGER_QNAME, singer1Attribute);
        singerCN.setOperation(EditConfigOperations.CREATE);
        singerCN.setChangeSource(EditChangeSource.user);
        EditChangeNode singer2CN = new EditChangeNode(SINGER_QNAME, singer2Attribute);
        singer2CN.setOperation(EditConfigOperations.CREATE);
        singer2CN.setChangeSource(EditChangeSource.user);
        editNode.addChangeNode(singerCN);
        editNode.addChangeNode(singer2CN);
        m_editContext = new EditContext(editNode, null, null, null);

        when(m_songModelNode.getAttribute(NAME_QNAME)).thenReturn(new GenericConfigAttribute(NAME, JB_NS, "fly away"));
        when(m_songModelNode.getLeafList(SINGER_QNAME)).thenReturn(null);

        m_setConfigAttributeCommand.addSetInfo(m_schemaRegistry, m_testConfigAttributeHelpers, m_testChildLeafListeHelpers, m_songModelNode, m_editContext, m_testChangeTreeNode);

        WritableChangeTreeNode songCTN = new ChangeTreeNodeImpl(m_testSchemaRegistry, null, FLY_AWAY_MNID, m_songDSN, m_nodesOfType,
                m_attrIndex, m_changedNodeSPs, m_contextMap, m_nodesOfTypeWithinSchemaMount);
        songCTN.setMountPoint(true);

        m_setConfigAttributeCommand.appendChange(songCTN, false);

        Map<ModelNodeId, ChangeTreeNode> songNodeChildren = songCTN.getChildren();
        assertEquals(2, songNodeChildren.size());
        assertEquals("singer -> create { previousVal = 'null', currentVal = 'NewSinger' }\n" +
                "singer -> create { previousVal = 'null', currentVal = 'NewSinger2' }", songCTN.getNodesOfType(SINGER_SCHEMA_PATH).
                iterator().next().print().trim());
        assertFalse(songNodeChildren.get(SINGER_MNID).isImplied());
        assertEquals(EditChangeSource.user, songNodeChildren.get(SINGER_MNID).getChangeSource());
        assertTrue(songCTN.getNodesOfType(SINGER_SCHEMA_PATH).iterator().next().isMountPoint());
    }

    @Test
    public void testAppendChangesForDeletedLeafLists() throws SchemaBuildException {
        initializePreRequisiteForLeafListNode();
        EditContainmentNode editNode = new TestEditContainmentNode(SONG_QNAME, EditConfigOperations.MERGE, m_schemaRegistry);
        editNode.addMatchNode(NAME_QNAME, new GenericConfigAttribute(NAME, JB_NS, "fly away"));
        ConfigLeafAttribute singer1Attribute = new GenericConfigAttribute(SINGER_LOCAL_NAME, JB_NS, "NewSinger");
        ConfigLeafAttribute singer2Attribute = new GenericConfigAttribute(SINGER_LOCAL_NAME, JB_NS, "NewSinger2");
        EditChangeNode singerCN = new EditChangeNode(SINGER_QNAME, singer1Attribute);
        singerCN.setOperation(EditConfigOperations.DELETE);
        EditChangeNode singer2CN = new EditChangeNode(SINGER_QNAME, singer2Attribute);
        singer2CN.setOperation(EditConfigOperations.DELETE);
        editNode.addChangeNode(singerCN);
        editNode.addChangeNode(singer2CN);
        m_editContext = new EditContext(editNode, null, null, null);

        Set<ConfigLeafAttribute> leafListAttributes = new LinkedHashSet<>();
        leafListAttributes.add(singer1Attribute);
        leafListAttributes.add(singer2Attribute);
        when(m_songModelNode.getAttribute(NAME_QNAME)).thenReturn(new GenericConfigAttribute(NAME, JB_NS, "fly away"));
        when(m_songModelNode.getLeafList(SINGER_QNAME)).thenReturn(leafListAttributes);

        m_setConfigAttributeCommand.addSetInfo(m_schemaRegistry, m_testConfigAttributeHelpers, m_testChildLeafListeHelpers, m_songModelNode, m_editContext, m_testChangeTreeNode);

        WritableChangeTreeNode songCTN = new ChangeTreeNodeImpl(m_testSchemaRegistry, null, FLY_AWAY_MNID, m_songDSN, m_nodesOfType, m_attrIndex,
                m_changedNodeSPs, m_contextMap, m_nodesOfTypeWithinSchemaMount);
        songCTN.setMountPoint(true);

        m_setConfigAttributeCommand.appendChange(songCTN, false);

        Map<ModelNodeId, ChangeTreeNode> songNodeChildren = songCTN.getChildren();
        assertEquals(2, songNodeChildren.size());
        assertEquals("singer -> delete { previousVal = 'NewSinger', currentVal = 'null' }\n" +
                "singer -> delete { previousVal = 'NewSinger2', currentVal = 'null' }", songCTN.getNodesOfType(SINGER_SCHEMA_PATH).iterator().next()
                .print().trim());
        assertTrue(songCTN.getNodesOfType(SINGER_SCHEMA_PATH).iterator().next().isMountPoint());
    }

    @Test
    public void testAppendChangesForModifiedLeafLists() throws SchemaBuildException {
        initializePreRequisiteForLeafListNode();
        EditContainmentNode editNode = new TestEditContainmentNode(SONG_QNAME, EditConfigOperations.MERGE, m_schemaRegistry);
        editNode.addMatchNode(NAME_QNAME, new GenericConfigAttribute(NAME, JB_NS, "fly away"));
        ConfigLeafAttribute singer1Attribute = new GenericConfigAttribute(SINGER_LOCAL_NAME, JB_NS, "NewSinger");
        ConfigLeafAttribute singer2Attribute = new GenericConfigAttribute(SINGER_LOCAL_NAME, JB_NS, "NewSinger2");
        ConfigLeafAttribute singer3Attribute = new GenericConfigAttribute(SINGER_LOCAL_NAME, JB_NS, "NewSinger3");
        EditChangeNode singer2CN = new EditChangeNode(SINGER_QNAME, singer2Attribute);
        singer2CN.setOperation(EditConfigOperations.DELETE);
        EditChangeNode singer3CN = new EditChangeNode(SINGER_QNAME, singer3Attribute);
        singer3CN.setOperation(EditConfigOperations.CREATE);
        editNode.addChangeNode(singer2CN);
        editNode.addChangeNode(singer3CN);
        m_editContext = new EditContext(editNode, null, null, null);

        Set<ConfigLeafAttribute> leafListAttributes = new LinkedHashSet<>();
        leafListAttributes.add(singer1Attribute);
        leafListAttributes.add(singer2Attribute);
        when(m_songModelNode.getAttribute(NAME_QNAME)).thenReturn(new GenericConfigAttribute(NAME, JB_NS, "fly away"));
        when(m_songModelNode.getLeafList(SINGER_QNAME)).thenReturn(leafListAttributes);

        m_setConfigAttributeCommand.addSetInfo(m_schemaRegistry, m_testConfigAttributeHelpers, m_testChildLeafListeHelpers, m_songModelNode, m_editContext, m_testChangeTreeNode);

        WritableChangeTreeNode songCTN = new ChangeTreeNodeImpl(m_testSchemaRegistry, null, FLY_AWAY_MNID, m_songDSN, m_nodesOfType, m_attrIndex,
                m_changedNodeSPs, m_contextMap, m_nodesOfTypeWithinSchemaMount);

        m_setConfigAttributeCommand.appendChange(songCTN, false);

        Map<ModelNodeId, ChangeTreeNode> songNodeChildren = songCTN.getChildren();
        assertEquals(2, songNodeChildren.size());
        assertEquals("singer -> modify { previousVal = 'NewSinger', currentVal = 'NewSinger' }\n" +
                        "singer -> modify { previousVal = 'NewSinger2', currentVal = 'null' }\n" + "singer -> modify { previousVal = 'null', currentVal = 'NewSinger3' }",
                songCTN.getNodesOfType(SINGER_SCHEMA_PATH).iterator().next().print().trim());
        assertFalse(songCTN.getNodesOfType(SINGER_SCHEMA_PATH).iterator().next().isMountPoint());
    }

    @Test
    public void testAppendChangesForNoChangesToLeafLists() throws SchemaBuildException {
        initializePreRequisiteForLeafListNode();
        EditContainmentNode editNode = new TestEditContainmentNode(SONG_QNAME, EditConfigOperations.MERGE, m_schemaRegistry);
        editNode.addMatchNode(NAME_QNAME, new GenericConfigAttribute(NAME, JB_NS, "fly away"));
        ConfigLeafAttribute singer1Attribute = new GenericConfigAttribute(SINGER_LOCAL_NAME, JB_NS, "NewSinger");
        ConfigLeafAttribute singer2Attribute = new GenericConfigAttribute(SINGER_LOCAL_NAME, JB_NS, "NewSinger2");
        EditChangeNode singerCN = new EditChangeNode(SINGER_QNAME, singer1Attribute);
        EditChangeNode singer2CN = new EditChangeNode(SINGER_QNAME, singer2Attribute);
        editNode.addChangeNode(singerCN);
        editNode.addChangeNode(singer2CN);
        m_editContext = new EditContext(editNode, null, null, null);

        Set<ConfigLeafAttribute> leafListAttributes = new LinkedHashSet<>();
        leafListAttributes.add(singer1Attribute);
        leafListAttributes.add(singer2Attribute);
        when(m_songModelNode.getAttribute(NAME_QNAME)).thenReturn(new GenericConfigAttribute(NAME, JB_NS, "fly away"));
        when(m_songModelNode.getLeafList(SINGER_QNAME)).thenReturn(leafListAttributes);

        m_setConfigAttributeCommand.addSetInfo(m_schemaRegistry, m_testConfigAttributeHelpers, m_testChildLeafListeHelpers,
                m_songModelNode, m_editContext, m_testChangeTreeNode);

        WritableChangeTreeNode songCTN = new ChangeTreeNodeImpl(m_testSchemaRegistry, null, FLY_AWAY_MNID, m_songDSN,
                m_nodesOfType, m_attrIndex, m_changedNodeSPs, m_contextMap, m_nodesOfTypeWithinSchemaMount);

        m_setConfigAttributeCommand.appendChange(songCTN, false);

        Map<ModelNodeId, ChangeTreeNode> songNodeChildren = songCTN.getChildren();
        assertEquals(2, songNodeChildren.size());
        assertEquals("singer -> none { previousVal = 'NewSinger', currentVal = 'NewSinger' }\n" +
                        "singer -> none { previousVal = 'NewSinger2', currentVal = 'NewSinger2' }",
                songCTN.getNodesOfType(SINGER_SCHEMA_PATH).iterator().next().print().trim());
        assertFalse(songCTN.getNodesOfType(SINGER_SCHEMA_PATH).iterator().next().isMountPoint());
    }

    private void initializerForSetConfigAttributeCommandExecute() {
        when(m_testEditChangeNode1.getQName()).thenReturn(m_qName);
        when(m_testEditContext.getEditNode()).thenReturn(m_testEditContainmentNode);
        List<EditChangeNode> listEditChangeNodes = new ArrayList<>();
        listEditChangeNodes.add(m_testEditChangeNode1);
        when(m_testEditChangeNode1.getConfigLeafAttribute()).thenReturn(m_testNodeValue1);
        when(m_testEditChangeNode1.getValue()).thenReturn(TEST_NODE_VALUE1);
        when(m_testEditContext.getEditNode().getChangeNodes()).thenReturn(listEditChangeNodes);
        when(m_testEditContext.getEditNode().getEditOperation()).thenReturn(EditConfigOperations.MERGE);
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
        listObjects.add(new GenericConfigAttribute(TEST_LOCAL_NAME, TEST_JB_NS, TEST_NODE_VALUE1));
        when(m_childLeafListHelper.getValue(m_testInstance)).thenReturn(listObjects);
        when(m_testEditChangeNode1.getOperation()).thenReturn(EditConfigOperations.MERGE);
        m_setConfigAttributeCommand.addSetInfo(m_testSchemaRegistry, m_testConfigAttributeHelpers, m_testChildLeafListeHelpers,
                m_testInstance, m_testEditContext, m_testChangeTreeNode);
    }

    private void initializerForCommandExecuteOfUserOrdered() throws GetAttributeException {
        when(m_testEditChangeNode2.getQName()).thenReturn(m_qName);
        when(m_testEditChangeNode3.getQName()).thenReturn(m_qName);
        when(m_testEditContext.getEditNode()).thenReturn(m_testEditContainmentNode);
        List<EditChangeNode> listEditChangeNodes = new ArrayList<>();
        listEditChangeNodes.add(m_testEditChangeNode2);
        listEditChangeNodes.add(m_testEditChangeNode3);
        when(m_testEditChangeNode2.getConfigLeafAttribute()).thenReturn(m_testNodeValue2);
        when(m_testEditChangeNode2.getValue()).thenReturn(TEST_NODE_VALUE2);
        when(m_testEditChangeNode3.getConfigLeafAttribute()).thenReturn(m_testNodeValue3);
        when(m_testEditChangeNode3.getValue()).thenReturn(TEST_NODE_VALUE3);
        when(m_testEditContext.getEditNode().getChangeNodes()).thenReturn(listEditChangeNodes);
        when(m_testEditContext.getEditNode().getEditOperation()).thenReturn(EditConfigOperations.MERGE);
        SchemaPath schemaPath = mock(SchemaPath.class);
        when(m_testInstance.getModelNodeSchemaPath()).thenReturn(schemaPath);
        Collection<DataSchemaNode> dataSchemaNodes = new ArrayList<>();
        DataSchemaNode dataSchemaNode = mock(LeafListSchemaNode.class);
        dataSchemaNodes.add(dataSchemaNode);
        when(m_testSchemaRegistry.getChildren(schemaPath)).thenReturn(dataSchemaNodes);
        when(dataSchemaNode.getQName()).thenReturn(m_qName);
        when(m_configAttributeHelper.getValue(m_testInstance)).thenReturn(m_testValue);

        Collection<ConfigLeafAttribute> listObjects = new ArrayList<>();
        listObjects.add(new GenericConfigAttribute(TEST_LOCAL_NAME, TEST_JB_NS, TEST_NODE_VALUE1));

        Collection<ConfigLeafAttribute> listObjects2 = new ArrayList<>();
        listObjects2.add(new GenericConfigAttribute(TEST_LOCAL_NAME, TEST_JB_NS, TEST_NODE_VALUE2));
        listObjects2.add(new GenericConfigAttribute(TEST_LOCAL_NAME, TEST_JB_NS, TEST_NODE_VALUE1));

        when(m_childLeafListHelper.getValue(m_testInstance)).thenReturn(listObjects).thenReturn(listObjects2);

        when(m_testEditChangeNode2.getOperation()).thenReturn(EditConfigOperations.CREATE);
        when(m_testEditChangeNode3.getOperation()).thenReturn(EditConfigOperations.CREATE);
        m_setConfigAttributeCommand.addSetInfo(m_testSchemaRegistry, m_testConfigAttributeHelpers, m_testChildLeafListeHelpers,
                m_testInstance, m_testEditContext, m_testChangeTreeNode);

        when(((LeafListSchemaNode) dataSchemaNode).isUserOrdered()).thenReturn(true);
    }

    private void initializeJukeboxModel() throws SchemaBuildException {
        List<YangTextSchemaSource> yangs = TestUtil.getJukeBoxDeps();
        yangs.add(TestUtil.getByteSource(EXAMPLE_JUKEBOX_YANG));
        m_schemaRegistry = new SchemaRegistryImpl(yangs, Collections.emptySet(), Collections.emptyMap(), new NoLockService());
        m_songModelNode = mock(HelperDrivenModelNode.class);
        when(m_songModelNode.getModelNodeId()).thenReturn(FLY_AWAY_MNID);
        when(m_songModelNode.getSchemaRegistry()).thenReturn(m_testSchemaRegistry);
        YinAnnotationService yinService = mock(YinAnnotationService.class);
        when(m_testSchemaRegistry.getYinAnnotationService()).thenReturn(yinService);
        when(yinService.isPassword(Mockito.any(),Mockito.anyString())).thenReturn(false);
        m_nodesOfType = new HashMap<>();
        m_nodesOfTypeWithinSchemaMount = new HashMap<>();
        m_attrIndex = new HashMap<>();
        m_changedNodeSPs = new HashSet<>();
        m_contextMap = new HashMap<>();
        m_songDSN = m_schemaRegistry.getDataSchemaNode(SONG_SCHEMA_PATH);
    }

    private void initializePreRequisiteForLeafNode() throws SchemaBuildException {
        initializeJukeboxModel();

        ConfigAttributeHelper songNameHelper = mock(ConfigAttributeHelper.class);
        LeafSchemaNode songNameDSN = (LeafSchemaNode) m_schemaRegistry.getDataSchemaNode(SONG_NAME_SCHEMA_PATH);
        when(songNameHelper.getLeafSchemaNode()).thenReturn(songNameDSN);

        ConfigAttributeHelper locationHelper = mock(ConfigAttributeHelper.class);
        LeafSchemaNode locationDSN = (LeafSchemaNode) m_schemaRegistry.getDataSchemaNode(LOCATION_SCHEMA_PATH);
        when(locationHelper.getLeafSchemaNode()).thenReturn(locationDSN);

        ConfigAttributeHelper formatHelper = mock(ConfigAttributeHelper.class);
        LeafSchemaNode formatDSN = (LeafSchemaNode) m_schemaRegistry.getDataSchemaNode(FORMAT_SCHEMA_PATH);
        when(formatHelper.getLeafSchemaNode()).thenReturn(formatDSN);

        m_testConfigAttributeHelpers.put(NAME_QNAME, songNameHelper);
        m_testConfigAttributeHelpers.put(LOCATION_QNAME, locationHelper);
        m_testConfigAttributeHelpers.put(FORMAT_QNAME, formatHelper);
    }

    private void initializePreRequisiteForLeafListNode() throws SchemaBuildException {
        initializeJukeboxModel();

        ConfigAttributeHelper songNameHelper = mock(ConfigAttributeHelper.class);
        LeafSchemaNode songNameDSN = (LeafSchemaNode) m_schemaRegistry.getDataSchemaNode(SONG_NAME_SCHEMA_PATH);
        when(songNameHelper.getLeafSchemaNode()).thenReturn(songNameDSN);

        ChildLeafListHelper singerCLLHelper = mock(ChildLeafListHelper.class);
        LeafListSchemaNode singerDSN = (LeafListSchemaNode) m_schemaRegistry.getDataSchemaNode(SINGER_SCHEMA_PATH);
        when(singerCLLHelper.getLeafListSchemaNode()).thenReturn(singerDSN);

        m_testConfigAttributeHelpers.put(NAME_QNAME, songNameHelper);
        m_testChildLeafListeHelpers.put(SINGER_QNAME, singerCLLHelper);
    }
}
