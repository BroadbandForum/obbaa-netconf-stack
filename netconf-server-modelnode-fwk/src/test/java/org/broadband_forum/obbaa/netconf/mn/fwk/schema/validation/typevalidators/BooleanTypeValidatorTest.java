package org.broadband_forum.obbaa.netconf.mn.fwk.schema.validation.typevalidators;

import org.junit.Test;

import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;

public class BooleanTypeValidatorTest extends AbstractTypeValidatorTest {

	@Test
	public void testPass() throws NetconfMessageBuilderException {
		/*
		 * "true"
		 */
		testPass("booleanvalidator/valid-boolean-1.xml");
		/*
		 * "false"
		 */
		testPass("booleanvalidator/valid-boolean-2.xml");
	}

	@Test
	public void testFail() throws NetconfMessageBuilderException {
		/*
		 * "True"
		 */
		testFail("booleanvalidator/invalid-boolean-1.xml", "Invalid value. It should be \"true\" or \"false\" instead of \"True\"",
				"/validation:validation/validation:type-validation[validation:id='1']/validation:boolean-type", null);
		/*
		 * "False"
		 */
		testFail("booleanvalidator/invalid-boolean-2.xml", "Invalid value. It should be \"true\" or \"false\" instead of \"False\"",
				"/validation:validation/validation:type-validation[validation:id='1']/validation:boolean-type", null);
		/*
		 * "on"
		 */
		testFail("booleanvalidator/invalid-boolean-3.xml", "Invalid value. It should be \"true\" or \"false\" instead of \"on\"",
				"/validation:validation/validation:type-validation[validation:id='1']/validation:boolean-type", null);
	}
}
