package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.commands;

import java.util.HashMap;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildContainerHelper;
import org.opendaylight.yangtools.yang.common.QName;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditMatchNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeCreateException;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.DefaultCapabilityCommandInterceptor;

public class CreateChildCommand extends AbstractChildCreationCommand {

    private ModelNode m_parentNode;
    private ChildContainerHelper m_childContainerHelper;

    public CreateChildCommand(EditContext editContext, DefaultCapabilityCommandInterceptor interceptor) {
        super(new EditContainmentNode(editContext.getEditNode()), editContext.getNotificationContext(), editContext.getErrorOption(),
                interceptor, editContext.getClientInfo(), editContext);
	}

    public CreateChildCommand addCreateInfo(ChildContainerHelper childContainerHelper, ModelNode instance) {
        this.m_childContainerHelper = childContainerHelper;
        this.m_parentNode = instance;
        return this;
    }

	@Override
	protected ModelNode createChild() throws ModelNodeCreateException {
		Map<QName, ConfigLeafAttribute> keyAttrs = new HashMap<>();
        for (EditMatchNode node : m_editData.getMatchNodes()) {
            keyAttrs.put(node.getQName(), node.getConfigLeafAttribute());
        }

        ModelNode newNode = m_childContainerHelper.createChild(m_parentNode, keyAttrs);

        return newNode;
	}

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CreateChildCommand{");
        sb.append("m_parentNode=").append(m_parentNode);
        sb.append(", m_childContainerHelper=").append(m_childContainerHelper);
        sb.append('}');
        return sb.toString();
    }
}
