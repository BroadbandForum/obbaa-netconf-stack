package org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util;

import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.junit.Test;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

import static org.junit.Assert.assertEquals;


public class NamespaceUtilTest {

    @Test
    public void testGetAttributeNameSpace() throws ParserConfigurationException, SAXException, IOException {

        Element parentNode = DocumentUtils.getDocumentElement("<p1:parent xmlns:p1=\"parentNs\">\n" +
                "<p1:childcontainer>\n" +
                "<p1:childlist xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" nc:operation=\"create\">\n" +
                "<p1:key>melt321</p1:key>\n" +
                "<p1:leaf>p1:value</p1:leaf>\n" +
                "</p1:childlist>\n" +
                "</p1:childcontainer>\n" +
                "</p1:parent>");

        Element childElement = DocumentUtils.getChildElement(parentNode, "p1:leaf");
        assertEquals("parentNs", NamespaceUtil.getAttributeNameSpace(childElement,"p1"));
    }
}
