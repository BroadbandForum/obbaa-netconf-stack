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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support;

import org.broadband_forum.obbaa.netconf.mn.fwk.WritableChangeTreeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.Command;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.CommandExecutionException;

/**
 * This command is just used to append CTNs for nodes which does not have any effective change
 * as these nodes wont have any commands generated for them to append CTN.
 */
public class AppendCtnCommand implements Command {
    private final WritableChangeTreeNode m_changeTreeNode;
    private final WritableChangeTreeNode m_parentCtn;

    public AppendCtnCommand(WritableChangeTreeNode parentCtn, WritableChangeTreeNode changeTreeNode) {
        m_parentCtn = parentCtn;
        m_changeTreeNode = changeTreeNode;
    }

    @Override
    public void execute() throws CommandExecutionException {
        if (m_changeTreeNode.hasChanged()){
            m_parentCtn.appendChildNode(m_changeTreeNode);
        }
    }
}
