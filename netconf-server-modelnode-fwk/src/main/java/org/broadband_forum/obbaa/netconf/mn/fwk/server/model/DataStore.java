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

import static org.broadband_forum.obbaa.netconf.api.util.NetconfResources.IS_FULL_DS_REBUILT;
import static org.broadband_forum.obbaa.netconf.api.util.NetconfResources.TRIGGER_SYNC_UPON_SUCCESS_QNAME;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationUtil.isPostEditValidationSupported;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import javax.transaction.Transactional;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.logger.NetconfLogger;
import org.broadband_forum.obbaa.netconf.api.messages.AbstractNetconfRequest;
import org.broadband_forum.obbaa.netconf.api.messages.ActionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigTestOptions;
import org.broadband_forum.obbaa.netconf.api.messages.EditInfo;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfConfigChangeNotification;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorInfo;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorSeverity;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorType;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.messages.PojoToDocumentTransformer;
import org.broadband_forum.obbaa.netconf.api.server.NetconfQueryParams;
import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationService;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.api.utils.SystemPropertyUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.ChangeTreeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.PersistenceException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.DSValidationContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.DataStoreValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.DynamicDataStoreValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.TimingLogger;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.DSMTimingLogger;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.utils.TxException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.utils.TxService;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.NetconfRpcErrorUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.Operation;
import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.broadband_forum.obbaa.netconf.server.rpc.RequestType;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcPayloadConstraintParser;
import org.broadband_forum.obbaa.netconf.server.ssh.auth.AccessDeniedException;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.joda.time.DateTime;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.slf4j.helpers.MessageFormatter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.annotations.VisibleForTesting;

public class DataStore {
    
    public static final Long REQUEST_TIME_LIMIT = 10000L;
    public static boolean c_forcePrintStats;

    static {
        c_forcePrintStats = Boolean.valueOf(SystemPropertyUtils.getInstance().getFromEnvOrSysProperty("FORCE_PRINT_DS_STATS", "false"));
    }

    public static final int NO_LOCK = -1;
    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(DataStore.class, LogAppNames.NETCONF_STACK);
    private int m_lockOwner = NO_LOCK;
    private boolean m_confirmedCommitPending = false;
    private boolean m_hasUncommitedChanges = false;

    private RootModelNodeAggregator m_rootModelNodeAggregator;
    private String m_name;
    private NotificationExecutor m_editNotificationExecutor;
    private NotificationService m_notificationService;
    private DataStoreValidator m_validator;
    private DynamicDataStoreValidator m_dynamicValidator;
    private NamespaceContext m_namespaceContext;
    private NbiNotificationHelper m_nbiNotificationHelper;
    private TxService m_txService;
    private NetconfLogger m_netconfLogger;
    private RpcPayloadConstraintParser m_rpcConstraintParser;
    private static final TransformerFactory c_transformerFactory = TransformerFactory.newInstance();
    private List<AbstractNetconfRequest> m_requestListForTest = null;    
    private DateTime m_lockTime = null;
    private SubSystem m_globalSubSystem;

    public DateTime getLockTime() {
        return m_lockTime;
    }

    protected void setLockTime(DateTime lockTime) {
        this.m_lockTime = lockTime;
    }

    /*
    * for UT Only
    * ***/
    protected void setHasUnCommitedChanges(boolean value) {
        m_hasUncommitedChanges = value;
    }

    /*
    * for UT Only
    * ***/
    protected void setconfirmedCommitPending(boolean value) {
        m_confirmedCommitPending = value;
    }

    /*
    * for UT Only
    * ***/
    protected void setLockOwner(int value) {
        m_lockOwner = value;
    }

    public int getLockOwner() {
        return m_lockOwner;
    }

    @Deprecated
    public DataStore(String name, RootModelNodeAggregator root, SubSystemRegistry subSystemRegistry) {
    	this(name, root, subSystemRegistry, null);
    }

    public DataStore(String name, RootModelNodeAggregator root, SubSystemRegistry subSystemRegistry, DataStoreValidator validator) {
        this(name, root, subSystemRegistry, validator, new TxService(), null);
    }
    
    public DataStore(String name, RootModelNodeAggregator root, SubSystemRegistry subSystemRegistry, DataStoreValidator validator, TxService txService) {
        this(name, root, subSystemRegistry, validator, txService, null);
    }

    public DataStore(String name, RootModelNodeAggregator root, SubSystemRegistry subSystemRegistry, DataStoreValidator validator, TxService txService, NetconfLogger netconfLogger) {
        m_name = name;
        m_rootModelNodeAggregator = root;
        m_editNotificationExecutor = new SubsystemNotificationExecutor(root.getSubsystemRegistry());
        m_validator = validator;
        m_txService = txService;
        m_netconfLogger = netconfLogger;
        m_globalSubSystem = root.getSubsystemRegistry().getCompositeSubSystem();
    }

    public DataStoreValidator getValidator() {
        return m_validator;
    }

    public void setValidator(DataStoreValidator validator) {
        this.m_validator = validator;
    }

    public DynamicDataStoreValidator getDynamicValidator() {
        return m_dynamicValidator;
    }

    public void setDynamicValidator(DynamicDataStoreValidator validator) {
        this.m_dynamicValidator = validator;
    }

    public NamespaceContext getNamespaceContext() {
        return m_namespaceContext;
    }

    public void setNamespaceContext(NamespaceContext namespaceContext) {
        this.m_namespaceContext = namespaceContext;
    }


    public NbiNotificationHelper getNbiNotificationHelper() {
        return m_nbiNotificationHelper;
    }

    public void setNbiNotificationHelper(NbiNotificationHelper nbiNotificationHelper) {
        this.m_nbiNotificationHelper = nbiNotificationHelper;
    }
    
	public RpcPayloadConstraintParser getRpcConstraintParser() {
		return m_rpcConstraintParser;
	}

	public void setRpcConstraintParser(RpcPayloadConstraintParser rpcConstraintParser) {
		this.m_rpcConstraintParser = rpcConstraintParser;
	}

    public void lock(int sessionId) throws LockDeniedOtherOwnerException, LockDeniedConfirmedCommitException,
        LockDeniedUncommitedChangesException {
        if (m_hasUncommitedChanges) {
            // only relevant for a candidate datastore ....
            throw new LockDeniedUncommitedChangesException();

        } else if (m_lockOwner >= 0 && m_lockOwner != sessionId) {
            if (m_confirmedCommitPending) {
                // can only happen on a running datastore
                throw new LockDeniedConfirmedCommitException(m_lockOwner);
            } else {
                throw new LockDeniedOtherOwnerException(m_lockOwner);
            }
        }

        // all conditions satisfied
        this.m_lockOwner = sessionId;        
        this.m_lockTime = new DateTime();
    }

    public void unlock(int sessionId) throws UnlockFailedNoLockActiveException, UnlockFailedOtherOwnerException {
        if (m_lockOwner == NO_LOCK) {
            throw new UnlockFailedNoLockActiveException();
        } else if (m_lockOwner == sessionId) {
            m_lockOwner = NO_LOCK;
            m_lockTime = null;//resetting lock time
        } else {
            throw new UnlockFailedOtherOwnerException(m_lockOwner);
        }
    }

    public boolean hasUncommitedChanges() {
        return false;
    }

    public boolean hasOngoingConfirmedCommitFromOtherSession(int sessionId) {
        return false;
    }

    @Transactional(value=javax.transaction.Transactional.TxType.REQUIRED, rollbackOn={RuntimeException.class,Exception.class})
    public void copyFrom(NetconfClientInfo clientInfo, DataStore source) throws LockedByOtherSessionException, CopyConfigException {
        if (m_lockOwner != NO_LOCK && m_lockOwner != clientInfo.getSessionId()) {
            throw new LockedByOtherSessionException(m_lockOwner);
        }
        try {
            m_rootModelNodeAggregator.copyConfig(source.getConfig(clientInfo, DocumentUtils.getNewDocument(), null, NetconfQueryParams.NO_PARAMS));
            List<ChangeNotification> copyNotification = new ArrayList<ChangeNotification>();
            for (ModelNode rootNode : m_rootModelNodeAggregator.getModelServiceRoots()) {
                copyNotification.add(new CopyConfigChangeNotification(source.getName(), this.getName(), null, rootNode));
            }
            //FIXME: FNMS-833 provide a way to deploy a subsystem for root node
            //m_subSystemRegistry.lookupSubsystem(m_rootModelNodeAggregator.getClass().getName()).notifyChanged(copyNotification);
        } catch (ParserConfigurationException e) {
            LOGGER.error("Copy config failed ", e);
            throw new CopyConfigException(NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.OPERATION_FAILED,
                    e.getMessage()));
        } catch (GetException e) {
            LOGGER.error("Error while getting ModelNodeId ", e);
            throw new CopyConfigException(e.getRpcError());
        }

    }

    @Transactional(value=javax.transaction.Transactional.TxType.REQUIRED, rollbackOn={RuntimeException.class,Exception.class})
    public void copyFrom(NetconfClientInfo clientInfo, Element sourceConfigElement) throws LockedByOtherSessionException, CopyConfigException {
        if (m_lockOwner != NO_LOCK && m_lockOwner != clientInfo.getSessionId()) {
            throw new LockedByOtherSessionException(m_lockOwner);
        }
        m_rootModelNodeAggregator.copyConfig(DocumentUtils.getChildElements(sourceConfigElement));
        List<ChangeNotification> copyNotification = new ArrayList<ChangeNotification>();
        for (ModelNode rootNode : m_rootModelNodeAggregator.getModelServiceRoots()) {
            copyNotification.add(new CopyConfigChangeNotification(null, this.getName(), sourceConfigElement, rootNode));
        }

        //FIXME:FNMS-833  provide a way to deploy a subsystem for root node
        //m_subSystemRegistry.lookupSubsystem(m_rootModelNodeAggregator.getClass().getName()).notifyChanged(copyNotification);
    }

    public String getName() {
        return m_name;
    }

    @Transactional(value=javax.transaction.Transactional.TxType.REQUIRED, rollbackOn={RuntimeException.class,Exception.class})
    public List<Element> getConfig(NetconfClientInfo client, Document doc, FilterNode root, NetconfQueryParams params) throws GetException {
        TimingLogger.start();
        DSMTimingLogger.start();
        try {
            DSMTimingLogger.startPhase("get-config");
            return m_rootModelNodeAggregator.getConfig(new GetConfigContext(client, doc, root, new ConfigAttributeGetContext()), params);
        }finally{
            DSMTimingLogger.endPhase("get-config");
            try {
                TimingLogger timings = TimingLogger.finish();
                DSMTimingLogger dsmTimingLogger = DSMTimingLogger.finish();
                Long totalTime = timings.getTotalTime();
                boolean forcePrint = totalTime > REQUEST_TIME_LIMIT || c_forcePrintStats;

                if (forcePrint) {
                    String timingsString = "get-config Time details: " + timings.toString();
                    String dsmTimingString = "get-config DSM time details: " + dsmTimingLogger.toString();
                    LOGGER.info(timingsString);
                    LOGGER.info(dsmTimingString);
                }
            } catch (Exception e) {
                LOGGER.warn("Problem in logging timing info", e);
            }
        }
    }

    public List<Element> get(NetconfClientInfo client, Document doc, FilterNode root, NetconfQueryParams params) throws GetException {
        TimingLogger.start();
        DSMTimingLogger.start();
        try {
            DSMTimingLogger.startPhase("get");
            return m_rootModelNodeAggregator.get(new GetContext(client, doc, root, new StateAttributeGetContext(), new ConfigAttributeGetContext()), params);
        } finally{
            DSMTimingLogger.endPhase("get");
            try {
                TimingLogger timings = TimingLogger.finish();
                DSMTimingLogger dsmTimingLogger = DSMTimingLogger.finish();
                Long totalTime = timings.getTotalTime();
                boolean forcePrint = totalTime > REQUEST_TIME_LIMIT || c_forcePrintStats;

                if (forcePrint) {
                    String timingsString = "get Time details: " + timings.toString();
                    String dsmTimingString = "get DSM time details: " + dsmTimingLogger.toString();
                    LOGGER.info(timingsString);
                    LOGGER.info(dsmTimingString);
                }
            } catch (Exception e) {
                LOGGER.warn("Problem in logging timing info", e);
            }
        }
    }

    public List<Element> action(Document doc, ActionRequest actionRequest, NetconfClientInfo clientInfo, SchemaRegistry registry) throws ActionException {
        return m_rootModelNodeAggregator.action(actionRequest, clientInfo, registry);
    }
    
    
    /**
     * Validate the datastore and create validation internal edit requests recursively
     * @param request
     * @param clientInfo
     * @return List of validation internal edit requests
     * @throws EditConfigException
     * @throws EditConfigTestFailedException
     * @throws PersistenceException
     * @throws LockedByOtherSessionException
     */
	public Map<EditConfigRequest, NotificationContext> editInternal(EditConfigRequest request, NetconfClientInfo clientInfo) throws EditConfigException,
			LockedByOtherSessionException, ValidationException {
        TimingLogger.start();
        DSMTimingLogger.start();
        NotificationContext notificationContext = new NotificationContext();
        try {

            if (m_requestListForTest != null) {
                m_requestListForTest.add(request);
            }

            int sessionId = 0;
            long startTime = System.currentTimeMillis();
            long validationTimeElapsed = 0l;
            long dsTimeElapsed = 0l;
            if (clientInfo == null) {
                sessionId = DataStore.NO_LOCK;
            } else {
                sessionId = clientInfo.getSessionId();
            }
            if (m_lockOwner != NO_LOCK && m_lockOwner != sessionId) {
                throw new LockedByOtherSessionException(m_lockOwner);
            }

            // Currently can work with set only
            if (!request.getTestOption().equals(EditConfigTestOptions.SET)) {
                throw new EditConfigException(NetconfRpcErrorUtil.getNetconfRpcError(
                        NetconfRpcErrorTag.OPERATION_NOT_SUPPORTED, NetconfRpcErrorType.Protocol,
                        NetconfRpcErrorSeverity.Error, "Can do only test-option set only"));
            }

            if(request.getClientInfo() == null) {
                request.setClientInfo(clientInfo);
            }

            // save the messageid and sessionid, to regroup the changes later.
            TimingLogger.startPhase("createEditTree");
            DataStoreValidationUtil.setRootModelNodeAggregatorInCache(m_rootModelNodeAggregator);
            List<EditContainmentNode> editTrees = m_rootModelNodeAggregator.editConfig(request, notificationContext);
            TimingLogger.endPhase("createEditTree");
            dsTimeElapsed = System.currentTimeMillis() - startTime;
            List<EditConfigRequest> internalEditRequests = null;
            Map<EditConfigRequest, NotificationContext> editRequestNotificationContextMap = new LinkedHashMap<EditConfigRequest, NotificationContext>();
            editRequestNotificationContextMap.put(request, notificationContext);
            if (isPostEditValidationSupported()) {
                TimingLogger.startPhase("iterateEditTree");
                for (EditContainmentNode editTree : editTrees) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("performing post edit-config data store validation on root node {}",
                                editTree.getQName());
                    }
                    // Validate the data store and return validation edit request if any
                    TimingLogger.startPhase("iterateEditTree.validateDataStore");
                    internalEditRequests = validateDataStore(editTree, notificationContext.getChangeTree(), request, clientInfo);
                    TimingLogger.endPhase("iterateEditTree.validateDataStore", false);
                    if (internalEditRequests != null && !internalEditRequests.isEmpty()) {
                        EditConfigRequest internalEditRequest = internalEditRequests.get(0);
                        TimingLogger.startPhase("iterateEditTree.validateEditRequest");
                        validateEditRequest(internalEditRequest);
                        TimingLogger.endPhase("iterateEditTree.validateEditRequest", false);
                        // Call editInternal() recursively if we have internal validation edit request
                        if (m_netconfLogger != null) {
                            try {
                                m_netconfLogger.logRequest(clientInfo.getRemoteHost(), clientInfo.getRemotePort(), clientInfo.getUsername(), "Internal request for session " + clientInfo.getSessionId(), internalEditRequest.getRequestDocument());
                            } catch (NetconfMessageBuilderException e) {
                            }
                        }
                        TimingLogger.startPhase("iterateEditTree.editInternal");
                        Map<EditConfigRequest, NotificationContext> internalEditRequestMap = editInternal(internalEditRequest, internalEditRequest.getClientInfo());
                        TimingLogger.endPhase("iterateEditTree.editInternal", false);
                        if (!internalEditRequestMap.isEmpty()) {
                            editRequestNotificationContextMap.putAll(internalEditRequestMap);
                        }
                    }
                }
                TimingLogger.endPhase("iterateEditTree");
                TimingLogger.startPhase("validateDataStoreDynamic");
                try {
                    validateDataStoreDynamic(editTrees, request, clientInfo);
                } finally {
                    TimingLogger.endPhase("validateDataStoreDynamic");
                }
                validationTimeElapsed = System.currentTimeMillis() - startTime - dsTimeElapsed;
            } else {
                LOGGER.debug("post edit-config data store validation skipped");
            }
            long totalTimeElapsed = System.currentTimeMillis() - startTime;
            boolean forcePrint = totalTimeElapsed > REQUEST_TIME_LIMIT || c_forcePrintStats;
            if (forcePrint) {
                String timingString = format("edit-config with message-id {} took {} ms in which validation took {} ms , changes to datastore took {} ms",
                        request.getMessageId(), totalTimeElapsed, validationTimeElapsed, dsTimeElapsed);
                TotalTime tt = getTotalTime();
                tt.add(totalTimeElapsed, validationTimeElapsed, dsTimeElapsed);
                LOGGER.info(timingString);
                if(LOGGER.isDebugEnabled()){
                    LOGGER.debug("Edit trees: {}", LOGGER.sensitiveData(editTrees));
                }
            }
            return editRequestNotificationContextMap;
        } finally {
            DataStoreValidationUtil.clearRootModelNodeAggregatorInCache();
            // Validation timing should be printed before createOrDeleteNodes
            // to avoid nested info
            try {
                TimingLogger timings = TimingLogger.finish();
                DSMTimingLogger dsmTimingLogger = DSMTimingLogger.finish();
                Long totalTime = timings.getTotalTime();
                boolean forcePrint = totalTime > REQUEST_TIME_LIMIT || c_forcePrintStats;

                if (forcePrint) {
                    String timingsString = "Edit Time details for request with message-id " + request.getMessageId() + " is: " + timings.toString();
                    String dsmTimingString = "DSM time details for request with message-id " + request.getMessageId() + " is: " + dsmTimingLogger.toString();
                    LOGGER.info(timingsString);
                    LOGGER.info(dsmTimingString);
                }
            } catch (Exception e) {
                LOGGER.warn("Problem in logging timing info", e);
            }
        }
    }

    public void checkRequiredPermissions(Set<SubSystem> subSystems, NetconfClientInfo clientInfo) throws AccessDeniedException {
        // Verify if required permissions are present
        try {
            for(SubSystem subSystem : subSystems) {
                if (subSystem != null) {
                    subSystem.checkRequiredPermissions(clientInfo, Operation.EDIT.getType());
                }
            }
        } catch (AccessDeniedException e) {
            throw new EditConfigException(e.getRpcError());
        }
    }

    private TotalTime getTotalTime() {
        TotalTime tt = (TotalTime) RequestScope.getCurrentScope().getFromCache("DataStore#totalTime");
        if(tt == null){
            tt = new TotalTime();
            RequestScope.getCurrentScope().putInCache("DataStore#totalTime", tt);
        }
        return tt;
    }


    /**
	 * In this method, validate the editconfig request and it's internal edit requests recursively
	 * @param request
	 * @param response
	 * @param clientInfo
	 * @return List of Notification
	 * @throws EditConfigException
	 * @throws EditConfigTestFailedException
	 * @throws PersistenceException
	 * @throws LockedByOtherSessionException
	 */
	public List<Notification> edit(EditConfigRequest request, NetConfResponse response, NetconfClientInfo clientInfo) throws EditConfigException,
			EditConfigTestFailedException, PersistenceException, LockedByOtherSessionException {
        long startTime = System.currentTimeMillis();
        AtomicLong preCommitTime = new AtomicLong();
        /**
		 * 1) Validating the datastore - Make a single transaction and editInternal() internally called recursively
		 * 2) Sending the notifications - After successful validation, send the notification to SBI and return the notifications
		 */
		Map<EditConfigRequest, NotificationContext> editRequestsMap = Collections.emptyMap();
		try {
			editRequestsMap = m_txService
				.executeWithTxRequired(() -> {
                    try {
                        // Validate the DataStore and return editrequest and it's corresponding NotificaitonContext
                        Map<EditConfigRequest, NotificationContext> editReqsMap = editInternal(request, clientInfo);
                        if ( m_validator != null){
                            m_validator.clearValidationCache();
                        }
                        preCommitTime.set(preCommit(request, startTime, editReqsMap));
                        return editReqsMap;
                    } catch (EditConfigException | LockedByOtherSessionException | ValidationException e) {
                        throw new TxException(e);
                    }
                });
		} catch (TxException e) {
			// Throw the actual exception to NetconfServerImpl#editConfig
			Throwable cause = e.getCause();
			checkInstanceAndRethrow(cause);
		}
		long postCommitStartTime = System.currentTimeMillis();
        List<Notification> notifications = postCommit(request, response, startTime, editRequestsMap);
        long postCommitEndTime = System.currentTimeMillis();
        boolean tookMoreTime = (postCommitEndTime - startTime) > REQUEST_TIME_LIMIT;
        if(tookMoreTime || c_forcePrintStats) {
            printTotalTime(request.getMessageId(), postCommitEndTime - postCommitStartTime, preCommitTime.get()); // add message-id to log
        }
		return notifications;
    }

    private long preCommit(EditConfigRequest request,long startTime, Map<EditConfigRequest, NotificationContext> editRequestsMap) {
        long preCommitStartTime = System.currentTimeMillis();
        TimingLogger.start();
        try {
            sendPreCommitNotifications(request, editRequestsMap);
            long preCommitEndTime = System.currentTimeMillis();
            long preCommitTimeElapsed = preCommitEndTime - preCommitStartTime;
            boolean tookMoreTime = (preCommitEndTime - startTime) > REQUEST_TIME_LIMIT;
            if(tookMoreTime || c_forcePrintStats) {
                LOGGER.debug("Pre commit notifications took {} ms", preCommitTimeElapsed);
            }
            return preCommitTimeElapsed;
        } finally {
            try {
                TimingLogger timings = TimingLogger.finish();
                Long totalPreCommitTime = timings.getTotalTime();
                boolean forcePrint = totalPreCommitTime > REQUEST_TIME_LIMIT || c_forcePrintStats;
                if (forcePrint) {
                    String timingsString = "Pre commit notification Time details: " + timings.toString();
                    LOGGER.info(timingsString);
                }
            } catch (Exception e) {
                LOGGER.warn("Problem in logging timing info", e);
            }
        }
    }

    private void sendPreCommitNotifications(EditConfigRequest request, Map<EditConfigRequest, NotificationContext> editRequestsMap) {
        try {
            if (editRequestsMap != null && !editRequestsMap.isEmpty()) {
                for (Map.Entry<EditConfigRequest, NotificationContext> entry : editRequestsMap.entrySet()) {
                    EditConfigRequest editRequest = entry.getKey();
                    NotificationContext notificationContext = entry.getValue();

                    if (isNewNotifStructureEnabled()) {
                        Map<SubSystem, Map<SchemaPath, List<ChangeTreeNode>>> subSystemChangeTreeNodeMap =
                                m_editNotificationExecutor.getSubSystemChangeTreeNodeMap(notificationContext.getChangeTree(), request.isUploadToPmaRequest(), request.getClientInfo());
                        checkRequiredPermissions(subSystemChangeTreeNodeMap.keySet(), editRequest.getClientInfo());
                        Map<SchemaPath, List<ChangeTreeNode>> allChanges = new HashMap<>();
                        for (Map<SchemaPath, List<ChangeTreeNode>> change : subSystemChangeTreeNodeMap.values()) {
                            allChanges.putAll(change);
                        }
                        //this will be the Composite SubSystem
                        m_globalSubSystem.preCommit(allChanges);
                        m_editNotificationExecutor.sendPreCommitNotificationsV2(subSystemChangeTreeNodeMap);
                    } else {
                        Map<SubSystem, IndexedNotifList> subSystemNotificationMap = m_editNotificationExecutor
                                .getSubSystemNotificationMap(request.getTarget(), notificationContext.getNotificationInfos(), request);
                        checkRequiredPermissions(subSystemNotificationMap.keySet(), editRequest.getClientInfo());
                        m_editNotificationExecutor.sendPreCommitNotifications(subSystemNotificationMap);
                    }
                }
            }
        } catch (SubSystemValidationException e) {
            LOGGER.error("Sending precommit notifications failed due to: ", e);
            throw new EditConfigException(e.getRpcError());
        }
        LOGGER.debug("Sending pre-commit notifications to subsystems done");
    }

    private List<Notification> postCommit(EditConfigRequest request, NetConfResponse response, long startTime,
                                          Map<EditConfigRequest, NotificationContext> editRequestsMap) {
        if (!Boolean.parseBoolean(SystemPropertyUtils.getInstance().getFromEnvOrSysProperty(IS_FULL_DS_REBUILT, "false"))) {
            long pcStartTime = System.currentTimeMillis();
            TimingLogger.start();
            try {
                List<Notification> notifications = sendNotification(request, response, editRequestsMap);
                long pcEndTime = System.currentTimeMillis();
                long postCommitTimeElapsed = pcEndTime - pcStartTime;
                boolean tookMoreTime = (pcEndTime - startTime) > REQUEST_TIME_LIMIT;
                if (tookMoreTime || c_forcePrintStats) {
                    LOGGER.debug("Post commit notifications took {} ms", postCommitTimeElapsed);
                }
                return notifications;
            } finally {
                try {
                    TimingLogger timings = TimingLogger.finish();
                    Long totalPostCommitTime = timings.getTotalTime();
                    boolean forcePrint = totalPostCommitTime > REQUEST_TIME_LIMIT || c_forcePrintStats;
                    if (forcePrint) {
                        String timingsString = "Post commit notification Time details: " + timings.toString();
                        LOGGER.info(timingsString);
                    }
                } catch (Exception e) {
                    LOGGER.warn("Problem in logging timing info", e);
                }
            }
        }
        return null;
    }

    private void printTotalTime(String messageId, long postCommitTimeElapsed, long preCommitTime) {
        TotalTime tt = getTotalTime();
        String timingString = format("Entire edit-config(including edits for default handling) with message-id {} took {} ms in which validation took {} ms , " +
                        "changes to datastore took {} ms and pre-commit notifications took {} ms, post-commit notifications took {}", messageId,
                tt.getTotalTime() + postCommitTimeElapsed, tt.getValidationTime(), tt.getDsTime(), preCommitTime, postCommitTimeElapsed);
        LOGGER.info(timingString);
        LOGGER.info("Entire edit-config time csv {}, {}, {}, {}, {}", tt.getTotalTime() + postCommitTimeElapsed, tt.getValidationTime(), tt.getDsTime(), preCommitTime, postCommitTimeElapsed);
    }

    /**
	 * Send edit-config change notification, NC Extension notification to SBI after successful Datastore validation
	 * @param request
	 * @param response
	 * @param editRequestsMap
	 * @return
	 */
	private List<Notification> sendNotification(EditConfigRequest request, NetConfResponse response,
			Map<EditConfigRequest, NotificationContext> editRequestsMap) {

		List<Notification> changeNotifications = new ArrayList<Notification>();
		if (editRequestsMap != null && !editRequestsMap.isEmpty()) {
			for (Map.Entry<EditConfigRequest, NotificationContext> entry : editRequestsMap.entrySet()) {
				EditConfigRequest editRequest = entry.getKey();
				NotificationContext notificationContext = entry.getValue();
                Map<SubSystem, IndexedNotifList> subSystemNotificationMap = null;
                Map<SubSystem, Map<SchemaPath, List<ChangeTreeNode>>> subSystemChangeTreeNodeMap = null;
                if(isNewNotifStructureEnabled()) {
                    subSystemChangeTreeNodeMap =
                            m_editNotificationExecutor.getSubSystemChangeTreeNodeMap(notificationContext.getChangeTree(), request.isUploadToPmaRequest(), editRequest.getClientInfo());
                } else {
                    subSystemNotificationMap = m_txService.executeWithTxRequired(() -> m_editNotificationExecutor
                            .getSubSystemNotificationMap(m_name, notificationContext.getNotificationInfos(), editRequest));
                }
                TimingLogger.startPhase("getNetconfConfigChangeNotifications");
                List<Notification> editConfigChangeNotifications;
                if(isNewNotifStructureEnabled()) {
                    editConfigChangeNotifications = m_nbiNotificationHelper.getNetconfConfigChangeNotificationsUsingAggregatorCTN(notificationContext.getChangeTree(), editRequest.getClientInfo());
                } else {
                    editConfigChangeNotifications = m_nbiNotificationHelper
                            .getNetconfConfigChangeNotifications(subSystemNotificationMap, editRequest.getClientInfo());
                }
                TimingLogger.endPhase("getNetconfConfigChangeNotifications");
				LOGGER.debug("sending edit-config change notification to subsystems");
                TimingLogger.startPhase("sendNotifications");
                if(isNewNotifStructureEnabled()) {
                    m_editNotificationExecutor.sendNotificationsV2(subSystemChangeTreeNodeMap);
                } else {
                    m_editNotificationExecutor.sendNotifications(subSystemNotificationMap);
                }
                TimingLogger.endPhase("sendNotifications");
                if (request.isTriggerSyncUponSuccess()) {
                	LOGGER.debug("sending handle NC extensions notification to subsystems");
	                TimingLogger.startPhase("sendHandleNcExtNotifications");
	                List<Element> ncExtensionResponses = m_editNotificationExecutor.sendNcExtensionNotifications(subSystemChangeTreeNodeMap, TRIGGER_SYNC_UPON_SUCCESS_QNAME);
	                response.setNcExtensionResponses(TRIGGER_SYNC_UPON_SUCCESS_QNAME, ncExtensionResponses);
	                TimingLogger.endPhase("sendHandleNcExtNotifications");
                }
                if (editRequest.isUploadToPmaRequest()) {
					for (Notification notification : editConfigChangeNotifications) {
						((SubsystemNotificationExecutor) m_editNotificationExecutor)
								.refineNetconfConfigChangeNotification((NetconfConfigChangeNotification) notification);
					}
				}
				// skip Implied as true for original edit request
				if (!editRequest.equals(request)) {
					for (Notification notification : editConfigChangeNotifications) {
						if (notification instanceof NetconfConfigChangeNotification) {
							List<EditInfo> editList = ((NetconfConfigChangeNotification) notification).getEditList();
							for (EditInfo editInfo : editList) {
								editInfo.setImplied(true);
							}
						}
					}
				}
				changeNotifications.addAll(editConfigChangeNotifications);
			}
		}
		return changeNotifications;
	}

    private boolean isNewNotifStructureEnabled() {
        if (SystemPropertyUtils.getInstance() != null) {
           // return Boolean.parseBoolean(SystemPropertyUtils.getInstance().getFromEnvOrSysProperty("ENABLE_NEW_NOTIF_STRUCTURE", "true")); commented for obbaa
            return Boolean.parseBoolean(SystemPropertyUtils.getInstance().getFromEnvOrSysProperty("ENABLE_NEW_NOTIF_STRUCTURE", "false"));
        }
        return false;
    }

	protected void checkInstanceAndRethrow(Throwable cause) throws EditConfigTestFailedException, EditConfigException,
			PersistenceException, LockedByOtherSessionException {
		if (cause instanceof EditConfigException) {
			throw (EditConfigException) cause;
		} else if (cause instanceof EditConfigTestFailedException) {
			throw (EditConfigTestFailedException) cause;
		} else if (cause instanceof PersistenceException) {
			throw (PersistenceException) cause;
		} else if (cause instanceof LockedByOtherSessionException) {
			throw (LockedByOtherSessionException) cause;
		} else if(cause instanceof ValidationException){
			throw (ValidationException) cause;
		} else {
			throw new RuntimeException(cause);
		}
	}
    
    public static String format(String string, Object...objects) {
        return MessageFormatter.arrayFormat(string, objects).getMessage();
    }
    
    /**
     * for UT Only
     */
    @VisibleForTesting
    protected void validateDataStores(EditContainmentNode editTree, EditConfigRequest request, NetconfClientInfo clientInfo) throws EditConfigException {
        validateDataStore(editTree, null, request, clientInfo);
    }

    private List<EditConfigRequest> validateDataStore(EditContainmentNode editTree, ChangeTreeNode changeTree, EditConfigRequest request,
                                                      NetconfClientInfo clientInfo) throws EditConfigException {
        try {
            if (m_validator != null) {
                EditContainmentNode.setParentForEditContainmentNode(editTree, null);
                return m_validator.validate(m_rootModelNodeAggregator, editTree, changeTree, request, clientInfo);
            } else {
                LOGGER.warn("DS Validator is null. Post-edit config validation will not happen for editTree {}", LOGGER.sensitiveData(editTree));
            }
        } catch (ValidationException e) {
            LOGGER.error("Post edit-config validation failed.", e);
            EditConfigException exception = new EditConfigException(e.getRpcError());
            exception.setRpcErrors(e.getRpcErrors());
            exception.addSuppressed(e);
            throw exception;
        }
        return null;
    }

    private List<Notification> validateDataStoreDynamic(List<EditContainmentNode> editTrees, EditConfigRequest request, NetconfClientInfo clientInfo) throws EditConfigException {
        try {
            if (m_dynamicValidator != null) {
                return m_dynamicValidator.validate(m_rootModelNodeAggregator, editTrees, request, clientInfo);
            } else {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("DS m_dynamicValidator is null. Post-edit config dynamic validation will not happen for editTree {}", LOGGER.sensitiveData(editTrees));
                }
            }
        } catch (ValidationException e) {
            LOGGER.error("Post edit-config dynamic validation failed.", e);
            EditConfigException exception = new EditConfigException(e.getRpcError());
            exception.setRpcErrors(e.getRpcErrors());
            exception.addSuppressed(e);
            throw exception;
        }
        return null;
    }
    
    /**
     * Validate the edit request
     * @param request
     * @throws ValidationException
     */
	private void validateEditRequest(EditConfigRequest request) throws ValidationException {
		NetconfRpcError rpcError = null;
		if (request.getMessageId() == null || request.getMessageId().isEmpty()) {
			rpcError = NetconfRpcErrorUtil
					.getNetconfRpcError(NetconfRpcErrorTag.MISSING_ATTRIBUTE, NetconfRpcErrorType.RPC,
							NetconfRpcErrorSeverity.Error, "<message-id> cannot be null/empty")
					.addErrorInfoElement(NetconfRpcErrorInfo.BadAttribute, NetconfResources.MESSAGE_ID)
					.addErrorInfoElement(NetconfRpcErrorInfo.BadElement, NetconfResources.RPC);

		}

		if (!PojoToDocumentTransformer.validateDefaultEditOpertation(request.getDefaultOperation())) {
			rpcError = NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.INVALID_VALUE,
					"invalid default-operation: " + request.getDefaultOperation());
		}

		if (!PojoToDocumentTransformer.validateTestOption(request.getTestOption())) {
			rpcError = NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.INVALID_VALUE,
					"invalid test-option: " + request.getTestOption());
		}

		if (!PojoToDocumentTransformer.validateErrorOption(request.getErrorOption())) {
			rpcError = NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.INVALID_VALUE,
					"invalid error-option: " + request.getErrorOption());
		}

		if (rpcError != null) {
			throw new ValidationException(rpcError);
		}
		if(m_rpcConstraintParser != null){
			m_rpcConstraintParser.validate(request, RequestType.EDIT_CONFIG);
		}
		
		OutputStream byteOS = new ByteArrayOutputStream();
		try {
			c_transformerFactory.newTransformer().transform(new DOMSource(request.getConfigElement().getXmlElement()),
					new StreamResult(byteOS));
		} catch (TransformerException | TransformerFactoryConfigurationError | NetconfMessageBuilderException e) {
			// Ignored
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Error while transform DOMSource", e.getMessage());
			}
		}
	}

    /**
     * retrieve the subsystem associated with the datastore
     */
    public SubSystem getSubsystem() {
        return null;
        //FIXME: FNMS-833 provide a way to deploy a subsystem for root node
        //return m_subSystemRegistry.lookupSubsystem(m_rootModelNodeAggregator.getClass().getName());
    }

    public NotificationService getNotificationService() {
        return m_notificationService;
    }

    public void setNotificationService(NotificationService notificationService) {
        this.m_notificationService = notificationService;
    }

    /**
     * for UTs
     *
     * @param editNotificationExecutor
     */
    protected void setEditNotificationExecutor(NotificationExecutor editNotificationExecutor) {
        m_editNotificationExecutor = editNotificationExecutor;
    }

    public <T> T withValidationContext(ValidationContextTemplate<T> validationContextTemplate) throws ValidationException {
        return new DSValidationContext().withRootNodeAggregator(new DSValidationContext.RootNodeAggregatorTemplate<T>() {
            @Override
            public <T> T execute() {
                return validationContextTemplate.validate();
            }
        }, m_rootModelNodeAggregator);
    }
    
	public void enableUTSupport() {
	    m_requestListForTest = new ArrayList<>();
	}
	
	public void disableUTSupport() {
	    m_requestListForTest = null;
	}
	
	public List<AbstractNetconfRequest> getRequestListForTest() {
	    return m_requestListForTest;
	}

    private class TotalTime {
        private long m_totalTime;
        private long m_validationTime;
        private long m_dsTime;

        public void add(long totalTime, long validationTime, long dsTime) {
            m_totalTime += totalTime;
            m_validationTime += validationTime;
            m_dsTime += dsTime;
        }

        public long getTotalTime() {
            return m_totalTime;
        }

        public long getValidationTime() {
            return m_validationTime;
        }

        public long getDsTime() {
            return m_dsTime;
        }
    }
}

