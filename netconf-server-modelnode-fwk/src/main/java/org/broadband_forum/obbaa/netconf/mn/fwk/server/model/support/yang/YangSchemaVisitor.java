package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

public interface YangSchemaVisitor {

	public void visit(DataSchemaNode schemaNode, QName qName, ModelNodeId modelNodeId); 
}
