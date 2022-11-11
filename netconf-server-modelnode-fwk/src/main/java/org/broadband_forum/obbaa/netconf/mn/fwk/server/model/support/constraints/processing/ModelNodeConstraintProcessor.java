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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.processing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.api.messages.EditConfigOperations;
import org.broadband_forum.obbaa.netconf.api.messages.InsertOperation;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorType;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.ChoiceCaseNodeUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditChangeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditConfigException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditMatchNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeKey;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildContainerHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildLeafListHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildListHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigAttributeHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConstraintHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeDeleteException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeGetException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.SetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.EMNKeyUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.MNKeyUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.NetconfRpcErrorUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

/**
 * This class helps to check the constraints during processing of NETCONF operation.
 * The following cases should be detected:
 * 1. Delete requests for non-existent data
 * 2. Create request for existent data
 * 3. Insert request with "before" or "after" parameters that do not exist.
 * 4. Create data node under a "choice", any existing nodes should be deleted.
 * 
 * Created by ntdiemtrang on 03/11/16.
 */

public class ModelNodeConstraintProcessor {

    public static final String DATA_DOES_NOT_EXIST = "Data does not exist";

    public static final String MODEL_NODE_NOT_FOUND = "Model node not found : ";

    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(ModelNodeConstraintProcessor.class, LogAppNames.NETCONF_STACK);

    public static ModelNode getChildNode(ConstraintHelper helper, ModelNode node,
                                         EditContainmentNode editNode) throws EditConfigException {
        ModelNode value = null;
        try {
            if (helper instanceof ChildContainerHelper) {
                ChildContainerHelper containerHelper = (ChildContainerHelper) helper;
                value = containerHelper.getValue(node);
                if (value == null) {
                    throw new EditConfigException(NetconfRpcErrorUtil.getNetconfRpcErrorForModelNode(node, NetconfRpcErrorTag.DATA_MISSING,
                            MODEL_NODE_NOT_FOUND + editNode.getQName().getLocalName()));
                }
                return value;
            } else if (helper instanceof ChildListHelper) {
                ChildListHelper listHelper = (ChildListHelper) helper;
                Map<QName, ConfigLeafAttribute> matchCriteria = getMatchCriteria(editNode);
                Collection<ModelNode> children = listHelper.getValue(node, matchCriteria);
                if (children.isEmpty()) {
                    SchemaPath childModelNodeSchemaPath = listHelper.getChildModelNodeSchemaPath();
                    SchemaRegistry schemaRegistry = node.getSchemaRegistry();
                    ModelNodeKey modelNodeKey = MNKeyUtil.getKeyFromCriteria(childModelNodeSchemaPath, matchCriteria, schemaRegistry);
                    ModelNodeId parentid = node.getModelNodeId();
                    ModelNodeId modelNodeId = EMNKeyUtil.getModelNodeId(modelNodeKey,parentid, childModelNodeSchemaPath);
                    String xpath = modelNodeId.xPathString(schemaRegistry);
                    NetconfRpcError netconfRpcErrorForModelNode = NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.DATA_MISSING,
                            DATA_DOES_NOT_EXIST);
                    if (modelNodeId != null) {
                        netconfRpcErrorForModelNode.setErrorPath(xpath, modelNodeId.xPathStringNsByPrefix(schemaRegistry));
                    }
                    throw new EditConfigException(netconfRpcErrorForModelNode);
                } else {
                    return children.iterator().next();
                }
            } else {
                // An invalid node
                throw new EditConfigException(NetconfRpcErrorUtil.getNetconfRpcErrorForModelNode(node, NetconfRpcErrorTag.DATA_MISSING, "Invalid edit node :" + editNode));
            }
        } catch (ModelNodeGetException e) {
            LOGGER.debug("Could not get child containers ", e);
            EditConfigException exception = new EditConfigException(
                    NetconfRpcErrorUtil.getNetconfRpcErrorForModelNode(node, NetconfRpcErrorTag.DATA_MISSING, "Could not get child containers " + e.getMessage()));
            exception.addSuppressed(e);
            throw exception;
        }
    }

    public static Map<QName, ConfigLeafAttribute> getMatchCriteria(EditContainmentNode editNode) {
        Map<QName, ConfigLeafAttribute> matchCriteria = new HashMap<>();
        for (EditMatchNode matchNode : editNode.getMatchNodes()) {
            matchCriteria.put(matchNode.getQName(), matchNode.getConfigLeafAttribute());
        }
        return matchCriteria;
    }

    public static void validateExistentContainer(ChildContainerHelper helper, ModelNode node, EditContainmentNode editNode)
            throws EditConfigException {
        try {
            if (helper.getValue(node) != null) {
                NetconfRpcError error = getInstanceAlreadyExistsError(node, null, editNode.getQName().getLocalName());
                appendNameAndUpdateErrorPath(node, editNode, error);
                throw new EditConfigException(error);
            }
        } catch (ModelNodeGetException e) {
            LOGGER.debug("Could not get child containers ", e);
            EditConfigException exception = new EditConfigException(
                    NetconfRpcErrorUtil.getNetconfRpcErrorForModelNode(node, NetconfRpcErrorTag.DATA_MISSING, "Could not get child containers " + e.getMessage()));
            exception.addSuppressed(e);
            throw exception;
        }
    }

	private static void appendNameAndUpdateErrorPath(ModelNode node,
			EditContainmentNode editNode, NetconfRpcError error) {
		String existingErrorPath = error.getErrorPath();
		String editNodeNS = editNode.getQName().getNamespace().toString();
		String editNodePrefix = node.getSchemaRegistry().getPrefix(editNodeNS);
		
		if (editNodePrefix != null) {
			String errorPath = existingErrorPath + "/" + editNodePrefix + ":" + editNode.getQName().getLocalName();			
			error.setErrorPath(errorPath, Collections.singletonMap(editNodePrefix, editNodeNS));
		} else {
			String errorPath = existingErrorPath + "/" + editNode.getQName().getLocalName();
			error.setErrorPath(errorPath, Collections.emptyMap());
		}
	}

    public static void validateExistentList(ChildListHelper helper, ModelNode node,
                                            EditContainmentNode editNode) throws EditConfigException {
        if (helper != null) {
            try {
                Map<QName, ConfigLeafAttribute> matchCriteria = getMatchCriteria(editNode);
                Collection<ModelNode> children = helper.getValue(node, matchCriteria);
                if (children != null && children.size() > 0) {
                    ModelNode child = children.iterator().next();
                    NetconfRpcError error = getInstanceAlreadyExistsError(node, child, null);
                    throw new EditConfigException(error);
                }
            } catch (ModelNodeGetException e) {
                LOGGER.debug("Could not get child containers ", e);
                EditConfigException exception = new EditConfigException(
                        NetconfRpcErrorUtil.getNetconfRpcErrorForModelNode(node, NetconfRpcErrorTag.DATA_MISSING, "Could not get child containers " + e.getMessage()));
                exception.addSuppressed(e);
                throw exception;
            }
        } else {
            // An invalid node
            throw new EditConfigException(NetconfRpcErrorUtil.getNetconfRpcErrorForModelNode(node, NetconfRpcErrorTag.INVALID_VALUE, "Invalid edit node :" + editNode));
        }
    }

    private static NetconfRpcError getInstanceAlreadyExistsError(ModelNode node, ModelNode child, String containerName) {
    	ModelNode errorNode = node;
        String name = null;
        if (child != null) {
            name = child.getQName().getLocalName();
            errorNode = child;
        } else if (containerName != null) {
            name = containerName;
        } else {
            name = node.getQName().getLocalName();
        }
        return NetconfRpcErrorUtil.getNetconfRpcErrorForModelNode(errorNode, NetconfRpcErrorTag.DATA_EXISTS,
                "'" + name + "' already exists. Create request Failed.");
    }

    public static Map<QName, Object> handleChoiceCaseNode(SchemaRegistry schemaRegistry, Map<QName, ? extends ConstraintHelper> helpers,
            ModelNode modelNode, QName qName) throws GetAttributeException, ModelNodeDeleteException {
        Map<QName, Object> oldValues = new HashMap<>();
        SchemaPath descendantPath = schemaRegistry.getDescendantSchemaPath(modelNode.getModelNodeSchemaPath(), qName);
        if (descendantPath != null) {
            SchemaPath schemaPath = descendantPath.getParent();
            if (schemaPath != null) {
                Set<CaseSchemaNode> choiceCaseNodes = ChoiceCaseNodeUtil.checkIsCaseNodeAndReturnAllOtherCases(schemaRegistry, schemaPath);
                if (choiceCaseNodes != null && !choiceCaseNodes.isEmpty()) {
                    List<DataSchemaNode> schemaNodes = ChoiceCaseNodeUtil.getAllNodesFromCases(choiceCaseNodes);
                    for (DataSchemaNode dataSchemaNode : schemaNodes) {
                        ConstraintHelper helper = helpers.get(dataSchemaNode.getQName());
                        if (helper != null) {
                            if (helper instanceof ConfigAttributeHelper) {
                                ConfigAttributeHelper configAttributeHelper = (ConfigAttributeHelper) helper;
                                ConfigLeafAttribute oldValue = configAttributeHelper.getValue(modelNode);
                                if (oldValue != null) {
                                    configAttributeHelper.removeAttribute(modelNode);
                                    oldValues.put(dataSchemaNode.getQName(), oldValue);
                                }
                            } else if (helper instanceof ChildLeafListHelper) {
                                ChildLeafListHelper childLeafListHelper = (ChildLeafListHelper) helper;
                                Collection<Object> oldValue = new ArrayList<>(childLeafListHelper.getValue(modelNode));
                                if (!oldValue.isEmpty()) {
                                    childLeafListHelper.removeAllChild(modelNode);
                                    oldValues.put(dataSchemaNode.getQName(), oldValue);
                                }
                            }
                        }
                    }
                }
            }
        }
        return oldValues;
    }

    public static void validateInsertRequest(EditChangeNode changeNode, String leafListOperation,
                                             InsertOperation insertOperation, ModelNode parentModelNode,
                                             SchemaRegistry schemaRegistry, ChildLeafListHelper helper) throws GetAttributeException, SetAttributeException {
        Set<ConfigLeafAttribute> existedValues = new HashSet<>();

        ModelNode reloadedParentNode = parentModelNode.getModelNodeDSM().findNode(parentModelNode.getModelNodeSchemaPath(), MNKeyUtil.getModelNodeKey(parentModelNode, schemaRegistry),
                EMNKeyUtil.getParentId(schemaRegistry, parentModelNode.getModelNodeSchemaPath(), parentModelNode.getModelNodeId()), parentModelNode.getSchemaRegistry());

        existedValues.addAll(helper.getValue(reloadedParentNode));

        if (insertOperation.getName().equals(InsertOperation.AFTER) || insertOperation.getName().equals(InsertOperation.BEFORE)) {
            boolean isLeafListNodeExists = false;
            Iterator<ConfigLeafAttribute> iterator = existedValues.iterator();
            while (iterator.hasNext()){
                ConfigLeafAttribute configLeafAttribute = iterator.next();
                if(configLeafAttribute!=null && configLeafAttribute.getStringValue().equals(insertOperation.getValue())){
                    isLeafListNodeExists = true;
                    break;
                }
            }
        	if (!isLeafListNodeExists) {
                // throw a rfc error data-existed
                LOGGER.debug("The insert value attribute doesn't exist in leaf-list elements: {}" , insertOperation.getValue());
                NetconfRpcError rpcError = NetconfRpcError.getBadAttributeError(NetconfResources.VALUE, NetconfRpcErrorType.Application,
                        String.format("The value attribute '%s' doesn't exist.", insertOperation.getValue()));
                rpcError.setErrorAppTag(NetconfRpcErrorTag.DATA_MISSING.value());
                throw new SetAttributeException(rpcError);
            }

            if (leafListOperation.equals(EditConfigOperations.MERGE) || leafListOperation.equals(EditConfigOperations.REPLACE)) {
                if (insertOperation.getValue().equals(changeNode.getValue())) {
                    LOGGER.debug("The insert value attribute '{}' can't be the same as value",insertOperation.getValue());
                    throw new SetAttributeException(
                            "The insert value attribute '" + insertOperation.getValue() + "' can't be the same as value");
                }
            }
        }
    }

}
