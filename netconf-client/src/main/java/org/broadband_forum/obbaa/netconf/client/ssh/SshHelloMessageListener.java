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

import static org.broadband_forum.obbaa.netconf.api.util.ByteBufUtils.releaseByteBuf;
import static org.broadband_forum.obbaa.netconf.api.util.ByteBufUtils.unpooledHeapByteBuf;
import static org.broadband_forum.obbaa.netconf.api.util.DocumentUtils.documentToPrettyString;

import java.io.IOException;

import org.apache.sshd.client.channel.ChannelSubsystem;
import org.apache.sshd.common.channel.Channel;
import org.apache.sshd.common.channel.ChannelListener;
import org.apache.sshd.common.future.SshFutureListener;
import org.apache.sshd.common.io.IoReadFuture;
import org.apache.sshd.common.util.buffer.Buffer;
import org.broadband_forum.obbaa.netconf.api.LogAppNames;
import org.broadband_forum.obbaa.netconf.api.codec.v2.DocumentInfo;
import org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfHelloMessage;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.w3c.dom.Document;

import com.google.common.annotations.VisibleForTesting;

import io.netty.buffer.ByteBuf;

public class SshHelloMessageListener implements SshFutureListener<IoReadFuture> {
    private ByteBuf m_byteBuf;
    private ChannelSubsystem m_clientChannel;
    private SshNetconfClientSession m_clientSession;

    private Object m_lockObject = new Object();
    private boolean m_isHelloMessageReceived = false;

    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(SshHelloMessageListener.class, LogAppNames.NETCONF_LIB);

    public SshHelloMessageListener(ChannelSubsystem clientChannel, SshNetconfClientSession netconfSession) {
        this.m_clientChannel = clientChannel;
        this.m_clientSession = netconfSession;
        m_clientChannel.addChannelListener(new ChannelListener() {
            @Override
            public void channelClosed(Channel channel, Throwable reason) {
                synchronized (m_lockObject) {
                    m_lockObject.notifyAll();
                    releaseByteBuf(m_byteBuf);
                }
            }
        });
        m_byteBuf = unpooledHeapByteBuf();
    }

    @Override
    public void operationComplete(IoReadFuture future) {
        if (m_clientChannel.isOpen()) {
            try {
                future.verify();
                Buffer buffer = future.getBuffer();
                m_byteBuf.writeBytes(buffer.array(), buffer.rpos(), buffer.available());
                DocumentInfo documentInfo = m_clientSession.getCodec().decode(m_byteBuf);
                if (documentInfo != null && documentInfo.getDocument() != null) {
                    Document replyDoc = documentInfo.getDocument();
                    if (!NetconfResources.HELLO.equals(replyDoc.getFirstChild().getNodeName())) {
                        LOGGER.info("Invalid hello from server closing the channel : {}", LOGGER.sensitiveData(documentToPrettyString(replyDoc)));
                        // If you get a message which is not hello, close the session.
                        closeChannel();
                        releaseByteBuf(m_byteBuf);
                    } else {
                        // Let the client session take necessary actions for hello message
                        m_clientSession.responseRecieved(documentInfo);
                        buffer.rpos(buffer.rpos() + buffer.available());
                        buffer.compact();
                        NetconfHelloMessage hello = DocumentToPojoTransformer.getHelloMessage(replyDoc);
                        if (hello.getCapabilities().contains(NetconfResources.NETCONF_BASE_CAP_1_1)
                                && m_clientSession.getClientCapability(NetconfResources.NETCONF_BASE_CAP_1_1)) {
                            m_clientSession.useChunkedFraming();
                        }
                        m_clientChannel.getAsyncOut().read(buffer)
                                .addListener(new SshNetconfClientSessionListener(m_clientChannel, m_clientSession, m_byteBuf));

                        /* Now hello-message exchange is done and codec is switched accordingly.
                         * its time to notify the nc client dispatcher to send the requests ,who were waiting for
                         * m_lockObject to be released (indicating the completion of hello-exchange)
                         */
                        m_isHelloMessageReceived = true;
                        synchronized (m_lockObject) {
                            m_lockObject.notifyAll();
                        }
                    }
                } else {
                    buffer.rpos(buffer.rpos() + buffer.available());
                    buffer.compact();
                    m_clientChannel.getAsyncOut().read(buffer).addListener(this);
                }
            } catch (Exception e) {
                LOGGER.error("Error while processing message, closing the session", e);
                try {
                    closeChannel();
                    releaseByteBuf(m_byteBuf);
                } catch (IOException ex) {
                    LOGGER.error("Error while waiting for the asynchronous operation to complete", ex);
                }
            }
        } else {
            LOGGER.trace("Ignoring a ReadFuture because the channel is closed / is closing");
        }
    }

    private void closeChannel() throws IOException {
        if (m_clientChannel.isOpen()) {
            m_clientChannel.close(true).await();
        }
    }

    /**
     * await for hello message received
     *
     * @throws InterruptedException
     * @param configuredTimeout
     */
    public boolean await(long configuredTimeout) throws InterruptedException {
        if (!m_isHelloMessageReceived) {
            synchronized (m_lockObject) {
                m_lockObject.wait(configuredTimeout);
            }
        }
        return m_isHelloMessageReceived;
    }

    /**
     * For UTs
     *
     * @param helloMessageRecieved
     */
    public void setHelloMessageRecieved(boolean helloMessageRecieved) {
        m_isHelloMessageReceived = helloMessageRecieved;
    }

    @VisibleForTesting
    public ByteBuf getByteBuf() {
        return m_byteBuf;
    }

}
