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

package org.broadband_forum.obbaa.netconf.mn.fwk.schema;

import java.util.Collections;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDSMRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDSMRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.AddDefaultDataInterceptor;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DSExpressionValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.ReadWriteLockService;

public class MountRegistries {
    
    private SubSystemRegistry m_subSystemRegistry;
    private SchemaRegistry m_schemaRegistry;
    private ModelNodeDSMRegistry m_dsmRegistry;
    private ModelNodeHelperRegistry m_modelNodeHelperRegistry;
    private ReadWriteLockService m_readWriteLockService;
    private RootModelNodeAggregator m_rootModelNodeAggregator;
    
    public MountRegistries(ReadWriteLockService readWriteLockService) {
        m_readWriteLockService = readWriteLockService;
    }
    
    public SubSystemRegistry getSubSystemRegistry() {
        if (m_subSystemRegistry == null) {
            m_subSystemRegistry = new SubSystemRegistryImpl();
        }
        return m_subSystemRegistry;
    }
    public SchemaRegistry getSchemaRegistry() {
        if (m_schemaRegistry == null) {
            try {
                m_schemaRegistry = new SchemaRegistryImpl(Collections.emptyList(), Collections.emptySet(), Collections.emptyMap(), m_readWriteLockService);
            } catch (SchemaBuildException e) {
                throw new RuntimeException("Unable to get mount schema registry",e);
            }
        }
        return m_schemaRegistry;
    }

    public SchemaRegistry getSchemaRegistry(boolean isYangLibraryIgnored) {
        if (m_schemaRegistry == null) {
            try {
                if (!isYangLibraryIgnored) {
                    m_schemaRegistry = new SchemaRegistryImpl(Collections.emptyList(), Collections.emptySet(), Collections.emptyMap(),
                            m_readWriteLockService);
                } else {
                    m_schemaRegistry = new SchemaRegistryImpl(Collections.emptyList(), null, null,
                            m_readWriteLockService, true);
                }
            } catch (SchemaBuildException e) {
                throw new RuntimeException("Unable to get mount schema registry", e);
            }
        }
        return m_schemaRegistry;
    }

    public ModelNodeDSMRegistry getDsmRegistry() {
        if (m_dsmRegistry == null) {
            m_dsmRegistry = new ModelNodeDSMRegistryImpl();
        }
        return m_dsmRegistry;
    }
    
    public ModelNodeHelperRegistry getModelNodeHelperRegistry()  {
        if (m_modelNodeHelperRegistry == null) {
            m_modelNodeHelperRegistry = new ModelNodeHelperRegistryImpl(getSchemaRegistry());
            DSExpressionValidator validator = new DSExpressionValidator(getSchemaRegistry(), m_modelNodeHelperRegistry, getSubSystemRegistry());
            AddDefaultDataInterceptor interceptor = new AddDefaultDataInterceptor(m_modelNodeHelperRegistry, getSchemaRegistry(), validator);
            m_modelNodeHelperRegistry.setDefaultCapabilityCommandInterceptor(interceptor);
        }
        return m_modelNodeHelperRegistry;
    }
    
    public RootModelNodeAggregator getRootModelNodeAggregator() {
        return m_rootModelNodeAggregator;
    }

    public void setRootModelNodeAggregator(RootModelNodeAggregator rootModelNodeAggregator) {
        m_rootModelNodeAggregator = rootModelNodeAggregator;
    }
}
