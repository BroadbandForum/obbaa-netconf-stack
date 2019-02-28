package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.commands;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildContainerHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeDeleteException;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.Command;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.CommandExecutionException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;

public class DeleteChildCommand implements Command{

	private ChildContainerHelper m_childContainerHelper;
	private ModelNode m_instance;
	private ModelNode m_child;

	public DeleteChildCommand addDeleteInfo(ChildContainerHelper childContainerHelper, ModelNode instance, ModelNode child) {
		this.m_childContainerHelper = childContainerHelper;
		this.m_instance = instance;
		this.m_child = child;
		return this;
	}

	@Override
	public void execute() throws CommandExecutionException {
		try {
		    m_childContainerHelper.deleteChild(m_instance);
		} catch ( ModelNodeDeleteException e) {
			throw new CommandExecutionException(e);
		}
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("DeleteChildCommand{");
		sb.append("m_childContainerHelper=").append(m_childContainerHelper);
		sb.append(", m_instance=").append(m_instance);
		sb.append(", m_child=").append(m_child);
		sb.append('}');
		return sb.toString();
	}
}
