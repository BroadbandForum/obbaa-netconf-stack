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

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;

import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorSeverity;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorType;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;

/**
 * Utility class for generating an NetconfRpcError
 */

public class NetconfRpcErrorUtil {

    private static final NetconfRpcErrorSeverity SEVERITY_ERROR = NetconfRpcErrorSeverity.Error;
    private static final NetconfRpcErrorType TYPE_APPLICATION = NetconfRpcErrorType.Application;

    public static NetconfRpcError getNetconfRpcError(NetconfRpcErrorTag tag, NetconfRpcErrorType type,
                                                     NetconfRpcErrorSeverity severity, String message) {
        NetconfRpcError error = new NetconfRpcError(tag, type, severity, message);
        return error;
    }

    public static NetconfRpcError getApplicationError(NetconfRpcErrorTag tag, String message) {
        NetconfRpcError error = new NetconfRpcError(tag, TYPE_APPLICATION, SEVERITY_ERROR, message);
        return error;
    }

    public static NetconfRpcError getNetconfRpcErrorForModelNodeId(ModelNodeId nodeId, NetconfRpcErrorTag tag, String
            message, SchemaRegistry schemaRegistry) {
        NetconfRpcError error = getApplicationError(tag, message);
        if (nodeId != null) {
            error.setErrorPath(nodeId.xPathString(schemaRegistry), nodeId.xPathStringNsByPrefix(schemaRegistry));
        }
        return error;
    }

    public static NetconfRpcError getNetconfRpcErrorForModelNode(ModelNode node, NetconfRpcErrorTag tag, String
            message) {
        NetconfRpcError error = getApplicationError(tag, message);
        if (node.getModelNodeId() != null) {
            ModelNodeId nodeId = node.getModelNodeId();
            error.setErrorPath(nodeId.xPathString(node.getSchemaRegistry()), nodeId.xPathStringNsByPrefix(node
                    .getSchemaRegistry()));
        }
        return error;
    }
}