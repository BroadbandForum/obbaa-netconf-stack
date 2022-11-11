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

package org.broadband_forum.obbaa.netconf.api.server;

import java.util.List;
import java.util.Set;

import org.w3c.dom.Document;

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

/**
 * A listener to be implemented by the netconf server to listen to netconf messages from clients.
 * 
 * 
 * 
 */
public interface NetconfServerMessageListener {
    public static final String ON_HELLO_METHOD = "onHello";
    public static final String ON_GET_METHOD = "onGet";
    public static final String ON_GET_CONFIG_METHOD = "onGetConfig";
    public static final String ON_EDIT_CONFIG_METHOD = "onEditConfig";
    public static final String ON_RPC_METHOD = "onRpc";
    public static final String ON_ACTION = "onAction";
    
    public static final String ON_COPY_CONFIG_METHOD = "onCopyConfig";
    public static final String ON_DELETE_CONFIG_METHOD = "onDeleteConfig";
    public static final String ON_LOCK_METHOD = "onLock";
    public static final String ON_UNLOCK_METHOD = "onUnlock";
    public static final String ON_CLOSE_SESSION_METHOD = "onCloseSession";
    public static final String ON_KILL_SESSION_METHOD = "onKillSession";
    public static final String ON_CREATE_SUBSCRIPTION_METHOD = "onCreateSubscription";
    int CLOSE_RESPONSE_TIME_OUT_SECS = 10;

    public void onHello(NetconfClientInfo info, Set<String> clientCaps);

    public void onGet(NetconfClientInfo info, GetRequest req, NetConfResponse resp);

    public void onGetConfig(NetconfClientInfo info, GetConfigRequest req, NetConfResponse resp);

    public List<Notification> onEditConfig(NetconfClientInfo info, EditConfigRequest req, NetConfResponse resp);

    public void onCopyConfig(NetconfClientInfo info, CopyConfigRequest req, NetConfResponse resp);

    public void onDeleteConfig(NetconfClientInfo info, DeleteConfigRequest req, NetConfResponse resp);

    public void onLock(NetconfClientInfo info, LockRequest req, NetConfResponse resp);

    public void onUnlock(NetconfClientInfo info, UnLockRequest req, NetConfResponse resp);

    public void onCloseSession(NetconfClientInfo info, CloseSessionRequest req, NetConfResponse resp);

    public void onKillSession(NetconfClientInfo info, KillSessionRequest req, NetConfResponse resp);

    public List<Notification> onRpc(NetconfClientInfo info, NetconfRpcRequest rpcRequest, NetconfRpcResponse response);

    public void onCreateSubscription(NetconfClientInfo info, NetconfRpcRequest req, ResponseChannel responseChannel);

    public void onInvalidRequest(NetconfClientInfo info, Document req, NetConfResponse resp);

    public void onAction(NetconfClientInfo info, ActionRequest req, ActionResponse resp);

    /**
     * This is a call back when a session is closed by the server. This can happen for multiple reasons. For example:
     * 
     * <pre>
     * {@code
     * 1. The client sent a <close-session> (In this case the NetconfServerMessageListener#onCloseSession(NetconfClientInfo, CloseSessionRequest, NetConfResponse) is also called).
     * 2. Idle time out occurred, etc
     * }
     * @param reasonForClosing
     */
    public void sessionClosed(String reasonForClosing, int sessionId);

}
