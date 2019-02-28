package org.broadband_forum.obbaa.netconf.mn.fwk.schema;

/**
 * Created by keshava on 11/19/15.
 */
public class SchemaBuildException extends Exception {
    public SchemaBuildException() {
    }

    public SchemaBuildException(String message) {
        super(message);
    }

    public SchemaBuildException(String message, Throwable cause) {
        super(message, cause);
    }

    public SchemaBuildException(Throwable cause) {
        super(cause);
    }

    public SchemaBuildException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
