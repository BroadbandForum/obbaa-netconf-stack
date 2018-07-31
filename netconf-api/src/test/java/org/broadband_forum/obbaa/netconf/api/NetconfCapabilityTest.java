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

package org.broadband_forum.obbaa.netconf.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class NetconfCapabilityTest {

    @Test
    public void testInstance() {
        String capStr1 = "urn:ietf:params:xml:ns:netmod:notification?module=nc-notifications&revision=2008-07-14" +
                "&features=replay-is-supported";
        NetconfCapability netconfCap1 = new NetconfCapability(capStr1);

        String expectedUri1 = "urn:ietf:params:xml:ns:netmod:notification";
        assertEquals(expectedUri1, netconfCap1.getUri());
        Map<String, String> expectedParam1 = new HashMap<>();
        expectedParam1.put("module", "nc-notifications");
        expectedParam1.put("revision", "2008-07-14");
        expectedParam1.put("features", "replay-is-supported");
        assertEquals(expectedParam1, netconfCap1.getParameters());

        assertEquals("2008-07-14", netconfCap1.getParameter("revision"));
        assertEquals("replay-is-supported", netconfCap1.getParameter("features"));
        assertEquals("nc-notifications", netconfCap1.getParameter("module"));

        String uri = "urn:ietf:params:xml:ns:netmod:notification";
        String revison = "2008-07-14";
        String module = "nc-notifications";
        NetconfCapability netconfCap2 = new NetconfCapability(uri, module, revison);
        List<String> expectedCap2 = new ArrayList<>();
        expectedCap2.add("urn:ietf:params:xml:ns:netmod:notification?revision=2008-07-14&module=nc-notifications");
        expectedCap2.add("urn:ietf:params:xml:ns:netmod:notification?module=nc-notifications&revision=2008-07-14");
        assertTrue(expectedCap2.contains(netconfCap2.toString()));

        String capStr3 = "urn:ietf:params:xml:ns:netmod:notification?module=nc-notifications&revision=2008-07-14";
        NetconfCapability netconfCap3 = new NetconfCapability(capStr3);
        List<String> expectedParam3 = new ArrayList<>();
        expectedParam3.add("urn:ietf:params:xml:ns:netmod:notification?module=nc-notifications&revision=2008-07-14");
        expectedParam3.add("urn:ietf:params:xml:ns:netmod:notification?revision=2008-07-14&module=nc-notifications");
        assertTrue(expectedParam3.contains(netconfCap3.toString()));

        String features = "entity-state,entity-sensor";
        NetconfCapability netconfCap4 = new NetconfCapability(uri, module, revison, features, null);
        String expectedUri4 = "urn:ietf:params:xml:ns:netmod:notification?module=nc-notifications&revision=2008-07-14" +
                "&features=entity-state,entity-sensor";
        assertEquals(expectedUri4, netconfCap4.toString());

        String deviations = "test-ietf-interfaces-dev";
        uri = "urn:ietf:params:xml:ns:yang:ietf-interfaces";
        module = "ietf-interfaces";
        NetconfCapability netconfCap5 = new NetconfCapability(uri, module, revison, null, deviations);
        String expectedUri5 = "urn:ietf:params:xml:ns:yang:ietf-interfaces?module=ietf-interfaces&revision=2008-07-14" +
                "&deviations=test-ietf-interfaces-dev";
        assertEquals(expectedUri5, netconfCap5.toString());

        capStr1 = "urn:ietf:params:xml:ns:netmod:notification";
        netconfCap1 = new NetconfCapability(capStr1);

        assertEquals(Collections.emptyMap(), netconfCap1.getParameters());
        assertNull(netconfCap1.getParameter("revision"));
    }
}
