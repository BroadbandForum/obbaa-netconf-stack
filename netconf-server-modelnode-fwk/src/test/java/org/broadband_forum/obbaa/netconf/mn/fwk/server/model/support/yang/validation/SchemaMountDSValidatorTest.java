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
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(RequestScopeJunitRunner.class)
public class SchemaMountDSValidatorTest extends AbstractSchemaMountTest{

    @Test
    public void testMustConstraintWithCoreFuntionWithCurrentOnlyBoolean() throws Exception {

        String request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" +
                        "       <classifiers>"+
                        "           <classifier-entry>"+
                        "               <name>cls_bac</name>"+
                        "               <classifier-action-entry-cfg>"+
                        "                   <action-type>blue</action-type>"+
                        "               </classifier-action-entry-cfg>"+
                        "               <filter>"+
                        "                   <name>test</name>"+
                        "               </filter>"+
                        "           </classifier-entry>"+
                        "       </classifiers>"+
                        "    </schemaMount>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;
        NetConfResponse netconfResponse = editConfigAsFalse(request); // Should Fail
        assertEquals(1,netconfResponse.getErrors().size());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/smt:schemaMount/smt:classifiers/smt:classifier-entry[smt:name='cls_bac']/smt:filter[smt:name='test']", netconfResponse.getErrors().get(0).getErrorPath());
        assertEquals("qos-policies filter action-type should be green or orange", netconfResponse.getErrors().get(0).getErrorMessage());
        assertEquals("must-violation", netconfResponse.getErrors().get(0).getErrorAppTag());
        assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, netconfResponse.getErrors().get(0).getErrorTag());       
    }

    @Test
    public void testMustConstraintWithCoreFuntionWithCurrentOnlyBooleanScenario2() throws Exception {

        String request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" +
                        "       <classifiers>"+
                        "           <classifier-entry>"+
                        "               <name>cls_bac</name>"+
                        "               <classifier-action-entry-cfg>"+
                        "                   <action-type>green</action-type>"+
                        "               </classifier-action-entry-cfg>"+
                        "               <filter>"+
                        "                   <name>test</name>"+
                        "               </filter>"+
                        "           </classifier-entry>"+
                        "       </classifiers>"+
                        "    </schemaMount>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;

        editConfig(request); // Should pass

        //Verify GET response
        String response = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" + 
                "  <data>\n" + 
                "    <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">\n" + 
                "      <validation:xml-subtree>\n" + 
                "        <validation:plugType>PLUG-1.0</validation:plugType>\n" + 
                "        <schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">\n" + 
                "          <smt:schemaMount xmlns:smt=\"schema-mount-test\">\n" + 
                "            <smt:classifiers>\n" + 
                "              <smt:classifier-entry>\n" + 
                "                <smt:classifier-action-entry-cfg>\n" + 
                "                  <smt:action-type>green</smt:action-type>\n" + 
                "                </smt:classifier-action-entry-cfg>\n" + 
                "                <smt:filter>\n" + 
                "                  <smt:name>test</smt:name>\n" + 
                "                </smt:filter>\n" + 
                "                <smt:name>cls_bac</smt:name>\n" + 
                "              </smt:classifier-entry>\n" + 
                "            </smt:classifiers>\n" + 
                "          </smt:schemaMount>\n" + 
                "        </schemaMountPoint>\n" + 
                "      </validation:xml-subtree>\n" + 
                "    </validation:validation>\n" + 
                "  </data>\n" + 
                "</rpc-reply>";
        verifyGet(m_server, m_clientInfo, response);  
        
        request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" +
                        "       <classifiers>"+
                        "           <classifier-entry>"+
                        "               <name>cls_bac</name>"+
                        "               <classifier-action-entry-cfg>"+
                        "                   <action-type>red</action-type>"+
                        "               </classifier-action-entry-cfg>"+
                        "           </classifier-entry>"+
                        "       </classifiers>"+
                        "    </schemaMount>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;

        NetConfResponse netconfResponse = editConfigAsFalse(request); // Should Fail
        assertEquals(1,netconfResponse.getErrors().size());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/smt:schemaMount/smt:classifiers/smt:classifier-entry[smt:name='cls_bac']/smt:filter[smt:name='test']", netconfResponse.getErrors().get(0).getErrorPath());
        assertEquals("qos-policies filter action-type should be green or orange", netconfResponse.getErrors().get(0).getErrorMessage());
        assertEquals("must-violation", netconfResponse.getErrors().get(0).getErrorAppTag());
        assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, netconfResponse.getErrors().get(0).getErrorTag());          
    }

    @Test
    public void testMustConstraintWithCoreFuntionWithCurrentOnlyBooleanScenario3() throws Exception {

        String request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" +
                        "       <classifiers>"+
                        "           <classifier-entry>"+
                        "               <name>cls_bac</name>"+
                        "               <classifier-action-entry-cfg>"+
                        "                   <action-type>orange</action-type>"+
                        "               </classifier-action-entry-cfg>"+
                        "               <filter>"+
                        "                   <name>test</name>"+
                        "               </filter>"+
                        "           </classifier-entry>"+
                        "       </classifiers>"+
                        "    </schemaMount>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;

        editConfig(request); // Should pass

        //Verify GET response
        String response = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" + 
                "  <data>\n" + 
                "    <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">\n" + 
                "      <validation:xml-subtree>\n" + 
                "        <validation:plugType>PLUG-1.0</validation:plugType>\n" + 
                "        <schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">\n" + 
                "          <smt:schemaMount xmlns:smt=\"schema-mount-test\">\n" + 
                "            <smt:classifiers>\n" + 
                "              <smt:classifier-entry>\n" + 
                "                <smt:classifier-action-entry-cfg>\n" + 
                "                  <smt:action-type>orange</smt:action-type>\n" + 
                "                </smt:classifier-action-entry-cfg>\n" + 
                "                <smt:filter>\n" + 
                "                  <smt:name>test</smt:name>\n" + 
                "                </smt:filter>\n" + 
                "                <smt:name>cls_bac</smt:name>\n" + 
                "              </smt:classifier-entry>\n" + 
                "            </smt:classifiers>\n" + 
                "          </smt:schemaMount>\n" + 
                "        </schemaMountPoint>\n" + 
                "      </validation:xml-subtree>\n" + 
                "    </validation:validation>\n" + 
                "  </data>\n" + 
                "</rpc-reply>";
        verifyGet(m_server, m_clientInfo, response);  
        
        request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" +
                        "       <classifiers>"+
                        "           <classifier-entry>"+
                        "               <name>cls_bac</name>"+
                        "               <classifier-action-entry-cfg xc:operation=\"delete\">"+
                        "                   <action-type>orange</action-type>"+
                        "               </classifier-action-entry-cfg>"+
                        "           </classifier-entry>"+
                        "       </classifiers>"+
                        "    </schemaMount>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;

        editConfig(request); // Should pass

        //Verify GET response
        response = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" + 
                "  <data>\n" + 
                "    <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">\n" + 
                "      <validation:xml-subtree>\n" + 
                "        <validation:plugType>PLUG-1.0</validation:plugType>\n" + 
                "        <schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">\n" + 
                "          <smt:schemaMount xmlns:smt=\"schema-mount-test\">\n" + 
                "            <smt:classifiers>\n" + 
                "              <smt:classifier-entry>\n" + 
                "                <smt:filter>\n" + 
                "                  <smt:name>test</smt:name>\n" + 
                "                </smt:filter>\n" + 
                "                <smt:name>cls_bac</smt:name>\n" + 
                "              </smt:classifier-entry>\n" + 
                "            </smt:classifiers>\n" + 
                "          </smt:schemaMount>\n" + 
                "        </schemaMountPoint>\n" + 
                "      </validation:xml-subtree>\n" + 
                "    </validation:validation>\n" + 
                "  </data>\n" + 
                "</rpc-reply>";
        verifyGet(m_server, m_clientInfo, response); 
    }

    // UT's for validating nodeset for core-operation
    @Test
    public void testMustConstraintWithCoreFuntion_NodeSetWithStringTypeValidation() throws Exception {

        // create classifier-entry list
        String request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" +
                        "		<classifiers>"+
                        "			<classifier-entry>"+
                        "				<name>cls_bac</name>"+
                        "				  <match-criteria>"+
                        "					<flow-color>green</flow-color>"+
                        "				  </match-criteria>"+
                        "				<classifier-action-entry-cfg>"+
                        "					<action-type>bac-color</action-type>"+
                        "				</classifier-action-entry-cfg>"+
                        "			</classifier-entry>"+
                        "		</classifiers>"+
                        "    </schemaMount>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;
        editConfig(request); // Should pass

        //Verify GET response
        String response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "  <data>"
                        + "   <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                        + "    <validation:xml-subtree>"
                        + "     <validation:plugType>PLUG-1.0</validation:plugType>" 
                        + "     <validation:schemaMountPoint>"
                        + "      <smt:schemaMount xmlns:smt=\"schema-mount-test\">"
                        + "		  <smt:classifiers>"
                        + "			<smt:classifier-entry>"
                        + "				<smt:name>cls_bac</smt:name>"
                        + "				  <smt:match-criteria>"
                        + "					<smt:flow-color>green</smt:flow-color>"
                        + "				  </smt:match-criteria>"
                        + "				<smt:classifier-action-entry-cfg>"
                        + "					<smt:action-type>bac-color</smt:action-type>"
                        + "				</smt:classifier-action-entry-cfg>"
                        + "			</smt:classifier-entry>"
                        + "		  </smt:classifiers>"
                        + "      </smt:schemaMount>"
                        + "     </validation:schemaMountPoint>"
                        + "    </validation:xml-subtree>"
                        + "   </validation:validation>"
                        + "  </data>"
                        + " </rpc-reply>"
                        ;
        verifyGet(m_server, m_clientInfo, response);

        // must constraint should fail
        request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" +
                        "		<classifiers>"+
                        "			<classifier-entry>"+
                        "				<name>cls_bac</name>"+
                        "				<classifier-action-entry-cfg>"+
                        "					<action-type>dei-marking</action-type>"+
                        "				</classifier-action-entry-cfg>"+
                        "			</classifier-entry>"+
                        "		</classifiers>"+
                        "    </schemaMount>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;
        NetConfResponse netconfResponse = editConfigAsFalse(request); // Should Fail
        assertEquals(1,netconfResponse.getErrors().size());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/smt:schemaMount/smt:classifiers/smt:classifier-entry[smt:name='cls_bac']/smt:match-criteria/smt:flow-color", netconfResponse.getErrors().get(0).getErrorPath());
        assertEquals("Violate must constraints: not(current()='green' and current()/../../classifier-action-entry-cfg/action-type = 'dei-marking')", netconfResponse.getErrors().get(0).getErrorMessage());
        assertEquals("must-violation", netconfResponse.getErrors().get(0).getErrorAppTag());
        assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, netconfResponse.getErrors().get(0).getErrorTag());
    }


    @Test
    public void testMustConstraintWithCoreOperation_NodeSetValidationInListWithSingleValue() throws Exception {

        //create list
        String request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" +
                        "		<classifiers>"+
                        "			<nodeset-list>"+
                        "				<name>list</name>"+
                        "			</nodeset-list>"+
                        "		</classifiers>"+
                        "    </schemaMount>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;
        editConfig(request); // Should pass

        // verify get response
        String response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "  <data>"
                        + "   <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                        + "    <validation:xml-subtree>"
                        + "     <validation:plugType>PLUG-1.0</validation:plugType>" 
                        + "     <validation:schemaMountPoint>"
                        + "      <smt:schemaMount xmlns:smt=\"schema-mount-test\">"
                        + "		  <smt:classifiers>"
                        + "			<smt:nodeset-list>"
                        + "				<smt:name>list</smt:name>"
                        + "			</smt:nodeset-list>"
                        + "		  </smt:classifiers>"
                        + "      </smt:schemaMount>"
                        + "     </validation:schemaMountPoint>"
                        + "    </validation:xml-subtree>"
                        + "   </validation:validation>"
                        + "  </data>"
                        + " </rpc-reply>"
                        ;
        verifyGet(m_server, m_clientInfo, response);

        // validate must constraint against the node-set of list
        request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" +
                        "		<classifiers>"+
                        "			<nodeset-list-validation>list</nodeset-list-validation>"+
                        "		</classifiers>"+
                        "    </schemaMount>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;

        editConfig(request); // Should pass
    }

    @Test
    public void testMustConstraintWithCoreOperation_NodeSetValidationInListWithMultipleValues() throws Exception {

        // create list with multiple entries
        String request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" +
                        "		<classifiers>"+
                        "			<nodeset-list>"+
                        "				<name>list1</name>"+
                        "			</nodeset-list>"+
                        "			<nodeset-list>"+
                        "				<name>list2</name>"+
                        "			</nodeset-list>"+
                        "			<nodeset-list>"+
                        "				<name>list3</name>"+
                        "			</nodeset-list>"+
                        "		</classifiers>"+
                        "    </schemaMount>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;

        editConfig(request); // Should pass

        // verify get response
        String response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "  <data>"
                        + "   <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                        + "    <validation:xml-subtree>"
                        + "     <validation:plugType>PLUG-1.0</validation:plugType>" 
                        + "     <validation:schemaMountPoint>"
                        + "      <smt:schemaMount xmlns:smt=\"schema-mount-test\">"
                        + "		  <smt:classifiers>"
                        + "			<smt:nodeset-list>"
                        + "				<smt:name>list1</smt:name>"
                        + "			</smt:nodeset-list>"
                        + "			<smt:nodeset-list>"
                        + "				<smt:name>list2</smt:name>"
                        + "			</smt:nodeset-list>"
                        + "			<smt:nodeset-list>"
                        + "				<smt:name>list3</smt:name>"
                        + "			</smt:nodeset-list>"
                        + "		  </smt:classifiers>"
                        + "      </smt:schemaMount>"
                        + "     </validation:schemaMountPoint>"
                        + "    </validation:xml-subtree>"
                        + "   </validation:validation>"
                        + "  </data>"
                        + " </rpc-reply>"
                        ;
        verifyGet(m_server, m_clientInfo, response);

        // validate must constraint against the node-set of list
        request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" +
                        "		<classifiers>"+
                        "			<nodeset-list-validation>list2</nodeset-list-validation>"+
                        "		</classifiers>"+
                        "    </schemaMount>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;

        editConfig(request); // Should pass

        // failure case - validating incorrect value
        request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" +
                        "		<classifiers>"+
                        "			<nodeset-list-validation>list5</nodeset-list-validation>"+
                        "		</classifiers>"+
                        "    </schemaMount>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;

        NetConfResponse netconfResponse = editConfigAsFalse(request); // Should Fail
        assertEquals(1,netconfResponse.getErrors().size());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/smt:schemaMount/smt:classifiers/smt:nodeset-list-validation", netconfResponse.getErrors().get(0).getErrorPath());
        assertEquals("Violate must constraints: /schemaMount/classifiers/nodeset-list/name = current()", netconfResponse.getErrors().get(0).getErrorMessage());
        assertEquals("must-violation", netconfResponse.getErrors().get(0).getErrorAppTag());
        assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, netconfResponse.getErrors().get(0).getErrorTag());
    }


    @Test
    public void testMustConstraintWithCoreOperation_NodeSetValidationInLeafListWithSingleValue() throws Exception {

        // create leaf-list node
        String request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" +
                        "		<classifiers>"+
                        "			<nodeset-leaflist>leaflist</nodeset-leaflist>"+
                        "		</classifiers>"+
                        "    </schemaMount>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;
        editConfig(request); // Should pass

        // verify get response
        String response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "  <data>"
                        + "   <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                        + "    <validation:xml-subtree>"
                        + "     <validation:plugType>PLUG-1.0</validation:plugType>" 
                        + "     <validation:schemaMountPoint>"
                        + "      <smt:schemaMount xmlns:smt=\"schema-mount-test\">"
                        + "		  <smt:classifiers>"
                        + "			<smt:nodeset-leaflist>leaflist</smt:nodeset-leaflist>"
                        + "		  </smt:classifiers>"
                        + "      </smt:schemaMount>"
                        + "     </validation:schemaMountPoint>"
                        + "    </validation:xml-subtree>"
                        + "   </validation:validation>"
                        + "  </data>"
                        + " </rpc-reply>"
                        ;
        verifyGet(m_server, m_clientInfo, response);

        // must constraint for validating nodeset with leaf-list
        request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" +
                        "		<classifiers>"+
                        "			<nodeset-leaflist-validation>leaflist</nodeset-leaflist-validation>"+
                        "		</classifiers>"+
                        "    </schemaMount>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;

        editConfig(request); // Should pass

        // verify get response
        response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "  <data>"
                        + "   <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                        + "    <validation:xml-subtree>"
                        + "     <validation:plugType>PLUG-1.0</validation:plugType>" 
                        + "     <validation:schemaMountPoint>"
                        + "      <smt:schemaMount xmlns:smt=\"schema-mount-test\">"
                        + "		  <smt:classifiers>"
                        + "			<smt:nodeset-leaflist>leaflist</smt:nodeset-leaflist>"
                        + "			<smt:nodeset-leaflist-validation>leaflist</smt:nodeset-leaflist-validation>"
                        + "		  </smt:classifiers>"
                        + "      </smt:schemaMount>"
                        + "     </validation:schemaMountPoint>"
                        + "    </validation:xml-subtree>"
                        + "   </validation:validation>"
                        + "  </data>"
                        + " </rpc-reply>"
                        ;
        verifyGet(m_server, m_clientInfo, response);

        // Failure case - validate incorrect leaf-list value
        request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" +
                        "		<classifiers>"+
                        "			<nodeset-leaflist-validation>leaflist2</nodeset-leaflist-validation>"+
                        "		</classifiers>"+
                        "    </schemaMount>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;

        NetConfResponse netconfResponse = editConfigAsFalse(request); // Should Fail
        assertEquals(1,netconfResponse.getErrors().size());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/smt:schemaMount/smt:classifiers/smt:nodeset-leaflist-validation", netconfResponse.getErrors().get(0).getErrorPath());
        assertEquals("Violate must constraints: /schemaMount/classifiers/nodeset-leaflist = current()", netconfResponse.getErrors().get(0).getErrorMessage());
        assertEquals("must-violation", netconfResponse.getErrors().get(0).getErrorAppTag());
        assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, netconfResponse.getErrors().get(0).getErrorTag());
    }
    @Test
    public void testMustConstraintWithCoreOperation_NodeSetValidationInLeafListWithMultipleValues() throws Exception {

        // create leaf-list with multiple values
        String request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" +
                        "		<classifiers>"+
                        "			<nodeset-leaflist>leaflist1</nodeset-leaflist>"+
                        "			<nodeset-leaflist>leaflist3</nodeset-leaflist>"+
                        "			<nodeset-leaflist>leaflist2</nodeset-leaflist>"+
                        "			<nodeset-leaflist>leaflist4</nodeset-leaflist>"+
                        "		</classifiers>"+
                        "    </schemaMount>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;
        editConfig(request); // Should pass

        // must constraint for validating node-set values
        request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" +
                        "		<classifiers>"+
                        "			<nodeset-leaflist-validation>leaflist2</nodeset-leaflist-validation>"+
                        "		</classifiers>"+
                        "    </schemaMount>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;
        editConfig(request); // Should pass

        // verify get response
        String response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "  <data>"
                        + "   <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                        + "    <validation:xml-subtree>"
                        + "     <validation:plugType>PLUG-1.0</validation:plugType>" 
                        + "     <validation:schemaMountPoint>"
                        + "      <smt:schemaMount xmlns:smt=\"schema-mount-test\">"
                        + "		  <smt:classifiers>"
                        + "			<smt:nodeset-leaflist>leaflist1</smt:nodeset-leaflist>"
                        + "			<smt:nodeset-leaflist>leaflist3</smt:nodeset-leaflist>"
                        + "			<smt:nodeset-leaflist>leaflist2</smt:nodeset-leaflist>"
                        + "			<smt:nodeset-leaflist>leaflist4</smt:nodeset-leaflist>"
                        + "			<smt:nodeset-leaflist-validation>leaflist2</smt:nodeset-leaflist-validation>"
                        + "		  </smt:classifiers>"
                        + "      </smt:schemaMount>"
                        + "     </validation:schemaMountPoint>"
                        + "    </validation:xml-subtree>"
                        + "   </validation:validation>"
                        + "  </data>"
                        + " </rpc-reply>"
                        ;
        verifyGet(m_server, m_clientInfo, response);

        // Failure case - validating incorrect leaf-list value against node-set of leaf-list 
        request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" +
                        "		<classifiers>"+
                        "			<nodeset-leaflist-validation>leaflist6</nodeset-leaflist-validation>"+
                        "		</classifiers>"+
                        "    </schemaMount>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;

        NetConfResponse netconfResponse = editConfigAsFalse(request); // Should Fail
        assertEquals(1,netconfResponse.getErrors().size());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/smt:schemaMount/smt:classifiers/smt:nodeset-leaflist-validation", netconfResponse.getErrors().get(0).getErrorPath());
        assertEquals("Violate must constraints: /schemaMount/classifiers/nodeset-leaflist = current()", netconfResponse.getErrors().get(0).getErrorMessage());
        assertEquals("must-violation", netconfResponse.getErrors().get(0).getErrorAppTag());
        assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, netconfResponse.getErrors().get(0).getErrorTag());
    }


    @Test
    public void testMustConstraintWithCompareTwoNodeSetValidationInLeafList() throws Exception {

        // create leaf-list with multiple values
        String request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" +
                        "		<classifiers>"+
                        "			<nodeset-leaflist>leaflist1</nodeset-leaflist>"+
                        "			<nodeset-leaflist>leaflist2</nodeset-leaflist>"+
                        "			<nodeset-leaflist>leaflist3</nodeset-leaflist>"+
                        "			<nodeset-leaflist>leaflist4</nodeset-leaflist>"+
                        "		</classifiers>"+
                        "    </schemaMount>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;
        editConfig(request); // Should pass


        // create another leaf-list with multiple values
        request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" +
                        "		<classifiers>"+
                        "			<nodeset-leaflist1>leaflist1</nodeset-leaflist1>"+
                        "           <nodeset-leaflist1>leaflist2</nodeset-leaflist1>"+
                        "			<nodeset-leaflist1>leaflist3</nodeset-leaflist1>"+
                        "			<nodeset-leaflist1>leaflist4</nodeset-leaflist1>"+
                        "		</classifiers>"+
                        "    </schemaMount>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;
        editConfig(request); // Should pass

        // verify get response
        String response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "  <data>"
                        + "   <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                        + "    <validation:xml-subtree>"
                        + "     <validation:plugType>PLUG-1.0</validation:plugType>" 
                        + "     <validation:schemaMountPoint>"
                        + "      <smt:schemaMount xmlns:smt=\"schema-mount-test\">"
                        + "		  <smt:classifiers>"
                        + "			<smt:nodeset-leaflist>leaflist1</smt:nodeset-leaflist>"
                        + "			<smt:nodeset-leaflist>leaflist2</smt:nodeset-leaflist>"
                        + "			<smt:nodeset-leaflist>leaflist3</smt:nodeset-leaflist>"
                        + "			<smt:nodeset-leaflist>leaflist4</smt:nodeset-leaflist>"
                        + "			<smt:nodeset-leaflist1>leaflist1</smt:nodeset-leaflist1>"
                        + "			<smt:nodeset-leaflist1>leaflist3</smt:nodeset-leaflist1>"
                        + "			<smt:nodeset-leaflist1>leaflist2</smt:nodeset-leaflist1>"
                        + "			<smt:nodeset-leaflist1>leaflist4</smt:nodeset-leaflist1>"
                        + "		  </smt:classifiers>"
                        + "      </smt:schemaMount>"
                        + "     </validation:schemaMountPoint>"
                        + "    </validation:xml-subtree>"
                        + "   </validation:validation>"
                        + "  </data>"
                        + " </rpc-reply>"
                        ;
        verifyGet(m_server, m_clientInfo, response);

        // validate two different nede-sets for leaf-list
        request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" +
                        "		<classifiers>"+
                        "			<two-nodeset-leaflist-validation>nodesetvalidation</two-nodeset-leaflist-validation>"+
                        "		</classifiers>"+
                        "    </schemaMount>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;
        editConfig(request); // Should pass

        // Failure case - Impact validation should be failed if remove one of the value from leaf-list nodeset
        request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" +
                        "		<classifiers>"+
                        "			<nodeset-leaflist1 xc:operation=\"remove\">leaflist1</nodeset-leaflist1>"+
                        "		</classifiers>"+
                        "    </schemaMount>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;
        NetConfResponse netconfResponse = editConfigAsFalse(request); // Should pass
        assertEquals(1,netconfResponse.getErrors().size());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/smt:schemaMount/smt:classifiers/smt:two-nodeset-leaflist-validation", netconfResponse.getErrors().get(0).getErrorPath());
        assertEquals("Violate must constraints: /schemaMount/classifiers/nodeset-leaflist = current()/../nodeset-leaflist1", netconfResponse.getErrors().get(0).getErrorMessage());
        assertEquals("must-violation", netconfResponse.getErrors().get(0).getErrorAppTag());
        assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, netconfResponse.getErrors().get(0).getErrorTag());

    }

    @Test
    public void testMustConstraintWithCompareTwoNodeSetValidationInLeafList_Fail() throws Exception {

        // create leaf-list with multiple values
        String request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" +
                        "		<classifiers>"+
                        "			<nodeset-leaflist>leaflist1</nodeset-leaflist>"+
                        "			<nodeset-leaflist>leaflist2</nodeset-leaflist>"+
                        "			<nodeset-leaflist>leaflist3</nodeset-leaflist>"+
                        "			<nodeset-leaflist>leaflist4</nodeset-leaflist>"+
                        "		</classifiers>"+
                        "    </schemaMount>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;
        editConfig(request); // Should pass


        // create leaf-list with multiple values
        request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" +
                        "		<classifiers>"+
                        "			<nodeset-leaflist1>leaflist1</nodeset-leaflist1>"+
                        "			<nodeset-leaflist1>leaflist3</nodeset-leaflist1>"+
                        "			<nodeset-leaflist1>leaflist4</nodeset-leaflist1>"+
                        "		</classifiers>"+
                        "    </schemaMount>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;
        editConfig(request); // Should pass

        // verify get response
        String response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "  <data>"
                        + "   <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                        + "    <validation:xml-subtree>"
                        + "     <validation:plugType>PLUG-1.0</validation:plugType>" 
                        + "     <validation:schemaMountPoint>"
                        + "      <smt:schemaMount xmlns:smt=\"schema-mount-test\">"
                        + "		  <smt:classifiers>"
                        + "			<smt:nodeset-leaflist>leaflist1</smt:nodeset-leaflist>"
                        + "			<smt:nodeset-leaflist>leaflist2</smt:nodeset-leaflist>"
                        + "			<smt:nodeset-leaflist>leaflist3</smt:nodeset-leaflist>"
                        + "			<smt:nodeset-leaflist>leaflist4</smt:nodeset-leaflist>"
                        + "			<smt:nodeset-leaflist1>leaflist1</smt:nodeset-leaflist1>"
                        + "			<smt:nodeset-leaflist1>leaflist3</smt:nodeset-leaflist1>"
                        + "			<smt:nodeset-leaflist1>leaflist4</smt:nodeset-leaflist1>"
                        + "		  </smt:classifiers>"
                        + "      </smt:schemaMount>"
                        + "     </validation:schemaMountPoint>"
                        + "    </validation:xml-subtree>"
                        + "   </validation:validation>"
                        + "  </data>"
                        + " </rpc-reply>"
                        ;
        verifyGet(m_server, m_clientInfo, response);

        // Failure - nede-set values are different for leaf-list
        request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" +
                        "		<classifiers>"+
                        "			<two-nodeset-leaflist-validation>nodesetvalidation</two-nodeset-leaflist-validation>"+
                        "		</classifiers>"+
                        "    </schemaMount>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;
        NetConfResponse netconfResponse = editConfigAsFalse(request); // Should pass
        assertEquals(1,netconfResponse.getErrors().size());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/smt:schemaMount/smt:classifiers/smt:two-nodeset-leaflist-validation", netconfResponse.getErrors().get(0).getErrorPath());
        assertEquals("Violate must constraints: /schemaMount/classifiers/nodeset-leaflist = current()/../nodeset-leaflist1", netconfResponse.getErrors().get(0).getErrorMessage());
        assertEquals("must-violation", netconfResponse.getErrors().get(0).getErrorAppTag());
        assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, netconfResponse.getErrors().get(0).getErrorTag());
    }

    @Test
    public void testMustConstraintWithCompareTwoNodeSetValidationForList() throws Exception {

        // create two lists with multiple entries
        String request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" +
                        "		<classifiers>"+
                        "			<nodeset-list>"+
                        "				<name>list1</name>"+
                        "			</nodeset-list>"+
                        "			<nodeset-list>"+
                        "				<name>list2</name>"+
                        "			</nodeset-list>"+
                        "			<nodeset-list1>"+
                        "				<name>list1</name>"+
                        "			</nodeset-list1>"+
                        "			<nodeset-list1>"+
                        "				<name>list2</name>"+
                        "			</nodeset-list1>"+
                        "		</classifiers>"+
                        "    </schemaMount>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;

        editConfig(request); // Should pass

        // verify get response
        String response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "  <data>"
                        + "   <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                        + "    <validation:xml-subtree>"
                        + "     <validation:plugType>PLUG-1.0</validation:plugType>" 
                        + "     <validation:schemaMountPoint>"
                        + "      <smt:schemaMount xmlns:smt=\"schema-mount-test\">"
                        + "		  <smt:classifiers>"
                        + "			<smt:nodeset-list>"
                        + "				<smt:name>list1</smt:name>"
                        + "			</smt:nodeset-list>"
                        + "			<smt:nodeset-list>"
                        + "				<smt:name>list2</smt:name>"
                        + "			</smt:nodeset-list>"
                        + "			<smt:nodeset-list1>"
                        + "				<smt:name>list1</smt:name>"
                        + "			</smt:nodeset-list1>"
                        + "			<smt:nodeset-list1>"
                        + "				<smt:name>list2</smt:name>"
                        + "			</smt:nodeset-list1>"
                        + "		  </smt:classifiers>"
                        + "      </smt:schemaMount>"
                        + "     </validation:schemaMountPoint>"
                        + "    </validation:xml-subtree>"
                        + "   </validation:validation>"
                        + "  </data>"
                        + " </rpc-reply>"
                        ;
        verifyGet(m_server, m_clientInfo, response);

        // validating two different  list node-sets 
        request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" +
                        "		<classifiers>"+
                        "			<two-nodeset-list-validation>nodesetvalidation</two-nodeset-list-validation>"+
                        "		</classifiers>"+
                        "    </schemaMount>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;
        editConfig(request); // Should pass

        // Failure case - Impact validation should be failed if remove one of the list entries from one list
        request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" +
                        "		<classifiers>"+
                        "			<nodeset-list1 xc:operation=\"remove\">"+
                        "				<name>list1</name>"+
                        "			</nodeset-list1>"+
                        "		</classifiers>"+
                        "    </schemaMount>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;
        NetConfResponse netconfResponse = editConfigAsFalse(request); // Should pass
        assertEquals(1,netconfResponse.getErrors().size());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/smt:schemaMount/smt:classifiers/smt:two-nodeset-list-validation", netconfResponse.getErrors().get(0).getErrorPath());
        assertEquals("Violate must constraints: /schemaMount/classifiers/nodeset-list1/name = current()/../nodeset-list/name", netconfResponse.getErrors().get(0).getErrorMessage());
        assertEquals("must-violation", netconfResponse.getErrors().get(0).getErrorAppTag());
        assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, netconfResponse.getErrors().get(0).getErrorTag());
    }

    @Test
    public void testMustConstraintWithCompareTwoNodeSetValidationForList_Fail() throws Exception {

        // create leaf-list with multiple values
        String request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" +
                        "		<classifiers>"+
                        "			<nodeset-list>"+
                        "				<name>list1</name>"+
                        "			</nodeset-list>"+
                        "			<nodeset-list>"+
                        "				<name>list2</name>"+
                        "			</nodeset-list>"+
                        "			<nodeset-list1>"+
                        "				<name>list2</name>"+
                        "			</nodeset-list1>"+
                        "			<nodeset-list1>"+
                        "				<name>list1</name>"+
                        "			</nodeset-list1>"+
                        "			<nodeset-list1>"+
                        "				<name>list5</name>"+
                        "			</nodeset-list1>"+
                        "		</classifiers>"+
                        "    </schemaMount>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;

        editConfig(request); // Should pass

        // verify get response
        String response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "  <data>"
                        + "   <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                        + "    <validation:xml-subtree>"
                        + "     <validation:plugType>PLUG-1.0</validation:plugType>" 
                        + "     <validation:schemaMountPoint>"
                        + "      <smt:schemaMount xmlns:smt=\"schema-mount-test\">"
                        + "		  <smt:classifiers>"
                        + "			<smt:nodeset-list>"
                        + "				<smt:name>list1</smt:name>"
                        + "			</smt:nodeset-list>"
                        + "			<smt:nodeset-list>"
                        + "				<smt:name>list2</smt:name>"
                        + "			</smt:nodeset-list>"
                        + "			<smt:nodeset-list1>"
                        + "				<smt:name>list2</smt:name>"
                        + "			</smt:nodeset-list1>"
                        + "			<smt:nodeset-list1>"
                        + "				<smt:name>list1</smt:name>"
                        + "			</smt:nodeset-list1>"
                        + "			<smt:nodeset-list1>"
                        + "				<smt:name>list5</smt:name>"
                        + "			</smt:nodeset-list1>"
                        + "		  </smt:classifiers>"
                        + "      </smt:schemaMount>"
                        + "     </validation:schemaMountPoint>"
                        + "    </validation:xml-subtree>"
                        + "   </validation:validation>"
                        + "  </data>"
                        + " </rpc-reply>"
                        ;
        verifyGet(m_server, m_clientInfo, response);

        // Failure case - both the list having different node-sets
        request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" +
                        "		<classifiers>"+
                        "			<two-nodeset-list-validation>nodesetvalidation</two-nodeset-list-validation>"+
                        "		</classifiers>"+
                        "    </schemaMount>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;
        NetConfResponse netconfResponse = editConfigAsFalse(request); // Should pass
        assertEquals(1,netconfResponse.getErrors().size());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/smt:schemaMount/smt:classifiers/smt:two-nodeset-list-validation", netconfResponse.getErrors().get(0).getErrorPath());
        assertEquals("Violate must constraints: /schemaMount/classifiers/nodeset-list1/name = current()/../nodeset-list/name", netconfResponse.getErrors().get(0).getErrorMessage());
        assertEquals("must-violation", netconfResponse.getErrors().get(0).getErrorAppTag());
        assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, netconfResponse.getErrors().get(0).getErrorTag());
    }

    @Test
    public void testLeafMustConstraintWithCoreFunction() throws Exception {
        // create multicast-vpn list with ipv6 address
        String request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <smt:multicast xmlns:smt=\"schema-mount-test\">" +
                        "     <smt:mgmd>" +
                        "      <smt:multicast-vpn>" +
                        "        <smt:name>multicast</smt:name>" +
                        "        <smt:ipv6-address>ABFF::FF01:1</smt:ipv6-address>"+
                        "      </smt:multicast-vpn>" +
                        "     </smt:mgmd>" +
                        "    </smt:multicast>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;
        editConfig(request); // Should pass

        //verify GET request
        String response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"+
                        "<data>"+
                        "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"+
                        "<validation:xml-subtree>"+
                        "<validation:plugType>PLUG-1.0</validation:plugType>"+
                        "<schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">"+
                        "<smt:multicast xmlns:smt=\"schema-mount-test\">"+
                        "<smt:mgmd>"+
                        "<smt:multicast-vpn>"+
                        "<smt:name>multicast</smt:name>"+
                        "<smt:ipv6-address>ABFF::FF01:1</smt:ipv6-address>"+
                        "</smt:multicast-vpn>"+
                        "</smt:mgmd>"+
                        "</smt:multicast>"+
                        "</schemaMountPoint>"+
                        "</validation:xml-subtree>"+
                        "</validation:validation>"+
                        "</data>"+
                        "</rpc-reply>";
        verifyGet(response);

        // modify ipv6-address with different value '::0'
        request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <smt:multicast xmlns:smt=\"schema-mount-test\">" +
                        "     <smt:mgmd>" +
                        "      <smt:multicast-vpn>" +
                        "        <smt:name>multicast</smt:name>" +
                        "        <smt:ipv6-address>::0</smt:ipv6-address>"+
                        "      </smt:multicast-vpn>" +
                        "     </smt:mgmd>" +
                        "    </smt:multicast>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;
        editConfig(request); // Should pass

        //verify GET request
        response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"+
                        "<data>"+
                        "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"+
                        "<validation:xml-subtree>"+
                        "<validation:plugType>PLUG-1.0</validation:plugType>"+
                        "<schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">"+
                        "<smt:multicast xmlns:smt=\"schema-mount-test\">"+
                        "<smt:mgmd>"+
                        "<smt:multicast-vpn>"+
                        "<smt:name>multicast</smt:name>"+
                        "<smt:ipv6-address>::0</smt:ipv6-address>"+
                        "</smt:multicast-vpn>"+
                        "</smt:mgmd>"+
                        "</smt:multicast>"+
                        "</schemaMountPoint>"+
                        "</validation:xml-subtree>"+
                        "</validation:validation>"+
                        "</data>"+
                        "</rpc-reply>";
        verifyGet(response);

        // modify ipv6-address starting with 'FF'
        request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <smt:multicast xmlns:smt=\"schema-mount-test\">" +
                        "     <smt:mgmd>" +
                        "      <smt:multicast-vpn>" +
                        "        <smt:name>multicast</smt:name>" +
                        "        <smt:ipv6-address>FF03::FF01:1</smt:ipv6-address>"+
                        "      </smt:multicast-vpn>" +
                        "     </smt:mgmd>" +
                        "    </smt:multicast>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;
        NetConfResponse netconfResponse = editConfigAsFalse(request); // Should Fail
        assertEquals(1,netconfResponse.getErrors().size());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/smt:multicast/smt:mgmd/smt:multicast-vpn[smt:name='multicast']/smt:ipv6-address", netconfResponse.getErrors().get(0).getErrorPath());
        assertEquals("ipv6-address of network-interface can not be multicast address", netconfResponse.getErrors().get(0).getErrorMessage());
        assertEquals("must-violation", netconfResponse.getErrors().get(0).getErrorAppTag());
        assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, netconfResponse.getErrors().get(0).getErrorTag());

        // modify ipv6-address starting with 'fF'
        request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <smt:multicast xmlns:smt=\"schema-mount-test\">" +
                        "     <smt:mgmd>" +
                        "      <smt:multicast-vpn>" +
                        "        <smt:name>multicast</smt:name>" +
                        "        <smt:ipv6-address>fF06::FF01:1</smt:ipv6-address>"+
                        "      </smt:multicast-vpn>" +
                        "     </smt:mgmd>" +
                        "    </smt:multicast>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;
        netconfResponse = editConfigAsFalse(request); // Should Fail
        assertEquals(1,netconfResponse.getErrors().size());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/smt:multicast/smt:mgmd/smt:multicast-vpn[smt:name='multicast']/smt:ipv6-address", netconfResponse.getErrors().get(0).getErrorPath());
        assertEquals("ipv6-address of network-interface can not be multicast address", netconfResponse.getErrors().get(0).getErrorMessage());
        assertEquals("must-violation", netconfResponse.getErrors().get(0).getErrorAppTag());
        assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, netconfResponse.getErrors().get(0).getErrorTag());
    }

    @Test
    public void testDefaultLeafInitiationForWhenConstraintWithUses() throws Exception {
        // Default leafs should be initiated if only uses-when becomes TRUE  
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                "     <if:interfaces xmlns:if=\"test-interfaces\">"+
                "      <if:interface>"+
                "       <if:name>testInterface</if:name>"+
                "		<if:type>if:fastdsl</if:type>"+	
                "		<smt:traps xmlns:smt=\"schema-mount-test\">"+
                "			<smt:lol-alm-trap>disabled</smt:lol-alm-trap>"+
                "		</smt:traps>"+
                "     </if:interface>"+
                "      <if:interface>"+
                "       <if:name>testInterface1</if:name>"+
                "		<if:type>if:fastdsl</if:type>"+	
                "     </if:interface>"+
                "    </if:interfaces>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";
        editConfig(m_server, m_clientInfo, requestXml, true);

        String response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        +" <data>"
                        +"  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                        +"   <validation:xml-subtree>"
                        +"    <validation:plugType>PLUG-1.0</validation:plugType>" 
                        +"    <schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">"
                        +"      <if:interfaces xmlns:if=\"test-interfaces\">"
                        +"        <if:interface>"
                        +"		    <if:type>if:fastdsl</if:type>"
                        +"          <if:name>testInterface</if:name>"
                        +"          <if:mybits>fourthBit</if:mybits>\n"
                        +"           <if:mybitsdefaultValue>thirdBit</if:mybitsdefaultValue>\n"
                        +"           <if:mybitsoverridden>fourthBit</if:mybitsoverridden>\n"
                        +"			<smt:traps xmlns:smt=\"schema-mount-test\">"
                        +"				<smt:lol-alm-trap>disabled</smt:lol-alm-trap>"
                        +"				<smt:los-alm-trap>enabled</smt:los-alm-trap>"
                        +"				<smt:lom-alm-trap>enabled</smt:lom-alm-trap>"
                        +"			</smt:traps>"
                        +"        </if:interface>"
                        +"        <if:interface>"
                        +"		    <if:type>if:fastdsl</if:type>"
                        +"          <if:name>testInterface1</if:name>"
                        +"          <if:mybits>fourthBit</if:mybits>\n"
                        +"           <if:mybitsdefaultValue>thirdBit</if:mybitsdefaultValue>\n"
                        +"           <if:mybitsoverridden>fourthBit</if:mybitsoverridden>\n"
                        +"			<smt:traps xmlns:smt=\"schema-mount-test\">"
                        +"				<smt:lol-alm-trap>enabled</smt:lol-alm-trap>"
                        +"				<smt:los-alm-trap>enabled</smt:los-alm-trap>"
                        +"				<smt:lom-alm-trap>enabled</smt:lom-alm-trap>"
                        +"			</smt:traps>"
                        +"        </if:interface>"
                        +"      </if:interfaces>"
                        +"   </schemaMountPoint>"
                        +"  </validation:xml-subtree>"
                        +" </validation:validation>"
                        +"</data>"
                        +"</rpc-reply>";

        verifyGet(m_server, m_clientInfo, response);

        //change interface type to if:ptm ==> default leafs should be deleted. 
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                "     <if:interfaces xmlns:if=\"test-interfaces\">"+
                "      <if:interface>"+
                "       <if:name>testInterface5</if:name>"+
                "		<if:type>if:ptm</if:type>"+	
                "     </if:interface>"+
                "      <if:interface>"+
                "       <if:name>testInterface1</if:name>"+
                "		<if:type>if:ptm</if:type>"+	
                "     </if:interface>"+
                "    </if:interfaces>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";
        editConfig(m_server, m_clientInfo, requestXml, true);

        response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        +" <data>"
                        +"  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                        +"   <validation:xml-subtree>"
                        +"    <validation:plugType>PLUG-1.0</validation:plugType>" 
                        +"    <schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">"
                        +"      <if:interfaces xmlns:if=\"test-interfaces\">"
                        +"        <if:interface>"
                        +"		    <if:type>if:fastdsl</if:type>"
                        +"          <if:name>testInterface</if:name>"
                        +"          <if:mybits>fourthBit</if:mybits>\n"
                        +"           <if:mybitsdefaultValue>thirdBit</if:mybitsdefaultValue>\n"
                        +"           <if:mybitsoverridden>fourthBit</if:mybitsoverridden>\n"
                        +"			<smt:traps xmlns:smt=\"schema-mount-test\">"
                        +"				<smt:lol-alm-trap>disabled</smt:lol-alm-trap>"
                        +"				<smt:los-alm-trap>enabled</smt:los-alm-trap>"
                        +"				<smt:lom-alm-trap>enabled</smt:lom-alm-trap>"
                        +"			</smt:traps>"
                        +"        </if:interface>"
                        +"        <if:interface>"
                        +"		    <if:type>if:ptm</if:type>"
                        +"          <if:name>testInterface1</if:name>"
                        +"          <if:mybits>fourthBit</if:mybits>\n"
                        +"           <if:mybitsdefaultValue>thirdBit</if:mybitsdefaultValue>\n"
                        +"           <if:mybitsoverridden>fourthBit</if:mybitsoverridden>\n"
                        +"			<smt:traps xmlns:smt=\"schema-mount-test\"/>"
                        +"        </if:interface>"
                        +"        <if:interface>"
                        +"		    <if:type>if:ptm</if:type>"
                        +"          <if:name>testInterface5</if:name>"
                        +"          <if:mybits>fourthBit</if:mybits>\n"
                        +"           <if:mybitsdefaultValue>thirdBit</if:mybitsdefaultValue>\n"
                        +"           <if:mybitsoverridden>fourthBit</if:mybitsoverridden>\n"
                        +"        </if:interface>"
                        +"      </if:interfaces>"
                        +"   </schemaMountPoint>"
                        +"  </validation:xml-subtree>"
                        +" </validation:validation>"
                        +"</data>"
                        +"</rpc-reply>";

        verifyGet(m_server, m_clientInfo, response);

        //1) create new interfaces with type if:fastdsl ==> interface should be created with default leafs
        //2) change interface type to if:ptm ==> Default leafs should be removed
        //3) remove an existing and non-existing interface  ==> Interfaces should be removed
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                "     <if:interfaces xmlns:if=\"test-interfaces\">"+
                "      <if:interface>"+
                "       <if:name>testInterface</if:name>"+
                "		<if:type>if:ptm</if:type>"+	
                "     </if:interface>"+
                "      <if:interface>"+
                "       <if:name>testInterface2</if:name>"+
                "		<if:type>if:fastdsl</if:type>"+	
                "     </if:interface>"+
                "      <if:interface xc:operation=\"remove\">"+
                "       <if:name>testInterface5</if:name>"+
                "		<if:type>if:fastdsl</if:type>"+	
                "     </if:interface>"+
                "      <if:interface xc:operation=\"remove\">"+
                "       <if:name>testInterface6</if:name>"+
                "		<if:type>if:fastdsl</if:type>"+	
                "     </if:interface>"+
                "    </if:interfaces>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";
        editConfig(m_server, m_clientInfo, requestXml, true);

        response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        +" <data>"
                        +"  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                        +"   <validation:xml-subtree>"
                        +"    <validation:plugType>PLUG-1.0</validation:plugType>" 
                        +"    <schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">"
                        +"      <if:interfaces xmlns:if=\"test-interfaces\">"
                        +"        <if:interface>"
                        +"		    <if:type>if:ptm</if:type>"
                        +"          <if:name>testInterface</if:name>"
                        +"          <if:mybits>fourthBit</if:mybits>\n"
                        +"           <if:mybitsdefaultValue>thirdBit</if:mybitsdefaultValue>\n"
                        +"           <if:mybitsoverridden>fourthBit</if:mybitsoverridden>\n"
                        +"			<smt:traps xmlns:smt=\"schema-mount-test\"/>"
                        +"        </if:interface>"
                        +"        <if:interface>"
                        +"		    <if:type>if:ptm</if:type>"
                        +"          <if:name>testInterface1</if:name>"
                        +"          <if:mybits>fourthBit</if:mybits>\n"
                        +"           <if:mybitsdefaultValue>thirdBit</if:mybitsdefaultValue>\n"
                        +"           <if:mybitsoverridden>fourthBit</if:mybitsoverridden>\n"
                        +"			<smt:traps xmlns:smt=\"schema-mount-test\"/>"
                        +"        </if:interface>"
                        +"        <if:interface>" 
                        +"          <if:type>if:fastdsl</if:type>"
                        +"          <if:name>testInterface2</if:name>" 
                        +"          <if:mybits>fourthBit</if:mybits>\n"
                        +"           <if:mybitsdefaultValue>thirdBit</if:mybitsdefaultValue>\n"
                        +"           <if:mybitsoverridden>fourthBit</if:mybitsoverridden>\n"
                        +"			<smt:traps xmlns:smt=\"schema-mount-test\">"
                        +"				<smt:lol-alm-trap>enabled</smt:lol-alm-trap>"
                        +"				<smt:los-alm-trap>enabled</smt:los-alm-trap>"
                        +"				<smt:lom-alm-trap>enabled</smt:lom-alm-trap>"
                        +"			</smt:traps>"
                        +"        </if:interface>"
                        +"      </if:interfaces>"
                        +"   </schemaMountPoint>"
                        +"  </validation:xml-subtree>"
                        +" </validation:validation>"
                        +"</data>"
                        +"</rpc-reply>";

        verifyGet(m_server, m_clientInfo, response);

        // when-violation 
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                "     <if:interfaces xmlns:if=\"test-interfaces\">"+
                "      <if:interface>"+
                "       <if:name>testInterface7</if:name>"+
                "		<if:type>if:ptm</if:type>"+	
                "		<smt:traps xmlns:smt=\"schema-mount-test\">"+
                "			<smt:lol-alm-trap>disabled</smt:lol-alm-trap>"+
                "		</smt:traps>"+
                "     </if:interface>"+
                "    </if:interfaces>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";

        NetConfResponse netconfResponse = editConfigAsFalse(requestXml); // Should Fail
        assertEquals(1,netconfResponse.getErrors().size());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/if:interfaces/if:interface[if:name='testInterface7']/smt:traps/smt:lol-alm-trap", netconfResponse.getErrors().get(0).getErrorPath());
        assertEquals("Violate when constraints: ../type = 'test1:fastdsl'", netconfResponse.getErrors().get(0).getErrorMessage());
        assertEquals("when-violation", netconfResponse.getErrors().get(0).getErrorAppTag());
        assertEquals(NetconfRpcErrorTag.UNKNOWN_ELEMENT, netconfResponse.getErrors().get(0).getErrorTag());
    }

    @Test
    public void testMustConstraint_StringLengthWithDotArgs() throws Exception {

        // Create LIST entry with name dot --> It should be passed since it's length less than 10
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "      <string-validation>" +
                "        <name>dot</name>" +
                "      </string-validation>" +
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";
        editConfig(requestXml);

        // Verify GET response
        String response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "  <data>"
                        + "   <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                        + "    <validation:xml-subtree>"
                        + "     <validation:plugType>PLUG-1.0</validation:plugType>"
                        + "     <validation:schemaMountPoint>"
                        + "      <smt:schemaMount xmlns:smt=\"schema-mount-test\">"
                        + "      <smt:string-validation>"
                        + "       <smt:name>dot</smt:name>"
                        + "      </smt:string-validation>"
                        + "     </smt:schemaMount>"
                        + "     </validation:schemaMountPoint>"
                        + "    </validation:xml-subtree>"
                        + "   </validation:validation>"
                        + "  </data>"
                        + " </rpc-reply>";
        verifyGet(m_server, m_clientInfo, response);

        // Create another LIST entry with name StringLengthWithDot --> It should be failed since it's length not less than 10
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "      <string-validation>" +
                "        <name>StringLengthWithDot</name>" +
                "      </string-validation>" +
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";

        // Verify RPC response
        NetConfResponse netconfResponse = editConfigAsFalse(requestXml);
        assertEquals(1, netconfResponse.getErrors().size());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/" +
                "validation:schemaMountPoint/smt:schemaMount/smt:string-validation[smt:name='StringLengthWithDot']/smt:name",
                netconfResponse.getErrors().get(0).getErrorPath());
        assertEquals("Violate must constraints: string-length(.) < 10", netconfResponse.getErrors().get(0).getErrorMessage());
        assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, netconfResponse.getErrors().get(0).getErrorTag());
        assertEquals("must-violation", netconfResponse.getErrors().get(0).getErrorAppTag());
    }

    @Test
    public void testMustConstraint_StringLengthArgsWithLocationPath() throws Exception {

        // Create LIST entry with name dot --> It should be passed since it's length less than 5
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "      <string-validation1>" +
                "        <name>dot</name>" +
                "        <type>gfast</type>" +
                "      </string-validation1>" +
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";
        editConfig(requestXml);

        // Verify GET response
        String response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "  <data>"
                        + "   <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                        + "    <validation:xml-subtree>"
                        + "     <validation:plugType>PLUG-1.0</validation:plugType>"
                        + "     <validation:schemaMountPoint>"
                        + "      <smt:schemaMount xmlns:smt=\"schema-mount-test\">"
                        + "      <smt:string-validation1>"
                        + "       <smt:name>dot</smt:name>"
                        + "       <smt:type>gfast</smt:type>"
                        + "      </smt:string-validation1>"
                        + "     </smt:schemaMount>"
                        + "     </validation:schemaMountPoint>"
                        + "    </validation:xml-subtree>"
                        + "   </validation:validation>"
                        + "  </data>"
                        + " </rpc-reply>";
        verifyGet(m_server, m_clientInfo, response);

        // Create another LIST entry with name StringLength --> It should be failed since it's length not less than 5
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "      <string-validation1>" +
                "        <name>StringLength</name>" +
                "        <type>type2</type>" +
                "      </string-validation1>" +
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";

        // Verify RPC response
        NetConfResponse netconfResponse = editConfigAsFalse(requestXml);
        assertEquals(1, netconfResponse.getErrors().size());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/" +
                "validation:schemaMountPoint/smt:schemaMount/smt:string-validation1[smt:name='StringLength']/smt:type",
                netconfResponse.getErrors().get(0).getErrorPath());
        assertEquals("Violate must constraints: string-length(current()/../name) < 5",
                netconfResponse.getErrors().get(0).getErrorMessage());
        assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, netconfResponse.getErrors().get(0).getErrorTag());
        assertEquals("must-violation", netconfResponse.getErrors().get(0).getErrorAppTag());
    }


    @Test
    public void testMustConstraint_StringLengthArgsWithLocationPath1() throws Exception {

        // Create LIST entry with name dot --> It should be passed since it's length less than 5
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "      <string-validation2>" +
                "        <name>dot</name>" +
                "        <type>gfast</type>" +
                "      </string-validation2>" +
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";
        editConfig(requestXml);

        // Verify GET response
        String response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "  <data>"
                        + "   <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                        + "    <validation:xml-subtree>"
                        + "     <validation:plugType>PLUG-1.0</validation:plugType>"
                        + "     <validation:schemaMountPoint>"
                        + "      <smt:schemaMount xmlns:smt=\"schema-mount-test\">"
                        + "      <smt:string-validation2>"
                        + "       <smt:name>dot</smt:name>"
                        + "       <smt:type>gfast</smt:type>"
                        + "      </smt:string-validation2>"
                        + "     </smt:schemaMount>"
                        + "     </validation:schemaMountPoint>"
                        + "    </validation:xml-subtree>"
                        + "   </validation:validation>"
                        + "  </data>"
                        + " </rpc-reply>";
        verifyGet(m_server, m_clientInfo, response);

        // Create another LIST entry with name StringLength --> It should be failed since it's length not less than 5
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "      <string-validation2>" +
                "        <name>StringLength</name>" +
                "        <type>type2</type>" +
                "      </string-validation2>" +
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";

        // Verify RPC response
        NetConfResponse netconfResponse = editConfigAsFalse(requestXml);
        assertEquals(1, netconfResponse.getErrors().size());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/" +
                "validation:schemaMountPoint/smt:schemaMount/smt:string-validation2[smt:name='StringLength']/smt:type",
                netconfResponse.getErrors().get(0).getErrorPath());
        assertEquals("Violate must constraints: string-length(../name) < 5",
                netconfResponse.getErrors().get(0).getErrorMessage());
        assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, netconfResponse.getErrors().get(0).getErrorTag());
        assertEquals("must-violation", netconfResponse.getErrors().get(0).getErrorAppTag());
    }

    @Test
    public void testMustConstraintFailureForListNode() throws Exception {

        // Create Two LIST entries with type node 'must-type' --> Must constraint should be passed
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "               <must-validation>" +
                "                 <name>must1</name>" +
                "                 <type>must-type</type>" +
                "               </must-validation>" +
                "               <must-validation>" +
                "                 <name>must2</name>" +
                "                 <type>must-type</type>" +
                "               </must-validation>" +
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";
        editConfig(requestXml);

        // Verify GET response
        String response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "  <data>"
                        + "   <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                        + "    <validation:xml-subtree>"
                        + "     <validation:plugType>PLUG-1.0</validation:plugType>"
                        + "     <validation:schemaMountPoint>"
                        + "      <smt:schemaMount xmlns:smt=\"schema-mount-test\">"
                        + "      <smt:must-validation>"
                        + "       <smt:name>must1</smt:name>"
                        + "       <smt:type>must-type</smt:type>"
                        + "      </smt:must-validation>"
                        + "      <smt:must-validation>"
                        + "       <smt:name>must2</smt:name>"
                        + "       <smt:type>must-type</smt:type>"
                        + "      </smt:must-validation>"
                        + "     </smt:schemaMount>"
                        + "     </validation:schemaMountPoint>"
                        + "    </validation:xml-subtree>"
                        + "   </validation:validation>"
                        + "  </data>"
                        + " </rpc-reply>";
        verifyGet(m_server, m_clientInfo, response);

        // Try to create another LIST entry with invalid type node --> Must constraint should be Failed
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "               <must-validation>" +
                "                 <name>must3</name>" +
                "                 <type>type</type>" +
                "               </must-validation>" +
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";

        // Verify that Error path should be points to exact LIST entry 'must3' --> smt:must-validation[smt:name='must3']
        NetConfResponse netconfResponse = editConfigAsFalse(requestXml);
        assertEquals(1, netconfResponse.getErrors().size());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/smt:schemaMount/smt:must-validation[smt:name='must3']", netconfResponse.getErrors().get(0).getErrorPath());
        assertEquals("Violate must constraints: type = 'must-type'", netconfResponse.getErrors().get(0).getErrorMessage());
        assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, netconfResponse.getErrors().get(0).getErrorTag());
        assertEquals("must-violation", netconfResponse.getErrors().get(0).getErrorAppTag());
    }

    @Test
    public void testMustConstraintFailureForListNode_impactValidation() throws Exception {

        // Create Two LIST entries with type node 'must-type' --> Must constraint should be passed
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "	  <must-validation>" +
                " 	   <name>must1</name>" +
                "      <type>must-type</type>" +
                "     </must-validation>" +
                "     <must-validation>" +
                "      <name>must2</name>" +
                "      <type>must-type</type>" +
                "     </must-validation>" +
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";
        editConfig(requestXml);

        // Verify GET response
        String response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "  <data>"
                        + "   <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                        + "    <validation:xml-subtree>"
                        + "     <validation:plugType>PLUG-1.0</validation:plugType>"
                        + "     <validation:schemaMountPoint>"
                        + "      <smt:schemaMount xmlns:smt=\"schema-mount-test\">"
                        + "      <smt:must-validation>"
                        + "       <smt:name>must1</smt:name>"
                        + "       <smt:type>must-type</smt:type>"
                        + "      </smt:must-validation>"
                        + "      <smt:must-validation>"
                        + "       <smt:name>must2</smt:name>"
                        + "       <smt:type>must-type</smt:type>"
                        + "      </smt:must-validation>"
                        + "     </smt:schemaMount>"
                        + "     </validation:schemaMountPoint>"
                        + "    </validation:xml-subtree>"
                        + "   </validation:validation>"
                        + "  </data>"
                        + " </rpc-reply>";
        verifyGet(m_server, m_clientInfo, response);

        // Modify the type value for first entry of LIST --> Must constraint should be Failed
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "     <must-validation>" +
                "      <name>must1</name>" +
                "      <type>type1</type>" +
                "     </must-validation>" +
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";

        // Modify the type value as 'type1' for first LIST entry. It should thrown an error.
        NetConfResponse netconfResponse = editConfigAsFalse(requestXml);
        assertEquals(1, netconfResponse.getErrors().size());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1" +
                ".0']/validation:schemaMountPoint/smt:schemaMount/smt:must-validation[smt:name='must1']", netconfResponse.getErrors().get(0).getErrorPath());
        assertEquals("Violate must constraints: type = 'must-type'", netconfResponse.getErrors().get(0).getErrorMessage());
        assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, netconfResponse.getErrors().get(0).getErrorTag());
        assertEquals("must-violation", netconfResponse.getErrors().get(0).getErrorAppTag());
    }

    @Test
    public void testMustConstraintFailureWithCoreFunctionForListNode_impactValidation() throws Exception {

        // Create Three LIST entries with out 'type' leaf --> Must constraint should be passed
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "	  <must-validation2>" +
                "		<name>must1</name>" +
                "     </must-validation2>" +
                "     <must-validation2>" +
                "      <name>must2</name>" +
                "     </must-validation2>" +
                "     <must-validation2>" +
                "      <name>must3</name>" +
                "     </must-validation2>" +
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";
        editConfig(requestXml);

        // Verify GET response
        String response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "  <data>"
                        + "   <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                        + "    <validation:xml-subtree>"
                        + "     <validation:plugType>PLUG-1.0</validation:plugType>"
                        + "     <validation:schemaMountPoint>"
                        + "      <smt:schemaMount xmlns:smt=\"schema-mount-test\">"
                        + "      <smt:must-validation2>"
                        + "       <smt:name>must1</smt:name>"
                        + "      </smt:must-validation2>"
                        + "      <smt:must-validation2>"
                        + "       <smt:name>must2</smt:name>"
                        + "      </smt:must-validation2>"
                        + "      <smt:must-validation2>"
                        + "       <smt:name>must3</smt:name>"
                        + "      </smt:must-validation2>"
                        + "     </smt:schemaMount>"
                        + "     </validation:schemaMountPoint>"
                        + "    </validation:xml-subtree>"
                        + "   </validation:validation>"
                        + "  </data>"
                        + " </rpc-reply>";
        verifyGet(m_server, m_clientInfo, response);

        // add 'type' leaf for middle entry of LIST --> Must constraint should be Failed for LIST node
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "     <must-validation2>" +
                "      <name>must2</name>" +
                "      <type>type2</type>" +
                "     </must-validation2>" +
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";

        NetConfResponse netconfResponse = editConfigAsFalse(requestXml);
        assertEquals(1, netconfResponse.getErrors().size());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1" +
                ".0']/validation:schemaMountPoint/smt:schemaMount/smt:must-validation2[smt:name='must2']",
                netconfResponse.getErrors().get(0).getErrorPath());
        assertEquals("Violate must constraints: not(boolean(current()/type))", netconfResponse.getErrors().get(0).getErrorMessage());
        assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, netconfResponse.getErrors().get(0).getErrorTag());
        assertEquals("must-violation", netconfResponse.getErrors().get(0).getErrorAppTag());
    }

    @Test
    public void testMustConstraintFailureForListNode_impactValidation1() throws Exception {

        // Create Three LIST entries with type node 'must-type' --> Must constraint should be passed
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "      <must-validation>" +
                "       <name>must1</name>" +
                "       <type>must-type</type>" +
                "      </must-validation>" +
                "      <must-validation>" +
                "       <name>must2</name>" +
                "       <type>must-type</type>" +
                "      </must-validation>" +
                "      <must-validation>" +
                "       <name>must3</name>" +
                "       <type>must-type</type>" +
                "      </must-validation>" +
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";
        editConfig(requestXml);

        // Verify GET response
        String response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "  <data>"
                        + "   <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                        + "    <validation:xml-subtree>"
                        + "     <validation:plugType>PLUG-1.0</validation:plugType>"
                        + "     <validation:schemaMountPoint>"
                        + "      <smt:schemaMount xmlns:smt=\"schema-mount-test\">"
                        + "      <smt:must-validation>"
                        + "       <smt:name>must1</smt:name>"
                        + "       <smt:type>must-type</smt:type>"
                        + "      </smt:must-validation>"
                        + "      <smt:must-validation>"
                        + "       <smt:name>must2</smt:name>"
                        + "       <smt:type>must-type</smt:type>"
                        + "      </smt:must-validation>"
                        + "      <smt:must-validation>"
                        + "       <smt:name>must3</smt:name>"
                        + "       <smt:type>must-type</smt:type>"
                        + "      </smt:must-validation>"
                        + "     </smt:schemaMount>"
                        + "     </validation:schemaMountPoint>"
                        + "    </validation:xml-subtree>"
                        + "   </validation:validation>"
                        + "  </data>"
                        + " </rpc-reply>";
        verifyGet(m_server, m_clientInfo, response);

        // Modify type value for middle entry of LIST --> Must constraint should be Failed
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "     <must-validation>" +
                "      <name>must2</name>" +
                "      <type>type2</type>" +
                "     </must-validation>" +
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";

        // Modify the type value as 'type2' for middle of LIST entry. It should thrown an error.
        NetConfResponse netconfResponse = editConfigAsFalse(requestXml);
        assertEquals(1, netconfResponse.getErrors().size());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1" +
                ".0']/validation:schemaMountPoint/smt:schemaMount/smt:must-validation[smt:name='must2']",
                netconfResponse.getErrors().get(0).getErrorPath());
        assertEquals("Violate must constraints: type = 'must-type'", netconfResponse.getErrors().get(0).getErrorMessage());
        assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, netconfResponse.getErrors().get(0).getErrorTag());
        assertEquals("must-violation", netconfResponse.getErrors().get(0).getErrorAppTag());
    }

    @Test
    public void testMustConstraintFailureForListNode_impactValidation3() throws Exception {

        // Create Three LIST entries with type node other than 'must-type' --> Must constraint should be passed
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "      <must-validation3>" +
                "       <name>must1</name>" +
                "       <type>must-type1</type>" +
                "      </must-validation3>" +
                "      <must-validation3>" +
                "       <name>must2</name>" +
                "       <type>must-type2</type>" +
                "      </must-validation3>" +
                "      <must-validation3>" +
                "       <name>must3</name>" +
                "       <type>must-type3</type>" +
                "      </must-validation3>" +
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";
        editConfig(requestXml);

        // Verify GET response
        String response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "  <data>"
                        + "   <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                        + "    <validation:xml-subtree>"
                        + "     <validation:plugType>PLUG-1.0</validation:plugType>"
                        + "     <validation:schemaMountPoint>"
                        + "      <smt:schemaMount xmlns:smt=\"schema-mount-test\">"
                        + "      <smt:must-validation3>"
                        + "       <smt:name>must1</smt:name>"
                        + "       <smt:type>must-type1</smt:type>"
                        + "      </smt:must-validation3>"
                        + "      <smt:must-validation3>"
                        + "       <smt:name>must2</smt:name>"
                        + "       <smt:type>must-type2</smt:type>"
                        + "      </smt:must-validation3>"
                        + "      <smt:must-validation3>"
                        + "       <smt:name>must3</smt:name>"
                        + "       <smt:type>must-type3</smt:type>"
                        + "      </smt:must-validation3>"
                        + "     </smt:schemaMount>"
                        + "     </validation:schemaMountPoint>"
                        + "    </validation:xml-subtree>"
                        + "   </validation:validation>"
                        + "  </data>"
                        + " </rpc-reply>";
        verifyGet(m_server, m_clientInfo, response);

        // Modify type value for middle entry of LIST --> Must constraint should be Failed
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "     <must-validation3>" +
                "      <name>must2</name>" +
                "      <type>must-type</type>" +
                "     </must-validation3>" +
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";

        // Modify the type value as 'must-type' for middle of LIST entry. It should thrown an error.
        NetConfResponse netconfResponse = editConfigAsFalse(requestXml);
        assertEquals(1, netconfResponse.getErrors().size());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1" +
                ".0']/validation:schemaMountPoint/smt:schemaMount/smt:must-validation3[smt:name='must2']",
                netconfResponse.getErrors().get(0).getErrorPath());
        assertEquals("Violate must constraints: type != 'must-type'", netconfResponse.getErrors().get(0).getErrorMessage());
        assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, netconfResponse.getErrors().get(0).getErrorTag());
        assertEquals("must-violation", netconfResponse.getErrors().get(0).getErrorAppTag());
    }

    @Test
    public void testMustConstraintFailureForListNode1() throws Exception {

        // Create Two LIST entries with type 'must-type'
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "               <must-validation>" +
                "                 <name>must1</name>" +
                "                 <type>must-type</type>" +
                "               </must-validation>" +
                "               <must-validation>" +
                "                 <name>must2</name>" +
                "                 <type>must-type</type>" +
                "               </must-validation>" +
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";
        editConfig(requestXml);

        // Verify GET response
        String response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "  <data>"
                        + "   <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                        + "    <validation:xml-subtree>"
                        + "     <validation:plugType>PLUG-1.0</validation:plugType>"
                        + "     <validation:schemaMountPoint>"
                        + "      <smt:schemaMount xmlns:smt=\"schema-mount-test\">"
                        + "      <smt:must-validation>"
                        + "       <smt:name>must1</smt:name>"
                        + "       <smt:type>must-type</smt:type>"
                        + "      </smt:must-validation>"
                        + "      <smt:must-validation>"
                        + "       <smt:name>must2</smt:name>"
                        + "       <smt:type>must-type</smt:type>"
                        + "      </smt:must-validation>"
                        + "     </smt:schemaMount>"
                        + "     </validation:schemaMountPoint>"
                        + "    </validation:xml-subtree>"
                        + "   </validation:validation>"
                        + "  </data>"
                        + " </rpc-reply>";
        verifyGet(m_server, m_clientInfo, response);

        // Try to create one more LIST with-out type node --> Must constraint should be failed
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "               <must-validation>" +
                "                 <name>must3</name>" +
                "               </must-validation>" +
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";

        // Verify Error path points to exact LIST entry 'must3'--> smt:must-validation[smt:name='must3']
        NetConfResponse netconfResponse = editConfigAsFalse(requestXml);
        assertEquals(1, netconfResponse.getErrors().size());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/smt:schemaMount/smt:must-validation[smt:name='must3']", netconfResponse.getErrors().get(0).getErrorPath());
        assertEquals("Violate must constraints: type = 'must-type'", netconfResponse.getErrors().get(0).getErrorMessage());
        assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, netconfResponse.getErrors().get(0).getErrorTag());
        assertEquals("must-violation", netconfResponse.getErrors().get(0).getErrorAppTag());
    }

    @Test
    public void testMustConstraintFailureForListNode_MultipleKey() throws Exception {

        // Create Two LIST entries with type node 'must-type' --> Must constraint should be passed
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "               <must-validation1>" +
                "                 <name>must1</name>" +
                "                 <name1>must11</name1>" +
                "                 <type>must-type</type>" +
                "               </must-validation1>" +
                "               <must-validation1>" +
                "                 <name>must2</name>" +
                "                 <name1>must22</name1>" +
                "                 <type>must-type</type>" +
                "               </must-validation1>" +
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";
        editConfig(requestXml);

        // Verify GET response
        String response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "  <data>"
                        + "   <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                        + "    <validation:xml-subtree>"
                        + "     <validation:plugType>PLUG-1.0</validation:plugType>"
                        + "     <validation:schemaMountPoint>"
                        + "      <smt:schemaMount xmlns:smt=\"schema-mount-test\">"
                        + "      <smt:must-validation1>"
                        + "       <smt:name>must1</smt:name>"
                        + "       <smt:name1>must11</smt:name1>"
                        + "       <smt:type>must-type</smt:type>"
                        + "      </smt:must-validation1>"
                        + "      <smt:must-validation1>"
                        + "       <smt:name>must2</smt:name>"
                        + "       <smt:name1>must22</smt:name1>"
                        + "       <smt:type>must-type</smt:type>"
                        + "      </smt:must-validation1>"
                        + "     </smt:schemaMount>"
                        + "     </validation:schemaMountPoint>"
                        + "    </validation:xml-subtree>"
                        + "   </validation:validation>"
                        + "  </data>"
                        + " </rpc-reply>";
        verifyGet(m_server, m_clientInfo, response);

        // Try to create another LIST entry with invalid type node --> Must constraint should be Failed
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "               <must-validation1>" +
                "                 <name>must3</name>" +
                "                 <name1>must33</name1>" +
                "                 <type>type</type>" +
                "               </must-validation1>" +
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";

        // Verify that Error path should be points to exact LIST entry 'must3' --> smt:must-validation[smt:name='must3']
        NetConfResponse netconfResponse = editConfigAsFalse(requestXml);
        assertEquals(1, netconfResponse.getErrors().size());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/smt:schemaMount/smt:must-validation1[smt:name='must3'][smt:name1='must33']", netconfResponse.getErrors().get(0).getErrorPath());
        assertEquals("Violate must constraints: type = 'must-type'", netconfResponse.getErrors().get(0).getErrorMessage());
        assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, netconfResponse.getErrors().get(0).getErrorTag());
        assertEquals("must-violation", netconfResponse.getErrors().get(0).getErrorAppTag());
    }

    @Test
    public void testWhenConstraintFailureForListNode() throws Exception {

        // Create Two LIST entries with type node 'when-type' --> When constraint should be passed
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "               <when-validation>" +
                "                 <name>when1</name>" +
                "                 <type>when-type</type>" +
                "               </when-validation>" +
                "               <when-validation>" +
                "                 <name>when2</name>" +
                "                 <type>when-type</type>" +
                "               </when-validation>" +
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";
        editConfig(requestXml);

        // Verify GET response
        String response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "  <data>"
                        + "   <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                        + "    <validation:xml-subtree>"
                        + "     <validation:plugType>PLUG-1.0</validation:plugType>"
                        + "     <validation:schemaMountPoint>"
                        + "      <smt:schemaMount xmlns:smt=\"schema-mount-test\">"
                        + "      <smt:when-validation>"
                        + "       <smt:name>when1</smt:name>"
                        + "       <smt:type>when-type</smt:type>"
                        + "      </smt:when-validation>"
                        + "      <smt:when-validation>"
                        + "       <smt:name>when2</smt:name>"
                        + "       <smt:type>when-type</smt:type>"
                        + "      </smt:when-validation>"
                        + "     </smt:schemaMount>"
                        + "     </validation:schemaMountPoint>"
                        + "    </validation:xml-subtree>"
                        + "   </validation:validation>"
                        + "  </data>"
                        + " </rpc-reply>";
        verifyGet(m_server, m_clientInfo, response);

        // Try to create another LIST entry with invalid type node --> When constraint should be Failed
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "               <when-validation>" +
                "                 <name>when3</name>" +
                "                 <type>type</type>" +
                "               </when-validation>" +
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";

        // Verify that Error path should be points to exact LIST entry 'when3' --> smt:when-validation[smt:name='when3']
        NetConfResponse netconfResponse = editConfigAsFalse(requestXml);
        assertEquals(1, netconfResponse.getErrors().size());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/smt:schemaMount/smt:when-validation[smt:name='when3']", netconfResponse.getErrors().get(0).getErrorPath());
        assertEquals("Violate when constraints: type = 'when-type'", netconfResponse.getErrors().get(0).getErrorMessage());
        assertEquals(NetconfRpcErrorTag.UNKNOWN_ELEMENT, netconfResponse.getErrors().get(0).getErrorTag());
        assertEquals("when-violation", netconfResponse.getErrors().get(0).getErrorAppTag());
    }

    @Test
    public void testMustConstraintWithBooleanWithOnlyCurrentOnList() throws Exception {
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "     <boolean-validation>" +
                "      <testDiscardList>" +
                "       <name>test</name>" +
                "      </testDiscardList>" +	                
                "     </boolean-validation>" +
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";
        NetConfResponse response = editConfigAsFalse(requestXml);
        NetconfRpcError error = response.getErrors().get(0);
        assertEquals("Violate must constraints: not(boolean(current()))", error.getErrorMessage());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/smt:schemaMount/smt:boolean-validation/smt:testDiscardList[smt:name='test']", error.getErrorPath());
        assertEquals("must-violation", error.getErrorAppTag());
    }

    @Test
    public void testMustConstraintWithBooleanWithOnlyCurrentOnLeafList() throws Exception {
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "     <boolean-validation>" +
                "      <testDiscardLeafList>test1</testDiscardLeafList>" +
                "      <testDiscardLeafList>test2</testDiscardLeafList>" +
                "     </boolean-validation>" +
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";
        NetConfResponse response = editConfigAsFalse(requestXml);
        NetconfRpcError error = response.getErrors().get(0);
        assertEquals("Violate must constraints: not(boolean(current()))", error.getErrorMessage());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/smt:schemaMount/smt:boolean-validation/smt:testDiscardLeafList", error.getErrorPath());
        assertEquals("must-violation", error.getErrorAppTag());
    }

    @Test
    public void testMustConstraintWithEmptyTypeForBoolean() throws Exception {
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "     <boolean-validation>" +
                "     </boolean-validation>" +
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";
        editConfig(requestXml);
        String output = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
                "   <data>\n" +
                "      <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">\n" +
                "         <validation:xml-subtree>\n" +
                "            <validation:plugType>PLUG-1.0</validation:plugType>\n" +
                "            <schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">\n" +
                "               <smt:schemaMount xmlns:smt=\"schema-mount-test\">\n" +
                "                  <smt:boolean-validation/>\n" +
                "               </smt:schemaMount>\n" +
                "            </schemaMountPoint>\n" +
                "         </validation:xml-subtree>\n" +
                "      </validation:validation>\n" +
                "   </data>\n" +
                "</rpc-reply>";
        verifyGet(output);

        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "     <boolean-validation>" +
                "      <discard/>" +
                "     </boolean-validation>" +
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";
        NetConfResponse response = editConfigAsFalse(requestXml);
        NetconfRpcError error = response.getErrors().get(0);
        assertEquals("Violate must constraints: not(boolean(current()))", error.getErrorMessage());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/smt:schemaMount/smt:boolean-validation/smt:discard", error.getErrorPath());
        assertEquals("must-violation", error.getErrorAppTag());
    }

    @Test
    public void testWhenConstraintFailureForListNode_impactValidation() throws Exception {

        // Create Two LIST entries with type node 'when-type' --> When constraint should be passed
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "     <when-validation>" +
                "      <name>when1</name>" +
                "      <type>when-type</type>" +
                "     </when-validation>" +
                "     <when-validation>" +
                "      <name>when2</name>" +
                "      <type>when-type</type>" +
                "     </when-validation>" +
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";
        editConfig(requestXml);

        // Verify GET response
        String response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "  <data>"
                        + "   <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                        + "    <validation:xml-subtree>"
                        + "     <validation:plugType>PLUG-1.0</validation:plugType>"
                        + "     <validation:schemaMountPoint>"
                        + "      <smt:schemaMount xmlns:smt=\"schema-mount-test\">"
                        + "      <smt:when-validation>"
                        + "       <smt:name>when1</smt:name>"
                        + "       <smt:type>when-type</smt:type>"
                        + "      </smt:when-validation>"
                        + "      <smt:when-validation>"
                        + "       <smt:name>when2</smt:name>"
                        + "       <smt:type>when-type</smt:type>"
                        + "      </smt:when-validation>"
                        + "     </smt:schemaMount>"
                        + "     </validation:schemaMountPoint>"
                        + "    </validation:xml-subtree>"
                        + "   </validation:validation>"
                        + "  </data>"
                        + " </rpc-reply>";
        verifyGet(m_server, m_clientInfo, response);

        // modify type value of fist entry of LIST --> When constraint should be Failed
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "     <when-validation>" +
                "       <name>when1</name>" +
                "       <type>type1</type>" +
                "     </when-validation>" +
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";

        NetConfResponse netconfResponse = editConfigAsFalse(requestXml);
        assertEquals(1, netconfResponse.getErrors().size());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/smt:schemaMount/smt:when-validation[smt:name='when1']", netconfResponse.getErrors().get(0).getErrorPath());
        assertEquals("Violate when constraints: type = 'when-type'", netconfResponse.getErrors().get(0).getErrorMessage());
        assertEquals(NetconfRpcErrorTag.UNKNOWN_ELEMENT, netconfResponse.getErrors().get(0).getErrorTag());
        assertEquals("when-violation", netconfResponse.getErrors().get(0).getErrorAppTag());
    }

    @Test
    public void testWhenConstraintFailureForListNode_impactValidation1() throws Exception {

        // Create Three LIST entries with type node 'when-type' --> When constraint should be passed
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "     <when-validation>" +
                "      <name>when1</name>" +
                "      <type>when-type</type>" +
                "     </when-validation>" +
                "     <when-validation>" +
                "      <name>when2</name>" +
                "      <type>when-type</type>" +
                "     </when-validation>" +
                "     <when-validation>" +
                "      <name>when3</name>" +
                "      <type>when-type</type>" +
                "     </when-validation>" +
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";
        editConfig(requestXml);

        // Verify GET response
        String response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "  <data>"
                        + "   <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                        + "    <validation:xml-subtree>"
                        + "     <validation:plugType>PLUG-1.0</validation:plugType>"
                        + "     <validation:schemaMountPoint>"
                        + "      <smt:schemaMount xmlns:smt=\"schema-mount-test\">"
                        + "      <smt:when-validation>"
                        + "       <smt:name>when1</smt:name>"
                        + "       <smt:type>when-type</smt:type>"
                        + "      </smt:when-validation>"
                        + "      <smt:when-validation>"
                        + "       <smt:name>when2</smt:name>"
                        + "       <smt:type>when-type</smt:type>"
                        + "      </smt:when-validation>"
                        + "      <smt:when-validation>"
                        + "       <smt:name>when3</smt:name>"
                        + "       <smt:type>when-type</smt:type>"
                        + "      </smt:when-validation>"
                        + "     </smt:schemaMount>"
                        + "     </validation:schemaMountPoint>"
                        + "    </validation:xml-subtree>"
                        + "   </validation:validation>"
                        + "  </data>"
                        + " </rpc-reply>";
        verifyGet(m_server, m_clientInfo, response);

        // Modify type value for middle entry of LIST --> When constraint should be failed
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "     <when-validation>" +
                "      <name>when2</name>" +
                "      <type>type2</type>" +
                "     </when-validation>" +
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";

        // Verify that if any one of the List entries failed, then thrown an when-violation error
        NetConfResponse netconfResponse = editConfigAsFalse(requestXml);
        assertEquals(1, netconfResponse.getErrors().size());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/smt:schemaMount/smt:when-validation[smt:name='when2']", netconfResponse.getErrors().get(0).getErrorPath());
        assertEquals("Violate when constraints: type = 'when-type'", netconfResponse.getErrors().get(0).getErrorMessage());
        assertEquals(NetconfRpcErrorTag.UNKNOWN_ELEMENT, netconfResponse.getErrors().get(0).getErrorTag());
        assertEquals("when-violation", netconfResponse.getErrors().get(0).getErrorAppTag());
    }

    @Test
    public void testWhenConstraintFailureForListNode1() throws Exception {

        // Create Two LIST entries with type 'when-type' --> Should be passed
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "               <when-validation>" +
                "                 <name>when1</name>" +
                "                 <type>when-type</type>" +
                "               </when-validation>" +
                "               <when-validation>" +
                "                 <name>when2</name>" +
                "                 <type>when-type</type>" +
                "               </when-validation>" +
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";
        editConfig(requestXml);

        // Verify GET response
        String response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "  <data>"
                        + "   <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                        + "    <validation:xml-subtree>"
                        + "     <validation:plugType>PLUG-1.0</validation:plugType>"
                        + "     <validation:schemaMountPoint>"
                        + "      <smt:schemaMount xmlns:smt=\"schema-mount-test\">"
                        + "      <smt:when-validation>"
                        + "       <smt:name>when1</smt:name>"
                        + "       <smt:type>when-type</smt:type>"
                        + "      </smt:when-validation>"
                        + "      <smt:when-validation>"
                        + "       <smt:name>when2</smt:name>"
                        + "       <smt:type>when-type</smt:type>"
                        + "      </smt:when-validation>"
                        + "     </smt:schemaMount>"
                        + "     </validation:schemaMountPoint>"
                        + "    </validation:xml-subtree>"
                        + "   </validation:validation>"
                        + "  </data>"
                        + " </rpc-reply>";
        verifyGet(m_server, m_clientInfo, response);

        // Try to create one more LIST with-out type node --> When constraint should be failed
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "               <when-validation>" +
                "                 <name>when3</name>" +
                "               </when-validation>" +
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";

        // Verify Error path points to exact LIST entry 'when3'--> smt:when-validation[smt:name='when3']
        NetConfResponse netconfResponse = editConfigAsFalse(requestXml);
        assertEquals(1, netconfResponse.getErrors().size());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/smt:schemaMount/smt:when-validation[smt:name='when3']", netconfResponse.getErrors().get(0).getErrorPath());
        assertEquals("Violate when constraints: type = 'when-type'", netconfResponse.getErrors().get(0).getErrorMessage());
        assertEquals(NetconfRpcErrorTag.UNKNOWN_ELEMENT, netconfResponse.getErrors().get(0).getErrorTag());
    }

    @Test
    public void testMissingMandatoryNodes() throws Exception {
        String request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <smt:ber-tca-profiles xmlns:smt=\"schema-mount-test\">" +
                        "     <smt:ber-tca-profile>" +
                        "        <smt:name>profile</smt:name>" +
                        "        <smt:sf-threshold>4</smt:sf-threshold>" +
                        "     </smt:ber-tca-profile>" +
                        "    </smt:ber-tca-profiles>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>";
        NetConfResponse netconfResponse = editConfigAsFalse(request);
        assertEquals(1, netconfResponse.getErrors().size());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1" +
                        ".0']/validation:schemaMountPoint/smt:ber-tca-profiles/smt:ber-tca-profile[smt:name='profile']/smt:sd-threshold",
                netconfResponse.getErrors().get(0).getErrorPath());
        assertEquals("Missing mandatory node - sd-threshold", netconfResponse.getErrors().get(0).getErrorMessage());
        assertEquals(NetconfRpcErrorTag.DATA_MISSING, netconfResponse.getErrors().get(0).getErrorTag());
        assertEquals("instance-required", netconfResponse.getErrors().get(0).getErrorAppTag());
    }

    @Test
    public void testMissingMandatoryNodesOnMustConstraint() throws Exception {
        String request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <smt:ber-tca-profiles xmlns:smt=\"schema-mount-test\">" +
                        "     <smt:ber-tca-profile xc:operation=\"create\">" +
                        "        <smt:name>profile</smt:name>" +
                        "        <smt:sd-threshold>4</smt:sd-threshold>" +
                        "     </smt:ber-tca-profile>" +
                        "    </smt:ber-tca-profiles>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>";

        NetConfResponse netconfResponse = editConfigAsFalse(request);
        assertEquals(1, netconfResponse.getErrors().size());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1" +
                        ".0']/validation:schemaMountPoint/smt:ber-tca-profiles/smt:ber-tca-profile[smt:name='profile']/smt:sf-threshold",
                netconfResponse.getErrors().get(0).getErrorPath());
        assertEquals("Mandatory leaf 'sf-threshold' is missing", netconfResponse.getErrors().get(0).getErrorMessage());
        assertEquals(NetconfRpcErrorTag.DATA_MISSING, netconfResponse.getErrors().get(0).getErrorTag());

    }

    @Test
    public void testMustConstraintWithMandatoryNodes_ImpactValidation() throws Exception {
        String request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <smt:ber-tca-profiles xmlns:smt=\"schema-mount-test\">" +
                        "     <smt:ber-tca-profile xc:operation=\"create\">" +
                        "        <smt:name>profile</smt:name>" +
                        "        <smt:sd-threshold>6</smt:sd-threshold>" +
                        "        <smt:sf-threshold>4</smt:sf-threshold>" +
                        "     </smt:ber-tca-profile>" +
                        "    </smt:ber-tca-profiles>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>";

        editConfig(request);

        String response = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
                "   <data>\n" +
                "      <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">\n" +
                "         <validation:xml-subtree>\n" +
                "            <validation:plugType>PLUG-1.0</validation:plugType>\n" +
                "            <schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">\n" +
                "               <smt:ber-tca-profiles xmlns:smt=\"schema-mount-test\">\n" +
                "                  <smt:ber-tca-profile>\n" +
                "                     <smt:name>profile</smt:name>\n" +
                "                     <smt:sd-threshold>6</smt:sd-threshold>\n" +
                "                     <smt:sf-threshold>4</smt:sf-threshold>\n" +
                "                  </smt:ber-tca-profile>\n" +
                "               </smt:ber-tca-profiles>\n" +
                "            </schemaMountPoint>\n" +
                "         </validation:xml-subtree>\n" +
                "      </validation:validation>\n" +
                "   </data>\n" +
                "</rpc-reply>";

        verifyGet(m_server, m_clientInfo, response);

        request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <smt:ber-tca-profiles xmlns:smt=\"schema-mount-test\">" +
                        "     <smt:ber-tca-profile>" +
                        "        <smt:name>profile</smt:name>" +
                        "        <smt:sf-threshold xc:operation=\"remove\">4</smt:sf-threshold>" +
                        "     </smt:ber-tca-profile>" +
                        "    </smt:ber-tca-profiles>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>";

        NetConfResponse netconfResponse = editConfigAsFalse(request);
        assertEquals(1, netconfResponse.getErrors().size());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1" +
                        ".0']/validation:schemaMountPoint/smt:ber-tca-profiles/smt:ber-tca-profile[smt:name='profile']/smt:sd-threshold",
                netconfResponse.getErrors().get(0).getErrorPath());
        assertEquals("The sd-threshold must be lower than the sf-threshold value (i.e., x > y.)",
                netconfResponse.getErrors().get(0).getErrorMessage());
        assertEquals("must-violation", netconfResponse.getErrors().get(0).getErrorAppTag());
        assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, netconfResponse.getErrors().get(0).getErrorTag());
    }

    @Test
    public void testMustConstraintWithMandatoryNodes_ImpactValidation1() throws Exception {
        String request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <smt:ber-tca-profiles xmlns:smt=\"schema-mount-test\">" +
                        "     <smt:ber-tca-profile xc:operation=\"create\">" +
                        "        <smt:name>profile</smt:name>" +
                        "        <smt:sd-threshold>6</smt:sd-threshold>" +
                        "        <smt:sf-threshold>4</smt:sf-threshold>" +
                        "     </smt:ber-tca-profile>" +
                        "    </smt:ber-tca-profiles>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>";

        editConfig(request);

        String response = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
                "   <data>\n" +
                "      <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">\n" +
                "         <validation:xml-subtree>\n" +
                "            <validation:plugType>PLUG-1.0</validation:plugType>\n" +
                "            <schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">\n" +
                "               <smt:ber-tca-profiles xmlns:smt=\"schema-mount-test\">\n" +
                "                  <smt:ber-tca-profile>\n" +
                "                     <smt:name>profile</smt:name>\n" +
                "                     <smt:sd-threshold>6</smt:sd-threshold>\n" +
                "                     <smt:sf-threshold>4</smt:sf-threshold>\n" +
                "                  </smt:ber-tca-profile>\n" +
                "               </smt:ber-tca-profiles>\n" +
                "            </schemaMountPoint>\n" +
                "         </validation:xml-subtree>\n" +
                "      </validation:validation>\n" +
                "   </data>\n" +
                "</rpc-reply>";

        verifyGet(m_server, m_clientInfo, response);

        request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <smt:ber-tca-profiles xmlns:smt=\"schema-mount-test\">" +
                        "     <smt:ber-tca-profile>" +
                        "        <smt:name>profile</smt:name>" +
                        "        <smt:sd-threshold xc:operation=\"remove\">6</smt:sd-threshold>" +
                        "     </smt:ber-tca-profile>" +
                        "    </smt:ber-tca-profiles>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>";

        NetConfResponse netconfResponse = editConfigAsFalse(request);
        assertEquals(1, netconfResponse.getErrors().size());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1" +
                        ".0']/validation:schemaMountPoint/smt:ber-tca-profiles/smt:ber-tca-profile[smt:name='profile']/smt:sd-threshold",
                netconfResponse.getErrors().get(0).getErrorPath());
        assertEquals("Missing mandatory node - sd-threshold",
                netconfResponse.getErrors().get(0).getErrorMessage());
        assertEquals("instance-required", netconfResponse.getErrors().get(0).getErrorAppTag());
        assertEquals(NetconfRpcErrorTag.DATA_MISSING, netconfResponse.getErrors().get(0).getErrorTag());
    }

    @Test
    public void testMustConstraintWithEmptyLeaf() throws Exception {
        String request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <smt:interface-container xmlns:smt=\"schema-mount-test\">" +
                        "     <smt:interface-list>" +
                        "        <smt:name>interface</smt:name>" +
                        "        <smt:inline-frame-processing>"+
                        "          <smt:ingress-rule>"+
                        "            <smt:rule>"+
                        "               <smt:rule-name>rule1</smt:rule-name>"+
                        "               <smt:priority>1</smt:priority>"+
                        "               <smt:flexible-match>"+
                        "                 <smt:match-criteria>"+
                        "                   <smt:ipv6-multicast-address/>"+
                        "                 </smt:match-criteria>"+
                        "               </smt:flexible-match>"+
                        "            </smt:rule>"+
                        "            <smt:rule>"+
                        "               <smt:rule-name>rule2</smt:rule-name>"+
                        "               <smt:priority>2</smt:priority>"+
                        "               <smt:flexible-match>"+
                        "                 <smt:match-criteria>"+
                        "                   <smt:ipv4-multicast-address/>"+
                        "                 </smt:match-criteria>"+
                        "               </smt:flexible-match>"+
                        "            </smt:rule>"+
                        "          </smt:ingress-rule>"+
                        "        </smt:inline-frame-processing>"+
                        "     </smt:interface-list>" +
                        "    </smt:interface-container>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>";

        editConfig(request);

        // create one more rule entry
        request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <smt:interface-container xmlns:smt=\"schema-mount-test\">" +
                        "     <smt:interface-list>" +
                        "        <smt:name>interface</smt:name>" +
                        "        <smt:inline-frame-processing>"+
                        "          <smt:ingress-rule>"+
                        "            <smt:rule>"+
                        "               <smt:rule-name>rule3</smt:rule-name>"+
                        "               <smt:priority>3</smt:priority>"+
                        "               <smt:flexible-match>"+
                        "                 <smt:match-criteria>"+
                        "                   <smt:ipv6-multicast-address/>"+
                        "                 </smt:match-criteria>"+
                        "               </smt:flexible-match>"+
                        "            </smt:rule>"+
                        "          </smt:ingress-rule>"+
                        "        </smt:inline-frame-processing>"+
                        "     </smt:interface-list>" +
                        "    </smt:interface-container>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>";

        NetConfResponse netconfResponse = editConfigAsFalse(request);
        assertEquals(1, netconfResponse.getErrors().size());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/smt:interface-container/smt:interface-list[smt:name='interface']/smt:inline-frame-processing/smt:ingress-rule/smt:rule[smt:rule-name='rule3']/smt:flexible-match/smt:match-criteria",
                netconfResponse.getErrors().get(0).getErrorPath());
        assertEquals("Violate must constraints: not(boolean(current()/ipv6-multicast-address))\n" +
                "       or (count(current()/../../../../ingress-rule/rule) = 2)", netconfResponse.getErrors().get(0).getErrorMessage());
        assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, netconfResponse.getErrors().get(0).getErrorTag());
        assertEquals("must-violation", netconfResponse.getErrors().get(0).getErrorAppTag());

    }

    @Test
    public void testMustConstraintWithDotOnDerivedFromOrSelfIdentity() throws Exception {
        String request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <smt:interface-container xmlns:smt=\"schema-mount-test\">" +
                        "     <smt:interface-list>" +
                        "        <smt:name>interface</smt:name>" +
                        "        <smt:bonding-group>" +
                        "         <smt:bonded-interface-mode xmlns:test1=\"test-interfaces\">test1:mode-fast</smt:bonded-interface-mode>" +
                        "        </smt:bonding-group>" +
                        "     </smt:interface-list>" +
                        "    </smt:interface-container>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>";

        editConfig(request);

        // Verify get response
        String response = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
                "   <data>\n" +
                "      <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">\n" +
                "         <validation:xml-subtree>\n" +
                "            <validation:plugType>PLUG-1.0</validation:plugType>\n" +
                "            <schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">\n" +
                "               <smt:interface-container xmlns:smt=\"schema-mount-test\">\n" +
                "                  <smt:interface-list>\n" +
                "                     <smt:bonding-group>\n" +
                "                        <smt:bonded-interface-mode xmlns:if=\"test-interfaces\">if:mode-fast</smt:bonded-interface-mode>\n" +
                "                     </smt:bonding-group>\n" +
                "                     <smt:name>interface</smt:name>\n" +
                "                  </smt:interface-list>\n" +
                "               </smt:interface-container>\n" +
                "            </schemaMountPoint>\n" +
                "         </validation:xml-subtree>\n" +
                "      </validation:validation>\n" +
                "   </data>\n" +
                "</rpc-reply>";

        verifyGet(m_server, m_clientInfo, response);
        // modify invalid identity-ref value
        request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <smt:interface-container xmlns:smt=\"schema-mount-test\">" +
                        "     <smt:interface-list>" +
                        "        <smt:name>interface</smt:name>" +
                        "        <smt:bonding-group>" +
                        "         <smt:bonded-interface-mode xmlns:test1=\"test-interfaces\">test1:mode-vdsl</smt:bonded-interface-mode>" +
                        "        </smt:bonding-group>" +
                        "     </smt:interface-list>" +
                        "    </smt:interface-container>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>";

        NetConfResponse netconfResponse = editConfigAsFalse(request);
        assertEquals(1, netconfResponse.getErrors().size());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/" +
                        "smt:interface-container/smt:interface-list[smt:name='interface']/smt:bonding-group/smt:bonded-interface-mode",
                netconfResponse.getErrors().get(0).getErrorPath());
        assertEquals("bonded-interface-mode can only be set to mode-fast", netconfResponse.getErrors().get(0).getErrorMessage());
        assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, netconfResponse.getErrors().get(0).getErrorTag());
        assertEquals("must-violation", netconfResponse.getErrors().get(0).getErrorAppTag());
    }

    @Test
    public void testMustConstraintWithDotOnBooleanFunction() throws Exception {
        String request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <smt:interface-container xmlns:smt=\"schema-mount-test\">" +
                        "     <smt:interface-list>" +
                        "        <smt:name>interface</smt:name>" +
                        "        <smt:bonding-group>" +
                        "         <smt:bonded-interface-enabled>false</smt:bonded-interface-enabled>" +
                        "        </smt:bonding-group>" +
                        "     </smt:interface-list>" +
                        "    </smt:interface-container>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>";

        editConfig(request);

        // Verify get response
        String response = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
                "   <data>\n" +
                "      <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">\n" +
                "         <validation:xml-subtree>\n" +
                "            <validation:plugType>PLUG-1.0</validation:plugType>\n" +
                "            <schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">\n" +
                "               <smt:interface-container xmlns:smt=\"schema-mount-test\">\n" +
                "                  <smt:interface-list>\n" +
                "                     <smt:bonding-group>\n" +
                "                        <smt:bonded-interface-enabled>false</smt:bonded-interface-enabled>\n" +
                "                     </smt:bonding-group>\n" +
                "                     <smt:name>interface</smt:name>\n" +
                "                  </smt:interface-list>\n" +
                "               </smt:interface-container>\n" +
                "            </schemaMountPoint>\n" +
                "         </validation:xml-subtree>\n" +
                "      </validation:validation>\n" +
                "   </data>\n" +
                "</rpc-reply>";

        verifyGet(m_server, m_clientInfo, response);
    }

    @Test
    public void testMustConstraintDotWithCoreOperation() throws Exception {

        // Verify dot with core operation ==> . < current()/../bonded-interface-high-speed
        String request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <smt:interface-container xmlns:smt=\"schema-mount-test\">" +
                        "     <smt:interface-list>" +
                        "        <smt:name>interface</smt:name>" +
                        "        <smt:bonding-group>" +
                        "         <smt:bonded-interface-low-speed>50</smt:bonded-interface-low-speed>" +
                        "         <smt:bonded-interface-high-speed>200</smt:bonded-interface-high-speed>" +
                        "        </smt:bonding-group>" +
                        "     </smt:interface-list>" +
                        "    </smt:interface-container>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>";

        editConfig(request);

        // Verify get response
        String response = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
                "   <data>\n" +
                "      <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">\n" +
                "         <validation:xml-subtree>\n" +
                "            <validation:plugType>PLUG-1.0</validation:plugType>\n" +
                "            <schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">\n" +
                "               <smt:interface-container xmlns:smt=\"schema-mount-test\">\n" +
                "                  <smt:interface-list>\n" +
                "                     <smt:bonding-group>\n" +
                "                        <smt:bonded-interface-low-speed>50</smt:bonded-interface-low-speed>\n" +
                "                        <smt:bonded-interface-high-speed>200</smt:bonded-interface-high-speed>\n" +
                "                     </smt:bonding-group>\n" +
                "                     <smt:name>interface</smt:name>\n" +
                "                  </smt:interface-list>\n" +
                "               </smt:interface-container>\n" +
                "            </schemaMountPoint>\n" +
                "         </validation:xml-subtree>\n" +
                "      </validation:validation>\n" +
                "   </data>\n" +
                "</rpc-reply>";

        verifyGet(m_server, m_clientInfo, response);

        // Impact validation
        request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <smt:interface-container xmlns:smt=\"schema-mount-test\">" +
                        "     <smt:interface-list>" +
                        "        <smt:name>interface</smt:name>" +
                        "        <smt:bonding-group>" +
                        "         <smt:bonded-interface-high-speed>36</smt:bonded-interface-high-speed>" +
                        "        </smt:bonding-group>" +
                        "     </smt:interface-list>" +
                        "    </smt:interface-container>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>";

        NetConfResponse netconfResponse = editConfigAsFalse(request);
        assertEquals(1, netconfResponse.getErrors().size());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/" +
                        "smt:interface-container/smt:interface-list[smt:name='interface']/smt:bonding-group/smt:bonded-interface-low-speed",
                netconfResponse.getErrors().get(0).getErrorPath());
        assertEquals("Violate must constraints: . < current()/../bonded-interface-high-speed",
                netconfResponse.getErrors().get(0).getErrorMessage());
        assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, netconfResponse.getErrors().get(0).getErrorTag());
        assertEquals("must-violation", netconfResponse.getErrors().get(0).getErrorAppTag());
    }
}
