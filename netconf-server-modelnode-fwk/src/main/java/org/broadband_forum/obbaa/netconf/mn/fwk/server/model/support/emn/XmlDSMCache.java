package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn;

import java.util.List;

import org.opendaylight.yangtools.yang.model.api.SchemaPath;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;

public interface XmlDSMCache {
    void putInCache(SchemaPath nodeType, ModelNodeId nodeId, XmlModelNodeImpl node);
    XmlModelNodeImpl getFromCache(SchemaPath nodeType, ModelNodeId nodeId);
    void markNodeToBeUpdated(SchemaPath nodeType, ModelNodeId nodeId);
    List<XmlModelNodeImpl> getNodesToBeUpdated();
    void removeFromCache(ModelNodeId nodeId);
}
