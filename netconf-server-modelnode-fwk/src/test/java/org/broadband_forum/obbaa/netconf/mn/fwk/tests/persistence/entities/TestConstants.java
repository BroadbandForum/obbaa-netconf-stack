package org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities;

import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn.CONTAINER;

import org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn;

/**
 * Created by keshava on 2/18/16.
 */
public class TestConstants {
    public static final ModelNodeId EMPTY_NODE_ID = new ModelNodeId();
    public static final ModelNodeId CERT_MGMT_NODE_ID = new ModelNodeId()
            .addRdn(new ModelNodeRdn(CONTAINER, JukeboxConstants.CERT_NS, JukeboxConstants.CERT_MGMT_LOCAL_NAME));
    public static final ModelNodeId PMA_CERT_NODE_ID = new ModelNodeId()
            .addRdn(new ModelNodeRdn(CONTAINER, JukeboxConstants.CERT_NS, JukeboxConstants.CERT_MGMT_LOCAL_NAME))
            .addRdn(new ModelNodeRdn(CONTAINER, JukeboxConstants.CERT_NS, JukeboxConstants.PMA_CERTS_LOCAL_NAME));
    public static final ModelNodeId CA_CERT_NODE_ID = new ModelNodeId()
            .addRdn(new ModelNodeRdn(CONTAINER, JukeboxConstants.CERT_NS, JukeboxConstants.CERT_MGMT_LOCAL_NAME))
            .addRdn(new ModelNodeRdn(CONTAINER, JukeboxConstants.CERT_NS,JukeboxConstants.TRUSTED_CA_CERTS_LOCAL_NAME));
    public static final ModelNodeId HOME_ADDRESS_NODE_ID = new ModelNodeId()
            .addRdn(new ModelNodeRdn(CONTAINER, JukeboxConstants.ADDR_NS,JukeboxConstants.HOME_ADDRESS_LOCAL_NAME))
            .addRdn(new ModelNodeRdn(JukeboxConstants.ADDRESS_NAME_Q_NAME, JukeboxConstants.HOME_ADDRESS));
    public static final ModelNodeId OFFICE_ADDRESS_NODE_ID = new ModelNodeId()
            .addRdn(new ModelNodeRdn(CONTAINER, JukeboxConstants .ADDR_NS, JukeboxConstants.OFFICE_ADDRESS_LOCAL_NAME))
            .addRdn(new ModelNodeRdn(JukeboxConstants.ADDRESS_NAME_Q_NAME, JukeboxConstants.OFFICE_ADDRESS));
    
    public static final String DEVICE_HOLDER_NAME = "device-holder-name";
    public static final String DEVICE_HOLDER_NAMESPACE = "http://www.test-company.com/solutions/anv-device-holders";
    public static final String ANV_NAMESPACE = "http://www.test-company.com/solutions/anv";
    public static final String DEVICE_ID_LOCAL_NAME = "device-id";
    public static final String IP_ADDRESS = "ip-address";
    public static final String REVISION = "2015-07-14";
    public static final String IP_PORT = "ip-port";
    public static final String DEVICE_HOLDER = "device-holder";
    public static final String DEVICE = "device";
    public static final String HARDWARE_TYPE = "hardware-type";
    public static final String INTERFACE_VERSION = "interface-version";
    public static final String DEVICE_ID = "device-id";
}
