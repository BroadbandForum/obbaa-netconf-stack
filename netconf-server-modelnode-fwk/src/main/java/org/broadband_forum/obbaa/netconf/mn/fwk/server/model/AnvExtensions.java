package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import java.util.List;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil;

public enum AnvExtensions {

    MERGED_IN_PARENT_OBJECT(Constants.EXT_NS, "merged-in-parent-object", Constants.EXT_REV),
    IGNORE_DEFAULT(Constants.EXT_NS, "ignore-default", Constants.EXT_REV),
    TREAT_AS_RELATIVE_PATH(Constants.EXT_NS, "treat-as-relative-path", Constants.EXT_REV),
    MOUNT_POINT(Constants.SCHEMA_MOUNT_NS,"mount-point",Constants.SCHEMA_MOUNT_REV)
    ;
    
    public static class Constants {
        public static final String EXT_NS = "http://www.test-company.com/solutions/anv-yang-extensions";
        public static final Revision EXT_REV = Revision.of("2016-01-07");
        
        public static final String SCHEMA_MOUNT_NS = "urn:ietf:params:xml:ns:yang:ietf-yang-schema-mount";
        public static final Revision SCHEMA_MOUNT_REV = Revision.of("2017-10-09");
    }
    
    

    private final String m_namespace;
    private final Revision m_revision;
    private final String m_moduleName;

    private AnvExtensions(String nameSpace, String moduleName, Revision revision) {
        m_namespace = nameSpace;
        m_revision = revision;
        m_moduleName = moduleName;
    }

    private boolean isExtensionIn(ExtensionDefinition node) {
        if (this.equals(MOUNT_POINT) && !SchemaRegistryUtil.isMountPointEnabled()) {
            return false;
        }
        QName qName = node.getQName();
        String namespace = qName.getNamespace().toString();
        Revision revision = qName.getRevision().orElse(null);
        String moduleName = qName.getLocalName();
        return m_namespace.equals(namespace) && m_revision.equals(revision) && m_moduleName.equals(moduleName);
    }

    public boolean isExtensionIn(DataSchemaNode dataNode) {
        if (dataNode != null) {
            List<UnknownSchemaNode> unknownSchemaNodes = dataNode.getUnknownSchemaNodes();
            for (UnknownSchemaNode unKnownSchemaNode : unknownSchemaNodes) {
                ExtensionDefinition extDef = unKnownSchemaNode.getExtensionDefinition();
                if (isExtensionIn(extDef)) {
                    return true;
                }
            }
        }
        return false;
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
