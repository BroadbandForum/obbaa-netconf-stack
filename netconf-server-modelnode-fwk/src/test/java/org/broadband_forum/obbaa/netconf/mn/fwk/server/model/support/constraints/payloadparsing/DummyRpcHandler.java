package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.payloadparsing;

import java.util.List;

import org.broadband_forum.obbaa.netconf.api.NetconfMessage;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcResponse;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.messages.RpcName;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.rpc.AbstractRpcRequestHandler;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcPayloadConstraintParser;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcProcessException;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcValidationException;

public class DummyRpcHandler extends AbstractRpcRequestHandler {

	public DummyRpcHandler(RpcName rpcQName) {
		super(rpcQName);
	}

	@Override
	public void validate(RpcPayloadConstraintParser rpcConstraintParser,
			NetconfMessage request) throws RpcValidationException {
		super.validate(rpcConstraintParser, request);
	}

	@Override
	public List<Notification> processRequest(NetconfClientInfo clientInfo, NetconfRpcRequest request,
			NetconfRpcResponse response) throws RpcProcessException {
		return null;
	}
}
