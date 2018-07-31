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

import org.w3c.dom.Element;

/**
 * Created by sgs on 2/2/17.
 */
public class GenericConfigAttribute implements ConfigLeafAttribute, Comparable<GenericConfigAttribute> {
    private String m_value;

    public GenericConfigAttribute(String value) {
        m_value = value;
    }

    @Override
    public Element getDOMValue() {
        throw new UnsupportedOperationException("Invalid operation for Generic config attribute");
    }

    @Override
    public String getStringValue() {
        return m_value;
    }

    @Override
    public String getNamespace() {
        throw new UnsupportedOperationException("Invalid operation for Generic config attribute");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        GenericConfigAttribute that = (GenericConfigAttribute) o;

        return m_value.equals(that.m_value);

    }

    @Override
    public int hashCode() {
        return m_value.hashCode();
    }

    @Override
    public String toString() {
        return "GenericConfigAttribute{" +
                "m_value='" + m_value + '\'' +
                '}';
    }

    @Override
    public int compareTo(GenericConfigAttribute genericConfigAttribute) {
        return this.getStringValue().compareTo(genericConfigAttribute.getStringValue());
    }
}
