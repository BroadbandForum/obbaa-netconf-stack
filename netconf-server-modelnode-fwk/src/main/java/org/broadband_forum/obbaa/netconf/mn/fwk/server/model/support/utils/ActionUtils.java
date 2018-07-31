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

    public static Set<ActionDefinition> retrieveActionDefinitionForSchemaNode(SchemaPath schemaPath, SchemaRegistry
            schemaRegistry) {
        DataSchemaNode dsn = schemaRegistry.getDataSchemaNode(schemaPath);
        if (dsn instanceof ContainerSchemaNode) {
            Set<ActionDefinition> actionDefs = ((ContainerSchemaNode) dsn).getActions();
            return actionDefs;
        } else if (dsn instanceof ListSchemaNode) {
            Set<ActionDefinition> actionDefs = ((ListSchemaNode) dsn).getActions();
            return actionDefs;
        }
        return Collections.emptySet();
    }
}
