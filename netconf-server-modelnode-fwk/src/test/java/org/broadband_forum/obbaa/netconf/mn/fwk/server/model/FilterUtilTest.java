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

import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn.CONTAINER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.xml.sax.SAXException;

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

    private static final String FILTER_WITH_MULTIPLE_MATCH_NODES_IMPROVED =
            "<jukebox xmlns=\"http://example.com/ns/example-jukebox\">\n" +
                    "  <library>\n" +
                    "    <artist>\n" +
                    "      <album>\n" +
                    "          <name>Album name1</name>\n" +
                    "        <song>\n" +
                    "          <name>Song name1</name>\n" +
                    "        </song>\n" +
                    "      </album>\n" +
                    "      <album>\n" +
                    "          <name>Album name1</name>\n" +
                    "        <song>\n" +
                    "          <name>Song name2</name>\n" +
                    "        </song>\n" +
                    "      </album>\n" +
                    "      <album>\n" +
                    "          <name>Album name1</name>\n" +
                    "        <song>\n" +
                    "          <name>Song name3</name>\n" +
                    "        </song>\n" +
                    "      </album>\n" +
                    "    </artist>\n" +
                    "  </library>\n" +
                    "</jukebox>\n";

    private static final String FILTER_WITH_MULTIPLE_MATCH_NODES_IMPROVED_V2 =
            "<jukebox xmlns=\"http://example.com/ns/example-jukebox\">\n" +
                    "  <library>\n" +
                    "    <artist>\n" +
                    "      <album>\n" +
                    "          <name>Album name1</name>\n" +
                    "        <song>\n" +
                    "          <name>Song name1</name>\n" +
                    "        </song>\n" +
                    "      </album>\n" +
                    "      <album>\n" +
                    "          <name>Album name2</name>\n" +
                    "        <song>\n" +
                    "          <name>Song name2</name>\n" +
                    "        </song>\n" +
                    "      </album>\n" +
                    "      <album>\n" +
                    "          <name>Album name1</name>\n" +
                    "        <song>\n" +
                    "          <name>Song name3</name>\n" +
                    "        </song>\n" +
                    "      </album>\n" +
                    "    </artist>\n" +
                    "  </library>\n" +
                    "</jukebox>\n";

    private static final String FILTER_WITH_MULTPLE_MATCH_NODES_REDUCED =
            "<jukebox xmlns=http://example.com/ns/example-jukebox>\n" +
                    "  <library>\n" +
                    "    <artist>\n" +
                    "      <album>\n" +
                    "          <name>Album name1</name>\n" +
                    "        <song>\n" +
                    "          <name>Song name1</name>\n" +
                    "        </song>\n" +
                    "        <song>\n" +
                    "          <name>Song name2</name>\n" +
                    "        </song>\n" +
                    "        <song>\n" +
                    "          <name>Song name3</name>\n" +
                    "        </song>\n" +
                    "      </album>\n" +
                    "    </artist>\n" +
                    "  </library>\n" +
                    "</jukebox>";


    private static final String FILTER_WITH_ATTRIBUTES_IN_MATCH_NODES =
            "<jukebox xmlns=\"http://example.com/ns/example-jukebox\">\n" +
                    "  <library>\n" +
                    "    <artist>\n" +
                    "      <album>\n" +
                    "        <song>\n" +
                    "          <name xmlns:pf=\"ns\">pf:Song name1</name>\n" +
                    "        </song>\n" +
                    "      </album>\n" +
                    "    </artist>\n" +
                    "  </library>\n" +
                    "</jukebox>\n";

    private static final String FILTER_WITH_MULTIPLE_KEYS_1=
            "<root-container xmlns=\"http://example.com/ns/example-filter\">\n" +
                    "    <container-one xmlns=\"http://example.com/ns/example-filter\">\n" +
                    "        <list-one xmlns=\"http://example.com/ns/example-filter\">\n" +
                    "            <list-one-key xmlns:pf=\"ns\">pf:list-one</list-one-key>\n" +
                    "        </list-one>\n" +
                    "    </container-one>\n" +
                    "     <container-one xmlns=\"http://example.com/ns/example-filter\">\n" +
                    "        <list-two xmlns=\"http://example.com/ns/example-filter\">\n" +
                    "            <list-two-key xmlns:pf=\"ns\">pf:list-two</list-two-key>\n" +
                    "        </list-two>\n" +
                    "    </container-one>\n" +
                    "</root-container>\n";
    private static final String EXPECTED_FILTER_WITH_MULTIPLE_KEYS_1=
            "<root-container xmlns=\"http://example.com/ns/example-filter\">\n" +
                    "    <container-one xmlns=\"http://example.com/ns/example-filter\">\n" +
                    "        <list-one xmlns=\"http://example.com/ns/example-filter\">\n" +
                    "            <list-one-key xmlns:pf=\"ns\">pf:list-one</list-one-key>\n" +
                    "        </list-one>\n" +
                    "        <list-two xmlns=\"http://example.com/ns/example-filter\">\n" +
                    "            <list-two-key xmlns:pf=\"ns\">pf:list-two</list-two-key>\n" +
                    "        </list-two>\n" +
                    "    </container-one>\n" +
                    "</root-container>";



    private static final String FILTER_WITH_MULTIPLE_KEYS_2=
            "<root-container xmlns=\"http://example.com/ns/example-filter\">\n" +
                    "    <container-one xmlns=\"http://example.com/ns/example-filter\">\n" +
                    "        <list-one xmlns=\"http://example.com/ns/example-filter\">\n" +
                    "            <list-one-key xmlns:pf=\"ns\">pf:list-one</list-one-key>\n" +
                    "        </list-one>\n" +
                    "    </container-one>\n" +
                    "     <container-one xmlns=\"http://example.com/ns/example-filter\">\n" +
                    "        <list-two xmlns=\"http://example.com/ns/example-filter\">\n" +
                    "            <list-two-key xmlns:pf=\"ns\">pf:list-two</list-two-key>\n" +
                    "        </list-two>\n" +
                    "    </container-one>\n" +
                    "     <container-two xmlns=\"http://example.com/ns/example-filter\">\n" +
                    "        <list-three xmlns=\"http://example.com/ns/example-filter\">\n" +
                    "            <list-three-key xmlns:pf=\"ns\">pf:list-two</list-three-key>\n" +
                    "        </list-three>\n" +
                    "    </container-two>\n" +
                    "</root-container>\n";

    private static final String EXPECTED_FILTER_WITH_MULTIPLE_KEYS_2= "<root-container xmlns=\"http://example.com/ns/example-filter\">\n"+
            "    <container-one xmlns=\"http://example.com/ns/example-filter\">\n"+
            "        <list-one xmlns=\"http://example.com/ns/example-filter\">\n"+
            "            <list-one-key xmlns:pf=\"ns\">pf:list-one</list-one-key>\n"+
            "        </list-one>\n"+
            "        <list-two xmlns=\"http://example.com/ns/example-filter\">\n"+
            "            <list-two-key xmlns:pf=\"ns\">pf:list-two</list-two-key>\n"+
            "        </list-two>\n"+
            "    </container-one>\n"+
            "     <container-two xmlns=\"http://example.com/ns/example-filter\">\n"+
            "        <list-three xmlns=\"http://example.com/ns/example-filter\">\n"+
            "            <list-three-key xmlns:pf=\"ns\">pf:list-two</list-three-key>\n"+
            "        </list-three>\n"+
            "    </container-two>\n"+
            "</root-container>";

    private static final String FILTER_WITH_MULTIPLE_KEYS_3="<root-container xmlns=\"http://example.com/ns/example-filter\">\n" +
            "    <container-one xmlns=\"http://example.com/ns/example-filter\">\n" +
            "        <list-one xmlns=\"http://example.com/ns/example-filter\">\n" +
            "            <list-one-key xmlns:pf=\"ns\">pf:list-one</list-one-key>\n" +
            "        </list-one>\n" +
            "    </container-one>\n" +
            "     <container-one xmlns=\"http://example.com/ns/example-filter\">\n" +
            "        <list-one xmlns=\"http://example.com/ns/example-filter\">\n" +
            "            <list-one-key xmlns:pf=\"ns\">pf:list-one</list-one-key>\n" +
            "            <inner-list-one xmlns=\"http://example.com/ns/example-filter\">\n" +
            "                <inner-list-one-key xmlns:pf=\"ns\">pf:inner-list-one</inner-list-one-key>\n" +
            "                <inner-list-one-leaf>inner-list-one</inner-list-one-leaf>\n" +
            "            </inner-list-one>\n" +
            "        </list-one>\n" +
            "    </container-one>\n" +
            "</root-container>\n";

    private static final String FILTER_WITH_MULTIPLE_KEYS_4="<root-container xmlns=\"http://example.com/ns/example-filter\">\n" +
            "    <container-one xmlns=\"http://example.com/ns/example-filter\">\n" +
            "        <list-one xmlns=\"http://example.com/ns/example-filter\">\n" +
            "            <list-one-key xmlns:pf=\"ns\">pf:list-one</list-one-key>\n" +
            "        </list-one>\n" +
            "        <list-one xmlns=\"http://example.com/ns/example-filter\">\n" +
            "            <list-one-key xmlns:pf=\"ns\">pf:list-one</list-one-key>\n" +
            "        </list-one>\n" +
            "        <list-one xmlns=\"http://example.com/ns/example-filter\">\n" +
            "            <list-one-key xmlns:pf=\"ns\">pf:list-two</list-one-key>\n" +
            "        </list-one>\n" +
            "    </container-one>\n" +
            "</root-container>\n";

    private static final String FILTER_WITH_MULTIPLE_KEYS_5="<root-container xmlns=\"http://example.com/ns/example-filter\">\n" +
            "    <container-one xmlns=\"http://example.com/ns/example-filter\">\n" +
            "        <list-one xmlns=\"http://example.com/ns/example-filter\">\n" +
            "            <list-one-key xmlns:pf=\"ns\">pf:list-one</list-one-key>\n" +
            "        </list-one>\n" +
            "    </container-one>\n" +
            "     <container-one xmlns=\"http://example.com/ns/example-filter\">\n" +
            "        <list-one xmlns=\"http://example.com/ns/example-filter\">\n" +
            "            <list-one-key xmlns:pf=\"ns\">pf:list-one</list-one-key>\n" +
            "        </list-one>\n" +
            "         <list-one xmlns=\"http://example.com/ns/example-filter\">\n" +
            "            <list-one-key xmlns:pf=\"ns\">pf:list-two</list-one-key>\n" +
            "        </list-one>\n" +
            "    </container-one>\n" +
            "</root-container>\n";

    private static final String FILTER_WITH_MULTIPLE_KEYS_6="<root-container xmlns=\"http://example.com/ns/example-filter\">\n" +
            "    <top-list xmlns=\"http://example.com/ns/example-filter\">\n" +
            "        <top-list-key xmlns:pf=\"ns\">pf:top-list</top-list-key>\n" +
            "        <sub-list-one xmlns=\"http://example.com/ns/example-filter\">\n" +
            "            <sub-list-one-key xmlns:pf=\"ns\">pf:sub-list-one</sub-list-one-key>\n" +
            "        </sub-list-one>\n" +
            "    </top-list>\n" +
            "    <top-list xmlns=\"http://example.com/ns/example-filter\">\n" +
            "        <top-list-key xmlns:pf=\"ns\">pf:top-list</top-list-key>\n" +
            "        <sub-list-two xmlns=\"http://example.com/ns/example-filter\">\n" +
            "            <sub-list-two-key xmlns:pf=\"ns\">pf:sub-list-two</sub-list-two-key>\n" +
            "        </sub-list-two>\n" +
            "    </top-list>\n" +
            "</root-container>\n";

    private static final String FILTER_WITH_MULTIPLE_KEYS_7="<root-container xmlns=\"http://example.com/ns/example-filter\">\n" +
            "    <top-list xmlns=\"http://example.com/ns/example-filter\">\n" +
            "        <top-list-key xmlns:pf=\"ns\">pf:top-list-one</top-list-key>\n" +
            "        <sub-list-one xmlns=\"http://example.com/ns/example-filter\">\n" +
            "            <sub-list-one-key xmlns:pf=\"ns\">pf:sub-list-one</sub-list-one-key>\n" +
            "        </sub-list-one>\n" +
            "    </top-list>\n" +
            "    <top-list xmlns=\"http://example.com/ns/example-filter\">\n" +
            "        <top-list-key xmlns:pf=\"ns\">pf:top-list-one</top-list-key>\n" +
            "        <sub-list-two xmlns=\"http://example.com/ns/example-filter\">\n" +
            "            <sub-list-two-key xmlns:pf=\"ns\">pf:sub-list-two</sub-list-two-key>\n" +
            "        </sub-list-two>\n" +
            "    </top-list>\n" +
            "    <top-list xmlns=\"http://example.com/ns/example-filter\">\n" +
            "        <top-list-key xmlns:pf=\"ns\">pf:top-list-two</top-list-key>\n" +
            "        <sub-list-two xmlns=\"http://example.com/ns/example-filter\">\n" +
            "            <sub-list-two-key xmlns:pf=\"ns\">pf:sub-list-two</sub-list-two-key>\n" +
            "        </sub-list-two>\n" +
            "    </top-list>\n" +
            "</root-container>\n";

    private static final String FILTER_WITH_MULTIPLE_KEYS_8="<root-container xmlns=\"http://example.com/ns/example-filter\">\n" +
            "        <top-list xmlns=\"http://example.com/ns/example-filter\">\n" +
            "                <sub-list-one  xmlns=\"http://example.com/ns/example-filter\">pf:list-one-key</sub-list-one>\n" +
            "        </top-list>\n" +
            "        <top-list xmlns=\"http://example.com/ns/example-filter\">\n" +
            "                <sub-list-one   xmlns=\"http://example.com/ns/example-filter\"/>"+
            "        </top-list>\n" +
            "</root-container>";

    private static final String FILTER_WITH_MULTIPLE_KEYS_9="<root-container xmlns=\"http://example.com/ns/example-filter\">\n" +
            "    <top-list xmlns=\"http://example.com/ns/example-filter\">\n" +
            "        <top-list-key xmlns:pf=\"ns\">pf:top-list-one</top-list-key>\n" +
            "        <sub-list-one/>\n" +
            "    </top-list>\n" +
            "    <top-list xmlns=\"http://example.com/ns/example-filter\">" +
            "        <top-list-key xmlns:pf=\"ns\">pf:top-list-one</top-list-key>\n" +
            "        <sub-list-one/>\n" +
            "    </top-list>\n" +
            "</root-container>\n";

    private static final String EXPECTED_FILTER_WITH_MULTIPLE_KEYS_9="<root-container xmlns=\"http://example.com/ns/example-filter\">\n" +
            "    <top-list xmlns=\"http://example.com/ns/example-filter\">\n" +
            "        <top-list-key xmlns:pf=\"ns\">pf:top-list-one</top-list-key>\n" +
            "        <sub-list-one/>\n" +
            "    </top-list>\n" +
            "</root-container>\n";

    private static final String EXPECTED_FILTER_WITH_MULTIPLE_KEYS_8="<root-container xmlns=\"http://example.com/ns/example-filter\">\n" +
            "<top-list xmlns=\"http://example.com/ns/example-filter\">\n" +
            "  <sub-list-one/>\n" +
            "</top-list>\n" +
            "</root-container>";

    private static final String EXPECTED_FILTER_WITH_MULTIPLE_KEYS_3="<root-container xmlns=\"http://example.com/ns/example-filter\">\n" +
            "    <container-one xmlns=\"http://example.com/ns/example-filter\">\n" +
            "        <list-one xmlns=\"http://example.com/ns/example-filter\">\n" +
            "            <list-one-key xmlns:pf=\"ns\">pf:list-one</list-one-key>\n" +
            "            <inner-list-one xmlns=\"http://example.com/ns/example-filter\">\n" +
            "                <inner-list-one-key xmlns:pf=\"ns\">pf:inner-list-one</inner-list-one-key>\n" +
            "                <inner-list-one-leaf>inner-list-one</inner-list-one-leaf>\n" +
            "            </inner-list-one>\n" +
            "        </list-one>\n" +
            "    </container-one>\n" +
            "</root-container>";

    private static final String EXPECTED_FILTER_WITH_MULTIPLE_KEYS_4="<root-container xmlns=\"http://example.com/ns/example-filter\">\n" +
            "    <container-one xmlns=\"http://example.com/ns/example-filter\">\n" +
            "        <list-one xmlns=\"http://example.com/ns/example-filter\">\n" +
            "            <list-one-key xmlns:pf=\"ns\">pf:list-one</list-one-key>\n" +
            "        </list-one>\n" +
            "        <list-one xmlns=\"http://example.com/ns/example-filter\">\n" +
            "            <list-one-key xmlns:pf=\"ns\">pf:list-two</list-one-key>\n" +
            "        </list-one>\n" +
            "    </container-one>\n" +
            "</root-container>";

    private static final String EXPECTED_FILTER_WITH_MULTIPLE_KEYS_5="<root-container xmlns=\"http://example.com/ns/example-filter\">\n" +
            "    <container-one xmlns=\"http://example.com/ns/example-filter\">\n" +
            "        <list-one xmlns=\"http://example.com/ns/example-filter\">\n" +
            "            <list-one-key xmlns:pf=\"ns\">pf:list-one</list-one-key>\n" +
            "        </list-one>\n" +
            "         <list-one xmlns=\"http://example.com/ns/example-filter\">\n" +
            "            <list-one-key xmlns:pf=\"ns\">pf:list-two</list-one-key>\n" +
            "        </list-one>\n" +
            "    </container-one>\n" +
            "</root-container>";

    private static final String EXPECTED_FILTER_WITH_MULTIPLE_KEYS_6="<root-container xmlns=\"http://example.com/ns/example-filter\">\n" +
            "    <top-list xmlns=\"http://example.com/ns/example-filter\">\n" +
            "        <top-list-key xmlns:pf=\"ns\">pf:top-list</top-list-key>\n" +
            "        <sub-list-one xmlns=\"http://example.com/ns/example-filter\">\n" +
            "            <sub-list-one-key xmlns:pf=\"ns\">pf:sub-list-one</sub-list-one-key>\n" +
            "        </sub-list-one>\n" +
            "         <sub-list-two xmlns=\"http://example.com/ns/example-filter\">\n" +
            "            <sub-list-two-key xmlns:pf=\"ns\">pf:sub-list-two</sub-list-two-key>\n" +
            "        </sub-list-two>\n" +
            "    </top-list>\n" +
            "</root-container>";

    private static final String EXPECTED_FILTER_WITH_MULTIPLE_KEYS_7="<root-container xmlns=\"http://example.com/ns/example-filter\">\n" +
            "    <top-list xmlns=\"http://example.com/ns/example-filter\">\n" +
            "        <top-list-key xmlns:pf=\"ns\">pf:top-list-one</top-list-key>\n" +
            "        <sub-list-one xmlns=\"http://example.com/ns/example-filter\">\n" +
            "            <sub-list-one-key xmlns:pf=\"ns\">pf:sub-list-one</sub-list-one-key>\n" +
            "        </sub-list-one>\n" +
            "         <sub-list-two xmlns=\"http://example.com/ns/example-filter\">\n" +
            "            <sub-list-two-key xmlns:pf=\"ns\">pf:sub-list-two</sub-list-two-key>\n" +
            "        </sub-list-two>\n" +
            "    </top-list>\n" +
            "    <top-list xmlns=\"http://example.com/ns/example-filter\">\n" +
            "        <top-list-key xmlns:pf=\"ns\">pf:top-list-two</top-list-key>\n" +
            "        <sub-list-two xmlns=\"http://example.com/ns/example-filter\">\n" +
            "            <sub-list-two-key xmlns:pf=\"ns\">pf:sub-list-two</sub-list-two-key>\n" +
            "        </sub-list-two>\n" +
            "    </top-list>\n" +
            "</root-container>";

    private static final String FILTER_WITH_MULTIPLE_SUBTREE_FILTERS_1 =
            "<jukebox xmlns=\"http://example.com/ns/example-jukebox\">\n" +
            "    <library>\n" +
            "        <artist>\n" +
            "            <album>\n" +
            "                <name>Album 1</name>\n" +
            "                <admin>\n" +
            "                    <label>Label 1</label>\n" +
            "                </admin>\n" +
            "            </album>\n" +
            "            <album>\n" +
            "                <name/>\n" +
            "                <admin>\n" +
            "                    <label/>\n" +
            "                </admin>\n" +
            "            </album>\n" +
            "        </artist>\n" +
            "    </library>\n" +
            "</jukebox>";


    private static final String EXPECTED_FILTER_WITH_MULTIPLE_SUBTREE_FILTERS_1 =
            "<jukebox xmlns=\"http://example.com/ns/example-jukebox\">\n" +
            "    <library>\n" +
            "        <artist>\n" +
            "            <album>\n" +
            "                <name/>\n" +
            "                <admin>\n" +
            "                    <label/>\n" +
            "                </admin>\n" +
            "            </album>\n" +
            "        </artist>\n" +
            "    </library>\n" +
            "</jukebox>";

    private  static final String FILTER_WITH_MULTIPLE_SUBTREE_FILTERS_2 =
            "<jukebox xmlns=\"http://example.com/ns/example-jukebox\">\n" +
            "    <library>\n" +
            "        <artist>\n" +
            "            <album>\n" +
            "                <name>Album 1</name>\n" +
            "                <admin>\n" +
            "                    <label>Label 1</label>\n" +
            "                </admin>\n" +
            "            </album>\n" +
            "            <album>\n" +
            "                <name>Album 1</name>\n" +
            "                <admin>\n" +
            "                    <catalogue-number>Catalogue 1</catalogue-number>\n" +
            "                </admin>\n" +
            "            </album>\n" +
            "        </artist>\n" +
            "    </library>\n" +
            "</jukebox>";

    private static final String EXPECTED_FILTER_WITH_MULTIPLE_SUBTREE_FILTERS_2 =
            "<jukebox xmlns=\"http://example.com/ns/example-jukebox\">\n" +
            "    <library>\n" +
            "        <artist>\n" +
            "            <album>\n" +
            "                <name>Album 1</name>\n" +
            "                <admin>\n" +
            "                    <label>Label 1</label>\n" +
            "                </admin>\n" +
            "                <admin>\n" +
            "                    <catalogue-number>Catalogue 1</catalogue-number>\n" +
            "                </admin>\n" +
            "            </album>\n" +
            "        </artist>\n" +
            "    </library>\n" +
            "</jukebox>";;

    private  static final String FILTER_WITH_MULTIPLE_SUBTREE_FILTERS_3 =
            "<jukebox xmlns=\"http://example.com/ns/example-jukebox\">\n" +
            "    <library>\n" +
            "        <artist>\n" +
            "            <album>\n" +
            "                <name>Album 1</name>\n" +
            "                <admin>\n" +
            "                    <label>Label 1</label>\n" +
            "                </admin>\n" +
            "            </album>\n" +
            "            <album>\n" +
            "                <name>Album 1</name>\n" +
            "                <admin>\n" +
            "                    <label/>\n" +
            "                </admin>\n" +
            "            </album>\n" +
            "        </artist>\n" +
            "    </library>\n" +
            "</jukebox>";

    private static final String EXPECTED_FILTER_WITH_MULTIPLE_SUBTREE_FILTERS_3 =
            "<jukebox xmlns=\"http://example.com/ns/example-jukebox\">\n" +
            "    <library>\n" +
            "        <artist>\n" +
            "            <album>\n" +
            "                <name>Album 1</name>\n" +
            "                <admin>\n" +
            "                    <label/>\n" +
            "                </admin>\n" +
            "            </album>\n" +
            "        </artist>\n" +
            "    </library>\n" +
            "</jukebox>";

    private  static final String FILTER_WITH_MULTIPLE_SUBTREE_FILTERS_4 =
            "<jukebox xmlns=\"http://example.com/ns/example-jukebox\">\n" +
            "    <library>\n" +
            "        <artist>\n" +
            "            <album>\n" +
            "                <name>Album 1</name>\n" +
            "                <admin>\n" +
            "                    <label>Label 1</label>\n" +
            "                </admin>\n" +
            "            </album>\n" +
            "            <album>\n" +
            "                <name>Album 1</name>\n" +
            "                <admin>\n" +
            "                    <catalogue-number/>\n" +
            "                </admin>\n" +
            "            </album>\n" +
            "        </artist>\n" +
            "    </library>\n" +
            "</jukebox>";

    private static final String EXPECTED_FILTER_WITH_MULTIPLE_SUBTREE_FILTERS_4 =
            "<jukebox xmlns=\"http://example.com/ns/example-jukebox\">\n" +
            "    <library>\n" +
            "        <artist>\n" +
            "            <album>\n" +
            "                <name>Album 1</name>\n" +
            "                <admin>\n" +
            "                    <label>Label 1</label>\n" +
            "                    <catalogue-number/>\n" +
            "                </admin>\n" +
            "            </album>\n" +
            "        </artist>\n" +
            "    </library>\n" +
            "</jukebox>";

    @Mock
    private SchemaRegistry m_schemaRegistry;

    @Before
    public void setUp(){
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testFilterWithMultipleKeys_1() throws NetconfMessageBuilderException{
        FilterNode expectedRoot = new FilterNode();
        FilterUtil.processFilter(expectedRoot, Arrays.asList(DocumentUtils.stringToDocument(EXPECTED_FILTER_WITH_MULTIPLE_KEYS_1).getDocumentElement()));

        FilterNode root = new FilterNode();
        FilterUtil.buildMergedFilter(root, Arrays.asList(DocumentUtils.stringToDocument(FILTER_WITH_MULTIPLE_KEYS_1).getDocumentElement()), m_schemaRegistry);

        assertEquals(expectedRoot, root);

    }

    @Test
    public void testFilterWithMultipleKeys_2() throws NetconfMessageBuilderException{
        FilterNode expectedRoot = new FilterNode();
        FilterUtil.processFilter(expectedRoot, Arrays.asList(DocumentUtils.stringToDocument(EXPECTED_FILTER_WITH_MULTIPLE_KEYS_2).getDocumentElement()));

        FilterNode root = new FilterNode();
        FilterUtil.buildMergedFilter(root, Arrays.asList(DocumentUtils.stringToDocument(FILTER_WITH_MULTIPLE_KEYS_2).getDocumentElement()), m_schemaRegistry);

        assertEquals(expectedRoot, root);
    }

    @Test
    public void testFilterWithMultipleKeys_3() throws NetconfMessageBuilderException{
        FilterNode expectedRoot = new FilterNode();
        FilterUtil.processFilter(expectedRoot, Arrays.asList(DocumentUtils.stringToDocument(EXPECTED_FILTER_WITH_MULTIPLE_KEYS_3).getDocumentElement()));

        FilterNode root = new FilterNode();
        FilterUtil.buildMergedFilter(root, Arrays.asList(DocumentUtils.stringToDocument(FILTER_WITH_MULTIPLE_KEYS_3).getDocumentElement()), m_schemaRegistry);

        assertEquals(expectedRoot, root);
    }


    @Test
    public void testFilterWithMultipleKeys_4() throws NetconfMessageBuilderException{
        FilterNode expectedRoot = new FilterNode();
        FilterUtil.processFilter(expectedRoot, Arrays.asList(DocumentUtils.stringToDocument(EXPECTED_FILTER_WITH_MULTIPLE_KEYS_4).getDocumentElement()));

        FilterNode root = new FilterNode();
        FilterUtil.buildMergedFilter(root, Arrays.asList(DocumentUtils.stringToDocument(FILTER_WITH_MULTIPLE_KEYS_4).getDocumentElement()), m_schemaRegistry);

        assertEquals(expectedRoot, root);

    }

    @Test
    public void testFilterWithMultipleKeys_5() throws NetconfMessageBuilderException{
        FilterNode expectedRoot = new FilterNode();
        FilterUtil.processFilter(expectedRoot, Arrays.asList(DocumentUtils.stringToDocument(EXPECTED_FILTER_WITH_MULTIPLE_KEYS_5).getDocumentElement()));

        FilterNode root = new FilterNode();
        FilterUtil.buildMergedFilter(root, Arrays.asList(DocumentUtils.stringToDocument(FILTER_WITH_MULTIPLE_KEYS_5).getDocumentElement()), m_schemaRegistry);

        assertEquals(expectedRoot, root);
    }

    @Test
    public void testFilterWithMultipleKeys_6() throws NetconfMessageBuilderException{
        FilterNode expectedRoot = new FilterNode();
        FilterUtil.processFilter(expectedRoot, Arrays.asList(DocumentUtils.stringToDocument(EXPECTED_FILTER_WITH_MULTIPLE_KEYS_6).getDocumentElement()));

        FilterNode root = new FilterNode();
        FilterUtil.buildMergedFilter(root, Arrays.asList(DocumentUtils.stringToDocument(FILTER_WITH_MULTIPLE_KEYS_6).getDocumentElement()), m_schemaRegistry);

        assertEquals(expectedRoot, root);
    }

    @Test
    public void testFilterWithMultipleKeys_7() throws NetconfMessageBuilderException{
        FilterNode expectedRoot = new FilterNode();
        FilterUtil.processFilter(expectedRoot, Arrays.asList(DocumentUtils.stringToDocument(EXPECTED_FILTER_WITH_MULTIPLE_KEYS_7).getDocumentElement()));

        FilterNode root = new FilterNode();
        FilterUtil.buildMergedFilter(root, Arrays.asList(DocumentUtils.stringToDocument(FILTER_WITH_MULTIPLE_KEYS_7).getDocumentElement()), m_schemaRegistry);

        assertEquals(expectedRoot, root);
    }


    @Test
    public void testFilterWithMultipleKeys_8() throws NetconfMessageBuilderException{
        FilterNode expectedRoot = new FilterNode();
        FilterUtil.processFilter(expectedRoot, Arrays.asList(DocumentUtils.stringToDocument(EXPECTED_FILTER_WITH_MULTIPLE_KEYS_8).getDocumentElement()));

        FilterNode root = new FilterNode();
        FilterUtil.buildMergedFilter(root, Arrays.asList(DocumentUtils.stringToDocument(FILTER_WITH_MULTIPLE_KEYS_8).getDocumentElement()), m_schemaRegistry);

        assertEquals(expectedRoot, root);
    }

    @Test
    public void testFilterWithMultipleKeys_9() throws NetconfMessageBuilderException{
        FilterNode expectedRoot = new FilterNode();
        FilterUtil.processFilter(expectedRoot, Arrays.asList(DocumentUtils.stringToDocument(EXPECTED_FILTER_WITH_MULTIPLE_KEYS_9).getDocumentElement()));

        FilterNode root = new FilterNode();
        FilterUtil.buildMergedFilter(root, Arrays.asList(DocumentUtils.stringToDocument(FILTER_WITH_MULTIPLE_KEYS_9).getDocumentElement()), m_schemaRegistry);

        assertEquals(expectedRoot, root);
    }

    @Test
    public void testProcessFilter_FilterWithMultipleSelectNodes() throws NetconfMessageBuilderException {
        FilterNode root = new FilterNode();
        FilterUtil.processFilter(root, Arrays.asList(DocumentUtils.stringToDocument(FILTER_WITH_MULTIPLE_SELECT_NODES).getDocumentElement()));

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
        FilterUtil.processFilter(root, Arrays.asList(DocumentUtils.stringToDocument(FILTER_WITH_SELECT_AND_CONTAINMENT_NODES1).getDocumentElement()));

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
        FilterUtil.processFilter(root, Arrays.asList(DocumentUtils.stringToDocument(FILTER_WITH_SELECT_AND_CONTAINMENT_NODES2)
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
        FilterUtil.processFilter(root, Arrays.asList(DocumentUtils.stringToDocument(FILTER_WITH_SELECT_AND_CONTAINMENT_NODES3)
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

        FilterNode expected = new FilterNode();

        FilterNode jbNode = expected.addContainmentNode("jukebox","http://example.com/ns/example-jukebox");
        FilterNode libraryNode = jbNode.addContainmentNode("library","http://example.com/ns/example-jukebox");
        FilterNode artistNode = libraryNode.addContainmentNode("artist","http://example.com/ns/example-jukebox");
        FilterNode albumNode = artistNode.addContainmentNode("album","http://example.com/ns/example-jukebox");
        FilterNode songNode1 = albumNode.addContainmentNode("song","http://example.com/ns/example-jukebox");
        songNode1.addMatchNode("name","http://example.com/ns/example-jukebox","Song name1");
        FilterNode songNode2 = albumNode.addContainmentNode("song","http://example.com/ns/example-jukebox");
        songNode2.addMatchNode("name","http://example.com/ns/example-jukebox","Song name2");
        assertEquals(expected , root);
    }

    @Test
    public void testProcessFilter_FilterWithMultipleMatchNodesImproved() throws NetconfMessageBuilderException {
        FilterNode root = new FilterNode();
        FilterUtil.buildMergedFilter(root, Arrays.asList(DocumentUtils.stringToDocument(FILTER_WITH_MULTIPLE_MATCH_NODES_IMPROVED)
                .getDocumentElement()),m_schemaRegistry);
        

        FilterNode expected = new FilterNode();

        FilterNode jbNode = expected.addContainmentNode("jukebox","http://example.com/ns/example-jukebox");
        FilterNode libraryNode = jbNode.addContainmentNode("library","http://example.com/ns/example-jukebox");
        FilterNode artistNode = libraryNode.addContainmentNode("artist","http://example.com/ns/example-jukebox");
        FilterNode albumNode = artistNode.addContainmentNode("album","http://example.com/ns/example-jukebox");
        albumNode.addMatchNode("name","http://example.com/ns/example-jukebox","Album name1");

        FilterNode songNode1 = albumNode.addContainmentNode("song","http://example.com/ns/example-jukebox");
        songNode1.addMatchNode("name","http://example.com/ns/example-jukebox","Song name1");
        FilterNode songNode2 = albumNode.addContainmentNode("song","http://example.com/ns/example-jukebox");
        songNode2.addMatchNode("name","http://example.com/ns/example-jukebox","Song name2");
        FilterNode songNode3 = albumNode.addContainmentNode("song","http://example.com/ns/example-jukebox");
        songNode3.addMatchNode("name","http://example.com/ns/example-jukebox","Song name3");

        assertEquals(expected , root);
    }


    @Test
    public void testProcessFilter_FilterWithMultipleMatchNodesImprovedV2() throws NetconfMessageBuilderException {
        FilterNode root = new FilterNode();
        FilterUtil.buildMergedFilter(root, Arrays.asList(DocumentUtils.stringToDocument(FILTER_WITH_MULTIPLE_MATCH_NODES_IMPROVED_V2)
                .getDocumentElement()), m_schemaRegistry);
        

        FilterNode expected = new FilterNode();

        FilterNode jbNode = expected.addContainmentNode("jukebox","http://example.com/ns/example-jukebox");
        FilterNode libraryNode = jbNode.addContainmentNode("library","http://example.com/ns/example-jukebox");
        FilterNode artistNode = libraryNode.addContainmentNode("artist","http://example.com/ns/example-jukebox");
        FilterNode albumNode1 = artistNode.addContainmentNode("album","http://example.com/ns/example-jukebox");
        albumNode1.addMatchNode("name","http://example.com/ns/example-jukebox","Album name1");
        FilterNode songNode1 = albumNode1.addContainmentNode("song","http://example.com/ns/example-jukebox");
        songNode1.addMatchNode("name","http://example.com/ns/example-jukebox","Song name1");
        FilterNode songNode3 = albumNode1.addContainmentNode("song","http://example.com/ns/example-jukebox");
        songNode3.addMatchNode("name","http://example.com/ns/example-jukebox","Song name3");
        FilterNode albumNode2 = artistNode.addContainmentNode("album","http://example.com/ns/example-jukebox");
        albumNode2.addMatchNode("name","http://example.com/ns/example-jukebox","Album name2");
        FilterNode songNode2 = albumNode2.addContainmentNode("song","http://example.com/ns/example-jukebox");
        songNode2.addMatchNode("name","http://example.com/ns/example-jukebox","Song name2");

        assertEquals(expected , root);
    }

    @Test
    public void testModelNodeIdIsConvertedIntoFilter() throws NetconfMessageBuilderException, IOException, SAXException {
        ModelNodeId nodeId = new ModelNodeId("/container=root/container=level1/key1=value1/container=level2/container=level3", "ns1");
        nodeId.addRdn(new ModelNodeRdn(CONTAINER, "ns2", "level3"));
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

    @Test
    public void testProcessFilter_FilterWithMatchNodeAttr() throws NetconfMessageBuilderException {
        FilterNode root = new FilterNode();
        FilterUtil.processFilter(root, Arrays.asList(DocumentUtils.stringToDocument(FILTER_WITH_ATTRIBUTES_IN_MATCH_NODES)
                .getDocumentElement()));

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

        FilterNode song1Node = albumNode.getChildNodes().get(0);
        assertEquals("song", song1Node.getNodeName());
        assertEquals("http://example.com/ns/example-jukebox", song1Node.getNamespace());
        assertEquals(1, song1Node.getMatchNodes().size());
        assertEquals(0, song1Node.getSelectNodes().size());
        assertEquals(0, song1Node.getChildNodes().size());
        FilterMatchNode nameMatchNode = song1Node.getMatchNodes().get(0);
        assertEquals("name", nameMatchNode.getNodeName());
        assertEquals("pf:Song name1", nameMatchNode.getFilter());
        assertEquals("http://example.com/ns/example-jukebox", nameMatchNode.getNamespace());
        Map<String,String> attributes = new HashMap<>();
        attributes.put("xmlns:pf","ns");
        assertEquals(attributes, nameMatchNode.getAttributes());

    }

    @Test
    public void testGetSubSystem() throws Exception{
    	QName VALIDATION_QNAME = QName.create("urn:org:bbf2:pma:validation", "2015-12-14", "validation");
    	SchemaPath schemaPath = SchemaPath.create(true, VALIDATION_QNAME);
    	SubSystemRegistry subSystemRegistry = Mockito.mock(SubSystemRegistry.class);
    	FilterUtil.getSubSystem(subSystemRegistry, schemaPath);
    }

    @Test
    public void testMergingTwoSubtreeFilters_1() throws NetconfMessageBuilderException {

        FilterNode expectedRoot = new FilterNode();
        FilterUtil.processFilter(expectedRoot, Arrays.asList(DocumentUtils.stringToDocument(EXPECTED_FILTER_WITH_MULTIPLE_SUBTREE_FILTERS_1).getDocumentElement()));

        FilterNode root = new FilterNode();
        FilterUtil.buildMergedFilter(root, Arrays.asList(DocumentUtils.stringToDocument(FILTER_WITH_MULTIPLE_SUBTREE_FILTERS_1).getDocumentElement()), m_schemaRegistry);

        assertEquals(expectedRoot, root);
    }

    @Test
    public void testMergingTwoSubtreeFilters_2() throws NetconfMessageBuilderException {

        FilterNode expectedRoot = new FilterNode();
        FilterUtil.processFilter(expectedRoot, Arrays.asList(DocumentUtils.stringToDocument(EXPECTED_FILTER_WITH_MULTIPLE_SUBTREE_FILTERS_2).getDocumentElement()));

        FilterNode root = new FilterNode();
        FilterUtil.buildMergedFilter(root, Arrays.asList(DocumentUtils.stringToDocument(FILTER_WITH_MULTIPLE_SUBTREE_FILTERS_2).getDocumentElement()), m_schemaRegistry);

        assertEquals(expectedRoot, root);
    }

    @Test
    public void testMergingTwoSubtreeFilters_3() throws NetconfMessageBuilderException {

        FilterNode expectedRoot = new FilterNode();
        FilterUtil.processFilter(expectedRoot, Arrays.asList(DocumentUtils.stringToDocument(EXPECTED_FILTER_WITH_MULTIPLE_SUBTREE_FILTERS_3).getDocumentElement()));

        FilterNode root = new FilterNode();
        FilterUtil.buildMergedFilter(root, Arrays.asList(DocumentUtils.stringToDocument(FILTER_WITH_MULTIPLE_SUBTREE_FILTERS_3).getDocumentElement()), m_schemaRegistry);

        assertEquals(expectedRoot, root);
    }

    @Test
    public void testMergingTwoSubtreeFilters_4() throws NetconfMessageBuilderException {

        FilterNode expectedRoot = new FilterNode();
        FilterUtil.processFilter(expectedRoot, Arrays.asList(DocumentUtils.stringToDocument(EXPECTED_FILTER_WITH_MULTIPLE_SUBTREE_FILTERS_4).getDocumentElement()));

        FilterNode root = new FilterNode();
        FilterUtil.buildMergedFilter(root, Arrays.asList(DocumentUtils.stringToDocument(FILTER_WITH_MULTIPLE_SUBTREE_FILTERS_4).getDocumentElement()), m_schemaRegistry);

        assertEquals(expectedRoot, root);
    }
}
