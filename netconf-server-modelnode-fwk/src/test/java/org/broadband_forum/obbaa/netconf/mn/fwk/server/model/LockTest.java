package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.createEmptyJukeBox;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.createJukeBoxModel;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.loadAsXml;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

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
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.jukebox2.Jukebox;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregatorImpl;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;

public class LockTest {
	private NetConfServerImpl m_server;
	private SubSystemRegistry m_subSystemRegistry = new SubSystemRegistryImpl();
    private SchemaRegistry m_schemaRegistry;
    private String m_componentId = "test";
	private ModelNodeHelperRegistry m_modelNodeHelperRegistry = new ModelNodeHelperRegistryImpl(m_schemaRegistry);

	@Before
    public void initServer() throws SchemaBuildException {
        m_schemaRegistry = new SchemaRegistryImpl(TestUtil.getJukeBoxYangs(), Collections.emptySet(), Collections.emptyMap(), new NoLockService());
        m_server = new NetConfServerImpl(m_schemaRegistry);
        RootModelNodeAggregator rootModelNodeAggregator = new RootModelNodeAggregatorImpl(m_schemaRegistry, m_modelNodeHelperRegistry,
				mock(ModelNodeDataStoreManager.class), m_subSystemRegistry).addModelServiceRoot(m_componentId, createEmptyJukeBox(m_modelNodeHelperRegistry, m_subSystemRegistry, m_schemaRegistry));
        DataStore dataStore = new DataStore(StandardDataStores.RUNNING, rootModelNodeAggregator, m_subSystemRegistry);
        NbiNotificationHelper nbiNotificationHelper = mock(NbiNotificationHelper.class);
        dataStore.setNbiNotificationHelper(nbiNotificationHelper);
        m_server.setRunningDataStore(dataStore);
        NetconfClientInfo client = new NetconfClientInfo("test", 1);
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
	public void testOnlyASingleSessionCanHaveLock() {
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

			assertEquals(TestUtil.load("/lockfailed-othersession.xml"), TestUtil.responseToString(response));
			
		}
	}
	
	@Test
	public void testLockUnLockOnNonExistingDatastore(){

		Jukebox jukeBoxModel = createJukeBoxModel(m_modelNodeHelperRegistry, m_subSystemRegistry, m_schemaRegistry);
		RootModelNodeAggregator rootModelNodeAggregator = new RootModelNodeAggregatorImpl(m_schemaRegistry, m_modelNodeHelperRegistry,
				mock(ModelNodeDataStoreManager.class), m_subSystemRegistry).addModelServiceRoot(m_componentId, jukeBoxModel);
		m_server.setRunningDataStore(new DataStore("test", rootModelNodeAggregator, m_subSystemRegistry));
		
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
