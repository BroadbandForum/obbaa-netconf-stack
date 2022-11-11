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
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.commands.AbstractCommandTest.GREATEST_HITS_ID;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.commands.AbstractCommandTest.LENNY_MNID;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ALBUM_QNAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ALBUM_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ARTIST_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.GENRE_QNAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JB_NS;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.NAME_QNAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.YEAR;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.YEAR_QNAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.broadband_forum.obbaa.netconf.api.messages.EditConfigOperations;
import org.broadband_forum.obbaa.netconf.api.messages.InsertOperation;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder;
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
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditConfigException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.TestEditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildListHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.GenericConfigAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.HelperDrivenModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.IdentityRefConfigAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeGetException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;

@RunWith(RequestScopeJunitRunner.class)
public class MergeListCommandTest {

    private static final String CONTAINER = "container";
    private static final String TEST_VALUE = "test='A'fter";
    private static final String NAMESPACE = "namespace";

    private MergeListCommand m_mergeListCommand;
    private ChildListHelper m_childListHelper;
    private QName m_qname;
    private EditContext m_editContext;
    private ModelNode m_instance;
    private ModelNode m_childNode;
    private ModelNode m_parentNode;
    private ModelNodeHelperRegistry m_modelNodeHelperRegistry;
    private EditContainmentNode m_editNode;
    private SchemaPath m_schemaPath;
    private SchemaRegistry m_schemaRegistry;
    private Collection<ModelNode> childList;
    private Collection<DataSchemaNode> dataSchemaNodeCollection;
    private ListSchemaNode m_listSchemaNode;
    private QName m_keyQname;
    private LeafSchemaNode m_keyLeafnode;
    private HelperDrivenModelNode m_artistModelNode;
    private HelperDrivenModelNode m_albumNodelNode;

    private Map<SchemaPath, Map<ModelNodeId, ChangeTreeNode>> m_nodesOfType;
    private Map<SchemaPath, Map<ModelNodeId, ChangeTreeNode>> m_nodesOfTypeWithinSchemaMount;
    private Map<AttributeIndex, Set<ChangeTreeNode>> m_attrIndex;
    private Set<SchemaPath> m_changedNodeSPs;
    private Map<String, Object> m_contextMap;

    public DataSchemaNode m_artistDSN;
    public DataSchemaNode m_albumDSN;

    @Before
    public void setUp() {
        m_mergeListCommand = new MergeListCommand();
        m_childListHelper = mock(ChildListHelper.class);
        m_editContext = mock(EditContext.class);
        m_instance = mock(HelperDrivenModelNode.class);
        m_childNode = mock(ModelNode.class);
        m_parentNode = mock(ModelNode.class);
        m_modelNodeHelperRegistry = mock(ModelNodeHelperRegistry.class);
        m_editNode = mock(EditContainmentNode.class);
        m_schemaPath = mock(SchemaPath.class);
        m_schemaRegistry = mock(SchemaRegistry.class);
        childList = new ArrayList<>();
        dataSchemaNodeCollection = new ArrayList<>();
        m_qname = QName.create(NAMESPACE, "testQname");
        m_keyQname = QName.create(NAMESPACE, "est");
    }

    @Test
    public void testExecuteOuter() throws CommandExecutionException, EditConfigException, ModelNodeGetException { //useless
        initializeExecute();
        Map<QName, ConfigLeafAttribute> map = new HashMap<>();
        map.put(m_qname, new GenericConfigAttribute(m_qname.getLocalName(), null, CONTAINER));
        childList.addAll(m_childListHelper.getValue(m_parentNode, map));
        Collection<ModelNode> modelNodeCollection = new ArrayList<>();
        modelNodeCollection.add(m_childNode);
        when(m_editContext.getEditNode()).thenReturn(m_editNode);
        when(m_editNode.getInsertOperation()).thenReturn(InsertOperation.FIRST_OP);
        when(m_childListHelper.getValue(m_instance, Collections.emptyMap())).thenReturn(modelNodeCollection);
        when(m_childNode.getEditCommand(any(EditContext.class), any(WritableChangeTreeNode.class))).thenReturn(mock(CompositeEditCommand.class));
        m_mergeListCommand.addAddInfo(m_childListHelper, m_instance, m_editContext, m_childNode, null);
        m_mergeListCommand.execute();
    }

    @Test
    public void testExecuteOuterChoiceSchema() throws CommandExecutionException, EditConfigException,
            ModelNodeGetException {
        when(m_editContext.getEditNode()).thenReturn(m_editNode);
        when(m_editNode.getQName()).thenReturn(m_qname);
        when(m_editNode.getInsertOperation()).thenReturn(InsertOperation.FIRST_OP);
        when(m_instance.getModelNodeSchemaPath()).thenReturn(m_schemaPath);
        when(m_instance.getSchemaRegistry()).thenReturn(m_schemaRegistry);
        DataSchemaNode dataSchemaNode = mock(ChoiceSchemaNode.class);
        dataSchemaNodeCollection.add(dataSchemaNode);
        when(dataSchemaNode.getQName()).thenReturn(QName.create(NAMESPACE, "somenode"));
        ChoiceSchemaNode choiceSchemaNode = (ChoiceSchemaNode) dataSchemaNode;
        when(m_schemaRegistry.getChildren(m_schemaPath)).thenReturn(dataSchemaNodeCollection);
        CaseSchemaNode choiceCase = mock(CaseSchemaNode.class);
        QName choiceCaseQName = QName.create(NAMESPACE, "case");
        SortedMap<QName, CaseSchemaNode> schemaChoiceCases = new TreeMap<QName, CaseSchemaNode>();
        schemaChoiceCases.put(choiceCaseQName, choiceCase);
        when(choiceSchemaNode.getCases()).thenReturn(schemaChoiceCases);
        DataSchemaNode dataSchemaNode2 = mock(ListSchemaNode.class);
        when(dataSchemaNode2.getQName()).thenReturn(m_qname);
        when(choiceCase.getChildNodes()).thenReturn(Arrays.asList(dataSchemaNode2));
        when(m_childNode.getEditCommand(any(EditContext.class), any(WritableChangeTreeNode.class))).thenReturn(mock(CompositeEditCommand.class));
        m_mergeListCommand.addAddInfo(m_childListHelper, m_instance, m_editContext, m_childNode, null);
        m_mergeListCommand.execute();
    }

    @Test
    public void testExecuteListSchemaNull() throws EditConfigException, ModelNodeGetException {
        when(m_editContext.getEditNode()).thenReturn(m_editNode);
        when(m_editNode.getQName()).thenReturn(null);
        when(m_editNode.getInsertOperation()).thenReturn(InsertOperation.get("after", "dummy"));
        when(m_instance.getModelNodeSchemaPath()).thenReturn(m_schemaPath);
        when(m_instance.getSchemaRegistry()).thenReturn(m_schemaRegistry);
        m_mergeListCommand.addAddInfo(m_childListHelper, m_instance, m_editContext, m_childNode, null);
        try {
            m_mergeListCommand.populateAdditionalInfo();
            m_mergeListCommand.execute();
            fail("should have thrown ModelNodeCreateException");
        } catch (Exception e) {
            assertEquals("Cannot get the schema node for 'null'", e.getMessage());
        }
    }

    @Test
    public void testExecuteOuterWhenInsertOpNotEqual()
            throws CommandExecutionException, EditConfigException, ModelNodeGetException {
        initializeExecute();
        InsertOperation insertOperation = InsertOperation.FIRST_OP;
        when(m_editNode.getInsertOperation()).thenReturn(insertOperation);
        when(((HelperDrivenModelNode) m_instance).getModelNodeHelperRegistry()).thenReturn(m_modelNodeHelperRegistry);
        when(m_childNode.getEditCommand(any(EditContext.class), any(WritableChangeTreeNode.class))).thenReturn(mock(CompositeEditCommand.class));
        m_mergeListCommand.addAddInfo(m_childListHelper, m_instance, m_editContext, m_childNode, null);
        m_mergeListCommand.execute();
    }

    @Test
    public void testExecuteOuterNull() throws EditConfigException, ModelNodeGetException {
        initializeExecute();
        InsertOperation insertOperation = InsertOperation.get(InsertOperation.AFTER, TEST_VALUE);
        when(m_keyLeafnode.getQName()).thenReturn(m_qname);
        when(m_editNode.getInsertOperation()).thenReturn(insertOperation);
        when(((HelperDrivenModelNode) m_instance).getModelNodeHelperRegistry()).thenReturn(m_modelNodeHelperRegistry);
        m_mergeListCommand.addAddInfo(m_childListHelper, m_instance, m_editContext, m_childNode, null);
        try {
            m_mergeListCommand.execute();
            fail("should have thrown CommandExecutionException");
        } catch (CommandExecutionException e) {
            assertEquals("The instance - testQname getting by key 'test='A'fter' does not exist. Request Failed.", e.getMessage());
        }
    }

    @Test
    public void testExecuteOuterNotNull() throws EditConfigException, ModelNodeGetException {
        initializeExecute();
        InsertOperation insertOperation = InsertOperation.get(InsertOperation.AFTER, TEST_VALUE);

        when(m_keyLeafnode.getQName()).thenReturn(m_keyQname);
        when(m_editNode.getInsertOperation()).thenReturn(insertOperation);
        when(((HelperDrivenModelNode) m_instance).getModelNodeHelperRegistry()).thenReturn(m_modelNodeHelperRegistry);
        m_mergeListCommand.addAddInfo(m_childListHelper, m_instance, m_editContext, m_childNode, null);
        Map<QName, ConfigLeafAttribute> keyPredicates = new HashMap<>();
        keyPredicates.put(m_keyQname, new GenericConfigAttribute(m_keyQname.getLocalName(), NAMESPACE, "A"));
        childList.add(m_childNode);
        when(m_childListHelper.getValue(m_instance, keyPredicates)).thenReturn(childList);
        try {
            m_mergeListCommand.populateAdditionalInfo();
            m_mergeListCommand.execute();
            fail("should have thrown CommandExecutionException");
        } catch (Exception e) {
            assertEquals("The instance - testQname getting by key 'test='A'fter' can't be same as the edit node. Request Failed.",
                    e.getMessage());
        }
    }

    @Test
    public void testAppendChangesAppendsInsertOperationInfo() throws SchemaBuildException {
        initializePrerequisites();

        populateEditContainmentNodeForAlbum();
        m_editNode.setChangeSource(EditChangeSource.system);

        m_mergeListCommand.addAddInfo(m_childListHelper, m_artistModelNode, new EditContext(m_editNode, null, null,
                null), m_albumNodelNode, null);
        when(m_childListHelper.isOrderUpdateNeededForChild(anyCollection(), any(ModelNode.class), anyInt())).thenReturn(true);

        WritableChangeTreeNode artistCTN = new ChangeTreeNodeImpl(m_schemaRegistry, null, LENNY_MNID, m_artistDSN, m_nodesOfType,
                m_attrIndex, m_changedNodeSPs, m_contextMap, m_nodesOfTypeWithinSchemaMount);
        m_mergeListCommand.appendChange(artistCTN, false);

        Map<ModelNodeId, ChangeTreeNode> artistTNChildren = artistCTN.getChildren();
        assertEquals(1, artistTNChildren.size());
        ChangeTreeNode albumTN = artistTNChildren.get(GREATEST_HITS_ID);
        assertNull(albumTN.getInsertOperation().getValue());
        assertEquals("first", albumTN.getInsertOperation().getName());
        assertEquals("album[/jukebox/library/artist[name='Lenny']/album[name='Greatest Hits']] -> modify\n" +
                " name -> none { previousVal = 'Greatest Hits', currentVal = 'Greatest Hits' }", albumTN.print().trim());
        assertFalse(albumTN.isImplied());
        assertEquals(EditConfigOperations.CREATE, albumTN.getEditOperation());
        assertEquals(EditChangeSource.system, albumTN.getChangeSource());
        assertFalse(artistCTN.getNodesOfType(ALBUM_SCHEMA_PATH).iterator().next().isMountPoint());
    }

    @Test
    public void testAppendChangesDoesNotAppendAnyChangesForNoChange() throws SchemaBuildException {
        initializePrerequisites();

        populateEditContainmentNodeForAlbum();

        when(m_childListHelper.isOrderUpdateNeededForChild(anyCollection(), any(ModelNode.class), anyInt())).thenReturn(false);
        m_mergeListCommand.addAddInfo(m_childListHelper, m_artistModelNode, new EditContext(m_editNode, null, null,
                null), m_albumNodelNode, null);

        WritableChangeTreeNode artistTN = new ChangeTreeNodeImpl(m_schemaRegistry, null, LENNY_MNID, m_artistDSN, m_nodesOfType,
                m_attrIndex, m_changedNodeSPs, m_contextMap, m_nodesOfTypeWithinSchemaMount);

        m_mergeListCommand.appendChange(artistTN, false);

        Map<ModelNodeId, ChangeTreeNode> artistTNChildren = artistTN.getChildren();
        assertEquals(0, artistTNChildren.size());
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
        when(m_schemaRegistry.getDataSchemaNode(new SchemaPathBuilder().withParent(listSchemaPath).appendQName(m_keyQname).build()))
                .thenReturn(m_keyLeafnode);
    }

    private void initializePrerequisites() throws SchemaBuildException {
        List<YangTextSchemaSource> yangs = TestUtil.getJukeBoxDeps();
        yangs.add(TestUtil.getByteSource(EXAMPLE_JUKEBOX_YANG));
        m_schemaRegistry = new SchemaRegistryImpl(yangs, Collections.emptySet(), Collections.emptyMap(), new NoLockService());

        m_artistModelNode = mock(HelperDrivenModelNode.class);
        m_albumNodelNode = mock(HelperDrivenModelNode.class);

        when(m_artistModelNode.getSchemaRegistry()).thenReturn(m_schemaRegistry);
        when(m_artistModelNode.getModelNodeId()).thenReturn(LENNY_MNID);

        m_artistDSN = m_schemaRegistry.getDataSchemaNode(ARTIST_SCHEMA_PATH);
        m_albumDSN = m_schemaRegistry.getDataSchemaNode(ALBUM_SCHEMA_PATH);

        when(m_childListHelper.getSchemaNode()).thenReturn((ListSchemaNode) m_albumDSN);

        m_nodesOfType = new HashMap<>();
        m_nodesOfTypeWithinSchemaMount = new HashMap<>();
        m_attrIndex = new HashMap<>();
        m_changedNodeSPs = new HashSet<>();
        m_contextMap = new HashMap<>();
    }

    private void populateEditContainmentNodeForAlbum() {
        m_editNode = new TestEditContainmentNode(ALBUM_QNAME, EditConfigOperations.CREATE, m_schemaRegistry);
        m_editNode.addMatchNode(NAME_QNAME, new GenericConfigAttribute(NAME, JB_NS, "Greatest Hits"));
        EditChangeNode genreCN = new EditChangeNode(GENRE_QNAME, new IdentityRefConfigAttribute(JB_NS, "jbox",
                DocumentUtils.createDocument().createElementNS(JB_NS, "blues")));
        EditChangeNode yearCN = new EditChangeNode(YEAR_QNAME, new GenericConfigAttribute(YEAR, JB_NS, "2000"));
        m_editNode.addChangeNode(genreCN);
        m_editNode.addChangeNode(yearCN);
        m_editNode.setInsertOperation(InsertOperation.FIRST_OP);
    }
}
