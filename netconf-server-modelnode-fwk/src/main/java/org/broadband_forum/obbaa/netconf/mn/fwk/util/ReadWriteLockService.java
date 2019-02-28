package org.broadband_forum.obbaa.netconf.mn.fwk.util;

public interface ReadWriteLockService {

    <T> T executeWithReadLock(ReadLockTemplate<T> readLockTemplate) throws LockServiceException;

    <T> T executeWithWriteLock(WriteLockTemplate<T> writeLockTemplate) throws LockServiceException;
}
