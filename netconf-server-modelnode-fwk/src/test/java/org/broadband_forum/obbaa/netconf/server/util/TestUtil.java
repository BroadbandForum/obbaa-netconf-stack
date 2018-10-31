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

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.jukebox2.Album;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.jukebox2.AlbumImage;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.jukebox2.Artist;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.jukebox2.Jukebox;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.jukebox2.Library;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeFactoryException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceConstants;
import org.custommonkey.xmlunit.DifferenceListener;
import org.custommonkey.xmlunit.ElementNameAndTextQualifier;
import org.custommonkey.xmlunit.XMLUnit;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.AbstractNetconfRequest;
import org.broadband_forum.obbaa.netconf.api.messages.CopyConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigElement;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigErrorOptions;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigTestOptions;
import org.broadband_forum.obbaa.netconf.api.messages.GetConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfFilter;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcResponse;
import org.broadband_forum.obbaa.netconf.api.parser.YangParserUtil;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerMessageListener;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.utils.FileUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NetConfServerImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NetconfServer;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.jukebox2.Song;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.AbstractModelNodeFactory;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeRegistrar;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.LoggerFactory;

/**
 * Various test utilities
 *
 * @author steven
 */
public class TestUtil {
    private static final AdvancedLogger LOGGER = LoggerFactory.getLogger(TestUtil.class, "netconf-server-datastore",
            "DEBUG", "GLOBAL");
    private static DocumentBuilder m_builder;
    private static Transformer m_transformer;

    //make it ignore by default
    static {
        XMLUnit.setIgnoreComments(true);
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            m_builder = dbf.newDocumentBuilder();
            m_transformer = TransformerFactory.newInstance().newTransformer();
        } catch (Exception e) {
            LOGGER.error("Could not initialize TestUtils", e);
        }
    }

    /**
     * Call this method with 'true' to compare comments as well, by default XMLUnit is set to ignore comments by this
     * util.
     *
     * @param ignore
     */
    public void setIgnoreComments(boolean ignore) {
        XMLUnit.setIgnoreComments(ignore);
    }

    public static Jukebox createJukeBoxModel(ModelNodeHelperRegistry modelNodeHelperRegistry, SubSystemRegistry
            subSystemRegistry,
                                             SchemaRegistry schemaRegistry) {
        registerClasses(modelNodeHelperRegistry);
        Jukebox jukebox = null;
        try {
            jukebox = new Jukebox(null, new ModelNodeId(), modelNodeHelperRegistry, subSystemRegistry, schemaRegistry);
            jukebox.setLibrary(new Library(jukebox, jukebox.getModelNodeId(), modelNodeHelperRegistry,
                    subSystemRegistry, schemaRegistry));
            Artist lenny;
            lenny = jukebox.getLibrary().addArtist("Lenny");

            lenny.addAlbum("Greatest hits").addSong("Are you gonne go my way").addSong("Fly Away");
            lenny.addAlbum("Circus").addSong("Rock and roll is dead").addSong("Circus").addSong("Beyond the 7th Sky");
        } catch (EditException e) {
            LOGGER.error("Error while creating model", e);
        }
        return jukebox;
    }

    public static Jukebox createEmptyJukeBox(ModelNodeHelperRegistry modelNodeHelperRegistry, SubSystemRegistry
            subSystemRegistry, SchemaRegistry schemaRegistry) {
        registerClasses(modelNodeHelperRegistry);
        Jukebox jukebox = new Jukebox(null, new ModelNodeId(), modelNodeHelperRegistry, subSystemRegistry,
                schemaRegistry);
        return jukebox;
    }

    public static Jukebox createJukeBoxModelWithYear(ModelNodeHelperRegistry modelNodeHelperRegistry,
                                                     SubSystemRegistry subSystemRegistry,
                                                     SchemaRegistry schemaRegistry) {
        registerClasses(modelNodeHelperRegistry);
        Jukebox jukebox = new Jukebox(null, new ModelNodeId(), modelNodeHelperRegistry, subSystemRegistry,
                schemaRegistry);

        try {
            jukebox.setLibrary(new Library(jukebox, new ModelNodeId(), modelNodeHelperRegistry, subSystemRegistry,
                    schemaRegistry));
            Artist lenny = jukebox.getLibrary().addArtist("Lenny");
            lenny.addAlbum("Greatest hits").setYear(2000).addSong("Are you gonne go my way").addSong("Fly Away");
            lenny.addAlbum("Circus").setYear(1995).addSong("Rock and roll is dead").addSong("Circus").addSong("Beyond" +
                    " the 7th Sky");
        } catch (EditException e) {
            LOGGER.error("Error while creating model", e);
        }
        return jukebox;
    }

    public static Jukebox createJukeBoxWithEmptyLibrary(ModelNodeHelperRegistry modelNodeHelperRegistry,
                                                        SubSystemRegistry subSystemRegistry, SchemaRegistry
                                                                schemaRegistry) {
        registerClasses(modelNodeHelperRegistry);
        Jukebox jukebox = new Jukebox(null, new ModelNodeId(), modelNodeHelperRegistry, subSystemRegistry,
                schemaRegistry);
        try {
            jukebox.setLibrary(new Library(jukebox, new ModelNodeId(), modelNodeHelperRegistry, subSystemRegistry,
                    schemaRegistry));
        } catch (EditException e) {
            LOGGER.error("Error while creating model", e);
        }
        return jukebox;
    }

    /**
     * * You want to use this if you have one filter child represented by filterInput.
     *
     * @param server
     * @param filterInput
     * @param expectedXmlPath
     * @param messageId
     * @throws SAXException
     * @throws IOException
     */
    public static void verifyGet(NetconfServer server, String filterInput, String expectedXmlPath, String messageId)
            throws SAXException, IOException {
        NetconfFilter filter = null;
        if (filterInput != null && !filterInput.isEmpty()) {
            filter = new NetconfFilter();
            filter.addXmlFilter(loadAsXml(filterInput));
        }
        verifyGet(server, filter, expectedXmlPath, messageId);
    }

    public static NetConfResponse verifyGet(NetconfServerMessageListener server, String filterInput, String
            expectedXmlPath, String messageId) throws SAXException, IOException {
        NetconfFilter filter = null;
        if (filterInput != null && !filterInput.isEmpty()) {
            filter = new NetconfFilter();
            filter.addXmlFilter(loadAsXml(filterInput));
        }
        return verifyGet(server, filter, expectedXmlPath, messageId);
    }

    /**
     * You want to use this if you have more than one filter which possibly target nodes in 2 different yang modules.
     *
     * @param server
     * @param netconfFilter
     * @param expectedXmlPath
     * @param messageId
     * @throws SAXException
     * @throws IOException
     */
    public static void verifyGet(NetconfServer server, NetconfFilter netconfFilter, String expectedXmlPath, String
            messageId) throws SAXException, IOException {
        NetconfClientInfo client = new NetconfClientInfo("test", 1);
        GetRequest request = new GetRequest();
        request.setMessageId(messageId);
        request.setFilter(netconfFilter);
        NetConfResponse response = new NetConfResponse();
        response.setMessageId(messageId);
        server.onGet(client, request, response);
        assertXMLEquals(expectedXmlPath, response);
    }

    public static NetConfResponse verifyGet(NetconfServerMessageListener server, NetconfFilter netconfFilter, String
            expectedXmlPath, String messageId) throws SAXException, IOException {
        NetconfClientInfo client = new NetconfClientInfo("test", 1);
        GetRequest request = new GetRequest();
        request.setMessageId(messageId);
        request.setFilter(netconfFilter);
        NetConfResponse response = new NetConfResponse();
        response.setMessageId(messageId);
        server.onGet(client, request, response);
        assertXMLEquals(expectedXmlPath, response);
        return response;
    }

    /**
     * * You want to use this if you have one filter child represented by filterInput.
     *
     * @param server
     * @param filterInput
     * @param expectedOutputFilePath
     * @param messageId
     * @throws SAXException
     * @throws IOException
     */
    public static void verifyGetConfig(NetconfServer server, String filterInput, String expectedOutputFilePath,
                                       String messageId) throws SAXException, IOException {
        NetconfFilter filter = null;
        if (filterInput != null && !filterInput.isEmpty()) {
            filter = new NetconfFilter();
            filter.addXmlFilter(loadAsXml(filterInput));
        }
        verifyGetConfig(server, filter, expectedOutputFilePath, messageId);
    }

    public static void verifyGetConfig(NetconfServer server, String expectedResponseString) throws
            NetconfMessageBuilderException,
            IOException, SAXException {
        NetconfClientInfo client = new NetconfClientInfo("test", 1);
        GetConfigRequest request = new GetConfigRequest();
        request.setMessageId("1");
        NetConfResponse response = new NetConfResponse();
        response.setMessageId("1");
        server.onGetConfig(client, request, response);
        assertXMLEquals(DocumentToPojoTransformer.getNetconfResponse(DocumentUtils.stringToDocument
                (expectedResponseString)), response);
    }

    /**
     * You want to use this if you have more than one filter which possibly target nodes in 2 different yang modules.
     *
     * @param server
     * @param netconfFilter
     * @param expectedOutputFilePath
     * @param messageId
     * @throws SAXException
     * @throws IOException
     */
    public static void verifyGetConfig(NetconfServer server, NetconfFilter netconfFilter, String
            expectedOutputFilePath, String messageId) throws SAXException, IOException {
        NetconfClientInfo client = new NetconfClientInfo("test", 1);
        GetConfigRequest request = new GetConfigRequest();
        request.setMessageId(messageId);
        request.setFilter(netconfFilter);
        NetConfResponse response = new NetConfResponse();
        response.setMessageId(messageId);
        server.onGetConfig(client, request, response);
        assertXMLEquals(expectedOutputFilePath, response);
    }

    public static void verifyGetConfig(NetconfServer server, String requestFilterString, String expectedOutput,
                                       NetconfClientInfo clientInfo) throws SAXException, IOException {
        GetConfigRequest request = new GetConfigRequest();
        NetconfFilter filter = null;
        request.setMessageId("1");
        if (requestFilterString != null && !requestFilterString.isEmpty()) {
            filter = new NetconfFilter();
            Element filterElement = transformToElement(requestFilterString);
            filter.addXmlFilter(filterElement);
        }
        request.setFilter(filter);
        NetConfResponse response = new NetConfResponse();
        response.setMessageId("1");
        server.onGetConfig(clientInfo, request, response);
        Element expectedElement = transformToElement(expectedOutput);
        Element actualElement = transformToElement(response.responseToString());
        assertXMLEquals(expectedElement, actualElement);
    }

    public static boolean assertXMLEquals(Element expectedElement, Element actualElement, List<String>
            ignoreElements) throws SAXException, IOException {
        boolean result = compareXMLEquals(expectedElement, actualElement, ignoreElements);
        LOGGER.info("assertXMLEquals result: " + result);
        if (!result) {
            String expectedXml = null;
            try {
                expectedXml = DocumentUtils.documentToPrettyString(expectedElement);
                String actualXml = DocumentUtils.documentToPrettyString(actualElement);
                LOGGER.error("expected: \n {}", expectedXml);
                LOGGER.error("actual: \n {}", actualXml);
            } catch (NetconfMessageBuilderException e) {
                throw new RuntimeException(e);
            }

        }
        assertTrue(result);
        return result;
    }

    public static boolean assertXMLEqualsWithOrder(Element expectedElement, Element actualElement) throws
            SAXException, IOException {
        boolean result = compareXMLEqualsWithOrder(expectedElement, actualElement);
        LOGGER.info("assertXMLEquals result: " + result);
        if (!result) {
            String expectedXml = null;
            try {
                expectedXml = DocumentUtils.documentToPrettyString(expectedElement);
                String actualXml = DocumentUtils.documentToPrettyString(actualElement);
                LOGGER.error("expected: \n {}", expectedXml);
                LOGGER.error("actual: \n {}", actualXml);
            } catch (NetconfMessageBuilderException e) {
                throw new RuntimeException(e);
            }
        }
        assertTrue(result);
        return result;
    }

    public static boolean assertXMLEquals(Element expectedElement, Element actualElement) throws SAXException,
            IOException {
        return assertXMLEquals(expectedElement, actualElement, Collections.EMPTY_LIST);
    }

    public static boolean assertXMLEquals(Element expectedElement, NetConfResponse actualResponse) throws
            SAXException, IOException {
        return assertXMLEquals(expectedElement, responseToElement(actualResponse));
    }

    public static boolean assertXMLEquals(Element expectedElement, AbstractNetconfRequest actualRequest) throws
            SAXException, IOException {
        return assertXMLEquals(expectedElement, requestToElement(actualRequest));
    }

    public static boolean assertXMLEquals(String expectedXmlPath, NetConfResponse actualResponse) throws
            SAXException, IOException {
        return assertXMLEquals(loadAsXml(expectedXmlPath), responseToElement(actualResponse));
    }

    public static boolean assertXMLEquals(String expectedXmlPath, AbstractNetconfRequest actualRequest) throws
            SAXException, IOException {
        return assertXMLEquals(loadAsXml(expectedXmlPath), requestToElement(actualRequest));
    }

    public static boolean assertXMLEquals(String expectedXmlPath, Element actualElement) throws SAXException,
            IOException {
        return assertXMLEquals(loadAsXml(expectedXmlPath), actualElement);
    }

    public static boolean assertXMLEquals(NetConfResponse expectedResponse, Element actualElement) throws
            SAXException, IOException {
        return assertXMLEquals(responseToElement(expectedResponse), actualElement);
    }

    public static boolean assertXMLEquals(NetConfResponse expectedResponse, NetConfResponse actualResponse) throws
            SAXException, IOException {
        return assertXMLEquals(responseToElement(expectedResponse), responseToElement(actualResponse));
    }

    public static boolean assertXMLEquals(AbstractNetconfRequest expectedRequest, Element actualElement) throws
            SAXException, IOException {
        return assertXMLEquals(requestToElement(expectedRequest), actualElement);
    }

    public static boolean assertXMLEquals(AbstractNetconfRequest expectedRequest, AbstractNetconfRequest
            actualRequest) throws SAXException, IOException {
        return assertXMLEquals(requestToElement(expectedRequest), requestToElement(actualRequest));
    }

    public static boolean compareResponseEquals(NetConfResponse expectedResponse, NetConfResponse actualResponse,
                                                List<String> ignoreElements) throws IOException, SAXException {
        return compareXMLEquals(responseToElement(expectedResponse), responseToElement(actualResponse), ignoreElements);
    }

    public static boolean compareXMLEquals(Element expectedElement, Element actualElement, List<String>
            ignoreElements) throws IOException, SAXException {
        try {
            //sort expected and actual elements
            sortElement(expectedElement);
            sortElement(actualElement);

            String expectedXml = DocumentUtils.documentToPrettyString(expectedElement);
            String actualXml = DocumentUtils.documentToPrettyString(actualElement);
            LOGGER.info("expected: \n {}", expectedXml);
            LOGGER.info("actual: \n {}", actualXml);
            Diff diff = createXMLDiff(expectedXml, actualXml, ignoreElements);
            boolean similar = diff.similar();
            if (!similar) {
                LOGGER.error(diff.toString());
            }
            return similar;
        } catch (NetconfMessageBuilderException e) {
            throw new RuntimeException(e);
        }
    }


    public static boolean compareXMLEqualsWithOrder(Element expectedElement, Element actualElement) throws
            IOException, SAXException {
        try {

            sortElement(expectedElement);
            sortElement(actualElement);

            String expectedXml = DocumentUtils.documentToPrettyString(expectedElement);
            String actualXml = DocumentUtils.documentToPrettyString(actualElement);
            LOGGER.info("expected: \n {}", expectedXml);
            LOGGER.info("actual: \n {}", actualXml);
            Diff diff = createXMLDiffWithOrder(expectedXml, actualXml);
            boolean identical = diff.identical();
            if (!identical) {
                LOGGER.error(diff.toString());
            }
            return identical;
        } catch (NetconfMessageBuilderException e) {
            throw new RuntimeException(e);
        }
    }

    private static Diff createXMLDiff(String expectedXml, String actualXml, final List<String> ignoreElements) throws
            SAXException, IOException {
        Diff diff = new Diff(expectedXml, actualXml);
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
                    LOGGER.info("Skipping Password Comparison due to Dynamic Salt generation");
                    return RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
                } else if (difference.getTestNodeDetail().getNode().getParentNode().getNodeName().equalsIgnoreCase
                        ("pma:reachable-last-change")) {
                    LOGGER.info("Skipping reachable-last-change Comparison due to current time changing");
                    return RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
                } else if (ignoreElements.contains(difference.getTestNodeDetail().getNode().getParentNode()
                        .getNodeName())) {
                    LOGGER.info("Skipping comparison for " + difference.getTestNodeDetail().getNode().getParentNode()
                            .getNodeName() + " as requested");
                    return RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
                }
                return RETURN_ACCEPT_DIFFERENCE;
            }
        });
        XMLUnit.setIgnoreAttributeOrder(true);
        XMLUnit.setNormalizeWhitespace(true);
        return diff;
    }


    private static Diff createXMLDiffWithOrder(String expectedXml, String actualXml) throws SAXException, IOException {
        Diff diff = new Diff(expectedXml, actualXml);
        diff.overrideElementQualifier(new ElementNameAndTextQualifier());
        diff.overrideDifferenceListener(new DifferenceListener() {
            @Override
            public void skippedComparison(Node arg0, Node arg1) {

            }

            @Override
            public int differenceFound(Difference difference) {
                if (DifferenceConstants.NAMESPACE_PREFIX.equals(difference)) {
                    return RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
                }
                if (difference.getTestNodeDetail().getNode().getNodeType() == Node.TEXT_NODE && difference
                        .getTestNodeDetail().getValue().startsWith("$") && difference.getTestNodeDetail().getNode()
                        .getParentNode().getNodeName().equalsIgnoreCase("pma:password")) {
                    LOGGER.info("Skipping Password Comparison due to Dynamic Salt generation");
                    return RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
                } else if (difference.getTestNodeDetail().getNode().getParentNode().getNodeName().equalsIgnoreCase
                        ("pma:reachable-last-change")) {
                    LOGGER.info("Skipping reachable-last-change Comparison due to current time changing");
                    return RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
                }
                return RETURN_ACCEPT_DIFFERENCE;
            }
        });
        XMLUnit.setIgnoreAttributeOrder(true);
        XMLUnit.setNormalizeWhitespace(true);
        return diff;
    }

    public static void sortElement(Element parentElement) {
        NodeList nodeList = parentElement.getChildNodes();
        List<Element> childElementList = new ArrayList<Element>();
        List<Node> childEmptyTextList = new ArrayList<Node>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node instanceof Element) {
                childElementList.add((Element) node);
            } else if (node.getNodeType() == Node.TEXT_NODE) {
                String textvalue = node.getTextContent().trim();
                if (textvalue.isEmpty()) {
                    childEmptyTextList.add(node);
                }
            }
        }
        //remove child element now, later will append in sorted order
        for (Element element : childElementList) {
            parentElement.removeChild(element);
        }
        //remove empty text nodes
        for (Node textNode : childEmptyTextList) {
            parentElement.removeChild(textNode);
        }
        //sort immediate children
        if (childElementList.size() > 1) {
            Collections.sort(childElementList, new Comparator<Element>() {
                @Override
                public int compare(Element element1, Element element2) {
                    String name1 = element1.getLocalName();
                    String name2 = element2.getLocalName();
                    int diff = name1.compareTo(name2);
                    if (diff != 0) {
                        return diff;
                    }
                    String namespace1 = element1.getNamespaceURI();
                    String namespace2 = element2.getNamespaceURI();
                    diff = namespace1.compareTo(namespace2);
                    if (diff != 0) {
                        return diff;
                    }
                    return 0;
                }
            });
        }

        //append sorted children, and sort descendant node recursively
        for (Element element : childElementList) {
            parentElement.appendChild(element);
            sortElement(element);
        }
    }

    private static Element requestToElement(AbstractNetconfRequest request) {
        try {
            return request.getRequestDocument().getDocumentElement();
        } catch (NetconfMessageBuilderException e) {
            throw new RuntimeException(e);
        }
    }

    private static Element responseToElement(NetConfResponse response) {
        try {
            return response.getResponseDocument().getDocumentElement();
        } catch (NetconfMessageBuilderException e) {
            throw new RuntimeException(e);
        }
    }

    public static void verifyGetConfig(NetconfServer server, String source, String filterInput, String
            expectedOutput, String messageId) throws SAXException, IOException {
        NetconfClientInfo client = new NetconfClientInfo("test", 1);
        GetConfigRequest request = new GetConfigRequest();
        request.setMessageId(messageId);
        request.setSource(source);
        if (filterInput != null) {
            NetconfFilter filter = new NetconfFilter();
            filter.addXmlFilter(loadAsXml(filterInput));
            request.setFilter(filter);
        }
        LOGGER.info("Request: {}", request.requestToString());
        NetConfResponse response = new NetConfResponse();
        response.setMessageId(messageId);
        server.onGetConfig(client, request, response);
        assertXMLEquals(expectedOutput, response);
    }

    public static void verifyGetConfigWithOrder(NetconfServer server, String source, String filterInput, String
            expectedOutput, String messageId) throws SAXException, IOException, NetconfMessageBuilderException{
        NetconfClientInfo client = new NetconfClientInfo("test", 1);
        GetConfigRequest request = new GetConfigRequest();
        request.setMessageId(messageId);
        request.setSource(source);
        if (filterInput != null) {
            NetconfFilter filter = new NetconfFilter();
            filter.addXmlFilter(loadAsXml(filterInput));
            request.setFilter(filter);
        }
        LOGGER.info("Request: {}", request.requestToString());
        NetConfResponse response = new NetConfResponse();
        response.setMessageId(messageId);
        server.onGetConfig(client, request, response);
        assertTrue(createXMLDiffWithOrder(DocumentUtils.documentToPrettyString(loadAsXml(expectedOutput)), response.responseToString()).identical());
    }

    public static void registerClasses(ModelNodeHelperRegistry modelNodeHelperRegistry) {
        try {
            String jukebox = "jukebox";
            ModelNodeRegistrar.registerAnnotationModelNodeClass(jukebox, Jukebox.JUKEBOX_SCHEMA_PATH, Jukebox.class,
                    modelNodeHelperRegistry);
            ModelNodeRegistrar.registerAnnotationModelNodeClass(jukebox, Library.LIBRARY_SCHEMA_PATH, Library.class,
                    modelNodeHelperRegistry);
            ModelNodeRegistrar.registerAnnotationModelNodeClass(jukebox, Artist.ARTIST_SCHEMA_PATH, Artist.class,
                    modelNodeHelperRegistry);
            ModelNodeRegistrar.registerAnnotationModelNodeClass(jukebox, Album.ALBUM_SCHEMA_PATH, Album.class,
                    modelNodeHelperRegistry);
            ModelNodeRegistrar.registerAnnotationModelNodeClass(jukebox, AlbumImage.ALBUMIMAGE_SCHEMA_PATH,
                    AlbumImage.class, modelNodeHelperRegistry);
            ModelNodeRegistrar.registerAnnotationModelNodeClass(jukebox, Song.SONG_SCHEMA_PATH, Song.class,
                    modelNodeHelperRegistry);
            registerModelNodeFactories(modelNodeHelperRegistry);
        } catch (ModelNodeInitException e) {
            LOGGER.error("Error while registeration ", e);
        }
    }

    private static void registerModelNodeFactories(ModelNodeHelperRegistry modelNodeHelperRegistry) {
        try {
            ModelNodeRegistrar.registerModelNodeFactory("AbstractModelNodeFactory", new AbstractModelNodeFactory(),
                    modelNodeHelperRegistry);
        } catch (ModelNodeFactoryException e) {
            LOGGER.error("Error while registering factory", e);
        }
    }

    public static String loadAsString(String name) {
        return FileUtil.loadAsString(name);
    }

    public static Element transformToElement(String xmldata) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setNamespaceAware(true);
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            InputStream stream = new ByteArrayInputStream(xmldata.getBytes(StandardCharsets.UTF_8));
            Document doc = dBuilder.parse(stream);
            return doc.getDocumentElement();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Element loadAsXml(String name) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setNamespaceAware(true);
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(TestUtil.class.getResourceAsStream(name));
            Element xml = null;
            NodeList childNodes = doc.getChildNodes();
            for(int i=0; i< childNodes.getLength(); i++) {
                if(childNodes.item(i) instanceof Element){
                    xml = (Element) childNodes.item(i);
                    break;
                }
            }
            return xml;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Document loadXmlDocument(String name) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(TestUtil.class.getResourceAsStream(name));
            return doc;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String load(String name) {
        StringBuffer sb = new StringBuffer();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(TestUtil.class.getResourceAsStream
                (name)))) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append(System.getProperty("line.separator"));
            }
            return sb.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String prettyPrint(Node element) {
        try {
            Source xmlInput = new DOMSource(DocumentUtils.stringToDocument(DocumentUtils.format(DocumentUtils
                    .documentToString(element))));
            Transformer transformer;
            transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

            StreamResult xmlOutput = new StreamResult(new StringWriter());

            transformer.transform(xmlInput, xmlOutput);
            return xmlOutput.getWriter().toString();
        } catch (TransformerFactoryConfigurationError | TransformerException | NetconfMessageBuilderException e) {
            throw new RuntimeException(e);
        }

    }

    public static String responseToString(NetConfResponse response) {
        try {

            Document responseDoc = response.getResponseDocument();
            return prettyPrint(responseDoc);
        } catch (NetconfMessageBuilderException e) {
            throw new RuntimeException(e);
        }

    }

    public static NetConfResponse sendCopyConfig(NetconfServer server, NetconfClientInfo clientInfo, String source,
                                                 String target, String messageId) {
        CopyConfigRequest copyConfigRequest = new CopyConfigRequest().setSource(source, false).setTarget(target, false);
        copyConfigRequest.setMessageId(messageId);
        NetConfResponse response = new NetConfResponse();
        response.setMessageId(messageId);
        server.onCopyConfig(clientInfo, copyConfigRequest, response);
        return response;
    }

    public static void sendEditConfig(NetConfServerImpl server, Element configElement) {
        sendEditConfig(server, new NetconfClientInfo("UT", 1), configElement, "1");
    }

    public static void sendEditConfig(NetConfServerImpl server, Element configElement, String messageId) {
        sendEditConfig(server, new NetconfClientInfo("UT", 1), configElement, messageId);
    }

    public static NetConfResponse sendEditConfig(NetconfServer server, NetconfClientInfo clientInfo, Element
            configElement, String messageId) {
        EditConfigRequest request = new EditConfigRequest()
                .setTargetRunning()
                .setTestOption(EditConfigTestOptions.SET)
                .setErrorOption(EditConfigErrorOptions.STOP_ON_ERROR)
                .setConfigElement(new EditConfigElement()
                        .addConfigElementContent(configElement));
        request.setMessageId(messageId);
        NetConfResponse response = new NetConfResponse().setMessageId(messageId);
        server.onEditConfig(clientInfo, request, response);
        return response;
    }

    public static NetConfResponse onRpc(NetconfServer server, NetconfClientInfo clientInfo, Element configElement,
                                        String messageId) {
        NetconfRpcRequest request = new NetconfRpcRequest();
        request.setRpcInput(configElement);
        request.setMessageId(messageId);
        request.setClientInfo(clientInfo);
        request.setMessageId(messageId);
        NetconfRpcResponse response = new NetconfRpcResponse();
        response.setMessageId(messageId);
        server.onRpc(clientInfo, request, response);
        return response;

    }

    public static NetConfResponse sendEditConfig(NetconfServerMessageListener server, NetconfClientInfo clientInfo,
                                                 Element configElement, String messageId) {
        EditConfigRequest request = new EditConfigRequest()
                .setTargetRunning()
                .setTestOption(EditConfigTestOptions.SET)
                .setErrorOption(EditConfigErrorOptions.STOP_ON_ERROR)
                .setConfigElement(new EditConfigElement()
                        .addConfigElementContent(configElement));
        request.setMessageId(messageId);
        NetConfResponse response = new NetConfResponse().setMessageId(messageId);
        server.onEditConfig(clientInfo, request, response);
        return response;
    }

    public static List<YangTextSchemaSource> getJukeBoxYangs() {
        List<String> yangFiles = new ArrayList<>();
        yangFiles.add("/referenceyangs/jukebox/example-jukebox@2014-07-03.yang");
        yangFiles.add("/referenceyangs/ietf-restconf.yang");
        yangFiles.add("/referenceyangs/ietf-yang-types.yang");
        yangFiles.add("/referenceyangs/ietf-inet-types.yang");
        return getByteSources(yangFiles);
    }

    public static List<YangTextSchemaSource> getJukeBoxDeps() {
        List<String> yangFiles = new ArrayList<>();
        yangFiles.add("/referenceyangs/ietf-restconf.yang");
        yangFiles.add("/referenceyangs/ietf-yang-types.yang");
        yangFiles.add("/referenceyangs/ietf-inet-types.yang");
        return getByteSources(yangFiles);
    }

    public static YangTextSchemaSource getByteSource(String file) {
        return YangParserUtil.getYangSource(TestUtil.class.getResource(file));
    }

    public static List<YangTextSchemaSource> getByteSources(List<String> yangFiles) {
        List<YangTextSchemaSource> byteSrsList = new ArrayList<>();
        if (yangFiles != null) {
            for (String yang : yangFiles) {
                byteSrsList.add(getByteSource(yang));
            }
        }
        return byteSrsList;
    }

    public static List<YangTextSchemaSource> getJukeBoxYangsWithAlbumStateAttributes() {
        List<YangTextSchemaSource> jukeBoxYangs = new ArrayList<>();
        jukeBoxYangs.add(getByteSource("/referenceyangs/jukebox/example-jukebox-with-album-state-attrs@2014-07" +
                "-03.yang"));
        jukeBoxYangs.addAll(getJukeBoxDeps());
        return jukeBoxYangs;
    }

    public static String xmlToString(Element elt) throws Exception {
        StringWriter buffer = new StringWriter();
        m_transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        m_transformer.transform(new DOMSource(elt), new StreamResult(buffer));
        return buffer.toString();
    }

    public static Element parseXml(String text) throws Exception {
        return parseXmlDocument(text).getDocumentElement();
    }

    public static Document parseXmlDocument(String text) throws Exception {
        return m_builder.parse(new ByteArrayInputStream(text.getBytes()));
    }

    public static void addOutputElements(Element element, NetconfRpcResponse response) {
        NodeList nodes = element.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Element.ELEMENT_NODE) {
                response.addRpcOutputElement((Element) node);
            }
        }
    }

    public static boolean isAvailable(List<Element> elements, String localName, String value) {
        for (Element element : elements) {
            if (element.getLocalName().equals(localName) && element.getTextContent().equals(value)) {
                return true;
            }
        }

        return false;
    }
}
