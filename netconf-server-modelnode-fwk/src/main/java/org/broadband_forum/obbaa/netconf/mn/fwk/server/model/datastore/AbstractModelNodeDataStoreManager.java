package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore;

import org.broadband_forum.obbaa.netconf.persistence.EntityDataStoreManager;
import org.broadband_forum.obbaa.netconf.persistence.PersistenceManagerUtil;


public abstract class AbstractModelNodeDataStoreManager {

	private PersistenceManagerUtil m_persistenceManagerUtil;

	public AbstractModelNodeDataStoreManager(PersistenceManagerUtil persistenceManagerUtil) {
		this.m_persistenceManagerUtil = persistenceManagerUtil;
	}

	protected EntityDataStoreManager getPersistenceManager(){
		return m_persistenceManagerUtil.getEntityDataStoreManager();
	}
}
