package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support;

import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditConfigException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;

public interface DefaultCapabilityCommandInterceptor {
	EditContainmentNode processMissingData(EditContainmentNode oldEditData, ModelNode childModelNode) throws EditConfigException ;
	void populateChoiceCase(EditContainmentNode newData, CaseSchemaNode caseNode, ModelNode childModelNode, boolean isDefaultCase);
}
