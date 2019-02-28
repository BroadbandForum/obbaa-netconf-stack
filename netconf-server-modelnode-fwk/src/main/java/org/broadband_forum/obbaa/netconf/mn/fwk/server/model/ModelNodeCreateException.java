package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;

public class ModelNodeCreateException extends Exception {
    private static final long serialVersionUID = 1L;

    private NetconfRpcError m_rpcError;
    
    public ModelNodeCreateException() {
        super();
    }

    public ModelNodeCreateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public ModelNodeCreateException(String message, Throwable cause) {
        super(message, cause);
    }

    public ModelNodeCreateException(String message) {
        super(message);
    }
    
    public ModelNodeCreateException(NetconfRpcError rpcError) {
    	super(rpcError.getErrorMessage());
    	m_rpcError = rpcError;
    }

    public ModelNodeCreateException(Throwable cause) {
        super(cause);
    }
    
    public NetconfRpcError getRpcError() {
    	return m_rpcError;
    }

}
