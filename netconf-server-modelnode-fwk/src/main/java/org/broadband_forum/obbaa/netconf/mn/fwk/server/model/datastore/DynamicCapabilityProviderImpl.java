package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;

/**
 * Created by keshava on 2/12/16.
 */
public class DynamicCapabilityProviderImpl implements DynamicCapabilityProvider {
    private final SchemaRegistry m_schemaRegistry;
    private Set<String> m_staticCaps = new HashSet<>();
    private Set<String> m_ignoredYangModules = new HashSet<>();

    public DynamicCapabilityProviderImpl(SchemaRegistry schemaRegistry) {
        this(schemaRegistry, Collections.<String>emptySet());
    }
    public DynamicCapabilityProviderImpl(SchemaRegistry schemaRegistry, Set<String> staticCapabilities) {
        this.m_schemaRegistry = schemaRegistry;
        m_staticCaps.addAll(staticCapabilities);
    }

    @Override
    public void addStaticCapabilities(Set<String> capabilities) {
        m_staticCaps.addAll(capabilities);
    }

    @Override
    public void removeStaticCapabilities(Set<String> capabilities) {
        m_staticCaps.removeAll(capabilities);
    }

    @Override
    public void clearStaticCapabilities() {
        m_staticCaps.clear();
    }

    @Override
    public void addIgnoredYangModules(Set<String> ignoreSet) {
        m_ignoredYangModules = ignoreSet;
    }

    @Override
    public Set<String> getCapabilities() {
        Set<String> caps = new HashSet<>();
        caps.addAll(m_staticCaps);
        caps.addAll(m_schemaRegistry.getModuleCapabilities(true));
        caps.removeAll(m_ignoredYangModules);
        return caps;
    }
}
