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

public abstract class AbstractLockService implements ReadWriteLockService {

    public abstract void writeLock();

    public abstract void readLock();

    public abstract void readUnlock();

    public abstract void writeUnlock();

    @Override
    public <T> T executeWithReadLock(ReadLockTemplate<T> readLockTemplate) {
        readLock();
        try {
            return readLockTemplate.execute();
        } finally {
            readUnlock();
        }
    }

    @Override
    public <T> T executeWithWriteLock(WriteLockTemplate<T> writeLockTemplate) throws LockServiceException {
        writeLock();
        try {
            return writeLockTemplate.execute();
        } finally {
            writeUnlock();
        }
    }
}
