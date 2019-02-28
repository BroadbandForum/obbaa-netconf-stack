package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util;

import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterMatchNode;
import org.junit.Test;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FilterNodeUtilTest {

    @Test
    public void testAddMatchNodeWithAttributes() throws NetconfMessageBuilderException {
        String matchNodeString = "<type xmlns:ianaift=\"test-namespace\">ianaift:fastdsl</type>";
        Element matchNode = DocumentUtils.stringToDocument(matchNodeString).getDocumentElement();

        FilterMatchNode filterMatchNode = new FilterMatchNode("type","ns","ianaift:fastdsl");
        FilterNodeUtil.xmlElementToFilterNode(filterMatchNode, matchNode);

        Map<String, String> attributes = filterMatchNode.getAttributes();
        assertEquals(1, attributes.size());
        assertTrue(attributes.containsKey("xmlns:ianaift"));
        assertTrue(attributes.containsValue("test-namespace"));

        matchNodeString = "<type xmlns:ianaift=\"test-namespace\" xmlns:if=\"test-namespace2\">ianaift:fastdsl</type>";
        matchNode = DocumentUtils.stringToDocument(matchNodeString).getDocumentElement();

        filterMatchNode = new FilterMatchNode("type","ns","ianaift:fastdsl");
        FilterNodeUtil.xmlElementToFilterNode(filterMatchNode,matchNode);

        attributes = filterMatchNode.getAttributes();
        assertEquals(2, attributes.size());
        Map<String, String> expectedAttributes = new HashMap<>();
        expectedAttributes.put("xmlns:ianaift","test-namespace");
        expectedAttributes.put("xmlns:if","test-namespace2");
    }

    @Test
    public void testAddMatchNodeWithoutAttributes() throws NetconfMessageBuilderException {
        String matchNodeString = "<type>ianaift:fastdsl</type>";
        Element matchNodeElement = DocumentUtils.stringToDocument(matchNodeString).getDocumentElement();

        FilterMatchNode filterMatchNode = new FilterMatchNode("type","ns", "ianaift:fastdsl");
        FilterNodeUtil.xmlElementToFilterNode(filterMatchNode,matchNodeElement);

        assertTrue(filterMatchNode.getAttributes().isEmpty());
    }

    @Test
    public void testFilterNodeToXmlElement() throws NetconfMessageBuilderException {
        String matchNodeString = "<type>ianaift:fastdsl</type>";
        Element matchNodeElement = DocumentUtils.stringToDocument(matchNodeString).getDocumentElement();

        FilterMatchNode filterMatchNode = new FilterMatchNode("type","ns", "ianaift:fastdsl");
        filterMatchNode.addAttribute("xmlns:ianaift","test-namespace");
        filterMatchNode.addAttribute("xmlns:operation","testing");

        FilterNodeUtil.filterNodeToXmlElement(filterMatchNode,matchNodeElement);

        String expectedElement = "<type xmlns:ianaift=\"test-namespace\" xmlns:operation=\"testing\">ianaift:fastdsl</type>";
        assertEquals(expectedElement, DocumentUtils.documentToString(matchNodeElement));

    }
}
