package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.service;

import java.util.List;

/**
 * ModelServiceDeployer is a deployer that deploys given ModelService into NCY Stack.
 * It is responsible for updating all the NCY internal registries to make the new ModelService available for service.
 *
 *
 */
public interface ModelServiceDeployer {
    void addRootNodeHelpers(ModelService service);

    void deploy(List<ModelService> services) throws ModelServiceDeployerException;

    void undeploy(List<ModelService> services) throws ModelServiceDeployerException;

    void addDeployListener(DeployListener listener);

    void removeDeployListener(DeployListener listener);

    void postStartup(ModelService service) throws ModelServiceDeployerException;
}
