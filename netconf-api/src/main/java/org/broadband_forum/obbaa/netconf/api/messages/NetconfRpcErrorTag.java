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

public enum NetconfRpcErrorTag {
    DATA_EXISTS("data-exists"), DATA_MISSING("data-missing"), BAD_ATTRIBUTE("bad-attribute"), INVALID_VALUE("invalid-value"), IN_USE(
            "in-use"), TOO_BIG("too-big"), MISSING_ATTRIBUTE("missing-attribute"), UNKNOWN_ATTRIBUTE("unknown-attribute"), MISSING_ELEMENT(
            "missing-element"), BAD_ELEMENT("bad-element"), UNKNOWN_ELEMENT("unknown-element"), UNKNOWN_NAMESPACE("unknown-namespace"), ACCESS_DENIED(
            "access-denied"), LOCK_DENIED("lock-denied"), RESOURCE_DENIED("resource-denied"), ROLLBACK_FAILED("rollback-failed"), OPERATION_NOT_SUPPORTED(
            "operation-not-supported"), OPERATION_FAILED("operation-failed"), PARTIAL_OPERATION("partial-operation"), MALFORMED_MESSAGE(
            "malformed-message"), GENERAL_ERROR("general-error");

    private String m_value;
    private static HashMap<String, NetconfRpcErrorTag> m_valueToName;

    private NetconfRpcErrorTag(String value) {
        this.m_value = value;
        registerValueToName(this, value);
    }

    private synchronized void registerValueToName(NetconfRpcErrorTag tag, String value) {
        if (m_valueToName == null) {
            m_valueToName = new HashMap<String, NetconfRpcErrorTag>();
        }
        if (m_valueToName.containsKey(value)) {
            throw new RuntimeException("Value already exists:" + value);
        }
        m_valueToName.put(value, tag);
    }

    public String value() {
        return m_value;
    }

    public static NetconfRpcErrorTag getType(String value) {

        return m_valueToName.get(value);
    }
}
