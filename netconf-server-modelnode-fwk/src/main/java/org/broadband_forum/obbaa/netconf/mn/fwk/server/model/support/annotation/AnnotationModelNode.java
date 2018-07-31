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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.annotation;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.Container;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.HelperDrivenModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;

import org.opendaylight.yangtools.yang.common.QName;

import java.io.Serializable;

public abstract class AnnotationModelNode extends HelperDrivenModelNode implements Serializable {
    private static final long serialVersionUID = 1L;

    private QName m_qName = null;

    public AnnotationModelNode(ModelNode parent, ModelNodeId parentNodeId, ModelNodeHelperRegistry helperRegistry,
                               SubSystemRegistry subSystemRegistry,
                               SchemaRegistry schemaRegistry) {
        super(parent, parentNodeId, helperRegistry, subSystemRegistry, schemaRegistry);
    }

    @Override
    public QName getQName() {
        if (m_qName == null) {
            Container container = getClass().getAnnotation(Container.class);
            String namespace = container.namespace();
            String name = container.name();
            m_qName = getSchemaRegistry().lookupQName(namespace, name);
        }
        return m_qName;
    }
}
