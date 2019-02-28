package org.broadband_forum.obbaa.netconf.mn.fwk.tests;

import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.w3c.dom.Document;

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
import org.broadband_forum.obbaa.netconf.api.util.ExecutorServiceProvider;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.server.dispatcher.NetconfServerDispatcherImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.DataStore;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NetConfServerImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.jukebox2.Artist;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.jukebox2.Jukebox;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.jukebox2.Library;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregatorImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.inmemory.InMemoryDSM;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.LocalSubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.YangUtils;
import org.broadband_forum.obbaa.netconf.server.ssh.auth.PasswordAuthHandler;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;

public class SshNetconfServerTest {
    private ModelNodeHelperRegistry m_modelNodeHelperRegistry;
    private SubSystemRegistry m_subSystemRegistry;
    private SchemaRegistry m_schemaRegistry;
    private String m_componentId = "test";

    @Before
    public void setUp() throws SchemaBuildException {
        m_subSystemRegistry = new SubSystemRegistryImpl();
        m_schemaRegistry = new SchemaRegistryImpl(Collections.<YangTextSchemaSource>emptyList(), Collections.emptySet(), Collections.emptyMap(), new NoLockService());
        m_modelNodeHelperRegistry = new ModelNodeHelperRegistryImpl(m_schemaRegistry);
    }

    @Test
    public void testServerRuns() throws NetconfServerDispatcherException, InterruptedException, NetconfConfigurationBuilderException,
            ExecutionException, IOException, EditException, SchemaBuildException {
        TestUtil.registerClasses(m_modelNodeHelperRegistry);
        // the actual model

        NetConfServerImpl server = new NetConfServerImpl(m_schemaRegistry);
        m_schemaRegistry = new SchemaRegistryImpl(TestUtil.getJukeBoxYangs(), Collections.emptySet(), Collections.emptyMap(), new NoLockService());
        ModelNode jukeBoxModel = createJukeBoxModel();

        RootModelNodeAggregator rootModelNodeAggregator = new RootModelNodeAggregatorImpl(m_schemaRegistry, m_modelNodeHelperRegistry,
                mock(ModelNodeDataStoreManager.class), m_subSystemRegistry).addModelServiceRoot(m_componentId, jukeBoxModel);
        server.setRunningDataStore(new DataStore(StandardDataStores.RUNNING, rootModelNodeAggregator, m_subSystemRegistry));
        testServer(server);
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
        testServer(server);
    }

    private void testServer(NetConfServerImpl server)
            throws UnknownHostException, IOException,
            NetconfConfigurationBuilderException, InterruptedException,
            ExecutionException, NetconfServerDispatcherException {
        // start a server
        HashSet<String> serverCaps = new HashSet<String>();
        serverCaps.add(NetconfResources.NETCONF_BASE_CAP_1_0);
        serverCaps.add(NetconfResources.NETCONF_WRITABLE_RUNNNG);
        NetconfServerConfigurationBuilder builder = NetconfServerConfigurationBuilder.createDefaultNcServerBuilder(findFreePort())
                .setAuthenticationHandler(new UTAuthHandler()).setNetconfServerMessageListener(server).setCapabilities(serverCaps);

        NetconfServerDispatcher dispatcher = new NetconfServerDispatcherImpl(ExecutorServiceProvider.getInstance().getExecutorService());
        NetconfServerSession s = dispatcher.createServer(builder.build()).get();

        // finally kill the server
        s.killServer(true);
    }

    private int findFreePort() throws IOException {
        ServerSocket socket = new ServerSocket(0);
        int port = socket.getLocalPort();
        socket.close();
        return port;
    }

    private ModelNode createJukeBoxModel() throws EditException {
        Jukebox jukebox = new Jukebox(null, new ModelNodeId(), m_modelNodeHelperRegistry, m_subSystemRegistry, m_schemaRegistry);
        jukebox.setLibrary(new Library(jukebox, new ModelNodeId(jukebox.getModelNodeId()), m_modelNodeHelperRegistry, m_subSystemRegistry, m_schemaRegistry));
        Artist lenny = jukebox.getLibrary().addArtist("Lenny");

        lenny.addAlbum("Greatest hits").addSong("Are you gonne go my way").addSong("Fly Away");
        lenny.addAlbum("Circus").addSong("Rock and roll is dead").addSong("Circus").addSong("Beyond the 7th Sky");

        return jukebox;
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

        private static final Logger LOGGER = Logger.getLogger(UTServerMessageListener.class);

        @Override
        public void onHello(NetconfClientInfo info, Set<String> clientCaps) {
            LOGGER.info(clientCaps);

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

