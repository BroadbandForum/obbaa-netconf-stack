package org.broadband_forum.obbaa.netconf.samples.serverfwk.nbi;

import java.io.Serializable;
import java.security.PublicKey;

import org.broadband_forum.obbaa.netconf.api.server.auth.AuthenticationResult;
import org.broadband_forum.obbaa.netconf.api.server.auth.ClientAuthenticationInfo;
import org.broadband_forum.obbaa.netconf.api.server.auth.NetconfServerAuthenticationHandler;
import org.broadband_forum.obbaa.netconf.api.server.auth.NetconfServerSessionListener;


public class SampleAuthService implements NetconfServerAuthenticationHandler {

    private static final String USERNAME = "admin";
    private static final String PASSWORD = "password";
    /**
     * User name and password are set by Sprint boot in application-context.xml file.
     */
    private String m_strUserName = "admin";

    private String m_strPassword = "password";


    public void setUserName(String userName) {
        m_strUserName = userName;
    }

    public void setPassword(String password) {
        m_strPassword = password;
    }

    public SampleAuthService() {
        m_strUserName = USERNAME;
        m_strPassword = PASSWORD;
    }

    /**
     *  NBI SSH Netconf server authenticate function based on user name and password.
     */
    @Override
    public AuthenticationResult authenticate(ClientAuthenticationInfo clientAuthInfo) {

        if (m_strUserName.equals(clientAuthInfo.getUsername()) && m_strPassword.equals(clientAuthInfo.getPassword())) {
            LOGGER.info("Authentication is successful");
            return new AuthenticationResult(true, clientAuthInfo.getClientSessionId());
        }

        LOGGER.info("Authentication is failed");
        return AuthenticationResult.failedAuthResult();
    }

    /**
     *  NBI SSH Netconf server authenticate function based on public key.
     */
    @Override
    public AuthenticationResult authenticate(PublicKey pubKey) {

        //Currently does not support public key authentication
        return AuthenticationResult.failedAuthResult();
    }

    @Override
    public void logout(Serializable sshSessionId) {

    }

    @Override
    public void registerServerSessionListener(Serializable sessionId, NetconfServerSessionListener sessionListener) {

    }

    @Override
    public void unregisterServerSessionListener(Serializable sessionId) {

    }

    @Override
    public NetconfServerSessionListener getServerSessionListener(Serializable sessionId) {
        return null;
    }
}
