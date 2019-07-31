package org.broadband_forum.obbaa.netconf.samples.jb.api;

import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public interface JBConstants {
    String JB_NS = "http://example.com/ns/example-jukebox";
    String JB_REVISION = "2014-07-03";

    SchemaPath JB_SP = SchemaPathBuilder.fromString("("+ JB_NS +"?revision="+ JB_REVISION +")jukebox");
    SchemaPath LIB_SP = SchemaPathBuilder.fromString("("+ JB_NS +"?revision="+ JB_REVISION +")jukebox,library");
}
