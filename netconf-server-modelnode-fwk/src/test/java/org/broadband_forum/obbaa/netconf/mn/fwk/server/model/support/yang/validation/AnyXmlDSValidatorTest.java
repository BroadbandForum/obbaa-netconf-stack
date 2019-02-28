package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.validation;

import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorSeverity;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorType;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;

public class AnyXmlDSValidatorTest extends AbstractDataStoreValidatorTest {
	
	//@Test
	public void testViolateWhenconstraints() throws ModelNodeInitException {
		testFail("/datastorevalidatortest/rangevalidation/defaultxml/invalid-when-constraint-anyxml.xml", NetconfRpcErrorTag.OPERATION_FAILED,
				NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error, "when-violation",
				"Violate when constraints: ../result-anyxml = 'success'", "/validation/when-validation/extraInfo");
	}
	
	//@Test
	public void testValidMustConstraint() throws ModelNodeInitException {
		testPass("/datastorevalidatortest/rangevalidation/defaultxml/valid-must-constraint-anyxml.xml");
	}
	
	//@Test
	public void testViolateMustConstraint() throws ModelNodeInitException {
		testFail("/datastorevalidatortest/rangevalidation/defaultxml/invalid-must-constraint-anyxml.xml", NetconfRpcErrorTag.OPERATION_FAILED,
				NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error, "must-violation",
				"An ifIndex must be 100 .. 200", "/validation/must-validation/interface/ifInfo");
	}
	
	//@Test
	public void testViolateWhenConstraintForLeafListCaseNode() throws ModelNodeInitException {
		testFail("/datastorevalidatortest/rangevalidation/defaultxml/invalid-when-constraint-anyxml-casenode.xml", NetconfRpcErrorTag.OPERATION_FAILED,
				NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error, "when-violation",
				"Violate when constraints: validation:result-choice = 'failed'", "/validation/when-validation/choicecase/data-failed");
	}

	
	//@Test
	public void testValidWhenConstraintForLeafListCaseNode() throws ModelNodeInitException {
		testPass("/datastorevalidatortest/rangevalidation/defaultxml/valid-when-constraint-anyxml-casenode.xml");		
	}
}
