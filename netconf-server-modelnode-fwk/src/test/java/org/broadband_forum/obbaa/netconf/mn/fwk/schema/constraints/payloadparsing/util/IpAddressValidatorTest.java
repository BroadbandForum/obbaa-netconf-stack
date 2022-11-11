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

package org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class IpAddressValidatorTest {

    @Test
    public void testIsValidIPV6() {
        assertFalse(IPAddressValidator.isValidIpV6(""));
        assertFalse(IPAddressValidator.isValidIpV6(null));
        assertFalse(IPAddressValidator.isValidIpV6("10.131.226.116"));
        assertTrue(IPAddressValidator.isValidIpV6("2001:db8:0:5:f816:3eff:fe53:338a"));
    }

    @Test
    public void testEncapsulateIPV6_IPIsNullOrEmpty() {
        assertNull(IPAddressValidator.encapsulateIPV6(null));
        assertEquals("", IPAddressValidator.encapsulateIPV6(""));
    }

    @Test
    public void testEncapsulateIPV6_InvalidIPV6() {
        String ipAddress = "10.131.226.116";
        String expectedResponse = "10.131.226.116";
        assertEquals(expectedResponse, IPAddressValidator.encapsulateIPV6(ipAddress));
    }

    @Test
    public void testEncapsulateIPV6_ValidIPV6() {
        String ipAddress = "2001:db8:0:5:f816:3eff:fe53:338a";
        String expectedResponse = "[2001:db8:0:5:f816:3eff:fe53:338a]";
        assertEquals(expectedResponse, IPAddressValidator.encapsulateIPV6(ipAddress));
    }

}
