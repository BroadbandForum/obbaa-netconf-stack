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

package org.broadband_forum.obbaa.netconf.api.client.util;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.net.UnknownHostException;
import java.nio.channels.AsynchronousChannelGroup;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import org.broadband_forum.obbaa.netconf.api.NetconfConfigurationBuilderException;
import org.broadband_forum.obbaa.netconf.api.authentication.AuthenticationListener;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientConfiguration;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientSessionListener;
import org.broadband_forum.obbaa.netconf.api.client.NetconfLoginProvider;
import org.broadband_forum.obbaa.netconf.api.client.NotificationListener;
import org.broadband_forum.obbaa.netconf.api.transport.api.NetconfTransport;

import io.netty.channel.EventLoopGroup;

public class NetconfClientConfigurationBuilderTest {

    @Test
    public void testConfigurationBuild() {
        NetconfClientConfigurationBuilder builder = spy(new NetconfClientConfigurationBuilder());
        NetconfLoginProvider loginProvider = mock(NetconfLoginProvider.class);
        AuthenticationListener authListener = mock(AuthenticationListener.class);
        NetconfTransport transport = mock(NetconfTransport.class);
        Set<String> caps = mock(Set.class);
        NetconfClientSessionListener clientSessListener = mock(NetconfClientSessionListener.class);
        EventLoopGroup evl = mock(EventLoopGroup.class);
        AsynchronousChannelGroup asycChannelGroup = mock(AsynchronousChannelGroup.class);
        builder.setNetconfLoginProvider(loginProvider)
                .setAuthenticationListener(authListener)
                .setTransport(transport)
                .setConnectionTimeout(1000L)
                .setCapabilities(caps)
                .setClientSessionListener(clientSessListener)
                .setEventLoopGroup(evl).setAsynchronousChannelGroup(asycChannelGroup);
        NetconfClientConfiguration config = builder.build();
        assertEquals(loginProvider, config.getNetconfLoginProvider());
        assertEquals(authListener, config.getAuthenticationListener());
        assertEquals(transport, config.getTransport());
        assertEquals(caps, config.getCaps());
        assertEquals(clientSessListener, config.getClientSessionListener());
        assertEquals(evl, config.getEventLoopGroup());
        assertEquals(asycChannelGroup, config.getAsynchronousChannelGroup());

    }
    
    @Test
    public void testDefaultNcClientBuilder() throws UnknownHostException, NetconfConfigurationBuilderException{
    	NetconfClientConfigurationBuilder builder = NetconfClientConfigurationBuilder.createDefaultNcClientBuilder();
    	NotificationListener notificationListener = mock(NotificationListener.class);
        AsynchronousChannelGroup asycChannelGroup = mock(AsynchronousChannelGroup.class);
    	builder.setConnectionTimeout(1000L)
    			.setAsynchronousChannelGroup(asycChannelGroup);
    	NetconfClientConfiguration config = builder.build();
    	config.setNotificationListener(notificationListener);
        config.setAsynchronousChannelGroup(asycChannelGroup);
        
    	assertNotNull(config.toString());
        assertEquals(notificationListener, config.getNotificationListener());
        assertEquals(asycChannelGroup, config.getAsynchronousChannelGroup());
        assertEquals(new Long(1000L), config.getConnectTimeoutMillis());
    }

    @Test
    public void testAddCapability() throws UnknownHostException, NetconfConfigurationBuilderException{
        NetconfClientConfigurationBuilder builder = NetconfClientConfigurationBuilder.createDefaultNcClientBuilder();
        AsynchronousChannelGroup asycChannelGroup = mock(AsynchronousChannelGroup.class);
        builder.setConnectionTimeout(1000L)
                .setAsynchronousChannelGroup(asycChannelGroup);
        builder.addCapability("cap1").addCapability("cap2").addCapability("cap1");
        Set<String> expectedCaps = new HashSet<>();
        expectedCaps.add("cap2");
        expectedCaps.add("cap1");
        Set<String> actualCaps = builder.build().getCaps();
        assertEquals(expectedCaps, actualCaps);
    }

}
