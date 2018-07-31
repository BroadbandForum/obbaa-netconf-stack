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

import org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.apache.log4j.Logger;
import org.apache.sshd.client.channel.ChannelSubsystem;
import org.apache.sshd.common.future.SshFutureListener;
import org.apache.sshd.common.io.IoReadFuture;
import org.apache.sshd.common.util.buffer.Buffer;
import org.w3c.dom.Document;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class SshNetconfChunkedClientSessionListener implements SshFutureListener<IoReadFuture> {
    ByteArrayOutputStream m_baosOut = new ByteArrayOutputStream();
    private ChannelSubsystem m_clientChannel;
    private SshNetconfClientSession m_clientSession;

    private static final Logger LOGGER = Logger.getLogger(SshNetconfChunkedClientSessionListener.class);

    public SshNetconfChunkedClientSessionListener(ChannelSubsystem clientChannel, SshNetconfClientSession
            clientSession) {
        this.m_clientChannel = clientChannel;
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
                if (rpcReply.endsWith(NetconfResources.RPC_CHUNKED_DELIMITER)) {
                    rpcReply = DocumentToPojoTransformer.processChunkedMessage(rpcReply);
                    Document replyDoc = DocumentUtils.stringToDocument(rpcReply);
                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace(String.format("NC Response received from %s : %s", m_clientChannel.getSession()
                                .getIoSession()
                                .getRemoteAddress(), rpcReply));
                    }
                    m_clientSession.responseRecieved(replyDoc);
                    m_baosOut = new ByteArrayOutputStream();
                }
                buffer.rpos(buffer.rpos() + buffer.available());
                buffer.compact();
                m_clientChannel.getAsyncOut().read(future.getBuffer()).addListener(this);
            }
        } catch (IOException | NetconfMessageBuilderException e) {
            LOGGER.error("Error while processing async request ", e);
        }
    }

}
