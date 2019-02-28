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

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientSession;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.apache.log4j.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.ParserConfigurationException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Response of a Netconf request.
 * 
 *
 * 
 */
public class NetConfResponse implements CompletableMessage {

    private String m_messageId;

    private boolean m_ok = false;
    
    private CompletableFuture<String> m_messageSentFuture = new CompletableFuture<String>();

    private List<NetconfRpcError> m_errors = new ArrayList<NetconfRpcError>();

    private Element m_data;

    private Map<String, String> m_otherRpcAttributes = new HashMap<String, String>();

    private Document m_doc;

    private static final Logger LOGGER = Logger.getLogger(NetConfResponse.class);
    
	private boolean m_instanceReplaced = false;

	private boolean closeSession = false;

	/**
     * This is unique message-id per NetconfRequest per {@link NetconfClientSession}. This is mapped to
     * {@code messaged-id tag in a <rpc> and <rpc-reply> }.
     * 
     * <pre>
     * It is the responsibility of the {@link NetconfClientSession} to set this value.
     * </pre>
     * 
     * @return the value of message-id.
     */
    public String getMessageId() {
        return m_messageId;
    }

    public NetConfResponse setMessageId(String messageId) {
        this.m_messageId = messageId;
        return this;
    }

    public boolean isOk() {
        return m_ok;
    }
    
    public CompletableFuture<String> getMessageSentFuture() {
        return m_messageSentFuture;
    }

    public NetConfResponse setOk(boolean positive) {
        this.m_ok = positive;
        return this;
    }

    public List<NetconfRpcError> getErrors() {
        return m_errors;
    }

    public NetConfResponse addError(NetconfRpcError error) {
        this.m_errors.add(error);
        return this;
    }

    public NetConfResponse addErrors(List<NetconfRpcError> errors) {
        this.m_errors.addAll(errors);
        return this;
    }

    public Element getData() {
        return m_data;
    }

    /**
     * Provides the child element list of data elemnt of get/getconfig netconf response.
     * 
     * @return
     */
    public List<Element> getDataContent() {
        List<Element> elements = new ArrayList<Element>();
        if (m_data != null) {
            NodeList list = m_data.getChildNodes();
            for (int i = 0; i < list.getLength(); i++) {
                Node node = list.item(i);
                if (node instanceof Element) {
                    elements.add((Element) node);
                }
            }
        }
        return elements;
    }

    /**
     * Add the top element contents of a netconf response.
     * 
     * <pre>
     * <b>Example:</b>
     * {@code
     * --------------------------------
     * RPC reply xml response
     * --------------------------------
     * <rpc-reply message-id="101"
     *      xmlns="urn:ietf:params:xml:ns:netconf:base:1.0">
     *    <data>
     *     <top xmlns="http://example.com/schema/1.2/stats">
     *       <interfaces>
     *         <interface>
     *           <ifName>eth0</ifName>
     *           <ifInOctets>45621</ifInOctets>
     *           <ifOutOctets>774344</ifOutOctets>
     *         </interface>
     *       </interfaces>
     *     </top>
     *    </data>
     *  </rpc-reply>
     * --------------------------------
     * Corresponding NetConfResponse
     * --------------------------------
     *  NetConfResponse response = new NetConfResponse()
     *                             .addDataContent(dataContent); // dataContent contains data starting from <top> element
     *  @param dataContent
     * @return the modified response
     */
    public NetConfResponse addDataContent(Element dataContent) {
        try {
            if (this.m_data == null) {
                this.m_data = createDataElement();
            }
            if (dataContent != null) {

                this.m_data.appendChild(m_doc.importNode(dataContent, true));
            }
        } catch (DOMException | ParserConfigurationException e) {
            LOGGER.error("Error while setting data", e);
            throw new RuntimeException(e);
        }
        return this;
    }

    /**
     * Set the list of top element contents of a netconf response.
     * 
     * <pre>
     * <b>Example:</b>
     * {@code
     * --------------------------------
     * RPC reply xml response
     * --------------------------------
     * <rpc-reply message-id="101"
     *      xmlns="urn:ietf:params:xml:ns:netconf:base:1.0">
     *    <data>
     *      <top xmlns="http://example.com/schema/1.2/stats">
     *        <interfaces>
     *          <interface>
     *            <ifName>eth0</ifName>
     *            <ifInOctets>45621</ifInOctets>
     *            <ifOutOctets>774344</ifOutOctets>
     *          </interface>
     *        </interfaces>
     *      </top>
     *      <netconf-state>
     *     	 <capabilities>
     *     	 	...
     *     	 </capabilities>
     *     	 ...
     *      </netconf-state>
     *    </data>
     *  </rpc-reply>
     * --------------------------------
     * Corresponding NetConfResponse
     * --------------------------------
     *  NetConfResponse response = new NetConfResponse()
     *                             .setDataContent(dataContent);// dataContent contains data starting from <top> elements
     *  @param dataContent
     * @return the modified response
     */
    public NetConfResponse setDataContent(List<Element> dataContents) {
        try {
            if (dataContents != null) {
                this.m_data = createDataElement();
                for (Element dataContent : dataContents) {
                    this.m_data.appendChild(this.m_doc.importNode(dataContent, true));
                }
            }
        } catch (DOMException | ParserConfigurationException e) {
            LOGGER.error("Error while setting data", e);
            throw new RuntimeException(e);
        }
        return this;
    }

    private Element createDataElement() throws ParserConfigurationException {
        if (this.m_doc == null) {
            this.m_doc = DocumentUtils.getNewDocument();
        }
        return this.m_doc.createElementNS(NetconfResources.NETCONF_RPC_NS_1_0, NetconfResources.RPC_REPLY_DATA);
    }

    public Map<String, String> getOtherRpcAttributes() {
        return m_otherRpcAttributes;
    }

    public NetConfResponse setOtherRpcAttributes(Map<String, String> otherRpcAttributes) {
        if (otherRpcAttributes != null) {
            this.m_otherRpcAttributes = otherRpcAttributes;
        }
        return this;
    }

    public NetConfResponse addOtherRpcAttribute(String key, String value) {
        this.m_otherRpcAttributes.put(key, value);
        return this;
    }

    @Override
    public String toString() {
        return "NetConfResponse [messageId=" + m_messageId + ", ok=" + m_ok + ", error=" + m_errors + ", data=" + m_data + "]";
    }

    public Document getResponseDocument() throws NetconfMessageBuilderException {
        synchronized (this){
            PojoToDocumentTransformer responseBuilder = new PojoToDocumentTransformer()
                    .newNetconfRpcReplyDocument(getMessageId(), m_otherRpcAttributes).addRpcErrors(getErrors()).addData(getData());
            if (m_ok) {
                responseBuilder.addOk();
            }
            return responseBuilder.build();
        }
    }

    public String responseToString() {
        return PojoToDocumentTransformer.responseToString(this);
    }

    public NetConfResponse setData(Element dataFromRpcReply) {
        this.m_data = dataFromRpcReply;
        return this;
    }
    
    public void setInstanceReplaced(boolean instanceReplaced) {
    	m_instanceReplaced = instanceReplaced;
    }
    
    public boolean getInstanceReplaced() {
    	return m_instanceReplaced;
    }

    public boolean isCloseSession() {
		return closeSession;
	}

	public void setCloseSession(boolean closeSession) {
		this.closeSession = closeSession;
	}
}
