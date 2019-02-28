package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import java.util.LinkedList;
import java.util.List;

import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;

public class EditConfigException extends RuntimeException {
    private List<NetconfRpcError> m_rpcErrors = new LinkedList<NetconfRpcError>();

    public EditConfigException(NetconfRpcError rpcError) {
        super(rpcError.getErrorMessage());
        this.m_rpcErrors.add(rpcError);
    }

    public NetconfRpcError getRpcError() {
        if (m_rpcErrors.isEmpty()) {
            return null;
        } else {
            return m_rpcErrors.get(0);
        }
    }
    
    public List<NetconfRpcError> getRpcErrors() {
        return m_rpcErrors;
    }
    
    public void setRpcErrors(List<NetconfRpcError> rpcErrors) {
        m_rpcErrors = rpcErrors;
    }

    @Override
    public String toString() {
        return "EditConfigException [rpcError=" + m_rpcErrors + "]";
    }

    private static final long serialVersionUID = 1L;

}
