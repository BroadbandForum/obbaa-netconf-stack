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

package org.broadband_forum.obbaa.netconf.client.ssh;

import static junit.framework.TestCase.fail;
import static org.broadband_forum.obbaa.netconf.api.util.DocumentUtils.getDocFromFile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.net.URL;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import io.netty.util.concurrent.Future;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ChannelSubsystem;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.future.CloseFuture;
import org.apache.sshd.common.future.SshFutureListener;
import org.apache.sshd.common.io.IoOutputStream;
import org.apache.sshd.common.io.IoSession;
import org.apache.sshd.common.io.IoWriteFuture;
import org.apache.sshd.common.io.nio2.Nio2Session;
import org.apache.sshd.common.util.buffer.Buffer;
import org.broadband_forum.obbaa.netconf.api.LogAppNames;
import org.broadband_forum.obbaa.netconf.api.NetconfSessionClosedException;
import org.broadband_forum.obbaa.netconf.api.client.NotificationListener;
import org.broadband_forum.obbaa.netconf.api.messages.CloseSessionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.CopyConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.DeleteConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigDefaultOperations;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigElement;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigErrorOptions;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigTestOptions;
import org.broadband_forum.obbaa.netconf.api.messages.GetConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetRequest;
import org.broadband_forum.obbaa.netconf.api.messages.KillSessionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.LockRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfFilter;
import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.api.messages.UnLockRequest;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.Test;
import org.mockito.stubbing.Answer;
import org.w3c.dom.Document;

public class SshNetconfClientSessionTest {

    private ChannelSubsystem m_clientChannel;
    private SshNetconfClientSession m_session;
    private IoOutputStream m_dummyStream;
    private Document m_lastSentMessage;
    private ClientSession m_clientSession;
    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(SshNetconfClientSessionTest.class, LogAppNames.NETCONF_LIB);
    private SshClient m_mockClient;
    private Nio2Session m_nio2Session;
    private AsynchronousSocketChannel m_asynSock;
    private ExecutorService m_executorService;

    @Before
    public void setUp() throws Exception {
        XMLUnit.setIgnoreWhitespace(true);
        NotificationListener notificationListener = mock(NotificationListener.class);
        m_executorService = mock(ExecutorService.class);
        doAnswer((Answer<Future<?>>) invocation -> {
            Callable callable = (Callable) invocation.getArguments()[0];
            callable.call();
            return null;
        }).when(m_executorService).submit(any(Callable.class));
        m_session = new SshNetconfClientSession(m_executorService);
        m_session.addNotificationListener(notificationListener);
        m_clientChannel = mock(ChannelSubsystem.class);
        m_clientSession = mock(ClientSession.class);
        m_nio2Session = mock(Nio2Session.class);
        m_asynSock = mock(AsynchronousSocketChannel.class);
        CloseFuture mockFuture = mock(CloseFuture.class);
        when(m_clientChannel.isOpen()).thenReturn(true);
        when(m_clientSession.isClosed()).thenReturn(false);
        when(m_clientSession.close(true)).thenReturn(mockFuture);
        when(m_clientSession.close(false)).thenReturn(mockFuture);
        m_mockClient = spy(new SshClient());
        m_session.setSshClient(m_mockClient);
        m_session.setClientChannel(m_clientChannel);
        m_session.setClientSession(m_clientSession);
        m_dummyStream = new IoOutputStream() {
            @Override
            public void close() throws IOException {

            }

            @Override
            public boolean isOpen() {
                return false;
            }

            @Override
            public boolean isClosing() {
                return false;
            }

            @Override
            public boolean isClosed() {
                return false;
            }

            @Override
            public CloseFuture close(boolean immediately) {
                return null;
            }

            @Override
            public void addCloseFutureListener(SshFutureListener<CloseFuture> listener) {

            }

            @Override
            public void removeCloseFutureListener(SshFutureListener<CloseFuture> listener) {

            }

            @Override
            public IoWriteFuture writePacket(Buffer buffer) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                baos.write(buffer.array(), buffer.rpos(), buffer.available());
                String lastMessageStr = baos.toString();
                lastMessageStr = lastMessageStr.substring(0, lastMessageStr.indexOf(NetconfResources.RPC_EOM_DELIMITER));
                try {
                    m_lastSentMessage = DocumentUtils.stringToDocument(lastMessageStr);
                    DocumentUtils.prettyPrint(m_lastSentMessage);
                } catch (NetconfMessageBuilderException e) {
                    LOGGER.error("failed while parsing reply", e);
                    fail("failed while parsing reply");
                }
                return null;
            }
        };

        when(m_clientChannel.getAsyncIn()).thenReturn(m_dummyStream);
        IoSession ioSession = mock(IoSession.class);
        when(ioSession.getLocalAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 4335));
        when(ioSession.getRemoteAddress()).thenReturn(new InetSocketAddress("135.0.0.1", 9496));
        when(m_clientSession.getIoSession()).thenReturn(ioSession);
    }

    @Test
    public void testGet() throws NetconfMessageBuilderException, InterruptedException, ExecutionException {
        URL url = Thread.currentThread().getContextClassLoader().getResource("get.xml");
        File file = new File(url.getPath());
        Document requestDocument = getDocFromFile(file);

        NetconfFilter filter = new NetconfFilter().setType(NetconfResources.SUBTREE_FILTER).addXmlFilter(
                DocumentUtils.getInstance().getElementByName(requestDocument, "top"));
        GetRequest request = new GetRequest().setFilter(filter);

        m_session.get(request);
        assertTrue(XMLUnit.compareXML(m_lastSentMessage, requestDocument).similar());
    }

    @Test
    public void testRPCWriteFailure() throws NetconfMessageBuilderException, IOException {
        GetRequest request = new GetRequest().setFilter(null);

        IoOutputStream ioOutputStream = mock(IoOutputStream.class);
        IoWriteFuture writeFuture = mock(IoWriteFuture.class);
        when(m_clientChannel.getAsyncIn()).thenReturn(ioOutputStream);
        when(ioOutputStream.writePacket(any(Buffer.class))).thenReturn(writeFuture);
        when(writeFuture.await(anyLong(), any(TimeUnit.class))).thenReturn(false);

        when(m_clientSession.isOpen()).thenReturn(true);
        doReturn(true).when(m_mockClient).isOpen();

        verify(m_clientSession, never()).close(true);
        assertFalse(m_mockClient.isClosed());

        m_session.get(request);

        verify(m_clientSession).close(true);
        assertTrue(m_mockClient.isClosed());
    }

    @Test
    public void testGetConfig() throws NetconfMessageBuilderException, InterruptedException, ExecutionException {
        URL url = Thread.currentThread().getContextClassLoader().getResource("getConfig.xml");
        File file = new File(url.getPath());
        Document requestDocument = getDocFromFile(file);

        NetconfFilter filter = new NetconfFilter().setType(NetconfResources.SUBTREE_FILTER).addXmlFilter(
                DocumentUtils.getInstance().getElementByName(requestDocument, "top"));
        ;
        GetConfigRequest request = new GetConfigRequest().setSourceRunning().setFilter(filter);
        m_session.getConfig(request);
        assertTrue(XMLUnit.compareXML(m_lastSentMessage, requestDocument).similar());
    }

    @Test
    public void testEditConfig() throws NetconfMessageBuilderException, InterruptedException, ExecutionException {
        URL url = Thread.currentThread().getContextClassLoader().getResource("editConfig.xml");
        File file = new File(url.getPath());
        Document requestDocument = getDocFromFile(file);

        EditConfigElement configElement = new EditConfigElement();
        configElement.addConfigElementContent(DocumentUtils.getInstance().getElementByName(requestDocument, "configuration"));
        EditConfigRequest request = new EditConfigRequest().setTargetRunning().setTestOption(EditConfigTestOptions.TEST_THEN_SET)
                .setErrorOption(EditConfigErrorOptions.ROLLBACK_ON_ERROR).setDefaultOperation(EditConfigDefaultOperations.MERGE)
                .setConfigElement(configElement);
        m_session.editConfig(request);
        assertTrue(XMLUnit.compareXML(m_lastSentMessage, requestDocument).similar());
    }

    @Test
    public void testCopyConfig() throws NetconfMessageBuilderException, InterruptedException, ExecutionException {
        URL url = Thread.currentThread().getContextClassLoader().getResource("copyConfig.xml");
        File file = new File(url.getPath());
        Document requestDocument = getDocFromFile(file);
        CopyConfigRequest request = new CopyConfigRequest().setTargetRunning().setSource("https://user:password@example.com/cfg/new.txt",
                true);
        m_session.copyConfig(request);
        assertTrue(XMLUnit.compareXML(m_lastSentMessage, requestDocument).similar());
    }

    @Test
    public void testDeleteConfig() throws NetconfMessageBuilderException, InterruptedException, ExecutionException {
        URL url = Thread.currentThread().getContextClassLoader().getResource("deleteConfig.xml");
        File file = new File(url.getPath());
        Document requestDocument = getDocFromFile(file);
        DeleteConfigRequest request = new DeleteConfigRequest().setTarget("startup");
        m_session.deleteConfig(request);
        assertTrue(XMLUnit.compareXML(m_lastSentMessage, requestDocument).similar());
    }

    @Test
    public void testLock() throws NetconfMessageBuilderException, InterruptedException, ExecutionException {
        URL url = Thread.currentThread().getContextClassLoader().getResource("lock.xml");
        File file = new File(url.getPath());
        Document requestDocument = getDocFromFile(file);
        LockRequest request = new LockRequest().setTarget(StandardDataStores.CANDIDATE);
        m_session.lock(request);
        assertTrue(XMLUnit.compareXML(m_lastSentMessage, requestDocument).similar());
    }

    @Test
    public void testUnLock() throws NetconfMessageBuilderException, InterruptedException, ExecutionException {
        URL url = Thread.currentThread().getContextClassLoader().getResource("unLock.xml");
        File file = new File(url.getPath());
        Document requestDocument = getDocFromFile(file);
        UnLockRequest request = new UnLockRequest().setTarget(StandardDataStores.CANDIDATE);
        m_session.unlock(request);
        assertTrue(XMLUnit.compareXML(m_lastSentMessage, requestDocument).similar());
    }

    @Test
    public void testCloseSession() throws NetconfMessageBuilderException, InterruptedException, ExecutionException {
        URL url = Thread.currentThread().getContextClassLoader().getResource("closeSession.xml");
        File file = new File(url.getPath());
        Document requestDocument = getDocFromFile(file);
        CloseSessionRequest request = new CloseSessionRequest();
        m_session.closeSession(request);
        assertTrue(XMLUnit.compareXML(m_lastSentMessage, requestDocument).similar());
    }

    @Test
    public void testSshSessionClose() throws NetconfMessageBuilderException, InterruptedException, ExecutionException, IOException {
        when(m_clientSession.isOpen()).thenReturn(true);
        doReturn(true).when(m_mockClient).isOpen();
        m_session.close();
        verify(m_clientSession, times(1)).close(true);
        assertTrue(m_session.getSshClient().isClosed());

        when(m_clientSession.isClosed()).thenReturn(true);
        CopyConfigRequest request = new CopyConfigRequest().setTargetRunning().setSource("https://user:password@example.com/cfg/new.txt",
                true);
        try {
            m_session.copyConfig(request);
            fail("Expecting NetconfSessionClosedException");
        } catch (NetconfSessionClosedException e) {
        }
    }

    @Test
    public void testKillSession() throws NetconfMessageBuilderException, InterruptedException, ExecutionException {
        URL url = Thread.currentThread().getContextClassLoader().getResource("killSession.xml");
        File file = new File(url.getPath());
        Document requestDocument = getDocFromFile(file);
        KillSessionRequest request = new KillSessionRequest().setSessionId(4);
        m_session.killSession(request);
        assertTrue(XMLUnit.compareXML(m_lastSentMessage, requestDocument).similar());
    }

    @Test
    public void testIsOpen(){
        when(m_clientChannel.isOpen()).thenReturn(true);
        assertTrue(m_session.isOpen());

        when(m_clientChannel.isOpen()).thenReturn(false);
        assertFalse(m_session.isOpen());

        m_session.setClientChannel(null);
        assertFalse(m_session.isOpen());
    }

    @Test
    public void testToString(){
        assertEquals("SshNetconfClientSession{localsocket=/127.0.0.1:4335, remotesocket=/135.0.0.1:9496, creationtime="+NetconfResources
                .DATE_TIME_FORMATTER.print(m_session.getCreationTime())+"}", m_session.toString());
    }

    @Test
    public void testCloseAsync(){
        when(m_clientSession.isOpen()).thenReturn(true);
        doReturn(true).when(m_mockClient).isOpen();
        m_session.closeAsync();
        verify(m_executorService).submit(any(Callable.class));
        verify(m_clientSession).close(true);
    }

    @Test
    public void testCloseGraceFully() throws IOException {
        when(m_clientSession.isOpen()).thenReturn(true);
        doReturn(true).when(m_mockClient).isOpen();
        m_session.closeGracefully();
        verify(m_clientSession).close(false);
    }

    @Test
    public void testSetKa() throws IOException {
        when(m_clientSession.getIoSession()).thenReturn(m_nio2Session);
        when(m_nio2Session.getSocket()).thenReturn(m_asynSock);
        when(m_asynSock.setOption(StandardSocketOptions.SO_KEEPALIVE, true)).thenReturn(m_asynSock);
        m_session.setTcpKeepAlive(true);
        verify(m_asynSock).setOption(StandardSocketOptions.SO_KEEPALIVE, true);

        doThrow(new RuntimeException("Exception setting TCP KA")).when(m_asynSock).setOption(StandardSocketOptions.SO_KEEPALIVE, false);
        try {
            m_session.setTcpKeepAlive(false);
        } catch(Exception e) {
            assertEquals("Exception setting TCP KA", e.getMessage());
        }
    }
}
