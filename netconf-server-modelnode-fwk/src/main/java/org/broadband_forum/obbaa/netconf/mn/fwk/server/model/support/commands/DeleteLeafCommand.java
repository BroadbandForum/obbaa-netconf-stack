package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.commands;

import java.util.Optional;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.Command;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.CommandExecutionException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditConfigException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NotificationContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigAttributeHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.GenericConfigAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.HelperDrivenModelNode;
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

public class DeleteLeafCommand implements Command {

	private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(DeleteLeafCommand.class, LogAppNames.NETCONF_STACK);

	private SchemaRegistry m_schemaRegistry;

	private EditContainmentNode m_editContainmentNode;

	private ConfigAttributeHelper m_configAttributeHelper;

	private ModelNode m_instance;

	private QName m_editChangeNode;

	private NotificationContext m_notifyContext;

	private String m_errorOption;

    private NetconfClientInfo m_clientInfo;
	private boolean m_setToDefault;

	public DeleteLeafCommand addDeleteInfo(SchemaRegistry schemaRegistry, EditContext editContext,
										   ConfigAttributeHelper configAttributeHelper, ModelNode instance, QName changeNodeQName,
										   boolean setToDefault) {
        this.m_schemaRegistry = ((HelperDrivenModelNode)instance).getSchemaRegistry();
		this.m_editContainmentNode = editContext.getEditNode();
		this.m_configAttributeHelper = configAttributeHelper;
		this.m_instance = instance;
		this.m_editChangeNode = changeNodeQName;
		this.m_notifyContext = editContext.getNotificationContext();
		this.m_errorOption = editContext.getErrorOption();
        this.m_clientInfo = editContext.getClientInfo();
		this.m_setToDefault = setToDefault;
		return this;
	}

	@Override
	public void execute() throws CommandExecutionException {
		try {
			m_configAttributeHelper.removeAttribute(m_instance);
			SchemaPath leafPath = m_schemaRegistry.getDescendantSchemaPath(m_instance.getModelNodeSchemaPath(), m_editChangeNode);
			DataSchemaNode leafNode = m_schemaRegistry.getDataSchemaNode(leafPath);
			if(m_setToDefault && !SchemaRegistryUtil.containsWhen(leafNode)) {
				SchemaPath schemaPath = m_schemaRegistry.getDescendantSchemaPath(m_instance.getModelNodeSchemaPath(),
						m_editChangeNode);
				SchemaPath defaultChoiceCaseSchemaPath = getDefaultChoiceCaseSchemaPath(schemaPath);
                if (defaultChoiceCaseSchemaPath == null) {
					setDefaultLeaf(schemaPath);
				}
			} else {
				m_setToDefault = false;
			}

		} catch (SetAttributeException | EditConfigException e) {
			LOGGER.error("Error while delete attribute for ModelNode {}" , m_instance, e);
			throw new CommandExecutionException(e);
		}
	}

	private void setDefaultLeaf(SchemaPath schemaPath) throws SetAttributeException {
		DataSchemaNode node = m_schemaRegistry.getDataSchemaNode(schemaPath);
		if (node instanceof LeafSchemaNode) {
			Optional<? extends Object> optDefaultValue = ((LeafSchemaNode) node).getType().getDefaultValue();
			if (optDefaultValue.isPresent()) {
			    Object defaultValue = optDefaultValue.get();
				m_configAttributeHelper.setValue(m_instance, new GenericConfigAttribute(node.getQName().getLocalName(), node.getQName().getNamespace().toString(), defaultValue.toString()));
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
		sb.append(", m_instance=").append(m_instance);
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
}
