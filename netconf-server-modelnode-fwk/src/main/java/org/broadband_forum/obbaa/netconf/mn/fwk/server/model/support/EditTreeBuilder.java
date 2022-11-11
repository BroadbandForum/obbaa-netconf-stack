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

import static java.util.Collections.reverse;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.EditTreeTransformer.getLocalName;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.api.messages.EditConfigOperations;
import org.broadband_forum.obbaa.netconf.api.messages.InsertOperation;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorType;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaMountRegistryProvider;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.AnvExtensions;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditChangeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditConfigException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditMatchNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.NetconfRpcErrorUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class EditTreeBuilder {
    private static final String EMPTY_STR = "";
    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(EditTreeBuilder.class, LogAppNames.NETCONF_STACK);

    public void prepareEditSubTree(EditContainmentNode root, Element editConfigXml, SchemaPath modelNodeSchemaPath,
                                   SchemaRegistry schemaRegistry, ModelNodeHelperRegistry modelNodeHelperRegistry, ModelNodeId modelNodeId)
            throws EditConfigException {

        if (editConfigXml != null) {
            String nodeName = getLocalName(editConfigXml);
            if (!getContainerName(modelNodeSchemaPath).equals(nodeName)) {
                throw new EditConfigException(NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.INVALID_VALUE,
                        "Invalid root config node :" + nodeName));
            }

            root.setQName(getQName(schemaRegistry, editConfigXml));
            String editOperation = EditTreeBuilder.getEditOperation(editConfigXml);
            if (editOperation != null) {
                root.setEditOperation(editOperation);
            }

            // add insert attributes for list
            String insertAttribute = EditTreeBuilder.resolveInsertAttributes(editConfigXml, NetconfResources.INSERT);
            String keyAttribute = EditTreeBuilder.resolveInsertAttributes(editConfigXml, NetconfResources.KEY);
            String visibilityAttribute = EditTreeBuilder.resolveVisibilityAttribute(editConfigXml);
            String parentOperation = (root.getParent() != null)? root.getParent().getEditOperation() : EditConfigOperations.NONE;
            DataSchemaNode dataSchemaNode = schemaRegistry.getDataSchemaNode(modelNodeSchemaPath);
            root.setInsertOperation(getInsertOperation(insertAttribute, keyAttribute, dataSchemaNode, parentOperation));
            root.setVisibility(getVisibilityValue(visibilityAttribute));

            NodeList nodeList = editConfigXml.getChildNodes();
            // processChildren
            List<Node> listsAndContainersToBeProcessed = new ArrayList<>();

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node child = nodeList.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    try {
                        QName childQName = getQName(schemaRegistry, child);
                        DataSchemaNode childSchemaNode = childQName != null ?
                                schemaRegistry.getChild(modelNodeSchemaPath, childQName) : null;
                        if ((childQName == null || childSchemaNode == null)
                                && AnvExtensions.MOUNT_POINT.isExtensionIn(schemaRegistry.getDataSchemaNode(modelNodeSchemaPath))) {
                            SchemaMountRegistryProvider provider = schemaRegistry.getMountRegistry().getProvider(modelNodeSchemaPath);
                            provider.setCorrectPlugMountContextInCache(root.getParent());
                            schemaRegistry = provider.getSchemaRegistry(root);

                            if (schemaRegistry != null) {
                                childQName = getQName(schemaRegistry, child);
                                modelNodeHelperRegistry = provider.getModelNodeHelperRegistry(root);
                            }
                        }
                        // throw an error if childQname is null
                        handleChildNotFoundCase(child, childQName);

                        if (modelNodeHelperRegistry.getNaturalKeyHelper(modelNodeSchemaPath, childQName) != null) {
                            // it is a match node
                            EditTreeBuilder.addEditMatchNode(schemaRegistry, modelNodeSchemaPath, root,
                                    childQName, child);
                        } else if(!(isDeleteOperation(getOperationAttribute(root, child.getParentNode())))){
                            // Don't add non match nodes as child of node with delete operation
                            if (modelNodeHelperRegistry.getChildListHelper(modelNodeSchemaPath, childQName) != null) {
                                listsAndContainersToBeProcessed.add(child);
                            } else if (modelNodeHelperRegistry.getChildContainerHelper(modelNodeSchemaPath, childQName) != null) {
                                listsAndContainersToBeProcessed.add(child);
                            } else  if (modelNodeHelperRegistry.getConfigAttributeHelper(modelNodeSchemaPath, childQName) != null) {
                                // it is a change node
                                EditTreeBuilder.addLeafEditChangeNode(root, childQName, child, modelNodeSchemaPath, schemaRegistry);
                            } else if (modelNodeHelperRegistry.getConfigLeafListHelper(modelNodeSchemaPath, childQName) != null) {
                                // it is a change node
                                EditTreeBuilder.addLeafListEditChangeNode(root, childQName, child, modelNodeSchemaPath, schemaRegistry);
                            } else {
                                // it may be a state attribute, which has no place in edit request
                                // TODO: FNMS-10123 add all error fields
                                LOGGER.error("Invalid item in edit request :{}; child of - {}", child.getNodeName(), modelNodeSchemaPath);
                                throw new EditConfigException(NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.INVALID_VALUE,
                                        "Invalid item in edit request: "
                                                + child.getNodeName()).setErrorPath(modelNodeId.xPathString(schemaRegistry), modelNodeId.xPathStringNsByPrefix(schemaRegistry)));
                            }
                        }
                    } catch (IllegalArgumentException e) {
                        // TODO: FNMS-10123 add all error fields
                        LOGGER.error("Error while constructing edit-tree from edit-config", e);
                        throw new EditConfigException(NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.OPERATION_FAILED,
                                e.getMessage()));
                    }

                } else {
                    LOGGER.trace("found non-element child : {}", child.getNodeName());
                }
            }

            for (Node child : listsAndContainersToBeProcessed) {
                try {
                    String operation = getOperationAttribute(root, child);
                    QName childQName = getQName(schemaRegistry, child);
                    handleChildNotFoundCase(child, childQName);
                    // take the parent's edit operation by default, if there is any change, child will process it
                    EditContainmentNode node = createEditNodeAndSetParent(root, schemaRegistry, child, operation);

                    if (modelNodeHelperRegistry.getChildListHelper(modelNodeSchemaPath, childQName) != null) {
                        prepareSubtreeFromChildListContainer(node, child, modelNodeSchemaPath, schemaRegistry, modelNodeHelperRegistry,
                                modelNodeId);
                        root.addChild(node);
                    } else if (modelNodeHelperRegistry.getChildContainerHelper(modelNodeSchemaPath, childQName) != null) {
                        prepareSubtreeFromChildContainer(node, child, modelNodeSchemaPath, schemaRegistry, modelNodeHelperRegistry,
                                modelNodeId);
                        root.addChild(node);
                    }
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException e) {
                    // TODO: FNMS-10123 add all error fields
                    LOGGER.error("Error while constructing edit-tree from edit-config", e);
                    throw new EditConfigException(NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.OPERATION_FAILED,
                            e.getMessage()));
                }
            }
        }
    }

    private boolean getVisibilityValue(String visibilityAttribute) {
        if(visibilityAttribute != null) {
            if(visibilityAttribute.equals("true") || visibilityAttribute.equals("false")) {
                return Boolean.valueOf(visibilityAttribute);
            } else {
                LOGGER.error("Invalid <edit-config> visibility operation : {}", visibilityAttribute);
                NetconfRpcError rpcError = NetconfRpcError.getBadAttributeError(NetconfResources.VISIBILITY, NetconfRpcErrorType.Application,
                        String.format("Bad visibility attribute : %s", visibilityAttribute));
                throw new EditConfigException(rpcError);
            }
        }
        return true;
    }

    private EditContainmentNode createEditNodeAndSetParent(EditContainmentNode root, SchemaRegistry schemaRegistry, Node child, String operation) {
        EditContainmentNode node = new EditContainmentNode(getSchemaPath(schemaRegistry, child), operation, schemaRegistry, child);
        node.setParent(root);
        return node;
    }

    private void handleChildNotFoundCase(Node child, QName childQName) {
        // throw an error if childQname is null
        if (childQName == null) {
            String errorMsg = String.format("%s with namespace %s not found", child.getLocalName(),
                    child.getNamespaceURI());
            LOGGER.error(errorMsg);
            throw new EditConfigException(NetconfRpcErrorUtil
                    .getApplicationError(NetconfRpcErrorTag.INVALID_VALUE, errorMsg));
        }
    }

    private String getOperationAttribute(EditContainmentNode root, Node node){
        String operation = null;
        if(node != null){
            operation = resolveOperationAttribute(node);
        }
        if (operation == null) {
            operation = root.getEditOperation();
        }
        return operation;
    }

    private boolean isDeleteOperation(String operation) {
        return EditConfigOperations.DELETE.equals(operation) || EditConfigOperations.REMOVE.equals(operation);
    }

    public static void addLeafListEditChangeNode(EditContainmentNode root, QName childQName, Node child, SchemaPath modelNodeSchemaPath, SchemaRegistry schemaRegistry) {
        for (EditChangeNode changeNode : root.getChangeNodes()) {
            if (changeNode.getQName().equals(childQName) && changeNode.getValue().equals(child.getTextContent())) {
                EditContainmentNode.throwDuplicateNodesEditConfigException(root.getModelNodeId().xPathString() + "/" + childQName.getLocalName(), childQName);
            }
        }
        addEditChangeNode(root, childQName, child, modelNodeSchemaPath, schemaRegistry);
    }

    public static void addLeafEditChangeNode(EditContainmentNode root, QName childQName, Node child, SchemaPath modelNodeSchemaPath, SchemaRegistry schemaRegistry) {
        for (EditChangeNode changeNode : root.getChangeNodes()) {
            if (changeNode.getQName().equals(childQName)) {
                EditContainmentNode.throwDuplicateNodesEditConfigException(root.getModelNodeId().xPathString() + "/" + childQName.getLocalName(), childQName);
            }
        }
        addEditChangeNode(root, childQName, child, modelNodeSchemaPath, schemaRegistry);
    }

    public static String getContainerName(SchemaPath modelNodeSchemaPath) {
        return modelNodeSchemaPath.getLastComponent().getLocalName();
    }

    private static String resolveOperationAttribute(Node child) throws EditConfigException {
        Element childElement = (Element) child;
        String operation = childElement.getAttributeNS(NetconfResources.NETCONF_RPC_NS_1_0, NetconfResources.OPERATION);
        if (operation == null || operation.isEmpty()) {
            return null;
        } else {
            return operation;
        }
    }

    private static String resolveInsertAttributes(Node child, String localName) {
        Element childElement = (Element) child;
        String attribute = childElement.getAttributeNS(NetconfResources.NETCONF_YANG_1, localName);
        if (attribute == null || attribute.isEmpty()) {
            return null;
        } else {
            return attribute;
        }
    }

    private static String resolveVisibilityAttribute(Node child) {
        Element childElement = (Element) child;
        String attribute = childElement.getAttributeNS(NetconfResources.NC_STACK_EXTENSIONS_NS, NetconfResources.VISIBILITY);
        if (attribute == null || attribute.isEmpty()) {
            return null;
        } else {
            return attribute;
        }
    }

    public static QName getQName(SchemaRegistry schemaRegistry, Node node) {
        String localName = getLocalName(node);
        String namespace = node.getNamespaceURI();
        return schemaRegistry.lookupQName(namespace, localName);
    }

    public static SchemaPath getSchemaPath(SchemaRegistry schemaRegistry, Node node) {
        List<QName> qNames = new ArrayList<>();
        populateQNamesFromNodeToRoot(schemaRegistry, node, qNames);
        reverse(qNames);
        return SchemaPath.create(qNames, true);
    }

    private static void populateQNamesFromNodeToRoot(SchemaRegistry schemaRegistry, Node node, List<QName> qNames) {
        if(node != null) {
            String localName = getLocalName(node);
            String namespace = node.getNamespaceURI();
            QName qName = schemaRegistry.lookupQName(namespace, localName);
            if(qName == null) {
                return;
            }
            qNames.add(qName);
            populateQNamesFromNodeToRoot(schemaRegistry, node.getParentNode(), qNames);
        }
    }

    private static EditChangeNode addEditChangeNode(EditContainmentNode root, QName childQName, Node child, SchemaPath parentSchemaPath, SchemaRegistry schemaRegistry) throws DOMException, EditConfigException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("CHANGE NODE == {}, Condition == {}", child.getNodeName(), child.getNodeValue());
        }

        // add leaf-list attributes
        String operation = getEditOperation((Element) child);
        String insertName = resolveInsertAttributes(child, NetconfResources.INSERT);
        String insertValue = resolveInsertAttributes(child, NetconfResources.VALUE);
        DataSchemaNode dataSchemaNode = schemaRegistry.getDataSchemaNode(schemaRegistry.getDescendantSchemaPath(parentSchemaPath, childQName));
        InsertOperation insertOperation = getInsertOperation(insertName, insertValue, dataSchemaNode, root.getEditOperation());
        ConfigLeafAttribute configLeafAttribute;
        try {
            configLeafAttribute = ConfigAttributeFactory.getConfigAttribute(schemaRegistry, parentSchemaPath,
                    childQName, child);
        } catch (InvalidIdentityRefException e) {
            throw new EditConfigException(e.getRpcError());
        }

        EditChangeNode editChangeNode = new EditChangeNode(childQName, configLeafAttribute);
        editChangeNode.setOperation(operation);
        editChangeNode.setInsertOperation(insertOperation);
        root.addChangeNode(editChangeNode);
        if (!(configLeafAttribute instanceof GenericConfigAttribute)) {
            editChangeNode.setIdentityRefNode(true); //FIXME : FNMS-10114 Remove this boolean
        }
        return editChangeNode;
    }

    public static EditMatchNode addEditMatchNode(SchemaRegistry schemaRegistry, SchemaPath modelNodeSchemaPath, EditContainmentNode root, QName childQName, Node child) throws DOMException, EditConfigException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("MATCH NODE == {}, Condition == {}", child.getNodeName(), child.getNodeValue());
        }
        ConfigLeafAttribute configLeafAttribute;
        try {
            configLeafAttribute = ConfigAttributeFactory.getConfigAttribute(schemaRegistry, modelNodeSchemaPath,
                    childQName, child);
        } catch (InvalidIdentityRefException e) {
            throw new EditConfigException(e.getRpcError());
        }
        EditMatchNode editMatchNode = new EditMatchNode(childQName, configLeafAttribute);
        if (!(configLeafAttribute instanceof GenericConfigAttribute)) {
            editMatchNode.setIdentityRefNode(true); //FIXME : FNMS-10114 Remove this boolean
        }
        root.addMatchNode(editMatchNode);
        return editMatchNode;
    }

    public void prepareSubtreeFromChildContainer(EditContainmentNode node, Node xmlNode, SchemaPath modelNodeURI,
                                                 SchemaRegistry schemaRegistry, ModelNodeHelperRegistry modelNodeHelperRegistry, ModelNodeId modelNodeId)
            throws IllegalAccessException, InvocationTargetException, EditConfigException, InstantiationException {
        ChildContainerHelper childContainerHelper = modelNodeHelperRegistry.getChildContainerHelper(modelNodeURI,
                getQName(schemaRegistry, xmlNode));
        ModelNodeId id = new ModelNodeId(modelNodeId);
        id.addRdn(new ModelNodeRdn(ModelNodeRdn.CONTAINER, xmlNode.getNamespaceURI(), node.getName()));
        prepareEditSubTree(node, (Element) xmlNode, childContainerHelper.getChildModelNodeSchemaPath(), schemaRegistry, modelNodeHelperRegistry,
                id);
    }

    public void prepareSubtreeFromChildListContainer(EditContainmentNode node, Node xmlNode, SchemaPath modelNodeURI,
                                                     SchemaRegistry schemaRegistry, ModelNodeHelperRegistry modelNodeHelperRegistry, ModelNodeId modelNodeId)
            throws IllegalAccessException, InvocationTargetException, EditConfigException {
        ChildListHelper childListHelper = modelNodeHelperRegistry.getChildListHelper(modelNodeURI, getQName(schemaRegistry, xmlNode));

        boolean found = false;
        SchemaPath uri = childListHelper.getChildModelNodeSchemaPath();
        String containerName = getContainerName(uri);
        if (getLocalName(xmlNode).equals(containerName)) {
            ModelNodeId id = new ModelNodeId(modelNodeId);
            id.addRdn(new ModelNodeRdn(ModelNodeRdn.CONTAINER, xmlNode.getNamespaceURI(), containerName));
            Set<QName> keyQnames = modelNodeHelperRegistry.getNaturalKeyHelpers(uri).keySet();
            if (!keyQnames.isEmpty()) {
                NodeList nodeList = xmlNode.getChildNodes();
                for (int i = 0; i < nodeList.getLength(); i++) {
                    Node child = nodeList.item(i);
                    if (child.getNodeType() == Node.ELEMENT_NODE) {
                        QName qname = getQName(schemaRegistry, child);
                        if (keyQnames.contains(qname)) {
                            id.addRdn(new ModelNodeRdn(qname.getLocalName(), child.getNamespaceURI(), child.getTextContent()));
                        }
                    }
                }
            }

            prepareEditSubTree(node, (Element) xmlNode, uri, schemaRegistry, modelNodeHelperRegistry, id);
            found = true;
        }
        if (!found) {
            throw new EditConfigException(NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.INVALID_VALUE,
                    "Invalid config node :" + xmlNode.getNodeName()));
        }

    }

    public static String getEditOperation(Element editConfigXml) throws EditConfigException {
        String operation = editConfigXml.getAttributeNS(NetconfResources.NETCONF_RPC_NS_1_0, NetconfResources.EDIT_CONFIG_OPERATION);
        if (operation != null && !EMPTY_STR.equals(operation)) {
            if (EditConfigOperations.MERGE.equals(operation)) {
                return EditConfigOperations.MERGE;
            }
            if (EditConfigOperations.DELETE.equals(operation)) {
                return EditConfigOperations.DELETE;
            }
            if (EditConfigOperations.CREATE.equals(operation)) {
                return EditConfigOperations.CREATE;
            }
            if (EditConfigOperations.REMOVE.equals(operation)) {
                return EditConfigOperations.REMOVE;
            }
            if (EditConfigOperations.REPLACE.equals(operation)) {
                return EditConfigOperations.REPLACE;
            }
            LOGGER.error("Invalid <edit-config> operation : {}", operation);
            throw new EditConfigException(NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.OPERATION_FAILED,
                    "Invalid <edit-config> operation : " + operation));
        }
        return null;

    }

    public static InsertOperation getInsertOperation(String insert, String value,DataSchemaNode node, String parentOperation) throws EditConfigException {
        if((node instanceof ListSchemaNode && ((ListSchemaNode) node).isUserOrdered()) || (node instanceof LeafListSchemaNode && ((LeafListSchemaNode) node).isUserOrdered())) {
            if (insert == null) {
                if (EditConfigOperations.REPLACE.equals(parentOperation)) {
                    return InsertOperation.LAST_OP;
                }
                return null;
            } else if (!EMPTY_STR.equals(insert) && (insert.equals(InsertOperation.FIRST) || insert.equals(InsertOperation.LAST) || insert.equals(InsertOperation.BEFORE) || insert.equals(InsertOperation.AFTER))) {
                return InsertOperation.get(insert, value);
            } else {
                LOGGER.error("Invalid <edit-config> insert operation : {}", insert);
                NetconfRpcError rpcError = NetconfRpcError.getBadAttributeError(NetconfResources.INSERT, NetconfRpcErrorType.Application,
                        String.format("Bad insert attribute : %s", insert));
                rpcError.setErrorAppTag("missing-instance");
                throw new EditConfigException(rpcError);
            }
        }
        return null;
    }

}
