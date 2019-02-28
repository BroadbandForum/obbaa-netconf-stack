package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.api.IndexedList;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.HelperDrivenModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.w3c.dom.Element;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;

public class XmlModelNodeImpl extends ModelNodeWithAttributes implements IndexedList.IndexableListEntry<ModelNodeId> {

    private Map<QName, IndexedList<ModelNodeId, XmlModelNodeImpl>> m_children = null;
    private final List<Element> m_childrenXml;
    private final XmlModelNodeToXmlMapper m_xmlModelNodeToXmlMapper;
    private XmlModelNodeImpl m_parentModelNode;
    private boolean m_toBeUpdated = false;

    public XmlModelNodeImpl(SchemaPath schemaPath, Map<QName, ConfigLeafAttribute> attributes, List<Element> childrenXml,
                            XmlModelNodeImpl parentModelNode, ModelNodeId parentId, XmlModelNodeToXmlMapper xmlModelNodeToXmlMapper,
                            ModelNodeHelperRegistry modelNodeHelperRegistry, SchemaRegistry schemaRegistry,
                            SubSystemRegistry subSystemRegistry, ModelNodeDataStoreManager modelNodeDSM){
        super(schemaPath, parentId,modelNodeHelperRegistry, subSystemRegistry, schemaRegistry, modelNodeDSM);
        m_childrenXml = childrenXml;
        m_xmlModelNodeToXmlMapper = xmlModelNodeToXmlMapper;
        m_parentModelNode = parentModelNode;
        setAttributes(attributes);
        setSchemaMountChild(parentModelNode == null ? false : parentModelNode.hasSchemaMount());
        setParentMountPath(parentModelNode == null ? null : parentModelNode.getModelNodeSchemaPath());
    }

    public Map<QName, IndexedList<ModelNodeId, XmlModelNodeImpl>> getChildren() {
        if(m_children == null){
            synchronized (this){
                if(m_children == null) {
                    m_children = new LinkedHashMap<>();
                    for (Element childXml : m_childrenXml) {
                        HelperDrivenModelNode.checkForGetTimeOut();
                        XmlModelNodeImpl childNode = m_xmlModelNodeToXmlMapper.getModelNodeFromParentSchemaPath(childXml, m_schemaPath, getModelNodeId(), this, getModelNodeDSM());
                        IndexedList<ModelNodeId, XmlModelNodeImpl> childrenOfType = m_children.get(childNode.getQName());
                        if(childrenOfType == null){
                            childrenOfType = new IndexedList<ModelNodeId, XmlModelNodeImpl>();
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
        Map<QName, IndexedList<ModelNodeId, XmlModelNodeImpl>> allChildren = getChildren();
        IndexedList<ModelNodeId, XmlModelNodeImpl> childrenOfType = allChildren.get(qName);
        if(childrenOfType == null){
            childrenOfType = new IndexedList<>();
            allChildren.put(qName, childrenOfType);
        }
        childrenOfType.add(child);
    }

    public XmlModelNodeImpl getParentModelNode() {
        return m_parentModelNode;
    }

	public void addChildAtSpecificIndex(QName qName, XmlModelNodeImpl modelNode, int insertIndex) {
		Map<QName, IndexedList<ModelNodeId, XmlModelNodeImpl>> allChildren = getChildren();
        IndexedList<ModelNodeId, XmlModelNodeImpl> childrenOfType = allChildren.get(qName);
        if(childrenOfType == null){
            childrenOfType = new IndexedList<>();
            allChildren.put(qName, childrenOfType);
        }
        childrenOfType.add(insertIndex, modelNode);
	}

    public void toBeUpdated() {
        m_toBeUpdated =true;
    }

    public boolean isToBeUpdated() {
        return m_toBeUpdated;
    }

    @Override
    public ModelNodeId getKey() {
        return getModelNodeId();
    }
}
