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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import java.util.List;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;

public enum AnvExtensions {

    MERGED_IN_PARENT_OBJECT(Constants.EXT_NS, "merged-in-parent-object", Constants.EXT_REV),
    IGNORE_DEFAULT(Constants.EXT_NS, "ignore-default", Constants.EXT_REV),
    BIG_LIST(Constants.EXT_NS,"big-list", Constants.EXT_REV),
    REQUIRES_PERMISSION(Constants.NC_STACK_EXTN_NS,"requires-permission", Constants.NC_STACK_EXTN_REV),
    VALIDATION_HINTS(Constants.NC_STACK_EXTN_NS,"validation-hints",Constants.NC_STACK_EXTN_REV),
    VALIDATION_HINTS_ON_NODE(Constants.NC_STACK_EXTN_NS,"validation-hints-on-node",Constants.NC_STACK_EXTN_REV),
    REFERRING_NODE_IS_UNDER_CHANGED_NODE(Constants.NC_STACK_EXTN_NS,"referring-node-is-under-changed-node",Constants.NC_STACK_EXTN_REV),
    TREAT_AS_RELATIVE_PATH(Constants.EXT_NS, "treat-as-relative-path", Constants.EXT_REV),
    MOUNT_POINT(Constants.SCHEMA_MOUNT_NS,"mount-point",Constants.SCHEMA_MOUNT_REV),
    EXPRESSION(Constants.NC_STACK_EXTN_NS,"expression",Constants.NC_STACK_EXTN_REV),
    HINTS(Constants.NC_STACK_EXTN_NS,"hints",Constants.NC_STACK_EXTN_REV),
    DESCRIPTION(Constants.NC_STACK_EXTN_NS,"description",Constants.NC_STACK_EXTN_REV),
    UAL_DISABLED(Constants.NC_STACK_EXTN_NS,"ual-disabled", Constants.NC_STACK_EXTN_REV),
    UAL_APPLICATION(Constants.NC_STACK_EXTN_NS,"ual-application", Constants.NC_STACK_EXTN_REV);

    public static class Constants {
        public static final String EXT_NS = "http://www.test-company.com/solutions/anv-yang-extensions";
        public static final Revision EXT_REV = Revision.of("2016-01-07");
        
        public static final String SCHEMA_MOUNT_NS = "urn:ietf:params:xml:ns:yang:ietf-yang-schema-mount";
        public static final Revision SCHEMA_MOUNT_REV = Revision.of("2017-10-09");
        
        public static final String NC_STACK_EXTN_NS = "http://www.test-company.com/solutions/nc-stack-extensions";
        public static final Revision NC_STACK_EXTN_REV = Revision.of("2020-09-09");

    }
    
    private final String m_namespace;
    private final Revision m_revision;
    private final String m_moduleName;

    private AnvExtensions(String nameSpace, String moduleName, Revision revision) {
        m_namespace = nameSpace;
        m_revision = revision;
        m_moduleName = moduleName;
    }

    public boolean isExtensionIn(ExtensionDefinition node) {
        QName qName = node.getQName();
        String namespace = qName.getNamespace().toString();
        Revision revision = qName.getRevision().orElse(null);
        String moduleName = qName.getLocalName();
        return m_namespace.equals(namespace) && m_revision.equals(revision) && m_moduleName.equals(moduleName);
    }

    public boolean isExtensionIn(DataSchemaNode dataNode) {
        return getExtensionDefinition(dataNode) != null;
    }

    public UnknownSchemaNode getExtensionDefinition(SchemaNode dataNode) {
        if (dataNode != null) {
            List<UnknownSchemaNode> unknownSchemaNodes = dataNode.getUnknownSchemaNodes();
            for (UnknownSchemaNode unKnownSchemaNode : unknownSchemaNodes) {
                ExtensionDefinition extDef = unKnownSchemaNode.getExtensionDefinition();
                if (isExtensionIn(extDef)) {
                    return unKnownSchemaNode;
                }
            }
        }
        return null;
    }
    
    public String getName(){
        return m_moduleName;
    }
    
    public String getExtensionNamespace(){
        return m_namespace;
    }
    
    public Revision getRevision(){
        return m_revision;
    }
    
    public static String getExtensionPrefix() {
        return "anvext";
    }
}
