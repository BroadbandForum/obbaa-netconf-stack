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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Collections;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.CloseSessionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.KillSessionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.LockRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationService;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregatorImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(RequestScopeJunitRunner.class)
public class LockUnLockKillCloseTest {
    private static final Logger LOGGER = LogManager.getLogger(LockUnLockKillCloseTest.class);
    private NetConfServerImpl m_server;
	private SubSystemRegistry m_subSystemRegistry = new SubSystemRegistryImpl();
    private SchemaRegistry m_schemaRegistry;
    private String m_componentId = "test";
	private ModelNodeHelperRegistry m_modelNodeHelperRegistry = new ModelNodeHelperRegistryImpl(m_schemaRegistry);

    @Before
    public void initServer() throws SchemaBuildException {
        m_schemaRegistry = new SchemaRegistryImpl(TestUtil.getJukeBoxYangs(), Collections.emptySet(), Collections.emptyMap(), new NoLockService());
        m_server = new NetConfServerImpl(m_schemaRegistry);
        m_subSystemRegistry.setCompositeSubSystem(new CompositeSubSystemImpl());
        RootModelNodeAggregator rootModelNodeAggregator = new RootModelNodeAggregatorImpl(m_schemaRegistry, m_modelNodeHelperRegistry,
                mock(ModelNodeDataStoreManager.class), m_subSystemRegistry).addModelServiceRoot(m_componentId, null);
		DataStore dataStore = new DataStore(StandardDataStores.RUNNING, rootModelNodeAggregator, m_subSystemRegistry);
		dataStore.setNotificationService(mock(NotificationService.class));
		m_server.setRunningDataStore(dataStore);
    }

    @Test
    public void testLockAndCloseSessionWorks() {
        //2 clients
        NetconfClientInfo client = new NetconfClientInfo("test", 1);
        NetconfClientInfo client2 = new NetconfClientInfo("test2", 2);
        m_server.onHello(client, null);
        m_server.onHello(client2, null);

        LockRequest lockRequest = new LockRequest();
        lockRequest.setMessageId("1");
        lockRequest.setTarget(StandardDataStores.RUNNING);

        NetConfResponse response = new NetConfResponse();
        response.setMessageId("1");

        m_server.onLock(client, lockRequest, response);
        assertTrue(response.isOk());
        response = new NetConfResponse();
        
        //make sure this does not succeed
        m_server.onLock(client2, lockRequest, response);
        assertFalse(response.isOk());
        LOGGER.info(response.responseToString());
        
        //disconnect client1
        response = new NetConfResponse();
        CloseSessionRequest closeSession = (CloseSessionRequest) new CloseSessionRequest().setMessageId("2");
        m_server.onCloseSession(client, closeSession, response);
        
        //now the lock request from client2 should succeed
        m_server.onLock(client2, lockRequest, response);
        assertTrue(response.isOk());
        LOGGER.info(response.responseToString());
    }
    
    @Test
    public void testLockAndKillSessionWorks() {
        //2 clients
        NetconfClientInfo client = new NetconfClientInfo("test", 1);
        NetconfClientInfo client2 = new NetconfClientInfo("test2", 2);
        m_server.onHello(client, null);
        m_server.onHello(client2, null);

        LockRequest lockRequest = new LockRequest();
        lockRequest.setMessageId("1");
        lockRequest.setTarget(StandardDataStores.RUNNING);

        NetConfResponse response = new NetConfResponse();
        response.setMessageId("1");

        m_server.onLock(client, lockRequest, response);
        assertTrue(response.isOk());
        response = new NetConfResponse();
        
        //make sure this does not succeed
        m_server.onLock(client2, lockRequest, response);
        assertFalse(response.isOk());
        LOGGER.info(response.responseToString());
        
        //disconnect client1
        response = new NetConfResponse();
        KillSessionRequest killSession = (KillSessionRequest) new KillSessionRequest().setSessionId(1).setMessageId("2");
        m_server.onKillSession(client2, killSession, response);
        
        //now the lock request from client2 should succeed
        m_server.onLock(client2, lockRequest, response);
        assertTrue(response.isOk());
        LOGGER.info(response.responseToString());
    }
    
    @Test
    public void testKillSessionWorks(){
        NetconfClientInfo client1 = new NetconfClientInfo("test", 1);
        NetconfClientInfo client2 = new NetconfClientInfo("test", 2);
        m_server.onHello(client1, null);
        m_server.onHello(client2, null);

        NetConfResponse response = new NetConfResponse();
        response.setMessageId("1");
        
        //Session to be killed cannot be current session
        KillSessionRequest killSession = (KillSessionRequest) new KillSessionRequest().setSessionId(1).setMessageId("1");
        m_server.onKillSession(client1, killSession, response);
        assertFalse(response.isOk());

        // Client1 killing client2 session
        killSession = (KillSessionRequest) new KillSessionRequest().setSessionId(2).setMessageId("1");
        m_server.onKillSession(client1, killSession, response);
        assertTrue(response.isOk());

        // Unknown session ID in kill request
        killSession = (KillSessionRequest) new KillSessionRequest().setSessionId(5).setMessageId("1");
        m_server.onKillSession(client1, killSession, response);
        assertFalse(response.isOk());


    }
}
