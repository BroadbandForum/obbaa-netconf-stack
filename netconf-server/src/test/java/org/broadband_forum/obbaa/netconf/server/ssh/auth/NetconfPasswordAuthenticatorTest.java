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

package org.broadband_forum.obbaa.netconf.server.ssh.auth;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.apache.sshd.common.io.IoSession;
import org.apache.sshd.server.session.ServerSession;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

import org.broadband_forum.obbaa.netconf.api.authentication.AuthenticationListener;
import org.broadband_forum.obbaa.netconf.api.server.auth.AuthenticationResult;
import org.broadband_forum.obbaa.netconf.api.server.auth.ClientAuthenticationInfo;
import org.broadband_forum.obbaa.netconf.api.server.auth.NetconfServerAuthenticationHandler;

public class NetconfPasswordAuthenticatorTest {

    private NetconfServerAuthenticationHandler m_axsNetconfAuthenticationHandler;
    private NetconfPasswordAuthenticator m_netconfPasswordAuthenticator;
    private AuthenticationListener m_authenticationListener;
    ServerSession m_session;

    @Before
    public void setUp() {
        SocketAddress inetSourceAddress=new InetSocketAddress("192.168.95.15", 9995);
        SocketAddress inetDestinationAddress=new InetSocketAddress("192.168.95.16", 9996);
        m_session=mock(ServerSession.class);
        IoSession ioSession=mock(IoSession.class);
        when(ioSession.getRemoteAddress()).thenReturn(inetSourceAddress);
        when(ioSession.getLocalAddress()).thenReturn(inetDestinationAddress);
        when(m_session.getIoSession()).thenReturn(ioSession);
        m_axsNetconfAuthenticationHandler = mock(NetconfServerAuthenticationHandler.class);
        when(m_axsNetconfAuthenticationHandler .authenticate((ClientAuthenticationInfo) anyObject())).thenReturn(AuthenticationResult.failedAuthResult());
        m_authenticationListener = mock(AuthenticationListener.class);
        m_netconfPasswordAuthenticator = new NetconfPasswordAuthenticator(m_axsNetconfAuthenticationHandler, m_authenticationListener);
    }

    @Test
    public void testAuthenticate() {
        m_netconfPasswordAuthenticator.authenticate("admin", "admin", m_session);
        verify(m_axsNetconfAuthenticationHandler,times(1)).authenticate((ClientAuthenticationInfo) argThat(new ClientAuthenticationInfoArgumentMatcher()));
    }
    
    @SuppressWarnings("rawtypes")
    class ClientAuthenticationInfoArgumentMatcher extends ArgumentMatcher {
        
        public boolean matches(Object argument) {
            ClientAuthenticationInfo clientAuthInfo = (ClientAuthenticationInfo)argument;
            assertTrue(clientAuthInfo.getDestinationAddress().equals("192.168.95.16"));
            assertTrue(String.valueOf(clientAuthInfo.getDestinationPort()).equals("9996"));
            assertTrue(clientAuthInfo.getSourceAddress().equals("192.168.95.15"));
            assertTrue(String.valueOf(clientAuthInfo.getSourcePort()).equals("9995"));
            assertTrue(clientAuthInfo.getUsername().equals("admin"));
            assertTrue(clientAuthInfo.getPassword().equals("admin"));
            return true;
        }
        
    }

}
