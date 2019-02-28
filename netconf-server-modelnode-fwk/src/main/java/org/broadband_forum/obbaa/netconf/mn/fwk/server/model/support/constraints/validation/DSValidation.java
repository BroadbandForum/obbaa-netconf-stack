package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation;

import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;

public interface DSValidation {

    public Boolean evaluate(ModelNode parentNode, DataSchemaNode child);
}
