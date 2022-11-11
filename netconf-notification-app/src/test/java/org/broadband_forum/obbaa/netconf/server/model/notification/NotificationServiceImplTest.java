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

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

import javax.management.NotCompliantMBeanException;
import javax.xml.parsers.ParserConfigurationException;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientSession;
import org.broadband_forum.obbaa.netconf.api.client.NetconfResponseFuture;
import org.broadband_forum.obbaa.netconf.api.client.NotificationListener;
import org.broadband_forum.obbaa.netconf.api.logger.DefaultNetconfLogger;
import org.broadband_forum.obbaa.netconf.api.messages.CreateSubscriptionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfNotification;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.server.ResponseChannel;
import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationCallBack;
import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationCallBackInfo;
import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationContext;
import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationStream;
import org.broadband_forum.obbaa.netconf.api.server.notification.Stream;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.server.model.notification.utils.NotificationConstants;
import org.broadband_forum.obbaa.netconf.stack.NcNotificationCounterService;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.opendaylight.yangtools.yang.common.QName;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class NotificationServiceImplTest {
    private List<Stream> m_streamList;
    private NotificationLogger m_notificationLogger;
    private TimerManager m_timerManager;
    private NotificationServiceImpl m_notificationService;
    private NcNotificationCounterService m_notificationsInterceptor;
    private Stream m_netconfStream;
    private ResponseChannel m_responseChannel;
    private NcNotificationCounterService m_nbiNotifInterceptor;
    private Executor notifExecutor;
    private static final String ALARM_NOTIFICATION = "alarm-notification";
    private static final String TEST_NS = "testNamespace";
    private static final String TEST_MODULE = "test-module";
    private static final String REVISION = "2015-07-14";
    private static final String CAP_STRING_FORMAT = "%s?module=%s&revision=%s";
    private static final QName m_alarmNotificationQName = QName.create(TEST_NS, ALARM_NOTIFICATION);
    private static final String m_capsString = String.format(CAP_STRING_FORMAT, TEST_NS, TEST_MODULE, REVISION);
    private NetconfResponseFuture m_futureResponse;

    @Before
    public void setup() throws NotCompliantMBeanException {
        m_netconfStream = mock(Stream.class);
        when(m_netconfStream.getName()).thenReturn(NetconfResources.NETCONF);
        when(m_netconfStream.getReplaySupport()).thenReturn(true);

        m_notificationsInterceptor = mock(NcNotificationCounterService.class);
        m_streamList = Collections.singletonList(m_netconfStream);
        m_notificationLogger = mock(NotificationLogger.class);
        when(m_notificationLogger.isLogSupportedByStream(NetconfResources.NETCONF)).thenReturn(true);
        m_timerManager = mock(TimerManager.class);
        notifExecutor = mock(Executor.class);
        m_nbiNotifInterceptor = mock(NcNotificationCounterService.class);
        m_responseChannel = mock(ResponseChannel.class);
        when(m_responseChannel.getCloseFuture()).thenReturn(new CompletableFuture<>());
        m_notificationService = new NotificationServiceImpl(m_streamList, m_notificationLogger, m_timerManager, notifExecutor, new DefaultNetconfLogger(), m_nbiNotifInterceptor);
        m_futureResponse = mock(NetconfResponseFuture.class);
    }

    @After
    public void destroy() {
        m_notificationService = null;
        m_netconfStream = null;
        m_timerManager = null;
        m_notificationLogger = null;
        m_streamList = null;
    }

    @Test
    public void testGetCurrentTimeRequestToDevice() throws Exception {
        NetconfClientSession clientSession = mock(NetconfClientSession.class);
        NotificationListener subscriber = mock(NotificationListener.class);
        DeviceRefId deviceRefId = new DeviceRefId("R1.S1.LT1.PON1.ONT1");
        m_notificationService.getCurrentDatetimeFromDevice(clientSession, deviceRefId);
        ArgumentCaptor<GetRequest> rpcCaptor = ArgumentCaptor.forClass(GetRequest.class);

        ArgumentMatcher<GetRequest> expectedRequest = new ArgumentMatcher<GetRequest>() {
            @Override
            public boolean matches(Object argument) {
                GetRequest createSubscriptionRequest = (GetRequest) argument;
                assertEquals(
                        "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
                                "   <get>\n" +
                                "      <filter type=\"subtree\">\n" +
                                "         <system-state xmlns=\"urn:ietf:params:xml:ns:yang:ietf-system\">\n" +
                                "            <clock>\n" +
                                "               <current-datetime/>\n" +
                                "            </clock>\n" +
                                "         </system-state>\n" +
                                "      </filter>\n" +
                                "   </get>\n" +
                                "</rpc>\n", createSubscriptionRequest.requestToString());
                return true;
            }
        };
        verify(clientSession).get(argThat(expectedRequest));

    }

    @Test
    public void testCurrentDateTimeSupported() throws NotCompliantMBeanException, ParseException, NetconfMessageBuilderException, InterruptedException, ExecutionException {
        NetconfClientSession clientSession = mock(NetconfClientSession.class);
        NotificationListener subscriber = mock(NotificationListener.class);

        String response =
                "    <system-state xmlns=\"urn:ietf:params:xml:ns:yang:ietf-system\"> \n" +
                        "      <clock> \n" +
                        "        <current-datetime>2018-09-15T13:51:52+00:00</current-datetime> \n" +
                        "      </clock> \n" +
                        "    </system-state>";
        NetConfResponse netconfResponse = new NetConfResponse();
        netconfResponse.setData(DocumentUtils.stringToDocument(response).getDocumentElement());

        m_futureResponse = mock(NetconfResponseFuture.class);
        when(m_futureResponse.get()).thenReturn(netconfResponse);
        when(clientSession.get(any(GetRequest.class))).thenReturn(m_futureResponse);

        m_notificationService = new NotificationServiceImpl(m_streamList, m_notificationLogger, m_timerManager, notifExecutor, new DefaultNetconfLogger(), m_nbiNotifInterceptor);
        DeviceRefId deviceRefId = new DeviceRefId("R1.S1.LT1.PON1.ONT1");
        System.setProperty(NotificationServiceImpl.SUBSCR_START_OFFSET_SECS, NotificationServiceImpl.NO_OFFSET);


        // Verify device send create-sub-scription request with startTime is the retrieved current-datetime
        ArgumentMatcher<CreateSubscriptionRequest> expectedRequest = new ArgumentMatcher<CreateSubscriptionRequest>() {
            @Override
            public boolean matches(Object argument) {
                CreateSubscriptionRequest createSubscriptionRequest = (CreateSubscriptionRequest) argument;
                long startTimeMilisecond = createSubscriptionRequest.getStartTime().getMillis();
                assertEquals(new DateTime("2018-09-15T19:21:52+05:30").getMillis(), startTimeMilisecond);
                return true;
            }
        };
        m_notificationService.createSubscriptionWithCallback(clientSession, "1971-01-01T00:00:00+00:00", subscriber, deviceRefId, true);
        verify(clientSession).createSubscription(argThat(expectedRequest), eq(subscriber));
    }


    @Test
    public void testCurrentDateTimeSupportedIncaseResponseContainsPrefix() throws NotCompliantMBeanException, ParseException, NetconfMessageBuilderException, InterruptedException, ExecutionException {
        NetconfClientSession clientSession = mock(NetconfClientSession.class);
        NotificationListener subscriber = mock(NotificationListener.class);
        String currentDateTimeFromDevice = "2018-09-15T13:51:52+00:00";
        String response =
                "    <sys:system-state xmlns:sys=\"urn:ietf:params:xml:ns:yang:ietf-system\"> \n" +
                        "      <sys:clock> \n" +
                        "        <sys:current-datetime>2018-09-15T13:51:52+00:00</sys:current-datetime> \n" +
                        "      </sys:clock> \n" +
                        "    </sys:system-state>";
        NetConfResponse netconfResponse = new NetConfResponse();
        netconfResponse.setData(DocumentUtils.stringToDocument(response).getDocumentElement());
        m_futureResponse = mock(NetconfResponseFuture.class);
        when(m_futureResponse.get()).thenReturn(netconfResponse);
        when(clientSession.get(any(GetRequest.class))).thenReturn(m_futureResponse);

        m_notificationService = new NotificationServiceImpl(m_streamList, m_notificationLogger, m_timerManager, notifExecutor, new DefaultNetconfLogger(), m_nbiNotifInterceptor);
        DeviceRefId deviceRefId = new DeviceRefId("R1.S1.LT1.PON1.ONT1");
        System.setProperty(NotificationServiceImpl.SUBSCR_START_OFFSET_SECS, NotificationServiceImpl.NO_OFFSET);


        // Verify device send create-sub-scription request with startTime is the retrieved current-datetime
        ArgumentMatcher<CreateSubscriptionRequest> expectedRequest = new ArgumentMatcher<CreateSubscriptionRequest>() {
            @Override
            public boolean matches(Object argument) {
                CreateSubscriptionRequest createSubscriptionRequest = (CreateSubscriptionRequest) argument;
                long startTimeMilisecond = createSubscriptionRequest.getStartTime().getMillis();
                assertEquals(new DateTime("2018-09-15T19:21:52+05:30").getMillis(), startTimeMilisecond);
                return true;
            }
        };
        m_notificationService.createSubscriptionWithCallback(clientSession, "1971-01-01T00:00:00+00:00", subscriber, deviceRefId, true);
        verify(clientSession).createSubscription(argThat(expectedRequest), eq(subscriber));
    }

    @Test
    public void testCurrentDateTimeNotSupported() throws NotCompliantMBeanException, ParseException, NetconfMessageBuilderException {
        m_notificationService = new NotificationServiceImpl(m_streamList, m_notificationLogger, m_timerManager, notifExecutor, new DefaultNetconfLogger(), m_nbiNotifInterceptor);
        NetconfClientSession clientSession = mock(NetconfClientSession.class);
        NotificationListener subscriber = mock(NotificationListener.class);
        DeviceRefId deviceRefId = new DeviceRefId("R1.S1.LT1.PON1.ONT1");
        System.setProperty(NotificationServiceImpl.SUBSCR_START_OFFSET_SECS, NotificationServiceImpl.NO_OFFSET);
        DateTime dateTime = new DateTime(NetconfResources.DATE_TIME_WITH_TZ_WITHOUT_MS.parseDateTime("2018-01-01T00:00:00+00:00"));
        ArgumentMatcher<CreateSubscriptionRequest> expectedRequest = new ArgumentMatcher<CreateSubscriptionRequest>() {
            @Override
            public boolean matches(Object argument) {
                CreateSubscriptionRequest createSubscriptionRequest = (CreateSubscriptionRequest) argument;
                assertEquals("<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                        "   <create-subscription xmlns=\"urn:ietf:params:xml:ns:netconf:notification:1.0\">\n" +
                        "      <stream>NETCONF</stream>\n" +
                        "   </create-subscription>\n" +
                        "</rpc>\n", createSubscriptionRequest.requestToString());
                return true;
            }
        };
        m_notificationService.createSubscriptionWithCallback(clientSession, "1971-01-01T00:00:00+00:00", subscriber, deviceRefId, true);
        verify(clientSession).createSubscription(argThat(expectedRequest), eq(subscriber));
    }

    @Test
    public void testCreateSubscription() throws NetconfMessageBuilderException {
        // prepare subscription test request
        NetconfClientInfo clientInfo = new NetconfClientInfo("test", 1);
        CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setMessageId("1");
        request.setStream(NetconfResources.NETCONF);
        // verify createSubscription
        m_notificationService.createSubscription(clientInfo, request, m_responseChannel);
        verify(m_responseChannel, times(1)).sendResponse(any(NetConfResponse.class), any(CreateSubscriptionRequest.class));
        assertTrue(m_notificationService.isActiveSubscription(clientInfo));
    }

    @Test
    public void testCreateSubscriptionTwice() throws NetconfMessageBuilderException {
        // prepare subscription test request
        NetconfClientInfo clientInfo = new NetconfClientInfo("test", 1);
        CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setMessageId("1");
        request.setStream(NetconfResources.NETCONF);
        m_notificationService.createSubscription(clientInfo, request, m_responseChannel);
        // verify isActiveSubscription
        assertTrue(m_notificationService.isActiveSubscription(clientInfo));
        // call createSubscription again
        m_notificationService.createSubscription(clientInfo, request, m_responseChannel);

        ArgumentMatcher<NetConfResponse> response = new ArgumentMatcher<NetConfResponse>() {

            @Override
            public boolean matches(Object argument) {
                NetConfResponse response = (NetConfResponse) argument;
                return response.getMessageId().equals("1");
            }
        };

        verify(m_responseChannel, times(2)).sendResponse(argThat(response), any(CreateSubscriptionRequest.class));
    }

    @Test
    public void testCreateSubscriptionWithInvalidStream() throws NetconfMessageBuilderException {
        // prepare subscription test request
        NetconfClientInfo clientInfo = new NetconfClientInfo("test", 1);
        CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setMessageId("2");
        request.setStream("invalid-stream");
        m_notificationService.createSubscription(clientInfo, request, m_responseChannel);
        ArgumentMatcher<NetConfResponse> response = new ArgumentMatcher<NetConfResponse>() {

            @Override
            public boolean matches(Object argument) {
                NetConfResponse response = (NetConfResponse) argument;
                return response.getMessageId().equals("2");
            }
        };

        verify(m_responseChannel, times(1)).sendResponse(argThat(response), any(CreateSubscriptionRequest.class));
    }

    @Test
    public void testSendNotification() throws NetconfMessageBuilderException, NotCompliantMBeanException {
        m_notificationService = new NotificationServiceImpl(m_streamList, m_notificationLogger, m_timerManager, notifExecutor, new DefaultNetconfLogger(), m_nbiNotifInterceptor) {
            @Override
            public NotificationStream getDefaultNotificationStream() {
                NotificationStream defaultStream = mock(NotificationStream.class);
                when(defaultStream.isActiveSubscription(any())).thenReturn(true);
                return defaultStream;
            }
        };

        NetconfNotification notification = new NetconfNotification();
        notification.setEventTime(123456789123l);

        // prepare subscription test request
        NetconfClientInfo clientInfo = new NetconfClientInfo("test", 1);
        CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setMessageId("1");
        request.setStream(NetconfResources.NETCONF);
        // Does not support config change notification
        List<Stream> streamList = new ArrayList<>();

        Stream netconfStream1 = mock(Stream.class);

        when(netconfStream1.getName()).thenReturn(NetconfResources.CONFIG_CHANGE_STREAM);

        Stream netconfStream2 = mock(Stream.class);

        when(netconfStream2.getName()).thenReturn(NetconfResources.NETCONF);

        streamList.add(netconfStream1);
        streamList.add(netconfStream2);

        Executor notifExecutor = mock(Executor.class);
        m_notificationService = new NotificationServiceImpl(streamList, m_notificationLogger, m_timerManager, notifExecutor, new DefaultNetconfLogger(), m_notificationsInterceptor);

        m_notificationService.createSubscription(clientInfo, request, m_responseChannel);

        m_notificationService.supportNetconfConfigChangeNotification(false);

        m_notificationService.sendNotification(NetconfResources.CONFIG_CHANGE_STREAM, notification);

        //first time for the send notification in case 1, and the seconds time for send notification to default stream not for config change stream
        verify(m_responseChannel, times(1)).sendNotification(notification);
    }

    @Test
    public void testCloseSubscription() {
        NetconfClientInfo clientInfo = new NetconfClientInfo("test", 1);
        assertTrue(m_notificationService.getSupportedStreams().contains(m_netconfStream));
        NotificationStream netconfNotificationStream = m_notificationService.getNotificationStream("NETCONF");
        createSubscription();
        assertTrue(netconfNotificationStream.isActiveSubscription(clientInfo));
        m_notificationService.closeSubscription(clientInfo);
        assertFalse(netconfNotificationStream.isActiveSubscription(clientInfo));
    }

    private void createSubscription() {
        NetconfClientInfo clientInfo = new NetconfClientInfo("test", 1);
        CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setMessageId("1");
        request.setStream(NetconfResources.NETCONF);
        // verify createSubscription
        m_notificationService.createSubscription(clientInfo, request, m_responseChannel);
    }

    @Test
    public void testCreateSubscriptionWithNoneSynschronize() throws NetconfMessageBuilderException, InterruptedException, ExecutionException, ParseException {
        // Setup notification with registering call back
        NotificationCallBack callBack = mock(NotificationCallBack.class);
        List<NotificationCallBackInfo> listCallBackInfo = buildEmptyNotifCallback(callBack);
        m_notificationService.registerCallBack(listCallBackInfo);
        NetconfClientSession clientSession = mock(NetconfClientSession.class);
        NotificationListener subscriber = mock(NotificationListener.class);

        DeviceRefId deviceRefId = new DeviceRefId("R1.S1.LT1.PON1.ONT1");
        DateTime currentTime = new DateTime();
        String currentTimeStr = NetconfResources.DATE_TIME_WITH_TZ_WITHOUT_MS.print(currentTime);
        prepareForDeviceResponse(clientSession, "/notification/getStreamsResponse.xml");
        m_notificationService.createSubscriptionWithCallback(clientSession, currentTimeStr, subscriber, deviceRefId, true);
        verifyCreateSubscriptionInvoked(clientSession);
        verify(callBack, never()).resynchronize(deviceRefId);
    }

    @Test
    public void testCreateSubscriptionWithSynchronize() throws NetconfMessageBuilderException, InterruptedException, ExecutionException, ParseException {
        // Setup notification with registering call back
        NotificationCallBack callBack = mock(NotificationCallBack.class);
        List<NotificationCallBackInfo> listCallBackInfo = buildEmptyNotifCallback(callBack);
        m_notificationService.registerCallBack(listCallBackInfo);
        NetconfClientSession clientSession = mock(NetconfClientSession.class);
        NotificationListener subscriber = mock(NotificationListener.class);
        DeviceRefId deviceRefId = new DeviceRefId("R1.S1.LT1.PON1.ONT1");

        // Case subscription is sent after reSynchronization with call back complete
        DateTime epochTime = new DateTime("1970-01-01T00:00:00+00:00");
        m_notificationService.createSubscriptionWithCallback(clientSession,
                NetconfResources.DATE_TIME_WITH_TZ_WITHOUT_MS.print(epochTime), subscriber, deviceRefId, true);
        verify(callBack).resynchronize(deviceRefId);
        verifyCreateSubscriptionInvoked(clientSession);
    }

    @Test
    public void testCreateSubscriptionFailed() throws NetconfMessageBuilderException, InterruptedException, ExecutionException,
            ParseException {
        NetconfClientSession clientSession = mock(NetconfClientSession.class);
        NotificationListener subscriber = mock(NotificationListener.class);
        DeviceRefId deviceRefId = new DeviceRefId("R1.S1.LT1.PON1.ONT1");
        DateTime currentTime = new DateTime();
        String currentTimeStr = NetconfResources.DATE_TIME_WITH_TZ_WITHOUT_MS.print(currentTime);
        prepareForDeviceResponse(clientSession, "/notification/getStreamsResponse.xml");
        prepareForDeviceSubscriptionResponse(clientSession, false);
        NetConfResponse response = m_notificationService.createSubscriptionWithCallback(clientSession, currentTimeStr, subscriber,
                deviceRefId, true);
        assertFalse(response.isOk());
    }


    @Test
    public void testCreateSubscriptionSuccess() throws NetconfMessageBuilderException, InterruptedException, ExecutionException,
            ParseException {
        NetconfClientSession clientSession = mock(NetconfClientSession.class);
        NotificationListener subscriber = mock(NotificationListener.class);
        DeviceRefId deviceRefId = new DeviceRefId("R1.S1.LT1.PON1.ONT1");
        DateTime currentTime = new DateTime();
        String currentTimeStr = NetconfResources.DATE_TIME_WITH_TZ_WITHOUT_MS.print(currentTime);
        prepareForDeviceSubscriptionResponse(clientSession, true);
        NetConfResponse response = m_notificationService.createSubscriptionWithCallback(clientSession, currentTimeStr, subscriber,
                deviceRefId, true);
        assertTrue(response.isOk());
    }


    @Test
    public void testCreateSubscriptionFailed_NoResponseFromDevice() throws NetconfMessageBuilderException, InterruptedException, ExecutionException,
            ParseException {
        NetconfClientSession clientSession = mock(NetconfClientSession.class);
        NotificationListener subscriber = mock(NotificationListener.class);
        DeviceRefId deviceRefId = new DeviceRefId("R1.S1.LT1.PON1.ONT1");
        DateTime currentTime = new DateTime();
        String currentTimeStr = NetconfResources.DATE_TIME_WITH_TZ_WITHOUT_MS.print(currentTime);
        prepareForDeviceResponse(clientSession, null);
        Future<NetConfResponse> responseForSubscriptionRequest = mock(Future.class);
        when(clientSession.createSubscription(any(CreateSubscriptionRequest.class), any(NotificationListener.class))).thenReturn(
                null);
        NetConfResponse response = m_notificationService.createSubscriptionWithCallback(clientSession, currentTimeStr, subscriber,
                deviceRefId, true);
        assertNull(response);
    }

    @Test
    public void testCheckNotificationAndReplaySupported() throws NetconfMessageBuilderException, InterruptedException, ExecutionException, ParseException {
        // Check notification capability
        NetconfClientSession clientSession = mock(NetconfClientSession.class);
        when(clientSession.getServerCapabilities()).thenReturn(Collections.<String>emptySet());
        assertFalse(m_notificationService.isNotificationSupported(clientSession));
        Set<String> capabilites = new HashSet<String>();
        capabilites.add(NetconfResources.NOTIFICATION_INTERLEAVE);
        capabilites.add(NotificationConstants.CAPABILITY);
        capabilites.add(NetconfResources.NETCONF_NOTIFICATION);
        when(clientSession.getServerCapabilities()).thenReturn(capabilites);
        assertTrue(m_notificationService.isNotificationSupported(clientSession));

        // Check replay supported
        prepareForDeviceResponse(clientSession, "/notification/getStreamsResponse.xml");
        assertTrue(m_notificationService.isReplaySupported(clientSession));
        prepareForDeviceResponse(clientSession, "/notification/getStreamsResponseReplayUnSupported.xml");
        assertFalse(m_notificationService.isReplaySupported(clientSession));
    }

    @Test
    public void testExecuteCallBack() {
        // Setup notification with registering call back
        final DeviceRefId deviceRefId = new DeviceRefId("R1.S1.LT1.PON1.ONT1");
        Notification notification = mock(Notification.class);
        NotificationCallBack callBack = mock(NotificationCallBack.class);
        List<NotificationCallBackInfo> listCallBackInfo = buildEmptyNotifCallback(callBack);
        m_notificationService.registerCallBack(listCallBackInfo);
        NotificationContext context = new NotificationContext();
        context.put(DeviceRefId.DEVICE_ID, deviceRefId);
        m_notificationService.executeCallBack(notification, context);
        verify(callBack).onNotificationReceived(any(Notification.class), argThat(new ArgumentMatcher<NotificationContext>() {

            @Override
            public boolean matches(Object argument) {
                NotificationContext context1 = (NotificationContext) argument;
                return context1.get(DeviceRefId.DEVICE_ID).equals(deviceRefId);
            }
        }), any(DateTime.class));
    }

    @Test
    public void testIsNotificationCallbackAlreadyRegistered() {
        NotificationCallBack callBack = mock(NotificationCallBack.class);
        List<NotificationCallBackInfo> listCallBackInfo = buildNotifCallback(callBack);
        assertFalse(m_notificationService.isNotificationCallbackRegistered(m_alarmNotificationQName, m_capsString));
        m_notificationService.registerCallBack(listCallBackInfo);
        assertTrue(m_notificationService.isNotificationCallbackRegistered(m_alarmNotificationQName, m_capsString));
        Set<QName> notificationTypes = new HashSet<>();
        notificationTypes.add(m_alarmNotificationQName);
        m_notificationService.unregisterCallBack(notificationTypes);
        assertFalse(m_notificationService.isNotificationCallbackRegistered(m_alarmNotificationQName, m_capsString));
    }

    private List<NotificationCallBackInfo> buildEmptyNotifCallback(NotificationCallBack callBack) {
        NotificationCallBackInfo callBackInfo = new NotificationCallBackInfo();
        callBackInfo.setCapabilities(Collections.<String>emptySet());
        callBackInfo.setNotificationTypes(Collections.<QName>emptySet());
        callBackInfo.setCallBack(callBack);
        List<NotificationCallBackInfo> listCallBackInfo = new ArrayList<>();
        listCallBackInfo.add(callBackInfo);
        return listCallBackInfo;
    }

    private List<NotificationCallBackInfo> buildNotifCallback(NotificationCallBack callBack) {
        NotificationCallBackInfo callBackInfo = new NotificationCallBackInfo();
        Set<String> capsStringList = new HashSet<>();
        capsStringList.add(m_capsString);
        callBackInfo.setCapabilities(capsStringList);
        Set<QName> notificationTypes = new HashSet<>();
        notificationTypes.add(m_alarmNotificationQName);
        callBackInfo.setNotificationTypes(notificationTypes);
        callBackInfo.setCallBack(callBack);
        List<NotificationCallBackInfo> listCallBackInfo = new ArrayList<>();
        listCallBackInfo.add(callBackInfo);
        return listCallBackInfo;
    }

    @Test
    public void testCreateSubscriptionWithNonRelaySupported()
            throws NetconfMessageBuilderException, InterruptedException, ExecutionException, ParseException {
        // Setup notification with registering call back
        NotificationCallBack callBack = mock(NotificationCallBack.class);
        List<NotificationCallBackInfo> listCallBackInfo = buildEmptyNotifCallback(callBack);
        m_notificationService.registerCallBack(listCallBackInfo);
        NetconfClientSession clientSession = mock(NetconfClientSession.class);
        NotificationListener subscriber = mock(NotificationListener.class);
        DeviceRefId deviceRefId = new DeviceRefId("R1.S1.LT1.PON1.ONT1");

        // Case subscription is sent after reSynchronization with call back complete
        DateTime startTime = new DateTime("1970-01-01T00:00:00+00:00");
        m_notificationService.createSubscriptionWithCallback(clientSession, NetconfResources.DATE_TIME_WITH_TZ_WITHOUT_MS.print(startTime),
                subscriber, deviceRefId, false);
        verify(callBack, never()).resynchronize(deviceRefId);
        verifyCreateSubscriptionInvoked(clientSession);
    }

    @Test
    public void testCreateSubscriptionWithRelayLogAgedOut()
            throws NetconfMessageBuilderException, InterruptedException, ExecutionException, ParseException, IOException, SAXException, ParserConfigurationException {
        // Setup notification with registering call back
        NotificationCallBack callBack = mock(NotificationCallBack.class);
        List<NotificationCallBackInfo> listCallBackInfo = buildEmptyNotifCallback(callBack);
        m_notificationService.registerCallBack(listCallBackInfo);
        NetconfClientSession clientSession = mock(NetconfClientSession.class);
        NotificationListener subscriber = mock(NotificationListener.class);
        DeviceRefId deviceRefId = new DeviceRefId("R1.S1.LT1.PON1.ONT1");

        String startTime = NetconfResources.DATE_TIME_WITH_TZ.print(DateTime.now().minusMillis(1));
        String ReplayLogAgedTime = NetconfResources.DATE_TIME_WITH_TZ.print(DateTime.now());
        NetConfResponse response = createNetconfStreamsResponse(ReplayLogAgedTime);
        CompletableFuture<NetConfResponse> future = CompletableFuture.completedFuture(response);
        when(clientSession.get(any(GetRequest.class))).thenReturn(m_futureResponse);
        m_notificationService.createSubscriptionWithCallback(clientSession, startTime, subscriber, deviceRefId, true);
        verify(callBack, times(1)).resynchronize(deviceRefId);
        verifyCreateSubscriptionInvoked(clientSession);
    }

    @Test
    public void testCreateSubscriptionWithRelayLogNotAged()
            throws NetconfMessageBuilderException, InterruptedException, ExecutionException, ParseException, IOException, SAXException, ParserConfigurationException {
        // Setup notification with registering call back
        NotificationCallBack callBack = mock(NotificationCallBack.class);
        List<NotificationCallBackInfo> listCallBackInfo = buildEmptyNotifCallback(callBack);
        m_notificationService.registerCallBack(listCallBackInfo);
        NetconfClientSession clientSession = mock(NetconfClientSession.class);
        NotificationListener subscriber = mock(NotificationListener.class);
        DeviceRefId deviceRefId = new DeviceRefId("R1.S1.LT1.PON1.ONT1");

        String ReplayLogAgedTime = NetconfResources.DATE_TIME_WITH_TZ.print(DateTime.now());
        String startTime = NetconfResources.DATE_TIME_WITH_TZ.print(DateTime.now().plusMillis(1));

        NetConfResponse response = createNetconfStreamsResponse(ReplayLogAgedTime);
        when(clientSession.get(any(GetRequest.class))).thenReturn(NetconfResponseFuture.completedNetconfResponseFuture(response));
        m_notificationService.createSubscriptionWithCallback(clientSession, startTime, subscriber, deviceRefId, true);
        verify(callBack, never()).resynchronize(deviceRefId);
        verifyCreateSubscriptionInvoked(clientSession);
    }

    private NetConfResponse createNetconfStreamsResponse(String ReplayLogAgedTime)
            throws IOException, SAXException, ParserConfigurationException {
        NetConfResponse response = new NetConfResponse();
        response.setMessageId("1");
        Element dataContent = DocumentUtils.getDocumentElement(
                "<netconf xmlns=\"urn:ietf:params:xml:ns:netmod:notification\">"
                        + "  <streams>"
                        + "    <stream>"
                        + "      <name>NETCONF</name>"
                        + "      <description>RFC 5277 Default Notifications Stream</description>"
                        + "      <replaySupport>true</replaySupport>"
                        + "      <replayLogCreationTime>1970-01-01T00:00:48+00:00</replayLogCreationTime>"
                        + "      <replayLogAgedTime>" + ReplayLogAgedTime + "</replayLogAgedTime>"
                        + "    </stream>"
                        + "  </streams>"
                        + "</netconf>");
        response.addDataContent(dataContent);
        return response;
    }

    @Test
    public void testRegisterAndUnregister() throws NetconfMessageBuilderException {
        NotificationCallBack callBack = mock(NotificationCallBack.class);
        List<NotificationCallBackInfo> callBackInfos = buildEmptyNotifCallback(callBack);
        m_notificationService.registerCallBack(callBackInfos);
        NetconfClientSession clientSession = mock(NetconfClientSession.class);
        NotificationListener subscriber = mock(NotificationListener.class);
        DeviceRefId deviceRefId = new DeviceRefId("R1.S1.LT1.PON1.ONT1");

        // Case subscription is sent after reSynchronization with call back complete
        DateTime epochTime = new DateTime("1970-01-01T00:00:00+00:00");
        m_notificationService.createSubscriptionWithCallback(clientSession,
                NetconfResources.DATE_TIME_WITH_TZ_WITHOUT_MS.print(epochTime), subscriber, deviceRefId, true);
        verify(callBack).resynchronize(deviceRefId);

        m_notificationService.unregisterCallBack(Collections.emptySet());
        m_notificationService.createSubscriptionWithCallback(clientSession,
                NetconfResources.DATE_TIME_WITH_TZ_WITHOUT_MS.print(epochTime), subscriber, deviceRefId, true);
        verify(callBack).resynchronize(deviceRefId);
    }

    @Test
    public void testFullResynchronizeHandler_WithOffset() throws NetconfMessageBuilderException {
        NetconfClientSession clientSession = mock(NetconfClientSession.class);
        NotificationListener subscriber = mock(NotificationListener.class);
        DeviceRefId deviceRefId = new DeviceRefId("R1.S1.LT1.PON1.ONT1");

        System.setProperty(NotificationServiceImpl.SUBSCR_START_OFFSET_SECS, "2");
        String timeStr = "1971-01-01T00:00:00+00:00";
        DateTime time = new DateTime("1971-01-01T00:00:00+00:00");
        m_notificationService.fullResynchronizeHandler(clientSession, subscriber, deviceRefId, timeStr);
        ArgumentCaptor<CreateSubscriptionRequest> reqCaptor = ArgumentCaptor.forClass(CreateSubscriptionRequest.class);
        verify(clientSession).createSubscription(reqCaptor.capture(), eq(subscriber));
        long expectedTimeMiliseconds = new DateTime("1971-01-01T05:30:02.000+05:30").getMillis();
        assertEquals(expectedTimeMiliseconds, new DateTime(reqCaptor.getValue().getStartTime().toString()).getMillis());

        System.setProperty(NotificationServiceImpl.SUBSCR_START_OFFSET_SECS, "-2");
        m_notificationService.fullResynchronizeHandler(clientSession, subscriber, deviceRefId, timeStr);
        reqCaptor = ArgumentCaptor.forClass(CreateSubscriptionRequest.class);
        verify(clientSession, atLeastOnce()).createSubscription(reqCaptor.capture(), eq(subscriber));
        assertEquals(time.minus(2000), reqCaptor.getValue().getStartTime());

        System.setProperty(NotificationServiceImpl.SUBSCR_START_OFFSET_SECS, "2");
        m_notificationService.fullResynchronizeHandler(clientSession, subscriber, deviceRefId, timeStr);
        reqCaptor = ArgumentCaptor.forClass(CreateSubscriptionRequest.class);
        verify(clientSession, atLeastOnce()).createSubscription(reqCaptor.capture(), eq(subscriber));
        assertEquals(time.plus(2000), reqCaptor.getValue().getStartTime());

        System.setProperty(NotificationServiceImpl.SUBSCR_START_OFFSET_SECS, NotificationServiceImpl.NO_OFFSET);
        m_notificationService.fullResynchronizeHandler(clientSession, subscriber, deviceRefId, timeStr);
        reqCaptor = ArgumentCaptor.forClass(CreateSubscriptionRequest.class);
        verify(clientSession, atLeastOnce()).createSubscription(reqCaptor.capture(), eq(subscriber));
        assertEquals(time, reqCaptor.getValue().getStartTime());

        System.setProperty(NotificationServiceImpl.SUBSCR_START_OFFSET_SECS, NotificationServiceImpl.EPOCH);
        m_notificationService.fullResynchronizeHandler(clientSession, subscriber, deviceRefId, timeStr);
        reqCaptor = ArgumentCaptor.forClass(CreateSubscriptionRequest.class);
        verify(clientSession, atLeastOnce()).createSubscription(reqCaptor.capture(), eq(subscriber));
        assertEquals(new DateTime("1970-01-01T00:00:00+00:00"), reqCaptor.getValue().getStartTime());

        System.setProperty(NotificationServiceImpl.SUBSCR_START_OFFSET_SECS, NotificationServiceImpl.NULL);
        m_notificationService.fullResynchronizeHandler(clientSession, subscriber, deviceRefId, timeStr);
        reqCaptor = ArgumentCaptor.forClass(CreateSubscriptionRequest.class);
        verify(clientSession, atLeastOnce()).createSubscription(reqCaptor.capture(), eq(subscriber));
        assertNull(reqCaptor.getValue().getStartTime());
    }

    private void prepareForDeviceResponse(NetconfClientSession clientSession, String responsePathFile)
            throws NetconfMessageBuilderException, InterruptedException, ExecutionException, ParseException {
        Set<String> caps = new HashSet<>();
        caps.add(NetconfResources.NOTIFICATION_INTERLEAVE);
        caps.add(NotificationConstants.CAPABILITY);
        caps.add(NetconfResources.NETCONF_NOTIFICATION);
        when(clientSession.getServerCapabilities()).thenReturn(caps);

        when(clientSession.get(argThat(new ArgumentMatcher<GetRequest>() {

            @Override
            public boolean matches(Object argument) {
                return true;
            }
        }))).thenReturn(m_futureResponse);

        if (responsePathFile != null) {
            NetConfResponse getResponse = mock(NetConfResponse.class);
            when(m_futureResponse.get()).thenReturn(getResponse);
            Document doc = DocumentUtils.loadXmlDocument(this.getClass().getResourceAsStream(responsePathFile));
            when(getResponse.getResponseDocument()).thenReturn(doc);
        }else {
            when(m_futureResponse.get()).thenReturn(null);
        }

    }

    private void prepareForDeviceSubscriptionResponse(NetconfClientSession clientSession, boolean isOk)
            throws NetconfMessageBuilderException, InterruptedException, ExecutionException {
            when(clientSession.createSubscription(any(CreateSubscriptionRequest.class), any(NotificationListener.class))).thenReturn(
                    m_futureResponse);
            NetConfResponse deviceResponse = mock(NetConfResponse.class);
            when(deviceResponse.isOk()).thenReturn(isOk);
            when(m_futureResponse.get()).thenReturn(deviceResponse);
        }


    private void verifyCreateSubscriptionInvoked(NetconfClientSession clientSession) throws NetconfMessageBuilderException {
        verify(clientSession).createSubscription(argThat(new ArgumentMatcher<CreateSubscriptionRequest>() {

            @Override
            public boolean matches(Object argument) {
                CreateSubscriptionRequest request = (CreateSubscriptionRequest) argument;
                return NetconfResources.NETCONF.equals(request.getStream()) && request.getFilter() == null;
            }
        }), any(NotificationListener.class));
    }
}
    class DeviceRefId {
        private String deviceId;
        public static final String DEVICE_ID = "device-id";

        public DeviceRefId(String part1) {
            deviceId = part1;
        }

        public String getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(String deviceId) {
            this.deviceId = deviceId;
        }
    }

