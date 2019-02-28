package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import java.sql.Timestamp;
import java.util.Set;
import java.util.TreeSet;

public class NetConfSession {

	private Set<String> m_lockedStores = new TreeSet<>();

    private String m_userName;
    private String m_sourceHost;
    private Transport m_transport;
    private Timestamp m_loginTime;

    public NetConfSession() {
    }

    public NetConfSession(String userName, String sourceHost, Transport transport, Timestamp loginTime) {
        this.m_userName = userName;
        this.m_sourceHost = sourceHost;
        this.m_transport = transport;
        this.m_loginTime = loginTime;
    }

    public String getUserName() {
        return m_userName;
    }

    public String getSourceHost() {
        return m_sourceHost;
    }

    public Transport getTransport() {
        return m_transport;
    }

    public Timestamp getloginTime() {
        return m_loginTime;
    }
     
    public NetConfSession addLockedStore(String storeName) {
        this.m_lockedStores.add(storeName);
        return this;
    }

    public NetConfSession removeLockedStore(String storeName) {
        this.m_lockedStores.remove(storeName);
        return this;
    }

    public Set<String> getLockedStores() {
        return m_lockedStores;
    }
}
