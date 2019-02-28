package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.yang;

import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.YangUtils.loadXmlDataIntoServer;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.assertXMLEquals;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.loadAsXml;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.sendEditConfig;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.verifyGetConfig;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.DataStore;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NbiNotificationHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NetConfServerImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregatorImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.inmemory.InMemoryDSM;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.LocalSubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.YangUtils;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcPayloadConstraintParser;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;

public class YangEditConfigCreateTest extends AbstractYangValidationTestSetup {
    private NetConfServerImpl m_server;
    private SubSystemRegistry m_subSystemRegistry = new SubSystemRegistryImpl();
    private static final String m_empty_jukebox_xmlFile = YangEditConfigCreateTest.class.getResource("/empty-example-jukebox.xml").getPath();
	private static final String m_empty_library_xmlFile = YangEditConfigCreateTest.class.getResource("/empty-library-example-jukebox.xml").getPath();
    private RootModelNodeAggregator m_rootModelNodeAggregator;
    private SchemaRegistry m_schemaRegistry;
    private ModelNodeWithAttributes m_modelWithAttributes;
    private InMemoryDSM m_modelNodeDsm;
	private ModelNodeHelperRegistry m_modelNodeHelperRegistry = new ModelNodeHelperRegistryImpl(m_schemaRegistry);

    @Before
    public void initServer() throws SchemaBuildException, ModelNodeInitException {
        m_schemaRegistry = new SchemaRegistryImpl(Collections.emptyList(), Collections.emptySet(), Collections.emptyMap(), new NoLockService());
        m_schemaRegistry.loadSchemaContext("jukebox", TestUtil.getJukeBoxYangs(), Collections.emptySet(), Collections.emptyMap());
        m_server = new NetConfServerImpl(m_schemaRegistry, mock(RpcPayloadConstraintParser.class));
        m_modelNodeDsm = new InMemoryDSM(m_schemaRegistry);
        m_modelWithAttributes = YangUtils.createInMemoryModelNode(YANG_PATH, new LocalSubSystem(),
                m_modelNodeHelperRegistry, m_subSystemRegistry, m_schemaRegistry, m_modelNodeDsm);
        m_rootModelNodeAggregator = new RootModelNodeAggregatorImpl(m_schemaRegistry, m_modelNodeHelperRegistry, m_modelNodeDsm, m_subSystemRegistry)
                .addModelServiceRoot(m_componentId, m_modelWithAttributes);
        DataStore dataStore = new DataStore(StandardDataStores.RUNNING, m_rootModelNodeAggregator, m_subSystemRegistry);
        NbiNotificationHelper nbiNotificationHelper = mock(NbiNotificationHelper.class);
        dataStore.setNbiNotificationHelper(nbiNotificationHelper);
        m_server.setRunningDataStore(dataStore);
    }

    @Test
    public void testCreateLibraryWhenThereIsNoLibraryWorks() throws ModelNodeInitException, SAXException, IOException {
    	//load server without library
        loadXmlDataIntoServer(m_server,m_empty_jukebox_xmlFile);

        //send edit config to create library
        NetConfResponse response = sendEditConfig(m_server, m_clientInfo, loadAsXml("/create-library.xml"), "1");

        // assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        // do a get-config to be sure
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/empty-library.xml", "1");
        
        verifyGetConfig(m_server, StandardDataStores.RUNNING, "/filter-library.xml", "/empty-library.xml", "1");
    }
    
     @Test
    public void testCreateLibraryWhenThereIsLibraryThrowsProperError() throws SAXException, IOException, ModelNodeInitException {

         loadXmlDataIntoServer(m_server, XML_PATH);

    	//send edit config to create library that already exist
        NetConfResponse response = sendEditConfig(m_server, m_clientInfo, loadAsXml("/create-library.xml"), "1");

        // assert not-Ok response
        assertXMLEquals("/data-exists-error.xml", response);
    }

    @Test
    public void testCreateAlbumCircus() throws SAXException, IOException, ModelNodeInitException {
    	//load server without library
        loadXmlDataIntoServer(m_server, m_empty_library_xmlFile);
        
        //send edit config to create album Circus
        NetConfResponse response = sendEditConfig(m_server, m_clientInfo, loadAsXml("/create-album-circus.xml"), "1");
        
        // assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        // do a get-config to be sure
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/with-circus.xml", "1");
    }

    @Test
    public void testCreateAlbumCircusThrowsError() throws SAXException, IOException, ModelNodeInitException {
    	//load server with empty library
        loadXmlDataIntoServer(m_server, m_empty_library_xmlFile);
        //send edit config to create album Circus
        NetConfResponse response = sendEditConfig(m_server, m_clientInfo, loadAsXml("/create-album-circus.xml"), "1");

        // assert Ok response
        assertXMLEquals("/ok-response.xml", response);

        // do a get-config to be sure
        verifyGetConfig(m_server, StandardDataStores.RUNNING, null, "/with-circus.xml", "1");
        
        //again send edit config to create album Cirus that already exists
        NetConfResponse failedResponse = sendEditConfig(m_server, m_clientInfo, loadAsXml("/create-album-circus.xml"), "1");
        
        // assert not-Ok response
        assertXMLEquals("/data-exists-error-album.xml", failedResponse);
    }
}
