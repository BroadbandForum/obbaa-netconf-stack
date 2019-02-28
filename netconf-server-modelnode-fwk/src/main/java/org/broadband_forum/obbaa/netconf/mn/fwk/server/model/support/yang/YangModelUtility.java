package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;

import org.broadband_forum.obbaa.netconf.api.NetconfCapability;

public class YangModelUtility {
	
	public static boolean isBaseIdentity(String identity, IdentitySchemaNode schemaNode){
		Set<IdentitySchemaNode> identitySchemaNodes = schemaNode.getBaseIdentities();
		if (identitySchemaNodes.isEmpty() && schemaNode.getQName().getLocalName().equals(identity)){
			return true;
		}else if (!identitySchemaNodes.isEmpty()){	
			for(IdentitySchemaNode identitySchemaNode : identitySchemaNodes){
				return isBaseIdentity(identity, identitySchemaNode);}
		}
		return false;
	}	
	
    public static boolean handleCheckServerCapabilityContainsAll(Set<String> serverCapabilities, List<String> requiredCaps) {
        List<NetconfCapability> listServerCapabilities = convertStringToCapabilities(serverCapabilities);
        List<NetconfCapability> listRequiredCaps = convertStringToCapabilities(new HashSet<String>(requiredCaps));
        return listServerCapabilities.containsAll(listRequiredCaps);
    }
    
    private static List<NetconfCapability> convertStringToCapabilities(Set<String> notificationCaps) {
        List<NetconfCapability> list = new ArrayList<>();
        if (notificationCaps != null) {
            for (String capabilityString : notificationCaps) {
                NetconfCapability netconfCapability = new NetconfCapability(capabilityString);
                list.add(netconfCapability);
            }
        }
        return list;
    }
	
}
