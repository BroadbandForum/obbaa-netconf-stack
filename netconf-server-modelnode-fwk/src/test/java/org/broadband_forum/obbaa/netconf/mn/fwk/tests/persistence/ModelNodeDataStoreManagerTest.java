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

package org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence;

import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn.CONTAINER;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ADDRESS_NAME_LOCAL_NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ADDRESS_NAME_Q_NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ADDRESS_Q_NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ADDR_NS;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ALBUM_LOCAL_NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ALBUM_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ARTIST_LOCAL_NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ARTIST_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.CA_CERT_CONTAINER_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.CA_CERT_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.CELL_PHONE;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.CERT_NS;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.CHENNAI_OFFICE_ADDRESS;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.HOME_ADDRESS;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.HOME_ADDRESSES_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.HOME_ADDRESS_LOCAL_NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.HOME_CELL;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.HOME_LAND_LINE;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.HOME_TELPHONE_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JB_NS;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JB_REVISION;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JUKEBOX_LOCAL_NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JUKEBOX_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.LAND_LINE;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.LIBRARY_LOCAL_NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.LIBRARY_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants
        .MY_HOME_ADDRESS_KARNATAKA_560092;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.NAME_QNAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.OFFICE_ADDRESS;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.OFFICE_ADDRESSES_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.OFFICE_CELL;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.OFFICE_LAND_LINE;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.OFFICE_TELPHONE_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.PMA_CERT_CONTAINER_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.PMA_CERT_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.QNAME_CERTIFICATE_ID;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.REVISION;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.SINGER_QNAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.SINGER_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.SONG_LOCAL_NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.SONG_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.TELEPHONE_NUMBER_QNAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.TELEPHONE_TYPE_QNAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeKeyBuilder;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.utils.AnnotationAnalysisException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigAttributeHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.AnnotationBasedModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.EntityRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.annotation.dao.JukeboxDao;
import org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.TestConstants;
import org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.addresses.TelephoneNumber;
import org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.certificates.v2.CertMgmt;
import org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.certificates.v2.Certificate;
import org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.certificates.v2.TrustedCaCerts;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import org.broadband_forum.obbaa.netconf.api.util.SchemaPathUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.addresses.HomeAddress;
import org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.addresses.OfficeAddress;
import org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.certificates.v2.PmaCerts;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.DataStoreException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDSMRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDSMRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeKey;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.utils.EntityRegistryBuilder;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.GenericConfigAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.EntityRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.utils.TestTxUtils;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.broadband_forum.obbaa.netconf.persistence.EntityDataStoreManager;
import org.broadband_forum.obbaa.netconf.persistence.PersistenceManagerUtil;
import org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.Album;
import org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.Artist;
import org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.Jukebox;
import org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.Library;
import org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.Song;

/**
 * Created by keshava on 4/12/15.
 */
public class ModelNodeDataStoreManagerTest {

    private ModelNodeDataStoreManager m_dataStoreManager;
    private JukeboxDao m_jukeboxDao;
    private EntityRegistry m_entityRegistry;
    private SchemaRegistry m_schemaRegistry;
    private ModelNodeHelperRegistry m_modelNodeHelperRegistry;
    private ModelNodeDSMRegistry m_modelNodeDSMRegistry;
    private ModelNodeId m_jukeboxNodeId = new ModelNodeId().addRdn(new ModelNodeRdn(CONTAINER, JB_NS,
            JUKEBOX_LOCAL_NAME));
    private ModelNodeId m_libraryNodeId = new ModelNodeId(m_jukeboxNodeId).addRdn(new ModelNodeRdn(CONTAINER, JB_NS,
            LIBRARY_LOCAL_NAME));
    private final ModelNodeId m_artistId = new ModelNodeId(m_libraryNodeId).addRdn(new ModelNodeRdn(CONTAINER, JB_NS,
            ARTIST_LOCAL_NAME))
            .addRdn(new ModelNodeRdn("name", JB_NS, "keshava"));
    private final ModelNodeId m_albumId = new ModelNodeId(m_artistId).addRdn(new ModelNodeRdn(CONTAINER, JB_NS,
            ALBUM_LOCAL_NAME))
            .addRdn(new ModelNodeRdn("name", JB_NS, "Refactor Times"));
    private final ModelNodeId m_songId = new ModelNodeId(m_albumId).addRdn(new ModelNodeRdn(CONTAINER, JB_NS,
            SONG_LOCAL_NAME))
            .addRdn(new ModelNodeRdn("name", JB_NS, "My Song"));

    private Jukebox m_newJukebox;
    private PersistenceManagerUtil m_persistenceManagerUtil;

    @Before
    public void setUp() throws AnnotationAnalysisException, SchemaBuildException, GetAttributeException {
        m_entityRegistry = new EntityRegistryImpl();
        m_modelNodeDSMRegistry = new ModelNodeDSMRegistryImpl();
        AbstractApplicationContext context = new ClassPathXmlApplicationContext
                ("/modeldatastoremanagertest/test-applicationContext.xml");
        List<Class> classes = new ArrayList<>();
        classes.add(Jukebox.class);
        classes.add(CertMgmt.class);
        classes.add(TrustedCaCerts.class);
        classes.add(PmaCerts.class);
        classes.add(HomeAddress.class);
        classes.add(OfficeAddress.class);
        List<YangTextSchemaSource> yangSources = TestUtil.getJukeBoxYangs();
        yangSources.addAll(TestUtil.getByteSources(Arrays.asList("/yangs/addresses@2015-12-08.yang",
                "/yangs/certificates@2015-12-08.yang")));
        m_schemaRegistry = new SchemaRegistryImpl(yangSources, new NoLockService());
        m_persistenceManagerUtil = (PersistenceManagerUtil) context.getBean("persistenceManagerUtil");
        EntityRegistryBuilder.updateEntityRegistry("Jukebox", classes, m_entityRegistry, m_schemaRegistry,
                m_persistenceManagerUtil.getEntityDataStoreManager(), m_modelNodeDSMRegistry);
        m_jukeboxDao = ((JukeboxDao) context.getBean("jukeboxDao"));
        m_modelNodeHelperRegistry = mock(ModelNodeHelperRegistry.class);
        Map<QName, ConfigAttributeHelper> value = new LinkedHashMap<>();
        ConfigAttributeHelper configHelper = mock(ConfigAttributeHelper.class);
        doAnswer(new Answer<ConfigLeafAttribute>() {

            @Override
            public ConfigLeafAttribute answer(InvocationOnMock invocation) throws Throwable {
                ModelNodeWithAttributes modelnode = (ModelNodeWithAttributes) invocation.getArguments()[0];
                return modelnode.getAttribute(NAME_QNAME);
            }

        }).when(configHelper).getValue(any(ModelNode.class));
        value.put(NAME_QNAME, configHelper);
        when(m_modelNodeHelperRegistry.getNaturalKeyHelpers(ARTIST_SCHEMA_PATH)).thenReturn(value);

        m_dataStoreManager = new AnnotationBasedModelNodeDataStoreManager(m_persistenceManagerUtil, m_entityRegistry,
                m_schemaRegistry, m_modelNodeHelperRegistry, null, m_modelNodeDSMRegistry);
        m_dataStoreManager = TestTxUtils.getTxDecoratedDSM(m_persistenceManagerUtil, m_dataStoreManager);
        createJukebox();
    }

    private void createJukebox() {
        m_newJukebox = new Jukebox();
        m_newJukebox.setSchemaPath(SchemaPathUtil.toString(JUKEBOX_SCHEMA_PATH));
        m_newJukebox.setParentId(TestConstants.EMPTY_NODE_ID.getModelNodeIdAsString());
        Library library = new Library();
        library.setSchemaPath(SchemaPathUtil.toString(LIBRARY_SCHEMA_PATH));
        library.setParentId(m_jukeboxNodeId.getModelNodeIdAsString());
        Artist artist = new Artist();
        artist.setSchemaPath(SchemaPathUtil.toString(ARTIST_SCHEMA_PATH));
        artist.setName("keshava");
        artist.setParentId(m_libraryNodeId.getModelNodeIdAsString());
        artist.setInsertOrder(0);
        library.addArtists(artist);
        m_newJukebox.setLibrary(library);
        Album refactorHits = new Album();
        refactorHits.setSchemaPath(SchemaPathUtil.toString(ALBUM_SCHEMA_PATH));
        refactorHits.setName("Refactor Times");
        refactorHits.setParentId(m_artistId.getModelNodeIdAsString());
        refactorHits.setInsertOrder(0);
        Song entitySong = new Song();
        entitySong.setSchemaPath(SchemaPathUtil.toString(SONG_SCHEMA_PATH));
        entitySong.setParentId(m_albumId.getModelNodeIdAsString());
        entitySong.setName("Entity Refactor");
        entitySong.setInsertOrder(0);

        refactorHits.getSongs().add(entitySong);
        m_newJukebox.getLibrary().getArtists().get(0).getAlbums().add(refactorHits);
        if (m_jukeboxDao.findByIdWithWriteLock(TestConstants.EMPTY_NODE_ID.getModelNodeIdAsString()) == null) {
            m_jukeboxDao.createAndCommit(m_newJukebox);
        }
    }

    // Test Read methods
    @Test
    public void testListNodes() throws DataStoreException {

        assertEquals(1, m_dataStoreManager.listNodes(JUKEBOX_SCHEMA_PATH).size());
        assertEquals(1, m_dataStoreManager.listNodes(LIBRARY_SCHEMA_PATH).size());
        assertEquals(1, m_dataStoreManager.listNodes(ARTIST_SCHEMA_PATH).size());
        assertEquals(1, m_dataStoreManager.listNodes(ALBUM_SCHEMA_PATH).size());
        assertEquals(1, m_dataStoreManager.listNodes(SONG_SCHEMA_PATH).size());

        Jukebox jukebox = m_jukeboxDao.findByIdWithWriteLock(TestConstants.EMPTY_NODE_ID.getModelNodeIdAsString());
        Album refactorHits = new Album();
        refactorHits.setSchemaPath(SchemaPathUtil.toString(ALBUM_SCHEMA_PATH));
        refactorHits.setParentId(m_artistId.getModelNodeIdAsString());
        refactorHits.setName("Refactor Times Side B");
        Song entitySong = new Song();
        entitySong.setSchemaPath(SchemaPathUtil.toString(SONG_SCHEMA_PATH));
        ModelNodeId newAlbumId = new ModelNodeId(m_artistId).addRdn(new ModelNodeRdn(CONTAINER, JB_NS,
                ALBUM_LOCAL_NAME))
                .addRdn(new ModelNodeRdn("name", JB_NS, "Refactor Times Side B"));
        entitySong.setParentId(newAlbumId.getModelNodeIdAsString());
        entitySong.setName("Node Refactor");
        refactorHits.getSongs().add(entitySong);
        jukebox.getLibrary().getArtists().get(0).getAlbums().add(refactorHits);
        m_jukeboxDao.create(jukebox);

        assertEquals(1, m_dataStoreManager.listNodes(JUKEBOX_SCHEMA_PATH).size());
        assertEquals(1, m_dataStoreManager.listNodes(LIBRARY_SCHEMA_PATH).size());
        assertEquals(1, m_dataStoreManager.listNodes(ARTIST_SCHEMA_PATH).size());
        assertEquals(2, m_dataStoreManager.listNodes(ALBUM_SCHEMA_PATH).size());
        assertEquals(2, m_dataStoreManager.listNodes(SONG_SCHEMA_PATH).size());

    }

    @Test
    public void testListChildNodes() throws DataStoreException {
        assertEquals(1, m_dataStoreManager.listChildNodes(ARTIST_SCHEMA_PATH, m_libraryNodeId).size());
        assertEquals(1, m_dataStoreManager.listChildNodes(ALBUM_SCHEMA_PATH, m_artistId).size());
        assertEquals(1, m_dataStoreManager.listChildNodes(SONG_SCHEMA_PATH, m_albumId).size());
    }

    @Test
    public void testListNodeWithParentKey() throws DataStoreException, SchemaBuildException {
        addHomeAddresses();
        addOfficeAddresses();

        List<YangTextSchemaSource> yangs = new ArrayList<>();
        yangs.add(TestUtil.getByteSource("/yangs/addresses@2015-12-08.yang"));
        m_schemaRegistry.loadSchemaContext("address-model", yangs, null, Collections.emptyMap());

        ModelNodeId homeAddressNodeId = new ModelNodeId().addRdn(new ModelNodeRdn(CONTAINER, ADDR_NS,
                HOME_ADDRESS_LOCAL_NAME))
                .addRdn(new ModelNodeRdn(ADDRESS_NAME_LOCAL_NAME, ADDR_NS, HOME_ADDRESS));
        assertEquals(2, m_dataStoreManager.listChildNodes(HOME_TELPHONE_SCHEMA_PATH, homeAddressNodeId).size());
    }

    @Test
    public void testFindNode() throws DataStoreException {
        addPmaCerts();
        addCaCerts();

        Map<QName, String> keys = new LinkedHashMap<>();
        ModelNodeWithAttributes caCertContainerNode = (ModelNodeWithAttributes) m_dataStoreManager.findNode
                (CA_CERT_CONTAINER_SCHEMA_PATH,
                new ModelNodeKey(keys), TestConstants.CERT_MGMT_NODE_ID);
        assertEquals(CA_CERT_CONTAINER_SCHEMA_PATH.getLastComponent(), caCertContainerNode.getQName());

        ModelNodeWithAttributes pmaCertContainerNode = (ModelNodeWithAttributes) m_dataStoreManager.findNode
                (PMA_CERT_CONTAINER_SCHEMA_PATH,
                new ModelNodeKey(keys), TestConstants.CERT_MGMT_NODE_ID);
        assertEquals(PMA_CERT_CONTAINER_SCHEMA_PATH.getLastComponent(), pmaCertContainerNode.getQName());

        keys.put(QNAME_CERTIFICATE_ID, "pma-cert1");
        ModelNodeWithAttributes pmaCertNode = (ModelNodeWithAttributes) m_dataStoreManager.findNode
                (PMA_CERT_SCHEMA_PATH, new ModelNodeKey(keys), TestConstants.PMA_CERT_NODE_ID);
        assertEquals(PMA_CERT_SCHEMA_PATH.getLastComponent(), pmaCertNode.getQName());
        assertEquals("pma-cert1-binary", pmaCertNode.getAttributes().get(QName.create(CERT_NS, REVISION,
                "cert-binary")).getStringValue());

        keys.put(QNAME_CERTIFICATE_ID, "pma-cert2");
        pmaCertNode = (ModelNodeWithAttributes) m_dataStoreManager.findNode(PMA_CERT_SCHEMA_PATH, new ModelNodeKey
                (keys), TestConstants.PMA_CERT_NODE_ID);
        assertEquals(PMA_CERT_SCHEMA_PATH.getLastComponent(), pmaCertNode.getQName());
        assertEquals("pma-cert2-binary", pmaCertNode.getAttributes().get(QName.create(CERT_NS, REVISION,
                "cert-binary")).getStringValue());

        keys.put(QNAME_CERTIFICATE_ID, "ca-cert1");
        pmaCertNode = (ModelNodeWithAttributes) m_dataStoreManager.findNode(CA_CERT_SCHEMA_PATH, new ModelNodeKey
                (keys), TestConstants.CA_CERT_NODE_ID);
        assertEquals(CA_CERT_SCHEMA_PATH.getLastComponent(), pmaCertNode.getQName());
        assertEquals("ca-cert1-binary", pmaCertNode.getAttributes().get(QName.create(CERT_NS, REVISION,
                "cert-binary")).getStringValue());

        keys.put(QNAME_CERTIFICATE_ID, "ca-cert2");
        pmaCertNode = (ModelNodeWithAttributes) m_dataStoreManager.findNode(CA_CERT_SCHEMA_PATH, new ModelNodeKey
                (keys), TestConstants.CA_CERT_NODE_ID);
        assertEquals(CA_CERT_SCHEMA_PATH.getLastComponent(), pmaCertNode.getQName());
        assertEquals("ca-cert2-binary", pmaCertNode.getAttributes().get(QName.create(CERT_NS, REVISION,
                "cert-binary")).getStringValue());
    }

    @Test
    public void testFindNodeForComplicatedCases() throws DataStoreException {
        addHomeAddresses();
        addOfficeAddresses();

        ModelNodeKey homeAddressKey = new ModelNodeKeyBuilder().appendKey(ADDRESS_NAME_Q_NAME, HOME_ADDRESS).build();
        ModelNodeWithAttributes homeAddressNode = (ModelNodeWithAttributes) m_dataStoreManager.findNode
                (HOME_ADDRESSES_SCHEMA_PATH,
                homeAddressKey, new ModelNodeId());
        assertEquals(HOME_ADDRESSES_SCHEMA_PATH.getLastComponent(), homeAddressNode.getQName());
        assertEquals(MY_HOME_ADDRESS_KARNATAKA_560092, homeAddressNode.getAttributes().get(ADDRESS_Q_NAME)
                .getStringValue());

        ModelNodeKey homeLandLineKey = new ModelNodeKeyBuilder().appendKey(TELEPHONE_TYPE_QNAME, LAND_LINE).build();
        ModelNodeWithAttributes homeTelephoneNode = (ModelNodeWithAttributes) m_dataStoreManager.findNode
                (HOME_TELPHONE_SCHEMA_PATH,
                homeLandLineKey, TestConstants.HOME_ADDRESS_NODE_ID);
        assertEquals(HOME_TELPHONE_SCHEMA_PATH.getLastComponent(), homeTelephoneNode.getQName());
        assertEquals(LAND_LINE, homeTelephoneNode.getAttributes().get(TELEPHONE_TYPE_QNAME).getStringValue());
        assertEquals(HOME_LAND_LINE, homeTelephoneNode.getAttributes().get(TELEPHONE_NUMBER_QNAME).getStringValue());

        ModelNodeKey homeCellKey = new ModelNodeKeyBuilder().appendKey(TELEPHONE_TYPE_QNAME, CELL_PHONE).build();
        ModelNodeWithAttributes homeCellode = (ModelNodeWithAttributes) m_dataStoreManager.findNode
                (HOME_TELPHONE_SCHEMA_PATH,
                homeCellKey, TestConstants.HOME_ADDRESS_NODE_ID);
        assertEquals(HOME_TELPHONE_SCHEMA_PATH.getLastComponent(), homeCellode.getQName());
        assertEquals(CELL_PHONE, homeCellode.getAttributes().get(TELEPHONE_TYPE_QNAME).getStringValue());
        assertEquals(HOME_CELL, homeCellode.getAttributes().get(TELEPHONE_NUMBER_QNAME).getStringValue());


        ModelNodeKey officeAddressKey = new ModelNodeKeyBuilder().appendKey(ADDRESS_NAME_Q_NAME, OFFICE_ADDRESS)
                .build();
        ModelNodeWithAttributes officeAddressNode = (ModelNodeWithAttributes) m_dataStoreManager.findNode
                (OFFICE_ADDRESSES_SCHEMA_PATH,
                officeAddressKey, new ModelNodeId());
        assertEquals(OFFICE_ADDRESSES_SCHEMA_PATH.getLastComponent(), officeAddressNode.getQName());
        assertEquals(CHENNAI_OFFICE_ADDRESS, officeAddressNode.getAttributes().get(ADDRESS_Q_NAME).getStringValue());

        ModelNodeKey officeLandLineKey = new ModelNodeKeyBuilder().appendKey(TELEPHONE_TYPE_QNAME, LAND_LINE).build();
        ModelNodeWithAttributes officeLandLineNode = (ModelNodeWithAttributes) m_dataStoreManager.findNode
                (OFFICE_TELPHONE_SCHEMA_PATH,
                officeLandLineKey, TestConstants.OFFICE_ADDRESS_NODE_ID);
        assertEquals(OFFICE_TELPHONE_SCHEMA_PATH.getLastComponent(), officeLandLineNode.getQName());
        assertEquals(LAND_LINE, officeLandLineNode.getAttributes().get(TELEPHONE_TYPE_QNAME).getStringValue());
        assertEquals(OFFICE_LAND_LINE, officeLandLineNode.getAttributes().get(TELEPHONE_NUMBER_QNAME).getStringValue());

        ModelNodeKey officeCellKey = new ModelNodeKeyBuilder().appendKey(TELEPHONE_TYPE_QNAME, CELL_PHONE).build();
        ModelNodeWithAttributes officeCellNode = (ModelNodeWithAttributes) m_dataStoreManager.findNode
                (OFFICE_TELPHONE_SCHEMA_PATH,
                officeCellKey, TestConstants.OFFICE_ADDRESS_NODE_ID);
        assertEquals(OFFICE_TELPHONE_SCHEMA_PATH.getLastComponent(), officeCellNode.getQName());
        assertEquals(CELL_PHONE, officeCellNode.getAttributes().get(TELEPHONE_TYPE_QNAME).getStringValue());
        assertEquals(OFFICE_CELL, officeCellNode.getAttributes().get(TELEPHONE_NUMBER_QNAME).getStringValue());

    }

    // Test Write methods - Create and update
    @Test
    public void testCreateModelNodeJukebox() throws DataStoreException {
        assertEquals(1, m_dataStoreManager.listNodes(JUKEBOX_SCHEMA_PATH).size());
        assertEquals(1, m_dataStoreManager.listNodes(LIBRARY_SCHEMA_PATH).size());
        assertEquals(1, m_dataStoreManager.listNodes(ARTIST_SCHEMA_PATH).size());
        assertEquals(1, m_dataStoreManager.listNodes(ALBUM_SCHEMA_PATH).size());
        assertEquals(1, m_dataStoreManager.listNodes(SONG_SCHEMA_PATH).size());

        //Case 1: Add one artist to existing library container
        m_dataStoreManager.createNode(getArtistModelNode("new-artist"), m_libraryNodeId);
        assertEquals(2, m_dataStoreManager.listNodes(ARTIST_SCHEMA_PATH).size());

        // Case 2 : Add one album to existing artist list
        ModelNodeId newArtistId = new ModelNodeId(m_libraryNodeId).addRdn(new ModelNodeRdn(CONTAINER, JB_NS,
                ARTIST_LOCAL_NAME))
                .addRdn(new ModelNodeRdn("name", JB_NS, "new-artist"));
        m_dataStoreManager.createNode(getAlbumModelNode("new-album", newArtistId), newArtistId);

        assertEquals(2, m_dataStoreManager.listNodes(ALBUM_SCHEMA_PATH).size());

        // Case 3 : Add one song to existing album list
        ModelNodeId newAlbumId = new ModelNodeId(newArtistId).addRdn(new ModelNodeRdn(CONTAINER, JB_NS,
                ALBUM_LOCAL_NAME))
                .addRdn(new ModelNodeRdn("name", JB_NS, "new-album"));
        m_dataStoreManager.createNode(getSongModelNode("song-name", newAlbumId), newAlbumId);

        assertEquals(2, m_dataStoreManager.listNodes(SONG_SCHEMA_PATH).size());

    }

    @Test
    public void testCreateModelNodeJukeboxWithInsertIndex() throws DataStoreException {
        List<ModelNode> listArtistNodes = m_dataStoreManager.listNodes(ARTIST_SCHEMA_PATH);

        //Case 1a: Add one artist as the first element to the existing library container
        ModelNode newArtistInFirstIndex = m_dataStoreManager.createNode(getArtistModelNode
                ("new-artist-in-first-index"), m_libraryNodeId, 0);
        List<ModelNode> expectedArtistList = new ArrayList<ModelNode>();
        expectedArtistList.add(newArtistInFirstIndex);
        expectedArtistList.addAll(listArtistNodes);

        List<ModelNode> actualArtistList = new ArrayList<ModelNode>(m_dataStoreManager.listNodes(ARTIST_SCHEMA_PATH));
        assertEquals(2, actualArtistList.size());

        for (int i = 0; i < actualArtistList.size(); i++) {
            assertEquals(0, ((ModelNodeWithAttributes) expectedArtistList.get(i)).compareTo((ModelNodeWithAttributes)
                    actualArtistList.get(i)));
        }

        //Case 1b: Add one artist as the last element to the existing library container
        ModelNode newArtistInLastIndex = m_dataStoreManager.createNode(getArtistModelNode("new-artist-in-last-index")
                , m_libraryNodeId, 2);
        expectedArtistList = new ArrayList<ModelNode>();
        expectedArtistList.addAll(actualArtistList);
        expectedArtistList.add(newArtistInLastIndex);

        actualArtistList = m_dataStoreManager.listNodes(ARTIST_SCHEMA_PATH);
        assertEquals(3, actualArtistList.size());
        for (int i = 0; i < actualArtistList.size(); i++) {
            assertEquals(0, ((ModelNodeWithAttributes) expectedArtistList.get(i)).compareTo((ModelNodeWithAttributes)
                    actualArtistList.get(i)));
        }

        //Case 1c: Add one artist in between to the existing library container
        ModelNode newArtistInBetween = m_dataStoreManager.createNode(getArtistModelNode("new-artist-in-between"),
                m_libraryNodeId, 1);
        expectedArtistList = new ArrayList<ModelNode>();
        expectedArtistList.add(newArtistInFirstIndex);
        expectedArtistList.add(newArtistInBetween);
        expectedArtistList.addAll(listArtistNodes);
        expectedArtistList.add(newArtistInLastIndex);

        actualArtistList = m_dataStoreManager.listNodes(ARTIST_SCHEMA_PATH);
        assertEquals(4, actualArtistList.size());
        for (int i = 0; i < actualArtistList.size(); i++) {
            assertEquals(0, ((ModelNodeWithAttributes) expectedArtistList.get(i)).compareTo((ModelNodeWithAttributes)
                    actualArtistList.get(i)));
        }

        //Case 1d: Add one more artist in between to the existing library container
        ModelNode artistInBetween = m_dataStoreManager.createNode(getArtistModelNode("artist-in-between"),
                m_libraryNodeId, 2);
        expectedArtistList = new ArrayList<ModelNode>();
        expectedArtistList.add(newArtistInFirstIndex);
        expectedArtistList.add(newArtistInBetween);
        expectedArtistList.add(artistInBetween);
        expectedArtistList.addAll(listArtistNodes);
        expectedArtistList.add(newArtistInLastIndex);

        actualArtistList = m_dataStoreManager.listNodes(ARTIST_SCHEMA_PATH);
        assertEquals(5, actualArtistList.size());
        for (int i = 0; i < actualArtistList.size(); i++) {
            assertEquals(0, ((ModelNodeWithAttributes) expectedArtistList.get(i)).compareTo((ModelNodeWithAttributes)
                    actualArtistList.get(i)));
        }

        // Case 2 : Add one album to existing artist list
        ModelNodeId newArtistId = new ModelNodeId(m_libraryNodeId).addRdn(new ModelNodeRdn(CONTAINER, JB_NS,
                ARTIST_LOCAL_NAME))
                .addRdn(new ModelNodeRdn("name", JB_NS, "new-artist-in-first-index"));
        m_dataStoreManager.createNode(getAlbumModelNode("new-album", newArtistId), newArtistId, 0);

        assertEquals(2, m_dataStoreManager.listNodes(ALBUM_SCHEMA_PATH).size());

        // Case 3 : Add one song to existing album list
        ModelNodeId newAlbumId = new ModelNodeId(newArtistId).addRdn(new ModelNodeRdn(CONTAINER, JB_NS,
                ALBUM_LOCAL_NAME))
                .addRdn(new ModelNodeRdn("name", JB_NS, "new-album"));
        m_dataStoreManager.createNode(getSongModelNode("song-name", newAlbumId), newAlbumId, 0);

        assertEquals(2, m_dataStoreManager.listNodes(SONG_SCHEMA_PATH).size());

    }

    @Test
    public void testCreateModelNodeHomeAddressModel() throws DataStoreException, SchemaBuildException {
        List<YangTextSchemaSource> yangs = new ArrayList<>();
        yangs.add(TestUtil.getByteSource("/yangs/addresses@2015-12-08.yang"));
        m_schemaRegistry.loadSchemaContext("address-model", yangs, null, Collections.emptyMap());
        //Case 1: Add one home address
        assertEquals(0, m_dataStoreManager.listNodes(HOME_ADDRESSES_SCHEMA_PATH).size());

        ModelNodeWithAttributes homeAddressModelNode = new ModelNodeWithAttributes(HOME_ADDRESSES_SCHEMA_PATH, null,
                null, null, m_schemaRegistry, m_dataStoreManager);
        Map<QName, ConfigLeafAttribute> configAttributes = new HashMap<>();
        configAttributes.put(ADDRESS_NAME_Q_NAME, new GenericConfigAttribute("new-home"));
        configAttributes.put(ADDRESS_Q_NAME, new GenericConfigAttribute("new-home-address"));
        homeAddressModelNode.setAttributes(configAttributes);

        m_dataStoreManager.createNode(homeAddressModelNode, TestConstants.EMPTY_NODE_ID);

        assertEquals(1, m_dataStoreManager.listNodes(HOME_ADDRESSES_SCHEMA_PATH).size());

        // Case 2: Add one telephone number to existing home address
        assertEquals(0, m_dataStoreManager.listNodes(HOME_TELPHONE_SCHEMA_PATH).size());

        ModelNodeWithAttributes telephoneNumberModelNode = new ModelNodeWithAttributes(HOME_TELPHONE_SCHEMA_PATH,
                null, null, null, m_schemaRegistry, m_dataStoreManager);
        configAttributes = new HashMap<>();
        configAttributes.put(TELEPHONE_TYPE_QNAME, new GenericConfigAttribute(LAND_LINE));
        configAttributes.put(TELEPHONE_NUMBER_QNAME, new GenericConfigAttribute("someNumber"));
        telephoneNumberModelNode.setAttributes(configAttributes);
        ModelNodeId homeAddressNodeId = new ModelNodeId().addRdn(new ModelNodeRdn(CONTAINER, ADDR_NS, "home-address")
        ).addRdn(new
                ModelNodeRdn(ADDRESS_NAME_Q_NAME, "new-home"));

        m_dataStoreManager.createNode(telephoneNumberModelNode, homeAddressNodeId);
        assertEquals(1, m_dataStoreManager.listNodes(HOME_TELPHONE_SCHEMA_PATH).size());
    }

    // Test Remove method
    @Test
    public void testRemoveJukeboxModel() throws DataStoreException {
        assertEquals(1, m_dataStoreManager.listNodes(JUKEBOX_SCHEMA_PATH).size());
        assertEquals(1, m_dataStoreManager.listNodes(LIBRARY_SCHEMA_PATH).size());
        assertEquals(1, m_dataStoreManager.listNodes(ARTIST_SCHEMA_PATH).size());
        assertEquals(1, m_dataStoreManager.listNodes(ALBUM_SCHEMA_PATH).size());
        assertEquals(1, m_dataStoreManager.listNodes(SONG_SCHEMA_PATH).size());

        // Remove existing song
        m_dataStoreManager.removeNode(getSongModelNode("Entity Refactor", m_albumId), m_albumId);
        assertEquals(0, m_dataStoreManager.listNodes(SONG_SCHEMA_PATH).size());

        // Remove existing album
        m_dataStoreManager.removeNode(getAlbumModelNode("Refactor Times", m_artistId), m_artistId);
        assertEquals(0, m_dataStoreManager.listNodes(ALBUM_SCHEMA_PATH).size());

        //Remove existing artist
        m_dataStoreManager.removeNode(getArtistModelNode("keshava"), m_libraryNodeId);
        assertEquals(0, m_dataStoreManager.listNodes(ARTIST_SCHEMA_PATH).size());

        //Remove existing library
        ModelNodeWithAttributes libraryModelNode = new ModelNodeWithAttributes(LIBRARY_SCHEMA_PATH, null, null, null,
                m_schemaRegistry, m_dataStoreManager);
        m_dataStoreManager.removeNode(libraryModelNode, m_jukeboxNodeId);
        assertEquals(0, m_dataStoreManager.listNodes(LIBRARY_SCHEMA_PATH).size());

        m_dataStoreManager.createNode(libraryModelNode, m_jukeboxNodeId);
        assertEquals(1, m_dataStoreManager.listNodes(LIBRARY_SCHEMA_PATH).size());
    }

    @Test
    public void testRemoveHomeAddressModel() throws DataStoreException, SchemaBuildException {
        List<YangTextSchemaSource> yangs = new ArrayList<>();
        yangs.add(TestUtil.getByteSource("/yangs/addresses@2015-12-08.yang"));
        m_schemaRegistry.loadSchemaContext("address-model", yangs, null, Collections.emptyMap());

        addHomeAddresses();
        addOfficeAddresses();
        assertEquals(1, m_dataStoreManager.listNodes(HOME_ADDRESSES_SCHEMA_PATH).size());
        assertEquals(2, m_dataStoreManager.listNodes(HOME_TELPHONE_SCHEMA_PATH).size());
        //Remove one telephone number from home address
        ModelNodeWithAttributes telephoneNumberModelNode = new ModelNodeWithAttributes(HOME_TELPHONE_SCHEMA_PATH,
                null, null, null,
                m_schemaRegistry, m_dataStoreManager);
        Map<QName, ConfigLeafAttribute> configAttributes = new HashMap<>();
        configAttributes.put(TELEPHONE_TYPE_QNAME, new GenericConfigAttribute(LAND_LINE));
        configAttributes.put(TELEPHONE_NUMBER_QNAME, new GenericConfigAttribute(HOME_LAND_LINE));
        telephoneNumberModelNode.setAttributes(configAttributes);

        m_dataStoreManager.removeNode(telephoneNumberModelNode, TestConstants.HOME_ADDRESS_NODE_ID);
        assertEquals(1, m_dataStoreManager.listNodes(HOME_TELPHONE_SCHEMA_PATH).size());
    }

    @Test
    public void testRemoveAllJukeboxModel() throws DataStoreException {
        assertEquals(1, m_dataStoreManager.listNodes(JUKEBOX_SCHEMA_PATH).size());
        assertEquals(1, m_dataStoreManager.listNodes(LIBRARY_SCHEMA_PATH).size());
        assertEquals(1, m_dataStoreManager.listNodes(ARTIST_SCHEMA_PATH).size());
        assertEquals(1, m_dataStoreManager.listNodes(ALBUM_SCHEMA_PATH).size());
        assertEquals(1, m_dataStoreManager.listNodes(SONG_SCHEMA_PATH).size());

        // Add one song to existing album list
        m_dataStoreManager.createNode(getSongModelNode("song-name", m_albumId), m_albumId);
        assertEquals(2, m_dataStoreManager.listNodes(SONG_SCHEMA_PATH).size());

        //Remove all songs from album
        m_dataStoreManager.removeAllNodes(getAlbumModelNode("Refactor Times", m_artistId), SONG_SCHEMA_PATH,
                m_artistId);
        assertEquals(0, m_dataStoreManager.listNodes(SONG_SCHEMA_PATH).size());

        //Remove all albums from artist
        m_dataStoreManager.removeAllNodes(getArtistModelNode("keshava"), ALBUM_SCHEMA_PATH, m_libraryNodeId);
        assertEquals(0, m_dataStoreManager.listNodes(ALBUM_SCHEMA_PATH).size());

        //Remove all artists from library
        ModelNodeWithAttributes libraryModelNode = new ModelNodeWithAttributes(LIBRARY_SCHEMA_PATH, m_jukeboxNodeId,
                null, null, m_schemaRegistry, m_dataStoreManager);
        m_dataStoreManager.removeAllNodes(libraryModelNode, ARTIST_SCHEMA_PATH, m_jukeboxNodeId);
        assertEquals(0, m_dataStoreManager.listNodes(ARTIST_SCHEMA_PATH).size());
    }

    @Test
    public void testUpdateNodeJukeboxModel() throws DataStoreException {

        assertEquals(1, m_dataStoreManager.listNodes(SONG_SCHEMA_PATH).size());

        Map<QName, String> keys = new LinkedHashMap<>();
        keys.put(NAME_QNAME, "Entity Refactor");
        ModelNodeKey modelNodeKey = new ModelNodeKey(keys);
        QName locationQname = QName.create(JB_NS, JB_REVISION, "location");

        //Verify location is null
        ModelNodeWithAttributes modelNodeWithAttributes = (ModelNodeWithAttributes) m_dataStoreManager.findNode
                (SONG_SCHEMA_PATH, modelNodeKey, m_albumId);
        Map<QName, ConfigLeafAttribute> attributes = modelNodeWithAttributes.getAttributes();
        assertNull(attributes.get(locationQname));

        //Add location to song
        Map<QName, ConfigLeafAttribute> configAttributes = new HashMap<>();
        configAttributes.put(locationQname, new GenericConfigAttribute("somelocation"));
        m_dataStoreManager.updateNode(getSongModelNode("Entity Refactor", m_albumId), m_albumId, configAttributes,
                null, false);
        modelNodeWithAttributes = (ModelNodeWithAttributes) m_dataStoreManager.findNode(SONG_SCHEMA_PATH,
                modelNodeKey, m_albumId);
        attributes = modelNodeWithAttributes.getAttributes();
        assertEquals("somelocation", attributes.get(locationQname).getStringValue());

        //Set location back to null
        configAttributes.put(locationQname, null);
        m_dataStoreManager.updateNode(getSongModelNode("Entity Refactor", m_albumId), m_albumId, configAttributes,
                null, false);
        modelNodeWithAttributes = (ModelNodeWithAttributes) m_dataStoreManager.findNode(SONG_SCHEMA_PATH,
                modelNodeKey, m_albumId);
        attributes = modelNodeWithAttributes.getAttributes();
        assertNull(attributes.get(locationQname));

        // Verify there is no singer
        modelNodeWithAttributes = (ModelNodeWithAttributes) m_dataStoreManager.findNode(SONG_SCHEMA_PATH,
                modelNodeKey, m_albumId);
        assertNull(modelNodeWithAttributes.getLeafList(SINGER_QNAME));

        // Update with one singer
        m_dataStoreManager.createNode(getSongModelNode("My Song", m_albumId), m_albumId);

        keys = new LinkedHashMap<>();
        keys.put(NAME_QNAME, "My Song");
        modelNodeKey = new ModelNodeKey(keys);

        Map<QName, LinkedHashSet<ConfigLeafAttribute>> leafLists = new HashMap<>();
        LinkedHashSet<ConfigLeafAttribute> values = new LinkedHashSet<>();
        values.add(new GenericConfigAttribute("Singer1"));
        leafLists.put(SINGER_QNAME, values);
        m_dataStoreManager.updateNode(getSingerModelNode("Singer1", m_songId), m_songId, null, leafLists, false);
        modelNodeWithAttributes = (ModelNodeWithAttributes) m_dataStoreManager.findNode(SONG_SCHEMA_PATH,
                modelNodeKey, m_albumId);
        assertEquals(1, modelNodeWithAttributes.getLeafList(SINGER_QNAME).size());
    }

    @Test
    public void testUpdateNodeWithInsertIndex() throws DataStoreException {

        assertEquals(1, m_dataStoreManager.listNodes(SONG_SCHEMA_PATH).size());

        m_dataStoreManager.createNode(getSongModelNode("My Song", m_albumId), m_albumId, 0);

        Map<QName, String> keys = new LinkedHashMap<>();
        keys.put(NAME_QNAME, "My Song");
        ModelNodeKey modelNodeKey = new ModelNodeKey(keys);

        Map<QName, LinkedHashSet<ConfigLeafAttribute>> leafLists = new HashMap<>();
        List<ConfigLeafAttribute> valuesList = new ArrayList<ConfigLeafAttribute>();
        valuesList.add(0, new GenericConfigAttribute("Singer1"));
        LinkedHashSet<ConfigLeafAttribute> values = new LinkedHashSet<>(valuesList);
        leafLists.put(SINGER_QNAME, values);
        m_dataStoreManager.updateNode(getSingerModelNode("Singer1", m_songId), m_songId, null, leafLists, 0, false);
        ModelNodeWithAttributes modelNodeWithAttributes = (ModelNodeWithAttributes) m_dataStoreManager.findNode
                (SONG_SCHEMA_PATH, modelNodeKey, m_albumId);
        assertEquals(1, modelNodeWithAttributes.getLeafList(SINGER_QNAME).size());

    }


    // Helper methods
    private void addOfficeAddresses() {
        OfficeAddress officeAddress = new OfficeAddress();
        officeAddress.setSchemaPath(SchemaPathUtil.toString(OFFICE_ADDRESSES_SCHEMA_PATH));
        officeAddress.setAddressName(OFFICE_ADDRESS);
        officeAddress.setAddress(CHENNAI_OFFICE_ADDRESS);
        officeAddress.setParentId(TestConstants.EMPTY_NODE_ID.getModelNodeIdAsString());
        TelephoneNumber landline = new TelephoneNumber();
        landline.setSchemaPath(SchemaPathUtil.toString(OFFICE_TELPHONE_SCHEMA_PATH));
        landline.setNumber(OFFICE_LAND_LINE);
        landline.setParentId(TestConstants.OFFICE_ADDRESS_NODE_ID.getModelNodeIdAsString());
        landline.setType(LAND_LINE);
        officeAddress.addTelephoneNumbers(landline);

        //Seriously ? cell phone for office ? :D
        TelephoneNumber cellPhone = new TelephoneNumber();
        cellPhone.setSchemaPath(SchemaPathUtil.toString(OFFICE_TELPHONE_SCHEMA_PATH));
        cellPhone.setType(CELL_PHONE);
        cellPhone.setParentId(TestConstants.OFFICE_ADDRESS_NODE_ID.getModelNodeIdAsString());
        cellPhone.setNumber(OFFICE_CELL);
        officeAddress.addTelephoneNumbers(cellPhone);

        EntityDataStoreManager enityDataStoreManager = m_persistenceManagerUtil.getEntityDataStoreManager();
        enityDataStoreManager.beginTransaction();
        try {
            enityDataStoreManager.create(officeAddress);
            enityDataStoreManager.commitTransaction();
        } catch (Exception e) {
            enityDataStoreManager.rollbackTransaction();
            throw e;
        }
    }

    private void addHomeAddresses() {
        HomeAddress homeAddress = new HomeAddress();
        homeAddress.setSchemaPath(SchemaPathUtil.toString(HOME_ADDRESSES_SCHEMA_PATH));
        homeAddress.setAddressName(HOME_ADDRESS);
        homeAddress.setAddress(MY_HOME_ADDRESS_KARNATAKA_560092);
        homeAddress.setParentId(TestConstants.EMPTY_NODE_ID.getModelNodeIdAsString());
        TelephoneNumber landline = new TelephoneNumber();
        landline.setSchemaPath(SchemaPathUtil.toString(HOME_TELPHONE_SCHEMA_PATH));
        landline.setNumber(HOME_LAND_LINE);
        landline.setParentId(TestConstants.HOME_ADDRESS_NODE_ID.getModelNodeIdAsString());
        landline.setType(LAND_LINE);
        homeAddress.addTelephoneNumbers(landline);

        //Seriously ? cell phone for office ? :D
        TelephoneNumber cellPhone = new TelephoneNumber();
        cellPhone.setSchemaPath(SchemaPathUtil.toString(HOME_TELPHONE_SCHEMA_PATH));
        cellPhone.setType(CELL_PHONE);
        cellPhone.setParentId(TestConstants.HOME_ADDRESS_NODE_ID.getModelNodeIdAsString());
        cellPhone.setNumber(HOME_CELL);
        homeAddress.addTelephoneNumbers(cellPhone);

        EntityDataStoreManager enityDataStoreManager = m_persistenceManagerUtil.getEntityDataStoreManager();
        enityDataStoreManager.beginTransaction();
        try {
            enityDataStoreManager.create(homeAddress);
            enityDataStoreManager.commitTransaction();
        } catch (Exception e) {
            enityDataStoreManager.rollbackTransaction();
            throw e;
        }
    }

    private void addCaCerts() {
        Certificate cert1;
        Certificate cert2;
        TrustedCaCerts trustedCaCerts = new TrustedCaCerts();
        trustedCaCerts.setSchemaPath(SchemaPathUtil.toString(CA_CERT_CONTAINER_SCHEMA_PATH));
        trustedCaCerts.setParentId(TestConstants.CERT_MGMT_NODE_ID.getModelNodeIdAsString());
        cert1 = new Certificate();
        cert1.setSchemaPath(SchemaPathUtil.toString(CA_CERT_SCHEMA_PATH));
        cert1.setId("ca-cert1");
        cert1.setCerBinary("ca-cert1-binary");
        cert1.setParentId(TestConstants.CA_CERT_NODE_ID.getModelNodeIdAsString());
        trustedCaCerts.addCertificates(cert1);

        cert2 = new Certificate();
        cert2.setSchemaPath(SchemaPathUtil.toString(CA_CERT_SCHEMA_PATH));
        cert2.setId("ca-cert2");
        cert2.setCerBinary("ca-cert2-binary");
        cert2.setParentId(TestConstants.CA_CERT_NODE_ID.getModelNodeIdAsString());
        trustedCaCerts.addCertificates(cert2);

        EntityDataStoreManager enityDataStoreManager = m_persistenceManagerUtil.getEntityDataStoreManager();
        enityDataStoreManager.beginTransaction();
        try {
            enityDataStoreManager.create(trustedCaCerts);
            enityDataStoreManager.commitTransaction();
        } catch (Exception e) {
            enityDataStoreManager.rollbackTransaction();
        }

    }

    private void addPmaCerts() {
        PmaCerts pmaCerts = new PmaCerts();
        pmaCerts.setSchemaPath(SchemaPathUtil.toString(PMA_CERT_CONTAINER_SCHEMA_PATH));
        pmaCerts.setParentId(TestConstants.CERT_MGMT_NODE_ID.getModelNodeIdAsString());
        Certificate cert1 = new Certificate();
        cert1.setSchemaPath(SchemaPathUtil.toString(PMA_CERT_SCHEMA_PATH));
        cert1.setId("pma-cert1");
        cert1.setCerBinary("pma-cert1-binary");
        cert1.setParentId(TestConstants.PMA_CERT_NODE_ID.getModelNodeIdAsString());
        pmaCerts.addCertificates(cert1);

        Certificate cert2 = new Certificate();
        cert2.setSchemaPath(SchemaPathUtil.toString(PMA_CERT_SCHEMA_PATH));
        cert2.setId("pma-cert2");
        cert2.setCerBinary("pma-cert2-binary");
        cert2.setParentId(TestConstants.PMA_CERT_NODE_ID.getModelNodeIdAsString());
        pmaCerts.addCertificates(cert2);

        EntityDataStoreManager enityDataStoreManager = m_persistenceManagerUtil.getEntityDataStoreManager();
        enityDataStoreManager.beginTransaction();
        try {
            enityDataStoreManager.create(pmaCerts);
            enityDataStoreManager.commitTransaction();
        } catch (Exception e) {
            enityDataStoreManager.rollbackTransaction();
            throw e;
        }
    }

    private ModelNodeWithAttributes getSingerModelNode(String singerName, ModelNodeId parentId) {
        Map<QName, ConfigLeafAttribute> configAttributes;
        ModelNodeWithAttributes singerModelNode = new ModelNodeWithAttributes(SINGER_SCHEMA_PATH, parentId, null,
                null, m_schemaRegistry, m_dataStoreManager);
        configAttributes = new HashMap<>();
        configAttributes.put(SINGER_QNAME, new GenericConfigAttribute(singerName));
        singerModelNode.setAttributes(configAttributes);
        return singerModelNode;
    }

    private ModelNodeWithAttributes getSongModelNode(String songName, ModelNodeId parentId) {
        Map<QName, ConfigLeafAttribute> configAttributes;
        ModelNodeWithAttributes songModelNode = new ModelNodeWithAttributes(SONG_SCHEMA_PATH, parentId, null, null,
                m_schemaRegistry, m_dataStoreManager);
        configAttributes = new HashMap<>();
        configAttributes.put(NAME_QNAME, new GenericConfigAttribute(songName));
        songModelNode.setAttributes(configAttributes);
        return songModelNode;
    }

    private ModelNodeWithAttributes getAlbumModelNode(String albumName, ModelNodeId parentId) {
        Map<QName, ConfigLeafAttribute> configAttributes;
        ModelNodeWithAttributes albumModelNode = new ModelNodeWithAttributes(ALBUM_SCHEMA_PATH, parentId, null, null,
                m_schemaRegistry, m_dataStoreManager);
        configAttributes = new HashMap<>();
        configAttributes.put(NAME_QNAME, new GenericConfigAttribute(albumName));
        albumModelNode.setAttributes(configAttributes);
        return albumModelNode;
    }

    private ModelNodeWithAttributes getArtistModelNode(String artistName) {
        ModelNodeWithAttributes artistModelNode = new ModelNodeWithAttributes(ARTIST_SCHEMA_PATH, m_libraryNodeId,
                m_modelNodeHelperRegistry, null, m_schemaRegistry, m_dataStoreManager);
        Map<QName, ConfigLeafAttribute> configAttributes = new HashMap<>();
        configAttributes.put(NAME_QNAME, new GenericConfigAttribute(artistName));
        artistModelNode.setAttributes(configAttributes);
        return artistModelNode;
    }
}
