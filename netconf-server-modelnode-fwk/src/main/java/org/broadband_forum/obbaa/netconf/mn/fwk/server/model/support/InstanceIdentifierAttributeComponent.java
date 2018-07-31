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

/**
 * Created by sgs on 2/13/17.
 */
public class InstanceIdentifierAttributeComponent {

    private String m_prefix;
    private String m_value;
    private String m_namespace;

    public InstanceIdentifierAttributeComponent(String namespace, String prefix, String value) {
        this.m_namespace = namespace;
        this.m_prefix = prefix;
        this.m_value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        InstanceIdentifierAttributeComponent that = (InstanceIdentifierAttributeComponent) o;

        if (!m_value.equals(that.m_value))
            return false;
        return m_namespace.equals(that.m_namespace);

    }

    @Override
    public int hashCode() {
        int result = m_value.hashCode();
        result = 31 * result + m_namespace.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "InstanceIdentifierAttributeComponent{" +
                "m_value='" + m_value + '\'' +
                ", m_namespace='" + m_namespace + '\'' +
                '}';
    }
}
