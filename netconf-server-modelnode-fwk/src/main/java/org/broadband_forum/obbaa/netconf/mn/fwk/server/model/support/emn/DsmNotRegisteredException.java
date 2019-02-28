package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.DataStoreException;

public class DsmNotRegisteredException extends DataStoreException {

    public DsmNotRegisteredException(String message) {
        super(message);
    }

}
