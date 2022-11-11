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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn;

import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn.CONTAINER;
import static org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.TestConstants.EMPTY_NODE_ID;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ALBUM_LOCAL_NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ALBUM_QNAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ALBUM_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ARTIST_LOCAL_NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ARTIST_QNAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ARTIST_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JB_NS;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JB_REVISION;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JUKEBOX_LOCAL_NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JUKEBOX_MODULE_NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JUKEBOX_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.LIBRARY_LOCAL_NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.LIBRARY_QNAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.LIBRARY_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.NAME_QNAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.SINGER_LOCAL_NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.SINGER_QNAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.SONG_LOCAL_NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.SONG_QNAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.SONG_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.YEAR;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.YEAR_QNAME;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.assertXMLStringEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.api.IndexedList;
import org.broadband_forum.obbaa.netconf.api.util.CryptUtil2;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder;
import org.broadband_forum.obbaa.netconf.api.util.SchemaPathUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryTraverser;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDSMRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.utils.AnnotationAnalysisException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.GenericConfigAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.Service;
import org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.albumxmlsubtree.Album;
import org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.libraryxmlsubtree.Jukebox;
import org.broadband_forum.obbaa.netconf.persistence.EntityDataStoreManager;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

@RunWith(RequestScopeJunitRunner.class)
public class XmlModelNodeToXmlMapperTest {

    private static final String SONG_WITH_CHOICE_CASE_CD_XML = "/xmlmodelnodetoxmlmappertest/song-with-choice-case-cd.xml";
    private static final String SONG_WITH_CHOICE_CASE_DISKETTE_XML = "/xmlmodelnodetoxmlmappertest/song-with-choice-case-diskette.xml";
    private static final String JUKEBOX_SUBTREE_XML = "/xmlmodelnodetoxmlmappertest/jukebox-subtree.xml";
    private static final String LIBRARY_SUBTREE_ARTIST1_XML = "/xmlmodelnodetoxmlmappertest/library-subtree-artist1.xml";
    private static final String LIBRARY_SUBTREE_ARTIST2_XML = "/xmlmodelnodetoxmlmappertest/library-subtree-artist2.xml";
    private static final String ARTIST_XML_WITH_PREFIX = "/xmlmodelnodetoxmlmappertest/artist-xml-with-prefix.xml";
    private static final String ARTIST_XML_WITH_PREFIX_IN_VALUE_IDENTITY_REF = "/xmlmodelnodetoxmlmappertest/artist-with-prefix-in-value_identity-ref.xml";
    private static final String ARTIST_XML_WITH_PREFIX_IN_VALUE_INSTANCE_IDENTIFIER = "/xmlmodelnodetoxmlmappertest/artist-with-prefix-in-value_instance-identifier.xml";
    private static final String ARTIST_XML_WITH_PREFIX_IN_VALUE_FROM_ANOTHER_NS_IDENTITY_REF = "/xmlmodelnodetoxmlmappertest/artist-with-prefix-in-value-anotherNS_identity-ref.xml";
    private static final String ARTIST_XML_WITH_PREFIX_IN_VALUE_FROM_ANOTHER_NS_INSTANCE_IDENTIFIER = "/xmlmodelnodetoxmlmappertest/artist-with-prefix-in-value-anotherNS_instance-identifier.xml";
    private static final String ARTIST_XML_WITH_PREFIX_IN_VALUE_FROM_MULTIPLE_NS_INSTANCE_IDENTIFIER = "/xmlmodelnodetoxmlmappertest/artist-with-prefix-in-value-multipleNS_instance-identifier.xml";
    private static final String ARTIST_SUBTREE_ALBUM1_XML = "/xmlmodelnodetoxmlmappertest/artist-subtree-album1.xml";
    private static final String ARTIST_SUBTREE_ALBUM2_XML = "/xmlmodelnodetoxmlmappertest/artist-subtree-album2.xml";
    private static final String ALBUM_SUBTREE_ADMIN_XML = "/xmlmodelnodetoxmlmappertest/album-subtree-admin.xml";
    private static final String ALBUM_SUBTREE_SONG1_XML = "/xmlmodelnodetoxmlmappertest/album-subtree-song1.xml";
    private static final String ALBUM_SUBTREE_SONG2_XML = "/xmlmodelnodetoxmlmappertest/album-subtree-song2.xml";
    private static final String LIBRARY_XML = "/xmlmodelnodetoxmlmappertest/library.xml";
    private static final String ARTIST_XML = "/xmlmodelnodetoxmlmappertest/artist.xml";
    private static final String ALBUM_LIBRARYSUBTREE_XML = "/xmlmodelnodetoxmlmappertest/album-librarysubtree.xml";
    private static final String ALBUM_ALBUMSUBTREE_XML = "/xmlmodelnodetoxmlmappertest/album-albumsubtree.xml";
    private static final String SERVICE_SUBTREE_XML = "/xmlmodelnodetoxmlmappertest/jukebox-servicesubtree.xml";
    private static final QName ADMIN_QNAME = QName.create(JB_NS, JB_REVISION, "admin");
    private static final QName CD_QNAME = QName.create(JB_NS, JB_REVISION, "cd");
    private static final QName DISKETTE_QNAME = QName.create(JB_NS, JB_REVISION, "diskette");
    private static final Map<QName, ConfigLeafAttribute> ARTIST_ATRIBUTES = Collections.singletonMap(NAME_QNAME, new GenericConfigAttribute(NAME, JB_NS, "Artist1"));
    private static final QName STOCK_QNAME = QName.create(JB_NS, JB_REVISION, "stock");

    private XmlDSMCache m_dsmCache;
    private ModelNodeId m_jukeboxNodeId = new ModelNodeId().addRdn(new ModelNodeRdn(CONTAINER, JB_NS, JUKEBOX_LOCAL_NAME));
    private ModelNodeId m_libraryNodeId = new ModelNodeId(m_jukeboxNodeId).addRdn(new ModelNodeRdn(CONTAINER, JB_NS, LIBRARY_LOCAL_NAME));
    private ModelNodeId m_artist1NodeId = new ModelNodeId(m_libraryNodeId).addRdn(new ModelNodeRdn(CONTAINER, JB_NS, ARTIST_LOCAL_NAME))
            .addRdn(new ModelNodeRdn(NAME_QNAME, "Artist1"));
    private ModelNodeId m_albumModelNodeId = new ModelNodeId(m_libraryNodeId).addRdn(new ModelNodeRdn(CONTAINER, JB_NS, ALBUM_LOCAL_NAME))
            .addRdn(new ModelNodeRdn(NAME_QNAME, "album1"));
    ;
    private ModelNodeId m_songModelNodeId = new ModelNodeId(m_albumModelNodeId).addRdn(new ModelNodeRdn(CONTAINER, JB_NS, SONG_LOCAL_NAME))
            .addRdn(new ModelNodeRdn(NAME_QNAME, "Let it go"));
    private static SchemaRegistry m_schemaRegistry;
    private XmlModelNodeToXmlMapper m_xmlModelNodeToXmlMapper;
    private static EntityModelNodeHelperDeployer m_entityModelNodeHelperDeployer;
    private static ModelNodeHelperRegistry m_modelNodeHelperRegistry;
    private static SubSystemRegistry m_subSystemRegistry;
    private static EntityRegistry m_entityRegistry;
    private static ModelNodeDSMRegistry m_modelNodeDSMRegistry;
    private ModelNodeIndex m_index;
    private Document m_document;

    @Before
    public void setUp() {
        m_document = DocumentUtils.createDocument();
        AbstractApplicationContext context = new ClassPathXmlApplicationContext("/xmlsubtreedsmtest/test-applicationContext.xml");
        m_entityModelNodeHelperDeployer = (EntityModelNodeHelperDeployer) context.getBean("entityModelNodeHelperDeployer");
        m_schemaRegistry = (SchemaRegistry) context.getBean("schemaRegistry");
        m_subSystemRegistry = (SubSystemRegistry) context.getBean("subSystemRegistry");
        m_modelNodeDSMRegistry = (ModelNodeDSMRegistry) context.getBean("modelNodeDSMRegistry");
        SchemaRegistryTraverser traverser = new SchemaRegistryTraverser("JukeBox", Collections.singletonList(m_entityModelNodeHelperDeployer), m_schemaRegistry,
                m_schemaRegistry.getModule(JUKEBOX_MODULE_NAME, Revision.of(JB_REVISION)).orElse(null));
        traverser.traverse();
        SchemaRegistryTraverser traverser2 = new SchemaRegistryTraverser("JukeBox-Aug", Collections.singletonList(m_entityModelNodeHelperDeployer), m_schemaRegistry,
                m_schemaRegistry.getModule("augment-container", Revision.of(JB_REVISION)).orElse(null));
        traverser2.traverse();
        m_modelNodeHelperRegistry = (ModelNodeHelperRegistry) context.getBean("modelNodeHelperRegistry");
        m_entityRegistry = (EntityRegistry) context.getBean("entityRegistry");
        m_dsmCache = mock(XmlDSMCache.class);
        m_xmlModelNodeToXmlMapper = new XmlModelNodeToXmlMapperImpl(m_dsmCache, m_schemaRegistry, m_modelNodeHelperRegistry, m_subSystemRegistry,
                m_entityRegistry, null);
        m_index = new ModelNodeIndex();
        String keyFilePath = getClass().getResource("/domvisitortest/keyfile.plain").getPath();
        CryptUtil2 cryptUtil2 = new CryptUtil2();
        cryptUtil2.setKeyFilePathForTest(keyFilePath);
        cryptUtil2.initFile();
    }

    @Test
    public void testGetXmlModelNode() {
        // Case 1: Jukebox having library as subtree
        Element jukeboxSubtree = TestUtil.loadAsXml(JUKEBOX_SUBTREE_XML);
        XmlModelNodeImpl jukeboxXmlModelNode = new XmlModelNodeImpl(m_document, JUKEBOX_SCHEMA_PATH, new HashMap<>(),
                Collections.singletonList(jukeboxSubtree), null, EMPTY_NODE_ID, m_xmlModelNodeToXmlMapper, m_modelNodeHelperRegistry,
                m_schemaRegistry, m_subSystemRegistry, null, null, true, null);

        Map<QName, IndexedList<ModelNodeId, XmlModelNodeImpl>> children = jukeboxXmlModelNode.getChildren();

        assertEquals(Collections.emptyMap(), jukeboxXmlModelNode.getAttributes()); // No attributes
        assertEquals(1, children.size()); // Assert there is only one child which is library
        IndexedList<ModelNodeId, XmlModelNodeImpl> xmlModelNodes = children.get(LIBRARY_QNAME);
        assertEquals(1, xmlModelNodes.size()); // Assert there is only one library
        XmlModelNodeImpl actualXmlModelNode = xmlModelNodes.get(0);
        assertEquals(LIBRARY_QNAME, actualXmlModelNode.getQName());
        assertEquals(LIBRARY_SCHEMA_PATH, actualXmlModelNode.getModelNodeSchemaPath());

        // Case 2: Library having artist as subtree
        XmlModelNodeImpl libraryXmlModelNode = getLibraryXmlModelNode();

        assertEquals(Collections.emptyMap(), libraryXmlModelNode.getAttributes()); // No attributes
        children = libraryXmlModelNode.getChildren();
        assertEquals(1, children.size()); // Library has only artist as child
        xmlModelNodes = children.get(ARTIST_QNAME);
        assertEquals(2, xmlModelNodes.size());

        // Case 3: Artist having album as subtree
        XmlModelNodeImpl artistXmlModelNode = getArtistXmlModelNode(ARTIST_SCHEMA_PATH, ARTIST_ATRIBUTES, ARTIST_SUBTREE_ALBUM1_XML, ARTIST_SUBTREE_ALBUM2_XML);
        assertEquals(1, artistXmlModelNode.getAttributes().size()); // Artist has one attribute
        assertEquals("Artist1", artistXmlModelNode.getAttributes().get(NAME_QNAME).getStringValue());
        children = artistXmlModelNode.getChildren();
        assertEquals(1, children.size()); // Artist has only album as child
        xmlModelNodes = children.get(ALBUM_QNAME);
        assertEquals(2, xmlModelNodes.size());

        // Case 4: Album having admin and song as subtree
        XmlModelNodeImpl albumXmlModelNode = getAlbumXmlModelNode();

        assertEquals(2, albumXmlModelNode.getAttributes().size());
        children = albumXmlModelNode.getChildren();
        assertEquals(2, children.size()); // admin and songs
        assertEquals(2, children.get(SONG_QNAME).size());
        assertEquals(1, children.get(ADMIN_QNAME).size());
        assertEquals(2, children.get(SONG_QNAME).get(0).getLeafList(SINGER_QNAME).size());
    }

    @Test
    public void testGetXmlFromXmlModelNodeLibrarySubtree() throws IOException, SAXException, AnnotationAnalysisException {
        // Case 1 : When XmlmodelNode doesnt have any attributes or leafLists as separate column
        // i.e., YangAttribute/YangLeafList
        m_entityRegistry.updateRegistry("Jukebox", Arrays.<Class> asList(
                Jukebox.class), m_schemaRegistry, null, m_modelNodeDSMRegistry);
        XmlModelNodeImpl libraryXmlModelNode = getLibraryXmlModelNode();
        Element libraryXml = m_xmlModelNodeToXmlMapper.getXmlValue(libraryXmlModelNode);
        TestUtil.assertXMLEquals(LIBRARY_XML, libraryXml);

        XmlModelNodeImpl artistXmlModelNode = getArtistXmlModelNode(ARTIST_SCHEMA_PATH, ARTIST_ATRIBUTES, ARTIST_SUBTREE_ALBUM1_XML, ARTIST_SUBTREE_ALBUM2_XML);
        Element artistXml = m_xmlModelNodeToXmlMapper.getXmlValue(artistXmlModelNode);
        TestUtil.assertXMLEquals(ARTIST_XML, artistXml);

        XmlModelNodeImpl albumXmlModelNode = getAlbumXmlModelNode();
        Element albumXml = m_xmlModelNodeToXmlMapper.getXmlValue(albumXmlModelNode);

        // test the first element first, because assertXMLEquals will reorder the children
        // keys need to come first in a list
        Element albumFirstChild = getFirstChildElement(albumXml);
        assertEquals(NAME_QNAME.getLocalName(), albumFirstChild.getNodeName());
        assertEquals(NAME_QNAME.getNamespace().toString(), albumFirstChild.getNamespaceURI());

        NodeList songs = albumXml.getElementsByTagNameNS(SONG_QNAME.getNamespace().toString(), SONG_QNAME.getLocalName());
        for (int i = 0; i < songs.getLength(); i++) {
            Element song = (Element) songs.item(i);
            Element songFirstChild = getFirstChildElement(song);
            assertEquals(NAME_QNAME.getLocalName(), songFirstChild.getLocalName());
            assertEquals(NAME_QNAME.getNamespace().toString(), songFirstChild.getNamespaceURI());
        }

        TestUtil.assertXMLEquals(ALBUM_LIBRARYSUBTREE_XML, albumXml);
    }

    public Element getFirstChildElement(Element elt) {
        NodeList childNodes = elt.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            if (childNode instanceof Element) {
                return (Element) childNode;
            }
        }
        return null;
    }

    @Test
    public void testGetXmlFromXmlModelNodeAlbumSubtree() throws AnnotationAnalysisException, IOException, SAXException {
        // Case 2 : When XmlModelNode has attributes as separate column
        m_entityRegistry.updateRegistry("Jukebox", Arrays.<Class> asList(org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.albumxmlsubtree.Jukebox.class), m_schemaRegistry, mock(EntityDataStoreManager.class), m_modelNodeDSMRegistry);
        XmlModelNodeImpl  albumXmlModelNode= getAlbumXmlModelNode();
        TestUtil.assertXMLEquals(ALBUM_ALBUMSUBTREE_XML, m_xmlModelNodeToXmlMapper.getXmlValue(albumXmlModelNode));
    }

    @Test
    public void testGetXmlModelNodeFromEntity() throws AnnotationAnalysisException {
        Album album = getAlbumEntity(TestUtil.loadAsString(ALBUM_LIBRARYSUBTREE_XML));

        XmlModelNodeImpl albumModelNode = m_xmlModelNodeToXmlMapper.getModelNode(album, null);
        assertEquals(2, albumModelNode.getAttributes().size());
        assertEquals("album11", albumModelNode.getAttribute(NAME_QNAME).getStringValue());
        assertNull(albumModelNode.getParentModelNode());
        Map<QName, IndexedList<ModelNodeId, XmlModelNodeImpl>> children = albumModelNode.getChildren();
        assertEquals(2, children.size());
        assertEquals(2, children.get(SONG_QNAME).size());
        Set<ConfigLeafAttribute> leafLists = children.get(SONG_QNAME).get(0).getLeafList(SINGER_QNAME);
        assertEquals(2, leafLists.size());
        assertEquals(1, children.get(SONG_QNAME).get(1).getLeafList(SINGER_QNAME).size());
        Set<ConfigLeafAttribute> expectedLeafLists = new HashSet<>();
        expectedLeafLists.add(new GenericConfigAttribute(SINGER_LOCAL_NAME, JB_NS, "Singer1"));
        expectedLeafLists.add(new GenericConfigAttribute(SINGER_LOCAL_NAME, JB_NS, "Singer2"));
        assertEquals(expectedLeafLists, leafLists);
    }

    @Test
    public void testMNIDCreatedCorrectly_ParentChildDiffNS() throws AnnotationAnalysisException {
        Service service = getServiceEntity(TestUtil.loadAsString(SERVICE_SUBTREE_XML));

        XmlModelNodeImpl serviceModelNode = m_xmlModelNodeToXmlMapper.getModelNode(service, null);
        ModelNodeId serviceModelNodeId = new ModelNodeId(m_jukeboxNodeId).
                addRdn(new ModelNodeRdn(CONTAINER, "http://example.com/ns/example-jukebox-augment","service")).
                addRdn(new ModelNodeRdn("type","http://example.com/ns/example-jukebox-augment","type1"));
        assertEquals(m_jukeboxNodeId, serviceModelNode.getParentNodeId());
        assertEquals(serviceModelNodeId,serviceModelNode.getModelNodeId());
    }

    private Album getAlbumEntity(String xmlSubtree) throws AnnotationAnalysisException {
        m_entityRegistry.updateRegistry("Jukebox", Arrays.<Class>asList(org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.albumxmlsubtree.Jukebox.class), m_schemaRegistry, mock(EntityDataStoreManager.class), m_modelNodeDSMRegistry);
        Album album = new Album();
        album.setName("album11");
        album.setYear("2016");
        album.setParentId(m_artist1NodeId.getModelNodeIdAsString());
        album.setXmlSubtree(xmlSubtree);
        album.setSchemaPath(SchemaPathUtil.toString(ALBUM_SCHEMA_PATH));
        return album;
    }

    private Service getServiceEntity(String xmlSubtree) throws AnnotationAnalysisException {
        m_entityRegistry.updateRegistry("JukeBox-Aug", Arrays.asList(org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.Service.class),
                m_schemaRegistry, mock(EntityDataStoreManager.class), m_modelNodeDSMRegistry);
        SchemaPath serviceSP = new SchemaPathBuilder().withParent(JUKEBOX_SCHEMA_PATH).appendQName(QName.create("http://example.com/ns/example-jukebox-augment","service")).build();
        Service service = new Service();
        service.setType("type1");
        service.setSpecificData(xmlSubtree);
        service.setParentId(m_jukeboxNodeId.getModelNodeIdAsString());
        service.setSchemaPath(SchemaPathUtil.toString(serviceSP));
        return service;
    }

    @Test
    public void testGetXmlModelNodeEmptyXmlSubtree() throws AnnotationAnalysisException {
        Album album = getAlbumEntity("");

        XmlModelNodeImpl albumModelNode = m_xmlModelNodeToXmlMapper.getModelNode(album, null);
        assertEquals(2, albumModelNode.getAttributes().size());
        assertEquals("album11", albumModelNode.getAttribute(NAME_QNAME).getStringValue());
        assertNull(albumModelNode.getParentModelNode());
    }

    @Test
    public void testGetXmlModelNodeNullXmlSubtree() throws AnnotationAnalysisException {
        Album album = getAlbumEntity(null);

        XmlModelNodeImpl albumModelNode = m_xmlModelNodeToXmlMapper.getModelNode(album, null);
        assertEquals(2, albumModelNode.getAttributes().size());
        assertEquals("album11", albumModelNode.getAttribute(NAME_QNAME).getStringValue());
        assertNull(albumModelNode.getParentModelNode());
    }

    private XmlModelNodeImpl getLibraryXmlModelNode() {
        Element librarySubtreeArtist1 = TestUtil.loadAsXml(LIBRARY_SUBTREE_ARTIST1_XML);
        Element librarySubtreeArtist2 = TestUtil.loadAsXml(LIBRARY_SUBTREE_ARTIST2_XML);
        List<Element> childrenXml = new ArrayList<>();
        childrenXml.add((Element) m_document.importNode(librarySubtreeArtist1, true));
        childrenXml.add((Element) m_document.importNode(librarySubtreeArtist2, true));

        return new XmlModelNodeImpl(m_document, LIBRARY_SCHEMA_PATH, new HashMap<>(), childrenXml, null, m_jukeboxNodeId,
                m_xmlModelNodeToXmlMapper, m_modelNodeHelperRegistry, m_schemaRegistry, m_subSystemRegistry, null, null, true, null);
    }

    private XmlModelNodeImpl getArtistXmlModelNode(final SchemaPath schemaPath, final Map<QName, ConfigLeafAttribute> attributes, final String... xmlPaths) {
        List<Element> childrenXml = new ArrayList<>();
        for (final String path : xmlPaths) {
            childrenXml.add((Element) m_document.importNode(TestUtil.loadAsXml(path), true));
        }

        XmlModelNodeImpl xmlModelNode = new XmlModelNodeImpl(m_document, schemaPath, attributes, childrenXml, null, m_libraryNodeId,
                m_xmlModelNodeToXmlMapper, m_modelNodeHelperRegistry, m_schemaRegistry, m_subSystemRegistry, null, null, true, null);
        materialise(xmlModelNode);
        return xmlModelNode;
    }

    private XmlModelNodeImpl getAlbumXmlModelNode() {
        List<Element> childrenXml;
        Map<QName, ConfigLeafAttribute> configAttributes = new LinkedHashMap<>();
        configAttributes.put(NAME_QNAME, new GenericConfigAttribute(NAME, JB_NS, "album11"));
        configAttributes.put(YEAR_QNAME, new GenericConfigAttribute(YEAR, JB_NS, "2016"));
        Element albumSubtreeAdmin = TestUtil.loadAsXml(ALBUM_SUBTREE_ADMIN_XML);
        Element albumSubtreeSong1 = TestUtil.loadAsXml(ALBUM_SUBTREE_SONG1_XML);
        Element albumSubtreeSong2 = TestUtil.loadAsXml(ALBUM_SUBTREE_SONG2_XML);
        childrenXml = new ArrayList<>();
        childrenXml.add((Element) m_document.importNode(albumSubtreeAdmin, true));
        childrenXml.add((Element) m_document.importNode(albumSubtreeSong1, true));
        childrenXml.add((Element) m_document.importNode(albumSubtreeSong2, true));

        return new XmlModelNodeImpl(m_document, ALBUM_SCHEMA_PATH, configAttributes, childrenXml, null, m_artist1NodeId, m_xmlModelNodeToXmlMapper,
                m_modelNodeHelperRegistry, m_schemaRegistry, m_subSystemRegistry, null, null, true, null);
    }

    @Test
    public void testGetModelNodeFromNodeSchemaPathWithChoiceNode() throws AnnotationAnalysisException {
        Element songwithCDElement = TestUtil.loadAsXml(SONG_WITH_CHOICE_CASE_CD_XML);

        XmlModelNodeImpl albumModelNode = mock(XmlModelNodeImpl.class);
        when(albumModelNode.getSchemaRegistry()).thenReturn(m_schemaRegistry);
        when(albumModelNode.getModelNodeHelperRegistry()).thenReturn(m_modelNodeHelperRegistry);
        when(albumModelNode.getIndex()).thenReturn(m_index);
        ModelNodeId albumNodeId = new ModelNodeId("/container=jukebox/container=library/container=album/name=album1", "test:ns");
        when(albumModelNode.getIndexNodeId()).thenReturn(albumNodeId);
        when(albumModelNode.getModelNodeId()).thenReturn(albumNodeId);
        when(albumModelNode.getSchemaRoot()).thenReturn(albumModelNode);

        XmlModelNodeImpl songWithCDModelNode = m_xmlModelNodeToXmlMapper.getModelNodeFromNodeSchemaPath(true, songwithCDElement, Collections.emptyMap(), new HashMap<>(), SONG_SCHEMA_PATH,
                m_albumModelNodeId, albumModelNode, null, null, null).get(0);
        Map<QName, IndexedList<ModelNodeId, XmlModelNodeImpl>> songWithCDChildrenMap = songWithCDModelNode.getChildren();
        assertEquals(1, songWithCDChildrenMap.size());

        //verify choice case cd modelnode
        IndexedList<ModelNodeId, XmlModelNodeImpl> cdModelNodeList = songWithCDChildrenMap.get(CD_QNAME);
        assertEquals(1, cdModelNodeList.size());
        XmlModelNodeImpl cdModelNode = cdModelNodeList.get(0);
        assertEquals("100", cdModelNode.getAttribute(STOCK_QNAME).getStringValue());

        Element songWithDisketteElement = TestUtil.loadAsXml(SONG_WITH_CHOICE_CASE_DISKETTE_XML);
        XmlModelNodeImpl songWithDisketteModelNode = m_xmlModelNodeToXmlMapper.getModelNodeFromNodeSchemaPath(true, songWithDisketteElement, Collections.emptyMap(), new HashMap<>(), SONG_SCHEMA_PATH,
                m_albumModelNodeId, albumModelNode, null, null, null).get(0);
        Map<QName, IndexedList<ModelNodeId, XmlModelNodeImpl>> songWithDisketteChildrenMap = songWithDisketteModelNode.getChildren();
        assertEquals(1, songWithDisketteChildrenMap.size());

        //verify choice case diskette modelnode
        IndexedList<ModelNodeId, XmlModelNodeImpl> disketteModelNodeList = songWithDisketteChildrenMap.get(DISKETTE_QNAME);
        assertEquals(1, disketteModelNodeList.size());
        XmlModelNodeImpl disketteModelNode = disketteModelNodeList.get(0);
        assertEquals("200", disketteModelNode.getAttribute(STOCK_QNAME).getStringValue());

    }

    @Test
    public void testGetModelNodeFromNodeSchemaPathReturnsNullWhenNodeNotFound() throws Exception {
        Element libraryWithoutSongs = DocumentUtils.stringToDocument(
                "<not-song xmlns=\"http://example.com/ns/example-jukebox\">\n" +
                        "    <name>Let it go</name>\n" +
                        "    <location>somelocation</location>\n" +
                        "    <format>mp3</format>\n" +
                        "    <singer>Singer1</singer>\n" +
                        "    <singer>Singer2</singer>\n" +
                        "    <cd>\n" +
                        "    <stock>100</stock>\n" +
                        "    </cd>\n" +
                        "</not-song>").getDocumentElement();

        XmlModelNodeImpl albumModelNode = mock(XmlModelNodeImpl.class);
        assertEquals(0, m_xmlModelNodeToXmlMapper.getModelNodeFromNodeSchemaPath(true, libraryWithoutSongs, Collections.emptyMap(), Collections.emptyMap(),
                SONG_SCHEMA_PATH,
                m_albumModelNodeId, albumModelNode, null, null, null).size());
    }

    @Test
    public void testCacheHit() {
        Element songwithCDElement = TestUtil.loadAsXml(SONG_WITH_CHOICE_CASE_CD_XML);

        XmlModelNodeImpl albumModelNode = mock(XmlModelNodeImpl.class);
        when(albumModelNode.getSchemaRegistry()).thenReturn(m_schemaRegistry);
        when(albumModelNode.getModelNodeHelperRegistry()).thenReturn(m_modelNodeHelperRegistry);
        when(albumModelNode.getIndex()).thenReturn(m_index);
        ModelNodeId albumNodeId = new ModelNodeId("/container=jukebox/container=library/container=album/name=album1", "test:ns");
        when(albumModelNode.getIndexNodeId()).thenReturn(albumNodeId);
        when(albumModelNode.getModelNodeId()).thenReturn(albumNodeId);
        when(albumModelNode.getSchemaRoot()).thenReturn(albumModelNode);
        XmlModelNodeImpl songWithCDModelNode = m_xmlModelNodeToXmlMapper.getModelNodeFromNodeSchemaPath(true, songwithCDElement, Collections.emptyMap(), new HashMap<>(), SONG_SCHEMA_PATH,
                m_albumModelNodeId, albumModelNode, null, null, null).get(0);
        Map<QName, IndexedList<ModelNodeId, XmlModelNodeImpl>> songWithCDChildrenMap = songWithCDModelNode.getChildren();
        assertEquals(1, songWithCDChildrenMap.size());

        verify(m_dsmCache).getFromCache(SONG_SCHEMA_PATH, m_songModelNodeId);
        verify(m_dsmCache).putInCache(SONG_SCHEMA_PATH, m_songModelNodeId, songWithCDModelNode);
    }

    @Test
    public void testGetXmlModelNodeChildRedundantPrefixIsRemoved() throws Exception {
        m_entityRegistry.updateRegistry("Jukebox", Arrays.<Class>asList(
                Jukebox.class), m_schemaRegistry, null, m_modelNodeDSMRegistry);
        XmlModelNodeImpl artistXmlModelNode = getArtistXmlModelNode(LIBRARY_SCHEMA_PATH, new HashMap<>(), ARTIST_XML_WITH_PREFIX);

        Element libraryXml = m_xmlModelNodeToXmlMapper.getXmlValue(artistXmlModelNode);
        assertXMLStringEquals("<artist xmlns=\"http://example.com/ns/example-jukebox\">\n" +
                "  <name>Artist1</name>\n" +
                "  <album>\n" +
                "    <name>album11</name>\n" +
                "    <year>2016</year>\n" +
                "  </album>\n" +
                "</artist>", DocumentUtils.documentToPrettyString(DocumentUtils.getChildElement(libraryXml, "artist")).trim());
    }

    @Test
    public void testGetXmlModelNodeChildThePrefixInValueRemainsTheSameForAnIdentityRefBuiltInType() throws Exception {
        m_entityRegistry.updateRegistry("Jukebox", Arrays.<Class>asList(
                Jukebox.class), m_schemaRegistry, null, m_modelNodeDSMRegistry);
        XmlModelNodeImpl artistXmlModelNode = getArtistXmlModelNode(LIBRARY_SCHEMA_PATH, new HashMap<>(), ARTIST_XML_WITH_PREFIX_IN_VALUE_IDENTITY_REF);

        Element libraryXml = m_xmlModelNodeToXmlMapper.getXmlValue(artistXmlModelNode);
        assertXMLStringEquals("<artist xmlns=\"http://example.com/ns/example-jukebox\">\n" +
                "  <name>Artist1</name>\n" +
                "  <album>\n" +
                "    <name>album322222</name>\n" +
                "    <genre2 xmlns:jbox=\"http://example.com/ns/example-jukebox\">jbox:pop</genre2>\n" +
                "  </album>\n" +
                "</artist>", DocumentUtils.documentToPrettyString(DocumentUtils.getChildElement(libraryXml, "artist")).trim());
    }

    @Test
    public void testGetXmlModelNodeChildThePrefixInValueFromAnotherNSForAnIdentityRefBuiltInType() throws Exception {
        m_entityRegistry.updateRegistry("Jukebox", Arrays.<Class>asList(
                Jukebox.class), m_schemaRegistry, null, m_modelNodeDSMRegistry);
        XmlModelNodeImpl artistXmlModelNode = getArtistXmlModelNode(LIBRARY_SCHEMA_PATH, new HashMap<>(), ARTIST_XML_WITH_PREFIX_IN_VALUE_FROM_ANOTHER_NS_IDENTITY_REF);

        Element libraryXml = m_xmlModelNodeToXmlMapper.getXmlValue(artistXmlModelNode);
        assertXMLStringEquals("<artist xmlns=\"http://example.com/ns/example-jukebox\">\n" +
                "  <name>Artist1</name>\n" +
                "  <album>\n" +
                "    <name>album322222</name>\n" +
                "    <genre2 xmlns:gn=\"http://example.com/ns/genre2\">gn:base-genre2</genre2>\n" +
                "  </album>\n" +
                "</artist>", DocumentUtils.documentToPrettyString(DocumentUtils.getChildElement(libraryXml, "artist")).trim());
    }

    @Test
    public void testGetXmlModelNodeChildThePrefixInValueRemainsTheSameForAnInstanceIdentifierBuiltInType() throws Exception {
        m_entityRegistry.updateRegistry("Jukebox", Arrays.<Class>asList(
                Jukebox.class), m_schemaRegistry, null, m_modelNodeDSMRegistry);
        XmlModelNodeImpl artistXmlModelNode = getArtistXmlModelNode(LIBRARY_SCHEMA_PATH, new HashMap<>(), ARTIST_XML_WITH_PREFIX_IN_VALUE_INSTANCE_IDENTIFIER);

        Element libraryXml = m_xmlModelNodeToXmlMapper.getXmlValue(artistXmlModelNode);
        assertXMLStringEquals("<artist xmlns=\"http://example.com/ns/example-jukebox\">\n" +
                "  <name>Artist1</name>\n" +
                "  <mgmt-jukebox xmlns:jbox=\"http://example.com/ns/example-jukebox\">/jbox:jukebox/jbox:library/jbox:artist/jbox:album/jbox:name</mgmt-jukebox>\n" +
                "  <album>\n" +
                "    <name>album1</name>\n" +
                "  </album>\n" +
                "  <album>\n" +
                "    <name>album2</name>\n" +
                "  </album>\n" +
                "</artist>", DocumentUtils.documentToPrettyString(DocumentUtils.getChildElement(libraryXml, "artist")).trim());
    }

    @Test
    public void testGetXmlModelNodeChildThePrefixInValueFromAnotherNSForAnInstanceIdentifierBuiltInType () throws Exception {
        m_entityRegistry.updateRegistry("Jukebox", Arrays.<Class>asList(
                Jukebox.class), m_schemaRegistry, null, m_modelNodeDSMRegistry);
        XmlModelNodeImpl artistXmlModelNode = getArtistXmlModelNode(LIBRARY_SCHEMA_PATH, new HashMap<>(), ARTIST_XML_WITH_PREFIX_IN_VALUE_FROM_ANOTHER_NS_INSTANCE_IDENTIFIER);

        Element libraryXml = m_xmlModelNodeToXmlMapper.getXmlValue(artistXmlModelNode);
        assertXMLStringEquals("<artist xmlns=\"http://example.com/ns/example-jukebox\">\n" +
                "  <name>Artist1</name>\n" +
                "  <mgmt-jukebox xmlns:mgb=\"http://example.com/ns/mgmt-jukebox\">/mgb:mgmt-jukebox-1/mgb:mgmt-jukebox-2/mgb:name</mgmt-jukebox>\n" +
                "  <album>\n" +
                "    <name>album1</name>\n" +
                "  </album>\n" +
                "  <album>\n" +
                "    <name>album2</name>\n" +
                "  </album>\n" +
                "</artist>", DocumentUtils.documentToPrettyString(DocumentUtils.getChildElement(libraryXml, "artist")).trim());
    }

    @Test
    public void testGetXmlModelNodeChildThePrefixInValueWithMultiplenameSpacesForAnInstanceIdentifierBuiltInType () throws Exception {
        m_entityRegistry.updateRegistry("Jukebox", Arrays.<Class>asList(
                Jukebox.class), m_schemaRegistry, null, m_modelNodeDSMRegistry);
        XmlModelNodeImpl artistXmlModelNode = getArtistXmlModelNode(LIBRARY_SCHEMA_PATH, new HashMap<>(), ARTIST_XML_WITH_PREFIX_IN_VALUE_FROM_MULTIPLE_NS_INSTANCE_IDENTIFIER);
        materialise(artistXmlModelNode);
        Element libraryXml = m_xmlModelNodeToXmlMapper.getXmlValue(artistXmlModelNode);
        assertXMLStringEquals("<artist xmlns=\"http://example.com/ns/example-jukebox\">\n" +
                "  <name>Artist1</name>\n" +
                "  <mgmt-jukebox xmlns:jbox=\"http://example.com/ns/example-jukebox\" xmlns:mgb=\"http://example.com/ns/mgmt-jukebox\" xmlns:mgb1=\"http://example.com/ns/mgmt-jukebox-1\">\n" +
                "        /jbox:xyz/mgb:abc/mgb1:lmn/mgb1:name</mgmt-jukebox>\n" +
                "  <album>\n" +
                "    <name>album1</name>\n" +
                "  </album>\n" +
                "  <album>\n" +
                "    <name>album2</name>\n" +
                "  </album>\n" +
                "</artist>", DocumentUtils.documentToPrettyString(DocumentUtils.getChildElement(libraryXml, "artist")).trim());
    }

    @Test
    public void testPasswordsAreEncryptedInXml() throws Exception {
        XmlModelNodeImpl mockLibraryXmlMN = mock(XmlModelNodeImpl.class);
        when(mockLibraryXmlMN.getModelNodeSchemaPath()).thenReturn(LIBRARY_SCHEMA_PATH);
        m_entityRegistry.updateRegistry("Jukebox", Arrays.<Class>asList(
                Jukebox.class), m_schemaRegistry, null, m_modelNodeDSMRegistry);
        when(mockLibraryXmlMN.getDocument()).thenReturn(m_document);

        QName leaf1Qname = QName.create(JB_NS, JB_REVISION, "leaf1");
        QName leaf2Qname = QName.create(JB_NS, JB_REVISION, "leaf2");
        QName leaflist1Qname = QName.create(JB_NS, JB_REVISION, "leaf-list1");
        QName leaflist2Qname = QName.create(JB_NS, JB_REVISION, "leaf-list2");

        mockLeafAttributes(mockLibraryXmlMN, leaf1Qname, leaf2Qname);
        mockLeafLists(mockLibraryXmlMN, leaflist1Qname, leaflist2Qname);

        Element actualXml = m_xmlModelNodeToXmlMapper.getXmlValue(mockLibraryXmlMN);
        Element leaf1Dom = DocumentUtils.getChildElements(actualXml, "leaf1", JB_NS).get(0);
        Element leaf2Dom = DocumentUtils.getChildElements(actualXml, "leaf2", JB_NS).get(0);

        Element leafList11Dom = DocumentUtils.getChildElements(actualXml, "leaf-list1", JB_NS).get(0);
        Element leafList12Dom = DocumentUtils.getChildElements(actualXml, "leaf-list1", JB_NS).get(1);
        Element leafList21Dom = DocumentUtils.getChildElements(actualXml, "leaf-list2", JB_NS).get(0);
        Element leafList22Dom = DocumentUtils.getChildElements(actualXml, "leaf-list2", JB_NS).get(1);

        verifyEncryptedValue("unencryptedleaf1", leaf1Dom);
        verifyEncryptedValue("unencryptedleaf2", leaf2Dom);
        verifyEncryptedValue("unencryptedll12", leafList12Dom);
        verifyEncryptedValue("unencryptedll21", leafList21Dom);
        verifyEncryptedValue("unencryptedll22", leafList22Dom);

        assertEquals("$-0$6fgm/NeQ6IftVFVoy2ToU/8UJVezqP8e9dteaGCdkH4=", leafList11Dom.getTextContent());
        assertEquals("true", leafList11Dom.getAttribute("is-password"));
    }

    public void verifyEncryptedValue(String decryptedValue, Element domElement) {
        assertEquals(decryptedValue, CryptUtil2.decrypt(domElement.getTextContent()));
        assertEquals("true", domElement.getAttribute("is-password"));
    }

    public void mockLeafLists(XmlModelNodeImpl mockLibraryXmlMN, QName leaflist1Qname, QName leaflist2Qname) {
        Map<QName, LinkedHashSet<ConfigLeafAttribute>> mockedLeafLists = new LinkedHashMap<>();
        ConfigLeafAttribute leaflist11 = new GenericConfigAttribute("leaf-list1",JB_NS,"$-0$6fgm/NeQ6IftVFVoy2ToU/8UJVezqP8e9dteaGCdkH4=");
        ConfigLeafAttribute leaflist12 = new GenericConfigAttribute("leaf-list1",JB_NS,"unencryptedll12");
        LinkedHashSet<ConfigLeafAttribute> ll1 = new LinkedHashSet<>();
        ll1.add(leaflist11);
        ll1.add(leaflist12);
        when(mockLibraryXmlMN.getLeafList(leaflist1Qname)).thenReturn(ll1);
        ConfigLeafAttribute leaflist21 = new GenericConfigAttribute("leaf-list2",JB_NS,"unencryptedll21");
        ConfigLeafAttribute leaflist22 = new GenericConfigAttribute("leaf-list2",JB_NS,"unencryptedll22");
        LinkedHashSet<ConfigLeafAttribute> ll2 = new LinkedHashSet<>();
        ll2.add(leaflist21);
        ll2.add(leaflist22);
        when(mockLibraryXmlMN.getLeafList(leaflist2Qname)).thenReturn(ll2);
        mockedLeafLists.put(leaflist1Qname,ll1);
        mockedLeafLists.put(leaflist2Qname,ll2);
        when(mockLibraryXmlMN.getLeafLists()).thenReturn(mockedLeafLists);
        leaflist11.setIsPassword(true);
        leaflist12.setIsPassword(true);
        leaflist21.setIsPassword(true);
        leaflist22.setIsPassword(true);
    }

    public void mockLeafAttributes(XmlModelNodeImpl mockLibraryXmlMN, QName leaf1Qname, QName leaf2Qname) {
        Map<QName, ConfigLeafAttribute> mockedAttributes= new LinkedHashMap<>();
        ConfigLeafAttribute leaf1ConfigAttr = new GenericConfigAttribute("leaf1",JB_NS,"unencryptedleaf1");
        ConfigLeafAttribute leaf2ConfigAttr = new GenericConfigAttribute("leaf2",JB_NS,"unencryptedleaf2");
        mockedAttributes.put(leaf1Qname, leaf1ConfigAttr);
        mockedAttributes.put(leaf2Qname, leaf2ConfigAttr);
        when(mockLibraryXmlMN.getAttributes()).thenReturn(mockedAttributes);
        when(mockLibraryXmlMN.getAttribute(leaf1Qname)).thenReturn(leaf1ConfigAttr);
        when(mockLibraryXmlMN.getAttribute(leaf2Qname)).thenReturn(leaf2ConfigAttr);

        leaf1ConfigAttr.setIsPassword(true);
        leaf2ConfigAttr.setIsPassword(true);
    }

    private void materialise(XmlModelNodeImpl node) {
        for (Map.Entry<QName, IndexedList<ModelNodeId, XmlModelNodeImpl>> childrenOfType : node.getChildren().entrySet()) {
            for (XmlModelNodeImpl child : childrenOfType.getValue().list()) {
                materialise(child);
            }
        }
    }

}