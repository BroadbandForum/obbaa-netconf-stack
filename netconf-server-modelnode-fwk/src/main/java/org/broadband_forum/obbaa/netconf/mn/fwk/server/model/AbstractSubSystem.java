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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.api.messages.ActionRequest;
import org.broadband_forum.obbaa.netconf.api.server.NetconfQueryParams;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.StateAttributeUtil;
import org.opendaylight.yangtools.yang.common.QName;
import org.w3c.dom.Element;

public abstract class AbstractSubSystem implements SubSystem {

    @Override
    public void notifyPreCommitChange(List<ChangeNotification> changeNotificationList) throws
            SubSystemValidationException {

    }

    @Override
    public void testChange(EditContext editContext, ModelNodeChangeType changeType, ModelNode changedNode,
                           ModelNodeHelperRegistry modelNodeHelperRegistry)
            throws SubSystemValidationException {
    }

    @Override
    public boolean handleChanges(EditContext editContext, ModelNodeChangeType changeType, ModelNode changedNode) {
        return false;
    }

    @Override
    public void notifyChanged(List<ChangeNotification> changeNotificationList) {
    }

    @Override
    public Map<ModelNodeId, List<Element>> retrieveStateAttributes(Map<ModelNodeId, Pair<List<QName>,
            List<FilterNode>>> attributes,
                                                                   NetconfQueryParams queryParams) throws
            GetAttributeException {

        Map<ModelNodeId, List<Element>> stateInfo = retrieveStateAttributes(attributes);
        StateAttributeUtil.trimResultBelowDepth(stateInfo, queryParams);
        return stateInfo;
    }

    protected Map<ModelNodeId, List<Element>> retrieveStateAttributes(Map<ModelNodeId, Pair<List<QName>,
            List<FilterNode>>> attributes)
            throws GetAttributeException {
        return Collections.emptyMap();
    }

    protected boolean isAboveDepth(NetconfQueryParams params, int newNodeDepth) {
        return params.getDepth() == NetconfQueryParams.UNBOUNDED ||
                params.getDepth() > newNodeDepth;
    }

    @Override
    public void appDeployed() {

    }

    @Override
    public void appUndeployed() {

    }

    @Override
    public List<Element> executeAction(ActionRequest actionRequest) throws ActionException {
        return null;
    }
}
