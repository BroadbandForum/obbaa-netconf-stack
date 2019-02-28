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

package org.broadband_forum.obbaa.netconf.api.messages;

import static org.broadband_forum.obbaa.netconf.api.util.TestXML.assertXMLEquals;
import static org.broadband_forum.obbaa.netconf.api.util.TestXML.loadAsXml;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import java.io.IOException;
import org.junit.Test;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;

public class CopyConfigRequestTest {

    private CopyConfigRequest m_copyConfigRequest = new CopyConfigRequest();
    private static final String TEST_SOURCE = "https://user:password@example.com/cfg/new.txt";
    private static final String TEST_TARGET = StandardDataStores.RUNNING;
    private String m_source = TEST_SOURCE;
    private boolean m_sourceIsUrl = false;
    private String m_target = TEST_TARGET;
    private boolean m_targetIsUrl = false;
    private String m_messageId = "101";
    private Element m_sourceConfigElement = mock(Element.class);

    @Test
    public void testGetRequestDocument() throws NetconfMessageBuilderException, SAXException, IOException {

        m_copyConfigRequest.setSource(m_source, true);
        m_copyConfigRequest.setTarget(m_target, true);
        m_copyConfigRequest.setTargetRunning();
        m_copyConfigRequest.setSourceConfigElement(m_sourceConfigElement);
        m_copyConfigRequest.setMessageId(m_messageId);
        assertNotNull(m_copyConfigRequest.getRequestDocument());
        assertXMLEquals(loadAsXml("copyConfig.xml"), m_copyConfigRequest.getRequestDocument().getDocumentElement());

    }

    @Test
    public void testSetAndGetSource() {

        assertEquals(m_copyConfigRequest, m_copyConfigRequest.setSource(m_source, m_sourceIsUrl));
        assertEquals(TEST_SOURCE, m_copyConfigRequest.getSource());
    }

    @Test
    public void testSetSourceRunning() {
        CopyConfigRequest copyConfigRequest1 = m_copyConfigRequest.setSourceRunning();
        assertEquals(StandardDataStores.RUNNING, copyConfigRequest1.getSource());

    }

    @Test
    public void testSetAndGetTarget() {

        assertEquals(m_copyConfigRequest, m_copyConfigRequest.setTarget(m_target, m_targetIsUrl));
        assertEquals(TEST_TARGET, m_copyConfigRequest.getTarget());
    }

    @Test
    public void testSetTargetRunning() {
        CopyConfigRequest copyConfigRequest = m_copyConfigRequest.setTargetRunning();
        assertEquals(StandardDataStores.RUNNING, copyConfigRequest.getTarget());
    }

    @Test
    public void testSetAndGetSourceConfigElement() {
        assertEquals(m_copyConfigRequest, m_copyConfigRequest.setSourceConfigElement(m_sourceConfigElement));
        assertNotNull(m_copyConfigRequest.getSourceConfigElement());
    }

    @Test
    public void testToString() {
        assertEquals("CopyConfigRequest [source=null, sourceIsUrl=false, target=null, targetIsUrl=false, sourceConfigElement=null]",
                m_copyConfigRequest.toString());
        assertNotNull(m_copyConfigRequest);
    }

}
