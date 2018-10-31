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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DSExpressionValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.AnvExtensions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.EmptyTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.type.BaseTypes;

import org.broadband_forum.obbaa.netconf.api.messages.EditConfigOperations;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;

@RunWith(MockitoJUnitRunner.class)
public class AddDefaultDataInterceptorTest {

    private static final String BBF_FAST_NAMESPACE = "urn:broadband-forum-org:yang:bbf-fast";
    private static final String DOWN_STREAM_PROFILE = "down-stream-profile";
    private static final String NAME = "name";
    private static final String DATA_RATE = "data-rate";
    private static final String CHOICE_NODE = "choice-node";
    private static final String CASE_NODE = "case-node";
    private static final String LEAF_WITH_EMPTY_TYPE = "leaf-with-empty-type";

    @Mock
    private SchemaRegistry m_schemaRegistry;
    @Mock
    private SchemaContext m_schemaContext;
    @Mock
    private ModelNodeHelperRegistry m_modelNodeHelperRegistry;
    @Mock
    private ModelNode m_modelNode;
    @Mock
    private LeafSchemaNode m_dataRateLeafNode;
    @Mock
    private LeafSchemaNode m_nameLeafNode;
    @Mock
    private ChoiceSchemaNode m_choiceSchemaNode;
    @Mock
    private ChoiceCaseNode m_choiceCaseNode;
    @Mock
    private LeafSchemaNode m_leafWithEmptyType;
    @Mock
    private ContainerSchemaNode m_dsProfileSchemaNode;
    @Mock
    private DSExpressionValidator m_expValidator;
    @Mock
    private UnknownSchemaNode m_unKnownSchemaNode;
    @Mock
    private ExtensionDefinition m_extensionDef;
    @InjectMocks
    private AddDefaultDataInterceptor m_addDefaultDataInterceptor;

    private static QName qname(String localName) {
        return QName.create(BBF_FAST_NAMESPACE, localName);
    }

    @Before
    public void setUp() throws Exception {
        m_addDefaultDataInterceptor = new AddDefaultDataInterceptor(m_modelNodeHelperRegistry, m_schemaRegistry,
                m_expValidator);
        m_addDefaultDataInterceptor.init();
        when(m_schemaRegistry.getSchemaContext()).thenReturn(m_schemaContext);

        initNode(m_dsProfileSchemaNode, null, DOWN_STREAM_PROFILE, null, true);
        initNode(m_nameLeafNode, m_dsProfileSchemaNode, NAME, null, false);
        initNode(m_dataRateLeafNode, m_dsProfileSchemaNode, DATA_RATE, null, true);

        initNode(m_choiceSchemaNode, m_dsProfileSchemaNode, CHOICE_NODE, null, true);
        when(m_choiceSchemaNode.getDefaultCase()).thenReturn(CASE_NODE);
        initNode(m_choiceCaseNode, m_choiceSchemaNode, CASE_NODE, null, true);
        initNode(m_leafWithEmptyType, m_choiceCaseNode, LEAF_WITH_EMPTY_TYPE, mock(EmptyTypeDefinition.class), true);

        SchemaPath path = m_dsProfileSchemaNode.getPath();
        when(m_modelNode.getModelNodeSchemaPath()).thenReturn(path);
        when(m_expValidator.validateWhenConditionOnModule(m_modelNode, m_leafWithEmptyType)).thenReturn(true);
        AnvExtensions ignoreDefaultExt = AnvExtensions.IGNORE_DEFAULT;
        QName ignoreDefault = QName.create(ignoreDefaultExt.getExtensionNamespace(), ignoreDefaultExt.getRevision(),
                ignoreDefaultExt.getName());
        when(m_extensionDef.getQName()).thenReturn(ignoreDefault);
        when(m_unKnownSchemaNode.getExtensionDefinition()).thenReturn(m_extensionDef);
    }

    private void initNode(DataSchemaNode node, DataSchemaNode parent, String localName, TypeDefinition type, boolean
            isConfiguration) {
        when(node.getQName()).thenReturn(qname(localName));
        when(node.isConfiguration()).thenReturn(isConfiguration);
        SchemaPath path = null;
        if (parent == null) {
            path = SchemaPath.create(true, qname(localName));
        } else {
            path = parent.getPath().createChild(qname(localName));
            if (parent instanceof DataNodeContainer) {
                Collection<DataSchemaNode> childNodes = ((DataNodeContainer) parent).getChildNodes();
                childNodes.add(node);
            } else if (parent instanceof ChoiceSchemaNode && node instanceof ChoiceCaseNode) {
                ChoiceSchemaNode choiceNode = (ChoiceSchemaNode) parent;
                Set<ChoiceCaseNode> cases = choiceNode.getCases();
                cases.add((ChoiceCaseNode) node);
                when(choiceNode.getCaseNodeByName(localName)).thenReturn((ChoiceCaseNode) node);
            }
        }
        when(node.getPath()).thenReturn(path);
        when(m_schemaRegistry.getDataSchemaNode(path)).thenReturn(node);

        if (node instanceof DataNodeContainer) {
            when(((DataNodeContainer) node).getChildNodes()).thenReturn(new ArrayList<DataSchemaNode>());
        }
        if (node instanceof ChoiceSchemaNode) {
            when(((ChoiceSchemaNode) node).getCases()).thenReturn(new HashSet<ChoiceCaseNode>());
        }
        if (node instanceof LeafSchemaNode) {
            when(((LeafSchemaNode) node).getType()).thenReturn(type);
        }
    }

    /**
     * Test that when creating down-stream-profile:
     * - default value for data-rate is added
     * - the empty node for leaf-with-empty-type is added
     */
    @Test
    public void testProcessMissingDataWhenCreate() {
        EditContainmentNode editContainmentNode = new EditContainmentNode(qname(DOWN_STREAM_PROFILE),
                EditConfigOperations.CREATE);
        editContainmentNode.addMatchNode(qname(NAME), new GenericConfigAttribute(NAME, BBF_FAST_NAMESPACE, "ds_profile1"));
        //set default value in schema
        when(m_dataRateLeafNode.getDefault()).thenReturn("0xFFFFFFFF");
        when(m_dataRateLeafNode.getType()).thenReturn((TypeDefinition) BaseTypes.int64Type());

        EditContainmentNode interceptedContainmentNode = m_addDefaultDataInterceptor.processMissingData
                (editContainmentNode, m_modelNode);

        assertEquals(2, interceptedContainmentNode.getChangeNodes().size());
        assertEquals("4294967295", interceptedContainmentNode.getChangeNode(qname(DATA_RATE)).getValue());
        assertEquals("", interceptedContainmentNode.getChangeNode(qname(LEAF_WITH_EMPTY_TYPE)).getValue());
    }

    @Test
    public void testProcessMissingData_DisabledNodes() {
        EditContainmentNode editContainmentNode = new EditContainmentNode(qname(DOWN_STREAM_PROFILE),
                EditConfigOperations.CREATE);
        editContainmentNode.addMatchNode(qname(NAME), new GenericConfigAttribute(NAME, BBF_FAST_NAMESPACE, "ds_profile1"));
        editContainmentNode.addDisabledDefaultCreationNode(qname(DATA_RATE));
        //set default value in schema
        when(m_dataRateLeafNode.getDefault()).thenReturn("0xFFFFFFFF");
        when(m_dataRateLeafNode.getType()).thenReturn((TypeDefinition) BaseTypes.int64Type());

        EditContainmentNode interceptedContainmentNode = m_addDefaultDataInterceptor.processMissingData
                (editContainmentNode, m_modelNode);

        assertEquals(1, interceptedContainmentNode.getChangeNodes().size());
        assertNull(interceptedContainmentNode.getChangeNode(qname(DATA_RATE)));
        assertEquals("", interceptedContainmentNode.getChangeNode(qname(LEAF_WITH_EMPTY_TYPE)).getValue());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testProcessMissingDataWhenTypeDefIsIdentityRef() {
        EditContainmentNode editContainmentNode = new EditContainmentNode(qname(DOWN_STREAM_PROFILE),
                EditConfigOperations.CREATE);
        editContainmentNode.addMatchNode(qname(NAME), new GenericConfigAttribute(NAME, BBF_FAST_NAMESPACE, "ds_profile1"));
        //set default value in schema
        IdentityrefTypeDefinition identityrefTypeDefinition1 = mock(IdentityrefTypeDefinition.class);
        IdentitySchemaNode identitySchemaNode = mock(IdentitySchemaNode.class);
        when(m_dataRateLeafNode.getType()).thenReturn((TypeDefinition) identityrefTypeDefinition1);
        Set<IdentitySchemaNode> identitySchemaNodes = new HashSet<>();
        identitySchemaNodes.add(identitySchemaNode);
        when(identityrefTypeDefinition1.getIdentities()).thenReturn(identitySchemaNodes);
        when(identitySchemaNode.getQName()).thenReturn(qname(DATA_RATE));

        //default value with prefix
        when(m_dataRateLeafNode.getDefault()).thenReturn("pf:dValue");
        EditContainmentNode interceptedContainmentNode = m_addDefaultDataInterceptor.processMissingData
                (editContainmentNode, m_modelNode);

        assertEquals(2, interceptedContainmentNode.getChangeNodes().size());
        assertEquals("pf:dValue", interceptedContainmentNode.getChangeNode(qname(DATA_RATE)).getValue());
        assertEquals(BBF_FAST_NAMESPACE, interceptedContainmentNode.getChangeNode(qname(DATA_RATE)).getNamespace());
        assertEquals("", interceptedContainmentNode.getChangeNode(qname(LEAF_WITH_EMPTY_TYPE)).getValue());

        //default value without prefix
        when(m_dataRateLeafNode.getDefault()).thenReturn("dValue");
        when(m_schemaRegistry.getPrefix(BBF_FAST_NAMESPACE)).thenReturn("pf");
        interceptedContainmentNode = m_addDefaultDataInterceptor.processMissingData(editContainmentNode, m_modelNode);

        assertEquals(2, interceptedContainmentNode.getChangeNodes().size());
        assertEquals("pf:dValue", interceptedContainmentNode.getChangeNode(qname(DATA_RATE)).getValue());
        ConfigLeafAttribute configLeafAttribute = interceptedContainmentNode.getChangeNode(qname(DATA_RATE))
                .getConfigLeafAttribute();
        assertTrue(configLeafAttribute instanceof IdentityRefConfigAttribute);
        assertEquals(BBF_FAST_NAMESPACE, configLeafAttribute.getDOMValue().lookupNamespaceURI("pf"));
        assertEquals(BBF_FAST_NAMESPACE, interceptedContainmentNode.getChangeNode(qname(DATA_RATE)).getNamespace());
        assertEquals("", interceptedContainmentNode.getChangeNode(qname(LEAF_WITH_EMPTY_TYPE)).getValue());

        //Default ignored
        when(m_dataRateLeafNode.getUnknownSchemaNodes()).thenReturn(Arrays.asList(m_unKnownSchemaNode));
        when(m_choiceSchemaNode.getUnknownSchemaNodes()).thenReturn(Arrays.asList(m_unKnownSchemaNode));
        interceptedContainmentNode = m_addDefaultDataInterceptor.processMissingData(editContainmentNode, m_modelNode);
        assertNull(interceptedContainmentNode.getChangeNode(qname(DATA_RATE)));
        assertNull(interceptedContainmentNode.getChangeNode(qname(LEAF_WITH_EMPTY_TYPE)));
    }

    @After
    public void tearDown() {
        m_addDefaultDataInterceptor.destroy();
    }
}
