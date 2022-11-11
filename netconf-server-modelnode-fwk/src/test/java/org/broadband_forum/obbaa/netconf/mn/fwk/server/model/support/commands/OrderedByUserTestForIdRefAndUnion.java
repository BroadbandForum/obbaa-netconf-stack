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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.commands;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.CompositeSubSystemImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.DataStore;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NbiNotificationHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NetConfServerImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregatorImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.inmemory.InMemoryDSM;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.LocalSubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.YangUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.yang.AbstractValidationTestSetup;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.xml.sax.SAXException;

@RunWith(RequestScopeJunitRunner.class)
public class OrderedByUserTestForIdRefAndUnion extends AbstractValidationTestSetup {

    private static final String CLIENT_USERNAME = "test";
    private static final String COMPONENT_ID = "test";

    private SubSystemRegistry m_subSystemRegistry;
    private SchemaRegistry m_schemaRegistry;
    private ModelNodeHelperRegistry m_modelNodeHelperRegistry;
    private NetConfServerImpl m_server;
    private ModelNodeDataStoreManager m_modelNodeDSM;
    private RootModelNodeAggregator m_rootModelNodeAggregator;
    private NbiNotificationHelper m_nbiNotificationHelper;
    private NetconfClientInfo m_clientInfo;

    @Before
    @Override
    public void setup() throws Exception {
        m_schemaRegistry = new SchemaRegistryImpl(getYangTextSchema(), Collections.emptySet(), Collections.emptyMap(), new NoLockService());

        m_modelNodeHelperRegistry = new ModelNodeHelperRegistryImpl(m_schemaRegistry);
        m_subSystemRegistry = new SubSystemRegistryImpl();
        m_subSystemRegistry.setCompositeSubSystem(new CompositeSubSystemImpl());
        m_modelNodeDSM = new InMemoryDSM(m_schemaRegistry);

        m_rootModelNodeAggregator = new RootModelNodeAggregatorImpl(m_schemaRegistry, m_modelNodeHelperRegistry,
                m_modelNodeDSM, m_subSystemRegistry);

        m_server = new NetConfServerImpl(m_schemaRegistry);
        ModelNode jukeboxModelNode = YangUtils.createInMemoryModelNode(getYangFilePaths(),
                new LocalSubSystem(), m_modelNodeHelperRegistry,
                m_subSystemRegistry, m_schemaRegistry, m_modelNodeDSM);
        m_rootModelNodeAggregator.addModelServiceRoot(COMPONENT_ID, jukeboxModelNode);

        m_nbiNotificationHelper = mock(NbiNotificationHelper.class);
        DataStore dataStore = new DataStore(StandardDataStores.RUNNING, m_rootModelNodeAggregator, m_subSystemRegistry);
        dataStore.setNbiNotificationHelper(m_nbiNotificationHelper);
        m_server.setRunningDataStore(dataStore);
        String xmlFilePath = TestUtil.class.getResource("/orderedbyuserforidrefandunion/default-data.xml").getPath();
        YangUtils.loadXmlDataIntoServer(m_server, xmlFilePath);
        m_clientInfo = new NetconfClientInfo(CLIENT_USERNAME, 1);
    }

    @Test
    public void testForEnumKey1AndEnumKey2Present() throws IOException, SAXException {
        String expected = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
                "    <data>\n" +
                "        <test:test-container xmlns:test=\"http://example.com/ns/ordered-by-user\">\n" +
                "        </test:test-container>\n" +
                "    </data>\n" +
                "</rpc-reply>";

        verifyGet(m_server, m_clientInfo, expected);

        String request = "<test-container xmlns=\"http://example.com/ns/ordered-by-user\">\n" +
                "    <order-by-user-mulitple-keys>\n" +
                "        <key1>key1-enum</key1>\n" +
                "        <key2>all</key2>\n" +
                "        <sample-leaf>test</sample-leaf>\n" +
                "    </order-by-user-mulitple-keys>\n" +
                "</test-container>";
        editConfig(m_server, m_clientInfo, request, true);

        expected = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
                "    <data>\n" +
                "    <test-container xmlns=\"http://example.com/ns/ordered-by-user\">\n" +
                "      <order-by-user-mulitple-keys>\n" +
                "         <key1>key1-enum</key1>\n" +
                "         <key2>all</key2>\n" +
                "         <sample-leaf>test</sample-leaf>\n" +
                "      </order-by-user-mulitple-keys>\n" +
                "    </test-container>\n" +
                "    </data>\n" +
                "</rpc-reply>";

        verifyGet(m_server, m_clientInfo, expected);

        request = "<test-container xmlns=\"http://example.com/ns/ordered-by-user\">\n" +
                "    <order-by-user-mulitple-keys\n" +
                "                                 xmlns:yang=\"urn:ietf:params:xml:ns:yang:1\"\n" +
                "                                 yang:insert=\"before\"\n" +
                "                                 yang:key=\"[key1='key1-enum'][key2='all']\">\n" +
                "        <key1>key1-enum</key1>\n" +
                "        <key2>key2-enum1</key2>\n" +
                "        <sample-leaf>test</sample-leaf>\n" +
                "    </order-by-user-mulitple-keys>\n" +
                "</test-container>";
        editConfig(m_server, m_clientInfo, request, true);

        expected = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
                "    <data>\n" +
                "    <test-container xmlns=\"http://example.com/ns/ordered-by-user\">\n" +
                "      <order-by-user-mulitple-keys>\n" +
                "         <key1>key1-enum</key1>\n" +
                "         <key2>key2-enum1</key2>\n" +
                "         <sample-leaf>test</sample-leaf>\n" +
                "      </order-by-user-mulitple-keys>\n" +
                "      <order-by-user-mulitple-keys>\n" +
                "         <key1>key1-enum</key1>\n" +
                "         <key2>all</key2>\n" +
                "         <sample-leaf>test</sample-leaf>\n" +
                "      </order-by-user-mulitple-keys>\n" +
                "    </test-container>\n" +
                "    </data>\n" +
                "</rpc-reply>";

        verifyGet(m_server, m_clientInfo, expected);

        request = "<test-container xmlns=\"http://example.com/ns/ordered-by-user\">\n" +
                "    <order-by-user-mulitple-keys xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\"\n" +
                "                                 xmlns:yang=\"urn:ietf:params:xml:ns:yang:1\"\n" +
                "                                 xmlns:test=\"http://example.com/ns/ordered-by-user\"" +
                "                                 nc:operation=\"create\"\n" +
                "                                 yang:insert=\"after\"\n" +
                "                                 yang:key=\"[test:key1='key1-enum'][key2='all']\">\n" +
                "        <key1 xmlns:test=\"http://example.com/ns/ordered-by-user\">test:idref1</key1>\n" +
                "        <key2>all</key2>\n" +
                "        <sample-leaf>test</sample-leaf>\n" +
                "    </order-by-user-mulitple-keys>\n" +
                "</test-container>";
        editConfig(m_server, m_clientInfo, request, true);

        expected = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
                "    <data>\n" +
                "    <test-container xmlns=\"http://example.com/ns/ordered-by-user\">\n" +
                "      <order-by-user-mulitple-keys>\n" +
                "         <key1>key1-enum</key1>\n" +
                "         <key2>key2-enum1</key2>\n" +
                "         <sample-leaf>test</sample-leaf>\n" +
                "      </order-by-user-mulitple-keys>\n" +
                "      <order-by-user-mulitple-keys>\n" +
                "         <key1>key1-enum</key1>\n" +
                "         <key2>all</key2>\n" +
                "         <sample-leaf>test</sample-leaf>\n" +
                "      </order-by-user-mulitple-keys>\n" +
                "      <order-by-user-mulitple-keys>\n" +
                "         <key1>test:idref1</key1>\n" +
                "         <key2>all</key2>\n" +
                "         <sample-leaf>test</sample-leaf>\n" +
                "      </order-by-user-mulitple-keys>\n" +
                "    </test-container>\n" +
                "    </data>\n" +
                "</rpc-reply>";

        verifyGet(m_server, m_clientInfo, expected);
    }

    @Test
    public void testForIdrefKey1AndEnumKey2Present() throws IOException, SAXException {
        String expected = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
                "    <data>\n" +
                "        <test:test-container xmlns:test=\"http://example.com/ns/ordered-by-user\">\n" +
                "        </test:test-container>\n" +
                "    </data>\n" +
                "</rpc-reply>";

        verifyGet(m_server, m_clientInfo, expected);

        String request = "<test-container xmlns=\"http://example.com/ns/ordered-by-user\">\n" +
                "    <order-by-user-mulitple-keys>\n" +
                "        <key1>idref1</key1>\n" +
                "        <key2>all</key2>\n" +
                "        <sample-leaf>test</sample-leaf>\n" +
                "    </order-by-user-mulitple-keys>\n" +
                "</test-container>";
        editConfig(m_server, m_clientInfo, request, true);

        expected = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
                "    <data>\n" +
                "    <test-container xmlns=\"http://example.com/ns/ordered-by-user\">\n" +
                "      <order-by-user-mulitple-keys>\n" +
                "         <key1>test:idref1</key1>\n" +
                "         <key2>all</key2>\n" +
                "         <sample-leaf>test</sample-leaf>\n" +
                "      </order-by-user-mulitple-keys>\n" +
                "    </test-container>\n" +
                "    </data>\n" +
                "</rpc-reply>";

        verifyGet(m_server, m_clientInfo, expected);

        request = "<test-container xmlns=\"http://example.com/ns/ordered-by-user\">\n" +
                "    <order-by-user-mulitple-keys\n" +
                "                                 xmlns:yang=\"urn:ietf:params:xml:ns:yang:1\"\n" +
                "                                 yang:insert=\"before\"\n" +
                "                                 xmlns:test=\"http://example.com/ns/ordered-by-user\"\n" +
                "                                 yang:key=\"[key1='test:idref1'][key2='all']\">\n" +
                "        <key1>key1-enum</key1>\n" +
                "        <key2>key2-enum1</key2>\n" +
                "        <sample-leaf>test</sample-leaf>\n" +
                "    </order-by-user-mulitple-keys>\n" +
                "</test-container>";
        editConfig(m_server, m_clientInfo, request, true);


        expected = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
                "    <data>\n" +
                "    <test-container xmlns=\"http://example.com/ns/ordered-by-user\">\n" +
                "      <order-by-user-mulitple-keys>\n" +
                "         <key1>key1-enum</key1>\n" +
                "         <key2>key2-enum1</key2>\n" +
                "         <sample-leaf>test</sample-leaf>\n" +
                "      </order-by-user-mulitple-keys>\n" +
                "      <order-by-user-mulitple-keys>\n" +
                "         <key1>test:idref1</key1>\n" +
                "         <key2>all</key2>\n" +
                "         <sample-leaf>test</sample-leaf>\n" +
                "      </order-by-user-mulitple-keys>\n" +
                "    </test-container>\n" +
                "    </data>\n" +
                "</rpc-reply>";

        verifyGet(m_server, m_clientInfo, expected);

        request = "<test-container xmlns=\"http://example.com/ns/ordered-by-user\">\n" +
                "    <order-by-user-mulitple-keys\n" +
                "                                 xmlns:yang=\"urn:ietf:params:xml:ns:yang:1\"\n" +
                "                                 yang:insert=\"after\"\n" +
                "                                 xmlns:test=\"http://example.com/ns/ordered-by-user\"\n" +
                "                                 yang:key=\"[key1='idref1'][key2='all']\">\n" +
                "        <key1>key1-enum</key1>\n" +
                "        <key2>key2-enum1</key2>\n" +
                "        <sample-leaf>test</sample-leaf>\n" +
                "    </order-by-user-mulitple-keys>\n" +
                "</test-container>";
        editConfig(m_server, m_clientInfo, request, true);

        expected = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
                "    <data>\n" +
                "    <test-container xmlns=\"http://example.com/ns/ordered-by-user\">\n" +
                "      <order-by-user-mulitple-keys>\n" +
                "         <key1>test:idref1</key1>\n" +
                "         <key2>all</key2>\n" +
                "         <sample-leaf>test</sample-leaf>\n" +
                "      </order-by-user-mulitple-keys>\n" +
                "      <order-by-user-mulitple-keys>\n" +
                "         <key1>key1-enum</key1>\n" +
                "         <key2>key2-enum1</key2>\n" +
                "         <sample-leaf>test</sample-leaf>\n" +
                "      </order-by-user-mulitple-keys>\n" +
                "    </test-container>\n" +
                "    </data>\n" +
                "</rpc-reply>";

        verifyGet(m_server, m_clientInfo, expected);
    }

    @Test
    public void testForSingleKeyIdRefTypeExists() throws IOException, SAXException {
        String expected = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
                "    <data>\n" +
                "        <test:test-container xmlns:test=\"http://example.com/ns/ordered-by-user\">\n" +
                "        </test:test-container>\n" +
                "    </data>\n" +
                "</rpc-reply>";

        verifyGet(m_server, m_clientInfo, expected);

        String request = "<test-container xmlns=\"http://example.com/ns/ordered-by-user\">\n" +
                "    <order-by-user-single-key>\n" +
                "        <single-key xmlns:sample1=\"http://example.com/ns/sample\">sample1:sample-idref1</single-key>\n" +
                "        <sample-leaf2>test</sample-leaf2>\n" +
                "    </order-by-user-single-key>\n" +
                "</test-container>";
        editConfig(m_server, m_clientInfo, request, true);

        expected = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
                "    <data>\n" +
                "    <test-container xmlns=\"http://example.com/ns/ordered-by-user\">\n" +
                "      <order-by-user-single-key>\n" +
                "         <single-key>sample:sample-idref1</single-key>\n" +
                "         <sample-leaf2>test</sample-leaf2>\n" +
                "      </order-by-user-single-key>\n" +
                "    </test-container>\n" +
                "    </data>\n" +
                "</rpc-reply>";

        verifyGet(m_server, m_clientInfo, expected);

        request = "<test-container xmlns=\"http://example.com/ns/ordered-by-user\" xmlns:sample1=\"http://example.com/ns/sample\">" +
                "    <order-by-user-single-key xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\"\n" +
                "                                 xmlns:yang=\"urn:ietf:params:xml:ns:yang:1\"\n" +
                "                                 nc:operation=\"create\"\n" +
                "                                 yang:insert=\"after\"\n" +
                "                                 yang:key=\"[single-key='sample1:sample-idref1']\">\n" +
                "        <single-key xmlns:sample1=\"http://example.com/ns/sample\">sample1:sample-idref2</single-key>\n" +
                "        <sample-leaf2>test</sample-leaf2>\n" +
                "    </order-by-user-single-key>\n" +
                "</test-container>";
        editConfig(m_server, m_clientInfo, request, true);

        expected = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
                "    <data>\n" +
                "    <test-container xmlns=\"http://example.com/ns/ordered-by-user\">\n" +
                "      <order-by-user-single-key>\n" +
                "         <single-key>sample:sample-idref1</single-key>\n" +
                "         <sample-leaf2>test</sample-leaf2>\n" +
                "      </order-by-user-single-key>\n" +
                "      <order-by-user-single-key>\n" +
                "         <single-key>sample:sample-idref2</single-key>\n" +
                "         <sample-leaf2>test</sample-leaf2>\n" +
                "      </order-by-user-single-key>\n" +
                "    </test-container>\n" +
                "    </data>\n" +
                "</rpc-reply>";

        verifyGet(m_server, m_clientInfo, expected);
    }

    @Test
    public void testForSingleKeyIdRefTypeDefinedInAnotherYangExists() throws IOException, SAXException {
        String expected = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
                "    <data>\n" +
                "        <test:test-container xmlns:test=\"http://example.com/ns/ordered-by-user\">\n" +
                "        </test:test-container>\n" +
                "    </data>\n" +
                "</rpc-reply>";

        verifyGet(m_server, m_clientInfo, expected);

        String request = "<test-container xmlns=\"http://example.com/ns/ordered-by-user\">\n" +
                "    <order-by-user-single-key-idref>\n" +
                "        <single-key-idref xmlns:idref=\"http://example.com/ns/idref-typedef\">idref:idref1-yang</single-key-idref>\n" +
                "        <sample-leaf3>test</sample-leaf3>\n" +
                "    </order-by-user-single-key-idref>\n" +
                "</test-container>";
        editConfig(m_server, m_clientInfo, request, true);

        expected = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
                "    <data>\n" +
                "    <test-container xmlns=\"http://example.com/ns/ordered-by-user\">\n" +
                "      <order-by-user-single-key-idref>\n" +
                "         <single-key-idref>idref:idref1-yang</single-key-idref>\n" +
                "         <sample-leaf3>test</sample-leaf3>\n" +
                "      </order-by-user-single-key-idref>\n" +
                "    </test-container>\n" +
                "    </data>\n" +
                "</rpc-reply>";

        verifyGet(m_server, m_clientInfo, expected);

        request = "<test-container xmlns=\"http://example.com/ns/ordered-by-user\">" +
                "    <order-by-user-single-key-idref xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\"\n" +
                "                                 xmlns:yang=\"urn:ietf:params:xml:ns:yang:1\"\n" +
                "                                 nc:operation=\"create\"\n" +
                "                                 yang:insert=\"after\"\n" +
                "                                 xmlns:idref2=\"http://example.com/ns/idref-typedef\"\n" +
                "                                 yang:key=\"[single-key-idref='idref2:idref1-yang']\">\n" +
                "        <single-key-idref xmlns:idref=\"http://example.com/ns/idref-typedef\">idref:idref2-yang</single-key-idref>\n" +
                "        <sample-leaf3>test</sample-leaf3>\n" +
                "    </order-by-user-single-key-idref>\n" +
                "</test-container>";
        editConfig(m_server, m_clientInfo, request, true);

        expected = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
                "    <data>\n" +
                "    <test-container xmlns=\"http://example.com/ns/ordered-by-user\">\n" +
                "      <order-by-user-single-key-idref>\n" +
                "         <single-key-idref>idref:idref1-yang</single-key-idref>\n" +
                "         <sample-leaf3>test</sample-leaf3>\n" +
                "      </order-by-user-single-key-idref>\n" +
                "      <order-by-user-single-key-idref>\n" +
                "         <single-key-idref>idref:idref2-yang</single-key-idref>\n" +
                "         <sample-leaf3>test</sample-leaf3>\n" +
                "      </order-by-user-single-key-idref>\n" +
                "    </test-container>\n" +
                "    </data>\n" +
                "</rpc-reply>";

        verifyGet(m_server, m_clientInfo, expected);
    }

    @Test
    public void testForSingleKeyEnumExists() throws IOException, SAXException {
        String expected = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
                "    <data>\n" +
                "        <test:test-container xmlns:test=\"http://example.com/ns/ordered-by-user\">\n" +
                "        </test:test-container>\n" +
                "    </data>\n" +
                "</rpc-reply>";

        verifyGet(m_server, m_clientInfo, expected);

        String request = "<test-container xmlns=\"http://example.com/ns/ordered-by-user\">\n" +
                "    <order-by-user-single-key>\n" +
                "        <single-key>key1-enum</single-key>\n" +
                "        <sample-leaf2>test</sample-leaf2>\n" +
                "    </order-by-user-single-key>\n" +
                "</test-container>";
        editConfig(m_server, m_clientInfo, request, true);

        expected = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
                "    <data>\n" +
                "    <test-container xmlns=\"http://example.com/ns/ordered-by-user\">\n" +
                "      <order-by-user-single-key>\n" +
                "         <single-key>key1-enum</single-key>\n" +
                "         <sample-leaf2>test</sample-leaf2>\n" +
                "      </order-by-user-single-key>\n" +
                "    </test-container>\n" +
                "    </data>\n" +
                "</rpc-reply>";

        verifyGet(m_server, m_clientInfo, expected);

        request = "<test-container xmlns=\"http://example.com/ns/ordered-by-user\">\n" +
                "    <order-by-user-single-key xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\"\n" +
                "                                 xmlns:yang=\"urn:ietf:params:xml:ns:yang:1\"\n" +
                "                                 xmlns:sample=\"http://example.com/ns/sample\"" +
                "                                 nc:operation=\"create\"\n" +
                "                                 yang:insert=\"after\"\n" +
                "                                 yang:key=\"[single-key='key1-enum']\">\n" +
                "        <single-key xmlns:sample=\"http://example.com/ns/sample\">sample:sample-idref2</single-key>\n" +
                "        <sample-leaf2>test</sample-leaf2>\n" +
                "    </order-by-user-single-key>\n" +
                "</test-container>";
        editConfig(m_server, m_clientInfo, request, true);

        expected = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
                "    <data>\n" +
                "    <test-container xmlns=\"http://example.com/ns/ordered-by-user\">\n" +
                "      <order-by-user-single-key>\n" +
                "         <single-key>key1-enum</single-key>\n" +
                "         <sample-leaf2>test</sample-leaf2>\n" +
                "      </order-by-user-single-key>\n" +
                "      <order-by-user-single-key>\n" +
                "         <single-key>sample:sample-idref2</single-key>\n" +
                "         <sample-leaf2>test</sample-leaf2>\n" +
                "      </order-by-user-single-key>\n" +
                "    </test-container>\n" +
                "    </data>\n" +
                "</rpc-reply>";

        verifyGet(m_server, m_clientInfo, expected);
    }

    @Test
    public void testErrorThrownForListWith1KeyMissing() throws IOException, SAXException {
        String expected = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
                "    <data>\n" +
                "        <test:test-container xmlns:test=\"http://example.com/ns/ordered-by-user\">\n" +
                "        </test:test-container>\n" +
                "    </data>\n" +
                "</rpc-reply>";

        verifyGet(m_server, m_clientInfo, expected);

        String request = "<test-container xmlns=\"http://example.com/ns/ordered-by-user\">\n" +
                "    <order-by-user-mulitple-keys>\n" +
                "        <key1>key1-enum</key1>\n" +
                "        <key2>all</key2>\n" +
                "        <sample-leaf>test</sample-leaf>\n" +
                "    </order-by-user-mulitple-keys>\n" +
                "</test-container>";
        editConfig(m_server, m_clientInfo, request, true);

        expected = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
                "    <data>\n" +
                "    <test-container xmlns=\"http://example.com/ns/ordered-by-user\">\n" +
                "      <order-by-user-mulitple-keys>\n" +
                "         <key1>key1-enum</key1>\n" +
                "         <key2>all</key2>\n" +
                "         <sample-leaf>test</sample-leaf>\n" +
                "      </order-by-user-mulitple-keys>\n" +
                "    </test-container>\n" +
                "    </data>\n" +
                "</rpc-reply>";

        verifyGet(m_server, m_clientInfo, expected);

        request = "<test-container xmlns=\"http://example.com/ns/ordered-by-user\">\n" +
                "    <order-by-user-mulitple-keys\n" +
                "                                 xmlns:yang=\"urn:ietf:params:xml:ns:yang:1\"\n" +
                "                                 yang:insert=\"before\"\n" +
                "                                 yang:key=\"[key1='key1-enum']\">\n" +
                "        <key1>key1-enum</key1>\n" +
                "        <key2>key2-enum1</key2>\n" +
                "        <sample-leaf>test</sample-leaf>\n" +
                "    </order-by-user-mulitple-keys>\n" +
                "</test-container>";

           NetConfResponse errorResponse = editConfig(m_server, m_clientInfo, request, false);
           expected = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
                   "   <rpc-error>\n" +
                   "      <error-type>application</error-type>\n" +
                   "      <error-tag>bad-attribute</error-tag>\n" +
                   "      <error-severity>error</error-severity>\n" +
                   "      <error-app-tag>missing-instance</error-app-tag>\n" +
                   "      <error-message>Missing key 'key2' in key '[key1='key1-enum']' attribute</error-message>\n" +
                   "      <error-info>\n" +
                   "         <bad-attribute>key</bad-attribute>\n" +
                   "      </error-info>\n" +
                   "   </rpc-error>\n" +
                   "</rpc-reply>";

           assertEquals(expected, errorResponse.responseToString().trim());
    }

    private static List<YangTextSchemaSource> getYangTextSchema() {
        List<String> yangFiles = getYangFilePaths();
        return TestUtil.getByteSources(yangFiles);
    }

    private static List<String> getYangFilePaths() {
        List<String> yangFiles = new ArrayList<>();
        yangFiles.add("/orderedbyuserforidrefandunion/ordered-by-user-for-idref-and-union.yang");
        yangFiles.add("/orderedbyuserforidrefandunion/sample.yang");
        yangFiles.add("/orderedbyuserforidrefandunion/idref-typedef.yang");
        return yangFiles;
    }
}
