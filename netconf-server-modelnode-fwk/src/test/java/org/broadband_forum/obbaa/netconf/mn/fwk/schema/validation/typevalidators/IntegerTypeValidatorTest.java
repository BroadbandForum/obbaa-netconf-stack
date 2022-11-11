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

public class IntegerTypeValidatorTest extends AbstractTypeValidatorTest {
	
	@Test
	public void testInt8() throws NetconfMessageBuilderException {
		testPass("integervalidator/valid-int8.xml");
		
		testFail("integervalidator/invalid-int8-1.xml",
				"The argument is out of bounds <-128, 0>, <10, 100>, <120, 127>", "/validation:validation/validation:type-validation[validation:id='1']/validation:int8-type", "range-out-of-specified-bounds");
		
		testFail("integervalidator/invalid-int8-2.xml",
				"The argument is out of bounds <-128, 0>, <10, 100>, <120, 127>", "/validation:validation/validation:type-validation[validation:id='1']/validation:int8-type", "range-out-of-specified-bounds");
		
		testFail("integervalidator/invalid-int8-3.xml",
				"The argument is out of bounds <-128, 0>, <10, 100>, <120, 127>",
				"/validation:validation/validation:type-validation[validation:id='1']/validation:int8-type", "range-out-of-specified-bounds");
		
		testFail("integervalidator/invalid-int8-4.xml",
				"The argument is out of bounds <-128, 0>, <10, 100>, <120, 127>",
				"/validation:validation/validation:type-validation[validation:id='1']/validation:int8-type", "range-out-of-specified-bounds");
	}
	
	@Test
	public void testCustomInt8() throws NetconfMessageBuilderException {
		String validCustomReq1 = "<custom-int8-type>120</custom-int8-type>";
		testCustomPass(formRequestString(validCustomReq1));
	
		String validCustomReq2 = "<custom-int8-type>0</custom-int8-type>";
		testCustomPass(formRequestString(validCustomReq2));
		
		String invalidCustomReq1 = "<custom-int8-type>-130</custom-int8-type>";
		testCustomFail(formRequestString(invalidCustomReq1),
				"range constraint error-app-message",
				"/validation:validation/validation:type-validation[validation:id='1']/validation:custom-int8-type", "range constraint error-app-tag");
	}
	
	@Test
	public void testInt16() throws NetconfMessageBuilderException {
		testPass("integervalidator/valid-int16.xml");
		
		testFail("integervalidator/invalid-int16-1.xml",
				"The argument is out of bounds <10, 1000>", "/validation:validation/validation:type-validation[validation:id='1']/validation:int16-type", "range-out-of-specified-bounds");
		
		testFail("integervalidator/invalid-int16-2.xml",
				"The argument is out of bounds <10, 1000>", "/validation:validation/validation:type-validation[validation:id='1']/validation:int16-type", "range-out-of-specified-bounds");
		
		testFail("integervalidator/invalid-int16-3.xml",
				"The argument is out of bounds <10, 1000>",
				"/validation:validation/validation:type-validation[validation:id='1']/validation:int16-type", "range-out-of-specified-bounds");
		
		testFail("integervalidator/invalid-int16-4.xml",
				"The argument is out of bounds <10, 1000>",
				"/validation:validation/validation:type-validation[validation:id='1']/validation:int16-type", "range-out-of-specified-bounds");
		
		testFail("integervalidator/invalid-int16-5.xml",
				"The argument is out of bounds <10, 1000>",
				"/validation:validation/validation:type-validation[validation:id='1']/validation:int16-type", "range-out-of-specified-bounds");
		
		testFail("integervalidator/invalid-int16-6.xml",
				"The argument is out of bounds <10, 1000>", "/validation:validation/validation:type-validation[validation:id='1']/validation:int16-type-ref", "range-out-of-specified-bounds");
		
		testFail("integervalidator/invalid-int16-7.xml",
				"The argument is out of bounds <10, 1000>", "/validation:validation/validation:type-validation[validation:id='1']/validation:int16-type-ref", "range-out-of-specified-bounds");
		
		testFail("integervalidator/invalid-int16-8.xml",
				"The argument is out of bounds <10, 1000>",
				"/validation:validation/validation:type-validation[validation:id='1']/validation:int16-type-ref", "range-out-of-specified-bounds");
		
		testFail("integervalidator/invalid-int16-9.xml",
				"The argument is out of bounds <10, 1000>",
				"/validation:validation/validation:type-validation[validation:id='1']/validation:int16-type-ref", "range-out-of-specified-bounds");
		
		testFail("integervalidator/invalid-int16-10.xml",
				"The argument is out of bounds <10, 1000>",
				"/validation:validation/validation:type-validation[validation:id='1']/validation:int16-type-ref", "range-out-of-specified-bounds");
	}
	
	@Test
	public void testCustomInt16() throws NetconfMessageBuilderException {
		String validCustomReq1 = "<custom-int16-type>120</custom-int16-type>";
		testCustomPass(formRequestString(validCustomReq1));
		
		String invalidCustomReq1 = "<custom-int16-type>-32770</custom-int16-type>";
		testCustomFail(formRequestString(invalidCustomReq1),
				"range constraint error-app-message",
				"/validation:validation/validation:type-validation[validation:id='1']/validation:custom-int16-type", "range constraint error-app-tag");
	}
	
	@Test
	public void testInt32() throws NetconfMessageBuilderException {
		testPass("integervalidator/valid-int32.xml");
		
		testFail("integervalidator/invalid-int32-1.xml",
				"The argument is out of bounds <-2147483648, 2147483647>", "/validation:validation/validation:type-validation[validation:id='1']/validation:int32-type", "range-out-of-specified-bounds");
		
		testFail("integervalidator/invalid-int32-2.xml",
				"The argument is out of bounds <-2147483648, 2147483647>", "/validation:validation/validation:type-validation[validation:id='1']/validation:int32-type", "range-out-of-specified-bounds");
		
		testFail("integervalidator/invalid-int32-3.xml",
				"The argument is out of bounds <-2147483648, 2147483647>", "/validation:validation/validation:type-validation[validation:id='1']/validation:int32-type", "range-out-of-specified-bounds");
		
		testFail("integervalidator/invalid-int32-4.xml",
				"The argument is out of bounds <-2147483648, 2147483647>", "/validation:validation/validation:type-validation[validation:id='1']/validation:int32-type-ref", "range-out-of-specified-bounds");
		
		testFail("integervalidator/invalid-int32-5.xml",
				"The argument is out of bounds <-2147483648, 2147483647>", "/validation:validation/validation:type-validation[validation:id='1']/validation:int32-type-ref", "range-out-of-specified-bounds");
		
		testFail("integervalidator/invalid-int32-6.xml",
				"The argument is out of bounds <-2147483648, 2147483647>", "/validation:validation/validation:type-validation[validation:id='1']/validation:int32-type-ref", "range-out-of-specified-bounds");
	}
	
	@Test
	public void testInt64() throws NetconfMessageBuilderException {
		testPass("integervalidator/valid-int64.xml");
		
		testFail("integervalidator/invalid-int64-1.xml",
				"The argument is out of bounds <-9223372036854775808, 9223372036854775807>", "/validation:validation/validation:type-validation[validation:id='1']/validation:int64-type", "range-out-of-specified-bounds");
		
		testFail("integervalidator/invalid-int64-2.xml",
				"The argument is out of bounds <-9223372036854775808, 9223372036854775807>", "/validation:validation/validation:type-validation[validation:id='1']/validation:int64-type", "range-out-of-specified-bounds");
	}
}
