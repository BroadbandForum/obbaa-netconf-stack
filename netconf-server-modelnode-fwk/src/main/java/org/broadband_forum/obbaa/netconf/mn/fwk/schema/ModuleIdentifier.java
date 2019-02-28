package org.broadband_forum.obbaa.netconf.mn.fwk.schema;

import java.net.URI;
import java.util.Optional;

import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;

public interface ModuleIdentifier {
    
    QNameModule getQNameModule();
    String getName();
    URI getNamespace();
    Optional<Revision> getRevision();

}
