package org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.toaster;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

/**
 * Created by pgorai on 4/4/16.
 */
public class ToasterConstants {
    public static final String TOASTER_NS = "http://netconfcentral.org/ns/toaster";
    public static final String TOASTER_LOCAL_NAME = "toaster";
    public static final String TOASTER_REVISION = "2009-11-20";
    public static final SchemaPath TOASTER_SCHEMA_PATH = SchemaPath.create(true, QName.create(TOASTER_NS, TOASTER_REVISION, TOASTER_LOCAL_NAME));
}
