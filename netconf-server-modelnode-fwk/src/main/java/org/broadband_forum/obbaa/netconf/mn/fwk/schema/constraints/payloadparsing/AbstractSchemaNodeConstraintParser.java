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

package org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.PojoToDocumentTransformer;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.type.builtin.TypeValidatorFactory;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.TypeValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.ChoiceCaseNodeUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.IdentityRefConfigAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeGetException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.DSValidationContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DSExpressionValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationErrors;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.UniqueConstraintCheck;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.jxpath.JXPathUtils;
import org.broadband_forum.obbaa.netconf.server.rpc.RequestType;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ElementCountConstraint;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UniqueConstraint;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Relative;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * Created by keshava on 11/24/15.
 */
public abstract class AbstractSchemaNodeConstraintParser implements SchemaNodeConstraintParser {
    protected final TypeDefinition<?> m_typeDefinition;
    protected final SchemaRegistry m_schemaRegistry;
    protected final DataSchemaNode m_schemaNode;
    protected final DSExpressionValidator m_expValidator;
    private final SchemaPath m_schemaPath;

    public AbstractSchemaNodeConstraintParser(TypeDefinition<?> typeDefinition, DataSchemaNode schemaNode, SchemaRegistry schemaRegistry,
            DSExpressionValidator expValidator){
        m_typeDefinition = typeDefinition;
        m_schemaNode = schemaNode;
        m_schemaRegistry = schemaRegistry;
        m_expValidator = expValidator;
        m_schemaPath = schemaNode.getPath();
    }
    @Override
    public void validate(Element dataNode, RequestType requestType) throws ValidationException {
    	SchemaRegistry schemaRegistry = m_schemaRegistry;
        TypeValidator typeTypeValidator = TypeValidatorFactory.getInstance().getValidator(m_typeDefinition, schemaRegistry);
        String operationAttribute = dataNode.getAttributeNS(NetconfResources.NETCONF_RPC_NS_1_0, NetconfResources.OPERATION);
        if(typeTypeValidator != null && needsTypeValidation(operationAttribute)) {
            try {
                typeTypeValidator.validate(dataNode, false, null);
            } catch (ValidationException e) {
                NetconfRpcError rpcError = e.getRpcError();
                DataSchemaNode parentSchemaNode = schemaRegistry.getNonChoiceParent(m_schemaNode.getPath());
                Pair<String, Map<String, String>> errorPathPair = null;
                if (dataNode.getParentNode() != null) {
                    /**
                     * dataNode parent will be null, when the element is constructed back
                     * from DS during type validation in phase 3. In those case, the error path will be
                     * taken care by the caller who generated the element. 
                     */
                	errorPathPair = (parentSchemaNode == null) ? new Pair<String, Map<String, String>>("/", Collections.emptyMap()) : SchemaRegistryUtil.getErrorPath(dataNode.getParentNode(), parentSchemaNode,
                			schemaRegistry, dataNode);
                    rpcError.setErrorPath(errorPathPair.getFirst(), errorPathPair.getSecond());
                } else {
                    rpcError.setErrorPath(null, Collections.emptyMap());
                }
                throw e;
            }
        }
    }
    
    protected void validateChoiceMultiCaseElements(Element dataNode){
    	Collection<DataSchemaNode> childNodes = ((DataNodeContainer)m_schemaNode).getChildNodes();
    	ChoiceCasesConstraintParser.checkIfMultipleChoiceCaseElementExists(childNodes, m_schemaNode, dataNode, m_schemaRegistry);
    }
    
    protected Element getConfigLeafAttributeElement(ConfigLeafAttribute attribute, ModelNode parentModelNode, DSValidationContext validationContext){
    	QName qname = m_schemaNode.getQName();
    	Element attributeElement = validationContext.getDocument().createElementNS(qname.getNamespace().toString(), qname.getLocalName());
    	if (attribute.getStringValue() != null) {
    		attributeElement.setTextContent(attribute.getStringValue());
    	}

    	if (attribute instanceof IdentityRefConfigAttribute) {
    		/**
    		 * Only in the case of IdentityRef, the existing set of attributes available in the DOM is recreated. The assumption is that the existing
    		 * DOM element inside the IdentityRefAttribute has the proper set of attributes saved. This is because, when saving to the data
    		 * store, the identity ref is stored with the proper NS (even if it is default). Since we are at phase 3, the identity ref
    		 * constructed back from DS should have the right NS and prefix appended.
    		 **/
    		IdentityRefConfigAttribute refAttribute = (IdentityRefConfigAttribute) attribute;
    		Element domElement = refAttribute.getDOMValue();
    		NamedNodeMap nodeMap = domElement.getAttributes();
    		for (int i=0;i<nodeMap.getLength();i++) {
    			Node node = nodeMap.item(i);
    			if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
    				attributeElement.setAttributeNS(node.getNamespaceURI(),PojoToDocumentTransformer.XMLNS+node.getNodeName().split(":")[1],
    						node.getNodeValue());
    			}
    		}
    		/**
    		 * If IdentityRef type attribute added by 'uses' keyword and originally it is in different module/namespace, 
    		 * then original namespace should be appended in element for IdentityRef Type validation
    		 */
    		DataStoreValidationUtil.setOrginalNSIfAddedByUses(m_schemaNode, attributeElement, parentModelNode);
    	}
    	return attributeElement;
    }
    
    protected void validateType(ConfigLeafAttribute attribute, ModelNode parentModelNode, DSValidationContext validationContext) {
    	QName qname = m_schemaNode.getQName();
    	Element attributeElement = getConfigLeafAttributeElement(attribute, parentModelNode, validationContext);
    	try {
    		if (m_typeDefinition instanceof UnionTypeDefinition) {
    			validateUnionType(attributeElement, parentModelNode, m_typeDefinition, attribute, validationContext);
    		} else {
    			validate(attributeElement, null);
    		}
    	} catch(ValidationException e) {
    		NetconfRpcError error = e.getRpcError();
    		ModelNodeId errorId = new ModelNodeId(parentModelNode.getModelNodeId());
    		SchemaRegistry registry = SchemaRegistryUtil.getSchemaRegistry(parentModelNode, m_schemaRegistry);
    		errorId.addRdn(new ModelNodeRdn(ModelNodeRdn.CONTAINER,qname.getNamespace().toString(), qname.getLocalName()));
    		error.setErrorPath(errorId.xPathString(registry), errorId.xPathStringNsByPrefix(registry));
    		throw e;
    	}
    }
    
    protected void validateUnionType(Element dataNodeElement, ModelNode modelNode, TypeDefinition<?> typeDefinition,
    		ConfigLeafAttribute attribute, DSValidationContext validationContext) throws ValidationException {
    	SchemaRegistry schemaRegistry = m_schemaRegistry;
    	TypeValidator typeTypeValidator = TypeValidatorFactory.getInstance().getValidator(typeDefinition,
    			schemaRegistry);
    	String operationAttribute = dataNodeElement.getAttributeNS(NetconfResources.NETCONF_RPC_NS_1_0,
    			NetconfResources.OPERATION);
    	if (typeTypeValidator != null && needsTypeValidation(operationAttribute)) {
    		boolean isValid = false;
    		NetconfRpcError netconfRpcError = null;
    		StringBuilder errorMessage = new StringBuilder();
    		StringBuilder errorAppTag = new StringBuilder();
    		if (typeDefinition instanceof UnionTypeDefinition) {
    			UnionTypeDefinition unionType = (UnionTypeDefinition) typeDefinition;
    			for (TypeDefinition<?> type : unionType.getTypes()) {
    				try {
    					if (type instanceof UnionTypeDefinition) {
    						// validate Union type again if nested union type
    						validateUnionType(dataNodeElement, modelNode, type, attribute, validationContext);
    						isValid = true;
    						break;
    					} else if (type instanceof LeafrefTypeDefinition) {
    						// validate leaf-ref type under union type
    						validateLeafRefValue(modelNode, (LeafrefTypeDefinition) type, attribute, validationContext);
    						isValid = true;
    						break;
    					} else if (type instanceof InstanceIdentifierTypeDefinition) {
    						// validate instance-identifier type under union
    						// type
    						validateInstanceIdentifierValue(modelNode, type, attribute, validationContext);
    						isValid = true;
    						break;
    					} else {
    						TypeValidator validator = TypeValidatorFactory.getInstance().getValidator(type,
    								m_schemaRegistry);
    						if (validator != null) {
    							// validate other types (except leaf-ref,
    							// instance-identifier) under union type
    							validator.validate(dataNodeElement, false, null);
    							isValid = true;
    							break;
    						}
    					}
    				} catch (ValidationException e) {
    					errorMessage.append(e.getRpcError().getErrorMessage()).append(" or ");
    					if (e.getRpcError().getErrorAppTag() != null) {
    						errorAppTag.append(e.getRpcError().getErrorAppTag()).append(" or ");
    					}
    					netconfRpcError = e.getRpcError();
    				}
    			}

    			if (!isValid) {
    				netconfRpcError.setErrorMessage(errorMessage.substring(0, errorMessage.length() - 4));
    				netconfRpcError.setErrorAppTag(
    						errorAppTag.substring(0, errorAppTag.length() == 0 ? 0 : errorAppTag.length() - 4));
    				DataSchemaNode parentSchemaNode = schemaRegistry.getNonChoiceParent(m_schemaNode.getPath());
    				if (dataNodeElement.getParentNode() != null) {
    					Pair<String, Map<String, String>> errorPathPair = (parentSchemaNode == null)
    							? new Pair<String, Map<String, String>>("/", Collections.emptyMap())
    									: SchemaRegistryUtil.getErrorPath(dataNodeElement.getParentNode(), parentSchemaNode,
    											schemaRegistry, dataNodeElement);
    							netconfRpcError.setErrorPath(errorPathPair.getFirst(), errorPathPair.getSecond());
    				} else {
    					netconfRpcError.setErrorPath(null, Collections.emptyMap());
    				}
    				throw new ValidationException(netconfRpcError);
    			}
    		}
    	}
    }
	
    protected boolean validateInstanceIdentifierValue(ModelNode modelNode, TypeDefinition<?> type,
			ConfigLeafAttribute attribute, DSValidationContext validationContext) {
    	return true;
	}
    protected boolean validateLeafRefValue(ModelNode modelNode, TypeDefinition<?> type,
			ConfigLeafAttribute attribute, DSValidationContext validationContext) {
    	return true;
	}
	protected boolean needsTypeValidation(String operationAttribute) {
        return true;
    }

    protected void validateSizeRange(ElementCountConstraint constraints, ModelNode parentModelNode, int currentSize, String childName, String namespace)
            throws ValidationException {
    	SchemaRegistry registry = parentModelNode.getMountRegistry();
        Integer minElements = constraints.getMinElements();
        if (minElements != null && currentSize < minElements) {
            ValidationException violateMinException = DataStoreValidationErrors.getViolateMinElementException(childName, minElements);
            ModelNodeId id = buildModelNodeId(parentModelNode, childName, namespace);
            violateMinException.getRpcError().setErrorPath(id.xPathString(registry), id.xPathStringNsByPrefix(registry));
            throw violateMinException;
        }
    
        Integer maxElements = constraints.getMaxElements();
        if (maxElements != null && currentSize > maxElements) {
            ValidationException violateMaxException = DataStoreValidationErrors.getViolateMaxElementException(childName, maxElements);
            ModelNodeId id = buildModelNodeId(parentModelNode, childName, namespace);
            violateMaxException.getRpcError().setErrorPath(id.xPathString(registry), id.xPathStringNsByPrefix(registry));
            throw violateMaxException;
        }
    }
	protected ModelNodeId buildModelNodeId(ModelNode modeNode, String childName, String namespace) {
		ModelNodeId id = new ModelNodeId(modeNode.getModelNodeId());
		id.addRdn(new ModelNodeRdn(ModelNodeRdn.CONTAINER, namespace, childName));
		return id;
	}

    protected void validateChoiceCase(ModelNode modelNode, DataSchemaNode schemaNode, DSValidationContext validationContext) {
        SchemaPath schemaPath = schemaNode.getPath().getParent();
        if (schemaPath != null) {
            ChoiceSchemaNode choiceSchemaNode = ChoiceCaseNodeUtil.getChoiceSchemaNodeFromCaseNodeSchemaPath(m_schemaRegistry, schemaPath);
            if (choiceSchemaNode != null) {
                validationContext.setChildOfChoiceCase(schemaNode);
                Optional<RevisionAwareXPath> choiceOptWhenCondition = choiceSchemaNode.getWhenCondition();
                if (choiceOptWhenCondition.isPresent()) {
                    RevisionAwareXPath awareXPath = choiceOptWhenCondition.get();
					boolean isChoiceSkipValidation = m_schemaRegistry.isSkipValidationBySchemaPathWithConstraintXpath(choiceSchemaNode.getPath(), awareXPath.getOriginalString());
                    if (!isChoiceSkipValidation && isDataAvailable(modelNode) && awareXPath != null) {
                        m_expValidator.validateWhen(JXPathUtils.getExpression(awareXPath.getOriginalString()),choiceSchemaNode, modelNode
								, validationContext.getImpactValidation(), validationContext);
                    }
                }
                DataSchemaNode choiceCaseNode = m_schemaRegistry.getDataSchemaNode(schemaPath);
                if (choiceCaseNode != null) {
                    Optional<RevisionAwareXPath> caseOptWhenCondition = choiceCaseNode.getWhenCondition();
                    if (caseOptWhenCondition.isPresent()) {
                        RevisionAwareXPath awareXPath = caseOptWhenCondition.get();
						boolean isCaseSkipValidation = m_schemaRegistry.isSkipValidationBySchemaPathWithConstraintXpath(schemaPath, awareXPath.getOriginalString());
                        if (!isCaseSkipValidation && isDataAvailable(modelNode) && awareXPath != null) {
                            m_expValidator.validateWhen(JXPathUtils.getExpression(awareXPath.getOriginalString()),choiceCaseNode,
									modelNode, validationContext.getImpactValidation(), validationContext);
                        }
                    }
                }
            }
        }
    }
    


	protected abstract AdvancedLogger getLogger();

	protected abstract boolean isDataAvailable(ModelNode parentNode);
}
