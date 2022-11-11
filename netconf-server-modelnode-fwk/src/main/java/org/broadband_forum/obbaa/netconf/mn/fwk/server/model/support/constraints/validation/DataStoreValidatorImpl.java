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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation;

import static org.broadband_forum.obbaa.netconf.api.messages.EditConfigOperations.DELETE;
import static org.broadband_forum.obbaa.netconf.api.messages.EditConfigOperations.REMOVE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.jxpath.ri.compiler.LocationPath;
import org.apache.commons.jxpath.ri.compiler.Step;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigOperations;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.ChangeTreeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.ReferringNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.ReferringNode.ReferenceType;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.ReferringNodes;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.ValidationHint;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.ChoiceCaseNodeUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.AnvExtensions;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditChangeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditChangeSource;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditMatchNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.DataStoreException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeKey;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildContainerHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildListHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigAttributeFactory;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeDynaBean;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeDynaBeanContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeDynaBeanFactory;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeGetException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithIndex;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.service.DSAugmentOrUsesValidation;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.service.DSConstraintValidation;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DSExpressionValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DSExtensionFunctionValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationErrors;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationPathBuilder;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.MNKeyUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.XmlModelNodeImpl;
import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ElementCountConstraint;
import org.opendaylight.yangtools.yang.model.api.ElementCountConstraintAware;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public class DataStoreValidatorImpl implements DataStoreValidator {

    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(DataStoreValidatorImpl.class, LogAppNames.NETCONF_STACK);

    private final SchemaRegistry m_schemaRegistry;
    private final ModelNodeHelperRegistry m_modelNodeHelperRegistry;
    private final ModelNodeDataStoreManager m_modelNodeDSM;
    private final DataStoreIntegrityService m_integrityService;
    private final ChoiceMandatoryChildrenValidator m_choiceCaseValidator;
    private final DSExpressionValidator m_expValidator;
    private final DataStoreValidationPathBuilder m_dsPathTraverser;
    private final DSValidation m_augmentOrUsesValidator;
    private final DSValidation m_constraintValidator;

    //For UT
    private boolean m_validatedChildCacheHitStatus = false;

    private void logDebug(String message, Object... objects) {
        Boolean isDebugEnabled = (Boolean) RequestScope.getCurrentScope().getFromCache(DataStoreValidatorImpl.class.getName());
        if (isDebugEnabled == null) {
            isDebugEnabled = LOGGER.isDebugEnabled();
            RequestScope.getCurrentScope().putInCache(DataStoreValidatorImpl.class.getName(), isDebugEnabled);
        }

        if (isDebugEnabled) {
            LOGGER.debug(message, objects);
        }
    }

    public DataStoreValidatorImpl(SchemaRegistry schemaRegistry, ModelNodeHelperRegistry modelNodeHelperRegistry,
            ModelNodeDataStoreManager modelNodeDSM, DataStoreIntegrityService integrityService, DSExpressionValidator expValidator) {
        m_schemaRegistry = schemaRegistry;
        m_modelNodeHelperRegistry = modelNodeHelperRegistry;
        m_modelNodeDSM = modelNodeDSM;
        m_integrityService = integrityService;
        m_expValidator = expValidator;
        m_choiceCaseValidator = new ChoiceMandatoryChildrenValidator(m_schemaRegistry, m_modelNodeHelperRegistry, this);
        m_dsPathTraverser = new DataStoreValidationPathBuilder(m_schemaRegistry, m_modelNodeHelperRegistry);
        m_augmentOrUsesValidator = new DSAugmentOrUsesValidation(expValidator, schemaRegistry);
        m_constraintValidator = new DSConstraintValidation(expValidator);
    }

    private void initializeCache(){
        DataStoreValidationUtil.resetValidationContext();
    }

    private void addToDeletedCache(EditContainmentNode node, QName attribute, DSValidationContext validationContext) {
        String modelNodeId = node.getModelNodeId().pathString();
        if (attribute != null) {
            // Indicates only leaf is deleted
            validationContext.recordDeletedChangeAttribute(modelNodeId, attribute);
        } else {
            // indicates the entire edit containment is deleted
            validationContext.recordDeletedChangeNode(modelNodeId, node);
        }
    }

    private boolean isNodeDeleted(EditContainmentNode changedNode, DSValidationContext validationContext) {
        EditContainmentNode node = validationContext.getDeletedNode(changedNode.getModelNodeId().pathString());
        return node != null;
    }

    private boolean isNodeDeleted(EditContainmentNode changedNode, DataSchemaNode changeNode, DSValidationContext validationContext) {
        if (isNodeDeleted(changedNode, validationContext)) {
            return true;
        }
        List<QName> qnames = validationContext.getDeletedAttribute(changedNode.getModelNodeId().pathString());
        if (qnames != null && !qnames.isEmpty()) {
            for (QName qname : qnames) {
                if (changeNode.getQName().equals(qname)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void cacheDeleteChangNode(EditContainmentNode changedNode, DSValidationContext validationContext) {
        ModelNodeId id = changedNode.getModelNodeId();

        if (validationContext.getDeletedNode(id.pathString()) == null) {
            // if the node is not already identified as deleted.
            EditContainmentNode parentEditContainement = changedNode.getParent();
            if (parentEditContainement != null) {
                if (validationContext.getDeletedNode(parentEditContainement.getModelNodeId().pathString()) != null) {
                    // if its parent node is deleted, so is the child
                    addToDeletedCache(changedNode, null,validationContext);
                    return;
                }
            }

            String operation = changedNode.getEditOperation();
            if (operation.equals(EditConfigOperations.DELETE) || operation.equals(EditConfigOperations.REMOVE)) {
                // if the node is deleted/removed add to cache
                addToDeletedCache(changedNode, null, validationContext);
                return;
            }

            for (EditChangeNode changeNode : changedNode.getChangeNodes()) {
                operation = changeNode.getOperation();
                if (operation.equals(EditConfigOperations.DELETE) || operation.equals(EditConfigOperations.REMOVE)) {
                    // add the attribute that is deleted/removed
                    addToDeletedCache(changedNode, changeNode.getQName(), validationContext);
                }
            }
        }
    }

    @Override
    public List<EditConfigRequest> validate(RootModelNodeAggregator aggregator, EditContainmentNode editTree, ChangeTreeNode changeTree, EditConfigRequest request,
                                            NetconfClientInfo clientInfo) throws ValidationException {
        TimingLogger.startPhase("iterateEditTree.validateDataStore.init");
        Set<SchemaPath> changeNodes = new LinkedHashSet<SchemaPath>();
        Map<SchemaPath, Collection<EditContainmentNode>> changeNodeMap = new HashMap<SchemaPath, Collection<EditContainmentNode>>();
        List<ModelNode> rootNodes = aggregator.getModelServiceRootsForEdit(request);
        ModelNode rootModelNode = null;
        SchemaPath rootSchemaPath = null;
        TimingLogger.endPhase("iterateEditTree.validateDataStore.init", false);

        try {
            TimingLogger.startPhase("iterateEditTree.validateDataStore.initCache");
            initializeCache();
            DSValidationContext context = new DSValidationContext();
            context.setRootNodeAggregator(aggregator);
            for (ModelNode rootNode : rootNodes) {
                /*
                 * every editTree will start from a root node. Find the right root node for the edit tree and
                 * validate
                 */
                if (editTree.getQName().equals(rootNode.getQName())) {
                    rootSchemaPath = rootNode.getModelNodeSchemaPath();
                    rootModelNode = rootNode;
                    context.addToModelNodeCache(rootSchemaPath, rootModelNode);
                    break;
                }
            }
            TimingLogger.endPhase("iterateEditTree.validateDataStore.initCache", false);
            TimingLogger.startPhase("iterateEditTree.validateDataStore.buildAllChangeSchemaPaths");
            buildAllChangeSchemaPaths(rootSchemaPath, editTree, changeNodes, changeNodeMap, m_schemaRegistry, new HashMap<>(), context);
            TimingLogger.endPhase("iterateEditTree.validateDataStore.buildAllChangeSchemaPaths", false);
            TimingLogger.startPhase("iterateEditTree.validateDataStore.validateChanges");
            // timing logged inside the method
            validateChanges(editTree, changeTree, changeNodes, changeNodeMap, rootModelNode, context);
            TimingLogger.endPhase("iterateEditTree.validateDataStore.validateChanges", false);
            TimingLogger.startPhase("iterateEditTree.validateDataStore.checkForDeletion");
            checkForNonPresenceContainerDeletion(context);
            TimingLogger.endPhase("iterateEditTree.validateDataStore.checkForDeletion", false);
            try {
                TimingLogger.startPhase("iterateEditTree.validateDataStore.createInternalEditRequests");
                return m_integrityService.createInternalEditRequests(request, clientInfo,context);
            } finally {
                TimingLogger.endPhase("iterateEditTree.validateDataStore.createInternalEditRequests", false);
            }
        } catch (GetAttributeException | ModelNodeGetException e) {
            throw new ValidationException(e);
        }
    }

    public void clearValidationCache() {
        ModelNodeDynaBeanFactory.clearDynaBeanCachedNodes();
        DSExtensionFunctionValidator.clearCachedNodes();
        DSExpressionValidator.clearCachedNodes();
    }

    private void checkForNonPresenceContainerDeletion(DSValidationContext validationContext) throws ModelNodeGetException {
        DSValidationNodeIndex deleteNodes = new DSValidationNodeIndex(validationContext.getDeleteList());

        for (Map.Entry<ModelNode, Collection<QName>> deleteList : deleteNodes.entrySet()) {
            if (deleteList.getKey() instanceof ModelNodeWithAttributes) {
                for (QName qnameInList : deleteList.getValue()) {
                    checkForDeletion(deleteList.getKey(), qnameInList, validationContext);
                }
            }
        }
    }

    private boolean hasChild(DSValidationNodeIndex nodeMap, ModelNode parentNode, QName childName) {
        Collection<QName> childList = nodeMap.get(parentNode);
        if (childList != null) {
            return childList.contains(childName);
        }
        return false;
    }

    private void checkForDeletion(ModelNode node, QName childName, DSValidationContext validationContext) throws ModelNodeGetException {
        if (node.getParent() == null) {
            // delete/remove on root node not allowed
            return;
        }

        if (node instanceof ModelNodeWithAttributes) {
            if (hasNoOtherChildren((ModelNodeWithAttributes) node, childName)) {
                ModelNode parentNode = node.getParent();
                if (parentNode != null) {
                    if (!validationContext.isNodeForCreate(node)) {
                        validationContext.removeFromDelete(node);
                        validationContext.removeFromMerge(node);
                        validationContext.recordForDelete(parentNode, node.getQName());
                        checkForDeletion(parentNode, node.getQName(), validationContext);
                    }
                }
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private boolean hasNoOtherChildren(ModelNodeWithAttributes modelNode, QName exception) throws ModelNodeGetException {
        SchemaPath schemaPath = modelNode.getModelNodeSchemaPath();
        DSValidationMountContext context = SchemaRegistryUtil.getDataSchemaNode(modelNode.getSchemaRegistry(), schemaPath);
        SchemaRegistry schemaRegistry = context.getSchemaRegistry();
        DataSchemaNode schemaNode = context.getDataSchemaNode();
        ModelNodeHelperRegistry modelNodeHelperRegistry = modelNode.getMountModelNodeHelperRegistry();
        // check if it is a container
        if (!(schemaNode instanceof ContainerSchemaNode)) {
            return false;
        }

        //check if it is a presence container
        if (((ContainerSchemaNode) schemaNode).isPresenceContainer()) {
            return false;
        }

        SchemaPath childSchemaPath = schemaRegistry.getDescendantSchemaPath(schemaPath, exception);
        DataSchemaNode childSchemaNode = SchemaRegistryUtil.getDataSchemaNode(schemaRegistry, childSchemaPath).getDataSchemaNode();

        Map leafLists = modelNode.getLeafLists();
        Map attributes = modelNode.getAttributes();
        // check if there are more than 1 leafList then return false
        // if there is only one leafList and if it is not exception, return false
        if (leafLists.size() > 1) {
            return false;
        } else if (leafLists.size() == 1 && leafLists.get(exception) == null && !(childSchemaNode instanceof LeafListSchemaNode)) {
            return false;
        }

        // if there are more than one attribute return false
        // if there is only one attribute and it is not exception, return false
        if (attributes.size() > 1) {
            return false;
        } else if (attributes.size() == 1 && attributes.get(exception) == null && !(childSchemaNode instanceof LeafSchemaNode)) {
            return false;
        }

        //if there are any childContainers and that is not exception return false
        Map<QName, ChildContainerHelper> containers = modelNodeHelperRegistry.getChildContainerHelpers(schemaPath);
        for (Map.Entry<QName, ChildContainerHelper> helper : containers.entrySet()) {
            if (!helper.getKey().equals(exception) && helper.getValue() != null && helper.getValue().getValue(modelNode) != null) {
                return false;
            }
        }

        //if there are any childList and that is not exception, return false
        Map<QName, ChildListHelper> lists = modelNodeHelperRegistry.getChildListHelpers(schemaPath);
        for (Map.Entry<QName, ChildListHelper> helper : lists.entrySet()) {
            if (!helper.getKey().equals(exception) && helper.getValue() != null && !helper.getValue().getValue(modelNode,
                    Collections.emptyMap()).isEmpty()) {
                return false;
            }
        }

        Collection<DataSchemaNode> children = schemaRegistry.getChildren(schemaPath);

        //Need to add some logic here so that it checks for container/list nodes inside choice

        // if there are any state nodes, return false

        for (DataSchemaNode child : children) {
            if (!child.isConfiguration()) {
                return false;
            } else if(child instanceof ChoiceSchemaNode){
                if(ChoiceCaseNodeUtil.hasChoiceChildExceptNodeToBeDeleted((ChoiceSchemaNode) child, modelNode, childSchemaNode)){
                    return false;
                }
            }
        }
        return true;
    }

    private List<ModelNode> getParentWithAccessPath(EditContainmentNode editNode, DynaBean parentBean, LocationPath accessPath,
            List<ModelNode> parentNodes, DataSchemaNode referenceNode, boolean nodeNotDeleted, boolean missingParentNode,
                                                    DSValidationContext validationContext) {
        Step[] accessPathSteps = accessPath.getSteps();
        if (accessPath.isAbsolute()) {
            DataSchemaNode rootSchemaNode = DataStoreValidationUtil.getRootSchemaNodeWithModuleNameInPrefix(editNode.getSchemaRegistry(),
                    accessPathSteps[0]);
            if (rootSchemaNode != null) {
                parentBean = DataStoreValidationUtil.getRootModelNode(editNode.getSchemaRegistry(), rootSchemaNode, validationContext);
                if (parentBean == null) {
                    // impact node does not exists. return
                    return parentNodes;
                }

                Step[] newAccessPathSteps = new Step[accessPathSteps.length - 1];
                System.arraycopy(accessPathSteps, 1, newAccessPathSteps, 0, newAccessPathSteps.length);
                accessPathSteps = newAccessPathSteps;

            }
            // if this is a abs access path, then it does not matter if the node is deleted or not, since the access path is from root
            parentNodes = m_dsPathTraverser.getParentModelNodeWithAccessPath(editNode, parentBean, accessPathSteps, referenceNode, true,
                    missingParentNode, validationContext);
        } else {
            parentNodes = m_dsPathTraverser.getParentModelNodeWithAccessPath(editNode, parentBean, accessPathSteps, referenceNode,
                    nodeNotDeleted, missingParentNode, validationContext);
        }
        return parentNodes;
    }

    private void validateImpactedNode(ModelNode rootModelNode, ModelNode parentNode, EditContainmentNode changedEditNode,
            DataSchemaNode changeSchemaNode, SchemaPath referencedSchemaPath, ReferringNode referringNode,
            boolean nodeNotDeleted, Set<Pair<ModelNode, DataSchemaNode>> mMNToCheckTriggeredByImpact, DSValidationContext validationContext) {
        LocationPath accessPath = (LocationPath) referringNode.getReferringNodeAP();

        String constraint = getConstraint(referringNode);
        boolean logConstraint = accessPath != null;
        if (logConstraint) {
            TimingLogger.startConstraint(TimingLogger.ConstraintType.IMPACT.toString() + "/" + referringNode.getReferenceType().toString(), constraint);
        }
        TimingLogger.startPhase("validateImpact.validateImpactedNode.getParentNodesOfReferringNodes");

        List<ModelNode> parentNodes = getParentNodesOfReferringNodes(rootModelNode, parentNode, changedEditNode, changeSchemaNode,
                referencedSchemaPath, nodeNotDeleted, accessPath, referringNode, false, validationContext);
        TimingLogger.endPhase("validateImpact.validateImpactedNode.getParentNodesOfReferringNodes", false);
        SchemaRegistry registry = changedEditNode.getSchemaRegistry();
        DSValidationMountContext context = SchemaRegistryUtil.getDataSchemaNode(registry, referencedSchemaPath);
        DataSchemaNode schemaNode = context.getDataSchemaNode();
        boolean impactedNodeParentFound = false;
        for (ModelNode parentModelNode : parentNodes) {
            if (isParent(parentModelNode, referencedSchemaPath)) {
                if (schemaNode instanceof LeafSchemaNode) {
                    if (isLeafAlreadyExists(parentModelNode, referencedSchemaPath)){
                        validationContext.getSchemaPathsToCreate().remove(referencedSchemaPath);
                    } else {
                        if ( SchemaRegistryUtil.needsMMNCheck(schemaNode)){
                            // need to check the missing mandatory nodes triggered by the referring node
                            // That is when referring node and referred nodes are not in same yang hierarchy and referring node/or its parent not present in the
                            // request, we end up checking the mandatory nodes. Here it is validating.
                            addImpactedNodeToCheckMMN(mMNToCheckTriggeredByImpact, schemaNode, parentModelNode);
                            impactedNodeParentFound = true;
                        }
                    }

                } else if (schemaNode instanceof ContainerSchemaNode && doesContainerAlreadyExist(parentModelNode, schemaNode)) {
                    validationContext.getSchemaPathsToCreate().remove(referencedSchemaPath);
                }
                SchemaPath caseParentPath = ChoiceCaseNodeUtil.getCaseSchemaNodeFromChildNodeIfApplicable(registry, schemaNode.getPath());
                boolean anyCasePresent = true;
                boolean isCurrentCasePresent = true;
                if(caseParentPath!=null){
                    anyCasePresent = ChoiceCaseNodeUtil.hasChoiceChildInModelNode((ChoiceSchemaNode) registry.getDataSchemaNode(caseParentPath.getParent()), parentModelNode);
                    if(parentModelNode instanceof ModelNodeWithAttributes) {
                        isCurrentCasePresent = ChoiceCaseNodeUtil.hasCaseChildInModelNode((CaseSchemaNode) registry.getDataSchemaNode(caseParentPath), parentModelNode);
                    }
                }
                if(!anyCasePresent || (anyCasePresent && isCurrentCasePresent)) {
                    validateChildIfRequired(parentModelNode, schemaNode, referringNode, validationContext);
                }
            }
        }
        if ( !impactedNodeParentFound && parentNode != null){
            if ( SchemaRegistryUtil.needsMMNCheck(schemaNode)){
                List<ModelNode> missingParentNodes = getParentNodesOfReferringNodes(rootModelNode, parentNode, changedEditNode, changeSchemaNode,
                    referencedSchemaPath, nodeNotDeleted, accessPath, referringNode, true, validationContext);
                for (ModelNode parentModelNode : missingParentNodes) {
                    addImpactedNodeToCheckMMN(mMNToCheckTriggeredByImpact, schemaNode, parentModelNode);
                }
            }
        }
        if (logConstraint) {
            TimingLogger.endConstraint(TimingLogger.ConstraintType.IMPACT.toString() + "/" + referringNode.getReferenceType().toString(),
                    constraint);
        }

    }

    private void addImpactedNodeToCheckMMN(Set<Pair<ModelNode, DataSchemaNode>> mMNToCheckTriggeredByImpact, DataSchemaNode schemaNode, ModelNode parentModelNode) {
        ModelNode existingParentNodeInDs = parentModelNode;
        DataSchemaNode existingNodeInDs = schemaNode;
        while (existingParentNodeInDs != null && existingParentNodeInDs instanceof ProxyValidationModelNode) {
            existingParentNodeInDs = existingParentNodeInDs.getParent();
        }
        if (existingParentNodeInDs != null && existingParentNodeInDs.getParent() != null) {
            SchemaRegistry registry = existingParentNodeInDs.getSchemaRegistry();
            existingNodeInDs = registry.getDataSchemaNode(existingParentNodeInDs.getModelNodeSchemaPath());
            if (existingNodeInDs != null){ // If root node is not created yet
                mMNToCheckTriggeredByImpact.add(new Pair<>(existingParentNodeInDs.getParent(), existingNodeInDs));
            }
        }
    }

    private List<ModelNode> getParentNodesOfReferringNodes(ModelNode rootModelNode, ModelNode parentNode,
            EditContainmentNode changedEditNode, DataSchemaNode changeSchemaNode, SchemaPath referencedSchemaPath,
            boolean nodeNotDeleted, LocationPath accessPath, ReferringNode referringNode, boolean missingParentNode,
                                                           DSValidationContext validationContext) {
        List<ModelNode> parentNodes = new ArrayList<>();
        if (referringNode.isReferredNodeIsUnderChangedNode() && parentNode instanceof XmlModelNodeImpl) {
            return getParentNodeFromIndex((XmlModelNodeImpl) parentNode, referringNode, parentNodes);
        }

        boolean logConstraint = false;
        DSValidationMountContext context = SchemaRegistryUtil.getDataSchemaNode(changedEditNode.getSchemaRegistry(),
                referencedSchemaPath);
        DataSchemaNode referenceNode = context.getDataSchemaNode();
        Step[] accessPathSteps = accessPath == null ? null : accessPath.getSteps();
        if (parentNode == null) {
            parentNodes = getParentModelNode(changeSchemaNode, changedEditNode, referencedSchemaPath, accessPathSteps, referenceNode, validationContext);
        } else {
            if (accessPath == null || accessPath.getSteps().length == 0) {
                accessPath = buildAccessPath(referencedSchemaPath, changedEditNode.getSchemaRegistry());
                TimingLogger.startConstraint(TimingLogger.ConstraintType.IMPACT.toString() + "/" + referringNode.getReferenceType().toString() + " getting parentNodes", referringNode.getSimpleReferredSP() + " -impacts -> " + accessPath.toString());
                logConstraint = true;
                accessPathSteps = accessPath.getSteps();
            }
            DynaBean parentBean = (DynaBean) parentNode.getValue();
            
            parentNodes = getParentWithAccessPath(changedEditNode, parentBean, accessPath, parentNodes, referenceNode, nodeNotDeleted,
                    missingParentNode, validationContext);
            if (parentNodes.isEmpty()) {
                if (SchemaRegistryUtil.hasDefaults(referenceNode) || (referenceNode instanceof ContainerSchemaNode
                        && DataStoreValidationUtil.containerHasDefaultLeafs(context.getSchemaRegistry(),
                        (ContainerSchemaNode) referenceNode))) {
                    // might be a parent Node missing for a when condition leaf inside a non-presence container.
                    // might be a parent Node missing for a when condition container with default leafs.
                    parentNodes = getParentWithAccessPath(changedEditNode, parentBean, accessPath, parentNodes, referenceNode,
                            nodeNotDeleted, true, validationContext);
                }
            }
        }

        if (parentNodes.isEmpty() && accessPath != null) {
            //could be abs path
            parentNodes = getParentWithAccessPath(changedEditNode, (DynaBean) rootModelNode.getValue(), accessPath, parentNodes,
                    referenceNode, nodeNotDeleted, false, validationContext);
        }
        if (logConstraint) {
            TimingLogger.endConstraint(TimingLogger.ConstraintType.IMPACT.toString() + "/" + referringNode.getReferenceType().toString() + " getting parentNodes", referringNode.getSimpleReferredSP() + " -impacts -> " + accessPath.toString());
        }
        return parentNodes;
    }

    private List<ModelNode> getParentNodeFromIndex(XmlModelNodeImpl parentNode, ReferringNode referringNode, List<ModelNode> parentNodes) {
        XmlModelNodeImpl xmlNode = parentNode;
        //eg: /interfaces/interface[name= 'xyz']
        String changedNodeXPath = xmlNode.getIndexNodeId().xPathString(null, true, true);
        //eg: channel-pair
        List<QName> relativeQnames = DataStoreValidationUtil.getRelativeQNames(xmlNode, referringNode.getReferringSP());
        String parentXPath = DataStoreValidationUtil.buildIndexXPath(changedNodeXPath, relativeQnames);
        ModelNode parentOfReferringNode = (ModelNode) xmlNode.getIndexedValue(parentXPath);
        if (parentOfReferringNode != null) {
            parentNodes.add(parentOfReferringNode);

        }
        return parentNodes;
    }

    private String getConstraint(ReferringNode referringNode) {
        if (referringNode.getReferringNodeAP() == null) {
            return null;
        }
        return referringNode.getSimpleReferredSP() + " -impacts-> " + referringNode.getReferringNodeAP().toString() + " -with-constraint" +
        "->" +
        " " + referringNode.getConstraintXPath();
    }

    @SuppressWarnings("rawtypes")
    private boolean isParent(ModelNode modelNode, SchemaPath childSchemaPath) {
        /*
         * This method checks whether the childSchemaPath is present in the
         * modelNode or not. Basically this check is needed so that
         * childSchemaPath is validated under the correct parentModelNode
         *
         */
        SchemaRegistry schemaRegistry = modelNode.getSchemaRegistry();
        DataSchemaNode dataSchemaNode = schemaRegistry.getDataSchemaNode(childSchemaPath);
        if ( ChoiceCaseNodeUtil.isChoiceOrCaseNode(dataSchemaNode)) {
            DataSchemaNode nonChoiceParent = schemaRegistry.getNonChoiceParent(childSchemaPath);
            if ( modelNode.getModelNodeSchemaPath().equals(nonChoiceParent.getPath())) {
                return true;
            }
        }
        DynaBean dynaBean = (DynaBean) modelNode.getValue();
        final String localName = ModelNodeDynaBeanFactory.getDynaBeanAttributeName(childSchemaPath.getLastComponent().getLocalName());
        DynaProperty childProperty = dynaBean.getDynaClass().getDynaProperty(localName);
        if (childProperty != null) {
            ModelNodeDynaBeanContext dynaBeanContext = DataStoreValidationUtil
                    .getModelNodeDynaBeanContext(schemaRegistry, localName, schemaRegistry
                            .getNamespaceURI(childSchemaPath.getLastComponent().getModule().getNamespace().toString()),
                            null);
            Object children = ModelNodeDynaBean.withContext(dynaBeanContext, () -> dynaBean.get(localName));
            if (children != null) {
                if (children instanceof List && ((List) children).size() > 0) {
                    children = ((List) children).get(0);
                }
                if (children instanceof ModelNodeDynaBean) {
                    ModelNode childNode = (ModelNode) ((DynaBean) children).get(ModelNodeWithAttributes.MODEL_NODE);
                    if (!(childNode.getModelNodeSchemaPath().equals(childSchemaPath))) {
                        return false;
                    }
                }
            }
        }

        DataSchemaNode parentSchemanode = modelNode.getSchemaRegistry().getNonChoiceParent(childSchemaPath);
        if (parentSchemanode != null) {
            if (!modelNode.getModelNodeSchemaPath().getLastComponent().equals(parentSchemanode.getQName())) {
                return false;
            }
        }
        return true;
    }

    private boolean isLeafAlreadyExists(ModelNode modelNode, SchemaPath leafSchemaPath) {
        DynaBean dynaBean = (DynaBean) modelNode.getValue();
        String localName = ModelNodeDynaBeanFactory.getDynaBeanAttributeName(leafSchemaPath.getLastComponent().getLocalName());
        DynaProperty childProperty = dynaBean.getDynaClass().getDynaProperty(localName);
        if (childProperty != null) {
            return true;
        }
        return false;
    }

    private boolean doesContainerAlreadyExist(ModelNode modelNode, DataSchemaNode node) {
        try {
            ChildContainerHelper childContainerHelper =
                    modelNode.getMountModelNodeHelperRegistry().getChildContainerHelpers(modelNode.getModelNodeSchemaPath())
                    .get(node.getQName());

            if (childContainerHelper != null) {
                ModelNode childContainerHelper2 = childContainerHelper.getValue(modelNode);
                if (childContainerHelper2 != null) {
                    return true;
                }
            }

        } catch (ModelNodeGetException e) {
            throw new ValidationException(e);
        }

        return false;
    }

    private List<ModelNode> getParentModelNode(DataSchemaNode changeSchemaNode, EditContainmentNode changeEditNode,
            SchemaPath referencePath, Step[] accessPathSteps, DataSchemaNode referenceNode, DSValidationContext validationContext) {
        List<ModelNode> returnValue = new ArrayList<ModelNode>();
        boolean newAccessPath = false;
        if (accessPathSteps == null || accessPathSteps.length == 0) {
            accessPathSteps = buildAccessPath(referencePath, changeEditNode.getSchemaRegistry()).getSteps();
            newAccessPath = true;
        }
        if (accessPathSteps != null && accessPathSteps[0] != null) {
            Step[] steps = null;
            String nodeName = DataStoreValidationUtil.getLocalName(accessPathSteps[0]);
            String operation = changeEditNode.getEditOperation();
            boolean deleteNode = false;
            if (!newAccessPath && (operation.equals(DELETE) || operation.equals(REMOVE))) {
                deleteNode = true;
            }

            EditContainmentNode nextNode = null;
            if (changeEditNode.getQName().getLocalName().equals(nodeName) && deleteNode) {
                nextNode = changeEditNode.getParent();
                steps = new Step[accessPathSteps.length - 1];
                System.arraycopy(accessPathSteps, 1, steps, 0, steps.length);
            }
            /**
             * if nextNode!=null --> indicates we have reached next step of the path. Continue to find the next path
             * if nextNode == null && deleteNode is true --> indicates the modelNode is deleted and we need to skip the step and proceed
             * if nextNode == null && deleteNode is false, we might have hit the end node.
             * 		if schemaNode is not null here (indicating we have not exhausted the steps and could not find the node) find
             *      the modelNode with the remaning steps in accessPath and the current schemaNode
             */
            if (nextNode != null && changeSchemaNode != null) {
                DSValidationMountContext context = SchemaRegistryUtil.getDataSchemaNode(changeEditNode.getSchemaRegistry(),
                        changeSchemaNode.getPath().getParent());
                DataSchemaNode parentSchemaNode = context.getDataSchemaNode();
                returnValue.addAll(getParentModelNode(parentSchemaNode, nextNode, referencePath, steps, referenceNode, validationContext));
            } else if (deleteNode && nextNode == null && changeSchemaNode != null) {
                // could be a attribute in a deleted parent node, that is not part of the change
                steps = new Step[accessPathSteps.length - 1];
                System.arraycopy(accessPathSteps, 1, steps, 0, steps.length);
                DSValidationMountContext context = SchemaRegistryUtil.getDataSchemaNode(changeEditNode.getSchemaRegistry(),
                        changeSchemaNode.getPath().getParent());
                DataSchemaNode parentSchemaNode = context.getDataSchemaNode();
                returnValue.addAll(getParentModelNode(parentSchemaNode, changeEditNode, referencePath, steps, referenceNode, validationContext));
            } else if (changeSchemaNode != null) {
                ModelNode parentModelNode = getParentNode(changeSchemaNode.getPath(), changeEditNode, validationContext);
                if (parentModelNode != null) {
                    returnValue.addAll(m_dsPathTraverser.getParentModelNodeWithAccessPath(changeEditNode,
                            (DynaBean) parentModelNode.getValue(), accessPathSteps, referenceNode, true, false, validationContext));
                }
            } else {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Reached the rootNode therefore returning the existing ModelNode list as changeSchemaNode will be null " +
                            "now");
                }
            }

        }
        return returnValue;
    }

    private LocationPath buildAccessPath(SchemaPath referencePath, SchemaRegistry schemaRegistry) {
        LocationPath accessPath;
        List<Step> inputPath = new LinkedList<Step>();
        DSValidationMountContext context = SchemaRegistryUtil.getDataSchemaNode(schemaRegistry, referencePath);
        DataStoreValidationUtil.buildAbsAccessPath(context.getSchemaRegistry(), context.getDataSchemaNode(), inputPath);
        accessPath = new LocationPath(true, inputPath.toArray(new Step[0]));
        return accessPath;
    }

    private void validateChanges(EditContainmentNode editTree, ChangeTreeNode changeTree, Set<SchemaPath> changeNodes, Map<SchemaPath,
            Collection<EditContainmentNode>> changeNodeMap, ModelNode rootModelNode, DSValidationContext validationContext)
                    throws ValidationException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Validating changes for {}", editTree);
        }
        Set<SchemaPath> changeNodesSet = new HashSet<SchemaPath>(changeNodes);
        Collection<SchemaPath> schemaPathToDelete = validationContext.getSchemaPathsToDelete();
        Set<Pair<ModelNode, DataSchemaNode>> mMNToCheckTriggeredByImpact = new HashSet<>();
        for (SchemaPath changePath : changeNodes) {
            for (EditContainmentNode changedNode : changeNodeMap.get(changePath)) {
                SchemaRegistry schemaRegistry = changedNode.getSchemaRegistry();
                DSValidationMountContext context = SchemaRegistryUtil.getDataSchemaNode(schemaRegistry, changePath);
                DataSchemaNode changeSchemaNode = context.getDataSchemaNode();
                if (changeSchemaNode == null) {
                    throw new ValidationException(String.format("DataSchemaNode is null for schemaPath %s", changePath));
                }
                validationContext.setCurrentModelNodeId(changedNode.getModelNodeId());
                // for each new expression evaluation, reset the single step count
                RequestScope.getCurrentScope().putInCache(DataStoreValidationUtil.SINGLE_STEP_COUNT, null);

                TimingLogger.startPhase("iterateEditTree.validateDataStore.validateChanges.getParent");
                ModelNode parentNode = null;
                SchemaPath childPath = changeSchemaNode.getPath();
                parentNode = getParentNode(childPath, changedNode, validationContext);
                TimingLogger.endPhase("iterateEditTree.validateDataStore.validateChanges.getParent", false);
                boolean nodeNotDeleted = true;
                if (isNodeDeleted(changedNode, changeSchemaNode, validationContext)) {
                    nodeNotDeleted = false;
                }

                if (parentNode == null) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("parentNode is null for dataSchemaNode:{} and path:{}. is node not deleted?:{}", changeSchemaNode,
                                changePath, nodeNotDeleted);
                    }
                    if (changeSchemaNode.getQName().equals(rootModelNode.getQName())) {
                        parentNode = rootModelNode;
                    }
                }

                boolean hasNodeChanged = hasNodeChanged(changeTree, changedNode, changeSchemaNode, changePath) || schemaRegistry.getRootDataSchemaNodes().contains(changeSchemaNode);

                if (nodeNotDeleted && hasNodeChanged) {
                    /*
                     * we dont want to validate a deleted node.
                     */
                    TimingLogger.startPhase("iterateEditTree.validateDataStore.validateChanges.validateChild");
                    validateChild(parentNode, changeSchemaNode, validationContext);
                    TimingLogger.endPhase("iterateEditTree.validateDataStore.validateChanges.validateChild", false);
                }
                if(hasNodeChanged) {
                    TimingLogger.startPhase("iterateEditTree.validateDataStore.validateChanges.validateImpact");
                    parentNode = validateImpact(changeNodeMap, changeTree, rootModelNode, changeNodesSet, schemaPathToDelete, changePath,
                            changeSchemaNode, changedNode, nodeNotDeleted, parentNode, mMNToCheckTriggeredByImpact, validationContext);
                    TimingLogger.endPhase("iterateEditTree.validateDataStore.validateChanges.validateImpact", false);
                }

                if (hasNodeChanged && isUserChange(changedNode,
                        changePath) && !isNodeDeleted(changedNode, validationContext) ) {
                    if (parentNode == null) {
                        parentNode = rootModelNode;
                    }
                    // after validating all impact nodes for creation/deletion check for mandatory node existence
                    TimingLogger.startPhase("iterateEditTree.validateDataStore.validateChanges.checkForMandatoryNodes");
                    checkForMandatoryNodes(parentNode, changeSchemaNode, getMatchCriteria(changeSchemaNode, changedNode), changeNodesSet,
                            changeNodeMap,validationContext);
                    TimingLogger.endPhase("iterateEditTree.validateDataStore.validateChanges.checkForMandatoryNodes", false);
                }
            }
        }
        for ( Pair<ModelNode, DataSchemaNode> mmnToCheckTriggeredByImpact : mMNToCheckTriggeredByImpact){
            TimingLogger.startPhase("iterateEditTree.validateDataStore.validateChanges.validateImpact.checkForMandatoryNodes");
            checkForMandatoryNodes(mmnToCheckTriggeredByImpact.getFirst(), mmnToCheckTriggeredByImpact.getSecond(),
                    Collections.emptyMap(), changeNodesSet, changeNodeMap, validationContext);
            TimingLogger.endPhase("iterateEditTree.validateDataStore.validateChanges.validateImpact.checkForMandatoryNodes", false);
        }

    }

    private boolean isUserChange(EditContainmentNode changedNode, SchemaPath changePath){
        EditChangeNode change = changedNode.getChangeNode(changePath.getLastComponent());
        if (change != null && EditChangeSource.system.equals(change.getChangeSource())){
            return false;
        }
        return true;
    }

    private ModelNode validateImpact(Map<SchemaPath, Collection<EditContainmentNode>> changeNodeMap, ChangeTreeNode changeTree, ModelNode rootModelNode,
            Set<SchemaPath> changeNodesSet, Collection<SchemaPath> schemaPathToDelete, SchemaPath changePath, DataSchemaNode changeSchemaNode,
            EditContainmentNode changedNode, boolean nodeNotDeleted, ModelNode parentNode,
                                     Set<Pair<ModelNode, DataSchemaNode>> mMNToCheckTriggeredByImpact, DSValidationContext validationContext) {
        try {
            validationContext.setImpactValidation(true);
            /**
             * validate all the referenced nodes that are impacted by
             * the changeNode
             */
            ReferringNodes impactedPaths = changedNode.getSchemaRegistry().getReferringNodesForSchemaPath(changePath);
            LOGGER.debug("impacted nodes for the given changePath {} are {} ", changePath, impactedPaths);
            TimingLogger.startPhase("validateImpact.gettingImpactedPaths");

            if (EditConfigOperations.REMOVE.equals(changedNode.getEditOperation())
                    || EditConfigOperations.DELETE.equals(changedNode.getEditOperation())) {
                if (changeSchemaNode instanceof ListSchemaNode || changeSchemaNode instanceof ContainerSchemaNode) {
                    Set<QName> skipImmediateChildQNames = new HashSet<>();
                    changedNode.getMatchNodes().forEach(editMatchNode -> skipImmediateChildQNames.add(editMatchNode.getQName()));
                    changedNode.getChangeNodes().forEach(editChangeNode -> skipImmediateChildQNames.add(editChangeNode.getQName()));
                    ReferringNodes childImpactPaths = parentNode.getSchemaRegistry().addChildImpactPaths(changePath, skipImmediateChildQNames);
                    Map<SchemaPath, Set<ReferringNode>> referringNodes = impactedPaths.getReferringNodes();
                    impactedPaths = new ReferringNodes(impactedPaths.getRefferedNodeSP());
                    impactedPaths.putAll(referringNodes);
                    impactedPaths.putAll(childImpactPaths);
                }
            }
            TimingLogger.endPhase("validateImpact.gettingImpactedPaths", false);
            if (schemaPathToDelete != null) {
                Collection<SchemaPath> schemaPaths = impactedPaths.keySet();
                for (SchemaPath schemaPath : schemaPaths) {
                    if(changeTree.getNodesIndex().get(schemaPath) == null) {
                        schemaPathToDelete.add(schemaPath);
                    }
                }
            }
            TimingLogger.startPhase("validateImpact.iterateThtoughImpactPaths");
            for (Map.Entry<SchemaPath, Set<ReferringNode>> impactedPath : impactedPaths.entrySet()) {
                TimingLogger.startPhase("validateImpact.iterateThtoughImpactPaths.part1");
                boolean validate = false;
                SchemaPath referringNodeSP = impactedPath.getKey();
                boolean containsImpactPath = changeNodesSet.contains(referringNodeSP);
                if (containsImpactPath) {
                    TimingLogger.startPhase("validateImpact.isDeletedList");
                    if (isDeletedList(changeNodeMap, referringNodeSP, changedNode.getSchemaRegistry(), validationContext)
                            || (changeSchemaNode instanceof ListSchemaNode
                                    && isDeletedList(changeNodeMap, changePath, changedNode.getSchemaRegistry(), validationContext))) {
                        // one of the instances of the list is deleted, not necessarily all
                        validate = true;
                    }
                    TimingLogger.endPhase("validateImpact.isDeletedList", false);

                    if(!validate) {
                        TimingLogger.startPhase("validateImpact.valueHasChanged");
                        Map<ModelNodeId, ChangeTreeNode> modelNodeIdChangeTreeNodeMap = changeTree.getNodesIndex().get(referringNodeSP);
                        if(modelNodeIdChangeTreeNodeMap != null) {
                            for (ChangeTreeNode value : modelNodeIdChangeTreeNodeMap.values()) {
                                if (!value.hasChanged()) {
                                    validate = true;
                                    break;
                                }
                            }
                        } else {
                            validate = true;
                        }
                        TimingLogger.endPhase("validateImpact.valueHasChanged", false);
                    }
                } else {
                    validate = true;
                }
                TimingLogger.endPhase("validateImpact.iterateThtoughImpactPaths.part1", false);
                TimingLogger.startPhase("validateImpact.iterateThtoughImpactPaths.part2");
                for (ReferringNode referringNode : impactedPath.getValue()) {
                    if (validate) {
                        TimingLogger.startPhase("readingValidationHints");
                        validate = readHints(parentNode, changeTree, changedNode, referringNode);
                        TimingLogger.endPhase("readingValidationHints", false);
                    }

                    if (validate) {
                        DSValidationMountContext context = SchemaRegistryUtil.getDataSchemaNode(changedNode.getSchemaRegistry(),
                                impactedPath.getKey());
                        DataSchemaNode constraintNode = context.getDataSchemaNode();
                        if (constraintNode != null) {
                            boolean isDefaultCreationAllowed =
                                    ChoiceCaseNodeUtil.isDefaultCreationAllowed(changedNode.getSchemaRegistry(), constraintNode);
                            if (constraintNode instanceof LeafSchemaNode) {
                                Optional<?> defaultValue = ((LeafSchemaNode) constraintNode).getType().getDefaultValue();
                                if (defaultValue.isPresent() && (isDefaultCreationAllowed || isConstraintNodeParentPartOfChangeSet(changeNodesSet, changedNode, constraintNode))) {
                                    // only if the leaf has a default, it is OK to create the leaf on when becoming true
                                    validationContext.getSchemaPathsToCreate().add(impactedPath.getKey());
                                }
                            }
                            if (constraintNode instanceof ContainerSchemaNode) {
                                if (!((ContainerSchemaNode) constraintNode).isPresenceContainer() && isDefaultCreationAllowed) {
                                    // only if it is a non-presence container, it is
                                    // OK to create the container on when becoming true
                                    validationContext.getSchemaPathsToCreate().add(impactedPath.getKey());
                                }
                            }

                            // For leaf-list, no default are defined
                            // For list, a default referringNodeSP is never mostly defined.
                        }
                        if (parentNode == null) {
                            TimingLogger.startPhase("validateImpact.getParentNode");
                            parentNode = getParentNode(changeSchemaNode.getPath(), changedNode, validationContext);
                            TimingLogger.endPhase("validateImpact.getParentNode", false);
                        }
                        LOGGER.debug("perform impact validation for the changedNode {} , referringNode {} ", changedNode, referringNode);
                        TimingLogger.startPhase("validateImpact.validateImpactedNode");
                        validateImpactedNode(rootModelNode, parentNode, changedNode, changeSchemaNode, referringNodeSP, referringNode,
                                nodeNotDeleted, mMNToCheckTriggeredByImpact, validationContext);
                        TimingLogger.endPhase("validateImpact.validateImpactedNode", false);
                    }
                }
                TimingLogger.endPhase("validateImpact.iterateThtoughImpactPaths.part2", false);
            }
            TimingLogger.endPhase("validateImpact.iterateThtoughImpactPaths", false);
        } finally {
            schemaPathToDelete.clear();
            validationContext.setImpactValidation(false);
            validationContext.clearSchemaPathsToCreate();
        }
        return parentNode;
    }

    private boolean readHints(ModelNode parentNode, ChangeTreeNode changeTree, EditContainmentNode changedNode, ReferringNode referringNode) {
        SchemaPath referringSP = referringNode.getReferringSP();
        SchemaRegistry schemaRegistry = changedNode.getSchemaRegistry();
        if(schemaRegistry.isSkipValidationBySchemaPathWithConstraintXpath(referringSP, referringNode.getConstraintXPath())){
            return false;
        }
        if (ReferringNode.ReferenceType.LEAFREF.equals(referringNode.getReferenceType()) &&
                referringNode.isKeyLeaf()) {
            if (!(REMOVE.equals(changedNode.getEditOperation()) || DELETE.equals(changedNode.getEditOperation()))) {
                //we need to do leafref impact validation only when the key leaf is deleted or removed
                return false;
            }
        }

        ValidationHint hint = referringNode.getValidationHint();
        if (ValidationHint.SKIP_IMPACT_VALIDATION.equals(hint)) {
            return false;
        }

        if (parentNode instanceof ModelNodeWithIndex) {
            boolean isNewNode =
                    ((ModelNodeWithIndex) parentNode).isNewNode(changedNode.getModelNodeId().xPathString(schemaRegistry,
                            true, true));
            if (ValidationHint.SKIP_IMPACT_ON_CREATE.equals(hint)) {
                if (isNewNode) {
                    return false;
                }
            }
            if(changeTree != null) {
                if (!hasNodeChanged(changeTree, changedNode, referringNode)){
                    return false;
                }
            }

            // we need to do impact validation for List-Key leaf only when key node is newly created
            if(!isNewNode && referringNode.isKeyLeaf()){
                if (!(REMOVE.equals(changedNode.getEditOperation()) || DELETE.equals(changedNode.getEditOperation()))) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean hasNodeChanged(ChangeTreeNode changeTree, EditContainmentNode changedNode, ReferringNode referringNode) {
        SchemaPath referredSP = referringNode.getReferredSP();
        return hasNodeChanged(changeTree, changedNode, referringNode.getReferredSchemaNode(), referredSP);
    }

    private boolean hasNodeChanged(ChangeTreeNode changeTree, EditContainmentNode changedNode, DataSchemaNode referredSchemaNode, SchemaPath referredSP) {
        Map<ModelNodeId, ChangeTreeNode> nodesOfType = changeTree.getNodesIndex().get(referredSP);
        if (nodesOfType == null) {
            //if node is removed, one will not find the changes of the children
            if (!nodeRemoved(changedNode)) {
                return false;
            }
        } else {
            ChangeTreeNode change = null;

            if (!(referredSchemaNode instanceof DataNodeContainer)) {
                ModelNodeId changeId = new ModelNodeId(changedNode.getModelNodeId());
                QName qName = referredSchemaNode.getQName();
                changeId.addRdn(new ModelNodeRdn(ModelNodeRdn.CONTAINER,
                        qName.getNamespace().toString(), qName.getLocalName()));
                change = nodesOfType.get(changeId);
            } else {
                change = nodesOfType.get(changedNode.getModelNodeId());
            }
            //only if the node has changed, we need to do impact validation
            if (change == null || !change.hasChanged()) {
                return false;
            }
        }
        return true;
    }

    private boolean nodeRemoved(EditContainmentNode changedNode) {
        return DELETE.equals(changedNode.getEditOperation()) || REMOVE.equals(changedNode.getEditOperation());
    }

    private boolean isConstraintNodeParentPartOfChangeSet(Set<SchemaPath> changeNodesSet,
            EditContainmentNode changedNode, DataSchemaNode constraintNode) {
        SchemaPath caseSchemaPath = ChoiceCaseNodeUtil
                .getCaseSchemaNodeFromChildNodeIfApplicable(changedNode.getSchemaRegistry(), constraintNode.getPath());
        CaseSchemaNode caseNode = (CaseSchemaNode) changedNode.getSchemaRegistry().getDataSchemaNode(caseSchemaPath);
        if (caseNode != null) {
            for (DataSchemaNode caseChild : caseNode.getChildNodes()) {
                SchemaPath caseChildSchemaPath = caseChild.getPath();
                if (changeNodesSet.contains(caseChildSchemaPath)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isDeletedList(Map<SchemaPath, Collection<EditContainmentNode>> changeNodeMap, SchemaPath referencePath,
            SchemaRegistry schemaRegistry, DSValidationContext validationContext) {
        boolean result = false;
        TimingLogger.startPhase("isDeletedList");
        ValidatedDeletedList validatedDeletedList = validationContext.getValidatedIsDeletedList();
        boolean isAlreadyValidated = validatedDeletedList.isValidated(referencePath);
        if(isAlreadyValidated) {
            result = validatedDeletedList.getValidationResult(referencePath);
        } else if (changeNodeMap.containsKey(referencePath)) {
            DSValidationMountContext context = SchemaRegistryUtil.getDataSchemaNode(schemaRegistry, referencePath);
            DataSchemaNode schemaNode = context.getDataSchemaNode();
            if (schemaNode instanceof ListSchemaNode) {
                Collection<EditContainmentNode> editNodes = changeNodeMap.get(referencePath);
                for (EditContainmentNode editNode : editNodes) {
                    if (isNodeDeleted(editNode, validationContext)) {
                        result = true;
                        break;
                    }
                }
            } else {
                // is its parent a list and deleted? 
                context = SchemaRegistryUtil.getDataSchemaNode(schemaRegistry, referencePath.getParent());
                schemaNode = context.getDataSchemaNode();
                if (schemaNode instanceof ListSchemaNode) {
                    Collection<EditContainmentNode> editNodes = changeNodeMap.get(referencePath);
                    for (EditContainmentNode editNode : editNodes) {
                        if (isNodeDeleted(editNode, validationContext)) {
                            result = true;
                            break;
                        }
                    }
                }
            }
            validatedDeletedList.storeResult(referencePath, result);
        }
        TimingLogger.endPhase("isDeletedList", false);
        return result;
    }

    private Map<QName, ConfigLeafAttribute> getMatchCriteria(DataSchemaNode schemaNode, EditContainmentNode editNode) {
        if (schemaNode instanceof ListSchemaNode) {
            // if it id a list, build a hashmap of all keys and values from EditContainmentNode
            List<EditMatchNode> matchNodes = editNode.getMatchNodes();
            SchemaRegistry schemaRegistry = editNode.getSchemaRegistry();
            Map<QName, ConfigLeafAttribute> matchCriteria = new HashMap<>();
            for (EditMatchNode matchNode : matchNodes) {
                SchemaPath childPath = schemaRegistry.getDescendantSchemaPath(schemaNode.getPath(), matchNode.getQName());
                LeafSchemaNode childNode = (LeafSchemaNode) schemaRegistry.getDataSchemaNode(childPath);
                ConfigLeafAttribute attribute = ConfigAttributeFactory.getConfigLeafAttribute(schemaRegistry, childNode,
                        matchNode.getValue());
                matchCriteria.put(matchNode.getQName(), attribute);
            }

            return matchCriteria;
        }

        return Collections.emptyMap();
    }

    private void buildAllChangeSchemaPaths(SchemaPath parentPath, EditContainmentNode editTree, Set<SchemaPath> changeNodesPath,
            Map<SchemaPath, Collection<EditContainmentNode>> changeNodeMap,
            SchemaRegistry schemaRegistry, Map<String, Object> mountCurrentScope, DSValidationContext validationContext) throws DataStoreException,
            GetAttributeException {
        // cache if the node is deleted
        cacheDeleteChangNode(editTree, validationContext);

        boolean nodeAdded = false;
        boolean isMountPath = false;
        /**
         * For every changeNode in a EditContainmentNode, record the schemaPath for the change and also the parent edit node.
         * We need the parent Edit node to identify the right list/leaf list model nodes.
         */
        for (EditChangeNode changeNode : editTree.getChangeNodes()) {
            DSValidationMountContext mountContext = DataStoreValidationUtil.populateChildSchemaPath(editTree.getSchemaRegistry(),
                    parentPath,
                    changeNode.getQName());
            schemaRegistry = mountContext.getSchemaRegistry();
            SchemaPath childPath = mountContext.getSchemaPath();
            addToChangeList(editTree, changeNodesPath, changeNodeMap, childPath, validationContext);
            nodeAdded = true;
        }

        for (EditContainmentNode childNode : editTree.getChildren()) {
            DSValidationMountContext mountContext = DataStoreValidationUtil.populateChildSchemaPath(childNode.getSchemaRegistry(),
                    parentPath,
                    childNode.getQName());
            if (!schemaRegistry.equals(mountContext.getSchemaRegistry())) {
                isMountPath = true;
            }
            schemaRegistry = mountContext.getSchemaRegistry();
            if (mountContext.getMountCurrentScope() != null) {
                mountCurrentScope = mountContext.getMountCurrentScope();
            }
            SchemaPath childPath = mountContext.getSchemaPath();
            buildAllChangeSchemaPaths(childPath, childNode, changeNodesPath, changeNodeMap, schemaRegistry, mountCurrentScope, validationContext);
        }

        if (nodeAdded || !editTree.getEditOperation().equalsIgnoreCase(EditConfigOperations.NONE)) {
            if (isMountPath && schemaRegistry.getParentRegistry() != null) { // Resetting schema registry to parent registry (for mount
                // node, global registry need to be taken
                schemaRegistry = schemaRegistry.getParentRegistry();
            }
            // if there is already a node added, we will validate the entire editNode
            // if no node is added so far, if there is an operation on the edit node itself,
            // then add the match node and container 
            for (EditMatchNode matchNode : editTree.getMatchNodes()) {
                DSValidationMountContext mountContext = DataStoreValidationUtil.populateChildSchemaPath(schemaRegistry, parentPath,
                        matchNode.getQName());
                schemaRegistry = mountContext.getSchemaRegistry();
                SchemaPath childPath = mountContext.getSchemaPath();
                addToChangeList(editTree, changeNodesPath, changeNodeMap, childPath, validationContext);
            }

            /**
             * Record the schemaPath of EditContainmentNode. We need to check for the integrity at every container/list 
             * level after the change if this has undergone a change
             */
            DSValidationMountContext mountContext = DataStoreValidationUtil.populateChildSchemaPath(schemaRegistry, parentPath.getParent(),
                    editTree.getQName());
            SchemaPath editNodePath = mountContext.getSchemaPath();
            if (editNodePath != null) {
                addToChangeList(editTree, changeNodesPath, changeNodeMap, editNodePath, validationContext);
            } else if (parentPath.getParent().getLastComponent() == null) {
                // indicates it is a root node
                addToChangeList(editTree, changeNodesPath, changeNodeMap, parentPath, validationContext);
            }
        }
    }

    private Map<String, ModelNode> getFromModelNodeCache(SchemaPath schemaPath, DSValidationContext validationContext) {
        return validationContext.getModelNodes(schemaPath);
    }


    private void addToModelNodeCache(Collection<ModelNode> modelNodes, SchemaPath schemaPath, DSValidationContext validationContext) {
        for (ModelNode modelNode : modelNodes) {
            validationContext.addToModelNodeCache(schemaPath, modelNode);
        }
    }

    private void addToMountPathModelNodeIdCache(SchemaPath schemaPath, Collection<ModelNode> modelNodes, DSValidationContext validationContext) {
        for (ModelNode modelNode : modelNodes) {
            validationContext.addToMountPathModelNodeIdCache(schemaPath, modelNode.getModelNodeId());
        }
    }

    private void addToChangeList(EditContainmentNode editTree, Set<SchemaPath> changeNodesPath, Map<SchemaPath,
            Collection<EditContainmentNode>> changeNodeMap, SchemaPath childPath, DSValidationContext validationContext) {
        changeNodesPath.add(childPath);
        Collection<EditContainmentNode> containmentNodes = changeNodeMap.get(childPath);
        if (containmentNodes == null) {
            containmentNodes = new HashSet<EditContainmentNode>();
            changeNodeMap.put(childPath, containmentNodes);
        }
        containmentNodes.add(editTree);
        if(editTree!= null && editTree.getParent() !=null) {
            validationContext.setChangeNodeParentModelNodeIdMap(childPath, editTree);
        }
    }

    @Override
    public boolean validateChild(ModelNode parentModelNode, DataSchemaNode childSchemaNode, DSValidationContext validationContext) throws ValidationException {
        ValidatedChildren validatedChilds = validationContext.getValidatedChilds();
        boolean isAlreadyValidated = validatedChilds.isValidated(parentModelNode,
                childSchemaNode);
        boolean validation;
        if (!isAlreadyValidated) {
            SchemaRegistry schemaRegistry = m_schemaRegistry;
            if (parentModelNode != null) {
                schemaRegistry = parentModelNode.getSchemaRegistry();
            }
            DataStoreConstraintValidator validator = DataStoreConstraintValidatorFactory.getInstance().getValidator(childSchemaNode,
                    schemaRegistry, m_modelNodeHelperRegistry, m_expValidator);
            if (validator != null && parentModelNode != null) {
                validator.validateLeafRef(parentModelNode, validationContext);
            }
            validation = validateConstraints(parentModelNode, childSchemaNode, validationContext);
            // evaluate type specific validation
            if (validation) {
                if (validator != null && parentModelNode != null) {
                    validator.validate(parentModelNode, validationContext);
                }
            }
            validatedChilds.storeResult(parentModelNode, childSchemaNode, validation);
        } else {
            // we should return the result of already validated child
            validation = validatedChilds.getValidationResult(parentModelNode, childSchemaNode);
        }

        return validation;
    }

    private boolean validateConstraints(ModelNode parentModelNode, DataSchemaNode childSchemaNode, DSValidationContext validationContext){
        ValidatedConstraints validatedConstraints = validationContext.getValidatedConstraints();
        boolean isAlreadyValidated = validatedConstraints.isValidated(parentModelNode,
                childSchemaNode);
        boolean result;
        if(!isAlreadyValidated) {
            //     first augmentation if any has to be validated && evaluate node constraints if any -- must/when
            result = m_augmentOrUsesValidator.evaluate(parentModelNode, childSchemaNode, validationContext) &&
                    m_constraintValidator.evaluate(parentModelNode, childSchemaNode, validationContext);
            validatedConstraints.storeResult(parentModelNode, childSchemaNode, result);
        } else {
            result =  validatedConstraints.getValidationResult(parentModelNode,
                    childSchemaNode);
        }
        return result;
    }

    private void checkForMandatoryNodes(ModelNode parentModelNode, DataSchemaNode childSchemaNode,
            Map<QName, ConfigLeafAttribute> matchCriteria, Collection<SchemaPath> changeNodeSet, Map<SchemaPath,
            Collection<EditContainmentNode>> changeNodeMap, DSValidationContext validationContext) {
        String simplePath = DataStoreValidationUtil.getSimplePath(childSchemaNode);
        try {
            validationContext.setAsMandatoryNodesCheck(true);
            TimingLogger.startConstraint(TimingLogger.ConstraintType.MANDATORY, simplePath);
            if (childSchemaNode instanceof ListSchemaNode) {
                // We only have a matchCriteria and a schemaNode.
                // So we need to fetch the right modelNode that was modified as part of
                // edit request and then validate for missing mandatory nodes

                // Since the modelNode is part of the Validation context, the model node and all impacted children will
                // be already available in DynaBean cache. We have to validate only those that were impacted in this request.
                Collection<ModelNode> modelNodes = DataStoreValidationUtil.getChildListModelNodes(parentModelNode,
                        (ListSchemaNode) childSchemaNode, matchCriteria);
                for (ModelNode modelNode : modelNodes) {
                    boolean match = true;
                    for (Map.Entry<QName, ConfigLeafAttribute> entry : matchCriteria.entrySet()) {
                        ConfigLeafAttribute nodeAttribute = ((ModelNodeWithAttributes) modelNode).getAttribute(entry.getKey());
                        if (!nodeAttribute.equals(entry.getValue())) {
                            match = false;
                            break;
                        }
                    }

                    if (match) {
                        validateMissingChildren((ModelNodeWithAttributes) modelNode, changeNodeSet, changeNodeMap, validationContext);
                        break;
                    }
                }
            } else if (childSchemaNode instanceof ContainerSchemaNode) {
                ModelNode modelNode = DataStoreValidationUtil.getChildContainerModelNode(parentModelNode, childSchemaNode);
                if (modelNode == null && childSchemaNode.getPath().getParent().getLastComponent() == null) {
                    //indicate it ia root node
                    Collection<ModelNode> rootNodes = validationContext.getRootNodesOfType(childSchemaNode.getPath(),
                            parentModelNode.getSchemaRegistry());
                    for (ModelNode rootNode : rootNodes) {
                        if (rootNode.getModelNodeSchemaPath().equals(childSchemaNode.getPath())) {
                            modelNode = rootNode;
                            break;
                        }
                    }

                }
                if (modelNode != null) {
                    validateMissingChildren((ModelNodeWithAttributes) modelNode, changeNodeSet, changeNodeMap, validationContext);
                }
            } else {
                // not a list or container
                if (changeNodeSet.contains(childSchemaNode.getPath())) {
                    SchemaRegistry schemaRegistry = m_schemaRegistry;
                    if (parentModelNode != null) {
                        schemaRegistry = parentModelNode.getSchemaRegistry();
                    }
                    validateMissingChild((ModelNodeWithAttributes) parentModelNode, childSchemaNode, changeNodeSet, changeNodeMap,
                            schemaRegistry, validationContext);
                }

            }
        } catch (ModelNodeGetException e) {
            throw new ValidationException(e);
        } finally {
            TimingLogger.endConstraint(TimingLogger.ConstraintType.MANDATORY, simplePath);
            validationContext.setAsMandatoryNodesCheck(false);
        }
    }

    private boolean isNodeForCreateOrDelete(ModelNode modelNode, DataSchemaNode child, DSValidationContext validationContext) {
        DSValidationNodeIndex createNodes = validationContext.getMergeList();
        DSValidationNodeIndex deleteNodes = validationContext.getDeleteList();
        if (createNodes.presentInTypeIndex(modelNode.getModelNodeSchemaPath(), child.getQName())) {
            return true;
        }
        if (deleteNodes.presentInTypeIndex(modelNode.getModelNodeSchemaPath(), child.getQName())) {
            return true;
        }


        return false;
    }

    private boolean isNonPresenceContainerWithDefaults(ModelNode modelNode) {
        SchemaPath schemaPath = modelNode.getModelNodeSchemaPath();
        DSValidationMountContext context = SchemaRegistryUtil.getDataSchemaNode(modelNode.getSchemaRegistryForCurrentNode(), schemaPath);
        ContainerSchemaNode schemaNode = (ContainerSchemaNode) context.getDataSchemaNode();
        if (!schemaNode.isPresenceContainer()) {
            Collection<DataSchemaNode> children = context.getSchemaRegistry().getChildren(schemaPath);
            for (DataSchemaNode child : children) {
                if (SchemaRegistryUtil.hasDefaults(child)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkValidateMissingChildren(SchemaPath childPath, ModelNodeId parentModelNode, DSValidationContext validationContext) {
        Map<ModelNodeId, EditContainmentNode> parentModelNodeIds = validationContext.getParentModelNodeIdByChangeNodePath(childPath);
        if(parentModelNodeIds != null && !parentModelNodeIds.isEmpty()) {
            EditContainmentNode parentTreeNode = parentModelNodeIds.get(parentModelNode);
            if (parentTreeNode != null && !EditConfigOperations.isOperationDeleteOrRemove(parentTreeNode.getEditOperation())) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public void validateMissingChildren(ModelNodeWithAttributes modelNode, Collection<SchemaPath> changeNodeSet, Map<SchemaPath,
            Collection<EditContainmentNode>> changeNodeMap, DSValidationContext validationContext) throws ModelNodeGetException {
        SchemaPath schemaPath = modelNode.getModelNodeSchemaPath();
        SchemaRegistry schemaRegistry = modelNode.getSchemaRegistryForParent();
        Collection<DataSchemaNode> children = schemaRegistry.getChildren(schemaPath);
        if (schemaRegistry.getDataSchemaNode(schemaPath) == null) {
            if (modelNode != null) {
                schemaRegistry = modelNode.getSchemaRegistry();
                children = schemaRegistry.getChildren(schemaPath);
            }
        }
        for (DataSchemaNode child : children) {
            TimingLogger.startPhase("isNodeForCreateOrDelete");
            boolean nodeForCreateOrDelete = isNodeForCreateOrDelete(modelNode, child, validationContext);
            TimingLogger.endPhase("isNodeForCreateOrDelete", false);
            if (!nodeForCreateOrDelete) {
                // If it is not already identified for create/delete dont validate it, 
                // it will be validated in the next cycle when it is created. 
                // We dont want to validate a delete container for mandatory node

                boolean shouldCheckForMissingChild = true;

                // if child is already exists in changeNodeMap, then dont need to validate it again
                Collection<EditContainmentNode> nodes = changeNodeMap.get(child.getPath());
                if (nodes != null && (child instanceof ContainerSchemaNode || child instanceof ListSchemaNode)) {
                    shouldCheckForMissingChild = checkValidateMissingChildren(child.getPath(), modelNode.getModelNodeId(), validationContext);
                }
                if (shouldCheckForMissingChild) {
                    validateMissingChild(modelNode, child, changeNodeSet, changeNodeMap, schemaRegistry, validationContext);
                }
            }
        }

    }

    @SuppressWarnings("rawtypes")
    protected void validateMissingChild(ModelNodeWithAttributes modelNode, DataSchemaNode child, Collection<SchemaPath> changeNodeSet,Map<SchemaPath, Collection<EditContainmentNode>> changeNodeMap,
            SchemaRegistry schemaRegistry, DSValidationContext validationContext) throws ModelNodeGetException {
        try {
            validationContext.getSchemaPathsToDelete().add(child.getPath());
            QName childQName = child.getQName();
            boolean proxyModelNode = modelNode instanceof ProxyValidationModelNode;
            if (!child.isConfiguration()) {
                return;
            } else if (child instanceof ContainerSchemaNode) {
                ModelNode childModelNode = DataStoreValidationUtil.getChildContainerModelNode(modelNode, child);

                if (((ContainerSchemaNode) child).isPresenceContainer() && childModelNode != null && !proxyModelNode) {
                    validateMissingChildren((ModelNodeWithAttributes) childModelNode, changeNodeSet, changeNodeMap, validationContext);
                } else if (!((ContainerSchemaNode) child).isPresenceContainer()) {
                    if (childModelNode == null) {
                        boolean valid = true;
                        try {
                            valid = validateChild(modelNode, child, validationContext);
                        } catch (ValidationException e) {
                            valid = false;
                            logDebug(null, "Mandatory validation on child {} for modelNode{} will not be processed further. " +
                                    "ProxyModelNode {}",
                                    child, modelNode, proxyModelNode);
                        }

                        // if the container is not created yet and it is not a presence container,
                        // create a validation container and check for any mandatory nodes inside it
                        if (valid) {
                            // it is ok to validate and expect further validation on this non-presence container
                            childModelNode = new ProxyValidationModelNode(modelNode, modelNode.getMountModelNodeHelperRegistry(), child.getPath());
                            validateMissingChildren((ModelNodeWithAttributes) childModelNode, changeNodeSet, changeNodeMap, validationContext);
                        }
                    } else if (isNonPresenceContainerWithDefaults(childModelNode)) {
                        // indicates this is a non presence container with default leafs
                        // and created when the parent container was created. Not necessarily was created with all mandatory nodes
                        validateMissingChildren((ModelNodeWithAttributes) childModelNode, changeNodeSet, changeNodeMap, validationContext);
                    } else {

                        logDebug("modelNode {} already exists. It is already validated when it was last modified",
                                childModelNode.getModelNodeId());
                    }
                }

            } else {
                logDebug("Not a container node so go through validataion for modelNode {} child {}", modelNode.getModelNodeId(),
                        child.getQName());
                boolean hasWhenConstraint = DataStoreValidationUtil.containsWhenConstraint(schemaRegistry, child);
                
                if (child instanceof LeafSchemaNode) {
                    // if it is a leaf node, and constraint is mandatory
                    // if it has a when/must validate and check for its existance
                    if (((LeafSchemaNode) child).isMandatory()) {                        
                        if (hasWhenConstraint) {
                            boolean validateWhen = validateChild(modelNode, child, validationContext);
                            // If mandatory leaf is missing then thrown an exception
                            if (validateWhen && modelNode.getAttribute(childQName) == null) {
                                DataStoreValidationErrors.throwDataMissingException(schemaRegistry, modelNode, childQName);
                            }
                        } else {
                            SchemaPath caseSchemaPath = ChoiceCaseNodeUtil.getCaseSchemaNodeFromChildNodeIfApplicable(schemaRegistry, child.getPath());
                            boolean isExceptionToBeThrown = false;
                            if(caseSchemaPath == null){
                                isExceptionToBeThrown = true;
                            } else if(isCurrentNodeUnderCase(modelNode, schemaRegistry, caseSchemaPath)){
                                isExceptionToBeThrown = true;
                            } else if(isCaseNodeExists(modelNode, schemaRegistry, caseSchemaPath, validationContext)){
                                isExceptionToBeThrown = true;
                            } else if(!isDefaultCaseNodeExists(modelNode, child, schemaRegistry)) {
                                isExceptionToBeThrown = true;
                            }
                            
                            if (modelNode.getAttribute(childQName) == null && isExceptionToBeThrown) {
                                DataStoreValidationErrors.throwDataMissingException(schemaRegistry, modelNode, childQName);
                            }

                        }
                    } else if (((LeafSchemaNode) child).getType().getDefaultValue().isPresent() && !AnvExtensions.IGNORE_DEFAULT.isExtensionIn(child)) {
                        // if this leaf has defaults, is missing and not identified for create
                        // throw exception
                        boolean mustWhen = DataStoreValidationUtil.containsMustWhen(schemaRegistry, child);
                        if (mustWhen && modelNode.getAttribute(childQName) == null) {
                            DSValidationNodeIndex nodesToCreate = validationContext.getMergeList();
                            if (!hasChild(nodesToCreate, modelNode, childQName)) {
                                // in case if the constraints on the node becomes true and it has a default, create it.
                                Collection<SchemaPath> schemaPathsToCreate =
                                        validationContext.getSchemaPathsToCreate();
                                boolean addPathToCreate = true;
                                try {
                                    boolean result = validateChild(modelNode, child, validationContext);
                                    if ((child.isAugmenting() || child.isAddedByUses()) && result) {
                                        recordDefaultNodeAndAddEntryToMerge(modelNode, child, childQName, nodesToCreate, validationContext);
                                    }
                                } catch (ValidationException e) {
                                    logDebug("child {} cannot exists under modelNode {}", child, modelNode);
                                    addPathToCreate = false;
                                }
                                if (addPathToCreate && !child.isAugmenting() && !child.isAddedByUses()) {
                                    schemaPathsToCreate.add(child.getPath());
                                }
                            }
                        } else if (modelNode.getAttribute(childQName) == null) {
                            // if it is case child node, the just skip it, as its choice/case missing nodes would have been validated already
                            SchemaPath caseSP = ChoiceCaseNodeUtil.getCaseSchemaNodeFromChildNodeIfApplicable(schemaRegistry, child.getPath());
                            if(caseSP == null){
                                DSValidationNodeIndex nodesToCreate = validationContext.getMergeList();
                                recordDefaultNodeAndAddEntryToMerge(modelNode, child, childQName, nodesToCreate, validationContext);
                            }
                        }
                    }
                } else if (child instanceof ChoiceSchemaNode) {
                    ChoiceSchemaNode choiceNode = (ChoiceSchemaNode) child;
                    m_choiceCaseValidator.validateMandatoryChoiceChildren(modelNode, choiceNode, validationContext);
                } else {
                    ElementCountConstraint elementCountConstraint = null;
                    if (child instanceof ElementCountConstraintAware) {
                        Optional<ElementCountConstraint> optElementCountConstraint =
                                ((ElementCountConstraintAware) child).getElementCountConstraint();
                        if (optElementCountConstraint.isPresent()) {
                            elementCountConstraint = optElementCountConstraint.get();
                        }
                    }
                    if (elementCountConstraint != null && elementCountConstraint.getMinElements() != null && elementCountConstraint.getMinElements() > 0) {
                        int minElements = elementCountConstraint.getMinElements();
                        if (!hasWhenConstraint) {
                            if (child instanceof LeafListSchemaNode) {
                                Set leafLists = modelNode.getLeafList(childQName);
                                if (leafLists == null || leafLists.isEmpty() || leafLists.size() < minElements) {
                                    SchemaPath caseSchemaPath = ChoiceCaseNodeUtil.getCaseSchemaNodeFromChildNodeIfApplicable(schemaRegistry, child.getPath());
                                    boolean isExceptionToBeThrown = false;
                                    if(caseSchemaPath == null){
                                        isExceptionToBeThrown = true;
                                    } else if(isCurrentNodeUnderCase(modelNode, schemaRegistry, caseSchemaPath)){
                                        isExceptionToBeThrown = true;
                                    } else if(isCaseNodeExists(modelNode, schemaRegistry, caseSchemaPath, validationContext)){
                                        isExceptionToBeThrown = true;
                                    }
                                    if(isExceptionToBeThrown) {
                                        ValidationException exception =
                                                DataStoreValidationErrors.getViolateMinElementException(childQName.getLocalName(),
                                                        minElements);
                                        ModelNodeId errorId = new ModelNodeId(modelNode.getModelNodeId());
                                        errorId.addRdn(ModelNodeRdn.CONTAINER, childQName.getNamespace().toString(), childQName.getLocalName());
                                        exception.getRpcError().setErrorPath(errorId.xPathString(schemaRegistry),
                                                errorId.xPathStringNsByPrefix(schemaRegistry));
                                        throw exception;
                                    }
                                }
                            } else if (child instanceof ListSchemaNode) {
                                Collection listNodes = DataStoreValidationUtil.getChildListModelNodes(modelNode, (ListSchemaNode) child);

                                if (listNodes == null || listNodes.isEmpty() || listNodes.size() < minElements) {
                                    ValidationException exception =
                                            DataStoreValidationErrors.getViolateMinElementException(childQName.getLocalName(),
                                                    elementCountConstraint.getMinElements());
                                    ModelNodeId errorId = new ModelNodeId(modelNode.getModelNodeId());
                                    errorId.addRdn(ModelNodeRdn.CONTAINER, childQName.getNamespace().toString(), childQName.getLocalName());
                                    exception.getRpcError().setErrorPath(errorId.xPathString(schemaRegistry),
                                            errorId.xPathStringNsByPrefix(schemaRegistry));
                                    throw exception;
                                }
                            }
                        } else {
                            if (!proxyModelNode) {
                                boolean validateWhen = validateChild(modelNode, child, validationContext);
                                if (validateWhen && child instanceof LeafListSchemaNode) {
                                    Set leafLists = modelNode.getLeafList(childQName);
                                    // If min-elements leaf list is missing, then thrown an exception
                                    if (leafLists == null || leafLists.isEmpty() || leafLists.size() < minElements) {
                                        ValidationException exception =
                                                DataStoreValidationErrors.getViolateMinElementException(childQName.getLocalName(),
                                                        minElements);
                                        ModelNodeId errorId = new ModelNodeId(modelNode.getModelNodeId());
                                        errorId.addRdn(ModelNodeRdn.CONTAINER, childQName.getNamespace().toString(),
                                                childQName.getLocalName());
                                        exception.getRpcError().setErrorPath(errorId.xPathString(schemaRegistry),
                                                errorId.xPathStringNsByPrefix(schemaRegistry));
                                        throw exception;
                                    }
                                }
                            } else {
                                DataStoreValidationErrors.throwDataMissingException(schemaRegistry, modelNode, childQName);
                            }
                        }
                    }
                }

            }
        } finally {
            validationContext.getSchemaPathsToDelete().clear();
        }
    }

    private boolean isDefaultCaseNodeExists(ModelNodeWithAttributes modelNode, DataSchemaNode child, SchemaRegistry schemaRegistry) {
        SchemaPath caseSP = ChoiceCaseNodeUtil.getCaseSchemaNodeFromChildNodeIfApplicable(schemaRegistry, child.getPath());
        SchemaPath choiceSP = caseSP == null? null : caseSP.getParent();
        if(choiceSP != null) {
            ChoiceSchemaNode choiceNode = (ChoiceSchemaNode) schemaRegistry.getDataSchemaNode(choiceSP);
            if(choiceNode.getDefaultCase().isPresent()) {
                return true;
            }
        }
        return false;
    }

    private boolean isCurrentNodeUnderCase(ModelNodeWithAttributes modelNode, SchemaRegistry schemaRegistry, SchemaPath caseSchemaPath) {
        SchemaPath currentModelNodeCaseSchemaPath = ChoiceCaseNodeUtil.getCaseSchemaNodeFromChildNodeIfApplicable(schemaRegistry, modelNode.getModelNodeSchemaPath());
        if(currentModelNodeCaseSchemaPath != null && currentModelNodeCaseSchemaPath.equals(caseSchemaPath)){
            // current modelNode already part of caseSN 
            return true;
        }
        return false;
    }

    private boolean isCaseNodeExists(ModelNodeWithAttributes modelNode, SchemaRegistry schemaRegistry, SchemaPath caseSchemaPath,
                                     DSValidationContext validationContext) {
        Collection<DataSchemaNode> caseChildNodes = schemaRegistry.getChildren(caseSchemaPath);
        return m_choiceCaseValidator.checkForCasePresence(modelNode, caseChildNodes, validationContext);
    }

    private void recordDefaultNodeAndAddEntryToMerge(ModelNodeWithAttributes modelNode, DataSchemaNode child,
            QName childQName, DSValidationNodeIndex nodesToCreate, DSValidationContext validationContext) {
        Collection<QName> childQNames = nodesToCreate.get(modelNode);
        if (childQNames == null) {
            childQNames = new LinkedHashSet<QName>();
            nodesToCreate.put(modelNode, childQNames);
        }
        childQNames.add(childQName);
        ConfigLeafAttribute configLeafAttribute = child instanceof LeafSchemaNode ?
                ConfigAttributeFactory.getConfigAttributeFromDefaultValue(modelNode.getSchemaRegistry(), (LeafSchemaNode) child) : null;
        validationContext.recordDefaultValue(child.getPath(), modelNode, configLeafAttribute);
    }

    private ModelNode getParentNode(SchemaPath childPath, EditContainmentNode childEditNode, DSValidationContext validationContext) {
        ModelNode returnValue = null;
        DSValidationMountContext context = SchemaRegistryUtil.getDataSchemaNode(childEditNode.getSchemaRegistry(), childPath);
        DataSchemaNode childNode = context.getDataSchemaNode();
        if (childNode == null) {
            return null;
        }
        DataSchemaNode parentNode = SchemaRegistryUtil.getEffectiveParentNode(childNode, childEditNode.getSchemaRegistry());
        if (parentNode != null) {
            Map<String, ModelNode> possibleParentNodes = getFromModelNodeCache(parentNode.getPath(), validationContext);
            if (possibleParentNodes != null && !possibleParentNodes.isEmpty()) {
                EditContainmentNode parentEditNode = null;
                if (childNode instanceof LeafSchemaNode || childNode instanceof LeafListSchemaNode) {
                    parentEditNode = childEditNode;
                } else {
                    parentEditNode = childEditNode.getParent() != null ? childEditNode.getParent() : childEditNode;
                }

                returnValue = possibleParentNodes.get(parentEditNode.getModelNodeId().xPathString());
            }

            if (returnValue == null) {
                if (childNode instanceof LeafSchemaNode || childNode instanceof LeafListSchemaNode) {
                    /**
                     * What do we get here?
                     * container c {
                     *    list a {
                     *      leaf b;
                     *    }
                     * }
                     *
                     * If childPath is 'leaf b', we will have EditContainmentNode of 'list a'.
                     *
                     * So we need to find the ModelNode of 'container c' and then get the ModelNode of 'list a' from 'container c'
                     */
                    SchemaPath parentPath =
                            SchemaRegistryUtil.getEffectiveParentNode(childNode, childEditNode.getSchemaRegistry()).getPath();
                    ModelNode grandParentModelNode = getParentNode(parentPath, childEditNode, validationContext);
                    if (grandParentModelNode != null && grandParentModelNode.getModelNodeId().equals(childEditNode.getParent().getModelNodeId())) {
                        Map<String, ModelNode> modelNodesMap = getFromModelNodeCache(parentPath, validationContext);
                        returnValue = matchModelNode(modelNodesMap, childEditNode);
                        if (returnValue == null) {
                            if (m_modelNodeDSM.isChildTypeBigList(parentPath, grandParentModelNode.getSchemaRegistry())) {
                                ModelNodeKey modelNodeKey = MNKeyUtil.getModelNodeKey(childEditNode.getSchemaRegistry(), parentPath,
                                        childEditNode.getModelNodeId());
                                returnValue = m_modelNodeDSM.findNode(parentPath, modelNodeKey, grandParentModelNode.getModelNodeId(),
                                        grandParentModelNode.getSchemaRegistry());
                            } else {
                                List<ModelNode> modelNodes = new ArrayList<>();
                                modelNodes = m_modelNodeDSM.listChildNodes(parentPath, grandParentModelNode.getModelNodeId(),
                                        grandParentModelNode.getSchemaRegistry());
                                if (modelNodes != null && !modelNodes.isEmpty()) {
                                    addToModelNodeCache(modelNodes, modelNodes.get(0).getModelNodeSchemaPath(), validationContext);
                                }
                                if (modelNodes != null && !modelNodes.isEmpty() && isNodeDeleted(childEditNode, validationContext)) {
                                    // we are here when the actual child is deleted which impacts any other nodes of same type
                                    // instances of same type - yang list. So every such node will have
                                    // same path, lets validate one of them.
                                    returnValue = modelNodes.iterator().next();
                                } else {
                                    modelNodesMap = getFromModelNodeCache(parentPath, validationContext);
                                    returnValue = matchModelNode(modelNodesMap, childEditNode);
                                }
                            }
                        }
                    }

                } else {
                    SchemaPath parentPath = parentNode.getPath();
                    ModelNode parentModelNode = getParentNode(parentPath, childEditNode.getParent(), validationContext);
                    if (parentModelNode != null) {
                        if (!parentModelNode.getModelNodeId().equals(childEditNode.getParent().getModelNodeId())
                                && parentModelNode.getModelNodeId().equals(childEditNode.getParent().getParent().getModelNodeId())) {
                            if (m_modelNodeDSM.isChildTypeBigList(parentPath, parentModelNode.getSchemaRegistry())) {
                                ModelNodeKey modelNodeKey = MNKeyUtil.getModelNodeKey(childEditNode.getSchemaRegistry(), parentPath,
                                        childEditNode.getParent().getModelNodeId());
                                returnValue = m_modelNodeDSM.findNode(parentPath, modelNodeKey, parentModelNode.getModelNodeId(),
                                        parentModelNode.getSchemaRegistry());
                            } else {
                                List<ModelNode> parentModelNodes = m_modelNodeDSM.listChildNodes(parentPath,
                                        parentModelNode.getModelNodeId(), parentModelNode.getSchemaRegistry());
                                returnValue = matchModelNode(parentModelNodes, childEditNode.getParent());
                                if (!parentModelNodes.isEmpty()) {
                                    addToModelNodeCache(parentModelNodes, parentModelNodes.get(0).getModelNodeSchemaPath(), validationContext);
                                    // Cache mountpath and it's modelNode Id\
                                    SchemaRegistry mountRegistry = parentModelNode.getSchemaRegistry();
                                    if (mountRegistry.getMountPath() != null
                                            && mountRegistry.getMountPath().equals(parentModelNodes.get(0).getModelNodeSchemaPath())) {
                                        addToMountPathModelNodeIdCache(parentModelNodes.get(0).getModelNodeSchemaPath(), parentModelNodes
                                                , validationContext);
                                    }
                                }
                            }
                        } else if (parentModelNode.getModelNodeId().equals(childEditNode.getParent().getModelNodeId())) {
                            returnValue = parentModelNode;
                        }
                    }
                }

            }
        }
        return returnValue;

    }

    private ModelNode matchModelNode(Map<String, ModelNode> modelNodes, EditContainmentNode editNode) {
        if (modelNodes != null) {
            ModelNode returnValue = modelNodes.get(editNode.getModelNodeId().xPathString());
            if (returnValue != null) {
                return returnValue;
            }
        }

        return null;
    }

    private ModelNode matchModelNode(List<ModelNode> modelNodes, EditContainmentNode editNode) {
        if (modelNodes != null) {
            for (ModelNode modelNode : modelNodes) {
                if (modelNode.getModelNodeId().xPathString().equals(editNode.getModelNodeId().xPathString())) {
                    return modelNode;
                }
            }
        }
        return null;
    }

    /*
    This method prevents duplicate calls to validate same leaf node under a parent Model node
     */
    private boolean validateChildIfRequired(ModelNode modelNode, DataSchemaNode childSchemaNode, ReferringNode referringNode,
                                            DSValidationContext validationContext) {
        boolean result = false;
        /**
         * Here we should check the cache whether childSchemaNode is already validated or not.
         *
         * If it is not validated already,
         * 		then we should validate the childSchemaNode and cache it along with validation result.
         * Else
         * 		we should get validation result of childSchemaNode from cache and return it.
         */
        if (referringNode.getReferenceType().equals(ReferenceType.LEAFREF)){
            // leafref needs child's type validation, when and must doesn't need it
            result = validateChild(modelNode, childSchemaNode, validationContext);
        } else {
            result = validateConstraints(modelNode, childSchemaNode, validationContext);
        }
        return result;
    }

    @Override
    public DSExpressionValidator getValidator() {
        return m_expValidator;
    }

    //For UT only
    public boolean getValidatedChildCacheHitStatus() {
        return m_validatedChildCacheHitStatus;
    }

    public void setValidatedChildCacheHitStatus(boolean validatedChildCacheHitStatus) {
        this.m_validatedChildCacheHitStatus = validatedChildCacheHitStatus;
    }
}
