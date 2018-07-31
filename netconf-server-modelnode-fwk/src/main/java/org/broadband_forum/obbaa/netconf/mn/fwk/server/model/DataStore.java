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

import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationUtil
        .isPostEditValidationSupported;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.ParserConfigurationException;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.PersistenceException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.DataStoreValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.DynamicDataStoreValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.NetconfRpcErrorUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.ActionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigTestOptions;
import org.broadband_forum.obbaa.netconf.api.messages.EditInfo;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfConfigChangeNotification;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorSeverity;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorType;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.server.NetconfQueryParams;
import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationService;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;

import com.google.common.annotations.VisibleForTesting;

import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.LoggerFactory;

public class DataStore {

    public static final int NO_LOCK = -1;
    private static final AdvancedLogger LOGGER = LoggerFactory.getLogger(DataStore.class, "netconf-server-datastore",
            "DEBUG", "GLOBAL");
    private int m_lockOwner = NO_LOCK;
    private boolean m_confirmedCommitPending = false;
    private boolean m_hasUncommitedChanges = false;

    private RootModelNodeAggregator m_rootModelNodeAggregator;
    private String m_name;
    private NotificationExecutor m_editNotificationExecutor;
    private SubSystemRegistry m_subSystemRegistry;
    private NotificationService m_notificationService;
    private DataStoreValidator m_validator;
    private DynamicDataStoreValidator m_dynamicValidator;
    private NamespaceContext m_namespaceContext;
    private NbiNotificationHelper m_nbiNotificationHelper;


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

    @Deprecated
    public DataStore(String name, RootModelNodeAggregator root, SubSystemRegistry subSystemRegistry) {
        m_name = name;
        m_rootModelNodeAggregator = root;
        m_editNotificationExecutor = new SubsystemNotificationExecutor();
        m_subSystemRegistry = subSystemRegistry;
    }

    public DataStore(String name, RootModelNodeAggregator root, SubSystemRegistry subSystemRegistry,
                     DataStoreValidator validator) {
        m_name = name;
        m_rootModelNodeAggregator = root;
        m_editNotificationExecutor = new SubsystemNotificationExecutor();
        m_subSystemRegistry = subSystemRegistry;
        m_validator = validator;
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
    }

    public void unlock(int sessionId) throws UnlockFailedNoLockActiveException, UnlockFailedOtherOwnerException {
        if (m_lockOwner == NO_LOCK) {
            throw new UnlockFailedNoLockActiveException();
        } else if (m_lockOwner == sessionId) {
            m_lockOwner = NO_LOCK;
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

    public void copyFrom(int sessionId, DataStore source) throws LockedByOtherSessionException, CopyConfigException {
        if (m_lockOwner != NO_LOCK && m_lockOwner != sessionId) {
            throw new LockedByOtherSessionException(m_lockOwner);
        }
        try {
            m_rootModelNodeAggregator.copyConfig(source.getConfig(DocumentUtils.getNewDocument(), null,
                    NetconfQueryParams.NO_PARAMS));
            List<ChangeNotification> copyNotification = new ArrayList<ChangeNotification>();
            for (ModelNode rootNode : m_rootModelNodeAggregator.getModelServiceRoots()) {
                copyNotification.add(new CopyConfigChangeNotification(source.getName(), this.getName(), null,
                        rootNode));
            }
            //FIXME: FNMS-833 provide a way to deploy a subsystem for root node
            //m_subSystemRegistry.lookupSubsystem(m_rootModelNodeAggregator.getClass().getName()).notifyChanged
            // (copyNotification);
        } catch (ParserConfigurationException e) {
            LOGGER.error("Copy config failed ", e);
            throw new CopyConfigException(NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.OPERATION_FAILED,
                    e.getMessage()));
        } catch (GetException e) {
            LOGGER.error("Error while getting ModelNodeId ", e);
            throw new CopyConfigException(e.getRpcError());
        }

    }

    public void copyFrom(int sessionId, Element sourceConfigElement) throws LockedByOtherSessionException,
            CopyConfigException {
        if (m_lockOwner != NO_LOCK && m_lockOwner != sessionId) {
            throw new LockedByOtherSessionException(m_lockOwner);
        }
        m_rootModelNodeAggregator.copyConfig(DocumentUtils.getChildElements(sourceConfigElement));
        List<ChangeNotification> copyNotification = new ArrayList<ChangeNotification>();
        for (ModelNode rootNode : m_rootModelNodeAggregator.getModelServiceRoots()) {
            copyNotification.add(new CopyConfigChangeNotification(null, this.getName(), sourceConfigElement, rootNode));
        }

        //FIXME:FNMS-833  provide a way to deploy a subsystem for root node
        //m_subSystemRegistry.lookupSubsystem(m_rootModelNodeAggregator.getClass().getName()).notifyChanged
        // (copyNotification);
    }

    public String getName() {
        return m_name;
    }

    public List<Element> getConfig(Document doc, FilterNode root, NetconfQueryParams params) throws GetException {
        return m_rootModelNodeAggregator.getConfig(new GetConfigContext(doc, root), params);
    }

    public List<Element> get(Document doc, FilterNode root, NetconfQueryParams params) throws GetException {
        return m_rootModelNodeAggregator.get(new GetContext(doc, root, new StateAttributeGetContext()), params);
    }

    public List<Element> action(Document doc, ActionRequest actionRequest, NetconfClientInfo clientInfo) throws
            ActionException {
        return m_rootModelNodeAggregator.action(actionRequest);
    }

    @Transactional(value = TxType.REQUIRED, rollbackOn = {EditConfigException.class, RuntimeException.class,
            Exception.class})
    public List<Notification> edit(EditConfigRequest request, NetconfClientInfo clientInfo) throws
            EditConfigException, EditConfigTestFailedException,
            PersistenceException, LockedByOtherSessionException {
        boolean isLogDebugEnabled = LOGGER.isDebugEnabled();

        int sessionId = 0;
        long editTime = System.currentTimeMillis();
        long validationTime = 0l;
        long dsTime = 0l;
        if (clientInfo == null) {
            sessionId = DataStore.NO_LOCK;
        } else {
            sessionId = clientInfo.getSessionId();
        }
        if (m_lockOwner != NO_LOCK && m_lockOwner != sessionId) {
            throw new LockedByOtherSessionException(m_lockOwner);
        }

        //Currently can work with set only
        if (!request.getTestOption().equals(EditConfigTestOptions.SET)) {
            throw new EditConfigException(NetconfRpcErrorUtil.getNetconfRpcError(NetconfRpcErrorTag
                            .OPERATION_NOT_SUPPORTED,
                    NetconfRpcErrorType.Protocol,
                    NetconfRpcErrorSeverity.Error,
                    "Can do only test-option set only"));
        }

        //save the messageid and sessionid, to regroup the changes later.
        NotificationContext notificationContext = new NotificationContext();

        List<EditContainmentNode> editTrees = m_rootModelNodeAggregator.editConfig(request, notificationContext);
        List<Notification> impliedChangesFromPostEditValidation = null;
        dsTime = System.currentTimeMillis() - editTime;

        if (isPostEditValidationSupported()) {
            for (EditContainmentNode editTree : editTrees) {
                if (isLogDebugEnabled) {
                    LOGGER.debug("performing post edit-config data store validation on root node {}", editTree
                            .getQName());
                }
                impliedChangesFromPostEditValidation = validateDataStore(editTree, request, clientInfo);
            }
            validateDataStoreDynamic(editTrees, request, clientInfo);
            validationTime = System.currentTimeMillis() - dsTime - editTime;
        } else {
            LOGGER.debug("post edit-config data store validation skipped");
        }
        Map<SubSystem, List<ChangeNotification>> subSystemNotificationMap = m_editNotificationExecutor
                .getSubSystemNotificationMap(m_name, notificationContext.getNotificationInfos(), request);

        List<Notification> editConfigChangeNotifications = m_nbiNotificationHelper
                .getNetconfConfigChangeNotifications(subSystemNotificationMap, clientInfo, m_namespaceContext);
        LOGGER.debug("sending edit-config change notification to subsystems");
        m_editNotificationExecutor.sendNotifications(subSystemNotificationMap);
        if (request.isUploadToPmaRequest()) {
            for (Notification notification : editConfigChangeNotifications) {
                ((SubsystemNotificationExecutor) m_editNotificationExecutor)
                        .refineNetconfConfigChangeNotification((NetconfConfigChangeNotification) notification);
            }
        }
        long totalTime = System.currentTimeMillis() - editTime;
        LOGGER.info("edit-config took {} ms in which validation took {} ms  and changes to datastore took {} ms",
                totalTime, validationTime, dsTime);
        if (isLogDebugEnabled) {
            LOGGER.debug("edit-config took {} ms in which validation took {} ms  and changes to datastore took {} ms " +
                            "for {}",
                    totalTime, validationTime, dsTime, editTrees);
        }
        if (impliedChangesFromPostEditValidation != null) {
            for (Notification notification : impliedChangesFromPostEditValidation) {
                if (notification instanceof NetconfConfigChangeNotification) {
                    List<EditInfo> editList = ((NetconfConfigChangeNotification) notification).getEditList();
                    for (EditInfo editInfo : editList) {
                        editInfo.setImplied(true);
                    }
                }
            }
            editConfigChangeNotifications.addAll(impliedChangesFromPostEditValidation);
        }
        return editConfigChangeNotifications;

    }

    /**
     * for UT Only
     */
    @VisibleForTesting
    protected void validateDataStores(EditContainmentNode editTree, EditConfigRequest request, NetconfClientInfo
            clientInfo) throws EditConfigException {
        validateDataStore(editTree, request, clientInfo);
    }

    private List<Notification> validateDataStore(EditContainmentNode editTree, EditConfigRequest request,
                                                 NetconfClientInfo clientInfo) throws EditConfigException {
        try {
            if (m_validator != null) {
                EditContainmentNode.setParentForEditContainmentNode(editTree, null);
                return m_validator.validate(m_rootModelNodeAggregator, editTree, request, clientInfo);
            } else {
                LOGGER.warn("DS Validator is null. Post-edit config validation will not happen for editTree {}",
                        editTree);
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

    private List<Notification> validateDataStoreDynamic(List<EditContainmentNode> editTrees, EditConfigRequest
            request, NetconfClientInfo clientInfo) throws EditConfigException {
        try {
            if (m_dynamicValidator != null) {
                return m_dynamicValidator.validate(m_rootModelNodeAggregator, editTrees, request, clientInfo);
            } else {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("DS m_dynamicValidator is null. Post-edit config dynamic validation will not happen " +
                            "for editTree {}", editTrees);
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
}
