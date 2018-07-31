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

import io.netty.channel.Channel;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.net.InetSocketAddress;
import java.security.cert.X509Certificate;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;

import org.apache.log4j.Logger;

import org.broadband_forum.obbaa.netconf.api.authentication.AuthenticationListener;
import org.broadband_forum.obbaa.netconf.api.authentication.FailureInfo;
import org.broadband_forum.obbaa.netconf.api.authentication.PointOfFailure;
import org.broadband_forum.obbaa.netconf.api.authentication.SuccessInfo;
import org.broadband_forum.obbaa.netconf.api.client.CallHomeListener;
import org.broadband_forum.obbaa.netconf.api.client.NotificationListener;
import org.broadband_forum.obbaa.netconf.api.messages.LogUtil;
import org.broadband_forum.obbaa.netconf.api.x509certificates.CertificateUtil;
import org.broadband_forum.obbaa.netconf.api.x509certificates.PeerCertificateException;

public final class SslFutureChannelListener implements GenericFutureListener<Future<Channel>> {

    private static final Logger LOGGER = Logger.getLogger(SslFutureChannelListener.class);
    private SSLEngine m_sSLEngine;
    private SocketChannel m_socketChannel;
    private AuthenticationListener m_authenticationListener;
    private boolean m_selfSigned;
    private Set<String> m_capabilities;
    private ExecutorService m_executorService;
    private NotificationListener m_notificationListener;
    private final ExecutorService m_callHomeExecutorService;
    private CallHomeListener m_callHomeListener;

    public SslFutureChannelListener(SSLEngine sSLEngine, SocketChannel channel, AuthenticationListener
            authenticationListener,
                                    boolean selfSigned, Set<String> capabilities, NotificationListener
                                            notificationListener, ExecutorService executorService,
                                    ExecutorService callHomeExecutorService, CallHomeListener callHomeListener) {
        m_sSLEngine = sSLEngine;
        m_socketChannel = channel;
        m_authenticationListener = authenticationListener;
        m_selfSigned = selfSigned;
        m_capabilities = capabilities;
        m_notificationListener = notificationListener;
        m_executorService = executorService;
        m_callHomeExecutorService = callHomeExecutorService;
        m_callHomeListener = callHomeListener;
    }

    @Override
    public void operationComplete(Future<Channel> future) {
        try {
            if (future.isSuccess()) {
                LOGGER.debug(String.format("Authentication is successful on channel %s", m_socketChannel));
                handleAuthenticationSuccess();
            } else {
                LOGGER.debug(String.format("Authentication failed on channel %s", m_socketChannel));
                handleAuthenticationFailure(future);
            }
        } catch (Exception e) {
            logAndCloseChannel(e);
            throw e;
        }
    }

    private void logAndCloseChannel(Exception e) {
        LOGGER.error(String.format("Error while handling authentication, closing the channel %s", m_socketChannel), e);
        try {
            m_socketChannel.close().sync();
        } catch (InterruptedException e1) {
            throw new RuntimeException("Interrupted while closing channel", e1);
        }
    }

    private void handleAuthenticationSuccess() {
        final X509Certificate peerX509Certificate = CertificateUtil.getPeerX509Certifcate(m_sSLEngine.getSession());
        if (m_authenticationListener != null) {
            InetSocketAddress remoteAddress = ((SocketChannel) m_socketChannel).remoteAddress();
            SuccessInfo successInfo = new SuccessInfo().setIp(remoteAddress.getAddress().getHostAddress()).setPort
                    (remoteAddress.getPort())
                    .setPeerCertificate(peerX509Certificate);
            m_authenticationListener.authenticationSucceeded(successInfo);
        }
        // Add netconf handler to the pipeline
        final TlsNettyChannelNetconfClientSession session = new TlsNettyChannelNetconfClientSession(m_socketChannel,
                m_executorService);
        SecureNetconfClientHandler clientHandler = SecureNetconfClientHandlerFactory.getInstance()
                .getSecureNetconfClientHandler(session);

        session.addNotificationListener(m_notificationListener);
        clientHandler.setNetconfSession(session);
        session.sendHelloMessage(m_capabilities);
        m_socketChannel.pipeline().addLast(clientHandler);
        notifyCallHomeListener(session, peerX509Certificate);
    }

    private void notifyCallHomeListener(final TlsNettyChannelNetconfClientSession session, final X509Certificate
            peerX509Certificate) {
        try {
            // inform call home listeners
            LogUtil.logDebug(LOGGER, "Calling call-home listener to inform new call home connection with " +
                            "remote-address: %s",
                    session.getRemoteAddress());

            m_callHomeExecutorService.execute(() -> {
                try {
                    // Let the client know that some server called home and is ready to talk netconf
                    m_callHomeListener.connectionEstablished(session, null, peerX509Certificate, m_selfSigned);
                } catch (Exception e) {
                    logAndCloseChannel(e);
                    throw e;
                }
            });
        } catch (RejectedExecutionException ree) {
            LOGGER.warn(String.format("executor service could not notify call-home listener"
                    + " about connection established for the session %s, closing the connection", session
                    .getRemoteAddress().toString()));
            logAndCloseChannel(ree);
        }
    }

    private void handleAuthenticationFailure(Future<Channel> future) {
        String ip = m_socketChannel.remoteAddress().getAddress().getHostAddress();
        int port = m_socketChannel.remoteAddress().getPort();
        if (isSslHandshakeTimeException(future.cause())) {
            LOGGER.error("Authentication failed. SSL Handshake timeout for the connection [ip:" + ip + ", port:" +
                    port + "]");
            return;
        }
        if (m_authenticationListener != null) {
            PeerCertificateException certificateException = getPeerCertificatException(future.cause());
            FailureInfo failureInfo = new FailureInfo().setIp(ip).setPort(port);
            if (certificateException != null) {
                // if peer certificate is not trusted, then extract from exception
                failureInfo.setPointOfFailure(PointOfFailure.client);
                X509Certificate peerCertificate = getPeerCertificateFromException(certificateException);
                failureInfo.setPeerCertificate(peerCertificate);
            } else {
                // if peer certificate is trusted, then extract from handshake session
                failureInfo.setPointOfFailure(PointOfFailure.server);
                X509Certificate peerCertificate = CertificateUtil.getPeerX509Certifcate(m_sSLEngine
                        .getHandshakeSession());
                failureInfo.setPeerCertificate(peerCertificate);
            }
            m_authenticationListener.authenticationFailed(failureInfo);
        }
        LOGGER.debug("Authentication failed ", future.cause());
    }

    private X509Certificate getPeerCertificateFromException(PeerCertificateException certificateException) {
        if (certificateException.getPeerCertificates() != null && certificateException.getPeerCertificates().length >
                0) {
            return certificateException.getPeerCertificates()[0];
        }
        return null;
    }

    /**
     * Retrieve PeerCertificateException cause exception wrapped by {@link}SSLHandshakeException{@link} otherwise
     * return null.
     *
     * @param throwable
     * @return
     */
    private PeerCertificateException getPeerCertificatException(Throwable throwable) {
        if (throwable instanceof PeerCertificateException) {
            return (PeerCertificateException) throwable;
        }
        Throwable cause = throwable;
        // defensive code for netty sslhandler exception handling
        while (cause instanceof SSLHandshakeException) {
            //return null, if no caused by exception
            if (cause == cause.getCause()) {
                return null;
            }
            cause = cause.getCause();
            if (cause instanceof PeerCertificateException) {
                return (PeerCertificateException) cause;
            }
        }
        return null;
    }

    private boolean isSslHandshakeTimeException(Throwable throwable) {
        if (throwable instanceof SSLException) {
            return "handshake timed out".equalsIgnoreCase(throwable.getMessage());
        }
        return false;
    }
}
