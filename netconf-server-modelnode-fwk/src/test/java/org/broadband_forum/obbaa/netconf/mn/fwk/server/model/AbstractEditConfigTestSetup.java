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

import static org.mockito.Mockito.mock;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DSExpressionValidator;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.yang.AbstractValidationTestSetup;

import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregatorImpl;

import org.broadband_forum.obbaa.netconf.server.rpc.RpcPayloadConstraintParser;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;

public abstract class AbstractEditConfigTestSetup extends AbstractValidationTestSetup {

    protected ModelNode m_model;

    protected RootModelNodeAggregator m_rootModelNodeAggregator;
    protected NetConfServerImpl m_server;

    @Override
    public void setup() throws SchemaBuildException {
        m_schemaRegistry = new SchemaRegistryImpl(TestUtil.getJukeBoxYangs(), new NoLockService());
        m_modelNodeHelperRegistry = new ModelNodeHelperRegistryImpl(m_schemaRegistry);
        m_expValidator = new DSExpressionValidator(m_schemaRegistry, m_modelNodeHelperRegistry);
        createServerWithNonEmptyJukeBox();
    }

    public void createServerWithNonEmptyJukeBox() throws SchemaBuildException {
        m_server = new NetConfServerImpl(m_schemaRegistry, mock(RpcPayloadConstraintParser.class));
        m_model = TestUtil.createJukeBoxModelWithYear(m_modelNodeHelperRegistry, m_subSystemRegistry, m_schemaRegistry);
        m_rootModelNodeAggregator = new RootModelNodeAggregatorImpl(m_schemaRegistry, m_modelNodeHelperRegistry,
                mock(ModelNodeDataStoreManager.class), m_subSystemRegistry).addModelServiceRoot(m_componentId, m_model);
        DataStore dataStore = new DataStore(StandardDataStores.RUNNING, m_rootModelNodeAggregator, m_subSystemRegistry);
        NbiNotificationHelper nbiNotificationHelper = mock(NbiNotificationHelper.class);
        dataStore.setNbiNotificationHelper(nbiNotificationHelper);
        m_server.setRunningDataStore(dataStore);
    }

    public void createEmptyServer() throws SchemaBuildException {
        m_server = new NetConfServerImpl(m_schemaRegistry, mock(RpcPayloadConstraintParser.class));
        m_model = TestUtil.createEmptyJukeBox(m_modelNodeHelperRegistry, m_subSystemRegistry, m_schemaRegistry);
        m_rootModelNodeAggregator = new RootModelNodeAggregatorImpl(m_schemaRegistry, m_modelNodeHelperRegistry,
                mock(ModelNodeDataStoreManager.class), m_subSystemRegistry).addModelServiceRoot(m_componentId, m_model);
        DataStore dataStore = new DataStore(StandardDataStores.RUNNING, m_rootModelNodeAggregator, m_subSystemRegistry);
        NbiNotificationHelper nbiNotificationHelper = mock(NbiNotificationHelper.class);
        dataStore.setNbiNotificationHelper(nbiNotificationHelper);
        m_server.setRunningDataStore(dataStore);
    }
}
