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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.w3c.dom.Element;

public class XmlModelNodeImpl extends ModelNodeWithAttributes {

    private Map<QName, List<XmlModelNodeImpl>> m_children = null;
    private final List<Element> m_childrenXml;
    private final XmlModelNodeToXmlMapper m_xmlModelNodeToXmlMapper;
    private XmlModelNodeImpl m_parentModelNode;
    private boolean m_toBeUpdated = false;

    public XmlModelNodeImpl(SchemaPath schemaPath, Map<QName, ConfigLeafAttribute> attributes, List<Element>
            childrenXml,
                            XmlModelNodeImpl parentModelNode, ModelNodeId parentId, XmlModelNodeToXmlMapper
                                    xmlModelNodeToXmlMapper,
                            ModelNodeHelperRegistry modelNodeHelperRegistry, SchemaRegistry schemaRegistry,
                            SubSystemRegistry subSystemRegistry, ModelNodeDataStoreManager modelNodeDSM) {
        super(schemaPath, parentId, modelNodeHelperRegistry, subSystemRegistry, schemaRegistry, modelNodeDSM);
        m_childrenXml = childrenXml;
        m_xmlModelNodeToXmlMapper = xmlModelNodeToXmlMapper;
        m_parentModelNode = parentModelNode;
        setAttributes(attributes);
    }

    public Map<QName, List<XmlModelNodeImpl>> getChildren() {
        if (m_children == null) {
            synchronized (this) {
                if (m_children == null) {
                    m_children = new LinkedHashMap<>();
                    for (Element childXml : m_childrenXml) {
                        XmlModelNodeImpl childNode = m_xmlModelNodeToXmlMapper.getModelNodeFromParentSchemaPath
                                (childXml, m_schemaPath, getModelNodeId(), this, getModelNodeDSM());
                        List<XmlModelNodeImpl> childrenOfType = m_children.get(childNode.getQName());
                        if (childrenOfType == null) {
                            childrenOfType = new ArrayList<>();
                        }
                        childrenOfType.add(childNode);
                        m_children.put(childNode.getQName(), childrenOfType);
                    }
                }
            }
        }
        return m_children;
    }

    public void addChild(QName qName, XmlModelNodeImpl child) {
        Map<QName, List<XmlModelNodeImpl>> allChildren = getChildren();
        List<XmlModelNodeImpl> childrenOfType = allChildren.get(qName);
        if (childrenOfType == null) {
            childrenOfType = new ArrayList<>();
            allChildren.put(qName, childrenOfType);
        }
        childrenOfType.add(child);
    }

    public XmlModelNodeImpl getParentModelNode() {
        return m_parentModelNode;
    }

    public void addChildAtSpecificIndex(QName qName, XmlModelNodeImpl modelNode, int insertIndex) {
        Map<QName, List<XmlModelNodeImpl>> allChildren = getChildren();
        List<XmlModelNodeImpl> childrenOfType = allChildren.get(qName);
        if (childrenOfType == null) {
            childrenOfType = new ArrayList<>();
            allChildren.put(qName, childrenOfType);
        }
        childrenOfType.add(insertIndex, modelNode);
    }

    public void toBeUpdated() {
        m_toBeUpdated = true;
    }

    public boolean isToBeUpdated() {
        return m_toBeUpdated;
    }
}
