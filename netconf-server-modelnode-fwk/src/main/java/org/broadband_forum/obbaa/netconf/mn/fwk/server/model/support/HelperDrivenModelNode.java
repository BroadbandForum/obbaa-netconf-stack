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

import static org.broadband_forum.obbaa.netconf.api.util.CryptUtil2.ENCR_STR_PATTERN;
import static org.broadband_forum.obbaa.netconf.api.util.NetconfResources.IS_FULL_DS_REBUILT;
import static org.broadband_forum.obbaa.netconf.api.util.SchemaPathUtil.isRootSchemaPath;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.commands.CommandUtils.addVisibilityContext;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.commands.CommandUtils.appendKeyCTN;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.processing.ModelNodeConstraintProcessor.getMatchCriteria;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.StateAttributeUtil.getFilteredElements;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.EditTreeTransformer.getLocalName;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.EditTreeTransformer.resolveLocalName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigOperations;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorInfo;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorSeverity;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorType;
import org.broadband_forum.obbaa.netconf.api.server.NetconfQueryParams;
import org.broadband_forum.obbaa.netconf.api.util.CryptUtil2;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.api.utils.SystemPropertyUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.ChangeTreeNode.ChangeType;
import org.broadband_forum.obbaa.netconf.mn.fwk.ChangeTreeNodeImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.WritableChangeTreeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaMountRegistryProvider;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.BitsTypeUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.ChoiceCaseNodeUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.AnvExtensions;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ChangeCommand;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.Command;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.CommandExecutionException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ConfigAttributeGetContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.CopyConfigException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditChangeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditChangeSource;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditConfigException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditMatchNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterMatchNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FlagForRestPutOperations;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetConfigContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeChange;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeChangeType;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeCreateException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeInterceptor;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.StateAttributeGetContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemValidationException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.dom.EncryptDecryptUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.commands.CompositeEditCommand;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.commands.CreateContainerCommand;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.commands.CreateListCommand;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.commands.DeleteContainerCommand;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.commands.DeleteLeafCommand;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.commands.DeleteLeafListCommand;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.commands.DeleteListCommand;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.commands.MergeListCommand;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.commands.NotificationDecorator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.commands.ReplaceContainerCommand;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.commands.ReplaceListCommand;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.commands.SetConfigAttributeCommand;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.commands.SubSystemValidationDecorator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.processing.ModelNodeConstraintProcessor;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.TimingLogger;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.utils.ReplaceMNUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.NetconfRpcErrorUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.Operation;
import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.broadband_forum.obbaa.netconf.server.RequestTask;
import org.broadband_forum.obbaa.netconf.server.ssh.auth.AccessDeniedException;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A Abstract modelNode implementation that uses a set of Helpers to perform its job.
 * For Example ConfigAttributeHelper s help in getting the config attributes related to the node.
 */
public abstract class HelperDrivenModelNode implements ModelNode {

    protected static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(HelperDrivenModelNode.class, LogAppNames.NETCONF_STACK);

    private static final String MODELNODEID_GET_ERROR="Error while getting ModelNodeId for ";
    public static final String MANDATORY_ATTRIBUTE_VALIDATION_ENABLED = "mandatoryAttributeValidationEnabled";
    private static final String DEVICE_ADAPTERS = "device-adapters";

    private static final long THREE_MINUTES_IN_MILLIS = 3 * 60 * 1000;

    private final ModelNode m_parent;


    private ModelNodeHelperRegistry m_modelNodeHelperRegistry = null;

    private SubSystemRegistry m_subSystemRegistry;
    private ModelNodeId m_parentNodeId;
    private ModelNodeId m_modelNodeId;
    private ModelNodeInterceptor m_interceptor;
    private ModelNodeDataStoreManager m_modelNodeDSM;

    private SchemaRegistry m_schemaRegistry;

    protected Boolean m_schemaMountNode;

    protected ModelNodeHelperRegistry m_mountModelNodeHelperRegistry;

    protected boolean m_schemaMountChild = false;

    protected SchemaPath m_parentMountPath;

    protected boolean m_visibility = true;

    public boolean isVisible() {
        return m_visibility;
    }

    public void setVisibility(boolean visibility) {
        this.m_visibility = visibility;
    }

    private static final String ENV_GET_TIMEOUT_THRESHOLD = "GET_TIMEOUT_THRESHOLD";

    private static long c_getTimeoutThreshold = THREE_MINUTES_IN_MILLIS;

    static {
        String timeOutValue = System.getenv(ENV_GET_TIMEOUT_THRESHOLD);
        if (timeOutValue != null && timeOutValue.trim().length() > 0) {
            try {
                c_getTimeoutThreshold = Long.parseLong(timeOutValue);
                LOGGER.info("Get Timeout Configured - " + c_getTimeoutThreshold + " milliseconds");
            } catch (NumberFormatException e) {
                LOGGER.info("Problem parsing Get Timeout; Using default of 3 Minutes");
            }
        } else {
            LOGGER.info("Get Timeout Not Configured; Using default of 3 Minutes");
        }
    }

    public static long timeoutGetOperationInMillis() {
        return c_getTimeoutThreshold;
    }

    public HelperDrivenModelNode(ModelNode parent, ModelNodeId parentNodeId, ModelNodeHelperRegistry modelNodeHelperRegistry,
            SubSystemRegistry subSystemRegistry, SchemaRegistry schemaRegistry, ModelNodeDataStoreManager modelNodeDSM) {
        this.m_parent = parent;
        this.m_parentNodeId = parentNodeId;
        if (modelNodeHelperRegistry != null) {
            this.m_modelNodeHelperRegistry = modelNodeHelperRegistry.unwrap();
        }
        if (subSystemRegistry != null) {
            m_subSystemRegistry = subSystemRegistry.unwrap();
        }
        m_interceptor = ModelNodeInterceptorChain.getInstance().createInterceptor();
        if (schemaRegistry != null) {
            m_schemaRegistry = schemaRegistry.unwrap();
        }
        m_modelNodeDSM = modelNodeDSM;
    }

    public ModelNodeId getParentNodeId() {
        return m_parentNodeId;
    }

    @Override
    public ModelNode getParent(){
        return m_parent;
    }

    @Override
    public ModelNodeDataStoreManager getModelNodeDSM(){
        return m_modelNodeDSM;
    }

    private void readMountPointValue() {
        if (m_schemaMountNode == null) {
            DataSchemaNode dataNode = m_schemaRegistry.getDataSchemaNode(getModelNodeSchemaPath());
            m_schemaMountNode = dataNode != null && AnvExtensions.MOUNT_POINT.isExtensionIn(dataNode);
        }
    }

    public SchemaRegistry getMountRegistry() throws GetException{
        readMountPointValue();

        if (m_schemaMountNode) {
            // If Schema mount is supported by this node, we return the specific registry if any, else we return the global registry
            SchemaMountRegistryProvider provider = m_schemaRegistry.getMountRegistry().getProvider(getModelNodeSchemaPath());
            if (provider != null) {
                SchemaRegistry registry = provider.getSchemaRegistry(getModelNodeId());
                if (registry != null) {
                    return registry.unwrap();
                }
            }
        }
        return m_schemaRegistry;
    }

    public SchemaRegistry getSchemaRegistryForCurrentNode() {
        return m_schemaRegistry;
    }

    @Override
    public SubSystemRegistry getMountSubSystemRegistry() {
        readMountPointValue();

        if (m_schemaMountNode) {
            // If Schema mount is supported by this node, we return the specific registry if any, else we return the global registry
            SchemaMountRegistryProvider provider = m_schemaRegistry.getMountRegistry().getProvider(getModelNodeSchemaPath());
            if (provider != null) {
                SubSystemRegistry registry = provider.getSubSystemRegistry(getModelNodeId());
                if (registry != null) {
                    return registry;
                }
            }
        }
        return m_subSystemRegistry;
    }

    @Override
    public SchemaRegistry getSchemaRegistry() {
        return getMountRegistry();
    }

    @Override
    public ModelNodeHelperRegistry getMountModelNodeHelperRegistry() {
        readMountPointValue();

        if (m_schemaMountNode) {
            if (m_mountModelNodeHelperRegistry != null) {
                return m_mountModelNodeHelperRegistry;
            }
            //if schema mount is supported for this node, we return the specific registry if any, else we return the global registry
            SchemaMountRegistryProvider provider = m_schemaRegistry.getMountRegistry().getProvider(getModelNodeSchemaPath());
            if (provider != null && provider.getModelNodeHelperRegistry(getModelNodeId())!= null) {
                m_modelNodeHelperRegistry = provider.getModelNodeHelperRegistry(getModelNodeId());
                m_mountModelNodeHelperRegistry = m_modelNodeHelperRegistry;
            }
        }

        return m_modelNodeHelperRegistry;
    }

    public ModelNodeHelperRegistry getModelNodeHelperRegistry(){
        return getMountModelNodeHelperRegistry();
    }

    @Override
    public String getContainerName() {
        return this.getModelNodeSchemaPath().getLastComponent().getLocalName();
    }

    public SchemaPath getSubSystemId() {
        return getModelNodeSchemaPath();
    }

    /**
     * By default there is no subsystem associated, so returning a dummy
     */
    @Override
    public SubSystem getSubSystem() {
        return getSubSystemRegistry().lookupSubsystem(getSubSystemId());
    }

    @Override
    public SubSystem getSubSystem(SchemaPath schemaPath){
        return getSubSystemRegistry().lookupSubsystem(schemaPath);
    }

    protected void addXmlValue(Document document, Element parent, ConfigLeafAttribute value, boolean isKeyAttribute) {
        if(!isKeyAttribute && value == null) {
            return;
        }
        parent.appendChild(document.importNode(value.getDOMValue(),true));
    }


    @Override
    public Element getConfig(GetConfigContext getConfigContext, NetconfQueryParams restCcontext) throws GetException {
        checkForGetTimeOut();
        try {
            if (hasSchemaMount()) {
                RequestScope.getCurrentScope().putInCache(SchemaRegistryUtil.MOUNT_PATH, getModelNodeSchemaPath());
            }
            return createGetResponse(getConfigContext.getClient(), getConfigContext.getDoc(), getConfigContext.getFilter(), getModelNodeId(), false, null, getConfigContext.getConfigGetContext(), restCcontext);
        } catch (GetAttributeException e) {
            LOGGER.error(MODELNODEID_GET_ERROR+this, e);
            GetException exception = new GetException(NetconfRpcErrorUtil.getNetconfRpcErrorForModelNodeId(getModelNodeId(),NetconfRpcErrorTag.OPERATION_FAILED,MODELNODEID_GET_ERROR+this+" " +e.getMessage(), getSchemaRegistry()));
            exception.addSuppressed(e);
            throw exception;
        }
    }

    @Override
    public Element get(GetContext getContext, NetconfQueryParams params) throws GetException {
        checkForGetTimeOut();
        try {
            if (hasSchemaMount()) {
                RequestScope.getCurrentScope().putInCache(SchemaRegistryUtil.MOUNT_PATH, getModelNodeSchemaPath());
            }
            return createGetResponse(getContext.getClient(), getContext.getDoc(), getContext.getFilter(), getModelNodeId(), true,
                    getContext.getStateAttributeContext(), getContext.getConfigGetContext(), params);
        } catch (GetAttributeException e) {
            LOGGER.error(MODELNODEID_GET_ERROR+this, e);
            GetException exception = new GetException(NetconfRpcErrorUtil.getNetconfRpcErrorForModelNodeId(getModelNodeId(),NetconfRpcErrorTag.OPERATION_FAILED,MODELNODEID_GET_ERROR+this+" " +e.getMessage(), getSchemaRegistry()));
            exception.addSuppressed(e);
            throw exception;
        }

    }

    public static void checkForGetTimeOut() throws GetException {
        RequestScope currentScope = RequestScope.getCurrentScope();
        String reqType = (String) currentScope.getFromCache(RequestTask.CURRENT_REQ_TYPE);
        if (RequestTask.REQ_TYPE_GET.equals(reqType) || RequestTask.REQ_TYPE_GET_CONFIG.equals(reqType)) {
            long startTime = (long) currentScope.getFromCache(RequestTask.CURRENT_REQ_START_TIME);
            long diff = System.currentTimeMillis() - startTime;
            LOGGER.debug("MessageID {} is running for {} milliseconds", currentScope.getFromCache(RequestTask.CURRENT_REQ_MESSAGE_ID), diff);
            if (diff > c_getTimeoutThreshold) {
                throw new GetException(new NetconfRpcError(NetconfRpcErrorTag.OPERATION_FAILED,
                        NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error, "Operation Timed Out"));
            }
        }
    }

    @Override
    public abstract QName getQName();



    protected Element createGetResponse(NetconfClientInfo clientInfo, Document doc, FilterNode filter, ModelNodeId modelNodeId, boolean includeState,
                                        StateAttributeGetContext stateContext, ConfigAttributeGetContext configContext, NetconfQueryParams params) throws GetException, DOMException, GetAttributeException {
        // check permission for subtree first
        SchemaPath schemaPath = this.getModelNodeSchemaPath();
        SubSystem subSystem = m_subSystemRegistry.lookupSubsystem(schemaPath);
        boolean subSystemAuthorized = configContext.isSubSystemAuthorized(subSystem);
        if (!subSystemAuthorized && subSystem != null) {
            try {
                subSystem.checkRequiredPermissions(clientInfo, Operation.GET.getType());
                configContext.addAuthorizedSubSystem(subSystem);
            } catch (AccessDeniedException e) {
                LOGGER.warn(null, "User '{}' is not permitted to access SchemaPath '{}' from subsystem '{}'", LOGGER.sensitiveData(clientInfo.getUsername()), schemaPath, subSystem);
                return null;
            }
        }

        // create the container, at this stage we are not sure yet whether that needs to go in
        String localName = null;
        boolean match = false;
        String containerName = getContainerName();

        String namespace = getQName().getNamespace().toString();

        localName = resolveLocalName(getSchemaRegistry(), namespace, containerName);

        Element parent = doc.createElementNS(namespace, localName);
        LOGGER.trace("createGetResponse: " + this + ", filter =\n" + filter);

        DataSchemaNode schemaNode = getSchemaRegistry().getDataSchemaNode(getModelNodeSchemaPath());
        if ((schemaNode != null) && (!schemaNode.isConfiguration()) && (!includeState)) {
            return null;
        }
        // now need to check the filter
        if (filter == null || filter.isEmpty()) {
            LOGGER.trace("no filter, copying everything to output");
            // this means everything ...
            try {
                copyCompletelyToOutput(clientInfo, doc, modelNodeId, parent, includeState, stateContext, configContext, params);
            } catch (DOMException | GetAttributeException e) {
                LOGGER.error("cannot create get response", e);
            }
            return parent;
        } else {
            //Has select node for this container, then copy complete
            FilterNode selectNode = filter.getSelectNode(getContainerName(), getQName().getNamespace().toString());
            if (selectNode != null) {
                LOGGER.trace("select node: {}, copying everything to output", selectNode.getNodeName());
                try {
                    copyCompletelyToOutput(clientInfo, doc, modelNodeId, parent, includeState, stateContext, configContext, params);
                } catch (DOMException | GetAttributeException e) {
                    LOGGER.debug("cannot create get response", e);
                }
            } else if (filter.hasContainmentNode(getContainerName())) {
                LOGGER.trace("containment node: {}, next level evaluation", getContainerName());
                // process deeper

                List<FilterNode> childFilters = filter.getContainmentNodes(getContainerName());

                for (FilterNode cn : childFilters) {
                    boolean thisMatch = copyFilteredToOutput(clientInfo, doc, parent, modelNodeId, cn, includeState,
                            stateContext, configContext, params);
                    match = match || thisMatch;
                }
            }

            if (parent.hasChildNodes() || (selectNode != null && parent.getLocalName().equals(selectNode.getNodeName())) || match) {
                return parent;
            }
            return null;
        }
    }

    private boolean isAboveDepth(NetconfQueryParams params) {
        return params.getDepth() == NetconfQueryParams.UNBOUNDED || params.getDepth()-1 > m_modelNodeId.getDepth();
    }

    protected boolean isAboveDepth(NetconfQueryParams params, ModelNodeId childNodeId) {
        if ( isAboveDepth(params)){
            return true;
        } else {
            Map<String, List<QName>> fields = params.getFields();
            if ( fields.isEmpty()){
                return false;
            }

            List<ModelNodeRdn> relativeRdns = childNodeId.getRdnsReadOnly().subList(params.getDepth(), childNodeId.getRdnsReadOnly().size());
            for ( ModelNodeRdn rdn : relativeRdns){
                if (  ModelNodeRdn.CONTAINER.equals(rdn.getRdnName()) && !fields.containsKey(rdn.getRdnValue())){
                    return false;
                }
            }
            return true;
        }
    }

    private boolean isAboveDepth(NetconfQueryParams params, QName attributeName) {
        if ( isAboveDepth(params)){
            return true;
        } else {
            Map<String, List<QName>> fields = params.getFields();
            if ( fields.containsKey(this.getContainerName())){
                if( fields.get(this.getContainerName()).contains(attributeName)){
                    return true;
                }
            }
            return false;
        }
    }


    private void filterStateAttributesAboveDepth(NetconfQueryParams params, Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> map) {

        Pair<List<QName>, List<FilterNode>> entry = map.get(m_modelNodeId);
        if (entry != null) {
            List<QName> stateAttributes = entry.getFirst();
            Iterator<QName> iterator = stateAttributes.iterator();
            while ( iterator.hasNext()){
                QName attributeName = iterator.next();
                if ( !isAboveDepth(params, attributeName)){
                    iterator.remove();
                }
            }
        }
    }

    /**
     * This method evaluates get/get-config filters.
     * The algorithm is as follows
     * 1.	Evaluate match conditions at the current node
     *        a.	If the match conditions evaluate to true
     *          i.	Then evaluate state match conditions and state child filters (only in case of get)
     *          1.	If state conditions evaluate to true
     *            a.	Evaluate child filters
     * if child filter evaluation was success, the start populating response
     *
     * @param doc
     * @param parent
     * @param modelNodeId
     * @param filter
     * @param includeState
     * @throws GetException
     * @throws GetAttributeException
     * @throws DOMException
     */
    private boolean copyFilteredToOutput(NetconfClientInfo clientInfo, Document doc, Element parent, ModelNodeId modelNodeId, FilterNode filter, boolean includeState,
            StateAttributeGetContext stateContext, ConfigAttributeGetContext configContext, NetconfQueryParams params) throws GetException, DOMException,
    GetAttributeException {
        // we are in the right container, first verify the match nodes ....
        LOGGER.trace("copyFilteredToOutput " + this + ", filter=n" + filter);

        boolean proceedFurther = true;

        List<FilterMatchNode> stateFilterMatchNodes = new ArrayList<>();
        proceedFurther = evaluateMatchNodes(filter, includeState, proceedFurther, stateFilterMatchNodes);

        List<FilterNode> stateSubtreeFilterNodes = new ArrayList<>();
        List<FilterNode> configSubtreeFilterNodes = new ArrayList<>();
        List<Element> stateElements = new ArrayList<>();

        if (proceedFurther) {
            populateConfigAndStateChildSubtreeFilterNodes(filter, stateSubtreeFilterNodes, configSubtreeFilterNodes);
            proceedFurther = evaluateStateConditions(modelNodeId, filter, stateContext, stateFilterMatchNodes,
                    stateSubtreeFilterNodes, stateElements);
            if (proceedFurther) {
                if ((filter.getSelectNodes().isEmpty()) && (filter.getChildNodes().isEmpty())) {
                    copyCompletelyToOutput(clientInfo, doc, modelNodeId, parent, includeState, stateContext, configContext, params);
                } else {
                    LOGGER.trace("copyFilteredToOutput: proceedFurther!");
                    List<QName> stateAttributeQNames = new ArrayList<>();

                    boolean childHasMatchCondition = filter.childHasMatchCondition();
                    List<Element> childElements = new ArrayList<>();
                    evaluateChildNodeConditions(clientInfo, doc, filter, includeState, stateContext, configContext, params, childElements);

                    if (childHasMatchCondition && childElements.isEmpty() && stateElements.isEmpty() && filter.getSelectNodes().isEmpty()
                            && filter.getMatchNodes().isEmpty()) {
                        return false;
                    }

                    populateResponse(clientInfo, doc, parent, modelNodeId, filter, includeState, stateContext, configContext, params,
                            stateSubtreeFilterNodes, stateAttributeQNames, childElements);

                    if(stateAttributeQNames.isEmpty() && stateSubtreeFilterNodes.isEmpty() && !isConfigDataPopulated(filter, parent, params)) {
                        removeChildren(parent);
                        return false;
                    }
                }

            } else {
                LOGGER.trace("copyFilteredToOutput: NO match!");
            }
        }

        return proceedFurther;
    }

    private void removeChildren(Node node) {
        while (node.hasChildNodes()) {
            node.removeChild(node.getFirstChild());
        }
    }

    private boolean isConfigDataPopulated(FilterNode filter, Element element, NetconfQueryParams params) {
        if (filter.getNodeName().equals(element.getLocalName()) && filter.getNamespace().equals(element.getNamespaceURI())) {
            if(!isAboveDepth(params)) {
                return true;
            }
            NodeList childNodes = element.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node currentNode = childNodes.item(i);
                if (currentNode.getNodeType() == Node.ELEMENT_NODE){
                    String localName = currentNode.getLocalName();
                    String namespaceURI = currentNode.getNamespaceURI();

                    if(filter.getMatchNodes().stream()
                            .anyMatch(matchNode -> matchNode.getNodeName().equals(localName)
                                    && matchNode.getNamespace().equals(namespaceURI)
                                    && matchNode.getFilter().equals(currentNode.getTextContent()))){
                        return true;
                    }

                    if(filter.getSelectNodes().stream()
                            .anyMatch(selectNode -> selectNode.getNodeName().equals(localName)
                                    && selectNode.getNamespace().equals(namespaceURI))){
                        return true;
                    }

                    if(filter.getChildNodes().stream()
                            .anyMatch(childNode -> childNode.getNodeName().equals(localName)
                                    && childNode.getNamespace().equals(namespaceURI))){
                        return true;
                    }
                }
            }
            return false;
        }
        return true;
    }

    private void populateResponse(NetconfClientInfo clientInfo, Document doc, Element parent, ModelNodeId modelNodeId, FilterNode filter, boolean includeState, StateAttributeGetContext stateContext,
                                  ConfigAttributeGetContext configContext, NetconfQueryParams params, List<FilterNode> stateSubtreeFilterNodes,
            List<QName> stateAttributeQNames, List<Element> childElements) throws GetAttributeException, GetException {
        ArrayList<String> keyValues = new ArrayList<String>();
        //copy keys first refer RFC 6020 section 7.8.5
        copyKeyAttribute(doc, parent, modelNodeId, keyValues, params);

        //Copy the match conditions also to output
        for (FilterMatchNode fmn : filter.getMatchNodes()) {
            if (!(keyValues.contains(fmn.getNodeName())) ) {
                QName qName = getSchemaRegistry().lookupQName(fmn.getNamespace(), fmn.getNodeName());
                qName = getQNameFromMountRegistry(filter, qName);
                if (params.isIncludeConfig() && qName != null) {
                    if (getModelNodeHelperRegistry().getConfigAttributeHelpers(this.getModelNodeSchemaPath()).containsKey(qName)) {
                        copyAttributeToOutput(doc, parent, qName, modelNodeId);
                    } else if (getModelNodeHelperRegistry().getConfigLeafListHelper(this.getModelNodeSchemaPath(), qName) != null) {
                        copyLeafListFilteredToOutput(doc, parent, qName, fmn.getFilter(), modelNodeId);
                    }
                }
            }
        }
        if (isAboveDepth(params, this.getModelNodeId())) {
            //copy child elements
            for(Element childElement : childElements){
                parent.appendChild(childElement);
            }
        }

        // select need to be copied to the output for sure, they can be
        // direct attributes or containment nodes
        for (FilterNode fsn : filter.getSelectNodes()) {
            try {
                if (!(keyValues.contains(fsn.getNodeName()))) {
                    // a containment node can refer both to a child
                    // container or a list
                    QName qName = getSchemaRegistry().lookupQName(fsn.getNamespace(), fsn.getNodeName());
                    qName = getQNameFromMountRegistry(fsn, qName);
                    if (qName != null) {
                        if (getModelNodeHelperRegistry().getChildListHelpers(this.getModelNodeSchemaPath()).containsKey(qName)) {
                            Collection<ModelNode> children = getChildModelNodes(qName, fsn.getMatchNodes());
                            for (ModelNode child : children) {
                                if(child.isVisible()) {
                                    Element elemChild = delegateToChild(clientInfo, doc, filter, includeState, child, stateContext, configContext, params);
                                    appendChildIfNotNull(elemChild, parent);
                                }
                            }
                        } else if (getModelNodeHelperRegistry().getChildContainerHelpers(this.getModelNodeSchemaPath()).containsKey(qName)) {
                            ModelNode child = getChildModelNode(qName);
                            if (child != null && child.isVisible()) {
                                Element elemChild = delegateToChild(clientInfo, doc, filter, includeState, child, stateContext, configContext, params);
                                appendChildIfNotNull(elemChild, parent);
                            }
                        } else if (includeState && SchemaRegistryUtil.getStateChildLists(this, getModelNodeSchemaPath()).contains(qName)) {
                            stateSubtreeFilterNodes.add(fsn);
                        } else if (includeState && SchemaRegistryUtil.getStateChildContainers(this, getModelNodeSchemaPath()).contains(qName)) {
                            stateSubtreeFilterNodes.add(fsn);
                        } else if (params.isIncludeConfig() && getModelNodeHelperRegistry().getConfigAttributeHelpers(this.getModelNodeSchemaPath()).containsKey(qName)) {
                            copyAttributeToOutput(doc, parent, qName, modelNodeId);
                        } else if (includeState && SchemaRegistryUtil.getStateAttributes(getModelNodeSchemaPath(), getSchemaRegistry()).contains(qName)) {
                            if (!isKeyAttribute(qName)) {
                                stateAttributeQNames.add(qName);
                            }
                        } else if (params.isIncludeConfig() && getModelNodeHelperRegistry().getConfigLeafListHelpers(this.getModelNodeSchemaPath()).containsKey(qName)) {
                            copyLeafListToOutput(doc, parent, qName, modelNodeId);
                        } else if (includeState && SchemaRegistryUtil.getStateLeafListAttributes(getModelNodeSchemaPath(), getSchemaRegistry()).contains(qName)) {
                            stateAttributeQNames.add(qName);
                        } else {
                            LOGGER.debug("the node {} is not a config node", fsn.getNodeName());
                        }
                    }
                }
            } catch (ModelNodeGetException | GetAttributeException e) {
                LOGGER.error("could not retrieve children {}",fsn.getNodeName(), e);
            }
        }

        if (!stateAttributeQNames.isEmpty() || !stateSubtreeFilterNodes.isEmpty()) {
            if (!stateSubtreeFilterNodes.isEmpty() && stateSubtreeFilterNodes.get(0).getNodeName().equals(DEVICE_ADAPTERS)) {
                SchemaRegistry schemaRegistry = getSchemaRegistry();
                DataSchemaNode node = schemaRegistry.getDataSchemaNode(getModelNodeSchemaPath());
                if (node instanceof DataNodeContainer) {
                    if (AnvExtensions.MOUNT_POINT.isExtensionIn(node)) {
                        schemaRegistry = getMountRegistry();
                    }
                }

                Collection<DataSchemaNode> childrenSchemaNodes = schemaRegistry.getChildren(getModelNodeSchemaPath());
                for (DataSchemaNode schemaNode : childrenSchemaNodes) {
                    addStateAttributesIfNotExist(stateContext, getSubSystem(schemaNode.getPath()), modelNodeId,
                            new ArrayList<>(stateAttributeQNames), stateSubtreeFilterNodes, params);
                }
                addStateAttributesIfNotExist(stateContext, getSubSystem(), modelNodeId, stateAttributeQNames, stateSubtreeFilterNodes, params);
            } else {
                populateStateContextPerSubSystem(stateAttributeQNames,stateSubtreeFilterNodes,
                        getModelNodeSchemaPath(),stateContext,getModelNodeId(),params);
            }
        }
    }

    private QName getQNameFromMountRegistry(FilterNode fsn, QName qName) {
        if(qName == null && fsn.isMountPointImmediateChild()){
            qName = getMountRegistry().lookupQName(fsn.getNamespace(), fsn.getNodeName());
        }
        return qName;
    }

    private void evaluateChildNodeConditions(NetconfClientInfo clientInfo, Document doc, FilterNode filter, boolean includeState,
            StateAttributeGetContext stateContext, ConfigAttributeGetContext configContext, NetconfQueryParams params, List<Element> childElements) throws GetException {
        // containment nodes need further evaluation
        for (FilterNode childFilter : filter.getChildNodes()) {
            try {
                // a containment node can refer both to a child
                // container or a list
                QName qName = getSchemaRegistry().lookupQName(childFilter.getNamespace(), childFilter.getNodeName());
                qName = getQNameFromMountRegistry(childFilter, qName);
                if (qName != null) {
                    if ( getModelNodeHelperRegistry().getChildListHelpers(this.getModelNodeSchemaPath()).containsKey(qName)) {
                        Collection<ModelNode> children = getChildModelNodes(qName, childFilter.getMatchNodes());
                        for (ModelNode child : children) {
                            if(child.isVisible()) {
                                Element elemChild = delegateToChild(clientInfo, doc, filter, includeState, child, stateContext, configContext, params);
                                if (elemChild != null) {
                                    childElements.add(elemChild);
                                }
                            }
                        }
                    } else if ( getModelNodeHelperRegistry().getChildContainerHelpers(this.getModelNodeSchemaPath()).containsKey(qName)) {
                        ModelNode child = getChildModelNode(qName);
                        if (child != null && child.isVisible()) {
                            Element elemChild = delegateToChild(clientInfo, doc, filter, includeState, child,
                                    stateContext, configContext, params);
                            if(elemChild != null) {
                                childElements.add(elemChild);
                            }
                        }
                    }else {
                        LOGGER.debug("the node {} is not a config node", childFilter.getNodeName());
                    }
                }
            } catch (DOMException | ModelNodeGetException e) {
                LOGGER.error("could not retrieve children {}", childFilter.getNodeName(), e);
            }
        }
    }

    /**
     * we should evaluate state conditions if atleast one of the following is true
     * 1. if there is a state leaf match condition (that is stateFilterMatchNodes is not empty)
     * 2. if there is atleast one stateSubtreeFilterNode which has a child match condition
     * @param modelNodeId
     * @param filter
     * @param stateContext
     * @param stateFilterMatchNodes
     * @param stateSubtreeFilterNodes
     * @param stateElements
     * @return
     * @throws GetAttributeException
     */
    private boolean evaluateStateConditions(ModelNodeId modelNodeId, FilterNode filter, StateAttributeGetContext stateContext,
            List<FilterMatchNode> stateFilterMatchNodes, List<FilterNode> stateSubtreeFilterNodes, List<Element> stateElements) throws GetAttributeException {
        populateStateElementsAndContext(modelNodeId, stateContext, stateFilterMatchNodes, stateSubtreeFilterNodes, stateElements, filter);
        // state leaf/leaf-list
        if (!stateFilterMatchNodes.isEmpty()) {
            return isAllStateMatchNodesPopulated(stateFilterMatchNodes, stateElements);
        }
        return true;
    }

    private void populateConfigAndStateChildSubtreeFilterNodes(FilterNode filter, List<FilterNode> stateSubtreeFilterNodes, List<FilterNode> configSubtreeFilterNodes) {
        for (FilterNode childFilter : filter.getChildNodes()) {
            QName qName = getSchemaRegistry().lookupQName(childFilter.getNamespace(), childFilter.getNodeName());
            if (SchemaRegistryUtil.getStateChildContainers(this, getModelNodeSchemaPath()).contains(qName)) {
                stateSubtreeFilterNodes.add(childFilter);
            } else if (SchemaRegistryUtil.getStateChildLists(this, getModelNodeSchemaPath()).contains(qName)) {
                stateSubtreeFilterNodes.add(childFilter);
            } else {
                configSubtreeFilterNodes.add(childFilter);
            }
        }
    }

    private boolean isAllStateMatchNodesPopulated(List<FilterMatchNode> stateFilterMatchNodes, List<Element> stateElements) {
        if (!stateElements.isEmpty()) {
            for (FilterMatchNode fmn : stateFilterMatchNodes) {
                boolean matchingElementExists = false;
                for (Element element : stateElements) {
                    if (element.getLocalName().equals(fmn.getNodeName()) && element.getNamespaceURI().equals(fmn.getNamespace())
                            && element.getTextContent().equals(fmn.getFilter())) {
                        matchingElementExists = true;
                        break;
                    }
                }
                if (!matchingElementExists) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private void populateStateContext(ModelNodeId modelNodeId, FilterNode filter, StateAttributeGetContext stateContext,
            List<FilterMatchNode> stateFilterMatchNodes, List<Element> stateElements) {
        if ((filter.getSelectNodes().isEmpty()) && (filter.getChildNodes().isEmpty())) {
            stateContext.getStateMatchNodes().put(modelNodeId, stateElements);
        } else {
            // need to get filtered elements based on FilterMatchNodes (for leaf-list)
            List<Element> filteredElements = getFilteredElements(stateFilterMatchNodes, stateElements);
            stateContext.getStateMatchNodes().put(modelNodeId, filteredElements);
        }
    }

    private void populateStateElementsAndContext(ModelNodeId modelNodeId, StateAttributeGetContext stateContext, List<FilterMatchNode> stateFilterMatchNodes,
            List<FilterNode> stateSubtreeFilterNodes, List<Element> stateElements, FilterNode filter) throws GetAttributeException {
        stateElements.addAll(FilterUtil.checkAndGetStateFilterElements(stateContext, stateFilterMatchNodes,
                stateSubtreeFilterNodes, modelNodeId, this.getModelNodeSchemaPath(), getSchemaRegistry(), getSubSystemRegistry()));
        if(!stateElements.isEmpty()) {
            populateStateContext(modelNodeId, filter, stateContext, stateFilterMatchNodes, stateElements);
        }
    }

    private boolean evaluateMatchNodes(FilterNode filter, boolean includeState, boolean proceedFurther, List<FilterMatchNode> stateFilterMatchNodes) {
        if (!filter.getMatchNodes().isEmpty()) {
            for (FilterMatchNode fmn : filter.getMatchNodes()) {
                LOGGER.trace("evaluating " + fmn);
                try {
                    QName fmnQname = getSchemaRegistry().lookupQName(fmn.getNamespace(), fmn.getNodeName());
                    ChildLeafListHelper childLeafListhelper = getModelNodeHelperRegistry().getConfigLeafListHelper(this.getModelNodeSchemaPath(), fmnQname);
                    if (childLeafListhelper != null) {
                        Collection<ConfigLeafAttribute> leafListValues = childLeafListhelper.getValue(this);
                        if (!leafListMatchesFilter(leafListValues, fmn.getFilter())) {
                            proceedFurther = false;
                            // this tree needs to be ignored as is without
                            // further checking
                            break;
                        }
                    } else {
                        ConfigAttributeHelper configHelper = getModelNodeHelperRegistry().getConfigAttributeHelper(this.getModelNodeSchemaPath(), fmnQname);
                        if (configHelper != null) {
                            ConfigLeafAttribute attributeValue = configHelper.getValue(this);
                            if (attributeValue==null || (attributeValue!=null && !fmn.getFilter().equals(attributeValue.getStringValue()))) {
                                proceedFurther = false;
                                // this tree needs to be ignored as is without further checking
                                break;
                            }
                        } else if (SchemaRegistryUtil.getStateLeafListAttributes(getModelNodeSchemaPath(), getSchemaRegistry()).contains(fmnQname) || SchemaRegistryUtil.getStateAttributes(getModelNodeSchemaPath(), getSchemaRegistry()).contains(fmnQname)) {
                            if (!includeState) {
                                // state match condition in get-config is considered as false
                                proceedFurther = false;
                                break;
                            }
                            stateFilterMatchNodes.add(fmn);
                        } else {
                            LOGGER.error("Attribute {} not found",fmnQname);
                            proceedFurther = false;
                            break;
                        }
                    }

                } catch (GetAttributeException e) {
                    LOGGER.error("could not retrieve attribute value, condition is false", e);
                    proceedFurther = false;
                    break;
                }
            }
        }
        return proceedFurther;
    }

    private boolean leafListMatchesFilter(Collection<ConfigLeafAttribute> leafListValues, String filter) {
        Iterator<ConfigLeafAttribute> leafListIterator = leafListValues.iterator();
        while(leafListIterator.hasNext()){
            ConfigLeafAttribute configLeafAttribute = leafListIterator.next();
            if(filter.equals(configLeafAttribute.getStringValue())){
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unlikely-arg-type")
    protected boolean copyKeyAttribute(Document doc, Element parent, ModelNodeId modelNodeId, List<String> keyValues, NetconfQueryParams params) throws GetAttributeException {
        DataSchemaNode dataSchemaNode = m_schemaRegistry.getDataSchemaNode(getModelNodeSchemaPath());
        if (dataSchemaNode instanceof ListSchemaNode && !(((ListSchemaNode) dataSchemaNode).getKeyDefinition().isEmpty())) {
            for (QName keyAttribute : ((ListSchemaNode) dataSchemaNode).getKeyDefinition()) {
                if (isAboveDepth(params, keyAttribute)) {
                    if (!keyValues.contains(keyAttribute)) {
                        addXmlValue(doc, parent, getAttributeValue(keyAttribute, modelNodeId), true);
                        keyValues.add(keyAttribute.getLocalName());
                        LOGGER.debug("Key Attribute Copied : " + keyAttribute);
                    }
                }
            }

            if (((ListSchemaNode) dataSchemaNode).getKeyDefinition().size() == keyValues.size()) {
                return true;
            }
        }
        return false;
    }

    private boolean isKeyAttribute(QName qname) {
        Map<QName, ConfigAttributeHelper> naturalKeyHelpers = getModelNodeHelperRegistry().getNaturalKeyHelpers(this.getModelNodeSchemaPath());
        if (!naturalKeyHelpers.isEmpty()) {
            for(QName keyAttribute : naturalKeyHelpers.keySet()){
                if (qname.equals(keyAttribute)) {
                    return true;
                }
            }
        }
        return false;
    }

    private Element delegateToChild(NetconfClientInfo clientInfo, Document doc, FilterNode filter, boolean includeState, ModelNode child,
            StateAttributeGetContext stateContext, ConfigAttributeGetContext configContext, NetconfQueryParams params)
                    throws GetException {
        Element result;
        if (includeState) {
            result = child.get(new GetContext(clientInfo, doc, filter, stateContext, configContext), params);
        } else {
            result = child.getConfig(new GetConfigContext(clientInfo, doc, filter, configContext), params);
        }
        return result;
    }

    @Override
    public ModelNode getChildModelNode(QName nodeName) throws ModelNodeGetException {
        return getModelNodeHelperRegistry().getChildContainerHelpers(this.getModelNodeSchemaPath()).get(nodeName).getValue(this);
    }

    @Override
    public Collection<ModelNode> getChildModelNodes(QName nodeName, List<FilterMatchNode> matchNodes) throws ModelNodeGetException {
        Map<QName, ConfigLeafAttribute> matchCriteria = new HashMap<>();
        ChildListHelper childListHelper = getModelNodeHelperRegistry().getChildListHelper(this.getModelNodeSchemaPath(), nodeName);
        for(FilterMatchNode matchNode: matchNodes){
            QName qName = getSchemaRegistry().lookupQName(matchNode.getNamespace(), matchNode.getNodeName());
            DataSchemaNode nonChoiceChild = getSchemaRegistry().getNonChoiceChild(childListHelper.getChildModelNodeSchemaPath(), qName);
            updateFilter(matchNode, nonChoiceChild);
            if(nonChoiceChild != null && nonChoiceChild.isConfiguration()) {
                matchCriteria.put(qName, new GenericConfigAttribute(qName.getLocalName(), qName.getNamespace().toString(), matchNode.getFilter()));
            }
        }
        return getModelNodeHelperRegistry().getChildListHelpers(this.getModelNodeSchemaPath()).get(nodeName).getValue(this, matchCriteria);
    }

    private void updateFilter(FilterMatchNode matchNode, DataSchemaNode nonChoiceChild) {
        if (nonChoiceChild instanceof LeafSchemaNode) {
            String filter = BitsTypeUtil.orderBitsValue((LeafSchemaNode) nonChoiceChild, matchNode.getFilter());
            matchNode.setFilter(filter);
        }
    }

    @SuppressWarnings("unchecked")
    private Collection<ConfigLeafAttribute> getChildLeafListValues(QName nodeName, ModelNodeId modelNodeId) throws GetAttributeException {
        ChildLeafListHelper configLeafListHelper = getModelNodeHelperRegistry().getConfigLeafListHelper(this.getModelNodeSchemaPath(), nodeName);
        if (configLeafListHelper != null) {
            return configLeafListHelper.getValue(this);
        }
        if (SchemaRegistryUtil.getStateLeafListAttributes(getModelNodeSchemaPath(), getSchemaRegistry()).contains(nodeName)) {
            Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> stateAttrs = new HashMap<>();
            List<QName> nodes = new ArrayList<>();
            nodes.add(nodeName);
            stateAttrs.put(modelNodeId, new Pair<>(nodes, Collections.EMPTY_LIST));
            Map<ModelNodeId, List<Element>> result = getSubSystem().retrieveStateAttributes(stateAttrs, NetconfQueryParams.NO_PARAMS);
            List<Element> stateAttrPair = result.get(modelNodeId);
            Collection<ConfigLeafAttribute> data = new ArrayList<>();
            for (Element e : stateAttrPair) {
                try {
                    data.add(ConfigAttributeFactory.getConfigAttribute(getSchemaRegistry(),this.getModelNodeSchemaPath(),nodeName,e));
                } catch (InvalidIdentityRefException e1) {
                    throw new GetAttributeException(e1.getRpcError());
                }
            }
            return data;
        }
        LOGGER.error("Attribute " + nodeName + " not found");
        throw new GetAttributeException("LeafList " + nodeName + " not found");

    }

    private ConfigLeafAttribute getAttributeValue(QName nodeName, ModelNodeId modelNodeId) throws GetAttributeException {
        ConfigAttributeHelper configHelper =  getModelNodeHelperRegistry().getConfigAttributeHelper(this.getModelNodeSchemaPath(), nodeName);
        if (configHelper != null) {
            return configHelper.getValue(this);
        }
        if (SchemaRegistryUtil.getStateAttributes(getModelNodeSchemaPath(), getSchemaRegistry()).contains(nodeName)) {
            List<QName> nodes = new ArrayList<>();
            nodes.add(nodeName);
            Map<ModelNodeId,Pair<List<QName>, List<FilterNode>>> stateAttrs = new HashMap<>();
            stateAttrs.put(modelNodeId, new Pair<List<QName>, List<FilterNode>>(nodes, Collections.<FilterNode> emptyList()));
            Map<ModelNodeId, List<Element>> result = getSubSystem().retrieveStateAttributes(stateAttrs, NetconfQueryParams.NO_PARAMS);
            List<Element> stateAttrPair = result.get(modelNodeId);
            if(stateAttrPair != null && !stateAttrPair.isEmpty()){
                String stateAttribute = stateAttrPair.get(0).getTextContent();
                if(stateAttribute != null){
                    try {
                        return ConfigAttributeFactory.getConfigAttribute(getSchemaRegistry(),this.getModelNodeSchemaPath(),nodeName,stateAttrPair.get
                                (0));
                    } catch (InvalidIdentityRefException e) {
                        throw new GetAttributeException(e.getRpcError());
                    }
                }
            } else {
                LOGGER.debug("Couldn't fetch state attributes for {} from SubSystem {}", nodeName, getSubSystem());
            }
            return null;
        }
        LOGGER.error("Attribute " + nodeName + " not found");
        throw new GetAttributeException("Attribute " + nodeName + " not found");
    }

    private void copyAttributeToOutput(Document doc, Element parent, QName nodeName, ModelNodeId modelNodeId) {
        try {
            if (!isKeyAttribute(nodeName)) {
                addXmlValue(doc, parent, getAttributeValue(nodeName, modelNodeId), false);
            }
        } catch (GetAttributeException e) {
            LOGGER.error("Attribute " + nodeName.getLocalName() + " value could not be retrieved", e);
        }
    }

    private void copyLeafListFilteredToOutput(Document doc, Element parent, QName nodeName, String filterValue, ModelNodeId modelNodeId) {
        try {
            Collection<ConfigLeafAttribute> leafListValues = getChildLeafListValues(nodeName, modelNodeId);
            for (ConfigLeafAttribute value : leafListValues) {
                if(value!=null && value.getStringValue().equals(filterValue)) {
                    addXmlValue(doc, parent, value, false);
                }
            }
        } catch (GetAttributeException e) {
            LOGGER.error("LeafList " + nodeName + " value could not be retrieved", e);
        }
    }

    private void copyLeafListToOutput(Document doc, Element parent, QName nodeName, ModelNodeId modelNodeId) throws GetAttributeException {
        Collection<ConfigLeafAttribute> leafListValues = getChildLeafListValues(nodeName, modelNodeId);
        for (ConfigLeafAttribute value : leafListValues) {
            addXmlValue(doc, parent, value, false);
        }
    }


    protected void copyCompletelyToOutput(NetconfClientInfo clientInfo, Document doc, ModelNodeId modelNodeId, Element parent, boolean includeState,
            StateAttributeGetContext stateAttributeContext, ConfigAttributeGetContext configContext, NetconfQueryParams params) throws DOMException,
    GetException, GetAttributeException {
        // copy keys first
        ArrayList<String> keyValues = new ArrayList<>();
        copyKeyAttribute(doc, parent, modelNodeId, keyValues, params);
        if (includeState) {
            storeAllStateAttributes(modelNodeId, stateAttributeContext, params);
        }
        if (params.isIncludeConfig()) {
            // get config values
            copyAllConfigAttributesToOutput(doc, parent, keyValues, params);
            // get config leaf list values
            copyAllConfigLeafListToOutput(doc, parent, keyValues, params);
        }
        if(areChildrenAboveDepth(params)){
            // add the child containers lists
            copyAllChildListToOutput(clientInfo, doc, parent, includeState, stateAttributeContext, configContext, params);
            // add the child containers
            copyAllContainerToOutput(clientInfo, doc, parent, includeState, stateAttributeContext, configContext, params);
        }
    }

    /**
     * Children will always have a depth of parent depth + 1, so to know if children are to be copied you could subtract
     * one level from current node's depth and evaluate. This saves the children lookup.
     * @param params
     * @return
     */
    private boolean areChildrenAboveDepth(NetconfQueryParams params) {
        Map<String, List<QName>> fields = params.getFields();
        if (!fields.isEmpty()){
            return true;
        }
        if(params.getDepth() == NetconfQueryParams.UNBOUNDED){
            return true;
        }
        if((Integer.valueOf(params.getDepth())-2) >= m_modelNodeId.getDepth()){
            return true;
        }
        return false;
    }

    /**
     * Grand children will always have a depth of parent depth + 2, so to know if grand children are to be copied you could subtract
     * two level from current node's depth and evaluate. This saves the grand children lookup when only children node needs to be populated in response.
     * @param params
     * @return
     */
    protected boolean grandChildNodesToBeLoaded(NetconfQueryParams params) {
        Map<String, List<QName>> fields = params.getFields();
        if (!fields.isEmpty()){
            return true;
        }
        if(params.getDepth() == NetconfQueryParams.UNBOUNDED){
            return true;
        }
        if((Integer.valueOf(params.getDepth())-3) >= m_modelNodeId.getDepth()){
            return true;
        }
        return false;
    }


    private void storeAllStateAttributes(ModelNodeId modelNodeId, StateAttributeGetContext stateContext, NetconfQueryParams params) {
        Set<QName> stateAttributes = SchemaRegistryUtil.getStateAttributes(getModelNodeSchemaPath(), getSchemaRegistry());
        if (!stateAttributes.isEmpty()) {
            List<Element> elements = stateContext.getStateMatchNodes().get(modelNodeId);
            if (elements != null) {
                for (Element e : elements) {
                    QName fmnQname = getSchemaRegistry().lookupQName(e.getNamespaceURI(), e.getLocalName());
                    stateAttributes.remove(fmnQname);
                }
            }
        }
        List<FilterNode> filterList = new ArrayList<FilterNode>();
        if ( isAboveDepth(params)){
            addContainerAndListStateNodes(filterList);
        }
		else if (params.getDepth() == 2){
			markStateRootNodeToBeCreated(modelNodeId, stateContext);
		}

        if (!stateAttributes.isEmpty() || !filterList.isEmpty()) {
            populateStateContextPerSubSystem(new ArrayList<>(stateAttributes), filterList, getModelNodeSchemaPath(),
                    stateContext, modelNodeId, params);
        }
    }

    // Add this method to retain old code for fixing OBBAA-599
    private void addStateAttributesIfNotExist(StateAttributeGetContext stateContext, SubSystem system,
                                              ModelNodeId modelNodeId, List<QName> stateAttributes, List<FilterNode> stateSubtrees, NetconfQueryParams params) {
        for (Entry<SubSystem, Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>>> entry : stateContext.getSubSystems().entrySet()) {
            SubSystem subSystem = entry.getKey();
            if (subSystem.getClass().getName().equals(system.getClass().getName())) {
                Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> map = entry.getValue();
                if (!map.containsKey(modelNodeId)) {
                    Pair<List<QName>, List<FilterNode>> pair = new Pair<List<QName>, List<FilterNode>>(stateAttributes, stateSubtrees);
                    map.put(modelNodeId, pair);
                }
                filterStateAttributesAboveDepth(params, map);
                return;
            }
        }

        Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> map = new HashMap<>();
        map.put(modelNodeId, new Pair<List<QName>, List<FilterNode>>(stateAttributes, stateSubtrees));
        filterStateAttributesAboveDepth(params, map);
        stateContext.updateSubSystems(system, map);
    }

    private void populateStateContextPerSubSystem(List<QName> stateAttributes, List<FilterNode> filterList,
                                                  SchemaPath parentSP, StateAttributeGetContext stateContext,
                                                  ModelNodeId parentMNId, NetconfQueryParams params) {
        Map<SubSystem, List<QName>> subSystemAttributeMap = new HashMap<>();
        Map<SubSystem, List<FilterNode>> subSystemFNMap = new HashMap<>();
        segregateStateAttrPerSubsystem(stateAttributes, parentSP, subSystemAttributeMap);
        segregateStateFNPerSubsystem(filterList, parentSP, subSystemFNMap);
        Map<SubSystem, Pair<List<QName>, List<FilterNode>>> subSystemPairMap = new HashMap<>();
        populateSubsystemToStateMap(subSystemAttributeMap, subSystemFNMap, subSystemPairMap);

        for (Entry<SubSystem, Pair<List<QName>, List<FilterNode>>> entry : subSystemPairMap.entrySet()) {
            SubSystem subSystem = entry.getKey();
            Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> existingMap = stateContext.getSubSystems().get(subSystem);
            if(existingMap == null){
                existingMap = new HashMap<>();
                existingMap.put(parentMNId, entry.getValue());
            }else{
                if (!existingMap.containsKey(parentMNId)) {
                    existingMap.put(parentMNId, entry.getValue());
                }
            }
            filterStateAttributesAboveDepth(params, existingMap);
            stateContext.updateSubSystems(subSystem, existingMap);
        }
    }

    private void populateSubsystemToStateMap(Map<SubSystem, List<QName>> subSystemAttributeMap,
                                             Map<SubSystem, List<FilterNode>> subSystemFNMap,
                                             Map<SubSystem, Pair<List<QName>, List<FilterNode>>> subSystemPairMap) {
        for (Entry<SubSystem, List<QName>> entry : subSystemAttributeMap.entrySet()) {
            subSystemPairMap.put(entry.getKey(), new Pair<>(entry.getValue(),Collections.EMPTY_LIST));
        }

        for (Entry<SubSystem, List<FilterNode>> entry : subSystemFNMap.entrySet()) {
            Pair<List<QName>, List<FilterNode>> pair = subSystemPairMap.get(entry.getKey());
            if(pair == null){
                pair = new Pair<>(Collections.EMPTY_LIST, entry.getValue());
                subSystemPairMap.put(entry.getKey(), pair);
            }else{
                pair.setSecond(entry.getValue());
            }
        }
    }

    private void segregateStateFNPerSubsystem(List<FilterNode> filterList, SchemaPath parentSP,
                                              Map<SubSystem, List<FilterNode>> subSystemFNMap) {
        for (FilterNode filterNode : filterList) {
            SubSystem subSystem = getSubsystemForStateFN(filterNode, parentSP);
            List<FilterNode> filterNodes = subSystemFNMap.get(subSystem);
            if(filterNodes == null){
                filterNodes = new ArrayList<>();
                subSystemFNMap.put(subSystem, filterNodes);
            }
            filterNodes.add(filterNode);
        }
    }

    private void segregateStateAttrPerSubsystem(List<QName> stateAttributes, SchemaPath parentSP,
                                                Map<SubSystem, List<QName>> subSystemAttributeMap) {
        for (QName stateAttribute : stateAttributes) {
            SubSystem subSystem = getSubsystemForStateAttr(stateAttribute, parentSP);
            List<QName> stateAttrs = subSystemAttributeMap.get(subSystem);
            if(stateAttrs == null){
                stateAttrs = new ArrayList<>();
                subSystemAttributeMap.put(subSystem, stateAttrs);
            }
            stateAttrs.add(stateAttribute);
        }
    }

    private SubSystem getSubsystemForStateAttr(QName stateAttrQname, SchemaPath parentSP) {
        SchemaPath schemaPath = getSchemaRegistry().getNonChoiceChild(parentSP, stateAttrQname).getPath();
        return getSubSystemRegistry().lookupSubsystem(schemaPath);
    }

    private SubSystem getSubsystemForStateFN(FilterNode filterNode, SchemaPath parentSP) {
        Collection<DataSchemaNode> nonChoiceChildren = getSchemaRegistry().getNonChoiceChildren(parentSP);
        for (DataSchemaNode nonChoiceChild : nonChoiceChildren) {
            if(nonChoiceChild.getQName().getNamespace().toString().equals(filterNode.getNamespace()) &&
                    nonChoiceChild.getQName().getLocalName().equals(filterNode.getNodeName())){
                return getSubSystemRegistry().lookupSubsystem(nonChoiceChild.getPath());
            }
        }
        return null;
    }

    private void addContainerAndListStateNodes(List<FilterNode> filterList) {
        DataSchemaNode schemaNode = getSchemaRegistryForParent().getDataSchemaNode(getModelNodeSchemaPath());
        if(schemaNode instanceof DataNodeContainer){
            Collection<DataSchemaNode> children = getSchemaRegistry().getChildren(schemaNode.getPath());
            for(DataSchemaNode child :children){
                if(!child.isConfiguration() && (child instanceof ListSchemaNode || child instanceof ContainerSchemaNode)){
                    FilterNode filterNode = new FilterNode(child.getQName());
                    filterList.add(filterNode);
                }
                if(child instanceof ChoiceSchemaNode){
                    Collection<CaseSchemaNode> cases = ((ChoiceSchemaNode)child).getCases().values();
                    List<DataSchemaNode> schemaNodes = ChoiceCaseNodeUtil.getAllNodesFromCases(cases);
                    for(DataSchemaNode node : schemaNodes){
                        if(!node.isConfiguration() && (node instanceof ListSchemaNode || node instanceof ContainerSchemaNode)){
                            FilterNode filterNode = new FilterNode(node.getQName());
                            filterList.add(filterNode);
                        }
                    }
                }
            }
        }
    }
	private void markStateRootNodeToBeCreated(ModelNodeId modelNodeId, StateAttributeGetContext stateContext) {
		DataSchemaNode schemaNode = getSchemaRegistryForParent().getDataSchemaNode(getModelNodeSchemaPath());
		if(schemaNode instanceof DataNodeContainer && !schemaNode.isConfiguration()){
			Map<ModelNodeId, List<Element>> stateMatchNodes = stateContext.getStateMatchNodes();
			List<Element> existingElements = stateMatchNodes.get(modelNodeId);
			if(existingElements == null){
				existingElements = new ArrayList<>();
				stateMatchNodes.put(modelNodeId,existingElements);
			}
		}
	}

    /**
     *
     * @param doc
     * @param parent
     */
    protected void copyAllConfigAttributesToOutput(Document doc, Element parent, List<String> keyValues, NetconfQueryParams params) {
        for (Map.Entry<QName, ConfigAttributeHelper> helperEntry : getModelNodeHelperRegistry().getConfigAttributeHelpers(this.getModelNodeSchemaPath()).entrySet()) {
            QName configAttribute = helperEntry.getKey();
            if ( isAboveDepth(params, configAttribute)){
                if (keyValues.contains(configAttribute.getLocalName())) {
                    continue;
                }
                ConfigAttributeHelper configAttributeHelper = helperEntry.getValue();
                try {
                    if (configAttributeHelper.isMandatory()) {
                        addXmlValue(doc, parent, configAttributeHelper.getValue(this), false);
                    } else if(configAttributeHelper.isChildSet(this)){
                        addXmlValue(doc, parent, configAttributeHelper.getValue(this), false);
                    }
                } catch (GetAttributeException e) {
                    LOGGER.error("failed to add attribute to output " + configAttribute, e);
                }
            }
        }
    }

    /**
     *
     * @param doc
     * @param parent
     */
    protected void copyAllConfigLeafListToOutput(Document doc,  Element parent, List<String> keyValues, NetconfQueryParams params) {
        for (Map.Entry<QName, ChildLeafListHelper> helperEntry : getModelNodeHelperRegistry().getConfigLeafListHelpers(this
                .getModelNodeSchemaPath()).entrySet()) {
            try {
                ChildLeafListHelper leafListHelper = helperEntry.getValue();
                if (leafListHelper.isConfiguration() ) {
                    QName leafListName = helperEntry.getKey();
                    if (isAboveDepth(params, leafListName)){
                        if (keyValues.contains(leafListName.getLocalName())) {
                            continue;
                        }
                        Collection<ConfigLeafAttribute> result = leafListHelper.getValue(this);
                        for (ConfigLeafAttribute value : result) {
                            addXmlValue(doc, parent, value, false);
                        }
                    }
                }
            } catch (IllegalArgumentException | GetAttributeException e) {
                LOGGER.error("failed to add container list to output " + helperEntry, e);
            }
        }
    }

    /**
     *
     * @param doc
     * @param parent
     * @param includeState
     * @throws GetException
     */
    private void copyAllChildListToOutput(NetconfClientInfo clientInfo, Document doc, Element parent, boolean includeState, StateAttributeGetContext stateContext, ConfigAttributeGetContext configContext, NetconfQueryParams params) throws GetException {
        for (ChildListHelper helper : getModelNodeHelperRegistry().getChildListHelpers(this.getModelNodeSchemaPath()).values()) {
            try {
                Collection<ModelNode> result = helper.getValue(this, Collections.<QName, ConfigLeafAttribute>emptyMap());
                for (ModelNode child : result) {
                    if ( isAboveDepth(params, child.getModelNodeId()) && child.isVisible()){
                        if (includeState) {
                            Element element = child.get(new GetContext(clientInfo, doc, null, stateContext, configContext), params);
                            appendChildIfNotNull(element, parent);
                        } else if (params.isIncludeConfig()) {
                            Element element =child.getConfig(new GetConfigContext(clientInfo, doc, null, configContext), params);
                            appendChildIfNotNull(element, parent);
                        }
                    }
                }

            } catch (IllegalArgumentException | ModelNodeGetException e) {
                LOGGER.error("failed to add container list to output " + helper, e);
            }
        }
    }

    protected void appendChildIfNotNull(Element element, Element parent) {
        if (element != null) {
            parent.appendChild(element);
        }
    }

    /**
     * @param doc
     * @param parent
     * @param includeState
     * @throws GetException
     */
    protected void copyAllContainerToOutput(NetconfClientInfo clientInfo, Document doc, Element parent, boolean includeState,
            StateAttributeGetContext stateContext, ConfigAttributeGetContext configContext, NetconfQueryParams params) throws GetException {
        for (ChildContainerHelper helper : getModelNodeHelperRegistry().getChildContainerHelpers(this.getModelNodeSchemaPath()).values()) {
            try {
                if (helper.isMandatory() || helper.isChildSet(this)) {
                    copyContainerToOutput(clientInfo, doc, parent, includeState, stateContext, configContext, params, helper);
                }

            } catch (ModelNodeGetException e) {
                LOGGER.error("failed to add container to output " + helper, e);
            }
        }
    }

    protected void copyContainerToOutput(NetconfClientInfo clientInfo, Document doc, Element parent, boolean includeState,
                                         StateAttributeGetContext stateContext, ConfigAttributeGetContext configContext,
                                         NetconfQueryParams params, ChildContainerHelper helper) {
        ModelNode result = helper.getValue(this);
        if (result != null && result.isVisible()) {
            if ( isAboveDepth(params, result.getModelNodeId())){
                if (includeState) {
                    Element element = result.get(new GetContext(clientInfo, doc, null, stateContext, configContext), params);
                    appendChildIfNotNull(element, parent);
                } else if (params.isIncludeConfig()) {
                    Element element = result.getConfig(new GetConfigContext(clientInfo, doc, null, configContext), params);
                    appendChildIfNotNull(element, parent);
                }
            }
        }
    }

    protected Element checkPermissionAndGetElement(Document doc, SchemaPath childSP, ConfigAttributeGetContext configContext,
                                                   NetconfClientInfo clientInfo, ModelNodeId childMnId) {
        // check permission for subtree first
        SubSystem subSystem = m_subSystemRegistry.lookupSubsystem(childSP);
        boolean subSystemAuthorized = configContext.isSubSystemAuthorized(subSystem);
        if (!subSystemAuthorized && subSystem != null) {
            try {
                subSystem.checkRequiredPermissions(clientInfo, Operation.GET.getType());
                configContext.addAuthorizedSubSystem(subSystem);
            } catch (AccessDeniedException e) {
                LOGGER.warn(null, "User '{}' is not permitted to access SchemaPath '{}' from subsystem '{}'", LOGGER.sensitiveData(clientInfo.getUsername()), childSP, subSystem);
                return null;
            }
        }

        if (subSystem.isNodePresent(childSP, childMnId)) {
            String containerName = childSP.getLastComponent().getLocalName();
            String namespace = childSP.getLastComponent().getNamespace().toString();
            String localName = resolveLocalName(getSchemaRegistry(), namespace, containerName);
            return doc.createElementNS(namespace, localName);
        }
        return null;
    }

    @Override
    public void prepareEditSubTree(EditContainmentNode root, Element configElementContent) throws EditConfigException {
        new EditTreeBuilder().prepareEditSubTree(root, configElementContent, this.getModelNodeSchemaPath(), getSchemaRegistry(),
                getModelNodeHelperRegistry(), this.getModelNodeId());
        EditContainmentNode.setParentForEditContainmentNode(root, null);
    }


    @Override
    public void editConfig(EditContext editContext, WritableChangeTreeNode changeTreeNode) throws EditConfigException{
        interceptEditConfig(editContext, changeTreeNode);
        TimingLogger.startPhase("createEditTree.makeChangesInDataStore.editNode");
        editNode(editContext, changeTreeNode);
        TimingLogger.endPhase("createEditTree.makeChangesInDataStore.editNode", false);
    }

    @Override
    public void interceptEditConfig(EditContext editContext, WritableChangeTreeNode changeTreeNode) {
        m_interceptor.interceptEditConfig(this, editContext, changeTreeNode);
    }

    public void editNode(EditContext editContext, WritableChangeTreeNode changeTreeNode) throws EditConfigException {
        try {
            //Prepare commands using just the  editTree for now (and test,error option going forward)
            CompositeEditCommand command = getEditCommand(editContext, changeTreeNode);
            if(LOGGER.isTraceEnabled()) {
                LOGGER.trace("Executing command : " + command);
            }
            command.execute();
            if(command.isError()){
                NetconfRpcError error	=	NetconfRpcErrorUtil.getNetconfRpcErrorForModelNodeId(getModelNodeId(),NetconfRpcErrorTag.OPERATION_FAILED,
                        "Following errors occured while executing edit config : "+command.getErrorString(), getSchemaRegistry());
                throw new EditConfigException(error);
            }
        } catch (CommandExecutionException e) {
            if(e.getCause() instanceof EditConfigException){
                EditConfigException innerException = (EditConfigException)e.getCause();
                throw innerException;
            }
            LOGGER.debug("Error while executing edit config : " , e);
            EditConfigException exception = null;
            if (e.getRpcError() != null) {
                NetconfRpcError error = e.getRpcError();
                if (getModelNodeId()!=null && error.getErrorPath() == null) {
                    error.setErrorPath(getModelNodeId().xPathString(getSchemaRegistry()), getModelNodeId().xPathStringNsByPrefix(getSchemaRegistry()));
                }
                exception = new EditConfigException(error);
            } else {
                exception = new EditConfigException(NetconfRpcErrorUtil.getNetconfRpcErrorForModelNodeId(getModelNodeId(),
                        NetconfRpcErrorTag.OPERATION_FAILED, e.getMessage(), getSchemaRegistry()));
            }
            exception.addSuppressed(e);
            throw exception;
        }

    }

    @Override
    public CompositeEditCommand getEditCommand(EditContext editContext, WritableChangeTreeNode parentChangeTreeNode) throws EditConfigException{
        CompositeEditCommand compositeEdit = new CompositeEditCommand().setErrorOption(editContext.getErrorOption());
        EditContainmentNode editNode = editContext.getEditNode();
        if(!EditConfigOperations.CREATE.equals(editNode.getEditOperation())){
            //check if it is the right instance
            boolean thisInstance = isThisInstance(editNode);
            if (!thisInstance) {
                return null;
            }
        }

        WritableChangeTreeNode ctn = null;
        if (parentChangeTreeNode != null) {
            ctn = (WritableChangeTreeNode) parentChangeTreeNode.getChildren().get(getModelNodeId());
            if (ctn == null && !Boolean.parseBoolean(SystemPropertyUtils.getInstance().getFromEnvOrSysProperty(IS_FULL_DS_REBUILT, "false"))) {
                DataSchemaNode dataSchemaNode = editNode.getSchemaRegistry().getDataSchemaNode(getModelNodeSchemaPath());
                ctn = new ChangeTreeNodeImpl(getSchemaRegistry(), parentChangeTreeNode, getModelNodeId(),
                        dataSchemaNode, parentChangeTreeNode.getNodesIndex(),
                        parentChangeTreeNode.getAttributeIndex(), parentChangeTreeNode.getChangedNodeTypes(), parentChangeTreeNode.getContextMap(), parentChangeTreeNode.getNodesOfTypeIndexWithinSchemaMount());
                ctn.setEditOperation(editContext.getEditNode().getEditOperation());
                if(parentChangeTreeNode.isMountPoint()) {
                    ctn.setMountPoint(true);
                }
                if(!isVisible()) {
                    addVisibilityContext(ctn);
                }
                if(dataSchemaNode instanceof ListSchemaNode) {
                    appendKeyCTN((ListSchemaNode) dataSchemaNode, getModelNodeId(), ctn, editNode, ChangeType.none);
                }
            }
        }

        if(null == this.getParent() && editNode.getEditOperation().equals(EditConfigOperations.REPLACE) && isThisInstance(editNode)){
            deleteChildrenNotPresentInEditNode(editContext, compositeEdit, editNode, this, this.getModelNodeSchemaPath(), ctn);
        }

        createDefaultCaseNodes(editContext);
        createCaseDefaultNodes(editContext);
        //check if this node is being replaced, if yes, then its the node already exists case for sure, then append the delete commands for the children that are to be deleted, appe
        addCommandsForCurrentNodeLeafChanges(editContext, compositeEdit, ctn);
        appendDeleteCommandsForNonExistingChildren(editContext, compositeEdit, editNode, this, ctn);
        addCommandsForChildNodeChanges(editContext, compositeEdit, ctn);

        if (ctn != null) {
            compositeEdit.appendCommand(new AppendCtnCommand(parentChangeTreeNode, ctn));
        }
        return compositeEdit;
    }
    
    /**
     * Updates the current Edit Tree to instantiate default case nodes when the other case node is being deleted.
     * @param editContext
     */
    private void createDefaultCaseNodes(EditContext editContext) {
        DefaultCapabilityCommandInterceptor interceptor = getModelNodeHelperRegistry().getDefaultCapabilityCommandInterceptor();
        List<CaseSchemaNode> defaultCaseNodes = new ArrayList<>();
        EditContainmentNode editNode = editContext.getEditNode();
        EditContainmentNode newParentEditNode = editContext.getEditNode();
        SchemaPath modelNodeSchemaPath = this.getModelNodeSchemaPath();
        for (EditContainmentNode childEditNode : editNode.getChildren()) {
            if (EditConfigOperations.REMOVE.equals(childEditNode.getEditOperation()) ||
                    EditConfigOperations.DELETE.equals(childEditNode.getEditOperation())) {
                QName childQName = childEditNode.getQName();
                ChildContainerHelper childContainerHelper = getModelNodeHelperRegistry().getChildContainerHelper
                        (modelNodeSchemaPath, childQName);
                SchemaPath childSchemaPath = getSchemaRegistry().getDescendantSchemaPath(modelNodeSchemaPath, childQName);
                ChoiceSchemaNode choiceParentSchemaNode = ChoiceCaseNodeUtil.getChoiceParentSchemaNode(childSchemaPath, getSchemaRegistry());
                // in nested choice, if child choice doesn't have the defaults, its parent choice defaults should be instantiated.
                choiceParentSchemaNode = getChoiceWithDefaults(editNode, childSchemaPath, choiceParentSchemaNode);

                if ( hasDefaults(choiceParentSchemaNode) && isAllowedToInstantiateDefaultCase(childSchemaPath, editNode)){
                    if (childContainerHelper != null) {
                        // handle choice case node
                        getDefaultCaseNodes(defaultCaseNodes, choiceParentSchemaNode);

                    } else {
                        ChildListHelper childListHelper = getModelNodeHelperRegistry().getChildListHelper(modelNodeSchemaPath,
                                childEditNode.getQName());
                        if (choiceParentSchemaNode != null) {
                            Optional<CaseSchemaNode> defaultCase = choiceParentSchemaNode.getDefaultCase();
                            if (defaultCase.isPresent()) {
                                try {
                                    Map<QName, ConfigLeafAttribute> matchCriteria = getMatchCriteria(childEditNode);
                                    Collection<ModelNode> children = childListHelper.getValue(this, matchCriteria);
                                    Collection<ModelNode> availableModelNodes = childListHelper.getValue(this, Collections.emptyMap());
                                    List<EditContainmentNode> allListNodes = editNode.getChildNodes(childEditNode.getQName());

                                    //if there are no more list entries, default needs to be created.
                                    if ((children.size() == 1 &&  availableModelNodes.size()== 1) || isAllListDeleted(allListNodes, availableModelNodes)) {
                                        QName defaultCaseName = defaultCase.get().getQName();
                                        defaultCaseNodes.add(choiceParentSchemaNode.getCases().get(defaultCaseName));
                                    }
                                } catch (ModelNodeGetException | GetAttributeException e) {
                                    //we should not be getting this exception, so throwing as RT exception
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                    }
                }
            }
        }
        for (EditChangeNode editChangeNode : editNode.getChangeNodes()) {
            if (EditConfigOperations.REMOVE.equals(editChangeNode.getOperation()) ||
                    EditConfigOperations.DELETE.equals(editChangeNode.getOperation())) {
                QName childQName = editChangeNode.getQName();
                ChildLeafListHelper childLeafListHelper = getModelNodeHelperRegistry().getConfigLeafListHelper(modelNodeSchemaPath, childQName);
                SchemaPath childSchemaPath = getSchemaRegistry().getDescendantSchemaPath(modelNodeSchemaPath, childQName);
                ChoiceSchemaNode choiceParentSchemaNode = ChoiceCaseNodeUtil.getChoiceParentSchemaNode(childSchemaPath, getSchemaRegistry());
                // in nested choice, if child choice doesn't have the defaults, its parent choice defaults should be instantiated.
                choiceParentSchemaNode = getChoiceWithDefaults(editNode, childSchemaPath, choiceParentSchemaNode);
                if ( hasDefaults(choiceParentSchemaNode) && isAllowedToInstantiateDefaultCase(childSchemaPath, editNode)){
                    if (childLeafListHelper != null) {
                        // if there are no more leaf-list entries, default needs to be created.
                        if (areAllLeafListValuesBeingDeleted(editNode, childLeafListHelper)) {
                            getDefaultCaseNodes(defaultCaseNodes, choiceParentSchemaNode);
                        }
                    } else {
                        ConfigAttributeHelper configAttributeHelper = getModelNodeHelperRegistry().getConfigAttributeHelper(modelNodeSchemaPath, childQName);
                        if (configAttributeHelper != null) {
                            getDefaultCaseNodes(defaultCaseNodes, choiceParentSchemaNode);
                        }
                    }
                }
            }
        }
        //check if there are default cases to be created,iterate through that new list and call interceptor.
        for (CaseSchemaNode choiceCaseNode : defaultCaseNodes) {
            interceptor.populateChoiceCase(newParentEditNode, choiceCaseNode, this, false);
        }

    }

    /**
     * Updates the current Edit Tree to instantiate default leaf nodes when the case sibling node is being created.
     * @param editContext
     */
    private void createCaseDefaultNodes(EditContext editContext) {
        DefaultCapabilityCommandInterceptor interceptor = getModelNodeHelperRegistry().getDefaultCapabilityCommandInterceptor();
        Set<CaseSchemaNode> currentCaseNodes = new HashSet<>();
        EditContainmentNode editNode = editContext.getEditNode();
        EditContainmentNode newParentEditNode = editContext.getEditNode();
        SchemaPath modelNodeSchemaPath = this.getModelNodeSchemaPath();
        for (EditContainmentNode childEditNode : editNode.getChildren()) {            
            if (EditConfigOperations.CREATE.equals(childEditNode.getEditOperation()) ||
                    EditConfigOperations.MERGE.equals(childEditNode.getEditOperation())) {
                QName childQName = childEditNode.getQName();
                SchemaPath childSchemaPath = getSchemaRegistry().getDescendantSchemaPath(modelNodeSchemaPath, childQName);
                Set<CaseSchemaNode> caseNodes = ChoiceCaseNodeUtil.getParentCaseSchemaNodes(childSchemaPath, getSchemaRegistry());
                currentCaseNodes.addAll(caseNodes);
            }
        }
        for (EditChangeNode editChangeNode : editNode.getChangeNodes()) {
            if (EditConfigOperations.CREATE.equals(editChangeNode.getOperation()) ||
                    EditConfigOperations.MERGE.equals(editChangeNode.getOperation())) {
                QName childQName = editChangeNode.getQName();
                SchemaPath childSchemaPath = getSchemaRegistry().getDescendantSchemaPath(modelNodeSchemaPath, childQName);
                Set<CaseSchemaNode> caseNodes = ChoiceCaseNodeUtil.getParentCaseSchemaNodes(childSchemaPath, getSchemaRegistry());
                currentCaseNodes.addAll(caseNodes);
            }
        }
        for (CaseSchemaNode choiceCaseNode : currentCaseNodes) {
            interceptor.populateChoiceCase(newParentEditNode, choiceCaseNode, this, true);
        }

    }

    private ChoiceSchemaNode getChoiceWithDefaults(EditContainmentNode editNode, SchemaPath childSchemaPath, ChoiceSchemaNode choiceParentSchemaNode) {
        if ( choiceParentSchemaNode != null) {
            while ( !(hasDefaults(choiceParentSchemaNode) && isAllowedToInstantiateDefaultCase(childSchemaPath, editNode))){
                ChoiceSchemaNode parentChoiceNode = ChoiceCaseNodeUtil.checkParentNodeIsChoiceAndReturn(choiceParentSchemaNode, getSchemaRegistry());
                if ( parentChoiceNode == null) {
                    break;
                } else {
                    choiceParentSchemaNode = parentChoiceNode;
                }
            }
        }
        return choiceParentSchemaNode;
    }

    private boolean isAllListDeleted(List<EditContainmentNode> allListNodes, Collection<ModelNode> availableModelNodes) throws GetAttributeException {
        if(allListNodes.size() == availableModelNodes.size()) {
            for(EditContainmentNode childNode : allListNodes) {
                if(isListEntryFoundInEditNode(childNode, availableModelNodes) && !EditConfigOperations.isOperationDeleteOrRemove(childNode.getEditOperation())) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
    
    private boolean hasDefaults(ChoiceSchemaNode choiceParentSchemaNode) {
        if (choiceParentSchemaNode != null) {
            Optional<CaseSchemaNode> defaultCase = choiceParentSchemaNode.getDefaultCase();
            if (defaultCase.isPresent()) {
                return true;
            }
        }
        return false;
    }

    private boolean isAllowedToInstantiateDefaultCase(SchemaPath schemapath, EditContainmentNode editNode){
        SchemaPath modelNodeSchemaPath = this.getModelNodeSchemaPath();
        SchemaPath modelNodeCaseSP = ChoiceCaseNodeUtil.getCaseSchemaNodeFromChildNodeIfApplicable(this.getSchemaRegistry(), modelNodeSchemaPath);
        SchemaPath childCaseSP = ChoiceCaseNodeUtil.getCaseSchemaNodeFromChildNodeIfApplicable(this.getSchemaRegistry(), schemapath);
        if(modelNodeCaseSP != null && childCaseSP != null && modelNodeCaseSP.equals(childCaseSP)) {
            return false;
        }
        List<DataSchemaNode> siblings = ChoiceCaseNodeUtil.getDataSiblingsFromParentCaseNode(schemapath, getSchemaRegistry());
        for ( DataSchemaNode sibling : siblings){
            boolean isAttributeValueNull = true;
            if ( sibling instanceof ContainerSchemaNode){
                try {
                    ModelNode value = this.getChildModelNode(sibling.getQName());
                    isAttributeValueNull = isAttributeValueNull(value, editNode, sibling.getQName(), true);

                } catch (ModelNodeGetException | GetAttributeException e) {
                    throw new RuntimeException(e);
                }

            } else if (sibling instanceof ListSchemaNode){
                Map<QName, ConfigLeafAttribute> matchCriteria = getMatchCriteria(editNode);
                ChildListHelper childListHelper = getModelNodeHelperRegistry().getChildListHelper(modelNodeSchemaPath, sibling.getQName());
                try {
                    Collection<ModelNode> children = childListHelper.getValue(this, matchCriteria);
                    isAttributeValueNull = isAttributeValueNull(children, editNode, sibling.getQName(), true);
                } catch (ModelNodeGetException | GetAttributeException e) {
                    throw new RuntimeException(e);
                }
            } else {
                ChildLeafListHelper siblingLeafListHelper = getModelNodeHelperRegistry().getConfigLeafListHelper(modelNodeSchemaPath, sibling.getQName());
                if (siblingLeafListHelper != null) {
                    try {
                        Collection<ConfigLeafAttribute> value = siblingLeafListHelper.getValue(this);
                        isAttributeValueNull = isAttributeValueNull(value, editNode, sibling.getQName(), false);
                    } catch (GetAttributeException e) {
                        throw new RuntimeException(e);
                    }

                } else {
                    ConfigAttributeHelper siblingAttributeHelper = getModelNodeHelperRegistry().getConfigAttributeHelper(modelNodeSchemaPath, sibling.getQName());
                    if (siblingAttributeHelper != null) {
                        try {
                            ConfigLeafAttribute value = siblingAttributeHelper.getValue(this);
                            isAttributeValueNull = isAttributeValueNull(value, editNode, sibling.getQName(), false);
                        } catch (GetAttributeException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
            if (! isAttributeValueNull){
                return false;
            }
        }
        return true;
    }

    private boolean isAttributeValueNull(Object value, EditContainmentNode editNode, QName qName, boolean isChild) throws GetAttributeException{
        if ( value != null){
            String operation = null;
            if ( isChild){
                QName childQName = qName;
                if ( value instanceof Collection<?>){ // list case
                    @SuppressWarnings("unchecked")
                    Collection<ModelNode> listEntries = (Collection<ModelNode>) value;
                    if ( listEntries.isEmpty()){
                        return true;
                    }
                    List<EditContainmentNode> child = editNode.getChildNodes(childQName);
                    if (child.isEmpty()) {
                        return false; // There is no child present in edit nodes ,, but available in modelNode;
                    }
                    boolean isListEntryFoundInEditNode = false;
                    for(EditContainmentNode node : child) {
                        isListEntryFoundInEditNode = isListEntryFoundInEditNode(node, listEntries);
                        if(isListEntryFoundInEditNode) {
                            operation = node.getEditOperation();
                            break;
                        }
                    }
                    if ( !isListEntryFoundInEditNode){
                        return false;
                    }
                } else {
                    EditContainmentNode child = editNode.getChildNode(childQName);
                    if ( child == null) {
                        return false; // There is no child present, but available in modelNode;
                    }
                    operation = child.getEditOperation();
                }
            } else {
                QName siblingQName = qName;
                EditChangeNode changeNode = editNode.getChangeNode(siblingQName);
                if ( changeNode == null) {
                    return false; // There is a leaf/leaf-list with value and not present in change nodes (not deleted);
                }
                operation = changeNode.getOperation();
            }
            if (!(EditConfigOperations.REMOVE.equals(operation) ||
                    EditConfigOperations.DELETE.equals(operation))) {
                return false; // There is a leaf/leaf-list/child with value and it is present in change nodes but with some other operation.
            }
        }
        return true;
    }

    private boolean isListEntryFoundInEditNode(EditContainmentNode child, Collection<ModelNode> listEntries) throws GetAttributeException {
        for ( ModelNode listEntry : listEntries){
            Map<QName, String> listKeys = listEntry.getListKeys();
            List<EditMatchNode> keys = child.getMatchNodes();
            boolean allKeyValuesMatch = checkForAllKeysMatch(listKeys, keys);
            if(allKeyValuesMatch) {
                return true;
            }
        }
        return false;
    }

    private void getDefaultCaseNodes(List<CaseSchemaNode> defaultCaseNodes, ChoiceSchemaNode choiceNode) {
        ChoiceCaseNodeUtil.getDefaultCaseNodes(defaultCaseNodes, choiceNode);
    }

    private boolean checkForAllKeysMatch(Map<QName, String> listKeys, List<EditMatchNode> keys) {        
        for ( EditMatchNode key : keys){
            if(listKeys.containsKey(key.getQName())){
                if(!listKeys.get(key.getQName()).equals(key.getValue())) {
                    return false;
                }
            } else {
                return false;
            }
        }        
        return true;
    }

    private boolean areAllLeafListValuesBeingDeleted(EditContainmentNode editNode, ChildLeafListHelper childLeafListHelper) {
        Collection<ConfigLeafAttribute> leafListValues;
        try {
            leafListValues = childLeafListHelper.getValue(this);
        } catch (GetAttributeException e) {
            //we should not be getting this exception, so throwing as RT exception
            throw new RuntimeException(e);
        }
        if(leafListValues !=null && leafListValues.size() == 1){
            // Last leaf-list being deleted.
            return true;
        }else{
            List<EditChangeNode> editChangeNodes = editNode.getChangeNodes();
            for(ConfigLeafAttribute value : leafListValues){
                boolean found = false;
                for(EditChangeNode editChangeNode : editChangeNodes){
                    if(value.equals(editChangeNode.getConfigLeafAttribute())){
                        found = true;
                        break;
                    }
                }
                if(!found){
                    return false;
                }
            }
        }
        return true;
    }

    private void addCommandsForChildNodeChanges(EditContext parentEditContext, CompositeEditCommand parentCompositeEdit, WritableChangeTreeNode changeTreeNode) throws EditConfigException {
        EditContainmentNode parentEditNode = parentEditContext.getEditNode();
        List<EditContainmentNode> childEditNodes = new ArrayList<>(parentEditNode.getChildren());
        for(EditContainmentNode childEditNode: childEditNodes){
            EditContext childEditContext = new EditContext(childEditNode, parentEditContext.getNotificationContext(), parentEditContext.getErrorOption(), parentEditContext.getClientInfo());
            childEditContext.setParentContext(parentEditContext);
            if(EditConfigOperations.CREATE.equals(childEditNode.getEditOperation())){
                addCreateCommands(childEditContext, parentCompositeEdit, changeTreeNode);
            }else if(EditConfigOperations.MERGE.equals(childEditNode.getEditOperation())){
                addMergeCommands(childEditContext, parentCompositeEdit, changeTreeNode);
            }else if(EditConfigOperations.DELETE.equals(childEditNode.getEditOperation())){
                addDeleteCommands(childEditContext, parentCompositeEdit, changeTreeNode);
            }else if(EditConfigOperations.REMOVE.equals(childEditNode.getEditOperation())){
                addRemoveCommands(childEditContext, parentCompositeEdit, changeTreeNode);
            }else if(EditConfigOperations.REPLACE.equals(childEditNode.getEditOperation())){
                addReplaceCommands(childEditContext, parentCompositeEdit, changeTreeNode);
            }else{
                addNoneCommands(childEditContext,parentCompositeEdit, changeTreeNode);
            }
        }

    }

    private void addReplaceCommands(EditContext editContext, CompositeEditCommand parentCompositeEdit, WritableChangeTreeNode changeTreeNode) throws EditConfigException {
        EditContainmentNode childEditNode = editContext.getEditNode();
        ModelNode child;
        ChildContainerHelper childContainerHelper = getModelNodeHelperRegistry().getChildContainerHelper(this.getModelNodeSchemaPath(), childEditNode.getQName());
        if(childContainerHelper != null){
            try {
                //To know whether child exists or not
                child = ModelNodeConstraintProcessor.getChildNode(childContainerHelper,
                        this, childEditNode);
                ReplaceContainerCommand replaceCommand = new ReplaceContainerCommand(editContext, getModelNodeHelperRegistry().getDefaultCapabilityCommandInterceptor(), changeTreeNode).addReplaceInfo(childContainerHelper, this);
                decorateAndAppend(editContext, ModelNodeChangeType.replace, parentCompositeEdit, replaceCommand, changeTreeNode);
            } catch (EditConfigException e) {
                // create if the node not found
                String errorMessage = e.getRpcError().getErrorMessage();
                if (errorMessage.contains(ModelNodeConstraintProcessor.DATA_DOES_NOT_EXIST) || errorMessage.contains(ModelNodeConstraintProcessor.MODEL_NODE_NOT_FOUND)) {
                    CreateContainerCommand createCommand = new CreateContainerCommand(editContext, getModelNodeHelperRegistry().getDefaultCapabilityCommandInterceptor(), changeTreeNode).addCreateInfo(childContainerHelper, this);
                    decorateAndAppend(editContext, ModelNodeChangeType.replace, parentCompositeEdit, createCommand, changeTreeNode);
                } else {
                    throw e;
                }
            }
        }else {
            ChildListHelper childListHelper = getModelNodeHelperRegistry().getChildListHelper(this.getModelNodeSchemaPath(), childEditNode.getQName());
            try {
                child = ModelNodeConstraintProcessor.getChildNode(childListHelper, this, childEditNode);
                ReplaceListCommand addCommand = new ReplaceListCommand(editContext,
                        getModelNodeHelperRegistry().getDefaultCapabilityCommandInterceptor(), changeTreeNode)
                        .addReplaceInfo(childListHelper, this, child);
                decorateAndAppend(editContext, ModelNodeChangeType.replace, parentCompositeEdit, addCommand, changeTreeNode);
            } catch (EditConfigException e) {
                // create if the node not found
                String errorMessage = e.getRpcError().getErrorMessage();
                if (errorMessage.contains(ModelNodeConstraintProcessor.DATA_DOES_NOT_EXIST) || errorMessage.contains(ModelNodeConstraintProcessor.MODEL_NODE_NOT_FOUND)) {
                    CreateListCommand addCommand = new CreateListCommand(editContext, getModelNodeHelperRegistry().getDefaultCapabilityCommandInterceptor(), changeTreeNode).addAddInfo(childListHelper, this);
                    decorateAndAppend(editContext, ModelNodeChangeType.replace, parentCompositeEdit, addCommand, changeTreeNode);
                } else {
                    throw e;
                }
            }
        }
    }

    private void appendDeleteCommandsForNonExistingChildren(EditContext editContext, CompositeEditCommand parentCompositeEdit, EditContainmentNode childEditNode, ModelNode child, WritableChangeTreeNode ctn) {
        if(EditConfigOperations.REPLACE.equals(childEditNode.getEditOperation())){
            deleteChildrenNotPresentInEditNode(editContext, parentCompositeEdit, childEditNode, child, child.getModelNodeSchemaPath(), ctn);
        }
    }

    private void deleteChildrenNotPresentInEditNode(EditContext editContext, CompositeEditCommand compositeEdit,
            EditContainmentNode editNode, ModelNode modelNode, SchemaPath schemaPath, WritableChangeTreeNode parentChangeTreeNode) {

        Collection<DataSchemaNode> firstLevelChildSchemaNodes = getMountRegistry().getNonChoiceChildren(schemaPath);
        Set<QName> listKeys = ReplaceMNUtil.getKeysFromModelNode(modelNode).keySet();
        EditChangeSource firstLevelChildChangeSource = editNode.getChangeSource();
        for (DataSchemaNode firstLevelChildSchemaNode : firstLevelChildSchemaNodes) {
            if(firstLevelChildSchemaNode.isConfiguration()){
                QName firstLevelChildQName = firstLevelChildSchemaNode.getQName();
                if(firstLevelChildSchemaNode instanceof ContainerSchemaNode){
                    ModelNode firstLevelChild = modelNode.getChildModelNode(firstLevelChildQName);
                    if (firstLevelChild!=null && !ReplaceMNUtil.checkModelNodeExistsInEditNode(firstLevelChild,editNode)) {
                        ChildContainerHelper firstLevelChildContainerHelper = getModelNodeHelperRegistry().getChildContainerHelper
                                (modelNode.getModelNodeSchemaPath(), firstLevelChildQName);
                        ChangeCommand removeCommand = new DeleteContainerCommand().addDeleteInfo(firstLevelChildContainerHelper, modelNode, firstLevelChild, firstLevelChildChangeSource);
                        decorateAndAppend(editContext, ModelNodeChangeType.delete, compositeEdit, removeCommand, parentChangeTreeNode);
                    }
                } else if (firstLevelChildSchemaNode instanceof ListSchemaNode){
                    Collection<ModelNode> firstLevelChildren = modelNode.getChildModelNodes(firstLevelChildQName, Collections.emptyList()); // FIXME
                    for (ModelNode firstLevelChild : firstLevelChildren) {
                        if(firstLevelChild!=null && !ReplaceMNUtil.checkModelNodeExistsInEditNode(firstLevelChild,editNode)){
                            ChildListHelper childListHelper = getModelNodeHelperRegistry().getChildListHelper
                                    (modelNode.getModelNodeSchemaPath(), firstLevelChildQName);
                            ChangeCommand removeCommand = new DeleteListCommand().addRemoveInfo(childListHelper, modelNode, firstLevelChild, firstLevelChildChangeSource);
                            decorateAndAppend(editContext, ModelNodeChangeType.delete, compositeEdit, removeCommand, parentChangeTreeNode);
                        }
                    }
                } else if (firstLevelChildSchemaNode instanceof LeafSchemaNode && !listKeys.contains(firstLevelChildQName)){
                    ConfigAttributeHelper firstLevelChildConfigAttrHelper = getModelNodeHelperRegistry().getConfigAttributeHelper
                            (modelNode.getModelNodeSchemaPath(), firstLevelChildQName);
                    if (firstLevelChildConfigAttrHelper!=null && !ReplaceMNUtil.checkLeafExistsInEditNode(modelNode, firstLevelChildQName, editNode , firstLevelChildConfigAttrHelper)) {
                        DeleteLeafCommand deleteLeafCommand = new DeleteLeafCommand().
                                addDeleteInfo(getMountRegistry(),editContext,firstLevelChildConfigAttrHelper,modelNode, firstLevelChildQName,true, firstLevelChildChangeSource);
                        decorateAndAppend(editContext,ModelNodeChangeType.delete, compositeEdit, deleteLeafCommand, parentChangeTreeNode);
                    }
                } else if (firstLevelChildSchemaNode instanceof LeafListSchemaNode) {
                    ChildLeafListHelper firstLevelChildLeafListHelper = getModelNodeHelperRegistry().getConfigLeafListHelper(modelNode.getModelNodeSchemaPath(), firstLevelChildQName);
                    if (firstLevelChildLeafListHelper != null && !ReplaceMNUtil.checkExistingLeafListIsPresentInEditNode(modelNode, firstLevelChildQName, editNode, firstLevelChildLeafListHelper)) {
                        //Removes all the leaf-lists if there is no changeNodes for that leaf-list in current editNode else SetConfigAttributeCommands handles it
                        DeleteLeafListCommand deleteLeafListCommand = new DeleteLeafListCommand().addDeleteInfo(firstLevelChildLeafListHelper, modelNode,firstLevelChildChangeSource);
                        decorateAndAppend(editContext,ModelNodeChangeType.delete, compositeEdit, deleteLeafListCommand, parentChangeTreeNode);
                    }
                }
            }
        }
    }

    private void addRemoveCommands(EditContext editContext, CompositeEditCommand parentCompositeEdit, WritableChangeTreeNode changeTreeNode) throws EditConfigException {
        EditContainmentNode childEditNode = editContext.getEditNode();
        ChildContainerHelper childContainerHelper = getModelNodeHelperRegistry().getChildContainerHelper(this.getModelNodeSchemaPath(), childEditNode.getQName());

        if(childContainerHelper != null){
            ModelNode child = null;
            try {
                child = ModelNodeConstraintProcessor.getChildNode(childContainerHelper, this, childEditNode);
                DeleteContainerCommand deleteCommand = new DeleteContainerCommand().addDeleteInfo(childContainerHelper, this, child,
                        childEditNode.getChangeSource());
                decorateAndAppend(editContext, ModelNodeChangeType.remove, parentCompositeEdit, deleteCommand, changeTreeNode);
            } catch (EditConfigException e) {
                String errorMessage = e.getRpcError().getErrorMessage();
                if (errorMessage.contains(ModelNodeConstraintProcessor.DATA_DOES_NOT_EXIST) || errorMessage.contains(ModelNodeConstraintProcessor.MODEL_NODE_NOT_FOUND)) {
                    // ignore, "remove" unlike "delete" should ignore if the item is not present
                } else {
                    throw e;
                }
            }
        }else {
            ChildListHelper childListHelper = getModelNodeHelperRegistry().getChildListHelper(this.getModelNodeSchemaPath(), childEditNode.getQName());
            try {
                ModelNode child = ModelNodeConstraintProcessor
                        .getChildNode(childListHelper, this, childEditNode);
                ChangeCommand removeCommand = new DeleteListCommand().addRemoveInfo(childListHelper, this, child,
                        childEditNode.getChangeSource());
                decorateAndAppend(editContext, ModelNodeChangeType.remove, parentCompositeEdit, removeCommand, child, changeTreeNode);
            } catch (EditConfigException e) {
                String errorMessage = e.getRpcError().getErrorMessage();
                if (errorMessage.contains(ModelNodeConstraintProcessor.DATA_DOES_NOT_EXIST) || errorMessage.contains(ModelNodeConstraintProcessor.MODEL_NODE_NOT_FOUND)) {
                    // ignore, "remove" unlike "delete" should ignore if the item is not present
                } else {
                    throw e;
                }
            }
        }
    }

    protected void testMerge(EditContext editContext, CompositeEditCommand parentCompositeEdit) throws EditConfigException {
        addMergeCommands(editContext,parentCompositeEdit, null);
    }

    private void addMergeCommands(EditContext editContext, CompositeEditCommand parentCompositeEdit, WritableChangeTreeNode changeTreeNode) throws EditConfigException {
        EditContainmentNode childEditNode = editContext.getEditNode();

        ChildContainerHelper childContainerHelper = getModelNodeHelperRegistry().getChildContainerHelper(this.getModelNodeSchemaPath(), childEditNode.getQName());
        if(childContainerHelper != null){
            try {
                ModelNode value = childContainerHelper.getValue(this);
                if(value == null){
                    // empty child container case. create child container
                    addCreateCommands(editContext, parentCompositeEdit, changeTreeNode);
                } else {
                    Command childCommand = value.getEditCommand(editContext, changeTreeNode);
                    parentCompositeEdit.appendCommand(childCommand);
                }
            } catch (ModelNodeGetException e) {
                LOGGER.error("Could not get child containers " , e);
                EditConfigException exception = new EditConfigException(NetconfRpcErrorUtil.getNetconfRpcErrorForModelNodeId(getModelNodeId(),NetconfRpcErrorTag.DATA_MISSING,
                        "Could not get child containers " +e.getMessage(), getSchemaRegistry()));
                exception.addSuppressed(e);
                throw exception;
            }
        }else {
            ChildListHelper childListHelper = getModelNodeHelperRegistry().getChildListHelper(this.getModelNodeSchemaPath(), childEditNode.getQName());
            ModelNode child = null;
            try {
                child = ModelNodeConstraintProcessor.getChildNode(childListHelper, this, childEditNode);
            } catch (EditConfigException e) {
                String errorMessage = e.getRpcError().getErrorMessage();
                if (errorMessage.contains(ModelNodeConstraintProcessor.DATA_DOES_NOT_EXIST) || errorMessage.contains(ModelNodeConstraintProcessor.MODEL_NODE_NOT_FOUND)) {
                    addCreateCommands(editContext, parentCompositeEdit, changeTreeNode);
                    return;
                } else {
                    throw e;
                }
            }
            try {
                //SubSystem.testChange() should be called even if MergeListCommand is not appended to make sure the changes are proper
                child.getSubSystem().testChange(editContext, ModelNodeChangeType.merge, child, getModelNodeHelperRegistry());
                if(isPositionChanged(editContext.getEditNode(), this)) {
                    //MergeListCommand is appended to composite command only when the list is ordered-by user and position needs to be updated
                    MergeListCommand mergeCommand = new MergeListCommand();
                    mergeCommand.addAddInfo(childListHelper, this, editContext, child, changeTreeNode);
                    decorateAndAppend(editContext, ModelNodeChangeType.merge, parentCompositeEdit, mergeCommand, changeTreeNode);
                } else {
                    Command childCommand = child.getEditCommand(new EditContext(editContext), changeTreeNode);
                    parentCompositeEdit.appendCommand(childCommand);
                }
            } catch (SubSystemValidationException exception) {
                if (exception.getRpcError() == null) {
                    NetconfRpcError error = new NetconfRpcError(NetconfRpcErrorTag.OPERATION_FAILED,
                            NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error, exception.getMessage());
                    throw new EditConfigException(error);
                } else {
                    throw new EditConfigException(exception.getRpcError());
                }
            }
        }
    }

    private boolean isPositionChanged(EditContainmentNode childNode, ModelNode parentNode) {
        return SchemaRegistryUtil.isListOrderedByUser(childNode.getQName(), parentNode.getModelNodeSchemaPath(), childNode.getSchemaRegistry())
                && childNode.getInsertOperation() != null;
    }

    private void addDeleteAttributeCommandForChangedNodes(EditContext editContext, CompositeEditCommand compositeEditCommand,
            ConfigAttributeHelper helper, EditChangeNode changeNode, WritableChangeTreeNode changeTreeNode) throws EditConfigException {
        DeleteLeafCommand cmd = new DeleteLeafCommand().addDeleteInfo(getSchemaRegistry(), editContext, helper, this,
                changeNode.getQName(), true, changeNode.getChangeSource());
        decorateAndAppend(editContext, ModelNodeChangeType.merge, compositeEditCommand, cmd, changeTreeNode);
    }

    private void addDeleteCommandsForContainerChoiceCaseNode(ChildContainerHelper childContainerHelper,
            EditContext editContext, CompositeEditCommand parentCompositeEdit,
            WritableChangeTreeNode changeTreeNode, EditChangeSource changeSource) throws EditConfigException {
        ModelNode value = null;
        try {
            value = childContainerHelper.getValue(this);
            if(value == null){
                return;
            }
        } catch (ModelNodeGetException e) {
            LOGGER.error("Could not get child containers " , e);
            EditConfigException exception = new EditConfigException(NetconfRpcErrorUtil.getNetconfRpcErrorForModelNodeId(getModelNodeId(),NetconfRpcErrorTag.DATA_MISSING,"Could not get child containers " +e.getMessage(), getSchemaRegistry()));
            exception.addSuppressed(e);
            exception.addSuppressed(e);
            throw exception;
        }
        DeleteContainerCommand deleteCommand = new DeleteContainerCommand().addDeleteInfo(childContainerHelper, this, value,changeSource);
        decorateAndAppend(editContext, ModelNodeChangeType.delete, parentCompositeEdit, deleteCommand, changeTreeNode);
    }

    private void addDeleteCommandsForListChoiceCaseNode(ChildListHelper childListHelper, EditContext editContext,
            CompositeEditCommand parentCompositeEdit, WritableChangeTreeNode changeTreeNode,
            EditChangeSource changeSource) throws EditConfigException {
        Collection<ModelNode> children;
        try {
            children = childListHelper.getValue(this, Collections.<QName, ConfigLeafAttribute>emptyMap());
            if (children.isEmpty()) {
                return;
            } else {
                for (ModelNode child : children) {
                    ChangeCommand removeCommand = new DeleteListCommand().addRemoveInfo(childListHelper, this, child,changeSource);
                    decorateAndAppend(editContext, ModelNodeChangeType.delete, parentCompositeEdit, removeCommand, changeTreeNode);
                }
            }
        } catch (ModelNodeGetException e) {
            LOGGER.error("Could not get child containers ", e);
            NetconfRpcError error = NetconfRpcErrorUtil.getNetconfRpcErrorForModelNodeId(getModelNodeId(),NetconfRpcErrorTag.DATA_MISSING, "Could not get child containers", getSchemaRegistry());
            throw new EditConfigException(error);
        }
    }



    private void deleteOtherCaseSiblings(EditContext editContext, CompositeEditCommand parentCompositeEdit, QName childQName, WritableChangeTreeNode changeTreeNode) throws EditConfigException {
        EditContainmentNode editNode = editContext.getEditNode();
        if(EditChangeSource.user.equals(editNode.getChangeSource())){
            SchemaPath descendantSchemaPath = getSchemaRegistry().getDescendantSchemaPath(this.getModelNodeSchemaPath(), childQName);
            if (descendantSchemaPath != null) {
                SchemaPath schemaPath = descendantSchemaPath.getParent();
                if (schemaPath != null) {
                    deleteOtherCaseNodes(editContext, parentCompositeEdit, schemaPath, changeTreeNode);
                }
            }
        }
    }

    private void deleteOtherCaseNodes(EditContext editContext, CompositeEditCommand compositeEdit,
            SchemaPath schemaPath, WritableChangeTreeNode changeTreeNode) throws EditConfigException {
        Set<CaseSchemaNode> caseSchemaNodes = new HashSet<>();
        DataSchemaNode node = getSchemaRegistry().getDataSchemaNode(schemaPath);
        if (node != null && node instanceof ChoiceSchemaNode) {
            ChoiceSchemaNode choiceSchemaNode = (ChoiceSchemaNode) node;
            Collection<CaseSchemaNode> allCaseSchemaNodes = choiceSchemaNode.getCases().values();
            caseSchemaNodes.addAll(allCaseSchemaNodes);
        } else {
            caseSchemaNodes = ChoiceCaseNodeUtil.checkIsCaseNodeAndReturnAllOtherCases(getSchemaRegistry(), schemaPath);
        }
        if (caseSchemaNodes != null && !caseSchemaNodes.isEmpty()) {
            List<DataSchemaNode> schemaNodes = ChoiceCaseNodeUtil.getAllNodesFromCases(caseSchemaNodes);
            for (DataSchemaNode dataSchemaNode : schemaNodes) {
                QName qName = dataSchemaNode.getQName();
                EditContainmentNode editContainmentNode = editContext.getEditNode();
                EditChangeNode choiceCaseChangeNodeToBeDelete = null;
                EditContainmentNode choiceCaseChildNodeToBeDelete = null;
                if (dataSchemaNode instanceof LeafSchemaNode || dataSchemaNode instanceof LeafListSchemaNode) {
                    choiceCaseChangeNodeToBeDelete = new EditChangeNode(qName,
                            createConfigAttribute(dataSchemaNode));
                    choiceCaseChangeNodeToBeDelete.setOperation(EditConfigOperations.DELETE);
                    choiceCaseChangeNodeToBeDelete.setChangeSource(EditChangeSource.system);
                } else if (dataSchemaNode instanceof ContainerSchemaNode || dataSchemaNode instanceof ListSchemaNode) {
                    choiceCaseChildNodeToBeDelete = new EditContainmentNode(dataSchemaNode.getPath(), EditConfigOperations.DELETE, editContainmentNode.getSchemaRegistry(), null);
                    choiceCaseChildNodeToBeDelete.setChangeSource(EditChangeSource.system);
                }

                if (dataSchemaNode instanceof ChoiceSchemaNode) {
                    deleteOtherCaseNodes(editContext, compositeEdit, dataSchemaNode.getPath(), changeTreeNode);
                }
                try {
                    if (!(editContainmentNode.getChangeNodes().contains(choiceCaseChangeNodeToBeDelete) || isOneAmong(choiceCaseChildNodeToBeDelete, editContainmentNode.getChildren()))) {
                        SchemaPath modelNodeSchemaPath = this.getModelNodeSchemaPath();
                        ChildContainerHelper childContainerHelper = getModelNodeHelperRegistry()
                                .getChildContainerHelper(modelNodeSchemaPath, qName);
                        if (childContainerHelper != null && childContainerHelper.getValue(this) != null) {
                            editContainmentNode.addChild(choiceCaseChildNodeToBeDelete);
                            addDeleteCommandsForContainerChoiceCaseNode(childContainerHelper, editContext, compositeEdit, changeTreeNode,
                                    choiceCaseChildNodeToBeDelete.getChangeSource());
                        } else {
                            ChildListHelper childListHelper = getModelNodeHelperRegistry()
                                    .getChildListHelper(modelNodeSchemaPath, qName);
                            if (childListHelper != null) {
                                Collection<ModelNode> modelNodes = childListHelper.getValue(this,
                                        Collections.emptyMap());
                                if (modelNodes != null && !modelNodes.isEmpty()) {
                                    editContainmentNode.addChild(choiceCaseChildNodeToBeDelete);
                                    addDeleteCommandsForListChoiceCaseNode(childListHelper, editContext, compositeEdit, changeTreeNode,
                                            choiceCaseChildNodeToBeDelete.getChangeSource());
                                }
                            } else {
                                ConfigAttributeHelper configAttributeHelper = getModelNodeHelperRegistry()
                                        .getConfigAttributeHelper(modelNodeSchemaPath, qName);
                                if (configAttributeHelper != null && configAttributeHelper.getValue(this) != null) {
                                    editContainmentNode.addChangeNode(choiceCaseChangeNodeToBeDelete);
                                    addDeleteCommandForLeafChoiceCaseNode(editContext, compositeEdit, qName,
                                            configAttributeHelper, changeTreeNode, choiceCaseChangeNodeToBeDelete.getChangeSource());
                                } else {
                                    ChildLeafListHelper configLeafListHelper = getModelNodeHelperRegistry()
                                            .getConfigLeafListHelper(modelNodeSchemaPath, qName);
                                    if (configLeafListHelper != null) {
                                        Collection<ConfigLeafAttribute> leafLists = configLeafListHelper.getValue(this);
                                        if (leafLists != null && !leafLists.isEmpty()) {
                                            editContainmentNode.addChangeNode(choiceCaseChangeNodeToBeDelete);
                                            addDeleteCommandForLeafListChoiceCaseNode(compositeEdit,
                                                    configLeafListHelper, choiceCaseChangeNodeToBeDelete.getChangeSource());
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (GetAttributeException e) {
                    EditConfigException exception = new EditConfigException(e.getRpcError());
                    exception.addSuppressed(e);
                    throw exception;
                } catch (ModelNodeGetException e) {
                    NetconfRpcError error = new NetconfRpcError(NetconfRpcErrorTag.OPERATION_FAILED,
                            NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error, e.getMessage());
                    EditConfigException exception = new EditConfigException(error);
                    exception.addSuppressed(e);
                    throw exception;
                }
            }
        }
    }

    private boolean isOneAmong(EditContainmentNode newChild, List<EditContainmentNode> children) {
        if (newChild != null) {
            for (EditContainmentNode child : children) {
                if (child.getQName().equals(newChild.getQName())) {
                    return true;
                }
            }
        }
        return false;
    }

    private ConfigLeafAttribute createConfigAttribute(DataSchemaNode dataSchemaNode) {
        if(dataSchemaNode instanceof LeafSchemaNode){
            return ConfigAttributeFactory.getConfigLeafAttribute(getSchemaRegistry(), (LeafSchemaNode) dataSchemaNode, "");
        }
        return null;
    }

    private void addDeleteCommandForLeafChoiceCaseNode(EditContext editContext, CompositeEditCommand parentCompositeEdit,
            QName qName, ConfigAttributeHelper configAttributeHelper,
            WritableChangeTreeNode changeTreeNode, EditChangeSource changeSource) throws
    EditConfigException {
        try {
            if (configAttributeHelper.getValue(this) != null) {
                DeleteLeafCommand cmd = new DeleteLeafCommand()
                        .addDeleteInfo(getSchemaRegistry(), editContext, configAttributeHelper, this, qName, false, changeSource);
                decorateAndAppend(editContext, ModelNodeChangeType.delete, parentCompositeEdit, cmd, true, changeTreeNode);
            }
        } catch (GetAttributeException e) {
            LOGGER.error("Could not get leaf value", e);
            NetconfRpcError error = NetconfRpcErrorUtil.getNetconfRpcErrorForModelNodeId(getModelNodeId(),NetconfRpcErrorTag.DATA_MISSING, "Could not get leaf value", getSchemaRegistry());
            throw new EditConfigException(error);
        }
    }

    private void addDeleteCommandForLeafListChoiceCaseNode(CompositeEditCommand parentCompositeEdit,
            ChildLeafListHelper childLeafListHelper, EditChangeSource changeSource) throws EditConfigException {
        try {
            Collection<ConfigLeafAttribute> leafListValues = childLeafListHelper.getValue(this);
            if (leafListValues!=null && !leafListValues.isEmpty()) {
                DeleteLeafListCommand deleteLeafListCommand = new DeleteLeafListCommand().addDeleteInfo(childLeafListHelper,this, changeSource);
                parentCompositeEdit.appendCommand(deleteLeafListCommand);
            }
        } catch (GetAttributeException e) {
            LOGGER.error("Could not get leaf-list values", e);
            NetconfRpcError error = NetconfRpcErrorUtil.getNetconfRpcErrorForModelNodeId(getModelNodeId(),NetconfRpcErrorTag.DATA_MISSING, "Could not get leaf-list values", getSchemaRegistry());
            throw new EditConfigException(error);
        }
    }


    private void addDeleteCommands(EditContext editContext, CompositeEditCommand parentCompositeEdit, WritableChangeTreeNode changeTreeNode) throws EditConfigException {
        EditContainmentNode childEditNode = editContext.getEditNode();
        ChildContainerHelper childContainerHelper = getModelNodeHelperRegistry().getChildContainerHelper(this.getModelNodeSchemaPath(),
                childEditNode.getQName());

        if(childContainerHelper != null){
            ModelNode child = ModelNodeConstraintProcessor.getChildNode(childContainerHelper, this, childEditNode);
            DeleteContainerCommand deleteCommand = new DeleteContainerCommand().addDeleteInfo(childContainerHelper, this, child, childEditNode.getChangeSource());
            decorateAndAppend(editContext, ModelNodeChangeType.delete, parentCompositeEdit, deleteCommand, changeTreeNode);
        }else {
            ChildListHelper childListHelper = getModelNodeHelperRegistry().getChildListHelper(this.getModelNodeSchemaPath(), childEditNode
                    .getQName());
            ModelNode child = ModelNodeConstraintProcessor.getChildNode(childListHelper, this, childEditNode);
            ChangeCommand removeCommand = new DeleteListCommand().addRemoveInfo(childListHelper, this, child, childEditNode.getChangeSource());
            decorateAndAppend(editContext, ModelNodeChangeType.delete, parentCompositeEdit, removeCommand, child, changeTreeNode);

        }
    }

    private void addCreateCommands(EditContext editContext, CompositeEditCommand parentCompositeEdit, WritableChangeTreeNode changeTreeNode) throws EditConfigException {

        EditContainmentNode childEditNode = editContext.getEditNode();

        ChildContainerHelper childContainerHelper = getModelNodeHelperRegistry().getChildContainerHelper(this.getModelNodeSchemaPath(), childEditNode.getQName());

        if(childContainerHelper != null){
            // handle choice case node

            deleteOtherCaseSiblings(editContext.getParent(), parentCompositeEdit, editContext.getEditNode().getQName(), changeTreeNode);
            ModelNodeConstraintProcessor.validateExistentContainer(childContainerHelper, this, childEditNode);
            //Create command takes care of all the edits in the create subtree
            CreateContainerCommand createCommand = new CreateContainerCommand(editContext, getModelNodeHelperRegistry().getDefaultCapabilityCommandInterceptor(), changeTreeNode).addCreateInfo(childContainerHelper, this);
            decorateAndAppend(editContext, ModelNodeChangeType.create, parentCompositeEdit, createCommand, changeTreeNode);

        } else {
            ChildListHelper childListHelper = getModelNodeHelperRegistry().getChildListHelper(this.getModelNodeSchemaPath(), childEditNode.getQName());
            // handle choice case node
            deleteOtherCaseSiblings(editContext.getParent(), parentCompositeEdit, editContext.getEditNode().getQName(), changeTreeNode);
            // check if the child already exists ...
            ModelNodeConstraintProcessor.validateExistentList(childListHelper, this, childEditNode);

            // Add command takes care of all the edits in the create subtree
            CreateListCommand addCommand = new CreateListCommand(editContext, getModelNodeHelperRegistry().getDefaultCapabilityCommandInterceptor(), changeTreeNode).addAddInfo(childListHelper, this);
            decorateAndAppend(editContext, ModelNodeChangeType.create, parentCompositeEdit, addCommand, changeTreeNode);
        }
    }

    private void addNoneCommands(EditContext editContext, CompositeEditCommand parentCompositeEdit, WritableChangeTreeNode changeTreeNode) throws EditConfigException {
        EditContainmentNode childEditNode = editContext.getEditNode();

        ChildContainerHelper childContainerHelper = getModelNodeHelperRegistry().getChildContainerHelper(this.getModelNodeSchemaPath(), childEditNode.getQName());
        if(childContainerHelper != null){
            ModelNode child = ModelNodeConstraintProcessor.getChildNode(childContainerHelper, this, childEditNode);
            try {
                child.getSubSystem().testChange(editContext, ModelNodeChangeType.none, child,
                        getModelNodeHelperRegistry());
            } catch (SubSystemValidationException exception) {
                if (exception.getRpcError() == null) {
                    NetconfRpcError error = new NetconfRpcError(NetconfRpcErrorTag.OPERATION_FAILED,
                            NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error, exception.getMessage());
                    throw new EditConfigException(error);
                } else {
                    throw new EditConfigException(exception.getRpcError());
                }
            }
            Command childCommand = child.getEditCommand(new EditContext(editContext), changeTreeNode);
            parentCompositeEdit.appendCommand(childCommand);
        }else {
            ChildListHelper childListHelper = getModelNodeHelperRegistry().getChildListHelper(this.getModelNodeSchemaPath(), childEditNode.getQName());
            ModelNode child = ModelNodeConstraintProcessor.getChildNode(childListHelper, this,
                    childEditNode);
            try {
                child.getSubSystem().testChange(editContext, ModelNodeChangeType.none, child,
                        getModelNodeHelperRegistry());
            } catch (SubSystemValidationException exception) {
                if (exception.getRpcError() == null) {
                    NetconfRpcError error = new NetconfRpcError(NetconfRpcErrorTag.OPERATION_FAILED,
                            NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error, exception.getMessage());
                    throw new EditConfigException(error);
                } else {
                    throw new EditConfigException(exception.getRpcError());
                }
            }
            Command childCommand = child.getEditCommand(new EditContext(editContext), changeTreeNode);
            parentCompositeEdit.appendCommand(childCommand);
        }
    }

    private void decorateAndAppend(EditContext editContext, ModelNodeChangeType changeType, CompositeEditCommand parentCompositeEdit, ChangeCommand innerCommand, boolean isImpliedChange, ModelNode childModelNode, WritableChangeTreeNode changeTreeNode) throws EditConfigException {
        ModelNodeChange change = new ModelNodeChange(changeType, editContext.getEditNode());
        SubSystemValidationDecorator subSystemValidationDecorator = new SubSystemValidationDecorator(innerCommand, getSubSystem(), editContext, changeType, this, getModelNodeHelperRegistry());
        NotificationDecorator decoratedCommand = new NotificationDecorator(subSystemValidationDecorator, this, change,
                editContext.getNotificationContext(), editContext.getClientInfo(), isImpliedChange, childModelNode, changeTreeNode);
        parentCompositeEdit.appendCommand(decoratedCommand);
    }

    private void decorateAndAppend(EditContext editContext, ModelNodeChangeType changeType, CompositeEditCommand parentCompositeEdit, ChangeCommand innerCommand, WritableChangeTreeNode changeTreeNode) throws EditConfigException {
        decorateAndAppend(editContext, changeType, parentCompositeEdit, innerCommand, false, changeTreeNode);
    }

    private void decorateAndAppend(EditContext editContext, ModelNodeChangeType changeType, CompositeEditCommand parentCompositeEdit, ChangeCommand innerCommand, ModelNode childModelNode, WritableChangeTreeNode changeTreeNode) throws EditConfigException {
        decorateAndAppend(editContext, changeType, parentCompositeEdit, innerCommand, false, childModelNode, changeTreeNode);
    }

    private void decorateAndAppend(EditContext editContext, ModelNodeChangeType changeType, CompositeEditCommand parentCompositeEdit, ChangeCommand innerCommand, boolean isImpliedChange, WritableChangeTreeNode changeTreeNode) throws EditConfigException {
        decorateAndAppend(editContext, changeType, parentCompositeEdit, innerCommand, isImpliedChange, null, changeTreeNode);
    }

    private void addCommandsForCurrentNodeLeafChanges(EditContext editContext, CompositeEditCommand compositeEdit, WritableChangeTreeNode changeTreeNode) throws EditConfigException {
        if (hasDeltaChanges(editContext, this, compositeEdit, changeTreeNode)) {
            SetConfigAttributeCommand setCommand = new SetConfigAttributeCommand().addSetInfo(getSchemaRegistry(),
                    getModelNodeHelperRegistry().getConfigAttributeHelpers(this.getModelNodeSchemaPath()),
                    getModelNodeHelperRegistry().getConfigLeafListHelpers(this.getModelNodeSchemaPath()), this, editContext, changeTreeNode);
            decorateAndAppend(editContext, ModelNodeChangeType.merge, compositeEdit, setCommand, changeTreeNode);
            Map<EditChangeNode, SchemaPath> nodesToBeCheckedForCases = new HashMap<>();

            for (EditChangeNode changeNode : editContext.getEditNode().getChangeNodes()) {
                if(EditChangeSource.user.equals(changeNode.getChangeSource())){
                    SchemaPath changeNodeSP = getSchemaRegistry().getDescendantSchemaPath(this.getModelNodeSchemaPath(),
                            changeNode.getQName());
                    SchemaPath parentSchemaPath = changeNodeSP.getParent();
                    if (parentSchemaPath != null) {
                        nodesToBeCheckedForCases.put(changeNode, parentSchemaPath);
                    }
                }
            }
            for (Entry<EditChangeNode, SchemaPath> nodeToBeCheckedForCases : nodesToBeCheckedForCases.entrySet()) {
                deleteOtherCaseNodes(editContext, compositeEdit, nodeToBeCheckedForCases.getValue(), changeTreeNode);
            }
        }
    }

    private boolean hasDeltaChanges(EditContext editContext, ModelNode changeModeNode, CompositeEditCommand compositeEdit, WritableChangeTreeNode changeTreeNode) throws EditConfigException {
        List<EditChangeNode> changeNodes = editContext.getEditNode().getChangeNodes();
        boolean hasDeltaChanges = false;
        LeafListContext leafListContext = new LeafListContext();
        if (null != changeNodes && !changeNodes.isEmpty()) {
            Iterator<EditChangeNode> changeNodesItr = changeNodes.iterator();
            while (changeNodesItr.hasNext()) {
                EditChangeNode editChangeNode = changeNodesItr.next();
                QName qName = editChangeNode.getQName();
                ConfigAttributeHelper configAttributeHelper = getModelNodeHelperRegistry().
                        getConfigAttributeHelper(changeModeNode.getModelNodeSchemaPath(), qName);
                try {
                    LOGGER.debug("Compare current value and changing value of: " + qName.toString());
                    if (null != configAttributeHelper) {
                        ConfigLeafAttribute configLeafAttribute = configAttributeHelper.getValue(changeModeNode);
                        String dbValue = configLeafAttribute != null ? configLeafAttribute.getStringValue() : null;
                        String changingValue = editChangeNode.getValue();

                        boolean isPassword = EncryptDecryptUtil.isPassword(configAttributeHelper.getLeafSchemaNode(), getSchemaRegistry());
                        boolean isValueSame = areValuesSame(dbValue, changingValue, isPassword);
                        boolean isEmpty = isChangingValueEmpty(changingValue, isPassword);
                        boolean setDeltaChanges = true;

                        switch (editChangeNode.getOperation()) {
                        case EditConfigOperations.CREATE:
                            if(isValueSame){
                                throw new EditConfigException(NetconfRpcErrorUtil.getNetconfRpcErrorForModelNode(changeModeNode,
                                        NetconfRpcErrorTag.DATA_EXISTS,"'" + qName.getLocalName() + "' already exists with same Value. Create request Failed."));
                            }
                            break;
                        case EditConfigOperations.DELETE:
                            if(dbValue != null && (isEmpty || isValueSame)){
                                addDeleteAttributeCommandForChangedNodes(editContext, compositeEdit, configAttributeHelper, editChangeNode, changeTreeNode);
                            } else {
                                throw new EditConfigException(NetconfRpcErrorUtil.getNetconfRpcErrorForModelNode(changeModeNode,
                                        NetconfRpcErrorTag.DATA_MISSING, ModelNodeConstraintProcessor.DATA_DOES_NOT_EXIST + " " + qName.getLocalName()));
                            }
                            break;
                        case EditConfigOperations.REMOVE:
                            if (dbValue != null && (isEmpty || isValueSame)) {
                                addDeleteAttributeCommandForChangedNodes(editContext, compositeEdit, configAttributeHelper, editChangeNode, changeTreeNode);
                            } else {
                                //if db value is null or if the supplied value does not match the DB value, there is nothing to do.
                                setDeltaChanges = false;
                            }
                            break;
                        case EditConfigOperations.REPLACE:
                            if (dbValue != null) {
                                FlagForRestPutOperations.setInstanceReplaceFlag();
                            }
                            break;
                        case EditConfigOperations.MERGE:
                            //proceed further
                        }
                        if (!isValueSame && setDeltaChanges) {
                            hasDeltaChanges = true;
                        }

                    } else {
                        // if it is not a leaf, it should be a leaf-list
                        ChildLeafListHelper configLeafListHelper = getModelNodeHelperRegistry()
                                .getConfigLeafListHelper(changeModeNode.getModelNodeSchemaPath(), qName);

                        if(configLeafListHelper != null) {
                            String newValue = editChangeNode.getValue();
                            leafListContext.populateExistingLeafListValues(configLeafListHelper, changeModeNode);
                            leafListContext.addLeafListPresentInEditNode(configLeafListHelper, newValue);
                            Collection<ConfigLeafAttribute> currentValues = leafListContext.getExistingLeafLists(configLeafListHelper);
                            boolean isPassword = EncryptDecryptUtil.isPassword(configLeafListHelper.getLeafListSchemaNode(), getSchemaRegistry());
                            if(newValue != null) {
                                if(!isChangingValueEmpty(newValue, isPassword)) {
                                    if(EditConfigOperations.CREATE.equals(editChangeNode.getOperation())) {
                                        if(!isDataAlreadyExists(newValue, currentValues, isPassword)) {
                                            hasDeltaChanges =true;
                                        } else {
                                            NetconfRpcError error = NetconfRpcErrorUtil.getNetconfRpcErrorForModelNode(changeModeNode, NetconfRpcErrorTag.DATA_EXISTS,
                                                    "Create instance attempted while the instance - " + qName.getLocalName() + " already exists with specified value; Request Failed.");
                                            throw new EditConfigException(error);
                                        }
                                    } else if(EditConfigOperations.MERGE.equals(editChangeNode.getOperation())) {
                                        if(!isDataAlreadyExists(newValue, currentValues, isPassword) || editChangeNode.getInsertOperation() != null) {
                                            hasDeltaChanges =true;
                                        }
                                    } else if(EditConfigOperations.REPLACE.equals(editChangeNode.getOperation())) {
                                        if(!isDataAlreadyExists(newValue, currentValues, isPassword) || editChangeNode.getInsertOperation() != null) {
                                            hasDeltaChanges =true;
                                        } else {
                                            FlagForRestPutOperations.setInstanceReplaceFlag();
                                        }
                                    } else if(EditConfigOperations.DELETE.equals(editChangeNode.getOperation())) {
                                        if(!isDataAlreadyExists(newValue, currentValues, isPassword)) {
                                            NetconfRpcError error = NetconfRpcErrorUtil.getNetconfRpcErrorForModelNode(changeModeNode, NetconfRpcErrorTag.DATA_MISSING,
                                                    "Delete instance attempted while the instance - " + qName.getLocalName() + " does not exist in datastore. Delete request Failed.");
                                            throw new EditConfigException(error);
                                        } else {
                                            hasDeltaChanges =true;
                                        }
                                    } else if(EditConfigOperations.REMOVE.equals(editChangeNode.getOperation())) {
                                        if(isDataAlreadyExists(newValue, currentValues, isPassword)) {
                                            hasDeltaChanges =true;
                                        }
                                    }
                                } else {
                                    NetconfRpcError error = NetconfRpcErrorUtil.getNetconfRpcErrorForModelNode(changeModeNode, NetconfRpcErrorTag.INVALID_VALUE,
                                            "Value for the node '" + qName.getLocalName() + "' should not be empty");
                                    throw new EditConfigException(error);
                                }
                            }
                        }
                    }
                } catch (GetAttributeException e) {
                    LOGGER.error("Could not fetch current value of  " + qName, e);
                    return false;
                }
            }
        }
        if(!hasDeltaChanges && EditConfigOperations.REPLACE.equals(editContext.getEditNode().getEditOperation())){
            return leafListContext.checkIfAnyLeafListsToBeDeleted();
        }
        return hasDeltaChanges;
    }

    private boolean isChangingValueEmpty(String changingValue, boolean isPassword) {
        if(isPassword && ENCR_STR_PATTERN.matcher(changingValue).matches()){
            return CryptUtil2.decrypt(changingValue).isEmpty();
        }else{
            return changingValue.isEmpty();
        }
    }

    private boolean areValuesSame(String dbValue, String changingValue, boolean isPassword) {
        if(isPassword){
            if((dbValue == null && changingValue != null) || (dbValue != null && changingValue == null)) {
                return false;
            } else {
                String decryptedDBValue = dbValue;
                String decryptedchangingValue = changingValue;
                if(ENCR_STR_PATTERN.matcher(dbValue).matches()){
                    decryptedDBValue = CryptUtil2.decrypt(dbValue);
                }
                if(ENCR_STR_PATTERN.matcher(changingValue).matches()){
                    decryptedchangingValue =  CryptUtil2.decrypt(changingValue);
                }
                return decryptedchangingValue.equals(decryptedDBValue);
            }
        }else{
            return changingValue.equals(dbValue);
        }
    }

    private boolean isDataAlreadyExists(String newValue, Collection<ConfigLeafAttribute> currentValues, boolean isPassword) {
        boolean dataExists = false;
        if (!isPassword) {
            for (ConfigLeafAttribute currentValue : currentValues) {
                if (!currentValue.getStringValue().isEmpty()) {
                    if (newValue.equals(currentValue.getStringValue())) {
                        dataExists = true;
                        break;
                    }
                }
            }
        }else{
            Collection<String> decryptedCurrentValues = currentValues.stream()
                    .map(value -> {
                        if(ENCR_STR_PATTERN.matcher(value.getStringValue()).matches()){
                            return CryptUtil2.decrypt(value.getStringValue());
                        }else{
                            return value.getStringValue();
                        }
                    }).collect(Collectors.toList());
            String decryptedValue = ENCR_STR_PATTERN.matcher(newValue).matches() ? CryptUtil2.decrypt(newValue) : newValue;
            for (String currentValue : decryptedCurrentValues) {
                if (decryptedValue.equals(currentValue)) {
                    dataExists = true;
                    break;
                }
            }
        }
        return dataExists;
    }

    public boolean isThisInstance(EditContainmentNode editNode) throws EditConfigException {
        boolean thisInstance = true;
        for (QName key : getModelNodeHelperRegistry().getNaturalKeyHelpers(this.getModelNodeSchemaPath()).keySet()) {
            for (EditMatchNode matchNode : editNode.getMatchNodes()) {
                if (matchNode.getQName().equals(key)) {
                    try {
                        if (!matchNode.getValue().equals(getModelNodeHelperRegistry()
                                .getNaturalKeyHelper(this.getModelNodeSchemaPath(), key).getValue(this).getStringValue())) {
                            thisInstance = false;
                            break;
                        }
                    } catch (GetAttributeException e) {
                        LOGGER.error("Could get value from ModelNode", e);
                        EditConfigException exception = new EditConfigException(NetconfRpcErrorUtil.getNetconfRpcErrorForModelNodeId(getModelNodeId(),NetconfRpcErrorTag.DATA_MISSING,"Could get value from ModelNode"+e.getMessage(), getSchemaRegistry()));
                        exception.addSuppressed(e);
                        throw exception;
                    }
                }
            }
        }
        return thisInstance;
    }

    public void setKeyAttributes(Map<QName, ConfigLeafAttribute> constructorArgs) throws SetAttributeException {
        for(Map.Entry<QName,ConfigLeafAttribute> qNameConfigLeafAttributeEntry : constructorArgs.entrySet()){
            ConfigAttributeHelper keyAttributeHelper = getModelNodeHelperRegistry().getNaturalKeyHelpers(this.getModelNodeSchemaPath()).get
                    (qNameConfigLeafAttributeEntry.getKey());
            keyAttributeHelper.setValue(this,qNameConfigLeafAttributeEntry.getValue());
        }

    }

    @Override
    public ModelNodeId getModelNodeId() {
        if (m_modelNodeId == null) {
            synchronized (this) {
                if (m_modelNodeId == null) {
                    m_modelNodeId = new ModelNodeId(m_parentNodeId);
                    m_modelNodeId.addRdn(ModelNodeRdn.CONTAINER, getQName().getNamespace().toString(), getContainerName());
                    DataSchemaNode dataSchemaNode = m_schemaRegistry.getDataSchemaNode(getModelNodeSchemaPath());
                    if (dataSchemaNode instanceof ListSchemaNode) {
                        Map<QName, ConfigAttributeHelper> naturalKeyHelpers = getModelNodeHelperRegistry().getNaturalKeyHelpers(this
                                .getModelNodeSchemaPath());
                        for (QName helperName : ((ListSchemaNode) dataSchemaNode).getKeyDefinition()) {
                            try {
                                m_modelNodeId.addRdn(helperName.getLocalName(), helperName.getNamespace().toString(),
                                        naturalKeyHelpers.get(helperName).getValue(this).getStringValue());
                            } catch ( Exception e) {
                                throw new RuntimeException("Could not get key attributes ", e);
                            }
                        }
                    }
                }
            }
        }
        return m_modelNodeId;
    }

    /**
     * Mostly used by tests
     * @param modelNodeId
     */
    public void setModelNodeId(ModelNodeId modelNodeId) {
        m_modelNodeId = modelNodeId;
    }

    @Override
    public void copyConfig(Element config) throws CopyConfigException{
        String namespace = config.getNamespaceURI();
        if(namespace != null && !getSchemaRegistry().isKnownNamespace(namespace)) {
            LOGGER.error("An unexpected namespace " + namespace + " is present in " + getLocalName(config));
            NetconfRpcError rpcError = NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.UNKNOWN_NAMESPACE,
                    "An unexpected namespace is present");
            rpcError.addErrorInfoElement(NetconfRpcErrorInfo.BadElement, getLocalName(config));
            rpcError.addErrorInfoElement(NetconfRpcErrorInfo.BadNamespace, namespace);
            throw new CopyConfigException(rpcError);
        }
        copyConfigAttributes(config);
        copyConfigLeafLists(config);
        copyChildContainers(config);
        copyChildLists(config);
    }

    private void copyChildLists(Element config) throws CopyConfigException {
        Map<QName, ChildListHelper> childListHelpers = getModelNodeHelperRegistry().getChildListHelpers(this.getModelNodeSchemaPath());
        for(Entry<QName, ChildListHelper> childListHelperEntry : childListHelpers.entrySet()){
            NodeList childNodes = config.getChildNodes();
            boolean found = false;
            ChildListHelper helper = childListHelperEntry.getValue();

            Set<QName> alreadyProcessed = new HashSet<QName>();

            for(int i=0; i<childNodes.getLength();i++){
                Node iItem = childNodes.item(i);
                if (iItem.getNodeType() == Node.ELEMENT_NODE) {
                    QName iITemQName = getQName(iItem);
                    if(!alreadyProcessed.contains(iITemQName)){
                        QName helperName = childListHelperEntry.getKey();
                        if(helperName.equals(iITemQName)){
                            found = true;
                            try {
                                //remove all children
                                Collection<ModelNode> children = helper.getValue(this, Collections.<QName, ConfigLeafAttribute>emptyMap());
                                if(children != null && !children.isEmpty()){
                                    helper.removeAllChild(this);
                                }

                                //recreate from copy request
                                // get modelNodeUris and form key and config attributes
                                Map<QName, ConfigLeafAttribute> keyAttrs = new HashMap<>();
                                Map<QName, ConfigLeafAttribute> configAttrs = new HashMap<>();
                                SchemaPath uri = helper.getChildModelNodeSchemaPath();
                                QName containerQName = uri.getLastComponent();
                                if(iITemQName.equals(containerQName)){
                                    //Get key attributes
                                    Map<QName, ConfigAttributeHelper> keyAttrHelper = getModelNodeHelperRegistry().getNaturalKeyHelpers(uri);
                                    for(Entry<QName, ConfigAttributeHelper> configAttributeHelperEntry : keyAttrHelper.entrySet()){

                                        NodeList nodeList = iItem.getChildNodes();
                                        for(int j=0;j<nodeList.getLength();j++){
                                            Node node = nodeList.item(j);
                                            if((node instanceof Element) && configAttributeHelperEntry.getKey().getLocalName().equals(node.getLocalName())){
                                                keyAttrs.put(configAttributeHelperEntry.getKey(),ConfigAttributeFactory
                                                        .getConfigAttribute(getSchemaRegistry(),uri,configAttributeHelperEntry
                                                                .getKey(),node));
                                            }
                                        }
                                    }
                                    //Get config attributes
                                    Map<QName, ConfigAttributeHelper> configAttrHelper = getModelNodeHelperRegistry().getConfigAttributeHelpers(uri);
                                    for(Entry<QName, ConfigAttributeHelper> configAttributeHelperEntry : configAttrHelper.entrySet()){
                                        NodeList nodeList = iItem.getChildNodes();
                                        for(int j=0;j<nodeList.getLength();j++){
                                            Node node = nodeList.item(j);
                                            if((node instanceof Element) && configAttributeHelperEntry.getKey().getLocalName().equals(node.getLocalName())){
                                                configAttrs.put(configAttributeHelperEntry.getKey(),ConfigAttributeFactory
                                                        .getConfigAttribute(getSchemaRegistry(),uri,configAttributeHelperEntry.getKey(),node));
                                            }
                                        }
                                    }
                                }

                                ModelNode child = helper.addChild(this, true, keyAttrs, configAttrs);
                                child.copyConfig((Element) iItem);

                                alreadyProcessed.add(helperName);
                                //process all other nodes of the same type
                                for(int j=i+1; j<childNodes.getLength(); j++){
                                    Node jItem = childNodes.item(j);
                                    if(jItem.getNodeType() == Node.ELEMENT_NODE){
                                        QName jItemQName = getQName(jItem);
                                        if(helperName.equals(jItemQName)){
                                            Map<QName, ConfigLeafAttribute> keyAttributes = new HashMap<QName,ConfigLeafAttribute>();
                                            Map<QName, ConfigLeafAttribute> configAttributes = new HashMap<QName,ConfigLeafAttribute>();
                                            //Get key attributes
                                            Map<QName, ConfigAttributeHelper> keyAttrHelper = getModelNodeHelperRegistry().getNaturalKeyHelpers(uri);
                                            for(Entry<QName, ConfigAttributeHelper> configAttributeHelperEntry : keyAttrHelper.entrySet()){

                                                NodeList jNodeList = jItem.getChildNodes();
                                                for(int k=0;k<jNodeList.getLength();k++){
                                                    Node kNode = jNodeList.item(k);
                                                    if((kNode instanceof Element) && configAttributeHelperEntry.getKey().getLocalName().equals(kNode.getLocalName())){
                                                        keyAttributes.put(configAttributeHelperEntry.getKey(),ConfigAttributeFactory
                                                                .getConfigAttribute(getSchemaRegistry(),uri,configAttributeHelperEntry
                                                                        .getKey(),kNode));
                                                    }
                                                }
                                            }
                                            //Get config attributes
                                            Map<QName, ConfigAttributeHelper> configAttrHelper = getModelNodeHelperRegistry().getConfigAttributeHelpers(uri);
                                            for(Entry<QName, ConfigAttributeHelper> configAttributeHelperEntry : configAttrHelper.entrySet()){
                                                NodeList jNodeList = jItem.getChildNodes();
                                                for(int k=0;k<jNodeList.getLength();k++){
                                                    Node kNode = jNodeList.item(k);
                                                    if((kNode instanceof Element) && configAttributeHelperEntry.getKey().getLocalName().equals(kNode.getLocalName())){
                                                        configAttributes.put(configAttributeHelperEntry.getKey(),ConfigAttributeFactory
                                                                .getConfigAttribute(getSchemaRegistry(),uri,configAttributeHelperEntry
                                                                        .getKey(),jNodeList.item(k)));
                                                    }
                                                }
                                            }
                                            child = helper.addChild(this, true, keyAttributes, configAttributes);
                                            child.copyConfig((Element) jItem);
                                        }
                                    }//ignore otherwise

                                }
                            } catch (ModelNodeGetException | ModelNodeCreateException | ModelNodeDeleteException e) {
                                LOGGER.error("Failed to set child ", e);
                                CopyConfigException exception = new CopyConfigException(NetconfRpcErrorUtil.getNetconfRpcErrorForModelNodeId(getModelNodeId(),NetconfRpcErrorTag.OPERATION_FAILED,"Failed to set child ", getSchemaRegistry()));
                                exception.addSuppressed(e);
                                throw exception;
                            } catch (InvalidIdentityRefException e){
                                CopyConfigException exception = new CopyConfigException(e.getRpcError());
                                throw exception;
                            }
                            break;
                        }
                    }
                }//else ignore it
            }
            if(!found){
                try {
                    //reset if there is no configuration
                    helper.removeAllChild(this);
                } catch (ModelNodeDeleteException e) {
                    LOGGER.error("Failed to re-set child ", e);
                    CopyConfigException exception	=	new CopyConfigException(NetconfRpcErrorUtil.getNetconfRpcErrorForModelNodeId(getModelNodeId(),NetconfRpcErrorTag.OPERATION_FAILED,"Failed to re-set child ", getSchemaRegistry()));
                    exception.addSuppressed(e);
                    throw exception;
                }
            }
        }
    }

    private QName getQName(Node iItem) {
        QName qname = null;
        String namespace = iItem.getNamespaceURI();
        String localName = iItem.getLocalName();
        qname = getSchemaRegistry().lookupQName(namespace, localName);
        return qname;
    }

    private void copyChildContainers(Element config) throws CopyConfigException {
        Map<QName, ChildContainerHelper> annotationChildContainerHelpers = getModelNodeHelperRegistry().getChildContainerHelpers(this.getModelNodeSchemaPath());
        for(Entry<QName, ChildContainerHelper> childContainerHelperEntry : annotationChildContainerHelpers.entrySet()){
            NodeList childNodes = config.getChildNodes();
            boolean found = false;
            ChildContainerHelper helper = childContainerHelperEntry.getValue();
            for(int i=0; i<childNodes.getLength();i++){
                QName helperName = childContainerHelperEntry.getKey();
                Node iItem = childNodes.item(i);
                if (iItem.getNodeType() == Node.ELEMENT_NODE) {
                    QName iITemQName = getQName(iItem);
                    if(helperName.equals(iITemQName)){
                        found = true;
                        try {
                            ModelNode child = helper.getValue(this);
                            if(child == null){
                                child = helper.createChild(this, new HashMap<>());
                            }
                            child.copyConfig((Element) iItem);
                        } catch (ModelNodeCreateException | ModelNodeGetException e) {
                            LOGGER.error("Failed to set child ", e);
                            CopyConfigException exception	=	new CopyConfigException(NetconfRpcErrorUtil.getNetconfRpcErrorForModelNodeId(getModelNodeId(),NetconfRpcErrorTag.OPERATION_FAILED,"Failed to set child ", getSchemaRegistry()));
                            exception.addSuppressed(e);
                            throw exception;
                        }
                        break;
                    }
                }//else ignore it

            }
            if(!found){
                try {
                    //reset if there is no configuration
                    helper.deleteChild(this);
                } catch (ModelNodeDeleteException e) {
                    LOGGER.error("Failed to re-set child ", e);
                    CopyConfigException exception	=	new CopyConfigException(NetconfRpcErrorUtil.getNetconfRpcErrorForModelNodeId(getModelNodeId(),NetconfRpcErrorTag.OPERATION_FAILED,"Failed to re-set child ", getSchemaRegistry()));
                    exception.addSuppressed(e);
                    throw exception;
                }
            }

        }
    }

    private void copyConfigAttributes(Element config) throws CopyConfigException {
        Map<QName, ConfigAttributeHelper> configAttributeHelpers = getModelNodeHelperRegistry().getConfigAttributeHelpers(this.getModelNodeSchemaPath());
        for(Entry<QName, ConfigAttributeHelper> configAttributeHelperEntry : configAttributeHelpers.entrySet()){
            NodeList childNodes = config.getChildNodes();
            boolean found = false;
            ConfigAttributeHelper helper = configAttributeHelperEntry.getValue();
            for(int i=0; i<childNodes.getLength();i++){
                QName helperName = configAttributeHelperEntry.getKey();
                Node iItem = childNodes.item(i);

                if(iItem.getNodeType() == Node.ELEMENT_NODE){
                    QName iITemQName = getQName(iItem);

                    if(helperName.equals(iITemQName)){
                        found = true;
                        try {
                            helper.setValue(this,ConfigAttributeFactory.getConfigAttribute(getSchemaRegistry(),this.getModelNodeSchemaPath()
                                    ,configAttributeHelperEntry.getKey(),iItem));
                        } catch (SetAttributeException  e) {
                            LOGGER.error("Failed to set attribute ", e);
                            CopyConfigException exception	=	new CopyConfigException(NetconfRpcErrorUtil.getNetconfRpcErrorForModelNodeId(getModelNodeId(),NetconfRpcErrorTag.OPERATION_FAILED,"Failed to set attribute ", getSchemaRegistry()));
                            exception.addSuppressed(e);
                            throw exception;
                        } catch (InvalidIdentityRefException e){
                            CopyConfigException exception = new CopyConfigException(e.getRpcError());
                            throw exception;
                        }
                        break;
                    }
                }//else ignore it

            }
            if(!found){
                try {
                    if (!helper.isMandatory()) {
                        //reset if there is no configuration
                        helper.setValue(this,null);
                    } /*else {
						CopyConfigException exception = new CopyConfigException(NetconfRpcErrorUtil.getNetconfRpcErrorForModelNodeId(getModelNodeId(),NetconfRpcErrorTag.OPERATION_FAILED,"Copy-config is missing mandatory attribute "+helper.getLeafSchemaNode().getQName().getLocalName(), getSchemaRegistry()));
						throw exception;
					}*/
                } catch (SetAttributeException e) {
                    LOGGER.error("Failed to re-set attribute ", e);
                    CopyConfigException exception	=	new CopyConfigException(NetconfRpcErrorUtil.getNetconfRpcErrorForModelNodeId(getModelNodeId(),NetconfRpcErrorTag.OPERATION_FAILED,"Failed to re-set attribute ", getSchemaRegistry()));
                    exception.addSuppressed(e);
                    throw exception;
                }
            }

        }
    }

    private void copyConfigLeafLists(Element config) throws CopyConfigException {
        Map<QName, ChildLeafListHelper> childLeafListHelpers = getModelNodeHelperRegistry().getConfigLeafListHelpers(this.getModelNodeSchemaPath());
        for(Entry<QName, ChildLeafListHelper> childLeafListHelperEntry : childLeafListHelpers.entrySet()){
            NodeList childNodes = config.getChildNodes();
            boolean found = false;
            ChildLeafListHelper helper = childLeafListHelperEntry.getValue();
            if (helper.isConfiguration()) {
                for (int i = 0; i < childNodes.getLength(); i++) {
                    QName helperName = childLeafListHelperEntry.getKey();
                    Node iItem = childNodes.item(i);
                    if(iItem.getNodeType() == Node.ELEMENT_NODE){
                        QName iITemQName = getQName(iItem);

                        if (helperName.equals(iITemQName)) {
                            found = true;
                            try {
                                helper.addChild(this,ConfigAttributeFactory.getConfigAttribute(getSchemaRegistry(),this
                                        .getModelNodeSchemaPath(),childLeafListHelperEntry.getKey(),iItem));
                            } catch (DOMException | SetAttributeException e) {
                                LOGGER.error("Failed to add LeafList attribute ", e);
                                CopyConfigException exception	=	new CopyConfigException(NetconfRpcErrorUtil.getNetconfRpcErrorForModelNodeId(getModelNodeId(),NetconfRpcErrorTag.OPERATION_FAILED,"Failed to add LeafList attribute ", getSchemaRegistry()));
                                exception.addSuppressed(e);
                                throw exception;
                            }catch (InvalidIdentityRefException e){
                                CopyConfigException exception = new CopyConfigException(e.getRpcError());
                                throw exception;
                            }
                        }
                    }//ignore otherwise
                }
                if (!found) {
                    try {
                        if (helper.isMandatory()) {
                            //reset if there is no configuration
                            helper.addChild(this, null);
                        }
                    } catch (SetAttributeException e) {
                        LOGGER.error("Failed to re-set Leaf List attribute ", e);
                        CopyConfigException exception	=	new CopyConfigException(NetconfRpcErrorUtil.getNetconfRpcErrorForModelNodeId(getModelNodeId(),NetconfRpcErrorTag.OPERATION_FAILED,"Failed to re-set Leaf List attribute ", getSchemaRegistry()));
                        exception.addSuppressed(e);
                        throw exception;
                    }
                }
            }
        }
    }

    public SubSystemRegistry getSubSystemRegistry() {
        return getMountSubSystemRegistry();
    }

    @Override
    public Map<QName, String> getListKeys() throws GetAttributeException {
        Map<QName,String> keyValues = new HashMap<>();
        for(Entry<QName, ConfigAttributeHelper> keyEntry : getModelNodeHelperRegistry().getNaturalKeyHelpers(getModelNodeSchemaPath()).entrySet()){
            ConfigAttributeHelper helper = keyEntry.getValue();
            QName keyQname = keyEntry.getKey();
            keyValues.put(keyQname, helper.getValue(this).getStringValue());
        }
        return keyValues;
    }

    @Override
    public void setValue(Object value) {
    }

    @Override
    public Object getValue()  {
        return null;
    }

    @Override
    public String toString() {
        return "HelperDrivenModelNode [m_modelNodeId=" + m_modelNodeId + "]";
    }

    @Override
    public boolean isRoot() {
        if (isSchemaMountImmediateChild()) {
            return false;
        }
        return isRootSchemaPath(getModelNodeSchemaPath());
    }

    @Override
    public boolean hasSchemaMount() {
        readMountPointValue();
        return m_schemaMountNode;
    }

    public SchemaRegistry getGlobalSchemaRegistry() {
        return m_schemaRegistry;
    }

    public void setSchemaMountChild(boolean value) {
        m_schemaMountChild = value;
    }

    public boolean isSchemaMountImmediateChild() {
        return m_schemaMountChild;
    }

    @Override
    public SchemaPath getParentMountPath() {
        return m_parentMountPath;
    }

    @Override
    public boolean isChildBigList(DataSchemaNode childSchemaNode) {
        ChildListHelper childListHelper = m_modelNodeHelperRegistry.getChildListHelper(getModelNodeSchemaPath(),
                childSchemaNode.getQName());
        if(childListHelper != null) {
            return childListHelper.isChildBigList();
        }

        return getSchemaRegistry().isChildBigList(childSchemaNode.getPath());
    }

    public SchemaRegistry getSchemaRegistryForParent() {
        if (hasSchemaMount()) {
            return getGlobalSchemaRegistry();
        }
        return getSchemaRegistry();
    }
}