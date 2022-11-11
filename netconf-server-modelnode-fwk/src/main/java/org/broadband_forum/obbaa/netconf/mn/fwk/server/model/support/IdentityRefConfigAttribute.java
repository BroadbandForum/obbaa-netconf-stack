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

import static org.broadband_forum.obbaa.netconf.api.messages.PojoToDocumentTransformer.XMLNS;
import static org.broadband_forum.obbaa.netconf.api.messages.PojoToDocumentTransformer.XMLNS_NAMESPACE;
import static org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.IdentityRefUtil.COLON;

import org.broadband_forum.obbaa.netconf.api.logger.NetconfExtensions;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class IdentityRefConfigAttribute implements ConfigLeafAttribute, Comparable<IdentityRefConfigAttribute> {

    private final String m_attributePrefix;
    private Element m_domValue;
    private final String m_attributeLocalName;
    private final String m_attributeValue;
    private final String m_attributeNS;
    private final String m_identityRefNS;
    private final String m_identityRefPrefix;
    private Integer m_insertIndex = -1;
    private boolean m_isPassword = false;

    public IdentityRefConfigAttribute(String identityRefNs, String identityRefPrefix, String attributeLocalName, String attributeValue,
                                      String attributeNamespace) {
        this(ConfigAttributeFactory.getDocument(), identityRefNs, identityRefPrefix, attributeLocalName, attributeValue,
                attributeNamespace, null);
    }
    public IdentityRefConfigAttribute(Document doc, String identityRefNs, String identityRefPrefix, String attributeLocalName,
                                      String attributeValue,
                                      String attributeNamespace, String attributePrefix) {
        m_domValue = constructIdentityRefElement(doc, identityRefNs, identityRefPrefix, attributeLocalName,
                attributeValue, attributeNamespace, attributePrefix);
        m_attributeLocalName = attributeLocalName;
        m_attributeValue = attributeValue;
        m_attributeNS = attributeNamespace;
        m_identityRefNS = identityRefNs;
        m_identityRefPrefix = identityRefPrefix;
        m_attributePrefix = attributePrefix;
    }

    public IdentityRefConfigAttribute(String identityRefNs, String identityRefPrefix, Element element) {
        this(identityRefNs, identityRefPrefix, element.getLocalName(), element.getTextContent(), element.getNamespaceURI());
    }

    private Element constructIdentityRefElement(Document document, String identityRefNs, String identityRefPrefix,
            String attributeLocalName, String attributeValue, String attributeNamespace, String attrPrefix) {
        if (attributeNamespace != null) {
            if(attrPrefix != null && !attrPrefix.isEmpty()){
                m_domValue = document.createElementNS(attributeNamespace, attrPrefix+":"+attributeLocalName);
            } else {
                m_domValue = document.createElementNS(attributeNamespace, attributeLocalName);
            }
        } else {
            m_domValue = document.createElement(attributeLocalName);
        }
        if (identityRefNs != null && identityRefPrefix != null) {
            m_domValue.setAttributeNS(XMLNS_NAMESPACE, XMLNS + identityRefPrefix, identityRefNs);
        }
        m_domValue.setTextContent(attributeValue.trim());
        return m_domValue;
    }

    @Override
    public Element getDOMValue() {
        return m_domValue;
    }

    @Override
    public Element getDOMValue(final String namespace, final String prefix) {
        if (m_attributeNS.equals(namespace)) {
            if (m_identityRefNS.equals(namespace)) {
                m_domValue.setPrefix(prefix);
            } else {
                m_domValue.setPrefix("");
            }
        }
        return m_domValue;
    }

    @Override
    public String getStringValue() {
        return m_domValue.getTextContent().trim();
    }

    @Override
    public String getNamespace() {
        return m_identityRefNS;
    }

    @Override
    public String xPathString(SchemaRegistry schemaRegistry, String parentNodeXPath) {
        return GenericConfigAttribute.xPathString(schemaRegistry, m_attributeNS, parentNodeXPath, m_attributeLocalName);
    }

    @Override
    public Integer getInsertIndex() {
        return m_insertIndex;
    }

    @Override
    public void setInsertIndex(Integer insertIndex) {
        m_insertIndex = insertIndex;
    }

    @Override
    public void setIsPassword(boolean isPassword) {
        m_isPassword = isPassword;
    }

    @Override
    public Boolean isPassword() {
        return m_isPassword;
    }

    public String getNamespacePrefix() {
        return m_identityRefPrefix;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        IdentityRefConfigAttribute that = (IdentityRefConfigAttribute) o;

        if (!m_attributeLocalName.equals(that.m_attributeLocalName))
            return false;
        String attrVal = removeColon(m_attributeValue);
        String thatAttrVal = removeColon(that.m_attributeValue);
        if (!attrVal.equals(thatAttrVal))
            return false;
        if (m_attributeNS != null ? !m_attributeNS.equals(that.m_attributeNS) : that.m_attributeNS != null)
            return false;
        return m_identityRefNS.equals(that.m_identityRefNS);

    }

    private String removeColon(String str) {
        if (str.contains(COLON)) {
            String[] split = str.split(COLON);
            str = split[1].trim();
        }
        return str;
    }

    @Override
    public int hashCode() {
        int result = m_attributeLocalName.hashCode();
        result = 31 * result + m_attributeValue.hashCode();
        result = 31 * result + (m_attributeNS != null ? m_attributeNS.hashCode() : 0);
        result = 31 * result + m_identityRefNS.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "IdentityRefConfigAttribute{" +
                "m_attributeLocalName='" + m_attributeLocalName + '\'' +
                ", m_attributeValue='" + m_attributeValue + '\'' +
                ", m_attributeNS='" + m_attributeNS + '\'' +
                ", m_identityRefNS='" + m_identityRefNS + '\'' +
                ", m_identityRefPrefix='" + m_identityRefPrefix + '\'' +
                '}';
    }

    @Override
    public int compareTo(IdentityRefConfigAttribute identityRefConfigAttribute) {
        return this.getStringValue().compareTo(identityRefConfigAttribute.getStringValue());
    }
}