package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.w3c.dom.Node;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaMountRegistryProvider;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;

public class MountProviderInfo {

    private SchemaMountRegistryProvider m_provder = null;
    private Node m_mountedXmlNodeFromRequest = null;
    private DataSchemaNode m_mountedDataSchemaNode = null;
    private SchemaRegistry m_mountedRegistry = null;
    
    public MountProviderInfo(SchemaMountRegistryProvider provder, Node mountedXmlNodeFromRequest, DataSchemaNode mountedDataSchemaNode) {
        m_provder = provder;
        m_mountedXmlNodeFromRequest = mountedXmlNodeFromRequest;
        m_mountedDataSchemaNode = mountedDataSchemaNode;
        m_mountedRegistry = provder.getSchemaRegistry(null, mountedXmlNodeFromRequest);;
    }

    public SchemaMountRegistryProvider getProvder() {
        return m_provder;
    }

    public Node getMountedXmlNodeFromRequest() {
        return m_mountedXmlNodeFromRequest;
    }

    public DataSchemaNode getMountedDataSchemaNode() {
        return m_mountedDataSchemaNode;
    }
    
    public SchemaRegistry getMountedRegistry(){
        return m_mountedRegistry;
    }
    
}
