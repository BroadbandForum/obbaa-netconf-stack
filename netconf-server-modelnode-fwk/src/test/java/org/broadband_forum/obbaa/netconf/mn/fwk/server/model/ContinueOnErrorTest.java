package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.createJukeBoxModelWithYear;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.loadAsXml;
import static org.junit.Assert.assertFalse;
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
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregatorImpl;

import org.broadband_forum.obbaa.netconf.server.rpc.RpcPayloadConstraintParser;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;

public class ContinueOnErrorTest {
    private final static Logger LOGGER = Logger.getLogger(ContinueOnErrorTest.class);

    private NetConfServerImpl m_server;
    private NetconfClientInfo m_clientInfo = new NetconfClientInfo("unit-test", 1);

    private ModelNode m_model;

	private SubSystemRegistry m_subSystemRegistry = new SubSystemRegistryImpl();
	private SchemaRegistry m_schemaRegistry;
    private RootModelNodeAggregator m_rootModelNodeAggregator;
    private String m_componentId = "test";
	private ModelNodeHelperRegistry m_modelNodeHelperRegistry = new ModelNodeHelperRegistryImpl(m_schemaRegistry);


    @Before
    public void initServer() throws SchemaBuildException {
    	m_schemaRegistry = new SchemaRegistryImpl(TestUtil.getJukeBoxYangs(), Collections.emptySet(), Collections.emptyMap(), new NoLockService());
        createNonEmptyServer();
    }

    @Test
    public void testRollbackForAttributeChanges() throws IOException, SAXException {
        EditConfigRequest request = new EditConfigRequest()
                .setTargetRunning()
                .setTestOption(EditConfigTestOptions.SET)
                .setErrorOption(EditConfigErrorOptions.CONTINUE_ON_ERROR)//add rollback behavior
                .setConfigElement(new EditConfigElement()
                        .addConfigElementContent(loadAsXml("/rollback-attr-change1.xml")));
        request.setMessageId("1");
        NetConfResponse response = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request, response);
        // assert not-Ok response
        assertFalse(response.isOk());
        LOGGER.info(response.responseToString());
        // do a get-config to be sure
        verifyGetConfig(null, "/continue-on-error-expected-year-changed.xml");
    }
    private void verifyGetConfig(String filterInput, String expectedOutput) throws IOException, SAXException {
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

        TestUtil.assertXMLEquals(expectedOutput, response);
    }

    private void createNonEmptyServer() {
        m_server = new NetConfServerImpl(m_schemaRegistry, mock(RpcPayloadConstraintParser.class));
        m_model = createJukeBoxModelWithYear(m_modelNodeHelperRegistry, m_subSystemRegistry, m_schemaRegistry);
        m_rootModelNodeAggregator = new RootModelNodeAggregatorImpl(m_schemaRegistry, m_modelNodeHelperRegistry,
                mock(ModelNodeDataStoreManager.class), m_subSystemRegistry).addModelServiceRoot(m_componentId, m_model);
        m_server.setRunningDataStore(new DataStore(StandardDataStores.RUNNING, m_rootModelNodeAggregator, m_subSystemRegistry));
    }
}
