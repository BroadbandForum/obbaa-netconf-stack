/*
 * Copyright 2018 Broadband Forum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.service;

import java.util.List;

/**
 * ModelServiceDeployer is a deployer that deploys given ModelService into NCY Stack.
 * It is responsible for updating all the NCY internal registries to make the new ModelService available for service.
 *
 * @author gnanavek
 */
public interface ModelServiceDeployer {
    void addRootNodeHelpers(ModelService service);

    void deploy(List<ModelService> services) throws ModelServiceDeployerException;

    void undeploy(List<ModelService> services) throws ModelServiceDeployerException;

    void addDeployListener(DeployListener listener);

    void removeDeployListener(DeployListener listener);

    void postStartup(ModelService service) throws ModelServiceDeployerException;
}
