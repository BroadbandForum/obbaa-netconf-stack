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

package org.broadband_forum.obbaa.netconf.persistence.jpa;

import org.broadband_forum.obbaa.netconf.persistence.EMFactory;
import org.broadband_forum.obbaa.netconf.persistence.EntityDataStoreManager;
import org.broadband_forum.obbaa.netconf.persistence.PersistenceManagerUtil;

/**
 * Created by keshava on 2/19/16.
 */
public abstract class AbstractThreadLocalPersistenceManagerUtil implements PersistenceManagerUtil {
    private final EMFactory m_entityManagerFactory;
    ThreadLocalPersistenceManager m_threadLocalPersistenceManager = new ThreadLocalPersistenceManager();

    public AbstractThreadLocalPersistenceManagerUtil(EMFactory entityManagerFactory) {
        m_entityManagerFactory = entityManagerFactory;
    }

    @Override
    public EntityDataStoreManager getEntityDataStoreManager() {
        return m_threadLocalPersistenceManager.get();
    }

    @Override
    public void closePersistenceManager() {
        m_threadLocalPersistenceManager.remove();
    }

    private class ThreadLocalPersistenceManager extends ThreadLocal<EntityDataStoreManager> {
        @Override
        public EntityDataStoreManager get() {
            EntityDataStoreManager pm = super.get();
            //if it is not set or not open, create a new one
            if (pm == null || !pm.isOpen()) {
                m_threadLocalPersistenceManager.remove();
                pm = createEntityDataStoreManager();
                set(pm);
            }
            return pm;
        }

        public void remove() {
            //close before removing
            EntityDataStoreManager pm = super.get();
            if (pm != null) {
                if (pm.isOpen()) {
                    pm.close();
                }
            }
            super.remove();
        }
    }

    public EMFactory getEntityManagerFactory() {
        return m_entityManagerFactory;
    }

    protected abstract EntityDataStoreManager createEntityDataStoreManager();
}
