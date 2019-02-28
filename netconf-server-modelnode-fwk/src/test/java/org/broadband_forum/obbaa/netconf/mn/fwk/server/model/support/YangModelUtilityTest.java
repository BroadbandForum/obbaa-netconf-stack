package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.YangModelUtility;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.ModuleIdentifier;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;

import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;

public class YangModelUtilityTest {
	
	@Test
	public void testIsBaseIdentity() throws SchemaBuildException{
		SchemaRegistry schemaRegistry = new SchemaRegistryImpl(TestUtil.getJukeBoxYangs(), Collections.emptySet(), Collections.emptyMap(), new NoLockService());
		Set<ModuleIdentifier> moduleIds = schemaRegistry.getAllModuleIdentifiers();
		Module module =null;
		for (ModuleIdentifier moduleId:moduleIds){
			if (moduleId.getName().equals("example-jukebox")){
				module = schemaRegistry.getModule(moduleId.getName(), moduleId.getRevision().orElse(null)).orElse(null);
				break;
			}
		}
		Set<IdentitySchemaNode> identities = module.getIdentities();
		for (IdentitySchemaNode id:identities){
			if(!(id.getQName().getLocalName().equals("genre2"))) {
				assertTrue(YangModelUtility.isBaseIdentity("genre", id));
				assertFalse(YangModelUtility.isBaseIdentity("Genre",id));
			}
		}
	}
	@Test
    public void testHandleCheckServerCapabilityContainsAll(){
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
