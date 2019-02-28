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

package org.broadband_forum.obbaa.netconf.server.model.notification;

import static org.broadband_forum.obbaa.netconf.server.ResponseChannelUtil.sendResponse;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;

import org.broadband_forum.obbaa.netconf.server.model.notification.rpchandlers.CreateSubscriptionRpcHandlerImpl;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfFilter;

import org.apache.commons.jxpath.JXPathContext;
import org.broadband_forum.obbaa.netconf.api.utils.SystemPropertyUtils;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.joda.time.DateTime;
import org.opendaylight.yangtools.yang.common.QName;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientSession;
import org.broadband_forum.obbaa.netconf.api.client.NotificationListener;
import org.broadband_forum.obbaa.netconf.api.logger.NetconfLogger;
import org.broadband_forum.obbaa.netconf.api.messages.CreateSubscriptionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer;
import org.broadband_forum.obbaa.netconf.api.messages.GetRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorInfo;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorSeverity;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorType;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.server.ResponseChannel;
import org.broadband_forum.obbaa.netconf.api.server.notification.NetconfConfigChangeNotificationJMX;
import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationCallBackInfo;
import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationService;
import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationStream;
import org.broadband_forum.obbaa.netconf.api.server.notification.Stream;
import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationContext;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.YangModelUtility;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.NetconfRpcErrorUtil;
import org.broadband_forum.obbaa.netconf.stack.NcNotificationCounterService;

public class NotificationServiceImpl extends StandardMBean implements NotificationService, NetconfConfigChangeNotificationJMX {

    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(CreateSubscriptionRpcHandlerImpl.class, LogAppNames
            .NETCONF_NOTIFICATION);
	
    private static final List<String> REQUIRED_NOTIFICATION_CAPS = Arrays.asList(NetconfResources.NETCONF_NOTIFICATION,
            NetconfResources.NOTIFICATION_INTERLEAVE);
    private static final String THE_EPOCH_TIME = NetconfResources.DATE_TIME_WITH_TZ_WITHOUT_MS.print(new DateTime(
            "1970-01-01T00:00:00+00:00"));
    public static final String SUBSCR_START_OFFSET_SECS = "SUBSCR_START_OFFSET_SECS";
    public static final String NO_OFFSET = "NO_OFFSET";
    public static final Pattern OFFSET_PATTERN = Pattern.compile("^(-?)(\\d+$)");
    public static final String EPOCH = "EPOCH";
    public static final String NULL = "NULL";
    private final Executor m_notifExecutor;
    /**
     * ietf-yang-types.yang: typedef date-and-time { type string { pattern
     * '\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(\.\d+)?(Z|[\+\-]\d{2}:\d{2})'; }
     */

    private List<Stream> m_streamList;
    private Map<String, NotificationStream> m_supportedStreams;
    private NotificationLogger m_notificationLogger;
    private boolean m_supportNetconfConfigChangetNotification = true;
    private List<NotificationCallBackInfo> m_notificationCallBacks;
    private Set<Object> m_synchronizingIds;
    private int m_thresholdNotification = 10;
    private NetconfLogger m_netconfLogger;
    private NcNotificationCounterService m_notificationsCounterInterceptor;

    public NotificationServiceImpl(List<Stream> streamList, NotificationLogger notificationLogger,
                                   TimerManager timerMgr, Executor notifExecutor, NetconfLogger netconfLogger, NcNotificationCounterService ncNotificationCounterService)
            throws NotCompliantMBeanException {
        super(NetconfConfigChangeNotificationJMX.class);
        m_streamList = streamList;
        m_notificationLogger = notificationLogger;
        m_notifExecutor = notifExecutor;
        m_notificationCallBacks = new CopyOnWriteArrayList<>();
        m_synchronizingIds = new HashSet<Object>();
        m_supportedStreams = new HashMap<String, NotificationStream>();
        m_notificationsCounterInterceptor =  ncNotificationCounterService;
        m_netconfLogger = netconfLogger;
        for (Stream stream : m_streamList) {
            m_supportedStreams.put(stream.getName(), new NotificationStreamImpl(stream, m_notificationLogger, timerMgr, m_notifExecutor, m_notificationsCounterInterceptor));
        }
    }

    @Override
    public NotificationStream getDefaultNotificationStream() {
        return m_supportedStreams.get(NetconfResources.NETCONF);
    }

    @Override
    public NotificationStream getNotificationStream(String streamName) {
        if (streamName != null && !streamName.isEmpty()) {
            return m_supportedStreams.get(streamName);
        }
        return null;
    }

    public List<Stream> getSupportedStreams() {
        return m_streamList;
    }

    @Override
    public void closeSubscription(NetconfClientInfo clientInfo) {
        for (NotificationStream notificationStream : m_supportedStreams.values()) {
            if (notificationStream.isActiveSubscription(clientInfo)) {
                notificationStream.closeSubscription(clientInfo);
                break;
            }
        }
    }

    @Override
    public void createSubscription(NetconfClientInfo clientInfo, CreateSubscriptionRequest subscriptionRequest,
            ResponseChannel responseChannel) {
        if (clientInfo != null && subscriptionRequest != null) {
            // check there is already active session, if so return rpc error.
            if (isActiveSubscription(clientInfo)) {
                NetConfResponse response = new NetConfResponse();
                response.setMessageId(subscriptionRequest.getMessageId());
                setAlreadyActiveSessionError(clientInfo, response);
                sendResponse(responseChannel, subscriptionRequest, response);
                return;
            }

            String stream = subscriptionRequest.getStream();
            NotificationStream notificationStream = getNotificationStream(stream);

            LOGGER.debug(null, "Create subscription with notificationStream {}", notificationStream);
            if (notificationStream == null) {
                NetConfResponse response = new NetConfResponse();
                response.setMessageId(subscriptionRequest.getMessageId());
                setInvalidStreamError(stream, response);
                sendResponse(responseChannel, subscriptionRequest, response);
                return;
            } else {
                notificationStream.createSubscription(clientInfo, subscriptionRequest, responseChannel);
            }
        }
    }

    @Override
    public void sendNotification(String streamName, Notification notification) {
        // check stream name is valid and supported
        checkStreamSupported(streamName);

        if (!streamName.equals(NetconfResources.NETCONF)) {
            // broadcast notification to the specific stream
            NotificationStream notificationStream = getNotificationStream(streamName);
            if (notificationStream != null) {
                if (NetconfResources.CONFIG_CHANGE_STREAM.equals(streamName)) {
                    broadcastIfConfigChangeNotificationIsSupported(notification, notificationStream);
                } else {
                    broadcastAndLogNotification(streamName, notification, notificationStream);
                }
            }
        }
        // always broadcast notification to default stream
        NotificationStream defaultStream = getDefaultNotificationStream();
        m_netconfLogger.logNotificationOut(NetconfResources.NETCONF, notification);
        defaultStream.broadcastNotification(notification);
        if (m_notificationLogger.isLogSupportedByStream(NetconfResources.NETCONF)) {
            m_notificationLogger.logNotification(DateTime.now(), notification, NetconfResources.NETCONF);
        }
    }

    private void broadcastAndLogNotification(String streamName, Notification notification, NotificationStream notificationStream) {
        notificationStream.broadcastNotification(notification);
        if (m_notificationLogger.isLogSupportedByStream(streamName)) {
            m_notificationLogger.logNotification(DateTime.now(), notification, streamName);
        }
    }

    private void broadcastIfConfigChangeNotificationIsSupported(Notification notification, NotificationStream notificationStream) {
        if (m_supportNetconfConfigChangetNotification) {
            broadcastAndLogNotification(NetconfResources.CONFIG_CHANGE_STREAM, notification, notificationStream);
        }
    }

    @Override
    public boolean isActiveSubscription(NetconfClientInfo clientInfo) {
        for (NotificationStream notificationStream : m_supportedStreams.values()) {
            if (notificationStream.isActiveSubscription(clientInfo)) {
                LOGGER.debug(null, "Client {} has is an active subscription", clientInfo);
                return true;
            }
        }
        LOGGER.debug(null, "Client {} does not have an active subscription", clientInfo);
        return false;
    }

    /**
     * @param clientInfo
     * @param response
     */
    private void setAlreadyActiveSessionError(NetconfClientInfo clientInfo, NetConfResponse response) {
        String errorMessage = "another subscription is already active on this netconf session";
        LOGGER.error(null, errorMessage + ": " + clientInfo);
        response.setOk(false);
        NetconfRpcError rpcError = NetconfRpcErrorUtil.getNetconfRpcError(NetconfRpcErrorTag.OPERATION_FAILED,
                NetconfRpcErrorType.Protocol, NetconfRpcErrorSeverity.Error, errorMessage);
        response.addError(rpcError);
    }

    private void setInvalidStreamError(String streamName, NetConfResponse response) {
        String errorMessage = "notification stream specified is not found.";
        LOGGER.error(null, "notification stream specified is not found : {}", streamName);
        response.setOk(false);
        NetconfRpcError rpcError = NetconfRpcErrorUtil.getNetconfRpcError(NetconfRpcErrorTag.BAD_ELEMENT, NetconfRpcErrorType.Protocol,
                NetconfRpcErrorSeverity.Error, errorMessage);
        rpcError.addErrorInfoElement(NetconfRpcErrorInfo.BadElement, "stream");
        response.addError(rpcError);
    }

    private void checkStreamSupported(String streamName) {
        if (streamName == null) {
            throw new IllegalArgumentException("null is not valid stream name");
        } else if (streamName.isEmpty()) {
            throw new IllegalArgumentException("empty is valid stream name");
        } else if (getNotificationStream(streamName) == null) {
            throw new IllegalArgumentException("stream: " + streamName + "is not found");
        }
    }

    @Override
    public void supportNetconfConfigChangeNotification(boolean isSupport) {
        m_supportNetconfConfigChangetNotification = isSupport;
    }

    @Override
    public boolean isNetconfConfigChangeNotificationSupported() {
        return m_supportNetconfConfigChangetNotification;
    }

    @Override
    public void registerCallBack(List<NotificationCallBackInfo> callBackInfos) {
        m_notificationCallBacks.addAll(callBackInfos);
    }

    @Override
    public void unregisterCallBack(Set<QName> notificationTypes) {
        List<NotificationCallBackInfo> callbackInfoList = new ArrayList<>();
        callbackInfoList.addAll(m_notificationCallBacks);
    	for (NotificationCallBackInfo callBackInfo : callbackInfoList) {
    		Set<QName> callbackNotificationTypes = callBackInfo.getNotificationTypes();
    		if(callbackNotificationTypes.equals(notificationTypes)) {
    			m_notificationCallBacks.remove(callBackInfo);
    		}
    	}
    }
    
    @Override
    public void executeCallBack(Notification notification, NotificationContext context) {
        DateTime receivedTime = new DateTime();
        QName notificationType = notification.getType();
        for (NotificationCallBackInfo callBackInfo : m_notificationCallBacks) {
            Set<QName> notificationTypes = callBackInfo.getNotificationTypes();
            if (notificationTypes.isEmpty() || notificationTypes.contains(notificationType)) {
                // Execute call back for empty registered Notification Types
                // or matched Type
                LOGGER.debug(null, "The callBack {} is executing", callBackInfo.getCallBack());
				if (callBackInfo.getNotificationApplicableCheck() != null) {
					if (callBackInfo.getNotificationApplicableCheck().isApplicable(context)) {
						callBackInfo.getCallBack().onNotificationReceived(notification, context, receivedTime);
					}
				} else {
					callBackInfo.getCallBack().onNotificationReceived(notification, context, receivedTime);
				}
            }
        }
    }
    
    public boolean isNotificationCallbackRegistered(QName qname, String moduleCapString){
        for (NotificationCallBackInfo callBackInfo : m_notificationCallBacks) {
            Set<QName> notificationTypes = callBackInfo.getNotificationTypes();
            Set<String> capabilityStrings = callBackInfo.getCapabilities();
            if (notificationTypes.contains(qname) && capabilityStrings.contains(moduleCapString)) {
            	return true;
            }
        }
        return false;
    }
    
    @Override
    public NetConfResponse createSubscriptionWithCallback(NetconfClientSession clientSession, String timeOfLastSentEvent,
            NotificationListener subscriber, Object synchronizedId) {
       return createSubscriptionWithCallback(clientSession, timeOfLastSentEvent, subscriber, synchronizedId, true);
    }
    
    @Override
    public NetConfResponse createSubscriptionWithCallback(NetconfClientSession clientSession, String timeOfLastSentEvent,
            NotificationListener subscriber, Object synchronizedId, boolean isReplaySupported) {
        if (isReplaySupported) {
            if (timeOfLastSentEvent.equals(THE_EPOCH_TIME) || !isReplayPossible(clientSession, timeOfLastSentEvent)) {
                String currentDateTime = getCurrentDatetimeFromDevice(clientSession, synchronizedId);
                return fullResynchronizeHandler(clientSession, subscriber, synchronizedId, currentDateTime);
            }
            return createSubscription(clientSession, subscriber, timeOfLastSentEvent, synchronizedId);

        }
        return createSubscription(clientSession, subscriber, null,synchronizedId);
    }

    private GetRequest getCurrentDateTimeRequest() {
        Document document = DocumentUtils.createDocument();
        Element systemState = document.createElementNS(NetconfResources.SYSTEM_STATE_NS, NetconfResources.SYSTEM_STATE);
        Element clock = document.createElement(NetconfResources.CLOCK);
        Element currentDatetime = document.createElement(NetconfResources.CURRENT_DATE_TIME);
        clock.appendChild(currentDatetime);
        systemState.appendChild(clock);
        document.appendChild(systemState);
        GetRequest request = new GetRequest();
        NetconfFilter requestFilter = new NetconfFilter();
        requestFilter.setType(NetconfResources.SUBTREE_FILTER);
        requestFilter.addXmlFilter(document.getDocumentElement());
        request.setFilter(requestFilter);
        request.setMessageId("1");
        request.setReplyTimeout(10000);
        return request;
    }

    protected String getCurrentDatetimeFromDevice(NetconfClientSession clientSession, Object deviceRefId) {
        try {
            GetRequest getRequest = getCurrentDateTimeRequest();
            Future<NetConfResponse> netconfResponse = clientSession.get(getRequest);
            if (netconfResponse != null) {
                NetConfResponse response = netconfResponse.get();
                if (response != null) {
                    List<Element> dataContent = response.getDataContent();
                    if(dataContent.size() > 0) {
                        Element element = dataContent.get(0);
                        Element currentTime = DocumentUtils.getDescendant(element, NetconfResources.CURRENT_DATE_TIME, NetconfResources.SYSTEM_STATE_NAMESPACE);
                        if (currentTime != null) {
                            return currentTime.getTextContent();
                        }
                    }
                } else {
                    LOGGER.debug(null, "Could not get data from device {}, response is {}", deviceRefId.toString(), response.responseToString());
                }
            }
        } catch (Exception e) {
            LOGGER.error(null, "Error while get current-datetime from device {}", deviceRefId.toString(), e);
        }
        return null;
    }

    @Override
    public boolean isNotificationSupported(NetconfClientSession clientSession) {
        boolean result = false;
        if (clientSession != null) {
            Set<String> serverCapability = clientSession.getServerCapabilities();
            boolean isDeviceNotificationSupported = YangModelUtility.handleCheckServerCapabilityContainsAll(serverCapability,
                    REQUIRED_NOTIFICATION_CAPS);
            result = isDeviceNotificationSupported;
        } else {
            LOGGER.error(null, "Error while checking if notification is supported since session is null");
        }
        return result;
    }

    /*
     * Checking replaySupport for NETCONF stream is supported or not
     */
    @Override
    public boolean isReplaySupported(NetconfClientSession clientSession) throws NetconfMessageBuilderException, InterruptedException,
            ExecutionException {
    	 NetConfResponse response = getResponseFromClient(clientSession);
         if (response != null) {
             Document responseDocument = response.getResponseDocument();
             JXPathContext context = JXPathContext.newContext(responseDocument);
             String value = getValueFromReplayResponse("replaySupport", context);
             LOGGER.debug(null, "The value of replay support: {}", value);
             if (value != null) {
                 return Boolean.valueOf(value);

             }
         }
         return false;
    }

    NetConfResponse fullResynchronizeHandler(NetconfClientSession clientSession, NotificationListener subscriber,
                                                     Object synchronizedId, String startSyncTime) {
        LOGGER.debug(null, "The object: {} is executing full resynchronize in client session {}", synchronizedId, clientSession);
        boolean isSynchronizationSuccess = false;
        try {
            m_synchronizingIds.add(synchronizedId);
            Set<String> serverCapability = clientSession.getServerCapabilities();
                for (NotificationCallBackInfo callBackInfo : m_notificationCallBacks) {
                    List<String> checkedCapabilities = new ArrayList<String>(callBackInfo.getCapabilities());
                    // need to check NETCONF capabilities, Execute if call back
                    // info stored empty capabilities or match NETCONF's
                    // capabilities
                    if (callBackInfo.getCapabilities().isEmpty()
                            || YangModelUtility.handleCheckServerCapabilityContainsAll(serverCapability, checkedCapabilities)) {
                        callBackInfo.getCallBack().resynchronize(synchronizedId);
                    }
            }
            isSynchronizationSuccess = true;
        } catch (Exception e) {
            LOGGER.error(null, "Error in reSynchronization call back : {}", e);
            throw e;
        } finally {
            // Make sure that device is removed from keeping set after
            // synchronization for
            // updating time-of-last-sent-event action can be executed
            m_synchronizingIds.remove(synchronizedId);
        }
        startSyncTime = getOffsetTime(startSyncTime);
        if (isSynchronizationSuccess) {
            String startTime = null;
            if(startSyncTime != null) {
                startTime = startSyncTime;
            }

            return createSubscription(clientSession, subscriber, startTime, synchronizedId);
        } else {
            LOGGER.error(null, "Re sychronization to application failed");
        }
        return null;
    }

    private String getOffsetTime(String startSyncTime) {
        String offset = SystemPropertyUtils.getInstance().getFromEnvOrSysProperty(SUBSCR_START_OFFSET_SECS, NO_OFFSET);
        if(NO_OFFSET.equals(offset)){
            return startSyncTime;
        }
        if(EPOCH.equals(offset)){
            return new DateTime(THE_EPOCH_TIME).toString();
        }
        if(NULL.equals(offset)){
            return null;
        }
        if (startSyncTime != null) {
            DateTime dateTime = new DateTime(startSyncTime);
            Matcher matcher = OFFSET_PATTERN.matcher(offset);
            if(matcher.matches()){
                String sign = matcher.group(1);
                int scalar = 1;
                if("-".equals(sign)){
                    scalar = -1;
                }
                long millis = TimeUnit.SECONDS.toMillis(Long.valueOf(matcher.group(2)));
                return dateTime.withDurationAdded(millis, scalar).toString();
            }
        }
        return startSyncTime;
    }

    private NetConfResponse createSubscription(NetconfClientSession clientSession, NotificationListener subscriber, String startTime, Object id) {
        try {
            CreateSubscriptionRequest request = new CreateSubscriptionRequest();
            request.setStream(NetconfResources.NETCONF);
            LOGGER.info(null, "Sending subscription for {} with startTime: {}, request is {}", id, startTime, request.requestToString());
            if (startTime != null) {
                request.setStartTime(startTime);
            }
            Future<NetConfResponse> futureResponse = clientSession.createSubscription(request, subscriber);
            if (futureResponse != null) {
                NetConfResponse response = futureResponse.get();
                if(response !=null ){
                    if (response.isOk()) {
                        // Check replay is possible when receiving response success
                        // from device
                    /*if (!isReplayPossible(clientSession, startTime)) {
                        LOGGER.info("Replay is not possible. Close session");
                        // Close session when replay is impossible
                        CloseSessionRequest closeRequest = new CloseSessionRequest();
                        clientSession.closeSession(closeRequest);
                    } else {
                        LOGGER.info("Send subscription successfully");
                    }*/
                        LOGGER.info(null, "Create subscription for {} successful", id);
                    } else {
                        // Device return subscription failed, Raise alarm
                        LOGGER.info(null, "Create subscription for {} failed with response {}", id, response);
                    }
                    return response;

                }
            } else {
                LOGGER.error(null, "Can't receive any response when sending subscription for {}", id);
            }
        } catch (NetconfMessageBuilderException e) {
            LOGGER.error(null, "Create subscription for "+id+" is failed. {}", e);
        } catch (InterruptedException e) {
            LOGGER.error(null, "Create subscription for "+id+" is failed. {}", e);
        } catch (ExecutionException e) {
            LOGGER.error(null, "Create subscription for "+id+" is failed. {}", e);
        } catch (ParseException e) {
            LOGGER.error(null, "Create subscription for "+id+" is failed. {}", e);
        }
        return null;
    }

    public boolean isReplayPossible(NetconfClientSession clientSession, String startTime) {
    	boolean result = false;
        try {
            NetConfResponse response = getResponseFromClient(clientSession);
            if (response != null) {
                Document responseDocument = response.getResponseDocument();
                JXPathContext context = JXPathContext.newContext(responseDocument);
                String replaySupportStr = getValueFromReplayResponse("replaySupport", context);
                if (replaySupportStr == null || !Boolean.valueOf(replaySupportStr)) {
                    // Replay support is false return this replay is not
                    LOGGER.debug(null, "Replay possible is {} when replaySupport: {}", result, replaySupportStr);
                    // possible
                    return result;
                }
                String replayLogAgedTime = getValueFromReplayResponse("replayLogAgedTime", context);
                String replayLogCreationTime = getValueFromReplayResponse("replayLogCreationTime", context);
                LOGGER.debug(null, "Checking replay is possible with startTime: {}, replayLogCreationTime: {}, replayLogAgedTime: {}",
                        startTime, replayLogCreationTime, replayLogAgedTime);
                DateTime startDateTime = NetconfResources.parseDateTime(startTime);
                if (replayLogCreationTime != null) {
                    DateTime replayLogCreationDateTime = NetconfResources.parseDateTime(replayLogCreationTime);
                    result = startDateTime.isAfter(replayLogCreationDateTime);
                    //if startTime after replayLogCreationTime, then also check startTime is after replayLogAgedTime 
                    if (result && replayLogAgedTime != null) {
                        DateTime replayLogAgedDateTime = NetconfResources.parseDateTime(replayLogAgedTime);
                        result = startDateTime.isAfter(replayLogAgedDateTime);
                    } 
                } else {
                    LOGGER.error(null, "replayLogAgedTime and replayLogCreationTime don't have value");
                }
            }
        } catch (NetconfMessageBuilderException | InterruptedException | ExecutionException e) {
            LOGGER.error(null, "Error when checking If replay is possible {}", e);
        }
        LOGGER.debug(null, "Replay possible is {} with startTime: {}", result, startTime);
        return result;
    }
    
    private NetConfResponse getResponseFromClient(NetconfClientSession clientSession)
            throws NetconfMessageBuilderException, InterruptedException, ExecutionException {
        NetConfResponse response = null;
        String requestPath = "/getStreamsRequest.xml";
        Document requestDocument = DocumentUtils.loadXmlDocument(this.getClass().getResourceAsStream(requestPath));
        GetRequest getRequest = DocumentToPojoTransformer.getGet(requestDocument);
        Future<NetConfResponse> futureResponse = clientSession.get(getRequest);
        if (futureResponse != null) {
            response = futureResponse.get();
        }
        return response;
    }

    private String getValueFromReplayResponse(String nodeName, JXPathContext context) {
        String result = null;
        String localName = "*[local-name()";// Use 'local-name()' to avoid handling namespace
        Element selectNode = (Element) context.selectSingleNode("//" + localName + "='stream'][" + localName + "='name']='"
                + NetconfResources.NETCONF + "']/" + localName + "='" + nodeName + "']");
        if (selectNode != null) {
            result = selectNode.getTextContent();

        }
        return result;
    }

    @Override
    public void configThresholdForPersistTimeLastSentEvent(int counter) {
        m_thresholdNotification = counter;
    }

    @Override
    public int getThresholdForPersistTimeLastSentEvent() {
        return m_thresholdNotification;
    }

    @Override
    public boolean isSynchronizingId(Object synchronizedId) {
        return m_synchronizingIds.contains(synchronizedId);
    }

    @Override
	public List<NotificationCallBackInfo> getNotificationCallBacks() {
		return m_notificationCallBacks;
	}

	@Override
	public void unregisterCallBackInfo(NotificationCallBackInfo notificationCallBackInfo) {
		m_notificationCallBacks.remove(notificationCallBackInfo);
	}
}
