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

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import java.util.Arrays;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterUtil;
import org.junit.Test;

/**
 * Created by vgotagi on 5/8/18.
 */
public class FilterNodeTest {
    @Test
    public void testIsSameTypeReturnsTrueForSameNodeTypes() throws Exception {
        FilterNode node1 = new FilterNode();
        FilterUtil.processFilter(node1, Arrays.asList(DocumentUtils.stringToDocument(
                "<list-one xmlns=\"http://example.com/ns/example-filter\">\n" +
                "    <list-one-key xmlns:pf=\"ns\">pf:list-one-key1</list-one-key>\n" +
                "</list-one>" ).getDocumentElement()));
        FilterNode node2 = new FilterNode();
        FilterUtil.processFilter(node2, Arrays.asList(DocumentUtils.stringToDocument(
                "<list-one xmlns=\"http://example.com/ns/example-filter\">\n" +
                        "    <list-one-key xmlns:pf=\"ns\">pf:list-one-key2</list-one-key>\n" +
                        "</list-one>" ).getDocumentElement()));
        assertTrue(node1.getChildNodes().get(0).isSameType(node2.getChildNodes().get(0)));
    }

    @Test
    public void testIsSameTypeReturnsFalseForNodeTypesWithDiffNS() throws Exception {
        FilterNode node1 = new FilterNode();
        FilterUtil.processFilter(node1, Arrays.asList(DocumentUtils.stringToDocument(
                "<list-one xmlns=\"http://example.com/ns/example-filter\">\n" +
                        "    <list-one-key xmlns:pf=\"ns\">pf:list-one-key1</list-one-key>\n" +
                        "</list-one>" ).getDocumentElement()));
        FilterNode node2 = new FilterNode();
        FilterUtil.processFilter(node2, Arrays.asList(DocumentUtils.stringToDocument(
                "<list-one xmlns=\"http://example.com/ns/example-filter2\">\n" +
                        "    <list-one-key xmlns:pf=\"ns\">pf:list-one-key2</list-one-key>\n" +
                        "</list-one>" ).getDocumentElement()));
        assertFalse(node1.getChildNodes().get(0).isSameType(node2.getChildNodes().get(0)));
    }

    @Test
    public void testIsSameTypeReturnsFalseForNodeTypesWithDiffNodeName() throws Exception {
        FilterNode node1 = new FilterNode();
        FilterUtil.processFilter(node1, Arrays.asList(DocumentUtils.stringToDocument(
                "<list-one xmlns=\"http://example.com/ns/example-filter\">\n" +
                        "    <list-one-key xmlns:pf=\"ns\">pf:list-one-key1</list-one-key>\n" +
                        "</list-one>" ).getDocumentElement()));
        FilterNode node2 = new FilterNode();
        FilterUtil.processFilter(node2, Arrays.asList(DocumentUtils.stringToDocument(
                "<list-one2 xmlns=\"http://example.com/ns/example-filter\">\n" +
                        "    <list-one-key xmlns:pf=\"ns\">pf:list-one-key2</list-one-key>\n" +
                        "</list-one2>" ).getDocumentElement()));
        assertFalse(node1.getChildNodes().get(0).isSameType(node2.getChildNodes().get(0)));
    }

    @Test
    public void testNodesCanBeMergedWhenMatchNodesSame() throws Exception{
        FilterNode node1 = new FilterNode();
        FilterUtil.processFilter(node1, Arrays.asList(DocumentUtils.stringToDocument(
                "<list-one xmlns=\"http://example.com/ns/example-filter\">\n" +
                        "    <list-one-key xmlns:pf=\"ns\">pf:list-one-key1</list-one-key>\n" +
                        "</list-one>" ).getDocumentElement()));
        FilterNode node2 = new FilterNode();
        FilterUtil.processFilter(node2, Arrays.asList(DocumentUtils.stringToDocument(
                "<list-one xmlns=\"http://example.com/ns/example-filter\">\n" +
                        "    <list-one-key xmlns:pf=\"ns\">pf:list-one-key1</list-one-key>\n" +
                        "</list-one>" ).getDocumentElement()));
        assertTrue(node1.getChildNodes().get(0).canBeMerged(node2.getChildNodes().get(0)));
    }

    @Test
    public void testNodesCannotBeMergedWhenMatchNodesDifferent() throws Exception{
        FilterNode node1 = new FilterNode();
        FilterUtil.processFilter(node1, Arrays.asList(DocumentUtils.stringToDocument(
                "<list-one xmlns=\"http://example.com/ns/example-filter\">\n" +
                        "    <list-one-key xmlns:pf=\"ns\">pf:list-one-key1</list-one-key>\n" +
                        "</list-one>" ).getDocumentElement()));
        FilterNode node2 = new FilterNode();
        FilterUtil.processFilter(node2, Arrays.asList(DocumentUtils.stringToDocument(
                "<list-one xmlns=\"http://example.com/ns/example-filter\">\n" +
                        "    <list-one-key xmlns:pf=\"ns\">pf:list-one-key2</list-one-key>\n" +
                        "</list-one>" ).getDocumentElement()));
        assertFalse(node1.getChildNodes().get(0).canBeMerged(node2.getChildNodes().get(0)));
    }

    @Test
    public void testNodesCannotBeMergedWhenMatchNodesDifferentInNumber() throws Exception{
        FilterNode node1 = new FilterNode();
        FilterUtil.processFilter(node1, Arrays.asList(DocumentUtils.stringToDocument(
                "<list-one xmlns=\"http://example.com/ns/example-filter\">\n" +
                        "    <list-one-key xmlns:pf=\"ns\">pf:list-one-key1</list-one-key>\n" +
                        "</list-one>" ).getDocumentElement()));
        FilterNode node2 = new FilterNode();
        FilterUtil.processFilter(node2, Arrays.asList(DocumentUtils.stringToDocument(
                "<list-one xmlns=\"http://example.com/ns/example-filter\">\n" +
                        "    <list-one-key xmlns:pf=\"ns\">pf:list-one-key1</list-one-key>\n" +
                        "    <list-one-key2 xmlns:pf=\"ns\">pf:list-one-key1</list-one-key2>\n" +
                        "</list-one>" ).getDocumentElement()));
        assertFalse(node1.getChildNodes().get(0).canBeMerged(node2.getChildNodes().get(0)));
    }

    @Test
    public void testNodesCannotBeMergedWhenNodeIsOFDiffType() throws Exception{
        FilterNode node1 = new FilterNode();
        FilterUtil.processFilter(node1, Arrays.asList(DocumentUtils.stringToDocument(
                "<list-one xmlns=\"http://example.com/ns/example-filter\">\n" +
                        "    <list-one-key xmlns:pf=\"ns\">pf:list-one-key1</list-one-key>\n" +
                        "</list-one>" ).getDocumentElement()));
        FilterNode node2 = new FilterNode();
        FilterUtil.processFilter(node2, Arrays.asList(DocumentUtils.stringToDocument(
                "<list-one2 xmlns=\"http://example.com/ns/example-filter\">\n" +
                        "    <list-one-key xmlns:pf=\"ns\">pf:list-one-key1</list-one-key>\n" +
                        "</list-one2>" ).getDocumentElement()));
        assertFalse(node1.getChildNodes().get(0).canBeMerged(node2.getChildNodes().get(0)));
    }

    @Test
    public void testNodesGetMergedWhenNodesAreExactlySame() throws Exception {
        FilterNode node1 = new FilterNode();
        FilterUtil.processFilter(node1, Arrays.asList(DocumentUtils.stringToDocument(
                "<list-one xmlns=\"http://example.com/ns/example-filter\">\n" +
                        "    <list-one-key xmlns:pf=\"ns\">pf:list-one-key1</list-one-key>\n" +
                        "</list-one>" ).getDocumentElement()));
        FilterNode node2 = new FilterNode();
        FilterUtil.processFilter(node2, Arrays.asList(DocumentUtils.stringToDocument(
                "<list-one xmlns=\"http://example.com/ns/example-filter\">\n" +
                        "    <list-one-key xmlns:pf=\"ns\">pf:list-one-key1</list-one-key>\n" +
                        "</list-one>" ).getDocumentElement()));

        FilterNode firstChildNode = node1.getChildNodes().get(0);
        FilterNode secondChildNode = node2.getChildNodes().get(0);
        firstChildNode.merge(secondChildNode);
        FilterNode expectedNode = new FilterNode();
        FilterUtil.processFilter(expectedNode, Arrays.asList(DocumentUtils.stringToDocument(
                "<list-one xmlns=\"http://example.com/ns/example-filter\">\n" +
                        "    <list-one-key xmlns:pf=\"ns\">pf:list-one-key1</list-one-key>\n" +
                        "</list-one>" ).getDocumentElement()));
        expectedNode = expectedNode.getChildNodes().get(0);
        assertEquals(expectedNode, firstChildNode);
    }

    @Test
    public void testNodesGetMergedWhenNodesHaveSelectNode() throws Exception {
        FilterNode node1 = new FilterNode();
        FilterUtil.processFilter(node1, Arrays.asList(DocumentUtils.stringToDocument(
                "<list-one xmlns=\"http://example.com/ns/example-filter\">\n" +
                        "    <list-one-key xmlns:pf=\"ns\">pf:list-one-key1</list-one-key>\n" +
                        "</list-one>" ).getDocumentElement()));
        FilterNode node2 = new FilterNode();
        FilterUtil.processFilter(node2, Arrays.asList(DocumentUtils.stringToDocument(
                "<list-one xmlns=\"http://example.com/ns/example-filter\">\n" +
                        "    <list-one-key xmlns:pf=\"ns\">pf:list-one-key1</list-one-key>\n" +
                        "    <selectNode/>\n" +
                        "</list-one>" ).getDocumentElement()));

        FilterNode firstChildNode = node1.getChildNodes().get(0);
        FilterNode secondChildNode = node2.getChildNodes().get(0);
        firstChildNode.merge(secondChildNode);
        FilterNode expectedNode = new FilterNode();
        FilterUtil.processFilter(expectedNode, Arrays.asList(DocumentUtils.stringToDocument(
                "<list-one xmlns=\"http://example.com/ns/example-filter\">\n" +
                        "    <list-one-key xmlns:pf=\"ns\">pf:list-one-key1</list-one-key>\n" +
                        "    <selectNode/>\n" +
                        "</list-one>" ).getDocumentElement()));
        expectedNode = expectedNode.getChildNodes().get(0);
        assertEquals(expectedNode, firstChildNode);
    }

    @Test
    public void testNodesGetMergedWhenNodesHaveChildNodes() throws Exception {
        FilterNode node1 = new FilterNode();
        FilterUtil.processFilter(node1, Arrays.asList(DocumentUtils.stringToDocument(
                "<list-one xmlns=\"http://example.com/ns/example-filter\">\n" +
                        "            <list-one-key xmlns:pf=\"ns\">pf:list-one-key1</list-one-key>\n" +
                        "            <inner-list-one2 xmlns=\"http://example.com/ns/example-filter\">\n" +
                        "                <inner-list-one-key xmlns:pf=\"ns\">pf:inner-list-one</inner-list-one-key>\n" +
                        "                <inner-list-one-leaf>inner-list-one</inner-list-one-leaf>\n" +
                        "            </inner-list-one2>\n" +
                        "        </list-one>" ).getDocumentElement()));
        FilterNode node2 = new FilterNode();
        FilterUtil.processFilter(node2, Arrays.asList(DocumentUtils.stringToDocument(
                "<list-one xmlns=\"http://example.com/ns/example-filter\">\n" +
                        "            <list-one-key xmlns:pf=\"ns\">pf:list-one-key1</list-one-key>\n" +
                        "            <inner-list-one xmlns=\"http://example.com/ns/example-filter\">\n" +
                        "                <inner-list-one-key xmlns:pf=\"ns\">pf:inner-list-one</inner-list-one-key>\n" +
                        "                <inner-list-one-leaf>inner-list-one</inner-list-one-leaf>\n" +
                        "            </inner-list-one>\n" +
                        "        </list-one>" ).getDocumentElement()));

        FilterNode firstChildNode = node1.getChildNodes().get(0);
        FilterNode secondChildNode = node2.getChildNodes().get(0);
        firstChildNode.merge(secondChildNode);
        FilterNode expectedNode = new FilterNode();
        FilterUtil.processFilter(expectedNode, Arrays.asList(DocumentUtils.stringToDocument(
                "<list-one xmlns=\"http://example.com/ns/example-filter\">\n" +
                        "            <list-one-key xmlns:pf=\"ns\">pf:list-one-key1</list-one-key>\n" +
                        "            <inner-list-one2 xmlns=\"http://example.com/ns/example-filter\">\n" +
                        "                <inner-list-one-key xmlns:pf=\"ns\">pf:inner-list-one</inner-list-one-key>\n" +
                        "                <inner-list-one-leaf>inner-list-one</inner-list-one-leaf>\n" +
                        "            </inner-list-one2>\n" +
                        "            <inner-list-one xmlns=\"http://example.com/ns/example-filter\">\n" +
                        "                <inner-list-one-key xmlns:pf=\"ns\">pf:inner-list-one</inner-list-one-key>\n" +
                        "                <inner-list-one-leaf>inner-list-one</inner-list-one-leaf>\n" +
                        "            </inner-list-one>\n" +
                        "        </list-one>" ).getDocumentElement()));
        expectedNode = expectedNode.getChildNodes().get(0);
        assertEquals(expectedNode, firstChildNode);
    }

}
