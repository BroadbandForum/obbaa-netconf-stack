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

import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.verifyGetConfigWithPassword;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xml.sax.SAXException;

@RunWith(RequestScopeJunitRunner.class)
public class ChoiceDSValidatorTest extends AbstractDataStoreValidatorTest {

    @Test
    public void testMandatoryChoiceWithContainers() throws ModelNodeInitException, SAXException, IOException {
        getModelNode();
        initialiseInterceptor();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <testMandatory>" +
                "   <key>test</key>" +
                "   <udp>" +
                "   <port>5555</port>" +
                "  </udp>" +
                "   <polling>" +
                "   <period>60</period>" +
                "  </polling>" +
                " </testMandatory>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true); // positive case
        String expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:testMandatory>"
                + "<choice-case-test:dummyContainer/>"
                + "<choice-case-test:key>test</choice-case-test:key>"
                + "<choice-case-test:udp>"
                + "<choice-case-test:port>5555</choice-case-test:port>"
                + "</choice-case-test:udp>"
                + "<choice-case-test:polling>"
                + "<choice-case-test:period>60</choice-case-test:period>"
                + "</choice-case-test:polling>"
                + "</choice-case-test:testMandatory>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);          
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <testMandatory>" +
                "   <key>test</key>" +
                "   <udp xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" nc:operation=\"delete\" />" +
                " </testMandatory>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true); // positive case

        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <testMandatory>" +
                "   <key>test</key>" +
                "   <polling xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" nc:operation=\"delete\" />" +
                " </testMandatory>" +
                "</choice-container>" ;
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false); // shouldn't delete mandatory choice node single child case
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:testMandatory[choice-case-test:key='test']/choice-case-test:device-connection", "Missing mandatory node - device-connection", NetconfRpcErrorTag.DATA_MISSING);
    }

    @Test
    public void testCaseLeafWithWhen() throws Exception {
        getModelNode();

        // Mandatory leaf under choice with when condition(when condition evaluates to false)
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <choice-case-leaf>test</choice-case-leaf>" +
                "</choice-container>";
        editConfig(m_server, m_clientInfo, requestXml, true);

        String expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\"><data>"
                +"<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                +"<choice-case-test:choice-case-leaf>test</choice-case-test:choice-case-leaf>"
                +"</choice-case-test:choice-container>"
                +"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/></data>"
                + "</rpc-reply>";
        verifyGet(expResponse );

        //Mandatory leaf condition when the when condition evaluates to true.
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <choice-case-leaf>test2</choice-case-leaf>" +
                "</choice-container>";
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:choice-case-leaf-with-pattern", "Missing mandatory node - choice-case-leaf-with-pattern", NetconfRpcErrorTag.DATA_MISSING);

        //Mandatory leaf condition when the when condition evaluates to true(positive case)
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <choice-case-leaf>test2</choice-case-leaf>" +
                " <choice-case-leaf-with-pattern>test</choice-case-leaf-with-pattern>" +
                "</choice-container>";

        editConfig(m_server, m_clientInfo, requestXml, true);

        expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\"><data>"
                +"<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                +"<choice-case-test:choice-case-leaf>test2</choice-case-test:choice-case-leaf>"
                +"<choice-case-test:choice-case-leaf-with-pattern>test</choice-case-test:choice-case-leaf-with-pattern>"
                +"</choice-case-test:choice-container>"
                +"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/></data>"
                + "</rpc-reply>";
        verifyGet(expResponse );

        //Verification of auto-deletion when when evaluates to false
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <choice-case-leaf>test</choice-case-leaf>" +
                "</choice-container>";
        editConfig(m_server, m_clientInfo, requestXml, true);

        expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\"><data>"
                +"<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                +"<choice-case-test:choice-case-leaf>test</choice-case-test:choice-case-leaf>"
                +"</choice-case-test:choice-container>"
                +"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/></data>"
                + "</rpc-reply>";
        verifyGet(expResponse );

    }

    @Test
    public void testCaseLeafListWithWhen() throws Exception {
        getModelNode();

        // LeafList with min-elements under choice with when condition(when condition evaluates to false)
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <leaf1>test</leaf1>" +
                "</choice-container>";
        editConfig(m_server, m_clientInfo, requestXml, true);

        String expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\"><data>"
                +"<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                +"<choice-case-test:leaf1>test</choice-case-test:leaf1>"
                +"</choice-case-test:choice-container>"
                +"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/></data>"
                + "</rpc-reply>";
        verifyGet(expResponse );

        // LeafList with min-elements under choice with when condition(when condition evaluates to true)
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <leaf1>test2</leaf1>" +
                "</choice-container>";
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:choice-case-leaf-list-with-pattern", "Missing mandatory node - choice-case-leaf-list-with-pattern", NetconfRpcErrorTag.DATA_MISSING);

        // LeafList with min-elements under choice with when condition(when condition evaluates to true), positive case
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <leaf1>test2</leaf1>" +
                " <choice-case-leaf-list-with-pattern>string1</choice-case-leaf-list-with-pattern>" +
                " <choice-case-leaf-list-with-pattern>string2</choice-case-leaf-list-with-pattern>" +
                "</choice-container>";

        editConfig(m_server, m_clientInfo, requestXml, true);

        expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\"><data>"
                +"<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                +"<choice-case-test:leaf1>test2</choice-case-test:leaf1>"
                +"<choice-case-test:choice-case-leaf-list-with-pattern>string1</choice-case-test:choice-case-leaf-list-with-pattern>"
                +"<choice-case-test:choice-case-leaf-list-with-pattern>string2</choice-case-test:choice-case-leaf-list-with-pattern>"
                +"</choice-case-test:choice-container>"
                +"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/></data>"
                + "</rpc-reply>";
        verifyGet(expResponse );

        //Verification of auto-deletion when when evaluates to false
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <leaf1>test</leaf1>" +
                "</choice-container>";
        editConfig(m_server, m_clientInfo, requestXml, true);

        expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\"><data>"
                +"<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                +"<choice-case-test:leaf1>test</choice-case-test:leaf1>"
                +"</choice-case-test:choice-container>"
                +"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/></data>"
                + "</rpc-reply>";
        verifyGet(expResponse );
    }

    @Test
    public void testCaseListWithWhen() throws Exception {
        getModelNode();

        // List with min-elements under choice with when condition(when condition evaluates to false)
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<leaf-for-list>test</leaf-for-list>" +
                "</choice-container>";
        editConfig(m_server, m_clientInfo, requestXml, true);

        String expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\"><data>"
                +"<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                +"<choice-case-test:leaf-for-list>test</choice-case-test:leaf-for-list>"
                +"</choice-case-test:choice-container>"
                +"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/></data>"
                + "</rpc-reply>";
        verifyGet(expResponse );

        // List with min-elements under choice with when condition(when condition evaluates to true)
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<leaf-for-list>test2</leaf-for-list>" +
                "</choice-container>";
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:choice-case-list-with-when", "Missing mandatory node - choice-case-list-with-when", NetconfRpcErrorTag.DATA_MISSING);

        // List with min-elements under choice with when condition(when condition evaluates to true), positive case.
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<leaf-for-list>test2</leaf-for-list>" +
                "<choice-case-list-with-when>" +
                "<name>test1</name>" +
                "</choice-case-list-with-when>" +
                "</choice-container>";

        editConfig(m_server, m_clientInfo, requestXml, true);

        expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\"><data>"
                +"<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                +"<choice-case-test:leaf-for-list>test2</choice-case-test:leaf-for-list>"
                +"<choice-case-test:choice-case-list-with-when>"
                +"<choice-case-test:name>test1</choice-case-test:name>"
                +"</choice-case-test:choice-case-list-with-when>"
                +"</choice-case-test:choice-container>"
                +"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/></data>"
                + "</rpc-reply>";
        verifyGet(expResponse );

        //Verification of auto-deletion when when evaluates to false
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<leaf-for-list>test</leaf-for-list>" +
                "</choice-container>";

        editConfig(m_server, m_clientInfo, requestXml, true);

        expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\"><data>"
                +"<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                +"<choice-case-test:leaf-for-list>test</choice-case-test:leaf-for-list>"
                +"</choice-case-test:choice-container>"
                +"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/></data>"
                + "</rpc-reply>";
        verifyGet(expResponse );
    }

    @Test
    public void testNestedChoiceWithWhenAndMandatory() throws Exception {
        getModelNode();

        // Nested leaf min-elements under choice with when condition(when condition evaluates to false)
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<choice-case-nested-leaf>test2</choice-case-nested-leaf>" +
                "</choice-container>";
        editConfig(m_server, m_clientInfo, requestXml, true);

        String expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\"><data>"
                +"<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                +"<choice-case-test:choice-case-nested-leaf>test2</choice-case-test:choice-case-nested-leaf>"
                +"</choice-case-test:choice-container>"
                +"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/></data>"
                + "</rpc-reply>";
        verifyGet(expResponse );

        // Leaf with min-elements under choice with when condition(when condition evaluates to true), negative case

        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<choice-case-nested-leaf>test</choice-case-nested-leaf>" +
                "</choice-container>";
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:choice-case-nested-leaf-with-when", "Missing mandatory node - choice-case-nested-leaf-with-when", NetconfRpcErrorTag.DATA_MISSING);

        // Leaf with min-elements under choice with when condition(when condition evaluates to true), positive case
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<choice-case-nested-leaf>test</choice-case-nested-leaf>" +
                "<choice-case-nested-leaf-with-when>success</choice-case-nested-leaf-with-when>" +
                "</choice-container>";

        editConfig(m_server, m_clientInfo, requestXml, true);

        expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\"><data>"
                +"<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                +"<choice-case-test:choice-case-nested-leaf>test</choice-case-test:choice-case-nested-leaf>"
                +"<choice-case-test:choice-case-nested-leaf-with-when>success</choice-case-test:choice-case-nested-leaf-with-when>"
                +"</choice-case-test:choice-container>"
                +"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/></data>"
                + "</rpc-reply>";

        verifyGet(expResponse );

        //Switchover from leaf case to leaf-list case
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<choice-case-nested-leaflist>test2</choice-case-nested-leaflist>" +
                "</choice-container>";

        editConfig(m_server, m_clientInfo, requestXml, true);

        expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\"><data>"
                +"<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                +"<choice-case-test:choice-case-nested-leaflist>test2</choice-case-test:choice-case-nested-leaflist>"
                +"</choice-case-test:choice-container>"
                +"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/></data>"
                + "</rpc-reply>";

        verifyGet(expResponse );
    }

    @Test
    public void testMandatoryChoice() throws ModelNodeInitException, SAXException, IOException {
        getModelNode();
        initialiseInterceptor();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <testMandatory>" +
                "   <key>test</key>" +
                " <dummyContainer>" +
                "   <dummyLeaf>test</dummyLeaf>" +
                " </dummyContainer>" +
                " </testMandatory>" +
                "</choice-container>" ;
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:testMandatory[choice-case-test:key='test']/choice-case-test:device-connection", "Missing mandatory node - device-connection", NetconfRpcErrorTag.DATA_MISSING);
    }

    @Test
    public void testMinElementsTogetherInChoiceWithMust() throws ModelNodeInitException, SAXException, IOException {
        getModelNode();
        initialiseInterceptor();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <topList1>" +
                "   <key>test</key>" +
                " </topList1>" +
                " <topList2>" +
                "   <key>test</key>" +
                " </topList2>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true);

        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <topList1>" +
                "   <key>test1</key>" +
                "   <tag>" +
                "       <index>1</index>" +
                "   </tag>" +
                " </topList1>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true);
    }

    @Test
    public void testMinElementsTogetherInChoiceWithMustNegativeCase() throws ModelNodeInitException, SAXException, IOException {
        getModelNode();
        initialiseInterceptor();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <topList1>" +
                "   <key>test</key>" +
                "   <tag>" +
                "       <index>1</index>" +
                "   </tag>" +
                " </topList1>" +
                "</choice-container>" ;
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:topList1[choice-case-test:key='test']/choice-case-test:tag[choice-case-test:index='1']", 
                "Violate must constraints: count(/choice-container/topList2)>0", NetconfRpcErrorTag.OPERATION_FAILED);
    }

    @Test
    public void testMandatoryChoiceWithPresenceContainerAlone() throws ModelNodeInitException, SAXException, IOException {
        getModelNode();
        initialiseInterceptor();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <testMandatoryWithPresenceContainerAlone>" +
                "   <key>one</key>" +
                "   <presenceContainer>" +
                "       <onlyLeaf>test</onlyLeaf>" +
                "   </presenceContainer>" +
                " </testMandatoryWithPresenceContainerAlone>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true);

        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <testMandatoryWithPresenceContainerAlone>" +
                "   <key>two</key>" +
                " </testMandatoryWithPresenceContainerAlone>" +
                "</choice-container>" ;
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:testMandatoryWithPresenceContainerAlone[choice-case-test:key='two']/choice-case-test:mandatoryChoiceWithPresenceContainer", 
                "Missing mandatory node - mandatoryChoiceWithPresenceContainer", NetconfRpcErrorTag.DATA_MISSING);
    }

    @Test
    public void testMandatoryChoiceUnderPresenceContainer() throws ModelNodeInitException, SAXException, IOException {
        getModelNode();
        initialiseInterceptor();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <testMandatoryChoiceUnderPresenceContainer>" +
                "   <key>one</key>" +
                " </testMandatoryChoiceUnderPresenceContainer>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true);

        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <testMandatoryChoiceUnderPresenceContainer>" +
                "   <key>one</key>" +
                "   <presenceContainer>" +
                "       <outsideLeaf>test</outsideLeaf>" +
                "   </presenceContainer>" +
                " </testMandatoryChoiceUnderPresenceContainer>" +
                "</choice-container>" ;
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:testMandatoryChoiceUnderPresenceContainer[choice-case-test:key='one']/choice-case-test:presenceContainer/choice-case-test:mandatoryChoice", 
                "Missing mandatory node - mandatoryChoice", NetconfRpcErrorTag.DATA_MISSING);
    }

    @Test
    public void testMandatoryChoiceWithPresenceContainerWithMandatoryLeafs() throws ModelNodeInitException, SAXException, IOException {
        getModelNode();
        initialiseInterceptor();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <testMandatoryWithPresenceContainerWithMandatoryLeaf>" +
                "   <key>one</key>" +
                "   <presenceContainer>" +
                "       <onlyLeaf>test</onlyLeaf>" +
                "   </presenceContainer>" +
                " </testMandatoryWithPresenceContainerWithMandatoryLeaf>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true);

        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <testMandatoryWithPresenceContainerWithMandatoryLeaf>" +
                "   <key>one</key>" +
                "   <secondCasePresenceContainer>" +
                "       <anotherLeaf>test</anotherLeaf>" +
                "   </secondCasePresenceContainer>" +
                " </testMandatoryWithPresenceContainerWithMandatoryLeaf>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true);

        String expReponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\"><data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:testMandatoryWithPresenceContainerWithMandatoryLeaf>"
                + "<choice-case-test:key>one</choice-case-test:key>"
                + "<choice-case-test:secondCasePresenceContainer>"
                + "<choice-case-test:anotherLeaf>test</choice-case-test:anotherLeaf>"
                + "</choice-case-test:secondCasePresenceContainer>"
                + "</choice-case-test:testMandatoryWithPresenceContainerWithMandatoryLeaf>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/></data>"
                + "</rpc-reply>";
        verifyGet(expReponse );

        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <testMandatoryWithPresenceContainerWithMandatoryLeaf>" +
                "   <key>two</key>" +
                "   <presenceContainer>" +
                "       <onlyLeaf>test</onlyLeaf>" +
                "   </presenceContainer>" +
                "   <presenceWithMandatory>" +
                "       <testingLeaf>test</testingLeaf>" +
                "   </presenceWithMandatory>" +
                " </testMandatoryWithPresenceContainerWithMandatoryLeaf>" +
                "</choice-container>" ;
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:testMandatoryWithPresenceContainerWithMandatoryLeaf[choice-case-test:key='two']/choice-case-test:presenceWithMandatory/choice-case-test:mandatoryLeaf", 
                "Missing mandatory node - mandatoryLeaf", NetconfRpcErrorTag.DATA_MISSING);
    }

    @Test
    public void testMandatoryChoiceWithListAlone() throws ModelNodeInitException, SAXException, IOException {
        getModelNode();
        initialiseInterceptor();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <testMandatoryWithListAlone>" +
                "   <key>one</key>" +
                "   <testList>" +
                "       <key>test</key>" +
                "   </testList>" +
                " </testMandatoryWithListAlone>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true);
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <testMandatoryWithListAlone>" +
                "   <key>one</key>" +
                "   <testList xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\">" +
                "       <key>test</key>" +
                "   </testList>" +
                " </testMandatoryWithListAlone>" +
                "</choice-container>" ;
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:testMandatoryWithListAlone[choice-case-test:key='one']/choice-case-test:mandatoryChoice", "Missing mandatory node - mandatoryChoice", NetconfRpcErrorTag.DATA_MISSING);

        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <testMandatoryWithListAlone>" +
                "   <key>two</key>" +
                " </testMandatoryWithListAlone>" +
                "</choice-container>" ;
        response = editConfig(m_server, m_clientInfo, requestXml, false);
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:testMandatoryWithListAlone[choice-case-test:key='two']/choice-case-test:mandatoryChoice", "Missing mandatory node - mandatoryChoice", NetconfRpcErrorTag.DATA_MISSING);
    }

    @Test
    public void testwhenWithMandatoryChoicePositiveCase() throws ModelNodeInitException, SAXException, IOException {
        getModelNode();
        initialiseInterceptor();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <rootNodeLeaf>test</rootNodeLeaf>" +
                " <testList>" +
                "   <key>whenNman</key>" +
                "   <whenWithMandatoryLeaf1>check</whenWithMandatoryLeaf1>" +
                " </testList>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true); // positive case
        String expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:rootNodeLeaf>test</choice-case-test:rootNodeLeaf>"
                + "<choice-case-test:testList>"
                + "<choice-case-test:choiceWithDefaultContainer>"
                + "<choice-case-test:case1LeafUnderContainer>imDef-leafWithDefValue</choice-case-test:case1LeafUnderContainer>"
                + "</choice-case-test:choiceWithDefaultContainer>"
                + "<choice-case-test:choiceWithDefaultLeaf>imDefault</choice-case-test:choiceWithDefaultLeaf>"
                + "<choice-case-test:key>whenNman</choice-case-test:key>"
                + "<choice-case-test:whenWithMandatoryLeaf1>check</choice-case-test:whenWithMandatoryLeaf1>"
                + "</choice-case-test:testList>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);          
    }


    @Test
    public void testwhenWithMandatoryChoiceNegativeCase1() throws ModelNodeInitException, SAXException, IOException {
        getModelNode();
        initialiseInterceptor();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <rootNodeLeaf>test</rootNodeLeaf>" +
                " <testList>" +
                "   <key>test</key>" +
                "   <whenWithMandatoryLeaf1>check</whenWithMandatoryLeaf1>" +
                " </testList>" +
                "</choice-container>" ;
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false); // when not satisfied
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:testList[choice-case-test:key='test']/choice-case-test:whenWithMandatoryLeaf1", "Violate when constraints: key = 'whenNman'", NetconfRpcErrorTag.UNKNOWN_ELEMENT);
    }

    @Test
    public void testwhenWithMandatoryChoiceNegativeCase2() throws ModelNodeInitException, SAXException, IOException {
        getModelNode();
        initialiseInterceptor();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <rootNodeLeaf>test</rootNodeLeaf>" +
                " <testList>" +
                "   <key>whenNman</key>" +
                " </testList>" +
                "</choice-container>" ;
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false); // when satisfied but mandatory missing
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:testList[choice-case-test:key='whenNman']/choice-case-test:whenWithMandatoryChoice", "Missing mandatory node - whenWithMandatoryChoice", NetconfRpcErrorTag.DATA_MISSING);
    }

    @Test
    public void testChoiceWithDefaultCase1() throws Exception {

        /**
         * By creating a entry in a list, 
         * Default data under that choice should be instantiated, 
         * It should avoid instantiating, choice child data with when condition (if not satisfied) though it has defaults. 
         */

        getModelNode();
        initialiseInterceptor();

        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <rootNodeLeaf>test</rootNodeLeaf>" +
                " <testList>" +
                "   <key>test</key>" +
                " </testList>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true); // simple default case of choice
        String expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<validation:choice-container xmlns:validation=\"urn:org:bbf:pma:choice-case-test\">"
                + "<validation:rootNodeLeaf>test</validation:rootNodeLeaf>"
                + "<validation:testList>"
                + "<validation:choiceWithDefaultContainer>" // Container instantiated here with leaf and its default value below
                + "<validation:case1LeafUnderContainer>imDef-leafWithDefValue</validation:case1LeafUnderContainer>"
                + "</validation:choiceWithDefaultContainer>"
                + "<validation:choiceWithDefaultLeaf>imDefault</validation:choiceWithDefaultLeaf>" // Leaf with default value under choice's default case
                + "<validation:key>test</validation:key>"
                + "</validation:testList>"
                + "</validation:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);              
    }

    @Test
    public void testChoiceWithDefaultCase2() throws Exception {
        getModelNode();
        initialiseInterceptor();

        /**
         * choice 
         * - - default case 
         * - - - leaf with default value. 
         * - - non default case 
         * - - - multiple leafs with values respectively. 
         *
         * if any one leaf from non-default-case was removed, it should not instantiate the default leaf of default case. 
         */

        // Creating 4 leafs of non-default-case
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <testList1>" +
                "   <key>test</key>" +
                "   <ip-address>ipa</ip-address>" +
                "   <ip-port>port</ip-port>" +
                "   <transport-protocol>transport</transport-protocol>" +
                "   <username>username</username>" +
                "   <password>password</password>" +
                " </testList1>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true); 
        String expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:testList1>"
                + "<choice-case-test:ip-address>ipa</choice-case-test:ip-address>"
                + "<choice-case-test:ip-port>port</choice-case-test:ip-port>"
                + "<choice-case-test:key>test</choice-case-test:key>"
                + "<choice-case-test:password>password</choice-case-test:password>"
                + "<choice-case-test:transport-protocol>transport</choice-case-test:transport-protocol>"
                + "<choice-case-test:username>username</choice-case-test:username>"
                + "</choice-case-test:testList1>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGetConfigWithPassword(m_server,expResponse, MESSAGE_ID);

        // Removing one of the leaf from there. 
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <testList1>" +
                "   <key>test</key>" +
                "   <username xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" nc:operation=\"delete\">username</username>" +
                " </testList1>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true); 
        expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:testList1>"
                + "<choice-case-test:ip-address>ipa</choice-case-test:ip-address>"
                + "<choice-case-test:ip-port>port</choice-case-test:ip-port>"
                + "<choice-case-test:key>test</choice-case-test:key>"
                + "<choice-case-test:password>password</choice-case-test:password>"
                + "<choice-case-test:transport-protocol>transport</choice-case-test:transport-protocol>"
                + "</choice-case-test:testList1>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGetConfigWithPassword(m_server, expResponse,MESSAGE_ID);

        // Removing all other leafs. Now it should instantiate the default leaf of default case. 
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <testList1>" +
                "   <key>test</key>" +
                "   <ip-address xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" nc:operation=\"delete\">ipa</ip-address>" +
                "   <ip-port xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" nc:operation=\"delete\">port</ip-port>" +
                "   <transport-protocol xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" nc:operation=\"delete\">transport</transport-protocol>" +
                "   <password xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" nc:operation=\"delete\">password</password>" +
                " </testList1>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true);
        expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:testList1>"
                + "<choice-case-test:duid/>"
                + "<choice-case-test:key>test</choice-case-test:key>"
                + "</choice-case-test:testList1>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGetConfigWithPassword(m_server,expResponse, MESSAGE_ID);

        // Now setting default case value. so it should remove the other leafs

        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <testList1>" +
                "   <key>test</key>" +
                "   <ip-address>ipa</ip-address>" +
                "   <ip-port>port</ip-port>" +
                "   <transport-protocol>transport</transport-protocol>" +
                "   <username>username</username>" +
                "   <password>password</password>" +
                " </testList1>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true); 
        expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:testList1>"
                + "<choice-case-test:ip-address>ipa</choice-case-test:ip-address>"
                + "<choice-case-test:ip-port>port</choice-case-test:ip-port>"
                + "<choice-case-test:key>test</choice-case-test:key>"
                + "<choice-case-test:password>password</choice-case-test:password>"
                + "<choice-case-test:transport-protocol>transport</choice-case-test:transport-protocol>"
                + "<choice-case-test:username>username</choice-case-test:username>"
                + "</choice-case-test:testList1>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGetConfigWithPassword(m_server,expResponse, MESSAGE_ID);

        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <testList1>" +
                "   <key>test</key>" +
                "   <duid/>" +
                " </testList1>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true); 

        expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:testList1>"
                + "<choice-case-test:duid/>"
                + "<choice-case-test:key>test</choice-case-test:key>"
                + "</choice-case-test:testList1>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse); 
    }

    @Test
    public void testChoiceWithDefaultCase3() throws Exception {
        getModelNode();
        initialiseInterceptor();

        /**
         * Follow-up from the above UT, change is, along with the leafs in non-default-leafs, there is container also. 
         */

        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <testList2>" +
                "   <key>test</key>" +
                "   <ip-address>ipa</ip-address>" +
                "   <ip-port>port</ip-port>" +
                "   <transport-protocol>transport</transport-protocol>" +
                "   <username>username</username>" +
                "   <password>password</password>" +
                "   <deviceTypeContainer>" +
                "       <deviceType>deviceType</deviceType>" +
                "   </deviceTypeContainer>" +
                " </testList2>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true); 
        String expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:testList2>"
                + "<choice-case-test:ip-address>ipa</choice-case-test:ip-address>"
                + "<choice-case-test:ip-port>port</choice-case-test:ip-port>"
                + "<choice-case-test:key>test</choice-case-test:key>"
                + "<choice-case-test:password>password</choice-case-test:password>"
                + "<choice-case-test:transport-protocol>transport</choice-case-test:transport-protocol>"
                + "<choice-case-test:username>username</choice-case-test:username>"
                + "<choice-case-test:deviceTypeContainer>"
                + "<choice-case-test:deviceType>deviceType</choice-case-test:deviceType>"
                + "</choice-case-test:deviceTypeContainer>"
                + "</choice-case-test:testList2>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGetConfigWithPassword(m_server, expResponse, MESSAGE_ID);

        // Remove all leafs except container
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <testList2>" +
                "   <key>test</key>" +
                "   <username xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" nc:operation=\"delete\">username</username>" +
                "   <ip-address xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" nc:operation=\"delete\">ipa</ip-address>" +
                "   <ip-port xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" nc:operation=\"delete\">port</ip-port>" +
                "   <transport-protocol xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" nc:operation=\"delete\">transport</transport-protocol>" +
                "   <password xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" nc:operation=\"delete\">password</password>" +
                " </testList2>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true); 
        expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:testList2>"
                + "<choice-case-test:key>test</choice-case-test:key>"
                + "<choice-case-test:deviceTypeContainer>"
                + "<choice-case-test:deviceType>deviceType</choice-case-test:deviceType>"
                + "</choice-case-test:deviceTypeContainer>"
                + "</choice-case-test:testList2>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse); 

        // now remove container too. It should reset to duid
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <testList2>" +
                "   <key>test</key>" +
                "   <deviceTypeContainer xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" nc:operation=\"delete\" />" +
                " </testList2>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true);
        expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:testList2>"
                + "<choice-case-test:duid/>"
                + "<choice-case-test:key>test</choice-case-test:key>"
                + "</choice-case-test:testList2>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse); 
    }

    @Test
    public void testChoiceWithDefaultCase4() throws ModelNodeInitException, SAXException, IOException {
        getModelNode();
        initialiseInterceptor();

        /**
         * Follow-up from the above UT, change is, It has nested choices
         */

        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <testList3>" +
                "   <key>test</key>" +
                "   <username>username</username>" +
                " </testList3>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true); 
        String expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:testList3>"
                + "<choice-case-test:key>test</choice-case-test:key>"
                + "<choice-case-test:username>username</choice-case-test:username>"
                + "<choice-case-test:fxLeaf>fxType</choice-case-test:fxLeaf>"
                + "</choice-case-test:testList3>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);

        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <testList3>" +
                "   <key>test</key>" +
                "   <username xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" nc:operation=\"delete\">username</username>" +
                " </testList3>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true); 
        expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:testList3>"
                + "<choice-case-test:key>test</choice-case-test:key>"
                + "<choice-case-test:fxLeaf>fxType</choice-case-test:fxLeaf>"
                + "</choice-case-test:testList3>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);
    }

    @Test
    public void testChoiceWithDefaultCase5() throws ModelNodeInitException, SAXException, IOException {
        getModelNode();
        initialiseInterceptor();

        /**
         * Follow-up from the above UT, change is, two containers in non-default case and no leafs. So there are no change nodes in Edit node. 
         * Going to create both the containers, and delete one container -> it should not move to duid case. 
         */

        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <testList4>" +
                "   <key>test</key>" +
                "   <deviceTypeContainer>" +
                "       <deviceType>deviceType</deviceType>" +
                "   </deviceTypeContainer>" +
                "   <pollingDetails>" +
                "       <pollingTime>60</pollingTime>" +
                "   </pollingDetails>" +
                " </testList4>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true); 
        String expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:testList4>"
                + "<choice-case-test:key>test</choice-case-test:key>"
                + "<choice-case-test:deviceTypeContainer>"
                + "<choice-case-test:deviceType>deviceType</choice-case-test:deviceType>"
                + "</choice-case-test:deviceTypeContainer>"
                + "<choice-case-test:pollingDetails>"
                + "<choice-case-test:pollingTime>60</choice-case-test:pollingTime>"
                + "</choice-case-test:pollingDetails>"
                + "</choice-case-test:testList4>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);

        // Remove one container from this. 
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <testList4>" +
                "   <key>test</key>" +
                "   <deviceTypeContainer xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" nc:operation=\"delete\" />" +
                " </testList4>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true);

        expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:testList4>"
                + "<choice-case-test:key>test</choice-case-test:key>"
                + "<choice-case-test:pollingDetails>"
                + "<choice-case-test:pollingTime>60</choice-case-test:pollingTime>"
                + "</choice-case-test:pollingDetails>"
                + "</choice-case-test:testList4>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);

        // Now remoeve the other container too. now it should set the duid.
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <testList4>" +
                "   <key>test</key>" +
                "   <pollingDetails xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" nc:operation=\"delete\" />" +
                " </testList4>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true);

        expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:testList4>"
                + "<choice-case-test:key>test</choice-case-test:key>"
                + "<choice-case-test:duid/>"
                + "</choice-case-test:testList4>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);
    }

    @Test
    public void testChoiceWithDefaultCase6() throws ModelNodeInitException, SAXException, IOException {
        getModelNode();
        initialiseInterceptor();

        /**
         *  Almost same as above. Now its time for lists instead of containers
         */

        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <testList5>" +
                "   <key>test</key>" +
                "   <nonDefList1>" +
                "       <index>no1</index>" +
                "   </nonDefList1>" +
                "   <nonDefList2>" +
                "       <id>one</id>" +
                "   </nonDefList2>" +
                " </testList5>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true); 
        String expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:testList5>"
                + "<choice-case-test:key>test</choice-case-test:key>"
                + "<choice-case-test:nonDefList1>"
                + "<choice-case-test:index>no1</choice-case-test:index>"
                + "</choice-case-test:nonDefList1>"
                + "<choice-case-test:nonDefList2>"
                + "<choice-case-test:id>one</choice-case-test:id>"
                + "</choice-case-test:nonDefList2>"
                + "</choice-case-test:testList5>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);

        // Remove one list from this. 
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <testList5>" +
                "   <key>test</key>" +
                "   <nonDefList1 xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" nc:operation=\"delete\">" +
                "       <index>no1</index>" +
                "   </nonDefList1>" +
                " </testList5>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true);

        expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:testList5>"
                + "<choice-case-test:key>test</choice-case-test:key>"
                + "<choice-case-test:nonDefList2>"
                + "<choice-case-test:id>one</choice-case-test:id>"
                + "</choice-case-test:nonDefList2>"
                + "</choice-case-test:testList5>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);

        // Remove one list from this. 
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <testList5>" +
                "   <key>test</key>" +
                "   <nonDefList2 xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" nc:operation=\"delete\">" +
                "       <id>one</id>" +
                "   </nonDefList2>" +
                " </testList5>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true);

        expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:testList5>"
                + "<choice-case-test:key>test</choice-case-test:key>"
                + "<choice-case-test:duid/>"
                + "</choice-case-test:testList5>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);        
    }

    @Test
    public void testChoiceWithDefaultCase7() throws ModelNodeInitException, SAXException, IOException {
        getModelNode();
        initialiseInterceptor();

        /**
         *  With multiple list entries in list now
         */

        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <testList5>" +
                "   <key>test</key>" +
                "   <nonDefList1>" +
                "       <index>no1</index>" +
                "   </nonDefList1>" +
                "   <nonDefList2>" +
                "       <id>one</id>" +
                "   </nonDefList2>" +
                "   <nonDefList1>" +
                "       <index>no2</index>" +
                "   </nonDefList1>" +
                "   <nonDefList2>" +
                "       <id>two</id>" +
                "   </nonDefList2>" +
                " </testList5>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true); 
        String expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:testList5>"
                + "<choice-case-test:key>test</choice-case-test:key>"
                + "<choice-case-test:nonDefList1>"
                + "<choice-case-test:index>no1</choice-case-test:index>"
                + "</choice-case-test:nonDefList1>"
                + "<choice-case-test:nonDefList2>"
                + "<choice-case-test:id>one</choice-case-test:id>"
                + "</choice-case-test:nonDefList2>"
                + "<choice-case-test:nonDefList1>"
                + "<choice-case-test:index>no2</choice-case-test:index>"
                + "</choice-case-test:nonDefList1>"
                + "<choice-case-test:nonDefList2>"
                + "<choice-case-test:id>two</choice-case-test:id>"
                + "</choice-case-test:nonDefList2>"
                + "</choice-case-test:testList5>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);

        // Remove first entry from list. 
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <testList5>" +
                "   <key>test</key>" +
                "   <nonDefList1 xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" nc:operation=\"delete\">" +
                "       <index>no1</index>" +
                "   </nonDefList1>" +
                " </testList5>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true);
        expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:testList5>"
                + "<choice-case-test:key>test</choice-case-test:key>"
                + "<choice-case-test:nonDefList2>"
                + "<choice-case-test:id>one</choice-case-test:id>"
                + "</choice-case-test:nonDefList2>"
                + "<choice-case-test:nonDefList1>"
                + "<choice-case-test:index>no2</choice-case-test:index>"
                + "</choice-case-test:nonDefList1>"
                + "<choice-case-test:nonDefList2>"
                + "<choice-case-test:id>two</choice-case-test:id>"
                + "</choice-case-test:nonDefList2>"
                + "</choice-case-test:testList5>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);

        // Remove second entry from list. 
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <testList5>" +
                "   <key>test</key>" +
                "   <nonDefList1 xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" nc:operation=\"delete\">" +
                "       <index>no2</index>" +
                "   </nonDefList1>" +
                " </testList5>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true);
        expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:testList5>"
                + "<choice-case-test:key>test</choice-case-test:key>"
                + "<choice-case-test:nonDefList2>"
                + "<choice-case-test:id>one</choice-case-test:id>"
                + "</choice-case-test:nonDefList2>"
                + "<choice-case-test:nonDefList2>"
                + "<choice-case-test:id>two</choice-case-test:id>"
                + "</choice-case-test:nonDefList2>"
                + "</choice-case-test:testList5>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);

        // Remove third entry from list. 
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <testList5>" +
                "   <key>test</key>" +
                "   <nonDefList2 xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" nc:operation=\"delete\">" +
                "       <id>one</id>" +
                "   </nonDefList2>" +
                " </testList5>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true);
        expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:testList5>"
                + "<choice-case-test:key>test</choice-case-test:key>"
                + "<choice-case-test:nonDefList2>"
                + "<choice-case-test:id>two</choice-case-test:id>"
                + "</choice-case-test:nonDefList2>"
                + "</choice-case-test:testList5>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);

        // Remove fourth and that is last entry from list. 
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <testList5>" +
                "   <key>test</key>" +
                "   <nonDefList2 xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" nc:operation=\"delete\">" +
                "       <id>two</id>" +
                "   </nonDefList2>" +
                " </testList5>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true);
        expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:testList5>"
                + "<choice-case-test:key>test</choice-case-test:key>"
                + "<choice-case-test:duid/>"
                + "</choice-case-test:testList5>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);
    }

    @Test
    public void testChoiceWithWhenAndDefaultCase() throws Exception {
        /**
         * By creating a entry in a list, 
         * Default data under that choice should be instantiated and also the data in Choices with satisfying the when, 
         */

        getModelNode();
        initialiseInterceptor();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <rootNodeLeaf>test</rootNodeLeaf>" +
                " <testList>" +
                "   <key>whenNdef</key>" +
                " </testList>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true); // simple default case of choice with satisfying when

        String expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<validation:choice-container xmlns:validation=\"urn:org:bbf:pma:choice-case-test\">"
                + "<validation:rootNodeLeaf>test</validation:rootNodeLeaf>"
                + "<validation:testList>"
                + "<validation:choiceWithDefaultContainer>" // default container without when condition
                + "<validation:case1LeafUnderContainer>imDef-leafWithDefValue</validation:case1LeafUnderContainer>"
                + "</validation:choiceWithDefaultContainer>"
                + "<validation:choiceWithDefaultLeaf>imDefault</validation:choiceWithDefaultLeaf>"
                + "<validation:choiceWithWhenAndDefaultContainer>"
                + "<validation:leafWithDefValue>imDef-leafWithDefValue</validation:leafWithDefValue>"
                + "</validation:choiceWithWhenAndDefaultContainer>" // default container with when condition
                + "<validation:key>whenNdef</validation:key>"
                + "<validation:whenWithDefaultLeaf1>imDefault</validation:whenWithDefaultLeaf1>"
                + "</validation:testList>"
                + "</validation:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);  
    }

    @Test
    public void testChoiceWhenWithSingleCase() throws Exception {
        getModelNode();
        initialiseInterceptor();

        verifyGet("<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
                "   <data>\n" +
                "      <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n" +
                "   </data>\n" +
                "</rpc-reply>");
        //change node on ip-address-type impacts ip-address-choice with when case
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <rootNodeLeaf>test</rootNodeLeaf>" +
                " <testList>" +
                "   <key>whenNdef1</key>" +
                "   <ip-address-type>ipv4</ip-address-type>" +
                " </testList>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true);

        String expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<validation:choice-container xmlns:validation=\"urn:org:bbf:pma:choice-case-test\">"
                + "<validation:rootNodeLeaf>test</validation:rootNodeLeaf>"
                + "<validation:testList>"
                + "<validation:ip-address-type>ipv4</validation:ip-address-type>"
                + "<validation:choiceWithDefaultContainer>"
                + "<validation:case1LeafUnderContainer>imDef-leafWithDefValue</validation:case1LeafUnderContainer>"
                + "</validation:choiceWithDefaultContainer>"
                + "<validation:choiceWithDefaultLeaf>imDefault</validation:choiceWithDefaultLeaf>"
                + "<validation:key>whenNdef1</validation:key>"
                + "<validation:multicast-address3>225.0.0.1</validation:multicast-address3>"
                + "</validation:testList>"
                + "</validation:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);  

        //change node on ip-address-type impacts ip-address-choice with when case
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <rootNodeLeaf>test</rootNodeLeaf>" +
                " <testList>" +
                "   <key>whenNdef1</key>" +
                "   <ip-address-type>ipv6</ip-address-type>" +
                " </testList>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true);

        expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<validation:choice-container xmlns:validation=\"urn:org:bbf:pma:choice-case-test\">"
                + "<validation:rootNodeLeaf>test</validation:rootNodeLeaf>"
                + "<validation:testList>"
                + "<validation:ip-address-type>ipv6</validation:ip-address-type>"
                + "<validation:choiceWithDefaultContainer>"
                + "<validation:case1LeafUnderContainer>imDef-leafWithDefValue</validation:case1LeafUnderContainer>"
                + "</validation:choiceWithDefaultContainer>"
                + "<validation:choiceWithDefaultLeaf>imDefault</validation:choiceWithDefaultLeaf>"
                + "<validation:key>whenNdef1</validation:key>"
                + "</validation:testList>"
                + "</validation:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse); 
    }

    @Test
    public void testChoiceWhenWithSingleCaseWithExplicitValues() throws Exception {
        getModelNode();
        initialiseInterceptor(); 

        //change node on ip-address-type impacts ip-address-choice with when case
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <rootNodeLeaf>test</rootNodeLeaf>" +
                " <testList>" +
                "   <key>whenNdef1</key>" +
                "   <ip-address-type>ipv4</ip-address-type>" +
                "   <ip-address>" +
                "    <ipv4>0.0.0.0</ipv4>" +
                "   </ip-address>" +
                " </testList>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true);

        String expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<validation:choice-container xmlns:validation=\"urn:org:bbf:pma:choice-case-test\">"
                + "<validation:rootNodeLeaf>test</validation:rootNodeLeaf>"
                + "<validation:testList>"
                + "<validation:ip-address-type>ipv4</validation:ip-address-type>"
                + "<validation:choiceWithDefaultContainer>"
                + "<validation:case1LeafUnderContainer>imDef-leafWithDefValue</validation:case1LeafUnderContainer>"
                + "</validation:choiceWithDefaultContainer>"
                + "<validation:choiceWithDefaultLeaf>imDefault</validation:choiceWithDefaultLeaf>"
                + "<validation:key>whenNdef1</validation:key>"
                + "<validation:ip-address>"
                + "<validation:ipv4>0.0.0.0</validation:ipv4>"
                + "<validation:localhost>localhost</validation:localhost>"
                + "</validation:ip-address>"
                + "<validation:multicast-address>225.0.0.1</validation:multicast-address>"
                + "<validation:multicast-address3>225.0.0.1</validation:multicast-address3>"
                + "</validation:testList>"
                + "</validation:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);  

        // change localhost default value
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <rootNodeLeaf>test</rootNodeLeaf>" +
                " <testList>" +
                "   <key>whenNdef1</key>" +
                "   <ip-address-type>ipv4</ip-address-type>" +
                "   <ip-address>" +
                "    <ipv4>0.0.0.0</ipv4>" +
                "    <localhost>127.0.0.1</localhost>" +
                "   </ip-address>" +
                " </testList>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true);

        expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<validation:choice-container xmlns:validation=\"urn:org:bbf:pma:choice-case-test\">"
                + "<validation:rootNodeLeaf>test</validation:rootNodeLeaf>"
                + "<validation:testList>"
                + "<validation:ip-address-type>ipv4</validation:ip-address-type>"
                + "<validation:choiceWithDefaultContainer>"
                + "<validation:case1LeafUnderContainer>imDef-leafWithDefValue</validation:case1LeafUnderContainer>"
                + "</validation:choiceWithDefaultContainer>"
                + "<validation:choiceWithDefaultLeaf>imDefault</validation:choiceWithDefaultLeaf>"
                + "<validation:key>whenNdef1</validation:key>"
                + "<validation:ip-address>"
                + "<validation:ipv4>0.0.0.0</validation:ipv4>"
                + "<validation:localhost>127.0.0.1</validation:localhost>"
                + "</validation:ip-address>"
                + "<validation:multicast-address>225.0.0.1</validation:multicast-address>"
                + "<validation:multicast-address3>225.0.0.1</validation:multicast-address3>"
                + "</validation:testList>"
                + "</validation:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);  
    }

    @Test
    public void testChoiceWhenWithMultiCaseWithoutExplicitValues() throws Exception {
        getModelNode();
        initialiseInterceptor(); 

        //change node on ip-address-type impacts ip-address-choice with when case
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <rootNodeLeaf>test</rootNodeLeaf>" +
                " <testList>" +
                "   <key>whenNdef2</key>" +
                "   <ip-address-type>ipv4</ip-address-type>" +
                " </testList>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true);

        String expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<validation:choice-container xmlns:validation=\"urn:org:bbf:pma:choice-case-test\">"
                + "<validation:rootNodeLeaf>test</validation:rootNodeLeaf>"
                + "<validation:testList>"
                + "<validation:ip-address-type>ipv4</validation:ip-address-type>"
                + "<validation:choiceWithDefaultContainer>"
                + "<validation:case1LeafUnderContainer>imDef-leafWithDefValue</validation:case1LeafUnderContainer>"
                + "</validation:choiceWithDefaultContainer>"
                + "<validation:choiceWithDefaultLeaf>imDefault</validation:choiceWithDefaultLeaf>"
                + "<validation:key>whenNdef2</validation:key>"
                + "<validation:multicast-address3>225.0.0.1</validation:multicast-address3>"
                + "</validation:testList>"
                + "</validation:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);  
    }

    @Test
    public void testChoiceWhenWithMultiCaseWithExplicitValues() throws Exception {
        getModelNode();
        initialiseInterceptor(); 

        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <rootNodeLeaf>test</rootNodeLeaf>" +
                " <testList>" +
                "   <key>whenNdef2</key>" +
                "   <ip-address-type>ipv4</ip-address-type>" +
                "   <ip-address-multicase>" +
                "    <ipv4>0.0.0.0</ipv4>" +
                "   </ip-address-multicase>" +
                " </testList>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true);

        String expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<validation:choice-container xmlns:validation=\"urn:org:bbf:pma:choice-case-test\">"
                + "<validation:rootNodeLeaf>test</validation:rootNodeLeaf>"
                + "<validation:testList>"
                + "<validation:ip-address-type>ipv4</validation:ip-address-type>"
                + "<validation:choiceWithDefaultContainer>"
                + "<validation:case1LeafUnderContainer>imDef-leafWithDefValue</validation:case1LeafUnderContainer>"
                + "</validation:choiceWithDefaultContainer>"
                + "<validation:choiceWithDefaultLeaf>imDefault</validation:choiceWithDefaultLeaf>"
                + "<validation:key>whenNdef2</validation:key>"
                + "<validation:ip-address-multicase>"
                + "<validation:ipv4>0.0.0.0</validation:ipv4>"
                + "<validation:localhost>localhost</validation:localhost>"
                + "</validation:ip-address-multicase>"
                + "<validation:multicast-address3>225.0.0.1</validation:multicast-address3>"
                + "</validation:testList>"
                + "</validation:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);  

        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <rootNodeLeaf>test</rootNodeLeaf>" +
                " <testList>" +
                "   <key>whenNdef2</key>" +
                "   <ip-address-type>ipv4</ip-address-type>" +
                "   <multicast-address1>225.1.1.1</multicast-address1>" +
                " </testList>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true);

        expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<validation:choice-container xmlns:validation=\"urn:org:bbf:pma:choice-case-test\">"
                + "<validation:rootNodeLeaf>test</validation:rootNodeLeaf>"
                + "<validation:testList>"
                + "<validation:ip-address-type>ipv4</validation:ip-address-type>"
                + "<validation:choiceWithDefaultContainer>"
                + "<validation:case1LeafUnderContainer>imDef-leafWithDefValue</validation:case1LeafUnderContainer>"
                + "</validation:choiceWithDefaultContainer>"
                + "<validation:choiceWithDefaultLeaf>imDefault</validation:choiceWithDefaultLeaf>"
                + "<validation:key>whenNdef2</validation:key>"
                + "<validation:multicast-address1>225.1.1.1</validation:multicast-address1>"
                + "<validation:multicast-address3>225.0.0.1</validation:multicast-address3>"
                + "</validation:testList>"
                + "</validation:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);  
    }

    @Test
    public void testChoiceWhenWithDefaultMultiCase() throws Exception {
        getModelNode();
        initialiseInterceptor(); 

        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <rootNodeLeaf>test</rootNodeLeaf>" +
                " <testList>" +
                "   <key>whenNdef2</key>" +
                "   <ip-address-type>ipv4</ip-address-type>" +
                " </testList>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true);

        String expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<validation:choice-container xmlns:validation=\"urn:org:bbf:pma:choice-case-test\">"
                + "<validation:rootNodeLeaf>test</validation:rootNodeLeaf>"
                + "<validation:testList>"
                + "<validation:ip-address-type>ipv4</validation:ip-address-type>"
                + "<validation:choiceWithDefaultContainer>"
                + "<validation:case1LeafUnderContainer>imDef-leafWithDefValue</validation:case1LeafUnderContainer>"
                + "</validation:choiceWithDefaultContainer>"
                + "<validation:choiceWithDefaultLeaf>imDefault</validation:choiceWithDefaultLeaf>"
                + "<validation:key>whenNdef2</validation:key>"
                + "<validation:multicast-address3>225.0.0.1</validation:multicast-address3>"
                + "</validation:testList>"
                + "</validation:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);  
    }

    @Test
    public void testChoiceWithWhenAndDefaultCase2() throws Exception {
        /**
         * By creating a entry in a list, with non default case leaf in the request - > default leaf shouldn't come
         */

        getModelNode();
        initialiseInterceptor();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <rootNodeLeaf>test</rootNodeLeaf>" +
                " <testList>" +
                "   <key>whenNdef</key>" +
                "   <nonDefaultCaseLeaf1>nowDefaultShouldIgnore</nonDefaultCaseLeaf1>" +
                " </testList>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true); // simple default case of choice with satisfying when

        String expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\"> "
                + "<data>"
                + "<validation:choice-container xmlns:validation=\"urn:org:bbf:pma:choice-case-test\">"
                + "<validation:rootNodeLeaf>test</validation:rootNodeLeaf>"
                + "<validation:testList>"
                + "<validation:choiceWithDefaultContainer>"
                + "<validation:case1LeafUnderContainer>imDef-leafWithDefValue</validation:case1LeafUnderContainer>"
                + "</validation:choiceWithDefaultContainer>"
                + "<validation:choiceWithDefaultLeaf>imDefault</validation:choiceWithDefaultLeaf>"
                + "<validation:key>whenNdef</validation:key>"
                + "<validation:nonDefaultCaseLeaf1>nowDefaultShouldIgnore</validation:nonDefaultCaseLeaf1>"
                + "<validation:nonDefaultCaseLeaf2>yahooo</validation:nonDefaultCaseLeaf2>"
                + "</validation:testList>"
                + "</validation:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);  
    }

    @Test
    public void testChoiceWithWhenAndChoosingNonDefaultCase() throws Exception {
        /**
         * By creating a entry in a list, And also give the value of a leaf which is present in non default case
         * It should not instantiate the nodes in default case of choice.  And should instantiate the other leafs with default values in the chosen case.  
         */

        getModelNode();
        initialiseInterceptor();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <rootNodeLeaf>test</rootNodeLeaf>" +
                " <testList>" +
                "   <key>whenNdef</key>" +
                "   <nonDefaultCaseLeaf1>check</nonDefaultCaseLeaf1>" + // This is leaf in non default case of choice
                " </testList>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true); // simple default case of choice with satisfying when

        String expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<validation:choice-container xmlns:validation=\"urn:org:bbf:pma:choice-case-test\">"
                + "<validation:rootNodeLeaf>test</validation:rootNodeLeaf>"
                + "<validation:testList>"
                + "<validation:choiceWithDefaultContainer>" // Default container 
                + "<validation:case1LeafUnderContainer>imDef-leafWithDefValue</validation:case1LeafUnderContainer>"
                + "</validation:choiceWithDefaultContainer>"
                + "<validation:choiceWithDefaultLeaf>imDefault</validation:choiceWithDefaultLeaf>"
                + "<validation:key>whenNdef</validation:key>"
                + "<validation:nonDefaultCaseLeaf1>check</validation:nonDefaultCaseLeaf1>" // given leaf in requuest which is not part of choice's default case
                + "<validation:nonDefaultCaseLeaf2>yahooo</validation:nonDefaultCaseLeaf2>"
                + "</validation:testList>"
                + "</validation:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);
    }

    @Test
    public void testChoiceWithWhenAndDefaultCaseNegativeCase() throws Exception {
        getModelNode();
        initialiseInterceptor();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <rootNodeLeaf>test</rootNodeLeaf>" +
                " <testList>" +
                "   <key>dummy</key>" +
                "   <whenWithDefaultLeaf1>check</whenWithDefaultLeaf1>" + // When condition not satisfied but still I am giving leaf under choice.. 
                " </testList>" +
                "</choice-container>" ;
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false); // when not satisfied
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:testList[choice-case-test:key='dummy']/choice-case-test:whenWithDefaultLeaf1", "Violate when constraints: key = 'whenNdef'", NetconfRpcErrorTag.UNKNOWN_ELEMENT);
    }

    @Test
    public void testMultipleUsesInChoiceCases() throws Exception {
        getModelNode();
        initialiseInterceptor();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <rootNodeLeaf>test</rootNodeLeaf>" +
                " <multiUses>" +
                "   <key>1</key>" +
                " </multiUses>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true); // should pickup the defaults from uses
        String expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<validation:choice-container xmlns:validation=\"urn:org:bbf:pma:choice-case-test\">"
                + "<validation:multiUses>"
                + "<validation:directLeaf1>def-directLeaf1</validation:directLeaf1>"
                + "<validation:group1Leaf>def-group1Leaf</validation:group1Leaf>"
                + "<validation:groupContainer1>"
                + "<validation:groupContainerLeaf1>def-groupContainer1</validation:groupContainerLeaf1>"
                + "</validation:groupContainer1>"
                + "<validation:key>1</validation:key>"
                + "</validation:multiUses>"
                + "<validation:rootNodeLeaf>test</validation:rootNodeLeaf>"
                + "</validation:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);// Verify the default node value of default case
    }

    @Test
    public void testMandatoryChoiceInNestedChoicesPositiveCase3() throws Exception {
        getModelNode();
        initialiseInterceptor();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <nestedChoicesForMandatoryCheck1>" +
                "   <key>10</key>" +
                "   <mandatoryChoiceLeaf1>manPresent</mandatoryChoiceLeaf1>" + // mandatory leaf present. so no noise
                " </nestedChoicesForMandatoryCheck1>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true); 

        String expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:nestedChoicesForMandatoryCheck1>"
                + "<choice-case-test:key>10</choice-case-test:key>"
                + "<choice-case-test:mandatoryChoiceLeaf1>manPresent</choice-case-test:mandatoryChoiceLeaf1>"
                + "</choice-case-test:nestedChoicesForMandatoryCheck1>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);
    }

    @Test
    public void testMandatoryChoiceInNestedChoicesNegativeCase1() throws Exception {
        getModelNode();
        initialiseInterceptor();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <nestedChoicesForMandatoryCheck1>" +
                "   <key>1</key>" + // Missing mandatory node. 
                " </nestedChoicesForMandatoryCheck1>" +
                "</choice-container>" ;
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false); 
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:nestedChoicesForMandatoryCheck1[choice-case-test:key='1']/choice-case-test:l1", "Missing mandatory node - l1", NetconfRpcErrorTag.DATA_MISSING);
    }

    @Test
    public void testMandatoryChoiceInNestedChoiceCases_OuterChoiceCaseLeaf() throws Exception {
        getModelNode();
        initialiseInterceptor();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\"  xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                " <nestedChoicesForMandatoryCheck xc:operation=\"create\">" +
                "   <key>10</key>" +
                "   <mandatoryChoiceLeaf2>mandatoryPresent</mandatoryChoiceLeaf2>" +
                " </nestedChoicesForMandatoryCheck>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true); 

        String expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:nestedChoicesForMandatoryCheck>"
                + "<choice-case-test:key>10</choice-case-test:key>"
                + "<choice-case-test:mandatoryChoiceLeaf2>mandatoryPresent</choice-case-test:mandatoryChoiceLeaf2>"
                + "</choice-case-test:nestedChoicesForMandatoryCheck>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);
    }

    @Test
    public void testMandatoryChoiceInNestedChoiceCases_MissingMandatoryChoiceNode() throws Exception {
        getModelNode();
        initialiseInterceptor();
        //verify missing inner mandatory choice case
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                " <nestedChoicesForMandatoryCheck xc:operation=\"create\">" +
                "   <key>10</key>" +
                "   <mandatoryChoiceLeaf1>mandatoryPresent</mandatoryChoiceLeaf1>" +  // missing mandatory inner choice case node
                " </nestedChoicesForMandatoryCheck>" +
                "</choice-container>" ;
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false); 
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:nestedChoicesForMandatoryCheck[choice-case-test:key='10']/choice-case-test:nestedChoice", "Mandatory choice 'nestedChoice' is missing", NetconfRpcErrorTag.DATA_MISSING);
    }

    @Test
    public void testMandatoryChoiceInNestedChoices_MorethanOneCaseValues() throws Exception {
        getModelNode();
        initialiseInterceptor();
        // verify more than one case values
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                " <nestedChoicesForMandatoryCheck>" +
                "   <key>10</key>" +
                "   <mandatoryChoiceLeaf1>mandatoryPresent</mandatoryChoiceLeaf1>" +
                "   <mandatoryChoiceLeaf2>mandatoryPresent</mandatoryChoiceLeaf2>" +
                " </nestedChoicesForMandatoryCheck>" +
                "</choice-container>" ;
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false); 
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:nestedChoicesForMandatoryCheck[choice-case-test:key='10']/choice-case-test:l1", "Invalid element in choice node ", NetconfRpcErrorTag.BAD_ELEMENT);
    }

    @Test
    public void testMandatoryChoiceInNestedChoices_InnerChoiceCaseLeaf() throws Exception {
        getModelNode();
        initialiseInterceptor();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                " <nestedChoicesForMandatoryCheck xc:operation=\"create\">" +
                "   <key>10</key>" +
                "   <mandatoryChoiceLeaf3>manPresent</mandatoryChoiceLeaf3>" +
                " </nestedChoicesForMandatoryCheck>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true); 

        String expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:nestedChoicesForMandatoryCheck>"
                + "<choice-case-test:key>10</choice-case-test:key>"
                + "<choice-case-test:mandatoryChoiceLeaf3>manPresent</choice-case-test:mandatoryChoiceLeaf3>"
                + "</choice-case-test:nestedChoicesForMandatoryCheck>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);
    }

    @Test
    public void testMandatoryChoiceNodeInNestedChoices() throws Exception {
        getModelNode();
        initialiseInterceptor();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                " <nestedChoicesForMandatoryCheck xc:operation=\"create\">" +
                "   <key>10</key>" +
                "   <mandatoryChoiceLeaf1>test</mandatoryChoiceLeaf1>" +
                "   <mandatoryChoiceLeaf4>test1</mandatoryChoiceLeaf4>" +
                " </nestedChoicesForMandatoryCheck>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true); 

        String expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:nestedChoicesForMandatoryCheck>"
                + "<choice-case-test:key>10</choice-case-test:key>"
                + "<choice-case-test:mandatoryChoiceLeaf1>test</choice-case-test:mandatoryChoiceLeaf1>"
                + "<choice-case-test:mandatoryChoiceLeaf4>test1</choice-case-test:mandatoryChoiceLeaf4>"
                + "</choice-case-test:nestedChoicesForMandatoryCheck>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);
    }

    @Test
    public void testDefaultsChoiceInNestedChoicesPositiveCase1() throws Exception {
        getModelNode();
        initialiseInterceptor();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <nestedChoices2>" +
                "   <leaf1>test</leaf1>" +
                " </nestedChoices2>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true); 

        String expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:nestedChoices2>"
                + "<choice-case-test:leaf1>test</choice-case-test:leaf1>"
                + "<choice-case-test:leaf3>leaf3Present</choice-case-test:leaf3>" // This is nested choice defaults node
                + "</choice-case-test:nestedChoices2>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);        
    }

    @Test
    public void testDefaultsChoiceInNestedChoicesPositiveCase2() throws Exception {
        getModelNode();
        initialiseInterceptor();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <nestedChoices2>" +
                "   <leaf1>test</leaf1>" +
                "   <leaf4>leaf4Present</leaf4>" + // Giving top level choice's non default case node value. So it should not keep leaf3 now
                " </nestedChoices2>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true); 

        String expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:nestedChoices2>"
                + "<choice-case-test:leaf1>test</choice-case-test:leaf1>"
                + "<choice-case-test:leaf4>leaf4Present</choice-case-test:leaf4>" // Just non default node here
                + "</choice-case-test:nestedChoices2>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);
    }

    @Test
    public void testDefaultLeafUnderNonDefaultCase() throws Exception {
        getModelNode();
        initialiseInterceptor();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <defaultTest>" +
                " <inline-frame-processing>" +
                "   <push-tags>dummy</push-tags>" +
                " </inline-frame-processing>" +
                " </defaultTest>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true); 

        String expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:defaultTest>"
                + "<choice-case-test:inline-frame-processing>"
                + "<choice-case-test:pop-tags>0</choice-case-test:pop-tags>" //default leaf gets created
                + "<choice-case-test:push-tags>dummy</choice-case-test:push-tags>"
                + "</choice-case-test:inline-frame-processing>"
                + "<choice-case-test:outerContainer/>"
                + "</choice-case-test:defaultTest>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);

        //provide value of default leaf in request
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <defaultTest>" +
                " <inline-frame-processing>" +
                "   <pop-tags>196</pop-tags>" +
                "   <push-tags>dummy</push-tags>" +
                " </inline-frame-processing>" +
                " </defaultTest>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true); 

        expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:defaultTest>"
                + "<choice-case-test:inline-frame-processing>"
                + "<choice-case-test:pop-tags>196</choice-case-test:pop-tags>" //request value retained
                + "<choice-case-test:push-tags>dummy</choice-case-test:push-tags>"
                + "</choice-case-test:inline-frame-processing>"
                + "<choice-case-test:outerContainer/>"
                + "</choice-case-test:defaultTest>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);
    }

    @Test
    public void testDefaultLeafUnderNonDefaultNestedCase() throws Exception {
        getModelNode();
        initialiseInterceptor();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <defaultTest>" +
                "   <outerContainer>" +
                "     <inner-case>" +
                "       <push-tags>dummy</push-tags>" +
                "     </inner-case>" +
                "   </outerContainer>" +
                " </defaultTest>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true); 

        String expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:defaultTest>"
                + "<choice-case-test:outerContainer>"
                + "<choice-case-test:inner-case>"
                + "<choice-case-test:pop-tags>0</choice-case-test:pop-tags>" //default leaf gets created
                + "<choice-case-test:push-tags>dummy</choice-case-test:push-tags>"
                + "</choice-case-test:inner-case>"
                + "</choice-case-test:outerContainer>"
                + "</choice-case-test:defaultTest>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);

        //provide value of default leaf in request
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <defaultTest>" +
                "   <outerContainer>" +
                "     <inner-case>" +
                "       <pop-tags>196</pop-tags>" +
                "       <push-tags>dummy</push-tags>" +
                "     </inner-case>" +
                "   </outerContainer>" +
                " </defaultTest>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true); 

        expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:defaultTest>"
                + "<choice-case-test:outerContainer>"
                + "<choice-case-test:inner-case>"
                + "<choice-case-test:pop-tags>196</choice-case-test:pop-tags>" //request value retained
                + "<choice-case-test:push-tags>dummy</choice-case-test:push-tags>"
                + "</choice-case-test:inner-case>"
                + "</choice-case-test:outerContainer>"
                + "</choice-case-test:defaultTest>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);
    }

    @Test
    public void testDefaultsChoiceInNestedChoicesPositiveCase3() throws Exception {
        getModelNode();
        initialiseInterceptor();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <nestedChoices2>" +
                "   <leaf1>test</leaf1>" +
                "   <leaf2>leaf2Present</leaf2>" + // Giving next level choice's non default case node value. So it should not keep leaf3 now
                " </nestedChoices2>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true); 

        String expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:nestedChoices2>"
                + "<choice-case-test:leaf1>test</choice-case-test:leaf1>"
                + "<choice-case-test:leaf2>leaf2Present</choice-case-test:leaf2>" // Just non default node here
                + "</choice-case-test:nestedChoices2>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);
    }

    @Test
    public void testDefaultsChoiceInNestedChoicesWithWhenPositiveCase1() throws Exception {
        getModelNode();
        initialiseInterceptor();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <nestedChoices3>" +
                "   <topLeaf1>test</topLeaf1>" +
                "   <topLeaf2>l2Present</topLeaf2>" + // Plain case
                " </nestedChoices3>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true); 

        String expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:nestedChoices3>"
                + "<choice-case-test:topLeaf1>test</choice-case-test:topLeaf1>"
                + "<choice-case-test:topLeaf2>l2Present</choice-case-test:topLeaf2>"
                + "</choice-case-test:nestedChoices3>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);
    }

    @Test
    public void testDefaultsChoiceInNestedChoicesWithWhenPositiveCase2() throws Exception {
        getModelNode();
        initialiseInterceptor();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <nestedChoices3>" +
                "   <topLeaf1>l1Present</topLeaf1>" +
                "   <topLeaf2>test</topLeaf2>" + // Plain case
                " </nestedChoices3>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true); 

        String expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:nestedChoices3>"
                + "<choice-case-test:topLeaf1>l1Present</choice-case-test:topLeaf1>"
                + "<choice-case-test:topLeaf2>test</choice-case-test:topLeaf2>"
                + "</choice-case-test:nestedChoices3>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);
    }

    // TODO to be addressed by FNMS-31166
    //    @Test 
    public void testDefaultsChoiceInNestedChoicesWithWhenPositiveCase3() throws Exception {
        getModelNode();
        initialiseInterceptor();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <nestedChoices3>" +
                "   <topLeaf1>l1Present</topLeaf1>" +
                "   <topLeaf2>l2Present</topLeaf2>" + // Both the when satisfied
                " </nestedChoices3>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true); 

        String expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:nestedChoices3>"
                + "<choice-case-test:topLeaf1>l1Present</choice-case-test:topLeaf1>"
                + "<choice-case-test:topLeaf2>l2Present</choice-case-test:topLeaf2>"
                + "<choice-case-test:leaf3>leaf3Present</choice-case-test:leaf3>"
                + "</choice-case-test:nestedChoices3>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);
    }
    // TODO to be addressed by FNMS-31166
    //    @Test 
    public void testDefaultsChoiceInNestedChoicesWithWhenPositiveCase4() throws Exception {
        getModelNode();
        initialiseInterceptor();
        // In this case, one of the when condition (nested choice when condition), checks the default value of top level choice case ->  as it is default value, it will not present in the request
        // so that when should pass and it should include default value
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <nestedChoices4>" +
                "   <topLeaf1>l1Present</topLeaf1>" +
                " </nestedChoices4>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true); 

        String expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:nestedChoices3>"
                + "<choice-case-test:topLeaf1>l1Present</choice-case-test:topLeaf1>"
                + "<choice-case-test:c1Leaf1>c1Leaf1Present</choice-case-test:c1Leaf1>"
                + "<choice-case-test:leaf2>leaf2Present</choice-case-test:leaf2>"
                + "</choice-case-test:nestedChoices3>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);
    }        

    @Test
    public void testDuplicateLeafListInNestedChoice() throws Exception {
        getModelNode();
        initialiseInterceptor();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <leaflistandlistinnestedchoicelevel>" +
                "       <countryname>india</countryname>" +
                "       <countryname>india</countryname>" + 
                "       <statename>tamilnadu</statename>" +
                "       <currencyname>rupee</currencyname>" + 
                " </leaflistandlistinnestedchoicelevel>" +
                "</choice-container>" ;
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false); 
        checkErrors(response, "/choice-container/leaflistandlistinnestedchoicelevel/countryname", "Duplicate elements in node (urn:org:bbf:pma:choice-case-test?revision=2019-02-25)countryname", NetconfRpcErrorTag.OPERATION_FAILED);        
    }

    @Test
    public void testLeafListInNestedChoice() throws Exception {
        getModelNode();
        initialiseInterceptor();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <leaflistandlistinnestedchoicelevel>" +
                "       <countryname>india</countryname>" +
                "       <countryname>srilanka</countryname>" +
                "       <statename>tamilnadu</statename>" +
                "       <currencyname>rupee</currencyname>" +                 
                " </leaflistandlistinnestedchoicelevel>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true); 

        String expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:leaflistandlistinnestedchoicelevel>"
                + "<choice-case-test:countryname>india</choice-case-test:countryname>"
                + "<choice-case-test:countryname>srilanka</choice-case-test:countryname>"
                + "<choice-case-test:statename>tamilnadu</choice-case-test:statename>"
                + "<choice-case-test:currencyname>rupee</choice-case-test:currencyname>"                
                + "</choice-case-test:leaflistandlistinnestedchoicelevel>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);
    }       

    @Test
    public void testListInNestedChoice() throws Exception {
        getModelNode();
        initialiseInterceptor();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <leaflistandlistinnestedchoicelevel>" +
                "     <student>" +
                "       <studentname>test</studentname>" +
                "       <schoolname>alpha</schoolname>" + 
                "     </student>" +
                "     <subject>" +
                "       <subjectname>maths</subjectname>" +
                "     </subject>" +
                " </leaflistandlistinnestedchoicelevel>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true); 

        String expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:leaflistandlistinnestedchoicelevel>"
                + "<choice-case-test:student>"
                + "<choice-case-test:studentname>test</choice-case-test:studentname>"
                + "<choice-case-test:schoolname>alpha</choice-case-test:schoolname>"                
                + "</choice-case-test:student>"
                + "<choice-case-test:subject>"
                + "<choice-case-test:subjectname>maths</choice-case-test:subjectname>"               
                + "</choice-case-test:subject>"
                + "</choice-case-test:leaflistandlistinnestedchoicelevel>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);
    }

    @Test
    public void testLeafListInsideListInNestedChoice() throws Exception {
        getModelNode();
        initialiseInterceptor();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <leaflistandlistinnestedchoicelevel>" +
                "     <organization>" +
                "       <organizationname>test</organizationname>" +
                "       <areaname>southeast</areaname>" +
                "     </organization>" +
                "       <zonename>south</zonename>" +
                "       <cityname>chennai</cityname>" + 
                " </leaflistandlistinnestedchoicelevel>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true); 

        String expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:leaflistandlistinnestedchoicelevel>"
                + "<choice-case-test:organization>"
                + "<choice-case-test:organizationname>test</choice-case-test:organizationname>"
                + "<choice-case-test:areaname>southeast</choice-case-test:areaname>"
                + "</choice-case-test:organization>"
                + "<choice-case-test:zonename>south</choice-case-test:zonename>"
                + "<choice-case-test:cityname>chennai</choice-case-test:cityname>"
                + "</choice-case-test:leaflistandlistinnestedchoicelevel>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);
    }

    @Test
    public void testListInsidecontainerInNestedChoice() throws Exception {
        getModelNode();
        initialiseInterceptor();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <leaflistandlistinnestedchoicelevel>" +
                " <listtestcontainer>" +
                "     <testlist>" +
                "       <testleafname>test</testleafname>" +
                "     </testlist>" +
                " </listtestcontainer>" +
                " </leaflistandlistinnestedchoicelevel>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true); 

        String expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:leaflistandlistinnestedchoicelevel>"
                + "<choice-case-test:listtestcontainer>"
                + "<choice-case-test:testlist>"
                + "<choice-case-test:testleafname>test</choice-case-test:testleafname>"
                + "</choice-case-test:testlist>"
                + "</choice-case-test:listtestcontainer>"                 
                + "</choice-case-test:leaflistandlistinnestedchoicelevel>"                
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);
    }
    @Test
    public void testLeafListInsidecontainerInNestedChoice() throws Exception {
        getModelNode();
        initialiseInterceptor();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <leaflistandlistinnestedchoicelevel>" +
                " <leaflisttestcontainer>" +
                "       <testleaflistname>test</testleaflistname>" +
                " </leaflisttestcontainer>" +
                " </leaflistandlistinnestedchoicelevel>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true); 

        String expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:leaflistandlistinnestedchoicelevel>"
                + "<choice-case-test:leaflisttestcontainer>"
                + "<choice-case-test:testleaflistname>test</choice-case-test:testleaflistname>"
                + "</choice-case-test:leaflisttestcontainer>"                 
                + "</choice-case-test:leaflistandlistinnestedchoicelevel>"                
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);
    }

    @Test
    public void testMissingMandatoryNodesInsideChoiceCase() throws Exception {
        getModelNode();
        initialiseInterceptor();

        // create default case which has default leaf
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <mandatory-container>" +
                " </mandatory-container>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true); 

        // verify default case
        String expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:mandatory-container>"
                + "<choice-case-test:no-profiles-attached-leaf>leaf</choice-case-test:no-profiles-attached-leaf>"
                + "</choice-case-test:mandatory-container>"                 
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);

        // replace case3 with missing mandatory node (case1 --> case3)
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <mandatory-container>" +
                " <indirect-attachment-mode-leaf>madatory1</indirect-attachment-mode-leaf>"+
                " </mandatory-container>" +
                "</choice-container>" ;
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false); 
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:mandatory-container/choice-case-test:indirect-attachment-mode-leaf1", "Missing mandatory node - indirect-attachment-mode-leaf1", NetconfRpcErrorTag.DATA_MISSING);        
    }

    @Test
    public void testMissingMandatoryNodesInsideChoiceCase1() throws Exception {
        getModelNode();
        initialiseInterceptor();

        // create case3 with mandatory nodes
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <mandatory-container>" +
                " <indirect-attachment-mode-leaf>madatory1</indirect-attachment-mode-leaf>"+
                " <indirect-attachment-mode-leaf1>madatory2</indirect-attachment-mode-leaf1>"+
                " </mandatory-container>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true); 

        // verify case3 nodes
        String expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:mandatory-container>"
                + "<choice-case-test:indirect-attachment-mode-leaf>madatory1</choice-case-test:indirect-attachment-mode-leaf>"
                + "<choice-case-test:indirect-attachment-mode-leaf1>madatory2</choice-case-test:indirect-attachment-mode-leaf1>"
                + "</choice-case-test:mandatory-container>"                 
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";
        verifyGet(expResponse);

        // replace case2 mandatory modes (case3 --> case2)
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <mandatory-container>" +
                " <direct-attachment-mode-leaf>madatory1</direct-attachment-mode-leaf>"+
                " <direct-attachment-mode-leaf1>madatory2</direct-attachment-mode-leaf1>"+
                " <channel>"+
                " <direct-attachment-mode-leaf2>madatory3</direct-attachment-mode-leaf2>"+
                " </channel>"+
                " </mandatory-container>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true); 

        // verify case2 nodes
        expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:mandatory-container>"
                + "<choice-case-test:direct-attachment-mode-leaf>madatory1</choice-case-test:direct-attachment-mode-leaf>"
                + "<choice-case-test:direct-attachment-mode-leaf1>madatory2</choice-case-test:direct-attachment-mode-leaf1>"
                + "<choice-case-test:channel>"
                + "<choice-case-test:direct-attachment-mode-leaf2>madatory3</choice-case-test:direct-attachment-mode-leaf2>"
                + "</choice-case-test:channel>"
                + "</choice-case-test:mandatory-container>"                 
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";
        verifyGet(expResponse);

        //replace case1 (case2 -> case1)
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <mandatory-container>" +
                "<no-profiles-attached-leaf>leaf1</no-profiles-attached-leaf>"+
                " </mandatory-container>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true); 

        // verify case1 nodes
        expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:mandatory-container>"
                + "<choice-case-test:no-profiles-attached-leaf>leaf1</choice-case-test:no-profiles-attached-leaf>"
                + "</choice-case-test:mandatory-container>"                 
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";
        verifyGet(expResponse);

        // replace case2 with missing mandatory nodes (case1 -> case2)
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <mandatory-container>" +
                " <channel>"+
                " <direct-attachment-mode-leaf2>madatory3</direct-attachment-mode-leaf2>"+
                " </channel>"+
                " </mandatory-container>" +
                "</choice-container>" ;
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
        // verify error response
        List<NetconfRpcError> errors = response.getErrors();
        assertEquals(2, errors.size());
        checkErrors(errors.get(0), "/choice-case-test:choice-container/choice-case-test:mandatory-container/choice-case-test:direct-attachment-mode-leaf", "Missing mandatory node - direct-attachment-mode-leaf", NetconfRpcErrorTag.DATA_MISSING);        
        checkErrors(errors.get(1), "/choice-case-test:choice-container/choice-case-test:mandatory-container/choice-case-test:direct-attachment-mode-leaf1", "Missing mandatory node - direct-attachment-mode-leaf1", NetconfRpcErrorTag.DATA_MISSING);
    }

    @Test
    public void testMissingMandatoryLeafWithContainer() throws Exception {
        getModelNode();
        initialiseInterceptor();

        // create default case which has default leaf
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <mandatory-container>" +
                " </mandatory-container>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true); 

        // verify default case
        String expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:mandatory-container>"
                + "<choice-case-test:no-profiles-attached-leaf>leaf</choice-case-test:no-profiles-attached-leaf>"
                + "</choice-case-test:mandatory-container>"                 
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);

        // replace case2 with missing mandatory node (case1 --> case2)
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <mandatory-container>" +
                " <direct-attachment-mode-leaf>madatory1</direct-attachment-mode-leaf>"+
                " <direct-attachment-mode-leaf1>madatory2</direct-attachment-mode-leaf1>"+
                " </mandatory-container>" +
                "</choice-container>" ;
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false); 
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:mandatory-container/choice-case-test:channel/choice-case-test:direct-attachment-mode-leaf2", "Missing mandatory node - direct-attachment-mode-leaf2", NetconfRpcErrorTag.DATA_MISSING);        
    }

    @Test
    public void testMissingMandatoryListNode_MinElementConstraint() throws Exception {
        getModelNode();
        initialiseInterceptor();

        // create default case which has default leaf
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <mandatory-container>" +
                " </mandatory-container>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true); 

        // verify default case
        String expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:mandatory-container>"
                + "<choice-case-test:no-profiles-attached-leaf>leaf</choice-case-test:no-profiles-attached-leaf>"
                + "</choice-case-test:mandatory-container>"                 
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);

        // replace case4 with missing mandatory list node (case1 --> case4)
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <mandatory-container>" +
                " <profile-attachment-mode-leaf>leaf1</profile-attachment-mode-leaf>"+
                " </mandatory-container>" +
                "</choice-container>" ;
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false); 
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:mandatory-container/choice-case-test:profiles/choice-case-test:profile", "Minimum elements required for profile is 1.", NetconfRpcErrorTag.OPERATION_FAILED);        
    }

    @Test
    public void testMissingMandatoryListAndLeafListNode() throws Exception {
        getModelNode();
        initialiseInterceptor();

        // create case4 nodes
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <mandatory-container>" +
                " <profile-attachment-mode-leaf>leaf1</profile-attachment-mode-leaf>"+
                " <profiles>"+
                " <profile>"+
                " <name>profile1</name>"+
                " </profile>"+
                " </profiles>"+
                " </mandatory-container>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true); 

        // verify case4 nodes
        String expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:mandatory-container>"
                + "<choice-case-test:profile-attachment-mode-leaf>leaf1</choice-case-test:profile-attachment-mode-leaf>"
                + "<choice-case-test:profiles>"
                + "<choice-case-test:profile>"
                + "<choice-case-test:name>profile1</choice-case-test:name>"
                + "</choice-case-test:profile>"
                + "</choice-case-test:profiles>"
                + "</choice-case-test:mandatory-container>"                 
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);

        // replace case5 with missing mandatory List node (case4 --> case5)
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <mandatory-container>" +
                " <case5-leaf>leaf1</case5-leaf>"+
                " </mandatory-container>" +
                "</choice-container>" ;
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false); 
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:mandatory-container/choice-case-test:case5-mandatory-list", "Missing mandatory node - case5-mandatory-list", NetconfRpcErrorTag.DATA_MISSING);        
    }

    @Test
    public void testMissingMandatoryListNode() throws Exception {
        getModelNode();
        initialiseInterceptor();

        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <mandatory-container>" +
                " <case5-leaf>leaf1</case5-leaf>"+
                " <case5-mandatory-inner-container>"+
                " <city>chennai</city>"+
                " </case5-mandatory-inner-container>"+
                " </mandatory-container>" +
                "</choice-container>" ;
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false); 
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:mandatory-container/choice-case-test:case5-mandatory-list", "Missing mandatory node - case5-mandatory-list", NetconfRpcErrorTag.DATA_MISSING);
    }

    @Test
    public void testMissingMandatoryLeafListWithContainer() throws Exception {
        getModelNode();
        initialiseInterceptor();

        // create case4 nodes
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <mandatory-container>" +
                " <profile-attachment-mode-leaf>leaf1</profile-attachment-mode-leaf>"+
                " <profiles>"+
                " <profile>"+
                " <name>profile1</name>"+
                " </profile>"+
                " </profiles>"+
                " </mandatory-container>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true); 

        // verify case4 nodes
        String expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:mandatory-container>"
                + "<choice-case-test:profile-attachment-mode-leaf>leaf1</choice-case-test:profile-attachment-mode-leaf>"
                + "<choice-case-test:profiles>"
                + "<choice-case-test:profile>"
                + "<choice-case-test:name>profile1</choice-case-test:name>"
                + "</choice-case-test:profile>"
                + "</choice-case-test:profiles>"
                + "</choice-case-test:mandatory-container>"                 
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);

        // replace case5 with missing mandatory node (case4 --> case5)
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <mandatory-container>" +
                " <case5-leaf>leaf1</case5-leaf>"+
                " <case5-mandatory-list>"+
                " <name>bbf</name>"+
                " </case5-mandatory-list>"+
                " </mandatory-container>" +
                "</choice-container>" ;
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false); 
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:mandatory-container/choice-case-test:case5-mandatory-inner-container/choice-case-test:city", "Minimum elements required for city is 1.", NetconfRpcErrorTag.OPERATION_FAILED);        
    }

    @Test
    public void testRemoveMandatoryListNode() throws Exception {
        getModelNode();
        initialiseInterceptor();

        // create case4 nodes (two list entries)
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <mandatory-container>" +
                " <profile-attachment-mode-leaf>leaf1</profile-attachment-mode-leaf>"+
                " <profiles>"+
                " <profile>"+
                " <name>profile1</name>"+
                " </profile>"+
                " <profile>"+
                " <name>profile2</name>"+
                " </profile>"+
                " </profiles>"+
                " </mandatory-container>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true); 

        // verify case4 nodes
        String expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:mandatory-container>"
                + "<choice-case-test:profile-attachment-mode-leaf>leaf1</choice-case-test:profile-attachment-mode-leaf>"
                + "<choice-case-test:profiles>"
                + "<choice-case-test:profile>"
                + "<choice-case-test:name>profile1</choice-case-test:name>"
                + "</choice-case-test:profile>"
                + "<choice-case-test:profile>"
                + "<choice-case-test:name>profile2</choice-case-test:name>"
                + "</choice-case-test:profile>"
                + "</choice-case-test:profiles>"
                + "</choice-case-test:mandatory-container>"                 
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);

        //remove one list entry from case4
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <mandatory-container>" +
                " <profiles>"+
                " <profile xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\">"+
                " <name>profile1</name>"+
                " </profile>"+
                " </profiles>"+
                " </mandatory-container>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true); 

        //verify GET response
        expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:mandatory-container>"
                + "<choice-case-test:profile-attachment-mode-leaf>leaf1</choice-case-test:profile-attachment-mode-leaf>"
                + "<choice-case-test:profiles>"
                + "<choice-case-test:profile>"
                + "<choice-case-test:name>profile2</choice-case-test:name>"
                + "</choice-case-test:profile>"
                + "</choice-case-test:profiles>"
                + "</choice-case-test:mandatory-container>"                 
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);

        // remove profiles container which has mandatory list (min-element constraint as '1')
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <mandatory-container>" +
                " <profiles xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\">"+
                " </profiles>"+
                " </mandatory-container>" +
                "</choice-container>" ;
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false); 
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:mandatory-container/choice-case-test:profiles/choice-case-test:profile", "Minimum elements required for profile is 1.", NetconfRpcErrorTag.OPERATION_FAILED);        
    }

    @Test
    public void testMissingMandatoryNestedChoice() throws Exception {
        getModelNode();
        initialiseInterceptor();

        // create case4 nodes
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <mandatory-container>" +
                " <profile-attachment-mode-leaf>leaf1</profile-attachment-mode-leaf>"+
                " <profiles>"+
                " <profile>"+
                " <name>profile1</name>"+
                " </profile>"+
                " <profile>"+
                " <name>profile2</name>"+
                " </profile>"+
                " </profiles>"+
                " </mandatory-container>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true); 

        // verify case4 nodes
        String expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:mandatory-container>"
                + "<choice-case-test:profile-attachment-mode-leaf>leaf1</choice-case-test:profile-attachment-mode-leaf>"
                + "<choice-case-test:profiles>"
                + "<choice-case-test:profile>"
                + "<choice-case-test:name>profile1</choice-case-test:name>"
                + "</choice-case-test:profile>"
                + "<choice-case-test:profile>"
                + "<choice-case-test:name>profile2</choice-case-test:name>"
                + "</choice-case-test:profile>"
                + "</choice-case-test:profiles>"
                + "</choice-case-test:mandatory-container>"                 
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);

        // replace case5 nodes with missing nested mandatory choice
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <mandatory-container>" +
                " <case6-container>"+
                " <case6-container-leaf>leaf1</case6-container-leaf>"+
                " </case6-container>"+
                " </mandatory-container>" +
                "</choice-container>" ;

        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false); 
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:mandatory-container/choice-case-test:case6-container/choice-case-test:nested-mandatory-choice", "Missing mandatory node - nested-mandatory-choice", NetconfRpcErrorTag.DATA_MISSING);        
    }

    @Test
    public void testMissingMandatoryNestedChoiceCase() throws Exception {
        getModelNode();
        initialiseInterceptor();
        //creating case6 nodes with missing mandatory nested choice
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <mandatory-container>" +
                " <case6-container>"+
                " <case6-container-leaf>leaf1</case6-container-leaf>"+
                " </case6-container>"+
                " </mandatory-container>" +
                "</choice-container>" ;

        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false); 
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:mandatory-container/choice-case-test:case6-container/choice-case-test:nested-mandatory-choice",  "Missing mandatory node - nested-mandatory-choice", NetconfRpcErrorTag.DATA_MISSING);
    }

    @Test
    public void testMissingMandatoryList_MinElementConstraintViolation() throws Exception {
        getModelNode();
        initialiseInterceptor();
        //creating case6 nodes with missing mandatory list (min-element is2 for case6-nested-container-list node)
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <mandatory-container>" +
                " <case6-container>"+
                " <case6-container-leaf>leaf1</case6-container-leaf>"+
                " <case6-nested-container>"+
                " <case6-nested-container-list>"+
                " <name>chennai</name>"+
                " </case6-nested-container-list>"+
                " </case6-nested-container>"+
                " </case6-container>"+
                " </mandatory-container>" +
                "</choice-container>" ;

        //verify error messages
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false); 
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:mandatory-container/choice-case-test:case6-container/choice-case-test:case6-nested-container/choice-case-test:case6-nested-container-list", "Minimum elements required for case6-nested-container-list is 2.", NetconfRpcErrorTag.OPERATION_FAILED);
    }

    @Test
    public void testReplaceNodesBetweenNestedChoiceCases() throws Exception {
        getModelNode();
        initialiseInterceptor();

        //create case6 nodes with all mandatory nodes
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <mandatory-container>" +
                " <case6-container>"+
                " <case6-container-leaf>leaf1</case6-container-leaf>"+
                " <nested-choice-case1-leaf>nested-choice-case</nested-choice-case1-leaf>"+
                " <case6-nested-container>"+
                " <case6-nested-container-list>"+
                " <name>name1</name>"+
                " </case6-nested-container-list>"+
                " <case6-nested-container-list>"+
                " <name>name2</name>"+
                " </case6-nested-container-list>"+
                " <case6-nested-container-list>"+
                " <name>name3</name>"+
                " </case6-nested-container-list>"+
                " </case6-nested-container>"+
                " </case6-container>"+
                " </mandatory-container>" +
                "</choice-container>" ;

        editConfig(m_server, m_clientInfo, requestXml, true);

        //verify GET response for case6 nodes
        String expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:mandatory-container>"
                + "<choice-case-test:case6-default-leaf>test1</choice-case-test:case6-default-leaf>"
                + "<choice-case-test:case6-container>"
                + "<choice-case-test:nested-choice-case1-leaf>nested-choice-case</choice-case-test:nested-choice-case1-leaf>"
                + "<choice-case-test:case6-nested-container>"
                + "<choice-case-test:case6-nested-container-list>"
                + "<choice-case-test:name>name1</choice-case-test:name>"
                + "</choice-case-test:case6-nested-container-list>"
                + "<choice-case-test:case6-nested-container-list>"
                + "<choice-case-test:name>name2</choice-case-test:name>"
                + "</choice-case-test:case6-nested-container-list>"
                + "<choice-case-test:case6-nested-container-list>"
                + "<choice-case-test:name>name3</choice-case-test:name>"
                + "</choice-case-test:case6-nested-container-list>"
                + "</choice-case-test:case6-nested-container>"
                + "<choice-case-test:case6-container-leaf>leaf1</choice-case-test:case6-container-leaf>"
                + "</choice-case-test:case6-container>"
                + "</choice-case-test:mandatory-container>"                 
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);

        // replace the nodes between choice-cases (nested choice case1 --> case2)
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <mandatory-container>" +
                " <case6-container>"+
                " <nested-choice-case2-leaf>leaf1</nested-choice-case2-leaf>"+
                " <nested-choice-case2-container>"+
                " <nested-choice-case2-container-leaf>leaf12</nested-choice-case2-container-leaf>"+
                " </nested-choice-case2-container>"+
                " </case6-container>"+
                " </mandatory-container>" +
                "</choice-container>" ;

        editConfig(m_server, m_clientInfo, requestXml, true);

        expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:mandatory-container>"
                + "<choice-case-test:case6-default-leaf>test1</choice-case-test:case6-default-leaf>"
                + "<choice-case-test:case6-container>"
                + "<choice-case-test:case6-container-leaf>leaf1</choice-case-test:case6-container-leaf>"
                +"<choice-case-test:nested-choice-case2-container>"
                +"<choice-case-test:nested-choice-case2-container-leaf>leaf12</choice-case-test:nested-choice-case2-container-leaf>"
                +"</choice-case-test:nested-choice-case2-container>"
                +"<choice-case-test:nested-choice-case2-leaf>leaf1</choice-case-test:nested-choice-case2-leaf>"
                + "<choice-case-test:case6-nested-container>"
                + "<choice-case-test:case6-nested-container-list>"
                + "<choice-case-test:name>name1</choice-case-test:name>"
                + "</choice-case-test:case6-nested-container-list>"
                + "<choice-case-test:case6-nested-container-list>"
                + "<choice-case-test:name>name2</choice-case-test:name>"
                + "</choice-case-test:case6-nested-container-list>"
                + "<choice-case-test:case6-nested-container-list>"
                + "<choice-case-test:name>name3</choice-case-test:name>"
                + "</choice-case-test:case6-nested-container-list>"
                + "</choice-case-test:case6-nested-container>"
                + "</choice-case-test:case6-container>"
                + "</choice-case-test:mandatory-container>"                 
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);

        // case6 --> case1 (deleted case6 nodes)
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <mandatory-container>" +
                " <no-profiles-attached-leaf>defaultCase</no-profiles-attached-leaf>"+
                " </mandatory-container>" +
                "</choice-container>" ;

        editConfig(m_server, m_clientInfo, requestXml, true);

        // verify case1 nodes
        expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:mandatory-container>"
                + "<choice-case-test:no-profiles-attached-leaf>defaultCase</choice-case-test:no-profiles-attached-leaf>"
                + "</choice-case-test:mandatory-container>"                 
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);
    }

    @Test
    public void testMandatoryChoiceNegativecase() throws Exception {
        getModelNode();
        initialiseInterceptor();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>test</mandatory-choice-control-leaf>"+
                "<mandatory-choice-container/>" +
                "</choice-container>" ;
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:mandatory-choice-container/choice-case-test:choice-test", "Missing mandatory node - choice-test", NetconfRpcErrorTag.DATA_MISSING);
    }

    @Test
    public void testDefaultChoiceCaseLeafDeletion() throws Exception {
        getModelNode();
        initialiseInterceptor();
        //selecting case2
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>test</mandatory-choice-control-leaf>"+
                "<mandatory-choice-container>" +
                "<leaf2>test</leaf2>"+
                "<case1Leaf1>test</case1Leaf1>"+
                "</mandatory-choice-container>" +
                "</choice-container>" ;

        editConfig(m_server, m_clientInfo, requestXml, true);

        String expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:mandatory-choice-control-leaf>test</choice-case-test:mandatory-choice-control-leaf>"
                + "<choice-case-test:mandatory-choice-container>"
                + "<choice-case-test:case1Leaf1>test</choice-case-test:case1Leaf1>"    
                + "<choice-case-test:case1Leaf2>XYZ</choice-case-test:case1Leaf2>"  
                + "<choice-case-test:leaf2>test</choice-case-test:leaf2>"  
                + "<choice-case-test:test-container>"
                + "<choice-case-test:case1Defaultleaf1>xxx</choice-case-test:case1Defaultleaf1>"
                + "</choice-case-test:test-container>"
                + "</choice-case-test:mandatory-choice-container>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);

        //delete case2 would automatically select default case1 nodes
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <mandatory-choice-container>" +
                "  <leaf2 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>"+
                " </mandatory-choice-container>" +
                "</choice-container>" ;

        editConfig(m_server, m_clientInfo, requestXml, true);

        expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:mandatory-choice-control-leaf>test</choice-case-test:mandatory-choice-control-leaf>"
                + "<choice-case-test:mandatory-choice-container>"
                + "<choice-case-test:case1Leaf1>test</choice-case-test:case1Leaf1>"    
                + "<choice-case-test:case1Leaf2>XYZ</choice-case-test:case1Leaf2>"  
                + "<choice-case-test:leaf1>hello</choice-case-test:leaf1>"  
                + "<choice-case-test:test-container>"
                + "<choice-case-test:case1Defaultleaf1>xxx</choice-case-test:case1Defaultleaf1>"
                + "</choice-case-test:test-container>"
                + "</choice-case-test:mandatory-choice-container>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);
    }

    @Test
    public void testMandatoryChoiceDeletingDefaultLeavesUnderCase() throws Exception {
        getModelNode();
        initialiseInterceptor();
        //selecting case1
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>test</mandatory-choice-control-leaf>"+
                "<mandatory-choice-container>" +
                "<case1Leaf1>test</case1Leaf1>"+
                "</mandatory-choice-container>" +
                "</choice-container>" ;

        editConfig(m_server, m_clientInfo, requestXml, true);

        String expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:mandatory-choice-control-leaf>test</choice-case-test:mandatory-choice-control-leaf>"
                + "<choice-case-test:mandatory-choice-container>"
                + "<choice-case-test:case1Leaf1>test</choice-case-test:case1Leaf1>"    
                + "<choice-case-test:case1Leaf2>XYZ</choice-case-test:case1Leaf2>"  
                + "<choice-case-test:leaf1>hello</choice-case-test:leaf1>"
                + "<choice-case-test:test-container>"
                + "<choice-case-test:case1Defaultleaf1>xxx</choice-case-test:case1Defaultleaf1>"
                + "</choice-case-test:test-container>"
                + "</choice-case-test:mandatory-choice-container>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);

        //explicilty removing case1 nodes including default leaves, still it gets selected as there are no other case selected
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <mandatory-choice-container>" +
                "  <case1Leaf1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>"+
                "  <case1Leaf2 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>"+
                "  <test-container xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>"+
                " </mandatory-choice-container>" +
                "</choice-container>" ;

        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:mandatory-choice-container/choice-case-test:choice-test", "Missing mandatory node - choice-test", NetconfRpcErrorTag.DATA_MISSING);
    }

    @Test
    public void testMandatoryChoiceDeletingCaseLeafListNodes() throws Exception {
        getModelNode();
        initialiseInterceptor();
        //selecting case3 without specifying leaf-list node that has min-element constraint
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>test</mandatory-choice-control-leaf>"+
                "<mandatory-choice-container>" +
                "<case3Leaf1>test</case3Leaf1>"+
                "</mandatory-choice-container>" +
                "</choice-container>" ;

        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:mandatory-choice-container/choice-case-test:case3Leaf2", "Missing mandatory node - case3Leaf2", NetconfRpcErrorTag.DATA_MISSING);

        // still fails has only 1 entry exists for case3Leaf2 node
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>test</mandatory-choice-control-leaf>"+
                "<mandatory-choice-container>" +
                "<case3Leaf1>test</case3Leaf1>"+
                "<case3Leaf2>one</case3Leaf2>"+
                "</mandatory-choice-container>" +
                "</choice-container>" ;

        response = editConfig(m_server, m_clientInfo, requestXml, false);
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:mandatory-choice-container/choice-case-test:case3Leaf2", "Minimum elements required for case3Leaf2 is 2.", NetconfRpcErrorTag.OPERATION_FAILED);

        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>test</mandatory-choice-control-leaf>"+
                "<mandatory-choice-container>" +
                "<case3Leaf1>test</case3Leaf1>"+
                "<case3Leaf2>one</case3Leaf2>"+
                "<case3Leaf2>two</case3Leaf2>"+
                "</mandatory-choice-container>" +
                "</choice-container>" ;

        response = editConfig(m_server, m_clientInfo, requestXml, true);

        String expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:mandatory-choice-control-leaf>test</choice-case-test:mandatory-choice-control-leaf>"
                + "<choice-case-test:mandatory-choice-container>"
                + "<choice-case-test:case3Leaf1>test</choice-case-test:case3Leaf1>"    
                + "<choice-case-test:case3Leaf2>one</choice-case-test:case3Leaf2>"  
                + "<choice-case-test:case3Leaf2>two</choice-case-test:case3Leaf2>" 
                + "<choice-case-test:leaf1>hello</choice-case-test:leaf1>"
                + "</choice-case-test:mandatory-choice-container>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);      

        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-container>" +
                "<case3Leaf1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>"+
                "<case3Leaf2 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\">one</case3Leaf2>"+
                "</mandatory-choice-container>" +
                "</choice-container>" ;

        response = editConfig(m_server, m_clientInfo, requestXml, false);
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:mandatory-choice-container/choice-case-test:case3Leaf2", "Minimum elements required for case3Leaf2 is 2.", NetconfRpcErrorTag.OPERATION_FAILED);

        //as UT's are using in-memory DSM , it doesn't have the rollback support, so just select the case3 nodes again and delete the whole nodes at 1 shot
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-container>" +
                "<case3Leaf1>test</case3Leaf1>"+
                "<case3Leaf2>one</case3Leaf2>"+
                "<case3Leaf2>two</case3Leaf2>"+
                "</mandatory-choice-container>" +
                "</choice-container>" ;

        response = editConfig(m_server, m_clientInfo, requestXml, true);

        expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:mandatory-choice-control-leaf>test</choice-case-test:mandatory-choice-control-leaf>"
                + "<choice-case-test:mandatory-choice-container>"
                + "<choice-case-test:case3Leaf1>test</choice-case-test:case3Leaf1>"    
                + "<choice-case-test:case3Leaf2>one</choice-case-test:case3Leaf2>"  
                + "<choice-case-test:case3Leaf2>two</choice-case-test:case3Leaf2>"  
                + "<choice-case-test:leaf1>hello</choice-case-test:leaf1>"
                + "</choice-case-test:mandatory-choice-container>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse); 

        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-container>" +
                "<case3Leaf1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>"+
                "</mandatory-choice-container>" +
                "</choice-container>" ;

        response = editConfig(m_server, m_clientInfo, requestXml, true);

        expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:mandatory-choice-control-leaf>test</choice-case-test:mandatory-choice-control-leaf>"
                + "<choice-case-test:mandatory-choice-container>"
                + "<choice-case-test:case3Leaf2>one</choice-case-test:case3Leaf2>"  
                + "<choice-case-test:case3Leaf2>two</choice-case-test:case3Leaf2>"  
                + "<choice-case-test:leaf1>hello</choice-case-test:leaf1>"
                + "</choice-case-test:mandatory-choice-container>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse); 

        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-container>" +
                "<case3Leaf2 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\">one</case3Leaf2>"+
                "<case3Leaf2 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\">two</case3Leaf2>"+
                "</mandatory-choice-container>" +
                "</choice-container>" ;

        response = editConfig(m_server, m_clientInfo, requestXml, false);
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:mandatory-choice-container/choice-case-test:choice-test", "Missing mandatory node - choice-test", NetconfRpcErrorTag.DATA_MISSING);
    }

    @Test
    public void testMandatoryChoiceDeletingListNodes() throws Exception {
        getModelNode();
        initialiseInterceptor();
        //selecting case9 without specifying list node that has min-element constraint
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>test</mandatory-choice-control-leaf>"+
                "<mandatory-choice-container>" +
                "<case9List1>" +
                "<case9Leaf1>test1</case9Leaf1>"+
                "</case9List1>"            +     
                "</mandatory-choice-container>" +
                "</choice-container>" ;

        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:mandatory-choice-container/choice-case-test:case9List1", "Minimum elements required for case9List1 is 2.", NetconfRpcErrorTag.OPERATION_FAILED);

        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>test</mandatory-choice-control-leaf>"+
                "<mandatory-choice-container>" +
                "<case9List1>" +
                "<case9Leaf1>test1</case9Leaf1>"+
                "</case9List1>"    +  
                "<case9List1>" +
                "<case9Leaf1>test2</case9Leaf1>"+
                "</case9List1>"   +     
                "</mandatory-choice-container>" +
                "</choice-container>" ;

        editConfig(m_server, m_clientInfo, requestXml, true); 

        String expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:mandatory-choice-control-leaf>test</choice-case-test:mandatory-choice-control-leaf>"
                + "<choice-case-test:mandatory-choice-container>"
                + "<choice-case-test:case9List1>"
                + "<choice-case-test:case9Leaf1>test1</choice-case-test:case9Leaf1>"  
                + "</choice-case-test:case9List1>"
                + "<choice-case-test:case9List1>"
                + "<choice-case-test:case9Leaf1>test2</choice-case-test:case9Leaf1>"  
                + "</choice-case-test:case9List1>"
                + "<choice-case-test:leaf1>hello</choice-case-test:leaf1>"  
                + "</choice-case-test:mandatory-choice-container>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);

        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>test</mandatory-choice-control-leaf>"+
                "<mandatory-choice-container>" +
                "<case9List1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\">" +
                "<case9Leaf1>test1</case9Leaf1>"+
                "</case9List1>"    +  
                "<case9List1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\">" +
                "<case9Leaf1>test2</case9Leaf1>"+
                "</case9List1>"   +     
                "</mandatory-choice-container>" +
                "</choice-container>" ;

        response =  editConfig(m_server, m_clientInfo, requestXml, false); 
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:mandatory-choice-container/choice-case-test:choice-test", "Missing mandatory node - choice-test", NetconfRpcErrorTag.DATA_MISSING);
    }

    @Test
    public void testMandatoryChoiceDeleteContainerNodesUnderCase() throws Exception {
        getModelNode();
        initialiseInterceptor();

        //selecting case4 with empty container
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>test</mandatory-choice-control-leaf>"+
                "<mandatory-choice-container>" +
                "<case4Container3/>"+
                "</mandatory-choice-container>" +
                "</choice-container>" ;

        editConfig(m_server, m_clientInfo, requestXml, true);

        String expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:mandatory-choice-control-leaf>test</choice-case-test:mandatory-choice-control-leaf>"
                + "<choice-case-test:mandatory-choice-container>"
                + "<choice-case-test:case4Container1/>"
                + "<choice-case-test:case4Container2/>"
                + "<choice-case-test:case4Container3/>"
                + "<choice-case-test:leaf1>hello</choice-case-test:leaf1>"
                + "</choice-case-test:mandatory-choice-container>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);

        //selecting case4 with only leaf node
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>test</mandatory-choice-control-leaf>"+
                "<mandatory-choice-container>" +
                "<case4Leaf1>test</case4Leaf1>"+
                "</mandatory-choice-container>" +
                "</choice-container>" ;

        editConfig(m_server, m_clientInfo, requestXml, true);

        expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:mandatory-choice-control-leaf>test</choice-case-test:mandatory-choice-control-leaf>"
                + "<choice-case-test:mandatory-choice-container>"
                + "<choice-case-test:case4Leaf1>test</choice-case-test:case4Leaf1>"    
                + "<choice-case-test:case4Container1/>"
                + "<choice-case-test:case4Container2/>"
                + "<choice-case-test:case4Container3/>"
                + "<choice-case-test:leaf1>hello</choice-case-test:leaf1>"
                + "</choice-case-test:mandatory-choice-container>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);

        // update case4 with other container nodes
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-container>" +
                "<case4Container1>" +
                "<case4Leaf2>test</case4Leaf2>"+
                "</case4Container1>" +
                "<case4Container2>" +
                "<case4Leaf3>test</case4Leaf3>"+
                "</case4Container2>" +
                "</mandatory-choice-container>" +
                "</choice-container>" ;

        editConfig(m_server, m_clientInfo, requestXml, true);

        expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:mandatory-choice-control-leaf>test</choice-case-test:mandatory-choice-control-leaf>"
                + "<choice-case-test:mandatory-choice-container>"
                + "<choice-case-test:case4Leaf1>test</choice-case-test:case4Leaf1>"    
                + "<choice-case-test:case4Container1>"
                + "<choice-case-test:case4Leaf2>test</choice-case-test:case4Leaf2>"    
                + "</choice-case-test:case4Container1>"
                + "<choice-case-test:case4Container2>"
                + "<choice-case-test:case4Leaf3>test</choice-case-test:case4Leaf3>"   
                + "</choice-case-test:case4Container2>"
                + "<choice-case-test:case4Container3/>"
                + "<choice-case-test:leaf1>hello</choice-case-test:leaf1>"
                + "</choice-case-test:mandatory-choice-container>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);

        //delete all leaf nodes of case4
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-container>" +
                "<case4Leaf1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>"+
                "<case4Container1>" +
                "<case4Leaf2 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>"+
                "</case4Container1>" +
                "<case4Container2>" +
                "<case4Leaf3 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>"+
                "</case4Container2>" +
                "</mandatory-choice-container>" +
                "</choice-container>" ;

        editConfig(m_server, m_clientInfo, requestXml, true);

        expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:mandatory-choice-control-leaf>test</choice-case-test:mandatory-choice-control-leaf>"
                + "<choice-case-test:mandatory-choice-container>"
                + "<choice-case-test:case4Container1/>"
                + "<choice-case-test:case4Container2/>"
                + "<choice-case-test:case4Container3/>"
                + "<choice-case-test:leaf1>hello</choice-case-test:leaf1>"
                + "</choice-case-test:mandatory-choice-container>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);

        // delete case4Container1 of case4
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-container>" +
                "<case4Container1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>" +
                "</mandatory-choice-container>" +
                "</choice-container>" ;

        editConfig(m_server, m_clientInfo, requestXml, true);

        expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:mandatory-choice-control-leaf>test</choice-case-test:mandatory-choice-control-leaf>"
                + "<choice-case-test:mandatory-choice-container>"
                + "<choice-case-test:case4Container2/>"
                + "<choice-case-test:case4Container3/>"
                + "<choice-case-test:leaf1>hello</choice-case-test:leaf1>"
                + "</choice-case-test:mandatory-choice-container>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);

        // delete case4Container3 of case4
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-container>" +
                "<case4Container3 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>" +
                "</mandatory-choice-container>" +
                "</choice-container>" ;

        editConfig(m_server, m_clientInfo, requestXml, true);

        expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:mandatory-choice-control-leaf>test</choice-case-test:mandatory-choice-control-leaf>"
                + "<choice-case-test:mandatory-choice-container>"
                + "<choice-case-test:case4Container2/>"
                + "<choice-case-test:leaf1>hello</choice-case-test:leaf1>"
                + "</choice-case-test:mandatory-choice-container>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);

        // delete case4Container2 of case4
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-container>" +
                "<case4Container2 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>" +
                "</mandatory-choice-container>" +
                "</choice-container>" ;

        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:mandatory-choice-container/choice-case-test:choice-test", "Missing mandatory node - choice-test", NetconfRpcErrorTag.DATA_MISSING);
    }

    @Test
    public void testMandatoryChoiceDeleteListNodesUnderCase() throws Exception {
        getModelNode();
        initialiseInterceptor();
        //selecting case5 with only leaf node
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>test</mandatory-choice-control-leaf>"+
                "<mandatory-choice-container>" +
                "<case5Leaf1>test</case5Leaf1>"+
                "</mandatory-choice-container>" +
                "</choice-container>" ;

        editConfig(m_server, m_clientInfo, requestXml, true);

        String expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:mandatory-choice-control-leaf>test</choice-case-test:mandatory-choice-control-leaf>"
                + "<choice-case-test:mandatory-choice-container>"
                + "<choice-case-test:case5Leaf1>test</choice-case-test:case5Leaf1>"    
                + "<choice-case-test:leaf1>hello</choice-case-test:leaf1>"
                + "</choice-case-test:mandatory-choice-container>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);

        //update case5 list entries
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-container>" +
                "<case5List1>" +
                "<case5Leaf2>test1</case5Leaf2>"+
                "</case5List1>" +
                "<case5List1>" +
                "<case5Leaf2>test2</case5Leaf2>"+
                "</case5List1>" +
                "<case5List2>" +
                "<case5Leaf3>test</case5Leaf3>"+
                "</case5List2>" +
                "</mandatory-choice-container>" +
                "</choice-container>" ;

        editConfig(m_server, m_clientInfo, requestXml, true);

        expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:mandatory-choice-control-leaf>test</choice-case-test:mandatory-choice-control-leaf>"
                + "<choice-case-test:mandatory-choice-container>"
                + "<choice-case-test:case5Leaf1>test</choice-case-test:case5Leaf1>"    
                + "<choice-case-test:case5List1>"
                + "<choice-case-test:case5Leaf2>test1</choice-case-test:case5Leaf2>"    
                + "</choice-case-test:case5List1>"
                + "<choice-case-test:case5List1>"
                + "<choice-case-test:case5Leaf2>test2</choice-case-test:case5Leaf2>"   
                + "</choice-case-test:case5List1>"
                + "<choice-case-test:case5List2>"
                + "<choice-case-test:case5Leaf3>test</choice-case-test:case5Leaf3>"   
                + "</choice-case-test:case5List2>"
                + "<choice-case-test:leaf1>hello</choice-case-test:leaf1>"
                + "</choice-case-test:mandatory-choice-container>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);

        // delete case5Leaf1 of case5
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-container>" +
                "<case5Leaf1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>" +
                "</mandatory-choice-container>" +
                "</choice-container>" ;

        editConfig(m_server, m_clientInfo, requestXml, true);

        expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:mandatory-choice-control-leaf>test</choice-case-test:mandatory-choice-control-leaf>"
                + "<choice-case-test:mandatory-choice-container>"
                + "<choice-case-test:case5List1>"
                + "<choice-case-test:case5Leaf2>test1</choice-case-test:case5Leaf2>"    
                + "</choice-case-test:case5List1>"
                + "<choice-case-test:case5List1>"
                + "<choice-case-test:case5Leaf2>test2</choice-case-test:case5Leaf2>"   
                + "</choice-case-test:case5List1>"
                + "<choice-case-test:case5List2>"
                + "<choice-case-test:case5Leaf3>test</choice-case-test:case5Leaf3>"   
                + "</choice-case-test:case5List2>"
                + "<choice-case-test:leaf1>hello</choice-case-test:leaf1>"
                + "</choice-case-test:mandatory-choice-container>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);

        // delete case5List2 of case5
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-container>" +
                "<case5List2 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\">" +
                "<case5Leaf3>test</case5Leaf3>" +
                "</case5List2>" +
                "</mandatory-choice-container>" +
                "</choice-container>" ;

        editConfig(m_server, m_clientInfo, requestXml, true);

        expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:mandatory-choice-control-leaf>test</choice-case-test:mandatory-choice-control-leaf>"
                + "<choice-case-test:mandatory-choice-container>"
                + "<choice-case-test:case5List1>"
                + "<choice-case-test:case5Leaf2>test1</choice-case-test:case5Leaf2>"    
                + "</choice-case-test:case5List1>"
                + "<choice-case-test:case5List1>"
                + "<choice-case-test:case5Leaf2>test2</choice-case-test:case5Leaf2>"   
                + "</choice-case-test:case5List1>"
                + "<choice-case-test:leaf1>hello</choice-case-test:leaf1>"
                + "</choice-case-test:mandatory-choice-container>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);

        // delete case5List1 entries of case5
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-container>" +
                "<case5List1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\">" +
                "<case5Leaf2>test1</case5Leaf2>" +
                "</case5List1>" +
                "</mandatory-choice-container>" +
                "</choice-container>" ;

        editConfig(m_server, m_clientInfo, requestXml, true);

        expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:mandatory-choice-control-leaf>test</choice-case-test:mandatory-choice-control-leaf>"
                + "<choice-case-test:mandatory-choice-container>"
                + "<choice-case-test:case5List1>"
                + "<choice-case-test:case5Leaf2>test2</choice-case-test:case5Leaf2>"   
                + "</choice-case-test:case5List1>"
                + "<choice-case-test:leaf1>hello</choice-case-test:leaf1>"
                + "</choice-case-test:mandatory-choice-container>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);

        // delete case5List1 entries of case5
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-container>" +
                "<case5List1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\">" +
                "<case5Leaf2>test2</case5Leaf2>" +
                "</case5List1>" +
                "</mandatory-choice-container>" +
                "</choice-container>" ;

        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:mandatory-choice-container/choice-case-test:choice-test", "Missing mandatory node - choice-test", NetconfRpcErrorTag.DATA_MISSING);
    }

    @Test
    public void testMandatoryChoiceChangeCaseNodes() throws Exception {
        getModelNode();
        initialiseInterceptor();
        //selecting case5 
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>test</mandatory-choice-control-leaf>"+
                "<mandatory-choice-container>" +
                "<case5Leaf1>test</case5Leaf1>"+
                "<case5List1>" +
                "<case5Leaf2>test1</case5Leaf2>"+
                "</case5List1>" +
                "<case5List1>" +
                "<case5Leaf2>test2</case5Leaf2>"+
                "</case5List1>" +
                "<case5List2>" +
                "<case5Leaf3>test</case5Leaf3>"+
                "</case5List2>" +
                "</mandatory-choice-container>" +
                "</choice-container>" ;

        editConfig(m_server, m_clientInfo, requestXml, true);

        String expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:mandatory-choice-control-leaf>test</choice-case-test:mandatory-choice-control-leaf>"
                + "<choice-case-test:mandatory-choice-container>"
                + "<choice-case-test:case5Leaf1>test</choice-case-test:case5Leaf1>"    
                + "<choice-case-test:case5List1>"
                + "<choice-case-test:case5Leaf2>test1</choice-case-test:case5Leaf2>"    
                + "</choice-case-test:case5List1>"
                + "<choice-case-test:case5List1>"
                + "<choice-case-test:case5Leaf2>test2</choice-case-test:case5Leaf2>"   
                + "</choice-case-test:case5List1>"
                + "<choice-case-test:case5List2>"
                + "<choice-case-test:case5Leaf3>test</choice-case-test:case5Leaf3>"   
                + "</choice-case-test:case5List2>"
                + "<choice-case-test:leaf1>hello</choice-case-test:leaf1>"
                + "</choice-case-test:mandatory-choice-container>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);

        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-container>" +
                "<case1Leaf1>test</case1Leaf1>"+
                "</mandatory-choice-container>" +
                "</choice-container>" ;

        editConfig(m_server, m_clientInfo, requestXml, true);

        // default nodes in the selected cases are not created because of FNMS-44195
        expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:mandatory-choice-control-leaf>test</choice-case-test:mandatory-choice-control-leaf>"
                + "<choice-case-test:mandatory-choice-container>"
                + "<choice-case-test:case1Leaf1>test</choice-case-test:case1Leaf1>"    
                + "<choice-case-test:leaf1>hello</choice-case-test:leaf1>"
                + "<choice-case-test:case1Leaf2>XYZ</choice-case-test:case1Leaf2>"
                + "<choice-case-test:test-container>"
                + "<choice-case-test:case1Defaultleaf1>xxx</choice-case-test:case1Defaultleaf1>"
                + "</choice-case-test:test-container>"
                + "</choice-case-test:mandatory-choice-container>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);
    }

    @Test
    public void testMandatoryChoiceDeleteAllCase() throws Exception {
        getModelNode();
        initialiseInterceptor();
        //selecting case1
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>test</mandatory-choice-control-leaf>"+
                "<mandatory-choice-container>" +
                "<case1Leaf1>test</case1Leaf1>"+
                "</mandatory-choice-container>" +
                "</choice-container>" ;

        editConfig(m_server, m_clientInfo, requestXml, true);

        String expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:mandatory-choice-control-leaf>test</choice-case-test:mandatory-choice-control-leaf>"
                + "<choice-case-test:mandatory-choice-container>"
                + "<choice-case-test:case1Leaf1>test</choice-case-test:case1Leaf1>"    
                + "<choice-case-test:case1Leaf2>XYZ</choice-case-test:case1Leaf2>"  
                + "<choice-case-test:test-container>"
                + "<choice-case-test:case1Defaultleaf1>xxx</choice-case-test:case1Defaultleaf1>"
                + "</choice-case-test:test-container>"
                + "<choice-case-test:leaf1>hello</choice-case-test:leaf1>"
                + "</choice-case-test:mandatory-choice-container>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);

        //selecting case2
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <mandatory-choice-container>" +
                " <case2Leaf1>test</case2Leaf1>"+
                "</mandatory-choice-container>" +
                "</choice-container>" ;

        editConfig(m_server, m_clientInfo, requestXml, true);

        expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:mandatory-choice-control-leaf>test</choice-case-test:mandatory-choice-control-leaf>"
                + "<choice-case-test:mandatory-choice-container>"
                + "<choice-case-test:case2Leaf1>test</choice-case-test:case2Leaf1>"       
                + "<choice-case-test:leaf1>hello</choice-case-test:leaf1>"
                + "</choice-case-test:mandatory-choice-container>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);

        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <mandatory-choice-container>" +
                "  <case2Leaf1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>"+
                " </mandatory-choice-container>" +
                "</choice-container>" ;
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:mandatory-choice-container/choice-case-test:choice-test", "Missing mandatory node - choice-test", NetconfRpcErrorTag.DATA_MISSING);
    }

    @Test
    public void testMandatoryChoiceCaseWithContainerAndList() throws Exception {
        getModelNode();
        initialiseInterceptor();
        //selecting case6
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>test</mandatory-choice-control-leaf>"+
                "<mandatory-choice-container>" +
                "<case6Leaf1>test</case6Leaf1>"+
                "<case6List1>" +
                "<case6Leaf3>test1</case6Leaf3>"+
                "</case6List1>" +
                "<case6List1>" +
                "<case6Leaf3>test2</case6Leaf3>"+
                "</case6List1>" +
                "<case6Container1>" +
                "<case6Leaf2>test</case6Leaf2>"+
                "</case6Container1>" +
                "</mandatory-choice-container>" +
                "</choice-container>" ;

        editConfig(m_server, m_clientInfo, requestXml, true);

        String expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:mandatory-choice-control-leaf>test</choice-case-test:mandatory-choice-control-leaf>"
                + "<choice-case-test:mandatory-choice-container>"
                + "<choice-case-test:case6Leaf1>test</choice-case-test:case6Leaf1>"
                + "<choice-case-test:case6List1>" 
                + "<choice-case-test:case6Leaf3>test1</choice-case-test:case6Leaf3>"
                + "</choice-case-test:case6List1>" 
                + "<choice-case-test:case6List1>" 
                + "<choice-case-test:case6Leaf3>test2</choice-case-test:case6Leaf3>"
                + "</choice-case-test:case6List1>" 
                + "<choice-case-test:case6Container1>" 
                + "<choice-case-test:case6Leaf2>test</choice-case-test:case6Leaf2>"
                + "</choice-case-test:case6Container1>" 
                + "<choice-case-test:leaf1>hello</choice-case-test:leaf1>"
                + "</choice-case-test:mandatory-choice-container>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);

        //delete one of list entry
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <mandatory-choice-container>" +
                "  <case6Leaf1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>"+
                "  <case6List1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\">" +
                "  <case6Leaf3>test1</case6Leaf3>" +
                "  </case6List1>" +
                " </mandatory-choice-container>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true);

        expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:mandatory-choice-control-leaf>test</choice-case-test:mandatory-choice-control-leaf>"
                + "<choice-case-test:mandatory-choice-container>"
                + "<choice-case-test:case6List1>" 
                + "<choice-case-test:case6Leaf3>test2</choice-case-test:case6Leaf3>"
                + "</choice-case-test:case6List1>" 
                + "<choice-case-test:case6Container1>" 
                + "<choice-case-test:case6Leaf2>test</choice-case-test:case6Leaf2>"
                + "</choice-case-test:case6Container1>" 
                + "<choice-case-test:leaf1>hello</choice-case-test:leaf1>"
                + "</choice-case-test:mandatory-choice-container>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);

        //delete all list entries
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <mandatory-choice-container>" +
                "  <case6List1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\">" +
                "  <case6Leaf3>test2</case6Leaf3>" +
                "  </case6List1>" +
                " </mandatory-choice-container>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true);

        expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:mandatory-choice-control-leaf>test</choice-case-test:mandatory-choice-control-leaf>"
                + "<choice-case-test:mandatory-choice-container>"
                + "<choice-case-test:case6Container1>" 
                + "<choice-case-test:case6Leaf2>test</choice-case-test:case6Leaf2>"
                + "</choice-case-test:case6Container1>" 
                + "<choice-case-test:leaf1>hello</choice-case-test:leaf1>"
                + "</choice-case-test:mandatory-choice-container>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);

        //delete container's leaf
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <mandatory-choice-container>" +
                "  <case6Container1>" +
                "  <case6Leaf2 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>" +
                "  </case6Container1>" +
                " </mandatory-choice-container>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true);

        expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:mandatory-choice-control-leaf>test</choice-case-test:mandatory-choice-control-leaf>"
                + "<choice-case-test:mandatory-choice-container>"
                + "<choice-case-test:case6Container1 >" 
                + "</choice-case-test:case6Container1>" 
                + "<choice-case-test:leaf1>hello</choice-case-test:leaf1>"
                + "</choice-case-test:mandatory-choice-container>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);

        //delete container itself
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <mandatory-choice-container>" +
                "  <case6Container1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>" +
                " </mandatory-choice-container>" +
                "</choice-container>" ;
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:mandatory-choice-container/choice-case-test:choice-test", "Missing mandatory node - choice-test", NetconfRpcErrorTag.DATA_MISSING);
    }

    @Test
    public void testMandatoryChoiceCaseWithNestedContainerList() throws Exception {
        getModelNode();
        initialiseInterceptor();
        //selecting case7 with top list entry
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>test</mandatory-choice-control-leaf>"+
                "<mandatory-choice-container>" +
                "<case7List1>" +
                "<case7Leaf1>test1</case7Leaf1>"+
                "</case7List1>" +
                "</mandatory-choice-container>" +
                "</choice-container>" ;

        editConfig(m_server, m_clientInfo, requestXml, true);

        String expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:mandatory-choice-control-leaf>test</choice-case-test:mandatory-choice-control-leaf>"
                + "<choice-case-test:mandatory-choice-container>"
                + "<choice-case-test:case7Container1>" 
                + "<choice-case-test:case7InnerContainer/>"
                + "</choice-case-test:case7Container1>" 
                + "<choice-case-test:case7List1>" 
                + "<choice-case-test:case7Leaf1>test1</choice-case-test:case7Leaf1>"
                + "</choice-case-test:case7List1>" 
                + "<choice-case-test:leaf1>hello</choice-case-test:leaf1>"
                + "</choice-case-test:mandatory-choice-container>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);

        //update case with inner-list entries
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>test</mandatory-choice-control-leaf>"+
                "<mandatory-choice-container>" +
                "<case7List1>" +
                "<case7Leaf1>test1</case7Leaf1>"+
                "<case7InnerList>" +
                "<case7InnerListLeaf>innerList1</case7InnerListLeaf>"+
                "<case7InnerNonKeyLeaf>rock</case7InnerNonKeyLeaf>"+
                "</case7InnerList>" +
                "<case7InnerList>" +
                "<case7InnerListLeaf>innerList2</case7InnerListLeaf>"+
                "<case7InnerNonKeyLeaf>rock</case7InnerNonKeyLeaf>"+
                "</case7InnerList>" +
                "</case7List1>" +
                "<case7List1>" +
                "<case7Leaf1>test2</case7Leaf1>"+
                "<case7InnerList>" +
                "<case7InnerListLeaf>innerList1</case7InnerListLeaf>"+
                "<case7InnerNonKeyLeaf>rock</case7InnerNonKeyLeaf>"+
                "</case7InnerList>" +
                "</case7List1>" +
                "</mandatory-choice-container>" +
                "</choice-container>" ;

        editConfig(m_server, m_clientInfo, requestXml, true);

        expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:mandatory-choice-control-leaf>test</choice-case-test:mandatory-choice-control-leaf>"
                + "<choice-case-test:mandatory-choice-container>"
                + "<choice-case-test:case7Container1>"
                + "<choice-case-test:case7InnerContainer/>"
                + "</choice-case-test:case7Container1>"
                + "<choice-case-test:case7List1>"
                + "<choice-case-test:case7InnerList>"
                + "<choice-case-test:case7InnerListLeaf>innerList1</choice-case-test:case7InnerListLeaf>"
                + "<choice-case-test:case7InnerNonKeyLeaf>rock</choice-case-test:case7InnerNonKeyLeaf>"
                + "</choice-case-test:case7InnerList>"
                + "<choice-case-test:case7InnerList>"
                + "<choice-case-test:case7InnerListLeaf>innerList2</choice-case-test:case7InnerListLeaf>"
                + "<choice-case-test:case7InnerNonKeyLeaf>rock</choice-case-test:case7InnerNonKeyLeaf>"
                + "</choice-case-test:case7InnerList>"
                + "<choice-case-test:case7Leaf1>test1</choice-case-test:case7Leaf1>"
                + "</choice-case-test:case7List1>"
                + "<choice-case-test:case7List1>"
                + "<choice-case-test:case7InnerList>"
                + "<choice-case-test:case7InnerListLeaf>innerList1</choice-case-test:case7InnerListLeaf>"
                + "<choice-case-test:case7InnerNonKeyLeaf>rock</choice-case-test:case7InnerNonKeyLeaf>"
                + "</choice-case-test:case7InnerList>"
                + "<choice-case-test:case7Leaf1>test2</choice-case-test:case7Leaf1>"
                + "</choice-case-test:case7List1>"
                + "<choice-case-test:leaf1>hello</choice-case-test:leaf1>"
                + "</choice-case-test:mandatory-choice-container>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);

        //delete inner-container 
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <mandatory-choice-container>" +
                "  <case7Container1>" +
                "  <case7InnerContainer xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>" +
                "  </case7Container1>" +
                " </mandatory-choice-container>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true);

        expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:mandatory-choice-control-leaf>test</choice-case-test:mandatory-choice-control-leaf>"
                + "<choice-case-test:mandatory-choice-container>"
                + "<choice-case-test:case7Container1>"
                + "</choice-case-test:case7Container1>"
                + "<choice-case-test:case7List1>"
                + "<choice-case-test:case7InnerList>"
                + "<choice-case-test:case7InnerListLeaf>innerList1</choice-case-test:case7InnerListLeaf>"
                + "<choice-case-test:case7InnerNonKeyLeaf>rock</choice-case-test:case7InnerNonKeyLeaf>"
                + "</choice-case-test:case7InnerList>"
                + "<choice-case-test:case7InnerList>"
                + "<choice-case-test:case7InnerListLeaf>innerList2</choice-case-test:case7InnerListLeaf>"
                + "<choice-case-test:case7InnerNonKeyLeaf>rock</choice-case-test:case7InnerNonKeyLeaf>"
                + "</choice-case-test:case7InnerList>"
                + "<choice-case-test:case7Leaf1>test1</choice-case-test:case7Leaf1>"
                + "</choice-case-test:case7List1>"
                + "<choice-case-test:case7List1>"
                + "<choice-case-test:case7InnerList>"
                + "<choice-case-test:case7InnerListLeaf>innerList1</choice-case-test:case7InnerListLeaf>"
                + "<choice-case-test:case7InnerNonKeyLeaf>rock</choice-case-test:case7InnerNonKeyLeaf>"
                + "</choice-case-test:case7InnerList>"
                + "<choice-case-test:case7Leaf1>test2</choice-case-test:case7Leaf1>"
                + "</choice-case-test:case7List1>"
                + "<choice-case-test:leaf1>hello</choice-case-test:leaf1>"
                + "</choice-case-test:mandatory-choice-container>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);

        //delete case container
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <mandatory-choice-container>" +
                "  <case7Container1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>" +
                " </mandatory-choice-container>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true);

        expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:mandatory-choice-control-leaf>test</choice-case-test:mandatory-choice-control-leaf>"
                + "<choice-case-test:mandatory-choice-container>"
                + "<choice-case-test:case7List1>"
                + "<choice-case-test:case7InnerList>"
                + "<choice-case-test:case7InnerListLeaf>innerList1</choice-case-test:case7InnerListLeaf>"
                + "<choice-case-test:case7InnerNonKeyLeaf>rock</choice-case-test:case7InnerNonKeyLeaf>"
                + "</choice-case-test:case7InnerList>"
                + "<choice-case-test:case7InnerList>"
                + "<choice-case-test:case7InnerListLeaf>innerList2</choice-case-test:case7InnerListLeaf>"
                + "<choice-case-test:case7InnerNonKeyLeaf>rock</choice-case-test:case7InnerNonKeyLeaf>"
                + "</choice-case-test:case7InnerList>"
                + "<choice-case-test:case7Leaf1>test1</choice-case-test:case7Leaf1>"
                + "</choice-case-test:case7List1>"
                + "<choice-case-test:case7List1>"
                + "<choice-case-test:case7InnerList>"
                + "<choice-case-test:case7InnerListLeaf>innerList1</choice-case-test:case7InnerListLeaf>"
                + "<choice-case-test:case7InnerNonKeyLeaf>rock</choice-case-test:case7InnerNonKeyLeaf>"
                + "</choice-case-test:case7InnerList>"
                + "<choice-case-test:case7Leaf1>test2</choice-case-test:case7Leaf1>"
                + "</choice-case-test:case7List1>"
                + "<choice-case-test:leaf1>hello</choice-case-test:leaf1>"
                + "</choice-case-test:mandatory-choice-container>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);

        // delete case 7 list's inner-list non-key leaf
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <mandatory-choice-container>" +
                "  <case7List1>" +
                "  <case7Leaf1>test2</case7Leaf1>" +
                "  <case7InnerList>" +
                "  <case7InnerListLeaf>innerList1</case7InnerListLeaf>" +
                "  <case7InnerNonKeyLeaf xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>" +
                "  </case7InnerList>" +
                "  </case7List1>" +
                " </mandatory-choice-container>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true);

        expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:mandatory-choice-control-leaf>test</choice-case-test:mandatory-choice-control-leaf>"
                + "<choice-case-test:mandatory-choice-container>"
                + "<choice-case-test:case7Container1>" 
                + "<choice-case-test:case7InnerContainer/>" 
                + "</choice-case-test:case7Container1>"
                + "<choice-case-test:case7List1>"
                + "<choice-case-test:case7InnerList>"
                + "<choice-case-test:case7InnerListLeaf>innerList1</choice-case-test:case7InnerListLeaf>"
                + "<choice-case-test:case7InnerNonKeyLeaf>rock</choice-case-test:case7InnerNonKeyLeaf>"
                + "</choice-case-test:case7InnerList>"
                + "<choice-case-test:case7InnerList>"
                + "<choice-case-test:case7InnerListLeaf>innerList2</choice-case-test:case7InnerListLeaf>"
                + "<choice-case-test:case7InnerNonKeyLeaf>rock</choice-case-test:case7InnerNonKeyLeaf>"
                + "</choice-case-test:case7InnerList>"
                + "<choice-case-test:case7Leaf1>test1</choice-case-test:case7Leaf1>"
                + "</choice-case-test:case7List1>"
                + "<choice-case-test:case7List1>"
                + "<choice-case-test:case7InnerList>"
                + "<choice-case-test:case7InnerListLeaf>innerList1</choice-case-test:case7InnerListLeaf>"
                + "</choice-case-test:case7InnerList>"
                + "<choice-case-test:case7Leaf1>test2</choice-case-test:case7Leaf1>"
                + "</choice-case-test:case7List1>"
                + "<choice-case-test:leaf1>hello</choice-case-test:leaf1>"
                + "</choice-case-test:mandatory-choice-container>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);

        //delete case list's inner-list one of the entry
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <mandatory-choice-container>" +
                "  <case7List1>" +
                "  <case7Leaf1>test2</case7Leaf1>" +
                "  <case7InnerList xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\">" +
                "  <case7InnerListLeaf>innerList1</case7InnerListLeaf>" +
                "  </case7InnerList>" +
                "  </case7List1>" +
                " </mandatory-choice-container>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true);

        expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:mandatory-choice-control-leaf>test</choice-case-test:mandatory-choice-control-leaf>"
                + "<choice-case-test:mandatory-choice-container>"
                + "<choice-case-test:case7Container1>" 
                + "<choice-case-test:case7InnerContainer/>" 
                + "</choice-case-test:case7Container1>"
                + "<choice-case-test:case7List1>"
                + "<choice-case-test:case7InnerList>"
                + "<choice-case-test:case7InnerListLeaf>innerList1</choice-case-test:case7InnerListLeaf>"
                + "<choice-case-test:case7InnerNonKeyLeaf>rock</choice-case-test:case7InnerNonKeyLeaf>"
                + "</choice-case-test:case7InnerList>"
                + "<choice-case-test:case7InnerList>"
                + "<choice-case-test:case7InnerListLeaf>innerList2</choice-case-test:case7InnerListLeaf>"
                + "<choice-case-test:case7InnerNonKeyLeaf>rock</choice-case-test:case7InnerNonKeyLeaf>"
                + "</choice-case-test:case7InnerList>"
                + "<choice-case-test:case7Leaf1>test1</choice-case-test:case7Leaf1>"
                + "</choice-case-test:case7List1>"
                + "<choice-case-test:case7List1>"
                + "<choice-case-test:case7Leaf1>test2</choice-case-test:case7Leaf1>"
                + "</choice-case-test:case7List1>"
                + "<choice-case-test:leaf1>hello</choice-case-test:leaf1>"
                + "</choice-case-test:mandatory-choice-container>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);

        //delete list's entry
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <mandatory-choice-container>" +
                "  <case7List1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\">" +
                "  <case7Leaf1>test2</case7Leaf1>" +
                "  </case7List1>" +
                " </mandatory-choice-container>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true);

        expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:mandatory-choice-control-leaf>test</choice-case-test:mandatory-choice-control-leaf>"
                + "<choice-case-test:mandatory-choice-container>"
                + "<choice-case-test:case7Container1>" 
                + "<choice-case-test:case7InnerContainer/>" 
                + "</choice-case-test:case7Container1>"
                + "<choice-case-test:case7List1>"
                + "<choice-case-test:case7InnerList>"
                + "<choice-case-test:case7InnerListLeaf>innerList1</choice-case-test:case7InnerListLeaf>"
                + "<choice-case-test:case7InnerNonKeyLeaf>rock</choice-case-test:case7InnerNonKeyLeaf>"
                + "</choice-case-test:case7InnerList>"
                + "<choice-case-test:case7InnerList>"
                + "<choice-case-test:case7InnerListLeaf>innerList2</choice-case-test:case7InnerListLeaf>"
                + "<choice-case-test:case7InnerNonKeyLeaf>rock</choice-case-test:case7InnerNonKeyLeaf>"
                + "</choice-case-test:case7InnerList>"
                + "<choice-case-test:case7Leaf1>test1</choice-case-test:case7Leaf1>"
                + "</choice-case-test:case7List1>"
                + "<choice-case-test:leaf1>hello</choice-case-test:leaf1>"
                + "</choice-case-test:mandatory-choice-container>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);

        //delete case list's other entry inner-list entry
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <mandatory-choice-container>" +
                "  <case7List1>" +
                "  <case7Leaf1>test1</case7Leaf1>" +
                "  <case7InnerList xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\">" +
                "  <case7InnerListLeaf>innerList2</case7InnerListLeaf>" +
                "  </case7InnerList>" +
                "  </case7List1>" +
                " </mandatory-choice-container>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true);

        expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:mandatory-choice-control-leaf>test</choice-case-test:mandatory-choice-control-leaf>"
                + "<choice-case-test:mandatory-choice-container>"
                + "<choice-case-test:case7Container1>" 
                + "<choice-case-test:case7InnerContainer/>" 
                + "</choice-case-test:case7Container1>"
                + "<choice-case-test:case7List1>"
                + "<choice-case-test:case7InnerList>"
                + "<choice-case-test:case7InnerListLeaf>innerList1</choice-case-test:case7InnerListLeaf>"
                + "<choice-case-test:case7InnerNonKeyLeaf>rock</choice-case-test:case7InnerNonKeyLeaf>"
                + "</choice-case-test:case7InnerList>"
                + "<choice-case-test:case7Leaf1>test1</choice-case-test:case7Leaf1>"
                + "</choice-case-test:case7List1>"
                + "<choice-case-test:leaf1>hello</choice-case-test:leaf1>"
                + "</choice-case-test:mandatory-choice-container>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);

        //delete case list's other entry inner-list entry
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <mandatory-choice-container>" +
                "  <case7List1>" +
                "  <case7Leaf1>test1</case7Leaf1>" +
                "  <case7InnerList xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\">" +
                "  <case7InnerListLeaf>innerList1</case7InnerListLeaf>" +
                "  </case7InnerList>" +
                "  </case7List1>" +
                " </mandatory-choice-container>" +
                "</choice-container>" ;
        editConfig(m_server, m_clientInfo, requestXml, true);

        expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:mandatory-choice-control-leaf>test</choice-case-test:mandatory-choice-control-leaf>"
                + "<choice-case-test:mandatory-choice-container>"
                + "<choice-case-test:case7Container1>" 
                + "<choice-case-test:case7InnerContainer/>" 
                + "</choice-case-test:case7Container1>"
                + "<choice-case-test:case7List1>"
                + "<choice-case-test:case7Leaf1>test1</choice-case-test:case7Leaf1>"
                + "</choice-case-test:case7List1>"
                + "<choice-case-test:leaf1>hello</choice-case-test:leaf1>"
                + "</choice-case-test:mandatory-choice-container>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);

        //delete case list entry itself
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                " <mandatory-choice-container>" +
                "  <case7Container1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>" +
                "  <case7List1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\">" +
                "  <case7Leaf1>test1</case7Leaf1>" +
                "  </case7List1>" +
                " </mandatory-choice-container>" +
                "</choice-container>" ;
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:mandatory-choice-container/choice-case-test:choice-test", "Missing mandatory node - choice-test", NetconfRpcErrorTag.DATA_MISSING);
    }

    @Test
    public void testMandatoryChoiceCaseWithNestedContainerListUsecase2() throws Exception {
        getModelNode();
        initialiseInterceptor();
        //selecting case7 with inner-container entry
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>test</mandatory-choice-control-leaf>"+
                "<mandatory-choice-container>" +
                "<case7Container1>" +
                "<case7InnerContainer>" +
                "<case7Leaf1>test1</case7Leaf1>"+
                "</case7InnerContainer>" +
                "</case7Container1>" +
                "</mandatory-choice-container>" +
                "</choice-container>" ;

        editConfig(m_server, m_clientInfo, requestXml, true);

        String expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:mandatory-choice-control-leaf>test</choice-case-test:mandatory-choice-control-leaf>"
                + "<choice-case-test:mandatory-choice-container>"
                + "<choice-case-test:case7Container1>" 
                + "<choice-case-test:case7InnerContainer>"
                + "<choice-case-test:case7Leaf1>test1</choice-case-test:case7Leaf1>"
                + "</choice-case-test:case7InnerContainer>"
                + "</choice-case-test:case7Container1>" 
                + "<choice-case-test:leaf1>hello</choice-case-test:leaf1>"
                + "</choice-case-test:mandatory-choice-container>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);

        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>test</mandatory-choice-control-leaf>"+
                "<mandatory-choice-container>" +
                "<case7Container1>" +
                "<case7InnerContainer>" +
                "<case7Leaf1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>"+
                "</case7InnerContainer>" +
                "</case7Container1>" +
                "</mandatory-choice-container>" +
                "</choice-container>" ;

        editConfig(m_server, m_clientInfo, requestXml, true);

        expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:mandatory-choice-control-leaf>test</choice-case-test:mandatory-choice-control-leaf>"
                + "<choice-case-test:mandatory-choice-container>"
                + "<choice-case-test:case7Container1>" 
                + "<choice-case-test:case7InnerContainer>"
                + "</choice-case-test:case7InnerContainer>"
                + "</choice-case-test:case7Container1>" 
                + "<choice-case-test:leaf1>hello</choice-case-test:leaf1>"
                + "</choice-case-test:mandatory-choice-container>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);

        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>test</mandatory-choice-control-leaf>"+
                "<mandatory-choice-container>" +
                "<case7Container1>" +
                "<case7InnerContainer xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>" +
                "</case7Container1>" +
                "</mandatory-choice-container>" +
                "</choice-container>" ;

        editConfig(m_server, m_clientInfo, requestXml, true);

        expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:mandatory-choice-control-leaf>test</choice-case-test:mandatory-choice-control-leaf>"
                + "<choice-case-test:mandatory-choice-container>"
                + "<choice-case-test:case7Container1>" 
                + "</choice-case-test:case7Container1>" 
                + "<choice-case-test:leaf1>hello</choice-case-test:leaf1>"
                + "</choice-case-test:mandatory-choice-container>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);

        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>test</mandatory-choice-control-leaf>"+
                "<mandatory-choice-container>" +
                "<case7Container1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>" +
                "</mandatory-choice-container>" +
                "</choice-container>" ;

        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:mandatory-choice-container/choice-case-test:choice-test", "Missing mandatory node - choice-test", NetconfRpcErrorTag.DATA_MISSING);
    }

    @Test
    public void testMandatoryNestedChoiceCase() throws Exception {
        getModelNode();
        initialiseInterceptor();
        //selecting case8 nested choice
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>test</mandatory-choice-control-leaf>"+
                "<mandatory-choice-container>" +
                "<case8Leaf1>test</case8Leaf1>"+
                "<innerCase1Leaf1>innerChoiceLeaf</innerCase1Leaf1>"+
                "<innerCase1List1>" +
                "<innerCase1ListKey>key1</innerCase1ListKey>"+
                "<innerCaseNonKeyLeaf>non-key1</innerCaseNonKeyLeaf>"+
                "</innerCase1List1>" +
                "<innerCase1List1>" +
                "<innerCase1ListKey>key2</innerCase1ListKey>"+
                "<innerCaseNonKeyLeaf>non-key1</innerCaseNonKeyLeaf>"+
                "</innerCase1List1>" +
                "</mandatory-choice-container>" +
                "</choice-container>" ;

        editConfig(m_server, m_clientInfo, requestXml, true);

        String expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:mandatory-choice-control-leaf>test</choice-case-test:mandatory-choice-control-leaf>"
                + "<choice-case-test:mandatory-choice-container>"
                + "<choice-case-test:case8Leaf1>test</choice-case-test:case8Leaf1>"
                + "<choice-case-test:innerCase1Leaf1>innerChoiceLeaf</choice-case-test:innerCase1Leaf1>"
                + "<choice-case-test:innerCase1List1>"
                + "<choice-case-test:innerCase1ListKey>key1</choice-case-test:innerCase1ListKey>"
                + "<choice-case-test:innerCaseNonKeyLeaf>non-key1</choice-case-test:innerCaseNonKeyLeaf>"
                + "</choice-case-test:innerCase1List1>"
                + "<choice-case-test:innerCase1List1>"
                + "<choice-case-test:innerCase1ListKey>key2</choice-case-test:innerCase1ListKey>"
                + "<choice-case-test:innerCaseNonKeyLeaf>non-key1</choice-case-test:innerCaseNonKeyLeaf>"
                + "</choice-case-test:innerCase1List1>"
                + "<choice-case-test:innerCaseContainer1/>"
                + "<choice-case-test:leaf1>hello</choice-case-test:leaf1>"
                + "</choice-case-test:mandatory-choice-container>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);

        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>test</mandatory-choice-control-leaf>"+
                "<mandatory-choice-container>" +
                "<case8Leaf1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>"+
                "<innerCaseContainer1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>"+
                "</mandatory-choice-container>" +
                "</choice-container>" ;

        editConfig(m_server, m_clientInfo, requestXml, true);

        expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:mandatory-choice-control-leaf>test</choice-case-test:mandatory-choice-control-leaf>"
                + "<choice-case-test:mandatory-choice-container>"
                + "<choice-case-test:innerCase1Leaf1>innerChoiceLeaf</choice-case-test:innerCase1Leaf1>"
                + "<choice-case-test:innerCase1List1>"
                + "<choice-case-test:innerCase1ListKey>key1</choice-case-test:innerCase1ListKey>"
                + "<choice-case-test:innerCaseNonKeyLeaf>non-key1</choice-case-test:innerCaseNonKeyLeaf>"
                + "</choice-case-test:innerCase1List1>"
                + "<choice-case-test:innerCase1List1>"
                + "<choice-case-test:innerCase1ListKey>key2</choice-case-test:innerCase1ListKey>"
                + "<choice-case-test:innerCaseNonKeyLeaf>non-key1</choice-case-test:innerCaseNonKeyLeaf>"
                + "</choice-case-test:innerCase1List1>"
                + "<choice-case-test:leaf1>hello</choice-case-test:leaf1>"
                + "</choice-case-test:mandatory-choice-container>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);

        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>test</mandatory-choice-control-leaf>"+
                "<mandatory-choice-container>" +
                "<innerCase1List1>" +
                "<innerCase1ListKey>key2</innerCase1ListKey>" +
                "<innerCaseNonKeyLeaf xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>" +
                "</innerCase1List1>" +
                "</mandatory-choice-container>" +
                "</choice-container>" ;

        editConfig(m_server, m_clientInfo, requestXml, true);

        expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:mandatory-choice-control-leaf>test</choice-case-test:mandatory-choice-control-leaf>"
                + "<choice-case-test:mandatory-choice-container>"
                + "<choice-case-test:innerCase1Leaf1>innerChoiceLeaf</choice-case-test:innerCase1Leaf1>"
                + "<choice-case-test:innerCase1List1>"
                + "<choice-case-test:innerCase1ListKey>key1</choice-case-test:innerCase1ListKey>"
                + "<choice-case-test:innerCaseNonKeyLeaf>non-key1</choice-case-test:innerCaseNonKeyLeaf>"
                + "</choice-case-test:innerCase1List1>"
                + "<choice-case-test:innerCase1List1>"
                + "<choice-case-test:innerCase1ListKey>key2</choice-case-test:innerCase1ListKey>"
                + "</choice-case-test:innerCase1List1>"
                + "<choice-case-test:innerCaseContainer1/>"
                + "<choice-case-test:leaf1>hello</choice-case-test:leaf1>"
                + "</choice-case-test:mandatory-choice-container>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);

        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>test</mandatory-choice-control-leaf>"+
                "<mandatory-choice-container>" +
                "<innerCase1List1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\">" +
                "<innerCase1ListKey>key2</innerCase1ListKey>" +
                "</innerCase1List1>" +
                "</mandatory-choice-container>" +
                "</choice-container>" ;

        editConfig(m_server, m_clientInfo, requestXml, true);

        expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:mandatory-choice-control-leaf>test</choice-case-test:mandatory-choice-control-leaf>"
                + "<choice-case-test:mandatory-choice-container>"
                + "<choice-case-test:innerCase1Leaf1>innerChoiceLeaf</choice-case-test:innerCase1Leaf1>"
                + "<choice-case-test:innerCase1List1>"
                + "<choice-case-test:innerCase1ListKey>key1</choice-case-test:innerCase1ListKey>"
                + "<choice-case-test:innerCaseNonKeyLeaf>non-key1</choice-case-test:innerCaseNonKeyLeaf>"
                + "</choice-case-test:innerCase1List1>"
                + "<choice-case-test:innerCaseContainer1/>"
                + "<choice-case-test:leaf1>hello</choice-case-test:leaf1>"
                + "</choice-case-test:mandatory-choice-container>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);

        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>test</mandatory-choice-control-leaf>"+
                "<mandatory-choice-container>" +
                "<innerCase1List1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\">" +
                "<innerCase1ListKey>key1</innerCase1ListKey>" +
                "</innerCase1List1>" +
                "</mandatory-choice-container>" +
                "</choice-container>" ;

        editConfig(m_server, m_clientInfo, requestXml, true);

        expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:mandatory-choice-control-leaf>test</choice-case-test:mandatory-choice-control-leaf>"
                + "<choice-case-test:mandatory-choice-container>"
                + "<choice-case-test:innerCase1Leaf1>innerChoiceLeaf</choice-case-test:innerCase1Leaf1>"
                +" <choice-case-test:innerCaseContainer1/>"
                + "<choice-case-test:leaf1>hello</choice-case-test:leaf1>"
                + "</choice-case-test:mandatory-choice-container>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);

        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>test</mandatory-choice-control-leaf>"+
                "<mandatory-choice-container>" +
                "<innerCase1Leaf1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>" +
                "<innerCaseContainer1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>" +
                "</mandatory-choice-container>" +
                "</choice-container>" ;

        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:mandatory-choice-container/choice-case-test:choice-test", "Missing mandatory node - choice-test", NetconfRpcErrorTag.DATA_MISSING);
    }

    @Test
    public void testMandatoryNestedChoiceCaseWithContainer() throws Exception {
        getModelNode();
        initialiseInterceptor();
        //selecting case8 nested choice
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>test</mandatory-choice-control-leaf>"+
                "<mandatory-choice-container>" +
                "<case8Leaf1>test</case8Leaf1>"+
                "<innerCaseContainer1>" +
                "<innerCaseContainerLeaf>test</innerCaseContainerLeaf>"+
                "</innerCaseContainer1>" +
                "</mandatory-choice-container>" +
                "</choice-container>" ;

        editConfig(m_server, m_clientInfo, requestXml, true);

        String expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:mandatory-choice-control-leaf>test</choice-case-test:mandatory-choice-control-leaf>"
                + "<choice-case-test:mandatory-choice-container>"
                + "<choice-case-test:case8Leaf1>test</choice-case-test:case8Leaf1>"
                + "<choice-case-test:innerCaseContainer1>"
                + "<choice-case-test:innerCaseContainerLeaf>test</choice-case-test:innerCaseContainerLeaf>"
                + "</choice-case-test:innerCaseContainer1>"
                + "<choice-case-test:leaf1>hello</choice-case-test:leaf1>"
                + "</choice-case-test:mandatory-choice-container>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);

        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>test</mandatory-choice-control-leaf>"+
                "<mandatory-choice-container>" +
                "<case8Leaf1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>" +
                "</mandatory-choice-container>" +
                "</choice-container>" ;

        editConfig(m_server, m_clientInfo, requestXml, true);

        expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:mandatory-choice-control-leaf>test</choice-case-test:mandatory-choice-control-leaf>"
                + "<choice-case-test:mandatory-choice-container>"
                + "<choice-case-test:innerCaseContainer1>"
                + "<choice-case-test:innerCaseContainerLeaf>test</choice-case-test:innerCaseContainerLeaf>"
                + "</choice-case-test:innerCaseContainer1>"
                + "<choice-case-test:leaf1>hello</choice-case-test:leaf1>"
                + "</choice-case-test:mandatory-choice-container>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);

        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>test</mandatory-choice-control-leaf>"+
                "<mandatory-choice-container>" +
                "<innerCaseContainer1>" +
                "<innerCaseContainerLeaf xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>"+
                "</innerCaseContainer1>" +
                "</mandatory-choice-container>" +
                "</choice-container>" ;

        editConfig(m_server, m_clientInfo, requestXml, true);

        expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">"
                + "<choice-case-test:mandatory-choice-control-leaf>test</choice-case-test:mandatory-choice-control-leaf>"
                + "<choice-case-test:mandatory-choice-container>"
                + "<choice-case-test:innerCaseContainer1>"
                + "</choice-case-test:innerCaseContainer1>"
                + "<choice-case-test:leaf1>hello</choice-case-test:leaf1>"
                + "</choice-case-test:mandatory-choice-container>"
                + "</choice-case-test:choice-container>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";

        verifyGet(expResponse);

        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>test</mandatory-choice-control-leaf>"+
                "<mandatory-choice-container>" +
                "<innerCaseContainer1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>" +
                "</mandatory-choice-container>" +
                "</choice-container>" ;

        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:mandatory-choice-container/choice-case-test:choice-test", "Missing mandatory node - choice-test", NetconfRpcErrorTag.DATA_MISSING);
    }

    @Test
    public void testDefaultChoiceCaseAutoSelection() throws Exception {
        getModelNode();
        initialiseInterceptor();
        //mandatory-choice-control-leaf set to "defaultEnabled", so default case should be selected
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>"+               
                "</choice-container>" ;

        editConfig(m_server, m_clientInfo, requestXml, true);

        String expResponse = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" + 
                "<data>\n" + 
                "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">\n" + 
                "<choice-case-test:default-choice-container>\n" + 
                "<choice-case-test:case1Leaf1>XYZ</choice-case-test:case1Leaf1>\n" + 
                "</choice-case-test:default-choice-container>\n" + 
                "<choice-case-test:mandatory-choice-control-leaf>defaultEnabled</choice-case-test:mandatory-choice-control-leaf>\n" + 
                "</choice-case-test:choice-container>\n" + 
                "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n" +
                "</data>\n" + 
                "</rpc-reply>";
        verifyGet(expResponse);

        //set case1Leaf2--non-default leaf
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<default-choice-container>"+
                "<case1Leaf1>test</case1Leaf1>"+               
                "<case1Leaf2>test</case1Leaf2>"+               
                "</default-choice-container>"+
                "</choice-container>" ;

        editConfig(m_server, m_clientInfo, requestXml, true);

        expResponse = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" + 
                "<data>\n" + 
                "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">\n" + 
                "<choice-case-test:default-choice-container>\n" + 
                "<choice-case-test:case1Leaf1>test</choice-case-test:case1Leaf1>\n" + 
                "<choice-case-test:case1Leaf2>test</choice-case-test:case1Leaf2>\n" + 
                "</choice-case-test:default-choice-container>\n" + 
                "<choice-case-test:mandatory-choice-control-leaf>defaultEnabled</choice-case-test:mandatory-choice-control-leaf>\n" + 
                "</choice-case-test:choice-container>\n" + 
                "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n" +
                "</data>\n" + 
                "</rpc-reply>";
        verifyGet(expResponse);

        //delete case1Leaf2--non-default leaf
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<default-choice-container>"+
                "<case1Leaf2 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>"+               
                "</default-choice-container>"+
                "</choice-container>" ;

        editConfig(m_server, m_clientInfo, requestXml, true);

        expResponse = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" + 
                "<data>\n" + 
                "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">\n" + 
                "<choice-case-test:default-choice-container>\n" + 
                "<choice-case-test:case1Leaf1>test</choice-case-test:case1Leaf1>\n" + 
                "</choice-case-test:default-choice-container>\n" + 
                "<choice-case-test:mandatory-choice-control-leaf>defaultEnabled</choice-case-test:mandatory-choice-control-leaf>\n" + 
                "</choice-case-test:choice-container>\n" + 
                "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n" +
                "</data>\n" + 
                "</rpc-reply>";
        verifyGet(expResponse);  

        //delete case1Leaf1--default leaf
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<default-choice-container>"+
                "<case1Leaf1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>"+               
                "</default-choice-container>"+
                "</choice-container>" ;

        editConfig(m_server, m_clientInfo, requestXml, true);

        expResponse = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" + 
                "<data>\n" + 
                "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">\n" + 
                "<choice-case-test:default-choice-container>\n" + 
                "<choice-case-test:case1Leaf1>XYZ</choice-case-test:case1Leaf1>\n" + 
                "</choice-case-test:default-choice-container>\n" + 
                "<choice-case-test:mandatory-choice-control-leaf>defaultEnabled</choice-case-test:mandatory-choice-control-leaf>\n" + 
                "</choice-case-test:choice-container>\n" + 
                "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n" +
                "</data>\n" + 
                "</rpc-reply>";
        verifyGet(expResponse); 
    }

    @Test
    public void testDefaultChoiceCase2() throws Exception {
        getModelNode();
        initialiseInterceptor();
        //case2Leaf1 is set, but missing case2LeafList1 which has min-elements constraints
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>"+  
                "<default-choice-container>"+
                "<case2Leaf1>test</case2Leaf1>"+               
                "</default-choice-container>"+
                "</choice-container>" ;

        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:default-choice-container/choice-case-test:case2LeafList1", "Missing mandatory node - case2LeafList1", NetconfRpcErrorTag.DATA_MISSING);

        //case2Leaf1 is set, but missing case2LeafList1 which has min-elements constraints of '2', provided only 1 element
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>"+  
                "<default-choice-container>"+
                "<case2Leaf1>test</case2Leaf1>"+   
                "<case2LeafList1>test1</case2LeafList1>"+               
                "</default-choice-container>"+
                "</choice-container>" ;

        response = editConfig(m_server, m_clientInfo, requestXml, false);
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:default-choice-container/choice-case-test:case2LeafList1", "Minimum elements required for case2LeafList1 is 2.", NetconfRpcErrorTag.OPERATION_FAILED);

        //both case2Leaf1 and case2LeafList1 is set
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>"+  
                "<default-choice-container>"+
                "<case2Leaf1>test</case2Leaf1>"+   
                "<case2LeafList1>test1</case2LeafList1>"+    
                "<case2LeafList1>test2</case2LeafList1>"+               
                "</default-choice-container>"+
                "</choice-container>" ;

        response = editConfig(m_server, m_clientInfo, requestXml, true);

        String expResponse = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" + 
                "<data>\n" + 
                "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">\n" + 
                "<choice-case-test:default-choice-container>\n" + 
                "<choice-case-test:case2Leaf1>test</choice-case-test:case2Leaf1>\n" + 
                "<choice-case-test:case2LeafList1>test1</choice-case-test:case2LeafList1>\n" + 
                "<choice-case-test:case2LeafList1>test2</choice-case-test:case2LeafList1>\n" + 
                "</choice-case-test:default-choice-container>\n" + 
                "<choice-case-test:mandatory-choice-control-leaf>defaultEnabled</choice-case-test:mandatory-choice-control-leaf>\n" + 
                "</choice-case-test:choice-container>\n" + 
                "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n" +
                "</data>\n" + 
                "</rpc-reply>";
        verifyGet(expResponse);

        //delete case2Leaf1 ->non-mandatory leaf
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>"+  
                "<default-choice-container>"+
                "<case2Leaf1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>"+                
                "</default-choice-container>"+
                "</choice-container>" ;

        response = editConfig(m_server, m_clientInfo, requestXml, true);

        expResponse = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" + 
                "<data>\n" + 
                "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">\n" + 
                "<choice-case-test:default-choice-container>\n" + 
                "<choice-case-test:case2LeafList1>test1</choice-case-test:case2LeafList1>\n" + 
                "<choice-case-test:case2LeafList1>test2</choice-case-test:case2LeafList1>\n" + 
                "</choice-case-test:default-choice-container>\n" + 
                "<choice-case-test:mandatory-choice-control-leaf>defaultEnabled</choice-case-test:mandatory-choice-control-leaf>\n" + 
                "</choice-case-test:choice-container>\n" + 
                "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n" +
                "</data>\n" + 
                "</rpc-reply>";
        verifyGet(expResponse);

        //delete case2LeafList1 one entry should fail
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>"+  
                "<default-choice-container>"+
                "<case2LeafList1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\">test1</case2LeafList1>"+    
                "</default-choice-container>"+
                "</choice-container>" ;

        response = editConfig(m_server, m_clientInfo, requestXml, false);
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:default-choice-container/choice-case-test:case2LeafList1", "Minimum elements required for case2LeafList1 is 2.", NetconfRpcErrorTag.OPERATION_FAILED);

        //delete case2LeafList1 both entries should fall back to default case
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>"+  
                "<default-choice-container>"+
                "<case2LeafList1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\">test1</case2LeafList1>"+  
                "<case2LeafList1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\">test2</case2LeafList1>"+    
                "</default-choice-container>"+
                "</choice-container>" ;

        response = editConfig(m_server, m_clientInfo, requestXml, true);
        expResponse = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" + 
                "<data>\n" + 
                "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">\n" + 
                "<choice-case-test:default-choice-container>\n" + 
                "<choice-case-test:case1Leaf1>XYZ</choice-case-test:case1Leaf1>\n" + 
                "</choice-case-test:default-choice-container>\n" + 
                "<choice-case-test:mandatory-choice-control-leaf>defaultEnabled</choice-case-test:mandatory-choice-control-leaf>\n" + 
                "</choice-case-test:choice-container>\n" + 
                "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n" +
                "</data>\n" + 
                "</rpc-reply>";
        verifyGet(expResponse);
    }

    @Test
    public void testDefaultChoiceCase3() throws Exception {
        getModelNode();
        initialiseInterceptor();
        //case3Leaf2 is set, but other mandatory nodes are missing
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>"+  
                "<default-choice-container>"+
                "<case3Leaf2>test</case3Leaf2>"+               
                "</default-choice-container>"+
                "</choice-container>" ;

        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:default-choice-container/choice-case-test:case3Container1/choice-case-test:case3Container1Leaf1", "Missing mandatory node - case3Container1Leaf1", NetconfRpcErrorTag.DATA_MISSING);

        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>"+  
                "<default-choice-container>"+
                "<case3Leaf2>test</case3Leaf2>"+   
                "<case3Container1>"+
                "<case3Container1Leaf1>test</case3Container1Leaf1>"+   
                "</case3Container1>"+
                "</default-choice-container>"+
                "</choice-container>" ;

        response = editConfig(m_server, m_clientInfo, requestXml, false);
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:default-choice-container/choice-case-test:case3Container1/choice-case-test:case3Container1LeafList1", "Minimum elements required for case3Container1LeafList1 is 2.", NetconfRpcErrorTag.OPERATION_FAILED);

        //case3Leaf1 is set, case3Container1Leaf1 is set, case3Container1LeafList1 is set with only 1 entry which has min-element '2' constraint
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>"+  
                "<default-choice-container>"+
                "<case3Leaf2>test</case3Leaf2>"+   
                "<case3Container1>"+
                "<case3Container1Leaf1>test</case3Container1Leaf1>"+   
                "<case3Container1LeafList1>test1</case3Container1LeafList1>"+   
                "</case3Container1>"+
                "</default-choice-container>"+
                "</choice-container>" ;        
        response = editConfig(m_server, m_clientInfo, requestXml, false);
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:default-choice-container/choice-case-test:case3Container1/choice-case-test:case3Container1LeafList1", "Minimum elements required for case3Container1LeafList1 is 2.", NetconfRpcErrorTag.OPERATION_FAILED);

        //case3Leaf1 is set, case3Container1Leaf1 is set, case3Container1LeafList1 is set 
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>"+  
                "<default-choice-container>"+
                "<case3Leaf2>test</case3Leaf2>"+  
                "<case3Leaf1>test</case3Leaf1>"+   
                "<case3Container1>"+
                "<case3Container1Leaf1>test</case3Container1Leaf1>"+   
                "<case3Container1Leaf2>test</case3Container1Leaf2>"+   
                "<case3Container1LeafList1>test1</case3Container1LeafList1>"+   
                "<case3Container1LeafList1>test2</case3Container1LeafList1>"+   
                "</case3Container1>"+
                "</default-choice-container>"+
                "</choice-container>" ;        
        response = editConfig(m_server, m_clientInfo, requestXml, true);

        String expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" + 
                "<data>\n" + 
                "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">\n" + 
                "<choice-case-test:default-choice-container>\n" + 
                "<choice-case-test:case3Container1>\n" + 
                "<choice-case-test:case3Container1Leaf1>test</choice-case-test:case3Container1Leaf1>\n" + 
                "<choice-case-test:case3Container1Leaf2>test</choice-case-test:case3Container1Leaf2>\n" + 
                "<choice-case-test:case3Container1LeafList1>test1</choice-case-test:case3Container1LeafList1>\n" + 
                "<choice-case-test:case3Container1LeafList1>test2</choice-case-test:case3Container1LeafList1>\n" + 
                "</choice-case-test:case3Container1>\n" + 
                "<choice-case-test:case3Leaf2>test</choice-case-test:case3Leaf2>\n" + 
                "<choice-case-test:case3Leaf1>test</choice-case-test:case3Leaf1>\n" + 
                "</choice-case-test:default-choice-container>\n" + 
                "<choice-case-test:mandatory-choice-control-leaf>defaultEnabled</choice-case-test:mandatory-choice-control-leaf>\n" + 
                "</choice-case-test:choice-container>\n" + 
                "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n" +
                "</data>\n" + 
                "</rpc-reply>";

        verifyGet(expResponse);

        //delete case3Leaf1--non-mandatory leaf
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<default-choice-container>"+
                "<case3Leaf1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>"+    
                "</default-choice-container>"+
                "</choice-container>" ;

        editConfig(m_server, m_clientInfo, requestXml, true);        

        expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" + 
                "<data>\n" + 
                "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">\n" + 
                "<choice-case-test:default-choice-container>\n" + 
                "<choice-case-test:case3Container1>\n" + 
                "<choice-case-test:case3Container1Leaf1>test</choice-case-test:case3Container1Leaf1>\n" + 
                "<choice-case-test:case3Container1Leaf2>test</choice-case-test:case3Container1Leaf2>\n" + 
                "<choice-case-test:case3Container1LeafList1>test1</choice-case-test:case3Container1LeafList1>\n" + 
                "<choice-case-test:case3Container1LeafList1>test2</choice-case-test:case3Container1LeafList1>\n" + 
                "</choice-case-test:case3Container1>\n" + 
                "<choice-case-test:case3Leaf2>test</choice-case-test:case3Leaf2>\n" + 
                "</choice-case-test:default-choice-container>\n" + 
                "<choice-case-test:mandatory-choice-control-leaf>defaultEnabled</choice-case-test:mandatory-choice-control-leaf>\n" + 
                "</choice-case-test:choice-container>\n" + 
                "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n" +
                "</data>\n" + 
                "</rpc-reply>";

        verifyGet(expResponse);

        // delete non-mandatory leaf 'case3Container1Leaf2' under case3Container1
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<default-choice-container>"+
                "<case3Container1>" +
                "<case3Container1Leaf2 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>"+    
                "</case3Container1>"+
                "</default-choice-container>"+
                "</choice-container>" ;

        editConfig(m_server, m_clientInfo, requestXml, true); 

        expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" + 
                "<data>\n" + 
                "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">\n" + 
                "<choice-case-test:default-choice-container>\n" + 
                "<choice-case-test:case3Container1>\n" + 
                "<choice-case-test:case3Container1Leaf1>test</choice-case-test:case3Container1Leaf1>\n" + 
                "<choice-case-test:case3Container1LeafList1>test1</choice-case-test:case3Container1LeafList1>\n" + 
                "<choice-case-test:case3Container1LeafList1>test2</choice-case-test:case3Container1LeafList1>\n" + 
                "</choice-case-test:case3Container1>\n" + 
                "<choice-case-test:case3Leaf2>test</choice-case-test:case3Leaf2>\n" + 
                "</choice-case-test:default-choice-container>\n" + 
                "<choice-case-test:mandatory-choice-control-leaf>defaultEnabled</choice-case-test:mandatory-choice-control-leaf>\n" + 
                "</choice-case-test:choice-container>\n" + 
                "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n" +
                "</data>\n" + 
                "</rpc-reply>";

        verifyGet(expResponse);               
    }

    @Test
    public void testDefaultChoiceCase3DeleteScenario1() throws Exception {
        getModelNode();
        initialiseInterceptor();

        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>"+  
                "<default-choice-container>"+
                "<case3Leaf2>test</case3Leaf2>"+  
                "<case3Leaf1>test</case3Leaf1>"+   
                "<case3Container1>"+
                "<case3Container1Leaf1>test</case3Container1Leaf1>"+   
                "<case3Container1Leaf2>test</case3Container1Leaf2>"+   
                "<case3Container1LeafList1>test1</case3Container1LeafList1>"+   
                "<case3Container1LeafList1>test2</case3Container1LeafList1>"+   
                "</case3Container1>"+
                "</default-choice-container>"+
                "</choice-container>" ;        
        editConfig(m_server, m_clientInfo, requestXml, true);

        String expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" + 
                "<data>\n" + 
                "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">\n" + 
                "<choice-case-test:default-choice-container>\n" + 
                "<choice-case-test:case3Container1>\n" + 
                "<choice-case-test:case3Container1Leaf1>test</choice-case-test:case3Container1Leaf1>\n" + 
                "<choice-case-test:case3Container1Leaf2>test</choice-case-test:case3Container1Leaf2>\n" + 
                "<choice-case-test:case3Container1LeafList1>test1</choice-case-test:case3Container1LeafList1>\n" + 
                "<choice-case-test:case3Container1LeafList1>test2</choice-case-test:case3Container1LeafList1>\n" + 
                "</choice-case-test:case3Container1>\n" + 
                "<choice-case-test:case3Leaf2>test</choice-case-test:case3Leaf2>\n" + 
                "<choice-case-test:case3Leaf1>test</choice-case-test:case3Leaf1>\n" + 
                "</choice-case-test:default-choice-container>\n" + 
                "<choice-case-test:mandatory-choice-control-leaf>defaultEnabled</choice-case-test:mandatory-choice-control-leaf>\n" + 
                "</choice-case-test:choice-container>\n" + 
                "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n" +
                "</data>\n" + 
                "</rpc-reply>";
        verifyGet(expResponse);   

        //delete case3Leaf2, but fails as it is mandatory
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>"+  
                "<default-choice-container>"+
                "<case3Leaf2 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>"+  
                "</default-choice-container>"+
                "</choice-container>" ;        
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:default-choice-container/choice-case-test:case3Leaf2", "Missing mandatory node - case3Leaf2", NetconfRpcErrorTag.DATA_MISSING);
    }

    @Test
    public void testDefaultChoiceCase3DeleteScenario2() throws Exception {
        getModelNode();
        initialiseInterceptor();

        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>"+  
                "<default-choice-container>"+
                "<case3Leaf2>test</case3Leaf2>"+  
                "<case3Leaf1>test</case3Leaf1>"+   
                "<case3Container1>"+
                "<case3Container1Leaf1>test</case3Container1Leaf1>"+   
                "<case3Container1Leaf2>test</case3Container1Leaf2>"+   
                "<case3Container1LeafList1>test1</case3Container1LeafList1>"+   
                "<case3Container1LeafList1>test2</case3Container1LeafList1>"+   
                "</case3Container1>"+
                "</default-choice-container>"+
                "</choice-container>" ;        
        editConfig(m_server, m_clientInfo, requestXml, true);

        String expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" + 
                "<data>\n" + 
                "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">\n" + 
                "<choice-case-test:default-choice-container>\n" + 
                "<choice-case-test:case3Container1>\n" + 
                "<choice-case-test:case3Container1Leaf1>test</choice-case-test:case3Container1Leaf1>\n" + 
                "<choice-case-test:case3Container1Leaf2>test</choice-case-test:case3Container1Leaf2>\n" + 
                "<choice-case-test:case3Container1LeafList1>test1</choice-case-test:case3Container1LeafList1>\n" + 
                "<choice-case-test:case3Container1LeafList1>test2</choice-case-test:case3Container1LeafList1>\n" + 
                "</choice-case-test:case3Container1>\n" + 
                "<choice-case-test:case3Leaf2>test</choice-case-test:case3Leaf2>\n" + 
                "<choice-case-test:case3Leaf1>test</choice-case-test:case3Leaf1>\n" + 
                "</choice-case-test:default-choice-container>\n" + 
                "<choice-case-test:mandatory-choice-control-leaf>defaultEnabled</choice-case-test:mandatory-choice-control-leaf>\n" + 
                "</choice-case-test:choice-container>\n" + 
                "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n" +
                "</data>\n" + 
                "</rpc-reply>";
        verifyGet(expResponse);   

        //delete case3Container1 but fails as it has mandatory node inside it
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>"+  
                "<default-choice-container>"+
                "<case3Container1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>"+
                "</default-choice-container>"+
                "</choice-container>" ;        
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:default-choice-container/choice-case-test:case3Container1/choice-case-test:case3Container1Leaf1", "Missing mandatory node - case3Container1Leaf1", NetconfRpcErrorTag.DATA_MISSING);
    }

    @Test
    public void testDefaultChoiceCase3DeleteScenario3() throws Exception {
        getModelNode();
        initialiseInterceptor();

        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>"+  
                "<default-choice-container>"+
                "<case3Leaf2>test</case3Leaf2>"+  
                "<case3Container1>"+
                "<case3Container1Leaf1>test</case3Container1Leaf1>"+   
                "<case3Container1Leaf2>test</case3Container1Leaf2>"+   
                "<case3Container1LeafList1>test1</case3Container1LeafList1>"+   
                "<case3Container1LeafList1>test2</case3Container1LeafList1>"+   
                "</case3Container1>"+
                "</default-choice-container>"+
                "</choice-container>" ;        
        editConfig(m_server, m_clientInfo, requestXml, true);

        String expResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" + 
                "<data>\n" + 
                "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">\n" + 
                "<choice-case-test:default-choice-container>\n" + 
                "<choice-case-test:case3Container1>\n" + 
                "<choice-case-test:case3Container1Leaf1>test</choice-case-test:case3Container1Leaf1>\n" + 
                "<choice-case-test:case3Container1Leaf2>test</choice-case-test:case3Container1Leaf2>\n" + 
                "<choice-case-test:case3Container1LeafList1>test1</choice-case-test:case3Container1LeafList1>\n" + 
                "<choice-case-test:case3Container1LeafList1>test2</choice-case-test:case3Container1LeafList1>\n" + 
                "</choice-case-test:case3Container1>\n" + 
                "<choice-case-test:case3Leaf2>test</choice-case-test:case3Leaf2>\n" + 
                "</choice-case-test:default-choice-container>\n" + 
                "<choice-case-test:mandatory-choice-control-leaf>defaultEnabled</choice-case-test:mandatory-choice-control-leaf>\n" + 
                "</choice-case-test:choice-container>\n" + 
                "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n" +
                "</data>\n" + 
                "</rpc-reply>";
        verifyGet(expResponse);   

        //delete case3Container1 and case3Leaf2
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>"+  
                "<default-choice-container>"+
                "<case3Leaf2 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>"+
                "<case3Container1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>"+
                "</default-choice-container>"+
                "</choice-container>" ;   

        //it needs to succeed and fall back to default case
        editConfig(m_server, m_clientInfo, requestXml, true);
        expResponse = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" + 
                "<data>\n" + 
                "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">\n" + 
                "<choice-case-test:default-choice-container>\n" + 
                "<choice-case-test:case1Leaf1>XYZ</choice-case-test:case1Leaf1>\n" + 
                "</choice-case-test:default-choice-container>\n" + 
                "<choice-case-test:mandatory-choice-control-leaf>defaultEnabled</choice-case-test:mandatory-choice-control-leaf>\n" + 
                "</choice-case-test:choice-container>\n" + 
                "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n" +
                "</data>\n" + 
                "</rpc-reply>";
        verifyGet(expResponse);
    }

    @Test
    public void testDefaultChoiceCase4() throws Exception {
        getModelNode();
        initialiseInterceptor();
        //case4Leaf1 is set, but other mandatory nodes are missing
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>"+  
                "<default-choice-container>"+
                "<case4Leaf1>test</case4Leaf1>"+               
                "</default-choice-container>"+
                "</choice-container>" ;

        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
        List<NetconfRpcError> errors = response.getErrors();
        assertEquals(2, errors.size());
        checkErrors(errors.get(0), "/choice-case-test:choice-container/choice-case-test:default-choice-container/choice-case-test:case4List1", "Missing mandatory node - case4List1", NetconfRpcErrorTag.DATA_MISSING);
        checkErrors(errors.get(1), "/choice-case-test:choice-container/choice-case-test:default-choice-container/choice-case-test:case4Leaf2", "Missing mandatory node - case4Leaf2", NetconfRpcErrorTag.DATA_MISSING);

    }

    @Test
    public void testDefaultChoiceCase4_1() throws Exception {
        getModelNode();
        initialiseInterceptor();
        //case4Leaf1, case4Leaf2 is set, but other mandatory nodes are missing
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>" +
                "<default-choice-container>" +
                "<case4Leaf1>test</case4Leaf1>" +
                "<case4Leaf2>test</case4Leaf2>" +
                "</default-choice-container>" +
                "</choice-container>";;

        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:default-choice-container/choice-case-test:case4List1", "Missing mandatory node - case4List1", NetconfRpcErrorTag.DATA_MISSING);

    }

    @Test
    public void testDefaultChoiceCase4_2() throws Exception {
        getModelNode();
        initialiseInterceptor();
        //case4Leaf1, case4Leaf2 is set, but case4List1 fails with mandatory node missing
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>" +
                "<default-choice-container>" +
                "<case4Leaf1>test</case4Leaf1>" +
                "<case4Leaf2>test</case4Leaf2>" +
                "<case4List1>" +
                "<case4List1Leaf1>list1</case4List1Leaf1>" +
                "<case4List1Leaf2>test</case4List1Leaf2>" +
                "</case4List1>" +
                "<case4List1>" +
                "<case4List1Leaf1>list2</case4List1Leaf1>" +
                "<case4List1Leaf3>test</case4List1Leaf3>" +
                "<case4List1Container1>" +
                "<case4List1Container1Leaf1>test</case4List1Container1Leaf1>" +
                "</case4List1Container1>" +
                "</case4List1>" +
                "</default-choice-container>" +
                "</choice-container>";;

        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:default-choice-container/choice-case-test:case4List1[choice-case-test:case4List1Leaf1='list1']/choice-case-test:case4List1Container1/choice-case-test:case4List1Container1Leaf1", "Missing mandatory node - case4List1Container1Leaf1", NetconfRpcErrorTag.DATA_MISSING);

    }

    @Test
    public void testDefaultChoiceCase4_3() throws Exception {
        getModelNode();
        initialiseInterceptor();

        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>" +
                "<default-choice-container>" +
                "<case4Leaf1>test</case4Leaf1>" +
                "<case4Leaf2>test</case4Leaf2>" +
                "<case4List1>" +
                "<case4List1Leaf1>list1</case4List1Leaf1>" +
                "<case4List1Leaf2>test</case4List1Leaf2>" +
                "<case4List1Container1>" +
                "<case4List1Container1Leaf1>test</case4List1Container1Leaf1>" +
                "</case4List1Container1>" +
                "</case4List1>" +
                "<case4List1>" +
                "<case4List1Leaf1>list2</case4List1Leaf1>" +
                "<case4List1Leaf3>test</case4List1Leaf3>" +
                "<case4List1Container1>" +
                "<case4List1Container1Leaf1>test</case4List1Container1Leaf1>" +
                "</case4List1Container1>" +
                "</case4List1>" +
                "</default-choice-container>" +
                "</choice-container>";;

        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:default-choice-container/choice-case-test:case4List1[choice-case-test:case4List1Leaf1='list2']/choice-case-test:case4List1Leaf2", "Missing mandatory node - case4List1Leaf2", NetconfRpcErrorTag.DATA_MISSING);

    }

    @Test
    public void testDefaultChoiceCase4_4() throws Exception {
        getModelNode();
        initialiseInterceptor();

        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>" +
                "<default-choice-container>" +
                "<case4Leaf1>test</case4Leaf1>" +
                "<case4Leaf2>test</case4Leaf2>" +
                "<case4List1>" +
                "<case4List1Leaf1>list1</case4List1Leaf1>" +
                "<case4List1Leaf2>test</case4List1Leaf2>" +
                "<case4List1Container1>" +
                "<case4List1Container1Leaf1>test</case4List1Container1Leaf1>" +
                "</case4List1Container1>" +
                "</case4List1>" +
                "<case4List1>" +
                "<case4List1Leaf1>list2</case4List1Leaf1>" +
                "<case4List1Leaf2>test</case4List1Leaf2>" +
                "<case4List1Leaf3>test</case4List1Leaf3>" +
                "<case4List1Container1>" +
                "<case4List1Container1Leaf1>test</case4List1Container1Leaf1>" +
                "</case4List1Container1>" +
                "</case4List1>" +
                "</default-choice-container>" +
                "</choice-container>";;

        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, true);

        String expResponse = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" + 
                "<data>\n" + 
                "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">\n" + 
                "<choice-case-test:default-choice-container>\n" + 
                "<choice-case-test:case4Leaf1>test</choice-case-test:case4Leaf1>\n" + 
                "<choice-case-test:case4Leaf2>test</choice-case-test:case4Leaf2>\n" + 
                "<choice-case-test:case4List1>\n" + 
                "<choice-case-test:case4List1Leaf1>list1</choice-case-test:case4List1Leaf1>\n" + 
                "<choice-case-test:case4List1Leaf2>test</choice-case-test:case4List1Leaf2>\n" + 
                "<choice-case-test:case4List1Container1>"+
                "<choice-case-test:case4List1Container1Leaf1>test</choice-case-test:case4List1Container1Leaf1>"+
                "</choice-case-test:case4List1Container1>"+
                "</choice-case-test:case4List1>\n" + 
                "<choice-case-test:case4List1>\n" + 
                "<choice-case-test:case4List1Leaf1>list2</choice-case-test:case4List1Leaf1>\n" + 
                "<choice-case-test:case4List1Leaf2>test</choice-case-test:case4List1Leaf2>\n" + 
                "<choice-case-test:case4List1Leaf3>test</choice-case-test:case4List1Leaf3>\n" + 
                "<choice-case-test:case4List1Container1>"+
                "<choice-case-test:case4List1Container1Leaf1>test</choice-case-test:case4List1Container1Leaf1>"+
                "</choice-case-test:case4List1Container1>"+
                "</choice-case-test:case4List1>\n" + 
                "</choice-case-test:default-choice-container>\n" + 
                "<choice-case-test:mandatory-choice-control-leaf>defaultEnabled</choice-case-test:mandatory-choice-control-leaf>\n" + 
                "</choice-case-test:choice-container>\n" + 
                "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n" +
                "</data>\n" + 
                "</rpc-reply>";        
        verifyGet(expResponse);

        // delete case4List1Container1 which has mandatory node
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>"+  
                "<default-choice-container>"+  
                "<case4List1>"+
                "<case4List1Leaf1>list1</case4List1Leaf1>" + 
                "<case4List1Container1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>"+
                "</case4List1>"+  
                "</default-choice-container>"+
                "</choice-container>" ;

        response = editConfig(m_server, m_clientInfo, requestXml, false);
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:default-choice-container/choice-case-test:case4List1[choice-case-test:case4List1Leaf1='list1']/choice-case-test:case4List1Container1/choice-case-test:case4List1Container1Leaf1", "Missing mandatory node - case4List1Container1Leaf1", NetconfRpcErrorTag.DATA_MISSING);

    }

    @Test
    public void testDefaultChoiceCase4DeleteScenario1() throws Exception {
        getModelNode();
        initialiseInterceptor();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>"+  
                "<default-choice-container>"+
                "<case4Leaf1>test</case4Leaf1>"+  
                "<case4Leaf2>test</case4Leaf2>"+     
                "<case4List1>"+
                "<case4List1Leaf1>list1</case4List1Leaf1>"+  
                "<case4List1Leaf2>test</case4List1Leaf2>"+  
                "<case4List1Container1>"+
                "<case4List1Container1Leaf1>test</case4List1Container1Leaf1>"+  
                "</case4List1Container1>"+
                "</case4List1>"+  
                "<case4List1>"+
                "<case4List1Leaf1>list2</case4List1Leaf1>"+ 
                "<case4List1Leaf2>test</case4List1Leaf2>"+  
                "<case4List1Leaf3>test</case4List1Leaf3>"+  
                "<case4List1Container1>"+
                "<case4List1Container1Leaf1>test</case4List1Container1Leaf1>"+  
                "</case4List1Container1>"+
                "</case4List1>"+  
                "</default-choice-container>"+
                "</choice-container>" ;

        editConfig(m_server, m_clientInfo, requestXml, true);

        String expResponse = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" + 
                "<data>\n" + 
                "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">\n" + 
                "<choice-case-test:default-choice-container>\n" + 
                "<choice-case-test:case4Leaf1>test</choice-case-test:case4Leaf1>\n" + 
                "<choice-case-test:case4Leaf2>test</choice-case-test:case4Leaf2>\n" + 
                "<choice-case-test:case4List1>\n" + 
                "<choice-case-test:case4List1Leaf1>list1</choice-case-test:case4List1Leaf1>\n" + 
                "<choice-case-test:case4List1Leaf2>test</choice-case-test:case4List1Leaf2>\n" + 
                "<choice-case-test:case4List1Container1>"+
                "<choice-case-test:case4List1Container1Leaf1>test</choice-case-test:case4List1Container1Leaf1>"+
                "</choice-case-test:case4List1Container1>"+
                "</choice-case-test:case4List1>\n" + 
                "<choice-case-test:case4List1>\n" + 
                "<choice-case-test:case4List1Leaf1>list2</choice-case-test:case4List1Leaf1>\n" + 
                "<choice-case-test:case4List1Leaf2>test</choice-case-test:case4List1Leaf2>\n" + 
                "<choice-case-test:case4List1Leaf3>test</choice-case-test:case4List1Leaf3>\n" + 
                "<choice-case-test:case4List1Container1>"+
                "<choice-case-test:case4List1Container1Leaf1>test</choice-case-test:case4List1Container1Leaf1>"+
                "</choice-case-test:case4List1Container1>"+
                "</choice-case-test:case4List1>\n" + 
                "</choice-case-test:default-choice-container>\n" + 
                "<choice-case-test:mandatory-choice-control-leaf>defaultEnabled</choice-case-test:mandatory-choice-control-leaf>\n" + 
                "</choice-case-test:choice-container>\n" + 
                "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n" +
                "</data>\n" + 
                "</rpc-reply>";        
        verifyGet(expResponse);

        //delete case4List1Leaf2 mandatory node, should fail
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>"+  
                "<default-choice-container>"+  
                "<case4List1>"+
                "<case4List1Leaf1>list1</case4List1Leaf1>" + 
                "<case4List1Leaf2 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>"+
                "</case4List1>"+  
                "</default-choice-container>"+
                "</choice-container>" ;

        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:default-choice-container/choice-case-test:case4List1[choice-case-test:case4List1Leaf1='list1']/choice-case-test:case4List1Leaf2", "Missing mandatory node - case4List1Leaf2", NetconfRpcErrorTag.DATA_MISSING);  
    }

    @Test
    public void testDefaultChoiceCase4DeleteScenario2() throws Exception {
        getModelNode();
        initialiseInterceptor();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>"+  
                "<default-choice-container>"+
                "<case4Leaf1>test</case4Leaf1>"+  
                "<case4Leaf2>test</case4Leaf2>"+     
                "<case4List1>"+
                "<case4List1Leaf1>list1</case4List1Leaf1>"+  
                "<case4List1Leaf2>test</case4List1Leaf2>"+  
                "<case4List1Container1>"+
                "<case4List1Container1Leaf1>test</case4List1Container1Leaf1>"+  
                "</case4List1Container1>"+
                "</case4List1>"+  
                "<case4List1>"+
                "<case4List1Leaf1>list2</case4List1Leaf1>"+ 
                "<case4List1Leaf2>test</case4List1Leaf2>"+  
                "<case4List1Leaf3>test</case4List1Leaf3>"+  
                "<case4List1Container1>"+
                "<case4List1Container1Leaf1>test</case4List1Container1Leaf1>"+  
                "</case4List1Container1>"+
                "</case4List1>"+  
                "</default-choice-container>"+
                "</choice-container>" ;

        editConfig(m_server, m_clientInfo, requestXml, true);

        String expResponse = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" + 
                "<data>\n" + 
                "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">\n" + 
                "<choice-case-test:default-choice-container>\n" + 
                "<choice-case-test:case4Leaf1>test</choice-case-test:case4Leaf1>\n" + 
                "<choice-case-test:case4Leaf2>test</choice-case-test:case4Leaf2>\n" + 
                "<choice-case-test:case4List1>\n" + 
                "<choice-case-test:case4List1Leaf1>list1</choice-case-test:case4List1Leaf1>\n" + 
                "<choice-case-test:case4List1Leaf2>test</choice-case-test:case4List1Leaf2>\n" + 
                "<choice-case-test:case4List1Container1>"+
                "<choice-case-test:case4List1Container1Leaf1>test</choice-case-test:case4List1Container1Leaf1>"+
                "</choice-case-test:case4List1Container1>"+
                "</choice-case-test:case4List1>\n" + 
                "<choice-case-test:case4List1>\n" + 
                "<choice-case-test:case4List1Leaf1>list2</choice-case-test:case4List1Leaf1>\n" + 
                "<choice-case-test:case4List1Leaf2>test</choice-case-test:case4List1Leaf2>\n" + 
                "<choice-case-test:case4List1Leaf3>test</choice-case-test:case4List1Leaf3>\n" + 
                "<choice-case-test:case4List1Container1>"+
                "<choice-case-test:case4List1Container1Leaf1>test</choice-case-test:case4List1Container1Leaf1>"+
                "</choice-case-test:case4List1Container1>"+
                "</choice-case-test:case4List1>\n" + 
                "</choice-case-test:default-choice-container>\n" + 
                "<choice-case-test:mandatory-choice-control-leaf>defaultEnabled</choice-case-test:mandatory-choice-control-leaf>\n" + 
                "</choice-case-test:choice-container>\n" + 
                "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n" +
                "</data>\n" + 
                "</rpc-reply>";        
        verifyGet(expResponse);

        //delete case4List1 entry1, should fail with min-element constraint
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>"+  
                "<default-choice-container>"+  
                "<case4List1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\">"+
                "<case4List1Leaf1>list1</case4List1Leaf1>" + 
                "</case4List1>"+  
                "</default-choice-container>"+
                "</choice-container>" ;

        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:default-choice-container/choice-case-test:case4List1", "Minimum elements required for case4List1 is 2.", NetconfRpcErrorTag.OPERATION_FAILED);  
    }

    @Test
    public void testDefaultChoiceCase4DeleteScenario3() throws Exception {
        getModelNode();
        initialiseInterceptor();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>"+  
                "<default-choice-container>"+
                "<case4Leaf1>test</case4Leaf1>"+  
                "<case4Leaf2>test</case4Leaf2>"+     
                "<case4List1>"+
                "<case4List1Leaf1>list1</case4List1Leaf1>"+  
                "<case4List1Leaf2>test</case4List1Leaf2>"+  
                "<case4List1Container1>"+
                "<case4List1Container1Leaf1>test</case4List1Container1Leaf1>"+  
                "</case4List1Container1>"+
                "</case4List1>"+  
                "<case4List1>"+
                "<case4List1Leaf1>list2</case4List1Leaf1>"+ 
                "<case4List1Leaf2>test</case4List1Leaf2>"+  
                "<case4List1Leaf3>test</case4List1Leaf3>"+  
                "<case4List1Container1>"+
                "<case4List1Container1Leaf1>test</case4List1Container1Leaf1>"+  
                "</case4List1Container1>"+
                "</case4List1>"+  
                "</default-choice-container>"+
                "</choice-container>" ;

        editConfig(m_server, m_clientInfo, requestXml, true);

        String expResponse = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" + 
                "<data>\n" + 
                "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">\n" + 
                "<choice-case-test:default-choice-container>\n" + 
                "<choice-case-test:case4Leaf1>test</choice-case-test:case4Leaf1>\n" + 
                "<choice-case-test:case4Leaf2>test</choice-case-test:case4Leaf2>\n" + 
                "<choice-case-test:case4List1>\n" + 
                "<choice-case-test:case4List1Leaf1>list1</choice-case-test:case4List1Leaf1>\n" + 
                "<choice-case-test:case4List1Leaf2>test</choice-case-test:case4List1Leaf2>\n" + 
                "<choice-case-test:case4List1Container1>"+
                "<choice-case-test:case4List1Container1Leaf1>test</choice-case-test:case4List1Container1Leaf1>"+
                "</choice-case-test:case4List1Container1>"+
                "</choice-case-test:case4List1>\n" + 
                "<choice-case-test:case4List1>\n" + 
                "<choice-case-test:case4List1Leaf1>list2</choice-case-test:case4List1Leaf1>\n" + 
                "<choice-case-test:case4List1Leaf2>test</choice-case-test:case4List1Leaf2>\n" + 
                "<choice-case-test:case4List1Leaf3>test</choice-case-test:case4List1Leaf3>\n" + 
                "<choice-case-test:case4List1Container1>"+
                "<choice-case-test:case4List1Container1Leaf1>test</choice-case-test:case4List1Container1Leaf1>"+
                "</choice-case-test:case4List1Container1>"+
                "</choice-case-test:case4List1>\n" + 
                "</choice-case-test:default-choice-container>\n" + 
                "<choice-case-test:mandatory-choice-control-leaf>defaultEnabled</choice-case-test:mandatory-choice-control-leaf>\n" + 
                "</choice-case-test:choice-container>\n" + 
                "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n" +
                "</data>\n" + 
                "</rpc-reply>";        
        verifyGet(expResponse);

        //delete all case4 nodes
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>"+  
                "<default-choice-container>"+  
                "<case4List1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\">"+
                "<case4List1Leaf1>list1</case4List1Leaf1>" + 
                "</case4List1>"+  
                "<case4List1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\">"+
                "<case4List1Leaf1>list2</case4List1Leaf1>" + 
                "</case4List1>"+  
                "<case4Leaf1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>"+
                "<case4Leaf2 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>"+
                "</default-choice-container>"+
                "</choice-container>" ;

        //it needs to succeed and fall back to default case
        editConfig(m_server, m_clientInfo, requestXml, true);
        expResponse = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" + 
                "<data>\n" + 
                "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">\n" + 
                "<choice-case-test:default-choice-container>\n" + 
                "<choice-case-test:case1Leaf1>XYZ</choice-case-test:case1Leaf1>\n" + 
                "</choice-case-test:default-choice-container>\n" + 
                "<choice-case-test:mandatory-choice-control-leaf>defaultEnabled</choice-case-test:mandatory-choice-control-leaf>\n" + 
                "</choice-case-test:choice-container>\n" + 
                "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n" +
                "</data>\n" + 
                "</rpc-reply>";
        verifyGet(expResponse);
    }

    @Test
    public void testDefaultChoiceCase4DeleteScenario5() throws Exception {
        getModelNode();
        initialiseInterceptor();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>"+  
                "<default-choice-container>"+
                "<case4Leaf1>test</case4Leaf1>"+  
                "<case4Leaf2>test</case4Leaf2>"+     
                "<case4List1>"+
                "<case4List1Leaf1>list1</case4List1Leaf1>"+  
                "<case4List1Leaf2>test</case4List1Leaf2>"+  
                "<case4List1Container1>"+
                "<case4List1Container1Leaf1>test</case4List1Container1Leaf1>"+  
                "</case4List1Container1>"+
                "</case4List1>"+  
                "<case4List1>"+
                "<case4List1Leaf1>list2</case4List1Leaf1>"+ 
                "<case4List1Leaf2>test</case4List1Leaf2>"+  
                "<case4List1Leaf3>test</case4List1Leaf3>"+  
                "<case4List1Container1>"+
                "<case4List1Container1Leaf1>test</case4List1Container1Leaf1>"+  
                "</case4List1Container1>"+
                "</case4List1>"+  
                "</default-choice-container>"+
                "</choice-container>" ;

        editConfig(m_server, m_clientInfo, requestXml, true);

        // delete case4Leaf2 mandatory node, should fail
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>"+  
                "<default-choice-container>"+  
                "<case4Leaf2 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>"+
                "</default-choice-container>"+
                "</choice-container>" ;

        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:default-choice-container/choice-case-test:case4Leaf2", "Missing mandatory node - case4Leaf2", NetconfRpcErrorTag.DATA_MISSING);
    }

    @Test
    public void testDefaultChoiceCase5() throws Exception {
        getModelNode();
        initialiseInterceptor();

        // set case5Leaf1, fail for missing mandatory nodes
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>"+  
                "<default-choice-container>"+
                "<case5Leaf1>test</case5Leaf1>"+   
                "</default-choice-container>"+
                "</choice-container>" ;

        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
        checkErrors(response.getErrors().get(1), "/choice-case-test:choice-container/choice-case-test:default-choice-container/choice-case-test:case5Leaf2", "Missing mandatory node - case5Leaf2", NetconfRpcErrorTag.DATA_MISSING);  

        // set case5Leaf1, case5Leaf2, fail for missing mandatory nodes
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>"+  
                "<default-choice-container>"+
                "<case5Leaf1>test</case5Leaf1>"+   
                "<case5Leaf2>test</case5Leaf2>"+   
                "</default-choice-container>"+
                "</choice-container>" ;

        response = editConfig(m_server, m_clientInfo, requestXml, false);
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:default-choice-container/choice-case-test:case5List1", "Missing mandatory node - case5List1", NetconfRpcErrorTag.DATA_MISSING);  

        // set case5Leaf1, case5Leaf2, case5List1 ,fail for min-elements constraints on case5List1
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>"+  
                "<default-choice-container>"+
                "<case5Leaf1>test</case5Leaf1>"+   
                "<case5Leaf2>test</case5Leaf2>"+  
                "<case5List1>"+
                "<case5List1Leaf1>list1</case5List1Leaf1>"+  
                "<case5List1Leaf2>test</case5List1Leaf2>"+  
                "</case5List1>"+ 
                "</default-choice-container>"+
                "</choice-container>" ;

        response = editConfig(m_server, m_clientInfo, requestXml, false);
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:default-choice-container/choice-case-test:case5List1", "Minimum elements required for case5List1 is 2.", NetconfRpcErrorTag.OPERATION_FAILED);   

        // set case5Leaf1, case5Leaf2, case5List1 ,fails for missing mandatory node on case5Container1
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>"+  
                "<default-choice-container>"+
                "<case5Leaf1>test</case5Leaf1>"+   
                "<case5Leaf2>test</case5Leaf2>"+  
                "<case5List1>"+
                "<case5List1Leaf1>list1</case5List1Leaf1>"+  
                "<case5List1Leaf2>test</case5List1Leaf2>"+  
                "</case5List1>"+ 
                "<case5List1>"+
                "<case5List1Leaf1>list2</case5List1Leaf1>"+  
                "<case5List1Leaf2>test</case5List1Leaf2>"+  
                "</case5List1>"+ 
                "</default-choice-container>"+
                "</choice-container>" ;

        response = editConfig(m_server, m_clientInfo, requestXml, false);
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:default-choice-container/choice-case-test:case5Container1/choice-case-test:case5Container1Leaf1", "Missing mandatory node - case5Container1Leaf1", NetconfRpcErrorTag.DATA_MISSING);  

        // set case5Leaf1, case5Leaf2, case5List1 ,fails for min-element constrait on case5Container1leaflist1
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>"+  
                "<default-choice-container>"+
                "<case5Leaf1>test</case5Leaf1>"+   
                "<case5Leaf2>test</case5Leaf2>"+  
                "<case5List1>"+
                "<case5List1Leaf1>list1</case5List1Leaf1>"+  
                "<case5List1Leaf2>test</case5List1Leaf2>"+  
                "</case5List1>"+ 
                "<case5List1>"+
                "<case5List1Leaf1>list2</case5List1Leaf1>"+  
                "<case5List1Leaf2>test</case5List1Leaf2>"+  
                "</case5List1>"+ 
                "<case5Container1>"+
                "<case5Container1Leaf1>test</case5Container1Leaf1>"+   
                "</case5Container1>"+ 
                "</default-choice-container>"+
                "</choice-container>" ;

        response = editConfig(m_server, m_clientInfo, requestXml, false);
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:default-choice-container/choice-case-test:case5Container1/choice-case-test:case5Container1LeafList1", "Minimum elements required for case5Container1LeafList1 is 2.", NetconfRpcErrorTag.OPERATION_FAILED);  

        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>"+  
                "<default-choice-container>"+
                "<case5Leaf1>test</case5Leaf1>"+   
                "<case5Leaf2>test</case5Leaf2>"+  
                "<case5List1>"+
                "<case5List1Leaf1>list1</case5List1Leaf1>"+  
                "<case5List1Leaf2>test</case5List1Leaf2>"+  
                "</case5List1>"+ 
                "<case5List1>"+
                "<case5List1Leaf1>list2</case5List1Leaf1>"+  
                "<case5List1Leaf2>test</case5List1Leaf2>"+  
                "</case5List1>"+ 
                "<case5Container1>"+
                "<case5Container1Leaf1>test</case5Container1Leaf1>"+  
                "<case5Container1LeafList1>entry1</case5Container1LeafList1>"+  
                "<case5Container1LeafList1>entry2</case5Container1LeafList1>"+  
                "</case5Container1>"+ 
                "</default-choice-container>"+
                "</choice-container>" ;

        response = editConfig(m_server, m_clientInfo, requestXml, true); 

        String expResponse = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" + 
                "<data>\n" + 
                "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">\n" + 
                "<choice-case-test:default-choice-container>\n" + 
                "<choice-case-test:case5Container1>\n" + 
                "<choice-case-test:case5Container1Leaf1>test</choice-case-test:case5Container1Leaf1>\n" + 
                "<choice-case-test:case5Container1LeafList1>entry1</choice-case-test:case5Container1LeafList1>\n" + 
                "<choice-case-test:case5Container1LeafList1>entry2</choice-case-test:case5Container1LeafList1>\n" + 
                "</choice-case-test:case5Container1>\n" + 
                "<choice-case-test:case5Leaf1>test</choice-case-test:case5Leaf1>\n" + 
                "<choice-case-test:case5Leaf2>test</choice-case-test:case5Leaf2>\n" + 
                "<choice-case-test:case5List1>\n" + 
                "<choice-case-test:case5List1Leaf1>list1</choice-case-test:case5List1Leaf1>\n" + 
                "<choice-case-test:case5List1Leaf2>test</choice-case-test:case5List1Leaf2>\n" + 
                "</choice-case-test:case5List1>\n" + 
                "<choice-case-test:case5List1>\n" + 
                "<choice-case-test:case5List1Leaf1>list2</choice-case-test:case5List1Leaf1>\n" + 
                "<choice-case-test:case5List1Leaf2>test</choice-case-test:case5List1Leaf2>\n" + 
                "</choice-case-test:case5List1>\n" + 
                "</choice-case-test:default-choice-container>\n" + 
                "<choice-case-test:mandatory-choice-control-leaf>defaultEnabled</choice-case-test:mandatory-choice-control-leaf>\n" + 
                "</choice-case-test:choice-container>\n" + 
                "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n" +
                "</data>\n" + 
                "</rpc-reply>\n" + 
                "";
        verifyGet(expResponse);
    }

    @Test
    public void testDefaultChoiceCase5DeleteScenario1() throws Exception {
        getModelNode();
        initialiseInterceptor();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>"+  
                "<default-choice-container>"+
                "<case5Leaf1>test</case5Leaf1>"+   
                "<case5Leaf2>test</case5Leaf2>"+  
                "<case5List1>"+
                "<case5List1Leaf1>list1</case5List1Leaf1>"+  
                "<case5List1Leaf2>test</case5List1Leaf2>"+  
                "</case5List1>"+ 
                "<case5List1>"+
                "<case5List1Leaf1>list2</case5List1Leaf1>"+  
                "<case5List1Leaf2>test</case5List1Leaf2>"+  
                "</case5List1>"+ 
                "<case5Container1>"+
                "<case5Container1Leaf1>test</case5Container1Leaf1>"+  
                "<case5Container1LeafList1>entry1</case5Container1LeafList1>"+  
                "<case5Container1LeafList1>entry2</case5Container1LeafList1>"+  
                "</case5Container1>"+ 
                "</default-choice-container>"+
                "</choice-container>" ;

        editConfig(m_server, m_clientInfo, requestXml, true);
        String expResponse = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" + 
                "<data>\n" + 
                "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">\n" + 
                "<choice-case-test:default-choice-container>\n" + 
                "<choice-case-test:case5Container1>\n" + 
                "<choice-case-test:case5Container1Leaf1>test</choice-case-test:case5Container1Leaf1>\n" + 
                "<choice-case-test:case5Container1LeafList1>entry1</choice-case-test:case5Container1LeafList1>\n" + 
                "<choice-case-test:case5Container1LeafList1>entry2</choice-case-test:case5Container1LeafList1>\n" + 
                "</choice-case-test:case5Container1>\n" + 
                "<choice-case-test:case5Leaf1>test</choice-case-test:case5Leaf1>\n" + 
                "<choice-case-test:case5Leaf2>test</choice-case-test:case5Leaf2>\n" + 
                "<choice-case-test:case5List1>\n" + 
                "<choice-case-test:case5List1Leaf1>list1</choice-case-test:case5List1Leaf1>\n" + 
                "<choice-case-test:case5List1Leaf2>test</choice-case-test:case5List1Leaf2>\n" + 
                "</choice-case-test:case5List1>\n" + 
                "<choice-case-test:case5List1>\n" + 
                "<choice-case-test:case5List1Leaf1>list2</choice-case-test:case5List1Leaf1>\n" + 
                "<choice-case-test:case5List1Leaf2>test</choice-case-test:case5List1Leaf2>\n" + 
                "</choice-case-test:case5List1>\n" + 
                "</choice-case-test:default-choice-container>\n" + 
                "<choice-case-test:mandatory-choice-control-leaf>defaultEnabled</choice-case-test:mandatory-choice-control-leaf>\n" + 
                "</choice-case-test:choice-container>\n" + 
                "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n" +
                "</data>\n" + 
                "</rpc-reply>\n" + 
                "";
        verifyGet(expResponse);

        //delete case5Leaf1 -->non-mandatory leaf
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>"+  
                "<default-choice-container>"+
                "<case5Leaf1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>"+   
                "</default-choice-container>"+
                "</choice-container>" ;

        editConfig(m_server, m_clientInfo, requestXml, true);
        expResponse = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" + 
                "<data>\n" + 
                "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">\n" + 
                "<choice-case-test:default-choice-container>\n" + 
                "<choice-case-test:case5Container1>\n" + 
                "<choice-case-test:case5Container1Leaf1>test</choice-case-test:case5Container1Leaf1>\n" + 
                "<choice-case-test:case5Container1LeafList1>entry1</choice-case-test:case5Container1LeafList1>\n" + 
                "<choice-case-test:case5Container1LeafList1>entry2</choice-case-test:case5Container1LeafList1>\n" + 
                "</choice-case-test:case5Container1>\n" + 
                "<choice-case-test:case5Leaf2>test</choice-case-test:case5Leaf2>\n" + 
                "<choice-case-test:case5List1>\n" + 
                "<choice-case-test:case5List1Leaf1>list1</choice-case-test:case5List1Leaf1>\n" + 
                "<choice-case-test:case5List1Leaf2>test</choice-case-test:case5List1Leaf2>\n" + 
                "</choice-case-test:case5List1>\n" + 
                "<choice-case-test:case5List1>\n" + 
                "<choice-case-test:case5List1Leaf1>list2</choice-case-test:case5List1Leaf1>\n" + 
                "<choice-case-test:case5List1Leaf2>test</choice-case-test:case5List1Leaf2>\n" + 
                "</choice-case-test:case5List1>\n" + 
                "</choice-case-test:default-choice-container>\n" + 
                "<choice-case-test:mandatory-choice-control-leaf>defaultEnabled</choice-case-test:mandatory-choice-control-leaf>\n" + 
                "</choice-case-test:choice-container>\n" + 
                "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n" +
                "</data>\n" + 
                "</rpc-reply>\n" + 
                "";
        verifyGet(expResponse);

        //delete case5Leaf2 -->mandatory leaf
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>"+  
                "<default-choice-container>"+
                "<case5Leaf2 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>"+   
                "</default-choice-container>"+
                "</choice-container>" ;

        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:default-choice-container/choice-case-test:case5Leaf2", "Missing mandatory node - case5Leaf2", NetconfRpcErrorTag.DATA_MISSING);  
    }

    @Test
    public void testDefaultChoiceCase5DeleteScenario2() throws Exception {
        getModelNode();
        initialiseInterceptor();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>"+  
                "<default-choice-container>"+
                "<case5Leaf1>test</case5Leaf1>"+   
                "<case5Leaf2>test</case5Leaf2>"+  
                "<case5List1>"+
                "<case5List1Leaf1>list1</case5List1Leaf1>"+  
                "<case5List1Leaf2>test</case5List1Leaf2>"+  
                "</case5List1>"+ 
                "<case5List1>"+
                "<case5List1Leaf1>list2</case5List1Leaf1>"+  
                "<case5List1Leaf2>test</case5List1Leaf2>"+  
                "</case5List1>"+ 
                "<case5Container1>"+
                "<case5Container1Leaf1>test</case5Container1Leaf1>"+  
                "<case5Container1LeafList1>entry1</case5Container1LeafList1>"+  
                "<case5Container1LeafList1>entry2</case5Container1LeafList1>"+  
                "</case5Container1>"+ 
                "</default-choice-container>"+
                "</choice-container>" ;

        editConfig(m_server, m_clientInfo, requestXml, true);
        String expResponse = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" + 
                "<data>\n" + 
                "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">\n" + 
                "<choice-case-test:default-choice-container>\n" + 
                "<choice-case-test:case5Container1>\n" + 
                "<choice-case-test:case5Container1Leaf1>test</choice-case-test:case5Container1Leaf1>\n" + 
                "<choice-case-test:case5Container1LeafList1>entry1</choice-case-test:case5Container1LeafList1>\n" + 
                "<choice-case-test:case5Container1LeafList1>entry2</choice-case-test:case5Container1LeafList1>\n" + 
                "</choice-case-test:case5Container1>\n" + 
                "<choice-case-test:case5Leaf1>test</choice-case-test:case5Leaf1>\n" + 
                "<choice-case-test:case5Leaf2>test</choice-case-test:case5Leaf2>\n" + 
                "<choice-case-test:case5List1>\n" + 
                "<choice-case-test:case5List1Leaf1>list1</choice-case-test:case5List1Leaf1>\n" + 
                "<choice-case-test:case5List1Leaf2>test</choice-case-test:case5List1Leaf2>\n" + 
                "</choice-case-test:case5List1>\n" + 
                "<choice-case-test:case5List1>\n" + 
                "<choice-case-test:case5List1Leaf1>list2</choice-case-test:case5List1Leaf1>\n" + 
                "<choice-case-test:case5List1Leaf2>test</choice-case-test:case5List1Leaf2>\n" + 
                "</choice-case-test:case5List1>\n" + 
                "</choice-case-test:default-choice-container>\n" + 
                "<choice-case-test:mandatory-choice-control-leaf>defaultEnabled</choice-case-test:mandatory-choice-control-leaf>\n" + 
                "</choice-case-test:choice-container>\n" + 
                "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n" +
                "</data>\n" + 
                "</rpc-reply>\n" + 
                "";
        verifyGet(expResponse);

        // delete an entry in list with min-element constraint
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>"+  
                "<default-choice-container>"+
                "<case5List1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\">"+
                "<case5List1Leaf1>list1</case5List1Leaf1>"+  
                "</case5List1>"+ 
                "</default-choice-container>"+
                "</choice-container>" ;

        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:default-choice-container/choice-case-test:case5List1", "Minimum elements required for case5List1 is 2.", NetconfRpcErrorTag.OPERATION_FAILED);  
    }

    @Test
    public void testDefaultChoiceCase5DeleteScenario3() throws Exception {
        getModelNode();
        initialiseInterceptor();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>"+  
                "<default-choice-container>"+
                "<case5Leaf1>test</case5Leaf1>"+   
                "<case5Leaf2>test</case5Leaf2>"+  
                "<case5List1>"+
                "<case5List1Leaf1>list1</case5List1Leaf1>"+  
                "<case5List1Leaf2>test</case5List1Leaf2>"+  
                "</case5List1>"+ 
                "<case5List1>"+
                "<case5List1Leaf1>list2</case5List1Leaf1>"+  
                "<case5List1Leaf2>test</case5List1Leaf2>"+  
                "</case5List1>"+ 
                "<case5Container1>"+
                "<case5Container1Leaf1>test</case5Container1Leaf1>"+  
                "<case5Container1LeafList1>entry1</case5Container1LeafList1>"+  
                "<case5Container1LeafList1>entry2</case5Container1LeafList1>"+  
                "</case5Container1>"+ 
                "</default-choice-container>"+
                "</choice-container>" ;

        editConfig(m_server, m_clientInfo, requestXml, true);

        // delete mandatory leaf case5Container1Leaf1
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>"+  
                "<default-choice-container>"+
                "<case5Container1>"+
                "<case5Container1Leaf1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>"+  
                "</case5Container1>"+ 
                "</default-choice-container>"+
                "</choice-container>" ;

        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:default-choice-container/choice-case-test:case5Container1/choice-case-test:case5Container1Leaf1", "Missing mandatory node - case5Container1Leaf1", NetconfRpcErrorTag.DATA_MISSING);  
    }

    @Test
    public void testDefaultChoiceCase5DeleteScenario4() throws Exception {
        getModelNode();
        initialiseInterceptor();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>"+  
                "<default-choice-container>"+
                "<case5Leaf1>test</case5Leaf1>"+   
                "<case5Leaf2>test</case5Leaf2>"+  
                "<case5List1>"+
                "<case5List1Leaf1>list1</case5List1Leaf1>"+  
                "<case5List1Leaf2>test</case5List1Leaf2>"+  
                "</case5List1>"+ 
                "<case5List1>"+
                "<case5List1Leaf1>list2</case5List1Leaf1>"+  
                "<case5List1Leaf2>test</case5List1Leaf2>"+  
                "</case5List1>"+ 
                "<case5Container1>"+
                "<case5Container1Leaf1>test</case5Container1Leaf1>"+  
                "<case5Container1LeafList1>entry1</case5Container1LeafList1>"+  
                "<case5Container1LeafList1>entry2</case5Container1LeafList1>"+  
                "</case5Container1>"+ 
                "</default-choice-container>"+
                "</choice-container>" ;

        editConfig(m_server, m_clientInfo, requestXml, true);

        // delete case5Container1LeafList1 with min-element constraint
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>"+  
                "<default-choice-container>"+
                "<case5Container1>"+
                "<case5Container1LeafList1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\">entry1</case5Container1LeafList1>"+  
                "</case5Container1>"+ 
                "</default-choice-container>"+
                "</choice-container>" ;

        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:default-choice-container/choice-case-test:case5Container1/choice-case-test:case5Container1LeafList1", "Minimum elements required for case5Container1LeafList1 is 2.", NetconfRpcErrorTag.OPERATION_FAILED);  
    }

    @Test
    public void testDefaultChoiceCase5DeleteScenario5() throws Exception {
        getModelNode();
        initialiseInterceptor();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>"+  
                "<default-choice-container>"+
                "<case5Leaf1>test</case5Leaf1>"+   
                "<case5Leaf2>test</case5Leaf2>"+  
                "<case5List1>"+
                "<case5List1Leaf1>list1</case5List1Leaf1>"+  
                "<case5List1Leaf2>test</case5List1Leaf2>"+  
                "</case5List1>"+ 
                "<case5List1>"+
                "<case5List1Leaf1>list2</case5List1Leaf1>"+  
                "<case5List1Leaf2>test</case5List1Leaf2>"+  
                "</case5List1>"+ 
                "<case5Container1>"+
                "<case5Container1Leaf1>test</case5Container1Leaf1>"+  
                "<case5Container1LeafList1>entry1</case5Container1LeafList1>"+  
                "<case5Container1LeafList1>entry2</case5Container1LeafList1>"+  
                "</case5Container1>"+ 
                "</default-choice-container>"+
                "</choice-container>" ;

        editConfig(m_server, m_clientInfo, requestXml, true);

        // delete case5Container1 having mandatory nodes
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>"+  
                "<default-choice-container>"+
                "<case5Container1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>"+
                "</default-choice-container>"+
                "</choice-container>" ;

        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:default-choice-container/choice-case-test:case5Container1/choice-case-test:case5Container1Leaf1", "Missing mandatory node - case5Container1Leaf1", NetconfRpcErrorTag.DATA_MISSING);  
    }

    @Test
    public void testDefaultChoiceCase5DeleteScenario6() throws Exception {
        getModelNode();
        initialiseInterceptor();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>"+  
                "<default-choice-container>"+
                "<case5Leaf1>test</case5Leaf1>"+   
                "<case5Leaf2>test</case5Leaf2>"+  
                "<case5List1>"+
                "<case5List1Leaf1>list1</case5List1Leaf1>"+  
                "<case5List1Leaf2>test</case5List1Leaf2>"+  
                "</case5List1>"+ 
                "<case5List1>"+
                "<case5List1Leaf1>list2</case5List1Leaf1>"+  
                "<case5List1Leaf2>test</case5List1Leaf2>"+  
                "</case5List1>"+ 
                "<case5Container1>"+
                "<case5Container1Leaf1>test</case5Container1Leaf1>"+  
                "<case5Container1LeafList1>entry1</case5Container1LeafList1>"+  
                "<case5Container1LeafList1>entry2</case5Container1LeafList1>"+  
                "</case5Container1>"+ 
                "</default-choice-container>"+
                "</choice-container>" ;

        editConfig(m_server, m_clientInfo, requestXml, true);

        // delete case5Container1 having mandatory nodes
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>"+  
                "<default-choice-container>"+
                "<case5Leaf1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>"+   
                "<case5Leaf2 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>"+  
                "<case5List1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\">"+
                "<case5List1Leaf1>list1</case5List1Leaf1>"+  
                "</case5List1>"+ 
                "<case5List1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\">"+
                "<case5List1Leaf1>list2</case5List1Leaf1>"+  
                "</case5List1>"+ 
                "<case5Container1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>"+
                "</default-choice-container>"+
                "</choice-container>" ;

        //it needs to succeed and fall back to default case
        editConfig(m_server, m_clientInfo, requestXml, true);
        String expResponse = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" + 
                "<data>\n" + 
                "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">\n" + 
                "<choice-case-test:default-choice-container>\n" + 
                "<choice-case-test:case1Leaf1>XYZ</choice-case-test:case1Leaf1>\n" + 
                "</choice-case-test:default-choice-container>\n" + 
                "<choice-case-test:mandatory-choice-control-leaf>defaultEnabled</choice-case-test:mandatory-choice-control-leaf>\n" + 
                "</choice-case-test:choice-container>\n" + 
                "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n" +
                "</data>\n" + 
                "</rpc-reply>";
        verifyGet(expResponse);
    }

    @Test
    public void testDefaultChoiceCase6() throws Exception {
        getModelNode();
        initialiseInterceptor();

        //setting case6Leaf1 -->fails with missing mandatory nodes
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>"+  
                "<default-choice-container>"+
                "<case6Leaf1>test</case6Leaf1>"+   
                "</default-choice-container>"+
                "</choice-container>" ;

        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:default-choice-container/choice-case-test:case6InnerChoice", "Missing mandatory node - case6InnerChoice", NetconfRpcErrorTag.DATA_MISSING);  

        //setting case6Leaf1,case6InnerLeaf1 but still missing other mandatory nodes
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>"+  
                "<default-choice-container>"+
                "<case6Leaf1>test</case6Leaf1>"+  
                "<case6InnerLeaf1>test</case6InnerLeaf1>"+   
                "</default-choice-container>"+
                "</choice-container>" ;

        response = editConfig(m_server, m_clientInfo, requestXml, false);
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:default-choice-container/choice-case-test:case6InnerChoice", "Missing mandatory node - case6InnerChoice", NetconfRpcErrorTag.DATA_MISSING);         

        //setting case6Leaf1,case6InnerLeaf1 but still missing case6InnerList1 having min-element constraint
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>"+  
                "<default-choice-container>"+
                "<case6Leaf1>test</case6Leaf1>"+  
                "<case6InnerLeaf1>test</case6InnerLeaf1>"+ 
                "<case6InnerList1>"+
                "<case6InnerList1Leaf1>list1</case6InnerList1Leaf1>"+  
                "<case6InnerList1Leaf2>test</case6InnerList1Leaf2>"+  
                "</case6InnerList1>"+ 
                "</default-choice-container>"+
                "</choice-container>" ;

        response = editConfig(m_server, m_clientInfo, requestXml, false);
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:default-choice-container/choice-case-test:case6InnerList1", "Minimum elements required for case6InnerList1 is 2.", NetconfRpcErrorTag.OPERATION_FAILED);         

        //setting case6Leaf1,case6InnerLeaf1,case6InnerList1 entries, but still missing case6Container1 mandatory nodes
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>"+  
                "<default-choice-container>"+
                "<case6Leaf1>test</case6Leaf1>"+  
                "<case6InnerLeaf1>test</case6InnerLeaf1>"+ 
                "<case6InnerList1>"+
                "<case6InnerList1Leaf1>list1</case6InnerList1Leaf1>"+  
                "<case6InnerList1Leaf2>test</case6InnerList1Leaf2>"+  
                "</case6InnerList1>"+ 
                "<case6InnerList1>"+
                "<case6InnerList1Leaf1>list2</case6InnerList1Leaf1>"+  
                "<case6InnerList1Leaf2>test</case6InnerList1Leaf2>"+  
                "</case6InnerList1>"+ 
                "</default-choice-container>"+
                "</choice-container>" ;

        response = editConfig(m_server, m_clientInfo, requestXml, false);
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:default-choice-container/choice-case-test:case6Container1/choice-case-test:case6Container1Leaf1", "Missing mandatory node - case6Container1Leaf1", NetconfRpcErrorTag.DATA_MISSING);         

        //setting case6Leaf1,case6InnerLeaf1,case6InnerList1 entries, but still missing case6Container1LeafList1 having min-element constraint
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>"+  
                "<default-choice-container>"+
                "<case6Leaf1>test</case6Leaf1>"+  
                "<case6InnerLeaf1>test</case6InnerLeaf1>"+ 
                "<case6InnerList1>"+
                "<case6InnerList1Leaf1>list1</case6InnerList1Leaf1>"+  
                "<case6InnerList1Leaf2>test</case6InnerList1Leaf2>"+  
                "</case6InnerList1>"+ 
                "<case6InnerList1>"+
                "<case6InnerList1Leaf1>list2</case6InnerList1Leaf1>"+  
                "<case6InnerList1Leaf2>test</case6InnerList1Leaf2>"+  
                "</case6InnerList1>"+ 
                "<case6Container1>"+
                "<case6Container1Leaf1>test</case6Container1Leaf1>"+  
                "</case6Container1>"+ 
                "</default-choice-container>"+
                "</choice-container>" ;

        response = editConfig(m_server, m_clientInfo, requestXml, false);
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:default-choice-container/choice-case-test:case6Container1/choice-case-test:case6Container1LeafList1", "Minimum elements required for case6Container1LeafList1 is 2.", NetconfRpcErrorTag.OPERATION_FAILED);         

        //setting case6Leaf1,case6InnerLeaf1,case6InnerList1 entries, but still missing case6Container1List1 having min-element constraint
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>"+  
                "<default-choice-container>"+
                "<case6Leaf1>test</case6Leaf1>"+  
                "<case6InnerLeaf1>test</case6InnerLeaf1>"+ 
                "<case6InnerList1>"+
                "<case6InnerList1Leaf1>list1</case6InnerList1Leaf1>"+  
                "<case6InnerList1Leaf2>test</case6InnerList1Leaf2>"+  
                "</case6InnerList1>"+ 
                "<case6InnerList1>"+
                "<case6InnerList1Leaf1>list2</case6InnerList1Leaf1>"+  
                "<case6InnerList1Leaf2>test</case6InnerList1Leaf2>"+  
                "</case6InnerList1>"+ 
                "<case6Container1>"+
                "<case6Container1Leaf1>test</case6Container1Leaf1>"+  
                "<case6Container1LeafList1>test1</case6Container1LeafList1>"+  
                "<case6Container1LeafList1>test2</case6Container1LeafList1>"+  
                "</case6Container1>"+ 
                "</default-choice-container>"+
                "</choice-container>" ;

        response = editConfig(m_server, m_clientInfo, requestXml, false);
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:default-choice-container/choice-case-test:case6Container1/choice-case-test:case6Container1List1", "Minimum elements required for case6Container1List1 is 2.", NetconfRpcErrorTag.OPERATION_FAILED); 

        //setting case6Leaf1,case6InnerLeaf1,case6InnerList1 entries, but still missing case6Container1List1 having min-element constraint
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>"+  
                "<default-choice-container>"+
                "<case6Leaf1>test</case6Leaf1>"+  
                "<case6InnerLeaf1>test</case6InnerLeaf1>"+ 
                "<case6InnerList1>"+
                "<case6InnerList1Leaf1>list1</case6InnerList1Leaf1>"+  
                "<case6InnerList1Leaf2>test</case6InnerList1Leaf2>"+  
                "</case6InnerList1>"+ 
                "<case6InnerList1>"+
                "<case6InnerList1Leaf1>list2</case6InnerList1Leaf1>"+  
                "<case6InnerList1Leaf2>test</case6InnerList1Leaf2>"+  
                "</case6InnerList1>"+ 
                "<case6Container1>"+
                "<case6Container1Leaf1>test</case6Container1Leaf1>"+  
                "<case6Container1LeafList1>test1</case6Container1LeafList1>"+  
                "<case6Container1LeafList1>test2</case6Container1LeafList1>"+  
                "<case6Container1List1>"+
                "<case6Container1List1Leaf1>innerlist1</case6Container1List1Leaf1>"+  
                "<case6Container1List1Leaf2>test1</case6Container1List1Leaf2>"+  
                "</case6Container1List1>"+ 
                "</case6Container1>"+ 
                "</default-choice-container>"+
                "</choice-container>" ;

        response = editConfig(m_server, m_clientInfo, requestXml, false);
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:default-choice-container/choice-case-test:case6Container1/choice-case-test:case6Container1List1", "Minimum elements required for case6Container1List1 is 2.", NetconfRpcErrorTag.OPERATION_FAILED); 

        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>"+  
                "<default-choice-container>"+
                "<case6Leaf1>test</case6Leaf1>"+  
                "<case6InnerLeaf1>test</case6InnerLeaf1>"+ 
                "<case6InnerList1>"+
                "<case6InnerList1Leaf1>list1</case6InnerList1Leaf1>"+  
                "<case6InnerList1Leaf2>test</case6InnerList1Leaf2>"+  
                "</case6InnerList1>"+ 
                "<case6InnerList1>"+
                "<case6InnerList1Leaf1>list2</case6InnerList1Leaf1>"+  
                "<case6InnerList1Leaf2>test</case6InnerList1Leaf2>"+  
                "</case6InnerList1>"+ 
                "<case6Container1>"+
                "<case6Container1Leaf1>test</case6Container1Leaf1>"+  
                "<case6Container1Leaf2>test</case6Container1Leaf2>"+  
                "<case6Container1LeafList1>test1</case6Container1LeafList1>"+  
                "<case6Container1LeafList1>test2</case6Container1LeafList1>"+  
                "<case6Container1List1>"+
                "<case6Container1List1Leaf1>innerlist1</case6Container1List1Leaf1>"+  
                "<case6Container1List1Leaf2>test1</case6Container1List1Leaf2>"+  
                "</case6Container1List1>"+ 
                "<case6Container1List1>"+
                "<case6Container1List1Leaf1>innerlist2</case6Container1List1Leaf1>"+  
                "<case6Container1List1Leaf2>test2</case6Container1List1Leaf2>"+  
                "</case6Container1List1>"+ 
                "</case6Container1>"+ 
                "</default-choice-container>"+
                "</choice-container>" ;

        response = editConfig(m_server, m_clientInfo, requestXml, true);

        String expResponse = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" + 
                "<data>\n" + 
                "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">\n" + 
                "<choice-case-test:default-choice-container>\n" + 
                "<choice-case-test:case6Container1>\n" + 
                "<choice-case-test:case6Container1Leaf1>test</choice-case-test:case6Container1Leaf1>\n" + 
                "<choice-case-test:case6Container1Leaf2>test</choice-case-test:case6Container1Leaf2>\n" + 
                "<choice-case-test:case6Container1LeafList1>test1</choice-case-test:case6Container1LeafList1>\n" + 
                "<choice-case-test:case6Container1LeafList1>test2</choice-case-test:case6Container1LeafList1>\n" + 
                "<choice-case-test:case6Container1List1>\n" + 
                "<choice-case-test:case6Container1List1Leaf1>innerlist1</choice-case-test:case6Container1List1Leaf1>\n" + 
                "<choice-case-test:case6Container1List1Leaf2>test1</choice-case-test:case6Container1List1Leaf2>\n" + 
                "</choice-case-test:case6Container1List1>\n" + 
                "<choice-case-test:case6Container1List1>\n" + 
                "<choice-case-test:case6Container1List1Leaf1>innerlist2</choice-case-test:case6Container1List1Leaf1>\n" + 
                "<choice-case-test:case6Container1List1Leaf2>test2</choice-case-test:case6Container1List1Leaf2>\n" + 
                "</choice-case-test:case6Container1List1>\n" + 
                "</choice-case-test:case6Container1>\n" + 
                "<choice-case-test:case6InnerLeaf1>test</choice-case-test:case6InnerLeaf1>\n" + 
                "<choice-case-test:case6InnerList1>\n" + 
                "<choice-case-test:case6InnerList1Leaf1>list1</choice-case-test:case6InnerList1Leaf1>\n" + 
                "<choice-case-test:case6InnerList1Leaf2>test</choice-case-test:case6InnerList1Leaf2>\n" + 
                "</choice-case-test:case6InnerList1>\n" + 
                "<choice-case-test:case6InnerList1>\n" + 
                "<choice-case-test:case6InnerList1Leaf1>list2</choice-case-test:case6InnerList1Leaf1>\n" + 
                "<choice-case-test:case6InnerList1Leaf2>test</choice-case-test:case6InnerList1Leaf2>\n" + 
                "</choice-case-test:case6InnerList1>\n" + 
                "<choice-case-test:case6Leaf1>test</choice-case-test:case6Leaf1>\n" + 
                "</choice-case-test:default-choice-container>\n" + 
                "<choice-case-test:mandatory-choice-control-leaf>defaultEnabled</choice-case-test:mandatory-choice-control-leaf>\n" + 
                "</choice-case-test:choice-container>\n" + 
                "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n" +
                "</data>\n" + 
                "</rpc-reply>";
        verifyGet(expResponse);
    }

    @Test
    public void testDefaultChoiceCase6DeleteScenario1() throws Exception {
        getModelNode();
        initialiseInterceptor();

        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>"+  
                "<default-choice-container>"+
                "<case6Leaf1>test</case6Leaf1>"+  
                "<case6InnerLeaf1>test</case6InnerLeaf1>"+ 
                "<case6InnerList1>"+
                "<case6InnerList1Leaf1>list1</case6InnerList1Leaf1>"+  
                "<case6InnerList1Leaf2>test</case6InnerList1Leaf2>"+  
                "</case6InnerList1>"+ 
                "<case6InnerList1>"+
                "<case6InnerList1Leaf1>list2</case6InnerList1Leaf1>"+  
                "<case6InnerList1Leaf2>test</case6InnerList1Leaf2>"+  
                "</case6InnerList1>"+ 
                "<case6Container1>"+
                "<case6Container1Leaf1>test</case6Container1Leaf1>"+  
                "<case6Container1Leaf2>test</case6Container1Leaf2>"+  
                "<case6Container1LeafList1>test1</case6Container1LeafList1>"+  
                "<case6Container1LeafList1>test2</case6Container1LeafList1>"+  
                "<case6Container1List1>"+
                "<case6Container1List1Leaf1>innerlist1</case6Container1List1Leaf1>"+  
                "<case6Container1List1Leaf2>test1</case6Container1List1Leaf2>"+  
                "</case6Container1List1>"+ 
                "<case6Container1List1>"+
                "<case6Container1List1Leaf1>innerlist2</case6Container1List1Leaf1>"+  
                "<case6Container1List1Leaf2>test2</case6Container1List1Leaf2>"+  
                "</case6Container1List1>"+ 
                "</case6Container1>"+ 
                "</default-choice-container>"+
                "</choice-container>" ;

        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, true);

        String expResponse = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" + 
                "<data>\n" + 
                "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">\n" + 
                "<choice-case-test:default-choice-container>\n" + 
                "<choice-case-test:case6Container1>\n" + 
                "<choice-case-test:case6Container1Leaf1>test</choice-case-test:case6Container1Leaf1>\n" + 
                "<choice-case-test:case6Container1Leaf2>test</choice-case-test:case6Container1Leaf2>\n" + 
                "<choice-case-test:case6Container1LeafList1>test1</choice-case-test:case6Container1LeafList1>\n" + 
                "<choice-case-test:case6Container1LeafList1>test2</choice-case-test:case6Container1LeafList1>\n" + 
                "<choice-case-test:case6Container1List1>\n" + 
                "<choice-case-test:case6Container1List1Leaf1>innerlist1</choice-case-test:case6Container1List1Leaf1>\n" + 
                "<choice-case-test:case6Container1List1Leaf2>test1</choice-case-test:case6Container1List1Leaf2>\n" + 
                "</choice-case-test:case6Container1List1>\n" + 
                "<choice-case-test:case6Container1List1>\n" + 
                "<choice-case-test:case6Container1List1Leaf1>innerlist2</choice-case-test:case6Container1List1Leaf1>\n" + 
                "<choice-case-test:case6Container1List1Leaf2>test2</choice-case-test:case6Container1List1Leaf2>\n" + 
                "</choice-case-test:case6Container1List1>\n" + 
                "</choice-case-test:case6Container1>\n" + 
                "<choice-case-test:case6InnerLeaf1>test</choice-case-test:case6InnerLeaf1>\n" + 
                "<choice-case-test:case6InnerList1>\n" + 
                "<choice-case-test:case6InnerList1Leaf1>list1</choice-case-test:case6InnerList1Leaf1>\n" + 
                "<choice-case-test:case6InnerList1Leaf2>test</choice-case-test:case6InnerList1Leaf2>\n" + 
                "</choice-case-test:case6InnerList1>\n" + 
                "<choice-case-test:case6InnerList1>\n" + 
                "<choice-case-test:case6InnerList1Leaf1>list2</choice-case-test:case6InnerList1Leaf1>\n" + 
                "<choice-case-test:case6InnerList1Leaf2>test</choice-case-test:case6InnerList1Leaf2>\n" + 
                "</choice-case-test:case6InnerList1>\n" + 
                "<choice-case-test:case6Leaf1>test</choice-case-test:case6Leaf1>\n" + 
                "</choice-case-test:default-choice-container>\n" + 
                "<choice-case-test:mandatory-choice-control-leaf>defaultEnabled</choice-case-test:mandatory-choice-control-leaf>\n" + 
                "</choice-case-test:choice-container>\n" + 
                "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n" +
                "</data>\n" + 
                "</rpc-reply>";
        verifyGet(expResponse);

        //delete case6Container1Leaf2 -->non-mandatory leaf
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>"+  
                "<default-choice-container>"+
                "<case6Container1>"+
                "<case6Container1Leaf2 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>"+  
                "</case6Container1>"+ 
                "</default-choice-container>"+
                "</choice-container>" ;

        response = editConfig(m_server, m_clientInfo, requestXml, true);

        //delete case6Container1Leaf1 -->non-mandatory leaf
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>"+  
                "<default-choice-container>"+
                "<case6Container1>"+
                "<case6Container1Leaf1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>"+  
                "</case6Container1>"+ 
                "</default-choice-container>"+
                "</choice-container>" ;

        response = editConfig(m_server, m_clientInfo, requestXml, false);
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:default-choice-container/choice-case-test:case6Container1/choice-case-test:case6Container1Leaf1", "Missing mandatory node - case6Container1Leaf1", NetconfRpcErrorTag.DATA_MISSING);         
    }

    @Test
    public void testDefaultChoiceCase6DeleteScenario2() throws Exception {
        getModelNode();
        initialiseInterceptor();

        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>"+  
                "<default-choice-container>"+
                "<case6Leaf1>test</case6Leaf1>"+  
                "<case6InnerLeaf1>test</case6InnerLeaf1>"+ 
                "<case6InnerList1>"+
                "<case6InnerList1Leaf1>list1</case6InnerList1Leaf1>"+  
                "<case6InnerList1Leaf2>test</case6InnerList1Leaf2>"+  
                "</case6InnerList1>"+ 
                "<case6InnerList1>"+
                "<case6InnerList1Leaf1>list2</case6InnerList1Leaf1>"+  
                "<case6InnerList1Leaf2>test</case6InnerList1Leaf2>"+  
                "</case6InnerList1>"+ 
                "<case6Container1>"+
                "<case6Container1Leaf1>test</case6Container1Leaf1>"+  
                "<case6Container1Leaf2>test</case6Container1Leaf2>"+  
                "<case6Container1LeafList1>test1</case6Container1LeafList1>"+  
                "<case6Container1LeafList1>test2</case6Container1LeafList1>"+  
                "<case6Container1List1>"+
                "<case6Container1List1Leaf1>innerlist1</case6Container1List1Leaf1>"+  
                "<case6Container1List1Leaf2>test1</case6Container1List1Leaf2>"+  
                "</case6Container1List1>"+ 
                "<case6Container1List1>"+
                "<case6Container1List1Leaf1>innerlist2</case6Container1List1Leaf1>"+  
                "<case6Container1List1Leaf2>test2</case6Container1List1Leaf2>"+  
                "</case6Container1List1>"+ 
                "</case6Container1>"+ 
                "</default-choice-container>"+
                "</choice-container>" ;

        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, true);

        //delete case6Container1LeafList1 -->fails with min-element constraint
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>"+  
                "<default-choice-container>"+
                "<case6Container1>"+
                "<case6Container1LeafList1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\">test1</case6Container1LeafList1>"+  
                "</case6Container1>"+ 
                "</default-choice-container>"+
                "</choice-container>" ;

        response = editConfig(m_server, m_clientInfo, requestXml, false);
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:default-choice-container/choice-case-test:case6Container1/choice-case-test:case6Container1LeafList1", "Minimum elements required for case6Container1LeafList1 is 2.", NetconfRpcErrorTag.OPERATION_FAILED);         
    }

    @Test
    public void testDefaultChoiceCase6DeleteScenario3() throws Exception {
        getModelNode();
        initialiseInterceptor();

        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>"+  
                "<default-choice-container>"+
                "<case6Leaf1>test</case6Leaf1>"+  
                "<case6InnerLeaf1>test</case6InnerLeaf1>"+ 
                "<case6InnerList1>"+
                "<case6InnerList1Leaf1>list1</case6InnerList1Leaf1>"+  
                "<case6InnerList1Leaf2>test</case6InnerList1Leaf2>"+  
                "</case6InnerList1>"+ 
                "<case6InnerList1>"+
                "<case6InnerList1Leaf1>list2</case6InnerList1Leaf1>"+  
                "<case6InnerList1Leaf2>test</case6InnerList1Leaf2>"+  
                "</case6InnerList1>"+ 
                "<case6Container1>"+
                "<case6Container1Leaf1>test</case6Container1Leaf1>"+  
                "<case6Container1Leaf2>test</case6Container1Leaf2>"+  
                "<case6Container1LeafList1>test1</case6Container1LeafList1>"+  
                "<case6Container1LeafList1>test2</case6Container1LeafList1>"+  
                "<case6Container1List1>"+
                "<case6Container1List1Leaf1>innerlist1</case6Container1List1Leaf1>"+  
                "<case6Container1List1Leaf2>test1</case6Container1List1Leaf2>"+  
                "</case6Container1List1>"+ 
                "<case6Container1List1>"+
                "<case6Container1List1Leaf1>innerlist2</case6Container1List1Leaf1>"+  
                "<case6Container1List1Leaf2>test2</case6Container1List1Leaf2>"+  
                "</case6Container1List1>"+ 
                "</case6Container1>"+ 
                "</default-choice-container>"+
                "</choice-container>" ;

        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, true);

        //delete case6Container1List1 -->fails with min-element constraint
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>"+  
                "<default-choice-container>"+
                "<case6Container1>"+
                "<case6Container1List1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\">"+
                "<case6Container1List1Leaf1>innerlist1</case6Container1List1Leaf1>"+  
                "</case6Container1List1>"+ 
                "</case6Container1>"+
                "</default-choice-container>"+
                "</choice-container>" ;

        response = editConfig(m_server, m_clientInfo, requestXml, false);
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:default-choice-container/choice-case-test:case6Container1/choice-case-test:case6Container1List1", "Minimum elements required for case6Container1List1 is 2.", NetconfRpcErrorTag.OPERATION_FAILED);         
    }

    @Test
    public void testDefaultChoiceCase6DeleteScenario4() throws Exception {
        getModelNode();
        initialiseInterceptor();

        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>"+  
                "<default-choice-container>"+
                "<case6Leaf1>test</case6Leaf1>"+  
                "<case6InnerLeaf1>test</case6InnerLeaf1>"+ 
                "<case6InnerList1>"+
                "<case6InnerList1Leaf1>list1</case6InnerList1Leaf1>"+  
                "<case6InnerList1Leaf2>test</case6InnerList1Leaf2>"+  
                "</case6InnerList1>"+ 
                "<case6InnerList1>"+
                "<case6InnerList1Leaf1>list2</case6InnerList1Leaf1>"+  
                "<case6InnerList1Leaf2>test</case6InnerList1Leaf2>"+  
                "</case6InnerList1>"+ 
                "<case6Container1>"+
                "<case6Container1Leaf1>test</case6Container1Leaf1>"+  
                "<case6Container1Leaf2>test</case6Container1Leaf2>"+  
                "<case6Container1LeafList1>test1</case6Container1LeafList1>"+  
                "<case6Container1LeafList1>test2</case6Container1LeafList1>"+  
                "<case6Container1List1>"+
                "<case6Container1List1Leaf1>innerlist1</case6Container1List1Leaf1>"+  
                "<case6Container1List1Leaf2>test1</case6Container1List1Leaf2>"+  
                "</case6Container1List1>"+ 
                "<case6Container1List1>"+
                "<case6Container1List1Leaf1>innerlist2</case6Container1List1Leaf1>"+  
                "<case6Container1List1Leaf2>test2</case6Container1List1Leaf2>"+  
                "</case6Container1List1>"+ 
                "</case6Container1>"+ 
                "</default-choice-container>"+
                "</choice-container>" ;

        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, true);

        //delete case6InnerList1 
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>"+  
                "<default-choice-container>"+
                "<case6InnerList1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\">"+
                "<case6InnerList1Leaf1>list1</case6InnerList1Leaf1>"+  
                "</case6InnerList1>"+ 
                "<case6InnerList1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\">"+
                "<case6InnerList1Leaf1>list2</case6InnerList1Leaf1>"+  
                "</case6InnerList1>"+ 
                "</default-choice-container>"+
                "</choice-container>" ;

        response = editConfig(m_server, m_clientInfo, requestXml, false);
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:default-choice-container/choice-case-test:case6InnerChoice", "Missing mandatory node - case6InnerChoice", NetconfRpcErrorTag.DATA_MISSING);         
    }

    @Test
    public void testDefaultChoiceCase6DeleteScenario5() throws Exception {
        getModelNode();
        initialiseInterceptor();

        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>"+  
                "<default-choice-container>"+
                "<case6Leaf1>test</case6Leaf1>"+  
                "<case6InnerLeaf1>test</case6InnerLeaf1>"+ 
                "<case6InnerList1>"+
                "<case6InnerList1Leaf1>list1</case6InnerList1Leaf1>"+  
                "<case6InnerList1Leaf2>test</case6InnerList1Leaf2>"+  
                "</case6InnerList1>"+ 
                "<case6InnerList1>"+
                "<case6InnerList1Leaf1>list2</case6InnerList1Leaf1>"+  
                "<case6InnerList1Leaf2>test</case6InnerList1Leaf2>"+  
                "</case6InnerList1>"+ 
                "<case6Container1>"+
                "<case6Container1Leaf1>test</case6Container1Leaf1>"+  
                "<case6Container1Leaf2>test</case6Container1Leaf2>"+  
                "<case6Container1LeafList1>test1</case6Container1LeafList1>"+  
                "<case6Container1LeafList1>test2</case6Container1LeafList1>"+  
                "<case6Container1List1>"+
                "<case6Container1List1Leaf1>innerlist1</case6Container1List1Leaf1>"+  
                "<case6Container1List1Leaf2>test1</case6Container1List1Leaf2>"+  
                "</case6Container1List1>"+ 
                "<case6Container1List1>"+
                "<case6Container1List1Leaf1>innerlist2</case6Container1List1Leaf1>"+  
                "<case6Container1List1Leaf2>test2</case6Container1List1Leaf2>"+  
                "</case6Container1List1>"+ 
                "</case6Container1>"+ 
                "</default-choice-container>"+
                "</choice-container>" ;

        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, true);

        //delete case6Container1 
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>"+  
                "<default-choice-container>"+
                "<case6Container1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>"+
                "</default-choice-container>"+
                "</choice-container>" ;

        response = editConfig(m_server, m_clientInfo, requestXml, false);
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:default-choice-container/choice-case-test:case6Container1/choice-case-test:case6Container1Leaf1", "Missing mandatory node - case6Container1Leaf1", NetconfRpcErrorTag.DATA_MISSING);         
    }

    @Test
    public void testDefaultChoiceCase6DeleteScenario6() throws Exception {
        getModelNode();
        initialiseInterceptor();

        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>"+  
                "<default-choice-container>"+
                "<case6Leaf1>test</case6Leaf1>"+  
                "<case6InnerLeaf1>test</case6InnerLeaf1>"+ 
                "<case6InnerList1>"+
                "<case6InnerList1Leaf1>list1</case6InnerList1Leaf1>"+  
                "<case6InnerList1Leaf2>test</case6InnerList1Leaf2>"+  
                "</case6InnerList1>"+ 
                "<case6InnerList1>"+
                "<case6InnerList1Leaf1>list2</case6InnerList1Leaf1>"+  
                "<case6InnerList1Leaf2>test</case6InnerList1Leaf2>"+  
                "</case6InnerList1>"+ 
                "<case6Container1>"+
                "<case6Container1Leaf1>test</case6Container1Leaf1>"+  
                "<case6Container1Leaf2>test</case6Container1Leaf2>"+  
                "<case6Container1LeafList1>test1</case6Container1LeafList1>"+  
                "<case6Container1LeafList1>test2</case6Container1LeafList1>"+  
                "<case6Container1List1>"+
                "<case6Container1List1Leaf1>innerlist1</case6Container1List1Leaf1>"+  
                "<case6Container1List1Leaf2>test1</case6Container1List1Leaf2>"+  
                "</case6Container1List1>"+ 
                "<case6Container1List1>"+
                "<case6Container1List1Leaf1>innerlist2</case6Container1List1Leaf1>"+  
                "<case6Container1List1Leaf2>test2</case6Container1List1Leaf2>"+  
                "</case6Container1List1>"+ 
                "</case6Container1>"+ 
                "</default-choice-container>"+
                "</choice-container>" ;

        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, true);

        //delete all, should fallback to default case 
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>"+  
                "<default-choice-container>"+
                "<case6Container1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>"+
                "<case6InnerList1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\">"+
                "<case6InnerList1Leaf1>list1</case6InnerList1Leaf1>"+  
                "</case6InnerList1>"+ 
                "<case6InnerList1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\">"+
                "<case6InnerList1Leaf1>list2</case6InnerList1Leaf1>"+  
                "</case6InnerList1>"+ 
                "<case6InnerLeaf1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>"+ 
                "<case6Leaf1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>"+ 
                "</default-choice-container>"+
                "</choice-container>" ;

        //it needs to succeed and fall back to default case
        editConfig(m_server, m_clientInfo, requestXml, true);
        String expResponse = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" + 
                "<data>\n" + 
                "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">\n" + 
                "<choice-case-test:default-choice-container>\n" + 
                "<choice-case-test:case1Leaf1>XYZ</choice-case-test:case1Leaf1>\n" + 
                "</choice-case-test:default-choice-container>\n" + 
                "<choice-case-test:mandatory-choice-control-leaf>defaultEnabled</choice-case-test:mandatory-choice-control-leaf>\n" + 
                "</choice-case-test:choice-container>\n" + 
                "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n" +
                "</data>\n" + 
                "</rpc-reply>";
        verifyGet(expResponse);       
    }

    @Test
    public void testDefaultChoiceCase2NegativeScenario1() throws Exception {
        getModelNode();
        initialiseInterceptor();
        
        //both case2Leaf1 and case2LeafList1 is set
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>"+  
                "<default-choice-container>"+
                "<case2Leaf1>test</case2Leaf1>"+   
                "<case2LeafList1>test1</case2LeafList1>"+    
                "<case2LeafList1>test2</case2LeafList1>"+               
                "</default-choice-container>"+
                "</choice-container>" ;

        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, true);

        String expResponse = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" + 
                "<data>\n" + 
                "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">\n" + 
                "<choice-case-test:default-choice-container>\n" + 
                "<choice-case-test:case2Leaf1>test</choice-case-test:case2Leaf1>\n" + 
                "<choice-case-test:case2LeafList1>test1</choice-case-test:case2LeafList1>\n" + 
                "<choice-case-test:case2LeafList1>test2</choice-case-test:case2LeafList1>\n" + 
                "</choice-case-test:default-choice-container>\n" + 
                "<choice-case-test:mandatory-choice-control-leaf>defaultEnabled</choice-case-test:mandatory-choice-control-leaf>\n" + 
                "</choice-case-test:choice-container>\n" + 
                "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n" +
                "</data>\n" + 
                "</rpc-reply>";
        verifyGet(expResponse);

        //add case2LeafList1 which has max-element constraint
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>"+  
                "<default-choice-container>"+
                "<case2LeafList1>test3</case2LeafList1>\n" + 
                "<case2LeafList1>test4</case2LeafList1>\n" + 
                "</default-choice-container>"+
                "</choice-container>" ;

        response = editConfig(m_server, m_clientInfo, requestXml, false);
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:default-choice-container/choice-case-test:case2LeafList1", "Maximum elements allowed for case2LeafList1 is 3.", NetconfRpcErrorTag.OPERATION_FAILED);         
    }
    
    @Test
    public void testDefaultChoiceCase4NegativeScenario() throws Exception {
        getModelNode();
        initialiseInterceptor();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>"+  
                "<default-choice-container>"+
                "<case4Leaf1>test</case4Leaf1>"+  
                "<case4Leaf2>test</case4Leaf2>"+     
                "<case4List1>"+
                "<case4List1Leaf1>list1</case4List1Leaf1>"+  
                "<case4List1Leaf2>test</case4List1Leaf2>"+  
                "<case4List1Container1>"+
                "<case4List1Container1Leaf1>test</case4List1Container1Leaf1>"+  
                "</case4List1Container1>"+
                "</case4List1>"+  
                "<case4List1>"+
                "<case4List1Leaf1>list2</case4List1Leaf1>"+ 
                "<case4List1Leaf2>test</case4List1Leaf2>"+  
                "<case4List1Leaf3>test</case4List1Leaf3>"+  
                "<case4List1Container1>"+
                "<case4List1Container1Leaf1>test</case4List1Container1Leaf1>"+  
                "</case4List1Container1>"+
                "</case4List1>"+  
                "</default-choice-container>"+
                "</choice-container>" ;

        editConfig(m_server, m_clientInfo, requestXml, true);
        
        //add case4List1 entries which has max-element '3' constraint
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>"+  
                "<default-choice-container>"+  
                "<case4List1>"+
                "<case4List1Leaf1>list3</case4List1Leaf1>"+  
                "<case4List1Leaf2>test</case4List1Leaf2>"+  
                "<case4List1Container1>"+
                "<case4List1Container1Leaf1>test</case4List1Container1Leaf1>"+  
                "</case4List1Container1>"+
                "</case4List1>"+  
                "<case4List1>"+
                "<case4List1Leaf1>list4</case4List1Leaf1>"+ 
                "<case4List1Leaf2>test</case4List1Leaf2>"+  
                "<case4List1Leaf3>test</case4List1Leaf3>"+  
                "<case4List1Container1>"+
                "<case4List1Container1Leaf1>test</case4List1Container1Leaf1>"+  
                "</case4List1Container1>"+
                "</case4List1>"+  
                "</default-choice-container>"+
                "</choice-container>" ;

        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
        checkErrors(response, "/choice-case-test:choice-container/choice-case-test:default-choice-container/choice-case-test:case4List1", "Maximum elements allowed for case4List1 is 3.", NetconfRpcErrorTag.OPERATION_FAILED);         
    }
    
    @Test
    public void testDefaultChoiceCase7() throws Exception {
        getModelNode();
        initialiseInterceptor();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>"+  
                "<default-choice-container>"+
                "<case7List1>"+
                "<case7List1Leaf1>list1</case7List1Leaf1>"+  
                "<case7List1Leaf2>test</case7List1Leaf2>"+  
                "</case7List1>"+  
                "<case7List1>"+
                "<case7List1Leaf1>list2</case7List1Leaf1>"+ 
                "<case7List1Leaf2>test</case7List1Leaf2>"+  
                "</case7List1>"+  
                "</default-choice-container>"+
                "</choice-container>" ;

        editConfig(m_server, m_clientInfo, requestXml, true);
        
        String expResponse = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" + 
                "<data>\n" + 
                "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">\n" + 
                "<choice-case-test:default-choice-container>\n" + 
                "<choice-case-test:case7List1>\n" + 
                "<choice-case-test:case7List1Leaf1>list1</choice-case-test:case7List1Leaf1>\n" + 
                "<choice-case-test:case7List1Leaf2>test</choice-case-test:case7List1Leaf2>\n" + 
                "</choice-case-test:case7List1>\n" + 
                "<choice-case-test:case7List1>\n" + 
                "<choice-case-test:case7List1Leaf1>list2</choice-case-test:case7List1Leaf1>\n" + 
                "<choice-case-test:case7List1Leaf2>test</choice-case-test:case7List1Leaf2>\n" + 
                "</choice-case-test:case7List1>\n" + 
                "</choice-case-test:default-choice-container>\n" + 
                "<choice-case-test:mandatory-choice-control-leaf>defaultEnabled</choice-case-test:mandatory-choice-control-leaf>\n" + 
                "</choice-case-test:choice-container>\n" + 
                "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n" +
                "</data>\n" + 
                "</rpc-reply>";
        verifyGet(expResponse);

        // delete case7List1 entries , fallback to default case
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<choice-container xmlns=\"urn:org:bbf:pma:choice-case-test\">" +
                "<mandatory-choice-control-leaf>defaultEnabled</mandatory-choice-control-leaf>"+  
                "<default-choice-container>"+  
                "<case7List1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\">"+
                "<case7List1Leaf1>list1</case7List1Leaf1>"+  
                "<case7List1Leaf2>test</case7List1Leaf2>"+  
                "</case7List1>"+  
                "<case7List1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\">"+
                "<case7List1Leaf1>list2</case7List1Leaf1>"+  
                "<case7List1Leaf2>test</case7List1Leaf2>"+  
                "</case7List1>"+  
                "</default-choice-container>"+
                "</choice-container>" ;

        editConfig(m_server, m_clientInfo, requestXml, true);
        
        expResponse = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" + 
                "<data>\n" + 
                "<choice-case-test:choice-container xmlns:choice-case-test=\"urn:org:bbf:pma:choice-case-test\">\n" + 
                "<choice-case-test:default-choice-container>\n" + 
                "<choice-case-test:case1Leaf1>XYZ</choice-case-test:case1Leaf1>\n" + 
                "</choice-case-test:default-choice-container>\n" + 
                "<choice-case-test:mandatory-choice-control-leaf>defaultEnabled</choice-case-test:mandatory-choice-control-leaf>\n" + 
                "</choice-case-test:choice-container>\n" + 
                "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n" +
                "</data>\n" + 
                "</rpc-reply>";
        verifyGet(expResponse);          
    }
    
    private void checkErrors(NetConfResponse response, String errorPath, String errorMessage, NetconfRpcErrorTag tag) {
        List<NetconfRpcError> errors = response.getErrors();
        assertEquals(1, errors.size());
        checkErrors(errors.get(0), errorPath, errorMessage, tag);
    }

    private void checkErrors(NetconfRpcError rpcError, String errorPath, String errorMessage, NetconfRpcErrorTag tag) {
        assertEquals(errorPath, rpcError.getErrorPath());
        assertEquals(errorMessage, rpcError.getErrorMessage());
        assertEquals(tag, rpcError.getErrorTag());
    }
}

