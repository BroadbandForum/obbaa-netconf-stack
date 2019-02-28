package org.broadband_forum.obbaa.netconf.mn.fwk.util;


public class LockServiceException extends RuntimeException {

    public LockServiceException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public LockServiceException(String s) {
        super(s);
    }

    public LockServiceException(Throwable e) {
        super(e);
    }
}
