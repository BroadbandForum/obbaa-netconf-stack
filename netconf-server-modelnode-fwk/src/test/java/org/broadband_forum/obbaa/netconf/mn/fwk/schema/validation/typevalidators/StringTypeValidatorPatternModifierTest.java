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

public class StringTypeValidatorPatternModifierTest extends AbstractTypeValidatorTest {

	@Test
	public void testPatternModifier_StringType() throws NetconfMessageBuilderException {

		// cases for multiple patterns with modifier
		String validCustomReq = "<pattern-modifier-leaf>ab</pattern-modifier-leaf>";
		testCustomPass(getRequestString(validCustomReq));

		validCustomReq = "<pattern-modifier-leaf>abc</pattern-modifier-leaf>";
		testCustomPass(getRequestString(validCustomReq));

		validCustomReq = "<pattern-modifier-leaf>abcd</pattern-modifier-leaf>";
		testCustomPass(getRequestString(validCustomReq));

		validCustomReq = "<pattern-modifier-leaf>xl9</pattern-modifier-leaf>";
		testCustomPass(getRequestString(validCustomReq));

		validCustomReq = "<pattern-modifier-leaf>xLab</pattern-modifier-leaf>";
		testCustomPass(getRequestString(validCustomReq));

		// validate exceed the length of the string
		String invalidCustomReq = "<pattern-modifier-leaf>acdbn</pattern-modifier-leaf>";
		testCustomFail(getRequestString(invalidCustomReq), "length constraint error-app-message",
				"/pattern-modifier-test:validation/pattern-modifier-test:type-validation[pattern-modifier-test:id='1']/pattern-modifier-test:pattern-modifier-leaf",
				"length constraint error-app-tag");

		// Cases for pattern modifier
		invalidCustomReq = "<pattern-modifier-leaf>10</pattern-modifier-leaf>";
		testCustomFail(getRequestString(invalidCustomReq),
				"Supplied value does not match the regular expression ^(?:[axX][blL].*)$.",
				"/pattern-modifier-test:validation/pattern-modifier-test:type-validation[pattern-modifier-test:id='1']/pattern-modifier-test:pattern-modifier-leaf",
				"invalid-regular-expression");

		invalidCustomReq = "<pattern-modifier-leaf>1L</pattern-modifier-leaf>";
		testCustomFail(getRequestString(invalidCustomReq),
				"Supplied value does not match the regular expression ^(?:[axX][blL].*)$.",
				"/pattern-modifier-test:validation/pattern-modifier-test:type-validation[pattern-modifier-test:id='1']/pattern-modifier-test:pattern-modifier-leaf",
				"invalid-regular-expression");

		// example from RFC
		validCustomReq = "<rfc-pattern-modifier-leaf>enable</rfc-pattern-modifier-leaf>";
		testCustomPass(getRequestString(validCustomReq));

		validCustomReq = "<rfc-pattern-modifier-leaf>bbf1234</rfc-pattern-modifier-leaf>";
		testCustomPass(getRequestString(validCustomReq));

		invalidCustomReq = "<rfc-pattern-modifier-leaf>10-bit</rfc-pattern-modifier-leaf>";
		testCustomFail(getRequestString(invalidCustomReq),
				"Supplied value does not match the regular expression ^(?:[a-zA-Z_][a-zA-Z0-9\\-_.]*)$.",
				"/pattern-modifier-test:validation/pattern-modifier-test:type-validation[pattern-modifier-test:id='1']/pattern-modifier-test:rfc-pattern-modifier-leaf",
				"invalid-regular-expression");

		invalidCustomReq = "<rfc-pattern-modifier-leaf>xml-element</rfc-pattern-modifier-leaf>";
		testCustomFail(getRequestString(invalidCustomReq),
				"pattern modifier constraint error-app-message",
				"/pattern-modifier-test:validation/pattern-modifier-test:type-validation[pattern-modifier-test:id='1']/pattern-modifier-test:rfc-pattern-modifier-leaf",
				"pattern modifier constraint error-app-tag");
	}

	/**
	 * Cover all cases of typedef for patterns with modifier invert-match
	 * @throws NetconfMessageBuilderException
	 */
	@Test
	public void testPatternModifier_TypeDefString() throws NetconfMessageBuilderException {
		/**
		 * Override the patterns from typedef of 'custom-type-pattern-modifier'
		 *
		 * leaf custom-typedef-pattern-modifier-leaf {
		 * 		type custom-type-pattern-modifier {
		 * 			pattern '[0-9]';
		 * 			pattern '[^5-9]' {
		 * 				modifier invert-match;
		 * 			}
		 * 		}
		 * }
		 */
		String validCustomReq = "<custom-typedef-pattern-modifier-leaf>8</custom-typedef-pattern-modifier-leaf>";
		testCustomPass(getRequestString(validCustomReq));

		String invalidCustomReq = "<custom-typedef-pattern-modifier-leaf>a</custom-typedef-pattern-modifier-leaf>";
		testCustomFail(getRequestString(invalidCustomReq),
				"Supplied value does not match the regular expression ^(?:[0-9])$.",
				"/pattern-modifier-test:validation/pattern-modifier-test:type-validation[pattern-modifier-test:id='1']/pattern-modifier-test:custom-typedef-pattern-modifier-leaf",
				"invalid-regular-expression");

		invalidCustomReq = "<custom-typedef-pattern-modifier-leaf>x</custom-typedef-pattern-modifier-leaf>";
		testCustomFail(getRequestString(invalidCustomReq),
				"Supplied value does not match the regular expression ^(?:[0-9])$.",
				"/pattern-modifier-test:validation/pattern-modifier-test:type-validation[pattern-modifier-test:id='1']/pattern-modifier-test:custom-typedef-pattern-modifier-leaf",
				"invalid-regular-expression");

		invalidCustomReq = "<custom-typedef-pattern-modifier-leaf>1</custom-typedef-pattern-modifier-leaf>";
		testCustomFail(getRequestString(invalidCustomReq),
				"Supplied value does not match the regular expression ^(?:[^5-9])$.",
				"/pattern-modifier-test:validation/pattern-modifier-test:type-validation[pattern-modifier-test:id='1']/pattern-modifier-test:custom-typedef-pattern-modifier-leaf",
				"invalid-regular-expression");

		/**
		 * Just follow the patterns which is defined in typedef
		 *
		 * 		leaf custom-typedef-pattern-leaf {
		 *  		type custom-type-pattern;
		 * 		}
		 */

		validCustomReq = "<custom-typedef-pattern-leaf>b</custom-typedef-pattern-leaf>";
		testCustomPass(getRequestString(validCustomReq));

		invalidCustomReq = "<custom-typedef-pattern-leaf>ab</custom-typedef-pattern-leaf>";
		testCustomFail(getRequestString(invalidCustomReq),
				"Supplied value does not match the regular expression ^(?:[a-z])$.",
				"/pattern-modifier-test:validation/pattern-modifier-test:type-validation[pattern-modifier-test:id='1']/pattern-modifier-test:custom-typedef-pattern-leaf",
				"invalid-regular-expression");

		invalidCustomReq = "<custom-typedef-pattern-leaf>5</custom-typedef-pattern-leaf>";
		testCustomFail(getRequestString(invalidCustomReq),
				"Supplied value does not match the regular expression ^(?:[a-z])$.",
				"/pattern-modifier-test:validation/pattern-modifier-test:type-validation[pattern-modifier-test:id='1']/pattern-modifier-test:custom-typedef-pattern-leaf",
				"invalid-regular-expression");

		/**
		 * Override the pattern single pattern from typedef of 'custom-type-pattern' type
		 *
		 * 		leaf custom-typedef-pattern-override-leaf {
		 *  		type custom-type-pattern{
		 * 				pattern '[0-9]';
		 *			}
		 *		}
		 */

		validCustomReq = "<custom-typedef-pattern-override-leaf>5</custom-typedef-pattern-override-leaf>";
		testCustomPass(getRequestString(validCustomReq));

		invalidCustomReq = "<custom-typedef-pattern-override-leaf>a</custom-typedef-pattern-override-leaf>";
		testCustomFail(getRequestString(invalidCustomReq),
				"Supplied value does not match the regular expression ^(?:[0-9])$.",
				"/pattern-modifier-test:validation/pattern-modifier-test:type-validation[pattern-modifier-test:id='1']/pattern-modifier-test:custom-typedef-pattern-override-leaf",
				"invalid-regular-expression");

		/**
		 * Override the pattern modifier invert-match from typedef of 'custom-type-pattern' type
		 *
		 * 		leaf custom-typedef-pattern-modifier-leaf1 {
		 *  		type custom-type-pattern {
		 * 				pattern '[^5-9]' {
		 *       			modifier invert-match;
		 *       		}
		 *			}
		 *		}
		 */

		validCustomReq = "<custom-typedef-pattern-modifier-leaf1>8</custom-typedef-pattern-modifier-leaf1>";
		testCustomPass(getRequestString(validCustomReq));

		validCustomReq = "<custom-typedef-pattern-modifier-leaf1>73</custom-typedef-pattern-modifier-leaf1>";
		testCustomPass(getRequestString(validCustomReq));

		validCustomReq = "<custom-typedef-pattern-modifier-leaf1>abcd</custom-typedef-pattern-modifier-leaf1>";
		testCustomPass(getRequestString(validCustomReq));

		invalidCustomReq = "<custom-typedef-pattern-modifier-leaf1>a</custom-typedef-pattern-modifier-leaf1>";
		testCustomFail(getRequestString(invalidCustomReq),
				"Supplied value does not match the regular expression ^(?:[^5-9])$.",
				"/pattern-modifier-test:validation/pattern-modifier-test:type-validation[pattern-modifier-test:id='1']/pattern-modifier-test:custom-typedef-pattern-modifier-leaf1",
				"invalid-regular-expression");

		invalidCustomReq = "<custom-typedef-pattern-modifier-leaf1>3</custom-typedef-pattern-modifier-leaf1>";
		testCustomFail(getRequestString(invalidCustomReq),
				"Supplied value does not match the regular expression ^(?:[^5-9])$.",
				"/pattern-modifier-test:validation/pattern-modifier-test:type-validation[pattern-modifier-test:id='1']/pattern-modifier-test:custom-typedef-pattern-modifier-leaf1",
				"invalid-regular-expression");

	}

	public String getRequestString(String customMsg) {
		String req1 = "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + "<edit-config>"
				+ "<target>" + "<running />" + "</target>" + "<test-option>set</test-option>" + "<config>"
				+ "<validation xmlns=\"urn:org:bbf2:pattern-modifier-test\">"
				+ "<type-validation xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" nc:operation=\"create\">"
				+ "<id>1</id>";
		String req2 = "</type-validation>" + "</validation>" + "</config>" + "</edit-config>" + "</rpc>";

		return req1 + customMsg + req2;
	}
}
