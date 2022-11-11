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

import static org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil.getListSchemaNode;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.utils.ParseKeyValuePredicateUtil.fetchKeyValuePairs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.api.IndexedList;
import org.broadband_forum.obbaa.netconf.api.messages.InsertOperation;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder;
import org.broadband_forum.obbaa.netconf.mn.fwk.ChangeTreeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.ChangeTreeNode.ChangeType;
import org.broadband_forum.obbaa.netconf.mn.fwk.ChangeTreeNodeImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.WritableChangeTreeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditChangeSource;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditMatchNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildListHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigAttributeFactory;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigAttributeHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttributeWithInsertOp;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.InvalidIdentityRefException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeGetException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.XmlModelNodeImpl;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

/**
 * Created by keshava on 5/6/16.
 */
public class CommandUtils {

    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(CommandUtils.class, LogAppNames.NETCONF_STACK);
    public static final String CONTEXT_MAP_INVISIBLE_NODES = "visibility";

    public static ModelNode getExistingNodeBasedOnInsertOperation(EditContainmentNode childNode, ChildListHelper childListHelper,
                                                                  ModelNode parentNode, ModelNodeHelperRegistry modelNodeHelperRegistry, SchemaRegistry schemaRegistry) {
        InsertOperation insertOperation = childNode.getInsertOperation();
        if (InsertOperation.AFTER.equals(insertOperation.getName()) || InsertOperation.BEFORE.equals(insertOperation.getName())) {
            return getExistingNode(childNode, childListHelper, parentNode, modelNodeHelperRegistry, schemaRegistry);
        }
        return null;
    }

    public static ModelNode getExistingNode(EditContainmentNode editContainmentNode, ChildListHelper childListHelper, ModelNode parentNode,
                                            ModelNodeHelperRegistry modelNodeHelperRegistry, SchemaRegistry schemaRegistry) {
        Map<QName, ConfigLeafAttribute> keyPredicates = getKeyPredicates(editContainmentNode, schemaRegistry, parentNode.getModelNodeSchemaPath());
        ModelNode existingNode = null;
        int countMatchKeys = 0;
        try {
            Collection<ModelNode> currentListChilds = childListHelper.getValue(parentNode, keyPredicates);
            for (ModelNode child : currentListChilds) {
                Map<QName, ConfigAttributeHelper> naturalKeyHelpers = modelNodeHelperRegistry.getNaturalKeyHelpers(child.getModelNodeSchemaPath());
                for (QName key : modelNodeHelperRegistry.getNaturalKeyHelpers(child.getModelNodeSchemaPath()).keySet()) {
                    for (QName keyQName : keyPredicates.keySet()) {
                        if (key.equals(keyQName)) {
                            ConfigLeafAttribute keyPredicateValue = keyPredicates.get(keyQName);
                            if (keyPredicateValue.equals(modelNodeHelperRegistry.getNaturalKeyHelper(child.getModelNodeSchemaPath(), key)
                                    .getValue(child))) {
                                countMatchKeys++;
                            }
                        }
                    }
                }
                if (countMatchKeys == naturalKeyHelpers.size()) {
                    existingNode = child;
                    break;
                }
                countMatchKeys = 0; // reset
            }
        } catch (GetAttributeException e) {
            ModelNodeGetException exception = new ModelNodeGetException("could not get value from ModelNode." + e.getMessage(), e);
            throw exception;
        }

        return existingNode;
    }

    public static void setPreviousLeafValues(WritableChangeTreeNode parentCTN, ModelNodeId parentMNID, ModelNode childModelNode, EditChangeSource editChangeSource) {
        if (childModelNode != null) {
            Map<QName, ConfigLeafAttribute> previousLeafValues = childModelNode.getAttributes();
            if (previousLeafValues != null && !previousLeafValues.isEmpty()) {
                for (Map.Entry<QName, ConfigLeafAttribute> entry : previousLeafValues.entrySet()) {
                    ModelNodeId modelNodeId = new ModelNodeId(parentMNID);
                    QName leafQName = entry.getKey();
                    ModelNodeId leafMNID = modelNodeId.addRdn(new ModelNodeRdn(ModelNodeRdn.CONTAINER, leafQName.getNamespace().toString(), leafQName.getLocalName()));
                    WritableChangeTreeNode leafChangeTreeNode = (WritableChangeTreeNode) parentCTN.getChildren().get(leafMNID);
                    if (leafChangeTreeNode == null) {
                        SchemaRegistry schemaRegistry = childModelNode.getSchemaRegistry();
                        ModelNodeHelperRegistry modelNodeHelperRegistry = childModelNode.getMountModelNodeHelperRegistry();
                        SchemaPath childSchemaPath = childModelNode.getModelNodeSchemaPath();
                        ConfigAttributeHelper configAttributeHelper = modelNodeHelperRegistry.getConfigAttributeHelper(childSchemaPath,leafQName);
                        if (configAttributeHelper != null && configAttributeHelper.getLeafSchemaNode() != null) {
                            WritableChangeTreeNode leafChange = new ChangeTreeNodeImpl(schemaRegistry, parentCTN, leafMNID, configAttributeHelper.getLeafSchemaNode(),
                                    parentCTN.getNodesIndex(), parentCTN.getAttributeIndex(), parentCTN.getChangedNodeTypes(), parentCTN.getContextMap(), parentCTN.getNodesOfTypeIndexWithinSchemaMount());
                            leafChange.setPreviousValue(entry.getValue());
                            leafChange.setEditChangeSource(editChangeSource);
                            if(parentCTN.isMountPoint()) {
                                leafChange.setMountPoint(true);
                            }
                            parentCTN.appendChildNode(leafChange);
                        }else{
                            LOGGER.debug("Not a config node {} : {} : Not adding it in ChangeTreeNode", childSchemaPath, leafQName.toString());
                        }
                    }
                }
            }
        }
    }

    public static void appendLeafChange(WritableChangeTreeNode parent, ModelNodeId parentMNID, ConfigLeafAttribute configLeafAttribute,
                                        EditChangeSource changeSource, LeafSchemaNode leafSchemaNode, SchemaRegistry schemaRegistry,
                                        ChangeType changeType) {
        QName leafQName = leafSchemaNode.getQName();
        ModelNodeId modelNodeId = new ModelNodeId(parentMNID);
        ModelNodeId leafMNID = modelNodeId.addRdn(new ModelNodeRdn(ModelNodeRdn.CONTAINER, leafQName.getNamespace().toString(), leafQName.getLocalName()));
        WritableChangeTreeNode leafChange = new ChangeTreeNodeImpl(schemaRegistry, parent, leafMNID, leafSchemaNode,
                parent.getNodesIndex(), parent.getAttributeIndex(), parent.getChangedNodeTypes(), parent.getContextMap(), parent.getNodesOfTypeIndexWithinSchemaMount());
        if (!ChangeType.create.equals(changeType)) {
            leafChange.setPreviousValue(configLeafAttribute);
        }
        if (!ChangeType.delete.equals(changeType)) {
            leafChange.setCurrentValue(configLeafAttribute);
        }
        if(changeSource != null) {
            leafChange.setEditChangeSource(changeSource);
        }
        if(parent.isMountPoint()) {
            leafChange.setMountPoint(true);
        }
        parent.appendChildNode(leafChange);
    }

    public static void appendLeafChange(WritableChangeTreeNode parent, ModelNodeId parentMNID, ModelNode parentModelNode,
                                        ConfigAttributeHelper helper, ConfigLeafAttribute configLeafAttribute,
                                        EditChangeSource changeSource, boolean isImplied, SchemaRegistry schemaRegistry) {
        if (helper != null) {
            LeafSchemaNode leafSchemaNode = helper.getLeafSchemaNode();
            QName leafQName = leafSchemaNode.getQName();
            ModelNodeId modelNodeId = new ModelNodeId(parentMNID);
            ModelNodeId leafMNID = modelNodeId.addRdn(new ModelNodeRdn(ModelNodeRdn.CONTAINER, leafQName.getNamespace().toString(), leafQName.getLocalName()));
            WritableChangeTreeNode leafChangeTreeNode = (WritableChangeTreeNode) parent.getChildren().get(leafMNID);
            if (leafChangeTreeNode == null) {
                WritableChangeTreeNode leafChange = new ChangeTreeNodeImpl(parentModelNode.getSchemaRegistry(), parent, leafMNID, leafSchemaNode,
                        parent.getNodesIndex(), parent.getAttributeIndex(), parent.getChangedNodeTypes(), parent.getContextMap(), parent.getNodesOfTypeIndexWithinSchemaMount());
                ConfigLeafAttribute previousValue = parentModelNode.getAttribute(leafQName);
                leafChange.setPreviousValue(previousValue);
                leafChange.setCurrentValue(configLeafAttribute);
                leafChange.setImplied(isImplied);
                if(changeSource != null) {
                    leafChange.setEditChangeSource(changeSource);
                }
                if(parent.isMountPoint()) {
                    leafChange.setMountPoint(true);
                }
                parent.appendChildNode(leafChange);
            }
        }
    }

    public static void appendLeafListChange(WritableChangeTreeNode parent, ModelNodeId parentMNID, LinkedHashSet<ConfigLeafAttributeWithInsertOp> configLeafAttributes, LeafListSchemaNode leafListSchemaNode,EditChangeSource changeSource, SchemaRegistry schemaRegistry) {
        QName leafListQName = leafListSchemaNode.getQName();
        ModelNodeId modelNodeId = new ModelNodeId(parentMNID);
        ModelNodeId leafListMNId = modelNodeId.addRdn(new ModelNodeRdn(ModelNodeRdn.CONTAINER, leafListQName.getNamespace().toString(), leafListQName.getLocalName()));
        WritableChangeTreeNode leafListChange = new ChangeTreeNodeImpl(schemaRegistry, parent, leafListMNId, leafListSchemaNode,
                parent.getNodesIndex(), parent.getAttributeIndex(), parent.getChangedNodeTypes(), parent.getContextMap(), parent.getNodesOfTypeIndexWithinSchemaMount());
        leafListChange.setCurrentValue(configLeafAttributes);
        leafListChange.setEditChangeSource(changeSource);
        if(parent.isMountPoint()) {
            leafListChange.setMountPoint(true);
        }
        parent.appendChildNode(leafListChange);
    }

    public static void appendKeyCTN(ListSchemaNode listSchemaNode, ModelNodeId listId, WritableChangeTreeNode listChange, EditContainmentNode editData, ChangeType changeType) {
        Collection<DataSchemaNode> childSchemaNodes = listSchemaNode.getChildNodes();
        for (EditMatchNode matchNode : editData.getMatchNodes()) {
            LeafSchemaNode leafSchemaNode = null;
            for (DataSchemaNode dataSchemaNode : childSchemaNodes) {
                if (matchNode.getQName().equals(dataSchemaNode.getQName())) {
                    leafSchemaNode = (LeafSchemaNode) dataSchemaNode;
                    break;
                }
            }
            if (leafSchemaNode != null) {
                appendLeafChange(listChange, listId, matchNode.getConfigLeafAttribute(), null, leafSchemaNode, editData.getSchemaRegistry(), changeType);
            }
        }
    }

    public static ModelNodeId buildModelNodeId(ModelNodeId parentId, EditContainmentNode editData, ListSchemaNode listSchemaNode) {
        ModelNodeId modelNodeId = new ModelNodeId(parentId.getRdnsReadOnly());
        QName listQName = listSchemaNode.getQName();
        modelNodeId.addRdn(new ModelNodeRdn(ModelNodeRdn.CONTAINER, listQName.getNamespace().toString(), listQName.getLocalName()));
        for (EditMatchNode matchNode : editData.getMatchNodes()) {
            modelNodeId.addRdn(new ModelNodeRdn(matchNode.getQName().getLocalName(), matchNode.getNamespace(), matchNode.getValue()));
        }
        return modelNodeId;
    }

    public static void setInsertOperation(EditContainmentNode editNode, WritableChangeTreeNode listChange) {
        if(editNode.getInsertOperation() != null) {
            listChange.setInsertOperation(editNode.getInsertOperation());
        }
    }

    private static Map<QName, ConfigLeafAttribute> getKeyPredicates(EditContainmentNode editContainmentNode, SchemaRegistry schemaRegistry, SchemaPath
            parentNodeSchemaPath) {
        Map<QName, ConfigLeafAttribute> keyPairs = new HashMap<>();
        ListSchemaNode listSchemaNode = getListSchemaNode(editContainmentNode.getQName(), parentNodeSchemaPath, schemaRegistry);
        List<QName> keys = listSchemaNode.getKeyDefinition();
        List<Pair<String, String>> keyValuePairs = fetchKeyValuePairs(editContainmentNode.getInsertOperation().getValue());
        StringBuilder updatedValue = new StringBuilder();
        for (Pair<String, String> pair : keyValuePairs) {
            QName keyQName = getKeyQname(keys, pair.getFirst());
            SchemaPath keySchemaPath = new SchemaPathBuilder().withParent(listSchemaNode.getPath()).appendQName(keyQName).build();
            DataSchemaNode keyLeaf = schemaRegistry.getDataSchemaNode(keySchemaPath);
            ConfigLeafAttribute configAttribute;
            try {
                configAttribute = ConfigAttributeFactory.getConfigAttribute(schemaRegistry, keyLeaf, editContainmentNode.getDomValue(), pair.getSecond(), false);
                keyPairs.put(keyQName, configAttribute);
            } catch (InvalidIdentityRefException | NetconfMessageBuilderException e) {
                throw new RuntimeException(e);
            }
            updatedValue.append("[").append(pair.getFirst()).append("=").append("'").append(configAttribute.getStringValue()).append("'").append("]");
            editContainmentNode.getInsertOperation().setUpdatedValueForList(updatedValue.toString());
        }

        return keyPairs; // must return a map of full key/value within the list
    }

    private static QName getKeyQname(List<QName> keyDefinition, String keyName) {
        QName keyDefined = null;
        for (QName key : keyDefinition) {
            if (key.getLocalName().equals(keyName)) {
                keyDefined = key;
                break;
            }
        }
        return keyDefined;
    }

    public static void appendDeleteCtnForChildNodes(WritableChangeTreeNode parentCtn, ModelNode parentMn) {
        if(parentMn instanceof XmlModelNodeImpl){
            Map<QName, IndexedList<ModelNodeId, XmlModelNodeImpl>> allChildren = ((XmlModelNodeImpl) parentMn).getChildren();
            for (Map.Entry<QName, IndexedList<ModelNodeId, XmlModelNodeImpl>> childrenOfTypeEntry : allChildren.entrySet()) {
                IndexedList<ModelNodeId, XmlModelNodeImpl> childrenOfType = childrenOfTypeEntry.getValue();
                for (XmlModelNodeImpl child : childrenOfType.list()) {
                    WritableChangeTreeNode childCtn = child.buildCtnForDelete(parentCtn);
                    if(parentCtn.isMountPoint()) {
                        childCtn.setMountPoint(true);
                    }
                    setPreviousLeafValues(childCtn,child.getModelNodeId(), child, childCtn.getChangeSource());
                    parentCtn.appendChildNode(childCtn);
                }
            }
        }
    }

    public static void addVisibilityContext(ChangeTreeNode ctn) {
        List<ModelNodeId> modelNodeIds = (List<ModelNodeId>) ctn.getContextValue(CONTEXT_MAP_INVISIBLE_NODES);
        if(modelNodeIds == null) {
            modelNodeIds = new ArrayList<>();
        }
        modelNodeIds.add(ctn.getId());
        ctn.addContextValue(CONTEXT_MAP_INVISIBLE_NODES, modelNodeIds);
    }
}
