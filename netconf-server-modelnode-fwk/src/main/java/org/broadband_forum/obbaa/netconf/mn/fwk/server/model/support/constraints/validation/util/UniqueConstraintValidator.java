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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.ChoiceCaseNodeUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildListHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeGetException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.DSValidationContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.TimingLogger;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.ValidatedUniqueConstraints;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.*;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.*;

public class UniqueConstraintValidator {

    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(UniqueConstraintValidator.class, LogAppNames.NETCONF_STACK);

    public static void validateUniqueConstraintForLeafChange(ModelNode parentModelNode, LeafSchemaNode leafSchemaNode,
                                                             DSValidationContext validationContext){
        SchemaRegistry schemaRegistry = parentModelNode.getSchemaRegistry();
        while ( parentModelNode != null){
            SchemaPath parentSchemaPath = parentModelNode.getModelNodeSchemaPath();
            DataSchemaNode schemaNode = schemaRegistry.getDataSchemaNode(parentSchemaPath);
            if ( schemaNode instanceof ListSchemaNode && !((ListSchemaNode) schemaNode).getUniqueConstraints().isEmpty()){
                for ( UniqueConstraint uniqueConstraint : ((ListSchemaNode) schemaNode).getUniqueConstraints()){
                    for (SchemaNodeIdentifier.Relative relative : uniqueConstraint.getTag()) {
                        SchemaPath relativePath = relative.asSchemaPath();
                        SchemaPath uniqueNodePath = parentSchemaPath.createChild(relativePath);
                        if ( uniqueNodePath.equals(leafSchemaNode.getPath())){
                            ModelNode listParentModelNode = parentModelNode.getParent();
                            if ( listParentModelNode != null){
                                validateUniqueConstraint(listParentModelNode, (ListSchemaNode)schemaNode, validationContext);
                            }
                        }
                    }
                }
            }
            parentModelNode = parentModelNode.getParent();
        }
    }

    public static void validateUniqueConstraint(ModelNode modelNode, ListSchemaNode listSchemaNode, DSValidationContext validationContext) throws ValidationException {
        ValidatedUniqueConstraints validatedUniqueConstraints = validationContext.getValidatedUniqueConstraints();
        boolean isAlreadyValidated =
                validatedUniqueConstraints.isValidated(modelNode, listSchemaNode);
        if(!isAlreadyValidated) {
            Collection<UniqueConstraint> uniqueConstraints = listSchemaNode.getUniqueConstraints();
            if (uniqueConstraints != null && !uniqueConstraints.isEmpty()) {
                QName qName = listSchemaNode.getQName();
                TimingLogger.startConstraint(TimingLogger.ConstraintType.UNIQUE, qName.toString());
                try {
                    Collection<ModelNode> childModelNodes = getChildListModelNodeFromParentNode(modelNode, listSchemaNode);
                    validateUniqueConstraint(listSchemaNode.getUniqueConstraints(), modelNode, childModelNodes,
                            qName.getLocalName(), qName.getNamespace().toString(), listSchemaNode.getPath());
                } finally {
                    TimingLogger.endConstraint(TimingLogger.ConstraintType.UNIQUE, qName.toString());
                }
            }
            validatedUniqueConstraints.markAsValidated(modelNode, listSchemaNode);
        }
    }

    private static Collection<ModelNode> getChildListModelNodeFromParentNode(ModelNode parentNode, ListSchemaNode listSchemaNode) {
        Collection<ModelNode> childModelNodes = new ArrayList<>();
        ChildListHelper childListHelper = parentNode.getMountModelNodeHelperRegistry().getChildListHelper(parentNode.getModelNodeSchemaPath(), listSchemaNode.getQName());
        if (childListHelper != null) {
            try {
                childModelNodes = childListHelper.getValue(parentNode, Collections.<QName, ConfigLeafAttribute>emptyMap());
            } catch (ModelNodeGetException e) {
                LOGGER.error("Error when getting child ModelNodes ChildListHelper.getValue(ModelNode, Map)", e);
            }
        }
        return childModelNodes;
    }

    private static void validateUniqueConstraint(Collection<UniqueConstraint> constraints, ModelNode parentModelNode,
                                            Collection<ModelNode> childModelNodes, String childName, String namespace, SchemaPath ListNodeSchemaPath) throws ValidationException {
        SchemaRegistry registry = parentModelNode.getSchemaRegistry();
        ValidationException violateUniqueException = DataStoreValidationErrors.getUniqueConstraintException();
        Document doc = DocumentUtils.createDocument();
        Element errorInfo = doc.createElementNS(NetconfResources.NETCONF_RPC_NS_1_0, NetconfResources.RPC_ERROR_INFO);
        // validate each unique constraint
        for (UniqueConstraint constraint : constraints) {
            Set<SchemaPath> uniqueConstraintPaths = new HashSet<>();
            for (SchemaNodeIdentifier.Relative relative : constraint.getTag()) {
                uniqueConstraintPaths.add(relative.asSchemaPath());
            }
            BiMap<ModelNode, UniqueConstraintCheck> modelNodeToAllUniqueAttr = HashBiMap.create();
            boolean isUnique = true;
            int nonUniqueChild = 0;
            for (ModelNode childModelNode : childModelNodes) {
                ModelNodeWithAttributes attr = (ModelNodeWithAttributes) childModelNode;
                Map<SchemaPath, ConfigLeafAttribute> attributes = getAllUniqueConstraintAttributes(attr, uniqueConstraintPaths, registry, ListNodeSchemaPath);
                if (attributes.keySet().containsAll(uniqueConstraintPaths)) {
                    UniqueConstraintCheck unique = new UniqueConstraintCheck();
                    for (SchemaPath schemaPath : uniqueConstraintPaths) {
                        unique.m_attributes.put(schemaPath.toString(), attributes.get(schemaPath).getStringValue());
                    }
                    try {
                        modelNodeToAllUniqueAttr.put(childModelNode, unique);
                    } catch (IllegalArgumentException e) {
                        ModelNodeId id = buildModelNodeId(parentModelNode, childName, namespace);
                        isUnique = false;
                        nonUniqueChild++;
                        Element nonUnique = doc.createElementNS(NetconfResources.NETCONF_YANG_1, "non-unique");
                        ModelNodeId childModelNodeId = buildModelNodeId(childModelNode, childName, namespace);
                        String instanceIdentifier = childModelNodeId.xPathString(registry);
                        nonUnique.setTextContent(instanceIdentifier);
                        errorInfo.appendChild(nonUnique);
                        violateUniqueException.getRpcError().setErrorPath(id.xPathString(registry), id.xPathStringNsByPrefix(registry));
                        if(nonUniqueChild == 1) {
                            violateUniqueException.getRpcError().setErrorMessage(e.getMessage());
                        }
                    }
                }
            }
            if (!isUnique) {
                violateUniqueException.getRpcError().setErrorInfo(errorInfo);
                throw violateUniqueException;
            }
        }
    }

    private static Map<SchemaPath, ConfigLeafAttribute> getAllUniqueConstraintAttributes(ModelNodeWithAttributes attr, Set<SchemaPath> uniqueConstraintNodes, SchemaRegistry registry, SchemaPath listPath) {
        Map<SchemaPath, ConfigLeafAttribute> attributeValues = new HashMap<>();
        for ( SchemaPath path : uniqueConstraintNodes){
            Iterator<QName> itr = path.getPathFromRoot().iterator();
            SchemaPath parentPath = listPath;
            ModelNodeWithAttributes parent = attr;
            DataSchemaNode dsn = null;
            while ( itr.hasNext()){
                QName qName = itr.next();
                if ( ChoiceCaseNodeUtil.isChoiceSchemaNode(dsn)){
                    dsn = ChoiceCaseNodeUtil.getCaseSchemaNode((ChoiceSchemaNode) dsn, qName);
                } else if ( ChoiceCaseNodeUtil.isCaseSchemaNode(dsn)){
                    dsn = ChoiceCaseNodeUtil.getCaseChildNode((CaseSchemaNode) dsn, qName);
                } else {
                    dsn = registry.getChild(parentPath, qName);
                }
                parentPath = dsn.getPath();
                if ( dsn instanceof ContainerSchemaNode){
                    try {
                        parent = (ModelNodeWithAttributes) parent.getChildContainerModelNode(qName);
                    } catch (ModelNodeGetException e) {
                        // Considering no child model node. So unique constraint should go through.
                        dsn = null;
                        break;
                    }
                }
            }
            if ( parent != null){
                ConfigLeafAttribute attribute = parent.getAttribute(path.getLastComponent());
                if ( dsn != null && attribute != null){
                    attributeValues.put(path, attribute);
                }
            }
        }
        return attributeValues;
    }

    private static ModelNodeId buildModelNodeId(ModelNode modeNode, String childName, String namespace) {
        ModelNodeId id = new ModelNodeId(modeNode.getModelNodeId());
        id.addRdn(new ModelNodeRdn(ModelNodeRdn.CONTAINER, namespace, childName));
        return id;
    }
}
