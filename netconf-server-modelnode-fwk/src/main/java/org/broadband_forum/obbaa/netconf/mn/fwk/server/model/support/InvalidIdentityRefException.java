package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support;

import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;

/**
 * Created by sgs on 2/20/17.
 */
public class InvalidIdentityRefException extends Exception {

    private NetconfRpcError m_rpcError;

    public InvalidIdentityRefException(NetconfRpcError rpcError) {
        super(rpcError.getErrorMessage());
        this.m_rpcError = rpcError;
    }

    public NetconfRpcError getRpcError() {
        return m_rpcError;
    }

    @Override
    public String toString() {
        return "InvalidIdentityRefException{" +
                "m_rpcError=" + m_rpcError +
                '}';
    }
}
