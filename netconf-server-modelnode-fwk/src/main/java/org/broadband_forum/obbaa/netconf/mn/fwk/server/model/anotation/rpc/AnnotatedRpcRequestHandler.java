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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.anotation.rpc;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcResponse;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.messages.RpcName;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.rpc.AbstractRpcRequestHandler;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcProcessException;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by kbhatk on 6/8/16.
 */
public class AnnotatedRpcRequestHandler extends AbstractRpcRequestHandler {
    private RpcArgsInfo m_rpcArgsInfo;
    private InvocationType m_invocationType;
    private Method m_rpcMethod;
    private Object m_bean;

    public AnnotatedRpcRequestHandler(RpcName rpcQName) {
        super(rpcQName);
    }

    @Override
    public List<Notification> processRequest(NetconfClientInfo clientInfo, NetconfRpcRequest request,
                                             NetconfRpcResponse response) throws RpcProcessException {
        return null;
    }

    public RpcArgsInfo getRpcArgsInfo() {
        return m_rpcArgsInfo;
    }

    public void setRpcArgsInfo(RpcArgsInfo rpcArgsInfo) {
        m_rpcArgsInfo = rpcArgsInfo;
    }

    public InvocationType getInvocationType() {
        return m_invocationType;
    }

    public void setInvocationType(InvocationType invocationType) {
        m_invocationType = invocationType;
    }

    public Method getRpcMethod() {
        return m_rpcMethod;
    }

    public void setRpcMethod(Method rpcMethod) {
        m_rpcMethod = rpcMethod;
    }

    public Object getBean() {
        return m_bean;
    }

    public void setBean(Object bean) {
        m_bean = bean;
    }
}
