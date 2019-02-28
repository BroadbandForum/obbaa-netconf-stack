package org.broadband_forum.obbaa.netconf.mn.fwk.schema;

import org.opendaylight.yangtools.yang.model.api.SchemaNode;

/**
 * Created by keshava on 11/19/15.
 */
public interface SchemaNodeVisitor {
    void visit(SchemaNode node);
}
