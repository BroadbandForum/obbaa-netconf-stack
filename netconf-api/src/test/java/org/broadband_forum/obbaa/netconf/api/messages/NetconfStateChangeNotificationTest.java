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

package org.broadband_forum.obbaa.netconf.api.messages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;

import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;

public class NetconfStateChangeNotificationTest {

    public static final QName TYPE = QName.create(NetconfResources.NC_STACK_NS, "netconf-state-change");

    private NetconfStateChangeNotification m_netconfStateChangeNotification;
    private StateChangeInfo m_stateChangeInfo;
    private List<StateChangeInfo> m_stateChangeInfos;

    @Before
    public void initialize() {
        m_stateChangeInfo = new StateChangeInfo();
        m_netconfStateChangeNotification = new NetconfStateChangeNotification(m_stateChangeInfo);
        m_stateChangeInfos = new ArrayList<>();
    }

    @Test
    public void testGetAndSetChangesList() {
        m_stateChangeInfos.add(m_stateChangeInfo);
        StateChangeInfo stateChangeInfo = mock(StateChangeInfo.class);
        m_stateChangeInfos.add(stateChangeInfo);
        m_netconfStateChangeNotification.setChangesList(stateChangeInfo);
        assertEquals(m_stateChangeInfos, m_netconfStateChangeNotification.getChangesList());
        stateChangeInfo = mock(StateChangeInfo.class);
        m_stateChangeInfos.add(stateChangeInfo);
        m_netconfStateChangeNotification.setChangesList(m_stateChangeInfos);
        assertEquals(m_stateChangeInfos, m_netconfStateChangeNotification.getChangesList());
    }

    @Test
    public void testGetNotificationElement() {
        m_netconfStateChangeNotification = new NetconfStateChangeNotification(m_stateChangeInfos);
        assertNotNull(m_netconfStateChangeNotification.getNotificationElement());
    }

    @Test
    public void testGetType() {
        assertEquals(TYPE, m_netconfStateChangeNotification.getType());
    }
}
