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

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeDynaBean;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn;

/**
 * This is a dummy ModelNode used in Validation context, to simulate a non-existent (in data tree) non-presence container ModelNode to help
 * in creation of nodes during when validation
 *
 */
public class ProxyValidationModelNode extends ModelNodeWithAttributes {

    ModelNode m_parent;
    ModelNodeDynaBean m_dynaBean;
    ModelNodeId m_modelNodeId;

    public ProxyValidationModelNode(ModelNode parentModelNode, ModelNodeHelperRegistry modelNodeHelperRegistry, SchemaPath schemaPath) {
        super(schemaPath, parentModelNode.getModelNodeId(), modelNodeHelperRegistry, null, parentModelNode.getSchemaRegistry(), null);
        m_parent = parentModelNode;
    }

    @Override
    public ModelNode getParent() {
        return m_parent;
    }

    @Override
    public Map<QName, ConfigLeafAttribute> getAttributes() {
        return Collections.emptyMap();
    }

    @Override
    public Map<QName, LinkedHashSet<ConfigLeafAttribute>> getLeafLists() {
        return Collections.emptyMap();
    }

    @Override
    public ConfigLeafAttribute getAttribute(QName qname) {
        return null;
    }

    @Override
    public Set<ConfigLeafAttribute> getLeafList(QName qName) {
        return null;
    }

    @Override
    public Map<QName, String> getListKeys() throws GetAttributeException {
        return Collections.emptyMap();
    }

    @Override
    public ModelNodeId getModelNodeId() {
        if (m_modelNodeId == null) {
            m_modelNodeId = new ModelNodeId(m_parent.getModelNodeId());
            m_modelNodeId.addRdn(ModelNodeRdn.CONTAINER, getQName().getNamespace().toString(), getContainerName());
        }
        return m_modelNodeId;
    }

    @Override
    public ModelNodeDynaBean getValue() {
        return (ModelNodeDynaBean) super.getValue();
    }
}
