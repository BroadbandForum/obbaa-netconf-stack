package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditConfigException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.HelperDrivenModelNode;

public interface ModelNodeInterceptor {

	public void interceptEditConfig(HelperDrivenModelNode modelNode,EditContext editContext) throws EditConfigException;
}
