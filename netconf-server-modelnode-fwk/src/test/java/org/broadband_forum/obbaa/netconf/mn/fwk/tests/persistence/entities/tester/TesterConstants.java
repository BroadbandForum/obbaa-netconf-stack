package org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.tester;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public class TesterConstants {
    public static final String TESTER_NS = "http://netconfcentral.org/ns/tester";
    public static final String TESTER_LOCAL_NAME = "state-root";
    public static final String TESTER_REVISION = "2008-07-14";
    public static final SchemaPath STATE_ROOT_SCHEMA_PATH = SchemaPath.create(true, QName.create(TESTER_NS, TESTER_REVISION, TESTER_LOCAL_NAME));
    
    public static final SchemaPath STATE_CONTAINER_SCHEMA_PATH = SchemaPath.create(true, QName.create(TESTER_NS, TESTER_REVISION, "state-container"));


}
