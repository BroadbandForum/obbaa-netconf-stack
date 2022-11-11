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
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorType;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(RequestScopeJunitRunner.class)
public class DSExtensionFunctionValidatorTest extends AbstractDataStoreValidatorTest {
    
    @Test
    public void testSingleDerivedFunctionsInChoice() throws Exception {
        getModelNode();
        
        //SingleDerivedFromWithMust
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<exfunctionCanister xmlns=\"urn:opendaylight:datastore-extension-functions-test\">"
                + "  <baseLeafList>alpha</baseLeafList>"
                + "  <baseLeafList>beta</baseLeafList>"
                + "  <cricket>play</cricket>"
                + "</exfunctionCanister>";

        editConfig(m_server, m_clientInfo, requestXml1, true);
        
        //SingleDerivedFromOrSelfWithWhen
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<exfunctionCanister xmlns=\"urn:opendaylight:datastore-extension-functions-test\">"
                + "  <baseLeafList2>derivedFirst</baseLeafList2>"
                + "  <baseLeafList2>second</baseLeafList2>"
                + "  <football>play</football>"
                + "</exfunctionCanister>";

        editConfig(m_server, m_clientInfo, requestXml1, true);
        
        String getResponse = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
                  "<data>\n" +
                     "<ds-ext-func:exfunctionCanister xmlns:ds-ext-func=\"urn:opendaylight:datastore-extension-functions-test\">\n" +
                        "<ds-ext-func:baseLeafList>ds-ext-func:alpha</ds-ext-func:baseLeafList>\n" +
                        "<ds-ext-func:baseLeafList>ds-ext-func:beta</ds-ext-func:baseLeafList>\n" +
                        "<ds-ext-func:baseLeafList2>ds-ext-func:derivedFirst</ds-ext-func:baseLeafList2>\n" +
                        "<ds-ext-func:baseLeafList2>ds-ext-func:second</ds-ext-func:baseLeafList2>\n" +
                        "<ds-ext-func:football>play</ds-ext-func:football>\n" +
                     "</ds-ext-func:exfunctionCanister>\n" +
                  "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n" +
                  "</data>\n" +
                "</rpc-reply>"
                ;
        verifyGet(getResponse);
    }
    
    @Test
    public void testMultiDerivedFunctionsForWhenMust() throws Exception {
        getModelNode();
        
        //MultiDerivedFromWithMust
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<exfunctionCanister xmlns=\"urn:opendaylight:datastore-extension-functions-test\">"
                + "  <baseLeaf>beta</baseLeaf>"
                + "  <baseLeaf2>second</baseLeaf2>"
                + "  <leafWithMustAndMultiDerivedFrom>abcde</leafWithMustAndMultiDerivedFrom>"
                + "</exfunctionCanister>";

        editConfig(m_server, m_clientInfo, requestXml1, true);
        
        //MultiDerivedFromWithWhen
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<exfunctionCanister xmlns=\"urn:opendaylight:datastore-extension-functions-test\">"
                + "  <leafWithWhenAndMultiDerivedFrom>abcde</leafWithWhenAndMultiDerivedFrom>"
                + "</exfunctionCanister>";

        editConfig(m_server, m_clientInfo, requestXml1, true);
        
        //MultiDerivedFromOrSelfWithMust
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<exfunctionCanister xmlns=\"urn:opendaylight:datastore-extension-functions-test\">"
                + "  <baseLeaf3>derivedAlpha</baseLeaf3>"
                + "  <baseLeaf4>derivedFirst</baseLeaf4>"
                + "  <leafWithMustAndMultiDerivedFromOrSelf>abcde</leafWithMustAndMultiDerivedFromOrSelf>"
                + "</exfunctionCanister>";

        editConfig(m_server, m_clientInfo, requestXml1, true);
        
        //MultiDerivedFromOrSelfWithWhen
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<exfunctionCanister xmlns=\"urn:opendaylight:datastore-extension-functions-test\">"
                + "  <leafWithWhenAndMultiDerivedFromOrSelf>abcde</leafWithWhenAndMultiDerivedFromOrSelf>"
                + "</exfunctionCanister>";

        editConfig(m_server, m_clientInfo, requestXml1, true);
        
        String getResponse = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
                    "<data>\n" +
                       "<ds-ext-func:exfunctionCanister xmlns:ds-ext-func=\"urn:opendaylight:datastore-extension-functions-test\">\n" +
                           "<ds-ext-func:baseLeaf>ds-ext-func:beta</ds-ext-func:baseLeaf>\n" +
                           "<ds-ext-func:baseLeaf2>ds-ext-func:second</ds-ext-func:baseLeaf2>\n" +
                           "<ds-ext-func:baseLeaf3>ds-ext-func:derivedAlpha</ds-ext-func:baseLeaf3>\n" +
                           "<ds-ext-func:baseLeaf4>ds-ext-func:derivedFirst</ds-ext-func:baseLeaf4>\n" +
                           "<ds-ext-func:leafWithMustAndMultiDerivedFrom>abcde</ds-ext-func:leafWithMustAndMultiDerivedFrom>\n" +
                           "<ds-ext-func:leafWithMustAndMultiDerivedFromOrSelf>abcde</ds-ext-func:leafWithMustAndMultiDerivedFromOrSelf>\n" +
                           "<ds-ext-func:leafWithWhenAndMultiDerivedFrom>abcde</ds-ext-func:leafWithWhenAndMultiDerivedFrom>\n" +
                           "<ds-ext-func:leafWithWhenAndMultiDerivedFromOrSelf>abcde</ds-ext-func:leafWithWhenAndMultiDerivedFromOrSelf>\n" +
                       "</ds-ext-func:exfunctionCanister>\n" +
                       "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n" +
                    "</data>\n" +
                "</rpc-reply>"
                ;
        verifyGet(getResponse);
    }
    
    @Test
    public void testCrossDerivedFunctions() throws Exception {
        getModelNode();
        
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<exfunctionCanister xmlns=\"urn:opendaylight:datastore-extension-functions-test\">"
                + "  <baseLeaf>beta</baseLeaf>"
                + "  <baseLeaf5>triple-identity</baseLeaf5>"
                + "</exfunctionCanister>";

        editConfig(m_server, m_clientInfo, requestXml1, true);
        
        //BothDerivedFromAndDerivedFromOrSelf
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<exfunctionCanister xmlns=\"urn:opendaylight:datastore-extension-functions-test\">"
                + "  <leafWithBothDerivedFromAndDerivedFromOrSelf>abcde</leafWithBothDerivedFromAndDerivedFromOrSelf>"
                + "</exfunctionCanister>";

        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals(1,response.getErrors().size());
        assertEquals("must-violation", response.getErrors().get(0).getErrorAppTag());
        assertEquals(NetconfRpcErrorType.Application, response.getErrors().get(0).getErrorType());
        assertEquals("/ds-ext-func:exfunctionCanister/ds-ext-func:leafWithBothDerivedFromAndDerivedFromOrSelf", response.getErrors().get(0).getErrorPath());
        assertEquals("Violate must constraints: derived-from(../baseLeaf, \"base-identity\") and derived-from-or-self(../baseLeaf5, \"single-identity\")", response.getErrors().get(0).getErrorMessage());
    }
    
    @Test
    public void testDerivedFunctionsWithNot() throws Exception {
        getModelNode();
        
        //baseLeaf2 is of type "second" which not derived from base-identity, still ok reply due to not() function on whole expression
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<exfunctionCanister xmlns=\"urn:opendaylight:datastore-extension-functions-test\">"
                + "  <baseLeaf2>second</baseLeaf2>"
                + "  <leafWithDerivedFromAndNot>abcde</leafWithDerivedFromAndNot>"
                + "</exfunctionCanister>";
        
        editConfig(m_server, m_clientInfo, requestXml1, true);
        
        //baseLeaf4 is of type "derivedFirst" which is derived from or self from derivedFirst, but reply is rpc error due to not() function on whole expression
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<exfunctionCanister xmlns=\"urn:opendaylight:datastore-extension-functions-test\">"
                + "  <baseLeaf4>derivedFirst</baseLeaf4>"
                + "  <leafWithDerivedFromOrSelfAndNot>abcde</leafWithDerivedFromOrSelfAndNot>"
                + "</exfunctionCanister>";

        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals(1,response.getErrors().size());
        assertEquals("when-violation", response.getErrors().get(0).getErrorAppTag());
        assertEquals(NetconfRpcErrorType.Application, response.getErrors().get(0).getErrorType());
        assertEquals("/ds-ext-func:exfunctionCanister/ds-ext-func:leafWithDerivedFromOrSelfAndNot", response.getErrors().get(0).getErrorPath());
        assertEquals("Violate when constraints: not(derived-from-or-self(../baseLeaf4, 'derivedFirst'))", response.getErrors().get(0).getErrorMessage());
    }
    
    @Test
    public void testEnumValueWithNot() throws Exception {
        getModelNode();
        
        //positive case
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<exfunctionCanister xmlns=\"urn:opendaylight:datastore-extension-functions-test\">"
                + " <test-enum-value-container>"
                + "  <test-container-two>"
                + "   <inner-container-two>"
                + "    <leaf1>one</leaf1>"
                + "    <test-enum-leaf-with-must-not>test</test-enum-leaf-with-must-not>"
                + "   </inner-container-two>"
                + "  </test-container-two>"
                + " </test-enum-value-container>"
                + "</exfunctionCanister>";
        
        editConfig(m_server, m_clientInfo, requestXml1, true);
        
        //negative case
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<exfunctionCanister xmlns=\"urn:opendaylight:datastore-extension-functions-test\">"
                + " <test-enum-value-container>"
                + "  <test-container-two>"
                + "   <inner-container-two>"
                + "    <leaf1>two</leaf1>"
                + "    <test-enum-leaf-with-must-not>test</test-enum-leaf-with-must-not>"
                + "   </inner-container-two>"
                + "  </test-container-two>"
                + " </test-enum-value-container>"
                + "</exfunctionCanister>";

        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals(1,response.getErrors().size());
        assertEquals("must-violation", response.getErrors().get(0).getErrorAppTag());
        assertEquals(NetconfRpcErrorType.Application, response.getErrors().get(0).getErrorType());
        assertEquals("/ds-ext-func:exfunctionCanister/ds-ext-func:test-enum-value-container/ds-ext-func:test-container-two/ds-ext-func:inner-container-two/ds-ext-func:test-enum-leaf-with-must-not", response.getErrors().get(0).getErrorPath());
        assertEquals("Violate must constraints: not(enum-value(parent::node()/leaf1) > 1)", response.getErrors().get(0).getErrorMessage());
    }
    
    @Test
    public void testEnumValueWithLongFormXpath() throws Exception {
        getModelNode();
        
        //positive case
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<exfunctionCanister xmlns=\"urn:opendaylight:datastore-extension-functions-test\">"
                + " <test-enum-value-container>"
                + "  <test-container-two>"
                + "   <inner-container-two>"
                + "    <leaf1>two</leaf1>"
                + "    <test-enum-leaf-with-long-form>test</test-enum-leaf-with-long-form>"
                + "   </inner-container-two>"
                + "  </test-container-two>"
                + " </test-enum-value-container>"
                + "</exfunctionCanister>";
        
        editConfig(m_server, m_clientInfo, requestXml1, true);
        
        //negative case
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<exfunctionCanister xmlns=\"urn:opendaylight:datastore-extension-functions-test\">"
                + " <test-enum-value-container>"
                + "  <test-container-two>"
                + "   <inner-container-two>"
                + "    <leaf1>one</leaf1>"
                + "    <test-enum-leaf-with-long-form>test</test-enum-leaf-with-long-form>"
                + "   </inner-container-two>"
                + "  </test-container-two>"
                + " </test-enum-value-container>"
                + "</exfunctionCanister>";

        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals(1,response.getErrors().size());
        assertEquals("when-violation", response.getErrors().get(0).getErrorAppTag());
        assertEquals(NetconfRpcErrorType.Application, response.getErrors().get(0).getErrorType());
        assertEquals("/ds-ext-func:exfunctionCanister/ds-ext-func:test-enum-value-container/ds-ext-func:test-container-two/ds-ext-func:inner-container-two/ds-ext-func:test-enum-leaf-with-long-form", response.getErrors().get(0).getErrorPath());
        assertEquals("Violate when constraints: enum-value(./../leaf1) > 1", response.getErrors().get(0).getErrorMessage());
        
        //positive case with self node long form
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<exfunctionCanister xmlns=\"urn:opendaylight:datastore-extension-functions-test\">"
                + " <test-enum-value-container>"
                + "  <test-container-two>"
                + "   <inner-container-two>"
                + "    <test-enum-leaf-with-long-form-self-node>two</test-enum-leaf-with-long-form-self-node>"
                + "   </inner-container-two>"
                + "  </test-container-two>"
                + " </test-enum-value-container>"
                + "</exfunctionCanister>";
        
        editConfig(m_server, m_clientInfo, requestXml1, true);
        
        //negative case with self node long form
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<exfunctionCanister xmlns=\"urn:opendaylight:datastore-extension-functions-test\">"
                + " <test-enum-value-container>"
                + "  <test-container-two>"
                + "   <inner-container-two>"
                + "    <test-enum-leaf-with-long-form-self-node>one</test-enum-leaf-with-long-form-self-node>"
                + "   </inner-container-two>"
                + "  </test-container-two>"
                + " </test-enum-value-container>"
                + "</exfunctionCanister>";

        response = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals(1,response.getErrors().size());
        assertEquals("must-violation", response.getErrors().get(0).getErrorAppTag());
        assertEquals(NetconfRpcErrorType.Application, response.getErrors().get(0).getErrorType());
        assertEquals("/ds-ext-func:exfunctionCanister/ds-ext-func:test-enum-value-container/ds-ext-func:test-container-two/ds-ext-func:inner-container-two/ds-ext-func:test-enum-leaf-with-long-form-self-node", response.getErrors().get(0).getErrorPath());
        assertEquals("Violate must constraints: enum-value(self::node()) > 1", response.getErrors().get(0).getErrorMessage());
    }
    
    @Test
    public void testEnumValueWithWhen() throws Exception {
        getModelNode();
        
        //positive case using default with when condition true
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<exfunctionCanister xmlns=\"urn:opendaylight:datastore-extension-functions-test\">"
                + " <test-enum-value-container>"
                + "  <test-container-two>"
                + "   <inner-container-two>"
                + "    <leaf1>two</leaf1>"
                + "   </inner-container-two>"
                + "  </test-container-two>"
                + " </test-enum-value-container>"
                + "</exfunctionCanister>";
        
        editConfig(m_server, m_clientInfo, requestXml1, true);
        
        String getResponse = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
                  "<data>\n" +
                    "<ds-ext-func:exfunctionCanister xmlns:ds-ext-func=\"urn:opendaylight:datastore-extension-functions-test\">\n" +
                      "<ds-ext-func:test-enum-value-container>\n" +
                        "<ds-ext-func:test-container-two>\n" +
                          "<ds-ext-func:inner-container-two>\n" +
                            "<ds-ext-func:leaf1>two</ds-ext-func:leaf1>\n" +
                            "<ds-ext-func:test-enum-leaf-with-when>iAmDefault</ds-ext-func:test-enum-leaf-with-when>\n" +
                          "</ds-ext-func:inner-container-two>\n" +
                        "</ds-ext-func:test-container-two>\n" +
                      "</ds-ext-func:test-enum-value-container>\n" +
                    "</ds-ext-func:exfunctionCanister>\n" +
                    "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n" +
                   "</data>\n" +
                "</rpc-reply>"
                ;
        verifyGet(getResponse);
        
        //negative case
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<exfunctionCanister xmlns=\"urn:opendaylight:datastore-extension-functions-test\">"
                + " <test-enum-value-container>"
                + "  <test-container-two>"
                + "   <inner-container-two>"
                + "    <leaf1>one</leaf1>"
                + "    <test-enum-leaf-with-when>hello</test-enum-leaf-with-when>"
                + "   </inner-container-two>"
                + "  </test-container-two>"
                + " </test-enum-value-container>"
                + "</exfunctionCanister>";
        
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals("Violate when constraints: enum-value(../leaf1) > 1",response.getErrors().get(0).getErrorMessage());
        assertEquals(
                "/ds-ext-func:exfunctionCanister/ds-ext-func:test-enum-value-container/ds-ext-func:test-container-two/ds-ext-func:inner-container-two/ds-ext-func:test-enum-leaf-with-when",
                response.getErrors().get(0).getErrorPath());
    }
    
    @Test
    public void testEnumValueWithParentPattern() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<exfunctionCanister xmlns=\"urn:opendaylight:datastore-extension-functions-test\">"
                + " <alarm-list>"
                + "  <key>key1</key>"
                + "  <severity>critical</severity>"
                + "  <test-enum-value-with-parent-pattern>hello</test-enum-value-with-parent-pattern>"
                + " </alarm-list>"
                + "</exfunctionCanister>";
       
       editConfig(m_server, m_clientInfo, requestXml1, true);

       requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<exfunctionCanister xmlns=\"urn:opendaylight:datastore-extension-functions-test\">"
                + " <alarm-list>"
                + "  <key>key2</key>"
                + "  <severity>minor</severity>"
                + "  <test-enum-value-with-parent-pattern>hello</test-enum-value-with-parent-pattern>"
                + " </alarm-list>"
                + "</exfunctionCanister>";
        
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals("Violate must constraints: enum-value(../severity) >= 5",response.getErrors().get(0).getErrorMessage());
        assertEquals(
                "/ds-ext-func:exfunctionCanister/ds-ext-func:alarm-list[ds-ext-func:key='key2']/ds-ext-func:test-enum-value-with-parent-pattern",
                response.getErrors().get(0).getErrorPath());
    }
    
    @Test
    public void testEnumValueOnNonExistingLeaf() throws Exception {
        getModelNode();
        
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<exfunctionCanister xmlns=\"urn:opendaylight:datastore-extension-functions-test\">"
                + " <test-enum-value-container>"
                + "  <test-container-two>"
                + "  <test-enum-leaf>hello</test-enum-leaf>"
                + "  </test-container-two>"
                + " </test-enum-value-container>"
                + "</exfunctionCanister>";
        
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals("Violate must constraints: enum-value(../../test-container-one/leaf1) > 1",response.getErrors().get(0).getErrorMessage());
    }
    
    @Test
    public void testEnumValueWithAbsoluteXpath() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<exfunctionCanister xmlns=\"urn:opendaylight:datastore-extension-functions-test\">"
                + " <test-enum-value-container>"
                + "  <test-container-one>"
                + "   <list1>"
                + "    <key>key1</key>"
                + "    <valueLeaf>one</valueLeaf>"
                + "   </list1>"
                + "   <list1>"
                + "    <key>key2</key>"
                + "    <valueLeaf>two</valueLeaf>"
                + "   </list1>"
                + "  </test-container-one>"
                + " </test-enum-value-container>"
                + "</exfunctionCanister>";
       
        editConfig(m_server, m_clientInfo, requestXml1, true);
        
        //negative case
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<exfunctionCanister xmlns=\"urn:opendaylight:datastore-extension-functions-test\">"
                + " <test-enum-value-container>"
                + "  <test-container-two>"
                + "  <test-enum-leaf-with-absolute-path>key1</test-enum-leaf-with-absolute-path>"
                + "  </test-container-two>"
                + " </test-enum-value-container>"
                + "</exfunctionCanister>";
           
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals("Violate must constraints: enum-value(/exfunctionCanister/test-enum-value-container/test-container-one/list1[key = current()]/valueLeaf) > 1",response.getErrors().get(0).getErrorMessage());
        assertEquals(
                "/ds-ext-func:exfunctionCanister/ds-ext-func:test-enum-value-container/ds-ext-func:test-container-two/ds-ext-func:test-enum-leaf-with-absolute-path",
                response.getErrors().get(0).getErrorPath());
        
        //positive case
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<exfunctionCanister xmlns=\"urn:opendaylight:datastore-extension-functions-test\">"
                + " <test-enum-value-container>"
                + "  <test-container-two>"
                + "  <test-enum-leaf-with-absolute-path>key2</test-enum-leaf-with-absolute-path>"
                + "  </test-container-two>"
                + " </test-enum-value-container>"
                + "</exfunctionCanister>";
        
        editConfig(m_server, m_clientInfo, requestXml1, true);
        
        String ncResponse = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <ds-ext-func:exfunctionCanister xmlns:ds-ext-func=\"urn:opendaylight:datastore-extension-functions-test\">"
                + "   <ds-ext-func:test-enum-value-container>"
                + "    <ds-ext-func:test-container-one>"
                + "       <ds-ext-func:list1>"
                + "       <ds-ext-func:key>key1</ds-ext-func:key>"
                + "       <ds-ext-func:valueLeaf>one</ds-ext-func:valueLeaf>"
                + "       </ds-ext-func:list1>"
                + "       <ds-ext-func:list1>"
                + "       <ds-ext-func:key>key2</ds-ext-func:key>"
                + "       <ds-ext-func:valueLeaf>two</ds-ext-func:valueLeaf>"
                + "       </ds-ext-func:list1>"
                + "    </ds-ext-func:test-container-one>"
                + "    <ds-ext-func:test-container-two>"
                + "       <ds-ext-func:test-enum-leaf-with-absolute-path>key2</ds-ext-func:test-enum-leaf-with-absolute-path>"
                + "    </ds-ext-func:test-container-two>"
                + "   </ds-ext-func:test-enum-value-container>"
                + "  </ds-ext-func:exfunctionCanister>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n"
                + " </data>"
                + "</rpc-reply>"
                ;
        verifyGet(ncResponse);
    }

    @Test
    public void testEnumValueWithNoArguments() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<exfunctionCanister xmlns=\"urn:opendaylight:datastore-extension-functions-test\">"
                + " <alarm-list>"
                + "  <key>key1</key>"
                + "  <severity>minor</severity>"
                + "  <test-enum-value-with-empty-node-set>hello</test-enum-value-with-empty-node-set>"
                + " </alarm-list>"
                + "</exfunctionCanister>";
        
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, response.getErrors().get(0).getErrorTag());
        assertEquals(
                "Missing argument in enum-value function. You need to provide one argument in the enum-value() function : enum-value() in node : test-enum-value-with-empty-node-set",
                response.getErrors().get(0).getErrorMessage());
    }
    
    @Test
    public void testEnumValueWithArgumentsNegativeCase() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<exfunctionCanister xmlns=\"urn:opendaylight:datastore-extension-functions-test\">"
                + " <test-enum-value-container>"
                + "  <test-container-one>"
                + "  <leaf1>one</leaf1>"
                + "  </test-container-one>"
                + " </test-enum-value-container>"
                + "</exfunctionCanister>";
        
        editConfig(m_server, m_clientInfo, requestXml1, true);
   
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<exfunctionCanister xmlns=\"urn:opendaylight:datastore-extension-functions-test\">"
                + " <test-enum-value-container>"
                + "  <test-container-two>"
                + "  <test-enum-leaf>hello</test-enum-leaf>"
                + "  </test-container-two>"
                + " </test-enum-value-container>"
                + "</exfunctionCanister>";
        
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals("/ds-ext-func:exfunctionCanister/ds-ext-func:test-enum-value-container/ds-ext-func:test-container-two/ds-ext-func:test-enum-leaf", response.getErrors().get(0).getErrorPath());
        assertEquals(
                "Violate must constraints: enum-value(../../test-container-one/leaf1) > 1",
                response.getErrors().get(0).getErrorMessage());
    }
    
    @Test
    public void testEnumValueWithArgumentsPositiveCase() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<exfunctionCanister xmlns=\"urn:opendaylight:datastore-extension-functions-test\">"
                + " <test-enum-value-container>"
                + "  <test-container-one>"
                + "  <leaf1>two</leaf1>"
                + "  </test-container-one>"
                + " </test-enum-value-container>"
                + "</exfunctionCanister>";
        
        editConfig(m_server, m_clientInfo, requestXml1, true);
   
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<exfunctionCanister xmlns=\"urn:opendaylight:datastore-extension-functions-test\">"
                + " <test-enum-value-container>"
                + "  <test-container-two>"
                + "  <test-enum-leaf>hello</test-enum-leaf>"
                + "  </test-container-two>"
                + " </test-enum-value-container>"
                + "</exfunctionCanister>";
        
        editConfig(m_server, m_clientInfo, requestXml1, true);
        
        String ncResponse = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <ds-ext-func:exfunctionCanister xmlns:ds-ext-func=\"urn:opendaylight:datastore-extension-functions-test\">"
                + "   <ds-ext-func:test-enum-value-container>"
                + "    <ds-ext-func:test-container-one>"
                + "       <ds-ext-func:leaf1>two</ds-ext-func:leaf1>"
                + "    </ds-ext-func:test-container-one>"
                + "    <ds-ext-func:test-container-two>"
                + "       <ds-ext-func:test-enum-leaf>hello</ds-ext-func:test-enum-leaf>"
                + "    </ds-ext-func:test-container-two>"
                + "   </ds-ext-func:test-enum-value-container>"
                + "  </ds-ext-func:exfunctionCanister>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n"
                + " </data>"
                + "</rpc-reply>"
                ;
        verifyGet(ncResponse);
        
    }
    
    @Test
    public void testEnumValueForLeafListPositiveCase() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<exfunctionCanister xmlns=\"urn:opendaylight:datastore-extension-functions-test\">"
                + " <test-enum-value-container>"
                + "  <test-container-one>"
                + "  <leaflist1>three</leaflist1>"
                + "  <leaflist1>four</leaflist1>"
                + "  </test-container-one>"
                + " </test-enum-value-container>"
                + "</exfunctionCanister>";
        
        editConfig(m_server, m_clientInfo, requestXml1, true);
   
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<exfunctionCanister xmlns=\"urn:opendaylight:datastore-extension-functions-test\">"
                + " <test-enum-value-container>"
                + "  <test-container-two>"
                + "  <test-enum-leaf-for-leaf-list>hello</test-enum-leaf-for-leaf-list>"
                + "  </test-container-two>"
                + " </test-enum-value-container>"
                + "</exfunctionCanister>";
        
        editConfig(m_server, m_clientInfo, requestXml1, true);
       
        String ncResponse = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <ds-ext-func:exfunctionCanister xmlns:ds-ext-func=\"urn:opendaylight:datastore-extension-functions-test\">"
                + "   <ds-ext-func:test-enum-value-container>"
                + "    <ds-ext-func:test-container-one>"
                + "       <ds-ext-func:leaflist1>three</ds-ext-func:leaflist1>"
                + "       <ds-ext-func:leaflist1>four</ds-ext-func:leaflist1>"
                + "    </ds-ext-func:test-container-one>"
                + "    <ds-ext-func:test-container-two>"
                + "       <ds-ext-func:test-enum-leaf-for-leaf-list>hello</ds-ext-func:test-enum-leaf-for-leaf-list>"
                + "    </ds-ext-func:test-container-two>"
                + "   </ds-ext-func:test-enum-value-container>"
                + "  </ds-ext-func:exfunctionCanister>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n"
                + " </data>"
                + "</rpc-reply>"
                ;
        verifyGet(ncResponse);
        
    }
    
    @Test
    public void testEnumValueForLeafListNegativeCase() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<exfunctionCanister xmlns=\"urn:opendaylight:datastore-extension-functions-test\">"
                + " <test-enum-value-container>"
                + "  <test-container-one>"
                + "  <leaflist1>one</leaflist1>"
                + "  <leaflist1>four</leaflist1>"
                + "  </test-container-one>"
                + " </test-enum-value-container>"
                + "</exfunctionCanister>";
        
        editConfig(m_server, m_clientInfo, requestXml1, true);
   
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<exfunctionCanister xmlns=\"urn:opendaylight:datastore-extension-functions-test\">"
                + " <test-enum-value-container>"
                + "  <test-container-two>"
                + "  <test-enum-leaf-for-leaf-list>hello</test-enum-leaf-for-leaf-list>"
                + "  </test-container-two>"
                + " </test-enum-value-container>"
                + "</exfunctionCanister>";
        
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals("/ds-ext-func:exfunctionCanister/ds-ext-func:test-enum-value-container/ds-ext-func:test-container-two/ds-ext-func:test-enum-leaf-for-leaf-list", response.getErrors().get(0).getErrorPath());
        assertEquals(
                "Violate must constraints: enum-value(../../test-container-one/leaflist1) > 2",
                response.getErrors().get(0).getErrorMessage());
        
    }
    
    @Test
    public void testEnumValueForNonEnumLeafAndLeafList() throws Exception {
        getModelNode();
        
        //leaf
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<exfunctionCanister xmlns=\"urn:opendaylight:datastore-extension-functions-test\">"
                + " <test-enum-value-container>"
                + "  <test-container-one>"
                + "  <non-enum-leaf1>one</non-enum-leaf1>"
                + "  <non-enum-leaf-list1>two</non-enum-leaf-list1>"
                + "  </test-container-one>"
                + " </test-enum-value-container>"
                + "</exfunctionCanister>";
        
        editConfig(m_server, m_clientInfo, requestXml1, true);
   
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<exfunctionCanister xmlns=\"urn:opendaylight:datastore-extension-functions-test\">"
                + " <test-enum-value-container>"
                + "  <test-container-two>"
                + "  <test-for-non-enum-leaf>hello</test-for-non-enum-leaf>"
                + "  </test-container-two>"
                + " </test-enum-value-container>"
                + "</exfunctionCanister>";
        
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals(
                "Violate must constraints: enum-value(/exfunctionCanister/test-enum-value-container/test-container-one/non-enum-leaf1) > 1",
                response.getErrors().get(0).getErrorMessage());
        
        //leaf-list
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<exfunctionCanister xmlns=\"urn:opendaylight:datastore-extension-functions-test\">"
                + " <test-enum-value-container>"
                + "  <test-container-one>"
                + "  <non-enum-leaf-list1>one</non-enum-leaf-list1>"
                + "  <non-enum-leaf-list1>two</non-enum-leaf-list1>"
                + "  </test-container-one>"
                + " </test-enum-value-container>"
                + "</exfunctionCanister>";
        
        editConfig(m_server, m_clientInfo, requestXml1, true);
   
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<exfunctionCanister xmlns=\"urn:opendaylight:datastore-extension-functions-test\">"
                + " <test-enum-value-container>"
                + "  <test-container-two>"
                + "  <test-for-non-enum-leaf-list>hello</test-for-non-enum-leaf-list>"
                + "  </test-container-two>"
                + " </test-enum-value-container>"
                + "</exfunctionCanister>";
        
        response = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals(
                "Violate must constraints: enum-value(../../test-container-one/non-enum-leaf-list1) > 2",
                response.getErrors().get(0).getErrorMessage());
        
    }
    
    @Test
    public void testEnumValueWithCurrentLeafListOrLeaf() throws Exception {
        getModelNode();
        
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<exfunctionCanister xmlns=\"urn:opendaylight:datastore-extension-functions-test\">"
                + " <test-enum-value-container>"
                + "  <test-container-two>"
                + "  <test-enum-leaf-list-with-dot-operator>one</test-enum-leaf-list-with-dot-operator>"
                + "  <test-enum-leaf-list-with-dot-operator>two</test-enum-leaf-list-with-dot-operator>"
                + "  </test-container-two>"
                + " </test-enum-value-container>"
                + "</exfunctionCanister>";
        
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals("/ds-ext-func:exfunctionCanister/ds-ext-func:test-enum-value-container/ds-ext-func:test-container-two/ds-ext-func:test-enum-leaf-list-with-dot-operator", response.getErrors().get(0).getErrorPath());
        assertEquals(
                "Violate must constraints: enum-value(.) > 2",
                response.getErrors().get(0).getErrorMessage());
        
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<exfunctionCanister xmlns=\"urn:opendaylight:datastore-extension-functions-test\">"
                + " <test-enum-value-container>"
                + "  <test-container-two>"
                + "  <test-enum-leaf-with-dot-operator>two</test-enum-leaf-with-dot-operator>"
                + "  </test-container-two>"
                + " </test-enum-value-container>"
                + "</exfunctionCanister>";
        
        editConfig(m_server, m_clientInfo, requestXml1, true);
        
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<exfunctionCanister xmlns=\"urn:opendaylight:datastore-extension-functions-test\">"
                + " <test-enum-value-container>"
                + "  <test-container-two>"
                + "  <test-enum-leaf-with-dot-operator>one</test-enum-leaf-with-dot-operator>"
                + "  </test-container-two>"
                + " </test-enum-value-container>"
                + "</exfunctionCanister>";
        
        response = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals("/ds-ext-func:exfunctionCanister/ds-ext-func:test-enum-value-container/ds-ext-func:test-container-two/ds-ext-func:test-enum-leaf-with-dot-operator", response.getErrors().get(0).getErrorPath());
        assertEquals(
                "Violate must constraints: enum-value(.) > 1",
                response.getErrors().get(0).getErrorMessage());
        
    }
    
    @Test
    public void testEnumValueWithTargetLeafInChoiceCase() throws Exception {
        getModelNode();
        
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<exfunctionCanister xmlns=\"urn:opendaylight:datastore-extension-functions-test\">"
                + " <test-enum-value-container>"
                + "  <test-container-one>"
                + "  <leaf22>one</leaf22>"
                + "  </test-container-one>"
                + " </test-enum-value-container>"
                + "</exfunctionCanister>";
        
        editConfig(m_server, m_clientInfo, requestXml1, true);
        
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<exfunctionCanister xmlns=\"urn:opendaylight:datastore-extension-functions-test\">"
                + " <test-enum-value-container>"
                + "  <test-container-two>"
                + "  <test-enum-leaf-in-choice-case>hello</test-enum-leaf-in-choice-case>"
                + "  </test-container-two>"
                + " </test-enum-value-container>"
                + "</exfunctionCanister>";
        
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals("/ds-ext-func:exfunctionCanister/ds-ext-func:test-enum-value-container/ds-ext-func:test-container-two/ds-ext-func:test-enum-leaf-in-choice-case", response.getErrors().get(0).getErrorPath());
        assertEquals(
                "Violate must constraints: enum-value(../../test-container-one/leaf22) > 1",
                response.getErrors().get(0).getErrorMessage());
    }
    
    @Test
    public void testEnumValueWithXpathFromCurrentNode() throws Exception {
        getModelNode();
        
        //Negative Case
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<exfunctionCanister xmlns=\"urn:opendaylight:datastore-extension-functions-test\">"
                + " <test-enum-value-container>"
                + "  <test-container-two>"
                + "  <test-leaf>one</test-leaf>"
                + "  </test-container-two>"
                + " </test-enum-value-container>"
                + "</exfunctionCanister>";
        
        editConfig(m_server, m_clientInfo, requestXml1, true);
        
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<exfunctionCanister xmlns=\"urn:opendaylight:datastore-extension-functions-test\">"
                + " <test-enum-value-container>"
                + "  <test-container-two>"
                + "  <test-enum-leaf-xpath-from-dot>one</test-enum-leaf-xpath-from-dot>"
                + "  </test-container-two>"
                + " </test-enum-value-container>"
                + "</exfunctionCanister>";
        
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals("/ds-ext-func:exfunctionCanister/ds-ext-func:test-enum-value-container/ds-ext-func:test-container-two/ds-ext-func:test-enum-leaf-xpath-from-dot", response.getErrors().get(0).getErrorPath());
        assertEquals(
                "Violate must constraints: enum-value(./../test-leaf) > enum-value(.)",
                response.getErrors().get(0).getErrorMessage());
        
        //Positive Case
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<exfunctionCanister xmlns=\"urn:opendaylight:datastore-extension-functions-test\">"
                + " <test-enum-value-container>"
                + "  <test-container-two>"
                + "  <test-leaf>three</test-leaf>"
                + "  </test-container-two>"
                + " </test-enum-value-container>"
                + "</exfunctionCanister>";
        
        editConfig(m_server, m_clientInfo, requestXml1, true);
        
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<exfunctionCanister xmlns=\"urn:opendaylight:datastore-extension-functions-test\">"
                + " <test-enum-value-container>"
                + "  <test-container-two>"
                + "  <test-enum-leaf-xpath-from-dot>two</test-enum-leaf-xpath-from-dot>"
                + "  </test-container-two>"
                + " </test-enum-value-container>"
                + "</exfunctionCanister>";
        
        editConfig(m_server, m_clientInfo, requestXml1, true);
       
        String ncResponse = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <data>"
                + "  <ds-ext-func:exfunctionCanister xmlns:ds-ext-func=\"urn:opendaylight:datastore-extension-functions-test\">"
                + "   <ds-ext-func:test-enum-value-container>"
                + "    <ds-ext-func:test-container-two>"
                + "       <ds-ext-func:test-leaf>three</ds-ext-func:test-leaf>"
                + "       <ds-ext-func:test-enum-leaf-xpath-from-dot>two</ds-ext-func:test-enum-leaf-xpath-from-dot>"
                + "    </ds-ext-func:test-container-two>"
                + "   </ds-ext-func:test-enum-value-container>"
                + "  </ds-ext-func:exfunctionCanister>"
                + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n"
                + " </data>"
                + "</rpc-reply>"
                ;
        verifyGet(ncResponse);
    }

    @Test
    public void testReMatchXpathFunction() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<exfunctionCanister xmlns=\"urn:opendaylight:datastore-extension-functions-test\">"
                + " <test-re-match-container>"
                + "  <test-container-one>"
                + "  <leaf1>abc</leaf1>"
                + "  </test-container-one>"
                + " </test-re-match-container>"
                + "</exfunctionCanister>";

        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, true);

        //positive case with value matching the pattern: \\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<exfunctionCanister xmlns=\"urn:opendaylight:datastore-extension-functions-test\">"
                + " <test-re-match-container>"
                + "  <test-container-one>"
                + "  <leaf2>10.131.228.113</leaf2>"
                + "  </test-container-one>"
                + " </test-re-match-container>"
                + "</exfunctionCanister>";

        response = editConfig(m_server, m_clientInfo, requestXml1, true);

        //negative case with value not matching the pattern: \\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<exfunctionCanister xmlns=\"urn:opendaylight:datastore-extension-functions-test\">"
                + " <test-re-match-container>"
                + "  <test-container-one>"
                + "  <leaf2>10.131.228.1134</leaf2>"
                + "  </test-container-one>"
                + " </test-re-match-container>"
                + "</exfunctionCanister>";

        response = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals("input value does not match regex: \\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}", response.getErrors().get(0).getErrorMessage());
        assertEquals(
                "/ds-ext-func:exfunctionCanister/ds-ext-func:test-re-match-container/ds-ext-func:test-container-one/ds-ext-func:leaf2",
                response.getErrors().get(0).getErrorPath());

        //re-match along with substring-before
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<exfunctionCanister xmlns=\"urn:opendaylight:datastore-extension-functions-test\">"
                + " <test-re-match-container>"
                + "  <test-container-one>"
                + "  <leaf3>226.131.228.113</leaf3>"
                + "  </test-container-one>"
                + " </test-re-match-container>"
                + "</exfunctionCanister>";

        response = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals("224.0.0.0~255.255.255.255 are not valid", response.getErrors().get(0).getErrorMessage());
        assertEquals(
                "/ds-ext-func:exfunctionCanister/ds-ext-func:test-re-match-container/ds-ext-func:test-container-one/ds-ext-func:leaf3",
                response.getErrors().get(0).getErrorPath());

        //re-match along with starts-with
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<exfunctionCanister xmlns=\"urn:opendaylight:datastore-extension-functions-test\">"
                + " <test-re-match-container>"
                + "  <test-container-one>"
                + "  <leaf4>0.0.0.214</leaf4>"
                + "  </test-container-one>"
                + " </test-re-match-container>"
                + "</exfunctionCanister>";

        response = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals("0.0.0.[0-255] is not valid address", response.getErrors().get(0).getErrorMessage());
        assertEquals(
                "/ds-ext-func:exfunctionCanister/ds-ext-func:test-re-match-container/ds-ext-func:test-container-one/ds-ext-func:leaf4",
                response.getErrors().get(0).getErrorPath());
    }

    @Test
    public void testErrorWithIncorrectReMatchFunction() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<exfunctionCanister xmlns=\"urn:opendaylight:datastore-extension-functions-test\">"
                + " <test-re-match-container>"
                + "  <test-container-one>"
                + "  <leaf6>10.131.228.113</leaf6>"
                + "  </test-container-one>"
                + " </test-re-match-container>"
                + "</exfunctionCanister>";

        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, response.getErrors().get(0).getErrorTag());
        assertEquals(NetconfRpcErrorType.Application, response.getErrors().get(0).getErrorType());
        assertEquals("Missing arguments in re-match function. You need to provide two arguments in the re-match() function : re-match('\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}') in node : leaf6", response.getErrors().get(0).getErrorMessage());
    }

    @Test
    public void testReMatchXpathFunctionWithChoice() throws Exception {
        getModelNode();
        //first case with xpath beginning from self path ie. re-match(./../../leaf1,"[a-z]+")
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<exfunctionCanister xmlns=\"urn:opendaylight:datastore-extension-functions-test\">"
                + " <test-re-match-container>"
                + "  <test-container-two>"
                + "   <leaf1>123abc</leaf1>"
                + "  </test-container-two>"
                + " </test-re-match-container>"
                + "</exfunctionCanister>";

        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, true);

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<exfunctionCanister xmlns=\"urn:opendaylight:datastore-extension-functions-test\">"
                + " <test-re-match-container>"
                + "  <test-container-two>"
                + "   <tango>"
                + "    <tango-leaf>10.131.228.113</tango-leaf>"
                + "   </tango>"
                + "  </test-container-two>"
                + " </test-re-match-container>"
                + "</exfunctionCanister>";

        response = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals("Violate must constraints: re-match(./../../leaf1,\"[a-z]+\")", response.getErrors().get(0).getErrorMessage());
        assertEquals(
                "/ds-ext-func:exfunctionCanister/ds-ext-func:test-re-match-container/ds-ext-func:test-container-two/ds-ext-func:tango/ds-ext-func:tango-leaf",
                response.getErrors().get(0).getErrorPath());

        //second case with leaflist : positive version
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<exfunctionCanister xmlns=\"urn:opendaylight:datastore-extension-functions-test\">"
                + " <test-re-match-container>"
                + "  <test-container-two>"
                + "   <charlie>"
                + "    <charlie-leaf>88</charlie-leaf>"
                + "    <charlie-leaf>99</charlie-leaf>"
                + "   </charlie>"
                + "  </test-container-two>"
                + " </test-re-match-container>"
                + "</exfunctionCanister>";

        editConfig(m_server, m_clientInfo, requestXml1, true);

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<exfunctionCanister xmlns=\"urn:opendaylight:datastore-extension-functions-test\">"
                + " <test-re-match-container>"
                + "  <test-container-two>"
                + "   <leaf2>test</leaf2>"
                + "  </test-container-two>"
                + " </test-re-match-container>"
                + "</exfunctionCanister>";

        editConfig(m_server, m_clientInfo, requestXml1, true);

        //second case with leaflist : negative version
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<exfunctionCanister xmlns=\"urn:opendaylight:datastore-extension-functions-test\">"
                + " <test-re-match-container>"
                + "  <test-container-two>"
                + "   <delta>"
                + "    <delta-leaf>44</delta-leaf>"
                + "    <delta-leaf>100</delta-leaf>"
                + "   </delta>"
                + "  </test-container-two>"
                + " </test-re-match-container>"
                + "</exfunctionCanister>";

        response = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals("Violate must constraints: re-match(/exfunctionCanister/test-re-match-container/test-container-two/charlie/charlie-leaf,\"[0-9]{2}\")", response.getErrors().get(0).getErrorMessage());
        assertEquals(
                "/ds-ext-func:exfunctionCanister/ds-ext-func:test-re-match-container/ds-ext-func:test-container-two/ds-ext-func:leaf2",
                response.getErrors().get(0).getErrorPath());

        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<exfunctionCanister xmlns=\"urn:opendaylight:datastore-extension-functions-test\">"
                + " <test-re-match-container>"
                + "  <test-container-two>"
                + "   <leaf3>test2</leaf3>"
                + "  </test-container-two>"
                + " </test-re-match-container>"
                + "</exfunctionCanister>";

        response = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals("Violate must constraints: re-match(/exfunctionCanister/test-re-match-container/test-container-two/delta/delta-leaf,\"[0-9]{2}\")", response.getErrors().get(0).getErrorMessage());
        assertEquals(
                "/ds-ext-func:exfunctionCanister/ds-ext-func:test-re-match-container/ds-ext-func:test-container-two/ds-ext-func:leaf3",
                response.getErrors().get(0).getErrorPath());
    }

    @Test
    public void testReMatchXpathFunctionWithCount() throws Exception {
        getModelNode();
        //create first interface
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<exfunctionCanister xmlns=\"urn:opendaylight:datastore-extension-functions-test\">"
                + " <test-re-match-container>"
                + "  <test-container-one>"
                + "   <test-interface>"
                + "    <name>eth0.1</name>"
                + "    <interface-data>eth0.1data</interface-data>"
                + "   </test-interface>"
                + "  </test-container-one>"
                + " </test-re-match-container>"
                + "</exfunctionCanister>";

        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, true);

        //this fails as greater than 1 ie. two interfaces are required which matches the pattern : etho0.<number>
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<exfunctionCanister xmlns=\"urn:opendaylight:datastore-extension-functions-test\">"
                + " <test-re-match-container>"
                + "  <test-container-one>"
                + "  <leaf5>With1ExistingInterface</leaf5>"
                + "  </test-container-one>"
                + " </test-re-match-container>"
                + "</exfunctionCanister>";

        response = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals("Violate must constraints: count(/exfunctionCanister/test-re-match-container/test-container-one/test-interface[re-match(name, \"eth0\\.\\d+\")]) > 1", response.getErrors().get(0).getErrorMessage());
        assertEquals(
                "/ds-ext-func:exfunctionCanister/ds-ext-func:test-re-match-container/ds-ext-func:test-container-one/ds-ext-func:leaf5",
                response.getErrors().get(0).getErrorPath());

        //create second interface
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<exfunctionCanister xmlns=\"urn:opendaylight:datastore-extension-functions-test\">"
                + " <test-re-match-container>"
                + "  <test-container-one>"
                + "   <test-interface>"
                + "    <name>eth0.2</name>"
                + "    <interface-data>eth0.2data</interface-data>"
                + "   </test-interface>"
                + "  </test-container-one>"
                + " </test-re-match-container>"
                + "</exfunctionCanister>";

        editConfig(m_server, m_clientInfo, requestXml1, true);

        //after two interfaces with eth0.<number> pattern , must condition evaluates to true
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<exfunctionCanister xmlns=\"urn:opendaylight:datastore-extension-functions-test\">"
                + " <test-re-match-container>"
                + "  <test-container-one>"
                + "  <leaf5>With2ExistingInterfaces</leaf5>"
                + "  </test-container-one>"
                + " </test-re-match-container>"
                + "</exfunctionCanister>";

        editConfig(m_server, m_clientInfo, requestXml1, true);
    }

    @Test
    public void testReMatchXpathFunctionWithCurrent() throws Exception {
        getModelNode();

        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<exfunctionCanister xmlns=\"urn:opendaylight:datastore-extension-functions-test\">"
                + " <test-re-match-container>"
                + "  <test-container-one>"
                + "   <test-interface>"
                + "    <name>if1</name>"
                + "    <interface-data>77</interface-data>"
                + "   </test-interface>"
                + "   <test-interface>"
                + "    <name>if2</name>"
                + "    <interface-data>999</interface-data>"
                + "   </test-interface>"
                + "  </test-container-one>"
                + " </test-re-match-container>"
                + "</exfunctionCanister>";

        editConfig(m_server, m_clientInfo, requestXml1, true);

        //case 1 : current() with CoreOperation Equal - positive scenario
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<exfunctionCanister xmlns=\"urn:opendaylight:datastore-extension-functions-test\">"
                + " <test-re-match-container>"
                + "  <test-container-one>"
                + "  <leaf7>if1</leaf7>"
                + "  </test-container-one>"
                + " </test-re-match-container>"
                + "</exfunctionCanister>";

        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml1, true);

        //case 1 : current() with CoreOperation Equal - negative scenario
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<exfunctionCanister xmlns=\"urn:opendaylight:datastore-extension-functions-test\">"
                + " <test-re-match-container>"
                + "  <test-container-one>"
                + "  <leaf7>if2</leaf7>"
                + "  </test-container-one>"
                + " </test-re-match-container>"
                + "</exfunctionCanister>";

        response = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals("Violate must constraints: re-match(./../test-interface[name = current()]/interface-data,\"[0-9]{2}\")", response.getErrors().get(0).getErrorMessage());
        assertEquals(
                "/ds-ext-func:exfunctionCanister/ds-ext-func:test-re-match-container/ds-ext-func:test-container-one/ds-ext-func:leaf7",
                response.getErrors().get(0).getErrorPath());

        //case 2 : current() without CoreOperation Equal - positive scenario
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<exfunctionCanister xmlns=\"urn:opendaylight:datastore-extension-functions-test\">"
                + " <test-re-match-container>"
                + "  <test-container-one>"
                + "  <leaf8>if1</leaf8>"
                + "  </test-container-one>"
                + " </test-re-match-container>"
                + "</exfunctionCanister>";

        response = editConfig(m_server, m_clientInfo, requestXml1, true);

        //case 2 : current() without CoreOperation Equal - negative scenario
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<exfunctionCanister xmlns=\"urn:opendaylight:datastore-extension-functions-test\">"
                + " <test-re-match-container>"
                + "  <test-container-one>"
                + "  <leaf8>if2</leaf8>"
                + "  </test-container-one>"
                + " </test-re-match-container>"
                + "</exfunctionCanister>";

        response = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals("Violate must constraints: re-match(./../test-interface[current()]/interface-data,\"[0-9]{2}\")", response.getErrors().get(0).getErrorMessage());
        assertEquals(
                "/ds-ext-func:exfunctionCanister/ds-ext-func:test-re-match-container/ds-ext-func:test-container-one/ds-ext-func:leaf8",
                response.getErrors().get(0).getErrorPath());

        //case 3 : current() node - positive scenario
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<exfunctionCanister xmlns=\"urn:opendaylight:datastore-extension-functions-test\">"
                + " <test-re-match-container>"
                + "  <test-container-one>"
                + "  <leaf9>99</leaf9>"
                + "  </test-container-one>"
                + " </test-re-match-container>"
                + "</exfunctionCanister>";

        response = editConfig(m_server, m_clientInfo, requestXml1, true);

        //case 3 : current() node - negative scenario
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<exfunctionCanister xmlns=\"urn:opendaylight:datastore-extension-functions-test\">"
                + " <test-re-match-container>"
                + "  <test-container-one>"
                + "  <leaf9>100</leaf9>"
                + "  </test-container-one>"
                + " </test-re-match-container>"
                + "</exfunctionCanister>";

        response = editConfig(m_server, m_clientInfo, requestXml1, false);
        assertEquals("Violate must constraints: re-match(current(),\"[0-9]{2}\")", response.getErrors().get(0).getErrorMessage());
        assertEquals(
                "/ds-ext-func:exfunctionCanister/ds-ext-func:test-re-match-container/ds-ext-func:test-container-one/ds-ext-func:leaf9",
                response.getErrors().get(0).getErrorPath());
    }
}
