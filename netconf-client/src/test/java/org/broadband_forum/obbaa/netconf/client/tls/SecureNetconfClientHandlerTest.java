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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;

import org.broadband_forum.obbaa.netconf.api.client.CallHomeListener;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientSession;
import org.broadband_forum.obbaa.netconf.api.client.NetconfLoginProvider;
import org.broadband_forum.obbaa.netconf.api.codec.v2.ChunkedNetconfMessageCodecV2;
import org.broadband_forum.obbaa.netconf.api.codec.v2.DocumentInfo;
import org.broadband_forum.obbaa.netconf.api.codec.v2.EOMNetconfMessageCodecV2;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.api.utils.FileUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.w3c.dom.Document;

import com.google.common.base.Splitter;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.concurrent.Future;

@RunWith(MockitoJUnitRunner.class)
public class SecureNetconfClientHandlerTest {

    @Mock
    private TlsNettyChannelNetconfClientSession m_clientSession;
    @Mock private ExecutorService m_callHomeExecutorService;
    @Mock private CallHomeListener m_callHomeListener;
    @Mock private X509Certificate m_peerCertificate;
    @Mock private ChannelHandlerContext m_ctx;
    @Mock private ChannelPipeline m_pipeline;
    @Mock private SocketChannel m_socketChannel;
    @Mock private ChannelFuture m_channelFuture;

    private SecureNetconfClientHandler m_clientHandler;
    @Before
    public void setup() {
        HashSet<String> caps = new HashSet<>();
        caps.add(NetconfResources.NETCONF_BASE_CAP_1_0);
        caps.add(NetconfResources.NETCONF_BASE_CAP_1_1);
        m_clientHandler = new SecureNetconfClientHandler(m_clientSession, caps,
                m_callHomeExecutorService, m_callHomeListener, m_peerCertificate, false);
        when(m_ctx.pipeline()).thenReturn(m_pipeline);
        when(m_clientSession.getServerChannel()).thenReturn(m_socketChannel);
        when(m_socketChannel.close()).thenReturn(m_channelFuture);

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
    public void testHelloMsg1_0() throws Exception {
        String helloMsg = FileUtil.loadAsString("/sampleEomMessageHello.txt");
        m_clientHandler.channelRead0(m_ctx, helloMsg);
        verify(m_clientSession).responseRecieved(any(DocumentInfo.class));
        assertTrue(m_clientHandler.getCodec() instanceof EOMNetconfMessageCodecV2);

        verify(m_callHomeExecutorService, times(1)).execute(any(Runnable.class));

        verify(m_callHomeListener, times(1)).connectionEstablished(any(NetconfClientSession.class), any(NetconfLoginProvider.class),
                any(X509Certificate.class), any(boolean.class));
    }

    @Test
    public void testHelloMsg10InManyPackets() throws Exception {
        String helloMsg = FileUtil.loadAsString("/sampleEomMessageHello.txt");
        sendMessageInPackets(helloMsg);
        verify(m_clientSession).responseRecieved(any(DocumentInfo.class));
        assertTrue(m_clientHandler.getCodec() instanceof EOMNetconfMessageCodecV2);

        verify(m_callHomeExecutorService, times(1)).execute(any(Runnable.class));

        verify(m_callHomeListener, times(1)).connectionEstablished(any(NetconfClientSession.class), any(NetconfLoginProvider.class),
                any(X509Certificate.class), any(boolean.class));
    }

    @Test
    public void testHello10WithAnotherRPC() throws Exception {
        String rpcs = FileUtil.loadAsString("/sampleEomMessageHello.txt") + FileUtil.loadAsString("/EOMDelimitedEditConfigReq.txt");
        m_clientHandler.channelRead0(m_ctx, rpcs);

        assertTrue(m_clientHandler.getCodec() instanceof EOMNetconfMessageCodecV2);

        verify(m_clientSession, times(2)).responseRecieved(any(DocumentInfo.class));
        verify(m_callHomeExecutorService, times(1)).execute(any(Runnable.class));

        verify(m_callHomeListener, times(1)).connectionEstablished(any(NetconfClientSession.class), any(NetconfLoginProvider.class),
                any(X509Certificate.class), any(boolean.class));
    }

    @Test
    public void testHello10WithAnotherRpcMultiplePackets() throws Exception {
        String rpcs = FileUtil.loadAsString("/sampleEomMessageHello.txt") + FileUtil.loadAsString("/EOMDelimitedEditConfigReq.txt");
        sendMessageInPackets(rpcs);

        assertTrue(m_clientHandler.getCodec() instanceof EOMNetconfMessageCodecV2);

        verify(m_clientSession, times(2)).responseRecieved(any(DocumentInfo.class));
        verify(m_callHomeExecutorService, times(1)).execute(any(Runnable.class));

        verify(m_callHomeListener, times(1)).connectionEstablished(any(NetconfClientSession.class), any(NetconfLoginProvider.class),
                any(X509Certificate.class), any(boolean.class));
    }

    @Test
    public void testHelloMsg1_1() throws Exception {
        assertTrue(m_clientHandler.getCodec() instanceof EOMNetconfMessageCodecV2);
        String helloMsg = FileUtil.loadAsString("/sampleHelloMessageWith_1_1.txt") ;
        m_clientHandler.channelRead0(m_ctx, helloMsg);
        assertTrue(m_clientHandler.getCodec() instanceof ChunkedNetconfMessageCodecV2);
        verify(m_clientSession).responseRecieved(any(DocumentInfo.class));

        verify(m_callHomeExecutorService, times(1)).execute(any(Runnable.class));

        verify(m_callHomeListener, times(1)).connectionEstablished(any(NetconfClientSession.class), any(NetconfLoginProvider.class),
                any(X509Certificate.class), any(boolean.class));
    }

    @Test
    public void testHelloMsg1_1WithAnotherRPC() throws Exception {
        assertTrue(m_clientHandler.getCodec() instanceof EOMNetconfMessageCodecV2);
        String rpcs = FileUtil.loadAsString("/sampleHelloMessageWith_1_1.txt") + FileUtil.loadAsString("/sampleChunkedMessage2.txt");
        m_clientHandler.channelRead0(m_ctx, rpcs);
        assertTrue(m_clientHandler.getCodec() instanceof ChunkedNetconfMessageCodecV2);
        verify(m_clientSession, times(2)).responseRecieved(any(DocumentInfo.class));

        verify(m_callHomeExecutorService, times(1)).execute(any(Runnable.class));

        verify(m_callHomeListener, times(1)).connectionEstablished(any(NetconfClientSession.class), any(NetconfLoginProvider.class),
                any(X509Certificate.class), any(boolean.class));
    }

    @Test
    public void testHelloMsg1_1WithHelloAndAnInvalidRpcSessionNotClosed() throws NetconfMessageBuilderException, InterruptedException {
        assertTrue(m_clientHandler.getCodec() instanceof EOMNetconfMessageCodecV2);
        String rpc1 = FileUtil.loadAsString("/sampleHelloMessageWith_1_1.txt");
        String rpc2 = "\n" +
                "#477\n" +
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<rpc-reply xmlns:ncx=\"http://netconfcentral.org/ns/yuma-ncx\"\n" +
                "  xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "  <rpc-error>\n" +
                "    <error-type>transport</error-type>\n" +
                "    <error-tag>malformed-message</error-tag>\n" +
                "    <error-severity>error</error-severity>\n" +
                "    <error-app-tag>data-invalid</error-app-tag>\n" +
                "    <error-message xml:lang=\"en\">invalid protocol framing characters received</error-message>\n" +
                "    <error-info>\n" +  //error info has no end tag
                "  </rpc-error>\n" +
                "</rpc-reply\n" +
                "#1\n" +
                ">\n##\n";
        m_clientHandler.channelRead0(m_ctx, rpc1);

        assertTrue(m_clientHandler.getCodec() instanceof ChunkedNetconfMessageCodecV2);
        verify(m_clientSession, times(1)).responseRecieved(any(DocumentInfo.class));

        verify(m_callHomeExecutorService, times(1)).execute(any(Runnable.class));

        verify(m_callHomeListener, times(1)).connectionEstablished(eq(m_clientSession), any(NetconfLoginProvider.class),
                any(X509Certificate.class), any(boolean.class));

        m_clientHandler.channelRead0(m_ctx, rpc2);
        verify(m_clientSession, times(0)).close();
    }

    @Test
    public void testHelloInvalidSessionClosed() throws InterruptedException {
        String invalidHello = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                " <hello xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "   <capabilities>\n" +
                "     <capability>\n" +
                "       urn:ietf:params:netconf:base:1.0\n" +
                "     </capability>\n" +
                "     <capability>\n" +
                "       urn:ietf:params:netconf:base:1.1\n" +
                "     </capability>\n" +
                "     <capability>\n" +
                "       urn:ietf:params:ns:netconf:capability:startup:1.0\n" +
                "     </capability>\n" +
                "   </capabilities>\n" +
                "   <session-id>\n" + //no closing tag for session-id
                " </hello>\n" +
                "]]>]]>";

        m_clientHandler.channelRead0(m_ctx, invalidHello);
        verify(m_clientSession).close();
    }

    @Test
    public void testHelloMsg1_1WithHelloAndAnIncorrectChunkSessionIsClosed() throws Exception {
        assertTrue(m_clientHandler.getCodec() instanceof EOMNetconfMessageCodecV2);
        String rpc1 = FileUtil.loadAsString("/sampleHelloMessageWith_1_1.txt");
        String rpc2 = "\n#13\n<rpc-reply/>\n##\n";
        m_clientHandler.channelRead0(m_ctx, rpc1);

        assertTrue(m_clientHandler.getCodec() instanceof ChunkedNetconfMessageCodecV2);
        verify(m_clientSession, times(1)).responseRecieved(any(DocumentInfo.class));

        verify(m_callHomeExecutorService, times(1)).execute(any(Runnable.class));

        verify(m_callHomeListener, times(1)).connectionEstablished(eq(m_clientSession), any(NetconfLoginProvider.class),
                any(X509Certificate.class), any(boolean.class));

        m_clientHandler.channelRead0(m_ctx, rpc2);
        verify(m_clientSession).close();
    }

    @Test
    public void testHelloMsg1_1WithAnotherRPCInDifferentCalls() throws Exception {
        assertTrue(m_clientHandler.getCodec() instanceof EOMNetconfMessageCodecV2);
        String rpc1 = FileUtil.loadAsString("/sampleHelloMessageWith_1_1.txt");
        String rpc2 = FileUtil.loadAsString("/sampleChunkedMessage2.txt");
        m_clientHandler.channelRead0(m_ctx, rpc1);
        m_clientHandler.channelRead0(m_ctx, rpc2);
        assertTrue(m_clientHandler.getCodec() instanceof ChunkedNetconfMessageCodecV2);
        verify(m_clientSession, times(2)).responseRecieved(any(DocumentInfo.class));

        verify(m_callHomeExecutorService, times(1)).execute(any(Runnable.class));

        verify(m_callHomeListener, times(1)).connectionEstablished(any(NetconfClientSession.class), any(NetconfLoginProvider.class),
                any(X509Certificate.class), any(boolean.class));
    }

    @Test
    public void testHelloMsg11InManyPackets() throws Exception {
        assertTrue(m_clientHandler.getCodec() instanceof EOMNetconfMessageCodecV2);
        String helloMsg = FileUtil.loadAsString("/sampleHelloMessageWith_1_1.txt");
        sendMessageInPackets(helloMsg);
        assertTrue(m_clientHandler.getCodec() instanceof ChunkedNetconfMessageCodecV2);
        verify(m_clientSession).responseRecieved(any(DocumentInfo.class));

        verify(m_callHomeExecutorService, times(1)).execute(any(Runnable.class));

        verify(m_callHomeListener, times(1)).connectionEstablished(any(NetconfClientSession.class), any(NetconfLoginProvider.class),
                any(X509Certificate.class), any(boolean.class));
    }

    @Test
    public void testHelloMsg1_1WithAnotherRPCMultiplePackets() throws Exception {
        assertTrue(m_clientHandler.getCodec() instanceof EOMNetconfMessageCodecV2);
        String rpcs = FileUtil.loadAsString("/sampleHelloMessageWith_1_1.txt") + FileUtil.loadAsString("/sampleChunkedMessage2.txt");
        sendMessageInPackets(rpcs);
        assertTrue(m_clientHandler.getCodec() instanceof ChunkedNetconfMessageCodecV2);
        verify(m_clientSession, times(2)).responseRecieved(any(DocumentInfo.class));

        verify(m_callHomeExecutorService, times(1)).execute(any(Runnable.class));

        verify(m_callHomeListener, times(1)).connectionEstablished(any(NetconfClientSession.class), any(NetconfLoginProvider.class),
                any(X509Certificate.class), any(boolean.class));
    }

    @Test
    public void testWhenChannelInactiveSessionIsClosedAndBufIsReleased() throws Exception {
        m_clientHandler.channelInactive(m_ctx);
        verify(m_ctx).fireChannelInactive();
        verify(m_clientSession).sessionClosed();
        assertEquals(0, m_clientHandler.getByteBuf().refCnt());
    }

    @Test
    public void testWhenExceptionCaughtSessionIsClosedAndBufIsReleased() throws Exception {
        m_clientHandler.exceptionCaught(m_ctx, new Throwable());
        verify(m_ctx).close();
        verify(m_clientSession).sessionClosed();
        assertEquals(0, m_clientHandler.getByteBuf().refCnt());
    }

    @Test
    public void testWhenConnectionEstablishedThrowsException() throws Exception {
        doThrow(new RuntimeException("exception while establishing connection")).when(m_callHomeListener).connectionEstablished(any(NetconfClientSession.class), any(NetconfLoginProvider.class),
                any(X509Certificate.class), any(boolean.class));
        assertTrue(m_clientHandler.getCodec() instanceof EOMNetconfMessageCodecV2);
        String helloMsg = FileUtil.loadAsString("/sampleHelloMessageWith_1_1.txt") ;
        m_clientHandler.channelRead0(m_ctx, helloMsg);
        verify(m_channelFuture).sync();
        assertEquals(0, m_clientHandler.getByteBuf().refCnt());
    }

    private void sendMessageInPackets(String message) throws Exception {
        for (final String token :
                Splitter.fixedLength(10).split(message)) {
            m_clientHandler.channelRead0(m_ctx, token);
        }
    }
}
