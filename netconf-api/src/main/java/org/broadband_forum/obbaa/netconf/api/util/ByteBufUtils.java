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

package org.broadband_forum.obbaa.netconf.api.util;

import org.broadband_forum.obbaa.netconf.api.LogAppNames;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;

public class ByteBufUtils {
    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(ByteBufUtils.class, LogAppNames.NETCONF_LIB);

    public static CompositeByteBuf unpooledHeapByteBuf() {
        return new CompositeByteBuf(new UnpooledByteBufAllocator(false), false, 16);
    }

    public static void releaseByteBuf(ByteBuf byteBuf) {
        if(byteBuf != null) {
            int refCount = byteBuf.refCnt();
            if(refCount > 1) {
                LOGGER.info("The reference count for ByteBuf is {}(greater than 1) which means the byte buffer is not released where is it supposed to be", refCount);
            }
            if (refCount > 0) {
                byteBuf.release(refCount);
            }
        }
    }

    public static ByteBuf reInitializeByteBuf(ByteBuf byteBuf) {
        if (byteBuf.readableBytes() == 0 && byteBuf.refCnt() > 0) { //all bytes are read. so release it
            byteBuf.release();
            byteBuf = unpooledHeapByteBuf();
        }
        return byteBuf;
    }
}
