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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util;

import javax.xml.parsers.ParserConfigurationException;

import org.broadband_forum.obbaa.netconf.api.messages.EditConfigDefaultOperations;
import org.broadband_forum.obbaa.netconf.api.messages.InsertOperation;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditChangeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditConfigException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditMatchNode;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.google.common.annotations.VisibleForTesting;

public class EditTreeTransformer {

	private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(EditTreeTransformer.class, LogAppNames.NETCONF_STACK);
	private static final String COLON = ":";
	
	@VisibleForTesting
	static boolean m_includePrefix = true;

    public static Element toXml(SchemaRegistry schemaRegistry, EditContainmentNode editNode, boolean includeOperationAttr,
            String defaultNamespace) throws EditConfigException {
        try {
            Document doc = DocumentUtils.getNewDocument();
            return toXmlInternal(schemaRegistry, editNode, doc, includeOperationAttr, EditConfigDefaultOperations.NONE, defaultNamespace);
            
        } catch (ParserConfigurationException e) {
            LOGGER.error("Could not transform the edit tree ", e);
            throw new EditConfigException(NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.INVALID_VALUE,
					"could not parse"));
            
        }
    }

    private static Element toXmlInternal(SchemaRegistry schemaRegistry, EditContainmentNode editNode, Document doc,
            boolean includeOperationAttr, String defaultEditOperation, String defaultNamespace) throws EditConfigException {
        String namespace = editNode.getNamespace();
        Element node = doc.createElementNS(namespace, resolveLocalName(schemaRegistry, namespace, editNode.getName()));
        if(includeOperationAttr) {
            setOperationAttribute(editNode, node, defaultEditOperation);
        }
        includeInsertOperation(editNode, node);
        addNodeData(schemaRegistry, editNode, doc, node);
        addChildNodeChanges(schemaRegistry, editNode, doc, node, includeOperationAttr, defaultNamespace);
        return node;
    }

    private static void addChildNodeChanges(SchemaRegistry schemaRegistry, EditContainmentNode editNode, Document doc, Element node,
            boolean includeOperationAttr, String defaultNamespace) throws EditConfigException {
        for(EditContainmentNode child :  editNode.getChildren()){
            node.appendChild(toXmlInternal(schemaRegistry, child, doc, includeOperationAttr, editNode.getEditOperation(), defaultNamespace));
        }
    }

    private static void addNodeData(SchemaRegistry schemaRegistry, EditContainmentNode editNode, Document doc, Element node) throws EditConfigException {
        for(EditMatchNode editMatchNode : editNode.getMatchNodes()){
        	String namespace = editMatchNode.getNamespace();
            if (editMatchNode.isIdentityRefNode()){
                Node importNode = doc.importNode(editMatchNode.getConfigLeafAttribute().getDOMValue(), true);
                node.appendChild(importNode);
            }else {
                Element changeNode = doc.createElementNS(namespace, resolveLocalName(schemaRegistry, namespace, editMatchNode.getName()));
                changeNode.setTextContent(editMatchNode.getValue());
                node.appendChild(changeNode);
            }
        }
        
        for(EditChangeNode editChangeNode : editNode.getChangeNodes()){
        	String namespace = editChangeNode.getNamespace();
            Element changeNode;
            if (editChangeNode.isIdentityRefNode()){
                changeNode = (Element) doc.importNode(editChangeNode.getConfigLeafAttribute().getDOMValue(), true);
                node.appendChild(changeNode);
            } else {
                changeNode = doc.createElementNS(namespace, resolveLocalName(schemaRegistry, namespace, editChangeNode.getName()));
            	changeNode.setTextContent(editChangeNode.getValue());
                includeInsertOperation(editChangeNode, changeNode);
                node.appendChild(changeNode);
            }
            if(editChangeNode.getOperation()!=null &&
                    !editChangeNode.getOperation().equalsIgnoreCase(EditConfigDefaultOperations.MERGE)){
                changeNode.setAttributeNS(NetconfResources.NETCONF_RPC_NS_1_0, NetconfResources.NETCONF_RPC_NS_PREFIX+NetconfResources.EDIT_CONFIG_OPERATION,
                        editChangeNode.getOperation());
            }
        }
    }

    private static void setOperationAttribute(EditContainmentNode editNode, Element node, String defaultEditOperation) {
        String editOperation = editNode.getEditOperation();
		if(editOperation!= null && !editOperation.equals(defaultEditOperation)){
            node.setAttributeNS(NetconfResources.NETCONF_RPC_NS_1_0, NetconfResources.NETCONF_RPC_NS_PREFIX+NetconfResources.EDIT_CONFIG_OPERATION, editOperation);
        }
    }

    private static void includeInsertOperation(EditContainmentNode editNode, Element node){
        InsertOperation insertOperation = editNode.getInsertOperation();
        if(insertOperation != null){
            node.setAttributeNS(NetconfResources.NETCONF_YANG_1, NetconfResources.NETCONF_YANG_PREFIX+NetconfResources.INSERT, insertOperation.getName());
            //Check for operation value, in case of null, simply dont add the attribute
            if(insertOperation.getValue() != null) {
                node.setAttributeNS(NetconfResources.NETCONF_YANG_1, NetconfResources.NETCONF_YANG_PREFIX+NetconfResources.KEY, insertOperation.getValue());
            }
        }
    }

    private static void includeInsertOperation(EditChangeNode editChangeNode, Element changeNode){
        InsertOperation insertOperation = editChangeNode.getInsertOperation();
        if(insertOperation != null){
            changeNode.setAttributeNS(NetconfResources.NETCONF_YANG_1, NetconfResources.NETCONF_YANG_PREFIX+NetconfResources.INSERT, insertOperation.getName());
            //Check for operation value, in case of null, simply dont add the attribute
            if(insertOperation.getValue() != null) {
                changeNode.setAttributeNS(NetconfResources.NETCONF_YANG_1, NetconfResources.NETCONF_YANG_PREFIX+NetconfResources.VALUE, insertOperation.getValue());
            }
        }
    }
    
    public static String resolveLocalName(SchemaRegistry schemaRegistry, String namespace, String name) {
    	String localName = null;
		String prefix = schemaRegistry.getPrefix(namespace);
		if (m_includePrefix && prefix != null && !prefix.isEmpty()) {
			localName = prefix + COLON + name;
		} else {
			localName = name;
		}
//        String moduleName = schemaRegistry.getModuleNameByNamespace(namespace);
//        if (m_includePrefix && moduleName != null && !moduleName.isEmpty()) {
//            localName = moduleName + COLON + name;
//        } else {
//            localName = name;
//        }
		return localName;
    }
    
    public static String getLocalName(Node node) {
    	if (node.getLocalName() == null) {
    		return node.getNodeName();
    	}
		return node.getLocalName();
	}

}
