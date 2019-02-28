package org.broadband_forum.obbaa.netconf.mn.fwk.schema;

import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil;
import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDSMRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;

public interface SchemaMountRegistryProvider {

    public static String MOUNT_DSM_REGISTRY = "MOUNT_DSM_REGISTRY";
    public static String MOUNT_SCHEMA_REGISTRY = SchemaRegistryUtil.MOUNT_CONTEXT_SCHEMA_REGISTRY;
    public static String MOUNT_HELPER_REGISTRY = "MOUNT_HELPER_REGISTRY";
    public static String MOUNT_CURRENT_SCOPE = "MOUNT_CURRENT_SCOPE";
    public static String PLUG_CONTEXT = "plugContext";

    SchemaRegistry getSchemaRegistry(ModelNode modelNode, Object...objects) throws GetException;
    SchemaRegistry getSchemaRegistry(Map<String, String> keyValues);
    SchemaRegistry getSchemaRegistry(String mountKey);
    ModelNodeHelperRegistry getModelNodeHelperRegistry(ModelNode modelNode, Object...objects);
    SubSystemRegistry getSubSystemRegistry(ModelNode modelNode, Object...objects);
    ModelNodeDataStoreManager getMountDSM(ModelNode modelNode, Object...objects);
    SchemaMountKey getSchemaMountKey();
    
    List<MountContext> getMountContexts();
    public static SchemaRegistry getCurrentSchemaRegistry() {
        @SuppressWarnings("rawtypes")
		Map currentScopeMap = (Map) RequestScope.getCurrentScope().getFromCache(MOUNT_CURRENT_SCOPE);
        if (currentScopeMap!=null) {
            return (SchemaRegistry) currentScopeMap.get(MOUNT_SCHEMA_REGISTRY);
        }
        return null;
    }
    
    public static ModelNodeDSMRegistry getCurrentDsmRegistry() {
        @SuppressWarnings("rawtypes")
		Map currentScopeMap = (Map) RequestScope.getCurrentScope().getFromCache(MOUNT_CURRENT_SCOPE);
        if (currentScopeMap!=null) {
            return (ModelNodeDSMRegistry) currentScopeMap.get(MOUNT_DSM_REGISTRY);
        }
        return null;
        
    }

    public static ModelNodeHelperRegistry getCurrentHelperRegistry() {
        @SuppressWarnings("rawtypes")
		Map currentScopeMap = (Map) RequestScope.getCurrentScope().getFromCache(MOUNT_CURRENT_SCOPE);
        if (currentScopeMap!=null) {
            return (ModelNodeHelperRegistry) currentScopeMap.get(MOUNT_HELPER_REGISTRY);
        }
        return null;
        
    }

}
