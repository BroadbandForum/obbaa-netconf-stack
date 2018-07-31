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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigAttributeHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeGetException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.CommandExecutionException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FlagForRestPutOperations;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeCreateException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildListHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.DefaultCapabilityCommandInterceptor;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.HelperDrivenModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeDeleteException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.NetconfRpcErrorUtil;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

import org.broadband_forum.obbaa.netconf.api.messages.InsertOperation;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditChangeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditMatchNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetAttributeException;

public class ReplaceChildInListCommand extends AbstractChildCreationCommand {

    private ChildListHelper m_childListHelper;
    private ModelNode m_parentNode;
    private ModelNode m_childNode;
    private SchemaRegistry m_schemaRegistry;
    private ModelNodeHelperRegistry m_modelNodeHelperRegistry;

    public ReplaceChildInListCommand(EditContext editContext, DefaultCapabilityCommandInterceptor interceptor) {
        super(new EditContainmentNode(editContext.getEditNode()), editContext.getNotificationContext(), editContext
                        .getErrorOption(),
                interceptor, editContext.getClientInfo(), editContext);
    }

    public ReplaceChildInListCommand addReplaceInfo(ChildListHelper childListHelper, ModelNode instance, ModelNode
            child) {
        this.m_childListHelper = childListHelper;
        this.m_parentNode = instance;
        this.m_childNode = child;
        this.m_schemaRegistry = ((HelperDrivenModelNode) instance).getSchemaRegistry();
        this.m_modelNodeHelperRegistry = ((HelperDrivenModelNode) instance).getModelNodeHelperRegistry();
        return this;
    }

    @Override
    public void execute() throws CommandExecutionException {
        super.execute();

    }

    @Override
    protected ModelNode createChild() throws ModelNodeCreateException {
        Map<QName, ConfigLeafAttribute> keyAttrs = new HashMap<>();
        for (EditMatchNode node : m_editData.getMatchNodes()) {
            keyAttrs.put(node.getQName(), node.getConfigLeafAttribute());
        }

        Map<QName, ConfigLeafAttribute> configAttrs = new HashMap<>();
        for (EditChangeNode node : m_editData.getChangeNodes()) {
            Collection<DataSchemaNode> schemaNodes = ((ListSchemaNode) m_schemaRegistry.getDataSchemaNode
                    (m_childListHelper.getChildModelNodeSchemaPath())).getChildNodes();
            for (DataSchemaNode dataSchemaNode : schemaNodes) {
                if (node.getQName().equals(dataSchemaNode.getQName())) {
                    if (dataSchemaNode instanceof LeafSchemaNode) {
                        configAttrs.put(node.getQName(), node.getConfigLeafAttribute());
                        break;
                    }
                }
            }
        }

        ModelNode newNode = null;
        try {
            if (!isListOrderedByUser(m_editData.getQName())) { //ordered-by system
                if (m_childNode != null) {
                    m_childListHelper.removeChild(m_parentNode, m_childNode);
                    FlagForRestPutOperations.setInstanceReplaceFlag();
                }
                newNode = m_childListHelper.addChild(m_parentNode, m_editData.getName(), keyAttrs, configAttrs);
            } else { //ordered-by user
                InsertOperation insertOperation = m_editData.getInsertOperation();
                ModelNode existingNode = null;
                if (insertOperation == null) {
                    insertOperation = new InsertOperation(InsertOperation.LAST, null);
                } else if (InsertOperation.AFTER.equals(insertOperation.getName()) || InsertOperation.BEFORE.equals
                        (insertOperation.getName())) {
                    try {
                        existingNode = CommandUtils.getExistingNode(m_editData.getQName(), insertOperation.getValue()
                                , m_childListHelper,
                                m_parentNode, m_modelNodeHelperRegistry, m_schemaRegistry);
                    } catch (ModelNodeGetException e) {
                        throw new ModelNodeCreateException(e.getMessage(), e);
                    }
                    if (existingNode == null) {
                        throw new ModelNodeCreateException(NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag
                                        .BAD_ATTRIBUTE,
                                "The instance - " + m_editData.getQName().getLocalName() + " getting by key '"
                                        + insertOperation.getValue() + "' does not exist. Request Failed."));
                    }
                    //check existing index insert node can't be same the replace node
                    boolean isSame = checkSameNode(keyAttrs, existingNode);
                    if (isSame) {
                        String errorMessage = "The instance - " + existingNode.getQName().getLocalName() + " getting " +
                                "by key '"
                                + insertOperation.getValue() + "' can't be same the edit node. Request Failed.";
                        throw new ModelNodeCreateException(NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag
                                .DATA_MISSING, errorMessage));
                    }
                }

                if (m_childNode == null) { // childNode didn't create
                    newNode = m_childListHelper.addChildByUserOrder(m_parentNode, keyAttrs, configAttrs,
                            insertOperation, existingNode);
                    return newNode;
                } else {
                    m_childListHelper.removeChild(m_parentNode, m_childNode);
                    FlagForRestPutOperations.setInstanceReplaceFlag();
                    newNode = m_childListHelper.addChildByUserOrder(m_parentNode, keyAttrs, configAttrs,
                            insertOperation, existingNode);
                    return newNode;
                }
            }

        } catch (ModelNodeDeleteException | GetAttributeException e) {
            throw new ModelNodeCreateException(e);
        }

        return newNode;
    }

    private boolean isListOrderedByUser(QName qName) throws ModelNodeCreateException {
        boolean isOrderedByUser = false;
        ListSchemaNode listSchemaNode = CommandUtils.getListSchemaNode(qName, m_schemaRegistry, m_parentNode
                .getModelNodeSchemaPath());
        if (listSchemaNode != null) {
            isOrderedByUser = listSchemaNode.isUserOrdered();
        }
        return isOrderedByUser;
    }

    private boolean checkSameNode(Map<QName, ConfigLeafAttribute> keyAttributeRelaceNode, ModelNode insertNode)
            throws GetAttributeException {
        boolean isSame = true;
        for (QName keyReplaceNode : keyAttributeRelaceNode.keySet()) {
            Map<QName, ConfigAttributeHelper> naturalKeyHelpers = m_modelNodeHelperRegistry.getNaturalKeyHelpers
                    (insertNode.getModelNodeSchemaPath());
            for (QName keyInsertNode : naturalKeyHelpers.keySet()) {
                if (keyInsertNode.equals(keyReplaceNode)) {
                    String valueReplaceNode = keyAttributeRelaceNode.get(keyReplaceNode).getStringValue();
                    String valueInsertNode = m_modelNodeHelperRegistry.getNaturalKeyHelper(insertNode.getModelNodeSchemaPath(),
                            keyInsertNode).getValue(insertNode).getStringValue();
                    if (!valueReplaceNode.equals(valueInsertNode)) {
                        isSame = false;
                        break;
                    }
                }

            }
        }
        return isSame;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ReplaceChildInListCommand{");
        sb.append("m_childListHelper=").append(m_childListHelper);
        sb.append(", m_parentNode=").append(m_parentNode);
        sb.append(", m_childNode=").append(m_childNode);
        sb.append(", m_schemaRegistry=").append(m_schemaRegistry);
        sb.append(", m_modelNodeHelperRegistry=").append(m_modelNodeHelperRegistry);
        sb.append('}');
        return sb.toString();
    }
}
