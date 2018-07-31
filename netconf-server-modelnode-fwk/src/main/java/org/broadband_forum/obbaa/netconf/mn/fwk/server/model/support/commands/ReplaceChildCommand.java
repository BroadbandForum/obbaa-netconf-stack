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
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditConfigException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildContainerHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeGetException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.Command;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.CommandExecutionException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FlagForRestPutOperations;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeCreateException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NotificationContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.DefaultCapabilityCommandInterceptor;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeDeleteException;
import org.opendaylight.yangtools.yang.common.QName;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigOperations;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditMatchNode;

public class ReplaceChildCommand implements Command {

    private EditContainmentNode m_editData;
    private ModelNode m_parentNode;
    private ModelNode m_childNode;
    private ChildContainerHelper m_childContainerHelper;
    private DefaultCapabilityCommandInterceptor m_interceptor;
    private NotificationContext m_notificationContext;
    private String m_errorOption;
    private NetconfClientInfo m_clientInfo;

    public ReplaceChildCommand(EditContext editContext, DefaultCapabilityCommandInterceptor interceptor) {
        m_editData = new EditContainmentNode(editContext.getEditNode());
        m_notificationContext = editContext.getNotificationContext();
        m_errorOption = editContext.getErrorOption();
        m_interceptor = interceptor;
        m_clientInfo = editContext.getClientInfo();
    }

    public ReplaceChildCommand addReplaceInfo(ChildContainerHelper childContainerHelper, ModelNode instance) {
        this.m_childContainerHelper = childContainerHelper;
        this.m_parentNode = instance;
        return this;
    }

    protected ModelNode replaceChild() throws ModelNodeCreateException {
        Map<QName, ConfigLeafAttribute> keyAttrs = new HashMap<>();
        for (EditMatchNode node : m_editData.getMatchNodes()) {
            keyAttrs.put(node.getQName(), node.getConfigLeafAttribute());
        }
        try {
            m_childNode = m_childContainerHelper.getValue(m_parentNode);
            if (m_childNode != null) {
                m_childContainerHelper.deleteChild(m_parentNode);
                FlagForRestPutOperations.setInstanceReplaceFlag();
            }
        } catch (ModelNodeGetException e1) {
            throw new ModelNodeCreateException("Error while getting a copy of old value ", e1);
        } catch (ModelNodeDeleteException e) {
            throw new ModelNodeCreateException("Error while deleting old value ", e);
        }
        ModelNode newNode = m_childContainerHelper.createChild(m_parentNode, keyAttrs);

        return newNode;
    }

    @Override
    public void execute() throws CommandExecutionException {
        try {
            ModelNode childNode = replaceChild();
            EditContainmentNode newEditData = m_editData;
            if (childNode != null) {
                newEditData = m_interceptor.processMissingData(newEditData, childNode);
            }
            delegateToChild(newEditData, childNode);
        } catch (EditConfigException | ModelNodeCreateException e1) {
            throw new CommandExecutionException(e1);
        }
    }

    protected void delegateToChild(EditContainmentNode editData, ModelNode childNode) throws EditConfigException {
        if (editData.getChangeNodes().size() > 0 || editData.getChildren().size() > 0) {
            editData.setEditOperation(EditConfigOperations.MERGE);
            EditContext context = new EditContext(editData, m_notificationContext, m_errorOption, m_clientInfo);
            childNode.editConfig(context);
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ReplaceChildCommand{");
        sb.append("m_editData=").append(m_editData);
        sb.append(", m_parentNode=").append(m_parentNode);
        sb.append(", m_childNode=").append(m_childNode);
        sb.append(", m_childContainerHelper=").append(m_childContainerHelper);
        sb.append(", m_interceptor=").append(m_interceptor);
        sb.append(", m_notificationContext=").append(m_notificationContext);
        sb.append(", m_errorOption='").append(m_errorOption).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
