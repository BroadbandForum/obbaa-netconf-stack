package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.yang;

import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.load;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.loadAsXml;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.responseToString;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.sendEditConfig;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.verifyGetConfig;
import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;

public class YangEditConfigRemoveTest extends AbstractYangValidationTestSetup{

    @Before
    public void initServer() throws SchemaBuildException, ModelNodeInitException {
       super.setup();
    }

    @Test
    public void testRemoveLibrary() throws SAXException, IOException {
    	//send editconfig to remove libarary
        NetConfResponse response = sendEditConfig(m_server, m_clientInfo, loadAsXml("/remove-config-delete-library.xml"), "1");

        // assert Ok response
        assertEquals(load("/ok-response.xml"), responseToString(response));

        // do a get-config to be sure
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/empty-jukebox.xml", "1");
        
        //again send editconfig to remove library that already removed
        response = sendEditConfig(m_server, m_clientInfo, loadAsXml("/remove-config-delete-library.xml"), "1");
       
        // assert Ok response
        assertEquals(load("/ok-response.xml"), responseToString(response));

        // do a get-config to be sure
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/empty-jukebox.xml", "1");
    }

    @Test
    public void testRemoveAlbum() throws SAXException, IOException {
        //send editconfig to remove libarary
        NetConfResponse response = sendEditConfig(m_server, m_clientInfo, loadAsXml("/remove-album-circus.xml"), "1");

        // assert Ok response
        assertEquals(load("/ok-response.xml"), responseToString(response));

        // do a get-config to be sure
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/without-circus-yang.xml", "1");
        
        //again remove library that already removed
        response = sendEditConfig(m_server, m_clientInfo, loadAsXml("/remove-album-circus.xml"), "1");
        // assert Ok response
        assertEquals(load("/ok-response.xml"), responseToString(response));

        // do a get-config to be sure
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/without-circus-yang.xml", "1");
    }

}
