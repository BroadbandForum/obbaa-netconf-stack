package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;

public interface ConstraintHelper {
	
	public boolean isMandatory();

    //FIXME: FNMS-10110  don't understand what this method supposed to do ??
	public boolean isChildSet(ModelNode node);
	
}
