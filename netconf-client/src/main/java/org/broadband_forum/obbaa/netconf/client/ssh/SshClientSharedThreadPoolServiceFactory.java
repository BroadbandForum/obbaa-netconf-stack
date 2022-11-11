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

import java.io.IOException;
import java.nio.channels.AsynchronousChannelGroup;

import org.apache.sshd.common.FactoryManager;
import org.apache.sshd.common.io.IoAcceptor;
import org.apache.sshd.common.io.IoConnector;
import org.apache.sshd.common.io.IoHandler;
import org.apache.sshd.common.io.IoServiceEventListener;
import org.apache.sshd.common.io.IoServiceFactory;
import org.apache.sshd.common.io.nio2.Nio2Acceptor;
import org.apache.sshd.common.io.nio2.Nio2Connector;
import org.apache.sshd.common.util.closeable.AbstractCloseable;

/**
 * 
 * Similar to Nio2ServiceFactory except that the ExecutorService is created with configurable number of NIO threads for SSH connection in
 * PMA. AsynchronousChannelGroup with this executorService is used to create NIO connector and acceptor.
 * 
 *
 * 
 */
public class SshClientSharedThreadPoolServiceFactory extends AbstractCloseable implements IoServiceFactory {

    private final FactoryManager m_manager;
    private final AsynchronousChannelGroup m_asyncChannelGroup;

    public SshClientSharedThreadPoolServiceFactory(FactoryManager manager, AsynchronousChannelGroup asyncChannelGroup) {
        this.m_manager = manager;
        m_asyncChannelGroup = asyncChannelGroup;
    }

    @Override
    public void close() throws IOException {
        super.close();
    }

    @Override
    public boolean isOpen() {
        return super.isOpen();
    }

    public IoConnector createConnector(IoHandler handler) {
        return new Nio2Connector(m_manager, handler, m_asyncChannelGroup);
    }

    public IoAcceptor createAcceptor(IoHandler handler) {
        return new Nio2Acceptor(m_manager, handler, m_asyncChannelGroup);
    }

    @Override
    public IoServiceEventListener getIoServiceEventListener() {
        return null;
    }

    @Override
    public void setIoServiceEventListener(IoServiceEventListener ioServiceEventListener) {

    }
}
