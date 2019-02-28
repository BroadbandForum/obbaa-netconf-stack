package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SimpleNcyApp;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.service.ModelService;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.service.ModelServiceDeployer;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.service.ModelServiceDeployerException;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Created by keshava on 4/26/16.
 */
public class SimpleNcyAppTest {
    private SimpleNcyApp m_app;
    private ModelServiceDeployer m_deployer;
    private List<ModelService> m_modelServices;

    @Before
    public void setUp(){
        m_app = new SimpleNcyApp();
        m_app.setAppName("UTApp");
        m_modelServices = new ArrayList<>();
        m_modelServices.add(mock(ModelService.class));
        m_modelServices.add(mock(ModelService.class));
        m_app.setModelServices(m_modelServices);
        m_deployer = mock(ModelServiceDeployer.class);
        m_app.setModelServiceDeployer(m_deployer);
    }

    @Test
    public void testDeployOnInit() throws ModelServiceDeployerException {
        m_app.init();
        verify(m_deployer).deploy(m_modelServices);
    }
    @Test
    public void testUndeployWhenDeployFails() throws ModelServiceDeployerException {

        try {
            doThrow(new ModelServiceDeployerException()).when(m_deployer).deploy(anyList());
            m_app.init();
            fail("Expected exception not thrown");
        } catch (ModelServiceDeployerException e) {
            //expected exception
        }
        verify(m_deployer).deploy(m_modelServices);
    }



    @Test
    public void testUndeployOnDestroy() throws ModelServiceDeployerException {
        m_app.destroy();
        verify(m_deployer).undeploy(m_modelServices);
    }
    
    @Test
    public void testNcyAppWithParameters() throws ModelServiceDeployerException {
    	SimpleNcyApp app = new SimpleNcyApp(m_modelServices, m_deployer, "UTApp");
        verify(m_deployer).deploy(m_modelServices);
    }
}
