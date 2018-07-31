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

package org.broadband_forum.obbaa.netconf.api.authentication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

public class SuccessInfoTest {

    private static final String IP_ADDRESS = "127.0.0.1";
    private static final int PORT = 1234;
    private SuccessInfo m_successInfo;

    @Before
    public void setUp() {
        m_successInfo = new SuccessInfo().setIp(IP_ADDRESS).setPort(PORT).setPeerCertificate(null);
    }

    @Test
    public void testPojo() {
        assertEquals(IP_ADDRESS, m_successInfo.getIp());
        assertEquals(PORT, m_successInfo.getPort());

        assertNotEquals("127.0.0.2", m_successInfo.getIp());
        assertNotEquals(124, m_successInfo.getPort());
        assertNull(m_successInfo.getPeerCertificate());
    }

    @Test
    public void testToString() {
        SuccessInfo temp_successInfo = new SuccessInfo().setIp(IP_ADDRESS).setPort(PORT);
        assertEquals(m_successInfo, temp_successInfo);
        assertNotEquals(m_successInfo, null);

        assertEquals("SuccessInfo [m_ip=" + m_successInfo.getIp() + ", m_port=" + m_successInfo.getPort() + "]",
                m_successInfo.toString());
    }
}
