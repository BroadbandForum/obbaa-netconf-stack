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

import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.commands.CommandUtils.addVisibilityContext;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.commands.CommandUtils.appendKeyCTN;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.commands.CommandUtils.appendLeafChange;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.commands.CommandUtils.appendLeafListChange;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.commands.CommandUtils.buildModelNodeId;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.commands.CommandUtils.setInsertOperation;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.api.messages.InsertOperation;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.mn.fwk.ChangeTreeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.ChangeTreeNode.ChangeType;
import org.broadband_forum.obbaa.netconf.mn.fwk.ChangeTreeNodeImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.WritableChangeTreeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.ChoiceCaseNodeUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ChangeCommand;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditChangeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditChangeSource;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditMatchNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeCreateException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildListHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttributeWithInsertOp;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.DefaultCapabilityCommandInterceptor;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.HelperDrivenModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeGetException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.NetconfRpcErrorUtil;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

public class CreateListCommand extends AbstractChildCreationCommand implements ChangeCommand {

    private ModelNode m_listModelNode;
    private ChildListHelper m_childListHelper;
    private SchemaRegistry m_schemaRegistry;
    private ModelNodeHelperRegistry m_modelNodeHelperRegistry;

    public CreateListCommand(EditContext editContext, DefaultCapabilityCommandInterceptor interceptor, WritableChangeTreeNode changeTreeNode) {
        super(new EditContainmentNode(editContext.getEditNode()), editContext.getNotificationContext(), editContext.getErrorOption(),
                interceptor, editContext.getClientInfo(), editContext, changeTreeNode);
	}

    public CreateListCommand addAddInfo(ChildListHelper childListHelper, ModelNode instance) {
        this.m_childListHelper = childListHelper;
        this.m_listModelNode = instance;
        this.m_schemaRegistry = instance.getSchemaRegistry();
        this.m_modelNodeHelperRegistry = ((HelperDrivenModelNode)instance).getModelNodeHelperRegistry();
        return this;
    }


	@Override
	protected ModelNode createChild() throws ModelNodeCreateException {
		Map<QName, ConfigLeafAttribute> keyAttrs = new HashMap<>();
        for(EditMatchNode node : m_editData.getMatchNodes()){
            keyAttrs.put(node.getQName(), node.getConfigLeafAttribute());
        }

        Map<QName, ConfigLeafAttribute> configAttrs = new HashMap<>();
        for(EditChangeNode node : m_editData.getChangeNodes()){
			Collection<DataSchemaNode> schemaNodes = ((ListSchemaNode)m_schemaRegistry.getDataSchemaNode
					(m_childListHelper.getChildModelNodeSchemaPath())).getChildNodes();
			for (DataSchemaNode dataSchemaNode : schemaNodes){
				if(node.getQName().equals(dataSchemaNode.getQName())){
					if(dataSchemaNode instanceof LeafSchemaNode) {
						configAttrs.put(node.getQName(), node.getConfigLeafAttribute());
						break;
					}
				}
			}
        }

        ModelNode newNode = null;
        if (!isListOrderedByUser(m_editData.getQName())) { //ordered-by system
            newNode = m_childListHelper.addChild(m_listModelNode, m_editData.isVisible(), keyAttrs, configAttrs);
        } else { //ordered-by user
            InsertOperation insertOperation = m_editData.getInsertOperation();
            ModelNode existingNode = null;
            if (insertOperation == null) {
                insertOperation = InsertOperation.get(InsertOperation.LAST, null);
            } else if (InsertOperation.AFTER.equals(insertOperation.getName()) || InsertOperation.BEFORE.equals(insertOperation.getName())) {
                try {
                    existingNode = CommandUtils.getExistingNode(m_editData, m_childListHelper,
                            m_listModelNode, m_modelNodeHelperRegistry, m_schemaRegistry);
                } catch (ModelNodeGetException e) {
                    throw new ModelNodeCreateException(e.getMessage(), e);
                }
                if (existingNode == null) {
                    throw new ModelNodeCreateException(NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.BAD_ATTRIBUTE,
                            "The instance - " + m_editData.getQName().getLocalName() + " getting by key '"
                                    + insertOperation.getValue() + "' does not exist. Request Failed."));
                }
            }
            newNode = m_childListHelper.addChildByUserOrder(m_listModelNode, keyAttrs, configAttrs, insertOperation, existingNode, m_editData.isVisible());
        }

        return newNode;
	}

	private boolean isListOrderedByUser(QName qName) throws ModelNodeCreateException {
		boolean isOrderedByUser = false;
		ListSchemaNode listSchemaNode = getListSchemaNode(qName);
		if (listSchemaNode != null) {
			isOrderedByUser = listSchemaNode.isUserOrdered();
		}
		return isOrderedByUser;
	}

    private ListSchemaNode getListSchemaNode(QName qName) throws ModelNodeCreateException {
        Collection<DataSchemaNode> schemaNodes = m_schemaRegistry.getChildren(m_listModelNode.getModelNodeSchemaPath());
        ListSchemaNode listSchemaNode = null;
        for (DataSchemaNode dataSchemaNode : schemaNodes) {
            if (dataSchemaNode.getQName().equals(qName)) {
                if (dataSchemaNode instanceof ListSchemaNode) {
                    listSchemaNode = (ListSchemaNode) dataSchemaNode;
                    break;
                }
            }
            if (dataSchemaNode instanceof ChoiceSchemaNode) {
                ListSchemaNode getListFromNestedChoice = ChoiceCaseNodeUtil.getListSchemaNodeFromNestedChoice(dataSchemaNode, qName);
                if (getListFromNestedChoice != null) {
                    return getListFromNestedChoice;
                }
            }
        }
        if (listSchemaNode == null) {
            throw new ModelNodeCreateException(String.format("Cannot get the schema node for '%s'", qName));
        }
        return listSchemaNode;
    }
    
	@Override
	public String toString() {
        final StringBuilder sb = new StringBuilder("CreateListCommand{");
        sb.append("m_parentNode=").append(m_listModelNode);
		sb.append(", m_childListHelper=").append(m_childListHelper);
		sb.append(", m_schemaRegistry=").append(m_schemaRegistry);
		sb.append(", m_modelNodeHelperRegistry=").append(m_modelNodeHelperRegistry);
		sb.append('}');
		return sb.toString();
	}

    @Override
    public void appendChange(WritableChangeTreeNode parent, boolean isImplied) {
        //create list CTN
        SchemaRegistry schemaRegistry = m_listModelNode.getSchemaRegistry();
        ListSchemaNode listSchemaNode = m_childListHelper.getSchemaNode();
        ModelNodeId parentId = m_listModelNode.getModelNodeId();
        ModelNodeId listID = buildModelNodeId(parentId, m_editData, listSchemaNode);
        WritableChangeTreeNode listChange = new ChangeTreeNodeImpl(schemaRegistry, parent, listID, listSchemaNode,
                parent.getNodesIndex(), parent.getAttributeIndex(), parent.getChangedNodeTypes(), parent.getContextMap(), parent.getNodesOfTypeIndexWithinSchemaMount());
        listChange.setChangeType(ChangeTreeNode.ChangeType.create);
        if(listSchemaNode.isUserOrdered()) {
            setInsertOperation(m_editData, listChange);
        }
        listChange.setEditOperation(m_editData.getEditOperation());
        listChange.setImplied(isImplied);
        listChange.setEditChangeSource(m_editData.getChangeSource());
        if(parent.isMountPoint()) {
            listChange.setMountPoint(true);
        }
        if(!m_editData.isVisible()) {
            addVisibilityContext(listChange);
        }

        //create key CTNs
        appendKeyCTN(listSchemaNode, listID, listChange, m_editData, ChangeType.create);

        //segregate leaf and leaflist changes
        Collection<DataSchemaNode> childSchemaNodes = listSchemaNode.getChildNodes();
        Map<LeafSchemaNode, EditChangeNode> leafDSNChangeNodeMap = new LinkedHashMap<>();
        Map<LeafListSchemaNode, Set<EditChangeNode>> leafListDSNChangeNodesMap = new LinkedHashMap<>();
        for (EditChangeNode changeNode : m_editData.getChangeNodes()) {
            for (DataSchemaNode dataSchemaNode : childSchemaNodes) {
                if (changeNode.getQName().equals(dataSchemaNode.getQName())) {
                    if (dataSchemaNode instanceof LeafSchemaNode) {
                        leafDSNChangeNodeMap.putIfAbsent((LeafSchemaNode) dataSchemaNode, changeNode);
                        break;
                    } else if (dataSchemaNode instanceof LeafListSchemaNode) {
                        Set<EditChangeNode> changeNodes = leafListDSNChangeNodesMap.get(dataSchemaNode);
                        if(changeNodes == null) {
                            changeNodes = new LinkedHashSet<>();
                        }
                        changeNodes.add(changeNode);
                        leafListDSNChangeNodesMap.put((LeafListSchemaNode) dataSchemaNode, changeNodes);
                        break;
                    }
                }
            }
        }

        // create CTNs for leaf changes
        for(Map.Entry<LeafSchemaNode, EditChangeNode> entry : leafDSNChangeNodeMap.entrySet()) {
            appendLeafChange(listChange, listID, entry.getValue().getConfigLeafAttribute(), entry.getValue().getChangeSource(), entry.getKey(), m_schemaRegistry, ChangeType.create);
        }

        // create CTNs for leaflist changes
        EditChangeSource changeSource = null;
        for(Map.Entry<LeafListSchemaNode, Set<EditChangeNode>> entry : leafListDSNChangeNodesMap.entrySet()) {
            LeafListSchemaNode leafListSchemaNode = entry.getKey();
            LinkedHashSet<ConfigLeafAttributeWithInsertOp> leafListAttributes = new LinkedHashSet<>();
            for(EditChangeNode changeNode : entry.getValue()) {
                ConfigLeafAttributeWithInsertOp attributeWithInsertOp = new ConfigLeafAttributeWithInsertOp(changeNode.getConfigLeafAttribute(), changeNode.getInsertOperation());
                leafListAttributes.add(attributeWithInsertOp);
                changeSource = changeNode.getChangeSource(); // FIXME : One ChangeTreeNode is created for all leaf-list changes. Cant set changesource for each leaf-list
            }
            appendLeafListChange(listChange, listID, leafListAttributes, leafListSchemaNode, changeSource, m_schemaRegistry);
        }
        parent.appendChildNode(listChange);
    }
}
