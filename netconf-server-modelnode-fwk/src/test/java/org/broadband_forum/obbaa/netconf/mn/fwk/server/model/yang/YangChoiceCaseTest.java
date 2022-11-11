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

import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.assertXMLEquals;

import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Element;

/**
 * Created by kbhatk on 9/22/16.
 */
@RunWith(RequestScopeJunitRunner.class)
public class YangChoiceCaseTest extends AbstractYangValidationTestSetup{
    private static final String CREATE_DEVICE_WITH_DUID =
            "    <device-manager xmlns=\"urn:choice-case-test\">\n" +
            "      <device-holder>\n" +
            "        <name>OLT-1</name>\n" +
            "        <device>\n" +
            "          <device-id>ONT1</device-id>\n" +
            "          <duid>MAC-FC12-32AC-23DE</duid>\n" +
            "        </device>\n" +
            "      </device-holder>\n" +
            "    </device-manager>\n";
    private static final String RESPONSE_AFTER_CREATE_DEVICE = "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base" +
            ":1.0\">\n" +
            "  <data>\n" +
            "    <cct:device-manager xmlns:cct=\"urn:choice-case-test\">\n" +
            "      <cct:device-holder>\n" +
            "        <cct:name>OLT-1</cct:name>\n" +
            "        <cct:device>\n" +
            "          <cct:device-id>ONT1</cct:device-id>\n" +
            "          <cct:duid>MAC-FC12-32AC-23DE</cct:duid>\n" +
            "        </cct:device>\n" +
            "      </cct:device-holder>\n" +
            "    </cct:device-manager>\n" +
            "  </data>\n" +
            "</rpc-reply>\n";
    private static final String ADD_CONFIGURED_DEVICE_PROPERTIES =
            "    <device-manager xmlns=\"urn:choice-case-test\">\n" +
                    "      <device-holder>\n" +
                    "        <name>OLT-1</name>\n" +
                    "        <device>\n" +
                    "          <device-id>ONT1</device-id>\n" +
                    "           <configured-device-properties>\n" +
                    "            <ip-address>135.249.45.153</ip-address>\n" +
                    "            <ip-port>9496</ip-port>\n" +
                    "          </configured-device-properties>\n" +
                    "        </device>\n" +
                    "      </device-holder>\n" +
                    "    </device-manager>\n";;
    private static final String RESPONSE_AFTER_ADD_CONFIGURED_DEVICE_PROPERTIES =
            "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
            "  <data>\n" +
            "    <cct:device-manager xmlns:cct=\"urn:choice-case-test\">\n" +
            "      <cct:device-holder>\n" +
            "        <cct:name>OLT-1</cct:name>\n" +
            "        <cct:device>\n" +
            "          <cct:device-id>ONT1</cct:device-id>\n" +
            "          <cct:configured-device-properties>\n" +
            "            <cct:ip-address>135.249.45.153</cct:ip-address>\n" +
            "            <cct:ip-port>9496</cct:ip-port>\n" +
            "          </cct:configured-device-properties>\n" +
            "        </cct:device>\n" +
            "      </cct:device-holder>\n" +
            "    </cct:device-manager>\n" +
            "  </data>\n" +
            "</rpc-reply>\n";

    private static final String ADD_NESTED_CHOICE_CASE_LEVEL1_LIST =
            "<root-container xmlns=\"urn:nested-choice-case-test\">\n" +
            "  <level1-case1-list>\n" +
            "    <level1-case1-list-key>level1-case1-list</level1-case1-list-key>\n" +
            "  </level1-case1-list>\n" +
            "</root-container>";
    private static final String ADD_NESTED_CHOICE_CASE_LEVEL1_LIST_RESPONSE =
            "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
            " <data>\n" +
            "  <nestedchoice:root-container xmlns:nestedchoice=\"urn:nested-choice-case-test\">\n" +
            "   <nestedchoice:level1-case1-list>\n" +
            "    <nestedchoice:level1-case1-list-key>level1-case1-list</nestedchoice:level1-case1-list-key>\n" +
            "   </nestedchoice:level1-case1-list>\n" +
            "  </nestedchoice:root-container>\n" +
            " </data>\n" +
            "</rpc-reply>";
    private static final String MERGE_LEAF_NESTED_CHOICE_CASE_LEVEL1_LIST =
            "<root-container xmlns=\"urn:nested-choice-case-test\">\n" +
            "  <level1-case1-list>\n" +
            "    <level1-case1-list-key>level1-case1-list</level1-case1-list-key>\n" +
            "    <level1-case1-list-leaf>level1-case1-list-leaf</level1-case1-list-leaf>\n" +
            "  </level1-case1-list>\n" +
            "</root-container>";
    private static final String MERGE_LEAF_NESTED_CHOICE_CASE_LEVEL1_LIST_RESPONSE =
            "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
            " <data>\n" +
            "  <nestedchoice:root-container xmlns:nestedchoice=\"urn:nested-choice-case-test\">\n" +
            "   <nestedchoice:level1-case1-list>\n" +
            "    <nestedchoice:level1-case1-list-key>level1-case1-list</nestedchoice:level1-case1-list-key>\n" +
            "    <nestedchoice:level1-case1-list-leaf>level1-case1-list-leaf</nestedchoice:level1-case1-list-leaf>\n" +
            "   </nestedchoice:level1-case1-list>\n" +
            "  </nestedchoice:root-container>\n" +
            " </data>\n" +
            "</rpc-reply>";

    private static final String ADD_NESTED_CHOICE_CASE_LEVEL1_LIST_ORDERED =
            "<root-container xmlns=\"urn:nested-choice-case-test\">\n" +
            "  <level1-case1-list-ordered>\n" +
            "    <level1-case1-list-key-ordered>level1-case1-list-ordered1</level1-case1-list-key-ordered>\n" +
            "  </level1-case1-list-ordered>\n" +
            "  <level1-case1-list-ordered>\n" +
            "    <level1-case1-list-key-ordered>level1-case1-list-ordered2</level1-case1-list-key-ordered>\n" +
            "  </level1-case1-list-ordered>\n" +
            "</root-container>";
    private static final String ADD_NESTED_CHOICE_CASE_LEVEL1_LIST_ORDERED_RESPONSE =
            "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
            " <data>\n" +
            "  <nestedchoice:root-container xmlns:nestedchoice=\"urn:nested-choice-case-test\">\n" +
            "   <nestedchoice:level1-case1-list-ordered>\n" +
            "    <nestedchoice:level1-case1-list-key-ordered>level1-case1-list-ordered1</nestedchoice:level1-case1-list-key-ordered>\n" +
            "   </nestedchoice:level1-case1-list-ordered>\n" +
            "   <nestedchoice:level1-case1-list-ordered>\n" +
            "    <nestedchoice:level1-case1-list-key-ordered>level1-case1-list-ordered2</nestedchoice:level1-case1-list-key-ordered>\n" +
            "   </nestedchoice:level1-case1-list-ordered>\n" +
            "  </nestedchoice:root-container>\n" +
            " </data>\n" +
            "</rpc-reply>";
    private static final String MERGE_LEAF_NESTED_CHOICE_CASE_LEVEL1_LIST_ORDERED =
            "<root-container xmlns=\"urn:nested-choice-case-test\">\n" +
            "  <level1-case1-list-ordered>\n" +
            "    <level1-case1-list-key-ordered>level1-case1-list-ordered1</level1-case1-list-key-ordered>\n" +
            "    <level1-case1-list-leaf-ordered>level1-case1-list-leaf-ordered1</level1-case1-list-leaf-ordered>\n" +
            "  </level1-case1-list-ordered>\n" +
            "  <level1-case1-list-ordered>\n" +
            "    <level1-case1-list-key-ordered>level1-case1-list-ordered2</level1-case1-list-key-ordered>\n" +
            "    <level1-case1-list-leaf-ordered>level1-case1-list-leaf-ordered2</level1-case1-list-leaf-ordered>\n" +
            "  </level1-case1-list-ordered>\n" +
            "</root-container>";
    private static final String MERGE_LEAF_NESTED_CHOICE_CASE_LEVEL1_LIST_ORDERED_RESPONSE =
            "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
            " <data>\n" +
            "  <nestedchoice:root-container xmlns:nestedchoice=\"urn:nested-choice-case-test\">\n" +
            "   <nestedchoice:level1-case1-list-ordered>\n" +
            "    <nestedchoice:level1-case1-list-key-ordered>level1-case1-list-ordered1</nestedchoice:level1-case1-list-key-ordered>\n" +
            "    <nestedchoice:level1-case1-list-leaf-ordered>level1-case1-list-leaf-ordered1</nestedchoice:level1-case1-list-leaf-ordered>\n" +
            "   </nestedchoice:level1-case1-list-ordered>\n" +
            "   <nestedchoice:level1-case1-list-ordered>\n" +
            "    <nestedchoice:level1-case1-list-key-ordered>level1-case1-list-ordered2</nestedchoice:level1-case1-list-key-ordered>\n" +
            "    <nestedchoice:level1-case1-list-leaf-ordered>level1-case1-list-leaf-ordered2</nestedchoice:level1-case1-list-leaf-ordered>\n" +
            "   </nestedchoice:level1-case1-list-ordered>\n" +
            "  </nestedchoice:root-container>\n" +
            " </data>\n" +
            "</rpc-reply>";

    private static final String ADD_NESTED_CHOICE_CASE_LEVEL2_LIST =
            "<root-container xmlns=\"urn:nested-choice-case-test\">\n" +
            "  <level2-case1-list>\n" +
            "    <level2-case1-list-key>level2-list</level2-case1-list-key>\n" +
            "  </level2-case1-list>\n" +
            "</root-container>";
    private static final String ADD_NESTED_CHOICE_CASE_LEVEL2_LIST_RESPONSE =
            "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
            " <data>\n" +
            "  <nestedchoice:root-container xmlns:nestedchoice=\"urn:nested-choice-case-test\">\n" +
            "   <nestedchoice:level2-case1-list>\n" +
            "    <nestedchoice:level2-case1-list-key>level2-list</nestedchoice:level2-case1-list-key>\n" +
            "   </nestedchoice:level2-case1-list>\n" +
            "  </nestedchoice:root-container>\n" +
            " </data>\n" +
            "</rpc-reply>";
    private static final String MERGE_LEAF_NESTED_CHOICE_CASE_LEVEL2_LIST =
            "<root-container xmlns=\"urn:nested-choice-case-test\">\n" +
            "  <level2-case1-list>\n" +
            "    <level2-case1-list-key>level2-list</level2-case1-list-key>\n" +
            "    <level2-case1-list-leaf>level2-list-leaf</level2-case1-list-leaf>\n" +
            "  </level2-case1-list>\n" +
            "</root-container>";
    private static final String MERGE_LEAF_NESTED_CHOICE_CASE_LEVEL2_LIST_RESPONSE =
            "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
            " <data>\n" +
            "  <nestedchoice:root-container xmlns:nestedchoice=\"urn:nested-choice-case-test\">\n" +
            "   <nestedchoice:level2-case1-list>\n" +
            "    <nestedchoice:level2-case1-list-key>level2-list</nestedchoice:level2-case1-list-key>\n" +
            "    <nestedchoice:level2-case1-list-leaf>level2-list-leaf</nestedchoice:level2-case1-list-leaf>\n" +
            "   </nestedchoice:level2-case1-list>\n" +
            "  </nestedchoice:root-container>\n" +
            " </data>\n" +
            "</rpc-reply>";

    private static final String ADD_NESTED_CHOICE_CASE_LEVEL2_LIST_ORDERED =
            "<root-container xmlns=\"urn:nested-choice-case-test\">\n" +
            "  <level2-case1-list-ordered>\n" +
            "    <level2-case1-list-key-ordered>level2-case1-list-ordered1</level2-case1-list-key-ordered>\n" +
            "  </level2-case1-list-ordered>\n" +
            "  <level2-case1-list-ordered>\n" +
            "    <level2-case1-list-key-ordered>level2-case1-list-ordered2</level2-case1-list-key-ordered>\n" +
            "  </level2-case1-list-ordered>\n" +
            "</root-container>";
    private static final String ADD_NESTED_CHOICE_CASE_LEVEL2_LIST_ORDERED_RESPONSE =
            "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
            " <data>\n" +
            "  <nestedchoice:root-container xmlns:nestedchoice=\"urn:nested-choice-case-test\">\n" +
            "   <nestedchoice:level2-case1-list-ordered>\n" +
            "    <nestedchoice:level2-case1-list-key-ordered>level2-case1-list-ordered1</nestedchoice:level2-case1-list-key-ordered>\n" +
            "   </nestedchoice:level2-case1-list-ordered>\n" +
            "   <nestedchoice:level2-case1-list-ordered>\n" +
            "    <nestedchoice:level2-case1-list-key-ordered>level2-case1-list-ordered2</nestedchoice:level2-case1-list-key-ordered>\n" +
            "   </nestedchoice:level2-case1-list-ordered>\n" +
            "  </nestedchoice:root-container>\n" +
            " </data>\n" +
            "</rpc-reply>";
    private static final String MERGE_LEAF_NESTED_CHOICE_CASE_LEVEL2_LIST_ORDERED =
            "<root-container xmlns=\"urn:nested-choice-case-test\">\n" +
            "  <level2-case1-list-ordered>\n" +
            "    <level2-case1-list-key-ordered>level2-case1-list-ordered1</level2-case1-list-key-ordered>\n" +
            "    <level2-case1-list-leaf-ordered>level2-case1-list-leaf-ordered1</level2-case1-list-leaf-ordered>\n" +
            "  </level2-case1-list-ordered>\n" +
            "  <level2-case1-list-ordered>\n" +
            "    <level2-case1-list-key-ordered>level2-case1-list-ordered2</level2-case1-list-key-ordered>\n" +
            "    <level2-case1-list-leaf-ordered>level2-case1-list-leaf-ordered2</level2-case1-list-leaf-ordered>\n" +
            "  </level2-case1-list-ordered>\n" +
            "</root-container>";
    private static final String MERGE_LEAF_NESTED_CHOICE_CASE_LEVEL2_LIST_ORDERED_RESPONSE =
            "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
            " <data>\n" +
            "  <nestedchoice:root-container xmlns:nestedchoice=\"urn:nested-choice-case-test\">\n" +
            "   <nestedchoice:level2-case1-list-ordered>\n" +
            "    <nestedchoice:level2-case1-list-key-ordered>level2-case1-list-ordered1</nestedchoice:level2-case1-list-key-ordered>\n" +
            "    <nestedchoice:level2-case1-list-leaf-ordered>level2-case1-list-leaf-ordered1</nestedchoice:level2-case1-list-leaf-ordered>\n" +
            "   </nestedchoice:level2-case1-list-ordered>\n" +
            "   <nestedchoice:level2-case1-list-ordered>\n" +
            "    <nestedchoice:level2-case1-list-key-ordered>level2-case1-list-ordered2</nestedchoice:level2-case1-list-key-ordered>\n" +
            "    <nestedchoice:level2-case1-list-leaf-ordered>level2-case1-list-leaf-ordered2</nestedchoice:level2-case1-list-leaf-ordered>\n" +
            "   </nestedchoice:level2-case1-list-ordered>\n" +
            "  </nestedchoice:root-container>\n" +
            " </data>\n" +
            "</rpc-reply>";

    @Before
    public void setUp() throws Exception {
        super.setUpForChoiceCase();
   }

    @Test
    public void testChoiceCaseMergeDeletesOtherCases() throws Exception {
        Element createDeviceWithDUID = DocumentUtils.stringToDocument(CREATE_DEVICE_WITH_DUID).getDocumentElement();
        TestUtil.sendEditConfig(m_server, createDeviceWithDUID, StandardDataStores.RUNNING);
        TestUtil.verifyGetConfig(m_server, RESPONSE_AFTER_CREATE_DEVICE);

        //add configured device properties
        Element configuredDeviceProperties = DocumentUtils.stringToDocument(ADD_CONFIGURED_DEVICE_PROPERTIES).getDocumentElement();
        TestUtil.sendEditConfig(m_server, configuredDeviceProperties, StandardDataStores.RUNNING);
        TestUtil.verifyGetConfig(m_server, RESPONSE_AFTER_ADD_CONFIGURED_DEVICE_PROPERTIES);

    }
    @Test
    public void testChoiceCaseMergeDeletesOtherCases2() throws Exception {
        //add configured device properties
        Element configuredDeviceProperties = DocumentUtils.stringToDocument(ADD_CONFIGURED_DEVICE_PROPERTIES).getDocumentElement();
        TestUtil.sendEditConfig(m_server, configuredDeviceProperties, StandardDataStores.RUNNING);
        TestUtil.verifyGetConfig(m_server, RESPONSE_AFTER_ADD_CONFIGURED_DEVICE_PROPERTIES);

        Element createDeviceWithDUID = DocumentUtils.stringToDocument(CREATE_DEVICE_WITH_DUID).getDocumentElement();
        TestUtil.sendEditConfig(m_server, createDeviceWithDUID, StandardDataStores.RUNNING);
        TestUtil.verifyGetConfig(m_server, RESPONSE_AFTER_CREATE_DEVICE);

    }

    @Test
    public void testMergeNestedCase_ListInLevel1() throws Exception {
        setUpForNestedChoiceCase();

        Element nestedChoiceCaseList = DocumentUtils.stringToDocument(ADD_NESTED_CHOICE_CASE_LEVEL1_LIST).getDocumentElement();
        NetConfResponse response = TestUtil.sendEditConfig(m_server, nestedChoiceCaseList, "1");

        assertXMLEquals("/ok-response.xml", response);
        TestUtil.verifyGetConfig(m_server, ADD_NESTED_CHOICE_CASE_LEVEL1_LIST_RESPONSE);

        Element mergeLeafToNestedChoiceCaseList = DocumentUtils.stringToDocument(MERGE_LEAF_NESTED_CHOICE_CASE_LEVEL1_LIST).getDocumentElement();
        response = TestUtil.sendEditConfig(m_server, mergeLeafToNestedChoiceCaseList, "1");
        assertXMLEquals("/ok-response.xml", response);
        TestUtil.verifyGetConfig(m_server, MERGE_LEAF_NESTED_CHOICE_CASE_LEVEL1_LIST_RESPONSE);
    }

    @Test
    public void testMergeNestedCase_OrderedListInLevel1() throws Exception {
        setUpForNestedChoiceCase();

        Element nestedChoiceCaseList = DocumentUtils.stringToDocument(ADD_NESTED_CHOICE_CASE_LEVEL1_LIST_ORDERED).getDocumentElement();
        NetConfResponse response = TestUtil.sendEditConfig(m_server, nestedChoiceCaseList, "1");

        assertXMLEquals("/ok-response.xml", response);
        TestUtil.verifyGetConfig(m_server, ADD_NESTED_CHOICE_CASE_LEVEL1_LIST_ORDERED_RESPONSE);

        Element mergeLeafToNestedChoiceCaseList = DocumentUtils.stringToDocument(MERGE_LEAF_NESTED_CHOICE_CASE_LEVEL1_LIST_ORDERED).getDocumentElement();
        response = TestUtil.sendEditConfig(m_server, mergeLeafToNestedChoiceCaseList, "1");
        assertXMLEquals("/ok-response.xml", response);
        TestUtil.verifyGetConfig(m_server, MERGE_LEAF_NESTED_CHOICE_CASE_LEVEL1_LIST_ORDERED_RESPONSE);
    }

    @Test
    public void testMergeNestedCase_ListInLevel2() throws Exception {
        setUpForNestedChoiceCase();

        Element nestedChoiceCaseList = DocumentUtils.stringToDocument(ADD_NESTED_CHOICE_CASE_LEVEL2_LIST).getDocumentElement();
        NetConfResponse response = TestUtil.sendEditConfig(m_server, nestedChoiceCaseList, "1");

        assertXMLEquals("/ok-response.xml", response);
        TestUtil.verifyGetConfig(m_server, ADD_NESTED_CHOICE_CASE_LEVEL2_LIST_RESPONSE);

        Element mergeLeafToNestedChoiceCaseList = DocumentUtils.stringToDocument(MERGE_LEAF_NESTED_CHOICE_CASE_LEVEL2_LIST).getDocumentElement();
        response = TestUtil.sendEditConfig(m_server, mergeLeafToNestedChoiceCaseList, "1");
        assertXMLEquals("/ok-response.xml", response);
        TestUtil.verifyGetConfig(m_server, MERGE_LEAF_NESTED_CHOICE_CASE_LEVEL2_LIST_RESPONSE);
    }

    @Test
    public void testMergeNestedCase_OrderedListInLevel2() throws Exception {
        setUpForNestedChoiceCase();

        Element nestedChoiceCaseList = DocumentUtils.stringToDocument(ADD_NESTED_CHOICE_CASE_LEVEL2_LIST_ORDERED).getDocumentElement();
        NetConfResponse response = TestUtil.sendEditConfig(m_server, nestedChoiceCaseList, "1");

        assertXMLEquals("/ok-response.xml", response);
        TestUtil.verifyGetConfig(m_server, ADD_NESTED_CHOICE_CASE_LEVEL2_LIST_ORDERED_RESPONSE);

        Element mergeLeafToNestedChoiceCaseList = DocumentUtils.stringToDocument(MERGE_LEAF_NESTED_CHOICE_CASE_LEVEL2_LIST_ORDERED).getDocumentElement();
        response = TestUtil.sendEditConfig(m_server, mergeLeafToNestedChoiceCaseList, "1");
        assertXMLEquals("/ok-response.xml", response);
        TestUtil.verifyGetConfig(m_server, MERGE_LEAF_NESTED_CHOICE_CASE_LEVEL2_LIST_ORDERED_RESPONSE);
    }
}
