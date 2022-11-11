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

import static org.broadband_forum.obbaa.netconf.api.util.NetconfResources.GET_CONFIG;

import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.w3c.dom.Document;

/**
 * Netconf request to perform {@code <get-config>} operation.
 * 
 * 
 * 
 */
public class GetConfigRequest extends AbstractNetconfGetRequest {

    public static final String RUNNING_DATA_STORE = StandardDataStores.RUNNING;

    private String m_source = RUNNING_DATA_STORE;

    private NetconfFilter m_filter;

    public String getSource() {
        return m_source;
    }

    public GetConfigRequest setSource(String source) {
        this.m_source = source;
        return this;
    }

    public GetConfigRequest setFilter(NetconfFilter filter) {
        this.m_filter = filter;
        return this;
    }

    public NetconfFilter getFilter() {
        return m_filter;
    }

    public GetConfigRequest setSourceRunning() {
        setSource(RUNNING_DATA_STORE);
        return this;
    }

    @Override
    public Document getRequestDocumentInternal() throws NetconfMessageBuilderException {
        Document doc = new PojoToDocumentTransformer().newNetconfRpcDocument(m_messageId)
                .addGetConfigElement(m_source, m_filter, getWithDefaults(), m_withDelay, m_depth, m_fieldValues).build();
        return doc;
    }

    @Override
    public String getRpcType() {
        return GET_CONFIG;
    }

}
