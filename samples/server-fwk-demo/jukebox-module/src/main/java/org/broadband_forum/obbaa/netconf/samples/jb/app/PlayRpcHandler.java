package org.broadband_forum.obbaa.netconf.samples.jb.app;

import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcResponse;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.messages.RpcName;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.rpc.AbstractRpcRequestHandler;
import org.broadband_forum.obbaa.netconf.samples.jb.api.JBConstants;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcProcessException;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class PlayRpcHandler extends AbstractRpcRequestHandler {
    private static final AdvancedLogger LOGGER = LoggerFactory.getLogger(PlayRpcHandler.class, "jukebox", "CUSTOMER", "GLOBAL");

    public PlayRpcHandler() {
        super(new RpcName(JBConstants.JB_NS, "play"));
    }

    @Override
    public List<Notification> processRequest(NetconfClientInfo clientInfo, NetconfRpcRequest request, NetconfRpcResponse response) throws RpcProcessException {
        LOGGER.info("Got rpc {} ", request.requestToString());
        try {
            Document newDocument = DocumentUtils.getNewDocument();
            Element done = newDocument.createElementNS(JBConstants.JB_NS, "success");
            done.setTextContent("true");
            response.addRpcOutputElement(done);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
