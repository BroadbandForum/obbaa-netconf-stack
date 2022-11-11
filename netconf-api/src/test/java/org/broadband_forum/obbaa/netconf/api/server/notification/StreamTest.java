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

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StreamTest {

    private Stream m_stream;

    @Before
    public void init() throws ParseException {
        m_stream = new Stream();
        m_stream.setName("NETCONF");
        m_stream.setDescription("default NETCONF event stream");
        m_stream.setReplaySupport(true);
        m_stream.setDestination("test");
        m_stream.setReplayLogAgedTime(1615175292687L);
        m_stream.setReplayLogCreationTime(1615175292687L);
        Access access = new Access();
        access.setEncoding("xml");
        access.setLocation("restconf/streams/NETCONF/xml");
        List<Access> accesses = new ArrayList<>();
        accesses.add(access);
        m_stream.setAccess(accesses);
    }

    @Test
    public void testToString() {
        assertEquals(1, m_stream.getAccess().size());
        assertEquals("xml", m_stream.getAccess().get(0).getEncoding());
        assertEquals("restconf/streams/NETCONF/xml", m_stream.getAccess().get(0).getLocation());
        assertEquals("NETCONF", m_stream.getName());
        assertEquals("default NETCONF event stream", m_stream.getDescription());
        assertEquals("test", m_stream.getDestination());
        assertEquals(true, m_stream.getReplaySupport());
        String streamToString = m_stream.toString();
        assertTrue(streamToString.contains("Stream [name=NETCONF, description=default NETCONF event stream, replaySupport=true,"));
        assertTrue(streamToString.contains("destination=test, m_access=[Access{m_encoding='xml', m_location='restconf/streams/NETCONF/xml'}]]"));
    }

    @Test
    public void testEqual() throws ParseException {
        Stream stream = new Stream();
        stream.setName("NETCONF");
        stream.setDescription("default NETCONF event stream");
        stream.setReplaySupport(true);
        stream.setDestination("test");
        stream.setReplayLogAgedTime( 1615175292687L);
        stream.setReplayLogCreationTime(1615175292687L);
        Access access = new Access();
        access.setEncoding("xml");
        access.setLocation("restconf/streams/NETCONF/xml");
        List<Access> accesses = new ArrayList<>();
        accesses.add(access);
        stream.setAccess(accesses);
        assertEquals(stream, stream);
        assertFalse(stream.equals(null));
        assertFalse(stream.equals(new Access()));
        assertEquals(stream, m_stream);
        assertEquals(stream.hashCode(), m_stream.hashCode());
    }

    @Test
    public void testEquals_nameNotEqual() {
        Stream stream = new Stream();
        stream.setName(null);
        assertFalse(stream.equals(m_stream));
        stream.setName("ALARM");
        assertFalse(stream.equals(m_stream));
    }

    @Test
    public void testEquals_replayLogCreationTimeNotEqual() throws ParseException {
        Stream stream = new Stream();
        stream.setName("NETCONF");
        stream.setReplaySupport(true);
        assertFalse(stream.equals(m_stream));
        stream.setReplayLogCreationTime(0L);
        assertFalse(stream.equals(m_stream));
    }

    @Test
    public void testEquals_replayLogAgedTimeNotEqual() throws ParseException {
        Stream stream = new Stream();
        stream.setName("NETCONF");
        stream.setReplaySupport(true);
        stream.setReplayLogCreationTime(1615175292687L);
        assertFalse(stream.equals(m_stream));
        stream.setReplayLogAgedTime(0L);
        assertFalse(stream.equals(m_stream));
    }

    @Test
    public void testEquals_accessNotEqual() throws ParseException {
        Stream stream = new Stream();
        stream.setName("NETCONF");
        stream.setReplaySupport(true);
        stream.setReplayLogCreationTime(1615175292687L);
        stream.setReplayLogAgedTime(1615175292687L);
        assertFalse(stream.equals(m_stream));
        Access access = new Access();
        access.setEncoding("json");
        access.setLocation("restconf/streams/NETCONF/xml");
        List<Access> accesses = Arrays.asList(access);
        stream.setAccess(accesses);
        assertFalse(stream.equals(m_stream));
    }


}