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

import static org.broadband_forum.obbaa.netconf.api.util.NetconfResources.GET;

import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.w3c.dom.Document;

/**
 * Netconf request to perform {@code <get>} operation.
 * 
 * 
 * 
 */
public class GetRequest extends AbstractNetconfGetRequest {

    public static final String RUNNING_DATA_STORE = StandardDataStores.RUNNING;

    private NetconfFilter m_filter;

    private boolean m_includeConfig = true;

    private String m_sliceOwner;

    public boolean isIncludeConfig() {
        return m_includeConfig;
    }

    public void setIncludeConfig(boolean includeConfig) {
        m_includeConfig = includeConfig;
    }

    public GetRequest setFilter(NetconfFilter filter) {
        this.m_filter = filter;
        return this;
    }

    public String getSliceOwner() {
        return m_sliceOwner;
    }

    public void setSliceOwner(String sliceOwner) {
        m_sliceOwner = sliceOwner;
    }

    public NetconfFilter getFilter() {
        return m_filter;
    }

    @Override
    public Document getRequestDocumentInternal() throws NetconfMessageBuilderException {
        Document doc = new PojoToDocumentTransformer().newNetconfRpcDocument(m_messageId)
                .addGetElement(m_filter,m_sliceOwner, getWithDefaults(), m_withDelay, m_depth, m_fieldValues).build();
        return doc;
    }

    @Override
    public String getRpcType() {
        return GET;
    }

}
