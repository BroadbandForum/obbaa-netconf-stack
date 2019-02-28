package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import java.util.List;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.service.ModelService;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.service.ModelServiceDeployer;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.service.ModelServiceDeployerException;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;

/**
 * A class that helps deployment of a simple app on NCY stack.
 * Created by keshava on 4/26/16.
 */
public class SimpleNcyApp implements NetconfStack {
    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(SimpleNcyApp.class, LogAppNames.NETCONF_STACK);
    public static final String SIMPLE_NCY_APP = "SimpleNcyApp";
    ModelServiceDeployer m_modelServiceDeployer;
    List<ModelService> m_modelServices;
    private String m_appName = SIMPLE_NCY_APP;

    public ModelServiceDeployer getModelServiceDeployer() {
        return m_modelServiceDeployer;
    }

    public void setModelServiceDeployer(ModelServiceDeployer modelServiceDeployer) {
        this.m_modelServiceDeployer = modelServiceDeployer;
    }

    public List<ModelService> getModelServices() {
        return m_modelServices;
    }

    @Override
    public void setModelServices(List<ModelService> modelServices) {
        this.m_modelServices = modelServices;
    }

    @Override
    public void init() throws ModelServiceDeployerException {
        try {
            LOGGER.info("deploying app {} ", m_appName);
            getModelServiceDeployer().deploy(m_modelServices);
            LOGGER.info("app {} deployed", m_appName);
        } catch (ModelServiceDeployerException e) {
            LOGGER.error("Error while deploying: "+m_appName, e);
            throw e;
        }
    }

    public SimpleNcyApp() {
    	
    }
    
    public SimpleNcyApp(List<ModelService> modelServices, 
    		ModelServiceDeployer modelServiceDeployer,
    		String appName) throws ModelServiceDeployerException{
	  m_modelServices = modelServices;
	  m_modelServiceDeployer = modelServiceDeployer;
	  m_appName = appName;
      init();
    }

    @Override
    public void destroy(){
        try {
            LOGGER.info("un-deploying app {} ", m_appName);
            getModelServiceDeployer().undeploy(m_modelServices);
            LOGGER.info("app {} un-deployed", m_appName);
        } catch (ModelServiceDeployerException e) {
            LOGGER.error( "Error while un-deploying :"+ m_appName, e);
        }
    }

    public String getAppName() {
        return m_appName;
    }

    public void setAppName(String appName) {
        m_appName = appName;
    }
}
