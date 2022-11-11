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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.jxpath;

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
import org.apache.commons.lang3.StringUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationUtil;
import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private static XPath m_xPath = XPathFactory.newInstance().newXPath();
    private static final String XPATH_EXPRESSION_LOCAL_NAME_FORMAT = "/*[local-name()='%s']";
    private static final String XPATH_EXPRESSION_LOCAL_VALUE_FORMAT = "[*[local-name()='%s' and . %s %s]]";

    public static final String PIPE = "|";
    public static final String PIPE_REGEX = "\\|";
    public static final String SLASH = "/";
    public static final String COLON = ":";
    public static final String EMPTY = "";
    public static final String SQUARE_BRACE_OPEN = "[";
    public static final String SQUARE_BRACE_CLOSE = "]";

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
     * @param values
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
            if (functionCode == Compiler.FUNCTION_NOT || functionCode == Compiler.FUNCTION_BOOLEAN ) {
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
                    constantValue = new String(); // for boolean function, we should not send "string value of false", it treats it as string and by length it evaluates the value to true.
                }
            } else if (value instanceof Collection) {
                constantValue = String.valueOf(((Collection) value).size());
                if(functionCode == Compiler.FUNCTION_COUNT){
                    functionCode = Compiler.FUNCTION_NUMBER;
                }
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

    public static Expression getExpressionWithoutCache(String xPathCondition) {
        return getExpression((JXPathCompiledExpression) JXPathContext.compile(xPathCondition));
    }

    public static boolean isInvalidFilter(String filter) {
        if (StringUtils.isEmpty(filter)) {
            return false;
        }

        // filter=/a/b='1'|/c/d='2' -> having PIPE (|)
        // filters = [ /a/b='1' ,  /e/f='2' ]
        // Need to loop and validate every single filter
        if (isBracketOutsideQuote(filter)) {
            LOGGER.error("XPath {} predicate special characters [ or ] is present outside single/double quotes", filter);
            return true;
        }

        String[] paths = filter.split(PIPE_REGEX);
        for (String path : paths) {
            if (StringUtils.isNotEmpty(path)) {
                Expression expression = buildExpression(path.trim());
                if (expression == null) {
                    return true;
                }
                if (expression instanceof CoreOperationCompare || expression instanceof CoreOperationRelationalExpression) {
                    CoreOperation operation = (CoreOperation) expression;
                    if (operation.getArguments()[0] != null && operation.getArguments()[0].toString().contains(COLON)) {
                        LOGGER.error("Invalid XPath {}. The axes '{}' is specified in the form of <prefix>:<name>", path, operation.getArguments()[0].toString());
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static Expression buildExpression(String xPath) {
        try {
            return JXPathUtils.getExpression((JXPathCompiledExpression) JXPathContext.compile(xPath));
        } catch (Exception e) {
            LOGGER.error("Error while compile xPath {}, ", xPath, e);
            return null;
        }
    }

    public static boolean isNotificationMatchesXpath(Document notification, String xPath, String xPathExpression) throws Exception {
        NodeList nodeList = getListDataFromXpath(notification, xPathExpression);

        if (nodeList != null && nodeList.getLength() > 0) {
            LOGGER.debug("Found the notification {} is matched with xPath {}", notification, xPath);
            return true;
        } else {
            LOGGER.debug("The notification {} does not match with xPath {}", notification, xPath);
            return false;
        }
    }

    public static String buildExpressionFromXpath(String xPathStr) {
        if (StringUtils.isEmpty(xPathStr)) {
            return null;
        }
        Set<String> expressions = new HashSet<>();
        String[] xPaths = xPathStr.contains(PIPE) ? xPathStr.split(PIPE_REGEX) : new String[]{ xPathStr };
        for (String xPath : xPaths) {
            String[] paths = xPath.split(SLASH);
            StringBuilder xPathBuilder = new StringBuilder();
            for (String path : paths) {
                if (StringUtils.isNotEmpty(path)) {
                    Expression expression = buildExpression(path);
                    // CoreOperationCompare: =,  !=
                    // CoreOperationRelationalExpression: <, <=, >, >=
                    if (expression instanceof CoreOperationCompare || expression instanceof CoreOperationRelationalExpression) {
                        CoreOperation operation = (CoreOperation) expression;
                        xPathBuilder.append(String.format(XPATH_EXPRESSION_LOCAL_VALUE_FORMAT, operation.getArguments()[0], operation.getSymbol(), operation.getArguments()[1]));
                    } else {
                        xPathBuilder.append(String.format(XPATH_EXPRESSION_LOCAL_NAME_FORMAT, path));
                    }
                }
            }
            expressions.add(xPathBuilder.toString());
        }

        return StringUtils.join(expressions, " | ");
    }

    private static NodeList getListDataFromXpath(Document document, String xPathExpression) {
        try {
            return (NodeList) m_xPath.evaluate(xPathExpression, document, XPathConstants.NODESET); // NOSONAR;
        } catch (XPathExpressionException e) {
            LOGGER.error("Failed to fetch data from XPath expression {}", xPathExpression, e);
        }
        return null;
    }

    private static boolean isBracketOutsideQuote(String xPathStr) {
        String[] xPaths = xPathStr.split(PIPE_REGEX);
        for (String xPath : xPaths) {
            xPath = xPath.startsWith(SLASH) ? xPath.substring(1) : xPath;
            String[] paths = xPath.split(SLASH);
            for(int i = 0; i< paths.length; i++) {
                String content = paths[i];
                String temp = content;
                //double quote
                Pattern pattern = Pattern.compile("\"([^\"]*)\"");
                Matcher matcher = pattern.matcher(content);
                while (matcher.find()) {
                    temp = temp.replace(matcher.group(1), EMPTY);
                }
                //single quote
                pattern = Pattern.compile("\'([^\"]*)\'");
                matcher = pattern.matcher(content);
                while (matcher.find()) {
                    temp = temp.replace(matcher.group(1), EMPTY);
                }
                if(temp.contains(SQUARE_BRACE_OPEN) || temp.contains(SQUARE_BRACE_CLOSE)) {
                    return true;
                }
            }
        }
        return false;
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
