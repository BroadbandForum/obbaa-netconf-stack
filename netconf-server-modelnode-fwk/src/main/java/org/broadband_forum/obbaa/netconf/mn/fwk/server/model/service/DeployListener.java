package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.service;

public interface DeployListener {

    void preDeploy();
    void postDeploy();
    
    void preUndeploy();
    void postUndeploy();
}
