package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;

public class CopyConfigException extends Exception {
    private static final long serialVersionUID = 1L;
    private NetconfRpcError m_rpcError;

    public CopyConfigException(NetconfRpcError rpcError) {
        super(rpcError.getErrorMessage());
        this.m_rpcError = rpcError;
    }

    public NetconfRpcError getRpcError() {
        return m_rpcError;
    }

    @Override
    public String toString() {
        return "CopyConfigException [rpcError=" + m_rpcError + "]";
    }

}
