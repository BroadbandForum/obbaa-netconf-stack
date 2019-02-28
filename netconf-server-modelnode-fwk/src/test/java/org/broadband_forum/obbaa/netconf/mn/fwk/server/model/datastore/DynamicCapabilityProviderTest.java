package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;

public class DynamicCapabilityProviderTest {

    private static final Logger LOGGER = Logger.getLogger(DynamicCapabilityProviderTest.class);
    private SchemaRegistry m_schemaRegistry = null;
    private DynamicCapabilityProvider m_dynamicCapabilitiyProvider = null;

    @Before
    public void setUp() throws Exception {
        m_schemaRegistry = new SchemaRegistryImpl(TestUtil.getJukeBoxYangs(), Collections.emptySet(), Collections.emptyMap(), false, new NoLockService());
        m_dynamicCapabilitiyProvider = new DynamicCapabilityProviderImpl(m_schemaRegistry);
    }
    @Test
    public void testGetCapabilities(){
        Set<String> caps = new HashSet<>();
        caps.addAll(getModuleCaps());
        assertEquals(caps, m_dynamicCapabilitiyProvider.getCapabilities());

        caps.addAll(getBaseCaps());
        m_dynamicCapabilitiyProvider.addStaticCapabilities(getBaseCaps());
        assertEquals(caps, m_dynamicCapabilitiyProvider.getCapabilities());

        caps.removeAll(getBaseCaps());
        m_dynamicCapabilitiyProvider.clearStaticCapabilities();
        assertEquals(caps, m_dynamicCapabilitiyProvider.getCapabilities());
    }

    @Test
    public void testRemoveAndClearStaticCapabilities(){
        Set<String> caps = new HashSet<>();
        caps.addAll(getBaseCaps());
        caps.addAll(getModuleCaps());
        m_dynamicCapabilitiyProvider.addStaticCapabilities(getBaseCaps());
        assertEquals(caps, m_dynamicCapabilitiyProvider.getCapabilities());

        m_dynamicCapabilitiyProvider.removeStaticCapabilities(new HashSet<String>(Arrays.asList
                (NetconfResources.NETCONF_BASE_CAP_1_1)));
        Set<String> moduleCaps = getModuleCaps();
        moduleCaps.add(NetconfResources.NETCONF_BASE_CAP_1_0);
        assertEquals(moduleCaps, m_dynamicCapabilitiyProvider.getCapabilities());

        m_dynamicCapabilitiyProvider.clearStaticCapabilities();
        assertEquals(getModuleCaps(), m_dynamicCapabilitiyProvider.getCapabilities());
    }

    @Test
    public void testAddIgnoredYangModules(){
        Set<String> caps = new HashSet<>();
        caps.addAll(getBaseCaps());
        caps.addAll(getModuleCaps());
        m_dynamicCapabilitiyProvider.addStaticCapabilities(getBaseCaps());
        Set<String> set = new HashSet<>();
        set.add("urn:ietf:params:xml:ns:yang:ietf-restconf?module=ietf-restconf&revision=2014-07-03");
        m_dynamicCapabilitiyProvider.addIgnoredYangModules(set);

        Set<String> expectedCaps = getModuleCaps();
        expectedCaps.addAll(getBaseCaps());
        expectedCaps.remove("urn:ietf:params:xml:ns:yang:ietf-restconf?module=ietf-restconf&revision=2014-07-03");
        assertEquals(expectedCaps, m_dynamicCapabilitiyProvider.getCapabilities());
    }

    @Test
    public void multiThreadedTest() throws InterruptedException {
        Set<String> caps = new HashSet<>();
        caps.addAll(getBaseCaps());
        caps.addAll(getModuleCaps());
        m_dynamicCapabilitiyProvider.addStaticCapabilities(getBaseCaps());
        Set<String> set = new HashSet<>();
        set.add("urn:ietf:params:xml:ns:yang:ietf-restconf?module=ietf-restconf&revision=2014-07-03");
        m_dynamicCapabilitiyProvider.addIgnoredYangModules(set);

        ExecutorService service = Executors.newFixedThreadPool(100);
        for(int i=0 ;i <1000 ; i ++){
            service.execute(() -> {
                try{
                    Set<String> caps1 = m_dynamicCapabilitiyProvider.getCapabilities();
                    LOGGER.info(caps1);
                }catch (Exception e ){
                    LOGGER.error("Error ", e);
                }

            });
        }

        service.shutdown();
        service.awaitTermination(100, TimeUnit.SECONDS);
    }


    private Set<String> getModuleCaps() {
        return m_schemaRegistry.getModuleCapabilities(false);
    }

    private Set<String> getBaseCaps() {
        HashSet<String> baseCaps = new HashSet<String>();
        baseCaps.add(NetconfResources.NETCONF_BASE_CAP_1_0);
        baseCaps.add(NetconfResources.NETCONF_BASE_CAP_1_1);
        return baseCaps;
    }
}
