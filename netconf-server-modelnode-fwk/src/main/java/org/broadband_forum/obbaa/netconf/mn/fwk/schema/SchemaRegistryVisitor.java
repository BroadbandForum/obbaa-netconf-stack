package org.broadband_forum.obbaa.netconf.mn.fwk.schema;

import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

/**
 * Implement this interface to traverse the nodes in SchemaRegistry.
 * Use SchemaRegistryTraverser to begin traversal.
 *
 * @Code{
 * traverser = new SchemaRegistryTraverser(new MySchemaRegistryVisitor(), m_schemaRegistry, null, rootSchemaPaths);
 * traverser.traverse();
 * }
 *  *
 *
 * Created by keshava on 12/9/15.
 */
public interface SchemaRegistryVisitor {
    void visitLeafListNode(String componentId, SchemaPath parentSchemaPath, LeafListSchemaNode leafListNode);

    void visitLeafNode(String componentId, SchemaPath parentSchemaPath, LeafSchemaNode leafSchemaNode);

    void visitChoiceCaseNode(String componentId, SchemaPath parentPath, CaseSchemaNode choiceCaseNode);

    void visitChoiceNode(String componentId, SchemaPath parentPath, ChoiceSchemaNode choiceSchemaNode);

    void visitAnyXmlNode(String componentId, SchemaPath parentPath, AnyXmlSchemaNode anyXmlSchemaNode);

    void visitListNode(String componentId, SchemaPath parentPath, ListSchemaNode listSchemaNode);

    void visitContainerNode(String componentId, SchemaPath parentSchemaPath, ContainerSchemaNode containerSchemaNode);

    void visitIdentityNode(String componentId, IdentitySchemaNode identitySchemaNode);

    void visitEnter(String componentId, SchemaPath parentPath, SchemaPath schemaPath);

    void visitLeave(String componentId, SchemaPath parentPath, SchemaPath schemaPath);
}
