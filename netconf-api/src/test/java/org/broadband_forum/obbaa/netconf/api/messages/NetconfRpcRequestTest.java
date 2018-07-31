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

package org.broadband_forum.obbaa.netconf.api.messages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class NetconfRpcRequestTest {

    private NetconfRpcRequest m_netconfRpcRequest = new NetconfRpcRequest();
    private RpcName m_rpcName = new RpcName("testName", "test");
    private Element m_inputElement = mock(Element.class);

    @Test
    public void testSetAndGetRpcInput() {

        assertEquals(m_netconfRpcRequest, m_netconfRpcRequest.setRpcInput(m_inputElement));
        assertNotNull(m_netconfRpcRequest.getRpcInput());
    }

    @Test
    public void testGetRpcName() {
        m_netconfRpcRequest.setRpcInput(m_inputElement);
        assertNotEquals(m_rpcName, m_netconfRpcRequest.getRpcName());
    }

    @Test
    public void testRpcRequestType() {
        assertTrue(m_netconfRpcRequest.getType().isRequest());
        assertFalse(m_netconfRpcRequest.getType().isResponse());
    }

    @Test
    public void testGetDocument() throws NetconfMessageBuilderException {
        Element element = DocumentUtils.createDocument().createElementNS("test", "test");
        m_netconfRpcRequest.setRpcInput(element);
        Document document = m_netconfRpcRequest.getRequestDocument();
        Node node = document.getFirstChild();
        if (node instanceof Element) {
            assertTrue(((Element) node).getLocalName().equals("rpc"));
            if (node.getFirstChild() instanceof Element) {
                assertTrue(((Element) node.getFirstChild()).getLocalName().equals("test"));
                assertTrue(((Element) node.getFirstChild()).getNamespaceURI().equals("test"));
            }
        }

    }

}
