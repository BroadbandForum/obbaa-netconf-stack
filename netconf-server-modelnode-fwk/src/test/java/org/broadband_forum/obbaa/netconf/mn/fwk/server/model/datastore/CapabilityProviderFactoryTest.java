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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.api.utils.SystemPropertyUtilsUserTest;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


public class CapabilityProviderFactoryTest extends SystemPropertyUtilsUserTest {

    public static final String INVALID = "inavalid";
    public static final String VALUE_1_0 = "1.0";
    public static final String VALUE_1_1 = "1.1";
    @Mock
    private SchemaRegistry m_schemaRegistry;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }


    @Test
    public void getProvider() throws Exception {
        mockPropertyUtils(NetconfResources.CAPABILITY_TYPE, VALUE_1_0);
        Set<String> expected = new HashSet<>();
        expected.add("urn:ietf:params:netconf:base:1.0");
        DynamicCapabilityProvider actual = CapabilityProviderFactory.getProvider(m_schemaRegistry, Collections.emptySet());
        assertEquals(expected, actual.getCapabilities());
    }

    @Test
    public void getProviderWithAdditionalCaps() throws Exception {
        mockPropertyUtils(NetconfResources.CAPABILITY_TYPE, VALUE_1_1);
        Set<String> additionalCaps = new HashSet<>();
        additionalCaps.add("extra");
        Set<String> expected = new HashSet<>();
        expected.add("urn:ietf:params:netconf:base:1.0");
        expected.add("urn:ietf:params:netconf:base:1.1");
        expected.addAll(additionalCaps);
        DynamicCapabilityProvider actual = CapabilityProviderFactory.getProvider(m_schemaRegistry, additionalCaps);
        assertEquals(expected, actual.getCapabilities());
    }

    @Test
    public void getStaticCapabilities() throws Exception {
        mockPropertyUtils(NetconfResources.CAPABILITY_TYPE, VALUE_1_1);
        Set<String> expected = new HashSet<>();
        expected.add("urn:ietf:params:netconf:base:1.0");
        expected.add("urn:ietf:params:netconf:base:1.1");
        Set<String> actual = CapabilityProviderFactory.getStaticCapabilities(Collections.emptySet());
        assertEquals(expected, actual);
    }

    @Test
    public void getStaticCapabilitiesWithAdditionalCaps() throws Exception {
        mockPropertyUtils(NetconfResources.CAPABILITY_TYPE, VALUE_1_1);
        Set<String> additionalCaps = new HashSet<>();
        additionalCaps.add("extra");
        Set<String> expected = new HashSet<>();
        expected.add("urn:ietf:params:netconf:base:1.0");
        expected.add("urn:ietf:params:netconf:base:1.1");
        expected.addAll(additionalCaps);
        Set<String> actual = CapabilityProviderFactory.getStaticCapabilities(additionalCaps);
        assertEquals(expected, actual);
    }

    @Test
    public void testInvalidValue() {
        mockPropertyUtils(NetconfResources.CAPABILITY_TYPE, INVALID);
        Set<String> actual = CapabilityProviderFactory.getStaticCapabilities(Collections.emptySet());
        assertEquals(0, actual.size());
    }
}