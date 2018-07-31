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


import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ALBUM_LOCAL_NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ALBUM_QNAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ALBUM_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ARTIST_LOCAL_NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.FORMAT_QNAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JB_NS;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JUKEBOX_LOCAL_NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JUKEBOX_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.LIBRARY_LOCAL_NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.LOCATION_QNAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.NAME_QNAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.SONG_LOCAL_NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.SONG_QNAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.SONG_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn.CONTAINER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigAttributeHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeCreateException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.utils.AnnotationAnalysisException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import org.broadband_forum.obbaa.netconf.api.messages.EditConfigOperations;
import org.broadband_forum.obbaa.netconf.api.messages.InsertOperation;
import org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.DataStoreException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeKey;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.GenericConfigAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;

public class XmlListModelNodeHelperTest {

    private static final String SONG1 = "song1";
    private static final String SONG2 = "song2";
    private static final String SONG3 = "song3";
    private static final String SONG4 = "song4";

    private ModelNodeDataStoreManager m_datastoreManager;
    private SchemaRegistry m_schemaRegistry;
    private ListSchemaNode m_listSchemaNode;
    private ModelNodeHelperRegistry m_modelNodeHelperRegistry;
    private XmlListModelNodeHelper m_xmlListModelNodeHelper;

    public ModelNodeId m_jukeboxNodeId = new ModelNodeId().addRdn(new ModelNodeRdn(CONTAINER, JB_NS,
            JUKEBOX_LOCAL_NAME));
    public ModelNodeId m_libraryNodeId = new ModelNodeId(m_jukeboxNodeId).addRdn(new ModelNodeRdn(CONTAINER, JB_NS,
            LIBRARY_LOCAL_NAME));
    public final ModelNodeId m_artistId = new ModelNodeId(m_libraryNodeId).addRdn(new ModelNodeRdn(CONTAINER, JB_NS,
            ARTIST_LOCAL_NAME))
            .addRdn(new ModelNodeRdn("name", JB_NS, "artist"));
    public final ModelNodeId m_albumNodeId = new ModelNodeId(m_artistId).addRdn(new ModelNodeRdn(CONTAINER, JB_NS,
            ALBUM_LOCAL_NAME))
            .addRdn(new ModelNodeRdn("name", JB_NS, "1st Album"));

    @Before
    public void initServer() throws SchemaBuildException, ModelNodeInitException, AnnotationAnalysisException,
            DataStoreException, GetAttributeException {
        m_schemaRegistry = mock(SchemaRegistry.class);
        m_listSchemaNode = mock(ListSchemaNode.class);
        m_datastoreManager = mock(ModelNodeDataStoreManager.class);
        m_modelNodeHelperRegistry = mock(ModelNodeHelperRegistry.class);
        when(m_modelNodeHelperRegistry.getNaturalKeyHelpers(JUKEBOX_SCHEMA_PATH)).thenReturn(Collections.<QName,
                ConfigAttributeHelper>emptyMap());

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
        when(m_modelNodeHelperRegistry.getNaturalKeyHelpers(ALBUM_SCHEMA_PATH)).thenReturn(value);

        m_xmlListModelNodeHelper = new XmlListModelNodeHelper(m_listSchemaNode, m_modelNodeHelperRegistry,
                m_datastoreManager, m_schemaRegistry, null);
    }

    @Test
    public void testAddChildFindsUpdatedParentNode() throws ModelNodeCreateException, DataStoreException {
        XmlModelNodeImpl albumNode = new XmlModelNodeImpl(ALBUM_SCHEMA_PATH,
                Collections.singletonMap(NAME_QNAME, new GenericConfigAttribute("1st Album")), Collections
                .<Element>emptyList(), null,
                m_artistId,
                null, m_modelNodeHelperRegistry, m_schemaRegistry, null, m_datastoreManager);

        XmlModelNodeImpl freshAlbumNode = new XmlModelNodeImpl(ALBUM_SCHEMA_PATH,
                Collections.singletonMap(NAME_QNAME, new GenericConfigAttribute("1st Album")), Collections
                .<Element>emptyList(), null,
                m_artistId,
                null, m_modelNodeHelperRegistry, m_schemaRegistry, null, m_datastoreManager);
        freshAlbumNode.setModelNodeId(m_albumNodeId);

        when(m_datastoreManager.findNode(any(SchemaPath.class), any(ModelNodeKey.class), any(ModelNodeId.class))).
                thenReturn(freshAlbumNode);
        when(m_listSchemaNode.getPath()).thenReturn(SONG_SCHEMA_PATH);
        Map<QName, ConfigLeafAttribute> attributes = new HashMap<>();
        attributes.put(FORMAT_QNAME, new GenericConfigAttribute("mp3"));
        attributes.put(LOCATION_QNAME, new GenericConfigAttribute("mymusic"));
        ModelNode newSongNode = m_xmlListModelNodeHelper.addChild(albumNode, SONG_LOCAL_NAME,
                Collections.singletonMap(NAME_QNAME, new GenericConfigAttribute("song2")), attributes);
        verify(m_datastoreManager).findNode(eq(ALBUM_SCHEMA_PATH), any(ModelNodeKey.class), any(ModelNodeId.class));
        assertTrue(newSongNode instanceof XmlModelNodeImpl);
        assertEquals(SONG_QNAME, newSongNode.getQName());
        assertEquals(freshAlbumNode, newSongNode.getParent());
        assertEquals(freshAlbumNode.getModelNodeId(), ((XmlModelNodeImpl) newSongNode).getParentNodeId());

        Map<QName, ConfigLeafAttribute> allAttributes = ((XmlModelNodeImpl) newSongNode).getAttributes();
        assertEquals(NAME_QNAME, allAttributes.keySet().iterator().next());
    }

    @Test
    public void testAddChildByUserOrderFindsUpdatedParentNode() throws ModelNodeCreateException, DataStoreException {
        XmlModelNodeImpl albumNode = new XmlModelNodeImpl(ALBUM_SCHEMA_PATH,
                Collections.singletonMap(NAME_QNAME, new GenericConfigAttribute("1st Album")), Collections
                .<Element>emptyList(), null,
                m_artistId,
                null, m_modelNodeHelperRegistry, m_schemaRegistry, null, m_datastoreManager);

        XmlModelNodeImpl freshAlbumNode = new XmlModelNodeImpl(ALBUM_SCHEMA_PATH,
                Collections.singletonMap(NAME_QNAME, new GenericConfigAttribute("1st Album")), Collections
                .<Element>emptyList(), null,
                m_artistId,
                null, m_modelNodeHelperRegistry, m_schemaRegistry, null, m_datastoreManager);
        freshAlbumNode.setModelNodeId(m_albumNodeId);

        when(m_datastoreManager.findNode(any(SchemaPath.class), any(ModelNodeKey.class), any(ModelNodeId.class))).
                thenReturn(freshAlbumNode);

        when(m_listSchemaNode.getPath()).thenReturn(SONG_SCHEMA_PATH);

        InsertOperation insertOper = new InsertOperation(InsertOperation.LAST, null);
        Map<QName, ConfigLeafAttribute> attributes = new HashMap<>();
        attributes.put(FORMAT_QNAME, new GenericConfigAttribute("mp3"));
        attributes.put(LOCATION_QNAME, new GenericConfigAttribute("mymusic"));
        ModelNode newSongNode = m_xmlListModelNodeHelper.addChildByUserOrder(albumNode, Collections.singletonMap
                        (NAME_QNAME, new
                        GenericConfigAttribute("song2")),
                attributes, insertOper, null);
        verify(m_datastoreManager).findNode(eq(ALBUM_SCHEMA_PATH), any(ModelNodeKey.class), any(ModelNodeId.class));
        assertTrue(newSongNode instanceof XmlModelNodeImpl);
        assertEquals(SONG_QNAME, newSongNode.getQName());
        assertEquals(freshAlbumNode, newSongNode.getParent());
        assertEquals(freshAlbumNode.getModelNodeId(), ((XmlModelNodeImpl) newSongNode).getParentNodeId());

        Map<QName, ConfigLeafAttribute> allAttributes = ((XmlModelNodeImpl) newSongNode).getAttributes();
        assertEquals(NAME_QNAME, allAttributes.keySet().iterator().next());
    }

    @Test
    public void testListCreateOrderedByUser() throws ModelNodeInitException, SAXException, IOException,
            SchemaBuildException, ModelNodeCreateException, DataStoreException {

        XmlModelNodeImpl albumNode = new XmlModelNodeImpl(ALBUM_SCHEMA_PATH,
                Collections.<QName, ConfigLeafAttribute>emptyMap(),
                Collections.<Element>emptyList(), null, m_artistId, null,
                m_modelNodeHelperRegistry, m_schemaRegistry, null, m_datastoreManager);

        albumNode.setModelNodeId(m_albumNodeId);

        List<ModelNode> modelNodeList = new ArrayList<>();
        when(m_datastoreManager.findNodes(eq(ALBUM_SCHEMA_PATH), anyMap(), eq(m_albumNodeId))).thenReturn
                (modelNodeList);
        when(m_datastoreManager.findNode(eq(ALBUM_SCHEMA_PATH), any(ModelNodeKey.class), eq(m_artistId))).thenReturn
                (albumNode);
        when(m_listSchemaNode.getQName()).thenReturn(ALBUM_QNAME);
        when(m_listSchemaNode.getPath()).thenReturn(ALBUM_SCHEMA_PATH);
        when(m_listSchemaNode.isUserOrdered()).thenReturn(true);

        InsertOperation insertOper1 = new InsertOperation(InsertOperation.FIRST, null);
        InsertOperation insertOper2 = new InsertOperation(InsertOperation.BEFORE, null);
        InsertOperation insertOper3 = new InsertOperation(InsertOperation.AFTER, null);
        InsertOperation insertOper4 = new InsertOperation(InsertOperation.LAST, null);

        ModelNode song1 = addChildByUser(albumNode, modelNodeList, SONG1, EditConfigOperations.CREATE, insertOper1,
                null);
        ModelNode song2 = addChildByUser(albumNode, modelNodeList, SONG2, EditConfigOperations.CREATE, insertOper2,
                song1);
        ModelNode song3 = addChildByUser(albumNode, modelNodeList, SONG3, EditConfigOperations.CREATE, insertOper3,
                song1);
        ModelNode song4 = addChildByUser(albumNode, modelNodeList, SONG4, EditConfigOperations.CREATE, insertOper4,
                null);

        List<ModelNode> expectedModelNodeList = new ArrayList<ModelNode>();
        expectedModelNodeList.add(song2);
        expectedModelNodeList.add(song1);
        expectedModelNodeList.add(song3);
        expectedModelNodeList.add(song4);

        assertEquals(expectedModelNodeList, modelNodeList);

    }

    private ModelNode addChildByUser(XmlModelNodeImpl albumNode, List<ModelNode> modelNodeList, String songName,
                                     String operation, InsertOperation insertOperation, ModelNode indexSong) throws
            ModelNodeCreateException {
        Map<QName, ConfigLeafAttribute> keyAttrs = new HashMap<>();
        keyAttrs.put(JukeboxConstants.NAME_QNAME, new GenericConfigAttribute(songName));

        ModelNode song = m_xmlListModelNodeHelper.addChildByUserOrder(albumNode, keyAttrs, Collections.<QName,
                        ConfigLeafAttribute>emptyMap(),
                insertOperation, indexSong);
        assertTrue(song instanceof XmlModelNodeImpl);
        int insertIndex = m_xmlListModelNodeHelper.getInsertIndex();
        if (insertIndex == -1) {
            insertIndex = modelNodeList.size();
        }
        modelNodeList.add(insertIndex, song);
        return song;
    }

    private ModelNode addChild(XmlModelNodeImpl albumNode, List<ModelNode> modelNodeList, String songName, String
            operation) throws ModelNodeCreateException {
        Map<QName, ConfigLeafAttribute> keyAttrs = new HashMap<>();
        keyAttrs.put(JukeboxConstants.NAME_QNAME, new GenericConfigAttribute(songName));

        ModelNode song = m_xmlListModelNodeHelper.addChild(albumNode, SONG_LOCAL_NAME, keyAttrs, Collections.<QName,
                ConfigLeafAttribute>emptyMap());
        assertTrue(song instanceof XmlModelNodeImpl);
        modelNodeList.add(song);
        return song;
    }

    @Test
    public void testListMergeOrderedByUser() throws ModelNodeInitException, SAXException, IOException,
            SchemaBuildException, DataStoreException, ModelNodeCreateException {
        XmlModelNodeImpl albumNode = new XmlModelNodeImpl(ALBUM_SCHEMA_PATH,
                Collections.<QName, ConfigLeafAttribute>emptyMap(),
                Collections.<Element>emptyList(), null, m_artistId, null,
                m_modelNodeHelperRegistry, m_schemaRegistry, null, m_datastoreManager);

        albumNode.setModelNodeId(m_albumNodeId);

        List<ModelNode> modelNodeList = new ArrayList<ModelNode>();
        when(m_datastoreManager.findNodes(eq(ALBUM_SCHEMA_PATH), anyMap(), eq(m_albumNodeId))).thenReturn
                (modelNodeList);
        when(m_datastoreManager.findNode(eq(ALBUM_SCHEMA_PATH), any(ModelNodeKey.class), eq(m_artistId))).thenReturn
                (albumNode);
        when(m_listSchemaNode.getQName()).thenReturn(ALBUM_QNAME);
        when(m_listSchemaNode.getPath()).thenReturn(ALBUM_SCHEMA_PATH);
        when(m_listSchemaNode.isUserOrdered()).thenReturn(true);

        InsertOperation insertOper1 = new InsertOperation(InsertOperation.FIRST, null);
        InsertOperation insertOper2 = new InsertOperation(InsertOperation.BEFORE, null);
        InsertOperation insertOper3 = new InsertOperation(InsertOperation.AFTER, null);
        InsertOperation insertOper4 = new InsertOperation(InsertOperation.LAST, null);

        ModelNode song1 = addChildByUser(albumNode, modelNodeList, SONG1, EditConfigOperations.MERGE, insertOper1,
                null);
        ModelNode song2 = addChildByUser(albumNode, modelNodeList, SONG2, EditConfigOperations.MERGE, insertOper2,
                song1);
        ModelNode song3 = addChildByUser(albumNode, modelNodeList, SONG3, EditConfigOperations.MERGE, insertOper3,
                song1);
        ModelNode song4 = addChildByUser(albumNode, modelNodeList, SONG4, EditConfigOperations.MERGE, insertOper4,
                null);

        List<ModelNode> expectedModelNodeList = new ArrayList<ModelNode>();
        expectedModelNodeList.add(song2);
        expectedModelNodeList.add(song1);
        expectedModelNodeList.add(song3);
        expectedModelNodeList.add(song4);

        assertEquals(expectedModelNodeList, modelNodeList);
    }

    @Test
    public void testListReplaceOrderedByUser() throws ModelNodeInitException, SAXException, IOException,
            SchemaBuildException, ModelNodeCreateException, DataStoreException {
        XmlModelNodeImpl albumNode = new XmlModelNodeImpl(ALBUM_SCHEMA_PATH,
                Collections.<QName, ConfigLeafAttribute>emptyMap(),
                Collections.<Element>emptyList(), null, m_artistId, null,
                m_modelNodeHelperRegistry, m_schemaRegistry, null, m_datastoreManager);

        albumNode.setModelNodeId(m_albumNodeId);

        List<ModelNode> modelNodeList = new ArrayList<ModelNode>();
        when(m_datastoreManager.findNodes(eq(ALBUM_SCHEMA_PATH), anyMap(), eq(m_albumNodeId))).thenReturn
                (modelNodeList);
        when(m_datastoreManager.findNode(eq(ALBUM_SCHEMA_PATH), any(ModelNodeKey.class), eq(m_artistId))).thenReturn
                (albumNode);
        when(m_listSchemaNode.getQName()).thenReturn(ALBUM_QNAME);
        when(m_listSchemaNode.getPath()).thenReturn(ALBUM_SCHEMA_PATH);
        when(m_listSchemaNode.isUserOrdered()).thenReturn(true);

        InsertOperation insertOper1 = new InsertOperation(InsertOperation.FIRST, null);
        InsertOperation insertOper2 = new InsertOperation(InsertOperation.BEFORE, null);
        InsertOperation insertOper3 = new InsertOperation(InsertOperation.AFTER, null);
        InsertOperation insertOper4 = new InsertOperation(InsertOperation.LAST, null);

        ModelNode song1 = addChildByUser(albumNode, modelNodeList, SONG1, EditConfigOperations.REPLACE, insertOper1,
                null);
        ModelNode song2 = addChildByUser(albumNode, modelNodeList, SONG2, EditConfigOperations.REPLACE, insertOper2,
                song1);
        ModelNode song3 = addChildByUser(albumNode, modelNodeList, SONG3, EditConfigOperations.REPLACE, insertOper3,
                song1);
        ModelNode song4 = addChildByUser(albumNode, modelNodeList, SONG4, EditConfigOperations.REPLACE, insertOper4,
                null);

        List<ModelNode> expectedModelNodeList = new ArrayList<ModelNode>();
        expectedModelNodeList.add(song2);
        expectedModelNodeList.add(song1);
        expectedModelNodeList.add(song3);
        expectedModelNodeList.add(song4);

        assertEquals(expectedModelNodeList, modelNodeList);
    }

    @Test
    public void testListOrderedBySystem() throws SAXException, IOException, DataStoreException,
            ModelNodeCreateException {
        XmlModelNodeImpl albumNode = new XmlModelNodeImpl(ALBUM_SCHEMA_PATH,
                Collections.<QName, ConfigLeafAttribute>emptyMap(),
                Collections.<Element>emptyList(), null, m_artistId, null,
                m_modelNodeHelperRegistry, m_schemaRegistry, null, m_datastoreManager);

        albumNode.setModelNodeId(m_albumNodeId);

        List<ModelNode> modelNodeList = new ArrayList<>();
        when(m_datastoreManager.listChildNodes(ALBUM_SCHEMA_PATH, m_albumNodeId)).thenReturn(modelNodeList);
        when(m_datastoreManager.findNode(eq(ALBUM_SCHEMA_PATH), (ModelNodeKey) anyObject(), eq(m_artistId)))
                .thenReturn(albumNode);
        when(m_listSchemaNode.getQName()).thenReturn(ALBUM_QNAME);
        when(m_listSchemaNode.getPath()).thenReturn(ALBUM_SCHEMA_PATH);

        ModelNode testSong = addChild(albumNode, modelNodeList, "TestSong", EditConfigOperations.CREATE);
        ModelNode mySong = addChild(albumNode, modelNodeList, "MySong", EditConfigOperations.CREATE);
        ModelNode aSong = addChild(albumNode, modelNodeList, "aSong", EditConfigOperations.CREATE);
        ModelNode nextSong = addChild(albumNode, modelNodeList, "nextSong", EditConfigOperations.CREATE);

        modelNodeList = new ArrayList<>(new TreeSet<>(modelNodeList));
        List<ModelNode> expectedModelNodeList = new ArrayList<>();
        expectedModelNodeList.add(mySong);
        expectedModelNodeList.add(testSong);
        expectedModelNodeList.add(aSong);
        expectedModelNodeList.add(nextSong);

        assertEquals(expectedModelNodeList, modelNodeList);

    }
}
