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

import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeDynaBeanFactory.getModelNodeAttributeName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.compiler.Constant;
import org.apache.commons.jxpath.ri.compiler.CoreFunction;
import org.apache.commons.jxpath.ri.compiler.CoreOperation;
import org.apache.commons.jxpath.ri.compiler.Expression;
import org.apache.commons.jxpath.ri.compiler.LocationPath;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.Step;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigOperations;
import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.ChoiceCaseNodeUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeDynaBean;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeDynaBeanContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeDynaBeanFactory;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.DSValidationContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.ProxyValidationModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.jxpath.JXPathUtils;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public class DataStoreValidationPathBuilder {

    private final static AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(DataStoreValidationPathBuilder.class, LogAppNames.NETCONF_STACK);

    private final SchemaRegistry m_schemaRegistry;
    private final ModelNodeHelperRegistry m_modelNodeHelperRegistry;
    
    public DataStoreValidationPathBuilder(SchemaRegistry schemaRegistry, ModelNodeHelperRegistry modelNodeHelperRegistry) {
        m_schemaRegistry = schemaRegistry;
        m_modelNodeHelperRegistry = modelNodeHelperRegistry;
    }

    private boolean isLastAugmentedStep(Step[] augSteps, DataSchemaNode currentNode) {
        Step lastStep = augSteps[augSteps.length - 1];
        if (lastStep.getNodeTest() instanceof NodeNameTest) {
            if (isNodeSame(((NodeNameTest) lastStep.getNodeTest()), currentNode.getQName())) {
                return true;
            }
        }
        return false;
    }
        
    private boolean isNodeSame(NodeNameTest targetNode, QName qname) {
        boolean foundNode = false;
        if (targetNode.getNodeName().getPrefix() == null && qname.getLocalName().equals(targetNode.getNodeName().getName())) {
            foundNode = true;
        } else if (m_schemaRegistry.getPrefix(qname.getNamespace().toString()).equals(targetNode.getNodeName().getPrefix())
                && qname.getLocalName().equals(targetNode.getNodeName().getName())) {
            foundNode = true;
        }
        return foundNode;
    }

    private String getRelativePath(String augmentation, DataSchemaNode schemaNode, Expression expression) {
        String returnValue = null;
        boolean foundRootNode = false;
        boolean foundLastAugStep = false;
        DataSchemaNode currentNode = m_schemaRegistry.getDataSchemaNode(schemaNode.getPath());
        NodeNameTest targetNode = null;
        int parentPathCount = 0;
        LocationPath path = (LocationPath) expression;
        LocationPath augPath = (LocationPath) JXPathUtils.getExpression(augmentation);

        Step[] oldSteps = path.getSteps();
        Step[] augSteps = augPath.getSteps();
        Step[] newSteps = new Step[oldSteps.length - augSteps.length];

        /*
         * Here after the loop
         * e.g: oldSteps --> /device-manager/device-holder/device/device-specific-data/interfaces/interface/name
         *      augSteps --> /device-manager/device-holder/device/device-specific-data
         *      newSteps --> /interfaces/interface/name
         */
        System.arraycopy(oldSteps, augSteps.length, newSteps, 0, (oldSteps.length - augSteps.length));

        if (newSteps[0].getNodeTest() instanceof NodeNameTest) {
            targetNode = (NodeNameTest) newSteps[0].getNodeTest();
        }

        if (targetNode == null) {
            foundRootNode = true;
        }
        do {
            if (currentNode == null) {
                break;
            }

            SchemaPath schemaPath = currentNode.getPath();

            if (ChoiceCaseNodeUtil.isChoiceOrCaseNode(schemaNode)) {
                /**
                 * Indicates the current xpath is on a case/choice. We need to take into account the choice step also.
                 */
                SchemaPath parentPath = schemaPath.getParent();
                currentNode = m_schemaRegistry.getDataSchemaNode(parentPath);
            } else {
                if (isLastAugmentedStep(augSteps, currentNode)) {
                    // indicates currentNode is last augmentedStep.
                    // E.g: currentNode is device-specific-data in /device-manager/device-holder/device/device-specific-data
                    Collection<DataSchemaNode> children = m_schemaRegistry.getChildren(currentNode.getPath());
                    for (DataSchemaNode child : children) {
                        if (isNodeSame(targetNode, child.getQName())) {
                            foundLastAugStep = true;
                            break;
                        }
                    }
                } else {
                    // for every getParent() traversal add "../" to the path
                    parentPathCount++;
                    currentNode = SchemaRegistryUtil.getEffectiveParentNode(currentNode, m_schemaRegistry);
                }
            }

        } while (!foundRootNode && !foundLastAugStep);

        if (foundRootNode || foundLastAugStep) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < parentPathCount; i++) {
                if (i == 0) {
                    builder.append(DataStoreValidationUtil.PARENT_PATTERN);
                } else {
                    builder.append(DataStoreValidationUtil.SLASH).append(DataStoreValidationUtil.PARENT_PATTERN);
                }
            }
            int startIndex = 0;
            if (foundRootNode) {
                /*
                 * Here newSteps --> /interfaces/interface/name
                 * we would have located interfaces from the dataSchemaNode we received. So that step must be discarded from path. 
                 * The new path will be ../interface/name, the number of ../ depends on the getParent() call
                 */
                startIndex = 1;
            }
            for (int i = startIndex; i < newSteps.length; i++) {
                if (builder.toString().isEmpty()) {
                    builder.append(newSteps[i].toString());
                } else {
                    builder.append(DataStoreValidationUtil.SLASH).append(newSteps[i].toString());
                }
            }
            returnValue = builder.toString();
        }
        return returnValue;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private List<ModelNode> getReferencedModelNode(EditContainmentNode editNode, DynaBean inParentBean, Step[] pathSteps,
                                                   DataSchemaNode referenceNode, boolean nodeNotDeleted, boolean missingParentNode,
                                                   DSValidationContext validationContext) {
        boolean isChoiceOrCaseNode = ChoiceCaseNodeUtil.isChoiceOrCaseNode(referenceNode);
        List<ModelNode> returnValue = validationContext.getRetrievedParentModelNodes().fetchAlreadyRegisteredParentModelNodes(editNode, inParentBean, pathSteps, nodeNotDeleted, missingParentNode, isChoiceOrCaseNode);
        if(returnValue != null) {
            return returnValue;
        }
        returnValue = new ArrayList<>();
        if (pathSteps != null && pathSteps.length > 0) {
            Step[] steps = pathSteps;
            DynaBean bean = inParentBean;
            Object beanGetValue = null;
            int startIndex = 0;
            int endIndex = steps.length;

            if (nodeNotDeleted) {
                startIndex = 0;
            } else {
                startIndex = 2;// ignore first step, the node that is deleted
                               // and the parent Node of the deleted node
                if (EditConfigOperations.REMOVE.equals(editNode.getEditOperation())
                        || EditConfigOperations.DELETE.equals(editNode.getEditOperation())) {
                    for (int i = startIndex - 1; i < endIndex; i++) {
                        String stepName = DataStoreValidationUtil.getLocalName(steps[i]);
                        if (editNode.getName().equals(stepName)
                                && DataStoreValidationUtil.isReadable(inParentBean, stepName)) {
                            startIndex = i + 2;
                            break;
                        }
                    }
                }
            }
            if (startIndex == steps.length) {
                String stepName = DataStoreValidationUtil.getLocalName(steps[startIndex-1]); // the first step from current bean
                if (DataStoreValidationUtil.isReadable(inParentBean, stepName)) {
                    bean = ModelNodeDynaBeanFactory.getDynaBeanForLeaf((ModelNode) bean.get(ModelNodeWithAttributes.MODEL_NODE), bean, stepName,
                            (String) beanGetValue, null);
                }
            }
            for (int i = startIndex; i < endIndex; i++) {
                String stepName = DataStoreValidationUtil.getLocalName(steps[i]);
                String prefix = DataStoreValidationUtil.getPrefix(steps[i]);
                if (stepName == null && steps[i].getAxis() == Compiler.AXIS_PARENT && !DataStoreValidationUtil.isReadable(bean, ModelNodeWithAttributes.PARENT)) {
                    // indicates we need to look for a different tree.
                    if (i + 1 < steps.length) {
                        DataSchemaNode rootSchemaNode = DataStoreValidationUtil.getRootSchemaNode(m_schemaRegistry, steps[++i]);
                        bean = DataStoreValidationUtil.getRootModelNode(m_schemaRegistry, rootSchemaNode, validationContext);
                        continue;
                    }
                }
                boolean shouldLookInBean = !(beanGetValue instanceof String) && (DataStoreValidationUtil.isReadable(bean, stepName));
                if (shouldLookInBean) {
                    shouldLookInBean = isValidChildOfBean(bean, stepName, prefix, shouldLookInBean);
                }
                if (shouldLookInBean) {
                    SchemaRegistry schemaRegistry = ((ModelNode) bean.get(ModelNodeWithAttributes.MODEL_NODE)).getSchemaRegistry();
                    ModelNodeDynaBeanContext dynaBeanContext = DataStoreValidationUtil
                            .getModelNodeDynaBeanContext(schemaRegistry, stepName, schemaRegistry
                                            .getNamespaceURI(DataStoreValidationUtil.getPrefix(steps[i])),
                                    null);
                    final DynaBean beanforContext = bean;
                    beanGetValue = ModelNodeDynaBean.withContext(dynaBeanContext, () -> beanforContext.get(stepName));
                    if (beanGetValue instanceof String) {
                        // is a leaf/leaf-list
                        bean = ModelNodeDynaBeanFactory.getDynaBeanForLeaf((ModelNode) bean.get(ModelNodeWithAttributes.MODEL_NODE), bean, stepName,
                                (String) beanGetValue, null);
                    } else if (beanGetValue instanceof ModelNodeDynaBean) {
                        bean = (DynaBean) beanGetValue;
                    } else if (beanGetValue instanceof List) {
                        // could be a list
                        if (((List) beanGetValue).size() == 1) {
                            bean = (DynaBean) ((List) beanGetValue).get(0);
                        } else {
                            List listBeans = (List) beanGetValue;
                            Step[] newSteps = new Step[steps.length - (i+1)];
                            System.arraycopy(steps, i+1, newSteps, 0, steps.length-(i+1));
                            for (Object object : listBeans) {
                                if (newSteps.length > 0) {
                                    returnValue.addAll(getReferencedModelNode(editNode, (DynaBean) object, newSteps, referenceNode, true,
                                            missingParentNode, validationContext));
                                }
                            }
                            if ( newSteps.length == 0) { // Referring to the same list,here we can take any list entry. Because we will take its parent. So it doesn't matter which list entry it is, both the list entries parent is same.
                                bean = (DynaBean) ((List) beanGetValue).get(0);
                            } 
                            break;
                        }
                    } else if (beanGetValue instanceof Set) {
                        Set<ConfigLeafAttribute> leafLists = (Set) beanGetValue;
                        if (leafLists.size() > 0) {
                            String leafListInstanceValue = leafLists.iterator().next().getStringValue();
                            bean = ModelNodeDynaBeanFactory.getDynaBeanForLeaf((ModelNode) bean.get(ModelNodeWithAttributes.MODEL_NODE), bean, stepName,
                                    leafListInstanceValue, null);
                        } else {
                            bean = null;
                        }

                    } else if (beanGetValue != null) {
                        LOGGER.warn("unhandled type {}", beanGetValue.getClass());
                        bean = null;
                    } else if (beanGetValue == null && missingParentNode) {
                        bean = buildDummySupport(steps,i, bean);
                        break;
                    } else if (i == steps.length - 1 && beanGetValue == null
                            && validationContext.getImpactValidation()) {

                        // indicates it is an impact validation.
                        // need to check if this is the last step in the accessPath and if it is missing,
                        // we are already on the parentPath.
                        returnValue.add((ModelNode)bean.get(ModelNodeWithAttributes.MODEL_NODE));
                        
                        // we set the startIndex = steps.length, so that the paret of the current bean
                        // is not added as the identified parentNode. 
                        startIndex = steps.length;
                        break;
                    } else {
                        bean = null;
                        break;
                    }
                } else if (DataStoreValidationUtil.isReadable(bean, ModelNodeWithAttributes.PARENT)) {

                    // check if it is the parent step
                    beanGetValue = (DynaBean) bean.get(ModelNodeWithAttributes.PARENT);
                    ModelNode parent = (ModelNode) ((DynaBean) beanGetValue).get(ModelNodeWithAttributes.MODEL_NODE);
                    if (parent.getQName().getLocalName().equals(stepName)) {
                        bean = (DynaBean) beanGetValue;
                    } else if (missingParentNode) {
                        bean = buildDummySupport(steps, i, bean);
                        break;
                    } else {
                        bean = null;
                        break;
                    }
                } else if (missingParentNode) {
                    bean = buildDummySupport(steps, i, bean);
                    break;
                } else {
                    bean = null;
                    break;
                }
            }
            if (bean != null && DataStoreValidationUtil.isReadable(bean, ModelNodeWithAttributes.PARENT) && !(startIndex == steps.length)) {
            	// if start index == steps.length indicates, there is a possibility of deleted node
            	// and we are already at the parent of the deleted node
                if (isChoiceOrCaseNode) {
                    if ( DataStoreValidationUtil.isReadable(bean, ModelNodeWithAttributes.MODEL_NODE)) {
                        returnValue.add((ModelNode) ((DynaBean) bean).get(ModelNodeWithAttributes.MODEL_NODE));
                    } else { // proxy model node case
                        returnValue.add((ModelNode) ((DynaBean) bean.get(ModelNodeWithAttributes.PARENT)).get(ModelNodeWithAttributes.MODEL_NODE));
                    }
                } else {
                    returnValue.add((ModelNode) ((DynaBean) bean.get(ModelNodeWithAttributes.PARENT)).get(ModelNodeWithAttributes.MODEL_NODE));
                }
            }
        }
        validationContext.getRetrievedParentModelNodes().registerParentModelNodes(returnValue, editNode, inParentBean, pathSteps, nodeNotDeleted, missingParentNode, isChoiceOrCaseNode);
        return returnValue;
    }

    private boolean isValidChildOfBean(DynaBean bean, String stepName, String prefix, boolean shouldLookInBean) {
        SchemaRegistry schemaRegistry = ((ModelNode) bean.get(ModelNodeWithAttributes.MODEL_NODE)).getSchemaRegistry();
        stepName = getModelNodeAttributeName(stepName);
        if (prefix != null) {
            String namespaceURI = schemaRegistry.getNamespaceURI(prefix);
            if(namespaceURI != null) {
                String moduleName = schemaRegistry.getModuleNameByNamespace(namespaceURI);
                if(moduleName != null) {
                    SchemaPath childPathModuleNameAware = DataStoreValidationUtil.getChildPathModuleNameAware(schemaRegistry,
                            ((ModelNode) bean.get(ModelNodeWithAttributes.MODEL_NODE)).getModelNodeSchemaPath(), stepName, moduleName);
                    if (childPathModuleNameAware == null) {
                        shouldLookInBean = false;
                    }
                }
            }
        }
        return shouldLookInBean;
    }

    private DynaBean buildDummySupport(Step[] steps, int currentIndex, DynaBean currentBean) {

        
        if (currentIndex + 1 <= steps.length) {
            if (currentIndex + 1 == steps.length) {
                return ModelNodeDynaBeanFactory.getDynaBeanForLeaf((ModelNode) currentBean.get(ModelNodeWithAttributes.MODEL_NODE), currentBean,
                        DataStoreValidationUtil.getLocalName(steps[currentIndex]), null, null);
            } else {
                String stepName = DataStoreValidationUtil.getLocalName(steps[currentIndex]);
                currentIndex++;
                ModelNode parentModelNode = DataStoreValidationUtil.isReadable(currentBean, ModelNodeWithAttributes.MODEL_NODE) ? (ModelNode) currentBean.get(ModelNodeWithAttributes.MODEL_NODE) : null;
                if(parentModelNode == null){
                    LOGGER.error("Could not get model-node attribute from dynaBean {} for current step {}", currentBean, stepName);
                    return null;
                }
                SchemaRegistry schemaRegistry = parentModelNode.getSchemaRegistry();
                SchemaPath schemaPath = DataStoreValidationUtil.getChildPath(schemaRegistry, parentModelNode.getModelNodeSchemaPath(), stepName);
                DataSchemaNode schemaNode = schemaRegistry.getDataSchemaNode(schemaPath);
                if (schemaNode instanceof ContainerSchemaNode && !((ContainerSchemaNode) schemaNode).isPresenceContainer()) {
                    ProxyValidationModelNode modelNode = new ProxyValidationModelNode(parentModelNode,
                            m_modelNodeHelperRegistry, schemaPath);
                    ModelNodeDynaBean contextBean = modelNode.getValue();
                    if (parentModelNode instanceof ProxyValidationModelNode) {
                        ModelNodeDynaBean parentBean = (ModelNodeDynaBean) currentBean;
                        parentBean.set(stepName, contextBean);
                    }
                    return buildDummySupport(steps, currentIndex, contextBean);
                }
            }
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("for steps {} currentIndex {} and dynaBean {}, unable to build dummy support", steps, currentIndex,
                    currentBean);
        }
        return null;
    }
    
    /**
     * Given a original abs path, its augmentation and current schemaNode on which the path is defined,
     * the method returns a relative path, that is used to access the node at the given original path
     * from the current schemaNode. (More of an access path from currentNode to the targetNode)
     */
    public String getRelativePath(String originalPath, String augmentation, DataSchemaNode schemaNode) {
        String returnValue = originalPath;
        Expression expression = JXPathUtils.getExpression(originalPath);
        if (expression instanceof LocationPath) {
            if (augmentation != null && originalPath.startsWith(augmentation)) {
                returnValue = getRelativePath(augmentation, schemaNode, expression);
            }
        } else if (expression instanceof CoreOperation) {
            CoreOperation operation = (CoreOperation) expression;
            Expression[] newExpressions = new Expression[operation.getArguments().length];
            int index = 0;
            for (Expression ex : operation.getArguments()) {
                if (ex instanceof Constant) {
                    newExpressions[index++] = ex;
                } else {
                    String path = getRelativePath(ex.toString(), augmentation, schemaNode);
                    newExpressions[index++] = JXPathUtils.getExpression(path);
                }
            }
            CoreOperation newOperation = JXPathUtils.getCoreOperation(operation, newExpressions);
            returnValue = newOperation.toString();
        } else if (expression instanceof CoreFunction) {
            CoreFunction function = (CoreFunction) expression;
            Expression[] newExpressions = new Expression[function.getArgumentCount()];
            int index = 0;
            for (Expression ex : function.getArguments()) {
                if (ex instanceof Constant) {
                    newExpressions[index++] = ex;
                } else {
                    if (augmentation == null) {
                        augmentation = m_schemaRegistry.getMatchingPath(ex.toString());
                    }
                    String path = getRelativePath(ex.toString(), augmentation, schemaNode);
                    newExpressions[index++] = JXPathUtils.getExpression(path);
                }
            }
            CoreFunction newFunction = JXPathUtils.getCoreFunction(function.getFunctionCode(), newExpressions);
            returnValue = newFunction.toString();
        } else {
            LOGGER.info("relative path not built for path :{} and augmentation:{}", originalPath, augmentation);
        }
        return returnValue;
    }

    /**
     * For the given dataSchemaNode and xpath, the target schemaPath will be returned as a list. 
     * Eg:
     * container a{
     *     leaf a{
     *         when ../b > 1 and ../c > 2
     *         type string;
     *     }
     *     
     *     leaf b{
     *         type uint8;
     *     }
     *     
     *     leaf c{
     *         type uint8;
     *     }
     * }
     * 
     * The input to this function will be dataSchemaNode(a), xpath=../b > 1 and ../c > 2
     * 
     * The return list will contain schemaPath(c),path from a to c(../c) 
     * and schemaPath(b), path from a to b(../b)
     */
    public Map<SchemaPath, ArrayList<String>> getSchemaPathsFromXPath(DataSchemaNode schemaNode,
            String xPathString, SchemaPath augmentedSP) {
    	return AccessSchemaPathUtil.getSchemaPathsFromXPath(m_schemaRegistry, schemaNode, xPathString, augmentedSP);
    }


    public List<ModelNode> getParentModelNodeWithAccessPath(EditContainmentNode editNode, DynaBean inParentBean, Step[] pathSteps,
                                                            DataSchemaNode referenceNode, boolean nodeNotDeleted,
                                                            boolean missingParentNode, DSValidationContext validationContext) {
        return getReferencedModelNode(editNode,inParentBean, pathSteps, referenceNode, nodeNotDeleted, missingParentNode, validationContext);
    }

    public Map<SchemaPath, String> getValidationHints(Set<SchemaPath> allReferredSPs, String hintStr) {
        Map<SchemaPath, String> hints = new HashMap<>();
        String [] hintsForSps = hintStr.split("\\?\\?\\?");
        if(hintsForSps.length<1){
            throw new IllegalArgumentException("Cannot parse extension argument: "+hintStr);
        }
        for(String hintForSp : hintsForSps){
            String [] split = hintForSp.trim().split("->");
            if(split.length > 2){
                throw new IllegalArgumentException("Cannot parse part of extension argument: "+hintForSp);
            }
            String spStr = split[0].trim();
            String hint = split[1].trim();
            if("*".equals(spStr)){
                hints.clear();
                for (SchemaPath referredSP : allReferredSPs) {
                    hints.put(referredSP, hint);
                }
                break;
            }
            SchemaPath sp = SchemaPathBuilder.fromMultipleStrings(spStr);
            hints.put(sp, hint);
        }
        return hints;
    }
}
