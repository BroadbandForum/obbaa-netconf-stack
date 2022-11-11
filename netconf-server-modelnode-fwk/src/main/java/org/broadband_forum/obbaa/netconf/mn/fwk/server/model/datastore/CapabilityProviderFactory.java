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
