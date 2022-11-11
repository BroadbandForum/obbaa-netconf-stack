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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation;

import java.util.Map;

import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;

/**
 * Used for the purpose of validation. 
 * This pojo is expected to hold the global or mount schema registry depending 
 * on the type of node that is used upon completion of a transaction. 
 * 
 *  Note: Transaction as in an input-output process, not a DB transaction. 
 */
public class DSValidationMountContext {
    
    private SchemaRegistry m_schemaRegistry;
    private SchemaPath m_outputPath;
    private DataSchemaNode m_outputSchemaNode;
    private Map<String,Object> mountCurrentScope;
    
    public Map<String, Object> getMountCurrentScope() {
		return mountCurrentScope;
	}
	public void setMountCurrentScope(Map<String, Object> mountCurrentScope) {
		this.mountCurrentScope = mountCurrentScope;
	}
	public SchemaRegistry getSchemaRegistry() {
        return m_schemaRegistry;
    }
    public SchemaPath getSchemaPath() {
        return m_outputPath;
    }
    public void setSchemaRegistry(SchemaRegistry schemaRegistry) {
        this.m_schemaRegistry = schemaRegistry;
    }
    public void setSchemaPath(SchemaPath outputPath) {
        this.m_outputPath = outputPath;
    }
    public void setDataSchemaNode(DataSchemaNode schemaNode) {
    	m_outputSchemaNode = schemaNode;
    }
    public DataSchemaNode getDataSchemaNode(){
    	return m_outputSchemaNode;
    }
    
    
}
