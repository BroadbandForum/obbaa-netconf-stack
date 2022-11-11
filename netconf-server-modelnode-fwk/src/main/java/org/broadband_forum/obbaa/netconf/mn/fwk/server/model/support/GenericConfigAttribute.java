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

import static org.broadband_forum.obbaa.netconf.api.util.CryptUtil2.ENCR_STR_PATTERN;

import org.apache.commons.lang3.StringUtils;
import org.broadband_forum.obbaa.netconf.api.util.CryptUtil2;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class GenericConfigAttribute implements ConfigLeafAttribute,Comparable<GenericConfigAttribute> {
    private final String m_attributeLocalName;
    private final String m_attributeNS;
    private final String m_attributeValue;
    private Element m_domValue;
    private Integer m_insertIndex = -1;
    public static final String CDATA_OPEN_TAG = "<![CDATA[";
    public static final String CDATA_CLOSE_TAG = "]]>";
    private boolean m_isPassword = false;

    public GenericConfigAttribute(String attributeLocalName, String attributeNS, String attributeValue) {
        m_attributeLocalName = attributeLocalName;
        m_attributeNS = attributeNS;
        m_attributeValue = attributeValue;
        constructGenericConfigAttrElement();
    }

    private Element constructGenericConfigAttrElement() {
        Document document = ConfigAttributeFactory.getDocument();
        m_domValue = document.createElementNS(m_attributeNS, m_attributeLocalName);
        if (m_attributeValue.trim().startsWith(CDATA_OPEN_TAG) && m_attributeValue.trim().endsWith(CDATA_CLOSE_TAG)) {
            //removing CDATA tag before creating CDATA section because createCDATASection() automatically wrap CDATA tag around value
            m_domValue.appendChild(document.createCDATASection(removeCDATAFromAttributeValue(m_attributeValue)));
        } else {
            m_domValue.setTextContent(m_attributeValue);
        }
        return m_domValue;
    }

    private String removeCDATAFromAttributeValue(String attributeValue) {
        return attributeValue.trim()
                .replace(CDATA_OPEN_TAG, StringUtils.EMPTY)
                .replace(CDATA_CLOSE_TAG, StringUtils.EMPTY);
    }

    @Override
    public synchronized Element getDOMValue() {
        return m_domValue;
    }

    @Override
    public Element getDOMValue(final String namespace, final String prefix) {
        if (getNamespace().equals(namespace)) {
            m_domValue.setPrefix(prefix);
        }
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
    public String xPathString(SchemaRegistry schemaRegistry, String parentNodeXPath) {
        return xPathString(schemaRegistry, m_attributeNS, parentNodeXPath, m_attributeLocalName);
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

    public static String xPathString(SchemaRegistry schemaRegistry, String nameSpace, String parentNodeXPath, String localName) {
        StringBuilder sb = new StringBuilder();
        localName = removePrefix(localName);
        sb.append(parentNodeXPath).append("/");
        String moduleName = schemaRegistry.getModuleNameByNamespace(nameSpace);
        if(moduleName != null) {
            sb.append(moduleName + ":" +localName);
        } else {
            sb.append(localName);
        }
        return sb.toString();
    }

    private static String removePrefix(String localName) {
        int colonIndex = localName.indexOf(":");
        if(colonIndex >-1){
            return localName.substring(colonIndex+1);
        }
        return localName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GenericConfigAttribute that = (GenericConfigAttribute) o;

        return m_attributeValue != null ? areValuesEqual(that) : that.m_attributeValue == null;

    }

    public boolean areValuesEqual(GenericConfigAttribute that) {
        if(isPassword()){
            String thisDecryptedValue = ENCR_STR_PATTERN.matcher(m_attributeValue).matches() ? CryptUtil2.decrypt(m_attributeValue) : m_attributeValue;
            String thatDecryptedValue = ENCR_STR_PATTERN.matcher(that.m_attributeValue).matches() ? CryptUtil2.decrypt(that.m_attributeValue) : that.m_attributeValue;
            return thisDecryptedValue.equals(thatDecryptedValue);
        }else {
            return m_attributeValue.equals(that.m_attributeValue);
        }
    }

    @Override
    public int hashCode() {
        return m_attributeValue != null ? getAttributeValueHashCode() : 0;
    }

    public int getAttributeValueHashCode() {
        return isPassword() && ENCR_STR_PATTERN.matcher(m_attributeValue).matches() ? CryptUtil2.decrypt(m_attributeValue).hashCode() : m_attributeValue.hashCode();
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
