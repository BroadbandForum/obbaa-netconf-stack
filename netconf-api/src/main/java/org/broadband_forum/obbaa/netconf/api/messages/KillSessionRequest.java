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

import static org.broadband_forum.obbaa.netconf.api.util.NetconfResources.KILL_SESSION;

import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.w3c.dom.Document;

/**
 * Netconf request to perform {@code <kill-session>} operation.
 * 
 * 
 * 
 */
public class KillSessionRequest extends AbstractNetconfRequest {

    private Integer m_sessionId;

    @Override
    public Document getRequestDocumentInternal() throws NetconfMessageBuilderException {
        Document doc = new PojoToDocumentTransformer().newNetconfRpcDocument(m_messageId).addKillSessionElement(m_sessionId).build();
        return doc;
    }

    public KillSessionRequest setSessionId(Integer sessionId) {
        this.m_sessionId = sessionId;
        return this;
    }

    public Integer getSessionId() {
        return m_sessionId;
    }

    @Override
    public String toString() {
        return "KillSessionRequest [sessionId=" + m_sessionId + "]";
    }

    @Override
    public String getRpcType() {
        return KILL_SESSION;
    }

}
