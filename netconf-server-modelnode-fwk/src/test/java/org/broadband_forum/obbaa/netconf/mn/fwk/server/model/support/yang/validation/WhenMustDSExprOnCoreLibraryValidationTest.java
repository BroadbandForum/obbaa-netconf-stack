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
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(RequestScopeJunitRunner.class)
public class WhenMustDSExprOnCoreLibraryValidationTest extends AbstractDataStoreValidatorTest {

    @Test
    public void testCombinedWhenExpressions() throws Exception {
        getModelNode();
                
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + "    <combined-when-expressions-list>"
                + "      <name>test1</name>"
                + "      <combined-when-expressions-container>"
                + "        <when-combined-leaf1>fail</when-combined-leaf1>"
                + "       </combined-when-expressions-container>"
                + "    </combined-when-expressions-list>"
                + "  </whenMustContainer>";
        
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
        NetconfRpcError error = response.getErrors().get(0);
        assertEquals("when-violation", error.getErrorAppTag());
        assertEquals("/whenMust:whenMustContainer/whenMust:combined-when-expressions-list[whenMust:name='test1']/whenMust:combined-when-expressions-container/whenMust:when-combined-leaf1", error.getErrorPath());
        assertEquals("Violate when constraints: ../hardware-type[. != 'FX-I' and . != '']", error.getErrorMessage());
        
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + "    <combined-when-expressions-list>"
                + "      <name>test1</name>"
                + "      <combined-when-expressions-container>"
                + "        <hardware-type>FX-I</hardware-type>"
                + "        <when-combined-leaf1>fail</when-combined-leaf1>"
                + "      </combined-when-expressions-container>"
                + "    </combined-when-expressions-list>"
                + "  </whenMustContainer>";
        
        response = editConfig(m_server, m_clientInfo, requestXml1, false);
        error = response.getErrors().get(0);
        assertEquals("when-violation", error.getErrorAppTag());
        assertEquals("/whenMust:whenMustContainer/whenMust:combined-when-expressions-list[whenMust:name='test1']/whenMust:combined-when-expressions-container/whenMust:when-combined-leaf1", error.getErrorPath());
        assertEquals("Violate when constraints: ../hardware-type[. != 'FX-I' and . != '']", error.getErrorMessage());
        
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + "    <combined-when-expressions-list>"
                + "      <name>test1</name>"
                + "      <combined-when-expressions-container>"
                + "        <hardware-type>FX-II</hardware-type>"
                + "        <when-combined-leaf1>pass</when-combined-leaf1>"
                + "        <dummy-leaf>someValue</dummy-leaf>"
                + "      </combined-when-expressions-container>"
                + "    </combined-when-expressions-list>"
                + "  </whenMustContainer>";
        
        editConfig(m_server, m_clientInfo, requestXml1, true);
        
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + "    <combined-when-expressions-list>"
                + "      <name>test2</name>"
                + "      <combined-when-expressions-container>"
                + "        <hardware-type>FX-II</hardware-type>"
                + "        <when-combined-leaf2>test</when-combined-leaf2>"
                + "      </combined-when-expressions-container>"
                + "    </combined-when-expressions-list>"
                + "  </whenMustContainer>";
        editConfig(m_server, m_clientInfo, requestXml1, true);
        
        String responsexml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "<whenMust:whenMustContainer xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + "  <whenMust:combined-when-expressions-list>"
                + "    <whenMust:combined-when-expressions-container>"
                + "      <whenMust:hardware-type>FX-II</whenMust:hardware-type>"
                + "      <whenMust:when-combined-leaf1>pass</whenMust:when-combined-leaf1>"
                + "      <whenMust:dummy-leaf>someValue</whenMust:dummy-leaf>"
                + "    </whenMust:combined-when-expressions-container>"
                + "    <whenMust:name>test1</whenMust:name>"
                + "  </whenMust:combined-when-expressions-list>"
                + "  <whenMust:combined-when-expressions-list>"
                + "    <whenMust:combined-when-expressions-container>"
                + "      <whenMust:hardware-type>FX-II</whenMust:hardware-type>"
                + "      <whenMust:when-combined-leaf2>test</whenMust:when-combined-leaf2>"
                + "    </whenMust:combined-when-expressions-container>"
                + "    <whenMust:name>test2</whenMust:name>"
                + "  </whenMust:combined-when-expressions-list>"
                + "</whenMust:whenMustContainer>"
                + "</data>"
                + "</rpc-reply>";                
        
        verifyGet(responsexml);
        
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + "    <combined-when-expressions-list>"
                + "      <name>test2</name>"
                + "      <combined-when-expressions-container>"
                + "        <hardware-type>FX-II</hardware-type>"
                + "        <when-combined-leaf2>someValue</when-combined-leaf2>"
                + "      </combined-when-expressions-container>"
                + "    </combined-when-expressions-list>"
                + "  </whenMustContainer>";
        editConfig(m_server, m_clientInfo, requestXml1, true);
        
        responsexml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "<whenMust:whenMustContainer xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + "  <whenMust:combined-when-expressions-list>"
                + "    <whenMust:combined-when-expressions-container>"
                + "      <whenMust:hardware-type>FX-II</whenMust:hardware-type>"
                + "      <whenMust:when-combined-leaf1>pass</whenMust:when-combined-leaf1>"
                + "      <whenMust:dummy-leaf>someValue</whenMust:dummy-leaf>"
                + "    </whenMust:combined-when-expressions-container>"
                + "    <whenMust:name>test1</whenMust:name>"
                + "  </whenMust:combined-when-expressions-list>"
                + "  <whenMust:combined-when-expressions-list>"
                + "    <whenMust:combined-when-expressions-container>"
                + "      <whenMust:hardware-type>FX-II</whenMust:hardware-type>"
                + "      <whenMust:when-combined-leaf2>someValue</whenMust:when-combined-leaf2>"
                + "    </whenMust:combined-when-expressions-container>"
                + "    <whenMust:name>test2</whenMust:name>"
                + "  </whenMust:combined-when-expressions-list>"
                + "</whenMust:whenMustContainer>"
                + "</data>"
                + "</rpc-reply>";                
        
        verifyGet(responsexml);
        
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + "    <combined-when-expressions-list>"
                + "      <name>test1</name>"
                + "      <combined-when-expressions-container>"
                + "        <hardware-type xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>"
                + "      </combined-when-expressions-container>"
                + "    </combined-when-expressions-list>"
                + "  </whenMustContainer>";
        
        editConfig(m_server, m_clientInfo, requestXml1, true);
        
        responsexml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "<whenMust:whenMustContainer xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + "  <whenMust:combined-when-expressions-list>"
                + "    <whenMust:combined-when-expressions-container>"
                + "      <whenMust:dummy-leaf>someValue</whenMust:dummy-leaf>"
                + "    </whenMust:combined-when-expressions-container>"
                + "    <whenMust:name>test1</whenMust:name>"
                + "  </whenMust:combined-when-expressions-list>"
                + "  <whenMust:combined-when-expressions-list>"
                + "    <whenMust:combined-when-expressions-container>"
                + "      <whenMust:hardware-type>FX-II</whenMust:hardware-type>"
                + "      <whenMust:when-combined-leaf2>someValue</whenMust:when-combined-leaf2>"
                + "    </whenMust:combined-when-expressions-container>"
                + "    <whenMust:name>test2</whenMust:name>"
                + "  </whenMust:combined-when-expressions-list>"
                + "</whenMust:whenMustContainer>"
                + "</data>"
                + "</rpc-reply>";                
        
        verifyGet(responsexml);
        
    }
    
    @Test
    public void testNestedCombinedWhenExpressions() throws Exception {
        getModelNode();
        
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + "    <combined-when-expressions-list>"
                + "      <name>test1</name>"
                + "      <inner-list>"
                + "        <name>test</name>"
                + "        <inner-container>"
                + "          <test-leaf>test3</test-leaf>"
                + "        </inner-container>"
                + "      </inner-list>"
                + "      <combined-when-expressions-container>"
                + "        <combined-path-with-inner-path-leaf>fail</combined-path-with-inner-path-leaf>"
                + "      </combined-when-expressions-container>"
                + "    </combined-when-expressions-list>"
                + "  </whenMustContainer>";
        
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
        NetconfRpcError error = response.getErrors().get(0);
        assertEquals("when-violation", error.getErrorAppTag());
        assertEquals("/whenMust:whenMustContainer/whenMust:combined-when-expressions-list[whenMust:name='test1']/whenMust:combined-when-expressions-container/whenMust:combined-path-with-inner-path-leaf", error.getErrorPath());
        assertEquals("Violate when constraints: ../../inner-list[name = 'test' and inner-container/test-leaf[. = 'test' or . = 'test2']]", error.getErrorMessage());
        
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + "    <combined-when-expressions-list>"
                + "      <name>test1</name>"
                + "      <inner-list>"
                + "        <name>test</name>"
                + "        <inner-container>"
                + "          <test-leaf>test</test-leaf>"
                + "        </inner-container>"
                + "      </inner-list>"
                + "      <combined-when-expressions-container>"
                + "        <combined-path-with-inner-path-leaf>pass</combined-path-with-inner-path-leaf>"
                + "      </combined-when-expressions-container>"
                + "    </combined-when-expressions-list>"
                + "  </whenMustContainer>";
        
        editConfig(m_server, m_clientInfo, requestXml1, true);
        
        String responsexml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "<whenMust:whenMustContainer xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + "  <whenMust:combined-when-expressions-list>"
                + "    <whenMust:inner-list>"
                + "      <whenMust:inner-container>"
                + "        <whenMust:test-leaf>test</whenMust:test-leaf>"
                + "      </whenMust:inner-container>"
                + "      <whenMust:name>test</whenMust:name>"
                + "    </whenMust:inner-list>"
                + "    <whenMust:combined-when-expressions-container>"
                + "      <whenMust:combined-path-with-inner-path-leaf>pass</whenMust:combined-path-with-inner-path-leaf>"
                + "    </whenMust:combined-when-expressions-container>"
                + "    <whenMust:name>test1</whenMust:name>"
                + "  </whenMust:combined-when-expressions-list>"
                + "</whenMust:whenMustContainer>"
                + "</data>"
                + "</rpc-reply>";                
        
        verifyGet(responsexml);
        
    }
    
    @Test
    public void testCombinedWhenExpressionWithRelativePath() throws Exception {
        getModelNode();
        
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + "    <combined-when-expressions-list>"
                + "      <name>test1</name>"
                + "      <inner-list>"
                + "        <name>test</name>"
                + "        <inner-container>"
                + "        </inner-container>"
                + "      </inner-list>"
                + "      <combined-when-expressions-container>"
                + "        <self-axis-with-current>test</self-axis-with-current>"
                + "        <hardware-type>FX-II</hardware-type>"
                + "      </combined-when-expressions-container>"
                + "    </combined-when-expressions-list>"
                + "  </whenMustContainer>";
        
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
        NetconfRpcError error = response.getErrors().get(0);
        assertEquals("when-violation", error.getErrorAppTag());
        assertEquals("/whenMust:whenMustContainer/whenMust:combined-when-expressions-list[whenMust:name='test1']/whenMust:combined-when-expressions-container/whenMust:self-axis-with-current", error.getErrorPath());
        assertEquals("Violate when constraints: ../../inner-list[name = current() and inner-container/test-leaf[. = current()/../hardware-type and . != '' or . = 'someValue']]", error.getErrorMessage());
        
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + "    <combined-when-expressions-list>"
                + "      <name>test1</name>"
                + "      <inner-list>"
                + "        <name>test</name>"
                + "        <inner-container>"
                + "          <test-leaf>FX-II</test-leaf>"
                + "        </inner-container>"
                + "      </inner-list>"
                + "      <combined-when-expressions-container>"
                + "        <hardware-type>FX-I</hardware-type>"
                + "        <self-axis-with-current>test</self-axis-with-current>"
                + "      </combined-when-expressions-container>"
                + "    </combined-when-expressions-list>"
                + "  </whenMustContainer>";
        
        response = editConfig(m_server, m_clientInfo, requestXml1, false);
        error = response.getErrors().get(0);
        assertEquals("when-violation", error.getErrorAppTag());
        assertEquals("/whenMust:whenMustContainer/whenMust:combined-when-expressions-list[whenMust:name='test1']/whenMust:combined-when-expressions-container/whenMust:self-axis-with-current", error.getErrorPath());
        assertEquals("Violate when constraints: ../../inner-list[name = current() and inner-container/test-leaf[. = current()/../hardware-type and . != '' or . = 'someValue']]", error.getErrorMessage());
        
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + "    <combined-when-expressions-list>"
                + "      <name>test1</name>"
                + "      <inner-list>"
                + "        <name>test</name>"
                + "        <inner-container>"
                + "          <test-leaf>FX-II</test-leaf>"
                + "        </inner-container>"
                + "      </inner-list>"
                + "      <combined-when-expressions-container>"
                + "        <hardware-type>FX-II</hardware-type>"
                + "        <self-axis-with-current>test</self-axis-with-current>"
                + "      </combined-when-expressions-container>"
                + "    </combined-when-expressions-list>"
                + "  </whenMustContainer>";
        
        editConfig(m_server, m_clientInfo, requestXml1, true);
        
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + "    <combined-when-expressions-list>"
                + "      <name>test1</name>"
                + "      <inner-list>"
                + "        <name>test</name>"
                + "        <inner-container>"
                + "          <test-leaf>someValue</test-leaf>"
                + "        </inner-container>"
                + "      </inner-list>"
                + "      <combined-when-expressions-container>"
                + "        <self-axis-with-current>test</self-axis-with-current>"
                + "      </combined-when-expressions-container>"
                + "    </combined-when-expressions-list>"
                + "  </whenMustContainer>";
        
        editConfig(m_server, m_clientInfo, requestXml1, true);
        
        String responsexml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "<whenMust:whenMustContainer xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + "  <whenMust:combined-when-expressions-list>"
                + "    <whenMust:inner-list>"
                + "      <whenMust:inner-container>"
                + "        <whenMust:test-leaf>someValue</whenMust:test-leaf>"
                + "      </whenMust:inner-container>"
                + "      <whenMust:name>test</whenMust:name>"
                + "    </whenMust:inner-list>"
                + "    <whenMust:combined-when-expressions-container>"
                + "      <whenMust:hardware-type>FX-II</whenMust:hardware-type>"
                + "      <whenMust:self-axis-with-current>test</whenMust:self-axis-with-current>"
                + "    </whenMust:combined-when-expressions-container>"
                + "    <whenMust:name>test1</whenMust:name>"
                + "  </whenMust:combined-when-expressions-list>"
                + "</whenMust:whenMustContainer>"
                + "</data>"
                + "</rpc-reply>";                
        
        verifyGet(responsexml);
    }
    
    @Test
    public void testCombinedWhenExpressionWithAbsolutePath() throws Exception {
        getModelNode();
        
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + "    <combined-when-expressions-list>"
                + "      <name>test1</name>"
                + "      <inner-list>"
                + "        <name>test</name>"
                + "        <inner-container>"
                + "          <test-leaf>someValue</test-leaf>"
                + "        </inner-container>"
                + "      </inner-list>"
                + "      <combined-when-expressions-container>"
                + "        <self-axis-with-absolute-path>test</self-axis-with-absolute-path>"
                + "        <hardware-type>FX-II</hardware-type>"
                + "      </combined-when-expressions-container>"
                + "    </combined-when-expressions-list>"
                + "  </whenMustContainer>";
        
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
        NetconfRpcError error = response.getErrors().get(0);
        assertEquals("when-violation", error.getErrorAppTag());
        assertEquals("/whenMust:whenMustContainer/whenMust:combined-when-expressions-list[whenMust:name='test1']/whenMust:combined-when-expressions-container/whenMust:self-axis-with-absolute-path", error.getErrorPath());
        assertEquals("Violate when constraints: ../../inner-list[name = 'test' and inner-container/test-leaf[. = /whenMust:whenMustContainer/whenMust:combined-when-expressions-list/whenMust:combined-when-expressions-container/whenMust:hardware-type and . != '']]", error.getErrorMessage());
        
        
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + "    <combined-when-expressions-list>"
                + "      <name>test1</name>"
                + "      <inner-list>"
                + "        <name>test</name>"
                + "        <inner-container>"
                + "          <test-leaf>someValue</test-leaf>"
                + "        </inner-container>"
                + "      </inner-list>"
                + "      <combined-when-expressions-container>"
                + "        <self-axis-with-absolute-path>test</self-axis-with-absolute-path>"
                + "        <hardware-type>someValue</hardware-type>"
                + "      </combined-when-expressions-container>"
                + "    </combined-when-expressions-list>"
                + "  </whenMustContainer>";
        
        editConfig(m_server, m_clientInfo, requestXml1, true);
        
        String responsexml = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "<whenMust:whenMustContainer xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + "  <whenMust:combined-when-expressions-list>"
                + "    <whenMust:inner-list>"
                + "      <whenMust:inner-container>"
                + "        <whenMust:test-leaf>someValue</whenMust:test-leaf>"
                + "      </whenMust:inner-container>"
                + "      <whenMust:name>test</whenMust:name>"
                + "    </whenMust:inner-list>"
                + "    <whenMust:combined-when-expressions-container>"
                + "      <whenMust:hardware-type>someValue</whenMust:hardware-type>"
                + "      <whenMust:self-axis-with-absolute-path>test</whenMust:self-axis-with-absolute-path>"
                + "    </whenMust:combined-when-expressions-container>"
                + "    <whenMust:name>test1</whenMust:name>"
                + "  </whenMust:combined-when-expressions-list>"
                + "</whenMust:whenMustContainer>"
                + "</data>"
                + "</rpc-reply>";                
        
        verifyGet(responsexml);
        
    }   
    
	@Test public void testMustWithNotAndBoolean() throws Exception {
		getModelNode();

		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
							 + "  <interfaces xmlns=\"test-interfaces\">"
							 + "    <interface>"
							 + "     <name>testInterface</name>"
							 + "     <type>ieee80211</type>"
							 + "     <fwd:interface-usage xmlns:fwd=\"test-forwarders\">"
							 + "     <fwd:interface-usage>network-port</fwd:interface-usage>"
							 + "     </fwd:interface-usage>"
							 + "    </interface>"
							 + "    <interface>"
							 + "     <name>testInterface2</name>"
							 + "     <type>ieee80211</type>"
							 + "     <fwd:interface-usage xmlns:fwd=\"test-forwarders\">"
							 + "     <fwd:interface-usage>network-port</fwd:interface-usage>"
							 + "     </fwd:interface-usage>"
							 + "    </interface>"
							 + "  </interfaces>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
					  + "<forwarders xmlns=\"test-forwarders\">"
					  + "<forwarder>"
					  + "<name>test</name>"
					  + "<if-name>sample</if-name>"
					  + "<ports>"
					  + "<port>"
					  + "<name>dummy</name>"
					  + "<sub-interface>testInterface</sub-interface>"
					  + "</port>"
					  + "<port>"
					  + "<name>dummy2</name>"
					  + "<sub-interface>testInterface2</sub-interface>"
					  + "</port>"
					  + "</ports>"
					  + "</forwarder>"
					  + "</forwarders>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		String response = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						  + " <data>"
						  + "  <bbf-l2-fwd:forwarders xmlns:bbf-l2-fwd=\"test-forwarders\">"
						  + "    <bbf-l2-fwd:forwarder>"
						  + "      <bbf-l2-fwd:name>test</bbf-l2-fwd:name>"
						  + "      <bbf-l2-fwd:if-name>sample</bbf-l2-fwd:if-name>"
						  + "      <bbf-l2-fwd:ports>"
						  + "        <bbf-l2-fwd:port>"
						  + "          <bbf-l2-fwd:name>dummy</bbf-l2-fwd:name>"
						  + "          <bbf-l2-fwd:sub-interface>testInterface</bbf-l2-fwd:sub-interface>"
						  + "        </bbf-l2-fwd:port>"
						  + "        <bbf-l2-fwd:port>"
						  + "          <bbf-l2-fwd:name>dummy2</bbf-l2-fwd:name>"
						  + "          <bbf-l2-fwd:sub-interface>testInterface2</bbf-l2-fwd:sub-interface>"
						  + "        </bbf-l2-fwd:port>"
						  + "      </bbf-l2-fwd:ports>"
						  + "    </bbf-l2-fwd:forwarder>"
						  + "  </bbf-l2-fwd:forwarders>"
						  + "  <if:interfaces xmlns:if=\"test-interfaces\">"
						  + "    <if:interface>"
						  + "      <bbf-l2-fwd:interface-usage xmlns:bbf-l2-fwd=\"test-forwarders\">"
						  + "        <bbf-l2-fwd:interface-usage>network-port</bbf-l2-fwd:interface-usage>"
						  + "      </bbf-l2-fwd:interface-usage>"
						  + "          <if:mybits>fourthBit</if:mybits>\n" 
						  + "           <if:mybitsdefaultValue>thirdBit</if:mybitsdefaultValue>\n"
						  + "           <if:mybitsoverridden>fourthBit</if:mybitsoverridden>\n"
						  + "      <if:name>testInterface</if:name>"
						  + "      <if:type>if:ieee80211</if:type>"
						  + "    </if:interface>"
						  + "    <if:interface>"
						  + "      <bbf-l2-fwd:interface-usage xmlns:bbf-l2-fwd=\"test-forwarders\">"
						  + "        <bbf-l2-fwd:interface-usage>network-port</bbf-l2-fwd:interface-usage>"
						  + "      </bbf-l2-fwd:interface-usage>"
						  + "          <if:mybits>fourthBit</if:mybits>\n"  
                          + "           <if:mybitsdefaultValue>thirdBit</if:mybitsdefaultValue>\n"  
                          + "           <if:mybitsoverridden>fourthBit</if:mybitsoverridden>\n"
						  + "      <if:name>testInterface2</if:name>"
						  + "      <if:type>if:ieee80211</if:type>"
						  + "    </if:interface>"
						  + "  </if:interfaces>"
						  + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						  + " </data>"
						  + " </rpc-reply>";
		verifyGet(response);
	}
	
	@Test public void testMustWithNotAndBooleanSingleInstance() throws Exception {
        getModelNode();

        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                             + "  <interfaces xmlns=\"test-interfaces\">"
                             + "    <interface>"
                             + "     <name>testInterface</name>"
                             + "     <type>ieee80211</type>"
                             + "     <fwd:interface-usage xmlns:fwd=\"test-forwarders\">"
                             + "     <fwd:interface-usage>network-port</fwd:interface-usage>"
                             + "     </fwd:interface-usage>"
                             + "    </interface>"
                             + "  </interfaces>";
        editConfig(m_server, m_clientInfo, requestXml1, true);

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                      + "<forwarders xmlns=\"test-forwarders\">"
                      + "<forwarder>"
                      + "<name>test</name>"
                      + "<if-name>sample</if-name>"
                      + "<ports>"
                      + "<port>"
                      + "<name>test</name>"
                      + "<sub-interface>testInterface</sub-interface>"
                      + "<leaf1>network-port</leaf1>"
                      + "<leaf2>dummy</leaf2>"
                      + "</port>"
                      + "</ports>"
                      + "</forwarder>"
                      + "</forwarders>";
        editConfig(m_server, m_clientInfo, requestXml1, true);

	}

	
	@Test public void testExpressionListWithoutKeys() throws Exception {
	    /**
	     * one interface-usage is network-port and another is user-port. So it should fail.  
	     */
	    getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                             + "  <interfaces xmlns=\"test-interfaces\">"
                             + "    <interface>"
                             + "     <name>testInterface</name>"
                             + "     <type>ieee80211</type>"
                             + "     <fwd:interface-usage xmlns:fwd=\"test-forwarders\">"
                             + "     <fwd:interface-usage>network-port</fwd:interface-usage>"
                             + "     </fwd:interface-usage>"
                             + "    </interface>"
                             + "    <interface>"
                             + "     <name>testInterface2</name>"
                             + "     <type>ieee80211</type>"
                             + "     <fwd:interface-usage xmlns:fwd=\"test-forwarders\">"
                             + "     <fwd:interface-usage>user-port</fwd:interface-usage>"
                             + "     </fwd:interface-usage>"
                             + "    </interface>"
                             + "  </interfaces>";
        editConfig(m_server, m_clientInfo, requestXml1, true);

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                      + "<forwarders xmlns=\"test-forwarders\">"
                      + "<forwarder>"
                      + "<name>test</name>"
                      + "<if-name>sample</if-name>"
                      + "<ports>"
                      + "<port>"
                      + "<name>dummy</name>"
                      + "<sub-interface>testInterface</sub-interface>"
                      + "</port>"
                      + "<port>"
                      + "<name>dummy2</name>"
                      + "<sub-interface>testInterface2</sub-interface>"
                      + "</port>"
                      + "</ports>"
                      + "</forwarder>"
                      + "</forwarders>";
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
        NetconfRpcError error = response.getErrors().get(0);
        assertEquals("must-violation", error.getErrorAppTag());
        assertEquals("/bbf-l2-fwd:forwarders/bbf-l2-fwd:forwarder[bbf-l2-fwd:name='test']", error.getErrorPath());
        assertEquals("Violate must constraints: not(boolean(/if:interfaces/if:interface[if:name = current()/bbf-l2-fwd:ports/bbf-l2-fwd:port/bbf-l2-fwd:sub-interface and bbf-if-usg:interface-usage/bbf-if-usg:interface-usage = 'user-port']))", error.getErrorMessage());
    }
	
	@Test public void testExpressionListWithoutKeysCase2() throws Exception {
	    /**
	     * With two forwarders and two ports
	     */
	    getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                             + "  <interfaces xmlns=\"test-interfaces\">"
                             + "    <interface>"
                             + "     <name>testInterface</name>"
                             + "     <type>ieee80211</type>"
                             + "     <fwd:interface-usage xmlns:fwd=\"test-forwarders\">"
                             + "     <fwd:interface-usage>network-port</fwd:interface-usage>"
                             + "     </fwd:interface-usage>"
                             + "    </interface>"
                             + "    <interface>"
                             + "     <name>testInterface2</name>"
                             + "     <type>ieee80211</type>"
                             + "     <fwd:interface-usage xmlns:fwd=\"test-forwarders\">"
                             + "     <fwd:interface-usage>network-port</fwd:interface-usage>"
                             + "     </fwd:interface-usage>"
                             + "    </interface>"
                             + "  </interfaces>";
        editConfig(m_server, m_clientInfo, requestXml1, true);

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                      + "<forwarders xmlns=\"test-forwarders\">"
                      + "<forwarder>"
                      + "<name>test1</name>"
                      + "<if-name>sample</if-name>"
                      + "<ports>"
                      + "<port>"
                      + "<name>dummy</name>"
                      + "<sub-interface>testInterface</sub-interface>"
                      + "</port>"
                      + "</ports>"
                      + "</forwarder>"                      
                      + "<forwarder>"
                      + "<name>test2</name>"
                      + "<if-name>sample</if-name>"
                      + "<ports>"
                      + "<port>"
                      + "<name>dummy</name>"
                      + "<sub-interface>testInterface</sub-interface>"
                      + "</port>"
                      + "<port>"
                      + "<name>dummy2</name>"
                      + "<sub-interface>testInterface2</sub-interface>"
                      + "</port>"
                      + "</ports>"
                      + "</forwarder>"
                      + "</forwarders>";
        editConfig(m_server, m_clientInfo, requestXml1, true);
    }
	
	@Test public void testExpressionListWithoutKeysCase3() throws Exception {
        /**
         * With two forwarders and two ports one with UserPort
         */
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                             + "  <interfaces xmlns=\"test-interfaces\">"
                             + "    <interface>"
                             + "     <name>testInterface</name>"
                             + "     <type>ieee80211</type>"
                             + "     <fwd:interface-usage xmlns:fwd=\"test-forwarders\">"
                             + "     <fwd:interface-usage>network-port</fwd:interface-usage>"
                             + "     </fwd:interface-usage>"
                             + "    </interface>"
                             + "    <interface>"
                             + "     <name>testInterface2</name>"
                             + "     <type>ieee80211</type>"
                             + "     <fwd:interface-usage xmlns:fwd=\"test-forwarders\">"
                             + "     <fwd:interface-usage>user-port</fwd:interface-usage>"
                             + "     </fwd:interface-usage>"
                             + "    </interface>"
                             + "  </interfaces>";
        editConfig(m_server, m_clientInfo, requestXml1, true);

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                      + "<forwarders xmlns=\"test-forwarders\">"
                      + "<forwarder>"
                      + "<name>test1</name>"
                      + "<if-name>sample</if-name>"
                      + "<ports>"
                      + "<port>"
                      + "<name>dummy</name>"
                      + "<sub-interface>testInterface</sub-interface>"
                      + "</port>"
                      + "</ports>"
                      + "</forwarder>"                      
                      + "<forwarder>"
                      + "<name>test2</name>"
                      + "<if-name>sample</if-name>"
                      + "<ports>"
                      + "<port>"
                      + "<name>dummy</name>"
                      + "<sub-interface>testInterface</sub-interface>"
                      + "</port>"
                      + "<port>"
                      + "<name>dummy2</name>"
                      + "<sub-interface>testInterface2</sub-interface>"
                      + "</port>"
                      + "</ports>"
                      + "</forwarder>"
                      + "</forwarders>";
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
        NetconfRpcError error = response.getErrors().get(0);
        assertEquals("must-violation", error.getErrorAppTag());
        assertEquals("/bbf-l2-fwd:forwarders/bbf-l2-fwd:forwarder[bbf-l2-fwd:name='test1']", error.getErrorPath());
        assertEquals("Violate must constraints: not(boolean(/if:interfaces/if:interface[if:name = current()/bbf-l2-fwd:ports/bbf-l2-fwd:port/bbf-l2-fwd:sub-interface and bbf-if-usg:interface-usage/bbf-if-usg:interface-usage = 'user-port']))", error.getErrorMessage());
    }
	
	@Test public void testExpressionListWithoutKeysCase4() throws Exception {
        /**
         * With two expressions separated by OR 
         */
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                             + "  <interfaces xmlns=\"test-interfaces\">"
                             + "    <interface>"
                             + "     <name>testInterface</name>"
                             + "     <type>ieee80211</type>"
                             + "     <fwd:interface-usage xmlns:fwd=\"test-forwarders\">"
                             + "     <fwd:interface-usage>inherit</fwd:interface-usage>"
                             + "     </fwd:interface-usage>"
                             + "    </interface>"
                             + "    <interface>"
                             + "     <name>testInterface2</name>"
                             + "     <type>ieee80211</type>"
                             + "     <fwd:interface-usage xmlns:fwd=\"test-forwarders\">"
                             + "     <fwd:interface-usage>inherit</fwd:interface-usage>"
                             + "     </fwd:interface-usage>"
                             + "    </interface>"
                             + "  </interfaces>";
        editConfig(m_server, m_clientInfo, requestXml1, true);

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                      + "<forwarders xmlns=\"test-forwarders\">"
                      + "<test-forwarder>"
                      + "<name>test</name>"
                      + "<if-name>sample</if-name>"
                      + "<ports>"
                      + "<port>"
                      + "<name>dummy</name>"
                      + "<sub-interface>testInterface</sub-interface>"
                      + "</port>"
                      + "<port>"
                      + "<name>dummy2</name>"
                      + "<sub-interface>testInterface2</sub-interface>"
                      + "</port>"
                      + "</ports>"
                      + "</test-forwarder>"
                      + "</forwarders>";
        editConfig(m_server, m_clientInfo, requestXml1, true);
    }
	
	@Test public void testMustWithNotAndBooleanFailureCase() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
							 + "  <interfaces xmlns=\"test-interfaces\">"
							 + "    <interface>"
							 + "     <name>testInterface</name>"
							 + "     <type>ieee80211</type>"
							 + "     <fwd:interface-usage xmlns:fwd=\"test-forwarders\">"
							 + "     <fwd:interface-usage>inherit</fwd:interface-usage>"
							 + "     </fwd:interface-usage>"
							 + "    </interface>"
							 + "    <interface>"
							 + "     <name>testInterface2</name>"
							 + "     <type>ieee80211</type>"
							 + "     <fwd:interface-usage xmlns:fwd=\"test-forwarders\">"
							 + "     <fwd:interface-usage>network-port</fwd:interface-usage>"
							 + "     </fwd:interface-usage>"
							 + "    </interface>"
							 + "  </interfaces>";

		editConfig(m_server, m_clientInfo, requestXml1, true);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
					  + "<forwarders xmlns=\"test-forwarders\">"
					  + "  <forwarder>"
					  + "    <name>test</name>"
					  + "    <if-name>sample</if-name>"
					  + "    <ports>"
					  + "      <port>"
					  + "        <name>dummy</name>"
					  + "        <sub-interface>testInterface</sub-interface>"
					  + "      </port>"
					  + "      <port>"
					  + "        <name>dummy2</name>"
					  + "        <sub-interface>testInterface2</sub-interface>"
					  + "      </port>"
					  + "    </ports>"
					  + "  </forwarder>"
					  + "</forwarders>";

		//Will pass now (skipping must constraint) due to FNMS-51842. Should fail after 49373 is fixed.
		editConfig(m_server, m_clientInfo, requestXml1, true);

		String response = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						  + " <data>"
						  + "  <bbf-l2-fwd:forwarders xmlns:bbf-l2-fwd=\"test-forwarders\">"
						  + "    <bbf-l2-fwd:forwarder>"
						  + "      <bbf-l2-fwd:name>test</bbf-l2-fwd:name>"
						  + "      <bbf-l2-fwd:if-name>sample</bbf-l2-fwd:if-name>"
						  + "      <bbf-l2-fwd:ports>"
						  + "        <bbf-l2-fwd:port>"
						  + "          <bbf-l2-fwd:name>dummy</bbf-l2-fwd:name>"
						  + "          <bbf-l2-fwd:sub-interface>testInterface</bbf-l2-fwd:sub-interface>"
						  + "        </bbf-l2-fwd:port>"
						  + "        <bbf-l2-fwd:port>"
						  + "          <bbf-l2-fwd:name>dummy2</bbf-l2-fwd:name>"
						  + "          <bbf-l2-fwd:sub-interface>testInterface2</bbf-l2-fwd:sub-interface>"
						  + "        </bbf-l2-fwd:port>"
						  + "      </bbf-l2-fwd:ports>"
						  + "    </bbf-l2-fwd:forwarder>"
						  + "  </bbf-l2-fwd:forwarders>"
						  + "  <if:interfaces xmlns:if=\"test-interfaces\">"
						  + "    <if:interface>"
						  + "      <bbf-l2-fwd:interface-usage xmlns:bbf-l2-fwd=\"test-forwarders\">"
						  + "        <bbf-l2-fwd:interface-usage>inherit</bbf-l2-fwd:interface-usage>"
						  + "      </bbf-l2-fwd:interface-usage>"
						  + "          <if:mybits>fourthBit</if:mybits>\n" 
						  + "           <if:mybitsdefaultValue>thirdBit</if:mybitsdefaultValue>\n"
						  + "           <if:mybitsoverridden>fourthBit</if:mybitsoverridden>\n" 
						  + "      <if:name>testInterface</if:name>"
						  + "      <if:type>if:ieee80211</if:type>"
						  + "    </if:interface>"
						  + "    <if:interface>"
						  + "      <bbf-l2-fwd:interface-usage xmlns:bbf-l2-fwd=\"test-forwarders\">"
						  + "        <bbf-l2-fwd:interface-usage>network-port</bbf-l2-fwd:interface-usage>"
						  + "      </bbf-l2-fwd:interface-usage>"
						  + "          <if:mybits>fourthBit</if:mybits>\n" 
						  + "           <if:mybitsdefaultValue>thirdBit</if:mybitsdefaultValue>\n"
						  + "           <if:mybitsoverridden>fourthBit</if:mybitsoverridden>\n"
						  + "      <if:name>testInterface2</if:name>"
						  + "      <if:type>if:ieee80211</if:type>"
						  + "    </if:interface>"
						  + "  </if:interfaces>"
						  + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						  + " </data>"
						  + " </rpc-reply>";
		verifyGet(response);
	}

	@Test public void testSkipConstraintValidation_IfXpathHasWithOutKeyInPredicates_MultipleMustConstraints() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
							 + "  <interfaces xmlns=\"test-interfaces\">"
							 + "    <interface>"
							 + "     <name>testInterface</name>"
							 + "     <type>ieee80211</type>"
							 + "     <fwd:interface-usage xmlns:fwd=\"test-forwarders\">"
							 + "     <fwd:interface-usage>inherit</fwd:interface-usage>"
							 + "     </fwd:interface-usage>"
							 + "    </interface>"
							 + "    <interface>"
							 + "     <name>testInterface2</name>"
							 + "     <type>ieee80212</type>"
							 + "     <fwd:interface-usage xmlns:fwd=\"test-forwarders\">"
							 + "     <fwd:interface-usage>network-port</fwd:interface-usage>"
							 + "     </fwd:interface-usage>"
							 + "    </interface>"
							 + "  </interfaces>";

		editConfig(m_server, m_clientInfo, requestXml1, true);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
					  + "<forwarders xmlns=\"test-forwarders\">"
					  + "  <forwarder>"
					  + "    <name>test</name>"
					  + "    <if-name>test</if-name>"
					  + "    <ports>"
					  + "      <port>"
					  + "        <name>dummy</name>"
					  + "        <sub-interface>testInterface</sub-interface>"
					  + "      </port>"
					  + "      <port>"
					  + "        <name>dummy2</name>"
					  + "        <sub-interface>testInterface2</sub-interface>"
					  + "      </port>"
					  + "    </ports>"
					  + "  </forwarder>"
					  + "</forwarders>";

		// Two must constraints are present for forwarder LIST, One should be skipped due to FNMS-51842,
		// but another one should be validated. Here second must constraint should be failed due to must-violation
		NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(1, response.getErrors().size());
		assertEquals("/bbf-l2-fwd:forwarders/bbf-l2-fwd:forwarder[bbf-l2-fwd:name='test']", response.getErrors().get(0).getErrorPath());
		assertEquals("Violate must constraints: current()/bbf-l2-fwd:if-name = 'sample'", response.getErrors().get(0).getErrorMessage());
	}

	@Test
	public void testWhenOnLocalNameFunction_1() throws Exception {
		getModelNode();

		// local-name() argument refers parent name
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ " <node-set-functions>"
				+ "   <localNameWhenRefersParent>test</localNameWhenRefersParent>"
				+ " </node-set-functions>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		String response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "   <node-set-functions>"
						+ "    <localNameWhenRefersParent>test</localNameWhenRefersParent>"
						+ "   </node-set-functions>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response);

		// local-name() argument refers non-existing node
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "   <node-set-functions>"
				+ "   <localNameWhenRefersDummyLeaf>test</localNameWhenRefersDummyLeaf>"
				+ "  </node-set-functions>"
				+ "</whenMustContainer>";
		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(1,ncResponse.getErrors().size());
		assertEquals("/whenMust:whenMustContainer/whenMust:node-set-functions/whenMust:localNameWhenRefersDummyLeaf", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("Violate when constraints: local-name(../leaf1) = 'leaf1'", ncResponse.getErrors().get(0).getErrorMessage());
	}

	@Test
	public void testWhenOnLocalNameFunction_2() throws Exception {
		getModelNode();

		// local-name() argument refers parent name
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ " <node-set-functions>"
				+ "   <localNameWhenRefersParent>test</localNameWhenRefersParent>"
				+ " </node-set-functions>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		String response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "   <node-set-functions>"
						+ "    <localNameWhenRefersParent>test</localNameWhenRefersParent>"
						+ "   </node-set-functions>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
				;
		verifyGet(response);

//		// local-name() argument refers non-existing node
//		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
//				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
//				+ "   <node-set-functions>"
//				+ "   <localNameWhenRefersDummyLeaf>test</localNameWhenRefersDummyLeaf>"
//				+ "  </node-set-functions>"
//				+ "</whenMustContainer>";
//		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
//		assertEquals(1,ncResponse.getErrors().size());
//		assertEquals("/whenMust:whenMustContainer/whenMust:node-set-functions/whenMust:localNameWhenRefersDummyLeaf", ncResponse.getErrors().get(0).getErrorPath());
//		assertEquals("Violate when constraints: local-name(../leaf1) = 'leaf1'", ncResponse.getErrors().get(0).getErrorMessage());

		// local-name() argument refers empty dummy node
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "   <node-set-functions>"
				+ "   <leaf1/>"
				+ "   <localNameWhenRefersDummyLeaf>test</localNameWhenRefersDummyLeaf>"
				+ "  </node-set-functions>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "   <node-set-functions>"
						+ "   <localNameWhenRefersParent>test</localNameWhenRefersParent>"
						+ "   <leaf1/>"
						+ "   <localNameWhenRefersDummyLeaf>test</localNameWhenRefersDummyLeaf>"
						+ "  </node-set-functions>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
		;
		verifyGet(response);

		// local-name() argument refers existing node
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "   <node-set-functions>"
				+ "   <leaf1>test</leaf1>"
				+ "   <localNameWhenRefersDummyLeaf>test</localNameWhenRefersDummyLeaf>"
				+ "  </node-set-functions>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "   <node-set-functions>"
						+ "   <localNameWhenRefersParent>test</localNameWhenRefersParent>"
						+ "   <leaf1>test</leaf1>"
						+ "   <localNameWhenRefersDummyLeaf>test</localNameWhenRefersDummyLeaf>"
						+ "  </node-set-functions>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
		;
		verifyGet(response);

		// local-name() with delete on existing referred node where the impact node given in the request
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "   <node-set-functions>"
				+ "   <leaf1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>"
				+ "   <localNameWhenRefersDummyLeaf>test</localNameWhenRefersDummyLeaf>"
				+ "  </node-set-functions>"
				+ "</whenMustContainer>";
		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(1,ncResponse.getErrors().size());
		assertEquals("/whenMust:whenMustContainer/whenMust:node-set-functions/whenMust:localNameWhenRefersDummyLeaf", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("Violate when constraints: local-name(../leaf1) = 'leaf1'", ncResponse.getErrors().get(0).getErrorMessage());

	}

	@Test
	public void testWhenOnLocalNameFunction_3() throws Exception {
		getModelNode();

		// local-name() argument refers parent name
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ " <node-set-functions>"
				+ "   <localNameWhenRefersParent>test</localNameWhenRefersParent>"
				+ " </node-set-functions>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		String response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "   <node-set-functions>"
						+ "    <localNameWhenRefersParent>test</localNameWhenRefersParent>"
						+ "   </node-set-functions>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
				;
		verifyGet(response);

//		// local-name() argument refers non-existing node
//		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
//				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
//				+ "   <node-set-functions>"
//				+ "   <localNameWhenRefersDummyLeaf>test</localNameWhenRefersDummyLeaf>"
//				+ "  </node-set-functions>"
//				+ "</whenMustContainer>";
//		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
//		assertEquals(1,ncResponse.getErrors().size());
//		assertEquals("/whenMust:whenMustContainer/whenMust:node-set-functions/whenMust:localNameWhenRefersDummyLeaf", ncResponse.getErrors().get(0).getErrorPath());
//		assertEquals("Violate when constraints: local-name(../leaf1) = 'leaf1'", ncResponse.getErrors().get(0).getErrorMessage());

		// local-name() argument refers empty dummy node
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "   <node-set-functions>"
				+ "   <leaf1/>"
				+ "   <localNameWhenRefersDummyLeaf>test</localNameWhenRefersDummyLeaf>"
				+ "  </node-set-functions>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "   <node-set-functions>"
						+ "   <localNameWhenRefersParent>test</localNameWhenRefersParent>"
						+ "   <leaf1/>"
						+ "   <localNameWhenRefersDummyLeaf>test</localNameWhenRefersDummyLeaf>"
						+ "  </node-set-functions>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
		;
		verifyGet(response);

		// local-name() argument refers existing node
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "   <node-set-functions>"
				+ "   <leaf1>test</leaf1>"
				+ "   <localNameWhenRefersDummyLeaf>test</localNameWhenRefersDummyLeaf>"
				+ "  </node-set-functions>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "   <node-set-functions>"
						+ "   <localNameWhenRefersParent>test</localNameWhenRefersParent>"
						+ "   <leaf1>test</leaf1>"
						+ "   <localNameWhenRefersDummyLeaf>test</localNameWhenRefersDummyLeaf>"
						+ "  </node-set-functions>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
		;
		verifyGet(response);

//		// local-name() with delete on existing referred node where the impact node given in the request
//		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
//				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
//				+ "   <node-set-functions>"
//				+ "   <leaf1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>"
//				+ "   <localNameWhenRefersDummyLeaf>test</localNameWhenRefersDummyLeaf>"
//				+ "  </node-set-functions>"
//				+ "</whenMustContainer>";
//		ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
//		assertEquals(1,ncResponse.getErrors().size());
//		assertEquals("/whenMust:whenMustContainer/whenMust:node-set-functions/whenMust:localNameWhenRefersDummyLeaf", ncResponse.getErrors().get(0).getErrorPath());
//		assertEquals("Violate when constraints: local-name(../leaf1) = 'leaf1'", ncResponse.getErrors().get(0).getErrorMessage());

		// local-name() with delete on existing referred node where the impact node not given in the request
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "   <node-set-functions>"
				+ "   <leaf1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>"
				+ "  </node-set-functions>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "   <node-set-functions>"
						+ "   <localNameWhenRefersParent>test</localNameWhenRefersParent>"
						+ "  </node-set-functions>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
		;
		verifyGet(response);
	}

	@Test
	public void testMustOnLocalNameFunction() throws Exception {
		getModelNode();

		// local-name() argument refers parent name
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "   <node-set-functions>"
				+ "   <localNameMustRefersParent>test</localNameMustRefersParent>"
				+ "  </node-set-functions>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		String response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "   <node-set-functions>"
						+ "   <localNameMustRefersParent>test</localNameMustRefersParent>"
						+ "  </node-set-functions>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response);

		// with no-arg on local-name()
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "   <node-set-functions>"
				+ "   <localNameMustWithNoArg>test</localNameMustWithNoArg>"
				+ "  </node-set-functions>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "   <node-set-functions>"
						+ "   <localNameMustRefersParent>test</localNameMustRefersParent>"
						+ "   <localNameMustWithNoArg>test</localNameMustWithNoArg>"
						+ "  </node-set-functions>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response);

		// local-name() argument refers non-existing node
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "   <node-set-functions>"
				+ "   <localNameMustRefersDummyLeaf>test</localNameMustRefersDummyLeaf>"
				+ "  </node-set-functions>"
				+ "</whenMustContainer>";
		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(1,ncResponse.getErrors().size());
		assertEquals("/whenMust:whenMustContainer/whenMust:node-set-functions/whenMust:localNameMustRefersDummyLeaf", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("Violate must constraints: local-name(../leaf1) = 'leaf1'", ncResponse.getErrors().get(0).getErrorMessage());

		// local-name() argument refers empty dummy node
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "   <node-set-functions>"
				+ "   <leaf1/>"
				+ "   <localNameMustRefersDummyLeaf>test</localNameMustRefersDummyLeaf>"
				+ "  </node-set-functions>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "   <node-set-functions>"
						+ "   <localNameMustRefersParent>test</localNameMustRefersParent>"
						+ "   <localNameMustWithNoArg>test</localNameMustWithNoArg>"
						+ "   <leaf1/>"
						+ "   <localNameMustRefersDummyLeaf>test</localNameMustRefersDummyLeaf>"
						+ "  </node-set-functions>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response);

		// local-name() argument refers existing node
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "   <node-set-functions>"
				+ "   <leaf1>test</leaf1>"
				+ "   <localNameMustRefersDummyLeaf>test</localNameMustRefersDummyLeaf>"
				+ "  </node-set-functions>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "   <node-set-functions>"
						+ "   <localNameMustRefersParent>test</localNameMustRefersParent>"
						+ "   <localNameMustWithNoArg>test</localNameMustWithNoArg>"
						+ "   <leaf1>test</leaf1>"
						+ "   <localNameMustRefersDummyLeaf>test</localNameMustRefersDummyLeaf>"
						+ "  </node-set-functions>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response);

		// local-name() with delete on existing referred node
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "   <node-set-functions>"
				+ "   <leaf1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>"
				+ "   <localNameMustRefersDummyLeaf>test</localNameMustRefersDummyLeaf>"
				+ "  </node-set-functions>"
				+ "</whenMustContainer>";
		ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(1,ncResponse.getErrors().size());
		assertEquals("/whenMust:whenMustContainer/whenMust:node-set-functions/whenMust:localNameMustRefersDummyLeaf", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("Violate must constraints: local-name(../leaf1) = 'leaf1'", ncResponse.getErrors().get(0).getErrorMessage());
	}

	@Test
	public void testWhenOnNamespaceUriFunction() throws Exception {
		getModelNode();
		// namespace-uri() argument refers parent's node default namespace
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "   <node-set-functions>"
				+ "   <when-parent-namespace-leaf>test</when-parent-namespace-leaf>"
				+ "  </node-set-functions>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		String response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "   <node-set-functions>"
						+ "   <when-parent-namespace-leaf>test</when-parent-namespace-leaf>"
						+ "  </node-set-functions>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response);

		// namespace-uri() argument refers parent's node namespace
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMust:whenMustContainer xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "   <whenMust:node-set-functions>"
				+ "   <whenMust:when-parent-namespace-leaf>test</whenMust:when-parent-namespace-leaf>"
				+ "  </whenMust:node-set-functions>"
				+ "</whenMust:whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "   <node-set-functions>"
						+ "   <when-parent-namespace-leaf>test</when-parent-namespace-leaf>"
						+ "  </node-set-functions>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response);

		// namespace-uri() argument with no-arg i.e refers context-node
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "   <node-set-functions>"
				+ "   <when-no-arg-namespace-leaf>test</when-no-arg-namespace-leaf>"
				+ "  </node-set-functions>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "   <node-set-functions>"
						+ "   <when-no-arg-namespace-leaf>test</when-no-arg-namespace-leaf>"
						+ "   <when-parent-namespace-leaf>test</when-parent-namespace-leaf>"
						+ "  </node-set-functions>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response);

		/*		// namespace-uri() argument refers sibling node
	    // This is not working yet, so only the above trivial case currently works (tracked by FNMS-20720)
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "   <node-set-functions>"
                + "   <leaf1/>"
                + "   <when-sibling-namespace-leaf>test</when-sibling-namespace-leaf>"
                + "  </node-set-functions>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "   <node-set-functions>"
		                + "   <when-no-arg-namespace-leaf>test</when-no-arg-namespace-leaf>"
		                + "   <when-parent-namespace-leaf>test</when-parent-namespace-leaf>"
		                + "   <when-sibling-namespace-leaf>test</when-sibling-namespace-leaf>"
		                + "  </node-set-functions>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response);*/		
	}

	@Test
	public void testMustOnNamespaceUriFunction() throws Exception {
		getModelNode();
		// namespace-uri() argument refers parent's node default namespace
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "   <node-set-functions>"
				+ "   <must-parent-namespace-leaf>test</must-parent-namespace-leaf>"
				+ "  </node-set-functions>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		String response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "   <node-set-functions>"
						+ "   <must-parent-namespace-leaf>test</must-parent-namespace-leaf>"
						+ "  </node-set-functions>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response);

		// namespace-uri() argument refers parent's node namespace
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMust:whenMustContainer xmlns:whenMust=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "   <whenMust:node-set-functions>"
				+ "   <whenMust:must-parent-namespace-leaf>test</whenMust:must-parent-namespace-leaf>"
				+ "  </whenMust:node-set-functions>"
				+ "</whenMust:whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "   <node-set-functions>"
						+ "   <must-parent-namespace-leaf>test</must-parent-namespace-leaf>"
						+ "  </node-set-functions>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response);

		// namespace-uri() argument with no-arg i.e refers context-node
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "   <node-set-functions>"
				+ "   <must-no-arg-namespace-leaf>test</must-no-arg-namespace-leaf>"
				+ "  </node-set-functions>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "   <node-set-functions>"
						+ "   <must-no-arg-namespace-leaf>test</must-no-arg-namespace-leaf>"
						+ "   <must-parent-namespace-leaf>test</must-parent-namespace-leaf>"
						+ "  </node-set-functions>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response);

		/*		// namespace-uri() argument refers sibling node
	    // This is not working yet, so only the above trivial case currently works (tracked by FNMS-20720)
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "   <node-set-functions>"
                + "   <leaf1/>"
                + "   <must-sibling-namespace-leaf>test</must-sibling-namespace-leaf>"
                + "  </node-set-functions>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
		                + "   <node-set-functions>"
		                + "   <must-no-arg-namespace-leaf>test</must-no-arg-namespace-leaf>"
		                + "   <must-parent-namespace-leaf>test</must-parent-namespace-leaf>"
		                + "   <must-sibling-namespace-leaf>test</must-sibling-namespace-leaf>"
		                + "  </node-set-functions>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response);*/		
	}

	@Test
	public void testWhenContainerLeafWithLocalNameFunction() throws Exception {
		getModelNode();

		// local-name() argument refers parent name
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "   <leaf1>test</leaf1>"
				+ "   <containerLeafLocalNameReferSibling>test</containerLeafLocalNameReferSibling>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		String response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "   <leaf1>test</leaf1>"
						+ "   <containerLeafLocalNameReferSibling>test</containerLeafLocalNameReferSibling>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response);

		// delete leaf1 with same value for containerLeafLocalNameReferSibling
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "   <leaf1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>"
				+ "   <containerLeafLocalNameReferSibling>test</containerLeafLocalNameReferSibling>"
				+ "</whenMustContainer>";
		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(1,ncResponse.getErrors().size());
		assertEquals("/whenMust:whenMustContainer/whenMust:containerLeafLocalNameReferSibling", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("Violate when constraints: local-name(../leaf1) = 'leaf1'", ncResponse.getErrors().get(0).getErrorMessage());
	}

	@Test
	public void testMustBooleanFunction() throws Exception {
		getModelNode();

		// create boolean-function-leaf refers to non-existent leaf
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-boolean-function-validation>"
				+ "    <boolean-function-leaf>test</boolean-function-leaf>"
				+ "  </must-boolean-function-validation>"		
				+ "</whenMustContainer>";
		// should fail
		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(1,ncResponse.getErrors().size());
		assertEquals("/whenMust:whenMustContainer/whenMust:must-boolean-function-validation/whenMust:boolean-function-leaf", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("Violate must constraints: boolean(../string1)", ncResponse.getErrors().get(0).getErrorMessage());

		// create boolean-function-leaf refers to existent empty nodes-set leaf
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-boolean-function-validation>"
				+ "    <string1></string1>"
				+ "    <boolean-function-leaf>test</boolean-function-leaf>"
				+ "  </must-boolean-function-validation>"		
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);    	

		String response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <must-boolean-function-validation>"
						+ "    <string1></string1>"
						+ "    <boolean-function-leaf>test</boolean-function-leaf>"
						+ "  </must-boolean-function-validation>"	
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response); 

		// create boolean-function-leaf refers to existent leaf
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-boolean-function-validation>"
				+ "    <string1>nonempty</string1>"
				+ "    <boolean-function-leaf>test</boolean-function-leaf>"
				+ "  </must-boolean-function-validation>"		
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);    	

		response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <must-boolean-function-validation>"
						+ "    <string1>nonempty</string1>"
						+ "    <boolean-function-leaf>test</boolean-function-leaf>"
						+ "  </must-boolean-function-validation>"	
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response);   	

		// create boolean-function-empty-string-arg
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-boolean-function-validation>"
				+ "    <boolean-function-empty-string-arg>test</boolean-function-empty-string-arg>"
				+ "  </must-boolean-function-validation>"		
				+ "</whenMustContainer>";
		// should fail
		ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(1,ncResponse.getErrors().size());
		assertEquals("/whenMust:whenMustContainer/whenMust:must-boolean-function-validation/whenMust:boolean-function-empty-string-arg", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("Violate must constraints: boolean('')", ncResponse.getErrors().get(0).getErrorMessage());  

		// create boolean-function-non-empty-string-arg
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-boolean-function-validation>"
				+ "    <boolean-function-non-empty-string-arg>test</boolean-function-non-empty-string-arg>"
				+ "  </must-boolean-function-validation>"		
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);
		response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <must-boolean-function-validation>"
						+ "    <string1>nonempty</string1>"
						+ "    <boolean-function-leaf>test</boolean-function-leaf>"
						+ "    <boolean-function-non-empty-string-arg>test</boolean-function-non-empty-string-arg>"
						+ "    <boolean-function-empty-string-arg>test</boolean-function-empty-string-arg>"
						+ "  </must-boolean-function-validation>"	
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response); 

		// create boolean-function-non-zero-number-arg
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-boolean-function-validation>"
				+ "    <boolean-function-non-zero-number-arg>test</boolean-function-non-zero-number-arg>"
				+ "  </must-boolean-function-validation>"		
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <must-boolean-function-validation>"
						+ "    <string1>nonempty</string1>"
						+ "    <boolean-function-leaf>test</boolean-function-leaf>"
						+ "    <boolean-function-non-empty-string-arg>test</boolean-function-non-empty-string-arg>"
						+ "    <boolean-function-empty-string-arg>test</boolean-function-empty-string-arg>"
						+ "    <boolean-function-non-zero-number-arg>test</boolean-function-non-zero-number-arg>"
						+ "  </must-boolean-function-validation>"	
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response); 

		// create boolean-function-zero-number-arg
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-boolean-function-validation>"
				+ "    <boolean-function-zero-number-arg>test</boolean-function-zero-number-arg>"
				+ "  </must-boolean-function-validation>"		
				+ "</whenMustContainer>";
		// should fail
		ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(1,ncResponse.getErrors().size());
		assertEquals("/whenMust:whenMustContainer/whenMust:must-boolean-function-validation/whenMust:boolean-function-zero-number-arg", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("Violate must constraints: boolean(0)", ncResponse.getErrors().get(0).getErrorMessage());  


		// create boolean-function-null-arg
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-boolean-function-validation>"
				+ "    <boolean-function-null-arg>test</boolean-function-null-arg>"
				+ "  </must-boolean-function-validation>"		
				+ "</whenMustContainer>";
		// should fail
		ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(1,ncResponse.getErrors().size());
		assertEquals("/whenMust:whenMustContainer/whenMust:must-boolean-function-validation/whenMust:boolean-function-null-arg", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("Violate must constraints: boolean(null)", ncResponse.getErrors().get(0).getErrorMessage()); 

	}

	@Test
	public void testMustNotBooleanFunction_1() throws Exception {
		getModelNode();

		// create not-function-leaf refers to non-existent leaf
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-boolean-function-validation>"
				+ "    <not-function-leaf>test</not-function-leaf>"
				+ "  </must-boolean-function-validation>"		
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);    	

		String response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <must-boolean-function-validation>"
						+ "    <not-function-leaf>test</not-function-leaf>"
						+ "  </must-boolean-function-validation>"	
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response); 

		// create not-function-leaf refers to existent leaf
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-boolean-function-validation>"
				+ "    <string1>nonempty</string1>"
				+ "    <not-function-leaf>test</not-function-leaf>"
				+ "  </must-boolean-function-validation>"		
				+ "</whenMustContainer>";
		// should fail
		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(1,ncResponse.getErrors().size());
		assertEquals("/whenMust:whenMustContainer/whenMust:must-boolean-function-validation/whenMust:not-function-leaf", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("Violate must constraints: not(boolean(../string1))", ncResponse.getErrors().get(0).getErrorMessage());

	}

	@Test
	public void testMustNotBooleanFunction_2() throws Exception {
		getModelNode();

		// create not-function-leaf refers to non-existent leaf
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-boolean-function-validation>"
				+ "    <not-function-leaf>test</not-function-leaf>"
				+ "  </must-boolean-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		String response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <must-boolean-function-validation>"
						+ "    <not-function-leaf>test</not-function-leaf>"
						+ "  </must-boolean-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
				;
		verifyGet(response);

		// no not-function-leaf exists
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-boolean-function-validation>"
				+ "    <string1>nonempty</string1>"
				+ "  </must-boolean-function-validation>"
				+ "</whenMustContainer>";
		// should fail
		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(1,ncResponse.getErrors().size());
		assertEquals("/whenMust:whenMustContainer/whenMust:must-boolean-function-validation/whenMust:not-function-leaf", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("Violate must constraints: not(boolean(../string1))", ncResponse.getErrors().get(0).getErrorMessage());

	}

	@Test
	public void testMustNotBooleanFunction_3() throws Exception {
		getModelNode();

		// create not-function-leaf refers to non-existent leaf
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-boolean-function-validation>"
				+ "    <not-function-leaf>test</not-function-leaf>"
				+ "  </must-boolean-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		String response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <must-boolean-function-validation>"
						+ "    <not-function-leaf>test</not-function-leaf>"
						+ "  </must-boolean-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
				;
		verifyGet(response);

		// create not-function-leaf refers to existent empty node-set leaf
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-boolean-function-validation>"
				+ "    <string1></string1>"
				+ "    <not-function-leaf>test</not-function-leaf>"
				+ "  </must-boolean-function-validation>"
				+ "</whenMustContainer>";
		// should fail
		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(1,ncResponse.getErrors().size());
		assertEquals("/whenMust:whenMustContainer/whenMust:must-boolean-function-validation/whenMust:not-function-leaf", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("Violate must constraints: not(boolean(../string1))", ncResponse.getErrors().get(0).getErrorMessage());

	}

	@Test
	public void testMustTrueFunction() throws Exception {
		getModelNode();
		// create true-function-leaf with true() defaults to true
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-boolean-function-validation>"
				+ "    <true-function-leaf>test</true-function-leaf>"
				+ "  </must-boolean-function-validation>"		
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);    	

		String response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <must-boolean-function-validation>"
						+ "    <true-function-leaf>test</true-function-leaf>"
						+ "  </must-boolean-function-validation>"	
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response); 
	}

	@Test
	public void testMustFalseFunction() throws Exception {
		getModelNode();

		// create false-function-leaf with false() defaults to false
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-boolean-function-validation>"
				+ "    <false-function-leaf>test</false-function-leaf>"
				+ "  </must-boolean-function-validation>"		
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);    	

		String response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <must-boolean-function-validation>"
						+ "    <false-function-leaf>test</false-function-leaf>"
						+ "  </must-boolean-function-validation>"	
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response); 
	}

	@Test
	public void testWhenBooleanFunction() throws Exception {
		getModelNode();

		// create boolean-function-leaf refers to non-existent leaf
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-boolean-function-validation>"
				+ "    <boolean-function-leaf>test</boolean-function-leaf>"
				+ "  </when-boolean-function-validation>"		
				+ "</whenMustContainer>";
		// should fail
		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(1,ncResponse.getErrors().size());
		assertEquals("/whenMust:whenMustContainer/whenMust:when-boolean-function-validation/whenMust:boolean-function-leaf", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("Violate when constraints: boolean(../string1)", ncResponse.getErrors().get(0).getErrorMessage());

		// create boolean-function-leaf refers to existent empty-nodeset leaf
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-boolean-function-validation>"
				+ "    <string1></string1>"
				+ "    <boolean-function-leaf>test</boolean-function-leaf>"
				+ "  </when-boolean-function-validation>"		
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);    	

		String response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <when-boolean-function-validation>"
						+ "    <string1></string1>"
						+ "    <boolean-function-leaf>test</boolean-function-leaf>"
						+ "  </when-boolean-function-validation>"	
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response);

		// create boolean-function-leaf refers to existent leaf
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-boolean-function-validation>"
				+ "    <string1>nonempty</string1>"
				+ "    <boolean-function-leaf>test</boolean-function-leaf>"
				+ "  </when-boolean-function-validation>"		
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);    	

		response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <when-boolean-function-validation>"
						+ "    <string1>nonempty</string1>"
						+ "    <boolean-function-leaf>test</boolean-function-leaf>"
						+ "  </when-boolean-function-validation>"	
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response);   	 

		// create boolean-function-empty-string-arg
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-boolean-function-validation>"
				+ "    <boolean-function-empty-string-arg>test</boolean-function-empty-string-arg>"
				+ "  </when-boolean-function-validation>"		
				+ "</whenMustContainer>";
		// should fail
		ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(1,ncResponse.getErrors().size());
		assertEquals("/whenMust:whenMustContainer/whenMust:when-boolean-function-validation/whenMust:boolean-function-empty-string-arg", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("Violate when constraints: boolean('')", ncResponse.getErrors().get(0).getErrorMessage());  

		// create boolean-function-non-empty-string-arg
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-boolean-function-validation>"
				+ "    <boolean-function-non-empty-string-arg>test</boolean-function-non-empty-string-arg>"
				+ "  </when-boolean-function-validation>"		
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);
		response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <when-boolean-function-validation>"
						+ "    <string1>nonempty</string1>"
						+ "    <boolean-function-leaf>test</boolean-function-leaf>"
						+ "    <boolean-function-non-empty-string-arg>test</boolean-function-non-empty-string-arg>"
						+ "    <boolean-function-empty-string-arg>test</boolean-function-empty-string-arg>"
						+ "  </when-boolean-function-validation>"	
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response); 

		// create boolean-function-non-zero-number-arg
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-boolean-function-validation>"
				+ "    <boolean-function-non-zero-number-arg>test</boolean-function-non-zero-number-arg>"
				+ "  </when-boolean-function-validation>"		
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <when-boolean-function-validation>"
						+ "    <string1>nonempty</string1>"
						+ "    <boolean-function-leaf>test</boolean-function-leaf>"
						+ "    <boolean-function-non-empty-string-arg>test</boolean-function-non-empty-string-arg>"
						+ "    <boolean-function-empty-string-arg>test</boolean-function-empty-string-arg>"
						+ "    <boolean-function-non-zero-number-arg>test</boolean-function-non-zero-number-arg>"
						+ "  </when-boolean-function-validation>"	
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response); 

		// create boolean-function-not-equals2
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-boolean-function-validation>"
				+ "    <boolean-function-not-equals2>test</boolean-function-not-equals2>"
				+ "  </when-boolean-function-validation>"		
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <when-boolean-function-validation>"
						+ "    <string1>nonempty</string1>"
						+ "    <boolean-function-leaf>test</boolean-function-leaf>"
						+ "    <boolean-function-non-empty-string-arg>test</boolean-function-non-empty-string-arg>"
						+ "    <boolean-function-empty-string-arg>test</boolean-function-empty-string-arg>"
						+ "    <boolean-function-non-zero-number-arg>test</boolean-function-non-zero-number-arg>"
						+ "    <boolean-function-not-equals2>test</boolean-function-not-equals2>"
						+ "  </when-boolean-function-validation>"	
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response); 

		// create boolean-function-not-equals1
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-boolean-function-validation>"
				+ "    <boolean-function-not-equals1>test</boolean-function-not-equals1>"
				+ "  </when-boolean-function-validation>"		
				+ "</whenMustContainer>";
		// should fail
		ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(1,ncResponse.getErrors().size());
		assertEquals("/whenMust:whenMustContainer/whenMust:when-boolean-function-validation/whenMust:boolean-function-not-equals1", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("Violate when constraints: boolean('test') != boolean('test')", ncResponse.getErrors().get(0).getErrorMessage());  

		// create boolean-function-zero-number-arg
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-boolean-function-validation>"
				+ "    <boolean-function-zero-number-arg>test</boolean-function-zero-number-arg>"
				+ "  </when-boolean-function-validation>"		
				+ "</whenMustContainer>";
		// should fail
		ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(1,ncResponse.getErrors().size());
		assertEquals("/whenMust:whenMustContainer/whenMust:when-boolean-function-validation/whenMust:boolean-function-zero-number-arg", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("Violate when constraints: boolean(0)", ncResponse.getErrors().get(0).getErrorMessage());  

	}

	@Test
	public void testWhenNotBooleanFunction() throws Exception {
		getModelNode();

		// create not-function-leaf refers to non-existent leaf
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-boolean-function-validation>"
				+ "    <not-function-leaf>test</not-function-leaf>"
				+ "  </when-boolean-function-validation>"		
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);    	

		String response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <when-boolean-function-validation>"
						+ "    <not-function-leaf>test</not-function-leaf>"
						+ "  </when-boolean-function-validation>"	
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response); 

		// create not-function-leaf refers to existent leaf
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-boolean-function-validation>"
				+ "    <string1>nonempty</string1>"
				+ "    <not-function-leaf>test</not-function-leaf>"
				+ "  </when-boolean-function-validation>"		
				+ "</whenMustContainer>";
		// should fail
		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(1,ncResponse.getErrors().size());
		assertEquals("/whenMust:whenMustContainer/whenMust:when-boolean-function-validation/whenMust:not-function-leaf", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("Violate when constraints: not(boolean(../string1))", ncResponse.getErrors().get(0).getErrorMessage());
	}

	@Test
	public void testWhenTrueFunction() throws Exception {
		getModelNode();
		// create true-function-leaf with true() defaults to true
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-boolean-function-validation>"
				+ "    <true-function-leaf>test</true-function-leaf>"
				+ "  </when-boolean-function-validation>"		
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);    	

		String response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <when-boolean-function-validation>"
						+ "    <true-function-leaf>test</true-function-leaf>"
						+ "  </when-boolean-function-validation>"	
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response); 
	}

	@Test
	public void testWhenFalseFunction() throws Exception {
		getModelNode();

		// create false-function-leaf with false() defaults to false
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-boolean-function-validation>"
				+ "    <false-function-leaf>test</false-function-leaf>"
				+ "  </when-boolean-function-validation>"		
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);    	

		String response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <when-boolean-function-validation>"
						+ "    <false-function-leaf>test</false-function-leaf>"
						+ "  </when-boolean-function-validation>"	
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response); 
	}

	@Test
	public void testMustStringFunction_1() throws Exception {
		getModelNode();

		//create number1
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-string-function-validation>"
				+ "    <number1>42</number1>"
				+ "    <string-function-leaf>test</string-function-leaf>"
				+ "  </must-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);   

		String response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <must-string-function-validation>"
						+ "    <number1>42</number1>"
						+ "    <string-function-leaf>test</string-function-leaf>"
						+ "  </must-string-function-validation>"	
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response);

		//no-arg string function
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-string-function-validation>"
				+ "    <no-arg-string-function-leaf>test</no-arg-string-function-leaf>"
				+ "  </must-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <must-string-function-validation>"
						+ "    <number1>42</number1>"
						+ "    <string-function-leaf>test</string-function-leaf>"
						+ "    <no-arg-string-function-leaf>test</no-arg-string-function-leaf>"
						+ "  </must-string-function-validation>"	
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response); 

		//non-existent leaf string function
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-string-function-validation>"
				+ "    <string2></string2>"
				+ "    <string-function-non-existent-leaf>test</string-function-non-existent-leaf>"
				+ "  </must-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <must-string-function-validation>"
						+ "    <number1>42</number1>"
						+ "    <string2></string2>"
						+ "    <string-function-leaf>test</string-function-leaf>"
						+ "    <no-arg-string-function-leaf>test</no-arg-string-function-leaf>"
						+ "    <string-function-non-existent-leaf>test</string-function-non-existent-leaf>"
						+ "  </must-string-function-validation>"	
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response); 

		//string function with arg as boolean true
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-string-function-validation>"
				+ "    <string-function-boolean-true-leaf>test</string-function-boolean-true-leaf>"
				+ "  </must-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		//string function with arg as boolean false
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-string-function-validation>"
				+ "    <string-function-boolean-false-leaf>test</string-function-boolean-false-leaf>"
				+ "  </must-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <must-string-function-validation>"
						+ "    <string-function-boolean-false-leaf>test</string-function-boolean-false-leaf>"
						+ "    <string-function-boolean-true-leaf>test</string-function-boolean-true-leaf>"
						+ "    <number1>42</number1>"
						+ "    <string2></string2>"
						+ "    <string-function-leaf>test</string-function-leaf>"
						+ "    <no-arg-string-function-leaf>test</no-arg-string-function-leaf>"
						+ "    <string-function-non-existent-leaf>test</string-function-non-existent-leaf>"
						+ "  </must-string-function-validation>"	
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response); 

				/*//non-existent leaf-list string function
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-string-function-validation>"
				+ "    <string-function-refers-leaflist>test</string-function-refers-leaflist>"
				+ "  </must-string-function-validation>"
				+ "</whenMustContainer>";
		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(1,ncResponse.getErrors().size());
		assertEquals("/whenMust:whenMustContainer/whenMust:must-string-function-validation/whenMust:string-function-refers-leaflist", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("Violate must constraints: string(../string3) != ''", ncResponse.getErrors().get(0).getErrorMessage());

		//create leaf-list string function
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-string-function-validation>"
				+ "    <string3>test1</string3>"
				+ "    <string3>test2</string3>"
				+ "    <string-function-refers-leaflist>test</string-function-refers-leaflist>"
				+ "  </must-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, false);*/

		//existent leaf string function
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-string-function-validation>"
				+ "    <string2>test</string2>"
				+ "    <string-function-non-existent-leaf>test</string-function-non-existent-leaf>"
				+ "  </must-string-function-validation>"
				+ "</whenMustContainer>";
		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(1,ncResponse.getErrors().size());
		assertEquals("/whenMust:whenMustContainer/whenMust:must-string-function-validation/whenMust:string-function-non-existent-leaf", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("Violate must constraints: string(../string2) = ''", ncResponse.getErrors().get(0).getErrorMessage());

	}

	@Test
	public void testMustStringFunction_2() throws Exception {
		getModelNode();

		//create number1
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-string-function-validation>"
				+ "    <number1>42</number1>"
				+ "    <string-function-leaf>test</string-function-leaf>"
				+ "  </must-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		String response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <must-string-function-validation>"
						+ "    <number1>42</number1>"
						+ "    <string-function-leaf>test</string-function-leaf>"
						+ "  </must-string-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
				;
		verifyGet(response);

		//no-arg string function
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-string-function-validation>"
				+ "    <no-arg-string-function-leaf>test</no-arg-string-function-leaf>"
				+ "  </must-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <must-string-function-validation>"
						+ "    <number1>42</number1>"
						+ "    <string-function-leaf>test</string-function-leaf>"
						+ "    <no-arg-string-function-leaf>test</no-arg-string-function-leaf>"
						+ "  </must-string-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
		;
		verifyGet(response);

		//non-existent leaf string function
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-string-function-validation>"
				+ "    <string2></string2>"
				+ "    <string-function-non-existent-leaf>test</string-function-non-existent-leaf>"
				+ "  </must-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <must-string-function-validation>"
						+ "    <number1>42</number1>"
						+ "    <string2></string2>"
						+ "    <string-function-leaf>test</string-function-leaf>"
						+ "    <no-arg-string-function-leaf>test</no-arg-string-function-leaf>"
						+ "    <string-function-non-existent-leaf>test</string-function-non-existent-leaf>"
						+ "  </must-string-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
		;
		verifyGet(response);

		//string function with arg as boolean true
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-string-function-validation>"
				+ "    <string-function-boolean-true-leaf>test</string-function-boolean-true-leaf>"
				+ "  </must-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		//string function with arg as boolean false
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-string-function-validation>"
				+ "    <string-function-boolean-false-leaf>test</string-function-boolean-false-leaf>"
				+ "  </must-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <must-string-function-validation>"
						+ "    <string-function-boolean-false-leaf>test</string-function-boolean-false-leaf>"
						+ "    <string-function-boolean-true-leaf>test</string-function-boolean-true-leaf>"
						+ "    <number1>42</number1>"
						+ "    <string2></string2>"
						+ "    <string-function-leaf>test</string-function-leaf>"
						+ "    <no-arg-string-function-leaf>test</no-arg-string-function-leaf>"
						+ "    <string-function-non-existent-leaf>test</string-function-non-existent-leaf>"
						+ "  </must-string-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
		;
		verifyGet(response);

				/*//non-existent leaf-list string function
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-string-function-validation>"
				+ "    <string-function-refers-leaflist>test</string-function-refers-leaflist>"
				+ "  </must-string-function-validation>"
				+ "</whenMustContainer>";
		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(1,ncResponse.getErrors().size());
		assertEquals("/whenMust:whenMustContainer/whenMust:must-string-function-validation/whenMust:string-function-refers-leaflist", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("Violate must constraints: string(../string3) != ''", ncResponse.getErrors().get(0).getErrorMessage());

		//create leaf-list string function
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-string-function-validation>"
				+ "    <string3>test1</string3>"
				+ "    <string3>test2</string3>"
				+ "    <string-function-refers-leaflist>test</string-function-refers-leaflist>"
				+ "  </must-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, false);*/


		// update number1 impacts string-function-leaf and the impacted node exists in the request
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-string-function-validation>"
				+ "    <number1>41</number1>"
				+ "    <string-function-leaf>test</string-function-leaf>"
				+ "  </must-string-function-validation>"
				+ "</whenMustContainer>";
		// should fail
		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(1,ncResponse.getErrors().size());
		assertEquals("/whenMust:whenMustContainer/whenMust:must-string-function-validation/whenMust:string-function-leaf", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("Violate must constraints: string(../number1) = '42'", ncResponse.getErrors().get(0).getErrorMessage());

	}

	@Test
	public void testMustStringFunction_3() throws Exception {
		getModelNode();

		//create number1
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-string-function-validation>"
				+ "    <number1>42</number1>"
				+ "    <string-function-leaf>test</string-function-leaf>"
				+ "  </must-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		String response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <must-string-function-validation>"
						+ "    <number1>42</number1>"
						+ "    <string-function-leaf>test</string-function-leaf>"
						+ "  </must-string-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
				;
		verifyGet(response);

		//no-arg string function
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-string-function-validation>"
				+ "    <no-arg-string-function-leaf>test</no-arg-string-function-leaf>"
				+ "  </must-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <must-string-function-validation>"
						+ "    <number1>42</number1>"
						+ "    <string-function-leaf>test</string-function-leaf>"
						+ "    <no-arg-string-function-leaf>test</no-arg-string-function-leaf>"
						+ "  </must-string-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
		;
		verifyGet(response);

		//non-existent leaf string function
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-string-function-validation>"
				+ "    <string2></string2>"
				+ "    <string-function-non-existent-leaf>test</string-function-non-existent-leaf>"
				+ "  </must-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <must-string-function-validation>"
						+ "    <number1>42</number1>"
						+ "    <string2></string2>"
						+ "    <string-function-leaf>test</string-function-leaf>"
						+ "    <no-arg-string-function-leaf>test</no-arg-string-function-leaf>"
						+ "    <string-function-non-existent-leaf>test</string-function-non-existent-leaf>"
						+ "  </must-string-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
		;
		verifyGet(response);

		//string function with arg as boolean true
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-string-function-validation>"
				+ "    <string-function-boolean-true-leaf>test</string-function-boolean-true-leaf>"
				+ "  </must-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		//string function with arg as boolean false
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-string-function-validation>"
				+ "    <string-function-boolean-false-leaf>test</string-function-boolean-false-leaf>"
				+ "  </must-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <must-string-function-validation>"
						+ "    <string-function-boolean-false-leaf>test</string-function-boolean-false-leaf>"
						+ "    <string-function-boolean-true-leaf>test</string-function-boolean-true-leaf>"
						+ "    <number1>42</number1>"
						+ "    <string2></string2>"
						+ "    <string-function-leaf>test</string-function-leaf>"
						+ "    <no-arg-string-function-leaf>test</no-arg-string-function-leaf>"
						+ "    <string-function-non-existent-leaf>test</string-function-non-existent-leaf>"
						+ "  </must-string-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
		;
		verifyGet(response);

				/*//non-existent leaf-list string function
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-string-function-validation>"
				+ "    <string-function-refers-leaflist>test</string-function-refers-leaflist>"
				+ "  </must-string-function-validation>"
				+ "</whenMustContainer>";
		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(1,ncResponse.getErrors().size());
		assertEquals("/whenMust:whenMustContainer/whenMust:must-string-function-validation/whenMust:string-function-refers-leaflist", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("Violate must constraints: string(../string3) != ''", ncResponse.getErrors().get(0).getErrorMessage());

		//create leaf-list string function
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-string-function-validation>"
				+ "    <string3>test1</string3>"
				+ "    <string3>test2</string3>"
				+ "    <string-function-refers-leaflist>test</string-function-refers-leaflist>"
				+ "  </must-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, false);*/


		// update number1 impacts string-function-leaf and the impacted node not exists in the request
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-string-function-validation>"
				+ "    <number1>41</number1>"
				+ "  </must-string-function-validation>"
				+ "</whenMustContainer>";
		// should fail
		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(1,ncResponse.getErrors().size());
		assertEquals("/whenMust:whenMustContainer/whenMust:must-string-function-validation/whenMust:string-function-leaf", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("Violate must constraints: string(../number1) = '42'", ncResponse.getErrors().get(0).getErrorMessage());
	}

	@Test
	public void testMustConcatFunction_1() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-string-function-validation>"
				+ "    <string1>A</string1>"
				+ "    <string2>B</string2>"
				+ "    <concat-function-leaf>test</concat-function-leaf>"
				+ "  </must-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true); 

		String response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <must-string-function-validation>"
						+ "    <string1>A</string1>"
						+ "    <string2>B</string2>"
						+ "    <concat-function-leaf>test</concat-function-leaf>"
						+ "  </must-string-function-validation>"	
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response); 

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-string-function-validation>"
				+ "    <string1>A</string1>"
				+ "    <string2>C</string2>"
				+ "    <concat-function-leaf>test</concat-function-leaf>"
				+ "  </must-string-function-validation>"
				+ "</whenMustContainer>";

		// should fail
		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(1,ncResponse.getErrors().size());
		assertEquals("/whenMust:whenMustContainer/whenMust:must-string-function-validation/whenMust:concat-function-leaf", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("Violate must constraints: concat(../string1,../string2) = 'AB'", ncResponse.getErrors().get(0).getErrorMessage());


	}

	@Test
	public void testMustConcatFunction_2() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-string-function-validation>"
				+ "    <string1>A</string1>"
				+ "    <string2>B</string2>"
				+ "    <concat-function-leaf>test</concat-function-leaf>"
				+ "  </must-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		String response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <must-string-function-validation>"
						+ "    <string1>A</string1>"
						+ "    <string2>B</string2>"
						+ "    <concat-function-leaf>test</concat-function-leaf>"
						+ "  </must-string-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
				;
		verifyGet(response);


		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-string-function-validation>"
				+ "    <string1>A</string1>"
				+ "    <string2>C</string2>"
				+ "  </must-string-function-validation>"
				+ "</whenMustContainer>";

		// should fail
		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(1,ncResponse.getErrors().size());
		assertEquals("/whenMust:whenMustContainer/whenMust:must-string-function-validation/whenMust:concat-function-leaf", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("Violate must constraints: concat(../string1,../string2) = 'AB'", ncResponse.getErrors().get(0).getErrorMessage());
	}

	@Test
	public void testMustStartsWithFunction_1() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-string-function-validation>"
				+ "    <string1>DSL1</string1>"
				+ "    <starts-with-function-leaf>test</starts-with-function-leaf>"
				+ "  </must-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true); 

		String response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <must-string-function-validation>"
						+ "    <string1>DSL1</string1>"
						+ "    <starts-with-function-leaf>test</starts-with-function-leaf>"
						+ "  </must-string-function-validation>"	
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response); 

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-string-function-validation>"
				+ "    <string1>ITF1</string1>"
				+ "    <starts-with-function-leaf>test</starts-with-function-leaf>"
				+ "  </must-string-function-validation>"
				+ "</whenMustContainer>";

		// should fail
		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(1,ncResponse.getErrors().size());
		assertEquals("/whenMust:whenMustContainer/whenMust:must-string-function-validation/whenMust:starts-with-function-leaf", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("Violate must constraints: starts-with(../string1,'DSL')", ncResponse.getErrors().get(0).getErrorMessage());

	}

	@Test
	public void testMustStartsWithFunction_2() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-string-function-validation>"
				+ "    <string1>DSL1</string1>"
				+ "    <starts-with-function-leaf>test</starts-with-function-leaf>"
				+ "  </must-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		String response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <must-string-function-validation>"
						+ "    <string1>DSL1</string1>"
						+ "    <starts-with-function-leaf>test</starts-with-function-leaf>"
						+ "  </must-string-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
				;
		verifyGet(response);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-string-function-validation>"
				+ "    <string1>ITF1</string1>"
				+ "  </must-string-function-validation>"
				+ "</whenMustContainer>";

		// should fail
		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(1,ncResponse.getErrors().size());
		assertEquals("/whenMust:whenMustContainer/whenMust:must-string-function-validation/whenMust:starts-with-function-leaf", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("Violate must constraints: starts-with(../string1,'DSL')", ncResponse.getErrors().get(0).getErrorMessage());
	}

	@Test
	public void testMustContainsFunction_1() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-string-function-validation>"
				+ "    <string1>TestDSL1</string1>"
				+ "    <contains-function-leaf>test</contains-function-leaf>"
				+ "  </must-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true); 

		String response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <must-string-function-validation>"
						+ "    <string1>TestDSL1</string1>"
						+ "    <contains-function-leaf>test</contains-function-leaf>"
						+ "  </must-string-function-validation>"	
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response); 

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-string-function-validation>"
				+ "    <string1>TestITF1</string1>"
				+ "    <contains-function-leaf>test</contains-function-leaf>"
				+ "  </must-string-function-validation>"
				+ "</whenMustContainer>";

		//should fail
		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(1,ncResponse.getErrors().size());
		assertEquals("/whenMust:whenMustContainer/whenMust:must-string-function-validation/whenMust:contains-function-leaf", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("Violate must constraints: contains(../string1,'DSL')", ncResponse.getErrors().get(0).getErrorMessage());

	}

	@Test
	public void testMustContainsFunction_2() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-string-function-validation>"
				+ "    <string1>TestDSL1</string1>"
				+ "    <contains-function-leaf>test</contains-function-leaf>"
				+ "  </must-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		String response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <must-string-function-validation>"
						+ "    <string1>TestDSL1</string1>"
						+ "    <contains-function-leaf>test</contains-function-leaf>"
						+ "  </must-string-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
				;
		verifyGet(response);


		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-string-function-validation>"
				+ "    <string1>TestITF1</string1>"
				+ "  </must-string-function-validation>"
				+ "</whenMustContainer>";

		//should fail
		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(1,ncResponse.getErrors().size());
		assertEquals("/whenMust:whenMustContainer/whenMust:must-string-function-validation/whenMust:contains-function-leaf", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("Violate must constraints: contains(../string1,'DSL')", ncResponse.getErrors().get(0).getErrorMessage());
	}

	@Test
	public void testMustSubstringFunction_1() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-string-function-validation>"
				+ "    <string1>12345</string1>"
				+ "    <substring-function-leaf>test</substring-function-leaf>"
				+ "  </must-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);    	

		String response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <must-string-function-validation>"
						+ "    <string1>12345</string1>"
						+ "    <substring-function-leaf>test</substring-function-leaf>"
						+ "  </must-string-function-validation>"	
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response); 

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-string-function-validation>"
				+ "    <string1>ABCDE</string1>"
				+ "    <substring-function-leaf>test</substring-function-leaf>"
				+ "  </must-string-function-validation>"
				+ "</whenMustContainer>";

		// should fail
		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(1,ncResponse.getErrors().size());
		assertEquals("/whenMust:whenMustContainer/whenMust:must-string-function-validation/whenMust:substring-function-leaf", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("Violate must constraints: substring(../string1,2,3) = '234'", ncResponse.getErrors().get(0).getErrorMessage());

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-string-function-validation>"
				+ "    <string1>ABCDE</string1>"
				+ "  </must-string-function-validation>"
				+ "</whenMustContainer>";

	}

	@Test
	public void testMustSubstringFunction_2() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-string-function-validation>"
				+ "    <string1>12345</string1>"
				+ "    <substring-function-leaf>test</substring-function-leaf>"
				+ "  </must-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		String response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <must-string-function-validation>"
						+ "    <string1>12345</string1>"
						+ "    <substring-function-leaf>test</substring-function-leaf>"
						+ "  </must-string-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
				;
		verifyGet(response);


		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-string-function-validation>"
				+ "    <string1>ABCDE</string1>"
				+ "  </must-string-function-validation>"
				+ "</whenMustContainer>";

		// should fail
		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(1,ncResponse.getErrors().size());
		assertEquals("/whenMust:whenMustContainer/whenMust:must-string-function-validation/whenMust:substring-function-leaf", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("Violate must constraints: substring(../string1,2,3) = '234'", ncResponse.getErrors().get(0).getErrorMessage());


	}

	@Test
	public void testMustSubstringFunction_3() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-string-function-validation>"
				+ "    <string1>12345</string1>"
				+ "    <substring-function-leaf>test</substring-function-leaf>"
				+ "  </must-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		String response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <must-string-function-validation>"
						+ "    <string1>12345</string1>"
						+ "    <substring-function-leaf>test</substring-function-leaf>"
						+ "  </must-string-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
				;
		verifyGet(response);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-string-function-validation>"
				+ "    <string1>ABCDE</string1>"
				+ "    <substring-function-leaf>test</substring-function-leaf>"
				+ "  </must-string-function-validation>"
				+ "</whenMustContainer>";


		// with no-length arg
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-string-function-validation>"
				+ "    <string1>12345</string1>"
				+ "    <substring-function-leaf-no-length-arg>test</substring-function-leaf-no-length-arg>"
				+ "  </must-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <must-string-function-validation>"
						+ "    <string1>12345</string1>"
						+ "    <substring-function-leaf-no-length-arg>test</substring-function-leaf-no-length-arg>"
						+ "    <substring-function-leaf>test</substring-function-leaf>"
						+ "  </must-string-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
		;
		verifyGet(response);

		// with decimal arg
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-string-function-validation>"
				+ "    <string1>12345</string1>"
				+ "    <substring-function-with-decimal-args>test</substring-function-with-decimal-args>"
				+ "  </must-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <must-string-function-validation>"
						+ "    <string1>12345</string1>"
						+ "    <substring-function-leaf-no-length-arg>test</substring-function-leaf-no-length-arg>"
						+ "    <substring-function-leaf>test</substring-function-leaf>"
						+ "    <substring-function-with-decimal-args>test</substring-function-with-decimal-args>"
						+ "  </must-string-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
		;
		verifyGet(response);
	}

	@Test
	public void testMustSubstringBeforeFunction_1() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-string-function-validation>"
				+ "    <string1>1999/04/01</string1>"
				+ "    <substring-before-function-leaf>test</substring-before-function-leaf>"
				+ "  </must-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);    

		String response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <must-string-function-validation>"
						+ "    <string1>1999/04/01</string1>"
						+ "    <substring-before-function-leaf>test</substring-before-function-leaf>"
						+ "  </must-string-function-validation>"	
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response); 

		// not contains returns empty string
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-string-function-validation>"
				+ "    <string1>1999/04/01</string1>"
				+ "    <substring-before-function-not-exists>test</substring-before-function-not-exists>"
				+ "  </must-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);    

		response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <must-string-function-validation>"
						+ "    <string1>1999/04/01</string1>"
						+ "    <substring-before-function-leaf>test</substring-before-function-leaf>"
						+ "    <substring-before-function-not-exists>test</substring-before-function-not-exists>"
						+ "  </must-string-function-validation>"	
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response); 

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-string-function-validation>"
				+ "    <string1>2000/10/05</string1>"
				+ "    <substring-before-function-leaf>test</substring-before-function-leaf>"
				+ "  </must-string-function-validation>"
				+ "</whenMustContainer>";

		// should fail
		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(1,ncResponse.getErrors().size());
		assertEquals("/whenMust:whenMustContainer/whenMust:must-string-function-validation/whenMust:substring-before-function-leaf", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("Violate must constraints: substring-before(../string1,'/') = '1999'", ncResponse.getErrors().get(0).getErrorMessage());

	}

	@Test
	public void testMustSubstringBeforeFunction_2() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-string-function-validation>"
				+ "    <string1>1999/04/01</string1>"
				+ "    <substring-before-function-leaf>test</substring-before-function-leaf>"
				+ "  </must-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		String response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <must-string-function-validation>"
						+ "    <string1>1999/04/01</string1>"
						+ "    <substring-before-function-leaf>test</substring-before-function-leaf>"
						+ "  </must-string-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
				;
		verifyGet(response);

		// not contains returns empty string
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-string-function-validation>"
				+ "    <string1>1999/04/01</string1>"
				+ "    <substring-before-function-not-exists>test</substring-before-function-not-exists>"
				+ "  </must-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <must-string-function-validation>"
						+ "    <string1>1999/04/01</string1>"
						+ "    <substring-before-function-leaf>test</substring-before-function-leaf>"
						+ "    <substring-before-function-not-exists>test</substring-before-function-not-exists>"
						+ "  </must-string-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
		;
		verifyGet(response);


		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-string-function-validation>"
				+ "    <string1>2000/10/05</string1>"
				+ "  </must-string-function-validation>"
				+ "</whenMustContainer>";

		// should fail
		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(1,ncResponse.getErrors().size());
		assertEquals("/whenMust:whenMustContainer/whenMust:must-string-function-validation/whenMust:substring-before-function-leaf", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("Violate must constraints: substring-before(../string1,'/') = '1999'", ncResponse.getErrors().get(0).getErrorMessage());
	}

	@Test
	public void testMustSubstringAfterFunction_1() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-string-function-validation>"
				+ "    <string1>1999/04/01</string1>"
				+ "    <substring-after-function-leaf>test</substring-after-function-leaf>"
				+ "  </must-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);    	

		String response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <must-string-function-validation>"
						+ "    <string1>1999/04/01</string1>"
						+ "    <substring-after-function-leaf>test</substring-after-function-leaf>"
						+ "  </must-string-function-validation>"	
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response); 

		// not contains returns empty string
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-string-function-validation>"
				+ "    <string1>1999/04/01</string1>"
				+ "    <substring-after-function-not-exists>test</substring-after-function-not-exists>"
				+ "  </must-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);    	

		response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <must-string-function-validation>"
						+ "    <string1>1999/04/01</string1>"
						+ "    <substring-after-function-not-exists>test</substring-after-function-not-exists>"
						+ "    <substring-after-function-leaf>test</substring-after-function-leaf>"
						+ "  </must-string-function-validation>"	
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response); 

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-string-function-validation>"
				+ "    <string1>2000/10/05</string1>"
				+ "    <substring-after-function-leaf>test</substring-after-function-leaf>"
				+ "  </must-string-function-validation>"
				+ "</whenMustContainer>";
		// should fail
		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(1,ncResponse.getErrors().size());
		assertEquals("/whenMust:whenMustContainer/whenMust:must-string-function-validation/whenMust:substring-after-function-leaf", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("Violate must constraints: substring-after(../string1,'/') = '04/01'", ncResponse.getErrors().get(0).getErrorMessage());

	}

	@Test
	public void testMustSubstringAfterFunction_2() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-string-function-validation>"
				+ "    <string1>1999/04/01</string1>"
				+ "    <substring-after-function-leaf>test</substring-after-function-leaf>"
				+ "  </must-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		String response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <must-string-function-validation>"
						+ "    <string1>1999/04/01</string1>"
						+ "    <substring-after-function-leaf>test</substring-after-function-leaf>"
						+ "  </must-string-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
				;
		verifyGet(response);

		// not contains returns empty string
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-string-function-validation>"
				+ "    <string1>1999/04/01</string1>"
				+ "    <substring-after-function-not-exists>test</substring-after-function-not-exists>"
				+ "  </must-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <must-string-function-validation>"
						+ "    <string1>1999/04/01</string1>"
						+ "    <substring-after-function-not-exists>test</substring-after-function-not-exists>"
						+ "    <substring-after-function-leaf>test</substring-after-function-leaf>"
						+ "  </must-string-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
		;
		verifyGet(response);


		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-string-function-validation>"
				+ "    <string1>2000/10/05</string1>"
				+ "  </must-string-function-validation>"
				+ "</whenMustContainer>";
		// should fail
		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(1,ncResponse.getErrors().size());
		assertEquals("/whenMust:whenMustContainer/whenMust:must-string-function-validation/whenMust:substring-after-function-leaf", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("Violate must constraints: substring-after(../string1,'/') = '04/01'", ncResponse.getErrors().get(0).getErrorMessage());
	}

	@Test
	public void testMustStringLengthFunction_1() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-string-function-validation>"
				+ "    <string1>12345</string1>"
				+ "    <string-length-function-leaf>test</string-length-function-leaf>"
				+ "  </must-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);    	

		String response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <must-string-function-validation>"
						+ "    <string1>12345</string1>"
						+ "    <string-length-function-leaf>test</string-length-function-leaf>"
						+ "  </must-string-function-validation>"	
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response); 

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-string-function-validation>"
				+ "    <string1>1234</string1>"
				+ "    <string-length-function-leaf>test</string-length-function-leaf>"
				+ "  </must-string-function-validation>"
				+ "</whenMustContainer>";
		// should fail
		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(1,ncResponse.getErrors().size());
		assertEquals("/whenMust:whenMustContainer/whenMust:must-string-function-validation/whenMust:string-length-function-leaf", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("Violate must constraints: string-length(../string1) = 5", ncResponse.getErrors().get(0).getErrorMessage());

	}

	@Test
	public void testMustStringLengthFunction_2() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-string-function-validation>"
				+ "    <string1>12345</string1>"
				+ "    <string-length-function-leaf>test</string-length-function-leaf>"
				+ "  </must-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		String response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <must-string-function-validation>"
						+ "    <string1>12345</string1>"
						+ "    <string-length-function-leaf>test</string-length-function-leaf>"
						+ "  </must-string-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
				;
		verifyGet(response);

//		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
//				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
//				+ "  <must-string-function-validation>"
//				+ "    <string1>1234</string1>"
//				+ "    <string-length-function-leaf>test</string-length-function-leaf>"
//				+ "  </must-string-function-validation>"
//				+ "</whenMustContainer>";
//		// should fail
//		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
//		assertEquals(1,ncResponse.getErrors().size());
//		assertEquals("/whenMust:whenMustContainer/whenMust:must-string-function-validation/whenMust:string-length-function-leaf", ncResponse.getErrors().get(0).getErrorPath());
//		assertEquals("Violate must constraints: string-length(../string1) = 5", ncResponse.getErrors().get(0).getErrorMessage());

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-string-function-validation>"
				+ "    <string1>1234</string1>"
				+ "  </must-string-function-validation>"
				+ "</whenMustContainer>";
		// should fail
		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(1,ncResponse.getErrors().size());
		assertEquals("/whenMust:whenMustContainer/whenMust:must-string-function-validation/whenMust:string-length-function-leaf", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("Violate must constraints: string-length(../string1) = 5", ncResponse.getErrors().get(0).getErrorMessage());

	}

	@Test
	public void testMustStringLengthFunction_3() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-string-function-validation>"
				+ "    <string1>12345</string1>"
				+ "    <string-length-function-leaf>test</string-length-function-leaf>"
				+ "  </must-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		String response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <must-string-function-validation>"
						+ "    <string1>12345</string1>"
						+ "    <string-length-function-leaf>test</string-length-function-leaf>"
						+ "  </must-string-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
				;
		verifyGet(response);

//		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
//				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
//				+ "  <must-string-function-validation>"
//				+ "    <string1>1234</string1>"
//				+ "    <string-length-function-leaf>test</string-length-function-leaf>"
//				+ "  </must-string-function-validation>"
//				+ "</whenMustContainer>";
//		// should fail
//		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
//		assertEquals(1,ncResponse.getErrors().size());
//		assertEquals("/whenMust:whenMustContainer/whenMust:must-string-function-validation/whenMust:string-length-function-leaf", ncResponse.getErrors().get(0).getErrorPath());
//		assertEquals("Violate must constraints: string-length(../string1) = 5", ncResponse.getErrors().get(0).getErrorMessage());
//
//		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
//				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
//				+ "  <must-string-function-validation>"
//				+ "    <string1>1234</string1>"
//				+ "  </must-string-function-validation>"
//				+ "</whenMustContainer>";
//		// should fail
//		ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
//		assertEquals(1,ncResponse.getErrors().size());
//		assertEquals("/whenMust:whenMustContainer/whenMust:must-string-function-validation/whenMust:string-length-function-leaf", ncResponse.getErrors().get(0).getErrorPath());
//		assertEquals("Violate must constraints: string-length(../string1) = 5", ncResponse.getErrors().get(0).getErrorMessage());

		//no-arg string-length function
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-string-function-validation>"
				+ "    <no-arg-string-length-function-leaf>test</no-arg-string-length-function-leaf>"
				+ "  </must-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);
	}

	@Test
	public void testMustNormalizeSpaceFunction_1() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-string-function-validation>"
				+ "    <string1>  ITF  1  </string1>"
				+ "    <normalize-space-function-leaf>test</normalize-space-function-leaf>"
				+ "  </must-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);    

		String response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <must-string-function-validation>"
						+ "    <string1>  ITF  1  </string1>"
						+ "    <normalize-space-function-leaf>test</normalize-space-function-leaf>"
						+ "  </must-string-function-validation>"	
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response); 

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-string-function-validation>"
				+ "    <string1>  ITF  12  </string1>"
				+ "    <normalize-space-function-leaf>test</normalize-space-function-leaf>"
				+ "  </must-string-function-validation>"
				+ "</whenMustContainer>";
		// should fail
		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(1,ncResponse.getErrors().size());
		assertEquals("/whenMust:whenMustContainer/whenMust:must-string-function-validation/whenMust:normalize-space-function-leaf", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("Violate must constraints: normalize-space(../string1) = 'ITF 1'", ncResponse.getErrors().get(0).getErrorMessage());

	}

	@Test
	public void testMustNormalizeSpaceFunction_2() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-string-function-validation>"
				+ "    <string1>  ITF  1  </string1>"
				+ "    <normalize-space-function-leaf>test</normalize-space-function-leaf>"
				+ "  </must-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		String response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <must-string-function-validation>"
						+ "    <string1>  ITF  1  </string1>"
						+ "    <normalize-space-function-leaf>test</normalize-space-function-leaf>"
						+ "  </must-string-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
				;
		verifyGet(response);


		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-string-function-validation>"
				+ "    <string1>  ITF  12  </string1>"
				+ "  </must-string-function-validation>"
				+ "</whenMustContainer>";

		// should fail
		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(1,ncResponse.getErrors().size());
		assertEquals("/whenMust:whenMustContainer/whenMust:must-string-function-validation/whenMust:normalize-space-function-leaf", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("Violate must constraints: normalize-space(../string1) = 'ITF 1'", ncResponse.getErrors().get(0).getErrorMessage());

	}

	@Test
	public void testMustNormalizeSpaceFunction_3() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-string-function-validation>"
				+ "    <string1>  ITF  1  </string1>"
				+ "    <normalize-space-function-leaf>test</normalize-space-function-leaf>"
				+ "  </must-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		String response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <must-string-function-validation>"
						+ "    <string1>  ITF  1  </string1>"
						+ "    <normalize-space-function-leaf>test</normalize-space-function-leaf>"
						+ "  </must-string-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
				;
		verifyGet(response);


		//no-arg normalize-space function
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-string-function-validation>"
				+ "    <no-arg-normalize-space-function-leaf> test </no-arg-normalize-space-function-leaf>"
				+ "  </must-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);
	}

	@Test
	public void testMustTranslateFunction_1() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-string-function-validation>"
				+ "    <string1>-bar-</string1>"
				+ "    <translate-function-leaf>test</translate-function-leaf>"
				+ "  </must-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);    	

		String response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <must-string-function-validation>"
						+ "    <string1>-bar-</string1>"
						+ "    <translate-function-leaf>test</translate-function-leaf>"
						+ "  </must-string-function-validation>"	
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response); 

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-string-function-validation>"
				+ "    <string1>dsl</string1>"
				+ "    <translate-function-leaf>test</translate-function-leaf>"
				+ "  </must-string-function-validation>"
				+ "</whenMustContainer>";
		// should fail
		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(1,ncResponse.getErrors().size());
		assertEquals("/whenMust:whenMustContainer/whenMust:must-string-function-validation/whenMust:translate-function-leaf", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("Violate must constraints: translate(../string1,'abc-','ABC') = 'BAr'", ncResponse.getErrors().get(0).getErrorMessage());

	}

	@Test
	public void testMustTranslateFunction_2() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-string-function-validation>"
				+ "    <string1>-bar-</string1>"
				+ "    <translate-function-leaf>test</translate-function-leaf>"
				+ "  </must-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		String response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <must-string-function-validation>"
						+ "    <string1>-bar-</string1>"
						+ "    <translate-function-leaf>test</translate-function-leaf>"
						+ "  </must-string-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
				;
		verifyGet(response);


		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-string-function-validation>"
				+ "    <string1>dsl</string1>"
				+ "  </must-string-function-validation>"
				+ "</whenMustContainer>";
		// should fail
		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(1,ncResponse.getErrors().size());
		assertEquals("/whenMust:whenMustContainer/whenMust:must-string-function-validation/whenMust:translate-function-leaf", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("Violate must constraints: translate(../string1,'abc-','ABC') = 'BAr'", ncResponse.getErrors().get(0).getErrorMessage());
	}

	@Test
	public void testWhenStringFunction_1() throws Exception {
		getModelNode();

		//create number1
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-string-function-validation>"
				+ "    <number1>42</number1>"
				+ "    <string-function-leaf>test</string-function-leaf>"
				+ "  </when-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);   

		String response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <when-string-function-validation>"
						+ "    <number1>42</number1>"
						+ "    <string-function-leaf>test</string-function-leaf>"
						+ "  </when-string-function-validation>"	
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response); 

		//non-existent leaf string function
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-string-function-validation>"
				+ "    <string2></string2>"
				+ "    <string-function-non-existent-leaf>test</string-function-non-existent-leaf>"
				+ "  </when-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <when-string-function-validation>"
						+ "    <number1>42</number1>"
						+ "    <string2></string2>"
						+ "    <string-function-leaf>test</string-function-leaf>"
						+ "    <string-function-non-existent-leaf>test</string-function-non-existent-leaf>"
						+ "  </when-string-function-validation>"	
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response); 

		//string function with arg as boolean true
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-string-function-validation>"
				+ "    <string-function-boolean-true-leaf>test</string-function-boolean-true-leaf>"
				+ "  </when-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		//string function with arg as boolean false
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-string-function-validation>"
				+ "    <string-function-boolean-false-leaf>test</string-function-boolean-false-leaf>"
				+ "  </when-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <when-string-function-validation>"
						+ "    <string-function-boolean-false-leaf>test</string-function-boolean-false-leaf>"
						+ "    <string-function-boolean-true-leaf>test</string-function-boolean-true-leaf>"
						+ "    <number1>42</number1>"
						+ "    <string2></string2>"
						+ "    <string-function-leaf>test</string-function-leaf>"
						+ "    <string-function-non-existent-leaf>test</string-function-non-existent-leaf>"
						+ "  </when-string-function-validation>"	
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response); 

		/*		//non-existent leaf-list string function
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-string-function-validation>"
				+ "    <string-function-refers-leaflist>test</string-function-refers-leaflist>"
				+ "  </when-string-function-validation>"
				+ "</whenMustContainer>";
		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(1,ncResponse.getErrors().size());
		assertEquals("/whenMust:whenMustContainer/whenMust:when-string-function-validation/whenMust:string-function-refers-leaflist", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("Violate when constraints: string(../string3) != ''", ncResponse.getErrors().get(0).getErrorMessage());

		//create leaf-list string function
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-string-function-validation>"
				+ "    <string3>test1</string3>"
				+ "    <string3>test2</string3>"
				+ "    <string-function-refers-leaflist>test</string-function-refers-leaflist>"
				+ "  </when-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, false);*/

		//existent leaf string function
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-string-function-validation>"
				+ "    <string2>test</string2>"
				+ "    <string-function-non-existent-leaf>test</string-function-non-existent-leaf>"
				+ "  </when-string-function-validation>"
				+ "</whenMustContainer>";
		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(1,ncResponse.getErrors().size());
		assertEquals("/whenMust:whenMustContainer/whenMust:when-string-function-validation/whenMust:string-function-non-existent-leaf", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("Violate when constraints: string(../string2) = ''", ncResponse.getErrors().get(0).getErrorMessage());

	}

	@Test
	public void testWhenStringFunction_2() throws Exception {
		getModelNode();

		//create number1
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-string-function-validation>"
				+ "    <number1>42</number1>"
				+ "    <string-function-leaf>test</string-function-leaf>"
				+ "  </when-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		String response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <when-string-function-validation>"
						+ "    <number1>42</number1>"
						+ "    <string-function-leaf>test</string-function-leaf>"
						+ "  </when-string-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
				;
		verifyGet(response);

		//non-existent leaf string function
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-string-function-validation>"
				+ "    <string2></string2>"
				+ "    <string-function-non-existent-leaf>test</string-function-non-existent-leaf>"
				+ "  </when-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <when-string-function-validation>"
						+ "    <number1>42</number1>"
						+ "    <string2></string2>"
						+ "    <string-function-leaf>test</string-function-leaf>"
						+ "    <string-function-non-existent-leaf>test</string-function-non-existent-leaf>"
						+ "  </when-string-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
		;
		verifyGet(response);

		//string function with arg as boolean true
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-string-function-validation>"
				+ "    <string-function-boolean-true-leaf>test</string-function-boolean-true-leaf>"
				+ "  </when-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		//string function with arg as boolean false
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-string-function-validation>"
				+ "    <string-function-boolean-false-leaf>test</string-function-boolean-false-leaf>"
				+ "  </when-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <when-string-function-validation>"
						+ "    <string-function-boolean-false-leaf>test</string-function-boolean-false-leaf>"
						+ "    <string-function-boolean-true-leaf>test</string-function-boolean-true-leaf>"
						+ "    <number1>42</number1>"
						+ "    <string2></string2>"
						+ "    <string-function-leaf>test</string-function-leaf>"
						+ "    <string-function-non-existent-leaf>test</string-function-non-existent-leaf>"
						+ "  </when-string-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
		;
		verifyGet(response);

		/*		//non-existent leaf-list string function
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-string-function-validation>"
				+ "    <string-function-refers-leaflist>test</string-function-refers-leaflist>"
				+ "  </when-string-function-validation>"
				+ "</whenMustContainer>";
		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(1,ncResponse.getErrors().size());
		assertEquals("/whenMust:whenMustContainer/whenMust:when-string-function-validation/whenMust:string-function-refers-leaflist", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("Violate when constraints: string(../string3) != ''", ncResponse.getErrors().get(0).getErrorMessage());

		//create leaf-list string function
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-string-function-validation>"
				+ "    <string3>test1</string3>"
				+ "    <string3>test2</string3>"
				+ "    <string-function-refers-leaflist>test</string-function-refers-leaflist>"
				+ "  </when-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, false);*/


		// update number1 impacts string-function-leaf and the impacted node exists in the request
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-string-function-validation>"
				+ "    <number1>41</number1>"
				+ "    <string-function-leaf>test</string-function-leaf>"
				+ "  </when-string-function-validation>"
				+ "</whenMustContainer>";
		// should fail
		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(1,ncResponse.getErrors().size());
		assertEquals("/whenMust:whenMustContainer/whenMust:when-string-function-validation/whenMust:string-function-leaf", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("Violate when constraints: string(../number1) = '42'", ncResponse.getErrors().get(0).getErrorMessage());

	}

	@Test
	public void testWhenStringFunction_3() throws Exception {
		getModelNode();

		//create number1
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-string-function-validation>"
				+ "    <number1>42</number1>"
				+ "    <string-function-leaf>test</string-function-leaf>"
				+ "  </when-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		String response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <when-string-function-validation>"
						+ "    <number1>42</number1>"
						+ "    <string-function-leaf>test</string-function-leaf>"
						+ "  </when-string-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
				;
		verifyGet(response);

		//non-existent leaf string function
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-string-function-validation>"
				+ "    <string2></string2>"
				+ "    <string-function-non-existent-leaf>test</string-function-non-existent-leaf>"
				+ "  </when-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <when-string-function-validation>"
						+ "    <number1>42</number1>"
						+ "    <string2></string2>"
						+ "    <string-function-leaf>test</string-function-leaf>"
						+ "    <string-function-non-existent-leaf>test</string-function-non-existent-leaf>"
						+ "  </when-string-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
		;
		verifyGet(response);

		//string function with arg as boolean true
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-string-function-validation>"
				+ "    <string-function-boolean-true-leaf>test</string-function-boolean-true-leaf>"
				+ "  </when-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		//string function with arg as boolean false
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-string-function-validation>"
				+ "    <string-function-boolean-false-leaf>test</string-function-boolean-false-leaf>"
				+ "  </when-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <when-string-function-validation>"
						+ "    <string-function-boolean-false-leaf>test</string-function-boolean-false-leaf>"
						+ "    <string-function-boolean-true-leaf>test</string-function-boolean-true-leaf>"
						+ "    <number1>42</number1>"
						+ "    <string2></string2>"
						+ "    <string-function-leaf>test</string-function-leaf>"
						+ "    <string-function-non-existent-leaf>test</string-function-non-existent-leaf>"
						+ "  </when-string-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
		;
		verifyGet(response);

		/*		//non-existent leaf-list string function
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-string-function-validation>"
				+ "    <string-function-refers-leaflist>test</string-function-refers-leaflist>"
				+ "  </when-string-function-validation>"
				+ "</whenMustContainer>";
		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(1,ncResponse.getErrors().size());
		assertEquals("/whenMust:whenMustContainer/whenMust:when-string-function-validation/whenMust:string-function-refers-leaflist", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("Violate when constraints: string(../string3) != ''", ncResponse.getErrors().get(0).getErrorMessage());

		//create leaf-list string function
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-string-function-validation>"
				+ "    <string3>test1</string3>"
				+ "    <string3>test2</string3>"
				+ "    <string-function-refers-leaflist>test</string-function-refers-leaflist>"
				+ "  </when-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, false);*/


		// update number1 impacts string-function-leaf and the impacted node not exists in the request
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-string-function-validation>"
				+ "    <number1>41</number1>"
				+ "  </when-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <when-string-function-validation>"
						+ "    <string-function-boolean-false-leaf>test</string-function-boolean-false-leaf>"
						+ "    <string-function-boolean-true-leaf>test</string-function-boolean-true-leaf>"
						+ "    <number1>41</number1>"
						+ "    <string2></string2>"
						+ "    <string-function-non-existent-leaf>test</string-function-non-existent-leaf>"
						+ "  </when-string-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
		;
		verifyGet(response);
	}

	@Test
	public void testWhenConcatFunction_1() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-string-function-validation>"
				+ "    <string1>A</string1>"
				+ "    <string2>B</string2>"
				+ "    <concat-function-leaf>test</concat-function-leaf>"
				+ "  </when-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true); 

		String response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <when-string-function-validation>"
						+ "    <string1>A</string1>"
						+ "    <string2>B</string2>"
						+ "    <concat-function-leaf>test</concat-function-leaf>"
						+ "  </when-string-function-validation>"	
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response); 

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-string-function-validation>"
				+ "    <string1>A</string1>"
				+ "    <string2>C</string2>"
				+ "    <concat-function-leaf>test</concat-function-leaf>"
				+ "  </when-string-function-validation>"
				+ "</whenMustContainer>";

		// should fail
		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(1,ncResponse.getErrors().size());
		assertEquals("/whenMust:whenMustContainer/whenMust:when-string-function-validation/whenMust:concat-function-leaf", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("Violate when constraints: concat(../string1, ../string2) = 'AB'", ncResponse.getErrors().get(0).getErrorMessage());

	}

	@Test
	public void testWhenConcatFunction_2() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-string-function-validation>"
				+ "    <string1>A</string1>"
				+ "    <string2>B</string2>"
				+ "    <concat-function-leaf>test</concat-function-leaf>"
				+ "  </when-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		String response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <when-string-function-validation>"
						+ "    <string1>A</string1>"
						+ "    <string2>B</string2>"
						+ "    <concat-function-leaf>test</concat-function-leaf>"
						+ "  </when-string-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
				;
		verifyGet(response);

//		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
//				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
//				+ "  <when-string-function-validation>"
//				+ "    <string1>A</string1>"
//				+ "    <string2>C</string2>"
//				+ "    <concat-function-leaf>test</concat-function-leaf>"
//				+ "  </when-string-function-validation>"
//				+ "</whenMustContainer>";
//
//		// should fail
//		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
//		assertEquals(1,ncResponse.getErrors().size());
//		assertEquals("/whenMust:whenMustContainer/whenMust:when-string-function-validation/whenMust:concat-function-leaf", ncResponse.getErrors().get(0).getErrorPath());
//		assertEquals("Violate when constraints: concat(../string1, ../string2) = 'AB'", ncResponse.getErrors().get(0).getErrorMessage());


		//update string & string2 impacts concat-function-leaf
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-string-function-validation>"
				+ "    <string1>A</string1>"
				+ "    <string2>C</string2>"
				+ "  </when-string-function-validation>"
				+ "</whenMustContainer>";

		editConfig(m_server, m_clientInfo, requestXml1, true);
		response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <when-string-function-validation>"
						+ "    <string1>A</string1>"
						+ "    <string2>C</string2>"
						+ "  </when-string-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
		;
		verifyGet(response);
	}

	@Test
	public void testWhenStartsWithFunction_1() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-string-function-validation>"
				+ "    <string1>DSL1</string1>"
				+ "    <starts-with-function-leaf>test</starts-with-function-leaf>"
				+ "  </when-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true); 

		String response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <when-string-function-validation>"
						+ "    <string1>DSL1</string1>"
						+ "    <starts-with-function-leaf>test</starts-with-function-leaf>"
						+ "  </when-string-function-validation>"	
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response); 

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-string-function-validation>"
				+ "    <string1>ITF1</string1>"
				+ "    <starts-with-function-leaf>test</starts-with-function-leaf>"
				+ "  </when-string-function-validation>"
				+ "</whenMustContainer>";

		// should fail
		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(1,ncResponse.getErrors().size());
		assertEquals("/whenMust:whenMustContainer/whenMust:when-string-function-validation/whenMust:starts-with-function-leaf", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("Violate when constraints: starts-with(../string1, 'DSL')", ncResponse.getErrors().get(0).getErrorMessage());


	}

	@Test
	public void testWhenStartsWithFunction_2() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-string-function-validation>"
				+ "    <string1>DSL1</string1>"
				+ "    <starts-with-function-leaf>test</starts-with-function-leaf>"
				+ "  </when-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		String response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <when-string-function-validation>"
						+ "    <string1>DSL1</string1>"
						+ "    <starts-with-function-leaf>test</starts-with-function-leaf>"
						+ "  </when-string-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
				;
		verifyGet(response);

//		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
//				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
//				+ "  <when-string-function-validation>"
//				+ "    <string1>ITF1</string1>"
//				+ "    <starts-with-function-leaf>test</starts-with-function-leaf>"
//				+ "  </when-string-function-validation>"
//				+ "</whenMustContainer>";
//
//		// should fail
//		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
//		assertEquals(1,ncResponse.getErrors().size());
//		assertEquals("/whenMust:whenMustContainer/whenMust:when-string-function-validation/whenMust:starts-with-function-leaf", ncResponse.getErrors().get(0).getErrorPath());
//		assertEquals("Violate when constraints: starts-with(../string1, 'DSL')", ncResponse.getErrors().get(0).getErrorMessage());

		// update string1 impacts starts-with-function-leaf
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-string-function-validation>"
				+ "    <string1>ITF1</string1>"
				+ "  </when-string-function-validation>"
				+ "</whenMustContainer>";

		editConfig(m_server, m_clientInfo, requestXml1, true);
		response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <when-string-function-validation>"
						+ "    <string1>ITF1</string1>"
						+ "  </when-string-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
		;
		verifyGet(response);
	}

	@Test
	public void testWhenContainsFunction_1() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-string-function-validation>"
				+ "    <string1>TestDSL1</string1>"
				+ "    <contains-function-leaf>test</contains-function-leaf>"
				+ "  </when-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true); 

		String response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <when-string-function-validation>"
						+ "    <string1>TestDSL1</string1>"
						+ "    <contains-function-leaf>test</contains-function-leaf>"
						+ "  </when-string-function-validation>"	
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response); 

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-string-function-validation>"
				+ "    <string1>TestITF1</string1>"
				+ "    <contains-function-leaf>test</contains-function-leaf>"
				+ "  </when-string-function-validation>"
				+ "</whenMustContainer>";

		//should fail
		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(1,ncResponse.getErrors().size());
		assertEquals("/whenMust:whenMustContainer/whenMust:when-string-function-validation/whenMust:contains-function-leaf", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("Violate when constraints: contains(../string1, 'DSL')", ncResponse.getErrors().get(0).getErrorMessage());

	}

	@Test
	public void testWhenContainsFunction_2() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-string-function-validation>"
				+ "    <string1>TestDSL1</string1>"
				+ "    <contains-function-leaf>test</contains-function-leaf>"
				+ "  </when-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		String response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <when-string-function-validation>"
						+ "    <string1>TestDSL1</string1>"
						+ "    <contains-function-leaf>test</contains-function-leaf>"
						+ "  </when-string-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
				;
		verifyGet(response);

		// update string1 impacts contains-function-leaf
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-string-function-validation>"
				+ "    <string1>TestITF1</string1>"
				+ "  </when-string-function-validation>"
				+ "</whenMustContainer>";

		editConfig(m_server, m_clientInfo, requestXml1, true);
		response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <when-string-function-validation>"
						+ "    <string1>TestITF1</string1>"
						+ "  </when-string-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
		;
		verifyGet(response);
	}

	@Test
	public void testWhenSubstringFunction_1() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-string-function-validation>"
				+ "    <string1>12345</string1>"
				+ "    <substring-function-leaf>test</substring-function-leaf>"
				+ "  </when-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);    	

		String response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <when-string-function-validation>"
						+ "    <string1>12345</string1>"
						+ "    <substring-function-leaf>test</substring-function-leaf>"
						+ "  </when-string-function-validation>"	
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response); 

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-string-function-validation>"
				+ "    <string1>ABCDE</string1>"
				+ "    <substring-function-leaf>test</substring-function-leaf>"
				+ "  </when-string-function-validation>"
				+ "</whenMustContainer>";

		// should fail
		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(1,ncResponse.getErrors().size());
		assertEquals("/whenMust:whenMustContainer/whenMust:when-string-function-validation/whenMust:substring-function-leaf", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("Violate when constraints: substring(../string1, 2, 3) = '234'", ncResponse.getErrors().get(0).getErrorMessage());

	}

	@Test
	public void testWhenSubstringFunction_2() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-string-function-validation>"
				+ "    <string1>12345</string1>"
				+ "    <substring-function-leaf>test</substring-function-leaf>"
				+ "  </when-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		String response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <when-string-function-validation>"
						+ "    <string1>12345</string1>"
						+ "    <substring-function-leaf>test</substring-function-leaf>"
						+ "  </when-string-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
				;
		verifyGet(response);


		// update string1 impacts substring-function-leaf
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-string-function-validation>"
				+ "    <string1>ABCDE</string1>"
				+ "  </when-string-function-validation>"
				+ "</whenMustContainer>";

		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, true);
		response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <when-string-function-validation>"
						+ "    <string1>ABCDE</string1>"
						+ "  </when-string-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
		;
		verifyGet(response);

		// with no-length arg
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-string-function-validation>"
				+ "    <string1>12345</string1>"
				+ "    <substring-function-leaf-no-length-arg>test</substring-function-leaf-no-length-arg>"
				+ "  </when-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <when-string-function-validation>"
						+ "    <string1>12345</string1>"
						+ "    <substring-function-leaf-no-length-arg>test</substring-function-leaf-no-length-arg>"
						+ "  </when-string-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
		;
		verifyGet(response);

		// with decimal arg
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-string-function-validation>"
				+ "    <string1>12345</string1>"
				+ "    <substring-function-with-decimal-args>test</substring-function-with-decimal-args>"
				+ "  </when-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <when-string-function-validation>"
						+ "    <string1>12345</string1>"
						+ "    <substring-function-leaf-no-length-arg>test</substring-function-leaf-no-length-arg>"
						+ "    <substring-function-with-decimal-args>test</substring-function-with-decimal-args>"
						+ "  </when-string-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
		;
		verifyGet(response);
	}

	@Test
	public void testWhenSubstringBeforeFunction_1() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-string-function-validation>"
				+ "    <string1>1999/04/01</string1>"
				+ "    <substring-before-function-leaf>test</substring-before-function-leaf>"
				+ "  </when-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);    

		String response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <when-string-function-validation>"
						+ "    <string1>1999/04/01</string1>"
						+ "    <substring-before-function-leaf>test</substring-before-function-leaf>"
						+ "  </when-string-function-validation>"	
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response); 

		// not contains returns empty string
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-string-function-validation>"
				+ "    <string1>1999/04/01</string1>"
				+ "    <substring-before-function-not-exists>test</substring-before-function-not-exists>"
				+ "  </when-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);    

		response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <when-string-function-validation>"
						+ "    <string1>1999/04/01</string1>"
						+ "    <substring-before-function-leaf>test</substring-before-function-leaf>"
						+ "    <substring-before-function-not-exists>test</substring-before-function-not-exists>"
						+ "  </when-string-function-validation>"	
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response); 

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-string-function-validation>"
				+ "    <string1>2000/10/05</string1>"
				+ "    <substring-before-function-leaf>test</substring-before-function-leaf>"
				+ "  </when-string-function-validation>"
				+ "</whenMustContainer>";

		// should fail
		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(1,ncResponse.getErrors().size());
		assertEquals("/whenMust:whenMustContainer/whenMust:when-string-function-validation/whenMust:substring-before-function-leaf", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("Violate when constraints: substring-before(../string1, '/') = '1999'", ncResponse.getErrors().get(0).getErrorMessage());

	}

	@Test
	public void testWhenSubstringBeforeFunction_2() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-string-function-validation>"
				+ "    <string1>1999/04/01</string1>"
				+ "    <substring-before-function-leaf>test</substring-before-function-leaf>"
				+ "  </when-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		String response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <when-string-function-validation>"
						+ "    <string1>1999/04/01</string1>"
						+ "    <substring-before-function-leaf>test</substring-before-function-leaf>"
						+ "  </when-string-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
				;
		verifyGet(response);

		// not contains returns empty string
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-string-function-validation>"
				+ "    <string1>1999/04/01</string1>"
				+ "    <substring-before-function-not-exists>test</substring-before-function-not-exists>"
				+ "  </when-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <when-string-function-validation>"
						+ "    <string1>1999/04/01</string1>"
						+ "    <substring-before-function-leaf>test</substring-before-function-leaf>"
						+ "    <substring-before-function-not-exists>test</substring-before-function-not-exists>"
						+ "  </when-string-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
		;
		verifyGet(response);

		// update string1 impacts substring-before-function-leaf
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-string-function-validation>"
				+ "    <string1>2000/10/05</string1>"
				+ "  </when-string-function-validation>"
				+ "</whenMustContainer>";

		editConfig(m_server, m_clientInfo, requestXml1, true);
		response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <when-string-function-validation>"
						+ "    <string1>2000/10/05</string1>"
						+ "    <substring-before-function-not-exists>test</substring-before-function-not-exists>"
						+ "  </when-string-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
		;
		verifyGet(response);
	}

	@Test
	public void testWhenSubstringAfterFunction_1() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-string-function-validation>"
				+ "    <string1>1999/04/01</string1>"
				+ "    <substring-after-function-leaf>test</substring-after-function-leaf>"
				+ "  </when-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);    	

		String response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <when-string-function-validation>"
						+ "    <string1>1999/04/01</string1>"
						+ "    <substring-after-function-leaf>test</substring-after-function-leaf>"
						+ "  </when-string-function-validation>"	
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response); 

		// not contains returns empty string
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-string-function-validation>"
				+ "    <string1>1999/04/01</string1>"
				+ "    <substring-after-function-not-exists>test</substring-after-function-not-exists>"
				+ "  </when-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);    	

		response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <when-string-function-validation>"
						+ "    <string1>1999/04/01</string1>"
						+ "    <substring-after-function-not-exists>test</substring-after-function-not-exists>"
						+ "    <substring-after-function-leaf>test</substring-after-function-leaf>"
						+ "  </when-string-function-validation>"	
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response); 

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-string-function-validation>"
				+ "    <string1>2000/10/05</string1>"
				+ "    <substring-after-function-leaf>test</substring-after-function-leaf>"
				+ "  </when-string-function-validation>"
				+ "</whenMustContainer>";
		// should fail
		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(1,ncResponse.getErrors().size());
		assertEquals("/whenMust:whenMustContainer/whenMust:when-string-function-validation/whenMust:substring-after-function-leaf", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("Violate when constraints: substring-after(../string1, '/') = '04/01'", ncResponse.getErrors().get(0).getErrorMessage());

	}

	@Test
	public void testWhenSubstringAfterFunction_2() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-string-function-validation>"
				+ "    <string1>1999/04/01</string1>"
				+ "    <substring-after-function-leaf>test</substring-after-function-leaf>"
				+ "  </when-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		String response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <when-string-function-validation>"
						+ "    <string1>1999/04/01</string1>"
						+ "    <substring-after-function-leaf>test</substring-after-function-leaf>"
						+ "  </when-string-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
				;
		verifyGet(response);

		// not contains returns empty string
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-string-function-validation>"
				+ "    <string1>1999/04/01</string1>"
				+ "    <substring-after-function-not-exists>test</substring-after-function-not-exists>"
				+ "  </when-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <when-string-function-validation>"
						+ "    <string1>1999/04/01</string1>"
						+ "    <substring-after-function-not-exists>test</substring-after-function-not-exists>"
						+ "    <substring-after-function-leaf>test</substring-after-function-leaf>"
						+ "  </when-string-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
		;
		verifyGet(response);

		// update string1 impacts substring-after-function-leaf
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-string-function-validation>"
				+ "    <string1>2000/10/05</string1>"
				+ "  </when-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);
		response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <when-string-function-validation>"
						+ "    <string1>2000/10/05</string1>"
						+ "    <substring-after-function-not-exists>test</substring-after-function-not-exists>"
						+ "  </when-string-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
		;
		verifyGet(response);
	}

	@Test
	public void testWhenStringLengthFunction_1() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-string-function-validation>"
				+ "    <string1>12345</string1>"
				+ "    <string-length-function-leaf>test</string-length-function-leaf>"
				+ "  </when-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);    	

		String response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <when-string-function-validation>"
						+ "    <string1>12345</string1>"
						+ "    <string-length-function-leaf>test</string-length-function-leaf>"
						+ "  </when-string-function-validation>"	
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response); 

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-string-function-validation>"
				+ "    <string1>1234</string1>"
				+ "    <string-length-function-leaf>test</string-length-function-leaf>"
				+ "  </when-string-function-validation>"
				+ "</whenMustContainer>";
		// should fail
		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(1,ncResponse.getErrors().size());
		assertEquals("/whenMust:whenMustContainer/whenMust:when-string-function-validation/whenMust:string-length-function-leaf", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("Violate when constraints: string-length(../string1) = 5", ncResponse.getErrors().get(0).getErrorMessage());

	}

	@Test
	public void testWhenStringLengthFunction_2() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-string-function-validation>"
				+ "    <string1>12345</string1>"
				+ "    <string-length-function-leaf>test</string-length-function-leaf>"
				+ "  </when-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		String response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <when-string-function-validation>"
						+ "    <string1>12345</string1>"
						+ "    <string-length-function-leaf>test</string-length-function-leaf>"
						+ "  </when-string-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
				;
		verifyGet(response);

		// update string1 impacts string-length-function-leaf
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-string-function-validation>"
				+ "    <string1>1234</string1>"
				+ "  </when-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);
		response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <when-string-function-validation>"
						+ "    <string1>1234</string1>"
						+ "  </when-string-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
		;
		verifyGet(response);
	}

	@Test
	public void testWhenNormalizeSpaceFunction_1() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-string-function-validation>"
				+ "    <string1>  ITF  1  </string1>"
				+ "    <normalize-space-function-leaf>test</normalize-space-function-leaf>"
				+ "  </when-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);    

		String response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <when-string-function-validation>"
						+ "    <string1>  ITF  1  </string1>"
						+ "    <normalize-space-function-leaf>test</normalize-space-function-leaf>"
						+ "  </when-string-function-validation>"	
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response); 

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-string-function-validation>"
				+ "    <string1>  ITF  12  </string1>"
				+ "    <normalize-space-function-leaf>test</normalize-space-function-leaf>"
				+ "  </when-string-function-validation>"
				+ "</whenMustContainer>";
		// should fail
		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(1,ncResponse.getErrors().size());
		assertEquals("/whenMust:whenMustContainer/whenMust:when-string-function-validation/whenMust:normalize-space-function-leaf", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("Violate when constraints: normalize-space(../string1) = 'ITF 1'", ncResponse.getErrors().get(0).getErrorMessage());

	}

	@Test
	public void testWhenNormalizeSpaceFunction_2() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-string-function-validation>"
				+ "    <string1>  ITF  1  </string1>"
				+ "    <normalize-space-function-leaf>test</normalize-space-function-leaf>"
				+ "  </when-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		String response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <when-string-function-validation>"
						+ "    <string1>  ITF  1  </string1>"
						+ "    <normalize-space-function-leaf>test</normalize-space-function-leaf>"
						+ "  </when-string-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
				;
		verifyGet(response);


		// update string1 impacts normalize-space-function-leaf
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-string-function-validation>"
				+ "    <string1>  ITF  12  </string1>"
				+ "  </when-string-function-validation>"
				+ "</whenMustContainer>";

		editConfig(m_server, m_clientInfo, requestXml1, true);
		response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <when-string-function-validation>"
						+ "    <string1>  ITF  12  </string1>"
						+ "  </when-string-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
		;
		verifyGet(response);
	}

	@Test
	public void testWhenTranslateFunction_1() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-string-function-validation>"
				+ "    <string1>-bar-</string1>"
				+ "    <translate-function-leaf>test</translate-function-leaf>"
				+ "  </when-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);    	

		String response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <when-string-function-validation>"
						+ "    <string1>-bar-</string1>"
						+ "    <translate-function-leaf>test</translate-function-leaf>"
						+ "  </when-string-function-validation>"	
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response); 

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-string-function-validation>"
				+ "    <string1>dsl</string1>"
				+ "    <translate-function-leaf>test</translate-function-leaf>"
				+ "  </when-string-function-validation>"
				+ "</whenMustContainer>";
		// should fail
		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(1,ncResponse.getErrors().size());
		assertEquals("/whenMust:whenMustContainer/whenMust:when-string-function-validation/whenMust:translate-function-leaf", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("Violate when constraints: translate(../string1, 'abc-', 'ABC') = 'BAr'", ncResponse.getErrors().get(0).getErrorMessage());

	}

	@Test
	public void testWhenTranslateFunction_2() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-string-function-validation>"
				+ "    <string1>-bar-</string1>"
				+ "    <translate-function-leaf>test</translate-function-leaf>"
				+ "  </when-string-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		String response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <when-string-function-validation>"
						+ "    <string1>-bar-</string1>"
						+ "    <translate-function-leaf>test</translate-function-leaf>"
						+ "  </when-string-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
				;
		verifyGet(response);


		// update string1 impacts translate-function-leaf
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-string-function-validation>"
				+ "    <string1>dsl</string1>"
				+ "  </when-string-function-validation>"
				+ "</whenMustContainer>";

		editConfig(m_server, m_clientInfo, requestXml1, true);
		response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <when-string-function-validation>"
						+ "    <string1>dsl</string1>"
						+ "  </when-string-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
		;
		verifyGet(response);
	}

	@Test
	public void testMustNumberFunction_1() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-number-function-validation>"
				+ "    <string1>42</string1>"
				+ "    <number-function-leaf>test</number-function-leaf>"
				+ "  </must-number-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);    	

		String response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <must-number-function-validation>"
						+ "    <string1>42</string1>"
						+ "    <number-function-leaf>test</number-function-leaf>"
						+ "  </must-number-function-validation>"	
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response); 

		/*		// not supported yet in JXPath error
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-number-function-validation>"
				+ "    <string1>42</string1>"
				+ "    <number-function-boolean-true>test</number-function-boolean-true>"
				+ "  </must-number-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);    	

		response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <must-number-function-validation>"
						+ "    <string1>42</string1>"
						+ "    <number-function-boolean-true>test</number-function-boolean-true>"
						+ "  </must-number-function-validation>"	
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response); */

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-number-function-validation>"
				+ "    <string1>41</string1>"
				+ "    <number-function-leaf>test</number-function-leaf>"
				+ "  </must-number-function-validation>"
				+ "</whenMustContainer>";
		// should fail
		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(1,ncResponse.getErrors().size());
		assertEquals("/whenMust:whenMustContainer/whenMust:must-number-function-validation/whenMust:number-function-leaf", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("Violate must constraints: number(../string1) = 42", ncResponse.getErrors().get(0).getErrorMessage());   	

	}

	@Test
	public void testMustNumberFunction_2() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-number-function-validation>"
				+ "    <string1>42</string1>"
				+ "    <number-function-leaf>test</number-function-leaf>"
				+ "  </must-number-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		String response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <must-number-function-validation>"
						+ "    <string1>42</string1>"
						+ "    <number-function-leaf>test</number-function-leaf>"
						+ "  </must-number-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
				;
		verifyGet(response);

		/*		// not supported yet in JXPath error
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-number-function-validation>"
				+ "    <string1>42</string1>"
				+ "    <number-function-boolean-true>test</number-function-boolean-true>"
				+ "  </must-number-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <must-number-function-validation>"
						+ "    <string1>42</string1>"
						+ "    <number-function-boolean-true>test</number-function-boolean-true>"
						+ "  </must-number-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response); */


		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-number-function-validation>"
				+ "    <string1>41</string1>"
				+ "  </must-number-function-validation>"
				+ "</whenMustContainer>";
		// should fail
		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(1,ncResponse.getErrors().size());
		assertEquals("/whenMust:whenMustContainer/whenMust:must-number-function-validation/whenMust:number-function-leaf", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("Violate must constraints: number(../string1) = 42", ncResponse.getErrors().get(0).getErrorMessage());


	}

	@Test
	public void testMustNumberFunction_3() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-number-function-validation>"
				+ "    <string1>42</string1>"
				+ "    <number-function-leaf>test</number-function-leaf>"
				+ "  </must-number-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		String response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <must-number-function-validation>"
						+ "    <string1>42</string1>"
						+ "    <number-function-leaf>test</number-function-leaf>"
						+ "  </must-number-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
				;
		verifyGet(response);

		/*		// not supported yet in JXPath error
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-number-function-validation>"
				+ "    <string1>42</string1>"
				+ "    <number-function-boolean-true>test</number-function-boolean-true>"
				+ "  </must-number-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <must-number-function-validation>"
						+ "    <string1>42</string1>"
						+ "    <number-function-boolean-true>test</number-function-boolean-true>"
						+ "  </must-number-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response); */

		//no args number function
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-number-function-validation>"
				+ "    <no-arg-number-function-leaf>1</no-arg-number-function-leaf>"
				+ "  </must-number-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <must-number-function-validation>"
						+ "    <string1>42</string1>"
						+ "    <number-function-leaf>test</number-function-leaf>"
						+ "    <no-arg-number-function-leaf>1</no-arg-number-function-leaf>"
						+ "  </must-number-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
		;
		verifyGet(response);
	}

	@Test
	public void testMustFloorFunction_1() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-number-function-validation>"
				+ "    <number1>42.42</number1>"
				+ "    <floor-function-leaf>test</floor-function-leaf>"
				+ "  </must-number-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);    	

		String response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <must-number-function-validation>"
						+ "    <number1>42.42</number1>"
						+ "    <floor-function-leaf>test</floor-function-leaf>"
						+ "  </must-number-function-validation>"	
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response); 

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-number-function-validation>"
				+ "    <number1>41.42</number1>"
				+ "    <floor-function-leaf>test</floor-function-leaf>"
				+ "  </must-number-function-validation>"
				+ "</whenMustContainer>";
		// should fail
		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(1,ncResponse.getErrors().size());
		assertEquals("/whenMust:whenMustContainer/whenMust:must-number-function-validation/whenMust:floor-function-leaf", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("Violate must constraints: floor(../number1) = 42", ncResponse.getErrors().get(0).getErrorMessage());   

	}

	@Test
	public void testMustFloorFunction_2() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-number-function-validation>"
				+ "    <number1>42.42</number1>"
				+ "    <floor-function-leaf>test</floor-function-leaf>"
				+ "  </must-number-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		String response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <must-number-function-validation>"
						+ "    <number1>42.42</number1>"
						+ "    <floor-function-leaf>test</floor-function-leaf>"
						+ "  </must-number-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
				;
		verifyGet(response);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-number-function-validation>"
				+ "    <number1>41.42</number1>"
				+ "  </must-number-function-validation>"
				+ "</whenMustContainer>";
		// should fail
		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(1,ncResponse.getErrors().size());
		assertEquals("/whenMust:whenMustContainer/whenMust:must-number-function-validation/whenMust:floor-function-leaf", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("Violate must constraints: floor(../number1) = 42", ncResponse.getErrors().get(0).getErrorMessage());
	}

	@Test
	public void testMustCeilingFunction_1() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-number-function-validation>"
				+ "    <number1>41.42</number1>"
				+ "    <ceiling-function-leaf>test</ceiling-function-leaf>"
				+ "  </must-number-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);    	

		String response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <must-number-function-validation>"
						+ "    <number1>41.42</number1>"
						+ "    <ceiling-function-leaf>test</ceiling-function-leaf>"
						+ "  </must-number-function-validation>"	
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response); 

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-number-function-validation>"
				+ "    <number1>42.42</number1>"
				+ "    <ceiling-function-leaf>test</ceiling-function-leaf>"
				+ "  </must-number-function-validation>"
				+ "</whenMustContainer>";
		// should fail
		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(1,ncResponse.getErrors().size());
		assertEquals("/whenMust:whenMustContainer/whenMust:must-number-function-validation/whenMust:ceiling-function-leaf", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("Violate must constraints: ceiling(../number1) = 42", ncResponse.getErrors().get(0).getErrorMessage());  
	}

	@Test
	public void testMustCeilingFunction_2() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-number-function-validation>"
				+ "    <number1>41.42</number1>"
				+ "    <ceiling-function-leaf>test</ceiling-function-leaf>"
				+ "  </must-number-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		String response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <must-number-function-validation>"
						+ "    <number1>41.42</number1>"
						+ "    <ceiling-function-leaf>test</ceiling-function-leaf>"
						+ "  </must-number-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
				;
		verifyGet(response);


		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-number-function-validation>"
				+ "    <number1>42.42</number1>"
				+ "  </must-number-function-validation>"
				+ "</whenMustContainer>";
		// should fail
		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(1,ncResponse.getErrors().size());
		assertEquals("/whenMust:whenMustContainer/whenMust:must-number-function-validation/whenMust:ceiling-function-leaf", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("Violate must constraints: ceiling(../number1) = 42", ncResponse.getErrors().get(0).getErrorMessage());
	}

	@Test
	public void testMustRoundFunction_1() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-number-function-validation>"
				+ "    <number1>42.42</number1>"
				+ "    <round-function-leaf>test</round-function-leaf>"
				+ "  </must-number-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);    
		String response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <must-number-function-validation>"
						+ "    <number1>42.42</number1>"
						+ "    <round-function-leaf>test</round-function-leaf>"
						+ "  </must-number-function-validation>"	
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response); 

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-number-function-validation>"
				+ "    <number1>41.72</number1>"
				+ "    <round-function-leaf>test</round-function-leaf>"
				+ "  </must-number-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);    	
		response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <must-number-function-validation>"
						+ "    <number1>41.72</number1>"
						+ "    <round-function-leaf>test</round-function-leaf>"
						+ "  </must-number-function-validation>"	
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-number-function-validation>"
				+ "    <number1>41.42</number1>"
				+ "    <round-function-leaf>test</round-function-leaf>"
				+ "  </must-number-function-validation>"
				+ "</whenMustContainer>";
		// should fail
		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(1,ncResponse.getErrors().size());
		assertEquals("/whenMust:whenMustContainer/whenMust:must-number-function-validation/whenMust:round-function-leaf", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("Violate must constraints: round(../number1) = 42", ncResponse.getErrors().get(0).getErrorMessage());  

	}

	@Test
	public void testMustRoundFunction_2() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-number-function-validation>"
				+ "    <number1>42.42</number1>"
				+ "    <round-function-leaf>test</round-function-leaf>"
				+ "  </must-number-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);
		String response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <must-number-function-validation>"
						+ "    <number1>42.42</number1>"
						+ "    <round-function-leaf>test</round-function-leaf>"
						+ "  </must-number-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
				;
		verifyGet(response);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-number-function-validation>"
				+ "    <number1>41.72</number1>"
				+ "    <round-function-leaf>test</round-function-leaf>"
				+ "  </must-number-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);
		response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <must-number-function-validation>"
						+ "    <number1>41.72</number1>"
						+ "    <round-function-leaf>test</round-function-leaf>"
						+ "  </must-number-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
		;
		verifyGet(response);

//		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
//				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
//				+ "  <must-number-function-validation>"
//				+ "    <number1>41.42</number1>"
//				+ "    <round-function-leaf>test</round-function-leaf>"
//				+ "  </must-number-function-validation>"
//				+ "</whenMustContainer>";
//		// should fail
//		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
//		assertEquals(1,ncResponse.getErrors().size());
//		assertEquals("/whenMust:whenMustContainer/whenMust:must-number-function-validation/whenMust:round-function-leaf", ncResponse.getErrors().get(0).getErrorPath());
//		assertEquals("Violate must constraints: round(../number1) = 42", ncResponse.getErrors().get(0).getErrorMessage());

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <must-number-function-validation>"
				+ "    <number1>41.42</number1>"
				+ "  </must-number-function-validation>"
				+ "</whenMustContainer>";
		// should fail
		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(1,ncResponse.getErrors().size());
		assertEquals("/whenMust:whenMustContainer/whenMust:must-number-function-validation/whenMust:round-function-leaf", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("Violate must constraints: round(../number1) = 42", ncResponse.getErrors().get(0).getErrorMessage());
	}

	@Test
	public void testWhenNumberFunction_1() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-number-function-validation>"
				+ "    <string1>42</string1>"
				+ "    <number-function-leaf>test</number-function-leaf>"
				+ "  </when-number-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);    	

		String response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <when-number-function-validation>"
						+ "    <string1>42</string1>"
						+ "    <number-function-leaf>test</number-function-leaf>"
						+ "  </when-number-function-validation>"	
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response); 

		/*		// not supported yet in JXPath error
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-number-function-validation>"
				+ "    <string1>42</string1>"
				+ "    <number-function-boolean-true>test</number-function-boolean-true>"
				+ "  </when-number-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);    	

		response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <when-number-function-validation>"
						+ "    <string1>42</string1>"
						+ "    <number-function-boolean-true>test</number-function-boolean-true>"
						+ "  </when-number-function-validation>"	
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response); */

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-number-function-validation>"
				+ "    <string1>41</string1>"
				+ "    <number-function-leaf>test</number-function-leaf>"
				+ "  </when-number-function-validation>"
				+ "</whenMustContainer>";
		// should fail
		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(1,ncResponse.getErrors().size());
		assertEquals("/whenMust:whenMustContainer/whenMust:when-number-function-validation/whenMust:number-function-leaf", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("Violate when constraints: number(../string1) = 42", ncResponse.getErrors().get(0).getErrorMessage());   	

	}

	@Test
	public void testWhenNumberFunction_2() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-number-function-validation>"
				+ "    <string1>42</string1>"
				+ "    <number-function-leaf>test</number-function-leaf>"
				+ "  </when-number-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		String response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <when-number-function-validation>"
						+ "    <string1>42</string1>"
						+ "    <number-function-leaf>test</number-function-leaf>"
						+ "  </when-number-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
				;
		verifyGet(response);

		/*		// not supported yet in JXPath error
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-number-function-validation>"
				+ "    <string1>42</string1>"
				+ "    <number-function-boolean-true>test</number-function-boolean-true>"
				+ "  </when-number-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <when-number-function-validation>"
						+ "    <string1>42</string1>"
						+ "    <number-function-boolean-true>test</number-function-boolean-true>"
						+ "  </when-number-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response); */


		// update string1 impacts number-function-leaf
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-number-function-validation>"
				+ "    <string1>41</string1>"
				+ "  </when-number-function-validation>"
				+ "</whenMustContainer>";
		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, true);
		response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <when-number-function-validation>"
						+ "    <string1>41</string1>"
						+ "  </when-number-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
		;
		verifyGet(response);

		// create number-function-both-when-must-leaf
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-number-function-validation>"
				+ "    <string1>41</string1>"
				+ "    <number-function-both-when-must-leaf>41</number-function-both-when-must-leaf>"
				+ "  </when-number-function-validation>"
				+ "</whenMustContainer>";
		ncResponse = editConfig(m_server, m_clientInfo, requestXml1, true);
		response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <when-number-function-validation>"
						+ "    <string1>41</string1>"
						+ "    <number-function-both-when-must-leaf>41</number-function-both-when-must-leaf>"
						+ "  </when-number-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
		;
		verifyGet(response);
	}

	@Test
	public void testWhenFloorFunction_1() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-number-function-validation>"
				+ "    <number1>42.42</number1>"
				+ "    <floor-function-leaf>test</floor-function-leaf>"
				+ "  </when-number-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);    	

		String response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <when-number-function-validation>"
						+ "    <number1>42.42</number1>"
						+ "    <floor-function-leaf>test</floor-function-leaf>"
						+ "  </when-number-function-validation>"	
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response); 

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-number-function-validation>"
				+ "    <number1>41.42</number1>"
				+ "    <floor-function-leaf>test</floor-function-leaf>"
				+ "  </when-number-function-validation>"
				+ "</whenMustContainer>";
		// should fail
		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(1,ncResponse.getErrors().size());
		assertEquals("/whenMust:whenMustContainer/whenMust:when-number-function-validation/whenMust:floor-function-leaf", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("Violate when constraints: floor(../number1) = 42", ncResponse.getErrors().get(0).getErrorMessage());   

	}

	@Test
	public void testWhenFloorFunction_2() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-number-function-validation>"
				+ "    <number1>42.42</number1>"
				+ "    <floor-function-leaf>test</floor-function-leaf>"
				+ "  </when-number-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		String response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <when-number-function-validation>"
						+ "    <number1>42.42</number1>"
						+ "    <floor-function-leaf>test</floor-function-leaf>"
						+ "  </when-number-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
				;
		verifyGet(response);


		// update number1 impacts floor-function-leaf
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-number-function-validation>"
				+ "    <number1>41.42</number1>"
				+ "  </when-number-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);
		response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <when-number-function-validation>"
						+ "    <number1>41.42</number1>"
						+ "  </when-number-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
		;
		verifyGet(response);
	}



	@Test
	public void testWhenCeilingFunction_1() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-number-function-validation>"
				+ "    <number1>41.42</number1>"
				+ "    <ceiling-function-leaf>test</ceiling-function-leaf>"
				+ "  </when-number-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);    	

		String response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <when-number-function-validation>"
						+ "    <number1>41.42</number1>"
						+ "    <ceiling-function-leaf>test</ceiling-function-leaf>"
						+ "  </when-number-function-validation>"	
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response); 

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-number-function-validation>"
				+ "    <number1>42.42</number1>"
				+ "    <ceiling-function-leaf>test</ceiling-function-leaf>"
				+ "  </when-number-function-validation>"
				+ "</whenMustContainer>";
		// should fail
		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(1,ncResponse.getErrors().size());
		assertEquals("/whenMust:whenMustContainer/whenMust:when-number-function-validation/whenMust:ceiling-function-leaf", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("Violate when constraints: ceiling(../number1) = 42", ncResponse.getErrors().get(0).getErrorMessage());  

	}

	@Test
	public void testWhenCeilingFunction_2() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-number-function-validation>"
				+ "    <number1>41.42</number1>"
				+ "    <ceiling-function-leaf>test</ceiling-function-leaf>"
				+ "  </when-number-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		String response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <when-number-function-validation>"
						+ "    <number1>41.42</number1>"
						+ "    <ceiling-function-leaf>test</ceiling-function-leaf>"
						+ "  </when-number-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
				;
		verifyGet(response);

//		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
//				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
//				+ "  <when-number-function-validation>"
//				+ "    <number1>42.42</number1>"
//				+ "    <ceiling-function-leaf>test</ceiling-function-leaf>"
//				+ "  </when-number-function-validation>"
//				+ "</whenMustContainer>";
//		// should fail
//		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
//		assertEquals(1,ncResponse.getErrors().size());
//		assertEquals("/whenMust:whenMustContainer/whenMust:when-number-function-validation/whenMust:ceiling-function-leaf", ncResponse.getErrors().get(0).getErrorPath());
//		assertEquals("Violate when constraints: ceiling(../number1) = 42", ncResponse.getErrors().get(0).getErrorMessage());

		// update number1 impacts ceiling-function-leaf
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-number-function-validation>"
				+ "    <number1>42.42</number1>"
				+ "  </when-number-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);
		response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <when-number-function-validation>"
						+ "    <number1>42.42</number1>"
						+ "  </when-number-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
		;
		verifyGet(response);
	}

	@Test
	public void testWhenRoundFunction_1() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-number-function-validation>"
				+ "    <number1>42.42</number1>"
				+ "    <round-function-leaf>test</round-function-leaf>"
				+ "  </when-number-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);    
		String response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <when-number-function-validation>"
						+ "    <number1>42.42</number1>"
						+ "    <round-function-leaf>test</round-function-leaf>"
						+ "  </when-number-function-validation>"	
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response); 

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-number-function-validation>"
				+ "    <number1>41.72</number1>"
				+ "    <round-function-leaf>test</round-function-leaf>"
				+ "  </when-number-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);    	
		response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <when-number-function-validation>"
						+ "    <number1>41.72</number1>"
						+ "    <round-function-leaf>test</round-function-leaf>"
						+ "  </when-number-function-validation>"	
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-number-function-validation>"
				+ "    <number1>41.42</number1>"
				+ "    <round-function-leaf>test</round-function-leaf>"
				+ "  </when-number-function-validation>"
				+ "</whenMustContainer>";
		// should fail
		NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml1, false);
		assertEquals(1,ncResponse.getErrors().size());
		assertEquals("/whenMust:whenMustContainer/whenMust:when-number-function-validation/whenMust:round-function-leaf", ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("Violate when constraints: round(../number1) = 42", ncResponse.getErrors().get(0).getErrorMessage());  

	}

	@Test
	public void testWhenRoundFunction_2() throws Exception {
		getModelNode();
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-number-function-validation>"
				+ "    <number1>42.42</number1>"
				+ "    <round-function-leaf>test</round-function-leaf>"
				+ "  </when-number-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);
		String response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <when-number-function-validation>"
						+ "    <number1>42.42</number1>"
						+ "    <round-function-leaf>test</round-function-leaf>"
						+ "  </when-number-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
				;
		verifyGet(response);

		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-number-function-validation>"
				+ "    <number1>41.72</number1>"
				+ "    <round-function-leaf>test</round-function-leaf>"
				+ "  </when-number-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);
		response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <when-number-function-validation>"
						+ "    <number1>41.72</number1>"
						+ "    <round-function-leaf>test</round-function-leaf>"
						+ "  </when-number-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
		;
		verifyGet(response);


		//update number1 impacts round-function-leaf
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ "  <when-number-function-validation>"
				+ "    <number1>41.42</number1>"
				+ "  </when-number-function-validation>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);
		response =
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <when-number-function-validation>"
						+ "    <number1>41.42</number1>"
						+ "  </when-number-function-validation>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
		;
		verifyGet(response);
	}

	@Test
	public void testWhenLocalNameAndNSUriOnGrouping() throws Exception {
		getModelNode();

		// local-name() and namespace-uri function refers node on other module container 'local-name-and-ns-uri1'
		String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ " <local-name-and-ns-uri1>"
				+ "   <groupingLeaf1>test</groupingLeaf1>"
				+ "   <groupingLeaf2>test</groupingLeaf2>"
				+ " </local-name-and-ns-uri1>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		String response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <local-name-and-ns-uri1>"
						+ "   <groupingLeaf1>test</groupingLeaf1>"
						+ "   <groupingLeaf2>test</groupingLeaf2>"
						+ "  </local-name-and-ns-uri1>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response);

		//namespace-uri refers node on other module container 'local-name-and-ns-uri2'
		requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
				+ " <local-name-and-ns-uri2>"
				+ "   <groupingLeaf2>test</groupingLeaf2>"
				+ " </local-name-and-ns-uri2>"
				+ "</whenMustContainer>";
		editConfig(m_server, m_clientInfo, requestXml1, true);

		response = 
				"<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
						+ " <data>"
						+ "  <whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
						+ "  <local-name-and-ns-uri1>"
						+ "   <groupingLeaf1>test</groupingLeaf1>"
						+ "   <groupingLeaf2>test</groupingLeaf2>"
						+ "  </local-name-and-ns-uri1>"
						+ "  <local-name-and-ns-uri2>"
						+ "   <groupingLeaf2>test</groupingLeaf2>"
						+ "  </local-name-and-ns-uri2>"
						+ "  </whenMustContainer>"
						+ "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
						+ " </data>"
						+ " </rpc-reply>"
						;
		verifyGet(response);
	}
	
    @Test
    public void testIPV6Leaf() throws Exception {
        getModelNode();

        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<whenMustContainer xmlns=\"urn:org:bbf2:pma:whenMustDSExprOnCoreLibraryFunction\">"
                + "  <ipv6-test>"
                + "    <ipv6-leaf1>2004::1</ipv6-leaf1>"
                + "  </ipv6-test>"
                + "</whenMustContainer>";
        editConfig(m_server, m_clientInfo, requestXml1, true);
    }
}
    
    

    
