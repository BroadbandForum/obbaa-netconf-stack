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

import static org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder.fromString;
import static org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.TestConstants.EMPTY_NODE_ID;
import static org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.embeddedchoicecase.RootContainerTestConstants
        .CFG_TYPE_PATH;
import static org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.embeddedchoicecase.RootContainerTestConstants
        .CHILDREN_TYPE_PATH;
import static org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.embeddedchoicecase.RootContainerTestConstants
        .ID_QNAME;
import static org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.embeddedchoicecase.RootContainerTestConstants
        .INLINE_PATH;
import static org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.embeddedchoicecase.RootContainerTestConstants.NS;
import static org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.embeddedchoicecase.RootContainerTestConstants
        .PRIORITY_QNAME;
import static org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.embeddedchoicecase.RootContainerTestConstants
        .QUEUES_PATH;
import static org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.embeddedchoicecase.RootContainerTestConstants
        .QUEUE_PATH;
import static org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.embeddedchoicecase.RootContainerTestConstants
        .ROOT_CONTAINER_PATH;
import static org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.embeddedchoicecase.RootContainerTestConstants
        .TCONTS_PATH;
import static org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.embeddedchoicecase.RootContainerTestConstants
        .TM_ROOT_PATH;
import static org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.embeddedchoicecase.RootContainerTestConstants
        .WEIGHT_QNAME;
import static org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.embeddedchoicecase.RootContainerTestConstants
    .WEIGHT;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ALBUM_LOCAL_NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ALBUM_QNAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ALBUM_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ARTIST_LOCAL_NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ARTIST_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.CD_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.FORMAT_QNAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JB_NS;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JB_REVISION;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JUKEBOX_LOCAL_NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JUKEBOX_MODULE_NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JUKEBOX_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.LIBRARY_LOCAL_NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.LIBRARY_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.LOCATION_QNAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.NAME_QNAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.SINGER_QNAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.SINGER_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.SONG_LOCAL_NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.SONG_QNAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.SONG_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.STOCK_QNAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.YEAR_QNAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.FORMAT;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.LOCATION;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.SINGER_LOCAL_NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.YEAR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
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
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.w3c.dom.Element;

import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.SchemaPathUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.albumxmlsubtree.Album;
import org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.albumxmlsubtree.Artist;
import org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.embeddedchoicecase.RootContainer;
import org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.embeddedchoicecase.RootContainerTestConstants;

import org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.yangwithparentchildsameqname.Root;
import org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.yangwithparentchildsameqname.RootList;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryTraverser;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryVisitor;
import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.DataStoreException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDSMRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeKey;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeKeyBuilder;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.utils.AnnotationAnalysisException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.utils.EntityRegistryBuilder;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.GenericConfigAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangAttribute;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangChild;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangContainer;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangList;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangListKey;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangParentId;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangSchemaPath;
import org.broadband_forum.obbaa.netconf.persistence.EntityDataStoreManager;
import org.broadband_forum.obbaa.netconf.persistence.PersistenceManagerUtil;

/**
 * Created by sgs on 1/27/16.
 */

public class XmlSubtreeDSMTest {

    private static final String TEST_APPLICATION_CONTEXT_XML = "/xmlsubtreedsmtest/test-applicationContext.xml";
    private static final String LIBRARY_XML = "/xmlsubtreedsmtest/library.xml";
    private static final String LIBRARY_XML_WITH_2_1948_ALBUMS = "/xmlsubtreedsmtest/library_with_2_1948_albums.xml";
    private static final String UPDATED_LIBRARY_XML = "/xmlsubtreedsmtest/updatedlibrary.xml";
    private static final String ALBUM_SUBTREE_XML = "/xmlsubtreedsmtest/album-subtree.xml";
    private static final String ALBUM_SUBTREE_with_CD_XML = "/xmlsubtreedsmtest/album-subtree-with-cd.xml";
    private static final String ARTIST1_NAME = "Artist1";
    private static final String ARTIST2_NAME = "Artist2";
    private static final ModelNodeKey ARTIST2_KEY = new ModelNodeKeyBuilder().appendKey(NAME_QNAME, ARTIST2_NAME)
            .build();
    private static final String ALBUM1_NAME = "album11";
    private static final String ALBUM2_NAME = "album22";
    private static final ModelNodeKey ALBUM2_KEY = new ModelNodeKeyBuilder().appendKey(NAME_QNAME, ALBUM2_NAME).build();
    private static final String SONG_NAME = "Let it go";
    private static final ModelNodeKey SONG_KEY = new ModelNodeKeyBuilder().appendKey(NAME_QNAME, SONG_NAME).build();
    protected static final String PINK_FLOYD = "Pink Floyd";
    public static final String CHOICE_CASE_NS = "urn:choice-case-test";
    public static final QName IP_PORT_QNAME = QName.create(CHOICE_CASE_NS, "2015-12-14", "ip-port");
    public static final QName IP_ADDRESS_QNAME = QName.create(CHOICE_CASE_NS, "2015-12-14", "ip-address");
    public static final String DEVICE_MGR_SP_STR = "(" + CHOICE_CASE_NS + "?revision=2015-12-14)," + "device-manager";
    public static final String DEVICE_HDR_SP_STR = "(" + CHOICE_CASE_NS + "?revision=2015-12-14)," + "device-manager," +
            " device-holder";
    public static final String DEVICE_SP_STR = "(" + CHOICE_CASE_NS + "?revision=2015-12-14)," + "device-manager, " +
            "device-holder, device";
    public static final String CONF_DEVICE_PROP_SP_STR = "(" + CHOICE_CASE_NS + "?revision=2015-12-14),"
            + "device-manager, device-holder, device, conn-type, non-call-home, configured-device-properties";

    public static final String YWPCSQ_NS = "urn:yang-with-parent-child-same-qname";
    public static final String YWPCSQ_ROOT_SP_STR = "(" + YWPCSQ_NS + "?revision=2016-07-11)," + "root";
    private static final String YWPCSQ_CWSQ1_SP_STR = "(" + YWPCSQ_NS + "?revision=2016-07-11)," + "root, same-qname1";
    private static final String YWPCSQ_CLWSQ1_SP_STR = "(" + YWPCSQ_NS + "?revision=2016-07-11)," + "root, " +
            "same-qname1, same-qname1";
    private static final String YWPCSQ_CWSQ2_SP_STR = "(" + YWPCSQ_NS + "?revision=2016-07-11)," + "root, same-qname2";
    private static final String YWPCSQ_CCWSQ2_SP_STR = "(" + YWPCSQ_NS + "?revision=2016-07-11)," + "root, " +
            "same-qname2, same-qname2";

    private static final String YWPCSQ_ROOT_LIST = "(" + YWPCSQ_NS + "?revision=2016-07-11)," + "root-list";
    private static final String YWPCSQ_ROOT_LIST_LEAF = "(" + YWPCSQ_NS + "?revision=2016-07-11)," + "root-list, " +
            "list-leaf";
    private static final String YWPCSQ_ROOT_LIST_CHILD_CONTAINER = "(" + YWPCSQ_NS + "?revision=2016-07-11)," +
            "root-list,child";
    private static final String YWPCSQ_ROOT_LIST_CONT_LEAF = "(" + YWPCSQ_NS + "?revision=2016-07-11),"
            + "root-list, child, container-leaf";
    public static final String YWPCSQ_REV = "2016-07-11";

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

    private ModelNodeId m_jukeboxNodeId = new ModelNodeId().addRdn(new ModelNodeRdn(ModelNodeRdn.CONTAINER, JB_NS,
            JUKEBOX_LOCAL_NAME));
    private ModelNodeId m_libraryNodeId = new ModelNodeId(m_jukeboxNodeId).addRdn(new ModelNodeRdn(ModelNodeRdn
            .CONTAINER, JB_NS, LIBRARY_LOCAL_NAME));
    private ModelNodeId m_artist1NodeId = new ModelNodeId(m_libraryNodeId).addRdn(new ModelNodeRdn(ModelNodeRdn
            .CONTAINER, JB_NS, ARTIST_LOCAL_NAME))
            .addRdn(new ModelNodeRdn(NAME_QNAME, ARTIST1_NAME));
    private ModelNodeId m_artist2NodeId = new ModelNodeId(m_libraryNodeId).addRdn(new ModelNodeRdn(ModelNodeRdn
            .CONTAINER, JB_NS, ARTIST_LOCAL_NAME))
            .addRdn(new ModelNodeRdn(NAME_QNAME, ARTIST2_NAME));
    private ModelNodeId m_albumNodeId1 = new ModelNodeId(m_artist1NodeId).addRdn(new ModelNodeRdn(ModelNodeRdn
            .CONTAINER, JB_NS, ALBUM_LOCAL_NAME))
            .addRdn(new ModelNodeRdn(NAME_QNAME, ALBUM1_NAME));
    private ModelNodeId m_albumNodeId = new ModelNodeId(m_artist2NodeId).addRdn(new ModelNodeRdn(ModelNodeRdn
            .CONTAINER, JB_NS, ALBUM_LOCAL_NAME))
            .addRdn(new ModelNodeRdn(NAME_QNAME, ALBUM2_NAME));
    public final ModelNodeId m_songNodeId = new ModelNodeId(m_albumNodeId).addRdn(new ModelNodeRdn(ModelNodeRdn
            .CONTAINER, JB_NS, SONG_LOCAL_NAME))
            .addRdn(new ModelNodeRdn(NAME_QNAME, "My Song"));
    public final ModelNodeId m_song1NodeId = new ModelNodeId(m_albumNodeId1).addRdn(new ModelNodeRdn(ModelNodeRdn
            .CONTAINER, JB_NS, SONG_LOCAL_NAME))
            .addRdn(new ModelNodeRdn(NAME_QNAME, "song-name-1"));
    public final ModelNodeId m_song2NodeId = new ModelNodeId(m_albumNodeId1).addRdn(new ModelNodeRdn(ModelNodeRdn
            .CONTAINER, JB_NS, SONG_LOCAL_NAME))
            .addRdn(new ModelNodeRdn(NAME_QNAME, "song-name-2"));

    @BeforeClass
    public static void setupClass() {
        RequestScope.setEnableThreadLocalInUT(true);
    }

    @Before
    public void setUp() {

        RequestScope.resetScope();
        m_context = new ClassPathXmlApplicationContext(TEST_APPLICATION_CONTEXT_XML);

        m_entityRegistry = (EntityRegistry) m_context.getBean("entityRegistry");
        m_persistenceManagerUtil = (PersistenceManagerUtil) m_context.getBean("persistenceManagerUtil");
        m_entityModelNodeHelperDeployer = (EntityModelNodeHelperDeployer) m_context.getBean
                ("entityModelNodeHelperDeployer");
        m_schemaRegistry = (SchemaRegistry) m_context.getBean("schemaRegistry");
        m_subSystemRegistry = (SubSystemRegistry) m_context.getBean("subSystemRegistry");
        m_modelNodeHelperRegistry = (ModelNodeHelperRegistry) m_context.getBean("modelNodeHelperRegistry");
        m_modelNodeDSMRegistry = (ModelNodeDSMRegistry) m_context.getBean("modelNodeDSMRegistry");
        m_xmlModelNodeToXmlMapper = new XmlModelNodeToXmlMapperImpl(m_dsmCache, m_schemaRegistry,
                m_modelNodeHelperRegistry, m_subSystemRegistry,
                m_entityRegistry);
        m_xmlSubtreeDSM = (ModelNodeDataStoreManager) m_context.getBean("xmlSubtreeDSM");
        SchemaRegistryTraverser traverser = new SchemaRegistryTraverser("JukeBox",
                Collections.singletonList((SchemaRegistryVisitor) m_entityModelNodeHelperDeployer), m_schemaRegistry,
                m_schemaRegistry.getModule(JUKEBOX_MODULE_NAME, JB_REVISION));
        traverser.traverse();
        ModelNodeDataStoreManager innerDsm = (AggregatedDSM) m_context.getBean("aggregatedDSM");

        m_aggregatedDSM = innerDsm;
    }

    @Test
    public void testListNodesLibrarySubtree() throws AnnotationAnalysisException, DataStoreException {

        initialiseLibrarySubtreeEntity();
        assertEquals(1, m_aggregatedDSM.listNodes(JUKEBOX_SCHEMA_PATH).size());
        assertEquals(1, m_aggregatedDSM.listNodes(LIBRARY_SCHEMA_PATH).size());
        assertEquals(2, m_aggregatedDSM.listNodes(ARTIST_SCHEMA_PATH).size());
        assertEquals(3, m_aggregatedDSM.listNodes(ALBUM_SCHEMA_PATH).size());
        assertEquals(1, m_aggregatedDSM.listNodes(SONG_SCHEMA_PATH).size());

        EntityDataStoreManager entityDataStoreManager = m_persistenceManagerUtil.getEntityDataStoreManager();

        entityDataStoreManager.beginTransaction();
        try {
            org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.libraryxmlsubtree.Jukebox jukebox = entityDataStoreManager.findById(org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.libraryxmlsubtree.Jukebox.class,
                    EMPTY_NODE_ID.getModelNodeIdAsString());
            org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.libraryxmlsubtree.Library library = jukebox.getLibrary();
            String updatedXmlSubtree = TestUtil.loadAsString(UPDATED_LIBRARY_XML);
            library.setXmlSubtree(updatedXmlSubtree);

            entityDataStoreManager.commitTransaction();
        } catch (Exception e) {
            entityDataStoreManager.rollbackTransaction();
            throw e;
        }
        assertEqualsWithScopeReset(3, m_aggregatedDSM.listNodes(ALBUM_SCHEMA_PATH).size());
        assertEqualsWithScopeReset(2, m_aggregatedDSM.listNodes(SONG_SCHEMA_PATH).size());
    }

    private void assertEqualsWithScopeReset(Object expected, Object actual) throws DataStoreException {
        executeWithResetScope(() -> {
            assertEquals(expected, actual);
            return null;
        });
    }

    private <T> T executeWithResetScope(ScopeTemplate<T> template) throws DataStoreException {
        RequestScope.resetScope();
        try {
            return template.execute();
        } finally {
            RequestScope.resetScope();
        }
    }

    @Test
    public void testListNodesChoiceCase() throws Exception {
        initialiseChoiceCaseEntities();
        assertEquals(1, m_aggregatedDSM.listNodes(fromString(DEVICE_MGR_SP_STR)).size());
        assertEquals(1, m_aggregatedDSM.listNodes(fromString(DEVICE_HDR_SP_STR)).size());
        assertEquals(1, m_aggregatedDSM.listNodes(fromString(DEVICE_SP_STR)).size());
        assertEquals(1, m_aggregatedDSM.listNodes(fromString(CONF_DEVICE_PROP_SP_STR)).size());
    }

    @Test
    public void testListChildNodesAlbumSubtree() throws AnnotationAnalysisException, DataStoreException {

        initializeAlbumSubtreeEntity();
        assertEquals(2, m_aggregatedDSM.listChildNodes(ARTIST_SCHEMA_PATH, m_libraryNodeId).size());
        assertEquals(1, m_aggregatedDSM.listChildNodes(ALBUM_SCHEMA_PATH, m_artist1NodeId).size());
        assertEquals(2, m_aggregatedDSM.listChildNodes(ALBUM_SCHEMA_PATH, m_artist2NodeId).size());
        assertEquals(2, m_aggregatedDSM.listChildNodes(SONG_SCHEMA_PATH, m_albumNodeId).size());
    }

    @Test
    public void testListChildNodesParentAndChildSameQName() throws Exception {
        initialiseParentAndChildSameQName();
        ModelNodeId parentId = new ModelNodeId();
        parentId.addRdn(ModelNodeRdn.CONTAINER, YWPCSQ_NS, "root").addRdn(ModelNodeRdn.CONTAINER, YWPCSQ_NS,
                "same-qname1");
        assertEquals(1, m_aggregatedDSM.listChildNodes(fromString(YWPCSQ_CLWSQ1_SP_STR), parentId).size());

        parentId = new ModelNodeId();
        parentId.addRdn(ModelNodeRdn.CONTAINER, YWPCSQ_NS, "root").addRdn(ModelNodeRdn.CONTAINER, YWPCSQ_NS,
                "same-qname2");
        assertEquals(1, m_aggregatedDSM.listChildNodes(fromString(YWPCSQ_CCWSQ2_SP_STR), parentId).size());
    }

    @Test
    public void testFindNodeLibrarySubtree() throws DataStoreException, AnnotationAnalysisException {

        initialiseLibrarySubtreeEntity();
        ModelNode jukeboxModelNode = m_aggregatedDSM.findNode(JUKEBOX_SCHEMA_PATH, ModelNodeKey.EMPTY_KEY,
                EMPTY_NODE_ID);
        assertTrue(jukeboxModelNode instanceof ModelNodeWithAttributes);
        assertEquals(JUKEBOX_LOCAL_NAME, jukeboxModelNode.getQName().getLocalName());

        ModelNode libraryModelNode = m_aggregatedDSM.findNode(LIBRARY_SCHEMA_PATH, ModelNodeKey.EMPTY_KEY,
                m_jukeboxNodeId);
        assertTrue(libraryModelNode instanceof XmlModelNodeImpl);
        assertEquals(LIBRARY_LOCAL_NAME, libraryModelNode.getQName().getLocalName());

        XmlModelNodeImpl artistModelNode = (XmlModelNodeImpl) m_aggregatedDSM.findNode(ARTIST_SCHEMA_PATH,
                ARTIST2_KEY, m_libraryNodeId);
        assertTrue(artistModelNode instanceof XmlModelNodeImpl);
        assertEquals(ARTIST_SCHEMA_PATH, artistModelNode.getModelNodeSchemaPath());
        assertEquals(2, artistModelNode.getChildren().get(ALBUM_QNAME).size());
        assertEquals(1, artistModelNode.getAttributes().size());

        XmlModelNodeImpl albumModelNode = (XmlModelNodeImpl) m_aggregatedDSM.findNode(ALBUM_SCHEMA_PATH, ALBUM2_KEY,
                m_artist2NodeId);
        assertTrue(albumModelNode instanceof XmlModelNodeImpl);
        assertEquals(ALBUM_SCHEMA_PATH, albumModelNode.getModelNodeSchemaPath());
        assertEquals(2, albumModelNode.getAttributes().size());
        assertEquals(ALBUM2_NAME, albumModelNode.getAttributes().get(NAME_QNAME).getStringValue());
        assertEquals(1, albumModelNode.getChildren().get(SONG_QNAME).size());

        XmlModelNodeImpl songModelNode = (XmlModelNodeImpl) m_aggregatedDSM.findNode(SONG_SCHEMA_PATH, SONG_KEY,
                m_albumNodeId);
        assertTrue(songModelNode instanceof XmlModelNodeImpl);
        assertEquals(SONG_SCHEMA_PATH, songModelNode.getModelNodeSchemaPath());
        assertEquals(SONG_NAME, songModelNode.getAttributes().get(NAME_QNAME).getStringValue());
    }

    @Test
    public void testFindNodeChoiceCase() throws Exception {
        initialiseChoiceCaseEntities();
        ModelNodeId deviceId = getDevice1Id();

        ModelNode actualNode = m_aggregatedDSM.findNode(fromString(CONF_DEVICE_PROP_SP_STR), ModelNodeKey.EMPTY_KEY,
                deviceId);
        assertTrue(actualNode instanceof ModelNodeWithAttributes);
        assertEquals("configured-device-properties", actualNode.getQName().getLocalName());
    }

    private ModelNodeId getDevice1Id() {
        ModelNodeId device1Id = new ModelNodeId();
        device1Id.addRdn(ModelNodeRdn.CONTAINER, CHOICE_CASE_NS, "device-manager");
        device1Id.addRdn(ModelNodeRdn.CONTAINER, CHOICE_CASE_NS, "device-holder");
        device1Id.addRdn("name", CHOICE_CASE_NS, "OLT1");
        device1Id.addRdn(ModelNodeRdn.CONTAINER, CHOICE_CASE_NS, "device");
        device1Id.addRdn("device-id", CHOICE_CASE_NS, "ONT1");
        return device1Id;
    }

    @Test
    public void testFindNodesArtistSubtree() throws DataStoreException, AnnotationAnalysisException {
        initialiseLibrarySubtreeEntity();
        HashMap<QName, ConfigLeafAttribute> configAttributes = new HashMap<>();
        configAttributes.put(YEAR_QNAME, new GenericConfigAttribute(YEAR, JB_NS, "1948"));

        List<ModelNode> albumModelNodes = m_aggregatedDSM.findNodes(ALBUM_SCHEMA_PATH, configAttributes,
                m_artist2NodeId);
        assertEquals(1, albumModelNodes.size());
        assertTrue(albumModelNodes.get(0) instanceof XmlModelNodeImpl);
        XmlModelNodeImpl albumNode = (XmlModelNodeImpl) albumModelNodes.get(0);
        assertEquals(ALBUM_SCHEMA_PATH, albumNode.getModelNodeSchemaPath());
        assertEquals(2, albumNode.getAttributes().size());
        assertEquals(ALBUM2_NAME, albumNode.getAttributes().get(NAME_QNAME).getStringValue());
        assertEquals(1, albumNode.getChildren().get(SONG_QNAME).size());

        // add one more album with year 1948
        addAnother1948AlbumToLibrarySubtree();

        albumModelNodes = executeWithResetScope((ScopeTemplate<List>)
                () -> m_aggregatedDSM.findNodes(ALBUM_SCHEMA_PATH, configAttributes, m_artist2NodeId));
        assertTrue(albumModelNodes.get(0) instanceof XmlModelNodeImpl);
        albumNode = (XmlModelNodeImpl) albumModelNodes.get(0);
        assertEquals(ALBUM_SCHEMA_PATH, albumNode.getModelNodeSchemaPath());
        assertEquals(2, albumNode.getAttributes().size());
        assertEquals(ALBUM2_NAME, albumNode.getAttributes().get(NAME_QNAME).getStringValue());
        assertEquals(1, albumNode.getChildren().get(SONG_QNAME).size());

        assertTrue(albumModelNodes.get(1) instanceof XmlModelNodeImpl);
        albumNode = (XmlModelNodeImpl) albumModelNodes.get(1);
        assertEquals(ALBUM_SCHEMA_PATH, albumNode.getModelNodeSchemaPath());
        assertEquals(2, albumNode.getAttributes().size());
        assertEquals(PINK_FLOYD, albumNode.getAttributes().get(NAME_QNAME).getStringValue());
        assertEquals(2, albumNode.getChildren().get(SONG_QNAME).size());
    }

    private void addAnother1948AlbumToLibrarySubtree() {
        EntityDataStoreManager enityDataStoreManager = m_persistenceManagerUtil.getEntityDataStoreManager();
        enityDataStoreManager.beginTransaction();
        try {
            org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.libraryxmlsubtree.Library library = enityDataStoreManager.findAll(org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.libraryxmlsubtree.Library.class).get(0);
            String librarySubtree = TestUtil.loadAsString(LIBRARY_XML_WITH_2_1948_ALBUMS);
            library.setXmlSubtree(librarySubtree);
            enityDataStoreManager.commitTransaction();
        } catch (Exception e) {
            enityDataStoreManager.rollbackTransaction();
            throw e;
        }
    }

    @Test
    public void testFindNodesAlbumSubtree() throws AnnotationAnalysisException, DataStoreException {
        initializeAlbumSubtreeEntity();
        HashMap<QName, ConfigLeafAttribute> keys = new HashMap<>();
        List<ModelNode> jukeboxModelNodes = m_aggregatedDSM.findNodes(JUKEBOX_SCHEMA_PATH, keys, EMPTY_NODE_ID);
        assertEquals(1, jukeboxModelNodes.size());
        ModelNode jukeboxModelNode = jukeboxModelNodes.get(0);
        assertTrue(jukeboxModelNode instanceof ModelNodeWithAttributes);
        assertEquals(JUKEBOX_LOCAL_NAME, jukeboxModelNode.getQName().getLocalName());

        List<ModelNode> libraryModelNodes = m_aggregatedDSM.findNodes(LIBRARY_SCHEMA_PATH, keys, m_jukeboxNodeId);
        assertEquals(1, libraryModelNodes.size());
        ModelNode libraryModelNode = libraryModelNodes.get(0);
        assertTrue(libraryModelNode instanceof ModelNodeWithAttributes);
        assertEquals(LIBRARY_LOCAL_NAME, libraryModelNode.getQName().getLocalName());

        keys.put(NAME_QNAME, new GenericConfigAttribute(NAME, JB_NS, ARTIST2_NAME));
        List<ModelNode> artistModelNodes = m_aggregatedDSM.findNodes(ARTIST_SCHEMA_PATH, keys, m_libraryNodeId);
        assertEquals(1, artistModelNodes.size());
        ModelNode artistModelNode = artistModelNodes.get(0);
        assertTrue(artistModelNode instanceof ModelNodeWithAttributes);
        assertEquals(1, ((ModelNodeWithAttributes) artistModelNode).getAttributes().size());
        assertEquals(ARTIST_SCHEMA_PATH, artistModelNode.getModelNodeSchemaPath());

        keys.clear();
        artistModelNodes = m_aggregatedDSM.findNodes(ARTIST_SCHEMA_PATH, keys, m_libraryNodeId);
        assertEquals(2, artistModelNodes.size());

        keys.put(NAME_QNAME, new GenericConfigAttribute(NAME, JB_NS, ALBUM2_NAME));
        List<ModelNode> albumModelNodes = m_aggregatedDSM.findNodes(ALBUM_SCHEMA_PATH, keys, m_artist2NodeId);
        assertEquals(1, albumModelNodes.size());
        ModelNode albumModelNode = albumModelNodes.get(0);
        assertTrue(albumModelNode instanceof XmlModelNodeImpl);
        assertEquals(1, ((XmlModelNodeImpl) albumModelNode).getAttributes().size());
        assertEquals(ALBUM_SCHEMA_PATH, albumModelNode.getModelNodeSchemaPath());

        keys.clear();
        keys.put(NAME_QNAME, new GenericConfigAttribute(NAME, JB_NS, "Pocketful of Sunshine"));
        List<ModelNode> songModelNodes = m_aggregatedDSM.findNodes(SONG_SCHEMA_PATH, keys, m_albumNodeId);
        assertEquals(1, songModelNodes.size());
        XmlModelNodeImpl songModelNode = (XmlModelNodeImpl) songModelNodes.get(0);
        assertEquals(SONG_SCHEMA_PATH, songModelNode.getModelNodeSchemaPath());
        assertEquals(3, songModelNode.getAttributes().size());
        assertEquals("Pocketful of Sunshine", songModelNode.getAttributes().get(NAME_QNAME).getStringValue());

        keys.clear();
        keys.put(FORMAT_QNAME, new GenericConfigAttribute(FORMAT, JB_NS, "mp3"));
        songModelNodes = m_aggregatedDSM.findNodes(SONG_SCHEMA_PATH, keys, m_albumNodeId);
        assertEquals(2, songModelNodes.size());
        songModelNode = (XmlModelNodeImpl) songModelNodes.get(0);
        assertEquals(2, songModelNode.getAttributes().size());
        assertEquals("mp3", songModelNode.getAttributes().get(FORMAT_QNAME).getStringValue());
        assertEquals(SONG_SCHEMA_PATH, songModelNode.getModelNodeSchemaPath());

        songModelNode = (XmlModelNodeImpl) songModelNodes.get(1);
        assertEquals(3, songModelNode.getAttributes().size());
        assertEquals("mp3", songModelNode.getAttributes().get(FORMAT_QNAME).getStringValue());
        assertEquals(SONG_SCHEMA_PATH, songModelNode.getModelNodeSchemaPath());

        keys.clear();
        keys.put(FORMAT_QNAME, new GenericConfigAttribute(FORMAT, JB_NS, "mp3"));
        keys.put(LOCATION_QNAME, new GenericConfigAttribute(LOCATION, JB_NS, "somelocation"));
        songModelNodes = m_aggregatedDSM.findNodes(SONG_SCHEMA_PATH, keys, m_albumNodeId);
        assertEquals(1, songModelNodes.size());
        songModelNode = (XmlModelNodeImpl) songModelNodes.get(0);
        assertEquals(3, songModelNode.getAttributes().size());
        assertEquals("mp3", songModelNode.getAttributes().get(FORMAT_QNAME).getStringValue());
        assertEquals("somelocation", songModelNode.getAttributes().get(LOCATION_QNAME).getStringValue());
        assertEquals(SONG_SCHEMA_PATH, songModelNode.getModelNodeSchemaPath());

        keys.clear();
        keys.put(FORMAT_QNAME, new GenericConfigAttribute(FORMAT, JB_NS, "mp3"));
        keys.put(LOCATION_QNAME, new GenericConfigAttribute(LOCATION, JB_NS, "someOtherlocation"));
        songModelNodes = m_aggregatedDSM.findNodes(SONG_SCHEMA_PATH, keys, m_albumNodeId);
        assertEquals(0, songModelNodes.size());

        setAlbum22Year("1948");
        keys.clear();
        keys.put(YEAR_QNAME, new GenericConfigAttribute(YEAR, JB_NS, "1948"));
        albumModelNodes = executeWithResetScope(() -> m_aggregatedDSM.findNodes(ALBUM_SCHEMA_PATH, keys,
                m_artist2NodeId));
        assertEquals(1, albumModelNodes.size());
        albumModelNode = albumModelNodes.get(0);
        assertTrue(albumModelNode instanceof XmlModelNodeImpl);
        assertEquals("1948", ((XmlModelNodeImpl) albumModelNode).getAttributes().get(YEAR_QNAME).getStringValue());
        assertEquals(ALBUM_SCHEMA_PATH, albumModelNode.getModelNodeSchemaPath());

        keys.clear();
        keys.put(YEAR_QNAME, new GenericConfigAttribute(YEAR, JB_NS, "2016"));
        albumModelNodes = executeWithResetScope(() -> m_aggregatedDSM.findNodes(ALBUM_SCHEMA_PATH, keys,
                m_artist2NodeId));
        assertEquals(0, albumModelNodes.size());

    }

    @Test
    public void testFindNodesWithNestedChildrenInAlbumSubtree() throws AnnotationAnalysisException, DataStoreException {
        initializeCDSubtreeEntity();
        HashMap<QName, ConfigLeafAttribute> keys = new HashMap<>();
        List<ModelNode> cdModelNodes = m_aggregatedDSM.findNodes(CD_SCHEMA_PATH, keys, m_song2NodeId);
        assertEquals(1, cdModelNodes.size());
        XmlModelNodeImpl cdModelNode = (XmlModelNodeImpl) cdModelNodes.get(0);
        assertEquals(CD_SCHEMA_PATH, cdModelNode.getModelNodeSchemaPath());
        assertEquals(1, cdModelNode.getAttributes().size());
        assertEquals("200", cdModelNode.getAttributes().get(STOCK_QNAME).getStringValue());

        cdModelNodes = m_aggregatedDSM.findNodes(CD_SCHEMA_PATH, keys, m_song1NodeId);
        assertEquals(1, cdModelNodes.size());
        XmlModelNodeImpl cdModelNode2 = (XmlModelNodeImpl) cdModelNodes.get(0);
        assertEquals(1, cdModelNode2.getAttributes().size());
        assertEquals("100", cdModelNode2.getAttributes().get(STOCK_QNAME).getStringValue());
        assertEquals(CD_SCHEMA_PATH, cdModelNode2.getModelNodeSchemaPath());
    }

    @Test
    public void testFindNodeAlbumSubtree() throws AnnotationAnalysisException, DataStoreException {

        initializeAlbumSubtreeEntity();
        Map<QName, String> keys = new LinkedHashMap<>();
        ModelNode jukeboxModelNode = m_aggregatedDSM.findNode(JUKEBOX_SCHEMA_PATH, new ModelNodeKey(keys),
                EMPTY_NODE_ID);
        assertTrue(jukeboxModelNode instanceof ModelNodeWithAttributes);
        assertEquals(JUKEBOX_LOCAL_NAME, jukeboxModelNode.getQName().getLocalName());

        ModelNode libraryModelNode = m_aggregatedDSM.findNode(LIBRARY_SCHEMA_PATH, new ModelNodeKey(keys),
                m_jukeboxNodeId);
        assertTrue(libraryModelNode instanceof ModelNodeWithAttributes);
        assertEquals(LIBRARY_LOCAL_NAME, libraryModelNode.getQName().getLocalName());

        keys.put(NAME_QNAME, ARTIST2_NAME);
        ModelNode artistModelNode = m_aggregatedDSM.findNode(ARTIST_SCHEMA_PATH, new ModelNodeKey(keys),
                m_libraryNodeId);
        assertTrue(artistModelNode instanceof ModelNodeWithAttributes);
        assertEquals(1, ((ModelNodeWithAttributes) artistModelNode).getAttributes().size());
        assertEquals(ARTIST_SCHEMA_PATH, artistModelNode.getModelNodeSchemaPath());

        keys.put(NAME_QNAME, ALBUM2_NAME);
        ModelNode albumModelNode = m_aggregatedDSM.findNode(ALBUM_SCHEMA_PATH, new ModelNodeKey(keys), m_artist2NodeId);
        assertTrue(albumModelNode instanceof XmlModelNodeImpl);
        assertEquals(1, ((XmlModelNodeImpl) albumModelNode).getAttributes().size());
        assertEquals(ALBUM_SCHEMA_PATH, albumModelNode.getModelNodeSchemaPath());

        keys.put(NAME_QNAME, "Pocketful of Sunshine");
        XmlModelNodeImpl songModelNode = (XmlModelNodeImpl) m_aggregatedDSM.findNode(SONG_SCHEMA_PATH, new
                        ModelNodeKey(keys),
                m_albumNodeId);
        assertEquals(SONG_SCHEMA_PATH, songModelNode.getModelNodeSchemaPath());
        assertEquals(3, songModelNode.getAttributes().size());
        assertEquals("Pocketful of Sunshine", songModelNode.getAttributes().get(NAME_QNAME).getStringValue());
    }

    @Test
    public void testCreateModelNodeLibrarySubtree() throws AnnotationAnalysisException, DataStoreException {

        // Initialise Jukebox with Library having XML Subtree and verify the node count
        initialiseLibrarySubtreeEntity();
        assertEquals(1, m_aggregatedDSM.listNodes(JUKEBOX_SCHEMA_PATH).size());
        assertEquals(1, m_aggregatedDSM.listNodes(LIBRARY_SCHEMA_PATH).size());
        assertEquals(2, m_aggregatedDSM.listNodes(ARTIST_SCHEMA_PATH).size());
        assertEquals(3, m_aggregatedDSM.listNodes(ALBUM_SCHEMA_PATH).size());
        assertEquals(1, m_aggregatedDSM.listNodes(SONG_SCHEMA_PATH).size());

        // Case 1: Add one artist to existing library container
        XmlModelNodeImpl libraryModelNode = (XmlModelNodeImpl) m_aggregatedDSM.findNode(LIBRARY_SCHEMA_PATH,
                new ModelNodeKey(new LinkedHashMap<>()), m_jukeboxNodeId);
        Map<QName, ConfigLeafAttribute> configAttributes = new HashMap<>();
        configAttributes.put(NAME_QNAME, new GenericConfigAttribute(NAME, JB_NS, "new-artist"));
        XmlModelNodeImpl artistModelNode = new XmlModelNodeImpl(ARTIST_SCHEMA_PATH, configAttributes, Collections
                .<Element>emptyList(),
                libraryModelNode, m_libraryNodeId, m_xmlModelNodeToXmlMapper, m_modelNodeHelperRegistry,
                m_schemaRegistry,
                m_subSystemRegistry, null);

        m_aggregatedDSM.createNode(artistModelNode, m_libraryNodeId);
        assertEquals(3, m_aggregatedDSM.listNodes(ARTIST_SCHEMA_PATH).size());

        // Case 2: Add one album to existing artist list
        Map<QName, String> keys = new LinkedHashMap<>();
        keys.put(NAME_QNAME, ARTIST2_NAME);
        XmlModelNodeImpl updatedArtistModelNode = (XmlModelNodeImpl) m_aggregatedDSM.findNode(ARTIST_SCHEMA_PATH, new
                        ModelNodeKey(keys),
                m_libraryNodeId);
        configAttributes = new HashMap<>();
        configAttributes.put(NAME_QNAME, new GenericConfigAttribute(NAME, JB_NS, "album23"));
        XmlModelNodeImpl albumModelNode = new XmlModelNodeImpl(ALBUM_SCHEMA_PATH, configAttributes, Collections
                .<Element>emptyList(),
                updatedArtistModelNode, m_artist2NodeId, m_xmlModelNodeToXmlMapper, m_modelNodeHelperRegistry,
                m_schemaRegistry,
                m_subSystemRegistry, null);

        m_aggregatedDSM.createNode(albumModelNode, m_artist2NodeId);
        assertEquals(4, m_aggregatedDSM.listNodes(ALBUM_SCHEMA_PATH).size());

        // Case 3: Add one song to existing album list
        keys = new LinkedHashMap<>();
        keys.put(NAME_QNAME, ALBUM2_NAME);
        XmlModelNodeImpl updatedAlbumModelNode = (XmlModelNodeImpl) m_aggregatedDSM.findNode(ALBUM_SCHEMA_PATH, new
                        ModelNodeKey(keys),
                m_artist2NodeId);
        configAttributes = new HashMap<>();
        configAttributes.put(NAME_QNAME, new GenericConfigAttribute(NAME, JB_NS, "Pocketful of Sunshine"));
        XmlModelNodeImpl songModelNode = new XmlModelNodeImpl(SONG_SCHEMA_PATH, configAttributes, Collections
                .<Element>emptyList(),
                updatedAlbumModelNode, m_albumNodeId, m_xmlModelNodeToXmlMapper, m_modelNodeHelperRegistry,
                m_schemaRegistry,
                m_subSystemRegistry, null);

        m_aggregatedDSM.createNode(songModelNode, m_albumNodeId);
        assertEquals(2, m_aggregatedDSM.listNodes(SONG_SCHEMA_PATH).size());
    }

    @Test
    public void testCreateModelNodeAlbumSubtree() throws AnnotationAnalysisException, DataStoreException {

        // Initialise Jukebox with Album having XML Subtree and verify the node count
        initializeAlbumSubtreeEntity();
        assertEquals(1, m_aggregatedDSM.listNodes(JUKEBOX_SCHEMA_PATH).size());
        assertEquals(1, m_aggregatedDSM.listNodes(LIBRARY_SCHEMA_PATH).size());
        assertEquals(2, m_aggregatedDSM.listChildNodes(ARTIST_SCHEMA_PATH, m_libraryNodeId).size());
        assertEquals(2, m_aggregatedDSM.listChildNodes(ALBUM_SCHEMA_PATH, m_artist2NodeId).size());
        assertEquals(2, m_aggregatedDSM.listChildNodes(SONG_SCHEMA_PATH, m_albumNodeId).size());

        // Case 1: Add one artist to existing library container
        Map<QName, ConfigLeafAttribute> configAttributes = new HashMap<>();
        configAttributes.put(NAME_QNAME, new GenericConfigAttribute(NAME, JB_NS, "new-artist"));
        ModelNodeWithAttributes artistModelNode = new ModelNodeWithAttributes(ARTIST_SCHEMA_PATH, m_libraryNodeId,
                m_modelNodeHelperRegistry, null, m_schemaRegistry, null);
        artistModelNode.setAttributes(configAttributes);

        m_aggregatedDSM.createNode(artistModelNode, m_libraryNodeId);
        assertEquals(3, m_aggregatedDSM.listChildNodes(ARTIST_SCHEMA_PATH, m_libraryNodeId).size());

        // Case 2: Add one album to existing artist list
        configAttributes = new HashMap<>();
        configAttributes.put(NAME_QNAME, new GenericConfigAttribute(NAME, JB_NS, "album23"));
        XmlModelNodeImpl albumModelNode = new XmlModelNodeImpl(ALBUM_SCHEMA_PATH, configAttributes, Collections
                .<Element>emptyList(), null,
                m_artist2NodeId, m_xmlModelNodeToXmlMapper, m_modelNodeHelperRegistry, m_schemaRegistry,
                m_subSystemRegistry, null);

        m_aggregatedDSM.createNode(albumModelNode, m_artist2NodeId);
        assertEquals(3, m_aggregatedDSM.listChildNodes(ALBUM_SCHEMA_PATH, m_artist2NodeId).size());

        // Case 3: Add one song to existing album list
        Map<QName, String> keys = new LinkedHashMap<>();
        keys.put(NAME_QNAME, ALBUM2_NAME);
        XmlModelNodeImpl updatedAlbumModelNode = (XmlModelNodeImpl) m_aggregatedDSM.findNode(ALBUM_SCHEMA_PATH, new
                        ModelNodeKey(keys),
                m_artist2NodeId);
        configAttributes = new HashMap<>();
        configAttributes.put(NAME_QNAME, new GenericConfigAttribute(NAME, JB_NS, "Cake by the ocean"));
        XmlModelNodeImpl songModelNode = new XmlModelNodeImpl(SONG_SCHEMA_PATH, configAttributes, Collections
                .<Element>emptyList(),
                updatedAlbumModelNode, m_albumNodeId, m_xmlModelNodeToXmlMapper, m_modelNodeHelperRegistry,
                m_schemaRegistry,
                m_subSystemRegistry, null);
        m_aggregatedDSM.createNode(songModelNode, m_albumNodeId);
        assertEquals(3, m_aggregatedDSM.listChildNodes(SONG_SCHEMA_PATH, m_albumNodeId).size());
    }

    @Test
    public void testCreateLibrarySubtreeWithInsertIndex() throws AnnotationAnalysisException, DataStoreException {

        // Initialise Jukebox with Library having XML Subtree and verify the node count
        initialiseLibrarySubtreeEntity();
        assertEquals(1, m_aggregatedDSM.listNodes(JUKEBOX_SCHEMA_PATH).size());
        assertEquals(1, m_aggregatedDSM.listNodes(LIBRARY_SCHEMA_PATH).size());
        assertEquals(2, m_aggregatedDSM.listNodes(ARTIST_SCHEMA_PATH).size());
        assertEquals(3, m_aggregatedDSM.listNodes(ALBUM_SCHEMA_PATH).size());
        assertEquals(1, m_aggregatedDSM.listNodes(SONG_SCHEMA_PATH).size());

        // Case 1a: Add one artist at position 0 to existing library container
        List<ModelNode> listArtistNodes = m_aggregatedDSM.listNodes(ARTIST_SCHEMA_PATH);
        XmlModelNodeImpl libraryModelNode = (XmlModelNodeImpl) m_aggregatedDSM.findNode(LIBRARY_SCHEMA_PATH,
                new ModelNodeKey(new LinkedHashMap<>()), m_jukeboxNodeId);
        Map<QName, ConfigLeafAttribute> configAttributes = new HashMap<>();
        configAttributes.put(NAME_QNAME, new GenericConfigAttribute(NAME, JB_NS, "new-artist"));
        XmlModelNodeImpl artistModelNode = new XmlModelNodeImpl(ARTIST_SCHEMA_PATH, configAttributes, Collections
                .<Element>emptyList(),
                libraryModelNode, m_libraryNodeId, m_xmlModelNodeToXmlMapper, m_modelNodeHelperRegistry,
                m_schemaRegistry,
                m_subSystemRegistry, null);

        m_aggregatedDSM.createNode(artistModelNode, m_libraryNodeId, 0);

        List<ModelNode> expectedArtistList = new ArrayList<ModelNode>();
        expectedArtistList.add(artistModelNode);
        expectedArtistList.addAll(listArtistNodes);

        List<ModelNode> actualArtistList = new ArrayList<ModelNode>(m_aggregatedDSM.listNodes(ARTIST_SCHEMA_PATH));
        assertEquals(3, actualArtistList.size());

        for (int i = 0; i < actualArtistList.size(); i++) {
            assertEquals(0, ((XmlModelNodeImpl) expectedArtistList.get(i)).compareTo((XmlModelNodeImpl)
                    actualArtistList.get(i)));
        }

        // Case 1b: Add one more artist in between to existing library container
        configAttributes = new HashMap<>();
        configAttributes.put(NAME_QNAME, new GenericConfigAttribute(NAME, JB_NS, "new-artist-in-between"));
        XmlModelNodeImpl artistModelNode1 = new XmlModelNodeImpl(ARTIST_SCHEMA_PATH, configAttributes, Collections
                .<Element>emptyList(),
                libraryModelNode, m_libraryNodeId, m_xmlModelNodeToXmlMapper, m_modelNodeHelperRegistry,
                m_schemaRegistry,
                m_subSystemRegistry, null);

        m_aggregatedDSM.createNode(artistModelNode1, m_libraryNodeId, 2);

        expectedArtistList.add(2, artistModelNode1);

        actualArtistList = new ArrayList<ModelNode>(m_aggregatedDSM.listNodes(ARTIST_SCHEMA_PATH));
        assertEquals(4, actualArtistList.size());

        for (int i = 0; i < actualArtistList.size(); i++) {
            assertEquals(0, ((XmlModelNodeImpl) expectedArtistList.get(i)).compareTo((XmlModelNodeImpl)
                    actualArtistList.get(i)));
        }

        // Case 1c: Add one more artist at last position to existing library container
        configAttributes = new HashMap<>();
        configAttributes.put(NAME_QNAME, new GenericConfigAttribute(NAME, JB_NS, "new-artist-again"));
        XmlModelNodeImpl artistModelNode2 = new XmlModelNodeImpl(ARTIST_SCHEMA_PATH, configAttributes, Collections
                .<Element>emptyList(),
                libraryModelNode, m_libraryNodeId, m_xmlModelNodeToXmlMapper, m_modelNodeHelperRegistry,
                m_schemaRegistry,
                m_subSystemRegistry, null);

        m_aggregatedDSM.createNode(artistModelNode2, m_libraryNodeId, m_aggregatedDSM.listNodes(ARTIST_SCHEMA_PATH)
                .size());

        expectedArtistList.add(artistModelNode2);

        actualArtistList = new ArrayList<ModelNode>(m_aggregatedDSM.listNodes(ARTIST_SCHEMA_PATH));
        assertEquals(5, actualArtistList.size());

        for (int i = 0; i < actualArtistList.size(); i++) {
            assertEquals(0, ((XmlModelNodeImpl) expectedArtistList.get(i)).compareTo((XmlModelNodeImpl)
                    actualArtistList.get(i)));
        }

        // Case 2a: Add one album at position 0 to existing artist list
        SortedMap<QName, String> keys = new TreeMap<>();
        keys.put(NAME_QNAME, ARTIST2_NAME);
        List<ModelNode> listAlbumNodes = m_aggregatedDSM.listChildNodes(ALBUM_SCHEMA_PATH, m_artist2NodeId);
        XmlModelNodeImpl updatedArtistModelNode = (XmlModelNodeImpl) m_aggregatedDSM.findNode(ARTIST_SCHEMA_PATH, new
                        ModelNodeKey(keys),
                m_libraryNodeId);
        configAttributes = new HashMap<>();
        configAttributes.put(NAME_QNAME, new GenericConfigAttribute(NAME, JB_NS, "album23"));
        XmlModelNodeImpl albumModelNode = new XmlModelNodeImpl(ALBUM_SCHEMA_PATH, configAttributes, Collections
                .<Element>emptyList(),
                updatedArtistModelNode, m_artist2NodeId, m_xmlModelNodeToXmlMapper, m_modelNodeHelperRegistry,
                m_schemaRegistry,
                m_subSystemRegistry, null);

        m_aggregatedDSM.createNode(albumModelNode, m_artist2NodeId, 0);

        List<ModelNode> expectedAlbumList = new ArrayList<ModelNode>();
        expectedAlbumList.add(albumModelNode);
        expectedAlbumList.addAll(listAlbumNodes);

        List<ModelNode> actualAlbumList = new ArrayList<ModelNode>(m_aggregatedDSM.listChildNodes(ALBUM_SCHEMA_PATH,
                m_artist2NodeId));
        assertEquals(3, actualAlbumList.size());

        for (int i = 0; i < actualAlbumList.size(); i++) {
            assertEquals(0, ((XmlModelNodeImpl) expectedAlbumList.get(i)).compareTo((XmlModelNodeImpl)
                    actualAlbumList.get(i)));
        }

        // Case 2b: Add one more album at position 0 to existing artist list
        configAttributes = new HashMap<>();
        configAttributes.put(NAME_QNAME, new GenericConfigAttribute(NAME, JB_NS, "album24"));

        XmlModelNodeImpl albumModelNode1 = new XmlModelNodeImpl(ALBUM_SCHEMA_PATH, configAttributes, Collections
                .<Element>emptyList(),
                updatedArtistModelNode, m_artist2NodeId, m_xmlModelNodeToXmlMapper, m_modelNodeHelperRegistry,
                m_schemaRegistry,
                m_subSystemRegistry, null);

        m_aggregatedDSM.createNode(albumModelNode1, m_artist2NodeId, 0);
        assertEquals(5, m_aggregatedDSM.listNodes(ALBUM_SCHEMA_PATH).size());

        // Case 3a: Add one song at position 0 to existing album list
        keys = new TreeMap<>();
        keys.put(NAME_QNAME, ALBUM2_NAME);
        List<ModelNode> listSongNodes = m_aggregatedDSM.listNodes(SONG_SCHEMA_PATH);
        XmlModelNodeImpl updatedAlbumModelNode = (XmlModelNodeImpl) m_aggregatedDSM.findNode(ALBUM_SCHEMA_PATH, new
                        ModelNodeKey(keys),
                m_artist2NodeId);
        configAttributes = new HashMap<>();
        configAttributes.put(NAME_QNAME, new GenericConfigAttribute(NAME, JB_NS, "Pocketful of Sunshine"));
        XmlModelNodeImpl songModelNode = new XmlModelNodeImpl(SONG_SCHEMA_PATH, configAttributes, Collections
                .<Element>emptyList(),
                updatedAlbumModelNode, m_albumNodeId, m_xmlModelNodeToXmlMapper, m_modelNodeHelperRegistry,
                m_schemaRegistry,
                m_subSystemRegistry, null);

        m_aggregatedDSM.createNode(songModelNode, m_albumNodeId, 0);

        List<ModelNode> expectedSongList = new ArrayList<ModelNode>();
        expectedSongList.add(songModelNode);
        expectedSongList.addAll(listSongNodes);

        List<ModelNode> actualSongList = new ArrayList<ModelNode>(m_aggregatedDSM.listNodes(SONG_SCHEMA_PATH));
        assertEquals(2, actualSongList.size());

        for (int i = 0; i < actualSongList.size(); i++) {
            assertEquals(0, ((XmlModelNodeImpl) expectedSongList.get(i)).compareTo((XmlModelNodeImpl) actualSongList
                    .get(i)));
        }

        // Case 3b: Add one more song at position 0 to existing album list
        configAttributes = new HashMap<>();
        configAttributes.put(NAME_QNAME, new GenericConfigAttribute(NAME, JB_NS, "One more Pocketful of Sunshine"));
        XmlModelNodeImpl songModelNode1 = new XmlModelNodeImpl(SONG_SCHEMA_PATH, configAttributes, Collections
                .<Element>emptyList(),
                updatedAlbumModelNode, m_albumNodeId, m_xmlModelNodeToXmlMapper, m_modelNodeHelperRegistry,
                m_schemaRegistry,
                m_subSystemRegistry, null);

        m_aggregatedDSM.createNode(songModelNode1, m_albumNodeId, 0);
        assertEquals(3, m_aggregatedDSM.listNodes(SONG_SCHEMA_PATH).size());
    }

    @Test
    public void testUpdateModelNodeLibrarySubtree() throws AnnotationAnalysisException, DataStoreException {
        initialiseLibrarySubtreeEntity();
        assertEquals(1, m_aggregatedDSM.listNodes(SONG_SCHEMA_PATH).size());
        testUpdateModelNode();
        testUpdateModelNodeWithInsertIndex();
    }

    @Test
    public void testUpdateModelNodeAlbumSubtree() throws AnnotationAnalysisException, DataStoreException {
        initializeAlbumSubtreeEntity();
        assertEquals(2, m_aggregatedDSM.listChildNodes(SONG_SCHEMA_PATH, m_albumNodeId).size());
        testUpdateModelNode();

        //test update of entity attributes as well as xml attrbutes
        XmlModelNodeImpl albumModelNode = (XmlModelNodeImpl) m_aggregatedDSM.findNode(ALBUM_SCHEMA_PATH, ALBUM2_KEY,
                m_artist2NodeId);
        assertNull(albumModelNode.getAttribute(YEAR_QNAME));
        Map<QName, ConfigLeafAttribute> configAttributes = new HashMap<>();
        configAttributes.clear();
        configAttributes.put(YEAR_QNAME, new GenericConfigAttribute(YEAR, JB_NS, "1947"));
        m_aggregatedDSM.updateNode(albumModelNode, m_artist2NodeId, configAttributes, null, false);
        albumModelNode = (XmlModelNodeImpl) m_aggregatedDSM.findNode(ALBUM_SCHEMA_PATH, ALBUM2_KEY, m_artist2NodeId);
        assertEquals("1947", albumModelNode.getAttribute(YEAR_QNAME).getStringValue());
    }

    @Test
    public void testUpdateModelNodeChoiceCase() throws Exception {
        initialiseChoiceCaseEntities();
        List<ModelNode> modelNodes = m_aggregatedDSM.listNodes(fromString(CONF_DEVICE_PROP_SP_STR));
        assertEquals(1, modelNodes.size());
        ModelNodeWithAttributes cdpNode = (ModelNodeWithAttributes) modelNodes.get(0);
        assertEquals("127.0.0.1", cdpNode.getAttribute(IP_ADDRESS_QNAME).getStringValue());
        assertEquals("123", cdpNode.getAttribute(IP_PORT_QNAME).getStringValue());

        Map<QName, ConfigLeafAttribute> configAttrs = new HashMap<>();
        configAttrs.put(IP_ADDRESS_QNAME, new GenericConfigAttribute("ip-address", CHOICE_CASE_NS, "135.249.45.153"));
        m_aggregatedDSM.updateNode(cdpNode, getDevice1Id(), configAttrs, null, false);

        modelNodes = m_aggregatedDSM.listNodes(fromString(CONF_DEVICE_PROP_SP_STR));
        assertEquals(1, modelNodes.size());
        cdpNode = (ModelNodeWithAttributes) modelNodes.get(0);
        assertEquals("135.249.45.153", cdpNode.getAttribute(IP_ADDRESS_QNAME).getStringValue());
        assertEquals("123", cdpNode.getAttribute(IP_PORT_QNAME).getStringValue());
    }

    @Test
    public void testUpdateParentAndChildSameQNameNodes() throws Exception {
        initialiseParentAndChildSameQName();

        QName keyQname = QName.create(YWPCSQ_NS, YWPCSQ_REV, "key-leaf");
        ModelNodeKey modelNodeKey = new ModelNodeKeyBuilder().appendKey(keyQname, "key-value").build();
        ModelNodeId parentId = new ModelNodeId();
        parentId.addRdn(ModelNodeRdn.CONTAINER, YWPCSQ_NS, "root").addRdn(ModelNodeRdn.CONTAINER, YWPCSQ_NS,
                "same-qname1");
        XmlModelNodeImpl listNode = (XmlModelNodeImpl) m_aggregatedDSM.findNode(fromString(YWPCSQ_CLWSQ1_SP_STR),
                modelNodeKey, parentId);
        QName leafQname = QName.create(YWPCSQ_NS, YWPCSQ_REV, "value-leaf");
        assertEquals("leaf-value", listNode.getAttribute(leafQname).getStringValue());

        Map<QName, ConfigLeafAttribute> configAttributes = new HashMap<>();
        configAttributes.put(leafQname, new GenericConfigAttribute("value-leaf", YWPCSQ_NS, "leaf-value2"));
        m_aggregatedDSM.updateNode(listNode, parentId, configAttributes, null, false);
        listNode = (XmlModelNodeImpl) m_aggregatedDSM.findNode(fromString(YWPCSQ_CLWSQ1_SP_STR), modelNodeKey,
                parentId);
        assertEquals("leaf-value2", listNode.getAttributes().get(leafQname).getStringValue());
    }

    @Test
    public void testDeleteRootNode() throws Exception {

        initialiseParentAndChildSameQName();
        createRootListEntity();
        assertEquals(1, m_aggregatedDSM.listNodes(fromString(YWPCSQ_ROOT_SP_STR)).size());
        assertEquals(1, m_aggregatedDSM.listNodes(fromString(YWPCSQ_ROOT_LIST)).size());

        XmlModelNodeImpl rootListModelNode = (XmlModelNodeImpl) m_aggregatedDSM.findNode(fromString(YWPCSQ_ROOT_LIST),
                ModelNodeKey.EMPTY_KEY, new ModelNodeId());
        m_aggregatedDSM.removeNode(rootListModelNode, new ModelNodeId());
        assertEquals(1, m_aggregatedDSM.listNodes(fromString(YWPCSQ_ROOT_SP_STR)).size());
        assertEquals(0, m_aggregatedDSM.listNodes(fromString(YWPCSQ_ROOT_LIST)).size());

        XmlModelNodeImpl rootContainerModelNode = (XmlModelNodeImpl) m_aggregatedDSM.findNode(fromString
                        (YWPCSQ_ROOT_SP_STR),
                ModelNodeKey.EMPTY_KEY, new ModelNodeId());
        m_aggregatedDSM.removeNode(rootContainerModelNode, new ModelNodeId());
        assertEquals(0, m_aggregatedDSM.listNodes(fromString(YWPCSQ_ROOT_SP_STR)).size());
    }

    @Test
    public void testRemoveModelNodeLibrarySubtree() throws AnnotationAnalysisException, DataStoreException {

        initialiseLibrarySubtreeEntity();
        assertEquals(1, m_aggregatedDSM.listNodes(JUKEBOX_SCHEMA_PATH).size());
        assertEquals(1, m_aggregatedDSM.listNodes(LIBRARY_SCHEMA_PATH).size());
        assertEquals(2, m_aggregatedDSM.listNodes(ARTIST_SCHEMA_PATH).size());
        assertEquals(3, m_aggregatedDSM.listNodes(ALBUM_SCHEMA_PATH).size());
        assertEquals(1, m_aggregatedDSM.listNodes(SONG_SCHEMA_PATH).size());

        XmlModelNodeImpl songModelNode = (XmlModelNodeImpl) m_aggregatedDSM.findNode(SONG_SCHEMA_PATH, SONG_KEY,
                m_albumNodeId);
        m_aggregatedDSM.removeNode(songModelNode, m_albumNodeId);
        assertEquals(0, m_aggregatedDSM.listNodes(SONG_SCHEMA_PATH).size());

        XmlModelNodeImpl albumModelNode = (XmlModelNodeImpl) m_aggregatedDSM.findNode(ALBUM_SCHEMA_PATH, ALBUM2_KEY,
                m_artist2NodeId);
        m_aggregatedDSM.removeNode(albumModelNode, m_artist2NodeId);
        assertEquals(2, m_aggregatedDSM.listNodes(ALBUM_SCHEMA_PATH).size());

        XmlModelNodeImpl artistModelNode = (XmlModelNodeImpl) m_aggregatedDSM.findNode(ARTIST_SCHEMA_PATH,
                ARTIST2_KEY, m_libraryNodeId);
        m_aggregatedDSM.removeNode(artistModelNode, m_libraryNodeId);
        assertEquals(1, m_aggregatedDSM.listNodes(ARTIST_SCHEMA_PATH).size());

        XmlModelNodeImpl libraryModelNode = (XmlModelNodeImpl) m_aggregatedDSM.findNode(LIBRARY_SCHEMA_PATH,
                ModelNodeKey.EMPTY_KEY,
                m_jukeboxNodeId);
        m_aggregatedDSM.removeNode(libraryModelNode, m_jukeboxNodeId);
        assertEquals(0, m_aggregatedDSM.listNodes(LIBRARY_SCHEMA_PATH).size());

        ModelNode jukeboxModelNode = m_aggregatedDSM.findNode(JUKEBOX_SCHEMA_PATH, ModelNodeKey.EMPTY_KEY, new
                ModelNodeId());
        m_aggregatedDSM.removeNode(jukeboxModelNode, new ModelNodeId());
        assertEquals(0, m_aggregatedDSM.listNodes(JUKEBOX_SCHEMA_PATH).size());

    }

    @Test
    public void testRemoveModelNodeAlbumSubtree() throws AnnotationAnalysisException, DataStoreException {

        initializeAlbumSubtreeEntity();
        assertEquals(1, m_aggregatedDSM.listNodes(JUKEBOX_SCHEMA_PATH).size());
        assertEquals(1, m_aggregatedDSM.listNodes(LIBRARY_SCHEMA_PATH).size());
        assertEquals(2, m_aggregatedDSM.listChildNodes(ARTIST_SCHEMA_PATH, m_libraryNodeId).size());
        assertEquals(2, m_aggregatedDSM.listChildNodes(ALBUM_SCHEMA_PATH, m_artist2NodeId).size());
        assertEquals(2, m_aggregatedDSM.listChildNodes(SONG_SCHEMA_PATH, m_albumNodeId).size());

        XmlModelNodeImpl songModelNode = (XmlModelNodeImpl) m_aggregatedDSM.findNode(SONG_SCHEMA_PATH, SONG_KEY,
                m_albumNodeId);
        m_aggregatedDSM.removeNode(songModelNode, m_albumNodeId);
        assertEquals(1, m_aggregatedDSM.listChildNodes(SONG_SCHEMA_PATH, m_albumNodeId).size());

        XmlModelNodeImpl albumModelNode = (XmlModelNodeImpl) m_aggregatedDSM.findNode(ALBUM_SCHEMA_PATH, ALBUM2_KEY,
                m_artist2NodeId);
        long currentMillis = System.currentTimeMillis();
        m_aggregatedDSM.removeNode(albumModelNode, m_artist2NodeId);
        assertTrue(Album.c_lastSetXmlSubtreeCall >= currentMillis);
        assertEquals("", Album.c_lastSetXmlSubtreeStr);
        assertEquals(1, m_aggregatedDSM.listChildNodes(ALBUM_SCHEMA_PATH, m_artist2NodeId).size());

        ModelNode artistModelNode = m_aggregatedDSM.findNode(ARTIST_SCHEMA_PATH, ARTIST2_KEY, m_libraryNodeId);
        m_aggregatedDSM.removeNode(artistModelNode, m_libraryNodeId);
        assertEquals(1, m_aggregatedDSM.listChildNodes(ARTIST_SCHEMA_PATH, m_libraryNodeId).size());

        ModelNode jukeboxModelNode = m_aggregatedDSM.findNode(JUKEBOX_SCHEMA_PATH, ModelNodeKey.EMPTY_KEY, new
                ModelNodeId());
        m_aggregatedDSM.removeNode(jukeboxModelNode, new ModelNodeId());
        assertEquals(0, m_aggregatedDSM.listNodes(JUKEBOX_SCHEMA_PATH).size());
    }

    @Test
    public void testRemoveModelNodeChoiceCase() throws Exception {
        initialiseChoiceCaseEntities();
        List<ModelNode> modelNodes = m_aggregatedDSM.listNodes(fromString(CONF_DEVICE_PROP_SP_STR));
        assertEquals(1, modelNodes.size());
        ModelNodeWithAttributes cdpNode = (ModelNodeWithAttributes) modelNodes.get(0);

        m_aggregatedDSM.removeNode(cdpNode, getDevice1Id());

        modelNodes = m_aggregatedDSM.listNodes(fromString(CONF_DEVICE_PROP_SP_STR));
        assertEquals(0, modelNodes.size());
    }

    @Test
    public void testRemoveAllModelNodeLibrarySubtree() throws AnnotationAnalysisException, DataStoreException {

        initialiseLibrarySubtreeEntity();
        assertEquals(1, m_aggregatedDSM.listNodes(JUKEBOX_SCHEMA_PATH).size());
        assertEquals(1, m_aggregatedDSM.listNodes(LIBRARY_SCHEMA_PATH).size());
        assertEquals(2, m_aggregatedDSM.listNodes(ARTIST_SCHEMA_PATH).size());
        assertEquals(3, m_aggregatedDSM.listNodes(ALBUM_SCHEMA_PATH).size());
        assertEquals(1, m_aggregatedDSM.listNodes(SONG_SCHEMA_PATH).size());

        XmlModelNodeImpl albumModelNode = (XmlModelNodeImpl) m_aggregatedDSM.findNode(ALBUM_SCHEMA_PATH, ALBUM2_KEY,
                m_artist2NodeId);
        m_aggregatedDSM.removeAllNodes(albumModelNode, SONG_SCHEMA_PATH, m_artist2NodeId);
        assertEquals(1, m_aggregatedDSM.listNodes(JUKEBOX_SCHEMA_PATH).size());
        assertEquals(1, m_aggregatedDSM.listNodes(LIBRARY_SCHEMA_PATH).size());
        assertEquals(2, m_aggregatedDSM.listNodes(ARTIST_SCHEMA_PATH).size());
        assertEquals(3, m_aggregatedDSM.listNodes(ALBUM_SCHEMA_PATH).size());
        assertEquals(0, m_aggregatedDSM.listNodes(SONG_SCHEMA_PATH).size());

        XmlModelNodeImpl artistModelNode = (XmlModelNodeImpl) m_aggregatedDSM.findNode(ARTIST_SCHEMA_PATH,
                ARTIST2_KEY, m_libraryNodeId);
        m_aggregatedDSM.removeAllNodes(artistModelNode, ALBUM_SCHEMA_PATH, m_libraryNodeId);
        assertEquals(1, m_aggregatedDSM.listNodes(JUKEBOX_SCHEMA_PATH).size());
        assertEquals(1, m_aggregatedDSM.listNodes(LIBRARY_SCHEMA_PATH).size());
        assertEquals(2, m_aggregatedDSM.listNodes(ARTIST_SCHEMA_PATH).size());
        assertEquals(1, m_aggregatedDSM.listNodes(ALBUM_SCHEMA_PATH).size());

        XmlModelNodeImpl libraryModelNode = (XmlModelNodeImpl) m_aggregatedDSM.findNode(LIBRARY_SCHEMA_PATH,
                ModelNodeKey.EMPTY_KEY,
                m_jukeboxNodeId);
        m_aggregatedDSM.removeAllNodes(libraryModelNode, ARTIST_SCHEMA_PATH, m_jukeboxNodeId);
        assertEquals(1, m_aggregatedDSM.listNodes(JUKEBOX_SCHEMA_PATH).size());
        assertEquals(1, m_aggregatedDSM.listNodes(LIBRARY_SCHEMA_PATH).size());
        assertEquals(0, m_aggregatedDSM.listNodes(ARTIST_SCHEMA_PATH).size());

    }

    @Test
    public void testRemoveAllModelNodeAlbumSubtree() throws AnnotationAnalysisException, DataStoreException {

        initializeAlbumSubtreeEntity();

        assertEquals(1, m_aggregatedDSM.listNodes(JUKEBOX_SCHEMA_PATH).size());
        assertEquals(1, m_aggregatedDSM.listNodes(LIBRARY_SCHEMA_PATH).size());
        assertEquals(2, m_aggregatedDSM.listChildNodes(ARTIST_SCHEMA_PATH, m_libraryNodeId).size());
        assertEquals(2, m_aggregatedDSM.listChildNodes(ALBUM_SCHEMA_PATH, m_artist2NodeId).size());
        assertEquals(2, m_aggregatedDSM.listChildNodes(SONG_SCHEMA_PATH, m_albumNodeId).size());

        ModelNode albumModelNode = m_aggregatedDSM.findNode(ALBUM_SCHEMA_PATH, ALBUM2_KEY, m_artist2NodeId);
        m_aggregatedDSM.removeAllNodes(albumModelNode, SONG_SCHEMA_PATH, m_artist2NodeId);
        assertEquals(1, m_aggregatedDSM.listNodes(JUKEBOX_SCHEMA_PATH).size());
        assertEquals(1, m_aggregatedDSM.listNodes(LIBRARY_SCHEMA_PATH).size());
        assertEquals(2, m_aggregatedDSM.listChildNodes(ARTIST_SCHEMA_PATH, m_libraryNodeId).size());
        assertEquals(2, m_aggregatedDSM.listChildNodes(ALBUM_SCHEMA_PATH, m_artist2NodeId).size());
        assertEquals(0, m_aggregatedDSM.listChildNodes(SONG_SCHEMA_PATH, m_albumNodeId).size());

        ModelNode artistModelNode = m_aggregatedDSM.findNode(ARTIST_SCHEMA_PATH, ARTIST2_KEY, m_libraryNodeId);
        m_aggregatedDSM.removeAllNodes(artistModelNode, ALBUM_SCHEMA_PATH, m_libraryNodeId);
        assertEquals(1, m_aggregatedDSM.listNodes(JUKEBOX_SCHEMA_PATH).size());
        assertEquals(1, m_aggregatedDSM.listNodes(LIBRARY_SCHEMA_PATH).size());
        assertEquals(2, m_aggregatedDSM.listChildNodes(ARTIST_SCHEMA_PATH, m_libraryNodeId).size());
        assertEquals(0, m_aggregatedDSM.listChildNodes(ALBUM_SCHEMA_PATH, m_artist2NodeId).size());

        ModelNode libraryModelNode = m_aggregatedDSM.findNode(LIBRARY_SCHEMA_PATH, ModelNodeKey.EMPTY_KEY,
                m_jukeboxNodeId);
        m_aggregatedDSM.removeAllNodes(libraryModelNode, ARTIST_SCHEMA_PATH, m_jukeboxNodeId);
        assertEquals(1, m_aggregatedDSM.listNodes(JUKEBOX_SCHEMA_PATH).size());
        assertEquals(1, m_aggregatedDSM.listNodes(LIBRARY_SCHEMA_PATH).size());
        assertEquals(0, m_aggregatedDSM.listChildNodes(ARTIST_SCHEMA_PATH, m_libraryNodeId).size());
    }

    @Test
    public void testEmbeddedChoiceCase() throws AnnotationAnalysisException, DataStoreException {
        initialiseEmbeddedChoiceCase();
        assertEquals(1, m_aggregatedDSM.listNodes(ROOT_CONTAINER_PATH).size());
        assertEquals(1, m_aggregatedDSM.listNodes(TCONTS_PATH).size());
        assertEquals(1, m_aggregatedDSM.listNodes(TM_ROOT_PATH).size());
        assertEquals(2, m_aggregatedDSM.listNodes(QUEUE_PATH).size());

        ModelNodeId parentId = new ModelNodeId("/container=root-container/container=tconts/name=tcont-1-1-1/container" +
                "=tm-root", NS);
        Map<QName, String> keys = new TreeMap<>();
        keys.put(ID_QNAME, "0");
        ModelNodeKey key = new ModelNodeKey((SortedMap<QName, String>) keys);
        ModelNodeWithAttributes initialQueue = (ModelNodeWithAttributes) m_aggregatedDSM.findNode(QUEUE_PATH, key,
                parentId);
        assertEquals("3", initialQueue.getAttribute(PRIORITY_QNAME).getStringValue());
        assertEquals("1", initialQueue.getAttribute(WEIGHT_QNAME).getStringValue());

        Map<QName, ConfigLeafAttribute> configAttrs = new HashMap<>();
        configAttrs.put(WEIGHT_QNAME,new GenericConfigAttribute(WEIGHT, NS, "2"));
        m_aggregatedDSM.updateNode(initialQueue, parentId, configAttrs, null, false);

        ModelNodeWithAttributes updatedQueue = (ModelNodeWithAttributes) m_aggregatedDSM.findNode(QUEUE_PATH, key,
                parentId);
        assertEquals("2", updatedQueue.getAttribute(WEIGHT_QNAME).getStringValue());
        assertEquals("3", initialQueue.getAttribute(PRIORITY_QNAME).getStringValue());
    }

    private void initialiseEmbeddedChoiceCase() throws AnnotationAnalysisException {
        List<Class> classes = new ArrayList<>();
        classes.add(RootContainer.class);
        EntityRegistryBuilder.updateEntityRegistry("RootContainer", classes, m_entityRegistry, m_schemaRegistry,
                m_persistenceManagerUtil.getEntityDataStoreManager(), m_modelNodeDSMRegistry);
        ModelNodeDSMRegistry modelNodeDSMRegistry = (ModelNodeDSMRegistry) m_context.getBean("modelNodeDSMRegistry");
        modelNodeDSMRegistry.register("ROOT_CONTAINER", ROOT_CONTAINER_PATH, m_xmlSubtreeDSM);
        modelNodeDSMRegistry.register("ROOT_CONTAINER", TCONTS_PATH, m_xmlSubtreeDSM);
        modelNodeDSMRegistry.register("ROOT_CONTAINER", TM_ROOT_PATH, m_xmlSubtreeDSM);
        modelNodeDSMRegistry.register("ROOT_CONTAINER", CHILDREN_TYPE_PATH, m_xmlSubtreeDSM);
        modelNodeDSMRegistry.register("ROOT_CONTAINER", QUEUES_PATH, m_xmlSubtreeDSM);
        modelNodeDSMRegistry.register("ROOT_CONTAINER", QUEUE_PATH, m_xmlSubtreeDSM);
        modelNodeDSMRegistry.register("ROOT_CONTAINER", CFG_TYPE_PATH, m_xmlSubtreeDSM);
        modelNodeDSMRegistry.register("ROOT_CONTAINER", INLINE_PATH, m_xmlSubtreeDSM);

        SchemaRegistryTraverser traverser = new SchemaRegistryTraverser("ROOT_CONTAINER",
                Collections.singletonList((SchemaRegistryVisitor) m_entityModelNodeHelperDeployer), m_schemaRegistry,
                m_schemaRegistry.getModule("embedded-choice-case", RootContainerTestConstants.REVISION));
        traverser.traverse();
        initialiseEmbeddedChoiceCaseEntity();
    }

    private void initialiseEmbeddedChoiceCaseEntity() {
        EntityDataStoreManager enityDataStoreManager = m_persistenceManagerUtil.getEntityDataStoreManager();
        enityDataStoreManager.beginTransaction();
        try {
            RootContainer root = new RootContainer();
            root.setParentId(EMPTY_NODE_ID.getModelNodeIdAsString());
            root.setSchemaPath(SchemaPathUtil.toString(ROOT_CONTAINER_PATH));
            String subtree = "<root-container  xmlns=\"urn:embedded-choice-case-test\"> \n" +
                    "              <tconts>" +
                    "                  <name>tcont-1-1-1</name> \n" +
                    "                  <tm-root> \n" +
                    "                     <queue> \n" +
                    "                        <id>0</id> \n" +
                    "                        <priority>3</priority> \n" +
                    "                        <weight>1</weight> \n" +
                    "                    </queue> \n" +
                    "                    <queue> \n" +
                    "                        <id>1</id> \n" +
                    "                        <priority>2</priority> \n" +
                    "                        <weight>1</weight> \n" +
                    "                    </queue> \n" +
                    "                  </tm-root> \n" +
                    "               </tconts> \n" +
                    "         </root-container > ";

            root.setXmlSubtree(subtree);
            enityDataStoreManager.create(root);
            enityDataStoreManager.commitTransaction();
        } catch (Exception e) {
            enityDataStoreManager.rollbackTransaction();
            throw e;
        }
    }

    // Helper methods
    private void initialiseParentAndChildSameQName() throws Exception {
        List<Class> classes = new ArrayList<>();
        classes.add(Root.class);
        classes.add(RootList.class);
        EntityRegistryBuilder.updateEntityRegistry("Root", classes, m_entityRegistry, m_schemaRegistry,
                m_persistenceManagerUtil.getEntityDataStoreManager(), m_modelNodeDSMRegistry);
        ModelNodeDSMRegistry modelNodeDSMRegistry = (ModelNodeDSMRegistry) m_context.getBean("modelNodeDSMRegistry");
        modelNodeDSMRegistry.register("Root", fromString(YWPCSQ_ROOT_SP_STR), m_xmlSubtreeDSM);
        modelNodeDSMRegistry.register("Root", fromString(YWPCSQ_CWSQ1_SP_STR), m_xmlSubtreeDSM);
        modelNodeDSMRegistry.register("Root", fromString(YWPCSQ_CLWSQ1_SP_STR), m_xmlSubtreeDSM);
        modelNodeDSMRegistry.register("Root", fromString(YWPCSQ_CWSQ2_SP_STR), m_xmlSubtreeDSM);
        modelNodeDSMRegistry.register("Root", fromString(YWPCSQ_CCWSQ2_SP_STR), m_xmlSubtreeDSM);

        modelNodeDSMRegistry.register("Root", fromString(YWPCSQ_ROOT_LIST), m_xmlSubtreeDSM);
        modelNodeDSMRegistry.register("Root", fromString(YWPCSQ_ROOT_LIST_LEAF), m_xmlSubtreeDSM);
        modelNodeDSMRegistry.register("Root", fromString(YWPCSQ_ROOT_LIST_CHILD_CONTAINER), m_xmlSubtreeDSM);
        modelNodeDSMRegistry.register("Root", fromString(YWPCSQ_ROOT_LIST_CONT_LEAF), m_xmlSubtreeDSM);

        SchemaRegistryTraverser traverser = new SchemaRegistryTraverser("JukeBox",
                Collections.singletonList((SchemaRegistryVisitor) m_entityModelNodeHelperDeployer), m_schemaRegistry,
                m_schemaRegistry.getModule("yang-with-parent-child-same-qname", YWPCSQ_REV));
        traverser.traverse();

        createParentAndChildSameQNameNodes();
    }

    private void createParentAndChildSameQNameNodes() throws SchemaPathBuilderException {
        EntityDataStoreManager enityDataStoreManager = m_persistenceManagerUtil.getEntityDataStoreManager();
        enityDataStoreManager.beginTransaction();
        try {
            Root root = new Root();
            root.setParentId(EMPTY_NODE_ID.getModelNodeIdAsString());
            root.setSchemaPath(SchemaPathUtil.toString(fromString(YWPCSQ_ROOT_SP_STR)));
            String subtree = "<root xmlns=\"urn:yang-with-parent-child-same-qname\">\n" + "    <same-qname1>\n" + "  " +
                    "      <same-qname1>\n"
                    + "            <key-leaf>key-value</key-leaf>\n" + "            " +
                    "<value-leaf>leaf-value</value-leaf>\n"
                    + "        </same-qname1>\n" + "    </same-qname1>\n" + "    <same-qname2>\n" + "        " +
                    "<same-qname2>\n"
                    + "            <some-leaf>some-value</some-leaf>\n" + "        </same-qname2>\n" + "    " +
                    "</same-qname2>\n" + "</root>";

            root.setXmlSubtree(subtree);
            enityDataStoreManager.create(root);
            enityDataStoreManager.commitTransaction();
        } catch (Exception e) {
            enityDataStoreManager.rollbackTransaction();
            throw e;
        }
    }

    private void createRootListEntity() throws SchemaPathBuilderException {
        EntityDataStoreManager enityDataStoreManager = m_persistenceManagerUtil.getEntityDataStoreManager();
        enityDataStoreManager.beginTransaction();
        try {
            RootList root = new RootList();
            root.setParentId(EMPTY_NODE_ID.getModelNodeIdAsString());
            root.setSchemaPath(SchemaPathUtil.toString(fromString(YWPCSQ_ROOT_LIST)));
            String subtree = "<root-list xmlns=\"urn:yang-with-parent-child-same-qname\">\n" +
                    "\t<list-leaf>leaf-value1</list-leaf>\n"
                    + "\t<child>\n" + "\t\t<container-leaf>leaf-value2</container-leaf>\n" + "\t</child>\n" +
                    "</root-list>";

            root.setXmlSubtree(subtree);
            enityDataStoreManager.create(root);
            enityDataStoreManager.commitTransaction();
        } catch (Exception e) {
            enityDataStoreManager.rollbackTransaction();
            throw e;
        }
    }

    private void initialiseChoiceCaseEntities() throws Exception {
        List<Class> classes = new ArrayList<>();
        classes.add(DeviceManager.class);
        EntityRegistryBuilder.updateEntityRegistry("DeviceManager", classes, m_entityRegistry, m_schemaRegistry,
                m_persistenceManagerUtil.getEntityDataStoreManager(), m_modelNodeDSMRegistry);
        ModelNodeDSMRegistry modelNodeDSMRegistry = (ModelNodeDSMRegistry) m_context.getBean("modelNodeDSMRegistry");
        modelNodeDSMRegistry.register("DeviceManager", fromString(DEVICE_MGR_SP_STR), m_xmlSubtreeDSM);
        modelNodeDSMRegistry.register("DeviceManager", fromString(DEVICE_HDR_SP_STR), m_xmlSubtreeDSM);
        modelNodeDSMRegistry.register("DeviceManager", fromString(DEVICE_SP_STR), m_xmlSubtreeDSM);
        modelNodeDSMRegistry.register("DeviceManager", fromString(CONF_DEVICE_PROP_SP_STR), m_xmlSubtreeDSM);

        createChoiceCaseNodes();
    }

    private void createChoiceCaseNodes() throws Exception {
        EntityDataStoreManager enityDataStoreManager = m_persistenceManagerUtil.getEntityDataStoreManager();
        enityDataStoreManager.beginTransaction();
        try {
            DeviceManager deviceMgr = new DeviceManager();
            deviceMgr.setParentId(EMPTY_NODE_ID.getModelNodeIdAsString());
            deviceMgr.setSchemaPath(SchemaPathUtil.toString(fromString(DEVICE_MGR_SP_STR)));

            DeviceHolder dh = new DeviceHolder();
            dh.setParentId("/container=device-manager");
            dh.setName("OLT1");
            dh.setSchemaPath(SchemaPathUtil.toString(fromString(DEVICE_HDR_SP_STR)));
            deviceMgr.setDeviceHolders(new HashSet<DeviceHolder>(Arrays.asList(dh)));
            Device device1 = new Device();
            device1.setSchemaPath(SchemaPathUtil.toString(fromString(DEVICE_SP_STR)));
            device1.setParentId("/container=device-manager/container=device-holder/name=OLT1");
            device1.setDeviceId("ONT1");
            dh.setDevices(new HashSet<Device>(Arrays.asList(device1)));
            ConfiguredDeviceProperties cdp = new ConfiguredDeviceProperties();
            cdp.setSchemaPath(SchemaPathUtil.toString(fromString(CONF_DEVICE_PROP_SP_STR)));
            cdp.setParentId("/container=device-manager/container=device-holder/name=OLT1/container=device/device-id" +
                    "=ONT1");
            cdp.setIpAddress("127.0.0.1");
            cdp.setIpPort("123");
            device1.setConfiguredDeviceProperties(cdp);
            enityDataStoreManager.create(deviceMgr);
            enityDataStoreManager.commitTransaction();
        } catch (Exception e) {
            enityDataStoreManager.rollbackTransaction();
            throw e;
        }
    }

    private void testUpdateModelNode() throws DataStoreException {
        XmlModelNodeImpl songModelNode = (XmlModelNodeImpl) m_aggregatedDSM.findNode(SONG_SCHEMA_PATH, SONG_KEY,
                m_albumNodeId);
        QName locationQname = QName.create(JB_NS, JB_REVISION, "location");
        assertNull(songModelNode.getAttribute(locationQname));

        Map<QName, ConfigLeafAttribute> configAttributes = new HashMap<>();
        configAttributes.put(locationQname, new GenericConfigAttribute(LOCATION, JB_NS, "somelocation"));
        m_aggregatedDSM.updateNode(songModelNode, m_albumNodeId, configAttributes, null, false);
        songModelNode = (XmlModelNodeImpl) m_aggregatedDSM.findNode(SONG_SCHEMA_PATH, SONG_KEY, m_albumNodeId);
        assertEquals("somelocation", songModelNode.getAttributes().get(locationQname).getStringValue());

        configAttributes.put(locationQname, null);
        m_aggregatedDSM.updateNode(songModelNode, m_albumNodeId, configAttributes, null, false);
        songModelNode = (XmlModelNodeImpl) m_aggregatedDSM.findNode(SONG_SCHEMA_PATH, SONG_KEY, m_albumNodeId);
        assertNull(songModelNode.getAttribute(locationQname));

        // Verify there is no singer
        songModelNode = (XmlModelNodeImpl) m_aggregatedDSM.findNode(SONG_SCHEMA_PATH, SONG_KEY, m_albumNodeId);
        assertNull(songModelNode.getLeafList(SINGER_QNAME));

        // Update with one singer
        Map<QName, LinkedHashSet<ConfigLeafAttribute>> leafLists = new HashMap<>();
        LinkedHashSet<ConfigLeafAttribute> values = new LinkedHashSet<>();
        values.add(new GenericConfigAttribute(SINGER_LOCAL_NAME, JB_NS, "Singer1"));
        leafLists.put(SINGER_QNAME, values);
        m_aggregatedDSM.updateNode(songModelNode, m_albumNodeId, null, leafLists, false);
        songModelNode = (XmlModelNodeImpl) m_aggregatedDSM.findNode(SONG_SCHEMA_PATH, SONG_KEY, m_albumNodeId);
        assertEquals(1, songModelNode.getLeafList(SINGER_QNAME).size());
    }

    private void testUpdateModelNodeWithInsertIndex() throws DataStoreException {

        assertEquals(1, m_aggregatedDSM.listNodes(SONG_SCHEMA_PATH).size());

        Map<QName, LinkedHashSet<ConfigLeafAttribute>> leafLists = new HashMap<>();
        List<ConfigLeafAttribute> valuesList = new ArrayList<>();
        valuesList.add(0, new GenericConfigAttribute(SINGER_LOCAL_NAME, JB_NS, "Singer1"));
        LinkedHashSet<ConfigLeafAttribute> values = new LinkedHashSet<>(valuesList);
        leafLists.put(SINGER_QNAME, values);
        m_aggregatedDSM.updateNode(getSingerModelNode("Singer1", m_songNodeId), m_songNodeId, null, leafLists, 0,
                false);
        XmlModelNodeImpl songModelNode = (XmlModelNodeImpl) m_aggregatedDSM.findNode(SONG_SCHEMA_PATH, SONG_KEY,
                m_albumNodeId);
        assertEquals(1, songModelNode.getLeafList(SINGER_QNAME).size());
    }

    private XmlModelNodeImpl getSingerModelNode(String singerName, ModelNodeId parentId) throws DataStoreException {
        Map<QName, ConfigLeafAttribute> configAttributes;
        XmlModelNodeImpl songModelNode = (XmlModelNodeImpl) m_aggregatedDSM.findNode(SONG_SCHEMA_PATH,
                new ModelNodeKeyBuilder().appendKey(NAME_QNAME, "My Song").build(), m_albumNodeId);
        configAttributes = new HashMap<>();
        configAttributes.put(SINGER_QNAME, new GenericConfigAttribute(SINGER_LOCAL_NAME, JB_NS, singerName));
        XmlModelNodeImpl singerModelNode = new XmlModelNodeImpl(SINGER_SCHEMA_PATH, configAttributes, Collections
                .<Element>emptyList(),
                songModelNode, parentId, m_xmlModelNodeToXmlMapper, m_modelNodeHelperRegistry, m_schemaRegistry,
                m_subSystemRegistry, null);
        return singerModelNode;
    }

    private void initialiseLibrarySubtreeEntity() throws AnnotationAnalysisException {
        List<Class> classes = new ArrayList<>();
        classes.add(org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.libraryxmlsubtree.Jukebox.class);
        EntityRegistryBuilder.updateEntityRegistry("Jukebox", classes, m_entityRegistry, m_schemaRegistry,
                m_persistenceManagerUtil.getEntityDataStoreManager(), m_modelNodeDSMRegistry);
        ModelNodeDSMRegistry modelNodeDSMRegistry = (ModelNodeDSMRegistry) m_context.getBean("modelNodeDSMRegistry");
        modelNodeDSMRegistry.register("Jukebox", JUKEBOX_SCHEMA_PATH, m_xmlSubtreeDSM);
        modelNodeDSMRegistry.register("Jukebox", LIBRARY_SCHEMA_PATH, m_xmlSubtreeDSM);
        modelNodeDSMRegistry.register("Jukebox", ARTIST_SCHEMA_PATH, m_xmlSubtreeDSM);
        modelNodeDSMRegistry.register("Jukebox", ALBUM_SCHEMA_PATH, m_xmlSubtreeDSM);
        modelNodeDSMRegistry.register("Jukebox", SONG_SCHEMA_PATH, m_xmlSubtreeDSM);
        modelNodeDSMRegistry.register("Jukebox", SINGER_SCHEMA_PATH, m_xmlSubtreeDSM);
        createJukeboxWithLibrarySubtree();
    }

    private void createJukeboxWithLibrarySubtree() {
        org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.libraryxmlsubtree.Jukebox jukebox;
        jukebox = new org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.libraryxmlsubtree.Jukebox();
        jukebox.setSchemaPath(SchemaPathUtil.toString(JUKEBOX_SCHEMA_PATH));
        jukebox.setParentId(EMPTY_NODE_ID.getModelNodeIdAsString());
        org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.libraryxmlsubtree.Library library = new org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.libraryxmlsubtree.Library();
        library.setSchemaPath(SchemaPathUtil.toString(LIBRARY_SCHEMA_PATH));
        library.setParentId(m_jukeboxNodeId.getModelNodeIdAsString());
        String librarySubtree = TestUtil.loadAsString(LIBRARY_XML);
        library.setXmlSubtree(librarySubtree);
        jukebox.setLibrary(library);

        EntityDataStoreManager enityDataStoreManager = m_persistenceManagerUtil.getEntityDataStoreManager();
        enityDataStoreManager.beginTransaction();
        try {
            enityDataStoreManager.create(jukebox);
            enityDataStoreManager.commitTransaction();
        } catch (Exception e) {
            enityDataStoreManager.rollbackTransaction();
            throw e;
        }
    }

    private void initializeAlbumSubtreeEntity() throws AnnotationAnalysisException {
        initializeRegistry();
        createJukeboxWithAlbumSubtree();
    }

    private void initializeCDSubtreeEntity() throws AnnotationAnalysisException {
        initializeRegistry();
        createJukeboxWithCDSubtree();
    }

    private void initializeRegistry() throws AnnotationAnalysisException {
        List<Class> classes = new ArrayList<>();
        classes.add(org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.albumxmlsubtree.Jukebox.class);
        EntityRegistryBuilder.updateEntityRegistry("Jukebox", classes, m_entityRegistry, m_schemaRegistry,
                m_persistenceManagerUtil.getEntityDataStoreManager(), m_modelNodeDSMRegistry);
        ModelNodeDSMRegistry modelNodeDSMRegistry = (ModelNodeDSMRegistry) m_context.getBean("modelNodeDSMRegistry");
        modelNodeDSMRegistry.register("Jukebox", JUKEBOX_SCHEMA_PATH, m_xmlSubtreeDSM);
        modelNodeDSMRegistry.register("Jukebox", LIBRARY_SCHEMA_PATH, m_xmlSubtreeDSM);
        modelNodeDSMRegistry.register("Jukebox", ARTIST_SCHEMA_PATH, m_xmlSubtreeDSM);
        modelNodeDSMRegistry.register("Jukebox", ALBUM_SCHEMA_PATH, m_xmlSubtreeDSM);
        modelNodeDSMRegistry.register("Jukebox", SONG_SCHEMA_PATH, m_xmlSubtreeDSM);
        modelNodeDSMRegistry.register("Jukebox", SINGER_SCHEMA_PATH, m_xmlSubtreeDSM);
        modelNodeDSMRegistry.register("Jukebox", CD_SCHEMA_PATH, m_xmlSubtreeDSM);
    }

    private void createJukeboxWithAlbumSubtree() {
        org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.albumxmlsubtree.Jukebox jukebox = new org.broadband_forum.obbaa
                .netconf.mn.fwk.tests.persistence.entities.albumxmlsubtree.Jukebox();
        jukebox.setParentId(EMPTY_NODE_ID.getModelNodeIdAsString());
        jukebox.setSchemaPath(SchemaPathUtil.toString(JUKEBOX_SCHEMA_PATH));

        org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.albumxmlsubtree.Library library = new org.broadband_forum.obbaa
                .netconf.mn.fwk.tests.persistence.entities.albumxmlsubtree.Library();
        library.setParentId(m_jukeboxNodeId.getModelNodeIdAsString());
        library.setSchemaPath(SchemaPathUtil.toString(LIBRARY_SCHEMA_PATH));

        Artist artist1 = new Artist();
        artist1.setName(ARTIST1_NAME);
        artist1.setParentId(m_libraryNodeId.getModelNodeIdAsString());
        artist1.setSchemaPath(SchemaPathUtil.toString(ARTIST_SCHEMA_PATH));
        artist1.setInsertOrder(0);

        Set<Album> albums = new HashSet<>();
        Album album11 = new Album();
        album11.setName("album11");
        album11.setParentId(m_artist1NodeId.getModelNodeIdAsString());
        album11.setSchemaPath(SchemaPathUtil.toString(ALBUM_SCHEMA_PATH));
        album11.setInsertOrder(0);
        albums.add(album11);
        artist1.setAlbums(albums);

        Artist artist2 = new Artist();
        artist2.setParentId(m_libraryNodeId.getModelNodeIdAsString());
        artist2.setSchemaPath(SchemaPathUtil.toString(ARTIST_SCHEMA_PATH));
        artist2.setName(ARTIST2_NAME);
        artist2.setInsertOrder(1);

        albums = new HashSet<>();
        Album album21 = new Album();
        album21.setParentId(m_artist2NodeId.getModelNodeIdAsString());
        album21.setSchemaPath(SchemaPathUtil.toString(ALBUM_SCHEMA_PATH));
        album21.setName("album21");
        album21.setInsertOrder(1);

        Album album22 = new Album();
        album22.setParentId(m_artist2NodeId.getModelNodeIdAsString());
        album22.setSchemaPath(SchemaPathUtil.toString(ALBUM_SCHEMA_PATH));
        album22.setName(ALBUM2_NAME);
        album22.setInsertOrder(2);
        String albumSubtree = TestUtil.loadAsString(ALBUM_SUBTREE_XML);
        album22.setXmlSubtree(albumSubtree);
        albums.add(album21);
        albums.add(album22);
        artist2.setAlbums(albums);

        List<Artist> artists = new ArrayList<>();
        artists.add(artist1);
        artists.add(artist2);
        library.setArtists(artists);
        jukebox.setLibrary(library);

        storeEntity(jukebox);
    }

    private void storeEntity(org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.albumxmlsubtree.Jukebox jukebox) {
        EntityDataStoreManager enityDataStoreManager = m_persistenceManagerUtil.getEntityDataStoreManager();
        enityDataStoreManager.beginTransaction();
        try {
            enityDataStoreManager.create(jukebox);
            enityDataStoreManager.commitTransaction();
        } catch (Exception e) {
            enityDataStoreManager.rollbackTransaction();
            throw e;
        }
    }

    private void createJukeboxWithCDSubtree() {
        org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.albumxmlsubtree.Jukebox jukebox = new org.broadband_forum.obbaa
                .netconf.mn.fwk.tests.persistence.entities.albumxmlsubtree.Jukebox();
        jukebox.setParentId(EMPTY_NODE_ID.getModelNodeIdAsString());
        jukebox.setSchemaPath(SchemaPathUtil.toString(JUKEBOX_SCHEMA_PATH));

        org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.albumxmlsubtree.Library library = new org.broadband_forum.obbaa
                .netconf.mn.fwk.tests.persistence.entities.albumxmlsubtree.Library();
        library.setParentId(m_jukeboxNodeId.getModelNodeIdAsString());
        library.setSchemaPath(SchemaPathUtil.toString(LIBRARY_SCHEMA_PATH));

        Artist artist1 = new Artist();
        artist1.setName(ARTIST1_NAME);
        artist1.setParentId(m_libraryNodeId.getModelNodeIdAsString());
        artist1.setSchemaPath(SchemaPathUtil.toString(ARTIST_SCHEMA_PATH));
        artist1.setInsertOrder(0);

        Set<Album> albums = new HashSet<>();
        Album album11 = new Album();
        album11.setName("album11");
        album11.setParentId(m_artist1NodeId.getModelNodeIdAsString());
        album11.setSchemaPath(SchemaPathUtil.toString(ALBUM_SCHEMA_PATH));
        album11.setInsertOrder(0);

        String albumSubtree = TestUtil.loadAsString(ALBUM_SUBTREE_with_CD_XML);
        album11.setXmlSubtree(albumSubtree);

        albums.add(album11);
        artist1.setAlbums(albums);

        List<Artist> artists = new ArrayList<>();
        artists.add(artist1);
        library.setArtists(artists);
        jukebox.setLibrary(library);

        storeEntity(jukebox);
    }

    private void setAlbum22Year(String year) {
        EntityDataStoreManager enityDataStoreManager = m_persistenceManagerUtil.getEntityDataStoreManager();
        enityDataStoreManager.beginTransaction();
        try {
            List<Album> albums = enityDataStoreManager
                    .findAll(Album.class);
            for (Album album : albums) {
                if (album.getName().equals(ALBUM2_NAME)) {
                    album.setYear(year);
                    break;
                }
            }
            enityDataStoreManager.commitTransaction();
        } catch (Exception e) {
            enityDataStoreManager.rollbackTransaction();
            throw e;
        }
    }

    @YangContainer(name = "device-manager", namespace = CHOICE_CASE_NS, revision = "2015-12-14")
    @Entity
    @Table(name = "DM")
    class DeviceManager {
        @Id
        @YangParentId
        @Column(name = YangParentId.PARENT_ID_FIELD_NAME)
        private String parentId;

        @YangSchemaPath
        @Column(length = 1000)
        private String schemaPath;

        @YangChild
        @OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY, orphanRemoval = true)
        private Set<DeviceHolder> deviceHolders = new HashSet<>();

        public String getParentId() {
            return parentId;
        }

        public void setParentId(String parentId) {
            this.parentId = parentId;
        }

        public String getSchemaPath() {
            return schemaPath;
        }

        public void setSchemaPath(String schemaPath) {
            this.schemaPath = schemaPath;
        }

        public Set<DeviceHolder> getDeviceHolders() {
            return deviceHolders;
        }

        public void setDeviceHolders(Set<DeviceHolder> deviceHolders) {
            this.deviceHolders = deviceHolders;
        }
    }

    public static class DeviceHolderPK implements Serializable {
        String name;
        String parentId;

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            result = prime * result + ((parentId == null) ? 0 : parentId.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            DeviceHolderPK other = (DeviceHolderPK) obj;
            if (name == null) {
                if (other.name != null)
                    return false;
            } else if (!name.equals(other.name))
                return false;
            if (parentId == null) {
                if (other.parentId != null)
                    return false;
            } else if (!parentId.equals(other.parentId))
                return false;
            return true;
        }
    }

    @YangList(name = "device-holder", namespace = CHOICE_CASE_NS, revision = "2015-12-14")
    @Entity
    @IdClass(value = DeviceHolderPK.class)
    @Table(name = "DH")
    class DeviceHolder {
        @Id
        @YangParentId
        @Column(name = YangParentId.PARENT_ID_FIELD_NAME)
        private String parentId;

        @YangSchemaPath
        @Column(length = 1000)
        private String schemaPath;

        @Id
        @YangListKey(name = "name")
        @Column
        private String name;

        @OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY, orphanRemoval = true)
        @YangChild(name = "device")
        private Set<Device> devices = new TreeSet<>();

        public String getParentId() {
            return parentId;
        }

        public void setParentId(String parentId) {
            this.parentId = parentId;
        }

        public String getSchemaPath() {
            return schemaPath;
        }

        public void setSchemaPath(String schemaPath) {
            this.schemaPath = schemaPath;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Set<Device> getDevices() {
            return devices;
        }

        public void setDevices(Set<Device> devices) {
            this.devices = devices;
        }
    }

    public static class DevicePK implements Serializable {
        private String parentId;
        private String deviceId;

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((deviceId == null) ? 0 : deviceId.hashCode());
            result = prime * result + ((parentId == null) ? 0 : parentId.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            DevicePK other = (DevicePK) obj;
            if (deviceId == null) {
                if (other.deviceId != null)
                    return false;
            } else if (!deviceId.equals(other.deviceId))
                return false;
            if (parentId == null) {
                if (other.parentId != null)
                    return false;
            } else if (!parentId.equals(other.parentId))
                return false;
            return true;
        }
    }

    @YangList(name = "device", namespace = CHOICE_CASE_NS, revision = "2015-12-14")
    @Entity
    @IdClass(DevicePK.class)
    @Table(name = "D")
    class Device {
        @Id
        @YangParentId
        @Column(name = YangParentId.PARENT_ID_FIELD_NAME)
        private String parentId;

        @Id
        @YangListKey(name = "device-id")
        @Column
        private String deviceId;

        @YangSchemaPath
        @Column(length = 1000)
        private String schemaPath;

        @YangAttribute(name = "duid")
        @Column
        private String duid;

        @YangAttribute(name = "dhcp-option-82")
        @Column
        private String dhcpOption82;

        @YangChild(name = "configured-device-properties")
        @OneToOne(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY, orphanRemoval = true)
        private ConfiguredDeviceProperties configuredDeviceProperties;

        public String getParentId() {
            return parentId;
        }

        public void setParentId(String parentId) {
            this.parentId = parentId;
        }

        public String getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(String deviceId) {
            this.deviceId = deviceId;
        }

        public String getSchemaPath() {
            return schemaPath;
        }

        public void setSchemaPath(String schemaPath) {
            this.schemaPath = schemaPath;
        }

        public String getDuid() {
            return duid;
        }

        public void setDuid(String duid) {
            this.duid = duid;
        }

        public String getDhcpOption82() {
            return dhcpOption82;
        }

        public void setDhcpOption82(String dhcpOption82) {
            this.dhcpOption82 = dhcpOption82;
        }

        public ConfiguredDeviceProperties getConfiguredDeviceProperties() {
            return configuredDeviceProperties;
        }

        public void setConfiguredDeviceProperties(ConfiguredDeviceProperties configuredDeviceProperties) {
            this.configuredDeviceProperties = configuredDeviceProperties;
        }
    }

    @YangContainer(name = "configured-device-properties", namespace = CHOICE_CASE_NS, revision = "2015-12-14")
    @Entity
    @Table(name = "CDP")
    class ConfiguredDeviceProperties {
        @Id
        @YangParentId
        @Column(name = YangParentId.PARENT_ID_FIELD_NAME)
        private String parentId;

        @YangSchemaPath
        @Column(length = 1000)
        private String schemaPath;

        @YangAttribute(name = "ip-address")
        @Column
        private String ipAddress;

        @YangAttribute(name = "ip-port")
        @Column
        private String ipPort;

        public String getParentId() {
            return parentId;
        }

        public void setParentId(String parentId) {
            this.parentId = parentId;
        }

        public String getSchemaPath() {
            return schemaPath;
        }

        public void setSchemaPath(String schemaPath) {
            this.schemaPath = schemaPath;
        }

        public String getIpAddress() {
            return ipAddress;
        }

        public void setIpAddress(String ipAddress) {
            this.ipAddress = ipAddress;
        }

        public String getIpPort() {
            return ipPort;
        }

        public void setIpPort(String ipPort) {
            this.ipPort = ipPort;
        }
    }

    @After
    public void tearDown() {
        RequestScope.resetScope();
    }

    @AfterClass
    public static void tearDownClass() {
        RequestScope.setEnableThreadLocalInUT(false);
    }

    private interface ScopeTemplate<T> {
        T execute() throws DataStoreException;
    }
}
