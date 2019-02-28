package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.service;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import org.broadband_forum.obbaa.netconf.api.logger.NetconfExtensions;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.MustConstraintAware;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.WhenConditionAware;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryVisitor;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.AnvExtensions;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationPathBuilder;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationUtil;

public class SchemaPathRegistrar implements SchemaRegistryVisitor {
    
    private final SchemaRegistry m_schemaRegistry;
    private final DataStoreValidationPathBuilder m_pathBuilder;
    
    public SchemaPathRegistrar(SchemaRegistry schemaRegistry, ModelNodeHelperRegistry modelNodeHelperRegistry) {
        m_schemaRegistry = schemaRegistry;
        m_pathBuilder = new DataStoreValidationPathBuilder(schemaRegistry, modelNodeHelperRegistry);
    }

    private boolean isInDataTree(SchemaPath targetPath) {
        SchemaPath parentPath = targetPath.getParent();
        while (parentPath != null && !parentPath.equals(SchemaPath.ROOT)) {
            DataSchemaNode parentNode = m_schemaRegistry.getDataSchemaNode(parentPath);
            if (parentNode == null) {
                // parent is not in data tree, is e.g. a notification
                return false;
            }
            parentPath = parentPath.getParent();
        }
        return true;
    }
    
    private void registerConstraintSchemaPaths(String componentId, DataSchemaNode dataSchemaNode) {
        if (!isInDataTree(dataSchemaNode.getPath())) {
            return;
        }
        if (dataSchemaNode instanceof WhenConditionAware) {
            Optional<RevisionAwareXPath> optWhenCondition = dataSchemaNode.getWhenCondition();
            if (optWhenCondition != null && optWhenCondition.isPresent()) {
                registerSchemaPaths(componentId, dataSchemaNode, optWhenCondition.get(), false);
            }
        }
        if (dataSchemaNode instanceof MustConstraintAware) {
            Collection<MustDefinition> mustConstraints = ((MustConstraintAware) dataSchemaNode).getMustConstraints();
            if (mustConstraints != null) {
                for (MustDefinition mustConstraint : mustConstraints) {
                    registerSchemaPaths(componentId, dataSchemaNode, mustConstraint.getXpath(), false);
                }
            }
        } 
        if(dataSchemaNode.isAugmenting() && dataSchemaNode instanceof LeafSchemaNode && dataSchemaNode.isConfiguration()){
            DataSchemaNode parentNode = m_schemaRegistry.getDataSchemaNode(dataSchemaNode.getPath().getParent());
            AugmentationSchemaNode augSchema = DataStoreValidationUtil.getAugmentationSchema(parentNode, dataSchemaNode);
            RevisionAwareXPath xpath = augSchema == null ? null : augSchema.getWhenCondition().orElse(null);
            if (xpath != null) {
                registerSchemaPaths(componentId, dataSchemaNode, xpath, true);
            }
        }
    }
    
    private void registerLeafRefSchemaPaths(String componentId, DataSchemaNode dataSchemaNode) {
        if (!isInDataTree(dataSchemaNode.getPath())) {
            return;
        }
        if (dataSchemaNode instanceof LeafSchemaNode) {
            LeafSchemaNode leafSchemaNode = (LeafSchemaNode) dataSchemaNode;
            if (leafSchemaNode.getType() instanceof LeafrefTypeDefinition) {
                LeafrefTypeDefinition type = (LeafrefTypeDefinition) leafSchemaNode.getType();
                if (type.requireInstance()) {
                    registerSchemaPaths(componentId, dataSchemaNode, type.getPathStatement(), false);
                    registerRelativePath(dataSchemaNode, type.getPathStatement(), type);
                }
            } 
        } else if (dataSchemaNode instanceof LeafListSchemaNode) {
            LeafListSchemaNode leafListSchemaNode = (LeafListSchemaNode) dataSchemaNode;
            if (leafListSchemaNode.getType() instanceof LeafrefTypeDefinition) {
                LeafrefTypeDefinition type = (LeafrefTypeDefinition) leafListSchemaNode.getType();
                if (type.requireInstance()) {
                    registerSchemaPaths(componentId, dataSchemaNode, type.getPathStatement(), false);
                    registerRelativePath(dataSchemaNode, type.getPathStatement(), type);
                }
            }
        }
    }

    private void registerSchemaPaths(String componentId, DataSchemaNode dataSchemaNode, RevisionAwareXPath xPath, boolean isAugment) {
        SchemaPath nodeSchemaPath = dataSchemaNode.getPath();
        registerRelativePath(dataSchemaNode, xPath, null); 
        if(dataSchemaNode.isConfiguration()) {
            Map<SchemaPath,String> constraintSchemaPaths = m_pathBuilder.getSchemaPathsFromXPath(dataSchemaNode, xPath, isAugment);
            for(Map.Entry<SchemaPath, String> entry:constraintSchemaPaths.entrySet()) {
                m_schemaRegistry.registerNodesReferencedInConstraints(componentId, nodeSchemaPath, entry.getKey(), entry.getValue());
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private void registerRelativePath(DataSchemaNode dataSchemaNode, RevisionAwareXPath xPath, TypeDefinition typeDefition) {
        if (NetconfExtensions.IS_TREAT_AS_RELATIVE_PATH.isExtensionIn(dataSchemaNode)){
            String path = m_schemaRegistry.getMatchingPath(xPath.toString());
            String relativePath = m_pathBuilder.getRelativePath(xPath.toString(), path, dataSchemaNode);
            m_schemaRegistry.registerRelativePath(xPath.toString(), relativePath, dataSchemaNode);
        }
        
        if (NetconfExtensions.IS_TREAT_AS_RELATIVE_PATH.isExtensionIn(typeDefition)){
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
    public void visitChoiceCaseNode(String componentId, SchemaPath parentPath, CaseSchemaNode choiceCaseNode) {
        registerConstraintSchemaPaths(componentId, choiceCaseNode);
        for (DataSchemaNode dataSchemaNode: choiceCaseNode.getChildNodes()) {
            registerConstraintSchemaPaths(componentId, dataSchemaNode);
        }
    }

    @Override
    public void visitChoiceNode(String componentId, SchemaPath parentPath, ChoiceSchemaNode choiceSchemaNode) {
        registerConstraintSchemaPaths(componentId, choiceSchemaNode);
        for(CaseSchemaNode caseNode : choiceSchemaNode.getCases().values()){
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
        registerMountPointSchemaPath(componentId, listSchemaNode);
    }

    @Override
    public void visitContainerNode(String componentId, SchemaPath parentSchemaPath, ContainerSchemaNode containerSchemaNode) {
        registerConstraintSchemaPaths(componentId, containerSchemaNode);
        registerMountPointSchemaPath(componentId, containerSchemaNode);
    }

    private void registerMountPointSchemaPath(String componentId, DataSchemaNode dataSchemaNode){
    	if(AnvExtensions.MOUNT_POINT.isExtensionIn(dataSchemaNode)){
    		m_schemaRegistry.registerMountPointSchemaPath(componentId, dataSchemaNode);
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
