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

import java.text.ParseException;

import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.joda.time.DateTime;
import org.w3c.dom.Document;

/**
 * Netconf request to perform {@code <create-subscription>} operation.
 *
 * @author pregunat
 */

public class CreateSubscriptionRequest extends NetconfRpcRequest {

    public static final String STREAM = "stream";
    public static final String FILTER = "filter";
    public static final String START_TIME = "startTime";
    public static final String STOP_TIME = "stopTime";

    private String m_stream = NetconfResources.NETCONF;
    private NetconfFilter m_filter;
    private DateTime m_startTime;
    private DateTime m_stopTime;

    public String getStream() {
        return m_stream;
    }

    public CreateSubscriptionRequest setStream(String stream) {
        m_stream = stream;
        return this;
    }

    public NetconfFilter getFilter() {
        return m_filter;
    }

    public void setFilter(NetconfFilter filter) {
        m_filter = filter;
    }

    public DateTime getStartTime() {
        return m_startTime;
    }

    /**
     * RFC 5277: "A parameter, <startTime>, used to trigger the replay feature and indicate that the replay should
     * start at the time
     * specified. If <startTime> is not present, this is not a replay subscription. It is not valid to specify start
     * times that are later
     * than the current time. If the <startTime> specified is earlier than the log can support, the replay will begin
     * with the earliest
     * available notification. This parameter is of type dateTime and compliant to [RFC3339]. Implementations must
     * support time zones."
     *
     * @param startTime
     * @return
     * @throws ParseException
     */
    public void setStartTime(String startTime) throws ParseException {
        m_startTime = new DateTime(startTime);
    }

    public void startingNow() throws ParseException {
        m_startTime = DateTime.now();
    }

    public DateTime getStopTime() {
        return m_stopTime;
    }

    /**
     * RFC 5277: "An optional parameter, <stopTime>, used with the optional replay feature to indicate the newest
     * notifications of interest.
     * If <stopTime> is not present, the notifications will continue until the subscription is terminated. Must be
     * used with and be later
     * than <startTime>. Values of <stopTime> in the future are valid. This parameter is of type dateTime and
     * compliant to [RFC3339].
     * Implementations must support time zones."
     *
     * @param stopTime
     * @return
     * @throws ParseException
     */
    public void setStopTime(String stopTime) throws ParseException {
        m_stopTime = new DateTime(stopTime);
    }

    public Document getRequestDocument() throws NetconfMessageBuilderException {
        Document doc = new PojoToDocumentTransformer().newNetconfRpcDocument(m_messageId)
                .addCreateSubscriptionElement(m_stream, m_filter, m_startTime, m_stopTime).build();
        return doc;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_filter == null) ? 0 : m_filter.hashCode());
        result = prime * result + ((m_startTime == null) ? 0 : m_startTime.hashCode());
        result = prime * result + ((m_stopTime == null) ? 0 : m_stopTime.hashCode());
        result = prime * result + ((m_stream == null) ? 0 : m_stream.hashCode());
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
        CreateSubscriptionRequest other = (CreateSubscriptionRequest) obj;
        if (m_filter == null) {
            if (other.m_filter != null) {
                return false;
            }
        } else if (!m_filter.equals(other.m_filter)) {
            return false;
        }
        if (m_startTime == null) {
            if (other.m_startTime != null) {
                return false;
            }
        } else if (!m_startTime.equals(other.m_startTime)) {
            return false;
        }
        if (m_stopTime == null) {
            if (other.m_stopTime != null) {
                return false;
            }
        } else if (!m_stopTime.equals(other.m_stopTime)) {
            return false;
        }
        if (m_stream == null) {
            if (other.m_stream != null) {
                return false;
            }
        } else if (!m_stream.equals(other.m_stream)) {
            return false;
        }
        if (!m_messageId.equals(other.m_messageId)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Create Subscription [Stream: " + m_stream + ", Filter: " + m_filter + ", Start Time: " + m_startTime
                + ", Stop Time: "
                + m_stopTime + "]";
    }
}
