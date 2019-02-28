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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.api.IndexedList;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.SchemaPathUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryTraverser;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryVisitor;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDSMRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.utils.AnnotationAnalysisException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.GenericConfigAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.albumxmlsubtree.Album;
import org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.libraryxmlsubtree.Jukebox;
import org.broadband_forum.obbaa.netconf.persistence.EntityDataStoreManager;
import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

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

    @Before
    public void setUp() {
        AbstractApplicationContext context = new ClassPathXmlApplicationContext("/xmlsubtreedsmtest/test-applicationContext.xml");
        m_entityModelNodeHelperDeployer = (EntityModelNodeHelperDeployer) context.getBean("entityModelNodeHelperDeployer");
        m_schemaRegistry = (SchemaRegistry) context.getBean("schemaRegistry");
        m_subSystemRegistry = (SubSystemRegistry) context.getBean("subSystemRegistry");
        m_modelNodeDSMRegistry = (ModelNodeDSMRegistry) context.getBean("modelNodeDSMRegistry");
        SchemaRegistryTraverser traverser = new SchemaRegistryTraverser("JukeBox", Collections.singletonList((SchemaRegistryVisitor) m_entityModelNodeHelperDeployer), m_schemaRegistry,
                m_schemaRegistry.getModule(JUKEBOX_MODULE_NAME, Revision.of(JB_REVISION)).orElse(null));
        traverser.traverse();
        m_modelNodeHelperRegistry = (ModelNodeHelperRegistry) context.getBean("modelNodeHelperRegistry");
        m_entityRegistry = (EntityRegistry) context.getBean("entityRegistry");
        m_dsmCache = mock(XmlDSMCache.class);
        m_xmlModelNodeToXmlMapper = new XmlModelNodeToXmlMapperImpl(m_dsmCache, m_schemaRegistry, m_modelNodeHelperRegistry, m_subSystemRegistry,
                m_entityRegistry);
        RequestScope.resetScope();
    }

    @Test
    public void testGetXmlModelNode() {
        // Case 1: Jukebox having library as subtree
        Element jukeboxSubtree = TestUtil.loadAsXml(JUKEBOX_SUBTREE_XML);
        XmlModelNodeImpl jukeboxXmlModelNode = new XmlModelNodeImpl(JUKEBOX_SCHEMA_PATH, new HashMap<>(),
                Collections.singletonList(jukeboxSubtree), null, EMPTY_NODE_ID, m_xmlModelNodeToXmlMapper, m_modelNodeHelperRegistry,
                m_schemaRegistry, m_subSystemRegistry, null);

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
        childrenXml.add(librarySubtreeArtist1);
        childrenXml.add(librarySubtreeArtist2);

        return new XmlModelNodeImpl(LIBRARY_SCHEMA_PATH, new HashMap<>(), childrenXml, null, m_jukeboxNodeId,
                m_xmlModelNodeToXmlMapper, m_modelNodeHelperRegistry, m_schemaRegistry, m_subSystemRegistry, null);
    }

    private XmlModelNodeImpl getArtistXmlModelNode(final SchemaPath schemaPath, final Map<QName, ConfigLeafAttribute> attributes, final String... xmlPaths) {
        List<Element> childrenXml = new ArrayList<>();
        for (final String path : xmlPaths) {
            childrenXml.add(TestUtil.loadAsXml(path));
        }

        return new XmlModelNodeImpl(schemaPath, attributes, childrenXml, null, m_libraryNodeId,
                m_xmlModelNodeToXmlMapper, m_modelNodeHelperRegistry, m_schemaRegistry, m_subSystemRegistry, null);
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
        childrenXml.add(albumSubtreeAdmin);
        childrenXml.add(albumSubtreeSong1);
        childrenXml.add(albumSubtreeSong2);

        return new XmlModelNodeImpl(ALBUM_SCHEMA_PATH, configAttributes, childrenXml, null, m_artist1NodeId, m_xmlModelNodeToXmlMapper,
                m_modelNodeHelperRegistry, m_schemaRegistry, m_subSystemRegistry, null);
    }

    @Test
    public void testGetModelNodeFromNodeSchemaPathWithChoiceNode() throws AnnotationAnalysisException {
        Element songwithCDElement = TestUtil.loadAsXml(SONG_WITH_CHOICE_CASE_CD_XML);

        XmlModelNodeImpl albumModelNode = mock(XmlModelNodeImpl.class);
        when(albumModelNode.getSchemaRegistry()).thenReturn(m_schemaRegistry);
        when(albumModelNode.getModelNodeHelperRegistry()).thenReturn(m_modelNodeHelperRegistry);

        XmlModelNodeImpl songWithCDModelNode = m_xmlModelNodeToXmlMapper.getModelNodeFromNodeSchemaPath(songwithCDElement, Collections.emptyMap(), SONG_SCHEMA_PATH,
                m_albumModelNodeId, albumModelNode, null).get(0);
        Map<QName, IndexedList<ModelNodeId, XmlModelNodeImpl>> songWithCDChildrenMap = songWithCDModelNode.getChildren();
        assertEquals(1, songWithCDChildrenMap.size());

        //verify choice case cd modelnode
        IndexedList<ModelNodeId, XmlModelNodeImpl> cdModelNodeList = songWithCDChildrenMap.get(CD_QNAME);
        assertEquals(1, cdModelNodeList.size());
        XmlModelNodeImpl cdModelNode = cdModelNodeList.get(0);
        assertEquals("100", cdModelNode.getAttribute(STOCK_QNAME).getStringValue());

        Element songWithDisketteElement = TestUtil.loadAsXml(SONG_WITH_CHOICE_CASE_DISKETTE_XML);
        XmlModelNodeImpl songWithDisketteModelNode = m_xmlModelNodeToXmlMapper.getModelNodeFromNodeSchemaPath(songWithDisketteElement, Collections.emptyMap(), SONG_SCHEMA_PATH,
                m_albumModelNodeId, albumModelNode, null).get(0);
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
        assertEquals(0, m_xmlModelNodeToXmlMapper.getModelNodeFromNodeSchemaPath(libraryWithoutSongs, Collections.emptyMap(),
                SONG_SCHEMA_PATH,
                m_albumModelNodeId, albumModelNode, null).size());
    }

    @Test
    public void testCacheHit() {
        Element songwithCDElement = TestUtil.loadAsXml(SONG_WITH_CHOICE_CASE_CD_XML);

        XmlModelNodeImpl albumModelNode = mock(XmlModelNodeImpl.class);
        when(albumModelNode.getSchemaRegistry()).thenReturn(m_schemaRegistry);
        when(albumModelNode.getModelNodeHelperRegistry()).thenReturn(m_modelNodeHelperRegistry);
        XmlModelNodeImpl songWithCDModelNode = m_xmlModelNodeToXmlMapper.getModelNodeFromNodeSchemaPath(songwithCDElement, Collections.emptyMap(), SONG_SCHEMA_PATH,
                m_albumModelNodeId, albumModelNode, null).get(0);
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
        assertEquals("<artist xmlns=\"http://example.com/ns/example-jukebox\">\n" +
                "<name>Artist1</name>\n" +
                "<album>\n" +
                "<name>album11</name>\n" +
                "<year>2016</year>\n" +
                "</album>\n" +
                "</artist>", DocumentUtils.documentToPrettyString(DocumentUtils.getChildElement(libraryXml, "artist")).trim());
    }

    @Test
    public void testGetXmlModelNodeChildThePrefixInValueRemainsTheSameForAnIdentityRefBuiltInType() throws Exception {
        m_entityRegistry.updateRegistry("Jukebox", Arrays.<Class>asList(
                Jukebox.class), m_schemaRegistry, null, m_modelNodeDSMRegistry);
        XmlModelNodeImpl artistXmlModelNode = getArtistXmlModelNode(LIBRARY_SCHEMA_PATH, new HashMap<>(), ARTIST_XML_WITH_PREFIX_IN_VALUE_IDENTITY_REF);

        Element libraryXml = m_xmlModelNodeToXmlMapper.getXmlValue(artistXmlModelNode);
        assertEquals("<artist xmlns=\"http://example.com/ns/example-jukebox\">\n" +
                "<name>Artist1</name>\n" +
                "<album>\n" +
                "<name>album322222</name>\n" +
                "<genre2 xmlns:jbox=\"http://example.com/ns/example-jukebox\">jbox:pop</genre2>\n" +
                "</album>\n" +
                "</artist>", DocumentUtils.documentToPrettyString(DocumentUtils.getChildElement(libraryXml, "artist")).trim());
    }

    @Test
    public void testGetXmlModelNodeChildThePrefixInValueFromAnotherNSForAnIdentityRefBuiltInType() throws Exception {
        m_entityRegistry.updateRegistry("Jukebox", Arrays.<Class>asList(
                Jukebox.class), m_schemaRegistry, null, m_modelNodeDSMRegistry);
        XmlModelNodeImpl artistXmlModelNode = getArtistXmlModelNode(LIBRARY_SCHEMA_PATH, new HashMap<>(), ARTIST_XML_WITH_PREFIX_IN_VALUE_FROM_ANOTHER_NS_IDENTITY_REF);

        Element libraryXml = m_xmlModelNodeToXmlMapper.getXmlValue(artistXmlModelNode);
        assertEquals("<artist xmlns=\"http://example.com/ns/example-jukebox\">\n" +
                "<name>Artist1</name>\n" +
                "<album>\n" +
                "<name>album322222</name>\n" +
                "<genre2 xmlns:gn=\"http://example.com/ns/genre2\">gn:base-genre2</genre2>\n" +
                "</album>\n" +
                "</artist>", DocumentUtils.documentToPrettyString(DocumentUtils.getChildElement(libraryXml, "artist")).trim());
    }

    @Test
    public void testGetXmlModelNodeChildThePrefixInValueRemainsTheSameForAnInstanceIdentifierBuiltInType() throws Exception {
        m_entityRegistry.updateRegistry("Jukebox", Arrays.<Class>asList(
                Jukebox.class), m_schemaRegistry, null, m_modelNodeDSMRegistry);
        XmlModelNodeImpl artistXmlModelNode = getArtistXmlModelNode(LIBRARY_SCHEMA_PATH, new HashMap<>(), ARTIST_XML_WITH_PREFIX_IN_VALUE_INSTANCE_IDENTIFIER);

        Element libraryXml = m_xmlModelNodeToXmlMapper.getXmlValue(artistXmlModelNode);
        assertEquals("<artist xmlns=\"http://example.com/ns/example-jukebox\">\n" +
                "<name>Artist1</name>\n" +
                "<mgmt-jukebox xmlns:jbox=\"http://example.com/ns/example-jukebox\">/jbox:jukebox/jbox:library/jbox:artist/jbox:album/jbox:name</mgmt-jukebox>\n" +
                "<album>\n" +
                "<name>album1</name>\n" +
                "</album>\n" +
                "<album>\n" +
                "<name>album2</name>\n" +
                "</album>\n" +
                "</artist>", DocumentUtils.documentToPrettyString(DocumentUtils.getChildElement(libraryXml, "artist")).trim());
    }

    @Test
    public void testGetXmlModelNodeChildThePrefixInValueFromAnotherNSForAnInstanceIdentifierBuiltInType () throws Exception {
        m_entityRegistry.updateRegistry("Jukebox", Arrays.<Class>asList(
                Jukebox.class), m_schemaRegistry, null, m_modelNodeDSMRegistry);
        XmlModelNodeImpl artistXmlModelNode = getArtistXmlModelNode(LIBRARY_SCHEMA_PATH, new HashMap<>(), ARTIST_XML_WITH_PREFIX_IN_VALUE_FROM_ANOTHER_NS_INSTANCE_IDENTIFIER);

        Element libraryXml = m_xmlModelNodeToXmlMapper.getXmlValue(artistXmlModelNode);
        assertEquals("<artist xmlns=\"http://example.com/ns/example-jukebox\">\n" +
                "<name>Artist1</name>\n" +
                "<mgmt-jukebox xmlns:mgb=\"http://example.com/ns/mgmt-jukebox\">/mgb:mgmt-jukebox-1/mgb:mgmt-jukebox-2/mgb:name</mgmt-jukebox>\n" +
                "<album>\n" +
                "<name>album1</name>\n" +
                "</album>\n" +
                "<album>\n" +
                "<name>album2</name>\n" +
                "</album>\n" +
                "</artist>", DocumentUtils.documentToPrettyString(DocumentUtils.getChildElement(libraryXml, "artist")).trim());
    }

    @Test
    public void testGetXmlModelNodeChildThePrefixInValueWithMultiplenameSpacesForAnInstanceIdentifierBuiltInType () throws Exception {
        m_entityRegistry.updateRegistry("Jukebox", Arrays.<Class>asList(
                Jukebox.class), m_schemaRegistry, null, m_modelNodeDSMRegistry);
        XmlModelNodeImpl artistXmlModelNode = getArtistXmlModelNode(LIBRARY_SCHEMA_PATH, new HashMap<>(), ARTIST_XML_WITH_PREFIX_IN_VALUE_FROM_MULTIPLE_NS_INSTANCE_IDENTIFIER);

        Element libraryXml = m_xmlModelNodeToXmlMapper.getXmlValue(artistXmlModelNode);
        assertEquals("<artist xmlns=\"http://example.com/ns/example-jukebox\">\n" +
                "<name>Artist1</name>\n" +
                "<mgmt-jukebox xmlns:jbox=\"http://example.com/ns/example-jukebox\" xmlns:mgb=\"http://example.com/ns/mgmt-jukebox\" xmlns:mgb1=\"http://example.com/ns/mgmt-jukebox-1\">\n" +
                "        /jbox:xyz/mgb:abc/mgb1:lmn/mgb1:name</mgmt-jukebox>\n" +
                "<album>\n" +
                "<name>album1</name>\n" +
                "</album>\n" +
                "<album>\n" +
                "<name>album2</name>\n" +
                "</album>\n" +
                "</artist>", DocumentUtils.documentToPrettyString(DocumentUtils.getChildElement(libraryXml, "artist")).trim());
    }

}