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

package org.broadband_forum.obbaa.netconf.persistence.jpa.dao;


import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Collections;

import javax.persistence.LockModeType;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.broadband_forum.obbaa.netconf.persistence.EntityDataStoreManager;
import org.broadband_forum.obbaa.netconf.persistence.PagingInput;
import org.broadband_forum.obbaa.netconf.persistence.PersistenceManagerUtil;

public class AbstractDaoTest {

    private AbstractDao m_dao;
    @Mock
    private PersistenceManagerUtil m_util;
    @Mock
    private EntityDataStoreManager m_edsm;

    @Before
    public void setUp(){
        MockitoAnnotations.initMocks(this);
        when(m_util.getEntityDataStoreManager()).thenReturn(m_edsm);
        Class klass = AbstractDaoTest.class;
        m_dao = new AbstractDao(m_util, klass) {
            @Override
            protected Class getEntityClass() {
                return super.getEntityClass();
            }
        };

    }

    @Test
    public void testFindByIdWithReadLock(){
        Serializable pk = mock(Serializable.class);
        m_dao.findByIdWithReadLock(pk);
        verify(m_edsm).findById(AbstractDaoTest.class, pk, LockModeType.PESSIMISTIC_READ);
    }

    @Test
    public void testFindById(){
        Serializable pk = mock(Serializable.class);
        m_dao.findById(pk);
        verify(m_edsm).findById(AbstractDaoTest.class, pk, LockModeType.PESSIMISTIC_READ);
    }

    @Test
    public void testCreate(){
        AbstractDaoTest entity = new AbstractDaoTest();
        m_dao.create(entity);
        verify(m_edsm).create(entity);
    }

    @Test
    public void testDelete(){
        AbstractDaoTest entity = new AbstractDaoTest();
        m_dao.delete(entity);
        verify(m_edsm).delete(entity);
    }

    @Test
    public void testDeleteAll(){
        m_dao.deleteAll();
        verify(m_edsm).deleteAll(AbstractDaoTest.class);
    }

    @Test
    public void testFindAll(){
        m_dao.findAll();
        verify(m_edsm).findAll(AbstractDaoTest.class, Collections.emptyList());
    }

    @Test
    public void testDeleteById(){
        Serializable pk = mock(Serializable.class);
        m_dao.deleteById(pk);
        verify(m_edsm).deleteById(AbstractDaoTest.class, pk);
    }

    @Test
    public void testFindByIdWithWriteLock(){
        Serializable pk = mock(Serializable.class);
        m_dao.findByIdWithWriteLock(pk);
        verify(m_edsm).findById(AbstractDaoTest.class, pk, LockModeType.PESSIMISTIC_WRITE);
    }

    @Test
    public void testMerge(){
        AbstractDaoTest entity = new AbstractDaoTest();
        m_dao.merge(entity);
        verify(m_edsm).merge(entity);
    }

    @Test
    public void testFindWithPaging(){
        PagingInput input = mock(PagingInput.class);
        m_dao.findWithPaging(input);
        verify(m_edsm).findWithPaging(AbstractDaoTest.class, input);
    }
    @Test
    public void testFindWithPagingAndOrderByColumn(){
        PagingInput input = mock(PagingInput.class);
        m_dao.findWithPagingAndOrderByColumn(input, Collections.emptyMap(), "column", true);
        verify(m_edsm).findWithPagingAndOrderByColumn(AbstractDaoTest.class, input, Collections.emptyMap(), "column", true);
    }
}
