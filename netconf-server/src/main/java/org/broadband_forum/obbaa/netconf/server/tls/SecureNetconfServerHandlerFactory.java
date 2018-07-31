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

import org.broadband_forum.obbaa.netconf.api.logger.NetconfLogger;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerMessageListener;
import org.broadband_forum.obbaa.netconf.api.server.ServerMessageHandler;

public class SecureNetconfServerHandlerFactory {
    private static final SecureNetconfServerHandlerFactory INSTANCE = new SecureNetconfServerHandlerFactory();

    private SecureNetconfServerHandlerFactory() {

    }

    public static SecureNetconfServerHandlerFactory getInstance() {
        return INSTANCE;
    }

    // FNMS-5482 - In Karaf OSGI, default classloader here is App classloader (sun.misc.Launcher$AppClassLoader), it
    // cannot see io/netty/util/internal/TypeParameterMatcher
    // which is used inside super class constructor of SecureNetconfClientHandler.
    // Some times this causes NoClassDefFoundError (most the time it's ClassNotFoundException, it's not clear yet how
    // come the NoClassDefFoundError occurs).
    // We're switching to bundle classloader here, this CL has access to class in package io.netty.util.internal

    public SecureNetconfServerHandler getSecureNetconfServerHandler(NetconfServerMessageListener
                                                                            axsNetconfServerMessageListener,
                                                                    ServerMessageHandler serverMessageHandler, int
                                                                            sessionId, NetconfLogger netconfLogger) {
        ClassLoader prevCL = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            return new SecureNetconfServerHandler(axsNetconfServerMessageListener,
                    serverMessageHandler, sessionId, netconfLogger);
        } finally {
            Thread.currentThread().setContextClassLoader(prevCL);
        }
    }
}
