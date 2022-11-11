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
import static org.broadband_forum.obbaa.netconf.api.util.NetconfResources.RPC_EOM_DELIMITER;
import static org.junit.Assert.assertNull;

import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.utils.FileUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.custommonkey.xmlunit.Diff;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

public class EomNetconfMessageCodecTest {
    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(EomNetconfMessageCodecTest.class, LogAppNames.NETCONF_LIB);

    private EomNetconfMessageCodec m_eomNetconfMessageCodec;

    @Before
    public void setup() {
        m_eomNetconfMessageCodec = new EomNetconfMessageCodec();
    }

    @Test
    public void testDecodeEom() throws NetconfMessageBuilderException {

        Document actual = m_eomNetconfMessageCodec.decode(FileUtil
                .loadAsString("/sampleEomMessage1.txt").replaceAll("]]>]]>\\n", "]]>]]>"));

        Document expected = DocumentUtils.stringToDocument(FileUtil.loadAsString("/expectedEomMessage1.txt"));
        Diff diff = new Diff(expected, actual);
        boolean similar = diff.similar();
        LOGGER.info(diff.toString());
        assertTrue(similar);

        actual = m_eomNetconfMessageCodec.decode(FileUtil.loadAsString("/sampleEomMessage2.txt").replaceAll("]]>]]>\\n", "]]>]]>"));
        expected = DocumentUtils.stringToDocument(FileUtil.loadAsString("/expectedEomMessage2.txt"));
        diff = new Diff(expected, actual);
        similar = diff.similar();
        LOGGER.info(diff.toString());
        assertTrue(similar);

    }

    @Test
    public void testEncodeEom() throws NetconfMessageBuilderException {
        Document sampleResponse = DocumentUtils.stringToDocument(FileUtil
                .loadAsString("/sampleResponseEOM.xml"));
        // 1 is added because the we are doing INDENT as "yes" in DocumentToPojoTransformer.getBytesFromDocument
        Long expectedLength = new Long(DocumentUtils.documentToString(sampleResponse).getBytes().length + 1 + RPC_EOM_DELIMITER.length());
        assertEquals(expectedLength, new Long(m_eomNetconfMessageCodec.encode(sampleResponse).length));
    }

    @Test
    public void testDecodeChunkedReturnNull() throws NetconfMessageBuilderException {
        assertNull(m_eomNetconfMessageCodec.decode(FileUtil.loadAsString("/sampleChunkedMessage1.txt")));
    }
}
