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

import static junit.framework.TestCase.assertTrue;
import static org.broadband_forum.obbaa.netconf.api.util.ByteBufUtils.unpooledHeapByteBuf;
import static org.broadband_forum.obbaa.netconf.api.util.NetconfResources.CHUNK_SIZE;
import static org.broadband_forum.obbaa.netconf.api.util.NetconfResources.MAXIMUM_SIZE_OF_CHUNKED_MESSAGES;
import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.broadband_forum.obbaa.netconf.api.LogAppNames;
import org.broadband_forum.obbaa.netconf.api.MessageToolargeException;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.utils.FileUtil;
import org.broadband_forum.obbaa.netconf.api.utils.SystemPropertyUtilsUserTest;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.custommonkey.xmlunit.Diff;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import io.netty.buffer.ByteBuf;
import junit.framework.Assert;

public class ChunkedNetconfMessageCodecV2Test extends SystemPropertyUtilsUserTest {

    private ChunkedNetconfMessageCodecV2 m_codec;
    private ByteBuf m_buf;
    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(ChunkedNetconfMessageCodecV2.class, LogAppNames.NETCONF_LIB);

    @Before
    public void setUp() {
        mockPropertyUtils(MAXIMUM_SIZE_OF_CHUNKED_MESSAGES, "200");
        mockPropertyUtils(CHUNK_SIZE, "200");
        m_codec = new ChunkedNetconfMessageCodecV2();
        m_buf = unpooledHeapByteBuf();
    }

    @Test
    public void testChunkedDecodingWithOneProperChunk() throws NetconfMessageBuilderException, MessageToolargeException {
        String payload = "\n#6\n<rpc/>\n##\n";
        m_buf.writeBytes(payload.getBytes());
        DocumentInfo rpc = m_codec.decode(m_buf);
        assertXMLEqual(DocumentUtils.stringToDocument("<rpc/>"), rpc.getDocument());

        //test for invalidRange
        m_buf.clear();
        payload = "\n#0\n##\n";
        m_buf.writeBytes(payload.getBytes());
        try {
            m_codec.decode(m_buf);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertEquals("Got invalid value for chunk header: 0, byte: 48", e.getMessage());
        }
    }

    @Test
    public void testChunkedDecodingWithImproperChunk() throws NetconfMessageBuilderException, MessageToolargeException {
        String payload = "\n#x\n\n##\n";
        m_buf.writeBytes(payload.getBytes());
        try {
            m_codec.decode(m_buf);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertEquals("Got invalid value for chunk header: x, byte: 120", e.getMessage());
        }
    }

    @Test
    public void testChunkedDecodingWithOneChunkWhenBufferStillReading() throws NetconfMessageBuilderException, MessageToolargeException {
        String payloadPart1 = "\n#6\n<rp";
        String payloadPart2 = "c/>";
        String payloadPart3 = "\n##\n";
        m_buf.writeBytes(payloadPart1.getBytes());
        DocumentInfo rpc;
        rpc = m_codec.decode(m_buf);
        assertNull(rpc);
        m_buf.writeBytes(payloadPart2.getBytes());
        rpc = m_codec.decode(m_buf);
        assertNull(rpc);
        m_buf.writeBytes(payloadPart3.getBytes());
        rpc = m_codec.decode(m_buf);
        assertXMLEqual(DocumentUtils.stringToDocument("<rpc/>"), rpc.getDocument());
    }

    @Test
    public void testChunkedDecodingWhenHeaderLengthIsLesserThanChunkContent() throws NetconfMessageBuilderException, MessageToolargeException {
        String payload = "\n#5\n<rpc/>\n##\n";
        m_buf.writeBytes(payload.getBytes());
        try {
            m_codec.decode(m_buf);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertEquals("Got invalid character > (byte)62, while expecting \n", e.getMessage());
        }
    }

    @Test
    public void testChunkedDecodingWhenHeaderLengthIsGreaterThanChunkContent() throws NetconfMessageBuilderException, MessageToolargeException {
        String  payload = "\n#7\n<rpc/>\n##\n";
        m_buf.writeBytes(payload.getBytes());
        try {
            m_codec.decode(m_buf);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertEquals("Got invalid character # (byte)35, while expecting \n", e.getMessage());
        }
    }

    @Test
    public void testChunkedDecodingWhenHeaderLengthIsMoreThan1Digit() throws NetconfMessageBuilderException, MessageToolargeException {
        String payload = "\n#39\n<test-header-length-more-than-1-digit/>\n##\n";
        m_buf.writeBytes(payload.getBytes());
        Document rpc = m_codec.decode(m_buf).getDocument();
        assertXMLEqual(DocumentUtils.stringToDocument("<test-header-length-more-than-1-digit/>"), rpc);
    }

    @Test
    public void testChunkedDecodingWhenThereIsMoreThan1Chunk() throws NetconfMessageBuilderException, MessageToolargeException {
        String expectedRpc = "<rpc message-id=\"100\"\nxmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n  <close-session/>\n</rpc>";
        String payload = "\n#4\n<rpc\n#18\n message-id=\"100\"\n\n\n  \n     \n#78\n     xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n  <close-session/>\n</rpc\n#1\n>\n##\n";
        m_buf.writeBytes(payload.getBytes());
        DocumentInfo rpc = m_codec.decode(m_buf);
        assertXMLEqual(DocumentUtils.stringToDocument(expectedRpc), rpc.getDocument());

        //When there are multiple invocations to processBytes
        m_buf.clear();
        String payloadPart1 = "\n#4\n<rpc\n#18\n message-id=\"100\"\n\n#";
        String payloadPart2 = "79\n     xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n  <close-session/>\n</rpc>\n#";
        String payloadPart3 = "#\n";
        m_buf.writeBytes(payloadPart1.getBytes());
        rpc = m_codec.decode(m_buf);
        assertNull(rpc);
        m_buf.writeBytes(payloadPart2.getBytes());
        rpc = m_codec.decode(m_buf);
        assertNull(rpc);
        m_buf.writeBytes(payloadPart3.getBytes());
        rpc = m_codec.decode(m_buf);
        assertXMLEqual(DocumentUtils.stringToDocument(expectedRpc), rpc.getDocument());

        //When the header of next chunk is invalid((i.e), neither Chunk EOM nor proper header first length)
        m_buf.clear();
        payload = "\n#6\n<rpc/>\n#0\n";
        m_buf.writeBytes(payload.getBytes());
        try {
            m_codec.decode(m_buf);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertEquals("Got invalid value: 0, byte: 48", e.getMessage());
        }
    }

    @Test
    public void testChunkedDecodingMultipleChunksWith3DigitChunkHeader() throws NetconfMessageBuilderException, MessageToolargeException {
        String payload = "\n#162\n<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<rpc-reply            \n" +
                "           xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "  <rpc-error>\n" +
                "  </rpc-error>\n" +
                "</rpc-reply\n#1\n>\n##\n";
        String expectedRpc = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "  <rpc-error>\n" +
                "  </rpc-error>\n" +
                "</rpc-reply>";

        m_buf.writeBytes(payload.getBytes());
        Document rpc = m_codec.decode(m_buf).getDocument();
        assertXMLEqual(DocumentUtils.stringToDocument(expectedRpc), rpc);
    }

    @Test
    public void testWhenChunkSizeExceedsMax() throws NetconfMessageBuilderException, MessageToolargeException {
        String payload = "\n#315\n<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "  <rpc-error>\n" +
                "    <error-type>transport</error-type>\n" +
                "    <error-tag>malformed-message</error-tag>\n" +
                "    <error-severity>error</error-severity>\n" +
                "    <error-app-tag>data-invalid</error-app-tag>\n" +
                "  </rpc-error>\n" +
                "</rpc-reply>\n##\n";
        m_buf.writeBytes(payload.getBytes());
        try {
            m_codec.decode(m_buf);
            fail("Should have thrown a MessageToolargeException");
        } catch (MessageToolargeException e) {
            assertEquals("Incoming message too long. Not decoding the message further" +
                    " to avoid buffer overrun, message size: 315 max-size: 200", e.getMessage());
        }
    }
    
    @Test
    public void testChunkedDecodingWithLeadingAndTrailingLineFeeds() throws NetconfMessageBuilderException, MessageToolargeException {
        //test for trailing line feeds
        String payload = "\n#6\n<rpc/>\n##\n\n\n\n\n\n";
        m_buf.writeBytes(payload.getBytes());
        Document rpc = m_codec.decode(m_buf).getDocument();
        assertXMLEqual(DocumentUtils.stringToDocument("<rpc/>"), rpc);

        //test for leading line feeds and spaces
        payload = "\n\n\n\n\n      \n#6\n<rpc/>\n##\n";
        m_buf.writeBytes(payload.getBytes());
        rpc = m_codec.decode(m_buf).getDocument();
        assertXMLEqual(DocumentUtils.stringToDocument("<rpc/>"), rpc);
    }

    @Test
    public void testChunkedDecodingFor2ChunkEom() throws NetconfMessageBuilderException, MessageToolargeException {
        String payload = "\n\n#6\n<rpc/>\n##\n      \n \n#7\n<rpc2/>\n##\n";
        m_buf.writeBytes(payload.getBytes());
        DocumentInfo rpc = m_codec.decode(m_buf);
        assertXMLEqual(DocumentUtils.stringToDocument("<rpc/>"), rpc.getDocument());
        rpc = m_codec.decode(m_buf);
        assertXMLEqual(DocumentUtils.stringToDocument("<rpc2/>"), rpc.getDocument());

        //When there are multiple invocations to processBytes for 2 Chunked EOMs
        m_buf.clear();
        String payload1 = "\n#6\n<rpc/>\n";
        String payload2 = "##\n\n#7\n<rpc2/>\n##\n";
        m_buf.writeBytes(payload1.getBytes());
        rpc = m_codec.decode(m_buf);
        assertNull(rpc);
        m_buf.writeBytes(payload2.getBytes());
        rpc = m_codec.decode(m_buf);
        assertXMLEqual(DocumentUtils.stringToDocument("<rpc/>"), rpc.getDocument());
        rpc = m_codec.decode(m_buf);
        assertXMLEqual(DocumentUtils.stringToDocument("<rpc2/>"), rpc.getDocument());
    }

    @Test
    public void testChunkEomAsComment() throws NetconfMessageBuilderException, IOException, SAXException, MessageToolargeException {
        String payload = "\n\n#17\n<!--\n##\n--><rpc/>\n##\n";
        m_buf.writeBytes(payload.getBytes());
        Document rpc = m_codec.decode(m_buf).getDocument();
        assertEquals("<!--\n##\n--><rpc/>", DocumentUtils.documentToPrettyString(rpc).trim());
    }

    @Test
    public void testEncodeChunkedMessage() throws NetconfMessageBuilderException, MessageToolargeException, UnsupportedEncodingException {
        mockPropertyUtils(MAXIMUM_SIZE_OF_CHUNKED_MESSAGES, "65536");
        mockPropertyUtils(CHUNK_SIZE, "65536");
        ChunkedNetconfMessageCodecV2 codec2 = new ChunkedNetconfMessageCodecV2();
        Document sampleResponse = DocumentUtils.stringToDocument(FileUtil
                .loadAsString("/sampleResponse.xml"));
        Long expectedLength = new Long(DocumentUtils.documentToString(sampleResponse).getBytes().length + "\n#2931\n".getBytes().length
                + "\n##\n".getBytes().length);
        String encoded = new String(codec2.encode(sampleResponse), "UTF-8");
        int num = encoded.split("\n##\n", -1).length - 1;
        Assert.assertEquals(2,num);
        Document decoded = codec2.decode(m_buf.writeBytes(encoded.getBytes())).getDocument();
        String decodedMsg = DocumentUtils.documentToString(decoded);
        num = decodedMsg.split("\n##\n", -1).length - 1;
        Assert.assertEquals(1,num);
        Assert.assertEquals(expectedLength, new Long(codec2.encode(sampleResponse).length));
    }


    @Test
    public void testDecodeChunkedMessage() throws NetconfMessageBuilderException, MessageToolargeException {
        mockPropertyUtils(MAXIMUM_SIZE_OF_CHUNKED_MESSAGES, "65536");
        mockPropertyUtils(CHUNK_SIZE, "65536");
        m_codec = new ChunkedNetconfMessageCodecV2();

        Document actual = m_codec.decode(m_buf.writeBytes(FileUtil.loadAsString("/sampleChunkedMessage1.txt").getBytes())).getDocument();
        Document expected = DocumentUtils.stringToDocument(FileUtil.loadAsString("/expectedMessage1.txt"));
        Diff diff = new Diff(expected, actual);
        boolean similar = diff.similar();
        LOGGER.info(diff.toString());
        assertTrue(similar);

        actual = m_codec.decode(m_buf.writeBytes(FileUtil.loadAsString("/sampleChunkedMessage6.txt").getBytes())).getDocument();
        expected = DocumentUtils.stringToDocument(FileUtil.loadAsString("/expectedMessage6.txt"));
        diff = new Diff(expected, actual);
        similar = diff.similar();
        LOGGER.info(diff.toString());
        assertTrue(similar);

        actual = m_codec.decode(m_buf.writeBytes(FileUtil.loadAsString("/sampleChunkedMessage7.txt").getBytes())).getDocument();
        expected = DocumentUtils.stringToDocument(FileUtil.loadAsString("/expectedMessage7.txt"));
        diff = new Diff(expected, actual);
        similar = diff.similar();
        LOGGER.info(diff.toString());
        assertTrue(similar);

        actual = m_codec.decode(m_buf.writeBytes(FileUtil.loadAsString("/sampleChunkedMessage2.txt").getBytes())).getDocument();
        expected = DocumentUtils.stringToDocument(FileUtil.loadAsString("/expectedMessage2.txt"));
        diff = new Diff(expected, actual);
        similar = diff.similar();
        LOGGER.info(diff.toString());
        assertTrue(similar);

        actual = m_codec.decode(m_buf.writeBytes(FileUtil.loadAsString("/sampleChunkedMessage4.txt").getBytes())).getDocument();
        expected = DocumentUtils.stringToDocument(FileUtil.loadAsString("/expectedMessage2.txt"));
        diff = new Diff(expected, actual);
        similar = diff.similar();
        LOGGER.info(diff.toString());
        assertTrue(similar);
    }

    @Test
    public void testDecodeChunkedMessageWithException() throws NetconfMessageBuilderException {
        m_codec = new ChunkedNetconfMessageCodecV2();
        try{
            m_codec.decode(m_buf.writeBytes(FileUtil.loadAsString("/sampleChunkedMessage1.txt").getBytes()));
            fail("Should have thrown a MessageToolargeException");
        } catch (MessageToolargeException e){
            assertEquals("Incoming message too long. Not decoding the message further to avoid buffer overrun, message size: 581 max-size: 200",e.getMessage());
        }
    }

    @Test
    public void testMessageTooLargeExceptionWith2Chunks() throws NetconfMessageBuilderException {
        ChunkedNetconfMessageCodecV2 codec = new ChunkedNetconfMessageCodecV2(580, 500);
        try {
            String chunk = "\n" +
                    "#580\n" +
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<rpc-reply xmlns:ncx=\"http://netconfcentral.org/ns/yuma-ncx\"\n" +
                    "  xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                    "  <rpc-error>\n" +
                    "    <error-type>transport</error-type>\n" +
                    "    <error-tag>malformed-message</error-tag>\n" +
                    "    <error-severity>error</error-severity>\n" +
                    "    <error-app-tag>data-invalid</error-app-tag>\n" +
                    "    <error-message xml:lang=\"en\">invalid protocol framing characters received</error-message>\n" +
                    "    <error-info>\n" +
                    "      <error-number xmlns=\"http://netconfcentral.org/ns/yuma-ncx\">378</error-number>\n" +
                    "    </error-info>\n" +
                    "  </rpc-error>\n" +
                    "</rpc-reply\n" +
                    "#1\n" +
                    ">\n" +
                    "##\n";
            codec.decode(m_buf.writeBytes(chunk.getBytes()));
            fail("Should have thrown a MessageToolargeException");
        } catch (MessageToolargeException e) {
            assertEquals("Incoming message too long. Not decoding the message further to avoid buffer overrun, message size: 581 max-size: 580",e.getMessage());
        }
    }
    @Test
    public void testMessageParsedWhenChunkMsgSizeEqualToMax() throws NetconfMessageBuilderException, MessageToolargeException {
        ChunkedNetconfMessageCodecV2 codec = new ChunkedNetconfMessageCodecV2(410, 400);
        String chunk = "\n#400\n<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<rpc-reply xmlns:ncx=\"http://netconfcentral.org/ns/yuma-ncx\"\n" +
                "  xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "  <rpc-error>\n" +
                "    <error-type>transport</error-type>\n" +
                "    <error-tag>malformed-message</error-tag>\n" +
                "    <error-severity>error</error-severity>\n" +
                "    <error-app-tag>data-invalid</error-app-tag>\n" +
                "    <error-message>testmsg</error-message>\n" +
                "  </rpc-error>\n" +
                "</\n#10\nrpc-reply>\n##\n";
        String expectedRpc = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<rpc-reply xmlns:ncx=\"http://netconfcentral.org/ns/yuma-ncx\"\n" +
                "  xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "  <rpc-error>\n" +
                "    <error-type>transport</error-type>\n" +
                "    <error-tag>malformed-message</error-tag>\n" +
                "    <error-severity>error</error-severity>\n" +
                "    <error-app-tag>data-invalid</error-app-tag>\n" +
                "    <error-message>testmsg</error-message>\n" +
                "  </rpc-error>\n" +
                "</rpc-reply>";
        assertXMLEqual(DocumentUtils.stringToDocument(expectedRpc), codec.decode(m_buf.writeBytes(chunk.getBytes())).getDocument());
    }
}
