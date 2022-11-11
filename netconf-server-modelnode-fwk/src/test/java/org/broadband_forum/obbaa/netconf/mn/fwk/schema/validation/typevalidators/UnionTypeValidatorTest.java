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

package org.broadband_forum.obbaa.netconf.mn.fwk.schema.validation.typevalidators;

import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.junit.Test;

public class UnionTypeValidatorTest extends AbstractTypeValidatorTest {
	
	@Test
	public void testValidUnion() throws NetconfMessageBuilderException {
		testPass("unionvalidator/valid-union.xml");
	}
	
	@Test
	public void testInValidUnion() throws NetconfMessageBuilderException {
		testFail("unionvalidator/invalid-union-1.xml", 
				"The argument is out of bounds <-128, 0>, <10, 100>, <120, 127> or Value \"9\" is an invalid value. Expected values: [union]", "/validation:validation/validation:type-validation[validation:id='1']/validation:union-type", "range-out-of-specified-bounds");
		
		testFail("unionvalidator/invalid-union-2.xml", 
				"The argument is out of bounds <-128, 0>, <10, 100>, <120, 127> or Value \"128\" is an invalid value. Expected values: [union]", "/validation:validation/validation:type-validation[validation:id='1']/validation:union-type", "range-out-of-specified-bounds");
		
		testFail("unionvalidator/invalid-union-3.xml", 
				"The argument is out of bounds <-128, 0>, <10, 100>, <120, 127> or Value \"abc\" is an invalid value. Expected values: [union]", "/validation:validation/validation:type-validation[validation:id='1']/validation:union-type", "range-out-of-specified-bounds");
		
		testFail("unionvalidator/invalid-union-4.xml", 
				"The argument is out of bounds <-128, 0>, <10, 100>, <120, 127> or Value \"\" is an invalid value. Expected values: [union]", "/validation:validation/validation:type-validation[validation:id='1']/validation:union-type", "range-out-of-specified-bounds");
	}
	
	@Test
	public void testCustomUnion() throws NetconfMessageBuilderException {
		String validCustomReq1 = "<custom-union-type>100</custom-union-type>";
		testCustomPass(formRequestString(validCustomReq1));
	
		String validCustomReq2 = "<custom-union-type>10</custom-union-type>";
		testCustomPass(formRequestString(validCustomReq2));
		
		String validCustomReq3 = "<custom-union-type>union</custom-union-type>";
		testCustomPass(formRequestString(validCustomReq3));
		
		String invalidCustomReq1 = "<custom-union-type>8</custom-union-type>";
		testCustomFail(formRequestString(invalidCustomReq1),
				"range constraint error-app-message or Value \"8\" is an invalid value. Expected values: [union]",
				"/validation:validation/validation:type-validation[validation:id='1']/validation:custom-union-type", "range constraint error-app-tag");
		
		String invalidCustomReq2 = "<custom-union-type>test</custom-union-type>";
		testCustomFail(formRequestString(invalidCustomReq2),
				"range constraint error-app-message or Value \"test\" is an invalid value. Expected values: [union]",
				"/validation:validation/validation:type-validation[validation:id='1']/validation:custom-union-type", "range constraint error-app-tag");
	}
	
	@Test
	public void testErrorWithoutAppTag() throws NetconfMessageBuilderException {
        String invalidCustomReq1 = "<union-type-without-app-tag>8</union-type-without-app-tag>";
        testCustomFail(formRequestString(invalidCustomReq1),
                "Invalid value. It should be \"true\" or \"false\" instead of \"8\"",
                "/validation:validation/validation:type-validation[validation:id='1']/validation:union-type-without-app-tag", "");
	    
	}
}
