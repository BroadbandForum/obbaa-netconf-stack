package org.broadband_forum.obbaa.netconf.samples.jb.persistence;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.broadband_forum.obbaa.netconf.persistence.jpa.AbstractEntityDataStoreManager;

@Transactional
public class OsgiEntityDataStoreManager extends AbstractEntityDataStoreManager {

    @PersistenceContext(unitName="jb")
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
     * @param entityManager
     */
    protected void setEntityManager(EntityManager entityManager) {
        m_entityManager = entityManager;
    }
}
