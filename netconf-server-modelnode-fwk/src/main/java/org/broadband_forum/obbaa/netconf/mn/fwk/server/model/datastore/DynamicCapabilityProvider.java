package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore;

import org.broadband_forum.obbaa.netconf.api.server.ServerCapabilityProvider;

import java.util.Set;

/**
 * A CapabilityProvider for NCY Stack.
 * Following are the responsibilities
 * 1. Serve NC Server with capabilities derived from static capabilities like base:1.0/base:1.1 and the list yang modules currently loaded in the SchemaRegistry
 * 2. Allow caching of the capabilities there by reducing calls to SchemaRegistry. - TBD
 * The cache is kept upto date by listening to bundle deploy events and loading the capabilities everytime such an event happens.
 *
 * Created by keshava on 2/12/16.
 */
public interface DynamicCapabilityProvider extends ServerCapabilityProvider {
    public void addStaticCapabilities(Set<String> capabilities);
    public void removeStaticCapabilities(Set<String> capabilities);
    public void clearStaticCapabilities();
    void addIgnoredYangModules(Set<String> ignoreSet);
}
