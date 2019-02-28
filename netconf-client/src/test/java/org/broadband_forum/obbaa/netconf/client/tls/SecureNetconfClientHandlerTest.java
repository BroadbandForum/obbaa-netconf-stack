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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.security.cert.X509Certificate;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import org.broadband_forum.obbaa.netconf.api.client.CallHomeListener;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientSession;
import org.broadband_forum.obbaa.netconf.api.client.NetconfLoginProvider;
import org.broadband_forum.obbaa.netconf.api.utils.FileUtil;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;

@RunWith(MockitoJUnitRunner.class)
public class SecureNetconfClientHandlerTest {
    
    @Mock private TlsNettyChannelNetconfClientSession m_clientSession;
    @Mock private Set<String> m_caps;
    @Mock private ExecutorService m_callHomeExecutorService;
    @Mock private CallHomeListener m_callHomeListener;
    @Mock private X509Certificate m_peerCertificate;
    @Mock private ChannelHandlerContext m_ctx;
    
    private SecureNetconfClientHandler m_clientHandler;

    @Before
    public void setup() {
        m_clientHandler = new SecureNetconfClientHandler(m_clientSession, m_caps,
                m_callHomeExecutorService, m_callHomeListener, m_peerCertificate, false);
        
        doAnswer(new Answer<Future<?>>() {
            @Override
            public Future<?> answer(InvocationOnMock invocation) throws Throwable {
                Runnable runnable = (Runnable) invocation.getArguments()[0];
                runnable.run();
                return null;
            }
        }).when(m_callHomeExecutorService).execute(any(Runnable.class));
    }
    
    @Test
    public void testHelloMsg() throws Exception {
        String helloMsg = FileUtil
                .loadAsString("/sampleEomMessageHello.txt").replaceAll("]]>]]>\\n", "]]>]]>");
        m_clientHandler.channelRead0(m_ctx, helloMsg);
        
        verify(m_callHomeExecutorService, times(1)).execute(any(Runnable.class));

        verify(m_callHomeListener, times(1)).connectionEstablished(any(NetconfClientSession.class), any(NetconfLoginProvider.class),
                any(X509Certificate.class), any(boolean.class));
        
    }
}
