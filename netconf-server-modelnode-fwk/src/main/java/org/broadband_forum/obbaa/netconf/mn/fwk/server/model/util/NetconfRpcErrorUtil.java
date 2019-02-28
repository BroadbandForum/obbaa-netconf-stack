package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util;

import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorSeverity;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorType;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;

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
}