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

import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;

public class StateChangeNotificationTest {

    private static final String TARGET = "target";
    private static final String VALUE = "value";
    public static final QName TYPE = QName.create(NetconfResources.NC_STACK_NS, "state-change-notification");

    private StateChangeNotification m_stateChangeNotification;

    @Before
    public void initialize() {
        m_stateChangeNotification = new StateChangeNotification(TARGET, VALUE);
    }

    @Test
    public void testGetNotificationElement() {
        assertNotNull(m_stateChangeNotification.getNotificationElement());
    }

    @Test
    public void testGetType() {
        assertEquals(TYPE, m_stateChangeNotification.getType());
    }

    @Test
    public void testGetValue() {
        assertEquals(VALUE, m_stateChangeNotification.getValue());
    }

    @Test
    public void testGetTarget() {
        assertEquals(TARGET, m_stateChangeNotification.getTarget());
    }
}
