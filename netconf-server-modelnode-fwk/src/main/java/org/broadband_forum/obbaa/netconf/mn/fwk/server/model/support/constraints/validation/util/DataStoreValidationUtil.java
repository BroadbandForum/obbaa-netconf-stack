package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.xml.XMLConstants;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.compiler.Constant;
import org.apache.commons.jxpath.ri.compiler.CoreFunction;
import org.apache.commons.jxpath.ri.compiler.CoreOperation;
import org.apache.commons.jxpath.ri.compiler.CoreOperationAnd;
import org.apache.commons.jxpath.ri.compiler.CoreOperationOr;
import org.apache.commons.jxpath.ri.compiler.Expression;
import org.apache.commons.jxpath.ri.compiler.ExpressionPath;
import org.apache.commons.jxpath.ri.compiler.ExtensionFunction;
import org.apache.commons.jxpath.ri.compiler.LocationPath;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.Operation;
import org.apache.commons.jxpath.ri.compiler.Step;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigOperations;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaMountRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaMountRegistryProvider;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.ChoiceCaseNodeUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.AnvExtensions;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildContainerHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildListHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeDynaBean;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeDynaBeanFactory;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeGetException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.DSValidationContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.DSValidationMountContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.ProxyValidationModelNode;
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
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DerivableSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.MustConstraintAware;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
        if (!requestType.isRpc() && (operationAttribute == null || operationAttribute.isEmpty()
                || (!operationAttribute.equals(EditConfigOperations.CREATE) && !operationAttribute.equals(EditConfigOperations.REPLACE)))) {
            return false;

        }

        return true;
    }

    /**
     * Given a DataSchemaNode, collects each data step name in the path towards root in a list. Returns the schemaPath of the 
     * input schemaNode
     */
    public static SchemaPath buildAbsAccessPath(SchemaRegistry schemaRegistry, DataSchemaNode schemaNode, List<Step> inputPath) {
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
            if (dynaBean instanceof ModelNodeDynaBean) {
                return ((ModelNodeDynaBean)dynaBean).isReadable(localName);
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
        return new LocationPath(true, newSteps);
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
    
    public static Collection<ModelNode> getChildListModelNodes(ModelNode parentNode, DataSchemaNode childSchemaNode) throws ModelNodeGetException {
        return getChildListModelNodes(parentNode, childSchemaNode, Collections.emptyMap());
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static Collection<ModelNode> getChildListModelNodes(ModelNode parentNode, DataSchemaNode childSchemaNode,
            Map<QName,ConfigLeafAttribute> matchCriteria) throws ModelNodeGetException {
        Collection<ModelNode> listNodes = new ArrayList<>();
        QName childQName = childSchemaNode.getQName();
        ModelNodeId parentModelNodeId = parentNode.getModelNodeId();
        String dynaAttributeName = ModelNodeDynaBeanFactory.getDynaBeanAttributeName(childQName.getLocalName());

        boolean proxyModelNode = parentNode instanceof ProxyValidationModelNode;

        if (ModelNodeDynaBeanFactory.containsBeanForModelNode(parentModelNodeId)) {
            // If the modelNodeId is in cache, dynaBean is already created
            Collection<DynaBean> beans = null;
            DynaBean dynaBean = (DynaBean) parentNode.getValue();
            if(parentNode.isChildBigList(childSchemaNode)){
                beans = ModelNodeDynaBean.withMatchCriteria(matchCriteria, () -> dynaBean == null ? null : (Collection) dynaBean.get(dynaAttributeName));
            }else {
                beans = dynaBean == null ? null : (Collection) dynaBean.get(dynaAttributeName);
            }

            if (beans != null) {
                for (DynaBean bean:beans) {
                    if (bean instanceof ModelNodeDynaBean) {
                        ModelNodeWithAttributes listNode = (ModelNodeWithAttributes) bean.get(ModelNodeWithAttributes.MODEL_NODE);
						boolean matchFound = true;
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

    @SuppressWarnings("unchecked")
	public static ModelNode getChildContainerModelNode(ModelNode parentNode, DataSchemaNode childSchemaNode) throws ModelNodeGetException {
        ModelNode childContainer = null;
        QName childQName = childSchemaNode.getQName();
        String dynaAttributeName = ModelNodeDynaBeanFactory.getDynaBeanAttributeName(childQName.getLocalName());
        boolean proxyModelNode = parentNode instanceof ProxyValidationModelNode;
        ModelNodeId parentModelNodeId = parentNode.getModelNodeId();

        if (ModelNodeDynaBeanFactory.containsBeanForModelNode(parentModelNodeId)) {
            ModelNodeDynaBean dynaBean = (ModelNodeDynaBean) parentNode.getValue();
			if (DataStoreValidationUtil.isReadable(dynaBean, dynaAttributeName)) {
				Object value = dynaBean.get(dynaAttributeName);
				if (value instanceof List) {
					List<Object> objects = (List<Object>) value;
					if (objects != null) {
						for (Object innerObject : objects) {
							if (innerObject instanceof DynaBean) {
								// for a root node which can have an attribute as the
								// same name as the node, we will have a non
								// bean object.
								DynaBean childBean = (DynaBean) innerObject;
								childContainer = (ModelNode) (childBean == null ? null
										: childBean.get(ModelNodeWithAttributes.MODEL_NODE));
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
    
    public static Document getValidationDocument() {
        return getValidationContext().getDocument();
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
        
    public static AugmentationSchemaNode getAugmentationSchema(DataSchemaNode parentSchemaNode, DataSchemaNode child) {
        if (parentSchemaNode instanceof AugmentationTarget) {
            Set<AugmentationSchemaNode> augs = ((AugmentationTarget) parentSchemaNode).getAvailableAugmentations();
            for (AugmentationSchemaNode aug : augs) {
                if (aug.getChildNodes().contains(child)) {
                    return aug;
                }
            }
        }
        return null;
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
            AugmentationSchemaNode augSchema = DataStoreValidationUtil.getAugmentationSchema(parentNode, node);
            Optional<RevisionAwareXPath> xpath = augSchema == null ? null : augSchema.getWhenCondition();
            if (xpath != null && xpath.isPresent()) {
                mustWhen = true;
            }
        }

        return mustWhen;
    }

    public static DSValidationContext getValidationContext() {
        DSValidationContext context = (DSValidationContext) RequestScope.getCurrentScope().getFromCache(DSValidationContext.class.getName());
        if (context == null) {
            context = new DSValidationContext();
            RequestScope.getCurrentScope().putInCache(DSValidationContext.class.getName(), context);
        }
        return context;
    }

    public static void resetValidationContext() {
        ModelNodeDynaBeanFactory.resetCache();
        RequestScope.getCurrentScope().putInCache(DSValidationContext.class.getName(), null);
        SchemaRegistryUtil.resetCache();
    }
    
    public static DataSchemaNode getRootSchemaNode(SchemaRegistry schemaRegistry, Step step) {
        if (step != null && step.getNodeTest() instanceof NodeNameTest) {
            NodeNameTest nodeTest = (NodeNameTest) step.getNodeTest();
            String nodeName = nodeTest.getNodeName().getName();
            String prefix = nodeTest.getNodeName().getPrefix();
            Collection<DataSchemaNode> rootNodes = SchemaRegistryUtil.getSchemaRegistry(schemaRegistry).getRootDataSchemaNodes();
            for (DataSchemaNode rootNode:rootNodes) {
                QName qname = rootNode.getQName();
                String rootPrefix = schemaRegistry.getPrefix(qname.getNamespace().toString());
                if (qname.getLocalName().equals(nodeName)) {
                    if (prefix != null && prefix.equals(rootPrefix)) {
                        return rootNode;
                    } else if (prefix == null) {
                        return rootNode;
                    }
                }
            }
        }
        return null;
    }

    public static DynaBean getRootModelNode(SchemaRegistry schemaRegistry, DataSchemaNode rootSchemaNode) {
        DynaBean contextBean = null;
        if(rootSchemaNode == null){
            return null;
        }
        QName qName = rootSchemaNode.getQName();
        Collection<ModelNode> rootModelNodes = DataStoreValidationUtil.getValidationContext().getRootNodesOfType(qName.getNamespace().toString(), qName.getLocalName());
        for (ModelNode rootModelNode:rootModelNodes) {
            DataSchemaNode rootModelNodeSchemaNode = schemaRegistry.getDataSchemaNode(rootModelNode.getModelNodeSchemaPath());
            if (rootModelNodeSchemaNode.equals(rootSchemaNode)) {
                contextBean = (DynaBean) rootModelNode.getValue();
                break;
            }
        }
        return contextBean;
    }
    
    /**
     * Get the Model node of root schema node from Helper for Schema mount enabled case
     * 
     * @param globalSchemaRegistry
     * @param rootSchemaNode
     * @param parentModelNode
     * @return {DynaBean}
     */
	public static DynaBean getMountRootModelNode(SchemaRegistry globalSchemaRegistry, DataSchemaNode rootSchemaNode,
			ModelNode parentModelNode) {
		DynaBean contextBean = null;
		if (rootSchemaNode == null) {
			return null;
		}
		SchemaPath mountPath = SchemaRegistryUtil.getSchemaRegistry(globalSchemaRegistry).getMountPath();
		SchemaMountRegistry mountRegistry = globalSchemaRegistry.getMountRegistry();
		if (mountPath != null && mountRegistry != null) {
			SchemaMountRegistryProvider provider = mountRegistry.getProvider(mountPath);
			if (provider != null) {
				ModelNodeHelperRegistry helperRegistry = provider.getModelNodeHelperRegistry(null);
				if (helperRegistry != null) {
			        Collection<ModelNode> rootModelNodes = getModuleRootFromChildHelpers(parentModelNode,mountPath,rootSchemaNode,helperRegistry);
			        for (ModelNode rootModelNode:rootModelNodes) {
			            DataSchemaNode rootModelNodeSchemaNode = SchemaRegistryUtil.getSchemaRegistry(globalSchemaRegistry).getDataSchemaNode(rootModelNode.getModelNodeSchemaPath());
			            if (rootModelNodeSchemaNode.equals(rootSchemaNode)) {
			                contextBean = (DynaBean) rootModelNode.getValue();
			                break;
			            }
			        }
				}
			}
		}
		return contextBean;
	}
	
	private static List<ModelNode> getModuleRootFromChildHelpers(ModelNode parentModelNode, SchemaPath mountPath, DataSchemaNode rootSchemaNode, ModelNodeHelperRegistry helperRegistry) {
		List<ModelNode> rootNodes = new ArrayList<>();
		String requiredLocalName = rootSchemaNode.getQName().getLocalName();
		String requiredNameSpace = rootSchemaNode.getQName().getNamespace().toString();
		Collection<ChildContainerHelper> childContainerHelpers = helperRegistry.getChildContainerHelpers(mountPath).values();
		if (childContainerHelpers != null && !childContainerHelpers.isEmpty()) {
			for (ChildContainerHelper helper : childContainerHelpers) {
				try {
					DataSchemaNode helperRootNode = helper.getSchemaNode();
					if (helperRootNode != null) {
						QName helperQName = helperRootNode.getQName();
						if (helperQName.getLocalName().equals(requiredLocalName) && helperQName.getNamespace().toString().equals(requiredNameSpace)) {
							ModelNode rootModelNode = helper.getValue(parentModelNode);
							if(rootModelNode != null){
								rootNodes.add(rootModelNode);
							}
						}
					}
				} catch (ModelNodeGetException e) {
					LOGGER.error("Error while getting root node from helpers", e);
				}
			}
		}
		Collection<ChildListHelper> childListHelpers = helperRegistry.getChildListHelpers(mountPath).values();
		if(childListHelpers != null && !childListHelpers.isEmpty()){
		    for (ChildListHelper helper : childListHelpers) {
	            try {
	                ListSchemaNode helperSchemaNode = helper.getSchemaNode();
	                if (helperSchemaNode != null) {
	                    QName helperQName = helperSchemaNode.getQName();
	                    if (helperQName.getLocalName().equals(rootSchemaNode.getQName().getLocalName()) && helperQName.getNamespace().toString().equals(rootSchemaNode.getQName().getNamespace().toString())) {
	                        rootNodes.addAll(helper.getValue(parentModelNode, Collections.emptyMap()));
	                    }
	                }
	            } catch (ModelNodeGetException e) {
	                LOGGER.error("Error while getting root node from helpers", e);
	            }
	        }
		}
	    return rootNodes;
	}
    
    private static SchemaPath getChildPath(SchemaPath parentPath, QName childQName, SchemaRegistry schemaRegistry) {
    	SchemaPath childPath = null;
    	DataSchemaNode childNode = schemaRegistry.getChild(parentPath, childQName);
    	if (childNode == null) {
    		Collection<DataSchemaNode> children = ChoiceCaseNodeUtil.getChildrenUnderChoice(schemaRegistry, parentPath);
    		Collection<DataSchemaNode> choiceChildren = new LinkedList<DataSchemaNode>();
    		for (DataSchemaNode child:children) {
    			if (ChoiceCaseNodeUtil.isChoiceOrCaseNode(child)) {
    				choiceChildren.addAll(ChoiceCaseNodeUtil.getImmediateChildrenOfChoice(child));
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
        if (childPath == null && parentPath.getLastComponent() != null) {
            DataSchemaNode schemaNode = schemaRegistry.getDataSchemaNode(parentPath);
            if (AnvExtensions.MOUNT_POINT.isExtensionIn(schemaNode)) {
                SchemaRegistry mountRegistry = SchemaRegistryUtil.getMountRegistry();
                if (mountRegistry != null) {
                    childPath = mountRegistry.getDescendantSchemaPath(parentPath, childQName);
                    mountContext.setSchemaRegistry(mountRegistry);
                }
            }
        }
        mountContext.setSchemaPath(childPath);
        return mountContext;
    }
    
    public static DSValidationMountContext populateChildSchemaPath(SchemaRegistry schemaRegistry, SchemaPath parentPath, EditContainmentNode childNode) {
    	QName childQName = childNode.getQName();
        DSValidationMountContext mountContext = new DSValidationMountContext();
        mountContext.setSchemaRegistry(schemaRegistry);
        SchemaPath childPath = getChildPath(parentPath, childQName, schemaRegistry);
        if (childPath == null && parentPath.getLastComponent() != null) {
            DataSchemaNode schemaNode = schemaRegistry.getDataSchemaNode(parentPath);
            if (AnvExtensions.MOUNT_POINT.isExtensionIn(schemaNode)) {
            	SchemaRegistryUtil.resetSchemaRegistryCache();
                SchemaRegistry mountRegistry = SchemaRegistryUtil.getMountRegistry();
                if (mountRegistry != null) {
                    childPath = mountRegistry.getDescendantSchemaPath(parentPath, childQName);
                    mountContext.setSchemaRegistry(mountRegistry);
                    
                } else {
                	SchemaMountRegistryProvider provider = schemaRegistry.getMountRegistry().getProvider(parentPath);
                    if (provider != null) {
        				try {
        					mountRegistry = provider.getSchemaRegistry(null, childNode);
        				} catch (GetException e) {
        					throw new ValidationException(e.getRpcError());
        				}
        				childPath = mountRegistry.getDescendantSchemaPath(parentPath, childQName);
        				mountContext.setSchemaRegistry(mountRegistry);
                    }
                }
                Map<String, Object> currentScope = SchemaRegistryUtil.getMountCurrentScope();
                mountContext.setMountCurrentScope(currentScope);
            }
        }
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
						qualifiedName += ":" + prefix;
					}
					element.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, qualifiedName, nameSpace);
				}
			}
		}
	}

}
