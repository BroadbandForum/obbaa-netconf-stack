package org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.w3c.dom.Element;

import org.broadband_forum.obbaa.netconf.server.rpc.RequestType;

/**
 * Created by keshava on 11/23/15.
 */
public interface SchemaNodeConstraintParser {
    DataSchemaNode getDataSchemaNode();
    
    /**
     * Perform yang phase 1 validation 
     * @param DOM element to be validated
     * @Param type of request RPC/others
     * @throws ValidationException
     */
    void validate(Element dataNode, RequestType requestType) throws ValidationException;
}
