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

package org.broadband_forum.obbaa.netconf.mn.fwk.util;

import java.util.Collection;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.MountRegistries;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaMountRegistryProvider;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDSMRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildContainerHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildListHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootEntityContainerModelNodeHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootEntityListModelNodeHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregator;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public class SchemaMountUtil {

    public static void addRootNodeHelpers(SchemaRegistry schemaRegistry, SubSystemRegistry subSystemRegistry, ModelNodeHelperRegistry modelNodeHelperRegistry, ModelNodeDataStoreManager modelNodeDsm, 
            Collection<SchemaPath> subtreeRootPaths, RootModelNodeAggregator rootModelNodeAggregator) {
        if (subtreeRootPaths != null) {
            for (SchemaPath rootSchemaPath : subtreeRootPaths) {
                if (!rootSchemaPath.getParent().getPathFromRoot().iterator().hasNext()) {
                    DataSchemaNode rootNode = schemaRegistry.getDataSchemaNode(rootSchemaPath);
                    if (!rootNode.isConfiguration() && !(rootNode instanceof ContainerSchemaNode)) {
                        throw new RuntimeException("Cannot load non-config node.");
                    } else {
                        if (rootNode instanceof ContainerSchemaNode) {
                            ChildContainerHelper childContainerHelper = new RootEntityContainerModelNodeHelper(
                                    (ContainerSchemaNode) schemaRegistry.getDataSchemaNode(rootSchemaPath), modelNodeHelperRegistry,
                                    subSystemRegistry, schemaRegistry, modelNodeDsm);
                            rootModelNodeAggregator.addModelServiceRootHelper(rootSchemaPath, childContainerHelper);
                        } else {
                            ChildListHelper childListHelper = new RootEntityListModelNodeHelper(
                                    (ListSchemaNode) schemaRegistry.getDataSchemaNode(rootSchemaPath), modelNodeHelperRegistry,
                                    modelNodeDsm, schemaRegistry, subSystemRegistry);
                            rootModelNodeAggregator.addModelServiceRootHelper(rootSchemaPath, childListHelper);
                        }
                    }
                }
            }
        }
    }
    
    public static RootModelNodeAggregator getMountedRootModelNodeAggregator( SchemaRegistry mountRegistry){
    	MountRegistries mountRegistries = getMountRegistries(mountRegistry);
    	if ( mountRegistries != null ){
    		return mountRegistries.getRootModelNodeAggregator();
    	}
        return null;
    }
    
    public static SubSystemRegistry getSubSystemRegistry( SchemaRegistry mountRegistry){
    	MountRegistries mountRegistries = getMountRegistries(mountRegistry);
    	if ( mountRegistries != null ){
    		return mountRegistries.getSubSystemRegistry();
    	}
        return null;
    }
    
    public static ModelNodeDSMRegistry getMountModelNodeDSMRegistry(SchemaRegistry mountRegistry){
    	MountRegistries mountRegistries = getMountRegistries(mountRegistry);
    	if ( mountRegistries != null ){
    		return mountRegistries.getDsmRegistry();
    	}
    	return null;
    }

	private static MountRegistries getMountRegistries(SchemaRegistry mountRegistry) {
		if ( mountRegistry.getParentRegistry() != null){
    		SchemaMountRegistryProvider mountRegistryProvider = mountRegistry.getParentRegistry().getMountRegistry().getProvider(mountRegistry.getMountPath());
    		return mountRegistryProvider.getMountRegistries(mountRegistry.getName());
    	}
		return null;
	}
}
