package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.opendaylight.yangtools.yang.common.QName;

public interface EditLeafNode {

    String getName();

    String getValue();

    ConfigLeafAttribute getConfigLeafAttribute();

    String getNamespace();

    QName getQName();
    
    void setQName(QName qname);
    
    boolean isIdentityRefNode();
    
    void setIdentityRefNode(boolean value);

}