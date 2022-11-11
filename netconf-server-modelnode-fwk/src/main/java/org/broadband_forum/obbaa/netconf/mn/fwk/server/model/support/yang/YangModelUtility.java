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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.api.NetconfCapability;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;

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

    public static NetconfCapability getNetconfCapability(String capability, String... ignoredParams) {
        NetconfCapability netconfCapability = new NetconfCapability(capability);
        for (String ignoredParam : ignoredParams) {
            netconfCapability.getParameters().remove(ignoredParam);
        }
        return netconfCapability;
    }
	
}
