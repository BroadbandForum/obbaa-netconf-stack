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

public class DecimalTypeValidatorTest extends AbstractTypeValidatorTest {
	
	@Test
	public void testFraction1Validator() throws NetconfMessageBuilderException {
		testPass("decimalvalidator/valid-decimal64-fraction1.xml");
		
		testFail("decimalvalidator/invalid-decimal64-5.xml", 
				"The argument is out of bounds <-922337203685477580.8, -92233720368547758.08>, <1, 3.14>, <20, 922337203685477580.7>", "/validation:validation/validation:type-validation[validation:id='1']/validation:decimal64-fraction1-type", "range-out-of-specified-bounds");

		testFail("decimalvalidator/invalid-decimal64-6.xml", 
				"The argument is out of bounds <-922337203685477580.8, -92233720368547758.08>, <1, 3.14>, <20, 922337203685477580.7>", "/validation:validation/validation:type-validation[validation:id='1']/validation:decimal64-fraction1-type", "range-out-of-specified-bounds");

		testFail("decimalvalidator/invalid-decimal64-7.xml", 
				"The argument is out of bounds <-922337203685477580.8, -92233720368547758.08>, <1, 3.14>, <20, 922337203685477580.7>", "/validation:validation/validation:type-validation[validation:id='1']/validation:decimal64-fraction1-type", "range-out-of-specified-bounds");

		testFail("decimalvalidator/invalid-decimal64-8.xml", 
				"The argument is out of bounds <-922337203685477580.8, -92233720368547758.08>, <1, 3.14>, <20, 922337203685477580.7>", "/validation:validation/validation:type-validation[validation:id='1']/validation:decimal64-fraction1-type", "range-out-of-specified-bounds");
		
		testFail("decimalvalidator/invalid-decimal64-9.xml", 
				"The argument is out of bounds <-922337203685477580.8, 922337203685477580.7>", "/validation:validation/validation:type-validation[validation:id='1']/validation:decimal64-fraction1-without-range-type", "range-out-of-specified-bounds");

		testFail("decimalvalidator/invalid-decimal64-10.xml", 
				"The argument is out of bounds <-922337203685477580.8, 922337203685477580.7>", "/validation:validation/validation:type-validation[validation:id='1']/validation:decimal64-fraction1-without-range-type", "range-out-of-specified-bounds");

		testFail("decimalvalidator/invalid-decimal64-11.xml", 
				"The argument is out of bounds <-922337203685477580.8, 922337203685477580.7>", "/validation:validation/validation:type-validation[validation:id='1']/validation:decimal64-fraction1-without-range-type", "range-out-of-specified-bounds");

		testFail("decimalvalidator/invalid-decimal64-12.xml",
		        "The argument is out of bounds <-922337203685477580.8, 922337203685477580.7>", "/validation:validation/validation:type-validation[validation:id='1']/validation:decimal64-fraction1-with-single-range-type", "range-out-of-specified-bounds");

		testFail("decimalvalidator/invalid-decimal64-14.xml",
		        "The argument is out of bounds <1.1, 2.1>", "/validation:validation/validation:type-validation[validation:id='1']/validation:decimal64-fraction1-with-overridden-range-type", "range-out-of-specified-bounds");

		testFail("decimalvalidator/invalid-decimal64-13.xml",
                "The argument is out of bounds <-922337203685477580.8, -92233720368547758.08>, <1, 3.14>, <20, 922337203685477580.7>", "/validation:validation/validation:type-validation[validation:id='1']/validation:decimal64-fraction1-type", "range-out-of-specified-bounds");

		testFail("decimalvalidator/invalid-decimal64-15.xml",
		        "The argument is out of bounds <-922337203685477580.8, -92233720368547758.08>, <1, 3.14>, <20, 922337203685477580.7>", "/validation:validation/validation:type-validation[validation:id='1']/validation:decimal64-fraction1-type", "range-out-of-specified-bounds");
	}
	
	@Test
	public void testFraction18Validator() throws NetconfMessageBuilderException {
		testPass("decimalvalidator/valid-decimal64-fraction18.xml");
		
		testFail("decimalvalidator/invalid-decimal64-1.xml", 
				"The argument is out of bounds <-9.223372036854775808, 3.14>, <5.1, 9.223372036854775807>", "/validation:validation/validation:type-validation[validation:id='1']/validation:decimal64-fraction18-type", "range-out-of-specified-bounds");
		
		testFail("decimalvalidator/invalid-decimal64-2.xml", 
				"The argument is out of bounds <-9.223372036854775808, 3.14>, <5.1, 9.223372036854775807>", "/validation:validation/validation:type-validation[validation:id='1']/validation:decimal64-fraction18-type", "range-out-of-specified-bounds");
		
		testFail("decimalvalidator/invalid-decimal64-3.xml", 
				"The argument is out of bounds <-9.223372036854775808, 9.223372036854775807>", "/validation:validation/validation:type-validation[validation:id='1']/validation:decimal64-fraction18-without-range-type", "range-out-of-specified-bounds");
		
		testFail("decimalvalidator/invalid-decimal64-4.xml", 
				"The argument is out of bounds <-9.223372036854775808, 9.223372036854775807>", "/validation:validation/validation:type-validation[validation:id='1']/validation:decimal64-fraction18-without-range-type", "range-out-of-specified-bounds");
		}
	
	@Test
	public void testCustomFraction18Validator() throws NetconfMessageBuilderException {
		String validCustomReq1 = "<custom-decimal64-fraction18-type>-9.223372036854775808</custom-decimal64-fraction18-type>";
		testCustomPass(formRequestString(validCustomReq1));
	
		String validCustomReq2 = "<custom-decimal64-fraction18-type>3.13</custom-decimal64-fraction18-type>";
		testCustomPass(formRequestString(validCustomReq2));
		
		String invalidCustomReq1 = "<custom-decimal64-fraction18-type>3.15</custom-decimal64-fraction18-type>";
		testCustomFail(formRequestString(invalidCustomReq1),
				"range constraint error-app-message",
				"/validation:validation/validation:type-validation[validation:id='1']/validation:custom-decimal64-fraction18-type", "range constraint error-app-tag");
	}
	
	@Test
	public void testCustomFraction1Validator() throws NetconfMessageBuilderException {
		String validCustomReq1 = "<custom-decimal64-fraction1-type>-922337203685477580.8</custom-decimal64-fraction1-type>";
		testCustomPass(formRequestString(validCustomReq1));
	
		String validCustomReq2 = "<custom-decimal64-fraction1-type>3.13</custom-decimal64-fraction1-type>";
		testCustomPass(formRequestString(validCustomReq2));

		String validCustomReq3 = "<custom-decimal64-fraction1-with-disjoint-range-type>3.1</custom-decimal64-fraction1-with-disjoint-range-type>";
		testCustomPass(formRequestString(validCustomReq3));

		String validCustomReq4 = "<custom-decimal64-fraction1-with-overridden-range-type>1.2</custom-decimal64-fraction1-with-overridden-range-type>";
		testCustomPass(formRequestString(validCustomReq4));

		String invalidCustomReq1 = "<custom-decimal64-fraction1-type>3.15</custom-decimal64-fraction1-type>";
		testCustomFail(formRequestString(invalidCustomReq1),
				"range constraint error-app-message",
				"/validation:validation/validation:type-validation[validation:id='1']/validation:custom-decimal64-fraction1-type", "range constraint error-app-tag");
	
	    String invalidCustomReq2 = "<custom-decimal64-fraction1-with-disjoint-range-type>5.1</custom-decimal64-fraction1-with-disjoint-range-type>";
	    testCustomFail(formRequestString(invalidCustomReq2),
                "range constraint The argument provided is out of bounds",
                "/validation:validation/validation:type-validation[validation:id='1']/validation:custom-decimal64-fraction1-with-disjoint-range-type", "range constraint range-out-of-specified-bounds");

	    String invalidCustomReq3 = "<custom-decimal64-fraction1-with-overridden-range-type>0.1</custom-decimal64-fraction1-with-overridden-range-type>";
	    testCustomFail(formRequestString(invalidCustomReq3),
                "range constraint The argument provided is out of bounds",
                "/validation:validation/validation:type-validation[validation:id='1']/validation:custom-decimal64-fraction1-with-overridden-range-type", "range constraint range-out-of-specified-bounds");
	}
}
