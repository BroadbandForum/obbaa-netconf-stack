package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support;

public class ModelNodeGetException extends Exception {

    private static final long serialVersionUID = -4567452590514478472L;

    public ModelNodeGetException(String message) {
        super(message);
    }

    public ModelNodeGetException(Throwable cause) {
        super(cause);
    }

    public ModelNodeGetException(String message, Throwable cause) {
        super(message, cause);
    }

}
