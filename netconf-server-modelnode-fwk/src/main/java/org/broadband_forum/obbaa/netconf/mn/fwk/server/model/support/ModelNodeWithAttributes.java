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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support;


import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeKey;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.EMNKeyUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.MNKeyUtil;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.ChoiceCaseNodeUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.LoggerFactory;

/**
 * A HelperDrivenModelNode which stores config attributes and leaf-list in local maps and keeps no reference to its
 * parent or children.
 */
public class ModelNodeWithAttributes extends HelperDrivenModelNode implements Comparable<ModelNodeWithAttributes> {
    private static final AdvancedLogger LOGGER = LoggerFactory.getLogger(ModelNodeWithAttributes.class,
            "netconf-server-datastore", "DEBUG", "GLOBAL");

    public static final String CHILD_LISTS = "childLists";
    public static final String CHILD_CONTAINERS = "childContainers";
    public static final String LEAF_LISTS = "leafLists";
    public static final String PARENT = "parent_bean";
    public static final String NAMESPACE = "ns";
    public static final String MODEL_NODE = "modelNode";
    public static final String ADD_MODEL_NODE = "addModelNode";
    public static final String LEAF_VALUE = "leafValue";
    public static final String LEAF_COUNT = "leafCount";
    public static final String LEAF_LIST_COUNT = "leafListCount";

    protected final SchemaPath m_schemaPath;
    private final DataSchemaNode m_dataSchemaNode;
    private QName m_qName;
    private Map<QName, ConfigLeafAttribute> m_attributes = new LinkedHashMap<>();
    private Map<QName, LinkedHashSet<ConfigLeafAttribute>> m_leafLists = new HashMap<>();
    private ModelNodeDataStoreManager m_modelNodeDSM;

    public ModelNodeWithAttributes(SchemaPath schemaPath, ModelNodeId parentId, ModelNodeHelperRegistry
            modelNodeHelperRegistry,
                                   SubSystemRegistry subsystemRegistry, SchemaRegistry schemaRegistry,
                                   ModelNodeDataStoreManager modelNodeDSM) {
        super(null, parentId, modelNodeHelperRegistry, subsystemRegistry, schemaRegistry);
        m_qName = schemaPath.getLastComponent();
        m_schemaPath = schemaPath;
        m_modelNodeDSM = modelNodeDSM;
        m_dataSchemaNode = schemaRegistry.getDataSchemaNode(schemaPath);
    }

    @Override
    public QName getQName() {
        return m_qName;
    }

    @Override
    public SchemaPath getModelNodeSchemaPath() {
        return m_schemaPath;
    }

    public Map<QName, ConfigLeafAttribute> getAttributes() {
        return m_attributes;
    }

    public void setAttributes(Map<QName, ConfigLeafAttribute> attrValues) {
        if (m_dataSchemaNode instanceof ListSchemaNode) {
            setAttributesWithOrder(new LinkedHashMap<>(attrValues));
        } else {
            m_attributes = new LinkedHashMap<>(attrValues);
        }
    }

    /**
     * If the value in configAttributes is null,the attribute is to be removed. Used in removeAttribute in
     * configAttributeHelper.
     *
     * @param configAttributes
     */
    public void updateConfigAttributes(Map<QName, ConfigLeafAttribute> configAttributes) {
        Iterator<Map.Entry<QName, ConfigLeafAttribute>> iterator = configAttributes.entrySet().iterator();
        Map<QName, ConfigLeafAttribute> attributes = getAttributes();
        while (iterator.hasNext()) {
            Map.Entry<QName, ConfigLeafAttribute> attribute = iterator.next();
            if (attribute.getValue() == null) {
                attributes.remove(attribute.getKey());
            } else {
                attributes.put(attribute.getKey(), attribute.getValue());
            }
        }
        setAttributes(attributes);
    }

    protected void setAttributesWithOrder(Map<QName, ConfigLeafAttribute> attrValues) {
        List<QName> keyDefinition = ((ListSchemaNode) m_dataSchemaNode).getKeyDefinition();
        Map<QName, ConfigLeafAttribute> orderedAttributes = new LinkedHashMap<>();
        //copy keys first
        for (QName key : keyDefinition) {
            orderedAttributes.put(key, attrValues.get(key));
            attrValues.remove(key);
        }
        //copy rest of the attributes
        orderedAttributes.putAll(attrValues);
        m_attributes = orderedAttributes;
    }

    public ConfigLeafAttribute getAttribute(QName qname) {
        return getAttributes().get(qname);
    }

    public Set<ConfigLeafAttribute> getLeafList(QName qName) {
        return m_leafLists.get(qName);
    }

    public void setLeafLists(Map<QName, LinkedHashSet<ConfigLeafAttribute>> leafLists) {
        m_leafLists.putAll(leafLists);
    }

    public Map<QName, LinkedHashSet<ConfigLeafAttribute>> getLeafLists() {
        return m_leafLists;
    }

    public void updateLeafListAttributes(Map<QName, LinkedHashSet<ConfigLeafAttribute>> leafListAttributes) {
        if (leafListAttributes != null) {
            for (QName qname : leafListAttributes.keySet()) {
                LinkedHashSet<ConfigLeafAttribute> leafListValues = leafListAttributes.get(qname);
                m_leafLists.put(qname, leafListValues);
            }
        }
    }

    public void removeLeafListAttributes(Map<QName, LinkedHashSet<ConfigLeafAttribute>> leafListAttributes) {
        if (leafListAttributes != null) {
            for (QName qname : leafListAttributes.keySet()) {
                LinkedHashSet<ConfigLeafAttribute> leafListValues = leafListAttributes.get(qname);
                Set<ConfigLeafAttribute> existingLeafList = m_leafLists.get(qname);
                if (existingLeafList != null && leafListValues != null) {
                    existingLeafList.removeAll(leafListValues);
                } else if (existingLeafList != null) {
                    existingLeafList.clear();
                }
            }
        }
    }


    @Override
    public int compareTo(ModelNodeWithAttributes otherNode) {
        if (this == otherNode) {
            return 0;
        } else {
            return getModelNodeId().compareTo(otherNode.getModelNodeId());
        }
    }

    @Override
    public ModelNode getParent() {
        SchemaPath parentSchemaPath = getModelNodeSchemaPath().getParent();
        DataSchemaNode parentSchemaNode = getSchemaRegistry().getDataSchemaNode(parentSchemaPath);
        if (ChoiceCaseNodeUtil.isChoiceOrCaseNode(parentSchemaNode)) {
            DataSchemaNode thisSchemaNode = getSchemaRegistry().getDataSchemaNode(getModelNodeSchemaPath());
            parentSchemaNode = SchemaRegistryUtil.getEffectiveParentNode(thisSchemaNode, getSchemaRegistry());
            parentSchemaPath = parentSchemaNode.getPath();
        }
        if (parentSchemaPath.getLastComponent() == null) {
            return null;
        }
        ModelNodeId parentNodeId = getParentNodeId();
        ModelNodeId grandParentId = EMNKeyUtil.getParentId(getSchemaRegistry(), parentSchemaPath, parentNodeId);
        ModelNodeKey modelNodeKey = MNKeyUtil.getModelNodeKey(parentNodeId, parentSchemaPath, getSchemaRegistry());
        return m_modelNodeDSM.findNode(parentSchemaPath, modelNodeKey, grandParentId);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(m_qName.getLocalName());
        if (m_attributes != null && !m_attributes.isEmpty()) {
            sb.append("[");
            for (QName qname : m_attributes.keySet()) {
                sb.append(m_attributes.get(qname) + " ");
            }
            return sb.toString().trim().concat("]");
        }
        return sb.toString();
    }

    public ModelNodeDataStoreManager getModelNodeDSM() {
        return m_modelNodeDSM;
    }

    /**
     * The caller of this method must call setValue(null) finally when all required processing is done.
     */
    public Object getValue() {
        return ModelNodeDynaBeanFactory.getDynaBean(this);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        ModelNodeWithAttributes that = (ModelNodeWithAttributes) other;

        if (m_schemaPath != null ? !m_schemaPath.equals(that.m_schemaPath) : that.m_schemaPath != null) return false;
        return getModelNodeId() != null ? getModelNodeId().equals(that.getModelNodeId()) : that.getModelNodeId() ==
                null;
    }

    @Override
    public int hashCode() {
        int result = m_schemaPath != null ? m_schemaPath.hashCode() : 0;
        result = 31 * result + (getModelNodeId() != null ? getModelNodeId().hashCode() : 0);
        return result;
    }

    public Map<QName, ConfigLeafAttribute> getKeyAttributes() {
        Map<QName, ConfigLeafAttribute> keyAttrs = new LinkedHashMap<>();

        if (m_dataSchemaNode instanceof ListSchemaNode) {
            ListSchemaNode listSchemaNode = (ListSchemaNode) m_dataSchemaNode;
            List<QName> keyDefinition = listSchemaNode.getKeyDefinition();
            for (QName keyQname : keyDefinition) {
                keyAttrs.put(keyQname, m_attributes.get(keyQname));
            }
        }
        return keyAttrs;
    }
}
