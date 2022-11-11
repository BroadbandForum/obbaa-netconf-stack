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

package org.broadband_forum.obbaa.netconf.mn.fwk.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Provider;
import java.security.Security;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.FileUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.broadband_forum.obbaa.netconf.api.LogAppNames;
import org.broadband_forum.obbaa.netconf.api.NetconfConfigurationBuilderException;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.AbstractNetconfRequest;
import org.broadband_forum.obbaa.netconf.api.messages.ActionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.ActionResponse;
import org.broadband_forum.obbaa.netconf.api.messages.CloseSessionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.CopyConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.DeleteConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.GetRequest;
import org.broadband_forum.obbaa.netconf.api.messages.KillSessionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.LockRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcResponse;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.api.messages.UnLockRequest;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerConfigurationBuilder;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerDispatcher;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerDispatcherException;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerMessageListener;
import org.broadband_forum.obbaa.netconf.api.server.NetconfServerSession;
import org.broadband_forum.obbaa.netconf.api.server.ResponseChannel;
import org.broadband_forum.obbaa.netconf.api.server.auth.AuthenticationResult;
import org.broadband_forum.obbaa.netconf.api.server.auth.ClientAuthenticationInfo;
import org.broadband_forum.obbaa.netconf.api.transport.NetconfTransportFactory;
import org.broadband_forum.obbaa.netconf.api.transport.NetconfTransportOrder;
import org.broadband_forum.obbaa.netconf.api.transport.NetconfTransportProtocol;
import org.broadband_forum.obbaa.netconf.api.util.ExecutorServiceProvider;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.CompositeSubSystemImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.DataStore;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NetConfServerImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregatorImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.inmemory.InMemoryDSM;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.LocalSubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.YangUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.broadband_forum.obbaa.netconf.server.dispatcher.NetconfServerDispatcherImpl;
import org.broadband_forum.obbaa.netconf.server.ssh.SshServerDispatcherImpl;
import org.broadband_forum.obbaa.netconf.server.ssh.auth.PasswordAuthHandler;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.w3c.dom.Document;

import junit.framework.TestCase;

@RunWith(RequestScopeJunitRunner.class)
public class SshNetconfServerTest {
    private ModelNodeHelperRegistry m_modelNodeHelperRegistry;
    private SubSystemRegistry m_subSystemRegistry;
    private SchemaRegistry m_schemaRegistry;
    private String m_componentId = "test";

    @Before
    public void setUp() throws SchemaBuildException {
        m_subSystemRegistry = new SubSystemRegistryImpl();
        m_subSystemRegistry.setCompositeSubSystem(new CompositeSubSystemImpl());
        m_schemaRegistry = new SchemaRegistryImpl(Collections.<YangTextSchemaSource>emptyList(), Collections.emptySet(), Collections.emptyMap(), new NoLockService());
        m_modelNodeHelperRegistry = new ModelNodeHelperRegistryImpl(m_schemaRegistry);
    }

    @Test
    public void testServerRuns() throws NetconfServerDispatcherException, InterruptedException, NetconfConfigurationBuilderException,
            ExecutionException, IOException, EditException, SchemaBuildException {
        // the actual model

        NetConfServerImpl server = new NetConfServerImpl(m_schemaRegistry);
        m_schemaRegistry = new SchemaRegistryImpl(TestUtil.getJukeBoxYangs(), Collections.emptySet(), Collections.emptyMap(), new NoLockService());

        RootModelNodeAggregator rootModelNodeAggregator = new RootModelNodeAggregatorImpl(m_schemaRegistry, m_modelNodeHelperRegistry,
                mock(ModelNodeDataStoreManager.class), m_subSystemRegistry).addModelServiceRoot(m_componentId, null);
        server.setRunningDataStore(new DataStore(StandardDataStores.RUNNING, rootModelNodeAggregator, m_subSystemRegistry));
        testServer(server, true);
    }

    @Test
    public void testServerRunsOnYang() throws NetconfServerDispatcherException, InterruptedException, NetconfConfigurationBuilderException,
            ExecutionException, IOException, EditException, ModelNodeInitException, SchemaBuildException {
        // the actual model
        NetConfServerImpl server = new NetConfServerImpl(m_schemaRegistry);
        String yangFilePath = SshNetconfServerTest.class.getResource("/yangs/example-jukebox.yang").getPath();
        m_schemaRegistry.loadSchemaContext(m_componentId, TestUtil.getJukeBoxYangs(), Collections.emptySet(), Collections.emptyMap());
        ModelNode jukeboxModelNode = YangUtils.createInMemoryModelNode(yangFilePath, new LocalSubSystem(), m_modelNodeHelperRegistry,
                m_subSystemRegistry, m_schemaRegistry, new InMemoryDSM(m_schemaRegistry));
        RootModelNodeAggregator rootModelNodeAggregator = new RootModelNodeAggregatorImpl(m_schemaRegistry, m_modelNodeHelperRegistry,
                mock(ModelNodeDataStoreManager.class), m_subSystemRegistry).addModelServiceRoot(m_componentId, jukeboxModelNode);
        server.setRunningDataStore(new DataStore(StandardDataStores.RUNNING, rootModelNodeAggregator, m_subSystemRegistry));
        testServer(server, false);
    }

    private void testServer(NetConfServerImpl server, boolean hostkeyExisting)
            throws UnknownHostException, IOException,
            NetconfConfigurationBuilderException, InterruptedException,
            ExecutionException, NetconfServerDispatcherException {
        // start a server
        HashSet<String> serverCaps = new HashSet<String>();
        serverCaps.add(NetconfResources.NETCONF_BASE_CAP_1_0);
        serverCaps.add(NetconfResources.NETCONF_WRITABLE_RUNNNG);
        NetconfServerConfigurationBuilder builder = NetconfServerConfigurationBuilder.createDefaultNcServerBuilder(NetconfResources.DEFAULT_SSH_CONNECTION_PORT)
                .setAuthenticationHandler(new UTAuthHandler()).setNetconfServerMessageListener(server).setCapabilities(serverCaps);

        String tmpDir = System.getProperty("java.io.tmpdir") + File.separator + System.getProperty("user.name") + "_" + System.currentTimeMillis();
        String hostKeyPath = tmpDir + File.separator + "hostkey";
        FileUtils.deleteDirectory(new File(tmpDir));

        String defaultKey = null;
        if (hostkeyExisting) {
            defaultKey = this.getClass().getClassLoader().getResource("serverprv.key").getPath();
            FileUtils.copyFile(new File(defaultKey), new File(hostKeyPath));
        }

        try {
            InetSocketAddress defaultServerSocketAddress = new InetSocketAddress(InetAddress.getLocalHost(), findFreePort());
            NetconfTransportOrder transportOrder = new NetconfTransportOrder();
            transportOrder.setTransportType(NetconfTransportProtocol.SSH.name());
            transportOrder.setServerSocketAddress(defaultServerSocketAddress);
            transportOrder.setServerSshHostKeyPath(hostKeyPath);
            builder.setTransport(NetconfTransportFactory.makeNetconfTransport(transportOrder));


            NetconfServerDispatcher dispatcher = new NetconfServerDispatcherImpl(ExecutorServiceProvider.getInstance().getExecutorService());
            NetconfServerSession s = dispatcher.createServer(builder.build()).get();

            assertTrue(Files.exists(Paths.get(hostKeyPath)));
            String defaultKeyValue = null;
            if (hostkeyExisting) {
                defaultKeyValue = FileUtils.readFileToString(new File(defaultKey));
            } else {
                InputStream stream = SshServerDispatcherImpl.class.getClassLoader().getResourceAsStream("hostkey");
                defaultKeyValue = readFromInputStream(stream);
            }
            String hostkeyValue = FileUtils.readFileToString(new File(hostKeyPath));
            assertEquals(defaultKeyValue, hostkeyValue);
            Provider provider = Security.getProvider(BouncyCastleProvider.PROVIDER_NAME);
            TestCase.assertEquals(BouncyCastleProvider.PROVIDER_NAME, provider.getName());
            // finally kill the server
            s.killServer(true);
        } finally {
            FileUtils.deleteDirectory(new File(tmpDir));
        }
    }

    private String readFromInputStream(InputStream inputStream) throws IOException {
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        }
        return resultStringBuilder.toString();
    }

    private int findFreePort() throws IOException {
        ServerSocket socket = new ServerSocket(0);
        int port = socket.getLocalPort();
        socket.close();
        return port;
    }

    public final class UTAuthHandler extends PasswordAuthHandler {
        @Override
        public AuthenticationResult authenticate(ClientAuthenticationInfo clientAuthInfo) {
            if ("UT".equals(clientAuthInfo.getUsername()) && "UT".equals(clientAuthInfo.getPassword())) {
                return new AuthenticationResult(true, null);
            }
            return AuthenticationResult.failedAuthResult();
        }

    }

    public final static class UTServerMessageListener implements NetconfServerMessageListener {

        private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(UTServerMessageListener.class, LogAppNames.NETCONF_LIB);

        @Override
        public void onHello(NetconfClientInfo info, Set<String> clientCaps) {
            LOGGER.info(clientCaps.toString());

        }

        @Override
        public void onGet(NetconfClientInfo info, GetRequest req, NetConfResponse resp) {
            getDummyOkResponse(req, resp);

        }

        private void getDummyOkResponse(AbstractNetconfRequest req, NetConfResponse resp) {
            resp.setOk(true);

        }

        @Override
        public void onGetConfig(NetconfClientInfo info, GetConfigRequest req, NetConfResponse resp) {
            getDummyOkResponse(req, resp);

        }

        @Override
        public List<Notification> onEditConfig(NetconfClientInfo info, EditConfigRequest req, NetConfResponse resp) {
            getDummyOkResponse(req, resp);
            return null;

        }

        @Override
        public void onCopyConfig(NetconfClientInfo info, CopyConfigRequest req, NetConfResponse resp) {
            getDummyOkResponse(req, resp);

        }

        @Override
        public void onDeleteConfig(NetconfClientInfo info, DeleteConfigRequest req, NetConfResponse resp) {
            getDummyOkResponse(req, resp);

        }

        @Override
        public void onLock(NetconfClientInfo info, LockRequest req, NetConfResponse resp) {
            getDummyOkResponse(req, resp);

        }

        @Override
        public void onUnlock(NetconfClientInfo info, UnLockRequest req, NetConfResponse resp) {
            getDummyOkResponse(req, resp);

        }

        @Override
        public void onCloseSession(NetconfClientInfo info, CloseSessionRequest req, NetConfResponse resp) {
            getDummyOkResponse(req, resp);

        }

        @Override
        public void onKillSession(NetconfClientInfo info, KillSessionRequest req, NetConfResponse resp) {
            getDummyOkResponse(req, resp);

        }

        @Override
        public void onInvalidRequest(NetconfClientInfo info, Document req, NetConfResponse resp) {

        }

        @Override
        public void sessionClosed(String arg0, int sessionId) {

        }

        @Override
        public List<Notification> onRpc(NetconfClientInfo info, NetconfRpcRequest rpcRequest, NetconfRpcResponse response) {
            return null;
        }

        @Override
        public void onCreateSubscription(NetconfClientInfo info,
                                         NetconfRpcRequest req, ResponseChannel responseChannel) {

        }

        @Override
        public void onAction(NetconfClientInfo info, ActionRequest req, ActionResponse resp) {

        }
    }
}


