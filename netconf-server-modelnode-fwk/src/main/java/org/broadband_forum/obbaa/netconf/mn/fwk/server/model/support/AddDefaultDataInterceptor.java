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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.beanutils.DynaBean;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigOperations;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.AnvExtensions;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditChangeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditChangeSource;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditConfigException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.DSValidationContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.TimingLogger;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DSExpressionValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationUtil;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public class AddDefaultDataInterceptor implements DefaultCapabilityCommandInterceptor {

    private final SchemaRegistry m_schemaRegistry;
    private final DSExpressionValidator m_ExpressionValidator;
    private final ModelNodeHelperRegistry m_modelNodeHelperRegistry;
    private DSValidationContext m_context = null;

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
    public EditContainmentNode processMissingData(EditContainmentNode oldEditData, ModelNode childModelNode)
            throws EditConfigException {
        DSValidationContext validationContext = getValidationContext();
        boolean editTreeHasChanged = false;
        EditContainmentNode newData = new EditContainmentNode(oldEditData);
        DataSchemaNode dataSchemaNode = m_schemaRegistry.getDataSchemaNode(childModelNode.getModelNodeSchemaPath());
        List<QName> disabledNodes = newData.getDisabledDefaultCreationNodes();
        if (dataSchemaNode instanceof DataNodeContainer) {
            Collection<DataSchemaNode> childNodes = ((DataNodeContainer) dataSchemaNode).getChildNodes();
            if ((childNodes == null || childNodes.isEmpty()) && childModelNode.hasSchemaMount()) {
                try {
                    SchemaRegistry mountRegistry = childModelNode.getMountRegistry();
                    if ( mountRegistry.getParentRegistry() != null){
                        childNodes = mountRegistry.getRootDataSchemaNodes();
                    }
                } catch (GetException e) {
                    // EditConfig failed due to GetException, so cast into
                    // EditConfigException
                    throw new EditConfigException(e.getRpcError());
                }
            }
            for (DataSchemaNode child : childNodes) {
                if (!disabledNodes.contains(child.getQName())) {
                    try {
                        boolean hasChanged = addDefaultData(newData, child, childModelNode, getListKeyDefinitions(dataSchemaNode), validationContext);
                        if(!editTreeHasChanged && hasChanged){
                            editTreeHasChanged = true;
                        }
                    } catch (ValidationException e) {
                        // EditConfig failed due to ValidationException, so cast
                        // into EditConfigException
                        throw new EditConfigException(e.getRpcError());
                    }
                }
            }
        }
        newData.setHasChanged(editTreeHasChanged);
        return newData;
    }

    private boolean addDefaultData(EditContainmentNode newData, DataSchemaNode child, ModelNode childModelNode,
                                   List<QName> keyDefinitions, DSValidationContext validationContext) {
        boolean isValidMountNode = childModelNode.hasSchemaMount();
        boolean changed = false;
        // Handle leaves
        if (child instanceof LeafSchemaNode && newData.getChangeNode(child.getQName()) == null
                && child.isConfiguration() && !AnvExtensions.IGNORE_DEFAULT.isExtensionIn(child) && !keyDefinitions.contains(child.getQName())) {
            LeafSchemaNode childLeaf = (LeafSchemaNode) child;
            Optional<? extends Object> defaultVal = childLeaf.getType().getDefaultValue();
            if (defaultVal.isPresent() && !SchemaRegistryUtil.containsWhen(child)) {
                if (DataStoreValidationUtil.evaluateAugmentUsesWithWhen(child, childModelNode, m_ExpressionValidator, validationContext)) {
                    SchemaRegistry registry = isValidMountNode ? childModelNode.getMountRegistry() : m_schemaRegistry;
                    ConfigLeafAttribute configLeafAttribute = ConfigAttributeFactory
                            .getConfigAttributeFromDefaultValue(registry, childLeaf);
                    newData.addLeafChangeNode(child.getQName(), configLeafAttribute, EditChangeSource.system);
                    changed = true;
                }
            }
        } else if (child instanceof ContainerSchemaNode && child.isConfiguration()) { // Handle
                                                                                      // non-presence
                                                                                      // containers
            if (!((ContainerSchemaNode) child).isPresenceContainer()
                    && (newData.getChildNode(child.getQName()) == null)) {
                final boolean[] isValid = {false};
                TimingLogger.withExtraInfo("DEFAULT", childModelNode.getSchemaRegistry().getShortPath(child.getPath()), () -> {
                    isValid[0] = m_ExpressionValidator.validateWhenConditionOnModule(childModelNode, child, validationContext);
                });
                boolean hasMandatoryChildren = DataStoreValidationUtil.isMandatoryChildPresentWithoutDefaultsUnderContainer((ContainerSchemaNode) child);
                if (isValid[0] && !hasMandatoryChildren) {
                    EditContainmentNode defaultNode = new EditContainmentNode(child.getPath(),
                            EditConfigOperations.CREATE, childModelNode.getSchemaRegistry(), null);
                    defaultNode.setChangeSource(EditChangeSource.system);
                    newData.addChild(defaultNode);
                    changed = true;
                }
            }
        } else if (child instanceof ChoiceSchemaNode && child.isConfiguration()) {
            ChoiceSchemaNode choiceNode = (ChoiceSchemaNode) child;
            boolean isValid = false;
            try {
                isValid = m_ExpressionValidator.validateWhenConditionOnModule(childModelNode, choiceNode,
                        validationContext);
            } catch (Exception e) {
            }
            if (isValid) {
                Collection<CaseSchemaNode> cases = choiceNode.getCases().values();
                boolean caseExists = false;
                for (CaseSchemaNode caseNode : cases) {
                    caseExists = checkCaseExistsRecursively(newData, caseExists, caseNode);
                    if (caseExists) {
                        boolean hasChanged = populateChoiceCase(newData, caseNode, childModelNode, false);
                        if(!changed && hasChanged){
                            changed = true;
                        }
                        break;
                    }
                }
                if (!caseExists) {
                    Optional<CaseSchemaNode> defaultCase = choiceNode.getDefaultCase();
                    if (defaultCase.isPresent() && !AnvExtensions.IGNORE_DEFAULT.isExtensionIn(child)) {
                        boolean hasChanged = populateChoiceCase(newData, defaultCase.get(), childModelNode, false);
                        if(!changed && hasChanged){
                            changed = true;
                        }
                    }
                }
            }
        }
        return changed;
    }

    private boolean checkCaseExistsRecursively(EditContainmentNode newData, boolean caseExists,
            CaseSchemaNode caseNode) {
        for (DataSchemaNode node : caseNode.getChildNodes()) {
            if (!(node instanceof ChoiceSchemaNode)) {
                if (newData.getChangeNode(node.getQName()) != null) {
                    caseExists = true;
                    break;
                }
                if (newData.getChildNode(node.getQName()) != null) {
                    caseExists = true;
                    break;
                }
            } else {
                ChoiceSchemaNode choiceNode = (ChoiceSchemaNode) node;
                for (CaseSchemaNode choiceCaseNode : choiceNode.getCases().values()) {
                    if (checkCaseExistsRecursively(newData, caseExists, choiceCaseNode)) {
                        caseExists = true;
                        return caseExists;
                    }
                }
            }
        }
        return caseExists;
    }

    @Override
    public boolean populateChoiceCase(EditContainmentNode newData, CaseSchemaNode caseNode, ModelNode childModelNode, boolean checkExistenceBeforeCreate) {
        return populateChoiceCase(newData, caseNode, childModelNode, checkExistenceBeforeCreate, getValidationContext());
    }

    private boolean populateChoiceCase(EditContainmentNode newData, CaseSchemaNode caseNode, ModelNode childModelNode,
                                      boolean checkExistenceBeforeCreate, DSValidationContext validationContext) {
        boolean isValidMountNode = childModelNode.hasSchemaMount();
        boolean changed = false;
        SchemaRegistry registry = isValidMountNode ? childModelNode.getMountRegistry() : m_schemaRegistry;

        for (DataSchemaNode childInside : caseNode.getChildNodes()) {
            DataSchemaNode childNode = registry.getDataSchemaNode(childInside.getPath());
            if (childNode instanceof LeafSchemaNode && newData.getChangeNode(childNode.getQName()) == null
                    && childNode.isConfiguration()) {
                Optional<? extends Object> defaultVal = ((LeafSchemaNode) childNode).getType().getDefaultValue();
                if (defaultVal != null && defaultVal.isPresent()) {
                    boolean proceedToCreate = true;
                    if ( checkExistenceBeforeCreate && nodeAlreadyExists(childModelNode, childNode, null, newData)) {
                        proceedToCreate = false;
                    }
                    if (proceedToCreate) {
                        //skip default leaf creation if it has when constraint
                        if (!SchemaRegistryUtil.containsWhen(childNode) && proceedToCreate) {
                            ConfigLeafAttribute configLeafAttribute = ConfigAttributeFactory
                                    .getConfigAttributeFromDefaultValue(registry, (LeafSchemaNode) childNode);
                            newData.addLeafChangeNode(childNode.getQName(), configLeafAttribute, EditChangeSource.system);
                            changed = true;
                        }
                    }
                }
            } else if (childNode instanceof ContainerSchemaNode && childNode.isConfiguration()) { // Handle non-presence containers
                if (!((ContainerSchemaNode) childNode).isPresenceContainer()
                        && (newData.getChildNode(childNode.getQName()) == null)) {
                    boolean proceedToCreate = true;
                    if ( checkExistenceBeforeCreate && nodeAlreadyExists(childModelNode, childNode, null, newData)) {
                        proceedToCreate = false;
                    }
                    if (proceedToCreate) {
                        boolean isValid = m_ExpressionValidator.validateWhenConditionOnModule(childModelNode, childNode, validationContext);
                        boolean hasMandatoryChildren = DataStoreValidationUtil.isMandatoryChildPresentWithoutDefaultsUnderContainer((ContainerSchemaNode) childNode);
                        if (isValid && !hasMandatoryChildren) {
                            EditContainmentNode defaultNode = new EditContainmentNode(childNode.getPath(),
                                    EditConfigOperations.CREATE, m_schemaRegistry, null);
                            defaultNode.setChangeSource(EditChangeSource.system);
                            newData.addChild(defaultNode);
                            changed = true;
                        }
                    }
                }
            } else if (childNode instanceof ChoiceSchemaNode && childNode.isConfiguration()) {
                boolean hasChanged = addDefaultData(newData, childNode, childModelNode, Collections.emptyList(), validationContext);
                if(!changed && hasChanged){
                    changed = true;
                }
            } else if (childNode instanceof ListSchemaNode && childNode.isConfiguration()) {
                List<QName> keyDefinition = ((ListSchemaNode) childNode).getKeyDefinition();
                SchemaPath parentSchemaPath = childNode.getPath();
                int counterForDefaultValueAndIsValid = 0;
                Map<QName, ConfigLeafAttribute> keyDefs = new HashMap<>();
                for (QName key : keyDefinition) {
                    SchemaPath keySchemaPath = registry.getDescendantSchemaPath(parentSchemaPath, key);
                    LeafSchemaNode keySchemaNode = (LeafSchemaNode) registry.getDataSchemaNode(keySchemaPath);
                    if (keySchemaNode != null) {
                        Optional<? extends Object> defaultVal = keySchemaNode.getType().getDefaultValue();
                        boolean isValid = m_ExpressionValidator.validateWhenConditionOnModule(childModelNode,
                                keySchemaNode, validationContext);
                        if ((defaultVal.isPresent()) && isValid) {
                            ConfigLeafAttribute configLeafAttribute = ConfigAttributeFactory.getConfigAttributeFromDefaultValue(registry, keySchemaNode);
                            keyDefs.put(key, configLeafAttribute);
                            counterForDefaultValueAndIsValid++;
                        }
                    }
                }
                if (keyDefinition.size() == counterForDefaultValueAndIsValid) {
                    boolean proceedToCreate = true;
                    if ( checkExistenceBeforeCreate && nodeAlreadyExists(childModelNode, childNode, keyDefs, newData )) {
                        proceedToCreate = false;
                    }
                    if ( proceedToCreate) {
                        getMatchNodesForList(keyDefinition, parentSchemaPath, newData, childNode, registry);
                        changed = true;
                    }
                }
            }
        }
        return changed;
    }

    private boolean nodeAlreadyExists(ModelNode childModelNode, DataSchemaNode childNode, Map<QName, ConfigLeafAttribute> keyDefs, EditContainmentNode newData) {
        if ( childNode instanceof LeafSchemaNode) {
            List<EditChangeNode> changedNodeInCurrentRequest = newData.getChangeNodes(childNode.getQName());
            if (! changedNodeInCurrentRequest.isEmpty()) {
                return true;
            }
        } else {
            EditContainmentNode changedInCurrentRequest = newData.getChildNode(childNode.getQName());
            if ( changedInCurrentRequest != null) {
                return true;
            }
        }
        DynaBean bean = (DynaBean) childModelNode.getValue();
        QName childQName = childNode.getQName();
        String stepName = ModelNodeDynaBeanFactory.getDynaBeanAttributeName(childQName.getLocalName());
        if (DataStoreValidationUtil.isReadable(bean, stepName)) {
            ModelNodeDynaBeanContext dynaBeanContext = DataStoreValidationUtil.getModelNodeDynaBeanContext(childModelNode.getSchemaRegistry(), 
                    stepName, childQName.getModule().getNamespace().toString(), keyDefs);
            Object childValue = ModelNodeDynaBean.withContext(dynaBeanContext, () -> bean.get(stepName));
            if (childValue != null ) {
                return true;
            }
        }
        return false;
    }

    private void getMatchNodesForList(List<QName> keyDefinition, SchemaPath parentSchemaPath,
            EditContainmentNode newData, DataSchemaNode childNode, SchemaRegistry registry) {
        EditContainmentNode defaultNode = new EditContainmentNode(childNode.getPath(), EditConfigOperations.CREATE,
                registry, null);
        defaultNode.setChangeSource(EditChangeSource.system);
        for (QName key : keyDefinition) {
            SchemaPath keySchemaPath = registry.getDescendantSchemaPath(parentSchemaPath, key);
            LeafSchemaNode keySchemaNode = (LeafSchemaNode) registry.getDataSchemaNode(keySchemaPath);
            ConfigLeafAttribute configLeafAttribute = ConfigAttributeFactory
                    .getConfigAttributeFromDefaultValue(registry, keySchemaNode);
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

    private List<QName> getListKeyDefinitions(DataSchemaNode dataSchemaNode) {
        List<QName> keyDefinitions = new ArrayList<>();
        if(dataSchemaNode instanceof ListSchemaNode) {
            ListSchemaNode listSchemaNode = (ListSchemaNode) dataSchemaNode;
            keyDefinitions = listSchemaNode.getKeyDefinition();
        }
        return  keyDefinitions;
    }

    private static DSValidationContext getValidationContext(){
        DSValidationContext context = new DSValidationContext();
        context.setRootNodeAggregator(DataStoreValidationUtil.getRootModelNodeAggregatorInCache());
        return context;
    }
}
