package org.broadband_forum.obbaa.netconf.mn.fwk.schema;

import java.util.Map;

import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

/**
 * Created by keshava on 11/19/15.
 */
public class SchemaNodeIndexBuilder implements SchemaNodeVisitor {

    private final Map<SchemaPath, DataSchemaNode> m_schemaNodes;
    private final Map<SchemaPath, ActionDefinition> m_actionDefinitions;
    private final Map<SchemaPath, NotificationDefinition> m_notificationDefinitions;

    public SchemaNodeIndexBuilder(
            Map<SchemaPath, DataSchemaNode> schemaNodes,
            Map<SchemaPath, ActionDefinition> actionDefinitions,
            Map<SchemaPath, NotificationDefinition> notificationDefinitions) {
        m_schemaNodes = schemaNodes;
        m_actionDefinitions = actionDefinitions;
        m_notificationDefinitions = notificationDefinitions;
    }

    @Override
    public void visit(SchemaNode node) {
        SchemaPath path = node.getPath();
        if (node != null) {
            if (node instanceof DataSchemaNode) {
                m_schemaNodes.put(path, (DataSchemaNode)node);
            }
            else if (node instanceof ActionDefinition) {
                m_actionDefinitions.put(path, (ActionDefinition)node);
            }
            else if (node instanceof NotificationDefinition) {
                m_notificationDefinitions.put(path, (NotificationDefinition)node);
            }
        }
    }
}
