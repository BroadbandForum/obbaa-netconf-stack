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

import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
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
 * @Code{ traverser = new SchemaRegistryTraverser(new MySchemaRegistryVisitor(), m_schemaRegistry, null,
 * rootSchemaPaths);
 * traverser.traverse();
 * }
 * *
 * <p>
 * Created by keshava on 12/9/15.
 */
public interface SchemaRegistryVisitor {
    void visitLeafListNode(String componentId, SchemaPath parentSchemaPath, LeafListSchemaNode leafListNode);

    void visitLeafNode(String componentId, SchemaPath parentSchemaPath, LeafSchemaNode leafSchemaNode);

    void visitChoiceCaseNode(String componentId, SchemaPath parentPath, ChoiceCaseNode choiceCaseNode);

    void visitChoiceNode(String componentId, SchemaPath parentPath, ChoiceSchemaNode choiceSchemaNode);

    void visitAnyXmlNode(String componentId, SchemaPath parentPath, AnyXmlSchemaNode anyXmlSchemaNode);

    void visitListNode(String componentId, SchemaPath parentPath, ListSchemaNode listSchemaNode);

    void visitContainerNode(String componentId, SchemaPath parentSchemaPath, ContainerSchemaNode containerSchemaNode);

    void visitIdentityNode(String componentId, IdentitySchemaNode identitySchemaNode);

    void visitEnter(String componentId, SchemaPath parentPath, SchemaPath schemaPath);

    void visitLeave(String componentId, SchemaPath parentPath, SchemaPath schemaPath);
}
