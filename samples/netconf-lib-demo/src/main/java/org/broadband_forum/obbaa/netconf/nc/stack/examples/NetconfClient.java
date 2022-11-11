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

package org.broadband_forum.obbaa.netconf.nc.stack.examples;

import static org.broadband_forum.obbaa.netconf.nc.stack.examples.SshNetconfClient.getSampleClientCaps;

import java.io.File;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.broadband_forum.obbaa.netconf.api.NetconfConfigurationBuilderException;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientConfiguration;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientDispatcher;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientDispatcherException;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientSession;
import org.broadband_forum.obbaa.netconf.api.client.NetconfLoginProvider;
import org.broadband_forum.obbaa.netconf.api.client.util.NetconfClientConfigurationBuilder;
import org.broadband_forum.obbaa.netconf.api.messages.AbstractNetconfRequest;
import org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.transport.NetconfTransportFactory;
import org.broadband_forum.obbaa.netconf.api.transport.NetconfTransportOrder;
import org.broadband_forum.obbaa.netconf.api.transport.NetconfTransportProtocol;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.client.dispatcher.NetconfClientDispatcherImpl;
import org.broadband_forum.obbaa.netconf.client.ssh.auth.PasswordLoginProvider;
import org.w3c.dom.Document;

public class NetconfClient {

    private final String m_host;
    private final Integer m_port;
    private final String m_username;
    private final String m_password;
    private final File m_file;

    public NetconfClient(String host, Integer port, String username, String password, File file) {
        m_host = host;
        m_port = port;
        m_username = username;
        m_password = password;
        m_file = file;
    }

    public static void main(String [] args) throws Exception {
        Options options = new Options();
        options.addRequiredOption("h", "host", true,"Hostname/IP address of the netconf server");
        options.addOption("p", "port", true,"Netconf port of netconf server, 830 is default");
        options.addOption("u", "username", true,"username to login to the netconf server, 'adminuser' is default");
        options.addOption("pw", "password", true,"password to login to the netconf serverm 'password' is default");
        options.addRequiredOption("rp", "rpc-file-path", true, "Absolute file path of the RPC xml file");
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);
            String host = cmd.getOptionValue("h");
            Integer port = Integer.valueOf(cmd.getOptionValue("p", "830"));
            String username = cmd.getOptionValue("u", "adminuser");
            String pass = cmd.getOptionValue("pw", "password");
            String rpcFile = cmd.getOptionValue("rp");

            NetconfClient client = new NetconfClient(host, port, username, pass, new File(rpcFile));
            client.run();
            System.exit(0);
        } catch(ParseException e){
            //ignore
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "java -jar netconf-client.jar ", options);
            System.exit(1);
        }

    }

    public void run() throws Exception {

        NetconfClientSession session = null;
        try {
            session = getSession();
            Document doc = DocumentUtils.getDocFromFile(m_file);
            AbstractNetconfRequest rpc = DocumentToPojoTransformer.getRpcRequest(doc) ;
            rpc.setReplyTimeout(Long.MAX_VALUE);
            NetConfResponse netConfResponse = session.sendRpc(rpc).get();
            if(netConfResponse != null) {
                System.out.println("Got response \n" + netConfResponse.responseToString()); //NOSONAR
            } else {
                System.out.println("Got null response"); //NOSONAR
            }
        } finally {
            if(session != null){
                session.close();
            }
        }

    }

    public NetconfClientSession getSession() throws UnknownHostException, NetconfConfigurationBuilderException, NetconfClientDispatcherException, InterruptedException, ExecutionException {
        NetconfClientDispatcher dispatcher = new NetconfClientDispatcherImpl();
        NetconfLoginProvider authorizationProvider = new PasswordLoginProvider(m_username, m_password);
        NetconfTransportOrder clientTransportOrder = new NetconfTransportOrder();
        clientTransportOrder.setServerSocketAddress(new InetSocketAddress(InetAddress.getByName(m_host), m_port));
        clientTransportOrder.setTransportType(NetconfTransportProtocol.SSH.name());

        Set<String> clientCaps = getSampleClientCaps();
        LoggingClientSessionListener listener = new LoggingClientSessionListener();
        NetconfClientConfiguration clientConfig = new NetconfClientConfigurationBuilder()
                .setNetconfLoginProvider(authorizationProvider) //provide username and password login
                .setTransport(NetconfTransportFactory.makeNetconfTransport(clientTransportOrder))
                .setCapabilities(clientCaps)
                .setClientSessionListener(listener).build(); //Callback for session close event
        Future<NetconfClientSession> futureSession = dispatcher.createClient(clientConfig);
        return futureSession.get();
    }
}
