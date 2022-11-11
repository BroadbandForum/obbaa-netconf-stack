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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.api.server.NetconfQueryParams;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.ChangeTreeNode;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.w3c.dom.Element;

public class CompositeSubSystemImpl extends AbstractSubSystem implements CompositeSubSystem {
    private Set<SubSystem> m_members = new LinkedHashSet<>();
    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(CompositeSubSystemImpl.class, LogAppNames.NETCONF_STACK);

    @Override
    public void register(SubSystem subSystem) {
        m_members.add(subSystem);
    }

    @Override
    public void unRegister(SubSystem subSystem) {
        m_members.remove(subSystem);
    }

    @Override
    public void preCommit(Map<SchemaPath, List<ChangeTreeNode>> changesMap) throws SubSystemValidationException {
        try {
            for (SubSystem member : m_members) {
                member.preCommit(changesMap);
            }
        } catch (SubSystemValidationException e) {
            LOGGER.error("error occurred in preCommit", e);
            throw new SubSystemValidationException(e.getRpcError());
        }
    }

    @Override
    public void postCommit(Map<SchemaPath, List<ChangeTreeNode>> changesMap) {

    }

    @Override
    public Map<ModelNodeId, List<Element>> retrieveStateAttributes(Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> attributes, NetconfQueryParams queryParams, StateAttributeGetContext stateContext) throws GetAttributeException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<ModelNodeId, List<Element>> retrieveStateAttributes(Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> attributes, NetconfQueryParams queryParams)
            throws GetAttributeException {
        throw new UnsupportedOperationException();
    }
}