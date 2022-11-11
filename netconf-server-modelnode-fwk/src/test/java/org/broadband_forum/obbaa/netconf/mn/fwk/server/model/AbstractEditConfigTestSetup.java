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

import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.YangUtils.loadXmlDataIntoServer;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.getJukeBoxYangFileName;
import static org.mockito.Mockito.mock;

import java.util.Collections;

import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregatorImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DSExpressionValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.inmemory.InMemoryDSM;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.LocalSubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.YangUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.yang.AbstractValidationTestSetup;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcPayloadConstraintParser;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.mockito.Mockito;

public abstract class AbstractEditConfigTestSetup extends AbstractValidationTestSetup {

    protected ModelNode m_model;

    protected RootModelNodeAggregator m_rootModelNodeAggregator;
    protected NetConfServerImpl m_server;
    protected ModelNodeDataStoreManager m_modelNodeDSM;
    private static final String JUKEBOX_FILE_WITH_DATA = AbstractEditConfigTestSetup.class.getResource("/data-with-artist-lenny-with-year.xml").getPath();
    private static final String EMPTY_JUKEBOX_FILE = AbstractEditConfigTestSetup.class.getResource("/empty-example-jukebox.xml").getPath();
    private static final String EMPTY_LIBRARY_FILE = AbstractEditConfigTestSetup.class.getResource("/empty-library-example-jukebox.xml").getPath();


    @Override
    public void setup() throws SchemaBuildException, ModelNodeInitException {
        m_schemaRegistry = new SchemaRegistryImpl(TestUtil.getJukeBoxYangs(), Collections.emptySet(), Collections.emptyMap(), new NoLockService());
        SubSystemRegistry registry = Mockito.mock(SubSystemRegistry.class);
        m_expValidator = new DSExpressionValidator(m_schemaRegistry, m_modelNodeHelperRegistry, registry);
        createServerWithNonEmptyJukeBox();
    }

    public void createServerWithNonEmptyJukeBox() throws SchemaBuildException, ModelNodeInitException {
        createServer();
        loadXmlDataIntoServer(m_server, JUKEBOX_FILE_WITH_DATA);
    }

    public void createEmptyServer() throws SchemaBuildException, ModelNodeInitException {
        createServer();
        loadXmlDataIntoServer(m_server, EMPTY_JUKEBOX_FILE);
    }

    public void createServerWithEmptyLibrary() throws ModelNodeInitException, SchemaBuildException {
        createServer();
        loadXmlDataIntoServer(m_server, EMPTY_LIBRARY_FILE);
    }

    private void createServer() throws ModelNodeInitException, SchemaBuildException {
        m_server = new NetConfServerImpl(m_schemaRegistry, mock(RpcPayloadConstraintParser.class));
        m_modelNodeDSM = new InMemoryDSM(m_schemaRegistry);
        m_modelNodeHelperRegistry = new ModelNodeHelperRegistryImpl(m_schemaRegistry);
        m_model = YangUtils.createInMemoryModelNode(getJukeBoxYangFileName(), new LocalSubSystem(), m_modelNodeHelperRegistry,
                m_subSystemRegistry, m_schemaRegistry, m_modelNodeDSM);
        m_subSystemRegistry.setCompositeSubSystem(new CompositeSubSystemImpl());
        m_rootModelNodeAggregator = new RootModelNodeAggregatorImpl(m_schemaRegistry, m_modelNodeHelperRegistry,
                m_modelNodeDSM, m_subSystemRegistry).addModelServiceRoot(m_componentId, m_model);
        DataStore dataStore = new DataStore(StandardDataStores.RUNNING, m_rootModelNodeAggregator, m_subSystemRegistry);
        NbiNotificationHelper nbiNotificationHelper = new NbiNotificationHelperImpl();
        dataStore.setNbiNotificationHelper(nbiNotificationHelper);
        m_server.setRunningDataStore(dataStore);
    }
}
