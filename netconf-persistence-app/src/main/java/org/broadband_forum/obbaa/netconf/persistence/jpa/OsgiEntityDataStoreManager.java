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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.metamodel.Metamodel;
import javax.transaction.Transactional;

import org.apache.log4j.Logger;


/**
 * An EntityDataStoreManager for OSGi.
 * Created by keshava on 2/19/16.
 */
@Transactional
public class OsgiEntityDataStoreManager extends AbstractEntityDataStoreManager {

    private static final Logger LOGGER = Logger.getLogger(OsgiEntityDataStoreManager.class);

    @PersistenceContext(unitName = "baa")
    private EntityManager m_entityManager;


    @Override
    public void beginTransaction() {
        //nothing to be done, container takes care of this
    }

    @Override
    public void commitTransaction() {
        //nothing to be done, container takes care of this
    }

    @Override
    public void rollbackTransaction() {
        //nothing to be done, container takes care of this
    }

    @Override
    public void close() {
        //nothing to be done, container takes care of this
    }

    @Override
    public EntityManager getEntityManager() {
        return m_entityManager;
    }

    /**
     * For unit Tests
     *
     * @param entityManager
     */
    protected void setEntityManager(EntityManager entityManager) {
        m_entityManager = entityManager;
    }

    @Override
    public Metamodel getMetaModel() {
        return m_entityManager.getMetamodel();
    }

    /**
     * Works only against MaraiDB - Used only for LOGGING purpose
     */
  /*  @Override
    public void dumpModifiedSessionVariables() {
        Query query = getEntityManager().createNativeQuery("select @@tx_isolation");
        List objects = query.getResultList();
        if (objects.size() == 1) {
            LOGGER.info(String.format("Current session isolation level to DB is %s", objects.get(0)));
        } else {
            LOGGER.info("Could not retrieve session isolation level from DB");
        }

        query = getEntityManager().createNativeQuery("select @@innodb_lock_wait_timeout");
        objects = query.getResultList();
        if (objects.size() == 1) {
            LOGGER.info(String.format("Current lock wait timeout in DB is %s", objects.get(0)));
        } else {
            LOGGER.info("Could not retrieve lock wait timeout from DB");
        }

    }*/

}
