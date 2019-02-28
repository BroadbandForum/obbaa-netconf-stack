package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn;

import java.util.Collections;
import java.util.Set;

import org.opendaylight.yangtools.yang.model.api.SchemaPath;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDSMRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.persistence.EntityDataStoreManager;

public class ThreadLocalDSMRegistry implements ModelNodeDSMRegistry {

    private static ThreadLocal<ModelNodeDataStoreManager> c_dsm = new ThreadLocal<>();
    private EntityDataStoreManager m_entityDsm;

    public ThreadLocalDSMRegistry() {
    }
    public ThreadLocalDSMRegistry(EntityDataStoreManager entityDsm) {
        this.m_entityDsm = entityDsm;
    }
    public static void setDsm(ModelNodeDataStoreManager dsm) {
        c_dsm.set(dsm);
    }

    @Override
    public Set<ModelNodeDataStoreManager> getAllDSMs() {
        return Collections.singleton(c_dsm.get());
    }

    @Override
    public void register(String componentId, SchemaPath schemaPath, ModelNodeDataStoreManager modelNodeDataStoreManager) {
    }

    @Override
    public ModelNodeDataStoreManager lookupDSM(SchemaPath schemaPath) {
        return c_dsm.get();
    }

    @Override
    public void undeploy(String componentId) {
    }

    @Override
    public void addEntityDSM(Class klass, EntityDataStoreManager entityDSM) {
    }

    @Override
    public EntityDataStoreManager getEntityDSM(Class klass) {
        return m_entityDsm;
    }

    public static void clearDsm() {
        c_dsm.remove();
    }
}
