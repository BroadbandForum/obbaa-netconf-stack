package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcRequest;
import org.broadband_forum.obbaa.netconf.api.messages.RpcName;
import org.broadband_forum.obbaa.netconf.server.rpc.MultiRpcRequestHandler;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcRequestHandler;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;

public class RpcRequestHandlerRegistryImpl implements RpcRequestHandlerRegistry {
	private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(RpcRequestHandlerRegistryImpl.class, LogAppNames.NETCONF_STACK);
	
	private ConcurrentHashMap<RpcName, RpcRequestHandler> m_registry = new ConcurrentHashMap<RpcName, RpcRequestHandler>();
	private ConcurrentHashMap<String, List<RpcName>> m_componentMap = new ConcurrentHashMap<>();
	private List<MultiRpcRequestHandler> m_multiRegistry = new ArrayList<MultiRpcRequestHandler>();

	public void register(String componentId, RpcName rpcName, RpcRequestHandler rpcRequestHandler) {
		m_registry.put(rpcName, rpcRequestHandler);
		List<RpcName> rpcsFromComponent = m_componentMap.get(componentId);
		if(rpcsFromComponent == null){
				rpcsFromComponent = new ArrayList<>();
				m_componentMap.putIfAbsent(componentId, rpcsFromComponent);
		}
		rpcsFromComponent.add(rpcName);

		LOGGER.debug("Registered a RPC request handler:{} ",rpcRequestHandler.getRpcQName().getName());
	}
	
	public RpcRequestHandler lookupRpcRequestHandler(RpcName rpcName) {
		RpcRequestHandler handler = m_registry.get(rpcName);
		if (handler != null) {
			return handler;
		}
		return null;
	}

	public void undeploy(String componentId){
		List<RpcName> rpcsFromComponent = m_componentMap.get(componentId);
		if (rpcsFromComponent != null) {
			for(RpcName rpcFromCompenent: rpcsFromComponent){
				RpcRequestHandler rpcRequestHandler = m_registry.remove(rpcFromCompenent);
				if(rpcRequestHandler != null) {
					LOGGER.debug("Unregistering a RPC request handler:{} ", rpcRequestHandler.getRpcQName().getName());
				}
			}
			m_componentMap.remove(componentId);
		}
	}

	@Override
	public void registerMultiRpcRequestHandler(MultiRpcRequestHandler multiRpcRequestHandler) {
		m_multiRegistry.add(multiRpcRequestHandler);
		LOGGER.debug("Registered a Multi RPC request handler:{} ",multiRpcRequestHandler);
	}

	@Override
	public void undeployMultiRpcRequestHandler(MultiRpcRequestHandler multiRpcRequestHandler) {
		if(multiRpcRequestHandler != null) {
			m_multiRegistry.remove(multiRpcRequestHandler);
			LOGGER.debug("Unregistering a Multi RPC request handler:{} ", multiRpcRequestHandler);
		}
	}

	@Override
	public List<MultiRpcRequestHandler> getMultiRpcRequestHandlers() {
		return m_multiRegistry;
	}

	@Override
	public MultiRpcRequestHandler getMultiRpcRequestHandler(NetconfRpcRequest rpcRequest) {
	    RpcName rpcName = rpcRequest.getRpcName();
        LOGGER.debug("Find the handler for rpc {} ", rpcName);
		List<MultiRpcRequestHandler> multiRpcRequestHandlers = getMultiRpcRequestHandlers();
		String handlerMatchingNsHead = "";
		MultiRpcRequestHandler multiRpcRequestHandler = null;
		for (MultiRpcRequestHandler multiRpcHandler : multiRpcRequestHandlers) {
		    if ( rpcRequest.isSchemaMountedRpc() && multiRpcHandler.isSchemaMountedRpcRequestHandler()){
		        return multiRpcHandler;
		    }
			String nsMatchingHead = multiRpcHandler.checkForRpcSupport(rpcName);
			if (nsMatchingHead != null) {
				//the handler that returns the longest initial substring of the namespace will be chosen
				if (nsMatchingHead.length() > handlerMatchingNsHead.length()) {
					handlerMatchingNsHead = nsMatchingHead;
					multiRpcRequestHandler = multiRpcHandler;
                    LOGGER.debug("Found the the matched rpc {} ", multiRpcRequestHandler);
				}
			}
		}
		return multiRpcRequestHandler;
	}
}
