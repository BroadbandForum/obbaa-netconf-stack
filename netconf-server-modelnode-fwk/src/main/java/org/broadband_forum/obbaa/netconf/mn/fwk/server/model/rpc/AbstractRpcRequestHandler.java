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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.rpc;

import java.util.List;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.w3c.dom.Element;

import org.broadband_forum.obbaa.netconf.api.NetconfMessage;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcResponse;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.messages.RpcName;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.server.rpc.RequestType;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcPayloadConstraintParser;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcProcessException;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcRequestHandler;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcValidationException;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.LoggerFactory;

public abstract class AbstractRpcRequestHandler implements RpcRequestHandler {

    protected final RpcName m_rpcQName;

    protected RpcDefinition m_rpcDefinition;

    static final AdvancedLogger LOGGER = LoggerFactory.getLogger(AbstractRpcRequestHandler.class, "netconf-stack",
            "DEBUG", "GLOBAL");

    public AbstractRpcRequestHandler(RpcName rpcQName) {
        m_rpcQName = rpcQName;
    }

    @Override
    public RpcName getRpcQName() {
        return m_rpcQName;
    }

    @Override
    public RpcDefinition getRpcDefinition() {
        return m_rpcDefinition;
    }

    @Override
    public void setRpcDefinition(final RpcDefinition rpcDefinition) {
        m_rpcDefinition = rpcDefinition;

    }

    @Override
    public abstract List<Notification> processRequest(final NetconfClientInfo clientInfo, final NetconfRpcRequest
            request, final NetconfRpcResponse response) throws RpcProcessException;

    @Override
    public void validate(final RpcPayloadConstraintParser rpcConstraintParser, final NetconfMessage rpc) throws
            RpcValidationException {
        //Concerete rpc handlers can validate some their logics
        try {
            if (rpc.getType().isRequest()) {
                rpcConstraintParser.validate((NetconfRpcRequest) rpc, RequestType.RPC);
            } else if (rpc.getType().isResponse()) {
                rpcConstraintParser.validate((NetconfRpcResponse) rpc, RequestType.RPC);
            }
        } catch (ValidationException e) {
            throw new RpcValidationException("RPC Validation failed: " + e.getRpcError().getErrorMessage(), e, e
                    .getRpcError(), false, true);
        }
        LOGGER.debug("{} is validated", rpc.toString());
    }

    protected Element getNode(NetconfRpcRequest request, String childLocalName, String namespace) {
        return DocumentUtils.getDescendant(request.getRpcInput(), childLocalName, namespace);
    }

}
