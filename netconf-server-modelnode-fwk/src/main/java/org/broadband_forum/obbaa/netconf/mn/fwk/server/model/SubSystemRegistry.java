package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystem;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public interface SubSystemRegistry {
	void register(String componentId, SchemaPath schemaPath, SubSystem subSystem);
	
	SubSystem lookupSubsystem(SchemaPath schemaPath);

	void undeploy(String componentId);
}
