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

import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;

/**
 * Created by kbhatk on 9/22/16.
 */
public class YangChoiceCaseTest extends AbstractYangValidationTestSetup {
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
    private static final String RESPONSE_AFTER_CREATE_DEVICE = "<rpc-reply message-id=\"1\" " +
            "xmlns=\"urn:ietf:params:xml:ns:netconf:base" +
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
                    "    </device-manager>\n";
    ;
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
        Element configuredDeviceProperties = DocumentUtils.stringToDocument(ADD_CONFIGURED_DEVICE_PROPERTIES)
                .getDocumentElement();
        TestUtil.sendEditConfig(m_server, configuredDeviceProperties, StandardDataStores.RUNNING);
        TestUtil.verifyGetConfig(m_server, RESPONSE_AFTER_ADD_CONFIGURED_DEVICE_PROPERTIES);

    }

    @Test
    public void testChoiceCaseMergeDeletesOtherCases2() throws Exception {
        //add configured device properties
        Element configuredDeviceProperties = DocumentUtils.stringToDocument(ADD_CONFIGURED_DEVICE_PROPERTIES)
                .getDocumentElement();
        TestUtil.sendEditConfig(m_server, configuredDeviceProperties, StandardDataStores.RUNNING);
        TestUtil.verifyGetConfig(m_server, RESPONSE_AFTER_ADD_CONFIGURED_DEVICE_PROPERTIES);

        Element createDeviceWithDUID = DocumentUtils.stringToDocument(CREATE_DEVICE_WITH_DUID).getDocumentElement();
        TestUtil.sendEditConfig(m_server, createDeviceWithDUID, StandardDataStores.RUNNING);
        TestUtil.verifyGetConfig(m_server, RESPONSE_AFTER_CREATE_DEVICE);

    }
}
