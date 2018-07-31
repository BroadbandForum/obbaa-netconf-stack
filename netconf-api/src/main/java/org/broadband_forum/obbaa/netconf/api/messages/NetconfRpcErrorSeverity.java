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

public enum NetconfRpcErrorSeverity {
    Error("error"), Warning("warning");

    private String m_value;
    private static HashMap<String, NetconfRpcErrorSeverity> c_valueToName;

    private NetconfRpcErrorSeverity(String value) {
        this.m_value = value;
        registerValueToName(value, this);
    }

    private synchronized static void registerValueToName(String value, NetconfRpcErrorSeverity sev) {
        if (c_valueToName == null) {
            c_valueToName = new HashMap<String, NetconfRpcErrorSeverity>();
        }
        if (c_valueToName.containsKey(value)) {
            throw new RuntimeException("Value already exists:" + value);
        }
        c_valueToName.put(value, sev);
    }

    public String value() {
        return m_value;
    }

    public static NetconfRpcErrorSeverity getType(String value) {
        return c_valueToName.get(value);
    }
}
