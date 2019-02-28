package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import static org.broadband_forum.obbaa.netconf.api.util.DocumentUtils.createDocument;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.sql.Timestamp;
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
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.RpcRequestConstraintParser;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.DataPathUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.PersistenceException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.NetconfRpcErrorUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.SubtreeFilterUtil;
import org.broadband_forum.obbaa.netconf.server.rpc.CreateSubscriptionRpcHandler;
import org.broadband_forum.obbaa.netconf.server.rpc.MultiRpcRequestHandler;
import org.broadband_forum.obbaa.netconf.server.rpc.RequestType;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcPayloadConstraintParser;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcProcessException;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcRequestHandler;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcValidationException;
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
        this(schemaRegistry, new RpcRequestConstraintParser(schemaRegistry, null, null));
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
		return sessionsMap!=null ? sessionsMap : Collections.emptyMap();
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
					target.copyFrom(client.getSessionId(), source);
				else
					target.copyFrom(client.getSessionId(), sourceConfigElement);
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
            netconfConfigChangeNotifications = store.edit(request, clientInfo);
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
			response.addError(NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.OPERATION_FAILED, 
					 ExceptionUtils.getStackTrace(e)));
			
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

		FilterNode root = getFilter(request.getFilter());
		DataStore store = m_dataStores.get(StandardDataStores.RUNNING);
		Document doc = createDocument();

		List<Element> elements;
		try {
			elements = store.get(doc, root, new NetconfQueryParams(request.getDepth(), request.isIncludeConfig(), request.getFieldValues()));
			
			if(elements != null && !elements.isEmpty()) {
				response.setDataContent(elements);
			}else{
				response.setDataContent(Collections.<Element>emptyList());
			}
			if(Boolean.valueOf(getFromEnvOrSysProperty(GET_FINAL_FILTER, "false"))){
				NetconfFilter filter = request.getFilter();
				if(filter != null){
					response.setData(m_filterUtil.filter(response.getData(),
							filter.getXmlFilter()));
				}
			}

		} catch (GetException e) {
			LOGGER.error("Error while evaluating filter ",e);
			response.addError(e.getRpcError());
		} catch (Exception e){
			LOGGER.error("Error while executing get",e);
			response.addError(NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.OPERATION_FAILED,
					"Error while executing get - "+e.getMessage()));
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
		FilterNode root = getFilter(request.getFilter());
		DataStore store = m_dataStores.get(request.getSource());
		Document doc = createDocument();
		List<Element> elements;
		try {
			elements = store.getConfig(doc, root, new NetconfQueryParams(request.getDepth(), true, request.getFieldValues()));
			
			if(elements != null && !elements.isEmpty()) {
				response.setDataContent(elements);
			}else{
				response.setDataContent(Collections.<Element>emptyList());
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
					LOGGER.error("Error occured while processing RPC", e);
					response.addErrors(e.getRpcErrors());
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
		DataStore store = m_dataStores.get(StandardDataStores.RUNNING);
		Document reqDoc = null;
		List<Element> elements = null;
		try {
			Element actionTreeElement = request.getActionTreeElement();
			Map<ActionDefinition, Element> matchedActionElements = retrieveMatchedActionElements(actionTreeElement);
			
			if(matchedActionElements.isEmpty()){
			    Pair<RpcDefinition, Element> matchedDeviceRpc = retrieveMatchedRpcElement(actionTreeElement);
			    if ( matchedDeviceRpc == null){
			        LOGGER.error("No matched action found on the model. Invalid request:{}", reqDoc);
	                response.addError(NetconfRpcErrorUtil.getNetconfRpcError(NetconfRpcErrorTag.BAD_ELEMENT,
	                          NetconfRpcErrorType.Protocol,
	                          NetconfRpcErrorSeverity.Error,
	                          "No matched action found on the models"));
	                return;			        
			    } else {
			        handleDeviceRpc(info, request, response, matchedDeviceRpc, actionTreeElement);
			    }
			} else if (matchedActionElements.size()>1){
				response.addError(NetconfRpcErrorUtil.getNetconfRpcError(NetconfRpcErrorTag.BAD_ELEMENT,
						  NetconfRpcErrorType.Protocol,
						  NetconfRpcErrorSeverity.Error,
						  "Multiple action element exists within RPC"));
				return;	
			} else {
				try {
					Map.Entry<ActionDefinition,Element> entry=matchedActionElements.entrySet().iterator().next();
					ActionDefinition actionDef = entry.getKey();
					request.setActionTargetpath(actionDef.getPath().getParent());
					request.setActionQName(actionDef.getQName());
					reqDoc = request.getRequestDocument();
					m_rpcConstraintParser.validate(request, RequestType.ACTION);
					elements = store.action(reqDoc, request, info);
					response.setActionDefinition(actionDef);
					
					if(elements != null && !elements.isEmpty()) {
						ContainerSchemaNode output = actionDef.getOutput();
						response.setActionOutputElements(elements);
						boolean isError = isError(elements);
						/**
						 *  If output is not defined in action, then action-response should be "<rpc-reply> <ok/> </rpc-reply>"
						 *  Hence dont need to validtion the output such case
						 */
						if(null != output && output.getChildNodes() != null && !output.getChildNodes().isEmpty() && !isError){
							m_rpcConstraintParser.validate(response, RequestType.ACTION);
						}
					}else{
						response.setOk(true);
					}
				} catch (ValidationException e) {
					LOGGER.error("Action request failed to validate:{} ", request, e);
					response.addError(e.getRpcError());
					return;
				} catch (ActionException e) {
					LOGGER.error("error while executing action request: {}", reqDoc, e);
					List<NetconfRpcError> errors = e.getRpcErrors();
					for (NetconfRpcError error:errors) {
			            response.addError(error);
					}
					return;
				}
			}
		} catch (NetconfMessageBuilderException e) {
			LOGGER.error("Invalid request: {}", reqDoc, e);
			onInvalidRequest(info, reqDoc, response);
			return;
		}
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
            Pair<RpcDefinition, Element> rpcElement, Element treeElement) {
	    NetconfRpcRequest rpcRequest = new NetconfRpcRequest();
	    rpcRequest.setMessageId(actionRequest.getMessageId());
	    rpcRequest.setIsSchemaMountedRpc(true);
	    rpcRequest.setRpcInput(treeElement);
        NetconfRpcResponse rpcResponse = new NetconfRpcResponse();
        rpcRequest.setRpcContext(treeElement);
        rpcResponse.setRpcContext(treeElement);
        QName rpcName = rpcElement.getFirst().getQName();
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

    private Map<ActionDefinition, Element> retrieveMatchedActionElements(Element actionRequest) {
        SchemaRegistry schemaRegistry = getMountRegistryIfApplicable(actionRequest);
        Map<ActionDefinition, Element> matchedActionElements = new HashMap<>();
        matchedActionElements = fetchActionElements(actionRequest, schemaRegistry);
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
				matchedActionElements.put(actionDef, (Element)actionElement);
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
    	while ( dataPathLevels.hasNext()){
    		QName qName = dataPathLevels.next().getQName();
    		if (isRootNode){
    			isRootNode = false;
    			continue;
    		}
    		List<Element> childElements = DocumentUtils.getChildElements(element);  
    		boolean found = false;
    		for(Element node : childElements){
				if (isSameNode(node, qName)){
					element = node;
					found = true;
					break;
				}
    		}
    		if ( !found){
    			return null;
    		}
    	}
    	if ( isSameNode(element, actionQName)){
    		return element;
    	}
    	return null;
    }

	private boolean isSameNode(Element node, QName qName){
		if (node.getNamespaceURI().toString().equals(qName.getNamespace().toString()) &&
				node.getLocalName().equals(qName.getLocalName())) {
			return true;
		}
		return false;
	}

    public SchemaRegistry getMountRegistryIfApplicable( Element request){
    	SchemaRegistry schemaRegistry = m_schemaRegistry;
		MountProviderInfo info = SchemaRegistryUtil.getMountProviderInfo(request, m_schemaRegistry);
		if ( info != null){
			schemaRegistry = info.getMountedRegistry();
		}
		return schemaRegistry;
    }
	
	private Pair<RpcDefinition, Element> retrieveMatchedRpcElement(Element actionRequest){
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
                        if ( rpcDef.getQName().getNamespace().toString().equals(childNode.getNamespaceURI().toString()) &&
                                rpcDef.getQName().getLocalName().equals(childNode.getLocalName())){
                            return new Pair<>(rpcDef, (Element)mountProviderInfo.getMountedXmlNodeFromRequest());
                        }
                    }
                }
            }
        }
        return null;
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
