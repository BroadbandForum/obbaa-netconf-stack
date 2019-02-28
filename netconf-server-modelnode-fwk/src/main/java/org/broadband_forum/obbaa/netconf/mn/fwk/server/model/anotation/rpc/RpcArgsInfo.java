package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.anotation.rpc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kbhatk on 8/8/16.
 */
public class RpcArgsInfo {
    private List<RpcArgumentInfo> m_rpcArgsInfo = new ArrayList<>();

    public RpcArgumentInfo getRpcArgument(int index) {
        if(index >= m_rpcArgsInfo.size()){
            return null;
        }
        return m_rpcArgsInfo.get(index);
    }

    public void setRpcArgsInfo(List<RpcArgumentInfo> rpcArgs) {
        m_rpcArgsInfo = rpcArgs;
    }

    public List<RpcArgumentInfo> getRpcArguments() {
        return m_rpcArgsInfo;
    }
}
