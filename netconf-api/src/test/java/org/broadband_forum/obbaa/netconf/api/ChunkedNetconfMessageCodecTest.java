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

package org.broadband_forum.obbaa.netconf.api;

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.utils.FileUtil;
import org.custommonkey.xmlunit.Diff;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.w3c.dom.Document;

import io.netty.channel.ChannelHandlerContext;

public class ChunkedNetconfMessageCodecTest {
    private static final Logger LOGGER = Logger.getLogger(ChunkedNetconfMessageCodecTest.class);

    private ChunkedNetconfMessageCodec m_chunkedNetconfMessageCodec;
    @Mock
    private ChannelHandlerContext m_ctx;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        m_chunkedNetconfMessageCodec = new ChunkedNetconfMessageCodec(65536,65536);
    }

    @Test
    public void testDecodeChunkedMessage() throws NetconfMessageBuilderException, MessageToolargeException {

        Document actual = m_chunkedNetconfMessageCodec.decode(FileUtil.loadAsString("/sampleChunkedMessage1.txt"));
        Document expected = DocumentUtils.stringToDocument(FileUtil.loadAsString("/expectedMessage1.txt"));
        Diff diff = new Diff(expected, actual);
        boolean similar = diff.similar();
        LOGGER.info(diff.toString());
        assertTrue(similar);

        actual = m_chunkedNetconfMessageCodec.decode(FileUtil.loadAsString("/sampleChunkedMessage6.txt"));
        expected = DocumentUtils.stringToDocument(FileUtil.loadAsString("/expectedMessage6.txt"));
        diff = new Diff(expected, actual);
        similar = diff.similar();
        LOGGER.info(diff.toString());
        assertTrue(similar);

        actual = m_chunkedNetconfMessageCodec.decode(FileUtil.loadAsString("/sampleChunkedMessage7.txt"));
        expected = DocumentUtils.stringToDocument(FileUtil.loadAsString("/expectedMessage7.txt"));
        diff = new Diff(expected, actual);
        similar = diff.similar();
        LOGGER.info(diff.toString());
        assertTrue(similar);

        actual = m_chunkedNetconfMessageCodec.decode(FileUtil.loadAsString("/sampleChunkedMessage2.txt"));
        expected = DocumentUtils.stringToDocument(FileUtil.loadAsString("/expectedMessage2.txt"));
        diff = new Diff(expected, actual);
        similar = diff.similar();
        LOGGER.info(diff.toString());
        assertTrue(similar);

        actual = m_chunkedNetconfMessageCodec.decode(FileUtil.loadAsString("/sampleChunkedMessage4.txt"));
        expected = DocumentUtils.stringToDocument(FileUtil.loadAsString("/expectedMessage2.txt"));
        diff = new Diff(expected, actual);
        similar = diff.similar();
        LOGGER.info(diff.toString());
        assertTrue(similar);

    }

    @Test
    public void testDecodeChunkedMessageWithException() throws NetconfMessageBuilderException {
        m_chunkedNetconfMessageCodec = new ChunkedNetconfMessageCodec(500,65536);
        try{
            m_chunkedNetconfMessageCodec.decode(FileUtil.loadAsString("/sampleChunkedMessage1.txt"));
            fail("Should have thrown a MessageToolargeExceptiona");
        } catch (MessageToolargeException e){
            assertEquals("Incoming message too long. Not decoding the message to avoid buffer overrun, message size: 591 max-size: 500",e.getMessage());
        }
    }

    @Test
    public void testEncodeChunkedMessage() throws NetconfMessageBuilderException, MessageToolargeException, UnsupportedEncodingException {
        Document sampleResponse = DocumentUtils.stringToDocument(FileUtil
                .loadAsString("/sampleResponse.xml"));
        Long expectedLength = new Long(DocumentUtils.documentToString(sampleResponse).getBytes().length + "\n#2931\n".getBytes().length
            + "\n##\n".getBytes().length);
        String encoded = new String(m_chunkedNetconfMessageCodec.encode(sampleResponse), "UTF-8");
        int num = encoded.split("\n##\n", -1).length - 1;
        assertEquals(2,num);
        Document decoded = m_chunkedNetconfMessageCodec.decode(encoded);
        String decodedMsg = DocumentUtils.documentToString(decoded);
        num = decodedMsg.split("\n##\n", -1).length - 1;
        assertEquals(1,num);
        assertEquals(expectedLength, new Long(m_chunkedNetconfMessageCodec.encode(sampleResponse).length));
    }

    @Test
    public void testDecodeEomReturnNull() throws NetconfMessageBuilderException, MessageToolargeException {
        assertNull(m_chunkedNetconfMessageCodec.decode(FileUtil.loadAsString("/sampleEomMessage1.txt")));
    }

}
