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
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;

public class NetconfRpcResponseTest {
    
    private NetconfRpcResponse m_response;
    private RpcName m_rpcName = new RpcName("testName", "test");
    private Element m_outputElement = mock(Element.class);
    
    @Before
    public void setup(){
        when(m_outputElement.getNamespaceURI()).thenReturn("testName");
        when(m_outputElement.getLocalName()).thenReturn("test");
    }
    
    @Test
    public void testRpcOutputElement(){
        m_response = new NetconfRpcResponse();
        m_response.setMessageId("1");
        m_response.setOk(true);
        m_response.addRpcOutputElement(m_outputElement);
        List<Element> rpcOutputElements = m_response.getRpcOutputElements();
        assertEquals(1, rpcOutputElements.size());
        assertEquals(m_outputElement,rpcOutputElements.get(0));
    }
    
    @Test
    public void testRpcName(){
        m_response = new NetconfRpcResponse();
        m_response.setMessageId("1");
        m_response.setOk(true);
        m_response.setRpcName(m_rpcName);
        RpcName name = m_response.getRpcName();
        System.out.println(name);
        assertTrue(name.equals(m_rpcName));
    }
    
    @Test
    public void testType(){
        m_response = new NetconfRpcResponse();
        m_response.setMessageId("1");
        m_response.setOk(true);
        assertTrue(m_response.getType().isResponse());
        assertFalse(m_response.getType().isRequest());
    }
    
    
    @Test
    public void testGetResponseDocument() throws NetconfMessageBuilderException{
        Element element = DocumentUtils.createDocument().createElementNS("test", "test");
        m_response = new NetconfRpcResponse();
        m_response.setMessageId("1");
        m_response.setOk(true);
        m_response.addRpcOutputElement(element);
        Document document = m_response.getResponseDocument();
        Node node = document.getFirstChild();
        if (node instanceof Element){
            Element nodeElement = (Element)node;
            assertTrue(validate(nodeElement,"rpc-reply"));
            assertTrue(validate(nodeElement,"ok"));
            assertTrue(validate(nodeElement,"test"));
            
        }
    }
    
    private boolean validate(Element element,String localName){
        
        if (element.getLocalName().equals(localName)){
            return true;
        }else{
            NodeList nodes = element.getChildNodes();
            for (int i=0;i<nodes.getLength();i++){
                if (nodes.item(i) instanceof Element && ((Element)nodes.item(i)).getLocalName().equals(localName)){
                    return true;
                }
                    
            }
        }
        
        return false;
    }
 
}
