package org.broadband_forum.obbaa.netconf.mn.fwk.schema.validation.typevalidators;

import org.junit.Test;

import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;

public class EmptyTypeValidatorTest extends AbstractTypeValidatorTest {
	
	@Test
	public void testEmpty() throws NetconfMessageBuilderException {
		testPass("emptyvalidator/valid-empty.xml");

		testFail("emptyvalidator/invalid-empty-1.xml",
				"Value \"non-empty\" does not meet the empty type constraints. Element \"empty-type\" should not have any value",
				"/validation:validation/validation:type-validation[validation:id='1']/validation:empty-type", null);
	}
}
