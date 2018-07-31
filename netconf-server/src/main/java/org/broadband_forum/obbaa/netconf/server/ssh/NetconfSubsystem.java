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

package org.broadband_forum.obbaa.netconf.server.ssh;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.apache.sshd.common.channel.BufferedIoOutputStream;
import org.apache.sshd.common.future.CloseFuture;
import org.apache.sshd.common.future.SshFutureListener;
import org.apache.sshd.common.io.IoInputStream;
import org.apache.sshd.common.io.IoOutputStream;
import org.apache.sshd.common.util.buffer.ByteArrayBuffer;
import org.apache.sshd.server.AsyncCommand;
import org.apache.sshd.server.ChannelSessionAware;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.channel.ChannelDataReceiver;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.session.ServerSession;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfHelloMessage;
import org.broadband_forum.obbaa.netconf.api.messages.PojoToDocumentTransformer;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerMessageListener;
import org.broadband_forum.obbaa.netconf.api.server.NetconfSessionIdProvider;
import org.broadband_forum.obbaa.netconf.api.server.ServerMessageHandler;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;

public class NetconfSubsystem implements AsyncCommand, ChannelDataReceiver, ChannelSessionAware {
    private static final String USER = "USER";
    public static final String HELLO_MESSAGE_NOT_RECIEVED = "hello message not received";
    private static final int MESSAGE_MAX_SIZE = 100000;
    private static Map<Integer, SshSessionInfo> m_sessions = new ConcurrentHashMap<>();
    private static final Logger LOGGER = Logger.getLogger(NetconfSubsystem.class);

    private IoOutputStream m_out;
    @SuppressWarnings("unused")
    private IoOutputStream m_in;
    private ExitCallback m_exitCallback;
    @SuppressWarnings("unused")
    private IoOutputStream m_err;
    private NetconfServerMessageListener m_netconfServerMessageListener;
    private ServerMessageHandler m_serverMessageHandler;
    private Set<String> m_caps;
    private boolean m_helloRecieved = false;
    private String m_currentDelimiter = NetconfResources.RPC_EOM_DELIMITER;
    private Set<String> m_clientCaps;
    private SshServerNetconfMessageHandler m_messageHandler;
    private NetconfClientInfo m_clientInfo;
    private Integer m_sessionId;
    private ChannelSession m_session;
    private Environment m_environment;
    private StringBuilder m_buffer = new StringBuilder(MESSAGE_MAX_SIZE);
    private final NetconfSessionIdProvider m_sessionIdProvider;

    public NetconfSubsystem(NetconfServerMessageListener netconfServerMessageListener,
                            ServerMessageHandler serverMessageHandler, Set<String> caps, NetconfSessionIdProvider
                                    sessionIdProvider) {
        m_netconfServerMessageListener = netconfServerMessageListener;
        m_serverMessageHandler = serverMessageHandler;
        m_caps = caps;
        m_sessionIdProvider = sessionIdProvider;
    }

    public void sendHelloToClient() throws IOException {
        try {
            Integer sessionId = m_sessionIdProvider.getNewSessionId();
            PojoToDocumentTransformer builder = new PojoToDocumentTransformer().newServerHelloMessage(m_caps,
                    sessionId);
            Document doc = builder.build();

            if (LOGGER.isDebugEnabled()) {
                try {
                    LOGGER.debug("Sending hello to client: " + DocumentUtils.documentToString(doc));
                } catch (Exception e) {
                    LOGGER.error("Error while logging hello message", e);
                }
            }

            byte[] helloBytes = DocumentToPojoTransformer.addRpcDelimiter(DocumentToPojoTransformer
                    .getBytesFromDocument(doc));
            m_out.write(new ByteArrayBuffer(helloBytes));
            LOGGER.info("after sending hello, m_out state: " + m_out.isClosed() + ", " + m_out.isClosing() + ", " +
                    "sessionId: " + sessionId);
            SocketAddress remoteAddress = m_session.getSession().getIoSession().getRemoteAddress();
            String remoteHost = NetconfClientInfo.NON_INET_HOST;
            String remotePort = NetconfClientInfo.NON_INET_PORT;
            if (remoteAddress instanceof InetSocketAddress) {
                remoteHost = ((InetSocketAddress) remoteAddress).getHostName();
                remotePort = String.valueOf(((InetSocketAddress) remoteAddress).getPort());
            }
            SshSessionInfo info = new SshSessionInfo().setSessionId(sessionId).setSubsystem(this).setUser
                    (m_environment.getEnv().get(USER));

            m_sessions.put(sessionId, info);
            m_sessionId = sessionId;
            ServerSession currentServerSession = m_session.getServerSession();
            m_clientInfo = new NetconfClientInfo(info.getUser(), sessionId, currentServerSession.getIoSession().getId
                    ()).setRemoteHost(remoteHost).setRemotePort(remotePort);
        } catch (Exception e) {
            LOGGER.error("Error while sending hello, closing connection", e);
            m_exitCallback.onExit(1, "could not send hello message from netconf server to netconf client, closing " +
                    "connection");
        }
    }

    @Override
    public void setOutputStream(OutputStream out) {
    }

    @Override
    public void setInputStream(InputStream in) {
    }

    @Override
    public void setErrorStream(OutputStream err) {
    }

    @Override
    public void setExitCallback(ExitCallback callback) {
        this.m_exitCallback = callback;

    }

    public static void removeSession(int sessionID) {
        m_sessions.remove(sessionID);
    }

    @Override
    public void destroy() {
        LOGGER.info("Netconf subsystem destroyed");
        m_netconfServerMessageListener.sessionClosed("Session Destroyed", m_sessionId);
        m_sessions.remove(m_sessionId);
    }

    public void kill(int exitCode, String exitMessage) {
        destroy();
        m_exitCallback.onExit(exitCode, exitMessage);
    }

    @Override
    public String toString() {
        return "NetconfSubsystem [m_caps=" + m_caps + ", m_helloRecieved=" + m_helloRecieved + ", " +
                "m_currentDelimiter=" + m_currentDelimiter
                + ", m_clientCaps=" + m_clientCaps + "]";
    }

    public static synchronized boolean killSession(Integer sessionId) {
        NetconfSubsystem session = m_sessions.get(sessionId).getSubsystem();
        if (session != null) {
            session.kill(1, "kill-m_session called on this m_session");
            return true;
        }
        return false;
    }

    @Override
    public void setChannelSession(ChannelSession session) {
        this.m_session = session;
    }

    @Override
    public int data(final ChannelSession channel, byte[] buf, int start, int len) throws IOException {
        String rpcMessage = new String(buf, start, len);
        if (rpcMessage.endsWith(m_currentDelimiter)) {
            if (m_buffer.length() > 0) {
                rpcMessage = m_buffer.toString() + rpcMessage;
                m_buffer.setLength(0);
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("RPC Message is : " + rpcMessage);
            }
            if (!m_helloRecieved) {
                rpcMessage = rpcMessage.substring(0, rpcMessage.indexOf(NetconfResources.RPC_EOM_DELIMITER));
                Document rpcDoc = null;
                try {
                    rpcDoc = DocumentUtils.stringToDocument(rpcMessage);
                } catch (NetconfMessageBuilderException exp) {
                    LOGGER.error("Error during procession request : ", exp);
                    m_exitCallback.onExit(1, HELLO_MESSAGE_NOT_RECIEVED);
                    m_netconfServerMessageListener.sessionClosed(HELLO_MESSAGE_NOT_RECIEVED, m_sessionId);
                }

                if (!NetconfResources.HELLO.equals(rpcDoc.getFirstChild().getLocalName()) && isBase10NS(rpcDoc
                        .getFirstChild())) {
                    LOGGER.error(HELLO_MESSAGE_NOT_RECIEVED);
                    m_exitCallback.onExit(1, HELLO_MESSAGE_NOT_RECIEVED);
                    m_netconfServerMessageListener.sessionClosed(HELLO_MESSAGE_NOT_RECIEVED, m_sessionId);
                } else {
                    LOGGER.info("Hello Message received from Client Successfully, m_out state: " + m_out.isClosed() +
                            ", " + m_out.isClosing());
                    m_helloRecieved = true;
                    NetconfHelloMessage hello = DocumentToPojoTransformer.getHelloMessage(rpcDoc);
                    this.m_clientCaps = hello.getCapabilities();
                    if (this.m_clientCaps.contains(NetconfResources.NETCONF_BASE_CAP_1_1)
                            && this.m_caps.contains(NetconfResources.NETCONF_BASE_CAP_1_1)) {
                        m_currentDelimiter = NetconfResources.RPC_CHUNKED_DELIMITER;
                        m_messageHandler = new ChunkedNetconfMessageHandler(m_netconfServerMessageListener, m_out,
                                m_exitCallback,
                                m_serverMessageHandler, channel);
                    } else {
                        m_messageHandler = new EomNetconfMessageHandler(m_netconfServerMessageListener, m_out,
                                m_exitCallback,
                                m_serverMessageHandler, channel);
                    }
                    m_messageHandler.onHello(m_clientInfo, this.m_clientCaps);
                }
            } else {
                try {
                    m_messageHandler.processRequest(rpcMessage);
                } catch (Exception exp) {
                    LOGGER.error("Error during processing request : ", exp);
                    throw new IOException(exp);

                }
            }
        } else {
            m_buffer.append(rpcMessage);
        }
        return len;
    }

    private boolean isBase10NS(Node node) {
        return NetconfResources.NETCONF_RPC_NS_1_0.equals(node.getNamespaceURI());
    }

    @Override
    public void close() throws IOException {
        LOGGER.warn("closing NetconfSubSystem with session: " + m_clientInfo + ", clientSessionId: " + m_clientInfo
                .getClientSessionId() + ", m_out: " + m_out.hashCode(), new Throwable());
        m_out.close(false).addListener(new SshFutureListener<CloseFuture>() {
            public void operationComplete(CloseFuture future) {
                LOGGER.info("completed closing NetconfSubSystem with session: " + m_sessionId + ", m_out: " + m_out
                        .hashCode());
                m_exitCallback.onExit(0);
            }
        });
    }

    @Override
    public void setIoErrorStream(IoOutputStream errOutputStream) {
        this.m_err = new BufferedIoOutputStream(errOutputStream);
    }

    @Override
    public void setIoInputStream(IoInputStream ioInputStream) {
    }

    @Override
    public void setIoOutputStream(IoOutputStream ioOutputStream) {
        this.m_out = new BufferedIoOutputStream(ioOutputStream);
    }

    @Override
    public void start(Environment env) throws IOException {
        m_session.setDataReceiver(this);
        this.m_environment = env;
        sendHelloToClient();
    }

    public boolean isHelloReceived() {
        return m_helloRecieved;
    }

    public void setSessionId(int sessionId) {
        m_sessionId = sessionId;
    }
}