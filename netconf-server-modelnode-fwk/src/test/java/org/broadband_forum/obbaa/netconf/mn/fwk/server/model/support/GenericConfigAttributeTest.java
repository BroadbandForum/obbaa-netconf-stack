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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support;

import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

@RunWith(RequestScopeJunitRunner.class)
public class GenericConfigAttributeTest {
    private Document m_document;
    private String m_attributeLocalName;
    private String m_attributeNS;
    private String m_attributeValue;

    @Before
    public void setup() {
        m_document = DocumentUtils.createDocument();
        m_attributeLocalName = "conftpl:device-specific-data";
        m_attributeNS = "http://www.test-company.com/solutions/anv-configuration-templates";
        m_attributeValue = "<![CDATA[<if:interfaces xmlns:bbfift=\"urn:broadband-forum-org:yang:bbf-if-type\" xmlns:if=\"urn:ietf:params:xml:ns:yang:ietf-interfaces\">\n" +
                "\t\t<if:interface>\n" +
                "\t\t\t<if:name>xdsl-line:1/1/1/1</if:name>\n" +
                "\t\t\t<if:type>bbfift:xdsl</if:type>\n" +
                "\t\t</if:interface>\n" +
                "\t</if:interfaces>\n" +
                "    <swmgmt:software-management xmlns:swmgmt=\"urn:broadband-forum-org:yang:bbf-software-image-management\"/>\n" +
                "\t<syslog:syslog xmlns:syslog=\"urn:ietf:params:xml:ns:yang:ietf-syslog\"/>]]>";
    }

    @Test
    public void testGenericConfigAttributeWithCDATASection() throws Exception {
        GenericConfigAttribute testSubject = new GenericConfigAttribute(m_attributeLocalName, m_attributeNS, m_attributeValue);
        Assert.assertEquals(
                "<conftpl:device-specific-data xmlns:conftpl=\"http://www.test-company.com/solutions/anv-configuration-templates\">" +
                        "&lt;if:interfaces xmlns:bbfift=\"urn:broadband-forum-org:yang:bbf-if-type\" xmlns:if=\"urn:ietf:params:xml:ns:yang:ietf-interfaces\"&gt;\n" +
                        "\t\t&lt;if:interface&gt;\n" +
                        "\t\t\t&lt;if:name&gt;xdsl-line:1/1/1/1&lt;/if:name&gt;\n" +
                        "\t\t\t&lt;if:type&gt;bbfift:xdsl&lt;/if:type&gt;\n" +
                        "\t\t&lt;/if:interface&gt;\n" +
                        "\t&lt;/if:interfaces&gt;\n" +
                        "    &lt;swmgmt:software-management xmlns:swmgmt=\"urn:broadband-forum-org:yang:bbf-software-image-management\"/&gt;\n" +
                        "\t&lt;syslog:syslog xmlns:syslog=\"urn:ietf:params:xml:ns:yang:ietf-syslog\"/&gt;" +
                        "</conftpl:device-specific-data>\n",
                        DocumentUtils.documentToPrettyString(testSubject.getDOMValue()));

        Node dummyElement = m_document.importNode(testSubject.getDOMValue(),true);
        Assert.assertEquals(
                "<conftpl:device-specific-data xmlns:conftpl=\"http://www.test-company.com/solutions/anv-configuration-templates\">" +
                        "&lt;if:interfaces xmlns:bbfift=\"urn:broadband-forum-org:yang:bbf-if-type\" xmlns:if=\"urn:ietf:params:xml:ns:yang:ietf-interfaces\"&gt;\n" +
                        "\t\t&lt;if:interface&gt;\n" +
                        "\t\t\t&lt;if:name&gt;xdsl-line:1/1/1/1&lt;/if:name&gt;\n" +
                        "\t\t\t&lt;if:type&gt;bbfift:xdsl&lt;/if:type&gt;\n" +
                        "\t\t&lt;/if:interface&gt;\n" +
                        "\t&lt;/if:interfaces&gt;\n" +
                        "    &lt;swmgmt:software-management xmlns:swmgmt=\"urn:broadband-forum-org:yang:bbf-software-image-management\"/&gt;\n" +
                        "\t&lt;syslog:syslog xmlns:syslog=\"urn:ietf:params:xml:ns:yang:ietf-syslog\"/&gt;" +
                        "</conftpl:device-specific-data>\n",
                        DocumentUtils.documentToPrettyString(dummyElement));
    }
}
