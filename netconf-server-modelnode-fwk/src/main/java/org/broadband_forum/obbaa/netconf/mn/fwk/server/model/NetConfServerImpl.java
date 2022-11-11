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

import static java.lang.Math.max;
import static org.broadband_forum.obbaa.netconf.api.server.NetconfQueryParams.UNBOUNDED;
import static org.broadband_forum.obbaa.netconf.api.util.DocumentUtils.createDocument;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.PrunedSubtreeFilterUtil.filter;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.PrunedSubtreeFilterUtil.getPseudoDepth;
import static org.broadband_forum.obbaa.netconf.server.RequestTask.CURRENT_REQ_TYPE;
import static org.broadband_forum.obbaa.netconf.server.RequestTask.REQ_TYPE_EDIT_CONFIG;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.AbstractNetconfRequest;
import org.broadband_forum.obbaa.netconf.api.messages.ActionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.ActionResponse;
import org.broadband_forum.obbaa.netconf.api.messages.CloseSessionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.CopyConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.DeleteConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetRequest;
import org.broadband_forum.obbaa.netconf.api.messages.KillSessionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.LockRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfFilter;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorInfo;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorSeverity;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorType;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcResponse;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.messages.PojoToDocumentTransformer;
import org.broadband_forum.obbaa.netconf.api.messages.RpcName;
import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.api.messages.UnLockRequest;
import org.broadband_forum.obbaa.netconf.api.server.NetconfQueryParams;
import org.broadband_forum.obbaa.netconf.api.server.ResponseChannel;
import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationService;
import org.broadband_forum.obbaa.netconf.api.util.DataPath;
import org.broadband_forum.obbaa.netconf.api.util.DataPathLevel;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.RpcRequestConstraintParser;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.DataPathUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.PersistenceException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.TimingLogger;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.DSMTimingLogger;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.NetconfRpcErrorUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.SubtreeFilterUtil;
import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.broadband_forum.obbaa.netconf.server.rpc.CreateSubscriptionRpcHandler;
import org.broadband_forum.obbaa.netconf.server.rpc.MultiRpcRequestHandler;
import org.broadband_forum.obbaa.netconf.server.rpc.RequestType;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcPayloadConstraintParser;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcProcessException;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcRequestHandler;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcValidationException;
import org.broadband_forum.obbaa.netconf.server.ssh.auth.AccessDeniedException;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class NetConfServerImpl implements NetconfServer {


    public static final String GET_FINAL_FILTER = "GET_FINAL_FILTER";
    private final SchemaRegistry m_schemaRegistry;
    private final RpcPayloadConstraintParser m_rpcConstraintParser;

    private static final TransformerFactory c_transformerFactory = TransformerFactory.newInstance();

    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(NetConfServerImpl.class, LogAppNames.NETCONF_STACK);

    private Map<Integer, NetConfSession> m_sessions = new HashMap<Integer, NetConfSession>();
    private Map<String, DataStore> m_dataStores = new HashMap<String, DataStore>();
    private RpcRequestHandlerRegistry m_rpcRequestHandlerRegistry;
    private SubtreeFilterUtil m_filterUtil = null;

    public NetConfServerImpl(SchemaRegistry schemaRegistry){
        this(schemaRegistry, new RpcRequestConstraintParser(schemaRegistry, null, null, null));
    }

    public NetConfServerImpl(SchemaRegistry schemaRegistry, RpcPayloadConstraintParser rpcRequestConstraintParser) {
        this(schemaRegistry, rpcRequestConstraintParser, new SubtreeFilterUtil(schemaRegistry));
    }

    public NetConfServerImpl(SchemaRegistry schemaRegistry, RpcPayloadConstraintParser rpcRequestConstraintParser,
            SubtreeFilterUtil filterUtil) {
        m_schemaRegistry = schemaRegistry;
        m_rpcConstraintParser = rpcRequestConstraintParser;
        m_filterUtil = filterUtil;
    }

    public void setRunningDataStore(DataStore dataStore) {
        m_dataStores.put(StandardDataStores.RUNNING, dataStore);
    }

    public void setDataStore(String storeName, DataStore store){
        m_dataStores.put(storeName, store);
    }

    public void setRpcRequestHandlerRegistry(RpcRequestHandlerRegistry registry) {
        m_rpcRequestHandlerRegistry = registry;
    }

    @Override
    public Map<Integer, NetConfSession> getNetconfSessions() {
        HashMap<Integer, NetConfSession> sessionsMap = new HashMap<Integer, NetConfSession>(m_sessions);
        return sessionsMap;
    }

    @Override
    public void killSession(Integer currentSession, Integer sessionToKill) {
        NetConfSession session = m_sessions.get(sessionToKill);
        if(session!=null){
            unlockStoresOwnedBySession(sessionToKill, session);
            m_sessions.remove(sessionToKill);
        }
    }

    @Override
    public void closeSession(Integer currentSession) {
        NetConfSession session = m_sessions.get(currentSession);
        if(session!=null){
            unlockStoresOwnedBySession(currentSession, session);
            m_sessions.remove(currentSession);
        }
    }

    private void unlockStoresOwnedBySession(Integer currentSession, NetConfSession session) {
        for(String storeName : session.getLockedStores()){
            try {
                m_dataStores.get(storeName).unlock(currentSession);
            } catch (UnlockFailedNoLockActiveException | UnlockFailedOtherOwnerException e) {
                LOGGER.error("Unlock attempt on a store failed on session close", e);
            }
        }
    }

    private void closeSubscription(NetconfClientInfo session){
        DataStore store = m_dataStores.get(StandardDataStores.RUNNING);
        NotificationService notificationService = store.getNotificationService();
        if(notificationService.isActiveSubscription(session)){
            notificationService.closeSubscription(session);
        }
    }

    @Override
    public void onKillSession(NetconfClientInfo client, KillSessionRequest request, NetConfResponse response) {
        Integer currentSession = client.getSessionId();
        Integer sessionToKill = new Integer(request.getSessionId());

        if(currentSession.equals(sessionToKill) || !m_sessions.containsKey(sessionToKill)){
            response.setOk(false);
            NetconfRpcError rpcError = NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.INVALID_VALUE,
                    "Invalid session ID");
            response.addError(rpcError);
            return;
        }else{
            closeSubscription(client);
            LOGGER.info("Session ID to be killed is {}" , sessionToKill);
            killSession(client.getSessionId(), sessionToKill);
            response.setOk(true);
        }
    }

    @Override
    public void onCloseSession(NetconfClientInfo client, CloseSessionRequest arg1, NetConfResponse response) {
        closeSubscription(client);
        closeSession(client.getSessionId());
        response.setOk(true);
    }

    @Override
    public void onCopyConfig(NetconfClientInfo client, CopyConfigRequest request, NetConfResponse response) {
        if (!validateMessageId(request, response)) {
            return;
        }
        DataStore source = m_dataStores.get(request.getSource());
        Element sourceConfigElement = null;
        if (source == null) {
            if(request.getSourceConfigElement()== null){
                response.setOk(false);
                NetconfRpcError rpcError = NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.INVALID_VALUE,
                        "invalid source");
                response.addError(rpcError);
                return;
            }else{
                sourceConfigElement = request.getSourceConfigElement();
            }

        }
        if(m_rpcConstraintParser!=null){
            m_rpcConstraintParser.validate(request, RequestType.COPY_CONFIG);
        }
        DataStore target = m_dataStores.get(request.getTarget());
        if (target == null) {
            response.setOk(false);
            NetconfRpcError rpcError = NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.INVALID_VALUE,
                    "invalid target");
            response.addError(rpcError);
            return;
        }

        try {

            if(source !=null)
                target.copyFrom(client, source);
            else
                target.copyFrom(client, sourceConfigElement);
            response.setOk(true);
        } catch (LockedByOtherSessionException e) {
            LOGGER.error("Copy config failed, locked by other session" , e);
            response.setOk(false);
            NetconfRpcError rpcError = NetconfRpcErrorUtil.getNetconfRpcError(NetconfRpcErrorTag.IN_USE,
                    NetconfRpcErrorType.Application,NetconfRpcErrorSeverity.Error,
                    "The request requires a resource that is already in use");
            response.addError(rpcError);
        } catch (CopyConfigException e) {
            LOGGER.error("Copy config failed" , e);
            response.setOk(false).addError(e.getRpcError());
        }
    }

    @Override
    public void onDeleteConfig(NetconfClientInfo arg0, DeleteConfigRequest arg1, NetConfResponse response) {
        // next, delete the data store, now we should not entirely delete it

    }

    protected boolean validateOptions(EditConfigRequest request, NetConfResponse response) {
        if (!PojoToDocumentTransformer.validateDefaultEditOpertation(request.getDefaultOperation())) {
            response.addError(NetconfRpcErrorUtil.getApplicationError(
                    NetconfRpcErrorTag.INVALID_VALUE,
                    "invalid default-operation: " + request.getDefaultOperation()));
            return false;
        }

        if (!PojoToDocumentTransformer.validateTestOption(request.getTestOption())) {
            response.addError(NetconfRpcErrorUtil.getApplicationError(
                    NetconfRpcErrorTag.INVALID_VALUE,
                    "invalid test-option: " + request.getTestOption()));
            return false;
        }

        if (!PojoToDocumentTransformer.validateErrorOption(request.getErrorOption())) {
            response.addError(NetconfRpcErrorUtil.getApplicationError(
                    NetconfRpcErrorTag.INVALID_VALUE,
                    "invalid error-option: " + request.getErrorOption()));
            return false;
        }
        return true;
    }
    @Override
    public List<Notification> onEditConfig(NetconfClientInfo clientInfo, EditConfigRequest request, NetConfResponse response) {
        RequestScope.getCurrentScope().putInCache(CURRENT_REQ_TYPE, REQ_TYPE_EDIT_CONFIG);
        List<Notification> netconfConfigChangeNotifications = null;

        if (request.getConfigElement() == null){
            // this should not be happening at this level, anyway
            response.addError(NetconfRpcErrorUtil.getNetconfRpcError(NetconfRpcErrorTag.INVALID_VALUE,
                    NetconfRpcErrorType.Protocol,
                    NetconfRpcErrorSeverity.Error,
                    "<config> element content is empty"));
            return null;

        }

        List<Element> configElementContent = request.getConfigElement().getConfigElementContents();
        if (configElementContent == null || configElementContent.isEmpty()) {
            // this should not be happening at this level, anyway
            response.addError(NetconfRpcErrorUtil.getNetconfRpcError(NetconfRpcErrorTag.INVALID_VALUE,
                    NetconfRpcErrorType.Protocol,
                    NetconfRpcErrorSeverity.Error,
                    "<config> element content is empty"));
            return null;
        }


        try {
            if (!validateMessageId(request, response)) {
                return null;
            }

            if (!validateOptions(request, response)) {
                return null;
            }

            if (request.getDefaultOperation() != null && !request.getDefaultOperation().equals("")) {
                RequestScope.getCurrentScope().putInCache("DEFAULT_OPERATION",request.getDefaultOperation());
            }

            m_rpcConstraintParser.validate(request, RequestType.EDIT_CONFIG);

            DataStore store = getDataStore(request.getTarget());

            if (store == null) {
                response.setOk(false);
                NetconfRpcError rpcError = NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.INVALID_VALUE,
                        "invalid target");
                response.addError(rpcError);
                return null;
            }
            OutputStream byteOS = new ByteArrayOutputStream();
            try {
                c_transformerFactory.newTransformer().transform(new DOMSource(request.getConfigElement().getXmlElement()), new StreamResult(byteOS));
            } catch (TransformerException
                    | TransformerFactoryConfigurationError
                    | NetconfMessageBuilderException e) {
                // Ignored
            }
            request.setClientInfo(clientInfo);
            netconfConfigChangeNotifications = store.edit(request, response, clientInfo);
            // method returned without exception, so success
            response.setOk(true);
            response.setInstanceReplaced(FlagForRestPutOperations.m_instanceReplace.get());
            return netconfConfigChangeNotifications;
        } catch (RpcValidationException e) {
            LOGGER.error("Validation failed", e);
            response.addError(e.getRpcError());
        } catch (EditConfigException e) {
            LOGGER.error("Edit config failed" , e);
            List<NetconfRpcError> errors = e.getRpcErrors();
            for (NetconfRpcError error:errors) {
                response.addError(error);// TODO: FNMS-9159 add more error options here
            }
        } catch (PersistenceException e) {
            LOGGER.error("Edit config change persistence failed" , e);
            response.addError(e.getRpcError());// TODO:FNMS-9159  add more error options here
        } catch (EditConfigTestFailedException e) {
            LOGGER.error("Edit config test failed" , e);
            response.addError(e.getRpcError());// TODO: FNMS-9159 add more error options here
        } catch (LockedByOtherSessionException e) {
            LOGGER.error("Edit config failed, locked by other session " , e);
            response.setOk(false);
            NetconfRpcError rpcError = NetconfRpcErrorUtil.getNetconfRpcError(NetconfRpcErrorTag.LOCK_DENIED,NetconfRpcErrorType.RPC,NetconfRpcErrorSeverity.Error,
                    "Edit config failed, locked by other session ")
                    .addErrorInfoElement(NetconfRpcErrorInfo.SessionId, Integer.toString(e.getLockOwner()));
            response.addError(rpcError);
        } catch (GetException e) {
            LOGGER.error("Edit config failed with GetException" , e);
            response.addError(e.getRpcError());// TODO:FNMS-9159  add more error options here
        } catch (Exception e){
            //any unexpected error - sever cause
            LOGGER.error("Edit config failed, unexpected behavior:",e);
            Element errorInfo = null;
            try {
                Document document = DocumentUtils.getNewDocument();
                errorInfo = document.createElementNS(NetconfResources.NETCONF_RPC_NS_1_0, NetconfResources.RPC_ERROR_INFO);
                Element stackTraceElement = document.createElementNS(NetconfResources.NC_STACK_NS, NetconfResources.RPC_STACK_TRACE);
                stackTraceElement.setTextContent(ExceptionUtils.getStackTrace(e));
                errorInfo.appendChild(stackTraceElement);
            } catch (Exception ex) {
                LOGGER.error("Error while creating error-info element", ex);
            }
            NetconfRpcError rpcError = NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.OPERATION_FAILED, "Operation Failed with Exception: " + e.getLocalizedMessage());
            if(errorInfo != null){
                rpcError.setErrorInfo(errorInfo);
            }
            response.addError(rpcError);

        } finally {
            // resetting the replace flag once request is complete
            FlagForRestPutOperations.resetInstanceReplaceFlag();
        }
        return null;
    }

    public DataStore getDataStore(String storeName) {
        return m_dataStores.get(storeName);
    }

    @Override
    public void onGet(NetconfClientInfo client, GetRequest request, NetConfResponse response) {
        if (!validateMessageId(request, response)) {
            return;
        }
        FilterNode filterNode = getFilter(request.getFilter());
        DataStore store = m_dataStores.get(StandardDataStores.RUNNING);
        Document doc = createDocument();
        if (request.getDepth() == 1) {
            List<Element> elements = new ArrayList();
            response.setDataContent(elements);
        } else {

            try {
                List<Element> elements;
                if(filterNode != null && filterNode.isTypePrunedSubtree() && request.getDepth() != UNBOUNDED) {
                    int pseudoDepth = getPseudoDepth(filterNode);
                    LOGGER.debug("Pseudo depth value calculated during pruned-subtree filtering is " + pseudoDepth);
                    elements = store.get(client, doc, filterNode, new NetconfQueryParams(max(pseudoDepth, request.getDepth()),
                            request.isIncludeConfig(), request.getFieldValues()));
                } else {
                    elements = store.get(client, doc, filterNode, new NetconfQueryParams(request.getDepth(),
                            request.isIncludeConfig(), request.getFieldValues()));
                }

                if (elements != null && !elements.isEmpty()) {
                    response.setDataContent(elements);
                } else {
                    response.setDataContent(Collections.<Element> emptyList());
                }
                if(filterNode != null && filterNode.isTypePrunedSubtree()) {
                    response.setData(filter(m_schemaRegistry, response.getData(), request.getFilter().getXmlFilter(), request.getDepth()));
                }
                if (Boolean.valueOf(getFromEnvOrSysProperty(GET_FINAL_FILTER, "false"))) {
                    NetconfFilter filter = request.getFilter();
                    if (filter != null) {
                        response.setData(m_filterUtil.filter(response.getData(), filter.getXmlFilter()));
                    }
                }

            } catch (GetException e) {
                LOGGER.error("Error while evaluating filter ", e);
                response.addError(e.getRpcError());
            } catch (Exception e) {
                LOGGER.error("Error while executing get", e);
                response.addError(NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.OPERATION_FAILED,
                        "Error while executing get - " + e.getMessage()));
            }
        }
    }

    String getFromEnvOrSysProperty(String name, String defaultValue) {
        String value = System.getenv(name);
        if (value == null) {
            value = System.getProperty(name, null);
        }
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    @Override
    public void onGetConfig(NetconfClientInfo client, GetConfigRequest request, NetConfResponse response) {
        if (!validateMessageId(request, response)) {
            return;
        }
        if ( request.getDepth()==1){
            response.setDataContent(Collections.<Element>emptyList());
            return;
        }
        FilterNode filterNode = getFilter(request.getFilter());
        DataStore store = m_dataStores.get(request.getSource());
        Document doc = createDocument();
        List<Element> elements;
        try {
            if(filterNode != null && filterNode.isTypePrunedSubtree() && request.getDepth() != UNBOUNDED) {
                int pseudoDepth = getPseudoDepth(filterNode);
                LOGGER.debug("Pseudo depth value calculated during pruned-subtree filtering is " + pseudoDepth);
                elements = store.getConfig(client, doc, filterNode, new NetconfQueryParams(max(pseudoDepth, request.getDepth()), true, request.getFieldValues()));
            } else {
                elements = store.getConfig(client, doc, filterNode, new NetconfQueryParams(request.getDepth(), true, request.getFieldValues()));
            }

            if(elements != null && !elements.isEmpty()) {
                response.setDataContent(elements);
            }else{
                response.setDataContent(Collections.emptyList());
            }
            if(filterNode != null && filterNode.isTypePrunedSubtree()) {
                response.setData(filter(m_schemaRegistry, response.getData(), request.getFilter().getXmlFilter(), request.getDepth()));
            }
        } catch (GetException e) {
            LOGGER.error("Get config failed " , e);
            response.addError(e.getRpcError());
        }catch (Exception e){
            LOGGER.error("Error while executing get-config",e);
            response.addError(NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.OPERATION_FAILED,
                    "Error while executing get-config. - " + e.getMessage()));
        }

    }

    public FilterNode getFilter(NetconfFilter filter) {
        FilterNode root = new FilterNode();
        if (filter != null && filter.getXmlFilterElements() != null) {
            root.setFilterType(filter.getType());
            List<Element> xmlFilterElements = filter.getXmlFilterElements();
            FilterUtil.buildMergedFilter(root, xmlFilterElements, m_schemaRegistry);
            LOGGER.trace("filter n {}" , root);
        } else {
            LOGGER.trace("no filter");
            root = null;
        }
        return root;
    }

    @Override
    public List<Notification> onRpc(NetconfClientInfo client, NetconfRpcRequest request, NetconfRpcResponse response) {
        return getDataStore(StandardDataStores.RUNNING).withValidationContext(new ValidationContextTemplate<List<Notification>>() {
            @SuppressWarnings("unchecked")
            @Override
            public List<Notification> validate() {
                List<Notification> netconfConfigChangeNotifications = null;
                Document reqDoc = null;
                try {
                    reqDoc = request.getRequestDocument();
                } catch (NetconfMessageBuilderException e) {
                    LOGGER.error("Invalid request: {}", reqDoc, e);
                    onInvalidRequest(client, reqDoc, response);
                    return null;
                }

                try {
                    TimingLogger.start();
                    DSMTimingLogger.start();

                    if (!validateMessageId(request, response)) {
                        return null;
                    }

                    RpcRequestHandler rpcHandler = m_rpcRequestHandlerRegistry.lookupRpcRequestHandler(request.getRpcName());

                    if (rpcHandler != null) {
                        rpcHandler.validate(m_rpcConstraintParser, request);
                        netconfConfigChangeNotifications = rpcHandler.processRequest(client, request, response);
                        response.setRpcName(request.getRpcName());
                        rpcHandler.validate(m_rpcConstraintParser, response);
                    } else {
                        MultiRpcRequestHandler multiRpcRequestHandler = m_rpcRequestHandlerRegistry.getMultiRpcRequestHandler(request);
                        if (multiRpcRequestHandler != null) {
                            multiRpcRequestHandler.validate(m_rpcConstraintParser, request);
                            multiRpcRequestHandler.checkRequiredPermissions(client, response.getRpcName().getName());
                            netconfConfigChangeNotifications = multiRpcRequestHandler.processRequest(client, request, response);
                            if ( response.getRpcName() == null){
                                response.setRpcName(request.getRpcName());
                            }
                            multiRpcRequestHandler.validate(m_rpcConstraintParser, response);
                        }
                        else {
                            LOGGER.error("No RPC handler found. Invalid request:{}", reqDoc);
                            onInvalidRequest(client, reqDoc, response);
                        }
                    }

                } catch (RpcValidationException e) {
                    LOGGER.error("RPC request failed to validate:{} ", request, e);
                    response.addError(e.getRpcError());
                    netconfConfigChangeNotifications = null;
                } catch(RpcProcessException e) {
                    LOGGER.error("Error occurred while processing RPC", e);
                    response.addErrors(e.getRpcErrors());
                } catch (AccessDeniedException e) {
                    LOGGER.error("Error occurred while processing RPC", e);
                    response.addError(e.getRpcError());
                } finally {
                    TimingLogger.finish();
                    DSMTimingLogger.finish();

                }
                return netconfConfigChangeNotifications;
            }
        });
    }

    @Override
    public void onAction(NetconfClientInfo info, ActionRequest request, ActionResponse response){
        if (!validateMessageId(request, response)) {
            return;
        }
        DataStore store = getDataStore(StandardDataStores.RUNNING);
        store.withValidationContext(new ValidationContextTemplate<Object>() {
            @SuppressWarnings("unchecked")
            @Override
            public Object validate() {
                Document reqDoc = null;
                List<Element> elements = null;
                try {
                    TimingLogger.start();
                    DSMTimingLogger.start();
                    Element actionTreeElement = request.getActionTreeElement();
                    SchemaRegistry mountRegistry = getMountRegistryIfApplicable(actionTreeElement);
                    Map<ActionDefinition, Element> matchedActionElements = retrieveMatchedActionElements(actionTreeElement, mountRegistry);

                    if(matchedActionElements.isEmpty()){
                        Map<RpcDefinition, Element> matchedDeviceRpc = retrieveMatchedRpcElement(actionTreeElement);
                        if ( matchedDeviceRpc.isEmpty()){
                            LOGGER.error("No matched action found on the model. Invalid request:{}", reqDoc);
                            response.addError(NetconfRpcErrorUtil.getNetconfRpcError(NetconfRpcErrorTag.BAD_ELEMENT,
                                    NetconfRpcErrorType.Protocol,
                                    NetconfRpcErrorSeverity.Error,
                                    "No matched action found on the models"));
                            return null;
                        } else if (matchedDeviceRpc.size() > 1) {
                            response.addError(NetconfRpcErrorUtil.getNetconfRpcError(NetconfRpcErrorTag.BAD_ELEMENT,
                                    NetconfRpcErrorType.Protocol,
                                    NetconfRpcErrorSeverity.Error,
                                    "Multiple action element exists within RPC"));
                            return null;
                        } else {
                            Map.Entry<RpcDefinition, Element> entry = matchedDeviceRpc.entrySet().iterator().next();
                            handleDeviceRpc(info, request, response, entry.getKey(), actionTreeElement);
                        }
                    } else if (matchedActionElements.size()>1){
                        response.addError(NetconfRpcErrorUtil.getNetconfRpcError(NetconfRpcErrorTag.BAD_ELEMENT,
                                NetconfRpcErrorType.Protocol,
                                NetconfRpcErrorSeverity.Error,
                                "Multiple action element exists within RPC" ));
                        return null;
                    } else {
                        try {
                            Map.Entry<ActionDefinition,Element> entry=matchedActionElements.entrySet().iterator().next();
                            ActionDefinition actionDef = entry.getKey();
                            request.setActionTargetpath(actionDef.getPath().getParent());
                            request.setActionDefinition(actionDef);
                            request.setActionQName(actionDef.getQName());
                            reqDoc = request.getRequestDocument();
                            m_rpcConstraintParser.validate(request, RequestType.ACTION);
                            elements = store.action(reqDoc, request, info, mountRegistry);
                            response.setActionDefinition(actionDef);
                            response.setActionContext(actionTreeElement);

                            if (elements != null && !elements.isEmpty()) {
                                ContainerSchemaNode output = actionDef.getOutput();
                                boolean isError = isError(elements);
                                if (isError) {
                                    for (Element element : elements) {
                                        if (element.getLocalName() != null && element.getNamespaceURI() != null) {
                                            if (element.getLocalName().equals(NetconfResources.RPC_ERROR)) {
                                                NetconfRpcErrorUtil.convertElementToNetconfRpcError(element, response);
                                            }
                                        }
                                    }
                                } else {
                                    response.setActionOutputElements(elements);
                                    /**
                                     * If output is not defined in action, then
                                     * action-response should be
                                     * "<rpc-reply> <ok/> </rpc-reply>" Hence
                                     * dont need to validtion the output such
                                     * case
                                     */
                                    if (null != output && output.getChildNodes() != null && !output.getChildNodes().isEmpty() && !isError) {
                                        m_rpcConstraintParser.validate(response, RequestType.ACTION);
                                    }
                                }
                            }else{
                                response.setOk(true);
                            }
                        } catch (ValidationException e) {
                            LOGGER.error("Action request failed to validate:{} ", request, e);
                            response.addError(e.getRpcError());
                            return null;
                        } catch (ActionException e) {
                            LOGGER.error("error while executing action request: {}", reqDoc, e);
                            List<NetconfRpcError> errors = e.getRpcErrors();
                            for (NetconfRpcError error:errors) {
                                response.addError(error);
                            }
                            return null;
                        }
                    }
                } catch (ValidationException e) {
                    LOGGER.error("Action request failed to validate:{} ", request, e);
                    response.addError(e.getRpcError());
                    return null;
                } catch (NetconfMessageBuilderException e) {
                    LOGGER.error("Invalid request: {}", reqDoc, e);
                    onInvalidRequest(info, reqDoc, response);
                    return null;
                } finally {
                    TimingLogger.finish();
                    DSMTimingLogger.finish();
                }
                return null;
            }
        });

        return;
    }



    private boolean isError(List<Element> elements) {
        for ( Element element : elements){
            if(element.getLocalName() != null && element.getNamespaceURI() != null){
                if (element.getLocalName().equals(NetconfResources.RPC_ERROR) && element.getNamespaceURI().equals(NetconfResources.NETCONF_RPC_NS_1_0)){
                    return true;
                }
            }
        }
        return false;
    }

    private void handleDeviceRpc(NetconfClientInfo info, ActionRequest actionRequest, ActionResponse actionResponse,
            RpcDefinition rpcDef, Element treeElement) {
        NetconfRpcRequest rpcRequest = new NetconfRpcRequest();
        rpcRequest.setMessageId(actionRequest.getMessageId());
        rpcRequest.setIsSchemaMountedRpc(true);
        rpcRequest.setRpcInput(treeElement);
        NetconfRpcResponse rpcResponse = new NetconfRpcResponse();
        rpcRequest.setRpcContext(treeElement);
        rpcResponse.setRpcContext(treeElement);
        QName rpcName = rpcDef.getQName();
        rpcResponse.setRpcName(new RpcName(rpcName.getNamespace().toString(), rpcName.getLocalName()));
        onRpc(info, rpcRequest, rpcResponse);
        if ( !rpcResponse.getErrors().isEmpty()){
            actionResponse.setOk(false);
            actionResponse.addErrors(rpcResponse.getErrors());
        } else {
            actionResponse.setOk(true);
            if ( ! rpcResponse.getRpcOutputElements().isEmpty()){
                actionResponse.setOk(false);
            }
            List<Element> outputElements = ((NetconfRpcResponse) rpcResponse).getRpcOutputElements();
            actionResponse.setActionOutputElements(outputElements);
            actionResponse.setDataContent(outputElements);
        }
    }

    private Map<ActionDefinition, Element> retrieveMatchedActionElements(Element actionRequest, SchemaRegistry mountRegistry) {

        Map<ActionDefinition, Element> matchedActionElements = new HashMap<>();
        matchedActionElements = fetchActionElements(actionRequest, mountRegistry);
        if (matchedActionElements.isEmpty()) {
            matchedActionElements = fetchActionElements(actionRequest, m_schemaRegistry);
        }
        return matchedActionElements;
    }

    private Map<ActionDefinition, Element> fetchActionElements(Element actionRequest, SchemaRegistry schemaRegistry) {
        Set<ActionDefinition> actionDefsList = schemaRegistry .retrieveAllActionDefinitions();
        Map<ActionDefinition, Element> matchedActionElements = new HashMap<>();
        for(ActionDefinition actionDef : actionDefsList){
            DataPath actionDataPath = getActionDataPath(actionDef, schemaRegistry);
            Element actionElement = getActionElement(actionDataPath, actionDef.getPath(), actionRequest);
            if ( actionElement != null){
                matchedActionElements.put(actionDef, actionElement);
            }

        }
        return matchedActionElements;
    }

    private DataPath getActionDataPath(ActionDefinition actionDef, SchemaRegistry schemaRegistry) {
        DataPath actionDataPath = DataPathUtil.buildParentDataPath(actionDef.getPath(), schemaRegistry);
        if (schemaRegistry.getMountPath() != null) {
            SchemaPath mountPath = schemaRegistry.getMountPath();
            DataPath mountDataPath = DataPathUtil.convertToDataPath(mountPath);
            return mountDataPath.createChild(actionDataPath.getPath());
        }
        return actionDataPath;
    }

    private Element getActionElement(DataPath datapath, SchemaPath actionSchemaPath, Element element){
        QName actionQName = actionSchemaPath.getLastComponent();
        Iterator<DataPathLevel> dataPathLevels = datapath.iterator();
        boolean isRootNode = true;
        List<Element> rootElements = new ArrayList();
        rootElements.add(element);
        while ( dataPathLevels.hasNext()){
            QName qName = dataPathLevels.next().getQName();
            if (isRootNode){
                isRootNode = false;
                continue;
            }
            boolean found = false;
            List<Element> childNodes = new ArrayList();
            for(Element rootElement : rootElements) {
                List<Element> childElements = DocumentUtils.getChildElements(rootElement);
                for (Element node : childElements) {
                    if (isSameNode(node, qName)) {
                        childNodes.add(node);
                        found = true;
                    }
                }
            }
            rootElements = childNodes;
            if ( !found){
                return null;
            }
        }
        List<Element> actionElements = new ArrayList<>();
        for(Element rootElement : rootElements) {
            if (isSameNode(rootElement, actionQName)) {
                actionElements.add(rootElement);
            }

            // thrown an error if duplicate action elements occured (more then one action nodes found in rpc)
            if(actionElements.size() > 1) {
                throwExceptionForDuplicateAction(actionSchemaPath.getLastComponent());
            }
        }
        return actionElements.size() > 0 ? actionElements.get(0) : null;
    }

    private void throwExceptionForDuplicateAction(QName errorPath) {
        throw new ValidationException(NetconfRpcErrorUtil.getNetconfRpcError(NetconfRpcErrorTag.BAD_ELEMENT,
                NetconfRpcErrorType.Protocol,
                NetconfRpcErrorSeverity.Error,
                String.format("Multiple action element exists within RPC %s", errorPath)));
    }

    private boolean isSameNode(Element node, QName qName){
        return node.getNamespaceURI().equals(qName.getNamespace().toString()) &&
                node.getLocalName().equals(qName.getLocalName());
    }

    public SchemaRegistry getMountRegistryIfApplicable( Element request){
        SchemaRegistry schemaRegistry = m_schemaRegistry;
        MountProviderInfo info = SchemaRegistryUtil.getMountProviderInfo(request, m_schemaRegistry);
        if ( info != null){
            schemaRegistry = info.getMountedRegistry();
        }
        return schemaRegistry;
    }

    private Map<RpcDefinition, Element> retrieveMatchedRpcElement(Element actionRequest){
        Map<RpcDefinition, Element> result = new HashMap<>();
        MountProviderInfo mountProviderInfo = SchemaRegistryUtil.getMountProviderInfo(actionRequest, m_schemaRegistry);
        if (mountProviderInfo != null && mountProviderInfo.getMountedRegistry() != null) {
            SchemaRegistry mountRegistry = mountProviderInfo.getMountedRegistry();
            Node rpcParentNode = mountProviderInfo.getMountedXmlNodeFromRequest();
            Collection<RpcDefinition> rpcDefs = mountRegistry.getRpcDefinitions();
            for ( RpcDefinition rpcDef : rpcDefs){
                NodeList childNodes = rpcParentNode.getChildNodes();
                for ( int index=0; index<childNodes.getLength(); index++){
                    Node childNode = childNodes.item(index);
                    if ( childNode.getNodeType()==Node.ELEMENT_NODE){
                        if ( rpcDef.getQName().getNamespace().toString().equals(childNode.getNamespaceURI()) &&
                                rpcDef.getQName().getLocalName().equals(childNode.getLocalName())){
                            result.put(rpcDef, (Element)mountProviderInfo.getMountedXmlNodeFromRequest());
                        }
                    }
                }
            }
        }
        return result;
    }

    public void onCreateSubscription(NetconfClientInfo client, NetconfRpcRequest request, ResponseChannel responseChannel) {
        NetConfResponse response = new NetConfResponse();
        response.setMessageId(request.getMessageId());
        if (!validateMessageId(request, response)) {
            sendResponse(responseChannel, request, response);
            return;
        }

        Document reqDoc = null;
        try {
            reqDoc = request.getRequestDocument();
        } catch (NetconfMessageBuilderException e) {
            LOGGER.error("Invalid request:{} ", reqDoc, e);
            onInvalidRequest(client, reqDoc, response);
            return;
        }

        RpcRequestHandler rpcHandler = m_rpcRequestHandlerRegistry.lookupRpcRequestHandler(request.getRpcName());
        try {
            TimingLogger.start();
            DSMTimingLogger.start();
            if (rpcHandler != null) {
                if (rpcHandler instanceof CreateSubscriptionRpcHandler) {
                    CreateSubscriptionRpcHandler createSubscriptionRpcHandler = (CreateSubscriptionRpcHandler)rpcHandler;
                    rpcHandler.validate(m_rpcConstraintParser, request);
                    createSubscriptionRpcHandler.processRequest(client, request, responseChannel);
                } else {
                    LOGGER.error("create-subscription RPC Handler is not found. Invalid request:{}", reqDoc);
                    onInvalidRequest(client, reqDoc, response);
                }
            } else {
                LOGGER.error("RPC handler is not found. Invalid request:{}", reqDoc);
                onInvalidRequest(client, reqDoc, response);
                sendResponse(responseChannel, request, response);
            }
        } catch (RpcValidationException e) {
            LOGGER.error("RPC request failed to validate:{} ", request, e);
            response.addError(e.getRpcError());
            sendResponse(responseChannel, request, response);
            return;
        } catch(RpcProcessException e) {
            LOGGER.error("Error occured while processing RPC", e);
            response.addErrors(e.getRpcErrors());
            sendResponse(responseChannel, request, response);
        }finally {
            TimingLogger.finish();
            DSMTimingLogger.finish();
        }
    }

    protected void sendResponse(ResponseChannel channel, AbstractNetconfRequest request, NetConfResponse response) {
        if (!channel.isSessionClosed()) {
            try {
                channel.sendResponse(response, request);
            } catch (NetconfMessageBuilderException e) {
                LOGGER.error("Can not send the response" , e);
            }
        }
    }


    @Override
    public void onHello(NetconfClientInfo client, Set<String> capabilities) {
        if(client == null){
            LOGGER.error("client info is null, cannot store session info");
        }else {
            Timestamp loginTime= new Timestamp(System.currentTimeMillis());
            m_sessions.put(client.getSessionId(), new NetConfSession(client.getUsername(), client.getRemoteHost(), Transport.NETCONF_SSH,loginTime));
        }
    }

    /**
     * Lock handling has specialized logic for both running and candidate stores.
     *
     * RFC is not clear on whether the lock follows the running logic or candidate logic
     */
    @Override
    public void onLock(NetconfClientInfo client, LockRequest lockRequest, NetConfResponse response) {
        if (!validateMessageId(lockRequest, response)) {
            return;
        }
        try {
            DataStore store = m_dataStores.get(lockRequest.getTarget());
            if (store == null) {
                response.setOk(false);
                NetconfRpcError rpcError = NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.INVALID_VALUE,
                        "Invalid target");
                response.addError(rpcError);
                return;
            }
            store.lock(client.getSessionId());
            m_sessions.get(client.getSessionId()).addLockedStore(store.getName());
            response.setOk(true);
        } catch (LockDeniedOtherOwnerException e) {
            LOGGER.error("Lock already held by other session ", e);
            response.setOk(false);
            NetconfRpcError rpcError = NetconfRpcErrorUtil.getNetconfRpcError(NetconfRpcErrorTag.LOCK_DENIED,
                    NetconfRpcErrorType.Protocol,
                    NetconfRpcErrorSeverity.Error,
                    "Lock failed, lock is already held")
                    .addErrorInfoElement(NetconfRpcErrorInfo.SessionId, Integer.toString(e.getLockOwner()));
            response.addError(rpcError);
            // response.addApplicationError("lock-denied", ErrorSeverity.ERROR).addErrorInfo("session-id", "" + e.getLockOwner());
        } catch (LockDeniedConfirmedCommitException e) {
            LOGGER.error("Lock denied, confirmed commit ongoing" , e);
            response.setOk(false);
            NetconfRpcError rpcError = NetconfRpcErrorUtil.getNetconfRpcError(NetconfRpcErrorTag.LOCK_DENIED,
                    NetconfRpcErrorType.Protocol,
                    NetconfRpcErrorSeverity.Error,
                    "Lock denied, confirmed commit ongoing")
                    .addErrorInfoElement(NetconfRpcErrorInfo.SessionId, Integer.toString(e.getLockOwner()));
            response.addError(rpcError);			
        } catch (LockDeniedUncommitedChangesException e) {
            LOGGER.error("Lock denied, Uncommitted changes present", e);
            response.setOk(false);
            NetconfRpcError rpcError = NetconfRpcErrorUtil.getNetconfRpcError(NetconfRpcErrorTag.LOCK_DENIED,
                    NetconfRpcErrorType.Protocol,
                    NetconfRpcErrorSeverity.Error,
                    "Lock denied, Uncommitted changes present");
            response.addError(rpcError);
        }
    }

    @Override
    public void onUnlock(NetconfClientInfo client, UnLockRequest unlockRequest, NetConfResponse response) {
        if (!validateMessageId(unlockRequest, response)) {
            return;
        }
        try {
            DataStore store = m_dataStores.get(unlockRequest.getTarget());
            if (store == null) {
                response.setOk(false);
                NetconfRpcError rpcError = NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.INVALID_VALUE,
                        "Invalid target");
                response.addError(rpcError);
                return;
            }
            store.unlock(client.getSessionId());
            m_sessions.get(client.getSessionId()).removeLockedStore(store.getName());
            response.setOk(true);
        } catch (UnlockFailedNoLockActiveException e) {
            LOGGER.error("Unlock failed, no active lock" , e);
            response.setOk(false);
            NetconfRpcError rpcError = NetconfRpcErrorUtil.getNetconfRpcError(NetconfRpcErrorTag.OPERATION_FAILED,
                    NetconfRpcErrorType.Protocol,
                    NetconfRpcErrorSeverity.Error,
                    "Unlock failed, no active lock");
            response.addError(rpcError);			
        } catch (UnlockFailedOtherOwnerException e) {
            LOGGER.error("Unlock failed, lock held by other owner" , e);
            response.setOk(false);
            NetconfRpcError rpcError = NetconfRpcErrorUtil.getNetconfRpcError(NetconfRpcErrorTag.OPERATION_FAILED,
                    NetconfRpcErrorType.Protocol,
                    NetconfRpcErrorSeverity.Error,
                    "Unlock failed, lock held by other owner").addErrorInfoElement(
                            NetconfRpcErrorInfo.SessionId,
                            Integer.toString(e.getLockOwner()));
            response.addError(rpcError);
        }
    }

    @Override
    public void onInvalidRequest(NetconfClientInfo client, Document invalidRequestDoc, NetConfResponse response) {
        try {
            LOGGER.error("Got a invalid netconf request from : {}{}{}",client ," ",DocumentUtils.documentToString(invalidRequestDoc));
        } catch (NetconfMessageBuilderException e) {
            LOGGER.error("Got a invalid netconf request from :{}",client, e);
        }
        response.setOk(false);
        NetconfRpcError rpcError = NetconfRpcErrorUtil.getNetconfRpcError(NetconfRpcErrorTag.MISSING_ATTRIBUTE,NetconfRpcErrorType.RPC,NetconfRpcErrorSeverity.Error,
                "Got an invalid netconf request from "+client);
        response.addError(rpcError);
    }

    /**
     * @param response
     */
    protected boolean validateMessageId(AbstractNetconfRequest request, NetConfResponse response) {
        if (request.getMessageId() == null || request.getMessageId().isEmpty()) {
            response.setOk(false);
            NetconfRpcError rpcError = NetconfRpcErrorUtil.getNetconfRpcError(
                    NetconfRpcErrorTag.MISSING_ATTRIBUTE,
                    NetconfRpcErrorType.RPC, NetconfRpcErrorSeverity.Error,
                    "<message-id> cannot be null/empty").addErrorInfoElement(
                            NetconfRpcErrorInfo.BadAttribute,
                            NetconfResources.MESSAGE_ID).addErrorInfoElement(
                                    NetconfRpcErrorInfo.BadElement, NetconfResources.RPC);
            response.addError(rpcError);
            return false;
        }
        return true;
    }

    @Override
    public void sessionClosed(String message, int sessionId) {
        closeSession(sessionId);
    }

}
