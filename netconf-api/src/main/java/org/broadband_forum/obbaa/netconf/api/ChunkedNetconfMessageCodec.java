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

package org.broadband_forum.obbaa.netconf.api;

import org.w3c.dom.Document;

import org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;

class ChunkedNetconfMessageCodec implements NetconfMessageCodec {
    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(ChunkedNetconfMessageCodec.class, LogAppNames.NETCONF_LIB);

    private int m_maxSizeOfChunkMsg;
    private int m_chunkSize;

    ChunkedNetconfMessageCodec(int maxSizeOfChunkMsg, int chunkSize) {
        m_maxSizeOfChunkMsg = maxSizeOfChunkMsg;
        m_chunkSize = chunkSize;
    }

    @Override
    public Document decode(String msg) throws NetconfMessageBuilderException, MessageToolargeException {
        LOGGER.debug("Decoding chunked message: {}", LOGGER.sensitiveData(msg));
        int msgSize = msg.getBytes().length;
        Document responseDoc = null;
        if (msgSize < m_maxSizeOfChunkMsg) {
            if (msg.endsWith(NetconfResources.RPC_CHUNKED_DELIMITER)) {
                String rpcMessageModified = DocumentToPojoTransformer.processChunkedMessage(msg);
                responseDoc = DocumentUtils.stringToDocument(rpcMessageModified);
            }
        } else {
            throw new MessageToolargeException("Incoming message too long. Not decoding the message to avoid buffer overrun, message size: " + msgSize +" max-size: " + m_maxSizeOfChunkMsg);
        }
        return responseDoc;
    }

    @Override
    public byte[] encode(Document document) throws NetconfMessageBuilderException {
        return DocumentToPojoTransformer.chunkMessage(m_chunkSize, DocumentUtils.documentToString(document)).getBytes();
    }
}
