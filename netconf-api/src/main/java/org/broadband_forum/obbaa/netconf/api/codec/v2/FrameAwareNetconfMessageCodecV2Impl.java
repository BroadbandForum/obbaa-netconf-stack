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

package org.broadband_forum.obbaa.netconf.api.codec.v2;

import org.broadband_forum.obbaa.netconf.api.MessageToolargeException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.w3c.dom.Document;

import io.netty.buffer.ByteBuf;

public class FrameAwareNetconfMessageCodecV2Impl implements FrameAwareNetconfMessageCodecV2 {
    private NetconfMessageCodecV2 m_currentCodec;

    public FrameAwareNetconfMessageCodecV2Impl() {
        m_currentCodec = new EOMNetconfMessageCodecV2();
    }

    @Override
    public synchronized void useChunkedFraming() {
        m_currentCodec = new ChunkedNetconfMessageCodecV2();
    }

    @Override
    public NetconfMessageCodecV2 currentCodec() {
        return m_currentCodec;
    }

    @Override
    public byte[] encode(Document msg) throws NetconfMessageBuilderException {
        return m_currentCodec.encode(msg);
    }

    @Override
    public DocumentInfo decode(ByteBuf in) throws NetconfMessageBuilderException, MessageToolargeException {
        try {
            return m_currentCodec.decode(in);
        } finally {
            in.discardReadBytes();
        }
    }
}