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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.yang;

import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.YangUtils.loadXmlDataIntoServer;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.assertXMLEquals;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.loadAsXml;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.sendEditConfig;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.verifyGetConfigWithPassword;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.util.Collections;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.api.util.CryptUtil2;
import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.CompositeSubSystemImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.DataStore;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NbiNotificationHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NbiNotificationHelperImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NetConfServerImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.dom.YinAnnotationService;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregatorImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.inmemory.InMemoryDSM;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.LocalSubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.YangUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcPayloadConstraintParser;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

@RunWith(RequestScopeJunitRunner.class)
public class PasswordHandlingTest {
    public static final String YANG_PATH = TestUtil.class.getResource("/yangs/example-jukebox-with-is-password-ext.yang").getPath();
    public static final String COMPONENT_ID = "jukebox";
    public static final String MESSAGE_ID = "1";
    public static final SchemaPath PASSWORD_SP = SchemaPathBuilder.fromString(
            "(http://example.com/ns/example-jukebox-with-is-password-ext?revision=2014-07-03)"
            +  "jukebox,library,artist,password-annotation");
    private NetConfServerImpl m_server;
    private SubSystemRegistry m_subSystemRegistry;
    private RootModelNodeAggregator m_rootModelNodeAggregator;
    private SchemaRegistryImpl m_schemaRegistry;
    private ModelNodeWithAttributes m_modelWithAttributes;
    private InMemoryDSM m_modelNodeDsm;
    private ModelNodeHelperRegistry m_modelNodeHelperRegistry;
    private LocalSubSystem m_testSubSystem;
    private NetconfClientInfo m_clientInfo;

    @Before
    public void setup() throws Exception {
        m_schemaRegistry = new SchemaRegistryImpl(Collections.emptyList(), Collections.emptySet(), Collections.emptyMap(), new NoLockService());
        m_schemaRegistry.loadSchemaContext(COMPONENT_ID, TestUtil.getByteSources(TestUtil.getJukeBoxYangFileWithPassword()), Collections.emptySet(), Collections.emptyMap());
        m_server = new NetConfServerImpl(m_schemaRegistry, mock(RpcPayloadConstraintParser.class));
        m_modelNodeHelperRegistry = new ModelNodeHelperRegistryImpl(m_schemaRegistry);
        m_subSystemRegistry = new SubSystemRegistryImpl();
        m_testSubSystem = spy(new LocalSubSystem());
        m_modelNodeDsm = new InMemoryDSM(m_schemaRegistry);
        m_modelWithAttributes = YangUtils.createInMemoryModelNode(YANG_PATH, m_testSubSystem,
                m_modelNodeHelperRegistry, m_subSystemRegistry, m_schemaRegistry, m_modelNodeDsm);
        m_subSystemRegistry.setCompositeSubSystem(new CompositeSubSystemImpl());
        m_rootModelNodeAggregator = new RootModelNodeAggregatorImpl(m_schemaRegistry, m_modelNodeHelperRegistry, m_modelNodeDsm, m_subSystemRegistry)
                .addModelServiceRoot(COMPONENT_ID, m_modelWithAttributes);
        DataStore dataStore = new DataStore(StandardDataStores.RUNNING, m_rootModelNodeAggregator, m_subSystemRegistry);
        NbiNotificationHelper nbiNotificationHelper = new NbiNotificationHelperImpl();
        dataStore.setNbiNotificationHelper(nbiNotificationHelper);
        m_server.setRunningDataStore(dataStore);
        String keyFilePath = getClass().getResource("/domvisitortest/keyfile.plain").getPath();
        CryptUtil2 cryptUtil2 = new CryptUtil2();
        cryptUtil2.setKeyFilePathForTest(keyFilePath);
        cryptUtil2.initFile();
        m_clientInfo = new NetconfClientInfo("unit-test", 1);

        YinAnnotationService yinAnnotationService = (schemaNode, mountPoint) -> PASSWORD_SP.equals(schemaNode.getPath()) ? true : false;
        m_schemaRegistry.setYinAnnotationService(yinAnnotationService);
    }

    @Test
    public void testPasswordCreation_AsListKey() throws Exception {
        loadXmlDataIntoServer(m_server,PasswordHandlingTest.class.getResource("/passwordhandlingtest/create-password-under-artist.xml").getPath());
        verifyGetConfigWithPassword(m_server, null, "/passwordhandlingtest/create-password-under-artist-response.xml", MESSAGE_ID);

        // Not working case
        NetConfResponse failedResponse = sendEditConfig(m_server, m_clientInfo, loadAsXml("/passwordhandlingtest/create-password-under-artist-samekey.xml"), MESSAGE_ID);
        assertXMLEquals("/data-exists-error-album-with-password.xml", failedResponse);
    }

    @Test
    public void testPasswordDeletion_WithLeafValueSpecified() throws Exception {
        loadXmlDataIntoServer(m_server,PasswordHandlingTest.class.getResource("/passwordhandlingtest/create-password-under-album.xml").getPath());
        verifyGetConfigWithPassword(m_server, null, "/passwordhandlingtest/create-password-under-album-response.xml", MESSAGE_ID);

        //artist key works with encrypted value only
        sendEditConfig(m_server, m_clientInfo, loadAsXml("/passwordhandlingtest/delete-password-under-album.xml"), MESSAGE_ID);
        verifyGetConfigWithPassword(m_server, null, "/passwordhandlingtest/delete-password-under-album-response.xml", MESSAGE_ID);
    }

    @Test
    public void testPasswordDeletion_WithLeafNotSpecified() throws Exception {
        loadXmlDataIntoServer(m_server,PasswordHandlingTest.class.getResource("/passwordhandlingtest/create-password-under-album.xml").getPath());
        verifyGetConfigWithPassword(m_server, null, "/passwordhandlingtest/create-password-under-album-response.xml", MESSAGE_ID);

        //artist key works with encrypted value only
        sendEditConfig(m_server, m_clientInfo, loadAsXml("/passwordhandlingtest/delete-password-under-album-novalue.xml"), MESSAGE_ID);
        verifyGetConfigWithPassword(m_server, null, "/passwordhandlingtest/delete-password-under-album-response.xml", MESSAGE_ID);
    }

    @Test
    public void testPasswordCreationDeletion_AsLeafList() throws Exception {
        loadXmlDataIntoServer(m_server,PasswordHandlingTest.class.getResource("/passwordhandlingtest/create-password-leaflists-under-album.xml").getPath());
        verifyGetConfigWithPassword(m_server, null, "/passwordhandlingtest/create-password-leaflists-under-album-response.xml", MESSAGE_ID);

        // add few leaf-lists
        sendEditConfig(m_server, m_clientInfo, loadAsXml("/passwordhandlingtest/add-password-leaflist-under-album.xml"), MESSAGE_ID);
        verifyGetConfigWithPassword(m_server, null, "/passwordhandlingtest/add-password-leaflist-under-album-response.xml", MESSAGE_ID);

        // delete one leaf-list
        sendEditConfig(m_server, m_clientInfo, loadAsXml("/passwordhandlingtest/delete-password-leaflist-under-album.xml"), MESSAGE_ID);
        verifyGetConfigWithPassword(m_server, null, "/passwordhandlingtest/delete-password-leaflist-under-album-response.xml", MESSAGE_ID);
    }

    @Test
    public void testGetConfig_PasswordAsAnnotation() throws Exception {
        loadXmlDataIntoServer(m_server,PasswordHandlingTest.class.getResource("/passwordhandlingtest/create-password-annotation-under-album.xml").getPath());
        verifyGetConfigWithPassword(m_server, null, "/passwordhandlingtest/create-password-annotation-under-album-response.xml", MESSAGE_ID);
    }
}
