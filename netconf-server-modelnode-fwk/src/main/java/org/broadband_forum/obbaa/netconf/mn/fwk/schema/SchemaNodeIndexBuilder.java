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

package org.broadband_forum.obbaa.netconf.mn.fwk.schema;

import java.util.Map;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.api.util.DataPath;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.DataPathUtil;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;

/**
 * Created by keshava on 11/19/15.
 */
public class SchemaNodeIndexBuilder implements SchemaNodeVisitor {

    private final Map<SchemaPath, DataSchemaNode> m_schemaNodes;
    private final Map<DataPath, ActionDefinition> m_actionDefinitionsByDataPath;
    private final Set<ActionDefinition> m_actionDefinitionsWithListAndLeafRef;
    private final Map<SchemaPath, NotificationDefinition> m_notificationDefinitions;
    private final SchemaContext m_schemaContext;

    public SchemaNodeIndexBuilder(Map<SchemaPath, DataSchemaNode> schemaNodes, Map<DataPath, ActionDefinition> actionDefinitionsByDataPath,
            Set<ActionDefinition> actionDefinitionsWithListAndLeafRef, Map<SchemaPath, NotificationDefinition> notificationDefinitions, SchemaContext schemaContext) {
        m_schemaNodes = schemaNodes;
        m_actionDefinitionsByDataPath = actionDefinitionsByDataPath;
        m_actionDefinitionsWithListAndLeafRef = actionDefinitionsWithListAndLeafRef;
        m_notificationDefinitions = notificationDefinitions;
        m_schemaContext = schemaContext;
    }

    @Override
    public void visit(SchemaNode node) {
        SchemaPath path = node.getPath();
        if (node != null) {
            if (node instanceof DataSchemaNode) {
                m_schemaNodes.put(path, (DataSchemaNode)node);
            }
            else if (node instanceof ActionDefinition) {
            	DataPath dataPath = DataPathUtil.buildDataPath(path, m_schemaContext);
            	for (DataSchemaNode outputChild : ((ActionDefinition) node).getOutput().getChildNodes()){
            	    if ( outputChild instanceof ListSchemaNode){
                        m_actionDefinitionsWithListAndLeafRef.add((ActionDefinition) node);
                        break;
                    } else if ( outputChild instanceof LeafSchemaNode){
                        TypeDefinition typeDef = ((LeafSchemaNode) outputChild).getType();
                        if ( typeDef instanceof LeafrefTypeDefinition){
                            m_actionDefinitionsWithListAndLeafRef.add((ActionDefinition) node);
                            break;
                        }
                    }
                }
                m_actionDefinitionsByDataPath.put(dataPath, (ActionDefinition)node);
            }
            else if (node instanceof NotificationDefinition) {
                m_notificationDefinitions.put(path, (NotificationDefinition)node);
            }
        }
    }
}
