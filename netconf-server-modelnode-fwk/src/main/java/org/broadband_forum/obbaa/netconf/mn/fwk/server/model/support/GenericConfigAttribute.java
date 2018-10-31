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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class GenericConfigAttribute implements ConfigLeafAttribute, Comparable<GenericConfigAttribute> {
    private final String m_attributeLocalName;
    private final String m_attributeNS;
    private final String m_attributeValue;
    private Element m_domValue;

    public GenericConfigAttribute(String attributeLocalName, String attributeNS, String attributeValue) {
        m_attributeLocalName = attributeLocalName;
        m_attributeNS = attributeNS;
        m_attributeValue = attributeValue;
        constructGenericConfigAttrElement();
    }

    private Element constructGenericConfigAttrElement() {
        Document document = ConfigAttributeFactory.getDocument();
        m_domValue = document.createElementNS(m_attributeNS, m_attributeLocalName);
        m_domValue.setTextContent(m_attributeValue);
        return m_domValue;
    }

    @Override
    public Element getDOMValue() {
        return m_domValue;
    }

    @Override
    public String getStringValue() {
        return m_attributeValue;
    }

    @Override
    public String getNamespace() {
        return m_attributeNS;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GenericConfigAttribute that = (GenericConfigAttribute) o;

        return m_attributeValue != null ? m_attributeValue.equals(that.m_attributeValue) : that.m_attributeValue == null;

    }

    @Override
    public int hashCode() {
        return m_attributeValue != null ? m_attributeValue.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "GenericConfigAttribute{" +
            "m_attributeLocalName='" + m_attributeLocalName + '\'' +
            ", m_attributeNS='" + m_attributeNS + '\'' +
            ", m_attributeValue='" + m_attributeValue + '\'' +
            '}';
    }

    @Override
    public int compareTo(GenericConfigAttribute genericConfigAttribute) {
        return this.getStringValue().compareTo(genericConfigAttribute.getStringValue());
    }
}
