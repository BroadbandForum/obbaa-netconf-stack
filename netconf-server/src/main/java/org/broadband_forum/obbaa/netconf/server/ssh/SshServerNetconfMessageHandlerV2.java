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

package org.broadband_forum.obbaa.netconf.server.ssh;

import java.util.Set;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.codec.v2.DocumentInfo;
import org.broadband_forum.obbaa.netconf.api.codec.v2.FrameAwareNetconfMessageCodecV2;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;

public interface SshServerNetconfMessageHandlerV2 extends FrameAwareNetconfMessageCodecV2 {
    void onHello(NetconfClientInfo clientInfo, Set<String> clientCaps);

    void processRequest(DocumentInfo request) throws NetconfMessageBuilderException;
}
