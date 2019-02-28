package org.broadband_forum.obbaa.netconf.mn.fwk.schema.validation.typevalidators;

import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.junit.Test;

public class EnumerationTypeValidatorTest extends AbstractTypeValidatorTest {
	
	@Test
	public void testValidEnum() throws NetconfMessageBuilderException {
		testPass("enumvalidator/valid-enum.xml");
	}
	
	@Test
	public void testInvalidEnum() throws NetconfMessageBuilderException {
		testFail("enumvalidator/invalid-enum-1.xml", 
				"Value \"abc\" is an invalid value. Expected values: [unknown, value1, value2]", "/validation:validation/validation:type-validation[validation:id='1']/validation:enum-type", null);
		
		testFail("enumvalidator/invalid-enum-2.xml", 
				"Value \"\" is an invalid value. Expected values: [unknown, value1, value2]", "/validation:validation/validation:type-validation[validation:id='1']/validation:enum-type", null);
		
		testFail("enumvalidator/invalid-enum-3.xml", 
				"Value \"abc\" is an invalid value. Expected values: [failed, complete]", "/validation:validation/validation:type-validation[validation:id='1']/validation:enum-ref-type", null);
	}

}
