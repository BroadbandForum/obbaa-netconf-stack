package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.createJukeBoxWithEmptyLibrary;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.load;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.loadAsXml;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.responseToString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.Collections;

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
import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregatorImpl;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;

public class EditConfigCreateTest extends AbstractEditConfigTestSetup{
    private final static Logger LOGGER = Logger.getLogger(EditConfigCreateTest.class);

    private NetconfClientInfo m_clientInfo = new NetconfClientInfo("unit-test", 1);

    @Before
    public void initServer() throws SchemaBuildException {
        m_schemaRegistry = new SchemaRegistryImpl(TestUtil.getJukeBoxYangs(), Collections.emptySet(), Collections.emptyMap(), new NoLockService());
        super.createServerWithNonEmptyJukeBox();
    }

    @Test
    public void testCreateLibraryWhenThereIsNoLibraryWorks() throws SchemaBuildException {
       
        createEmptyServer();
        EditConfigRequest request = new EditConfigRequest()
                .setTargetRunning()
                .setTestOption(EditConfigTestOptions.SET)
                .setErrorOption(EditConfigErrorOptions.STOP_ON_ERROR)
                .setConfigElement(new EditConfigElement()
                        .addConfigElementContent(loadAsXml("/create-library.xml")));
        request.setMessageId("1");
        NetConfResponse response = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request, response);
        // assert Ok response
        assertEquals(load("/ok-response.xml"), responseToString(response));

        // do a get-config to be sure
        verifyGetConfig(null, "/empty-library.xml");
    }
    
    /*@Test
    public void testCreateLibraryWhenThereIsNoLibraryRollsBack() {
        createEmptyServer();
        EditConfigRequest request = new EditConfigRequest()
                .setTargetRunning()
                .setTestOption(EditConfigTestOptions.SET)
                .setErrorOption(AxsEditConfigErrorOptions.ROLLBACK_ON_ERROR)
                .setConfigElement(new AxsEditConfigElement()
                        .setConfigElementContent(loadAsXml("/create-library-rollback.xml")));
		request.setMessageId("1");
        NetConfResponse response = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request, response);
        // assert Ok response
        assertEquals(load("/not-ok-response.xml"), responseToString(response));

        // do a get-config to be sure that jukebox is intact
        verifyGetConfig(null, "/empty-jukebox.xml");
    }*/
    
    @Test
    public void testCreateLibraryWhenThereIsLibraryThrowsProperError() {
        EditConfigRequest request = new EditConfigRequest()
                .setTargetRunning()
                .setTestOption(EditConfigTestOptions.SET)
                .setErrorOption(EditConfigErrorOptions.STOP_ON_ERROR)
                .setConfigElement(new EditConfigElement()
                        .addConfigElementContent(loadAsXml("/create-library.xml")));
        request.setMessageId("1");
        NetConfResponse response = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request, response);
        // assert not-Ok response
        assertEquals(load("/data-exists-error.xml"), responseToString(response));
    }
 
    private void createServerWithEmptyLibrary(){
        m_server = new NetConfServerImpl(m_schemaRegistry);
        m_model = createJukeBoxWithEmptyLibrary(m_modelNodeHelperRegistry, m_subSystemRegistry, m_schemaRegistry);
        m_rootModelNodeAggregator = new RootModelNodeAggregatorImpl(m_schemaRegistry, m_modelNodeHelperRegistry,
                mock(ModelNodeDataStoreManager.class), m_subSystemRegistry).addModelServiceRoot(m_componentId, m_model);
        DataStore dataStore = new DataStore(StandardDataStores.RUNNING, m_rootModelNodeAggregator,m_subSystemRegistry);
        NbiNotificationHelper nbiNotificationHelper = mock(NbiNotificationHelper.class);
        dataStore.setNbiNotificationHelper(nbiNotificationHelper);
        m_server.setRunningDataStore(dataStore);
    }

    @Test
    public void testCreateAlbumCircus() throws IOException, SAXException {
        createServerWithEmptyLibrary();
        EditConfigRequest request = new EditConfigRequest()
                .setTargetRunning()
                .setTestOption(EditConfigTestOptions.SET)
                .setErrorOption(EditConfigErrorOptions.STOP_ON_ERROR)
                .setConfigElement(new EditConfigElement()
                        .addConfigElementContent(loadAsXml("/create-album-circus.xml")));
        request.setMessageId("1");
        NetConfResponse response = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request, response);
        // assert Ok response
        assertEquals(load("/ok-response.xml"), responseToString(response));

        // do a get-config to be sure
        TestUtil.verifyGetConfig(m_server, (NetconfFilter) null, "/with-circus.xml", "1");

    }

    @Test
    public void testNaturalKeyWithSpaceWorks() throws IOException, SAXException {
        createServerWithEmptyLibrary();
        EditConfigRequest request = new EditConfigRequest()
                .setTargetRunning()
                .setTestOption(EditConfigTestOptions.SET)
                .setErrorOption(EditConfigErrorOptions.STOP_ON_ERROR)
                .setConfigElement(new EditConfigElement()
                        .addConfigElementContent(loadAsXml("/create-album-circus-with-space.xml")));
        request.setMessageId("1");
        NetConfResponse response = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request, response);
        // assert Ok response
        assertEquals(load("/ok-response.xml"), responseToString(response));

        // do a get-config to be sure
        verifyGetConfig(null, "/naturalkey-with-space.xml");

        // do a get-config with filter
        TestUtil.verifyGetConfig(m_server, "/get-artist-with-filter.xml", "/naturalkey-with-space.xml", "1");

    }
    /*@Test
    public void testCreateAlbumRollsback() {
        createServerWithEmptyLibrary();
        EditConfigRequest request = new EditConfigRequest()
                .setTargetRunning()
                .setTestOption(EditConfigTestOptions.SET)
                .setErrorOption(AxsEditConfigErrorOptions.ROLLBACK_ON_ERROR)
                .setConfigElement(new AxsEditConfigElement()
                        .setConfigElementContent(loadAsXml("/create-album-circus-rollback.xml")));
		request.setMessageId("1");
        NetConfResponse response = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request, response);
        // assert Ok response
        assertEquals(load("/not-ok-response.xml"), responseToString(response));

        // do a get-config to be sure
        verifyGetConfig(null, "/empty-library.xml");

    }
    */
    @Test
    public void testCreateAlbumCircusThrowsError() throws IOException, SAXException {
        createServerWithEmptyLibrary();
        EditConfigRequest request = new EditConfigRequest()
                .setTargetRunning()
                .setTestOption(EditConfigTestOptions.SET)
                .setErrorOption(EditConfigErrorOptions.STOP_ON_ERROR)
                .setConfigElement(new EditConfigElement()
                        .addConfigElementContent(loadAsXml("/create-album-circus.xml")));
        request.setMessageId("1");
        NetConfResponse response = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request, response);
        // assert Ok response
        assertEquals(load("/ok-response.xml"), responseToString(response));

        // do a get-config to be sure
        TestUtil.verifyGetConfig(m_server, (NetconfFilter) null, "/with-circus.xml","1");
        
        response = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request, response);
        // assert not-Ok response
        System.out.println(responseToString(response));
        assertEquals(load("/data-exists-error-child.xml"), responseToString(response));

    }

    private void verifyGetConfig(String filterInput, String expectedOutput) {
        NetconfClientInfo client = new NetconfClientInfo("test", 1);

        GetConfigRequest request = new GetConfigRequest();
        request.setMessageId("1");
        request.setSource("running");
        if (filterInput != null) {
            NetconfFilter filter = new NetconfFilter();
            // we have two variants fo the select node in here
            filter.addXmlFilter(loadAsXml(filterInput));
            request.setFilter(filter);
        }

        NetConfResponse response = new NetConfResponse();
        response.setMessageId("1");

        m_server.onGetConfig(client, request, response);

        try {
            TestUtil.assertXMLEquals(expectedOutput, response);
        } catch (SAXException | IOException e) {
            LOGGER.error("test comparison failed" , e);
            fail("test comparison failed" + e.getMessage());
        }
    }

}
