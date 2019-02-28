package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.spi;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.auth.spi.AuthorizationHandler;

public class DefaultAuthorizationHandler implements AuthorizationHandler {

	@Override
	public boolean isPermitted(Serializable netconfClientSessionId, String... permissions) throws Exception {
		return false;
	}
	
	@Override
	public boolean isPermittedAll(Serializable netconfClientSessionId, String... permissions) throws Exception {
		return false;
	}

    @Override
    public Set<String> getPermissions(String username) throws Exception {
        return new HashSet<String>();
    }

    @Override
    public boolean isPermitted(String username, String... permissions) throws Exception {
        return false;
    }

    @Override
    public boolean isPermittedAll(String username, String... permissions) throws Exception {
        return false;
    }
}
