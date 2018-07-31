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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.dsm;

import java.util.Collections;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigAttributeHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.SetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.YangConstraintHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.EMNKeyUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.YangTypeToClassConverter;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.LoggerFactory;

/**
 * Created by keshava on 21/12/15.
 */
public class DsmConfigAttributeHelper extends YangConstraintHelper implements ConfigAttributeHelper {
    private final ModelNodeDataStoreManager m_modelNodeDSM;
    private final SchemaRegistry m_schemaRegistry;
    private LeafSchemaNode m_leafNode;
    private QName m_qname;
    private static final AdvancedLogger LOGGER = LoggerFactory.getLogger(DsmConfigAttributeHelper.class,
            "netconf-server-datastore", "DEBUG", "GLOBAL");

    public DsmConfigAttributeHelper(ModelNodeDataStoreManager modelNodeDSM, SchemaRegistry schemaRegistry,
                                    LeafSchemaNode leafNode, QName qname) {
        super(leafNode);
        m_leafNode = leafNode;
        m_qname = qname;
        m_modelNodeDSM = modelNodeDSM;
        m_schemaRegistry = schemaRegistry;
    }

    @Override
    public Class<?> getAttributeType() {
        return YangTypeToClassConverter.getClass(m_leafNode.getType());
    }

    @Override
    public SchemaPath getChildModelNodeSchemaPath() {
        return m_leafNode.getPath();
    }

    @Override
    public String getDefault() {
        return m_leafNode.getDefault();
    }

    @Override
    public ConfigLeafAttribute getValue(ModelNode modelNode) throws GetAttributeException {
        ConfigLeafAttribute value = ((ModelNodeWithAttributes) modelNode).getAttribute(m_qname);
        return value;
    }

    @Override
    public void setValue(ModelNode modelNode, ConfigLeafAttribute value) throws SetAttributeException {
        ModelNodeId parentId = EMNKeyUtil.getParentId(m_schemaRegistry, modelNode.getModelNodeSchemaPath(), modelNode
                .getModelNodeId());
        m_modelNodeDSM.updateNode(modelNode, parentId, Collections.singletonMap(m_qname, value), null, false);
    }

    @Override
    public void removeAttribute(ModelNode modelNode) {
        ModelNodeId parentId = EMNKeyUtil.getParentId(m_schemaRegistry, modelNode.getModelNodeSchemaPath(), modelNode
                .getModelNodeId());
        m_modelNodeDSM.updateNode(modelNode, parentId, Collections.singletonMap(m_qname, null), null, false);
    }

    @Override
    public boolean isChildSet(ModelNode node) {
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_leafNode == null) ? 0 : m_leafNode.hashCode());
        result = prime * result + ((m_modelNodeDSM == null) ? 0 : m_modelNodeDSM.hashCode());
        result = prime * result + ((m_qname == null) ? 0 : m_qname.hashCode());
        result = prime * result + ((m_schemaRegistry == null) ? 0 : m_schemaRegistry.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DsmConfigAttributeHelper other = (DsmConfigAttributeHelper) obj;
        if (m_leafNode == null) {
            if (other.m_leafNode != null)
                return false;
        } else if (!m_leafNode.equals(other.m_leafNode))
            return false;
        if (m_modelNodeDSM == null) {
            if (other.m_modelNodeDSM != null)
                return false;
        } else if (!m_modelNodeDSM.equals(other.m_modelNodeDSM))
            return false;
        if (m_qname == null) {
            if (other.m_qname != null)
                return false;
        } else if (!m_qname.equals(other.m_qname))
            return false;
        if (m_schemaRegistry == null) {
            if (other.m_schemaRegistry != null)
                return false;
        } else if (!m_schemaRegistry.equals(other.m_schemaRegistry))
            return false;
        return true;
    }

}
