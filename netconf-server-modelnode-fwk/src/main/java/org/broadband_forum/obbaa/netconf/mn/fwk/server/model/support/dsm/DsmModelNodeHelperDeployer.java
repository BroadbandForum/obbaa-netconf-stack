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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.dsm;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AnyDataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildContainerHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildLeafListHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildListHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigAttributeHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.AbstractModelNodeHelperDeployer;

/**
 * Deployes DSM Based Helpers into ModelNodeHelperRegistry. Created by pgorai on 3/2/16.
 */
public class DsmModelNodeHelperDeployer extends AbstractModelNodeHelperDeployer {
    private final SchemaRegistry m_schemaRegistry;
    private final ModelNodeDataStoreManager m_modelNodeDSM;
    private final ModelNodeHelperRegistry m_modelNodeHelperRegistry;
    private final SubSystemRegistry m_subSystemRegistry;

    public DsmModelNodeHelperDeployer(SchemaRegistry schemaRegistry, ModelNodeDataStoreManager modelNodeDSM,
            ModelNodeHelperRegistry modelNodeHelperRegistry, SubSystemRegistry subSystemRegistry) {
        super(schemaRegistry);
        this.m_schemaRegistry = schemaRegistry;
        this.m_modelNodeDSM = modelNodeDSM;
        this.m_modelNodeHelperRegistry = modelNodeHelperRegistry;
        m_subSystemRegistry = subSystemRegistry;
    }

    private void registerConfigAttributeHelper(String componentId, SchemaPath parentSchemaPath, LeafSchemaNode leafSchemaNode) {
        if (leafSchemaNode.isConfiguration()) {
            ConfigAttributeHelper helper = new DsmConfigAttributeHelper(m_modelNodeDSM, m_schemaRegistry, leafSchemaNode,
                    leafSchemaNode.getQName());
            m_modelNodeHelperRegistry.registerConfigAttributeHelper(componentId, parentSchemaPath, leafSchemaNode.getQName(), helper);
        }
    }

    private void registerNaturalKeyHelpers(String componentId, SchemaPath schemaPath, LeafSchemaNode keyLeafSchemaNode) {
        ConfigAttributeHelper helper = new DsmConfigAttributeHelper(m_modelNodeDSM, m_schemaRegistry, keyLeafSchemaNode,
                keyLeafSchemaNode.getQName());
        m_modelNodeHelperRegistry.registerNaturalKeyHelper(componentId, schemaPath, keyLeafSchemaNode.getQName(), helper);
    }

    @Override
    public void visitLeafListNode(String componentId, SchemaPath parentSchemaPath, LeafListSchemaNode leafListNode) {
        if (parentSchemaPath != null && leafListNode.isConfiguration()) {
            QName qname = leafListNode.getQName();
            ChildLeafListHelper helper = new DsmChildLeafListHelper(leafListNode, qname, m_modelNodeDSM, m_schemaRegistry);
            m_modelNodeHelperRegistry.registerConfigLeafListHelper(componentId, parentSchemaPath, qname, helper);
        }
    }

    @Override
    public void visitLeafNode(String componentId, SchemaPath parentSchemaPath, LeafSchemaNode leafSchemaNode) {
        if (parentSchemaPath != null) {
            registerConfigAttributeHelper(componentId, parentSchemaPath, leafSchemaNode);
            if(isKeyLeafSchemaNode(parentSchemaPath, leafSchemaNode)) {
                registerNaturalKeyHelpers(componentId, parentSchemaPath, leafSchemaNode);
            }
        }
    }

    @Override
    public void visitChoiceCaseNode(String componentId, SchemaPath parentPath, CaseSchemaNode choiceCaseNode) {
    }

    @Override
    public void visitChoiceNode(String componentId, SchemaPath parentPath, ChoiceSchemaNode choiceSchemaNode) {
    }

    @Override
    public void visitAnyXmlNode(String componentId, SchemaPath parentPath, AnyXmlSchemaNode anyXmlSchemaNode) {
        // TODO:FNMS-859
    }
    
    @Override
    public void visitAnyDataNode(String componentId, SchemaPath parentPath, AnyDataSchemaNode anyDataSchemaNode) {        
        // TODO:FNMS-859
    }

    @Override
    public void visitListNode(String componentId, SchemaPath parentSchemaPath, ListSchemaNode listSchemaNode) {
        if (parentSchemaPath != null && listSchemaNode.isConfiguration()) {
            QName qname = listSchemaNode.getQName();
            ChildListHelper helper;
            helper = new DsmListModelNodeHelper(listSchemaNode, m_modelNodeHelperRegistry, m_modelNodeDSM, m_schemaRegistry,
                    m_subSystemRegistry);
            m_modelNodeHelperRegistry.registerChildListHelper(componentId, parentSchemaPath, qname, helper);
        }
    }

    @Override
    public void visitContainerNode(String componentId, SchemaPath parentSchemaPath, ContainerSchemaNode containerSchemaNode) {
        if (parentSchemaPath != null && containerSchemaNode.isConfiguration()) {
            QName qname = containerSchemaNode.getQName();
            ChildContainerHelper helper;
            helper = new DsmContainerModelNodeHelper(containerSchemaNode, m_modelNodeDSM, m_schemaRegistry);
            m_modelNodeHelperRegistry.registerChildContainerHelper(componentId, parentSchemaPath, qname, helper);
        }

    }

    @Override
    public void visitIdentityNode(String componentId, IdentitySchemaNode identitySchemaNode) {

    }

    @Override
    public void visitEnter(String componentId, SchemaPath parentPath, SchemaPath schemaPath) {

    }

    @Override
    public void visitLeave(String componentId, SchemaPath parentPath, SchemaPath schemaPath) {

    }
}
