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

import static org.broadband_forum.obbaa.netconf.api.util.DocumentUtils.createDocument;

import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.server.NetconfQueryParams;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.SchemaPathUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ConfigAttributeGetContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetConfigContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NotificationContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.StateAttributeGetContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDSMRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.utils.AnnotationAnalysisException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.service.ModelService;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.service.ModelServiceDeployer;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.service.ModelServiceDeployerException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.TimingLogger;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.EntityRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.annotation.dao.JukeboxDao;
import org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.Jukebox;
import org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunnerForSpring;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

/**
 * Created by keshava on 2/1/16.
 */
@RunWith(RequestScopeJunitRunnerForSpring.class)
@ContextConfiguration(locations = {"/lazyrootmodelnodeaggregatortest/app-context-test.xml"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class LazyRootModelNodeAggregatorTest {
    private static final Logger LOGGER = LogManager.getLogger(LazyRootModelNodeAggregatorTest.class);
    private static final SchemaPath m_jukeboxSchemaPath = JukeboxConstants.JUKEBOX_SCHEMA_PATH;

    @Autowired
    private RootModelNodeAggregator m_rootModelNodeAggregator;
    @Autowired
    JukeboxDao m_jukeboxDao;
    @Autowired
    ModelServiceDeployer m_modelServiceDeployer;
    @Autowired
    ModelService m_jukeboxService;
    @Autowired
    private SchemaRegistry m_schemaRegistry;
    @Autowired
    private ModelNodeHelperRegistry m_modelNodeHelperRegistry;
    @Autowired
    @Qualifier("dsmProxy")
    private ModelNodeDataStoreManager m_modelNodeDSM;
    @Autowired
    private SubSystemRegistry m_subsystemRegistry;
    @Autowired
    private EntityRegistry m_entityRegistry;

    private ChildContainerHelper m_containerHelper;

    @Mock
    private NetconfClientInfo m_clientInfo;

    private ConfigAttributeGetContext m_configAttributeGetContext;

    @Before
    public void setUp() throws ModelServiceDeployerException, AnnotationAnalysisException {
        m_configAttributeGetContext = new ConfigAttributeGetContext();
        m_entityRegistry.updateRegistry("test-component", Arrays.<Class>asList(Jukebox.class), m_schemaRegistry, null, new ModelNodeDSMRegistryImpl());
        Jukebox newJukebox = new Jukebox();
        newJukebox.setParentId(new ModelNodeId().getModelNodeIdAsString());
        newJukebox.setSchemaPath(SchemaPathUtil.toString(m_jukeboxSchemaPath));
        m_jukeboxDao.createAndCommit(newJukebox);
        m_modelServiceDeployer.deploy(Arrays.asList(m_jukeboxService));
        m_containerHelper = new RootEntityContainerModelNodeHelper((ContainerSchemaNode) m_schemaRegistry.getDataSchemaNode(m_jukeboxSchemaPath),
                m_modelNodeHelperRegistry, m_subsystemRegistry,
                m_schemaRegistry, m_modelNodeDSM);
    }

    @Test
    public void testLazyGet() throws Exception {
        m_rootModelNodeAggregator.addModelServiceRootHelper(m_jukeboxSchemaPath, m_containerHelper);
        GetContext getContext = new GetContext(m_clientInfo, createDocument(), null, new StateAttributeGetContext(), m_configAttributeGetContext);
        TestUtil.assertXMLEquals("/lazyrootmodelnodeaggregatortest/empty-jukebox.xml", m_rootModelNodeAggregator.get(getContext, NetconfQueryParams.NO_PARAMS).get(0));
    }

    @Test
    public void testLazyGetConfig() throws Exception {
        m_rootModelNodeAggregator.addModelServiceRootHelper(m_jukeboxSchemaPath, m_containerHelper);
        GetConfigContext context = new GetConfigContext(m_clientInfo, createDocument(), null, m_configAttributeGetContext);
        TestUtil.assertXMLEquals("/lazyrootmodelnodeaggregatortest/empty-jukebox.xml", m_rootModelNodeAggregator.getConfig(context, NetconfQueryParams.NO_PARAMS).get(0));
    }

    @Test
    public void testLazyEdit() throws Exception {
        m_rootModelNodeAggregator.addModelServiceRootHelper(m_jukeboxSchemaPath, m_containerHelper);
        GetConfigContext context = new GetConfigContext(m_clientInfo, createDocument(), null, m_configAttributeGetContext);
        EditConfigRequest request = DocumentToPojoTransformer.getEditConfig(DocumentUtils.stringToDocument(TestUtil.loadAsString("/lazyrootmodelnodeaggregatortest/add-song.xml")));
        TimingLogger.withStartAndFinish(() -> m_rootModelNodeAggregator.editConfig(request, new NotificationContext()));
        TestUtil.assertXMLEquals("/lazyrootmodelnodeaggregatortest/jukebox-with-song.xml", m_rootModelNodeAggregator.getConfig(context, NetconfQueryParams.NO_PARAMS).get(0));
    }

    @Test
    public void testLazyEditWhenThereIsNoRoot() throws Exception {
        removeJukeboxEntity();
        m_rootModelNodeAggregator.addModelServiceRootHelper(m_jukeboxSchemaPath, m_containerHelper);
        GetConfigContext context = new GetConfigContext(m_clientInfo, createDocument(), null, m_configAttributeGetContext);
        EditConfigRequest request = DocumentToPojoTransformer.getEditConfig(DocumentUtils.stringToDocument(TestUtil.loadAsString("/lazyrootmodelnodeaggregatortest/add-song.xml")));
        TimingLogger.withStartAndFinish(() -> m_rootModelNodeAggregator.editConfig(request, new NotificationContext()));
        TestUtil.assertXMLEquals("/lazyrootmodelnodeaggregatortest/jukebox-with-song.xml", m_rootModelNodeAggregator.getConfig(context, NetconfQueryParams.NO_PARAMS).get(0));
    }

    private void removeJukeboxEntity() {
        m_jukeboxDao.delete(m_jukeboxDao.findByIdWithWriteLock(new ModelNodeId().getModelNodeIdAsString()));

    }

}
