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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.JXPathCompiledExpression;
import org.apache.commons.jxpath.ri.compiler.Constant;
import org.apache.commons.jxpath.ri.compiler.CoreFunction;
import org.apache.commons.jxpath.ri.compiler.CoreOperation;
import org.apache.commons.jxpath.ri.compiler.CoreOperationCompare;
import org.apache.commons.jxpath.ri.compiler.CoreOperationRelationalExpression;
import org.apache.commons.jxpath.ri.compiler.Expression;
import org.apache.commons.jxpath.ri.compiler.LocationPath;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.Operation;
import org.apache.commons.jxpath.ri.compiler.Step;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeDynaBeanFactory;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.ProxyValidationModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.ChoiceCaseNodeUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeDynaBean;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.jxpath.JXPathUtils;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.util.RevisionAwareXPathImpl;

import org.broadband_forum.obbaa.netconf.api.logger.NetconfExtensions;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.LoggerFactory;

public class DataStoreValidationPathBuilder {

    private final static AdvancedLogger LOGGER = LoggerFactory.getLogger(DataStoreValidationPathBuilder.class,
            DataStoreValidationUtil.NC_DS_VALIDATION, "DEBUG", "GLOBAL");

    private final SchemaRegistry m_schemaRegistry;
    private final ModelNodeHelperRegistry m_modelNodeHelperRegistry;

    public DataStoreValidationPathBuilder(SchemaRegistry schemaRegistry, ModelNodeHelperRegistry
            modelNodeHelperRegistry) {
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
        if (targetNode.getNodeName().getPrefix() == null && qname.getLocalName().equals(targetNode.getNodeName()
                .getName())) {
            foundNode = true;
        } else if (m_schemaRegistry.getPrefix(qname.getNamespace().toString()).equals(targetNode.getNodeName()
                .getPrefix())
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
                    // E.g: currentNode is device-specific-data in
                    // /device-manager/device-holder/device/device-specific-data
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
                 * we would have located interfaces from the dataSchemaNode we received. So that step must be
                 * discarded from path.
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

    private SchemaPath getNextPath(SchemaPath parentPath, Step[] steps, int stepIndex, List<Step> path, boolean
            absPath) {
        List<Step> levelPath = new LinkedList<Step>();
        String nodeName = DataStoreValidationUtil.getLocalName(steps[stepIndex]);
        SchemaPath childPath = DataStoreValidationUtil.getChildPath(m_schemaRegistry, parentPath, nodeName);
        if (childPath != null) {
            levelPath.add(steps[stepIndex]);
            SchemaPath returnPath = null;
            if (stepIndex + 1 < steps.length) {
                returnPath = getNextPath(childPath, steps, stepIndex + 1, levelPath, absPath);
            } else {
                returnPath = childPath;
            }
            path.addAll(levelPath);
            return returnPath;
        }
        return null;
    }

    private void fetchFunctionSchemaPaths(Expression expression, DataSchemaNode inSchemaNode, Map<SchemaPath, String>
            manyPaths, String accessPath) {
        if (expression instanceof LocationPath) {
            LocationPath path = (LocationPath) expression;
            List<Step> steps = new ArrayList<>(Arrays.asList(path.getSteps()));
            List<SchemaPath> referencedPath = new ArrayList<>();
            fetchSchemaPaths(steps, inSchemaNode, referencedPath);
            for (SchemaPath schemaPath : referencedPath) {
                manyPaths.put(schemaPath, accessPath);
            }
        } else if (expression instanceof Operation) {
            if (((Operation) expression).getArguments() != null) {
                for (Expression ex : ((Operation) expression).getArguments()) {
                    fetchFunctionSchemaPaths(ex, inSchemaNode, manyPaths, accessPath);
                }
            }
        }
    }

    private void getSchemaPathsFromXPath(DataSchemaNode schemaNode, RevisionAwareXPath xPath,
                                         List<SchemaPath> aggregatedPaths) {

        Expression ex = JXPathUtils.getExpression((JXPathCompiledExpression) JXPathContext.compile(xPath.toString()));

        if (ex instanceof LocationPath) {
            LocationPath path = (LocationPath) ex;
            List<Step> steps = new ArrayList<Step>(Arrays.asList(path.getSteps()));
            fetchSchemaPaths(steps, schemaNode, aggregatedPaths);
        } else if (ex instanceof CoreOperation) {
            CoreOperation operation = (CoreOperation) ex;

            if (operation instanceof CoreOperationCompare || operation instanceof CoreOperationRelationalExpression) {
                // we will have only 2 expression here.
                // 1 -> locationPath
                // 2 -> some constant value to evaluate.
                // we need to fetch schemaPath of locationPath
                getSchemaPathsFromXPath(schemaNode,
                        new RevisionAwareXPathImpl(operation.getArguments()[0].toString(), xPath.isAbsolute()),
                        aggregatedPaths);
            } else {
                // indicates we have more than one xpath.
                for (Expression exp : operation.getArguments()) {
                    getSchemaPathsFromXPath(schemaNode, new RevisionAwareXPathImpl(exp.toString(), xPath.isAbsolute()),
                            aggregatedPaths);
                }
            }
        } else if (ex instanceof CoreFunction) {
            CoreFunction function = (CoreFunction) ex;
            for (Expression exp : function.getArguments()) {
                getSchemaPathsFromXPath(schemaNode, new RevisionAwareXPathImpl(exp.toString(), xPath.isAbsolute()),
                        aggregatedPaths);
            }
        } else {
            LOGGER.debug("Could not fetch all schemaPath for {}", ex);
        }

    }

    private void fetchSchemaPaths(List<Step> steps, DataSchemaNode schemaNode, List<SchemaPath> aggregatedPaths) {
        SchemaRegistry schemaRegistry = m_schemaRegistry;
        DataSchemaNode newNode = schemaNode;

        /**
         * for every '..' encountered, goto the parentNode
         */
        while (steps.size() > 0 && steps.get(0).getAxis() == Compiler.AXIS_PARENT) {
            newNode = schemaRegistry.getDataSchemaNode(newNode.getPath().getParent());
            if (newNode == null && steps.size() > 1 && steps.get(1).getAxis() == Compiler.AXIS_CHILD) {
                // a newNode = null indicates we have hit a root node.
                // if the path still has a parent and the next step is a child
                // we need to look for another root node.
                // it is baiscally like doing cd ../var inside /opt
                newNode = DataStoreValidationUtil.getRootSchemaNode(m_schemaRegistry, steps.get(1));
                steps.remove(0);
            }
            steps.remove(0);
        }

        if (steps.size() > 0) {
            Step step = steps.get(0);
            if (step.getNodeTest() instanceof NodeNameTest) {

                String nodeName = ((NodeNameTest) step.getNodeTest()).getNodeName().getName();
                QName qname = schemaRegistry.lookupQName(schemaNode.getQName().getNamespace().toString(), nodeName);
                SchemaPath stepSchemaPath = schemaRegistry.getDescendantSchemaPath(newNode.getPath(), qname);
                if (stepSchemaPath != null) {
                    /**
                     * if this is the last step in the xpath, add the schemaPath to the list.
                     */
                    if (steps.size() > 1) {
                        newNode = schemaRegistry.getDataSchemaNode(stepSchemaPath);
                        steps.remove(step);
                        fetchSchemaPaths(steps, newNode, aggregatedPaths);
                    } else {
                        aggregatedPaths.add(stepSchemaPath);
                    }
                } else if (newNode.getPath().getParent() != null && newNode.getPath().getParent().getLastComponent()
                        != null) {
                    DataSchemaNode parentNode = schemaRegistry.getDataSchemaNode(newNode.getPath().getParent());
                    Expression ex = new LocationPath(false, steps.toArray(new Step[steps.size()]));
                    getSchemaPathsFromXPath(parentNode, new RevisionAwareXPathImpl(ex.toString(), false),
                            aggregatedPaths);
                } else {
                    LOGGER.debug("schema path was null for qname {} and step{}", qname, newNode.getPath());
                }
            }
        }

    }

    private void fetchAccessPath(String xPath, DataSchemaNode inSchemaNode, Map<SchemaPath, String> manyPaths,
                                 boolean isCaseOrChoice) {

        Expression ex = JXPathUtils.getExpression(xPath);
        if (ex instanceof CoreOperation) {
            for (Expression exp : ((Operation) ex).getArguments()) {
                fetchAccessPath(exp.toString(), inSchemaNode, manyPaths, isCaseOrChoice);
            }
        } else if (ex instanceof LocationPath && ((LocationPath) ex).getSteps()[0].getAxis() == Compiler.AXIS_SELF) {
            manyPaths.put(inSchemaNode.getPath(), null);
        } else if (ex instanceof LocationPath && !((LocationPath) ex).isAbsolute()) {
            Step[] steps = ((LocationPath) ex).getSteps();
            DataSchemaNode schemaNode = m_schemaRegistry.getDataSchemaNode(inSchemaNode.getPath());
            /**
             * record the localName of each step as we traverse.
             *
             * eg:
             *
             * container a {
             *    leaf b{when "../c='a'";};
             *
             *    leaf c;
             * }
             *
             * xPath = ../c, inSchemaNode = leaf b
             *
             * as we traverse from b to c, we will build b/a/c. 
             * reversing the path, will give c/a/b which 
             * is the access path from c to reach b, its impact node
             *
             *
             */
            List<Step> path = new LinkedList<Step>();
            if (!isCaseOrChoice) {
                // if this is a case/choice node. Dont add it to data path
                path.add(DataStoreValidationUtil.getYangStep(schemaNode.getQName()));
            }
            SchemaPath returnPath = null;
            for (int i = 0; i < steps.length; i++) {
                if (steps[i].getAxis() == Compiler.AXIS_PARENT) {
                    boolean rootNode = false;

                    if (schemaNode != null && schemaNode.getPath().getParent().getLastComponent() == null) {
                        // indicate we are currently on a root node. 
                        // to corelate with the explanation above, we are in /opt
                        rootNode = true;
                    }
                    if (isCaseOrChoice && ChoiceCaseNodeUtil.isChoiceOrCaseNode(schemaNode)) {
                        schemaNode = m_schemaRegistry.getDataSchemaNode(schemaNode.getPath().getParent());
                    } else {
                        schemaNode = SchemaRegistryUtil.getEffectiveParentNode(schemaNode, m_schemaRegistry);
                    }

                    if (schemaNode == null && rootNode && steps[i].getAxis() == Compiler.AXIS_PARENT
                            && steps[i + 1].getAxis() == Compiler.AXIS_CHILD) {
                        path.add(steps[i]);
                        if (i + 1 < steps.length) {
                            schemaNode = DataStoreValidationUtil.getRootSchemaNode(m_schemaRegistry, steps[++i]);
                            path.add(steps[i]);
                        }
                        continue;
                    }
                    if (!ChoiceCaseNodeUtil.isChoiceOrCaseNode(schemaNode)) {
                        // we are out of case/choice steps. Now start adding data path
                        isCaseOrChoice = false;
                    }

                    if (!isCaseOrChoice) {
                        // if this is a choice/case node, dont add it to data path
                        path.add(DataStoreValidationUtil.getYangStep(schemaNode.getQName()));
                    }

                } else {
                    returnPath = getNextPath(schemaNode.getPath(), steps, i, path, false);
                    break;
                }
            }
            Collections.reverse(path);
            String sb1 = new LocationPath(false, path.toArray(new Step[0])).toString();

            if (returnPath != null) {
                manyPaths.put(returnPath, sb1);
            } else {
                List<SchemaPath> paths = new ArrayList<SchemaPath>();
                fetchSchemaPaths(new ArrayList<Step>(Arrays.asList(steps)), schemaNode, paths);
                for (SchemaPath newPath : paths) {
                    manyPaths.put(newPath, null);
                }
            }
        } else if (ex instanceof LocationPath) {
            /**
             * we are here, if it is an absolute path. access path for the root is recorded
             */
            Step[] steps = ((LocationPath) ex).getSteps();
            DataSchemaNode schemaNode = m_schemaRegistry.getDataSchemaNode(inSchemaNode.getPath());
            List<Step> inputPath = new LinkedList<Step>();
            SchemaPath inPath = DataStoreValidationUtil.buildAbsAccessPath(m_schemaRegistry, schemaNode, inputPath);
            List<Step> accessPath = new LinkedList<Step>();
            SchemaPath returnValue = getNextPath(inPath, steps, 1, accessPath, true);
            if (returnValue != null) {
                String sb1 = new LocationPath(true, inputPath.toArray(new Step[0])).toString();
                manyPaths.put(returnValue, sb1);
            }
        } else if (ex instanceof CoreFunction) {
            /**
             * here again, getting the relative path inside is difficult. So we record the access path from root
             */
            List<Step> inputPath = new LinkedList<Step>();
            DataStoreValidationUtil.buildAbsAccessPath(m_schemaRegistry, inSchemaNode, inputPath);
            String sb1 = new LocationPath(true, inputPath.toArray(new Step[0])).toString();
            /**
             * a generic count(list1), indicates it refers list1. All such
             * list1 instances in schematree are found and the path found above is store as 
             * access path for impact
             */
            fetchFunctionSchemaPaths(ex, inSchemaNode, manyPaths, sb1);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private List<ModelNode> getReferencedModelNode(DynaBean inParentBean, Step[] pathSteps, boolean nodeNotDeleted,
                                                   boolean missingParentNode) {

        List<ModelNode> returnValue = new ArrayList<ModelNode>();
        if (pathSteps != null && pathSteps.length > 0) {
            Step[] steps = pathSteps;
            DynaBean bean = inParentBean;
            Object beanGetValue = null;
            int startIndex = 0;
            int endIndex = steps.length;

            if (nodeNotDeleted) {
                startIndex = 0;
            } else {
                startIndex = 2;// ignore first step, the node that is deleted and the parent Node of the deleted node
            }
            if (startIndex == steps.length) {
                String stepName = DataStoreValidationUtil.getLocalName(steps[startIndex - 1]); // the first step from
                // current bean
                if (DataStoreValidationUtil.isReadable(inParentBean, stepName)) {
                    bean = ModelNodeDynaBeanFactory.getDynaBeanForLeaf((ModelNode) bean.get(ModelNodeWithAttributes.MODEL_NODE), bean, stepName,
                            (String) beanGetValue, null);
                }
            }
            for (int i = startIndex; i < endIndex; i++) {
                String stepName = DataStoreValidationUtil.getLocalName(steps[i]);
                if (stepName == null && steps[i].getAxis() == Compiler.AXIS_PARENT && !DataStoreValidationUtil
                        .isReadable(bean, ModelNodeWithAttributes.PARENT)) {
                    // indicates we need to look for a different tree.
                    if (i + 1 < steps.length) {
                        DataSchemaNode rootSchemaNode = DataStoreValidationUtil.getRootSchemaNode(m_schemaRegistry,
                                steps[++i]);
                        bean = DataStoreValidationUtil.getRootModelNode(m_schemaRegistry, rootSchemaNode);
                        continue;
                    }
                }
                if (!(beanGetValue instanceof String) && (DataStoreValidationUtil.isReadable(bean, stepName))) {
                    beanGetValue = bean.get(stepName);
                    if (beanGetValue instanceof String) {
                        // is a leaf/leaf-list
                        bean = ModelNodeDynaBeanFactory.getDynaBeanForLeaf((ModelNode) bean.get(ModelNodeWithAttributes.MODEL_NODE), bean,
                                stepName,
                                (String) beanGetValue, null);
                    } else if (beanGetValue instanceof ModelNodeDynaBean) {
                        bean = (DynaBean) beanGetValue;
                    } else if (beanGetValue instanceof List) {
                        // could be a list
                        if (((List) beanGetValue).size() == 1) {
                            bean = (DynaBean) ((List) beanGetValue).get(0);
                        } else {
                            List listBeans = (List) beanGetValue;
                            Step[] newSteps = new Step[steps.length - (i + 1)];
                            System.arraycopy(steps, i + 1, newSteps, 0, steps.length - (i + 1));
                            for (Object object : listBeans) {
                                if (newSteps.length > 0) {
                                    returnValue
                                            .addAll(getReferencedModelNode((DynaBean) object, newSteps,
                                                    nodeNotDeleted, missingParentNode));
                                }
                            }
                        }
                    } else if (beanGetValue instanceof Set) {
                        Set<ConfigLeafAttribute> leafLists = (Set) beanGetValue;
                        if (leafLists.size() > 0) {
                            String leafListInstanceValue = leafLists.iterator().next().getStringValue();
                            bean = ModelNodeDynaBeanFactory.getDynaBeanForLeaf((ModelNode) bean.get(ModelNodeWithAttributes.MODEL_NODE),
                                    bean, stepName,
                                    leafListInstanceValue, null);
                        } else {
                            bean = null;
                        }

                    } else if (beanGetValue != null) {
                        LOGGER.warn("unhandled type {}", beanGetValue.getClass());
                        bean = null;
                    } else if (beanGetValue == null && missingParentNode) {
                        bean = buildDummySupport(steps, i, bean);
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
            if (bean != null && DataStoreValidationUtil.isReadable(bean, ModelNodeWithAttributes.PARENT)) {
                returnValue.add((ModelNode) ((DynaBean) bean.get(ModelNodeWithAttributes.PARENT)).get(ModelNodeWithAttributes.MODEL_NODE));
            }
        }
        return returnValue;
    }

    private DynaBean buildDummySupport(Step[] steps, int currentIndex, DynaBean currentBean) {


        if (currentIndex + 1 <= steps.length) {
            if (currentIndex + 1 == steps.length) {
                // indicates the last step in the path is missing; and current bean is the parent bean.
                return ModelNodeDynaBeanFactory.getDynaBeanForLeaf((ModelNode) currentBean.get(ModelNodeWithAttributes.MODEL_NODE), currentBean,
                        DataStoreValidationUtil.getLocalName(steps[currentIndex]), null, null);
            } else {
                String stepName = DataStoreValidationUtil.getLocalName(steps[currentIndex]);
                currentIndex++;
                ModelNode parentModelNode = (ModelNode) currentBean.get(ModelNodeWithAttributes.MODEL_NODE);
                SchemaRegistry schemaRegistry = parentModelNode.getSchemaRegistry();
                SchemaPath schemaPath = DataStoreValidationUtil.getChildPath(m_schemaRegistry, parentModelNode
                        .getModelNodeSchemaPath(), stepName);
                DataSchemaNode schemaNode = schemaRegistry.getDataSchemaNode(schemaPath);
                if (schemaNode instanceof ContainerSchemaNode && !((ContainerSchemaNode) schemaNode)
                        .isPresenceContainer()) {
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
            LOGGER.debug("for steps {} currentIndex {} and dynaBean {}, unable to build dummy support", steps,
                    currentIndex,
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
     * leaf a{
     * when ../b > 1 and ../c > 2
     * type string;
     * }
     * <p>
     * leaf b{
     * type uint8;
     * }
     * <p>
     * leaf c{
     * type uint8;
     * }
     * }
     * <p>
     * The input to this function will be dataSchemaNode(a), xpath=../b > 1 and ../c > 2
     * <p>
     * The return list will contain schemaPath(c),path from a to c(../c)
     * and schemaPath(b), path from a to b(../b)
     */
    public Map<SchemaPath, String> getSchemaPathsFromXPath(DataSchemaNode schemaNode,
                                                           RevisionAwareXPath xPath) {

        String actualPath = xPath.toString();
        if (NetconfExtensions.IS_TREAT_AS_RELATIVE_PATH.isExtensionIn(schemaNode)) {
            Expression xpath = m_schemaRegistry.getRelativePath(xPath.toString(), schemaNode);
            if (xpath != null) {
                actualPath = xPath.toString();
            }
        }
        Map<SchemaPath, String> manyPaths = new HashMap<SchemaPath, String>();
        fetchAccessPath(actualPath, schemaNode, manyPaths, ChoiceCaseNodeUtil.isChoiceOrCaseNode(schemaNode));
        for (Map.Entry<SchemaPath, String> path : manyPaths.entrySet()) {
            if (path.getValue() != null) {
                /**
                 * Transform the access path so save time during validation.
                 */
                String newPath = DataStoreValidationUtil.getDynaBeanAlignedPath((LocationPath) JXPathUtils
                        .getExpression(path.getValue())).toString();
                manyPaths.put(path.getKey(), newPath);
            }
        }
        return manyPaths;
    }


    public List<ModelNode> getParentModelNodeWithAccessPath(DynaBean inParentBean, Step[] pathSteps, boolean
            nodeNotDeleted) {
        return getReferencedModelNode(inParentBean, pathSteps, nodeNotDeleted, false);
    }

    public List<ModelNode> getMissingParentNodeWithAccessPath(DynaBean inParentBean, Step[] pathSteps, boolean
            nodeNotDeleted) {
        return getReferencedModelNode(inParentBean, pathSteps, nodeNotDeleted, true);
    }

}
