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

package org.broadband_forum.obbaa.netconf.server.util;

import java.io.IOException;

import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

@RunWith(MockitoJUnitRunner.class)
public class TestUtilTest {

    /**
     * This UT fails with Saxon 9.6
     * @throws SAXException
     * @throws IOException
     */
    @Test(expected = AssertionError.class)
    public final void testAssertXMLEqualsFailsWhenXMLNotEqual() throws SAXException, IOException {
        String xmlA = "<a>BBF2</a>";
        String xmlB = "<b>BBF</b>";
        Element elementA = DocumentUtils.loadXmlDocument(new java.io.ByteArrayInputStream(xmlA.getBytes())).getDocumentElement();
        Element elementB = DocumentUtils.loadXmlDocument(new java.io.ByteArrayInputStream(xmlB.getBytes())).getDocumentElement();

        org.broadband_forum.obbaa.netconf.server.util.TestUtil.assertXMLEquals(elementA, elementB);
    }
    
    /**
     * This UT fails with xalan
     * @throws SAXException
     * @throws IOException
     */
    @Test
    public final void testAssertXMLEqualsWhenCommentsInEmptyElement() throws SAXException, IOException {
        String xmlA = "<foo><!-- test --><bar a=\"b\"/> </foo>";
        String xmlB = "<foo><bar a=\"b\"><!-- test --></bar> </foo>";
        Element elementA = DocumentUtils.loadXmlDocument(new java.io.ByteArrayInputStream(xmlA.getBytes())).getDocumentElement();
        Element elementB = DocumentUtils.loadXmlDocument(new java.io.ByteArrayInputStream(xmlB.getBytes())).getDocumentElement();

        org.broadband_forum.obbaa.netconf.server.util.TestUtil.assertXMLEquals(elementA, elementB);
    }
    
    @Test
    public final void testAssertXMLEqualsWhenCommentsPresent() throws SAXException, IOException {
        String xmlA = "<foo><!-- test --><bar a=\"b\"/> </foo>";
        String xmlB = "<foo><bar a=\"b\"/><!-- test --> </foo>";
        Element elementA = DocumentUtils.loadXmlDocument(new java.io.ByteArrayInputStream(xmlA.getBytes())).getDocumentElement();
        Element elementB = DocumentUtils.loadXmlDocument(new java.io.ByteArrayInputStream(xmlB.getBytes())).getDocumentElement();

        org.broadband_forum.obbaa.netconf.server.util.TestUtil.assertXMLEquals(elementA, elementB);
    }  
    
    @Test
    public final void testAssertXMLEqualsWhenWhiteSpacePresent() throws SAXException, IOException {
        String xmlA = "<foo>a = b;</foo>";
        String xmlB = "<foo>\r\n\ta =\tb; \r\n</foo>";
        Element elementA = DocumentUtils.loadXmlDocument(new java.io.ByteArrayInputStream(xmlA.getBytes())).getDocumentElement();
        Element elementB = DocumentUtils.loadXmlDocument(new java.io.ByteArrayInputStream(xmlB.getBytes())).getDocumentElement();

        org.broadband_forum.obbaa.netconf.server.util.TestUtil.assertXMLEquals(elementA, elementB);
    }    
    
    @Test
    public final void testAssertXMLEqualsWhenChildElementsAreDifferentlyOrdered() throws SAXException, IOException {
        String xmlA = "<foo><a/><b/></foo>";
        String xmlB = "<foo><b/><a/></foo>";
        Element elementA = DocumentUtils.loadXmlDocument(new java.io.ByteArrayInputStream(xmlA.getBytes())).getDocumentElement();
        Element elementB = DocumentUtils.loadXmlDocument(new java.io.ByteArrayInputStream(xmlB.getBytes())).getDocumentElement();

        org.broadband_forum.obbaa.netconf.server.util.TestUtil.assertXMLEquals(elementA, elementB);
    }
    
    /** 
     * This UT has failed with a NullPointerException in assertXMLEquals
     * @throws SAXException
     * @throws IOException
     */
    @Test(expected = AssertionError.class)
    public final void testAssertXMLEqualsWhenAttributeValuesAreDifferent() throws SAXException, IOException {
        String xmlA = "<foo><bar test=\"foo\"/></foo>";
        String xmlB = "<foo><bar test=\"bar\"/></foo>";
        Element elementA = DocumentUtils.loadXmlDocument(new java.io.ByteArrayInputStream(xmlA.getBytes())).getDocumentElement();
        Element elementB = DocumentUtils.loadXmlDocument(new java.io.ByteArrayInputStream(xmlB.getBytes())).getDocumentElement();

        org.broadband_forum.obbaa.netconf.server.util.TestUtil.assertXMLEquals(elementA, elementB);
    } 
    
    @Test(expected = AssertionError.class)
    public final void testAssertXMLEqualsWhenAttributeNamesAreDifferent() throws SAXException, IOException {
        String xmlA = "<foo><bar test=\"foo\"/></foo>";
        String xmlB = "<foo><bar testdifferent=\"foo\"/></foo>";
        Element elementA = DocumentUtils.loadXmlDocument(new java.io.ByteArrayInputStream(xmlA.getBytes())).getDocumentElement();
        Element elementB = DocumentUtils.loadXmlDocument(new java.io.ByteArrayInputStream(xmlB.getBytes())).getDocumentElement();

        org.broadband_forum.obbaa.netconf.server.util.TestUtil.assertXMLEquals(elementA, elementB);
    } 

    @Test
    public final void testXmlUnitUsesSaxon() {
        org.junit.Assert.assertEquals("class net.sf.saxon.TransformerFactoryImpl", XMLUnit.getTransformerFactory().getClass().toString());
    }
    
    @Test
    public final void testAssertXMLEqualsIgnoresNamespaceWithDifferentName() throws SAXException, IOException {
            String xmlA = "<match-criteria xmlns=\"urn:bbf:yang:bbf-sub-interface-tagging\""
                    + " xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\""
                    + " ns0:operation=\"merge\">"
                    + "   <untagged ns0:operation=\"delete\"/>"
                    + "</match-criteria>";
            String xmlB = "<match-criteria xmlns=\"urn:bbf:yang:bbf-sub-interface-tagging\""
                    + " xmlns:ns1=\"urn:ietf:params:xml:ns:netconf:base:1.0\""
                    + " ns1:operation=\"merge\">"
                    + "   <untagged ns1:operation=\"delete\"/>"
                    + "</match-criteria>";
            Element elementA = DocumentUtils.loadXmlDocument(new java.io.ByteArrayInputStream(xmlA.getBytes())).getDocumentElement();
            Element elementB = DocumentUtils.loadXmlDocument(new java.io.ByteArrayInputStream(xmlB.getBytes())).getDocumentElement();

            org.broadband_forum.obbaa.netconf.server.util.TestUtil.assertXMLEquals(elementA, elementB);
    }
}