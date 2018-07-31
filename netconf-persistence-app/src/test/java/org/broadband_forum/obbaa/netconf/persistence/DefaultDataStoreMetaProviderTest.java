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

package org.broadband_forum.obbaa.netconf.persistence;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.broadband_forum.obbaa.netconf.persistence.jpa.dao.DbVersionDao;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class DefaultDataStoreMetaProviderTest {

    private DefaultDataStoreMetaProvider m_provider;
    @Mock
    private DbVersionDao m_dao;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        m_provider = new DefaultDataStoreMetaProvider(m_dao);
    }

    @Test
    public void testGetDataStoreVersion() {
        when(m_dao.findByIdWithReadLock("UT")).thenReturn(null);
        assertEquals(0L, m_provider.getDataStoreVersion("UT"));

        DbVersion version = new DbVersion();
        version.setModuleId("UT");
        version.setVersion(1L);
        when(m_dao.findByIdWithReadLock("UT")).thenReturn(version);
        assertEquals(1L, m_provider.getDataStoreVersion("UT"));
    }

    @Test
    public void testUpdateDataStoreVersion() {
        when(m_dao.findByIdWithWriteLock("UT")).thenReturn(null);
        m_provider.updateDataStoreVersion("UT", 2);

        ArgumentCaptor<DbVersion> dbVersionCaptor = ArgumentCaptor.forClass(DbVersion.class);
        verify(m_dao).create(dbVersionCaptor.capture());
        assertEquals(2L, dbVersionCaptor.getValue().getVersion());
        assertEquals("UT", dbVersionCaptor.getValue().getModuleId());

        DbVersion version = mock(DbVersion.class);
        when(m_dao.findByIdWithWriteLock("UT")).thenReturn(version);
        m_provider.updateDataStoreVersion("UT", 3);
        verify(version).setVersion(3L);
        verify(m_dao).create(anyObject());

    }
}
