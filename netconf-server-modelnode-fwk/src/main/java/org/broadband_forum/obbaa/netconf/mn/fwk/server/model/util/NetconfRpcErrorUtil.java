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

import org.broadband_forum.obbaa.netconf.api.messages.*;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * Utility class for generating an NetconfRpcError
 *
 */

public class NetconfRpcErrorUtil {
    
    private static final NetconfRpcErrorSeverity SEVERITY_ERROR = NetconfRpcErrorSeverity.Error;
    private static final NetconfRpcErrorType TYPE_APPLICATION = NetconfRpcErrorType.Application;
    
    public static NetconfRpcError getNetconfRpcError(NetconfRpcErrorTag tag,NetconfRpcErrorType type,NetconfRpcErrorSeverity severity, String message){
        NetconfRpcError error = new NetconfRpcError(tag,type,severity,message);
        return error;
    }
    
    public static NetconfRpcError getNetconfRpcError(NetconfRpcErrorTag tag,NetconfRpcErrorType type,NetconfRpcErrorSeverity severity, String message, Element errorPath, Element errorInfo){
        NetconfRpcError error = new NetconfRpcError(tag,type,severity,message);
        error.setErrorInfo(errorInfo);
        error.setErrorPathElement(errorPath);
        return error;
    }
    
    public static NetconfRpcError getApplicationError(NetconfRpcErrorTag tag, String message){
        NetconfRpcError error = new NetconfRpcError(tag,TYPE_APPLICATION,SEVERITY_ERROR, message);
        return error;
    }
    
    public static NetconfRpcError getNetconfRpcErrorForModelNodeId(ModelNodeId nodeId, NetconfRpcErrorTag tag, String message, SchemaRegistry schemaRegistry){
        NetconfRpcError error = getApplicationError(tag,message);
        if (nodeId!=null){
            error.setErrorPath(nodeId.xPathString(schemaRegistry), nodeId.xPathStringNsByPrefix(schemaRegistry));
        }
        return error;
    }
    
    public static NetconfRpcError getNetconfRpcErrorForModelNode(ModelNode node, NetconfRpcErrorTag tag,String message) {
        NetconfRpcError error = getApplicationError(tag,message);
        if (node.getModelNodeId() != null) {
        	ModelNodeId nodeId = node.getModelNodeId();
            error.setErrorPath(nodeId.xPathString(node.getSchemaRegistry()), nodeId.xPathStringNsByPrefix(node.getSchemaRegistry()));
        }   
        return error;
    }

    public static void convertElementToNetconfRpcError(Element element, ActionResponse response) {
        NetconfRpcErrorTag errorTag = null;
        NetconfRpcErrorType errorType = null;
        NetconfRpcErrorSeverity errorSeverity = null;
        String errorMessage = null;
        Element errorInfo = null, errorPath = null;
        NodeList errorChildList = element.getChildNodes();
        for (int i = 0; i < errorChildList.getLength(); i++) {
            Node errorChild = errorChildList.item(i);
            if (errorChild.getNodeType() == Node.ELEMENT_NODE) {
                Element errorChildElement = (Element) errorChild;
                if (errorChildElement.getLocalName() != null
                        && errorChildElement.getNamespaceURI() != null) {
                    if (errorChildElement.getLocalName().equals(NetconfResources.RPC_ERROR_TAG)) {
                        errorTag = NetconfRpcErrorTag.getType(errorChildElement.getTextContent());
                    } else if (errorChildElement.getLocalName()
                            .equals(NetconfResources.RPC_ERROR_INFO)) {
                        errorInfo = errorChildElement;
                    } else if (errorChildElement.getLocalName()
                            .equals(NetconfResources.RPC_ERROR_MESSAGE)) {
                        errorMessage = errorChildElement.getTextContent();
                    } else if (errorChildElement.getLocalName()
                            .equals(NetconfResources.RPC_ERROR_SEVERITY)) {
                        errorSeverity = NetconfRpcErrorSeverity
                                .getType(errorChildElement.getTextContent());
                    } else if (errorChildElement.getLocalName()
                            .equals(NetconfResources.RPC_ERROR_PATH)) {
                        errorPath = errorChildElement;
                        String actionErrorPath = "/nc:rpc/yangns:action";
                        String errorPathString = errorChildElement.getTextContent();
                        if (errorPathString.contains(actionErrorPath)) {
                            errorPathString = errorPathString.replace(actionErrorPath, "");
                            errorPath.setTextContent(errorPathString);
                            errorPath.removeAttribute(NetconfResources.XMLNS + ":"
                                    + NetconfResources.NETCONF_YANG_NS_PREFIX);
                            errorPath.removeAttribute(NetconfResources.XMLNS + ":nc");
                        }

                    } else if (errorChildElement.getLocalName()
                            .equals(NetconfResources.RPC_ERROR_TYPE)) {
                        errorType = NetconfRpcErrorType.getType(errorChildElement.getTextContent());
                    }
                }
            }
        }
        NetconfRpcError error = NetconfRpcErrorUtil.getNetconfRpcError(errorTag, errorType,
                errorSeverity, errorMessage, errorPath, errorInfo);
        response.addError(error);
    }
}