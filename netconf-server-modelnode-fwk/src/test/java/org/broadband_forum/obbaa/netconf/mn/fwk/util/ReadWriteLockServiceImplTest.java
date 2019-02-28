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
