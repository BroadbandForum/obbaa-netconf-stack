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

package org.broadband_forum.obbaa.netconf.mn.fwk.validation;

import org.broadband_forum.obbaa.netconf.api.messages.*;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.AbstractSchemaNodeConstraintParser;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.type.builtin.TypeValidatorFactory;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.TypeValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildListHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeGetException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.DSValidationContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.DataStoreConstraintValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.TimingLogger;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.TimingLogger.ConstraintType;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DSExpressionValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.UniqueConstraintValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.NetconfRpcErrorUtil;
import org.broadband_forum.obbaa.netconf.server.rpc.RequestType;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ElementCountConstraint;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.*;

import static org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.MandatoryTypeConstraintParser.checkMandatoryElementExists;

/**
 * Single validation class, which performs validation of a List
 * at different phases
 * 
 * validate(Element) -> phase 1 validation
 * validate(ModelNode) -> phase 2 validation
 * 
 */

public class ListValidator extends AbstractSchemaNodeConstraintParser implements DataStoreConstraintValidator{
	private final SchemaRegistry m_schemaRegistry;
	private final ListSchemaNode m_listSchemaNode;
	private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(ListValidator.class, LogAppNames.NETCONF_STACK);

	private void validateKeys(Element dataNode) throws ValidationException {
        List<QName> keys = m_listSchemaNode.getKeyDefinition();
        List<QName> listElements = new ArrayList<>();
        NodeList childNodes = dataNode.getChildNodes();
        List<String> missingKeys = new ArrayList<>();
        List<String> misplacedKeys = new ArrayList<>();
        List<String> duplicateKeys = new ArrayList<>();
        List<Node> allNodes = new ArrayList<>();
        List<Node> keyNodes = new ArrayList<>();

        // the first N children need to be the N keys
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node child = childNodes.item(i);
			if(child.getNodeType() == Node.ELEMENT_NODE) {
				QName qName = SchemaRegistryUtil.getChildQname(child, m_listSchemaNode, m_schemaRegistry);
				listElements.add(qName);
				allNodes.add(child);
			}
		}

		keys.forEach(key -> {
			long countOfKeyOccurance = listElements.stream().filter(listElement -> key.equals(listElement)).count();
			if(countOfKeyOccurance == 0L ) {
				missingKeys.add(key.getLocalName());
			} else if (countOfKeyOccurance > 1L) {
				duplicateKeys.add(key.getLocalName());
			}
		});

        if(!duplicateKeys.isEmpty()) {
            NetconfRpcError rpcError = NetconfRpcError.getDuplicateKeyError(duplicateKeys, NetconfRpcErrorType.Application);
            throwRPCValidationException(dataNode, rpcError);
        }

        if(!missingKeys.isEmpty()) {
            NetconfRpcError rpcError = NetconfRpcError.getMissingKeyError(missingKeys, NetconfRpcErrorType.Application);
            throwRPCValidationException(dataNode, rpcError);
        }

		for(int i=0 ; i < keys.size(); i++) {
			QName key = keys.get(i);
			if(!key.equals(listElements.get(i))) {
				misplacedKeys.add(key.getLocalName());
			}
		}

		if (!misplacedKeys.isEmpty()) {
			NetconfRpcError rpcError = NetconfRpcError.getMisplacedKeyError(misplacedKeys, NetconfRpcErrorType.Application);
			throwRPCValidationException(dataNode, rpcError);
		}

		allNodes.forEach(node -> {
			QName qName = SchemaRegistryUtil.getChildQname(node, m_listSchemaNode, m_schemaRegistry);
			if(qName != null && keys.contains(qName)) {
				keyNodes.add(node);
			}});

		validateKeyNodesOperationAndEmpty(keyNodes);
    }

	private void throwRPCValidationException(Element dataNode, NetconfRpcError rpcError) {
		Pair<String, Map<String, String>> errorPathPair = SchemaRegistryUtil.getErrorPath(dataNode, m_listSchemaNode, m_schemaRegistry, (Element)null);
		rpcError.setErrorPath(errorPathPair.getFirst(), errorPathPair.getSecond());			
		throw new ValidationException(rpcError);
	}
	
	/*
	 * Don't allow to delete on key leaf
	 */
    private void validateKeyNodesOperationAndEmpty(List<Node> keyNodes) throws ValidationException {
        for (Node node : keyNodes) {
            Node operation = node.getAttributes().getNamedItemNS(NetconfResources.NETCONF_RPC_NS_1_0, NetconfResources.OPERATION);
            if (operation != null && EditConfigOperations.DELETE.equals(operation.getNodeValue())) {
                NetconfRpcError rpcError = NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.OPERATION_FAILED, "Cannot delete the key attribute '" + node.getNodeName() + "'");
                throw new ValidationException(rpcError);
            }
        }
	}
	
    private void validateMandatoryElement(Element dataNode) throws ValidationException {
        Collection<DataSchemaNode> childNodes = m_listSchemaNode.getChildNodes();
        checkMandatoryElementExists(dataNode, childNodes, m_listSchemaNode, m_schemaRegistry);            
    }

    private void validateInsertAttributes(Element dataNode) throws ValidationException {
    	String insert = getInsertAttributes(dataNode, NetconfResources.INSERT);
    	String key = getInsertAttributes(dataNode, NetconfResources.KEY);

    	if (InsertOperation.AFTER.equals(insert) || InsertOperation.BEFORE.equals(insert)) { //must have a 'value' attribute
    		if (m_listSchemaNode.isUserOrdered()) { //if ordered-by user , need to valid to value attribute is valid type
    			if (key == null) {
    				throw new ValidationException(getBadInsertAttributesError(NetconfResources.KEY,
    						"Key attribute can't be null or empty"));
    			}
    			// Validation for the keys in "key" attribute
    			Map<QName, String> keyPairs = validateKeyPredicates(dataNode, key);
    			// Validation for the key by the type of list 
    			validateTypeKeyAttributes(dataNode, keyPairs);
    			
    		} else { // if ordered-by system, the attributes "before" and "after" can't present
    			//Throw an "unknown-attribute" tag in the rpc-error
    			throw new ValidationException(getUnknownAttributesError(String.format("There is an unknown-attribute '%s' in element '%s'",
    					insert, dataNode.getLocalName())));

    		} 
    	} 

    }
    
    private static String getInsertAttributes(Element element,String localName) {
		String attribute = element.getAttributeNS(NetconfResources.NETCONF_YANG_1, localName);
		if (attribute == null || attribute.isEmpty()) {
			return null;
		}
		return attribute;
    }
    
    private boolean isKeyPredicate(String keyPredicate) {
    	String regex = "((\\w|\\W)+:(\\w|\\W)+='(\\w|\\W)+')|((\\w|\\W)+='(\\w|\\W)+')";
    	return keyPredicate.matches(regex);
    }
    
    private boolean isStringKeyPredicate(String str) {
    	return str.startsWith("[") && str.endsWith("]");
    }
    
	private Map<QName, String> validateKeyPredicates(Element dataNode, String keyAttribute) throws ValidationException {
		Map<QName, String> keyPairs = new HashMap<QName, String>();
		String listNamespace = dataNode.getNamespaceURI();
		String regex = "\\]\\["; // split keys
		if (!isStringKeyPredicate(keyAttribute)) {
			throw new ValidationException(getBadInsertAttributesError(NetconfResources.KEY,
					String.format("Key '%s' attribute is not the key predicates format.", keyAttribute)));
		}
		String keyAttributeFix = keyAttribute.substring(1,keyAttribute.length() - 1); // remove '[', ']'
		List<String> strKeys = Arrays.asList(keyAttributeFix.split(regex));
		Set<QName> addedKeys = new HashSet<QName>(); // check duplicate keys
		List<QName> keys = m_listSchemaNode.getKeyDefinition(); // key must present in key attribute ;
		for (String strKey : strKeys) {
			//validate for strKey
			if (!isKeyPredicate(strKey)) {
				throw new ValidationException(getBadInsertAttributesError(NetconfResources.KEY,
						String.format("'%s' is not a key predicate format.", "[" + strKey + "]")));
			}
			
			String key = strKey.substring(0, strKey.indexOf("="));
			String value = strKey.substring(strKey.indexOf("=") + 1, strKey.length()).trim();
			value = value.substring(value.indexOf("'") + 1,value.lastIndexOf("'"));
			if (key.indexOf(":") >= 0) {// contains prefix
				String prefix = key.substring(0,key.indexOf(":"));
				String prefixNamespace = getNamespaceFromPrefix(dataNode, prefix);
				key = key.substring(key.indexOf(":") + 1, key.length()).trim();
				// validate the namespace
				if (prefixNamespace == null) {

					throw new ValidationException(getBadInsertAttributesError(NetconfResources.KEY,
							String.format("There is an unknown prefix '%s' in key '%s' attribute", prefix, keyAttribute)));
				} else if (!prefixNamespace.equals(listNamespace)) {
					throw new ValidationException(getBadInsertAttributesError(NetconfResources.KEY,
							String.format("There is an unknown key '%s' in key '%s' attribute", prefix + ":" + key, keyAttribute)));
				}

			}
			//validate the key 
			QName qNameKey = validateKeyName(keys, addedKeys, key, keyAttribute);
			keyPairs.put(qNameKey, value);
		}

		//Validate the missing key 
		List<QName> missingKeys = new ArrayList<QName>();
		for (QName qNameKey : keys) {
			if (!addedKeys.contains(qNameKey)) {
				missingKeys.add(qNameKey);
			}
		}
		if (missingKeys.size() > 0) {
			throw new ValidationException(getBadInsertAttributesError(NetconfResources.KEY,
					String.format("Missing key '%s' in key '%s' attribute", getStringKeysMissing(missingKeys), keyAttribute)));
		}
		
		return keyPairs; // must return a map of full key/value within the list
	}
	
	private QName validateKeyName(List<QName> keyDefinition, Set<QName> addedKeys, String keyName, String keyAttribute) throws ValidationException {
		QName keyDefined = null;
		for (QName key : keyDefinition) {
			if (key.getLocalName().equals(keyName)) {
				keyDefined = key;
				break;
			}
		}
		if (keyDefined == null) {
			// key not existed
			throw new ValidationException(getBadInsertAttributesError(NetconfResources.KEY,
					String.format("There is an unknown key '%s' in key '%s' attribute", keyName, keyAttribute)));
		}
		if (!addedKeys.add(keyDefined)) {
			// key duplicated
			throw new ValidationException(getBadInsertAttributesError(NetconfResources.KEY,
					String.format("There is a duplicated key '%s' in key '%s' attribute", keyName, keyAttribute)));
		}
		return keyDefined;
	}
	
	private void validateTypeKeyAttributes(Element dataNode, Map<QName, String> keyPairs) throws ValidationException {
		for (QName key : keyPairs.keySet()) {
			DataSchemaNode dataSchemaNode = m_listSchemaNode.findDataChildByName(key).orElse(null);
			if (dataSchemaNode != null && dataSchemaNode instanceof LeafSchemaNode) {
				TypeValidator validator = TypeValidatorFactory.getInstance().getValidator(((LeafSchemaNode) dataSchemaNode).getType(), m_schemaRegistry);
				try {
					validator.validate(dataNode, true, keyPairs.get(key));
				} catch (ValidationException e) {
					NetconfRpcError rpcError = e.getRpcError();
					DataSchemaNode parentSchemaNode = m_schemaRegistry.getNonChoiceParent(m_listSchemaNode.getPath());
					//String errorPath = (parentSchemaNode == null) ? "/" : SchemaRegistryUtil.getErrorPath(dataNode.getParentNode(), parentSchemaNode,
						//	m_schemaRegistry, dataNode.getLocalName());

					Pair<String, Map<String, String>> errorPathPair = (parentSchemaNode == null) ? new Pair<String, Map<String, String>>("/", Collections.emptyMap()) : SchemaRegistryUtil.getErrorPath(dataNode.getParentNode(), parentSchemaNode,
							m_schemaRegistry, dataNode);
					rpcError = NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.BAD_ATTRIBUTE,e.getRpcError().getErrorMessage());
					rpcError.setErrorPath(errorPathPair.getFirst(), errorPathPair.getSecond());
					rpcError.addErrorInfoElement(NetconfRpcErrorInfo.BadAttribute, NetconfResources.KEY);
					throw new ValidationException(rpcError);
				}
			} else {
				throw new ValidationException(String.format("Key %s is not a leaf type", key.getLocalName()));
			}
		}
	}
	
	private String getNamespaceFromPrefix(Element element, String prefix) {
		String namespaceAttribute = "xmlns" + ":" + prefix;
		String namespaceValue = element.getAttribute(namespaceAttribute);
		if (namespaceValue != null && !namespaceValue.isEmpty()) {
			return namespaceValue;
		}
		
		Node parentNode = element.getParentNode();
		while (parentNode != null && parentNode.getNodeType() == Node.ELEMENT_NODE) {
			Node namedItem = parentNode.getAttributes().getNamedItem(namespaceAttribute);
			if (namedItem != null) {
				return namedItem.getTextContent();
			}
			
			parentNode = parentNode.getParentNode();
		}
		
		return null;
	}
	
	private String getStringKeysMissing(List<QName> missingKeys) {
		StringBuilder strBuilder = new StringBuilder();
		for (QName key : missingKeys) {
			strBuilder.append(key.getLocalName()).append(", ");
		}
		if (strBuilder.length() > 0) {
			strBuilder.deleteCharAt(strBuilder.length() - 1);
			strBuilder.deleteCharAt(strBuilder.length() - 1);
		}
		return strBuilder.toString();
	}
	
	private NetconfRpcError getBadInsertAttributesError(String badAttributeName, String errorMessage) {
		NetconfRpcError rpcError = NetconfRpcError.getBadAttributeError(badAttributeName,
				NetconfRpcErrorType.Application, errorMessage);
		rpcError.setErrorAppTag("missing-instance");
		return rpcError;
	}
	
	private NetconfRpcError getUnknownAttributesError(String errorMessage) {
		NetconfRpcError rpcError = NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.UNKNOWN_ATTRIBUTE, 
				 errorMessage);
		return rpcError;
	}

	private void validateSizeRange(ModelNode modelNode) throws ValidationException {
	    Optional<ElementCountConstraint> optElementCountConstraint = m_listSchemaNode.getElementCountConstraint();
	    if (optElementCountConstraint.isPresent()) {
	        QName qName = m_listSchemaNode.getQName();
	        TimingLogger.startConstraint(ConstraintType.SIZE, qName.toString());
	        try {
	            Collection<ModelNode> childModelNodes = getChildListModelNodeFromParentNode(modelNode);
	            validateSizeRange(optElementCountConstraint.get(), modelNode, childModelNodes.size(),
	                    qName.getLocalName(),
	                    qName.getNamespace().toString());
	        } finally {
	            TimingLogger.endConstraint(ConstraintType.SIZE, qName.toString());
	        }
	    }
	}

	private void validateUniqueConstraint(ModelNode modelNode, DSValidationContext validationContext) throws ValidationException {
		UniqueConstraintValidator.validateUniqueConstraint(modelNode, m_listSchemaNode, validationContext);
	}
	
	private Collection<ModelNode> getChildListModelNodeFromParentNode(ModelNode parentNode) {
		Collection<ModelNode> childModelNodes = new ArrayList<>();
		ChildListHelper childListHelper = parentNode.getMountModelNodeHelperRegistry().getChildListHelper(parentNode.getModelNodeSchemaPath(), m_listSchemaNode.getQName());
		if (childListHelper != null) {
			try {
				childModelNodes = childListHelper.getValue(parentNode, Collections.<QName, ConfigLeafAttribute>emptyMap());
			} catch (ModelNodeGetException e) {
				LOGGER.error("Error when getting child ModelNodes ChildListHelper.getValue(ModelNode, Map)", e);
			}
		}
		return childModelNodes;
	}

	
	public ListValidator(SchemaRegistry schemaRegistry, ModelNodeHelperRegistry modelNodeHelperRegistry, ListSchemaNode schemaNode, 
	        DSExpressionValidator expValidator){
		super(null, schemaNode, schemaRegistry, expValidator);
		m_schemaRegistry = schemaRegistry;
		m_listSchemaNode = schemaNode;
	}
	
	@Override
	public DataSchemaNode getDataSchemaNode() {
        return m_listSchemaNode;
	}

	/* (non-Javadoc)
	 * @see SchemaNodeConstraintParser#validate(org.w3c.dom.Element)
	 */
	@Override
	public void validate(Element dataNode, RequestType requestType) throws ValidationException {
    	validateInsertAttributes(dataNode); 
        validateKeys(dataNode);
        validateChoiceMultiCaseElements(dataNode);
        if (DataStoreValidationUtil.needsFurtherValidation(dataNode, requestType) && 
                !DataStoreValidationUtil.skipMandatoryConstaintValidation(m_listSchemaNode.getPath(), m_schemaRegistry, requestType)){
            validateMandatoryElement(dataNode);
        }
	}

	/* (non-Javadoc)
	 * @see DataStoreConstraintValidator#validate(org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode)
	 */
	@Override
	public void validate(ModelNode modelNode, DSValidationContext validationContext) throws ValidationException {
		validateChoiceCase(modelNode, m_listSchemaNode, validationContext);
		validateSizeRange(modelNode);
		validateUniqueConstraint(modelNode, validationContext);
	}

	@Override
	public void validateLeafRef(ModelNode modelNode, DSValidationContext validationContext) throws ValidationException {
		// Nothing to do here.
	}


	@Override
    protected AdvancedLogger getLogger() {
        return LOGGER;
    }

    @Override
    protected boolean isDataAvailable(ModelNode modelNode) {
        Collection<ModelNode> childModelNodes = getChildListModelNodeFromParentNode(modelNode);
        return childModelNodes != null && childModelNodes.size() > 0;
    }
}
