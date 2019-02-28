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

import org.broadband_forum.obbaa.netconf.api.messages.LogUtil;
import org.broadband_forum.obbaa.netconf.persistence.EMFactory;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import org.apache.log4j.Logger;


@Deprecated
public class JPAEntityDataStoreManager extends AbstractEntityDataStoreManager{
	protected EntityManager m_manager;
    private static final Logger LOGGER = Logger.getLogger(JPAEntityDataStoreManager.class);


	public JPAEntityDataStoreManager(EMFactory factory) {
        this();
		m_manager = factory.createNewEntityManager();
	}

    public JPAEntityDataStoreManager() {
	}

    @Override
    public <E> E findById(Class<E> entityClass, Object primaryKey) {
    	return  getEntityManager().find(entityClass, primaryKey);
    }

	@Override
	public EntityManager getEntityManager() {
		return m_manager;
	}

	public void beginTransaction() {
        m_manager.getTransaction().begin();
	}

	public void commitTransaction() {
        if (m_manager.getTransaction().isActive()){
            m_manager.getTransaction().commit();
        }
	}

	public void rollbackTransaction() {
        if(m_manager.getTransaction().isActive()){
            m_manager.getTransaction().rollback();
        }
	}
	
	public void close() {
		if(m_manager != null && m_manager.isOpen()) {
			m_manager.close();
		}
	}
	
    @Override
    public <E> E findById(Class<E> entityClass, Object primaryKey, LockModeType lockMode) {
        E entity = getEntityManager().find(entityClass, primaryKey);
        return entity;
    }

    @Override
    public <E> boolean merge(E entity) {
        LogUtil.logDebug(LOGGER, "Update entity called for : %s", entity);
        m_manager.merge(entity);
        LogUtil.logDebug(LOGGER, "Update entity returned for : %s", entity);
        return true;
    }

    @Override
    public void persist(Object entity) {
        LogUtil.logDebug(LOGGER, "persist entity called for : %s", entity);
        m_manager.persist(entity);
        LogUtil.logDebug(LOGGER, "persist entity returned for : %s", entity);
    }

    @Override
    public <E> boolean create(E entity) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("create entity: " + entity);
        }
        persist(entity);
        return true;
    }
}
