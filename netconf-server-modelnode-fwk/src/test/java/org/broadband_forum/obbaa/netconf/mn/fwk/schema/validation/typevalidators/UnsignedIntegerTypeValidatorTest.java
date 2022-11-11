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


public class UnsignedIntegerTypeValidatorTest extends AbstractTypeValidatorTest {

	@Test
	public void testUint8() throws NetconfMessageBuilderException {
		testPass("unsignedintegervalidator/valid-uint8.xml");
		
		testFail("unsignedintegervalidator/invalid-uint8-1.xml",
				"The argument is out of bounds <0, 10>, <20, 100>, <120, 255>", "/validation:validation/validation:type-validation[validation:id='1']/validation:uint8-type", "range-out-of-specified-bounds");
		
		testFail("unsignedintegervalidator/invalid-uint8-2.xml",
				"The argument is out of bounds <0, 10>, <20, 100>, <120, 255>", "/validation:validation/validation:type-validation[validation:id='1']/validation:uint8-type", "range-out-of-specified-bounds");
		
		testFail("unsignedintegervalidator/invalid-uint8-3.xml",
				"The argument is out of bounds <0, 10>, <20, 100>, <120, 255>",
				"/validation:validation/validation:type-validation[validation:id='1']/validation:uint8-type", "range-out-of-specified-bounds");
		
		testFail("unsignedintegervalidator/invalid-uint8-4.xml",
				"The argument is out of bounds <0, 10>, <20, 100>, <120, 255>",
				"/validation:validation/validation:type-validation[validation:id='1']/validation:uint8-type", "range-out-of-specified-bounds");
	}

	@Test
	public void testCustomUInt8() throws NetconfMessageBuilderException {
		String validCustomReq1 = "<custom-uint8-type>120</custom-uint8-type>";
		testCustomPass(formRequestString(validCustomReq1));
	
		String validCustomReq2 = "<custom-uint8-type>0</custom-uint8-type>";
		testCustomPass(formRequestString(validCustomReq2));
		
		String invalidCustomReq1 = "<custom-uint8-type>-130</custom-uint8-type>";
		testCustomFail(formRequestString(invalidCustomReq1),
				"range constraint error-app-message",
				"/validation:validation/validation:type-validation[validation:id='1']/validation:custom-uint8-type", "range constraint error-app-tag");
	}
	
	@Test
	public void testUint16() throws NetconfMessageBuilderException {
		testPass("unsignedintegervalidator/valid-uint16.xml");
		
		testFail("unsignedintegervalidator/invalid-uint16-1.xml",
				"The argument is out of bounds <10, 10000>", "/validation:validation/validation:type-validation[validation:id='1']/validation:uint16-type", "range-out-of-specified-bounds");
		
		testFail("unsignedintegervalidator/invalid-uint16-2.xml",
				"The argument is out of bounds <10, 10000>", "/validation:validation/validation:type-validation[validation:id='1']/validation:uint16-type", "range-out-of-specified-bounds");
		
		testFail("unsignedintegervalidator/invalid-uint16-3.xml",
				"The argument is out of bounds <10, 10000>",
				"/validation:validation/validation:type-validation[validation:id='1']/validation:uint16-type", "range-out-of-specified-bounds");
		
		testFail("unsignedintegervalidator/invalid-uint16-4.xml",
				"The argument is out of bounds <10, 10000>",
				"/validation:validation/validation:type-validation[validation:id='1']/validation:uint16-type", "range-out-of-specified-bounds");
		
		testFail("unsignedintegervalidator/invalid-uint16-5.xml",
				"The argument is out of bounds <10, 10000>",
				"/validation:validation/validation:type-validation[validation:id='1']/validation:uint16-type", "range-out-of-specified-bounds");
		
		testFail("unsignedintegervalidator/invalid-uint16-6.xml",
				"The argument is out of bounds <10, 10000>", "/validation:validation/validation:type-validation[validation:id='1']/validation:uint16-type-ref", "range-out-of-specified-bounds");
		
		testFail("unsignedintegervalidator/invalid-uint16-7.xml",
				"The argument is out of bounds <10, 10000>", "/validation:validation/validation:type-validation[validation:id='1']/validation:uint16-type-ref", "range-out-of-specified-bounds");
		
		testFail("unsignedintegervalidator/invalid-uint16-8.xml",
				"The argument is out of bounds <10, 10000>",
				"/validation:validation/validation:type-validation[validation:id='1']/validation:uint16-type-ref", "range-out-of-specified-bounds");
		
		testFail("unsignedintegervalidator/invalid-uint16-9.xml",
				"The argument is out of bounds <10, 10000>",
				"/validation:validation/validation:type-validation[validation:id='1']/validation:uint16-type-ref", "range-out-of-specified-bounds");
		
		testFail("unsignedintegervalidator/invalid-uint16-10.xml",
				"The argument is out of bounds <10, 10000>",
				"/validation:validation/validation:type-validation[validation:id='1']/validation:uint16-type-ref", "range-out-of-specified-bounds");
		
	}
	
	@Test
	public void testCustomUInt16() throws NetconfMessageBuilderException {
		String validCustomReq1 = "<custom-uint16-type>120</custom-uint16-type>";
		testCustomPass(formRequestString(validCustomReq1));
		
		String invalidCustomReq1 = "<custom-uint16-type>-1</custom-uint16-type>";
		testCustomFail(formRequestString(invalidCustomReq1),
				"range constraint error-app-message",
				"/validation:validation/validation:type-validation[validation:id='1']/validation:custom-uint16-type", "range constraint error-app-tag");
	}
	
	@Test
	public void testUint32() throws NetconfMessageBuilderException {
		testPass("unsignedintegervalidator/valid-uint32.xml");
		
		testFail("unsignedintegervalidator/invalid-uint32-1.xml",
				"The argument is out of bounds <0, 4294967295>", "/validation:validation/validation:type-validation[validation:id='1']/validation:uint32-type", "range-out-of-specified-bounds");
		
		testFail("unsignedintegervalidator/invalid-uint32-2.xml",
				"The argument is out of bounds <0, 4294967295>", "/validation:validation/validation:type-validation[validation:id='1']/validation:uint32-type", "range-out-of-specified-bounds");
		
		testFail("unsignedintegervalidator/invalid-uint32-3.xml",
				"The argument is out of bounds <0, 4294967295>", "/validation:validation/validation:type-validation[validation:id='1']/validation:uint32-type", "range-out-of-specified-bounds");
		
		testFail("unsignedintegervalidator/invalid-uint32-4.xml",
				"The argument is out of bounds <0, 4294967295>", "/validation:validation/validation:type-validation[validation:id='1']/validation:uint32-type-ref", "range-out-of-specified-bounds");
		
		testFail("unsignedintegervalidator/invalid-uint32-5.xml",
				"The argument is out of bounds <0, 4294967295>", "/validation:validation/validation:type-validation[validation:id='1']/validation:uint32-type-ref", "range-out-of-specified-bounds");
		
		testFail("unsignedintegervalidator/invalid-uint32-6.xml",
				"The argument is out of bounds <0, 4294967295>", "/validation:validation/validation:type-validation[validation:id='1']/validation:uint32-type-ref", "range-out-of-specified-bounds");
	}
	
	@Test
	public void testUint64() throws NetconfMessageBuilderException {
		testPass("unsignedintegervalidator/valid-uint64.xml");
		
		testFail("unsignedintegervalidator/invalid-uint64-1.xml",
				"The argument is out of bounds <0, 18446744073709551615>", "/validation:validation/validation:type-validation[validation:id='1']/validation:uint64-type", "range-out-of-specified-bounds");
		
		testFail("unsignedintegervalidator/invalid-uint64-2.xml",
				"The argument is out of bounds <0, 18446744073709551615>", "/validation:validation/validation:type-validation[validation:id='1']/validation:uint64-type", "range-out-of-specified-bounds");
		
	}
}
