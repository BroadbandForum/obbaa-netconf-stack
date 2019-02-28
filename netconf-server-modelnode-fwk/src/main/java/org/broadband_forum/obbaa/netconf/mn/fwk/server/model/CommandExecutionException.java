package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;

public class CommandExecutionException extends Exception {
	private static final long serialVersionUID = 1L;

	private NetconfRpcError m_rpcError;
	
	public CommandExecutionException() {
		super();
	}
	
	public CommandExecutionException(NetconfRpcError rpcError, Throwable cause) {
		super(cause.getMessage(), cause);
        this.m_rpcError = rpcError;
    }
	
	public NetconfRpcError getRpcError() {
		return m_rpcError;
	}

	public CommandExecutionException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public CommandExecutionException(String message, Throwable cause) {
		super(message, cause);
	}

	public CommandExecutionException(String message) {
		super(message);
	}

	public CommandExecutionException(Throwable cause) {
		super(cause);
	}
	
	@Override
	public String toString() {
		StringBuilder commandToString = new StringBuilder();
		if (getMessage() != null) {
		    commandToString.append(getMessage());
		}
		
		if (getRpcError() != null) {
		    commandToString.append(getRpcError());
		}
		
        return commandToString.toString();
	}

}
