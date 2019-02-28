package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;

public class EditConfigTestFailedException extends Exception {
    private static final long serialVersionUID = 1L;
    private NetconfRpcError m_rpcError;

    public EditConfigTestFailedException(NetconfRpcError rpcError) {
        super(rpcError.getErrorMessage());
        this.m_rpcError = rpcError;
    }

    public NetconfRpcError getRpcError() {
        return m_rpcError;
    }

    @Override
    public String toString() {
        return "EditConfigTestFailedException [rpcError=" + m_rpcError + "]";
    }
}
