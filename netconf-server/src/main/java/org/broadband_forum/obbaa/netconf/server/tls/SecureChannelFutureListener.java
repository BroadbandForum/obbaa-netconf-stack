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

import org.apache.log4j.Logger;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.CompletableMessage;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

class SecureChannelFutureListener implements ChannelFutureListener {

    private static final Logger LOGGER = Logger.getLogger(SecureChannelFutureListener.class);

    private CompletableMessage m_message;
    private NetconfClientInfo m_clientInfo;

    public SecureChannelFutureListener(CompletableMessage message, NetconfClientInfo clientInfo) {
        m_message = message;
        m_clientInfo = clientInfo;
    }

    @Override
    public void operationComplete(ChannelFuture future) throws Exception {
        if (future.isSuccess()) {
            m_message.getMessageSentFuture().complete("Response Sent");
        } else {
            LOGGER.error("Response not sent to " + m_clientInfo.toString(), future.cause());
            m_message.getMessageSentFuture().completeExceptionally(future.cause());
        }
    }
}
