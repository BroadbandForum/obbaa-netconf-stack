package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

/**
 * An Exception that is thrown when an invalid argument is supplied to a method
 * Created by keshava on 5/12/16.
 */
public class InvalidArgumentException extends Exception{
    public InvalidArgumentException() {
    }

    public InvalidArgumentException(String message) {
        super(message);
    }

    public InvalidArgumentException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidArgumentException(Throwable cause) {
        super(cause);
    }

    public InvalidArgumentException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
