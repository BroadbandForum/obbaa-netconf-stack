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

package org.broadband_forum.obbaa.netconf.server.model.notification.utils;

import org.broadband_forum.obbaa.netconf.api.server.notification.Stream;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class StreamUtilTest {

    @Test
    public void testLoadStreamList() {
        List<Stream> streamList = StreamUtil.loadStreamList("/streams.xml");
        Stream netconfStream = streamList.get(0);
        assertEquals("NETCONF", netconfStream.getName());
        assertEquals("default NETCONF event stream", netconfStream.getDescription());
        assertTrue(netconfStream.getReplaySupport());
        assertNull(netconfStream.getReplayLogCreationTime());
        assertNull(netconfStream.getReplayLogAgedTime());

        Stream alarmStream = streamList.get(1);
        assertEquals("ALARM", alarmStream.getName());
        assertEquals("ALARM event stream", alarmStream.getDescription());
        assertTrue(alarmStream.getReplaySupport());
        assertNull(alarmStream.getReplayLogCreationTime());
        assertNull(alarmStream.getReplayLogAgedTime());

        Stream configChangeStream = streamList.get(2);
        assertEquals("CONFIG_CHANGE", configChangeStream.getName());
        assertEquals("CONFIGURATION CHANGES event stream", configChangeStream.getDescription());
        assertTrue(configChangeStream.getReplaySupport());
        assertNull(configChangeStream.getReplayLogCreationTime());
        assertNull(configChangeStream.getReplayLogAgedTime());

        Stream stateChangeStream = streamList.get(3);
        assertEquals("STATE_CHANGE", stateChangeStream.getName());
        assertEquals("STATE CHANGES event stream", stateChangeStream.getDescription());
        assertTrue(stateChangeStream.getReplaySupport());
        assertNull(stateChangeStream.getReplayLogCreationTime());
        assertNull(stateChangeStream.getReplayLogAgedTime());
    }

    @Test
    public void testLoadStreamListWithException() {
        try {
            StreamUtil.loadStreamList("/getStreamsRequest.xml");
            fail("Expected the stream will be returned but exception is threw");
        } catch (RuntimeException expectedEx) {
            assertEquals("Cannot unmarshal xml", expectedEx.getMessage());
        }
    }
}
