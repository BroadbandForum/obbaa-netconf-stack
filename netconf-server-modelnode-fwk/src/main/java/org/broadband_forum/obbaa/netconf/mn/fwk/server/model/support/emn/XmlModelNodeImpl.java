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

import static org.broadband_forum.obbaa.netconf.server.RequestTask.CURRENT_REQ_TYPE;
import static org.broadband_forum.obbaa.netconf.server.RequestTask.REQ_TYPE_EDIT_CONFIG;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.api.IndexedList;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.server.NetconfQueryParams;
import org.broadband_forum.obbaa.netconf.mn.fwk.ChangeTreeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.ChangeTreeNodeImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.WritableChangeTreeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.AnvExtensions;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ConfigAttributeGetContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.StateAttributeGetContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.DataStoreException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.dom.DOMisPasswordAttributeRemover;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildContainerHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.HelperDrivenModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeGetException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithIndex;
import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XmlModelNodeImpl extends ModelNodeWithAttributes implements IndexedList.IndexableListEntry<ModelNodeId>, ModelNodeWithIndex {
    private final ModelNodeIndex m_index;
    private final boolean m_newNode;
    private boolean m_childrenMaterialised = false;
    private Map<QName, IndexedList<ModelNodeId, XmlModelNodeImpl>> m_children = null;
    private final Document m_document;
    private List<Element> m_childrenXml;
    private final XmlModelNodeToXmlMapper m_xmlModelNodeToXmlMapper;
    private XmlModelNodeImpl m_parentModelNode;
    private boolean m_toBeUpdated = false;
    private XmlModelNodeImpl m_schemaRoot;
    private ModelNodeId m_indexNodeId;
    private NodeIndex m_nodeIndex;
    private boolean m_childrenXMLLoadedFromDb;
    private Object m_storedParentEntity;
    private SchemaPath m_storedParentSchemaPath;

    public XmlModelNodeImpl(Document document, SchemaPath schemaPath, Map<QName, ConfigLeafAttribute> attributes, List<Element> childrenXml,
                            XmlModelNodeImpl parentModelNode, ModelNodeId parentId, XmlModelNodeToXmlMapper xmlModelNodeToXmlMapper,
                            ModelNodeHelperRegistry modelNodeHelperRegistry, SchemaRegistry schemaRegistry,
                            SubSystemRegistry subSystemRegistry, ModelNodeDataStoreManager modelNodeDSM, Object storedParentEntity, boolean childrenXMLLoadedFromDb, SchemaPath storedParentSchemaPath){
        this(document, schemaPath, attributes, childrenXml, parentModelNode, parentId, xmlModelNodeToXmlMapper, modelNodeHelperRegistry,
                schemaRegistry, subSystemRegistry, modelNodeDSM, false, storedParentEntity, childrenXMLLoadedFromDb, storedParentSchemaPath);
    }

    public XmlModelNodeImpl(Document document, SchemaPath schemaPath, Map<QName, ConfigLeafAttribute> attributes, List<Element> childrenXml,
                            XmlModelNodeImpl parentModelNode, ModelNodeId parentId, XmlModelNodeToXmlMapper xmlModelNodeToXmlMapper,
                            ModelNodeHelperRegistry modelNodeHelperRegistry, SchemaRegistry schemaRegistry,
                            SubSystemRegistry subSystemRegistry, ModelNodeDataStoreManager modelNodeDSM, boolean newNode, Object storedParentEntity, boolean childrenXMLLoadedFromDb, SchemaPath storedParentSchemaPath) {
        super(schemaPath, parentId,modelNodeHelperRegistry, subSystemRegistry, schemaRegistry, modelNodeDSM);
        m_document = document;
        m_childrenXml = childrenXml;
        m_childrenXMLLoadedFromDb = childrenXMLLoadedFromDb;
        m_xmlModelNodeToXmlMapper = xmlModelNodeToXmlMapper;
        m_parentModelNode = parentModelNode;
        m_storedParentEntity = storedParentEntity;
        m_storedParentSchemaPath = storedParentSchemaPath;
        if(parentModelNode == null){
            m_schemaRoot = this;
            m_index = new ModelNodeIndex();
        } else {
            m_schemaRoot = parentModelNode.getSchemaRoot();
            if(hasSchemaMount()){
                m_schemaRoot = this;
            }
            m_index = parentModelNode.getIndex();
        }
        m_newNode = newNode;
        super.setAttributes(attributes);
        setSchemaMountChild(parentModelNode != null && parentModelNode.hasSchemaMount());
        setParentMountPath(parentModelNode == null ? null : parentModelNode.getModelNodeSchemaPath());
        addToIndex(m_newNode);

    }

    public ModelNodeIndex getIndex() {
        return m_index;
    }

    private void addToIndex(boolean newNode) {
        m_nodeIndex = m_index.addToIndex(this, newNode);
    }

    public ModelNodeId getIndexNodeId() {
        if(m_indexNodeId == null){
            m_indexNodeId = new ModelNodeId(getModelNodeId());
            m_indexNodeId.removeFirst(m_schemaRoot.getModelNodeId().getRdnsReadOnly().size());
        }
        return m_indexNodeId;
    }

    @Override
    public Map<String, List<ConfigLeafAttribute>> getAttrsOfType(String attrTypeXPath) {
        return m_index.getAttrsOfType(attrTypeXPath);
    }

    @Override
    public Object getIndexedValue(String xpath) {
        return m_index.getIndexedValue(xpath);
    }

    @Override
    public void setAttributes(Map<QName, ConfigLeafAttribute> attrValues) {
        if(!getAttributes().isEmpty()) {
            m_index.clearAttributeIndices(m_nodeIndex);
        }
        super.setAttributes(attrValues);
        ModelNodeId nodeId = getIndexNodeId();
        String xPathWithoutPrefixes = nodeId.xPathString(getSchemaRegistry(), true, true);
        m_index.buildAttrIndices(xPathWithoutPrefixes, this, m_nodeIndex);
    }

    public XmlModelNodeImpl getSchemaRoot() {
        return m_schemaRoot;
    }

    @Override
    public ModelNodeId getSchemaRootId() {
        return m_schemaRoot.getModelNodeId();
    }

    public Map<QName, IndexedList<ModelNodeId, XmlModelNodeImpl>> getChildren() {
        if(m_children == null){
            synchronized (this){
                m_childrenMaterialised = true;
                if(m_children == null) {
                    m_children = new LinkedHashMap<>();
                    for (Element childXml : getChildrenXml()) {
                        HelperDrivenModelNode.checkForGetTimeOut();
                            XmlModelNodeImpl childNode = m_xmlModelNodeToXmlMapper.getModelNodeFromParentSchemaPath(childXml, m_schemaPath, getModelNodeId(), this, getModelNodeDSM(), m_storedParentEntity, m_storedParentSchemaPath);
                            IndexedList<ModelNodeId, XmlModelNodeImpl> childrenOfType = m_children.get(childNode.getQName());
                            if (childrenOfType == null) {
                                childrenOfType = new IndexedList<>();
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

    public void updateChildIndex(QName qName, XmlModelNodeImpl modelNode, int newIndex) {
        Map<QName, IndexedList<ModelNodeId, XmlModelNodeImpl>> allChildren = getChildren();
        IndexedList<ModelNodeId, XmlModelNodeImpl> childrenOfType = allChildren.get(qName);
        if (childrenOfType != null) {
            if(newIndex != -1 && newIndex < childrenOfType.size()) {
                childrenOfType.remove(modelNode.getKey());
                childrenOfType.add(newIndex, modelNode);
            } else {
                throw new DataStoreException("Specified index is invalid");
            }
        } else {
            throw new DataStoreException("Child entity does not exist");
        }
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

    public void removeChild(XmlModelNodeImpl child) {
        getChildren().remove(child.getModelNodeId());
        m_index.removeNode(child);
    }

    public NodeIndex getNodeIndex() {
        return m_nodeIndex;
    }

    @Override
    public boolean isNewNode() {
        return m_index.isNewNode(getModelNodeId().xPathString(getSchemaRegistry(), true,true));
    }

    @Override
    public boolean isNewNode(String nodeXPath) {
        return m_index.isNewNode(nodeXPath);
    }

    public boolean childrenMaterialised() {
        return m_childrenMaterialised;
    }

    public List<Element> getChildrenXml() {
        if(!m_childrenXMLLoadedFromDb && m_xmlModelNodeToXmlMapper != null) {
            SchemaRegistry schemaRegistry = getSchemaRegistryForCurrentNode();
            m_childrenXml = m_xmlModelNodeToXmlMapper.loadXmlValue(m_storedParentEntity, m_schemaPath, schemaRegistry, m_storedParentSchemaPath);
            m_childrenXMLLoadedFromDb = true;
        }
        return m_childrenXml;
    }

    public Document getDocument() {
        return m_document;
    }

    public WritableChangeTreeNode buildCtnForDelete(WritableChangeTreeNode parent) {
        SchemaRegistry sr = getSchemaRegistryForCurrentNode();
        WritableChangeTreeNode ctn = new ChangeTreeNodeImpl(sr, parent, getModelNodeId(), sr.getDataSchemaNode(getModelNodeSchemaPath()),
                parent.getNodesIndex(), parent.getAttributeIndex(), parent.getChangedNodeTypes(), parent.getContextMap(), parent.getNodesOfTypeIndexWithinSchemaMount());

        ctn.setChangeType(ChangeTreeNode.ChangeType.delete);
        ctn.setImplied(parent.isImplied());
        ctn.setEditChangeSource(parent.getChangeSource());
        return ctn;
    }

    @Override
    protected void copyCompletelyToOutput(NetconfClientInfo clientInfo, Document doc, ModelNodeId modelNodeId, Element parent, boolean includeState, StateAttributeGetContext stateAttributeContext, ConfigAttributeGetContext configContext, NetconfQueryParams params) throws DOMException, GetException, GetAttributeException {
        if (!includeState && params.isIncludeConfig() && params.getDepth() == NetconfQueryParams.UNBOUNDED && isVisible()) {
            //Append childrenXml only if the node does not have any children which has its own Entity Class
            SchemaRegistry schemaRegistry = getMountRegistry();
            if (!this.isRoot() && !isAnyChildNodeHasItsOwnEntityClass(schemaRegistry)
                    && !REQ_TYPE_EDIT_CONFIG.equals(RequestScope.getCurrentScope().getFromCache(CURRENT_REQ_TYPE))) {
                LOGGER.debug("Appending childrenXml to response directly instead of iterating over child nodes");
                List<String> keyValues = new ArrayList<>();
                copyKeyAttribute(doc, parent, modelNodeId, keyValues, params);
                copyAllConfigAttributesToOutput(doc, parent, keyValues, params);
                copyAllConfigLeafListToOutput(doc, parent, keyValues, params);
                List<Element> xmlChildren = getChildrenXml();
                for (Element child : xmlChildren) {
                    DOMisPasswordAttributeRemover doMisPasswordAttributeRemover = new DOMisPasswordAttributeRemover(child);
                    doMisPasswordAttributeRemover.traverse();
                    parent.appendChild(doc.importNode(child, true));
                }
                return;
            }
        }
        super.copyCompletelyToOutput(clientInfo, doc, modelNodeId, parent, includeState, stateAttributeContext, configContext, params);
    }

    @Override
    protected void copyAllContainerToOutput(NetconfClientInfo clientInfo, Document doc, Element parent, boolean includeState,
                                            StateAttributeGetContext stateContext, ConfigAttributeGetContext configContext, NetconfQueryParams params) throws GetException {
        for (ChildContainerHelper helper : getModelNodeHelperRegistry().getChildContainerHelpers(this.getModelNodeSchemaPath()).values()) {
            try {
                if (helper.isMandatory() || helper.isChildSet(this)) {
                    SchemaPath childSP = helper.getSchemaNode().getPath();
                    DataSchemaNode dataSchemaNode = getSchemaRegistry().getDataSchemaNode(childSP);
                    if (AnvExtensions.MOUNT_POINT.isExtensionIn(dataSchemaNode) && !grandChildNodesToBeLoaded(params)) {
                        ModelNodeId childMnId = getChildContainerMNId(getModelNodeId(), childSP);
                        Element element = checkPermissionAndGetElement(doc, childSP, configContext, clientInfo, childMnId);
                        if(element != null){
                            parent.appendChild(element);
                        }
                    } else {
                        super.copyContainerToOutput(clientInfo,doc,parent,includeState,stateContext,configContext,params, helper);
                    }
                }
            } catch (ModelNodeGetException e) {
                LOGGER.error("failed to add container to output " + helper, e);
            }
        }
    }

    private ModelNodeId getChildContainerMNId(ModelNodeId parentMNId, SchemaPath childSP) {
        return new ModelNodeId(parentMNId).addRdn(new ModelNodeRdn(ModelNodeRdn.CONTAINER, childSP.getLastComponent().getNamespace().toString(), childSP.getLastComponent().getLocalName()));
    }

    private boolean isAnyChildNodeHasItsOwnEntityClass(SchemaRegistry schemaRegistry) {
        EntityRegistry entityRegistry = getModelNodeDSM().getEntityRegistry(m_schemaPath, schemaRegistry);
        Collection<DataSchemaNode> childNodes = schemaRegistry.getNonChoiceChildren(m_schemaPath);
        for (DataSchemaNode dataSchemaNode : childNodes) {
            if (dataSchemaNode instanceof ContainerSchemaNode || dataSchemaNode instanceof ListSchemaNode) {
                Class entityClass = entityRegistry.getEntityClass(dataSchemaNode.getPath());
                if (entityClass != null) {
                    return true;
                }
            }
        }
        return false;
    }
}
