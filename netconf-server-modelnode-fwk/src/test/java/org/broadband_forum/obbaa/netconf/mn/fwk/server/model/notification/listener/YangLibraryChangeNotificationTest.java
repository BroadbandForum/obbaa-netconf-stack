package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.notification.listener;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.notification.YangLibraryChangeNotification;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;

public class YangLibraryChangeNotificationTest {
    private YangLibraryChangeNotification m_notification;
    
 @Before
 public void setUp() throws SchemaBuildException {
    m_notification = new YangLibraryChangeNotification("yanglib", "aabb");
 }
 
 @Test
 public void testYangLibraryChangeNotificationElement() throws Exception{
    String notificationStr = "<yanglib:yang-library-change xmlns:yanglib=\"urn:ietf:params:xml:ns:yang:ietf-yang-library\">" +
                               "<yanglib:module-set-id>aabb</yanglib:module-set-id>" +
                             "</yanglib:yang-library-change>";
    
    Document doc = DocumentUtils.stringToDocument(notificationStr);
    TestUtil.assertXMLEquals(doc.getDocumentElement(), m_notification.getNotificationElement());
 }
}
