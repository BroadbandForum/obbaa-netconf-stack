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
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.assertXMLEquals;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.loadAsXml;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.Collections;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigElement;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigErrorOptions;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigTestOptions;
import org.broadband_forum.obbaa.netconf.api.messages.LockRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorSeverity;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorType;
import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.api.messages.UnLockRequest;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregatorImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.inmemory.InMemoryDSM;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.LocalSubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.YangUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xml.sax.SAXException;

@RunWith(RequestScopeJunitRunner.class)
public class LockTest {
	private NetConfServerImpl m_server;
	private SubSystemRegistry m_subSystemRegistry = new SubSystemRegistryImpl();
    private SchemaRegistry m_schemaRegistry;
    private String m_componentId = "test";
	private ModelNodeHelperRegistry m_modelNodeHelperRegistry = new ModelNodeHelperRegistryImpl(m_schemaRegistry);
	private ModelNodeDataStoreManager m_modelNodeDsm;
	private static final String EMPTY_JUKEBOX_FILE = GetGetconfigWithStateFilterTest.class.getResource("/empty-example-jukebox.xml").getPath();

	@Before
    public void initServer() throws SchemaBuildException, ModelNodeInitException {
        m_schemaRegistry = new SchemaRegistryImpl(TestUtil.getJukeBoxYangs(), Collections.emptySet(), Collections.emptyMap(), new NoLockService());
        m_server = new NetConfServerImpl(m_schemaRegistry);
		m_modelNodeDsm = new InMemoryDSM(m_schemaRegistry);
		m_subSystemRegistry.setCompositeSubSystem(new CompositeSubSystemImpl());
		ModelNode jukeboxNode = YangUtils.createInMemoryModelNode(TestUtil.getJukeBoxDepNamesWithStateAttrs(), new LocalSubSystem(),
				m_modelNodeHelperRegistry, m_subSystemRegistry, m_schemaRegistry, m_modelNodeDsm);;
		RootModelNodeAggregator rootModelNodeAggregator = new RootModelNodeAggregatorImpl(m_schemaRegistry, m_modelNodeHelperRegistry,
				mock(ModelNodeDataStoreManager.class), m_subSystemRegistry).addModelServiceRoot(m_componentId, jukeboxNode);
		DataStore dataStore = new DataStore(StandardDataStores.RUNNING, rootModelNodeAggregator, m_subSystemRegistry);
		NbiNotificationHelper nbiNotificationHelper = mock(NbiNotificationHelper.class);
		dataStore.setNbiNotificationHelper(nbiNotificationHelper);
		m_server.setRunningDataStore(dataStore);
		NetconfClientInfo client = new NetconfClientInfo("test", 1);
		m_server.onHello(client, null);
		loadXmlDataIntoServer(m_server, EMPTY_JUKEBOX_FILE);
        m_server.onHello(client, null);
    }

	@Test
	public void testLockAndUnlockSameSession() {
		NetconfClientInfo client = new NetconfClientInfo("test", 1);
		m_server.onHello(client, null);
		LockRequest lockRequest = new LockRequest();
		lockRequest.setMessageId("1");
		lockRequest.setTarget(StandardDataStores.RUNNING);

		NetConfResponse response = new NetConfResponse();
		response.setMessageId("1");

		m_server.onLock(client, lockRequest, response);
		assertTrue(response.isOk());

		UnLockRequest unlockRequest = new UnLockRequest();
		unlockRequest.setTarget(StandardDataStores.RUNNING);
		unlockRequest.setMessageId("2");

		NetConfResponse unlockResponse = new NetConfResponse();
		unlockResponse.setMessageId("2");

		m_server.onUnlock(client, unlockRequest, unlockResponse);

		assertTrue(unlockResponse.isOk());
	}

	@Test
	public void testLockUnlockWithoutLockShouldFail() {
		NetconfClientInfo client = new NetconfClientInfo("test", 1);

		UnLockRequest unlockRequest = new UnLockRequest();
		unlockRequest.setTarget(StandardDataStores.RUNNING);
		unlockRequest.setMessageId("1");

		NetConfResponse unlockResponse = new NetConfResponse();
		unlockResponse.setMessageId("1");

		m_server.onUnlock(client, unlockRequest, unlockResponse);

		assertFalse(unlockResponse.isOk());
	}

	@Test
	public void testOnlyASingleSessionCanHaveLock() throws SAXException, IOException {
		// first session
		{
			NetconfClientInfo client = new NetconfClientInfo("test", 1);

			LockRequest lockRequest = new LockRequest();
			lockRequest.setMessageId("1");
			lockRequest.setTarget(StandardDataStores.RUNNING);

			NetConfResponse response = new NetConfResponse();
			response.setMessageId("1");

			m_server.onLock(client, lockRequest, response);
			assertTrue(response.isOk());
		}

		// second session
		{
			NetconfClientInfo client = new NetconfClientInfo("test", 2);

			LockRequest lockRequest = new LockRequest();
			lockRequest.setMessageId("1");
			lockRequest.setTarget(StandardDataStores.RUNNING);

			NetConfResponse response = new NetConfResponse();
			response.setMessageId("1");

			m_server.onLock(client, lockRequest, response);
			assertFalse(response.isOk());

			assertXMLEquals("/lockfailed-othersession.xml", response);
			
		}
	}
	
	@Test
	public void testLockUnLockOnNonExistingDatastore(){
		NetconfClientInfo client = new NetconfClientInfo("test", 1);

		LockRequest lockRequest = new LockRequest();
		lockRequest.setTarget("test");
		lockRequest.setMessageId("1");

		NetConfResponse lockResponse = new NetConfResponse();
		lockResponse.setMessageId("1");

		m_server.onLock(client, lockRequest, lockResponse);

		assertFalse(lockResponse.isOk());
		assertEquals(1, lockResponse.getErrors().size());
        NetconfRpcError lockRpcError = lockResponse.getErrors().get(0);
		assertEquals(NetconfRpcErrorType.Application, lockRpcError.getErrorType());		
		assertEquals("Invalid target", lockRpcError.getErrorMessage());
		assertEquals(NetconfRpcErrorSeverity.Error, lockRpcError.getErrorSeverity());
		assertEquals(NetconfRpcErrorTag.INVALID_VALUE, lockRpcError.getErrorTag());
		
		UnLockRequest unlockRequest = new UnLockRequest();
		unlockRequest.setTarget("test");
		unlockRequest.setMessageId("1");

		NetConfResponse unlockResponse = new NetConfResponse();
		unlockResponse.setMessageId("1");
		
		m_server.onUnlock(client, unlockRequest, unlockResponse);

		assertFalse(unlockResponse.isOk());
		assertEquals(1, unlockResponse.getErrors().size());
        NetconfRpcError unlockRpcError = unlockResponse.getErrors().get(0);
		assertEquals(NetconfRpcErrorType.Application, unlockRpcError.getErrorType());
		assertEquals("Invalid target", unlockRpcError.getErrorMessage());
		assertEquals(NetconfRpcErrorSeverity.Error, unlockRpcError.getErrorSeverity());
		assertEquals(NetconfRpcErrorTag.INVALID_VALUE, unlockRpcError.getErrorTag());
		
	}
	
	@Test
	public void testLockUnlockWithEditConfig() {
		// session 1 lock the datastore
		NetconfClientInfo client1 = new NetconfClientInfo("test", 1);
		m_server.onHello(client1, null);
		LockRequest lockRequest = new LockRequest();
		lockRequest.setMessageId("1");
		lockRequest.setTarget(StandardDataStores.RUNNING);

		NetConfResponse response = new NetConfResponse();
		response.setMessageId("1");

		m_server.onLock(client1, lockRequest, response);
		assertTrue(response.isOk());
		
		
		// session 1 send edit config
		EditConfigRequest editRequest = new EditConfigRequest().setTargetRunning().setTestOption(EditConfigTestOptions.SET)
				.setErrorOption(EditConfigErrorOptions.STOP_ON_ERROR)
				.setConfigElement(new EditConfigElement().addConfigElementContent(loadAsXml("/editconfig-simple.xml")));
		editRequest.setMessageId("2");
		response = new NetConfResponse().setMessageId("2");
		m_server.onEditConfig(client1, editRequest, response);
		assertTrue(response.isOk());
		
		
		
		// session 2 send edit config fail because session 1 is holding the lock
		NetconfClientInfo client2 = new NetconfClientInfo("test", 2);
		m_server.onHello(client2, null);
		editRequest.setMessageId("3");
		response = new NetConfResponse().setMessageId("3");
		m_server.onEditConfig(client2, editRequest, response);
		assertFalse(response.isOk());
		

		// session 1 unlock the datastore
		UnLockRequest unlockRequest = new UnLockRequest();
		unlockRequest.setTarget(StandardDataStores.RUNNING);
		unlockRequest.setMessageId("4");

		NetConfResponse unlockResponse = new NetConfResponse();
		unlockResponse.setMessageId("4");

		m_server.onUnlock(client1, unlockRequest, unlockResponse);

		assertTrue(unlockResponse.isOk());
		
		
		// session 2 send edit config success because session 1 released the lock
		editRequest.setMessageId("5");
		response = new NetConfResponse().setMessageId("5");
		m_server.onEditConfig(client2, editRequest, response);
		assertTrue(response.isOk());
	}
}
