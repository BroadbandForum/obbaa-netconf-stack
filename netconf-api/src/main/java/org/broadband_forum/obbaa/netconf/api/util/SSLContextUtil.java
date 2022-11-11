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

package org.broadband_forum.obbaa.netconf.api.util;

import java.security.cert.CertificateException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;

import org.broadband_forum.obbaa.netconf.api.LogAppNames;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientConfiguration;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientDispatcherException;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerConfiguration;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerDispatcherException;
import org.broadband_forum.obbaa.netconf.api.transport.AbstractTLSNetconfTransport;
import org.broadband_forum.obbaa.netconf.api.transport.security.X509KeyManagerFactory;
import org.broadband_forum.obbaa.netconf.api.transport.security.X509KeyManagerFactorySpi;
import org.broadband_forum.obbaa.netconf.api.transport.security.X509TrustManagerFactory;
import org.broadband_forum.obbaa.netconf.api.transport.security.X509TrustManagerFactorySpi;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.SupportedCipherSuiteFilter;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.ssl.util.SelfSignedCertificate;

/**
 * Created by kbhatk on 7/27/16.
 */
public class SSLContextUtil {
    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(SSLContextUtil.class, LogAppNames.NETCONF_LIB);

    public static SslContext getServerSSLContext(NetconfServerConfiguration config) throws NetconfServerDispatcherException {
        AbstractTLSNetconfTransport transport = (AbstractTLSNetconfTransport) config.getNetconfTransport();
        SslContextBuilder builder;
        try {
            if (transport.isSelfSigned()) {
                SelfSignedCertificate ssc;
                ssc = new SelfSignedCertificate();
                builder = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey());
                LOGGER.info("Starting the "+ transport.getTranportProtocol() +" server with Self Signed Certificate");
            } else {
                KeyManagerFactory keyFactory = null;
                if (transport.getKeyManager() != null && SslProvider.JDK.equals(transport.getSslProvider())){
                    keyFactory = new X509KeyManagerFactory(new X509KeyManagerFactorySpi(transport.getKeyManager()));
                    builder = SslContextBuilder.forServer(keyFactory);
                } else{
                    //file way
                    builder = SslContextBuilder.forServer(transport.getCertificateChain(), transport.getPrivateKey(),
                            transport.getPrivateKeyPassword());
                }
                if(transport.getTrustManager() != null){
                    TrustManagerFactory trustFactory = null;
                    trustFactory = new X509TrustManagerFactory(new X509TrustManagerFactorySpi(transport.getTrustManager()));
                    builder.trustManager(trustFactory);
                }else{
                    //file way
                    builder.trustManager(transport.getTrustChain());
                }
                LOGGER.info("Starting the "+ transport.getTranportProtocol() +" server with CA Signed Certificate");
            }
            builder.sslProvider(transport.getSslProvider())
                    .ciphers(null, SupportedCipherSuiteFilter.INSTANCE);

            return builder.build();
        } catch (SSLException | CertificateException e) {
            LOGGER.error("Exception while starting server ", e);
            throw new NetconfServerDispatcherException("Exception while starting server ", e);
        }
    }

    public static SslContext getClientSSLContext(NetconfClientConfiguration config) throws NetconfClientDispatcherException {
        AbstractTLSNetconfTransport transport = (AbstractTLSNetconfTransport) config.getTransport();
        SslContextBuilder builder = null;
        try {
            builder = SslContextBuilder.forClient();
            if (transport.isSelfSigned()) {
                builder.trustManager(InsecureTrustManagerFactory.INSTANCE);
                LOGGER.info("Starting the " + transport.getTranportProtocol() + " client with Self Signed Certificate");
            } else {
                KeyManagerFactory keyFactory = null;
                if (transport.getKeyManager() != null && SslProvider.JDK.equals(transport.getSslProvider())) {
                    keyFactory = new X509KeyManagerFactory(new X509KeyManagerFactorySpi(transport.getKeyManager()));
                    builder.keyManager(keyFactory);
                }else{
                    //file way
                    builder.keyManager(transport.getCertificateChain(), transport.getPrivateKey(),transport.getPrivateKeyPassword());
                }

                if(transport.getTrustManager() != null){
                    TrustManagerFactory trustFactory = null;
                    trustFactory = new X509TrustManagerFactory(new X509TrustManagerFactorySpi(transport.getTrustManager()));
                    builder.trustManager(trustFactory);
                }else{
                    //file way
                    builder.trustManager(transport.getTrustChain());
                }
                LOGGER.info("Starting the "+ transport.getTranportProtocol() +" client with CA Signed Certificate");
            }
            builder.sslProvider(transport.getSslProvider())
                    .ciphers(null, SupportedCipherSuiteFilter.INSTANCE);
            return builder.build();
        } catch (SSLException e) {
            LOGGER.error("Exception while starting server ", e);
            throw new NetconfClientDispatcherException("Exception while starting client ", e);
        }
    }
}
