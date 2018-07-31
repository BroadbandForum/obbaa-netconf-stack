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

import java.text.ParseException;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.joda.time.DateTime;

/**
 * A POJO containing various attributes of an Notification Event Stream This is mainly useful during <get/> and
 * notification logging
 *
 * @author pregunat
 */
@XmlType(propOrder = {"name", "description", "replaySupport"})
@XmlRootElement(name = "stream")
public class Stream {

    private String m_name;
    private String m_description;
    private boolean m_replaySupport;
    private String m_replayLogCreationTime;
    private String m_replayLogAgedTime;


    public String getName() {
        return m_name;
    }

    @XmlElement(name = "name", required = true)
    public Stream setName(String name) {
        this.m_name = name;
        return this;
    }

    public String getDescription() {
        return m_description;
    }

    @XmlElement(name = "description", required = true)
    public Stream setDescription(String description) {
        this.m_description = description;
        return this;
    }

    public boolean getReplaySupport() {
        return m_replaySupport;
    }

    @XmlElement(name = "replaySupport", required = true)
    public Stream setReplaySupport(boolean replaySupport) {
        this.m_replaySupport = replaySupport;
        return this;
    }


    public String getReplayLogCreationTime() {
        return m_replayLogCreationTime;
    }

    @XmlElement(name = "replayLogCreationTime")
    public Stream setReplayLogCreationTime(long replayLogCreationTime) throws ParseException {
        if (this.m_replaySupport) {
            this.m_replayLogCreationTime = NetconfResources.DATE_TIME_WITH_TZ.print(new DateTime(replayLogCreationTime));
        }
        return this;
    }

    public String getReplayLogAgedTime() {
        return m_replayLogAgedTime;
    }

    @XmlElement(name = "replayLogAgedTime")
    public Stream setReplayLogAgedTime(long replayLogAgedTime) throws ParseException {
        if (this.m_replaySupport) {
            this.m_replayLogAgedTime = NetconfResources.DATE_TIME_WITH_TZ.print(new DateTime(replayLogAgedTime));
        }
        return this;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_name == null) ? 0 : m_name.hashCode());
        result = prime * result + ((m_replayLogCreationTime == null) ? 0 : m_replayLogCreationTime.hashCode());
        result = prime * result + ((m_replayLogAgedTime == null) ? 0 : m_replayLogAgedTime.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Stream other = (Stream) obj;
        if (m_name == null) {
            if (other.m_name != null) {
                return false;
            }
        } else if (!m_name.equals(other.m_name)) {
            return false;
        }
        if (m_replayLogCreationTime == null) {
            if (other.m_replayLogCreationTime != null) {
                return false;
            }
        } else if (!m_replayLogCreationTime.equals(other.m_replayLogCreationTime)) {
            return false;
        }
        if (m_replayLogAgedTime == null) {
            if (other.m_replayLogAgedTime != null) {
                return false;
            }
        } else if (!m_replayLogAgedTime.equals(other.m_replayLogAgedTime)) {
            return false;
        }
        if (!m_replaySupport == other.m_replaySupport) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Stream [name=" + m_name + ", description=" + m_description + ", replaySupport=" + m_replaySupport
                + ", replayLogCreationTime=" + m_replayLogCreationTime + ", replayLogAgedTime=" + m_replayLogAgedTime
                + "]";
    }
}
