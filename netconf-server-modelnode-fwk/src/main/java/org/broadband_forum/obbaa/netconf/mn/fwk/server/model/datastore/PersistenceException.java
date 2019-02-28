package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore;

import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;

public class PersistenceException extends Exception {

	private static final long serialVersionUID = -1415222914213726915L;
	
	private NetconfRpcError m_rpcError;

	public PersistenceException(NetconfRpcError rpcError) {
		super(rpcError.getErrorMessage());
		this.m_rpcError = rpcError;
	}
	
	public NetconfRpcError getRpcError() {
	    return m_rpcError;
	}

    @Override
    public String toString() {
        return "EditConfigException [rpcError=" + m_rpcError + "]";
    }
}
