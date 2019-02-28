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
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;

/**
 * Netconf {@code <get-config>} {@code <filter>} element.
 * 
 * @see {@link GetConfigRequest}.
 * 
 *      Netconf {@code <create-subscription>} {@code <filter>} element.
 * @see {@link CreateSubscriptionRequest}.
 * 
 *
 * 
 */
public class NetconfFilter {
    
    public static final String SUBTREE_TYPE = "subtree";
    
    private String m_type;
    
    private List<Element> m_xmlFilters = new ArrayList<>();

    public String getType() {
        return m_type;
    }

    public NetconfFilter setType(String type) {
        this.m_type = type;
        return this;
    }

    public List<Element> getXmlFilterElements() {
        return m_xmlFilters;
    }

    public NetconfFilter setXmlFilters(List<Element> xmlFilters) {
        this.m_xmlFilters = xmlFilters;
        return this;
    }

    public NetconfFilter addXmlFilter(Element xmlFilter) {
        this.m_xmlFilters.add(xmlFilter);
        return this;
    }

    @Override
    public String toString() {
        return "Filter [type=" + m_type + ", xmlFilter=" + m_xmlFilters + "]";
    }

    public Element getXmlFilter() {
        Document doc = DocumentUtils.createDocument();
        Element filter = doc.createElementNS(NetconfResources.NETCONF_RPC_NS_1_0, NetconfResources.FILTER);
        for(Element filterElement : m_xmlFilters){
            filter.appendChild(doc.importNode(filterElement, true));
        }
        return filter;
    }
}
