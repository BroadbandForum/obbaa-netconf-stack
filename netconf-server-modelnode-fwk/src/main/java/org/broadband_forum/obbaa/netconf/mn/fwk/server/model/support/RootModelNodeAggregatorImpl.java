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

/**
 *
 */
package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ActionException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ChangeNotification;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.CopyConfigException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditConfigException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetConfigContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NotificationContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NotificationExecutor;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.StateAttributeGetContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemValidationException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubsystemNotificationExecutor;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeCreateException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.NetconfRpcErrorUtil;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.broadband_forum.obbaa.netconf.api.messages.ActionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorType;
import org.broadband_forum.obbaa.netconf.api.server.NetconfQueryParams;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.LoggerFactory;

/**
 * An aggregator for multiple Root level ModelNodes.
 * The calls to RootModelNodeAggregatorImpl is redirected to the aggregated ModelNodes.
 *
 * @author gnanavek
 */
public class RootModelNodeAggregatorImpl implements RootModelNodeAggregator {
    private static final AdvancedLogger LOGGER = LoggerFactory.getLogger(RootModelNodeAggregatorImpl.class,
            "netconf-server-datastore", "DEBUG", "GLOBAL");

    private Map<SchemaPath, ChildContainerHelper> m_rootContainerHelpers = new HashMap<>();
    private List<ModelNode> m_moduleRoots = new ArrayList<ModelNode>();
    private ModelNodeHelperRegistry m_modelNodeHelperRegistry;
    private Map<SchemaPath, ChildListHelper> m_rootListHelpers = new HashMap<>();
    ;
    private final SchemaRegistry m_schemaRegistry;
    private Map<String, List<ModelNode>> m_componentRoots = new HashMap<>();
    private final ModelNodeDataStoreManager m_dsm;
    private NotificationExecutor m_editNotificationExecutor;
    private SubSystemRegistry m_subsystemRegistry;

    public RootModelNodeAggregatorImpl(SchemaRegistry schemaRegistry, ModelNodeHelperRegistry modelNodeHelperRegistry,
                                       ModelNodeDataStoreManager dataStoreManager, SubSystemRegistry
                                               subsystemRegistry) {
        m_schemaRegistry = schemaRegistry;
        m_modelNodeHelperRegistry = modelNodeHelperRegistry;
        m_dsm = dataStoreManager;
        m_editNotificationExecutor = new SubsystemNotificationExecutor();
        m_subsystemRegistry = subsystemRegistry;
    }

    @Override
    public synchronized RootModelNodeAggregator addModelServiceRoot(String componentId, ModelNode modelNode) {

        m_moduleRoots.add(modelNode);
        List<ModelNode> rootsFromComponent = getComponentRoots(componentId);
        rootsFromComponent.add(modelNode);
        return this;
    }

    private List<ModelNode> getComponentRoots(String componentId) {
        List<ModelNode> rootsFromComponent = m_componentRoots.get(componentId);
        if (rootsFromComponent == null) {
            rootsFromComponent = new ArrayList<>();
            m_componentRoots.put(componentId, rootsFromComponent);
        }
        return rootsFromComponent;
    }

    @Override
    public void addModelServiceRoots(String componentId, List<ModelNode> modelNodes) {
        for (ModelNode modelNode : modelNodes) {
            addModelServiceRoot(componentId, modelNode);
        }
    }

    @Override
    public List<ModelNode> getModelServiceRoots() {
        List<ModelNode> rootNodes = new ArrayList<>();
        rootNodes.addAll(m_moduleRoots);
        rootNodes.addAll(getModuleRootsFromHelpers());
        return rootNodes;
    }

    private List<ModelNode> getModuleRootsFromHelpers() {
        List<ModelNode> rootNodes = new ArrayList<>();
        for (ChildContainerHelper helper : m_rootContainerHelpers.values()) {
            ModelNode rootNode = null;
            try {
                if (helper.getSchemaNode() != null && !helper.getSchemaNode().isConfiguration()) {
                    try {
                        rootNode = new StateRootNode(helper.getSchemaNode().getPath(), null,
                                m_modelNodeHelperRegistry, m_subsystemRegistry, m_schemaRegistry, m_dsm);
                    } catch (Exception e) {
                        LOGGER.error("Error while creating root node for state container", e);
                    }
                } else {
                    rootNode = helper.getValue(null);
                }

                if (rootNode != null) {
                    rootNodes.add(rootNode);
                }
            } catch (ModelNodeGetException e) {
                LOGGER.error("Error while getting root node from helpers", e);
            }
        }
        for (ChildListHelper helper : m_rootListHelpers.values()) {
            try {
                rootNodes.addAll(helper.getValue(null, Collections.<QName, ConfigLeafAttribute>emptyMap()));
            } catch (ModelNodeGetException e) {
                LOGGER.error("Error while getting root node from helpers", e);
            }
        }
        return rootNodes;
    }

    @Override
    public List<Element> get(GetContext getContext, NetconfQueryParams params) throws GetException {
        List<Element> result = getConfigAndFillStateContext(getContext, params);

        Map<ModelNodeId, List<Element>> elements = getAllStateElementsFromSubSystems(getContext
                .getStateAttributeContext(), params);
        mergeAllStateElementsAtRightPlaces(getContext.getDoc(), result, elements);
        return result;
    }


    @Transactional(value = TxType.REQUIRED, rollbackOn = {GetException.class, RuntimeException.class, Exception.class})
    public List<Element> getConfigAndFillStateContext(GetContext getContext, NetconfQueryParams params) throws
            GetException {
        List<Element> result = new ArrayList<>();
        for (ModelNode modelNode : getModelServiceRoots()) {
            Element element = modelNode.get(getContext, params);
            if (element != null) {
                result.add(element);
            }
        }
        return result;
    }

    @Override
    public List<Element> action(ActionRequest actionRequest) throws ActionException {
        SubSystem subsystem = m_subsystemRegistry.lookupSubsystem(actionRequest.getActionTargetpath());
        return subsystem.executeAction(actionRequest);
    }

    @Override
    public List<Element> getConfig(GetConfigContext getConfigContext, NetconfQueryParams params)
            throws GetException {
        List<Element> result = new ArrayList<>();
        for (ModelNode modelNode : getModelServiceRoots()) {
            DataSchemaNode schemaNode = m_schemaRegistry.getDataSchemaNode(modelNode.getModelNodeSchemaPath());
            if ((schemaNode != null) && (schemaNode.isConfiguration())) {
                Element element = modelNode.getConfig(getConfigContext, params);
                if (element != null) {
                    result.add(element);
                }
            }
        }
        return result;
    }

    /**
     * Iterates through child nodes to merge state Nodes at right places.
     *
     * @param doc
     * @param rootElements - all root elements
     * @param elements     - map between modelnodeId and map of state elements
     */
    private void mergeAllStateElementsAtRightPlaces(Document doc, List<Element> rootElements, Map<ModelNodeId,
            List<Element>> elements) {
        // if there are no state attributes, stop now
        if (elements.isEmpty()) {
            return;
        }
        createStateRootElements(doc, rootElements, elements);
        mergeStateDataToRootElements(rootElements, elements);
    }

    private void mergeStateDataToRootElements(List<Element> rootElements, Map<ModelNodeId, List<Element>> elements) {
        Set<SchemaPath> rootSchemaPaths = getSchemaRegistry().getRootSchemaPaths();
        for (Element rootElement : rootElements) {
            SchemaPath schemaPath = getSchemaPathForElement(rootElement, rootSchemaPaths);
            if (schemaPath != null) {
                try {
                    delegateToChildElement(rootElement, schemaPath, elements, new ModelNodeId());
                } catch (ValidationException e) {
                    LOGGER.error("Error while merging state attributes ", e);
                }
            }
        }
    }

    private void createStateRootElements(Document doc, List<Element> rootElements, Map<ModelNodeId, List<Element>>
            elements) {
        Collection<DataSchemaNode> rootDataSchemaNodes = m_schemaRegistry.getRootDataSchemaNodes();
        for (ModelNodeId nodeId : elements.keySet()) {
            if (nodeId.isRootNodeId()) {
                if (isStateNode(nodeId, rootDataSchemaNodes)) {
                    ModelNodeRdn firstRdn = nodeId.getRdns().get(0);
                    rootElements.add(doc.createElementNS(firstRdn.getNamespace(), firstRdn.getRdnValue()));
                }
            }
        }
    }

    private boolean isStateNode(ModelNodeId nodeId, Collection<DataSchemaNode> rootDataSchemaNodes) {
        ModelNodeRdn firstRdn = nodeId.getRdns().get(0);
        for (DataSchemaNode schemaNode : rootDataSchemaNodes) {
            QName qName = schemaNode.getQName();
            if (qName.getNamespace().toString().equals(firstRdn.getNamespace()) &&
                    qName.getLocalName().equals(firstRdn.getRdnValue())) {
                return !(schemaNode.isConfiguration());
            }
        }
        return false;
    }

    /**
     * Iterates through Map<SubSystem, Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>>> to get state elements
     * from corresponding subsystem.
     *
     * @param params
     * @return Map<ModelNodeId, List<Element>>
     */
    private Map<ModelNodeId, List<Element>> getAllStateElementsFromSubSystems(StateAttributeGetContext stateContext,
                                                                              NetconfQueryParams params) throws
            GetException {
        Map<ModelNodeId, List<Element>> nodeIds = stateContext.getStateMatchNodes();

        for (Entry<SubSystem, Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>>> subSystemEntry : stateContext
                .getSubSystems().entrySet()) {
            try {
                SubSystem subSystem = subSystemEntry.getKey();
                Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> attributes = subSystemEntry.getValue();
                // call retrieveStateAttributes only once for each SubSystem
                Map<ModelNodeId, List<Element>> stateAttributeValues = subSystem.retrieveStateAttributes(attributes,
                        params);

                for (Entry<ModelNodeId, List<Element>> entry : stateAttributeValues.entrySet()) {
                    ModelNodeId nodeId = entry.getKey();
                    if (nodeIds.containsKey(nodeId)) {
                        nodeIds.get(nodeId).addAll(entry.getValue());
                    } else {
                        nodeIds.put(nodeId, entry.getValue());
                    }
                }

            } catch (GetAttributeException e) {
                NetconfRpcError netconfRpcError = NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag
                        .DATA_MISSING, "Could not get state values " + e.getMessage());
                GetException exception = new GetException(netconfRpcError);
                exception.addSuppressed(e);
                throw exception;
            }

        }
        return nodeIds;
    }

    private ModelNodeId getModelNodeId(Element element, ModelNodeId parentNodeId, DataSchemaNode schemaNode) {
        ModelNodeId modelNodeId = new ModelNodeId(parentNodeId);
        modelNodeId.addRdn(ModelNodeRdn.CONTAINER, schemaNode.getQName().getNamespace().toString(), getContainerName
                (schemaNode));
        Set<QName> keys = m_modelNodeHelperRegistry.getNaturalKeyHelpers(schemaNode.getPath()).keySet();

        if (keys != null) {
            for (QName key : keys) {
                NodeList childNodes = element.getChildNodes();
                for (int i = 0; i < childNodes.getLength(); i++) {
                    Node child = childNodes.item(i);
                    if (child.getNodeType() == Node.ELEMENT_NODE) {
                        QName childQName = SchemaRegistryUtil.getChildQname(child, schemaNode, m_schemaRegistry);
                        if (childQName != null) {
                            if (key.equals(childQName)) {
                                modelNodeId.addRdn(childQName.getLocalName(), childQName.getNamespace().toString(),
                                        child.getTextContent());
                                break;
                            }
                        }
                    }
                }
            }
        }

        return modelNodeId;
    }

    private String getContainerName(DataSchemaNode schemaNode) {
        return schemaNode.getPath().getLastComponent().getLocalName();
    }

    private void delegateToChildElement(Element element, SchemaPath schemaPath, Map<ModelNodeId, List<Element>>
            stateElements, ModelNodeId parentNodeId)
            throws ValidationException {
        // if there are no state attributes, stop now
        if (stateElements.isEmpty()) {
            return;
        }

        DataSchemaNode schemaNode = m_schemaRegistry.getDataSchemaNode(schemaPath);

        // get ModelNodeId from current node
        ModelNodeId modelNodeId = getModelNodeId(element, parentNodeId, schemaNode);
        List<Element> elements = stateElements.remove(modelNodeId);
        if (elements != null && !elements.isEmpty()) {
            Node refChild = element.getChildNodes().item(0);
            List<QName> keyQNames = Collections.emptyList();
            if (schemaNode instanceof ListSchemaNode) {
                keyQNames = ((ListSchemaNode) schemaNode).getKeyDefinition();
                refChild = element.getChildNodes().item(keyQNames.size());
            }

            for (Element e : elements) {
                //the reply from subsystems contains keys as well, so stack need not copy them again
                if (!isKey(keyQNames, e)) {
                    element.insertBefore(element.getOwnerDocument().importNode(e, true), refChild);
                }
            }
        }

        // go though children
        NodeList nodes = element.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node childNode = nodes.item(i);
            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                Collection<DataSchemaNode> children = m_schemaRegistry.getChildren(schemaPath);
                SchemaPath childSchemaPath = getSchemaPathForElement((Element) childNode, getSchemaPathsForNodes
                        (children));
                if (childSchemaPath != null) {
                    DataSchemaNode childSchemaNode = m_schemaRegistry.getDataSchemaNode(childSchemaPath);
                    if (childSchemaNode instanceof DataNodeContainer) {
                        delegateToChildElement((Element) childNode, childSchemaPath, stateElements, modelNodeId);
                    }
                }
            }
        }
    }

    private boolean isKey(List<QName> keyQNames, Element e) {
        for (QName keyQname : keyQNames) {
            if (e.getLocalName().equals(keyQname.getLocalName()) && e.getNamespaceURI().equals(keyQname.getNamespace
                    ().toString())) {
                return true;
            }
        }
        return false;
    }

    private Collection<SchemaPath> getSchemaPathsForNodes(Collection<DataSchemaNode> nodes) {
        List<SchemaPath> schemaPaths = new ArrayList<>();
        for (DataSchemaNode node : nodes) {
            schemaPaths.add(node.getPath());
        }
        return schemaPaths;
    }

    private SchemaPath getSchemaPathForElement(Element element, Collection<SchemaPath> availableSchemaPaths) {
        for (SchemaPath schemaPath : availableSchemaPaths) {
            QName lastComponent = schemaPath.getLastComponent();
            if (lastComponent.getNamespace().toString().equals(element.getNamespaceURI()) && lastComponent
                    .getLocalName().equals(element.getLocalName())) {
                return schemaPath;
            }
        }
        return null;
    }

    public SchemaRegistry getSchemaRegistry() {
        return m_schemaRegistry;
    }

    @Override
    public List<EditContainmentNode> editConfig(EditConfigRequest request, NotificationContext notificationContext)
            throws EditConfigException {
        List<EditContainmentNode> editTrees = new ArrayList<EditContainmentNode>();
        //go over the list of items to edit, and choose the right node to edit and let it edit itself.
        for (Element rootElement : request.getConfigElement().getConfigElementContents()) {
            ModelNode modelNode = null;
            try {
                modelNode = getMatchingRootNode(rootElement, getModelServiceRoots());
            } catch (GetAttributeException e) {
                LOGGER.error("Error while getting matching root node", e);
                throw new EditConfigException(e.getRpcError());
            }
            if (modelNode == null) {
                try {
                    modelNode = createRootModelNode(rootElement);
                } catch (ModelNodeCreateException e) {
                    throw new EditConfigException(NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag
                                    .OPERATION_FAILED,
                            "Error while creating root node"));
                }
            }
            EditContainmentNode editTree = new EditContainmentNode();
            editTrees.add(editTree);
            if (request.getDefaultOperation() != null) {
                editTree.setEditOperation(request.getDefaultOperation());
            }
            try {
                m_dsm.beginModify();
                //prepare tree for this root node and let it edit itself
                modelNode.prepareEditSubTree(editTree, rootElement);
                EditContext editContext = new EditContext(editTree, notificationContext, request.getErrorOption(),
                        request.getClientInfo());
                modelNode.editConfig(editContext);

                //call end modify so that the Subsystems can access the backing store to validate changes.
                m_dsm.endModify();
                LOGGER.debug("Sending pre-commit notifications to subsystems");
                Map<SubSystem, List<ChangeNotification>> subSystemNotificationMap = m_editNotificationExecutor
                        .getSubSystemNotificationMap(request.getTarget(), notificationContext.getNotificationInfos(),
                                request);

                m_editNotificationExecutor.sendPreCommitNotifications(subSystemNotificationMap);
                LOGGER.debug("Sending pre-commit notifications to subsystems done");

            } catch (ValidationException e) {
                LOGGER.error("Validation constraints failed due to: ", e);
                throw new EditConfigException(e.getRpcError());
            } catch (SubSystemValidationException e) {
                LOGGER.error("Subsystem Validation failed due to: ", e);
                throw new EditConfigException(e.getRpcError());
            }
        }
        return editTrees;
    }

    private ModelNode createRootModelNode(Element rootElement) throws ModelNodeCreateException {
        ModelNode newRootModelNode = null;
        for (SchemaPath rootSchemaPath : m_rootContainerHelpers.keySet()) {
            QName rootNodeQname = rootSchemaPath.getLastComponent();
            if (rootElement.getNamespaceURI().equals(rootNodeQname.getNamespace().toString()) && rootElement
                    .getLocalName().equals(rootNodeQname.getLocalName())) {
                newRootModelNode = m_rootContainerHelpers.get(rootSchemaPath).createChild(null, Collections.<QName,
                        ConfigLeafAttribute>emptyMap());
                return newRootModelNode;
            }
        }
        for (SchemaPath rootSchemaPath : m_rootListHelpers.keySet()) {
            QName rootNodeQname = rootSchemaPath.getLastComponent();
            if (rootElement.getNamespaceURI().equals(rootNodeQname.getNamespace().toString()) && rootElement
                    .getLocalName().equals(rootNodeQname.getLocalName())) {
                Map<QName, ConfigLeafAttribute> KeyValues = new HashMap<QName, ConfigLeafAttribute>();
                for (QName keyQName : m_modelNodeHelperRegistry.getNaturalKeyHelpers(rootSchemaPath).keySet()) {
                    Node keyNode = DocumentUtils.getChildNodeByName(rootElement, keyQName.getLocalName(), keyQName
                            .getNamespace
                            ().toString());
                    if (keyNode != null) {
                        ConfigLeafAttribute configAttribute = null;
                        try {
                            configAttribute = ConfigAttributeFactory.getConfigAttribute(m_schemaRegistry, null,
                                    rootNodeQname, keyNode);
                        } catch (InvalidIdentityRefException e) {
                            throw new ModelNodeCreateException(e.getRpcError());
                        }
                        KeyValues.put(keyQName, configAttribute);
                    }
                }
                ChildListHelper childListHelper = m_rootListHelpers.get(rootSchemaPath);
                newRootModelNode = childListHelper.createModelNode(null, KeyValues);
                try {
                    return childListHelper.addChild(null, newRootModelNode);
                } catch (ModelNodeSetException e) {
                    throw new ModelNodeCreateException(e);
                }
            }
        }
        return null;
    }

    private void deleteRootModelNode(ModelNode node, SchemaPath nodeSchemaPath) throws CopyConfigException {
        SchemaNode schemaNode = m_schemaRegistry.getDataSchemaNode(nodeSchemaPath);
        if (schemaNode instanceof ContainerSchemaNode) {
            ChildContainerHelper childContainerHelper = m_rootContainerHelpers.get(nodeSchemaPath);
            if (childContainerHelper != null) {
                try {
                    childContainerHelper.deleteChild(node);
                } catch (ModelNodeDeleteException e) {
                    LOGGER.error("Error while deleting the root container during copy-config :{} ", nodeSchemaPath);
                    throw new CopyConfigException(NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag
                                    .OPERATION_FAILED,
                            "Error while deleting root container" + nodeSchemaPath));
                }
            }
        } else if (schemaNode instanceof ListSchemaNode) {
            ChildListHelper listHelper = m_rootListHelpers.get(nodeSchemaPath);
            if (listHelper != null) {
                try {
                    listHelper.removeChild(null, node);
                } catch (ModelNodeDeleteException e) {
                    LOGGER.error("Error while deleting the root list node during copy-config : {}", nodeSchemaPath);
                    throw new CopyConfigException(NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag
                                    .OPERATION_FAILED,
                            "Error while deleting root list" + nodeSchemaPath));
                }
            }
        }
    }

    /**
     * This method compares localName only.
     *
     * @param rootElement
     * @param modelServiceRoots
     * @return
     */
    private ModelNode getMatchingRootNode(Element rootElement, List<ModelNode> modelServiceRoots) throws
            GetAttributeException {
        for (ModelNode modelServiceRoot : modelServiceRoots) {
            if (rootElement.getLocalName().equals(modelServiceRoot.getQName().getLocalName()) &&
                    rootElement.getNamespaceURI().equals(modelServiceRoot.getQName().getNamespace().toString())) {
                DataSchemaNode dataSchemaNode = m_schemaRegistry.getDataSchemaNode(modelServiceRoot
                        .getModelNodeSchemaPath());
                if (dataSchemaNode instanceof ListSchemaNode) {
                    if (keysMatch(rootElement, modelServiceRoot, (ListSchemaNode) dataSchemaNode)) {
                        return modelServiceRoot;
                    }
                } else {
                    return modelServiceRoot;
                }
            }
        }
        return null;
    }

    private boolean keysMatch(Element rootElement, ModelNode modelServiceRoot, ListSchemaNode listSchemaNode) throws
            GetAttributeException {
        Map<QName, String> keyValues = modelServiceRoot.getListKeys();
        for (QName keyQname : listSchemaNode.getKeyDefinition()) {
            Node keyNode = DocumentUtils.getChildNodeByName(rootElement, keyQname.getLocalName(), keyQname
                    .getNamespace()
                    .toASCIIString());
            if (keyNode != null) {
                if (!keyValues.get(keyQname).equals(keyNode.getTextContent())) {
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    private boolean validNamespace(Element rootElement) {
        String namespace = rootElement.getNamespaceURI();
        if (namespace != null && m_schemaRegistry.isKnownNamespace(namespace)) {
            return true;
        }
        return false;
    }

    @Transactional(value = TxType.REQUIRED, rollbackOn = {CopyConfigException.class, RuntimeException.class,
            Exception.class})
    public void copyConfig(List<Element> copyConfigElements) throws CopyConfigException {
        m_dsm.beginModify();
        for (Element copyConfigElement : copyConfigElements) {
            if (!validNamespace(copyConfigElement)) {
                throw new CopyConfigException(NetconfRpcError.getUnknownNamespaceError(copyConfigElement
                        .getNamespaceURI(), copyConfigElement.getLocalName(), NetconfRpcErrorType.Application));
            }
        }
        Map<SchemaPath, Element> rootNodeToConfigElementMap = null;
        try {
            rootNodeToConfigElementMap = getRootNodeToConfigElementMap(getModelServiceRoots(), copyConfigElements);
        } catch (GetAttributeException e) {
            LOGGER.error("Error while getting the matching root node ", e);
            throw new CopyConfigException(e.getRpcError());
        }

        //check the request is valid
        checkEachRootNodeHasCopyConfigElement(getModelServiceRoots(), rootNodeToConfigElementMap);
        checkEachCopyConfigElementHasRootNode(copyConfigElements, rootNodeToConfigElementMap);

        //all good, so lets invoke copy-config on each of the modelNode roots exclude config false modelNodes
        for (ModelNode modelNode : getModelServiceRoots()) {
            if (isConfigurationNode(modelNode)) {
                modelNode.copyConfig(rootNodeToConfigElementMap.get(modelNode.getModelNodeSchemaPath()));

            }
        }
        m_dsm.endModify();
    }

    @Override
    public void addModelServiceRootHelper(SchemaPath rootNodeSchemaPath, ChildContainerHelper rootNodeHelper) {
        m_rootContainerHelpers.put(rootNodeSchemaPath, rootNodeHelper);
    }

    @Override
    public void addModelServiceRootHelper(SchemaPath rootNodeSchemaPath, ChildListHelper rootNodeHelper) {
        m_rootListHelpers.put(rootNodeSchemaPath, rootNodeHelper);
    }

    @Override
    public void removeModelServiceRootHelpers(SchemaPath rootSchemaPath) {
        m_rootContainerHelpers.remove(rootSchemaPath);
        m_rootListHelpers.remove(rootSchemaPath);
    }

    @Override
    public void removeModelServiceRoot(String componentId) {
        List<ModelNode> modelNodes = m_componentRoots.get(componentId);
        if (modelNodes != null) {
            m_moduleRoots.removeAll(modelNodes);
        }
    }

    private void checkEachCopyConfigElementHasRootNode(List<Element> copyConfigElements, Map<SchemaPath, Element>
            rootNodeToConfigElementMap) throws CopyConfigException {
        for (Element copyConfigElement : copyConfigElements) {
            if (!rootNodeToConfigElementMap.containsValue(copyConfigElement)) {
                try {
                    ModelNode rootNodeCreated = createRootModelNode(copyConfigElement);
                    rootNodeToConfigElementMap.put(rootNodeCreated.getModelNodeSchemaPath(), copyConfigElement);
                } catch (ModelNodeCreateException e) {
                    LOGGER.error("Error while creating rootnode", e);
                    throw new CopyConfigException(NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag
                                    .OPERATION_FAILED,
                            "Error while creating root node"));
                }
            }
        }
    }

    private void checkEachRootNodeHasCopyConfigElement(List<ModelNode> modelServiceRoots, Map<SchemaPath, Element>
            rootNodeToConfigElementMap) throws CopyConfigException {
        for (ModelNode node : modelServiceRoots) {
            if (isConfigurationNode(node)) {
                SchemaPath nodeSchemaPath = node.getModelNodeSchemaPath();
                if (!rootNodeToConfigElementMap.containsKey(nodeSchemaPath)) {
                    deleteRootModelNode(node, nodeSchemaPath);
                }
            }
        }
    }

    private boolean isConfigurationNode(ModelNode node) {
        DataSchemaNode rootNode = m_schemaRegistry.getDataSchemaNode(node.getModelNodeSchemaPath());
        return rootNode.isConfiguration();
    }

    private Map<SchemaPath, Element> getRootNodeToConfigElementMap(List<ModelNode> modelServiceRoots, List<Element>
            copyConfigElements) throws GetAttributeException {
        Map<SchemaPath, Element> rootNodeToConfigElementMap = new HashMap<>();
        for (Element copyConfigElement : copyConfigElements) {
            ModelNode node = getMatchingRootNode(copyConfigElement, modelServiceRoots);
            if (node != null) {
                rootNodeToConfigElementMap.put(node.getModelNodeSchemaPath(), copyConfigElement);
            }
        }
        return rootNodeToConfigElementMap;
    }

}
