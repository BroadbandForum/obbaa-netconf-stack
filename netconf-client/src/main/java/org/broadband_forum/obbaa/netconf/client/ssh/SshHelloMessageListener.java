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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.sshd.client.channel.ChannelSubsystem;
import org.apache.sshd.common.future.SshFutureListener;
import org.apache.sshd.common.io.IoReadFuture;
import org.apache.sshd.common.util.buffer.Buffer;
import org.w3c.dom.Document;

import org.broadband_forum.obbaa.netconf.api.LogAppNames;
import org.broadband_forum.obbaa.netconf.api.MessageToolargeException;
import org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfHelloMessage;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;

public class SshHelloMessageListener implements SshFutureListener<IoReadFuture> {
    ByteArrayOutputStream m_baosOut = new ByteArrayOutputStream();
    private ChannelSubsystem m_clientChannel;
    private SshNetconfClientSession m_clientSession;

    private Object m_lockObject = new Object();
    private boolean m_isHelloMessageReceived = false;

    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(SshHelloMessageListener.class, LogAppNames.NETCONF_LIB);

    public SshHelloMessageListener(ChannelSubsystem clientChannel, SshNetconfClientSession netconfSession) {
        this.m_clientChannel = clientChannel;
        this.m_clientSession = netconfSession;
    }

    @Override
    public void operationComplete(IoReadFuture future) {
        try {
            future.verify();
            Buffer buffer = future.getBuffer();
            m_baosOut.write(buffer.array(), buffer.rpos(), buffer.available());
            if (m_baosOut.toString().endsWith(NetconfResources.RPC_EOM_DELIMITER)) {
                String rpcReply = m_baosOut.toString();
                Document replyDoc = m_clientSession.getCodec().decode(rpcReply);
                if (!NetconfResources.HELLO.equals(replyDoc.getFirstChild().getNodeName())) {
                    LOGGER.info("Invalid hello from server closing the channel : {}", LOGGER.sensitiveData(rpcReply));
                    // If you get a message which is not hello, close the session.
                    m_clientChannel.close(true).await();
                } else {
                    // Let the client session take necessary actions for hello message
                    m_clientSession.responseRecieved(replyDoc);
                    m_baosOut = new ByteArrayOutputStream();
                    buffer.rpos(buffer.rpos() + buffer.available());
                    buffer.compact();
                    NetconfHelloMessage hello = DocumentToPojoTransformer.getHelloMessage(replyDoc);
                    m_isHelloMessageReceived = true;
                    synchronized (m_lockObject) {
                        m_lockObject.notify();
                    }
                    if (hello.getCapabilities().contains(NetconfResources.NETCONF_BASE_CAP_1_1)
                            && m_clientSession.getClientCapability(NetconfResources.NETCONF_BASE_CAP_1_1)) {
                        m_clientSession.useChunkedFraming();
                    }
                    m_clientChannel.getAsyncOut().read(buffer)
                            .addListener(new SshNetconfClientSessionListener(m_clientChannel, m_clientSession));

                }

            } else {
                buffer.rpos(buffer.rpos() + buffer.available());
                buffer.compact();
                m_clientChannel.getAsyncOut().read(buffer).addListener(this);
            }

        } catch (IOException | NetconfMessageBuilderException e) {
            LOGGER.error("Error while processing message ", e);
        } catch (MessageToolargeException e) {
            LOGGER.warn("Too long hello from server closing the channel", e);
            try {
                m_clientChannel.close(true).await();
            } catch (IOException e1) {
                LOGGER.error("Error while processing message ", e1);
            }
        }

    }

    /**
     * await for hello message received
     *
     * @throws InterruptedException
     */
    public void await() throws InterruptedException {
        if (!m_isHelloMessageReceived) {
            synchronized (m_lockObject) {
                m_lockObject.wait();
            }
        }
    }

    /**
     * For UTs
     *
     * @param helloMessageRecieved
     */
    public void setHelloMessageRecieved(boolean helloMessageRecieved) {
        m_isHelloMessageReceived = helloMessageRecieved;
    }
}
