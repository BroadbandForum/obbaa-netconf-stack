package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.yang;

import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.assertXMLEquals;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.loadAsXml;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.sendEditConfig;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.verifyGetConfig;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;

public class YangEditConfigMergeTest extends AbstractYangValidationTestSetup{
	
	@Before
	public void initServer() throws SchemaBuildException, ModelNodeInitException {	
	    super.setup();
	}

	@Test
	public void testChangeAlbumYear() throws SAXException, IOException {
		//send edit config to merge album 
		NetConfResponse response = sendEditConfig(m_server, m_clientInfo, loadAsXml("/editconfig-simple.xml"), "1");
		//assert Ok response
		assertXMLEquals("/ok-response.xml", response);

		//do a get-config to be sure
		verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/jukebox-year-editconfig-expected.xml", "1");
	}

	@Test
	public void testMergeNewSong() throws SAXException, IOException {
		//send edit config to merge album 
		NetConfResponse response = sendEditConfig(m_server, m_clientInfo, loadAsXml("/editconfig-merge-songs.xml"), "1");
		//assert Ok response
		assertXMLEquals("/ok-response.xml", response);

		//do a get-config to be sure
		verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/jukebox-year-editconfig-merge-songs-expected.xml", "1");
	}

}
