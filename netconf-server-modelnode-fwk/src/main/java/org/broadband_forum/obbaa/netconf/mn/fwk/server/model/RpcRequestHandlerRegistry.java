package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import java.util.List;

import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcRequest;
import org.broadband_forum.obbaa.netconf.api.messages.RpcName;
import org.broadband_forum.obbaa.netconf.server.rpc.MultiRpcRequestHandler;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcRequestHandler;

public interface RpcRequestHandlerRegistry {

	void register(String componentId,RpcName rpcName, RpcRequestHandler rpcRequestHandler);
	RpcRequestHandler lookupRpcRequestHandler(RpcName rpcName);
	void undeploy(String componentId);
	void registerMultiRpcRequestHandler(MultiRpcRequestHandler multiRpcRequestHandler);
	List<MultiRpcRequestHandler> getMultiRpcRequestHandlers();
	void undeployMultiRpcRequestHandler(MultiRpcRequestHandler multiRpcRequestHandler);
	MultiRpcRequestHandler getMultiRpcRequestHandler(NetconfRpcRequest rpcRequest);
}
