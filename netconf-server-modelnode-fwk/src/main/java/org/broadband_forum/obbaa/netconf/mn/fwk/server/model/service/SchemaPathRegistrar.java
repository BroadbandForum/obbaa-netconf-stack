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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.service;

import java.util.Map;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationPathBuilder;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryVisitor;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ConstraintDefinition;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;

import org.broadband_forum.obbaa.netconf.api.logger.NetconfExtensions;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;

public class SchemaPathRegistrar implements SchemaRegistryVisitor {

    private final SchemaRegistry m_schemaRegistry;
    private final DataStoreValidationPathBuilder m_pathBuilder;

    public SchemaPathRegistrar(SchemaRegistry schemaRegistry, ModelNodeHelperRegistry modelNodeHelperRegistry) {
        m_schemaRegistry = schemaRegistry;
        m_pathBuilder = new DataStoreValidationPathBuilder(schemaRegistry, modelNodeHelperRegistry);
    }

    private void registerConstraintSchemaPaths(String componentId, DataSchemaNode dataSchemaNode) {
        ConstraintDefinition constraints = dataSchemaNode.getConstraints();
        if (constraints != null) {
            RevisionAwareXPath xPath = constraints.getWhenCondition();
            if (xPath != null) {
                registerSchemaPaths(componentId, dataSchemaNode, xPath);
            }
            Set<MustDefinition> mustConstraints = constraints.getMustConstraints();
            if (mustConstraints != null) {
                for (MustDefinition mustConstraint : mustConstraints) {
                    registerSchemaPaths(componentId, dataSchemaNode, mustConstraint.getXpath());
                }
            }
        }
    }

    private void registerLeafRefSchemaPaths(String componentId, DataSchemaNode dataSchemaNode) {
        if (dataSchemaNode instanceof LeafSchemaNode) {
            LeafSchemaNode leafSchemaNode = (LeafSchemaNode) dataSchemaNode;
            if (leafSchemaNode.getType() instanceof LeafrefTypeDefinition) {
                LeafrefTypeDefinition type = (LeafrefTypeDefinition) leafSchemaNode.getType();
                registerSchemaPaths(componentId, dataSchemaNode, type.getPathStatement());
                registerRelativePath(dataSchemaNode, type.getPathStatement(), type);
            }
        } else if (dataSchemaNode instanceof LeafListSchemaNode) {
            LeafListSchemaNode leafListSchemaNode = (LeafListSchemaNode) dataSchemaNode;
            if (leafListSchemaNode.getType() instanceof LeafrefTypeDefinition) {
                LeafrefTypeDefinition type = (LeafrefTypeDefinition) leafListSchemaNode.getType();
                registerSchemaPaths(componentId, dataSchemaNode, type.getPathStatement());
                registerRelativePath(dataSchemaNode, type.getPathStatement(), type);
            }
        }
    }

    private void registerSchemaPaths(String componentId, DataSchemaNode dataSchemaNode, RevisionAwareXPath xPath) {
        SchemaPath nodeSchemaPath = dataSchemaNode.getPath();
        registerRelativePath(dataSchemaNode, xPath, null);
        Map<SchemaPath, String> constraintSchemaPaths = m_pathBuilder.getSchemaPathsFromXPath(dataSchemaNode, xPath);
        for (Map.Entry<SchemaPath, String> entry : constraintSchemaPaths.entrySet()) {
            m_schemaRegistry.registerNodesReferencedInConstraints(componentId, nodeSchemaPath, entry.getKey(), entry
                    .getValue());
        }
    }

    @SuppressWarnings("rawtypes")
    private void registerRelativePath(DataSchemaNode dataSchemaNode, RevisionAwareXPath xPath, TypeDefinition
            typeDefition) {
        if (NetconfExtensions.IS_TREAT_AS_RELATIVE_PATH.isExtensionIn(dataSchemaNode)) {
            String path = m_schemaRegistry.getMatchingPath(xPath.toString());
            String relativePath = m_pathBuilder.getRelativePath(xPath.toString(), path, dataSchemaNode);
            m_schemaRegistry.registerRelativePath(xPath.toString(), relativePath, dataSchemaNode);
        }

        if (NetconfExtensions.IS_TREAT_AS_RELATIVE_PATH.isExtensionIn(typeDefition)) {
            String path = m_schemaRegistry.getMatchingPath(xPath.toString());
            String relativePath = m_pathBuilder.getRelativePath(xPath.toString(), path, dataSchemaNode);
            m_schemaRegistry.registerRelativePath(xPath.toString(), relativePath, dataSchemaNode);
        }

    }

    @Override
    public void visitLeafListNode(String componentId, SchemaPath parentSchemaPath, LeafListSchemaNode leafListNode) {
        registerConstraintSchemaPaths(componentId, leafListNode);
        registerLeafRefSchemaPaths(componentId, leafListNode);
    }

    @Override
    public void visitLeafNode(String componentId, SchemaPath parentSchemaPath, LeafSchemaNode leafSchemaNode) {
        registerConstraintSchemaPaths(componentId, leafSchemaNode);
        registerLeafRefSchemaPaths(componentId, leafSchemaNode);
    }

    @Override
    public void visitChoiceCaseNode(String componentId, SchemaPath parentPath, ChoiceCaseNode choiceCaseNode) {
        registerConstraintSchemaPaths(componentId, choiceCaseNode);
        for (DataSchemaNode dataSchemaNode : choiceCaseNode.getChildNodes()) {
            registerConstraintSchemaPaths(componentId, dataSchemaNode);
        }
    }

    @Override
    public void visitChoiceNode(String componentId, SchemaPath parentPath, ChoiceSchemaNode choiceSchemaNode) {
        registerConstraintSchemaPaths(componentId, choiceSchemaNode);
        for (ChoiceCaseNode caseNode : choiceSchemaNode.getCases()) {
            visitChoiceCaseNode(componentId, parentPath, caseNode);
        }
    }

    @Override
    public void visitAnyXmlNode(String componentId, SchemaPath parentPath, AnyXmlSchemaNode anyXmlSchemaNode) {
        registerConstraintSchemaPaths(componentId, anyXmlSchemaNode);
    }

    @Override
    public void visitListNode(String componentId, SchemaPath parentPath, ListSchemaNode listSchemaNode) {
        registerConstraintSchemaPaths(componentId, listSchemaNode);
        m_schemaRegistry.registerActionSchemaNode(componentId, listSchemaNode.getPath(), listSchemaNode.getActions());
    }

    @Override
    public void visitContainerNode(String componentId, SchemaPath parentSchemaPath, ContainerSchemaNode
            containerSchemaNode) {
        registerConstraintSchemaPaths(componentId, containerSchemaNode);
        m_schemaRegistry.registerActionSchemaNode(componentId, containerSchemaNode.getPath(), containerSchemaNode
                .getActions());
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
