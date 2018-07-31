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

package org.broadband_forum.obbaa.netconf.server.ssh;

import org.apache.log4j.Logger;
import org.apache.sshd.common.io.IoOutputStream;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.channel.ChannelSession;
import org.w3c.dom.Document;

import org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerMessageListener;
import org.broadband_forum.obbaa.netconf.api.server.ServerMessageHandler;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;

public class ChunkedNetconfMessageHandler extends AbstractSshNetconfServerMessageHandler {
    private static final Logger LOGGER = Logger.getLogger(ChunkedNetconfMessageHandler.class);

    public ChunkedNetconfMessageHandler(NetconfServerMessageListener axsNetconfServerMessageListener, IoOutputStream
            out,
                                        ExitCallback exitCallBack, ServerMessageHandler serverMessageHandler,
                                        ChannelSession channel) {
        super(axsNetconfServerMessageListener, out, exitCallBack, serverMessageHandler, channel);
    }

    @Override
    protected Document getRequestDocument(String rpcMessage) throws NetconfMessageBuilderException {
        LOGGER.debug(String.format("Getting request document for string %s", rpcMessage));
        Document responseDoc = null;
        if (rpcMessage.endsWith(NetconfResources.RPC_CHUNKED_DELIMITER)) {
            String rpcMessageModified = DocumentToPojoTransformer.processChunkedMessage(rpcMessage);
            responseDoc = DocumentUtils.stringToDocument(rpcMessageModified);
        }
        return responseDoc;
    }

    @Override
    protected byte[] getResponseBytes(Document responseDoc) throws NetconfMessageBuilderException {
        return DocumentToPojoTransformer.chunkMessage(Integer.MAX_VALUE, DocumentUtils.documentToString(responseDoc))
                .getBytes();
    }

}
