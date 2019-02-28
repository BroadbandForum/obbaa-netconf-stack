package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterMatchNode;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;

public class FilterMatchNodeTest {
    @Test
    public void testIsSameQName(){
        FilterMatchNode matchNode = new FilterMatchNode("device", "http://ns", "ONT1");
        assertTrue(matchNode.isSameQName(QName.create("http://ns", "device")));
        assertFalse(matchNode.isSameQName(QName.create("http://ns1", "device")));
        assertFalse(matchNode.isSameQName(QName.create("http://ns1", "device2")));
        assertFalse(matchNode.isSameQName(QName.create("http://ns", "device2")));

    }
}
