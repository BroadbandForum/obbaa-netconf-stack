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

package org.broadband_forum.obbaa.netconf.server.tls;

import org.broadband_forum.obbaa.netconf.api.logger.NetconfLogger;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerMessageListener;
import org.broadband_forum.obbaa.netconf.api.server.NetconfSessionIdProvider;
import org.broadband_forum.obbaa.netconf.api.server.ServerMessageHandler;
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
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SecureNetconfServerInitializerTest {
    SecureNetconfServerInitializer m_initializer;
    @Mock
    private SslContext m_sslContext;
    @Mock
    private NetconfServerMessageListener m_serverMessageListener;
    @Mock
    private ServerMessageHandler m_serverMessageHandler;
    @Mock
    private NetconfSessionIdProvider m_sessionIdProvider;
    @Mock
    private NetconfLogger m_netconfLogger;
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

    @Before
    public  void setUp(){
        MockitoAnnotations.initMocks(this);
        m_initializer = new SecureNetconfServerInitializer(m_sslContext, Collections.<String>emptySet(), m_serverMessageListener,
                m_serverMessageHandler, null, false, false, m_sessionIdProvider, m_netconfLogger,30000);
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
        verify(m_channelPipeline).addLast(eq("EOM_HANDLER"),handlerCaptor.capture());
        assertTrue(handlerCaptor.getAllValues().get(0) instanceof SslHandler);
        assertEquals(30000, ((SslHandler)handlerCaptor.getAllValues().get(0)).getHandshakeTimeoutMillis());
        assertTrue(handlerCaptor.getAllValues().get(4) instanceof DelimiterBasedFrameDecoder);
        assertTrue(handlerCaptor.getAllValues().get(1) instanceof StringDecoder);
        assertTrue(handlerCaptor.getAllValues().get(2) instanceof StringEncoder);
        assertTrue(handlerCaptor.getAllValues().get(3) instanceof SecureNetconfServerHandler);
    }
}
