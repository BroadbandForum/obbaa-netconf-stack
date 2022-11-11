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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcRequest;
import org.broadband_forum.obbaa.netconf.api.messages.RpcName;
import org.broadband_forum.obbaa.netconf.server.rpc.MultiRpcRequestHandler;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcRequestHandler;

/**
 * Created by pgorai on 1/27/16.
 */
public class RpcRequestHandlerRegistryTest {
    public static final String TEST_COMPONENT2 = "test-component2";
    public static final String TEST_COMPONENT = "test-component";
    private RpcRequestHandlerRegistry m_registry;

    private RpcName m_rpcName;
    private RpcRequestHandler m_rpcHanlder;
    private RpcName m_rpcName2;
    private RpcRequestHandler m_rpcHanlder2;
    private RpcName m_rpcName3;
    private RpcRequestHandler m_rpcHanlder3;
    private RpcName m_rpcName4;
    private RpcRequestHandler m_rpcHanlder4;

    @Before
    public void setUp(){
        m_registry = new RpcRequestHandlerRegistryImpl();

        m_rpcHanlder = mock(RpcRequestHandler.class);
        m_rpcName = new RpcName("my-ns", "my-rpc");
        when(m_rpcHanlder.getRpcQName()).thenReturn(m_rpcName);

        m_rpcName2 = new RpcName("my-ns2", "my-rpc2");
        m_rpcHanlder2 = mock(RpcRequestHandler.class);
        when(m_rpcHanlder2.getRpcQName()).thenReturn(m_rpcName2);

        m_rpcName3 = new RpcName("my-ns3", "my-rpc3");
        m_rpcHanlder3 = mock(RpcRequestHandler.class);
        when(m_rpcHanlder3.getRpcQName()).thenReturn(m_rpcName3);

        m_rpcName4 = new RpcName("my-ns4", "my-rpc4");
        m_rpcHanlder4 = mock(RpcRequestHandler.class);
        when(m_rpcHanlder4.getRpcQName()).thenReturn(m_rpcName4);
    }

    @Test
    public void testUndeploy(){
        m_registry.register(TEST_COMPONENT, m_rpcName, m_rpcHanlder);
        m_registry.register(TEST_COMPONENT, m_rpcName2, m_rpcHanlder2);
        m_registry.register(TEST_COMPONENT2, m_rpcName3, m_rpcHanlder3);
        m_registry.register(TEST_COMPONENT2, m_rpcName4, m_rpcHanlder4);

        assertEquals(m_rpcHanlder, m_registry.lookupRpcRequestHandler(m_rpcName));
        assertEquals(m_rpcHanlder2, m_registry.lookupRpcRequestHandler(m_rpcName2));
        assertEquals(m_rpcHanlder3, m_registry.lookupRpcRequestHandler(m_rpcName3));
        assertEquals(m_rpcHanlder4, m_registry.lookupRpcRequestHandler(m_rpcName4));

        m_registry.undeploy(TEST_COMPONENT2);
        assertNull(m_registry.lookupRpcRequestHandler(m_rpcName4));
        assertNull(m_registry.lookupRpcRequestHandler(m_rpcName3));

        assertNotNull(m_registry.lookupRpcRequestHandler(m_rpcName2));
        assertNotNull(m_registry.lookupRpcRequestHandler(m_rpcName));

        m_registry.undeploy(TEST_COMPONENT);
        assertNull(m_registry.lookupRpcRequestHandler(m_rpcName4));
        assertNull(m_registry.lookupRpcRequestHandler(m_rpcName3));

        assertNull(m_registry.lookupRpcRequestHandler(m_rpcName2));
        assertNull(m_registry.lookupRpcRequestHandler(m_rpcName));

    }

    @Test
    public void testRetMultiRpcRequestHandlerInstance(){
        MultiRpcRequestHandler rpcHandler = mock(MultiRpcRequestHandler.class);
        NetconfRpcRequest request = mock(NetconfRpcRequest.class);
        RpcName rpcName = new RpcName("http://www.test-company.com/solutions/anv-plug", "mapped-device-rpc");
        when(request.getRpcName()).thenReturn(rpcName);
        when(rpcHandler.checkForRpcSupport(rpcName)).thenReturn("http://www.test-company.com/solutions/anv-plug");
        m_registry.registerMultiRpcRequestHandler(rpcHandler);
        MultiRpcRequestHandler actualHandler = m_registry.getMultiRpcRequestHandler(request);
        assertNotNull(actualHandler);
        rpcName = new RpcName("http://www.test-company.com/solutions/it-is-not-a-mapped-one", "mapped-device-rpc");
        when(request.getRpcName()).thenReturn(rpcName);
        actualHandler = m_registry.getMultiRpcRequestHandler(request);
        assertNull(actualHandler);
    }
}
