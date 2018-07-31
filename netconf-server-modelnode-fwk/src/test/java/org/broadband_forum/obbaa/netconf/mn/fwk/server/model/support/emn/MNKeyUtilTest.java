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

import org.broadband_forum.obbaa.netconf.api.util.SchemaPathUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeKey;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.GenericConfigAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by keshava on 4/28/16.
 */
public class MNKeyUtilTest {

    SchemaRegistry m_schemaRegistry;
    public static final String NAMESPACE = "http://example.com/ns/example-jukebox-with-singer";
    public static final String REVISION = "2014-07-03";
    public static final String DELIMITER = SchemaPathUtil.DELIMITER;
    public static final SchemaPath ALBUM_SCHEMA_PATH = SchemaPathUtil.fromString(NAMESPACE + DELIMITER + REVISION +
            DELIMITER + "jukebox" +
            DELIMITER + NAMESPACE + DELIMITER + REVISION + DELIMITER + "library" +
            DELIMITER + NAMESPACE + DELIMITER + REVISION + DELIMITER + "artist" +
            DELIMITER + NAMESPACE + DELIMITER + REVISION + DELIMITER + "album");
    public static final SchemaPath JUKEBOX_SCHEMA_PATH = SchemaPathUtil.fromString(NAMESPACE + DELIMITER + REVISION +
            DELIMITER + "jukebox");
    private static final SchemaPath SONG_SCHEMA_PATH = SchemaPathUtil.fromString(NAMESPACE + DELIMITER + REVISION +
            DELIMITER + "jukebox" +
            DELIMITER + NAMESPACE + DELIMITER + REVISION + DELIMITER + "library" +
            DELIMITER + NAMESPACE + DELIMITER + REVISION + DELIMITER + "artist" +
            DELIMITER + NAMESPACE + DELIMITER + REVISION + DELIMITER + "album" +
            DELIMITER + NAMESPACE + DELIMITER + REVISION + DELIMITER + "song");
    public static final QName NAME_QNAME = QName.create(NAMESPACE, REVISION, "name");
    public static final QName YEAR_QNAME = QName.create(NAMESPACE, REVISION, "year");
    public static final QName SINGER_QNAME = QName.create(NAMESPACE, REVISION, "singer");

    @Before
    public void setUp() throws Exception {
        List<YangTextSchemaSource> yangFiles = TestUtil.getJukeBoxDeps();
        yangFiles.add(TestUtil.getByteSource("/yangs/example-jukebox-with-leaf-list.yang"));
        m_schemaRegistry = new SchemaRegistryImpl(yangFiles, new NoLockService());
    }

    @Test
    public void testContainsAllKeys() {
        Map<QName, ConfigLeafAttribute> matchCriteria = new HashMap<>();
        matchCriteria.put(NAME_QNAME, new GenericConfigAttribute("album1"));
        assertTrue(MNKeyUtil.containsAllKeys(ALBUM_SCHEMA_PATH, matchCriteria, m_schemaRegistry));
        matchCriteria.put(YEAR_QNAME, new GenericConfigAttribute("1988"));
        assertTrue(MNKeyUtil.containsAllKeys(ALBUM_SCHEMA_PATH, matchCriteria, m_schemaRegistry));

        //no matter what the criteria is, it should be true for containers
        assertTrue(MNKeyUtil.containsAllKeys(JUKEBOX_SCHEMA_PATH, matchCriteria, m_schemaRegistry));

        matchCriteria.clear();
        matchCriteria.put(YEAR_QNAME, new GenericConfigAttribute("1988"));
        assertFalse(MNKeyUtil.containsAllKeys(ALBUM_SCHEMA_PATH, matchCriteria, m_schemaRegistry));

        assertFalse(MNKeyUtil.containsAllKeys(SchemaPath.create(true, NAME_QNAME), matchCriteria, m_schemaRegistry));
    }

    @Test
    public void testGetKeyFromCriteria() {
        Map<QName, ConfigLeafAttribute> matchCriteria = new HashMap<>();
        matchCriteria.put(NAME_QNAME, new GenericConfigAttribute("album1"));
        matchCriteria.put(YEAR_QNAME, new GenericConfigAttribute("1988"));

        ModelNodeKey key = MNKeyUtil.getKeyFromCriteria(ALBUM_SCHEMA_PATH, matchCriteria, m_schemaRegistry);
        assertEquals("album1", key.getKeys().get(NAME_QNAME));
        assertEquals(1, key.getKeys().size());

        key = MNKeyUtil.getKeyFromCriteria(JUKEBOX_SCHEMA_PATH, matchCriteria, m_schemaRegistry);
        assertTrue(key.getKeys().isEmpty());

        matchCriteria.clear();
        key = MNKeyUtil.getKeyFromCriteria(ALBUM_SCHEMA_PATH, matchCriteria, m_schemaRegistry);
        assertEquals(0, key.getKeys().size());
    }

    @Test
    public void testIsMatchLeafAttributes() {
        //both matchCriteria and attributes not null
        Map<QName, ConfigLeafAttribute> matchCriteria = new HashMap<>();
        matchCriteria.put(NAME_QNAME, new GenericConfigAttribute("album1"));
        matchCriteria.put(YEAR_QNAME, new GenericConfigAttribute("1988"));
        ModelNodeWithAttributes albumNode = new ModelNodeWithAttributes(ALBUM_SCHEMA_PATH, null, null, null,
                m_schemaRegistry, null);
        Map<QName, ConfigLeafAttribute> attributeValues = new HashMap<>();
        attributeValues.put(NAME_QNAME, new GenericConfigAttribute("album1"));
        attributeValues.put(YEAR_QNAME, new GenericConfigAttribute("1988"));
        albumNode.setAttributes(attributeValues);
        assertTrue(MNKeyUtil.isMatch(matchCriteria, albumNode, m_schemaRegistry));

        //matchCriteria has null and attributes not null
        matchCriteria.clear();
        matchCriteria.put(NAME_QNAME, new GenericConfigAttribute("album1"));
        matchCriteria.put(YEAR_QNAME, null);
        assertFalse(MNKeyUtil.isMatch(matchCriteria, albumNode, m_schemaRegistry));

        //matchCriteria has null and attributes also has null
        matchCriteria.clear();
        matchCriteria.put(NAME_QNAME, new GenericConfigAttribute("album1"));
        matchCriteria.put(YEAR_QNAME, null);
        attributeValues.clear();
        attributeValues.put(NAME_QNAME, new GenericConfigAttribute("album1"));
        attributeValues.put(YEAR_QNAME, null);
        albumNode = new ModelNodeWithAttributes(ALBUM_SCHEMA_PATH, null, null, null, m_schemaRegistry, null);
        albumNode.setAttributes(attributeValues);
        assertTrue(MNKeyUtil.isMatch(matchCriteria, albumNode, m_schemaRegistry));

        //matchCriteria not null and attributes has null
        matchCriteria.clear();
        matchCriteria.put(NAME_QNAME, new GenericConfigAttribute("album1"));
        matchCriteria.put(YEAR_QNAME, new GenericConfigAttribute("1988"));
        attributeValues.clear();
        attributeValues.put(NAME_QNAME, new GenericConfigAttribute("album1"));
        attributeValues.put(YEAR_QNAME, null);
        albumNode = new ModelNodeWithAttributes(ALBUM_SCHEMA_PATH, null, null, null, m_schemaRegistry, null);
        albumNode.setAttributes(attributeValues);
        assertFalse(MNKeyUtil.isMatch(matchCriteria, albumNode, m_schemaRegistry));
    }

    @Test
    public void testIsMatchLeafListAttributes() {
        Map<QName, ConfigLeafAttribute> matchCriteria = new HashMap<>();
        matchCriteria.put(NAME_QNAME, new GenericConfigAttribute("song1"));
        matchCriteria.put(SINGER_QNAME, new GenericConfigAttribute("singer1"));
        ModelNodeWithAttributes songNode = new ModelNodeWithAttributes(SONG_SCHEMA_PATH, null, null, null,
                m_schemaRegistry, null);
        Map<QName, ConfigLeafAttribute> attributeValues = new HashMap<>();
        attributeValues.put(NAME_QNAME, new GenericConfigAttribute("song1"));
        songNode.setAttributes(attributeValues);
        Map<QName, LinkedHashSet<ConfigLeafAttribute>> leafLists = new HashMap<>();
        LinkedHashSet<ConfigLeafAttribute> singers = new LinkedHashSet<>();
        singers.add(new GenericConfigAttribute("singer1"));
        singers.add(new GenericConfigAttribute("singer2"));
        leafLists.put(SINGER_QNAME, singers);
        songNode.setLeafLists(leafLists);
        assertTrue(MNKeyUtil.isMatch(matchCriteria, songNode, m_schemaRegistry));

        matchCriteria.put(SINGER_QNAME, new GenericConfigAttribute("nonExistingSinger"));
        assertFalse(MNKeyUtil.isMatch(matchCriteria, songNode, m_schemaRegistry));

        songNode = new ModelNodeWithAttributes(SONG_SCHEMA_PATH, null, null, null, m_schemaRegistry, null);
        singers = new LinkedHashSet<>();
        singers.add(new GenericConfigAttribute("singer1"));
        singers.add(new GenericConfigAttribute("singer2"));
        leafLists.put(SINGER_QNAME, singers);
        songNode.setLeafLists(leafLists);
        matchCriteria.clear();
        matchCriteria.put(SINGER_QNAME, null);
        assertFalse(MNKeyUtil.isMatch(matchCriteria, songNode, m_schemaRegistry));

        songNode = new ModelNodeWithAttributes(SONG_SCHEMA_PATH, null, null, null, m_schemaRegistry, null);
        matchCriteria.clear();
        matchCriteria.put(SINGER_QNAME, null);
        assertTrue(MNKeyUtil.isMatch(matchCriteria, songNode, m_schemaRegistry));

    }

    @Test
    public void testGetModelNodeKey_WithTwoKeys() {
        SchemaRegistry schemaRegistry = mock(SchemaRegistry.class);
        ModelNodeWithAttributes modelNode = mock(ModelNodeWithAttributes.class);

        SchemaPath schemaPath = mock(SchemaPath.class);
        when(modelNode.getModelNodeSchemaPath()).thenReturn(schemaPath);
        DataSchemaNode schemaNode = mock(ListSchemaNode.class);
        when(schemaRegistry.getDataSchemaNode(schemaPath)).thenReturn(schemaNode);

        List<QName> keyDefinitions = new LinkedList<>();
        QName key1 = QName.create("testNS", "key1");
        keyDefinitions.add(key1);
        QName key2 = QName.create("testNS", "key2");
        keyDefinitions.add(key2);
        when(((ListSchemaNode) schemaNode).getKeyDefinition()).thenReturn(keyDefinitions);

        Map<QName, ConfigLeafAttribute> attributes = new HashMap<>();
        attributes.put(key1, new GenericConfigAttribute("Value1"));
        attributes.put(key2, new GenericConfigAttribute("Value2"));
        when(modelNode.getAttributes()).thenReturn(attributes);


        ModelNodeKey modelNodeKey = MNKeyUtil.getModelNodeKey(modelNode, schemaRegistry);
        Map<QName, String> keys = modelNodeKey.getKeys();
        assertEquals(2, keys.size());
        Iterator<QName> iterator = keys.keySet().iterator();
        assertEquals("key1", iterator.next().getLocalName());
        assertEquals("key2", iterator.next().getLocalName());
    }
}
