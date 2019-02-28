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
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;

public class SshNetconfClientSessionListener implements SshFutureListener<IoReadFuture> {
    ByteArrayOutputStream m_baosOut = new ByteArrayOutputStream();
    private ChannelSubsystem m_clientChannel;
    private SshNetconfClientSession m_clientSession;
    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(SshNetconfClientSessionListener.class, LogAppNames.NETCONF_LIB);


    public SshNetconfClientSessionListener(ChannelSubsystem channel, SshNetconfClientSession clientSession) {
        this.m_clientChannel = channel;
        this.m_clientSession = clientSession;
    }

    @Override
    public void operationComplete(IoReadFuture future) {
        try {
            if (!(m_clientChannel.isClosed() || m_clientChannel.isClosing())) {
                future.verify();
                Buffer buffer = future.getBuffer();
                m_baosOut.write(buffer.array(), buffer.rpos(), buffer.available());
                String rpcReply = m_baosOut.toString();
                Document replyDoc = null;
                try {
                    replyDoc = m_clientSession.getCodec().decode(rpcReply);
                    if (replyDoc != null) {
                        m_clientSession.responseRecieved(replyDoc);
                        m_baosOut = new ByteArrayOutputStream();
                    }
                } catch (MessageToolargeException e) {
                    LOGGER.warn("Closing netconf session, incoming message too long to decode", e);
                    m_clientSession.close();
                }
                buffer.rpos(buffer.rpos() + buffer.available());
                buffer.compact();
                m_clientChannel.getAsyncOut().read(buffer).addListener(this);
            }
        } catch (NetconfMessageBuilderException | IOException e) {
            LOGGER.error("Error while processing request ", e);
        }
    }

}
