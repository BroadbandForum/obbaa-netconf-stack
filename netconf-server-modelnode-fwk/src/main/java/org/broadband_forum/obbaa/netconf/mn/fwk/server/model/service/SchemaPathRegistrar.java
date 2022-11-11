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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.service;

import static org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil.getSubtreeNodeWithDefaults;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationUtil.getSimplePath;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.jxpath.ri.compiler.Expression;
import org.apache.commons.lang3.StringUtils;
import org.broadband_forum.obbaa.netconf.api.logger.NetconfExtensions;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.ReferringNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryVisitor;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.ValidationHint;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.HintDetails;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.AnvExtensions;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.AccessSchemaPathUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationPathBuilder;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.jxpath.JXPathUtils;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.opendaylight.yangtools.yang.model.api.AnyDataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.MustConstraintAware;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.TypedDataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.UsesNode;
import org.opendaylight.yangtools.yang.model.api.WhenConditionAware;
import org.opendaylight.yangtools.yang.model.api.meta.ModelStatement;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
public class SchemaPathRegistrar implements SchemaRegistryVisitor {

    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(SchemaPathRegistrar.class, LogAppNames.NETCONF_STACK);;
    private final SchemaRegistry m_schemaRegistry;
    private Map<SchemaPath, ValidationHint> m_deviceValidationHints;
    private final DataStoreValidationPathBuilder m_pathBuilder;
    private List<String> m_errors = new ArrayList<>();
    private AugmentWhenConditionAnalyser m_augmentWhenAnalyser = new AugmentWhenConditionAnalyser();
    private Map<SchemaPath, HintDetails> m_globalValidationHints;
    private final ImpactHintsAnalyser m_impactHintsAnalyser;

    public SchemaPathRegistrar(SchemaRegistry schemaRegistry, ModelNodeHelperRegistry modelNodeHelperRegistry) {
        this(schemaRegistry, modelNodeHelperRegistry, Collections.emptyMap(), Collections.emptyMap());
    }

    public SchemaPathRegistrar(SchemaRegistry schemaRegistry, ModelNodeHelperRegistry modelNodeHelperRegistry, Map<SchemaPath, ValidationHint> validationHints) {
        this(schemaRegistry, modelNodeHelperRegistry, validationHints, Collections.emptyMap());
    }

    public SchemaPathRegistrar(SchemaRegistry schemaRegistry, ModelNodeHelperRegistry modelNodeHelperRegistry, Map<SchemaPath, ValidationHint> deviceValidationHints, Map<SchemaPath, HintDetails> globalValidationHints) {
        m_schemaRegistry = schemaRegistry;
        m_deviceValidationHints = deviceValidationHints;
        m_globalValidationHints = globalValidationHints;
        m_pathBuilder = new DataStoreValidationPathBuilder(schemaRegistry, modelNodeHelperRegistry);
        m_impactHintsAnalyser = new ImpactHintsAnalyser();
    }

    private boolean isInDataTree(SchemaPath targetPath) {
        SchemaPath parentPath = targetPath.getParent();
        while (parentPath != null && !parentPath.equals(SchemaPath.ROOT)) {
            DataSchemaNode parentNode = m_schemaRegistry.getDataSchemaNode(parentPath);
            if (parentNode == null) {
                // parent is not in data tree, is e.g. a notification
                return false;
            }
            parentPath = parentPath.getParent();
        }
        return true;
    }

    private void registerConstraintSchemaPaths(String componentId, DataSchemaNode dataSchemaNode) {
        Module module = m_schemaRegistry.getModuleByNamespace(dataSchemaNode.getQName().getNamespace().toString());
        if (!isInDataTree(dataSchemaNode.getPath())) {
            return;
        }
        if(m_schemaRegistry.getDataSchemaNode(dataSchemaNode.getPath()) != null) {
            if (dataSchemaNode instanceof WhenConditionAware) {
                Optional<RevisionAwareXPath> optWhenCondition = dataSchemaNode.getWhenCondition();
                if (optWhenCondition != null && optWhenCondition.isPresent()) {
                    String constraint = optWhenCondition.get().getOriginalString();
                    m_schemaRegistry.registerNodeConstraintDefinedModule(dataSchemaNode, constraint, module);

                    try {
                        setNodeConstraintModule(dataSchemaNode, module, constraint);
                        registerSchemaPaths(componentId, dataSchemaNode, constraint, ReferringNode.ReferenceType.WHEN, null);
                    } finally {
                        AccessSchemaPathUtil.clearNodeConstraintAssociatedModuleInCache();
                    }
                }
            }
            if (dataSchemaNode instanceof MustConstraintAware) {
                Collection<MustDefinition> mustConstraints = ((MustConstraintAware) dataSchemaNode).getMustConstraints();
                if (mustConstraints != null) {
                    for (MustDefinition mustConstraint : mustConstraints) {
                        String constraint = mustConstraint.getXpath().getOriginalString();
                        m_schemaRegistry.registerNodeConstraintDefinedModule(dataSchemaNode, constraint, module);
                        try {
                            setNodeConstraintModule(dataSchemaNode, module, constraint);
                            registerSchemaPaths(componentId, dataSchemaNode, mustConstraint.getXpath().getOriginalString(), ReferringNode.ReferenceType.MUST, null);
                        } finally {
                            AccessSchemaPathUtil.clearNodeConstraintAssociatedModuleInCache();
                        }
                    }
                }
            } 
            if(dataSchemaNode.isAugmenting() && dataSchemaNode.isConfiguration()){
                DataSchemaNode parentNode = m_schemaRegistry.getDataSchemaNode(dataSchemaNode.getPath().getParent());
                Pair<AugmentationSchemaNode,SchemaPath> augmentNodeAndItsResidingNode = DataStoreValidationUtil.getAugmentationSchema(m_schemaRegistry, parentNode, dataSchemaNode);
                AugmentationSchemaNode augSchema = augmentNodeAndItsResidingNode == null? null : augmentNodeAndItsResidingNode.getFirst();
                RevisionAwareXPath xpath = augSchema == null ? null : augSchema.getWhenCondition().orElse(null);

                if(xpath != null){
                    boolean isDirectChildOfAugmentStmt = isDirectChildOfAugmentStmt(dataSchemaNode, augSchema);
                    boolean isDefaultValueApplicable = false;
                    isDefaultValueApplicable = checkIfDefaultValueApplicable(dataSchemaNode, augSchema );
                    boolean nonDirectPresenceContainer = false;
                    if(!isDirectChildOfAugmentStmt) {
                        if (dataSchemaNode instanceof ContainerSchemaNode) {
                            nonDirectPresenceContainer = ((ContainerSchemaNode) dataSchemaNode).isPresenceContainer();
                        }
                    }
                    if(augSchema != null) {
                        DataSchemaNode augmentedNode =  m_schemaRegistry.getDataSchemaNode(augSchema.getTargetPath());
                        if(augmentedNode != null) {
                            Module augmentedModule = m_schemaRegistry.getModuleByNamespace(augmentedNode.getQName().getNamespace().toString());
                            m_schemaRegistry.registerNodeConstraintDefinedModule(augmentedNode, xpath.getOriginalString(), augmentedModule);
                        }
                    }
                    if (!nonDirectPresenceContainer && (isDirectChildOfAugmentStmt || (isDefaultValueApplicable))) {
                        registerSchemaPaths(componentId, dataSchemaNode, xpath.getOriginalString(), ReferringNode.ReferenceType.WHEN_ON_AUGMENT, augmentNodeAndItsResidingNode.getSecond());
                    }
                }
            }

            if(dataSchemaNode.isAddedByUses() && dataSchemaNode.isConfiguration() ){
                DataSchemaNode parentNode = m_schemaRegistry.getDataSchemaNode(dataSchemaNode.getPath().getParent());
                Pair<UsesNode,SchemaPath> usesNodeAndItsResidingNode = SchemaContextUtil.getUsesSchema(m_schemaRegistry, parentNode, dataSchemaNode);
                UsesNode usesNode = usesNodeAndItsResidingNode == null? null : usesNodeAndItsResidingNode.getFirst();
                RevisionAwareXPath xpath = usesNode == null ? null : usesNode.getWhenCondition().orElse(null);

                if (xpath != null) {
                    if(usesNode != null) {
                        DataSchemaNode usesresideNode =  m_schemaRegistry.getDataSchemaNode(usesNodeAndItsResidingNode.getSecond());
                        Module usesModule = m_schemaRegistry.getModuleByNamespace(usesresideNode.getQName().getNamespace().toString());
                        m_schemaRegistry.registerNodeConstraintDefinedModule(usesresideNode, xpath.getOriginalString(), usesModule);
                    }
                    registerSchemaPaths(componentId, dataSchemaNode, xpath.getOriginalString(), ReferringNode.ReferenceType.WHEN, usesNodeAndItsResidingNode.getSecond());
                }
            }
        }
    }

    private void setNodeConstraintModule(DataSchemaNode dataSchemaNode, Module module, String constraint) {
        Map<Expression, Module> nodeConstraintModules = m_schemaRegistry.getNodeConstraintDefinedModule(dataSchemaNode);
        Module constraintAssociatedModule = nodeConstraintModules.get(JXPathUtils.getExpression(constraint));
        if(constraintAssociatedModule != null) {
            AccessSchemaPathUtil.setNodeConstraintAssociatedModuleInCache(constraintAssociatedModule);
        } else {
            AccessSchemaPathUtil.setNodeConstraintAssociatedModuleInCache(module);
        }
    }

    private boolean hasDefaultValue(DataSchemaNode dataSchemaNode) {
        if(dataSchemaNode instanceof TypedDataSchemaNode){
            return ((TypedDataSchemaNode)dataSchemaNode).getType().getDefaultValue().isPresent();
        }
        return false;
    }

    /** If dataSchemaNode with default value is under list, it means that we won't consider it while registering schemapath. This function will 
     *  return true if the dataSchemaNode with default value has a parent(or an ancestor until the augment) that is not an instance of ListSchemaNode.
     */
    private boolean checkIfDefaultValueApplicable(DataSchemaNode dataSchemaNode, AugmentationSchemaNode augSchema) {
        DataSchemaNode parentSchemaNode;
        if(isDirectChildOfAugmentStmt(dataSchemaNode, augSchema) && hasDefaultValue(dataSchemaNode)) {
            parentSchemaNode = m_schemaRegistry.getDataSchemaNode(dataSchemaNode.getPath().getParent());
            if (parentSchemaNode != null && parentSchemaNode instanceof ListSchemaNode) {
                return false;
            }
        } else {
            if (hasDefaultValue(dataSchemaNode)) {
                do {
                    parentSchemaNode = m_schemaRegistry.getDataSchemaNode(dataSchemaNode.getPath().getParent());
                    if (parentSchemaNode != null && parentSchemaNode instanceof ListSchemaNode) {
                        return false;
                    }
                    dataSchemaNode = parentSchemaNode;
                } while (!(isDirectChildOfAugmentStmt(dataSchemaNode, augSchema)));
                return true;
            }
        }
        return false;
    }

    private boolean isDirectChildOfAugmentStmt(DataSchemaNode dataSchemaNode, AugmentationSchemaNode augSchema) {
        for (DataSchemaNode augmentDirectChild : augSchema.getChildNodes()) {
            if(dataSchemaNode.getPath().equals(augmentDirectChild.getPath())){
                return true;
            }
        }
        return false;
    }

    private void registerLeafRefSchemaPaths(String componentId, DataSchemaNode dataSchemaNode) {
        if (!isInDataTree(dataSchemaNode.getPath())) {
            return;
        }
        if(m_schemaRegistry.getDataSchemaNode(dataSchemaNode.getPath()) != null) {
            if (dataSchemaNode instanceof LeafSchemaNode) {
                LeafSchemaNode leafSchemaNode = (LeafSchemaNode) dataSchemaNode;
                if (leafSchemaNode.getType() instanceof LeafrefTypeDefinition) {
                    LeafrefTypeDefinition type = (LeafrefTypeDefinition) leafSchemaNode.getType();
                    if (type.requireInstance()) {
                        Map<SchemaPath, ArrayList<String>> targetPath = registerSchemaPaths(componentId, dataSchemaNode, type.getPathStatement().getOriginalString(), ReferringNode.ReferenceType.LEAFREF, null);
                        if ( targetPath != null){
                            for ( SchemaPath key : targetPath.keySet()){
                                DataSchemaNode schemaNode = m_schemaRegistry.getDataSchemaNode(key);
                                if (! schemaNode.isConfiguration()){
                                    m_errors.add("Configuration leafref node : " + dataSchemaNode.getPath() + " path " + type.getPathStatement().getOriginalString() + " is pointing to state data node " + schemaNode.getPath());
                                } else if ( ! (schemaNode instanceof LeafSchemaNode || schemaNode instanceof LeafListSchemaNode)){
                                    m_errors.add("Leafref "+ dataSchemaNode.getPath() + " target path is not referring to leaf/leaflist node. Target path - " +schemaNode.getPath());
                                }
                            }
                        }
                        registerRelativePath(dataSchemaNode, type.getPathStatement().getOriginalString(), type);
                    }
                } else if (leafSchemaNode.getType() instanceof UnionTypeDefinition) {
                    UnionTypeDefinition unionType = (UnionTypeDefinition) leafSchemaNode.getType();
                    for (TypeDefinition<?> typeDefinition : unionType.getTypes()) {
                        if (typeDefinition instanceof LeafrefTypeDefinition) {
                            registerLeafRefSchemaPath(componentId, dataSchemaNode, (LeafrefTypeDefinition) typeDefinition);
                        }
                    }
                }
            } else if (dataSchemaNode instanceof LeafListSchemaNode) {
                LeafListSchemaNode leafListSchemaNode = (LeafListSchemaNode) dataSchemaNode;
                if (leafListSchemaNode.getType() instanceof LeafrefTypeDefinition) {
                    registerLeafRefSchemaPath(componentId, dataSchemaNode, (LeafrefTypeDefinition) leafListSchemaNode.getType());
                }else if (leafListSchemaNode.getType() instanceof UnionTypeDefinition) {
                    UnionTypeDefinition unionType = (UnionTypeDefinition) leafListSchemaNode.getType();
                    for (TypeDefinition<?> typeDefinition : unionType.getTypes()) {
                        if (typeDefinition instanceof LeafrefTypeDefinition) {
                            registerLeafRefSchemaPath(componentId, dataSchemaNode, (LeafrefTypeDefinition) typeDefinition);
                        }
                    }
                }
            }
        }
    }

    // Register leaf-ref schemapaths
    private void registerLeafRefSchemaPath(String componentId, DataSchemaNode dataSchemaNode, LeafrefTypeDefinition type) {
        if (type.requireInstance()) {
            registerSchemaPaths(componentId, dataSchemaNode, type.getPathStatement().getOriginalString(), ReferringNode.ReferenceType.LEAFREF, null);
            registerRelativePath(dataSchemaNode, type.getPathStatement().getOriginalString(), type);
        }
    }


    private Map<SchemaPath, ArrayList<String>> registerSchemaPaths(String componentId, DataSchemaNode dataSchemaNode, String xPathString, 
            ReferringNode.ReferenceType referenceType, SchemaPath augmentedSP) {
        SchemaPath referringSP = dataSchemaNode.getPath();
        Map<SchemaPath,ArrayList<String>> constraints = null;
        registerRelativePath(dataSchemaNode, xPathString, null);
        boolean toBeSkipped = false;
        if(dataSchemaNode.isConfiguration()){
            AccessSchemaPathUtil.c_xpathExpr.set(new AccessSchemaPathUtil.XPathInfo(dataSchemaNode, xPathString));
            try {

                constraints = m_pathBuilder.getSchemaPathsFromXPath(dataSchemaNode, xPathString, augmentedSP);
                toBeSkipped = AccessSchemaPathUtil.c_xpathExpr.get().isToBeSkipped();
            } finally {
                AccessSchemaPathUtil.c_xpathExpr.remove();
                AccessSchemaPathUtil.c_withinPredicate.remove();
            }
            Pair<Map<SchemaPath,ValidationHint>, Boolean> parsedHints = getValidationHintDetails(referenceType, xPathString, dataSchemaNode,
                    constraints);
            for(Map.Entry<SchemaPath, ArrayList<String>> constraint:constraints.entrySet()) {
                SchemaPath referredSP = constraint.getKey();
                boolean keyLeaf = false;
                DataSchemaNode referredSN = m_schemaRegistry.getDataSchemaNode(referredSP);
                if(referredSN instanceof LeafSchemaNode) {
                    DataSchemaNode parentSN = m_schemaRegistry.getNonChoiceParent(referredSP);
                    if(parentSN instanceof ListSchemaNode){
                        keyLeaf = ((ListSchemaNode) parentSN).getKeyDefinition().contains(referredSN.getQName());
                    }
                }
                Map<SchemaPath,ValidationHint> hintDetails = parsedHints.getFirst();
                boolean isSkipValidation = parsedHints.getSecond() || toBeSkipped;
                if(isSkipValidation){
                    m_schemaRegistry.addToSkipValidationPaths(referringSP, xPathString);
                }
                ValidationHint validationHint = hintDetails.get(referredSP);
                validationHint = validationHint == null? null: validationHint;
                if(constraint.getValue() != null){
                    for(String accessPath : constraint.getValue()){
                        ReferringNode referringNode = registerNodesReferredInConstraints(componentId, dataSchemaNode, referringSP, referredSP, referenceType, keyLeaf, validationHint, accessPath, xPathString);
                        updateSubtreeHint(augmentedSP, referringNode);
                    }
                } else {
                    ReferringNode referringNode = registerNodesReferredInConstraints(componentId, dataSchemaNode, referringSP, referredSP, referenceType, keyLeaf, validationHint, null, xPathString);
                    updateSubtreeHint(augmentedSP, referringNode);
                }
            }
        }
        return constraints;
    }

    private Pair<Map<SchemaPath,ValidationHint>, Boolean> getValidationHintDetails(ReferringNode.ReferenceType referenceType, String xPath,
            DataSchemaNode dataSchemaNode, Map<SchemaPath, ArrayList<String>> constraints){
        Map<SchemaPath, ValidationHint> parsedHints = new HashMap<>();
        UnknownSchemaNode hintsNode = AnvExtensions.VALIDATION_HINTS_ON_NODE.getExtensionDefinition(dataSchemaNode);
        SchemaPath referringSP = dataSchemaNode.getPath();
        boolean isSkipValidation = false;
        if(hintsNode != null && hintsNode instanceof ModelStatement) {
            Module module = m_schemaRegistry.getModuleByNamespace(dataSchemaNode.getQName().getNamespace().toString());
            String hints = ((ModelStatement)hintsNode).argument().toString();
            Pair<Map<SchemaPath, ValidationHint>, Boolean> hintsPair = SchemaRegistryUtil.parseHints(constraints.keySet(), hints, m_schemaRegistry, module);
            parsedHints = hintsPair.getFirst();
            isSkipValidation = hintsPair.getSecond();
        } else {
            // No hints specified for this node at node level, so check for global level hints and register with that info if applicable
            HintDetails hintDetails = m_globalValidationHints.get(referringSP);
            if(hintDetails != null){
                parsedHints = hintDetails.getReferredSPToHints();
                isSkipValidation = hintDetails.isSkipValidation();
            } else if(!constraints.isEmpty()){
                boolean isWhenReference =
                        referenceType.equals(ReferringNode.ReferenceType.WHEN) || referenceType.equals(ReferringNode.ReferenceType.WHEN_ON_AUGMENT);
                // if its a when reference and there is a default leaf in the subtree, then it needs to be evaluated for instantiating them
                DataSchemaNode defaultNode = getSubtreeNodeWithDefaults(m_schemaRegistry, dataSchemaNode);
                StringBuilder logBuilder = new StringBuilder();
                if(!isWhenReference || defaultNode == null) {
                    Set<DataSchemaNode> snsWithHint = new HashSet<>();
                    for (SchemaPath referredSP : constraints.keySet()) {
                        if (!m_deviceValidationHints.containsKey(referredSP) && !m_globalValidationHints.containsKey(referredSP)) {
                            DataSchemaNode referredSN = m_schemaRegistry.getDataSchemaNode(referredSP);
                            Optional<ValidationHint> analysedHint =
                                    m_impactHintsAnalyser.getValidationHint(JXPathUtils.getExpression(xPath));
                            if (analysedHint.isPresent()) {
                                snsWithHint.add(referredSN);
                                parsedHints.put(referredSP, analysedHint.get());
                            }
                        }
                    }
                    if (LOGGER.isDebugEnabled() && !snsWithHint.isEmpty()) {
                        logBuilder.append("Added the hint SKIP_IMPACT_ON_CREATE automatically for the xpath ").append(JXPathUtils.getExpression(xPath));
                        logBuilder.append("\non referring Node ").append(getSimplePath(m_schemaRegistry.getDataSchemaNode(referringSP))).append(" on referred Nodes {");
                        for (DataSchemaNode snWithHint : snsWithHint) {
                            logBuilder.append("\n  ").append(getSimplePath(snWithHint));
                        }
                        logBuilder.append("\n}");
                        LOGGER.debug(logBuilder.toString());
                    }
                }else {
                    if(LOGGER.isDebugEnabled()){
                        logBuilder.append("Skipped analysing hints for the xpath ").append(xPath).append(" since the node ")
                        .append(getSimplePath(defaultNode)).append(" has defaults");
                        LOGGER.debug(logBuilder.toString());
                    }
                }
            }
        }
        return new Pair<>(parsedHints, isSkipValidation);
    }

    private void updateSubtreeHint(SchemaPath augmentedSP, ReferringNode referringNode) {
        if(ReferringNode.ReferenceType.WHEN_ON_AUGMENT.equals(referringNode.getReferenceType()) && !referringNode.isReferredNodeIsUnderChangedNode()
                && m_augmentWhenAnalyser.areAugmentConditionsUnderAugmentingSubtree(augmentedSP, referringNode)){
            referringNode.setReferredNodeIsUnderChangedNode(true);
        }
    }

    private ReferringNode registerNodesReferredInConstraints(String componentId, DataSchemaNode dataSchemaNode, SchemaPath referringSP, SchemaPath referredSP, 
            ReferringNode.ReferenceType referenceType, boolean keyLeaf, ValidationHint validationHint, String accessPath,
            String xPathString) {
        if (hasWhenCondition(dataSchemaNode)) {
            m_schemaRegistry.registerWhenReferringNodes(componentId, referringSP, referredSP, accessPath);
        }
        ReferringNode referringNode = new ReferringNode(keyLeaf, referredSP, referringSP, accessPath);
        referringNode.setReferenceType(referenceType);
        referringNode.setValidationHint(validationHint);
        referringNode.setConstraintXPath(xPathString);
        referringNode.setReferredSchemaNode(m_schemaRegistry.getDataSchemaNode(referredSP));
        referringNode.setReferringSchemaNode(m_schemaRegistry.getDataSchemaNode(referringSP));
        UnknownSchemaNode referringNodeUnderChangedNodeExtn = AnvExtensions.REFERRING_NODE_IS_UNDER_CHANGED_NODE.getExtensionDefinition(dataSchemaNode);
        if (referringNodeUnderChangedNodeExtn != null) {
            String constraints = ((ModelStatement) referringNodeUnderChangedNodeExtn).argument().toString();
            referringNode.parseReferringNodeIsUnderChangedNodeHints(constraints);
        }
        // If validation hints not specified either at node level or at global level, then check for deviceXmlLevelHint
        if(validationHint == null && !ReferringNode.ReferenceType.LEAFREF.equals(referenceType) && m_deviceValidationHints.containsKey(referredSP)){
            ValidationHint deviceValidationHint = m_deviceValidationHints.get(referredSP);
            LOGGER.debug("Adding ValidationHint {} on referringNode {}", deviceValidationHint, referringNode);
            referringNode.setValidationHint(deviceValidationHint);
        }
        m_schemaRegistry.registerNodesReferredInConstraints(componentId, referringNode);
        return referringNode;
    }

    private boolean hasWhenCondition(DataSchemaNode dataSchemaNode) {
        if (dataSchemaNode instanceof WhenConditionAware) {
            Optional<RevisionAwareXPath> optWhenCondition = dataSchemaNode.getWhenCondition();
            if (optWhenCondition != null && optWhenCondition.isPresent()) {
                return true;
            }
        }

        if (dataSchemaNode.isAugmenting()) {
            DataSchemaNode parentNode = m_schemaRegistry.getDataSchemaNode(dataSchemaNode.getPath().getParent());
            Pair<AugmentationSchemaNode,SchemaPath> augmentNodeAndItsResidingNode = DataStoreValidationUtil.getAugmentationSchema(m_schemaRegistry, parentNode, dataSchemaNode);
            AugmentationSchemaNode augSchema = augmentNodeAndItsResidingNode == null? null : augmentNodeAndItsResidingNode.getFirst();
            RevisionAwareXPath xpath = augSchema == null ? null : augSchema.getWhenCondition().orElse(null);
            if (xpath != null) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("rawtypes")
    private void registerRelativePath(DataSchemaNode dataSchemaNode, String xPathString, TypeDefinition typeDefition) {
        if (NetconfExtensions.IS_TREAT_AS_RELATIVE_PATH.isExtensionIn(dataSchemaNode)){
            String path = m_schemaRegistry.getMatchingPath(xPathString);
            String relativePath = m_pathBuilder.getRelativePath(xPathString, path, dataSchemaNode);
            m_schemaRegistry.registerRelativePath(xPathString, relativePath, dataSchemaNode);
        }

        if (NetconfExtensions.IS_TREAT_AS_RELATIVE_PATH.isExtensionIn(typeDefition)){
            String path = m_schemaRegistry.getMatchingPath(xPathString);
            String relativePath = m_pathBuilder.getRelativePath(xPathString, path, dataSchemaNode);
            m_schemaRegistry.registerRelativePath(xPathString, relativePath, dataSchemaNode);
        }

    }

    @Override
    public void visitLeafListNode(String componentId, SchemaPath parentSchemaPath, LeafListSchemaNode leafListNode) {
        registerConstraintSchemaPaths(componentId, leafListNode);
        registerLeafRefSchemaPaths(componentId, leafListNode);
    }

    @Override
    public void visitLeafNode(String componentId, SchemaPath parentSchemaPath, LeafSchemaNode leafSchemaNode) {
        registerConstraintSchemaPaths(componentId, leafSchemaNode);
        registerLeafRefSchemaPaths(componentId, leafSchemaNode);
    }

    @Override
    public void visitChoiceCaseNode(String componentId, SchemaPath parentPath, CaseSchemaNode choiceCaseNode) {
        registerConstraintSchemaPaths(componentId, choiceCaseNode);
        for (DataSchemaNode dataSchemaNode: choiceCaseNode.getChildNodes()) {
            registerConstraintSchemaPaths(componentId, dataSchemaNode);
        }
    }

    @Override
    public void visitChoiceNode(String componentId, SchemaPath parentPath, ChoiceSchemaNode choiceSchemaNode) {
        registerConstraintSchemaPaths(componentId, choiceSchemaNode);
        for(CaseSchemaNode caseNode : choiceSchemaNode.getCases().values()){
            visitChoiceCaseNode(componentId, parentPath, caseNode);
        }
    }

    @Override
    public void visitAnyXmlNode(String componentId, SchemaPath parentPath, AnyXmlSchemaNode anyXmlSchemaNode) {
        registerConstraintSchemaPaths(componentId, anyXmlSchemaNode);
    }

    @Override
    public void visitAnyDataNode(String componentId, SchemaPath parentPath, AnyDataSchemaNode anyDataSchemaNode) {
        registerConstraintSchemaPaths(componentId, anyDataSchemaNode);
    }

    @Override
    public void visitListNode(String componentId, SchemaPath parentPath, ListSchemaNode listSchemaNode) {
        registerConstraintSchemaPaths(componentId, listSchemaNode);
        registerMountPointSchemaPath(componentId, listSchemaNode);
        registerIfChildBigList(listSchemaNode);
    }

    @Override
    public void visitContainerNode(String componentId, SchemaPath parentSchemaPath, ContainerSchemaNode containerSchemaNode) {
        registerConstraintSchemaPaths(componentId, containerSchemaNode);
        registerMountPointSchemaPath(componentId, containerSchemaNode);
    }

    private void registerMountPointSchemaPath(String componentId, DataSchemaNode dataSchemaNode){
        if(AnvExtensions.MOUNT_POINT.isExtensionIn(dataSchemaNode)){
            m_schemaRegistry.registerMountPointSchemaPath(componentId, dataSchemaNode);
        }
    }

    private void registerIfChildBigList(DataSchemaNode dataSchemaNode){
        if(AnvExtensions.BIG_LIST.isExtensionIn(dataSchemaNode)){
            m_schemaRegistry.addToChildBigList(dataSchemaNode.getPath());
        }
    }

    @Override
    public void visitIdentityNode(String componentId, IdentitySchemaNode identitySchemaNode) {

    }

    @Override
    public void visitEnter(String componentId, SchemaPath parentPath, SchemaPath schemaPath) {

    }

    @Override
    public void visitLeave(String componentId, SchemaPath parentPath, SchemaPath schemaPath) {

    }

    @Override
    public String getErrors(){
        if (! m_errors.isEmpty()){
            return StringUtils.join(m_errors, "\n\n");
        }
        return null;
    }
}
