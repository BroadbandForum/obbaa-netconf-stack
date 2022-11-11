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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.ModelNodeHelperDeployer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.apache.commons.lang3.StringUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;

public abstract class AbstractModelNodeHelperDeployer implements ModelNodeHelperDeployer {

    private SchemaRegistry m_schemaRegistry;
    
    public AbstractModelNodeHelperDeployer(SchemaRegistry schemaRegistry) {
        m_schemaRegistry = schemaRegistry;
    }
    
    
    protected boolean isKeyLeafSchemaNode(SchemaPath parentSchemaPath, LeafSchemaNode leafSchemaNode) {
        DataSchemaNode parentNode = m_schemaRegistry.getDataSchemaNode(parentSchemaPath);
        if(parentNode instanceof ListSchemaNode){
            return ((ListSchemaNode)parentNode).getKeyDefinition().contains(leafSchemaNode.getQName());
        }
        return false;
    }
    
    @Override
    public String getErrors(){
    	return null;
    }
}
