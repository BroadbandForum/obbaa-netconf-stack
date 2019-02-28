package org.broadband_forum.obbaa.netconf.mn.fwk.util;

public interface WriteLockTemplate<T> {
    T execute() throws LockServiceException;
}
