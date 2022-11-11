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

package org.broadband_forum.obbaa.netconf.api.util;

import static junit.framework.TestCase.fail;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class DocumentBuilderFactoryWithoutDTDTest {

    private final String m_externalDTDXMLDocument = "<?xml version=\"1.0\"?>\n" +
            "<!DOCTYPE message SYSTEM \"message.dtd\">\n" +
            "<message>\n" +
            "Let the good times roll!\n" +
            "</message>";

    private final String m_internalDTDXMLDocument = "<?xml version=\"1.0\"?>\n" +
            "<!DOCTYPE message [\n" +
            "<!ELEMENT message (#PCDATA)>\n" +
            "]>\n" +
            "<message>\n" +
            "Let the good times roll!\n" +
            "</message>";

    private final String m_internalDTDXMLDocumentWithExpandingReferences = "<!DOCTYPE lolz [\n" +
            "<!ENTITY lol \"lol\" >\n" +
            "<!ENTITY lol1 \"&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;\">\n" +
            "<!ENTITY lol2 \"&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;\">\n" +
            "<!ENTITY lol3 \"&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;\">\n" +
            "<!ENTITY lol4 \"&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;\">]><license:license-key xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" xmlns:license=\"http://www.test-company.com/solutions/license-management\"><license:license-name>test&lol4;</license:license-name><license:key-string>AC4wLAIUBASWkSspEsH+sdMzsWU5rhi</license:key-string></license:license-key>";

    private final String m_predefinedEntityInXMLDocument = "<license:license-key xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" xmlns:license=\"http://www.test-company.com/solutions/license-management\"><license:license-name>test&gt;&lt;&amp;&apos;&quot;</license:license-name><license:key-string>AC4wLAIUBASWkSspEsH+sdMzsWU5rh</license:key-string></license:license-key>";


    private final String m_plainXMLDocumentWithNoDTD = "<license:license-key xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" xmlns:license=\"http://www.test-company.com/solutions/license-management\"><license:license-name>test;</license:license-name><license:key-string>AC4wLAIUBASWkSspEsH+sdMzsWU5rh</license:key-string></license:license-key>";



    @Test
    public void testDocumentWithExternalDTDThrowsError(){
        try{
            parseXML(m_externalDTDXMLDocument);
            fail();
        }catch (Exception e){
            Assert.assertTrue(e instanceof SAXParseException);
            Assert.assertEquals("DOCTYPE is disallowed when the feature \"http://apache.org/xml/features/disallow-doctype-decl\" set to true.", e.getMessage());
        }
    }

    @Test
    public void testDocumentWithInternalDTDThrowsError(){
        try{
            parseXML(m_internalDTDXMLDocument);
            fail();
        }catch (Exception e){
            Assert.assertTrue(e instanceof SAXParseException);
            Assert.assertEquals("DOCTYPE is disallowed when the feature \"http://apache.org/xml/features/disallow-doctype-decl\" set to true.", e.getMessage());
        }
    }

    @Test
    public void testDocumentWithInternalDTDAndExpandingReferencesThrowsError(){
        try{
            parseXML(m_internalDTDXMLDocumentWithExpandingReferences);
            fail();
        }catch (Exception e){
            Assert.assertTrue(e instanceof SAXParseException);
            Assert.assertEquals("DOCTYPE is disallowed when the feature \"http://apache.org/xml/features/disallow-doctype-decl\" set to true.", e.getMessage());
        }
    }

    @Test
    public void testDocumentWithPredefinedEntityDoesNotThrowError() throws IOException, SAXException, ParserConfigurationException {
        Document document = parseXML(m_predefinedEntityInXMLDocument);
        Assert.assertEquals("test><&'\"AC4wLAIUBASWkSspEsH+sdMzsWU5rh", document.getDocumentElement().getTextContent());
    }

    @Test
    public void testDocumentWithPlainXMLDoesNotThrowError() throws IOException, SAXException, ParserConfigurationException {
        parseXML(m_plainXMLDocumentWithNoDTD);
    }

    private Document parseXML(String xml) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory documentBuilderFactoryWithoutDTD = DocumentBuilderFactoryWithoutDTD.newInstance();
        Document document = documentBuilderFactoryWithoutDTD.newDocumentBuilder().parse(new InputSource(new StringReader(xml.trim())));
        return document;
    }
}
