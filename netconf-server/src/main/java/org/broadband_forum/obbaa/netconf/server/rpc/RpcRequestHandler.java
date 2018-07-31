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

package org.broadband_forum.obbaa.netconf.server.rpc;

import java.util.List;

import org.opendaylight.yangtools.yang.model.api.RpcDefinition;

import org.broadband_forum.obbaa.netconf.api.NetconfMessage;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcResponse;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.messages.RpcName;

public interface RpcRequestHandler {

    public RpcName getRpcQName();

    public RpcDefinition getRpcDefinition();

    /**
     * Model Service Framework uses this method to set the RPC definition from yang
     *
     * @param rpcDefinition
     */
    public void setRpcDefinition(final RpcDefinition rpcDefinition);

    /**
     * A basic level of yang-based validation is available. Special cases need to be implemented by the
     * RpcRequestHandlers
     *
     * @param rpcConstraintParser
     * @param the                 rpc
     * @throws RpcValidationException
     */
    public void validate(RpcPayloadConstraintParser rpcConstraintParser, final NetconfMessage rpc) throws
            RpcValidationException;

    /**
     * This method will contain the implementation of RPC requests.
     * <p>
     * The RpcRequestHandlers should implement this method and set {@link NetConfResponse#setOk(boolean)} method of
     * response
     * For failure cases, the RpcRequestHandlers should add NetConfResponse errors to
     * {@link NetConfResponse#addErrors(java.util.List)} method of
     * response
     *
     * @param clientInfo
     * @param request
     * @param response
     * @return
     * @throws RpcProcessException
     */

    public List<Notification> processRequest(final NetconfClientInfo clientInfo, final NetconfRpcRequest request,
                                             final NetconfRpcResponse response)
            throws RpcProcessException;
}
