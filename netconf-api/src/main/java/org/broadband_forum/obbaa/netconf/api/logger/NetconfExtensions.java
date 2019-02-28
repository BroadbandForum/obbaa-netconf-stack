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

package org.broadband_forum.obbaa.netconf.api.logger;

import java.util.List;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;

public enum NetconfExtensions {
    
    IS_PASSWORD(Constants.EXT_NS, "is-password", Constants.EXT_REV)
    ,IS_SENSITIVE(Constants.EXT_NS, "is-sensitive", Constants.EXT_REV)
    ,IS_TREAT_AS_RELATIVE_PATH(Constants.EXT_NS, "treat-as-relative-path", Constants.EXT_REV)
    ;
    
    public static class Constants {
        public static final String EXT_NS = "http://www.test-company.com/solutions/anv-yang-extensions";
        public static final Revision EXT_REV = Revision.of("2016-01-07");
    }
    
    
    private final String m_namespace;
    private final Revision m_revision;
    private final String m_moduleName;
    
    private NetconfExtensions(String nameSpace, String moduleName, Revision revision){
        m_namespace = nameSpace;
        m_revision = revision;
        m_moduleName = moduleName;
    }
    
    private boolean isExtensionIn(ExtensionDefinition node){
        QName qName = node.getQName();
        String namespace = qName.getNamespace().toString();
        Revision revision = (qName.getRevision().isPresent() ? qName.getRevision().get() : null);
        String moduleName = qName.getLocalName();
        return m_namespace.equals(namespace) && m_revision.equals(revision)
                && m_moduleName.equals(moduleName);
    }
    
    public boolean isExtensionIn(DataSchemaNode dataNode){
        List<UnknownSchemaNode> unknownSchemaNodes = dataNode.getUnknownSchemaNodes();
        for ( UnknownSchemaNode unKnownSchemaNode : unknownSchemaNodes) {
            ExtensionDefinition extDef = unKnownSchemaNode.getExtensionDefinition();
            if ( isExtensionIn(extDef)){
                return true; 
            }
        }
        return false;
    }
    
    @SuppressWarnings("rawtypes")
    public boolean isExtensionIn(TypeDefinition definition){
        if (definition != null){
            List<UnknownSchemaNode> unknownSchemaNodes = definition.getUnknownSchemaNodes();
            for ( UnknownSchemaNode unKnownSchemaNode : unknownSchemaNodes) {
                ExtensionDefinition extDef = unKnownSchemaNode.getExtensionDefinition();
                if ( isExtensionIn(extDef)){
                    return true; 
                }
            }
        }
        
        return false;
    }

}
