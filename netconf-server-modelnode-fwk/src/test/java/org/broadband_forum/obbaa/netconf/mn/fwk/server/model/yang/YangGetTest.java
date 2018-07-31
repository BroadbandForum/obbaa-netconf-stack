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

import java.io.IOException;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

public class YangGetTest extends AbstractYangValidationTestSetup {

    @Before
    public void initServer() throws SchemaBuildException, ModelNodeInitException {
        super.setup();
    }

    @Test
    public void testGetWithoutFiltering() throws SAXException, IOException {
        TestUtil.verifyGet(m_server, "", "/get-unfiltered-yang.xml", "1");
    }

    @Test
    public void testGetFilterNonMatchingNode() throws SAXException, IOException {
        TestUtil.verifyGet(m_server, "/filter-non-matching.xml", "/empty-response.xml", "1");
    }

    @Test
    public void testGetFilterNonMatchingHierarchy() throws SAXException, IOException {
        TestUtil.verifyGet(m_server, "/filter-non-matching-hierarchy.xml", "/empty-response.xml", "1");
    }

    @Test
    public void testGetFilterTwoMatchNodes() throws SAXException, IOException {
        TestUtil.verifyGet(m_server, "/filter-two-matching-yang.xml", "/get-response-two-match.xml", "1");
    }

    @Test
    public void testGetFilterSelectNode() throws SAXException, IOException {
        TestUtil.verifyGet(m_server, "/filter-select.xml", "/getconfig-response-select.xml", "1");
    }

    @Test
    public void testGetFilterKeyAttrs() throws IOException, SAXException {
        TestUtil.verifyGet(m_server, "/album-state-filter5.xml", "/get-with-album-state-filter5-response.xml", "1");
    }

    @Test
    public void testGetMultipleNodes() throws SAXException, IOException {
        TestUtil.verifyGet(m_server, "/filter-multiple-nodes.xml", "/getconfig-response-multiple-nodes.xml", "1");
    }

    @Test
    public void testGetMultipleNodesAndSelect() throws SAXException, IOException {
        TestUtil.verifyGet(m_server, "/filter-multiple-nodes-with-select.xml",
                "/get-response-multiple-nodes-with-select.xml", "1");
    }

    @Test
    public void testGetFilterNonKey() throws SAXException, IOException {
        TestUtil.verifyGet(m_server, "/filter-non-key-yang.xml", "/get-response-non-key.xml", "1");
    }

    @Test
    public void testGetFilterWithMatchAndSelectNodes() throws SAXException, IOException {
        TestUtil.verifyGet(m_server, "/filter-match-and-select-nodes.xml", "/get-response-match-and-select-nodes" +
                ".xml", "1");
    }

    @Test
    public void testGetFilterWithSelectAndChildNodes() throws SAXException, IOException {
        TestUtil.verifyGet(m_server, "/filter-select-and-child-nodes.xml", "/get-response-select-and-child-nodes" +
                ".xml", "1");
    }

    @Test
    public void testGetFilterWithMultipleSelectNodes() throws SAXException, IOException {
        TestUtil.verifyGet(m_server, "/filter-multiple-select-nodes.xml", "/get-response-multiple-select-nodes.xml", "1");
    }

}
