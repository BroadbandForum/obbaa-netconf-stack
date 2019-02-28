package org.broadband_forum.obbaa.netconf.mn.fwk.schema;

import java.util.Map;

import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public interface SchemaMountRegistry {

    void register(SchemaPath schemaPath, SchemaMountRegistryProvider provider);

    void deregister(SchemaPath schemaPath);

    SchemaMountRegistryProvider getProvider(SchemaPath schemaPath);
    
    Map<SchemaPath, SchemaMountRegistryProvider> getProviders();
}
