package org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class ChoiceCaseNodeUtil {
	
	public static Collection<DataSchemaNode> getImmediateChildrenOfChoice(DataSchemaNode choiceNode) {
		List<DataSchemaNode> children = new LinkedList<DataSchemaNode>();
		Collection<DataSchemaNode> returnValue = new LinkedList<DataSchemaNode>();
		fillCaseChildren(children, choiceNode);
		for (DataSchemaNode schemaNode:children) {
			if (isChoiceOrCaseNode(schemaNode)) {
				returnValue.addAll(getImmediateChildrenOfChoice(schemaNode));
			} else {
				returnValue.add(schemaNode);
			}
		}
		return returnValue;
	}
	public static Set<CaseSchemaNode> checkIsCaseNodeAndReturnAllOtherCases(SchemaRegistry schemaRegistry, SchemaPath schemaPath) {
		ChoiceSchemaNode choiceSchemaNode = getChoiceSchemaNodeFromCaseNodeSchemaPath(schemaRegistry, schemaPath);
		Set<CaseSchemaNode> remainChoiceCaseNodes = new HashSet<>();
		if (choiceSchemaNode != null) {
			Collection<CaseSchemaNode> allChoiceSchemaNodes = choiceSchemaNode.getCases().values();
			remainChoiceCaseNodes.addAll(allChoiceSchemaNodes);
			for (CaseSchemaNode caseNode : allChoiceSchemaNodes) {
				if (caseNode.getPath().equals(schemaPath)) {
					remainChoiceCaseNodes.remove(caseNode);
					return remainChoiceCaseNodes;
				}
			}
		}
		return null;
    }
	
	public static ChoiceSchemaNode getChoiceSchemaNodeFromCaseNodeSchemaPath( SchemaRegistry schemaRegistry, SchemaPath schemaPath) {
		DataSchemaNode schemaNode = schemaRegistry.getDataSchemaNode(schemaPath);
		if(schemaNode == null && SchemaRegistryUtil.isMountPointEnabled()){
			if(SchemaRegistryUtil.getMountRegistry() != null){
				schemaRegistry = SchemaRegistryUtil.getMountRegistry();
				schemaNode = schemaRegistry.getDataSchemaNode(schemaPath);
			}
		}
		if (schemaNode != null && schemaNode instanceof CaseSchemaNode) {
			SchemaPath choiceSchemaPath = schemaNode.getPath().getParent();
			return (ChoiceSchemaNode) schemaRegistry.getDataSchemaNode(choiceSchemaPath);
		}
		
		return null;
	}
	
	public static List<DataSchemaNode> getAllNodesFromCases(Collection<CaseSchemaNode> choiceCaseNodes) {
		List<DataSchemaNode> schemaNodes = new ArrayList<>();
		for (CaseSchemaNode caseNode : choiceCaseNodes) {
			schemaNodes.addAll(caseNode.getChildNodes());
		}
		return schemaNodes;
	}
	
	public static List<DataSchemaNode> getChildrenUnderChoice(SchemaRegistry schemaRegistry, SchemaPath schemaPath) {
	    List<DataSchemaNode> childrenNodes = new ArrayList<>();
        DataSchemaNode node = schemaRegistry.getDataSchemaNode(schemaPath);
        if(node != null && node instanceof DataNodeContainer){
            for (DataSchemaNode child : ((DataNodeContainer)node).getChildNodes()) {
                if (child instanceof ChoiceSchemaNode) {
                	fillCaseChildren(childrenNodes, child);
                } else {
                    childrenNodes.add(child);
                }
            }
        }
        return childrenNodes;
    }
	
	private static void fillCaseChildren(List<DataSchemaNode> childrenNodes, DataSchemaNode child) {
		Collection<CaseSchemaNode> cases = ((ChoiceSchemaNode) child).getCases().values();
		for (CaseSchemaNode caseNode : cases) {
		    for ( DataSchemaNode caseChild : caseNode.getChildNodes()){
		        if ( caseChild instanceof ChoiceSchemaNode){
		            fillCaseChildren(childrenNodes, caseChild);
		        } else {
		            childrenNodes.add(caseChild);
		        }
		    }
		}
	}
	
    public static boolean isChoiceSchemaPath(SchemaRegistry schemaRegistry, SchemaPath schemaPath) {
        DataSchemaNode currentSchemaNode = schemaRegistry.getDataSchemaNode(schemaPath);
        if (currentSchemaNode != null && currentSchemaNode instanceof ChoiceSchemaNode) {
            return true;
        }
        return false;
    }
    
    public static SchemaPath getChoiceCaseNodeSchemaPath(SchemaRegistry schemaRegistry, SchemaPath currentNodePath) {
        if (currentNodePath == null) {
            return null;
        }
        DataSchemaNode currentSchemaNode = schemaRegistry.getDataSchemaNode(currentNodePath.getParent());
        if (currentSchemaNode instanceof CaseSchemaNode) {
            return currentSchemaNode.getPath();
        }
        return getChoiceCaseNodeSchemaPath(schemaRegistry, currentNodePath.getParent());
    }

    public static boolean isChoiceOrCaseNode(DataSchemaNode schemaNode) {
        if (schemaNode instanceof ChoiceSchemaNode || schemaNode instanceof CaseSchemaNode) {
            return true;
        }
        return false;
    }
    
    public static boolean isDataNodeSuperSet(NodeList actualChildNodes, Set<QName> definedChildNodes) {

        for(int i =0; i< actualChildNodes.getLength();i++){

            Node node = actualChildNodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                String nodeLocalName = node.getLocalName();
                String nodeNamespace = node.getNamespaceURI();

                for(QName caseChildNode : definedChildNodes){
                    if(caseChildNode.getLocalName().equals(nodeLocalName) && caseChildNode.getNamespace().toString().equals(nodeNamespace)){
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
