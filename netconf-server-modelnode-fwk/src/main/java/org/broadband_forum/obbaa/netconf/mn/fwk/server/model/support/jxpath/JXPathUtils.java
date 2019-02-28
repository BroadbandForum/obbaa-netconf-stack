package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.jxpath;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.JXPathCompiledExpression;
import org.apache.commons.jxpath.ri.compiler.Constant;
import org.apache.commons.jxpath.ri.compiler.CoreFunction;
import org.apache.commons.jxpath.ri.compiler.CoreOperation;
import org.apache.commons.jxpath.ri.compiler.CoreOperationCompare;
import org.apache.commons.jxpath.ri.compiler.CoreOperationDivide;
import org.apache.commons.jxpath.ri.compiler.CoreOperationMod;
import org.apache.commons.jxpath.ri.compiler.CoreOperationMultiply;
import org.apache.commons.jxpath.ri.compiler.CoreOperationNegate;
import org.apache.commons.jxpath.ri.compiler.CoreOperationRelationalExpression;
import org.apache.commons.jxpath.ri.compiler.CoreOperationSubtract;
import org.apache.commons.jxpath.ri.compiler.Expression;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationUtil;
import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;

public class JXPathUtils {
	
	private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(JXPathUtils.class, LogAppNames.NETCONF_STACK);
	
	private static final Map<Integer, List<Integer>> XPATH_FUNCTION_STRING_ARGS = new HashMap<>();
	
	static {
	    XPATH_FUNCTION_STRING_ARGS.put(Compiler.FUNCTION_CONCAT, Arrays.asList(0, 1, 2));
	    XPATH_FUNCTION_STRING_ARGS.put(Compiler.FUNCTION_STARTS_WITH, Arrays.asList(0, 1));
	    XPATH_FUNCTION_STRING_ARGS.put(Compiler.FUNCTION_CONTAINS, Arrays.asList(0, 1));
	    XPATH_FUNCTION_STRING_ARGS.put(Compiler.FUNCTION_SUBSTRING_BEFORE, Arrays.asList(0, 1));
	    XPATH_FUNCTION_STRING_ARGS.put(Compiler.FUNCTION_SUBSTRING_AFTER, Arrays.asList(0, 1));
	    XPATH_FUNCTION_STRING_ARGS.put(Compiler.FUNCTION_SUBSTRING, Arrays.asList(0));
	    XPATH_FUNCTION_STRING_ARGS.put(Compiler.FUNCTION_STRING_LENGTH, Arrays.asList(0));
	    XPATH_FUNCTION_STRING_ARGS.put(Compiler.FUNCTION_NORMALIZE_SPACE, Arrays.asList(0));
	    XPATH_FUNCTION_STRING_ARGS.put(Compiler.FUNCTION_TRANSLATE, Arrays.asList(0, 1, 2));
	}

	private static final String EXPRESSION_CACHE = "EXPRESSION_CACHE";
	public static Expression getExpression(JXPathCompiledExpression compiledExpression) {
		if (compiledExpression != null) {
			try {
				Class<? extends JXPathCompiledExpression> clazz = compiledExpression.getClass();
				Method method;
				method = clazz.getDeclaredMethod("getExpression");
				method.setAccessible(true);
				return (Expression) method.invoke(compiledExpression);
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				LOGGER.error("Error while getting expression", e);
			}
		}
		return null;
	}

	/**
	 * Give an object, it checks if its a numeric value or not and returns a Constant Expression of JXPath api
	 * @param constantValue
	 * @return
	 */
	public static Constant getConstantExpression(Object constantValue) {
	    return getConstantExpression(constantValue, false);
	}
	
    public static Constant getConstantExpression(Object constantValue, boolean keepAsString) {
        Constant returnValue = null;
        if (constantValue != null) {
            String constantStringValue = constantValue.toString();
			if (!keepAsString && !constantStringValue.isEmpty() && constantStringValue.matches(DataStoreValidationUtil.NUMERIC)) {
                returnValue = new Constant(Double.parseDouble(constantStringValue));
            } else {
                returnValue = new Constant(constantStringValue);
            }
        }
        return returnValue;
    }

    /**
     * Given a JXPath function code and a list of object, a CoreFunction is returned
     * @param value
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static CoreFunction getCoreFunction(int functionCode, Object[] values) {
        Expression[] expressions = new Expression[values == null ? 1 : values.length];
        int index = 0;
        for (; index < values.length ; ) {
            Object value = values[index];
            if(value==null){
                //if the value is null, change it to JXPath Constant expression with null.
                //Null values will lead to NPE.
                expressions[index++] = new Constant((String)null);
                continue;
            }
            String constantValue = DataStoreValidationUtil.isConstant(value) ? ((Constant)value).computeValue(null).toString() : value.toString();
            if (functionCode == Compiler.FUNCTION_NOT) {
                /*
                 * reference: http://saxon.sourceforge.net/saxon6.5.3/expressions.html#BooleanExpressions 
                 * Numeric values: 0 is treated as false, everything else as true. 
                 * String values: the zero-length string is treated as false, everything else as true.
                 * Node-sets: the empty node set is treated as false, everything else as true.
                 * 
                 * Note: in the current ModelNodeDynaBean, for not(), JXPath does not know to reach out to parent and evaluate the path
                 * directly unlike other core functions.
                 */
                if (value instanceof Boolean && !((Boolean) value)) {
                    constantValue = new String();
                }
            } else if (value instanceof Collection) {
                constantValue = String.valueOf(((Collection) value).size());
            }
    
            if (DataStoreValidationUtil.isExpression(value)){
                expressions[index++] = (Expression) value;
            } else {
                boolean keepAsString = shouldBeStringArgument(functionCode, index);
                expressions[index++] = getConstantExpression(constantValue, keepAsString);
            }
        }
        
        CoreFunction function = new CoreFunction(functionCode, expressions);
        return function;
    }

    private static boolean shouldBeStringArgument(int functionCode, int index) {
        if (XPATH_FUNCTION_STRING_ARGS.containsKey(functionCode)) {
            List<Integer> argIndices = XPATH_FUNCTION_STRING_ARGS.get(functionCode);
            return argIndices.contains(index);
        }
        return false;
    }

    /**
     * For the list of expression supplied, a new CoreOperation is built which is of same type as input operation
     * Eg: If the input operation is CoreOperationDivide and expressions are '../a/b/c','../a/b' or '../a/b/c', '9' or '10','0'
     * returned value will be a CoreOperationDivide of {'../a/b/c','../a/b'}, {'../a/b/c','9'}, {'10','0'}
     */
    @SuppressWarnings("rawtypes")
    public static CoreOperation getCoreOperation(CoreOperation operation, Expression[] expressions) {
        Expression[] expressionInput = expressions;
        CoreOperation newOperation;
        try {
            Class[] klasses = null;
            Object[] values = null;
            if (operation instanceof CoreOperationCompare || 
                    operation instanceof CoreOperationRelationalExpression ||
                    operation instanceof CoreOperationDivide ||
                    operation instanceof CoreOperationMod ||
                    operation instanceof CoreOperationMultiply ||
                    operation instanceof CoreOperationSubtract){
                /**
                 * The compare and arithmetic always works with two arguments exception for addition
                 */
                klasses = new Class[2];
                klasses[0] = klasses[1] = Expression.class;
                values = new Object[2];
                values[0] = expressionInput[0];
                values[1] = expressionInput[1];
            } else if (operation instanceof CoreOperationNegate) {
                klasses = new Class[1];
                klasses[0] = Expression.class;
                values = new Object[1];
                values[0] = expressionInput[0];
            } else {
                klasses = new Class[1];
                klasses[0] = Expression[].class;
                values = new Object[1];
                values[0] = expressionInput;
            }
            newOperation = (CoreOperation) (operation.getClass().getDeclaredConstructor(klasses).newInstance(values));
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            throw new RuntimeException("Unable to compute expression for " + operation.toString(), e);
        }
        return newOperation;
    }

    /**
     * Given a String, builds an equivalent JXPath for the same
     * 
     * @param xPathCondition
     * @return
     */
    public static Expression getExpression(String xPathCondition) {
        Expression returnValue = getExpressionFromCache(xPathCondition);
        if (returnValue == null && xPathCondition != null) {
            returnValue = getExpression((JXPathCompiledExpression) JXPathContext.compile(xPathCondition)); 
            cacheExpression(xPathCondition, returnValue);
        }
        return returnValue;
    }
    
    @SuppressWarnings("unchecked")
    private static Expression getExpressionFromCache(String xPathCondition) {
        RequestScope currentScope = RequestScope.getCurrentScope();
        Map<String, Expression> expressionMap = (Map<String, Expression>) currentScope.getFromCache(EXPRESSION_CACHE);
        if (expressionMap == null) {
            expressionMap = new HashMap<String, Expression>(2000);
            currentScope.putInCache(EXPRESSION_CACHE, expressionMap);
        }
        return expressionMap.get(xPathCondition);
    }
    
    private static void cacheExpression(String xPathCondition, Expression expression) {
        RequestScope currentScope = RequestScope.getCurrentScope();
        Map<String, Expression> expressionMap = (Map<String, Expression>) currentScope.getFromCache(EXPRESSION_CACHE);
        if (expressionMap != null) {
            expressionMap.put(xPathCondition, expression);
        }
    }
}
