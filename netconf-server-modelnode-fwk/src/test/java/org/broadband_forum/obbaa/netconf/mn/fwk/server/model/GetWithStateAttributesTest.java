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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import static org.broadband_forum.obbaa.netconf.api.util.DocumentUtils.getNewDocument;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JB_NS;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.assertXMLEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.GetRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfFilter;
import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.api.server.NetconfQueryParams;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeFactoryException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregatorImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.inmemory.InMemoryDSM;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.YangUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.yang.DsmJukeboxSubsystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@RunWith(RequestScopeJunitRunner.class)
public class GetWithStateAttributesTest {

    private static final String EXAMPLE_JUKEBOX_WITH_STATE_ATTRIBUTES_YANG = "/getstateattributestest/example-jukebox-with-state-attributes.yang";
    private static final String EXAMPLE_JUKEBOX_XML = "/getstateattributestest/example-jukebox.xml";
    private static final String FULL_GET_RESPONSE_WITH_STATE_ATTRIBUTES_XML = "/getstateattributestest/full_get_response_with_state_attributes.xml";
    private static final String LIBRARY_ADMIN_XML = "/getstateattributestest/library-admin.xml";
    private static final String LIST_FAVOURITE_SONG_XML = "/getstateattributestest/list-favourite-song.xml";
    private static final String LEAFLIST_LANGUAGE_XML = "/getstateattributestest/leaf-list-language.xml";
    private static final String COMPONENT_ID = "jukebox";

    private SchemaRegistry m_schemaRegistry;
    private RootModelNodeAggregator m_rootModelNodeAggregator;
    private ModelNodeHelperRegistry m_modelNodeHelperRegistry;
    private SubSystemRegistry m_subSystemRegistry;
    private NetConfServerImpl m_server;
    private ModelNode m_jukeBoxModelNode;
    private DsmJukeboxSubsystem m_jukeBoxSystem;
    private NbiNotificationHelper m_nbiNotificationHelper;
    private List<String> m_listOfStateAttributes;

    @Before
    public void setUp() throws SchemaBuildException, ParserConfigurationException, GetAttributeException, ModelNodeInitException,
            ModelNodeFactoryException {

        String yangFilePath = GetWithStateContainerTest.class.getResource(EXAMPLE_JUKEBOX_WITH_STATE_ATTRIBUTES_YANG).getPath();
        String xmlFilePath = GetWithStateContainerTest.class.getResource(EXAMPLE_JUKEBOX_XML).getPath();
        m_schemaRegistry = new SchemaRegistryImpl(Collections.<YangTextSchemaSource> emptyList(), Collections.emptySet(), Collections.emptyMap(), new NoLockService());
        m_schemaRegistry.loadSchemaContext(COMPONENT_ID, Arrays.asList(TestUtil.getByteSource(EXAMPLE_JUKEBOX_WITH_STATE_ATTRIBUTES_YANG)), Collections.emptySet(), Collections.emptyMap());

        m_jukeBoxSystem = Mockito.mock(DsmJukeboxSubsystem.class);
        m_subSystemRegistry = new SubSystemRegistryImpl();
        m_subSystemRegistry.setCompositeSubSystem(new CompositeSubSystemImpl());
        m_modelNodeHelperRegistry = new ModelNodeHelperRegistryImpl(m_schemaRegistry);
        m_rootModelNodeAggregator = new RootModelNodeAggregatorImpl(m_schemaRegistry, m_modelNodeHelperRegistry,
                mock(ModelNodeDataStoreManager.class), m_subSystemRegistry);
        m_jukeBoxModelNode = YangUtils.createInMemoryModelNode(yangFilePath, m_jukeBoxSystem, m_modelNodeHelperRegistry,
                m_subSystemRegistry, m_schemaRegistry, new InMemoryDSM(m_schemaRegistry));
        m_rootModelNodeAggregator.addModelServiceRoot(COMPONENT_ID, m_jukeBoxModelNode);
        Map<ModelNodeId, List<Element>> values = mockSubSystemResponse();
        when(m_jukeBoxSystem.retrieveStateAttributes(Mockito.anyMap(), Mockito.any(NetconfQueryParams.class), Mockito.any(StateAttributeGetContext.class))).thenReturn(values);

        m_server = new NetConfServerImpl(m_schemaRegistry);
        m_nbiNotificationHelper = mock(NbiNotificationHelper.class);
        DataStore dataStore = new DataStore(StandardDataStores.RUNNING, m_rootModelNodeAggregator, m_subSystemRegistry);
        dataStore.setNbiNotificationHelper(m_nbiNotificationHelper);
        ModelNodeDataStoreManager modelNodeDsm = mock(ModelNodeDataStoreManager.class);
        m_server.setDataStore(StandardDataStores.RUNNING, dataStore);

        Module module = YangUtils.deployInMemoryHelpers(yangFilePath, m_jukeBoxSystem, m_modelNodeHelperRegistry, m_subSystemRegistry,
                m_schemaRegistry, modelNodeDsm);
        m_listOfStateAttributes = new ArrayList<>();
        createListOfStateAttributes(module.getChildNodes());

        YangUtils.loadXmlDataIntoServer(m_server, xmlFilePath);

    }

    @Test
    public void testGetStateAttributes() throws Exception {

        NetconfClientInfo client = new NetconfClientInfo("test", 1);
        GetRequest request = new GetRequest();
        request.setMessageId("1");
        request.setFilter((NetconfFilter) null);
        NetConfResponse response = new NetConfResponse();
        response.setMessageId("1");
        m_server.onGet(client, request, response);

        for (String stateAttribute : m_listOfStateAttributes)
            assertEquals(stateAttribute, DocumentUtils.getChildNodeByName(response.getData(), stateAttribute, JB_NS).getLocalName());

        assertXMLEquals(FULL_GET_RESPONSE_WITH_STATE_ATTRIBUTES_XML, response);
    }

    private List<String> createListOfStateAttributes(Collection<DataSchemaNode> dataSchemaNodes) {

        for (DataSchemaNode dataSchemaNode : dataSchemaNodes) {
            if (dataSchemaNode instanceof ContainerSchemaNode) {
                ContainerSchemaNode containerNode = (ContainerSchemaNode) dataSchemaNode;
                if (!containerNode.isConfiguration()) {
                    m_listOfStateAttributes.add(containerNode.getQName().getLocalName());
                    return m_listOfStateAttributes;
                } else {
                    createListOfStateAttributes(containerNode.getChildNodes());
                }
            } else if (dataSchemaNode instanceof LeafSchemaNode) {
                LeafSchemaNode leafSchemaNode = (LeafSchemaNode) dataSchemaNode;
                if (!leafSchemaNode.isConfiguration()) {
                    m_listOfStateAttributes.add(leafSchemaNode.getQName().getLocalName());
                }
            } else if (dataSchemaNode instanceof ListSchemaNode) {
                ListSchemaNode listSchemaNode = (ListSchemaNode) dataSchemaNode;
                if (!listSchemaNode.isConfiguration()) {
                    m_listOfStateAttributes.add(listSchemaNode.getQName().getLocalName());
                } else {
                    createListOfStateAttributes(listSchemaNode.getChildNodes());
                }
            } else if (dataSchemaNode instanceof LeafListSchemaNode) {
                LeafListSchemaNode leafListSchemaNode = (LeafListSchemaNode) dataSchemaNode;
                if (!leafListSchemaNode.isConfiguration()) {
                    m_listOfStateAttributes.add(leafListSchemaNode.getQName().getLocalName());
                }
            }
        }
        return m_listOfStateAttributes;
    }

    private Map<ModelNodeId, List<Element>> mockSubSystemResponse() throws ParserConfigurationException {

        Map<ModelNodeId, List<Element>> subSystemResponse = new HashMap<>();
        
        Document newDocument = getNewDocument();
        Element albumCount = newDocument.createElementNS(JB_NS, "album-count");
        albumCount.setTextContent("1");

        Element artistCount = newDocument.createElementNS(JB_NS, "artist-count");
        artistCount.setTextContent("2");

        Element songCount = newDocument.createElementNS(JB_NS, "song-count");
        songCount.setTextContent("5");

        Element favouriteSong = TestUtil.loadAsXml(LIST_FAVOURITE_SONG_XML);

        Element language = TestUtil.loadAsXml(LEAFLIST_LANGUAGE_XML);

        Element adminElement = TestUtil.loadAsXml(LIBRARY_ADMIN_XML);

        List<Element> elements = new ArrayList<>();
        elements.add(albumCount);
        elements.add(artistCount);
        elements.add(songCount);
        elements.add(adminElement);
        elements.add(favouriteSong);
        elements.add(language);

        subSystemResponse.put(new ModelNodeId("/container=jukebox/container=library", JukeboxConstants.JB_NS), elements);

        return subSystemResponse;
    }
}
