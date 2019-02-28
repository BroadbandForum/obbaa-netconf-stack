package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support;

public class ModelNodeFactoryException extends Exception {
    private static final long serialVersionUID = 1L;

    public ModelNodeFactoryException() {
        super();
    }

    public ModelNodeFactoryException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public ModelNodeFactoryException(String message, Throwable cause) {
        super(message, cause);
    }

    public ModelNodeFactoryException(String message) {
        super(message);
    }

    public ModelNodeFactoryException(Throwable cause) {
        super(cause);
    }

}
