package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import java.util.List;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.service.ModelService;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.service.ModelServiceDeployerException;

public interface NetconfStack {

    void setModelServices(List<ModelService> modelServices);

    /**
     * bean init method.
     * Do not use both init and constructor. Use only one. 
     */
    void init() throws ModelServiceDeployerException;

    /**
     * bean destroy method.
     */
    void destroy();

}
