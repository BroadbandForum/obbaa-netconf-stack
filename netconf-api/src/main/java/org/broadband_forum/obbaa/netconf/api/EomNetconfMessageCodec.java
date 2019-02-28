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

class EomNetconfMessageCodec implements NetconfMessageCodec {
    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(EomNetconfMessageCodec.class, LogAppNames.NETCONF_LIB);

    EomNetconfMessageCodec() {
    }

    @Override
    public Document decode(String msg) throws NetconfMessageBuilderException {
        LOGGER.debug("Getting request document for string {}", LOGGER.sensitiveData(msg));
        Document reqDoc = null;
        if (msg.endsWith(NetconfResources.RPC_EOM_DELIMITER)) {
            String rpcMessageModified = msg.substring(0, msg.indexOf(NetconfResources.RPC_EOM_DELIMITER));
            reqDoc = DocumentUtils.stringToDocument(rpcMessageModified);
        }
        return reqDoc;
    }

    @Override
    public byte[] encode(Document document) throws NetconfMessageBuilderException {
        return DocumentToPojoTransformer.addRpcDelimiter(DocumentToPojoTransformer.getBytesFromDocument(document));
    }
}
