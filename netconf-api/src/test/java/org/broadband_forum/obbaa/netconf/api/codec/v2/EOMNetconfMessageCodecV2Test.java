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
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;
import static org.broadband_forum.obbaa.netconf.api.util.ByteBufUtils.unpooledHeapByteBuf;
import static org.broadband_forum.obbaa.netconf.api.util.NetconfResources.RPC_EOM_DELIMITER;

import org.broadband_forum.obbaa.netconf.api.LogAppNames;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.utils.FileUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.custommonkey.xmlunit.Diff;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import io.netty.buffer.CompositeByteBuf;
import junit.framework.Assert;

public class EOMNetconfMessageCodecV2Test {
    private EOMNetconfMessageCodecV2 m_codec;
    private CompositeByteBuf m_buf;
    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(EOMNetconfMessageCodecV2Test.class, LogAppNames.NETCONF_LIB);

    @Before
    public void setUp() {
        m_codec = new EOMNetconfMessageCodecV2();
        m_buf = unpooledHeapByteBuf();
    }

    @Test
    public void testSingleDocIsProcessed() throws Exception {
        m_buf.writeBytes(("<rpc/>" + RPC_EOM_DELIMITER).getBytes());
        assertEquals ("<rpc/>\n", DocumentUtils.documentToPrettyString(m_codec.decode(m_buf).getDocument()));
    }

    @Test
    public void testSingleDocIsProcessedWithLeadingAndTrailingLineFeedsAndSpaces() throws Exception {
        //test for trailing linefeeds and spaces
        m_buf.writeBytes(("<rpc/>" + RPC_EOM_DELIMITER + "\n  \n \n").getBytes());
        assertEquals ("<rpc/>\n", DocumentUtils.documentToPrettyString(m_codec.decode(m_buf).getDocument()));

        //test for leading linefeeds and spaces
        m_buf.writeBytes(("\n\n \n   \n     <rpc/>" + RPC_EOM_DELIMITER).getBytes());
        assertEquals ("<rpc/>\n", DocumentUtils.documentToPrettyString(m_codec.decode(m_buf).getDocument()));
    }

    @Test
    public void testSingleDocMultiplePackets() throws NetconfMessageBuilderException {
        m_buf.writeBytes(("<rpc/").getBytes());
        assertNull(m_codec.decode(m_buf));
        m_buf.writeBytes((">" + RPC_EOM_DELIMITER).getBytes());
        assertEquals("<rpc/>\n", DocumentUtils.documentToPrettyString(m_codec.decode(m_buf).getDocument()));
    }

    @Test
    public void testMultipleRpcsBeingRead() throws NetconfMessageBuilderException {
        m_buf.writeBytes(("<rpc/>" + RPC_EOM_DELIMITER + "<rpc2/>" + RPC_EOM_DELIMITER + "<rpc3/>" + RPC_EOM_DELIMITER + "<rpc").getBytes());
        assertEquals("<rpc/>\n", DocumentUtils.documentToPrettyString(m_codec.decode(m_buf).getDocument()));
        assertEquals("<rpc2/>\n", DocumentUtils.documentToPrettyString(m_codec.decode(m_buf).getDocument()));
        assertEquals("<rpc3/>\n", DocumentUtils.documentToPrettyString(m_codec.decode(m_buf).getDocument()));
        assertEquals(4, m_buf.readableBytes());
    }

    @Test
    public void testMultipleRpcsBeingReadInDifferentPackets() throws NetconfMessageBuilderException {
        m_buf.writeBytes(("<rpc/>" + RPC_EOM_DELIMITER + "<rpc2/>      \n\n\n" +  RPC_EOM_DELIMITER + "<rpc").getBytes());
        assertEquals("<rpc/>\n", DocumentUtils.documentToPrettyString(m_codec.decode(m_buf).getDocument()));
        assertEquals("<rpc2/>\n", DocumentUtils.documentToPrettyString(m_codec.decode(m_buf).getDocument()));
        m_buf.writeBytes(("3/>" + RPC_EOM_DELIMITER + "       \n\n  \n  <rpc4/>").getBytes());
        assertEquals("<rpc3/>\n", DocumentUtils.documentToPrettyString(m_codec.decode(m_buf).getDocument()));
        m_buf.writeBytes(RPC_EOM_DELIMITER.getBytes());
        assertEquals("<rpc4/>\n", DocumentUtils.documentToPrettyString(m_codec.decode(m_buf).getDocument()));
    }

    @Test
    public void testEncodeEom() throws NetconfMessageBuilderException {
        Document sampleResponse = DocumentUtils.stringToDocument(FileUtil
                .loadAsString("/sampleResponseEOM.xml"));
        // 1 is added because the we are doing INDENT as "yes" in DocumentToPojoTransformer.getBytesFromDocument
        Long expectedLength = new Long(DocumentUtils.documentToString(sampleResponse).getBytes().length + 1 + RPC_EOM_DELIMITER.length());
        Assert.assertEquals(expectedLength, new Long(m_codec.encode(sampleResponse).length));
    }

    @Test
    public void testDecodeEom() throws NetconfMessageBuilderException {

        Document actual = m_codec.decode(m_buf.writeBytes(FileUtil
                .loadAsString("/sampleEomMessage1.txt").replaceAll("]]>]]>\\n", "]]>]]>").getBytes())).getDocument();

        Document expected = DocumentUtils.stringToDocument(FileUtil.loadAsString("/expectedEomMessage1.txt"));
        Diff diff = new Diff(expected, actual);
        boolean similar = diff.similar();
        LOGGER.info(diff.toString());
        assertTrue(similar);

        actual = m_codec.decode(m_buf.writeBytes(FileUtil.loadAsString("/sampleEomMessage2.txt")
                        .replaceAll("]]>]]>\\n", "]]>]]>").getBytes())).getDocument();
        expected = DocumentUtils.stringToDocument(FileUtil.loadAsString("/expectedEomMessage2.txt"));
        diff = new Diff(expected, actual);
        similar = diff.similar();
        LOGGER.info(diff.toString());
        assertTrue(similar);
    }

    @Test
    public void testDecodeChunkedReturnNull() throws NetconfMessageBuilderException {
        org.junit.Assert.assertNull(m_codec.decode(m_buf.writeBytes(FileUtil.loadAsString("/sampleChunkedMessage1.txt").getBytes())));
    }
}
