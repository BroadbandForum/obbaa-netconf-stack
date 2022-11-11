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

import static org.broadband_forum.obbaa.netconf.api.util.NetconfResources.COPY_CONFIG;

import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Netconf request to perform {@code <copy-config> } operation.
 * 
 * 
 * 
 */
public class CopyConfigRequest extends AbstractNetconfRequest {

    private String m_source;
    private boolean m_sourceIsUrl = false;
    private String m_target;
    private boolean m_targetIsUrl = false;
    private Element m_sourceConfigElement;
    private boolean m_withTransactionId = false;

    @Override
    public Document getRequestDocumentInternal() throws NetconfMessageBuilderException {
        PojoToDocumentTransformer builder = new PojoToDocumentTransformer().newNetconfRpcDocument(m_messageId).addCopyConfigElement(m_withTransactionId,
                m_source, m_sourceIsUrl, m_target, m_targetIsUrl, m_sourceConfigElement);
        return builder.build();
    }

    public CopyConfigRequest setSource(String source, boolean isUrl) {
        this.m_source = source;
        this.m_sourceIsUrl = isUrl;
        return this;
    }

    public CopyConfigRequest setSourceRunning() {
        this.m_source = StandardDataStores.RUNNING;
        this.m_sourceIsUrl = false;
        return this;
    }

    public String getTarget() {
        return m_target;
    }

    public CopyConfigRequest setTarget(String target, boolean isUrl) {
        this.m_target = target;
        this.m_targetIsUrl = isUrl;
        return this;
    }

    public CopyConfigRequest setTargetRunning() {
        this.m_target = StandardDataStores.RUNNING;
        this.m_targetIsUrl = false;
        return this;
    }

    public String getSource() {
        return m_source;
    }

    @Override
    public String toString() {
        return "CopyConfigRequest [source=" + m_source + ", sourceIsUrl=" + m_sourceIsUrl + ", target=" + m_target + ", targetIsUrl="
                + m_targetIsUrl + ", sourceConfigElement=" + m_sourceConfigElement + "]";
    }

    public Element getSourceConfigElement() {
        return m_sourceConfigElement;
    }

    public CopyConfigRequest setSourceConfigElement(Element sourceConfigElement) {
        this.m_sourceConfigElement = sourceConfigElement;
        return this;

    }

    public void setTransactionId(boolean withTransactionId) {
        m_withTransactionId = withTransactionId;
    }

    public boolean getTransactionId(){
        return m_withTransactionId;
    }

    @Override
    public String getRpcType() {
        return COPY_CONFIG;
    }
}
