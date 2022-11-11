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

package org.broadband_forum.obbaa.netconf.server.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import org.broadband_forum.obbaa.netconf.api.client.ContextSetter;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationService;
import org.broadband_forum.obbaa.netconf.server.AggregateContext;
import org.broadband_forum.obbaa.netconf.server.RequestCategory;
import org.broadband_forum.obbaa.netconf.server.RequestContext;
import org.broadband_forum.obbaa.netconf.server.RequestTask;
import org.broadband_forum.obbaa.netconf.server.UserContext;
import org.broadband_forum.obbaa.netconf.stack.logging.ual.UALLogger;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

public class MessageHandlerUtilTest {

    public static final String ADDITIONAL_USER_CTX = "additionaluserctx";
    public static final String ADDITIONAL_USER_SESSIONID = "11";
    public static final String CLIENT_USERNAME = "ut";
    @Mock
    private NetconfClientInfo m_clientInfo;
    @Mock
    private NotificationService m_notificationService;
    @Mock
    private UALLogger m_ualLogger;

    @Before
    public void setup(){
        initMocks(this);
        RequestContext.reset();
        when(m_clientInfo.getUsername()).thenReturn(CLIENT_USERNAME);
        when(m_clientInfo.getSessionId()).thenReturn(10);
        when(m_ualLogger.canLog()).thenReturn(true);
    }

    @Test
    public void testUpdateRequestTask(){
        EditConfigRequest request = mock(EditConfigRequest.class);
        RequestTask requestTask = mock(RequestTask.class);

        when(m_clientInfo.getClientSessionId()).thenReturn(12);
        when(request.getAdditionalUserContext()).thenReturn(ADDITIONAL_USER_CTX);
        when(request.getAdditionalUserSessionId()).thenReturn(ADDITIONAL_USER_SESSIONID);
        when(request.getApplication()).thenReturn("applicationname");

        MessageHandlerUtil.updateRequestTask(m_clientInfo, request, requestTask, m_notificationService , m_ualLogger);

        verify(requestTask).setNotificationService(eq(m_notificationService));
        verify(requestTask).setCanLog(eq(true));

        ArgumentCaptor<RequestContext> captor = ArgumentCaptor.forClass(RequestContext.class);
        verify(requestTask).setRequestContext(captor.capture());
        RequestContext requestCtx = captor.getValue();
        assertNotNull(requestCtx);
        assertEquals(RequestCategory.NBI, requestCtx.getRequestCategory());
        assertNull(requestCtx.getAggregateContext());
        assertEquals("applicationname", requestCtx.getApplication());
        assertEquals(ADDITIONAL_USER_CTX, requestCtx.getAdditionalUserCtxt().getUsername());
        assertEquals(ADDITIONAL_USER_SESSIONID, requestCtx.getAdditionalUserCtxt().getSessionId());
        assertEquals(CLIENT_USERNAME, requestCtx.getLoggedInUserCtxt().getUsername());
        assertEquals("12", requestCtx.getLoggedInUserCtxt().getSessionId());
    }

    @Test
    public void testGetLoggedInUserCtxt(){
        UserContext userCtx = new UserContext("UT", "1");
        RequestContext.setLoggedInUserCtxtTL(userCtx);

        assertEquals(userCtx , MessageHandlerUtil.getLoggedInUserCtxt(m_clientInfo));

        RequestContext.reset();
        ContextSetter contextSetter = mock(ContextSetter.class);
        when(contextSetter.getSessionId()).thenReturn("123");
        when(m_clientInfo.getClientContextSetter()).thenReturn(contextSetter);

        UserContext userContext = MessageHandlerUtil.getLoggedInUserCtxt(m_clientInfo);
        assertEquals(CLIENT_USERNAME, userContext.getUsername());
        assertEquals("123", userContext.getSessionId());

        RequestContext.reset();
        when(m_clientInfo.getClientContextSetter()).thenReturn(null);
        when(m_clientInfo.getClientSessionId()).thenReturn(546);

        userContext = MessageHandlerUtil.getLoggedInUserCtxt(m_clientInfo);
        assertEquals(CLIENT_USERNAME, userContext.getUsername());
        assertEquals("546", userContext.getSessionId());

        RequestContext.reset();
        when(m_clientInfo.getClientContextSetter()).thenReturn(null);
        when(m_clientInfo.getClientSessionId()).thenReturn(null);
        assertNull(MessageHandlerUtil.getLoggedInUserCtxt(m_clientInfo));
    }

    @Test
    public void testGetAdditionalUserContext(){
        EditConfigRequest request = mock(EditConfigRequest.class);
        assertNull(MessageHandlerUtil.getAdditionalUserContext(request));

        when(request.getAdditionalUserContext()).thenReturn(ADDITIONAL_USER_CTX);
        when(request.getAdditionalUserSessionId()).thenReturn(ADDITIONAL_USER_SESSIONID);

        UserContext userContext = MessageHandlerUtil.getAdditionalUserContext(request);
        assertEquals(ADDITIONAL_USER_CTX, userContext.getUsername());
        assertEquals(ADDITIONAL_USER_SESSIONID, userContext.getSessionId());
    }

    @Test
    public void testGetApplication(){
        EditConfigRequest request = mock(EditConfigRequest.class);
        assertNull(MessageHandlerUtil.getApplication(request));

        when(request.getApplication()).thenReturn("application1");
        assertEquals("application1", MessageHandlerUtil.getApplication(request));

        when(request.getApplication()).thenReturn(null);
        RequestContext.setApplicationTL("application2");
        assertEquals("application2", MessageHandlerUtil.getApplication(request));
    }

    @Test
    public void testGetAggregateContext(){
        assertNull(MessageHandlerUtil.getAggregateContext());

        AggregateContext aggregateCtx = mock(AggregateContext.class);
        RequestContext.setAggregateContextTL(aggregateCtx);
        assertEquals(aggregateCtx, MessageHandlerUtil.getAggregateContext());
    }
}
