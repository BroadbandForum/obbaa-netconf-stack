/*
 * Copyright 2018 Broadband Forum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.assertXMLEquals;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.loadAsXml;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.verifyGetConfig;

import java.io.IOException;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigDefaultOperations;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigElement;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigErrorOptions;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigOperations;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigTestOptions;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfFilter;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xml.sax.SAXException;

/**
 * Created by sgs on 8/25/15.
 */
@RunWith(RequestScopeJunitRunner.class)
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
    public void initServer() throws SchemaBuildException, ModelNodeInitException {
        super.setup();
    }

    @Test
    public void testDefaultOperationNoneWithEmptyJukebox() throws IOException, SAXException, SchemaBuildException, ModelNodeInitException {
       
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
        assertXMLEquals(CREATE_LIBRARY_ERROR_RESPONSE, response);

        // verify it is still empty
        verifyGetConfig(m_server, (NetconfFilter) null, EMPTY_JUKEBOX, MESSAGE_ID);
    }

    @Test
    public void testDefaultOperationNoneWithEmptyLibrary() throws IOException, SAXException, ModelNodeInitException, SchemaBuildException {
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
        assertXMLEquals(CREATE_ARTIST_ERROR_RESPONSE, response);

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
        assertXMLEquals(CREATE_ARTIST_ERROR_RESPONSE2, response);
        verifyGetConfig(m_server, (NetconfFilter) null, CREATE_SONG_RESPONSE, MESSAGE_ID); // The same test case would work with default-operation "merge" and create second artist
    }

    @Test
    public void testDefaultOperationMergeWithEmptyJukebox() throws IOException, SAXException, SchemaBuildException, ModelNodeInitException {
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
        assertXMLEquals(OK_RESPONSE, response);

        // verify song is created
        verifyGetConfig(m_server, (NetconfFilter) null, CREATE_SONG_RESPONSE, MESSAGE_ID);


    }

    @Test
    public void testDefaultOperationMergeWithEmptyLibrary() throws IOException, SAXException, ModelNodeInitException, SchemaBuildException {

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
        assertXMLEquals(OK_RESPONSE, response);

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

        assertXMLEquals(OK_RESPONSE, response);

        verifyGetConfig(m_server, (NetconfFilter) null, GET_AFTER_CREATE_SECOND_ARTIST_RESPONSE_XML, MESSAGE_ID);

    }

    @Test
    public void testDefaultOperationReplaceOnJukebox() throws IOException, SAXException, ModelNodeInitException, SchemaBuildException {
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
        assertXMLEquals(OK_RESPONSE, response);
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
        assertXMLEquals(OK_RESPONSE, response);
        //make sure the data store is intact
        verifyGetConfig(m_server, (NetconfFilter) null, EMPTY_JUKEBOX, MESSAGE_ID);
    }
}
