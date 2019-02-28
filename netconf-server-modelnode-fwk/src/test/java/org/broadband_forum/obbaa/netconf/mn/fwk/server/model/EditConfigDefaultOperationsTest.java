package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.createJukeBoxWithEmptyLibrary;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.load;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.loadAsXml;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.responseToString;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.verifyGetConfig;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigDefaultOperations;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigElement;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigErrorOptions;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigOperations;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigTestOptions;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfFilter;
import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregatorImpl;

import org.broadband_forum.obbaa.netconf.server.rpc.RpcPayloadConstraintParser;

/**
 * Created by sgs on 8/25/15.
 */
public class EditConfigDefaultOperationsTest extends AbstractEditConfigTestSetup {

    public static final String GET_AFTER_CREATE_ALBUM = "/editconfigdefaultoperationstest/getAfterCreateAlbumResponse.xml";
    public static final String CREATE_ALBUM_REQUEST = "/editconfigdefaultoperationstest/createAlbumRequest.xml";
    public static final String REPLACE_JUKEBOX_REQUEST = "/editconfigdefaultoperationstest/replace-config-jukebox.xml";
    public static final String GET_AFTER_REPLACE_JUKEBOX = "/editconfigdefaultoperationstest/replace-jukebox-response.xml";
    public static final String CREATE_SONG_RESPONSE = "/editconfigdefaultoperationstest/createSongResponse.xml";
    public static final String CREATE_SONG_REQUEST = "/editconfigdefaultoperationstest/createSongRequest.xml";
    public static final String CREATE_LIBRARY_ERROR_RESPONSE = "/editconfigdefaultoperationstest/createLibraryErrorResponse.xml";
    public static final String CREATE_ARTIST_ERROR_RESPONSE = "/editconfigdefaultoperationstest/createArtistErrorResponse.xml";
    public static final String CREATE_ARTIST_ERROR_RESPONSE2 = "/editconfigdefaultoperationstest/createArtistErrorResponse2.xml";
    public static final String CREATE_ARTIST_REQUEST = "/editconfigdefaultoperationstest/createArtistRequest.xml";
    public static final String CREATE_SECOND_ARTIST_REQUEST = "/editconfigdefaultoperationstest/createSecondArtistRequest.xml";
    public static final String GET_AFTER_CREATE_SECOND_ARTIST_RESPONSE_XML = "/editconfigdefaultoperationstest/getAfterCreateSecondArtistResponse.xml";
    public static final String EMPTY_LIBRARY = "/empty-library.xml";
    public static final String EMPTY_JUKEBOX = "/empty-jukebox.xml";
    public static final String OK_RESPONSE = "/ok-response.xml";
    public static final String MESSAGE_ID = "1";
    private NetconfClientInfo m_clientInfo = new NetconfClientInfo("unit-test", 1);

   // private ModelNode m_model;

    @Before
    public void initServer() throws SchemaBuildException {
        super.setup();
    }

    @Test
    public void testDefaultOperationNoneWithEmptyJukebox() throws IOException, SAXException, SchemaBuildException {
       
        createEmptyServer();
        
        //Verify empty jukebox
        verifyGetConfig(m_server, (NetconfFilter) null, EMPTY_JUKEBOX, MESSAGE_ID);

        EditConfigRequest request = new EditConfigRequest()
                .setTargetRunning()
                .setTestOption(EditConfigTestOptions.SET)
                .setErrorOption(EditConfigErrorOptions.STOP_ON_ERROR)
                .setDefaultOperation(EditConfigDefaultOperations.NONE)
                .setConfigElement(new EditConfigElement()
                .addConfigElementContent(loadAsXml(CREATE_SONG_REQUEST)));
        request.setMessageId(MESSAGE_ID);
        NetConfResponse response = new NetConfResponse().setMessageId(MESSAGE_ID);

        // Create song on empty jukebox
        m_server.onEditConfig(m_clientInfo, request, response);

        // verify error is thrown
        assertEquals(load(CREATE_LIBRARY_ERROR_RESPONSE), responseToString(response));

        // verify it is still empty
        verifyGetConfig(m_server, (NetconfFilter) null, EMPTY_JUKEBOX, MESSAGE_ID);
    }

    @Test
    public void testDefaultOperationNoneWithEmptyLibrary() throws IOException, SAXException {
        createServerWithEmptyLibrary();
        // verify empty library
        verifyGetConfig(m_server, (NetconfFilter) null, EMPTY_LIBRARY, MESSAGE_ID);

        EditConfigRequest request = new EditConfigRequest()
                .setTargetRunning()
                .setTestOption(EditConfigTestOptions.SET)
                .setErrorOption(EditConfigErrorOptions.STOP_ON_ERROR)
                .setDefaultOperation(EditConfigDefaultOperations.NONE)
                .setConfigElement(new EditConfigElement()
                        .addConfigElementContent(loadAsXml(CREATE_SONG_REQUEST)));
        request.setMessageId(MESSAGE_ID);
        NetConfResponse response = new NetConfResponse().setMessageId(MESSAGE_ID);

        // Create song on empty library
        m_server.onEditConfig(m_clientInfo, request, response);

        // verify error is thrown
        assertEquals(load(CREATE_ARTIST_ERROR_RESPONSE), responseToString(response));

        // verify library is still empty
        verifyGetConfig(m_server, (NetconfFilter) null, EMPTY_LIBRARY, MESSAGE_ID);


        request = new EditConfigRequest()
                .setTargetRunning()
                .setTestOption(EditConfigTestOptions.SET)
                .setErrorOption(EditConfigErrorOptions.STOP_ON_ERROR)
                .setConfigElement(new EditConfigElement()
                        .addConfigElementContent(loadAsXml(CREATE_ARTIST_REQUEST)));
        request.setMessageId(MESSAGE_ID);
        response = new NetConfResponse().setMessageId(MESSAGE_ID);

        // Create artist on empty library
        m_server.onEditConfig(m_clientInfo, request, response);

        verifyGetConfig(m_server, (NetconfFilter) null, CREATE_SONG_RESPONSE, MESSAGE_ID);


        request = new EditConfigRequest()
                .setTargetRunning()
                .setTestOption(EditConfigTestOptions.SET)
                .setErrorOption(EditConfigErrorOptions.STOP_ON_ERROR)
                .setDefaultOperation(EditConfigDefaultOperations.NONE)
                .setConfigElement(new EditConfigElement()
                        .addConfigElementContent(loadAsXml(CREATE_SECOND_ARTIST_REQUEST)));
        request.setMessageId(MESSAGE_ID);
        response = new NetConfResponse().setMessageId(MESSAGE_ID);

        // Create song on empty library
        m_server.onEditConfig(m_clientInfo, request, response);
        assertXMLStringEquals(load(CREATE_ARTIST_ERROR_RESPONSE2), responseToString(response));
        verifyGetConfig(m_server, (NetconfFilter) null, CREATE_SONG_RESPONSE, MESSAGE_ID); // The same test case would work with default-operation "merge" and create second artist
    }

    @Test
    public void testDefaultOperationMergeWithEmptyJukebox() throws IOException, SAXException, SchemaBuildException {
        createEmptyServer();
        //Verify empty jukebox
        verifyGetConfig(m_server, (NetconfFilter) null, EMPTY_JUKEBOX, MESSAGE_ID);

        EditConfigRequest request = new EditConfigRequest()
                .setTargetRunning()
                .setTestOption(EditConfigTestOptions.SET)
                .setErrorOption(EditConfigErrorOptions.STOP_ON_ERROR)
                .setConfigElement(new EditConfigElement()
                        .addConfigElementContent(loadAsXml(CREATE_SONG_REQUEST)));

        request.setMessageId("1");
        NetConfResponse response = new NetConfResponse().setMessageId(MESSAGE_ID);

        // Create song on empty jukebox
        m_server.onEditConfig(m_clientInfo, request, response);

        // assert Ok response
        assertEquals(load(OK_RESPONSE), responseToString(response));

        // verify song is created
        verifyGetConfig(m_server, (NetconfFilter) null, CREATE_SONG_RESPONSE, MESSAGE_ID);


    }

    @Test
    public void testDefaultOperationMergeWithEmptyLibrary() throws IOException, SAXException {

        createServerWithEmptyLibrary();
        // verify empty library
        verifyGetConfig(m_server, (NetconfFilter) null, EMPTY_LIBRARY, MESSAGE_ID);

        EditConfigRequest request = new EditConfigRequest()
                .setTargetRunning()
                .setTestOption(EditConfigTestOptions.SET)
                .setErrorOption(EditConfigErrorOptions.STOP_ON_ERROR)
                .setConfigElement(new EditConfigElement()
                        .addConfigElementContent(loadAsXml(CREATE_ALBUM_REQUEST)));
        request.setMessageId(MESSAGE_ID);
        NetConfResponse response = new NetConfResponse().setMessageId(MESSAGE_ID);

        //Create an album on empty library with no create tag on artist
        m_server.onEditConfig(m_clientInfo, request, response);

        // assert Ok response
        assertEquals(load(OK_RESPONSE), responseToString(response));

        //Verify album is created
        verifyGetConfig(m_server, (NetconfFilter) null, GET_AFTER_CREATE_ALBUM, MESSAGE_ID);

        request = new EditConfigRequest()
                .setTargetRunning()
                .setTestOption(EditConfigTestOptions.SET)
                .setErrorOption(EditConfigErrorOptions.STOP_ON_ERROR)
                .setConfigElement(new EditConfigElement()
                        .addConfigElementContent(loadAsXml(CREATE_ARTIST_REQUEST)));
        request.setMessageId(MESSAGE_ID);
        response = new NetConfResponse().setMessageId(MESSAGE_ID);

        // Create artist request, this should fail
        m_server.onEditConfig(m_clientInfo, request, response);

        //make sure the data store is intact
        verifyGetConfig(m_server, (NetconfFilter) null, GET_AFTER_CREATE_ALBUM, MESSAGE_ID);

        request = new EditConfigRequest()
                .setTargetRunning()
                .setTestOption(EditConfigTestOptions.SET)
                .setErrorOption(EditConfigErrorOptions.STOP_ON_ERROR)
                .setConfigElement(new EditConfigElement()
                        .addConfigElementContent(loadAsXml(CREATE_SECOND_ARTIST_REQUEST)));
        request.setMessageId(MESSAGE_ID);
        response = new NetConfResponse().setMessageId(MESSAGE_ID);

        // Create song on empty library
        m_server.onEditConfig(m_clientInfo, request, response);

        assertEquals(load(OK_RESPONSE), responseToString(response));

        verifyGetConfig(m_server, (NetconfFilter) null, GET_AFTER_CREATE_SECOND_ARTIST_RESPONSE_XML, MESSAGE_ID);

    }

    @Test
    public void testDefaultOperationReplaceOnJukebox() throws IOException, SAXException {
        createServerWithEmptyLibrary();
        // verify empty library
        verifyGetConfig(m_server, (NetconfFilter) null, EMPTY_LIBRARY, MESSAGE_ID);

        EditConfigRequest request = new EditConfigRequest()
                .setTargetRunning()
                .setTestOption(EditConfigTestOptions.SET)
                .setErrorOption(EditConfigErrorOptions.STOP_ON_ERROR)
                .setConfigElement(new EditConfigElement().addConfigElementContent(loadAsXml(CREATE_ALBUM_REQUEST)));
        request.setMessageId(MESSAGE_ID);
        NetConfResponse response = new NetConfResponse().setMessageId(MESSAGE_ID);

        //Create an album on empty library with no create tag on artist
        m_server.onEditConfig(m_clientInfo, request, response);
        // assert Ok response
        assertEquals(load(OK_RESPONSE), responseToString(response));
        //make sure the data store is intact
        verifyGetConfig(m_server, (NetconfFilter) null, GET_AFTER_CREATE_ALBUM, MESSAGE_ID);

        //Replace the jukebox with default operation=repace
        request = new EditConfigRequest()
                .setTargetRunning()
                .setTestOption(EditConfigTestOptions.SET)
                .setErrorOption(EditConfigErrorOptions.STOP_ON_ERROR)
                .setDefaultOperation(EditConfigOperations.REPLACE)
                .setConfigElement(new EditConfigElement().addConfigElementContent(loadAsXml(REPLACE_JUKEBOX_REQUEST)));
        request.setMessageId(MESSAGE_ID);
        response = new NetConfResponse().setMessageId(MESSAGE_ID);

        //Create an album on empty library with no create tag on artist
        m_server.onEditConfig(m_clientInfo, request, response);
        // assert Ok response
        assertEquals(load(OK_RESPONSE), responseToString(response));
        //make sure the data store is intact
        verifyGetConfig(m_server, (NetconfFilter) null, EMPTY_JUKEBOX, MESSAGE_ID);
    }

    private void createServerWithEmptyLibrary(){
        m_server = new NetConfServerImpl(m_schemaRegistry, mock(RpcPayloadConstraintParser.class));
        m_model = createJukeBoxWithEmptyLibrary(m_modelNodeHelperRegistry, m_subSystemRegistry, m_schemaRegistry);
        m_rootModelNodeAggregator = new RootModelNodeAggregatorImpl(m_schemaRegistry, m_modelNodeHelperRegistry,
                mock(ModelNodeDataStoreManager.class), m_subSystemRegistry).addModelServiceRoot(m_componentId, m_model);
        DataStore dataStore = new DataStore(StandardDataStores.RUNNING, m_rootModelNodeAggregator,m_subSystemRegistry);
        NbiNotificationHelper nbiNotificationHelper = mock(NbiNotificationHelper.class);
        dataStore.setNbiNotificationHelper(nbiNotificationHelper);
        m_server.setRunningDataStore(dataStore);
    }
}
