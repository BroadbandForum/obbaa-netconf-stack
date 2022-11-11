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
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ALBUM_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ARTIST_LOCAL_NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ARTIST_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JB_NS;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JB_REVISION;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JUKEBOX_LOCAL_NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JUKEBOX_MODULE_NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JUKEBOX_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.LIBRARY_LOCAL_NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.LIBRARY_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.NAME_QNAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.SINGER_LOCAL_NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.SINGER_QNAME;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder;
import org.broadband_forum.obbaa.netconf.api.util.SchemaPathUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryTraverser;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryVisitor;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDSMRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeKey;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.utils.AnnotationAnalysisException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.utils.EntityRegistryBuilder;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.GenericConfigAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.IdentityRefConfigAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.albumwithleaflists.Album;
import org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.albumwithleaflists.Artist;
import org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.albumwithleaflists.Jukebox;
import org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.albumwithleaflists.Library;
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

@RunWith(RequestScopeJunitRunner.class)
public class AnnotationBasedDSMLeafListTest {

    public static final String TEST_APPLICATION_CONTEXT_XML = "/annotationbasedDSMLeafListtest/test-applicationContext.xml";
    public static final String ARTIST_NAME = "Artist";
    public static final String ALBUM_NAME = "Album";
    public static final String DUMMY_LEAF_LIST_ID_REF = "dummy-leaf-list-id-ref";

    private AbstractApplicationContext m_context;
    private AggregatedDSM m_aggregatedDSM;
    private EntityRegistry m_entityRegistry;
    private PersistenceManagerUtil m_persistenceManagerUtil;
    private EntityModelNodeHelperDeployer m_entityModelNodeHelperDeployer;
    private SchemaRegistry m_schemaRegistry;
    private ModelNodeDataStoreManager m_annotationDSM;
    private ModelNodeDSMRegistry m_modelNodeDSMRegistry;

    public static final SchemaPath SINGER_SCHEMA_PATH = new SchemaPathBuilder().withParent(ALBUM_SCHEMA_PATH).
            appendLocalName(SINGER_LOCAL_NAME).build();
    public static final SchemaPath DUMMY_ID_REF_SCHEMA_PATH = new SchemaPathBuilder().withParent(ALBUM_SCHEMA_PATH).
            appendLocalName(DUMMY_LEAF_LIST_ID_REF).build();

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
        SchemaRegistryTraverser traverser = new SchemaRegistryTraverser("JukeBox",
                Collections.singletonList(m_entityModelNodeHelperDeployer), m_schemaRegistry,
                m_schemaRegistry.getModule(JUKEBOX_MODULE_NAME, Revision.of(JB_REVISION)).orElse(null));
        traverser.traverse();
        m_aggregatedDSM = (AggregatedDSM) m_context.getBean("aggregatedDSM");
        m_annotationDSM = (ModelNodeDataStoreManager) m_context.getBean("annotationBasedDSM");
        m_modelNodeDSMRegistry = (ModelNodeDSMRegistry) m_context.getBean("modelNodeDSMRegistry");
        SchemaRegistryUtil.resetCache();
    }

    @Test
    public void testUpdateNode_LeafListWithInsertionOrder() throws Exception {
        initialiseJukeboxEntity();

        assertEquals(1, m_aggregatedDSM.listNodes(JUKEBOX_SCHEMA_PATH, m_schemaRegistry).size());
        assertEquals(1, m_aggregatedDSM.listNodes(LIBRARY_SCHEMA_PATH, m_schemaRegistry).size());
        assertEquals(1, m_aggregatedDSM.listNodes(ARTIST_SCHEMA_PATH, m_schemaRegistry).size());
        assertEquals(1, m_aggregatedDSM.listNodes(ALBUM_SCHEMA_PATH, m_schemaRegistry).size());

        List<ModelNode> singerModelNodes = m_aggregatedDSM.findNodes(SINGER_SCHEMA_PATH, Collections.EMPTY_MAP, m_albumNodeId ,m_schemaRegistry);
        assertEquals(0, singerModelNodes.size());

        List<String> leaflistInorder = new ArrayList<>();
        LinkedHashSet<ConfigLeafAttribute> children = new LinkedHashSet<>();

        leaflistInorder.add("singer3");
        leaflistInorder.add("singer2");
        leaflistInorder.add("singer1");

        populateGenericConfigLeafAttr(leaflistInorder, children);
        verifyLeafListsUpdatedInOrder(leaflistInorder, children);

        leaflistInorder = new ArrayList<>();
        children = new LinkedHashSet<>();

        leaflistInorder.add("singer1");
        leaflistInorder.add("singer2");
        leaflistInorder.add("singer3");

        populateGenericConfigLeafAttr(leaflistInorder, children);
        verifyLeafListsUpdatedInOrder(leaflistInorder, children);
    }

    @Test
    public void testUpdateNode_LeafListWithInsertionOrder_IdentityRef() throws Exception{
        initialiseJukeboxEntity();

        assertEquals(1, m_aggregatedDSM.listNodes(JUKEBOX_SCHEMA_PATH, m_schemaRegistry).size());
        assertEquals(1, m_aggregatedDSM.listNodes(LIBRARY_SCHEMA_PATH, m_schemaRegistry).size());
        assertEquals(1, m_aggregatedDSM.listNodes(ARTIST_SCHEMA_PATH, m_schemaRegistry).size());
        assertEquals(1, m_aggregatedDSM.listNodes(ALBUM_SCHEMA_PATH, m_schemaRegistry).size());

        List<ModelNode> dummyIdRefModelNodes = m_aggregatedDSM.findNodes(DUMMY_ID_REF_SCHEMA_PATH, Collections.EMPTY_MAP, m_albumNodeId ,m_schemaRegistry);
        assertEquals(0, dummyIdRefModelNodes.size());

        List<String> leaflistInorder = new ArrayList<>();
        LinkedHashSet<ConfigLeafAttribute> children = new LinkedHashSet<>();

        leaflistInorder.add("dummy-id-ref2");
        leaflistInorder.add("dummy-id-ref3");
        leaflistInorder.add("dummy-id-ref1");
        populateIdRefConfigLeafAttr(leaflistInorder, children);
        verifyLeafListsUpdatedInOrder(leaflistInorder, children);

        leaflistInorder = new ArrayList<>();
        children = new LinkedHashSet<>();

        leaflistInorder.add("dummy-id-ref4");
        leaflistInorder.add("dummy-id-ref2");
        leaflistInorder.add("dummy-id-ref0");
        populateIdRefConfigLeafAttr(leaflistInorder, children);
        verifyLeafListsUpdatedInOrder(leaflistInorder, children);
    }

    private void verifyLeafListsUpdatedInOrder(List<String> leaflistInorder, LinkedHashSet<ConfigLeafAttribute> children) {
        Map<QName, String> keys = new LinkedHashMap<>();
        keys.put(NAME_QNAME,ALBUM_NAME);
        ModelNodeKey modelNodeKey = new ModelNodeKey(keys);
        ModelNodeWithAttributes existingAlbumModelNode = (ModelNodeWithAttributes) m_aggregatedDSM.findNode(ALBUM_SCHEMA_PATH,modelNodeKey,m_artistNodeId, m_schemaRegistry);

        m_aggregatedDSM.updateNode(existingAlbumModelNode, m_artistNodeId, null, Collections.singletonMap(SINGER_QNAME, children),
                -1, false);

        existingAlbumModelNode = (ModelNodeWithAttributes) m_aggregatedDSM.findNode(ALBUM_SCHEMA_PATH,modelNodeKey,m_artistNodeId, m_schemaRegistry);
        Set<ConfigLeafAttribute> leafLists = existingAlbumModelNode.getLeafList(SINGER_QNAME);
        assertEquals(3, leafLists.size());
        int i = 0;

        Iterator<ConfigLeafAttribute> iterator = leafLists.iterator();
        while (iterator.hasNext()){
            assertEquals(leaflistInorder.get(i), iterator.next().getStringValue());
            i++;
        }
    }

    private void populateGenericConfigLeafAttr(List<String> leaflistInorder, LinkedHashSet<ConfigLeafAttribute> children) {
        GenericConfigAttribute leafList0 = new GenericConfigAttribute(SINGER_LOCAL_NAME, JB_NS, leaflistInorder.get(0));
        leafList0.setInsertIndex(0);
        GenericConfigAttribute leafList1 = new GenericConfigAttribute(SINGER_LOCAL_NAME, JB_NS, leaflistInorder.get(1));
        leafList1.setInsertIndex(1);
        GenericConfigAttribute leafList2 = new GenericConfigAttribute(SINGER_LOCAL_NAME, JB_NS, leaflistInorder.get(2));
        leafList2.setInsertIndex(2);

        children.add(leafList0);
        children.add(leafList1);
        children.add(leafList2);
    }

    private void populateIdRefConfigLeafAttr(List<String> leaflistInorder, LinkedHashSet<ConfigLeafAttribute> children) {
        IdentityRefConfigAttribute leafList0 = new IdentityRefConfigAttribute(JB_NS, "jbox", DUMMY_LEAF_LIST_ID_REF, leaflistInorder.get(0), JB_NS);
        leafList0.setInsertIndex(0);
        IdentityRefConfigAttribute leafList1 = new IdentityRefConfigAttribute(JB_NS, "jbox", DUMMY_LEAF_LIST_ID_REF, leaflistInorder.get(1), JB_NS);
        leafList1.setInsertIndex(1);
        IdentityRefConfigAttribute leafList2 = new IdentityRefConfigAttribute(JB_NS, "jbox", DUMMY_LEAF_LIST_ID_REF, leaflistInorder.get(2), JB_NS);
        leafList2.setInsertIndex(2);

        children.add(leafList0);
        children.add(leafList1);
        children.add(leafList2);
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
        modelNodeDSMRegistry.register(JUKEBOX_LOCAL_NAME, SINGER_SCHEMA_PATH, m_annotationDSM);
        modelNodeDSMRegistry.register(JUKEBOX_LOCAL_NAME, DUMMY_ID_REF_SCHEMA_PATH, m_annotationDSM);
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


        Album album = new Album();
        album.setName(ALBUM_NAME);
        album.setParentId(m_artistNodeId.getModelNodeIdAsString());
        album.setSchemaPath(SchemaPathUtil.toStringNoRev(ALBUM_SCHEMA_PATH));
        album.setYear("1948");

        Set<Album> albums = new HashSet<>();
        albums.add(album);
        artist.setAlbums(albums);
        List<Artist> artists = new ArrayList<>();
        artists.add(artist);
        library.setArtists(artists);
        jukebox.setLibrary(library);

        EntityDataStoreManager entityDataStoreManager = m_persistenceManagerUtil.getEntityDataStoreManager();
        try {
            entityDataStoreManager.create(jukebox);
        } catch (Exception e) {
            throw e;
        }
    }
}
