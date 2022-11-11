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

import org.w3c.dom.Element;

import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;

/**
 * Inner {@code<config>} content of a Netconf {@code<edit-config>} request.
 * 
 * 
 * 
 */
public class EditConfigElement {

    private List<Element> m_configElementContents = new ArrayList<Element>();

    public List<Element> getConfigElementContents() {
        return m_configElementContents;
    }

    public EditConfigElement addConfigElementContent(Element configElementContent) {
        this.m_configElementContents.add(configElementContent);
        return this;
    }

    public EditConfigElement setConfigElementContents(List<Element> configElementContent) {
        this.m_configElementContents = configElementContent;
        return this;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_configElementContents == null) ? 0 : m_configElementContents.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        EditConfigElement other = (EditConfigElement) obj;
        if (m_configElementContents == null) {
            if (other.m_configElementContents != null)
                return false;
        } else {
            for (int i = 0; i < m_configElementContents.size(); i++) {
                Element thisElement = m_configElementContents.get(i);
                Element otherElement = other.m_configElementContents.get(i);
                if (!thisElement.isEqualNode(otherElement)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "EditConfigElement [configElementContents=" + m_configElementContents + "]";
    }

    public Element getXmlElement() throws NetconfMessageBuilderException {
        return new PojoToDocumentTransformer().newEditconfigElement(m_configElementContents);

    }

}
