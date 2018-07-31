/*
 * Copyright 2018 Broadband Forum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.broadband_forum.obbaa.netconf.api.messages.RpcName;
import org.broadband_forum.obbaa.netconf.server.rpc.MultiRpcRequestHandler;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcRequestHandler;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.LoggerFactory;

public class RpcRequestHandlerRegistryImpl implements RpcRequestHandlerRegistry {
    private static final AdvancedLogger LOGGER = LoggerFactory.getLogger(RpcRequestHandlerRegistryImpl.class,
            "netconf-server-datastore", "DEBUG", "GLOBAL");

    private ConcurrentHashMap<RpcName, RpcRequestHandler> m_registry = new ConcurrentHashMap<RpcName,
            RpcRequestHandler>();
    private ConcurrentHashMap<String, List<RpcName>> m_componentMap = new ConcurrentHashMap<>();
    private List<MultiRpcRequestHandler> m_multiRegistry = new ArrayList<MultiRpcRequestHandler>();

    public void register(String componentId, RpcName rpcName, RpcRequestHandler rpcRequestHandler) {
        m_registry.put(rpcName, rpcRequestHandler);
        List<RpcName> rpcsFromComponent = m_componentMap.get(componentId);
        if (rpcsFromComponent == null) {
            rpcsFromComponent = new ArrayList<>();
            m_componentMap.putIfAbsent(componentId, rpcsFromComponent);
        }
        rpcsFromComponent.add(rpcName);

        LOGGER.debug("Registered a RPC request handler:{} ", rpcRequestHandler.getRpcQName().getName());
    }

    public RpcRequestHandler lookupRpcRequestHandler(RpcName rpcName) {
        RpcRequestHandler handler = m_registry.get(rpcName);
        if (handler != null) {
            return handler;
        }
        return null;
    }

    public void undeploy(String componentId) {
        List<RpcName> rpcsFromComponent = m_componentMap.get(componentId);
        if (rpcsFromComponent != null) {
            for (RpcName rpcFromCompenent : rpcsFromComponent) {
                RpcRequestHandler rpcRequestHandler = m_registry.remove(rpcFromCompenent);
                if (rpcRequestHandler != null) {
                    LOGGER.debug("Unregistering a RPC request handler:{} ", rpcRequestHandler.getRpcQName().getName());
                }
            }
            m_componentMap.remove(componentId);
        }
    }

    @Override
    public void registerMultiRpcRequestHandler(MultiRpcRequestHandler multiRpcRequestHandler) {
        m_multiRegistry.add(multiRpcRequestHandler);
        LOGGER.debug("Registered a Multi RPC request handler:{} ", multiRpcRequestHandler);
    }

    @Override
    public void undeployMultiRpcRequestHandler(MultiRpcRequestHandler multiRpcRequestHandler) {
        if (multiRpcRequestHandler != null) {
            m_multiRegistry.remove(multiRpcRequestHandler);
            LOGGER.debug("Unregistering a Multi RPC request handler:{} ", multiRpcRequestHandler);
        }
    }

    @Override
    public List<MultiRpcRequestHandler> getMultiRpcRequestHandlers() {
        return m_multiRegistry;
    }

    @Override
    public MultiRpcRequestHandler getMultiRpcRequestHandler(RpcName rpcName) {
        LOGGER.debug("Find the handler for rpc {} ", rpcName);
        List<MultiRpcRequestHandler> multiRpcRequestHandlers = getMultiRpcRequestHandlers();
        String handlerMatchingNsHead = "";
        MultiRpcRequestHandler multiRpcRequestHandler = null;
        for (MultiRpcRequestHandler multiRpcHandler : multiRpcRequestHandlers) {
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
