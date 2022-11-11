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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.broadband_forum.obbaa.netconf.mn.fwk.WritableChangeTreeNode;
import org.junit.Before;
import org.junit.Test;

public class NotificationContextTest {

    private NotificationContext m_notificationContext;

    @Before
    public void setUp() throws Exception {
        m_notificationContext = new NotificationContext();
    }

    @Test
    public void testAddChangeTreeNode() {
        assertNull(m_notificationContext.getChangeTree());
        m_notificationContext.addChangeTreeNode(mock(WritableChangeTreeNode.class));
        assertNotNull(m_notificationContext.getChangeTree());
    }

    @Test
    public void testToString() {
        WritableChangeTreeNode aggregatorCTN = mock(WritableChangeTreeNode.class);
        m_notificationContext.addChangeTreeNode(aggregatorCTN);
        assertNotNull(m_notificationContext.getChangeTree());
        assertEquals("NotificationContext [m_aggregatorCTN=null]", m_notificationContext.toString());
        verify(aggregatorCTN).print();
    }
}