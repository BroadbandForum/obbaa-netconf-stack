package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore;

import java.util.HashSet;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.api.utils.SystemPropertyUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;


public class CapabilityProviderFactory {

    public static DynamicCapabilityProvider getProvider(SchemaRegistry schemaRegistry, Set<String> staticCapabilities){
        Set<String> caps = getStaticCapabilities(staticCapabilities);
        return new DynamicCapabilityProviderImpl(schemaRegistry, caps);
    }

    public static Set<String> getStaticCapabilities(Set<String> staticCapabilities) {
        Set<String> caps = new HashSet<>();
        caps.addAll(staticCapabilities);
        String capsFromYml = SystemPropertyUtils.getInstance().getFromEnvOrSysProperty(NetconfResources.CAPABILITY_TYPE, NetconfResources.DEFAULT_VALUE_1_1);
        if (capsFromYml.equals("1.0")) {
            caps.add("urn:ietf:params:netconf:base:1.0");
            caps.remove("urn:ietf:params:netconf:base:1.1");
        } else if (capsFromYml.equals("1.1")) {
            caps.add("urn:ietf:params:netconf:base:1.0");
            caps.add("urn:ietf:params:netconf:base:1.1");
        }
        return caps;
    }

}
