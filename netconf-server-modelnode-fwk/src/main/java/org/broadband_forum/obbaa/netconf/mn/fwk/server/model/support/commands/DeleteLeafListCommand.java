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

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.Command;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.CommandExecutionException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildLeafListHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeDeleteException;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.LoggerFactory;

public class DeleteLeafListCommand implements Command {

    private static final AdvancedLogger LOGGER = LoggerFactory.getLogger(DeleteLeafListCommand.class,
            "netconf-server-datastore", "DEBUG", "GLOBAL");

    private ChildLeafListHelper m_childLeafListHelper;

    private ModelNode m_instance;

    public DeleteLeafListCommand addDeleteInfo(ChildLeafListHelper childLeafListHelper, ModelNode instance) {
        this.m_childLeafListHelper = childLeafListHelper;
        this.m_instance = instance;
        return this;
    }

    @Override
    public void execute() throws CommandExecutionException {
        try {
            m_childLeafListHelper.removeAllChild(m_instance);
        } catch (ModelNodeDeleteException e) {
            LOGGER.error("Error while deleting leaf-lists for ModelNode {}", m_instance, e);
            throw new CommandExecutionException(e);
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DeleteLeafListCommand{");
        sb.append("m_childLeafListHelper=").append(m_childLeafListHelper);
        sb.append(", m_instance=").append(m_instance);
        sb.append('}');
        return sb.toString();
    }
}
