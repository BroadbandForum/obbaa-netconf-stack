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

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.api.utils.FileUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.broadband_forum.obbaa.netconf.api.server.NetconfQueryParams;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.StateAttributeUtil;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;

public class StateAttributeUtilTest {

    String NAMESPACE = "http://www.test-company.com/solutions/package-manager";

    @Test
    public void testTrimResultBelowDepth() throws Exception {
        Map<ModelNodeId, List<Element>> input = getInput();
        StateAttributeUtil.trimResultBelowDepth(input, NetconfQueryParams.NO_PARAMS);
        TestUtil.assertXMLEquals(getXml(), input.get(getNodeId()).get(0));

        input = getInput();
        StateAttributeUtil.trimResultBelowDepth(input, new NetconfQueryParams(4, true)); //4-rdncount=1
        TestUtil.assertXMLEquals(getXmlWithDepth1(), input.get(getNodeId()).get(0));

        input = getInput();
        StateAttributeUtil.trimResultBelowDepth(input, new NetconfQueryParams(5, true));
        TestUtil.assertXMLEquals(getXmlWithDepth2(), input.get(getNodeId()).get(0));

        input = getInput();
        StateAttributeUtil.trimResultBelowDepth(input, new NetconfQueryParams(6, true));
        TestUtil.assertXMLEquals(getXmlWithDepth3(), input.get(getNodeId()).get(0));

        input = new HashMap<ModelNodeId, List<Element>>();
        input.put(getNodeId(), Arrays.asList(getInputXml()));
        StateAttributeUtil.trimResultBelowDepth(input, new NetconfQueryParams(4, true));
        TestUtil.assertXMLEquals(getOutputXml(), input.get(getNodeId()).get(0));
    }

    @Test
    public void testTrimResultBelowDepthForRootDepth() throws Exception {
        ModelNodeRdn pmaRdn = new ModelNodeRdn("container", "urn:org:bbf:pma", "pma");
        List<ModelNodeRdn> rdns = Arrays.asList(pmaRdn);
        ModelNodeId nodeId = new ModelNodeId(rdns);
        Map<ModelNodeId, List<Element>> input = new HashMap<ModelNodeId, List<Element>>();
        String data = "<pma:hierarchy1 xmlns:pma=\"http://test-company.com/pma\" name=\"one\">\n"
                + "</pma:hierarchy1>";
        input.put(nodeId, Arrays.asList(DocumentUtils.stringToDocument(data).getDocumentElement()));
        StateAttributeUtil.trimResultBelowDepth(input, new NetconfQueryParams(1, true));
        assertEquals(Collections.EMPTY_LIST, input.get(nodeId));
    }

    private Map<ModelNodeId, List<Element>> getInput() throws Exception {
        ModelNodeId nodeId = getNodeId();
        Map<ModelNodeId, List<Element>> input = new HashMap<ModelNodeId, List<Element>>();
        input.put(nodeId, Arrays.asList(getXml()));
        return input;
    }

    private ModelNodeId getNodeId() {

        ModelNodeRdn pmaRdn = new ModelNodeRdn("container", "urn:org:bbf:pma", "pma");
        ModelNodeRdn dhRdn = new ModelNodeRdn("container", "urn:org:bbf:pma", "device-holder");
        ModelNodeRdn dRdn = new ModelNodeRdn("container", "urn:org:bbf:pma", "device");
        List<ModelNodeRdn> rdns = Arrays.asList(pmaRdn, dhRdn, dRdn);
        return new ModelNodeId(rdns);
    }

    private Element getXml() throws Exception {
        String data = "<pma:hierarchy1 xmlns:pma=\"http://test-company.com/pma\" name=\"one\">\n"
                + "     <pma:name>test</pma:name>\n"
                + "     <pma:hierarchy2 name=\"two\">\n"
                + "         <pma:hierarchy3 name=\"three\">\n"
                + "          </pma:hierarchy3>\n"
                + "     </pma:hierarchy2>\n"
                + "</pma:hierarchy1>";
        return DocumentUtils.stringToDocument(data).getDocumentElement();
    }

    private Element getXmlWithDepth1() throws Exception {
        String data = "<pma:hierarchy1 xmlns:pma=\"http://test-company.com/pma\" name=\"one\">\n"
                + "</pma:hierarchy1>";
        return DocumentUtils.stringToDocument(data).getDocumentElement();
    }

    private Element getXmlWithDepth2() throws Exception {
        String data = "<pma:hierarchy1 xmlns:pma=\"http://test-company.com/pma\" name=\"one\">\n"
                + "     <pma:hierarchy2 name=\"two\"/>\n"
                + "    <pma:name>test</pma:name>\n"
                + "  </pma:hierarchy1>";
        return DocumentUtils.stringToDocument(data).getDocumentElement();
    }

    private Element getXmlWithDepth3() throws Exception {
        String data = "<pma:hierarchy1 xmlns:pma=\"http://test-company.com/pma\" name=\"one\">\n"
                + "     <pma:name>test</pma:name>\n"
                + "     <pma:hierarchy2 name=\"two\">\n"
                + "         <pma:hierarchy3 name=\"three\">\n"
                + "          </pma:hierarchy3>\n"
                + "     </pma:hierarchy2>\n"
                + "</pma:hierarchy1>";
        return DocumentUtils.stringToDocument(data).getDocumentElement();
    }

    private Element getInputXml() throws Exception {
        String data =
                "<adch:device-config-history xmlns:adch=\"http://www.test-company.com/solutions/anv-device-config-history\">"
                + "<adch:number-of-config-backups>3</adch:number-of-config-backups>"
                + "<adch:config-backup>"
                + "<adch:version>1</adch:version>"
                + "<adch:timestamp>2016-09-21T12:00:33+05:30</adch:timestamp>"
                + "<adch:config-interface-version>1.0</adch:config-interface-version>"
                + "<adch:username>admin</adch:username>"
                + "</adch:config-backup>"
                + "<adch:config-backup>"
                + "<adch:version>2</adch:version>"
                + "<adch:timestamp>2016-09-21T12:02:26+05:30</adch:timestamp>"
                + "<adch:config-interface-version>1.0</adch:config-interface-version>"
                + "<adch:username>admin</adch:username>"
                + "</adch:config-backup>"
                + "<adch:config-backup>"
                + "<adch:version>3</adch:version>"
                + "<adch:timestamp>2016-09-21T12:07:45+05:30</adch:timestamp>"
                + "<adch:config-interface-version>1.0</adch:config-interface-version>"
                + "<adch:username>admin</adch:username>"
                + "</adch:config-backup>"
                + "</adch:device-config-history>";
        return DocumentUtils.stringToDocument(data).getDocumentElement();
    }

    private Element getOutputXml() throws Exception {
        String data =
                "<adch:device-config-history xmlns:adch=\"http://www.test-company.com/solutions/anv-device-config-history\">"
                + "</adch:device-config-history>";
        return DocumentUtils.stringToDocument(data).getDocumentElement();
    }

    @Test
    public void testFilter1() throws Exception {
        Document input = DocumentUtils.stringToDocument(FileUtil.loadAsString("/filter/testFilter.xml"));
        Document expected = DocumentUtils.stringToDocument(FileUtil.loadAsString("/filter/response1.xml"));
        Document doc = DocumentUtils.createDocument();
        FilterNode filterNode = new FilterNode("current-configration", NAMESPACE);
        filterNode.addSelectNode("feature", NAMESPACE);
        Element element = StateAttributeUtil.applyFilter(input.getDocumentElement(), filterNode, doc);
        TestUtil.assertXMLEquals(expected.getDocumentElement(), element);
    }

    @Test
    public void testFilter2() throws Exception {
        Document input = DocumentUtils.stringToDocument(FileUtil.loadAsString("/filter/testFilter.xml"));
        Document expected = DocumentUtils.stringToDocument(FileUtil.loadAsString("/filter/response2.xml"));
        Document doc = DocumentUtils.createDocument();
        FilterNode filterNode = new FilterNode("current-configuration", NAMESPACE);
        FilterNode filterNode1 = new FilterNode("feature", NAMESPACE);
        filterNode1.addMatchNode("required", NAMESPACE, "false");
        filterNode.addContainmentNode(filterNode1);
        Element element = StateAttributeUtil.applyFilter(input.getDocumentElement(), filterNode, doc);
        TestUtil.assertXMLEquals(expected.getDocumentElement(), element);
    }

    @Test
    public void testFilter3() throws Exception {
        Document input = DocumentUtils.stringToDocument(FileUtil.loadAsString("/filter/testFilter.xml"));
        Document expected = DocumentUtils.stringToDocument(FileUtil.loadAsString("/filter/response3.xml"));
        Document doc = DocumentUtils.createDocument();
        FilterNode filterNode = new FilterNode("repositories", NAMESPACE);
        filterNode.addMatchNode("repository", NAMESPACE, "mvn:org.bbf/feature/1.0.0/xml/features");
        Element element = StateAttributeUtil.applyFilter(input.getDocumentElement(), filterNode, doc);
        TestUtil.assertXMLEquals(expected.getDocumentElement(), element);
    }
}
