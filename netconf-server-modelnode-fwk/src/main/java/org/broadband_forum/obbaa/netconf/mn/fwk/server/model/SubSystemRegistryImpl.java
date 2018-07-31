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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.opendaylight.yangtools.yang.model.api.SchemaPath;

import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.LoggerFactory;

public class SubSystemRegistryImpl implements SubSystemRegistry {
    private ConcurrentHashMap<SchemaPath, SubSystem> m_registry = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, List<SchemaPath>> m_componentMap = new ConcurrentHashMap<>();
    private static final AdvancedLogger LOGGER = LoggerFactory.getLogger(SubSystemRegistryImpl.class,
            "netconf-server-datastore", "DEBUG", "GLOBAL");

    public void register(String componentId, SchemaPath schemaPath, SubSystem subSystem) {
        m_registry.put(schemaPath, subSystem);
        List<SchemaPath> schemaPathList = m_componentMap.get(componentId);
        if (schemaPathList == null) {
            schemaPathList = new ArrayList<>();
            m_componentMap.putIfAbsent(componentId, schemaPathList);
            schemaPathList = m_componentMap.get(componentId);
        }
        schemaPathList.add(schemaPath);

        LOGGER.debug("Registered a subsystem with schema-path: {}", schemaPath);
    }

    public SubSystem lookupSubsystem(SchemaPath schemaPath) {
        SubSystem system = m_registry.get(schemaPath);
        if (system == null) {
            return NoopSubSystem.c_instance;
        }
        return system;
    }

    @Override
    public void undeploy(String componentId) {

        List<SchemaPath> schemaPathList = m_componentMap.get(componentId);
        if (schemaPathList != null) {
            for (SchemaPath schemaPath : schemaPathList) {
                SubSystem subSystem = m_registry.remove(schemaPath);
                if (subSystem != null) {
                    LOGGER.debug("Unregistered a subsystem with schema-path: {}", schemaPath);
                }
            }
        }
        m_componentMap.remove(componentId);
    }
}
