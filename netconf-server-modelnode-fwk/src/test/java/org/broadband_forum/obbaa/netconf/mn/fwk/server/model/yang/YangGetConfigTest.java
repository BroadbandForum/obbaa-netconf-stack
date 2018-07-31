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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.yang;

import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.verifyGetConfig;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;

public class YangGetConfigTest extends AbstractYangValidationTestSetup {


    @Before
    public void initServer() throws SchemaBuildException, ModelNodeInitException {
        super.setup();
    }

    @Test
    public void testGetConfigWithoutFiltering() throws SAXException, IOException {
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/getconfig-unfiltered-yang_edited.xml", "1");
    }

    @Test
    public void testGetConfigFilterNonMatchingNode() throws SAXException, IOException {
        verifyGetConfig(m_server, StandardDataStores.RUNNING, "/filter-non-matching.xml", "/empty-response.xml", "1");
    }

    @Test
    public void testGetConfigFilterNonMatchingHierarchy() throws SAXException, IOException {
        verifyGetConfig(m_server, StandardDataStores.RUNNING, "/filter-non-matching-hierarchy.xml", "/empty-response" +
                ".xml", "1");
    }

    @Test
    public void testGetConfigFilterTwoMatchNodes() throws SAXException, IOException {
        verifyGetConfig(m_server, StandardDataStores.RUNNING, "/filter-two-matching-yang.xml",
                "/get-response-two-match.xml", "1");
    }

    @Test
    public void testGetConfigFilterSelectNode() throws SAXException, IOException {
        verifyGetConfig(m_server, StandardDataStores.RUNNING, "/filter-select.xml",
                "/getconfig-response-select_edited.xml", "1");
    }

    @Test
    public void testGetConfigMultipleNodes() throws SAXException, IOException {
        verifyGetConfig(m_server, StandardDataStores.RUNNING, "/filter-multiple-nodes.xml",
                "/getconfig-response-multiple-nodes_edited.xml", "1");
    }

    @Test
    public void testGetConfigMultipleNodesAndSelect() throws SAXException, IOException {
        verifyGetConfig(m_server, StandardDataStores.RUNNING, "/filter-multiple-nodes-with-select.xml", "/get-response-multiple-nodes-with-select_edited.xml", "1");
    }
}
