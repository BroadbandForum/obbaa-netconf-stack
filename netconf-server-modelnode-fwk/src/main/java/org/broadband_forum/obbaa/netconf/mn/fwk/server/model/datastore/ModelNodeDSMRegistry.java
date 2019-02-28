package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore;

import java.util.Set;

import org.opendaylight.yangtools.yang.model.api.SchemaPath;

import org.broadband_forum.obbaa.netconf.persistence.EntityDataStoreManager;

public interface ModelNodeDSMRegistry {
    Set<ModelNodeDataStoreManager> getAllDSMs();
    void register(String componentId, SchemaPath schemaPath, ModelNodeDataStoreManager modelNodeDataStoreManager);
    ModelNodeDataStoreManager lookupDSM(SchemaPath schemaPath);
    void undeploy(String componentId);
    void addEntityDSM(Class klass, EntityDataStoreManager entityDSM);
    EntityDataStoreManager getEntityDSM(Class klass);
}
