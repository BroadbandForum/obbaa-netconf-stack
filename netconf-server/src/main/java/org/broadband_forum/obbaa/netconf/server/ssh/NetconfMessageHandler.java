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

import org.apache.sshd.common.io.IoOutputStream;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.channel.ChannelSession;
import org.broadband_forum.obbaa.netconf.api.MessageToolargeException;
import org.broadband_forum.obbaa.netconf.api.codec.v2.DocumentInfo;
import org.broadband_forum.obbaa.netconf.api.codec.v2.FrameAwareNetconfMessageCodecV2;
import org.broadband_forum.obbaa.netconf.api.codec.v2.NetconfMessageCodecV2;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerMessageListener;
import org.broadband_forum.obbaa.netconf.api.server.ServerMessageHandler;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.w3c.dom.Document;

import io.netty.buffer.ByteBuf;

public class NetconfMessageHandler extends AbstractSshNetconfServerMessageHandler {

    private FrameAwareNetconfMessageCodecV2 m_codec;

    public NetconfMessageHandler(NetconfServerMessageListener axsNetconfServerMessageListener, IoOutputStream out,
                                 ExitCallback exitCallBack, ServerMessageHandler serverMessageHandler, ChannelSession channel, FrameAwareNetconfMessageCodecV2 codec) {
        super(axsNetconfServerMessageListener, out, exitCallBack, serverMessageHandler, channel);
        m_codec = codec;
    }

    @Override
    public void useChunkedFraming() {
        m_codec.useChunkedFraming();
    }

    @Override
    public NetconfMessageCodecV2 currentCodec() {
        return m_codec.currentCodec();
    }

    @Override
    public byte[] encode(Document msg) throws NetconfMessageBuilderException {
        return m_codec.encode(msg);
    }

    @Override
    public DocumentInfo decode(ByteBuf in) throws NetconfMessageBuilderException, MessageToolargeException {
        return m_codec.decode(in);
    }
}
