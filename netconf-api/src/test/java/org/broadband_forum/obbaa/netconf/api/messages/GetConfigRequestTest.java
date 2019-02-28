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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import java.io.IOException;
import org.junit.Test;
import org.xml.sax.SAXException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;

public class GetConfigRequestTest {

    public static final String RUNNING_DATA_STORE = StandardDataStores.RUNNING;
    private String m_source = RUNNING_DATA_STORE;
    private NetconfFilter m_filter = new NetconfFilter();
    private WithDefaults m_withDefaults = WithDefaults.REPORT_ALL;
    private GetConfigRequest m_getConfigRequest = new GetConfigRequest();
    private int m_depth = 1;
    private int m_withDelay = 1;
    private String m_messageId = "101";

    @Test
    public void testSetAndGetSource() {

        assertEquals(m_getConfigRequest, m_getConfigRequest.setSource(m_source));
        assertEquals("running", m_getConfigRequest.getSource());
    }

    @Test
    public void testSetAndGetFilter() {
        NetconfFilter netconfFilter2 = m_getConfigRequest.getFilter();
        m_getConfigRequest.setFilter(m_filter);
        assertNotEquals(netconfFilter2, m_filter);
    }

    @Test
    public void testSetSourceRunning() {
        GetConfigRequest getConfigRequest1 = m_getConfigRequest.setSourceRunning();
        assertEquals("running", getConfigRequest1.getSource());

    }

    @Test
    public void testSetAndGetWithDefaults() {
        WithDefaults withDefaults = m_getConfigRequest.getWithDefaults();
        m_getConfigRequest.setWithDefaults(m_withDefaults);
        assertNotEquals(withDefaults, m_withDefaults);
    }

    @Test
    public void testGetRequestDocument() throws NetconfMessageBuilderException, SAXException, IOException {

        m_getConfigRequest.setDepth(m_depth);
        m_getConfigRequest.setFilter(m_filter.setType("subtree"));
        m_getConfigRequest.setWithDefaults(m_withDefaults);
        m_getConfigRequest.setWithDelay(m_withDelay);
        m_getConfigRequest.setMessageId(m_messageId);
        m_getConfigRequest.setSource(m_source);
        assertNotNull(m_getConfigRequest.getRequestDocument());
        assertXMLEquals(loadAsXml("TestXMLResources/getConfigTest.xml"), m_getConfigRequest.getRequestDocument().getDocumentElement());

    }
}
