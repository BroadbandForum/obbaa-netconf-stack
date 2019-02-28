package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.utils;

import java.util.Collections;
import java.util.Set;

import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;

public class ActionUtils {
	
	public static Set<ActionDefinition> retrieveActionDefinitionForSchemaNode(SchemaPath schemaPath, SchemaRegistry schemaRegistry){
		DataSchemaNode dsn = schemaRegistry.getDataSchemaNode(schemaPath);
		if(dsn instanceof ContainerSchemaNode){
			Set<ActionDefinition> actionDefs = ((ContainerSchemaNode)dsn).getActions();
			return actionDefs;
		} else if(dsn instanceof ListSchemaNode){
			Set<ActionDefinition> actionDefs = ((ListSchemaNode)dsn).getActions();
			return actionDefs;
		}
		return Collections.emptySet();
	}
}
