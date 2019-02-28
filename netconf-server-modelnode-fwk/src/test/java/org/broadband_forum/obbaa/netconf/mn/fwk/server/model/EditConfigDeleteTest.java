package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.loadAsXml;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.responseToString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigElement;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigErrorOptions;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigTestOptions;
import org.broadband_forum.obbaa.netconf.api.messages.GetConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfFilter;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorSeverity;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorType;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.jukebox2.Jukebox;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;

public class EditConfigDeleteTest extends AbstractEditConfigTestSetup {

	private final static Logger LOGGER = Logger.getLogger(EditConfigDeleteTest.class);

	private NetconfClientInfo m_clientInfo = new NetconfClientInfo("unit-test", 1);
	
    @Before
	public void initServer() throws SchemaBuildException {
        super.setup();
		Jukebox.EMPTY_LIBRARY_NOT_ALLOWED = false;
	}


	@Test
	public void testDeleteLibrary(){
		EditConfigRequest request = new EditConfigRequest()
											.setTargetRunning()
											.setTestOption(EditConfigTestOptions.SET)
											.setErrorOption(EditConfigErrorOptions.STOP_ON_ERROR)
											.setConfigElement(new EditConfigElement()
																		.addConfigElementContent(loadAsXml("/delete-config-delete-library.xml")));
		request.setMessageId("1");
		NetConfResponse response = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request, response);
		//assert Ok response
        assertOkResponse(response);
		
		//do a get-config to be sure
		verifyGetConfig(null, "/empty-jukebox.xml");
	}
	
	/*@Test
    public void testDeleteLibraryRollsback(){
	    //make the delete fail
	    Jukebox.EMPTY_LIBRARY_NOT_ALLOWED = true;
        EditConfigRequest request = new EditConfigRequest()
                                            .setTargetRunning()
                                            .setTestOption(EditConfigTestOptions.SET)
                                            .setErrorOption(AxsEditConfigErrorOptions.ROLLBACK_ON_ERROR)
                                            .setConfigElement(new AxsEditConfigElement()
                                                                        .setConfigElementContent(loadAsXml("/delete-config-delete-library.xml")));
		request.setMessageId("1");
        NetConfResponse response = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request, response);
        //assert Ok response
        assertEquals(load("/not-ok-response.xml"), responseToString(response));
        
        //do a get-config to be sure
        verifyGetConfig(null, "/empty-library.xml");
    }*/

    @Test
    public void testDeleteLibraryGivesRpcError(){
        EditConfigRequest request = new EditConfigRequest()
                                            .setTargetRunning()
                                            .setTestOption(EditConfigTestOptions.SET)
                                            .setErrorOption(EditConfigErrorOptions.STOP_ON_ERROR)
                                            .setConfigElement(new EditConfigElement()
                                                                        .addConfigElementContent(loadAsXml("/delete-config-delete-library.xml")));
        request.setMessageId("1");
        NetConfResponse response = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request, response);
        //assert Ok response
        assertOkResponse(response);
        
        //do a get-config to be sure
        verifyGetConfig(null, "/empty-jukebox.xml");
        
        response = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request, response);
        //assert not Ok response
        assertFalse(response.isOk());
        assertEquals(1, response.getErrors().size());
        NetconfRpcError rpcError = response.getErrors().get(0);
        assertEquals(NetconfRpcErrorTag.DATA_MISSING, rpcError.getErrorTag());
        assertEquals(NetconfRpcErrorType.Application, rpcError.getErrorType());
        assertEquals(NetconfRpcErrorSeverity.Error, rpcError.getErrorSeverity());
        
    }
	
	@Test
	public void testDeleteAlbumCircus(){
		EditConfigRequest request = new EditConfigRequest()
											.setTargetRunning()
											.setTestOption(EditConfigTestOptions.SET)
											.setErrorOption(EditConfigErrorOptions.STOP_ON_ERROR)
											.setConfigElement(new EditConfigElement()
																		.addConfigElementContent(loadAsXml("/delete-album-circus.xml")));
		request.setMessageId("1");
		NetConfResponse response = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request, response);
		//assert Ok response
        assertOkResponse(response);

        //do a get-config to be sure
		verifyGetConfig(null, "/without-circus.xml");
		
		
	}

    private void assertOkResponse(NetConfResponse response) {
        try {
            TestUtil.assertXMLEquals("/ok-response.xml", response);
        } catch (SAXException | IOException e) {
            LOGGER.error("test comparison failed" , e);
            fail("test comparison failed" + e.getMessage());
        }
    }
	
	/*@Test
    public void testDeleteAlbumCircusRollsBack(){
	    Artist.ALBUMS_NOT_MODIFIABLE=true;
        EditConfigRequest request = new EditConfigRequest()
                                            .setTargetRunning()
                                            .setTestOption(EditConfigTestOptions.SET)
                                            .setErrorOption(AxsEditConfigErrorOptions.STOP_ON_ERROR)
                                            .setConfigElement(new AxsEditConfigElement()
                                                                        .setConfigElementContent(loadAsXml("/delete-album-circus.xml")));
		request.setMessageId("1");
        NetConfResponse response = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request, response);
        //assert Ok response
        assertEquals(load("/not-ok-response.xml"), responseToString(response));
        
        //do a get-config to be sure
        verifyGetConfig(null, "/without-circus.xml");
        
        
    }*/
	
	@Test
    public void testDeleteAlbumCircusGivesRpcError(){
        EditConfigRequest request = new EditConfigRequest()
                                            .setTargetRunning()
                                            .setTestOption(EditConfigTestOptions.SET)
                                            .setErrorOption(EditConfigErrorOptions.STOP_ON_ERROR)
                                            .setConfigElement(new EditConfigElement()
                                                                        .addConfigElementContent(loadAsXml("/delete-album-circus.xml")));
        request.setMessageId("1");
        NetConfResponse response = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request, response);
        //assert Ok response
        assertOkResponse(response);

        //do a get-config to be sure
        verifyGetConfig(null, "/without-circus.xml");
        
        response = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request, response);
        //assert not Ok response
        assertFalse(response.isOk());
        assertEquals(1, response.getErrors().size());
        NetconfRpcError rpcError = response.getErrors().get(0);
        assertEquals(NetconfRpcErrorTag.DATA_MISSING, rpcError.getErrorTag());
        assertEquals(NetconfRpcErrorType.Application, rpcError.getErrorType());
        assertEquals(NetconfRpcErrorSeverity.Error, rpcError.getErrorSeverity());
    }

    private void verifyGetConfig(String filterInput, String expectedOutput) {
		NetconfClientInfo client = new NetconfClientInfo("test", 1);

		GetConfigRequest request = new GetConfigRequest();
		request.setMessageId("1");
		request.setSource("running");
		if (filterInput != null) {
			NetconfFilter filter = new NetconfFilter();
			// we have two variants for the select node in here
			filter.addXmlFilter(loadAsXml(filterInput));
			request.setFilter(filter);
		}

		NetConfResponse response = new NetConfResponse();
		response.setMessageId("1");

		m_server.onGetConfig(client, request, response);

		LOGGER.info("Actual:: "+responseToString(response));
        try {
            TestUtil.assertXMLEquals(expectedOutput, response);
        } catch (SAXException | IOException e) {
            LOGGER.error("test comparison failed" , e);
            fail("test comparison failed" + e.getMessage());
        }
	}

}
