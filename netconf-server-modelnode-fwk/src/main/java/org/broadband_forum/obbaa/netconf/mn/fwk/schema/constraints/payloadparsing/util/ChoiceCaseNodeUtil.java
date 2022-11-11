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

package org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.jxpath.ri.compiler.Expression;
import org.broadband_forum.obbaa.netconf.api.util.DataPath;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.AnvExtensions;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeDynaBean;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeDynaBeanContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeDynaBeanFactory;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.DSValidationContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DSExpressionValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.jxpath.JXPathUtils;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.collect.Iterators;

public class ChoiceCaseNodeUtil {
    
    public static boolean isChoiceSchemaNode( DataSchemaNode node){
        return node instanceof ChoiceSchemaNode;
    }
    
    public static boolean isCaseSchemaNode( DataSchemaNode node){
        return node instanceof CaseSchemaNode;
    }
    
    public static CaseSchemaNode getCaseSchemaNode( ChoiceSchemaNode choice, QName caseQName){
        SortedMap<QName, CaseSchemaNode> cases = choice.getCases();
        return cases.get(caseQName);
    }
    
    public static DataSchemaNode getCaseChildNode( CaseSchemaNode caseNode, QName childQName){
        Collection<DataSchemaNode> caseChildren = caseNode.getChildNodes();
        for ( DataSchemaNode caseChild : caseChildren){
            if ( caseChild.getQName().equals(childQName)){
                return caseChild;
            }
        }
        return null;
    }

    public static Collection<DataSchemaNode> getImmediateChildrenOfChoice(ChoiceSchemaNode choiceNode) {
        List<DataSchemaNode> children = new LinkedList<DataSchemaNode>();
        Collection<DataSchemaNode> returnValue = new LinkedList<DataSchemaNode>();
        fillCaseChildren(children, choiceNode);
        for (DataSchemaNode schemaNode : children) {
            if (schemaNode instanceof ChoiceSchemaNode) {
                returnValue.addAll(getImmediateChildrenOfChoice((ChoiceSchemaNode) schemaNode));
            } else {
                returnValue.add(schemaNode);
            }
        }
        return returnValue;
    }

    public static Set<CaseSchemaNode> checkIsCaseNodeAndReturnAllOtherCases(SchemaRegistry schemaRegistry,
            SchemaPath schemaPath) {
        ChoiceSchemaNode choiceSchemaNode = getChoiceSchemaNodeFromCaseNodeSchemaPath(schemaRegistry, schemaPath);
        Set<CaseSchemaNode> remainChoiceCaseNodes = new HashSet<>();
        if (choiceSchemaNode != null) {
            addParentCaseNodesToBeDeleted(schemaRegistry, choiceSchemaNode.getPath(), remainChoiceCaseNodes);
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

    /**
     * In nested choice nodes, consider the following case container parent{
     * choice A{ case A1{ leaf p{ type string; } } case A2{ choice B{ case B1{ }
     * 
     * case B2{ leaf q{ type string; } } } } }
     * 
     * In the above case, if initially the datastore contains <parent>
     * <p>
     * test
     * </p>
     * </parent>
     * 
     * And an edit request is sent as below, <parent>
     * <q>test2</q> </parent> , we need to iterate till choice A, so that case
     * A1 gets deleted. This method adds case nodes of nested choice parents so
     * that those case nodes are deleted as well when the choice is modified.
     **/
    private static void addParentCaseNodesToBeDeleted(SchemaRegistry schemaRegistry, SchemaPath schemaPath,
            Set<CaseSchemaNode> remainChoiceCaseNodes) {

        while (schemaPath.getLastComponent() != null) {
            DataSchemaNode node = schemaRegistry.getDataSchemaNode(schemaPath);
            DataPath dataPath = DataPathUtil.buildParentDataPath(schemaPath, schemaRegistry);
            if (Iterators.size(dataPath.iterator()) == Iterators
                    .size((Iterator<?>) schemaPath.getPathFromRoot().iterator())) {
                break;
            }
            if (node instanceof CaseSchemaNode) {
                SchemaPath casePath = node.getPath();
                ChoiceSchemaNode parentChoiceNode = (ChoiceSchemaNode) schemaRegistry
                        .getDataSchemaNode(casePath.getParent());
                Collection<CaseSchemaNode> allChoiceSchemaNodes = parentChoiceNode.getCases().values();
                remainChoiceCaseNodes.addAll(allChoiceSchemaNodes);
                for (CaseSchemaNode caseNode : allChoiceSchemaNodes) {
                    if (caseNode.getPath().equals(casePath)) {
                        remainChoiceCaseNodes.remove(caseNode);
                    }
                }
            }
            schemaPath = schemaPath.getParent();
        }

    }

    public static ChoiceSchemaNode getChoiceSchemaNodeFromCaseNodeSchemaPath(SchemaRegistry schemaRegistry,
            SchemaPath schemaPath) {
        DataSchemaNode schemaNode = schemaRegistry.getDataSchemaNode(schemaPath);
        if (schemaNode != null && schemaNode instanceof CaseSchemaNode) {
            SchemaPath choiceSchemaPath = schemaNode.getPath().getParent();
            return (ChoiceSchemaNode) schemaRegistry.getDataSchemaNode(choiceSchemaPath);
        }

        return null;
    }

    public static boolean isDefaultCreationAllowed(SchemaRegistry schemaRegistry, DataSchemaNode constraintNode) {
        SchemaPath caseSchemaPath = ChoiceCaseNodeUtil.getCaseSchemaNodeFromChildNodeIfApplicable(schemaRegistry,
                constraintNode.getPath());
        ChoiceSchemaNode choiceSchemaNode = ChoiceCaseNodeUtil.getChoiceSchemaNodeFromCaseNodeSchemaPath(schemaRegistry,
                caseSchemaPath);
        boolean isDefaultCreationAllowed = true;
        if (choiceSchemaNode != null) {
            SchemaNode caseNode = schemaRegistry.getDataSchemaNode(caseSchemaPath);
            isDefaultCreationAllowed = isChoiceWithDefaultCaseExists(choiceSchemaNode, caseNode)
                    || choiceSchemaNode.isMandatory();
        }
        return isDefaultCreationAllowed;
    }

    private static boolean isChoiceWithDefaultCaseExists(ChoiceSchemaNode choiceSchemaNode, SchemaNode caseNode) {
        Optional<CaseSchemaNode> defaultCase = choiceSchemaNode.getDefaultCase();
        if (defaultCase.isPresent() && !AnvExtensions.IGNORE_DEFAULT.isExtensionIn(choiceSchemaNode)) {
            if (defaultCase.get().equals(caseNode)) {
                return true;
            }
        }
        return false;
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
        if (node != null && node instanceof DataNodeContainer) {
            for (DataSchemaNode child : ((DataNodeContainer) node).getChildNodes()) {
                if (child instanceof ChoiceSchemaNode) {
                    fillCaseChildren(childrenNodes, (ChoiceSchemaNode) child);
                } else {
                    childrenNodes.add(child);
                }
            }
        }
        return childrenNodes;
    }

    private static void fillCaseChildren(List<DataSchemaNode> childrenNodes, ChoiceSchemaNode child) {
        Collection<CaseSchemaNode> cases = ((ChoiceSchemaNode) child).getCases().values();
        for (CaseSchemaNode caseNode : cases) {
            for (DataSchemaNode caseChild : caseNode.getChildNodes()) {
                if (caseChild instanceof ChoiceSchemaNode) {
                    fillCaseChildren(childrenNodes, (ChoiceSchemaNode) caseChild);
                } else {
                    childrenNodes.add(caseChild);
                }
            }
        }
    }
    
    public static Set<DataSchemaNode> getCaseChildren(CaseSchemaNode caseNode){
        Set<DataSchemaNode> caseChildren = new HashSet<>();
        addCaseChildren(caseNode, caseChildren);
        return caseChildren;
    }
    
    private static void addCaseChildren(CaseSchemaNode caseNode, Set<DataSchemaNode> caseChildren){
        for  ( DataSchemaNode dataSchemaNode : caseNode.getChildNodes()) {
            if ( isChoiceSchemaNode(dataSchemaNode)) {
                ((ChoiceSchemaNode)dataSchemaNode).getCases().values().forEach(childCaseNode -> addCaseChildren(childCaseNode, caseChildren));
            } else {
                caseChildren.add(dataSchemaNode);
            }
        }
    }
    
    public static Set<DataSchemaNode> getChoiceChildren(ChoiceSchemaNode choiceNode){
        Set<DataSchemaNode> choiceChildren = new HashSet<>();
        choiceNode.getCases().values().forEach(caseNode -> addCaseChildren(caseNode, choiceChildren));
        return choiceChildren;
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

    public static boolean isChoiceOrCaseNode(SchemaNode schemaNode) {
        if (schemaNode instanceof ChoiceSchemaNode || schemaNode instanceof CaseSchemaNode) {
            return true;
        }
        return false;
    }

    public static boolean isDataNodeSuperSet(NodeList actualChildNodes, Set<QName> definedChildNodes) {

        for (int i = 0; i < actualChildNodes.getLength(); i++) {

            Node node = actualChildNodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                String nodeLocalName = node.getLocalName();
                String nodeNamespace = node.getNamespaceURI();

                for (QName caseChildNode : definedChildNodes) {
                    if (caseChildNode.getLocalName().equals(nodeLocalName)
                            && caseChildNode.getNamespace().toString().equals(nodeNamespace)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static SchemaPath getCaseSchemaNodeFromChildNodeIfApplicable(SchemaRegistry registry,
            SchemaPath schemaPath) {
        SchemaPath parentPath = schemaPath.getParent();
        while (parentPath != null && parentPath.getLastComponent() != null) {
            SchemaNode parentnode = registry.getDataSchemaNode(parentPath);
            if (parentnode instanceof CaseSchemaNode) {
                return parentPath;
            } else {
                parentPath = getCaseSchemaNodeFromChildNodeIfApplicable(registry, parentPath);
            }
        }
        return null;
    }

    public static List<DataSchemaNode> getDataSiblingsFromParentCaseNode(SchemaPath schemaPath,
            SchemaRegistry registry) {
        List<DataSchemaNode> dataSiblings = new ArrayList<>();
        if (schemaPath != null && schemaPath.getParent() != null) {
            DataSchemaNode dsn = registry.getDataSchemaNode(schemaPath.getParent());
            if (dsn instanceof CaseSchemaNode) {
                CaseSchemaNode caseNode = (CaseSchemaNode) dsn;
                List<DataSchemaNode> siblings = getAllNodesFromCases(Arrays.asList(caseNode));
                for (DataSchemaNode sibling : siblings) {
                    if (sibling instanceof ChoiceSchemaNode) {
                        dataSiblings.addAll(getImmediateChildrenOfChoice((ChoiceSchemaNode) sibling));
                    } else {
                        dataSiblings.add(sibling);
                    }
                }
            }
        }
        return dataSiblings;
    }
    
    public static ListSchemaNode getListSchemaNodeFromNestedChoice(DataSchemaNode choiceNode, QName qName) {
        for (CaseSchemaNode caseNode : ((ChoiceSchemaNode) choiceNode).getCases().values()) {
            for (DataSchemaNode childNode : caseNode.getChildNodes()) {
                if (childNode != null && childNode instanceof ListSchemaNode && childNode.getQName().equals(qName)) {
                    return (ListSchemaNode) childNode;
                }
                else if (childNode != null && childNode instanceof ChoiceSchemaNode)  {
                    ListSchemaNode getListFromNestedChoice = getListSchemaNodeFromNestedChoice(childNode, qName);
                    if (getListFromNestedChoice != null){
                        return getListFromNestedChoice;
                    }
                }
            }
        }
        return null;
    }
    
    public static LeafListSchemaNode getLeafListSchemaNodeFromNestedChoice(DataSchemaNode choiceNode, QName qName) {
        for (CaseSchemaNode caseNode : ((ChoiceSchemaNode) choiceNode).getCases().values()) {
            for (DataSchemaNode childNode : caseNode.getChildNodes()) {
                if (childNode != null && childNode instanceof LeafListSchemaNode && childNode.getQName().equals(qName)) {
                    return (LeafListSchemaNode) childNode;
                }
                else if (childNode != null && childNode instanceof ChoiceSchemaNode)  {
                    LeafListSchemaNode getLeafListFromNestedChoice = getLeafListSchemaNodeFromNestedChoice(childNode, qName);
                    if(getLeafListFromNestedChoice != null){
                        return getLeafListFromNestedChoice;
                    }
                }
            }
        }
        return null;
    }

    public static boolean hasCaseChildInModelNode(CaseSchemaNode node, ModelNode modelNode) {
        List<DataSchemaNode> childrenNodes = new ArrayList<>(getCaseChildren(node));
        DynaBean bean = (DynaBean) modelNode.getValue();
        for (DataSchemaNode childNode : childrenNodes) {
            QName childQName = childNode.getQName();
            String stepName = ModelNodeDynaBeanFactory.getDynaBeanAttributeName(childQName.getLocalName());
            if (DataStoreValidationUtil.isReadable(bean, stepName)) {
                ModelNodeDynaBeanContext dynaBeanContext = DataStoreValidationUtil
                        .getModelNodeDynaBeanContext(modelNode.getSchemaRegistry(), stepName, childQName.getModule().getNamespace().toString(),
                                null);
                Object childValue = ModelNodeDynaBean.withContext(dynaBeanContext, () -> bean.get(stepName));
                if (childValue != null) {
                    return true;
                }
            }
        }
        return false;
    }

    
    public static boolean hasChoiceChildInModelNode(ChoiceSchemaNode node, ModelNode modelNode) {
        List<DataSchemaNode> childrenNodes = new ArrayList<>();
        fillCaseChildren(childrenNodes , node);
        DynaBean bean = (DynaBean) modelNode.getValue();
        for ( DataSchemaNode childNode : childrenNodes) {
            QName childQName = childNode.getQName();
            String stepName = ModelNodeDynaBeanFactory.getDynaBeanAttributeName(childQName.getLocalName());
            if (DataStoreValidationUtil.isReadable(bean, childNode.getPath())) {
                ModelNodeDynaBeanContext dynaBeanContext = DataStoreValidationUtil
                        .getModelNodeDynaBeanContext(modelNode.getSchemaRegistry(), stepName, childQName.getModule().getNamespace().toString(),
                                null);
                Object childValue = ModelNodeDynaBean.withContext(dynaBeanContext, () -> bean.get(stepName));
                if ( childValue != null) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean hasChoiceChildExceptNodeToBeDeleted(ChoiceSchemaNode node, ModelNode modelNode, DataSchemaNode nodeToBeDeleted){
        {
            List<DataSchemaNode> childrenNodes = new ArrayList<>();
            fillCaseChildren(childrenNodes , node);
            DynaBean bean = (DynaBean) modelNode.getValue();
            for ( DataSchemaNode childNode : childrenNodes) {
                if (!childNode.getPath().equals(nodeToBeDeleted.getPath())) {
                    QName childQName = childNode.getQName();
                    String stepName = ModelNodeDynaBeanFactory.getDynaBeanAttributeName(childQName.getLocalName());
                    if (DataStoreValidationUtil.isReadable(bean, stepName)) {
                        ModelNodeDynaBeanContext dynaBeanContext = DataStoreValidationUtil
                                .getModelNodeDynaBeanContext(modelNode.getSchemaRegistry(), stepName, childQName.getModule().getNamespace().toString(),
                                        null);
                        Object childValue = ModelNodeDynaBean.withContext(dynaBeanContext, () -> bean.get(stepName));
                        if (childValue != null) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }
    }
    
    public static boolean choiceHasDefaults(ChoiceSchemaNode node) {
        if ( isChoiceSchemaNode(node)) {
            return ((ChoiceSchemaNode) node).getDefaultCase().isPresent();
        }
        return false;
    }
    
    public static Set<DataSchemaNode> getLeafsWithDefaultValues(ChoiceSchemaNode choiceNode, ModelNode modelNode,
                                                                DSExpressionValidator expressionValidator, DSValidationContext validationContext){
        Set<DataSchemaNode> leafsWithDefaultValues = new HashSet<>();
        addLeafsWithDefaultValues(choiceNode, leafsWithDefaultValues, modelNode, expressionValidator, false, validationContext);
        return leafsWithDefaultValues;
    }
    
    private static void addLeafsWithDefaultValues(ChoiceSchemaNode choiceNode, Set<DataSchemaNode> leafsWithDefaultValues,
                                                  ModelNode modelNode, DSExpressionValidator expressionValidator, boolean nestedChoice,
                                                  DSValidationContext validationContext) {
        if ( choiceNode.getDefaultCase().isPresent()) {
            CaseSchemaNode defaultCase = choiceNode.getDefaultCase().get();
            boolean isValid = true;
            if ( defaultCase.getWhenCondition().isPresent()) {
                isValid = validate(modelNode, expressionValidator, defaultCase, validationContext);
            }
            if ( nestedChoice && choiceNode.getWhenCondition().isPresent()) { // No need to validate when on top choice node. it is already validated.
                isValid = validate(modelNode, expressionValidator, choiceNode, validationContext);
            }
            if ( isValid) {
                for ( DataSchemaNode dataSchemaNode : defaultCase.getChildNodes()) {
                    if ( (dataSchemaNode instanceof LeafSchemaNode || dataSchemaNode instanceof LeafListSchemaNode) && SchemaRegistryUtil.hasDefaults(dataSchemaNode)) {
                        leafsWithDefaultValues.add(dataSchemaNode);
                    } else if ( dataSchemaNode instanceof ChoiceSchemaNode) {
                        addLeafsWithDefaultValues((ChoiceSchemaNode) dataSchemaNode, leafsWithDefaultValues, modelNode,
                                expressionValidator, true, validationContext);
                    }
                }
            }
            
        }
    }

    private static boolean validate(ModelNode modelNode, DSExpressionValidator expressionValidator, DataSchemaNode schemaNode,
                                    DSValidationContext validationContext) {
        boolean isValid = false;
        RevisionAwareXPath xpath = schemaNode.getWhenCondition().get();
        Expression expression = JXPathUtils.getExpression(xpath.getOriginalString());
        try {
            isValid = expressionValidator.validateXPathInModelNode(expression.toString(), modelNode, null, modelNode, schemaNode, validationContext);
        } catch (Exception e) {
            // Nothing to do 
        }
        return isValid;
    }
    
    public static ChoiceSchemaNode checkParentNodeIsChoiceAndReturn(DataSchemaNode node, SchemaRegistry schemaRegistry) {
        SchemaPath parentSchemaPath = node.getPath().getParent();
        if (parentSchemaPath != null) {
            DataSchemaNode parentSchemaNode = schemaRegistry.getDataSchemaNode(parentSchemaPath);
            if (parentSchemaNode instanceof ChoiceSchemaNode) {
                return (ChoiceSchemaNode) parentSchemaNode;
            } else if ( parentSchemaNode instanceof CaseSchemaNode) {
                return getChoiceSchemaNodeFromCaseNodeSchemaPath(schemaRegistry, parentSchemaPath);
            }
        }
        return null;
    }
    
    public static void getDefaultCaseNodes(List<CaseSchemaNode> defaultCaseNodes, ChoiceSchemaNode choiceNode) {
        if (choiceNode != null) {
            Optional<CaseSchemaNode> defaultCase = choiceNode.getDefaultCase();
            if (defaultCase.isPresent()) {
                QName defaultCaseName = defaultCase.get().getQName();
                defaultCaseNodes.add(choiceNode.getCases().get(defaultCaseName));
            }
        }
    }
    
    public static ChoiceSchemaNode getChoiceParentSchemaNode(SchemaPath schemaPath, SchemaRegistry schemaRegistry) {
        SchemaPath parentSchemaPath = schemaPath.getParent();
        while (parentSchemaPath != null) {
            DataSchemaNode parentSchemaNode = schemaRegistry.getDataSchemaNode(parentSchemaPath);
            if (parentSchemaNode instanceof ChoiceSchemaNode) {
                return (ChoiceSchemaNode) parentSchemaNode;
            }
            parentSchemaPath = parentSchemaPath.getParent();
        }
        return null;
    }
    
    public static Set<CaseSchemaNode> getParentCaseSchemaNodes(SchemaPath schemaPath, SchemaRegistry schemaRegistry) {
        Set<CaseSchemaNode> caseSchemaNodes = new HashSet<>();
        SchemaPath parentSchemaPath = schemaPath.getParent();
        while ( parentSchemaPath != null) {
            DataSchemaNode parentSchemaNode = schemaRegistry.getDataSchemaNode(parentSchemaPath);
            if (parentSchemaNode instanceof CaseSchemaNode) {
                caseSchemaNodes.add((CaseSchemaNode) parentSchemaNode);
                parentSchemaPath = parentSchemaPath.getParent();
                parentSchemaNode = schemaRegistry.getDataSchemaNode(parentSchemaPath);
            } 
            if (!(parentSchemaNode instanceof ChoiceSchemaNode)) {
                break;
            }
            parentSchemaPath = parentSchemaPath.getParent();
        }
        return caseSchemaNodes;
    }
    
    public static Collection<DataSchemaNode> fillAllOtherNestedCaseDataChildNodes(SchemaRegistry schemaRegistry, SchemaPath caseChildPath, Set<DataSchemaNode> fillChilds){
        SchemaPath selectedCaseSP = getCaseSchemaNodeFromChildNodeIfApplicable(schemaRegistry,
                caseChildPath);
        if(selectedCaseSP != null) {
            CaseSchemaNode selectedCaseSN = (CaseSchemaNode) schemaRegistry.getDataSchemaNode(selectedCaseSP);
            ChoiceSchemaNode choiceSchemaNode = getChoiceSchemaNodeFromCaseNodeSchemaPath(schemaRegistry,
                    selectedCaseSP);
            Set<CaseSchemaNode> allCaseSchemaNodes = new HashSet<>();
            allCaseSchemaNodes.addAll(choiceSchemaNode.getCases().values());
            allCaseSchemaNodes.remove(selectedCaseSN);
            for(CaseSchemaNode caseSN : allCaseSchemaNodes) {
                fillChilds.addAll(getCaseChildren(caseSN));
            }
            fillAllOtherNestedCaseDataChildNodes(schemaRegistry, choiceSchemaNode.getPath(), fillChilds);
        }
        return fillChilds;
    }
}
