package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.commands;

import java.util.Collection;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.HelperDrivenModelNode;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

import org.broadband_forum.obbaa.netconf.api.messages.EditConfigOperations;
import org.broadband_forum.obbaa.netconf.api.messages.InsertOperation;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.Command;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.CommandExecutionException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditConfigException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeCreateException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildListHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeDeleteException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeGetException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.NetconfRpcErrorUtil;

public class MergeChildInListCommand implements Command {

	private ModelNode m_parentNode;
	private ChildListHelper m_childListHelper;
	private SchemaRegistry m_schemaRegistry;
	private ModelNodeHelperRegistry m_modelNodeHelperRegistry;
	private EditContext m_editContext;
	private ModelNode m_childNode; // found by isThisIntance

	public MergeChildInListCommand addAddInfo(ChildListHelper childListHelper, ModelNode instance, EditContext editContext, ModelNode childNode) {
        this.m_childListHelper = childListHelper;
        this.m_parentNode = instance;
        this.m_schemaRegistry = ((HelperDrivenModelNode)instance).getSchemaRegistry();
        this.m_modelNodeHelperRegistry = ((HelperDrivenModelNode)instance).getModelNodeHelperRegistry();
		this.m_editContext = editContext;
		this.m_childNode = childNode;
        return this;
	}

	@Override
	public void execute() throws CommandExecutionException {
		EditContainmentNode editChildNode = m_editContext.getEditNode();
		try {
			if (isListOrderedByUser(editChildNode.getQName())) {// ordered-by user
				if (editChildNode.getInsertOperation() != null) {
					InsertOperation insertOperation = editChildNode.getInsertOperation();
		        	ModelNode existingNode = null;
		        	if (InsertOperation.AFTER.equals(insertOperation.getName()) || InsertOperation.BEFORE.equals(insertOperation.getName())) {
		        		try {
							existingNode = CommandUtils.getExistingNode(editChildNode.getQName(), insertOperation.getValue(),
									m_childListHelper, m_parentNode, m_modelNodeHelperRegistry, m_schemaRegistry);
						} catch (ModelNodeGetException e) {
							throw new CommandExecutionException(e.getMessage(), e);
						}
		        		if (existingNode == null) {
		        			String errorMessage = "The instance - " + editChildNode.getQName().getLocalName() + " getting by key '"
									+ insertOperation.getValue() + "' does not exist. Request Failed.";
		        			ModelNodeGetException ex = new ModelNodeGetException(errorMessage);
		        			throw new CommandExecutionException(NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.BAD_ATTRIBUTE, errorMessage), ex);
		        		}
		        		//Check same list node
		        		if (m_childNode.equals(existingNode)) {
		        			String errorMessage = "The instance - " + editChildNode.getQName().getLocalName() + " getting by key '"
									+ insertOperation.getValue() + "' can't be same the edit node. Request Failed.";
		        			ModelNodeGetException ex = new ModelNodeGetException(errorMessage);
		        			throw new CommandExecutionException(NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.DATA_MISSING, errorMessage), ex);
		        		}
		        	}
					//remove it before adding back new index
					m_childListHelper.removeChild(m_parentNode, m_childNode);
					m_childListHelper.addChildByUserOrder(m_parentNode, m_childNode, existingNode, insertOperation);
				}
			}
			delegateToChild(editChildNode, m_childNode);
		} catch (ModelNodeGetException | ModelNodeDeleteException | EditConfigException | ModelNodeCreateException e) {
			throw new CommandExecutionException(e);
		}

	}

	private boolean isListOrderedByUser(QName qName) throws ModelNodeGetException {
		boolean isOrderedByUser = false;
		ListSchemaNode listSchemaNode = getListSchemaNode(qName);
		if (listSchemaNode != null) {
			isOrderedByUser = listSchemaNode.isUserOrdered();
		}
		return isOrderedByUser;
	}

	private ListSchemaNode getListSchemaNode(QName qName) throws ModelNodeGetException {
		Collection<DataSchemaNode> schemaNodes = m_schemaRegistry.getChildren(m_parentNode.getModelNodeSchemaPath());
		ListSchemaNode listSchemaNode = null;
		for (DataSchemaNode dataSchemaNode : schemaNodes) {
			if (dataSchemaNode.getQName().equals(qName)) {
				if (dataSchemaNode instanceof ListSchemaNode) {
					listSchemaNode = (ListSchemaNode)dataSchemaNode;
					break;
				}
			}
			if (dataSchemaNode instanceof ChoiceSchemaNode) {
				Collection<CaseSchemaNode> schemaChoiceCases = ((ChoiceSchemaNode)dataSchemaNode).getCases().values();
				for (CaseSchemaNode choiceCaseNode : schemaChoiceCases) {
					if (choiceCaseNode.getDataChildByName(qName) != null && (choiceCaseNode.getDataChildByName(qName)) instanceof ListSchemaNode) {
						listSchemaNode = (ListSchemaNode)choiceCaseNode.getDataChildByName(qName);
						break;
					}
				}
			}
		}
		if (listSchemaNode == null) {
			throw new ModelNodeGetException(String.format("Cannot get the schema node for '%s'", qName));
		}
		return listSchemaNode;
	}

    protected void delegateToChild(EditContainmentNode editData, ModelNode childNode) throws EditConfigException {
		if(editData.getChangeNodes().size() > 0 || editData.getChildren().size() > 0) {
			editData.setEditOperation(EditConfigOperations.MERGE);
            EditContext context = new EditContext(editData, m_editContext.getNotificationContext(),
                    m_editContext.getErrorOption(), m_editContext.getClientInfo());
            childNode.editConfig(context);
        }
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("MergeChildInListCommand{");
		sb.append("m_parentNode=").append(m_parentNode);
		sb.append(", m_childListHelper=").append(m_childListHelper);
		sb.append(", m_schemaRegistry=").append(m_schemaRegistry);
		sb.append(", m_modelNodeHelperRegistry=").append(m_modelNodeHelperRegistry);
		sb.append(", m_editContext=").append(m_editContext);
		sb.append(", m_childNode=").append(m_childNode);
		sb.append('}');
		return sb.toString();
	}
}
