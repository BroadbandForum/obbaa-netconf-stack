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

import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigAttributeFactory.getConfigLeafAttribute;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.LeafDefaultValueUtility.getDefaultValue;

import java.util.Optional;

import org.broadband_forum.obbaa.netconf.mn.fwk.ChangeTreeNodeImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.WritableChangeTreeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.ChoiceCaseNodeUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ChangeCommand;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.CommandExecutionException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditChangeSource;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditConfigException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NotificationContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigAttributeFactory;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigAttributeHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.SetAttributeException;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

import com.google.common.annotations.VisibleForTesting;

public class DeleteLeafCommand implements ChangeCommand {

	private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(DeleteLeafCommand.class, LogAppNames.NETCONF_STACK);

	private SchemaRegistry m_schemaRegistry;

	private EditContainmentNode m_editContainmentNode;

	private ConfigAttributeHelper m_configAttributeHelper;

	private ModelNode m_parentModelNode;

	private QName m_editChangeNode;

	private NotificationContext m_notifyContext;

	private String m_errorOption;

	private boolean m_setToDefault;

	private SchemaPath m_leafSchemaPath;

	private EditChangeSource m_editChangeSource;

	public DeleteLeafCommand addDeleteInfo(SchemaRegistry schemaRegistry, EditContext editContext,
										   ConfigAttributeHelper configAttributeHelper, ModelNode parentModelNode, QName changeNodeQName,
										   boolean setToDefault, EditChangeSource editChangeSource) {
		this.m_schemaRegistry = schemaRegistry;
		this.m_editContainmentNode = editContext.getEditNode();
		this.m_configAttributeHelper = configAttributeHelper;
		this.m_parentModelNode = parentModelNode;
		this.m_editChangeNode = changeNodeQName;
		this.m_notifyContext = editContext.getNotificationContext();
		this.m_errorOption = editContext.getErrorOption();
		this.m_setToDefault = setToDefault;
        m_editChangeSource = editChangeSource;
        m_leafSchemaPath = m_schemaRegistry.getDescendantSchemaPath(m_parentModelNode.getModelNodeSchemaPath(), m_editChangeNode);
		return this;
	}

	@Override
	public void execute() throws CommandExecutionException {
		try {
			m_configAttributeHelper.removeAttribute(m_parentModelNode);
			DataSchemaNode leafNode = m_schemaRegistry.getDataSchemaNode(m_leafSchemaPath);
			if (isDefaultToBeSet(leafNode)) {
				setDefaultLeaf(m_leafSchemaPath);
			} else {
				m_setToDefault = false;
			}

		} catch (SetAttributeException | EditConfigException e) {
			LOGGER.error("Error while delete attribute for ModelNode {}", m_parentModelNode, e);
			throw new CommandExecutionException(e);
		}
	}

	private boolean isDefaultToBeSet(DataSchemaNode leafNode) {
	    if (m_setToDefault && !SchemaRegistryUtil.containsWhen(leafNode) && !SchemaRegistryUtil.containsAugmentWhen(leafNode, m_schemaRegistry) && !SchemaRegistryUtil.containsUsesWhen(leafNode, m_schemaRegistry)) {
	        DataSchemaNode choiceSchemaNode = getChoiceParentSchemaNode(m_leafSchemaPath);
	        SchemaPath defaultChoiceCaseSchemaPath = getDefaultChoiceCaseSchemaPath(m_leafSchemaPath);
	        SchemaPath caseSchemaPath = ChoiceCaseNodeUtil.getCaseSchemaNodeFromChildNodeIfApplicable(m_schemaRegistry, m_leafSchemaPath);

	        //if it is of caseChildNode, then return true only if that caseSchemaNode is default choice case
	        // Also, return true if it is not a under a choiceNode, so it will be decided in the caller whether the default value has to be set or not 
	        if (defaultChoiceCaseSchemaPath != null && caseSchemaPath != null && defaultChoiceCaseSchemaPath.equals(caseSchemaPath)) {
	            return true;
	        } else if(choiceSchemaNode == null){
	            return true;
	        }
	    }
	    return false;
	}

	private void setDefaultLeaf(SchemaPath schemaPath) throws SetAttributeException {
		DataSchemaNode node = m_schemaRegistry.getDataSchemaNode(schemaPath);
		if (node instanceof LeafSchemaNode) {
			Optional<? extends Object> optDefaultValue = ((LeafSchemaNode) node).getType().getDefaultValue();
			if (optDefaultValue.isPresent()) {
				m_configAttributeHelper.setValue(m_parentModelNode, ConfigAttributeFactory.getConfigAttributeFromDefaultValue(m_schemaRegistry, (LeafSchemaNode) node));
			}
		}
	}

	private SchemaPath getDefaultChoiceCaseSchemaPath(SchemaPath schemaPath) throws EditConfigException {
		DataSchemaNode parentChoiceNode = getChoiceParentSchemaNode(schemaPath);
		if (parentChoiceNode != null) {
			ChoiceSchemaNode choiceNode = (ChoiceSchemaNode) parentChoiceNode;
			Optional<CaseSchemaNode> optDefaultCase = choiceNode.getDefaultCase();
			if (optDefaultCase.isPresent()) {
				return optDefaultCase.get().getPath();
			}
		}
		return null;
	}

	private DataSchemaNode getChoiceParentSchemaNode(SchemaPath schemaPath) {
		SchemaPath parentSchemaPath = schemaPath.getParent();
		while (parentSchemaPath != null) {
			DataSchemaNode parentSchemaNode = m_schemaRegistry.getDataSchemaNode(parentSchemaPath);
			if (parentSchemaNode instanceof ChoiceSchemaNode) {
				return parentSchemaNode;
			}
			parentSchemaPath = parentSchemaPath.getParent();
		}
		return null;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("DeleteLeafCommand{");
		sb.append("m_schemaRegistry=").append(m_schemaRegistry);
		sb.append(", m_editContainmentNode=").append(m_editContainmentNode);
		sb.append(", m_configAttributeHelper=").append(m_configAttributeHelper);
		sb.append(", m_parentModelNode=").append(m_parentModelNode);
		sb.append(", m_editChangeNode=").append(m_editChangeNode);
		sb.append(", m_notifyContext=").append(m_notifyContext);
		sb.append(", m_errorOption='").append(m_errorOption).append('\'');
		sb.append('}');
		return sb.toString();
	}

	@VisibleForTesting
	boolean isSetToDefault(){
		return m_setToDefault;
	}

	@Override
	public void appendChange(WritableChangeTreeNode parent, boolean isImplied) {
		LeafSchemaNode leafSchemaNode = m_configAttributeHelper.getLeafSchemaNode();
		ModelNodeId leafId = new ModelNodeId(m_parentModelNode.getModelNodeId().getRdnsReadOnly());
		QName leafQname = leafSchemaNode.getQName();
		leafId.addRdn(new ModelNodeRdn(ModelNodeRdn.CONTAINER, leafQname.getNamespace().toString(), leafQname.getLocalName()));
		WritableChangeTreeNode leafChange = new ChangeTreeNodeImpl(m_schemaRegistry, parent, leafId, leafSchemaNode,
				parent.getNodesIndex(), parent.getAttributeIndex(), parent.getChangedNodeTypes(), parent.getContextMap(), parent.getNodesOfTypeIndexWithinSchemaMount());
		leafChange.setPreviousValue(m_parentModelNode.getAttribute(leafQname));
		if (isDefaultToBeSet(leafSchemaNode)) {
			String defaultValue = getDefaultValue(leafSchemaNode);
			if (defaultValue != null) {
				leafChange.setCurrentValue(getConfigLeafAttribute(m_schemaRegistry, leafSchemaNode, defaultValue));
			}
		}
		leafChange.setImplied(isImplied);
		leafChange.setEditChangeSource(m_editChangeSource);
		if (parent.isMountPoint()) {
			leafChange.setMountPoint(true);
		}
		parent.appendChildNode(leafChange);
	}
}
