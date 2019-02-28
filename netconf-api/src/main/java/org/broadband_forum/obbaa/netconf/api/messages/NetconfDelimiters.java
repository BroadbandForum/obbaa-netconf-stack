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

package org.broadband_forum.obbaa.netconf.api.messages;

import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class NetconfDelimiters {

    public static ByteBuf[] rpcEndOfMessageDelimiter() {
        return new ByteBuf[]{Unpooled.wrappedBuffer(NetconfResources.RPC_EOM_DELIMITER.getBytes()) // ]]>]]>
        };
    }

    public static String rpcEndOfMessageDelimiterString() {
        return NetconfResources.RPC_EOM_DELIMITER;
    }

    public static ByteBuf[] rpcChunkMessageDelimiter() {
        return new ByteBuf[]{Unpooled.wrappedBuffer(NetconfResources.RPC_CHUNKED_DELIMITER.getBytes()) //  \n##\n
        };
    }
}
