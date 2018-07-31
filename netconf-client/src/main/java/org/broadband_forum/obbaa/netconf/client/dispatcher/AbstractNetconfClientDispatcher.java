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

package org.broadband_forum.obbaa.netconf.client.dispatcher;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientConfiguration;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientDispatcher;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientDispatcherException;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientSession;
import org.broadband_forum.obbaa.netconf.api.client.TcpServerSession;

public abstract class AbstractNetconfClientDispatcher implements NetconfClientDispatcher {
    private static final Logger LOGGER = Logger.getLogger(AbstractNetconfClientDispatcher.class);

    private final ExecutorService m_executorService;// NOSONAR

    protected AbstractNetconfClientDispatcher(ExecutorService executorService) {// NOSONAR
        m_executorService = executorService;
    }

    protected ExecutorService getExecutorService() {// NOSONAR
        return m_executorService;
    }

    @Override
    public Future<NetconfClientSession> createClient(final NetconfClientConfiguration config) throws
            NetconfClientDispatcherException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Creating new client session with config " + config + "\n Executor status: " +
                    m_executorService);
        }
        return m_executorService.submit(new Callable<NetconfClientSession>() {
            @Override
            public NetconfClientSession call() throws Exception {
                return createFutureSession(config);
            }
        });
    }

    protected abstract NetconfClientSession createFutureSession(final NetconfClientConfiguration config)
            throws NetconfClientDispatcherException;

    @Override
    public Future<TcpServerSession> createReverseClient(NetconfClientConfiguration config) throws
            NetconfClientDispatcherException {
        throw new NetconfClientDispatcherException("This client dispatacher does not support reverse connnection");

    }

}
