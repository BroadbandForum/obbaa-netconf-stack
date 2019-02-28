package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.commands;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.Command;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.CommandExecutionException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditConfigException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;

public class DelegateToChildCommand implements Command {

    private EditContext m_editContext;
    private ModelNode m_instance;
    @Override
    public void execute() throws CommandExecutionException {
        try {
            this.m_instance.editConfig(m_editContext);            
        } catch (EditConfigException e) {
            throw new CommandExecutionException(e.getRpcError(), e);
        }
    }

    public DelegateToChildCommand addInfo(EditContext editContext, ModelNode child) {
        this.m_editContext = new EditContext(editContext);
        this.m_instance = child;
        return this;
    }

}
