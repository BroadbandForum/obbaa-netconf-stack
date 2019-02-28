package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;

public class GetAttributeException extends Exception {

	private static final long serialVersionUID = 1918964697503120893L;
	private NetconfRpcError m_rpcError;
	
	public GetAttributeException() {
		
	}
	
	public GetAttributeException(NetconfRpcError error){
		super(error.getErrorMessage());
		this.m_rpcError = error;
	}
	
	public NetconfRpcError getRpcError(){
		return m_rpcError;
	}

	public GetAttributeException(String message) {
		super(message);
	}

	public GetAttributeException(Throwable cause) {
		super(cause);
	}

	public GetAttributeException(String message, Throwable cause) {
		super(message, cause);
	}

	public GetAttributeException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
