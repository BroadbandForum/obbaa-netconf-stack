package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.rpc;

import java.util.List;
import java.util.Optional;

import org.broadband_forum.obbaa.netconf.api.NetconfMessage;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcResponse;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.messages.RpcName;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import org.broadband_forum.obbaa.netconf.server.rpc.RequestType;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcPayloadConstraintParser;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcProcessException;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcRequestHandler;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcValidationException;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.w3c.dom.Element;

public abstract class AbstractRpcRequestHandler implements RpcRequestHandler {
	
	protected final RpcName m_rpcQName;
	
	protected RpcDefinition m_rpcDefinition;
	
	static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(AbstractRpcRequestHandler.class, LogAppNames.NETCONF_STACK);

    public AbstractRpcRequestHandler(RpcName rpcQName) {
        m_rpcQName = rpcQName;
    }
	
	@Override
	public RpcName getRpcQName() {
		return m_rpcQName;
	}
	
	@Override
	public RpcDefinition getRpcDefinition() {
		return m_rpcDefinition;
	}

	@Override
	public void setRpcDefinition(final RpcDefinition rpcDefinition) {
		m_rpcDefinition = rpcDefinition;
		
	}

	@Override
	public abstract List<Notification> processRequest(final NetconfClientInfo clientInfo, final NetconfRpcRequest request, final NetconfRpcResponse response) throws RpcProcessException;
	
	@Override
	public void validate(final RpcPayloadConstraintParser rpcConstraintParser, final NetconfMessage rpc) throws RpcValidationException {
		//Concerete rpc handlers can validate some their logics
		try {
		    if (rpc.getType().isRequest()){
	            rpcConstraintParser.validate((NetconfRpcRequest)rpc, RequestType.RPC);
		    }else if (rpc.getType().isResponse()){
		        rpcConstraintParser.validate((NetconfRpcResponse)rpc, RequestType.RPC);
		    }
		} catch (ValidationException e) {
			throw new RpcValidationException("RPC Validation failed: "+e.getRpcError().getErrorMessage(), e, e.getRpcError(), false, true);
		}
		LOGGER.debug("{} is validated", rpc.toString());
	}
	
	protected Element getNode(NetconfRpcRequest request, String childLocalName, String namespace) {
	    Element childElement = DocumentUtils.getDescendant(request.getRpcInput(), childLocalName, namespace);
	    if(childElement == null){
	        String childValue = getDefaultValueIfExists(childLocalName, namespace);
	        if(childValue != null) {
	            childElement = DocumentUtils.createDocument().createElementNS(namespace, childLocalName);
	            childElement.setTextContent(childValue);
	        }
	    }
	    return childElement;
	}

	private String getDefaultValueIfExists(String childLocalName, String namespace){
	    RpcDefinition rpcDefinition = getRpcDefinition();
	    if(rpcDefinition != null) {
	        ContainerSchemaNode inputNode = rpcDefinition.getInput();
	        Optional<Revision> revision = inputNode.getQName().getRevision();
	        QName childQName = getQName(childLocalName, namespace, revision);
	        Optional<DataSchemaNode> childNode = inputNode.findDataChildByName(childQName);
	        if(childNode != null && childNode.isPresent() && childNode.get() instanceof LeafSchemaNode) {
	            LeafSchemaNode leafNode = (LeafSchemaNode) childNode.get();
	            Optional<? extends Object> defaultVal =leafNode.getType().getDefaultValue();
	            if(defaultVal.isPresent()){
	                return defaultVal.get().toString();
	            }
	        }
	    }
	    return null;
	}

	private QName getQName(String name, String namespace, Optional<Revision> revision) {
	    QName qname;
	    if (revision != null && revision.isPresent()){
	        qname = QName.create(namespace, revision.get().toString(), name);
	    } else {
	        qname = QName.create(namespace, name);             
	    }
	    return qname;
	}
}
