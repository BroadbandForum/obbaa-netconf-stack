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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.jukebox2;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.Container;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ContainerChild;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.annotation.AnnotationModelNode;
import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

@Container(name = "jukebox", namespace = Jukebox.NAMESPACE)
public class Jukebox extends AnnotationModelNode {
    public static final QName QNAME = QName.create("http://example.com/ns/example-jukebox", "2014-07-03", "jukebox");
    public static final SchemaPath JUKEBOX_SCHEMA_PATH = new SchemaPathBuilder().appendQName(QNAME).build();
    ;

    public Jukebox(ModelNode parent, ModelNodeId parentNodeId, ModelNodeHelperRegistry helperRegistry,
                   SubSystemRegistry subSystemRegistry, SchemaRegistry schemaRegistry) {
        super(parent, parentNodeId, helperRegistry, subSystemRegistry, schemaRegistry);
    }

    private static final long serialVersionUID = 1L;
    public static final String NAMESPACE = "http://example.com/ns/example-jukebox";
    public static boolean EMPTY_LIBRARY_NOT_ALLOWED = false;
    private Library m_library;

    @ContainerChild(name = "library", namespace = Jukebox.NAMESPACE, factoryName = "AbstractModelNodeFactory")
    public Library getLibrary() {
        return m_library;
    }

    public void setLibrary(Library lib) throws EditException {
        if (lib == null) {
            if (EMPTY_LIBRARY_NOT_ALLOWED) {
                throw new EditException("Empty library not allowed.");
            }

        }
        this.m_library = lib;
    }

    public void validate() {

    }

    @Override
    public SchemaPath getModelNodeSchemaPath() {
        return JUKEBOX_SCHEMA_PATH;
    }
}
