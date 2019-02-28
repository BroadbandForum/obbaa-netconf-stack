package org.broadband_forum.obbaa.netconf.mn.fwk.util;

public abstract class AbstractLockService  implements ReadWriteLockService {

    public abstract void writeLock();

    public abstract void readLock();

    public abstract void readUnlock();

    public abstract void writeUnlock();

    @Override
    public <T> T executeWithReadLock(ReadLockTemplate<T> readLockTemplate) {
        readLock();
        try {
            return readLockTemplate.execute();
        }finally {
            readUnlock();
        }
    }

    @Override
    public <T> T executeWithWriteLock(WriteLockTemplate<T> writeLockTemplate) throws LockServiceException {
        writeLock();
        try {
            return writeLockTemplate.execute();
        }finally {
            writeUnlock();
        }
    }
}
