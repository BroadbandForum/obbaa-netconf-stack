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

package org.broadband_forum.obbaa.netconf.client.tls;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.broadband_forum.obbaa.netconf.api.authentication.AuthenticationListener;
import org.broadband_forum.obbaa.netconf.api.client.NotificationListener;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.SocketChannelConfig;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;

/**
 * Created by kbhatk on 5/12/16.
 */
public class CallhomeTlsNetconfClientInitializerTest {
    CallhomeTlsNetconfClientInitializer m_initializer;
    @Mock
    private SslContext m_sslContext;
    @Mock
    private SocketChannel m_channel;
    @Mock
    private SocketChannelConfig m_socketChannelConfig;
    @Mock
    private SSLEngine m_sslEngine;
    @Mock
    private SSLSession m_sslSession;
    @Mock
    private ChannelPipeline m_channelPipeline;
    @Mock
    private AuthenticationListener m_authListener;
    @Mock
    private NotificationListener m_notificationListener;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        m_initializer = new CallhomeTlsNetconfClientInitializer(m_sslContext, null, Collections.<String>emptySet(),
                m_authListener,
                m_notificationListener, false, false, null, null, 35000);
        when(m_channel.config()).thenReturn(m_socketChannelConfig);
        when(m_channel.pipeline()).thenReturn(m_channelPipeline);
        when(m_sslContext.newEngine((ByteBufAllocator) anyObject())).thenReturn(m_sslEngine);
        when(m_sslEngine.getSession()).thenReturn(m_sslSession);
    }

    @Test
    public void testHandshakeTimeout() throws Exception {
        ArgumentCaptor<ChannelHandler> handlerCaptor = ArgumentCaptor.forClass(ChannelHandler.class);
        m_initializer.initChannel(m_channel);
        verify(m_channelPipeline, times(4)).addLast(handlerCaptor.capture());
        assertTrue(handlerCaptor.getAllValues().get(0) instanceof SslHandler);
        assertEquals(35000, ((SslHandler) handlerCaptor.getAllValues().get(0)).getHandshakeTimeoutMillis());
        assertTrue(handlerCaptor.getAllValues().get(1) instanceof DelimiterBasedFrameDecoder);
        assertTrue(handlerCaptor.getAllValues().get(2) instanceof StringDecoder);
        assertTrue(handlerCaptor.getAllValues().get(3) instanceof StringEncoder);
    }

}
