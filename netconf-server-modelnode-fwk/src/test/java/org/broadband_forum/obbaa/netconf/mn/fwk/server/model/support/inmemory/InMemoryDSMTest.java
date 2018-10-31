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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.inmemory;

import org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.DataStoreException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeKey;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.GenericConfigAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ALBUM_LOCAL_NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ARTIST_LOCAL_NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ARTIST_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JB_NS;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JB_REVISION;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JUKEBOX_LOCAL_NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JUKEBOX_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.LIBRARY_LOCAL_NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.YEAR;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.SINGER_LOCAL_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by pgorai on 2/25/16.
 */
public class InMemoryDSMTest {
    ModelNodeDataStoreManager m_inMemoryDSM;
    private SchemaPath m_jukeboxSchemaPath = JukeboxConstants.JUKEBOX_SCHEMA_PATH;
    private SchemaRegistry m_schemaRegistry;
    private SchemaPath m_librarySchemaPath = JukeboxConstants.LIBRARY_SCHEMA_PATH;
    public ModelNodeId m_jukeboxNodeId = new ModelNodeId().addRdn(new ModelNodeRdn(ModelNodeRdn.CONTAINER, JB_NS,
            JUKEBOX_LOCAL_NAME));
    public ModelNodeId m_libraryNodeId = new ModelNodeId(m_jukeboxNodeId).addRdn(new ModelNodeRdn(ModelNodeRdn
            .CONTAINER, JB_NS, LIBRARY_LOCAL_NAME));
    public final ModelNodeId m_artist1Id = new ModelNodeId(m_libraryNodeId).addRdn(new ModelNodeRdn(ModelNodeRdn
            .CONTAINER, JB_NS, ARTIST_LOCAL_NAME))
            .addRdn(new ModelNodeRdn("name", JB_NS, "keshava"));
    public final ModelNodeId m_artist2Id = new ModelNodeId(m_libraryNodeId).addRdn(new ModelNodeRdn(ModelNodeRdn
            .CONTAINER, JB_NS, ARTIST_LOCAL_NAME))
            .addRdn(new ModelNodeRdn("name", JB_NS, "Paramita"));
    public final ModelNodeId m_album1NodeId = new ModelNodeId(m_artist1Id).addRdn(new ModelNodeRdn(ModelNodeRdn
            .CONTAINER, JB_NS, ALBUM_LOCAL_NAME))
            .addRdn(new ModelNodeRdn("name", JB_NS, "1st Album"));
    private SchemaPath m_artistSchemaPath = JukeboxConstants.ARTIST_SCHEMA_PATH;
    private SchemaPath m_albumSchemaPath = JukeboxConstants.ALBUM_SCHEMA_PATH;

    @Before
    public void setUp() throws Exception {
        m_schemaRegistry = new SchemaRegistryImpl(TestUtil.getJukeBoxYangs(), new NoLockService());
        m_inMemoryDSM = new InMemoryDSM(m_schemaRegistry);
    }

    @Test
    public void testCreateNodes() throws DataStoreException {

        createJukeBoxWithArtist();
        Map<QName, String> keys = new LinkedHashMap<>();

        keys.put(JukeboxConstants.NAME_QNAME, "Paramita");
        ModelNodeWithAttributes artistNode = new ModelNodeWithAttributes(m_artistSchemaPath, m_libraryNodeId, null,
                null, m_schemaRegistry, m_inMemoryDSM);
        artistNode.setAttributes(Collections.singletonMap(JukeboxConstants.NAME_QNAME, new GenericConfigAttribute(NAME, JB_NS, "Paramita")));
        ModelNode createdNode = m_inMemoryDSM.createNode(artistNode, m_libraryNodeId);
        assertEquals(createdNode, artistNode);

        ModelNode node = m_inMemoryDSM.findNode(m_artistSchemaPath, new ModelNodeKey(keys), m_libraryNodeId);
        assertEquals(node, artistNode);
        assertEquals(2, m_inMemoryDSM.listNodes(m_artistSchemaPath).size());
    }

    @Test
    public void testFindNodes() throws DataStoreException {

        Map<QName, String> keys = new LinkedHashMap<>();
        createJukeBoxWithArtist();

        ModelNode node = m_inMemoryDSM.findNode(m_jukeboxSchemaPath, new ModelNodeKey(keys), null);
        assertEquals(node.getQName(), m_jukeboxSchemaPath.getLastComponent());

        node = m_inMemoryDSM.findNode(m_librarySchemaPath, new ModelNodeKey(keys), m_jukeboxNodeId);
        assertEquals(node.getQName(), m_librarySchemaPath.getLastComponent());

        keys.put(JukeboxConstants.NAME_QNAME, "keshava");
        node = m_inMemoryDSM.findNode(m_artistSchemaPath, new ModelNodeKey(keys), m_libraryNodeId);
        assertEquals(node.getQName(), m_artistSchemaPath.getLastComponent());
    }

    @Test
    public void testListNodes() throws DataStoreException {

        createJukeBoxWithArtist();
        assertEquals(1, m_inMemoryDSM.listNodes(m_artistSchemaPath).size());
        assertEquals(1, m_inMemoryDSM.listNodes(m_librarySchemaPath).size());
        assertEquals(1, m_inMemoryDSM.listNodes(m_jukeboxSchemaPath).size());

        Map<QName, String> keys = new LinkedHashMap<>();
        keys.put(JukeboxConstants.NAME_QNAME, "Paramita");
        ModelNodeWithAttributes artistNode = new ModelNodeWithAttributes(m_artistSchemaPath, m_libraryNodeId, null,
                null, m_schemaRegistry, m_inMemoryDSM);
        artistNode.setAttributes(Collections.singletonMap(JukeboxConstants.NAME_QNAME, new GenericConfigAttribute(NAME, JB_NS, "Paramita")));
        ModelNode createdNode = m_inMemoryDSM.createNode(artistNode, m_libraryNodeId);
        assertEquals(createdNode, artistNode);

        keys.put(JukeboxConstants.NAME_QNAME, "keshava");
        ModelNode node = m_inMemoryDSM.findNode(m_artistSchemaPath, new ModelNodeKey(keys), m_libraryNodeId);
        assertEquals(node.getQName(), m_artistSchemaPath.getLastComponent());

        assertEquals(2, m_inMemoryDSM.listNodes(m_artistSchemaPath).size());
    }

    @Test
    public void testListChildNodes() throws DataStoreException {

        createJukeBoxWithArtist();
        Map<QName, String> keys = new LinkedHashMap<>();
        keys.put(JukeboxConstants.NAME_QNAME, "keshava");
        ModelNode node = m_inMemoryDSM.findNode(m_artistSchemaPath, new ModelNodeKey(keys), m_libraryNodeId);
        assertEquals(node.getQName(), m_artistSchemaPath.getLastComponent());
        List<ModelNode> actual = m_inMemoryDSM.listChildNodes(m_artistSchemaPath, m_libraryNodeId);
        assertEquals(1, actual.size());
        ModelNode firstArtist = actual.get(0);
        assertEquals(m_artistSchemaPath, firstArtist.getModelNodeSchemaPath());
        assertEquals("keshava", ((ModelNodeWithAttributes) firstArtist).getAttribute(JukeboxConstants.NAME_QNAME)
                .getStringValue());

        addAnotherArtist();
        actual = m_inMemoryDSM.listChildNodes(m_artistSchemaPath, m_libraryNodeId);
        assertEquals(2, actual.size());
        ModelNode secondArtist = actual.get(1);
        assertEquals(m_artistSchemaPath, secondArtist.getModelNodeSchemaPath());
        assertEquals("Paramita", ((ModelNodeWithAttributes) secondArtist).getAttribute(JukeboxConstants.NAME_QNAME)
                .getStringValue());

    }

    private void addAnotherArtist() throws DataStoreException {
        SortedMap<QName, ConfigLeafAttribute> keys = new TreeMap<>();
        keys.put(JukeboxConstants.NAME_QNAME,new GenericConfigAttribute(NAME, JB_NS, "Paramita"));
        ModelNodeWithAttributes artistNode = new ModelNodeWithAttributes(m_artistSchemaPath, m_libraryNodeId, null,
                null, m_schemaRegistry, m_inMemoryDSM);
        artistNode.setModelNodeId(m_artist2Id);
        artistNode.setAttributes(keys);
        m_inMemoryDSM.createNode(artistNode, m_libraryNodeId);
    }


    public void createJukeBoxWithArtist() throws DataStoreException {
        SortedMap<QName, ConfigLeafAttribute> keys = new TreeMap<>();

        ModelNodeWithAttributes jukeboxNode = new ModelNodeWithAttributes(m_jukeboxSchemaPath, null, null, null,
                m_schemaRegistry, m_inMemoryDSM);
        jukeboxNode.setModelNodeId(m_jukeboxNodeId);
        m_inMemoryDSM.createNode(jukeboxNode, null);

        ModelNodeWithAttributes libraryNode = new ModelNodeWithAttributes(m_librarySchemaPath, m_jukeboxNodeId, null,
                null, m_schemaRegistry, m_inMemoryDSM);
        libraryNode.setModelNodeId(m_libraryNodeId);
        m_inMemoryDSM.createNode(libraryNode, m_jukeboxNodeId);

        keys.put(JukeboxConstants.NAME_QNAME, new GenericConfigAttribute(NAME, JB_NS, "keshava"));
        ModelNodeWithAttributes artistNode = new ModelNodeWithAttributes(m_artistSchemaPath, m_libraryNodeId, null,
                null, m_schemaRegistry, m_inMemoryDSM);
        artistNode.setModelNodeId(m_artist1Id);
        artistNode.setAttributes(keys);
        m_inMemoryDSM.createNode(artistNode, m_libraryNodeId);

    }

    private void addAlbum() throws DataStoreException {
        ModelNodeWithAttributes albumNode = new ModelNodeWithAttributes(m_albumSchemaPath, m_artist1Id, null, null,
                m_schemaRegistry, m_inMemoryDSM);
        Map<QName, ConfigLeafAttribute> albumKey = new HashMap<>();
        albumKey.put(JukeboxConstants.NAME_QNAME, new GenericConfigAttribute(NAME, JB_NS, "1st Album"));
        albumNode.setAttributes(albumKey);
        albumNode.setModelNodeId(m_album1NodeId);
        m_inMemoryDSM.createNode(albumNode, m_artist1Id);
    }

    @Test
    public void testRemoveNode() throws DataStoreException {
        createJukeBoxWithArtist();

        Map<QName, String> keys = new LinkedHashMap<>();
        keys.put(JukeboxConstants.NAME_QNAME, "keshava");
        ModelNode node = m_inMemoryDSM.findNode(m_artistSchemaPath, new ModelNodeKey(keys), m_libraryNodeId);
        assertEquals(node.getQName(), m_artistSchemaPath.getLastComponent());

        keys.put(JukeboxConstants.NAME_QNAME, "Paramita");
        ModelNodeWithAttributes artistNode = new ModelNodeWithAttributes(m_artistSchemaPath, m_libraryNodeId, null,
                null, m_schemaRegistry, m_inMemoryDSM);
        Map<QName, ConfigLeafAttribute> attributes = new HashMap<>();
        attributes.put(JukeboxConstants.NAME_QNAME, new GenericConfigAttribute(NAME, JB_NS, "keshava"));
        attributes.put(JukeboxConstants.NAME_QNAME, new GenericConfigAttribute(NAME, JB_NS, "Paramita"));
        artistNode.setAttributes(attributes);
        artistNode.setModelNodeId(m_artist2Id);
        m_inMemoryDSM.createNode(artistNode, m_libraryNodeId);

        keys.put(JukeboxConstants.NAME_QNAME, "keshava");
        node = m_inMemoryDSM.findNode(m_artistSchemaPath, new ModelNodeKey(keys), m_libraryNodeId);
        assertEquals(node.getQName(), m_artistSchemaPath.getLastComponent());
        m_inMemoryDSM.removeNode(node, m_libraryNodeId);
        assertEquals(null, m_inMemoryDSM.findNode(m_artistSchemaPath, new ModelNodeKey(keys), m_libraryNodeId));

        keys.put(JukeboxConstants.NAME_QNAME, "Paramita");
        node = m_inMemoryDSM.findNode(m_artistSchemaPath, new ModelNodeKey(keys), m_libraryNodeId);
        assertEquals(node.getQName(), m_artistSchemaPath.getLastComponent());

        ModelNodeWithAttributes libraryNode = new ModelNodeWithAttributes(m_librarySchemaPath, m_jukeboxNodeId, null,
                null, m_schemaRegistry, m_inMemoryDSM);
        libraryNode.setModelNodeId(m_libraryNodeId);
        m_inMemoryDSM.removeNode(libraryNode, m_jukeboxNodeId);
        assertNull(m_inMemoryDSM.findNode(m_artistSchemaPath, new ModelNodeKey(keys), m_libraryNodeId));
        assertNull(m_inMemoryDSM.findNode(m_librarySchemaPath, ModelNodeKey.EMPTY_KEY, m_jukeboxNodeId));
    }

    @Test
    public void testRemoveAllChildren() throws DataStoreException {
        createJukeBoxWithArtist();
        addAlbum();
        addAnotherArtist();

        Map<QName, String> artist1Key = new LinkedHashMap<>();
        artist1Key.put(JukeboxConstants.NAME_QNAME, "keshava");
        ModelNode node = m_inMemoryDSM.findNode(m_artistSchemaPath, new ModelNodeKey(artist1Key), m_libraryNodeId);
        assertEquals(node.getQName(), m_artistSchemaPath.getLastComponent());

        Map<QName, String> albumKey = new LinkedHashMap<>();
        albumKey.put(JukeboxConstants.NAME_QNAME, "1st Album");
        ModelNode albumNode = m_inMemoryDSM.findNode(m_albumSchemaPath, new ModelNodeKey(albumKey), m_artist1Id);
        assertEquals(albumNode.getQName(), m_albumSchemaPath.getLastComponent());

        ModelNode libraryNode = m_inMemoryDSM.findNode(m_librarySchemaPath, ModelNodeKey.EMPTY_KEY, m_jukeboxNodeId);
        assertEquals(libraryNode.getQName(), m_librarySchemaPath.getLastComponent());

        m_inMemoryDSM.removeAllNodes(libraryNode, ARTIST_SCHEMA_PATH, m_jukeboxNodeId);
        assertEquals(libraryNode, m_inMemoryDSM.findNode(m_librarySchemaPath, ModelNodeKey.EMPTY_KEY, m_jukeboxNodeId));
        assertNull(m_inMemoryDSM.findNode(m_artistSchemaPath, new ModelNodeKey(artist1Key), m_libraryNodeId));
        assertNull(m_inMemoryDSM.findNode(m_albumSchemaPath, new ModelNodeKey(albumKey), m_artist1Id));
    }

    @Test
    public void testRemoveAllChildrenNoChildrenCase() throws DataStoreException {
        createJukeBoxWithArtist();
        addAlbum();
        addAnotherArtist();

        ModelNode libraryNode = m_inMemoryDSM.findNode(m_librarySchemaPath, ModelNodeKey.EMPTY_KEY, m_jukeboxNodeId);
        assertEquals(libraryNode.getQName(), m_librarySchemaPath.getLastComponent());
        //Expect no exception
        m_inMemoryDSM.removeAllNodes(libraryNode, JUKEBOX_SCHEMA_PATH, m_jukeboxNodeId);

    }

    @Test
    public void testUpdateNode() throws DataStoreException {

        createJukeBoxWithArtist();
        addAlbum();
        Map<QName, String> albumKey = new LinkedHashMap<>();
        albumKey.put(JukeboxConstants.NAME_QNAME, "1st Album");
        ModelNodeWithAttributes albumNode = (ModelNodeWithAttributes) m_inMemoryDSM.findNode(m_albumSchemaPath, new
                ModelNodeKey(albumKey), m_artist1Id);
        Map<QName, ConfigLeafAttribute> retrievedAttributes;
        QName year = QName.create(JB_NS,JB_REVISION, YEAR);
        retrievedAttributes = albumNode.getAttributes();
        assertNull(retrievedAttributes.get(year));
        assertTrue(albumNode.getLeafLists().isEmpty());

        HashMap<QName, ConfigLeafAttribute> configAttribute = new HashMap<>();
        configAttribute.put(year, new GenericConfigAttribute(YEAR, JB_NS, "2016"));
        Map<QName, LinkedHashSet<ConfigLeafAttribute>> leafLists = new HashMap<>();
        LinkedHashSet<ConfigLeafAttribute> values = new LinkedHashSet<>();
        values.add(new GenericConfigAttribute(SINGER_LOCAL_NAME, JB_NS, "Singer1"));
        leafLists.put(JukeboxConstants.SINGER_QNAME, values);
        m_inMemoryDSM.updateNode(albumNode, m_artist1Id, configAttribute, leafLists, false);
        albumNode = (ModelNodeWithAttributes) m_inMemoryDSM.findNode(m_albumSchemaPath, new ModelNodeKey(albumKey),
                m_artist1Id);
        retrievedAttributes = albumNode.getAttributes();
        assertEquals("2016", retrievedAttributes.get(year).getStringValue());
        assertEquals(1, albumNode.getLeafList(JukeboxConstants.SINGER_QNAME).size());
    }

    @Test
    public void testModelNodeWithAttributesEqual() throws DataStoreException {
        ModelNodeWithAttributes jukeboxNode = new ModelNodeWithAttributes(m_jukeboxSchemaPath, null, null, null,
                m_schemaRegistry, m_inMemoryDSM);
        jukeboxNode.setModelNodeId(m_jukeboxNodeId);
        SortedMap<QName, ConfigLeafAttribute> keys = new TreeMap<>();
        keys.put(JukeboxConstants.NAME_QNAME, new GenericConfigAttribute(NAME, JB_NS, "testAttribute"));
        jukeboxNode.setAttributes(keys);

        ModelNodeWithAttributes anotherJukebox = jukeboxNode;
        assertEquals(0, jukeboxNode.compareTo(anotherJukebox));

        anotherJukebox = new ModelNodeWithAttributes(m_jukeboxSchemaPath, null, null, null, m_schemaRegistry,
                m_inMemoryDSM);
        anotherJukebox.setModelNodeId(m_jukeboxNodeId);
        keys = new TreeMap<>();
        keys.put(JukeboxConstants.NAME_QNAME, new GenericConfigAttribute(NAME, JB_NS, "testAttribute1"));
        anotherJukebox.setAttributes(keys);
        assertEquals(0, jukeboxNode.compareTo(anotherJukebox));

        ModelNodeWithAttributes libraryNode = new ModelNodeWithAttributes(m_librarySchemaPath, m_jukeboxNodeId, null,
                null, m_schemaRegistry, m_inMemoryDSM);
        libraryNode.setModelNodeId(m_libraryNodeId);
        keys = new TreeMap<>();
        keys.put(JukeboxConstants.NAME_QNAME, new GenericConfigAttribute(NAME, JB_NS, "testAttribute"));
        libraryNode.setAttributes(keys);
        assertTrue(jukeboxNode.compareTo(libraryNode) < 0);
    }
}
