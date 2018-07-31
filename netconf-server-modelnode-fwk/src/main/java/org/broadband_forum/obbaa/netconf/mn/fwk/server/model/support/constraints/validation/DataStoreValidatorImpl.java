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
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationUtil
        .NC_DS_VALIDATION;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.jxpath.ri.compiler.Expression;
import org.apache.commons.jxpath.ri.compiler.LocationPath;
import org.apache.commons.jxpath.ri.compiler.Step;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildContainerHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigAttributeFactory;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeGetException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DSExpressionValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildListHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeDynaBean;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.service.DSAugmentValidation;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.service.DSConstraintValidation;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ConstraintDefinition;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.ChoiceEffectiveStatementImpl;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigOperations;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil;
import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.AnvExtensions;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditChangeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditMatchNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.DataStoreException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregator;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationErrors;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationPathBuilder;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.LoggerFactory;

public class DataStoreValidatorImpl implements DataStoreValidator {
    private final SchemaRegistry m_schemaRegistry;
    private final ModelNodeHelperRegistry m_modelNodeHelperRegistry;
    private final ModelNodeDataStoreManager m_modelNodeDSM;
    private final DataStoreIntegrityService m_integrityService;
    private final ChoiceMandatoryChildrenValidator m_choiceCaseValidator;
    private final DSExpressionValidator m_expValidator;
    private final DataStoreValidationPathBuilder m_dsPathTraverser;
    private static final AdvancedLogger LOGGER = LoggerFactory.getLogger(DataStoreValidatorImpl.class,
            NC_DS_VALIDATION, "DEBUG",
            "GLOBAL");
    private final DSValidation m_augmentValidator;
    private final DSValidation m_constraintValidator;

    private void logDebug(String message, Object... objects) {
        Boolean isDebugEnabled = (Boolean) RequestScope.getCurrentScope().getFromCache(DataStoreValidatorImpl.class
                .getName());
        if (isDebugEnabled == null) {
            isDebugEnabled = LOGGER.isDebugEnabled();
            RequestScope.getCurrentScope().putInCache(DataStoreValidatorImpl.class.getName(), isDebugEnabled);
        }

        if (isDebugEnabled) {
            LOGGER.debug(message, objects);
        }
    }

    public DataStoreValidatorImpl(SchemaRegistry schemaRegistry, ModelNodeHelperRegistry modelNodeHelperRegistry,
                                  ModelNodeDataStoreManager modelNodeDSM, DataStoreIntegrityService integrityService,
                                  DSExpressionValidator expValidator) {
        m_schemaRegistry = schemaRegistry;
        m_modelNodeHelperRegistry = modelNodeHelperRegistry;
        m_modelNodeDSM = modelNodeDSM;
        m_integrityService = integrityService;
        m_expValidator = expValidator;
        m_choiceCaseValidator = new ChoiceMandatoryChildrenValidator(m_schemaRegistry, m_modelNodeHelperRegistry, this);
        m_dsPathTraverser = new DataStoreValidationPathBuilder(m_schemaRegistry, m_modelNodeHelperRegistry);
        m_augmentValidator = new DSAugmentValidation(expValidator, schemaRegistry);
        m_constraintValidator = new DSConstraintValidation(expValidator);
    }

    private void initializeCache() {
        DataStoreValidationUtil.resetValidationContext();
    }

    private void addToDeletedCache(EditContainmentNode node, QName attribute) {
        String modelNodeId = node.getModelNodeId().pathString();
        if (attribute != null) {
            // Indicates only leaf is deleted
            getValidationContext().recrodDeletedChangeAttribute(modelNodeId, attribute);
        } else {
            // indicates the entire edit containment is deleted
            getValidationContext().recordDeletedChangeNode(modelNodeId, node);
        }
    }

    private boolean isNodeDeleted(EditContainmentNode changedNode) {
        EditContainmentNode node = getValidationContext().getDeletedNode(changedNode.getModelNodeId().pathString());
        return node == null ? false : true;
    }

    private boolean isNodeDeleted(EditContainmentNode changedNode, DataSchemaNode changeNode) {
        if (isNodeDeleted(changedNode)) {
            return true;
        }
        QName qname = getValidationContext().getDeletedAttribute(changedNode.getModelNodeId().pathString());
        return qname == null ? false : true;
    }

    private void cacheDeleteChangNode(EditContainmentNode changedNode) {
        ModelNodeId id = changedNode.getModelNodeId();

        if (getValidationContext().getDeletedNode(id.pathString()) == null) {
            // if the node is not already identified as deleted.
            EditContainmentNode parentEditContainement = changedNode.getParent();
            if (parentEditContainement != null) {
                if (getValidationContext().getDeletedNode(parentEditContainement.getModelNodeId().pathString()) !=
                        null) {
                    // if its parent node is deleted, so is the child
                    addToDeletedCache(changedNode, null);
                    return;
                }
            }

            String operation = changedNode.getEditOperation();
            if (operation.equals(EditConfigOperations.DELETE) || operation.equals(EditConfigOperations.REMOVE)) {
                // if the node is deleted/removed add to cache
                addToDeletedCache(changedNode, null);
                return;
            }

            for (EditChangeNode changeNode : changedNode.getChangeNodes()) {
                operation = changeNode.getOperation();
                if (operation.equals(EditConfigOperations.DELETE) || operation.equals(EditConfigOperations.REMOVE)) {
                    // add the attribute that is deleted/removed
                    addToDeletedCache(changedNode, changeNode.getQName());
                }
            }
        }
    }

    @Override
    public List<Notification> validate(RootModelNodeAggregator rootModelNodes, EditContainmentNode editTree,
                                       EditConfigRequest request,
                                       NetconfClientInfo clientInfo) throws ValidationException {
        List<SchemaPath> changeNodes = new ArrayList<SchemaPath>();
        Map<SchemaPath, Collection<EditContainmentNode>> changeNodeMap = new HashMap<SchemaPath,
                Collection<EditContainmentNode>>();
        List<ModelNode> rootNodes = rootModelNodes.getModelServiceRoots();
        ModelNode rootModelNode = null;
        SchemaPath rootSchemaPath = null;


        try {
            initializeCache();
            getValidationContext().cacheRootNodes(rootNodes);
            for (ModelNode modelNode : rootNodes) {
                /*
	             * every editTree will start from a root node. Find the right root node for the edit tree and
	             * validate
	             */
                if (editTree.getQName().equals(modelNode.getQName())) {
                    rootSchemaPath = modelNode.getModelNodeSchemaPath();
                    rootModelNode = modelNode;
                    getValidationContext().addToModelNodeCache(rootSchemaPath, rootModelNode);
                    break;
                }
            }

            buildAllChangeSchemaPaths(rootSchemaPath, editTree, changeNodes, changeNodeMap);
            validateChanges(editTree, changeNodes, changeNodeMap, rootModelNode);
            checkForNonPresenceContainerDeletion();
            return m_integrityService.createOrDeleteNodes(request, clientInfo);
        } catch (GetAttributeException | ModelNodeGetException e) {
            throw new ValidationException(e);
        }
    }

    private void checkForNonPresenceContainerDeletion() throws ModelNodeGetException {
        Map<ModelNode, Collection<QName>> deleteNodes = new HashMap<ModelNode, Collection<QName>>
                (getValidationContext().getDeleteList());

        for (Map.Entry<ModelNode, Collection<QName>> deleteList : deleteNodes.entrySet()) {
            if (deleteList.getKey() instanceof ModelNodeWithAttributes) {
                for (QName qnameInList : deleteList.getValue()) {
                    checkForDeletion(deleteList.getKey(), qnameInList);
                }
            }
        }

    }

    private boolean hasChild(Map<ModelNode, Collection<QName>> nodeMap, ModelNode parentNode, QName childName) {
        Collection<QName> childList = nodeMap.get(parentNode);
        if (childList != null) {
            return childList.contains(childName);
        }
        return false;
    }

    private void addChild(Map<ModelNode, Collection<QName>> nodeMap, ModelNode parentNode, QName childName) {
        Collection<QName> childList = nodeMap.get(parentNode);
        if (childList == null) {
            childList = new HashSet<QName>();
            nodeMap.put(parentNode, childList);
        }
        childList.add(childName);
    }

    private void checkForDeletion(ModelNode node, QName childName) throws ModelNodeGetException {
        if (node.getParent() == null) {
            // delete/remove on root node not allowed
            return;
        }

        if (node instanceof ModelNodeWithAttributes) {
            if (hasNoOtherChildren((ModelNodeWithAttributes) node, childName)) {
                ModelNode parentNode = node.getParent();
                if (parentNode != null) {
                    if (!getValidationContext().isNodeForCreate(node)) {
                        getValidationContext().removeFromDelete(node);
                        getValidationContext().removeFromMerge(node);
                        getValidationContext().recordForDelete(parentNode, node.getQName());
                        checkForDeletion(parentNode, node.getQName());
                    }
                }
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private boolean hasNoOtherChildren(ModelNodeWithAttributes modelNode, QName exception) throws
            ModelNodeGetException {
        SchemaPath schemaPath = modelNode.getModelNodeSchemaPath();
        DataSchemaNode schemaNode = m_schemaRegistry.getDataSchemaNode(schemaPath);
        // check if it is a container
        if (!(schemaNode instanceof ContainerSchemaNode)) {
            return false;
        }

        //check if it is a presence container
        if (((ContainerSchemaNode) schemaNode).isPresenceContainer()) {
            return false;
        }

        SchemaPath childSchemaPath = m_schemaRegistry.getDescendantSchemaPath(schemaPath, exception);
        DataSchemaNode childSchemaNode = m_schemaRegistry.getDataSchemaNode(childSchemaPath);

        Map leafLists = modelNode.getLeafLists();
        Map attributes = modelNode.getAttributes();
        // check if there are more than 1 leafList then return false
        // if there is only one leafList and if it is not exception, return false
        if (leafLists.size() > 1) {
            return false;
        } else if (leafLists.size() == 1 && leafLists.get(exception) == null && !(childSchemaNode instanceof
                LeafListSchemaNode)) {
            return false;
        }

        // if there are more than one attribute return false
        // if there is only one attribute and it is not exception, return false
        if (attributes.size() > 1) {
            return false;
        } else if (attributes.size() == 1 && attributes.get(exception) == null && !(childSchemaNode instanceof
                LeafSchemaNode)) {
            return false;
        }

        //if there are any childContainers and that is not exception return false
        Map<QName, ChildContainerHelper> containers = m_modelNodeHelperRegistry.getChildContainerHelpers(schemaPath);
        for (Map.Entry<QName, ChildContainerHelper> helper : containers.entrySet()) {
            if (!helper.getKey().equals(exception) && helper.getValue() != null && helper.getValue().getValue
                    (modelNode) != null) {
                return false;
            }
        }

        //if there are any childList and that is not exception, return false
        Map<QName, ChildListHelper> lists = m_modelNodeHelperRegistry.getChildListHelpers(schemaPath);
        for (Map.Entry<QName, ChildListHelper> helper : lists.entrySet()) {
            if (!helper.getKey().equals(exception) && helper.getValue() != null && !helper.getValue().getValue
                    (modelNode, Collections.emptyMap()).isEmpty()) {
                return false;
            }
        }

        // if there are any state nodes, return false
        Collection<DataSchemaNode> children = m_schemaRegistry.getChildren(schemaPath);
        for (DataSchemaNode child : children) {
            if (!child.isConfiguration()) {
                return false;
            }
        }
        return true;
    }

    private List<ModelNode> getParentWithAccessPath(DynaBean parentBean, LocationPath accessPath, List<ModelNode>
            parentNodes, boolean nodeNotDeleted) {
        Step[] accessPathSteps = accessPath.getSteps();
        if (accessPath.isAbsolute()) {
            DataSchemaNode rootSchemaNode = DataStoreValidationUtil.getRootSchemaNode(m_schemaRegistry,
                    accessPathSteps[0]);
            if (rootSchemaNode != null) {
                parentBean = DataStoreValidationUtil.getRootModelNode(m_schemaRegistry, rootSchemaNode);
                if (parentBean == null) {
                    // impact node does not exists. return
                    return parentNodes;
                }

                Step[] newAccessPathSteps = new Step[accessPathSteps.length - 1];
                System.arraycopy(accessPathSteps, 1, newAccessPathSteps, 0, newAccessPathSteps.length);
                accessPathSteps = newAccessPathSteps;
            }
        }
        parentNodes = m_dsPathTraverser.getParentModelNodeWithAccessPath(parentBean, accessPathSteps, nodeNotDeleted);
        return parentNodes;
    }

    private void validateImpactedNode(ModelNode rootModelNode, ModelNode parentNode, EditContainmentNode
            changedEditNode,
                                      DataSchemaNode changeSchemaNode, SchemaPath referencedSchemaPath, LocationPath
                                              accessPath, boolean nodeNotDeleted) {
        List<ModelNode> parentNodes = new ArrayList<ModelNode>();
        Step[] accessPathSteps = accessPath == null ? null : accessPath.getSteps();
        if (parentNode == null) {
            parentNodes = getParentModelNode(changeSchemaNode, changedEditNode, referencedSchemaPath, accessPathSteps);
        } else {
            if (accessPath == null || accessPath.getSteps().length == 0) {
                accessPath = buildAccessPath(referencedSchemaPath);
                accessPathSteps = accessPath.getSteps();
            }
            DynaBean parentBean = (DynaBean) parentNode.getValue();
            DataSchemaNode referenceNode = m_schemaRegistry.getDataSchemaNode(referencedSchemaPath);
            parentNodes = getParentWithAccessPath(parentBean, accessPath, parentNodes, nodeNotDeleted);
            if (parentNodes.isEmpty()) {
                if (SchemaRegistryUtil.hasDefaults(referenceNode)) {
                    // might be a parent Node missing for a when condition leaf inside a non-presence container.
                    parentNodes = m_dsPathTraverser.getMissingParentNodeWithAccessPath(parentBean,
                            accessPathSteps, nodeNotDeleted);
                } else if (referenceNode instanceof ContainerSchemaNode
                        && DataStoreValidationUtil.containerHasDefaultLeafs(m_schemaRegistry, (ContainerSchemaNode)
                        referenceNode)) {
                    // might be a parent Node missing for a when condition container with default leafs. 
                    parentNodes = m_dsPathTraverser.getMissingParentNodeWithAccessPath(parentBean,
                            accessPathSteps, nodeNotDeleted);
                }
            }
        }

        if (parentNodes.isEmpty() && accessPath != null) {
            //could be abs path
            // if this is a abs access path, then it does not matter if the node is deleted or not, since the access
            // path is from root
            parentNodes = getParentWithAccessPath((DynaBean) rootModelNode.getValue(), accessPath, parentNodes, true);
        }
        DataSchemaNode schemaNode = m_schemaRegistry.getDataSchemaNode(referencedSchemaPath);
        for (ModelNode parentModelNode : parentNodes) {
            if (isParent(parentModelNode, referencedSchemaPath)) {
                validateChild(parentModelNode, schemaNode);
            }
        }


    }

    private boolean isParent(ModelNode modelNode, SchemaPath childSchemaPath) {
		/*
		 * This method checks whether the childSchemaPath is present in the
		 * modelNode or not. Basically this check is needed so that
		 * childSchemaPath is validated under the correct parentModelNode
		 *
		 */
        DynaBean dynaBean = (DynaBean) modelNode.getValue();
        String localName = childSchemaPath.getLastComponent().getLocalName();

        DynaProperty childProperty = dynaBean.getDynaClass().getDynaProperty(localName);
        if (childProperty != null) {
            {
                Object children = dynaBean.get(localName);
                if (children != null) {
                    if (children instanceof ModelNodeDynaBean) {
                        ModelNode childNode = (ModelNode) ((DynaBean) children).get(ModelNodeWithAttributes.MODEL_NODE);
                        if (!(childNode.getModelNodeSchemaPath().equals(childSchemaPath))) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }


    private List<ModelNode> getParentModelNode(DataSchemaNode changeSchemaNode, EditContainmentNode changeEditNode,
                                               SchemaPath referencePath, Step[] accessPathSteps) {
        List<ModelNode> returnValue = new ArrayList<ModelNode>();
        boolean newAccessPath = false;
        if (accessPathSteps == null || accessPathSteps.length == 0) {
            accessPathSteps = buildAccessPath(referencePath).getSteps();
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
             * if nextNode == null && deleteNode is true --> indicates the modelNode is deleted and we need to skip
             * the step and proceed
             * if nextNode == null && deleteNode is false, we might have hit the end node.
             * 		if schemaNode is not null here (indicating we have not exhausted the steps and could not find the
             * 		node) find
             *      the modelNode with the remaning steps in accessPath and the current schemaNode
             */
            if (nextNode != null && changeSchemaNode != null) {
                DataSchemaNode parentSchemaNode = m_schemaRegistry.getDataSchemaNode(changeSchemaNode.getPath()
                        .getParent());
                returnValue.addAll(getParentModelNode(parentSchemaNode, nextNode, referencePath, steps));
            } else if (deleteNode && nextNode == null && changeSchemaNode != null) {
                // could be a attribute in a deleted parent node, that is not part of the change
                steps = new Step[accessPathSteps.length - 1];
                System.arraycopy(accessPathSteps, 1, steps, 0, steps.length);
                DataSchemaNode parentSchemaNode = m_schemaRegistry.getDataSchemaNode(changeSchemaNode.getPath()
                        .getParent());
                returnValue.addAll(getParentModelNode(parentSchemaNode, changeEditNode, referencePath, steps));
            } else if (changeSchemaNode != null) {
                ModelNode parentModelNode = getParentNode(changeSchemaNode.getPath(), changeEditNode);
                if (parentModelNode != null) {
                    returnValue.addAll(m_dsPathTraverser.getParentModelNodeWithAccessPath((DynaBean) parentModelNode
                            .getValue(), accessPathSteps, true));
                }
            } else {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Reached the rootNode therefore returning the existing ModelNode list as " +
                            "changeSchemaNode will be null now");
                }
            }

        }
        return returnValue;
    }

    private LocationPath buildAccessPath(SchemaPath referencePath) {
        LocationPath accessPath;
        List<Step> inputPath = new LinkedList<Step>();
        DataStoreValidationUtil.buildAbsAccessPath(m_schemaRegistry, m_schemaRegistry.getDataSchemaNode
                (referencePath), inputPath);
        accessPath = new LocationPath(true, inputPath.toArray(new Step[0]));
        return accessPath;
    }

    private void checkForDuplicateLeafList(EditContainmentNode editNode, DataSchemaNode leafListSchemaNode) {
        Set<EditChangeNode> changeNodes = new HashSet<EditChangeNode>();
        int addCount = 0;
        QName leafListQName = leafListSchemaNode.getQName();
        for (EditChangeNode changeNode : editNode.getChangeNodes()) {
            if (changeNode.getQName().equals(leafListSchemaNode.getQName())) {
                changeNodes.add(changeNode);
                addCount++;
            }
        }

        if (addCount != changeNodes.size()) {

            ValidationException exception = DataStoreValidationErrors.getUniqueConstraintException("duplicate " +
                    "elements in leaf-list " + leafListQName);
            NetconfRpcError error = exception.getRpcError();
            ModelNodeId modelNodeId = new ModelNodeId(editNode.getModelNodeId());
            modelNodeId.addRdn(ModelNodeRdn.CONTAINER, leafListQName.getNamespace().toString(), leafListQName
                    .getLocalName());
            error.setErrorPath(modelNodeId.xPathString(m_schemaRegistry), modelNodeId.xPathStringNsByPrefix
                    (m_schemaRegistry));
            throw exception;
        }
    }

    /**
     * Checking whether duplicate key is present in the list. If duplicate key is present then throw
     * UniqueConstraintException
     *
     * @param editNode
     * @param listSchemaNode
     */
    private void checkForDuplicateList(EditContainmentNode editNode, DataSchemaNode listSchemaNode) {
        Set<List<EditMatchNode>> uniqueMatchedNode = new HashSet<>();
        EditContainmentNode parentNode = editNode.getParent();
        if (parentNode != null) {
            //Iterate each children and check if there is any duplicate key value
            for (EditContainmentNode childNode : parentNode.getChildren()) {
                if (childNode.getQName().equals(editNode.getQName())) {
                    if (!uniqueMatchedNode.contains(childNode.getMatchNodes())) {
                        uniqueMatchedNode.add(childNode.getMatchNodes());
                    } else {
                        ValidationException exception = DataStoreValidationErrors
                                .getUniqueConstraintException("duplicate elements in list " + childNode.getQName());
                        NetconfRpcError error = exception.getRpcError();
                        ModelNodeId modelNodeId = new ModelNodeId(childNode.getModelNodeId());
                        error.setErrorPath(modelNodeId.xPathString(m_schemaRegistry),
                                modelNodeId.xPathStringNsByPrefix(m_schemaRegistry));
                        throw exception;
                    }
                }
            }
        }
    }

    private void validateChanges(EditContainmentNode editTree, List<SchemaPath> changeNodes,
                                 Map<SchemaPath, Collection<EditContainmentNode>> changeNodeMap, ModelNode
                                         rootModelNode)
            throws ValidationException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Validating changes for {}", editTree);
        }
        Set<SchemaPath> changeNodesSet = new HashSet<SchemaPath>(changeNodes);
        Collection<SchemaPath> schemaPathToDelete = getValidationContext().getSchemaPathsToDelete();
        for (SchemaPath changePath : changeNodes) {
            DataSchemaNode changeSchemaNode = m_schemaRegistry.getDataSchemaNode(changePath);
            int nodesToCreateCount = getValidationContext().nodesToCreateCount();
            Set<SchemaPath> uniqueSchemapaths = new HashSet<SchemaPath>();
            for (EditContainmentNode changedNode : changeNodeMap.get(changeSchemaNode.getPath())) {
                // check for duplicate leaf-list
                if (changeSchemaNode instanceof LeafListSchemaNode) {
                    checkForDuplicateLeafList(changedNode, changeSchemaNode);
                }
                // check for duplicate key in list
                if (changeSchemaNode instanceof ListSchemaNode && !uniqueSchemapaths.contains(changeSchemaNode
                        .getPath())) {
                    checkForDuplicateList(changedNode, changeSchemaNode);
                    uniqueSchemapaths.add(changeSchemaNode.getPath());
                }
                // for each new expression evaluation, reset the single step count
                RequestScope.getCurrentScope().putInCache(DataStoreValidationUtil.SINGLE_STEP_COUNT, null);
                boolean nodeNotDeleted = true;
                ModelNode parentNode = null;
                SchemaPath childPath = changeSchemaNode.getPath();
                parentNode = getParentNode(childPath, changedNode);
                if (isNodeDeleted(changedNode, changeSchemaNode)) {
                    nodeNotDeleted = false;
                }


                if (parentNode == null) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("parentNode is null for dataSchemaNode:{} and path:{}. is node not deleted?:{}",
                                changeSchemaNode, changePath, nodeNotDeleted);
                    }
                    if (changeSchemaNode.getQName().equals(rootModelNode.getQName())) {
                        parentNode = rootModelNode;
                    }
                }

                if (nodeNotDeleted) {
				    /*
				     * we dont want to validate a deleted node. 
				     */
                    validateChild(parentNode, changeSchemaNode);
                }
                parentNode = validateImpact(changeNodeMap, rootModelNode, changeNodesSet, schemaPathToDelete,
                        changePath, changeSchemaNode,
                        nodesToCreateCount, changedNode, nodeNotDeleted, parentNode);

                if (!isNodeDeleted(changedNode)) {
                    if (parentNode == null) {
                        parentNode = rootModelNode;
                    }
                    // after validating all impact nodes for creation/deletion check for mandatory node existence
                    checkForMandatoryNodes(parentNode, changeSchemaNode, getMatchCriteria(changeSchemaNode,
                            changedNode), changeNodesSet);
                }

            }

        }
    }

    protected ModelNode validateImpact(Map<SchemaPath, Collection<EditContainmentNode>> changeNodeMap, ModelNode
            rootModelNode,
                                       Set<SchemaPath> changeNodesSet, Collection<SchemaPath> schemaPathToDelete,
                                       SchemaPath changePath,
                                       DataSchemaNode changeSchemaNode, int nodesToCreateCount, EditContainmentNode
                                               changedNode, boolean nodeNotDeleted,
                                       ModelNode parentNode) {
        /**
         * validate all the referenced nodes that are impacted by
         * the changeNode
         */
        Map<SchemaPath, Expression> impactedPaths = m_schemaRegistry.getReferencedNodesForSchemaPaths(changePath);
        if (schemaPathToDelete != null) {
            schemaPathToDelete.addAll(impactedPaths.keySet());
        }
        for (Map.Entry<SchemaPath, Expression> impactedPath : impactedPaths.entrySet()) {
            boolean validate = false;
            SchemaPath key = impactedPath.getKey();
            boolean containsImpactPath = changeNodesSet.contains(key);
            if (containsImpactPath && isDeletedList(changeNodeMap, key)) {
                // one of the instances of the list is deleted, not necessarily all
                validate = true;
            } else if (!containsImpactPath) {
                validate = true;
            }
            if (validate) {
                if (parentNode == null) {
                    parentNode = getParentNode(changeSchemaNode.getPath(), changedNode);
                }
                validateImpactedNode(rootModelNode, parentNode, changedNode, changeSchemaNode, key,
                        (LocationPath) impactedPath.getValue(), nodeNotDeleted);
                if (nodesToCreateCount < getValidationContext().nodesToCreateCount()) {
                    addMergeNode(parentNode, changeSchemaNode, changedNode);
                }
            }
        }
        schemaPathToDelete.clear();
        return parentNode;
    }


    private boolean isDeletedList(Map<SchemaPath, Collection<EditContainmentNode>> changeNodeMap, SchemaPath
            referencePath) {
        if (changeNodeMap.containsKey(referencePath)) {
            DataSchemaNode schemaNode = m_schemaRegistry.getDataSchemaNode(referencePath);
            if (schemaNode instanceof ListSchemaNode) {
                Collection<EditContainmentNode> editNodes = changeNodeMap.get(referencePath);
                for (EditContainmentNode editNode : editNodes) {
                    if (!isNodeDeleted(editNode)) {
                        return false;
                    }
                }
                return true;
            } else {
                // is its parent a list and deleted? 
                schemaNode = m_schemaRegistry.getDataSchemaNode(referencePath.getParent());
                if (schemaNode instanceof ListSchemaNode) {
                    Collection<EditContainmentNode> editNodes = changeNodeMap.get(referencePath);
                    for (EditContainmentNode editNode : editNodes) {
                        if (!isNodeDeleted(editNode)) {
                            return false;
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private Map<QName, ConfigLeafAttribute> getMatchCriteria(DataSchemaNode schemaNode, EditContainmentNode editNode) {
        if (schemaNode instanceof ListSchemaNode) {
            // if it id a list, build a hashmap of all keys and values from EditContainmentNode
            List<EditMatchNode> matchNodes = editNode.getMatchNodes();
            Map<QName, ConfigLeafAttribute> matchCriteria = new HashMap<>();
            for (EditMatchNode matchNode : matchNodes) {
                SchemaPath childPath = m_schemaRegistry.getDescendantSchemaPath(schemaNode.getPath(), matchNode
                        .getQName());
                LeafSchemaNode childNode = (LeafSchemaNode) m_schemaRegistry.getDataSchemaNode(childPath);
                ConfigLeafAttribute attribute = ConfigAttributeFactory.getConfigLeafAttribute(m_schemaRegistry,
                        childNode, matchNode.getValue());
                matchCriteria.put(matchNode.getQName(), attribute);
            }

            return matchCriteria;
        }

        return Collections.emptyMap();
    }

    private void addMergeNode(ModelNode parentNode, DataSchemaNode changeSchemaNode, EditContainmentNode changedNode) {

        Map<ModelNode, Collection<QName>> nodesToMerge = getValidationContext().getMergeList();
        Map<SchemaPath, Object> defaultValues = getValidationContext().getDefaultValues();
        if (nodesToMerge != null) {
            boolean found = false;
            for (EditChangeNode change : changedNode.getChangeNodes()) {
                if (change.getQName().equals(changeSchemaNode.getQName())) {
                    addChild(nodesToMerge, parentNode, change.getQName());
                    SchemaPath childPath = new SchemaPathBuilder().withParent(parentNode.getModelNodeSchemaPath())
                            .appendQName(change.getQName()).build();
                    defaultValues.put(childPath, change.getValue());
                    found = true;
                    break;
                }
            }

            if (!found) {
                for (EditMatchNode match : changedNode.getMatchNodes()) {
                    if (match.getQName().equals(changeSchemaNode.getQName())) {
                        addChild(nodesToMerge, parentNode, match.getQName());
                        SchemaPath childPath = new SchemaPathBuilder().withParent(parentNode.getModelNodeSchemaPath()
                        ).appendQName(match.getQName()).build();
                        defaultValues.put(childPath, match.getValue());
                        found = true;
                        break;
                    }

                }
            }
        }
    }

    private void buildAllChangeSchemaPaths(SchemaPath parentPath, EditContainmentNode editTree, List<SchemaPath>
            changeNodesPath,
                                           Map<SchemaPath, Collection<EditContainmentNode>> changeNodeMap) throws
            DataStoreException, GetAttributeException {
        // cache if the node is deleted
        cacheDeleteChangNode(editTree);

        boolean nodeAdded = false;
        /**
         * For every changeNode in a EditContainmentNode, record the schemaPath for the change and also the parent
         * edit node.
         * We need the parent Edit node to identify the right list/leaf list model nodes.
         */
        for (EditChangeNode changeNode : editTree.getChangeNodes()) {
            SchemaPath childPath = m_schemaRegistry.getDescendantSchemaPath(parentPath, changeNode.getQName());
            addToChangeList(editTree, changeNodesPath, changeNodeMap, childPath);
            nodeAdded = true;
        }

        for (EditContainmentNode childNode : editTree.getChildren()) {
            SchemaPath childPath = m_schemaRegistry.getDescendantSchemaPath(parentPath, childNode.getQName());
            buildAllChangeSchemaPaths(childPath, childNode, changeNodesPath, changeNodeMap);
        }

        if (nodeAdded || !editTree.getEditOperation().equalsIgnoreCase(EditConfigOperations.NONE)) {
            // if there is already a node added, we will validate the entire editNode
            // if no node is added so far, if there is an operation on the edit node itself,
            // then add the match node and container 
            for (EditMatchNode matchNode : editTree.getMatchNodes()) {
                SchemaPath childPath = m_schemaRegistry.getDescendantSchemaPath(parentPath, matchNode.getQName());
                addToChangeList(editTree, changeNodesPath, changeNodeMap, childPath);
            }

            /**
             * Record the schemaPath of EditContainmentNode. We need to check for the integrity at every container/list 
             * level after the change if this has undergone a change
             */
            SchemaPath editNodePath = m_schemaRegistry.getDescendantSchemaPath(parentPath.getParent(), editTree
                    .getQName());
            if (editNodePath != null) {
                addToChangeList(editTree, changeNodesPath, changeNodeMap, editNodePath);
            } else if (parentPath.getParent().getLastComponent() == null) {
                // indicates it is a root node
                addToChangeList(editTree, changeNodesPath, changeNodeMap, parentPath);
            }
        }


    }

    private Map<String, ModelNode> getFromModelNodeCache(SchemaPath schemaPath) {
        return getValidationContext().getModelNodes(schemaPath);
    }


    private void addToModelNodeCache(Collection<ModelNode> modelNodes, SchemaPath schemaPath) {
        for (ModelNode modelNode : modelNodes) {
            getValidationContext().addToModelNodeCache(schemaPath, modelNode);
        }
    }

    private void addToChangeList(EditContainmentNode editTree, List<SchemaPath> changeNodesPath,
                                 Map<SchemaPath, Collection<EditContainmentNode>> changeNodeMap, SchemaPath childPath) {
        changeNodesPath.add(childPath);
        Collection<EditContainmentNode> containmentNodes = changeNodeMap.get(childPath);
        if (containmentNodes == null) {
            containmentNodes = new HashSet<EditContainmentNode>();
            changeNodeMap.put(childPath, containmentNodes);
        }
        containmentNodes.add(editTree);
    }

    @Override
    public boolean validateChild(ModelNode parentModelNode, DataSchemaNode childSchemaNode) throws ValidationException {
        boolean validation = true;
        // first augmentation if any has to be validated
        validation = m_augmentValidator.evaluate(parentModelNode, childSchemaNode);
        if (validation) {
            // evaluate node constraints if any -- must/when
            validation = m_constraintValidator.evaluate(parentModelNode, childSchemaNode);

            // evaluate type specific validation
            if (validation) {
                DataStoreConstraintValidator validator = DataStoreConstraintValidatorFactory.getInstance()
                        .getValidator(childSchemaNode,
                        m_schemaRegistry, m_modelNodeHelperRegistry, m_expValidator);
                if (validator != null && parentModelNode != null) {
                    validator.validate(parentModelNode);
                }
            }
        }
        return validation;
    }

    private void checkForMandatoryNodes(ModelNode parentModelNode, DataSchemaNode childSchemaNode, Map<QName,
            ConfigLeafAttribute> matchCriteria, Collection<SchemaPath> changeNodeSet) {
        try {
            if (childSchemaNode instanceof ListSchemaNode) {
                // We only have a matchCriteria and a schemaNode.
                // So we need to fetch the right modelNode that was modified as part of
                // edit request and then validate for missing mandatory nodes

                // Since the modelNode is part of the Validation context, the model node and all impacted children will
                // be already available in DynaBean cache. We have to validate only those that were impacted in this
                // request.
                Collection<ModelNode> modelNodes = DataStoreValidationUtil.getChildListModelNodes(parentModelNode,
                        childSchemaNode, m_modelNodeHelperRegistry, matchCriteria);
                for (ModelNode modelNode : modelNodes) {
                    boolean match = true;
                    for (Map.Entry<QName, ConfigLeafAttribute> entry : matchCriteria.entrySet()) {
                        ConfigLeafAttribute nodeAttribute = ((ModelNodeWithAttributes) modelNode).getAttribute(entry
                                .getKey());
                        if (!nodeAttribute.equals(entry.getValue())) {
                            match = false;
                            break;
                        }
                    }

                    if (match) {
                        validateMissingChildren((ModelNodeWithAttributes) modelNode, changeNodeSet);
                        break;
                    }
                }
            } else if (childSchemaNode instanceof ContainerSchemaNode) {
                ModelNode modelNode = DataStoreValidationUtil.getChildContainerModelNode(parentModelNode,
                        childSchemaNode, m_modelNodeHelperRegistry);
                if (modelNode == null && childSchemaNode.getPath().getParent().getLastComponent() == null) {
                    //indicate it ia root node
                    Collection<ModelNode> rootNodes = getValidationContext().getRootNodes();
                    for (ModelNode rootNode : rootNodes) {
                        if (rootNode.getModelNodeSchemaPath().equals(childSchemaNode.getPath())) {
                            modelNode = rootNode;
                            break;
                        }
                    }

                }
                validateMissingChildren((ModelNodeWithAttributes) modelNode, changeNodeSet);
            } else {
                // not a list or container
                if (changeNodeSet.contains(childSchemaNode.getPath())) {
                    validateMissingChild((ModelNodeWithAttributes) parentModelNode, childSchemaNode, changeNodeSet);
                }

            }
        } catch (ModelNodeGetException e) {
            throw new ValidationException(e);
        }
    }

    private boolean isNodeForCreateOrDelete(ModelNode modelNode, DataSchemaNode child) {
        Map<ModelNode, Collection<QName>> createNodes = getValidationContext().getCreateList();
        Map<ModelNode, Collection<QName>> deleteNodes = getValidationContext().getDeleteList();

        for (Map.Entry<ModelNode, Collection<QName>> createEntry : createNodes.entrySet()) {
            ModelNode entryModelNode = createEntry.getKey();
            if (entryModelNode.getModelNodeId().xPathString().equals(modelNode.getModelNodeId().xPathString())
                    || entryModelNode.getModelNodeSchemaPath().equals(child.getPath())) {
                for (QName qnameInList : createEntry.getValue()) {
                    if (qnameInList.equals(child.getQName())) {
                        return true;
                    } else if (qnameInList.equals(modelNode.getQName())) {
                        return true;
                    }
                }
            }
        }

        for (Map.Entry<ModelNode, Collection<QName>> deleteEntry : deleteNodes.entrySet()) {
            ModelNode entryModelNode = deleteEntry.getKey();
            if (entryModelNode.getModelNodeId().xPathString().equals(modelNode.getModelNodeId().xPathString())
                    || entryModelNode.getModelNodeSchemaPath().equals(child.getPath())) {
                for (QName qnameInList : deleteEntry.getValue()) {
                    if (qnameInList.equals(child.getQName())) {
                        return true;
                    } else if (qnameInList.equals(modelNode.getQName())) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private boolean isNonPresenceContainerWithDefaults(ModelNode modelNode) {
        SchemaPath schemaPath = modelNode.getModelNodeSchemaPath();
        ContainerSchemaNode schemaNode = (ContainerSchemaNode) m_schemaRegistry.getDataSchemaNode(schemaPath);
        if (!schemaNode.isPresenceContainer()) {
            Collection<DataSchemaNode> children = m_schemaRegistry.getChildren(schemaPath);
            for (DataSchemaNode child : children) {
                if (SchemaRegistryUtil.hasDefaults(child)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void validateMissingChildren(ModelNodeWithAttributes modelNode, Collection<SchemaPath> changeNodeSet)
            throws ModelNodeGetException {
        SchemaPath schemaPath = modelNode.getModelNodeSchemaPath();
        Collection<DataSchemaNode> children = m_schemaRegistry.getChildren(schemaPath);
        for (DataSchemaNode child : children) {
            if (!isNodeForCreateOrDelete(modelNode, child)) {
                // If it is not already identified for create/delete dont validate it, 
                // it will be validated in the next cycle when it is created. 
                // We dont want to validate a delete container for mandatory node

                boolean shouldCheckForMissingChild = true;
                if ((child instanceof ContainerSchemaNode || child instanceof ListSchemaNode) && changeNodeSet
                        .contains(child.getPath())) {
                    shouldCheckForMissingChild = false;
                }

                if (shouldCheckForMissingChild) {
                    validateMissingChild(modelNode, child, changeNodeSet);
                }
            }
        }

    }

    @SuppressWarnings("rawtypes")
    protected void validateMissingChild(ModelNodeWithAttributes modelNode, DataSchemaNode child,
                                        Collection<SchemaPath> changeNodeSet) throws ModelNodeGetException {
        QName childQName = child.getQName();
        boolean proxyModelNode = modelNode instanceof ProxyValidationModelNode;
        if (!child.isConfiguration()) {
            return;
        } else if (child instanceof ContainerSchemaNode) {
            ModelNode childModelNode = DataStoreValidationUtil.getChildContainerModelNode(modelNode, child,
                    m_modelNodeHelperRegistry);

            if (((ContainerSchemaNode) child).isPresenceContainer() && childModelNode != null && !proxyModelNode) {
                validateMissingChildren((ModelNodeWithAttributes) childModelNode, changeNodeSet);
            } else if (!((ContainerSchemaNode) child).isPresenceContainer()) {
                if (childModelNode == null) {
                    boolean valid = true;
                    try {
                        valid = validateChild(modelNode, child);
                    } catch (ValidationException e) {
                        valid = false;
                        logDebug(null, "Mandatory validation on child {} for modelNode{} will not be processed " +
                                        "further. ProxyModelNode {}",
                                child, modelNode, proxyModelNode);
                    }

                    // if the container is not created yet and it is not a presence container,
                    // create a validation container and check for any mandatory nodes inside it
                    if (valid) {
                        // it is ok to validate and expect further validation on this non-presence container
                        childModelNode = new ProxyValidationModelNode(modelNode, m_modelNodeHelperRegistry, child
                                .getPath());
                        validateMissingChildren((ModelNodeWithAttributes) childModelNode, changeNodeSet);
                    }
                } else if (isNonPresenceContainerWithDefaults(childModelNode)) {
                    // indicates this is a non presence container with default leafs
                    // and created when the parent container was created. Not necessarily was created with all
                    // mandatory nodes
                    validateMissingChildren((ModelNodeWithAttributes) childModelNode, changeNodeSet);
                } else {

                    logDebug("modelNode {} already exists. It is already validated when it was last modified",
                            childModelNode.getModelNodeId());
                }
            }

        } else {
            logDebug("Not a container node so go through validataion for modelNode {} child {}", modelNode
                            .getModelNodeId(),
                    child.getQName());
            ConstraintDefinition constraint = child.getConstraints();
            boolean mustWhen = DataStoreValidationUtil.containsMustWhen(m_schemaRegistry, child);

            if (child instanceof LeafSchemaNode) {
                // if it is a leaf node, and constraint is mandatory
                // if it has a when/must validate and check for its existance
                if (constraint != null && constraint.isMandatory()) {
                    if (mustWhen) {
                        validateChild(modelNode, child);
                    } else {
                        if (modelNode.getAttribute(childQName) == null) {
                            DataStoreValidationErrors.throwDataMissingException(m_schemaRegistry, modelNode,
                                    childQName);
                        }
                    }
                } else if (((LeafSchemaNode) child).getDefault() != null && !AnvExtensions.IGNORE_DEFAULT
                        .isExtensionIn(child)) {
                    // if this leaf has defaults, is missing and not identified for create
                    // throw exception
                    if (mustWhen && modelNode.getAttribute(childQName) == null) {
                        Map<ModelNode, Collection<QName>> nodesToCreate = getValidationContext().getCreateList();
                        if (!hasChild(nodesToCreate, modelNode, childQName)) {
                            // in case if the constraints on the node becomes true and it has a default, create it.
                            Collection<SchemaPath> schemaPathsToCreate = DataStoreValidationUtil.getValidationContext
                                    ().getSchemaPathsToCreate();
                            schemaPathsToCreate.add(child.getPath());
                            validateChild(modelNode, child);
                        }
                    } else if (modelNode.getAttribute(childQName) == null) {

                        Map<ModelNode, Collection<QName>> nodesToCreate = getValidationContext().getCreateList();
                        if (!hasChild(nodesToCreate, modelNode, childQName)) {
                            Map<SchemaPath, Object> defaultValues = DataStoreValidationUtil.getValidationContext()
                                    .getDefaultValues();
                            Collection<QName> childQNames = nodesToCreate.get(modelNode);
                            if (childQNames == null) {
                                childQNames = new LinkedHashSet<QName>();
                                nodesToCreate.put(modelNode, childQNames);
                            }
                            childQNames.add(childQName);
                            defaultValues.put(child.getPath(), ((LeafSchemaNode) child).getDefault());
                        }
                    }
                }
            } else if (child instanceof ChoiceEffectiveStatementImpl) {
                m_choiceCaseValidator.validateMandatoryChoiceChildren(modelNode, (ChoiceEffectiveStatementImpl) child);
            } else {
                if (constraint != null && constraint.getMinElements() != null && constraint.getMinElements() > 0) {
                    int minElements = constraint.getMinElements();
                    if (!mustWhen) {
                        if (child instanceof LeafListSchemaNode) {
                            Set leafLists = modelNode.getLeafList(childQName);
                            if (leafLists == null || leafLists.isEmpty() || leafLists.size() < minElements) {
                                ValidationException exception = DataStoreValidationErrors
                                        .getViolateMinElementException(childQName.getLocalName(), minElements);
                                ModelNodeId errorId = new ModelNodeId(modelNode.getModelNodeId());
                                errorId.addRdn(ModelNodeRdn.CONTAINER, childQName.getNamespace().toString(),
                                        childQName.getLocalName());
                                exception.getRpcError().setErrorPath(errorId.xPathString(m_schemaRegistry), errorId
                                        .xPathStringNsByPrefix(m_schemaRegistry));
                                throw exception;
                            }
                        } else if (child instanceof ListSchemaNode) {
                            Collection listNodes = DataStoreValidationUtil.getChildListModelNodes(modelNode, child,
                                    m_modelNodeHelperRegistry);

                            if (listNodes == null || listNodes.isEmpty() || listNodes.size() < minElements) {
                                ValidationException exception = DataStoreValidationErrors
                                        .getViolateMinElementException(childQName.getLocalName(), constraint
                                                .getMinElements());
                                ModelNodeId errorId = new ModelNodeId(modelNode.getModelNodeId());
                                errorId.addRdn(ModelNodeRdn.CONTAINER, childQName.getNamespace().toString(),
                                        childQName.getLocalName());
                                exception.getRpcError().setErrorPath(errorId.xPathString(m_schemaRegistry), errorId
                                        .xPathStringNsByPrefix(m_schemaRegistry));
                                throw exception;
                            }
                        }
                    } else {
                        if (!proxyModelNode) {
                            validateChild(modelNode, child);
                        } else {
                            DataStoreValidationErrors.throwDataMissingException(m_schemaRegistry, modelNode,
                                    childQName);
                        }
                    }
                }
            }

        }
    }

    private ModelNode getParentNode(SchemaPath childPath, EditContainmentNode childEditNode) {
        ModelNode returnValue = null;

        DataSchemaNode childNode = m_schemaRegistry.getDataSchemaNode(childPath);
        DataSchemaNode parentNode = SchemaRegistryUtil.getEffectiveParentNode(childNode, m_schemaRegistry);
        if (parentNode != null) {
            Map<String, ModelNode> possibleParentNodes = getFromModelNodeCache(parentNode.getPath());
            if (possibleParentNodes != null && !possibleParentNodes.isEmpty()) {
                EditContainmentNode parentEditNode = null;
                if (childNode instanceof LeafSchemaNode || childNode instanceof LeafListSchemaNode) {
                    parentEditNode = childEditNode;
                } else {
                    parentEditNode = childEditNode.getParent();
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
                     * So we need to find the ModelNode of 'container c' and then get the ModelNode of 'list a' from
                     * 'container c'
                     */
                    SchemaPath parentPath = SchemaRegistryUtil.getEffectiveParentNode(childNode, m_schemaRegistry)
                            .getPath();
                    ModelNode grandParentModelNode = getParentNode(parentPath, childEditNode);
                    if (grandParentModelNode != null && grandParentModelNode.getModelNodeId().xPathString().equals
                            (childEditNode.getParent().getModelNodeId().xPathString())) {
                        Map<String, ModelNode> modelNodesMap = getFromModelNodeCache(parentPath);
                        returnValue = matchModelNode(modelNodesMap, childEditNode);
                        if (returnValue == null) {
                            List<ModelNode> modelNodes = m_modelNodeDSM.listChildNodes(parentPath,
                                    grandParentModelNode.getModelNodeId());
                            if (modelNodes != null && !modelNodes.isEmpty()) {
                                addToModelNodeCache(modelNodes, modelNodes.get(0).getModelNodeSchemaPath());
                            }
                            if (modelNodes != null && !modelNodes.isEmpty() && isNodeDeleted(childEditNode)) {
                                // we are here when the actual child is deleted which impacts any other nodes of same type
                                // instances of same type - yang list. So every such node will have
                                // same path, lets validate one of them.
                                returnValue = modelNodes.iterator().next();
                            } else {
                                modelNodesMap = getFromModelNodeCache(parentPath);
                                returnValue = matchModelNode(modelNodesMap, childEditNode);
                            }
                            if (returnValue != null) {
                                addToModelNodeCache(modelNodes, modelNodes.iterator().next().getModelNodeSchemaPath());
                            }
                        }
                    }

                } else {
                    SchemaPath parentPath = parentNode.getPath();
                    ModelNode parentModelNode = getParentNode(parentPath, childEditNode.getParent());
                    if (parentModelNode != null) {
                        if (!parentModelNode.getModelNodeId().xPathString().equals(childEditNode.getParent().getModelNodeId().xPathString())
                                && parentModelNode.getModelNodeId().xPathString().equals(childEditNode.getParent().getParent().getModelNodeId().xPathString())) {
                            List<ModelNode> parentModelNodes = m_modelNodeDSM.listChildNodes(parentPath, parentModelNode.getModelNodeId());
                            returnValue = matchModelNode(parentModelNodes, childEditNode.getParent());
                            if (!parentModelNodes.isEmpty()) {
                                addToModelNodeCache(parentModelNodes, parentModelNodes.get(0).getModelNodeSchemaPath());
                            }
                        } else if (parentModelNode.getModelNodeId().xPathString().equals(childEditNode.getParent().getModelNodeId().xPathString())) {
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


    @Override
    public DSExpressionValidator getValidator() {
        return m_expValidator;
    }

    private DSValidationContext getValidationContext() {
        return DataStoreValidationUtil.getValidationContext();
    }

}
