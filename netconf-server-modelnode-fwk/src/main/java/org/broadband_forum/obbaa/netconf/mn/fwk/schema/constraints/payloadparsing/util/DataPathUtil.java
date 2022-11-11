/*
 * Copyright 2018 Broadband Forum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.api.util.DataPath;
import org.broadband_forum.obbaa.netconf.api.util.DataPathLevel;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.ActionNodeContainer;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.stmt.InputEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OutputEffectiveStatement;

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
			SchemaNode parentSchemaNode = schemaRegistry.getDataSchemaNode(parentpath);
			if ( parentSchemaNode == null){
				parentSchemaNode = schemaRegistry.getActionDefinitionNode(DataPathUtil.convertToDataPath(parentpath));
			}
			if (!(parentSchemaNode instanceof ChoiceSchemaNode || parentSchemaNode instanceof CaseSchemaNode || parentSchemaNode instanceof InputEffectiveStatement || parentSchemaNode instanceof OutputEffectiveStatement)) {
				levels.add(new DataPathLevel(parentpath.getLastComponent()));
			}
			parentpath = parentpath.getParent();
		}
		Collections.reverse(levels);
		return DataPath.create(levels);
	}
	
	public static DataPath join(DataPath... dataPaths){
	    List<DataPathLevel> levels = new ArrayList<>();
	    for ( DataPath dataPath : dataPaths){
	        levels.addAll(dataPath.getPath());
	    }
	    return DataPath.create(levels);
	}
	
	public static DataPath buildParentDataPath(SchemaPath schemaPath, SchemaRegistry schemaRegistry, boolean includeMountPath) {
	    DataPath dataPath = buildParentDataPath(schemaPath, schemaRegistry);
	    if ( includeMountPath && schemaRegistry.getMountPath() != null){
	        SchemaPath mountPath = schemaRegistry.getMountPath();
	        DataPath mountDataPath = buildParentDataPath(mountPath, schemaRegistry.getParentRegistry());
	        return join(mountDataPath, dataPath);
	    }
	    return dataPath;
	}
	
	public static DataPath buildDataPath(SchemaPath schemaPath, SchemaContext context) {
		if (schemaPath == null) {
			return null;
		}
		List<DataPathLevel> levels = new ArrayList<>();
		Iterator<QName> it = schemaPath.getPathFromRoot().iterator();
		SchemaNode currentNode = context;
		while( it.hasNext()){
			QName qname = it.next();
			Collection<DataSchemaNode> children = null;
			if ( currentNode instanceof DataNodeContainer){
				children = ((DataNodeContainer) currentNode).getChildNodes();
			} else if ( currentNode instanceof ChoiceSchemaNode){
				children = new ArrayList<>();
				for ( CaseSchemaNode caseNode : ((ChoiceSchemaNode) currentNode).getCases().values()){
					children.add(caseNode);
				}
			} 
			if (currentNode instanceof ActionNodeContainer) {
				Set<ActionDefinition> actions = ((ActionNodeContainer) currentNode).getActions();
				for ( ActionDefinition action : actions){
					if ( action.getQName().equals(qname)){
						levels.add(new DataPathLevel(action.getQName()));
						break;
					}
				}
			}
			for ( SchemaNode child : children){
				if ( child.getQName().equals(qname)){
					currentNode = child;
					if (!(child instanceof ChoiceSchemaNode || child instanceof CaseSchemaNode)) {
						levels.add(new DataPathLevel(child.getQName()));
					}
					break;
				}
			}
		}
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
    
    public static DataPath convertToDataPath(List<QName> qnameList) {
        List<DataPathLevel> levels = new ArrayList<>();
        for (QName qName : qnameList) {
            levels.add(new DataPathLevel(qName));
        }
        return DataPath.create(levels);
    }
}
