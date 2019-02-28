package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.utils;

/**
 * Created by vgotagi on 1/12/17.
 */
public class TxException extends RuntimeException {
    /**
     * Create a TransactionalException with a given string and nested Throwable.
     */
    public TxException(String s, Throwable throwable)
    {
        super(s, throwable);
    }

    /**
     * Create a TransactionalException with a given Throwable.
     */
    public TxException(Throwable throwable)
    {
        super(throwable);
    }
}
