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

import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.utils.AnnotationAnalysisException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.AddDefaultDataInterceptor;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeFactoryException;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;

@RunWith(RequestScopeJunitRunner.class)
public class SchemaMountAugmentUsesDSValidatorTest extends AbstractSchemaMountTest {

    @Before
    public void setup() throws SchemaBuildException, AnnotationAnalysisException, ModelNodeFactoryException {
        super.setup();
        MockitoAnnotations.initMocks(this);
        initialiseInterceptor();
    }

    @Override
    protected void initialiseInterceptor() {
        m_addDefaultDataInterceptor = new AddDefaultDataInterceptor(m_mountModelNodeHelperRegistry, m_mountRegistry, m_expValidator);
        m_addDefaultDataInterceptor.init();
    }

    @Test
    public void testWhenConstraintWithAugmentNodes() throws Exception {
        // type is AAA --> created augment node 'when-leaf'
        String request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <classifiers xmlns=\"schema-mount-test\">" +
                        "       <classifier>" +
                        "           <when-leaf>when</when-leaf>" +
                        "           <type>AAA</type>" +
                        "       </classifier>" +
                        "    </classifiers>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>";
        editConfig(request);

        // verify GET request
        String response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "  <data>"
                        + "   <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                        + "    <validation:xml-subtree>"
                        + "     <validation:plugType>PLUG-1.0</validation:plugType>"
                        + "     <validation:schemaMountPoint>"
                        + "      <smt:classifiers xmlns:smt=\"schema-mount-test\">"
                        + "      <smt:classifier>"
                        + "      <smt:when-leaf>when</smt:when-leaf>"
                        + "      <smt:type>AAA</smt:type>"
                        + "      </smt:classifier>"
                        + "      </smt:classifiers>"
                        + "     </validation:schemaMountPoint>"
                        + "    </validation:xml-subtree>"
                        + "   </validation:validation>"
                        + "  </data>"
                        + " </rpc-reply>";
        verifyGet(m_server, m_clientInfo, response);

        //modify 'type' to 'BBB', so 'when-leaf' node should be deleted because when constraint of augment will be evaluated to FALSE
        request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <classifiers xmlns=\"schema-mount-test\">" +
                        "       <classifier>" +
                        "           <type>BBB</type>" +
                        "       </classifier>" +
                        "    </classifiers>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>";
        editConfig(request);

        // verify GET request
        response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "  <data>"
                        + "   <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                        + "    <validation:xml-subtree>"
                        + "     <validation:plugType>PLUG-1.0</validation:plugType>"
                        + "     <validation:schemaMountPoint>"
                        + "      <smt:classifiers xmlns:smt=\"schema-mount-test\">"
                        + "      <smt:classifier>"
                        + "       <smt:type>BBB</smt:type>"
                        + "      </smt:classifier>"
                        + "      </smt:classifiers>"
                        + "     </validation:schemaMountPoint>"
                        + "    </validation:xml-subtree>"
                        + "   </validation:validation>"
                        + "  </data>"
                        + " </rpc-reply>"
        ;
        verifyGet(m_server, m_clientInfo, response);
    }

    @Test
    public void testWhenConstraintWithAugmentNodes_WhenViolation() throws Exception {

        // try to create augment node 'when-leaf' with type 'BBB' --> should be Failed
        String request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <classifiers xmlns=\"schema-mount-test\">" +
                        "       <classifier>" +
                        "           <type>BBB</type>" +
                        "           <when-leaf>augment-node</when-leaf>" +
                        "       </classifier>" +
                        "    </classifiers>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>";
        NetConfResponse netconfResponse = editConfigAsFalse(request);
        assertEquals(1, netconfResponse.getErrors().size());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/smt:classifiers/smt:classifier/smt:when-leaf", netconfResponse.getErrors().get(0).getErrorPath());
        assertEquals("Violate when constraints: ../classifier/type = 'AAA'", netconfResponse.getErrors().get(0).getErrorMessage());
        assertEquals(NetconfRpcErrorTag.UNKNOWN_ELEMENT, netconfResponse.getErrors().get(0).getErrorTag());
    }

    @Test
    public void testWhenConstraintWithAugmentNodes_WhenViolation_RefferingNodeDoesNotExist() throws Exception {

        // Try to create augment node with out when reffering node --> Should be Failed
        String request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <classifiers xmlns=\"schema-mount-test\">" +
                        "       <classifier>" +
                        "           <when-leaf>leaf1</when-leaf>" +
                        "       </classifier>" +
                        "    </classifiers>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>";
        NetConfResponse netconfResponse = editConfigAsFalse(request);
        assertEquals(1, netconfResponse.getErrors().size());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/smt:classifiers/smt:classifier/smt:when-leaf", netconfResponse.getErrors().get(0).getErrorPath());
        assertEquals("Violate when constraints: ../classifier/type = 'AAA'", netconfResponse.getErrors().get(0).getErrorMessage());
        assertEquals(NetconfRpcErrorTag.UNKNOWN_ELEMENT, netconfResponse.getErrors().get(0).getErrorTag());
    }

    @Test
    public void testWhenConstraintWithAugmentUses() throws Exception {

        // create augment node - should be succeed
        String request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <policies xmlns=\"schema-mount-test\"/>" +
                        "    <classifiers xmlns=\"schema-mount-test\">" +
                        "       <classifier>" +
                        "           <type>AAA</type>" +
                        "           <when-leaf>leaf1</when-leaf>" +
                        "       </classifier>" +
                        "    </classifiers>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>";
        editConfig(request);

        // Augment with when constraint --> verify GET request
        String response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "  <data>"
                        + "   <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                        + "    <validation:xml-subtree>"
                        + "     <validation:plugType>PLUG-1.0</validation:plugType>"
                        + "     <validation:schemaMountPoint>"
                        + "      <smt:classifiers xmlns:smt=\"schema-mount-test\">"
                        + "      <smt:classifier>"
                        + "      <smt:type>AAA</smt:type>"
                        + "      <smt:when-leaf>leaf1</smt:when-leaf>"
                        + "      </smt:classifier>"
                        + "      </smt:classifiers>"
                        + "      <smt:policies xmlns:smt=\"schema-mount-test\">"
                        + "         <smt:policyAugmentContainer/>"
                        + "         <smt:whenAugmentContainer/>"
                        + "         <smt:test-uses-without-when>"
                        + "            <smt:innerUsesContainer>"
                        + "               <smt:uses-inner-leaf1>test</smt:uses-inner-leaf1>"
                        + "            </smt:innerUsesContainer>"
                        + "         </smt:test-uses-without-when>"
                        + "         <smt:test-uses-with-when>"
                        + "            <smt:innerUsesContainer>"
                        + "               <smt:uses-inner-leaf1>test</smt:uses-inner-leaf1>"
                        + "            </smt:innerUsesContainer>"
                        + "         </smt:test-uses-with-when>"
                        + "         <smt:test>"
                        + "            <smt:device-connection-container/>"
                        + "         </smt:test>"
                        + "         <smt:device-connection/>"
                        + "      </smt:policies>"
                        + "     </validation:schemaMountPoint>"
                        + "    </validation:xml-subtree>"
                        + "   </validation:validation>"
                        + "  </data>"
                        + " </rpc-reply>";
        verifyGet(m_server, m_clientInfo, response);

        // create uses nodes - when condition should be true
        request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <classifiers xmlns=\"schema-mount-test\">" +
                        "       <classifier>" +
                        "           <state>up</state>" +
                        "           <models>" +
                        "               <model-number>001</model-number>" +
                        "           </models>" +
                        "       </classifier>" +
                        "    </classifiers>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>";
        editConfig(request);

        // Uses with when --> verify GET request
        response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "  <data>"
                        + "   <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                        + "    <validation:xml-subtree>"
                        + "     <validation:plugType>PLUG-1.0</validation:plugType>"
                        + "     <validation:schemaMountPoint>"
                        + "      <smt:classifiers xmlns:smt=\"schema-mount-test\">"
                        + "      <smt:classifier>"
                        + "      <smt:state>up</smt:state>"
                        + "      <smt:type>AAA</smt:type>"
                        + "      <smt:when-leaf>leaf1</smt:when-leaf>"
                        + "      <smt:models>"
                        + "        <smt:model-number>001</smt:model-number>"
                        + "      </smt:models>"
                        + "      </smt:classifier>"
                        + "      </smt:classifiers>"
                        + "      <smt:policies xmlns:smt=\"schema-mount-test\">"
                        + "         <smt:policyAugmentContainer/>"
                        + "         <smt:whenAugmentContainer/>"
                        + "         <smt:test-uses-without-when>"
                        + "            <smt:innerUsesContainer>"
                        + "               <smt:uses-inner-leaf1>test</smt:uses-inner-leaf1>"
                        + "            </smt:innerUsesContainer>"
                        + "         </smt:test-uses-without-when>"
                        + "         <smt:test-uses-with-when>"
                        + "            <smt:innerUsesContainer>"
                        + "               <smt:uses-inner-leaf1>test</smt:uses-inner-leaf1>"
                        + "            </smt:innerUsesContainer>"
                        + "         </smt:test-uses-with-when>"
                        + "         <smt:test>"
                        + "            <smt:device-connection-container/>"
                        + "         </smt:test>"
                        + "         <smt:device-connection/>"
                        + "      </smt:policies>"
                        + "     </validation:schemaMountPoint>"
                        + "    </validation:xml-subtree>"
                        + "   </validation:validation>"
                        + "  </data>"
                        + " </rpc-reply>";
        verifyGet(m_server, m_clientInfo, response);

        // modify state as 'down' --> when constraint should be failed for uses and uses's nodes should be removed.
        request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <classifiers xmlns=\"schema-mount-test\">" +
                        "       <classifier>" +
                        "           <state>down</state>" +
                        "       </classifier>" +
                        "    </classifiers>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>";
        editConfig(request);

        // Uses nodes should be deleted --> verify GET request
        response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "  <data>"
                        + "   <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                        + "    <validation:xml-subtree>"
                        + "     <validation:plugType>PLUG-1.0</validation:plugType>"
                        + "     <validation:schemaMountPoint>"
                        + "      <smt:classifiers xmlns:smt=\"schema-mount-test\">"
                        + "      <smt:classifier>"
                        + "      <smt:state>down</smt:state>"
                        + "      <smt:type>AAA</smt:type>"
                        + "      <smt:when-leaf>leaf1</smt:when-leaf>"
                        + "      </smt:classifier>"
                        + "      </smt:classifiers>"
                        + "      <smt:policies xmlns:smt=\"schema-mount-test\">"
                        + "         <smt:policyAugmentContainer/>"
                        + "         <smt:whenAugmentContainer/>"
                        + "         <smt:test-uses-without-when>"
                        + "            <smt:innerUsesContainer>"
                        + "               <smt:uses-inner-leaf1>test</smt:uses-inner-leaf1>"
                        + "            </smt:innerUsesContainer>"
                        + "         </smt:test-uses-without-when>"
                        + "         <smt:test-uses-with-when>"
                        + "            <smt:innerUsesContainer>"
                        + "               <smt:uses-inner-leaf1>test</smt:uses-inner-leaf1>"
                        + "            </smt:innerUsesContainer>"
                        + "         </smt:test-uses-with-when>"
                        + "         <smt:test>"
                        + "            <smt:device-connection-container/>"
                        + "         </smt:test>"
                        + "         <smt:device-connection/>"
                        + "      </smt:policies>"
                        + "     </validation:schemaMountPoint>"
                        + "    </validation:xml-subtree>"
                        + "   </validation:validation>"
                        + "  </data>"
                        + " </rpc-reply>";
        verifyGet(m_server, m_clientInfo, response);

        //modify 'type' to 'BBB', so augment nodes should be deleted because when constraint of augment will be evaluated to FALSE
        request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <classifiers xmlns=\"schema-mount-test\">" +
                        "       <classifier>" +
                        "           <type>BBB</type>" +
                        "       </classifier>" +
                        "    </classifiers>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>";
        editConfig(request);

        // verify GET request
        response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "  <data>"
                        + "   <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                        + "    <validation:xml-subtree>"
                        + "     <validation:plugType>PLUG-1.0</validation:plugType>"
                        + "     <validation:schemaMountPoint>"
                        + "      <smt:classifiers xmlns:smt=\"schema-mount-test\">"
                        + "      <smt:classifier>"
                        + "      <smt:state>down</smt:state>"
                        + "      <smt:type>BBB</smt:type>"
                        + "      </smt:classifier>"
                        + "      </smt:classifiers>"
                        + "      <smt:policies xmlns:smt=\"schema-mount-test\">"
                        + "         <smt:policyAugmentContainer/>"
                        + "         <smt:whenAugmentContainer/>"
                        + "         <smt:test-uses-without-when>"
                        + "            <smt:innerUsesContainer>"
                        + "               <smt:uses-inner-leaf1>test</smt:uses-inner-leaf1>"
                        + "            </smt:innerUsesContainer>"
                        + "         </smt:test-uses-without-when>"
                        + "         <smt:test-uses-with-when>"
                        + "            <smt:innerUsesContainer>"
                        + "               <smt:uses-inner-leaf1>test</smt:uses-inner-leaf1>"
                        + "            </smt:innerUsesContainer>"
                        + "         </smt:test-uses-with-when>"
                        + "         <smt:test>"
                        + "            <smt:device-connection-container/>"
                        + "         </smt:test>"
                        + "         <smt:device-connection/>"
                        + "      </smt:policies>"
                        + "     </validation:schemaMountPoint>"
                        + "    </validation:xml-subtree>"
                        + "   </validation:validation>"
                        + "  </data>"
                        + " </rpc-reply>";
        verifyGet(m_server, m_clientInfo, response);
    }

    @Test
    public void testUsesWithoutAugmentNodes_WhenViolation() throws Exception {

        // try to create uses nodes with out augment when referring nodes
        String request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <policies xmlns=\"schema-mount-test\"/>" +
                        "    <classifiers xmlns=\"schema-mount-test\">" +
                        "       <classifier>" +
                        "           <state>up</state>" +
                        "           <models>" +
                        "               <model-number>001</model-number>" +
                        "           </models>" +
                        "       </classifier>" +
                        "    </classifiers>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>";

        NetConfResponse netconfResponse = editConfigAsFalse(request);
        assertEquals(1, netconfResponse.getErrors().size());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/smt:classifiers/smt:classifier/smt:models", netconfResponse.getErrors().get(0).getErrorPath());
        assertEquals("Violate when constraints: ../classifier/type = 'AAA'", netconfResponse.getErrors().get(0).getErrorMessage());
        assertEquals(NetconfRpcErrorTag.UNKNOWN_ELEMENT, netconfResponse.getErrors().get(0).getErrorTag());
    }

    @Test
    public void testUsesWithWhen_WhenViolation() throws Exception {

        // Try to create uses nodes without uses-when referring nodes -- should be Failed
        String request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <policies xmlns=\"schema-mount-test\"/>" +
                        "    <classifiers xmlns=\"schema-mount-test\">" +
                        "       <classifier>" +
                        "           <type>AAA</type>" +
                        "           <models>" +
                        "               <model-number>001</model-number>" +
                        "           </models>" +
                        "       </classifier>" +
                        "    </classifiers>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>";

        NetConfResponse netconfResponse = editConfigAsFalse(request);
        assertEquals(1, netconfResponse.getErrors().size());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/smt:classifiers/smt:classifier/smt:models", netconfResponse.getErrors().get(0).getErrorPath());
        assertEquals("Violate when constraints: ../classifier/state = 'up'", netconfResponse.getErrors().get(0).getErrorMessage());
        assertEquals(NetconfRpcErrorTag.UNKNOWN_ELEMENT, netconfResponse.getErrors().get(0).getErrorTag());
    }

    @Test
    public void testNestedAugmentNodesWithWhenConstraints() throws Exception {

        // Created nested augment nodes
        String request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <policies xmlns=\"schema-mount-test\">" +
                        "       <policy>" +
                        "           <name>policy1</name>" +
                        "       </policy>" +
                        "    </policies>" +
                        "    <classifiers xmlns=\"schema-mount-test\">" +
                        "       <classifier>" +
                        "         <models>" +
                        "           <augmentContainer>" +
                        "               <leaf1>leaf1</leaf1>" +
                        "           </augmentContainer>" +
                        "           <model-number>001</model-number>" +
                        "         </models>" +
                        "         <type>AAA</type>" +
                        "         <state>up</state>" +
                        "       </classifier>" +
                        "    </classifiers>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>";

        editConfig(request);

        // verify GET request
        String response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "  <data>"
                        + "   <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                        + "    <validation:xml-subtree>"
                        + "     <validation:plugType>PLUG-1.0</validation:plugType>"
                        + "     <validation:schemaMountPoint>"
                        + "      <smt:classifiers xmlns:smt=\"schema-mount-test\">"
                        + "      <smt:classifier>"
                        + "      <smt:models>"
                        + "      <smt:model-number>001</smt:model-number>"
                        + "      <smt:augmentContainer>"
                        + "      <smt:leaf1>leaf1</smt:leaf1>"
                        + "       </smt:augmentContainer>"
                        + "      </smt:models>"
                        + "      <smt:type>AAA</smt:type>"
                        + "      <smt:state>up</smt:state>"
                        + "      </smt:classifier>"
                        + "      </smt:classifiers>"
                        + "      <smt:policies xmlns:smt=\"schema-mount-test\">"
                        + "      <smt:policy>"
                        + "      <smt:name>policy1</smt:name>"
                        + "      </smt:policy>"
                        + "      <smt:policyAugmentContainer/>"
                        + "      <smt:whenAugmentContainer/>"
                        + "      <smt:test-uses-without-when>"
                        + "         <smt:innerUsesContainer>"
                        + "            <smt:uses-inner-leaf1>test</smt:uses-inner-leaf1>"
                        + "         </smt:innerUsesContainer>"
                        + "      </smt:test-uses-without-when>"
                        + "      <smt:test-uses-with-when>"
                        + "         <smt:innerUsesContainer>"
                        + "            <smt:uses-inner-leaf1>test</smt:uses-inner-leaf1>"
                        + "         </smt:innerUsesContainer>"
                        + "      </smt:test-uses-with-when>"
                        + "      <smt:test>"
                        + "        <smt:device-connection-container/>"
                        + "      </smt:test>"
                        + "      <smt:device-connection/>"
                        + "      </smt:policies>"
                        + "     </validation:schemaMountPoint>"
                        + "    </validation:xml-subtree>"
                        + "   </validation:validation>"
                        + "  </data>"
                        + " </rpc-reply>";
        verifyGet(m_server, m_clientInfo, response);

        // remove policy list node and augment nodes should be removed due to augment-when failure
        request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <policies xmlns=\"schema-mount-test\">" +
                        "       <policy xc:operation=\"remove\">" +
                        "           <name>policy1</name>" +
                        "       </policy>" +
                        "    </policies>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>";

        editConfig(request);

        // verify GET request
        response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "  <data>"
                        + "   <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                        + "    <validation:xml-subtree>"
                        + "     <validation:plugType>PLUG-1.0</validation:plugType>"
                        + "     <validation:schemaMountPoint>"
                        + "      <smt:classifiers xmlns:smt=\"schema-mount-test\">"
                        + "      <smt:classifier>"
                        + "      <smt:models>"
                        + "      <smt:model-number>001</smt:model-number>"
                        + "      </smt:models>"
                        + "      <smt:type>AAA</smt:type>"
                        + "      <smt:state>up</smt:state>"
                        + "      </smt:classifier>"
                        + "      </smt:classifiers>"
                        + "      <smt:policies xmlns:smt=\"schema-mount-test\">"
                        + "       <smt:policyAugmentContainer/>"
                        + "      <smt:whenAugmentContainer/>"
                        + "      <smt:test-uses-without-when>"
                        + "         <smt:innerUsesContainer>"
                        + "            <smt:uses-inner-leaf1>test</smt:uses-inner-leaf1>"
                        + "         </smt:innerUsesContainer>"
                        + "      </smt:test-uses-without-when>"
                        + "      <smt:test-uses-with-when>"
                        + "         <smt:innerUsesContainer>"
                        + "            <smt:uses-inner-leaf1>test</smt:uses-inner-leaf1>"
                        + "         </smt:innerUsesContainer>"
                        + "      </smt:test-uses-with-when>"
                        + "      <smt:test>"
                        + "        <smt:device-connection-container/>"
                        + "      </smt:test>"
                        + "      <smt:device-connection/>"
                        + "      </smt:policies>"
                        + "     </validation:schemaMountPoint>"
                        + "    </validation:xml-subtree>"
                        + "   </validation:validation>"
                        + "  </data>"
                        + " </rpc-reply>";

        verifyGet(m_server, m_clientInfo, response);

        //try to create 'augmentContainer' --> count(/policies/policy/name) > 0 failed
        request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <classifiers xmlns=\"schema-mount-test\">" +
                        "       <classifier>" +
                        "         <models>" +
                        "           <augmentContainer>" +
                        "             <leaf1>new-leaf</leaf1>" +
                        "           </augmentContainer>" +
                        "         </models>" +
                        "       </classifier>" +
                        "    </classifiers>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>";

        NetConfResponse netconfResponse = editConfigAsFalse(request);
        assertEquals(1, netconfResponse.getErrors().size());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/smt:classifiers/smt:classifier/smt:models/smt:augmentContainer", netconfResponse.getErrors().get(0).getErrorPath());
        assertEquals("Violate when constraints: count(/policies/policy/name) > 0", netconfResponse.getErrors().get(0).getErrorMessage());
        assertEquals(NetconfRpcErrorTag.UNKNOWN_ELEMENT, netconfResponse.getErrors().get(0).getErrorTag());
    }

    @Test
    public void testNestedAugmentNodesWithChoiceCases_WhenConstraints() throws Exception {

        // Create Case1 nodes which was mentioned under augment --> should be failed due to augment-when not satisfied.
        String request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <policies xmlns=\"schema-mount-test\">" +
                        "       <policy>" +
                        "           <name>policy1</name>" +
                        "       </policy>" +
                        "       <policyAugmentContainer>" +
                        "           <case1leaf1>case1</case1leaf1>" +  // augment-when not satisfied
                        "       </policyAugmentContainer>" +
                        "    </policies>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>";

        NetConfResponse netconfResponse = editConfigAsFalse(request);
        assertEquals(1, netconfResponse.getErrors().size());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/smt:policies/smt:policyAugmentContainer/smt:case1leaf1", netconfResponse.getErrors().get(0).getErrorPath());
        assertEquals("Violate when constraints: /policies/augment-choice-when = 'when'", netconfResponse.getErrors().get(0).getErrorMessage());
        assertEquals(NetconfRpcErrorTag.UNKNOWN_ELEMENT, netconfResponse.getErrors().get(0).getErrorTag());

        //Create Case1 nodes --> augment-when satisfied
        request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <policies xmlns=\"schema-mount-test\">" +
                        "       <policy>" +
                        "           <name>policy1</name>" +
                        "       </policy>" +
                        "       <policyAugmentContainer>" +
                        "           <case1leaf1>case1</case1leaf1>" +
                        "       </policyAugmentContainer>" +
                        "       <augment-choice-when>when</augment-choice-when>" +
                        "    </policies>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>";

        editConfig(request);

        // verify GET request
        String response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "  <data>"
                        + "   <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                        + "    <validation:xml-subtree>"
                        + "     <validation:plugType>PLUG-1.0</validation:plugType>"
                        + "     <validation:schemaMountPoint>"
                        + "      <smt:policies xmlns:smt=\"schema-mount-test\">"
                        + "        <smt:augment-choice-when>when</smt:augment-choice-when>"
                        + "        <smt:policy>"
                        + "          <smt:name>policy1</smt:name>"
                        + "        </smt:policy>"
                        + "       <smt:policyAugmentContainer>"
                        + "         <smt:case1leaf1>case1</smt:case1leaf1>"
                        + "       </smt:policyAugmentContainer>"
                        + "      <smt:whenAugmentContainer/>"
                        + "      <smt:test-uses-without-when>"
                        + "         <smt:innerUsesContainer>"
                        + "            <smt:uses-inner-leaf1>test</smt:uses-inner-leaf1>"
                        + "         </smt:innerUsesContainer>"
                        + "      </smt:test-uses-without-when>"
                        + "      <smt:test-uses-with-when>"
                        + "         <smt:innerUsesContainer>"
                        + "            <smt:uses-inner-leaf1>test</smt:uses-inner-leaf1>"
                        + "         </smt:innerUsesContainer>"
                        + "      </smt:test-uses-with-when>"
                        + "      <smt:test>"
                        + "        <smt:device-connection-container/>"
                        + "      </smt:test>"
                        + "      <smt:device-connection/>"
                        + "      </smt:policies>"
                        + "     </validation:schemaMountPoint>"
                        + "    </validation:xml-subtree>"
                        + "   </validation:validation>"
                        + "  </data>"
                        + " </rpc-reply>";

        verifyGet(m_server, m_clientInfo, response);

        // remove augment-choice-when leaf
        request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <policies xmlns=\"schema-mount-test\">" +
                        "       <augment-choice-when>must</augment-choice-when>" +
                        "    </policies>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>";

        editConfig(request);

        // Verify GET response
        response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "  <data>"
                        + "   <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                        + "    <validation:xml-subtree>"
                        + "     <validation:plugType>PLUG-1.0</validation:plugType>"
                        + "     <validation:schemaMountPoint>"
                        + "      <smt:policies xmlns:smt=\"schema-mount-test\">"
                        + "      <smt:augment-choice-when>must</smt:augment-choice-when>"
                        + "      <smt:policy>"
                        + "      <smt:name>policy1</smt:name>"
                        + "      </smt:policy>"
                        + "      <smt:whenAugmentContainer/>"
                        + "      <smt:test-uses-without-when>"
                        + "       <smt:innerUsesContainer>"
                        + "        <smt:uses-inner-leaf1>test</smt:uses-inner-leaf1>"
                        + "       </smt:innerUsesContainer>"
                        + "      </smt:test-uses-without-when>"
                        + "      <smt:test>"
                        + "        <smt:device-connection-container/>"
                        + "      </smt:test>"
                        + "      <smt:device-connection/>"
                        + "     </smt:policies>"
                        + "     </validation:schemaMountPoint>"
                        + "    </validation:xml-subtree>"
                        + "   </validation:validation>"
                        + "  </data>"
                        + " </rpc-reply>";

        verifyGet(m_server, m_clientInfo, response);
    }

    @Test
    public void testNestedChoiceUnderUsesAndList() {
        //Actual UT case for FNMS-69450
        String request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <policies xmlns=\"schema-mount-test\">" +
                        "       <classifier-entry>" +
                        "           <name>cls01</name>" +
                        "           <classifier-action-entry-cfg>" +
                        "               <action-type>dei-marking</action-type>" +
                        "               <dei-marking-cfg>" +
                        "                   <dei-marking-list>" +
                        "                       <index>111</index>" +
                        "                       <dei-value>1</dei-value>" +
                        "                   </dei-marking-list>" +
                        "               </dei-marking-cfg>" +
                        "           </classifier-action-entry-cfg>" +
                        "           <match-criteria>" +
                        "               <flow-color >yellow</flow-color>" +
                        "           </match-criteria>" +
                        "       </classifier-entry>" +
                        "    </policies>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>";

        editConfig(request);

        request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <policies xmlns=\"schema-mount-test\">" +
                        "       <classifier-entry>" +
                        "           <name>cls01</name>" +
                        "           <classifier-action-entry-cfg>" +
                        "               <action-type>dei-marking</action-type>" +
                        "               <dei-marking-cfg>" +
                        "                   <dei-marking-list>" +
                        "                       <index>111</index>" +
                        "                       <dei-value>0</dei-value>" +
                        "                   </dei-marking-list>" +
                        "               </dei-marking-cfg>" +
                        "           </classifier-action-entry-cfg>" +
                        "       </classifier-entry>" +
                        "    </policies>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>";

        NetConfResponse netconfResponse = editConfigAsFalse(request);
        assertEquals(1, netconfResponse.getErrors().size());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/smt:policies/smt:classifier-entry[smt:name='cls01']/smt:match-criteria/smt:flow-color", netconfResponse.getErrors().get(0).getErrorPath());
        assertEquals("In classfier, if the filter is flow-color and the action is DEI-remarking, the remarked DEI value can only be 1!", netconfResponse.getErrors().get(0).getErrorMessage());
        assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, netconfResponse.getErrors().get(0).getErrorTag());

        request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <policies xmlns=\"schema-mount-test\">" +
                        "       <classifier-entry>" +
                        "           <name>cls01</name>" +
                        "           <classifier-action-entry-cfg>" +
                        "               <action-type>dei-marking</action-type>" +
                        "               <dei-marking-cfg>" +
                        "                   <dei-marking-list>" +
                        "                       <index>111</index>" +
                        "                       <dei-value>1</dei-value>" +
                        "                   </dei-marking-list>" +
                        "               </dei-marking-cfg>" +
                        "           </classifier-action-entry-cfg>" +
                        "       </classifier-entry>" +
                        "    </policies>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>";

        editConfig(request);

    }

    @Test
    public void testNestedChoiceUnderUsesAndList2() {
        String request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <policies xmlns=\"schema-mount-test\">" +
                        "       <classifier-entry>"+
                        "           <name>cls01</name>" +
                        "           <classifier-action-entry-cfg>" +
                        "               <action-type>dei-marking</action-type>" +
                        "               <dei-marking-cfg>" +
                        "                   <dei-marking-list>" +
                        "                       <index>111</index>" +
                        "                       <dei-value>1</dei-value>" +
                        "                   </dei-marking-list>" +
                        "               </dei-marking-cfg>" +
                        "           </classifier-action-entry-cfg>" +
                        "           <enhanced-filter>" +
                        "               <outside-choice-leaf>yellow</outside-choice-leaf>" +
                        "           </enhanced-filter>" +
                        "       </classifier-entry>"+
                        "    </policies>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>";

        editConfig(request);

        request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <policies xmlns=\"schema-mount-test\">" +
                        "       <classifier-entry>"+
                        "           <name>cls01</name>" +
                        "           <classifier-action-entry-cfg>" +
                        "               <action-type>dei-marking</action-type>" +
                        "               <dei-marking-cfg>" +
                        "                   <dei-marking-list>" +
                        "                       <index>111</index>" +
                        "                       <dei-value>0</dei-value>" +
                        "                   </dei-marking-list>" +
                        "               </dei-marking-cfg>" +
                        "           </classifier-action-entry-cfg>" +
                        "       </classifier-entry>"+
                        "    </policies>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>";

        NetConfResponse netconfResponse = editConfigAsFalse(request);
        assertEquals(1, netconfResponse.getErrors().size());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/smt:policies/smt:classifier-entry[smt:name='cls01']/smt:enhanced-filter/smt:outside-choice-leaf", netconfResponse.getErrors().get(0).getErrorPath());
        assertEquals("For outside-choice-leaf inside container, dei-value should be greater than 0!", netconfResponse.getErrors().get(0).getErrorMessage());
        assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, netconfResponse.getErrors().get(0).getErrorTag());
    }

    @Test
    public void testNestedChoiceUnderUsesAndList3() {
        String request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <policies xmlns=\"schema-mount-test\">" +
                        "       <classifier-entry>"+
                        "           <name>cls01</name>" +
                        "           <classifier-action-entry-cfg>" +
                        "               <action-type>dei-marking</action-type>" +
                        "               <dei-marking-cfg>" +
                        "                   <dei-marking-list>" +
                        "                       <index>111</index>" +
                        "                       <dei-value>1</dei-value>" +
                        "                   </dei-marking-list>" +
                        "               </dei-marking-cfg>" +
                        "           </classifier-action-entry-cfg>" +
                        "           <by-ref-container>" +
                        "               <by-ref-leaf>yellow</by-ref-leaf>" +
                        "           </by-ref-container>" +
                        "       </classifier-entry>"+
                        "    </policies>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>";

        editConfig(request);

        request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <policies xmlns=\"schema-mount-test\">" +
                        "       <classifier-entry>"+
                        "           <name>cls01</name>" +
                        "           <classifier-action-entry-cfg>" +
                        "               <action-type>dei-marking</action-type>" +
                        "               <dei-marking-cfg>" +
                        "                   <dei-marking-list>" +
                        "                       <index>111</index>" +
                        "                       <dei-value>0</dei-value>" +
                        "                   </dei-marking-list>" +
                        "               </dei-marking-cfg>" +
                        "           </classifier-action-entry-cfg>" +
                        "       </classifier-entry>"+
                        "    </policies>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>";

        NetConfResponse netconfResponse = editConfigAsFalse(request);
        assertEquals(1, netconfResponse.getErrors().size());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/smt:policies/smt:classifier-entry[smt:name='cls01']/smt:by-ref-container/smt:by-ref-leaf", netconfResponse.getErrors().get(0).getErrorPath());
        assertEquals("For by-ref inside container, dei-value should be greater than 0!", netconfResponse.getErrors().get(0).getErrorMessage());
        assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, netconfResponse.getErrors().get(0).getErrorTag());

    }

    @Test
    public void testNestedChoiceUnderUsesAndList4() {
        String request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <policies xmlns=\"schema-mount-test\">" +
                        "       <classifier-entry>"+
                        "           <name>cls01</name>" +
                        "           <classifier-action-entry-cfg>" +
                        "               <action-type>dei-marking</action-type>" +
                        "               <dei-marking-cfg>" +
                        "                   <dei-marking-list>" +
                        "                       <index>111</index>" +
                        "                       <dei-value>1</dei-value>" +
                        "                   </dei-marking-list>" +
                        "               </dei-marking-cfg>" +
                        "           </classifier-action-entry-cfg>" +
                        "           <any-frame-container>" +
                        "               <any-frame-augment-leaf>yellow</any-frame-augment-leaf>" +
                        "           </any-frame-container>" +
                        "       </classifier-entry>"+
                        "    </policies>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>";

        editConfig(request);

        request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <policies xmlns=\"schema-mount-test\">" +
                        "       <classifier-entry>"+
                        "           <name>cls01</name>" +
                        "           <classifier-action-entry-cfg>" +
                        "               <action-type>dei-marking</action-type>" +
                        "               <dei-marking-cfg>" +
                        "                   <dei-marking-list>" +
                        "                       <index>111</index>" +
                        "                       <dei-value>0</dei-value>" +
                        "                   </dei-marking-list>" +
                        "               </dei-marking-cfg>" +
                        "           </classifier-action-entry-cfg>" +
                        "       </classifier-entry>"+
                        "    </policies>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>";

        NetConfResponse netconfResponse = editConfigAsFalse(request);
        assertEquals(1, netconfResponse.getErrors().size());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/smt:policies/smt:classifier-entry[smt:name='cls01']/smt:any-frame-container/smt:any-frame-augment-leaf", netconfResponse.getErrors().get(0).getErrorPath());
        assertEquals("For any-frame-augment, dei-value should be greater than 0!", netconfResponse.getErrors().get(0).getErrorMessage());
        assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, netconfResponse.getErrors().get(0).getErrorTag());

    }

    @Test
    public void testNestedChoiceUnderUsesAndList5() {
        String request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <policies xmlns=\"schema-mount-test\">" +
                        "       <classifier-entry>"+
                        "           <name>cls01</name>" +
                        "           <classifier-action-entry-cfg>" +
                        "               <action-type>dei-marking</action-type>" +
                        "               <dei-marking-cfg>" +
                        "                   <dei-marking-list>" +
                        "                       <index>111</index>" +
                        "                       <dei-value>0</dei-value>" +
                        "                   </dei-marking-list>" +
                        "               </dei-marking-cfg>" +
                        "           </classifier-action-entry-cfg>" +
                        "           <outside-must-leaf>yellow</outside-must-leaf>" +
                        "       </classifier-entry>"+
                        "    </policies>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>";

        editConfig(request);

        request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <policies xmlns=\"schema-mount-test\">" +
                        "       <classifier-entry>"+
                        "           <name>cls01</name>" +
                        "           <classifier-action-entry-cfg>" +
                        "               <action-type>dei-marking</action-type>" +
                        "               <dei-marking-cfg>" +
                        "                   <dei-marking-list>" +
                        "                       <index>111</index>" +
                        "                       <dei-value>1</dei-value>" +
                        "                   </dei-marking-list>" +
                        "               </dei-marking-cfg>" +
                        "           </classifier-action-entry-cfg>" +
                        "       </classifier-entry>"+
                        "    </policies>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>";

        NetConfResponse netconfResponse = editConfigAsFalse(request);
        assertEquals(1, netconfResponse.getErrors().size());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/smt:policies/smt:classifier-entry[smt:name='cls01']/smt:outside-must-leaf", netconfResponse.getErrors().get(0).getErrorPath());
        assertEquals("For leaf outside choice, dei-value should be equal to 0!", netconfResponse.getErrors().get(0).getErrorMessage());
        assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, netconfResponse.getErrors().get(0).getErrorTag());
    }

    @Test
    public void testAugmentWithUsesUnderListAndChoice() throws Exception {
        // Create Case1 --> augment-when satisfied
        String request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <policies xmlns=\"schema-mount-test\">" +
                        "       <test-uses-with-when-under-choice>"+
                        "           <name>test</name>" +
                        "       </test-uses-with-when-under-choice>"+
                        "       <policy>" +
                        "           <name>policy1</name>" +
                        "       </policy>" +
                        "       <augment-choice-when>when</augment-choice-when>" +
                        "       <policyAugmentContainer>" +
                        "           <case1leaf1>case1</case1leaf1>" +
                        "       </policyAugmentContainer>" +
                        "    </policies>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>";

        editConfig(request);

        String response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "  <data>"
                        + "   <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                        + "    <validation:xml-subtree>"
                        + "     <validation:plugType>PLUG-1.0</validation:plugType>"
                        + "     <validation:schemaMountPoint>"
                        + "      <smt:policies xmlns:smt=\"schema-mount-test\">"
                        + "      <smt:augment-choice-when>when</smt:augment-choice-when>"
                        + "      <smt:policy>"
                        + "      <smt:name>policy1</smt:name>"
                        + "      </smt:policy>"
                        + "      <smt:policyAugmentContainer>"
                        + "       <smt:case1leaf1>case1</smt:case1leaf1>"
                        + "      </smt:policyAugmentContainer>"
                        + "      <smt:whenAugmentContainer/>"
                        + "      <smt:test-uses-without-when>"
                        + "         <smt:innerUsesContainer>"
                        + "            <smt:uses-inner-leaf1>test</smt:uses-inner-leaf1>"
                        + "         </smt:innerUsesContainer>"
                        + "      </smt:test-uses-without-when>"
                        + "      <smt:test-uses-with-when>"
                        + "         <smt:innerUsesContainer>"
                        + "            <smt:uses-inner-leaf1>test</smt:uses-inner-leaf1>"
                        + "         </smt:innerUsesContainer>"
                        + "      </smt:test-uses-with-when>"
                        + "      <smt:test-uses-with-when-under-choice>"
                        + "         <smt:innerUsesContainer>"
                        + "             <smt:uses-inner-leaf1>test</smt:uses-inner-leaf1>"
                        + "         </smt:innerUsesContainer>"
                        + "         <smt:name>test</smt:name>"
                        + "      </smt:test-uses-with-when-under-choice>"
                        + "      <smt:test>"
                        + "        <smt:device-connection-container/>"
                        + "      </smt:test>"
                        + "      <smt:device-connection/>"
                        + "     </smt:policies>"
                        + "     </validation:schemaMountPoint>"
                        + "    </validation:xml-subtree>"
                        + "   </validation:validation>"
                        + "  </data>"
                        + " </rpc-reply>";

        verifyGet(m_server, m_clientInfo, response);

        request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <policies xmlns=\"schema-mount-test\">" +
                        "       <test-uses-with-when-under-choice>"+
                        "           <name>test</name>" +
                        "           <inner-case-leaf>test</inner-case-leaf>" +
                        "       </test-uses-with-when-under-choice>"+
                        "    </policies>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>";

        editConfig(request);

        response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "  <data>"
                        + "   <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                        + "    <validation:xml-subtree>"
                        + "     <validation:plugType>PLUG-1.0</validation:plugType>"
                        + "     <validation:schemaMountPoint>"
                        + "      <smt:policies xmlns:smt=\"schema-mount-test\">"
                        + "      <smt:augment-choice-when>when</smt:augment-choice-when>"
                        + "      <smt:policy>"
                        + "      <smt:name>policy1</smt:name>"
                        + "      </smt:policy>"
                        + "      <smt:policyAugmentContainer>"
                        + "       <smt:case1leaf1>case1</smt:case1leaf1>"
                        + "      </smt:policyAugmentContainer>"
                        + "      <smt:whenAugmentContainer/>"
                        + "      <smt:test-uses-without-when>"
                        + "         <smt:innerUsesContainer>"
                        + "            <smt:uses-inner-leaf1>test</smt:uses-inner-leaf1>"
                        + "         </smt:innerUsesContainer>"
                        + "      </smt:test-uses-without-when>"
                        + "      <smt:test-uses-with-when>"
                        + "         <smt:innerUsesContainer>"
                        + "            <smt:uses-inner-leaf1>test</smt:uses-inner-leaf1>"
                        + "         </smt:innerUsesContainer>"
                        + "      </smt:test-uses-with-when>"
                        + "      <smt:test-uses-with-when-under-choice>"
                        + "         <smt:name>test</smt:name>"
                        + "         <smt:inner-case-leaf>test</smt:inner-case-leaf>"
                        + "      </smt:test-uses-with-when-under-choice>"
                        + "      <smt:test>"
                        + "        <smt:device-connection-container/>"
                        + "      </smt:test>"
                        + "      <smt:device-connection/>"
                        + "     </smt:policies>"
                        + "     </validation:schemaMountPoint>"
                        + "    </validation:xml-subtree>"
                        + "   </validation:validation>"
                        + "  </data>"
                        + " </rpc-reply>";

        verifyGet(m_server, m_clientInfo, response);
    }

    @Test
    public void testChoiceWithDefaultCaseHavingWhenDefaultLeaf() throws Exception{
        String request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <policies xmlns=\"schema-mount-test\">" +
                        "       <policy>" +
                        "           <name>policy1</name>" +
                        "       </policy>" +
                        "       <augment-choice-when>when</augment-choice-when>" +
                        "       <test>" +
                        "         <when-leaf>test</when-leaf>" +
                        "         <device-connection-container>" +
                        "           <duid>case1</duid>" +
                        "         </device-connection-container>" +
                        "       </test>" +
                        "       <device-connection>" +
                        "         <when-leaf>test</when-leaf>" +
                        "         <duid>case1</duid>" +
                        "       </device-connection>" +
                        "    </policies>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>";

        editConfig(request);

        String response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "  <data>"
                        + "   <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                        + "    <validation:xml-subtree>"
                        + "     <validation:plugType>PLUG-1.0</validation:plugType>"
                        + "     <validation:schemaMountPoint>"
                        + "      <smt:policies xmlns:smt=\"schema-mount-test\">"
                        + "      <smt:augment-choice-when>when</smt:augment-choice-when>"
                        + "      <smt:policy>"
                        + "      <smt:name>policy1</smt:name>"
                        + "      </smt:policy>"
                        + "      <smt:policyAugmentContainer/>"
                        + "      <smt:whenAugmentContainer/>"
                        + "      <smt:test-uses-without-when>"
                        + "         <smt:innerUsesContainer>"
                        + "            <smt:uses-inner-leaf1>test</smt:uses-inner-leaf1>"
                        + "         </smt:innerUsesContainer>"
                        + "      </smt:test-uses-without-when>"
                        + "      <smt:test-uses-with-when>"
                        + "         <smt:innerUsesContainer>"
                        + "            <smt:uses-inner-leaf1>test</smt:uses-inner-leaf1>"
                        + "         </smt:innerUsesContainer>"
                        + "      </smt:test-uses-with-when>"
                        + "      <smt:test>"
                        + "        <smt:device-connection-container>"
                        + "          <smt:duid>case1</smt:duid>"
                        + "        </smt:device-connection-container>"
                        + "        <smt:when-leaf>test</smt:when-leaf>"
                        + "      </smt:test>"
                        + "      <smt:device-connection>"
                        + "        <smt:when-leaf>test</smt:when-leaf>"
                        + "        <smt:duid>case1</smt:duid>"
                        + "      </smt:device-connection>"
                        + "     </smt:policies>"
                        + "     </validation:schemaMountPoint>"
                        + "    </validation:xml-subtree>"
                        + "   </validation:validation>"
                        + "  </data>"
                        + " </rpc-reply>";

        verifyGet(m_server, m_clientInfo, response);
    }

    @Test
    public void testNestedAugmentNodesWhenWithChoiceCases() throws Exception {

        // Create Case1 --> augment-when satisfied
        String request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <policies xmlns=\"schema-mount-test\">" +
                        "       <policy>" +
                        "           <name>policy1</name>" +
                        "       </policy>" +
                        "       <augment-choice-when>when</augment-choice-when>" +
                        "       <policyAugmentContainer>" +
                        "           <case1leaf1>case1</case1leaf1>" +
                        "       </policyAugmentContainer>" +
                        "    </policies>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>";

        editConfig(request);

        String response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "  <data>"
                        + "   <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                        + "    <validation:xml-subtree>"
                        + "     <validation:plugType>PLUG-1.0</validation:plugType>"
                        + "     <validation:schemaMountPoint>"
                        + "      <smt:policies xmlns:smt=\"schema-mount-test\">"
                        + "      <smt:augment-choice-when>when</smt:augment-choice-when>"
                        + "      <smt:policy>"
                        + "      <smt:name>policy1</smt:name>"
                        + "      </smt:policy>"
                        + "      <smt:policyAugmentContainer>"
                        + "       <smt:case1leaf1>case1</smt:case1leaf1>"
                        + "      </smt:policyAugmentContainer>"
                        + "      <smt:whenAugmentContainer/>"
                        + "      <smt:test-uses-without-when>"
                        + "         <smt:innerUsesContainer>"
                        + "            <smt:uses-inner-leaf1>test</smt:uses-inner-leaf1>"
                        + "         </smt:innerUsesContainer>"
                        + "      </smt:test-uses-without-when>"
                        + "      <smt:test-uses-with-when>"
                        + "         <smt:innerUsesContainer>"
                        + "            <smt:uses-inner-leaf1>test</smt:uses-inner-leaf1>"
                        + "         </smt:innerUsesContainer>"
                        + "      </smt:test-uses-with-when>"
                        + "      <smt:test>"
                        + "         <smt:device-connection-container/>"
                        + "      </smt:test>"
                        + "      <smt:device-connection/>"
                        + "     </smt:policies>"
                        + "     </validation:schemaMountPoint>"
                        + "    </validation:xml-subtree>"
                        + "   </validation:validation>"
                        + "  </data>"
                        + " </rpc-reply>";

        verifyGet(m_server, m_clientInfo, response);

        // Switch to Case2 Node 'case2leaf1'
        request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <policies xmlns=\"schema-mount-test\">" +
                        "       <policyAugmentContainer>" +
                        "           <case2leaf1>leaf1</case2leaf1>" +
                        "       </policyAugmentContainer>" +
                        "    </policies>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>";

        editConfig(request);

        // Verify GET request
        response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "  <data>"
                        + "   <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                        + "    <validation:xml-subtree>"
                        + "     <validation:plugType>PLUG-1.0</validation:plugType>"
                        + "     <validation:schemaMountPoint>"
                        + "      <smt:policies xmlns:smt=\"schema-mount-test\">"
                        + "      <smt:augment-choice-when>when</smt:augment-choice-when>"
                        + "      <smt:policy>"
                        + "      <smt:name>policy1</smt:name>"
                        + "      </smt:policy>"
                        + "      <smt:policyAugmentContainer>"
                        + "       <smt:case2leaf1>leaf1</smt:case2leaf1>"
                        + "      </smt:policyAugmentContainer>"
                        + "      <smt:whenAugmentContainer/>"
                        + "      <smt:test-uses-without-when>"
                        + "         <smt:innerUsesContainer>"
                        + "            <smt:uses-inner-leaf1>test</smt:uses-inner-leaf1>"
                        + "         </smt:innerUsesContainer>"
                        + "      </smt:test-uses-without-when>"
                        + "      <smt:test-uses-with-when>"
                        + "         <smt:innerUsesContainer>"
                        + "            <smt:uses-inner-leaf1>test</smt:uses-inner-leaf1>"
                        + "         </smt:innerUsesContainer>"
                        + "      </smt:test-uses-with-when>"
                        + "      <smt:test>"
                        + "         <smt:device-connection-container/>"
                        + "      </smt:test>"
                        + "      <smt:device-connection/>"
                        + "     </smt:policies>"
                        + "     </validation:schemaMountPoint>"
                        + "    </validation:xml-subtree>"
                        + "   </validation:validation>"
                        + "  </data>"
                        + " </rpc-reply>";

        verifyGet(m_server, m_clientInfo, response);

        // Added one more Case2 node 'case2leaf2'--> Augment-when will be TRUE here
        request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <policies xmlns=\"schema-mount-test\">" +
                        "       <policy>" +
                        "           <name>policy1</name>" +
                        "           <type>case2-type</type>" +
                        "       </policy>" +
                        "       <policyAugmentContainer>" +
                        "           <case2leaf2>leaf2</case2leaf2>" + // xpath expression --> when "/policies/policy/type = 'case2-type'"
                        "       </policyAugmentContainer>" +
                        "    </policies>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>";

        editConfig(request);

        //Verify GET request
        response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "  <data>"
                        + "   <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                        + "    <validation:xml-subtree>"
                        + "     <validation:plugType>PLUG-1.0</validation:plugType>"
                        + "     <validation:schemaMountPoint>"
                        + "      <smt:policies xmlns:smt=\"schema-mount-test\">"
                        + "      <smt:augment-choice-when>when</smt:augment-choice-when>"
                        + "      <smt:policy>"
                        + "      <smt:name>policy1</smt:name>"
                        + "      <smt:type>case2-type</smt:type>"
                        + "      </smt:policy>"
                        + "      <smt:policyAugmentContainer>"
                        + "       <smt:case2leaf1>leaf1</smt:case2leaf1>"
                        + "       <smt:case2leaf2>leaf2</smt:case2leaf2>"
                        + "      </smt:policyAugmentContainer>"
                        + "      <smt:whenAugmentContainer/>"
                        + "      <smt:test-uses-without-when>"
                        + "         <smt:innerUsesContainer>"
                        + "            <smt:uses-inner-leaf1>test</smt:uses-inner-leaf1>"
                        + "         </smt:innerUsesContainer>"
                        + "      </smt:test-uses-without-when>"
                        + "      <smt:test-uses-with-when>"
                        + "         <smt:innerUsesContainer>"
                        + "            <smt:uses-inner-leaf1>test</smt:uses-inner-leaf1>"
                        + "         </smt:innerUsesContainer>"
                        + "      </smt:test-uses-with-when>"
                        + "      <smt:test>"
                        + "         <smt:device-connection-container/>"
                        + "      </smt:test>"
                        + "      <smt:device-connection/>"
                        + "     </smt:policies>"
                        + "     </validation:schemaMountPoint>"
                        + "    </validation:xml-subtree>"
                        + "   </validation:validation>"
                        + "  </data>"
                        + " </rpc-reply>";

        verifyGet(m_server, m_clientInfo, response);

        // Try to add another Case2 leaf 'case2leaf3' -> uses-when will be failed
        request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <policies xmlns=\"schema-mount-test\">" +
                        "       <policyAugmentContainer>" +
                        "           <case2leaf3>leaf3</case2leaf3>" +
                        "       </policyAugmentContainer>" +
                        "    </policies>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>";

        NetConfResponse netconfResponse = editConfigAsFalse(request);
        assertEquals(1, netconfResponse.getErrors().size());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/smt:policies/smt:policyAugmentContainer/smt:case2leaf3", netconfResponse.getErrors().get(0).getErrorPath());
        assertEquals("Violate when constraints: count(/policies/policy/type) > 1", netconfResponse.getErrors().get(0).getErrorMessage());
        assertEquals(NetconfRpcErrorTag.UNKNOWN_ELEMENT, netconfResponse.getErrors().get(0).getErrorTag());

        // Add Case2 leaf node 'case2leaf3' --> uses-when should be TRUE
        request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <policies xmlns=\"schema-mount-test\">" +
                        "      <policy>" +
                        "      <name>policy2</name>" +
                        "      <type>type2</type>" +
                        "      </policy>" +
                        "       <policyAugmentContainer>" +
                        "           <case2leaf3>leaf3</case2leaf3>" +
                        "       </policyAugmentContainer>" +
                        "    </policies>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>";

        editConfig(request);

        // Verify GET response
        response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "  <data>"
                        + "   <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                        + "    <validation:xml-subtree>"
                        + "     <validation:plugType>PLUG-1.0</validation:plugType>"
                        + "     <validation:schemaMountPoint>"
                        + "      <smt:policies xmlns:smt=\"schema-mount-test\">"
                        + "      <smt:augment-choice-when>when</smt:augment-choice-when>"
                        + "      <smt:policy>"
                        + "      <smt:name>policy1</smt:name>"
                        + "      <smt:type>case2-type</smt:type>"
                        + "      </smt:policy>"
                        + "      <smt:policy>"
                        + "      <smt:name>policy2</smt:name>"
                        + "      <smt:type>type2</smt:type>"
                        + "      </smt:policy>"
                        + "      <smt:policyAugmentContainer>"
                        + "       <smt:case2leaf1>leaf1</smt:case2leaf1>"
                        + "       <smt:case2leaf2>leaf2</smt:case2leaf2>"
                        + "       <smt:case2leaf3>leaf3</smt:case2leaf3>"
                        + "      </smt:policyAugmentContainer>"
                        + "      <smt:whenAugmentContainer/>"
                        + "      <smt:test-uses-without-when>"
                        + "         <smt:innerUsesContainer>"
                        + "            <smt:uses-inner-leaf1>test</smt:uses-inner-leaf1>"
                        + "         </smt:innerUsesContainer>"
                        + "      </smt:test-uses-without-when>"
                        + "      <smt:test-uses-with-when>"
                        + "         <smt:innerUsesContainer>"
                        + "            <smt:uses-inner-leaf1>test</smt:uses-inner-leaf1>"
                        + "         </smt:innerUsesContainer>"
                        + "      </smt:test-uses-with-when>"
                        + "      <smt:test>"
                        + "         <smt:device-connection-container/>"
                        + "      </smt:test>"
                        + "      <smt:device-connection/>"
                        + "     </smt:policies>"
                        + "     </validation:schemaMountPoint>"
                        + "    </validation:xml-subtree>"
                        + "   </validation:validation>"
                        + "  </data>"
                        + " </rpc-reply>";

        verifyGet(m_server, m_clientInfo, response);

        //Switch to CASE3 Nodes 'case3leaf1' - augment-when will be Failed
        request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <policies xmlns=\"schema-mount-test\">" +
                        "       <policyAugmentContainer>" +
                        "           <case3leaf1>leaf1</case3leaf1>" +
                        "       </policyAugmentContainer>" +
                        "    </policies>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>";

        netconfResponse = editConfigAsFalse(request);
        assertEquals(1, netconfResponse.getErrors().size());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/smt:policies/smt:policyAugmentContainer/smt:case3leaf1", netconfResponse.getErrors().get(0).getErrorPath());
        assertEquals("Violate when constraints: /policies/policy/type = 'case3-type'", netconfResponse.getErrors().get(0).getErrorMessage());
        assertEquals(NetconfRpcErrorTag.UNKNOWN_ELEMENT, netconfResponse.getErrors().get(0).getErrorTag());

        //Switch to CASE3 Nodes 'case3leaf1' - augment-when will be Success
        request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <policies xmlns=\"schema-mount-test\">" +
                        "       <policy>" +
                        "           <name>policy1</name>" +
                        "           <type>case3-type</type>" +
                        "       </policy>" +
                        "       <policyAugmentContainer>" +
                        "           <case3leaf1>leaf1</case3leaf1>" +
                        "       </policyAugmentContainer>" +
                        "    </policies>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>";

        editConfig(request);

        // Verify GET response
        response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "  <data>"
                        + "   <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                        + "    <validation:xml-subtree>"
                        + "     <validation:plugType>PLUG-1.0</validation:plugType>"
                        + "     <validation:schemaMountPoint>"
                        + "      <smt:policies xmlns:smt=\"schema-mount-test\">"
                        + "      <smt:augment-choice-when>when</smt:augment-choice-when>"
                        + "      <smt:policy>"
                        + "      <smt:name>policy1</smt:name>"
                        + "      <smt:type>case3-type</smt:type>"
                        + "      </smt:policy>"
                        + "      <smt:policy>"
                        + "      <smt:name>policy2</smt:name>"
                        + "      <smt:type>type2</smt:type>"
                        + "      </smt:policy>"
                        + "      <smt:policyAugmentContainer>"
                        + "       <smt:case3leaf1>leaf1</smt:case3leaf1>"
                        + "      </smt:policyAugmentContainer>"
                        + "      <smt:whenAugmentContainer/>"
                        + "      <smt:test-uses-without-when>"
                        + "         <smt:innerUsesContainer>"
                        + "            <smt:uses-inner-leaf1>test</smt:uses-inner-leaf1>"
                        + "         </smt:innerUsesContainer>"
                        + "      </smt:test-uses-without-when>"
                        + "      <smt:test-uses-with-when>"
                        + "         <smt:innerUsesContainer>"
                        + "            <smt:uses-inner-leaf1>test</smt:uses-inner-leaf1>"
                        + "         </smt:innerUsesContainer>"
                        + "      </smt:test-uses-with-when>"
                        + "      <smt:test>"
                        + "         <smt:device-connection-container/>"
                        + "      </smt:test>"
                        + "      <smt:device-connection/>"
                        + "     </smt:policies>"
                        + "     </validation:schemaMountPoint>"
                        + "    </validation:xml-subtree>"
                        + "   </validation:validation>"
                        + "  </data>"
                        + " </rpc-reply>";
        verifyGet(m_server, m_clientInfo, response);
    }

    @Test
    public void testContainerUnderChoiceCase() throws Exception{
        String request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <policies xmlns=\"schema-mount-test\">" +
                        "       <augment-choice-when>default-choice</augment-choice-when>" +
                        "       <test-uses-without-when>" +
                        "       </test-uses-without-when>" +
                        "    </policies>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>";

        editConfig(request);

        String response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "  <data>"
                        + "   <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                        + "    <validation:xml-subtree>"
                        + "     <validation:plugType>PLUG-1.0</validation:plugType>"
                        + "     <validation:schemaMountPoint>"
                        + "      <smt:policies xmlns:smt=\"schema-mount-test\">"
                        + "      <smt:test-uses-without-when>"
                        + "      <smt:uses-inner-leaf>inner-uses</smt:uses-inner-leaf>"
                        + "      <smt:innerUsesContainer>"
                        + "        <smt:uses-inner-leaf1>test</smt:uses-inner-leaf1>"
                        + "      </smt:innerUsesContainer>"
                        + "      </smt:test-uses-without-when>"
                        + "      <smt:augment-choice-when>default-choice</smt:augment-choice-when>"
                        + "      <smt:policyAugmentContainer>"
                        + "       <smt:case22leaf1>case22</smt:case22leaf1>"
                        + "       <smt:uses-default-leaf>enable</smt:uses-default-leaf>"
                        + "      </smt:policyAugmentContainer>"
                        + "      <smt:whenAugmentContainer/>"
                        + "      <smt:test-uses-with-when>"
                        + "      <smt:uses-inner-leaf>inner-uses</smt:uses-inner-leaf>"
                        + "         <smt:innerUsesContainer>"
                        + "            <smt:uses-inner-leaf1>test</smt:uses-inner-leaf1>"
                        + "         </smt:innerUsesContainer>"
                        + "      </smt:test-uses-with-when>"
                        + "      <smt:test>"
                        + "         <smt:device-connection-container/>"
                        + "      </smt:test>"
                        + "      <smt:device-connection/>"
                        + "     </smt:policies>"
                        + "     </validation:schemaMountPoint>"
                        + "    </validation:xml-subtree>"
                        + "   </validation:validation>"
                        + "  </data>"
                        + " </rpc-reply>";
        verifyGet(m_server, m_clientInfo, response);

        request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <policies xmlns=\"schema-mount-test\">" +
                        "       <policyAugmentContainer>" +
                        "         <uses-default-leaf>disable</uses-default-leaf>" +
                        "       </policyAugmentContainer>" +
                        "    </policies>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>";

        editConfig(request);

        response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "  <data>"
                        + "   <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                        + "    <validation:xml-subtree>"
                        + "     <validation:plugType>PLUG-1.0</validation:plugType>"
                        + "     <validation:schemaMountPoint>"
                        + "      <smt:policies xmlns:smt=\"schema-mount-test\">"
                        + "      <smt:test-uses-without-when>"
                        + "      <smt:innerUsesContainer>"
                        + "        <smt:uses-inner-leaf1>test</smt:uses-inner-leaf1>"
                        + "      </smt:innerUsesContainer>"
                        + "      </smt:test-uses-without-when>"
                        + "      <smt:augment-choice-when>default-choice</smt:augment-choice-when>"
                        + "      <smt:policyAugmentContainer>"
                        + "       <smt:case22leaf1>case22</smt:case22leaf1>"
                        + "       <smt:uses-default-leaf>disable</smt:uses-default-leaf>"
                        + "      </smt:policyAugmentContainer>"
                        + "      <smt:whenAugmentContainer/>"
                        + "      <smt:test>"
                        + "       <smt:device-connection-container/>"
                        + "      </smt:test>"
                        + "      <smt:device-connection/>"
                        + "      <smt:test-uses-with-when/>"
                        + "     </smt:policies>"
                        + "     </validation:schemaMountPoint>"
                        + "    </validation:xml-subtree>"
                        + "   </validation:validation>"
                        + "  </data>"
                        + " </rpc-reply>";
        verifyGet(m_server, m_clientInfo, response);
    }

    @Test
    public void testUsesWithWhen() throws Exception {

            String request =
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                            " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                            "  <xml-subtree>" +
                            "   <plugType>PLUG-1.0</plugType>" +
                            "   <schemaMountPoint>" +
                            "    <policies xmlns=\"schema-mount-test\">" +
                            "       <augment-choice-when>default-choice</augment-choice-when>" +
                            "       <test-uses-with-when/>" +
                            "       <test-uses-without-when/>" +
                            "    </policies>" +
                            "   </schemaMountPoint>" +
                            "  </xml-subtree>" +
                            " </validation>";

            editConfig(request);

            String response =
                    "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                            + "  <data>"
                            + "   <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                            + "    <validation:xml-subtree>"
                            + "     <validation:plugType>PLUG-1.0</validation:plugType>"
                            + "     <validation:schemaMountPoint>"
                            + "      <smt:policies xmlns:smt=\"schema-mount-test\">"
                            + "      <smt:test-uses-without-when>"
                            + "      <smt:uses-inner-leaf>inner-uses</smt:uses-inner-leaf>"
                            + "      <smt:innerUsesContainer>"
                            + "        <smt:uses-inner-leaf1>test</smt:uses-inner-leaf1>"
                            + "      </smt:innerUsesContainer>"
                            + "      </smt:test-uses-without-when>"
                            + "      <smt:test-uses-with-when>"
                            + "      <smt:uses-inner-leaf>inner-uses</smt:uses-inner-leaf>"
                            + "      <smt:innerUsesContainer>"
                            + "        <smt:uses-inner-leaf1>test</smt:uses-inner-leaf1>"
                            + "      </smt:innerUsesContainer>"
                            + "      </smt:test-uses-with-when>"
                            + "      <smt:augment-choice-when>default-choice</smt:augment-choice-when>"
                            + "      <smt:policyAugmentContainer>"
                            + "       <smt:case22leaf1>case22</smt:case22leaf1>"
                            + "       <smt:uses-default-leaf>enable</smt:uses-default-leaf>"
                            + "      </smt:policyAugmentContainer>"
                            + "      <smt:whenAugmentContainer/>"
                            + "      <smt:test>"
                            + "       <smt:device-connection-container/>"
                            + "      </smt:test>"
                            + "      <smt:device-connection/>"
                            + "     </smt:policies>"
                            + "     </validation:schemaMountPoint>"
                            + "    </validation:xml-subtree>"
                            + "   </validation:validation>"
                            + "  </data>"
                            + " </rpc-reply>";
            verifyGet(m_server, m_clientInfo, response);

            request =
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                            " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                            "  <xml-subtree>" +
                            "   <plugType>PLUG-1.0</plugType>" +
                            "   <schemaMountPoint>" +
                            "    <policies xmlns=\"schema-mount-test\">" +
                            "       <policyAugmentContainer>" +
                            "         <uses-default-leaf>disable</uses-default-leaf>" +
                            "       </policyAugmentContainer>" +
                            "    </policies>" +
                            "   </schemaMountPoint>" +
                            "  </xml-subtree>" +
                            " </validation>";

            editConfig(request);

            response =
                    "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                            + "  <data>"
                            + "   <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                            + "    <validation:xml-subtree>"
                            + "     <validation:plugType>PLUG-1.0</validation:plugType>"
                            + "     <validation:schemaMountPoint>"
                            + "      <smt:policies xmlns:smt=\"schema-mount-test\">"
                            + "      <smt:test-uses-without-when>"
                            + "      <smt:innerUsesContainer>"
                            + "        <smt:uses-inner-leaf1>test</smt:uses-inner-leaf1>"
                            + "      </smt:innerUsesContainer>"
                            + "      </smt:test-uses-without-when>"
                            + "      <smt:augment-choice-when>default-choice</smt:augment-choice-when>"
                            + "      <smt:policyAugmentContainer>"
                            + "       <smt:case22leaf1>case22</smt:case22leaf1>"
                            + "       <smt:uses-default-leaf>disable</smt:uses-default-leaf>"
                            + "      </smt:policyAugmentContainer>"
                            + "      <smt:whenAugmentContainer/>"
                            + "      <smt:test>"
                            + "       <smt:device-connection-container/>"
                            + "      </smt:test>"
                            + "      <smt:device-connection/>"
                            + "      <smt:test-uses-with-when/>"
                            + "     </smt:policies>"
                            + "     </validation:schemaMountPoint>"
                            + "    </validation:xml-subtree>"
                            + "   </validation:validation>"
                            + "  </data>"
                            + " </rpc-reply>";
            verifyGet(m_server, m_clientInfo, response);

    }

    @Test
    public void testAugmentWithWhen() throws Exception {
        String request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <policies xmlns=\"schema-mount-test\">" +
                        "       <augment-choice-when>default-choice</augment-choice-when>" +
                        "       <test-uses-with-when/>" +
                        "       <test-uses-without-when/>" +
                        "       <uses-default-leaf2>disable</uses-default-leaf2>"+
                         "      <whenAugmentContainer>"+
                        "       <name>test</name>"+
                        "      </whenAugmentContainer>"+
                        "    </policies>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>";

        editConfig(request);

        String response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "  <data>"
                        + "   <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                        + "    <validation:xml-subtree>"
                        + "     <validation:plugType>PLUG-1.0</validation:plugType>"
                        + "     <validation:schemaMountPoint>"
                        + "      <smt:policies xmlns:smt=\"schema-mount-test\">"
                        + "      <smt:test-uses-without-when>"
                        + "      <smt:uses-inner-leaf>inner-uses</smt:uses-inner-leaf>"
                        + "      <smt:innerUsesContainer>"
                        + "        <smt:uses-inner-leaf1>test</smt:uses-inner-leaf1>"
                        + "      </smt:innerUsesContainer>"
                        + "      </smt:test-uses-without-when>"
                        + "      <smt:test-uses-with-when>"
                        + "      <smt:uses-inner-leaf>inner-uses</smt:uses-inner-leaf>"
                        + "      <smt:innerUsesContainer>"
                        + "        <smt:uses-inner-leaf1>test</smt:uses-inner-leaf1>"
                        + "      </smt:innerUsesContainer>"
                        + "      </smt:test-uses-with-when>"
                        + "      <smt:augment-choice-when>default-choice</smt:augment-choice-when>"
                        + "      <smt:policyAugmentContainer>"
                        + "       <smt:case22leaf1>case22</smt:case22leaf1>"
                        + "       <smt:uses-default-leaf>enable</smt:uses-default-leaf>"
                        + "      </smt:policyAugmentContainer>"
                        + "      <smt:uses-default-leaf2>disable</smt:uses-default-leaf2>"
                        + "      <smt:whenAugmentContainer>"
                        + "       <smt:name>test</smt:name>"
                        + "      <smt:dummy>"
                        + "       <smt:test1>test</smt:test1>"
                        + "      </smt:dummy>"
                        + "      </smt:whenAugmentContainer>"
                        + "      <smt:test>"
                        + "       <smt:device-connection-container/>"
                        + "      </smt:test>"
                        + "      <smt:device-connection/>"
                        + "     </smt:policies>"
                        + "     </validation:schemaMountPoint>"
                        + "    </validation:xml-subtree>"
                        + "   </validation:validation>"
                        + "  </data>"
                        + " </rpc-reply>";
        verifyGet(m_server, m_clientInfo, response);

        request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <policies xmlns=\"schema-mount-test\">" +
                        "       <uses-default-leaf2>enable</uses-default-leaf2>"+
                        "    </policies>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>";

        editConfig(request);

        response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "  <data>"
                        + "   <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                        + "    <validation:xml-subtree>"
                        + "     <validation:plugType>PLUG-1.0</validation:plugType>"
                        + "     <validation:schemaMountPoint>"
                        + "      <smt:policies xmlns:smt=\"schema-mount-test\">"
                        + "      <smt:test-uses-without-when>"
                        + "      <smt:uses-inner-leaf>inner-uses</smt:uses-inner-leaf>"
                        + "      <smt:innerUsesContainer>"
                        + "        <smt:uses-inner-leaf1>test</smt:uses-inner-leaf1>"
                        + "      </smt:innerUsesContainer>"
                        + "      </smt:test-uses-without-when>"
                        + "      <smt:test-uses-with-when>"
                        + "      <smt:uses-inner-leaf>inner-uses</smt:uses-inner-leaf>"
                        + "      <smt:innerUsesContainer>"
                        + "        <smt:uses-inner-leaf1>test</smt:uses-inner-leaf1>"
                        + "      </smt:innerUsesContainer>"
                        + "      </smt:test-uses-with-when>"
                        + "      <smt:augment-choice-when>default-choice</smt:augment-choice-when>"
                        + "      <smt:policyAugmentContainer>"
                        + "       <smt:case22leaf1>case22</smt:case22leaf1>"
                        + "       <smt:uses-default-leaf>enable</smt:uses-default-leaf>"
                        + "      </smt:policyAugmentContainer>"
                        + "      <smt:uses-default-leaf2>enable</smt:uses-default-leaf2>"
                        + "      <smt:test>"
                        + "       <smt:device-connection-container/>"
                        + "      </smt:test>"
                        + "      <smt:device-connection/>"
                        + "      <smt:whenAugmentContainer/>"
                        + "     </smt:policies>"
                        + "     </validation:schemaMountPoint>"
                        + "    </validation:xml-subtree>"
                        + "   </validation:validation>"
                        + "  </data>"
                        + " </rpc-reply>";

        verifyGet(m_server, m_clientInfo, response);

    }

    @Test
    public void testNestedChoiceWithDefaultCaseOnAugmentUsesNodes() throws Exception {

        // default choice/case nodes are initiated (case22 nodes)
        String request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <policies xmlns=\"schema-mount-test\">" +
                        "       <policy>" +
                        "           <name>policy1</name>" +
                        "       </policy>" +
                        "       <augment-choice-when>default-choice</augment-choice-when>" +
                        "    </policies>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>";

        editConfig(request);

        // Verify GET response
        String response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "  <data>"
                        + "   <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                        + "    <validation:xml-subtree>"
                        + "     <validation:plugType>PLUG-1.0</validation:plugType>"
                        + "     <validation:schemaMountPoint>"
                        + "      <smt:policies xmlns:smt=\"schema-mount-test\">"
                        + "      <smt:augment-choice-when>default-choice</smt:augment-choice-when>"
                        + "      <smt:policy>"
                        + "      <smt:name>policy1</smt:name>"
                        + "      </smt:policy>"
                        + "      <smt:policyAugmentContainer>"
                        + "       <smt:case22leaf1>case22</smt:case22leaf1>"
                        + "       <smt:uses-default-leaf>enable</smt:uses-default-leaf>"
                        + "      </smt:policyAugmentContainer>"
                        + "      <smt:test-uses-with-when>"
                        + "       <smt:innerUsesContainer>"
                        + "        <smt:uses-inner-leaf1>test</smt:uses-inner-leaf1>"
                        + "       </smt:innerUsesContainer>"
                        + "       <smt:uses-inner-leaf>inner-uses</smt:uses-inner-leaf>"
                        + "      </smt:test-uses-with-when>"
                        + "      <smt:test-uses-without-when>"
                        + "        <smt:innerUsesContainer>"
                        + "         <smt:uses-inner-leaf1>test</smt:uses-inner-leaf1>"
                        + "        </smt:innerUsesContainer>"
                        + "      </smt:test-uses-without-when>"
                        + "     <smt:whenAugmentContainer/>"
                        + "      <smt:test>"
                        + "        <smt:device-connection-container/>"
                        + "      </smt:test>"
                        + "      <smt:device-connection/>"
                        + "     </smt:policies>"
                        + "     </validation:schemaMountPoint>"
                        + "    </validation:xml-subtree>"
                        + "   </validation:validation>"
                        + "  </data>"
                        + " </rpc-reply>";
        verifyGet(m_server, m_clientInfo, response);

        // Switch Case22 to Case11 nodes
        request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <policies xmlns=\"schema-mount-test\">" +
                        "       <policyAugmentContainer>" +
                        "           <case11leaf1>case11</case11leaf1>" +
                        "            <case11container>"+
                        "               <case111leaf1>case111</case111leaf1>"+
                        "               <innerUsesContainer>"+
                        "                 <uses-inner-leaf1>uses-inner-leaf1</uses-inner-leaf1>"+
                        "               </innerUsesContainer>"+
                        "            </case11container>"+
                        "       </policyAugmentContainer>" +
                        "    </policies>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>";

        editConfig(request);

        // Verify GET response
        response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "  <data>"
                        + "   <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                        + "    <validation:xml-subtree>"
                        + "     <validation:plugType>PLUG-1.0</validation:plugType>"
                        + "     <validation:schemaMountPoint>"
                        + "      <smt:policies xmlns:smt=\"schema-mount-test\">"
                        + "      <smt:augment-choice-when>default-choice</smt:augment-choice-when>"
                        + "      <smt:policy>"
                        + "      <smt:name>policy1</smt:name>"
                        + "      </smt:policy>"
                        + "      <smt:policyAugmentContainer>"
                        + "       <smt:case11container>"
                        + "       <smt:case111leaf1>case111</smt:case111leaf1>"
                        //+ "         <smt:uses-inner-leaf>inner-uses</smt:uses-inner-leaf>" needs to be fixed in FNMS-44822
                        + "         <smt:innerUsesContainer>"
                        + "           <smt:uses-inner-leaf1>uses-inner-leaf1</smt:uses-inner-leaf1>"
                        + "         </smt:innerUsesContainer>"
                        + "        </smt:case11container>"
                        + "       <smt:case11leaf1>case11</smt:case11leaf1>"
                        + "       <smt:uses-default-leaf>enable</smt:uses-default-leaf>"
                        + "      </smt:policyAugmentContainer>"
                        + "      <smt:test-uses-with-when>"
                        + "       <smt:innerUsesContainer>"
                        + "        <smt:uses-inner-leaf1>test</smt:uses-inner-leaf1>"
                        + "       </smt:innerUsesContainer>"
                        + "       <smt:uses-inner-leaf>inner-uses</smt:uses-inner-leaf>"
                        + "      </smt:test-uses-with-when>"
                        + "      <smt:test-uses-without-when>"
                        + "        <smt:innerUsesContainer>"
                        + "         <smt:uses-inner-leaf1>test</smt:uses-inner-leaf1>"
                        + "        </smt:innerUsesContainer>"
                        + "      </smt:test-uses-without-when>"
                        + "     <smt:whenAugmentContainer/>"
                        + "      <smt:test>"
                        + "        <smt:device-connection-container/>"
                        + "      </smt:test>"
                        + "      <smt:device-connection/>"
                        + "     </smt:policies>"
                        + "     </validation:schemaMountPoint>"
                        + "    </validation:xml-subtree>"
                        + "   </validation:validation>"
                        + "  </data>"
                        + " </rpc-reply>";
        verifyGet(m_server, m_clientInfo, response);

        // modify 'uses-default-leaf' node value to 'disable'
        request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <policies xmlns=\"schema-mount-test\">" +
                        "       <policyAugmentContainer>" +
                        "         <uses-default-leaf>disable</uses-default-leaf>" +
                        "       </policyAugmentContainer>" +
                        "    </policies>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>";

        editConfig(request);

        // Verify GET response -- Uses nodes should be removed
        response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "  <data>"
                        + "   <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                        + "    <validation:xml-subtree>"
                        + "     <validation:plugType>PLUG-1.0</validation:plugType>"
                        + "     <validation:schemaMountPoint>"
                        + "      <smt:policies xmlns:smt=\"schema-mount-test\">"
                        + "      <smt:augment-choice-when>default-choice</smt:augment-choice-when>"
                        + "      <smt:policy>"
                        + "      <smt:name>policy1</smt:name>"
                        + "      </smt:policy>"
                        + "      <smt:policyAugmentContainer>"
                        + "       <smt:case11container>"
                        + "       <smt:case111leaf1>case111</smt:case111leaf1>"
                        + "        </smt:case11container>"
                        + "       <smt:case11leaf1>case11</smt:case11leaf1>"
                        + "       <smt:uses-default-leaf>disable</smt:uses-default-leaf>"
                        + "      </smt:policyAugmentContainer>"
                        + "      <smt:test-uses-without-when>"
                        + "        <smt:innerUsesContainer>"
                        + "         <smt:uses-inner-leaf1>test</smt:uses-inner-leaf1>"
                        + "        </smt:innerUsesContainer>"
                        + "      </smt:test-uses-without-when>"
                        + "     <smt:whenAugmentContainer/>"
                        + "      <smt:test>"
                        + "        <smt:device-connection-container/>"
                        + "      </smt:test>"
                        + "      <smt:device-connection/>"
                        + "      <smt:test-uses-with-when/>"
                        + "     </smt:policies>"
                        + "     </validation:schemaMountPoint>"
                        + "    </validation:xml-subtree>"
                        + "   </validation:validation>"
                        + "  </data>"
                        + " </rpc-reply>";
        verifyGet(m_server, m_clientInfo, response);

        // modify 'uses-default-leaf' node value to 'enable'
        request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <policies xmlns=\"schema-mount-test\">" +
                        "       <policyAugmentContainer>" +
                        "         <uses-default-leaf>enable</uses-default-leaf>" +
                        "       </policyAugmentContainer>" +
                        "    </policies>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>";

        editConfig(request);

        // Verify GET response
        response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "  <data>"
                        + "   <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                        + "    <validation:xml-subtree>"
                        + "     <validation:plugType>PLUG-1.0</validation:plugType>"
                        + "     <validation:schemaMountPoint>"
                        + "      <smt:policies xmlns:smt=\"schema-mount-test\">"
                        + "      <smt:augment-choice-when>default-choice</smt:augment-choice-when>"
                        + "      <smt:policy>"
                        + "      <smt:name>policy1</smt:name>"
                        + "      </smt:policy>"
                        + "      <smt:policyAugmentContainer>"
                        + "       <smt:case11container>"
                        + "       <smt:case111leaf1>case111</smt:case111leaf1>"
                        + "         <smt:uses-inner-leaf>inner-uses</smt:uses-inner-leaf>"
                        + "        </smt:case11container>"
                        + "       <smt:case11leaf1>case11</smt:case11leaf1>"
                        + "       <smt:uses-default-leaf>enable</smt:uses-default-leaf>"
                        + "      </smt:policyAugmentContainer>"
                        + "      <smt:test>"
                        + "       <smt:device-connection-container/>"
                        + "      </smt:test>"
                        + "      <smt:device-connection/>"
                        + "      <smt:test-uses-with-when/>"
                        + "     </smt:policies>"
                        + "     </validation:schemaMountPoint>"
                        + "    </validation:xml-subtree>"
                        + "   </validation:validation>"
                        + "  </data>"
                        + " </rpc-reply>";
       // verifyGet(m_server, m_clientInfo, response);
    }
}