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
