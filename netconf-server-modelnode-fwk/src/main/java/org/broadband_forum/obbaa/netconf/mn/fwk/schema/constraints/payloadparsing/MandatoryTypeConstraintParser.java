package org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorInfo;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorMessages;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.ChoiceCaseNodeUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil;
import org.dom4j.dom.DOMNodeHelper;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ElementCountConstraint;
import org.opendaylight.yangtools.yang.model.api.ElementCountConstraintAware;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.NetconfRpcErrorUtil;

public class MandatoryTypeConstraintParser {
	
	public static final String MANDATORY_VALIDATION_CACHE = "MANDATORY_TYPE_VALIDATION_CACHE";
    
    public static void checkMandatoryElementExists(Element dataNode, Collection<DataSchemaNode> childNodes, DataSchemaNode dataSchemaNode, SchemaRegistry schemaRegistry) throws ValidationException {
    	Pair<String, Map<String, String>> errorPathPair = SchemaRegistryUtil.getErrorPath(dataNode, dataSchemaNode, schemaRegistry, (Element)null);
        checkMandatoryNodes(dataNode, childNodes, schemaRegistry, errorPathPair);
    }
    
	@SuppressWarnings("unchecked")
	private static Map<Element, List<Collection<DataSchemaNode>>> getCachedNodes() {
		RequestScope currentScope = RequestScope.getCurrentScope();
		HashMap<Element, List<Collection<DataSchemaNode>>> cachedNodes = (HashMap<Element, List<Collection<DataSchemaNode>>>) currentScope
				.getFromCache(MANDATORY_VALIDATION_CACHE);
		if (cachedNodes == null) {
			cachedNodes = new HashMap<Element, List<Collection<DataSchemaNode>>>();
			currentScope.putInCache(MANDATORY_VALIDATION_CACHE, cachedNodes);
		}
		return cachedNodes;
	}

    private static void checkMandatoryNodes(Element dataNode, Collection<DataSchemaNode> childNodes, SchemaRegistry schemaRegistry,
    		Pair<String, Map<String, String>> existingErrorPathPair) throws ValidationException {
    	Map<Element,List<Collection<DataSchemaNode>>> cachedNodes = getCachedNodes();
		if (cachedNodes.containsKey(dataNode)){
			for (Collection<DataSchemaNode> collection: cachedNodes.get(dataNode)){
				if (collection.equals(childNodes)){
		    		// That means this Element vs childNodes set is already validated. Lets not validate it again
					return;
				}
			}
		}
        for (DataSchemaNode schemaNode: childNodes) {
            ElementCountConstraint elementCountConstraint = null;
            if (schemaNode instanceof ElementCountConstraintAware) {
                Optional<ElementCountConstraint> optElementCountConstraint = ((ElementCountConstraintAware) schemaNode).getElementCountConstraint();
                if (optElementCountConstraint.isPresent()) {
                    elementCountConstraint = optElementCountConstraint.get();
                }
            }
            boolean complexMandatory = DataStoreValidationUtil.containsMustWhen(schemaRegistry, schemaNode);
            complexMandatory = complexMandatory || (elementCountConstraint != null && elementCountConstraint.getMinElements() != null && elementCountConstraint.getMinElements() > 0);
            if (complexMandatory) {
                // if this is not a simple mandatory, do not validate here. This will be taken care in post edit-config validation
                continue;
            }
            
            Element childNode = getChildNode(dataNode, schemaNode);
            
            if (schemaNode instanceof LeafSchemaNode) {
                if (((LeafSchemaNode)schemaNode).isMandatory()) {
                    checkIfMandatoryLeafNodeExists((LeafSchemaNode)schemaNode, childNode, schemaRegistry, existingErrorPathPair);
                }
            }
            else if (schemaNode instanceof ChoiceSchemaNode) {
                // TODO: FNMS-10124 How to check if ancestor is not a case node
                checkIfMandatoryChoiceNodeExists((ChoiceSchemaNode)schemaNode, dataNode, schemaRegistry, existingErrorPathPair);
            }
            else if (schemaNode instanceof AnyXmlSchemaNode) {
                if (((AnyXmlSchemaNode)schemaNode).isMandatory()) {
                    checkIfMandatoryAnyXmlNodeExists((AnyXmlSchemaNode)schemaNode, childNode, schemaRegistry, existingErrorPathPair);
                }
            }
            else if (schemaNode instanceof DataNodeContainer) {
            	Pair<String, Map<String, String>> errorPathPair = SchemaRegistryUtil.getErrorPath(existingErrorPathPair, childNode, schemaNode, schemaRegistry);
                if (childNode == null) {
                    if (schemaNode instanceof ContainerSchemaNode && !((ContainerSchemaNode) schemaNode).isPresenceContainer()) {
                        // we need to check whether there are mandatory nodes missing inside the missing container
                        checkMandatoryNodes(null, ((DataNodeContainer)schemaNode).getChildNodes(), schemaRegistry, errorPathPair);
                    }
                }
                else {
                    checkMandatoryNodes(childNode, ((DataNodeContainer)schemaNode).getChildNodes(), schemaRegistry, errorPathPair);
                }
            }
        }
		if (dataNode != null) {
			// add the current validated set to requestScope cache. Duplication
			// validation can be avoided on the same set
			List<Collection<DataSchemaNode>> validationList = null;
			if (cachedNodes.containsKey(dataNode)) {
				validationList = cachedNodes.get(dataNode);
			} else {
				validationList = new ArrayList<Collection<DataSchemaNode>>();
				cachedNodes.put(dataNode, validationList);
			}
			validationList.add(childNodes);
		}
    }

    private static void checkIfMandatoryLeafNodeExists(LeafSchemaNode leafSchemaNode, Element leafNode, SchemaRegistry schemaRegistry, Pair<String, Map<String, String>> existingErrorPathPair) throws ValidationException {
        if (leafSchemaNode.isConfiguration() && (leafNode == null || leafNode.getTextContent() == null)) {
        	Pair<String, Map<String, String>> errorPathPair = SchemaRegistryUtil.getErrorPath(existingErrorPathPair, leafNode, leafSchemaNode, schemaRegistry);
            throw new ValidationException(getDataMissingRpcError(leafSchemaNode, NetconfRpcErrorMessages.MANDATORY_LEAF_MISSING, NetconfRpcErrorInfo.MissingLeaf, errorPathPair));
        }        
    }
    
    private static void checkIfMandatoryChoiceNodeExists(ChoiceSchemaNode schemaNode, Element dataNode, SchemaRegistry schemaRegistry, Pair<String, Map<String, String>> existingErrorPathPair) throws ValidationException {
        boolean mandatoryChoice = schemaNode.isMandatory();
        CaseSchemaNode actualCaseNode = null;
        NodeList childNodes = (dataNode == null ? DOMNodeHelper.EMPTY_NODE_LIST : dataNode.getChildNodes());
        int count = 0; // To check if dataNode contains elements from more than one case
        
        for (CaseSchemaNode caseNode: schemaNode.getCases().values()) {
            Set<QName> caseChildNodes = new HashSet<>();
            for (DataSchemaNode caseChildNode: caseNode.getChildNodes()) {
                caseChildNodes.add(caseChildNode.getQName());
            }
            if (ChoiceCaseNodeUtil.isDataNodeSuperSet(childNodes, caseChildNodes)) {
                count++;
                actualCaseNode = caseNode;
            } 
        }
        
        if (mandatoryChoice && count == 0) {
            // dataNode does not contain elements from any case.
        	Pair<String, Map<String, String>> errorPathPair = SchemaRegistryUtil.getErrorPath(existingErrorPathPair, dataNode, schemaNode, schemaRegistry);
            throw new ValidationException(getDataMissingRpcError(schemaNode, NetconfRpcErrorMessages.MANDATORY_CHOICE_MISSING, NetconfRpcErrorInfo.MissingChoice, errorPathPair));
        }
        
        if (actualCaseNode != null) {
            checkMandatoryNodes(dataNode, actualCaseNode.getChildNodes(), schemaRegistry, existingErrorPathPair);
        }
        
    }
    
    private static void checkIfMandatoryAnyXmlNodeExists(AnyXmlSchemaNode anyxmlSchemaNode, Element anyxmlNode, SchemaRegistry schemaRegistry, Pair<String, Map<String, String>> existingErrorPathPair) throws ValidationException {
        if (anyxmlNode == null) {
        	Pair<String, Map<String, String>> errorPathPair = SchemaRegistryUtil.getErrorPath(existingErrorPathPair, anyxmlNode, anyxmlSchemaNode, schemaRegistry);
            throw new ValidationException(getDataMissingRpcError(anyxmlSchemaNode, NetconfRpcErrorMessages.MANDATORY_ANYXML_MISSING, NetconfRpcErrorInfo.MissingAnyxml, errorPathPair));            
        }
    }

    private static Element getChildNode(Element dataNode, DataSchemaNode schemaNode) {
        if (dataNode == null) {
            return null;
        }
        NodeList nodeList = dataNode.getChildNodes();
        String anyXmlLocalName = schemaNode.getQName().getLocalName();
        String anyXmlNamespace = schemaNode.getQName().getNamespace().toString();
        Element childNode = null;

        for(int i=0;i<nodeList.getLength();i++){
            Node node = nodeList.item(i);
            if(node.getNodeType() == Node.ELEMENT_NODE){
                if(anyXmlLocalName.equals(node.getLocalName()) && anyXmlNamespace.equals(node.getNamespaceURI())){
                    childNode = (Element)node;
                    break;
                }
            }
        }
        
        return childNode;
    }

    private static NetconfRpcError getDataMissingRpcError(DataSchemaNode schemaNode, String errorMsg, NetconfRpcErrorInfo errorInfo, Pair<String, Map<String, String>> errorPathPair) {
        String localName = schemaNode.getQName().getLocalName();
        NetconfRpcError netconfRpcError = NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.DATA_MISSING, 
                String.format(errorMsg, localName));
        netconfRpcError.addErrorInfoElement(errorInfo, localName);
        netconfRpcError.setErrorPath(errorPathPair.getFirst(), errorPathPair.getSecond());
        return netconfRpcError;
    }
}
