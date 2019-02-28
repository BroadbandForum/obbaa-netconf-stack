package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.service;

/**
 * Created by keshava on 12/9/15.
 */
public class ModelServiceDeployerException extends Exception {
    public ModelServiceDeployerException() {
    }

    public ModelServiceDeployerException(String message) {
        super(message);
    }

    public ModelServiceDeployerException(String message, Throwable cause) {
        super(message, cause);
    }

    public ModelServiceDeployerException(Throwable cause) {
        super(cause);
    }

    public ModelServiceDeployerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
