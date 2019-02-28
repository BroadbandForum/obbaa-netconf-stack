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

package org.broadband_forum.obbaa.netconf.server;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.ActionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.ActionResponse;
import org.broadband_forum.obbaa.netconf.api.messages.CloseSessionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.CopyConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.DeleteConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetRequest;
import org.broadband_forum.obbaa.netconf.api.messages.KillSessionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.LockRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcResponse;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.messages.UnLockRequest;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerMessageListener;
import org.broadband_forum.obbaa.netconf.api.server.ResponseChannel;
import org.w3c.dom.Document;

import java.util.List;
import java.util.Set;

public abstract class ServerMessageListenerAdapter implements NetconfServerMessageListener {
    @Override
    public void onHello(NetconfClientInfo info, Set<String> clientCaps) {

    }

    @Override
    public void onGet(NetconfClientInfo info, GetRequest req, NetConfResponse resp) {

    }

    @Override
    public void onGetConfig(NetconfClientInfo info, GetConfigRequest req, NetConfResponse resp) {

    }

    @Override
    public List<Notification> onEditConfig(NetconfClientInfo info, EditConfigRequest req, NetConfResponse resp) {
        return null;
    }

    @Override
    public void onCopyConfig(NetconfClientInfo info, CopyConfigRequest req, NetConfResponse resp) {

    }

    @Override
    public void onDeleteConfig(NetconfClientInfo info, DeleteConfigRequest req, NetConfResponse resp) {

    }

    @Override
    public void onLock(NetconfClientInfo info, LockRequest req, NetConfResponse resp) {

    }

    @Override
    public void onUnlock(NetconfClientInfo info, UnLockRequest req, NetConfResponse resp) {

    }

    @Override
    public void onCloseSession(NetconfClientInfo info, CloseSessionRequest req, NetConfResponse resp) {

    }

    @Override
    public void onKillSession(NetconfClientInfo info, KillSessionRequest req, NetConfResponse resp) {

    }

    @Override
    public List<Notification> onRpc(NetconfClientInfo info, NetconfRpcRequest rpcRequest, NetconfRpcResponse response) {
        return null;
    }

    @Override
    public void onCreateSubscription(NetconfClientInfo info, NetconfRpcRequest req, ResponseChannel responseChannel) {

    }

    @Override
    public void onInvalidRequest(NetconfClientInfo info, Document req, NetConfResponse resp) {

    }

    @Override
    public void sessionClosed(String reasonForClosing, int sessionId) {

    }
    
    @Override
    public void onAction(NetconfClientInfo info, ActionRequest req, ActionResponse resp){
    	
    }

}
