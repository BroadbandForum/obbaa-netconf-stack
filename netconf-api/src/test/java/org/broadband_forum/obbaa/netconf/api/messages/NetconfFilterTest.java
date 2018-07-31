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

import static junit.framework.TestCase.assertTrue;

import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceConstants;
import org.custommonkey.xmlunit.DifferenceListener;
import org.custommonkey.xmlunit.ElementNameAndTextQualifier;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class NetconfFilterTest {

    @Test
    public void testGetXmlFilter() throws Exception {
        XMLUnit.setIgnoreAttributeOrder(true);
        XMLUnit.setNormalizeWhitespace(true);
        String filterStr = "<nc:filter xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "    <world xmlns=\"http://test-company.test/country-universe\"/>" +
                "    <ut:universe xmlns:ut=\"http://test-company.test/country-universe\">\n" +
                "        <ut:name>universe 1</ut:name>\n" +
                "        <galaxy xmlns=\"http://test-company.test/country-universe\">\n" +
                "            <name>Milky way</name>\n" +
                "            <pet-name>My home galaxy</pet-name>\n" +
                "            <planetary-system/>\n" +
                "        </galaxy>\n" +
                "    </ut:universe>\n" +
                "</nc:filter>";
        String getStr = "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\"><get>" + filterStr +
                "</get></rpc>";
        Document document = DocumentUtils.stringToDocument(getStr);
        NetconfFilter filter = DocumentToPojoTransformer.getFilterFromRpcDocument(document);
        Diff diff = new Diff(filterStr, DocumentUtils.documentToPrettyString(filter.getXmlFilter()));
        ignoreTextNodes(diff);
        assertTrue("XMLs are different :" + diff.toString(), diff.similar());
    }

    private void ignoreTextNodes(Diff diff) {
        diff.overrideElementQualifier(new ElementNameAndTextQualifier());
        diff.overrideDifferenceListener(new DifferenceListener() {
            @Override
            public void skippedComparison(Node arg0, Node arg1) {

            }

            @Override
            public int differenceFound(Difference difference) {
                if (DifferenceConstants.CHILD_NODELIST_SEQUENCE.equals(difference)) {
                    return RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
                }
                if (DifferenceConstants.NAMESPACE_PREFIX.equals(difference)) {
                    return RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
                }
                if (difference.getTestNodeDetail().getNode().getNodeType() == Node.TEXT_NODE && difference
                        .getTestNodeDetail().getValue().startsWith("$") && difference.getTestNodeDetail().getNode()
                        .getParentNode().getNodeName().equalsIgnoreCase("pma:password")) {
                    return RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
                } else if (difference.getTestNodeDetail().getNode().getParentNode().getNodeName().equalsIgnoreCase
                        ("pma:reachable-last-change")) {
                    return RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
                }
                return RETURN_ACCEPT_DIFFERENCE;
            }
        });
    }
}
