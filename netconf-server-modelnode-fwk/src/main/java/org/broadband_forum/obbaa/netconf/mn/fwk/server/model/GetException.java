package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;

public class GetException extends RuntimeException {
    private NetconfRpcError m_rpcError;

    public GetException(NetconfRpcError rpcError) {
        super(rpcError.getErrorMessage());
        this.m_rpcError = rpcError;
    }

    public GetException(String message, Throwable cause) {
		super(message, cause);
		
	}

	public NetconfRpcError getRpcError() {
        return m_rpcError;
    }

    @Override
    public String toString() {
        return "GetException [rpcError=" + m_rpcError + "]";
    }

    private static final long serialVersionUID = 1L;
}
