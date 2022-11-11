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

import static org.broadband_forum.obbaa.netconf.api.util.DocumentUtils.createDocument;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildContainerHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeFactoryException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootEntityContainerModelNodeHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregatorImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.DataStoreIntegrityService;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.DataStoreValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.DataStoreValidatorImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.service.DataStoreIntegrityServiceImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DSExpressionValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.inmemory.InMemoryDSM;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.YangUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.restaurant.RestaurantConstants;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

@RunWith(RequestScopeJunitRunner.class)
public class GetStateLeafTest extends AbstractEditConfigTestSetup {

    private NetConfServerImpl m_server;

    public static final String MESSAGE_ID = "1";
    public static final String PRUNED_SUBTREE = "pruned-subtree";
    public static final String SUBTREE = "subtree";

    private SubSystemRegistry m_subSystemRegistry = new SubSystemRegistryImpl();

    private SchemaRegistry m_schemaRegistry = null;

    private RootModelNodeAggregator m_rootModelNodeAggregator;

    private ModelNodeId m_modelNodeId_Menu;
    private ModelNodeId m_modelNodeId_Table;

    private DataStore m_dataStore;

    private DataStoreValidator m_datastoreValidator;

    private ModelNodeDataStoreManager m_modelNodeDsm;

    private NbiNotificationHelper m_nbiNotificationHelper;
    private SubSystem m_subSystem;
    private DataStoreIntegrityService m_integrityService;
    private ModelNodeHelperRegistry m_modelNodeHelperRegistry = new ModelNodeHelperRegistryImpl(m_schemaRegistry);

    @Before
    public void initServer() throws SchemaBuildException, ModelNodeInitException, ModelNodeFactoryException, GetAttributeException {

        String yangFile = "/leaftest/example-restaurant-new.yang";
        String xmlFile = "/leaftest/example-restaurant-new-instance.xml";
        String yangFilePath = getClass().getResource(yangFile).getPath();
        String xmlFilePath = getClass().getResource(xmlFile).getPath();

        m_schemaRegistry = new SchemaRegistryImpl(Collections.<YangTextSchemaSource>emptyList(), Collections.emptySet(), Collections.emptyMap(), new NoLockService());
        m_schemaRegistry.loadSchemaContext("restaurant", Arrays.asList(TestUtil.getByteSource("/leaftest/example-restaurant-new.yang")), Collections.emptySet(), Collections.emptyMap());

        m_subSystem = new SubSystem();
        m_modelNodeDsm = new InMemoryDSM(m_schemaRegistry);
        m_rootModelNodeAggregator = new RootModelNodeAggregatorImpl(m_schemaRegistry, m_modelNodeHelperRegistry,
                mock(ModelNodeDataStoreManager.class), m_subSystemRegistry);
        m_server = new NetConfServerImpl(m_schemaRegistry);
        m_integrityService = new DataStoreIntegrityServiceImpl(m_server);
        m_expValidator = new DSExpressionValidator(m_schemaRegistry, m_modelNodeHelperRegistry, m_subSystemRegistry);
        m_datastoreValidator = new DataStoreValidatorImpl(m_schemaRegistry, m_modelNodeHelperRegistry, m_modelNodeDsm, m_integrityService, m_expValidator);

        YangUtils.deployInMemoryHelpers(yangFilePath, new SubSystem(), m_modelNodeHelperRegistry,
                m_subSystemRegistry, m_schemaRegistry, m_modelNodeDsm);
        ContainerSchemaNode schemaNode = (ContainerSchemaNode) m_schemaRegistry.getDataSchemaNode(RestaurantConstants.RESTAURANT_SCHEMA_PATH);
        ChildContainerHelper containerHelper = new RootEntityContainerModelNodeHelper(schemaNode,
                m_modelNodeHelperRegistry, m_subSystemRegistry, m_schemaRegistry, m_modelNodeDsm);
        m_subSystemRegistry.setCompositeSubSystem(new CompositeSubSystemImpl());
        m_rootModelNodeAggregator.addModelServiceRootHelper(RestaurantConstants.RESTAURANT_SCHEMA_PATH, containerHelper);
        m_dataStore = new DataStore(StandardDataStores.RUNNING, m_rootModelNodeAggregator, m_subSystemRegistry);
        m_dataStore.setValidator(m_datastoreValidator);
        m_nbiNotificationHelper = mock(NbiNotificationHelper.class);
        m_dataStore.setNbiNotificationHelper(m_nbiNotificationHelper);
        m_server.setRunningDataStore(m_dataStore);
        YangUtils.loadXmlDataIntoServer(m_server, xmlFilePath);
    }

    @Test
    public void testGetStateLeafWithMatchFilter() throws SAXException, IOException {
        String requestXml = "/leaftest/filter-match-node-count-viands.xml";
        TestUtil.verifyGet(m_server, requestXml, "/leaftest/filter-matchnodes-count-viand-response.xml", MESSAGE_ID, PRUNED_SUBTREE);
        TestUtil.verifyGet(m_server, requestXml, "/leaftest/filter-matchnodes-count-viand-response.xml", MESSAGE_ID, SUBTREE);
    }

    @Test
    public void testGetStateLeafWithMatchFilterEmptyResponse() throws SAXException, IOException {
        String requestXml = "/leaftest/filter-match-node-nomatch-count-viands.xml";
        TestUtil.verifyGet(m_server, requestXml, "/leaftest/filter-matchnodes-nomatch-count-viand-response.xml", MESSAGE_ID, PRUNED_SUBTREE);
    }

    @Test
    public void testGetStateLeafWithMatchFilterContainer() throws SAXException, IOException {
        String requestXml = "/leaftest/filter-match-node-containerLeaf-tableBooking.xml";
        TestUtil.verifyGet(m_server, requestXml, "/leaftest/filter-matchnodes-nomatch-bookingStatus-response.xml", MESSAGE_ID, PRUNED_SUBTREE);

        requestXml = "/leaftest/filter-match-node-containerLeaf-tableBooking-countViands.xml";
        TestUtil.verifyGet(m_server, requestXml, "/leaftest/filter-matchnodes-nomatch-bookingStatus-countViand-response.xml", MESSAGE_ID, PRUNED_SUBTREE);

    }

    private void populateCountViandStateValues(Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> stateAttributes, Map<ModelNodeId, List<Element>> stateAttributesValues) {
        Document document = createDocument();
        Element viandElement = document.createElementNS(RestaurantConstants.RESTAURANT_NS, "count-viands");
        viandElement.setTextContent("2");
        List<Element> elements = new ArrayList<>();
        elements.add(viandElement);
        stateAttributesValues.put(m_modelNodeId_Menu, elements);
   }

    private void populateBookingStatusStateValues(Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> stateAttributes, Map<ModelNodeId, List<Element>> stateAttributesValues) {
        Document document = createDocument();
        Element tableBookingElement = document.createElementNS(RestaurantConstants.RESTAURANT_NS, "bookingStatus");
        tableBookingElement.setTextContent("reserved");
        List<Element> tableElements = new ArrayList<>();
        tableElements.add(tableBookingElement);
        stateAttributesValues.put(m_modelNodeId_Table, tableElements);
    }


    private class SubSystem extends AbstractSubSystem {
        @Override
        protected boolean byPassAuthorization() {
            return true;
        }

        @Override
        public Map<ModelNodeId, List<Element>> retrieveStateAttributes(Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> mapAttributes) throws GetAttributeException {
            m_modelNodeId_Menu = new ModelNodeId("/container=restaurant/container=menu/name=breakfast", RestaurantConstants.RESTAURANT_NS);
            m_modelNodeId_Table = new ModelNodeId( "/container=restaurant/container=table", RestaurantConstants.RESTAURANT_NS);
            Map<ModelNodeId, List<Element>> stateInfo = new HashMap<>();
            Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> stateAttributes = new HashMap<>();
            List<QName> qNames = new ArrayList<>();
            for(Map.Entry<ModelNodeId, Pair<List<QName>, List<FilterNode>>> entry : mapAttributes.entrySet()) {
                ModelNodeId incomingMN = entry.getKey();
                if(incomingMN.equals(m_modelNodeId_Menu)) {
                    qNames.add(QName.create(RestaurantConstants.RESTAURANT_NS, RestaurantConstants.RESTAURANT_REVISION, "count-viands"));
                    stateAttributes.put(m_modelNodeId_Menu, new Pair<>(qNames, new ArrayList<>()));
                    populateCountViandStateValues(stateAttributes, stateInfo);
                } else if (incomingMN.equals(m_modelNodeId_Table)) {
                    qNames.add(QName.create(RestaurantConstants.RESTAURANT_NS, RestaurantConstants.RESTAURANT_REVISION, "bookingStatus"));
                    stateAttributes.put(m_modelNodeId_Table, new Pair<>(qNames, new ArrayList<>()));
                    populateBookingStatusStateValues(stateAttributes, stateInfo);
                }
            }
            return stateInfo;
        }
    }
}
