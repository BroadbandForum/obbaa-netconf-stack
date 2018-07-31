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

import java.util.List;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.AnvExtensions;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditChangeSource;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DSExpressionValidator;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EmptyTypeDefinition;

import org.broadband_forum.obbaa.netconf.api.messages.EditConfigOperations;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil;

public class AddDefaultDataInterceptor implements DefaultCapabilityCommandInterceptor {

    private final SchemaRegistry m_schemaRegistry;
    private final DSExpressionValidator m_ExpressionValidator;
    private final ModelNodeHelperRegistry m_modelNodeHelperRegistry;

    public AddDefaultDataInterceptor(ModelNodeHelperRegistry modelNodeHelperRegistry, SchemaRegistry schemaRegistry,
                                     DSExpressionValidator expValidator) {
        m_schemaRegistry = schemaRegistry;
        m_ExpressionValidator = expValidator;
        m_modelNodeHelperRegistry = modelNodeHelperRegistry;
    }

    public void init() {
        m_modelNodeHelperRegistry.setDefaultCapabilityCommandInterceptor(this);
    }

    public void destroy() {
        m_modelNodeHelperRegistry.resetDefaultCapabilityCommandInterceptor();
    }

    @Override
    public EditContainmentNode processMissingData(EditContainmentNode oldEditData, ModelNode childModelNode) {
        EditContainmentNode newData = new EditContainmentNode(oldEditData);
        DataSchemaNode dataSchemaNode = m_schemaRegistry.getDataSchemaNode(childModelNode.getModelNodeSchemaPath());
        List<QName> disabledNodes = newData.getDisabledDefaultCreationNodes();
        if (dataSchemaNode instanceof DataNodeContainer) {
            for (DataSchemaNode child : ((DataNodeContainer) dataSchemaNode).getChildNodes()) {
                if (!disabledNodes.contains(child.getQName())) {
                    addDefaultData(newData, child, childModelNode);
                }
            }
        }
        return newData;
    }

    private void addDefaultData(EditContainmentNode newData, DataSchemaNode child, ModelNode childModelNode) {
        // Handle leaves
        if (child instanceof LeafSchemaNode && newData.getChangeNode(child.getQName()) == null && child
                .isConfiguration()
                && !AnvExtensions.IGNORE_DEFAULT.isExtensionIn(child)) {
            LeafSchemaNode childLeaf = (LeafSchemaNode) child;
            String defaultVal = childLeaf.getDefault();
            if (defaultVal != null && !SchemaRegistryUtil.containsWhen(child)) {
                ConfigLeafAttribute configLeafAttribute = ConfigAttributeFactory.getConfigAttributeFromDefaultValue
                        (m_schemaRegistry, childLeaf);
                newData.addChangeNode(child.getQName(), configLeafAttribute, EditChangeSource.system);
            }
        } else if (child instanceof ContainerSchemaNode && child.isConfiguration()) { // Handle non-presence containers
            if (!((ContainerSchemaNode) child).isPresenceContainer() && (newData.getChildNode(child.getQName()) ==
                    null)) {
                boolean isValid = m_ExpressionValidator.validateWhenConditionOnModule(childModelNode, child);
                if (isValid) {
                    EditContainmentNode defaultNode = new EditContainmentNode(child.getQName(), EditConfigOperations
                            .CREATE);
                    defaultNode.setChangeSource(EditChangeSource.system);
                    newData.addChild(defaultNode);
                }
            }
        } else if (child instanceof ChoiceSchemaNode && child.isConfiguration()) {
            ChoiceSchemaNode choiceNode = (ChoiceSchemaNode) child;
            Set<ChoiceCaseNode> cases = choiceNode.getCases();
            boolean caseExists = false;
            for (ChoiceCaseNode caseNode : cases) {
                for (DataSchemaNode node : caseNode.getChildNodes()) {
                    if (newData.getChangeNode(node.getQName()) != null) {
                        caseExists = true;
                        break;
                    }
                    if (newData.getChildNode(node.getQName()) != null) {
                        caseExists = true;
                        break;
                    }
                }
                if (caseExists) {
                    populateChoiceCase(newData, caseNode, childModelNode, false);
                    break;
                }
            }
            if (!caseExists) {
                String defaultCaseName = choiceNode.getDefaultCase();
                if (defaultCaseName != null && !AnvExtensions.IGNORE_DEFAULT.isExtensionIn(child)) {
                    ChoiceCaseNode defaultCaseNode = choiceNode.getCaseNodeByName(defaultCaseName);
                    populateChoiceCase(newData, defaultCaseNode, childModelNode, true);
                }
            }
        }
    }

    public void populateChoiceCase(EditContainmentNode newData, ChoiceCaseNode caseNode, ModelNode childModelNode,
                                   boolean isDefaultCase) {
        for (DataSchemaNode childInside : caseNode.getChildNodes()) {
            DataSchemaNode childNode = m_schemaRegistry.getDataSchemaNode(childInside.getPath());
            if (childNode instanceof LeafSchemaNode && newData.getChangeNode(childNode.getQName()) == null &&
                    childNode.isConfiguration()) {
                TypeDefinition<?> type = ((LeafSchemaNode) childNode).getType();
                String defaultVal = ((LeafSchemaNode) childNode).getDefault();
                if (defaultVal != null) {
                    boolean isValid = m_ExpressionValidator.validateWhenConditionOnModule(childModelNode, childNode);
                    if (isValid) {
                        ConfigLeafAttribute configLeafAttribute = ConfigAttributeFactory
                                .getConfigAttributeFromDefaultValue(m_schemaRegistry, (LeafSchemaNode) childNode);
                        newData.addChangeNode(childNode.getQName(), configLeafAttribute, EditChangeSource.system);
                    }
                } else if (isDefaultCase && type instanceof EmptyTypeDefinition) {
                    // this is not explicitly mentioned in the RFC (an empty type has no default)
                    // but it makes sense that in a default case leafs with empty types are emitted
                    boolean isValid = m_ExpressionValidator.validateWhenConditionOnModule(childModelNode, childNode);
                    if (isValid) {
                        ConfigLeafAttribute configLeafAttribute = ConfigAttributeFactory
                                .getConfigAttributeFromDefaultValue(m_schemaRegistry, (LeafSchemaNode) childNode);
                        newData.addChangeNode(childNode.getQName(), configLeafAttribute, EditChangeSource.system);
                    }
                }
            } else if (childNode instanceof ContainerSchemaNode && childNode.isConfiguration()) { // Handle
                // non-presence containers
                if (!((ContainerSchemaNode) childNode).isPresenceContainer() && (newData.getChildNode(childNode
                        .getQName()) == null)) {
                    boolean isValid = m_ExpressionValidator.validateWhenConditionOnModule(childModelNode, childNode);
                    if (isValid) {
                        EditContainmentNode defaultNode = new EditContainmentNode(childNode.getQName(),
                                EditConfigOperations.CREATE);
                        defaultNode.setChangeSource(EditChangeSource.system);
                        newData.addChild(defaultNode);
                    }
                }
            } else if (childNode instanceof ChoiceSchemaNode && childNode.isConfiguration()) {
                addDefaultData(newData, childNode, childModelNode);
            } else if (childNode instanceof ListSchemaNode && childNode.isConfiguration()) {
                List<QName> keyDefinition = ((ListSchemaNode) childNode).getKeyDefinition();
                SchemaPath parentSchemaPath = childNode.getPath();
                int counterForDefaultValueAndIsValid = 0;
                for (QName key : keyDefinition) {
                    SchemaPath keySchemaPath = m_schemaRegistry.getDescendantSchemaPath(parentSchemaPath, key);
                    LeafSchemaNode keySchemaNode = (LeafSchemaNode) m_schemaRegistry.getDataSchemaNode(keySchemaPath);
                    if (keySchemaNode != null) {
                        String defaultVal = keySchemaNode.getDefault();
                        boolean isValid = m_ExpressionValidator.validateWhenConditionOnModule(childModelNode,
                                keySchemaNode);
                        if ((defaultVal != null) && isValid) {
                            counterForDefaultValueAndIsValid++;
                        }
                    }
                }
                if (keyDefinition.size() == counterForDefaultValueAndIsValid) {
                    getMatchNodesForList(keyDefinition, parentSchemaPath, newData, childNode);
                }
            }
        }
    }

    private void getMatchNodesForList(List<QName> keyDefinition, SchemaPath parentSchemaPath, EditContainmentNode
            newData, DataSchemaNode childNode) {
        EditContainmentNode defaultNode = new EditContainmentNode(childNode.getQName(), EditConfigOperations.CREATE);
        defaultNode.setChangeSource(EditChangeSource.system);
        for (QName key : keyDefinition) {
            SchemaPath keySchemaPath = m_schemaRegistry.getDescendantSchemaPath(parentSchemaPath, key);
            LeafSchemaNode keySchemaNode = (LeafSchemaNode) m_schemaRegistry.getDataSchemaNode(keySchemaPath);
            ConfigLeafAttribute configLeafAttribute = ConfigAttributeFactory.getConfigAttributeFromDefaultValue
                    (m_schemaRegistry, keySchemaNode);
            defaultNode.addMatchNode(key, configLeafAttribute);
        }
        newData.addChild(defaultNode);
    }

    public static String getFromEnvOrSysProperty(String name) {
        String value = System.getenv(name);
        if (value == null) {
            value = System.getProperty(name, null);
        }
        return value;
    }
}
