package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.assertXMLEquals;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.createJukeBoxModel;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.loadAsXml;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.w3c.dom.Element;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.GetConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfFilter;
import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.jukebox2.Library;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.jukebox2.LibrarySystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregatorImpl;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;

public class NetconfGetWithDepthTest {
	
	private NetConfServerImpl m_server;
	private SubSystemRegistry m_subSystemRegistry = new SubSystemRegistryImpl();
    private SchemaRegistry m_schemaRegistry;
    private static final String NS = "http://example.com/ns/example-jukebox";
    private static final String REVISION = "2014-07-03";
	private ModelNodeHelperRegistry m_modelNodeHelperRegistry = new ModelNodeHelperRegistryImpl(m_schemaRegistry);

	@Before
	public void initServer() throws SchemaBuildException {
		m_schemaRegistry = new SchemaRegistryImpl(TestUtil.getJukeBoxYangs(), Collections.emptySet(), Collections.emptyMap(), new NoLockService());
		m_server = new NetConfServerImpl(m_schemaRegistry);
		LibrarySystem librarySystem = new LibrarySystem();
		String componentId = Library.QNAME.toString();
		m_subSystemRegistry.register(componentId, Library.LIBRARY_SCHEMA_PATH, librarySystem);

    	ModelNode jukeboxNode = createJukeBoxModel(m_modelNodeHelperRegistry, m_subSystemRegistry, m_schemaRegistry);
    	librarySystem.setRootModelNode(jukeboxNode);
		RootModelNodeAggregator rootModelNodeAggregator = new RootModelNodeAggregatorImpl(m_schemaRegistry, m_modelNodeHelperRegistry,
				mock(ModelNodeDataStoreManager.class), m_subSystemRegistry)
                .addModelServiceRoot(componentId, jukeboxNode);
		m_server.setRunningDataStore(new DataStore(StandardDataStores.RUNNING, rootModelNodeAggregator, m_subSystemRegistry));
	}

	@Test
	public void testGetWithDepthWithoutFilter() throws Exception {
		NetconfClientInfo client = new NetconfClientInfo("test", 1);

		GetRequest request = new GetRequest();
		request.setMessageId("1");
		NetConfResponse response = new NetConfResponse();
		response.setMessageId("1");
		
		request.setDepth(1);
		m_server.onGet(client, request, response);
		Element actual = response.getData();
		Element expected = getExpectedUserFilterLevel1();
		assertXMLEquals(actual, expected);
		
		
		request.setDepth(2);
		m_server.onGet(client, request, response);
		actual = response.getData();
		expected = getExpectedUserFilterLevel2();
		assertXMLEquals(actual, expected);
	}
	
	@Test
    public void testGetWithDepthAndFieldsWithoutFilter() throws Exception {
        NetconfClientInfo client = new NetconfClientInfo("test", 1);

        GetRequest request = new GetRequest();
        request.setMessageId("1");
        NetConfResponse response = new NetConfResponse();
        response.setMessageId("1");
        
        request.setDepth(1);
        Map<String, List<QName>> fieldValues = new HashMap<>();
        
        QName artistQName = QName.create(NS, REVISION, "artist");
        List<QName> artistQNamesList = new ArrayList<QName>();
        artistQNamesList.add(artistQName);
        
        QName nameQName = QName.create(NS, REVISION, "name");
        List<QName> nameList = new ArrayList<QName>();
        nameList.add(nameQName);
        
        fieldValues.put("library", artistQNamesList);
        fieldValues.put("artist", nameList);
        
        request.setFieldValues(fieldValues );
        m_server.onGet(client, request, response);
        Element actual = response.getData();
        Element expected = getExpectedResultWithDepth1AndFields();
        assertXMLEquals(expected, actual);
        
        nameQName = QName.create(NS, REVISION, "name");
        nameList.clear();
        nameList.add(nameQName);
        
        fieldValues.put("artist", nameList);
        
        request.setFieldValues(fieldValues );
        m_server.onGet(client, request, response);
        request.setDepth(2);
        m_server.onGet(client, request, response);
        actual = response.getData();
        expected = getExpectedResultWithDepth1AndFields(); // Depth 1 or 2 doesn't matter for this case
        assertXMLEquals(actual, expected);
        
        nameQName = QName.create(NS, REVISION, "name");
        nameList.clear();
        nameList.add(nameQName);
        
        QName songCountQName = QName.create(NS, REVISION, "song-count");
        artistQNamesList.add(songCountQName);
        
        fieldValues.put("artist", nameList);
        fieldValues.put("library", artistQNamesList);
        
        request.setFieldValues(fieldValues );
        m_server.onGet(client, request, response);
        request.setDepth(2);
        m_server.onGet(client, request, response);
        actual = response.getData();
        expected = getExpectedResultWithDepth2AndFields(); 
        assertXMLEquals(actual, expected);
    }
	
	@Test
	public void testGetConfigWithDepthWithoutFilter() throws Exception {
		NetconfClientInfo client = new NetconfClientInfo("test", 1);

		GetConfigRequest request = new GetConfigRequest();
		request.setMessageId("1");
		NetConfResponse response = new NetConfResponse();
		response.setMessageId("1");
		
		request.setDepth(15); // Full tree
		m_server.onGetConfig(client, request, response);
		assertXMLEquals("/getconfig-unfiltered.xml", response);
		
		request.setDepth(5); // Full tree
		m_server.onGetConfig(client, request, response);
		Element expected = getExpectedConfigResultForLevel5();
		assertXMLEquals(expected, response.getData());
		
		request.setDepth(4); // Full tree
		m_server.onGetConfig(client, request, response);
		Element actual = response.getData();
		expected = getExpectedConfigResultForLevel4();
		assertXMLEquals(expected, actual);
	}
	
	@Test
	public void testGetWithDepthWithFilter() throws Exception {
		NetconfClientInfo client = new NetconfClientInfo("test", 1);

		GetRequest request = new GetRequest();
		request.setMessageId("1");
		String filterInput = "/filter-two-matching.xml";
		NetconfFilter filter = new NetconfFilter();
		filter.setType("subtree");
		filter.addXmlFilter(loadAsXml(filterInput));
		request.setFilter(filter);

		NetConfResponse response = new NetConfResponse();
		response.setMessageId("1");

		request.setDepth(3);
		m_server.onGet(client, request, response);

		Element actual = response.getData();
		Element expected = getExpectedResultForLevel3();
		assertXMLEquals(expected, actual);

	}
	
	@Test
    public void testGetWithDepthAndFieldsAndFilter() throws Exception {
        NetconfClientInfo client = new NetconfClientInfo("test", 1);

        GetRequest request = new GetRequest();
        request.setMessageId("1");
        String filterInput = "/filter-two-matching.xml";
        NetconfFilter filter = new NetconfFilter();
        filter.setType("subtree");
        filter.addXmlFilter(loadAsXml(filterInput));
        request.setFilter(filter);

        NetConfResponse response = new NetConfResponse();
        response.setMessageId("1");

        QName artistQName = QName.create(NS, REVISION, "artist");
        List<QName> artistQNamesList = new ArrayList<QName>();
        artistQNamesList.add(artistQName);
        
        QName albumQName = QName.create(NS, REVISION, "song");
        List<QName> albumQNamesList = new ArrayList<QName>();
        albumQNamesList.add(albumQName);
        
        QName songQName = QName.create(NS, REVISION, "song");
        List<QName> songQNamesList = new ArrayList<QName>();
        songQNamesList.add(songQName);
        
        QName songNameQName = QName.create(NS, REVISION, "name");
        List<QName> songNameList = new ArrayList<QName>();
        songNameList.add(songNameQName);
        
        Map<String, List<QName>> fieldValues = new HashMap<>();
        fieldValues.put("library", artistQNamesList);
        fieldValues.put("artist", songQNamesList);
        fieldValues.put("album", albumQNamesList);
        fieldValues.put("song", songNameList);
        request.setDepth(3);
        request.setFieldValues(fieldValues);
        m_server.onGet(client, request, response);

        Element actual = response.getData();
        Element expected = getExpectedResultForLevel3WithFields();
        assertXMLEquals(expected, actual);

    }
	
	@Test
	public void testGetConfigWithDepthWithFilter() throws Exception {
		NetconfClientInfo client = new NetconfClientInfo("test", 1);

		GetConfigRequest request = new GetConfigRequest();
		request.setMessageId("1");
		request.setSource("running");
		String filterInput = "/filter-two-matching.xml";
		NetconfFilter filter = new NetconfFilter();
		filter.setType("subtree");
		filter.addXmlFilter(loadAsXml(filterInput));
		request.setFilter(filter);

		NetConfResponse response = new NetConfResponse();
		response.setMessageId("1");

		request.setDepth(3);
		m_server.onGetConfig(client, request, response);

		Element actual = response.getData();
		Element expected = getExpectedResultForLevel3();
		assertXMLEquals(expected, actual);
	}
	
    private Element getExpectedUserFilterLevel1() throws Exception {
        String data = "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
        		+ "<jbox:jukebox xmlns:jbox=\"http://example.com/ns/example-jukebox\"/>"
        		+ "</data>";
        return DocumentUtils.stringToDocument(data).getDocumentElement();
    }
    
    private Element getExpectedResultWithDepth1AndFields() throws Exception {
        String data = "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + "<jbox:jukebox xmlns:jbox=\"http://example.com/ns/example-jukebox\">"
                + "<jbox:library>"
                + "<jbox:artist>"
                + "<jbox:name>Lenny</jbox:name>"
                + "</jbox:artist>"
                + "</jbox:library>"
                + "</jbox:jukebox>"
                + "</data>";
        return DocumentUtils.stringToDocument(data).getDocumentElement();
    }
    
    private Element getExpectedResultWithDepth2AndFields() throws Exception {
        String data = "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + "<jbox:jukebox xmlns:jbox=\"http://example.com/ns/example-jukebox\">"
                + "<jbox:library>"
                + "<jbox:song-count>5</jbox:song-count>"
                + "<jbox:artist>"
                + "<jbox:name>Lenny</jbox:name>"
                + "</jbox:artist>"
                + "</jbox:library>"
                + "</jbox:jukebox>"
                + "</data>";
        return DocumentUtils.stringToDocument(data).getDocumentElement();
    }
    
    private Element getExpectedUserFilterLevel2() throws Exception {
    	String data = "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
        		+ "<jbox:jukebox xmlns:jbox=\"http://example.com/ns/example-jukebox\">"
        		+ "<jbox:library>"
        		+ "</jbox:library>"
        		+ "</jbox:jukebox>"
        		+ "</data>";
        return DocumentUtils.stringToDocument(data).getDocumentElement();
    }
    
    private Element getExpectedResultForLevel3() throws Exception {
        String data = "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
        		+ "<jbox:jukebox xmlns:jbox=\"http://example.com/ns/example-jukebox\">"
        		+ "<jbox:library>"
        		+ "<jbox:artist>"
        		+ "</jbox:artist>"
        		+ "</jbox:library>"
        		+ "</jbox:jukebox>"
        		+ "</data>";
        return DocumentUtils.stringToDocument(data).getDocumentElement();
    }
    
    private Element getExpectedResultForLevel3WithFields() throws Exception {
        String data = "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + "<jbox:jukebox xmlns:jbox=\"http://example.com/ns/example-jukebox\">"
                + "<jbox:library>"
                + "<jbox:artist>"
                + "<jbox:album>"
                + "<jbox:song>"
                + "<jbox:name>Are you gonne go my way</jbox:name>"
                + "</jbox:song>"
                + "<jbox:song>"
                + "<jbox:name>Fly Away</jbox:name>"
                + "</jbox:song>"
                + "</jbox:album>"
                + "</jbox:artist>"
                + "</jbox:library>"
                + "</jbox:jukebox>"
                + "</data>";
        return DocumentUtils.stringToDocument(data).getDocumentElement();
    }
    
    private Element getExpectedConfigResultForLevel4() throws Exception{
    	String data = "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
        		+ "<jbox:jukebox xmlns:jbox=\"http://example.com/ns/example-jukebox\">"
    			+ "<jbox:library>"
    			+ "<jbox:artist>"
    			+ "<jbox:name>Lenny</jbox:name>"
    			+ "<jbox:album>"
    			+ "</jbox:album>"
    			+ "<jbox:album>"
    			+ "</jbox:album>"
    			+ "</jbox:artist>"
    			+ "</jbox:library>"
    			+ "</jbox:jukebox>"
    			+ "</data>";
    	return DocumentUtils.stringToDocument(data).getDocumentElement();
    }
    
    private Element getExpectedConfigResultForLevel5() throws Exception{
        String data = "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + "<jbox:jukebox xmlns:jbox=\"http://example.com/ns/example-jukebox\">"
                + "<jbox:library>"
                + "<jbox:artist>"
                + "<jbox:name>Lenny</jbox:name>"
                + "<jbox:album>"
                + "<jbox:name>Greatest hits</jbox:name>"
                + "<jbox:song/>"
                + "<jbox:song/>"
                + "<jbox:year>0</jbox:year>"
                + "</jbox:album>"
                + "<jbox:album>"
                + "<jbox:name>Circus</jbox:name>"
                + "<jbox:song/>"
                + "<jbox:song/>"
                + "<jbox:song/>"
                + "<jbox:year>0</jbox:year>"
                + "</jbox:album>"
                + "</jbox:artist>"
                + "</jbox:library>"
                + "</jbox:jukebox>"
                + "</data>";
        return DocumentUtils.stringToDocument(data).getDocumentElement();
    }
}
