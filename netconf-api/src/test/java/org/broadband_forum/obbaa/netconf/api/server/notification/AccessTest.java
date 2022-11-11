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

package org.broadband_forum.obbaa.netconf.api.server.notification;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AccessTest {

    private Access m_access;

    @Before
    public void init() {
        m_access = new Access();
        m_access.setEncoding("xml");
        m_access.setLocation("restconf/streams/NETCONF/xml");
    }

    @Test
    public void testGetEncoding() {
        assertEquals("xml", m_access.getEncoding());
        assertEquals("restconf/streams/NETCONF/xml", m_access.getLocation());
    }

    @Test
    public void testToString() {
        String toString = "Access{m_encoding='xml', m_location='restconf/streams/NETCONF/xml'}";
        assertEquals(toString, m_access.toString());
    }

    @Test
    public void testHashcode() {
        Access access = new Access();
        access.setEncoding("xml");
        access.setLocation("restconf/streams/NETCONF/xml");
        assertEquals(access.hashCode(), m_access.hashCode());
    }

    @Test
    public void testEquals_positiveCase() {
        Access access = new Access();
        access.setEncoding("xml");
        access.setLocation("restconf/streams/NETCONF/xml");
        assertTrue(access.equals(access));
        assertTrue(access.equals(m_access));
    }

    @Test
    public void testEquals_negativeCase() {
        Access access = new Access();
        access.setEncoding("json");
        access.setLocation("restconf/streams/NETCONF/xml");
        assertFalse(access.equals(m_access));
        assertFalse(access.equals(new Stream()));
    }

}