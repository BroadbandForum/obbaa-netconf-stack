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

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class ReadWriteLockServiceImpl extends AbstractLockService {

    private final ReentrantReadWriteLock m_rwLock = new ReentrantReadWriteLock();
    private final Lock m_readLock = m_rwLock.readLock();
    private final Lock m_writeLock = m_rwLock.writeLock();

    public void writeLock() {
        m_writeLock.lock();
    }


    public void readLock() {
        m_readLock.lock();
    }


    public void readUnlock() {
        m_readLock.unlock();
    }


    public void writeUnlock() {
        m_writeLock.unlock();
    }
}
