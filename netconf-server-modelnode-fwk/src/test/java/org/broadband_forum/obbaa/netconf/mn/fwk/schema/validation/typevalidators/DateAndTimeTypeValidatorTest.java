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
