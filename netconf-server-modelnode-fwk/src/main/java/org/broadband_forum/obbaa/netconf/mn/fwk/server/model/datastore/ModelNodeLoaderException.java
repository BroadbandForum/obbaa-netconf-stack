package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore;

/**
 * Created by keshava on 12/17/15.
 */
public class ModelNodeLoaderException extends Exception{
    public ModelNodeLoaderException() {
    }

    public ModelNodeLoaderException(String message) {
        super(message);
    }

    public ModelNodeLoaderException(String message, Throwable cause) {
        super(message, cause);
    }

    public ModelNodeLoaderException(Throwable cause) {
        super(cause);
    }

    public ModelNodeLoaderException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public ModelNodeLoaderException(Exception e) {
    }
}
