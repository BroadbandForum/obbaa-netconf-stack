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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.junit.Test;
import org.xml.sax.SAXException;

public class GetRequestTest {

    public static final String RUNNING_DATA_STORE = StandardDataStores.RUNNING;
    private NetconfFilter m_filter = new NetconfFilter();
    private WithDefaults m_withDefaults = WithDefaults.REPORT_ALL;
    private boolean m_includeConfig = true;
    private GetRequest m_getRequest = new GetRequest();
    private String m_messageId = "101";
    private int m_withDelay = 1;
    private int m_depth = 1;

    @Test
    public void testIsIncludeConfig() {
        m_getRequest.setIncludeConfig(m_includeConfig);
        assertTrue(m_getRequest.isIncludeConfig());
    }

    @Test
    public void testSetAndGetFilter() {

        NetconfFilter netconfFilter2 = m_getRequest.getFilter();
        m_getRequest.setFilter(m_filter);
        assertNotEquals(netconfFilter2, m_filter);
    }

    @Test
    public void testSetAndGetWithDefaults() {

        WithDefaults withDefaults = m_getRequest.getWithDefault();
        m_getRequest.setWithDefaults(m_withDefaults);
        assertNotEquals(withDefaults, m_withDefaults);
    }

    @Test
    public void testGetRequestDocument() throws NetconfMessageBuilderException, SAXException, IOException {

        m_getRequest.setDepth(m_depth);
        m_getRequest.setFilter(m_filter.setType("subtree"));
        m_getRequest.setWithDefaults(m_withDefaults);
        m_getRequest.setWithDelay(m_withDelay);
        m_getRequest.setMessageId(m_messageId);
        assertNotNull(m_getRequest.getRequestDocument());
        assertXMLEquals(loadAsXml("TestXMLResources/getTest.xml"), m_getRequest.getRequestDocument()
                .getDocumentElement());

    }

}
