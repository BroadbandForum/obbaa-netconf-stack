package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathInvalidSyntaxException;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.JXPathContextReferenceImpl;
import org.apache.commons.jxpath.ri.axes.RootContext;
import org.apache.commons.jxpath.ri.compiler.Constant;
import org.apache.commons.jxpath.ri.compiler.CoreFunction;
import org.apache.commons.jxpath.ri.compiler.Expression;
import org.apache.commons.jxpath.ri.compiler.LocationPath;
import org.apache.commons.jxpath.ri.compiler.Operation;
import org.apache.commons.jxpath.ri.compiler.Step;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeDynaBeanFactory;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.jxpath.JXPathUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.NetconfRpcErrorUtil;
import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcValidationException;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

/**
 * Takes care of evaluating core functions
 *
 */
public class DSFunctionValidator extends DSExpressionValidator {
    private final static AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(DSFunctionValidator.class, LogAppNames.NETCONF_STACK);

    public DSFunctionValidator(SchemaRegistry schemaRegistry, ModelNodeHelperRegistry modelNodeHelperRegistry, SubSystemRegistry registry,
            Map<Class<?>, DSExpressionValidator> validators) {
        this.m_schemaRegistry = schemaRegistry;
        this.m_modelNodeHelperRegistry = modelNodeHelperRegistry;
        this.m_subSystemRegistry = registry;
        setValidators(validators);
    }

    @Override
    protected Object evaluate(Expression xPathCondition, DynaBean contextBean, Object currentContextNode, String leafRefValue,
            QName leafQName) {
        if (DataStoreValidationUtil.isFunction(xPathCondition)) {
            Object returnValue =  getCoreFunction(contextBean, currentContextNode, null, leafQName, (CoreFunction) xPathCondition);
            return validateLeafRef(returnValue, leafRefValue);
        } else {
            return super.evaluate(xPathCondition, contextBean, currentContextNode, leafRefValue, leafQName);
        }
    }
    
    protected Object computeCount(DynaBean contextBean, Object currentContextNode, String leafRefValue, QName leafQName,
            CoreFunction expression) {
        Object returnValue = null;
        Expression[] expressions = expression.getArguments();
        Object[] objects = new Object[expressions.length];
        int index = 0;
        for (Expression exp : expressions) {
            if (exp instanceof CoreFunction) {
                objects[index++] = getCoreFunction(contextBean, currentContextNode, leafRefValue, leafQName, (CoreFunction) exp);
            } else if (exp instanceof LocationPath) {

                LocationPath path = DataStoreValidationUtil.getDynaBeanAlignedPath((LocationPath) exp);
                if (path.getSteps()[0].getAxis() != Compiler.AXIS_SELF) {
                    // this is specifically for expression like count(device)
                    // this is expensive, since all DynaBean has to explored
                    // in the entire tree and counted
                    int countIndex = 0;
                    DynaBean countContextBean = contextBean;
                    while (path.getSteps()[countIndex].getAxis() == Compiler.AXIS_PARENT) {
                        /*
                         * Increments the countIndex equal to the parent paths encountered in the expression
                         * Move up the hierarchy by fetching parent bean.
                         * eg. count(../../container1/child)
                         * countIndex = 2 , and grand parent bean will be fetched.
                         */
                        countIndex++;
                        if (DataStoreValidationUtil.isReadable(countContextBean, ModelNodeWithAttributes.PARENT)) {
                            countContextBean = (DynaBean) countContextBean.get(ModelNodeWithAttributes.PARENT);
                        } else if (countIndex < path.getSteps().length && path.getSteps()[countIndex].getAxis() == Compiler.AXIS_CHILD) {
                        	DataSchemaNode rootSchemaNode = DataStoreValidationUtil.getRootSchemaNode(m_schemaRegistry, path.getSteps()[countIndex]);
                            countContextBean = DataStoreValidationUtil.getRootModelNode(m_schemaRegistry, rootSchemaNode);
                            countIndex++;
                        }
                    }
                    Step step = path.getSteps()[path.getSteps().length-1];
                    String stepName = DataStoreValidationUtil.getLocalName(step);
                    countOccurenceOfStepInTree(countContextBean, path.getSteps(), currentContextNode, leafRefValue,
                            leafQName, countIndex);
                    Integer countValue = getCountFromCacheFor(stepName);
                    setCountToCacheFor(stepName, null);
                    RequestScope.getCurrentScope().putInCache(DataStoreValidationUtil.SINGLE_STEP_COUNT, null);
                    return countValue;
                } else {
                    Object value = evaluate(exp, contextBean, currentContextNode, leafRefValue, leafQName);
                    objects[index++] = value;
                }
            } else {
                objects[index++] = evaluate(exp, contextBean, currentContextNode, leafRefValue, leafQName);
            }
        }

        CoreFunction function = JXPathUtils.getCoreFunction(expression.getFunctionCode(), objects);
        returnValue = computeFunction(contextBean, function, leafQName);
        return returnValue;
    }

   
    protected boolean containsPath(Expression expression) {
    	if (DataStoreValidationUtil.isLocationPath(expression) || DataStoreValidationUtil.isExpressionPath(expression)) {
    		return true;
    	} else if (DataStoreValidationUtil.isOperation(expression)) {
    		Expression[] expressions = ((Operation)expression).getArguments();
    		for (Expression exp:expressions) {
    			if (containsPath(exp)) {
    				return true;
    			}
    		}
    	} 
    	return false;
    }
    
    
    protected Object getCoreFunction(DynaBean contextBean, Object currentContextNode, String leafRefValue, QName leafQName,
            CoreFunction expression) {
        Object returnValue = null;
        if (expression.getFunctionCode() == Compiler.FUNCTION_COUNT) {
            // if it is count() function
            returnValue = computeCount(contextBean, currentContextNode, leafRefValue, leafQName, expression);
        } else {
            if (expression.getArgumentCount() == 1 && DataStoreValidationUtil.isLocationPath(expression.getArg1())) {
                // if it is a single argument function SUM()
                returnValue = computeSingleArgumentFunction(contextBean, expression, leafQName);
            } else {
                // functions like contains(locationPath, value)
                Object[] objects = new Object[expression.getArgumentCount()];
                int index = 0;
                for (Expression exp:expression.getArguments()) {
                	Expression newExp = exp;
                    Object value = null;
                    value = evaluate(newExp, contextBean, currentContextNode, leafRefValue, leafQName);
                    if (value instanceof DynaBean) {
                        objects[index++] = ((DynaBean) value).get(ModelNodeWithAttributes.LEAF_VALUE);
                    } else if (value == null && isConstantTreatedAsLocationPath(exp, index)){
                        objects[index++] = exp.toString();
                    } else {
                        objects[index++] = value;
                    }
                }
                CoreFunction function = JXPathUtils.getCoreFunction(expression.getFunctionCode(), objects);
                returnValue = computeFunction(contextBean, function, leafQName);
            }
            
        }
        return returnValue;

    }

    protected Object computeSingleArgumentFunction(DynaBean contextBean, CoreFunction expression, QName leafQName) {
        Object returnValue = null;
        LocationPath locationPath = ((LocationPath)expression.getArg1());
        DynaBean nextBean = contextBean;
        CoreFunction function = expression;
        if (locationPath.isAbsolute()) {
            // if it is an absolute path, we need to be at the root. 
            ModelNode rootNode = getRootNodeFor(locationPath);
            if (rootNode != null) {
                nextBean = (DynaBean) rootNode.getValue();            
                
                LocationPath newLocationPath = DataStoreValidationUtil.excludeFirstStep(locationPath);
                Object[] objects = new Object[1];
                objects[0] = newLocationPath;
                function = JXPathUtils.getCoreFunction(expression.getFunctionCode(), objects);
            } else {
                LOGGER.warn(" there is no root modelNode for expression {}", expression);
            }
        } else if (locationPath.getSteps().length > 0 && locationPath.getSteps()[0].getAxis() == Compiler.AXIS_PARENT) {
            // if the path starts with a parent path, we need to discard them
            // and set the correct path
            Step[] steps = locationPath.getSteps();
            Step[] newSteps = null;
            int i=0;
            for (;i<steps.length && steps[i].getAxis() == Compiler.AXIS_PARENT;i++) {
                // as long we see a parent, we iterate
                if (DataStoreValidationUtil.isReadable(nextBean, ModelNodeWithAttributes.PARENT)) {
                    nextBean = (DynaBean) nextBean.get(ModelNodeWithAttributes.PARENT);
                } else {
                    // invalid path. return null;
                    LOGGER.warn("No path available for expression {} contextBean {}", expression, contextBean);
                    return returnValue;
                }
            }
            newSteps = new Step[steps.length-i];
            System.arraycopy(steps, i, newSteps, 0, steps.length-i);
            LocationPath newLocationPath = new LocationPath(false, newSteps);
            Object[] objects = new Object[1];
            objects[0] = newLocationPath;
            function = JXPathUtils.getCoreFunction(expression.getFunctionCode(), objects);
        }
        returnValue = computeFunction(nextBean, function, leafQName);
        return returnValue;
    }
    
    protected boolean hasOnlyEmptySteps(CoreFunction function) {
    	Expression expression = function.getArg1();
    	if (function.getArgumentCount() == 1 && expression instanceof LocationPath && ((LocationPath)expression).getSteps().length == 0) {
    		return true;
    	}
    	return false;
    }

	protected Object computeFunction(DynaBean contextBean, CoreFunction function, QName leafQName) {

		int fCode = function.getFunctionCode();
		if (fCode == Compiler.FUNCTION_LOCAL_NAME) {
			if (isFunctionWithoutArgs(function)) {
				if (DataStoreValidationUtil.isReadable(contextBean, leafQName.getLocalName())) {
					return leafQName.getLocalName();
				}
			} else if (hasOnlyEmptySteps(function)) {
				// special case: JXPath returns "root" for local-name of root object, so we have to compute the correct value ourselves
				// we'll delegate local-name for something in the subtree still to JXPath
				ModelNodeWithAttributes node = (ModelNodeWithAttributes) contextBean.get(ModelNodeWithAttributes.MODEL_NODE);
				return node.getQName().getLocalName();
			}
		} else if (fCode == Compiler.FUNCTION_NAME && hasOnlyEmptySteps(function)) {
			// similar for the name function
			ModelNodeWithAttributes node = (ModelNodeWithAttributes) contextBean.get(ModelNodeWithAttributes.MODEL_NODE);
			QName qName = node.getQName();
            SchemaRegistry registry = SchemaRegistryUtil.getSchemaRegistry(node, m_schemaRegistry);
			Optional<Module> module = registry.findModuleByNamespaceAndRevision(qName.getNamespace(),
					qName.getRevision().orElse(null));
			String prefix = module.get().getPrefix();
			return prefix + ":" + node.getQName().getLocalName();
		} else if (fCode == Compiler.FUNCTION_NAMESPACE_URI) {
			// similar for the namespace function
			if (isFunctionWithoutArgs(function)) {
				if (contextBean.get(ModelNodeWithAttributes.NAMESPACE).equals(leafQName.getNamespace().toString())) {
					return leafQName.getNamespace().toString();
				}
			} else if (hasOnlyEmptySteps(function)) {
				ModelNodeWithAttributes node = (ModelNodeWithAttributes) contextBean.get(ModelNodeWithAttributes.MODEL_NODE);
				QName qName = node.getQName();
				return qName.getNamespace().toString();
			}
		} else if (fCode == Compiler.FUNCTION_STRING || fCode == Compiler.FUNCTION_STRING_LENGTH
				|| fCode == Compiler.FUNCTION_NORMALIZE_SPACE || fCode == Compiler.FUNCTION_NUMBER) {
			if (isFunctionWithoutArgs(function)) {
				if (DataStoreValidationUtil.isReadable(contextBean, leafQName.getLocalName())) {
					/*
					 * Converting function() -> function(current())
					 * eg. , if current() = test
					 * then , string() -> string('test')
					 */
					Expression newExpressions[] = new Expression[function.getArgumentCount()+1];
					Constant newConstant = new Constant(contextBean.get(leafQName.getLocalName()).toString());
					newExpressions[0] = newConstant;
					CoreFunction currentNodeValueFunction = new CoreFunction(fCode, newExpressions);
					return computeFunction(contextBean, currentNodeValueFunction, leafQName);
				}
			} 
		}
		return computeFunctionValueFromContext(contextBean, function);
	}

	private Object computeFunctionValueFromContext(DynaBean contextBean, CoreFunction function) {
		try {
            Object returnValue = null;
            JXPathContext context = JXPathContext.newContext(contextBean);
            if (context instanceof JXPathContextReferenceImpl && context.getContextPointer() instanceof NodePointer) {
            	returnValue = function
            			.computeValue(new RootContext((JXPathContextReferenceImpl) context, (NodePointer) context.getContextPointer()));
            }
            return returnValue;
        } catch (JXPathInvalidSyntaxException e) {
            throw new RpcValidationException(NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.OPERATION_FAILED,
                    "Invalid JXPath syntax: " + e.getMessage()));
        }
	}

	private boolean isFunctionWithoutArgs(CoreFunction function) {
		if(function.getArgumentCount()==0){
			return true;
		}
		return false;
	}

	@SuppressWarnings("rawtypes")
	protected void countOccurenceOfStepInTree(DynaBean contextBean, Step[] step, Object currentContextNode,
			String leafRefValue, QName leafQName, int index) {
		/*
		 * Here the expression is broken down into steps and processed further.
		 * eg. count(/container/list[enabled='true']/childNode
		 * 
		 * steps : container, list[enabled] , childNode 
		 * For moving down the hierarchy "index" is incremented by 1. 
		 * For moving towards up thehierarchy(towards parent) , index is decremented by 1 , and parent
		 * bean is passed as an argument
		 * 
		 * Movement towards parent(up the hierarchy) is done until
		 * "appAugmentedPath" is reached
		 * 
		 */

		if (index < 0) {
			String stepName = DataStoreValidationUtil.getLocalName(step[0]);
			if (contextBean.getDynaClass().getName()
					.equals(ModelNodeDynaBeanFactory.getModelNodeAttributeName(stepName))) {
				index = 1;
				if (index > step.length - 1) {
					if (step.length > 1) {
						Integer countValue = getCountFromCacheFor(stepName);
						if(countValue == null){
							countValue = 0;
						}
						setCountToCacheFor(stepName, countValue + 1);
					}
					checkCountOnParent(contextBean, step, currentContextNode, leafRefValue, leafQName, index);
					return;
				}
			} else {
				/*
				 * Check for presence in bean else check in parent bean
				 */
				if (DataStoreValidationUtil.isReadable(contextBean, stepName)) {
					Integer countValue = getCountFromCacheFor(stepName);
					if(countValue == null){
						countValue = 0;
					}
					setCountToCacheFor(stepName, countValue + 1);
				} else {
					checkCountOnParent(contextBean, step, currentContextNode, leafRefValue, leafQName, index);
				}
				return;
			}
		}
		if (index > step.length - 1) {
			return;
		}
		String stepName = DataStoreValidationUtil.getLocalName(step[index]);
		if (!isBeanTraversed(contextBean, stepName)) {
			Integer countValue = getCountFromCacheFor(stepName);
			if (index == step.length - 1) {
				if (countValue == null) {
					countValue = 0;
					setCountToCacheFor(stepName, countValue);
				}

				if (contextBean.getDynaClass().getName()
						.equals(ModelNodeDynaBeanFactory.getModelNodeAttributeName(stepName))) {
					countValue++;
					setCountToCacheFor(stepName, countValue);
				}
			}

			if (DataStoreValidationUtil.isReadable(contextBean, stepName)) {
				Object value = contextBean.get(stepName);
				if (value != null) {
					if (value instanceof Collection) {
						for (Object object : ((Collection) value)) {
							if (object instanceof DynaBean) {
								if (step[index].getPredicates() == null || step[index].getPredicates().length < 1) {
									/*
									 * For simple cases like count(list1) Here
									 * the step does not requires further
									 * evaluation due to lack of predicates.
									 */
									countOccurenceOfStepInTree((DynaBean) object, step, currentContextNode,
											leafRefValue, leafQName, index + 1);
									if (index == step.length - 1) {
										countValue++;
									}
									setCountToCacheFor(stepName, countValue);
								} else {
									/*
									 * For cases like list1[enabled='true'] Here
									 * the step requires further evaluation as
									 * it contains predicates.
									 */
									boolean allPredicateYes = true;
									for (Expression predicate : step[index].getPredicates()) {
										Object result = evaluate(predicate, (DynaBean) object, currentContextNode,
												leafRefValue, leafQName);
										if (result == null || (result instanceof Boolean && !((Boolean) result))) {
											allPredicateYes = false;
											break;
										}
									}
									if (allPredicateYes && index == step.length - 1) {
										countValue++;
										setCountToCacheFor(stepName, countValue);
									} else if (allPredicateYes) {
										countOccurenceOfStepInTree((DynaBean) object, step, currentContextNode,
												leafRefValue, leafQName, index + 1);
									}
								}
							} else if (index == step.length - 1) {
								countValue++;
								setCountToCacheFor(stepName, countValue);
							}
						}
					} else if (value instanceof DynaBean) {
					    boolean predicatesValid = true;
					    Expression[] predicates = step[index].getPredicates();
					    if (predicates != null && predicates.length > 0) {
					        // if step has a condition. Like key of a list
					        // interface[name = 'interface']
					        for (Expression predicate:predicates) {
					            Object returnValue = evaluate(predicate, (DynaBean) value, currentContextNode, leafRefValue, leafQName);
					            if (returnValue != null) {
					                if (returnValue instanceof Boolean) {
					                    predicatesValid = predicatesValid && ((Boolean)returnValue);
					                } 
					            }
					            
					            if (!predicatesValid) {
					                break;
					            }
					        }
					    }
					    
					    if (predicatesValid) {
	                        if (index == step.length - 1) {
	                            countValue++;
	                            setCountToCacheFor(stepName, countValue);
	                        }
	                        countOccurenceOfStepInTree((DynaBean) value, step, currentContextNode, leafRefValue, leafQName,
	                                index + 1);
					    }
					} else {
						if (index == step.length - 1) {
							countValue++;
							setCountToCacheFor(stepName, countValue);
						}
					}
				}
			}

			checkCountOnParent(contextBean, step, currentContextNode, leafRefValue, leafQName, index);
		}
	}

	private void checkCountOnParent(DynaBean contextBean, Step[] step, Object currentContextNode, String leafRefValue,
			QName leafQName, int index) {
		if (DataStoreValidationUtil.isReadable(contextBean, ModelNodeWithAttributes.PARENT)) {
			DynaBean parentBean = (DynaBean) contextBean.get(ModelNodeWithAttributes.PARENT);
			if (parentBean != null) {
				ModelNode parentModelNode = (ModelNode) parentBean.get(ModelNodeWithAttributes.MODEL_NODE);
				SchemaPath parentSchemaPath = parentModelNode.getModelNodeSchemaPath();
				if (!isParentAppAugmentedSchemaPath(parentSchemaPath)) {
					countOccurenceOfStepInTree(parentBean, step, currentContextNode, leafRefValue, leafQName,
							index - 1);
				}
			}
		}
	}

	private boolean isParentAppAugmentedSchemaPath(SchemaPath parentSchemaPath) {
		Map<SchemaPath, String> appAugmentedPathsToComponentMap = SchemaRegistryUtil.getSchemaRegistry(m_schemaRegistry)
				.retrieveAppAugmentedPathToComponent();
		if (!appAugmentedPathsToComponentMap.isEmpty()
				&& appAugmentedPathsToComponentMap.containsKey(parentSchemaPath)) {
			return true;
		}
		return false;
	}
    
    protected Integer getCountFromCacheFor(String stepName) {
        Integer countValue = (Integer) RequestScope.getCurrentScope().getFromCache("count_"+stepName);
        return countValue;
    }
    
    protected void setCountToCacheFor(String stepName, Integer countValue) {
        RequestScope.getCurrentScope().putInCache("count_"+stepName, countValue);
        
    }

    @SuppressWarnings("unchecked")
    protected boolean isBeanTraversed(DynaBean contextBean, String stepName) {
        Set<ModelNodeId> traversedBean = (Set<ModelNodeId>) RequestScope.getCurrentScope().getFromCache(DataStoreValidationUtil.SINGLE_STEP_COUNT);
        if (traversedBean == null) {
            traversedBean = new HashSet<ModelNodeId>();
            RequestScope.getCurrentScope().putInCache(DataStoreValidationUtil.SINGLE_STEP_COUNT, traversedBean);
            setCountToCacheFor(stepName,null);
        }
        
        ModelNode modelNode = null;
        if (DataStoreValidationUtil.isReadable(contextBean, ModelNodeWithAttributes.MODEL_NODE)) {
            modelNode = (ModelNode) contextBean.get(ModelNodeWithAttributes.MODEL_NODE);
        }
        
        if (modelNode != null && traversedBean.contains(modelNode.getModelNodeId())) {
            return true;
        } else if (modelNode != null) {
            traversedBean.add(modelNode.getModelNodeId());
        }
        
        return false;
        
    }
    
    protected boolean isConstantTreatedAsLocationPath(Expression expression, Integer argumentIndex) {
        if (argumentIndex > 0 && DataStoreValidationUtil.isLocationPath(expression)) {
            // In a function, the second argument tends to be a constant and we recompute the constant, it will treated as a 
            // LocationPath unless it is a Number
            LocationPath path = (LocationPath) expression;
            if (path.getSteps().length == 1) {
                // if it is a locationPath, we will likely get more than 1 step to evalute a target node
                return true;
            }
        }
        return false;
    }

}
