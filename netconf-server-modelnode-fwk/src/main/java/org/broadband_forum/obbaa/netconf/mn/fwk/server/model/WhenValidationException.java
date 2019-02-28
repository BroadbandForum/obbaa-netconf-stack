package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;

public class WhenValidationException extends ValidationException {

    public WhenValidationException(NetconfRpcError error) {
        super(error);
    }

    public WhenValidationException(String message) {
        super(message);
    }

    private static final long serialVersionUID = -1640479875166838483L;

}
