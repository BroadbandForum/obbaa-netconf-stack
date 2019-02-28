package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support;

import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;

public class TransformerException extends Exception {
	private static final long serialVersionUID = 1L;

	private NetconfRpcError m_rpcError;
	
	public TransformerException() {
		super();
	}

	public TransformerException(NetconfRpcError rpcError) {
		super(rpcError.getErrorMessage());
		this.m_rpcError = rpcError;
	}
	
	public NetconfRpcError getRpcError() {
		return m_rpcError;
	}

	public TransformerException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public TransformerException(String message, Throwable cause) {
		super(message, cause);
	}

	public TransformerException(String message) {
		super(message);
	}

	public TransformerException(Throwable cause) {
		super(cause);
	}

}
