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

import static org.broadband_forum.obbaa.netconf.api.util.ByteBufUtils.unpooledHeapByteBuf;
import static org.broadband_forum.obbaa.netconf.api.util.NetconfResources.CHUNK_SIZE;
import static org.broadband_forum.obbaa.netconf.api.util.NetconfResources.MAXIMUM_SIZE_OF_CHUNKED_MESSAGES;
import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;

import static junit.framework.Assert.assertEquals;

import org.broadband_forum.obbaa.netconf.api.MessageToolargeException;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.junit.Before;
import org.junit.Test;

import io.netty.buffer.ByteBuf;

public class DocumentInfoTest {
    private ChunkedNetconfMessageCodecV2 m_codec;
    private ByteBuf m_buf;

    @Before
    public void setUp() {
        m_codec = new ChunkedNetconfMessageCodecV2();
        m_buf = unpooledHeapByteBuf();
    }

    @Test
    public void test_DocumentInfo() throws NetconfMessageBuilderException, MessageToolargeException {
        String payload = "\n#6\n<rpc/>\n##\n";
        m_buf.writeBytes(payload.getBytes());
        DocumentInfo documentInfo = m_codec.decode(m_buf);
        assertXMLEqual(DocumentUtils.stringToDocument("<rpc/>"), documentInfo.getDocument());
        assertEquals(6, documentInfo.getLength());
        assertEquals("<rpc/>", documentInfo.getDocumentString());
    }
}
