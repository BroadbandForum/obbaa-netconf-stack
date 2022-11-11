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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

public class MountedRootModelNodeAggregator extends RootModelNodeAggregatorImpl {
    
    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(MountedRootModelNodeAggregator.class, LogAppNames.NETCONF_STACK);

    public MountedRootModelNodeAggregator(SchemaRegistry schemaRegistry,
            ModelNodeHelperRegistry modelNodeHelperRegistry, ModelNodeDataStoreManager dataStoreManager,
            SubSystemRegistry subsystemRegistry) {
        super(schemaRegistry, modelNodeHelperRegistry, dataStoreManager, subsystemRegistry);
    }
    
    @Override
    public List<ModelNode> getModuleRootFromHelpers(String requiredElementNamespace, String requiredElementLocalName, ModelNode modelNode) {
        List<ModelNode> rootNodes = new ArrayList<>();
        for (ChildContainerHelper helper : m_rootContainerHelpers.values()) {
            ModelNode rootNode = null;
            try {
                DataSchemaNode helperSchemaNode = helper.getSchemaNode();
                if (helperSchemaNode != null) {
                    QName helperQName = helperSchemaNode.getQName();
                    if (helperQName.getLocalName().equals(requiredElementLocalName) && helperQName.getNamespace().toString().equals(requiredElementNamespace)) {
                        if (helperSchemaNode.isConfiguration()) {
                            rootNode = helper.getValue(modelNode);
                            if ( rootNode != null){
                                return Arrays.asList(rootNode);    
                            }
                        } 
                    }
                }
            } catch (ModelNodeGetException e) {
                LOGGER.error("Error while getting root node from helpers", e);
            }
        }
        for (ChildListHelper helper : m_rootListHelpers.values()) {
            try {
                ListSchemaNode helperSchemaNode = helper.getSchemaNode();
                if (helperSchemaNode != null) {
                    QName helperQName = helperSchemaNode.getQName();
                    if (helperQName.getLocalName().equals(requiredElementLocalName) && helperQName.getNamespace().toString().equals(requiredElementNamespace)) {
                        rootNodes.addAll(helper.getValue(modelNode, Collections.emptyMap()));
                    }
                }
            } catch (ModelNodeGetException e) {
                LOGGER.error("Error while getting root node from helpers", e);
            }
        }
        return rootNodes;
    }

}
