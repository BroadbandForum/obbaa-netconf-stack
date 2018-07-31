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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.YangModelUtility;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleIdentifier;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;

public class YangModelUtilityTest {

    @Test
    public void testIsBaseIdentity() throws SchemaBuildException {
        SchemaRegistry schemaRegistry = new SchemaRegistryImpl(TestUtil.getJukeBoxYangs(), new NoLockService());
        Set<ModuleIdentifier> moduleIds = schemaRegistry.getAllModuleIdentifiers();
        Module module = null;
        for (ModuleIdentifier moduleId : moduleIds) {
            if (moduleId.getName().equals("example-jukebox")) {
                module = schemaRegistry.getModule(moduleId.getName(), moduleId.getRevision());
                break;
            }
        }
        Set<IdentitySchemaNode> identities = module.getIdentities();
        for (IdentitySchemaNode id : identities) {
            assertTrue(YangModelUtility.isBaseIdentity("genre", id));
            assertFalse(YangModelUtility.isBaseIdentity("Genre", id));
        }
    }

    @Test
    public void testHandleCheckServerCapabilityContainsAll() {
        Set<String> caps = new HashSet<>();
        caps.add("urn:ietf:params:netconf:capability:interleave:1.0");
        caps.add("urn:ietf:params:xml:ns:netmod:notification?revision=2008-07-14&module=nc-notifications");
        {
            List<String> capsRequired = Arrays.asList("urn:ietf:params:netconf:capability:interleave:1.0",
                    "urn:ietf:params:xml:ns:netmod:notification?revision=2008-07-14&module=nc-notifications");
            assertTrue(YangModelUtility.handleCheckServerCapabilityContainsAll(caps, capsRequired));
        }
        {
            List<String> capsRequired = Arrays.asList("urn:ietf:params:netconf:capability:interleave:1.0",
                    "urn:ietf:params:xml:ns:netmod:notification?module=nc-notifications&revision=2008-07-14");
            assertTrue(YangModelUtility.handleCheckServerCapabilityContainsAll(caps, capsRequired));
        }
        {
            List<String> capsRequired = Arrays.asList("urn:ietf:params:netconf:capability:interleave:1.0",
                    "urn:ietf:params:xml:ns:netmod:notification?module=nc-notifications&revision=2008-08-16");
            assertFalse(YangModelUtility.handleCheckServerCapabilityContainsAll(caps, capsRequired));
        }

    }

}
