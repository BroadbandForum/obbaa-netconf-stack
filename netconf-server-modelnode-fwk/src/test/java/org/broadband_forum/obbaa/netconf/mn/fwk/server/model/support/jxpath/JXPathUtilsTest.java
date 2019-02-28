package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.jxpath;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.JXPathCompiledExpression;
import org.apache.commons.jxpath.ri.compiler.Constant;
import org.apache.commons.jxpath.ri.compiler.CoreFunction;
import org.apache.commons.jxpath.ri.compiler.CoreOperationEqual;
import org.apache.commons.jxpath.ri.compiler.Expression;
import org.junit.Test;


/**
 * Created by vgotagi on 10/18/16.
 */
public class JXPathUtilsTest {

    @Test
    public void testGetExpression(){
        Expression[] expressions = new Expression[2];
        Expression exp = new CoreOperationEqual(expressions[0],expressions[1]);
        JXPathCompiledExpression jcExp= new JXPathCompiledExpression("xPath",exp);
        assertNotNull(JXPathUtils.getExpression(jcExp));
    }

    @Test
    public void testNull(){
        assertNull(JXPathUtils.getExpression((JXPathCompiledExpression)null));
    }
    
    @Test
    public void testGetConstantExpression() {
    	Constant result = JXPathUtils.getConstantExpression("test");
    	Object constantvalue = result.compute(null);
    	assertEquals(String.class, constantvalue.getClass());
		assertEquals("test", constantvalue);
    	
    	result = JXPathUtils.getConstantExpression("5.6");
    	constantvalue = result.compute(null);
    	assertEquals(Double.class, constantvalue.getClass());
    	assertEquals(5.6D, (Double)constantvalue, 1e-15);
    	
    	result = JXPathUtils.getConstantExpression("56");
    	constantvalue = result.compute(null);
    	assertEquals(Double.class, constantvalue.getClass());
    	assertEquals(56D, (Double)constantvalue, 1e-15);
    	
    	result = JXPathUtils.getConstantExpression("1.0.0");
    	constantvalue = result.compute(null);
    	assertEquals(String.class, constantvalue.getClass());
    	assertEquals("1.0.0", constantvalue);
    	
    	result = JXPathUtils.getConstantExpression("04/01");
    	constantvalue = result.compute(null);
    	assertEquals(String.class, constantvalue.getClass());
    	assertEquals("04/01", constantvalue);
    }
    
    @Test
    public void testGetCoreFunction() throws Exception{
    	Set<String> set = new HashSet<>(); 
    	Constant result = JXPathUtils.getConstantExpression("test");
    	Object[] values = new Object[]{true, false, set, result};
    	Object[] nullValues = new Object[]{null};
    	Object[] emptyValues = new Object[]{};
    	
    	JXPathUtils.getCoreFunction(4, values);
    	
    	JXPathUtils.getCoreFunction(0, nullValues);
    	
    	JXPathUtils.getCoreFunction(0, emptyValues);
    	
    	JXPathUtils.getCoreFunction(Compiler.FUNCTION_NOT, values);

    }
    
    @Test
    public void testStringArguments() {
        Object[] values = new Object[2];
        values[0] = "3.0";
        values[1] = ".";
        CoreFunction coreFunction = JXPathUtils.getCoreFunction(Compiler.FUNCTION_SUBSTRING_BEFORE, values);
        Object result = coreFunction.compute(null);
        // without the implementation of JXPathUtils.shouldBeStringArgument, this returns "", which is wrong
        assertEquals("3", result);
        
        assertEquals(3.0D, JXPathUtils.getConstantExpression("3.0", false).compute(null));
        assertEquals("3.0", JXPathUtils.getConstantExpression("3.0", true).compute(null));
    }
}
