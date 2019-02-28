package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore;

/**
 * Created by keshava on 12/8/15.
 */
public class DataStoreException extends RuntimeException{
    public DataStoreException() {
    }

    public DataStoreException(String message) {
        super(message);
    }

    public DataStoreException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataStoreException(Throwable cause) {
        super(cause);
    }

    public DataStoreException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
