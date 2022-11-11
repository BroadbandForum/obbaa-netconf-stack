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

package org.broadband_forum.obbaa.netconf.mn.fwk;

import static org.broadband_forum.obbaa.netconf.api.util.DocumentUtils.stringToDocument;

import java.util.List;
import java.util.Objects;

import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigAttributeFactory;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.InvalidIdentityRefException;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public class AttributeIndex {
    private final SchemaPath m_attributeSP;
    private final ConfigLeafAttribute m_attribute;

    public AttributeIndex(SchemaPath attributeSP, ConfigLeafAttribute attribute) {
        m_attributeSP = attributeSP;
        m_attribute = attribute;
    }

    public AttributeIndex(SchemaRegistryImpl schemaRegistry, SchemaPath attrSP, String attrStrValue) {
        m_attributeSP = schemaRegistry.getDataSchemaNode(attrSP).getPath();
        try {
            m_attribute =  ConfigAttributeFactory.getConfigAttribute(schemaRegistry, schemaRegistry.getDataSchemaNode(attrSP), stringToDocument(attrStrValue).getDocumentElement());
        } catch (InvalidIdentityRefException | NetconfMessageBuilderException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AttributeIndex that = (AttributeIndex) o;
        return Objects.equals(m_attributeSP, that.m_attributeSP) &&
                Objects.equals(m_attribute, that.m_attribute);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_attributeSP, m_attribute);
    }

    @Override
    public String toString() {
        return "AttributeIndex{" +
                "m_attribute=" + m_attribute +
                '}';
    }
}
