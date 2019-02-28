package org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DSExpressionValidator;

import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

/**
 * Created by keshava on 11/23/15.
 */
public interface ConstraintValidatorFactory {
    SchemaNodeConstraintParser getConstraintNodeValidator(DataSchemaNode dataSchemaNode, SchemaRegistry schemaRegistry,
            DSExpressionValidator expValidator);
    
    void clearCache();
}
