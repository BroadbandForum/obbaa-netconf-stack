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

import static junit.framework.TestCase.assertEquals;
import static org.broadband_forum.obbaa.netconf.api.util.ByteBufUtils.unpooledHeapByteBuf;
import static org.broadband_forum.obbaa.netconf.api.util.DocumentUtils.documentToPrettyString;

import org.broadband_forum.obbaa.netconf.api.MessageToolargeException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.junit.Before;
import org.junit.Test;

import io.netty.buffer.CompositeByteBuf;

public class FrameAwareNetconfMessageCodecV2Test {

    private FrameAwareNetconfMessageCodecV2 m_codec;
    private CompositeByteBuf m_buf;

    @Before
    public void setUp() {
        m_codec = new FrameAwareNetconfMessageCodecV2Impl();
        m_buf = unpooledHeapByteBuf();
    }

    @Test
    public void testSwitching() throws NetconfMessageBuilderException, MessageToolargeException {
        m_buf.writeBytes(("<rpc/>"+ NetconfResources.RPC_EOM_DELIMITER).getBytes());
        assertEquals("<rpc/>\n", documentToPrettyString(m_codec.decode(m_buf).getDocument()));

        m_buf.clear();
        //switch
        m_codec.useChunkedFraming();
        //check it can now process chunks
        String payload = "\n#6\n<rpc/>\n##\n";
        m_buf.writeBytes(payload.getBytes());
        assertEquals("<rpc/>\n", documentToPrettyString(m_codec.decode(m_buf).getDocument()));
    }
}


