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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.validation;

import static org.junit.Assert.assertTrue;

import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(RequestScopeJunitRunner.class)
public class IdentityRefTest extends AbstractDataStoreValidatorTest{

    @Test
    public void testChoiceCaseIdentityandDefault() throws Exception {
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                            +"<identityrefContainer xmlns=\"urn:org:bbf2:pma:identityref-test\">"
                            +"    <case1Leaf>identity4</case1Leaf>"
                            +"    <default-leaf>identity5</default-leaf>"
                            +"</identityrefContainer>";
        getModelNode();
        EditConfigRequest request1 = createRequestFromString(requestXml1);
        request1.setMessageId("1");
        NetConfResponse response1 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request1, response1);        
        assertTrue(response1.isOk());
        
        String response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <idref:identityrefContainer xmlns:idref=\"urn:org:bbf2:pma:identityref-test\">"
                + "   <idref:case1Leaf>idref:identity4</idref:case1Leaf>"
                + "   <idref:default-leaf>idref:identity5</idref:default-leaf>"
                + "   </idref:identityrefContainer>"                
                + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + " </data>"
                + " </rpc-reply>"
                ;
        verifyGet(response);
        
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                      +"<identityrefContainer xmlns=\"urn:org:bbf2:pma:identityref-test\" >"
                      +"    <default-leaf>identity4</default-leaf>"
                      +"    <identityList>"
                      +"        <key-leaf>identity4</key-leaf>"
                      +"        <mustLeaf2>identity5</mustLeaf2>"
                      +"    </identityList>"
                      +"</identityrefContainer>";
        
        getModelNode();
        request1 = createRequestFromString(requestXml1);
        request1.setMessageId("1");
        response1 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request1, response1);        
        assertTrue(response1.isOk());
        
        response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <idref:identityrefContainer xmlns:idref=\"urn:org:bbf2:pma:identityref-test\">"
                + "   <idref:case1Leaf>idref:identity4</idref:case1Leaf>"
                + "   <idref:default-leaf>idref:identity4</idref:default-leaf>"
                + "    <idref:identityList>"
                + "        <idref:key-leaf>idref:identity4</idref:key-leaf>"
                + "        <idref:mustLeaf2>idref:identity5</idref:mustLeaf2>"
                + "        <idref:whenLeaf>idref:identity6</idref:whenLeaf>"
                + "    </idref:identityList>"
                + "   </idref:identityrefContainer>"                
                + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + " </data>"
                + " </rpc-reply>"
                ;
        verifyGet(response);
    }

}
