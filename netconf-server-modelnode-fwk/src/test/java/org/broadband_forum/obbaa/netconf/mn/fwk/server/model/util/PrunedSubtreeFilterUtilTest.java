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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util;

import static org.broadband_forum.obbaa.netconf.api.server.NetconfQueryParams.UNBOUNDED;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.PrunedSubtreeFilterUtil.filter;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.PrunedSubtreeFilterUtil.getPseudoDepth;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.assertXMLEquals;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

@RunWith(RequestScopeJunitRunner.class)
public class PrunedSubtreeFilterUtilTest {

    private SchemaRegistry m_schemaRegistry;

    private static final String FULL_DATA =
                    "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                    "    <jukebox xmlns=\"http://example.com/ns/example-jukebox\">\n" +
                    "        <library>\n" +
                    "            <artist>\n" +
                    "                <name>Lenny</name>\n" +
                    "                <album>\n" +
                    "                    <name>Greatest hits</name>\n" +
                    "                    <genre xmlns:jbox=\"http://example.com/ns/example-jukebox\">jbox:jazz</genre>\n" +
                    "                    <year>2000</year>\n" +
                    "                    <admin>\n" +
                    "                        <label>Sony</label>\n" +
                    "                    </admin>\n" +
                    "                    <song>\n" +
                    "                        <name>Are you gonne go my way</name>\n" +
                    "                        <location>desktop/mymusic</location>\n" +
                    "                        <format>mp3</format>\n" +
                    "                    </song>\n" +
                    "                    <song>\n" +
                    "                        <name>Fly Away</name>\n" +
                    "                        <location>desktop/mymusic</location>\n" +
                    "                        <format>flac</format>\n" +
                    "                    </song>\n" +
                    "                </album>\n" +
                    "                <album>\n" +
                    "                    <name>Circus</name>\n" +
                    "                    <genre xmlns:jbox=\"http://example.com/ns/example-jukebox\">jbox:rock</genre>\n" +
                    "                    <year>1995</year>\n" +
                    "                    <admin>\n" +
                    "                        <label>Sony</label>\n" +
                    "                    </admin>\n" +
                    "                    <song>\n" +
                    "                        <name>Rock and roll is dead</name>\n" +
                    "                        <location>desktop/mymusic</location>\n" +
                    "                        <format>amr</format>\n" +
                    "                    </song>\n" +
                    "                    <song>\n" +
                    "                        <name>Circus</name>\n" +
                    "                        <location>desktop/mymusic</location>\n" +
                    "                        <format>wma</format>\n" +
                    "                    </song>\n" +
                    "                    <song>\n" +
                    "                        <name>Beyond the 7th Sky</name>\n" +
                    "                        <location>desktop/mymusic</location>\n" +
                    "                        <format>mp3</format>\n" +
                    "                    </song>\n" +
                    "                </album>\n" +
                    "                <album>\n" +
                    "                    <name>AlbumWithoutGenre</name>\n" +
                    "                    <year>1995</year>\n" +
                    "                    <admin>\n" +
                    "                        <label>Sony</label>\n" +
                    "                    </admin>\n" +
                    "                    <song>\n" +
                    "                        <name>SomeSong</name>\n" +
                    "                        <location>desktop/mymusic</location>\n" +
                    "                        <format>amr</format>\n" +
                    "                    </song>\n" +
                    "                </album>\n" +
                    "                <album>\n" +
                    "                    <name>AlbumWithoutAdmin</name>\n" +
                    "                    <year>1995</year>\n" +
                    "                    <song>\n" +
                    "                        <name>New Song</name>\n" +
                    "                        <location>desktop/mymusic</location>\n" +
                    "                        <format>amr</format>\n" +
                    "                    </song>\n" +
                    "                </album>\n" +
                    "            </artist>\n" +
                    "        </library>\n" +
                    "    </jukebox>\n" +
                    "</data>";

    @Before
    public void setUp() throws Exception {
        m_schemaRegistry = new SchemaRegistryImpl(Collections.emptyList(), Collections.emptySet(), Collections.emptyMap(), new NoLockService());
        m_schemaRegistry.loadSchemaContext("jukebox", TestUtil.getJukeBoxYangsWithoutAlbumImage(), Collections.emptySet(), Collections.emptyMap());
    }

    private Element getElement(String xmlStr) {
        try {
            return DocumentUtils.stringToDocument(xmlStr).getDocumentElement();
        } catch (NetconfMessageBuilderException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testEmptyFilter() throws NetconfMessageBuilderException, IOException, SAXException {
        Element data = DocumentUtils.stringToDocumentElement(FULL_DATA);
        Element filteredXml = filter(m_schemaRegistry, data, getElement("<filter/>"), UNBOUNDED);
        assertXMLEquals(getElement("<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\"/>"), filteredXml);
    }

    @Test
    public void testFilterFullData() throws NetconfMessageBuilderException, IOException, SAXException {
        Element data = DocumentUtils.stringToDocumentElement(FULL_DATA);
        String filter = "<filter>\n" +
                        "   <jukebox xmlns=\"http://example.com/ns/example-jukebox\"/>\n" +
                        "</filter>";
        Element filteredXml = filter(m_schemaRegistry, data, getElement(filter), UNBOUNDED);
        assertXMLEquals(getElement(FULL_DATA), filteredXml);
    }

    @Test
    public void testFilterSelectNode() throws NetconfMessageBuilderException, IOException, SAXException {
        Element data = DocumentUtils.stringToDocumentElement(FULL_DATA);
        String filter = "<filter>\n" +
                        "   <jukebox xmlns=\"http://example.com/ns/example-jukebox\">\n" +
                        "      <library>\n" +
                        "         <artist>\n" +
                        "            <name/>\n" +
                        "         </artist>\n" +
                        "      </library>\n" +
                        "   </jukebox>\n" +
                        "</filter>";
        Element filteredXml = filter(m_schemaRegistry, data, getElement(filter), UNBOUNDED);
        String expectedXmlString =
                        "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                        "   <jbox:jukebox xmlns:jbox=\"http://example.com/ns/example-jukebox\">\n" +
                        "      <jbox:library>\n" +
                        "         <jbox:artist>\n" +
                        "            <jbox:name>Lenny</jbox:name>\n" +
                        "         </jbox:artist>\n" +
                        "      </jbox:library>\n" +
                        "   </jbox:jukebox>\n" +
                        "</data>";
        assertXMLEquals(getElement(expectedXmlString), filteredXml);
    }

    @Test
    public void testFilter2() throws NetconfMessageBuilderException, IOException, SAXException {
        Element data = DocumentUtils.stringToDocumentElement(FULL_DATA);
        String filter = "<filter>\n" +
                        "    <jukebox xmlns=\"http://example.com/ns/example-jukebox\">\n" +
                        "        <library>\n" +
                        "            <artist>\n" +
                        "                <name>Lenny</name>\n" +
                        "                <album>\n" +
                        "                    <name>Greatest hits</name>\n" +
                        "                    <song>\n" +
                        "                        <name>Fly Away</name>\n" +
                        "                    </song>\n" +
                        "                </album>\n" +
                        "            </artist>\n" +
                        "        </library>\n" +
                        "    </jukebox>\n" +
                        "</filter>";
        Element filteredXml = filter(m_schemaRegistry, data, getElement(filter), UNBOUNDED);
        String expectedXmlString =
                        "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                        "   <jukebox xmlns=\"http://example.com/ns/example-jukebox\">\n" +
                        "      <library>\n" +
                        "         <artist>\n" +
                        "            <name>Lenny</name>\n" +
                        "            <album>\n" +
                        "               <name>Greatest hits</name>\n" +
                        "               <song>\n" +
                        "                  <name>Fly Away</name>\n" +
                        "                  <format>flac</format>\n" +
                        "                  <location>desktop/mymusic</location>\n" +
                        "               </song>\n" +
                        "            </album>\n" +
                        "         </artist>\n" +
                        "      </library>\n" +
                        "   </jukebox>\n" +
                        "</data>";
        assertXMLEquals(getElement(expectedXmlString), filteredXml);
    }

    @Test
    public void testFilter3() throws NetconfMessageBuilderException, IOException, SAXException {
        Element data = DocumentUtils.stringToDocumentElement(FULL_DATA);
        String filter = "<filter>\n" +
                        "    <jukebox xmlns=\"http://example.com/ns/example-jukebox\">\n" +
                        "        <library>\n" +
                        "            <artist>\n" +
                        "                <name>Lenny</name>\n" +
                        "                <album>\n" +
                        "                    <name>Greatest hits</name>\n" +
                        "                    <song>\n" +
                        "                        <name>Fly Away2</name>\n" +
                        "                    </song>\n" +
                        "                </album>\n" +
                        "            </artist>\n" +
                        "        </library>\n" +
                        "    </jukebox>\n" +
                        "</filter>";
        Element filteredXml = filter(m_schemaRegistry, data, getElement(filter), UNBOUNDED);
        assertXMLEquals(getElement("<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\"/>"), filteredXml);
    }

    @Test
    public void testFilter4() throws NetconfMessageBuilderException, IOException, SAXException {
        Element data = DocumentUtils.stringToDocumentElement(FULL_DATA);
        String filter = "<filter>\n" +
                        "    <jukebox xmlns=\"http://example.com/ns/example-jukebox\">\n" +
                        "        <library>\n" +
                        "            <artist>\n" +
                        "                <name>Lenny</name>\n" +
                        "                <album>\n" +
                        "                    <name>Greatest hits</name>\n" +
                        "                    <song>\n" +
                        "                        <name>Fly Away2</name>\n" +
                        "                    </song>\n" +
                        "                    <song>\n" +
                        "                        <name>Fly Away</name>\n" +
                        "                    </song>\n" +
                        "                </album>\n" +
                        "            </artist>\n" +
                        "        </library>\n" +
                        "    </jukebox>\n" +
                        "</filter>";
        String expectedXmlString =
                        "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                        "   <jukebox xmlns=\"http://example.com/ns/example-jukebox\">\n" +
                        "      <library>\n" +
                        "         <artist>\n" +
                        "            <name>Lenny</name>\n" +
                        "            <album>\n" +
                        "               <name>Greatest hits</name>\n" +
                        "               <song>\n" +
                        "                  <name>Fly Away</name>\n" +
                        "                  <format>flac</format>\n" +
                        "                  <location>desktop/mymusic</location>\n" +
                        "               </song>\n" +
                        "            </album>\n" +
                        "         </artist>\n" +
                        "      </library>\n" +
                        "   </jukebox>\n" +
                        "</data>";
        Element filteredXml = filter(m_schemaRegistry, data, getElement(filter), UNBOUNDED);
        assertXMLEquals(getElement(expectedXmlString), filteredXml);
    }

    @Test
    public void testFilter5() throws NetconfMessageBuilderException, IOException, SAXException {
        Element data = DocumentUtils.stringToDocumentElement(FULL_DATA);
        String filter = "<filter>\n" +
                        "    <jukebox xmlns=\"http://example.com/ns/example-jukebox\">\n" +
                        "        <library>\n" +
                        "            <artist>\n" +
                        "                <name>Lenny</name>\n" +
                        "                <album>\n" +
                        "                    <name>Greatest hits</name>\n" +
                        "                    <song>\n" +
                        "                        <name>Fly Away</name>\n" +
                        "                    </song>\n" +
                        "                </album>\n" +
                        "                <album>\n" +
                        "                    <name>Circus</name>\n" +
                        "                    <song>\n" +
                        "                        <name>Circus</name>\n" +
                        "                    </song>\n" +
                        "                </album>\n" +
                        "            </artist>\n" +
                        "        </library>\n" +
                        "    </jukebox>\n" +
                        "</filter>";
        String expectedXmlString =
                        "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                        "   <jukebox xmlns=\"http://example.com/ns/example-jukebox\">\n" +
                        "      <library>\n" +
                        "         <artist>\n" +
                        "            <album>\n" +
                        "               <name>Greatest hits</name>\n" +
                        "               <song>\n" +
                        "                  <format>flac</format>\n" +
                        "                  <location>desktop/mymusic</location>\n" +
                        "                  <name>Fly Away</name>\n" +
                        "               </song>\n" +
                        "            </album>\n" +
                        "            <album>\n" +
                        "               <name>Circus</name>\n" +
                        "               <song>\n" +
                        "                  <format>wma</format>\n" +
                        "                  <location>desktop/mymusic</location>\n" +
                        "                  <name>Circus</name>\n" +
                        "               </song>\n" +
                        "            </album>\n" +
                        "            <name>Lenny</name>\n" +
                        "         </artist>\n" +
                        "      </library>\n" +
                        "   </jukebox>\n" +
                        "</data>";
        Element filteredXml = filter(m_schemaRegistry, data, getElement(filter), UNBOUNDED);
        assertXMLEquals(getElement(expectedXmlString), filteredXml);
    }

    @Test
    public void testFilter6() throws NetconfMessageBuilderException, IOException, SAXException {
        Element data = DocumentUtils.stringToDocumentElement(FULL_DATA);
        String filter = "<filter>\n" +
                        "    <jukebox xmlns=\"http://example.com/ns/example-jukebox\">\n" +
                        "        <library>\n" +
                        "            <artist>\n" +
                        "                <name>Lenny</name>\n" +
                        "                <album>\n" +
                        "                    <name>Greatest hits</name>\n" +
                        "                    <song>\n" +
                        "                        <name>Dont Fly Away</name>\n" +
                        "                    </song>\n" +
                        "                </album>\n" +
                        "                <album>\n" +
                        "                    <name>Circus</name>\n" +
                        "                    <song>\n" +
                        "                        <name>Circus</name>\n" +
                        "                    </song>\n" +
                        "                </album>\n" +
                        "            </artist>\n" +
                        "        </library>\n" +
                        "    </jukebox>\n" +
                        "</filter>";
        String expectedXmlString =
                        "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                        "   <jukebox xmlns=\"http://example.com/ns/example-jukebox\">\n" +
                        "      <library>\n" +
                        "         <artist>\n" +
                        "            <album>\n" +
                        "               <name>Circus</name>\n" +
                        "               <song>\n" +
                        "                  <format>wma</format>\n" +
                        "                  <location>desktop/mymusic</location>\n" +
                        "                  <name>Circus</name>\n" +
                        "               </song>\n" +
                        "            </album>\n" +
                        "            <name>Lenny</name>\n" +
                        "         </artist>\n" +
                        "      </library>\n" +
                        "   </jukebox>\n" +
                        "</data>";
        Element filteredXml = filter(m_schemaRegistry, data, getElement(filter), UNBOUNDED);
        assertXMLEquals(getElement(expectedXmlString), filteredXml);
    }

    @Test
    public void testFilter7() throws NetconfMessageBuilderException, IOException, SAXException {
        Element data = DocumentUtils.stringToDocumentElement(FULL_DATA);
        String filter = "<filter>\n" +
                        "    <jukebox xmlns=\"http://example.com/ns/example-jukebox\">\n" +
                        "        <library>\n" +
                        "            <artist>\n" +
                        "                <name>Lenny</name>\n" +
                        "                <album>\n" +
                        "                    <name>Greatest hits</name>\n" +
                        "                    <admin>\n" +
                        "                        <label>Dummy</label>\n" +
                        "                    </admin>\n" +
                        "                    <song>\n" +
                        "                        <name>Fly Away</name>\n" +
                        "                    </song>\n" +
                        "                </album>\n" +
                        "            </artist>\n" +
                        "        </library>\n" +
                        "    </jukebox>\n" +
                        "</filter>";
        Element filteredXml = filter(m_schemaRegistry, data, getElement(filter), UNBOUNDED);
        assertXMLEquals(getElement("<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\"/>"), filteredXml);
    }

    @Test
    public void testFilter8() throws NetconfMessageBuilderException, IOException, SAXException {
        Element data = DocumentUtils.stringToDocumentElement(FULL_DATA);
        String filter = "<filter>\n" +
                        "    <jukebox xmlns=\"http://example.com/ns/example-jukebox\">\n" +
                        "        <library>\n" +
                        "            <artist>\n" +
                        "                <name>Lenny</name>\n" +
                        "                <album>\n" +
                        "                    <name>Greatest hits</name>\n" +
                        "                    <admin>\n" +
                        "                        <label>Dummy</label>\n" +
                        "                    </admin>\n" +
                        "                    <song/>\n" +
                        "                </album>\n" +
                        "            </artist>\n" +
                        "        </library>\n" +
                        "    </jukebox>\n" +
                        "</filter>";
        Element filteredXml = filter(m_schemaRegistry, data, getElement(filter), UNBOUNDED);
        assertXMLEquals(getElement("<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\"/>"), filteredXml);
    }

    @Test
    public void testFilter9() throws NetconfMessageBuilderException, IOException, SAXException {
        Element data = DocumentUtils.stringToDocumentElement(FULL_DATA);
        String filter = "<filter>\n" +
                        "    <jukebox xmlns=\"http://example.com/ns/example-jukebox\">\n" +
                        "        <library>\n" +
                        "            <artist>\n" +
                        "                <name>Lenny</name>\n" +
                        "                <album>\n" +
                        "                    <name>AlbumWithoutAdmin</name>\n" +
                        "                    <admin>\n" +
                        "                        <label/>\n" +
                        "                    </admin>\n" +
                        "                    <song>\n" +
                        "                        <name>New Song</name>\n" +
                        "                    </song>\n" +
                        "                </album>\n" +
                        "            </artist>\n" +
                        "        </library>\n" +
                        "    </jukebox>\n" +
                        "</filter>";
        Element filteredXml = filter(m_schemaRegistry, data, getElement(filter), UNBOUNDED);
        String expectedXmlString =
                        "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                        "   <jukebox xmlns=\"http://example.com/ns/example-jukebox\">\n" +
                        "      <library>\n" +
                        "         <artist>\n" +
                        "            <album>\n" +
                        "               <name>AlbumWithoutAdmin</name>\n" +
                        "               <song>\n" +
                        "                  <format>amr</format>\n" +
                        "                  <location>desktop/mymusic</location>\n" +
                        "                  <name>New Song</name>\n" +
                        "               </song>\n" +
                        "            </album>\n" +
                        "            <name>Lenny</name>\n" +
                        "         </artist>\n" +
                        "      </library>\n" +
                        "   </jukebox>\n" +
                        "</data>";
        assertXMLEquals(getElement(expectedXmlString), filteredXml);
    }

    @Test
    public void testRemoveDataBelowDepth1() throws NetconfMessageBuilderException, IOException, SAXException {
        Element data = DocumentUtils.stringToDocumentElement(FULL_DATA);
        String filter = "<filter>\n" +
                        "   <jukebox xmlns=\"http://example.com/ns/example-jukebox\"/>\n" +
                        "</filter>";
        Element filteredXml = filter(m_schemaRegistry, data, getElement(filter), 1);
        assertXMLEquals(getElement("<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\"/>"), filteredXml);
    }


    @Test
    public void testRemoveDataBelowDepth2() throws NetconfMessageBuilderException, IOException, SAXException {
        Element data = DocumentUtils.stringToDocumentElement(FULL_DATA);
        String filter = "<filter>\n" +
                        "   <jukebox xmlns=\"http://example.com/ns/example-jukebox\"/>\n" +
                        "</filter>";
        Element filteredXml = filter(m_schemaRegistry, data, getElement(filter), 2);
        String expectedXmlString =
                        "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                        "   <jukebox xmlns=\"http://example.com/ns/example-jukebox\"/>\n" +
                        "</data>";
        assertXMLEquals(getElement(expectedXmlString), filteredXml);
    }

    @Test
    public void testRemoveDataBelowDepth3() throws NetconfMessageBuilderException, IOException, SAXException {
        Element data = DocumentUtils.stringToDocumentElement(FULL_DATA);
        String filter = "<filter>\n" +
                        "   <jukebox xmlns=\"http://example.com/ns/example-jukebox\"/>\n" +
                        "</filter>";
        Element filteredXml = filter(m_schemaRegistry, data, getElement(filter), 3);
        String expectedXmlString =
                        "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                        "   <jukebox xmlns=\"http://example.com/ns/example-jukebox\">\n" +
                        "      <library/>\n" +
                        "   </jukebox>\n" +
                        "</data>";
        assertXMLEquals(getElement(expectedXmlString), filteredXml);
    }

    @Test
    public void testRemoveDataBelowDepth5() throws NetconfMessageBuilderException, IOException, SAXException {
        Element data = DocumentUtils.stringToDocumentElement(FULL_DATA);
        String filter = "<filter>\n" +
                        "   <jukebox xmlns=\"http://example.com/ns/example-jukebox\"/>\n" +
                        "</filter>";
        Element filteredXml = filter(m_schemaRegistry, data, getElement(filter), 5);
        String expectedXmlString =
                        "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                        "   <jukebox xmlns=\"http://example.com/ns/example-jukebox\">\n" +
                        "      <library>\n" +
                        "         <artist>\n" +
                        "            <album/>\n" +
                        "            <album/>\n" +
                        "            <album/>\n" +
                        "            <album/>\n" +
                        "            <name>Lenny</name>\n" +
                        "         </artist>\n" +
                        "      </library>\n" +
                        "   </jukebox>\n" +
                        "</data>";
        assertXMLEquals(getElement(expectedXmlString), filteredXml);
    }

    @Test
    public void testPseudoDepthWithOneMatchNode() {
        String filter = "<jukebox xmlns=\"http://example.com/ns/example-jukebox\">\n" +
                        "    <library>\n" +
                        "        <artist>\n" +
                        "            <name>Lenny</name>\n" +
                        "        </artist>\n" +
                        "    </library>\n" +
                        "</jukebox>";
        FilterNode root = new FilterNode();
        FilterUtil.processFilter(root, Arrays.asList(getElement(filter)));
        assertEquals(5, getPseudoDepth(root));
    }

    @Test
    public void testPseudoDepthWithMultipleMatchNodesOnDifferentSubtrees() {
        String filter = "<jukebox xmlns=\"http://example.com/ns/example-jukebox\">\n" +
                        "    <library>\n" +
                        "        <artist>\n" +
                        "            <name>Lenny</name>\n" +
                        "            <album>\n" +
                        "                <name>Greatest hits</name>\n" +
                        "                <song>\n" +
                        "                    <name>Fly Away2</name>\n" +
                        "                </song>\n" +
                        "                <admin>" +
                        "                    <dummy>\n" +
                        "                        <name>Fly Away</name>\n" +
                        "                    </dummy>\n" +
                        "                </admin>\n" +
                        "            </album>\n" +
                        "        </artist>\n" +
                        "    </library>\n" +
                        "</jukebox>";
        FilterNode root = new FilterNode();
        FilterUtil.processFilter(root, Arrays.asList(getElement(filter)));
        assertEquals(8, getPseudoDepth(root));
    }

    @Test
    public void testPseudoDepthWithoutMatchNodes() {
        String filter = "<jukebox xmlns=\"http://example.com/ns/example-jukebox\">\n" +
                        "    <library>\n" +
                        "        <artist>\n" +
                        "            <name/>\n" +
                        "            <album>\n" +
                        "                <name/>\n" +
                        "            </album>\n" +
                        "        </artist>\n" +
                        "    </library>\n" +
                        "</jukebox>";
        FilterNode root = new FilterNode();
        FilterUtil.processFilter(root, Arrays.asList(getElement(filter)));
        assertEquals(-1, getPseudoDepth(root));
    }

    @Test
    public void testPseudoDepthWithMatchAndSelectNodes() {
        String filter = "<jukebox xmlns=\"http://example.com/ns/example-jukebox\">\n" +
                        "    <library>\n" +
                        "        <artist>\n" +
                        "            <name>Lenny</name>\n" +
                        "            <album>\n" +
                        "                <name/>\n" +
                        "                <song>\n" +
                        "                    <name/>\n" +
                        "                </song>\n" +
                        "            </album>\n" +
                        "        </artist>\n" +
                        "    </library>\n" +
                        "</jukebox>";
        FilterNode root = new FilterNode();
        FilterUtil.processFilter(root, Arrays.asList(getElement(filter)));
        assertEquals(5, getPseudoDepth(root));
    }
}