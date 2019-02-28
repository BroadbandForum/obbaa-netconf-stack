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

public class UnLockRequestTest {

    private UnLockRequest m_unLockRequest = new UnLockRequest();
    private String RUNNING = "running";
    private String m_target = "candidate";
    private String m_messageId = "101";

    @Test
    public void testGetRequestDocument() throws NetconfMessageBuilderException, SAXException, IOException {

        m_unLockRequest.setMessageId(m_messageId);
        m_unLockRequest.setTarget(m_target);
        assertNotNull(m_unLockRequest.getRequestDocument());
        assertXMLEquals(loadAsXml("unLock.xml"), m_unLockRequest.getRequestDocument().getDocumentElement());
    }

    @Test
    public void testSetAndGetTarget() {

        assertEquals(m_unLockRequest, m_unLockRequest.setTarget(m_target));
        assertEquals(m_target, m_unLockRequest.getTarget());
    }

    @Test
    public void testSetTargetRunning() {
        UnLockRequest unLockRequest1 = m_unLockRequest.setTargetRunning();
        assertEquals(RUNNING, unLockRequest1.getTarget());
    }

    @Test
    public void testToString() {

        assertEquals("UnLockRequest [target=null]", m_unLockRequest.toString());
        assertNotNull(m_unLockRequest.toString());
    }

}
