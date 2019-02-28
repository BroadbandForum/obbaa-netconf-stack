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
import org.broadband_forum.obbaa.netconf.api.messages.CloseSessionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetConfigRequest;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerMessageListener;
import org.broadband_forum.obbaa.netconf.api.server.ResponseChannel;
import org.broadband_forum.obbaa.netconf.server.QueuingMessageHandler;
import org.junit.Test;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class QueuingMessageHandlerTest {
    @Test
    public void testProcessRequest(){
        NetconfServerMessageListener msgListener = mock(NetconfServerMessageListener.class);
        QueuingMessageHandler handler = new QueuingMessageHandler(msgListener);
        NetconfClientInfo clientInfo = mock(NetconfClientInfo.class);
        CloseSessionRequest closeSessionRequest = mock(CloseSessionRequest.class);
        ResponseChannel channel = mock(ResponseChannel.class);
        handler.processRequest(clientInfo, closeSessionRequest, channel);
        verify(msgListener).onCloseSession(eq(clientInfo), eq(closeSessionRequest), anyObject());

        GetConfigRequest getConfigRequest = mock(GetConfigRequest.class);
        handler.processRequest(clientInfo, getConfigRequest, channel);
        verify(msgListener).onGetConfig(eq(clientInfo), eq(getConfigRequest), anyObject());
    }
}
