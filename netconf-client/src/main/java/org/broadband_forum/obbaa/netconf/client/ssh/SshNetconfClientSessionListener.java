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

import static org.broadband_forum.obbaa.netconf.api.util.ByteBufUtils.reInitializeByteBuf;
import static org.broadband_forum.obbaa.netconf.api.util.ByteBufUtils.releaseByteBuf;

import java.io.IOException;

import org.apache.sshd.client.channel.ChannelSubsystem;
import org.apache.sshd.common.future.SshFutureListener;
import org.apache.sshd.common.io.IoReadFuture;
import org.apache.sshd.common.util.buffer.Buffer;
import org.broadband_forum.obbaa.netconf.api.LogAppNames;
import org.broadband_forum.obbaa.netconf.api.codec.v2.DocumentInfo;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.w3c.dom.Document;

import com.google.common.annotations.VisibleForTesting;

import io.netty.buffer.ByteBuf;

public class SshNetconfClientSessionListener implements SshFutureListener<IoReadFuture> {
    private ByteBuf m_byteBuf;
    private ChannelSubsystem m_clientChannel;
    private SshNetconfClientSession m_clientSession;
    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(SshNetconfClientSessionListener.class, LogAppNames.NETCONF_LIB);


    public SshNetconfClientSessionListener(ChannelSubsystem channel, SshNetconfClientSession clientSession, ByteBuf byteBuf) {
        this.m_clientChannel = channel;
        this.m_clientSession = clientSession;
        m_byteBuf = byteBuf;
    }

    @Override
    public void operationComplete(IoReadFuture future) {
        if (!(m_clientChannel.isClosed() || m_clientChannel.isClosing())) {
            Buffer buffer = null;
            try {
                future.verify();
                buffer = future.getBuffer();
                m_byteBuf.writeBytes(buffer.array(), buffer.rpos(), buffer.available());
                try {
                    boolean readFurther = true;
                    while (readFurther) {
                        DocumentInfo documentInfo = m_clientSession.getCodec().decode(m_byteBuf);
                        if (documentInfo != null && documentInfo.getDocument() != null) {
                            m_clientSession.responseRecieved(documentInfo);
                        } else {
                            readFurther = false;
                        }
                    }
                } catch (NetconfMessageBuilderException e) {
                    LOGGER.error("Error - got malformed XML from {}", m_clientSession ,e);
                }
            } catch (Exception e) {
                LOGGER.error("Error while processing message, closing the session", e);
                try {
                    m_clientSession.close();
                    releaseByteBuf(m_byteBuf);
                } catch (IOException ex) {
                    LOGGER.error("Error while closing the session", ex);
                }
            } finally {
                if (buffer != null) {
                    buffer.rpos(buffer.rpos() + buffer.available());
                    buffer.compact();
                }
                m_byteBuf = reInitializeByteBuf(m_byteBuf);
                m_clientChannel.getAsyncOut().read(buffer).addListener(this);
            }
        }
    }

    @VisibleForTesting
    public ByteBuf getByteBuf() {
        return m_byteBuf;
    }
}