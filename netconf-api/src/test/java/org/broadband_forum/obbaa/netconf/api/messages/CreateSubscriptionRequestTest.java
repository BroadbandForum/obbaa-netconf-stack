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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.text.ParseException;
import org.joda.time.DateTime;
import org.junit.Test;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;

public class CreateSubscriptionRequestTest {

    private CreateSubscriptionRequest m_createSubscriptionRequest = new CreateSubscriptionRequest();
    public static final String STREAM = "stream";
    public static final String FILTER = "filter";
    public static final String START_TIME = "2012-08-16T07:22:05Z";
    public static final String STOP_TIME = "2016-09-26T09:20:10Z";
    private CreateSubscriptionRequest m_createSubscriptionRequest_1 = new CreateSubscriptionRequest();
    private String m_stream = NetconfResources.NETCONF;
    private NetconfFilter m_filter = new NetconfFilter();
    private CreateSubscriptionRequest m_createSubscriptionRequest_null;
    private DateTime m_startTime = new DateTime("2012-08-16T07:22:05Z");
    private DateTime m_stopTime = new DateTime("2016-09-26T09:20:10Z");
    private NetconfFilter m_filter_null = null;
    private String m_stream_null = null;
    private String m_startTime_null = null;
    private String m_stopTime_null = null;
    private String m_messageId = "1";

    @Test
    public void testSetAndGetStream() {

        assertEquals(m_createSubscriptionRequest, m_createSubscriptionRequest.setStream(m_stream));
        assertEquals(m_stream, m_createSubscriptionRequest.getStream());
    }

    @Test
    public void testSetAndGetFilter() {

        NetconfFilter netconfFilter1 = m_createSubscriptionRequest.getFilter();
        m_createSubscriptionRequest.setFilter(m_filter);
        assertNotEquals(netconfFilter1, m_filter);
    }

    @Test
    public void testSetAndGetStartTime() throws ParseException {

        m_createSubscriptionRequest.setStartTime(START_TIME);
        assertEquals(m_startTime, m_createSubscriptionRequest.getStartTime());
    }

    @Test
    public void testSetAndGetStopTime() throws ParseException {
        m_createSubscriptionRequest.setStopTime(STOP_TIME);
        assertEquals(m_stopTime, m_createSubscriptionRequest.getStopTime());
    }

    @Test
    public void testGetRequestDocument() throws NetconfMessageBuilderException, ParseException {
        m_createSubscriptionRequest.setStream(m_stream);
        m_createSubscriptionRequest.setMessageId(m_messageId);
        m_createSubscriptionRequest.setFilter(m_filter);
        m_createSubscriptionRequest.setStartTime(START_TIME);
        m_createSubscriptionRequest.setStopTime(STOP_TIME);
        m_createSubscriptionRequest.getRequestDocument();
        assertNotNull(m_createSubscriptionRequest.getRequestDocument());
        assertTrue(m_createSubscriptionRequest.getRequestDocument().getElementsByTagName("create-subscription").getLength() == 1);

    }

    @Test
    public void testHashCode() throws ParseException {
        assertEquals(-1733171806, m_createSubscriptionRequest_1.hashCode());
    }

    @Test
    public void testEquals() {
        m_createSubscriptionRequest_1.equals(m_createSubscriptionRequest_null);
        assertTrue(m_createSubscriptionRequest.equals(m_createSubscriptionRequest));
        assertFalse(m_filter.equals(null));
        assertNull(m_filter_null);
        assertNull(m_stream_null);
        assertNull(m_startTime_null);
        assertNull(m_stopTime_null);
    }

    @Test
    public void testToString() {
        assertEquals("Create Subscription [Stream: NETCONF, Filter: null, Start Time: null, Stop Time: null]",
                m_createSubscriptionRequest.toString());
        assertNotNull(m_createSubscriptionRequest.toString());
    }

}
