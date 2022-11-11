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

import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.ActionRequest;
import org.broadband_forum.obbaa.netconf.api.server.NetconfQueryParams;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.ChangeTreeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.server.ssh.auth.AccessDeniedException;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.w3c.dom.Element;

/**
 * The subsystem is really where all the business logic associated with the configuration model lives.
 * A subsystem bridges the configuration to the associated system component.
 * <p>
 * <pre>For example:
 * A netconf router server can have 2 subsystems called  IpSubsystem and RoutingSubsystem.
 * IpSubsystem takes care of assigning IP and RoutingSubsystem takes care of routing logic.
 *
 * IpSubsystem registers itself to be notified of the changes related to IpConfiguration node via SubsystemRegistry.
 * Similarly RoutingSubsystem registers itself to be notified of the changes related to RoutingConfiguration node via SubsystemRegistry.
 *
 * If the netconf server receives changes to the IpConfiguration IpSubsystem is notified via notifyChanged().
 * If the netconf server receives changes to the RoutingConfiguration RoutingSubsystem is notified via notifyChanged().
 * </pre>
 *
 *
 */
public interface SubSystem {


    void setScope(String scope);

    /**
     * notify changes before data store commits the config changes.
     * Each subsystem that is affected by the change is called once with a list of change notifications
     *
     * @param changeNotificationList
     * @throws SubSystemValidationException - Exception the be thrown by subsystems when edit changes are not be committed into the data
     * store .
     *
     * @deprecated  Use {@link #preCommit(Map<SchemaPath, List<ChangeTreeNode>>)} instead
     */
    @Deprecated
    public void notifyPreCommitChange(List<ChangeNotification> changeNotificationList) throws SubSystemValidationException;

    /**
     * Will be called after data store commits the config changes.
     * Each subsystem that is affected by the change is called once with a list of change notifications
     *
     * @param changeNotificationList
     *
     * @deprecated  Use {@link #postCommit(Map<SchemaPath, List<ChangeTreeNode>>)} instead
     */
    @Deprecated
    public void notifyChanged(List<ChangeNotification> changeNotificationList);

    /**
     * notify changes before data store commits the config changes.
     *
     * Each subsystem that is affected by the change is called once with a Map having changed SchemaPaths
     * that the subsystem is interested in as keys and list of changeTreeNodes for that schemaPath as values.
     * Only one changeTreeNode entry per subtree Root will be maintained.
     *
     * @param changesMap
     * @throws SubSystemValidationException - Exception the be thrown by subsystems when edit changes are not be committed into the data
     * store .
     */
    void preCommit(Map<SchemaPath, List<ChangeTreeNode>> changesMap) throws SubSystemValidationException;

    /**
     * Will be called after data store commits the config changes.
     *
     * Each subsystem that is affected by the change is called once with a Map having changed SchemaPaths
     * that the subsystem is interested in as keys and list of changeTreeNodes for that schemaPath as values.
     * Only one changeTreeNode entry per subtree Root will be maintained.
     *
     * @param changesMap
     */
    void postCommit(Map<SchemaPath, List<ChangeTreeNode>> changesMap);

    /**
     * Retrieves state attributes and state data subtree
     * When subtree is state data, Subsystem provides complete subtree state data
     *
     * @param attributes  - contains list of state attributes QNames and state subtree filters of a specific ModelNodeId
     * @param queryParams - to restrict the output subtree for the given queryParams(depth).
     * @return Map<ModelNodeId, List<Element>> - Map of state attributes/subtrees elements of a specific ModelNodeId
     * @throws GetAttributeException
     */
    public Map<ModelNodeId, List<Element>> retrieveStateAttributes(Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> attributes,
                                                                   NetconfQueryParams queryParams) throws GetAttributeException;
    Map<ModelNodeId, List<Element>> retrieveStateAttributes(Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> attributes,
                                                                   NetconfQueryParams queryParams, StateAttributeGetContext stateContext) throws GetAttributeException;


    /**
     * This method will check validation data of EditContext object
     *
     * @param editContext
     * @param changeType
     * @param changedNode
     * @param modelNodeHelperRegistry
     * @throws SubSystemValidationException
     */
    public void testChange(EditContext editContext, ModelNodeChangeType changeType, ModelNode changedNode, ModelNodeHelperRegistry
            modelNodeHelperRegistry) throws SubSystemValidationException;

    /**
     * This method will handle changing data of EditContext object.
     * if this object have any changes, it will return true
     *
     * @param editContext
     * @param changeType
     * @param changedNode
     * @return
     */
    public boolean handleChanges(EditContext editContext, ModelNodeChangeType changeType, ModelNode changedNode);

    /**
     * Notification to subsystems after app has been deployed.
     */
    void appDeployed();

    /**
     * Notification to subsystems after app has been un-deployed.
     */
    void appUndeployed();
    
    public List<Element> executeAction(ActionRequest actionRequest) throws ActionException;

    public void checkRequiredPermissions(NetconfClientInfo clientInfo, String operation) throws AccessDeniedException;
    
    
    /**
     * Will be called after data store commits the config changes.
     *
     * A call to each subsystem that is affected by the change is made with a Map having changed SchemaPaths
     * that the subsystem is interested in as keys and list of changeTreeNodes for that schemaPath as values and the NC Extension 
     * that is used in the request.
     *
     * @param changesMap
     * @param extensionQName
     * @return NC Extensions Response Element
     */
	default public Element handleNcExtensions(Map<SchemaPath, List<ChangeTreeNode>> changesMap, QName extensionQName) {
		// Only implemented by subsystems that want to handle the NC Extensions used in request
		return null;
	}

	boolean isBypassingAuthorization();

    default boolean isNodePresent(SchemaPath schemaPath, ModelNodeId mnId){ return true; }
}
