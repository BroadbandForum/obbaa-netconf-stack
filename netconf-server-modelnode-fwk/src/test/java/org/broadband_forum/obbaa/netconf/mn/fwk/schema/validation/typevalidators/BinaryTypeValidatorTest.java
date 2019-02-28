package org.broadband_forum.obbaa.netconf.mn.fwk.schema.validation.typevalidators;

import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.junit.Test;


public class BinaryTypeValidatorTest extends AbstractTypeValidatorTest {
	@Test
	public void testBinary() throws NetconfMessageBuilderException {
		testPass("binaryvalidator/valid-binary.xml");
		
		testFail("binaryvalidator/invalid-binary-1.xml",
				"The argument is out of bounds <1, 1>, <3, 4>",
				"/validation:validation/validation:type-validation[validation:id='1']/validation:binary-type", "length-out-of-specified-bounds");
		
		testFail("binaryvalidator/invalid-binary-2.xml",
				"The argument is out of bounds <1, 1>, <3, 4>",
				"/validation:validation/validation:type-validation[validation:id='1']/validation:binary-type", "length-out-of-specified-bounds");
		
		testFail("binaryvalidator/invalid-binary-3.xml",
				"The argument is out of bounds <1, 1>, <3, 4>",
				"/validation:validation/validation:type-validation[validation:id='1']/validation:binary-type", "length-out-of-specified-bounds");
		
	}
	
	@Test
	public void testCustomBinary() throws NetconfMessageBuilderException {
		String validCustomReq1 = "<custom-binary-type>11111111</custom-binary-type>";
		testCustomPass(formRequestString(validCustomReq1));

		String validCustomReq2 = "<custom-binary-type>111111111111111111111111</custom-binary-type>";
		testCustomPass(formRequestString(validCustomReq2));
		
		String invalidCustomReq1 = "<custom-binary-type>11</custom-binary-type>";
		testCustomFail(formRequestString(invalidCustomReq1),
				"length constraint error-app-message",
				"/validation:validation/validation:type-validation[validation:id='1']/validation:custom-binary-type", "length constraint error-app-tag");
		
		String invalidCustomReq2 = "<custom-binary-type>1111111111111111</custom-binary-type>";
		testCustomFail(formRequestString(invalidCustomReq2),
				"length constraint error-app-message",
				"/validation:validation/validation:type-validation[validation:id='1']/validation:custom-binary-type", "length constraint error-app-tag");
	}
}
