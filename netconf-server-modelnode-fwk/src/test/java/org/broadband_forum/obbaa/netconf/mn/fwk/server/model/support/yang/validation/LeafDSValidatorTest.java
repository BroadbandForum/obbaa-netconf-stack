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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;
import org.xml.sax.SAXException;

import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorSeverity;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorType;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;

public class LeafDSValidatorTest extends AbstractDataStoreValidatorTest {

    @Test
    public void testNotMatchedLeafRefValue() throws ModelNodeInitException {
        testFail("/datastorevalidatortest/leafrefvalidation/defaultxml/invalid-leaf-ref.xml", NetconfRpcErrorTag
                        .DATA_MISSING,
                NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error, "instance-required",
                "Instance required with value - ABC",
                "/validation:validation/validation:leaf-ref/validation:album[validation:name=Album1]/validation:song" +
                        "[validation:name=Circus]/validation:artist-name");
    }

    @Test
    public void testMatchedLeafRefValue() throws ModelNodeInitException {
        testPass("/datastorevalidatortest/leafrefvalidation/defaultxml/valid-leaf-ref.xml");
    }

    @Test
    public void testNotMatchedLeafRefValueWithCurrent() throws ModelNodeInitException {
        testFail("/datastorevalidatortest/leafrefvalidation/defaultxml/invalid-reference-leaf-ref.xml",
                NetconfRpcErrorTag.DATA_MISSING,
                NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error, "instance-required",
                "Instance required with value - Invalid_SONG",
                "/validation:validation/validation:leaf-ref/validation:music/validation:favourite-song");
    }

    @Test
    public void testMatchedLeafRefValueWithCurrent() throws ModelNodeInitException {
        String requestXml1 = "/datastorevalidatortest/leafrefvalidation/defaultxml/valid-reference-leaf-ref.xml";
        getModelNode();
        EditConfigRequest request1 = createRequest(requestXml1);
        request1.setMessageId("1");
        NetConfResponse response1 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request1, response1);

        assertTrue(response1.isOk());
    }

    @Test
    public void testNotLeaf() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "  <validation>abc</validation>" +
                "  <notLeaf>10</notLeaf>" +
                "</validation>                                                 ";
        editConfig(m_server, m_clientInfo, requestXml1, true);

        String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "  <validation>hello</validation>" +
                "</validation>                                                 ";
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml2, false);
        assertEquals(1, response.getErrors().size());
        assertEquals("/validation:validation/validation:notLeaf", response.getErrors().get(0).getErrorPath());
        assertEquals("Violate must constraints: not(../validation = 'hello')", response.getErrors().get(0)
                .getErrorMessage());

    }

    @Test
    public void testDeleteofContainer() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                " <when-validation>" +
                "   <result-container>12</result-container>" +
                "   <result-list>10</result-list>" +
                " </when-validation>" +
                "</validation>                                                 ";
        editConfig(m_server, m_clientInfo, requestXml1, true);

        String response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + " <data>"
                        + "  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
                        + "   <validation:when-validation>"
                        + "    <validation:result-container>12</validation:result-container>"
                        + "    <validation:result-list>10</validation:result-list>"
                        + "   </validation:when-validation>"
                        + "  </validation:validation>"
                        + " </data>"
                        + " </rpc-reply>";
        verifyGet(response);

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                " <when-validation>" +
                "   <container-type/>" +
                "   <list-type>" +
                "     <list-id>a</list-id>" +
                "   </list-type>" +
                " </when-validation>" +
                "</validation>                                                 ";
        editConfig(m_server, m_clientInfo, requestXml1, true);

        response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + " <data>"
                        + "  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
                        + "   <validation:when-validation>"
                        + "    <validation:result-container>12</validation:result-container>"
                        + "    <validation:result-list>10</validation:result-list>"
                        + "    <validation:container-type/>"
                        + "    <validation:list-type>"
                        + "     <validation:list-id>a</validation:list-id>"
                        + "    </validation:list-type>"
                        + "   </validation:when-validation>"
                        + "  </validation:validation>"
                        + " </data>"
                        + " </rpc-reply>"
        ;
        verifyGet(response);

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                " <when-validation>" +
                "   <result-container>9</result-container>" +
                "   <result-list>9</result-list>" +
                " </when-validation>" +
                "</validation>                                                 ";
        editConfig(m_server, m_clientInfo, requestXml1, true);

        response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + " <data>"
                        + "  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
                        + "   <validation:when-validation>"
                        + "    <validation:result-container>9</validation:result-container>"
                        + "    <validation:result-list>9</validation:result-list>"
                        + "   </validation:when-validation>"
                        + "  </validation:validation>"
                        + " </data>"
                        + " </rpc-reply>"
        ;
        verifyGet(response);
    }

    @Test
    public void testContainsLeaf() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "  <validation>hello</validation>" +
                "  <containsLeaf>10</containsLeaf>" +
                "</validation>                                                 ";
        editConfig(m_server, m_clientInfo, requestXml1, true);

        String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "  <validation>abc</validation>" +
                "</validation>                                                 ";
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml2, false);
        assertEquals(1, response.getErrors().size());
        assertEquals("/validation:validation/validation:containsLeaf", response.getErrors().get(0).getErrorPath());
        assertEquals("Violate must constraints: contains(../validation,'hello')", response.getErrors().get(0)
                .getErrorMessage());
    }


    @Test
    public void testImpactLeafRefOnDelete() throws ModelNodeInitException {
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>					" +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "	<validation>abc</validation>" +
                "  <leafref-validation>abc</leafref-validation>" +
                "</validation>													";
        getModelNode();
        editConfig(m_server, m_clientInfo, requestXml1, true);

        String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<validation xmlns=\"urn:org:bbf:pma:validation\"  " +
                "xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                "	<validation xc:operation=\"delete\">abc</validation>" +
                "</validation>													";

        NetConfResponse response2 = editConfig(m_server, m_clientInfo, requestXml2, false);
        assertFalse(response2.isOk());
        assertEquals("/validation:validation/validation:leafref-validation", response2.getErrors().get(0)
                .getErrorPath());

    }

    @Test
    public void testChoiceLeaf() throws Exception {
        RequestScope.setEnableThreadLocalInUT(true);
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "  <choicecase>" +
                "    <list1-type>" +
                "      <list-key>key</list-key>" +
                "      <case-leaf1>10</case-leaf1>" +
                "    </list1-type>" +
                "  </choicecase>" +
                "</validation>                                                 ";

        getModelNode();
        editConfig(m_server, m_clientInfo, requestXml1, true);
        RequestScope.setEnableThreadLocalInUT(false);
    }

    @Test
    public void testImpactListOnChange() throws Exception {
        RequestScope.setEnableThreadLocalInUT(true);
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "  <when-validation>" +
                "      <result-list>10</result-list>" +
                "      <list-type>" +
                "         <list-id>id</list-id>" +
                "         <list-value>value</list-value>" +
                "      </list-type>" +
                "  </when-validation>" +
                "</validation>                                                 ";
        getModelNode();
        editConfig(m_server, m_clientInfo, requestXml1, true);

        String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "  <when-validation>" +
                "      <result-list>9</result-list>" +
                "  </when-validation>" +
                "</validation>                                                 ";
        editConfig(m_server, m_clientInfo, requestXml2, true);
        String expectedOutput =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "<data>"
                        + " <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
                        + "  <validation:when-validation>"
                        + "   <validation:result-list>9</validation:result-list>"
                        + "  </validation:when-validation>"
                        + " </validation:validation>"
                        + "</data>"
                        + "</rpc-reply>";
        verifyGet(expectedOutput);

        RequestScope.setEnableThreadLocalInUT(false);
    }

    @Test
    public void testImpactLeafOnChange() throws ModelNodeInitException, NetconfMessageBuilderException,
            SchemaBuildException, SAXException, IOException {
        RequestScope.setEnableThreadLocalInUT(true);
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>					" +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "	<arithmetic-validation>" +
                "		<value1>5</value1>" +
                "		<mod-leaf>10</mod-leaf>" +
                "	</arithmetic-validation>" +
                "</validation>													";
        getModelNode();
        EditConfigRequest request1 = createRequestFromString(requestXml1);
        request1.setMessageId("1");
        NetConfResponse response1 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request1, response1);
        assertTrue(response1.isOk());

        String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "	<arithmetic-validation>" +
                "		<value1>4</value1>" +
                "	</arithmetic-validation>" +
                "</validation>													";
        getModelNode();
        EditConfigRequest request2 = createRequestFromString(requestXml2);
        request2.setMessageId("1");
        NetConfResponse response2 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request2, response2);
        assertTrue(response2.isOk());

        String expectedOutput =
                "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                        "<data>" +
                        "<validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">" +
                        "<validation:arithmetic-validation>" +
                        "<validation:value1>4</validation:value1>" +
                        "</validation:arithmetic-validation>" +
                        "</validation:validation>" +
                        "</data>" +
                        "</rpc-reply>";

        verifyGet(expectedOutput);
        RequestScope.setEnableThreadLocalInUT(false);

    }

    @Test
    public void testValidLeafRefWithCurrentAlone() throws ModelNodeInitException {
        testMatchedLeafRefValueWithCurrent();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>					" +
                "<validation xmlns=\"urn:org:bbf:pma:validation\"> 		" +
                "	<leaf-ref> 													" +
                "    	<artist> 												" +
                "    		<name>LENY</name> 									" +
                "    	</artist> 												" +
                "    	<album> 												" +
                "    		<name>Album1</name> 								" +
                "	    	<song>												" +
                "	    		<name>Last Christmas</name>						" +
                "	    		<artist-name>LENY</artist-name>					" +
                "	    	</song>												" +
                " 			<song-count>20</song-count>							" +
                "       </album>												" +
                "      <music>													" +
                "     		<kind>Balad</kind>									" +
                "    		<favourite-album>Album1</favourite-album>			" +
                "   		<favourite-song>Last Christmas</favourite-song>		" +
                "  	</music>												" +
                "		<current-alone>											" +
                "			<current-leaf>Album1</current-leaf>					" +
                "			<current-alone-leaf>Album1</current-alone-leaf>		" +
                "		</current-alone>										" +
                "	</leaf-ref>													" +
                "</validation>													";
        getModelNode();
        EditConfigRequest request1 = createRequestFromString(requestXml1);
        request1.setMessageId("1");
        NetConfResponse response1 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request1, response1);
        assertTrue(response1.isOk());

        String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>					" +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">			" +
                "    <leaf-ref>												" +
                "		<current-alone> 										" +
                "			<current-parent-leaf>Album1</current-parent-leaf>	" +
                "			<current-leaf-list>Test1</current-leaf-list>		" +
                "			<current-leaf-list>Test2</current-leaf-list>		" +
                "		</current-alone>										" +
                "	  </leaf-ref> 												" +
                "</validation>													";
        getModelNode();
        EditConfigRequest request2 = createRequestFromString(requestXml2);
        request2.setMessageId("1");
        NetConfResponse response2 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request2, response2);
        assertTrue(response2.isOk());
    }

    @Test
    public void testValidCount() throws ModelNodeInitException {
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "	<count-validation> " +
                "		<countable>8</countable>" +
                "		<count-list>" +
                "			<leaf1>10</leaf1>" +
                "		</count-list> " +
                "      <count-list>" +
                "          <leaf1>20</leaf1>" +
                "      </count-list> " +
                "	</count-validation> " +
                "</validation>";

        getModelNode();
        EditConfigRequest request1 = createRequestFromString(requestXml1);
        request1.setMessageId("1");
        NetConfResponse response1 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request1, response1);
        assertFalse(response1.isOk());
        assertNull(response1.getData());
        assertEquals("Violate when constraints: count(count-list) = 1", response1.getErrors().get(0).getErrorMessage());
        String expectedPath1 = "/validation:validation/validation:count-validation/validation:count-list[validation" +
                ":leaf1=10]/validation:leaf1";
        String expectedPath2 = "/validation:validation/validation:count-validation/validation:count-list[validation" +
                ":leaf1=20]/validation:leaf1";
        String errorPath = response1.getErrors().get(0).getErrorPath();
        // changeNodeMap contains a set of EditContainmentNodes, so which one will be used for the error is
        // unpredictable
        assertTrue(expectedPath1.equals(errorPath) || expectedPath2.equals(errorPath));
    }

    @Test
    public void testValidCount1() throws Exception {
        String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "  <count-validation> " +
                "      <countable>8</countable>" +
                "      <value2>0</value2>" +
                "      <count-list>" +
                "          <leaf1>11</leaf1>" +
                "      </count-list> " +
                "  </count-validation> " +
                "</validation>";

        getModelNode();
        EditConfigRequest request2 = createRequestFromString(requestXml2);
        request2.setMessageId("1");
        NetConfResponse response2 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request2, response2);
        assertFalse(response2.isOk());
        assertNull(response2.getData());
        assertEquals("Violate when constraints: count(countable) = 0", response2.getErrors().get(0).getErrorMessage());

    }

    @Test
    public void testWhenDeleteNode() throws ModelNodeInitException {
        String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "  <count-validation> " +
                "      <value2>0</value2>" +
                "      <count-list>" +
                "          <leaf1>11</leaf1>" +
                "      </count-list> " +
                "  </count-validation> " +
                "</validation>";

        getModelNode();
        EditConfigRequest request2 = createRequestFromString(requestXml2);
        request2.setMessageId("1");
        NetConfResponse response2 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request2, response2);
        assertTrue(response2.isOk());

        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "  <count-validation> " +
                "    <countable>8</countable>" +
                "  </count-validation> " +
                "</validation>";

        getModelNode();
        EditConfigRequest request1 = createRequestFromString(requestXml1);
        request1.setMessageId("1");
        NetConfResponse response1 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request1, response1);
        assertTrue(response1.isOk());

    }

    @Test
    public void testSelfCountList() throws ModelNodeInitException {
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "	<count-validation> " +
                "		<count-list>" +
                "			<leaf1>10</leaf1>" +
                "		</count-list> " +
                "	</count-validation> " +
                "</validation>";

        getModelNode();
        editConfig(m_server, m_clientInfo, requestXml1, true);

        String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "	<count-validation> " +
                "		<count-list>" +
                "			<leaf1>10</leaf1>" +
                "			<leaf2>11</leaf2>" +
                "		</count-list> " +
                "	</count-validation> " +
                "</validation>";

        getModelNode();
        editConfig(m_server, m_clientInfo, requestXml2, true);


    }

    @Test
    public void testIdentity() throws ModelNodeInitException {
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">" +
                "<validation:identity-validation>" +
                "		<validation:leaf1>identity1</validation:leaf1> " +
                "	</validation:identity-validation> " +
                " </validation:validation>";

        getModelNode();
        EditConfigRequest request1 = createRequestFromString(requestXml1);
        request1.setMessageId("1");
        NetConfResponse response1 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request1, response1);
        assertTrue(response1.isOk());

        String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "<identity-validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "		<leaf2>1</leaf2> " +
                "	</identity-validation> " +
                " </validation>";

        EditConfigRequest request2 = createRequestFromString(requestXml2);
        request2.setMessageId("1");
        NetConfResponse response2 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request2, response2);
        assertTrue(response2.isOk());
    }

    @Test
    public void testNotEqualOperator2() throws ModelNodeInitException {
        String requestXml3 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "	<when-validation> " +
                "		<result-leaf>10</result-leaf> " +
                "	</when-validation> " +
                "</validation>";

        getModelNode();
        EditConfigRequest request3 = createRequestFromString(requestXml3);
        request3.setMessageId("1");
        NetConfResponse response3 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request3, response3);
        assertTrue(response3.isOk());

        String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "	<when-validation> " +
                "		<not-equal>test1</not-equal> " +
                "	</when-validation> " +
                "</validation>";

        EditConfigRequest request2 = createRequestFromString(requestXml2);
        request2.setMessageId("1");
        NetConfResponse response2 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request2, response2);
        assertFalse(response2.isOk());
        assertTrue(response2.getErrors().get(0).getErrorMessage().contains("Violate when constraints"));
        assertTrue(response2.getErrors().get(0).getErrorPath()
                .equals("/validation:validation/validation:when-validation/validation:not-equal"));

    }

    @Test
    public void testArthimeticFailOperation() throws ModelNodeInitException {
        /// Add first leaf
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "	<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "  	<arithmetic-validation> " +
                "		 	<value1>15</value1> " +
                "	    </arithmetic-validation> " +
                "</validation>";

        getModelNode();
        EditConfigRequest request1 = createRequestFromString(requestXml1);
        request1.setMessageId("1");
        NetConfResponse response1 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request1, response1);
        assertTrue(response1.isOk());

        // add leaf to check + operator
        String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "	<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "		<arithmetic-validation> " +
                "			<fail-must-leaf >15</fail-must-leaf > " +
                "		</arithmetic-validation> " +
                "  </validation>";

        getModelNode();
        EditConfigRequest request2 = createRequestFromString(requestXml2);
        request2.setMessageId("1");
        NetConfResponse response2 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request2, response2);
        assertFalse(response2.isOk());
        assertTrue(response2.getErrors().get(0).getErrorMessage().contains("Violate must constraints: ../value1 + 10 " +
                "< 0"));
        assertNull(response2.getData());

    }

    @Test
    public void testArthimeticOperation() throws ModelNodeInitException {
        /// Add first leaf
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "	<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "  	<arithmetic-validation> " +
                "		 	<value1>15</value1> " +
                "	    </arithmetic-validation> " +
                "</validation>";

        getModelNode();
        EditConfigRequest request1 = createRequestFromString(requestXml1);
        request1.setMessageId("1");
        NetConfResponse response1 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request1, response1);
        assertTrue(response1.isOk());

        // add leaf to check + operator
        String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "	<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "		<arithmetic-validation> " +
                "			<value2>15</value2> " +
                "		</arithmetic-validation> " +
                "  </validation>";

        getModelNode();
        EditConfigRequest request2 = createRequestFromString(requestXml2);
        request2.setMessageId("1");
        NetConfResponse response2 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request2, response2);
        assertTrue(response2.isOk());

        // add leaf to check - operator
        String requestXml3 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "	<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "		<arithmetic-validation> " +
                "			<value3>15</value3> " +
                "		</arithmetic-validation> " +
                "  </validation>";

        getModelNode();
        EditConfigRequest request3 = createRequestFromString(requestXml3);
        request3.setMessageId("1");
        NetConfResponse response3 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request3, response3);
        assertTrue(response3.isOk());

        // add leaf to check * operator
        String requestXml4 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "	<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "		<arithmetic-validation> " +
                "			<value4>45</value4> " +
                "		</arithmetic-validation> " +
                "  </validation>";

        getModelNode();
        EditConfigRequest request4 = createRequestFromString(requestXml4);
        request4.setMessageId("1");
        NetConfResponse response4 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request4, response4);
        assertTrue(response4.isOk());

        // add leaf to check div operator
        String requestXml5 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "	<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "		<arithmetic-validation> " +
                "			<value5>15</value5> " +
                "		</arithmetic-validation> " +
                "  </validation>";

        getModelNode();
        EditConfigRequest request5 = createRequestFromString(requestXml5);
        request5.setMessageId("1");
        NetConfResponse response5 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request5, response5);
        assertTrue(response5.isOk());


    }

    @Test
    public void testAllArithmeticOperator() throws ModelNodeInitException {
        /// Add first leaf
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "	<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "  	<arithmetic-validation> " +
                "		 	<value1>15</value1> " +
                "	    </arithmetic-validation> " +
                "</validation>";

        getModelNode();
        EditConfigRequest request1 = createRequestFromString(requestXml1);
        request1.setMessageId("1");
        NetConfResponse response1 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request1, response1);
        assertTrue(response1.isOk());

        //add leaf to check mod operator
        String requestXml6 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "	<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "		<arithmetic-validation> " +
                "			<all-arith-leaf>10</all-arith-leaf>" +
                "			<all-must-leaf>10</all-must-leaf>" +
                "			<abs-leaf>10</abs-leaf>" +
                "			<mod-leaf>15</mod-leaf> " +
                "		</arithmetic-validation> " +
                "  </validation>";

        getModelNode();
        EditConfigRequest request6 = createRequestFromString(requestXml6);
        request6.setMessageId("1");
        NetConfResponse response6 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request6, response6);
        assertTrue(response6.isOk());

    }

    @Test
    public void testCrossReferencePath() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "  <validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "    <validation>validation</validation>" +
                "    <crossConstant>10</crossConstant>" +
                "    <constantCheck>10</constantCheck>" +
                "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);
    }

    @Test
    public void testNotEqualOperator() throws ModelNodeInitException {
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "	<when-validation> " +
                "		<result-leaf>15</result-leaf> " +
                "	</when-validation> " +
                "</validation>";

        getModelNode();
        EditConfigRequest request1 = createRequestFromString(requestXml1);
        request1.setMessageId("1");
        NetConfResponse response1 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request1, response1);
        assertTrue(response1.isOk());

        String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "	<when-validation> " +
                "		<not-equal>test1</not-equal> " +
                "	</when-validation> " +
                "</validation>";

        getModelNode();
        EditConfigRequest request2 = createRequestFromString(requestXml2);
        request2.setMessageId("1");
        NetConfResponse response2 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request2, response2);
        assertTrue(response2.isOk());
        assertNull(response2.getData());

    }

    @Test
    public void testInvalidLeafRefWithCurrentAlone() throws ModelNodeInitException {
        testMatchedLeafRefValueWithCurrent();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>					" +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">			" +
                "    <leaf-ref> 												" +
                "    	<album> 												" +
                "    		<name>Album2</name> 								" +
                "	    	<song>												" +
                "	    		<name>Last Christmas</name>						" +
                "	    		<artist-name>LENY</artist-name>					" +
                "	    	</song>												" +
                "	 		<song-count>0</song-count>							" +
                "       </album>												" +
                "	  </leaf-ref> 												" +
                "</validation>													";
        getModelNode();
        EditConfigRequest request1 = createRequestFromString(requestXml1);
        request1.setMessageId("1");
        NetConfResponse response1 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request1, response1);
        assertTrue(response1.isOk());

        String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>					" +
                "<validation xmlns=\"urn:org:bbf:pma:validation\"> 		" +
                "    <leaf-ref> 												" +
                "		<current-alone>											" +
                "			<current-parent-leaf>Album2</current-parent-leaf>	" +
                "			<current-leaf-list>Test1</current-leaf-list>		" +
                "			<current-leaf-list>Test2</current-leaf-list>		" +
                "		</current-alone> 										" +
                "	  </leaf-ref>												" +
                "</validation>";
        getModelNode();
        EditConfigRequest request2 = createRequestFromString(requestXml2);
        request2.setMessageId("1");
        NetConfResponse response2 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request2, response2);
        assertFalse(response2.isOk());
    }

    @Test
    public void testInvalidInstanceIdentifierForNotExistingContainer() throws ModelNodeInitException {
        testFail("/datastorevalidatortest/instanceidentifervalidation/defaultxml/invalid-instance-identifier" +
                        "-container.xml", NetconfRpcErrorTag.DATA_MISSING, NetconfRpcErrorType.Application, NetconfRpcErrorSeverity
                        .Error, "instance-required", "Missing required element /validation/instance-identifier-example/abc",
                "/validation:validation/validation:instance-identifier-example/validation:student[validation:student-id" +
                        "=ST001]/validation:student-instance-identifier1");
    }

    @Test
    public void testValidInstanceIdentifierWithContainerPath() throws ModelNodeInitException {
        testPass("/datastorevalidatortest/instanceidentifervalidation/defaultxml/valid-instance-identifier-container" +
                ".xml");
    }

    @Test
    public void testValidInstanceIdentifierForListPath() throws ModelNodeInitException {
        testPass("/datastorevalidatortest/instanceidentifervalidation/defaultxml/valid-instance-identifier-list.xml");
    }

    @Test
    public void testValidInstanceIdentifierForListLeafPath() throws ModelNodeInitException {
        testPass("/datastorevalidatortest/instanceidentifervalidation/defaultxml/valid-instance-identifier-leaf-of" +
                "-list.xml");
    }

    @Test
    public void testInvalidInstanceIdentifierForLeafInList() throws ModelNodeInitException {
        testFail("/datastorevalidatortest/instanceidentifervalidation/defaultxml/invalid-instance-identifier-leaf-of" +
                        "-list.xml", NetconfRpcErrorTag.DATA_MISSING, NetconfRpcErrorType.Application, NetconfRpcErrorSeverity
                        .Error, "instance-required", "Missing required element " +
                        "/validation/instance-identifier-example/subject[subject-id = 'SJ001']/subject-name",
                "/validation:validation/validation:instance-identifier-example/validation:student[validation:student-id" +
                        "=ST001]/validation:student-instance-identifier1");
    }

    @Test
    public void testInvalidInstanceIdentifierWithRequireInstance() throws ModelNodeInitException {
        testFail("/datastorevalidatortest/instanceidentifervalidation/defaultxml/invalid-instance-identifier-with" +
                        "-require-instance.xml",
                NetconfRpcErrorTag.DATA_MISSING, NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error,
                "instance-required",
                "Missing required element /validation/instance-identifier-example/address/national[national-id=BN]",
                "/validation:validation/validation:instance-identifier-example/validation:student[validation:student" +
                        "-id=ST001]/validation:student-instance-identifier1");
    }

    @Test
    public void testValidInstanceIdentifierListIncludedMultipleKeys() throws ModelNodeInitException {
        String requestXml1 = "/datastorevalidatortest/instanceidentifervalidation/defaultxml/valid-instance" +
                "-identifier-list-with-two-keys.xml";
        getModelNode();
        EditConfigRequest request1 = createRequest(requestXml1);
        request1.setMessageId("1");
        NetConfResponse response1 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request1, response1);

        assertTrue(response1.isOk());

        testPass("/datastorevalidatortest/instanceidentifervalidation/defaultxml/create-instance-identifier-list-with" +
                "-two-keys.xml");
    }

    @Test
    public void testValidInstanceIdentifierForLeaf() throws ModelNodeInitException {
        testPass("/datastorevalidatortest/instanceidentifervalidation/defaultxml/valid-instance-identifier-leaf.xml");
    }

    @Test
    public void testInvalidInstanceIdentifierForLeaf() throws ModelNodeInitException {
        testFail("/datastorevalidatortest/instanceidentifervalidation/defaultxml/invalid-instance-identifier-leaf" +
                        ".xml", NetconfRpcErrorTag.DATA_MISSING, NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error,
                "instance-required", "Missing required element " +
                        "/validation/instance-identifier-example/subject[1]/subject-name",
                "/validation:validation/validation:instance-identifier-example/validation:student[validation:student-id" +
                        "=ST001]/validation:student-instance-identifier1");
    }

    @Test
    public void testValidInstanceIdentifierForIndexList() throws ModelNodeInitException {
        testPass("/datastorevalidatortest/instanceidentifervalidation/defaultxml/valid-instance-identifier-index-list" +
                ".xml");
    }

    @Test
    public void testInValidInstanceIdentifierForIndexList() throws ModelNodeInitException {
        testFail("/datastorevalidatortest/instanceidentifervalidation/defaultxml/invalid-instance-identifier-index" +
                        "-list.xml", NetconfRpcErrorTag.DATA_MISSING, NetconfRpcErrorType.Application, NetconfRpcErrorSeverity
                        .Error, "instance-required", "Missing required element " +
                        "/validation/instance-identifier-example/subject[2]/subject-name",
                "/validation:validation/validation:instance-identifier-example/validation:student[validation:student-id" +
                        "=ST001]/validation:student-instance-identifier1");
    }

    @Test
    public void testViolateWhenconstraints() throws ModelNodeInitException {
        String requestXml1 = "/datastorevalidatortest/rangevalidation/defaultxml/invalid-when-constraint-leaf.xml";
        getModelNode();
        EditConfigRequest request1 = createRequest(requestXml1);
        request1.setMessageId("1");
        NetConfResponse response1 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request1, response1);
        testFail("/datastorevalidatortest/rangevalidation/defaultxml/create-when-leaf.xml", NetconfRpcErrorTag
                        .UNKNOWN_ELEMENT,
                NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error, "when-violation",
                "Violate when constraints: ../result-leaf >= 10",
                "/validation:validation/validation:when-validation/validation:leaf-type");
    }

    @Test
    public void testValidWhenconstraints() throws ModelNodeInitException {
        testPass("/datastorevalidatortest/rangevalidation/defaultxml/valid-when-constraint-leaf.xml");
    }

    @Test
    public void testValidWhenconstraintsOnAbsoluteLeaf() throws ModelNodeInitException {

        String requestXml1 = "/datastorevalidatortest/rangevalidation/defaultxml/valid-when-constraint-leaf.xml";
        getModelNode();
        EditConfigRequest request1 = createRequest(requestXml1);
        request1.setMessageId("1");
        NetConfResponse response1 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request1, response1);

        // single condition
        testPass("/datastorevalidatortest/rangevalidation/defaultxml/valid-when-constraint-absolute-leaf2.xml");

        //multiple condition and
        testPass("/datastorevalidatortest/rangevalidation/defaultxml/valid-when-constraint-absolute-leaf.xml");

        //multiple condition or
        testPass("/datastorevalidatortest/rangevalidation/defaultxml/valid-when-constraint-absolute-leaf3.xml");
    }

    @Test
    public void testValidMustConstraint() throws ModelNodeInitException {
        testPass("/datastorevalidatortest/rangevalidation/defaultxml/valid-must-constraint-leaf.xml");
    }

    @Test
    public void testViolateMustConstraint() throws ModelNodeInitException {
        testFail("/datastorevalidatortest/rangevalidation/defaultxml/invalid-must-constraint-leaf.xml",
                NetconfRpcErrorTag.OPERATION_FAILED,
                NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error, "must-violation",
                "An MTU must be  100 .. 5000",
                "/validation:validation/validation:must-validation/validation:leaf-type");
    }


    @Test
    public void testViolateWhenConstraintForLeafCaseNode() throws ModelNodeInitException {
        String requestXml1 = "/datastorevalidatortest/rangevalidation/defaultxml/invalid-when-constraint-leaf" +
                "-casenode-1.xml";
        getModelNode();
        EditConfigRequest request1 = createRequest(requestXml1);
        request1.setMessageId("1");
        NetConfResponse response1 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request1, response1);

        testFail("/datastorevalidatortest/rangevalidation/defaultxml/create-when-constraint-leaf-casenode-1.xml",
                NetconfRpcErrorTag.UNKNOWN_ELEMENT,
                NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error, "when-violation",
                "Violate when constraints: ../../validation:result-choice = 'success'",
                "/validation:validation/validation:when-validation/validation:choicecase/validation:leaf-case-success");

        requestXml1 = "/datastorevalidatortest/rangevalidation/defaultxml/invalid-when-constraint-leaf-casenode-2.xml";
        getModelNode();
        request1 = createRequest(requestXml1);
        request1.setMessageId("1");
        response1 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request1, response1);

        testFail("/datastorevalidatortest/rangevalidation/defaultxml/create-when-constraint-leaf-casenode-2.xml",
                NetconfRpcErrorTag.UNKNOWN_ELEMENT,
                NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error, "when-violation",
                "Violate when constraints: ../../validation:result-choice = 'failed'",
                "/validation:validation/validation:when-validation/validation:choicecase/validation:leaf-case-failed");
    }


    @Test
    public void testValidWhenConstraintForSuccessLeafCase() throws ModelNodeInitException {
        String requestXml1 = "/datastorevalidatortest/rangevalidation/defaultxml/valid-when-constraint-leaf-casenode" +
                "-1.xml";
        getModelNode();
        EditConfigRequest request1 = createRequest(requestXml1);
        request1.setMessageId("1");
        NetConfResponse response1 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request1, response1);

        assertTrue(response1.isOk());

        testPass("/datastorevalidatortest/rangevalidation/defaultxml/create-when-constraint-leaf-casenode-1.xml");
    }

    @Test
    public void testValidWhenConstraintForFailLeafCase() throws ModelNodeInitException {
        String requestXml1 = "/datastorevalidatortest/rangevalidation/defaultxml/valid-when-constraint-leaf-casenode" +
                "-2.xml";
        getModelNode();
        EditConfigRequest request1 = createRequest(requestXml1);
        request1.setMessageId("1");
        NetConfResponse response1 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request1, response1);

        assertTrue(response1.isOk());
        testPass("/datastorevalidatortest/rangevalidation/defaultxml/create-when-constraint-leaf-casenode-2.xml");
    }

    @Test
    public void testValidateLeafWithSameName() throws ModelNodeInitException {
        String requestXml = "/datastorevalidatortest/leafrefvalidation/defaultxml/valid-leaf-with-same-name.xml";
        getModelNode();
        EditConfigRequest request = createRequest(requestXml);
        request.setMessageId("1");
        NetConfResponse response = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request, response);

        assertTrue(response.isOk());
        testPass("/datastorevalidatortest/leafrefvalidation/defaultxml/valid-leafref-with-same-name.xml");
    }

    @Test
    public void testInvalidLeafRefWithSameName() throws ModelNodeInitException {
        String requestXml = "/datastorevalidatortest/leafrefvalidation/defaultxml/valid-leaf-with-same-name.xml";
        getModelNode();
        EditConfigRequest request = createRequest(requestXml);
        request.setMessageId("1");
        NetConfResponse response = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request, response);

        assertTrue(response.isOk());
        testFail("/datastorevalidatortest/leafrefvalidation/defaultxml/valid-invalid-leafref-with-same-name.xml",
                NetconfRpcErrorTag.DATA_MISSING, NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error,
                "instance-required",
                "Instance required with value - validation1", "/validation:validation/validation:leafref-validation");
    }

    @Override
    protected void initialiseInterceptor() {
        m_addDefaultDataInterceptor = null;
    }

    @Test
    public void testValidAbsoluteWhenConstraintForSuccessLeafCase() throws ModelNodeInitException {
        String requestXml1 = "/datastorevalidatortest/rangevalidation/defaultxml/valid-when-constraint-leaf-casenode" +
                "-1.xml";
        getModelNode();
        EditConfigRequest request1 = createRequest(requestXml1);
        request1.setMessageId("1");
        NetConfResponse response1 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request1, response1);

        assertTrue(response1.isOk());

        testPass("/datastorevalidatortest/rangevalidation/defaultxml/create-when-constraint-leaf-abs-casenode-1.xml");
    }

    @Test
    public void testValidAbsoluteWhenConstraintReferringACase() throws ModelNodeInitException {
        String requestXml1 = "/datastorevalidatortest/rangevalidation/defaultxml/valid-when-constraint-abs-leaflist" +
                "-casenode-1.xml";
        getModelNode();
        EditConfigRequest request1 = createRequest(requestXml1);
        request1.setMessageId("1");
        NetConfResponse response1 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request1, response1);

        assertTrue(response1.isOk());

        testPass("/datastorevalidatortest/rangevalidation/defaultxml/create-when-constraint-leaf-abs-casenode-2.xml");
    }

    @Test
    public void testInValidAbsoluteWhenConstraintForSuccessLeafCase() throws ModelNodeInitException {
        String requestXml1 = "/datastorevalidatortest/rangevalidation/defaultxml/valid-when-constraint-leaf-casenode" +
                "-2.xml";
        getModelNode();
        EditConfigRequest request1 = createRequest(requestXml1);
        request1.setMessageId("1");
        NetConfResponse response1 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request1, response1);

        assertTrue(response1.isOk());

        testFail("/datastorevalidatortest/rangevalidation/defaultxml/create-when-constraint-leaf-abs-casenode-1.xml",
                NetconfRpcErrorTag.UNKNOWN_ELEMENT, NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error,
                "when-violation", "Violate when constraints: /validation/when-validation/choicecase/result-choice = " +
                        "'success'",
                "/validation:validation/validation:when-validation/validation:choicecase/validation:absolute-case" +
                        "-success");
    }

    @Test
    public void testValidWhenconstraintsOnNotEqualsLeaf() throws ModelNodeInitException {

        String requestXml1 = "/datastorevalidatortest/rangevalidation/defaultxml/valid-when-constraint-leaf.xml";
        getModelNode();
        EditConfigRequest request1 = createRequest(requestXml1);
        request1.setMessageId("1");
        NetConfResponse response1 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request1, response1);

        // single condition
        testPass("/datastorevalidatortest/rangevalidation/defaultxml/valid-when-constraint-not-equal-validation1.xml");

        //multiple condition and
        testPass("/datastorevalidatortest/rangevalidation/defaultxml/valid-when-constraint-not-equal-validation2.xml");

        //multiple condition or
        testFail("/datastorevalidatortest/rangevalidation/defaultxml/valid-when-constraint-not-equal-validation3.xml",
                NetconfRpcErrorTag.UNKNOWN_ELEMENT, NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error,
                "when-violation", "Violate when constraints: ../result-leaf != 0 and " +
                        "/validation/when-validation/result-leaf != 15",
                "/validation:validation/validation:when-validation/validation:NotEqualLeaf3");
    }

    @Test
    public void testRootContinerOnListAddition() throws Exception {
        RequestScope.setEnableThreadLocalInUT(true);
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation1 xmlns=\"urn:org:bbf:pma:validation\">" +
                "  <leaf1>leaf1</leaf1>" +
                "</validation1>";
        getModelNode();
        editConfig(m_server, m_clientInfo, requestXml1, true);

        String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation1 xmlns=\"urn:org:bbf:pma:validation\">" +
                "  <list1>" +
                "    <key1>key1</key1>" +
                "  </list1>" +
                "</validation1>";

        editConfig(m_server, m_clientInfo, requestXml2, true);

        String requestXml3 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation1 xmlns=\"urn:org:bbf:pma:validation\">" +
                "  <list1>" +
                "    <key1>key2</key1>" +
                "  </list1>" +
                "</validation1>";
        NetConfResponse response3 = editConfig(m_server, m_clientInfo, requestXml3, false);

        assertFalse(response3.isOk());
        assertEquals("Violate must constraints: count(list1) <= 1", response3.getErrors().get(0).getErrorMessage());
        assertEquals("/validation:validation1", response3.getErrors().get(0).getErrorPath());

        RequestScope.setEnableThreadLocalInUT(false);
    }

    @Test
    public void testMustConstraintOnListChildNode() throws Exception {
        RequestScope.setEnableThreadLocalInUT(true);
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation2 xmlns=\"urn:org:bbf:pma:validation\">" +
                "  <list1>" +
                "    <key1>key1</key1>" +
                "    <enabled>true</enabled>" +
                "  </list1>" +
                "</validation2>";
        getModelNode();
        editConfig(m_server, m_clientInfo, requestXml1, true);

        String response = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\"/>"
                + "  <validation:validation2 xmlns:validation=\"urn:org:bbf:pma:validation\">"
                + "    <validation:list1>"
                + "     <validation:key1>key1</validation:key1>"
                + "     <validation:enabled>true</validation:enabled>"
                + "    </validation:list1>"
                + "  </validation:validation2>"
                + " </data>"
                + "</rpc-reply>";
        verifyGet(response);

        String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation2 xmlns=\"urn:org:bbf:pma:validation\">" +
                "  <list1>" +
                "    <key1>key2</key1>" +
                "    <enabled>true</enabled>" +
                "  </list1>" +
                "</validation2>";

        NetConfResponse response2 = editConfig(m_server, m_clientInfo, requestXml2, false);

        assertFalse(response2.isOk());
        assertEquals("Violate must constraints: count(list1[enabled='true']) <= 1", response2.getErrors().get(0)
                .getErrorMessage());
        assertEquals("/validation:validation2", response2.getErrors().get(0).getErrorPath());

        String requestXml3 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation2 xmlns=\"urn:org:bbf:pma:validation\">" +
                "  <list1>" +
                "    <key1>key2</key1>" +
                "    <enabled>false</enabled>" +
                "  </list1>" +
                "</validation2>";
        editConfig(m_server, m_clientInfo, requestXml3, true);

        RequestScope.setEnableThreadLocalInUT(false);
    }

    @Test
    public void testMustConstraintOnListChildNodeDepthCase() throws Exception {
        RequestScope.setEnableThreadLocalInUT(true);
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation3 xmlns=\"urn:org:bbf:pma:validation\">" +
                "  <list1>" +
                "    <key1>key1</key1>" +
                "    <container1>" +
                "    <list2>" +
                "    <key2>key2</key2>" +
                "    <enabled>true</enabled>" +
                "    </list2>" +
                "    </container1>" +
                "  </list1>" +
                "</validation3>";
        getModelNode();
        editConfig(m_server, m_clientInfo, requestXml1, true);

        String response = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\"/>"
                + "  <validation:validation3 xmlns:validation=\"urn:org:bbf:pma:validation\">"
                + "    <validation:list1>"
                + "     <validation:key1>key1</validation:key1>"
                + "     <validation:container1>"
                + "      <validation:list2>"
                + "       <validation:key2>key2</validation:key2>"
                + "       <validation:enabled>true</validation:enabled>"
                + "      </validation:list2>"
                + "     </validation:container1>"
                + "    </validation:list1>"
                + "  </validation:validation3>"
                + " </data>"
                + "</rpc-reply>";
        verifyGet(response);

        String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation3 xmlns=\"urn:org:bbf:pma:validation\">" +
                "  <list1>" +
                "    <key1>key1</key1>" +
                "    <container1>" +
                "    <list2>" +
                "    <key2>key3</key2>" +
                "    <enabled>true</enabled>" +
                "    </list2>" +
                "    </container1>" +
                "  </list1>" +
                "</validation3>";

        NetConfResponse response2 = editConfig(m_server, m_clientInfo, requestXml2, false);

        assertFalse(response2.isOk());
        assertEquals("Violate must constraints: count(../container1/list2[enabled='true']) <= 1", response2.getErrors
                ().get(0).getErrorMessage());
        assertEquals("/validation:validation3/validation:list1[validation:key1=key1]/validation:container1",
                response2.getErrors().get(0).getErrorPath());

        String requestXml3 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation3 xmlns=\"urn:org:bbf:pma:validation\">" +
                "  <list1>" +
                "    <key1>key1</key1>" +
                "    <container1>" +
                "    <list2>" +
                "    <key2>key3</key2>" +
                "    <enabled>false</enabled>" +
                "    </list2>" +
                "    </container1>" +
                "  </list1>" +
                "</validation3>";
        editConfig(m_server, m_clientInfo, requestXml3, true);

        RequestScope.setEnableThreadLocalInUT(false);
    }

    @Test
    public void testDefaultLeafCreationOnWhen() throws Exception {

        RequestScope.setEnableThreadLocalInUT(true);
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "  <validation>default</validation>" +
                "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);

        String response = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
                + "   <validation:validation>default</validation:validation>"
                + "   <validation:defaultLeaf>1</validation:defaultLeaf>"
                + "  </validation:validation>"
                + " </data>"
                + "</rpc-reply>";
        verifyGet(response);

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">" +
                "  <validation>default1</validation>" +
                "</validation>"
        ;
        editConfig(m_server, m_clientInfo, requestXml1, true);

        response = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
                + "   <validation:validation>default1</validation:validation>"
                + "   <validation:defaultLeaf1>2</validation:defaultLeaf1>"
                + "  </validation:validation>"
                + " </data>"
                + "</rpc-reply>"
        ;
        verifyGet(response);

    }

    @Test
    public void testCurrentAloneInMultiList() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + "<leaf-ref>"
                + " <current-alone>"
                + "  <current-alone-list>"
                + "    <key>10</key>"
                + "    <current-alone>11</current-alone>"
                + "  </current-alone-list>"
                + " </current-alone>"
                + "</leaf-ref>"
                + "</validation>";
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals("Violate when constraints: ../../current-alone-list[current()]/current-alone = .",
                response.getErrors().get(0).getErrorMessage());
    }

    @Test
    public void testCurrentAloneInMissingList() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + "<leaf-ref>"
                + " <current-alone>"
                + "  <current-alone-list-leaf>10</current-alone-list-leaf>"
                + " </current-alone>"
                + "</leaf-ref>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, false);
    }

    @Test
    public void testSymbolLeaf() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + " <validation>validation</validation>"
                + " <symbol-leaf-1.1_2>validation</symbol-leaf-1.1_2>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + " <validation>validation1</validation>"
                + " <symbol-leaf-1.1_2>validation</symbol-leaf-1.1_2>"
                + "</validation>"
        ;
        editConfig(m_server, m_clientInfo, requestXml1, false);
    }

    @Test
    public void testDefaultWhenCreationNonPresenceContainer() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + " <validation>default-when</validation>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);
        String response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + " <data>"
                        + "  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
                        + "   <validation:default-when-validation>"
                        + "    <validation:class>10</validation:class>"
                        + "   </validation:default-when-validation>"
                        + "  <validation:validation>default-when</validation:validation>"
                        + " </validation:validation>"
                        + "</data>"
                        + "</rpc-reply>";
        verifyGet(response);

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\" " +
                "xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <validation xc:operation='delete'>default-when</validation>"
                + "</validation>"
        ;
        editConfig(m_server, m_clientInfo, requestXml1, true);

        response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + " <data>"
                        + "  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
                        + " </validation:validation>"
                        + "</data>"
                        + "</rpc-reply>"
        ;
        verifyGet(response);
    }

    @Test
    public void testMandatoryLeafInNonPresenceContainer() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + " <validation>mandatory</validation>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + " <mandatory-validation-container/>"
                + "</validation>"
        ;
        NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals("Missing mandatory node", ncResponse.getErrors().get(0).getErrorMessage());
        assertEquals("/validation:validation/validation:mandatory-validation-container/validation:leafValidation" +
                        "/validation:leaf1",
                ncResponse.getErrors().get(0).getErrorPath());
        assertEquals(NetconfRpcErrorTag.DATA_MISSING, ncResponse.getErrors().get(0).getErrorTag());

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + " <mandatory-validation-container>"
                + "   <leafValidation>"
                + "     <leaf1>0</leaf1>"
                + "   </leafValidation>"
                + " </mandatory-validation-container>"
                + "</validation>"
        ;
        ncResponse = editConfig(m_server, m_clientInfo, requestXml1, true);
        String response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + " <data>"
                        + "  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
                        + "   <validation:mandatory-validation-container>"
                        + "    <validation:leafValidation>"
                        + "     <validation:leaf1>0</validation:leaf1>"
                        + "     <validation:leafDefault>0</validation:leafDefault>"
                        + "    </validation:leafValidation>"
                        + "   </validation:mandatory-validation-container>"
                        + "  <validation:validation>mandatory</validation:validation>"
                        + " </validation:validation>"
                        + "</data>"
                        + "</rpc-reply>";
        verifyGet(response);

    }

    @Test
    public void testForParentContainerDeletionWithAttributes() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + " <validate-parent-container-on-when-deletion>"
                + "  <leaf1>10</leaf1>"
                + "  <for-leaf>"
                + "   <leaf1>0</leaf1>"
                + "   <leaf2>0</leaf2>"
                + "   <innerContainer>"
                + "    <leaf1>0</leaf1>"
                + "   </innerContainer>"
                + "  </for-leaf>"
                + " </validate-parent-container-on-when-deletion>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\" " +
                "xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <validate-parent-container-on-when-deletion>"
                + "  <leaf1>0</leaf1>"
                + "  <for-leaf>"
                + "   <leaf2 xc:operation='delete'>0</leaf2>"
                + "  </for-leaf>"
                + " </validate-parent-container-on-when-deletion>"
                + "</validation>"
        ;
        editConfig(m_server, m_clientInfo, requestXml1, true);

        String response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + " <data>"
                        + " <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
                        + "  <validation:validate-parent-container-on-when-deletion>"
                        + "   <validation:for-leaf>"
                        + "    <validation:leaf1>0</validation:leaf1>"
                        + "   </validation:for-leaf>"
                        + "   <validation:leaf1>0</validation:leaf1>"
                        + "  </validation:validate-parent-container-on-when-deletion>"
                        + " </validation:validation>"
                        + "</data>"
                        + "</rpc-reply>";
        verifyGet(response);

    }

    @Test
    public void testDeleteOnLeafRef() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + " <leaf-ref>"
                + "  <album>"
                + "   <name>name</name>"
                + "  </album>"
                + "  <current-alone>"
                + "   <current-leaf>name</current-leaf>"
                + "  </current-alone>"
                + " </leaf-ref>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\" " +
                "xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <leaf-ref>"
                + "  <album xc:operation='delete'>"
                + "   <name>name</name>"
                + "  </album>"
                + " </leaf-ref>"
                + "</validation>"
        ;
        NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals("/validation:validation/validation:leaf-ref/validation:current-alone/validation:current-leaf",
                ncResponse.getErrors().get(0).getErrorPath());
        assertEquals("instance-required", ncResponse.getErrors().get(0).getErrorAppTag());

    }

    @Test
    public void testTypeValidation() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + "<valueCheck>4294967298</valueCheck>"
                + "</validation>";
        NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals("/validation:validation/validation:valueCheck", ncResponse.getErrors().get(0).getErrorPath());
        assertEquals("range-out-of-specified-bounds", ncResponse.getErrors().get(0).getErrorAppTag());
        assertEquals("The argument is out of bounds <-128, 127>", ncResponse.getErrors().get(0).getErrorMessage());

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + "<valueCheck1>429</valueCheck1>"
                + "</validation>"
        ;
        ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals("/validation:validation/validation:valueCheck1", ncResponse.getErrors().get(0).getErrorPath());
        assertEquals("range-out-of-specified-bounds", ncResponse.getErrors().get(0).getErrorAppTag());
        assertEquals("The argument is out of bounds <-128, 127>", ncResponse.getErrors().get(0).getErrorMessage());

    }

    @Test
    public void testCurrentWithMultipleParentBothCases() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + " <leaf-ref>"
                + "  <album>"
                + "   <name>Album1</name>"
                + "   <song-count>20</song-count>"
                + "  </album>"
                + "  <current-multi-parent>"
                + "    <current-some-leaf>Album1</current-some-leaf>"
                + "    <album-name-list>"
                + "        <name>TestName</name>"
                + "        <current-album-list-leaf>20</current-album-list-leaf>"
                + "    </album-name-list>"
                + "  </current-multi-parent>"
                + " </leaf-ref>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);

        //NegativeCase
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + " <leaf-ref>"
                + "  <album>"
                + "   <name>Album1</name>"
                + "   <song-count>20</song-count>"
                + "  </album>"
                + "  <current-multi-parent>"
                + "    <current-some-leaf>Album1</current-some-leaf>"
                + "    <album-name-list>"
                + "        <name>TestName</name>"
                + "        <current-album-list-leaf>21</current-album-list-leaf>"
                + "    </album-name-list>"
                + "  </current-multi-parent>"
                + " </leaf-ref>"
                + "</validation>";
        NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals("/validation:validation/validation:leaf-ref/validation:current-multi-parent/validation:album" +
                "-name-list[validation:name=TestName]/validation:current-album-list-leaf", ncResponse.getErrors().get(0)
                .getErrorPath());
        assertEquals("instance-required", ncResponse.getErrors().get(0).getErrorAppTag());
        assertEquals("Instance required with value - 21", ncResponse.getErrors().get(0).getErrorMessage());
    }

    @Test
    public void testMultipleCurrentSameLevelBothCases() throws Exception {
        getModelNode();

        //PositiveCase for path "current()/../../name-list[name = current()/../../current-some-leaf]/name-count"
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + " <leaf-ref>"
                + "  <album>"
                + "   <name>Album1</name>"
                + "   <song-count>20</song-count>"
                + "  </album>"
                + "  <current-multi-parent>"
                + "    <current-some-leaf>Album1</current-some-leaf>"
                + "    <name-list>"
                + "      <name>Album1</name>"
                + "      <name-count>1</name-count>"
                + "    </name-list>"
                + "    <album-name-list>"
                + "        <name>TestName</name>"
                + "        <current-album-list-leaf>20</current-album-list-leaf>"
                + "        <two-current-leaf>1</two-current-leaf>"
                + "    </album-name-list>"
                + "  </current-multi-parent>"
                + " </leaf-ref>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);

        //NegativeCase
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + " <leaf-ref>"
                + "  <album>"
                + "   <name>Album1</name>"
                + "   <song-count>20</song-count>"
                + "  </album>"
                + "  <current-multi-parent>"
                + "    <current-some-leaf>Album1</current-some-leaf>"
                + "    <name-list>"
                + "      <name>Album1</name>"
                + "      <name-count>1</name-count>"
                + "    </name-list>"
                + "    <album-name-list>"
                + "        <name>TestName</name>"
                + "        <current-album-list-leaf>20</current-album-list-leaf>"
                + "        <two-current-leaf>2</two-current-leaf>"
                + "    </album-name-list>"
                + "  </current-multi-parent>"
                + " </leaf-ref>"
                + "</validation>";
        NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals(
                "/validation:validation/validation:leaf-ref/validation:current-multi-parent/validation:album-name" +
                        "-list[validation:name=TestName]/validation:two-current-leaf",
                ncResponse.getErrors().get(0).getErrorPath());
        assertEquals("instance-required", ncResponse.getErrors().get(0).getErrorAppTag());
        assertEquals("Instance required with value - 2", ncResponse.getErrors().get(0).getErrorMessage());
    }

    @Test
    public void testNestedCurrentSameLevelBothCases() throws Exception {
        getModelNode();

        //PositiveCase (nested current) for path:
        //"../../../album[name = current()/../../name-list[name = current()
        // /../../current-some-leaf]/name-count]/song-count"
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + " <leaf-ref>"
                + "  <album>"
                + "   <name>1</name>"
                + "   <song-count>20</song-count>"
                + "  </album>"
                + "  <current-multi-parent>"
                + "    <current-some-leaf>InnerAlbum1</current-some-leaf>"
                + "    <name-list>"
                + "      <name>InnerAlbum1</name>"
                + "      <name-count>1</name-count>"
                + "    </name-list>"
                + "    <album-name-list>"
                + "        <name>TestName</name>"
                + "        <nested-current-leaf>20</nested-current-leaf>"
                + "    </album-name-list>"
                + "  </current-multi-parent>"
                + " </leaf-ref>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);

        //NegativeCase
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + " <leaf-ref>"
                + "  <album>"
                + "   <name>1</name>"
                + "   <song-count>20</song-count>"
                + "  </album>"
                + "  <current-multi-parent>"
                + "    <current-some-leaf>InnerAlbum1</current-some-leaf>"
                + "    <name-list>"
                + "      <name>InnerAlbum1</name>"
                + "      <name-count>1</name-count>"
                + "    </name-list>"
                + "    <album-name-list>"
                + "        <name>TestName</name>"
                + "        <nested-current-leaf>21</nested-current-leaf>"
                + "    </album-name-list>"
                + "  </current-multi-parent>"
                + " </leaf-ref>"
                + "</validation>";
        NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals(
                "/validation:validation/validation:leaf-ref/validation:current-multi-parent/validation:album-name" +
                        "-list[validation:name=TestName]/validation:nested-current-leaf",
                ncResponse.getErrors().get(0).getErrorPath());
        assertEquals("instance-required", ncResponse.getErrors().get(0).getErrorAppTag());
        assertEquals("Instance required with value - 21", ncResponse.getErrors().get(0).getErrorMessage());
    }

    @Test
    public void testAbsPathAtContainerSameAsRootName() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + "  <validation>sameNodeAsRootName</validation>"
                + "  <sameContainerAsRoot>"
                + "   <validation>"
                + "    <validation1>sameNodeAsRootName</validation1>"
                + "  </validation>"
                + " </sameContainerAsRoot>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);

    }

    @Test
    public void testSum() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + " <someList>"
                + "  <key>key1</key>"
                + "  <sumValue>4</sumValue>"
                + " </someList>"
                + "</validation>";
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals("Violate must constraints: sum(/validation/someList/sumValue) < 100 and sum" +
                "(../someList/sumValue) > 5", response.getErrors().get(0).getErrorMessage());

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + " <someList>"
                + "  <key>key1</key>"
                + "  <sumValue>10</sumValue>"
                + " </someList>"
                + "</validation>";
        response = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals("Violate must constraints: not(contains(.,10))", response.getErrors().get(0).getErrorMessage());

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + " <someList>"
                + "  <key>key1</key>"
                + "  <sumValue>11</sumValue>"
                + " </someList>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + " <someList>"
                + "  <key>key2</key>"
                + "  <sumValue>111</sumValue>"
                + " </someList>"
                + "</validation>";
        response = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals("Violate must constraints: sum(/validation/someList/sumValue) < 100 and sum" +
                "(../someList/sumValue) > 5", response.getErrors().get(0).getErrorMessage());

    }

    @Test
    public void testMustAsLeafMandatory() throws Exception {
        getModelNode();
        // test violation of must(./) on a leaf
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + " <mustMandatory>"
                + "  <key>key1</key>"
                + " </mustMandatory>"
                + "</validation>";
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals("Violate must constraints: ./mandatoryLeaf", response.getErrors().get(0).getErrorMessage());

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + " <mustMandatory>"
                + "  <key>key1</key>"
                + "  <mandatoryLeaf>4</mandatoryLeaf>"
                + " </mustMandatory>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);

        // test violation of must(./) on a container
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + " <mustMandatory>"
                + "  <key>key1</key>"
                + "  <mustMandatoryContainer/>"
                + " </mustMandatory>"
                + "</validation>";
        response = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals("Violate must constraints: ./mandatoryContainer", response.getErrors().get(0).getErrorMessage());

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + " <mustMandatory>"
                + "  <key>key1</key>"
                + "  <mustMandatoryContainer>"
                + "    <mandatoryContainer>"
                + "      <mandatoryList>"
                + "        <key>key</key>"
                + "      </mandatoryList>"
                + "    </mandatoryContainer>"
                + "  </mustMandatoryContainer>"
                + " </mustMandatory>"
                + "</validation>";
        response = editConfig(m_server, m_clientInfo, requestXml1, true);

        // test violation of must(./) on a leafList
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + " <mustMandatory>"
                + "  <key>key1</key>"
                + "  <mustMandatoryContainer>"
                + "    <mandatoryContainer>"
                + "      <anotherLeaf>leafList</anotherLeaf>"
                + "    </mandatoryContainer>"
                + "  </mustMandatoryContainer>"
                + " </mustMandatory>"
                + "</validation>";
        response = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals("Violate when constraints: ../mandatoryLeafList", response.getErrors().get(0).getErrorMessage());

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + " <mustMandatory>"
                + "  <key>key1</key>"
                + "  <mustMandatoryContainer>"
                + "    <mandatoryContainer>"
                + "      <anotherLeaf>leafList</anotherLeaf>"
                + "    </mandatoryContainer>"
                + "  </mustMandatoryContainer>"
                + " </mustMandatory>"
                + "</validation>";
        response = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals("Violate when constraints: ../mandatoryLeafList", response.getErrors().get(0).getErrorMessage());

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + " <mustMandatory>"
                + "  <key>key1</key>"
                + "  <mustMandatoryContainer>"
                + "    <mandatoryContainer>"
                + "      <mandatoryLeafList>mandatory</mandatoryLeafList>"
                + "      <anotherLeaf>leafList</anotherLeaf>"
                + "    </mandatoryContainer>"
                + "  </mustMandatoryContainer>"
                + " </mustMandatory>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);

        // delete a leaf and the impacted when(./) condition must also be removed/deleted
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + " <mustMandatory>"
                + "  <key>key1</key>"
                + "  <mustMandatoryContainer>"
                + "    <mandatoryContainer xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + "      <mandatoryLeafList xc:operation=\"delete\">mandatory</mandatoryLeafList>"
                + "    </mandatoryContainer>"
                + "  </mustMandatoryContainer>"
                + " </mustMandatory>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);

        String ncResponse =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + " <data>"
                        + "  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
                        + "   <validation:mustMandatory>"
                        + "    <validation:key>key1</validation:key>"
                        + "    <validation:mandatoryLeaf>4</validation:mandatoryLeaf>"
                        + "    <validation:mustMandatoryContainer>"
                        + "     <validation:mandatoryContainer>"
                        + "      <validation:mandatoryList>"
                        + "       <validation:key>key</validation:key>"
                        + "      </validation:mandatoryList>"
                        + "     </validation:mandatoryContainer>"
                        + "    </validation:mustMandatoryContainer>"
                        + "   </validation:mustMandatory>"
                        + "  </validation:validation>"
                        + " </data>"
                        + "</rpc-reply>";
        verifyGet(ncResponse);
    }

    @Test
    public void testBooleanCurrent() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + " <booleanCurrent>"
                + "  <key>key1</key>"
                + "  <leaf1>leaf1</leaf1>"
                + "  <leaf2>true</leaf2>"
                + " </booleanCurrent>"
                + "</validation>";
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, true);

    }

    @Test
    public void testOtherTreeRoot() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + " <validation>123</validation>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation3 xmlns=\"urn:org:bbf:pma:validation\">"
                + " <otherTreeRelativePath>123</otherTreeRelativePath>"
                + "</validation3>";
        editConfig(m_server, m_clientInfo, requestXml1, true);

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\" " +
                "xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <validation xc:operation=\"delete\">123</validation>"
                + "</validation>";
        NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals("Instance required with value - 123", ncResponse.getErrors().get(0).getErrorMessage());

    }

    @Test
    public void testOtherRootFunction() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + " <validation>Other</validation>"
                + " <booleanCurrent>"
                + "  <key>someKey</key>"
                + " </booleanCurrent>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation3 xmlns=\"urn:org:bbf:pma:validation\">"
                + " <someLeaf>someLeaf</someLeaf>"
                + "</validation3>";
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals("Violate must constraints: count(../../validation/booleanCurrent) > 0 "
                        + "and not(contains(../../validation/validation, 'Other'))",
                response.getErrors().get(0).getErrorMessage());

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + " <validation>other</validation>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation3 xmlns=\"urn:org:bbf:pma:validation\">"
                + " <someLeaf>someLeaf</someLeaf>"
                + "</validation3>";
        editConfig(m_server, m_clientInfo, requestXml1, true);

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + " <validation>Other</validation>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, false);

    }

    @Test
    public void testOtherRootWhen() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + " <validation>otherRoot</validation>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation3 xmlns=\"urn:org:bbf:pma:validation\">"
                + " <someLeaf1>otherRoot</someLeaf1>"
                + "</validation3>";
        editConfig(m_server, m_clientInfo, requestXml1, true);

        String response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + " <data>"
                        + "  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
                        + "   <validation:validation>otherRoot</validation:validation>"
                        + "  </validation:validation>"
                        + "  <validation:validation3 xmlns:validation=\"urn:org:bbf:pma:validation\">"
                        + "   <validation:someLeaf1>otherRoot</validation:someLeaf1>"
                        + "  </validation:validation3>"
                        + " </data>"
                        + "</rpc-reply>";
        verifyGet(response);

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + " <validation>otherRoot1</validation>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);

        response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + " <data>"
                        + "  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
                        + "   <validation:validation>otherRoot1</validation:validation>"
                        + "  </validation:validation>"
                        + "  <validation:validation3 xmlns:validation=\"urn:org:bbf:pma:validation\"/>"
                        + " </data>"
                        + "</rpc-reply>"
        ;
        verifyGet(response);

    }

    @Test
    public void testWhenOnParentLocalName() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + "  <when-validation>"
                + "   <when-parent-local-name-leaf>test</when-parent-local-name-leaf>"
                + " </when-validation>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);

    }

    @Test
    public void testWhenOnParentLocalNameChoiceCase() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + "  <when-validation>"
                + "   <choicecase/>"
                + "   <when-parent-local-name-container-leaf>test</when-parent-local-name-container-leaf>"
                + " </when-validation>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);

    }

    @Test
    public void testWhenOnParentName() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + "  <when-validation>"
                + "   <when-parent-name-leaf>test</when-parent-name-leaf>"
                + " </when-validation>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);

    }

    @Test
    public void testWhenOnParentNameChoiceCase() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + "  <when-validation>"
                + "   <choicecase/>"
                + "   <when-parent-name-container-leaf>test</when-parent-name-container-leaf>"
                + " </when-validation>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);

    }

    @Test
    public void testWhenOnParentNamespace() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + "  <when-validation>"
                + "   <when-parent-namespace-leaf>test</when-parent-namespace-leaf>"
                + " </when-validation>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);

    }

    // This is not working yet, so only the above trivial case currently works (tracked by FNMS-20720)
//    @Test
//    public void testWhenOnParentNamespaceChoiceCase() throws Exception {
//        getModelNode();
//        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
//                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
//                + "  <when-validation>"
//                + "   <choicecase/>"
//                + "   <when-parent-namespace-container-leaf>test</when-parent-namespace-container-leaf>"
//                + " </when-validation>"
//                + "</validation>";
//        editConfig(m_server, m_clientInfo, requestXml1, true);
//    	
    @Test
    public void testStringFunction() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + "  <string-function-validation>"
                + "    <number1>42</number1>"
                + "    <string-function-leaf>test</string-function-leaf>"
                + "  </string-function-validation>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + "  <string-function-validation>"
                + "    <number1>41</number1>"
                + "    <string-function-leaf>test</string-function-leaf>"
                + "  </string-function-validation>"
                + "</validation>";
        // should fail
        editConfig(m_server, m_clientInfo, requestXml1, false);

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + "  <string-function-validation>"
                + "    <number1>41</number1>"
                + "  </string-function-validation>"
                + "</validation>";
        // should fail
        editConfig(m_server, m_clientInfo, requestXml1, false);
    }

    @Test
    public void testConcatFunction() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + "  <string-function-validation>"
                + "    <string1>A</string1>"
                + "    <string2>B</string2>"
                + "    <concat-function-leaf>test</concat-function-leaf>"
                + "  </string-function-validation>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + "  <string-function-validation>"
                + "    <string1>A</string1>"
                + "    <string2>C</string2>"
                + "    <concat-function-leaf>test</concat-function-leaf>"
                + "  </string-function-validation>"
                + "</validation>";
        // should fail
        editConfig(m_server, m_clientInfo, requestXml1, false);

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + "  <string-function-validation>"
                + "    <string1>A</string1>"
                + "    <string2>C</string2>"
                + "  </string-function-validation>"
                + "</validation>";
        // should fail
        editConfig(m_server, m_clientInfo, requestXml1, false);
    }

    @Test
    public void testStartsWithFunction() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + "  <string-function-validation>"
                + "    <string1>DSL1</string1>"
                + "    <starts-with-function-leaf>test</starts-with-function-leaf>"
                + "  </string-function-validation>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + "  <string-function-validation>"
                + "    <string1>ITF1</string1>"
                + "    <starts-with-function-leaf>test</starts-with-function-leaf>"
                + "  </string-function-validation>"
                + "</validation>";
        // should fail
        editConfig(m_server, m_clientInfo, requestXml1, false);

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + "  <string-function-validation>"
                + "    <string1>ITF1</string1>"
                + "  </string-function-validation>"
                + "</validation>";
        // should fail
        editConfig(m_server, m_clientInfo, requestXml1, false);
    }

    @Test
    public void testContainsFunction() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + "  <string-function-validation>"
                + "    <string1>TestDSL1</string1>"
                + "    <contains-function-leaf>test</contains-function-leaf>"
                + "  </string-function-validation>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + "  <string-function-validation>"
                + "    <string1>TestITF1</string1>"
                + "    <contains-function-leaf>test</contains-function-leaf>"
                + "  </string-function-validation>"
                + "</validation>";
        // should fail
        editConfig(m_server, m_clientInfo, requestXml1, false);

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + "  <string-function-validation>"
                + "    <string1>TestITF1</string1>"
                + "  </string-function-validation>"
                + "</validation>";
        // should fail
        editConfig(m_server, m_clientInfo, requestXml1, false);
    }

    @Test
    public void testSubstringFunction() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + "  <string-function-validation>"
                + "    <string1>12345</string1>"
                + "    <substring-function-leaf>test</substring-function-leaf>"
                + "  </string-function-validation>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + "  <string-function-validation>"
                + "    <string1>ABCDE</string1>"
                + "    <substring-function-leaf>test</substring-function-leaf>"
                + "  </string-function-validation>"
                + "</validation>";
        // should fail
        editConfig(m_server, m_clientInfo, requestXml1, false);

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + "  <string-function-validation>"
                + "    <string1>ABCDE</string1>"
                + "  </string-function-validation>"
                + "</validation>";
        // should fail
        editConfig(m_server, m_clientInfo, requestXml1, false);
    }

    @Test
    public void testSubstringBeforeFunction() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + "  <string-function-validation>"
                + "    <string1>1999/04/01</string1>"
                + "    <substring-before-function-leaf>test</substring-before-function-leaf>"
                + "  </string-function-validation>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + "  <string-function-validation>"
                + "    <string1>2000/10/05</string1>"
                + "    <substring-before-function-leaf>test</substring-before-function-leaf>"
                + "  </string-function-validation>"
                + "</validation>";
        // should fail
        editConfig(m_server, m_clientInfo, requestXml1, false);

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + "  <string-function-validation>"
                + "    <string1>2000/10/05</string1>"
                + "  </string-function-validation>"
                + "</validation>";
        // should fail
        editConfig(m_server, m_clientInfo, requestXml1, false);
    }

    @Test
    public void testSubstringAfterFunction() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + "  <string-function-validation>"
                + "    <string1>1999/04/01</string1>"
                + "    <substring-after-function-leaf>test</substring-after-function-leaf>"
                + "  </string-function-validation>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + "  <string-function-validation>"
                + "    <string1>2000/10/05</string1>"
                + "    <substring-after-function-leaf>test</substring-after-function-leaf>"
                + "  </string-function-validation>"
                + "</validation>";
        // should fail
        editConfig(m_server, m_clientInfo, requestXml1, false);

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + "  <string-function-validation>"
                + "    <string1>2000/10/05</string1>"
                + "  </string-function-validation>"
                + "</validation>";
        // should fail
        editConfig(m_server, m_clientInfo, requestXml1, false);
    }

    @Test
    public void testStringLengthFunction() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + "  <string-function-validation>"
                + "    <string1>12345</string1>"
                + "    <string-length-function-leaf>test</string-length-function-leaf>"
                + "  </string-function-validation>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + "  <string-function-validation>"
                + "    <string1>1234</string1>"
                + "    <string-length-function-leaf>test</string-length-function-leaf>"
                + "  </string-function-validation>"
                + "</validation>";
        // should fail
        editConfig(m_server, m_clientInfo, requestXml1, false);

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + "  <string-function-validation>"
                + "    <string1>1234</string1>"
                + "  </string-function-validation>"
                + "</validation>";
        // should fail
        editConfig(m_server, m_clientInfo, requestXml1, false);
    }

    @Test
    public void testNormalizeSpaceFunction() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + "  <string-function-validation>"
                + "    <string1>  ITF  1  </string1>"
                + "    <normalize-space-function-leaf>test</normalize-space-function-leaf>"
                + "  </string-function-validation>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + "  <string-function-validation>"
                + "    <string1>  ITF  12  </string1>"
                + "    <normalize-space-function-leaf>test</normalize-space-function-leaf>"
                + "  </string-function-validation>"
                + "</validation>";
        // should fail
        editConfig(m_server, m_clientInfo, requestXml1, false);

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + "  <string-function-validation>"
                + "    <string1>  ITF  12  </string1>"
                + "  </string-function-validation>"
                + "</validation>";
        // should fail
        editConfig(m_server, m_clientInfo, requestXml1, false);
    }

    @Test
    public void testTranslateFunction() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + "  <string-function-validation>"
                + "    <string1>-bar-</string1>"
                + "    <translate-function-leaf>test</translate-function-leaf>"
                + "  </string-function-validation>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + "  <string-function-validation>"
                + "    <string1>dsl</string1>"
                + "    <translate-function-leaf>test</translate-function-leaf>"
                + "  </string-function-validation>"
                + "</validation>";
        // should fail
        editConfig(m_server, m_clientInfo, requestXml1, false);

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + "  <string-function-validation>"
                + "    <string1>dsl</string1>"
                + "  </string-function-validation>"
                + "</validation>";
        // should fail
        editConfig(m_server, m_clientInfo, requestXml1, false);
    }

    @Test
    public void testBooleanFunction() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + "  <boolean-function-validation>"
                + "    <boolean-function-leaf>test</boolean-function-leaf>"
                + "  </boolean-function-validation>"
                + "</validation>";
        // should fail
        editConfig(m_server, m_clientInfo, requestXml1, false);

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + "  <boolean-function-validation>"
                + "    <string1>nonempty</string1>"
                + "    <boolean-function-leaf>test</boolean-function-leaf>"
                + "  </boolean-function-validation>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + "  <boolean-function-validation>"
                + "    <string1>nonempty</string1>"
                + "  </boolean-function-validation>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);
    }

    @Test
    public void testNotFunction() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + "  <boolean-function-validation>"
                + "    <not-function-leaf>test</not-function-leaf>"
                + "  </boolean-function-validation>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + "  <boolean-function-validation>"
                + "    <string1>nonempty</string1>"
                + "    <not-function-leaf>test</not-function-leaf>"
                + "  </boolean-function-validation>"
                + "</validation>";
        // should fail
        editConfig(m_server, m_clientInfo, requestXml1, false);

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + "  <boolean-function-validation>"
                + "    <string1>nonempty</string1>"
                + "  </boolean-function-validation>"
                + "</validation>";
        // should fail
        editConfig(m_server, m_clientInfo, requestXml1, false);
    }

    // functions with zero arguments not working correctly yet in AV (tracked with FNMS-20733)
//    @Test
//    public void testTrueFunction() throws Exception {
//        getModelNode();
//        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
//                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
//                + "  <boolean-function-validation>"
//                + "    <true-function-leaf>test</true-function-leaf>"
//                + "  </boolean-function-validation>"
//                + "</validation>";
//        editConfig(m_server, m_clientInfo, requestXml1, true);    	
//    }

    // functions with zero arguments not working correctly yet in AV (tracked with FNMS-20733)
//    @Test
//    public void testFalseFunction() throws Exception {
//        getModelNode();
//        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
//                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
//                + "  <boolean-function-validation>"
//                + "    <false-function-leaf>test</false-function-leaf>"
//                + "  </boolean-function-validation>"
//                + "</validation>";
//        editConfig(m_server, m_clientInfo, requestXml1, true);    	
//    }

    @Test
    public void testNumberFunction() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + "  <number-function-validation>"
                + "    <string1>42</string1>"
                + "    <number-function-leaf>test</number-function-leaf>"
                + "  </number-function-validation>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + "  <number-function-validation>"
                + "    <string1>41</string1>"
                + "    <number-function-leaf>test</number-function-leaf>"
                + "  </number-function-validation>"
                + "</validation>";
        // should fail
        editConfig(m_server, m_clientInfo, requestXml1, false);

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + "  <number-function-validation>"
                + "    <string1>41</string1>"
                + "  </number-function-validation>"
                + "</validation>";
        // should fail
        editConfig(m_server, m_clientInfo, requestXml1, false);
    }

    @Test
    public void testFloorFunction() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + "  <number-function-validation>"
                + "    <number1>42.42</number1>"
                + "    <floor-function-leaf>test</floor-function-leaf>"
                + "  </number-function-validation>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + "  <number-function-validation>"
                + "    <number1>41.42</number1>"
                + "    <floor-function-leaf>test</floor-function-leaf>"
                + "  </number-function-validation>"
                + "</validation>";
        // should fail
        editConfig(m_server, m_clientInfo, requestXml1, false);

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + "  <number-function-validation>"
                + "    <number1>41.42</number1>"
                + "  </number-function-validation>"
                + "</validation>";
        // should fail
        editConfig(m_server, m_clientInfo, requestXml1, false);
    }

    @Test
    public void testCeilingFunction() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + "  <number-function-validation>"
                + "    <number1>41.42</number1>"
                + "    <ceiling-function-leaf>test</ceiling-function-leaf>"
                + "  </number-function-validation>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + "  <number-function-validation>"
                + "    <number1>42.42</number1>"
                + "    <ceiling-function-leaf>test</ceiling-function-leaf>"
                + "  </number-function-validation>"
                + "</validation>";
        // should fail
        editConfig(m_server, m_clientInfo, requestXml1, false);

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + "  <number-function-validation>"
                + "    <number1>42.42</number1>"
                + "  </number-function-validation>"
                + "</validation>";
        // should fail
        editConfig(m_server, m_clientInfo, requestXml1, false);
    }

    @Test
    public void testRoundFunction() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + "  <number-function-validation>"
                + "    <number1>42.42</number1>"
                + "    <round-function-leaf>test</round-function-leaf>"
                + "  </number-function-validation>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + "  <number-function-validation>"
                + "    <number1>41.72</number1>"
                + "    <round-function-leaf>test</round-function-leaf>"
                + "  </number-function-validation>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + "  <number-function-validation>"
                + "    <number1>41.42</number1>"
                + "    <round-function-leaf>test</round-function-leaf>"
                + "  </number-function-validation>"
                + "</validation>";
        // should fail
        editConfig(m_server, m_clientInfo, requestXml1, false);

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + "  <number-function-validation>"
                + "    <number1>41.42</number1>"
                + "  </number-function-validation>"
                + "</validation>";
        // should fail
        editConfig(m_server, m_clientInfo, requestXml1, false);
    }
}


