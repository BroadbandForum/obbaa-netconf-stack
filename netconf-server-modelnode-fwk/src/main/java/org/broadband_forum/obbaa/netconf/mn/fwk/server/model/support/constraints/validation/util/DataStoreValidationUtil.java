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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.xml.XMLConstants;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.InfoSetUtil;
import org.apache.commons.jxpath.ri.compiler.Constant;
import org.apache.commons.jxpath.ri.compiler.CoreFunction;
import org.apache.commons.jxpath.ri.compiler.CoreOperation;
import org.apache.commons.jxpath.ri.compiler.CoreOperationAnd;
import org.apache.commons.jxpath.ri.compiler.CoreOperationEqual;
import org.apache.commons.jxpath.ri.compiler.CoreOperationOr;
import org.apache.commons.jxpath.ri.compiler.Expression;
import org.apache.commons.jxpath.ri.compiler.ExpressionPath;
import org.apache.commons.jxpath.ri.compiler.ExtensionFunction;
import org.apache.commons.jxpath.ri.compiler.LocationPath;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.NodeTest;
import org.apache.commons.jxpath.ri.compiler.Operation;
import org.apache.commons.jxpath.ri.compiler.Step;
import org.broadband_forum.obbaa.netconf.api.client.InternalNetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigDefaultOperations;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigOperations;
import org.broadband_forum.obbaa.netconf.api.server.NetconfQueryParams;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.ChoiceCaseNodeUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ConfigAttributeGetContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetConfigContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeKey;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeKeyBuilder;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.service.SchemaContextUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildContainerHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildListHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigAttributeFactory;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeDynaBean;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeDynaBeanContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeDynaBeanContext.ModelNodeDynaBeanContextBuilder;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeDynaBeanFactory;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeGetException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithIndex;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.DSValidationContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.DSValidationMountContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.ProxyValidationModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.ValidatedAugment;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.service.DSAugmentOrUsesValidation;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.EMNKeyUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.MNKeyUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.jxpath.JXPathUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.jxpath.YangStep;
import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.broadband_forum.obbaa.netconf.server.rpc.RequestType;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationTarget;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DerivableSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ElementCountConstraint;
import org.opendaylight.yangtools.yang.model.api.ElementCountConstraintAware;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.MustConstraintAware;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypedDataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.UsesNode;
import org.opendaylight.yangtools.yang.model.api.stmt.InputEffectiveStatement;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Houses all constants and utility methods used for yang validation
 */
public class DataStoreValidationUtil {

    public static final String STATE_VALIDATION_MODULE_NS = "STATE_VALIDATION_MODULE_NS";
    public static final String CLOSING_SQUARE_BRACE = "]";
    public static final String SINGLE_STEP_COUNT = "SINGLE_STEP_COUNT";
    public static final String MISSING_MANDATORY_NODE = "Missing mandatory node";
    public static final String RESULTED_IN_NULL = " resulted in null";
    public static final String NUMERIC = "^[+-]?\\d+(\\.\\d+)?$";
    public static final String SLASH = "/";
    public static final String PARENT_PATTERN = "..";
    public static final String CURRENT_FUNCTION = "current";
    public static final String CURRENT_PATTERN = "current()";
    public static final String CURRENT_SINGLE_KEY = "[current()]";
    public static final String CURRENT_PARENT_SINGLE_KEY = "[current()/..";
    public static final String CURRENT_PARENT = CURRENT_PATTERN + SLASH + PARENT_PATTERN;
    public static final String CURRENT_MULTI_PARENT = CURRENT_PATTERN + SLASH + PARENT_PATTERN +SLASH+ PARENT_PATTERN;
    public static final String COLON = ":";
    public static final String NC_ENABLE_POST_EDIT_DS_VALIDATION_SUPPORT = "NC_ENABLE_POST_EDIT_DS_VALIDATION_SUPPORT";
    private static final String ROOT_MODELNODE_AGGREGATOR_CACHE = "ROOT_MODELNODE_AGGREGATOR_CACHE";

    public static String getSimplePath(DataSchemaNode childSchemaNode) {
        StringBuilder sb = new StringBuilder();
        for (QName qName : childSchemaNode.getPath().getPathFromRoot()) {
            sb.append("/").append(qName.getLocalName());
        }
        ;
        return sb.toString();
    }

    public static Collection<Object> evaluateXpath(JXPathContext context, String xPath) {
        Collection<Object> returnValue = null;
        Iterator<?> leafRefs = context.iterate(xPath);
        while (leafRefs.hasNext()) {
            if (returnValue == null) {
                returnValue = new ArrayList<Object>();
            }
            returnValue.add(leafRefs.next());
        }
        return returnValue;
    }

    public static Map<QName, Map<QName, ConfigLeafAttribute>> getMatchCriteria(LocationPath xPathForValidation, ModelNode contextModelNode) {
        Map<QName, Map<QName, ConfigLeafAttribute>> matchCriteria = new HashMap<>();
        LocationPath locationPath = xPathForValidation;
        Step[] steps = locationPath.getSteps();
        boolean shouldFillMatchCriteria = false;
        for (Step step : steps) {
            if(step.getPredicates().length == 1) {
                shouldFillMatchCriteria =true;
                break;
            }
        }
        if(shouldFillMatchCriteria) {
            SchemaRegistry schemaRegistry = contextModelNode.getSchemaRegistry();
            if (schemaRegistry != null) {
                SchemaPath modelNodeSchemaPath = contextModelNode.getModelNodeSchemaPath();
                if (modelNodeSchemaPath != null) {
                    SchemaPath parentSchemaPath = modelNodeSchemaPath;
                    DataSchemaNode parentDataSchemaNode = schemaRegistry.getDataSchemaNode(parentSchemaPath);
                    for (Step step : steps) {
                        NodeTest nodeTest = step.getNodeTest();
                        if (nodeTest instanceof NodeNameTest && parentSchemaPath!= null) {
                            String stepName = getLocalName(step);
                            String moduleName = getPrefix(step);
                            SchemaPath pathForStep = getChildPathModuleNameAware(schemaRegistry, parentSchemaPath,
                                    stepName, moduleName);
                            if (step.getPredicates().length == 1) {
                                Expression predicate = step.getPredicates()[0];
                                if(predicate instanceof CoreOperationEqual) {
                                    Expression[] arguements = ((CoreOperationEqual) predicate).getArguments();
                                    Expression lhs = arguements[0];
                                    Expression rhs = arguements[1];
                                    if (rhs instanceof Constant && lhs instanceof LocationPath) {
                                        LocationPath lhsLocationPath = (LocationPath) lhs;
                                        if (!lhsLocationPath.isAbsolute() && lhsLocationPath.getSteps().length == 1 && lhsLocationPath.getSteps()[0].getAxis() != Compiler.AXIS_SELF) {
                                            DataSchemaNode childSchemaNode = getChildDataSchemaNodeModuleNameAware(schemaRegistry, parentDataSchemaNode.getPath(), stepName, moduleName);
                                            if (childSchemaNode instanceof ListSchemaNode) {
                                                Step lhsStep = lhsLocationPath.getSteps()[0];
                                                String lhsLocalName = getLocalName(lhsStep);
                                                String lhsModuleName = getPrefix(lhsStep);
                                                DataSchemaNode predicateSchemaNode = getChildDataSchemaNodeModuleNameAware(schemaRegistry, childSchemaNode.getPath(), lhsLocalName, lhsModuleName);
                                                if (predicateSchemaNode instanceof LeafSchemaNode) {
                                                    Object rhsObject = rhs.compute(null);
                                                    String rhsValueToCompare;
                                                    if (rhsObject instanceof Number) {
                                                        rhsValueToCompare = InfoSetUtil.stringValue(rhsObject);
                                                    } else {
                                                        rhsValueToCompare = rhsObject.toString();
                                                    }
                                                    Map<QName, ConfigLeafAttribute> matchCriteriaPart = new HashMap<>();
                                                    matchCriteriaPart.put(predicateSchemaNode.getQName(),
                                                            ConfigAttributeFactory.getConfigLeafAttribute(schemaRegistry, (LeafSchemaNode) predicateSchemaNode, rhsValueToCompare));
                                                    matchCriteria.put(childSchemaNode.getQName(), matchCriteriaPart);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            parentSchemaPath = pathForStep;
                        }
                    }
                }
            }
        }
        return matchCriteria;
    }

    public static String getModuleNameFromPrefix(SchemaRegistry schemaRegistry, String prefix,
                                          AugmentationSchemaNode augmentationSchemaNodeForWhen) {
        Collection<DataSchemaNode> childNodes = augmentationSchemaNodeForWhen.getChildNodes();
        for (DataSchemaNode childNode : childNodes) {
            return getMatchedModuleNameForPrefix(schemaRegistry
                    .getModuleByNamespace(childNode.getQName().getNamespace().toString()), prefix);
        }
        return null;
    }

    public enum ExpressionTranformationType {
        FROM_PREFIX_TO_MODULENAME, FROM_MODULENAME_TO_MODULENAME_LOCALNAME_WITH_SEPARATOR
    }

    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(DataStoreValidationUtil.class, LogAppNames.NETCONF_STACK);

    /**
     * Introduced this flag for UT purpose. ComponentID validation should be skipped for Deploy Plug UT (By default this flag is enabled).
     */
    private static boolean COMPONENTID_VALIDATION = true;
    
    public static void skipComponentIDValidation() {
    	COMPONENTID_VALIDATION = false;
    }

    public static void enbleComponentIDValidation() {
    	COMPONENTID_VALIDATION = true;
    }

    public static boolean getComponentIDValidation(){
    	return COMPONENTID_VALIDATION;
    }

    /**
     * Returns true if 
     *   1) given ContainerSchemaNode is a non-presence container
     *   2a) has at least one leaf with default values or 
     *   2b) any of its child non-presence container node has at least one leaf with default values 
     */
    public static boolean containerHasDefaultLeafs(SchemaRegistry schemaRegistry, ContainerSchemaNode node) {
        if (node.isPresenceContainer()) {
            // this is a presence container. has to be created by a edit-config
            return false;
        }
        Collection<DataSchemaNode> children = schemaRegistry.getChildren(node.getPath());
        for (DataSchemaNode child:children) {
            if (SchemaRegistryUtil.hasDefaults(child)) {
                return true;
            }  else if (child instanceof ContainerSchemaNode) {
                boolean mustWhen = DataStoreValidationUtil.containsMustWhen(schemaRegistry, child);
                if (!mustWhen && containerHasDefaultLeafs(schemaRegistry, (ContainerSchemaNode) child)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean needsFurtherValidation(Element element, RequestType requestType) {
        String operationAttribute = element.getAttributeNS(NetconfResources.NETCONF_RPC_NS_1_0, NetconfResources.OPERATION);
        if (!requestType.isRpc() && !requestType.isAction() && (operationAttribute == null || operationAttribute.isEmpty()
                || (!operationAttribute.equals(EditConfigOperations.CREATE) && !operationAttribute.equals(EditConfigOperations.REPLACE)))) {
            return false;

        }

        return true;
    }
    
    /**
     * Skip Mandatory Constraint validation if it is outside Action nodes(above action nodes)
     */
   public static boolean skipMandatoryConstaintValidation(SchemaPath schemaPath, SchemaRegistry schemaRegistry, RequestType requestType) {
       if (requestType.isAction() && !isSchemaNodeUnderActionInputNode(schemaPath, schemaRegistry)) {
           return true;
       }
       return false;
   }
 
   private static boolean isSchemaNodeUnderActionInputNode(SchemaPath schemaPath, SchemaRegistry schemaRegistry) {
       SchemaPath parentSchemaPath = schemaPath;
       while (parentSchemaPath != null) {
           SchemaNode schemaNode = schemaRegistry.getDataSchemaNode(parentSchemaPath);
           if (schemaNode instanceof InputEffectiveStatement) {
               return true;
           }
           parentSchemaPath = parentSchemaPath.getParent();
       }
       return false;
   }

    /**
     * Given a DataSchemaNode, collects each data step name in the path towards root in a list. Returns the schemaPath of the 
     * input schemaNode
     */
    public static SchemaPath buildAbsAccessPath(SchemaRegistry schemaRegistry, SchemaNode schemaNode, List<Step> inputPath) {
    	SchemaPath inPath = schemaNode.getPath();
    	SchemaPath returnValue = inPath;
    	do {
    		DataSchemaNode node = schemaRegistry.getDataSchemaNode(inPath);
    		if (ChoiceCaseNodeUtil.isChoiceOrCaseNode(node)) {
    			node = SchemaRegistryUtil.getEffectiveParentNode(node, schemaRegistry);
    			inPath = node.getPath();
    		}
    		inputPath.add(getYangStep(inPath.getLastComponent()));
    		inPath = inPath.getParent();
    		returnValue = inPath.getParent() != null ? inPath : returnValue;
    	} while (inPath.getLastComponent() != null);
    	Collections.reverse(inputPath);
    	return returnValue;
    }

    /**
     * given a LocationPath->Step, retrieves the localName
     * @param step
     * @return
     */
    public static String getLocalName(Step step) {
        if (step.getNodeTest() instanceof NodeNameTest) {
            return ((NodeNameTest) step.getNodeTest()).getNodeName().getName();
        }
        return null;
    }

    /**
     * given a qualifiedName [prefix:localName] ->qualifiedName, retrieves the localName
     * @param qualifiedName
     * @return
     */
    public static String getLocalName(String qualifiedName) {
        if (qualifiedName != null) {
            if(qualifiedName.contains(COLON)) {
                return qualifiedName.split(COLON)[1];
            } else {
                return qualifiedName;
            }
        }
        return null;
    }

    /**
     * given a qualifiedName [prefix:localName] ->qualifiedName, retrieves the prefix
     * @param qualifiedName
     * @return
     */
    public static String getPrefix(String qualifiedName) {
        if (qualifiedName != null) {
            if(qualifiedName.contains(COLON)) {
                return qualifiedName.split(COLON)[0];
            }
        }
        return null;
    }

    /**
     * given a LocationPath->Step, retrieves the prefix
     * @param step
     * @return
     */
    public static String getPrefix(Step step) {
        if (step.getNodeTest() instanceof NodeNameTest) {
            return ((NodeNameTest) step.getNodeTest()).getNodeName().getPrefix();
        }
        return null;
    }

    /**
     *  Given a parent schemaPath, a child local name and child prefix, returns the first instance of child matching the prefix and local name
     */
    public static SchemaPath getChildPathPrefixAware(SchemaRegistry schemaRegistry, SchemaPath parentPath, String localName, String prefix) {
        SchemaPath returnValue = null;
        Collection<DataSchemaNode> childNodes = schemaRegistry.getChildren(parentPath);
        for (DataSchemaNode childNode : childNodes) {
            if(childNode instanceof ChoiceSchemaNode){
                Collection<DataSchemaNode> children = ChoiceCaseNodeUtil.getChildrenUnderChoice(schemaRegistry, parentPath);
                for(DataSchemaNode dataSchemaNode : children){
                    if (dataSchemaNode.getQName().getLocalName().equals(localName)
                            && schemaRegistry.getPrefix(dataSchemaNode.getQName().getNamespace().toString()).equals(prefix)) {

                        returnValue = dataSchemaNode.getPath();
                        break;
                    }
                }
            }
            if (childNode.getQName().getLocalName().equals(localName)
                    && schemaRegistry.getPrefix(childNode.getQName().getNamespace().toString()).equals(prefix)) {
                returnValue = childNode.getPath();
                break;
            }
        }
        return returnValue;
    }

    public static boolean isValidChild(SchemaRegistry schemaRegistry, SchemaPath parentPath, SchemaPath childPath) {
        Collection<DataSchemaNode> childNodes = schemaRegistry.getChildren(parentPath);
        for (DataSchemaNode childNode : childNodes) {
            if(childNode instanceof ChoiceSchemaNode){
                Collection<DataSchemaNode> children = ChoiceCaseNodeUtil.getChildrenUnderChoice(schemaRegistry, parentPath);
                for(DataSchemaNode dataSchemaNode : children){
                    if(dataSchemaNode.getPath().equals(childPath)) {
                        return true;
                    }
                }
            } else if(childNode.getPath().equals(childPath)) {
                return true;
            }
        }
        return false;
    }

    /**
     *  Given a parent schemaPath, a child local name and child module name, returns the first instance of child matching the module and local name
     */
    public static SchemaPath getChildPathModuleNameAware(SchemaRegistry schemaRegistry, SchemaPath parentPath, String localName, String moduleName) {
        SchemaPath returnValue = null;
        Collection<DataSchemaNode> childNodes = schemaRegistry.getChildren(parentPath);
        for (DataSchemaNode childNode : childNodes) {
            if(childNode instanceof ChoiceSchemaNode){
                Collection<DataSchemaNode> children = ChoiceCaseNodeUtil.getChildrenUnderChoice(schemaRegistry, parentPath);
                for(DataSchemaNode dataSchemaNode : children){
                    if (dataSchemaNode.getQName().getLocalName().equals(localName)
                            && schemaRegistry.getModuleNameByNamespace(dataSchemaNode.getQName().getNamespace().toString()).equals(moduleName)) {

                        returnValue = dataSchemaNode.getPath();
                        break;
                    }
                }
            }
            if (childNode.getQName().getLocalName().equals(localName)
                    && schemaRegistry.getModuleNameByNamespace(childNode.getQName().getNamespace().toString()).equals(moduleName)) {
                returnValue = childNode.getPath();
                break;
            }
        }
        return returnValue;
    }

    /**
     *  Given a parent schemaPath, a child local name and child module name, returns the first instance of child matching the module and local name
     */
    public static DataSchemaNode getChildDataSchemaNodeModuleNameAware(SchemaRegistry schemaRegistry, SchemaPath parentPath, String localName, String moduleName) {
        DataSchemaNode returnValue = null;
        Collection<DataSchemaNode> childNodes = schemaRegistry.getChildren(parentPath);
        for (DataSchemaNode childNode : childNodes) {
            if(childNode instanceof ChoiceSchemaNode){
                Collection<DataSchemaNode> children = ChoiceCaseNodeUtil.getChildrenUnderChoice(schemaRegistry, parentPath);
                for(DataSchemaNode dataSchemaNode : children){
                    if (dataSchemaNode.getQName().getLocalName().equals(localName)
                            && schemaRegistry.getModuleNameByNamespace(dataSchemaNode.getQName().getNamespace().toString()).equals(moduleName)) {

                        returnValue = dataSchemaNode;
                        break;
                    }
                }
            }
            if (childNode.getQName().getLocalName().equals(localName)
                    && schemaRegistry.getModuleNameByNamespace(childNode.getQName().getNamespace().toString()).equals(moduleName)) {
                returnValue = childNode;
                break;
            }
        }
        return returnValue;
    }

    /**
     *  Given a parent schemaPath and a child local name, returns the first instance of child matching the local name 
     */
    public static SchemaPath getChildPath(SchemaRegistry schemaRegistry, SchemaPath parentPath, String localName) {
        SchemaPath returnValue = null;
        Collection<DataSchemaNode> childNodes = schemaRegistry.getChildren(parentPath);
        for (DataSchemaNode childNode : childNodes) {
            if(childNode instanceof ChoiceSchemaNode){
                Collection<DataSchemaNode> children = ChoiceCaseNodeUtil.getChildrenUnderChoice(schemaRegistry, parentPath);
                for(DataSchemaNode dataSchemaNode : children){
                    if (dataSchemaNode.getQName().getLocalName().equals(localName)) {
                        returnValue = dataSchemaNode.getPath();
                        break;
                    }
                }
            }
            if (childNode.getQName().getLocalName().equals(localName)) {
                returnValue = childNode.getPath();
                break;
            }
        }
        return returnValue;
    }

    public static boolean isPostEditValidationSupported() {
        boolean returnValue = true;
        /* The property is for UT purpose only. Not to be documented */
        if (System.getProperties().containsKey(NC_ENABLE_POST_EDIT_DS_VALIDATION_SUPPORT)) {
            returnValue = Boolean.parseBoolean(System.getProperty(NC_ENABLE_POST_EDIT_DS_VALIDATION_SUPPORT));
        } else if (System.getenv().containsKey(NC_ENABLE_POST_EDIT_DS_VALIDATION_SUPPORT)) {
            returnValue = Boolean.parseBoolean(System.getenv(NC_ENABLE_POST_EDIT_DS_VALIDATION_SUPPORT));
        }
        // Returning true by default. Will be kept in observation
        // for a few days before removing.
        // PHASE 3 Yang validation is enabled
        LOGGER.debug("Post edit-config validation is {}", returnValue);
        return returnValue;
    }

    /**
     * Evaluates to true, if for a dynaBean has the given property name
     */
    @SuppressWarnings("unchecked")
    public static boolean isReadable(DynaBean dynaBean, String localName) {
        if (localName != null) {
            if(localName.contains(DataStoreValidationUtil.COLON)) {
                localName = DataStoreValidationUtil.getLocalName(localName);
            }
            if (dynaBean instanceof ModelNodeDynaBean) {
                return ((ModelNodeDynaBean)dynaBean).isReadable(localName);
            } else if (dynaBean != null){
                Set<String> properties = (Set<String>) dynaBean.get(ModelNodeDynaBeanFactory.ATTRIBUTE_LIST);
                return properties.contains(localName);
            }
        }
        
        return false;
    }

    public static boolean isReadable(DynaBean dynaBean, SchemaPath childSchemaPath) {
        if (childSchemaPath != null) {
            String localName = childSchemaPath.getLastComponent().getLocalName();
            if (dynaBean instanceof ModelNodeDynaBean) {
                if(((ModelNodeDynaBean)dynaBean).isReadable(localName)) {
                    ModelNode contextModelNode = ModelNodeDynaBean.getContextModelNode(dynaBean);
                    DataSchemaNode dataSchemaNode = contextModelNode.getSchemaRegistry().getDataSchemaNode(contextModelNode.getModelNodeSchemaPath());
                    return isValidChild(contextModelNode.getSchemaRegistry(), dataSchemaNode.getPath(), childSchemaPath);
                }
            } else if (dynaBean != null){
                Set<String> properties = (Set<String>) dynaBean.get(ModelNodeDynaBeanFactory.ATTRIBUTE_LIST);
                return properties.contains(localName);
            }
        }

        return false;
    }


    /**
     * Modifies the Expression to work on a JxPath/DynaBean evaluation by replacing certain non-compliance step names with
     * modified values in the given context  
     */
    public static Expression getDynaBeanAlignedPath(Expression expression) {
        if (expression instanceof LocationPath) {
            return getDynaBeanAlignedPath(((LocationPath) expression));
        } else if (expression instanceof Operation) {
            Expression[] innerExpressions = ((Operation) expression).getArguments();
            Expression[] expressions = new Expression[innerExpressions != null ? innerExpressions.length : 0];
            int index = 0;
            if (((Operation) expression).getArguments() != null) {
                for (Expression exp : ((Operation) expression).getArguments()) {
                    expressions[index++]= getDynaBeanAlignedPath(exp);
                }
            }

            if (expression instanceof CoreOperation) {
                return JXPathUtils.getCoreOperation((CoreOperation) expression, expressions);
            } else if (expression instanceof CoreFunction) {
                return JXPathUtils.getCoreFunction(((CoreFunction) expression).getFunctionCode(), expressions);
            } else if (expression instanceof ExtensionFunction) {
                ExtensionFunction oldFunction = (ExtensionFunction) expression;
                return new ExtensionFunction(oldFunction.getFunctionName(), expressions);
            } else {
                LOGGER.warn("A new type of operation is identified - {} for {}", expression.getClass(), expression.toString());
            }
        }

        return expression;
    }

    public static YangStep getYangStep(SchemaRegistry schemaRegistry, QName yangQName) {
        org.apache.commons.jxpath.ri.QName qname =
                new org.apache.commons.jxpath.ri.QName(schemaRegistry.getPrefix(yangQName.getNamespace().toString()),
                        yangQName.getLocalName());
        return new YangStep(qname, yangQName.getNamespace().toString());
    }

    public static YangStep getYangStep(QName yangQName) {
        org.apache.commons.jxpath.ri.QName qname = new org.apache.commons.jxpath.ri.QName(null, yangQName.getLocalName());
        return new YangStep(qname, yangQName.getNamespace().toString());
    }

    public static LocationPath getDynaBeanAlignedPath(LocationPath locationPath) {

        LocationPath newPath = locationPath;
        boolean stepModified = false;
        if (newPath.toString().contains(ModelNodeDynaBeanFactory.ATTRIBUTE_CLASS)) {
            Step[] oldSteps = newPath.getSteps();
            Step[] newSteps = new Step[oldSteps.length];
            for (int i=0;i<newSteps.length;i++) {
                Step step = oldSteps[i];
                if (step.toString().contains(ModelNodeDynaBeanFactory.ATTRIBUTE_CLASS) && step.getNodeTest() instanceof NodeNameTest) {
                    boolean somethingChanged = false;
                    NodeNameTest node = (NodeNameTest) step.getNodeTest();
                    String prefix = node.getNodeName().getPrefix();
                    String localName = node.getNodeName().getName();
                    List<Expression> newExpression = new LinkedList<Expression>();
                    String newName = localName;
                    List<Expression> oldExpression = new LinkedList<Expression>(Arrays.asList(step.getPredicates()));
                    if (localName.contains(ModelNodeDynaBeanFactory.ATTRIBUTE_CLASS)) {
                        newName = ModelNodeDynaBeanFactory.getDynaBeanAttributeName(localName);
                        if (!localName.equals(newName)) {
                            somethingChanged = true;
                        }
                    }

                    for (Expression expression : oldExpression) {
                        if (expression.toString().contains(ModelNodeDynaBeanFactory.ATTRIBUTE_CLASS)) {
                            newExpression.add(getDynaBeanAlignedPath(expression));
                        } else {
                            newExpression.add(expression);
                        }
                    }

                    if (!oldExpression.containsAll(newExpression)) {
                        oldExpression = newExpression;
                        somethingChanged = true;
                    }
                    if (somethingChanged) {
                        org.apache.commons.jxpath.ri.QName qname = new org.apache.commons.jxpath.ri.QName(prefix, newName);
                        Step newStep = new YangStep(qname, node.getNamespaceURI(),
                                oldExpression.toArray(new Expression[oldExpression.size()]));
                        newSteps[i] = newStep;
                        stepModified = true;
                    } else {
                        newSteps[i] = step;
                    }
                } else {
                    newSteps[i] = step;
                }
            }
            if (stepModified) {
                newPath = new LocationPath(locationPath.isAbsolute(), newSteps);
            }
        }

        return newPath;
    }

    public static LocationPath excludeFirstStep(LocationPath locationPath) {
        /**
         * Given a xpath the first step is removed and the resulted xpath is returned.
         * eg: input: ../a/b/c   output: a/b/c
         *     input: device-holder/device output: device
         */
        Step[] newSteps = new Step[locationPath.getSteps().length -1 ];
        System.arraycopy(locationPath.getSteps(), 1, newSteps, 0, newSteps.length);
        return new LocationPath(locationPath.isAbsolute(), newSteps);
    }

    public static SchemaPath getXPathSchemaPath(SchemaRegistry schemaRegistry, SchemaPath currentPath, LocationPath xPath) {
        Step[] steps = xPath.getSteps();
        SchemaPath nextPath = currentPath;
        if (steps[0].getAxis() == Compiler.AXIS_PARENT && nextPath.getParent() != null && nextPath.getParent().getLastComponent() != null) {
            return getXPathSchemaPath(schemaRegistry, SchemaRegistryUtil.getDataParentSchemaPath(schemaRegistry, nextPath), excludeFirstStep(xPath));
        } else {
            for (Step step:steps) {
                String localName = getLocalName(step);
                nextPath = getChildPath(schemaRegistry, nextPath, ModelNodeDynaBeanFactory.getModelNodeAttributeName(localName));
                if (nextPath == null) {
                    break;
                }
            }
            if (nextPath == null) {
                QName rootName = currentPath.getPathFromRoot().iterator().next();
                if (rootName.getLocalName().equals(getLocalName(steps[0]))) {
                    nextPath = currentPath;
                    while (nextPath.getParent().getLastComponent() != null){
                        nextPath = nextPath.getParent();
                    }
                    return getXPathSchemaPath(schemaRegistry, nextPath, excludeFirstStep(xPath));
				} else {
					// If root step of xPath(step[0]) is not in under currentPath, then it might be in different root tree 
					for (SchemaPath rootSchemaPath : schemaRegistry.getRootSchemaPaths()) {
						if (rootSchemaPath.getLastComponent().getLocalName().equals(getLocalName(steps[0]))) {
							nextPath = rootSchemaPath;
							break;
						}
					}
					if(nextPath != null) {
						return getXPathSchemaPath(schemaRegistry, nextPath, excludeFirstStep(xPath));
					}
				}
            } 
        }
        return nextPath;
    }
    
    public static Collection<ModelNode> getChildListModelNodes(ModelNode parentNode, ListSchemaNode childSchemaNode) throws ModelNodeGetException {
        return getChildListModelNodes(parentNode, childSchemaNode, Collections.emptyMap());
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static Collection<ModelNode> getChildListModelNodes(ModelNode parentNode, ListSchemaNode childSchemaNode,
            Map<QName,ConfigLeafAttribute> matchCriteria) throws ModelNodeGetException {
        Collection<ModelNode> listNodes = new ArrayList<>();
        QName childQName = childSchemaNode.getQName();
        ModelNodeId parentModelNodeId = parentNode.getModelNodeId();
        if(parentNode instanceof ModelNodeWithIndex){
            if(matchCriteria.keySet().containsAll(childSchemaNode.getKeyDefinition())){
                return retrieveChildNodeFromIndex((ModelNodeWithIndex) parentNode, childSchemaNode, matchCriteria, listNodes, parentModelNodeId);
            }
        }

        String dynaAttributeName = ModelNodeDynaBeanFactory.getDynaBeanAttributeName(childQName.getLocalName());

        boolean proxyModelNode = parentNode instanceof ProxyValidationModelNode;

        if (ModelNodeDynaBeanFactory.containsBeanForModelNode(parentModelNodeId, parentNode.getSchemaRegistry())) {
            // If the modelNodeId is in cache, dynaBean is already created
            Collection<DynaBean> beans = null;
            DynaBean dynaBean = (DynaBean) parentNode.getValue();
            ModelNodeDynaBeanContext dynaBeanContext;
            if(parentNode.isChildBigList(childSchemaNode)){
                dynaBeanContext = DataStoreValidationUtil
                        .getModelNodeDynaBeanContext(parentNode.getSchemaRegistry(), dynaAttributeName,
                                childQName.getModule().getNamespace().toString(),
                                matchCriteria);
                beans = ModelNodeDynaBean.withContext(dynaBeanContext, () -> dynaBean == null ? null : (Collection) dynaBean.get(dynaAttributeName));
            }else {
                dynaBeanContext = DataStoreValidationUtil
                        .getModelNodeDynaBeanContext(parentNode.getSchemaRegistry(), dynaAttributeName,
                                childQName.getModule().getNamespace().toString(),
                                null);
                beans = ModelNodeDynaBean.withContext(dynaBeanContext, () -> dynaBean == null ? null : (Collection) dynaBean.get(dynaAttributeName));
            }

            if (beans != null) {
                
                for (DynaBean bean:beans) {
                    if (bean instanceof ModelNodeDynaBean) {
                        ModelNodeWithAttributes listNode = (ModelNodeWithAttributes) bean.get(ModelNodeWithAttributes.MODEL_NODE);
                        boolean matchFound = true;
                        if(matchCriteria.isEmpty()){
                            listNodes.add(listNode);
                        } else {
                            for (Map.Entry<QName, ConfigLeafAttribute> key : matchCriteria.entrySet()) {
                                if (!key.getValue().equals(listNode.getAttribute(key.getKey()))) {
                                    matchFound = false;
                                    break;
                                }
                            }
                            if (matchFound) {
                                listNodes.add(listNode);
                                break;
                            }
                        }
                    }
                }
            }
        } else if (!proxyModelNode) {
            // if dynaBean is not created and this is a not a proxy Node
            ChildListHelper helper = parentNode.getMountModelNodeHelperRegistry().getChildListHelper(parentNode.getModelNodeSchemaPath(), childQName);
            if (helper != null) {
                listNodes = helper.getValue(parentNode, matchCriteria);
            }
        }
        if(listNodes == null){
            return Collections.emptyList();
        }
        return listNodes;
    }

    private static Collection<ModelNode> retrieveChildNodeFromIndex(ModelNodeWithIndex parentNode, ListSchemaNode childSchemaNode, Map<QName, ConfigLeafAttribute> matchCriteria, Collection<ModelNode> listNodes, ModelNodeId parentModelNodeId) {
        ModelNodeKey mnKey = MNKeyUtil.getKeyFromCriteria(childSchemaNode, matchCriteria);
        ModelNodeId childId = EMNKeyUtil.getModelNodeId(mnKey, parentModelNodeId, childSchemaNode.getPath());
        childId.removeFirst(parentNode.getSchemaRootId().getRdnsReadOnly().size());
        ModelNode childMn = (ModelNode) parentNode.getIndexedValue(childId.xPathString(((ModelNode)parentNode).getSchemaRegistry(), true, true));
        if(childMn != null) {
            listNodes.add(childMn);
        }
        return listNodes;
    }

    @SuppressWarnings("unchecked")
	public static ModelNode getChildContainerModelNode(ModelNode parentNode, DataSchemaNode childSchemaNode) throws ModelNodeGetException {
        ModelNode childContainer = null;
        QName childQName = childSchemaNode.getQName();
        String dynaAttributeName = ModelNodeDynaBeanFactory.getDynaBeanAttributeName(childQName.getLocalName());
        boolean proxyModelNode = parentNode instanceof ProxyValidationModelNode;
        ModelNodeId parentModelNodeId = parentNode.getModelNodeId();
        if(parentNode instanceof ModelNodeWithIndex && parentNode.getSchemaRegistry().getParentRegistry() != null ){
            ModelNodeId childId = EMNKeyUtil.getModelNodeId(new ModelNodeKeyBuilder().build(), parentModelNodeId, childSchemaNode.getPath());
            childId.removeFirst(((ModelNodeWithIndex)parentNode).getSchemaRootId().getRdnsReadOnly().size());
            ((ModelNodeWithIndex)parentNode).getChildren();
            ModelNode childMn = (ModelNode) ((ModelNodeWithIndex)parentNode).getIndexedValue(childId.xPathString(parentNode.getSchemaRegistry(), true, true));
            return childMn;
        }

        if (ModelNodeDynaBeanFactory.containsBeanForModelNode(parentModelNodeId, parentNode.getSchemaRegistry())) {
            ModelNodeDynaBean dynaBean = (ModelNodeDynaBean) parentNode.getValue();
			if (DataStoreValidationUtil.isReadable(dynaBean, dynaAttributeName)) {
			    SchemaRegistry schemaRegistry = parentNode.getSchemaRegistry();
                ModelNodeDynaBeanContext dynaBeanContext = DataStoreValidationUtil
                        .getModelNodeDynaBeanContext(schemaRegistry, dynaAttributeName, childQName.getModule().getNamespace().toString(),
                                null);
				Object value = ModelNodeDynaBean.withContext(dynaBeanContext, () -> dynaBean.get(dynaAttributeName));
				if (value instanceof List) {
					List<Object> objects = (List<Object>) value;
					if (objects != null) {
						for (Object innerObject : objects) {
							if (innerObject instanceof DynaBean) {
								// for a root node which can have an attribute as the
								// same name as the node, we will have a non
								// bean object.
								DynaBean childBean = (DynaBean) innerObject;
								childContainer = (ModelNode) childBean.get(ModelNodeWithAttributes.MODEL_NODE);
								if (childContainer != null
										&& childQName.getNamespace().equals(childContainer.getQName().getNamespace())) {
									break;
								}
							}
						}
					}
				}
			}
        } else if (!proxyModelNode) {
            ChildContainerHelper helper = parentNode.getMountModelNodeHelperRegistry().getChildContainerHelper(parentNode.getModelNodeSchemaPath(), childQName);
            if (helper != null) {
                childContainer = (ModelNodeWithAttributes) helper.getValue(parentNode);
            }
        }

        return childContainer;
    }
    
    public static boolean isConstant(Object expression) {
        return expression instanceof Constant;
    }
    
    public static boolean isLocationPath(Expression expression) {
        return expression instanceof LocationPath;
    }
    public static boolean isCoreOperationOr(Expression expression) {
        return expression instanceof CoreOperationOr;
    }

    public static boolean isCoreOperationAnd(Expression expression) {
        return expression instanceof CoreOperationAnd;
    }
    
    public static boolean isCoreOperation(Expression expression) {
        return expression instanceof CoreOperation;
    }
    
    public static boolean isFunction(Expression expression) {
        return expression instanceof CoreFunction;
    }
    
    public static boolean isExpression(Object expression) {
    	return expression instanceof Expression;
    }
    public static boolean isExpressionPath(Expression expression) {
        return expression instanceof ExpressionPath;
    }
    
    public static boolean isExtensionFunction(Expression expression) {
        return expression instanceof ExtensionFunction;
    }
    
    public static boolean isOperation(Expression expression) {
        return expression instanceof Operation;
    }

    public static Pair<AugmentationSchemaNode,SchemaPath> getAugmentationSchema(SchemaRegistry schemaRegistry, DataSchemaNode parentSchemaNode, DataSchemaNode child) {
    	if (parentSchemaNode != null && parentSchemaNode instanceof AugmentationTarget) {
    		Set<AugmentationSchemaNode> augs = ((AugmentationTarget) parentSchemaNode).getAvailableAugmentations();
    		if(ChoiceCaseNodeUtil.isChoiceOrCaseNode(parentSchemaNode)) {
                AugmentationSchemaNode matchedAug = getMatchedAugmentationSchema(augs, child);
                if (matchedAug == null) {
                    DataSchemaNode parentNode = schemaRegistry.getDataSchemaNode(parentSchemaNode.getPath().getParent());
                    return getAugmentationSchema(schemaRegistry, parentNode, parentSchemaNode);
                }
                parentSchemaNode = schemaRegistry.getNonChoiceParent(parentSchemaNode.getPath());
                return new Pair<AugmentationSchemaNode, SchemaPath>(matchedAug, parentSchemaNode.getPath());
            }else {
                AugmentationSchemaNode matchedAug = getMatchedAugmentationSchema(augs, child);
                if (matchedAug == null) {
                    DataSchemaNode parentNode = schemaRegistry.getNonChoiceParent(parentSchemaNode.getPath());
                    return getAugmentationSchema(schemaRegistry, parentNode, parentSchemaNode);
                }
                return new Pair<AugmentationSchemaNode, SchemaPath>(matchedAug, parentSchemaNode.getPath());
            }
    	}
    	return null;
    }
    
    private static AugmentationSchemaNode getMatchedAugmentationSchema(Set<AugmentationSchemaNode> augs, DataSchemaNode child){
		for (AugmentationSchemaNode aug : augs) {
		    for (DataSchemaNode augChild : aug.getChildNodes()) {
                if (augChild.getPath().equals(child.getPath())) {
                    return aug;
                }
            }
		}
		return null;
    }

    public static boolean containsWhenConstraint(SchemaRegistry schemaRegistry, DataSchemaNode node) {
        Optional<RevisionAwareXPath> whenCondition = node.getWhenCondition();
        boolean hasWhenConstraint = whenCondition.isPresent();

        if (!hasWhenConstraint && node.isAugmenting()) {
            DataSchemaNode parentNode = schemaRegistry.getDataSchemaNode(node.getPath().getParent());
            Pair<AugmentationSchemaNode,SchemaPath> augmentNodeAndItsResidingNode = DataStoreValidationUtil.getAugmentationSchema(schemaRegistry, parentNode, node);
            AugmentationSchemaNode augSchema = augmentNodeAndItsResidingNode == null? null : augmentNodeAndItsResidingNode.getFirst();
            Optional<RevisionAwareXPath> xpath = augSchema == null ? null : augSchema.getWhenCondition();
            if (xpath != null && xpath.isPresent()) {
                hasWhenConstraint = true;
            }
        }
        if (!hasWhenConstraint && node.isAddedByUses()) {
            DataSchemaNode parentNode = schemaRegistry.getDataSchemaNode(node.getPath().getParent());
            Pair<UsesNode,SchemaPath> usesNodeAndItsResidingNode = SchemaContextUtil.getUsesSchema(schemaRegistry, parentNode, node);
            UsesNode usesSchema = usesNodeAndItsResidingNode == null? null : usesNodeAndItsResidingNode.getFirst();
            Optional<RevisionAwareXPath> xpath = usesSchema == null ? null : usesSchema.getWhenCondition();
            if (xpath != null && xpath.isPresent()) {
                hasWhenConstraint = true;
            }
        }
        return hasWhenConstraint;
    }
    
    public static boolean containsMustWhen(SchemaRegistry schemaRegistry, DataSchemaNode node) {
        boolean mustWhen = false;
        Optional<RevisionAwareXPath> whenCondition = node.getWhenCondition();
        mustWhen = whenCondition.isPresent();
        if (node instanceof MustConstraintAware) {
            Collection<MustDefinition> mustConstraints = ((MustConstraintAware) node).getMustConstraints();
            mustWhen = mustWhen
                    || (mustConstraints != null && ! mustConstraints.isEmpty());
        }
        if (!mustWhen && node.isAugmenting()) {
            DataSchemaNode parentNode = schemaRegistry.getDataSchemaNode(node.getPath().getParent());
            Pair<AugmentationSchemaNode,SchemaPath> augmentNodeAndItsResidingNode = DataStoreValidationUtil.getAugmentationSchema(schemaRegistry, parentNode, node);
            AugmentationSchemaNode augSchema = augmentNodeAndItsResidingNode == null? null : augmentNodeAndItsResidingNode.getFirst();
            Optional<RevisionAwareXPath> xpath = augSchema == null ? null : augSchema.getWhenCondition();
            if (xpath != null && xpath.isPresent()) {
                mustWhen = true;
            }
        }
        if (!mustWhen && node.isAddedByUses()) {
            DataSchemaNode parentNode = schemaRegistry.getDataSchemaNode(node.getPath().getParent());
            Pair<UsesNode,SchemaPath> usesNodeAndItsResidingNode = SchemaContextUtil.getUsesSchema(schemaRegistry, parentNode, node);
            UsesNode usesSchema = usesNodeAndItsResidingNode == null? null : usesNodeAndItsResidingNode.getFirst();
            Optional<RevisionAwareXPath> xpath = usesSchema == null ? null : usesSchema.getWhenCondition();
            if (xpath != null && xpath.isPresent()) {
                mustWhen = true;
            }
        }
        return mustWhen;
    }

    public static boolean containsMustConstraint(DataSchemaNode node) {
        boolean isMustConstraint = false;
        if (node instanceof MustConstraintAware) {
            Collection<MustDefinition> mustConstraints = ((MustConstraintAware) node).getMustConstraints();
            return (mustConstraints != null && ! mustConstraints.isEmpty());
        }
        return isMustConstraint;
    }

    public static String getDefaultEditConfigOperationInCache(){
        String defaultOperation = (String) RequestScope.getCurrentScope().getFromCache("DEFAULT_OPERATION");
        if (defaultOperation == null) {
            defaultOperation = EditConfigDefaultOperations.MERGE;
        }
        return defaultOperation;
    }

    public static RootModelNodeAggregator getRootModelNodeAggregatorInCache(){
        return (RootModelNodeAggregator) RequestScope.getCurrentScope().getFromCache(ROOT_MODELNODE_AGGREGATOR_CACHE);
    }

    public static void setRootModelNodeAggregatorInCache(RootModelNodeAggregator rootModelNodeAggregator){
        RequestScope.getCurrentScope().putInCache(ROOT_MODELNODE_AGGREGATOR_CACHE, rootModelNodeAggregator);
    }

    public static void clearRootModelNodeAggregatorInCache(){
        RequestScope.getCurrentScope().putInCache(ROOT_MODELNODE_AGGREGATOR_CACHE, null);
    }

    public static void resetValidationContext() {
        ModelNodeDynaBeanFactory.resetCache();
        SchemaRegistryUtil.resetCache();
    }

    public static DataSchemaNode getRootSchemaNode(SchemaRegistry schemaRegistry, Step step) {
        if (step != null && step.getNodeTest() instanceof NodeNameTest) {
            NodeNameTest nodeTest = (NodeNameTest) step.getNodeTest();
            String nodeName = nodeTest.getNodeName().getName();
            Collection<DataSchemaNode> rootNodes = schemaRegistry.getRootDataSchemaNodes();
            for (DataSchemaNode rootNode:rootNodes) {
                QName qname = rootNode.getQName();
                if (qname.getLocalName().equals(nodeName)) {
                    return rootNode;
                }
            }
        }
        return null;
    }

    public static DataSchemaNode getRootSchemaNode(SchemaRegistry schemaRegistry, QName rootLocalNode) {
            Collection<DataSchemaNode> rootNodes = schemaRegistry.getRootDataSchemaNodes();
            for (DataSchemaNode rootNode:rootNodes) {
                QName qname = rootNode.getQName();
                if (qname.equals(rootLocalNode)) {
                    return rootNode;
                }
            }
        return null;
    }

    public static String getMatchedModuleNameForPrefix(Module module, String constraintPrefix) {
        if(module != null) {
            if(module.getPrefix().equals(constraintPrefix)) {
                return module.getName();
            }
            for(ModuleImport moduleImport : module.getImports()) {
                String prefix = moduleImport.getPrefix();
                if(prefix.equals(constraintPrefix)) {
                    String moduleName = moduleImport.getModuleName();
                    return moduleName;
                }
            }
        }
        return null;
    }
    
    public static Pair<SchemaRegistry, DataSchemaNode> getDataSchemaNodeAndSchemaRegistryPair(DynaBean contextBean,
                                                                                              Expression expressionToEvaluate, QName leafQName){
        DynaBean newContextBean = contextBean;
        ModelNode modelNode = null;
        while (!isReadable(newContextBean, ModelNodeWithAttributes.MODEL_NODE)
                && isReadable(newContextBean, ModelNodeWithAttributes.PARENT)) {
            newContextBean = (DynaBean) newContextBean.get(ModelNodeWithAttributes.PARENT);
        }
        if (isReadable(newContextBean, ModelNodeWithAttributes.MODEL_NODE)) {
            // get the modelNode and schemaRegistry.
            // build the childpath from the current ModelNode schemaPath
            modelNode = (ModelNode) newContextBean.get(ModelNodeWithAttributes.MODEL_NODE);
            SchemaRegistry schemaRegistry = modelNode.getSchemaRegistry();
            SchemaPath schemaPath = modelNode.getModelNodeSchemaPath();
            Expression evalExp = expressionToEvaluate;

            if (evalExp instanceof LocationPath) {
                SchemaPath childPath = null;
                if (isReadable(contextBean, ModelNodeWithAttributes.LEAF_VALUE)
                        && ((LocationPath) evalExp).getSteps()[0].getAxis() == Compiler.AXIS_PARENT) {
                    childPath = DataStoreValidationUtil.getXPathSchemaPath(schemaRegistry, schemaPath,
                            DataStoreValidationUtil.excludeFirstStep((LocationPath) evalExp));
                } else if (isReadable(contextBean, ModelNodeWithAttributes.LEAF_VALUE)
                        && ((LocationPath) evalExp).getSteps()[0].getAxis() == Compiler.AXIS_SELF && leafQName != null) {
                    childPath = getChildPath(schemaRegistry, schemaPath, ModelNodeDynaBeanFactory.getModelNodeAttributeName(leafQName.getLocalName()));
                } else {
                    childPath = DataStoreValidationUtil.getXPathSchemaPath(schemaRegistry, schemaPath, (LocationPath) evalExp);
                }
                DataSchemaNode childSchemaNode = schemaRegistry.getDataSchemaNode(childPath);

                // child SchemaNode has to be a leafRef with type IdentityRef. Else derive-from() wont work.
                if (childSchemaNode instanceof TypedDataSchemaNode) {
                    /*
                     * Recent introduction in ODL : TypedDataSchemaNode
                     * This node holds values of the same type
                     * This node is extended by LeafSchemaNode and LeafListSchemaNode
                     */
                    return new Pair<SchemaRegistry, DataSchemaNode>(schemaRegistry, childSchemaNode);
                }
            }
        }
        return null;
    }
    
    public static DataSchemaNode getRootSchemaNodeWithModuleNameInPrefix(SchemaRegistry schemaRegistry, Step step) {
        if (step != null && step.getNodeTest() instanceof NodeNameTest) {
            NodeNameTest nodeTest = (NodeNameTest) step.getNodeTest();
            String nodeName = nodeTest.getNodeName().getName();
            String moduleName = nodeTest.getNodeName().getPrefix();
            Collection<DataSchemaNode> rootNodes = schemaRegistry.getRootDataSchemaNodes();
            for (DataSchemaNode rootNode:rootNodes) {
                QName qname = rootNode.getQName();
                String rootModuleName = schemaRegistry.getModuleNameByNamespace(qname.getNamespace().toString());
                if (qname.getLocalName().equals(nodeName)) {
                    if (moduleName != null && moduleName.equals(rootModuleName)) {
                        return rootNode;
                    } else if (moduleName == null) {
                        return rootNode;
                    }
                }
            }
        }
        return null;
    }

    public static DynaBean getRootModelNode(SchemaRegistry schemaRegistry, DataSchemaNode rootSchemaNode, DSValidationContext validationContext) {
        DynaBean contextBean = null;
        if(rootSchemaNode == null){
            return null;
        }
        Collection<ModelNode> rootModelNodes = validationContext.getRootNodesOfType(rootSchemaNode.getPath(), schemaRegistry);
        for (ModelNode rootModelNode:rootModelNodes) {
            DataSchemaNode rootModelNodeSchemaNode = schemaRegistry.getDataSchemaNode(rootModelNode.getModelNodeSchemaPath());
            if (rootModelNodeSchemaNode.equals(rootSchemaNode)) {
                contextBean = (DynaBean) rootModelNode.getValue();
                break;
            }
        }
        return contextBean;
    }

    private static SchemaPath getChildPath(SchemaPath parentPath, QName childQName, SchemaRegistry schemaRegistry) {
    	SchemaPath childPath = null;
    	DataSchemaNode childNode = schemaRegistry.getChild(parentPath, childQName);
    	if (childNode == null) {
    		Collection<DataSchemaNode> children = ChoiceCaseNodeUtil.getChildrenUnderChoice(schemaRegistry, parentPath);
    		Collection<DataSchemaNode> choiceChildren = new LinkedList<DataSchemaNode>();
    		for (DataSchemaNode child:children) {
    			if (child instanceof ChoiceSchemaNode) {
    				choiceChildren.addAll(ChoiceCaseNodeUtil.getImmediateChildrenOfChoice((ChoiceSchemaNode) child));
    			} else {
    				choiceChildren.add(child);
    			}
    		}

    		for (DataSchemaNode choiceChild:choiceChildren) {
    			if (choiceChild.getQName().equals(childQName)) {
    				childPath = choiceChild.getPath();
    				break;
    			}
    		}
    	} else {
    		childPath = childNode.getPath();
    	}
    	return childPath;
    }
    
    public static DSValidationMountContext populateChildSchemaPath(SchemaRegistry schemaRegistry, SchemaPath parentPath, QName childQName) {
        DSValidationMountContext mountContext = new DSValidationMountContext();
        mountContext.setSchemaRegistry(schemaRegistry);
        SchemaPath childPath = getChildPath(parentPath, childQName, schemaRegistry);
        mountContext.setSchemaPath(childPath);
        return mountContext;
    }
    
    public static String excludeFirstStep(String xPath) {
        Expression expression = JXPathUtils.getExpression(xPath);
        if (isLocationPath(expression)) {
            return excludeFirstStep((LocationPath) expression).toString();
        }
        return xPath;
    }

    public static boolean isBoolean(String value) {
    	if (value.equals(Boolean.TRUE.toString()) || value.equals(Boolean.FALSE.toString())) {
    		return true;
    	}
    	return false;
    }
    
    /**
     * If schema-node is added by 'uses' keyword, then return the original schema node.
     * @param dataSchemaNode
     * @return
     */
    private static SchemaNode getOriginalNode(SchemaNode dataSchemaNode) {
        SchemaNode originalDataNode = dataSchemaNode;
        // find back the original definition (possibly in a grouping or augment)
        while (originalDataNode instanceof DerivableSchemaNode) {
            Optional<? extends SchemaNode> basePotential = ((DerivableSchemaNode) originalDataNode).getOriginal();
            if (basePotential.isPresent()) {
                originalDataNode = basePotential.get();
            } else {
                break;
            }            
        }
        return originalDataNode;
    }
    
    /**
     * If 'IdentityRef' type attribute is added by 'uses' keyword in edit tree and originally it is in different module/name-space, then original namespace should append in element for 'IdentityRef' type validation 
     * @param schemaNode
     * @param element
     * @param modelNode
     */
	public static void setOrginalNSIfAddedByUses(DataSchemaNode schemaNode, Element element, ModelNode modelNode) {
		if (schemaNode.isAddedByUses() && schemaNode.isAugmenting()) {
			if (schemaNode instanceof LeafSchemaNode) {
				SchemaNode node = getOriginalNode(schemaNode);
				if (node.getQName() != null && !node.getQName().equals(schemaNode.getQName())) {
					String nameSpace = node.getQName().getNamespace().toString();
					String prefix = modelNode.getSchemaRegistry().getPrefix(nameSpace);
					String qualifiedName = NetconfResources.XMLNS;
					if (prefix != null && !prefix.isEmpty()) {
						qualifiedName += COLON + prefix;
					}
					element.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, qualifiedName, nameSpace);
				}
			}
		}
	}
	
	/**
	 * Get Root SchemaPath by first step of absolute xpath
	 * @param schemaRegistry
	 * @param step
	 * @return @{SchemaPath}
	 */
	public static SchemaPath getRootSchemaPath(SchemaRegistry schemaRegistry, Step step) {
	    if (step != null && step.getNodeTest() instanceof NodeNameTest) {
	        NodeNameTest nodeTest = (NodeNameTest) step.getNodeTest();
	        String nodeName = nodeTest.getNodeName().getName();
	        Collection<SchemaPath> rootSchemaPaths = schemaRegistry.getRootSchemaPaths();
	        for (SchemaPath schemaPath:rootSchemaPaths) {
	            QName qname = schemaPath.getLastComponent();
	            if (qname.getLocalName().equals(nodeName)) {
	                return schemaPath;
	            }
	        }
	    }
	    return null;
	}
	
	public static boolean evaluateAugmentUsesWithWhen(DataSchemaNode leafNode, ModelNode childModelNode,
                                                      DSExpressionValidator expressionValidator, DSValidationContext validationContext){
	    if (leafNode.isAugmenting()) {
	        SchemaRegistry schemaRegistry = childModelNode.getSchemaRegistry();
	        DataSchemaNode parentNode = schemaRegistry.getDataSchemaNode(leafNode.getPath().getParent());
	        Pair<AugmentationSchemaNode,SchemaPath> augmentNodeAndItsResidingNode = DataStoreValidationUtil.getAugmentationSchema(schemaRegistry, parentNode, leafNode);
	        AugmentationSchemaNode augSchema = augmentNodeAndItsResidingNode == null? null : augmentNodeAndItsResidingNode.getFirst();
	        RevisionAwareXPath xpath = augSchema == null ? null : augSchema.getWhenCondition().orElse(null);
	        
	        if(xpath == null && leafNode.isAddedByUses()){
	            Pair<UsesNode,SchemaPath> usesNodeAndItsResidingNode = SchemaContextUtil.getUsesSchema(schemaRegistry, parentNode, leafNode);
	            UsesNode usesNode = usesNodeAndItsResidingNode == null? null : usesNodeAndItsResidingNode.getFirst();
	            xpath = usesNode == null ? null : usesNode.getWhenCondition().orElse(null);
	        }
	        if (xpath != null) {
                ValidatedAugment validatedAugment = validationContext.getValidatedAugment();
                boolean validated = validatedAugment.isValidated(childModelNode, xpath);
                if(!validated) {
                    DSAugmentOrUsesValidation augmentValidator = new DSAugmentOrUsesValidation(expressionValidator, childModelNode.getSchemaRegistry());
                    Boolean result = augmentValidator.evaluate(childModelNode, leafNode, validationContext);
                    validatedAugment.storeResult(childModelNode, xpath, result);
                    return result;
                } else {
                    return validatedAugment.getValidationResult(childModelNode, xpath);
                }
	        }
	    }
	    return true;
	}

    public static List<QName> getRelativeQNames(ModelNodeWithAttributes modelNode, SchemaPath childSP) {
        List<QName> relativeQNames = new ArrayList<>();
        SchemaPath parentSP = modelNode.getModelNodeSchemaPath();
        SchemaRegistry sr = modelNode.getSchemaRegistry();
        relativeQNames.addAll(getNonChoiceQNames(sr, childSP));

        List<QName> parentQnames = getNonChoiceQNames(modelNode.getSchemaRegistryForParent(), parentSP);
        if ( modelNode.hasSchemaMount()){
            return relativeQNames;
        }
        if( sr.getDataSchemaNode(childSP) instanceof DataNodeContainer) {
            return relativeQNames.subList(parentQnames.size(), relativeQNames.size());
        } else {
            //for leafs, leaf-lists, we need the parent
            return relativeQNames.subList(parentQnames.size(), relativeQNames.size() - 1);
        }
    }

    private static List<QName> getNonChoiceQNames(SchemaRegistry sr, SchemaPath sp) {
        List<QName> qnames = new ArrayList<>();
        DataSchemaNode dataSchemaNode = sr.getDataSchemaNode(sp);
        qnames.add(dataSchemaNode.getQName());
        dataSchemaNode = sr.getDataSchemaNode(dataSchemaNode.getPath().getParent());
        while(dataSchemaNode != null ){
            if(!(dataSchemaNode instanceof ChoiceSchemaNode || dataSchemaNode instanceof CaseSchemaNode)){
                qnames.add(dataSchemaNode.getQName());
            }
            dataSchemaNode = sr.getDataSchemaNode(dataSchemaNode.getPath().getParent());
        }
        Collections.reverse(qnames);
        return qnames;
    }

    public static String buildIndexXPath(String changedNodeXPath, List<QName> relativeQnames) {
        StringBuilder sb = new StringBuilder();
        sb.append(changedNodeXPath);
        for(QName qname: relativeQnames){
            sb.append("/").append(qname.getLocalName());
        }
        return sb.toString();
    }

    /*
    replaces/appends module name as prefix for a given expression (including predicates) AND schemaNode in which the expression is present

    /if:interfaces/if:interace[name="abc"]/enabled TO
    /ietf-interfaces:interfaces/ietf-interfaces:interface[ietf-interfaces:name="abc"]/ietf-interfaces:enabled

    /interfaces/interace[name="abc"]/enabled TO
    /ietf-interfaces:interfaces/ietf-interfaces:interface[ietf-interfaces:name="abc"]/ietf-interfaces:enabled
     */
    public static Expression replacePrefixesWithModuleName(Expression xPath, SchemaRegistry schemaRegistry, ModelNode contextModelNode, SchemaNode schemaNode, Module constraintNodeModule) {
        if(schemaNode == null) {
            schemaNode = schemaRegistry.getDataSchemaNode(contextModelNode.getModelNodeSchemaPath());
        }
        Expression returnExpression = schemaRegistry.getExpressionWithModuleNameInPrefix(schemaNode.getPath(), xPath);
        if(returnExpression != null) {
            return returnExpression;
        }
        Module currentModule = constraintNodeModule != null ? constraintNodeModule : schemaRegistry
                .getModuleByNamespace(schemaNode.getQName().getModule().getNamespace().toString());

        Expression newExpression = transformExpression(ExpressionTranformationType.FROM_PREFIX_TO_MODULENAME, xPath, schemaRegistry, schemaNode, currentModule, null);

        if (newExpression != null) {
            schemaRegistry.registerExpressionWithModuleNameInPrefix(schemaNode.getPath(), xPath, newExpression.toString());
            returnExpression = newExpression;
        } else {
            returnExpression = xPath;
        }

        return returnExpression;
    }

    public static Expression transformExpression(ExpressionTranformationType transformationType, Expression expression, SchemaRegistry schemaRegistry, SchemaNode schemaNode, Module currentModule, Module previousModule) {
        Expression returnExpression = null;
        if (expression instanceof LocationPath) {
            LocationPath locationPath = (LocationPath) expression;
            Step[] newSteps =  getNewStepsForTransformation(transformationType,
                    locationPath.getSteps(), schemaRegistry, schemaNode,
                    currentModule, previousModule);
            returnExpression = new LocationPath(((LocationPath) expression).isAbsolute(), newSteps);
        } else if (expression instanceof ExtensionFunction) {
            ExtensionFunction extensionFunction = (ExtensionFunction)expression;
            if(extensionFunction.getArguments() == null) {
                returnExpression = expression;
            } else {
                Expression[] newArguements = getNewExpressionsForTransformation(transformationType,
                        extensionFunction.getArguments(), schemaRegistry, schemaNode,
                        currentModule, previousModule);
                returnExpression = new ExtensionFunction(extensionFunction.getFunctionName(),
                        newArguements);
            }
        }
        else if (expression instanceof CoreFunction) {
            CoreFunction coreFunction = (CoreFunction) expression;
            Expression[] newArguements;
            if(coreFunction.getArguments() != null) {
                newArguements = getNewExpressionsForTransformation(transformationType,
                        coreFunction.getArguments(), schemaRegistry, schemaNode,
                        currentModule, previousModule);
            } else {
                newArguements = new Expression[0];
            }
            returnExpression = new CoreFunction(coreFunction.getFunctionCode(), newArguements);
        }
        else if (expression instanceof ExpressionPath) {
            ExpressionPath expressionPath = (ExpressionPath) expression;
            Expression oldExpression = expressionPath.getExpression();
            Expression newExpression = transformExpression(transformationType,
                    oldExpression, schemaRegistry, schemaNode,
                    currentModule, previousModule);

            Expression[] newPredicates =  getNewExpressionsForTransformation(transformationType,
                    expressionPath.getPredicates(), schemaRegistry, schemaNode,
                    currentModule, previousModule);

            Step[] newSteps = getNewStepsForTransformation(transformationType,
                    expressionPath.getSteps(), schemaRegistry, schemaNode,
                    currentModule, previousModule);

            returnExpression = new ExpressionPath(newExpression, newPredicates, newSteps);
        } else if (expression instanceof CoreOperation) {
            CoreOperation coreOperation = (CoreOperation) expression;
            Expression[] newArguements =  getNewExpressionsForTransformation(transformationType,
                    coreOperation.getArguments(), schemaRegistry, schemaNode,
                    currentModule, previousModule);

            returnExpression = JXPathUtils.getCoreOperation(coreOperation, newArguements);
        } else {
            returnExpression = expression;
        }
        return returnExpression;
    }

    private static Expression[] getNewExpressionsForTransformation(ExpressionTranformationType transformationType, Expression[] oldPredicates, SchemaRegistry schemaRegistry, SchemaNode schemaNode, Module currentModule, Module previousModule) {
        if(oldPredicates == null) {
            return new Expression[0];
        }
        Expression[] newPredicates = new Expression[oldPredicates.length];
        for (int predicateIndex = 0; predicateIndex < oldPredicates.length; predicateIndex++) {
            Expression oldPredicate = oldPredicates[predicateIndex];
            newPredicates[predicateIndex] = transformExpression(transformationType,
                    oldPredicate, schemaRegistry, schemaNode,
                    currentModule, previousModule);
        }
        return newPredicates;
    }

    private static Step[] getNewStepsForTransformation(ExpressionTranformationType transformationType, Step[] oldSteps, SchemaRegistry schemaRegistry, SchemaNode schemaNode, Module currentModule, Module previousModule) {
        Step[] newSteps = new Step[oldSteps.length];
        for (int stepIndex = 0; stepIndex < oldSteps.length; stepIndex++) {
            Step oldStep = oldSteps[stepIndex];
            if (oldStep.getNodeTest() instanceof NodeNameTest) {
                NodeNameTest node = (NodeNameTest) oldStep.getNodeTest();
                org.apache.commons.jxpath.ri.QName qName = getQNameForTransformation(transformationType, currentModule, previousModule, node);
                if(qName != null && qName.getPrefix() != null) {
                    Optional<Module> optModule = schemaRegistry.getModule(qName.getPrefix());
                    if (optModule.isPresent()) {
                        previousModule = optModule.get();
                    }
                }
                Expression[] newStepPredicates = getNewExpressionsForTransformation(transformationType,
                        oldStep.getPredicates(), schemaRegistry, schemaNode,
                        currentModule, previousModule);
                String nameSpace = null;
                if(schemaRegistry != null && qName.getPrefix() != null) {
                    nameSpace = schemaRegistry.getNamespaceOfModule(qName.getPrefix());
                }
                newSteps[stepIndex] = new YangStep(qName, nameSpace, newStepPredicates);
            } else {
                newSteps[stepIndex] = oldStep;
            }
        }
        return newSteps;
    }

    private static org.apache.commons.jxpath.ri.QName getQNameForTransformation(ExpressionTranformationType transformationType, Module currentModule, Module previousModule, NodeNameTest node) {
        switch (transformationType) {
            case FROM_MODULENAME_TO_MODULENAME_LOCALNAME_WITH_SEPARATOR:
                return getQNameWithModuleNameLocalNameHavingSeparator(node);
            case FROM_PREFIX_TO_MODULENAME:
                return  getQNameWithModuleNameAsPrefix(currentModule, previousModule, node);
            default:
                return null;
        }
    }

    private static org.apache.commons.jxpath.ri.QName getQNameWithModuleNameAsPrefix(Module currentModule, Module previousModule, NodeNameTest node) {
	    String currentPrefix = currentModule.getPrefix();
        String prefix = node.getNodeName().getPrefix();
        String localName = node.getNodeName().getName();
        String moduleName = null;
        if(prefix == null) {
            prefix = currentPrefix;
            moduleName = currentModule.getName();
        }
        if(moduleName == null) {
            if (prefix != null && currentModule.getPrefix().equals(prefix)) {
                moduleName = currentModule.getName();
            } else {
                Set<ModuleImport> moduleImports = currentModule.getImports();
                for (ModuleImport moduleImport : moduleImports) {
                    if (moduleImport.getPrefix().equals(prefix)) {
                        moduleName = moduleImport.getModuleName();
                        break;
                    }
                }
                if (moduleName == null) {
                    Set<Module> subModules = currentModule.getSubmodules();
                    for (Module subModule : subModules) {
                        if (subModule.getPrefix().equals(prefix)) {
                            moduleName = subModule.getName();
                            break;
                        } else {
                            Set<ModuleImport> subModuleImports = subModule.getImports();
                            for (ModuleImport subModuleImport : subModuleImports) {
                                if (subModuleImport.getPrefix().equals(prefix)) {
                                    moduleName = subModuleImport.getModuleName();
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

        org.apache.commons.jxpath.ri.QName qName = new org.apache.commons.jxpath.ri.QName(moduleName, localName);
        return qName;
    }

    private static org.apache.commons.jxpath.ri.QName getQNameWithModuleNameLocalNameHavingSeparator(NodeNameTest node) {
        String moduleName = node.getNodeName().getPrefix();
        String localName = node.getNodeName().getName();

        String transformedLocalName = localName;
        if(moduleName != null && !moduleName.isEmpty()) {
            transformedLocalName = moduleName + ModelNodeWithAttributes.MODULE_NAME_LOCAL_NAME_SEPARATOR + localName;
        }
        org.apache.commons.jxpath.ri.QName qName = new org.apache.commons.jxpath.ri.QName(null, transformedLocalName);
        return qName;
    }

    public static ModelNodeDynaBeanContext getModelNodeDynaBeanContext(DynaBean dynaBean, String localName, String nameSpace, Map<QName,ConfigLeafAttribute> matchCriteria) {
        ModelNode modelNode = DataStoreValidationUtil.getContextNodeFromDynaBean(dynaBean);
        SchemaRegistry schemaRegistry = null;
        if(modelNode != null) {
            schemaRegistry = modelNode.getSchemaRegistry();
        }
	    return getModelNodeDynaBeanContext(schemaRegistry, localName, nameSpace, matchCriteria);
    }

    public static ModelNodeDynaBeanContext getModelNodeDynaBeanContext(SchemaRegistry schemaRegistry, String localName, String nameSpace, Map<QName,ConfigLeafAttribute> matchCriteria) {
	    if(schemaRegistry != null) {
            String moduleName = schemaRegistry.getModuleNameByNamespace(nameSpace);
            return getModelNodeDynaBeanContext(localName, moduleName, matchCriteria);
        }
	    return (new ModelNodeDynaBeanContextBuilder()).build();
    }

    public static ModelNodeDynaBeanContext getModelNodeDynaBeanContext(String localName, String moduleName, Map<QName,ConfigLeafAttribute> matchCriteria) {
        String localNameWithModuleName = localName;
        if(moduleName != null) {
            localNameWithModuleName = moduleName + COLON + localName;
        }
        ModelNodeDynaBeanContextBuilder contextBuilder = new ModelNodeDynaBeanContextBuilder();
        contextBuilder.setLeafNameWithModuleNameInPrefix(localNameWithModuleName);
        contextBuilder.setMatchCriteria(matchCriteria);
        return contextBuilder.build();
    }

    public static ModelNode getContextNodeFromDynaBean(DynaBean dynaBean) {
        return DataStoreValidationUtil.isReadable(dynaBean, ModelNodeWithAttributes.MODEL_NODE)?(ModelNode) dynaBean.get(ModelNodeWithAttributes.MODEL_NODE):null;
    }
    
    public static boolean isMandatoryChildPresentWithoutDefaultsUnderContainer(final ContainerSchemaNode node) {
    	for (DataSchemaNode child : node.getChildNodes()) {
    		if (child instanceof LeafSchemaNode) {
    			LeafSchemaNode leafNode = (LeafSchemaNode) child;
    			if (leafNode.isMandatory() && !leafNode.getType().getDefaultValue().isPresent()) {
    				return true;
    			}
    		} else if (child instanceof ChoiceSchemaNode) {
    			if (((ChoiceSchemaNode) child).isMandatory()) {
    				return true;
    			}
    		} else if( child instanceof ElementCountConstraintAware){
    			Optional<ElementCountConstraint> optElementCountConstraint = ((ElementCountConstraintAware) child).getElementCountConstraint();
    			if (optElementCountConstraint.isPresent()) {
    				ElementCountConstraint elementCountConstraint = optElementCountConstraint.get();
    				if (elementCountConstraint != null && elementCountConstraint.getMinElements() != null && elementCountConstraint.getMinElements() > 0){
    					return true;
    				}
    			}
    		}
    	}
    	return false;
    }

    public static boolean isMatch(Map<QName, ConfigLeafAttribute> matchCriteria, ModelNode node) throws GetException {
        Element getConfig = node.getConfig(new GetConfigContext(new InternalNetconfClientInfo(NetconfResources.IMPLICATION_CHANGE, 1), DocumentUtils.createDocument(), null, new ConfigAttributeGetContext()),
                NetconfQueryParams.NO_PARAMS);
        for(Map.Entry<QName, ConfigLeafAttribute> criteriaEntry : matchCriteria.entrySet()){
            Node matchNode = DocumentUtils.getChildNodeByName(getConfig, criteriaEntry.getKey().getLocalName(), criteriaEntry
                    .getKey().getNamespace().toString());
            if(matchNode != null){
                if(!criteriaEntry.getValue().getStringValue().equals(matchNode.getTextContent())){
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Remove the default LEAF value from deviceConfig XML element.
     *
     * 1) Iterate all top level xml child elements
     *      Find equivalent yang schema node of top level xml element
     *          i) Iterate children of top level xml element and children of yang schema node
     *              a) IF schemaNode is CONTAINER
     *                      Iterate it's children again
     *                      Remove empty container node if it is Non-presence container without children
     *              b) IF schemaNode is LIST
     *                      Iterate it's Children again
     *              c) IF schemaNode is LEAF (default leaf)
     *                      Remove Element if it has default of yang leaf value
     *              d) Else
     *                      Skip other schema nodes like leaf-list
     */

    public static Element removeDefaultLeafValueFromXmlConfig(SchemaRegistry schemaRegistry, Element configXml) {
        List<Element> rootElements = DocumentUtils.getChildElements(configXml);
        Collection<DataSchemaNode> rootSchemaNodes = schemaRegistry.getRootDataSchemaNodes();
        if (rootElements != null && !rootElements.isEmpty() && !rootSchemaNodes.isEmpty()) {
            for (Element element : rootElements) {
                DataSchemaNode parentSchemaNode = findSchemaNode(schemaRegistry, rootSchemaNodes, element);
                if (parentSchemaNode != null) {
                    removeDefaultValueElement(schemaRegistry, parentSchemaNode, element);
                }
            }
        }
        return configXml;
    }

    private static boolean isNonPresenceContainerElementWithOutChildren(DataSchemaNode schemaNode, Element element) {
        return schemaNode instanceof ContainerSchemaNode && (!((ContainerSchemaNode) schemaNode).isPresenceContainer())
                && !DocumentUtils.hasDirectChildElement(element);
    }

    private static void removeDefaultValueElement(SchemaRegistry schemaRegistry, DataSchemaNode parentSchemaNode, Node parentElement) {
        List<Element> childNodes = DocumentUtils.getChildElements(parentElement);
        Collection<DataSchemaNode> childSchemaNodes = schemaRegistry.getNonChoiceChildren(parentSchemaNode.getPath());
        for (Element childElement : childNodes) {
            DataSchemaNode childSchemaNode = findSchemaNode(schemaRegistry, childSchemaNodes, childElement);
            if (childSchemaNode != null) {
                if (childSchemaNode instanceof ListSchemaNode) {
                    removeDefaultValueElement(schemaRegistry, childSchemaNode, childElement);
                } else if (childSchemaNode instanceof ContainerSchemaNode) {
                    removeDefaultValueElement(schemaRegistry, childSchemaNode, childElement);
                    // Remove Element if it non-presence container without any childnodes
                    if (isNonPresenceContainerElementWithOutChildren(childSchemaNode, childElement)) {
                        parentElement.removeChild(childElement);
                    }
                } else if (childSchemaNode instanceof LeafSchemaNode) {
                    LeafSchemaNode leafNode = (LeafSchemaNode) childSchemaNode;
                    Optional<? extends Object> defaultValue = leafNode.getType().getDefaultValue();
                    if (defaultValue.isPresent()) {
                        String defaultValueStr = defaultValue.get().toString();
                        boolean isKeyLeaf = isKeyNode(leafNode, leafNode.getQName(), schemaRegistry);
                        // Remove element if it has default leaf value AND dont remove if key leaf has default value
                        if (defaultValueStr.equals(childElement.getTextContent()) && !isKeyLeaf) {
                            parentElement.removeChild(childElement);
                        }
                    }
                } else {
                    // Skip all other schema nodes like leaflist
                }
            }
        }
    }

    public static DataSchemaNode findSchemaNode(SchemaRegistry schemaRegistry, Collection<DataSchemaNode> schemaNodes, Node child) {
        QName childQName = getQName(schemaRegistry, child);
        if (childQName != null) {
            for (DataSchemaNode schemaNode : schemaNodes) {
                if (schemaNode.getQName().getNamespace().equals(childQName.getNamespace())
                        && schemaNode.getQName().getLocalName().equals(childQName.getLocalName())) {
                    return schemaNode;
                }
            }
        }
        return null;
    }

    public static QName getQName(SchemaRegistry schemaRegistry, Node node) {
        String localName = node.getLocalName();
        String namespace = node.getNamespaceURI();
        return schemaRegistry.lookupQName(namespace, localName);
    }

    private static boolean isKeyNode(DataSchemaNode dataSchemaNode, QName nodeQName, SchemaRegistry schemaRegistry) {
        if (dataSchemaNode instanceof LeafSchemaNode) {
            DataSchemaNode parentNode = SchemaRegistryUtil.getDataParent(schemaRegistry, dataSchemaNode.getPath());
            if (parentNode != null && parentNode instanceof ListSchemaNode) {
                List<QName> keyDef = ((ListSchemaNode) parentNode).getKeyDefinition();
                return keyDef.contains(nodeQName);
            }
        }
        return false;
    }
}
