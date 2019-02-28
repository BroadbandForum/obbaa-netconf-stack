package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.opendaylight.yangtools.yang.common.QName;
import org.w3c.dom.Element;

import org.broadband_forum.obbaa.netconf.api.util.Pair;

public class NoopSubSystem extends AbstractSubSystem {
	
	public static NoopSubSystem c_instance = new NoopSubSystem();

	@Override
	public Map<ModelNodeId, List<Element>> retrieveStateAttributes(Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> attributes) throws GetAttributeException {
		return Collections.emptyMap();
	}
}
