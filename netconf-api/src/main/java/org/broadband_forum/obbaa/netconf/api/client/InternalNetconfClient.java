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

package org.broadband_forum.obbaa.netconf.api.client;

import java.util.concurrent.ExecutionException;

import org.w3c.dom.Document;

import org.broadband_forum.obbaa.netconf.api.logger.NetconfLogger;
import org.broadband_forum.obbaa.netconf.api.messages.AbstractNetconfRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcRequest;
import org.broadband_forum.obbaa.netconf.api.server.ServerMessageHandler;

public interface InternalNetconfClient {
    NetConfResponse get(GetRequest request) throws InterruptedException, ExecutionException;

    NetConfResponse editConfig(AbstractNetconfRequest request) throws InterruptedException, ExecutionException;

    NetConfResponse rpc(NetconfRpcRequest request) throws InterruptedException, ExecutionException;

    NetConfResponse getConfig(GetConfigRequest request) throws InterruptedException, ExecutionException;

    NetConfResponse sendRpcMessage(Document document) throws InterruptedException, ExecutionException;

    NetconfClientInfo getClientInfo();

    long getRequestTimeOut();

    NetconfLogger getNetconfLogger();

    void setRequestTimeout(long requestTimeout);

    ServerMessageHandler getServerMessageHandler();

    void setServerMessageHandler(ServerMessageHandler serverMessageHandler);
}
