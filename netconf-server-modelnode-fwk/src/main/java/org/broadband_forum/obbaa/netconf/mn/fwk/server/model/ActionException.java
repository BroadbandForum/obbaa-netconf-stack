package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import java.util.LinkedList;
import java.util.List;

import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;

public class ActionException extends Exception {
    private List<NetconfRpcError> m_rpcErrors = new LinkedList<NetconfRpcError>();

	public ActionException(NetconfRpcError rpcError) {
	    super(rpcError.getErrorMessage());
        this.m_rpcErrors.add(rpcError);
	}

	public ActionException(String message, Throwable cause) {
		super(message, cause);

	}

	public List<NetconfRpcError> getRpcErrors() {
		return m_rpcErrors;
	}

	@Override
	public String toString() {
		return "ActionException [rpcError=" + m_rpcErrors + "]";
	}

	private static final long serialVersionUID = 1L;
}
