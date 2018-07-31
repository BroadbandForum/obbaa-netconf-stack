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

package org.broadband_forum.obbaa.netconf.persistence.jpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.persistence.EntityManager;

import org.broadband_forum.obbaa.netconf.persistence.EMFactory;
import org.broadband_forum.obbaa.netconf.persistence.EntityDataStoreManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class ThreadLocalPersistenceManagerUtilTest {
    private ThreadLocalPersistenceManagerUtil m_util;
    @Mock
    private EMFactory m_factory;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        m_util = new ThreadLocalPersistenceManagerUtil(m_factory);
        setUpMocks();
    }

    @Test
    public void test2DiffThreadsGetDifferentEMs() throws InterruptedException {
        final EntityDataStoreManager[] ems = new EntityDataStoreManager[2];
        Thread t1 = new Thread(() -> {
            ems[0] = m_util.getEntityDataStoreManager();
        }, "Thread 1");
        Thread t2 = new Thread(() -> ems[1] = m_util.getEntityDataStoreManager(), "Thread 2");
        t1.start();
        t2.start();
        t1.join();
        t2.join();

        assertNotEquals(ems[1], ems[0]);
        verify(m_factory, times(2)).createNewEntityManager();

    }

    @Test
    public void testMultipleCallsOnSameThreadGivesSameEM() throws InterruptedException {
        final EntityDataStoreManager[] ems = new EntityDataStoreManager[2];
        Thread t1 = new Thread(() -> {
            ems[0] = m_util.getEntityDataStoreManager();
            ems[1] = m_util.getEntityDataStoreManager();
        }, "Thread 1");
        t1.start();
        t1.join();

        assertEquals(ems[1], ems[0]);
        verify(m_factory).createNewEntityManager();

    }

    private void setUpMocks() {
        doAnswer(new Answer() {
            int m_count = 0;

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                EntityManager em = mock(EntityManager.class);
                if (m_count % 2 == 0) {
                    when(em.isOpen()).thenReturn(true);
                } else {
                    when(em.isOpen()).thenReturn(false);
                }
                m_count++;
                return em;
            }
        }).when(m_factory).createNewEntityManager();
    }
}
