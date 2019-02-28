package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.broadband_forum.obbaa.netconf.api.messages.EditConfigOperations;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.AnvExtensions;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditChangeSource;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditConfigException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DSExpressionValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationUtil;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EmptyTypeDefinition;

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
    public EditContainmentNode processMissingData(EditContainmentNode oldEditData, ModelNode childModelNode) throws EditConfigException {
        EditContainmentNode newData = new EditContainmentNode(oldEditData);
        DataSchemaNode dataSchemaNode = m_schemaRegistry.getDataSchemaNode(childModelNode.getModelNodeSchemaPath());
        List<QName> disabledNodes = newData.getDisabledDefaultCreationNodes();
        if (dataSchemaNode instanceof DataNodeContainer) {
            Collection<DataSchemaNode> childNodes = ((DataNodeContainer) dataSchemaNode).getChildNodes();
            if ((childNodes == null || childNodes.isEmpty()) && childModelNode.hasSchemaMount() && SchemaRegistryUtil.isMountPointEnabled()) {
                try {
                    childNodes = childModelNode.getMountRegistry().getRootDataSchemaNodes();
                } catch (GetException e) {
                    //EditConfig failed due to GetException, so cast into EditConfigException
                    throw new EditConfigException(e.getRpcError());
                }
            }
            for (DataSchemaNode child : childNodes) {
                if (!disabledNodes.contains(child.getQName())) {
                    addDefaultData(newData, child, childModelNode);
                }
            }
        }
        return newData;
    }

    private void addDefaultData(EditContainmentNode newData, DataSchemaNode child, ModelNode childModelNode) {
        boolean isValidMountNode = SchemaRegistryUtil.isMountPointEnabled() && childModelNode.hasSchemaMount();

        // Handle leaves
        if (child instanceof LeafSchemaNode && newData.getChangeNode(child.getQName()) == null && child.isConfiguration()
                && !AnvExtensions.IGNORE_DEFAULT.isExtensionIn(child)) {
            LeafSchemaNode childLeaf = (LeafSchemaNode) child;
            Optional<? extends Object> defaultVal = childLeaf.getType().getDefaultValue();
            if (defaultVal.isPresent() && !SchemaRegistryUtil.containsWhen(child) && checkAugment(child, childModelNode)) {
                SchemaRegistry registry = isValidMountNode ? childModelNode.getMountRegistry() : m_schemaRegistry;
                ConfigLeafAttribute configLeafAttribute = ConfigAttributeFactory.getConfigAttributeFromDefaultValue(registry, childLeaf);
                newData.addLeafChangeNode(child.getQName(), configLeafAttribute, EditChangeSource.system);
            }
        } else if (child instanceof ContainerSchemaNode && child.isConfiguration()) { // Handle non-presence containers
            if (!((ContainerSchemaNode) child).isPresenceContainer() && (newData.getChildNode(child.getQName()) == null)) {
                boolean isValid = m_ExpressionValidator.validateWhenConditionOnModule(childModelNode, child);
                if (isValid && !isMandatoryLeafPresentWithoutDefaults((ContainerSchemaNode) child)) {
                    EditContainmentNode defaultNode = new EditContainmentNode(child.getQName(), EditConfigOperations.CREATE);
                    defaultNode.setChangeSource(EditChangeSource.system);
                    newData.addChild(defaultNode);
                }
            }
        } else if (child instanceof ChoiceSchemaNode && child.isConfiguration()) {
            ChoiceSchemaNode choiceNode = (ChoiceSchemaNode) child;
            Collection<CaseSchemaNode> cases = choiceNode.getCases().values();
            boolean caseExists = false;
            for (CaseSchemaNode caseNode : cases) {
                caseExists = checkCaseExistsRecursively(newData, caseExists, caseNode);
                if (caseExists) {
                    populateChoiceCase(newData, caseNode, childModelNode, false);
                    break;
                }
            }
            if (!caseExists) {
                Optional<CaseSchemaNode> defaultCase = choiceNode.getDefaultCase();
                if (defaultCase.isPresent() && !AnvExtensions.IGNORE_DEFAULT.isExtensionIn(child)) {
                    populateChoiceCase(newData, defaultCase.get(), childModelNode, true);
                }
            }
        }
    }

    private boolean checkAugment(DataSchemaNode leafNode, ModelNode childModelNode) {
        if (leafNode.isAugmenting()) {
            DataSchemaNode parentNode = m_schemaRegistry.getDataSchemaNode(leafNode.getPath().getParent());
            AugmentationSchemaNode augSchema = DataStoreValidationUtil.getAugmentationSchema(parentNode, leafNode);
            RevisionAwareXPath xpath = augSchema == null ? null : augSchema.getWhenCondition().orElse(null);
            if (xpath != null) {
                return false;
            }
        }
        return true;
    }

    private boolean checkCaseExistsRecursively(EditContainmentNode newData, boolean caseExists, CaseSchemaNode caseNode) {
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

    public void populateChoiceCase(EditContainmentNode newData, CaseSchemaNode caseNode, ModelNode childModelNode, boolean isDefaultCase) {
        boolean isValidMountNode = SchemaRegistryUtil.isMountPointEnabled() && childModelNode.hasSchemaMount();
        SchemaRegistry registry = isValidMountNode ? childModelNode.getMountRegistry() : m_schemaRegistry;

        for (DataSchemaNode childInside : caseNode.getChildNodes()) {
            DataSchemaNode childNode = registry.getDataSchemaNode(childInside.getPath());
            if (childNode instanceof LeafSchemaNode && newData.getChangeNode(childNode.getQName()) == null && childNode.isConfiguration()) {
                TypeDefinition<?> type = ((LeafSchemaNode) childNode).getType();
                Optional<? extends Object> defaultVal = ((LeafSchemaNode) childNode).getType().getDefaultValue();
                if (defaultVal != null && defaultVal.isPresent()) {
                    boolean isValid = m_ExpressionValidator.validateWhenConditionOnModule(childModelNode, childNode);
                    if (isValid) {
                        ConfigLeafAttribute configLeafAttribute = ConfigAttributeFactory.getConfigAttributeFromDefaultValue(registry, (LeafSchemaNode) childNode);
                        newData.addLeafChangeNode(childNode.getQName(), configLeafAttribute, EditChangeSource.system);
                    }
                } else if (isDefaultCase && type instanceof EmptyTypeDefinition) {
                    // this is not explicitly mentioned in the RFC (an empty type has no default)
                    // but it makes sense that in a default case leafs with empty types are emitted
                    boolean isValid = m_ExpressionValidator.validateWhenConditionOnModule(childModelNode, childNode);
                    if (isValid) {
                        ConfigLeafAttribute configLeafAttribute = ConfigAttributeFactory.getConfigAttributeFromDefaultValue(registry, (LeafSchemaNode) childNode);
                        newData.addLeafChangeNode(childNode.getQName(), configLeafAttribute, EditChangeSource.system);
                    }
                }
            } else if (childNode instanceof ContainerSchemaNode && childNode.isConfiguration()) { // Handle non-presence containers
                if (!((ContainerSchemaNode) childNode).isPresenceContainer() && (newData.getChildNode(childNode.getQName()) == null)) {
                    boolean isValid = m_ExpressionValidator.validateWhenConditionOnModule(childModelNode, childNode);
                    if (isValid) {
                        EditContainmentNode defaultNode = new EditContainmentNode(childNode.getQName(), EditConfigOperations.CREATE);
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
                    SchemaPath keySchemaPath = registry.getDescendantSchemaPath(parentSchemaPath, key);
                    LeafSchemaNode keySchemaNode = (LeafSchemaNode) registry.getDataSchemaNode(keySchemaPath);
                    if (keySchemaNode != null) {
                        Optional<? extends Object> defaultVal = keySchemaNode.getType().getDefaultValue();
                        boolean isValid = m_ExpressionValidator.validateWhenConditionOnModule(childModelNode, keySchemaNode);
                        if ((defaultVal.isPresent()) && isValid) {
                            counterForDefaultValueAndIsValid++;
                        }
                    }
                }
                if (keyDefinition.size() == counterForDefaultValueAndIsValid) {
                    getMatchNodesForList(keyDefinition, parentSchemaPath, newData, childNode, registry);
                }
            }
        }
    }

    private void getMatchNodesForList(List<QName> keyDefinition, SchemaPath parentSchemaPath, EditContainmentNode newData, DataSchemaNode childNode, SchemaRegistry registry) {
        EditContainmentNode defaultNode = new EditContainmentNode(childNode.getQName(), EditConfigOperations.CREATE);
        defaultNode.setChangeSource(EditChangeSource.system);
        for (QName key : keyDefinition) {
            SchemaPath keySchemaPath = registry.getDescendantSchemaPath(parentSchemaPath, key);
            LeafSchemaNode keySchemaNode = (LeafSchemaNode) registry.getDataSchemaNode(keySchemaPath);
            ConfigLeafAttribute configLeafAttribute = ConfigAttributeFactory.getConfigAttributeFromDefaultValue(registry, keySchemaNode);
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

    private boolean isMandatoryLeafPresentWithoutDefaults(final ContainerSchemaNode node) {
        for (DataSchemaNode child : node.getChildNodes()) {
            if (child instanceof LeafSchemaNode) {
                LeafSchemaNode leafNode = (LeafSchemaNode) child;
                if (leafNode.isMandatory() && !leafNode.getType().getDefaultValue().isPresent()) {
                    return true;
                }
            } else if (child instanceof ChoiceSchemaNode) {
                if (((ChoiceSchemaNode) child).isMandatory()) {
                    return true;
                }
            }
        }
        return false;
    }
}
