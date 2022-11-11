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

package org.broadband_forum.obbaa.netconf.mn.fwk.util;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class ReadWriteLockServiceImplTest {

    private ReadWriteLockServiceImpl m_readWriteLockService;

    @Before
    public void setup(){
        m_readWriteLockService = Mockito.spy(new ReadWriteLockServiceImpl());
    }

    @Test
    public void testExecuteWithWriteLock() throws LockServiceException {
        m_readWriteLockService.executeWithWriteLock(new WriteLockTemplate<String>() {
            @Override
            public String execute() {
                return "Write lock execution";
            }
        });

        verify(m_readWriteLockService).writeLock();
        verify(m_readWriteLockService).writeUnlock();
    }

    @Test
    public void testExecuteWithReadLock(){
        m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<String>() {
            @Override
            public String execute() {
                return "Read lock execution";
            }
        });

        verify(m_readWriteLockService).readLock();
        verify(m_readWriteLockService).readUnlock();
    }
}
