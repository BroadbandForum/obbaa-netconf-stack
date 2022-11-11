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

import static org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder.fromString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.mn.fwk.AttributeIndex;
import org.broadband_forum.obbaa.netconf.mn.fwk.ChangeTreeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.ChangeTreeNodeImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.WritableChangeTreeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditChangeSource;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigAttributeHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.HelperDrivenModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

@RunWith(RequestScopeJunitRunner.class)
public class DeleteLeafCommandTest {

    private static final String NS = "unit:test:dact";
    private static final ModelNodeId FATHER_MNID = new ModelNodeId("/container=family/container=father", NS);
    private static final String EXAMPLE_YANG = "/deleteattributecommandtest/deleteattributecommandtest@2014-07-03.yang";

    private SchemaRegistry m_schemaRegistry;
    private DeleteLeafCommand m_deleteLeafCommand;
    private DataSchemaNode m_fatherNodeType;
    private HelperDrivenModelNode m_fatherNode;
    private SchemaPath m_dressSP;
    private SchemaPath m_shirtSizeSP;
    private SchemaPath m_gogglesSP;
    private SchemaPath m_shoesSP;
    private ModelNodeId m_dressId;
    private ModelNodeId m_shirtSizeId;
    private ModelNodeId m_gogglesId;
    private ModelNodeId m_shoesId;

    private Map<SchemaPath, Map<ModelNodeId, ChangeTreeNode>> m_nodesOfType;
    private Map<SchemaPath, Map<ModelNodeId, ChangeTreeNode>> m_nodesOfTypeWithinSchemaMount;
    private Map<AttributeIndex, Set<ChangeTreeNode>> m_attrIndex;
    private Set<SchemaPath> m_changedNodeSPs;
    private Map<String, Object> m_contextMap;

    @Mock
    private ConfigAttributeHelper m_configAttrHelper;
    @Mock
    private ConfigLeafAttribute m_dressAttr;
    @Mock
    private ConfigLeafAttribute m_shirtSizeAttr;
    @Mock
    private ConfigLeafAttribute m_gogglesAttr;
    @Mock
    private ConfigLeafAttribute m_shoesAttr;

    @Before
    public void setUp() throws SchemaBuildException {
        MockitoAnnotations.initMocks(this);
        m_schemaRegistry = new SchemaRegistryImpl(Arrays.asList(TestUtil.getByteSource(EXAMPLE_YANG)), Collections.emptySet(),
                Collections.emptyMap(), new NoLockService());
        m_deleteLeafCommand = new DeleteLeafCommand();
        m_fatherNode = mock(HelperDrivenModelNode.class);
        when(m_fatherNode.getSchemaRegistry()).thenReturn(m_schemaRegistry);
        when(m_fatherNode.getQName()).thenReturn(QName.create(NS, "2014-07-03", "father"));
        when(m_fatherNode.getModelNodeId()).thenReturn(FATHER_MNID);
        m_fatherNodeType = m_schemaRegistry.getDataSchemaNode(fromString("(" + NS + "?revision=2014-07-03)family, father"));
        when(m_fatherNode.getModelNodeSchemaPath()).thenReturn(m_fatherNodeType.getPath());

        m_dressSP = fromString("(" + NS + "?revision=2014-07-03)family, father, dress");
        m_dressId = new ModelNodeId("/container=family/container=father/container=dress", NS);
        when(m_fatherNode.getAttribute(m_dressSP.getLastComponent())).thenReturn(m_dressAttr);
        when(m_dressAttr.getStringValue()).thenReturn("dhothi");

        m_shirtSizeSP = fromString("(" + NS + "?revision=2014-07-03)family, father, shirt-size");
        m_shirtSizeId = new ModelNodeId("/container=family/container=father/container=shirt-size", NS);
        when(m_shirtSizeAttr.getStringValue()).thenReturn("38");
        when(m_fatherNode.getAttribute(m_shirtSizeSP.getLastComponent())).thenReturn(m_shirtSizeAttr);

        m_gogglesSP = fromString("(" + NS + "?revision=2014-07-03)family, father, goggles");
        m_gogglesId = new ModelNodeId("/container=family/container=father/container=goggles", NS);
        when(m_gogglesAttr.getStringValue()).thenReturn("fasttrack");
        when(m_fatherNode.getAttribute(m_gogglesSP.getLastComponent())).thenReturn(m_gogglesAttr);

        m_shoesSP = fromString("(" + NS + "?revision=2014-07-03)family, father, shoes");
        m_shoesId = new ModelNodeId("/container=family/container=father/container=shoes", NS);
        when(m_shoesAttr.getStringValue()).thenReturn("brown");
        when(m_fatherNode.getAttribute(m_shoesSP.getLastComponent())).thenReturn(m_shoesAttr);

        m_nodesOfType = new HashMap<>();
        m_attrIndex = new HashMap<>();
        m_changedNodeSPs = new HashSet<>();
        m_contextMap = new HashMap<>();
        m_nodesOfTypeWithinSchemaMount = new HashMap<>();
    }

    @Test
    public void testDefaultValueSetOnConstraint() throws Exception {
        SchemaPath fatherSp = fromString("(" + NS + "?revision=2014-07-03),family,father");
        HelperDrivenModelNode parentModelNode = mock(HelperDrivenModelNode.class);
        when(parentModelNode.getModelNodeSchemaPath()).thenReturn(fatherSp);
        when(parentModelNode.getSchemaRegistry()).thenReturn(m_schemaRegistry);
        QName changeNodeQName = m_schemaRegistry.lookupQName(NS, "dress");

        m_deleteLeafCommand.addDeleteInfo(m_schemaRegistry, mock(EditContext.class), mock(ConfigAttributeHelper.class), parentModelNode,
                changeNodeQName, true, EditChangeSource.system);
        m_deleteLeafCommand.execute();

        assertFalse(m_deleteLeafCommand.isSetToDefault());
    }

    @Test
    public void testAppendChangeAppendsDeleteInfo() {
        when(m_configAttrHelper.getLeafSchemaNode()).thenReturn((LeafSchemaNode) m_schemaRegistry.getDataSchemaNode(m_dressSP));
        WritableChangeTreeNode fatherCTN = getFatherChangeTreeNode();
        fatherCTN.setMountPoint(true);

        m_deleteLeafCommand.addDeleteInfo(m_schemaRegistry, mock(EditContext.class), m_configAttrHelper, m_fatherNode, m_dressSP.getLastComponent(),
                false, EditChangeSource.system);
        m_deleteLeafCommand.appendChange(fatherCTN, false);

        Map<ModelNodeId, ChangeTreeNode> fatherNodeChildren = fatherCTN.getChildren();
        assertEquals(1, fatherNodeChildren.size());
        assertEquals("dress -> delete { previousVal = 'dhothi', currentVal = 'null' }", fatherCTN.getNodesOfType(m_dressSP).iterator()
                .next().print().trim());
        assertEquals("father[/family/father] -> modify\n" +
                " dress -> delete { previousVal = 'dhothi', currentVal = 'null' }", fatherCTN.getNodesOfType(new AttributeIndex(m_dressSP, m_dressAttr))
                .iterator().next().print().trim());
        assertFalse(fatherNodeChildren.get(m_dressId).isImplied());
        assertTrue(fatherCTN.getNodesOfType(m_dressSP).iterator().next().isMountPoint());
    }

    @Test
    public void testAppendChangeAppendsDefaultForLeaf() {
        when(m_configAttrHelper.getLeafSchemaNode()).thenReturn((LeafSchemaNode) m_schemaRegistry.getDataSchemaNode(m_shoesSP));
        WritableChangeTreeNode fatherCTN = getFatherChangeTreeNode();
        fatherCTN.setMountPoint(false);

        m_deleteLeafCommand.addDeleteInfo(m_schemaRegistry, mock(EditContext.class), m_configAttrHelper, m_fatherNode, m_shoesSP.getLastComponent(),
                true, EditChangeSource.user);
        m_deleteLeafCommand.appendChange(fatherCTN, true);

        Map<ModelNodeId, ChangeTreeNode> fatherNodeChildren = fatherCTN.getChildren();
        assertEquals(1, fatherNodeChildren.size());
        assertEquals("shoes -> modify { previousVal = 'brown', currentVal = 'black' }", fatherCTN.getNodesOfType(m_shoesSP).iterator()
                .next().print().trim());
        assertEquals("father[/family/father] -> modify\n" +
                " shoes -> modify { previousVal = 'brown', currentVal = 'black' }", fatherCTN.getNodesOfType(new AttributeIndex(m_shoesSP, m_shoesAttr))
                .iterator().next().print().trim());
        assertTrue(fatherNodeChildren.get(m_shoesId).isImplied());
        assertEquals(EditChangeSource.user, fatherNodeChildren.get(m_shoesId).getChangeSource());
        assertFalse(fatherCTN.getNodesOfType(m_shoesSP).iterator().next().isMountPoint());
    }

    @Test
    public void testAppendChangeDoesNotAppendDefaultIfLeafIsUnderWhen() {
        when(m_configAttrHelper.getLeafSchemaNode()).thenReturn((LeafSchemaNode) m_schemaRegistry.getDataSchemaNode(m_dressSP));
        WritableChangeTreeNode fatherCTN = getFatherChangeTreeNode();
        fatherCTN.setMountPoint(false);

        m_deleteLeafCommand.addDeleteInfo(m_schemaRegistry, mock(EditContext.class), m_configAttrHelper, m_fatherNode, m_dressSP.getLastComponent(),
                true, EditChangeSource.user);
        m_deleteLeafCommand.appendChange(fatherCTN, true);

        Map<ModelNodeId, ChangeTreeNode> fatherNodeChildren = fatherCTN.getChildren();
        assertEquals(1, fatherNodeChildren.size());
        assertEquals("dress -> delete { previousVal = 'dhothi', currentVal = 'null' }", fatherCTN.getNodesOfType(m_dressSP).iterator()
                .next().print().trim());
        assertEquals("father[/family/father] -> modify\n" +
                " dress -> delete { previousVal = 'dhothi', currentVal = 'null' }", fatherCTN.getNodesOfType(new AttributeIndex(m_dressSP, m_dressAttr))
                .iterator().next().print().trim());
        assertTrue(fatherNodeChildren.get(m_dressId).isImplied());
        assertEquals(EditChangeSource.user, fatherNodeChildren.get(m_dressId).getChangeSource());
        assertFalse(fatherCTN.getNodesOfType(m_dressSP).iterator().next().isMountPoint());
    }

    @Test
    public void testAppendChangeDoesNotAppendDefaultIfLeafIsUnderAugmentWhen() {
        when(m_configAttrHelper.getLeafSchemaNode()).thenReturn((LeafSchemaNode) m_schemaRegistry.getDataSchemaNode(m_shirtSizeSP));
        WritableChangeTreeNode fatherCTN = getFatherChangeTreeNode();
        fatherCTN.setMountPoint(false);

        m_deleteLeafCommand.addDeleteInfo(m_schemaRegistry, mock(EditContext.class), m_configAttrHelper, m_fatherNode,
                m_shirtSizeSP.getLastComponent(), true, EditChangeSource.user);
        m_deleteLeafCommand.appendChange(fatherCTN, true);

        Map<ModelNodeId, ChangeTreeNode> fatherNodeChildren = fatherCTN.getChildren();
        assertEquals(1, fatherNodeChildren.size());
        assertEquals("shirt-size -> delete { previousVal = '38', currentVal = 'null' }", fatherCTN.getNodesOfType(m_shirtSizeSP)
                .iterator().next().print().trim());
        assertEquals("father[/family/father] -> modify\n" +
                " shirt-size -> delete { previousVal = '38', currentVal = 'null' }", fatherCTN.getNodesOfType(new AttributeIndex(m_shirtSizeSP,
                m_shirtSizeAttr)).iterator().next().print().trim());
        assertTrue(fatherNodeChildren.get(m_shirtSizeId).isImplied());
        assertEquals(EditChangeSource.user, fatherNodeChildren.get(m_shirtSizeId).getChangeSource());
        assertFalse(fatherCTN.getNodesOfType(m_shirtSizeSP).iterator().next().isMountPoint());
    }

    @Test
    public void testAppendChangeDoesNotAppendDefaultIfLeafIsUnderUsesWhen() {
        when(m_configAttrHelper.getLeafSchemaNode()).thenReturn((LeafSchemaNode) m_schemaRegistry.getDataSchemaNode(m_gogglesSP));
        WritableChangeTreeNode fatherCTN = getFatherChangeTreeNode();
        fatherCTN.setMountPoint(false);

        m_deleteLeafCommand.addDeleteInfo(m_schemaRegistry, mock(EditContext.class), m_configAttrHelper, m_fatherNode, m_gogglesSP.getLastComponent(),
                true, EditChangeSource.user);
        m_deleteLeafCommand.appendChange(fatherCTN, true);

        Map<ModelNodeId, ChangeTreeNode> fatherNodeChildren = fatherCTN.getChildren();
        assertEquals(1, fatherNodeChildren.size());
        assertEquals("goggles -> delete { previousVal = 'fasttrack', currentVal = 'null' }", fatherCTN.getNodesOfType(m_gogglesSP).iterator()
                .next().print().trim());
        assertEquals("father[/family/father] -> modify\n" +
                " goggles -> delete { previousVal = 'fasttrack', currentVal = 'null' }", fatherCTN.getNodesOfType(new AttributeIndex(m_gogglesSP,
                m_gogglesAttr)).iterator().next().print().trim());
        assertTrue(fatherNodeChildren.get(m_gogglesId).isImplied());
        assertEquals(EditChangeSource.user, fatherNodeChildren.get(m_gogglesId).getChangeSource());
        assertFalse(fatherCTN.getNodesOfType(m_gogglesSP).iterator().next().isMountPoint());
    }

    private WritableChangeTreeNode getFatherChangeTreeNode() {
        return new ChangeTreeNodeImpl(m_schemaRegistry, null, FATHER_MNID, m_fatherNodeType, m_nodesOfType, m_attrIndex, m_changedNodeSPs,
                m_contextMap, m_nodesOfTypeWithinSchemaMount);
    }
}