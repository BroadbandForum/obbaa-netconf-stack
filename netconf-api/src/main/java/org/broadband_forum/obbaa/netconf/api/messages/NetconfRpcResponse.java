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

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.broadband_forum.obbaa.netconf.api.NetconfMessage;
import org.broadband_forum.obbaa.netconf.api.NetconfRpcPayLoadType;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;

public class NetconfRpcResponse extends NetConfResponse implements NetconfMessage{
    
    private List<Element> m_outputElements = new ArrayList<>();
    private RpcName m_rpcName;
    private Element m_rpcContext;

    public List<Element> getRpcOutputElements() {
        return m_outputElements;
    }
    
    public NetconfRpcResponse addRpcOutputElement(Element outputElement) {
        m_outputElements.add(outputElement);
        return this;
    }
    
    @Override
    public Document getResponseDocument() throws NetconfMessageBuilderException {
        synchronized (this){
            PojoToDocumentTransformer responseBuilder = new PojoToDocumentTransformer()
                    .newNetconfRpcReplyDocument(getMessageId(),
                            getOtherRpcAttributes()).addRpcErrors(getErrors());
            if (isOk()) {
                responseBuilder.addOk();
            }

            for (Element outputElement: m_outputElements) {
                responseBuilder.addRpcElement(outputElement);
            }

            return responseBuilder.build();
        }
    }
    
    public RpcName getRpcName() {
        return m_rpcName;
    }

    public void setRpcName(RpcName rpcName){
        m_rpcName = rpcName;
    }
    
    @Override
    public String toString() {
        return "NetconfRpcResponse [m_rpcName=" + m_rpcName + ",m_messageId=" + getMessageId() + ", m_ok=" + isOk() + "]";
    }

    @Override
    public NetconfRpcPayLoadType getType() {
        return NetconfRpcPayLoadType.RESPONSE;
    }
    
	public Element getRpcContext() {
		return m_rpcContext;
	}

	public void setRpcContext(Element rpcContext) {
		this.m_rpcContext = rpcContext;
	}
}
