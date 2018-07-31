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

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditChangeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditConfigException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.EditTreeTransformer;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.NetconfRpcErrorUtil;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.broadband_forum.obbaa.netconf.api.messages.EditConfigOperations;
import org.broadband_forum.obbaa.netconf.api.messages.InsertOperation;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorType;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditMatchNode;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.LoggerFactory;

public class EditTreeBuilder {
    private static final String EMPTY_STR = "";
    private static final AdvancedLogger LOGGER = LoggerFactory.getLogger(EditTreeBuilder.class,
            "netconf-server-datastore", "DEBUG", "GLOBAL");

    public void prepareEditSubTree(EditContainmentNode root, Element editConfigXml, SchemaPath modelNodeSchemaPath,
                                   SchemaRegistry schemaRegistry, ModelNodeHelperRegistry modelNodeHelperRegistry,
                                   ModelNodeId modelNodeId)
            throws EditConfigException {

        if (editConfigXml != null) {
            String nodeName = EditTreeTransformer.getLocalName(editConfigXml);
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
            root.setInsertOperation(getInsertOperation(insertAttribute, keyAttribute));

            NodeList nodeList = editConfigXml.getChildNodes();
            // processChildren
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node child = nodeList.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    try {
                        String operation = resolveOperationAttribute(child);
                        if (operation == null) {
                            operation = root.getEditOperation();
                        }
                        QName childQName = getQName(schemaRegistry, child);
                        // throw an error if childQname is null
                        if (childQName == null) {
                            String errorMsg = String.format("%s with namespace %s not found", child.getLocalName(),
                                    child.getNamespaceURI());
                            LOGGER.error(errorMsg);
                            throw new EditConfigException(NetconfRpcErrorUtil
                                    .getApplicationError(NetconfRpcErrorTag.INVALID_VALUE, errorMsg));
                        }
                        // take the parent's edit operation by default, if there is any change, child will process it
                        EditContainmentNode node = new EditContainmentNode(childQName, operation);

                        if (modelNodeHelperRegistry.getChildListHelper(modelNodeSchemaPath, childQName) != null) {
                            prepareSubtreeFromChildListContainer(node, child, modelNodeSchemaPath, schemaRegistry,
                                    modelNodeHelperRegistry,
                                    modelNodeId);
                            root.addChild(node);
                        } else if (modelNodeHelperRegistry.getChildContainerHelper(modelNodeSchemaPath, childQName)
                                != null) {
                            prepareSubtreeFromChildContainer(node, child, modelNodeSchemaPath, schemaRegistry,
                                    modelNodeHelperRegistry,
                                    modelNodeId);
                            root.addChild(node);
                        } else if (modelNodeHelperRegistry.getNaturalKeyHelper(modelNodeSchemaPath, childQName) !=
                                null) {
                            // it is a match node
                            EditTreeBuilder.addEditMatchNode(schemaRegistry, modelNodeSchemaPath, root,
                                    childQName, child);
                        } else if (modelNodeHelperRegistry.getConfigAttributeHelper(modelNodeSchemaPath, childQName)
                                != null) {
                            // it is a change node
                            EditTreeBuilder.addEditChangeNode(root, childQName, child, modelNodeSchemaPath,
                                    schemaRegistry);
                        } else if (modelNodeHelperRegistry.getConfigLeafListHelper(modelNodeSchemaPath, childQName)
                                != null) {
                            // it is a change node
                            EditTreeBuilder.addEditChangeNode(root, childQName, child, modelNodeSchemaPath,
                                    schemaRegistry);
                        } else {
                            // it may be a state attribute, which has no place in edit request
                            // TODO: FNMS-10123 add all error fields
                            LOGGER.error("Invalid item in edit request :{}; child of - {}", child.getNodeName(),
                                    modelNodeSchemaPath);
                            throw new EditConfigException(NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag
                                            .INVALID_VALUE,
                                    "Invalid item in edit request: "
                                            + child.getNodeName()).setErrorPath(modelNodeId.xPathString
                                    (schemaRegistry), modelNodeId.xPathStringNsByPrefix(schemaRegistry)));
                        }

                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException |
                            InstantiationException e) {
                        // TODO: FNMS-10123 add all error fields
                        throw new EditConfigException(NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag
                                        .OPERATION_FAILED,
                                e.getMessage()));
                    }

                } else {
                    LOGGER.trace("found non-element child : {}", child.getNodeName());
                }
            }
        }
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

    public static QName getQName(SchemaRegistry schemaRegistry, Node node) {
        String localName = EditTreeTransformer.getLocalName(node);
        String namespace = node.getNamespaceURI();
        return schemaRegistry.lookupQName(namespace, localName);

    }

    public static EditChangeNode addEditChangeNode(EditContainmentNode root, QName childQName, Node child, SchemaPath
            parentSchemaPath, SchemaRegistry schemaRegistry) throws DOMException, EditConfigException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("CHANGE NODE == {}, Condition == {}", child.getNodeName(), child.getNodeValue());
        }

        // add leaf-list attributes
        String operation = getEditOperation((Element) child);
        String insertName = resolveInsertAttributes(child, NetconfResources.INSERT);
        String insertValue = resolveInsertAttributes(child, NetconfResources.VALUE);
        InsertOperation insertOperation = getInsertOperation(insertName, insertValue);
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

    public static EditMatchNode addEditMatchNode(SchemaRegistry schemaRegistry, SchemaPath modelNodeSchemaPath,
                                                 EditContainmentNode root, QName childQName, Node child) throws
            DOMException, EditConfigException {
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

        root.addMatchNode(editMatchNode);
        return editMatchNode;
    }

    public void prepareSubtreeFromChildContainer(EditContainmentNode node, Node xmlNode, SchemaPath modelNodeURI,
                                                 SchemaRegistry schemaRegistry, ModelNodeHelperRegistry
                                                         modelNodeHelperRegistry, ModelNodeId modelNodeId)
            throws IllegalAccessException, InvocationTargetException, EditConfigException, InstantiationException {
        ChildContainerHelper childContainerHelper = modelNodeHelperRegistry.getChildContainerHelper(modelNodeURI,
                getQName(schemaRegistry, xmlNode));
        ModelNodeId id = new ModelNodeId(modelNodeId);
        id.addRdn(new ModelNodeRdn(ModelNodeRdn.CONTAINER, xmlNode.getNamespaceURI(), node.getName()));
        prepareEditSubTree(node, (Element) xmlNode, childContainerHelper.getChildModelNodeSchemaPath(),
                schemaRegistry, modelNodeHelperRegistry,
                id);
    }

    public void prepareSubtreeFromChildListContainer(EditContainmentNode node, Node xmlNode, SchemaPath modelNodeURI,
                                                     SchemaRegistry schemaRegistry, ModelNodeHelperRegistry
                                                             modelNodeHelperRegistry, ModelNodeId modelNodeId)
            throws IllegalAccessException, InvocationTargetException, EditConfigException {
        ChildListHelper childListHelper = modelNodeHelperRegistry.getChildListHelper(modelNodeURI, getQName
                (schemaRegistry, xmlNode));

        boolean found = false;
        SchemaPath uri = childListHelper.getChildModelNodeSchemaPath();
        String containerName = getContainerName(uri);
        if (EditTreeTransformer.getLocalName(xmlNode).equals(containerName)) {
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
                            id.addRdn(new ModelNodeRdn(qname.getLocalName(), child.getNamespaceURI(), child
                                    .getTextContent()));
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
        String operation = editConfigXml.getAttributeNS(NetconfResources.NETCONF_RPC_NS_1_0, NetconfResources
                .EDIT_CONFIG_OPERATION);
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

    public static InsertOperation getInsertOperation(String insert, String value) throws EditConfigException {
        if (insert == null) {
            return null;
        } else if (!EMPTY_STR.equals(insert) && (insert.equals(InsertOperation.FIRST) || insert.equals
                (InsertOperation.LAST) || insert.equals(InsertOperation.BEFORE) || insert.equals(InsertOperation
                .AFTER))) {
            return new InsertOperation(insert, value);
        } else {
            LOGGER.error("Invalid <edit-config> insert operation : {}", insert);
            NetconfRpcError rpcError = NetconfRpcError.getBadAttributeError(NetconfResources.INSERT, NetconfRpcErrorType.Application,
                    String.format("Bad insert attribute : %s", insert));
            rpcError.setErrorAppTag("missing-instance");
            throw new EditConfigException(rpcError);
        }
    }

}
