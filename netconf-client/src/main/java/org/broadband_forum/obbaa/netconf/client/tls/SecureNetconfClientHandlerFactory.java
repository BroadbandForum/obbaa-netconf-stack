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

import java.security.cert.X509Certificate;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.broadband_forum.obbaa.netconf.api.client.CallHomeListener;

public class SecureNetconfClientHandlerFactory {
    private static final SecureNetconfClientHandlerFactory INSTANCE = new SecureNetconfClientHandlerFactory();

    private SecureNetconfClientHandlerFactory() {
    }

    public static SecureNetconfClientHandlerFactory getInstance() {
        return INSTANCE;
    }

    // FNMS-5482 - In Karaf OSGI, default classloader here is App classloader (sun.misc.Launcher$AppClassLoader), it cannot see io/netty/util/internal/TypeParameterMatcher
    // which is used inside super class constructor of SecureNetconfClientHandler.
    // Some times this causes NoClassDefFoundError (most the time it's ClassNotFoundException, it's not clear yet how come the NoClassDefFoundError occurs).
    // We're switching to bundle classloader here, this CL has access to class in package io.netty.util.internal

    public SecureNetconfClientHandler getSecureNetconfClientHandler(TlsNettyChannelNetconfClientSession clientSession, Set<String> capabilities, 
            ExecutorService callHomeExecutorService, CallHomeListener callHomeListener, X509Certificate peerCertificate, boolean selfSigned) {
        ClassLoader prevCL = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            return new SecureNetconfClientHandler(clientSession, capabilities, callHomeExecutorService, callHomeListener, peerCertificate, selfSigned);
        } finally {
            Thread.currentThread().setContextClassLoader(prevCL);
        }
    }
}
