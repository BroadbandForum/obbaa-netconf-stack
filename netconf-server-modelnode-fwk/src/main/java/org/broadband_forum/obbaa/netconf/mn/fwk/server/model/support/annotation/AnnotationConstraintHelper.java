package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.annotation;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConstraintHelper;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;

public class AnnotationConstraintHelper implements ConstraintHelper {

	@Override
	public boolean isMandatory() {
		return true;
	}

	@Override
	public boolean isChildSet(ModelNode node) {
		return true;
	}

}
