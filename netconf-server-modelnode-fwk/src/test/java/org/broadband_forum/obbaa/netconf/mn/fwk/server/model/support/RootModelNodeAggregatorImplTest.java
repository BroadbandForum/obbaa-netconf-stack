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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support;

import static junit.framework.TestCase.assertFalse;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.load;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.loadAsXml;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.responseToString;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.verifyGet;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.verifyGetConfig;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.CopyConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigElement;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigErrorOptions;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigTestOptions;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfFilter;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ChangeNotification;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.DataStore;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditConfigException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NbiNotificationHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NetConfServerImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemValidationException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.inmemory.InMemoryDSM;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.LocalSubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.YangUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.NetconfRpcErrorUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.yang.DsmJukeboxSubsystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.tester.TesterConstants;
import org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.toaster.ToasterConstants;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcPayloadConstraintParser;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class RootModelNodeAggregatorImplTest {
    private static final String FULL_GET_RESPONSE = "/rootmodelnodetest/full_get_response.xml";

    private static final String JUKEBOX_FILTER = "/rootmodelnodetest/jukebox_filter.xml";
    private static final String JUKEBOX_FILTER_RESPONSE = "/rootmodelnodetest/jukebox_filter_response.xml";

    private static final String TOASTER_FILTER = "/rootmodelnodetest/toaster_filter.xml";
    private static final String TOASTER_FILTER_RESPONSE = "/rootmodelnodetest/toaster_filter_response.xml";

    private static final String JUKEBOX_AND_TOASTER_FILTER_RESPONSE =
            "/rootmodelnodetest/jukebox_and_toaster_filter_response.xml";

    private static final String INVALID_FILTER = "/rootmodelnodetest/invalid_filter.xml";
    private static final String INVALID_FILTER_RESPONSE = "/rootmodelnodetest/invalid_filter_response.xml";

    private static final String FULL_GET_CONFIG_RESPONSE = "/rootmodelnodetest/full_get_config_response.xml";
    private static final String FULL_GET_CONFIG_RESPONSE_AFTER_DO_COPY_CONFIG =
            "/rootmodelnodetest/full_get_config_response_after_copy_config_tester.xml";

    private static final String EDIT_ON_JUKEBOX = "/rootmodelnodetest/edit_on_jukebox.xml";
    private static final String RESPONSE_AFTER_EDIT_ON_JUKEBOX = "/rootmodelnodetest/response_after_edit_on_jukebox" +
            ".xml";

    private static final String EDIT_ON_JUKEBOX_WITH_NS_PREFIX = "/rootmodelnodetest/edit_on_jukebox_with_ns_prefix" +
            ".xml";
    private static final String RESPONSE_AFTER_EDIT_ON_JUKEBOX_WITH_NS_PREFIX =
            "/rootmodelnodetest/response_after_edit_on_jukebox_with_ns_prefix.xml";

    private static final String EDIT_ON_TOASTER = "/rootmodelnodetest/edit_on_toaster.xml";
    ;
    private static final String RESPONSE_AFTER_EDIT_ON_TOASTER = "/rootmodelnodetest/response_after_edit_on_toaster" +
            ".xml";

    private static final String RESPONSE_AFTER_EDIT_ON_BOTH_MODELS =
            "/rootmodelnodetest/response_after_edit_on_both_models.xml";

    private static final String INVALID_EDIT1 = "/rootmodelnodetest/invalid_edit1.xml";

    private static final String COPY_CONFIG1_XML = "/rootmodelnodetest/copy-config1.xml";
    private static final String INVALID_COPY_CONFIG1_XML = "/rootmodelnodetest/invalid-copy-config1.xml";
    private static final String INVALID_COPY_CONFIG2_XML = "/rootmodelnodetest/invalid-copy-config2.xml";
    private static final String ROOT_CONFIG_CONTAINER_CREATE = "/rootmodelnodetest/root-config-container-create.xml";
    private static final String RESPONSE_AFTER_ROOT_CONFIG_CONTAINER_CREATE =
            "/rootmodelnodetest/response_after_root_config_container_create.xml";
    private static final String ROOT_CITY_LIST_CREATE = "/rootmodelnodetest/root-city-list-create.xml";
    private static final String ROOT_CITY_LIST_CREATE2 = "/rootmodelnodetest/root-city-list-create2.xml";
    private static final String RESPONSE_AFTER_ROOT_CITY_LIST_CREATE =
            "/rootmodelnodetest/response_after_root_city_list_create.xml";
    private static final String RESPONSE_AFTER_ROOT_CITY_LIST_CREATE2 =
            "/rootmodelnodetest/response_after_root_city_list_create2.xml";
    private static final String ROOT_CONFIG_CONTAINER_REPLACE = "/rootmodelnodetest/root-config-container-replace.xml";
    private static final String ROOT_CITY_LIST_REPLACE = "/rootmodelnodetest/root-city-list-replace.xml";
    private static final String ROOT_CITY_LIST_REPLACE2 = "/rootmodelnodetest/root-city-list-replace2.xml";

    private static final String WA_NS = "http://www.test-company.com/solutions/anv-weather-app";
    private static final String WA_REVISION = "2016-04-25";
    private static final String WA_CONFIG_LOCALNAME = "config";

    private static final SchemaPath WA_CONFIG_SCHEMAPATH = SchemaPath.create(true, QName.create(WA_NS, WA_REVISION,
            WA_CONFIG_LOCALNAME));
    private static final String WA_CITY_LOCALNAME = "city";
    private static final SchemaPath WA_CITY_SCHEMAPATH = SchemaPath.create(true, QName.create(WA_NS, WA_REVISION,
            WA_CITY_LOCALNAME));
    public static final String ROOT_CONTAINER = "root-container";
    public static final SchemaPath WA_ROOT_CONTAINER_SCHEMAPATH = SchemaPath.create(true, QName.create(WA_NS,
            WA_REVISION, ROOT_CONTAINER));

    private static final String FULL_GET_CONFIG_RESPONSE_TESTER = "/rootmodelnodetest/full_get_config_response_tester" +
            ".xml";
    private static final String FULL_GET_RESPONSE_TESTER = "/rootmodelnodetest/full_get_response_tester.xml";

    private static final String COPY_CONFIG_ONE_ROOTCONTAINER_XML =
            "/rootmodelnodetest/copy-config-with-one-root-container.xml";
    private static final String GET_CONFIG_RESP_AFTER_COPY_CONFIG_ROOTCONT =
            "/rootmodelnodetest/get_config_resp_after_copy_config_rootcont.xml";

    private static final String COPY_CONFIG_TWO_ROOTS_XML = "/rootmodelnodetest/copy-config-with-two-roots.xml";
    private static final String GET_CONFIG_RESP_AFTER_COPY_CONFIG =
            "/rootmodelnodetest/get_config_resp_after_copy_config.xml";
    private static final String GET_CONFIG_RESPONSE_WITH_MULTIPLE_ROOTS_XML =
            "/rootmodelnodetest/get-config-response-with-multiple-roots.xml";

    private RootModelNodeAggregator m_runningAggregator;
    private SchemaRegistry m_schemaRegistry = null;
    private SubSystemRegistry m_subSystemRegistry = new SubSystemRegistryImpl();
    private NetConfServerImpl m_server;
    private ModelNodeDataStoreManager m_runningDsm;
    private ModelNodeDataStoreManager m_candidateDsm;

    private DsmJukeboxSubsystem m_jukeBoxSystem;
    private ModelNodeHelperRegistry m_modelNodeHelperRegistry = new ModelNodeHelperRegistryImpl(m_schemaRegistry);

    @Before
    public void setUp() throws ModelNodeInitException, SchemaBuildException, ModelNodeFactoryException {
        m_schemaRegistry = new SchemaRegistryImpl(Collections.<YangTextSchemaSource>emptyList(), new NoLockService());
        m_schemaRegistry.loadSchemaContext("jukebox", TestUtil.getJukeBoxYangs(), null, Collections.emptyMap());
        m_schemaRegistry.loadSchemaContext("toaster", Arrays.asList(TestUtil.getByteSource
                ("/rootmodelnodetest/toaster.yang")), null, Collections.emptyMap());
        m_runningDsm = spy(new InMemoryDSM(m_schemaRegistry, "running"));
        m_runningAggregator = new RootModelNodeAggregatorImpl(m_schemaRegistry, m_modelNodeHelperRegistry,
                m_runningDsm, m_subSystemRegistry);
        m_server = new NetConfServerImpl(m_schemaRegistry, mock(RpcPayloadConstraintParser.class));
        String xmlFilePath = TestUtil.class.getResource("/example-jukebox.xml").getPath();
        String xmlFilePath1 = getClass().getResource("/rootmodelnodetest/toaster.xml").getPath();
        deployJukeboxHelpers(m_runningDsm, m_modelNodeHelperRegistry, m_subSystemRegistry);
        deployToasterHelpers(m_runningDsm, m_modelNodeHelperRegistry, m_subSystemRegistry);

        ContainerSchemaNode jukeboxSchemaNode = (ContainerSchemaNode) m_schemaRegistry.getDataSchemaNode
                (JukeboxConstants.JUKEBOX_SCHEMA_PATH);
        ChildContainerHelper jukeBoxHelper = new RootEntityContainerModelNodeHelper(jukeboxSchemaNode,
                m_modelNodeHelperRegistry, m_subSystemRegistry, m_schemaRegistry, m_runningDsm);
        m_runningAggregator.addModelServiceRootHelper(JukeboxConstants.JUKEBOX_SCHEMA_PATH, jukeBoxHelper);

        ContainerSchemaNode toasterSchemaNode = (ContainerSchemaNode) m_schemaRegistry.getDataSchemaNode
                (ToasterConstants.TOASTER_SCHEMA_PATH);
        ChildContainerHelper toasterHelper = new RootEntityContainerModelNodeHelper(toasterSchemaNode,
                m_modelNodeHelperRegistry, m_subSystemRegistry, m_schemaRegistry, m_runningDsm);
        m_runningAggregator.addModelServiceRootHelper(ToasterConstants.TOASTER_SCHEMA_PATH, toasterHelper);

        DataStore dataStore = new DataStore(StandardDataStores.RUNNING, m_runningAggregator, m_subSystemRegistry);
        NbiNotificationHelper nbiNotificationHelper = mock(NbiNotificationHelper.class);
        dataStore.setNbiNotificationHelper(nbiNotificationHelper);

        m_server.setRunningDataStore(dataStore);
        YangUtils.loadXmlDataIntoServer(m_server, xmlFilePath);
        YangUtils.loadXmlDataIntoServer(m_server, xmlFilePath1);
    }

    private void deployToasterHelpers(ModelNodeDataStoreManager dsm, ModelNodeHelperRegistry modelNodeHelperRegistry,
                                      SubSystemRegistry subSystemRegistry) throws ModelNodeInitException,
            ModelNodeFactoryException {
        String yangFilePath = getClass().getResource("/rootmodelnodetest/toaster.yang").getPath();
        YangUtils.deployInMemoryHelpers(yangFilePath,
                new LocalSubSystem(), modelNodeHelperRegistry, subSystemRegistry, m_schemaRegistry, dsm);

    }

    private void deployJukeboxHelpers(ModelNodeDataStoreManager dsm, ModelNodeHelperRegistry modelNodeHelperRegistry,
                                      SubSystemRegistry subSystemRegistry) throws ModelNodeInitException,
            ModelNodeFactoryException {
        String yangFilePath = getClass().getResource("/yangs/example-jukebox.yang").getPath();
        m_jukeBoxSystem = spy(new DsmJukeboxSubsystem(dsm, "http://example.com/ns/example-jukebox"));
        YangUtils.deployInMemoryHelpers(yangFilePath, m_jukeBoxSystem, modelNodeHelperRegistry, subSystemRegistry,
                m_schemaRegistry, dsm);
    }

    private void setUpCandidateStore() throws ModelNodeInitException, ModelNodeFactoryException {
        m_candidateDsm = new InMemoryDSM(m_schemaRegistry, "candidate");
        ModelNodeHelperRegistry registryForCandidateDS = new ModelNodeHelperRegistryImpl(m_schemaRegistry);
        RootModelNodeAggregator candidateAggregator = new RootModelNodeAggregatorImpl(m_schemaRegistry,
                registryForCandidateDS, m_candidateDsm, m_subSystemRegistry);
        String xmlFilePath = getClass().getResource("/example-jukebox.xml").getPath();
        String xmlFilePath1 = getClass().getResource("/rootmodelnodetest/toaster.xml").getPath();
        deployJukeboxHelpers(m_candidateDsm, registryForCandidateDS, m_subSystemRegistry);
        deployToasterHelpers(m_candidateDsm, registryForCandidateDS, m_subSystemRegistry);

        ContainerSchemaNode jukeboxSchemaNode = (ContainerSchemaNode) m_schemaRegistry.getDataSchemaNode
                (JukeboxConstants.JUKEBOX_SCHEMA_PATH);
        ChildContainerHelper jukeBoxHelper = new RootEntityContainerModelNodeHelper(jukeboxSchemaNode,
                registryForCandidateDS, m_subSystemRegistry, m_schemaRegistry, m_candidateDsm);
        candidateAggregator.addModelServiceRootHelper(JukeboxConstants.JUKEBOX_SCHEMA_PATH, jukeBoxHelper);

        ContainerSchemaNode toasterSchemaNode = (ContainerSchemaNode) m_schemaRegistry.getDataSchemaNode
                (ToasterConstants.TOASTER_SCHEMA_PATH);
        ChildContainerHelper toasterHelper = new RootEntityContainerModelNodeHelper(toasterSchemaNode,
                registryForCandidateDS, m_subSystemRegistry, m_schemaRegistry, m_candidateDsm);
        candidateAggregator.addModelServiceRootHelper(ToasterConstants.TOASTER_SCHEMA_PATH, toasterHelper);

        DataStore dataStore = new DataStore(StandardDataStores.CANDIDATE, candidateAggregator, m_subSystemRegistry);
        NbiNotificationHelper nbiNotificationHelper = mock(NbiNotificationHelper.class);
        dataStore.setNbiNotificationHelper(nbiNotificationHelper);

        m_server.setDataStore(StandardDataStores.CANDIDATE, dataStore);
        YangUtils.loadXmlDataIntoServer(m_server, xmlFilePath, StandardDataStores.CANDIDATE);
        YangUtils.loadXmlDataIntoServer(m_server, xmlFilePath1, StandardDataStores.CANDIDATE);

    }

    @Test
    public void testGet() throws IOException, SAXException {
        //No filter
        verifyGet(m_server, (NetconfFilter) null, FULL_GET_RESPONSE, "1");

        //single filters
        verifyGet(m_server, JUKEBOX_FILTER, JUKEBOX_FILTER_RESPONSE, "1");
        verifyGet(m_server, TOASTER_FILTER, TOASTER_FILTER_RESPONSE, "1");

        //lets try 2 filters
        NetconfFilter filter = new NetconfFilter();
        filter.addXmlFilter(TestUtil.loadAsXml(JUKEBOX_FILTER))
                .addXmlFilter(TestUtil.loadAsXml(TOASTER_FILTER));
        verifyGet(m_server, filter, JUKEBOX_AND_TOASTER_FILTER_RESPONSE, "1");

        //just make sure there is no magic happening !
        verifyGet(m_server, INVALID_FILTER, INVALID_FILTER_RESPONSE, "1");

    }

    @Test
    public void testEditOnJukeboxAlone() throws EditConfigException, IOException, SAXException {
        EditConfigRequest request = new EditConfigRequest()
                .setTargetRunning()
                .setTestOption(EditConfigTestOptions.SET)
                .setErrorOption(EditConfigErrorOptions.STOP_ON_ERROR)
                .setConfigElement(new EditConfigElement().addConfigElementContent(loadAsXml(EDIT_ON_JUKEBOX)));
        request.setMessageId("1");
        NetConfResponse response = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(new NetconfClientInfo("unit-test", 1), request, response);
        verify(m_runningDsm, atLeastOnce()).beginModify();
        verify(m_runningDsm, atLeastOnce()).endModify();
        // assert Ok response
        assertEquals(load("/ok-response.xml"), responseToString(response));

        // do a get-config to be sure
        verifyGetConfig(m_server, (String) null, RESPONSE_AFTER_EDIT_ON_JUKEBOX, "1");
    }

    @Test
    public void testEditOnJukeboxAloneWithNsPrefix() throws EditConfigException, IOException, SAXException {
        EditConfigRequest request = new EditConfigRequest()
                .setTargetRunning()
                .setTestOption(EditConfigTestOptions.SET)
                .setErrorOption(EditConfigErrorOptions.STOP_ON_ERROR)
                .setConfigElement(new EditConfigElement().addConfigElementContent(loadAsXml
                        (EDIT_ON_JUKEBOX_WITH_NS_PREFIX)));
        request.setMessageId("1");
        NetConfResponse response = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(new NetconfClientInfo("unit-test", 1), request, response);
        // assert Ok response
        assertEquals(load("/ok-response.xml"), responseToString(response));

        // do a get-config to be sure
        verifyGetConfig(m_server, (String) null, RESPONSE_AFTER_EDIT_ON_JUKEBOX_WITH_NS_PREFIX, "1");
    }

    @Test
    public void testEditOnToasterAlone() throws EditConfigException, IOException, SAXException {
        EditConfigRequest request = new EditConfigRequest()
                .setTargetRunning()
                .setTestOption(EditConfigTestOptions.SET)
                .setErrorOption(EditConfigErrorOptions.STOP_ON_ERROR)
                .setConfigElement(new EditConfigElement().addConfigElementContent(loadAsXml(EDIT_ON_TOASTER)));
        request.setMessageId("1");
        NetConfResponse response = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(new NetconfClientInfo("unit-test", 1), request, response);
        // assert Ok response
        assertEquals(load("/ok-response.xml"), responseToString(response));

        // do a get-config to be sure
        verifyGetConfig(m_server, "", RESPONSE_AFTER_EDIT_ON_TOASTER, "1");
    }

    @Test
    public void testRootNodeCreate() throws Exception {
        m_schemaRegistry.loadSchemaContext("weather-app", Arrays.asList(TestUtil.getByteSource
                ("/rootmodelnodetest/weather-app.yang")), null, Collections.emptyMap());
        addWaRootHelpers();
        deployWeatherAppHelpers(m_runningDsm, m_modelNodeHelperRegistry, m_subSystemRegistry, m_schemaRegistry);
        EditConfigRequest request = new EditConfigRequest()
                .setTargetRunning()
                .setTestOption(EditConfigTestOptions.SET)
                .setErrorOption(EditConfigErrorOptions.STOP_ON_ERROR)
                .setConfigElement(new EditConfigElement().addConfigElementContent(loadAsXml
                        (ROOT_CONFIG_CONTAINER_CREATE)));
        request.setMessageId("1");
        NetConfResponse response = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(new NetconfClientInfo("unit-test", 1), request, response);
        // do a get-config to be sure
        verifyGetConfig(m_server, "/rootmodelnodetest/root_config_container_filter.xml",
                RESPONSE_AFTER_ROOT_CONFIG_CONTAINER_CREATE, "1");

        request.setConfigElement(new EditConfigElement().addConfigElementContent(loadAsXml(ROOT_CITY_LIST_CREATE)));
        response = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(new NetconfClientInfo("unit-test", 1), request, response);
        // do a get-config to be sure
        verifyGetConfig(m_server, "/rootmodelnodetest/root_city_list_filter.xml",
                RESPONSE_AFTER_ROOT_CITY_LIST_CREATE, "1");

        request.setConfigElement(new EditConfigElement().addConfigElementContent(loadAsXml(ROOT_CITY_LIST_CREATE2)));
        response = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(new NetconfClientInfo("unit-test", 1), request, response);
        // do a get-config to be sure
        verifyGetConfig(m_server, "/rootmodelnodetest/root_city_list_filter.xml",
                RESPONSE_AFTER_ROOT_CITY_LIST_CREATE2, "1");

    }

    @Test
    public void testRootNodeReplace() throws Exception {
        m_schemaRegistry.loadSchemaContext("weather-app", Arrays.asList(TestUtil.getByteSource
                ("/rootmodelnodetest/weather-app.yang")), null, Collections.emptyMap());
        addWaRootHelpers();
        deployWeatherAppHelpers(m_runningDsm, m_modelNodeHelperRegistry, m_subSystemRegistry, m_schemaRegistry);
        Element configElement = loadAsXml(ROOT_CONFIG_CONTAINER_REPLACE);
        EditConfigRequest request = new EditConfigRequest()
                .setTargetRunning()
                .setTestOption(EditConfigTestOptions.SET)
                .setErrorOption(EditConfigErrorOptions.STOP_ON_ERROR)
                .setConfigElement(new EditConfigElement().addConfigElementContent(configElement));
        request.setMessageId("1");
        NetConfResponse response = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(new NetconfClientInfo("unit-test", 1), request, response);
        // do a get-config to be sure
        verifyGetConfig(m_server, "/rootmodelnodetest/root_config_container_filter.xml",
                RESPONSE_AFTER_ROOT_CONFIG_CONTAINER_CREATE, "1");

        configElement = loadAsXml(ROOT_CITY_LIST_REPLACE);
        request.setConfigElement(new EditConfigElement().addConfigElementContent(configElement));
        response = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(new NetconfClientInfo("unit-test", 1), request, response);
        // do a get-config to be sure
        verifyGetConfig(m_server, "/rootmodelnodetest/root_city_list_filter.xml",
                RESPONSE_AFTER_ROOT_CITY_LIST_CREATE, "1");

        configElement = loadAsXml(ROOT_CITY_LIST_REPLACE2);
        request.setConfigElement(new EditConfigElement().addConfigElementContent(configElement));
        response = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(new NetconfClientInfo("unit-test", 1), request, response);
        // do a get-config to be sure
        verifyGetConfig(m_server, "/rootmodelnodetest/root_city_list_filter.xml",
                RESPONSE_AFTER_ROOT_CITY_LIST_CREATE2, "1");
    }

    private void addWaRootHelpers() {
        ContainerSchemaNode configSchemaNode = (ContainerSchemaNode) m_schemaRegistry.getDataSchemaNode
                (WA_CONFIG_SCHEMAPATH);
        ChildContainerHelper configNodeHelper = new RootEntityContainerModelNodeHelper(configSchemaNode,
                m_modelNodeHelperRegistry, m_subSystemRegistry, m_schemaRegistry, m_runningDsm);
        m_runningAggregator.addModelServiceRootHelper(WA_CONFIG_SCHEMAPATH, configNodeHelper);

        ContainerSchemaNode rootContainerSchemaNode = (ContainerSchemaNode) m_schemaRegistry.getDataSchemaNode
                (WA_ROOT_CONTAINER_SCHEMAPATH);
        ChildContainerHelper rootContainerNodeHelper = new RootEntityContainerModelNodeHelper(rootContainerSchemaNode,
                m_modelNodeHelperRegistry, m_subSystemRegistry, m_schemaRegistry, m_runningDsm);
        m_runningAggregator.addModelServiceRootHelper(WA_ROOT_CONTAINER_SCHEMAPATH, rootContainerNodeHelper);

        ListSchemaNode citySchemaNode = (ListSchemaNode) m_schemaRegistry.getDataSchemaNode(WA_CITY_SCHEMAPATH);
        ChildListHelper cityListHelper = new RootEntityListModelNodeHelper(citySchemaNode, m_modelNodeHelperRegistry,
                m_runningDsm,
                m_schemaRegistry, m_subSystemRegistry);
        m_runningAggregator.addModelServiceRootHelper(WA_CITY_SCHEMAPATH, cityListHelper);
    }

    private void deployWeatherAppHelpers(ModelNodeDataStoreManager dsm, ModelNodeHelperRegistry modelNodeHelperRegistry,
                                         SubSystemRegistry subSystemRegistry, SchemaRegistry schemaRegistry) throws
            ModelNodeInitException, ModelNodeFactoryException {
        String yangFilePath = getClass().getResource("/rootmodelnodetest/weather-app.yang").getPath();
        YangUtils.deployInMemoryHelpers(yangFilePath,
                new LocalSubSystem(), modelNodeHelperRegistry, subSystemRegistry, schemaRegistry, dsm);
    }

    @Test
    public void testEditOnBothModels() throws EditConfigException, IOException, SAXException {
        EditConfigRequest request = new EditConfigRequest()
                .setTargetRunning()
                .setTestOption(EditConfigTestOptions.SET)
                .setErrorOption(EditConfigErrorOptions.STOP_ON_ERROR)
                .setConfigElement(new EditConfigElement().addConfigElementContent(loadAsXml(EDIT_ON_JUKEBOX))
                        .addConfigElementContent(loadAsXml(EDIT_ON_TOASTER)));
        request.setMessageId("1");
        NetConfResponse response = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(new NetconfClientInfo("unit-test", 1), request, response);
        // assert Ok response
        assertEquals(load("/ok-response.xml"), responseToString(response));

        // do a get-config to be sure
        verifyGetConfig(m_server, "", RESPONSE_AFTER_EDIT_ON_BOTH_MODELS, "1");
    }

    @Test
    public void testEditConfigWhenPreCommitValidationFailed()
            throws EditConfigException, IOException, SAXException, SubSystemValidationException {
        EditConfigRequest request = new EditConfigRequest().setTargetRunning().setTestOption(EditConfigTestOptions.SET)
                .setErrorOption(EditConfigErrorOptions.ROLLBACK_ON_ERROR).setConfigElement(new EditConfigElement()
                        .addConfigElementContent(loadAsXml(EDIT_ON_JUKEBOX)).addConfigElementContent(loadAsXml
                                (EDIT_ON_TOASTER)));
        request.setMessageId("1");
        NetConfResponse response = new NetConfResponse().setMessageId("1");

        SubSystemValidationException validationException = new SubSystemValidationException(
                NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.OPERATION_FAILED,
                        "pre commit validation failed"));

        doThrow(validationException).when(m_jukeBoxSystem).notifyPreCommitChange(anyListOf(ChangeNotification.class));

        m_server.onEditConfig(new NetconfClientInfo("unit-test", 1), request, response);
        // assert Ok response
        assertEquals(load("/precommit-error-response.xml"), responseToString(response));
    }

    @Test
    public void testInvalidEdit1() throws IOException, SAXException {
        EditConfigRequest request = new EditConfigRequest()
                .setTargetRunning()
                .setTestOption(EditConfigTestOptions.SET)
                .setErrorOption(EditConfigErrorOptions.ROLLBACK_ON_ERROR)
                .setConfigElement(new EditConfigElement().addConfigElementContent(loadAsXml(INVALID_EDIT1)));
        request.setMessageId("1");
        NetConfResponse response = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(new NetconfClientInfo("unit-test", 1), request, response);
        // assert Not Ok response
        assertFalse(response.isOk());

        // do a get-config to be sure that nothing changes
        verifyGetConfig(m_server, "", FULL_GET_CONFIG_RESPONSE, "1");
    }

    @Test
    public void testInvalidEdit3() throws IOException, SAXException {
        EditConfigRequest request = new EditConfigRequest()
                .setTargetRunning()
                .setTestOption(EditConfigTestOptions.SET)
                .setErrorOption(EditConfigErrorOptions.ROLLBACK_ON_ERROR)
                .setConfigElement(new EditConfigElement().addConfigElementContent(loadAsXml(EDIT_ON_TOASTER)) //lets
                        // test rollback of the root model
                        .addConfigElementContent(loadAsXml(INVALID_EDIT1)));
        request.setMessageId("1");
        NetConfResponse response = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(new NetconfClientInfo("unit-test", 1), request, response);
        // assert Not Ok response
        assertFalse(response.isOk());
    }

    @Test
    public void testGetConfig() throws IOException, SAXException {
        //No filter
        verifyGetConfig(m_server, (NetconfFilter) null, FULL_GET_CONFIG_RESPONSE, "1");

        //single filters
        verifyGetConfig(m_server, JUKEBOX_FILTER, JUKEBOX_FILTER_RESPONSE, "1");
        verifyGetConfig(m_server, TOASTER_FILTER, TOASTER_FILTER_RESPONSE, "1");

        //lets try 2 filters
        NetconfFilter filter = new NetconfFilter();
        filter.addXmlFilter(TestUtil.loadAsXml(JUKEBOX_FILTER))
                .addXmlFilter(TestUtil.loadAsXml(TOASTER_FILTER));
        verifyGetConfig(m_server, filter, JUKEBOX_AND_TOASTER_FILTER_RESPONSE, "1");

        //just make sure there is no magic happening !
        verifyGetConfig(m_server, INVALID_FILTER, INVALID_FILTER_RESPONSE, "1");
    }

    @Test
    public void testGetConfig1() throws IOException, SAXException, SchemaBuildException, ModelNodeFactoryException,
            ModelNodeInitException {
        m_schemaRegistry.loadSchemaContext("tester", Arrays.asList(TestUtil.getByteSource("/rootmodelnodetest/tester" +
                ".yang")), null, Collections.emptyMap());
        String stateRootPath = getClass().getResource("/rootmodelnodetest/state-root.xml").getPath();
        String stateContainerPath = getClass().getResource("/rootmodelnodetest/state-container.xml").getPath();
        deployTesterHelpers(m_runningDsm, m_modelNodeHelperRegistry, m_subSystemRegistry);

        ContainerSchemaNode testerSchemaNode = (ContainerSchemaNode) m_schemaRegistry.getDataSchemaNode
                (TesterConstants.STATE_ROOT_SCHEMA_PATH);
        ChildContainerHelper testerHelper = new RootEntityContainerModelNodeHelper(testerSchemaNode,
                m_modelNodeHelperRegistry, m_subSystemRegistry, m_schemaRegistry, m_runningDsm);
        m_runningAggregator.addModelServiceRootHelper(TesterConstants.STATE_ROOT_SCHEMA_PATH, testerHelper);

        ContainerSchemaNode stateContainerSchemaNode = (ContainerSchemaNode) m_schemaRegistry.getDataSchemaNode
                (TesterConstants.STATE_CONTAINER_SCHEMA_PATH);
        ChildContainerHelper stateContainerHelper = new RootEntityContainerModelNodeHelper(stateContainerSchemaNode,
                m_modelNodeHelperRegistry, m_subSystemRegistry, m_schemaRegistry, m_runningDsm);
        m_runningAggregator.addModelServiceRootHelper(TesterConstants.STATE_CONTAINER_SCHEMA_PATH,
                stateContainerHelper);

        YangUtils.loadXmlDataIntoServer(m_server, stateRootPath);
        YangUtils.loadXmlDataIntoServer(m_server, stateContainerPath);

        verifyGetConfig(m_server, (NetconfFilter) null, FULL_GET_CONFIG_RESPONSE_TESTER, "1");
    }

    @Test
    public void testGetReturnsNoEmptyStateRoot() throws IOException, SAXException, SchemaBuildException,
            ModelNodeFactoryException, ModelNodeInitException {
        m_schemaRegistry.loadSchemaContext("tester", Arrays.asList(TestUtil.getByteSource("/rootmodelnodetest/tester" +
                ".yang")), null, Collections.emptyMap());
        String stateRootPath = getClass().getResource("/rootmodelnodetest/state-root.xml").getPath();
        String stateContainerPath = getClass().getResource("/rootmodelnodetest/state-container.xml").getPath();
        deployTesterHelpers(m_runningDsm, m_modelNodeHelperRegistry, m_subSystemRegistry);

        ContainerSchemaNode testerSchemaNode = (ContainerSchemaNode) m_schemaRegistry.getDataSchemaNode
                (TesterConstants.STATE_ROOT_SCHEMA_PATH);
        ChildContainerHelper testerHelper = new RootEntityContainerModelNodeHelper(testerSchemaNode,
                m_modelNodeHelperRegistry, m_subSystemRegistry, m_schemaRegistry, m_runningDsm);
        m_runningAggregator.addModelServiceRootHelper(TesterConstants.STATE_ROOT_SCHEMA_PATH, testerHelper);

        ContainerSchemaNode stateContainerSchemaNode = (ContainerSchemaNode) m_schemaRegistry.getDataSchemaNode
                (TesterConstants.STATE_CONTAINER_SCHEMA_PATH);
        ChildContainerHelper stateContainerHelper = new RootEntityContainerModelNodeHelper(stateContainerSchemaNode,
                m_modelNodeHelperRegistry, m_subSystemRegistry, m_schemaRegistry, m_runningDsm);
        m_runningAggregator.addModelServiceRootHelper(TesterConstants.STATE_CONTAINER_SCHEMA_PATH,
                stateContainerHelper);

        YangUtils.loadXmlDataIntoServer(m_server, stateRootPath);
        YangUtils.loadXmlDataIntoServer(m_server, stateContainerPath);

        verifyGet(m_server, (NetconfFilter) null, FULL_GET_RESPONSE_TESTER, "1");
    }

    private void deployTesterHelpers(ModelNodeDataStoreManager dsm, ModelNodeHelperRegistry modelNodeHelperRegistry,
                                     SubSystemRegistry subSystemRegistry) throws ModelNodeFactoryException {

        String yangFilePath = getClass().getResource("/rootmodelnodetest/tester.yang").getPath();

        SubSystem stateContainerSubSystem = new LocalSubSystem() {
            public Map<ModelNodeId, List<Element>> retrieveStateAttributes(Map<ModelNodeId, Pair<List<QName>,
                    List<FilterNode>>> mapAttributes)
                    throws GetAttributeException {
                Map<ModelNodeId, List<Element>> stateInfo = new HashMap<>();
                List<Element> elements = new ArrayList<>();
                Document document = DocumentUtils.createDocument();

                for (java.util.Map.Entry<ModelNodeId, Pair<List<QName>, List<FilterNode>>> entry : mapAttributes
                        .entrySet()) {
                    if (entry.getKey().xPathString().equals("/state-container")) {
                        List<Pair<String, Object>> childs = new ArrayList<>();
                        childs.add(new Pair<String, Object>("name", "test1"));
                        childs.add(new Pair<String, Object>("value", "value1"));

                        Element element = DocumentUtils.getElement(document, "person", childs, "http://netconfcentral" +
                                ".org/ns/tester", "testPrefix");
                        elements.add(element);
                        stateInfo.put(entry.getKey(), elements);
                    }
                }

                return stateInfo;
            }

            ;
        };


        YangUtils.deployInMemoryHelpers(yangFilePath, stateContainerSubSystem, modelNodeHelperRegistry,
                subSystemRegistry, m_schemaRegistry,
                dsm);

    }

    @Test
    public void testCopyConfig1() throws ModelNodeInitException, EditConfigException, SAXException, IOException,
            ModelNodeFactoryException {
        setUpCandidateStore();
        //do an edit
        testEditOnBothModels();
        //restore running
        copyFromCandidateToRunning();
        //make sure the running store is restored
        verifyGet(m_server, (NetconfFilter) null, FULL_GET_RESPONSE, "1");
        verify(m_runningDsm, atLeastOnce()).beginModify();
        verify(m_runningDsm, atLeastOnce()).endModify();
    }

    @Test
    public void testCopyConfig2() throws ModelNodeInitException, EditConfigException, SAXException, IOException,
            ModelNodeFactoryException {
        setUpCandidateStore();
        //do an edit on running
        testEditOnBothModels();
        //copy new runnig to candidate
        copyFromRunningToCandidate();
        //make sure the candidate also has changes
        verifyGetConfig(m_server, StandardDataStores.CANDIDATE, null, RESPONSE_AFTER_EDIT_ON_BOTH_MODELS, "1");
    }

    @Test
    public void testCopyConfig3() throws ModelNodeInitException, EditConfigException, SAXException, IOException,
            ModelNodeFactoryException, SchemaBuildException {
        //add more state root or container and make sure they are not changed when copy-config done.
        m_schemaRegistry.loadSchemaContext("tester", Arrays.asList(TestUtil.getByteSource("/rootmodelnodetest/tester" +
                ".yang")), null, Collections.emptyMap());
        String stateRootPath = getClass().getResource("/rootmodelnodetest/state-root.xml").getPath();
        String stateContainerPath = getClass().getResource("/rootmodelnodetest/state-container.xml").getPath();
        deployTesterHelpers(m_runningDsm, m_modelNodeHelperRegistry, m_subSystemRegistry);

        ContainerSchemaNode testerSchemaNode = (ContainerSchemaNode) m_schemaRegistry.getDataSchemaNode
                (TesterConstants.STATE_ROOT_SCHEMA_PATH);
        ChildContainerHelper testerHelper = new RootEntityContainerModelNodeHelper(testerSchemaNode,
                m_modelNodeHelperRegistry, m_subSystemRegistry, m_schemaRegistry, m_runningDsm);
        m_runningAggregator.addModelServiceRootHelper(TesterConstants.STATE_ROOT_SCHEMA_PATH, testerHelper);

        ContainerSchemaNode stateContainerSchemaNode = (ContainerSchemaNode) m_schemaRegistry.getDataSchemaNode
                (TesterConstants.STATE_CONTAINER_SCHEMA_PATH);
        ChildContainerHelper stateContainerHelper = new RootEntityContainerModelNodeHelper(stateContainerSchemaNode,
                m_modelNodeHelperRegistry, m_subSystemRegistry, m_schemaRegistry, m_runningDsm);
        m_runningAggregator.addModelServiceRootHelper(TesterConstants.STATE_CONTAINER_SCHEMA_PATH,
                stateContainerHelper);

        YangUtils.loadXmlDataIntoServer(m_server, stateRootPath);
        YangUtils.loadXmlDataIntoServer(m_server, stateContainerPath);


        copyConfig(loadAsXml(COPY_CONFIG1_XML), StandardDataStores.RUNNING);
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, FULL_GET_CONFIG_RESPONSE_AFTER_DO_COPY_CONFIG, "1");
    }

    @Test
    public void testInvalidCopyConfig() throws ModelNodeInitException, EditConfigException, SAXException,
            IOException, ModelNodeFactoryException {
        setUpCandidateStore();

        copyConfig(loadAsXml(INVALID_COPY_CONFIG1_XML), StandardDataStores.RUNNING);
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, FULL_GET_CONFIG_RESPONSE, "1");

        copyConfig(loadAsXml(INVALID_COPY_CONFIG2_XML), StandardDataStores.RUNNING);
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, FULL_GET_CONFIG_RESPONSE, "1");
    }

    @Test
    public void testCopyConfigDeletesRootContainerNotPresent()
            throws IOException, SAXException, ModelNodeFactoryException, ModelNodeInitException {

        // Case when DS has Root nodes 1,2 and copy-config has root nodes 1, Root node 2 is to be deleted.
        setUpCandidateStore();
        copyConfig(loadAsXml(COPY_CONFIG_ONE_ROOTCONTAINER_XML), StandardDataStores.RUNNING);
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, GET_CONFIG_RESP_AFTER_COPY_CONFIG_ROOTCONT, "1");
    }

    @Test
    public void testCopyConfigDeletesRootListNotPresent() throws Exception {
        m_runningAggregator.removeModelServiceRootHelpers(JukeboxConstants.JUKEBOX_SCHEMA_PATH);
        m_runningAggregator.removeModelServiceRootHelpers(ToasterConstants.TOASTER_SCHEMA_PATH);
        // Case when DS already has root nodes A & B, copy-config coming in with A & C; resulting C to be created & B
        // to be dropped
        // Weather-app has one list and two container as root elements
        m_schemaRegistry.loadSchemaContext("weather-app", Arrays.asList(TestUtil.getByteSource
                ("/rootmodelnodetest/weather-app.yang")), null, Collections.emptyMap());
        addWaRootHelpers();
        deployWeatherAppHelpers(m_runningDsm, m_modelNodeHelperRegistry, m_subSystemRegistry, m_schemaRegistry);

        List<Element> configElements = new ArrayList<>();
        Element rootContainer = TestUtil.transformToElement("<config xmlns=\"http://www.test-company" +
                ".com/solutions/anv-weather-app\">\n"
                + "<https-proxy>\n"
                + "<proxy-host>global.proxy.bbf.org</proxy-host>\n"
                + "<proxy-port>8000</proxy-port>\n"
                + "<username>user</username>\n"
                + "<password>psswd</password>\n"
                + "</https-proxy>\n"
                + "</config>");
        Element rootList1 = TestUtil.transformToElement("<city xmlns=\"http://www.test-company" +
                ".com/solutions/anv-weather-app\">\n"
                + "    <city-name>Bangalore</city-name>\n"
                + "    <country-name>India</country-name>\n"
                + "</city>");

        Element rootList2 = TestUtil.transformToElement("<city xmlns=\"http://www.test-company" +
                ".com/solutions/anv-weather-app\">\n"
                + "    <city-name>Mysore</city-name>\n"
                + "    <country-name>India</country-name>\n"
                + "</city>");

        configElements.add(rootContainer);
        configElements.add(rootList1);
        configElements.add(rootList2);
        EditConfigRequest request = new EditConfigRequest()
                .setTargetRunning()
                .setTestOption(EditConfigTestOptions.SET)
                .setErrorOption(EditConfigErrorOptions.STOP_ON_ERROR)
                .setConfigElement(new EditConfigElement().setConfigElementContents(configElements));
        request.setMessageId("1");
        NetConfResponse response = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(new NetconfClientInfo("unit-test", 1), request, response);
        // do a get-config to be sure
        verifyGetConfig(m_server, (NetconfFilter) null, GET_CONFIG_RESPONSE_WITH_MULTIPLE_ROOTS_XML, "1");

        // Copy-config coming in with config & root container; resulting root list to be deleted & root container to
        // be created
        copyConfig(loadAsXml(COPY_CONFIG_TWO_ROOTS_XML), StandardDataStores.RUNNING);
        verifyGetConfig(m_server, (NetconfFilter) null, GET_CONFIG_RESP_AFTER_COPY_CONFIG, "1");

    }


    @Test
    public void testCleanup() {
        m_runningAggregator.removeModelServiceRootHelpers(JukeboxConstants.JUKEBOX_SCHEMA_PATH);
        m_runningAggregator.removeModelServiceRootHelpers(ToasterConstants.TOASTER_SCHEMA_PATH);
        assertEquals(Collections.emptyList(), m_runningAggregator.getModelServiceRoots());
    }


    private void copyFromRunningToCandidate() {
        copyConfig(StandardDataStores.RUNNING, StandardDataStores.CANDIDATE);
    }

    private void copyFromCandidateToRunning() {
        copyConfig(StandardDataStores.CANDIDATE, StandardDataStores.RUNNING);
    }

    private void copyConfig(String source, String target) {
        CopyConfigRequest request = new CopyConfigRequest().setSource(source, false).setTarget(target, false);
        request.setMessageId("1");
        NetConfResponse response = new NetConfResponse().setMessageId("1");
        m_server.onCopyConfig(new NetconfClientInfo("unit-test", 1), request, response);
    }

    private void copyConfig(Element sourceConfigElement, String target) {
        CopyConfigRequest request = new CopyConfigRequest().setSourceConfigElement(sourceConfigElement).setTarget
                (target, false);
        request.setMessageId("1");
        NetConfResponse response = new NetConfResponse().setMessageId("1");
        m_server.onCopyConfig(new NetconfClientInfo("unit-test", 1), request, response);
    }

}
