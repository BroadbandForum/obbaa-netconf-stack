package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.jxpath.JXPathContext;
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
import org.apache.commons.jxpath.ri.compiler.NodeTest;
import org.apache.commons.jxpath.ri.compiler.Operation;
import org.apache.commons.jxpath.ri.compiler.Step;
import org.apache.commons.lang3.math.NumberUtils;
import org.broadband_forum.obbaa.netconf.api.server.NetconfQueryParams;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.ChoiceCaseNodeUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.utils.EntityRegistryBuilder;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildContainerHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.GenericConfigAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.InstanceIdentifierConfigAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeDynaBean;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeDynaBeanFactory;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeDynaClass;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeGetException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.ProxyValidationModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.ValidationTimingLogger;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.EntityRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.XmlModelNodeImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.jxpath.JXPathUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.jxpath.YangStep;
import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.annotations.VisibleForTesting;

public class DSExpressionValidator {

    private static final String EMPTY_STRING = "";
	protected SchemaRegistry m_schemaRegistry;
    protected ModelNodeHelperRegistry m_modelNodeHelperRegistry;
    protected SubSystemRegistry m_subSystemRegistry;

    private final static AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(DSExpressionValidator.class, LogAppNames.NETCONF_STACK);
    
    private Map<Class<?>, DSExpressionValidator> m_validators = new HashMap<Class<?>,DSExpressionValidator>();
    
    protected void logDebug(String message, Object...objects) {
        Boolean isDebugEnabled = (Boolean) RequestScope.getCurrentScope().getFromCache(DSExpressionValidator.class.getName());
        if (isDebugEnabled == null) {
            isDebugEnabled = LOGGER.isDebugEnabled();
            RequestScope.getCurrentScope().putInCache(DSExpressionValidator.class.getName(), isDebugEnabled);
        }
        if (isDebugEnabled) {
            LOGGER.debug(message, objects);
        }
    }
    
	protected boolean isLocationPathWithSteps(Expression expression) {
		return DataStoreValidationUtil.isLocationPath(expression) && ((LocationPath) expression).getSteps() != null
				&& ((LocationPath) expression).getSteps().length > 0;
	}
    
    protected boolean hasParentSteps(Expression expression) {
        if (expression instanceof CoreOperation) {
            return hasParentSteps((CoreOperation) expression);
        } else if (expression instanceof LocationPath) {
            return hasParentSteps((LocationPath) expression);
        } else {
            logDebug("{} expression is not of type LocationPath or CoreOperation. " + "Evaluating false for hasParentSteps()",
                    expression);
        }
        return false;
    }

    protected boolean hasParentSteps(CoreOperation operation) {
        // check only the first location path in the expression to see if it is a parent step
        for (Expression exp : operation.getArguments()) {
            if (exp instanceof LocationPath) {
                return hasParentSteps((LocationPath) exp);
            } else if (exp instanceof CoreOperation) {
                return hasParentSteps((CoreOperation) exp);
            } else {
                logDebug("{} is not a location Path or a core operation to check for parent steps", operation);
            }
        }
        return false;
    }

    protected boolean isAbsolutePath(Expression expression) {
        if (expression instanceof LocationPath && ((LocationPath) expression).isAbsolute()) {
            return true;
        } else if (expression instanceof CoreOperation) {
            CoreOperation operation = (CoreOperation) expression;
            if (operation.getArguments().length > 2) {
                return false;
            }

            if (isAbsolutePath(operation.getArguments()[0]) && operation.getArguments()[1] instanceof Constant) {
                return true;
            }

        }
        return false;
    }

    protected boolean hasParentSteps(LocationPath locationPath) {
        Step[] steps = locationPath.getSteps();
        if (steps.length > 0) {
            Step firstStep = steps[0];
            if (firstStep.getAxis() == Compiler.AXIS_PARENT) {
                return true;
            }
        }
        return false;
    }

    protected ModelNode getContextNode(ModelNode modelNode) {

        while (modelNode.getParent() != null) {
            modelNode = modelNode.getParent();
        }
        return modelNode;
    }

    protected boolean isSingleKeyCurrent(Step expression) {
        if (expression.toString().contains(DataStoreValidationUtil.CURRENT_PARENT_SINGLE_KEY) || expression.toString().contains(DataStoreValidationUtil.CURRENT_SINGLE_KEY)) {
            return true;
        }
        return false;
    }
    
    @VisibleForTesting
    Expression removePrefixes(Expression xPath) {
        Expression returnValue = xPath;
        Set<String> prefixes = new HashSet<String>();
        if (xPath instanceof LocationPath) {
            Step[] steps = ((LocationPath) xPath).getSteps();            
            addPrefixes(prefixes, steps);
            String xPathString = xPath.toString();
            for (String prefix : prefixes) {
                xPathString = xPathString.replace(prefix + DataStoreValidationUtil.COLON, EMPTY_STRING);
            }
            returnValue = JXPathUtils.getExpression(xPathString);
        }
        return returnValue;
    }

    private void addPrefixes(Set<String> prefixes, Step[] steps) {
        for (int i = 0; i < steps.length; i++) {
            Step step = steps[i];
            if (step.getNodeTest() instanceof NodeNameTest) {
                NodeNameTest node = (NodeNameTest) step.getNodeTest();
                String prefix = node.getNodeName().getPrefix();
                if (prefix != null && !prefix.isEmpty()) {
                    prefixes.add(prefix);
                }
                Expression[] predicates = step.getPredicates();
                for(Expression predicate : predicates) {
                    if (predicate instanceof LocationPath) {
                        addPrefixes(prefixes, ((LocationPath) predicate).getSteps());
                    } else if(predicate instanceof CoreOperation){
                        Expression[] args = ((CoreOperation) predicate).getArguments();
                        for(Expression arg : args){
                            if (arg instanceof LocationPath) {
                                addPrefixes(prefixes, ((LocationPath) arg).getSteps());
                            } 
                        }
                    }
                }
            }
        }
    }

    protected Expression evaluateCurrent(ExtensionFunction expression, DynaBean contextBean, Object contextModelNode, String prefix, String leafName) {
        Expression[] childArguments = expression.getArguments();
        if (childArguments == null || childArguments.length == 0) {
            if (contextModelNode instanceof ModelNodeDynaBean && DataStoreValidationUtil.isReadable((DynaBean) contextModelNode, leafName)) {
                Object value = ((DynaBean)contextModelNode).get(leafName);
                return new Constant(value.toString());
            } else if (contextModelNode instanceof DynaBean) {
                Object value = ModelNodeDynaClass.current((DynaBean) contextModelNode);
				if (value == null) {
					DynaBean dynaBean = (DynaBean) contextModelNode;
					ModelNode modelNode = null;
					if (DataStoreValidationUtil.isReadable(dynaBean, ModelNodeWithAttributes.LEAF_VALUE)) {
						// indicates this is a leafBean and we need to get the
						// parent modelNode
						modelNode = (ModelNode) dynaBean.get(ModelNodeWithAttributes.PARENT_MODELNODE);
					} else {
						modelNode = (ModelNode) dynaBean.get(ModelNodeWithAttributes.MODEL_NODE);
					}
			        SchemaRegistry registry = SchemaRegistryUtil.getSchemaRegistry(modelNode, m_schemaRegistry);
					ModelNodeId errorId = new ModelNodeId(modelNode.getModelNodeId());
					String namespace = modelNode.getQName().getNamespace().toString();
					errorId.addRdn(leafName, namespace, EMPTY_STRING);
					throw DataStoreValidationErrors.getMissingDataException(DataStoreValidationUtil.MISSING_MANDATORY_NODE,
							errorId.xPathString(registry), errorId.xPathStringNsByPrefix(registry));
				} else {
					return new Constant(value.toString());
				}
            } else if (contextModelNode instanceof ModelNodeWithAttributes){
                DynaBean bean = (DynaBean) ((ModelNode)contextModelNode).getValue();
                if (DataStoreValidationUtil.isReadable(bean, leafName)) {
                    Object value = bean.get(leafName).toString();
                    return new Constant(value.toString());
                }
            }
        } else {
            Expression[] newExpressions = new Expression[childArguments.length];
            boolean changed = false;
            for (int i=0;i<newExpressions.length;i++) {
                newExpressions[i] = checkForCurrentFunction(contextBean, contextModelNode, null, childArguments[i], prefix);
                if (!newExpressions[i].equals(childArguments[i])) {
                    changed = true;
                }
            }

            if (changed) {
                expression = new ExtensionFunction(expression.getFunctionName(), newExpressions);
            }
        }
        return expression;
    }
    
    protected Object evaluteCurrentOnExpressionPath(ExpressionPath expression, Object contextModelNode, QName leafQName) {
        Expression childExpression = expression.getExpression();
        if (childExpression instanceof ExtensionFunction) {
            Step[] steps = expression.getSteps();
            DynaBean contextBean = null;
            if (contextModelNode instanceof ModelNodeWithAttributes) {
                contextBean = (DynaBean) ((ModelNode)contextModelNode).getValue();
                // First step of the path has to be removed. 
                // because the ModelNode indicates we are already at current()/..
                
                Step[] newSteps = new Step[steps.length-1];
                System.arraycopy(steps, 1, newSteps, 0, steps.length-1);
                steps = newSteps;
            } else if (contextModelNode instanceof ModelNodeDynaBean){
                contextBean = (DynaBean) contextModelNode;
            } else if (contextModelNode instanceof DynaBean) {
                contextBean = (DynaBean) contextModelNode;
            }
            LocationPath path = new LocationPath(false, steps);
            Object value = checkForFunctionsAndEvaluate(path, contextBean, contextModelNode, null, leafQName);
            return value;
        }
        return null;
    	
    }
    protected Expression evaluateCurrent(ExpressionPath expression, Object contextModelNode, String prefix, QName leafQName) {
        Object value = evaluteCurrentOnExpressionPath(expression, contextModelNode, leafQName);
        return convertToExpression(expression, value);
    }
    
	private Expression convertToExpression(ExpressionPath expression, Object evaluavatedValue) {
		/**
		 * don't return the expression path with current() if value is null (or) collection
		 * example, expression: current()/../xponinfra:channel-pair-ref
		 * 
		 * After evaluating above expression, 
		 * 	if evaluavatedValue is null, then we should return empty-string (value does not exists in request/DS)
		 *  if evaluavatedValue is collection, then we can get the actual value from GenericConfigAttribute
		 */
		if (evaluavatedValue == null) {
			return new Constant(EMPTY_STRING);
		}
		if (evaluavatedValue instanceof Collection) {
			Object object = ((Collection) evaluavatedValue).iterator().next();
			if (object instanceof GenericConfigAttribute) {
				GenericConfigAttribute attribute = (GenericConfigAttribute) object;
				if (attribute != null && attribute.getStringValue() != null) {
					return new Constant(attribute.getStringValue());
				}
			}
			return expression;
		} else {
			return new Constant(evaluavatedValue.toString());
		}
	}

    /**
     * This is to take care of xpath which has the list[child] specified in the path. This
     * actually implies it is list[child = current()]
     * @param expression
     * @return
     */
    protected Expression addCurrentToSingleKeyList(Expression expression) {
        if (DataStoreValidationUtil.isLocationPath(expression)) {
            LocationPath locationPath = (LocationPath) expression;
            Step[] steps = locationPath.getSteps();
            Step[] newSteps = new Step[steps.length];
            boolean changed = false;
            for (int index = 0; index < steps.length; index++) {
                if (steps[index].getPredicates().length == 1 && DataStoreValidationUtil.isLocationPath(steps[index].getPredicates()[0])) {
                    LocationPath childPath = (LocationPath) steps[index].getPredicates()[0];
                    if (childPath.getSteps().length == 1)  {
                        Expression[] newExpressions = new Expression[1];
                        newExpressions[0] = JXPathUtils.getExpression(childPath.toString() + "=" + DataStoreValidationUtil.CURRENT_PATTERN);
                        Step oldStep = steps[index];
                        if (oldStep.getNodeTest() instanceof NodeNameTest) {
                            NodeNameTest node = (NodeNameTest) oldStep.getNodeTest();
                            YangStep newStep = new YangStep(node.getNodeName(), node.getNamespaceURI(), newExpressions);
                            newSteps[index] = newStep;
                            changed = true;
                        } else {
                            newSteps[index] = steps[index];
                        }
                    } else {
                        newSteps[index] = steps[index];
                    }
                } else {
                    newSteps[index] = steps[index];
                }
            }

            if (changed) {
                return new LocationPath(locationPath.isAbsolute(), newSteps);
            }
        } else if (DataStoreValidationUtil.isCoreOperation(expression)) {
            CoreOperation operation = (CoreOperation) expression;
            Expression[] expressions = operation.getArguments();
            Expression[] newExpressions = new Expression[expressions.length];
            boolean changed = false;
            for (int index = 0; index<expressions.length;index++) {
                newExpressions[index] = addCurrentToSingleKeyList(expressions[index]);
                if (!newExpressions[index].equals(expressions[index])) {
                    changed =true;
                }
            }
            if (changed) {
                return JXPathUtils.getCoreOperation(operation, newExpressions);
            }
        } else if (DataStoreValidationUtil.isFunction(expression)) {
            CoreFunction function = (CoreFunction) expression;
            Expression[] expressions = function.getArguments();
            Expression[] newExpressions = new Expression[expressions.length];
            boolean changed = false;
            for (int index = 0; index<expressions.length;index++) {
                newExpressions[index] = addCurrentToSingleKeyList(expressions[index]);
                if (!newExpressions[index].equals(expressions[index])) {
                    changed =true;
                }
            }
            if (changed) {
                return JXPathUtils.getCoreFunction(function.getFunctionCode(), newExpressions);
            }
        }

        return expression;
    }
    /**
     * This method is setting the extension function on DynaBean class.
     */
    protected Expression setFunctionOnContext(DynaBean contextBean, Object contextModelNode, Expression xPath, String prefix, QName leafQName) {
        
        if (xPath.toString().contains(DataStoreValidationUtil.CURRENT_PATTERN)) {
              Expression xpression = xPath;
              if (xpression instanceof LocationPath) {
                  Step[] originalSteps = ((LocationPath) xpression).getSteps();
                  Step[] newSteps = new Step[originalSteps.length];
                  for (int i=0;i<originalSteps.length;i++) {
                      boolean stepChanged = false;
                      Expression[] predicates = originalSteps[i].getPredicates();
                      Expression[] newPredicates = new Expression[predicates.length];
                      for (int j=0;j<predicates.length;j++) {
                          if (predicates[j].toString().contains(DataStoreValidationUtil.CURRENT_PATTERN)) {
                            if (predicates[j] instanceof CoreOperation) {
                                Expression childExpression[] = ((Operation) predicates[j]).getArguments();
                                Expression newChildExpression[] = new Expression[childExpression.length];
                                for (int k = 0; k < childExpression.length; k++) {
                                    if (childExpression[k] instanceof ExtensionFunction) {
                                        String leafName = childExpression[k-1].toString();
                                        newChildExpression[k] = evaluateCurrent((ExtensionFunction) childExpression[k], contextBean,
                                                contextModelNode, prefix, leafName);
                                    } else if (childExpression[k] instanceof ExpressionPath) {
                                        newChildExpression[k] = evaluateCurrent((ExpressionPath) childExpression[k], contextModelNode,
                                                prefix, leafQName);
									} else if (childExpression[k] instanceof CoreOperation) {
										newChildExpression[k] = setFunctionOnContext(contextBean, contextModelNode,
												(CoreOperation) childExpression[k], prefix, leafQName);
									} else {
										newChildExpression[k] = childExpression[k];
									}
                                }
                                newPredicates[j] = JXPathUtils.getCoreOperation((CoreOperation) predicates[j], newChildExpression);
                                stepChanged = true;
                            }
                          } else {
                              newPredicates[j] = predicates[j];
                          }
                      }
                      
                      if (stepChanged) {
                          NodeTest nodeTest = originalSteps[i].getNodeTest();
                          if (nodeTest instanceof NodeNameTest) {
                              NodeNameTest nameNode = (NodeNameTest) nodeTest;
                              newSteps[i] = new YangStep(nameNode.getNodeName(), nameNode.getNamespaceURI(), newPredicates);
                          } else {
                              newSteps[i] = originalSteps[i];
                          }
                      } else {
                          newSteps[i] = originalSteps[i];
                      }
                  }
                  return new LocationPath(((LocationPath)xpression).isAbsolute(), newSteps);
              } else if (xpression instanceof ExpressionPath) {
                  return evaluateCurrent((ExpressionPath)xpression, contextModelNode, prefix, leafQName);
              } else if (xpression instanceof ExtensionFunction) {
                  return evaluateCurrent((ExtensionFunction) xpression, contextBean, contextModelNode, prefix, leafQName.getLocalName());
              } else if (xpression instanceof CoreOperation) {
                  Expression[] newExpressions = new Expression[((CoreOperation) xpression).getArguments().length];
                  Expression[] expressions = ((CoreOperation) xpression).getArguments();
                  int index = 0;
                  for (Expression expression:expressions) {
                      newExpressions[index++] = setFunctionOnContext(contextBean, contextModelNode, expression, prefix, leafQName);
                  }
                  return JXPathUtils.getCoreOperation((CoreOperation) xpression, newExpressions);
              } else if (xpression instanceof CoreFunction) {
                  Expression[] expressions = ((CoreFunction) xpression).getArguments();
                  Expression[] newExpressions = new Expression[expressions.length];
                  int index = 0;
                  for (Expression expression:expressions) {
                     newExpressions[index++] = setFunctionOnContext(contextBean, contextModelNode, expression, prefix, leafQName);
                  }
                  return JXPathUtils.getCoreFunction( ((CoreFunction) xpression).getFunctionCode(), newExpressions);
              }
        }
        return xPath;
    }

    protected boolean isStepSameAsModelNode(String targetNodeNs, String targetNodeLocalName, Step step, SchemaRegistry schemaRegistry) {
        /**
         * Given a JXPath Path Step and a ModelNode, this method determines if the Step represents the same ModelNode.
         * 
         * Verify if the step Name and the ModelNode QName.getLocalName() are same, then
         * 
         * 1) If the Namespace is available, we need to compare the same --> return true if same 
         * 2) If only NS prefix is available we need to ensure they match --> return true if same 
         * 3) If no NS or its prefix, we return the targetNode
         * 
         */
        boolean returnValue = false;
        NodeTest nodeTest = step.getNodeTest();
        if (schemaRegistry == null) {
        	schemaRegistry = m_schemaRegistry;
        }
        if (nodeTest instanceof NodeNameTest) {
            NodeNameTest node = (NodeNameTest) nodeTest;
            String ns = node.getNamespaceURI();
            String name = node.getNodeName().getName();
            if (targetNodeLocalName.equals(name)) {
                // Both the local Names match.

                if (ns != null && ns.equals(targetNodeNs)) {
                    returnValue = true;
                } else {
                    String prefix = node.getNodeName().getPrefix();
                    if (prefix != null) {
                        String modelNodeNS = targetNodeNs;
                        String modelNodePrefix = schemaRegistry.getPrefix(modelNodeNS);
                        if (prefix.equals(modelNodePrefix)) {
                            returnValue = true;
                        }
                    } else {
                        // well we have only localName to match.
                        // no NS or prefix in xPath.
                        returnValue = true;
                    }

                }
            }
        }
        return returnValue;
    }

    protected ModelNode getRootNodeFor(Expression expression) {
        LocationPath locationPath = null;
        if (expression instanceof LocationPath) {
            /** the first step of an locationPath should match with 
            *   any of the Root Model node.
            *   Because all absolute xPath must have one of the root nodes
            *   as the first step
            */   
            locationPath = (LocationPath) expression;
            Step[] steps = locationPath.getSteps();

            List<Pair<String, String>> rootNodesNsPairs = DataStoreValidationUtil.getValidationContext().getRootNodeLocalNameNsPairs();
            for (Pair<String, String> pair : rootNodesNsPairs) {
                if (isStepSameAsModelNode(pair.getFirst(), pair.getSecond(), steps[0], null)) {
                    List<ModelNode> rootNodesOfType = DataStoreValidationUtil.getValidationContext().getRootNodesOfType(pair.getFirst(), pair.getSecond());
                    if (!rootNodesOfType.isEmpty()) {
                        return rootNodesOfType.get(0);
                    }
                }
            }
        }
        return null;
    }


    protected Expression checkForSingleKeyCurrent(Expression expression, DynaBean dynaBean, Object currentNode) {
        String xPath = expression.toString();
        if (!xPath.contains(DataStoreValidationUtil.CURRENT_PATTERN)) {
            return expression;
        }
        if (DataStoreValidationUtil.isLocationPath(expression)) {
            return checkForSingleKeyCurrent((LocationPath)expression, dynaBean);
        } else if (expression instanceof CoreFunction) {
            Expression[] childExpressions = new Expression[((CoreFunction) expression).getArgumentCount()];
            Expression[] expressions = ((CoreFunction) expression).getArguments();
            int index = 0;
            for (Expression childExpression:expressions) {
                childExpressions[index++] = checkForSingleKeyCurrent(childExpression, dynaBean, currentNode);
            }
            return JXPathUtils.getCoreFunction(((CoreFunction)expression).getFunctionCode(), childExpressions);
        } else if (DataStoreValidationUtil.isCoreOperation(expression)) {
            Expression[] childExpressions = new Expression[((CoreOperation) expression).getArguments().length];
            Expression[] expressions = ((CoreOperation) expression).getArguments();
            int index = 0;
            for (Expression childExpression:expressions) {
                childExpressions[index++] = (checkForSingleKeyCurrent(childExpression, dynaBean, currentNode));
            }
            return JXPathUtils.getCoreOperation((CoreOperation) expression, childExpressions);
        } else if (DataStoreValidationUtil.isExtensionFunction(expression)) {
            return evaluateCurrent((ExtensionFunction) expression, dynaBean, currentNode, null, null);
        } else if (DataStoreValidationUtil.isExpressionPath(expression)) {
            return checkForSingleKeyCurrentInExpressionPath((ExpressionPath) expression, dynaBean, null);
        } 
        return expression;
    }
    
    private Expression checkForSingleKeyCurrentInExpressionPath(ExpressionPath expressionPath, DynaBean dynaBean,
			QName leafName) {
		Step[] steps = expressionPath.getSteps();
		LocationPath nextPath = null;
		if (expressionPath.toString().startsWith(DataStoreValidationUtil.CURRENT_PATTERN)) {
			nextPath = new LocationPath(false, steps);
			Object value = checkForFunctionsAndEvaluate(nextPath, dynaBean, dynaBean, null, leafName);
			return convertToExpression(expressionPath, value);
		}
		return evaluateCurrent((ExpressionPath) expressionPath, dynaBean, null, null);
	}
    
	/**
     * For a yang ListSchemaNode which has single key, it can be referenced in xPath as 
     * /listSchemaNode[key = 'value'] or  /listSchemaNode[current()]
     * 
     * When [current()] is used, xPath basically indicates the single key value has to be matched with the node on which current() is
     * applied. But JXPath, scans DynaBean in the current implementation and looks for name/value pairs.
     * 
     * It has no unique way to distinguish between list key fields and ordinary list attributes.
     * 
     * This method will covert the /listSchemaNode[current()] to /listSchemaNode[key = current()]
     */
    @SuppressWarnings("rawtypes")
    protected LocationPath checkForSingleKeyCurrent(LocationPath locationPath, DynaBean dynaBean) {
        LocationPath newPath = locationPath;
        Step[] oldSteps = locationPath.getSteps();
        DynaBean contextBean = dynaBean;
        Step[] newSteps = new Step[oldSteps.length];
        SchemaPath currentPath = null;
        int index = 0;
        if (locationPath.toString().contains(DataStoreValidationUtil.CURRENT_SINGLE_KEY) || locationPath.toString().contains(DataStoreValidationUtil.CURRENT_PARENT_SINGLE_KEY)) {
            if (locationPath.isAbsolute()) {
                ModelNode rootNode = getRootNodeFor(locationPath);
                contextBean = (DynaBean) rootNode.getValue();
                newSteps[0] = oldSteps[0];
                index = 1;
            }
            for (int i = index; i < oldSteps.length; i++) {
                Object value = null;
                if (contextBean != null && DataStoreValidationUtil.isReadable(contextBean, DataStoreValidationUtil.getLocalName(oldSteps[i]))) {
                    value = contextBean.get(DataStoreValidationUtil.getLocalName(oldSteps[i]));
                } else if (contextBean != null && oldSteps[i].getAxis() == Compiler.AXIS_PARENT) {
                    value = contextBean.get(ModelNodeWithAttributes.PARENT);
                }

                if (value instanceof DynaBean) {
                    contextBean = (DynaBean) value;
                } else if (value instanceof Collection) {
                    Object object = ((Collection)value).iterator().next();
                    if (object instanceof DynaBean) {
                        contextBean = (DynaBean) object;
                    } else {
                        // last leaf/leaf list. no point going further
                        object = null;
                    }
                } else if (value != null && oldSteps.length - 1 == i) {
                    // we are at the last step
                    contextBean = null;
                    if (newSteps[0] != null) {
                        // most likely the last leaf/leaf-list in the xPath
                        newSteps[i] = oldSteps[i];
                    }
                    break;
                } else if (value != null && currentPath == null) {
                    // well we chose a wrong list node, which does not have further children down.
                    // but we can still traverse the schema.
                	ModelNode modelNode = (ModelNode) contextBean.get(ModelNodeWithAttributes.MODEL_NODE);
			        SchemaRegistry registry = SchemaRegistryUtil.getSchemaRegistry(modelNode, m_schemaRegistry);
                    SchemaPath previousPath = modelNode.getModelNodeSchemaPath();
                    String name = ModelNodeDynaBeanFactory.getModelNodeAttributeName(DataStoreValidationUtil.getLocalName(oldSteps[i]));
                    currentPath = DataStoreValidationUtil.getChildPath(registry, previousPath, name);
                    contextBean = null;
                } else if (currentPath != null) {
                    String name = ModelNodeDynaBeanFactory.getModelNodeAttributeName(DataStoreValidationUtil.getLocalName(oldSteps[i]));
                    currentPath = DataStoreValidationUtil.getChildPath(m_schemaRegistry, currentPath, name);
                } else if (value == null && currentPath == null && contextBean != null && DataStoreValidationUtil.isReadable(contextBean, ModelNodeWithAttributes.MODEL_NODE)) {
                	ModelNode modelNode = (ModelNode) contextBean.get(ModelNodeWithAttributes.MODEL_NODE);
			        SchemaRegistry registry = SchemaRegistryUtil.getSchemaRegistry(modelNode, m_schemaRegistry);
                    String stepName = ModelNodeDynaBeanFactory.getModelNodeAttributeName(DataStoreValidationUtil.getLocalName(oldSteps[i]));
                    currentPath = DataStoreValidationUtil.getChildPath(registry, modelNode.getModelNodeSchemaPath(), stepName);
                    contextBean = null;
                    if (currentPath == null) {
                        newSteps = null;
                        break;
                    }
                } else {
                    logDebug(null, " we had a wrong contextNode to start with for path - {} and bean {}", locationPath, dynaBean);
                    newSteps = null;
                    break;
                }

                if (isSingleKeyCurrent(oldSteps[i])) {
                    // Since [current()] is applicable only on List, the DynaBean will have a modelNode and it is
                    // expected to have only one key
                    DataSchemaNode schemaNode = null;
                    if (contextBean != null) {
                        ModelNode modelNode = (ModelNode) contextBean.get(ModelNodeWithAttributes.MODEL_NODE);
                        SchemaPath schemaPath = modelNode.getModelNodeSchemaPath();
                        schemaNode = (ListSchemaNode) modelNode.getSchemaRegistry().getDataSchemaNode(schemaPath);
                    } else if (currentPath != null) {
                        schemaNode = m_schemaRegistry.getDataSchemaNode(currentPath);
                    } else {
                        LOGGER.warn(" neither contextBean/CurrentPath can be null for {} and bean -", locationPath, dynaBean);
                        newSteps = null;
                        break;
                    }

                    if (schemaNode == null) {
                        logDebug("we traversed a wrong path for {} and bean ", locationPath, dynaBean);
                        newSteps = null;
                        break;
                    }
                    
                    QName keyName = null;
                    if (schemaNode instanceof ListSchemaNode) {
                        keyName = ((ListSchemaNode)schemaNode).getKeyDefinition().get(0);
                    }

                    // build key = current() as the new predicate
                    Expression[] newExpression = new Expression[1];
                    if (keyName != null) {
                        String dynaBeanAlignedName = ModelNodeDynaBeanFactory.getDynaBeanAttributeName(keyName.getLocalName());
                        StringBuilder newPredicate = new StringBuilder().append(dynaBeanAlignedName).append("=")
                                .append(oldSteps[i].getPredicates()[0].toString());
                        newExpression[0] = JXPathUtils.getExpression(newPredicate.toString());
                    }

                    // build the new Step
                    if (oldSteps[i].getNodeTest() instanceof NodeNameTest) {
                        NodeNameTest node = (NodeNameTest) oldSteps[i].getNodeTest();
                        Step newStep = new YangStep(node.getNodeName(), node.getNamespaceURI(), newExpression);
                        newSteps[i] = newStep;
                    }
                } else {
                    newSteps[i] = oldSteps[i];
                }
            }
        }

        if (newSteps != null && newSteps[0] != null) {
            newPath = new LocationPath(locationPath.isAbsolute(), newSteps);
        }

        return newPath;
    }
    
    protected Set<Object> evaluateXpath(JXPathContext context, String xPath) {
        Set<Object> returnValue = null;
        Iterator<?> leafRefs = context.iterate(xPath);
        while (leafRefs.hasNext()) {
            if (returnValue == null) {
                returnValue = new HashSet<Object>();
            }
            returnValue.add(leafRefs.next());
        }
        return returnValue;
    }

    protected boolean isAnyObjectNull(Object[] objects) {   
        for (Object object : objects) {
            if (object == null) {
                return true;
            }
        }
        return false;
    }

    protected boolean isAnyLocationPathFalse(CoreOperation operation) {
        for (Expression expression : operation.getArguments()) {
            if (expression.toString().equalsIgnoreCase("'false'")) {
                return true;
            }
        }
        return false;
    }

    protected boolean isAnyLocationPathTrue(CoreOperation operation) {
        for (Expression expression : operation.getArguments()) {
            if (expression.toString().equalsIgnoreCase("'true'")) {
                return true;
            }
        }
        return false;
    }

    protected boolean isAllArgumentsConstants(CoreOperation operation) {
        for (Expression expression : operation.getArguments()) {
            if (!(expression instanceof Constant)) {
                return false;
            }
        }
        return true;
    }

    protected boolean isBoolean(String value) {
    	return DataStoreValidationUtil.isBoolean(value);
    }

    protected Object getDefaults(DataSchemaNode node) {
        if (node instanceof LeafSchemaNode) {
            Optional<? extends Object> optDefaultValue = ((LeafSchemaNode) node).getType().getDefaultValue();
            return (optDefaultValue.isPresent() ? optDefaultValue.get() : null);
        }
        return null;
    }

    protected boolean isAnyExpressionIdentity(List<Expression> expressions, QName leafQName) {
        if (leafQName != null) {
            /**
             * An identity is always returned in the form of prefix:value in the current stack. 
             * the below validates if any of the values in the evaluated expressions is of the form prefix:value. 
             * 
             * Here the prefix is matched only with the leafQName ns prefix  
             * eg: Assume below is in namespace prefix=validation
             * 
             *      container identity-validation {
             *
             *          leaf leaf1 {
             *              type identityref {
             *                  base some-identity;
             *              }
             *          }
             *
             *          leaf leaf2 {
             *              when "../leaf1 = 'identity1'";
             *              type int8;
             *          }
             *      }
             *
             *  Here ../leaf1 will return a value validation:some-identity
             *  
             *  Now if this is the case we will return true. 
             *  
             *  Case2: What if the identity is in some other module? In that case, the target element must also be prefixed with that ns. 
             *  So when "../leaf1 = 'prefix:identity1'" is the right condition. In that case, the prefix is already here. 
             */
            String nsPrefix = m_schemaRegistry.getPrefix(leafQName.getNamespace().toString());
            for (Expression exp : expressions) {
                String constant = exp.compute(null).toString();
                if (constant.contains(DataStoreValidationUtil.COLON) && constant.split(DataStoreValidationUtil.COLON)[0].equals(nsPrefix)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected Object computeExpression(CoreOperation operation, List<Expression> expressions) {
        Object value;
        CoreOperation newOperation = JXPathUtils.getCoreOperation(operation, expressions.toArray(new Expression[0]));
        if (newOperation instanceof CoreOperationAnd && isAllArgumentsConstants(newOperation) && isAnyLocationPathFalse(newOperation)) {
            value = Boolean.FALSE;
        } else if (newOperation instanceof CoreOperationOr && isAllArgumentsConstants(newOperation)
                && isAnyLocationPathTrue(newOperation)) {
            value = Boolean.TRUE;
        } else if (newOperation instanceof CoreOperationAnd && isAllArgumentsConstants(newOperation)
                && !isAnyLocationPathFalse(newOperation)) {
            value = Boolean.TRUE;
        } else if (newOperation instanceof CoreOperationOr && isAllArgumentsConstants(newOperation)
                && !isAnyLocationPathTrue(newOperation)) {
            value = Boolean.FALSE;
        } else {
            value = newOperation.computeValue(null);
        }

        return value;
    }


    protected Object validatePathAtContextBean(Expression xPathCondition, DynaBean contextBean,
            Object currentContextNode, String leafRefValue, QName leafQName) {
        Object value = null;
        // if we have the first step of path and context bean at same level, then we need to exclude the parent path
        // and evalute
        //
        // eg> xpath = a/b/c/d and context bean is at 'a'
        //    excluded parent path is b/c/d
        
        Expression excludeParentPath = null;
        Expression newExpression = xPathCondition;
        if (newExpression instanceof LocationPath) {
            excludeParentPath = DataStoreValidationUtil.excludeFirstStep(((LocationPath) newExpression));
        } else if (newExpression instanceof CoreOperation) {
            List<Expression> expressions = Arrays.asList(((CoreOperation) newExpression).getArguments());
            LocationPath newLocationPath = DataStoreValidationUtil.excludeFirstStep((LocationPath) expressions.get(0));
            Expression[] newExpressions = new Expression[expressions.size()];
            int index = 0;
            newExpressions[index++] = newLocationPath;
            for (int i = 1; i < expressions.size(); i++) {
                newExpressions[index++] = expressions.get(i);
            }
            excludeParentPath = JXPathUtils.getCoreOperation((CoreOperation) newExpression, newExpressions);

        } else {
            /**
             * If we are here, indicates a clause not supported or a serious programming error. 
             * God forbid, if we see this exception
             */
            throw new RuntimeException("Not a valid Expression type for validation a locationPath:" + newExpression);
        }

        if (!excludeParentPath.toString().isEmpty()) {
            /**
             * If after removing the first path, there is no longer a path to travel.... 
             * No point in proceeding further. 
             */
            value = evaluate(excludeParentPath, contextBean, currentContextNode, leafRefValue, leafQName);
        } else {
            logDebug(null, "{} is already at parent of context {}", xPathCondition, ModelNodeDynaClass.getContextBeanName(contextBean));
        }
        return value;
    }

    protected Object validateAbsolutePath(Expression xPathCondition, DynaBean contextBean,
            Object currentContextNode, String leafRefValue, QName leafQName) {
        Object returnValue = null;
        // When we come here, we know we have a xpath and a constant or just an xpath.
        Expression ex = xPathCondition;
        LocationPath xPath = null;
        Constant constant = null;
        if (ex instanceof LocationPath) {
            xPath = (LocationPath) ex;
        } else {
            xPath = (LocationPath) ((CoreOperation) ex).getArguments()[0];
            constant = (Constant) ((CoreOperation) ex).getArguments()[1];
        }

        DynaBean rootBean = contextBean;
        
        boolean schemaMountRoot = false;
        if (SchemaRegistryUtil.isMountPointEnabled() && getRootNodeFor(xPathCondition) == null) {
        	// we are likely in a absolute path of a mount registry. 
        	schemaMountRoot = true;
        }
        
        while (DataStoreValidationUtil.isReadable(rootBean, ModelNodeWithAttributes.PARENT)) {
        	rootBean = (DynaBean) rootBean.get(ModelNodeWithAttributes.PARENT);
        	if (schemaMountRoot) {
        		ModelNode modelNode = (ModelNode) rootBean.get(ModelNodeWithAttributes.MODEL_NODE);
        		SchemaRegistry mountRegistry = modelNode.getSchemaRegistry();
        		if (modelNode.isSchemaMountImmediateChild()) {
        			if (isStepSameAsModelNode((String) rootBean.get(ModelNodeWithAttributes.NAMESPACE),
        					rootBean.getDynaClass().getName(), xPath.getSteps()[0], mountRegistry)) {
        				break;
        			}
        		} else if (modelNode.hasSchemaMount()) { 
        			ModelNodeHelperRegistry modelRegistry = modelNode.getMountModelNodeHelperRegistry();
        			SchemaPath modelNodeSchemaPath = modelNode.getModelNodeSchemaPath();
        			Map<QName, ChildContainerHelper> containerHelpers = modelRegistry.getChildContainerHelpers(modelNodeSchemaPath);
        			Pair<DynaBean, Boolean> childBeanNodePair = retrieveMatchingChildNode(xPath, modelNode, containerHelpers);
        			if(childBeanNodePair != null && childBeanNodePair.getSecond()){
        				rootBean = childBeanNodePair.getFirst();
        				break;
        			}
        		}
        	}
        }
        
        xPath = DataStoreValidationUtil.excludeFirstStep(xPath);
        if (xPath.getSteps().length > 0) {
            Expression xPathForValidation = removePrefixes(xPath);
            JXPathContext context = getContext(rootBean);
            if (currentContextNode == null && xPath.toString().contains(DataStoreValidationUtil.CURRENT_PATTERN)) {
                // indicates the node is not created yet.
                returnValue = true;
            }

            Set<Object> lhs = evaluateXpath(context, xPathForValidation.toString());
            if (lhs != null) {
                if (constant == null) {
                    returnValue = lhs;
                } else {
                    List<Expression> expressions = new ArrayList<Expression>(2);
                    for (Object value : lhs) {
                        if (value instanceof ConfigLeafAttribute) {
                            value = ((ConfigLeafAttribute) value).getStringValue();
                        }
                        expressions.add(JXPathUtils.getConstantExpression(value));
                        expressions.add(constant);
                        returnValue = computeExpression((CoreOperation) ex, expressions);
                        if (returnValue != null && returnValue instanceof Boolean && ((Boolean) returnValue)) {
                            break;
                        }
                        expressions.clear();
                    }
                }

            }
        }
        return returnValue;
    }

    private Pair<DynaBean, Boolean> retrieveMatchingChildNode(LocationPath xPath, ModelNode modelNode,
    		Map<QName, ChildContainerHelper> containerHelpers) {
    	for (ChildContainerHelper helper : containerHelpers.values()) {
    		try {
    			ModelNode childNode = helper.getValue(modelNode);
    			if(childNode != null) {
    				DynaBean childBean = (DynaBean)childNode.getValue();
    				if (isStepSameAsModelNode((String) childBean.get(ModelNodeWithAttributes.NAMESPACE),
    						childBean.getDynaClass().getName(), xPath.getSteps()[0], childNode.getSchemaRegistry())) {
    					return new Pair<DynaBean, Boolean>(childBean, true);
    				}
    			}
    		} catch (ModelNodeGetException e) {
    			throw new RuntimeException("Error while retrieving values from container helper for modelnode" +modelNode);
    		}
    	}		
    	return null;
    }

    protected boolean isValidationDone(CoreOperation operation, DynaBean contextBean) {
        if (operation.getArguments()[0] instanceof LocationPath
                && ((LocationPath) (operation.getArguments()[0])).getSteps()[0].getAxis() != Compiler.AXIS_PARENT
                && DataStoreValidationUtil.isReadable(contextBean, ModelNodeWithAttributes.PARENT)) {
            // indicates the contextBean has a parent and the current xpath is absolute.
            // We still have some distance to cover
            return false;
        } else if (operation.getArguments()[0] instanceof LocationPath
                && DataStoreValidationUtil.isReadable(contextBean, operation.getArguments()[0].toString())) {
            /**
             * Indicates we do not want to validate any further. 
             * Why? Because the property is already in the contextBean and the evaluation has returned a false
             */
            return true;
        } else {
            logDebug("{} does not start with ../, first step is not in current context {}", operation,
                    ModelNodeDynaClass.getContextBeanName(contextBean));
        }
        return false;
    }


    protected boolean isContextBeanAtFirstStep(Expression ex, DynaBean contextBean, String leafRefValue) {
        /**
         * <device-manager>   <!-- Assume contextBean is here -->
         *  <device-holder>
         *      <device>
         *          <device-id>name</device-id>
         *      </device>
         *  </device-holder>
         * </device-manager>
         * 
         * xpath: /device-manager/device-holder/device/name
         * 
         * 
         * We simply want to find out, if above is indeed the case - whether contextBean and start of Xpath are at same level
         */
        boolean returnValue = false;
        if (ex instanceof LocationPath && ((LocationPath)ex).isAbsolute() && !DataStoreValidationUtil.isReadable(contextBean, ModelNodeWithAttributes.PARENT)) {
            Step[] steps = ((LocationPath) ex).getSteps();
            Step firstStep = steps[0];
            if (firstStep.getNodeTest() instanceof NodeNameTest) {
                NodeNameTest node = ((NodeNameTest) firstStep.getNodeTest());
                String name = node.getNodeName().getName();
                String prefix = node.getNodeName().getPrefix();
                String beanNs = (String) contextBean.get(ModelNodeWithAttributes.NAMESPACE);
                if (prefix != null) {
                    String ns = m_schemaRegistry.getNamespaceURI(prefix);
                    if (ns.equals(beanNs) && name.equals(contextBean.getDynaClass().getName())) {
                        return true;
                    }
                } else if (name.equals(contextBean.getDynaClass().getName())) {
                    returnValue = true;
                }
            }
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("{} is neither a locationPath or the contextBean has a parent when checking if context bean {} is at first step",
                        ex, ModelNodeDynaClass.getContextBeanName(contextBean));
            }
        }
        return returnValue;
    }

    
    /**
     * Given a xpath of a leaf inside a list/leaf-list, the below method traverses through all the 
     * available such path. 
     * Eg: 
     * list a{
     *      key b;
     *      leaf b{
     *          type string;
     *      }
     * }
     * xpath /a/b --> This will fetch the list of all leaf b inside list of type a.
     * 
     * Such fetched leaf values are matched against the target value to find the right path
     */
    protected boolean validateLeafRef(JXPathContext context, String xPath, String leafRefValue) {
        return validateLeafRefExistence(context, xPath, leafRefValue);
    }
    
    private boolean validateLeafRefExistence(JXPathContext context, String xPath, String leafRefValue){
        /*
         * 1. Get Schema Path from Context and xPath
         * 
         * 2. Get Entity Class from Schema Path (EntityRegistry)
         * 
         * 3. Inspect the Class to know the mapped Property name for the leaf we're interested in.
         * 
         * 4. Invoke EntityDataStoreManager - findByMatchValue(Class, Map<String, Object> values);
         * 
         * 5. If the List is empty - return false; else return true;
         * 
         * 6. If the Step 2 is returning NULL.. then follow old code flow
         */


        /* expression : adh:device-manager/adh:device/adh:device-id
           newExpression :device-manager/device/device-id
           If firstStep is at the context Node, then remove first steps --> device/device-id
           then, Retrieve Entity Class for the first step if exists and check for the next step. the actual property exists in entity and get the matched result entries
         */
        Expression expression = JXPathUtils.getExpression(xPath);
        Expression newExpression = removePrefixes(expression);
        LocationPath path = (LocationPath) newExpression;
        Object contextBean = context.getContextBean();
        if(contextBean instanceof DynaBean){
            DynaBean dynaBean = (DynaBean)contextBean;
            ModelNode contextNode = DataStoreValidationUtil.isReadable(dynaBean, ModelNodeWithAttributes.MODEL_NODE)?(ModelNode) dynaBean.get(ModelNodeWithAttributes.MODEL_NODE):null;
            if(contextNode != null) {
                SchemaPath contextSchemaPath = contextNode.getModelNodeSchemaPath();
                Pair<SchemaPath, LocationPath> schemaPathAndLocationPathpair = getSchemaPathForFirstStepOfExpression(context, path, leafRefValue, contextSchemaPath);
                ModelNodeDataStoreManager modelNodeDsm = getModelNodeDSMFromModelNode(context);

                if(isEntityBasedDSMNode(modelNodeDsm, schemaPathAndLocationPathpair)){
                    EntityRegistry entityRegistry = modelNodeDsm.getEntityRegistry(schemaPathAndLocationPathpair.getFirst());
                    Class<?> klass = entityRegistry.getEntityClass(schemaPathAndLocationPathpair.getFirst());
                    Step[] childSteps = schemaPathAndLocationPathpair.getSecond().getSteps();
                    if(childSteps[0] != null){
                        NodeNameTest node = (NodeNameTest) childSteps[0].getNodeTest();
                        String stepName = node.getNodeName().getName();
                        Pair<String, String> yangToDBPropertyPair = checkAndGetYangPropertyExistenceInKlass(klass, stepName, schemaPathAndLocationPathpair.getFirst());
                        if(yangToDBPropertyPair != null && yangToDBPropertyPair.getFirst().equals(stepName)){
                            Map<String, Object> matchValues = new HashMap<>();
                            matchValues.put(yangToDBPropertyPair.getSecond(), leafRefValue);
                            List entries = modelNodeDsm.findByMatchValues(schemaPathAndLocationPathpair.getFirst(), matchValues);
                            if(entries != null && entries.size() >0){
                                return true;
                            }
                            return false;
                        }
                    }
                }
            }
        }
        return validateLeafRefExistenceFromModelNode(context, xPath, leafRefValue);
    }

    private boolean isEntityBasedDSMNode(ModelNodeDataStoreManager modelNodeDsm, Pair<SchemaPath, LocationPath> schemaPathAndLocationPathpair){
        if(modelNodeDsm != null && schemaPathAndLocationPathpair != null && schemaPathAndLocationPathpair.getFirst() != null){
            SchemaPath schemaPath = schemaPathAndLocationPathpair.getFirst();
            EntityRegistry entityRegistry = modelNodeDsm.getEntityRegistry(schemaPath);
            if(entityRegistry != null && entityRegistry.getEntityClass(schemaPath) != null){
                return true;
            }
        }
        return false;
    }
    
    private boolean validateLeafRefExistenceFromModelNode(JXPathContext context, String xPath, String leafRefValue) {
        Iterator<?> leafRefs = context.iterate(xPath);
        while (leafRefs.hasNext()) {
            Object value = leafRefs.next();
            Boolean returnValue = (Boolean) validateLeafRef(value, leafRefValue);
            if (returnValue) {
                return returnValue;
            }                
        }
        return false;
    }

    private Pair<String, String> checkAndGetYangPropertyExistenceInKlass(Class<?> klass, String yangProperty, SchemaPath parentPath){
        Field[] fields = klass.getDeclaredFields();
        for (Field field : fields){
            QName fieldAnnotationQName = EntityRegistryBuilder.getQName(field.getName(), field.getAnnotations(), parentPath.getLastComponent());
            if(fieldAnnotationQName.getLocalName().equals(yangProperty)){
                return new Pair<String, String>(yangProperty, field.getName());
            }
        }
        return null;
    }
    
    private Pair<SchemaPath, LocationPath> getSchemaPathForFirstStepOfExpression(JXPathContext context, LocationPath path, String leafRefValue, SchemaPath schemapath) {
        Object contextBean = context.getContextBean();
        Pair<SchemaPath, LocationPath> schemaPathAndLocationPathpair = null;
        if(contextBean != null && contextBean instanceof DynaBean) {
            DynaBean dynaBean = (DynaBean)contextBean;
            ModelNode contextNode = DataStoreValidationUtil.isReadable(dynaBean, ModelNodeWithAttributes.MODEL_NODE)?(ModelNode) dynaBean.get(ModelNodeWithAttributes.MODEL_NODE):null;
            if(contextNode != null) {
                SchemaRegistry registry = contextNode.getSchemaRegistry();
                if (isContextBeanAtFirstStep(path, dynaBean, leafRefValue)){
                    path = DataStoreValidationUtil.excludeFirstStep(path);
                    schemaPathAndLocationPathpair = getSchemaPathForFirstStep(schemapath, registry, path); 
                    if(schemaPathAndLocationPathpair != null && schemaPathAndLocationPathpair.getSecond().getSteps().length > 1){
                        return getSchemaPathForFirstStepOfExpression(context, schemaPathAndLocationPathpair.getSecond(), leafRefValue, schemaPathAndLocationPathpair.getFirst());
                    }
                    
                } else {
                    schemaPathAndLocationPathpair = getSchemaPathForFirstStep(schemapath, registry, path); 
                    if(schemaPathAndLocationPathpair != null && schemaPathAndLocationPathpair.getSecond().getSteps().length > 1){
                        return getSchemaPathForFirstStepOfExpression(context, schemaPathAndLocationPathpair.getSecond(), leafRefValue, schemaPathAndLocationPathpair.getFirst());
                    }
                }
            }
        }
        return schemaPathAndLocationPathpair;
    }

    private ModelNodeDataStoreManager getModelNodeDSMFromModelNode(JXPathContext context){
        Object contextBean = context.getContextBean();
        if(contextBean != null && contextBean instanceof DynaBean) {
            DynaBean dynaBean = (DynaBean)contextBean;
            ModelNode contextNode = DataStoreValidationUtil.isReadable(dynaBean, ModelNodeWithAttributes.MODEL_NODE)?(ModelNode) dynaBean.get(ModelNodeWithAttributes.MODEL_NODE):null;
            return contextNode == null? null: contextNode.getModelNodeDSM();
        }
        return null;
    }
    
    private Pair<SchemaPath, LocationPath> getSchemaPathForFirstStep(SchemaPath contextSchemaPath, SchemaRegistry registry,
            LocationPath path) {
        Step[] steps = path.getSteps();
        if(steps.length >0 && steps[0] != null) {
            Collection<DataSchemaNode> children = registry.getChildren(contextSchemaPath);
            for(DataSchemaNode child : children) {
                if(steps[0] != null) {
                    NodeNameTest node = (NodeNameTest) steps[0].getNodeTest();
                    String stepName = node.getNodeName().getName();
                    if(child.getQName().getLocalName().equals(stepName)){
                        path = DataStoreValidationUtil.excludeFirstStep(path);
                        return new Pair<SchemaPath, LocationPath>(child.getPath(), path);
                    }
                }
            }
        }
        return null;
    }
    
    @SuppressWarnings("rawtypes")
	protected Object validateLeafRef(Object evaluatedValue, String leafRefValue) {
    	if (leafRefValue == null) {
    		return evaluatedValue;
    	}
    	
    	if (evaluatedValue instanceof Collection) {
    		for (Object object:((Collection)evaluatedValue)) {
    			Boolean returnValue = false; 
    			returnValue = (Boolean) validateLeafRef(object, leafRefValue);
    			if (returnValue) {
    				return true;
    			}
    		}
    	}
    	
    	if (evaluatedValue instanceof DynaBean){
			// could be a leafBean
			String leafValue = DataStoreValidationUtil.isReadable(((DynaBean) evaluatedValue), ModelNodeWithAttributes.LEAF_VALUE)
					? (String) ((DynaBean) evaluatedValue).get(ModelNodeWithAttributes.LEAF_VALUE) : null;
			return leafValue.equals(leafRefValue);
    	} 
    	
    	if (evaluatedValue instanceof ConfigLeafAttribute) {
            return ((ConfigLeafAttribute)evaluatedValue).getStringValue().equals(leafRefValue);
        }
            
        if (evaluatedValue.toString().equals(leafRefValue)) {
            return true;
        }

        if (evaluatedValue.toString().matches(DataStoreValidationUtil.NUMERIC) && leafRefValue.matches(DataStoreValidationUtil.NUMERIC)) {
            if (Double.parseDouble(leafRefValue) == Double.parseDouble(evaluatedValue.toString())) {
                /**
                 * JXPath always retrieves all numeric values as a DOUBLE.toString(), like 10.0, 20.0. So when we have a yang validation
                 * expression like ../someLeaf = 10, this will be false. So converting the lhs/rhs as double, the retrieved value and
                 * the target value in DOUBLE for comparison
                 */
                return true;
            }
        }
        
        return false;
    }

    /**
     * A location path refers to the actual xpath that comes in the condition. For eg: In the following expression,
     * "../adh:hw-type = 'G.FAST'" ../adh:hw-type is the location path
     * 
     * A step is referred to each node in the location path. For eg: In the following expression, "../../adh:hw-type = 'G.FAST'" The
     * location path is ../../adh:hw-type and its steps are "..", ".." and "adh:hw-type"
     * 
     * This method is responsible for analyzing the location path and choosing the right ModelNode/ModelNodeBean as JXPath context by
     * traversing through the steps
     * 
     * @param schemaRegistry
     * @param contextBean
     * @param locationPath
     * @param currentContextNode
     * @return
     */
    protected Object validateLocationPath(DynaBean contextBean, LocationPath inLocationPath,
            Object currentContextNode, String leafRefValue, QName leafQName) {
        LocationPath locationPath = DataStoreValidationUtil.getDynaBeanAlignedPath(inLocationPath);
        Object value = null;
        if (!locationPath.isAbsolute()) {
            
            if (DataStoreValidationUtil.isReadable(contextBean, ModelNodeWithAttributes.PARENT)
                    && locationPath.getSteps()[0].getAxis() == Compiler.AXIS_PARENT) {
                
                /**
                 * If leafQName is null, the condition could well indicate an xpression on a list or container referring to its parent
                 * 
                 * container test {
                 *       leaf value1 {
                 *           type int8;
                 *       }
                 *       
                 *       container c1 {
                 *            must "../value1 = 10";
                 *       }
                 *  }
                 *  
                 *  Here the condition refers to a leaf in its parent bean, while the contextBean could be container C1.
                 */
                DynaBean nextBean = contextBean;
                int index = 0;
                do {
                    nextBean = (DynaBean) nextBean.get(ModelNodeWithAttributes.PARENT);
                    index++;
                } while (DataStoreValidationUtil.isReadable(nextBean, ModelNodeWithAttributes.PARENT) && locationPath.getSteps()[index].getAxis() == Compiler.AXIS_PARENT);
                Step[] newPathSteps = new Step[locationPath.getSteps().length-index];
                System.arraycopy(locationPath.getSteps(), index, newPathSteps, 0, newPathSteps.length);
                LocationPath newLocationPath = new LocationPath(false, newPathSteps);
                if (isContextBeanAtFirstStep(newLocationPath, nextBean, leafRefValue) && 
                        !DataStoreValidationUtil.isReadable(nextBean, DataStoreValidationUtil.getLocalName(newPathSteps[0]))) {
                    value = validatePathAtContextBean(newLocationPath, nextBean, currentContextNode, leafRefValue, leafQName);
                } else {
                    value = evaluate(newLocationPath, nextBean, currentContextNode, leafRefValue, leafQName);
                }
            } else {
                Step[] steps = locationPath.getSteps();
                if (steps.length > 1 && steps[1].getAxis() == Compiler.AXIS_CHILD) {
                    DataSchemaNode rootSchemaNode = DataStoreValidationUtil.getRootSchemaNode(m_schemaRegistry, steps[1]);
                    DynaBean rootBean = DataStoreValidationUtil.getRootModelNode(m_schemaRegistry, rootSchemaNode);
                    if (rootBean != null && steps.length > 2) {
                        Step[] newSteps = new Step[steps.length - 2];// ../<nextRoot> is already resolved
                        System.arraycopy(steps, 2, newSteps, 0, newSteps.length);
                        LocationPath newLocationPath = new LocationPath(false,newSteps);
                        value = evaluate(newLocationPath, rootBean, currentContextNode, leafRefValue, leafQName);
                    } else if (rootBean != null){
                        Step[] newSteps = new Step[steps.length - 1];// ../ is resolved
                        System.arraycopy(steps, 1, newSteps, 0, newSteps.length);
                        LocationPath newLocationPath = new LocationPath(false,newSteps);
                        value = evaluate(newLocationPath, rootBean, currentContextNode, leafRefValue, leafQName);
                    }
                } else {
                    logDebug("we are already at parent. no further traversel possible for {} and path {}", contextBean, inLocationPath);
                }
            }
        } else if (!isfirstStepSameAsRootNode(locationPath, contextBean)) {
            value = processCrossTreeReference(locationPath, currentContextNode, leafRefValue, leafQName);
        }else if (DataStoreValidationUtil.isReadable(contextBean, ModelNodeWithAttributes.PARENT)) {
            value = evaluate(locationPath, (DynaBean) contextBean.get(ModelNodeWithAttributes.PARENT),
                    currentContextNode, leafRefValue, leafQName);
        } else if (isContextBeanAtFirstStep(locationPath, contextBean, leafRefValue)) {
            value = validatePathAtContextBean(locationPath, contextBean, currentContextNode, leafRefValue,
                    leafQName);
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("{} is not an absolute location path, or a relative path and does not have a parent at context {}",
                        locationPath, ModelNodeDynaClass.getContextBeanName(contextBean));
            }
        }

        return value;
    }

    protected Object evaluateIdentity(CoreOperation operation, List<Expression> expressions,
            QName leafQName) {
        List<Expression> newExpression = new ArrayList<Expression>(expressions.size());
        String nsPrefix = m_schemaRegistry.getPrefix(leafQName.getNamespace().toString());
        for (Expression exp : expressions) {
            String constant = exp.compute(null).toString();

            if (!constant.contains(DataStoreValidationUtil.COLON)) {
                /**
                 * We are here, only because one of the xpression is an identity type. Now if the expression constant does not have a
                 * prefix, indicate it is local to the current LeafQName
                 */
                newExpression.add(JXPathUtils.getConstantExpression(nsPrefix + DataStoreValidationUtil.COLON + constant));
            } else {
                newExpression.add(exp);
            }
        }

        return computeExpression(operation, newExpression);
    }


    @SuppressWarnings("rawtypes")
    /**
     * Helps in computing the value of an expression.
     */
    protected Object getExpressionValue(DynaBean contextBean, Object currentContextNode, String leafRefValue, QName leafQName, CoreOperation operation) {
        List<Expression> expressions = new ArrayList<Expression>();
        List<HashSet> leafListValue = new ArrayList<HashSet>();
        boolean secondExpressionAdded = false;
        for (Expression argumentExpression : operation.getArguments()) {
            /*
             * for every sub-xpression in a xpression, get the value individually and do the computation finally. 
             * 
             * Eg: ../value + 10 < 10 
             *  here ../value+10 is a sub-expression
             */
            Object argumentValue = null;
            String argumentExpressionValue = argumentExpression.toString();
            if (isBoolean(argumentExpressionValue)) {
                // A simple true or false value
                argumentValue = argumentExpressionValue;
            } else {
                // Could be anything from constant to a complex locationPath
                argumentValue = evaluate(argumentExpression, contextBean, currentContextNode, leafRefValue,
                        leafQName);

                // If this is a SELF-VALUE '.' and if we have a leafQName indicates, this represents a leaf/leaflist.
                // In that case, we need to check if we have got the QName as a child in container/list -> received dynabean
                if (argumentValue instanceof DynaBean && argumentExpression instanceof LocationPath
                        && ((LocationPath) argumentExpression).getSteps()[0].getAxis() == Compiler.AXIS_SELF) {
                    DynaBean selfValueContainer = (DynaBean) argumentValue;
                    if (leafQName != null) {
                        String localName = ModelNodeDynaBeanFactory.getDynaBeanAttributeName(leafQName.getLocalName());
                        if (DataStoreValidationUtil.isReadable(selfValueContainer, localName)) {
                            argumentValue = selfValueContainer.get(localName);
                        } else {
                            LOGGER.warn("dynaBean {} did not contain attribute {}",selfValueContainer.getDynaClass().getName(), localName);
                        }
                    } // leafQName != null
                }
            }

            if (argumentValue instanceof HashSet) {
                /**
                 * If the returned value is a leafList, we need to iterate through the values to check if the condition matches. It
                 * indicates it is a leaf-list or a list
                 */
                leafListValue.add((HashSet) argumentValue);
                continue;
            } else if (argumentValue instanceof DynaBean) {
                /**
                 * Indicates this is most likely a current() value of a leaf
                 */
                DynaBean currentLeaf = (DynaBean) argumentValue;
                if (DataStoreValidationUtil.isReadable(currentLeaf, ModelNodeWithAttributes.LEAF_VALUE)) {
                    expressions.add(JXPathUtils.getConstantExpression(currentLeaf.get(ModelNodeWithAttributes.LEAF_VALUE)));
                } else {
                    throw new ValidationException("Not a valid leaf for current() " + currentLeaf.getDynaClass().getName());
                }

            }
            Expression ex = null;
            ex = JXPathUtils.getConstantExpression(argumentValue);
            expressions.add(ex);
            if (!leafListValue.isEmpty()) {
                secondExpressionAdded = true;
            }
        }

        return processOperationValue(leafQName, operation, expressions, leafListValue, secondExpressionAdded);
    }

    @SuppressWarnings("rawtypes")
    protected Object processOperationValue(QName leafQName, CoreOperation operation, List<Expression> expressions,
            List<HashSet> leafListValue, boolean secondExpressionAdded) {
        Object value = null;
        if(isAnyObjectNull(expressions)) {
            return false;
        } else if (leafListValue.size() == 1) {
            HashSet leafList = leafListValue.get(0);
            for (Object object : leafList) {
                Expression ex = null;
                List<Expression> expressionsForCompute = new ArrayList<>(expressions);
                if (object instanceof ConfigLeafAttribute) {
                    ex = JXPathUtils.getConstantExpression(((ConfigLeafAttribute) object).getStringValue());
                } else {
                    ex = JXPathUtils.getConstantExpression(object.toString());
                }
                if (secondExpressionAdded) {
                    expressionsForCompute.add(0, ex);
                } else {
                    expressionsForCompute.add(ex);
                }
                value = computeExpression(operation, expressionsForCompute);
                if (isBoolean(value.toString()) && value.toString().equalsIgnoreCase(Boolean.TRUE.toString())) {
                    break;
                }
            }
        } else if (leafListValue.size() == 2) {
            /**
             * Indicates boths sides of an operator has returned leaf-list
             */
            if (leafListValue.get(0).equals(leafListValue.get(1))) {
                value = true;
            } else {
                value = false;
            }

        } else if (isAnyExpressionIdentity(expressions, leafQName)) {
            value = evaluateIdentity(operation, expressions, leafQName);
        } else if (leafListValue.isEmpty()) {
            /**
             * Two straight forward values to compute
             */
            value = computeExpression(operation, expressions);
        } else {
            logDebug("we simply must have come here. We are not able to evaluate {}", operation);
        }

        return value;
    }

    @SuppressWarnings("rawtypes")
    private boolean isAnyObjectNull(Collection objects) {
        for(Object object : objects){
            if(object == null){
                return true;
            }
        }
        return false;
    }

    protected boolean isfirstStepSameAsRootNode(Expression expression, DynaBean contextBean) {
        if (!DataStoreValidationUtil.isReadable(contextBean, ModelNodeWithAttributes.PARENT)) {
            // only if this is a root node, NO PARENT would be available --> more like a GOD :)
            // go ahead
            
            if (expression instanceof LocationPath) {
                // mostly it would have gone through all paths before hitting here. So we can expect only a location path
                Step step = ((LocationPath)expression).getSteps()[0];
                if (DataStoreValidationUtil.isReadable(contextBean, ModelNodeWithAttributes.MODEL_NODE)) {
                    ModelNode modelNode = (ModelNode) contextBean.get(ModelNodeWithAttributes.MODEL_NODE);
                    if (modelNode != null && isStepSameAsModelNode(modelNode.getQName().getNamespace().toString(), 
                    		modelNode.getContainerName(), step, modelNode.getSchemaRegistry())) {
                        return true;
                    } else {
                        String beanName = contextBean.getDynaClass().getName();
                        logDebug("LocationPath does not have the right tree {}, {}", expression, beanName);
                    }
                } else {
                    logDebug("Context Bean {} has no modelNode and no parent for xPath {}", contextBean, expression);
                }
            } else {
                logDebug("Not a location path {} and cannot be validated further", expression);    
            }
           
        }
        return false;
    }
    
    protected Object checkForFunctionsAndEvaluate(Expression xPathCondition, DynaBean contextBean, Object currentContextNode,
            String leafRefValue, QName leafQName) {
        Expression xPath = checkForCurrentFunction(contextBean, currentContextNode, leafQName, xPathCondition, null);
        return evaluate(xPath, contextBean, currentContextNode,leafRefValue,leafQName);

    }
    /**
     * This is the main evaluation method that evaluates the xpath (both leafref and non leafref) in the contextModelNode 
     * 
     * Responsible for initializing JXPathContext for the current contextModelNode DynaBean. 
     * Note that, a call to getValue() method of modelNode would give DynaBean of the ModelNode.
     * 
     * If the xpath cannot be evaluated in the contextModelNode, this method compiles the xpath into JXPath Expression
     * This JXPath expression is further validated to find the right contextModelNode for evaluating the xpath 
     * 
     * This method defined a custom extension function on the dynabean to handle the current()
     * This custom extension function uses the currentContextNode argument
     * 
     * @param schemaRegistry
     * @param xPathCondition
     * @param contextBean
     * @param currentContextNode
     * @return
     */
    protected Object evaluate(Expression xPathCondition, DynaBean contextBean, Object currentContextNode,
            String leafRefValue, QName leafQName) {
        logDebug("Evalute for xpath {} contextBean {} currentContextNode {} leafRefValue {} leafQName {}", xPathCondition, contextBean,
                currentContextNode, leafRefValue, leafQName);
        Object value = null;
        Expression ex = xPathCondition;
        Expression xPathForValidation = null;
        Expression xPath = xPathCondition;
        
        DSExpressionValidator childValidator = m_validators.get(xPathCondition.getClass());
        if (childValidator != null) {
            logDebug("Found a validator {} for xpath {}", childValidator, xPathCondition);
            Object result = childValidator.evaluate(xPathCondition, contextBean, currentContextNode, leafRefValue, leafQName);
            logDebug("--> XPath {} evaluates to {}", xPathCondition, result);
            return result;
        }
        
        if (isLocationPathWithSteps(ex) && !hasParentSteps(ex)){
            xPathForValidation = removePrefixes(xPath);
            JXPathContext context = getContext(contextBean);

            if (currentContextNode == null && xPath.toString().contains(DataStoreValidationUtil.CURRENT_PATTERN)) {
                // indicates the node is not created yet.
                value = true;
            } else if (leafRefValue != null) {
                value = validateLeafRef(context, xPathForValidation.toString(), leafRefValue);
            } else {
                value = context.getValue(xPathForValidation.toString());
            }
            logDebug("evaluate value is {} for xpath {} contextBean {}", value, xPathForValidation, contextBean);

            if ((value == null || (value instanceof Boolean && !((Boolean) value))) && isAbsolutePath(ex)) {
                if (leafRefValue == null) {
                    value = validateAbsolutePath(xPathCondition, contextBean, currentContextNode, leafRefValue, leafQName);
                } else if (leafRefValue != null && ex instanceof LocationPath){
                    LocationPath newPath = (LocationPath) ex;
                    if (isContextBeanAtFirstStep(newPath, contextBean, leafRefValue)){
                        newPath = DataStoreValidationUtil.excludeFirstStep(newPath);
                    }
                    ModelNode contextNode = DataStoreValidationUtil.isReadable(contextBean, ModelNodeWithAttributes.MODEL_NODE)?(ModelNode) contextBean.get(ModelNodeWithAttributes.MODEL_NODE):null;
                    DataSchemaNode schemaNode = contextNode==null? null:SchemaRegistryUtil.getSchemaNodeForXpath(contextNode.getSchemaRegistry(), contextNode.getModelNodeSchemaPath(), (LocationPath) removePrefixes(newPath));
                    if((schemaNode != null && !schemaNode.isConfiguration()) || (schemaNode == null && isXPathForStateValidation(contextBean, newPath))) {
                        // if the path is for state validation
                        if (isStateValidation(contextBean, newPath)) {
                            value = evaluateStateData(newPath, contextBean, null, leafRefValue, leafQName);
                        } else {
                            // the next step is not state
                            value = evaluateStateInPath(newPath, contextBean, currentContextNode, leafRefValue, leafQName);
                        }
                    }
                }
            }
        }

        if (value == null || (value instanceof Boolean && !((Boolean) value))) {
            if (DataStoreValidationUtil.isCoreOperation(ex)) {
                // xpath is an expression a/b/c = 'a'
                value = getExpressionValue(contextBean, currentContextNode, leafRefValue, leafQName, (CoreOperation) ex);
            } else if (isLocationPathWithSteps(ex)) {
                if (xPathForValidation != null && ((LocationPath) ex).getSteps()[0].getAxis() == Compiler.AXIS_PARENT) {
                    // xpath is already processed and has a parent step
                    String stepName = DataStoreValidationUtil.getLocalName(((LocationPath) ex).getSteps()[0]);
                    if (xPathForValidation != null && stepName != null) {
                        stepName = ModelNodeDynaBeanFactory.getDynaBeanAttributeName(stepName);
                        String contextName = ModelNodeDynaBeanFactory.getDynaBeanAttributeName(contextBean.getDynaClass().getName());
                        if (!(DataStoreValidationUtil.isReadable(contextBean, stepName) && !contextName.equals(stepName))) {
                            // if contextBean already has the first step of the location step and
                            // xPathForValidation is not null, indicating it is built
                            // the dynaBean name and stepName are not same (meaning container/attribute do not share the same name)
                            // then we are good to validate
                            value = validateLocationPath(contextBean, (LocationPath) ex, currentContextNode, leafRefValue, leafQName);
                        } else {
                            logDebug("Path already evaluated at bean {} for {}", contextBean, ex);
                        }
                    }
                } else if (isContextBeanAtFirstStep(ex, contextBean, leafRefValue)) {
                    value = validatePathAtContextBean(xPathCondition, contextBean, currentContextNode, leafRefValue, leafQName);
                } else if (((LocationPath) ex).getSteps()[0].getAxis() != Compiler.AXIS_PARENT
                        && !isfirstStepSameAsRootNode(ex, contextBean)) {
                    /**
                     * If this is an absolute path starting with one of the root Nodes and the current ContextBean is on a different tree
                     * than the first step of this tree, well lets try and validate on a different Root Node that this xPath wants.
                     */
                    ModelNode modelNode = getRootNodeFor(ex);
                    if (modelNode != null) {
                        value = evaluate(xPathCondition, (DynaBean) modelNode.getValue(), currentContextNode, leafRefValue, leafQName);
                    } else {
                        logDebug("No config tree identified for expression {}", ex);
                    }
                } else {
                    value = validateLocationPath(contextBean, (LocationPath) ex, currentContextNode, leafRefValue, leafQName);
                }
            } else {
                LOGGER.warn(null, "ex {} not evaluated. Neither a locationPath/coreOperation or a coreFunction", ex.toString());
            }

        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(null,
                        "{} is neither at first step, is not a core operation and does not have a parent for contextBean or a locationPath {}",
                        ex, ModelNodeDynaClass.getContextBeanName(contextBean));
            }
        }
        logDebug("--> XPath {} evaluates to {}", xPathCondition, value);
        return value;
    }
    
	private JXPathContext getContext(DynaBean contextBean) {
		JXPathContext context = JXPathContext.newContext(contextBean);
		context.setLenient(true);
		return context;
	}
    
    protected Object processCrossTreeReference(Expression xPathCondition, Object currentContextNode, String leafRefValue, QName leafQName) {
        Object value = null;
        /**
         * If this is an absolute path starting with one of the root Nodes and the current ContextBean is on a different tree than the first
         * step of this tree, well lets try and validate on a different Root Node that this xPath wants.
         */
        ModelNode modelNode = getRootNodeFor(xPathCondition);
        if (modelNode != null) {
            value = evaluate(xPathCondition, (DynaBean) modelNode.getValue(), currentContextNode, leafRefValue, leafQName);
        } else {
            logDebug("No config tree identified for expression {}", xPathCondition);
        }
        return value;
    }

    private Expression checkForCurrentFunction(DynaBean contextBean, Object currentContextNode, QName leafQName, Expression xPath, String prefix) {
        Expression xPathForValidation = addCurrentToSingleKeyList(xPath);
        if (xPathForValidation.toString().contains(DataStoreValidationUtil.CURRENT_PATTERN)) {
            xPathForValidation = checkForSingleKeyCurrent(xPathForValidation, contextBean, currentContextNode);
            xPathForValidation = setFunctionOnContext(contextBean, currentContextNode, xPathForValidation, prefix, leafQName);
            return xPathForValidation;
        }
        return xPathForValidation;
    }
    
    protected DSExpressionValidator(){
        
    }
    
    protected void setValidators(Map<Class<?>, DSExpressionValidator> validators) {
        m_validators = validators;
    }

    protected boolean isStateValidation(DynaBean contextBean, LocationPath xPath) {
        DynaBean parentBean = contextBean;
        while (!DataStoreValidationUtil.isReadable(parentBean, ModelNodeWithAttributes.MODEL_NODE)) {
            // indicates this is a leaf bean/choice/case bean
            parentBean = (DynaBean) parentBean.get(ModelNodeWithAttributes.PARENT);
        }
        
        ModelNode modelNode = (ModelNode) parentBean.get(ModelNodeWithAttributes.MODEL_NODE);
        String firstStepName = DataStoreValidationUtil.getLocalName(xPath.getSteps()[0]);
        SchemaPath parentPath = modelNode.getModelNodeSchemaPath();
        DataSchemaNode childNode = getChildNode(modelNode.getSchemaRegistry(), parentPath, firstStepName);
    
        if (childNode != null && !childNode.isConfiguration()) {
            return true;
        } else if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(null, "childNode is {} for parentPath-{} and localName-{}", childNode, parentPath, firstStepName);
        }

        return false;
    }
    
    protected DataSchemaNode getChildNode(SchemaRegistry schemaRegistry, SchemaPath schemaPath, String childLocalName) {
        SchemaPath childPath = DataStoreValidationUtil.getChildPath(schemaRegistry, schemaPath, childLocalName);
        DataSchemaNode childNode = schemaRegistry.getDataSchemaNode(childPath);
        return childNode;
    }
    
    @SuppressWarnings("rawtypes")
    protected boolean isXPathForStateValidation(DynaBean contextBean, LocationPath xPath) {
        if (xPath != null && xPath.getSteps().length > 0) {
            if (isStateValidation(contextBean, xPath)) {
                return true;
            }
            String firstStepName = DataStoreValidationUtil.getLocalName(xPath.getSteps()[0]);
            if (firstStepName != null && DataStoreValidationUtil.isReadable(contextBean, firstStepName)) {
                Object child = contextBean.get(firstStepName);
                if (child != null && child instanceof DynaBean) {
                    // indicates child container
                    LocationPath nextPath = DataStoreValidationUtil.excludeFirstStep(xPath);
                    return isXPathForStateValidation((DynaBean)child, nextPath);
                } else if (child != null && child instanceof List) {
                    //indicates a child List
                    for (Object childObject:(List)child) {
                        LocationPath nextPath = DataStoreValidationUtil.excludeFirstStep(xPath);
                        return isXPathForStateValidation((DynaBean)childObject, nextPath);
                    }
                }
            }
        }
        return false;
    }

    @SuppressWarnings("rawtypes")
    protected Object evaluateStateInPath(LocationPath xPath, DynaBean contextBean, Object currentContextBean, String leafRefValue, QName leafQName) {
        Object returnValue = null;
        String firstStepName = DataStoreValidationUtil.getLocalName(xPath.getSteps()[0]);
        LocationPath nextPath = DataStoreValidationUtil.excludeFirstStep(xPath);
        Object child = contextBean.get(firstStepName);
        if (child != null && child instanceof DynaBean) {
            returnValue = evaluate(nextPath, (DynaBean) child, currentContextBean, leafRefValue, leafQName);
        } else if (child != null && child instanceof List) {
            // indicates a list
            for (Object childBean: (List)child) {
                returnValue = evaluate(nextPath, (DynaBean) childBean, currentContextBean, leafRefValue, leafQName);
                if (returnValue != null && ! (returnValue instanceof Boolean && !((Boolean)returnValue))) {
                    return returnValue;
                }
            }
        }
        return returnValue;
    }

    protected void setNSContext(JXPathContext context, Node node) {
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            NamedNodeMap nodeMap = child.getAttributes();
            for (int j = 0; nodeMap!=null && j < nodeMap.getLength(); j++) {
                String ns = nodeMap.item(j).getNamespaceURI();
                if (ns != null) {
                    String prefix = m_schemaRegistry.getPrefix(ns);
                    context.registerNamespace(prefix, ns);
                }
            }
            setNSContext(context, child);
        }
    }
    protected LocationPath appendMissingPrefixes(LocationPath xPath) {
        Step[] steps = xPath.getSteps();
        Step[] newSteps = new Step[steps.length];
        boolean newStepCreated = false;
        for (int i=0; i<steps.length;i++) {
            Step currentStep = steps[i];
            if (currentStep.getNodeTest() instanceof NodeNameTest) {
                NodeNameTest node = (NodeNameTest) currentStep.getNodeTest();
                if (node.getNodeName().getPrefix() == null) {
                    newStepCreated = true;
                    String ns = (String)RequestScope.getCurrentScope().getFromCache(DataStoreValidationUtil.STATE_VALIDATION_MODULE_NS);
                    String prefix = m_schemaRegistry.getPrefix(ns);
                    org.apache.commons.jxpath.ri.QName qname = new org.apache.commons.jxpath.ri.QName(prefix,node.getNodeName().getName());
                    Step newStep = new YangStep(qname, ns, currentStep.getPredicates());
                    newSteps[i] = newStep;
                } else {
                    newSteps[i] = currentStep;
                }
            }
        }
        
        LocationPath returnValue = xPath;
        if (newStepCreated) {
            returnValue = new LocationPath(xPath.isAbsolute(), newSteps);
        }
        return returnValue;
    }
    protected Object evaluateStateData(LocationPath xPath, DynaBean contextBean, ModelNode stateNode, String leafRefValue,
            QName leafQName) {
        try {
            Object returnValue = null;
            ModelNode modelNode = (ModelNode) contextBean.get(ModelNodeWithAttributes.MODEL_NODE);
            ModelNodeId modelNodeId = modelNode.getModelNodeId();
            SchemaPath modelNodeSchemaPath = modelNode.getModelNodeSchemaPath();
            SubSystem subSystem = m_subSystemRegistry.lookupSubsystem(modelNodeSchemaPath);
            if (subSystem != null) {
                // subsystem is available for the path
                LocationPath newXPath = appendMissingPrefixes(xPath);
                Step[] xPathSteps = xPath.getSteps();
                String firstStepName = DataStoreValidationUtil.getLocalName(xPathSteps[0]);
                DataSchemaNode childSchemaNode = getChildNode(modelNode.getMountRegistry(), modelNodeSchemaPath, firstStepName);
                
                // prepare state retrivale filter from subsystem. 
                // the first step will be the state and that will be the first QName
                Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> stateFilter = new HashMap<ModelNodeId, Pair<List<QName>, List<FilterNode>>>();
                List<QName> qnames = new LinkedList<QName>();
                if (childSchemaNode instanceof ContainerSchemaNode || childSchemaNode instanceof ListSchemaNode) {
                    // List/Container must be added as a filterNode
                    List<FilterNode> filterNodes = new LinkedList<FilterNode>();
                    FilterNode filterNode = new FilterNode(childSchemaNode.getQName());
                    DataSchemaNode nextChildNode = childSchemaNode;
                    FilterNode nextFilterNode = filterNode;
                    filterNodes.add(filterNode);
                    for (int i=1;i<xPathSteps.length;i++) {
                        nextChildNode = getChildNode(modelNode.getMountRegistry(), childSchemaNode.getPath(), DataStoreValidationUtil.getLocalName(xPathSteps[i]));
                        if (nextChildNode instanceof ListSchemaNode || nextChildNode instanceof ContainerSchemaNode) {
                            FilterNode newFilterNode = new FilterNode(nextChildNode.getQName());
                            nextFilterNode.addContainmentNode(newFilterNode);
                            nextFilterNode = newFilterNode;
                        }
                    }
                    Pair<List<QName>, List<FilterNode>> newPair = new Pair<List<QName>, List<FilterNode>>(qnames, filterNodes);
                    stateFilter.put(modelNodeId, newPair);
                } else {
                    // Attribute must be added as a child QName
                    QName childQName = childSchemaNode.getQName();
                    qnames.add(childQName);
                    Pair<List<QName>, List<FilterNode>> newPair = new Pair<List<QName>, List<FilterNode>>(qnames,
                            Collections.emptyList());
                    stateFilter.put(modelNodeId, newPair);
                }
                Map<ModelNodeId, List<Element>> stateData = subSystem.retrieveStateAttributes(stateFilter, NetconfQueryParams.NO_PARAMS);
                String xPathForValidation = newXPath.toString();
                
                // validate the path in the retrieved set of elements
                List<Element> elements = stateData.get(modelNodeId);
                for (Element element : elements) {
                    // create a parent element and append the new Element under it
                    // set the parent as JXPath context and evaluate the path with prefixes. 
                    Element parentElement = DocumentUtils.createDocument()
                            .createElementNS(modelNode.getQName().getNamespace().toString(), modelNode.getQName().getLocalName());
                    parentElement.appendChild(parentElement.getOwnerDocument().importNode(element, true));
                    JXPathContext context = JXPathContext.newContext(parentElement);

                    // In DOM validation, NS must be set in the context
                    setNSContext(context, parentElement);
                    String ns = (String) RequestScope.getCurrentScope().getFromCache(DataStoreValidationUtil.STATE_VALIDATION_MODULE_NS);
                    if (ns != null) {
                        String prefix = m_schemaRegistry.getPrefix(ns);
                        context.registerNamespace(prefix, ns);
                    }
                    
                    if (leafRefValue != null) {
                        returnValue = validateLeafRef(context, xPathForValidation, leafRefValue);
                    } else {
                        returnValue = context.getValue(xPathForValidation);
                    }
                    
                    if (returnValue != null && !(returnValue instanceof Boolean && !((Boolean)returnValue))) {
                        break;
                    }
                }
            } else if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(null, "Subsystem is null for path-{}", modelNodeSchemaPath);
            }
            return returnValue;
        } catch (GetAttributeException e) {
            LOGGER.error(null, "Unable to evaluate state data for xpath-{}, leafRefValue-{}, leafQName-{}", xPath, leafRefValue, leafQName,
                    e);
            ValidationException exception = new ValidationException("Unable to evaluate state data for xpath" + xPath);
            exception.addNetconfRpcError(e.getRpcError());
            exception.addSuppressed(e);
            throw exception;
        }
    }
    
    public DSExpressionValidator(SchemaRegistry schemaRegistry, ModelNodeHelperRegistry modelNodeHelperRegistry, SubSystemRegistry subsystemRegsitry) {
        m_schemaRegistry = schemaRegistry;
        m_modelNodeHelperRegistry = modelNodeHelperRegistry;
        m_subSystemRegistry = subsystemRegsitry;
        m_validators.put(ExtensionFunction.class, new DSExtensionFunctionValidator(schemaRegistry, modelNodeHelperRegistry, subsystemRegsitry, m_validators));
        m_validators.put(Constant.class, new DSConstantValidator(schemaRegistry, modelNodeHelperRegistry, subsystemRegsitry, m_validators));
        m_validators.put(CoreOperationOr.class, new DSExpressionOrValidator(schemaRegistry, modelNodeHelperRegistry, subsystemRegsitry, m_validators));
        m_validators.put(CoreOperationAnd.class, new DSExpressionAndValidator(schemaRegistry, modelNodeHelperRegistry, subsystemRegsitry, m_validators));
        m_validators.put(CoreFunction.class, new DSFunctionValidator(schemaRegistry, modelNodeHelperRegistry, subsystemRegsitry, m_validators));
        m_validators.put(ExpressionPath.class, new DSExpressionPathValidator(schemaRegistry, modelNodeHelperRegistry, m_validators));
    }

    public void validateMust(MustDefinition definition, DataSchemaNode schemaNode, ModelNode parentModelNode) {
        logDebug("must validation for xpath {} modelNode {} child {} callFromLeaf {} ", definition, parentModelNode.getModelNodeId(),
                schemaNode.getQName());
        String mustDefinitionString = definition.toString();
        ValidationTimingLogger.startConstraint(ValidationTimingLogger.ConstraintType.MUST, mustDefinitionString);
		try {
		    if (doesChildExistInModelNode(schemaNode, parentModelNode)) {
		          boolean isValidConstraint = validateXPathInModelNode(mustDefinitionString, parentModelNode, null, parentModelNode, schemaNode);
		            if (!isValidConstraint) {
		                if (LOGGER.isInfoEnabled()) {
		                    LOGGER.info("must validation failed for xPath {} modelNode{} child{}", definition,
		                            parentModelNode.getModelNodeId(), schemaNode.getQName());
		                }
		                ValidationException violateMustException = DataStoreValidationErrors
		                        .getViolateMustContrainsException(definition);
		                ModelNodeId id = getErrorPath(schemaNode, parentModelNode, schemaNode.getQName(),
		                        (DynaBean) parentModelNode.getValue());
		                violateMustException.getRpcError().setErrorPath(id.xPathString(parentModelNode.getMountRegistry()),
		                        id.xPathStringNsByPrefix(parentModelNode.getMountRegistry()));
		                throw violateMustException;
		            } 
		    }
		} finally {
			ValidationTimingLogger.endConstraint(ValidationTimingLogger.ConstraintType.MUST, mustDefinitionString);
		}
    }

    private boolean doesChildExistInModelNode(DataSchemaNode childSchemaNode, ModelNode parentModelNode) {
        DynaBean dynaBean = (DynaBean) parentModelNode.getValue();
        SchemaPath schemaPath = childSchemaNode.getPath();
        String localName = ModelNodeDynaBeanFactory.getDynaBeanAttributeName(schemaPath.getLastComponent().getLocalName());
        if (DataStoreValidationUtil.isReadable(dynaBean, localName)) {
            Object child = dynaBean.get(localName);
            if (child != null)
                return true;
        }
        if (childSchemaNode.getPath().isAbsolute() && parentModelNode.getModelNodeSchemaPath().equals(childSchemaNode.getPath())) {
            return true;
        }
        return false;
    }

    public boolean validateWhenConditionOnModule(ModelNode childModelNode, DataSchemaNode child) {
        boolean isValid = true;
        SchemaRegistry registry = SchemaRegistryUtil.getSchemaRegistry(childModelNode, m_schemaRegistry);
        QNameModule qModule = child.getQName().getModule();
        if (qModule != null) {
            Optional<Module> optModule = registry.findModuleByNamespaceAndRevision(qModule.getNamespace(), qModule.getRevision().orElse(null));
            if (optModule.isPresent()) {
                Module module = optModule.get();
                for (AugmentationSchemaNode augmentationSchema : module.getAugmentations()) {
                    Optional<RevisionAwareXPath> optWhenCondition = augmentationSchema.getWhenCondition();
                    if (optWhenCondition.isPresent() && augmentationSchema.getChildNodes().contains(child)) {
                        RevisionAwareXPath whenCondition = optWhenCondition.get();
                        isValid = validateXPathInModelNode(whenCondition.toString(), childModelNode,null , childModelNode, child);
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("{} is {} on {} for Model Node {}", whenCondition.toString(), isValid,
                                    child.getQName().getLocalName(), childModelNode);
                        }
                        return isValid;
                    }
                }
            }
        }
        if (isValid) {
            Optional<RevisionAwareXPath> optWhenCondition = child.getWhenCondition();
        	if (optWhenCondition.isPresent()) {
        	    RevisionAwareXPath xpath = optWhenCondition.get();
        		isValid = validateWhen(JXPathUtils.getExpression(xpath.toString()), child, childModelNode, false);
        	}
        }
        return isValid;
    }

    public boolean validateWhen(Expression expression, DataSchemaNode schemaNode, ModelNode parentNode, boolean impactValidation) {
    	String expressionString = expression.toString();
    	ValidationTimingLogger.startConstraint(ValidationTimingLogger.ConstraintType.WHEN, expressionString);
        boolean returnValue;
		try {
			returnValue = true;
			if (SchemaRegistryUtil.hasDefaults(schemaNode) && ((ModelNodeWithAttributes)parentNode).getAttribute(schemaNode.getQName()) == null) {
			    // if it is a leaf with default and that is not found in modelNode, then we need to create it if "when" validates to true
			    Collection<SchemaPath> schemaPathToCreate = DataStoreValidationUtil.getValidationContext().getSchemaPathsToCreate();
			    schemaPathToCreate.add(schemaNode.getPath());
			}
			
			returnValue = validateXPathInModelNode(expressionString, parentNode, null, parentNode, schemaNode);
			processWhenCondition(schemaNode, parentNode, returnValue, expressionString, impactValidation);
		} finally {
			ValidationTimingLogger.endConstraint(ValidationTimingLogger.ConstraintType.WHEN, expressionString);
		}
        return returnValue;
    }

    @SuppressWarnings("rawtypes")
    protected void processWhenCondition(DataSchemaNode schemaNode, ModelNode modelNode, boolean isValidConstraint, String xPath, boolean impactValidation) {
        Collection<SchemaPath> schemaPathsToDelete = DataStoreValidationUtil.getValidationContext().getSchemaPathsToDelete();
        Collection<SchemaPath> schemaPathsToCreate = DataStoreValidationUtil.getValidationContext().getSchemaPathsToCreate();
        QName childQName = schemaNode == null? null : schemaNode.getQName();
        SchemaPath constraintNodePath = schemaNode == null ? null : schemaNode.getPath();
        if (!isValidConstraint) {
            if (modelNode instanceof ProxyValidationModelNode) {
                // condition fails on a non-existent node. It can be ignored. 
                return;
            }
            logDebug("when validation is false for xpath{} modelNode {} qname {}", xPath, modelNode.getModelNodeId(), childQName);
            if (schemaPathsToDelete != null && schemaPathsToDelete.contains(constraintNodePath)) {
                /**
                 * if the schemaPath is present in this cache, we know we have hit a when condition for the schemaPath whose node has to
                 * be deleted.
                 * 
                 * Record this node as to be deleted.
                 */
                if (!ChoiceCaseNodeUtil.isChoiceOrCaseNode(schemaNode)) {
                    DynaBean bean = (DynaBean) modelNode.getValue();
                    String stepName = ModelNodeDynaBeanFactory.getDynaBeanAttributeName(childQName.getLocalName());
                    if (DataStoreValidationUtil.isReadable(bean, stepName)) {
                        Object childValue = bean.get(stepName);
                        if (childValue != null && !(childValue instanceof Collection && ((Collection)childValue).isEmpty())) {
                            /**
                             * When the node does exists, and if it evaluates to false we want to record that node for deletion, only if it is
                             * not identified for creation If it is identified for creation(schemaPathsToCreate) --> it means the node does not
                             * exists yet
                             */
                            Map<ModelNode, Collection<QName>> nodesToDelete = DataStoreValidationUtil.getValidationContext().getDeleteList();
                            Collection<QName> childQNames = nodesToDelete.get(modelNode);
                            if (childQNames == null) {
                                childQNames = new LinkedHashSet<QName>();
                                nodesToDelete.put(modelNode, childQNames);
                            }
                            childQNames.add(childQName);
                            logDebug("childQName {} must be deleted from modelNode {}", childQName, modelNode.getModelNodeId());
                        } else {
                            logDebug("Tried evaluating a non-existent node and it evaluated to false");
                        }
                    }
                } else {
                    logDebug("Tried evaluating a non-existent node and it evaluated to false");
                }
            } else if (!(schemaPathsToCreate != null && schemaPathsToCreate.contains(constraintNodePath))) {
                DynaBean bean = (DynaBean) modelNode.getValue();
                if(childQName != null ){
	                String stepName = ModelNodeDynaBeanFactory.getDynaBeanAttributeName(childQName.getLocalName());
	                if (DataStoreValidationUtil.isReadable(bean, stepName)) {
	                    Object childValue = bean.get(stepName);
	                    if (childValue != null && !(childValue instanceof Collection && ((Collection)childValue).isEmpty())) {
	                        // We will get collections when we have a leafList. The list could be empty due
	                        // to a delete operation in the current scope
	                        throwWhenViolation(schemaNode, modelNode, xPath, childQName, bean);
	                    }
	                } else if (ChoiceCaseNodeUtil.isChoiceOrCaseNode(schemaNode)) {
	                    throwWhenViolation(schemaNode, modelNode, xPath, childQName, bean);
	                }
                } else {
                	/*
                	 * In case of impact validation , we do not have to throw error.
                	 * Eg:
                	 * augment "a/augmentInterface" {
                	 * 	when "augmentType= 'typeA'"
                	 * 	container aType {
                	 * 	}
                	 * }
                	 * 
                	 * augment "a/augmentInterface" {
                	 * 	when "augmentType= 'typeB'"
                	 * 	container aType {
                	 * 	}
                	 * }
                	 * 
                	 * RPC:
                	 * <a>
                	 * <augmentInterface>
                	 * <augmentType>typeA</augmentType>
                	 * <aType/>
                	 * </augmentInteface>
                	 * </a>
                	 * 
                	 * Thus , during impact validation when condition "augmentType = 'typeB', will be false , 
                	 * which should not throw error.
                	 */
					if (!impactValidation) {
						DataSchemaNode augmentChildNode = DataStoreValidationUtil.getValidationContext()
								.getAugmentChildNode();
						SchemaPath augmentChildPath = augmentChildNode == null ? null : augmentChildNode.getPath();
						if (!schemaPathsToCreate.contains(augmentChildPath)) {
							if(DataStoreValidationUtil.getValidationContext().isThisMandatoryNodesCheck()){
								logDebug("Augment when condition {} resulted in false for augment child path {} during validating mandatory node", xPath , augmentChildPath);
							} else {
								throwWhenViolation(schemaNode, modelNode, xPath, childQName, bean);
							}
						}
					}
                }
            } else {
                // if a constraint is false and if its exists, we will record it for deletion
                // if its not available yet, we will not create it
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("A node is recorded for creation and also exists already. ParentNode:{},childQName:{}",
                            modelNode.getModelNodeId(), childQName);
                }
            }
        } else {
            if (constraintNodePath == null) {
                DataSchemaNode augmentChildNode = DataStoreValidationUtil.getValidationContext().getAugmentChildNode();
                constraintNodePath = augmentChildNode == null ? null : augmentChildNode.getPath();
                schemaNode = augmentChildNode;
                childQName = augmentChildNode.getQName();

            }
            if (schemaPathsToCreate.contains(constraintNodePath)) {
            	if (schemaNode != null && ChoiceCaseNodeUtil.isChoiceOrCaseNode(schemaNode)) {
            		return ;
            	}
            	Map<ModelNode, Collection<QName>> nodesToModify = DataStoreValidationUtil.getValidationContext().getMergeList();

                Collection<QName> childQNames = nodesToModify.get(modelNode);
                if (childQNames == null) {
                    childQNames = new LinkedHashSet<QName>();
                    nodesToModify.put(modelNode, childQNames);
                }
                childQNames.add(childQName);
                DataStoreValidationUtil.getValidationContext().recordDefaultValue(constraintNodePath, modelNode, getDefaults(schemaNode));
            }
        }
    }
    protected void throwWhenViolation(DataSchemaNode schemaNode, ModelNode modelNode, String xPath, QName childQName, DynaBean bean) {
        ValidationException violateWhenException = DataStoreValidationErrors.getViolateWhenConditionExceptionThrownUnknownElement(xPath);
        ModelNodeId id = getErrorPath(schemaNode, modelNode, childQName, bean);
        SchemaRegistry registry = SchemaRegistryUtil.getSchemaRegistry(modelNode, m_schemaRegistry);
        violateWhenException.getRpcError().setErrorPath(id.xPathString(registry), id.xPathStringNsByPrefix(m_schemaRegistry));
        throw violateWhenException;
    }
    @SuppressWarnings("unchecked")
    protected ModelNodeId getErrorPath(DataSchemaNode inSchemaNode, ModelNode modelNode, QName inChildQName, DynaBean bean) {
        DataSchemaNode schemaNode = inSchemaNode;
        QName childQName = inChildQName;
        if (ChoiceCaseNodeUtil.isChoiceOrCaseNode(schemaNode)) {
            schemaNode = DataStoreValidationUtil.getValidationContext().getChildOfChoiceCase();
            schemaNode = schemaNode == null ? inSchemaNode : schemaNode;
            childQName = schemaNode.getQName();
        }
        // inSchemaNode and inChildQName value are null if it is AugmentValidator. In this case, cached Augment child node will be fetched from DSValidationContext.
        if(inSchemaNode == null && inChildQName == null ){
        	schemaNode = DataStoreValidationUtil.getValidationContext().getAugmentChildNode();
        	if(schemaNode != null){
        		childQName = schemaNode.getQName();
        	}
        }
        ModelNodeId id = new ModelNodeId(modelNode.getModelNodeId());
        boolean leafValue = schemaNode instanceof LeafSchemaNode || schemaNode instanceof LeafListSchemaNode;
        boolean containerValue = schemaNode instanceof ContainerSchemaNode || schemaNode instanceof CaseSchemaNode;
        if (containerValue && schemaNode.getPath().getParent().getLastComponent() != null) {
            id.addRdn(new ModelNodeRdn(ModelNodeRdn.CONTAINER, childQName.getNamespace().toString(), childQName.getLocalName()));
        } else if (schemaNode instanceof ListSchemaNode){
            Collection<DynaBean> beans = (Collection<DynaBean>) bean.get(childQName.getLocalName());
            ModelNode childModelNode = (ModelNode) beans.iterator().next().get(ModelNodeWithAttributes.MODEL_NODE);
            id = childModelNode.getModelNodeId();
        } else if (leafValue) {
            id.addRdn(new ModelNodeRdn(ModelNodeRdn.CONTAINER, childQName.getNamespace().toString(), childQName.getLocalName()));
        }
        return id;
    }

    /**
     * Instance identifier is a special case in handling xpath XPaths come in the text content of the XML Element This method is responsible
     * for building the JXPath context and validating the instance-identifier on the right context node
     * 
     * CAUTION: org.apache.commons.jxpath.ri.model.dynabeans.DynaBeanPropertyPointer#getPropertyNames (Line 78) imposes a restriction that,
     * yang container/list/leaf/leafref cannot contain the name "class". The above mentioned line tries to find the property with name
     * 'class' and is skipping it. This is a limitation of DynaBean
     * 
     * @param modelNode
     * @param instanceIdAttr
     * @param isRequired
     * @param elementName
     * @throws ValidationException
     */
    public void validateInstanceIdentifierElement(ModelNode modelNode, ConfigLeafAttribute instanceIdAttr, boolean isRequired,
            String elementName, String namespace) throws ValidationException {

        ValidationTimingLogger.startConstraint(ValidationTimingLogger.ConstraintType.INSTANCEID, modelNode.getQName().toString());

        try {
            ModelNode contextNode = modelNode;
            SchemaRegistry registry = SchemaRegistryUtil.getSchemaRegistry(modelNode, m_schemaRegistry);
            if (isRequired) {
                ModelNodeId id = buildModelNodeId(modelNode, elementName, namespace);

                if (instanceIdAttr == null) {
                    throw DataStoreValidationErrors.getMissingDataException(
                            String.format("Missing required element %s", elementName), id.xPathString(registry),
                            id.xPathStringNsByPrefix(registry));
                } else {
                    LocationPath xPath = (LocationPath) JXPathUtils.getExpression(instanceIdAttr.getStringValue());

                    String ns = null;
                    if (instanceIdAttr instanceof InstanceIdentifierConfigAttribute) {
                        ns = ((InstanceIdentifierConfigAttribute) instanceIdAttr).getAttributeNamespace();
                    }
                    QName leafQName = registry.lookupQName(ns, instanceIdAttr.getDOMValue().getLocalName());
                    contextNode = getContextNode(contextNode);
                    xPath = DataStoreValidationUtil.getDynaBeanAlignedPath(xPath);

                    Object value = checkForFunctionsAndEvaluate(xPath, (DynaBean) contextNode.getValue(),
                            modelNode.getValue(), null, leafQName);

                    boolean result = false;
                    if (value != null && value instanceof Boolean) {
                        result = (boolean) value;
                    } else if (value instanceof Object) {
                        result = true;
                    }

                    if (!result) {
                        throw DataStoreValidationErrors.getMissingDataException(
                                String.format("Missing required element %s", instanceIdAttr.getStringValue()),
                                id.xPathString(registry), id.xPathStringNsByPrefix(registry));
                    }
                }
            }
        } finally {
            ValidationTimingLogger.endConstraint(ValidationTimingLogger.ConstraintType.INSTANCEID, modelNode.getQName().toString());
        }
    }

	private ModelNodeId buildModelNodeId(ModelNode modeNode, String childName, String namespace) {
		ModelNodeId id = new ModelNodeId(modeNode.getModelNodeId());
		id.addRdn(new ModelNodeRdn(ModelNodeRdn.CONTAINER, namespace, childName));
		return id;
	}
	
    /**
     * This method takes care of validating xpath in modelnode. Returns the result of the xpath expression as boolean
     * 
     * @param schemaRegistry
     * @param xPath
     * @param contextModelNode
     * @param leafRefValue
     * @param currentContextNode
     * @param schemaNode
     * @return
     */
    @SuppressWarnings("rawtypes")
	public boolean validateXPathInModelNode(String inputXPath, ModelNode contextModelNode,
            String leafRefValue, Object currentContextNode, DataSchemaNode schemaNode) {


		Object value = null;
        SchemaRegistry registry = SchemaRegistryUtil.getSchemaRegistry(contextModelNode, m_schemaRegistry);
		Expression xPath = registry.getRelativePath(inputXPath, schemaNode);
		if (xPath == null) {
			xPath = JXPathUtils.getExpression(inputXPath);
		}

		logDebug("Relative xpath is {} for {} ", xPath, inputXPath);

		Expression ex = DataStoreValidationUtil.getDynaBeanAlignedPath(xPath);
		if (ex instanceof LocationPath) {
			value = setContextAndEvaluteLocationPath(contextModelNode, leafRefValue, currentContextNode, schemaNode,
					value, xPath, ex);
		}
		if (value == null) {
			// if we encounter a leaf node we have to pass on the leaf dynaBean
			if (schemaNode instanceof LeafSchemaNode) {
				value = setContextForLeafAndEvaluate(contextModelNode, leafRefValue, currentContextNode, schemaNode,
						ex);
			} else {
				value = setContextAndEvaluateModelNode(contextModelNode, leafRefValue, schemaNode, value, ex);
			}
		}
		if (value != null) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("validateXPathInModelNode returns {} for xPath {} modelNode {} child {}", value,
						inputXPath, contextModelNode.getModelNodeId(), schemaNode == null ? null : schemaNode.getQName());
			}
			if (value instanceof Boolean) {
				return (boolean) value;
			} else if (value.equals(leafRefValue)) {
				return true;
			} else if (value instanceof Collection && !(((Collection) value).isEmpty()) && leafRefValue != null
					&& ((Collection) value).contains(leafRefValue)) {
				return true;
			} else if (!(value instanceof Collection && ((Collection) value).isEmpty()) && leafRefValue == null
					&& !(value instanceof Boolean)) {
				return true;
			}
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("validateXPathInModelNode returns false for xPath {} modelNode {} child {}", inputXPath,
					contextModelNode.getModelNodeId(), schemaNode == null ? null : schemaNode.getQName());
		}
		return false;
	}
    
	private Object setContextAndEvaluateModelNode(ModelNode contextModelNode, String leafRefValue,
			DataSchemaNode schemaNode, Object value, Expression ex) {
		ModelNodeDynaBean contextBean = (ModelNodeDynaBean) contextModelNode.getValue();
		String childName = schemaNode == null ? null
				: ModelNodeDynaBeanFactory.getDynaBeanAttributeName(schemaNode.getQName().getLocalName());
		String childNs = schemaNode == null ? null : schemaNode.getQName().getNamespace().toString();
		QName childQName = schemaNode == null ? null : schemaNode.getQName();
		DynaBean childBean;
		if (DataStoreValidationUtil.isReadable(contextBean, childName) && contextBean.get(childName) != null
				&& !ChoiceCaseNodeUtil.isChoiceOrCaseNode(schemaNode)) {
			value = fetchValueForNonChoiceOrCaseNodes(contextModelNode, schemaNode, value, ex, contextBean,
					childName, childNs, childQName);
		} else if (schemaNode instanceof ChoiceSchemaNode) {
            SchemaPath choicePath = ((ChoiceSchemaNode) schemaNode).getPath();
            QName choiceQName = choicePath.getLastComponent();
            DynaBean choiceBean = ModelNodeDynaBeanFactory.getDynaBeanForLeaf(contextModelNode, contextBean,
                    choiceQName.getLocalName(), null, choiceQName.getNamespace().toString());
			childBean = ModelNodeDynaBeanFactory.getDynaBeanForLeaf(contextModelNode, choiceBean, childName,
					null, childNs);
            DynaBean actualContextBean = (DynaBean) choiceBean.get(ModelNodeWithAttributes.PARENT);
			value = checkForFunctionsAndEvaluate(ex, actualContextBean, childBean, null, childQName);
		} else if (schemaNode instanceof CaseSchemaNode) {
			// if this is a case, create a bean for parent choice also
			SchemaPath choicePath = ((CaseSchemaNode) schemaNode).getPath().getParent();
			QName choiceQName = choicePath.getLastComponent();
			DynaBean choiceBean = ModelNodeDynaBeanFactory.getDynaBeanForLeaf(contextModelNode, contextBean,
					choiceQName.getLocalName(), null, choiceQName.getNamespace().toString());
			childBean = ModelNodeDynaBeanFactory.getDynaBeanForLeaf(contextModelNode, choiceBean, childName,
					null, childNs);
            DynaBean actualContextBean = (DynaBean) choiceBean.get(ModelNodeWithAttributes.PARENT);

			value = checkForFunctionsAndEvaluate(ex, actualContextBean, childBean, null, childQName);
		} else {
			Collection<SchemaPath> schemaPathToCreate = DataStoreValidationUtil.getValidationContext()
					.getSchemaPathsToCreate();
			if (schemaNode != null && schemaPathToCreate != null
					&& schemaPathToCreate.contains(schemaNode.getPath())) {
				// indicates a when condition checking for node to
				// create
				// it might have default value also.

				String defaultValue = null;
				if (schemaNode instanceof LeafSchemaNode) {
				    Optional<? extends Object> optDefaultValue = ((LeafSchemaNode) schemaNode).getType().getDefaultValue();
				    if (optDefaultValue.isPresent()) {
				        defaultValue = optDefaultValue.get().toString();
				    }
				}
				childBean = ModelNodeDynaBeanFactory.getDynaBeanForLeaf(contextModelNode, contextBean,
						childName, defaultValue, childNs);
				value = checkForFunctionsAndEvaluate(ex, childBean, childBean, leafRefValue, childQName);
			} else {
				if (childQName != null && schemaNode instanceof LeafListSchemaNode) {
					childBean = ModelNodeDynaBeanFactory.getDynaBeanForLeaf(contextModelNode,
							(DynaBean) contextModelNode.getValue(), childQName.getLocalName(), leafRefValue, childNs);
					value = checkForFunctionsAndEvaluate(ex, childBean, childBean, leafRefValue, childQName);
				} else {
					value = checkForFunctionsAndEvaluate(ex, contextBean, contextModelNode, leafRefValue, childQName);
				}
			}
		}
		return value;
	}

	private Object setContextForLeafAndEvaluate(ModelNode contextModelNode, String leafRefValue,
			Object currentContextNode, DataSchemaNode schemaNode, Expression ex) {
		Object value;
		QName leafQName;
		leafQName = schemaNode.getQName();
		Map<QName, ConfigLeafAttribute> attributes = ((ModelNodeWithAttributes) contextModelNode).getAttributes();
		DynaBean contextBean = null;
		for (QName attribute : attributes.keySet()) {
			if (schemaNode != null && attribute.equals(schemaNode.getQName())) {
				contextBean = ModelNodeDynaBeanFactory.getDynaBeanForLeaf(contextModelNode,
						(DynaBean) contextModelNode.getValue(), attribute.getLocalName(),
						attributes.get(attribute).getStringValue(), attribute.getNamespace().toString());
			}
		}
		if (contextBean != null) {
			value = checkForFunctionsAndEvaluate(ex, contextBean, contextBean, leafRefValue, leafQName);
		} else {
			if (leafQName != null) {
				contextBean = ModelNodeDynaBeanFactory.getDynaBeanForLeaf(contextModelNode,
						(DynaBean) contextModelNode.getValue(), leafQName.getLocalName(), leafRefValue,
						leafQName.getNamespace().toString());
				value = checkForFunctionsAndEvaluate(ex, contextBean, contextBean, leafRefValue, leafQName);
			} else {
				value = checkForFunctionsAndEvaluate(ex, (DynaBean) contextModelNode.getValue(), currentContextNode,
						leafRefValue, leafQName);
			}
		}
		return value;
	}
	
	@SuppressWarnings("unchecked")
	private Object fetchValueForNonChoiceOrCaseNodes(ModelNode contextModelNode, DataSchemaNode schemaNode,
			Object value, Expression ex, ModelNodeDynaBean contextBean, String childName, String childNs,
			QName childQName) {
		DynaBean childBean;
		if (schemaNode instanceof ContainerSchemaNode) {
			List<Object> childContainerBeans = (List<Object>) contextBean.get(childName);
			if (childContainerBeans != null) {
				for (Object childContainerBean : childContainerBeans) {
					value = checkForFunctionsAndEvaluate(ex, (DynaBean) childContainerBean, childContainerBean, null,
							childQName);
					if (value != null && !(value instanceof Boolean && !((Boolean) value))) {
						break;
					}
				}
			}
		} else if (schemaNode instanceof ListSchemaNode) {
		    List<Object> childListBeans = (List<Object>) contextBean.get(childName);
		    if (childListBeans != null) {
		        for (Object childListBean : childListBeans) {
		            value = checkForFunctionsAndEvaluate(ex, (DynaBean) childListBean, childListBean, null, childQName);
		            if (value != null && !(value instanceof Boolean && !((Boolean)value))) {
		                break;
		            }
		        }
		    }
		} else {
		    /**
		     * 1) if the ContextBean already has the qname 
		     *    a) if it is a collection, indicates ModelNodeWithAttributes#m_leafList. This
		     *       xpath has to be iterated over all the values in the list to check if it is true 
		     *    b) if it is a object, taken the string value convert it to leaf bean and validate the xpath 
		     * 2) If the contextBean does not have the qname, then currentContextBean and leafQname are null
		     */
		    Object beanObject = contextBean.get(childName);

		    if (beanObject instanceof Collection) {
		        Collection<Object> leafList = (Collection<Object>) beanObject;
		        for (Object leafObject : leafList) {
		            if (leafObject instanceof GenericConfigAttribute) {
		                childBean = ModelNodeDynaBeanFactory.getDynaBeanForLeaf(contextModelNode, contextBean, childName,
		                        ((GenericConfigAttribute)leafObject).getStringValue(), childNs);
		            } else {
		                childBean = ModelNodeDynaBeanFactory.getDynaBeanForLeaf(contextModelNode, contextBean, childName,
		                        leafObject.toString(), childNs);
		            }
		            value = checkForFunctionsAndEvaluate(ex, childBean, childBean, null, childQName);
		            if (value != null && !(value instanceof Boolean && !((Boolean)value))) {
		                break;
		            }
		        }
		    } else {
		        childBean = ModelNodeDynaBeanFactory.getDynaBeanForLeaf(contextModelNode, contextBean, childName, beanObject.toString(),
		                childNs);
		        value = checkForFunctionsAndEvaluate(ex, childBean, childBean, null, childQName);
		    }
		}
		return value;
	}
	
	private Object setContextAndEvaluteLocationPath(ModelNode contextModelNode, String leafRefValue,
			Object currentContextNode, DataSchemaNode schemaNode, Object value, Expression xPath, Expression ex) {
		
		ValidationTimingLogger.startConstraint(ValidationTimingLogger.ConstraintType.LEAFREF, ex.toString());
		SchemaRegistry registry = contextModelNode.getSchemaRegistry();
		try {
			logDebug("{} is identified to be a locationPath", xPath);
			Step[] steps = ((LocationPath) ex).getSteps();
			Map<QName, ConfigLeafAttribute> attributes = ((ModelNodeWithAttributes) contextModelNode).getAttributes();
			for (QName attribute : attributes.keySet()) {
				if (schemaNode != null && attribute.equals(schemaNode.getQName())) {
					DynaBean contextBean = ModelNodeDynaBeanFactory.getDynaBeanForLeaf(contextModelNode,
							(DynaBean) contextModelNode.getValue(), attribute.getLocalName(),
							attributes.get(attribute).getStringValue(), attribute.getNamespace().toString());
					List<Step> newSteps = new ArrayList<Step>();

					for (int i = 0; i < steps.length; i++) {
						if (steps[i].getAxis() != Compiler.AXIS_PARENT) {
							newSteps.add(steps[i]);
						} else {
							if (DataStoreValidationUtil.isReadable(contextBean, ModelNodeWithAttributes.PARENT)) {
								contextBean = (DynaBean) contextBean.get(ModelNodeWithAttributes.PARENT);
							} else if (i + 1 < steps.length) {
								// indicates we might have to look for a different root node/tree than where we have the constraint
								DataSchemaNode rootSchemaNode = DataStoreValidationUtil
										.getRootSchemaNode(registry, steps[++i]);
								contextBean = DataStoreValidationUtil.getRootModelNode(registry,
										rootSchemaNode);
							}
						}
					}

					if (SchemaRegistryUtil.isMountPointEnabled() && steps[0] != null
							&& steps[0].getAxis() != Compiler.AXIS_PARENT) {
						DataSchemaNode rootSchemaNode = DataStoreValidationUtil
								.getRootSchemaNode(SchemaRegistryUtil.getSchemaRegistry(m_schemaRegistry), steps[0]);
						if (rootSchemaNode != null) {
							if (DataStoreValidationUtil.isReadable(contextBean, ModelNodeWithAttributes.PARENT)) {
								DynaBean rootContextBean = (DynaBean) findRootModelNode(
										(DynaBean) contextBean.get(ModelNodeWithAttributes.PARENT));
								if (DataStoreValidationUtil.isReadable(rootContextBean,
										ModelNodeWithAttributes.PARENT)) {
									XmlModelNodeImpl xmlModelNode = (XmlModelNodeImpl) ((DynaBean) rootContextBean
											.get(ModelNodeWithAttributes.PARENT))
													.get(ModelNodeWithAttributes.MODEL_NODE);
									if (xmlModelNode != null
											&& xmlModelNode.getChildren().get(rootSchemaNode.getQName()) != null) {
										contextBean = (DynaBean) rootContextBean.get(ModelNodeWithAttributes.PARENT);
									}
								}

							}
						}
					}
					LocationPath newPath = new LocationPath(((LocationPath) ex).isAbsolute(),
							newSteps.toArray(new Step[0]));
					logDebug(
							"new path {} identified for xpath {} with contextBean {} currentNode {} leafRefValue {} attribute {}",
							newPath, xPath, contextBean, currentContextNode, leafRefValue, attribute);
					newPath = DataStoreValidationUtil.getDynaBeanAlignedPath(newPath);
					logDebug(
							"dyna bean aligned path {} identified for xpath {} with contextBean {} currentNode {} leafRefValue {} attribute {}",
							newPath, xPath, contextBean, currentContextNode, leafRefValue, attribute);
					value = checkForFunctionsAndEvaluate(newPath, contextBean, currentContextNode, leafRefValue,
							attribute);
					break;
				}
			}
			if (value == null) {
				value = setContextAndEvaluateModelNode(contextModelNode, leafRefValue, schemaNode, value, ex);
			}
			return value;
		} finally {
			ValidationTimingLogger.endConstraint(ValidationTimingLogger.ConstraintType.LEAFREF, ex.toString());
		}
	}
	
	/**
	 * Get Schema mount Root model node 
	 * @param parentContextBean
	 * @return
	 */
	private DynaBean findRootModelNode(DynaBean parentContextBean) {
		if (parentContextBean != null
				&& DataStoreValidationUtil.isReadable(parentContextBean, ModelNodeWithAttributes.MODEL_NODE)) {
			ModelNode modelNode = (ModelNode) parentContextBean.get(ModelNodeWithAttributes.MODEL_NODE);
			if (modelNode != null) {
				SchemaPath parentSchemaPath = modelNode.getModelNodeSchemaPath().getParent();
				SchemaNode parentSchemaNode = modelNode.getMountRegistry().getDataSchemaNode(parentSchemaPath);
				if (parentSchemaNode != null && DataStoreValidationUtil.isReadable(parentContextBean, ModelNodeWithAttributes.PARENT)) {
					return findRootModelNode((DynaBean) parentContextBean.get(ModelNodeWithAttributes.PARENT));
				}
			}
		}
		return parentContextBean;
	}
	

    public void registerValidators(Class<?> expression, DSExpressionValidator validator) {
        m_validators.put(expression, validator);
    }
    
    // This follows the rules of https://www.w3.org/TR/xpath/#function-boolean
    protected boolean convertToBoolean(Object value) {
        if (value == null) {
            return false;
        }
        else if (value instanceof Boolean) {
            return (Boolean)value;
        }
        else if (value instanceof Number) {
            return isNumberTrue((Number)value);
        }
        else if (value instanceof String) {
            String stringValue = (String)value;
            if ("true".equals(stringValue)) {
                return true;
            }
            else if ("false".equals(stringValue)) {
                return false;
            }
            else if (NumberUtils.isCreatable(stringValue)) {
                Number number = NumberUtils.createNumber(stringValue);
                return isNumberTrue(number);
            }
            else {
               return stringValue.length() > 0;
            }
        }
        else if (value instanceof Collection) {
            return ((Collection) value).size() > 0;
        }
        return false;
    }
    
    private boolean isNumberTrue(Number number) {
        if (number instanceof Byte || number instanceof Short || number instanceof Integer || number instanceof Long) {
            long longValue = number.longValue();
            return longValue != 0;
        }
        else if (number instanceof Float || number instanceof Double || number instanceof BigInteger || number instanceof BigDecimal) {
            double doubleValue = number.doubleValue();
            return doubleValue != 0 && doubleValue != Double.NaN;
        }
        return false;
    }
    
}
