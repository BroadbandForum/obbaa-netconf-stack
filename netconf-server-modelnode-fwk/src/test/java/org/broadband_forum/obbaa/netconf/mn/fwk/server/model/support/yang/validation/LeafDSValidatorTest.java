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

import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.assertXMLStringEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.api.messages.AbstractNetconfRequest;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigDefaultOperations;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorSeverity;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorType;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.ReferringNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.ReferringNodes;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.AddDefaultDataInterceptor;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

@RunWith(RequestScopeJunitRunner.class)
public class LeafDSValidatorTest extends AbstractDataStoreValidatorTest {
    
	@Test
	public void testNotMatchedLeafRefValue() throws ModelNodeInitException {
		testFail("/datastorevalidatortest/leafrefvalidation/defaultxml/invalid-leaf-ref.xml", NetconfRpcErrorTag.DATA_MISSING,
				NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error, "instance-required",
				"Dependency violated, 'ABC' must exist", "/validation:validation/validation:leaf-ref/validation:album[validation:name='Album1']/validation:song[validation:name='Circus']/validation:artist-name");
	}
	
	@Test
	public void testMatchedLeafRefValue() throws ModelNodeInitException {
		testPass("/datastorevalidatortest/leafrefvalidation/defaultxml/valid-leaf-ref.xml");
	}
	
	@Test
	public void testNotMatchedLeafRefValueWithCurrent() throws ModelNodeInitException {
		testFail("/datastorevalidatortest/leafrefvalidation/defaultxml/invalid-reference-leaf-ref.xml", NetconfRpcErrorTag.DATA_MISSING,
				NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error, "instance-required",
                "Dependency violated, 'Invalid_SONG' must exist", "/validation:validation/validation:leaf-ref/validation:music/validation:favourite-song");
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
    public void testInternalRequestContainerWithMust() throws Exception {
        getModelNode();
        initialiseInterceptor();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">" +
                "</validation-yang11>                                                 ";
        editConfig(m_server, m_clientInfo, requestXml1, true);

        String response = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <validation11:validation-yang11 xmlns:validation11=\"urn:org:bbf2:pma:validation-yang11\">"
                + "   <validation11:container-with-must>"
                + "     <validation11:remove-mac-addresses-from-port-down>false</validation11:remove-mac-addresses-from-port-down>"
                + "   </validation11:container-with-must>"
                + "  </validation11:validation-yang11>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + " </data>"
                + " </rpc-reply>";
        verifyGet(response);
    }
	
	@Test
	public void testAugmentedWhen() throws Exception{
	     
        String requestXml1 = "/datastorevalidatortest/leafrefvalidation/defaultxml/AugmentedWhen.xml";
        getModelNode();
        EditConfigRequest request1 = createRequest(requestXml1);
        request1.setMessageId("1");
        NetConfResponse response1 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request1, response1);        
        assertTrue(response1.isOk());
        
        String response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <validation11:validation-yang11 xmlns:validation11=\"urn:org:bbf2:pma:validation-yang11\">"
                + "   <validation11:identity-leaf>validation11:identity3</validation11:identity-leaf>"
                + "   <validation11:leaf-ref-yang11>"
                + "    <validation11:default-leaf>0</validation11:default-leaf>"                
                + "   </validation11:leaf-ref-yang11>"
                + "   <validation11:container-with-must>"
                + "     <validation11:remove-mac-addresses-from-port-down>false</validation11:remove-mac-addresses-from-port-down>"
                + "   </validation11:container-with-must>"
                + "  </validation11:validation-yang11>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + " </data>"
                + " </rpc-reply>"
                ;
        verifyGet(response);
    
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
				"<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">" +
				"  <leaf-ref-yang11>" +
				"   <default-leaf>2</default-leaf>" +
				"  </leaf-ref-yang11>" +
				"</validation-yang11>                                                 " ;
		editConfig(m_server, m_clientInfo, requestXml1, true);

		response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <validation11:validation-yang11 xmlns:validation11=\"urn:org:bbf2:pma:validation-yang11\">"
						+ "   <validation11:identity-leaf>validation11:identity3</validation11:identity-leaf>"
						+ "   <validation11:leaf-ref-yang11>"
						+ "    <validation11:default-leaf>2</validation11:default-leaf>"  
						+ "   </validation11:leaf-ref-yang11>"
						+ "   <validation11:container-with-must>"
						+ "     <validation11:remove-mac-addresses-from-port-down>false</validation11:remove-mac-addresses-from-port-down>"
						+ "   </validation11:container-with-must>"
						+ "  </validation11:validation-yang11>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response);
		
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
				"<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">" +
				"  <identity-leaf>identity7</identity-leaf>" +
				"</validation-yang11>                                                 " ;
		editConfig(m_server, m_clientInfo, requestXml1, true);

		response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <validation11:validation-yang11 xmlns:validation11=\"urn:org:bbf2:pma:validation-yang11\">"
						+ "   <validation11:identity-leaf>validation11:identity7</validation11:identity-leaf>"
						+ "   <validation11:container-with-must>"
						+ "     <validation11:remove-mac-addresses-from-port-down>false</validation11:remove-mac-addresses-from-port-down>"
						+ "   </validation11:container-with-must>"
						+ "  </validation11:validation-yang11>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response);
		
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
				"<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">" +
				"  <identity-leaf>identity7</identity-leaf>" +
				"  <leaf-ref-yang11>" +
				"   <default-leaf>2</default-leaf>" +
				"  </leaf-ref-yang11>" +
				"</validation-yang11>                                                 " ;
		NetConfResponse ncresponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(1,ncresponse.getErrors().size());
		assertEquals("/validation11:validation-yang11/validation11:leaf-ref-yang11/validation11:default-leaf", ncresponse.getErrors().get(0).getErrorPath());
		assertEquals("Violate when constraints: derived-from-or-self(../validation11:identity-leaf, 'validation11:identity3')", ncresponse.getErrors().get(0).getErrorMessage()); 
    }
	
	@Test
	public void testUsesWhenInsideAugmentWithoutWhen() throws Exception{
	    getModelNode();
	    initialiseInterceptor();

	    String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
	            "<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">" +
	            "  <test-leaf>FNMS</test-leaf>" +
	            "</validation-yang11>                                                 " ;
	    editConfig(m_server, m_clientInfo, requestXml1, true);

	    String response = 
	            "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
	                    + " <data>"
	                    + "  <validation11:validation-yang11 xmlns:validation11=\"urn:org:bbf2:pma:validation-yang11\">"
	                    + "   <validation11:container-with-must>"
	                    + "     <validation11:remove-mac-addresses-from-port-down>false</validation11:remove-mac-addresses-from-port-down>"
	                    + "   </validation11:container-with-must>"
	                    + "  <validation11:leaf-ref-yang11>"
	                    + "   <validation11:when-uses-inside-augment-container>"
	                    + "    <validation11:when-uses-inside-augment-leaf2>defaultLeaf2</validation11:when-uses-inside-augment-leaf2>"
	                    + "   </validation11:when-uses-inside-augment-container>"
	                    + "   <validation11:when-uses-inside-augment-leaf1>defaultLeaf1</validation11:when-uses-inside-augment-leaf1>"
	                    + "  </validation11:leaf-ref-yang11>"
	                    + "  <validation11:test-leaf>FNMS</validation11:test-leaf>"
	                    + "  </validation11:validation-yang11>"
	                    + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
	                    + " </data>"
	                    + " </rpc-reply>"
	                    ;
	    verifyGet(response);

	    requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
	            "<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">" +
	            "  <test-leaf>FNMS1</test-leaf>" +
	            "</validation-yang11>                                                 " ;
	    editConfig(m_server, m_clientInfo, requestXml1, true);

	    response = 
	            "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
	                    + " <data>"
	                    + "  <validation11:validation-yang11 xmlns:validation11=\"urn:org:bbf2:pma:validation-yang11\">"
                        + "   <validation11:container-with-must>"
                        + "     <validation11:remove-mac-addresses-from-port-down>false</validation11:remove-mac-addresses-from-port-down>"
                        + "   </validation11:container-with-must>"
	                    + "  <validation11:test-leaf>FNMS1</validation11:test-leaf>"
	                    + "   <validation11:leaf-ref-yang11/>"
	                    + "  </validation11:validation-yang11>"
	                    + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
	                    + " </data>"
	                    + " </rpc-reply>"
	                    ;
	    verifyGet(response);
	}
	   
	@Test
	public void testAugmentedWhenExplicitlyWithContainerAndList() throws Exception{
		getModelNode();
        initialiseInterceptor();

		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
				"<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">" +
				"  <identity-leaf>identity3</identity-leaf>" +
				"  <leaf-ref-yang11>" +
				"   <default-leaf>2</default-leaf>" +
				"   <insideAugmentedWhen>" +
				"    <insideAugmentedLeaf>check</insideAugmentedLeaf>" +
				"    <insideAugmentedList>" +
				"     <name>name1</name>" +
				"     <whenLeaf>dummy</whenLeaf>" +
				"    </insideAugmentedList>" +
				"   </insideAugmentedWhen>" +
				"  </leaf-ref-yang11>" +
				"</validation-yang11>                                                 " ;
		editConfig(m_server, m_clientInfo, requestXml1, true);

		String response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <validation11:validation-yang11 xmlns:validation11=\"urn:org:bbf2:pma:validation-yang11\">"
						+ "   <validation11:identity-leaf>validation11:identity3</validation11:identity-leaf>"
						+ "   <validation11:leaf-ref-yang11>"
						+ "    <validation11:default-leaf>2</validation11:default-leaf>"   
						+ "    <validation11:insideAugmentedWhen>" 
						+ "     <validation11:insideAugmentedLeaf>check</validation11:insideAugmentedLeaf>" 
						+ "     <validation11:insideAugmentedList>" 
						+ "      <validation11:name>name1</validation11:name>" 
						+ "      <validation11:type>type1</validation11:type>" 
						+ "      <validation11:whenLeaf>dummy</validation11:whenLeaf>"
						+ "     </validation11:insideAugmentedList>"
						+ "    </validation11:insideAugmentedWhen>" 
						+ "   </validation11:leaf-ref-yang11>"
						+ "   <validation11:container-with-must>"
						+ "     <validation11:remove-mac-addresses-from-port-down>false</validation11:remove-mac-addresses-from-port-down>"
						+ "   </validation11:container-with-must>"
						+ "  </validation11:validation-yang11>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
				"<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">" +
				"  <leaf-ref-yang11>" +
				"   <insideAugmentedWhen>" +
				"    <insideAugmentedLeaf>check1</insideAugmentedLeaf>" +
				"   </insideAugmentedWhen>" +
				"  </leaf-ref-yang11>" +
				"</validation-yang11>                                                 " ;
		editConfig(m_server, m_clientInfo, requestXml1, true);

		response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <validation11:validation-yang11 xmlns:validation11=\"urn:org:bbf2:pma:validation-yang11\">"
						+ "   <validation11:container-with-must>"
						+ "     <validation11:remove-mac-addresses-from-port-down>false</validation11:remove-mac-addresses-from-port-down>"
						+ "   </validation11:container-with-must>"
						+ "   <validation11:identity-leaf>validation11:identity3</validation11:identity-leaf>"
						+ "   <validation11:leaf-ref-yang11>"
						+ "    <validation11:default-leaf>2</validation11:default-leaf>"   
						+ "    <validation11:insideAugmentedWhen>" 
						+ "     <validation11:insideAugmentedLeaf>check1</validation11:insideAugmentedLeaf>"  
						+ "     <validation11:insideAugmentedList>" 
						+ "      <validation11:name>name1</validation11:name>" 
						+ "      <validation11:type>type1</validation11:type>" 
						+ "     </validation11:insideAugmentedList>"
						+ "    </validation11:insideAugmentedWhen>" 
						+ "   </validation11:leaf-ref-yang11>"
						+ "  </validation11:validation-yang11>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response);
		
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
				"<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">" +
				"  <identity-leaf>identity7</identity-leaf>" +
				"</validation-yang11>                                                 " ;
		editConfig(m_server, m_clientInfo, requestXml1, true);

		response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <validation11:validation-yang11 xmlns:validation11=\"urn:org:bbf2:pma:validation-yang11\">"
						+ "   <validation11:identity-leaf>validation11:identity7</validation11:identity-leaf>"
						+ "   <validation11:leaf-ref-yang11>"
						+ "   </validation11:leaf-ref-yang11>"
						+ "   <validation11:container-with-must>"
						+ "     <validation11:remove-mac-addresses-from-port-down>false</validation11:remove-mac-addresses-from-port-down>"
						+ "   </validation11:container-with-must>"
						+ "  </validation11:validation-yang11>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response);
		
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
				"<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">" +
				"  <identity-leaf>identity7</identity-leaf>" +
				"  <leaf-ref-yang11>" +
				"   <default-leaf>2</default-leaf>" +
				"   <insideAugmentedWhen>" +
				"    <insideAugmentedLeaf>check</insideAugmentedLeaf>" +
				"   </insideAugmentedWhen>" +
				"  </leaf-ref-yang11>" +
				"</validation-yang11>                                                 " ;
		NetConfResponse ncresponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(1,ncresponse.getErrors().size());
		assertEquals("/validation11:validation-yang11/validation11:leaf-ref-yang11/validation11:default-leaf", ncresponse.getErrors().get(0).getErrorPath());
		assertEquals("Violate when constraints: derived-from-or-self(../validation11:identity-leaf, 'validation11:identity3')", ncresponse.getErrors().get(0).getErrorMessage()); 
	}
	
	@Test
	public void testUsesInsideWhenAugmentation() throws Exception{
		getModelNode();
        initialiseInterceptor();

		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
				"<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">" +
				"  <identity-leaf>identity8</identity-leaf>" +
				"</validation-yang11>                                                 " ;
		editConfig(m_server, m_clientInfo, requestXml1, true);

		String response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <validation11:validation-yang11 xmlns:validation11=\"urn:org:bbf2:pma:validation-yang11\">"
						+ "   <validation11:identity-leaf>validation11:identity8</validation11:identity-leaf>"
						+ "   <validation11:leaf-ref-yang11>"
						+ "    <validation11:insideWhenAugmentedUsesDefaultLeaf>0</validation11:insideWhenAugmentedUsesDefaultLeaf>"   
						+ "    <validation11:insideWhenAugmentedUses/>" 
						+ "   </validation11:leaf-ref-yang11>"
						+ "   <validation11:container-with-must>"
						+ "     <validation11:remove-mac-addresses-from-port-down>false</validation11:remove-mac-addresses-from-port-down>"
						+ "   </validation11:container-with-must>"
						+ "  </validation11:validation-yang11>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response);
		
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
				"<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">" +
				"  <leaf-ref-yang11>" +
				"   <insideWhenAugmentedUses>" +
				"    <insideWhenAugmentedUsesLeaf>dummy</insideWhenAugmentedUsesLeaf>" +
				"   </insideWhenAugmentedUses>" +
				"  </leaf-ref-yang11>" +
				"</validation-yang11>                                                 " ;
		editConfig(m_server, m_clientInfo, requestXml1, true);

		response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <validation11:validation-yang11 xmlns:validation11=\"urn:org:bbf2:pma:validation-yang11\">"
						+ "   <validation11:container-with-must>"
						+ "     <validation11:remove-mac-addresses-from-port-down>false</validation11:remove-mac-addresses-from-port-down>"
						+ "   </validation11:container-with-must>"
						+ "   <validation11:identity-leaf>validation11:identity8</validation11:identity-leaf>"
						+ "   <validation11:leaf-ref-yang11>"
						+ "    <validation11:insideWhenAugmentedUsesDefaultLeaf>0</validation11:insideWhenAugmentedUsesDefaultLeaf>"   
						+ "    <validation11:insideWhenAugmentedUses>" 
						+ "     <validation11:insideWhenAugmentedUsesLeaf>dummy</validation11:insideWhenAugmentedUsesLeaf>"   
						+ "    </validation11:insideWhenAugmentedUses>" 
						+ "   </validation11:leaf-ref-yang11>"
						+ "  </validation11:validation-yang11>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response);
		
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
				"<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">" +
				"  <identity-leaf>identity7</identity-leaf>" +
				"</validation-yang11>                                                 " ;
		editConfig(m_server, m_clientInfo, requestXml1, true);

		response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <validation11:validation-yang11 xmlns:validation11=\"urn:org:bbf2:pma:validation-yang11\">"
						+ "   <validation11:identity-leaf>validation11:identity7</validation11:identity-leaf>"
						+ "   <validation11:leaf-ref-yang11/>" 
						+ "   <validation11:container-with-must>"
						+ "     <validation11:remove-mac-addresses-from-port-down>false</validation11:remove-mac-addresses-from-port-down>"
						+ "   </validation11:container-with-must>"
						+ "  </validation11:validation-yang11>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response);
	}
	
	@Test
	public void testUsesWhenExplicitly() throws Exception{
		getModelNode();

		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
				"<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">" +
				"  <test-leaf>test-leaf</test-leaf>" +
				"  <grouping-inside-container>" +
				"   <grouping-leaf1>grouping</grouping-leaf1>" +
		        "  </grouping-inside-container>" +
				"</validation-yang11>                                                 " ;
		NetConfResponse ncresponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(1,ncresponse.getErrors().size());
		assertEquals("/validation11:validation-yang11/validation11:grouping-inside-container/validation11:grouping-leaf1", ncresponse.getErrors().get(0).getErrorPath());
		assertEquals("Violate when constraints: ../test-leaf = 'test'", ncresponse.getErrors().get(0).getErrorMessage());
		
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
				"<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">" +
				"  <test-leaf>test</test-leaf>" +
				"  <grouping-inside-container>" +
				"   <grouping-leaf1>grouping</grouping-leaf1>" +
		        "  </grouping-inside-container>" +
				"</validation-yang11>                                                 " ;
		editConfig(m_server, m_clientInfo, requestXml1, true);

		String response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <validation11:validation-yang11 xmlns:validation11=\"urn:org:bbf2:pma:validation-yang11\">"
						+ "   <validation11:container-with-must>"
						+ "     <validation11:remove-mac-addresses-from-port-down>false</validation11:remove-mac-addresses-from-port-down>"
						+ "   </validation11:container-with-must>"
						+ "   <validation11:test-leaf>test</validation11:test-leaf>"
						+ "   <validation11:grouping-inside-container>" 
						+ "    <validation11:grouping-leaf1>grouping</validation11:grouping-leaf1>" 
				        + "   </validation11:grouping-inside-container>" 
						+ "  </validation11:validation-yang11>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response);
		
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
				"<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">" +
				"  <test-leaf>test</test-leaf>" +
				"  <grouping-inside-container>" +
		        "   <insideGroupingList>" +
				"     <name>name1</name>" +
				"     <whenLeaf>dummy</whenLeaf>" +
		        "   </insideGroupingList>" +
		        "  </grouping-inside-container>" +
				"</validation-yang11>                                                 " ;
		editConfig(m_server, m_clientInfo, requestXml1, true);

		response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <validation11:validation-yang11 xmlns:validation11=\"urn:org:bbf2:pma:validation-yang11\">"
						+ "   <validation11:container-with-must>"
						+ "     <validation11:remove-mac-addresses-from-port-down>false</validation11:remove-mac-addresses-from-port-down>"
						+ "   </validation11:container-with-must>"
						+ "   <validation11:test-leaf>test</validation11:test-leaf>"
						+ "   <validation11:grouping-inside-container>" 
						+ "    <validation11:grouping-leaf1>grouping</validation11:grouping-leaf1>" 
				        + "    <validation11:insideGroupingList>" 
						+ "     <validation11:name>name1</validation11:name>" 
						+ "     <validation11:type>type1</validation11:type>" 
						+ "     <validation11:whenLeaf>dummy</validation11:whenLeaf>" 
				        + "    </validation11:insideGroupingList>" 
				        + "   </validation11:grouping-inside-container>" 
						+ "  </validation11:validation-yang11>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response);
		
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
				"<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">" +
				"  <test-leaf>test-leaf</test-leaf>" +
				"</validation-yang11>                                                 " ;
		editConfig(m_server, m_clientInfo, requestXml1, true);

		response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <validation11:validation-yang11 xmlns:validation11=\"urn:org:bbf2:pma:validation-yang11\">"
						+ "   <validation11:test-leaf>test-leaf</validation11:test-leaf>"
						+ "   <validation11:grouping-inside-container/>"
						+ "   <validation11:container-with-must>"
						+ "     <validation11:remove-mac-addresses-from-port-down>false</validation11:remove-mac-addresses-from-port-down>"
						+ "   </validation11:container-with-must>"
						+ "  </validation11:validation-yang11>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response);
	}
	
    @Test
    public void testDecimal64DefaultTypeAllCases() throws Exception {
        String requestXml1 = "/datastorevalidatortest/leafrefvalidation/defaultxml/valid-decimal64-default.xml";
        getModelNode();
        EditConfigRequest request1 = createRequest(requestXml1);
        request1.setMessageId("1");
        NetConfResponse response1 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request1, response1);        
        assertTrue(response1.isOk());
        String response = 
                "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <data>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "  <validation:decimal64-type-validation7 xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                + "    <validation:type-validation> "
                + "      <validation:id>10</validation:id>"
                + "      <validation:decimal64-fraction1-with-default-value-type>1.6</validation:decimal64-fraction1-with-default-value-type>"
                + "      <validation:decimal64-fraction1-with-default-value-type2>1.8</validation:decimal64-fraction1-with-default-value-type2>"
                + "      <validation:decimal64-fraction1-with-default-value-type3>2.0</validation:decimal64-fraction1-with-default-value-type3>"
                + "    </validation:type-validation>"
                + "  </validation:decimal64-type-validation7>"
                + " </data>"
                + " </rpc-reply>"
                ;
        verifyGet(response); 
    }
    
	@Test
    public void testChildImpactNodeValidationForList() throws Exception {
        String requestXml1 = "/datastorevalidatortest/leafrefvalidation/defaultxml/ChildImpactValidation.xml";
        getModelNode();
        EditConfigRequest request1 = createRequest(requestXml1);
        request1.setMessageId("1");
        NetConfResponse response1 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request1, response1);
        assertTrue(response1.isOk());
    }
	
	@Test
	public void testChildImpactValidationForContainers() throws Exception{
	    String requestXml1 = "/datastorevalidatortest/leafrefvalidation/defaultxml/ChildImpactValidation3.xml";
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
                "<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <validation>abc</validation>" +
                "  <notLeaf>10</notLeaf>" +
                "</validation>                                                 " ;
        editConfig(m_server, m_clientInfo, requestXml1, true);

        String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <validation>hello</validation>" +
                "</validation>                                                 " ;
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml2, false);
        assertEquals(1,response.getErrors().size());
        assertEquals("/validation:validation/validation:notLeaf", response.getErrors().get(0).getErrorPath());
        assertEquals("Violate must constraints: not(../validation = 'hello')", response.getErrors().get(0).getErrorMessage());

	}
	
	@Test
	public void testDeleteofContainer() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
        		" <when-validation>" +
                "   <result-container>12</result-container>" +
        		"   <result-list>10</result-list>" +
        		" </when-validation>" +
                "</validation>                                                 " ;
        editConfig(m_server, m_clientInfo, requestXml1, true);
        
        String response = 
        		 "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
        		 + " <data>"
        		 + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
        		 + "   <validation:when-validation>"
        		 + "    <validation:result-container>12</validation:result-container>"
        		 + "    <validation:result-list>10</validation:result-list>"
        		 //Below empty container needs to be removed via FNMS-24459
        		 + "    <validation:container-type/>"
        		 + "   </validation:when-validation>"
        		 + "  </validation:validation>"
        		 + " </data>"
        		 + " </rpc-reply>"
        		 ;
        verifyGet(response);

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
        		" <when-validation>" +
                "   <container-type/>" +
        		"   <list-type>" +
                "     <list-id>a</list-id>" +
        		"   </list-type>" +
        		" </when-validation>" +
                "</validation>                                                 " ;
        editConfig(m_server, m_clientInfo, requestXml1, true);
        
        response = 
       		 "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
       		 + " <data>"
       		 + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
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
               "<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
       		   " <when-validation>" +
               "   <result-container>9</result-container>" +
               "   <result-list>9</result-list>" +
       		   " </when-validation>" +
               "</validation>                                                 " ;
       editConfig(m_server, m_clientInfo, requestXml1, true);

       response = 
         		 "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
         		 + " <data>"
         		 + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
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
    public void testBooleanFunctionOnLeafsWithEmptyValues() throws ModelNodeInitException, SAXException, IOException{
        getModelNode();
        
        // leaf of string type with empty value
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
                "   <validation:mustOnEmptyTypeLeaf>" +
                "       <validation:name>one</validation:name>" +
                "       <test:stringLeaf xmlns:test=\"urn:opendaylight:datastore-validator-augment-test\"/>" +
                "       <test:mustOnStringLeaf xmlns:test=\"urn:opendaylight:datastore-validator-augment-test\">two</test:mustOnStringLeaf>" +
                "   </validation:mustOnEmptyTypeLeaf>"+
                "</validation:validation> " ;
        editConfig(m_server, m_clientInfo, requestXml, true);
        
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
                "   <validation:mustOnEmptyTypeLeaf>" +
                "       <validation:name>two</validation:name>" +
                "       <test:mustOnStringLeaf xmlns:test=\"urn:opendaylight:datastore-validator-augment-test\">two</test:mustOnStringLeaf>" +
                "   </validation:mustOnEmptyTypeLeaf>"+
                "</validation:validation> " ;
        editConfig(m_server, m_clientInfo, requestXml, false);
        
        //  EMPTY type leaf.
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
                "   <validation:mustOnEmptyTypeLeaf>" +
                "       <validation:name>three</validation:name>" +
                "       <test:emptyTypeLeaf xmlns:test=\"urn:opendaylight:datastore-validator-augment-test\"/>" +
                "       <test:mustOnEmptyLeaf xmlns:test=\"urn:opendaylight:datastore-validator-augment-test\">two</test:mustOnEmptyLeaf>" +
                "   </validation:mustOnEmptyTypeLeaf>"+
                "</validation:validation> " ;
        editConfig(m_server, m_clientInfo, requestXml, true);
        
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
                "   <validation:mustOnEmptyTypeLeaf>" +
                "       <validation:name>four</validation:name>" +
                "       <test:mustOnEmptyLeaf xmlns:test=\"urn:opendaylight:datastore-validator-augment-test\">two</test:mustOnEmptyLeaf>" +
                "   </validation:mustOnEmptyTypeLeaf>"+
                "</validation:validation> " ;
        editConfig(m_server, m_clientInfo, requestXml, false);
        
        // leaf-list now 
        
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
                "   <validation:mustOnEmptyTypeLeaf>" +
                "       <validation:name>five</validation:name>" +
                "       <test:mustOnLeafList xmlns:test=\"urn:opendaylight:datastore-validator-augment-test\">two</test:mustOnLeafList>" +
                "   </validation:mustOnEmptyTypeLeaf>"+
                "</validation:validation> " ;
        editConfig(m_server, m_clientInfo, requestXml, false);
        
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
                "   <validation:mustOnEmptyTypeLeaf>" +
                "       <validation:name>six</validation:name>" +
                "       <test:leafList xmlns:test=\"urn:opendaylight:datastore-validator-augment-test\">ONE</test:leafList>" +
                "       <test:leafList xmlns:test=\"urn:opendaylight:datastore-validator-augment-test\">TWO</test:leafList>" +
                "       <test:mustOnLeafList xmlns:test=\"urn:opendaylight:datastore-validator-augment-test\">two</test:mustOnLeafList>" +
                "   </validation:mustOnEmptyTypeLeaf>"+
                "</validation:validation> " ;
        editConfig(m_server, m_clientInfo, requestXml, true);
    }

	@Test
    public void testContainsLeaf() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <validation>hello</validation>" +
                "  <containsLeaf>10</containsLeaf>" +
                "</validation>                                                 " ;
        editConfig(m_server, m_clientInfo, requestXml1, true);

        String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <validation>abc</validation>" +
                "</validation>                                                 " ;
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml2, false);
        assertEquals(1,response.getErrors().size());
        assertEquals("/validation:validation/validation:containsLeaf", response.getErrors().get(0).getErrorPath());
        assertEquals("Violate must constraints: contains(../validation,'hello')", response.getErrors().get(0).getErrorMessage());
    }
	
	@Test
    public void testMustEvaluationWithCurrent() throws Exception {
        getModelNode();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "   <testMustCountContainer>" +
                "       <leaf1>1</leaf1>" +
                "       <targetContainer>" +
                "           <leaf2>2</leaf2>" +
                "       </targetContainer>"+
                "       <dei-marking-list>" +
                "           <keyLeaf>1</keyLeaf>" +
                "       </dei-marking-list>"+
                "   </testMustCountContainer>"+
                "</validation> " ;
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
        NetconfRpcError netconfRpcError = response.getErrors().get(0);
        assertEquals("must-violation", netconfRpcError.getErrorAppTag());
        assertEquals("dei-marking-list should not exist", netconfRpcError.getErrorMessage());
        
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "   <testMustCountContainer xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" xc:operation=\"delete\" >" +
                "   </testMustCountContainer>"+
                "</validation> " ;
        editConfig(m_server, m_clientInfo, requestXml, true);
        
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "   <testMustCountContainer>" +
                "       <leaf1>1</leaf1>" +
                "       <targetContainer>" +
                "           <leaf2>2</leaf2>" +
                "       </targetContainer>"+
                "   </testMustCountContainer>"+
                "</validation> " ;
        editConfig(m_server, m_clientInfo, requestXml, true);
        
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "   <testMustCountContainer>" +
                "       <dei-marking-list>" +
                "           <keyLeaf>1</keyLeaf>" +
                "       </dei-marking-list>"+
                "   </testMustCountContainer>"+
                "</validation> " ;
        response = editConfig(m_server, m_clientInfo, requestXml, false);
        netconfRpcError = response.getErrors().get(0);
        assertEquals("must-violation", netconfRpcError.getErrorAppTag());
        assertEquals("dei-marking-list should not exist", netconfRpcError.getErrorMessage());
    }
	
	@Test
    public void testDefaultLeafCreationWithDefaultOperationAsNone() throws Exception {
	    getModelNode();
	    String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "   <impactNodeWithDefault  xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"create\">" +
                "       <keyLeaf>5</keyLeaf>" +
                "   </impactNodeWithDefault>"+
                "</validation> " ;
        editConfig(m_server, m_clientInfo, requestXml, true, EditConfigDefaultOperations.NONE);
        String response = "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\"><data>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                + "<validation:impactNodeWithDefault>"
                + "<validation:keyLeaf>5</validation:keyLeaf>"
                + "<validation:containerWithDefaltLeafWithoutWhen>"
                + "<validation:leafWithDefalt3>defValue3</validation:leafWithDefalt3>"
                + "</validation:containerWithDefaltLeafWithoutWhen>"
                + "</validation:impactNodeWithDefault>"
                + "</validation:validation>"
                + "</data></rpc-reply>";
        
        verifyGet(response);
	}
	
	@Test
	public void testWhenConstraintLeafWithDefault() throws Exception {
		getModelNode();
		String response = "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\"><data>"
				+ "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
				+ "<validation:impactNodeWithDefault>"
				+ "<validation:keyLeaf>11</validation:keyLeaf>"
				+ "<validation:impactLeafWithDefalt1>defValue1</validation:impactLeafWithDefalt1>"
				+ "<validation:impactContainerWithDefaltLeaf>"
				+ "<validation:impactLeafWithDefalt2>defValue2</validation:impactLeafWithDefalt2>"
				+ "</validation:impactContainerWithDefaltLeaf>"
				+ "<validation:containerWithDefaltLeafWithoutWhen>"
                + "<validation:leafWithDefalt3>defValue3</validation:leafWithDefalt3>"
                + "</validation:containerWithDefaltLeafWithoutWhen>"
				+ "</validation:impactNodeWithDefault>"
				+ "</validation:validation>"
				+ "</data></rpc-reply>";
		
		String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				"	<impactNodeWithDefault>" +
				"  		<keyLeaf>11</keyLeaf>" +
				"	</impactNodeWithDefault>"+
				"</validation> " ;
		editConfig(m_server, m_clientInfo, requestXml, true); // Create without defaults -> default values should be innstantiated.
		verifyGet(response);
		
		requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				"	<impactNodeWithDefault>" +
				"  		<keyLeaf>11</keyLeaf>" +
				"  		<impactLeafWithDefalt1>myValue1</impactLeafWithDefalt1>" +
				"		<impactContainerWithDefaltLeaf>" +
				"  			<impactLeafWithDefalt2>myValue2</impactLeafWithDefalt2>" +
				"		</impactContainerWithDefaltLeaf>" +
				"	</impactNodeWithDefault>"+
				"</validation> " ;
		editConfig(m_server, m_clientInfo, requestXml, true); // Change defaults values.
		
		requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				"	<impactNodeWithDefault>" +
				"  		<keyLeaf>11</keyLeaf>" +
				"  		<dummyLeaf>true</dummyLeaf>" +
				"	</impactNodeWithDefault>"+
				"</validation> " ;
		editConfig(m_server, m_clientInfo, requestXml, true); // now change some other node (by having the key node in request, which has impact nodes). Changed values should not be reset to defaults.

		String getResponse = "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\"><data>"
				+ "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
				+ "<validation:impactNodeWithDefault>"
				+ "<validation:keyLeaf>11</validation:keyLeaf>"
				+ "<validation:dummyLeaf>true</validation:dummyLeaf>"
				+ "<validation:impactLeafWithDefalt1>myValue1</validation:impactLeafWithDefalt1>"
				+ "<validation:impactContainerWithDefaltLeaf>"
				+ "<validation:impactLeafWithDefalt2>myValue2</validation:impactLeafWithDefalt2>"
				+ "</validation:impactContainerWithDefaltLeaf>"
				+ "<validation:containerWithDefaltLeafWithoutWhen>"
                + "<validation:leafWithDefalt3>defValue3</validation:leafWithDefalt3>"
                + "</validation:containerWithDefaltLeafWithoutWhen>"
				+ "</validation:impactNodeWithDefault>"
				+ "</validation:validation>"
				+ "</data></rpc-reply>";
        verifyGet(getResponse);
        
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				"	<impactNodeWithDefault>" +
				"  		<keyLeaf>12</keyLeaf>" +
				"	</impactNodeWithDefault>"+
				"</validation> " ;
		editConfig(m_server, m_clientInfo, requestXml, true); // Now create one more node without defaults. we should have old values in old node and default values in new node. 
		
		getResponse = "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\"><data>"
				+ "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
				+ "<validation:impactNodeWithDefault>"
				+ "<validation:keyLeaf>11</validation:keyLeaf>"
				+ "<validation:dummyLeaf>true</validation:dummyLeaf>"
				+ "<validation:impactLeafWithDefalt1>%s</validation:impactLeafWithDefalt1>"
				+ "<validation:impactContainerWithDefaltLeaf>"
				+ "<validation:impactLeafWithDefalt2>%s</validation:impactLeafWithDefalt2>"
				+ "</validation:impactContainerWithDefaltLeaf>"
				+ "<validation:containerWithDefaltLeafWithoutWhen>"
                + "<validation:leafWithDefalt3>defValue3</validation:leafWithDefalt3>"
                + "</validation:containerWithDefaltLeafWithoutWhen>"
				+ "</validation:impactNodeWithDefault>"
				+ "<validation:impactNodeWithDefault>"
				+ "<validation:keyLeaf>12</validation:keyLeaf>"
				+ "<validation:impactLeafWithDefalt1>%s</validation:impactLeafWithDefalt1>"
				+ "<validation:impactContainerWithDefaltLeaf>"
				+ "<validation:impactLeafWithDefalt2>%s</validation:impactLeafWithDefalt2>"
				+ "</validation:impactContainerWithDefaltLeaf>"
				+ "<validation:containerWithDefaltLeafWithoutWhen>"
                + "<validation:leafWithDefalt3>defValue3</validation:leafWithDefalt3>"
                + "</validation:containerWithDefaltLeafWithoutWhen>"
				+ "</validation:impactNodeWithDefault>"
				+ "</validation:validation>"
				+ "</data></rpc-reply>";
        verifyGet(String.format(getResponse, "myValue1", "myValue2", "defValue1", "defValue2"));
        
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				"	<impactNodeWithDefault>" +
				"  		<keyLeaf>12</keyLeaf>" +
				"  		<impactLeafWithDefalt1>myValue11</impactLeafWithDefalt1>" +
				"		<impactContainerWithDefaltLeaf>" +
				"  			<impactLeafWithDefalt2>myValue22</impactLeafWithDefalt2>" +
				"		</impactContainerWithDefaltLeaf>" +
				"	</impactNodeWithDefault>"+
				"</validation> " ;
		editConfig(m_server, m_clientInfo, requestXml, true); // Now update the default values of second node
		verifyGet(String.format(getResponse, "myValue1", "myValue2", "myValue11", "myValue22"));
		
		getResponse = "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\"><data>"
				+ "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
				+ "<validation:impactNodeWithDefault>"
				+ "<validation:keyLeaf>11</validation:keyLeaf>"
				+ "<validation:dummyLeaf>true</validation:dummyLeaf>"
				+ "<validation:impactLeafWithDefalt1>%s</validation:impactLeafWithDefalt1>"
				+ "<validation:impactContainerWithDefaltLeaf>"
				+ "<validation:impactLeafWithDefalt2>%s</validation:impactLeafWithDefalt2>"
				+ "</validation:impactContainerWithDefaltLeaf>"
				+ "<validation:containerWithDefaltLeafWithoutWhen>"
                + "<validation:leafWithDefalt3>defValue3</validation:leafWithDefalt3>"
                + "</validation:containerWithDefaltLeafWithoutWhen>"
				+ "</validation:impactNodeWithDefault>"
				+ "<validation:impactNodeWithDefault>"
				+ "<validation:keyLeaf>12</validation:keyLeaf>"
				+ "<validation:dummyLeaf>true</validation:dummyLeaf>"
				+ "<validation:impactLeafWithDefalt1>%s</validation:impactLeafWithDefalt1>"
				+ "<validation:impactContainerWithDefaltLeaf>"
				+ "<validation:impactLeafWithDefalt2>%s</validation:impactLeafWithDefalt2>"
				+ "</validation:impactContainerWithDefaltLeaf>"
				+ "<validation:containerWithDefaltLeafWithoutWhen>"
                + "<validation:leafWithDefalt3>defValue3</validation:leafWithDefalt3>"
                + "</validation:containerWithDefaltLeafWithoutWhen>"
				+ "</validation:impactNodeWithDefault>"
				+ "</validation:validation>"
				+ "</data></rpc-reply>";
		requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				"	<impactNodeWithDefault>" +
				"  		<keyLeaf>12</keyLeaf>" +
				"  		<dummyLeaf>true</dummyLeaf>" +
				"	</impactNodeWithDefault>"+
				"</validation> " ;
		editConfig(m_server, m_clientInfo, requestXml, true);
		verifyGet(String.format(getResponse, "myValue1", "myValue2", "myValue11", "myValue22"));
	}
	
	
	@Test
	public void testWhenConstraintLeaf() throws Exception {
		getModelNode();
		String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				"	<when-validation1>" +
				"  		<when-leaf>hello</when-leaf>" +
				"  		<enabled>true</enabled>" +
				"	</when-validation1>"+
				"</validation> " ;
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, true);
		assertTrue(response.isOk());
		assertEquals(0,response.getErrors().size());

		String responseXml = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
						+ "   <validation:when-validation1>"
						+ "    <validation:when-leaf>hello</validation:when-leaf>"
						+ "    <validation:enabled>true</validation:enabled>"
						+ "   </validation:when-validation1>"
						+ "  </validation:validation>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(responseXml);

		requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				"	<when-validation1>" +
				"  		<enabled>false</enabled>" +
				"	</when-validation1>"+
				"</validation>  ";
		response = editConfig(m_server, m_clientInfo, requestXml, true);
		assertTrue(response.isOk());
		assertEquals(0,response.getErrors().size());

		responseXml = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
						+ "   <validation:when-validation1>"
						+ "    <validation:enabled>false</validation:enabled>"
						+ "   </validation:when-validation1>"
						+ "  </validation:validation>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(responseXml);
	}

	@Test
	public void testMustConstraintLeaf() throws Exception {
		getModelNode();
		String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				"	<must-validation1>" +
				"  		<must-leaf>hello</must-leaf>" +
				"  		<enabled>true</enabled>" +
				"	</must-validation1>"+
				"</validation> " ;
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, true);
		assertTrue(response.isOk());
		assertEquals(0,response.getErrors().size());

		String responseXml = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
						+ "   <validation:must-validation1>"
						+ "    <validation:must-leaf>hello</validation:must-leaf>"
						+ "    <validation:enabled>true</validation:enabled>"
						+ "   </validation:must-validation1>"
						+ "  </validation:validation>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(responseXml);

		requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				"	<must-validation1>" +
				"  		<enabled>false</enabled>" +
				"	</must-validation1>"+
				"</validation>  ";
		response = editConfig(m_server, m_clientInfo, requestXml, false);
		assertFalse(response.isOk());
		assertEquals(1,response.getErrors().size());
		assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, response.getErrors().get(0).getErrorTag());
		assertEquals("must-violation", response.getErrors().get(0).getErrorAppTag());
		assertEquals("/validation:validation/validation:must-validation1/validation:must-leaf", response.getErrors().get(0).getErrorPath());
		assertEquals("Violate must constraints: ../enabled = 'true'", response.getErrors().get(0).getErrorMessage());
	}

	@Test
	public void testLeafWithMustAndWhenTogether() throws Exception {
		getModelNode();
		String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				"	<must-validation1>" +
				"  		<leafWithMustWhen>hello</leafWithMustWhen>" +
				"  		<enabled>true</enabled>" +
				"	</must-validation1>"+
				"</validation> " ;
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, true);
		assertTrue(response.isOk());
		assertEquals(0,response.getErrors().size());

		String responseXml =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
						+ "   <validation:must-validation1>"
						+ "    <validation:leafWithMustWhen>hello</validation:leafWithMustWhen>"
						+ "    <validation:enabled>true</validation:enabled>"
						+ "   </validation:must-validation1>"
						+ "  </validation:validation>"
						+ " </data>"
						+ " </rpc-reply>"
				;
		verifyGet(responseXml);

		requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				"	<must-validation1>" +
				"  		<leafWithMustWhen>hello2</leafWithMustWhen>" +
				"  		<enabled>false</enabled>" +
				"	</must-validation1>"+
				"</validation>  ";
		response = editConfig(m_server, m_clientInfo, requestXml, false);
		assertFalse(response.isOk());
		assertEquals(1,response.getErrors().size());
		assertEquals("when-violation", response.getErrors().get(0).getErrorAppTag());
		assertEquals("/validation:validation/validation:must-validation1/validation:leafWithMustWhen", response.getErrors().get(0).getErrorPath());
		assertEquals("Violate when constraints: ../enabled = 'true'", response.getErrors().get(0).getErrorMessage());

		//failing must condition
		requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				"	<must-validation1>" +
				"  		<leafWithMustWhen>ab</leafWithMustWhen>" +
				"  		<enabled>true</enabled>" +
				"	</must-validation1>"+
				"</validation>  ";
		response = editConfig(m_server, m_clientInfo, requestXml, false);
		assertFalse(response.isOk());
		assertEquals(1,response.getErrors().size());
		assertEquals("must-violation", response.getErrors().get(0).getErrorAppTag());
		assertEquals("/validation:validation/validation:must-validation1/validation:leafWithMustWhen", response.getErrors().get(0).getErrorPath());
		assertEquals("Violate must constraints: string-length(current()) > 3", response.getErrors().get(0).getErrorMessage());
	}

	@Test
	public void testWhenConstraintLeaf_Fail() throws Exception {
		getModelNode();
		String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				"	<when-validation1>" +
				"  		<when-leaf>hello</when-leaf>" +
				"  		<enabled>false</enabled>" +
				"	</when-validation1>"+
				"</validation> " ;
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
		assertFalse(response.isOk());
		assertEquals(1,response.getErrors().size());
		assertEquals(NetconfRpcErrorTag.UNKNOWN_ELEMENT, response.getErrors().get(0).getErrorTag());
		assertEquals("when-violation", response.getErrors().get(0).getErrorAppTag());
		assertEquals("/validation:validation/validation:when-validation1/validation:when-leaf", response.getErrors().get(0).getErrorPath());
		assertEquals("Violate when constraints: ../enabled = 'true'", response.getErrors().get(0).getErrorMessage());
	}

	@Test
	public void testMustConstraintLeaf_Fail() throws Exception {
		getModelNode();
		String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				"	<must-validation1>" +
				"  		<must-leaf>hello</must-leaf>" +
				"  		<enabled>false</enabled>" +
				"	</must-validation1>"+
				"</validation> " ;
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
		assertFalse(response.isOk());
		assertEquals(1,response.getErrors().size());
		assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, response.getErrors().get(0).getErrorTag());
		assertEquals("must-violation", response.getErrors().get(0).getErrorAppTag());
		assertEquals("/validation:validation/validation:must-validation1/validation:must-leaf", response.getErrors().get(0).getErrorPath());
		assertEquals("Violate must constraints: ../enabled = 'true'", response.getErrors().get(0).getErrorMessage());

	}

	@Test
	public void testWhenMandatoryLeaf_Fail() throws Exception {
		getModelNode();
		String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				"	<when-mandatory-validation>" +
				"  		<when-mandatory-leaf>hello</when-mandatory-leaf>" +
				"  		<enabled>false</enabled>" +
				"	</when-mandatory-validation>"+
				"</validation> " ;
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
		assertFalse(response.isOk());
		assertEquals(1,response.getErrors().size());
		assertEquals(NetconfRpcErrorTag.UNKNOWN_ELEMENT, response.getErrors().get(0).getErrorTag());
		assertEquals("when-violation", response.getErrors().get(0).getErrorAppTag());
		assertEquals("/validation:validation/validation:when-mandatory-validation/validation:when-mandatory-leaf", response.getErrors().get(0).getErrorPath());
		assertEquals("Violate when constraints: ../enabled = 'true'", response.getErrors().get(0).getErrorMessage());
	}
	
	@Test
	public void testWhenMandatoryLeaf() throws Exception {
		getModelNode();
		String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				"	<when-mandatory-validation>" +
				"  		<when-mandatory-leaf>hello</when-mandatory-leaf>" +
				"  		<enabled>true</enabled>" +
				"	</when-mandatory-validation>"+
				"</validation> " ;
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, true);
		assertTrue(response.isOk());
		assertEquals(0,response.getErrors().size());

		String responseXml = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
						+ "   <validation:when-mandatory-validation>"
						+ "    <validation:when-mandatory-leaf>hello</validation:when-mandatory-leaf>"
						+ "    <validation:enabled>true</validation:enabled>"
						+ "   </validation:when-mandatory-validation>"
						+ "  </validation:validation>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(responseXml);

		requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				"	<when-mandatory-validation>" +
				"  		<enabled>false</enabled>" +
				"	</when-mandatory-validation>"+
				"</validation>  ";
		response = editConfig(m_server, m_clientInfo, requestXml, true);
		assertTrue(response.isOk());
		assertEquals(0,response.getErrors().size());

		responseXml = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
						+ "   <validation:when-mandatory-validation>"
						+ "    <validation:enabled>false</validation:enabled>"
						+ "   </validation:when-mandatory-validation>"
						+ "  </validation:validation>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(responseXml);
	}

	@Test
	public void testWhenMandatoryLeafFail() throws Exception {
	    getModelNode();
	    String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
	            "<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
	            "   <when-mandatory-validation>" +
	            "       <enabled>true</enabled>" +
	            "   </when-mandatory-validation>"+
	            "</validation> " ;
	   Element requestElement = TestUtil.transformToElement(requestXml);
	   testFail(requestElement, NetconfRpcErrorTag.DATA_MISSING,
	                NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error, "instance-required",
	                "Missing mandatory node - when-mandatory-leaf", "/validation:validation/validation:when-mandatory-validation/validation:when-mandatory-leaf");
	}

	   @Test
	    public void testValidWhenAndMandatoryLeaf() throws Exception {
	        getModelNode();
	        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
	                "<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
	                "   <when-mandatory-validation>" +
	                "   <when-mandatory-leaf>yes</when-mandatory-leaf>" +
	                "       <enabled>true</enabled>" +
	                "   </when-mandatory-validation>"+
	                "</validation> " ;
	        editConfig(m_server, m_clientInfo, requestXml, true);
	        String expectedOutput = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
	                +"<data>"
	                +"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
	                +"<validation:when-mandatory-validation>"
	                +"<validation:enabled>true</validation:enabled>"
	                +"<validation:when-mandatory-leaf>yes</validation:when-mandatory-leaf>"
	                +"</validation:when-mandatory-validation>"
	                +"</validation:validation>"
	                +"</data>"
	                +"</rpc-reply>"
	                ;

	        verifyGet(expectedOutput);
	}

	@Test
	public void testWhenMandatoryLeafRef() throws Exception {
		getModelNode();
		String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				"	<when-mandatory-leafref-validation>" +
				"  		<when-mandatory-leaf>hello</when-mandatory-leaf>" +
				"  		<enabled>true</enabled>" +
				"		<profiles>"+
				"			<profile>"+
				"				<name>hello</name>"+
				"			</profile>"+
				"		</profiles>"+
				"	</when-mandatory-leafref-validation>"+
				"</validation> " ;
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, true);
		assertTrue(response.isOk());
		assertEquals(0,response.getErrors().size());

		String responseXml = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
						+ "   <validation:when-mandatory-leafref-validation>"
						+ "    <validation:when-mandatory-leaf>hello</validation:when-mandatory-leaf>"
						+ "    <validation:enabled>true</validation:enabled>"
						+ "    <validation:profiles>"
						+ "    	<validation:profile>"
						+ "    		<validation:name>hello</validation:name>"
						+ "    	</validation:profile>"
						+ "    	</validation:profiles>"
						+ "   </validation:when-mandatory-leafref-validation>"
						+ "  </validation:validation>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(responseXml);

		requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				"	<when-mandatory-leafref-validation>" +
				"  		<enabled>false</enabled>" +
				"	</when-mandatory-leafref-validation>"+
				"</validation>  ";
		response = editConfig(m_server, m_clientInfo, requestXml, true);
		assertTrue(response.isOk());
		assertEquals(0,response.getErrors().size());

		responseXml = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
						+ "   <validation:when-mandatory-leafref-validation>"
						+ "    <validation:enabled>false</validation:enabled>"
						+ "    <validation:profiles>"
						+ "    	<validation:profile>"
						+ "    		<validation:name>hello</validation:name>"
						+ "    	</validation:profile>"
						+ "    	</validation:profiles>"
						+ "   </validation:when-mandatory-leafref-validation>"
						+ "  </validation:validation>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(responseXml);
	}

	@Test
	public void testWhenMandatoryWithLeafRef_Fail() throws Exception {
		getModelNode();
		String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				"	<when-mandatory-leafref-validation>" +
				"  		<when-mandatory-leaf>hello1</when-mandatory-leaf>" +
				"  		<enabled>true</enabled>" +
				"		<profiles>"+
				"			<profile>"+
				"				<name>hello</name>"+
				"			</profile>"+
				"		</profiles>"+
				"	</when-mandatory-leafref-validation>"+
				"</validation> " ;
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
		assertFalse(response.isOk());
		assertEquals(1,response.getErrors().size());
		assertEquals(NetconfRpcErrorTag.DATA_MISSING, response.getErrors().get(0).getErrorTag());
		assertEquals("instance-required", response.getErrors().get(0).getErrorAppTag());
		assertEquals("/validation:validation/validation:when-mandatory-leafref-validation/validation:when-mandatory-leaf", response.getErrors().get(0).getErrorPath());
		assertEquals("Dependency violated, 'hello1' must exist", response.getErrors().get(0).getErrorMessage());
	}

	@Test
    public void testDuplicateLeafs() throws Exception {
        getModelNode();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <validation>hello</validation>" +
                "  <containsLeaf>10</containsLeaf>" +
                "  <containsLeaf>15</containsLeaf>" +
                "</validation>" ;
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
        String errorMsg = EditContainmentNode.DUPLICATE_ELEMENTS_FOUND + "(urn:org:bbf2:pma:validation?revision=2015-12-14)containsLeaf";
        NetconfRpcError error = response.getErrors().get(0);
        assertEquals(errorMsg, error.getErrorMessage());
        assertEquals(EditContainmentNode.DATA_NOT_UNIQUE, error.getErrorAppTag());
        
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <validation>hello</validation>" +
                "  <containsLeaf>10</containsLeaf>" +
                "  <inner-validation>"+
                "  <containsLeaf>10</containsLeaf>" +
                "  </inner-validation>"+
                "</validation>" ;
        response = editConfig(m_server, m_clientInfo, requestXml, true);
        assertTrue(response.isOk());
    }
    
	
	@Test
	public void testImpactLeafRefOnDelete() throws ModelNodeInitException {
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>					" +
				 "<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				 "	<validation>abc</validation>" +
				 "  <leafref-validation>abc</leafref-validation>" +
				 "</validation>													" ;
		getModelNode();
        editConfig(m_server, m_clientInfo, requestXml1, true);
		
		String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				 "<validation xmlns=\"urn:org:bbf2:pma:validation\"  xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
				 "	<validation xc:operation=\"delete\">abc</validation>" +
				 "</validation>													" ;

		NetConfResponse response2 = editConfig(m_server, m_clientInfo, requestXml2, false);
		assertFalse(response2.isOk());
		assertEquals("/validation:validation/validation:leafref-validation",response2.getErrors().get(0).getErrorPath());
		
	}
	
	@Test
	public void testChoiceLeaf() throws Exception {
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <choicecase>"+
                "    <list1-type>"+
                "      <list-key>key</list-key>"+
                "      <case-leaf1>10</case-leaf1>"+
                "    </list1-type>"+
                "  </choicecase>" +
                "</validation>                                                 " ;
        
        getModelNode();
        editConfig(m_server, m_clientInfo, requestXml1, true);
	}
	@Test
	public void testImpactListOnChange() throws Exception{
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <when-validation>"+
                "      <result-list>10</result-list>"+
                "      <list-type>" +
                "         <list-id>id</list-id>"+
                "         <list-value>value</list-value>"+
                "      </list-type>"+
                "  </when-validation>" +
                "</validation>                                                 " ;
       getModelNode();
       editConfig(m_server, m_clientInfo, requestXml1, true);
	    
       String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
               "<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
               "  <when-validation>"+
               "      <result-list>9</result-list>"+
               "  </when-validation>" +
               "</validation>                                                 " ;
       editConfig(m_server, m_clientInfo, requestXml2, true);
      String expectedOutput = 
              "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"  
              + "<data>"
              + " <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
              + "  <validation:when-validation>"
              + "   <validation:result-list>9</validation:result-list>"
              + "  </validation:when-validation>"
              + " </validation:validation>"
              + "</data>"
              +"</rpc-reply>"
              ;
      verifyGet(expectedOutput);

	}

	@Test
	public void testBigListWithLeafRef() throws Exception {
		getModelNode();

		SchemaPath listTypeSchemaPath = buildSchemaPath("big-list-validation", "list-type");

		Map<QName, ConfigLeafAttribute> emptyMatchCriteria = new HashMap<>();

		ModelNodeId bigListContainer = new ModelNodeId(Arrays.asList(
				new ModelNodeRdn(ModelNodeRdn.CONTAINER, NAMESPACE,
						"validation"),
				new ModelNodeRdn(ModelNodeRdn.CONTAINER, NAMESPACE,
						"big-list-validation")));

		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				" <big-list-validation>" +
				"   <list-type>" +
				"    <list-id>test</list-id>" +
				"   </list-type>" +
				"   <list-type>" +
				"    <list-id>test1</list-id>" +
				"   </list-type>" +
				"   <list-type>" +
				"    <list-id>test2</list-id>" +
				"    <leafref-leaf>test1</leafref-leaf>" +
				"   </list-type>" +
				" </big-list-validation>" +
				"</validation>" ;
		editConfig(m_server, m_clientInfo, requestXml1, true);

		verify( m_modelNodeDsm, atLeastOnce()).isChildTypeBigList(listTypeSchemaPath, m_schemaRegistry);
		// with big-list the match criteria should not be empty
		verify( m_modelNodeDsm, times(0)).findNodes(listTypeSchemaPath, emptyMatchCriteria, bigListContainer, m_schemaRegistry);

		String expectedOutput =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
				+ "<data>"
				+ "	<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
				+ "		<validation:big-list-validation>"
				+ "			<validation:list-type>"
				+ "				<validation:list-id>test</validation:list-id>"
				+ "			</validation:list-type>"
				+ "			<validation:list-type>"
				+ "				<validation:list-id>test1</validation:list-id>"
				+ "			</validation:list-type>"
				+ "			<validation:list-type>"
				+ "				<validation:leafref-leaf>test1</validation:leafref-leaf>"
				+ "				<validation:list-id>test2</validation:list-id>"
				+ "			</validation:list-type>"
				+ "		</validation:big-list-validation>"
				+ "	</validation:validation>"
				+ "</data>"
				+ "</rpc-reply>";

		verifyGet(expectedOutput);

		assertTrue(m_modelNodeDsm.isChildTypeBigList(listTypeSchemaPath, m_schemaRegistry));
	}

	@Test
	public void testWithoutBigListWithLeafRef() throws Exception {
		getModelNode();

		SchemaPath listTypeSchemaPath = buildSchemaPath("testContainer", "list-type");

		Map<QName, ConfigLeafAttribute> emptyMatchCriteria = new HashMap<>();

		ModelNodeId bigListContainer = new ModelNodeId(Arrays.asList(
				new ModelNodeRdn(ModelNodeRdn.CONTAINER, NAMESPACE,
						"validation"),
				new ModelNodeRdn(ModelNodeRdn.CONTAINER, NAMESPACE,
						"testContainer")));

		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				" <testContainer>" +
				"   <list-type>" +
				"    <list-id>test</list-id>" +
				"   </list-type>" +
				"   <list-type>" +
				"    <list-id>test1</list-id>" +
				"   </list-type>" +
				"   <list-type>" +
				"    <list-id>test2</list-id>" +
				"    <leafref-leaf>test1</leafref-leaf>" +
				"   </list-type>" +
				" </testContainer>" +
				"</validation>" ;
		editConfig(m_server, m_clientInfo, requestXml1, true);

		verify( m_modelNodeDsm, atLeastOnce()).isChildTypeBigList(listTypeSchemaPath, m_schemaRegistry);

		// with big-list the match criteria should be empty.
		//
		// there is scenario where, while fetching the node [in HelperDrivenModelNode::addMergeCommands we get childNode
		// ModelNodeConstraintProcessor.getChildNode(childListHelper, this, childEditNode);] match criteria will be filled eventhough child is not big-list
		// so only for leafref validation, the matchcriteria will be empty. we should check only this case [atLeastOnce() with empty matchcriteria] in UT
		// We cannot check times(0) with filled up matchcriteria AND expect the UT to pass, since in the above scenario matchcriteria will be filled for the above mentioned case
		verify( m_modelNodeDsm, atLeastOnce()).findNodes(listTypeSchemaPath, emptyMatchCriteria, bigListContainer, m_schemaRegistry);

		String expectedOutput =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ "<data>"
						+ "	<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
						+ "		<validation:testContainer>"
						+ "			<validation:list-type>"
						+ "				<validation:list-id>test</validation:list-id>"
						+ "			</validation:list-type>"
						+ "			<validation:list-type>"
						+ "				<validation:list-id>test1</validation:list-id>"
						+ "			</validation:list-type>"
						+ "			<validation:list-type>"
						+ "				<validation:leafref-leaf>test1</validation:leafref-leaf>"
						+ "				<validation:list-id>test2</validation:list-id>"
						+ "			</validation:list-type>"
						+ "		</validation:testContainer>"
						+ "	</validation:validation>"
						+ "</data>"
						+ "</rpc-reply>";

		verifyGet(expectedOutput);

		assertFalse(m_modelNodeDsm.isChildTypeBigList(listTypeSchemaPath, m_schemaRegistry));
	}

	@Test
	public void testImpactLeafOnChange() throws ModelNodeInitException, NetconfMessageBuilderException, SchemaBuildException, SAXException, IOException {
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>					" +
				 "<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				 "	<arithmetic-validation>"+
				 "		<value1>5</value1>"+
				 "		<mod-leaf>10</mod-leaf>"+
				 "	</arithmetic-validation>" +
				 "</validation>													" ;
		getModelNode();
		EditConfigRequest request1 = createRequestFromString(requestXml1);
		request1.setMessageId("1");
		NetConfResponse response1 = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request1, response1);
		assertTrue(response1.isOk());
	
		String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				 "<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
				 "	<arithmetic-validation>"+
				 "		<value1>4</value1>"+
				 "	</arithmetic-validation>" +
				 "</validation>													" ;
		getModelNode();
		EditConfigRequest request2 = createRequestFromString(requestXml2);
		request2.setMessageId("1");
		NetConfResponse response2 = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request2, response2);
		assertTrue(response2.isOk());

	    String expectedOutput = 
	                "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
	                  "<data>" +
	                    "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
	                       "<validation:arithmetic-validation>" +
	                         "<validation:value1>4</validation:value1>" +
	                       "</validation:arithmetic-validation>" +
	                    "</validation:validation>"+
	                  "</data>"+
	                "</rpc-reply>"
	                  ;

		verifyGet(expectedOutput);
		
	}

	@Test
	public void testValidLeafRefWithCurrentAlone() throws ModelNodeInitException {
		testMatchedLeafRefValueWithCurrent();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>					" +
							 "<validation xmlns=\"urn:org:bbf2:pma:validation\"> 		" +
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
							 "</validation>													" ;
        getModelNode();
        EditConfigRequest request1 = createRequestFromString(requestXml1);
        request1.setMessageId("1");
        NetConfResponse response1 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request1, response1);
        assertTrue(response1.isOk());
        
		String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>					" +
							 "<validation xmlns=\"urn:org:bbf2:pma:validation\">			" +
							 "    <leaf-ref>												" +
							 "		<current-alone> 										" +
							 "			<current-parent-leaf>Album1</current-parent-leaf>	" +
							 "			<current-leaf-list>Test1</current-leaf-list>		" +
							 "			<current-leaf-list>Test2</current-leaf-list>		" +
							 "		</current-alone>										" +
							 "	  </leaf-ref> 												" +
							 "</validation>													" ;
        getModelNode();
        EditConfigRequest request2 = createRequestFromString(requestXml2);
        request2.setMessageId("1");
        NetConfResponse response2 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request2, response2);
        assertTrue(response2.isOk());
	}
	
	@Test
	public void testCurrent_LeafWithWhenConstraints() throws Exception {
		
		String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>					" +
				 "<validation xmlns=\"urn:org:bbf2:pma:validation\">			" +
				 "    <leaf-ref>												" +
				 "    	<album> 												" +
				 "    		<name>Test</name> 								" +
				 " 			<song-count>20</song-count>							" +
				 "       </album>												" +
				 "		<current-alone> 										" +
				 "			<current-leaf1>Test</current-leaf1>		" +
				 "			<current-leaf2>Test2</current-leaf2>		" +
				 "		</current-alone>										" +
				 "	  </leaf-ref> 												" +
				 "</validation>													" ;
		getModelNode();
		EditConfigRequest request2 = createRequestFromString(requestXml2);
		request2.setMessageId("1");
		NetConfResponse response2 = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request2, response2);
		assertTrue(response2.isOk());
	}
	
	@Test
	public void testCurrentLeaf_FailWhenConstraints_1() throws Exception {
		
		getModelNode();
		/**
		 * current-leaf1 is not available in request as well as DS, so when constraints failed for current-leaf2
		 */
		String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>		" +
				 "<validation xmlns=\"urn:org:bbf2:pma:validation\">			" +
				 "    <leaf-ref>												" +
				 "		<current-alone> 										" +
				 "			<current-leaf2>Test2</current-leaf2>				" +
				 "		</current-alone>										" +
				 "	  </leaf-ref> 												" +
				 "</validation>													" ;
		
		EditConfigRequest request = createRequestFromString(requestXml);
		request.setMessageId("1");
		NetConfResponse response = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request, response);
		assertFalse(response.isOk());
		assertEquals(NetconfRpcErrorTag.UNKNOWN_ELEMENT, response.getErrors().get(0).getErrorTag());
		assertEquals("when-violation", response.getErrors().get(0).getErrorAppTag());
		assertEquals("Violate when constraints: /validation/leaf-ref/album[name = current()/../current-leaf1]/song-count >= 10", response.getErrors().get(0).getErrorMessage());
		assertEquals("/validation:validation/validation:leaf-ref/validation:current-alone/validation:current-leaf2", response.getErrors().get(0).getErrorPath());

	}

	@Test
	public void testCurrentLeaf_FailWhenConstraints_2() throws Exception {

		getModelNode();
		/**
		 * current-leaf1 value is Test, but album list is empty, so when constraints failed for current-leaf2
		 */
		String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>		" +
				"<validation xmlns=\"urn:org:bbf2:pma:validation\">			" +
				"    <leaf-ref>												" +
				"		<current-alone> 										" +
				"			<current-leaf2>Test2</current-leaf2>				" +
				"			<current-leaf1>Test</current-leaf1>					" +
				"		</current-alone>										" +
				"	  </leaf-ref> 												" +
				"</validation>													";;

		EditConfigRequest request = createRequestFromString(requestXml);
		request.setMessageId("1");
		NetConfResponse response = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request, response);
		assertFalse(response.isOk());
		assertEquals(NetconfRpcErrorTag.UNKNOWN_ELEMENT, response.getErrors().get(0).getErrorTag());
		assertEquals("when-violation", response.getErrors().get(0).getErrorAppTag());
		assertEquals("Violate when constraints: /validation/leaf-ref/album[name = current()/../current-leaf1]/song-count >= 10", response.getErrors().get(0).getErrorMessage());
		assertEquals("/validation:validation/validation:leaf-ref/validation:current-alone/validation:current-leaf2", response.getErrors().get(0).getErrorPath());


	}

	@Test
	public void testCurrentLeaf_FailWhenConstraints_3() throws Exception {

		getModelNode();
		/**
		 * current-leaf1 value is Test, but album list does not have any key with 'Test' so when constraints failed for current-leaf2
		 */

		String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>				" +
				"<validation xmlns=\"urn:org:bbf2:pma:validation\">			" +
				"    <leaf-ref>												" +
				"    	<album> 												" +
				"    		<name>Album1</name> 									" +
				" 			<song-count>20</song-count>							" +
				"       </album>												" +
				"		<current-alone> 										" +
				"			<current-leaf1>Test</current-leaf1>					" +
				"			<current-leaf2>Test2</current-leaf2>				" +
				"		</current-alone>										" +
				"	  </leaf-ref> 												" +
				"</validation>	";

		EditConfigRequest request = createRequestFromString(requestXml);
		request.setMessageId("1");
		NetConfResponse response = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request, response);
		assertFalse(response.isOk());
		assertEquals(NetconfRpcErrorTag.UNKNOWN_ELEMENT, response.getErrors().get(0).getErrorTag());
		assertEquals("when-violation", response.getErrors().get(0).getErrorAppTag());
		assertEquals("Violate when constraints: /validation/leaf-ref/album[name = current()/../current-leaf1]/song-count >= 10", response.getErrors().get(0).getErrorMessage());
		assertEquals("/validation:validation/validation:leaf-ref/validation:current-alone/validation:current-leaf2", response.getErrors().get(0).getErrorPath());
	}
	
	@Test
	public void testCountWithPredicatesNegativeCase1() throws ModelNodeInitException {
		getModelNode();
		
		String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				 "<validation xmlns=\"urn:org:bbf2:pma:validation\">"+
				 "	 <count-validation> "+
				 "		<dummyLeaf>target</dummyLeaf>"+
				 "		<countWithPredicates>IN</countWithPredicates>"+
				 "	</count-validation> "+
				 "</validation>";
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
		NetconfRpcError error = response.getErrors().get(0);
		assertEquals("/validation:validation/validation:count-validation/validation:countWithPredicates", error.getErrorPath());
		assertEquals("Violate when constraints: count(/validation/count-validation/dummyLeaf[. = /validation/listForCount/testLeaf]) > 0", error.getErrorMessage());
		assertEquals("when-violation", error.getErrorAppTag());
	}

	@Test
	public void testCountWithPredicatesNegativeCase2() throws ModelNodeInitException {
		getModelNode();

		String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf2:pma:validation\">"+
				"	<listForCount> "+
				"		<id>8</id>"+
				"		<testLeaf>notTarget</testLeaf>"+
				"	</listForCount> "+
				"	 <count-validation> "+
				"		<dummyLeaf>target</dummyLeaf>"+
				"		<countWithPredicates>IN</countWithPredicates>"+
				"	</count-validation> "+
				"</validation>";
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
		NetconfRpcError error = response.getErrors().get(0);
		assertEquals("/validation:validation/validation:count-validation/validation:countWithPredicates", error.getErrorPath());
		assertEquals("Violate when constraints: count(/validation/count-validation/dummyLeaf[. = /validation/listForCount/testLeaf]) > 0", error.getErrorMessage());
		assertEquals("when-violation", error.getErrorAppTag());
	}

	@Test
	public void testCountWithPredicatesPositiveCase() throws ModelNodeInitException {
		getModelNode();
		
		String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				 "<validation xmlns=\"urn:org:bbf2:pma:validation\">"+
				 "	<listForCount> "+
				 "		<id>8</id>"+
				 "		<testLeaf>notTarget</testLeaf>"+
				 "	</listForCount> "+
				 "	<listForCount> "+
				 "		<id>9</id>"+
				 "		<testLeaf>target</testLeaf>"+
				 "	</listForCount> "+
				 "	 <count-validation> "+
				 "		<dummyLeaf>target</dummyLeaf>"+
				 "		<countWithPredicates>IN</countWithPredicates>"+
				 "	</count-validation> "+
				 "</validation>";
		editConfig(m_server, m_clientInfo, requestXml, true);
	}
	@Test
	public void testCountWithPredicatesPositiveCase1() throws ModelNodeInitException {
		getModelNode();

		String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf2:pma:validation\">"+
				"	<cache> "+
				"		<name>one</name>"+
				"		<testLeaf>notNeeded</testLeaf>"+
				"		<cacheField> "+
				"			<id>1</id>"+
				"			<ieName>constantX</ieName>"+
				"		</cacheField> "+
				"		<cacheField> "+
				"			<id>2</id>"+
				"			<ieName>constant1</ieName>"+
				"		</cacheField> "+
				"	</cache> "+
				"	<cache> "+
				"		<name>two</name>"+
				"		<testLeaf>notNeeded</testLeaf>"+
				"		<cacheField> "+
				"			<id>1</id>"+
				"			<ieName>constantX</ieName>"+
				"		</cacheField> "+
				"		<cacheField> "+
				"			<id>2</id>"+
				"			<ieName>constant2</ieName>"+
				"		</cacheField> "+
				"	</cache> "+
				"</validation>";
		editConfig(m_server, m_clientInfo, requestXml, true);
	}
	
	@Test
	public void testCountWithPredicatesNegitiveCase1() throws ModelNodeInitException {
		getModelNode();

		String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf2:pma:validation\">"+
				"	<cache> "+
				"		<name>one</name>"+
				"		<testLeaf>notNeeded</testLeaf>"+
				"		<cacheField> "+
				"			<id>1</id>"+
				"			<ieName>constantX</ieName>"+
				"		</cacheField> "+
				"		<cacheField> "+
				"			<id>2</id>"+
				"			<ieName>constant1</ieName>"+
				"		</cacheField> "+
				"	</cache> "+
				"	<cache> "+
				"		<name>two</name>"+
				"		<testLeaf>notNeeded</testLeaf>"+
				"		<cacheField> "+
				"			<id>1</id>"+
				"			<ieName>constantX</ieName>"+
				"		</cacheField> "+
				"		<cacheField> "+
				"			<id>2</id>"+
				"			<ieName>constant1</ieName>"+
				"		</cacheField> "+
				"	</cache> "+
				"</validation>";
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
		NetconfRpcError error = response.getErrors().get(0);
		assertEquals("An ieName must be unique for constant 1&2", error.getErrorMessage());
		assertEquals("must-violation", error.getErrorAppTag());
	}

	@Test
	public void testCountWithPredicatesPositiveCase2() throws ModelNodeInitException {
		getModelNode();

		String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf2:pma:validation\">"+
				"	<cache> "+
				"		<name>one</name>"+
				"		<testLeaf>notNeeded</testLeaf>"+
				"		<cacheFieldWithRelatveMustPath> "+
				"			<id>1</id>"+
				"			<ieName>constantX</ieName>"+
				"		</cacheFieldWithRelatveMustPath> "+
				"		<cacheFieldWithRelatveMustPath> "+
				"			<id>2</id>"+
				"			<ieName>constant1</ieName>"+
				"		</cacheFieldWithRelatveMustPath> "+
				"	</cache> "+
				"	<cache> "+
				"		<name>two</name>"+
				"		<testLeaf>notNeeded</testLeaf>"+
				"		<cacheFieldWithRelatveMustPath> "+
				"			<id>1</id>"+
				"			<ieName>constantX</ieName>"+
				"		</cacheFieldWithRelatveMustPath> "+
				"		<cacheFieldWithRelatveMustPath> "+
				"			<id>2</id>"+
				"			<ieName>constant2</ieName>"+
				"		</cacheFieldWithRelatveMustPath> "+
				"	</cache> "+
				"</validation>";
		editConfig(m_server, m_clientInfo, requestXml, true);
	}

	@Test
	public void testCountWithPredicatesNegitiveCase2() throws ModelNodeInitException {
		getModelNode();

		String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation xmlns=\"urn:org:bbf2:pma:validation\">"+
				"	<cache> "+
				"		<name>one</name>"+
				"		<testLeaf>notNeeded</testLeaf>"+
				"		<cacheFieldWithRelatveMustPath> "+
				"			<id>1</id>"+
				"			<ieName>constantX</ieName>"+
				"		</cacheFieldWithRelatveMustPath> "+
				"		<cacheFieldWithRelatveMustPath> "+
				"			<id>2</id>"+
				"			<ieName>constant1</ieName>"+
				"		</cacheFieldWithRelatveMustPath> "+
				"	</cache> "+
				"	<cache> "+
				"		<name>two</name>"+
				"		<testLeaf>notNeeded</testLeaf>"+
				"		<cacheFieldWithRelatveMustPath> "+
				"			<id>1</id>"+
				"			<ieName>constantX</ieName>"+
				"		</cacheFieldWithRelatveMustPath> "+
				"		<cacheFieldWithRelatveMustPath> "+
				"			<id>2</id>"+
				"			<ieName>constant1</ieName>"+
				"		</cacheFieldWithRelatveMustPath> "+
				"	</cache> "+
				"</validation>";
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
		NetconfRpcError error = response.getErrors().get(0);
		assertEquals("An ieName must be unique for constant 1&2", error.getErrorMessage());
		assertEquals("must-violation", error.getErrorAppTag());
	}

	@Test
	public void testValidCount() throws ModelNodeInitException {
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				 "<validation xmlns=\"urn:org:bbf2:pma:validation\">"+
				 "	<count-validation> "+
				 "		<countable>8</countable>"+
				 "		<count-list>"+
				 "			<leaf1>10</leaf1>"+  
				 "		</count-list> "+
                 "      <count-list>"+
                 "          <leaf1>20</leaf1>"+  
                 "      </count-list> "+
				 "	</count-validation> "+
				 "</validation>"
				 ;

		getModelNode();
		EditConfigRequest request1 = createRequestFromString(requestXml1);
		request1.setMessageId("1");
		NetConfResponse response1 = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request1, response1);
        assertFalse(response1.isOk());
        assertNull(response1.getData());
        assertEquals("Violate when constraints: count(../../count-list) = 1", response1.getErrors().get(0).getErrorMessage());
        String expectedPath1 = "/validation:validation/validation:count-validation/validation:count-list[validation:leaf1='10']/validation:leaf1";
        String expectedPath2 = "/validation:validation/validation:count-validation/validation:count-list[validation:leaf1='20']/validation:leaf1";
        String errorPath = response1.getErrors().get(0).getErrorPath();
        // changeNodeMap contains a set of EditContainmentNodes, so which one will be used for the error is unpredictable
        assertTrue(expectedPath1.equals(errorPath) || expectedPath2.equals(errorPath));
	}
	
	@Test
	public void testValidCount1() throws Exception {
        String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf2:pma:validation\">"+
                "  <count-validation> "+
                "      <countable>8</countable>"+
                "      <value2>0</value2>"+
                "      <count-list>"+
                "          <leaf1>11</leaf1>"+
                "      </count-list> "+
                "  </count-validation> "+
                "</validation>"
                ;

       getModelNode();
       EditConfigRequest request2 = createRequestFromString(requestXml2);
       request2.setMessageId("1");
       NetConfResponse response2 = new NetConfResponse().setMessageId("1");
       m_server.onEditConfig(m_clientInfo, request2, response2);
       assertFalse(response2.isOk());
       assertNull(response2.getData());
       assertEquals("Violate when constraints: count(../countable) = 0", response2.getErrors().get(0).getErrorMessage());
	    
	}
	
	@Test
	public void testWhenDeleteNode() throws Exception {        
	    getModelNode();
	    m_dataStore.enableUTSupport();
        List<AbstractNetconfRequest> requestList = m_dataStore.getRequestListForTest();
        assertEquals(0, requestList.size());
        
	    String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
	            "<validation xmlns=\"urn:org:bbf2:pma:validation\">"+
	            "  <count-validation> "+
	            "      <value2>0</value2>"+
	            "      <count-list>"+
	            "          <leaf1>11</leaf1>"+
	            "      </count-list> "+
	            "  </count-validation> "+
	            "</validation>"
	            ;

	    EditConfigRequest request1 = createRequestFromString(requestXml1);
	    request1.setMessageId("1");
	    NetConfResponse response1 = new NetConfResponse().setMessageId("1");
	    m_server.onEditConfig(m_clientInfo, request1, response1);
	    
	    requestList = m_dataStore.getRequestListForTest();
	    assertEquals(1, requestList.size());
	    
	    EditConfigRequest req = (EditConfigRequest)requestList.get(0);
	    assertXMLStringEquals(request1.requestToString(), req.requestToString());
	    
	    assertTrue(response1.isOk());

	    String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
	            "<validation xmlns=\"urn:org:bbf2:pma:validation\">"+
	            "  <count-validation> "+
	            "    <countable>8</countable>"+
	            "  </count-validation> "+
	            "</validation>"
	            ;

	    EditConfigRequest request2 = createRequestFromString(requestXml2);
	    request2.setMessageId("1");
	    NetConfResponse response2 = new NetConfResponse().setMessageId("1");
	    m_server.onEditConfig(m_clientInfo, request2, response2);
	    assertTrue(response2.isOk());
        
	    requestList = m_dataStore.getRequestListForTest();
	    assertEquals(3, requestList.size());

        req = (EditConfigRequest)requestList.get(1);
        assertXMLStringEquals(request2.requestToString(), req.requestToString());

	    req = (EditConfigRequest)requestList.get(2);
        
        String implicitDelete = "<rpc message-id=\"internal_edit:1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\"> "
                + "  <edit-config> "
                + "    <target> "
                + "      <running/> "
                + "    </target> "
                + "    <default-operation>merge</default-operation> "
                + "    <test-option>set</test-option> "
                + "    <error-option>stop-on-error</error-option> "
                + "    <config> "
                + "      <validation xmlns=\"urn:org:bbf2:pma:validation\"> "
                + "        <count-validation> "
                + "          <value2 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\">0</value2> "
                + "        </count-validation> "
                + "      </validation> "
                + "    </config> "
                + "  </edit-config> "
                + "</rpc>";
        assertXMLStringEquals(implicitDelete, req.requestToString());
	}
	
	@Test
	public void testSelfCountList() throws ModelNodeInitException{
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				 "<validation xmlns=\"urn:org:bbf2:pma:validation\">"+
				 "	<count-validation> "+
				 "		<count-list>"+
				 "			<leaf1>10</leaf1>"+  
				 "		</count-list> "+
				 "	</count-validation> "+
				 "</validation>"
				 ;

		getModelNode();
		editConfig(m_server, m_clientInfo, requestXml1, true);
		
		String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				 "<validation xmlns=\"urn:org:bbf2:pma:validation\">"+
				 "	<count-validation> "+
				 "		<count-list>"+
				 "			<leaf1>10</leaf1>"+  
				 "			<leaf2>11</leaf2>"+  
				 "		</count-list> "+
				 "	</count-validation> "+
				 "</validation>"
				 ;

		getModelNode();
        editConfig(m_server, m_clientInfo, requestXml2, true);
		
		
	}
	
	@Test
	public void testIdentity() throws ModelNodeInitException {
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				 "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"+
				 "<validation:identity-validation>"+
				 "		<validation:leaf1>identity1</validation:leaf1> "+
				 "	</validation:identity-validation> " +
				 " </validation:validation>"
				 ;

		getModelNode();
		EditConfigRequest request1 = createRequestFromString(requestXml1);
		request1.setMessageId("1");
		NetConfResponse response1 = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request1, response1);
		assertTrue(response1.isOk());

		String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				 "<validation xmlns=\"urn:org:bbf2:pma:validation\">"+
				 "<identity-validation xmlns=\"urn:org:bbf2:pma:validation\">"+
				 "		<leaf2>1</leaf2> "+
				 "	</identity-validation> " +
				 " </validation>"
				 ;

		EditConfigRequest request2 = createRequestFromString(requestXml2);
		request2.setMessageId("1");
		NetConfResponse response2 = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request2, response2);
		assertTrue(response2.isOk());
	}
	
	@Test
	public void testIdentity2() throws ModelNodeInitException{

		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				 "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"+
				 "	<validation:identity-validation>"+
				 "		<validation:leaf3 xmlns:validation11=\"urn:org:bbf2:pma:validation-yang11\">validation11:identity3</validation:leaf3> "+
				 "	</validation:identity-validation> " +
				 "</validation:validation>"
				 ;

		getModelNode();
		EditConfigRequest request1 = createRequestFromString(requestXml1);
		request1.setMessageId("1");
		NetConfResponse response1 = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request1, response1);
		assertTrue(response1.isOk());
		
		String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				 "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"+
				 "	<validation:identity-validation>"+
				 "		<validation:leaf3 xmlns:validation11=\"urn:org:bbf2:pma:validation-yang11\">validation11:identity2</validation:leaf3> "+
				 "	</validation:identity-validation> " +
				 "</validation:validation>"
				 ;
		
		request1 = createRequestFromString(requestXml2);
		request1.setMessageId("1");
		response1 = new NetConfResponse().setMessageId("1");
		try{
		m_server.onEditConfig(m_clientInfo, request1, response1);
		}catch(ValidationException e){
			NetconfRpcError rpcError = e.getRpcError();
			assertEquals(NetconfRpcErrorTag.INVALID_VALUE, rpcError.getErrorTag());
			assertEquals("Value \"validation11:identity2\" is not a valid identityref value.", rpcError.getErrorMessage());
			assertEquals("/validation:validation/validation:identity-validation/validation:leaf3", rpcError.getErrorPath());			
		}	
	}
	
	@Test
	public void testNotEqualOperator2() throws ModelNodeInitException {
		String requestXml3 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				 "<validation xmlns=\"urn:org:bbf2:pma:validation\">"+
				 "	<when-validation> "+
				 "		<result-leaf>10</result-leaf> "+
				 "	</when-validation> "+
				 "</validation>"
				 ;

		getModelNode();
		EditConfigRequest request3 = createRequestFromString(requestXml3);
		request3.setMessageId("1");
		NetConfResponse response3 = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request3, response3);
		assertTrue(response3.isOk());

		String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				 "<validation xmlns=\"urn:org:bbf2:pma:validation\">"+
				 "	<when-validation> "+
				 "		<not-equal>test1</not-equal> "+
				 "	</when-validation> "+
				 "</validation>"
				 ;

		
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
				 			 "	<validation xmlns=\"urn:org:bbf2:pma:validation\">"+
				 			 "  	<arithmetic-validation> "+
				 			 "		 	<value1>15</value1> "+
				 			 "	    </arithmetic-validation> "+
				 			 "</validation>"
				 			 ;

		getModelNode();
		EditConfigRequest request1 = createRequestFromString(requestXml1);
		request1.setMessageId("1");
		NetConfResponse response1 = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request1, response1);
		assertTrue(response1.isOk());
		
		// add leaf to check + operator
		String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
							 "	<validation xmlns=\"urn:org:bbf2:pma:validation\">"+
							 "		<arithmetic-validation> "+
							 "			<fail-must-leaf >15</fail-must-leaf > "+
							 "		</arithmetic-validation> "+
							 "  </validation>"
							 ;

		getModelNode();
		EditConfigRequest request2 = createRequestFromString(requestXml2);
		request2.setMessageId("1");
		NetConfResponse response2 = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request2, response2);
		assertFalse(response2.isOk());
		assertTrue(response2.getErrors().get(0).getErrorMessage().contains("Violate must constraints: ../value1 + 10 < 0"));
		assertNull(response2.getData());
		
	}

	private void setUp_TestArthimeticNotOperation() throws ModelNodeInitException, SAXException, IOException {
		String expectedResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
				"<data>\n" +
				"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">\n" +
				"<validation:arithmetic-validation>\n" +
				"<validation:value1>15</validation:value1>\n" +
				"</validation:arithmetic-validation>\n" +
				"</validation:validation>\n" +
				"</data>\n" +
				"</rpc-reply>";

		sendEditConfigAndVerifyGetForArithmetic(
				getArithmeticValidationRequest("<value1>15</value1>"),
				expectedResponse);
	}

	@Test
	public void testArthimeticOperationOnMust_PassMustLeafNotExist() throws ModelNodeInitException, SAXException, IOException {

		setUp_TestArthimeticNotOperation();

		String expectedResponse  = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
				"<data>\n" +
				"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">\n" +
				"<validation:arithmetic-validation>\n" +
				"<validation:pass-must-leaf-not-exist>20</validation:pass-must-leaf-not-exist>\n" +
				"<validation:value1>15</validation:value1>\n" +
				"</validation:arithmetic-validation>\n" +
				"</validation:validation>\n" +
				"</data>\n" +
				"</rpc-reply>";

		sendEditConfigAndVerifyGetForArithmetic(
				getArithmeticValidationRequest("<pass-must-leaf-not-exist>20</pass-must-leaf-not-exist>"),
				expectedResponse);

		sendEditConfigAndVerifyFailureForArithmetic(
				getArithmeticValidationRequest("<must-value2-exist>15</must-value2-exist>"),
				"Violate must constraints: not (../must-value2-exist)",
				"/validation:validation/validation:arithmetic-validation/validation:pass-must-leaf-not-exist");
	}

	@Test
	public void testArthimeticOperationOnMust_PassMustLeafExist() throws ModelNodeInitException, SAXException, IOException {

		setUp_TestArthimeticNotOperation();

		sendEditConfigAndVerifyFailureForArithmetic(
				getArithmeticValidationRequest("<pass-must-leaf-exist>20</pass-must-leaf-exist>"),
				"Violate must constraints: not (not (../must-value3-exist))",
				"/validation:validation/validation:arithmetic-validation/validation:pass-must-leaf-exist");
	}

	@Test
	public void testArthimeticOperationOnMust_FailMustLeafNotCondition() throws ModelNodeInitException, SAXException, IOException {

		setUp_TestArthimeticNotOperation();

		String expectedResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
				"<data>\n" +
				"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">\n" +
				"<validation:arithmetic-validation>\n" +
				"<validation:must-value3-exist>15</validation:must-value3-exist>\n" +
				"<validation:pass-must-leaf-exist>20</validation:pass-must-leaf-exist>\n" +
				"<validation:value1>15</validation:value1>\n" +
				"</validation:arithmetic-validation>\n" +
				"</validation:validation>\n" +
				"</data>\n" +
				"</rpc-reply>";


		sendEditConfigAndVerifyGetForArithmetic(
				getArithmeticValidationRequest("<must-value3-exist>15</must-value3-exist><pass-must-leaf-exist>20</pass-must-leaf-exist>"),
				expectedResponse);

		expectedResponse= " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
				"<data>\n" +
				"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">\n" +
				"<validation:arithmetic-validation>\n" +
				"<validation:must-value3-exist>15</validation:must-value3-exist>\n" +
				"<validation:pass-must-leaf-exist>20</validation:pass-must-leaf-exist>\n" +
				"<validation:pass-must-leaf-not-condition>15</validation:pass-must-leaf-not-condition>\n" +
				"<validation:value1>15</validation:value1>\n" +
				"</validation:arithmetic-validation>\n" +
				"</validation:validation>\n" +
				"</data>\n" +
				"</rpc-reply>";

		sendEditConfigAndVerifyGetForArithmetic(
				getArithmeticValidationRequest("<pass-must-leaf-not-condition>15</pass-must-leaf-not-condition>"),
				expectedResponse);

		sendEditConfigAndVerifyFailureForArithmetic(
				getArithmeticValidationRequest("<fail-must-leaf-not-condition>15</fail-must-leaf-not-condition>"),
				"Violate must constraints: not (../value1 >= 15)",
				"/validation:validation/validation:arithmetic-validation/validation:fail-must-leaf-not-condition");

	}

	@Test
	public void testArthimeticOperationOnMust_FailMustLeafNotCondition1() throws ModelNodeInitException, SAXException, IOException {

		setUp_TestArthimeticNotOperation();

		sendEditConfigAndVerifyFailureForArithmetic(
				getArithmeticValidationRequest("<value2>15</value2><value3>15</value3><value4>45</value4><fail-must-leaf-not-condition1>15</fail-must-leaf-not-condition1>"),
				"Violate must constraints: not (../value2 - ../value1 < ../value4 div ../value1)",
				"/validation:validation/validation:arithmetic-validation/validation:fail-must-leaf-not-condition1");

	}



	@Test
	public void testArthimeticNotOperationOnWhen_PassWhenLeafExist() throws SAXException, IOException, ModelNodeInitException {

		setUp_TestArthimeticNotOperation();

		String expectedResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
				"<data>\n" +
				"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">\n" +
				"<validation:arithmetic-validation>\n" +
				"<validation:pass-when-leaf-not-exist>20</validation:pass-when-leaf-not-exist>\n" +
				"<validation:value1>15</validation:value1>\n" +
				"</validation:arithmetic-validation>\n" +
				"</validation:validation>\n" +
				"</data>\n" +
				"</rpc-reply>";

		sendEditConfigAndVerifyGetForArithmetic(
				getArithmeticValidationRequest("<pass-when-leaf-not-exist>20</pass-when-leaf-not-exist>"),
				expectedResponse);

		sendEditConfigAndVerifyFailureForArithmetic(
				getArithmeticValidationRequest("<pass-when-leaf-exist>20</pass-when-leaf-exist>"),
				"Violate when constraints: not(not(../when-value3-exist))",
				"/validation:validation/validation:arithmetic-validation/validation:pass-when-leaf-exist");
	}


	@Test
	public void testArthimeticNotOperationOnWhen_FailWhenLeafNotCondition() throws SAXException, IOException, ModelNodeInitException {

		setUp_TestArthimeticNotOperation();

		String expectedResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
				"<data>\n" +
				"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">\n" +
				"<validation:arithmetic-validation>\n" +
				"<validation:pass-when-leaf-exist>20</validation:pass-when-leaf-exist>\n" +
				"<validation:value1>15</validation:value1>\n" +
				"<validation:when-value3-exist>15</validation:when-value3-exist>\n" +
				"</validation:arithmetic-validation>\n" +
				"</validation:validation>\n" +
				"</data>\n" +
				"</rpc-reply>";


		sendEditConfigAndVerifyGetForArithmetic(
				getArithmeticValidationRequest("<when-value3-exist>15</when-value3-exist><pass-when-leaf-exist>20</pass-when-leaf-exist>"),
				expectedResponse);

		expectedResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
				"<data>\n" +
				"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">\n" +
				"<validation:arithmetic-validation>\n" +
				"<validation:value1>15</validation:value1>\n" +
				"</validation:arithmetic-validation>\n" +
				"</validation:validation>\n" +
				"</data>\n" +
				"</rpc-reply>";


		sendEditConfigAndVerifyGetForArithmetic(
				getArithmeticValidationRequest("<when-value3-exist xc:operation='remove'>15</when-value3-exist>"),
				expectedResponse);

		expectedResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
				"<data>\n" +
				"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">\n" +
				"<validation:arithmetic-validation>\n" +
				"<validation:pass-when-leaf-not-condition>15</validation:pass-when-leaf-not-condition>\n" +
				"<validation:value1>15</validation:value1>\n" +
				"</validation:arithmetic-validation>\n" +
				"</validation:validation>\n" +
				"</data>\n" +
				"</rpc-reply>";

		sendEditConfigAndVerifyGetForArithmetic(
				getArithmeticValidationRequest("<pass-when-leaf-not-condition>15</pass-when-leaf-not-condition>"),
				expectedResponse);

		sendEditConfigAndVerifyFailureForArithmetic(
				getArithmeticValidationRequest("<fail-when-leaf-not-condition>15</fail-when-leaf-not-condition>"),
				"Violate when constraints: not(../value1 >= 15)",
				"/validation:validation/validation:arithmetic-validation/validation:fail-when-leaf-not-condition");


	}


	@Test
	public void testArthimeticNotOperationOnWhen_FailWhenLeafNotCondition1() throws SAXException, IOException, ModelNodeInitException {

		setUp_TestArthimeticNotOperation();

		sendEditConfigAndVerifyFailureForArithmetic(
				getArithmeticValidationRequest("<value2>15</value2><value3>15</value3><value4>45</value4><fail-when-leaf-not-condition1>15</fail-when-leaf-not-condition1>"),
				"Violate when constraints: not(../value2 - ../value1 < ../value4 div ../value1)",
				"/validation:validation/validation:arithmetic-validation/validation:fail-when-leaf-not-condition1");

	}

	private String getArithmeticValidationRequest(String requestString)
	{
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<validation xmlns=\"urn:org:bbf2:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
				+ "	<arithmetic-validation>"
				+ requestString
				+ "	</arithmetic-validation>"
				+ "</validation>";
	}

	private void sendEditConfigAndVerifyGetForArithmetic(String requestXml, String espectedResponse) throws ModelNodeInitException, SAXException, IOException
	{
		getModelNode();
		EditConfigRequest request = createRequestFromString(requestXml);
		request.setMessageId("1");
		NetConfResponse response = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request, response);
		verifyGet(espectedResponse);
	}

	private void sendEditConfigAndVerifyFailureForArithmetic(String requestXml,
		String expectedErrorMessageXml,
		String expectedErrorPath) throws ModelNodeInitException
	{
		getModelNode();
		EditConfigRequest request = createRequestFromString(requestXml);
		request.setMessageId("1");
		NetConfResponse response = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request, response);
		assertFalse(response.isOk());
		assertEquals(expectedErrorMessageXml, response.getErrors().get(0).getErrorMessage());
		assertEquals(expectedErrorPath, response.getErrors().get(0).getErrorPath());
	}

	@Test
	public void testArthimeticOperation() throws ModelNodeInitException {
		/// Add first leaf
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				 			 "	<validation xmlns=\"urn:org:bbf2:pma:validation\">"+
				 			 "  	<arithmetic-validation> "+
				 			 "		 	<value1>15</value1> "+
				 			 "	    </arithmetic-validation> "+
				 			 "</validation>"
				 			 ;

		getModelNode();
		EditConfigRequest request1 = createRequestFromString(requestXml1);
		request1.setMessageId("1");
		NetConfResponse response1 = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request1, response1);
		assertTrue(response1.isOk());
		
		// add leaf to check + operator
		String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
							 "	<validation xmlns=\"urn:org:bbf2:pma:validation\">"+
							 "		<arithmetic-validation> "+
							 "			<value2>15</value2> "+
							 "		</arithmetic-validation> "+
							 "  </validation>"
							 ;

		getModelNode();
		EditConfigRequest request2 = createRequestFromString(requestXml2);
		request2.setMessageId("1");
		NetConfResponse response2 = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request2, response2);
		assertTrue(response2.isOk());
		
		// add leaf to check - operator
		String requestXml3 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				 "	<validation xmlns=\"urn:org:bbf2:pma:validation\">"+
				 "		<arithmetic-validation> "+
				 "			<value3>15</value3> "+
				 "		</arithmetic-validation> "+
				 "  </validation>"
				 ;

		getModelNode();
		EditConfigRequest request3 = createRequestFromString(requestXml3);
		request3.setMessageId("1");
		NetConfResponse response3 = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request3, response3);
		assertTrue(response3.isOk());

		// add leaf to check * operator
		String requestXml4 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				 "	<validation xmlns=\"urn:org:bbf2:pma:validation\">"+
				 "		<arithmetic-validation> "+
				 "			<value4>45</value4> "+
				 "		</arithmetic-validation> "+
				 "  </validation>"
				 ;

		getModelNode();
		EditConfigRequest request4 = createRequestFromString(requestXml4);
		request4.setMessageId("1");
		NetConfResponse response4 = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request4, response4);
		assertTrue(response4.isOk());

		// add leaf to check div operator
		String requestXml5 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				 "	<validation xmlns=\"urn:org:bbf2:pma:validation\">"+
				 "		<arithmetic-validation> "+
				 "			<value5>15</value5> "+
				 "		</arithmetic-validation> "+
				 "  </validation>"
				 ;

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
				 			 "	<validation xmlns=\"urn:org:bbf2:pma:validation\">"+
				 			 "  	<arithmetic-validation> "+
				 			 "		 	<value1>15</value1> "+
				 			 "	    </arithmetic-validation> "+
				 			 "</validation>"
				 			 ;

		getModelNode();
		EditConfigRequest request1 = createRequestFromString(requestXml1);
		request1.setMessageId("1");
		NetConfResponse response1 = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request1, response1);
		assertTrue(response1.isOk());
		
		//add leaf to check mod operator
		String requestXml6 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				 "	<validation xmlns=\"urn:org:bbf2:pma:validation\">"+
				 "		<arithmetic-validation> "+
				 "			<all-arith-leaf>10</all-arith-leaf>"+
				 "			<all-must-leaf>10</all-must-leaf>"+
				 "			<abs-leaf>10</abs-leaf>"+
				 "			<mod-leaf>15</mod-leaf> "+
				 "		</arithmetic-validation> "+
				 "  </validation>"
				 ;

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
                "  <validation xmlns=\"urn:org:bbf2:pma:validation\">"+
                "    <validation>validation</validation>" +
                "    <crossConstant>10</crossConstant>"+
                "    <constantCheck>10</constantCheck>"+
                "</validation>"
                ;
        editConfig(m_server, m_clientInfo, requestXml1, true);
	}
	
	@Test
	public void testNotEqualOperator() throws ModelNodeInitException {
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
							 "<validation xmlns=\"urn:org:bbf2:pma:validation\">"+
							 "	<when-validation> "+
							 "		<result-leaf>15</result-leaf> "+
							 "	</when-validation> "+
							 "</validation>"
							 ;
		
		getModelNode();
		EditConfigRequest request1 = createRequestFromString(requestXml1);
		request1.setMessageId("1");
		NetConfResponse response1 = new NetConfResponse().setMessageId("1");
		m_server.onEditConfig(m_clientInfo, request1, response1);
		assertTrue(response1.isOk());
        
		String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				 "<validation xmlns=\"urn:org:bbf2:pma:validation\">"+
				 "	<when-validation> "+
				 "		<not-equal>test1</not-equal> "+
				 "	</when-validation> "+
				 "</validation>"
				 ;
		
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
							 "<validation xmlns=\"urn:org:bbf2:pma:validation\">			" +
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
							 "</validation>													" ;
        getModelNode();
        EditConfigRequest request1 = createRequestFromString(requestXml1);
        request1.setMessageId("1");
        NetConfResponse response1 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request1, response1);
        assertTrue(response1.isOk());

        String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>					" +
							 "<validation xmlns=\"urn:org:bbf2:pma:validation\"> 		" +
							 "    <leaf-ref> 												" +
							 "		<current-alone>											" +
							 "			<current-parent-leaf>Album2</current-parent-leaf>	" +
							 "			<current-leaf-list>Test1</current-leaf-list>		" +
							 "			<current-leaf-list>Test2</current-leaf-list>		" +
							 "		</current-alone> 										" +
							 "	  </leaf-ref>												" +
							 "</validation>" ;
        getModelNode();
        EditConfigRequest request2 = createRequestFromString(requestXml2);
        request2.setMessageId("1");
        NetConfResponse response2 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request2, response2);
        assertFalse(response2.isOk());
	}

	@Test
	public void testInvalidInstanceIdentifierForNotExistingContainer() throws ModelNodeInitException {
		testFail("/datastorevalidatortest/instanceidentifervalidation/defaultxml/invalid-instance-identifier-container.xml", NetconfRpcErrorTag.DATA_MISSING, NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error, "instance-required", "Missing required element /validation/instance-identifier-example/abc", "/validation:validation/validation:instance-identifier-example/validation:student[validation:student-id='ST001']/validation:student-instance-identifier1");
	}
	
	@Test
	public void testValidInstanceIdentifierWithContainerPath() throws ModelNodeInitException {
		testPass("/datastorevalidatortest/instanceidentifervalidation/defaultxml/valid-instance-identifier-container.xml");
	}
	
	@Test
	public void testValidInstanceIdentifierForListPath() throws ModelNodeInitException {
		testPass("/datastorevalidatortest/instanceidentifervalidation/defaultxml/valid-instance-identifier-list.xml");
	}
	
	@Test
	public void testValidInstanceIdentifierForListLeafPath() throws ModelNodeInitException {
		testPass("/datastorevalidatortest/instanceidentifervalidation/defaultxml/valid-instance-identifier-leaf-of-list.xml");
	}
	
	@Test
	public void testInvalidInstanceIdentifierForLeafInList() throws ModelNodeInitException {
		testFail("/datastorevalidatortest/instanceidentifervalidation/defaultxml/invalid-instance-identifier-leaf-of-list.xml", NetconfRpcErrorTag.DATA_MISSING, NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error, "instance-required", "Missing required element /validation/instance-identifier-example/subject[subject-id = 'SJ001']/subject-name", "/validation:validation/validation:instance-identifier-example/validation:student[validation:student-id='ST001']/validation:student-instance-identifier1");
	}
	
	@Test
	public void testInvalidInstanceIdentifierWithRequireInstance() throws ModelNodeInitException {
		testFail("/datastorevalidatortest/instanceidentifervalidation/defaultxml/invalid-instance-identifier-with-require-instance.xml", 
				NetconfRpcErrorTag.DATA_MISSING, NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error, 
				"instance-required", 
				"Missing required element /validation/instance-identifier-example/address/national[national-id=BN]", 
				"/validation:validation/validation:instance-identifier-example/validation:student[validation:student-id='ST001']/validation:student-instance-identifier1");
	}
	
	@Test
	public void testValidInstanceIdentifierListIncludedMultipleKeys() throws ModelNodeInitException {
	    String requestXml1 = "/datastorevalidatortest/instanceidentifervalidation/defaultxml/valid-instance-identifier-list-with-two-keys.xml";
        getModelNode();
        EditConfigRequest request1 = createRequest(requestXml1);
        request1.setMessageId("1");
        NetConfResponse response1 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request1, response1);
        
        assertTrue(response1.isOk());
        
		testPass("/datastorevalidatortest/instanceidentifervalidation/defaultxml/create-instance-identifier-list-with-two-keys.xml");
	}
	
	@Test
	public void testValidInstanceIdentifierForLeaf() throws ModelNodeInitException {
		testPass("/datastorevalidatortest/instanceidentifervalidation/defaultxml/valid-instance-identifier-leaf.xml");
	}
	
	@Test
	public void testInvalidInstanceIdentifierForLeaf() throws ModelNodeInitException {
		testFail("/datastorevalidatortest/instanceidentifervalidation/defaultxml/invalid-instance-identifier-leaf.xml", NetconfRpcErrorTag.DATA_MISSING, NetconfRpcErrorType.Application,  NetconfRpcErrorSeverity.Error, "instance-required", "Missing required element /validation/instance-identifier-example/subject[1]/subject-name", "/validation:validation/validation:instance-identifier-example/validation:student[validation:student-id='ST001']/validation:student-instance-identifier1");
	}
	
	@Test
	public void testValidInstanceIdentifierForIndexList() throws ModelNodeInitException {
		testPass("/datastorevalidatortest/instanceidentifervalidation/defaultxml/valid-instance-identifier-index-list.xml");
	}
	
	@Test
	public void testInValidInstanceIdentifierForIndexList() throws ModelNodeInitException {
		testFail("/datastorevalidatortest/instanceidentifervalidation/defaultxml/invalid-instance-identifier-index-list.xml", NetconfRpcErrorTag.DATA_MISSING, NetconfRpcErrorType.Application,  NetconfRpcErrorSeverity.Error, "instance-required", "Missing required element /validation/instance-identifier-example/subject[2]/subject-name", "/validation:validation/validation:instance-identifier-example/validation:student[validation:student-id='ST001']/validation:student-instance-identifier1");
	}
	
	@Test		
	public void testViolateWhenconstraints() throws ModelNodeInitException {		
	    String requestXml1 = "/datastorevalidatortest/rangevalidation/defaultxml/invalid-when-constraint-leaf.xml";
        getModelNode();
        EditConfigRequest request1 = createRequest(requestXml1);
        request1.setMessageId("1");
        NetConfResponse response1 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request1, response1);
		testFail("/datastorevalidatortest/rangevalidation/defaultxml/create-when-leaf.xml", NetconfRpcErrorTag.UNKNOWN_ELEMENT,
				NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error, "when-violation",					
				"Violate when constraints: ../result-leaf >= 10", "/validation:validation/validation:when-validation/validation:leaf-type");		
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
		testFail("/datastorevalidatortest/rangevalidation/defaultxml/invalid-must-constraint-leaf.xml", NetconfRpcErrorTag.OPERATION_FAILED,		
				NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error, "must-violation",				
				"An MTU must be  100 .. 5000", "/validation:validation/validation:must-validation/validation:leaf-type");	
	}
	
	
	@Test
	public void testViolateWhenConstraintForLeafCaseNode() throws ModelNodeInitException {
	    String requestXml1 = "/datastorevalidatortest/rangevalidation/defaultxml/invalid-when-constraint-leaf-casenode-1.xml";
        getModelNode();
        EditConfigRequest request1 = createRequest(requestXml1);
        request1.setMessageId("1");
        NetConfResponse response1 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request1, response1);
	    
		testFail("/datastorevalidatortest/rangevalidation/defaultxml/create-when-constraint-leaf-casenode-1.xml", NetconfRpcErrorTag.UNKNOWN_ELEMENT,
				NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error, "when-violation",
				"Violate when constraints: validation:result-choice = 'success'", "/validation:validation/validation:when-validation/validation:choicecase/validation:leaf-case-success");
		
		requestXml1 = "/datastorevalidatortest/rangevalidation/defaultxml/invalid-when-constraint-leaf-casenode-2.xml";
        getModelNode();
        request1 = createRequest(requestXml1);
        request1.setMessageId("1");
        response1 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request1, response1);
		
		testFail("/datastorevalidatortest/rangevalidation/defaultxml/create-when-constraint-leaf-casenode-2.xml", NetconfRpcErrorTag.UNKNOWN_ELEMENT,
				NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error, "when-violation",
				"Violate when constraints: validation:result-choice = 'failed'", "/validation:validation/validation:when-validation/validation:choicecase/validation:leaf-case-failed");
	}

	
	@Test
	public void testValidWhenConstraintForSuccessLeafCase() throws ModelNodeInitException {
	    String requestXml1 = "/datastorevalidatortest/rangevalidation/defaultxml/valid-when-constraint-leaf-casenode-1.xml";
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
        String requestXml1 = "/datastorevalidatortest/rangevalidation/defaultxml/valid-when-constraint-leaf-casenode-2.xml";
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
                NetconfRpcErrorTag.DATA_MISSING, NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error, "instance-required",
                "Dependency violated, 'validation1' must exist", "/validation:validation/validation:leafref-validation");
    }

	@Override
	protected void initialiseInterceptor() {
		m_addDefaultDataInterceptor = new AddDefaultDataInterceptor(m_modelNodeHelperRegistry, m_schemaRegistry, m_expValidator);
		m_addDefaultDataInterceptor.init();
	}

    @Test
    public void testValidAbsoluteWhenConstraintForSuccessLeafCase() throws ModelNodeInitException {
        String requestXml1 = "/datastorevalidatortest/rangevalidation/defaultxml/valid-when-constraint-leaf-casenode-1.xml";
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
        String requestXml1 = "/datastorevalidatortest/rangevalidation/defaultxml/valid-when-constraint-abs-leaflist-casenode-1.xml";
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
        String requestXml1 = "/datastorevalidatortest/rangevalidation/defaultxml/valid-when-constraint-leaf-casenode-2.xml";
        getModelNode();
        EditConfigRequest request1 = createRequest(requestXml1);
        request1.setMessageId("1");
        NetConfResponse response1 = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request1, response1);

        assertTrue(response1.isOk());

        testFail("/datastorevalidatortest/rangevalidation/defaultxml/create-when-constraint-leaf-abs-casenode-1.xml",
                NetconfRpcErrorTag.UNKNOWN_ELEMENT, NetconfRpcErrorType.Application,NetconfRpcErrorSeverity.Error, 
                "when-violation", "Violate when constraints: /validation/when-validation/choicecase/result-choice = 'success'",
                "/validation:validation/validation:when-validation/validation:choicecase/validation:absolute-case-success");
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
                "when-violation","Violate when constraints: ../result-leaf != 0 and /validation/when-validation/result-leaf != 15",
                "/validation:validation/validation:when-validation/validation:NotEqualLeaf3");
    }
    
    @Test
    public void testRootContinerOnListAddition() throws Exception{

        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation1 xmlns=\"urn:org:bbf2:pma:validation\">"+
                "  <leaf1>leaf1</leaf1>"+
                "</validation1>"
                ;
        getModelNode();
        editConfig(m_server, m_clientInfo, requestXml1, true);
       
        String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation1 xmlns=\"urn:org:bbf2:pma:validation\">"+
                "  <list1>"+
                "    <key1>key1</key1>"+
                "  </list1>"+
                "</validation1>"
                ;

        editConfig(m_server, m_clientInfo, requestXml2, true);
        
        String requestXml3 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation1 xmlns=\"urn:org:bbf2:pma:validation\">"+
                "  <list1>"+
                "    <key1>key2</key1>"+
                "  </list1>"+
                "</validation1>"
                ;
        NetConfResponse response3 = editConfig(m_server, m_clientInfo, requestXml3, false);

        assertFalse(response3.isOk());
        assertEquals("Violate must constraints: count(list1) <= 1", response3.getErrors().get(0).getErrorMessage());
        assertEquals("/validation:validation1", response3.getErrors().get(0).getErrorPath());
       

    }

	@Test
	public void testCurrentInPredicatesSuccessCase() throws Exception {
		getModelNode();
		/**
		 * Expecation is otherLeaf's value substring from 'C' should be equal to key1
		 * in the success case I have given otehrLeaf as abCtarget - substring of 'C' is target, i have given that value in key1
		 */
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<currentInPredicates xmlns=\"urn:org:bbf2:pma:validation\">" +
				"    <mustLeaf>test</mustLeaf>" +
				"    <otherLeaf>abCtarget</otherLeaf>" +
				"  <currentInPredicatesList>" +
				"    <key1>target</key1>" +
				"  </currentInPredicatesList>" +
				"</currentInPredicates>";
		editConfig(m_server, m_clientInfo, requestXml1, true);
	}

	@Test
	public void testCurrentInPredicatesFailureCase() throws Exception {
		getModelNode();
		/**
		 * Expecation is otherLeaf's value substring from 'C' should be equal to key1
		 * in the Failure case I have given otehrLeaf as abCtargetMissed - substring of 'C' is targetMissed, But I have given the value of key1 as just target.
		 * So it should fail
		 */
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<currentInPredicates xmlns=\"urn:org:bbf2:pma:validation\">" +
				"    <mustLeaf>mustPresent</mustLeaf>" +
				"    <otherLeaf>abCtargetMissed</otherLeaf>" +
				"  <currentInPredicatesList>" +
				"    <key1>key</key1>" +
				"  	 <innerListTarget>" +
				"       <key2>key1</key2>" +
				"    </innerListTarget>" +
				"  </currentInPredicatesList>" +
				"</currentInPredicates>";
		editConfig(m_server, m_clientInfo, requestXml1, false);
	}

//	@Test FNMS-83325
	public void testCurrentInPredicatesSuccessCaseExpPath() throws Exception {
		getModelNode();
		/**
		 * Expecation is otherLeaf's value substring from 'C' should be equal to key1
		 * in the success case I have given otehrLeaf as abCtarget - substring of 'C' is target, i have given that value in key1
		 */
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<currentInPredicates xmlns=\"urn:org:bbf2:pma:validation\">" +
				"    <mustLeafForExpPath>test</mustLeafForExpPath>" +
				"    <otherLeaf>abCtarget</otherLeaf>" +
				"  <currentInPredicatesList>" +
				"    <key1>target</key1>" +
				"  </currentInPredicatesList>" +
				"</currentInPredicates>";
		editConfig(m_server, m_clientInfo, requestXml1, true);
	}

//	@Test FNMS-83325
	public void testCurrentInPredicatesFailureCaseExpPath() throws Exception {
		getModelNode();
		/**
		 * Expecation is otherLeaf's value substring from 'C' should be equal to key1
		 * in the Failure case I have given otehrLeaf as abCtargetMissed - substring of 'C' is targetMissed, But I have given the value of key1 as just target.
		 * So it should fail
		 */
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<currentInPredicates xmlns=\"urn:org:bbf2:pma:validation\">" +
				"    <mustLeafForExpPath>mustPresent</mustLeafForExpPath>" +
				"    <otherLeaf>abCtargetMissed</otherLeaf>" +
				"  <currentInPredicatesList>" +
				"    <key1>key</key1>" +
				"  	 <innerListTarget>" +
				"       <key2>key1</key2>" +
				"    </innerListTarget>" +
				"  </currentInPredicatesList>" +
				"</currentInPredicates>";
		editConfig(m_server, m_clientInfo, requestXml1, false);
	}
    
    @Test
    public void testMustConstraintOnListChildNode() throws Exception{

        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation2 xmlns=\"urn:org:bbf2:pma:validation\">"+
                "  <list1>"+
                "    <key1>key1</key1>"+
                "    <enabled>true</enabled>"+
                "  </list1>"+
                "</validation2>"
                ;
        getModelNode();
        editConfig(m_server, m_clientInfo, requestXml1, true);
       
        String response = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "  <validation:validation2 xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                + "    <validation:list1>"
                + "     <validation:key1>key1</validation:key1>"
                + "     <validation:enabled>true</validation:enabled>"
                + "    </validation:list1>"
                + "  </validation:validation2>"
                + " </data>"
                + "</rpc-reply>"
                ;
        verifyGet(response);
        
        String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation2 xmlns=\"urn:org:bbf2:pma:validation\">"+
                "  <list1>"+
                "    <key1>key2</key1>"+
                "    <enabled>true</enabled>"+
                "  </list1>"+
                "</validation2>"
                ;

        NetConfResponse response2 = editConfig(m_server, m_clientInfo, requestXml2, false);

        assertFalse(response2.isOk());
        assertEquals("Violate must constraints: count(list1[enabled='true']) <= 1", response2.getErrors().get(0).getErrorMessage());
        assertEquals("/validation:validation2", response2.getErrors().get(0).getErrorPath());
        
        String requestXml3 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation2 xmlns=\"urn:org:bbf2:pma:validation\">"+
                "  <list1>"+
                "    <key1>key2</key1>"+
                "    <enabled>false</enabled>"+
                "  </list1>"+
                "</validation2>"
                ;
        editConfig(m_server, m_clientInfo, requestXml3, true);


    }
    
    @Test
    public void testMustConstraintOnListChildNodeDepthCase() throws Exception{

        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation3 xmlns=\"urn:org:bbf2:pma:validation\">"+
                "  <must-validation-with-mandatory>"+
                "    <value>test</value>" +
                "    <mandatory-leaf>yes</mandatory-leaf>"+
                "  </must-validation-with-mandatory>"+
                "  <must-validation1>"+
                "    <key1>key</key1>" +
                "    <value>test</value>" +
                "    <mandatory-one>yes</mandatory-one>" +
                "  </must-validation1>"+
                "  <list1>"+
                "    <key1>key1</key1>"+
                "    <container1>"+
                "    <list2>"+
                "    <key2>key2</key2>"+
                "    <enabled>true</enabled>"+
                "    </list2>"+
                "    </container1>"+
                "  </list1>"+
                "</validation3>"
                ;
        getModelNode();
        editConfig(m_server, m_clientInfo, requestXml1, true);

        String response = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "  <validation:validation3 xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                + "   <validation:must-validation-with-mandatory>"
                + "     <validation:mandatory-leaf>yes</validation:mandatory-leaf>"
                + "     <validation:value>test</validation:value>"
                + "   </validation:must-validation-with-mandatory>"
                + "   <validation:must-validation1>" 
                + "     <validation:key1>key</validation:key1>"
                + "     <validation:mandatory-one>yes</validation:mandatory-one>"
                + "     <validation:value>test</validation:value>" 
                + "   </validation:must-validation1>"                
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
                + "</rpc-reply>"
                ;
        verifyGet(response);
        
        String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation3 xmlns=\"urn:org:bbf2:pma:validation\">"+
                "  <must-validation-with-mandatory>"+
                "    <value>test</value>" +
                "    <mandatory-leaf>yes</mandatory-leaf>"+
                "  </must-validation-with-mandatory>"+
                "  <must-validation1>"+
                "    <key1>key</key1>" +
                "    <value>test</value>" +
                "    <mandatory-one>yes</mandatory-one>" +
                "  </must-validation1>"+                
                "  <list1>"+
                "    <key1>key1</key1>"+
                "    <container1>"+
                "    <list2>"+
                "    <key2>key3</key2>"+
                "    <enabled>true</enabled>"+
                "    </list2>"+
                "    </container1>"+
                "  </list1>"+
                "</validation3>"
                ;

        NetConfResponse response2 = editConfig(m_server, m_clientInfo, requestXml2, false);
        assertFalse(response2.isOk());
        assertEquals("Violate must constraints: count(../container1/list2[enabled='true']) <= 1", response2.getErrors().get(0).getErrorMessage());
        assertEquals("/validation:validation3/validation:list1[validation:key1='key1']/validation:container1", response2.getErrors().get(0).getErrorPath());
        
        String requestXml3 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation3 xmlns=\"urn:org:bbf2:pma:validation\">"+
                "  <must-validation-with-mandatory>"+
                "    <value>test</value>" +
                "    <mandatory-leaf>yes</mandatory-leaf>"+
                "  </must-validation-with-mandatory>"+
                "  <must-validation1>"+
                "    <key1>key</key1>" +
                "    <value>test</value>" +
                "    <mandatory-one>yes</mandatory-one>" +
                "  </must-validation1>"+                
                "  <list1>"+
                "    <key1>key1</key1>"+
                "    <container1>"+
                "    <list2>"+
                "    <key2>key3</key2>"+
                "    <enabled>false</enabled>"+
                "    </list2>"+
                "    </container1>"+
                "  </list1>"+
                "</validation3>"
                ;
        editConfig(m_server, m_clientInfo, requestXml3, true);

    }
    
    @Test
    public void testDefaultLeafCreationOnWhen() throws Exception {


    	getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf2:pma:validation\">"+
                "  <validation>default</validation>"+
                "</validation>"
                ;
        editConfig(m_server, m_clientInfo, requestXml1, true);
        
        String response = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                + "   <validation:validation>default</validation:validation>"
                + "   <validation:defaultLeaf>1</validation:defaultLeaf>"
                + "  </validation:validation>"
                + " </data>"
                + "</rpc-reply>"
                ;
        verifyGet(response);

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf2:pma:validation\">"+
                "  <validation>default1</validation>"+
                "</validation>"
                ;
        editConfig(m_server, m_clientInfo, requestXml1, true);
        
        response = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
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
                "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + "<leaf-ref>"
                + " <current-alone>"
                + "  <current-alone-list>"
                + "    <key>10</key>"
                + "    <current-alone>11</current-alone>"
                + "  </current-alone-list>"
                + " </current-alone>"
                + "</leaf-ref>"
                + "</validation>"
                ;
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals("Violate must constraints: ../../current-alone-list[current()]/current-alone = .",
                response.getErrors().get(0).getErrorMessage());
    }
    
    @Test
    public void testCurrentAloneInMissingList() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + "<leaf-ref>"
                + " <current-alone>"
                + "  <current-alone-list-leaf>10</current-alone-list-leaf>"
                + " </current-alone>"
                + "</leaf-ref>"
                + "</validation>"
                ;
        editConfig(m_server, m_clientInfo, requestXml1, false);
    }
    
    @Test
    public void testSymbolLeaf() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + " <validation>validation</validation>"
                + " <symbol-leaf-1.1_2>validation</symbol-leaf-1.1_2>"
                + "</validation>"
                ;
        editConfig(m_server, m_clientInfo, requestXml1, true);

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
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
                "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + " <validation>default-when</validation>" 
                + "</validation>"
                ;
        editConfig(m_server, m_clientInfo, requestXml1, true);
        String response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                + "   <validation:default-when-validation>"
                + "    <validation:class>10</validation:class>"
                + "   </validation:default-when-validation>"
                + "  <validation:validation>default-when</validation:validation>"
                + " </validation:validation>"
                + "</data>"
                + "</rpc-reply>"
                ;
        verifyGet(response);
        
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf2:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <validation xc:operation='delete'>default-when</validation>" 
                + "</validation>"
                ;
        editConfig(m_server, m_clientInfo, requestXml1, true);
        
        response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
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
                "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + " <validation>mandatory</validation>" 
                + "</validation>"
                ;
        editConfig(m_server, m_clientInfo, requestXml1, true);

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + " <mandatory-validation-container/>" 
                + "</validation>"
                ;
        NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals("Missing mandatory node - leaf1", ncResponse.getErrors().get(0).getErrorMessage());
        assertEquals("/validation:validation/validation:mandatory-validation-container/validation:leafValidation/validation:leaf1",
                ncResponse.getErrors().get(0).getErrorPath());
        assertEquals(NetconfRpcErrorTag.DATA_MISSING, ncResponse.getErrors().get(0).getErrorTag());
        
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
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
                + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                + "   <validation:mandatory-validation-container>"
                + "    <validation:leafValidation>"
                + "     <validation:leaf1>0</validation:leaf1>"
                + "     <validation:leafDefault>0</validation:leafDefault>"
                + "    </validation:leafValidation>"
                + "   </validation:mandatory-validation-container>"
                + "  <validation:validation>mandatory</validation:validation>"
                + " </validation:validation>"
                + "</data>"
                + "</rpc-reply>"
                ;
        verifyGet(response);

    }
    
    @Test
    public void testForParentContainerDeletionWithAttributes() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
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
                + "</validation>"
                ;
        editConfig(m_server, m_clientInfo, requestXml1, true);
       
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf2:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
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
                + " <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                + "  <validation:validate-parent-container-on-when-deletion>"
                + "   <validation:for-leaf>"
                + "    <validation:leaf1>0</validation:leaf1>"
                + "   </validation:for-leaf>"
                + "   <validation:leaf1>0</validation:leaf1>"
                + "  </validation:validate-parent-container-on-when-deletion>"
                + " </validation:validation>"
                + "</data>"
                + "</rpc-reply>"
               ;                
        verifyGet(response);
    }
    
    @Test
    public void testDeleteOnLeafRef() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + " <leaf-ref>"
                + "  <album>"
                + "   <name>name</name>"
                + "  </album>"
                + "  <current-alone>"
                + "   <current-leaf>name</current-leaf>"
                + "  </current-alone>"
                + " </leaf-ref>"
                + "</validation>"
                ;
        editConfig(m_server, m_clientInfo, requestXml1, true);
       
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf2:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <leaf-ref>"
                + "  <album xc:operation='delete'>"
                + "   <name>name</name>"
                + "  </album>"
                + " </leaf-ref>"
                + "</validation>"
                ;
        NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals("/validation:validation/validation:leaf-ref/validation:current-alone/validation:current-leaf", ncResponse.getErrors().get(0).getErrorPath());
        assertEquals("instance-required", ncResponse.getErrors().get(0).getErrorAppTag());
       
    }
    
    @Test
    public void testTypeValidation() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + "<valueCheck>4294967298</valueCheck>"
                + "</validation>"
                ;
        NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals("/validation:validation/validation:valueCheck", ncResponse.getErrors().get(0).getErrorPath());
        assertEquals("range-out-of-specified-bounds", ncResponse.getErrors().get(0).getErrorAppTag());
        assertEquals("The argument is out of bounds <-128, 127>",ncResponse.getErrors().get(0).getErrorMessage());

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + "<valueCheck1>429</valueCheck1>"
                + "</validation>"
                ;
        ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals("/validation:validation/validation:valueCheck1", ncResponse.getErrors().get(0).getErrorPath());
        assertEquals("range-out-of-specified-bounds", ncResponse.getErrors().get(0).getErrorAppTag());
        assertEquals("The argument is out of bounds <-128, 127>",ncResponse.getErrors().get(0).getErrorMessage());

    }
    
    @Test
	public void testCurrentWithMultipleParentBothCases() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
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
				+ "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
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
	     assertEquals("/validation:validation/validation:leaf-ref/validation:current-multi-parent/validation:album-name-list[validation:name='TestName']/validation:current-album-list-leaf", ncResponse.getErrors().get(0).getErrorPath());
	     assertEquals("instance-required", ncResponse.getErrors().get(0).getErrorAppTag());
	     assertEquals("Dependency violated, '21' must exist", ncResponse.getErrors().get(0).getErrorMessage());
	}
    
    @Test
    public void testMultipleCurrentSameLevelBothCases() throws Exception {
        getModelNode();
        
        //PositiveCase for path "current()/../../name-list[name = current()/../../current-some-leaf]/name-count"
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + " <leaf-ref>"
                + "  <album>"
                + "   <name>Album1</name>"
                + "   <song-count>20</song-count>"
                + "  </album>" 
                + "  <current-multi-parent>" 
                + "    <current-some-leaf>Album1</current-some-leaf>"
                + "    <name-list>"
                +"      <name>Album1</name>"
                +"      <name-count>1</name-count>"
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
                + "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + " <leaf-ref>"
                + "  <album>"
                + "   <name>Album1</name>"
                + "   <song-count>20</song-count>"
                + "  </album>" 
                + "  <current-multi-parent>" 
                + "    <current-some-leaf>Album1</current-some-leaf>"
                + "    <name-list>"
                +"      <name>Album1</name>"
                +"      <name-count>1</name-count>"
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
				"/validation:validation/validation:leaf-ref/validation:current-multi-parent/validation:album-name-list[validation:name='TestName']/validation:two-current-leaf",
				ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("instance-required", ncResponse.getErrors().get(0).getErrorAppTag());
        assertEquals("Dependency violated, '2' must exist", ncResponse.getErrors().get(0).getErrorMessage());
    }
    
    @Test
    public void testAbsPathAtContainerSameAsRootName() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
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
                + "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + " <someList>"
                + "  <key>key1</key>"
                + "  <sumValue>4</sumValue>"
                + " </someList>"
                + "</validation>";
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals("Violate must constraints: sum(/validation/someList/sumValue) < 100 and sum(../someList/sumValue) > 5",response.getErrors().get(0).getErrorMessage());

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + " <someList>"
                + "  <key>key1</key>"
                + "  <sumValue>11</sumValue>"
                + " </someList>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);
        
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + " <someList>"
                + "  <key>key2</key>"
                + "  <sumValue>111</sumValue>"
                + " </someList>"
                + "</validation>";
        response = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals("Violate must constraints: sum(/validation/someList/sumValue) < 100 and sum(../someList/sumValue) > 5",response.getErrors().get(0).getErrorMessage());
        
    }
    



    @Test
    public void testMustAsLeafMandatory_1() throws Exception {
        getModelNode();
        // test violation of must(./) on a leaf
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + " <mustMandatory>"
                + "  <key>key1</key>"
                + " </mustMandatory>"
                + "</validation>";
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals("Violate must constraints: ./mandatoryLeaf",response.getErrors().get(0).getErrorMessage());
    }

	@Test
	public void testMustAsLeafMandatory_2() throws Exception {
		getModelNode();

		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
				+ " <mustMandatory>"
				+ "  <key>key1</key>"
				+ "  <mandatoryLeaf>4</mandatoryLeaf>"
				+ " </mustMandatory>"
				+ "</validation>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		// test violation of must(./) on a container
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
				+ " <mustMandatory>"
				+ "  <key>key1</key>"
				+ "  <mustMandatoryContainer/>"
				+ " </mustMandatory>"
				+ "</validation>";
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals("Violate must constraints: ./mandatoryContainer", response.getErrors().get(0).getErrorMessage());
	}

	@Test
	public void testMustAsLeafMandatory_3() throws Exception {
		getModelNode();

		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
				+ " <mustMandatory>"
				+ "  <key>key1</key>"
				+ "  <mandatoryLeaf>4</mandatoryLeaf>"
				+ " </mustMandatory>"
				+ "</validation>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
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
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, true);

		// test violation of must(./) on a leafList
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
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
	}

	@Test
	public void testMustAsLeafMandatory_4() throws Exception {
		getModelNode();

		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
				+ " <mustMandatory>"
				+ "  <key>key1</key>"
				+ "  <mandatoryLeaf>4</mandatoryLeaf>"
				+ " </mustMandatory>"
				+ "</validation>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
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
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, true);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
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
	}

	@Test
	public void testMustAsLeafMandatory_5() throws Exception {
		getModelNode();

		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
				+ " <mustMandatory>"
				+ "  <key>key1</key>"
				+ "  <mandatoryLeaf>4</mandatoryLeaf>"
				+ " </mustMandatory>"
				+ "</validation>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
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
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, true);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
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
				+ "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
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
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
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
						+ "</rpc-reply>"
				;
		verifyGet(ncResponse);
	}
    
    @Test
    public void testBooleanCurrent() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + " <booleanCurrent>"
                + "  <key>key1</key>"
                + "  <leaf1>leaf1</leaf1>"
                + "  <leaf2>true</leaf2>"
                + " </booleanCurrent>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);
        
    }
    
    @Test
    public void testOtherTreeRoot() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + " <validation>123</validation>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);
        
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation3 xmlns=\"urn:org:bbf2:pma:validation\">"
                + " <must-validation-with-mandatory>"
                + "    <value>test</value>" 
                + "    <mandatory-leaf>yes</mandatory-leaf>"
                + " </must-validation-with-mandatory>"
                + " <must-validation1>"
                + "    <key1>key</key1>" 
                + "    <value>test</value>" 
                + "    <mandatory-one>yes</mandatory-one>" 
                + " </must-validation1>"           
                + " <otherTreeRelativePath>123</otherTreeRelativePath>"
                + "</validation3>";
        editConfig(m_server, m_clientInfo, requestXml1, true);
        
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf2:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <validation xc:operation=\"delete\">123</validation>"
                + "</validation>";
        NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals("Dependency violated, '123' must exist", ncResponse.getErrors().get(0).getErrorMessage());
        
    }
    
    @Test
    public void testOtherRootFunction() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + " <validation>Other</validation>"
                + " <booleanCurrent>"
                + "  <key>someKey</key>"
                + " </booleanCurrent>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);
    
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation3 xmlns=\"urn:org:bbf2:pma:validation\">"
                + " <must-validation-with-mandatory>"
                + "    <value>test</value>" 
                + "    <mandatory-leaf>yes</mandatory-leaf>"
                + " </must-validation-with-mandatory>"
                + " <must-validation1>"
                + "    <key1>key</key1>" 
                + "    <value>test</value>" 
                + "    <mandatory-one>yes</mandatory-one>" 
                + " </must-validation1>"                   
                + " <someLeaf>someLeaf</someLeaf>"
                + "</validation3>";
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals("Violate must constraints: count(../../validation/booleanCurrent) > 0 "
                + "and not(contains(../../validation/validation, 'Other'))",
                response.getErrors().get(0).getErrorMessage());
    
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + " <validation>other</validation>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);
    
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation3 xmlns=\"urn:org:bbf2:pma:validation\">"
                + " <must-validation-with-mandatory>"
                + "    <value>test</value>" 
                + "    <mandatory-leaf>yes</mandatory-leaf>"
                + " </must-validation-with-mandatory>"
                + " <must-validation1>"
                + "    <key1>key</key1>" 
                + "    <value>test</value>" 
                + "    <mandatory-one>yes</mandatory-one>" 
                + " </must-validation1>"                    
                + " <someLeaf>someLeaf</someLeaf>"
                + "</validation3>";
        editConfig(m_server, m_clientInfo, requestXml1, true);
            
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + " <validation>Other</validation>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, false);
        
    }
    
    @Test
    public void testOtherRootWhen() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + " <validation>otherRoot</validation>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);
        
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation3 xmlns=\"urn:org:bbf2:pma:validation\">"
                + " <must-validation-with-mandatory>"
                + "    <value>test</value>" 
                + "    <mandatory-leaf>yes</mandatory-leaf>"
                + " </must-validation-with-mandatory>"
                + " <must-validation1>"
                + "    <key1>key</key1>" 
                + "    <value>test</value>" 
                + "    <mandatory-one>yes</mandatory-one>" 
                + " </must-validation1>"                
                + " <someLeaf1>otherRoot</someLeaf1>"
                + "</validation3>";
        editConfig(m_server, m_clientInfo, requestXml1, true);
        
        String response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                + "   <validation:validation>otherRoot</validation:validation>"
                + "  </validation:validation>"
                + "  <validation:validation3 xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                + "   <validation:must-validation-with-mandatory>"
                + "    <validation:mandatory-leaf>yes</validation:mandatory-leaf>"
                + "    <validation:value>test</validation:value>"
                + "   </validation:must-validation-with-mandatory>"
                + "   <validation:must-validation1>" 
                + "    <validation:key1>key</validation:key1>"
                + "    <validation:mandatory-one>yes</validation:mandatory-one>"
                + "    <validation:value>test</validation:value>" 
                + "   </validation:must-validation1>"                
                + "   <validation:someLeaf1>otherRoot</validation:someLeaf1>"
                + "  </validation:validation3>"
                + " </data>"
                + "</rpc-reply>"
                ;
        verifyGet(response);
        
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + " <validation>otherRoot1</validation>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);

        response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                + "   <validation:validation>otherRoot1</validation:validation>"
                + "  </validation:validation>"
                + "  <validation:validation3 xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                + "<validation:must-validation-with-mandatory>"
                + "<validation:mandatory-leaf>yes</validation:mandatory-leaf>"
                + "<validation:value>test</validation:value>"
                + "</validation:must-validation-with-mandatory>"
                + "<validation:must-validation1>" 
                + "<validation:key1>key</validation:key1>"
                + "<validation:mandatory-one>yes</validation:mandatory-one>"
                + "<validation:value>test</validation:value>" 
                + "</validation:must-validation1>"
                + "  </validation:validation3>"                
                + " </data>"
                + "</rpc-reply>"
                ;
        verifyGet(response);
        
    }
    
    @Test
    public void testBooleanStringConversion() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + "  <boolean-function-validation>"
                + "    <boolean-string-conversion-leaf>test</boolean-string-conversion-leaf>"
                + "  </boolean-function-validation>"
                + "</validation>";
        // should fail
        editConfig(m_server, m_clientInfo, requestXml1, false);     
        
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + "  <boolean-function-validation>"
                + "    <string1>nonempty</string1>"
                + "    <boolean-string-conversion-leaf>test</boolean-string-conversion-leaf>"
                + "  </boolean-function-validation>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);      
        
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + "  <boolean-function-validation>"
                + "    <string1>nonempty</string1>"
                + "  </boolean-function-validation>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);      
    }
    
    @Test
    public void testBooleanStringConversionInAnd() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + "  <boolean-function-validation>"
                + "    <string1></string1>"
                + "    <boolean-string-conversion-in-and-leaf>test</boolean-string-conversion-in-and-leaf>"
                + "  </boolean-function-validation>"
                + "</validation>";
        // should fail
        editConfig(m_server, m_clientInfo, requestXml1, false);     
        
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + "  <boolean-function-validation>"
                + "    <string1>nonempty</string1>"
                + "    <boolean-string-conversion-in-and-leaf>test</boolean-string-conversion-in-and-leaf>"
                + "  </boolean-function-validation>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);      
        
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + "  <boolean-function-validation>"
                + "    <string1>nonempty</string1>"
                + "  </boolean-function-validation>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);      
    }
    
    @Test
    public void testBooleanStringConversionInOr() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + "  <boolean-function-validation>"
                + "    <string1></string1>"
                + "    <boolean-string-conversion-in-or-leaf>test</boolean-string-conversion-in-or-leaf>"
                + "  </boolean-function-validation>"
                + "</validation>";
        // should fail
        editConfig(m_server, m_clientInfo, requestXml1, false);     
        
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + "  <boolean-function-validation>"
                + "    <string1>nonempty</string1>"
                + "    <boolean-string-conversion-in-or-leaf>test</boolean-string-conversion-in-or-leaf>"
                + "  </boolean-function-validation>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);      
        
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + "  <boolean-function-validation>"
                + "    <string1>nonempty</string1>"
                + "  </boolean-function-validation>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);      
    }
    
    @Test
    public void testBooleanNumberConversion() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + "  <boolean-function-validation>"
                + "    <number1>0</number1>"
                + "    <boolean-number-conversion-leaf>test</boolean-number-conversion-leaf>"
                + "  </boolean-function-validation>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);     
        
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + "  <boolean-function-validation>"
                + "    <number1>20</number1>"
                + "    <boolean-number-conversion-leaf>test</boolean-number-conversion-leaf>"
                + "  </boolean-function-validation>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);      
        
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + "  <boolean-function-validation>"
                + "    <number1>20</number1>"
                + "  </boolean-function-validation>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);      
    }
    
    @Test
    public void testBooleanNumberConversionInAnd() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + "  <boolean-function-validation>"
                + "    <number1>0</number1>"
                + "    <boolean-number-conversion-in-and-leaf>test</boolean-number-conversion-in-and-leaf>"
                + "  </boolean-function-validation>"
                + "</validation>";
        // should fail
        editConfig(m_server, m_clientInfo, requestXml1, false);     
        
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + "  <boolean-function-validation>"
                + "    <number1>20</number1>"
                + "    <boolean-number-conversion-in-and-leaf>test</boolean-number-conversion-in-and-leaf>"
                + "  </boolean-function-validation>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);      
        
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + "  <boolean-function-validation>"
                + "    <number1>20</number1>"
                + "  </boolean-function-validation>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);      
    }
    
    @Test
    public void testBooleanNumberConversionInOr() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + "  <boolean-function-validation>"
                + "    <number1>0</number1>"
                + "    <boolean-number-conversion-in-or-leaf>test</boolean-number-conversion-in-or-leaf>"
                + "  </boolean-function-validation>"
                + "</validation>";
        // should fail
        editConfig(m_server, m_clientInfo, requestXml1, false);     
        
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + "  <boolean-function-validation>"
                + "    <number1>20</number1>"
                + "    <boolean-number-conversion-in-or-leaf>test</boolean-number-conversion-in-or-leaf>"
                + "  </boolean-function-validation>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);      
        
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + "  <boolean-function-validation>"
                + "    <number1>20</number1>"
                + "  </boolean-function-validation>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);      
    }
    
    @Test
    public void testBooleanNodesetConversion() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + "  <boolean-function-validation>"
                + "    <boolean-nodeset-conversion-leaf>test</boolean-nodeset-conversion-leaf>"
                + "  </boolean-function-validation>"
                + "</validation>";
        // should fail
        editConfig(m_server, m_clientInfo, requestXml1, false);     
        
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + "  <boolean-function-validation>"
                + "    <nodeset1>some-value</nodeset1>"
                + "    <boolean-nodeset-conversion-leaf>test</boolean-nodeset-conversion-leaf>"
                + "  </boolean-function-validation>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);      
        
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + "  <boolean-function-validation>"
                + "    <nodeset1>some-value</nodeset1>"
                + "  </boolean-function-validation>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);      
    }

    @Test
    public void testBooleanNodesetConversionInAnd() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + "  <boolean-function-validation>"
                + "    <boolean-nodeset-conversion-in-and-leaf>test</boolean-nodeset-conversion-in-and-leaf>"
                + "  </boolean-function-validation>"
                + "</validation>";
        // should fail
        editConfig(m_server, m_clientInfo, requestXml1, false);     
        
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + "  <boolean-function-validation>"
                + "    <nodeset1>some-value</nodeset1>"
                + "    <boolean-nodeset-conversion-in-and-leaf>test</boolean-nodeset-conversion-in-and-leaf>"
                + "  </boolean-function-validation>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);      
        
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + "  <boolean-function-validation>"
                + "    <nodeset1>some-value</nodeset1>"
                + "  </boolean-function-validation>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);      
    }
    
    @Test
    public void testBooleanNodesetConversionInOr() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + "  <boolean-function-validation>"
                + "    <boolean-nodeset-conversion-in-or-leaf>test</boolean-nodeset-conversion-in-or-leaf>"
                + "  </boolean-function-validation>"
                + "</validation>";
        // should fail
        editConfig(m_server, m_clientInfo, requestXml1, false);     
        
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + "  <boolean-function-validation>"
                + "    <nodeset1>some-value</nodeset1>"
                + "    <boolean-nodeset-conversion-in-or-leaf>test</boolean-nodeset-conversion-in-or-leaf>"
                + "  </boolean-function-validation>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);      
        
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + "  <boolean-function-validation>"
                + "    <nodeset1>some-value</nodeset1>"
                + "  </boolean-function-validation>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);      
    }
    

    
    @Test
    public void testMustValidationDuringDeleteOfReferencedNode() throws Exception {
    	m_schemaRegistry.registerAppAllowedAugmentedPath("datastore-validator-augment-test", "/validation:validation/validation:test-interfaces", mock(SchemaPath.class));
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + "  <test-interfaces>"
                + "    <test-interface>"
                + "     <name>intName</name>"
                + "     <type>sampleType1</type>"
                + "    </test-interface>"
                + "  </test-interfaces>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);  
        
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + "  <test-interfaces>"
                + "    <test-interface>"
                + "     <name>intName</name>"
                + "     <type>sampleType1</type>"
                + "    </test-interface>"
                + "     <use-test-interface xmlns=\"urn:opendaylight:datastore-validator-augment-test\">"
                + "      <channel-leaf>sampleChannel</channel-leaf>"
                + "      <sample-leaf>intName</sample-leaf>"
                + "     </use-test-interface>"
                + "  </test-interfaces>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true); 
        
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf2:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + "  <test-interfaces>"
                + "    <test-interface xc:operation=\"remove\">"
                + "     <name>intName</name>"
                + "     <type>sampleType1</type>"
                + "    </test-interface>"
                + "  </test-interfaces>"
                + "</validation>";
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals("Dependency violated, 'intName' must exist",response.getErrors().get(0).getErrorMessage());
    }

	@Test
	public void testImpactPathsForSameLeafDifferentNamespace() throws Exception {
		getModelNode();

		SchemaPath securityLevelSchemaPath = SchemaPathBuilder.fromString(
				"(urn:org:bbf2:pma:validation-yang11?revision=2015-12-14)validation-yang11,",
				"(urn:org:bbf2:pma:validation-yang11?revision=2015-12-14)tm-root,",
				"(urn:org:bbf2:pma:validation-yang11?revision=2015-12-14)children-type,",
				"(urn:org:bbf2:pma:validation-yang11?revision=2015-12-14)scheduler-node,",
				"(urn:org:bbf2:pma:validation-yang11?revision=2015-12-14)scheduler-node,",
				"(urn:org:bbf2:pma:validation-yang11?revision=2015-12-14)scheduling-level");


		SchemaPath schedulerNodeNameSchemaPath = SchemaPathBuilder.fromString(
				"(urn:org:bbf2:pma:validation-yang11?revision=2015-12-14)validation-yang11,",
				"(urn:org:bbf2:pma:validation-yang11?revision=2015-12-14)tm-root,",
				"(urn:org:bbf2:pma:validation-yang11?revision=2015-12-14)children-type,",
				"(urn:org:bbf2:pma:validation-yang11?revision=2015-12-14)scheduler-node,",
				"(urn:org:bbf2:pma:validation-yang11?revision=2015-12-14)scheduler-node,",
				"(urn:org:bbf2:pma:validation-yang11?revision=2015-12-14)children-type,",
				"(urn:org:bbf2:pma:validation-yang11?revision=2015-12-14)scheduler-node,",
				"(urn:org:bbf2:pma:validation-yang11?revision=2015-12-14)child-scheduler-nodes,",
				"(urn:org:bbf2:pma:validation-yang11?revision=2015-12-14)name");

		SchemaPath schedulerNodeNameSchemaPathForAugment = SchemaPathBuilder.fromString(
				"(urn:org:bbf2:pma:validation-yang11?revision=2015-12-14)validation-yang11,",
				"(urn:org:bbf2:pma:validation-yang11?revision=2015-12-14)tm-root,",
				"(urn:org:bbf2:pma:validation-yang11?revision=2015-12-14)children-type,",
				"(urn:opendaylight:datastore-validator-augment-test11?revision=2018-03-07)scheduler-node,",
				"(urn:opendaylight:datastore-validator-augment-test11?revision=2018-03-07)scheduler-node,",
				"(urn:opendaylight:datastore-validator-augment-test11?revision=2018-03-07)children-type,",
				"(urn:opendaylight:datastore-validator-augment-test11?revision=2018-03-07)scheduler-node,",
				"(urn:opendaylight:datastore-validator-augment-test11?revision=2018-03-07)child-scheduler-nodes,",
				"(urn:opendaylight:datastore-validator-augment-test11?revision=2018-03-07)name");

		ReferringNodes referringNodes = m_schemaRegistry.getReferringNodesForSchemaPath(securityLevelSchemaPath);
		assertTrue(referringNodes.getReferringNodes().containsKey(schedulerNodeNameSchemaPath));
		assertEquals(1,referringNodes.getReferringNodes().size());
		assertFalse(referringNodes.getReferringNodes().containsKey(schedulerNodeNameSchemaPathForAugment));
	}

	@Test
	public void testContainerWithSameLocalNameDifferentNameSpace() throws Exception {
		getModelNode();

		String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">"
				+ "  <tm-root>"
				+ "    <scheduler-node  xmlns=\"urn:org:bbf2:pma:validation-yang11\">"
				+ "     	<name>child</name>"
				+ "     	<scheduling-level>3</scheduling-level>"
				+ "    </scheduler-node>"
				+ "    <scheduler-node  xmlns=\"urn:org:bbf2:pma:validation-yang11\">"
				+ "     <name>parent</name>"
				+ "     <scheduling-level>2</scheduling-level>"
				+ "			<child-scheduler-nodes>"
				+ " 	        <name>child</name>"
				+ "			</child-scheduler-nodes>"
				+ "    </scheduler-node>"
				+ "  </tm-root>"
				+ "</validation-yang11>";
		editConfig(m_server, m_clientInfo, requestXml, true);

		String ncResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
				+ "<data>"
				+ "		<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
				+ "		<validation11:validation-yang11 xmlns:validation11=\"urn:org:bbf2:pma:validation-yang11\">"
				+ "			<validation11:tm-root>"
				+ "				<validation11:scheduler-node>"
				+ "					<validation11:name>child</validation11:name>"
				+ "					<validation11:scheduling-level>3</validation11:scheduling-level>"
				+ "				</validation11:scheduler-node>"
				+ "				<validation11:scheduler-node>"
				+ "					<validation11:name>parent</validation11:name>"
				+ "					<validation11:scheduling-level>2</validation11:scheduling-level>"
				+ "					<validation11:child-scheduler-nodes>"
				+ "						<validation11:name>child</validation11:name>"
				+ "					</validation11:child-scheduler-nodes>"
				+ "				</validation11:scheduler-node>"
				+ "			</validation11:tm-root>"
				+ "                     <validation11:container-with-must>"
				+ "                         <validation11:remove-mac-addresses-from-port-down>false</validation11:remove-mac-addresses-from-port-down>"
				+ "                     </validation11:container-with-must>"
				+ "		</validation11:validation-yang11>"
				+ "	</data>"
				+ "</rpc-reply>";
		verifyGet(ncResponse);
	}

	@Test
	public void testContainerWithSameLocalNameDifferentNameSpaceAugmentYang() throws Exception {
		getModelNode();

		String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">"
				+ "  <tm-root>"
				+ "    <scheduler-node  xmlns=\"urn:opendaylight:datastore-validator-augment-test11\">"
				+ "     	<name>child</name>"
				+ "     	<scheduling-level>3</scheduling-level>"
				+ "    </scheduler-node>"
				+ "    <scheduler-node  xmlns=\"urn:opendaylight:datastore-validator-augment-test11\">"
				+ "     <name>parent</name>"
				+ "     <scheduling-level>2</scheduling-level>"
				+ "			<child-scheduler-nodes>"
				+ " 	        <name>child</name>"
				+ "			</child-scheduler-nodes>"
				+ "    </scheduler-node>"
				+ "  </tm-root>"
				+ "</validation-yang11>";
		editConfig(m_server, m_clientInfo, requestXml, true);

		String ncResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
				+ "<data>"
				+ "		<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
				+ "		<validation11:validation-yang11 xmlns:validation11=\"urn:org:bbf2:pma:validation-yang11\">"
				+ "			<validation11:tm-root>"
				+ "				<validation-augment11:scheduler-node xmlns:validation-augment11=\"urn:opendaylight:datastore-validator-augment-test11\">"
				+ "					<validation-augment11:name>child</validation-augment11:name>"
				+ "					<validation-augment11:scheduling-level>3</validation-augment11:scheduling-level>"
				+ "				</validation-augment11:scheduler-node>"
				+ "				<validation-augment11:scheduler-node xmlns:validation-augment11=\"urn:opendaylight:datastore-validator-augment-test11\">"
				+ "					<validation-augment11:name>parent</validation-augment11:name>"
				+ "					<validation-augment11:scheduling-level>2</validation-augment11:scheduling-level>"
				+ "					<validation-augment11:child-scheduler-nodes>"
				+ "						<validation-augment11:name>child</validation-augment11:name>"
				+ "					</validation-augment11:child-scheduler-nodes>"
				+ "				</validation-augment11:scheduler-node>"
				+ "			</validation11:tm-root>"
				+ "                 <validation11:container-with-must>"
				+ "                     <validation11:remove-mac-addresses-from-port-down>false</validation11:remove-mac-addresses-from-port-down>"
				+ "                 </validation11:container-with-must>"
				+ "		</validation11:validation-yang11>"
				+ "	</data>"
				+ "</rpc-reply>";
		verifyGet(ncResponse);
	}

	@Test
	public void testImpactPathsForNotGettingOverridden() throws Exception {
//		m_schemaRegistry.registerAppAllowedAugmentedPath("datastore-validator-augment-test", "/validation:validation/validation:test-interfaces", mock(SchemaPath.class));
		getModelNode();
		String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">"
				+ "  <hardware>"
				+ "    <component>"
				+ "     <name>parent_name</name>"
				+ "     <class>transceiver</class>"
				+ "    </component>"
				+ "  </hardware>"
				+ "</validation-yang11>";
		editConfig(m_server, m_clientInfo, requestXml, true);

		String expectedOutput = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" +
				"<data>" +
				"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>" +
				"<validation11:validation-yang11 xmlns:validation11=\"urn:org:bbf2:pma:validation-yang11\">" +
				"<validation11:hardware>" +
				"<validation11:component>" +
				"<validation11:class>transceiver</validation11:class>" +
				"<validation11:name>parent_name</validation11:name>" +
				"</validation11:component>" +
				"</validation11:hardware>" +
				"<validation11:container-with-must>"+
				"<validation11:remove-mac-addresses-from-port-down>false</validation11:remove-mac-addresses-from-port-down>"+
				"</validation11:container-with-must>"+
				"</validation11:validation-yang11>" +
				"</data>" +
				"</rpc-reply>";

		verifyGet(expectedOutput);

		requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">"
				+ "  <hardware>"
				+ "    <component>"
				+ "     <name>child_name</name>"
				+ "     <class>transceiver-link</class>"
				+ "     <parent>parent_name</parent>"
				+ "    </component>"
				+ "  </hardware>"
				+ "</validation-yang11>";
		editConfig(m_server, m_clientInfo, requestXml, true);

		expectedOutput = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" +
				"<data>" +
				"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>" +
				"<validation11:validation-yang11 xmlns:validation11=\"urn:org:bbf2:pma:validation-yang11\">" +
				"<validation11:hardware>" +
				"<validation11:component>" +
				"<validation11:class>transceiver</validation11:class>" +
				"<validation11:name>parent_name</validation11:name>" +
				"</validation11:component>" +
				"<validation11:component>" +
				"<validation11:class>transceiver-link</validation11:class>" +
				"<validation11:name>child_name</validation11:name>" +
				"<validation11:parent>parent_name</validation11:parent>" +
				"</validation11:component>" +
				"</validation11:hardware>" +
				"<validation11:container-with-must>"+
				"<validation11:remove-mac-addresses-from-port-down>false</validation11:remove-mac-addresses-from-port-down>"+
				"</validation11:container-with-must>"+
				"</validation11:validation-yang11>" +
				"</data>" +
				"</rpc-reply>";

		verifyGet(expectedOutput);

		requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\"  xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
				+ "  <hardware>"
				+ "    <component>"
				+ "     <name>parent_name1</name>"
				+ "     <class>transceiver</class>"
				+ "    </component>"
				+ "    <component>"
				+ "     <name>child_name1</name>"
				+ "     <class>transceiver-link</class>"
				+ "     <parent>parent_name1</parent>"
				+ "    </component>"
				+ "    <component xc:operation=\"remove\">"
				+ "     <name>parent_name</name>"
				+ "     <class>transceiver</class>"
				+ "    </component>"
				+ "  </hardware>"
				+ "</validation-yang11>";
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);

		assertFalse(response.isOk());
		NetconfRpcError netconfRpcError = response.getErrors().get(0);
		assertNetconfRpcError(NetconfRpcErrorTag.OPERATION_FAILED,
				NetconfRpcErrorType.Application,
				NetconfRpcErrorSeverity.Error,
				"must-violation",
				"A transceiver-link port component is only supported when contained in a transceiver component by this system.",
				"/validation11:validation-yang11/validation11:hardware/validation11:component[validation11:name='child_name']/validation11:class",
				netconfRpcError);

		assertEquals("A transceiver-link port component is only supported when contained in a transceiver component by this system.",response.getErrors().get(0).getErrorMessage());
	}

	@Test
	public void testImpactPathsForNotGettingOverriddenParentRelPositionBoard() throws Exception {
		testImpactPathsForNotGettingOverriddenParentRelPosition("board");
	}

	@Test
	public void testImpactPathsForNotGettingOverriddenParentRelPositionChassis() throws Exception {
		testImpactPathsForNotGettingOverriddenParentRelPosition("chassis");
	}

	private void testImpactPathsForNotGettingOverriddenParentRelPosition(String className) throws Exception {
//		m_schemaRegistry.registerAppAllowedAugmentedPath("datastore-validator-augment-test", "/validation:validation/validation:test-interfaces", mock(SchemaPath.class));
		getModelNode();
		String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">"
				+ "  <hardware>"
				+ "    <component>"
				+ "     <name>test</name>"
				+ "     <class>"+className+"</class>"
				+ "     <parent-rel-pos>1</parent-rel-pos>"
				+ "    </component>"
				+ "  </hardware>"
				+ "</validation-yang11>";
		editConfig(m_server, m_clientInfo, requestXml, true);

		String expectedOutput = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" +
				"<data>" +
				"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>" +
				"<validation11:validation-yang11 xmlns:validation11=\"urn:org:bbf2:pma:validation-yang11\">" +
				"<validation11:container-with-must>"+
				"<validation11:remove-mac-addresses-from-port-down>false</validation11:remove-mac-addresses-from-port-down>"+
				"</validation11:container-with-must>"+
				"<validation11:hardware>" +
				"<validation11:component>" +
				"<validation11:class>"+className+"</validation11:class>" +
				"<validation11:name>test</validation11:name>" +
				"<validation11:parent-rel-pos>1</validation11:parent-rel-pos>" +
				"</validation11:component>" +
				"</validation11:hardware>" +
				"</validation11:validation-yang11>" +
				"</data>" +
				"</rpc-reply>";

		verifyGet(expectedOutput);

		requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">"
				+ "  <hardware>"
				+ "    <component>"
				+ "     <name>test1</name>"
				+ "     <class>"+className+"</class>"
				+ "    </component>"
				+ "  </hardware>"
				+ "</validation-yang11>";

		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
		NetconfRpcError netconfRpcError = response.getErrors().get(0);
		assertNetconfRpcError(NetconfRpcErrorTag.OPERATION_FAILED,
				NetconfRpcErrorType.Application,
				NetconfRpcErrorSeverity.Error,
				"must-violation",
				"The parent relative position of a "+className+" component must be 1 for this system.",
				"/validation11:validation-yang11/validation11:hardware/validation11:component[validation11:name='test1']/validation11:class",
				netconfRpcError);

	}

	@Test
    public void testMustNotValidation() throws Exception {
    	getModelNode();
    	String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
    			"<validation3 xmlns=\"urn:org:bbf2:pma:validation\">"+
    			"  <must-validation-with-mandatory>"+
    			"    <value>test</value>" +
    			"    <mandatory-leaf>yes</mandatory-leaf>"+
    			"  </must-validation-with-mandatory>"+
    			"  <must-validation1>"+
    			"    <key1>key</key1>" +
    			"    <value>test</value>" +
    			"    <mandatory-one>yes</mandatory-one>" +
    			"  </must-validation1>"+
    			"  <list1>"+
    			"    <key1>key1</key1>"+
    			"    <container2>"+
    			"    <address>"+
    			"    <ip>1.1.1.1</ip>"+
    			"    </address>"+
    			"    </container2>"+
    			"  </list1>"+
    			"</validation3>"
    			;
    	editConfig(m_server, m_clientInfo, requestXml1, true);     

    	String expectedOutput = 
    			"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" +
    					"<data>" +
    					"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>" +
    					"<validation:validation3 xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
    					"<validation:list1>" +
    					"<validation:container2>" +
    					"<validation:address>" +
    					"<validation:ip>1.1.1.1</validation:ip>" +
    					"</validation:address>" +
    					"</validation:container2>" +
    					"<validation:key1>key1</validation:key1>" +
    					"</validation:list1>" +
    					"<validation:must-validation-with-mandatory>" +
    					"<validation:mandatory-leaf>yes</validation:mandatory-leaf>" +
    					"<validation:value>test</validation:value>" +
    					"</validation:must-validation-with-mandatory>"+
    					"<validation:must-validation1>" +
    					"<validation:key1>key</validation:key1>" +
    					"<validation:mandatory-one>yes</validation:mandatory-one>" +
    					"<validation:value>test</validation:value>" +
    					"</validation:must-validation1>"  +
    					"</validation:validation3>" +
    					"</data>" +
    					"</rpc-reply>";

    	verifyGet(expectedOutput);

    	requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
    			"<validation3 xmlns=\"urn:org:bbf2:pma:validation\">"+
    			"  <must-validation-with-mandatory>"+
    			"    <value>test</value>" +
    			"    <mandatory-leaf>yes</mandatory-leaf>"+
    			"  </must-validation-with-mandatory>"+
    			"  <must-validation1>"+
    			"    <key1>key</key1>" +
    			"    <value>test</value>" +
    			"    <mandatory-one>yes</mandatory-one>" +
    			"  </must-validation1>"+
    			"  <list1>"+
    			"    <key1>key1</key1>"+
    			"    <container2>"+
    			"    <address>"+
    			"    <ip>1.1.1.1</ip>"+
    			"    </address>"+
    			"    <address>"+
    			"    <ip>1.1.1.2</ip>"+
    			"    </address>"+
    			"    </container2>"+
    			"  </list1>"+
    			"</validation3>"
    			;
    	editConfig(m_server, m_clientInfo, requestXml1, true);  

    	expectedOutput = 
    			"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" +
    					"<data>" +
    					"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>" +
    					"<validation:validation3 xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
    					"<validation:list1>" +
    					"<validation:container2>" +
    					"<validation:address>" +
    					"<validation:ip>1.1.1.1</validation:ip>" +
    					"</validation:address>" +
    					"<validation:address>" +
    					"<validation:ip>1.1.1.2</validation:ip>" +
    					"</validation:address>" +
    					"</validation:container2>" +
    					"<validation:key1>key1</validation:key1>" +
    					"</validation:list1>" +
    					"<validation:must-validation-with-mandatory>" +
    					"<validation:mandatory-leaf>yes</validation:mandatory-leaf>" +
    					"<validation:value>test</validation:value>" +
    					"</validation:must-validation-with-mandatory>" +
    					"<validation:must-validation1>" +
    					"<validation:key1>key</validation:key1>" +
    					"<validation:mandatory-one>yes</validation:mandatory-one>" +
    					"<validation:value>test</validation:value>" +
    					"</validation:must-validation1>" +
    					"</validation:validation3>" +
    					"</data>" +
    					"</rpc-reply>";

    	verifyGet(expectedOutput);

    	requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
    			"<validation3 xmlns=\"urn:org:bbf2:pma:validation\">"+
    			"  <must-validation-with-mandatory>"+
    			"    <value>test</value>" +
    			"    <mandatory-leaf>yes</mandatory-leaf>"+
    			"  </must-validation-with-mandatory>"+
    			"  <must-validation1>"+
    			"    <key1>key</key1>" +
    			"    <value>test</value>" +
    			"    <mandatory-one>yes</mandatory-one>" +
    			"  </must-validation1>" +
    			"  <list1>"+
    			"    <key1>key1</key1>"+
    			"    <container2>"+
    			"    <address>"+
    			"    <ip>1.1.1.1</ip>"+
    			"    </address>"+
    			"    </container2>"+
    			"  </list1>"+
    			"</validation3>"
    			;
    	editConfig(m_server, m_clientInfo, requestXml1, true); 

    	expectedOutput = 
    			"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" +
    					"<data>" +
    					"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>" +
    					"<validation:validation3 xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
    					"<validation:list1>" +
    					"<validation:container2>" +
    					"<validation:address>" +
    					"<validation:ip>1.1.1.1</validation:ip>" +
    					"</validation:address>" +
    					"<validation:address>" +
    					"<validation:ip>1.1.1.2</validation:ip>" +
    					"</validation:address>" +
    					"</validation:container2>" +
    					"<validation:key1>key1</validation:key1>" +
    					"</validation:list1>" +
    					"<validation:must-validation-with-mandatory>" +
    					"<validation:mandatory-leaf>yes</validation:mandatory-leaf>" +
    					"<validation:value>test</validation:value>" +
    					"</validation:must-validation-with-mandatory>" +
    					"<validation:must-validation1>" +
    					"<validation:key1>key</validation:key1>" +
    					"<validation:mandatory-one>yes</validation:mandatory-one>" +
    					"<validation:value>test</validation:value>" +
    					"</validation:must-validation1>" + 
    					"</validation:validation3>" +
    					"</data>" +
    					"</rpc-reply>";

    	verifyGet(expectedOutput);

    	requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
    			"<validation3 xmlns=\"urn:org:bbf2:pma:validation\">"+
    			"  <must-validation-with-mandatory>"+
    			"    <value>test</value>" +
    			"    <mandatory-leaf>yes</mandatory-leaf>"+
    			"  </must-validation-with-mandatory>"+
    			"  <must-validation1>"+
    			"    <key1>key</key1>" +
    			"    <value>test</value>" +
    			"    <mandatory-one>yes</mandatory-one>" +
    			"  </must-validation1>"+
    			"  <list1>"+
    			"    <key1>key1</key1>"+
    			"    <container2>"+
    			"    <address>"+
    			"    <ip>1.1.1.1</ip>"+
    			"    </address>"+
    			"    <address>"+
    			"    <ip>1.1.1.1</ip>"+
    			"    </address>"+
    			"    </container2>"+
    			"  </list1>"+
    			"</validation3>"
    			;
    	NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false); 

    	String expectedErrorMsg = EditContainmentNode.DUPLICATE_ELEMENTS_FOUND + "(urn:org:bbf2:pma:validation?revision=2015-12-14)address";
    	assertEquals(expectedErrorMsg, response.getErrors().get(0).getErrorMessage());
        assertEquals("/validation3/list1[key1='key1']/container2/address[ip='1.1.1.1']",
    			response.getErrors().get(0).getErrorPath());
    	assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, response.getErrors().get(0).getErrorTag());
    } 

    @Test
    public void testNotOnCountForList() throws Exception {
        getModelNode();
        
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">"
                + "  <test-count>" 
                + "    <tag>"
                + "      <id>1</id>" 
                + "    </tag>" 
                + "    <pop-tag-list>"
                + "      <name>testCount</name>" 
                + "      <pop-tags>2</pop-tags>"
                + "    </pop-tag-list>" 
                + "    <match-criteria>" 
                + "      <not-check-leaf>testCount</not-check-leaf>"
                + "    </match-criteria>" 
                + "  </test-count>" 
                + "</validation-yang11>";
        
        editConfig(m_server, m_clientInfo, requestXml1, true);
        
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">"
                + "  <test-count>" 
                + "    <tag>"
                + "      <id>1</id>" 
                + "    </tag>" 
                + "    <tag>" 
                + "      <id>2</id>" 
                + "    </tag>"
                + "    <pop-tag-list>"
                + "      <name>testCount</name>" 
                + "      <pop-tags>2</pop-tags>"
                + "    </pop-tag-list>" 
                + "    <match-criteria>" 
                + "      <not-check-leaf>testCount</not-check-leaf>"
                + "    </match-criteria>" 
                + "  </test-count>" 
                + "</validation-yang11>";
        
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertFalse(response.isOk());
        assertEquals("must-violation", response.getErrors().get(0).getErrorAppTag());
        assertEquals("/validation11:validation-yang11/validation11:test-count/validation11:match-criteria/validation11:not-check-leaf",
                response.getErrors().get(0).getErrorPath());
        assertEquals("Violate must constraints: not(current()/../../pop-tag-list[name=current()]/pop-tags <= count(current()/../../tag))",
                response.getErrors().get(0).getErrorMessage());       
        
    }

    @Test
    public void testCountOnList() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">"
                + "  <test-count>" 
                + "	   <tag>"
                + "      <id>1</id>" 
                + "	   </tag>" 
                + "	   <tag>" 
                + "      <id>2</id>" 
                + "	   </tag>" 
                + "	   <pop-tag-list>"
                + "	     <name>testCount</name>" 
                + "      <pop-tags>2</pop-tags>" 
                + "	   </pop-tag-list>"
                + "	   <match-criteria>" 
                + "	     <count-check-leaf>testCount</count-check-leaf>" 
                + "	   </match-criteria>"
                + "  </test-count>" 
                + "</validation-yang11>";
        editConfig(m_server, m_clientInfo, requestXml1, true);

        String expectedOutput = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                                +"<data>"
                                +"  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                                +"  <validation11:validation-yang11 xmlns:validation11=\"urn:org:bbf2:pma:validation-yang11\">"
                                + "   <validation11:container-with-must>"
                                + "     <validation11:remove-mac-addresses-from-port-down>false</validation11:remove-mac-addresses-from-port-down>"
                                + "   </validation11:container-with-must>"
                                +"      <validation11:test-count>"
                                +"          <validation11:match-criteria>"
                                +"              <validation11:count-check-leaf>testCount</validation11:count-check-leaf>"
                                +"          </validation11:match-criteria>"
                                +"          <validation11:pop-tag-list>"
                                +"              <validation11:name>testCount</validation11:name>"
                                +"              <validation11:pop-tags>2</validation11:pop-tags>"
                                +"          </validation11:pop-tag-list>"
                                +"          <validation11:tag>"
                                +"              <validation11:id>1</validation11:id>"
                                +"          </validation11:tag>"
                                +"          <validation11:tag>"
                                +"              <validation11:id>2</validation11:id>"
                                +"          </validation11:tag>"
                                +"      </validation11:test-count>"
                                +"  </validation11:validation-yang11>"
                                +"</data>"
                                +"</rpc-reply>";

        verifyGet(expectedOutput);
        
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + "  <test-count>" 
                + "    <tag xc:operation=\"delete\">"
                + "      <id>1</id>" 
                + "    </tag>" 
                + "  </test-count>" 
                + "</validation-yang11>";
        
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertFalse(response.isOk());
        assertEquals("must-violation", response.getErrors().get(0).getErrorAppTag());
        assertEquals("/validation11:validation-yang11/validation11:test-count/validation11:match-criteria/validation11:count-check-leaf",
                response.getErrors().get(0).getErrorPath());
        assertEquals("Violate must constraints: current()/../../pop-tag-list[name=current()]/pop-tags <= count(current()/../../tag)",
                response.getErrors().get(0).getErrorMessage()); 
        
        
    }
	
	
    @Test
    public void testMustOnCurrentExtensionFunction() throws Exception {
    	getModelNode();
    	String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
    			"<validation3 xmlns=\"urn:org:bbf2:pma:validation\">"+
    			"  <must-validation-with-mandatory>"+
    			"    <value>test</value>" +
    			"    <mandatory-leaf>yes</mandatory-leaf>"+
    			"  </must-validation-with-mandatory>"+
    			"  <must-validation1>"+
    			"    <key1>key</key1>" +
    			"    <value>test</value>" +
    			"    <mandatory-one>yes</mandatory-one>" +
    			"  </must-validation1>"+   	        
    			"  <list1>"+
    			"    <key1>key1</key1>"+
    			"    <container2>"+
    			"    <address>"+
    			"    <ip>1.1.1.1</ip>"+
    			"    </address>"+
    			"    <refClass1>test</refClass1>"+
    			"    </container2>"+
    			"  </list1>"+
    			"</validation3>"
    			;
    	editConfig(m_server, m_clientInfo, requestXml1, true); 
    }
    
    @Test
    public void testMustOnCurrentExpressionPath() throws Exception {
    	getModelNode();
    	String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
    			"<validation3 xmlns=\"urn:org:bbf2:pma:validation\">"+
    			"  <must-validation-with-mandatory>"+
    			"    <value>test</value>" +
    			"    <mandatory-leaf>yes</mandatory-leaf>"+
    			"  </must-validation-with-mandatory>"+
    			"  <must-validation1>"+
    			"    <key1>key</key1>" +
    			"    <value>test</value>" +
    			"    <mandatory-one>yes</mandatory-one>" +
    			"  </must-validation1>"+    	        
    			"  <list1>"+
    			"    <key1>key1</key1>"+
    			"    <container2>"+
    			"    <address>"+
    			"    <ip>1.1.1.1</ip>"+
    			"    </address>"+
    			"    <refClass1>test</refClass1>"+
    			"    <refClass2>test</refClass2>"+
    			"    </container2>"+
    			"  </list1>"+
    			"</validation3>"
    			;
    	editConfig(m_server, m_clientInfo, requestXml1, true); 

    }
    
    @Test
    public void testMustCountWithMultipleCurrent_Fail() throws Exception {
    	getModelNode();
    	String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
    			"<validation xmlns=\"urn:org:bbf2:pma:validation\">"+
    			"	<current-validation>"+
    			"  		<list1>"+
    			"    		<key>key1</key>"+
    			"	 		<type>type1</type>"+
    			"	 		<value>10</value>"+
    			"  		</list1>"+
    			"		<leaf1>key2</leaf1>"+
    			"	</current-validation>"+
    			"</validation>";
    	
    	NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
    	String expectedErrorMsg = "Violate must constraints: count(/validation/current-validation/list1[key = current() and /validation/current-validation/list1[key = 'key1']/value = /validation/current-validation/list1[key = current()]/value]) = 1";
		assertEquals(expectedErrorMsg, response.getErrors().get(0).getErrorMessage());
		assertEquals("/validation:validation/validation:current-validation/validation:leaf1",
				response.getErrors().get(0).getErrorPath());
		assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, response.getErrors().get(0).getErrorTag());
    }
    
    @Test
    public void testMustCountWithMultipleCurrent() throws Exception {
    	getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
    			"<validation xmlns=\"urn:org:bbf2:pma:validation\">"+
    			"	<current-validation>"+
    			"  		<list1>"+
    			"    		<key>key1</key>"+
    			"	 		<type>type1</type>"+
    			"	 		<value>10</value>"+
    			"  		</list1>"+
    			"		<leaf1>key1</leaf1>"+
    			"	</current-validation>"+
    			"</validation>";
		
		editConfig(m_server, m_clientInfo, requestXml1, true);
		
		String expectedOutput = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
				+ "<data>" 
				+ " <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
				+"		<validation:current-validation>"
				+"			<validation:leaf1>key1</validation:leaf1>"
				+"			<validation:list1>"
				+"				<validation:key>key1</validation:key>"
				+"				<validation:type>type1</validation:type>"
				+"				<validation:value>10</validation:value>"
				+"			</validation:list1>"
				+"		</validation:current-validation>"
				+"	</validation:validation>"
				+" </data>"
				+"</rpc-reply>";

    	verifyGet(expectedOutput);
    }
    
    @Test
    public void testGroupReferenceWithoutPrefixes() throws Exception {
    	getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <validation>GroupPrefixes</validation>" +
                "  <group-validation-without-prefixes>" +
                "   <groupContainer2>" +
                "    <groupContainerLeaf2>GroupPrefixes</groupContainerLeaf2>" +
                "   </groupContainer2>" +
                "  </group-validation-without-prefixes>" +
                "</validation>                                                 " ;
        editConfig(m_server, m_clientInfo, requestXml1, true);
    	
    }
    
    @Test
    public void testCasesWithSameLeafName() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                 "<validation xmlns=\"urn:org:bbf2:pma:validation\">"   +
                 " <routing xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"replace\">" +
		         "  <control-plane-protocols>" +
		         "   <control-plane-protocol>" +
		         "    <type>identity2</type>" +
		         "    <name>1111</name>"+
		         "    <static-routes>"+
		         "     <ipv4>"+
		         "      <route>"+
		         "       <destination-prefix>0.0.0.0/0</destination-prefix>"+
		         "       <next-hop>"+
		         "        <next-hop-address>135.249.41.1</next-hop-address>"+
		         "       </next-hop>"+
		         "      </route>"+
		         "     </ipv4>"+
		         "    </static-routes>"+
		         "   </control-plane-protocol>"+
		         "  </control-plane-protocols>"+
		         " </routing>" +
                 "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);  
    }
    
    @Test
    public void testMustWithDerivedFromOrLeaf() throws Exception {
    	
    	String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation5 xmlns=\"urn:org:bbf2:pma:validation\">"+
                "    <type>identity1</type>" +
                "</validation5>";
    	
        getModelNode();
        NetConfResponse  response = editConfig(m_server, m_clientInfo, requestXml, false);  

		String expectedErrorMsg = "Violate must constraints: derived-from-or-self(/validation5/type, 'validation:identity2')";
		assertEquals(expectedErrorMsg, response.getErrors().get(0).getErrorMessage());
		assertEquals("/validation:validation5/validation:must-with-derived-from-or-self",
				response.getErrors().get(0).getErrorPath());
		assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, response.getErrors().get(0).getErrorTag());
        
		requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation5 xmlns=\"urn:org:bbf2:pma:validation\">"+
                "    <type>identity2</type>" +
                "</validation5>";
        editConfig(m_server, m_clientInfo, requestXml, true);
        
		String expectedOutput = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
				+ "<data>" 
				+ "	<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
				+ "		<validation:validation5 xmlns:validation=\"urn:org:bbf2:pma:validation\">"
				+ "			<validation:must-with-derived-from-or-self>" 
				+ "				<validation:mustLeaf>must</validation:mustLeaf>"
				+ "			</validation:must-with-derived-from-or-self>"
				+ "			<validation:type>validation:identity2</validation:type>" 
				+ "		</validation:validation5>" 
				+ "	</data>"
				+ "</rpc-reply>";

    	verifyGet(expectedOutput);
    }
    
    @Test
    public void testAugmentWhenWithMandatoryLeaf() throws Exception {
    	getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf2:pma:validation\">"   +
        		"  <augmentContainer xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"create\">" +
                "  </augmentContainer>" +
        		"</validation>"
                ;
        editConfig(m_server, m_clientInfo, requestXml1, true);
    }
    
    @Test
    public void testNonExistentLeaf() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf2:pma:validation\">"   +
                "  <nonExistantLeaf>test</nonExistantLeaf>"+
                "</validation>"
                ;
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false); 

        String expectedErrorMsg = "Violate when constraints: 1 = 1 and iamImpactNode != 'iAmNotNull'";
        assertEquals(expectedErrorMsg, response.getErrors().get(0).getErrorMessage());
        assertEquals("/validation:validation/validation:nonExistantLeaf",
                response.getErrors().get(0).getErrorPath());
    }

    @Test
    public void testNullOnFunctionValue() throws Exception{
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf2:pma:validation\">"+
                "<containsLeaf>1</containsLeaf>" +
                "</validation>";
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals("/validation:validation/validation:containsLeaf", response.getErrors().get(0).getErrorPath());
        assertEquals("Violate must constraints: contains(../validation,'hello')", response.getErrors().get(0).getErrorMessage());
    }
    
    @Test
    public void testImpactNodesWithMultipleLeafRefNodes() throws Exception{
    	getModelNode();
    	String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
    			"<validation3 xmlns=\"urn:org:bbf2:pma:validation\">"+
    			"  <must-validation-with-mandatory>"+
    			"    <value>test</value>" +
    			"    <mandatory-leaf>yes</mandatory-leaf>"+
    			"  </must-validation-with-mandatory>"+
    			"  <must-validation1>"+
    			"    <key1>key</key1>" +
    			"    <value>test</value>" +
    			"    <mandatory-one>yes</mandatory-one>" +
    			"  </must-validation1>"+
    			"  <classifiers>"+
    			"   <classifier-entry>" +
    			"    <name>class1</name>" +
    			"    <match-criteria>" +
    			"     <dscp-range>11</dscp-range>" +
    			"    </match-criteria>" +
    			"   </classifier-entry>" +
    			"   <classifier-entry>" +
    			"    <name>class2</name>" +
    			"    <match-criteria>" +
    			"     <dscp-range>11</dscp-range>" +
    			"    </match-criteria>" +
    			"   </classifier-entry>" +
    			"  </classifiers>"+
    			"  <policies>"+
    			"   <policy>" +
    			"    <name>policy1</name>" +
    			"    <classifiers>" +
    			"     <name>class1</name>" +
    			"    </classifiers>" +
    			"    <classifiers>" +
    			"     <name>class2</name>" +
    			"    </classifiers>" +
    			"   </policy>" +
    			"  </policies>"+
    			"  <qos-policy-profiles>"+
    			"   <policy-profile>" +
    			"    <name>qospolicy1</name>" +
    			"    <policy-list>" +
    			"     <name>policy1</name>" +
    			"    </policy-list>" +
    			"   </policy-profile>" +
    			"  </qos-policy-profiles>"+
    			"</validation3>";
        editConfig(m_server, m_clientInfo, requestXml1, true);
        
    	String expectedOutput = 
    			"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" +
    					"<data>" +
    					"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>" +
    					"<validation:validation3 xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
    					"<validation:must-validation-with-mandatory>" +
    					"<validation:mandatory-leaf>yes</validation:mandatory-leaf>" +
    					"<validation:value>test</validation:value>" +
    					"</validation:must-validation-with-mandatory>" +
    					"<validation:must-validation1>" +
    					"<validation:key1>key</validation:key1>" +
    					"<validation:mandatory-one>yes</validation:mandatory-one>" +
    					"<validation:value>test</validation:value>" +
    					"</validation:must-validation1>" +
    					"<validation:classifiers>" +
    					"<validation:classifier-entry>" +
    					"<validation:match-criteria>" +
    					"<validation:dscp-range>11</validation:dscp-range>" +
    					"</validation:match-criteria>" +
    					"<validation:name>class1</validation:name>" +
    					"</validation:classifier-entry>" +
    					"<validation:classifier-entry>" +
    					"<validation:match-criteria>" +
    					"<validation:dscp-range>11</validation:dscp-range>" +
    					"</validation:match-criteria>" +
    					"<validation:name>class2</validation:name>" +
    					"</validation:classifier-entry>" +
    					"</validation:classifiers>" +
    					"<validation:policies>" +
    					"<validation:policy>" +
    					"<validation:classifiers>" +
    					"<validation:name>class1</validation:name>" +
    					"</validation:classifiers>" +
    					"<validation:classifiers>" +
    					"<validation:name>class2</validation:name>" +
    					"</validation:classifiers>" +
    					"<validation:name>policy1</validation:name>" +
    					"</validation:policy>" +
    					"</validation:policies>" +
    					"<validation:qos-policy-profiles>" +
    					"<validation:policy-profile>" +
    					"<validation:name>qospolicy1</validation:name>" +
    					"<validation:policy-list>" +
    					"<validation:name>policy1</validation:name>" +
    					"</validation:policy-list>" +
    					"</validation:policy-profile>" +
    					"</validation:qos-policy-profiles>" +
    					"</validation:validation3>" +
    					"</data>" +
    					"</rpc-reply>";

    	verifyGet(expectedOutput);
    	
    	requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
    			"<validation3 xmlns=\"urn:org:bbf2:pma:validation\">"+
    			"  <must-validation-with-mandatory>"+
    			"    <value>test</value>" +
    			"    <mandatory-leaf>yes</mandatory-leaf>"+
    			"  </must-validation-with-mandatory>"+
    			"  <must-validation1>"+
    			"    <key1>key</key1>" + 
    			"    <value>test</value>" + 
    			"    <mandatory-one>yes</mandatory-one>" + 
    			"  </must-validation1>" +
    			"  <classifiers>"+
    			"   <classifier-entry>" +
    			"    <name>class3</name>" +
    			"    <match-criteria>" +
    			"     <dscp-range>11</dscp-range>" +
    			"    </match-criteria>" +
    			"   </classifier-entry>" +
    			"  </classifiers>"+
    			"</validation3>";
        editConfig(m_server, m_clientInfo, requestXml1, true);
        
    	expectedOutput = 
    			"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" +
    					"<data>" +
    					"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>" +
    					"<validation:validation3 xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
    					"<validation:must-validation-with-mandatory>" +
    					"<validation:mandatory-leaf>yes</validation:mandatory-leaf>" +
    					"<validation:value>test</validation:value>" +
    					"</validation:must-validation-with-mandatory>" +
    					"<validation:must-validation1>" +
    					"<validation:key1>key</validation:key1>" +
    					"<validation:mandatory-one>yes</validation:mandatory-one>" +
    					"<validation:value>test</validation:value>" +
    					"</validation:must-validation1>" +
    					"<validation:classifiers>" +
    					"<validation:classifier-entry>" +
    					"<validation:match-criteria>" +
    					"<validation:dscp-range>11</validation:dscp-range>" +
    					"</validation:match-criteria>" +
    					"<validation:name>class1</validation:name>" +
    					"</validation:classifier-entry>" +
    					"<validation:classifier-entry>" +
    					"<validation:match-criteria>" +
    					"<validation:dscp-range>11</validation:dscp-range>" +
    					"</validation:match-criteria>" +
    					"<validation:name>class2</validation:name>" +
    					"</validation:classifier-entry>" +
    					"<validation:classifier-entry>" +
    					"<validation:match-criteria>" +
    					"<validation:dscp-range>11</validation:dscp-range>" +
    					"</validation:match-criteria>" +
    					"<validation:name>class3</validation:name>" +
    					"</validation:classifier-entry>" +
    					"</validation:classifiers>" +
    					"<validation:policies>" +
    					"<validation:policy>" +
    					"<validation:classifiers>" +
    					"<validation:name>class1</validation:name>" +
    					"</validation:classifiers>" +
    					"<validation:classifiers>" +
    					"<validation:name>class2</validation:name>" +
    					"</validation:classifiers>" +
    					"<validation:name>policy1</validation:name>" +
    					"</validation:policy>" +
    					"</validation:policies>" +
    					"<validation:qos-policy-profiles>" +
    					"<validation:policy-profile>" +
    					"<validation:name>qospolicy1</validation:name>" +
    					"<validation:policy-list>" +
    					"<validation:name>policy1</validation:name>" +
    					"</validation:policy-list>" +
    					"</validation:policy-profile>" +
    					"</validation:qos-policy-profiles>" +
    					"</validation:validation3>" +
    					"</data>" +
    					"</rpc-reply>";

    	verifyGet(expectedOutput);
    }
    
	/**
	 * Test Identity ref validation which is defined in different module with default identity value
	 * @throws Exception
	 */
    @Test   
    public void testIdentityRefDifferentModule_withDefault() throws Exception {
    	getModelNode();
    	
    	// Validate identity ref validation and created default identity ref
    	String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
    			"<validation xmlns=\"urn:org:bbf2:pma:validation\">"+
    			"    <load-balancing>round-robin</load-balancing>"+
    			"</validation>"
    			;
    	editConfig(m_server, m_clientInfo, requestXml1, true);     

    	String expectedOutput = 
    			"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" +
    					"<data>" +
    					"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
    					"<validation:identityContainer>" +
    					"<validation:load-balancer>" +
    					"<validation:distribution-algorithm xmlns:lb=\"urn:org:bbf2:pma:load-balancer\">lb:round-robin</validation:distribution-algorithm>" +
    					"</validation:load-balancer>" +
    					"</validation:identityContainer>" +
    					"<validation:load-balancing>round-robin</validation:load-balancing>"+
    					"</validation:validation>" +
    					"</data>" +
    					"</rpc-reply>";
    	
    	verifyGet(expectedOutput);
    	
    	// Remove the container which has identity ref and default nodes still has to auto-instantiate
    	requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
    			"<validation xmlns=\"urn:org:bbf2:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"+
    			"<identityContainer xc:operation=\"remove\"/>" +
    			"</validation>"
    			;
    	editConfig(m_server, m_clientInfo, requestXml1, true);  
    	
    	expectedOutput = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" +
                        "<data>" +
                        "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
                        "<validation:identityContainer>" +
                        "<validation:load-balancer>" +
                        "<validation:distribution-algorithm xmlns:lb=\"urn:org:bbf2:pma:load-balancer\">lb:round-robin</validation:distribution-algorithm>" +
                        "</validation:load-balancer>" +
                        "</validation:identityContainer>" +
                        "<validation:load-balancing>round-robin</validation:load-balancing>"+
                        "</validation:validation>" +
                        "</data>" +
                        "</rpc-reply>";
    	
    	verifyGet(expectedOutput);
    	
    	// Again the default identity ref will be added
    	requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
    			"<validation xmlns=\"urn:org:bbf2:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"+
    			"<load-balancing xc:operation=\"replace\">round-robin</load-balancing>"+
    			"</validation>"
    			;
    	editConfig(m_server, m_clientInfo, requestXml1, true);  
    	
    	String expectedOutput1= 
    			"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" +
    					"<data>" +
    					"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">" +
    					"<validation:identityContainer>" +
    					"<validation:load-balancer>" +
    					"<validation:distribution-algorithm xmlns:lb=\"urn:org:bbf2:pma:load-balancer\">lb:round-robin</validation:distribution-algorithm>" +
    					"</validation:load-balancer>" +
    					"</validation:identityContainer>" +
    					"<validation:load-balancing>round-robin</validation:load-balancing>"+
    					"</validation:validation>" +
    					"</data>" +
    					"</rpc-reply>";
    	
    	verifyGet(expectedOutput1);
    }
    
    @Test
    public void testMustConstraint_LeafViolation() throws Exception{
    	
    	getModelNode();
    	String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
    			"<validation3 xmlns=\"urn:org:bbf2:pma:validation\">"+
    			"  <must-validation-with-mandatory>"+
    			"    <value>test</value>" +
    			"    <mandatory-leaf>yes</mandatory-leaf>"+
    			"  </must-validation-with-mandatory>"+
    			"  <must-validation1>"+
    			"    <key1>key</key1>" +
    			"    <value>test</value>" +
    			"    <mandatory-one>yes</mandatory-one>" +
    			"  </must-validation1>"+   	        
    			"  <must-validation>"+
    			"    <key1>key</key1>" +
    			"    <value>test</value>" +
    			"  </must-validation>"+
    			"</validation3>";
        editConfig(m_server, m_clientInfo, requestXml, true);
        
		String expectedOutput = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
				+ "<data>" 
				+ "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
				+ "<validation:validation3 xmlns:validation=\"urn:org:bbf2:pma:validation\">"
				+ "<validation:must-validation-with-mandatory>"
				+ "<validation:mandatory-leaf>yes</validation:mandatory-leaf>"
				+ "<validation:value>test</validation:value>"
				+ "</validation:must-validation-with-mandatory>"
				+ "<validation:must-validation1>" 
				+ "<validation:key1>key</validation:key1>"
				+ "<validation:mandatory-one>yes</validation:mandatory-one>"
				+ "<validation:value>test</validation:value>" 
				+ "</validation:must-validation1>"				
				+ "<validation:must-validation>" 
				+ "<validation:key1>key</validation:key1>"
				+ "<validation:value>test</validation:value>"
				+ "<validation:test-leaf>leaf3</validation:test-leaf>"
				+ "</validation:must-validation>"
				+ "</validation:validation3>" 
				+ "</data>" 
				+ "</rpc-reply>";
        
        verifyGet(expectedOutput);

    	requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
    			"<validation3 xmlns=\"urn:org:bbf2:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"+
    			"  <must-validation>"+
    			"    <key1>key</key1>" +
    			"    <value xc:operation=\"delete\">test</value>" +
    			"  </must-validation>"+
    			"</validation3>";
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
        assertEquals("/validation:validation3/validation:must-validation[validation:key1='key']", response.getErrors().get(0).getErrorPath());
        assertEquals("Violate must constraints: value ='test' ", response.getErrors().get(0).getErrorMessage());
    }
    
    @Test
    public void testMustConstraint_KeyandLeafViolation() throws Exception{
    	
    	getModelNode();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation3 xmlns=\"urn:org:bbf2:pma:validation\">"+
                "  <must-validation-with-mandatory>"+
                "    <value>test</value>" +
                "    <mandatory-leaf>yes</mandatory-leaf>"+
                "  </must-validation-with-mandatory>"+
                "  <must-validation1>"+
                "    <key1>key</key1>" +
                "    <value>test</value>" +
                "    <mandatory-one>yes</mandatory-one>" +
                "  </must-validation1>"+
                "</validation3>";
        editConfig(m_server, m_clientInfo, requestXml, true);
        
		String expectedOutput = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
				+ "<data>" 
				+ "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
				+ "<validation:validation3 xmlns:validation=\"urn:org:bbf2:pma:validation\">"
				+ "<validation:must-validation-with-mandatory>"
				+ "<validation:mandatory-leaf>yes</validation:mandatory-leaf>"
				+ "<validation:value>test</validation:value>"
				+ "</validation:must-validation-with-mandatory>"
				+ "<validation:must-validation1>" 
				+ "<validation:key1>key</validation:key1>"
				+ "<validation:mandatory-one>yes</validation:mandatory-one>"
				+ "<validation:value>test</validation:value>" 
				+ "</validation:must-validation1>"
				+ "</validation:validation3>" 
				+ "</data>" 
				+ "</rpc-reply>";
        
        verifyGet(expectedOutput);

    	requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
    			"<validation3 xmlns=\"urn:org:bbf2:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"+
    			"  <must-validation1>"+
    			"    <key1>key</key1>" +
    			"    <value xc:operation=\"delete\">test</value>" +
    			"    <mandatory-one>yes</mandatory-one>" +
    			"  </must-validation1>"+
    			"</validation3>";
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
        assertEquals("/validation:validation3/validation:must-validation1[validation:key1='key']", response.getErrors().get(0).getErrorPath());
        assertEquals("Violate must constraints: key1 = 'key' and value ='test' ", response.getErrors().get(0).getErrorMessage());
  
    }

    @Test
    public void testListWithMustConstraintAndMandatoryFail() throws Exception{

        getModelNode();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation3 xmlns=\"urn:org:bbf2:pma:validation\">"+
                "  <must-validation1>"+
                "    <key1>key</key1>" +
                "    <value>test</value>" +
                "  </must-validation1>"+
                "</validation3>";
        Element requestElement = TestUtil.transformToElement(requestXml);
        testFail(requestElement, NetconfRpcErrorTag.DATA_MISSING,
                     NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error, "instance-required",
                     "Missing mandatory node - mandatory-one", "/validation:validation3/validation:must-validation1[validation:key1='key']/validation:mandatory-one");
    }


    @Test
    public void testValidListAndContainerWithMustConstraintAndMandatory() throws Exception{

        getModelNode();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation3 xmlns=\"urn:org:bbf2:pma:validation\">"+
                "  <must-validation-with-mandatory>"+
                "    <value>test</value>" +
                "    <mandatory-leaf>yes</mandatory-leaf>"+
                "  </must-validation-with-mandatory>"+
                "  <must-validation1>"+
                "    <key1>key</key1>" +
                "    <value>test</value>" +
                "    <mandatory-one>yes</mandatory-one>" +
                "  </must-validation1>"+
                "</validation3>";
        editConfig(m_server, m_clientInfo, requestXml, true);
        String expectedOutput = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>" 
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "<validation:validation3 xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                + "<validation:must-validation-with-mandatory>"
                + "<validation:mandatory-leaf>yes</validation:mandatory-leaf>"
                + "<validation:value>test</validation:value>"
                + "</validation:must-validation-with-mandatory>"
                + "<validation:must-validation1>" 
                + "<validation:key1>key</validation:key1>"
                + "<validation:mandatory-one>yes</validation:mandatory-one>"
                + "<validation:value>test</validation:value>" 
                + "</validation:must-validation1>"
                + "</validation:validation3>" 
                + "</data>" 
                + "</rpc-reply>";

        verifyGet(expectedOutput);
    }

    @Test
    public void testContainerWithMustConstraintAndMandatoryFail() throws Exception{

        getModelNode();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation3 xmlns=\"urn:org:bbf2:pma:validation\">"+
                "  <must-validation-with-mandatory>"+
                "    <value>test</value>" +
                "  </must-validation-with-mandatory>"+
                "</validation3>";
        Element requestElement = TestUtil.transformToElement(requestXml);
        testFail(requestElement, NetconfRpcErrorTag.DATA_MISSING,
                     NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error, "instance-required",
                     "Missing mandatory node - mandatory-leaf", "/validation:validation3/validation:must-validation-with-mandatory/validation:mandatory-leaf");
    }

    @Test
    public void testMustConstraint_LeafListViolation() throws Exception{
    	
    	getModelNode();
    	String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
    			"<validation3 xmlns=\"urn:org:bbf2:pma:validation\">"+
    			"  <must-validation-with-mandatory>" +
    			"    <value>test</value>" +
    			"    <mandatory-leaf>yes</mandatory-leaf>"+
    			"  </must-validation-with-mandatory>"+
    			"  <must-validation1>"+
    			"    <key1>key</key1>" +
    			"    <value>test</value>" +
    			"    <mandatory-one>yes</mandatory-one>" +
    			"  </must-validation1>"+
    			"  <must-validation2>"+
    			"    <key1>key</key1>" +
    			"    <values>test</values>" +
    			"  </must-validation2>"+
    			"</validation3>";
        editConfig(m_server, m_clientInfo, requestXml, true);
        
		String expectedOutput = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
				+ "<data>" 
				+ "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
				+ "<validation:validation3 xmlns:validation=\"urn:org:bbf2:pma:validation\">"
				+ "<validation:must-validation-with-mandatory>"
				+ "<validation:mandatory-leaf>yes</validation:mandatory-leaf>"
				+ "<validation:value>test</validation:value>"
				+ "</validation:must-validation-with-mandatory>"
				+ "<validation:must-validation1>" 
				+ "<validation:key1>key</validation:key1>"
				+ "<validation:mandatory-one>yes</validation:mandatory-one>"
				+ "<validation:value>test</validation:value>" 
				+ "</validation:must-validation1>"				
				+ "<validation:must-validation2>" 
				+ "<validation:key1>key</validation:key1>"
				+ "<validation:values>test</validation:values>" 
				+ "</validation:must-validation2>"
				+ "</validation:validation3>" 
				+ "</data>" 
				+ "</rpc-reply>";
        
        verifyGet(expectedOutput);

    	requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
    			"<validation3 xmlns=\"urn:org:bbf2:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"+
    			"  <must-validation-with-mandatory>" +
    			"    <value>test</value>" +
    			"    <mandatory-leaf>yes</mandatory-leaf>"+
    			"  </must-validation-with-mandatory>"+
    			"  <must-validation1>"+
    			"    <key1>key</key1>" +
    			"    <value>test</value>" +
    			"    <mandatory-one>yes</mandatory-one>" +
    			"  </must-validation1>"+       	        
    			"  <must-validation2>"+
    			"    <key1>key</key1>" +
    			"    <values xc:operation=\"delete\">test</values>" +
    			"  </must-validation2>"+
    			"</validation3>";
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
        assertEquals("/validation:validation3/validation:must-validation2[validation:key1='key']", response.getErrors().get(0).getErrorPath());
        assertEquals("Violate must constraints: key1 = 'key' and values ='test' ", response.getErrors().get(0).getErrorMessage());
    }

    @Test
    public void testWhenConstraint_LeafListViolation() throws Exception{
    	
    	getModelNode();
    	String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
    			"<validation3 xmlns=\"urn:org:bbf2:pma:validation\">"+
    			"  <must-validation-with-mandatory>"+
    			"    <value>test</value>" +
    			"    <mandatory-leaf>yes</mandatory-leaf>"+
    			"  </must-validation-with-mandatory>"+
    			"  <must-validation1>"+
    			"    <key1>key</key1>" +
    			"    <value>test</value>" +
    			"    <mandatory-one>yes</mandatory-one>" +
    			"  </must-validation1>"+       	        
    			"  <when-validation>"+
    			"    <key1>key</key1>" +
    			"    <values>test</values>" +
    			"    <leaf1>value</leaf1>"  +
    			"  </when-validation>"+
    			"</validation3>";
        editConfig(m_server, m_clientInfo, requestXml, true);
        
		String expectedOutput = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
				+ "<data>" 
				+ "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
				+ "<validation:validation3 xmlns:validation=\"urn:org:bbf2:pma:validation\">"
				+ "<validation:must-validation-with-mandatory>"
				+ "<validation:mandatory-leaf>yes</validation:mandatory-leaf>" 
				+ "<validation:value>test</validation:value>"
				+ "</validation:must-validation-with-mandatory>" 
				+ "<validation:must-validation1>"
				+ "<validation:key1>key</validation:key1>" 
				+ "<validation:mandatory-one>yes</validation:mandatory-one>"
				+ "<validation:value>test</validation:value>" 
				+ "</validation:must-validation1>" 
				+ "<validation:when-validation>"
				+ "<validation:key1>key</validation:key1>"
				+ "<validation:leaf1>value</validation:leaf1>"
				+ "<validation:values>test</validation:values>" 
				+ "</validation:when-validation>"
				+ "</validation:validation3>" 
				+ "</data>" 
				+ "</rpc-reply>";
        
        verifyGet(expectedOutput);

    	requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
    			"<validation3 xmlns=\"urn:org:bbf2:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"+
    			"  <must-validation-with-mandatory>" +
    			"    <value>test</value>" + 
    			"    <mandatory-leaf>yes</mandatory-leaf>" + 
    			"  </must-validation-with-mandatory>" + 
    			"  <must-validation1>" + 
    			"    <key1>key</key1>" + 
    			"    <value>test</value>" + 
    			"    <mandatory-one>yes</mandatory-one>" + 
    			"  </must-validation1>" +
    			"  <when-validation>" +
    			"    <key1>key</key1>" +
    			"    <values xc:operation=\"delete\">test</values>" +
    			"  </when-validation>"+
    			"</validation3>";
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
        assertEquals("/validation:validation3/validation:when-validation[validation:key1='key']", response.getErrors().get(0).getErrorPath());
        assertEquals("Violate when constraints: key1 = 'key' and values = 'test'", response.getErrors().get(0).getErrorMessage());
    }

    @Test
    public void testListWhenMandtoryFail() throws Exception{

        getModelNode();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation3 xmlns=\"urn:org:bbf2:pma:validation\">"+
                "  <when-validation>"+
                "    <key1>key</key1>" +
                "    <values>test</values>" +
                "  </when-validation>"+
                "</validation3>"
                ;
        Element requestElement = TestUtil.transformToElement(requestXml);
        testFail(requestElement, NetconfRpcErrorTag.DATA_MISSING,
                     NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error, "instance-required",
                     "Missing mandatory node - leaf1", "/validation:validation3/validation:when-validation[validation:key1='key']/validation:leaf1");
    }
    
    @Test
    public void testWhenOnChoice() throws Exception {
    	String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation5 xmlns=\"urn:org:bbf2:pma:validation\">"+
                "    <type>identity2</type>" +
                "    <whenOnChoice>" +
                "    	<key2>10</key2>" +
                "    	<identityLeaf>identity2</identityLeaf>" +
               "    	<case1Leaf>test</case1Leaf>" +
                "    </whenOnChoice>" +
                "</validation5>";
    	
        getModelNode();
        editConfig(m_server, m_clientInfo, requestXml, true);
        
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation5 xmlns=\"urn:org:bbf2:pma:validation\">"+
                "    <whenOnChoice xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" xc:operation=\"remove\">" +
                "    	<key2>10</key2>" +
                "    </whenOnChoice>" +
                "</validation5>";
        editConfig(m_server, m_clientInfo, requestXml, true);
    }
    
	@Test
	public void testParentNodeDeletionWithImpactNodeAsChild() throws Exception{
		getModelNode();
		// Create spectrum-profile and mode-profile
		String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation3 xmlns=\"urn:org:bbf2:pma:validation\">"+
				"  <must-validation-with-mandatory>"+
				"    <value>test</value>" +
				"    <mandatory-leaf>yes</mandatory-leaf>"+
				"  </must-validation-with-mandatory>"+
				"  <must-validation1>"+
				"    <key1>key</key1>" +
				"    <value>test</value>" +
				"    <mandatory-one>yes</mandatory-one>" +
				"  </must-validation1>"+
				" <spectrum>"+
				"  <spectrum-profile>"+
				"    <name>bbf1</name>" +
				"  	 <list1>"+
				"    	<ts>key1</ts>" +
				"    	<mode-profile>profile1</mode-profile>" +
				"  	 </list1>"+
				"  </spectrum-profile>"+
				"  <mode-profile>"+
				"    <name>profile1</name>" +
				"    <ts>key1</ts>" +
				"  </mode-profile>"+
				" </spectrum>"+
				"</validation3>";

		editConfig(m_server, m_clientInfo, requestXml, true);
		
		requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation3 xmlns=\"urn:org:bbf2:pma:validation\">"+
				"  <must-validation-with-mandatory>"+
				"    <value>test</value>" +
				"    <mandatory-leaf>yes</mandatory-leaf>"+
				"  </must-validation-with-mandatory>"+
				"  <must-validation1>"+
				"    <key1>key</key1>" +
				"    <value>test</value>" +
				"    <mandatory-one>yes</mandatory-one>" +
				"  </must-validation1>" +
				" <spectrum>"+
				"  <spectrum-profile>"+
				"    <name>bbf</name>" +
				"  	 <list1>"+
				"    	<ts>key</ts>" +
				"    	<mode-profile>profile</mode-profile>" +
				"  	 </list1>"+
				"  </spectrum-profile>"+
				"  <mode-profile>"+
				"    <name>profile</name>" +
				"    <ts>key</ts>" +
				"  </mode-profile>"+
				" </spectrum>"+
				"</validation3>";
		editConfig(m_server, m_clientInfo, requestXml, true);

		// Verify the response
		String getResponse = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"+
				"<data>"+
				" <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"+
				" <validation:validation3 xmlns:validation=\"urn:org:bbf2:pma:validation\">"+
				"   <validation:must-validation-with-mandatory>" +
				"      <validation:mandatory-leaf>yes</validation:mandatory-leaf>" +
				"      <validation:value>test</validation:value>" +
				"   </validation:must-validation-with-mandatory>" +
				"   <validation:must-validation1>" +
				"      <validation:key1>key</validation:key1>" +
				"      <validation:mandatory-one>yes</validation:mandatory-one>" +
				"      <validation:value>test</validation:value>" +
				"   </validation:must-validation1>" +
				"	<validation:spectrum>"+
				"		<validation:mode-profile>"+
				"			<validation:name>profile1</validation:name>"+
				"			<validation:ts>key1</validation:ts>"+
				"		</validation:mode-profile>"+
				"		<validation:mode-profile>"+
				"			<validation:name>profile</validation:name>"+
				"			<validation:ts>key</validation:ts>"+
				"		</validation:mode-profile>"+
				"		<validation:spectrum-profile>"+
				"			<validation:list1>"+
				"				<validation:mode-profile>profile1</validation:mode-profile>"+
				"				<validation:ts>key1</validation:ts>"+
				"			</validation:list1>"+
				"			<validation:name>bbf1</validation:name>"+
				"		</validation:spectrum-profile>"+
				"		<validation:spectrum-profile>"+
				"			<validation:list1>"+
				"				<validation:mode-profile>profile</validation:mode-profile>"+
				"				<validation:ts>key</validation:ts>"+
				"			</validation:list1>"+
				"			<validation:name>bbf</validation:name>"+
				"		</validation:spectrum-profile>"+
				"	</validation:spectrum>"+
				"  </validation:validation3>"+
				" </data>"+
				"</rpc-reply>";
		verifyGet(getResponse);

		//Remove impact node alone
		requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation3 xmlns=\"urn:org:bbf2:pma:validation\">"+
				"  <must-validation-with-mandatory>"+
				"    <value>test</value>" +
				"    <mandatory-leaf>yes</mandatory-leaf>"+
				"  </must-validation-with-mandatory>"+
				"  <must-validation1>"+
				"    <key1>key</key1>" +
				"    <value>test</value>" +
				"    <mandatory-one>yes</mandatory-one>" +
				"  </must-validation1>"+		        
				" <spectrum>"+
				"  <mode-profile xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\">"+
				"    <name>profile</name>" +
				"    <ts>key</ts>" +
				"  </mode-profile>"+
				" </spectrum>"+
				"</validation3>";

		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
		assertEquals("instance-required", response.getErrors().get(0).getErrorAppTag());
		assertEquals("/validation:validation3/validation:spectrum/validation:spectrum-profile[validation:name='bbf']/validation:list1[validation:ts='key']/validation:mode-profile", response.getErrors().get(0).getErrorPath());
		assertEquals("Dependency violated, 'profile' must exist", response.getErrors().get(0).getErrorMessage());

		// Remove impacted node and reference node
		requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation3 xmlns=\"urn:org:bbf2:pma:validation\">"+
				"  <must-validation-with-mandatory>"+
				"    <value>test</value>" +
				"    <mandatory-leaf>yes</mandatory-leaf>"+
				"  </must-validation-with-mandatory>"+
				"  <must-validation1>"+
				"    <key1>key</key1>" +
				"    <value>test</value>" +
				"    <mandatory-one>yes</mandatory-one>" +
				"  </must-validation1>"+		        
				" <spectrum>"+
				"  <spectrum-profile xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\">"+
				"    <name>bbf</name>" +
				"  	 <list1>"+
				"    	<ts>key</ts>" +
				"    	<mode-profile>profile</mode-profile>" +
				"  	 </list1>"+
				"  </spectrum-profile>"+
				"  <mode-profile xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\">"+
				"    <name>profile</name>" +
				"    <ts>key</ts>" +
				"  </mode-profile>"+
				" </spectrum>"+
				"</validation3>";

		editConfig(m_server, m_clientInfo, requestXml, true);

		//Verify the response after deletion
		getResponse= "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"+
				"<data>"+
				"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"+
				" <validation:validation3 xmlns:validation=\"urn:org:bbf2:pma:validation\">"+
				"   <validation:must-validation-with-mandatory>" +
				"      <validation:mandatory-leaf>yes</validation:mandatory-leaf>" +
				"       <validation:value>test</validation:value>" +
				"   </validation:must-validation-with-mandatory>" +
				"   <validation:must-validation1>" +
				"       <validation:key1>key</validation:key1>" +
				"       <validation:mandatory-one>yes</validation:mandatory-one>" +
				"       <validation:value>test</validation:value>" +
				"   </validation:must-validation1>" +
				"	<validation:spectrum>"+
				"		<validation:mode-profile>"+
				"			<validation:name>profile1</validation:name>"+
				"			<validation:ts>key1</validation:ts>"+
				"		</validation:mode-profile>"+
				"		<validation:spectrum-profile>"+
				"			<validation:list1>"+
				"				<validation:mode-profile>profile1</validation:mode-profile>"+
				"				<validation:ts>key1</validation:ts>"+
				"			</validation:list1>"+
				"			<validation:name>bbf1</validation:name>"+
				"		</validation:spectrum-profile>"+
				"	</validation:spectrum>"+
				" </validation:validation3>"+
				"</data>"+
				"</rpc-reply>";
		verifyGet(getResponse);
		
		// remove reference node alone
		requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<validation3 xmlns=\"urn:org:bbf2:pma:validation\">"+
				"  <must-validation-with-mandatory>"+
				"    <value>test</value>" +
				"    <mandatory-leaf>yes</mandatory-leaf>"+
				"  </must-validation-with-mandatory>"+
				"  <must-validation1>"+
				"    <key1>key</key1>" +
				"    <value>test</value>" +
				"    <mandatory-one>yes</mandatory-one>" +
				"  </must-validation1>" +
				" <spectrum>"+
				"  <spectrum-profile xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\">"+
				"    <name>bbf1</name>" +
				"  	 <list1>"+
				"    	<ts>key1</ts>" +
				"    	<mode-profile>profile1</mode-profile>" +
				"  	 </list1>"+
				"  </spectrum-profile>"+
				" </spectrum>"+
				"</validation3>";

		editConfig(m_server, m_clientInfo, requestXml, true);
		
		//Verify the response after reference node deletion
		getResponse= "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"+
				"<data>"+
				"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"+
				" <validation:validation3 xmlns:validation=\"urn:org:bbf2:pma:validation\">"+
                "   <validation:must-validation-with-mandatory>" +			
                "      <validation:mandatory-leaf>yes</validation:mandatory-leaf>" +
                "      <validation:value>test</validation:value>" +
                "   </validation:must-validation-with-mandatory>" +
                "   <validation:must-validation1>" +
                "      <validation:key1>key</validation:key1>" +
                "      <validation:mandatory-one>yes</validation:mandatory-one>" +
                "      <validation:value>test</validation:value>" +
                "   </validation:must-validation1>" +
				"	<validation:spectrum>"+
				"		<validation:mode-profile>"+
				"			<validation:name>profile1</validation:name>"+
				"			<validation:ts>key1</validation:ts>"+
				"		</validation:mode-profile>"+
				"	</validation:spectrum>"+
				" </validation:validation3>"+
				"</data>"+
				"</rpc-reply>";
		verifyGet(getResponse);
	}

    @Test
    public void testLeafWithDefaultAndWhenConstraint() throws ModelNodeInitException, SAXException, IOException {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + " <leaf-list-add-validation>"
                + "   <configured-mode>fast</configured-mode>"
                + " </leaf-list-add-validation>"
                + "</validation>"
                ;
        editConfig(m_server, m_clientInfo, requestXml1, true);
        String response =
                     "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        +"<data>"
                          +"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                            +"<validation:leaf-list-add-validation>"
                               +"<validation:configured-mode>fast</validation:configured-mode>"
                               +"<validation:fast-leaf>fast1</validation:fast-leaf>"
                            +"</validation:leaf-list-add-validation>"
                          +"</validation:validation>"
                        +"</data>"
                     +"</rpc-reply>"
                     ;
        verifyGet(response);
    }

    @Test
    public void testValidMustConstraintWithDefault() throws ModelNodeInitException, SAXException, IOException {
        getModelNode();
        initialiseInterceptor();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + " <must-validation1>"
                + "   <enabled>true</enabled>"
                + "   <leafWithMustWhen>abcd</leafWithMustWhen>"
                + " </must-validation1>"
                + "</validation>"
                ;
        editConfig(m_server, m_clientInfo, requestXml1, true);
        String response =  "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                +"<data>"
                +  "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                +     "<validation:must-validation1>"
                +        "<validation:enabled>true</validation:enabled>"
                +         "<validation:leafWithMustWhen>abcd</validation:leafWithMustWhen>"
                +          "<validation:must-leaf>leaf1</validation:must-leaf>"
                +     "</validation:must-validation1>"
                +  "</validation:validation>"
                +"</data>"
                +"</rpc-reply>"
                ;
        verifyGet(response);
    }

    @Test
    public void testValidMustOnContainerAndLeafWithDefault() throws Exception{
        String requestXml1 = "<validation1 xmlns=\"urn:org:bbf2:pma:validation\">"
                +"  <list1>"
                +"    <key1>key1</key1>"
                +"  </list1>"
                +"</validation1>"
                ;
        getModelNode();
        editConfig(m_server, m_clientInfo, requestXml1, true);
        String response = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                +  "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                +     "<validation:validation1 xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                +        "<validation:leaf1>leaf1</validation:leaf1>"
                +        "<validation:list1>"
                +           "<validation:key1>key1</validation:key1>"
                +        "</validation:list1>"
                +  "</validation:validation1>"
                + "</data>"
                +"</rpc-reply>"
                ;
        verifyGet(response);
    }

    @Test
    public void testValidPresenceContainerAndbasetypeWithDefault() throws Exception{
        String requestXml1 = "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
                +"  <presence-container-leaf-with-default>"
                +"  <test>leaf2</test>"
                +"</presence-container-leaf-with-default>"
                +"</validation>"
                ;
        getModelNode();
        editConfig(m_server, m_clientInfo, requestXml1, true);
        String response = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                +  "<data>"
                +     "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                +        "<validation:presence-container-leaf-with-default>"
                +           "<validation:decimal64-fraction1-with-default-value-type>1.7</validation:decimal64-fraction1-with-default-value-type>"
                +           "<validation:decimal64-fraction1-with-default-value-type2>1.8</validation:decimal64-fraction1-with-default-value-type2>"
                +           "<validation:test>leaf2</validation:test>"
                +        "</validation:presence-container-leaf-with-default>"
                +     "</validation:validation>"
                +  "</data>"
                +"</rpc-reply>"
                ;
        verifyGet(response);
    }

    @Test
    public void testValidContainerAndbasetypeWithDefault() throws Exception{
        String requestXml1 =  "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " 
                +"<container-leaf-with-default xmlns=\"urn:org:bbf2:pma:validation\">"
                +"  <test>leaf2</test>"
                +"</container-leaf-with-default>"
                ;
        getModelNode();
        editConfig(m_server, m_clientInfo, requestXml1, true);
        String response = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                +"<data>"
                +"<validation:container-leaf-with-default xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                +"<validation:decimal64-fraction1-with-default-value-type>2.4</validation:decimal64-fraction1-with-default-value-type>"
                + "<validation:decimal64-fraction1-with-default-value-type2>1.8</validation:decimal64-fraction1-with-default-value-type2>"
                +"<validation:test>leaf2</validation:test>"
                +"</validation:container-leaf-with-default>"
                +"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                +"</data>"
                +"</rpc-reply>"
                ;
        verifyGet(response);
    }
    @Test
    public void testListWithMustAndLeafWithDefault() throws Exception{

        getModelNode();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation3 xmlns=\"urn:org:bbf2:pma:validation\">"+
                "  <must-validation-with-mandatory>"+
                "    <value>test</value>" +
                "    <mandatory-leaf>yes</mandatory-leaf>"+
                "  </must-validation-with-mandatory>"+
                "  <must-validation>"+
                "    <key1>key</key1>" +
                "    <value>test</value>"+
                "  </must-validation>"+
                "</validation3>";
        editConfig(m_server, m_clientInfo, requestXml, true);

        String expectedOutput = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "<validation:validation3 xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                + "<validation:must-validation>"
                + "<validation:key1>key</validation:key1>"
                + "<validation:value>test</validation:value>"
                + "<validation:test-leaf>leaf3</validation:test-leaf>"
                + "</validation:must-validation>"
                + "<validation:must-validation-with-mandatory>"
                + "<validation:mandatory-leaf>yes</validation:mandatory-leaf>"
                + "<validation:value>test</validation:value>"
                + "</validation:must-validation-with-mandatory>"
                + "</validation:validation3>"
                + "</data>"
                + "</rpc-reply>"
                ;
        verifyGet(expectedOutput);
    }

    @Test
    public void testLeafWithMustAndDefault() throws Exception {
        getModelNode();
        initialiseInterceptor();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<exfunctionCanister xmlns=\"urn:opendaylight:datastore-extension-functions-test\">"
                + " <alarm-list>"
                + "  <key>key1</key>"
                + "  <severity>critical</severity>"
                + " </alarm-list>"
                + "</exfunctionCanister>";

       editConfig(m_server, m_clientInfo, requestXml1, true);

       String expectedOutput = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
	           +"<data>"
	           +"<ds-ext-func:exfunctionCanister xmlns:ds-ext-func=\"urn:opendaylight:datastore-extension-functions-test\">"
               +"<ds-ext-func:alarm-list>"
               +"<ds-ext-func:key>key1</ds-ext-func:key>"
               +"<ds-ext-func:severity>critical</ds-ext-func:severity>"
               +"<ds-ext-func:test-enum-value-with-parent-pattern>hello</ds-ext-func:test-enum-value-with-parent-pattern>"
               +"</ds-ext-func:alarm-list>"
               +"</ds-ext-func:exfunctionCanister>"
               +"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
               +"</data>"
               +"</rpc-reply>"
               ;
       verifyGet(expectedOutput);
    }

	@Test
	public void testValidListWithKeyLeafDefault() throws ModelNodeInitException, SAXException, IOException {
        getModelNode();
		String request = "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
				+ "<key-leaf-with-default>"
				+    "<id>2</id>"
				+    "<value>value1</value>"
				+ "</key-leaf-with-default>"
				+ "</validation>";
       editConfig(m_server, m_clientInfo, request, true);

       String expectedOutput = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
               +   "<data>"
               +      "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
               +         "<validation:key-leaf-with-default>"
               +            "<validation:id>2</validation:id>"
               +            "<validation:value>value1</validation:value>"
               +         "</validation:key-leaf-with-default>"
               +      "</validation:validation>"
               +   "</data>"
               +"</rpc-reply>"
               ;
       verifyGet(expectedOutput);
	}

    @Test
    public void testOrderByUserListWithDefault() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<order-by-user-validation xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" "
                + "xmlns=\"urn:org:bbf2:pma:validation\" xmlns:yang=\"urn:ietf:params:xml:ns:yang:1\" xc:operation='replace'>"
                + "  <orderByUserList yang:insert=\"first\">"
                + "   <someKey>a</someKey>"
                + "   <value>1</value>"
                + "  </orderByUserList>"
                + "  <orderByUserList yang:insert=\"after\" yang:key=\"[someKey='a']\">"
                + "   <someKey>b</someKey>"
                + "   <value>2</value>"
                + "  </orderByUserList>"
                + "  <orderByUserList yang:insert=\"after\" yang:key=\"[someKey='b']\">"
                + "   <someKey>c</someKey>"
                + "   <value>3</value>"
                + "  </orderByUserList>"
                + "  <orderByUserList yang:insert=\"after\" yang:key=\"[someKey='c']\">"
                + "   <someKey>d</someKey>"
                + "  </orderByUserList>"
                + "  <orderByUserList yang:insert=\"last\">"
                + "   <someKey>f</someKey>"
                + "  </orderByUserList>"
                + "  <orderByUserList yang:insert=\"before\" yang:key=\"[someKey='f']\">"
                + "   <someKey>e</someKey>"
                + "   <value>5</value>"
                + "  </orderByUserList>"
                + "  <orderByUserList yang:insert=\"after\" yang:key=\"[someKey='a']\">"
                + "   <someKey>g</someKey>"
                + "   <value>8</value>"
                + "  </orderByUserList>"
                + "  <orderByUserList yang:insert=\"before\" yang:key=\"[someKey='d']\">"
                + "   <someKey>i</someKey>"
                + "   <value>7</value>"
                + "  </orderByUserList>"
                + "</order-by-user-validation>"
                ;
        editConfig(m_server, m_clientInfo, requestXml1, true);

        String response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                +"<validation:order-by-user-validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                + "    <validation:orderByUserList>"
                + "     <validation:someKey>a</validation:someKey>"
                + "     <validation:value>1</validation:value>"
                + "    </validation:orderByUserList>"
                + "    <validation:orderByUserList>"
                + "     <validation:someKey>g</validation:someKey>"
                + "     <validation:value>8</validation:value>"
                + "    </validation:orderByUserList>"
                + "    <validation:orderByUserList>"
                + "     <validation:someKey>b</validation:someKey>"
                + "     <validation:value>2</validation:value>"
                + "    </validation:orderByUserList>"
                + "    <validation:orderByUserList>"
                + "     <validation:someKey>c</validation:someKey>"
                + "     <validation:value>3</validation:value>"
                + "    </validation:orderByUserList>"
                + "    <validation:orderByUserList>"
                + "     <validation:someKey>i</validation:someKey>"
                + "     <validation:value>7</validation:value>"
                + "    </validation:orderByUserList>"
                + "    <validation:orderByUserList>"
                + "     <validation:someKey>d</validation:someKey>"
                + "     <validation:value>4</validation:value>"
                + "    </validation:orderByUserList>"
                + "    <validation:orderByUserList>"
                + "     <validation:someKey>e</validation:someKey>"
                + "     <validation:value>5</validation:value>"
                + "    </validation:orderByUserList>"
                + "    <validation:orderByUserList>"
                + "     <validation:someKey>f</validation:someKey>"
                + "     <validation:value>4</validation:value>"
                + "    </validation:orderByUserList>"
                + "</validation:order-by-user-validation>"
                +"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + " </data>"
                + "</rpc-reply>"
                ;
        verifyGet(m_server, m_clientInfo, response);
    }

    @Test
    public void testOrderByUserListWithLeafRef() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<order-by-user-validation xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" "
                + "xmlns=\"urn:org:bbf2:pma:validation\" xmlns:yang=\"urn:ietf:params:xml:ns:yang:1\" xc:operation='replace'>"
                + "  <leafRefTarget>"
                + "   <someKey>a</someKey>"
                + "  </leafRefTarget>"
                + "  <leafRefTarget>"
                + "   <someKey>b</someKey>"
                + "  </leafRefTarget>"
                + "  <leafRefTarget>"
                + "   <someKey>c</someKey>"
                + "  </leafRefTarget>"
                + "  <leafRefTarget>"
                + "   <someKey>d</someKey>"
                + "  </leafRefTarget>"
                + "  <leafRefTarget>"
                + "   <someKey>e</someKey>"
                + "  </leafRefTarget>"
                + "  <leafRefTarget>"
                + "   <someKey>f</someKey>"
                + "  </leafRefTarget>"
                + "  <leafRefTarget>"
                + "   <someKey>g</someKey>"
                + "  </leafRefTarget>"
                + "  <leafRefTarget>"
                + "   <someKey>i</someKey>"
                + "  </leafRefTarget>"
                + "  <orderByUserListWithLeafRef yang:insert=\"first\">"
                + "   <someKey>a</someKey>"
                + "  </orderByUserListWithLeafRef>"
                + "  <orderByUserListWithLeafRef yang:insert=\"after\" yang:key=\"[someKey='a']\">"
                + "   <someKey>b</someKey>"
                + "  </orderByUserListWithLeafRef>"
                + "  <orderByUserListWithLeafRef yang:insert=\"after\" yang:key=\"[someKey='b']\">"
                + "   <someKey>c</someKey>"
                + "  </orderByUserListWithLeafRef>"
                + "  <orderByUserListWithLeafRef yang:insert=\"after\" yang:key=\"[someKey='c']\">"
                + "   <someKey>d</someKey>"
                + "  </orderByUserListWithLeafRef>"
                + "  <orderByUserListWithLeafRef yang:insert=\"last\">"
                + "   <someKey>f</someKey>"
                + "  </orderByUserListWithLeafRef>"
                + "  <orderByUserListWithLeafRef yang:insert=\"before\" yang:key=\"[someKey='f']\">"
                + "   <someKey>e</someKey>"
                + "  </orderByUserListWithLeafRef>"
                + "  <orderByUserListWithLeafRef yang:insert=\"after\" yang:key=\"[someKey='a']\">"
                + "   <someKey>g</someKey>"
                + "  </orderByUserListWithLeafRef>"
                + "  <orderByUserListWithLeafRef yang:insert=\"before\" yang:key=\"[someKey='d']\">"
                + "   <someKey>i</someKey>"
                + "  </orderByUserListWithLeafRef>"
                + "</order-by-user-validation>"
                ;
        editConfig(m_server, m_clientInfo, requestXml1, true);

        String response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                +"<validation:order-by-user-validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                + "  <validation:leafRefTarget>"
                + "   <validation:someKey>a</validation:someKey>"
                + "  </validation:leafRefTarget>"
                + "  <validation:leafRefTarget>"
                + "   <validation:someKey>b</validation:someKey>"
                + "  </validation:leafRefTarget>"
                + "  <validation:leafRefTarget>"
                + "   <validation:someKey>c</validation:someKey>"
                + "  </validation:leafRefTarget>"
                + "  <validation:leafRefTarget>"
                + "   <validation:someKey>d</validation:someKey>"
                + "  </validation:leafRefTarget>"
                + "  <validation:leafRefTarget>"
                + "   <validation:someKey>e</validation:someKey>"
                + "  </validation:leafRefTarget>"
                + "  <validation:leafRefTarget>"
                + "   <validation:someKey>f</validation:someKey>"
                + "  </validation:leafRefTarget>"
                + "  <validation:leafRefTarget>"
                + "   <validation:someKey>g</validation:someKey>"
                + "  </validation:leafRefTarget>"
                + "  <validation:leafRefTarget>"
                + "   <validation:someKey>i</validation:someKey>"
                + "  </validation:leafRefTarget>"
                + "    <validation:orderByUserListWithLeafRef>"
                + "     <validation:someKey>a</validation:someKey>"
                + "    </validation:orderByUserListWithLeafRef>"
                + "    <validation:orderByUserListWithLeafRef>"
                + "     <validation:someKey>g</validation:someKey>"
                + "    </validation:orderByUserListWithLeafRef>"
                + "    <validation:orderByUserListWithLeafRef>"
                + "     <validation:someKey>b</validation:someKey>"
                + "    </validation:orderByUserListWithLeafRef>"
                + "    <validation:orderByUserListWithLeafRef>"
                + "     <validation:someKey>c</validation:someKey>"
                + "    </validation:orderByUserListWithLeafRef>"
                + "    <validation:orderByUserListWithLeafRef>"
                + "     <validation:someKey>i</validation:someKey>"
                + "    </validation:orderByUserListWithLeafRef>"
                + "    <validation:orderByUserListWithLeafRef>"
                + "     <validation:someKey>d</validation:someKey>"
                + "    </validation:orderByUserListWithLeafRef>"
                + "    <validation:orderByUserListWithLeafRef>"
                + "     <validation:someKey>e</validation:someKey>"
                + "    </validation:orderByUserListWithLeafRef>"
                + "    <validation:orderByUserListWithLeafRef>"
                + "     <validation:someKey>f</validation:someKey>"
                + "    </validation:orderByUserListWithLeafRef>"
                + "</validation:order-by-user-validation>"
                +"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + " </data>"
                + "</rpc-reply>"
                ;
        verifyGet(m_server, m_clientInfo, response);
    }

    @Test
    public void testOrderByUserListMultiKey() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    "
                + "<order-by-user-validation xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\""
                + "     xmlns=\"urn:org:bbf2:pma:validation\" xmlns:yang=\"urn:ietf:params:xml:ns:yang:1\" xc:operation='replace'>"
                + "     <orderByUserListMultiKey yang:insert=\"last\">"
                + "      <firstKey xmlns:validation=\"urn:org:bbf2:pma:validation\">validation:identity1</firstKey>"
                + "      <secondKey>a</secondKey>"
                + "     </orderByUserListMultiKey>"
                + "</order-by-user-validation>"
                ; 
        
        editConfig(m_server, m_clientInfo, requestXml1, true);
        String response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "  <data>"
                + "    <validation:order-by-user-validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                + "      <validation:orderByUserListMultiKey>"
                + "       <validation:firstKey>validation:identity1</validation:firstKey>"
                + "       <validation:secondKey>a</validation:secondKey>"
                + "      </validation:orderByUserListMultiKey>"
                + "     </validation:order-by-user-validation>"
                +"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "   </data>"
                + "  </rpc-reply>"
                ;
        verifyGet(m_server, m_clientInfo, response);
        
        String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    "
                + "<order-by-user-validation xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\""
                + "     xmlns=\"urn:org:bbf2:pma:validation\" xmlns:yang=\"urn:ietf:params:xml:ns:yang:1\" xc:operation='merge'>"
                + "     <orderByUserListMultiKey xmlns:validation=\"urn:org:bbf2:pma:validation\" "
                + "          yang:insert=\"after\" yang:key=\"[firstKey='validation:identity1'][secondKey='a']\">"
                + "      <firstKey>all</firstKey>"
                + "      <secondKey>b</secondKey>"
                + "     </orderByUserListMultiKey>"
                + "</order-by-user-validation>"
                ; 
        
        editConfig(m_server, m_clientInfo, requestXml2, true);
        response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "  <data>"
                + "    <validation:order-by-user-validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                + "      <validation:orderByUserListMultiKey>"
                + "       <validation:firstKey>validation:identity1</validation:firstKey>"
                + "       <validation:secondKey>a</validation:secondKey>"
                + "      </validation:orderByUserListMultiKey>"
                + "      <validation:orderByUserListMultiKey>"
                + "       <validation:firstKey>all</validation:firstKey>"
                + "       <validation:secondKey>b</validation:secondKey>"
				+ "       <validation:value>4</validation:value>"
                + "      </validation:orderByUserListMultiKey>"
                + "     </validation:order-by-user-validation>"
                +"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "   </data>"
                + "  </rpc-reply>"
                ;
        verifyGet(m_server, m_clientInfo, response);
        
    }

	@Test
	public void testStringContainUnicodeTabNewlinefeed() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
				+ " <validate-string-unicode-container>"
				+ "  <test-container>" 
				+ "  <leaf1>ABC&#x09;123&#x41;456&#x0A;789</leaf1>" 
				+ "  </test-container>"
				+ " </validate-string-unicode-container>" 
				+ "</validation>";

		editConfig(m_server, m_clientInfo, requestXml1, true);
		String ncResponse = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" 
				+ " <data>"
				+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
				+ "   <validation:validate-string-unicode-container>" 
				+ "    <validation:test-container>"
				+ "       <validation:leaf1>ABC	123A456" 
				+ "        789</validation:leaf1>"
				+ "    </validation:test-container>" 
				+ "   </validation:validate-string-unicode-container>"
				+ "  </validation:validation>" 
				+ " </data>" 
				+ "</rpc-reply>";
		verifyGet(ncResponse);
	}
	
	@Test
	public void testStringContainillegalUnicode() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
				+ " <validate-string-unicode-container>"
				+ "  <test-container>" 
				+ "  <leaf1>456&#x0000;789</leaf1>" // illegalUnicode - &#x0000-NUL
				+ "  </test-container>"
				+ " </validate-string-unicode-container>" 
				+ "</validation>";
		try {
			editConfig(m_server, m_clientInfo, requestXml1, false);
			fail();
		} catch (Exception e) {
			assertEquals("org.xml.sax.SAXParseException; lineNumber: 1; columnNumber: 162; Character reference \"&#",
					e.getMessage());
		}
	}
	
	@Test
	public void testStringContainNoncharacterUnicode() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
				+ " <validate-string-unicode-container>"
				+ "  <test-container>" 
				+ "  <leaf1>456&#xFDD9;</leaf1>" // Non-character unicode - &#xFDD9
				+ "  </test-container>"
				+ " </validate-string-unicode-container>" 
				+ "</validation>";
	
			NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
			assertEquals("String value contains unsupported noncharacter unicode: 456\ufdd9", response.getErrors().get(0).getErrorMessage());
			assertEquals(NetconfRpcErrorType.Application, response.getErrors().get(0).getErrorType());
			assertEquals("/validation:validation/validation:validate-string-unicode-container/validation:test-container/validation:leaf1",response.getErrors().get(0).getErrorPath());			
	}
	
	@Test
	public void testStringContainNoncharacterUnicode2() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
				+ " <validate-string-unicode-container>"
				+ "  <test-container>" 
				+ "  <leaf1>456&#x1FFFE;</leaf1>" // Non-character unicode - &#x1FFFE
				+ "  </test-container>"
				+ " </validate-string-unicode-container>" 
				+ "</validation>";
	
			NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
			assertEquals("String value contains unsupported noncharacter unicode: 456🿾", response.getErrors().get(0).getErrorMessage());
			assertEquals(NetconfRpcErrorType.Application, response.getErrors().get(0).getErrorType());
			assertEquals("/validation:validation/validation:validate-string-unicode-container/validation:test-container/validation:leaf1",response.getErrors().get(0).getErrorPath());			
	}
		
	@Test
	public void testCustomsubtypeStringPatternDefaultValue() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validate-customsubtype-string-defaultvalue xmlns=\"urn:org:bbf2:pma:validation\">"
				+ "  <test-container>" 
				+ "  </test-container>" 
				+ " </validate-customsubtype-string-defaultvalue>";

		editConfig(m_server, m_clientInfo, requestXml1, true);
		String ncResponse = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" 
				+ " <data>"
				+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
				+ "  <validation:validate-customsubtype-string-defaultvalue xmlns:validation=\"urn:org:bbf2:pma:validation\">"
				+ "    <validation:test-container>" 
				+ "       <validation:leaf1>hij</validation:leaf1>"
				+ "    </validation:test-container>" 
				+ "   </validation:validate-customsubtype-string-defaultvalue>"
				+ " </data>" 
				+ "</rpc-reply>";
		verifyGet(ncResponse);
	}

	@Test
	public void testCustomtypeStringPatternDefaultValue() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validate-customtype-string-defaultvalue xmlns=\"urn:org:bbf2:pma:validation\">"
				+ "  <test-container>" 
				+ "  </test-container>" 
				+ " </validate-customtype-string-defaultvalue>";

		editConfig(m_server, m_clientInfo, requestXml1, true);
		String ncResponse = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" 
				+ " <data>"
				+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
				+ "  <validation:validate-customtype-string-defaultvalue xmlns:validation=\"urn:org:bbf2:pma:validation\">"
				+ "    <validation:test-container>" 
				+ "       <validation:leaf1>xy</validation:leaf1>"
				+ "    </validation:test-container>" 
				+ "   </validation:validate-customtype-string-defaultvalue>"
				+ " </data>" 
				+ "</rpc-reply>";
		verifyGet(ncResponse);
	}

	@Test
	public void testCustomsubtypeStringPatternErrorcase() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validate-customsubtype-string-defaultvalue xmlns=\"urn:org:bbf2:pma:validation\">"
				+ "  <test-container>" 
				+ "   	<leaf1>xyz</leaf1>" 
				+ "  </test-container>"
				+ " </validate-customsubtype-string-defaultvalue>";

		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals("Provide pattern should between e to l", response.getErrors().get(0).getErrorMessage());
		assertEquals(NetconfRpcErrorType.Application, response.getErrors().get(0).getErrorType());
		assertEquals("Pattern does not match specified customsubtype constrain pattern", response.getErrors().get(0).getErrorAppTag());
		assertEquals("/validation:validate-customsubtype-string-defaultvalue/validation:test-container/validation:leaf1",response.getErrors().get(0).getErrorPath());
	}

	@Test
	public void testCustomtypeStringPatternErrorcase() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validate-customtype-string-defaultvalue xmlns=\"urn:org:bbf2:pma:validation\">"
				+ "  <test-container>" 
				+ "   	<leaf1>01</leaf1>" 
				+ "  </test-container>"
				+ " </validate-customtype-string-defaultvalue>";

		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals("Provide pattern should between a to z", response.getErrors().get(0).getErrorMessage());
		assertEquals(NetconfRpcErrorType.Application, response.getErrors().get(0).getErrorType());
		assertEquals("Pattern does not match specified customtype constrain pattern", response.getErrors().get(0).getErrorAppTag());
		assertEquals("/validation:validate-customtype-string-defaultvalue/validation:test-container/validation:leaf1",response.getErrors().get(0).getErrorPath());
	}    

	@Test
	public void testInvalidXml1() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
				+ " <validate-string-unicode-container>"
				+ "  <test-container>" 
				+ "  <leaf1>789</leaf1>>>" 
				+ "  </test-container>"
				+ " </validate-string-unicode-container>" 
				+ "</validation>";
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals("Invalid XML Syntax reported in an element: test-container",response.getErrors().get(0).getErrorMessage());
		assertEquals(NetconfRpcErrorType.Application, response.getErrors().get(0).getErrorType());
	}

	@Test
	public void testInvalidXml2() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
				+ "???</validation>";
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals("Invalid XML Syntax reported in an element: validation",response.getErrors().get(0).getErrorMessage());
		assertEquals(NetconfRpcErrorType.Application, response.getErrors().get(0).getErrorType());
	}

	@Test
	public void testvalidXml1withmultiplelinecomments() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
				+ " <validate-string-unicode-container>"
				+ "  <test-container>" 
				+ "  <leaf1>789</leaf1>" 
				+ "     <!--" 
				+ "          Multiple line comment"
				+ "          multi-line XML comment -->" 
				+ "  </test-container>"
				+ " </validate-string-unicode-container>" 
				+ "</validation>";
		editConfig(m_server, m_clientInfo, requestXml1, true);
		String ncResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" + " <data>"
				+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
				+ "   <validation:validate-string-unicode-container>" 
				+ "    <validation:test-container>"
				+ "       <validation:leaf1>789</validation:leaf1>" 
				+ "    </validation:test-container>"
				+ "   </validation:validate-string-unicode-container>" 
				+ "  </validation:validation>" 
				+ " </data>"
				+ "</rpc-reply>";
		verifyGet(ncResponse);
	}
	@Test
	public void testInvalidXml3() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation xmlns=\"urn:org:bbf2:pma:validation\">"
				+ " <validate-string-unicode-container>"
				+ "  <test-container>" 
				+ "  <leaf1>789</leaf1>"
				+ "  // "
				+ "  </test-container>"
				+ " </validate-string-unicode-container>" 
				+ "</validation>";
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals("Invalid XML Syntax reported in an element: test-container",response.getErrors().get(0).getErrorMessage());
		assertEquals(NetconfRpcErrorType.Application, response.getErrors().get(0).getErrorType());
	}

    @Test
    public void testEditConfigOperationRemoveAndDeleteOnLeaf() throws Exception{
        getModelNode();
        String requestXml1 =  "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                +"<container-leaf-with-default xmlns=\"urn:org:bbf2:pma:validation\">"
                +"  <test>leaf2</test>"
                +"  <decimal64-fraction1-with-default-value-type>2.4</decimal64-fraction1-with-default-value-type>"
                +"  <decimal64-fraction1-with-default-value-type2>1.8</decimal64-fraction1-with-default-value-type2>"
                +"</container-leaf-with-default>"
                ;
        editConfig(m_server, m_clientInfo, requestXml1, true);
        String response = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                +"<data>"
                +"<validation:container-leaf-with-default xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                +"<validation:decimal64-fraction1-with-default-value-type>2.4</validation:decimal64-fraction1-with-default-value-type>"
                +"<validation:decimal64-fraction1-with-default-value-type2>1.8</validation:decimal64-fraction1-with-default-value-type2>"
                +"<validation:test>leaf2</validation:test>"
                +"</validation:container-leaf-with-default>"
                +"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                +"</data>"
                +"</rpc-reply>"
                ;
        verifyGet(response);
        requestXml1 =  "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                +"<container-leaf-with-default xmlns=\"urn:org:bbf2:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                +"  <test xc:operation=\"remove\">leaf2</test>"
                +"  <decimal64-fraction1-with-default-value-type>2.4</decimal64-fraction1-with-default-value-type>"
                +"  <decimal64-fraction1-with-default-value-type2>1.8</decimal64-fraction1-with-default-value-type2>"
                +"</container-leaf-with-default>"
                ;
        editConfig(m_server, m_clientInfo, requestXml1, true);
        response = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                +"<data>"
                +"<validation:container-leaf-with-default xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                +"<validation:decimal64-fraction1-with-default-value-type>2.4</validation:decimal64-fraction1-with-default-value-type>"
                +"<validation:decimal64-fraction1-with-default-value-type2>1.8</validation:decimal64-fraction1-with-default-value-type2>"
                +"</validation:container-leaf-with-default>"
                +"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                +"</data>"
                +"</rpc-reply>"
                ;
        verifyGet(response);
        requestXml1 =  "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                +"<container-leaf-with-default xmlns=\"urn:org:bbf2:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                +"  <test xc:operation=\"remove\">leaf2</test>"
                +"  <decimal64-fraction1-with-default-value-type>2.4</decimal64-fraction1-with-default-value-type>"
                +"  <decimal64-fraction1-with-default-value-type2>1.8</decimal64-fraction1-with-default-value-type2>"
                +"</container-leaf-with-default>"
                ;
        NetConfResponse response1 = new NetConfResponse().setMessageId("1");
        EditConfigRequest request1 = createRequestFromString(requestXml1);
        request1.setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request1, response1);
        assertTrue(response1.isOk());
        requestXml1 =  "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                +"<container-leaf-with-default xmlns=\"urn:org:bbf2:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                +"  <test xc:operation=\"delete\">leaf2</test>"
                +"  <decimal64-fraction1-with-default-value-type>2.4</decimal64-fraction1-with-default-value-type>"
                +"  <decimal64-fraction1-with-default-value-type2>1.8</decimal64-fraction1-with-default-value-type2>"
                +"</container-leaf-with-default>"
                ;
        response1 = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertFalse(response1.isOk());
        assertEquals(NetconfRpcErrorType.Application, response1.getErrors().get(0).getErrorType());
        assertEquals(NetconfRpcErrorTag.DATA_MISSING, response1.getErrors().get(0).getErrorTag());
        assertEquals(NetconfRpcErrorSeverity.Error,   response1.getErrors().get(0).getErrorSeverity());
        assertEquals("Data does not exist test", response1.getErrors().get(0).getErrorMessage());
    }

    @Test
    public void testaccessPathWhenDefaultNotInList() throws Exception {
        getModelNode();
        SchemaPath currentSchemaPath = SchemaPathBuilder.fromString(
                "(urn:org:bbf2:pma:validation-yang11?revision=2015-12-14)validation-yang11,",
                "(urn:org:bbf2:pma:validation-yang11?revision=2015-12-14)one-with-augment,",
                "(urn:org:bbf2:pma:validation-yang11?revision=2015-12-14)dummy-leaf");
        ReferringNodes referringNodes = m_schemaRegistry.getReferringNodesForSchemaPath(currentSchemaPath);
        Map<SchemaPath, Set<ReferringNode>> refNodes = referringNodes.getReferringNodes();
        List<SchemaPath> schemaPaths = new ArrayList<SchemaPath>();
        for(Map.Entry<SchemaPath, Set<ReferringNode>> nodes : refNodes.entrySet()) {
            schemaPaths.add(nodes.getKey());
        }
        SchemaPath expectedSP1 = SchemaPathBuilder.fromString(
                "(urn:org:bbf2:pma:validation-yang11?revision=2015-12-14)validation-yang11,",
                "(urn:org:bbf2:pma:validation-yang11?revision=2015-12-14)one-with-augment,",
                "(urn:org:bbf2:pma:validation-yang11?revision=2015-12-14)testForDefaultInAugment,",
                "(urn:org:bbf2:pma:validation-yang11?revision=2015-12-14)test-container");

        SchemaPath expectedSP2 = SchemaPathBuilder.fromString(
                "(urn:org:bbf2:pma:validation-yang11?revision=2015-12-14)validation-yang11,",
                "(urn:org:bbf2:pma:validation-yang11?revision=2015-12-14)one-with-augment,",
                "(urn:org:bbf2:pma:validation-yang11?revision=2015-12-14)testForDefaultInAugment,",
                "(urn:org:bbf2:pma:validation-yang11?revision=2015-12-14)list-with-default-leaf2");

        SchemaPath expectedSP3 = SchemaPathBuilder.fromString(
                "(urn:org:bbf2:pma:validation-yang11?revision=2015-12-14)validation-yang11,",
                "(urn:org:bbf2:pma:validation-yang11?revision=2015-12-14)one-with-augment,",
                "(urn:org:bbf2:pma:validation-yang11?revision=2015-12-14)testForDefaultInAugment,",
                "(urn:org:bbf2:pma:validation-yang11?revision=2015-12-14)test-container,",
                "(urn:org:bbf2:pma:validation-yang11?revision=2015-12-14)leaf-with-default");

        SchemaPath expectedSP4 = SchemaPathBuilder.fromString(
                "(urn:org:bbf2:pma:validation-yang11?revision=2015-12-14)validation-yang11,",
                "(urn:org:bbf2:pma:validation-yang11?revision=2015-12-14)one-with-augment,",
                "(urn:org:bbf2:pma:validation-yang11?revision=2015-12-14)testForDefaultInAugment,",
                "(urn:org:bbf2:pma:validation-yang11?revision=2015-12-14)test-container2");

        Map<SchemaPath, String> expectedValueMap = new HashMap<SchemaPath,String>();
        expectedValueMap.put(expectedSP1, "validation11:dummy-leaf/validation11:one-with-augment/validation11:testForDefaultInAugment/validation11:test-container");
        expectedValueMap.put(expectedSP2, "validation11:dummy-leaf/validation11:one-with-augment/validation11:testForDefaultInAugment/validation11:list-with-default-leaf2");
        expectedValueMap.put(expectedSP3, "validation11:dummy-leaf/validation11:one-with-augment/validation11:testForDefaultInAugment/validation11:test-container/validation11:leaf-with-default");
        expectedValueMap.put(expectedSP4, "validation11:dummy-leaf/validation11:one-with-augment/validation11:testForDefaultInAugment/validation11:test-container2");

        for(SchemaPath sp : schemaPaths ) {
            Set<ReferringNode> referringNode = refNodes.get(sp);
            for(ReferringNode nodes : referringNode) {
                if(sp.equals(nodes.getReferringSP())) {
                    assertEquals(expectedValueMap.get(sp), nodes.getReferringNodeAP().toString());
                }
            }
        }
    }

    @Test
    public void testImpactValidation1() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">"
                + " <one-with-augment>"
                + "    <id>one</id>"
                + "    <testForDefaultInAugment>"
                + "        <test-container></test-container>"
                + "        <list-with-default-leaf2>"
                + "             <key1>one</key1>"
                + "             <another-leaf>must</another-leaf>"
                + "        </list-with-default-leaf2>"
                + "        <test-container2>"
                + "             <list-with-default-leaf3>"
                + "                   <key2>two</key2>"
                + "                   <leaf-with-default3>value</leaf-with-default3>"
                + "             </list-with-default-leaf3>"
                + "        </test-container2>"
                + "	   </testForDefaultInAugment>"
                + "    <dummy-leaf>dummy</dummy-leaf>"
                + " </one-with-augment>"
                + "</validation-yang11>";
        NetConfResponse response =  editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals("/validation11:validation-yang11/validation11:one-with-augment[validation11:id='one']/validation11:testForDefaultInAugment/validation11:test-container2/validation11:list-with-default-leaf3[validation11:key2='two']/validation11:leaf-with-default3", response.getErrors().get(0).getErrorPath());
        assertEquals("Violate must constraints: ../../../list-with-default-leaf2/another-leaf = 'mustleaf'", response.getErrors().get(0).getErrorMessage());
    }

	@Test
	public void testImpactValidation2() throws Exception {
		getModelNode();

		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">"
				+ " <one-with-augment>"
				+ "    <id>one</id>"
				+ "    <testForDefaultInAugment>"
				+ "        <test-container></test-container>"
				+ "        <list-with-default-leaf2>"
				+ "             <key1>one</key1>"
				+ "             <another-leaf>mustleaf</another-leaf>"
				+ "        </list-with-default-leaf2>"
				+ "        <test-container2>"
				+ "             <list-with-default-leaf3>"
				+ "                   <key2>two</key2>"
				+ "                   <leaf-with-default3>value</leaf-with-default3>"
				+ "             </list-with-default-leaf3>"
				+ "        </test-container2>"
				+ "	   </testForDefaultInAugment>"
				+ "    <dummy-leaf>yes</dummy-leaf>"
				+ " </one-with-augment>"
				+ "</validation-yang11>";
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
//		assertEquals("/validation11:validation-yang11/validation11:one-with-augment[validation11:id='one']"
//				+ "/validation11:testForDefaultInAugment/validation11:test-container", response.getErrors().get(0).getErrorPath());
//		assertEquals("Violate when constraints: ../validation11:dummy-leaf = 'dummy'", response.getErrors().get(0).getErrorMessage());
		assertEquals("/validation11:validation-yang11/validation11:one-with-augment[validation11:id='one']"
				+ "/validation11:testForDefaultInAugment/validation11:test-container", response.getErrors().get(0).getErrorPath());
		assertEquals("Violate when constraints: ../validation11:dummy-leaf = 'dummy'", response.getErrors().get(0).getErrorMessage());
	}

	@Test
	public void testImpactValidation3() throws Exception {
		getModelNode();

		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">"
				+ " <one-with-augment>"
				+ "    <id>one</id>"
				+ "    <testForDefaultInAugment>"
				+ "        <test-container></test-container>"
				+ "        <list-with-default-leaf2>"
				+ "             <key1>one</key1>"
				+ "             <another-leaf>mustleaf</another-leaf>"
				+ "        </list-with-default-leaf2>"
				+ "        <test-container2>"
				+ "             <list-with-default-leaf3>"
				+ "                   <key2>two</key2>"
				+ "                   <leaf-with-default3>value</leaf-with-default3>"
				+ "             </list-with-default-leaf3>"
				+ "        </test-container2>"
				+ "	   </testForDefaultInAugment>"
				+ "    <dummy-leaf>dummy</dummy-leaf>"
				+ " </one-with-augment>"
				+ "</validation-yang11>";
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, true);
		assertTrue(response.isOk());

		String expectedOutput = "<rpc-reply message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
				+"  <data>"
				+"    <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
				+"    <validation11:validation-yang11 xmlns:validation11=\"urn:org:bbf2:pma:validation-yang11\">"
				+ "   <validation11:container-with-must>"
				+ "     <validation11:remove-mac-addresses-from-port-down>false</validation11:remove-mac-addresses-from-port-down>"
				+ "   </validation11:container-with-must>"
				+"      <validation11:one-with-augment>"
				+"        <validation11:id>one</validation11:id>"
				+"        <validation11:dummy-leaf>dummy</validation11:dummy-leaf>"
				+"        <validation11:testForDefaultInAugment>"
				+"          <validation11:list-with-default-leaf2>"
				+"            <validation11:key1>one</validation11:key1>"
				+"            <validation11:another-leaf>mustleaf</validation11:another-leaf>"
				+"            <validation11:leaf-with-default2>defaultLeaf2</validation11:leaf-with-default2>"
				+"          </validation11:list-with-default-leaf2>"
				+"          <validation11:test-container>"
				+"            <validation11:leaf-with-default>defaultLeaf1</validation11:leaf-with-default>"
				+"          </validation11:test-container>"
				+"          <validation11:test-container2>"
				+"            <validation11:list-with-default-leaf3>"
				+"               <validation11:key2>two</validation11:key2>"
				+"               <validation11:leaf-with-default3>value</validation11:leaf-with-default3>"
				+"            </validation11:list-with-default-leaf3>"
				+"          </validation11:test-container2>"
				+"        </validation11:testForDefaultInAugment>"
				+"      </validation11:one-with-augment>"
				+"    </validation11:validation-yang11>"
				+"  </data>"
				+"</rpc-reply>";

		verifyGet(expectedOutput);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">"
				+ " <one-with-augment>"
				+ "    <id>one</id>"
				+ "    <testForDefaultInAugment>"
				+ "        <list-with-default-leaf2>"
				+ "             <key1>one</key1>"
				+ "             <another-leaf xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" xc:operation=\"remove\">mustleaf</another-leaf>"
				+ "        </list-with-default-leaf2>"
				+ "    </testForDefaultInAugment>"
				+ " </one-with-augment>"
				+ "</validation-yang11>";
		response =  editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals("/validation11:validation-yang11/validation11:one-with-augment[validation11:id='one']/validation11:testForDefaultInAugment/validation11:test-container2/validation11:list-with-default-leaf3[validation11:key2='two']/validation11:leaf-with-default3", response.getErrors().get(0).getErrorPath());
		assertEquals("Violate must constraints: ../../../list-with-default-leaf2/another-leaf = 'mustleaf'", response.getErrors().get(0).getErrorMessage());
	}

	@Test
	public void testAnyDefaultValuesInTheKeyLeafsAreIgnored() throws Exception {
		getModelNode();
		initialiseInterceptor();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<exfunctionCanister xmlns=\"urn:opendaylight:datastore-extension-functions-test\">\n" +
				"    <configure >\n" +
				"        <qos-servicerouter>\n" +
				"            <service-ingress>\n" +
				"                <service-ingress-policy-id>2</service-ingress-policy-id>\n" +
				"            </service-ingress>\n" +
				"        </qos-servicerouter>\n" +
				"    </configure>\n" +
				"</exfunctionCanister>";

		editConfig(m_server, m_clientInfo, requestXml1, true);

		String expectedOutput = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
				"<data>\n" +
				"    <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n" +
				"    <ds-ext-func:exfunctionCanister xmlns:ds-ext-func=\"urn:opendaylight:datastore-extension-functions-test\">\n" +
				"        <ds-ext-func:configure>\n" +
				"            <ds-ext-func:qos-servicerouter>\n" +
				"                <ds-ext-func:service-ingress>\n" +
				"                    <ds-ext-func:service-ingress-policy-id>2</ds-ext-func:service-ingress-policy-id>\n" +
				"                </ds-ext-func:service-ingress>\n" +
				"            </ds-ext-func:qos-servicerouter>\n" +
				"        </ds-ext-func:configure>\n" +
				"    </ds-ext-func:exfunctionCanister>\n" +
				"</data>\n" +
				"</rpc-reply>"
				;
		verifyGet(expectedOutput);

    }

    @After
    public void teardown() {
        m_dataStore.disableUTSupport();
        m_datastoreValidator.setValidatedChildCacheHitStatus(false);
   }
}


