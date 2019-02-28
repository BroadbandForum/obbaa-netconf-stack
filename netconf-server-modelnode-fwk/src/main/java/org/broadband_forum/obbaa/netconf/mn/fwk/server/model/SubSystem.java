package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.api.messages.ActionRequest;
import org.broadband_forum.obbaa.netconf.api.server.NetconfQueryParams;
import org.broadband_forum.obbaa.netconf.api.util.Pair;

import org.opendaylight.yangtools.yang.common.QName;
import org.w3c.dom.Element;

import java.util.List;
import java.util.Map;

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


    /**
     * notify changes before data store commits the config changes.
     * Each subsystem that is affected by the change is called once with a list of change notifications
     *
     * @param changeNotificationList
     * @throws SubSystemValidationException - Exception the be thrown by subsystems when edit changes are not be committed into the data
     * store .
     */
    public void notifyPreCommitChange(List<ChangeNotification> changeNotificationList) throws SubSystemValidationException;

    /**
     * Will be called after data store commits the config changes.
     * Each subsystem that is affected by the change is called once with a list of change notifications
     *
     * @param changeNotificationList
     */
    public void notifyChanged(List<ChangeNotification> changeNotificationList);

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
}