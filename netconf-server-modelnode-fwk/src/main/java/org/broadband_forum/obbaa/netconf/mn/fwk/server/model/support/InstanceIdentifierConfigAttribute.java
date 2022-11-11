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

import org.broadband_forum.obbaa.netconf.api.logger.NetconfExtensions;
import org.broadband_forum.obbaa.netconf.api.messages.PojoToDocumentTransformer;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;


public class InstanceIdentifierConfigAttribute implements ConfigLeafAttribute, Comparable<InstanceIdentifierConfigAttribute> {

    public static final String DELIMITER = " ";
    public static final String COMMA = ",";
    private Element m_domValue;
    private final String m_attributeLocalName;
    private final LinkedHashSet<InstanceIdentifierAttributeComponent> m_attributeValue;
    private final String m_attributeNS;
    private final Map<String, String> m_nsPrefixMap;
    private Integer m_insertIndex = -1;
    private boolean m_isPassword = false;

    public InstanceIdentifierConfigAttribute(Map<String, String> nsPrefixMap, String attributeNamespace, String attributeLocalName, String attributeValue) {
        constructInstanceIdentifierElement(nsPrefixMap, attributeNamespace, attributeLocalName, attributeValue);

        m_attributeLocalName = attributeLocalName;
        m_attributeNS = attributeNamespace;
        m_attributeValue = constructInstanceIdentifierAttrComponent(nsPrefixMap, attributeValue, attributeNamespace);
        m_nsPrefixMap = nsPrefixMap;
    }

    private void constructInstanceIdentifierElement(Map<String, String> nsPrefixMap, String attributeNamespace, String attributeLocalName, String attributeValue) {
        Document document = ConfigAttributeFactory.getDocument();
        m_domValue = document.createElementNS(attributeNamespace, attributeLocalName);
        for (Map.Entry<String, String> entry : nsPrefixMap.entrySet()) {
            m_domValue.setAttribute(PojoToDocumentTransformer.XMLNS + entry.getValue(), entry.getKey());
        }
        m_domValue.setTextContent(attributeValue);
    }

    private LinkedHashSet<InstanceIdentifierAttributeComponent> constructInstanceIdentifierAttrComponent(Map<String, String> nsPrefixMap,
                                                                                                         String attributeValue, String attributeNamespace) {
        LinkedHashSet<InstanceIdentifierAttributeComponent> attributeComponentValue = new LinkedHashSet<>();
        String[] prefixValues = attributeValue.split("/");
        for (String prefixValuePair : prefixValues) {
            if (!prefixValuePair.isEmpty()) {
                setNodeComponent(nsPrefixMap, attributeComponentValue, prefixValuePair, attributeNamespace);
            }
        }
        return attributeComponentValue;
    }

    private void setNodeComponent(Map<String, String> nsPrefixMap, LinkedHashSet<InstanceIdentifierAttributeComponent>
            attributeComponentValue, String prefixValuePair, String attributeNamespace) {
        if (prefixValuePair.contains("[")) {
            String[] values = prefixValuePair.split("\\[");
            for (String newValue : values) {
                setNodeComponent(nsPrefixMap, attributeComponentValue, newValue, attributeNamespace);
            }
        }
        else if (prefixValuePair.contains(":")){
            String[] attributeComponent = prefixValuePair.split(":");
            String prefix = attributeComponent[0];
            String namespace = getNamespace(prefix, nsPrefixMap);
            attributeComponentValue.add(new InstanceIdentifierAttributeComponent(namespace, prefix,
                    attributeComponent[1]));
        } else {
            attributeComponentValue.add(new InstanceIdentifierAttributeComponent(attributeNamespace, null, prefixValuePair));
        }
    }

    private String getNamespace(String prefix, Map<String, String> nsPrefixMap) {
        String namespace = null;
        for (Map.Entry<String, String> nsPrefixEntry : nsPrefixMap.entrySet()) {
            if (nsPrefixEntry.getValue().equals(prefix)) {
                namespace = nsPrefixEntry.getKey();
            }
        }
        return namespace;
    }

    @Override
    public Element getDOMValue() {
        return m_domValue;
    }

    @Override
    public Element getDOMValue(final String namespace, final String prefix) {
        Collection<String> attributeNs = m_nsPrefixMap.keySet();
        if (m_attributeNS.equals(namespace)) {
            boolean addParentPrefix = true;
            for (final String ns : attributeNs) {
                if (!ns.equals(namespace)) {
                    m_domValue.setPrefix("");
                    addParentPrefix = false;
                    break;
                }
            }
            if (addParentPrefix) {
                m_domValue.setPrefix(prefix);
            }
        }
        return m_domValue;
    }

    @Override
    public String getStringValue() {
        return m_domValue.getTextContent();
    }

    @Override
    public String getNamespace() {
        StringBuilder namespacePrefix = new StringBuilder();
        for (Map.Entry<String, String> nsPrefix : m_nsPrefixMap.entrySet()) {
            namespacePrefix.append(nsPrefix.getValue() + DELIMITER + nsPrefix.getKey()); //Append prefix and then namespace
            namespacePrefix.append(COMMA);
        }
        if (namespacePrefix.length() > 0) {
            namespacePrefix.setLength(namespacePrefix.length() - 1);
        }
        return namespacePrefix.toString();
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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        InstanceIdentifierConfigAttribute that = (InstanceIdentifierConfigAttribute) o;

        if (!m_attributeLocalName.equals(that.m_attributeLocalName))
            return false;
        if (!m_attributeValue.equals(that.m_attributeValue))
            return false;
        if (m_attributeNS != null ? !m_attributeNS.equals(that.m_attributeNS) : that.m_attributeNS != null)
            return false;
        return m_nsPrefixMap.keySet().equals(that.m_nsPrefixMap.keySet());

    }

    @Override
    public int hashCode() {
        int result = m_attributeLocalName.hashCode();
        result = 31 * result + m_attributeValue.hashCode();
        result = 31 * result + (m_attributeNS != null ? m_attributeNS.hashCode() : 0);
        result = 31 * result + m_nsPrefixMap.keySet().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "InstanceIdentifierConfigAttribute{" +
                "m_attributeLocalName='" + m_attributeLocalName + '\'' +
                ", m_attributeValue='" + m_attributeValue + '\'' +
                ", m_attributeNS='" + m_attributeNS + '\'' +
                ", m_nsPrefixMap=" + m_nsPrefixMap +
                '}';
    }

    @Override
    public int compareTo(InstanceIdentifierConfigAttribute instanceIdentifierConfigAttribute) {
        return this.getStringValue().compareTo(instanceIdentifierConfigAttribute.getStringValue());
    }

    public Map<String, String> getNsPrefixMap() {
        return Collections.unmodifiableMap(m_nsPrefixMap);
    }

    public String getAttributeLocalName() {
        return m_attributeLocalName;
    }

    public LinkedHashSet<InstanceIdentifierAttributeComponent> getAttributeValue() {
        return m_attributeValue;
    }

    public String getAttributeNamespace() {
        return m_attributeNS;
    }
}
