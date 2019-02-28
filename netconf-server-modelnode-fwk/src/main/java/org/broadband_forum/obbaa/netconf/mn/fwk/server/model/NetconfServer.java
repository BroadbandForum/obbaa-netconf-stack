package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import java.util.Map;

import org.broadband_forum.obbaa.netconf.api.messages.NetconfFilter;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerMessageListener;

public interface NetconfServer extends NetconfServerMessageListener {

    public void killSession(Integer currentSession, Integer sessionToKill);

    public void closeSession(Integer currentSession);

    public DataStore getDataStore(String storeName);

    public Map<Integer, NetConfSession> getNetconfSessions();

    public FilterNode getFilter(NetconfFilter filter);
}
