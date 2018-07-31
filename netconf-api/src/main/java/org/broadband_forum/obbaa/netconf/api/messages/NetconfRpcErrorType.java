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

import java.util.HashMap;

public enum NetconfRpcErrorType {
    Transport("transport"), RPC("rpc"), Protocol("protocol"), Application("application");

    private String m_value;

    private static HashMap<String, NetconfRpcErrorType> c_valueToName;

    private NetconfRpcErrorType(String value) {
        this.m_value = value;
        registerValueToName(this, value);
    }

    private synchronized static void registerValueToName(NetconfRpcErrorType type, String value) {
        if (c_valueToName == null) {
            c_valueToName = new HashMap<String, NetconfRpcErrorType>();
        }
        if (c_valueToName.containsKey(value)) {
            throw new RuntimeException("Value already exists:" + value);
        }
        c_valueToName.put(value, type);
    }

    public String value() {
        return m_value;
    }

    public static NetconfRpcErrorType getType(String value) {
        return c_valueToName.get(value);
    }
}
