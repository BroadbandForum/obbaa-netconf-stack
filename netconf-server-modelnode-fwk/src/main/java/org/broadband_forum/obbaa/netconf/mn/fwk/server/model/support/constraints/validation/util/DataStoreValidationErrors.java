package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util;

import java.util.HashMap;
import java.util.Map;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;

import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.WhenValidationException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.NetconfRpcErrorUtil;

/**
 * A utility class helps in generation of various ValidationException with NetconfRpcError during validation
 */
public class DataStoreValidationErrors {

    private static final String NETCONF_RPC_ERROR = "NETCONF_RPC_ERROR";
    
    private static NetconfRpcError getApplicationError(NetconfRpcErrorTag tag) {
        RequestScope scope = RequestScope.getCurrentScope();
        @SuppressWarnings("unchecked")
        Map<NetconfRpcErrorTag, NetconfRpcError> error = (Map<NetconfRpcErrorTag, NetconfRpcError>) scope.getFromCache(NETCONF_RPC_ERROR);
        if (error == null) {
            error = new HashMap<NetconfRpcErrorTag, NetconfRpcError>();
            scope.putInCache(NETCONF_RPC_ERROR, error);
        }
        NetconfRpcError rpcError = error.get(tag);
        if (rpcError == null) {
            rpcError = NetconfRpcErrorUtil.getApplicationError(tag, null);
            error.putIfAbsent(tag, rpcError);
            rpcError = error.get(tag);
        }
        
        return rpcError;
    }
    public static NetconfRpcError getDataMissingRpcError(String errorMessage, String errorPath, Map<String, String> prefixContext) {
        NetconfRpcError rpcError = NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.DATA_MISSING, errorMessage);
        rpcError.setErrorAppTag("instance-required");
        rpcError.setErrorPath(errorPath, prefixContext);
        return rpcError;
    }

    public static ValidationException getViolateMaxElementException(String nodeType, int maxElements) {
        NetconfRpcError rpcError = getApplicationError(NetconfRpcErrorTag.OPERATION_FAILED);
        rpcError.setErrorMessage(String.format("Maximum elements allowed for %s is %s.", nodeType, maxElements));
        rpcError.setErrorAppTag("too-many-elements");
        return new ValidationException(rpcError);
    }

    public static ValidationException getMissingDataException(String errorMessage, String errorPath, Map<String, String> prefixContext) {
    	NetconfRpcError rpcError = getDataMissingRpcError(errorMessage, errorPath, prefixContext);
        return new ValidationException(rpcError);
    }

    public static void throwDataMissingException(SchemaRegistry schemaRegistry, ModelNode modelNode, QName qname) {
        ModelNodeId modelNodeId = new ModelNodeId(modelNode.getModelNodeId());
        modelNodeId.addRdn(ModelNodeRdn.CONTAINER, qname.getNamespace().toString(), qname.getLocalName());
        throw getMissingDataException(DataStoreValidationUtil.MISSING_MANDATORY_NODE, modelNodeId.xPathString(schemaRegistry), modelNodeId.xPathStringNsByPrefix(schemaRegistry));
    }

    public static ValidationException getViolateMinElementException(String nodeType, int minElements) {
        NetconfRpcError rpcError = getApplicationError(NetconfRpcErrorTag.OPERATION_FAILED);
        rpcError.setErrorMessage(String.format("Minimum elements required for %s is %s.", nodeType ,minElements));
        rpcError.setErrorAppTag("too-few-elements");
        return new ValidationException(rpcError);
    }

    public static ValidationException getViolateWhenConditionExceptionThrownUnknownElement(String whenXPath) {
        NetconfRpcError rpcError = getApplicationError(NetconfRpcErrorTag.UNKNOWN_ELEMENT);
        rpcError.setErrorMessage("Violate when constraints: " + whenXPath);
        rpcError.setErrorAppTag("when-violation");
        return new WhenValidationException(rpcError);
    }

    public static ValidationException getViolateMustContrainsException(MustDefinition mustDefinition) {
        NetconfRpcError rpcError = getApplicationError(NetconfRpcErrorTag.OPERATION_FAILED);
        rpcError.setErrorMessage("Violate must constraints: " + mustDefinition.toString());

        // get ErrorAppTag from MustDefinition
        if (mustDefinition.getErrorAppTag().isPresent()) {
            rpcError.setErrorAppTag(mustDefinition.getErrorAppTag().get());
        } else {
            rpcError.setErrorAppTag("must-violation");
        }

        // get ErrorMessage from MustDefinition
        if (mustDefinition.getErrorMessage().isPresent()) {
            rpcError.setErrorMessage(mustDefinition.getErrorMessage().get());
        }
        return new ValidationException(rpcError);
    }

    public static ValidationException getUniqueConstraintException(String message) {
        NetconfRpcError rpcError = getApplicationError(NetconfRpcErrorTag.OPERATION_FAILED);
        rpcError.setErrorMessage(message);
        rpcError.setErrorAppTag("data-not-unique");
        return new ValidationException(rpcError);
    }

    public static String buildRpcErrorPath(ModelNode modelNode, String elementName, String namespace) {
        ModelNodeId id = new ModelNodeId(modelNode.getModelNodeId());
        id.addRdn(new ModelNodeRdn(ModelNodeRdn.CONTAINER, namespace, elementName));
        return id.xPathString();
    }
    public static NetconfRpcError getViolateMinElementRPCError(String nodeType, int minElements) {
        NetconfRpcError rpcError = getApplicationError(NetconfRpcErrorTag.OPERATION_FAILED);
        rpcError.setErrorMessage(String.format("Minimum elements required for %s is %s.", nodeType ,minElements));
        rpcError.setErrorAppTag("too-few-elements");
        return rpcError;
    }

}
