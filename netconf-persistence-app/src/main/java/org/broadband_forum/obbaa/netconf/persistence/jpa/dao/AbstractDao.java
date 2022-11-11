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

package org.broadband_forum.obbaa.netconf.persistence.jpa.dao;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;

import org.broadband_forum.obbaa.netconf.persistence.EntityDataStoreManager;
import org.broadband_forum.obbaa.netconf.persistence.PagingInput;
import org.broadband_forum.obbaa.netconf.persistence.PersistenceManagerUtil;

public abstract class AbstractDao<E, PK extends Serializable> implements EntityDAO<E, PK> {
    private final transient Class<E> m_entityClass;

    private PersistenceManagerUtil m_persistenceManagerUtil;

    public AbstractDao(PersistenceManagerUtil persistenceManagerUtil, Class<E> entityClass) {
        this.m_entityClass = entityClass;
        this.m_persistenceManagerUtil = persistenceManagerUtil;
    }

    protected Class<E> getEntityClass(){
        return m_entityClass;
    }
    
    protected EntityDataStoreManager getPersistenceManager() {
        return m_persistenceManagerUtil.getEntityDataStoreManager();
    }

    protected EntityManager getEntityManager() {
        return getPersistenceManager().getEntityManager();
    }

    @Override
    public boolean merge(E entity) {
        EntityDataStoreManager entityDataStoreManager = getPersistenceManager();
        boolean result = false;
        result = entityDataStoreManager.merge(entity);
        return result;
    }

    @Override
    @Deprecated
    public E findById(PK primaryKey) {
    	return findByIdWithReadLock(primaryKey);
    }
    @Override
    public E findByIdWithWriteLock(PK primaryKey) {
        return findById(primaryKey, LockModeType.PESSIMISTIC_WRITE);
    }
    
    @Override
    public E findByIdWithReadLock(PK primaryKey){
    	return findById(primaryKey, LockModeType.PESSIMISTIC_READ);
    }

    @Override
    public List<E> findAll() {
        return getPersistenceManager().findAll(m_entityClass, getDefaultOrderByColumns());
    }

    @Override
    public void create(E entity) {
        EntityDataStoreManager entityDataStoreManager = getPersistenceManager();
        entityDataStoreManager.create(entity);
    }

    public E findById(PK pk, LockModeType lockModeType){
        return getPersistenceManager().findById(m_entityClass, pk, lockModeType);
    }

    @Override
    public void delete(E entity) {
        getPersistenceManager().delete(entity);
    }

    @Override
    public int deleteAll() {
        EntityDataStoreManager entityDataStoreManager = getPersistenceManager();
        int result = 0;
        result = entityDataStoreManager.deleteAll(m_entityClass);
        return result;
    }

    @Override
    public boolean deleteById(PK id) {
        return getPersistenceManager().deleteById(m_entityClass, id);
    }

    @Override
    public List<E> findWithPaging(PagingInput pagingInput) {
        return getPersistenceManager().findWithPaging(m_entityClass, pagingInput);
    }
    
    @Override
    public List<E> findWithPagingAndOrderByColumn(PagingInput pagingInput, Map<String, Object> matchValues, String orderByColumn,
            Boolean isDesc) {
        return getPersistenceManager().findWithPagingAndOrderByColumn(m_entityClass, pagingInput, matchValues, orderByColumn, isDesc);
    }
    /**
     * The DAO of a Entity must override this method and supply a list of java entity attribute names
     * to return a system ordered list always
     */
    protected List<String> getDefaultOrderByColumns() {
        return Collections.emptyList();
    }

}
