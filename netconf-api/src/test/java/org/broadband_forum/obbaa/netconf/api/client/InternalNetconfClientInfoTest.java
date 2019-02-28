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

import org.broadband_forum.obbaa.netconf.api.client.InternalNetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Assert;

public class InternalNetconfClientInfoTest {
    @Test
    public void testThePojo() {
        NetconfClientInfo info = new InternalNetconfClientInfo("User", 10, 100);
        assertEquals(info.getUsername(), "User");
        assertEquals(info.getSessionId(), 10);
        assertEquals(info.getClientSessionId(), 100);
        assertTrue(info.isInternalUser());
        
        NetconfClientInfo netconfClientInfo = new InternalNetconfClientInfo("User", 10);
        assertEquals(netconfClientInfo.getUsername(), "User");
        assertEquals(netconfClientInfo.getSessionId(), 10);
        assertTrue(netconfClientInfo.isInternalUser());
        Assert.assertNotNull(netconfClientInfo.getClientSessionId());
        
    }
}
