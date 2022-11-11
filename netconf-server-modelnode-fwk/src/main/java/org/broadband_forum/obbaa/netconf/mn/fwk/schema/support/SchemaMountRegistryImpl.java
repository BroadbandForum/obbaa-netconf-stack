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

package org.broadband_forum.obbaa.netconf.mn.fwk.schema.support;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaMountRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaMountRegistryProvider;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

/**
 * Has a collection of Providers for various schema mounts. Each provider will give the SchemaRegistry to be used for the mount point
 *
 */
public class SchemaMountRegistryImpl implements SchemaMountRegistry {

    private static AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(SchemaMountRegistryImpl.class, LogAppNames.NETCONF_STACK);

    private Map<SchemaPath, SchemaMountRegistryProvider> m_providers = new ConcurrentHashMap<>();

    public SchemaMountRegistryImpl() {
    }

    @Override
    public void register(SchemaPath schemaPath, SchemaMountRegistryProvider provider) {
        m_providers.putIfAbsent(schemaPath, provider);
        LOGGER.info(" provider {} registered for {}", provider, schemaPath);
    }

    @Override
    public SchemaMountRegistryProvider getProvider(SchemaPath schemaPath) {
        return m_providers.get(schemaPath);
    }

    @Override
    public void deregister(SchemaPath schemaPath) {
        m_providers.remove(schemaPath);
        
    }
    
    @Override
    public Map<SchemaPath, SchemaMountRegistryProvider> getProviders() {
        return m_providers;
    }

}
