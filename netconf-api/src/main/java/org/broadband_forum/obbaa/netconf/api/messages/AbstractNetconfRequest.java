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

import org.w3c.dom.Document;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.w3c.dom.Element;

/**
 * An abstract class providing implementation for netconf message-id and reply timeout.
 * 
 * 
 * 
 */
public abstract class AbstractNetconfRequest {
    private static final long DEFAULT_REPLY_TIMEOUT = 60000L;
    protected String m_messageId;
    protected String m_netconfNamespace;
    protected long m_replyTimeout = DEFAULT_REPLY_TIMEOUT;
    protected int m_withDelay;
    private NetconfClientInfo m_clientInfo;
    private Element m_rpcElement;
    protected String m_userContext;
    protected String m_contextSessionId;
    private boolean m_forceInstanceCreation = false;
    protected String m_application;
    private long m_requestLength = -1;

    public Document getRequestDocument() throws NetconfMessageBuilderException {
        synchronized (this){
            return getRequestDocumentInternal();
        }
    }

    protected abstract Document getRequestDocumentInternal() throws NetconfMessageBuilderException;

    public String getMessageId() {
        return m_messageId;
    }

    public AbstractNetconfRequest setMessageId(String messageId) {
        this.m_messageId = messageId;
        return this;
    }

    public long getReplyTimeout() {
        return m_replyTimeout;
    }

    public AbstractNetconfRequest setReplyTimeout(long replyTimeoutMillis) {
        this.m_replyTimeout = replyTimeoutMillis;
        return this;
    }

    public long getRequestLength() {
        return m_requestLength;
    }

    public void setRequestLength(long requestLength) {
        m_requestLength = requestLength;
    }

    public int getWithDelay() {
        return m_withDelay;
    }

    public void setWithDelay(int withDelay) {
        this.m_withDelay = withDelay;
    }

    public NetconfClientInfo getClientInfo() {
        return m_clientInfo;
    }

    public void setClientInfo(NetconfClientInfo clientInfo) {
        this.m_clientInfo = clientInfo;
    }

    public boolean isForceInstanceCreation() {
        return m_forceInstanceCreation;
    }

    public void setForceInstanceCreation(boolean forceInstanceCreation) {
        this.m_forceInstanceCreation = forceInstanceCreation;
    }

    public String requestToString() {
        synchronized (this){
            try {
                return PojoToDocumentTransformer.requestToString(getRequestDocument());
            } catch (NetconfMessageBuilderException e) {
                throw new RuntimeException(e);
            }
        }
    }

	@Override
	public String toString() {
		return "NetconfRequest [m_messageId=" + m_messageId + ", m_clientInfo=" + m_clientInfo + "]";
	}

    public abstract String getRpcType();

    public String getRpcAttribute(String attrNs, String attr) {
        if(m_rpcElement == null){
            return null;
        }
        return m_rpcElement.getAttributeNS(attrNs, attr);
    }

    public void setRpcElement(Element rpcElement) {
        m_rpcElement = rpcElement;
    }

    public Element getRpcElement() {
        return m_rpcElement;
    }
    
    public String getAdditionalUserContext() {
        return m_userContext;
    }

    public void setUserContext(String context) {
        this.m_userContext = context;
    }

    public String getAdditionalUserSessionId() {
        return m_contextSessionId;
    }

    public void setContextSessionId(String sessionId) {
        this.m_contextSessionId = sessionId;
    }

	public String getApplication() {
		return m_application;
	}

	public void setApplication(String application) {
		this.m_application = application;
	}
    
}
