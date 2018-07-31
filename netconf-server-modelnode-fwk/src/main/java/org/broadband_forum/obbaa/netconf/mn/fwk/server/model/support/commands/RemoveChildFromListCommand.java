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
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildListHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeDeleteException;

public class RemoveChildFromListCommand implements Command {

    private ModelNode m_instance;
    private ChildListHelper m_childListHelper;
    private ModelNode m_item;

    @Override
    public void execute() throws CommandExecutionException {
        try {
            m_childListHelper.removeChild(m_instance, m_item);
        } catch (IllegalArgumentException | ModelNodeDeleteException e) {
            throw new CommandExecutionException(e);
        }
    }

    public Command addRemoveInfo(ChildListHelper childListHelper, ModelNode instance, ModelNode item) {
        this.m_childListHelper = childListHelper;
        this.m_instance = instance;
        this.m_item = item;
        return this;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RemoveChildFromListCommand{");
        sb.append("m_instance=").append(m_instance);
        sb.append(", m_childListHelper=").append(m_childListHelper);
        sb.append(", m_item=").append(m_item);
        sb.append('}');
        return sb.toString();
    }
}
