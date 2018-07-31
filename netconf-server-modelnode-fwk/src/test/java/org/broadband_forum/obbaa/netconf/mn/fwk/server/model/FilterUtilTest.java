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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class FilterUtilTest {

    private static final String FILTER_WITH_MULTIPLE_SELECT_NODES =
            "<jukebox xmlns=\"http://example.com/ns/example-jukebox\">\n" +
                    "  <library>\n" +
                    "    <artist/>\n" +
                    "    <artist/>\n" +
                    "  </library>\n" +
                    "</jukebox>\n";
    private static final String FILTER_WITH_SELECT_AND_CONTAINMENT_NODES1 =
            "<jukebox xmlns=\"http://example.com/ns/example-jukebox\">\n" +
                    "  <library>\n" +
                    "    <artist/>\n" +
                    "    <artist>\n" +
                    "      <album>\n" +
                    "        <song/>\n" +
                    "      </album>\n" +
                    "    </artist>\n" +
                    "  </library>\n" +
                    "</jukebox>\n";

    private static final String FILTER_WITH_SELECT_AND_CONTAINMENT_NODES2 =
            "<jukebox xmlns=\"http://example.com/ns/example-jukebox\">\n" +
                    "  <library>\n" +
                    "    <artist/>\n" +
                    "    <artist>\n" +
                    "      <album>\n" +
                    "        <song>\n" +
                    "          <name>Song name</name>\n" +
                    "        </song>\n" +
                    "      </album>\n" +
                    "    </artist>\n" +
                    "  </library>\n" +
                    "</jukebox>\n";
    private static final String FILTER_WITH_SELECT_AND_CONTAINMENT_NODES3 =
            "<jukebox xmlns=\"http://example.com/ns/example-jukebox\">\n" +
                    "  <library>\n" +
                    "    <artist>\n" +
                    "      <album>\n" +
                    "        <song>\n" +
                    "          <name>Song name1</name>\n" +
                    "        </song>\n" +
                    "      </album>\n" +
                    "    </artist>\n" +
                    "    <artist/>\n" +
                    "  </library>\n" +
                    "</jukebox>\n";

    private static final String FILTER_WITH_SELECT_AND_MATCH_NODES =
            "<jukebox xmlns=\"http://example.com/ns/example-jukebox\">\n" +
                    "  <library>\n" +
                    "    <artist>\n" +
                    "      <album>\n" +
                    "        <song>\n" +
                    "          <name/>\n" +
                    "          <name>Song name</name>\n" +
                    "        </song>\n" +
                    "      </album>\n" +
                    "    </artist>\n" +
                    "  </library>\n" +
                    "</jukebox>\n";
    private static final String FILTER_WITH_MULTIPLE_MATCH_NODES =
            "<jukebox xmlns=\"http://example.com/ns/example-jukebox\">\n" +
                    "  <library>\n" +
                    "    <artist>\n" +
                    "      <album>\n" +
                    "        <song>\n" +
                    "          <name>Song name1</name>\n" +
                    "        </song>\n" +
                    "        <song>\n" +
                    "          <name>Song name2</name>\n" +
                    "        </song>\n" +
                    "      </album>\n" +
                    "    </artist>\n" +
                    "  </library>\n" +
                    "</jukebox>\n";
    @Mock
    private SchemaRegistry m_schemaRegistry;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testProcessFilter_FilterWithMultipleSelectNodes() throws NetconfMessageBuilderException {
        FilterNode root = new FilterNode();
        FilterUtil.processFilter(root, Arrays.asList(DocumentUtils.stringToDocument
                (FILTER_WITH_MULTIPLE_SELECT_NODES).getDocumentElement()));

        assertEquals(0, root.getMatchNodes().size());
        assertEquals(0, root.getSelectNodes().size());
        assertEquals(1, root.getChildNodes().size());

        FilterNode jbNode = root.getChildNodes().get(0);
        assertEquals("jukebox", jbNode.getNodeName());
        assertEquals("http://example.com/ns/example-jukebox", jbNode.getNamespace());
        assertEquals(0, jbNode.getMatchNodes().size());
        assertEquals(0, jbNode.getSelectNodes().size());
        assertEquals(1, jbNode.getChildNodes().size());

        FilterNode libraryNode = jbNode.getChildNodes().get(0);
        assertEquals("library", libraryNode.getNodeName());
        assertEquals("http://example.com/ns/example-jukebox", libraryNode.getNamespace());
        assertEquals(0, libraryNode.getMatchNodes().size());
        assertEquals(1, libraryNode.getSelectNodes().size());
        assertEquals(0, libraryNode.getChildNodes().size());

        assertNotNull(libraryNode.getSelectNode("artist", "http://example.com/ns/example-jukebox"));

    }

    @Test
    public void testProcessFilter_FilterWithSelectAndContainmentNode1() throws NetconfMessageBuilderException {
        FilterNode root = new FilterNode();
        FilterUtil.processFilter(root, Arrays.asList(DocumentUtils.stringToDocument
                (FILTER_WITH_SELECT_AND_CONTAINMENT_NODES1).getDocumentElement()));

        assertEquals(0, root.getMatchNodes().size());
        assertEquals(0, root.getSelectNodes().size());
        assertEquals(1, root.getChildNodes().size());

        FilterNode jbNode = root.getChildNodes().get(0);
        assertEquals("jukebox", jbNode.getNodeName());
        assertEquals("http://example.com/ns/example-jukebox", jbNode.getNamespace());
        assertEquals(0, jbNode.getMatchNodes().size());
        assertEquals(0, jbNode.getSelectNodes().size());
        assertEquals(1, jbNode.getChildNodes().size());

        FilterNode libraryNode = jbNode.getChildNodes().get(0);
        assertEquals("library", libraryNode.getNodeName());
        assertEquals("http://example.com/ns/example-jukebox", libraryNode.getNamespace());
        assertEquals(0, libraryNode.getMatchNodes().size());
        assertEquals(1, libraryNode.getSelectNodes().size());
        assertEquals(0, libraryNode.getChildNodes().size());
        assertNotNull(libraryNode.getSelectNode("artist", "http://example.com/ns/example-jukebox"));
    }

    @Test
    public void testProcessFilter_FilterWithSelectAndContainmentNode2() throws NetconfMessageBuilderException {
        FilterNode root = new FilterNode();
        FilterUtil.processFilter(root, Arrays.asList(DocumentUtils.stringToDocument
                (FILTER_WITH_SELECT_AND_CONTAINMENT_NODES2)
                .getDocumentElement()));

        assertEquals(0, root.getMatchNodes().size());
        assertEquals(0, root.getSelectNodes().size());
        assertEquals(1, root.getChildNodes().size());

        FilterNode jbNode = root.getChildNodes().get(0);
        assertEquals("jukebox", jbNode.getNodeName());
        assertEquals("http://example.com/ns/example-jukebox", jbNode.getNamespace());
        assertEquals(0, jbNode.getMatchNodes().size());
        assertEquals(0, jbNode.getSelectNodes().size());
        assertEquals(1, jbNode.getChildNodes().size());

        FilterNode libraryNode = jbNode.getChildNodes().get(0);
        assertEquals("library", libraryNode.getNodeName());
        assertEquals("http://example.com/ns/example-jukebox", libraryNode.getNamespace());
        assertEquals(0, libraryNode.getMatchNodes().size());
        assertEquals(1, libraryNode.getSelectNodes().size());
        assertEquals(0, libraryNode.getChildNodes().size());
        assertNotNull(libraryNode.getSelectNode("artist", "http://example.com/ns/example-jukebox"));
    }

    @Test
    public void testProcessFilter_FilterWithSelectAndContainmentNode3() throws NetconfMessageBuilderException {
        FilterNode root = new FilterNode();
        FilterUtil.processFilter(root, Arrays.asList(DocumentUtils.stringToDocument
                (FILTER_WITH_SELECT_AND_CONTAINMENT_NODES3)
                .getDocumentElement()));

        assertEquals(0, root.getMatchNodes().size());
        assertEquals(0, root.getSelectNodes().size());
        assertEquals(1, root.getChildNodes().size());

        FilterNode jbNode = root.getChildNodes().get(0);
        assertEquals("jukebox", jbNode.getNodeName());
        assertEquals("http://example.com/ns/example-jukebox", jbNode.getNamespace());
        assertEquals(0, jbNode.getMatchNodes().size());
        assertEquals(0, jbNode.getSelectNodes().size());
        assertEquals(1, jbNode.getChildNodes().size());

        FilterNode libraryNode = jbNode.getChildNodes().get(0);
        assertEquals("library", libraryNode.getNodeName());
        assertEquals("http://example.com/ns/example-jukebox", libraryNode.getNamespace());
        assertEquals(0, libraryNode.getMatchNodes().size());
        assertEquals(1, libraryNode.getSelectNodes().size());
        assertEquals(0, libraryNode.getChildNodes().size());
        assertNotNull(libraryNode.getSelectNode("artist", "http://example.com/ns/example-jukebox"));
    }

    @Test
    public void testProcessFilter_FilterWithSelectAndMatchNode() throws NetconfMessageBuilderException {
        FilterNode root = new FilterNode();
        FilterUtil.processFilter(root, Arrays.asList(DocumentUtils.stringToDocument(FILTER_WITH_SELECT_AND_MATCH_NODES)
                .getDocumentElement()));

        assertEquals(0, root.getMatchNodes().size());
        assertEquals(0, root.getSelectNodes().size());
        assertEquals(1, root.getChildNodes().size());

        FilterNode jbNode = root.getChildNodes().get(0);
        assertEquals("jukebox", jbNode.getNodeName());
        assertEquals("http://example.com/ns/example-jukebox", jbNode.getNamespace());
        assertEquals(0, jbNode.getMatchNodes().size());
        assertEquals(0, jbNode.getSelectNodes().size());
        assertEquals(1, jbNode.getChildNodes().size());

        FilterNode libraryNode = jbNode.getChildNodes().get(0);
        assertEquals("library", libraryNode.getNodeName());
        assertEquals("http://example.com/ns/example-jukebox", libraryNode.getNamespace());
        assertEquals(0, libraryNode.getMatchNodes().size());
        assertEquals(0, libraryNode.getSelectNodes().size());
        assertEquals(1, libraryNode.getChildNodes().size());

        FilterNode artistNode = libraryNode.getChildNodes().get(0);
        assertEquals("artist", artistNode.getNodeName());
        assertEquals("http://example.com/ns/example-jukebox", artistNode.getNamespace());
        assertEquals(0, artistNode.getMatchNodes().size());
        assertEquals(0, artistNode.getSelectNodes().size());
        assertEquals(1, artistNode.getChildNodes().size());

        FilterNode albumNode = artistNode.getChildNodes().get(0);
        assertEquals("album", albumNode.getNodeName());
        assertEquals("http://example.com/ns/example-jukebox", albumNode.getNamespace());
        assertEquals(0, albumNode.getMatchNodes().size());
        assertEquals(0, albumNode.getSelectNodes().size());
        assertEquals(1, albumNode.getChildNodes().size());

        FilterNode songNode = albumNode.getChildNodes().get(0);
        assertEquals("song", songNode.getNodeName());
        assertEquals("http://example.com/ns/example-jukebox", songNode.getNamespace());
        assertEquals(0, songNode.getMatchNodes().size());
        assertEquals(1, songNode.getSelectNodes().size());
        assertEquals(0, songNode.getChildNodes().size());
        assertNotNull(songNode.getSelectNode("name", "http://example.com/ns/example-jukebox"));
    }

    @Test
    public void testProcessFilter_FilterWithMultipleMatchNodes() throws NetconfMessageBuilderException {
        FilterNode root = new FilterNode();
        FilterUtil.processFilter(root, Arrays.asList(DocumentUtils.stringToDocument(FILTER_WITH_MULTIPLE_MATCH_NODES)
                .getDocumentElement()));

        assertEquals(0, root.getMatchNodes().size());
        assertEquals(0, root.getSelectNodes().size());
        assertEquals(1, root.getChildNodes().size());

        FilterNode jbNode = root.getChildNodes().get(0);
        assertEquals("jukebox", jbNode.getNodeName());
        assertEquals("http://example.com/ns/example-jukebox", jbNode.getNamespace());
        assertEquals(0, jbNode.getMatchNodes().size());
        assertEquals(0, jbNode.getSelectNodes().size());
        assertEquals(1, jbNode.getChildNodes().size());

        FilterNode libraryNode = jbNode.getChildNodes().get(0);
        assertEquals("library", libraryNode.getNodeName());
        assertEquals("http://example.com/ns/example-jukebox", libraryNode.getNamespace());
        assertEquals(0, libraryNode.getMatchNodes().size());
        assertEquals(0, libraryNode.getSelectNodes().size());
        assertEquals(1, libraryNode.getChildNodes().size());

        FilterNode artistNode = libraryNode.getChildNodes().get(0);
        assertEquals("artist", artistNode.getNodeName());
        assertEquals("http://example.com/ns/example-jukebox", artistNode.getNamespace());
        assertEquals(0, artistNode.getMatchNodes().size());
        assertEquals(0, artistNode.getSelectNodes().size());
        assertEquals(1, artistNode.getChildNodes().size());

        FilterNode albumNode = artistNode.getChildNodes().get(0);
        assertEquals("album", albumNode.getNodeName());
        assertEquals("http://example.com/ns/example-jukebox", albumNode.getNamespace());
        assertEquals(0, albumNode.getMatchNodes().size());
        assertEquals(0, albumNode.getSelectNodes().size());
        assertEquals(2, albumNode.getChildNodes().size());

        FilterNode song1Node = albumNode.getChildNodes().get(0);
        assertEquals("song", song1Node.getNodeName());
        assertEquals("http://example.com/ns/example-jukebox", song1Node.getNamespace());
        assertEquals(1, song1Node.getMatchNodes().size());
        assertEquals(0, song1Node.getSelectNodes().size());
        assertEquals(0, song1Node.getChildNodes().size());
        FilterMatchNode nameMatchNode = song1Node.getMatchNodes().get(0);
        assertEquals("name", nameMatchNode.getNodeName());
        assertEquals("Song name1", nameMatchNode.getFilter());
        assertEquals("http://example.com/ns/example-jukebox", nameMatchNode.getNamespace());

        FilterNode song2Node = albumNode.getChildNodes().get(1);
        assertEquals("song", song2Node.getNodeName());
        assertEquals("http://example.com/ns/example-jukebox", song2Node.getNamespace());
        assertEquals(1, song2Node.getMatchNodes().size());
        assertEquals(0, song2Node.getSelectNodes().size());
        assertEquals(0, song2Node.getChildNodes().size());
        nameMatchNode = song2Node.getMatchNodes().get(0);
        assertEquals("name", nameMatchNode.getNodeName());
        assertEquals("Song name2", nameMatchNode.getFilter());
        assertEquals("http://example.com/ns/example-jukebox", nameMatchNode.getNamespace());
    }

    @Test
    public void testModelNodeIdIsConvertedIntoFilter() throws NetconfMessageBuilderException, IOException,
            SAXException {
        ModelNodeId nodeId = new ModelNodeId("/container=root/container=level1/key1=value1/container=level2/container" +
                "=level3", "ns1");
        nodeId.addRdn(new ModelNodeRdn(ModelNodeRdn.CONTAINER, "ns2", "level3"));
        FilterNode filterNode = FilterUtil.nodeIdToFilter(nodeId);
        TestUtil.assertXMLEquals(DocumentUtils.stringToDocument("<root xmlns=\"ns1\">\n" +
                "  <level1>\n" +
                "    <key1>value1</key1>\n" +
                "    <level2>\n" +
                "      <level3>\n" +
                "        <level3 xmlns=\"ns2\"/>\n" +
                "      </level3>\n" +
                "    </level2>\n" +
                "  </level1>\n" +
                "</root>").getDocumentElement(), FilterUtil.filterToXml(m_schemaRegistry, filterNode));

        assertNull(FilterUtil.nodeIdToFilter(new ModelNodeId()));
    }

}
