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

package org.broadband_forum.obbaa.netconf.api.server.auth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import java.net.InetSocketAddress;

import org.junit.Before;
import org.junit.Test;

public class ClientAuthenticationInfoTest {

    private ClientAuthenticationInfo m_clientAuth;
    private InetSocketAddress m_inetAddressSource;
    private InetSocketAddress m_inetAddressDestination;

    @Before
    public void setUp() {
        m_inetAddressSource = mock(InetSocketAddress.class);
        m_inetAddressDestination = mock(InetSocketAddress.class);
        m_clientAuth = new ClientAuthenticationInfo(1, "admin", "admin123", m_inetAddressSource,
                m_inetAddressDestination);
    }

    @Test
    public void testAuthenticate() {

        assertEquals("admin", m_clientAuth.getUsername());
        assertEquals("admin123", m_clientAuth.getPassword());
        assertEquals(1, m_clientAuth.getClientSessionId());
        assertNotNull(m_clientAuth.getInetAddressSource());
        assertNotNull(m_clientAuth.getInetAddressDestination());
    }

}
