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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.api.LogAppNames;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.ActionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.server.NetconfQueryParams;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.auth.spi.AuthorizationHandler;
import org.broadband_forum.obbaa.netconf.mn.fwk.ChangeTreeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.StateAttributeUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.NetconfRpcErrorUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.Operation;
import org.broadband_forum.obbaa.netconf.server.ssh.auth.AccessDeniedException;
import org.broadband_forum.obbaa.netconf.server.ssh.auth.Logical;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.w3c.dom.Element;

public abstract class AbstractSubSystem implements SubSystem {

    public static final AdvancedLogger DEBUG_LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(AbstractSubSystem.class, LogAppNames.USER_MANAGER);
    private boolean m_byPassAuthorization = byPassAuthorization();
    private static final String DEFAULT_PERMISSION = "NBI_RESIDUAL";
    private String m_permission =  DEFAULT_PERMISSION;
    private AuthorizationHandler m_authorizationHandler;

    public AbstractSubSystem() {}

    public AbstractSubSystem(AuthorizationHandler authorizationHandler) {
        m_authorizationHandler = authorizationHandler;
    }

    @Override
    public void setScope(String permission) {
        if (DEFAULT_PERMISSION.equals(m_permission)) {
            //only set permission per subsystem once
            m_permission = permission;
        }
    }

    @Override
    public void notifyPreCommitChange(List<ChangeNotification> changeNotificationList) throws SubSystemValidationException {
        
    }
    
    @Override
	public void testChange(EditContext editContext, ModelNodeChangeType changeType, ModelNode changedNode, ModelNodeHelperRegistry modelNodeHelperRegistry)
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
    public Map<ModelNodeId, List<Element>> retrieveStateAttributes(Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> attributes,
                                                                   NetconfQueryParams queryParams) throws GetAttributeException {
	    
	    Map<ModelNodeId, List<Element>> stateInfo = retrieveStateAttributes(attributes);
	    StateAttributeUtil.trimResultBelowDepth(stateInfo, queryParams);
        return stateInfo;
    }
	
    protected Map<ModelNodeId, List<Element>> retrieveStateAttributes(Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> attributes)
            throws GetAttributeException {
        return Collections.emptyMap();
    }

    protected boolean isAboveDepth(NetconfQueryParams params, int newNodeDepth) {
        return params.getDepth() == NetconfQueryParams.UNBOUNDED || 
                params.getDepth() > newNodeDepth;
    }

    @Override
    public Map<ModelNodeId, List<Element>> retrieveStateAttributes(Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> attributes,
                                                                   NetconfQueryParams queryParams, StateAttributeGetContext stateContext) throws GetAttributeException {
        if (stateContext != null) {
            Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> stateResponseNotInCache = new HashMap<>();
            Map<ModelNodeId, List<Element>> returnValues = new HashMap<>();
            stateContext.findAndPopulateStoredLeavesForSS(returnValues, stateResponseNotInCache, attributes, this);
            stateContext.findAndPopulateStoredFiltersForSS(returnValues, stateResponseNotInCache, attributes, this);
            if(!stateResponseNotInCache.isEmpty()){
                retrieveStateResponseNotInCache(queryParams, stateContext, stateResponseNotInCache, returnValues);
            }
            return returnValues;
        }else{
            return retrieveStateAttributes(attributes,queryParams);
        }
    }

    private void retrieveStateResponseNotInCache(NetconfQueryParams queryParams, StateAttributeGetContext stateContext, Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> stateResponseNotInCache, Map<ModelNodeId, List<Element>> returnValues) throws GetAttributeException {
        Map<ModelNodeId, List<Element>> stateValues = retrieveStateAttributes(stateResponseNotInCache,queryParams);
        if (!stateValues.isEmpty()) {
            stateContext.cacheStateResponse(stateValues, stateResponseNotInCache, this);
            for(Map.Entry<ModelNodeId, List<Element>> entry : stateValues.entrySet()){
                if(!entry.getValue().isEmpty()){
                    populateReturnValues(returnValues, entry.getKey(), entry.getValue());
                }
            }
        }
    }

    private void populateReturnValues(Map<ModelNodeId, List<Element>> returnValues, ModelNodeId mnId, List<Element> storedElement) {
        List<Element> valuesToReturn = returnValues.get(mnId);
        if(valuesToReturn == null){
            valuesToReturn = new ArrayList<>();
            returnValues.put(mnId, valuesToReturn);
        }
        valuesToReturn.addAll(storedElement);
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

    @Override
    public void preCommit(Map<SchemaPath, List<ChangeTreeNode>> changesMap) throws SubSystemValidationException {

    }

    @Override
    public void postCommit(Map<SchemaPath, List<ChangeTreeNode>> changesMap) {

    }

    public static String wrapNsAndRevision(String ns, String revision) {
        return "(" + ns + "?revision=" + revision + ")";
    }

    @Override
    public void checkRequiredPermissions(NetconfClientInfo clientInfo, String operation) throws AccessDeniedException {
        String currentSubSystem = this.getClass().getSimpleName();
        DEBUG_LOGGER.debug("Checking user permission at {}", currentSubSystem);
        if (m_byPassAuthorization || (clientInfo != null && clientInfo.isInternalUser())) {
            //by pass authorization check
            DEBUG_LOGGER.debug("By passing permission check at {}", currentSubSystem);
            return;
        }

        boolean isPermitted = false;
        Serializable sessionId = clientInfo.getClientSessionId();
        String username = clientInfo.getUsername();
        DEBUG_LOGGER.debug("Authorizing user '{}' with permission '{}' and logical '{}'", DEBUG_LOGGER.sensitiveData(username), DEBUG_LOGGER.sensitiveData(m_permission), Logical.AND);
        try {
            isPermitted = m_authorizationHandler.isPermittedAll(sessionId, m_permission);
        } catch (Exception e) {
            DEBUG_LOGGER.error("Error when checking permission at {}", currentSubSystem, e);
        }

        if (!isPermitted) {
            DEBUG_LOGGER.warn("User '{}' is not permitted to access '{}'", DEBUG_LOGGER.sensitiveData(username), currentSubSystem);
            throw getAccessDeniedException(username, operation);
        }
    }

    private AccessDeniedException getAccessDeniedException(String userName, String operation) throws AccessDeniedException {
        String message = String.format("Operation '%s' not authorized for user '%s'", operation, userName);
        NetconfRpcError rpcError = NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.ACCESS_DENIED, message);
        return new AccessDeniedException(rpcError);
    }

    protected boolean byPassAuthorization() {
        //return false; commented for obbaa
        return true;
    }

    @Override
    public boolean isBypassingAuthorization() {
        return m_byPassAuthorization;
    }
}
