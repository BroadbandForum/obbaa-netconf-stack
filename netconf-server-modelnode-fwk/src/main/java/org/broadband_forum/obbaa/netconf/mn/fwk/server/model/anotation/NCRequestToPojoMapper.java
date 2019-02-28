package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.anotation;

import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcRequest;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.anotation.rpc.RpcArgsInfo;

import java.util.List;

/**
 * Created by vishal on 18/8/16.
 */
public interface NCRequestToPojoMapper {
    List<Object> getRpcArguments(NetconfRpcRequest rpc, RpcArgsInfo rpcArgsInfo) throws NCRequestToPojoMapperException;
}
