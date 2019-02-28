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

package org.broadband_forum.obbaa.netconf.server.model.notification.rpchandlers;

import java.text.ParseException;
import java.util.List;

import org.broadband_forum.obbaa.netconf.api.NetconfMessage;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.CreateSubscriptionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcResponse;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.messages.RpcName;
import org.broadband_forum.obbaa.netconf.api.server.ResponseChannel;
import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationService;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.w3c.dom.DOMException;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.rpc.AbstractRpcRequestHandler;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.NetconfRpcErrorUtil;
import org.broadband_forum.obbaa.netconf.server.rpc.CreateSubscriptionRpcHandler;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcPayloadConstraintParser;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcProcessException;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcValidationException;


public class CreateSubscriptionRpcHandlerImpl extends AbstractRpcRequestHandler implements CreateSubscriptionRpcHandler {
	
	private NetconfClientInfo m_clientInfo;
	private RpcName m_rpcQName;
	private RpcDefinition m_rpcDefinition;
	private NotificationService m_notificationService;

	private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(CreateSubscriptionRpcHandlerImpl.class, LogAppNames
			.NETCONF_NOTIFICATION);
		
	public CreateSubscriptionRpcHandlerImpl(NotificationService notificationService, RpcName rpcName) {
	    super (rpcName);
		m_notificationService = notificationService;
		m_rpcQName = rpcName;
		
	}

	@Override
	public void validate(RpcPayloadConstraintParser rpcConstraintParser, NetconfMessage request) throws RpcValidationException {
		super.validate(rpcConstraintParser, request);
	}
	
	@Override
	public List<Notification> processRequest(final NetconfClientInfo clientInfo, final NetconfRpcRequest request,
			final NetconfRpcResponse response) throws RpcProcessException {
		throw new RpcProcessException(NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.OPERATION_FAILED,
				"create-subscription request requires callback dispatcher"));
	}
	
	@Override
	public void processRequest(final NetconfClientInfo clientInfo, final NetconfRpcRequest request, final ResponseChannel responseChannel) throws RpcProcessException {
		
		m_clientInfo = clientInfo;
		
		try { 
			CreateSubscriptionRequest subscriptionRequest = DocumentUtils.getInstance().getSubscriptionRequest(request);
			m_notificationService.createSubscription(m_clientInfo, subscriptionRequest, responseChannel);
		} catch (DOMException | ParseException e) {
 			LOGGER.error(null,"Error while parsing date", e);
 			throw new RpcProcessException(NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.OPERATION_FAILED, "Parsing date failed"));
 		}
	}
	
	public NetconfClientInfo getClientInfo() {
		return m_clientInfo;
	}
	
	@Override
	public RpcName getRpcQName() {
		return m_rpcQName;
	}
	
	public void setRpcQName(RpcName rpcName) {
		m_rpcQName = rpcName;
	}
	
	@Override
	public RpcDefinition getRpcDefinition() {
		return m_rpcDefinition;
	}

	@Override
	public void setRpcDefinition(final RpcDefinition rpcDefinition) {
		m_rpcDefinition = rpcDefinition;
		
	}
}