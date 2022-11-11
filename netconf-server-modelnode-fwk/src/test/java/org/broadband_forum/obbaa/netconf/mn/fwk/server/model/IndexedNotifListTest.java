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
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class IndexedNotifListTest {

    @Mock
    private ChangeNotification m_aNotif;
    private ModelNodeId m_aId = new ModelNodeId("/container=a", "ns:ns");

    @Mock
    private ChangeNotification m_abNotif;
    private ModelNodeId m_abId = new ModelNodeId("/container=a/container=b", "ns:ns");

    @Mock
    private ChangeNotification m_abcNotif;
    private ModelNodeId m_abcId = new ModelNodeId("/container=a/container=b/container=c", "ns:ns");

    @Mock
    private ChangeNotification m_abcdNotif;
    private ModelNodeId m_abcdId = new ModelNodeId("/container=a/container=b/container=c/container=d", "ns:ns");
    @Mock
    private ChangeNotification m_abceNotif;
    private ModelNodeId m_abceId = new ModelNodeId("/container=a/container=b/container=c/container=e", "ns:ns");

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(m_aNotif.getKey()).thenReturn(m_aId);
        when(m_abNotif.getKey()).thenReturn(m_abId);
        when(m_abcNotif.getKey()).thenReturn(m_abcId);
        when(m_abcdNotif.getKey()).thenReturn(m_abcdId);
        when(m_abceNotif.getKey()).thenReturn(m_abceId);
    }

    @Test
    public void testAddingParentRemovesChildNotifs() {
        IndexedNotifList list = new IndexedNotifList();
        list.replaceChildNotifsWithParentNotif(m_abcdNotif);
        assertEquals(1, list.size());
        list.replaceChildNotifsWithParentNotif(m_abcNotif);
        assertEquals(1, list.size());
        assertEquals(m_abcNotif, list.list().iterator().next());
        assertEquals(m_abcNotif, list.map().get(m_abcId));
    }

    @Test
    public void testAddingParentRemovesChildNotifsOfDifferentSiblings() {
        IndexedNotifList list = new IndexedNotifList();
        list.replaceChildNotifsWithParentNotif(m_abcdNotif);
        list.replaceChildNotifsWithParentNotif(m_abceNotif);

        assertEquals(2, list.size());
        list.replaceChildNotifsWithParentNotif(m_abcNotif);
        assertEquals(1, list.size());
        assertEquals(m_abcNotif, list.list().iterator().next());
        assertEquals(m_abcNotif, list.map().get(m_abcId));
    }

    @Test
    public void testAddAndReplaceSameNotification() {
        IndexedNotifList list = new IndexedNotifList();
        list.add(m_abNotif);
        list.add(m_abcNotif);
        list.add(m_abcdNotif);
        assertEquals(3, list.size());
        assertTrue(list.list().contains(m_abNotif));
        assertTrue(list.list().contains(m_abcNotif));
        assertTrue(list.list().contains(m_abcdNotif));
        assertEquals(m_abNotif, list.map().get(m_abId));
        assertEquals(m_abcNotif, list.map().get(m_abcId));
        assertEquals(m_abcdNotif, list.map().get(m_abcdId));
        list.replaceChildNotifsWithParentNotif(m_aNotif);
        assertEquals(1, list.size());
        assertEquals(1, list.map().size());
        assertTrue(list.list().contains(m_aNotif));
        assertEquals(m_aNotif, list.map().get(m_aId));
    }
}
