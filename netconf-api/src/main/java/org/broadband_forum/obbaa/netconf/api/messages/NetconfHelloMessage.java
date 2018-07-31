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

import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.w3c.dom.Document;

import java.util.Set;

public class NetconfHelloMessage {

    private int m_sessionId;
    private Set<String> m_caps;

    public NetconfHelloMessage setSessionId(int sessionId) {
        this.m_sessionId = sessionId;
        return this;
    }

    public NetconfHelloMessage setCapabilities(Set<String> caps) {
        this.m_caps = caps;
        return this;
    }

    public int getSessionId() {
        return m_sessionId;
    }

    public Set<String> getCapabilities() {
        return m_caps;
    }

    @Override
    public String toString() {
        return "NetconfHelloMessage [sessionId=" + m_sessionId + ", caps=" + m_caps + "]";
    }

    public Document getRequestDocument() throws NetconfMessageBuilderException {
        PojoToDocumentTransformer responseBuilder = new PojoToDocumentTransformer().newServerHelloMessage
                (getCapabilities(), getSessionId());
        return responseBuilder.build();
    }
}
