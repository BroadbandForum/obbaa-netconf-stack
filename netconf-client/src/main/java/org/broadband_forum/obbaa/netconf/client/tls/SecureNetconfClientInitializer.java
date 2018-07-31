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

import org.broadband_forum.obbaa.netconf.api.authentication.AuthenticationListener;
import org.broadband_forum.obbaa.netconf.api.authentication.FailureInfo;
import org.broadband_forum.obbaa.netconf.api.authentication.PointOfFailure;
import org.broadband_forum.obbaa.netconf.api.authentication.SuccessInfo;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfDelimiters;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import org.apache.log4j.Logger;

import javax.net.ssl.SSLEngine;

import java.net.InetSocketAddress;

public class SecureNetconfClientInitializer extends ChannelInitializer<SocketChannel> {

    private final SslContext m_sslCtx;
    private SecureNetconfClientHandler m_clientHandler;
    private static final Logger LOGGER = Logger.getLogger(SecureNetconfClientInitializer.class);
    protected AuthenticationListener m_authenticationListener;

    public SecureNetconfClientInitializer(SslContext sslCtx, SecureNetconfClientHandler clientHandler,
                                          AuthenticationListener authenticationListener) {
        this.m_sslCtx = sslCtx;
        this.m_clientHandler = clientHandler;
        m_authenticationListener = authenticationListener;
    }

    @Override
    public void initChannel(final SocketChannel ch) throws Exception {
        LOGGER.debug("Channel initialised " + ch);
        ChannelPipeline pipeline = ch.pipeline();

        SSLEngine sslEngine = m_sslCtx.newEngine(ch.alloc());
        SslHandler sslHanlder = new SslHandler(sslEngine);
        pipeline.addLast(sslHanlder);

        // On top of the SSL handler, add the text line codec.
        pipeline.addLast(new DelimiterBasedFrameDecoder(Integer.MAX_VALUE, NetconfDelimiters.rpcEndOfMessageDelimiter
                ()));

        pipeline.addLast(new StringDecoder());
        pipeline.addLast(new StringEncoder());
        //set the channel
        m_clientHandler.setServerSocketChannel(ch);
        pipeline.addLast(m_clientHandler);

        sslHanlder.handshakeFuture().addListener(new GenericFutureListener<Future<Channel>>() {

            @Override
            public void operationComplete(Future<Channel> future) throws Exception {
                if (future.isSuccess()) {
                    if (m_authenticationListener != null) {
                        InetSocketAddress remoteAddress = ((SocketChannel) ch).remoteAddress();
                        m_authenticationListener.authenticationSucceeded(new SuccessInfo()
                                .setIp(remoteAddress.getAddress().getHostAddress()).setPort(remoteAddress.getPort()));
                    }
                } else {
                    if (m_authenticationListener != null) {
                        InetSocketAddress remoteAddress = ((SocketChannel) ch).remoteAddress();
                        m_authenticationListener.authenticationFailed(new FailureInfo().setIp(remoteAddress
                                .getAddress().getHostAddress())
                                .setPort(remoteAddress.getPort()).setPointOfFailure(PointOfFailure.client));
                    }
                }

            }

        });
    }

}
