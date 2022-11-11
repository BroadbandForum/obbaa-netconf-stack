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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.dsm;

import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn.CONTAINER;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ALBUM_LOCAL_NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ALBUM_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ARTIST_LOCAL_NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JB_NS;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JUKEBOX_LOCAL_NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.LIBRARY_LOCAL_NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.NAME_QNAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.SINGER_LOCAL_NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.SINGER_QNAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.api.messages.InsertOperation;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeKey;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.GenericConfigAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.SetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;

@RunWith(RequestScopeJunitRunner.class)
public class DsmChildLeafListHelperTest {

    private static final String EXAMPLE_JUKEBOX_YANGFILE = "/dsmchildleaflisthelpertest/example-jukebox.yang";
    private static final String EXAMPLE_JUKEBOX_YANGTYPES = "/dsmchildleaflisthelpertest/example-jukebox-types.yang";
    private static final String ARTIST_NAME = "Artist";
    private static final String ALBUM_NAME = "album";
    public static final String SINGER_ORDERED_BY_USER_LOCALNAME = "singer-ordered-by-user";
    public static final QName SINGER_ORDERED_BY_USER_QNAME = QName.create(JB_NS, SINGER_ORDERED_BY_USER_LOCALNAME);

    private ModelNodeId m_jukeboxNodeId = new ModelNodeId().addRdn(new ModelNodeRdn(CONTAINER, JB_NS, JUKEBOX_LOCAL_NAME));
    private ModelNodeId m_libraryNodeId = new ModelNodeId(m_jukeboxNodeId).addRdn(new ModelNodeRdn(CONTAINER, JB_NS, LIBRARY_LOCAL_NAME));
    private ModelNodeId m_artistNodeId = new ModelNodeId(m_libraryNodeId).addRdn(new ModelNodeRdn(CONTAINER, JB_NS, ARTIST_LOCAL_NAME))
            .addRdn(new ModelNodeRdn(NAME_QNAME, ARTIST_NAME));
    private ModelNodeId m_albumNodeId = new ModelNodeId(m_artistNodeId).addRdn(new ModelNodeRdn(CONTAINER, JB_NS, ALBUM_LOCAL_NAME))
            .addRdn(new ModelNodeRdn(NAME_QNAME, ALBUM_NAME));

    private SchemaRegistry m_schemaRegistry;
    private ModelNodeDataStoreManager m_modelNodeDSM;

    @Before
    public void setup() throws SchemaBuildException {
        m_modelNodeDSM = mock(ModelNodeDataStoreManager.class);
        List<YangTextSchemaSource> yangs = TestUtil.getJukeBoxDeps();
        yangs.add(TestUtil.getByteSource(EXAMPLE_JUKEBOX_YANGTYPES));
        yangs.add(TestUtil.getByteSource(EXAMPLE_JUKEBOX_YANGFILE));
        m_schemaRegistry = new SchemaRegistryImpl(yangs, Collections.emptySet(), Collections.emptyMap(), new NoLockService());
    }

    @Test
    public void testAddChild() throws SetAttributeException, GetAttributeException {
        DsmChildLeafListHelper childLeafListHelper = spy(new DsmChildLeafListHelper(mock(LeafListSchemaNode.class), SINGER_QNAME,
                m_modelNodeDSM, m_schemaRegistry));

        ModelNodeWithAttributes albumModelNode = new ModelNodeWithAttributes(ALBUM_SCHEMA_PATH, m_albumNodeId,
                null, null, m_schemaRegistry, m_modelNodeDSM);
        populateValuesInModelNode(albumModelNode);

        ModelNodeWithAttributes albumModelNode1 = new ModelNodeWithAttributes(ALBUM_SCHEMA_PATH, m_albumNodeId,
                null, null, m_schemaRegistry, m_modelNodeDSM);
        populateValuesInModelNode(albumModelNode1);
        updateModelNode(albumModelNode1);

        when(m_modelNodeDSM.findNode(any(SchemaPath.class), any(ModelNodeKey.class), any(ModelNodeId.class), any(SchemaRegistry.class))).
                thenReturn(albumModelNode).thenReturn(albumModelNode1);
        
        when(albumModelNode.getParent()).thenReturn(null);
        when(albumModelNode1.getParent()).thenReturn(null);

        childLeafListHelper.addChild(albumModelNode, new GenericConfigAttribute(SINGER_LOCAL_NAME, JB_NS, "singer3"));
        verify(childLeafListHelper).getValue(albumModelNode);

        LinkedHashSet<ConfigLeafAttribute> values = new LinkedHashSet<>();
        values.add(new GenericConfigAttribute(SINGER_LOCAL_NAME, JB_NS, "singer1"));
        values.add(new GenericConfigAttribute(SINGER_LOCAL_NAME, JB_NS, "singer2"));
        values.add(new GenericConfigAttribute(SINGER_LOCAL_NAME, JB_NS, "singer3"));

        verify(m_modelNodeDSM, times(1)).updateNode(albumModelNode, m_artistNodeId, null, Collections.singletonMap(SINGER_QNAME, values), false);

        childLeafListHelper.addChild(albumModelNode, new GenericConfigAttribute(SINGER_LOCAL_NAME, JB_NS, "singer4"));
        verify(childLeafListHelper, times(2)).getValue(albumModelNode1);
        values.add(new GenericConfigAttribute(SINGER_LOCAL_NAME, JB_NS, "singer4"));

        verify(m_modelNodeDSM, times(1)).updateNode(albumModelNode, m_artistNodeId, null, Collections.singletonMap(SINGER_QNAME, values), false);
    }

    @Test
    public void testAddChildByUserOrder_NoExistingLeafLists() throws Exception {

        ModelNodeWithAttributes albumModelNode = getAlbumModelNode();

        LinkedHashSet<ConfigLeafAttribute> expectedLeafLists = new LinkedHashSet<>();
        ConfigLeafAttribute singer = new GenericConfigAttribute(SINGER_ORDERED_BY_USER_LOCALNAME, JB_NS, "singer-ordered-by-user0");
        expectedLeafLists.add(singer);

        for (ConfigLeafAttribute expectedLeafList : expectedLeafLists) {
            assertEquals(-1, expectedLeafList.getInsertIndex().intValue());
        }

        LeafListSchemaNode leafListSchemaNode = mock(LeafListSchemaNode.class);
        when(leafListSchemaNode.isUserOrdered()).thenReturn(true);
        DsmChildLeafListHelper childLeafListHelper = spy(new DsmChildLeafListHelper(leafListSchemaNode, SINGER_ORDERED_BY_USER_QNAME,
                m_modelNodeDSM, m_schemaRegistry));
        childLeafListHelper.addChildByUserOrder(albumModelNode, singer, "create", InsertOperation.FIRST_OP);

        verify(m_modelNodeDSM).updateNode(albumModelNode, m_artistNodeId, null, Collections.singletonMap(SINGER_ORDERED_BY_USER_QNAME,
                expectedLeafLists), -1,false);

        assertEquals(0, expectedLeafLists.iterator().next().getInsertIndex().intValue());
    }

    @Test
    public void testAddChildByUserOrder_InsertOpLast() throws Exception {

        ModelNodeWithAttributes albumModelNode = getAlbumModelNode();

        LinkedHashSet<ConfigLeafAttribute> expectedLeafLists = new LinkedHashSet<>();
        ConfigLeafAttribute singer3 = new GenericConfigAttribute(SINGER_ORDERED_BY_USER_LOCALNAME, JB_NS, "singer-ordered-by-user3");
        expectedLeafLists.add(singer3);
        expectedLeafLists.addAll(populateOrderedByUserValuesInModelNode(albumModelNode));

        for (ConfigLeafAttribute expectedLeafList : expectedLeafLists) {
            assertEquals(-1, expectedLeafList.getInsertIndex().intValue());
        }

        LeafListSchemaNode leafListSchemaNode = mock(LeafListSchemaNode.class);
        when(leafListSchemaNode.isUserOrdered()).thenReturn(true);
        DsmChildLeafListHelper childLeafListHelper = spy(new DsmChildLeafListHelper(leafListSchemaNode, SINGER_ORDERED_BY_USER_QNAME,
                m_modelNodeDSM, m_schemaRegistry));
        childLeafListHelper.addChildByUserOrder(albumModelNode, singer3, "create", InsertOperation.LAST_OP);

        verify(m_modelNodeDSM).updateNode(albumModelNode, m_artistNodeId, null, Collections.singletonMap(SINGER_ORDERED_BY_USER_QNAME,
                expectedLeafLists), -1,false);

        assertInsertIndex(0, "singer-ordered-by-user1", expectedLeafLists);
        assertInsertIndex(1, "singer-ordered-by-user2", expectedLeafLists);
        assertInsertIndex(2, "singer-ordered-by-user3", expectedLeafLists);
    }

    @Test
    public void testAddChildByUserOrder_InsertOpFirst() throws Exception {

        ModelNodeWithAttributes albumModelNode = getAlbumModelNode();

        LinkedHashSet<ConfigLeafAttribute> expectedLeafLists = new LinkedHashSet<>();
        ConfigLeafAttribute singer0 = new GenericConfigAttribute(SINGER_ORDERED_BY_USER_LOCALNAME, JB_NS, "singer-ordered-by-user0");
        expectedLeafLists.add(singer0);
        expectedLeafLists.addAll(populateOrderedByUserValuesInModelNode(albumModelNode));

        for (ConfigLeafAttribute expectedLeafList : expectedLeafLists) {
            assertEquals(-1, expectedLeafList.getInsertIndex().intValue());
        }

        LeafListSchemaNode leafListSchemaNode = mock(LeafListSchemaNode.class);
        when(leafListSchemaNode.isUserOrdered()).thenReturn(true);
        DsmChildLeafListHelper childLeafListHelper = spy(new DsmChildLeafListHelper(leafListSchemaNode, SINGER_ORDERED_BY_USER_QNAME,
                m_modelNodeDSM, m_schemaRegistry));
        childLeafListHelper.addChildByUserOrder(albumModelNode, singer0, "create", InsertOperation.FIRST_OP);

        verify(m_modelNodeDSM).updateNode(albumModelNode, m_artistNodeId, null, Collections.singletonMap(SINGER_ORDERED_BY_USER_QNAME,
                expectedLeafLists), -1,false);

        assertInsertIndex(0, "singer-ordered-by-user0", expectedLeafLists);
        assertInsertIndex(1, "singer-ordered-by-user1", expectedLeafLists);
        assertInsertIndex(2, "singer-ordered-by-user2", expectedLeafLists);
    }

    @Test
    public void testAddChildByUserOrder_InsertOpAfter() throws Exception {

        ModelNodeWithAttributes albumModelNode = getAlbumModelNode();

        LinkedHashSet<ConfigLeafAttribute> expectedLeafLists = new LinkedHashSet<>();
        ConfigLeafAttribute singer = new GenericConfigAttribute(SINGER_ORDERED_BY_USER_LOCALNAME, JB_NS, "singer-ordered-by-user-after-1");
        expectedLeafLists.add(singer);
        expectedLeafLists.addAll(populateOrderedByUserValuesInModelNode(albumModelNode));

        for (ConfigLeafAttribute expectedLeafList : expectedLeafLists) {
            assertEquals(-1, expectedLeafList.getInsertIndex().intValue());
        }

        LeafListSchemaNode leafListSchemaNode = mock(LeafListSchemaNode.class);
        when(leafListSchemaNode.isUserOrdered()).thenReturn(true);
        DsmChildLeafListHelper childLeafListHelper = spy(new DsmChildLeafListHelper(leafListSchemaNode, SINGER_ORDERED_BY_USER_QNAME,
                m_modelNodeDSM, m_schemaRegistry));
        childLeafListHelper.addChildByUserOrder(albumModelNode, singer, "create", InsertOperation.get(InsertOperation.AFTER,"singer-ordered-by-user1"));

        verify(m_modelNodeDSM).updateNode(albumModelNode, m_artistNodeId, null, Collections.singletonMap(SINGER_ORDERED_BY_USER_QNAME,
                expectedLeafLists), -1,false);

        assertInsertIndex(0, "singer-ordered-by-user1", expectedLeafLists);
        assertInsertIndex(1, "singer-ordered-by-user-after-1", expectedLeafLists);
        assertInsertIndex(2, "singer-ordered-by-user2", expectedLeafLists);
    }

    @Test
    public void testAddChildByUserOrder_InsertOpAfterLastValue() throws Exception {

        ModelNodeWithAttributes albumModelNode = getAlbumModelNode();

        LinkedHashSet<ConfigLeafAttribute> expectedLeafLists = new LinkedHashSet<>();
        ConfigLeafAttribute singer = new GenericConfigAttribute(SINGER_ORDERED_BY_USER_LOCALNAME, JB_NS, "singer-ordered-by-user-last");
        expectedLeafLists.add(singer);
        expectedLeafLists.addAll(populateOrderedByUserValuesInModelNode(albumModelNode));

        for (ConfigLeafAttribute expectedLeafList : expectedLeafLists) {
            assertEquals(-1, expectedLeafList.getInsertIndex().intValue());
        }

        LeafListSchemaNode leafListSchemaNode = mock(LeafListSchemaNode.class);
        when(leafListSchemaNode.isUserOrdered()).thenReturn(true);
        DsmChildLeafListHelper childLeafListHelper = spy(new DsmChildLeafListHelper(leafListSchemaNode, SINGER_ORDERED_BY_USER_QNAME,
                m_modelNodeDSM, m_schemaRegistry));
        childLeafListHelper.addChildByUserOrder(albumModelNode, singer, "create", InsertOperation.get(InsertOperation.AFTER,"singer-ordered-by-user2"));

        verify(m_modelNodeDSM).updateNode(albumModelNode, m_artistNodeId, null, Collections.singletonMap(SINGER_ORDERED_BY_USER_QNAME,
                expectedLeafLists), -1,false);

        assertInsertIndex(0, "singer-ordered-by-user1", expectedLeafLists);
        assertInsertIndex(1, "singer-ordered-by-user2", expectedLeafLists);
        assertInsertIndex(2, "singer-ordered-by-user-last", expectedLeafLists);
    }

    @Test
    public void testAddChildByUserOrder_InsertOpBefore() throws Exception {

        ModelNodeWithAttributes albumModelNode = getAlbumModelNode();

        LinkedHashSet<ConfigLeafAttribute> expectedLeafLists = new LinkedHashSet<>();
        ConfigLeafAttribute singer = new GenericConfigAttribute(SINGER_ORDERED_BY_USER_LOCALNAME, JB_NS, "singer-ordered-by-user-before-2");
        expectedLeafLists.add(singer);
        expectedLeafLists.addAll(populateOrderedByUserValuesInModelNode(albumModelNode));

        for (ConfigLeafAttribute expectedLeafList : expectedLeafLists) {
            assertEquals(-1, expectedLeafList.getInsertIndex().intValue());
        }

        LeafListSchemaNode leafListSchemaNode = mock(LeafListSchemaNode.class);
        when(leafListSchemaNode.isUserOrdered()).thenReturn(true);
        DsmChildLeafListHelper childLeafListHelper = spy(new DsmChildLeafListHelper(leafListSchemaNode, SINGER_ORDERED_BY_USER_QNAME,
                m_modelNodeDSM, m_schemaRegistry));
        childLeafListHelper.addChildByUserOrder(albumModelNode, singer, "create", InsertOperation.get(InsertOperation.BEFORE,"singer-ordered-by-user2"));

        verify(m_modelNodeDSM).updateNode(albumModelNode, m_artistNodeId, null, Collections.singletonMap(SINGER_ORDERED_BY_USER_QNAME,
                expectedLeafLists), -1,false);

        assertInsertIndex(0, "singer-ordered-by-user1", expectedLeafLists);
        assertInsertIndex(1, "singer-ordered-by-user-before-2", expectedLeafLists);
        assertInsertIndex(2, "singer-ordered-by-user2", expectedLeafLists);
    }

    private void assertInsertIndex(int expectedInsertIndex, String leafListValue, LinkedHashSet<ConfigLeafAttribute> expectedLeafLists) {
        boolean isPresent = false;
        for (ConfigLeafAttribute expectedLeafList : expectedLeafLists) {
            if(expectedLeafList.getStringValue().equals(leafListValue)){
                isPresent = true;
                assertEquals(expectedInsertIndex, expectedLeafList.getInsertIndex().intValue());
                break;
            }
        }
        if(!isPresent){
            fail("Expected Leaf-list not present ");
        }
    }

    private void populateValuesInModelNode(ModelNodeWithAttributes modelNode) {
        Map<QName, ConfigLeafAttribute> attributes = new HashMap<>();
        attributes.put(NAME_QNAME, new GenericConfigAttribute(SINGER_LOCAL_NAME, JB_NS, "album"));
        modelNode.setAttributes(attributes);

        Map<QName, LinkedHashSet<ConfigLeafAttribute>> leafListAttrs = new HashMap<>();
        LinkedHashSet<ConfigLeafAttribute> singerLeafList = new LinkedHashSet<>();
        singerLeafList.add(new GenericConfigAttribute(SINGER_LOCAL_NAME, JB_NS, "singer1"));
        singerLeafList.add(new GenericConfigAttribute(SINGER_LOCAL_NAME, JB_NS, "singer2"));
        leafListAttrs.put(SINGER_QNAME, singerLeafList);
        modelNode.setLeafLists(leafListAttrs);
        modelNode.setModelNodeId(new ModelNodeId(m_albumNodeId));
    }

    private LinkedHashSet<ConfigLeafAttribute> populateOrderedByUserValuesInModelNode(ModelNodeWithAttributes modelNode) {
        Map<QName, LinkedHashSet<ConfigLeafAttribute>> leafListAttrs = new HashMap<>();
        LinkedHashSet<ConfigLeafAttribute> singerLeafList = new LinkedHashSet<>();
        singerLeafList.add(new GenericConfigAttribute(SINGER_ORDERED_BY_USER_LOCALNAME, JB_NS, "singer-ordered-by-user1"));
        singerLeafList.add(new GenericConfigAttribute(SINGER_ORDERED_BY_USER_LOCALNAME, JB_NS, "singer-ordered-by-user2"));
        leafListAttrs.put(SINGER_ORDERED_BY_USER_QNAME, singerLeafList);
        modelNode.setLeafLists(leafListAttrs);
        return singerLeafList;
    }

    private void populateLeafAttributes(ModelNodeWithAttributes modelNode) {
        Map<QName, ConfigLeafAttribute> attributes = new HashMap<>();
        attributes.put(NAME_QNAME, new GenericConfigAttribute(NAME, JB_NS, "album"));
        modelNode.setAttributes(attributes);
        modelNode.setModelNodeId(new ModelNodeId(m_albumNodeId));
    }

    private ModelNodeWithAttributes getAlbumModelNode() {
        ModelNodeWithAttributes albumModelNode = new ModelNodeWithAttributes(ALBUM_SCHEMA_PATH, m_albumNodeId,
                null, null, m_schemaRegistry, m_modelNodeDSM);
        populateLeafAttributes(albumModelNode);

        Map<QName, String> keys = new HashMap<>();
        keys.put(NAME_QNAME, ALBUM_NAME);
        ModelNodeKey modelNodeKey = new ModelNodeKey(keys);
        when(m_modelNodeDSM.findNode(ALBUM_SCHEMA_PATH, modelNodeKey, m_artistNodeId, m_schemaRegistry)).
                thenReturn(albumModelNode);
        return albumModelNode;
    }

    private void updateModelNode(ModelNodeWithAttributes modelNode) {
        Map<QName, LinkedHashSet<ConfigLeafAttribute>> leafListAttrs = modelNode.getLeafLists();
        LinkedHashSet<ConfigLeafAttribute> singerLeafList = (LinkedHashSet<ConfigLeafAttribute>)
                modelNode.getLeafList(SINGER_QNAME);

        singerLeafList.add(new GenericConfigAttribute(SINGER_LOCAL_NAME, JB_NS, "singer3"));
        leafListAttrs.put(SINGER_QNAME, singerLeafList);
        modelNode.setLeafLists(leafListAttrs);
    }
}