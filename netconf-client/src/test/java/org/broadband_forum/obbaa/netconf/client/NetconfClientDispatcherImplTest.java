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

package org.broadband_forum.obbaa.netconf.client;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.net.UnknownHostException;

import org.broadband_forum.obbaa.netconf.api.NetconfConfigurationBuilderException;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientConfiguration;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientDispatcherException;
import org.broadband_forum.obbaa.netconf.api.client.util.NetconfClientConfigurationBuilder;
import org.broadband_forum.obbaa.netconf.client.dispatcher.NetconfClientDispatcherImpl;
import org.broadband_forum.obbaa.netconf.client.dispatcher.SshClientDispatcherImpl;

import junit.framework.TestCase;

public class NetconfClientDispatcherImplTest extends TestCase {

    SshClientDispatcherImpl m_sshSpy = spy(new SshClientDispatcherImpl());

    public void testSshDispatcherIsCalled() throws NetconfConfigurationBuilderException, UnknownHostException,
            NetconfClientDispatcherException {
        NetconfClientDispatcherImpl dispacther = new NetconfClientDispatcherImpl(m_sshSpy);

        NetconfClientConfiguration config = NetconfClientConfigurationBuilder.createDefaultNcClientBuilder().build();
        dispacther.createClient(config);
        verify(m_sshSpy).createClient(config);
    }
}
