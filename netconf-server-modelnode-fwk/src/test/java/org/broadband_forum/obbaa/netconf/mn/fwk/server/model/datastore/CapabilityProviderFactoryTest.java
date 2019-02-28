package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.api.utils.SystemPropertyUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;


public class CapabilityProviderFactoryTest {

    public static final String INVALID = "inavalid";
    public static final String VALUE_1_0 = "1.0";
    public static final String VALUE_1_1 = "1.1";
    @Mock
    private SchemaRegistry m_schemaRegistry;
    private SystemPropertyUtils m_systemPropertyUtils;
    private SystemPropertyUtils m_originalUtil;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        m_systemPropertyUtils = mock(SystemPropertyUtils.class);
        m_originalUtil = SystemPropertyUtils.getInstance();
        SystemPropertyUtils.setInstance(m_systemPropertyUtils);
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

    private void mockPropertyUtils(String key, String value) {
        when(m_systemPropertyUtils.getFromEnvOrSysProperty(key)).thenReturn(value);
        when(m_systemPropertyUtils.getFromEnvOrSysProperty(eq(key), anyString())).thenReturn(value);
        when(m_systemPropertyUtils.readFromEnvOrSysProperty(key)).thenReturn(value);
    }

    @After
    public void tearDownMockUtils() {
        SystemPropertyUtils.setInstance(m_originalUtil);
    }

}