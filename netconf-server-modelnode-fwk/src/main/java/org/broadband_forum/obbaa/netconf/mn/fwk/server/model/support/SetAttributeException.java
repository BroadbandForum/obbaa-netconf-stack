package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support;

import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;

public class SetAttributeException extends Exception {
    private static final long serialVersionUID = 1L;

    private NetconfRpcError m_rpcError;
    
    public SetAttributeException() {
        super();
    }
    
    public SetAttributeException(NetconfRpcError rpcError, Throwable cause) {
    	super(cause.getMessage(), cause);
        this.m_rpcError = rpcError;
    }
    
    public SetAttributeException(NetconfRpcError rpcError) {
    	super(rpcError.getErrorMessage());
    	this.m_rpcError = rpcError;
    }

    public NetconfRpcError getRpcError() {
        return m_rpcError;
    }


    public SetAttributeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public SetAttributeException(String message, Throwable cause) {
        super(message, cause);
    }

    public SetAttributeException(String message) {
        super(message);
    }

    public SetAttributeException(Throwable cause) {
        super(cause);
    }

}
