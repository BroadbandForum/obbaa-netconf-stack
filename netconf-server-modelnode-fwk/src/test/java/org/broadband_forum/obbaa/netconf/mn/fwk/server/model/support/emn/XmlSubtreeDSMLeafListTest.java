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
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JB_REVISION;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.SINGER_LOCAL_NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.SONG_LOCAL_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder;
import org.broadband_forum.obbaa.netconf.api.util.SchemaPathUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryTraverser;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDSMRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.utils.EntityRegistryBuilder;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.jukebox4.Album;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.jukebox4.Genre;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.jukebox4.Singer;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.jukebox4.Song;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.GenericConfigAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.persistence.EntityDataStoreManager;
import org.broadband_forum.obbaa.netconf.persistence.PersistenceManagerUtil;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.w3c.dom.Document;

@RunWith(RequestScopeJunitRunner.class)
public class XmlSubtreeDSMLeafListTest {

    private static final String TEST_APPLICATION_CONTEXT_XML = "/xmlsubtreedsmtest/test-applicationContext.xml";
    private static final String NAME = "name";
    private static final String VERSION = "version";
    private static final String GENRE_LOCAL_NAME = "genre";
    private static final String AWARDS_AND_NOMINATIONS = "awards-and-nominations";
    public static final String NS = "http://example.com/ns/example-leaflist";
    private static final SchemaPath ALBUM_SCHEMA_PATH = SchemaPath.create(true, QName.create(NS, JB_REVISION, ALBUM_LOCAL_NAME));
    private static final SchemaPath SONG_SCHEMA_PATH = new SchemaPathBuilder().withParent(ALBUM_SCHEMA_PATH).appendLocalName(SONG_LOCAL_NAME).build();
    private static final SchemaPath AWARDS_AND_NOMINATIONS_SCHEMA_PATH = new SchemaPathBuilder().withParent(SONG_SCHEMA_PATH).appendLocalName(AWARDS_AND_NOMINATIONS).build();
    private static final SchemaPath AWARD = new SchemaPathBuilder().withParent(AWARDS_AND_NOMINATIONS_SCHEMA_PATH).appendLocalName("award").build();
    private static final SchemaPath SINGER_SCHEMA_PATH = new SchemaPathBuilder().withParent(SONG_SCHEMA_PATH).appendLocalName(SINGER_LOCAL_NAME).build();
    private static final SchemaPath GENRE_SCHEMA_PATH = new SchemaPathBuilder().withParent(SONG_SCHEMA_PATH).appendLocalName(GENRE_LOCAL_NAME).build();
    private static final SchemaPath SONG_NAME_SCHEMA_PATH = new SchemaPathBuilder().withParent(SONG_SCHEMA_PATH).appendLocalName(NAME).build();
    private static final QName NAME_QNAME = QName.create(NS, JB_REVISION, NAME);
    private static final QName VERSION_QNAME = QName.create(NS, JB_REVISION, VERSION);
    private static final ModelNodeId ALBUM_MODEL_NODE_ID = new ModelNodeId().addRdn(new ModelNodeRdn(CONTAINER, NS, ALBUM_LOCAL_NAME));
    private final ModelNodeId SONG_MODEL_NODE_ID = new ModelNodeId(ALBUM_MODEL_NODE_ID).addRdn(new ModelNodeRdn(CONTAINER, NS, SONG_LOCAL_NAME))
            .addRdn(new ModelNodeRdn(NAME_QNAME, "See you again")).addRdn(new ModelNodeRdn(VERSION_QNAME, "1"));
    public static final QName SINGER_QNAME = QName.create(NS,JB_REVISION,SINGER_LOCAL_NAME);
    public static final QName GENRE_QNAME = QName.create(NS,JB_REVISION,GENRE_LOCAL_NAME);

    private XmlDSMCache m_dsmCache = new RequestScopeXmlDSMCache();
    private ModelNodeDataStoreManager m_aggregatedDSM;
    private EntityRegistry m_entityRegistry;
    private PersistenceManagerUtil m_persistenceManagerUtil;
    private EntityModelNodeHelperDeployer m_entityModelNodeHelperDeployer;
    private SchemaRegistry m_schemaRegistry;
    private SubSystemRegistry m_subSystemRegistry;
    private ModelNodeHelperRegistry m_modelNodeHelperRegistry;
    private XmlModelNodeToXmlMapper m_xmlModelNodeToXmlMapper;
    private ModelNodeDataStoreManager m_xmlSubtreeDSM;
    private AbstractApplicationContext m_context;
    private ModelNodeDSMRegistry m_modelNodeDSMRegistry;
    private Document m_document = DocumentUtils.createDocument();

    @Before
    public void setUp() {
        m_context = new ClassPathXmlApplicationContext(TEST_APPLICATION_CONTEXT_XML);

        m_entityRegistry = (EntityRegistry) m_context.getBean("entityRegistry");
        m_persistenceManagerUtil = (PersistenceManagerUtil) m_context.getBean("persistenceManagerUtil");
        m_entityModelNodeHelperDeployer = (EntityModelNodeHelperDeployer) m_context.getBean("entityModelNodeHelperDeployer");
        m_schemaRegistry = (SchemaRegistry) m_context.getBean("schemaRegistry");
        m_subSystemRegistry = (SubSystemRegistry) m_context.getBean("subSystemRegistry");
        m_modelNodeHelperRegistry = (ModelNodeHelperRegistry) m_context.getBean("modelNodeHelperRegistry");
        m_modelNodeDSMRegistry = (ModelNodeDSMRegistry) m_context.getBean("modelNodeDSMRegistry");
        m_xmlModelNodeToXmlMapper = new XmlModelNodeToXmlMapperImpl(m_dsmCache, m_schemaRegistry, m_modelNodeHelperRegistry, m_subSystemRegistry,
                m_entityRegistry, null);
        m_xmlSubtreeDSM = (ModelNodeDataStoreManager) m_context.getBean("xmlSubtreeDSM");
        m_aggregatedDSM = (AggregatedDSM) m_context.getBean("aggregatedDSM");
        m_document = DocumentUtils.createDocument();
    }


    @Test
    public void testFindNodesWithLeafListAndXmlSubtree1() throws Exception {
        initAlbumWithSongWithLeafListAndXmlSubtree();
        createAlbumWithSongWithLeafListAndXmlSubtree(true, true);
        HashMap<QName, ConfigLeafAttribute> configAttributes = new HashMap<>();

        //Match criteria with only the song name key
        configAttributes.put(NAME_QNAME, new GenericConfigAttribute(NAME, NS, "See you again"));

        List<ModelNode> songs = m_aggregatedDSM.findNodes(SONG_SCHEMA_PATH, configAttributes, ALBUM_MODEL_NODE_ID, m_schemaRegistry);

        verifySongXmlModelNode(songs.get(0));

        List<ModelNode> awards = m_aggregatedDSM.findNodes(AWARDS_AND_NOMINATIONS_SCHEMA_PATH, new HashMap<>(), SONG_MODEL_NODE_ID, m_schemaRegistry);

        //find awards leaf list in awards and nominations
        assertEquals(1, awards.get(0).getLeafLists().size());
        QName key = awards.get(0).getLeafLists().keySet().stream().findFirst().get();
        assertEquals(2, awards.get(0).getLeafLists().get(key).size());
        Iterator<ConfigLeafAttribute> leafListElements = awards.get(0).getLeafLists().get(key).iterator();
        assertEquals("American Music Awards", leafListElements.next().getStringValue());
        assertEquals("MTV Video Music Awards", leafListElements.next().getStringValue());
    }

    @Test
    public void testFindNodesWithLeafListAndXmlSubtree2() throws Exception {
        initAlbumWithSongWithLeafListAndXmlSubtree();
        createAlbumWithSongWithLeafListAndXmlSubtree(true, true);
        HashMap<QName, ConfigLeafAttribute> configAttributes = new HashMap<>();

        //Match criteria with all keys of song, i.e name & version, this will also test findNode
        configAttributes.put(NAME_QNAME, new GenericConfigAttribute(NAME, NS, "See you again"));
        configAttributes.put(VERSION_QNAME, new GenericConfigAttribute(VERSION, NS, "1"));

        List<ModelNode> songs = m_aggregatedDSM.findNodes(SONG_SCHEMA_PATH, configAttributes, ALBUM_MODEL_NODE_ID, m_schemaRegistry);
        verifySongXmlModelNode(songs.get(0));
    }

    @Test
    public void testFindNonExistentSubtreeNode() throws Exception {
        initAlbumWithSongWithLeafListAndXmlSubtree();
        createAlbumWithSongWithLeafListAndXmlSubtree(true, false); //EmptyXmlSubtree
        List<ModelNode> awards = m_aggregatedDSM.findNodes(AWARDS_AND_NOMINATIONS_SCHEMA_PATH, new HashMap<>(), SONG_MODEL_NODE_ID, m_schemaRegistry);
        assertTrue(awards.isEmpty());
    }

    @Test
    public void updateLeafListForANodeWithXmlSubtree() throws Exception {
        initAlbumWithSongWithLeafListAndXmlSubtree();
        createAlbumWithSongWithLeafListAndXmlSubtree(false, true); //Create Song without the leaflists

        Map<QName, ConfigLeafAttribute> configAttributes = new HashMap<>();
        configAttributes.put(NAME_QNAME, new GenericConfigAttribute(NAME, NS, "See you again"));
        configAttributes.put(VERSION_QNAME, new GenericConfigAttribute(VERSION, NS, "1"));

        List<ModelNode> songs = m_aggregatedDSM.findNodes(SONG_SCHEMA_PATH, configAttributes, ALBUM_MODEL_NODE_ID, m_schemaRegistry);

        assertEquals(0, songs.get(0).getLeafLists().size());

        //Prepare the leaflists and update
        Map<QName, LinkedHashSet<ConfigLeafAttribute>> leafLists = new HashMap<>();
        LinkedHashSet<ConfigLeafAttribute> singerLeafList = new LinkedHashSet<>();
        singerLeafList.add(new GenericConfigAttribute(SINGER_LOCAL_NAME, NS, "Wiz Khalifa"));
        singerLeafList.add(new GenericConfigAttribute(SINGER_LOCAL_NAME, NS, "Charlie Puth"));
        leafLists.put(SINGER_QNAME, singerLeafList);

        LinkedHashSet<ConfigLeafAttribute> genreLeafList = new LinkedHashSet<>();
        genreLeafList.add(new GenericConfigAttribute(GENRE_LOCAL_NAME, NS, "hip hop"));
        genreLeafList.add(new GenericConfigAttribute(GENRE_LOCAL_NAME, NS, "pop-rap"));
        leafLists.put(GENRE_QNAME, genreLeafList);

        m_aggregatedDSM.updateNode(songs.get(0), ALBUM_MODEL_NODE_ID, null, leafLists, false);
        songs = m_aggregatedDSM.findNodes(SONG_SCHEMA_PATH, configAttributes, ALBUM_MODEL_NODE_ID, m_schemaRegistry);
        verifySongXmlModelNode(songs.get(0));
    }

    private void verifySongXmlModelNode(ModelNode xmlModelNode) {
        //find singer & genre leaf lists in song
        assertEquals(2, xmlModelNode.getLeafLists().size());
        Iterator<QName> leafListQNameItr = xmlModelNode.getLeafLists().keySet().iterator();
        QName genreLeafListQName = leafListQNameItr.next();
        QName singerLeafListQName = leafListQNameItr.next();

        //Each leaf-list has two elements
        assertEquals(2, xmlModelNode.getLeafLists().get(singerLeafListQName).size());
        assertEquals(2, xmlModelNode.getLeafLists().get(genreLeafListQName).size());

        Iterator<ConfigLeafAttribute> singerLeafListValuesItr = xmlModelNode.getLeafLists().get(singerLeafListQName).iterator();
        Iterator<ConfigLeafAttribute> genreLeafListValuesItr = xmlModelNode.getLeafLists().get(genreLeafListQName).iterator();

        assertEquals("Wiz Khalifa", singerLeafListValuesItr.next().getStringValue());
        assertEquals("Charlie Puth", singerLeafListValuesItr.next().getStringValue());

        assertEquals("hip hop", genreLeafListValuesItr.next().getStringValue());
        assertEquals("pop-rap", genreLeafListValuesItr.next().getStringValue());
    }
    
    private void initAlbumWithSongWithLeafListAndXmlSubtree() throws Exception {
        final String componentId = "Album";
        List<Class> classes = new ArrayList<>();
        classes.add(Album.class);
        classes.add(Song.class);
        classes.add(Singer.class);
        classes.add(Genre.class);
        EntityRegistryBuilder.updateEntityRegistry("Album", classes, m_entityRegistry, m_schemaRegistry, m_persistenceManagerUtil.getEntityDataStoreManager(), m_modelNodeDSMRegistry);
        ModelNodeDSMRegistry modelNodeDSMRegistry = (ModelNodeDSMRegistry) m_context.getBean("modelNodeDSMRegistry");
        modelNodeDSMRegistry.register(componentId, ALBUM_SCHEMA_PATH, m_xmlSubtreeDSM);
        modelNodeDSMRegistry.register(componentId, SONG_SCHEMA_PATH, m_xmlSubtreeDSM);
        modelNodeDSMRegistry.register(componentId, SINGER_SCHEMA_PATH, m_xmlSubtreeDSM);
        modelNodeDSMRegistry.register(componentId, SONG_NAME_SCHEMA_PATH, m_xmlSubtreeDSM);
        modelNodeDSMRegistry.register(componentId, GENRE_SCHEMA_PATH, m_xmlSubtreeDSM);
        modelNodeDSMRegistry.register(componentId, AWARDS_AND_NOMINATIONS_SCHEMA_PATH, m_xmlSubtreeDSM);
        SchemaRegistryTraverser traverser = new SchemaRegistryTraverser(componentId,
                Collections.singletonList(m_entityModelNodeHelperDeployer), m_schemaRegistry,
                m_schemaRegistry.getModule("leaflist-and-xmlsubtree", Revision.of(JB_REVISION)).orElse(null));
        traverser.traverse();
    }

    private void createAlbumWithSongWithLeafListAndXmlSubtree(boolean createLeafLists, boolean createSubtree) {
        Song song = new Song();
        song.setName("See you again");
        song.setVersion("1");

        if (createLeafLists) {
            Singer singer1 = new Singer();
            singer1.setSinger("Wiz Khalifa");
            singer1.setParentId(SONG_MODEL_NODE_ID.getModelNodeIdAsString());
            singer1.setSchemaPath(SchemaPathUtil.toStringNoRev(SINGER_SCHEMA_PATH));

            Singer singer2 = new Singer();
            singer2.setSinger("Charlie Puth");
            singer2.setParentId(SONG_MODEL_NODE_ID.getModelNodeIdAsString());
            singer2.setSchemaPath(SchemaPathUtil.toStringNoRev(SINGER_SCHEMA_PATH));

            song.addSinger(singer1);
            song.addSinger(singer2);

            Genre genre1 = new Genre();
            genre1.setGenre("hip hop");
            genre1.setParentId(SONG_MODEL_NODE_ID.getModelNodeIdAsString());
            genre1.setSchemaPath(SchemaPathUtil.toStringNoRev(GENRE_SCHEMA_PATH));
            Genre genre2 = new Genre();
            genre2.setGenre("pop-rap");
            genre2.setParentId(SONG_MODEL_NODE_ID.getModelNodeIdAsString());
            genre2.setSchemaPath(SchemaPathUtil.toStringNoRev(GENRE_SCHEMA_PATH));

            song.addGenre(genre1);
            song.addGenre(genre2);
        }


        song.setParentId(ALBUM_MODEL_NODE_ID.getModelNodeIdAsString());
        song.setSchemaPath(SchemaPathUtil.toStringNoRev(SONG_SCHEMA_PATH));

        if (createSubtree) {
            String awards = "<song xmlns=\"http://example.com/ns/example-leaflist\">" +
                    "<awards-and-nominations>" +
                    "<award>American Music Awards</award>" +
                    "<award>MTV Video Music Awards</award>" +
                    "</awards-and-nominations>" +
                    "</song>";
            song.setAwards(awards);
        }

        Album album = new Album();
        album.addSong(song);
        album.setName("See You Again");
        album.setSchemaPath(SchemaPathUtil.toStringNoRev(ALBUM_SCHEMA_PATH));
        album.setParentId(EMPTY_NODE_ID.getModelNodeIdAsString());
        persist(album);
    }

    private void persist(Object entity) {
        EntityDataStoreManager entityDataStoreManager = m_persistenceManagerUtil.getEntityDataStoreManager();
        entityDataStoreManager.beginTransaction();
        try {
            entityDataStoreManager.create(entity);
            entityDataStoreManager.commitTransaction();
        } catch (Exception e) {
            entityDataStoreManager.rollbackTransaction();
            throw e;
        }
    }

}
