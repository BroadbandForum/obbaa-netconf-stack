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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.broadband_forum.obbaa.netconf.api.util.TestXML.assertXMLEquals;
import static org.broadband_forum.obbaa.netconf.api.util.TestXML.loadAsXml;
import java.io.IOException;
import org.junit.Test;
import org.xml.sax.SAXException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;

public class KillSessionRequestTest {

    private KillSessionRequest m_killSessionRequest = new KillSessionRequest();
    private String m_messageId = "101";
    private Integer m_sessionId = 4;

    @Test
    public void testGetRequestDocument() throws NetconfMessageBuilderException, SAXException, IOException {

        m_killSessionRequest.setMessageId(m_messageId);
        m_killSessionRequest.setSessionId(m_sessionId);
        assertNotNull(m_killSessionRequest.getRequestDocument());
        assertXMLEquals(loadAsXml("killSession.xml"), m_killSessionRequest.getRequestDocument().getDocumentElement());

    }

    @Test
    public void testSetAndGetSessionId() {

        assertEquals(m_killSessionRequest, m_killSessionRequest.setSessionId(m_sessionId));
        assertEquals("4", m_killSessionRequest.getSessionId().toString());
    }

    @Test
    public void testToString() {
        assertEquals("KillSessionRequest [sessionId=null]", m_killSessionRequest.toString());
    }

}
