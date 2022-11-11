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
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ALBUM_ORDERED_BY_USER_SCHEMA_PATH;
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.SchemaPathUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryTraverser;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryVisitor;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.DataStoreException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDSMRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDSMRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeKey;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.utils.AnnotationAnalysisException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.utils.EntityRegistryBuilder;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.GenericConfigAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.IdentityRefConfigAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.InstanceIdentifierConfigAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.artistsubtreewithconfigattr.Artist;
import org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.artistsubtreewithconfigattr.Jukebox;
import org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.artistsubtreewithconfigattr.Library;
import org.broadband_forum.obbaa.netconf.persistence.EntityDataStoreManager;
import org.broadband_forum.obbaa.netconf.persistence.PersistenceManagerUtil;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.xml.sax.SAXException;

/**
 * Created by sgs on 2/1/17.
 */
@RunWith(RequestScopeJunitRunner.class)
public class ConfigAttributeXmlSubtreeDSMTest {

    public static final String TEST_APPLICATION_CONTEXT_XML = "/configattributetest/test-applicationContext.xml";
    public static final String JUKEBOX_TYPES_NS = "http://example.com/ns/example-jukebox-types";
    public static final String JB_TYPES_PREFIX = "ejt";
    public static final String XMLNS_JBOX = "xmlns:jbox";
    public static final String XMLNS_EJT = "xmlns:ejt";
    public static final String ARTIST_NAME = "Artist";
    public static final String ALBUM_NAME = "album";

    public static final QName DUMMY_LEAFLIST_IDREF_QNAME = QName.create(JB_NS,JB_REVISION,"dummy-leaflist-id-ref");

    private XmlDSMCache m_dsmCache = mock(XmlDSMCache.class);
    private AbstractApplicationContext m_context;
    private AggregatedDSM m_aggregatedDSM;
    private EntityRegistry m_entityRegistry;
    private PersistenceManagerUtil m_persistenceManagerUtil;
    private EntityModelNodeHelperDeployer m_entityModelNodeHelperDeployer;
    private XmlModelNodeToXmlMapper m_xmlModelNodeToXmlMapper;
    private SchemaRegistry m_schemaRegistry;
    private ModelNodeHelperRegistry m_modelNodeHelperRegistry;
    private SubSystemRegistry m_subSystemRegistry;
    private ModelNodeDataStoreManager m_xmlSubtreeDSM;

    private ModelNodeId m_jukeboxNodeId = new ModelNodeId().addRdn(new ModelNodeRdn(CONTAINER, JB_NS, JUKEBOX_LOCAL_NAME));
    private ModelNodeId m_libraryNodeId = new ModelNodeId(m_jukeboxNodeId).addRdn(new ModelNodeRdn(CONTAINER, JB_NS, LIBRARY_LOCAL_NAME));
    private ModelNodeId m_artistNodeId = new ModelNodeId(m_libraryNodeId).addRdn(new ModelNodeRdn(CONTAINER, JB_NS, ARTIST_LOCAL_NAME))
            .addRdn(new ModelNodeRdn(NAME_QNAME, ARTIST_NAME));

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
        m_xmlSubtreeDSM = (ModelNodeDataStoreManager) m_context.getBean("xmlSubtreeDSM");

        m_modelNodeHelperRegistry = (ModelNodeHelperRegistry) m_context.getBean("modelNodeHelperRegistry");
        m_xmlModelNodeToXmlMapper = new XmlModelNodeToXmlMapperImpl(m_dsmCache, m_schemaRegistry, m_modelNodeHelperRegistry, m_subSystemRegistry,
                m_entityRegistry, null);
    }

    @Test
    public void testDSMReads() throws AnnotationAnalysisException, DataStoreException, ParserConfigurationException, SAXException, IOException {

        initialiseJukeboxEntity();

        assertEquals(1, m_aggregatedDSM.listChildNodes(JUKEBOX_SCHEMA_PATH, EMPTY_NODE_ID, m_schemaRegistry).size());
        assertEquals(1, m_aggregatedDSM.listChildNodes(LIBRARY_SCHEMA_PATH, m_jukeboxNodeId, m_schemaRegistry).size());
        assertEquals(1, m_aggregatedDSM.listChildNodes(ARTIST_SCHEMA_PATH,m_libraryNodeId, m_schemaRegistry).size());
        assertEquals(1, m_aggregatedDSM.listChildNodes(ALBUM_SCHEMA_PATH,m_artistNodeId, m_schemaRegistry).size());

        assertFalse(m_aggregatedDSM.listChildNodes(ARTIST_SCHEMA_PATH,m_libraryNodeId, m_schemaRegistry).get(0).isVisible());

        Map<QName, String> keys = new LinkedHashMap<>();
        keys.put(NAME_QNAME,ALBUM_NAME);
        ModelNodeKey modelNodeKey = new ModelNodeKey(keys);
        ModelNodeWithAttributes albumModelNode = (ModelNodeWithAttributes) m_aggregatedDSM.findNode(ALBUM_SCHEMA_PATH,modelNodeKey,m_artistNodeId, m_schemaRegistry);

        verifyAlbumAttributes(albumModelNode);

        // Verify findNodes with match criteria work
        HashMap<QName, ConfigLeafAttribute> matchAttributes = new HashMap<>();
        matchAttributes.put(YEAR_QNAME, new GenericConfigAttribute(YEAR, JB_NS, "1948"));
        matchAttributes.put(GENRE_QNAME, albumModelNode.getAttribute(GENRE_QNAME));

        albumModelNode = (ModelNodeWithAttributes) m_aggregatedDSM.findNodes(ALBUM_SCHEMA_PATH,matchAttributes,m_artistNodeId, m_schemaRegistry).get(0);
        verifyAlbumAttributes(albumModelNode);

        //Verify findNode with match criteria works with just key
        matchAttributes = new HashMap<>();
        matchAttributes.put(NAME_QNAME, new GenericConfigAttribute(NAME, JB_NS, ALBUM_NAME));

        albumModelNode = (ModelNodeWithAttributes) m_aggregatedDSM.findNodes(ALBUM_SCHEMA_PATH,matchAttributes,m_artistNodeId, m_schemaRegistry).get(0);
        verifyAlbumAttributes(albumModelNode);

        //Verify findNode with match criteria works with leaf-list value
        matchAttributes = new HashMap<>();
        matchAttributes.put(DUMMY_LEAFLIST_IDREF_QNAME, new IdentityRefConfigAttribute(JB_NS,"jbox","jbox:dummy-leaflist-id-ref",
                "jbox:dummy-id-ref3",JB_NS));

        albumModelNode = (ModelNodeWithAttributes) m_aggregatedDSM.findNodes(ALBUM_SCHEMA_PATH,matchAttributes,m_artistNodeId, m_schemaRegistry).get(0);
        verifyAlbumAttributes(albumModelNode);

    }

    @Test
    public void testDSMWrites() throws DataStoreException, AnnotationAnalysisException, ParserConfigurationException, SAXException,
            IOException {
        initialiseJukeboxEntity();
        assertEquals(1, m_aggregatedDSM.listNodes(JUKEBOX_SCHEMA_PATH, m_schemaRegistry).size());
        assertEquals(1, m_aggregatedDSM.listNodes(LIBRARY_SCHEMA_PATH, m_schemaRegistry).size());
        try {
            m_aggregatedDSM.listNodes(ARTIST_SCHEMA_PATH, m_schemaRegistry);
            fail("Expected an exception");
        }catch (Exception e){
            //expected
            assertEquals("Stored schema node is a list node, hence listNodes API will not work here, use findNodes API instead", e.getCause().getCause().getMessage());
        }
        assertFalse(m_aggregatedDSM.findNodes(ARTIST_SCHEMA_PATH, Collections.emptyMap(), m_libraryNodeId, m_schemaRegistry).get(0).isVisible());
        assertEquals(1, m_aggregatedDSM.listChildNodes(ALBUM_SCHEMA_PATH,m_artistNodeId, m_schemaRegistry).size());

        Map<QName, String> keys = new LinkedHashMap<>();
        keys.put(NAME_QNAME,ARTIST_NAME);
        ModelNodeKey modelNodeKey = new ModelNodeKey(keys);
        XmlModelNodeImpl artistModelNode = (XmlModelNodeImpl) m_aggregatedDSM.findNode(ARTIST_SCHEMA_PATH,modelNodeKey,m_libraryNodeId, m_schemaRegistry);
        assertFalse(artistModelNode.isVisible());

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

        Map<QName,LinkedHashSet<ConfigLeafAttribute>> leafListAttributesMap = new HashMap<>();
        LinkedHashSet<ConfigLeafAttribute> singerLeafList = new LinkedHashSet<>();
        singerLeafList.add(new GenericConfigAttribute(SINGER_LOCAL_NAME, JB_NS, "singer3"));
        leafListAttributesMap.put(SINGER_QNAME, singerLeafList);

        LinkedHashSet<ConfigLeafAttribute> dummyleafList = new LinkedHashSet<>();
        dummyleafList.add(new IdentityRefConfigAttribute(JB_NS,"jbox","jbox:dummy-leaflist-id-ref",
                "jbox:dummy-id-ref3",JB_NS));
        leafListAttributesMap.put(DUMMY_LEAFLIST_IDREF_QNAME,dummyleafList);

        ModelNodeWithAttributes albumToBeCreated = new XmlModelNodeImpl(DocumentUtils.createDocument(), ALBUM_SCHEMA_PATH,configAttributes,Collections.emptyList(),artistModelNode,
                m_artistNodeId,m_xmlModelNodeToXmlMapper,m_modelNodeHelperRegistry,m_schemaRegistry, m_subSystemRegistry,m_xmlSubtreeDSM, null, true, null);
        albumToBeCreated.setLeafLists(leafListAttributesMap);
        m_xmlSubtreeDSM.createNode(albumToBeCreated, m_artistNodeId);

        // find the created modelnode and make assertions
        keys = new LinkedHashMap<>();
        keys.put(NAME_QNAME,"newalbum");
        modelNodeKey = new ModelNodeKey(keys);
        ModelNodeWithAttributes newAlbumModelNode = (ModelNodeWithAttributes) m_aggregatedDSM.findNode(ALBUM_SCHEMA_PATH,modelNodeKey,m_artistNodeId, m_schemaRegistry);

        assertEquals("newalbum", newAlbumModelNode.getAttribute(NAME_QNAME).getStringValue());
        assertEquals(JB_NS,getNodeValue(newAlbumModelNode.getAttribute(GENRE_QNAME), XMLNS_JBOX));
        assertEquals("jbox:jazz",newAlbumModelNode.getAttribute(GENRE_QNAME).getStringValue());
        assertEquals("jbox:jukebox/ejt:dummyleaf",newAlbumModelNode.getAttribute(RESOURCE_QNAME).getStringValue());
        assertEquals(JB_NS,getNodeValue(newAlbumModelNode.getAttribute(RESOURCE_QNAME), XMLNS_JBOX));
        assertEquals(JUKEBOX_TYPES_NS,getNodeValue(newAlbumModelNode.getAttribute(RESOURCE_QNAME), XMLNS_EJT));
        assertEquals(2, newAlbumModelNode.getLeafLists().size());
        assertEquals(1, newAlbumModelNode.getLeafList(SINGER_QNAME).size());
        assertEquals(1, newAlbumModelNode.getLeafList(DUMMY_LEAFLIST_IDREF_QNAME).size());
        assertEquals(2, m_aggregatedDSM.listChildNodes(ALBUM_SCHEMA_PATH,m_artistNodeId, m_schemaRegistry).size());

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
        m_xmlSubtreeDSM.updateNode(newAlbumModelNode,m_artistNodeId,configAttributes, leafListAttributesMap, false);

        // find the updated modelnode and make assertions
        ModelNodeWithAttributes updatedAlbumNode = (ModelNodeWithAttributes) m_aggregatedDSM.findNode(ALBUM_SCHEMA_PATH,modelNodeKey,
                m_artistNodeId, m_schemaRegistry);

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
        assertEquals(1, m_aggregatedDSM.listNodes(JUKEBOX_SCHEMA_PATH, m_schemaRegistry).size());
        assertEquals(1, m_aggregatedDSM.listNodes(LIBRARY_SCHEMA_PATH, m_schemaRegistry).size());
        try{
            m_aggregatedDSM.listNodes(ARTIST_SCHEMA_PATH, m_schemaRegistry);
            fail("Expected an exception");
        }catch (Exception e){
            //expected
            assertEquals("Stored schema node is a list node, hence listNodes API will not work here, use findNodes API instead", e.getCause().getCause().getMessage());
        }
        assertFalse(m_aggregatedDSM.findNodes(ARTIST_SCHEMA_PATH, Collections.emptyMap(), m_libraryNodeId, m_schemaRegistry).get(0).isVisible());
        assertEquals(2, m_aggregatedDSM.listChildNodes(ALBUM_SCHEMA_PATH,m_artistNodeId, m_schemaRegistry).size());

        keys = new LinkedHashMap<>();
        keys.put(NAME_QNAME,ARTIST_NAME);
        modelNodeKey = new ModelNodeKey(keys);
        artistModelNode = (XmlModelNodeImpl) m_aggregatedDSM.findNode(ARTIST_SCHEMA_PATH,modelNodeKey,m_libraryNodeId, m_schemaRegistry);
        assertFalse(artistModelNode.isVisible());
        m_aggregatedDSM.removeAllNodes(artistModelNode,ALBUM_SCHEMA_PATH,m_libraryNodeId);

        assertEquals(1, m_aggregatedDSM.listNodes(JUKEBOX_SCHEMA_PATH, m_schemaRegistry).size());
        assertEquals(1, m_aggregatedDSM.listNodes(LIBRARY_SCHEMA_PATH, m_schemaRegistry).size());
        try{
            m_aggregatedDSM.listNodes(ARTIST_SCHEMA_PATH, m_schemaRegistry);
            fail("Expected an exception");
        }catch (Exception e){
            //expected
            assertEquals("Stored schema node is a list node, hence listNodes API will not work here, use findNodes API instead", e.getCause().getCause().getMessage());
        }
        assertFalse(m_aggregatedDSM.findNodes(ARTIST_SCHEMA_PATH, Collections.emptyMap(), m_libraryNodeId, m_schemaRegistry).get(0).isVisible());
        assertEquals(0, m_aggregatedDSM.listChildNodes(ALBUM_SCHEMA_PATH,m_artistNodeId, m_schemaRegistry).size());

        // remove
        m_aggregatedDSM.removeNode(artistModelNode,m_libraryNodeId);
        try{
            m_aggregatedDSM.listNodes(ARTIST_SCHEMA_PATH, m_schemaRegistry);
            fail("Expected an exception");
        }catch (Exception e){
            //expected
            assertEquals("Stored schema node is a list node, hence listNodes API will not work here, use findNodes API instead", e.getCause().getCause().getMessage());
        }
        ModelNodeWithAttributes libraryModelNode = (ModelNodeWithAttributes) m_aggregatedDSM.findNode(LIBRARY_SCHEMA_PATH,new
                ModelNodeKey(new LinkedHashMap<>()),m_jukeboxNodeId, m_schemaRegistry);
        m_aggregatedDSM.removeNode(libraryModelNode,m_jukeboxNodeId);
        assertEquals(0, m_aggregatedDSM.listNodes(LIBRARY_SCHEMA_PATH, m_schemaRegistry).size());
    }

    @Test
    public void testUpdateIndex() throws DataStoreException, AnnotationAnalysisException {
        initialiseJukeboxEntityForOrderedByData();
        assertEquals(1, m_aggregatedDSM.listNodes(JUKEBOX_SCHEMA_PATH, m_schemaRegistry).size());
        assertEquals(1, m_aggregatedDSM.listNodes(LIBRARY_SCHEMA_PATH, m_schemaRegistry).size());
        try {
            m_aggregatedDSM.listNodes(ARTIST_SCHEMA_PATH, m_schemaRegistry);
            fail("Expected an exception");
        }catch (Exception e){
            //expected
            assertEquals("Stored schema node is a list node, hence listNodes API will not work here, use findNodes API instead", e.getCause().getCause().getMessage());
        }
        assertEquals(0, m_aggregatedDSM.listChildNodes(ALBUM_ORDERED_BY_USER_SCHEMA_PATH, m_artistNodeId, m_schemaRegistry).size());

        Map<QName, String> keys = new LinkedHashMap<>();
        keys.put(NAME_QNAME, ARTIST_NAME);
        ModelNodeKey modelNodeKey = new ModelNodeKey(keys);
        XmlModelNodeImpl artistModelNode = (XmlModelNodeImpl) m_aggregatedDSM.findNode(ARTIST_SCHEMA_PATH, modelNodeKey,m_libraryNodeId, m_schemaRegistry);

        ModelNodeWithAttributes albumA = getAlbumWithAttributes(artistModelNode, "AlbumA", "2000");
        m_xmlSubtreeDSM.createNode(albumA, m_artistNodeId, 0);
        ModelNodeWithAttributes albumB = getAlbumWithAttributes(artistModelNode, "AlbumB", "2002");
        m_xmlSubtreeDSM.createNode(albumB, m_artistNodeId, 1);
        ModelNodeWithAttributes albumC = getAlbumWithAttributes(artistModelNode, "AlbumC", "2004");
        m_xmlSubtreeDSM.createNode(albumC, m_artistNodeId, 2);
        ModelNodeWithAttributes albumD = getAlbumWithAttributes(artistModelNode, "AlbumD", "2006");
        m_xmlSubtreeDSM.createNode(albumD, m_artistNodeId, 3);
        ModelNodeWithAttributes albumE = getAlbumWithAttributes(artistModelNode, "AlbumE", "2008");
        m_xmlSubtreeDSM.createNode(albumE, m_artistNodeId, 4);

        List<String> albumNames = Arrays.asList("AlbumA", "AlbumB", "AlbumC", "AlbumD", "AlbumE");
        List<String> years = Arrays.asList("2000", "2002", "2004", "2006", "2008");
        List storedAlbums = m_aggregatedDSM.findNodes(ALBUM_ORDERED_BY_USER_SCHEMA_PATH, Collections.emptyMap(), m_artistNodeId, m_schemaRegistry);
        assertStoredAlbums(storedAlbums, 5 , albumNames, years);

        //updating index to higher position
        m_xmlSubtreeDSM.updateIndex(albumB, m_artistNodeId, 3);
        albumNames = Arrays.asList("AlbumA", "AlbumC", "AlbumD", "AlbumB", "AlbumE");
        years = Arrays.asList("2000", "2004", "2006", "2002", "2008");
        storedAlbums = m_aggregatedDSM.findNodes(ALBUM_ORDERED_BY_USER_SCHEMA_PATH, Collections.emptyMap(), m_artistNodeId, m_schemaRegistry);
        assertStoredAlbums(storedAlbums, 5 , albumNames, years);

        //updating index to first position
        m_xmlSubtreeDSM.updateIndex(albumD, m_artistNodeId, 0);
        albumNames = Arrays.asList("AlbumD", "AlbumA", "AlbumC", "AlbumB", "AlbumE");
        years = Arrays.asList("2006", "2000", "2004", "2002", "2008");
        storedAlbums = m_aggregatedDSM.findNodes(ALBUM_ORDERED_BY_USER_SCHEMA_PATH, Collections.emptyMap(), m_artistNodeId, m_schemaRegistry);
        assertStoredAlbums(storedAlbums, 5 , albumNames, years);

        //updating index to lower position
        m_xmlSubtreeDSM.updateIndex(albumE, m_artistNodeId, 1);
        albumNames = Arrays.asList("AlbumD", "AlbumE", "AlbumA", "AlbumC", "AlbumB");
        years = Arrays.asList("2006", "2008", "2000", "2004", "2002");
        storedAlbums = m_aggregatedDSM.findNodes(ALBUM_ORDERED_BY_USER_SCHEMA_PATH, Collections.emptyMap(), m_artistNodeId, m_schemaRegistry);
        assertStoredAlbums(storedAlbums, 5 , albumNames, years);

        //updating index to last position
        m_xmlSubtreeDSM.updateIndex(albumA, m_artistNodeId, 4);
        albumNames = Arrays.asList("AlbumD", "AlbumE", "AlbumC", "AlbumB", "AlbumA");
        years = Arrays.asList("2006", "2008", "2004", "2002", "2000");
        storedAlbums = m_aggregatedDSM.findNodes(ALBUM_ORDERED_BY_USER_SCHEMA_PATH, Collections.emptyMap(), m_artistNodeId, m_schemaRegistry);
        assertStoredAlbums(storedAlbums, 5 , albumNames, years);

        //updating index with invalid value
        testUpdateIndexWithInvalidValue(albumA, -1);
        testUpdateIndexWithInvalidValue(albumA, 10);
    }

    private void testUpdateIndexWithInvalidValue(ModelNodeWithAttributes albumA, int newIndex) {
        try {
            m_xmlSubtreeDSM.updateIndex(albumA, m_artistNodeId, newIndex);
            fail("Expected an exception");
        } catch (Exception e) {
            assertEquals("Specified index is invalid", e.getCause().getCause().getMessage());
            assertTrue(e.getCause().getCause() instanceof DataStoreException);
        }
    }

    private void assertStoredAlbums(List<ModelNode> storedAlbums, int count, List<String> names, List<String> year) {
        assertEquals(count, storedAlbums.size());
        assertEquals(count, names.size());
        assertEquals(count, year.size());
        for(int i=0; i < count; i++) {
            Map<QName, ConfigLeafAttribute> attributes = ((ModelNodeWithAttributes) storedAlbums.get(i)).getAttributes();
            assertEquals(names.get(i), attributes.get(NAME_QNAME).getStringValue());
            assertEquals(year.get(i), attributes.get(YEAR_QNAME).getStringValue());
        }
    }

    private ModelNodeWithAttributes getAlbumWithAttributes(XmlModelNodeImpl artistModelNode, String albumName, String year) {
        Map<QName, ConfigLeafAttribute> configAttributes = new HashMap<>();
        configAttributes.put(NAME_QNAME, new GenericConfigAttribute(NAME, JB_NS, albumName));
        configAttributes.put(YEAR_QNAME, new GenericConfigAttribute(YEAR, JB_NS, year));

        return new XmlModelNodeImpl(DocumentUtils.createDocument(), ALBUM_ORDERED_BY_USER_SCHEMA_PATH,configAttributes, Collections.emptyList(), artistModelNode,
                m_artistNodeId,m_xmlModelNodeToXmlMapper,m_modelNodeHelperRegistry,m_schemaRegistry, m_subSystemRegistry,m_xmlSubtreeDSM, null, true, null);
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
        EntityRegistryBuilder.updateEntityRegistry(JUKEBOX_LOCAL_NAME, classes, m_entityRegistry, m_schemaRegistry, m_persistenceManagerUtil.getEntityDataStoreManager(), new ModelNodeDSMRegistryImpl());
        ModelNodeDSMRegistry modelNodeDSMRegistry = (ModelNodeDSMRegistry) m_context.getBean("modelNodeDSMRegistry");
        modelNodeDSMRegistry.register(JUKEBOX_LOCAL_NAME, JUKEBOX_SCHEMA_PATH, m_xmlSubtreeDSM);
        modelNodeDSMRegistry.register(JUKEBOX_LOCAL_NAME, LIBRARY_SCHEMA_PATH, m_xmlSubtreeDSM);
        modelNodeDSMRegistry.register(JUKEBOX_LOCAL_NAME, ARTIST_SCHEMA_PATH, m_xmlSubtreeDSM);
        modelNodeDSMRegistry.register(JUKEBOX_LOCAL_NAME, ALBUM_SCHEMA_PATH, m_xmlSubtreeDSM);
        createJukebox();
    }

    private void initialiseJukeboxEntityForOrderedByData() throws AnnotationAnalysisException {
        List<Class> classes = new ArrayList<>();
        classes.add(Jukebox.class);
        EntityRegistryBuilder.updateEntityRegistry(JUKEBOX_LOCAL_NAME, classes, m_entityRegistry, m_schemaRegistry, m_persistenceManagerUtil.getEntityDataStoreManager(), new ModelNodeDSMRegistryImpl());
        ModelNodeDSMRegistry modelNodeDSMRegistry = (ModelNodeDSMRegistry) m_context.getBean("modelNodeDSMRegistry");
        modelNodeDSMRegistry.register(JUKEBOX_LOCAL_NAME, JUKEBOX_SCHEMA_PATH, m_xmlSubtreeDSM);
        modelNodeDSMRegistry.register(JUKEBOX_LOCAL_NAME, LIBRARY_SCHEMA_PATH, m_xmlSubtreeDSM);
        modelNodeDSMRegistry.register(JUKEBOX_LOCAL_NAME, ARTIST_SCHEMA_PATH, m_xmlSubtreeDSM);
        modelNodeDSMRegistry.register(JUKEBOX_LOCAL_NAME, ALBUM_ORDERED_BY_USER_SCHEMA_PATH, m_xmlSubtreeDSM);
        createJukeboxForOrderedByData();
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
        artist.setXmlSubtree("<artist xmlns=\"http://example.com/ns/example-jukebox\">\n" +
                "    <album>\n" +
                "        <name>album</name>\n" +
                "        <year>1948</year>\n" +
                "        <genre xmlns:ejt=\"http://example.com/ns/example-jukebox-types\">ejt:blues</genre>\n" +
                "        <label xmlns:jbox=\"http://example.com/ns/example-jukebox\">jbox:indie</label>\n" +
                "        <resource xmlns:ejt=\"http://example" +
                ".com/ns/example-jukebox-types\">ejt:dummycontainer/ejt:dummyleaf</resource>\n" +
                "        <singer>singer1</singer>\n" +
                "        <singer>singer2</singer>\n" +
                "\t\t<dummy-leaflist-id-ref xmlns:ejt=\"http://example" +
                ".com/ns/example-jukebox-types\">ejt:dummy-id-ref1</dummy-leaflist-id-ref>\n" +
                "\t\t<dummy-leaflist-id-ref xmlns:jbox=\"http://example" +
                ".com/ns/example-jukebox\">jbox:dummy-id-ref3</dummy-leaflist-id-ref>\n" +
                "    </album>\n" +
                "</artist>");
        artist.setVisibility(false);

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

    private void createJukeboxForOrderedByData() {
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

    private String getNodeValue(ConfigLeafAttribute configLeafAttribute,String prefix) {
        return configLeafAttribute.getDOMValue().getAttributes().getNamedItem(prefix).getNodeValue();
    }
}