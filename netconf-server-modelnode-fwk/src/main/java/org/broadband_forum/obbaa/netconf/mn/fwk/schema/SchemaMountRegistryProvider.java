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

import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.w3c.dom.Element;

public interface SchemaMountRegistryProvider {

    String MOUNT_DSM_REGISTRY = "MOUNT_DSM_REGISTRY";
    String MOUNT_HELPER_REGISTRY = "MOUNT_HELPER_REGISTRY";
    String MOUNT_CURRENT_SCOPE = "MOUNT_CURRENT_SCOPE";
    String PLUG_CONTEXT = "plugContext";

    SchemaRegistry getSchemaRegistry(ModelNodeId modelNodeId);
    SchemaRegistry getSchemaRegistry(Element element);
    SchemaRegistry getSchemaRegistry(EditContainmentNode editContainmentNode);
    SchemaRegistry getSchemaRegistry(Map<String, String> keyValues);
    SchemaRegistry getSchemaRegistry(String mountKey);

    ModelNodeHelperRegistry getModelNodeHelperRegistry(ModelNodeId modelNodeId);
    ModelNodeHelperRegistry getModelNodeHelperRegistry(Element element);
    ModelNodeHelperRegistry getModelNodeHelperRegistry(EditContainmentNode editContainmentNode);

    SubSystemRegistry getSubSystemRegistry(ModelNodeId modelNodeId);

    SchemaMountKey getSchemaMountKey();
    MountRegistries getMountRegistries(String mountkey);
    void setCorrectPlugMountContextInCache(EditContainmentNode node);
    List<MountContext> getMountContexts();

    boolean isValidMountPoint(ModelNodeId nodeID);

    static boolean isNotNullAndNotEmpty(String deviceId) {
        return deviceId != null && !deviceId.isEmpty();
    }

}