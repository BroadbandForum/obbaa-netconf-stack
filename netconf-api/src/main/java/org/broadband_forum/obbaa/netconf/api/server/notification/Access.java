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

package org.broadband_forum.obbaa.netconf.api.server.notification;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "stream")
public class Access {

    private String m_encoding;
    private String m_location;

    public String getEncoding() {
        return m_encoding;
    }

    @XmlElement(name = "encoding")
    public void setEncoding(String encoding) {
        m_encoding = encoding;
    }

    public String getLocation() {
        return m_location;
    }

    @XmlElement(name = "location")
    public void setLocation(String location) {
        m_location = location;
    }

    @Override
    public String toString() {
        return "Access{" +
                "m_encoding='" + m_encoding + '\'' +
                ", m_location='" + m_location + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Access that = (Access) o;
        if (!m_encoding.equals(that.m_encoding)) return false;
        return m_location.equals(that.m_location);
    }

    @Override
    public int hashCode() {
        int result = m_encoding.hashCode();
        result = 31 * result + m_location.hashCode();
        return result;
    }
}
