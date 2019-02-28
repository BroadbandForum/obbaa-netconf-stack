package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn;

import static org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.TestConstants.EMPTY_NODE_ID;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn.CONTAINER;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ALBUM_LOCAL_NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ALBUM_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ARTIST_LOCAL_NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ARTIST_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.GENRE_QNAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JB_NS;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JB_REVISION;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JUKEBOX_LOCAL_NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JUKEBOX_MODULE_NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JUKEBOX_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.LABEL_QNAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.LIBRARY_LOCAL_NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.LIBRARY_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.NAME_QNAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.RESOURCE_QNAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.SINGER_LOCAL_NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.SINGER_QNAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.YEAR;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.YEAR_QNAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.GenericConfigAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.IdentityRefConfigAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.InstanceIdentifierConfigAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.xml.sax.SAXException;

import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder;
import org.broadband_forum.obbaa.netconf.api.util.SchemaPathUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.jukeboxwithconfigattr.Album;
import org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.jukeboxwithconfigattr.Artist;
import org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.jukeboxwithconfigattr.DummyLeaflistIdRef;
import org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.jukeboxwithconfigattr.Jukebox;
import org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.jukeboxwithconfigattr.Library;
import org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.jukeboxwithconfigattr.Singer;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryTraverser;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryVisitor;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.DataStoreException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDSMRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeKey;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.utils.AnnotationAnalysisException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.utils.EntityRegistryBuilder;
import org.broadband_forum.obbaa.netconf.persistence.EntityDataStoreManager;
import org.broadband_forum.obbaa.netconf.persistence.PersistenceManagerUtil;

/**
 * Created by sgs on 2/1/17.
 */
public class ConfigAttributeAnnotatedDSMTest {

    public static final String TEST_APPLICATION_CONTEXT_XML = "/configattributetest/test-applicationContext.xml";
    public static final String JUKEBOX_TYPES_NS = "http://example.com/ns/example-jukebox-types";
    public static final String JB_TYPES_PREFIX = "ejt";
    public static final String XMLNS_JBOX = "xmlns:jbox";
    public static final String XMLNS_EJT = "xmlns:ejt";
    public static final String ARTIST_NAME = "Artist";
    public static final String ALBUM_NAME = "album";

    private AbstractApplicationContext m_context;
    private AggregatedDSM m_aggregatedDSM;
    private EntityRegistry m_entityRegistry;
    private PersistenceManagerUtil m_persistenceManagerUtil;
    private EntityModelNodeHelperDeployer m_entityModelNodeHelperDeployer;
    private SchemaRegistry m_schemaRegistry;
    private ModelNodeHelperRegistry m_modelNodeHelperRegistry;
    private SubSystemRegistry m_subSystemRegistry;
    private ModelNodeDataStoreManager m_annotationDSM;
    private ModelNodeDSMRegistry m_modelNodeDSMRegistry;

    public static final QName DUMMY_LEAFLIST_IDREF_QNAME = QName.create(JB_NS,JB_REVISION,"dummy-leaflist-id-ref");
    public static final SchemaPath SINGER_SCHEMA_PATH = new SchemaPathBuilder().withParent(ALBUM_SCHEMA_PATH).
            appendLocalName(SINGER_LOCAL_NAME).build();
    public static final SchemaPath DUMMY_LEAFLIST_SCHEMA_PATH = new SchemaPathBuilder().withParent(ALBUM_SCHEMA_PATH).
            appendLocalName("dummy-leaflist-id-ref").build();

    private ModelNodeId m_jukeboxNodeId = new ModelNodeId().addRdn(new ModelNodeRdn(CONTAINER, JB_NS, JUKEBOX_LOCAL_NAME));
    private ModelNodeId m_libraryNodeId = new ModelNodeId(m_jukeboxNodeId).addRdn(new ModelNodeRdn(CONTAINER, JB_NS, LIBRARY_LOCAL_NAME));
    private ModelNodeId m_artistNodeId = new ModelNodeId(m_libraryNodeId).addRdn(new ModelNodeRdn(CONTAINER, JB_NS, ARTIST_LOCAL_NAME))
            .addRdn(new ModelNodeRdn(NAME_QNAME, ARTIST_NAME));
    private ModelNodeId m_albumNodeId = new ModelNodeId(m_artistNodeId).addRdn(new ModelNodeRdn(CONTAINER, JB_NS, ALBUM_LOCAL_NAME))
            .addRdn(new ModelNodeRdn(NAME_QNAME, ALBUM_NAME));

    @Before
    public void setUp() {

        m_context = new ClassPathXmlApplicationContext(TEST_APPLICATION_CONTEXT_XML);

        m_entityRegistry = (EntityRegistry) m_context.getBean("entityRegistry");
        m_persistenceManagerUtil = (PersistenceManagerUtil) m_context.getBean("persistenceManagerUtil");
        m_entityModelNodeHelperDeployer = (EntityModelNodeHelperDeployer) m_context.getBean("entityModelNodeHelperDeployer");
        m_schemaRegistry = (SchemaRegistry) m_context.getBean("schemaRegistry");
        m_modelNodeHelperRegistry = (ModelNodeHelperRegistry) m_context.getBean("modelNodeHelperRegistry");
        m_subSystemRegistry = (SubSystemRegistry) m_context.getBean("subSystemRegistry");
        SchemaRegistryTraverser traverser = new SchemaRegistryTraverser("JukeBox",
                Collections.singletonList((SchemaRegistryVisitor) m_entityModelNodeHelperDeployer), m_schemaRegistry,
                m_schemaRegistry.getModule(JUKEBOX_MODULE_NAME, Revision.of(JB_REVISION)).orElse(null));
        traverser.traverse();
        m_aggregatedDSM = (AggregatedDSM) m_context.getBean("aggregatedDSM");
        m_annotationDSM = (ModelNodeDataStoreManager) m_context.getBean("annotationBasedDSM");
        m_modelNodeDSMRegistry = (ModelNodeDSMRegistry) m_context.getBean("modelNodeDSMRegistry");
        SchemaRegistryUtil.resetCache();
    }

    @Test
    public void testDSMReads() throws AnnotationAnalysisException, DataStoreException, ParserConfigurationException, SAXException, IOException {

        initialiseJukeboxEntity();
        assertEquals(1, m_aggregatedDSM.listNodes(JUKEBOX_SCHEMA_PATH).size());
        assertEquals(1, m_aggregatedDSM.listNodes(LIBRARY_SCHEMA_PATH).size());
        assertEquals(1, m_aggregatedDSM.listNodes(ARTIST_SCHEMA_PATH).size());
        assertEquals(1, m_aggregatedDSM.listNodes(ALBUM_SCHEMA_PATH).size());

        assertEquals(1, m_aggregatedDSM.listChildNodes(JUKEBOX_SCHEMA_PATH, EMPTY_NODE_ID).size());
        assertEquals(1, m_aggregatedDSM.listChildNodes(LIBRARY_SCHEMA_PATH, m_jukeboxNodeId).size());
        assertEquals(1, m_aggregatedDSM.listChildNodes(ARTIST_SCHEMA_PATH,m_libraryNodeId).size());
        assertEquals(1, m_aggregatedDSM.listChildNodes(ALBUM_SCHEMA_PATH,m_artistNodeId).size());

        Map<QName, String> keys = new LinkedHashMap<>();
        keys.put(NAME_QNAME,ALBUM_NAME);
        ModelNodeKey modelNodeKey = new ModelNodeKey(keys);
        ModelNodeWithAttributes albumModelNode = (ModelNodeWithAttributes) m_aggregatedDSM.findNode(ALBUM_SCHEMA_PATH,modelNodeKey,m_artistNodeId);

        verifyAlbumAttributes(albumModelNode);

        // Verify findNodes with match criteria work
        HashMap<QName, ConfigLeafAttribute> matchAttributes = new HashMap<>();
        matchAttributes.put(YEAR_QNAME, new GenericConfigAttribute(YEAR, JB_NS, "1948"));
        matchAttributes.put(GENRE_QNAME, albumModelNode.getAttribute(GENRE_QNAME));

        albumModelNode = (ModelNodeWithAttributes) m_aggregatedDSM.findNodes(ALBUM_SCHEMA_PATH,matchAttributes,m_artistNodeId).get(0);
        verifyAlbumAttributes(albumModelNode);

        //Verify findNode with match criteria works with just key
        matchAttributes = new HashMap<>();
        matchAttributes.put(NAME_QNAME, new GenericConfigAttribute(NAME, JB_NS, ALBUM_NAME));

        albumModelNode = (ModelNodeWithAttributes) m_aggregatedDSM.findNodes(ALBUM_SCHEMA_PATH,matchAttributes,m_artistNodeId).get(0);
        verifyAlbumAttributes(albumModelNode);

    }

    @Test
    public void testDSMWrites() throws DataStoreException, AnnotationAnalysisException, ParserConfigurationException, SAXException,
            IOException {
        initialiseJukeboxEntity();
        assertEquals(1, m_aggregatedDSM.listNodes(JUKEBOX_SCHEMA_PATH).size());
        assertEquals(1, m_aggregatedDSM.listNodes(LIBRARY_SCHEMA_PATH).size());
        assertEquals(1, m_aggregatedDSM.listNodes(ARTIST_SCHEMA_PATH).size());
        assertEquals(1, m_aggregatedDSM.listNodes(ALBUM_SCHEMA_PATH).size());

        ModelNodeWithAttributes albumToBeCreated = new ModelNodeWithAttributes(ALBUM_SCHEMA_PATH,m_artistNodeId,
                m_modelNodeHelperRegistry,m_subSystemRegistry,m_schemaRegistry,m_annotationDSM);

        // create
        Map<QName,ConfigLeafAttribute> configAttributes = new HashMap<>();
        configAttributes.put(NAME_QNAME, new GenericConfigAttribute(NAME, JB_NS, "newalbum"));
        configAttributes.put(GENRE_QNAME,new IdentityRefConfigAttribute(JB_NS,"jbox",
                DocumentUtils.getDocumentElement("<genre xmlns:jbox=\"http://example.com/ns/example-jukebox\">jbox:jazz</genre>")));

        Map<String, String> nsPrefixMap = new HashMap<>();
        nsPrefixMap.put(JB_NS,"jbox");
        nsPrefixMap.put(JUKEBOX_TYPES_NS,"ejt");
        configAttributes.put(RESOURCE_QNAME,new InstanceIdentifierConfigAttribute(nsPrefixMap, JB_NS, "resource","jbox:jukebox/ejt:dummyleaf"
        ));
        albumToBeCreated.setAttributes(configAttributes);

        Map<QName,LinkedHashSet<ConfigLeafAttribute>> leafListAttributesMap = new HashMap<>();
        LinkedHashSet<ConfigLeafAttribute> singerLeafList = new LinkedHashSet<>();
        singerLeafList.add(new GenericConfigAttribute(SINGER_LOCAL_NAME, JB_NS, "singer3"));
        leafListAttributesMap.put(SINGER_QNAME, singerLeafList);

        LinkedHashSet<ConfigLeafAttribute> dummyleafList = new LinkedHashSet<>();
        dummyleafList.add(new IdentityRefConfigAttribute(JB_NS,"jbox","jbox:dummy-leaflist-id-ref",
                "jbox:dummy-id-ref3",JB_NS));
        leafListAttributesMap.put(DUMMY_LEAFLIST_IDREF_QNAME,dummyleafList);

        albumToBeCreated.setLeafLists(leafListAttributesMap);
        m_annotationDSM.createNode(albumToBeCreated,m_artistNodeId);

        // find the created modelnode and make assertions
        Map<QName, String> keys = new LinkedHashMap<>();
        keys.put(NAME_QNAME,"newalbum");
        ModelNodeKey modelNodeKey = new ModelNodeKey(keys);
        ModelNodeWithAttributes newAlbumModelNode = (ModelNodeWithAttributes) m_aggregatedDSM.findNode(ALBUM_SCHEMA_PATH,modelNodeKey,m_artistNodeId);

        assertEquals("newalbum", newAlbumModelNode.getAttribute(NAME_QNAME).getStringValue());
        assertEquals(JB_NS,getNodeValue(newAlbumModelNode.getAttribute(GENRE_QNAME), XMLNS_JBOX));
        assertEquals("jbox:jazz",newAlbumModelNode.getAttribute(GENRE_QNAME).getStringValue());
        assertEquals("jbox:jukebox/ejt:dummyleaf",newAlbumModelNode.getAttribute(RESOURCE_QNAME).getStringValue());
        assertEquals(JB_NS,getNodeValue(newAlbumModelNode.getAttribute(RESOURCE_QNAME), XMLNS_JBOX));
        assertEquals(JUKEBOX_TYPES_NS,getNodeValue(newAlbumModelNode.getAttribute(RESOURCE_QNAME), XMLNS_EJT));
        assertEquals(2, newAlbumModelNode.getLeafLists().size());
        assertEquals(1, newAlbumModelNode.getLeafList(SINGER_QNAME).size());
        assertEquals(1, newAlbumModelNode.getLeafList(DUMMY_LEAFLIST_IDREF_QNAME).size());
        assertEquals(2, m_aggregatedDSM.listNodes(ALBUM_SCHEMA_PATH).size());

        //update the new modelnode created
        configAttributes = new HashMap<>();
        configAttributes.put(YEAR_QNAME,new GenericConfigAttribute(YEAR, JB_NS, "1999"));
        configAttributes.put(GENRE_QNAME,new IdentityRefConfigAttribute(JUKEBOX_TYPES_NS, JB_TYPES_PREFIX,
                DocumentUtils.getDocumentElement("<genre xmlns:ejt=\"http://example.com/ns/example-jukebox-types\">ejt:country</genre>")));
        nsPrefixMap.remove(JB_NS);
        configAttributes.put(RESOURCE_QNAME,new InstanceIdentifierConfigAttribute(nsPrefixMap, JB_NS, "resource",
                "ejt:dummycontainer/ejt:dummyleaf"
        ));

        singerLeafList.add(new GenericConfigAttribute(SINGER_LOCAL_NAME, JB_NS, "singer4"));
        leafListAttributesMap.put(SINGER_QNAME,singerLeafList);

        IdentityRefConfigAttribute secondIdRefLeafListValue = new IdentityRefConfigAttribute(JUKEBOX_TYPES_NS, "ejt",
                "jbox:dummy-leaflist-id-ref",
                "ejt:dummy-id-ref1", JB_NS);
        dummyleafList.add(secondIdRefLeafListValue);
        leafListAttributesMap.put(DUMMY_LEAFLIST_IDREF_QNAME,dummyleafList);

        newAlbumModelNode.setLeafLists(leafListAttributesMap);
        m_annotationDSM.updateNode(newAlbumModelNode,m_artistNodeId,configAttributes, leafListAttributesMap, false);

        // find the updated modelnode and make assertions
        ModelNodeWithAttributes updatedAlbumNode = (ModelNodeWithAttributes) m_aggregatedDSM.findNode(ALBUM_SCHEMA_PATH,modelNodeKey,
                m_artistNodeId);

        assertEquals("newalbum", updatedAlbumNode.getAttribute(NAME_QNAME).getStringValue());
        assertEquals(JUKEBOX_TYPES_NS, getNodeValue(updatedAlbumNode.getAttribute(GENRE_QNAME), XMLNS_EJT));
        assertEquals(JUKEBOX_TYPES_NS, getNodeValue(updatedAlbumNode.getAttribute(RESOURCE_QNAME), XMLNS_EJT));
        assertEquals("ejt:dummycontainer/ejt:dummyleaf", updatedAlbumNode.getAttribute(RESOURCE_QNAME).getStringValue());
        assertEquals(2, updatedAlbumNode.getLeafList(SINGER_QNAME).size());
        assertEquals(2, updatedAlbumNode.getLeafList(DUMMY_LEAFLIST_IDREF_QNAME).size());

        // remove leaf-list
        singerLeafList.remove(new GenericConfigAttribute(SINGER_LOCAL_NAME, JB_NS, "singer4"));
        dummyleafList.remove(secondIdRefLeafListValue);
        updatedAlbumNode.setLeafLists(leafListAttributesMap);
        m_aggregatedDSM.updateNode(updatedAlbumNode,m_artistNodeId,configAttributes,leafListAttributesMap, false);
        assertEquals(1, updatedAlbumNode.getLeafList(SINGER_QNAME).size());
        assertEquals(1, updatedAlbumNode.getLeafList(DUMMY_LEAFLIST_IDREF_QNAME).size());

        //remove all
        assertEquals(1, m_aggregatedDSM.listNodes(JUKEBOX_SCHEMA_PATH).size());
        assertEquals(1, m_aggregatedDSM.listNodes(LIBRARY_SCHEMA_PATH).size());
        assertEquals(1, m_aggregatedDSM.listNodes(ARTIST_SCHEMA_PATH).size());
        assertEquals(2, m_aggregatedDSM.listNodes(ALBUM_SCHEMA_PATH).size());
        keys = new LinkedHashMap<>();
        keys.put(NAME_QNAME,ARTIST_NAME);
        modelNodeKey = new ModelNodeKey(keys);
        ModelNodeWithAttributes artistModelNode = (ModelNodeWithAttributes) m_aggregatedDSM.findNode(ARTIST_SCHEMA_PATH,modelNodeKey,m_libraryNodeId);
        m_aggregatedDSM.removeAllNodes(artistModelNode,ALBUM_SCHEMA_PATH,m_libraryNodeId);

        assertEquals(1, m_aggregatedDSM.listNodes(JUKEBOX_SCHEMA_PATH).size());
        assertEquals(1, m_aggregatedDSM.listNodes(LIBRARY_SCHEMA_PATH).size());
        assertEquals(1, m_aggregatedDSM.listNodes(ARTIST_SCHEMA_PATH).size());
        assertEquals(0, m_aggregatedDSM.listNodes(ALBUM_SCHEMA_PATH).size());

        // remove
        m_aggregatedDSM.removeNode(artistModelNode,m_libraryNodeId);
        assertEquals(0, m_aggregatedDSM.listNodes(ARTIST_SCHEMA_PATH).size());

        ModelNodeWithAttributes libraryModelNode = (ModelNodeWithAttributes) m_aggregatedDSM.findNode(LIBRARY_SCHEMA_PATH,new
                ModelNodeKey(new LinkedHashMap<>()),m_jukeboxNodeId);
        m_aggregatedDSM.removeNode(libraryModelNode,m_jukeboxNodeId);
        assertEquals(0, m_aggregatedDSM.listNodes(LIBRARY_SCHEMA_PATH).size());
    }

    private void verifyAlbumAttributes(ModelNodeWithAttributes albumModelNode) throws SAXException, IOException, ParserConfigurationException {
        Map<QName,ConfigLeafAttribute> albumAttributes = albumModelNode.getAttributes();

        assertTrue(albumAttributes.get(NAME_QNAME) instanceof GenericConfigAttribute);
        assertEquals(ALBUM_NAME,albumAttributes.get(NAME_QNAME).getStringValue());

        assertTrue(albumAttributes.get(GENRE_QNAME) instanceof IdentityRefConfigAttribute);
        assertEquals(JUKEBOX_TYPES_NS,getNodeValue(albumAttributes.get(GENRE_QNAME), XMLNS_EJT));
        assertEquals("ejt:blues",albumAttributes.get(GENRE_QNAME).getStringValue());

        assertTrue(albumAttributes.get(LABEL_QNAME) instanceof IdentityRefConfigAttribute);
        assertEquals(JB_NS,getNodeValue(albumAttributes.get(LABEL_QNAME), XMLNS_JBOX));
        assertEquals("jbox:indie",albumAttributes.get(LABEL_QNAME).getStringValue());

        assertTrue(albumAttributes.get(RESOURCE_QNAME) instanceof InstanceIdentifierConfigAttribute);
        assertEquals(JUKEBOX_TYPES_NS, getNodeValue(albumAttributes.get(RESOURCE_QNAME), XMLNS_EJT));
        assertEquals("ejt:dummycontainer/ejt:dummyleaf",albumAttributes.get(RESOURCE_QNAME).getStringValue());

        Set<ConfigLeafAttribute> singerLeafList = albumModelNode.getLeafList(SINGER_QNAME);
        assertEquals(2, singerLeafList.size());
        Iterator<ConfigLeafAttribute> singerIterator = singerLeafList.iterator();
        while (singerIterator.hasNext()){
            assertTrue(singerIterator.next() instanceof GenericConfigAttribute);
        }

        Set<ConfigLeafAttribute> dummyLeafListIdRef = albumModelNode.getLeafList(DUMMY_LEAFLIST_IDREF_QNAME);
        assertEquals(2,dummyLeafListIdRef.size());
        Iterator<ConfigLeafAttribute> dummyLeafListIdRefIter = dummyLeafListIdRef.iterator();
        while (dummyLeafListIdRefIter.hasNext()){
            ConfigLeafAttribute next = dummyLeafListIdRefIter.next();
            assertTrue(next instanceof IdentityRefConfigAttribute);
        }
    }

    private void initialiseJukeboxEntity() throws AnnotationAnalysisException {
        List<Class> classes = new ArrayList<>();
        classes.add(Jukebox.class);
        EntityRegistryBuilder.updateEntityRegistry(JUKEBOX_LOCAL_NAME, classes, m_entityRegistry, m_schemaRegistry, m_persistenceManagerUtil.getEntityDataStoreManager(), m_modelNodeDSMRegistry);
        ModelNodeDSMRegistry modelNodeDSMRegistry = (ModelNodeDSMRegistry) m_context.getBean("modelNodeDSMRegistry");
        modelNodeDSMRegistry.register(JUKEBOX_LOCAL_NAME, JUKEBOX_SCHEMA_PATH, m_annotationDSM);
        modelNodeDSMRegistry.register(JUKEBOX_LOCAL_NAME, LIBRARY_SCHEMA_PATH, m_annotationDSM);
        modelNodeDSMRegistry.register(JUKEBOX_LOCAL_NAME, ARTIST_SCHEMA_PATH, m_annotationDSM);
        modelNodeDSMRegistry.register(JUKEBOX_LOCAL_NAME, ALBUM_SCHEMA_PATH, m_annotationDSM);
        createJukebox();
    }

    private void createJukebox() {
        Jukebox jukebox = new Jukebox();
        jukebox.setParentId(EMPTY_NODE_ID.getModelNodeIdAsString());
        jukebox.setSchemaPath(SchemaPathUtil.toStringNoRev(JUKEBOX_SCHEMA_PATH));

        Library library = new Library();
        library.setParentId(m_jukeboxNodeId.getModelNodeIdAsString());
        library.setSchemaPath(SchemaPathUtil.toStringNoRev(LIBRARY_SCHEMA_PATH));

        Artist artist = new Artist();
        artist.setName(ARTIST_NAME);
        artist.setParentId(m_libraryNodeId.getModelNodeIdAsString());
        artist.setSchemaPath(SchemaPathUtil.toStringNoRev(ARTIST_SCHEMA_PATH));

        Set<Album> albums = new HashSet<>();
        Album album = new Album();
        album.setName(ALBUM_NAME);
        album.setParentId(m_artistNodeId.getModelNodeIdAsString());
        album.setSchemaPath(SchemaPathUtil.toStringNoRev(ALBUM_SCHEMA_PATH));

        // leaf identity ref values
        album.setGenre("ejt:blues");
        album.setGenreNS(JUKEBOX_TYPES_NS);
        album.setLabel("jbox:indie");
        album.setLabelNS(JB_NS);
        album.setYear("1948");

        // leaf instance identifier value
        album.setResourceNS(JB_TYPES_PREFIX+" " +JUKEBOX_TYPES_NS);
        album.setResource("ejt:dummycontainer/ejt:dummyleaf");
        albums.add(album);
        artist.setAlbums(albums);

        // leaf-list with string value
        Set<Singer> singers = new HashSet<>();
        Singer singer1 = new Singer();
        singer1.setParentId(m_albumNodeId.getModelNodeIdAsString());
        singer1.setSchemaPath(SchemaPathUtil.toStringNoRev(SINGER_SCHEMA_PATH));
        singer1.setSinger("singer1");

        Singer singer2 = new Singer();
        singer2.setParentId(m_albumNodeId.getModelNodeIdAsString());
        singer2.setSchemaPath(SchemaPathUtil.toStringNoRev(SINGER_SCHEMA_PATH));
        singer2.setSinger("singer2");

        singers.add(singer1);
        singers.add(singer2);
        album.setSingers(singers);

        //leaf-list with identityRef values
        Set<DummyLeaflistIdRef> dummyLeaflistIdRefs = new HashSet<>();
        DummyLeaflistIdRef dummyLeaflistIdRef1 = new DummyLeaflistIdRef();
        dummyLeaflistIdRef1.setParentId(m_albumNodeId.getModelNodeIdAsString());
        dummyLeaflistIdRef1.setSchemaPath(SchemaPathUtil.toStringNoRev(DUMMY_LEAFLIST_SCHEMA_PATH));
        dummyLeaflistIdRef1.setDummyLeaflistIdRef("ejt:dummy-id-ref1");
        dummyLeaflistIdRef1.setDummyLeaflistIdRefNs(JUKEBOX_TYPES_NS);

        DummyLeaflistIdRef dummyLeaflistIdRef2 = new DummyLeaflistIdRef();
        dummyLeaflistIdRef2.setParentId(m_albumNodeId.getModelNodeIdAsString());
        dummyLeaflistIdRef2.setSchemaPath(SchemaPathUtil.toStringNoRev(DUMMY_LEAFLIST_SCHEMA_PATH));
        dummyLeaflistIdRef2.setDummyLeaflistIdRef("jbox:dummy-id-ref3");
        dummyLeaflistIdRef2.setDummyLeaflistIdRefNs(JB_NS);
        dummyLeaflistIdRefs.add(dummyLeaflistIdRef1);
        dummyLeaflistIdRefs.add(dummyLeaflistIdRef2);
        album.setDummyLeaflistIdRef(dummyLeaflistIdRefs);

        List<Artist> artists = new ArrayList<>();
        artists.add(artist);
        library.setArtists(artists);
        jukebox.setLibrary(library);

        EntityDataStoreManager enityDataStoreManager = m_persistenceManagerUtil.getEntityDataStoreManager();
        try {
            enityDataStoreManager.create(jukebox);
        } catch (Exception e) {
            throw e;
        }
    }

    private String getNodeValue(ConfigLeafAttribute configLeafAttribute,String prefix) {
        return configLeafAttribute.getDOMValue().getAttributes().getNamedItem(prefix).getNodeValue();
    }
}