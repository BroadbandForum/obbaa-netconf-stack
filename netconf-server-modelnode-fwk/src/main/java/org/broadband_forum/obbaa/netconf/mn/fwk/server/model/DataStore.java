package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationUtil.isPostEditValidationSupported;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.PersistenceException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.DSValidationContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.DataStoreValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.DynamicDataStoreValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.utils.TxException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.utils.TxService;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.utils.TxTemplate;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.NetconfRpcErrorUtil;
import org.broadband_forum.obbaa.netconf.server.rpc.RequestType;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcPayloadConstraintParser;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.slf4j.helpers.MessageFormatter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.annotations.VisibleForTesting;

public class DataStore {
    
    private static final Long REQUEST_TIME_FORCE_PRINT_CUTOFF = 2000L;

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
        m_editNotificationExecutor = new SubsystemNotificationExecutor();
        m_validator = validator;
        m_txService = txService;
        m_netconfLogger = netconfLogger;
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
            m_rootModelNodeAggregator.copyConfig(source.getConfig(DocumentUtils.getNewDocument(), null, NetconfQueryParams.NO_PARAMS));
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

    public void copyFrom(int sessionId, Element sourceConfigElement) throws LockedByOtherSessionException, CopyConfigException {
        if (m_lockOwner != NO_LOCK && m_lockOwner != sessionId) {
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

    public List<Element> getConfig(Document doc, FilterNode root, NetconfQueryParams params) throws GetException {
        return m_rootModelNodeAggregator.getConfig(new GetConfigContext(doc, root), params);
    }

    public List<Element> get(Document doc, FilterNode root, NetconfQueryParams params) throws GetException {
        return m_rootModelNodeAggregator.get(new GetContext(doc, root, new StateAttributeGetContext()), params);
    }

    public List<Element> action(Document doc, ActionRequest actionRequest, NetconfClientInfo clientInfo) throws ActionException {
        return m_rootModelNodeAggregator.action(actionRequest);
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
	public Map<EditConfigRequest, NotificationContext> editInternal(EditConfigRequest request,
                                                                    NetconfClientInfo clientInfo) throws EditConfigException, EditConfigTestFailedException,
			PersistenceException, LockedByOtherSessionException, ValidationException {
		
		if (m_requestListForTest != null) {
		    m_requestListForTest.add(request);
		}

		int sessionId = 0;
        long startTime = System.currentTimeMillis();
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

		// Currently can work with set only
		if (!request.getTestOption().equals(EditConfigTestOptions.SET)) {
			throw new EditConfigException(NetconfRpcErrorUtil.getNetconfRpcError(
					NetconfRpcErrorTag.OPERATION_NOT_SUPPORTED, NetconfRpcErrorType.Protocol,
					NetconfRpcErrorSeverity.Error, "Can do only test-option set only"));
		}

		// save the messageid and sessionid, to regroup the changes later.
		NotificationContext notificationContext = new NotificationContext();
		DataStoreValidationUtil.getValidationContext().setRootNodeAggregator(m_rootModelNodeAggregator);
		List<EditContainmentNode> editTrees = m_rootModelNodeAggregator.editConfig(request, notificationContext);
		dsTime = System.currentTimeMillis() - startTime;
		List<EditConfigRequest> internalEditRequests = null;
		Map<EditConfigRequest, NotificationContext> editRequestNotificationContextMap = new LinkedHashMap<EditConfigRequest, NotificationContext>();
		editRequestNotificationContextMap.put(request, notificationContext);
		if (isPostEditValidationSupported()) {
			for (EditContainmentNode editTree : editTrees) {
				SchemaRegistryUtil.resetSchemaRegistryCache();
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("performing post edit-config data store validation on root node {}",
							editTree.getQName());
				}
				// Validate the data store and return validation edit request if any
				internalEditRequests = validateDataStore(editTree, request, clientInfo);
				if (internalEditRequests != null && !internalEditRequests.isEmpty()) {
					EditConfigRequest internalEditRequest = internalEditRequests.get(0);
					validateEditRequest(internalEditRequest);
					// Call editInternal() recursively if we have internal validation edit request
					if (m_netconfLogger != null) {
					    try {
					        m_netconfLogger.logRequest(clientInfo.getRemoteHost(), clientInfo.getRemotePort(), clientInfo.getUsername(), "Internal request for session " + clientInfo.getSessionId(), internalEditRequest.getRequestDocument());
					    } catch (NetconfMessageBuilderException e) {
					    }
					}
					Map<EditConfigRequest, NotificationContext> internalEditRequestMap = editInternal(internalEditRequest, internalEditRequest.getClientInfo());
					if (!internalEditRequestMap.isEmpty()) {
						editRequestNotificationContextMap.putAll(internalEditRequestMap);
					}
				}
			}
			validateDataStoreDynamic(editTrees, request, clientInfo);
			validationTime = System.currentTimeMillis() - dsTime - startTime;
		} else {
			LOGGER.debug("post edit-config data store validation skipped");
		}
		long totalTime = System.currentTimeMillis() - startTime;
        boolean forcePrint = totalTime > REQUEST_TIME_FORCE_PRINT_CUTOFF;
        if (LOGGER.isDebugEnabled() || forcePrint) {
            String timingString = format("edit-config took {} ms in which validation took {} ms and changes to datastore took {} ms",
                    totalTime, validationTime, dsTime);
            if (forcePrint) {
                LOGGER.warn(timingString);
            }
            else {
                LOGGER.debug(timingString);
            }
            LOGGER.debug("Edit trees: {}", LOGGER.sensitiveData(editTrees));
        }
        LOGGER.debug("Sending pre-commit notifications to subsystems");
        Map<SubSystem, List<ChangeNotification>> subSystemNotificationMap = m_editNotificationExecutor
                .getSubSystemNotificationMap(request.getTarget(), notificationContext.getNotificationInfos(), request);
        try {
            m_editNotificationExecutor.sendPreCommitNotifications(subSystemNotificationMap);
        } catch (SubSystemValidationException e) {
            LOGGER.error("Sending precommit notifications failed due to: ", e);
            throw new EditConfigException(e.getRpcError());
        }
        LOGGER.debug("Sending pre-commit notifications to subsystems done");
		return editRequestNotificationContextMap;
	}
	


	/**
	 * In this method, validate the editconfig request and it's internal edit requests recursively
	 * @param request
	 * @param clientInfo
	 * @return List of Notification
	 * @throws EditConfigException
	 * @throws EditConfigTestFailedException
	 * @throws PersistenceException
	 * @throws LockedByOtherSessionException
	 */
	public List<Notification> edit(EditConfigRequest request, NetconfClientInfo clientInfo) throws EditConfigException,
			EditConfigTestFailedException, PersistenceException, LockedByOtherSessionException {
		/**
		 * 1) Validating the datastore - Make a single transaction and editInternal() internally called recursively
		 * 2) Sending the notifications - After successful validation, send the notification to SBI and return the notifications
		 */
		Map<EditConfigRequest, NotificationContext> editRequestsMap = Collections.emptyMap();
		try {
			editRequestsMap = m_txService
				.executeWithTxRequired(new TxTemplate<Map<EditConfigRequest, NotificationContext>>() {
					@Override
					public Map<EditConfigRequest, NotificationContext> execute() throws TxException {
						try {
							// Validate the DataStore and return editrequest and it's corresponding NotificaitonContext
							return editInternal(request, clientInfo);
						} catch (EditConfigException | EditConfigTestFailedException | PersistenceException 
								| LockedByOtherSessionException | ValidationException e) {
							throw new TxException(e);
						}
					}
				});
		} catch (TxException e) {
			// Throw the actual exception to NetconfServerImpl#editConfig
			Throwable cause = e.getCause();
			checkInstanceAndRethrow(cause);
		}
		return sendNotification(request, editRequestsMap);
	}
	
	/**
	 * Send edit-config change notification to SBI after successful Datastore validation
	 * @param request
	 * @param editRequestsMap
	 * @return
	 */
	private List<Notification> sendNotification(EditConfigRequest request,
			Map<EditConfigRequest, NotificationContext> editRequestsMap) {

		List<Notification> changeNotifications = new ArrayList<Notification>();
		if (editRequestsMap != null && !editRequestsMap.isEmpty()) {
			for (Map.Entry<EditConfigRequest, NotificationContext> entry : editRequestsMap.entrySet()) {
				EditConfigRequest editRequest = entry.getKey();
				NotificationContext notificationContext = entry.getValue();
				Map<SubSystem, List<ChangeNotification>> subSystemNotificationMap = m_editNotificationExecutor
						.getSubSystemNotificationMap(m_name, notificationContext.getNotificationInfos(), editRequest);

				List<Notification> editConfigChangeNotifications = m_nbiNotificationHelper
						.getNetconfConfigChangeNotifications(subSystemNotificationMap, editRequest.getClientInfo(),
								m_namespaceContext);
				LOGGER.debug("sending edit-config change notification to subsystems");
				m_editNotificationExecutor.sendNotifications(subSystemNotificationMap);
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
        validateDataStore(editTree, request, clientInfo);
    }

    private List<EditConfigRequest> validateDataStore(EditContainmentNode editTree, EditConfigRequest request, NetconfClientInfo clientInfo) throws EditConfigException {
        try {
            if (m_validator != null) {
                EditContainmentNode.setParentForEditContainmentNode(editTree, null);
                return m_validator.validate(m_rootModelNodeAggregator, editTree, request, clientInfo);
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
        return DataStoreValidationUtil.getValidationContext().withRootNodeAggregator(new DSValidationContext.RootNodeAggregatorTemplate<T>() {
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
}

