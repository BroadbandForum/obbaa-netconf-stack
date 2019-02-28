package org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util;

import org.w3c.dom.Node;


public class NamespaceUtil {

    public static String getAttributeNameSpace(Node child, String prefix) {
        if(child == null) {
            return null;
        }
        return child.lookupNamespaceURI(prefix);
    }
}
