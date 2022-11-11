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

import static org.junit.Assert.assertEquals;

import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(RequestScopeJunitRunner.class)
public class WhenMustDSExpressionValidationTest extends AbstractDataStoreValidatorTest {
	
    @Test
    public void testInfiniteInternalEditConfigCase() throws Exception{
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "<router>" +
                "<router-name>Base</router-name>" +
                "<firewall/>" +
                "<interface>" +
                "<interface-name>System</interface-name>" +
                "<ptp-hw-assist></ptp-hw-assist>" +
                "</interface>" +
                "</router>" +
                "</whenMustRefersSiblings>" ;
        editConfig(m_server, m_clientInfo, requestXml1, true);
        
        String response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "  <whenmust:whenMustRefersSiblings xmlns:whenmust=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                + "      <whenmust:leaf4>leaf4</whenmust:leaf4>"
                + "      <whenmust:router>"
                + "          <whenmust:firewall/>"
                + "          <whenmust:interface>"
                + "              <whenmust:interface-name>System</whenmust:interface-name>"
                + "              <whenmust:ptp-hw-assist>"
                + "                  <whenmust:admin-state>disable</whenmust:admin-state>"
                + "              </whenmust:ptp-hw-assist>"
                + "          </whenmust:interface>"
                + "          <whenmust:router-name>Base</whenmust:router-name>"
                + "          <whenmust:twamp-light/>"
                + "      </whenmust:router>"                
                + "  </whenmust:whenMustRefersSiblings>"
                + " </data>"
                + "</rpc-reply>"
                ;
        verifyGet(response);
    }
    
	@Test
	public void testWhenMustRefersToSiblingNode_1() throws Exception{
	    getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <leaf1>test</leaf1>" +
                "</whenMustRefersSiblings>                                                 " ;
        editConfig(m_server, m_clientInfo, requestXml1, true);
        
        String response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "  <whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                + "   <containerWithLeaflist/>"
                + "   <leaf1>test</leaf1>"
                + "   <leaf4>leaf4</leaf4>"
                + "  </whenMustRefersSiblings>"
                + " </data>"
                + "</rpc-reply>"
                ;
        verifyGet(response);
        
        //update leaf1 still not impact when validation on leaf2 nor leaf3
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <leaf1>test1</leaf1>" +
                "</whenMustRefersSiblings>                                                 " ;
        editConfig(m_server, m_clientInfo, requestXml1, true);
        
        response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "  <whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                + "   <containerWithLeaflist/>"
                + "   <leaf1>test1</leaf1>"
                + "   <leaf4>leaf4</leaf4>"
                + "  </whenMustRefersSiblings>"
                + " </data>"
                + "</rpc-reply>"
                ;
        verifyGet(response);
        
        //update leaf1 still not impact when validation on leaf2 as the leaf2 not exists
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <leaf1>leaf1</leaf1>" +
                "</whenMustRefersSiblings>                                                 " ;
        editConfig(m_server, m_clientInfo, requestXml1, true);
        
        response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "  <whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                + "   <containerWithLeaflist/>"
                + "   <leaf1>leaf1</leaf1>"
                + "   <leaf4>leaf4</leaf4>"
                + "   <leaf5>leaf5</leaf5>"
                + "  </whenMustRefersSiblings>"
                + " </data>"
                + "</rpc-reply>"
                ;
        verifyGet(response);
        
        //update leaf1 impact when validation on leaf2
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <leaf1>test1</leaf1>" +
                "  <leaf2>test</leaf2>" +
                "</whenMustRefersSiblings>                                                 " ;
        
        NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals(1,ncResponse.getErrors().size());
        assertEquals("/whenmust:whenMustRefersSiblings/whenmust:leaf2", ncResponse.getErrors().get(0).getErrorPath());
        assertEquals("Violate when constraints: ../leaf1 = 'leaf1'", ncResponse.getErrors().get(0).getErrorMessage());
	
	}

    @Test
    public void testWhenMustRefersToSiblingNode_2() throws Exception{
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <leaf1>test</leaf1>" +
                "</whenMustRefersSiblings>                                                 " ;
        editConfig(m_server, m_clientInfo, requestXml1, true);

        String response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + " <data>"
                        + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                        + "  <whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                        + "   <containerWithLeaflist/>"
                        + "   <leaf1>test</leaf1>"
                        + "   <leaf4>leaf4</leaf4>"
                        + "  </whenMustRefersSiblings>"
                        + " </data>"
                        + "</rpc-reply>"
                ;
        verifyGet(response);

        //update leaf1 still not impact when validation on leaf2 nor leaf3
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <leaf1>test1</leaf1>" +
                "</whenMustRefersSiblings>                                                 " ;
        editConfig(m_server, m_clientInfo, requestXml1, true);

        response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + " <data>"
                        + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                        + "  <whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                        + "   <containerWithLeaflist/>"
                        + "   <leaf1>test1</leaf1>"
                        + "   <leaf4>leaf4</leaf4>"
                        + "  </whenMustRefersSiblings>"
                        + " </data>"
                        + "</rpc-reply>"
        ;
        verifyGet(response);

        //update leaf1 still not impact when validation on leaf2 as the leaf2 not exists
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <leaf1>leaf1</leaf1>" +
                "</whenMustRefersSiblings>                                                 " ;
        editConfig(m_server, m_clientInfo, requestXml1, true);

        response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + " <data>"
                        + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                        + "  <whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                        + "   <containerWithLeaflist/>"
                        + "   <leaf1>leaf1</leaf1>"
                        + "   <leaf4>leaf4</leaf4>"
                        + "   <leaf5>leaf5</leaf5>"
                        + "  </whenMustRefersSiblings>"
                        + " </data>"
                        + "</rpc-reply>"
        ;
        verifyGet(response);

        //update leaf1 impact when validation on leaf2
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <leaf1>leaf1</leaf1>" +
                "  <leaf2>leaf2</leaf2>" +
                "</whenMustRefersSiblings>                                                 " ;
        editConfig(m_server, m_clientInfo, requestXml1, true);

        response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + " <data>"
                        + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                        + "  <whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                        + "   <containerWithLeaflist/>"
                        + "   <leaf1>leaf1</leaf1>"
                        + "   <leaf2>leaf2</leaf2>"
                        + "   <leaf4>leaf4</leaf4>"
                        + "   <leaf5>leaf5</leaf5>"
                        + "  </whenMustRefersSiblings>"
                        + " </data>"
                        + "</rpc-reply>"
        ;
        verifyGet(response);

        //update leaf3
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <leaf3>leaf3</leaf3>" +
                "</whenMustRefersSiblings>                                                 " ;
        editConfig(m_server, m_clientInfo, requestXml1, true);

        response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + " <data>"
                        + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                        + "  <whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                        + "   <containerWithLeaflist/>"
                        + "   <leaf1>leaf1</leaf1>"
                        + "   <leaf2>leaf2</leaf2>"
                        + "   <leaf3>leaf3</leaf3>"
                        + "   <leaf4>leaf4</leaf4>"
                        + "   <leaf5>leaf5</leaf5>"
                        + "  </whenMustRefersSiblings>"
                        + " </data>"
                        + "</rpc-reply>"
        ;
        verifyGet(response);

        //update leaf1 impact must validation on leaf3
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <leaf1>test1</leaf1>" +
                "  <leaf3>leaf3</leaf3>" +
                "</whenMustRefersSiblings>                                                 " ;

        NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals(1,ncResponse.getErrors().size());
        assertEquals("/whenmust:whenMustRefersSiblings/whenmust:leaf3", ncResponse.getErrors().get(0).getErrorPath());
        assertEquals("Violate must constraints: ../leaf1 = 'leaf1'", ncResponse.getErrors().get(0).getErrorMessage());
    }

    @Test
    public void testWhenMustRefersToSiblingNode_3() throws Exception{
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <leaf1>test</leaf1>" +
                "</whenMustRefersSiblings>                                                 " ;
        editConfig(m_server, m_clientInfo, requestXml1, true);

        String response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + " <data>"
                        + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                        + "  <whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                        + "   <containerWithLeaflist/>"
                        + "   <leaf1>test</leaf1>"
                        + "   <leaf4>leaf4</leaf4>"
                        + "  </whenMustRefersSiblings>"
                        + " </data>"
                        + "</rpc-reply>"
                ;
        verifyGet(response);

        //update leaf1 still not impact when validation on leaf2 nor leaf3
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <leaf1>test1</leaf1>" +
                "</whenMustRefersSiblings>                                                 " ;
        editConfig(m_server, m_clientInfo, requestXml1, true);

        response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + " <data>"
                        + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                        + "  <whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                        + "   <containerWithLeaflist/>"
                        + "   <leaf1>test1</leaf1>"
                        + "   <leaf4>leaf4</leaf4>"
                        + "  </whenMustRefersSiblings>"
                        + " </data>"
                        + "</rpc-reply>"
        ;
        verifyGet(response);

        //update leaf1 still not impact when validation on leaf2 as the leaf2 not exists
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <leaf1>leaf1</leaf1>" +
                "</whenMustRefersSiblings>                                                 " ;
        editConfig(m_server, m_clientInfo, requestXml1, true);

        response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + " <data>"
                        + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                        + "  <whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                        + "   <containerWithLeaflist/>"
                        + "   <leaf1>leaf1</leaf1>"
                        + "   <leaf4>leaf4</leaf4>"
                        + "   <leaf5>leaf5</leaf5>"
                        + "  </whenMustRefersSiblings>"
                        + " </data>"
                        + "</rpc-reply>"
        ;
        verifyGet(response);

        //update leaf1 impact when validation on leaf2
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <leaf1>leaf1</leaf1>" +
                "  <leaf2>leaf2</leaf2>" +
                "</whenMustRefersSiblings>                                                 " ;
        editConfig(m_server, m_clientInfo, requestXml1, true);

        response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + " <data>"
                        + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                        + "  <whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                        + "   <containerWithLeaflist/>"
                        + "   <leaf1>leaf1</leaf1>"
                        + "   <leaf2>leaf2</leaf2>"
                        + "   <leaf4>leaf4</leaf4>"
                        + "   <leaf5>leaf5</leaf5>"
                        + "  </whenMustRefersSiblings>"
                        + " </data>"
                        + "</rpc-reply>"
        ;
        verifyGet(response);

        //update leaf3
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <leaf3>leaf3</leaf3>" +
                "</whenMustRefersSiblings>                                                 " ;
        editConfig(m_server, m_clientInfo, requestXml1, true);

        response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + " <data>"
                        + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                        + "  <whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                        + "   <containerWithLeaflist/>"
                        + "   <leaf1>leaf1</leaf1>"
                        + "   <leaf2>leaf2</leaf2>"
                        + "   <leaf3>leaf3</leaf3>"
                        + "   <leaf4>leaf4</leaf4>"
                        + "   <leaf5>leaf5</leaf5>"
                        + "  </whenMustRefersSiblings>"
                        + " </data>"
                        + "</rpc-reply>"
        ;
        verifyGet(response);

        //delete leaf1 impacts leaf2, leaf3 and leaf5
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <leaf1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>" +
                "</whenMustRefersSiblings>                                                 " ;
        NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals(1,ncResponse.getErrors().size());
        assertEquals("/whenmust:whenMustRefersSiblings/whenmust:leaf3", ncResponse.getErrors().get(0).getErrorPath());
        assertEquals("Violate must constraints: ../leaf1 = 'leaf1'", ncResponse.getErrors().get(0).getErrorMessage());
    }

    @Test
    public void testWhenMustRefersToSiblingNode_4() throws Exception{
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <leaf1>test</leaf1>" +
                "</whenMustRefersSiblings>                                                 " ;
        editConfig(m_server, m_clientInfo, requestXml1, true);

        String response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + " <data>"
                        + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                        + "  <whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                        + "   <containerWithLeaflist/>"
                        + "   <leaf1>test</leaf1>"
                        + "   <leaf4>leaf4</leaf4>"
                        + "  </whenMustRefersSiblings>"
                        + " </data>"
                        + "</rpc-reply>"
                ;
        verifyGet(response);

        //update leaf1 still not impact when validation on leaf2 nor leaf3
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <leaf1>test1</leaf1>" +
                "</whenMustRefersSiblings>                                                 " ;
        editConfig(m_server, m_clientInfo, requestXml1, true);

        response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + " <data>"
                        + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                        + "  <whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                        + "   <containerWithLeaflist/>"
                        + "   <leaf1>test1</leaf1>"
                        + "   <leaf4>leaf4</leaf4>"
                        + "  </whenMustRefersSiblings>"
                        + " </data>"
                        + "</rpc-reply>"
        ;
        verifyGet(response);

        //update leaf1 still not impact when validation on leaf2 as the leaf2 not exists
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <leaf1>leaf1</leaf1>" +
                "</whenMustRefersSiblings>                                                 " ;
        editConfig(m_server, m_clientInfo, requestXml1, true);

        response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + " <data>"
                        + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                        + "  <whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                        + "   <containerWithLeaflist/>"
                        + "   <leaf1>leaf1</leaf1>"
                        + "   <leaf4>leaf4</leaf4>"
                        + "   <leaf5>leaf5</leaf5>"
                        + "  </whenMustRefersSiblings>"
                        + " </data>"
                        + "</rpc-reply>"
        ;
        verifyGet(response);

        //update leaf1 impact when validation on leaf2
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <leaf1>leaf1</leaf1>" +
                "  <leaf2>leaf2</leaf2>" +
                "</whenMustRefersSiblings>                                                 " ;
        editConfig(m_server, m_clientInfo, requestXml1, true);

        response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + " <data>"
                        + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                        + "  <whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                        + "   <containerWithLeaflist/>"
                        + "   <leaf1>leaf1</leaf1>"
                        + "   <leaf2>leaf2</leaf2>"
                        + "   <leaf4>leaf4</leaf4>"
                        + "   <leaf5>leaf5</leaf5>"
                        + "  </whenMustRefersSiblings>"
                        + " </data>"
                        + "</rpc-reply>"
        ;
        verifyGet(response);

        //update leaf3
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <leaf3>leaf3</leaf3>" +
                "</whenMustRefersSiblings>                                                 " ;
        editConfig(m_server, m_clientInfo, requestXml1, true);

        response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + " <data>"
                        + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                        + "  <whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                        + "   <containerWithLeaflist/>"
                        + "   <leaf1>leaf1</leaf1>"
                        + "   <leaf2>leaf2</leaf2>"
                        + "   <leaf3>leaf3</leaf3>"
                        + "   <leaf4>leaf4</leaf4>"
                        + "   <leaf5>leaf5</leaf5>"
                        + "  </whenMustRefersSiblings>"
                        + " </data>"
                        + "</rpc-reply>"
        ;
        verifyGet(response);

        //delete impacted leaf3
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <leaf1>leaf1</leaf1>" +
                "  <leaf3 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>" +
                "</whenMustRefersSiblings>                                                 " ;
        editConfig(m_server, m_clientInfo, requestXml1, true);

        response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + " <data>"
                        + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                        + "  <whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                        + "   <containerWithLeaflist/>"
                        + "   <leaf1>leaf1</leaf1>"
                        + "   <leaf2>leaf2</leaf2>"
                        + "   <leaf4>leaf4</leaf4>"
                        + "   <leaf5>leaf5</leaf5>"
                        + "  </whenMustRefersSiblings>"
                        + " </data>"
                        + "</rpc-reply>"
        ;
        verifyGet(response);

        //update leaf1 that impacts leaf2 and leaf5
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <leaf1>leaf2</leaf1>" +
                "</whenMustRefersSiblings>                                                 " ;
        editConfig(m_server, m_clientInfo, requestXml1, true);

        response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + " <data>"
                        + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                        + "  <whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                        + "   <containerWithLeaflist/>"
                        + "   <leaf1>leaf2</leaf1>"
                        + "   <leaf4>leaf4</leaf4>"
                        + "  </whenMustRefersSiblings>"
                        + " </data>"
                        + "</rpc-reply>"
        ;
        verifyGet(response);

        //delete leaf1 that impacts leaf2 and leaf5
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <leaf1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>" +
                "</whenMustRefersSiblings>                                                 " ;
        editConfig(m_server, m_clientInfo, requestXml1, true);

        response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + " <data>"
                        + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                        + "  <whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                        + "   <leaf4>leaf4</leaf4>"
                        + "  </whenMustRefersSiblings>"
                        + " </data>"
                        + "</rpc-reply>"
        ;
        verifyGet(response);

        //case with both when and must on leaf7
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <leaf1>leaf1</leaf1>" +
                "  <leaf6>leaf6</leaf6>" +
                "  <leaf7>leaf7</leaf7>" +
                "</whenMustRefersSiblings>                                                 " ;
        editConfig(m_server, m_clientInfo, requestXml1, true);

        response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + " <data>"
                        + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                        + "  <whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                        + "   <containerWithLeaflist/>"
                        + "   <leaf1>leaf1</leaf1>"
                        + "   <leaf4>leaf4</leaf4>"
                        + "   <leaf5>leaf5</leaf5>"
                        + "   <leaf6>leaf6</leaf6>"
                        + "   <leaf7>leaf7</leaf7>"
                        + "  </whenMustRefersSiblings>"
                        + " </data>"
                        + "</rpc-reply>"
        ;
        verifyGet(response);

        //delete leaf6 impacts leaf7
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <leaf6 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>" +
                "</whenMustRefersSiblings>                                                 " ;
        NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals(1,ncResponse.getErrors().size());
        assertEquals("/whenmust:whenMustRefersSiblings/whenmust:leaf7", ncResponse.getErrors().get(0).getErrorPath());
        assertEquals("Violate must constraints: ../leaf6 = 'leaf6'", ncResponse.getErrors().get(0).getErrorMessage());

    }

    @Test
    public void testWhenMustRefersToSiblingNode_5() throws Exception{
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <leaf1>test</leaf1>" +
                "</whenMustRefersSiblings>                                                 " ;
        editConfig(m_server, m_clientInfo, requestXml1, true);

        String response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + " <data>"
                        + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                        + "  <whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                        + "   <containerWithLeaflist/>"
                        + "   <leaf1>test</leaf1>"
                        + "   <leaf4>leaf4</leaf4>"
                        + "  </whenMustRefersSiblings>"
                        + " </data>"
                        + "</rpc-reply>"
                ;
        verifyGet(response);

        //update leaf1 still not impact when validation on leaf2 nor leaf3
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <leaf1>test1</leaf1>" +
                "</whenMustRefersSiblings>                                                 " ;
        editConfig(m_server, m_clientInfo, requestXml1, true);

        response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + " <data>"
                        + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                        + "  <whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                        + "   <containerWithLeaflist/>"
                        + "   <leaf1>test1</leaf1>"
                        + "   <leaf4>leaf4</leaf4>"
                        + "  </whenMustRefersSiblings>"
                        + " </data>"
                        + "</rpc-reply>"
        ;
        verifyGet(response);

        //update leaf1 still not impact when validation on leaf2 as the leaf2 not exists
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <leaf1>leaf1</leaf1>" +
                "</whenMustRefersSiblings>                                                 " ;
        editConfig(m_server, m_clientInfo, requestXml1, true);

        response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + " <data>"
                        + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                        + "  <whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                        + "   <containerWithLeaflist/>"
                        + "   <leaf1>leaf1</leaf1>"
                        + "   <leaf4>leaf4</leaf4>"
                        + "   <leaf5>leaf5</leaf5>"
                        + "  </whenMustRefersSiblings>"
                        + " </data>"
                        + "</rpc-reply>"
        ;
        verifyGet(response);

        //update leaf1 impact when validation on leaf2
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <leaf1>leaf1</leaf1>" +
                "  <leaf2>leaf2</leaf2>" +
                "</whenMustRefersSiblings>                                                 " ;
        editConfig(m_server, m_clientInfo, requestXml1, true);

        response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + " <data>"
                        + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                        + "  <whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                        + "   <containerWithLeaflist/>"
                        + "   <leaf1>leaf1</leaf1>"
                        + "   <leaf2>leaf2</leaf2>"
                        + "   <leaf4>leaf4</leaf4>"
                        + "   <leaf5>leaf5</leaf5>"
                        + "  </whenMustRefersSiblings>"
                        + " </data>"
                        + "</rpc-reply>"
        ;
        verifyGet(response);

        //update leaf3
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <leaf3>leaf3</leaf3>" +
                "</whenMustRefersSiblings>                                                 " ;
        editConfig(m_server, m_clientInfo, requestXml1, true);

        response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + " <data>"
                        + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                        + "  <whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                        + "   <containerWithLeaflist/>"
                        + "   <leaf1>leaf1</leaf1>"
                        + "   <leaf2>leaf2</leaf2>"
                        + "   <leaf3>leaf3</leaf3>"
                        + "   <leaf4>leaf4</leaf4>"
                        + "   <leaf5>leaf5</leaf5>"
                        + "  </whenMustRefersSiblings>"
                        + " </data>"
                        + "</rpc-reply>"
        ;
        verifyGet(response);

        //delete impacted leaf3
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <leaf1>leaf1</leaf1>" +
                "  <leaf3 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>" +
                "</whenMustRefersSiblings>                                                 " ;
        editConfig(m_server, m_clientInfo, requestXml1, true);

        response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + " <data>"
                        + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                        + "  <whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                        + "   <containerWithLeaflist/>"
                        + "   <leaf1>leaf1</leaf1>"
                        + "   <leaf2>leaf2</leaf2>"
                        + "   <leaf4>leaf4</leaf4>"
                        + "   <leaf5>leaf5</leaf5>"
                        + "  </whenMustRefersSiblings>"
                        + " </data>"
                        + "</rpc-reply>"
        ;
        verifyGet(response);

        //update leaf1 that impacts leaf2 and leaf5
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <leaf1>leaf2</leaf1>" +
                "</whenMustRefersSiblings>                                                 " ;
        editConfig(m_server, m_clientInfo, requestXml1, true);

        response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + " <data>"
                        + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                        + "  <whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                        + "   <containerWithLeaflist/>"
                        + "   <leaf1>leaf2</leaf1>"
                        + "   <leaf4>leaf4</leaf4>"
                        + "  </whenMustRefersSiblings>"
                        + " </data>"
                        + "</rpc-reply>"
        ;
        verifyGet(response);

        //delete leaf1 that impacts leaf2 and leaf5
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <leaf1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>" +
                "</whenMustRefersSiblings>                                                 " ;
        editConfig(m_server, m_clientInfo, requestXml1, true);

        response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + " <data>"
                        + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                        + "  <whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                        + "   <leaf4>leaf4</leaf4>"
                        + "  </whenMustRefersSiblings>"
                        + " </data>"
                        + "</rpc-reply>"
        ;
        verifyGet(response);

        //case with both when and must on leaf7
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <leaf1>leaf1</leaf1>" +
                "  <leaf6>leaf6</leaf6>" +
                "  <leaf7>leaf7</leaf7>" +
                "</whenMustRefersSiblings>                                                 " ;
        editConfig(m_server, m_clientInfo, requestXml1, true);

        response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + " <data>"
                        + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                        + "  <whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                        + "   <containerWithLeaflist/>"
                        + "   <leaf1>leaf1</leaf1>"
                        + "   <leaf4>leaf4</leaf4>"
                        + "   <leaf5>leaf5</leaf5>"
                        + "   <leaf6>leaf6</leaf6>"
                        + "   <leaf7>leaf7</leaf7>"
                        + "  </whenMustRefersSiblings>"
                        + " </data>"
                        + "</rpc-reply>"
        ;
        verifyGet(response);


        //update leaf6 impacts leaf7
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <leaf6>test</leaf6>" +
                "</whenMustRefersSiblings>                                                 " ;
        NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals(1,ncResponse.getErrors().size());
        assertEquals("/whenmust:whenMustRefersSiblings/whenmust:leaf7", ncResponse.getErrors().get(0).getErrorPath());
        assertEquals("Violate must constraints: ../leaf6 = 'leaf6'", ncResponse.getErrors().get(0).getErrorMessage());
    }


	@Test
	public void testWhenNodeWithSelfAtStart() throws Exception{
	    getModelNode();
	    //create leaf1
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <leaf1>test</leaf1>" +
                "</whenMustRefersSiblings>                                                 " ;
        editConfig(m_server, m_clientInfo, requestXml1, true);
        
        String response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "  <whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                + "   <containerWithLeaflist/>"
                + "   <leaf1>test</leaf1>"
                + "   <leaf4>leaf4</leaf4>"
                + "  </whenMustRefersSiblings>"
                + " </data>"
                + "</rpc-reply>"
                ;
        verifyGet(response);
       
        //update innerLeaf1 impacts whenNodeWithSelfAtStart
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                " <whenNodeWithSelfAtStart xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <innerLeaf1>innerLeaf1</innerLeaf1>" +
                " </whenNodeWithSelfAtStart>   " +
                "</whenMustRefersSiblings>  " ;
        NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, true);
        
        response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "  <whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                + "   <containerWithLeaflist/>"
                + "   <leaf1>test</leaf1>"
                + "   <leaf4>leaf4</leaf4>"
                + "   <whenNodeWithSelfAtStart xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                + "    <innerLeaf1>innerLeaf1</innerLeaf1>"
                + "   </whenNodeWithSelfAtStart>   " 
                + "  </whenMustRefersSiblings>"
                + " </data>"
                + "</rpc-reply>"
                ;
        verifyGet(response);
        
        //update innerLeaf1 impacts whenNodeWithSelfAtStart
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                " <whenNodeWithSelfAtStart xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <innerLeaf1>leaf1</innerLeaf1>" +
                " </whenNodeWithSelfAtStart>   " +
                "</whenMustRefersSiblings>  " ;
        ncResponse = editConfig(m_server, m_clientInfo, requestXml1, true);
        response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "  <whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                + "   <containerWithLeaflist/>"
                + "   <leaf1>test</leaf1>"
                + "   <leaf4>leaf4</leaf4>"
                + "   <whenNodeWithSelfAtStart xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                + "    <innerLeaf1>leaf1</innerLeaf1>"
                + "   </whenNodeWithSelfAtStart>   " 
                + "  </whenMustRefersSiblings>"
                + " </data>"
                + "</rpc-reply>"
                ;
        verifyGet(response);
        
        //update leaf1 impacts innerLeaf3 on whenNodeWithSelfAtStart
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                " <leaf1>test</leaf1>" +
                " <whenNodeWithSelfAtStart xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <innerLeaf1>innerLeaf1</innerLeaf1>" +
                "  <innerLeaf3>innerLeaf3</innerLeaf3>" +
                " </whenNodeWithSelfAtStart>   " +
                "</whenMustRefersSiblings>  " ;
        ncResponse = editConfig(m_server, m_clientInfo, requestXml1, true);
        
        response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "  <whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                + "   <containerWithLeaflist/>"
                + "   <leaf1>test</leaf1>"
                + "   <leaf4>leaf4</leaf4>"
                + "   <whenNodeWithSelfAtStart xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                + "    <innerLeaf1>innerLeaf1</innerLeaf1>"
                + "    <innerLeaf3>innerLeaf3</innerLeaf3>"
                + "   </whenNodeWithSelfAtStart>   " 
                + "  </whenMustRefersSiblings>"
                + " </data>"
                + "</rpc-reply>"
                ;
        verifyGet(response);
        
        //create innerLeaf4
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                " <whenNodeWithSelfAtStart xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <innerLeaf4>innerLeaf4</innerLeaf4>" +
                " </whenNodeWithSelfAtStart>   " +
                "</whenMustRefersSiblings>  " ;
        ncResponse = editConfig(m_server, m_clientInfo, requestXml1, true);
        
        response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "  <whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                + "   <containerWithLeaflist/>"
                + "   <leaf1>test</leaf1>"
                + "   <leaf4>leaf4</leaf4>"
                + "   <whenNodeWithSelfAtStart xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                + "    <innerLeaf1>innerLeaf1</innerLeaf1>"
                + "    <innerLeaf3>innerLeaf3</innerLeaf3>"
                + "    <innerLeaf4>innerLeaf4</innerLeaf4>"
                + "   </whenNodeWithSelfAtStart>   " 
                + "  </whenMustRefersSiblings>"
                + " </data>"
                + "</rpc-reply>"
                ;
        verifyGet(response);
        
        //update leaf1 impacts innerLeaf4
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                " <leaf1>leaf1</leaf1>" +
                " <whenNodeWithSelfAtStart xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <innerLeaf4>innerLeaf4</innerLeaf4>" +
                " </whenNodeWithSelfAtStart>   " +
                "</whenMustRefersSiblings>  " ;
        ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals(1,ncResponse.getErrors().size());
        assertEquals("/whenmust:whenMustRefersSiblings/whenmust:whenNodeWithSelfAtStart/whenmust:innerLeaf4", ncResponse.getErrors().get(0).getErrorPath());
        assertEquals("Violate must constraints: ../../leaf1 = 'test'", ncResponse.getErrors().get(0).getErrorMessage());
	}
	
	@Test
	public void testWhenWithSelfRefersParent() throws Exception {
		getModelNode();
		// starts with self axis ./.. points to node-set
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
				"<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
				" <leaf1>leaf1</leaf1>" +
				" <whenNodeWithSelfAtStart xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
				"  <innerLeaf2>innerLeaf2</innerLeaf2>" +
				" </whenNodeWithSelfAtStart>   " +
				"</whenMustRefersSiblings>  " ;
		editConfig(m_server, m_clientInfo, requestXml1, true);

		String response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ "  <whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
						+ "   <containerWithLeaflist/>"
						+ "   <leaf1>leaf1</leaf1>"
						+ "   <leaf4>leaf4</leaf4>"
						+ "   <leaf5>leaf5</leaf5>"
						+ "   <whenNodeWithSelfAtStart xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
						+ "    <innerLeaf2>innerLeaf2</innerLeaf2>"
						+ "   </whenNodeWithSelfAtStart>   " 
						+ "  </whenMustRefersSiblings>"
						+ " </data>"
						+ "</rpc-reply>"
						;
		verifyGet(response);
	}
	
	@Test
	public void testMustNodeWithSelfAtStart_1() throws Exception{
	    getModelNode();
	    
	    //create leaf1
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <leaf1>test</leaf1>" +
                "</whenMustRefersSiblings>                                                 " ;
        editConfig(m_server, m_clientInfo, requestXml1, true);
        
        String response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "  <whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                + "   <containerWithLeaflist/>"
                + "   <leaf1>test</leaf1>"
                + "   <leaf4>leaf4</leaf4>"
                + "  </whenMustRefersSiblings>"
                + " </data>"
                + "</rpc-reply>"
                ;
        verifyGet(response);
        
        //create innerLeaf1 impacts mustNodeWithSelfAtStart
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                " <mustNodeWithSelfAtStart xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <innerLeaf1>leaf1</innerLeaf1>" +
                " </mustNodeWithSelfAtStart>   " +
                "</whenMustRefersSiblings>  " ;
        NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals(1,ncResponse.getErrors().size());
        assertEquals("/whenmust:whenMustRefersSiblings/whenmust:mustNodeWithSelfAtStart", ncResponse.getErrors().get(0).getErrorPath());
        assertEquals("Violate must constraints: ./innerLeaf1 = 'innerLeaf1'", ncResponse.getErrors().get(0).getErrorMessage());
	}

	@Test
    public void testMustNodeWithSelfAtStart_2() throws Exception{
        getModelNode();

        //create leaf1
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <leaf1>test</leaf1>" +
                "</whenMustRefersSiblings>                                                 " ;
        editConfig(m_server, m_clientInfo, requestXml1, true);

        String response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + " <data>"
                        + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                        + "  <whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                        + "   <containerWithLeaflist/>"
                        + "   <leaf1>test</leaf1>"
                        + "   <leaf4>leaf4</leaf4>"
                        + "  </whenMustRefersSiblings>"
                        + " </data>"
                        + "</rpc-reply>"
                ;
        verifyGet(response);

//        //create innerLeaf1 impacts mustNodeWithSelfAtStart
//        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
//                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
//                " <mustNodeWithSelfAtStart xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
//                "  <innerLeaf1>leaf1</innerLeaf1>" +
//                " </mustNodeWithSelfAtStart>   " +
//                "</whenMustRefersSiblings>  " ;
//        NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
//        assertEquals(1,ncResponse.getErrors().size());
//        assertEquals("/whenmust:whenMustRefersSiblings/whenmust:mustNodeWithSelfAtStart", ncResponse.getErrors().get(0).getErrorPath());
//        assertEquals("Violate must constraints: ./innerLeaf1 = 'innerLeaf1'", ncResponse.getErrors().get(0).getErrorMessage());

        //update innerLeaf1 impacts mustNodeWithSelfAtStart
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                " <mustNodeWithSelfAtStart xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <innerLeaf1>innerLeaf1</innerLeaf1>" +
                " </mustNodeWithSelfAtStart>   " +
                "</whenMustRefersSiblings>  " ;
        editConfig(m_server, m_clientInfo, requestXml1, true);

        response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + " <data>"
                        + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                        + "  <whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                        + "  <containerWithLeaflist/>"
                        + "   <leaf1>test</leaf1>"
                        + "   <leaf4>leaf4</leaf4>"
                        + "   <mustNodeWithSelfAtStart xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                        + "    <innerLeaf1>innerLeaf1</innerLeaf1>"
                        + "   </mustNodeWithSelfAtStart>   "
                        + "  </whenMustRefersSiblings>"
                        + " </data>"
                        + "</rpc-reply>"
        ;
        verifyGet(response);

        //update innerLeaf1 impacts mustNodeWithSelfAtStart
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                " <mustNodeWithSelfAtStart xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <innerLeaf1>leaf1</innerLeaf1>" +
                " </mustNodeWithSelfAtStart>   " +
                "</whenMustRefersSiblings>  " ;
        NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals(1,ncResponse.getErrors().size());
        assertEquals("/whenmust:whenMustRefersSiblings/whenmust:mustNodeWithSelfAtStart", ncResponse.getErrors().get(0).getErrorPath());
        assertEquals("Violate must constraints: ./innerLeaf1 = 'innerLeaf1'", ncResponse.getErrors().get(0).getErrorMessage());
    }

    @Test
    public void testMustNodeWithSelfAtStart_3() throws Exception{
        getModelNode();

        //create leaf1
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <leaf1>test</leaf1>" +
                "</whenMustRefersSiblings>                                                 " ;
        editConfig(m_server, m_clientInfo, requestXml1, true);

        String response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + " <data>"
                        + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                        + "  <whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                        + "   <containerWithLeaflist/>"
                        + "   <leaf1>test</leaf1>"
                        + "   <leaf4>leaf4</leaf4>"
                        + "  </whenMustRefersSiblings>"
                        + " </data>"
                        + "</rpc-reply>"
                ;
        verifyGet(response);

//        //create innerLeaf1 impacts mustNodeWithSelfAtStart
//        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
//                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
//                " <mustNodeWithSelfAtStart xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
//                "  <innerLeaf1>leaf1</innerLeaf1>" +
//                " </mustNodeWithSelfAtStart>   " +
//                "</whenMustRefersSiblings>  " ;
//        NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
//        assertEquals(1,ncResponse.getErrors().size());
//        assertEquals("/whenmust:whenMustRefersSiblings/whenmust:mustNodeWithSelfAtStart", ncResponse.getErrors().get(0).getErrorPath());
//        assertEquals("Violate must constraints: ./innerLeaf1 = 'innerLeaf1'", ncResponse.getErrors().get(0).getErrorMessage());

        //update innerLeaf1 impacts mustNodeWithSelfAtStart
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                " <mustNodeWithSelfAtStart xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <innerLeaf1>innerLeaf1</innerLeaf1>" +
                " </mustNodeWithSelfAtStart>   " +
                "</whenMustRefersSiblings>  " ;
        editConfig(m_server, m_clientInfo, requestXml1, true);

        response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + " <data>"
                        + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                        + "  <whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                        + "  <containerWithLeaflist/>"
                        + "   <leaf1>test</leaf1>"
                        + "   <leaf4>leaf4</leaf4>"
                        + "   <mustNodeWithSelfAtStart xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                        + "    <innerLeaf1>innerLeaf1</innerLeaf1>"
                        + "   </mustNodeWithSelfAtStart>   "
                        + "  </whenMustRefersSiblings>"
                        + " </data>"
                        + "</rpc-reply>"
        ;
        verifyGet(response);

//        //update innerLeaf1 impacts mustNodeWithSelfAtStart
//        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
//                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
//                " <mustNodeWithSelfAtStart xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
//                "  <innerLeaf1>leaf1</innerLeaf1>" +
//                " </mustNodeWithSelfAtStart>   " +
//                "</whenMustRefersSiblings>  " ;
//        ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
//        assertEquals(1,ncResponse.getErrors().size());
//        assertEquals("/whenmust:whenMustRefersSiblings/whenmust:mustNodeWithSelfAtStart", ncResponse.getErrors().get(0).getErrorPath());
//        assertEquals("Violate must constraints: ./innerLeaf1 = 'innerLeaf1'", ncResponse.getErrors().get(0).getErrorMessage());

        //update leaf1 impacts innerLeaf3 on whenNodeWithSelfAtStart
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                " <leaf1>test</leaf1>" +
                " <mustNodeWithSelfAtStart xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <innerLeaf1>innerLeaf1</innerLeaf1>" +
                "  <innerLeaf3>innerLeaf3</innerLeaf3>" +
                " </mustNodeWithSelfAtStart>   " +
                "</whenMustRefersSiblings>  " ;
        editConfig(m_server, m_clientInfo, requestXml1, true);

        response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + " <data>"
                        + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                        + "  <whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                        + "  <containerWithLeaflist/>"
                        + "   <leaf1>test</leaf1>"
                        + "   <leaf4>leaf4</leaf4>"
                        + "   <mustNodeWithSelfAtStart xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                        + "    <innerLeaf1>innerLeaf1</innerLeaf1>"
                        + "    <innerLeaf3>innerLeaf3</innerLeaf3>"
                        + "   </mustNodeWithSelfAtStart>   "
                        + "  </whenMustRefersSiblings>"
                        + " </data>"
                        + "</rpc-reply>"
        ;
        verifyGet(response);

        //create innerLeaf4
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                " <mustNodeWithSelfAtStart xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <innerLeaf4>innerLeaf4</innerLeaf4>" +
                " </mustNodeWithSelfAtStart>   " +
                "</whenMustRefersSiblings>  " ;
        editConfig(m_server, m_clientInfo, requestXml1, true);

        response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + " <data>"
                        + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                        + "  <whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                        + "  <containerWithLeaflist/>"
                        + "   <leaf1>test</leaf1>"
                        + "   <leaf4>leaf4</leaf4>"
                        + "   <mustNodeWithSelfAtStart xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                        + "    <innerLeaf1>innerLeaf1</innerLeaf1>"
                        + "    <innerLeaf3>innerLeaf3</innerLeaf3>"
                        + "    <innerLeaf4>innerLeaf4</innerLeaf4>"
                        + "   </mustNodeWithSelfAtStart>   "
                        + "  </whenMustRefersSiblings>"
                        + " </data>"
                        + "</rpc-reply>"
        ;
        verifyGet(response);

        //update leaf1 impacts innerLeaf4
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                " <leaf1>leaf1</leaf1>" +
                " <mustNodeWithSelfAtStart xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <innerLeaf3>innerLeaf3</innerLeaf3>" +
                " </mustNodeWithSelfAtStart>   " +
                "</whenMustRefersSiblings>  " ;
        NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals(1,ncResponse.getErrors().size());
        assertEquals("/whenmust:whenMustRefersSiblings/whenmust:mustNodeWithSelfAtStart/whenmust:innerLeaf3", ncResponse.getErrors().get(0).getErrorPath());
        assertEquals("Violate must constraints: ../../leaf1 = 'test'", ncResponse.getErrors().get(0).getErrorMessage());


    }

    @Test
    public void testMustNodeWithSelfAtStart_4() throws Exception{
        getModelNode();

        //create leaf1
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <leaf1>test</leaf1>" +
                "</whenMustRefersSiblings>                                                 " ;
        editConfig(m_server, m_clientInfo, requestXml1, true);

        String response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + " <data>"
                        + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                        + "  <whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                        + "   <containerWithLeaflist/>"
                        + "   <leaf1>test</leaf1>"
                        + "   <leaf4>leaf4</leaf4>"
                        + "  </whenMustRefersSiblings>"
                        + " </data>"
                        + "</rpc-reply>"
                ;
        verifyGet(response);

        //update innerLeaf1 impacts mustNodeWithSelfAtStart
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                " <mustNodeWithSelfAtStart xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <innerLeaf1>innerLeaf1</innerLeaf1>" +
                " </mustNodeWithSelfAtStart>   " +
                "</whenMustRefersSiblings>  " ;
        editConfig(m_server, m_clientInfo, requestXml1, true);

        response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + " <data>"
                        + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                        + "  <whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                        + "  <containerWithLeaflist/>"
                        + "   <leaf1>test</leaf1>"
                        + "   <leaf4>leaf4</leaf4>"
                        + "   <mustNodeWithSelfAtStart xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                        + "    <innerLeaf1>innerLeaf1</innerLeaf1>"
                        + "   </mustNodeWithSelfAtStart>   "
                        + "  </whenMustRefersSiblings>"
                        + " </data>"
                        + "</rpc-reply>"
        ;
        verifyGet(response);

        //update leaf1 impacts innerLeaf3 on whenNodeWithSelfAtStart
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                " <leaf1>test</leaf1>" +
                " <mustNodeWithSelfAtStart xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <innerLeaf1>innerLeaf1</innerLeaf1>" +
                "  <innerLeaf3>innerLeaf3</innerLeaf3>" +
                " </mustNodeWithSelfAtStart>   " +
                "</whenMustRefersSiblings>  " ;
        editConfig(m_server, m_clientInfo, requestXml1, true);

        response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + " <data>"
                        + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                        + "  <whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                        + "  <containerWithLeaflist/>"
                        + "   <leaf1>test</leaf1>"
                        + "   <leaf4>leaf4</leaf4>"
                        + "   <mustNodeWithSelfAtStart xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                        + "    <innerLeaf1>innerLeaf1</innerLeaf1>"
                        + "    <innerLeaf3>innerLeaf3</innerLeaf3>"
                        + "   </mustNodeWithSelfAtStart>   "
                        + "  </whenMustRefersSiblings>"
                        + " </data>"
                        + "</rpc-reply>"
        ;
        verifyGet(response);

        //create innerLeaf4
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                " <mustNodeWithSelfAtStart xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <innerLeaf4>innerLeaf4</innerLeaf4>" +
                " </mustNodeWithSelfAtStart>   " +
                "</whenMustRefersSiblings>  " ;
        editConfig(m_server, m_clientInfo, requestXml1, true);

        response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + " <data>"
                        + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                        + "  <whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                        + "  <containerWithLeaflist/>"
                        + "   <leaf1>test</leaf1>"
                        + "   <leaf4>leaf4</leaf4>"
                        + "   <mustNodeWithSelfAtStart xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                        + "    <innerLeaf1>innerLeaf1</innerLeaf1>"
                        + "    <innerLeaf3>innerLeaf3</innerLeaf3>"
                        + "    <innerLeaf4>innerLeaf4</innerLeaf4>"
                        + "   </mustNodeWithSelfAtStart>   "
                        + "  </whenMustRefersSiblings>"
                        + " </data>"
                        + "</rpc-reply>"
        ;
        verifyGet(response);

        // starts with self axis ./.. points to node-set
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                " <leaf1>test</leaf1>" +
                " <mustNodeWithSelfAtStart xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <innerLeaf2>innerLeaf2</innerLeaf2>" +
                " </mustNodeWithSelfAtStart>   " +
                "</whenMustRefersSiblings>  " ;
        editConfig(m_server, m_clientInfo, requestXml1, true);

        response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + " <data>"
                        + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                        + "  <whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                        + "  <containerWithLeaflist/>"
                        + "   <leaf1>test</leaf1>"
                        + "   <leaf4>leaf4</leaf4>"
                        + "   <mustNodeWithSelfAtStart xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                        + "    <innerLeaf1>innerLeaf1</innerLeaf1>"
                        + "    <innerLeaf2>innerLeaf2</innerLeaf2>"
                        + "    <innerLeaf3>innerLeaf3</innerLeaf3>"
                        + "    <innerLeaf4>innerLeaf4</innerLeaf4>"
                        + "   </mustNodeWithSelfAtStart>   "
                        + "  </whenMustRefersSiblings>"
                        + " </data>"
                        + "</rpc-reply>"
        ;
        verifyGet(response);
    }
	
	@Test
	public void testWhenMustNodeWithSingleKeyList() throws Exception{
	    getModelNode();
	    
	    //create list1
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <leaf1>test</leaf1>" +
                "  <whenMustListWithSingleKey xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "   <name>name1</name>" +
                "   <otherLeaf1>test</otherLeaf1>" +
                "  </whenMustListWithSingleKey>   " +
                "</whenMustRefersSiblings>                                                 " ;
        NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, true);
        
        String response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "  <whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                + "   <containerWithLeaflist/>"
                + "   <leaf1>test</leaf1>"
                + "   <leaf4>leaf4</leaf4>"
                + "   <whenMustListWithSingleKey xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                + "    <name>name1</name>" 
                + "    <otherLeaf1>test</otherLeaf1>" 
                + "   </whenMustListWithSingleKey>   " 
                + "  </whenMustRefersSiblings>"
                + " </data>"
                + "</rpc-reply>"
                ;
        verifyGet(response);
        
        //create innerList refers existing list
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <leaf1>test</leaf1>" +
                "  <whenMustListWithSingleKey xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "   <name>name1</name>" +
                "   <whenMustInnerListWithSingleKey>" +
                "    <name>name1</name>" +
                "   </whenMustInnerListWithSingleKey>" +
                "  </whenMustListWithSingleKey>   " +
                "</whenMustRefersSiblings>                                                 " ;
        ncResponse = editConfig(m_server, m_clientInfo, requestXml1, true);
        
        response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "  <whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                + "   <containerWithLeaflist/>"
                + "   <leaf1>test</leaf1>"
                + "   <leaf4>leaf4</leaf4>"
                + "   <whenMustListWithSingleKey xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                + "    <name>name1</name>" 
                + "    <otherLeaf1>test</otherLeaf1>" 
                + "    <whenMustInnerListWithSingleKey xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                + "     <name>name1</name>" 
                + "    </whenMustInnerListWithSingleKey>   " 
                + "   </whenMustListWithSingleKey>   " 
                + "  </whenMustRefersSiblings>"
                + " </data>"
                + "</rpc-reply>"
                ;
        verifyGet(response);
        
        //create list2 where innerlist refers existing list
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <leaf1>test</leaf1>" +
                "  <whenMustListWithSingleKey xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "   <name>name2</name>" +
                "   <otherLeaf1>test</otherLeaf1>" +
                "    <whenMustInnerListWithSingleKey xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "     <name>name1</name>" +
                "    </whenMustInnerListWithSingleKey>   "  +
                "  </whenMustListWithSingleKey>   " +
                "</whenMustRefersSiblings>                                                 " ;
        ncResponse = editConfig(m_server, m_clientInfo, requestXml1, true);
        
        response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "  <whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                + "   <containerWithLeaflist/>"
                + "   <leaf1>test</leaf1>"
                + "   <leaf4>leaf4</leaf4>"
                + "   <whenMustListWithSingleKey xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                + "    <name>name1</name>" 
                + "    <otherLeaf1>test</otherLeaf1>" 
                + "    <whenMustInnerListWithSingleKey xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                + "     <name>name1</name>" 
                + "    </whenMustInnerListWithSingleKey>   "
                + "   </whenMustListWithSingleKey>   " 
                + "   <whenMustListWithSingleKey xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                + "    <name>name2</name>" 
                + "    <otherLeaf1>test</otherLeaf1>" 
                + "    <whenMustInnerListWithSingleKey xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                + "     <name>name1</name>" 
                + "    </whenMustInnerListWithSingleKey>   " 
                + "   </whenMustListWithSingleKey>   " 
                + "  </whenMustRefersSiblings>"
                + " </data>"
                + "</rpc-reply>"
                ;
        verifyGet(response);
        
        //create whenRefersParentname and mustRefersParentname
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <leaf1>test</leaf1>" +
                "  <whenMustListWithSingleKey xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "   <name>name2</name>" +
                "   <otherLeaf1>test</otherLeaf1>" +
                "    <whenMustInnerListWithSingleKey xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "     <name>name1</name>" +
                "     <whenRefersParentname>whenRefersParentname</whenRefersParentname>" +
                "     <mustRefersParentname>mustRefersParentname</mustRefersParentname>" +
                "    </whenMustInnerListWithSingleKey>   "  +
                "  </whenMustListWithSingleKey>   " +
                "</whenMustRefersSiblings>                                                 " ;
        ncResponse = editConfig(m_server, m_clientInfo, requestXml1, true);
        
        response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "  <whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                + "   <containerWithLeaflist/>"
                + "   <leaf1>test</leaf1>"
                + "   <leaf4>leaf4</leaf4>"
                + "   <whenMustListWithSingleKey xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                + "    <name>name1</name>" 
                + "    <otherLeaf1>test</otherLeaf1>" 
                + "    <whenMustInnerListWithSingleKey xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                + "     <name>name1</name>" 
                + "    </whenMustInnerListWithSingleKey>   "
                + "   </whenMustListWithSingleKey>   " 
                + "   <whenMustListWithSingleKey xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                + "    <name>name2</name>" 
                + "    <otherLeaf1>test</otherLeaf1>" 
                + "    <whenMustInnerListWithSingleKey xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                + "     <name>name1</name>" 
                + "     <whenRefersParentname>whenRefersParentname</whenRefersParentname>" 
                + "     <mustRefersParentname>mustRefersParentname</mustRefersParentname>" 
                + "    </whenMustInnerListWithSingleKey>   " 
                + "   </whenMustListWithSingleKey>   " 
                + "  </whenMustRefersSiblings>"
                + " </data>"
                + "</rpc-reply>"
                ;
        verifyGet(response);
        
	    //delete list1
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <leaf1>test</leaf1>" +
                "  <whenMustListWithSingleKey xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\" xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\">" +
                "   <name>name1</name>" +
                "  </whenMustListWithSingleKey>   " +
                "</whenMustRefersSiblings>                                                 " ;
        ncResponse = editConfig(m_server, m_clientInfo, requestXml1, true);
                
        response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "  <whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                + "   <containerWithLeaflist/>"
                + "   <leaf1>test</leaf1>"
                + "   <leaf4>leaf4</leaf4>"
                + "   <whenMustListWithSingleKey xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                + "    <name>name2</name>" 
                + "    <otherLeaf1>test</otherLeaf1>" 
                + "    <whenMustInnerListWithSingleKey xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                + "     <name>name1</name>" 
                + "     <whenRefersParentname>whenRefersParentname</whenRefersParentname>" 
                + "     <mustRefersParentname>mustRefersParentname</mustRefersParentname>"
                + "    </whenMustInnerListWithSingleKey>   " 
                + "   </whenMustListWithSingleKey>   " 
                + "  </whenMustRefersSiblings>"
                + " </data>"
                + "</rpc-reply>"
                ;
        verifyGet(response);
        
        //update list2 where innerlist refers non-existing list
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <leaf1>test</leaf1>" +
                "  <whenMustListWithSingleKey xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "   <name>name2</name>" +
                "   <otherLeaf1>test</otherLeaf1>" +
                "    <whenMustInnerListWithSingleKey xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "     <name>name3</name>" +
                "    </whenMustInnerListWithSingleKey>   "  +
                "  </whenMustListWithSingleKey>   " +
                "</whenMustRefersSiblings>                                                 " ;
        ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals(1,ncResponse.getErrors().size());
        assertEquals("/whenmust:whenMustRefersSiblings/whenmust:whenMustListWithSingleKey[whenmust:name='name2']/whenmust:whenMustInnerListWithSingleKey[whenmust:name='name1']/whenmust:mustRefersParentname", ncResponse.getErrors().get(0).getErrorPath());
        assertEquals("Violate must constraints: ../../../whenMustListWithSingleKey[name=current()/../name]", ncResponse.getErrors().get(0).getErrorMessage());
	}

	@Test
	public void testWhenMustNodeWithMultiKeyList() throws Exception{
	    getModelNode();
        
	    //create list1
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <leaf1>test</leaf1>" +
                "  <whenMustListWithMultiKey xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "   <name>name1</name>" +
                "   <id>1</id>" +
                "   <otherLeaf1>test</otherLeaf1>" +
                "   <whenMustInnerListWithMultiKey>" +
                "    <name>name1</name>" +
                "   </whenMustInnerListWithMultiKey>" +
                "  </whenMustListWithMultiKey>   " +
                "</whenMustRefersSiblings>                                                 " ;
        NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, true);
        
        String response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "  <whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                + "   <containerWithLeaflist/>"
                + "   <leaf1>test</leaf1>"
                + "   <leaf4>leaf4</leaf4>"
                + "   <whenMustListWithMultiKey xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                + "    <name>name1</name>" 
                + "    <id>1</id>" 
                + "    <otherLeaf1>test</otherLeaf1>" 
                + "    <whenMustInnerListWithMultiKey xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                + "     <name>name1</name>" 
                + "    </whenMustInnerListWithMultiKey>   " 
                + "   </whenMustListWithMultiKey>   " 
                + "  </whenMustRefersSiblings>"
                + " </data>"
                + "</rpc-reply>"
                ;
        verifyGet(response);
        
	    //create list2 with innerlist refers existing list
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <leaf1>test</leaf1>" +
                "  <whenMustListWithMultiKey xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "   <name>name2</name>" +
                "   <id>2</id>" +
                "   <otherLeaf1>test</otherLeaf1>" +
                "   <whenMustInnerListWithMultiKey>" +
                "    <name>name1</name>" +
                "   </whenMustInnerListWithMultiKey>" +
                "  </whenMustListWithMultiKey>   " +
                "</whenMustRefersSiblings>                                                 " ;
        ncResponse = editConfig(m_server, m_clientInfo, requestXml1, true);
        
        response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "  <whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                + "   <containerWithLeaflist/>"
                + "   <leaf1>test</leaf1>"
                + "   <leaf4>leaf4</leaf4>"
                + "   <whenMustListWithMultiKey xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                + "    <name>name1</name>" 
                + "    <id>1</id>" 
                + "    <otherLeaf1>test</otherLeaf1>" 
                + "    <whenMustInnerListWithMultiKey xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                + "     <name>name1</name>" 
                + "    </whenMustInnerListWithMultiKey>   " 
                + "   </whenMustListWithMultiKey>   " 
                + "   <whenMustListWithMultiKey xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                + "    <name>name2</name>" 
                + "    <id>2</id>" 
                + "    <otherLeaf1>test</otherLeaf1>" 
                + "    <whenMustInnerListWithMultiKey xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                + "     <name>name1</name>" 
                + "    </whenMustInnerListWithMultiKey>   " 
                + "   </whenMustListWithMultiKey>   " 
                + "  </whenMustRefersSiblings>"
                + " </data>"
                + "</rpc-reply>"
                ;
        verifyGet(response);
        
	    //create list3
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <leaf1>test</leaf1>" +
                "  <whenMustListWithMultiKey xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "   <name>name3</name>" +
                "   <id>3</id>" +
                "   <otherLeaf1>test</otherLeaf1>" +
                "   <whenMustInnerListWithMultiKey>" +
                "    <name>name1</name>" +
                "    <innerLeaf2>innerLeaf2</innerLeaf2>" +
                "   </whenMustInnerListWithMultiKey>" +
                "  </whenMustListWithMultiKey>   " +
                "</whenMustRefersSiblings>                                                 " ;
        ncResponse = editConfig(m_server, m_clientInfo, requestXml1, true);
        
        response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "  <whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                + "   <containerWithLeaflist/>"
                + "   <leaf1>test</leaf1>"
                + "   <leaf4>leaf4</leaf4>"
                + "   <whenMustListWithMultiKey xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                + "    <name>name1</name>" 
                + "    <id>1</id>" 
                + "    <otherLeaf1>test</otherLeaf1>" 
                + "    <whenMustInnerListWithMultiKey xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                + "     <name>name1</name>" 
                + "    </whenMustInnerListWithMultiKey>   " 
                + "   </whenMustListWithMultiKey>   " 
                + "   <whenMustListWithMultiKey xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                + "    <name>name2</name>" 
                + "    <id>2</id>" 
                + "    <otherLeaf1>test</otherLeaf1>" 
                + "    <whenMustInnerListWithMultiKey xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                + "     <name>name1</name>" 
                + "    </whenMustInnerListWithMultiKey>   " 
                + "   </whenMustListWithMultiKey>   " 
                + "   <whenMustListWithMultiKey xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                + "    <name>name3</name>" 
                + "    <id>3</id>" 
                + "    <otherLeaf1>test</otherLeaf1>" 
                + "    <whenMustInnerListWithMultiKey xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                + "     <name>name1</name>" 
                + "     <innerLeaf2>innerLeaf2</innerLeaf2>" 
                + "    </whenMustInnerListWithMultiKey>   " 
                + "   </whenMustListWithMultiKey>   " 
                + "  </whenMustRefersSiblings>"
                + " </data>"
                + "</rpc-reply>"
                ;
        verifyGet(response);
        
	    //delete list1
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <leaf1>test</leaf1>" +
                "  <whenMustListWithMultiKey xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\" xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\">" +
                "   <name>name1</name>" +
                "   <id>1</id>" +
                "  </whenMustListWithMultiKey>   " +
                "</whenMustRefersSiblings>                                                 " ;
        ncResponse = editConfig(m_server, m_clientInfo, requestXml1, true);
                
        response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "  <whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                + "   <containerWithLeaflist/>"
                + "   <leaf1>test</leaf1>"
                + "   <leaf4>leaf4</leaf4>"
                + "   <whenMustListWithMultiKey xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                + "    <name>name2</name>" 
                + "    <id>2</id>" 
                + "    <otherLeaf1>test</otherLeaf1>" 
                + "    <whenMustInnerListWithMultiKey xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                + "     <name>name1</name>" 
                + "    </whenMustInnerListWithMultiKey>   " 
                + "   </whenMustListWithMultiKey>   "
                + "   <whenMustListWithMultiKey xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                + "    <name>name3</name>" 
                + "    <id>3</id>" 
                + "    <otherLeaf1>test</otherLeaf1>" 
                + "    <whenMustInnerListWithMultiKey xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                + "     <name>name1</name>" 
                + "     <innerLeaf2>innerLeaf2</innerLeaf2>" 
                + "    </whenMustInnerListWithMultiKey>   " 
                + "   </whenMustListWithMultiKey>   "
                + "  </whenMustRefersSiblings>"
                + " </data>"
                + "</rpc-reply>"
                ;
        verifyGet(response);
        
	    //update list2 with innerlist refers non-existing list
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <leaf1>test</leaf1>" +
                "  <whenMustListWithMultiKey xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "   <name>name2</name>" +
                "   <id>2</id>" +
                "   <otherLeaf1>test</otherLeaf1>" +
                "   <whenMustInnerListWithMultiKey>" +
                "    <name>name2</name>" +
                "   </whenMustInnerListWithMultiKey>" +
                "  </whenMustListWithMultiKey>   " +
                "</whenMustRefersSiblings>                                                 " ;
        ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals(1,ncResponse.getErrors().size());
        assertEquals("/whenmust:whenMustRefersSiblings/whenmust:whenMustListWithMultiKey[whenmust:name='name3'][whenmust:id='3']/whenmust:whenMustInnerListWithMultiKey[whenmust:name='name1']/whenmust:innerLeaf2", ncResponse.getErrors().get(0).getErrorPath());
        assertEquals("Violate must constraints: ../../../whenMustListWithMultiKey[name=current()][id='1']", ncResponse.getErrors().get(0).getErrorMessage());
	}
	
	@Test
	public void testWhenMustRefersOtherRootNodeInSameModule() throws Exception{
	    getModelNode();
	    
	    // create otherRootContainerLeaf
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<otherRootContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <otherRootLeaf>otherRootLeaf</otherRootLeaf>" +
                "</otherRootContainer>";
        editConfig(m_server, m_clientInfo, requestXml1, true);
        
        String response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "  <otherRootContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                + "   <otherRootLeaf>otherRootLeaf</otherRootLeaf>"
                + "   <otherDefaultLeaf>otherDefaultLeaf</otherDefaultLeaf>"
                + "  </otherRootContainer>"
                + " </data>"
                + "</rpc-reply>"
                ;
        verifyGet(response);
        
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <leaf1>test</leaf1>" +
                "  <leaf8>leaf8</leaf8>" +
                "</whenMustRefersSiblings>";
        editConfig(m_server, m_clientInfo, requestXml1, true);
        
        response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "  <whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                + "   <containerWithLeaflist/>"
                + "   <leaf1>test</leaf1>"
                + "   <leaf4>leaf4</leaf4>"
                + "   <leaf8>leaf8</leaf8>"
                + "  </whenMustRefersSiblings>"
                + "  <whenmust:otherRootContainer xmlns:whenmust=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                + "   <whenmust:otherRootLeaf>otherRootLeaf</whenmust:otherRootLeaf>"
                + "   <whenmust:otherDefaultLeaf>otherDefaultLeaf</whenmust:otherDefaultLeaf>"
                + "  </whenmust:otherRootContainer>"
                + " </data>"
                + "</rpc-reply>"
                ;
        verifyGet(response);
        
        // delete otherRootContainer impacts leaf8
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<otherRootContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <otherRootLeaf xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>" +
                "</otherRootContainer>";
        editConfig(m_server, m_clientInfo, requestXml1, true);
        
        response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "  <whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                + "   <containerWithLeaflist/>"
                + "   <leaf1>test</leaf1>"
                + "   <leaf4>leaf4</leaf4>"
                + "  </whenMustRefersSiblings>"
                + "  <whenmust:otherRootContainer xmlns:whenmust=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                + "   <whenmust:otherDefaultLeaf>otherDefaultLeaf</whenmust:otherDefaultLeaf>"
                + "  </whenmust:otherRootContainer>"
                + " </data>"
                + "</rpc-reply>"
                ;
        verifyGet(response);
	}
	
	@Test
	public void testWhenMustRefersOtherRootNodeInDifferentModule() throws Exception{
	    getModelNode();
	    
	    // create iamImpactNode
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <iamImpactNode>iamImpactNode</iamImpactNode>" +
                "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);
        
        String response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                + "   <validation:iamImpactNode>iamImpactNode</validation:iamImpactNode>"
                + "  </validation:validation>"
                + " </data>"
                + "</rpc-reply>"
                ;
        verifyGet(response);
        
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <leaf1>test</leaf1>" +
                "  <leaf9>leaf9</leaf9>" +
                "</whenMustRefersSiblings>";
        editConfig(m_server, m_clientInfo, requestXml1, true);
        
        response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                + "   <validation:iamImpactNode>iamImpactNode</validation:iamImpactNode>"
                + "  </validation:validation>"
                + "  <whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                + "   <containerWithLeaflist/>"
                + "   <leaf1>test</leaf1>"
                + "   <leaf4>leaf4</leaf4>"
                + "   <leaf9>leaf9</leaf9>"
                + "  </whenMustRefersSiblings>"
                + " </data>"
                + "</rpc-reply>"
                ;
        verifyGet(response);
        
        // delete iamImpactNode impacts leaf9
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <iamImpactNode xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>" +
                "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);
        
        response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "  <whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                + "   <containerWithLeaflist/>"
                + "   <leaf1>test</leaf1>"
                + "   <leaf4>leaf4</leaf4>"
                + "  </whenMustRefersSiblings>"
                + " </data>"
                + "</rpc-reply>"
                ;
        verifyGet(response);
	}
	
	@Test
	public void testNotOfWhenMustNodeWithSelfAtStart_1() throws Exception{
	    getModelNode();
	    //create leaf1 impacts not() of must on innerLeaf5 in mustNodeWithSelfAtStart
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <leaf1>test</leaf1>" +
                "  <mustNodeWithSelfAtStart>" +
                "   <innerLeaf5>innerLeaf5</innerLeaf5>" +
                "  </mustNodeWithSelfAtStart>" +
                "</whenMustRefersSiblings>                                                 " ;
        
        NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals(1,ncResponse.getErrors().size());
        assertEquals("/whenmust:whenMustRefersSiblings/whenmust:mustNodeWithSelfAtStart/whenmust:innerLeaf5", ncResponse.getErrors().get(0).getErrorPath());
        assertEquals("Violate must constraints: not(../../leaf1 = 'test')", ncResponse.getErrors().get(0).getErrorMessage());
	}

    @Test
    public void testNotOfWhenMustNodeWithSelfAtStart_2() throws Exception{
        getModelNode();

        //create leaf1 impacts not() of when on innerLeaf5 in whenNodeWithSelfAtStart
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <leaf1>test1</leaf1>" +
                "  <mustNodeWithSelfAtStart>" +
                "   <innerLeaf5>innerLeaf5</innerLeaf5>" +
                "   <innerLeaf1>innerLeaf1</innerLeaf1>" +
                "  </mustNodeWithSelfAtStart>" +
                "  <whenNodeWithSelfAtStart>" +
                "   <innerLeaf1>innerLeaf1</innerLeaf1>" +
                "   <innerLeaf5>innerLeaf5</innerLeaf5>" +
                "  </whenNodeWithSelfAtStart>" +
                "</whenMustRefersSiblings>                                                 ";;

        NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, true);

        String response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + " <data>"
                        + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                        + "  <whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                        + "  <containerWithLeaflist/>"
                        + "   <leaf1>test1</leaf1>"
                        + "   <leaf4>leaf4</leaf4>"
                        + "   <whenNodeWithSelfAtStart>"
                        + "    <innerLeaf5>innerLeaf5</innerLeaf5>"
                        + "    <innerLeaf1>innerLeaf1</innerLeaf1>"
                        + "   </whenNodeWithSelfAtStart>"
                        + "   <mustNodeWithSelfAtStart>"
                        + "    <innerLeaf5>innerLeaf5</innerLeaf5>"
                        + "    <innerLeaf1>innerLeaf1</innerLeaf1>"
                        + "   </mustNodeWithSelfAtStart>"
                        + "  </whenMustRefersSiblings>"
                        + " </data>"
                        + "</rpc-reply>"
                ;
        verifyGet(response);

        // update leaf1 impacts both innerLeaf5 on mustNodeWithSelfAtStart and whenNodeWithSelfAtStart
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <leaf1>leaf1</leaf1>" +
                "  <mustNodeWithSelfAtStart>" +
                "  <innerLeaf5>innerLeaf5</innerLeaf5>" +
                "  </mustNodeWithSelfAtStart>" +
                "</whenMustRefersSiblings>                                                 " ;

        ncResponse = editConfig(m_server, m_clientInfo, requestXml1, true);

        response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + " <data>"
                        + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                        + "  <whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                        + "  <containerWithLeaflist/>"
                        + "   <leaf1>leaf1</leaf1>"
                        + "   <leaf4>leaf4</leaf4>"
                        + "   <leaf5>leaf5</leaf5>"
                        + "   <whenNodeWithSelfAtStart>"
                        + "    <innerLeaf5>innerLeaf5</innerLeaf5>"
                        + "    <innerLeaf1>innerLeaf1</innerLeaf1>"
                        + "   </whenNodeWithSelfAtStart>"
                        + "   <mustNodeWithSelfAtStart>"
                        + "    <innerLeaf5>innerLeaf5</innerLeaf5>"
                        + "    <innerLeaf1>innerLeaf1</innerLeaf1>"
                        + "   </mustNodeWithSelfAtStart>"
                        + "  </whenMustRefersSiblings>"
                        + " </data>"
                        + "</rpc-reply>"
        ;
        verifyGet(response);
    }

	@Test
	public void testWhenMustRefersSiblingLeafList() throws Exception{
	    getModelNode();
	    
	    // create leaf2 refers existing leaf-list
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <leaf1>rock</leaf1>" +
                "  <containerWithLeaflist>" +
                "   <leaflist1>tom</leaflist1>" +
                "   <leaflist1>jerry</leaflist1>" +
                "   <leaf2>harry</leaf2>" +
                "  </containerWithLeaflist>" +
                "</whenMustRefersSiblings>"  ;
        editConfig(m_server, m_clientInfo, requestXml1, true);
        
        String response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "   <whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                + "    <leaf4>leaf4</leaf4>"
                + "    <leaf1>rock</leaf1>"
                + "    <containerWithLeaflist>"
                + "     <leaf2>harry</leaf2>"
                + "     <leaflist1>tom</leaflist1>"
                + "     <leaflist1>jerry</leaflist1>"
                + "    </containerWithLeaflist>"
                + "   </whenMustRefersSiblings>"
                + " </data>"
                + "</rpc-reply>"
                ;
        verifyGet(response);
        
	    // create leaf4 refers with != of existing leaf-list
        // see https://stackoverflow.com/questions/4629416/xpath-operator-how-does-it-work 
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <containerWithLeaflist>" +
                "   <leaf4>john</leaf4>" +
                "  </containerWithLeaflist>" +
                "</whenMustRefersSiblings>"  ;
        NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, true);
        
        response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "   <whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                + "    <leaf4>leaf4</leaf4>"
                + "    <leaf1>rock</leaf1>"
                + "    <containerWithLeaflist>"
                + "     <leaf2>harry</leaf2>"
                + "     <leaflist1>tom</leaflist1>"
                + "     <leaflist1>jerry</leaflist1>"
                + "     <leaf4>john</leaf4>"
                + "    </containerWithLeaflist>"
                + "   </whenMustRefersSiblings>"
                + " </data>"
                + "</rpc-reply>"
                ;
        verifyGet(response);
        
	    // create leaf3 refers with not() of existing leaf-list
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <containerWithLeaflist>" +
                "   <leaf3>harry</leaf3>" +
                "  </containerWithLeaflist>" +
                "</whenMustRefersSiblings>"  ;
        ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals(1,ncResponse.getErrors().size());
        assertEquals("/whenmust:whenMustRefersSiblings/whenmust:containerWithLeaflist/whenmust:leaf3", ncResponse.getErrors().get(0).getErrorPath());
        assertEquals("Violate when constraints: not(../leaflist1 = 'tom')", ncResponse.getErrors().get(0).getErrorMessage());
          
	    // delete 'jerry' leaflist1 instance impacts leaf4
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <containerWithLeaflist>" +
                "   <leaflist1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\">jerry</leaflist1>" +
                "  </containerWithLeaflist>" +
                "</whenMustRefersSiblings>"  ;
        editConfig(m_server, m_clientInfo, requestXml1, true);
        
        response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "   <whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                + "    <leaf4>leaf4</leaf4>"
                + "    <leaf1>rock</leaf1>"
                + "    <containerWithLeaflist>"
                + "     <leaf2>harry</leaf2>"
                + "     <leaflist1>tom</leaflist1>"
                + "    </containerWithLeaflist>"
                + "   </whenMustRefersSiblings>"
                + " </data>"
                + "</rpc-reply>"
                ;
        verifyGet(response);
        
	    // create leaf4 refers with != of existing leaf-list
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <containerWithLeaflist>" +
                "   <leaf4>john</leaf4>" +
                "  </containerWithLeaflist>" +
                "</whenMustRefersSiblings>"  ;
        ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);        
        assertEquals(1,ncResponse.getErrors().size());
        assertEquals("/whenmust:whenMustRefersSiblings/whenmust:containerWithLeaflist/whenmust:leaf4", ncResponse.getErrors().get(0).getErrorPath());
        assertEquals("Violate when constraints: ../leaflist1 != 'tom'", ncResponse.getErrors().get(0).getErrorMessage());
        
	    // delete 'tom' leaflist1 instance impacts leaf2
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <containerWithLeaflist>" +
                "   <leaflist1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\">tom</leaflist1>" +
                "  </containerWithLeaflist>" +
                "</whenMustRefersSiblings>"  ;
        editConfig(m_server, m_clientInfo, requestXml1, true);
        
        response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "   <whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                + "    <leaf4>leaf4</leaf4>"
                + "    <leaf1>rock</leaf1>"
                + "    <containerWithLeaflist/>"
                + "   </whenMustRefersSiblings>"
                + " </data>"
                + "</rpc-reply>"
                ;
        verifyGet(response);
        
	    // create leaf3 refers with not() of non-existing leaf-list
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <containerWithLeaflist>" +
                "   <leaf3>harry</leaf3>" +
                "  </containerWithLeaflist>" +
                "</whenMustRefersSiblings>"  ;
        ncResponse = editConfig(m_server, m_clientInfo, requestXml1, true);
        response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "   <whenMustRefersSiblings xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                + "    <leaf4>leaf4</leaf4>"
                + "    <leaf1>rock</leaf1>"
                + "    <containerWithLeaflist>"
                + "     <leaf3>harry</leaf3>" 
                + "    </containerWithLeaflist>"
                + "   </whenMustRefersSiblings>"
                + " </data>"
                + "</rpc-reply>"
                ;
        verifyGet(response);
	}
	
    /*
     * Below UT "testMustOnDefault()" verifies the anomaly (FNMS-28050) 
     * Summary: 
     * the problem arises because this is a must condition that needs a default value to
     * be instantiated before the must is being evaluated. The default value is on a leaf that is controlled by a when condition, so the
     * default is not instantiated by AddDefaultDataInterceptor. Because that default value is not there yet at that time, the must
     * condition fails.
     * 
     * Commenting the below UT until FNMS-31166 is completed.
     */
    
    //TODO : FNMS-31166
    //@Test
    public void testMustOnDefault() throws Exception {
        getModelNode();
        initialiseInterceptor();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<otherRootContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                " <must-validation-with-default xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "   <key>42</key>" +
                "   <ip-version>ipv4</ip-version>" +
                "   <group-ipv4-address>10.10.10.10</group-ipv4-address>" +
                " </must-validation-with-default>" +
                "</otherRootContainer>";
        editConfig(m_server, m_clientInfo, requestXml1, true);
        
        String response = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" 
                + "<data>"
                + "<whenmust:otherRootContainer xmlns:whenmust=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                + "  <whenmust:must-validation-with-default>" 
                + "    <whenmust:group-ipv4-address>10.10.10.10</whenmust:group-ipv4-address>"
                + "    <whenmust:ip-version>ipv4</whenmust:ip-version>" 
                + "    <whenmust:key>42</whenmust:key>"
                //below leaf should be present as part of default creation
                + "    <whenmust:group-ipv4-address-end>10.10.10.10</whenmust:group-ipv4-address-end>"
                + "  </whenmust:must-validation-with-default>" 
                + "</whenmust:otherRootContainer>" 
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>" 
                + "</rpc-reply>";

        verifyGet(response);
    }
	
	@Test
	public void testWhenMustWithSelfStartOnCoreOperation() throws Exception{
	    getModelNode();
	    
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<otherRootContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <otherRootLeaf>root</otherRootLeaf>" +
                "  <selfStartRefersSibling>selfStartRefersSibling</selfStartRefersSibling>" +
                "</otherRootContainer>";
        NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals(1,ncResponse.getErrors().size());
        assertEquals("/whenmust:otherRootContainer/whenmust:selfStartRefersSibling", ncResponse.getErrors().get(0).getErrorPath());
        assertEquals("Violate when constraints: ./../otherRootLeaf = 'test'", ncResponse.getErrors().get(0).getErrorMessage());
             
	    // create selfStartRefersSibling
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<otherRootContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <otherRootLeaf>test</otherRootLeaf>" +
                "  <selfStartRefersSibling>selfStartRefersSibling</selfStartRefersSibling>" +
                "</otherRootContainer>";
        editConfig(m_server, m_clientInfo, requestXml1, true);
        
        String response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "  <otherRootContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                + "   <otherRootLeaf>test</otherRootLeaf>"
                + "   <otherDefaultLeaf>otherDefaultLeaf</otherDefaultLeaf>"
                + "   <selfStartRefersSibling>selfStartRefersSibling</selfStartRefersSibling>"
                + "  </otherRootContainer>"
                + " </data>"
                + "</rpc-reply>"
                ;
        verifyGet(response);
        
	    // create selfStartRefersDefaultSibling
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<otherRootContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">" +
                "  <otherRootLeaf>test</otherRootLeaf>" +
                "  <selfStartRefersDefaultSibling>selfStartRefersDefaultSibling</selfStartRefersDefaultSibling>" +
                "</otherRootContainer>";
        editConfig(m_server, m_clientInfo, requestXml1, true);
        
        response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "  <otherRootContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprValidation-test\">"
                + "   <otherRootLeaf>test</otherRootLeaf>"
                + "   <otherDefaultLeaf>otherDefaultLeaf</otherDefaultLeaf>"
                + "   <selfStartRefersSibling>selfStartRefersSibling</selfStartRefersSibling>"
                + "   <selfStartRefersDefaultSibling>selfStartRefersDefaultSibling</selfStartRefersDefaultSibling>"
                + "  </otherRootContainer>"
                + " </data>"
                + "</rpc-reply>"
                ;
        verifyGet(response);
	}
}
