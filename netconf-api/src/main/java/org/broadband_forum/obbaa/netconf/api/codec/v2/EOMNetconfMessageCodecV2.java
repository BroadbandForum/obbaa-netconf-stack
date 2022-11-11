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

package org.broadband_forum.obbaa.netconf.api.codec.v2;

import static org.broadband_forum.obbaa.netconf.api.util.ByteBufUtils.unpooledHeapByteBuf;
import static org.broadband_forum.obbaa.netconf.api.util.NetconfResources.RPC_EOM_DELIMITER;

import java.nio.charset.StandardCharsets;

import org.broadband_forum.obbaa.netconf.api.LogAppNames;
import org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.w3c.dom.Document;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;

public class EOMNetconfMessageCodecV2 extends DelimiterBasedFrameDecoder implements NetconfMessageCodecV2 {
    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(EOMNetconfMessageCodecV2.class, LogAppNames.NETCONF_LIB);
    public EOMNetconfMessageCodecV2() {
        super(Integer.MAX_VALUE, unpooledHeapByteBuf().writeBytes(RPC_EOM_DELIMITER.getBytes()));
    }

    @Override
    public byte[] encode(Document msg) throws NetconfMessageBuilderException {
        return DocumentToPojoTransformer.addRpcDelimiter(DocumentToPojoTransformer.getBytesFromDocument(msg));
    }

    @Override
    public DocumentInfo decode(ByteBuf in) throws NetconfMessageBuilderException {
        int before = in.refCnt();
        int after = before;
        try {
            ByteBuf decoded = (ByteBuf) decode(null, in);
            after = in.refCnt();
            if (decoded != null) {
                String decodedStr = decoded.readCharSequence(decoded.readableBytes(), StandardCharsets.UTF_8).toString();
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("The content of the rpc after decoding is {}", LOGGER.sensitiveData(decodedStr));
                }
                return new DocumentInfo(DocumentUtils.stringToDocument(decodedStr), decodedStr);
            }
        } catch (Exception e) {
            throw new NetconfMessageBuilderException(e);
        } finally {
            int diff = after - before;
            if(diff > 0) {
                in.release(diff);
            }
        }
        return null;
    }
}

