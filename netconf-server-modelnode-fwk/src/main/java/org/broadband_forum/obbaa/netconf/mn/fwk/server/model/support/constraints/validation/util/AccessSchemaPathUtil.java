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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.JXPathCompiledExpression;
import org.apache.commons.jxpath.ri.compiler.CoreFunction;
import org.apache.commons.jxpath.ri.compiler.CoreOperation;
import org.apache.commons.jxpath.ri.compiler.CoreOperationCompare;
import org.apache.commons.jxpath.ri.compiler.CoreOperationRelationalExpression;
import org.apache.commons.jxpath.ri.compiler.Expression;
import org.apache.commons.jxpath.ri.compiler.ExpressionPath;
import org.apache.commons.jxpath.ri.compiler.ExtensionFunction;
import org.apache.commons.jxpath.ri.compiler.LocationPath;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.Operation;
import org.apache.commons.jxpath.ri.compiler.Step;
import org.broadband_forum.obbaa.netconf.api.logger.NetconfExtensions;
import org.broadband_forum.obbaa.netconf.api.util.DataPath;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.ChoiceCaseNodeUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.DataPathUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.jxpath.JXPathUtils;
import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.stmt.InputEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafListEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ListEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OutputEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.RevisionAwareXPathImpl;

public class AccessSchemaPathUtil {
    public static final ThreadLocal<Stack<Boolean>> c_withinPredicate = ThreadLocal.withInitial(() -> new Stack<>());
    public static final ThreadLocal<XPathInfo> c_xpathExpr = ThreadLocal.withInitial(() -> null);
    private final static AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(AccessSchemaPathUtil.class, LogAppNames.NETCONF_STACK);
    private static final String NODE_CONSTRAINT_ASSOCIATED_MODULE_CACHE = "NODE_CONSTRAINT_ASSOCIATED_MODULE_CACHE";

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
    public static Map<SchemaPath, ArrayList<String>> getSchemaPathsFromXPath(SchemaRegistry schemaRegistry, DataSchemaNode schemaNode,
            String xPathString, SchemaPath augmentedSP) {

        Map<SchemaPath, ArrayList<String>> manyPaths = new HashMap<SchemaPath, ArrayList<String>>();
        try {
            String actualPath = xPathString;
            if (NetconfExtensions.IS_TREAT_AS_RELATIVE_PATH.isExtensionIn(schemaNode)) {
                Expression xpath = schemaRegistry.getRelativePath(xPathString, schemaNode);
                if (xpath != null) {
                    actualPath = xpath.toString();
                }
            }
            fetchAccessPath(schemaRegistry, actualPath, schemaNode, manyPaths, ChoiceCaseNodeUtil.isChoiceOrCaseNode(schemaNode), augmentedSP, schemaNode);
            for (Map.Entry<SchemaPath, ArrayList<String>> path : manyPaths.entrySet()) {
                if (path.getValue() != null) {
                    ArrayList<String> accessPaths = new ArrayList<String>();
                    for(String xpath : path.getValue()){
                        /**
                         * Transform the access path so save time during validation.
                         */
                        String newPath = DataStoreValidationUtil.getDynaBeanAlignedPath((LocationPath) JXPathUtils.getExpression(xpath)).toString();
                        // Avoiding append of duplicate access for same schemanode path
                        if(!accessPaths.contains(newPath)) {
                            accessPaths.add(newPath);
                        }
                    }
                    manyPaths.put(path.getKey(), accessPaths);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(String.format("Problem processing xpath '%s' on schemaNode '%s'", xPathString, schemaNode.getPath()), e);
        }
        return manyPaths;
    }

    private static SchemaPath getNextPath(SchemaRegistry schemaRegistry, SchemaPath parentPath, Step[] steps, int stepIndex, List<Step> path, boolean absPath, Map<SchemaPath, ArrayList<String>> manyPaths, boolean isCaseOrChoice, SchemaPath augmentedSP) {
        List<Step> levelPath = new LinkedList<Step>();
        Step step = steps[stepIndex];

        String nodeName = DataStoreValidationUtil.getLocalName(step);
        String prefix = DataStoreValidationUtil.getPrefix(step);
        SchemaPath childPath;
        if ( step.getAxis() == Compiler.AXIS_PARENT){
            return parentPath;

        } else if(prefix != null){
            childPath = DataStoreValidationUtil.getChildPathPrefixAware(schemaRegistry, parentPath, nodeName, prefix);
        } else {
            childPath = DataStoreValidationUtil.getChildPath(schemaRegistry, parentPath, nodeName);
        }
        if (childPath != null) {
            DataSchemaNode childSN = schemaRegistry.getDataSchemaNode(childPath);


            if (!c_withinPredicate.get().empty() && childSN instanceof ListSchemaNode) {
                if (step.getPredicates().length < 1) {
                    String xPathString = c_xpathExpr.get().m_xPathString;
                    Expression xPath = JXPathUtils.getExpression(xPathString);
                    schemaRegistry.addExpressionsWithoutKeysInList(xPath.toString(), new LocationPath(absPath, steps));
                }
            }
            levelPath.add(DataStoreValidationUtil.getYangStep(schemaRegistry, childSN.getQName()));
            SchemaPath returnPath = null;
            if (stepIndex + 1 < steps.length) {
                returnPath = getNextPath(schemaRegistry, childPath, steps, stepIndex + 1, levelPath, absPath, manyPaths, isCaseOrChoice, augmentedSP);
            } else {
                returnPath = childPath;
            }
            path.addAll(levelPath);
            return returnPath;
        }        
        return null;
    }

    private static void fetchFunctionSchemaPaths(SchemaRegistry schemaRegistry, Expression expression, SchemaNode inSchemaNode, Map<SchemaPath, ArrayList<String>> manyPaths, String accessPath, boolean isCaseOrChoice, SchemaPath augmentedSP) {
        if (expression instanceof LocationPath) {
            SchemaPath parentPath = inSchemaNode.getPath();
            SchemaNode contextNode = inSchemaNode;
            SchemaNode parentNode = inSchemaNode;
            LocationPath path = (LocationPath) expression;
            List<Step> steps = new ArrayList<>(Arrays.asList(path.getSteps()));
            List<SchemaPath> referencedPath = new ArrayList<>();
            DataSchemaNode rootParentNode =  DataStoreValidationUtil.getRootSchemaNode(schemaRegistry, steps.get(0));
            if(rootParentNode != null){
                SchemaPath rootParentPath = rootParentNode.getPath();
                if(!rootParentPath.getLastComponent().getLocalName().equals(parentPath.getLastComponent().getLocalName())){
                    parentNode = rootParentNode;
                }
            }
            if ( ((LocationPath) expression).isAbsolute() && steps.size() > 1){
                steps.remove(0);
                if (steps.isEmpty()){
                    ArrayList<String> accessPaths = manyPaths.get(parentNode.getPath());
                    if( accessPaths == null){
                        accessPaths = new ArrayList<>();
                    }
                    accessPaths.add(accessPath);
                    manyPaths.put(parentNode.getPath(), accessPaths);
                }
            }
            fetchSchemaPaths(schemaRegistry, steps, parentNode, referencedPath, manyPaths, isCaseOrChoice, augmentedSP, contextNode);
            for (SchemaPath schemaPath : referencedPath) {
                ArrayList<String> accessPaths = manyPaths.get(schemaPath);
                if( accessPaths == null){
                    accessPaths = new ArrayList<>();
                }
                accessPaths.add(accessPath);
                manyPaths.put(schemaPath, accessPaths);
            }
        } else if (expression instanceof Operation) {
            if ( ((Operation) expression).getArguments() != null){
                for (Expression ex : ((Operation) expression).getArguments()) {
                    fetchFunctionSchemaPaths(schemaRegistry, ex, inSchemaNode, manyPaths, accessPath, isCaseOrChoice, augmentedSP);
                }
            }
        } else {
            fetchAccessPath(schemaRegistry, expression.toString(), inSchemaNode, manyPaths, isCaseOrChoice, augmentedSP, inSchemaNode);
        }
    }

    private static void getSchemaPathsFromXPath(SchemaRegistry schemaRegistry, DataSchemaNode schemaNode, RevisionAwareXPath xPath,
            List<SchemaPath> aggregatedPaths, Map<SchemaPath, ArrayList<String>> manyPaths, boolean isCaseOrChoice, SchemaPath augmentedSP) {

        Expression ex = JXPathUtils.getExpression((JXPathCompiledExpression) JXPathContext.compile(xPath.getOriginalString()));

        if (ex instanceof LocationPath) {
            LocationPath path = (LocationPath) ex;
            List<Step> steps = new ArrayList<Step>(Arrays.asList(path.getSteps()));
            fetchSchemaPaths(schemaRegistry, steps, schemaNode, aggregatedPaths, manyPaths, isCaseOrChoice, augmentedSP, schemaNode);
        } else if (ex instanceof CoreOperation) {
            CoreOperation operation = (CoreOperation) ex;

            if (operation instanceof CoreOperationCompare || operation instanceof CoreOperationRelationalExpression) {
                // we will have only 2 expression here.
                // 1 -> locationPath
                // 2 -> some constant value to evaluate.
                // we need to fetch schemaPath of locationPath
                getSchemaPathsFromXPath(schemaRegistry, schemaNode,
                        new RevisionAwareXPathImpl(operation.getArguments()[0].toString(), xPath.isAbsolute()), aggregatedPaths, manyPaths, isCaseOrChoice, augmentedSP);
            } else {
                // indicates we have more than one xpath.
                for (Expression exp : operation.getArguments()) {
                    getSchemaPathsFromXPath(schemaRegistry, schemaNode, new RevisionAwareXPathImpl(exp.toString(), xPath.isAbsolute()),
                            aggregatedPaths, manyPaths, isCaseOrChoice, augmentedSP);
                }
            }
        } else if (ex instanceof CoreFunction) {
            CoreFunction function = (CoreFunction) ex;
            for (Expression exp : function.getArguments()) {
                getSchemaPathsFromXPath(schemaRegistry, schemaNode, new RevisionAwareXPathImpl(exp.toString(), xPath.isAbsolute()),
                        aggregatedPaths, manyPaths, isCaseOrChoice, augmentedSP);
            }
        } else {
            LOGGER.debug("Could not fetch all schemaPath for {}", ex);
        }

    }

    private static void fetchSchemaPaths(SchemaRegistry schemaRegistry, List<Step> steps, SchemaNode schemaNode, List<SchemaPath> aggregatedPaths, Map<SchemaPath, ArrayList<String>> manyPaths, boolean isCaseOrChoice, SchemaPath augmentedSP, SchemaNode contextNode) {
        SchemaNode newNode = schemaNode;

        /**
         * for every '..' encountered, goto the parentNode
         */
        while (newNode != null && steps.size() > 0 && steps.get(0).getAxis() == Compiler.AXIS_PARENT) {
            SchemaNode previousNode = newNode;
            newNode = schemaRegistry.getNonChoiceParent(newNode.getPath());
            if (newNode == null && steps.size() > 1 && steps.get(1).getAxis() == Compiler.AXIS_CHILD) {
                // a newNode = null indicates we have hit a root node.
                // if the path still has a parent and the next step is a child
                // we need to look for another root node.
                // it is baiscally like doing cd ../var inside /opt
                newNode = DataStoreValidationUtil.getRootSchemaNode(schemaRegistry, steps.get(1));
                // if root node does not exists in xpath expression, then we can get rootschema node from previous node
                if(newNode == null){
                    newNode = DataStoreValidationUtil.getRootSchemaNode(schemaRegistry, previousNode.getQName());
                } else {
                    steps.remove(0);
                }
            }
            steps.remove(0);
        }

        if (steps.size() > 0) {
            Step step = steps.get(0);
            Expression[] predicates = step.getPredicates();
            if (step.getNodeTest() instanceof NodeNameTest) {
                if(contextNode instanceof ListEffectiveStatement || contextNode instanceof LeafListEffectiveStatement) {
                    c_withinPredicate.get().push(true);
                }
                try {
                    handlePredicates(schemaRegistry, contextNode, manyPaths, isCaseOrChoice, augmentedSP, predicates);
                }finally {
                    if(contextNode instanceof ListEffectiveStatement || contextNode instanceof LeafListEffectiveStatement) {
                        c_withinPredicate.get().pop();
                    }
                }

                org.apache.commons.jxpath.ri.QName stepNodeName = ((NodeNameTest) step.getNodeTest()).getNodeName();

                String nodeName = stepNodeName.getName();
                String namespace = schemaNode.getQName().getNamespace().toString();
                if ( stepNodeName.getPrefix() != null){
                    Module module = getNodeConstraintAssociatedModuleInCache();
                    if(module != null) {
                        String matchedModuleName = DataStoreValidationUtil.getMatchedModuleNameForPrefix(module, stepNodeName.getPrefix());
                        Optional<Module> matchedModule = schemaRegistry.getModule(matchedModuleName);
                        if(matchedModule.isPresent()) {
                            namespace = matchedModule.get().getNamespace().toString();
                        }
                    } else {
                        String namespaceFromPrefix = schemaRegistry.getNamespaceURI(stepNodeName.getPrefix());
                        if ( namespaceFromPrefix != null){
                            namespace = namespaceFromPrefix;
                        }
                    }
                }
                QName qname = schemaRegistry.lookupQName(namespace, nodeName);
                SchemaPath stepSchemaPath = schemaRegistry.getDescendantSchemaPath(newNode.getPath(), qname);
                if (stepSchemaPath != null) {
                    /**
                     * if this is the last step in the xpath, add the schemaPath to the list.
                     */
                    if (steps.size() > 1) {
                        newNode = schemaRegistry.getDataSchemaNode(stepSchemaPath);
                        steps.remove(step);
                        fetchSchemaPaths(schemaRegistry, steps, newNode, aggregatedPaths, manyPaths, isCaseOrChoice, augmentedSP, contextNode);
                        if(newNode instanceof  ListSchemaNode && predicates != null && predicates.length > 0) {
                            aggregatedPaths.add(stepSchemaPath);
                        }
                    } else {
                        aggregatedPaths.add(stepSchemaPath);
                    }
                } else if (newNode.getPath().getParent() != null && newNode.getPath().getParent().getLastComponent() != null) {
                    DataSchemaNode parentNode = schemaRegistry.getDataSchemaNode(newNode.getPath().getParent());
                    if(parentNode != null) {
                        Expression ex = new LocationPath(false, steps.toArray(new Step[steps.size()]));
                        getSchemaPathsFromXPath(schemaRegistry, parentNode, new RevisionAwareXPathImpl(ex.toString(), false), aggregatedPaths, manyPaths, isCaseOrChoice, augmentedSP);
                    }
                } else {
                    LOGGER.debug("schema path was null for qname {} and step{}", qname, newNode.getPath());
                }
            }
        }

    }



    private static void fetchAccessPath(SchemaRegistry schemaRegistry, String xPath, SchemaNode inSchemaNode, Map<SchemaPath, ArrayList<String>> manyPaths, boolean isCaseOrChoice, SchemaPath augmentedSP, SchemaNode contextNode) {

        Expression ex = JXPathUtils.getExpression(xPath);
        if (ex instanceof CoreOperation) {
            for (Expression exp : ((Operation) ex).getArguments()) {
                fetchAccessPath(schemaRegistry, exp.toString(), inSchemaNode, manyPaths, isCaseOrChoice, augmentedSP, contextNode);
            }
        } else if (ex instanceof LocationPath && ((LocationPath)ex).getSteps()[0].getAxis() == Compiler.AXIS_SELF){
            if(((LocationPath)ex).getSteps().length == 1) {
                manyPaths.put(inSchemaNode.getPath(), null);
            } else {
                fetchAccessPath(schemaRegistry, DataStoreValidationUtil.excludeFirstStep((LocationPath)ex).toString(), inSchemaNode, manyPaths, isCaseOrChoice, augmentedSP, contextNode);
            }
        } else if (ex instanceof LocationPath && !((LocationPath) ex).isAbsolute()) {
            Step[] steps = ((LocationPath) ex).getSteps();
            SchemaNode schemaNode = inSchemaNode;
            if(augmentedSP != null){
                /** In case of augmented when nodes, the when condition is defined for the augment and not the data node. Hence
                 * the schemaNode of the augment should be evaluated to get the location path.
                 * 
                 * 
                 * eg: augment '/if:interfaces/if:interface/bbf-subif:frame-processing/'
                    + 'bbf-subif:egress-rewrite' {

                    when 'derived-from-or-self(../../if:type,
                      "bbfift:vlan-sub-interface")'

                    leaf pop-tags{
                        type uint8 {
                        range "0..2";
                        }
                        default "0";                    
                    }

                 * In the above case, the when condition is for container egress-rewrite.*/
                schemaNode = schemaRegistry.getDataSchemaNode(augmentedSP);
            }
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
                path.add(DataStoreValidationUtil.getYangStep(schemaRegistry, schemaNode.getQName()));
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
                        schemaNode = schemaRegistry.getDataSchemaNode(schemaNode.getPath().getParent());
                    } else {
                        SchemaPath parentPath = schemaNode.getPath().getParent();
                        schemaNode = SchemaRegistryUtil.getEffectiveParentNode(schemaNode, schemaRegistry);
                        if ( schemaNode == null){
                            DataPath dataPath = DataPathUtil.buildDataPath(parentPath, schemaRegistry.getSchemaContext());
                            schemaNode = schemaRegistry.getActionDefinitionNode(dataPath);
                        }
                    }


                    if (schemaNode == null && rootNode && steps[i].getAxis() == Compiler.AXIS_PARENT
                            && steps[i + 1].getAxis() == Compiler.AXIS_CHILD) {
                        path.add(steps[i]);
                        if (i + 1 < steps.length) {
                            schemaNode = DataStoreValidationUtil.getRootSchemaNode(schemaRegistry, steps[++i]);
                            path.add(steps[i]);
                        }
                        continue;
                    }
                    if (!ChoiceCaseNodeUtil.isChoiceOrCaseNode(schemaNode)) {
                        // we are out of case/choice steps. Now start adding data path
                        isCaseOrChoice = false;
                    }
                    boolean isInputOrOutputLeaf = false;
                    if ( schemaNode instanceof InputEffectiveStatement || schemaNode instanceof OutputEffectiveStatement){
                        isInputOrOutputLeaf = true;
                    }
                    if (!(isCaseOrChoice || isInputOrOutputLeaf)) {
                        // if this is a choice/case node, dont add it to data path
                        path.add(DataStoreValidationUtil.getYangStep(schemaRegistry, schemaNode.getQName()));
                    }

                } else {
                    returnPath = getNextPath(schemaRegistry, schemaNode.getPath(), steps, i, path, false, manyPaths, isCaseOrChoice, augmentedSP);
                    break;
                }
            }
            Collections.reverse(path);
            if(augmentedSP != null){
                /** The path for an augmented node does not contain the actual node. As per documentation given above this means that
                 * the node(leaf pop-tags) for which the augmentation is done is not added in path. Hence we add it separately here.
                 *
                 * when we have inner leaf with default, the xpath is given for augment, then we have to iterate till
                 * top level node inside the augment to form the correct access path
                 */
                List<Step> path2 = new LinkedList<Step>();
                SchemaNode currentNode = inSchemaNode;
                DataSchemaNode augmentSchemaNode = schemaRegistry.getDataSchemaNode(augmentedSP);
                while (!(currentNode.getQName().equals(augmentSchemaNode.getQName()))) {
                    path2.add(DataStoreValidationUtil.getYangStep(schemaRegistry, currentNode.getQName()));
                    currentNode = schemaRegistry.getDataSchemaNode(currentNode.getPath().getParent());
                }
                Collections.reverse(path2);
                path.addAll(path2);
            }
            String sb1 = new LocationPath(false, path.toArray(new Step[0])).toString();

            if (returnPath != null) {
                ArrayList<String> accessPaths = manyPaths.get(returnPath);
                if( accessPaths == null){
                    accessPaths = new ArrayList<>();
                }
                accessPaths.add(sb1);
                manyPaths.put(returnPath, accessPaths);
            } else {
                List<SchemaPath> paths = new ArrayList<SchemaPath>();
                fetchSchemaPaths(schemaRegistry, new ArrayList<Step>(Arrays.asList(steps)), schemaNode, paths, manyPaths, isCaseOrChoice, augmentedSP, schemaNode);
                for (SchemaPath newPath : paths) {
                    ArrayList<String> accessPaths = manyPaths.get(newPath);
                    if(accessPaths == null){
                        manyPaths.put(newPath, null);
                    }
                }
            }
        } else if (ex instanceof LocationPath) {
            /**
             * we are here, if it is an absolute path. access path for the root is recorded
             * 
             * In legacy, 'device-manager' is the root node for all schemanodes, where as in schemamount mode root node should be different for each schemanode
             * 
             * consider below example for schema mount mode, there are two different root nodes.
             * 
             * eg:
             * 
             * container gsh {
             * 		list profile {
             * 			list handshake-profile{
             * 				key "name";
             * 				leaf name {type string;}
             * 			}
             * 		}
             * }
             * 
             * container interfaces {
             * 		list interface {
             * 			list line {
             * 				 container gsh{
             * 					leaf handshake-profile {
             * 						type leafref { 
             * 							path "/ghs:ghs/ghs:profiles/"ghs:handshake-profile/ghs:name";
             * 					 	}	
             * 					}
             * 				}
             * 			}
             * 		}
             * }	
             * 
             * xpath : /ghs:ghs/ghs:profiles/"ghs:handshake-profile/ghs:name
             * schemanode : handshake-profile [interfaces,interface,line,gsh,handshake-profile]
             * 
             */

            Step[] steps = ((LocationPath) ex).getSteps();
            DataSchemaNode schemaNode = schemaRegistry.getDataSchemaNode(inSchemaNode.getPath());
            List<Step> inputPath = new LinkedList<Step>(); 
            SchemaPath parentPath = DataStoreValidationUtil.buildAbsAccessPath(schemaRegistry, schemaNode, inputPath);  
            List<Step> accessPath = new LinkedList<Step>();
            // Check root node of xpath with root node of actual schemanode
            DataSchemaNode rootParentNode =  DataStoreValidationUtil.getRootSchemaNode(schemaRegistry,steps[0]);
            if(rootParentNode != null){
                SchemaPath rootParentPath = rootParentNode.getPath();
                if(!rootParentPath.getLastComponent().getLocalName().equals(parentPath.getLastComponent().getLocalName())){
                    parentPath = rootParentPath;
                }
            }
            // Here we should pass actual root node of steps.
            SchemaPath returnValue = getNextPath(schemaRegistry, parentPath, steps, 1, accessPath, true, manyPaths, isCaseOrChoice, augmentedSP);
            if (returnValue != null) {
                String sb1 = new LocationPath(true, inputPath.toArray(new Step[0])).toString();
                ArrayList<String> accessPaths = manyPaths.get(returnValue);
                if( accessPaths == null){
                    accessPaths = new ArrayList<>();
                }
                accessPaths.add(sb1);
                manyPaths.put(returnValue, accessPaths);
            }
        } else if (ex instanceof CoreFunction) {
            /**
             * here again, getting the relative path inside is difficult. So we record the access path from root
             */
            List<Step> inputPath = new LinkedList<Step>();
            DataStoreValidationUtil.buildAbsAccessPath(schemaRegistry, inSchemaNode, inputPath);
            String sb1 = new LocationPath(true, inputPath.toArray(new Step[0])).toString();
            /**
             * a generic count(list1), indicates it refers list1. All such
             * list1 instances in schematree are found and the path found above is store as 
             * access path for impact
             */
            fetchFunctionSchemaPaths(schemaRegistry, ex, inSchemaNode, manyPaths, sb1, isCaseOrChoice, augmentedSP);
        } else if (DataStoreValidationUtil.isExpressionPath(ex)) {
            ExpressionPath path = (ExpressionPath) ex;
            Expression[] predicates = path.getPredicates();
            Step[] steps = path.getSteps();
            handlePredicates(schemaRegistry, inSchemaNode, manyPaths, isCaseOrChoice, augmentedSP, predicates);
            if (steps != null  && steps.length > 0) {
                fetchAccessPath(schemaRegistry, new LocationPath(false, steps).toString(), inSchemaNode, manyPaths, isCaseOrChoice, augmentedSP, contextNode);
            } else {
                if (path.toString().startsWith(DataStoreValidationUtil.CURRENT_PATTERN) ) {
                    SchemaPath schemaPath = inSchemaNode.getPath();
                    List<Step> inputPath = new LinkedList<Step>();
                    DataStoreValidationUtil.buildAbsAccessPath(schemaRegistry, inSchemaNode, inputPath);
                    String ap = new LocationPath(true, inputPath.toArray(new Step[0])).toString();
                    ArrayList<String> accessPaths = manyPaths.get(schemaPath);
                    if( accessPaths == null){
                        accessPaths = new ArrayList<>();
                    }
                    accessPaths.add(ap);
                    manyPaths.put(schemaPath, accessPaths);
                }
            }
        } else if (DataStoreValidationUtil.isExtensionFunction(ex)) {
            if (((ExtensionFunction) ex).getArguments() != null) {
                for (Expression exp : ((ExtensionFunction) ex).getArguments()) {
                    fetchAccessPath(schemaRegistry, exp.toString(), inSchemaNode, manyPaths, isCaseOrChoice, augmentedSP, contextNode);
                }
            }
        }
    }

    private static void handlePredicates(SchemaRegistry schemaRegistry, SchemaNode inSchemaNode, Map<SchemaPath, ArrayList<String>> manyPaths, boolean isCaseOrChoice, SchemaPath augmentedSP,
            Expression[] predicates) {
        if (predicates != null) {
            for (Expression predicate:predicates) {
                fetchAccessPath(schemaRegistry, predicate.toString(), inSchemaNode, manyPaths, isCaseOrChoice, augmentedSP, inSchemaNode);
            }
        }
    }

    public static Map<SchemaPath, ArrayList<String>> getTargetSchemaNodePathFromXPath(SchemaRegistry schemaRegistry, DataSchemaNode schemaNode, String xPath) {
        Map<SchemaPath, ArrayList<String>> manyPaths = new HashMap<SchemaPath, ArrayList<String>>();
        try {
            fetchAccessPath(schemaRegistry, xPath, schemaNode, manyPaths, ChoiceCaseNodeUtil.isChoiceOrCaseNode(schemaNode), null, schemaNode);
            return manyPaths;
        } catch (Exception e) {
            LOGGER.error(null,String.format("Problem processing xpath '%s' on schemaNode '%s'", xPath, schemaNode.getPath()),e);
            throw new RuntimeException(String.format("Problem processing xpath '%s' on schemaNode '%s'", xPath, schemaNode.getPath()), e);
        }
    }

    public static boolean isTargetNodeConfiguration(SchemaRegistry schemaRegistry, DataSchemaNode schemaNode, String xPath) {
        /**
         * Return false if any of the target node is config false, otherwise return true
         */
        Map<SchemaPath, ArrayList<String>> targetnodeSchemapaths = AccessSchemaPathUtil.getTargetSchemaNodePathFromXPath(schemaRegistry, schemaNode, xPath);
        if(!targetnodeSchemapaths.isEmpty()){
            for (Map.Entry<SchemaPath, ArrayList<String>> path : targetnodeSchemapaths.entrySet()) {
                DataSchemaNode targetSchemaNode = schemaRegistry.getDataSchemaNode(path.getKey());
                // if any one of the targetnode is state configuration, then return false
                if(!targetSchemaNode.isConfiguration()){ 
                    return false;
                }
            }
        }
        return true;
    }

    public static Module getNodeConstraintAssociatedModuleInCache(){
        return (Module) RequestScope.getCurrentScope().getFromCache(NODE_CONSTRAINT_ASSOCIATED_MODULE_CACHE);
    }

    public static void setNodeConstraintAssociatedModuleInCache(Module nodeConstraintAssociatedModule){
        RequestScope.getCurrentScope().putInCache(NODE_CONSTRAINT_ASSOCIATED_MODULE_CACHE, nodeConstraintAssociatedModule);
    }

    public static void clearNodeConstraintAssociatedModuleInCache(){
        RequestScope.getCurrentScope().putInCache(NODE_CONSTRAINT_ASSOCIATED_MODULE_CACHE, null);
    }

    public static class XPathInfo {
        final DataSchemaNode m_referringNode;
        final String m_xPathString;
        boolean m_toBeSkipped = false;

        public XPathInfo(DataSchemaNode referringNode, String xPathString) {
            m_referringNode = referringNode;
            m_xPathString = xPathString;
        }

        public boolean isToBeSkipped() {
            return m_toBeSkipped;
        }

        public void setToBeSkipped(boolean toBeSkipped) {
            m_toBeSkipped = toBeSkipped;
        }

        @Override
        public String toString() {
            return "\nReferringNode =" + m_referringNode +
                    "\nXPath " + m_xPathString;
        }
    }
}
