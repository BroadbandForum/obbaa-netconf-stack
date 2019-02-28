package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;

public class SubSystemValidationException extends Exception {
	private static final long serialVersionUID = 1L;

	private NetconfRpcError m_rpcError;

	public SubSystemValidationException(NetconfRpcError rpcError) {
		super(rpcError.getErrorMessage());
		this.m_rpcError = rpcError;
	}
	
	public SubSystemValidationException(NetconfRpcError rpcError, Throwable cause) {
        super(rpcError.getErrorMessage(), cause);
        this.m_rpcError = rpcError;
    }

	public SubSystemValidationException(String message) {
		super(message);
	}

	public SubSystemValidationException(Throwable cause) {
		super(cause);
	}

    public NetconfRpcError getRpcError() {
        return m_rpcError;
    }

}
