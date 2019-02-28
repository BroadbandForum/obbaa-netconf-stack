package org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.broadband_forum.obbaa.netconf.api.util.DataPath;
import org.broadband_forum.obbaa.netconf.api.util.DataPathLevel;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public class DataPathUtil {

	/**
	 * This method used to build the data path of given schema path (skip the choice and case paths if it is exists in schemapath).
	 * @param schemaPath
	 * @param schemaRegistry
	 * @return {DataPath}
	 */
	public static DataPath buildParentDataPath(SchemaPath schemaPath, SchemaRegistry schemaRegistry) {
		if (schemaPath == null) {
			return null;
		}
		List<DataPathLevel> levels = new ArrayList<>();
		levels.add(new DataPathLevel(schemaPath.getLastComponent()));
		SchemaPath parentpath = schemaPath.getParent();
		/**
		 * Itreate the each parent schamapath until reach the root and build the
		 * data path of action node(ie., skip the choice and case qnames from
		 * action schemapath)
		 */
		while (parentpath != null && parentpath.getParent() != null) {
			DataSchemaNode parentSchemaNode = schemaRegistry.getDataSchemaNode(parentpath);
			if (!(parentSchemaNode instanceof ChoiceSchemaNode || parentSchemaNode instanceof CaseSchemaNode)) {
				levels.add(new DataPathLevel(parentpath.getLastComponent()));
			}
			parentpath = parentpath.getParent();
		}
		Collections.reverse(levels);
		return DataPath.create(levels);
	}
	
	/**
	 * Here simply convert the given schemapath to DataPath
	 */
    public static DataPath convertToDataPath(SchemaPath path) {
        if (path == null) {
            return null;
        }
        List<DataPathLevel> levels = new ArrayList<>();
        for (QName qName : path.getPathFromRoot()) {
            levels.add(new DataPathLevel(qName));
        }
        return DataPath.create(levels);
    }
    
}
