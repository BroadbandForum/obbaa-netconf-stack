package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.commands;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.HelperDrivenModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

import org.broadband_forum.obbaa.netconf.api.messages.InsertOperation;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditChangeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditMatchNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeCreateException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildListHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.DefaultCapabilityCommandInterceptor;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeGetException;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.NetconfRpcErrorUtil;

public class AddChildToListCommand extends AbstractChildCreationCommand {

	private ModelNode m_parentNode;
    private ChildListHelper m_childListHelper;
    private SchemaRegistry m_schemaRegistry;
    private ModelNodeHelperRegistry m_modelNodeHelperRegistry;

	public AddChildToListCommand(EditContext editContext, DefaultCapabilityCommandInterceptor interceptor) {
        super(new EditContainmentNode(editContext.getEditNode()), editContext.getNotificationContext(), editContext.getErrorOption(),
                interceptor, editContext.getClientInfo(), editContext);
	}

    public AddChildToListCommand addAddInfo(ChildListHelper childListHelper, ModelNode instance) {
        this.m_childListHelper = childListHelper;
        this.m_parentNode = instance;
        this.m_schemaRegistry = ((HelperDrivenModelNode)instance).getSchemaRegistry();
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
        	newNode = m_childListHelper.addChild(m_parentNode, m_editData.getName(), keyAttrs, configAttrs);
        } else { //ordered-by user
            InsertOperation insertOperation = m_editData.getInsertOperation();
            ModelNode existingNode = null;
            if (insertOperation == null) {
                insertOperation = new InsertOperation(InsertOperation.LAST, null);
            } else if (InsertOperation.AFTER.equals(insertOperation.getName()) || InsertOperation.BEFORE.equals(insertOperation.getName())) {
                try {
                    existingNode = CommandUtils.getExistingNode(m_editData.getQName(), insertOperation.getValue(), m_childListHelper,
							m_parentNode, m_modelNodeHelperRegistry, m_schemaRegistry);
                } catch (ModelNodeGetException e) {
                    throw new ModelNodeCreateException(e.getMessage(), e);
                }
                if (existingNode == null) {
                    throw new ModelNodeCreateException(NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.BAD_ATTRIBUTE,
                            "The instance - " + m_editData.getQName().getLocalName() + " getting by key '"
                                    + insertOperation.getValue() + "' does not exist. Request Failed."));
                }
            }
            newNode = m_childListHelper.addChildByUserOrder(m_parentNode, keyAttrs, configAttrs, insertOperation, existingNode);
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
			throw new ModelNodeCreateException(String.format("Cannot get the schema node for '%s'", qName));
		}
		return listSchemaNode;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("AddChildToListCommand{");
		sb.append("m_parentNode=").append(m_parentNode);
		sb.append(", m_childListHelper=").append(m_childListHelper);
		sb.append(", m_schemaRegistry=").append(m_schemaRegistry);
		sb.append(", m_modelNodeHelperRegistry=").append(m_modelNodeHelperRegistry);
		sb.append('}');
		return sb.toString();
	}
}
