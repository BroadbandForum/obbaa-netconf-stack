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

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;

public class SecureNetconfClientHandler extends SimpleChannelInboundHandler<String> {
    private static final Logger LOGGER = Logger.getLogger(SecureNetconfClientHandler.class);
    private TlsNettyChannelNetconfClientSession m_clientSession;

    public SecureNetconfClientHandler(TlsNettyChannelNetconfClientSession clientSession) {
        m_clientSession = clientSession;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        Document responseDoc = DocumentUtils.stringToDocument(msg);
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(String.format("NC Response received from %s : %s", ctx.channel().remoteAddress(), msg));
        }
        m_clientSession.responseRecieved(responseDoc);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        if (m_clientSession != null) {
            m_clientSession.sessionClosed();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.debug("Exception caught : ", cause);
        ctx.close();
        if (m_clientSession != null) {
            m_clientSession.sessionClosed();
        }
    }

    public void setNetconfSession(TlsNettyChannelNetconfClientSession session) {
        this.m_clientSession = session;

    }

    public void setServerSocketChannel(SocketChannel ch) {
        m_clientSession.setServerChannel(ch);
    }
}
