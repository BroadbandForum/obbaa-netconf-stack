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

import static org.broadband_forum.obbaa.netconf.api.util.NetconfResources.NETCONF_RPC_NS_1_0;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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

    public static final String XPATH_TYPE = "xpath";
    
    private String m_type = SUBTREE_TYPE;

    private List<Element> m_xmlFilters = new ArrayList<>();

    private Map<String, String> m_nsPrefixMap = new HashMap<>();

    private String m_selectAttribute;

    public String getType() {
        return m_type;
    }

    public NetconfFilter setType(String type) {
        if(type != null){
            this.m_type = type;
        }
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
        Element filter = doc.createElementNS(NETCONF_RPC_NS_1_0, NetconfResources.FILTER);
        if(!SUBTREE_TYPE.equals(m_type)){
            filter.setAttributeNS(NETCONF_RPC_NS_1_0, NetconfResources.NETCONF_RPC_NS_PREFIX+NetconfResources.TYPE, m_type);
        }

        for(Element filterElement : m_xmlFilters){
            filter.appendChild(doc.importNode(filterElement, true));
        }
        return filter;
    }

    public void setSelectAttribute(String selectAttr, Map<String, String> nsPrefixMap) {
        m_selectAttribute = selectAttr;
        m_nsPrefixMap = nsPrefixMap;
    }

    public Map<String, String> getNsPrefixMap() {
        return m_nsPrefixMap;
    }

    public String getSelectAttribute() {
        return m_selectAttribute;
    }
}
