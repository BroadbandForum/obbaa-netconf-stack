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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;

import io.netty.channel.ChannelFuture;

public class SecureChannelFutureListenerTest {

    private SecureChannelFutureListener m_initializer;
    
    private NetConfResponse m_response = new NetConfResponse();
    private NetconfClientInfo m_clientInfo = new NetconfClientInfo("adminuser", 1);
    
    @Mock
    private ChannelFuture m_channelFuture;

    @Before
    public  void setUp() throws Exception{
        MockitoAnnotations.initMocks(this);
        m_initializer = new SecureChannelFutureListener(m_response, m_clientInfo);
    }
    
    @Test
    public void testOperationComplete() throws Exception {
        when (m_channelFuture.isSuccess()).thenReturn(true);
        m_initializer.operationComplete(m_channelFuture);
        assertEquals("Response Sent", m_response.getMessageSentFuture().get());
    }
    
    @Test
    public void testOperationComplete_exception() throws Exception {
        when (m_channelFuture.cause()).thenReturn(new IOException("closed"));
        when (m_channelFuture.isSuccess()).thenReturn(false);
        m_initializer.operationComplete(m_channelFuture);
        try {
            m_response.getMessageSentFuture().get();
            fail("Expected exception not thrown");
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof IOException);
            assertTrue(m_response.getMessageSentFuture().isCompletedExceptionally());
        }
    }
}
