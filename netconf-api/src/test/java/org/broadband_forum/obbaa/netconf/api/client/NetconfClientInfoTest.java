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

package org.broadband_forum.obbaa.netconf.api.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Test;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;

public class NetconfClientInfoTest {

	private NetconfClientInfo m_netconfInfo;
	private static final String USERNAME = "user";
	private static final int SESSION_ID = 10;
	private static final String REMOTE_HOST = "127.0.0.1";
	private static final String REMOTE_PORT = "100";

	@Before
	public void init() {
		m_netconfInfo = new NetconfClientInfo(USERNAME, SESSION_ID)
				.setRemoteHost(REMOTE_HOST)
				.setRemotePort(REMOTE_PORT);
	}

	@Test
	public void testThePojo() {
		assertEquals(USERNAME, m_netconfInfo.getUsername());
		assertEquals(SESSION_ID, m_netconfInfo.getSessionId());
		assertEquals(REMOTE_HOST, m_netconfInfo.getRemoteHost());
		assertEquals(REMOTE_PORT, m_netconfInfo.getRemotePort());
		assertFalse(m_netconfInfo.isInternalUser());
	}

	@Test
	public void testToString() {
		NetconfClientInfo info = new NetconfClientInfo(USERNAME, SESSION_ID, null);
		assertEquals(m_netconfInfo, info);
		assertEquals(
				"NetconfClientInfo{" + "username='" + USERNAME + '\'' + ", sessionId=" + SESSION_ID
						+ ", m_remoteHost='" + REMOTE_HOST + '\'' + ", m_remotePort='" + REMOTE_PORT + '\'' + "}",
				m_netconfInfo.toString());
	}
}
