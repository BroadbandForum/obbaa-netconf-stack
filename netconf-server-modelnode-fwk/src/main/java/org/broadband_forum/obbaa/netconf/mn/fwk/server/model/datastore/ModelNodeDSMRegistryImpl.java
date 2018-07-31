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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.opendaylight.yangtools.yang.model.api.SchemaPath;

import org.broadband_forum.obbaa.netconf.persistence.EntityDataStoreManager;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.LoggerFactory;

public class ModelNodeDSMRegistryImpl implements ModelNodeDSMRegistry {

    private Map<SchemaPath, ModelNodeDataStoreManager> m_registry = new ConcurrentHashMap<>();
    private final static ModelNodeDSMRegistry INSTANCE = new ModelNodeDSMRegistryImpl();
    private ConcurrentHashMap<String, List<SchemaPath>> m_componentMap = new ConcurrentHashMap<>();
    private static final AdvancedLogger LOGGER = LoggerFactory.getLogger(ModelNodeDSMRegistryImpl.class,
            "netconf-server-datastore", "DEBUG", "GLOBAL");
    private final Map<Class, EntityDataStoreManager> m_entityDSMs = new ConcurrentHashMap<>();

    public ModelNodeDSMRegistryImpl() {
    }

    public static ModelNodeDSMRegistry getInstance() {
        return INSTANCE;
    }

    public void undeploy(String componentId) {
        List<SchemaPath> schemaPathList = m_componentMap.get(componentId);
        if (schemaPathList != null) {
            m_registry.keySet().removeAll(schemaPathList);
        }
        LOGGER.debug("un-registered {}", componentId);
        m_componentMap.remove(componentId);
    }

    @Override
    public Set<ModelNodeDataStoreManager> getAllDSMs() {
        return new HashSet<>(m_registry.values());
    }

    @Override
    public void register(String componentId, SchemaPath schemaPath, ModelNodeDataStoreManager
            modelNodeDataStoreManager) {
        m_registry.put(schemaPath, modelNodeDataStoreManager);
        addSchemaPathList(componentId, schemaPath);
        LOGGER.debug("Registered a ModelNodeDataStoreManager with schemaPath: {}", schemaPath.toString());
    }

    private void addSchemaPathList(String componentId, SchemaPath schemaPath) {
        List<SchemaPath> schemaPathList = m_componentMap.get(componentId);
        if (schemaPathList == null) {
            schemaPathList = new ArrayList<>();
            m_componentMap.putIfAbsent(componentId, schemaPathList);
            schemaPathList = m_componentMap.get(componentId);
        }
        schemaPathList.add(schemaPath);
    }

    @Override
    public ModelNodeDataStoreManager lookupDSM(SchemaPath schemaPath) {
        if (schemaPath != null) {
            ModelNodeDataStoreManager modelNodeDataStoreManager = m_registry.get(schemaPath);
            return modelNodeDataStoreManager;
        }
        return null;
    }

    @Override
    public EntityDataStoreManager getEntityDSM(Class klass) {
        return m_entityDSMs.get(klass);
    }

    @Override
    public void addEntityDSM(Class klass, EntityDataStoreManager entityDSM) {
        m_entityDSMs.put(klass, entityDSM);
    }
}
