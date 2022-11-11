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


public class DateAndTimeTypeValidatorTest extends AbstractTypeValidatorTest {
	@Test
	public void testDateAndTime() throws NetconfMessageBuilderException {
		testPass("dateandtimevalidator/valid-date-and-time.xml");
		
		testFail("dateandtimevalidator/invalid-date-and-time-1.xml",
				"2018-20-28T19:41:34.000Z is not a valid date-and-time", "/validation:validation/validation:type-validation[validation:id='1']/validation:date-and-time-type", null);
		
		testFail("dateandtimevalidator/invalid-date-and-time-2.xml",
				"Supplied value does not match the regular expression ^(?:\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?(Z|[\\+\\-]\\d{2}:\\d{2}))$.",
				"/validation:validation/validation:type-validation[validation:id='1']/validation:date-and-time-type", "invalid-regular-expression");
	}
	
}
