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

import java.nio.channels.AsynchronousChannelGroup;

import org.apache.sshd.common.Factory;
import org.apache.sshd.common.FactoryManager;
import org.apache.sshd.common.io.IoServiceFactory;
import org.apache.sshd.common.io.IoServiceFactoryFactory;
import org.apache.sshd.common.util.threads.CloseableExecutorService;

public class SshClientServiceFactoryFactory implements IoServiceFactoryFactory {

    private AsynchronousChannelGroup m_asyncChannelGroup;

    public SshClientServiceFactoryFactory(AsynchronousChannelGroup asyncChannelGroup) {
        m_asyncChannelGroup = asyncChannelGroup;
    }

    public IoServiceFactory create(FactoryManager manager) {
        return new SshClientSharedThreadPoolServiceFactory(manager, m_asyncChannelGroup);
    }

    @Override
    public void setExecutorServiceFactory(Factory<CloseableExecutorService> factory) {

    }

}