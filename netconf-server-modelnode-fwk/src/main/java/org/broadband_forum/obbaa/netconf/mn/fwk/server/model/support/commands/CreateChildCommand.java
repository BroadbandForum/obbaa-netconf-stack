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

import java.util.HashMap;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeCreateException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildContainerHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.DefaultCapabilityCommandInterceptor;
import org.opendaylight.yangtools.yang.common.QName;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditMatchNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;

public class CreateChildCommand extends AbstractChildCreationCommand {

    private ModelNode m_parentNode;
    private ChildContainerHelper m_childContainerHelper;

    public CreateChildCommand(EditContext editContext, DefaultCapabilityCommandInterceptor interceptor) {
        super(new EditContainmentNode(editContext.getEditNode()), editContext.getNotificationContext(), editContext
                        .getErrorOption(),
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
