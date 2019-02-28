package org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities;

import static org.mockito.Mockito.spy;

import org.broadband_forum.obbaa.netconf.persistence.EMFactory;
import org.broadband_forum.obbaa.netconf.persistence.EntityDataStoreManager;
import org.broadband_forum.obbaa.netconf.persistence.jpa.ThreadLocalPersistenceManagerUtil;

public class SpyingThreadLocalPersistenceManagerUtil extends ThreadLocalPersistenceManagerUtil {
    private EntityDataStoreManager m_spy;

    public SpyingThreadLocalPersistenceManagerUtil(EMFactory entityManagerFactory) {
        super(entityManagerFactory);
    }

    @Override
    protected EntityDataStoreManager createEntityDataStoreManager() {
        EntityDataStoreManager entityDataStoreManager = super.createEntityDataStoreManager();
        m_spy = spy(entityDataStoreManager);
        return m_spy;
    }

    public EntityDataStoreManager getSpy() {
        return m_spy;
    }
}
