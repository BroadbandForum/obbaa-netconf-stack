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


    public void readUnlock(){
        m_readLock.unlock();
    }


    public void writeUnlock() {
        m_writeLock.unlock();
    }
}
